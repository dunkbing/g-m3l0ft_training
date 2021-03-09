using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Net.Sockets;
using System.Net;
using System.Collections;
using Utils;

namespace BuildMonitor
{
    public partial class MainForm : Form
    {
        private bool m_isAutoRefreshing = false;
        private System.Threading.Timer  m_discoverTimer;
        private int m_timerMs = 60000;

        private Socket      m_socket;
        private IPEndPoint  m_iep;

        private Thread      m_discoverThread = null;
        private bool        m_stopDiscoverThread = false;

        private bool m_isHidden = false;

        private ServiceListControl m_serviceControl = null;

        public MainForm()
        {
            InitializeComponent();

            m_stopDiscoverThread = false;
            DiscoverDistccHostsStart();

            //Create the timer for the regular updates, but don't turn it on
            TimerCallback callback = new TimerCallback(SendDiscoverSignal);
            m_discoverTimer = new System.Threading.Timer(callback, 0, 0, Timeout.Infinite);
        }

        private void MainForm_Closing(object sender, EventArgs e)
        {
            DiscoverDistccHostsStop();
        }

        private void MainForm_Load(object sender, EventArgs e)
        {
            VersionLabel.Text = Globals.s_versionService;
            m_serviceControl = new ServiceListControl(this.dataGridView);
        }

        private void UpdateServiceList()
        {

        }

        private void SystemTrayIcon_MouseClick(object sender, MouseEventArgs e)
        {
            if (this.m_isHidden)
            {
                ShowWindow();
            }
            else
            {
                HideWindow();
            }
        }

        private void HideWindow()
        {
            m_isHidden = true;
            this.ShowInTaskbar = false;
            this.Hide();
        }

        private void ShowWindow()
        {
            m_isHidden = false;
            this.ShowInTaskbar = true;
            this.Show();
            WindowState = FormWindowState.Normal;
        }

        private void MainForm_Resize(object sender, EventArgs e)
        {
            if (FormWindowState.Minimized == this.WindowState)
            {
                HideWindow();
            }
        }

        /// <summary>
        /// SendDiscoverSignal(): This will send UDP (broadcast) messages
        ///   to inform all the computers in the network about 
        ///   the availability of this computer.
        ///   This function is used as a callback for a timer.
        /// </summary>
        static void SendDiscoverSignal(Object state)
        {
            Socket sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
            IPEndPoint iep1 = new IPEndPoint(IPAddress.Broadcast, Globals.s_discoverPort);
            string hostname = Dns.GetHostName();
            byte[] data = Encoding.ASCII.GetBytes(hostname);
            sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.Broadcast, 1);
            sock.SendTo(data, iep1);
            sock.Close();
        }

        private void ToggleAutoRefresh()
        {
            if (!m_isAutoRefreshing)
            {
                m_isAutoRefreshing = true;
                m_discoverTimer.Change(0, m_timerMs);
                
            }
            else
            {
                m_isAutoRefreshing = false;
                //This disables the timer
                m_discoverTimer.Change(Timeout.Infinite, Timeout.Infinite);
            }
        }
        
        private void DiscoverDistccHostsStart()
        {
            if (m_discoverThread == null)
            {
                try
                {
                    m_socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                    m_socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);

                    IPAddress _ipAddress = IPAddress.Parse(Globals.GetLocalIP());
                    if (_ipAddress != null)
                    {
                        m_iep = new IPEndPoint(_ipAddress, Globals.s_dataPort);
                    }
                    else
                    {
                        MessageBox.Show("You do not have an ip address!!!");
                        return;
                    }
                    m_socket.Bind(m_iep);

                }
                catch (Exception e)
                {

                    String errMsg = "DiscoverDistccHostsStart: " + "\r\n" + e.ToString();
                    if (Globals.s_workInConsole)
                    {
                        Console.WriteLine(errMsg);
                    }
                    else
                    {
                        MessageBox.Show(errMsg, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                    return;
                }

                m_discoverThread = new Thread(new ThreadStart(DiscoverDistccHostsWorkerThread));
            }

            if (m_discoverThread.ThreadState != System.Threading.ThreadState.Running)
                m_discoverThread.Start();
        }

        void DiscoverDistccHostsWorkerThread()
        {
            while (true)
            {
                if (m_stopDiscoverThread)
                    return;
                try
                {
                    if (m_socket.Available > 0)
                    {
                        EndPoint ep = (EndPoint)m_iep;

                        byte[] data = new byte[1024];
                        int recv = m_socket.ReceiveFrom(data, ref ep);
                        String[] stringData = Globals.DeSerializeInfoParams( Encoding.ASCII.GetString(data, 0, recv));

                        m_serviceControl.AddRow(stringData[0].GetHashCode(), stringData);

                        //Console.WriteLine("Received Data: " + stringData);
                    }
                }
                catch (Exception e)
                {
                    String errMsg = e.StackTrace.ToString();

                    Console.WriteLine(errMsg);
                }

                Thread.Sleep(1000);
            }
        }

        private void DiscoverDistccHostsStop()
        {
            if (m_discoverTimer != null)
            {
                m_discoverTimer.Dispose();
                m_discoverTimer = null;
            }

            if (m_discoverThread != null)
            {
                m_stopDiscoverThread = true;
                m_discoverThread.Join();
                m_stopDiscoverThread = false;
            }

            if (m_socket != null)
            {
                m_socket.Close();
                m_socket = null;
            }

            m_iep = null;
            m_discoverThread = null;
        }

        private void RefreshButton_Click(object sender, EventArgs e)
        {
            //NOTE: Parameter is ignored
            SendDiscoverSignal( this);
        }

        private void autoRefreshCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            ToggleAutoRefresh();
        }

    }
      
}