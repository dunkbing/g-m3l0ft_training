using System;
using System.Reflection;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.ServiceProcess;
using System.Text;
using System.Threading;
using System.IO;
using System.Net.Sockets;
using System.Net;
using Utils;

namespace GLLegacyService
{
    //class DistccHost

    //class for Service
    public partial class Service : ServiceBase
    {
        /**
        * SERVICE_STATE - enum with the states of the service
        */
        enum SERVICE_STATE
        {
            STOPPED,
            RUNNING,
            PAUSED,
        }

        /**
        * s_state - a SERVICE_STATE with the current state of the service
        */
        static SERVICE_STATE s_state = SERVICE_STATE.STOPPED;

        /**
        * s_checkDistccHostsFile - timer for checking distcc temp file (GLLegacyDisccTemp.txt)
        */
        static Timer s_checkDistccHostsFile = null;
        /**
        * s_checkDistccHostsFile - timer for checking distcc.exe is started 
        */
        static Timer s_checkDistccd = null;
        /**
        * s_checkDistccHostsFile - timer for checking the size of GLLegacyService.txt, if is too large to delete it
        */
        static Timer s_checkLogFileSize = null;
        /**
        * s_cpuCounter is a PerformanceCounter 
        */
        static PerformanceCounter s_cpuCounter = null;
        /**
        * s_DistccHostFileDataTime is a DateTime to keep the time distcc temp file (GLLegacyDisccTemp.txt) was modified to know if we read it again.
        */
        static DateTime s_DistccHostFileDataTime;
        /**
        * s_nValidHosts a int that keep the number of valid hosts
        */
        static int    s_nValidHosts = 0;
        /**
        * m_Hosts is a DisccHost array to keep the list with hosts (load from GLLegacyDisccTemp.txt)
        */
        static DisccHost[] m_Hosts = null ;
        /**
        * s_sockService is a Socket used by service
        */
        static Socket s_sockService = null;
        /**
        * s_iep is a IPEndPoint used by service
        */
        static IPEndPoint s_iep = null;
        /**
        * m_threadDistccDiscover is a Thread for discover distcc hosts, thread that listen for messages
        */
        static Thread m_threadDistccDiscover = null;
        /**
        * m_threadStopRequest an int set on 1 when we want to stop discover thread (m_threadDistccDiscover )
        */
        static int m_threadStopRequest = 0;
        /**
        * s_mutex is a Mutex for writing in files
        */
        private static Mutex s_mutex = new Mutex();

        /**
        * Log - write a message in a file
        *@param1 message is a string, the message to be written 
        *@param2 fileName is the name of the file to write in
        */
        static void Log(String message, String fileName)
        {
            s_mutex.WaitOne();

            try
            {
                
                StreamWriter log = new StreamWriter(fileName, true);
                if (log == null) return;
                log.WriteLine(message);
                log.Close();
                log.Dispose();
                log = null;
                
            }
            catch(Exception)
            {
                //MessageBox.Show(ex.StackTrace.ToString());
            }

            s_mutex.ReleaseMutex();
        }

        /**
        * Service - class constructor
        */
        public Service()
        {
            InitializeComponent();

            this.ServiceName = "GLLegacy Service";
            this.EventLog.Log = "Application";

            s_cpuCounter = new PerformanceCounter();

            s_cpuCounter.CategoryName = "Processor";
            s_cpuCounter.CounterName = "% Processor Time";
            s_cpuCounter.InstanceName = "_Total";

            // These Flags set whether or not to handle that specific
            //  type of event. Set to true if you need it, false otherwise.
            this.CanHandlePowerEvent = true;
            this.CanHandleSessionChangeEvent = true;
            this.CanPauseAndContinue = true;
            this.CanShutdown = true;
            this.CanStop = true;


            /*String str = Globals.GetRegValue("SYSTEM\\CurrentControlSet\\services\\GLLegacyService", "ImagePath");
            if (str != null)
            {
                str = str.Replace('"', ' ');
                str = str.Trim();
            }
            else
            {
                str = "C:\\";
            }*/

            Globals.s_logFileName = Globals.GetFullPathToGLLegacyServiceLogFile();//Path.Combine(Path.GetDirectoryName(str), Globals.s_logFileName);

        }

        /**
         * OnStart - start the service thread and set the state on RUNNING
         */
        protected override void OnStart(string[] args)
        {
            base.OnStart(args);

            s_state = SERVICE_STATE.RUNNING;

            Log("Service started on : " + DateTime.Now.ToString(), Globals.s_logFileName);

            thr.Start();
        }

        Thread thr = new Thread(new ThreadStart(run));


        /**
         * OnStop - stop the service thread and set the state on STOPPED
         */
        protected override void OnStop()
        {
            s_state = SERVICE_STATE.STOPPED;
            thr.Abort();

            Log("Service stoped on : " + DateTime.Now.ToString(), Globals.s_logFileName);
            
            thr.Abort();

            Thread.Sleep(1000);

            base.OnStop();
        }

        /**
         * OnPause - stop the service thread and set the state on STOPPED
         */
        protected override void OnPause()
        {

            s_state = SERVICE_STATE.PAUSED;

            Thread.Sleep(1000);

            base.OnPause();
        }

        /**
         * OnContinue - set the state of service on RUNNING
         */
        protected override void OnContinue()
        {
            s_state = SERVICE_STATE.RUNNING;

            Thread.Sleep(1000);

            base.OnContinue();
        }

        /**
        * OnShutdown()- Called when the System is shutting down
        *             - Put code here when you need special handling of code that deals with a system shutdown, such
        *               as saving special data before shutdown.
        */
        protected override void OnShutdown()
        {
            s_state = SERVICE_STATE.STOPPED;
            thr.Abort();

            Thread.Sleep(1000);

            base.OnShutdown();
        }

        /**
        * OnCustomCommand() - If you need to send a command to your service without the need for Remoting or Sockets, use
        *                     this method to do custom methods.
        * @param command - Arbitrary Integer between 128 & 256                    
        */
        protected override void OnCustomCommand(int command)
        {
            //  A custom command can be sent to a service by using this method:
            //#  int command = 128; //Some Arbitrary number between 128 & 256
            //#  ServiceController sc = new ServiceController("NameOfService");
            //#  sc.ExecuteCommand(command);

            base.OnCustomCommand(command);
        }

        /**
        * OnPowerEvent- Useful for detecting power status changes, such as going into Suspend mode or Low Battery for laptops.
        * @param powerStatus - The Power Broadcast Status(BatteryLow, Suspend, etc.)
        */
        protected override bool OnPowerEvent(PowerBroadcastStatus powerStatus)
        {
            return base.OnPowerEvent(powerStatus);
        }

        /**
        * OnSessionChange - To handle a change event from a Terminal Server session.
        *                 - Useful if you need to determine when a user logs in remotely or logs off,
        *                    or when someone logs into the console.
        * @param changeDescription - the Session Change Event that occured
        */ 
        protected override void OnSessionChange(SessionChangeDescription changeDescription)
        {
            base.OnSessionChange(changeDescription);
        }

        /**
         * TimerCheckDisccTempFile - callback function called call by timer
         *                         - check if GLLegacyService.txt size is greater than 10Mb, and delete it if it is greater
         * @param StateObj - not used
         */
        private static void TimerCheckLogFile(object StateObj)
        {
            try
            {
                FileInfo f = new FileInfo(Globals.s_logFileName);
                if (f.Length > 10485760) //10Mb
                {
                    File.Delete(Globals.s_logFileName);
                }
            }
            catch (System.Exception e)
            {
                Log("TimerCheckLogFile EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }
        }

        /**
         * TimerCheckDisccTempFile - callback function called every minute by timer
         *                         - check if GLLegacyDisccTemp.txt was modified for reading again and reaload list of hosts
         *                         - set valid hosts on 0. 
         * @param StateObj - not used
         */
        private static void TimerCheckDisccTempFile(object StateObj)
        {
            try
            {
                String temp = Globals.GetFullPathToDistccTempFile();
                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " check if modified: " + temp, Globals.s_logFileName);
                // Create new FileInfo object and get the Length.
                if (!File.Exists(temp))
                {
                    Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " Run GLLegacyInstaller. File not exists: " + temp, Globals.s_logFileName);
                    return;
                }

                FileInfo f = new FileInfo(temp);
                DateTime dt = f.LastWriteTime;
                //Log("discctempfile dt: " + dt.ToString(), Globals.s_logFileName);
                //if the host file is modified make a new host array (m_Hosts)
                if (dt != s_DistccHostFileDataTime || (s_nValidHosts == 0 && m_Hosts != null))
                {
                    s_DistccHostFileDataTime = dt;

                    string distccIPUsed_old = Globals.s_distccIPUsed;
                    Globals.DISTCC_LoadDisccTempFile(true);
                    Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " Globals.s_distccIPUsed " + Globals.s_distccIPUsed + " Globals.s_distccDriveSelected: " + Globals.s_distccDriveSelected + " Globals.s_distccWantedHosts: " + Globals.s_distccWantedHosts, Globals.s_logFileName);
                    if (distccIPUsed_old != Globals.s_distccIPUsed ) 
                    {
                        //IP used changed
                        Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + "IP used changed", Globals.s_logFileName);
                        DiscoverDistccHostsStop();
                        DiscoverDistccHostsStart();
                    }
                    //delete the old hosts array if exists
                    if (m_Hosts != null)
                    {
                        //Log("delete the old hosts: " + m_Hosts.Length, Globals.s_logFileName);
                        for (int i = 0; i < m_Hosts.Length; i++)
                        {
                            if (m_Hosts[i].m_timerSendRequest != null)
                                m_Hosts[i].m_timerSendRequest.Dispose();
                            if (m_Hosts[i].m_valid && m_Hosts[i].m_timerDeleteHost != null)
                                m_Hosts[i].m_timerDeleteHost.Dispose();
                            m_Hosts[i] = null;
                        }
                        m_Hosts = null;
                    }
                    s_nValidHosts = 0;

                    if (string.IsNullOrEmpty(Globals.s_distccWantedHosts))
                    {
                        SaveValidHosts();
                        Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " No hosts set for discc. Run GLLegacyInstaller.", Globals.s_logFileName);
                        return;
                    }

                    m_Hosts = new DisccHost[Globals.s_hostsWanted.Length];
                    //Log("hosts nr : " + m_Hosts.Length.ToString(), Globals.s_logFileName);
                    
                    //Log("s_nValidHosts : " + s_nValidHosts.ToString(), Globals.s_logFileName);
                    for (int i = 0; i < Globals.s_hostsWanted.Length; i++)
                    {
                        m_Hosts[i] = new DisccHost(Globals.s_hostsWanted[i]);
                        //Log("hosts : " + i.ToString() + " " + m_Hosts[i].m_hostName, Globals.s_logFileName);
                        string ipAddress = Globals.GetIP(m_Hosts[i].m_hostName);
                        //Log("hosts : " + i.ToString() + " " + ipAddress, Globals.s_logFileName);
                        if (!string.IsNullOrEmpty(ipAddress))
                        {                            
                            TimerCallback callback = new TimerCallback(NewRequest);
                            m_Hosts[i].m_timerSendRequest = new System.Threading.Timer(callback, ipAddress, 1000, Globals.s_timeSendRequest);
                        }
                        else
                        {
                            Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " .ERROR !!! IP null.", Globals.s_logFileName);
                        }
                        //TimerCallback callback2 = new TimerCallback(SetInvalidHost);
                        //m_Hosts[i].m_timerDeleteHost = new System.Threading.Timer(callback2, m_Hosts[i].m_desc, 1000 + Globals.s_timeDeleteHost, 0);
                    }
                    SaveValidHosts();
                }                
            }
            catch (Exception e)
            {
                Log("TimerCheckDisccHostFile EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }
        }

        /**
         * TimerCheckDistccServerStatus - callback function call every 10 seconds by timer
         *                         - check if distcc is running
         *                         - if it is stopped start it again.
         * @param StateObj - not used
         */
        private static void TimerCheckDistccServerStatus(object StateObj)
        {
            try
            {
                string output = "";
                string error = "";

                Process[] pname = Process.GetProcessesByName(Globals.s_distccdProcessName);
                if ( Globals.s_distccServer == 1 )
                {
                    if (pname.Length == 0)
                    {
                        string path = System.Reflection.Assembly.GetEntryAssembly().Location;
                        path = path.Trim();
                        if (path[path.Length - 1] == '\\')
                            path = path.Substring(0, path.Length - 1);
                        path = Path.GetDirectoryName(path);
                        string cmd = Path.Combine(path, Globals.s_distccServerScript);


                        System.Diagnostics.Process proc = new System.Diagnostics.Process();
                        proc.StartInfo.WorkingDirectory = path + "\\";
                        proc.StartInfo.FileName = cmd;
                        proc.StartInfo.RedirectStandardError = false;
                        proc.StartInfo.RedirectStandardInput = false;
                        proc.StartInfo.RedirectStandardOutput = false;
                        proc.StartInfo.CreateNoWindow = true;
                        proc.StartInfo.UseShellExecute = true;//c78 this must be on true , otherwise the script will not work.
                        proc.StartInfo.Arguments = "";//" /c " + path + "\\" + Path.GetFileName(Globals.s_distccServerScript);

                        Log("distccd daemon STOPPED! trying to start it...", Globals.s_logFileName);
                        Log("cmd: " + cmd + " " + proc.StartInfo.Arguments, Globals.s_logFileName);

                        proc.Start();
                        //output = proc.StandardOutput.ReadToEnd();
                        //error = proc.StandardError.ReadToEnd();
                        proc.WaitForExit();

                        proc.Close();
                        proc.Dispose();
                        proc = null;
                    }


                    pname = Process.GetProcessesByName(Globals.s_distccdProcessName);
                    if (pname.Length == 0)
                    {
                        Log("ERROR: distccd cannot be started!! \r\nStandardOutput: \r\n " + output + "\r\nStandardError: \r\n " + error, Globals.s_logFileName);

                        //Globals.s_distccServer = 0;
                    }
                    else
                    {
                        //Globals.s_distccServer = 1;
                    }
                }
                else
                {
                    if (pname.Length != 0)
                    {
                        foreach (System.Diagnostics.Process p in pname)
                        {
                            Log("kill process..." + p.ProcessName, Globals.s_logFileName);
                            
                            p.Kill();   
                        }                        
                    }
                }

            }
            catch (Exception e)
            {
                Log("TimerCheckDistccServerStatus EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }
        }

        /**
         * DiscoverDistccHostsStart - start a new thread for discovering hosts
         */
        static void DiscoverDistccHostsStart()
        {
            if (m_threadDistccDiscover == null)
                m_threadDistccDiscover = new Thread(new ThreadStart(DiscoverDistccHostsWorkerThread));

            if (m_threadDistccDiscover.ThreadState != System.Threading.ThreadState.Running)
            {
                m_threadDistccDiscover.Start();
                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " DiscoverDistccHostsStart()", Globals.s_logFileName);
            }
        }

        /**
         * DiscoverDistccHostsStop - stop the thread that discover hosts
         */
        static void DiscoverDistccHostsStop()
        {

            if (m_threadDistccDiscover != null)
            {
                m_threadStopRequest = 1;
                m_threadDistccDiscover.Join();
                m_threadStopRequest = 0;
            }

            if (s_sockService != null)
            {
                s_sockService.Close();
                s_sockService = null;
            }

            s_iep = null;
            m_threadDistccDiscover = null;
        }

        /**
         * run - GLLegacyService run function
         */
        static void run()
        {
            try
            {

                Log("===============================================", Globals.s_logFileName);
                s_checkDistccd = new Timer(TimerCheckDistccServerStatus, null, 1500, 10000);
                s_checkDistccHostsFile = new Timer(TimerCheckDisccTempFile, null, 0, 60000); //check for file modification every minute
                s_checkLogFileSize = new Timer(TimerCheckLogFile, null, 0, 60000*60*24); //check for file modification every minute


                //Log("m_Hosts: " + m_Hosts, Globals.s_logFileName);
                //m_Hosts = null;

                DiscoverDistccHostsStart();

                while (true)
                {
                    if (s_state == SERVICE_STATE.RUNNING)
                    {
                        //ListenForDiscoverSignal(Globals.s_discoverPort, Globals.s_dataPort);
                        ListenForDiscoverSignal();
                    }
                    else if (s_state == SERVICE_STATE.STOPPED)
                    {
                        DiscoverDistccHostsStop();
                        Log("Service main thread stopped...", Globals.s_logFileName);
                        break;
                    }

                    Thread.Sleep(1000);
                }

                s_checkDistccd.Dispose();
                s_checkDistccd = null;
                s_checkDistccHostsFile.Dispose();
                s_checkDistccHostsFile = null;
            }
            catch (Exception e)
            {
                Log("run EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }
        }

        /**
         * GetDataToSend - get data about host (host name, cores number, if is server or not, distcc drives, GLLegacyService version)
         * @return a byte array with data about host 
         */
        static byte[] GetDataToSend()
        {
            String[] infoParams = new String[Globals.s_infoParams.Length];
            byte[] data = null;

            for (int i = 0; i < Globals.s_infoParams.Length; i++)
            {
                if (Globals.s_infoParams[i] == "HOST")
                {
                    infoParams[i] = Dns.GetHostName();
                }
                else if (Globals.s_infoParams[i] == "CORES")
                {
                    infoParams[i] = Environment.ProcessorCount.ToString();
                }
                else if (Globals.s_infoParams[i] == "SERVER")
                {
                    infoParams[i] = Globals.s_distccServer.ToString();
                }
                else if (Globals.s_infoParams[i] == "DRIVE")
                {
                    //for (int j = 0; j < Globals.s_distccDrives.Count; j++)
                    foreach( string value in Globals.s_distccDrives )
                    {
                        if (!string.IsNullOrEmpty(value))
                            infoParams[i] += value + ";";
                    }
                }
                else if (Globals.s_infoParams[i] == "VER")
                {
                    string version = Assembly.GetExecutingAssembly().GetName().Version.ToString(); //Assembly.GetEntryAssembly().GetName().Version;
                    infoParams[i] = version;//Globals.s_versionService;
                }
                else if (Globals.s_infoParams[i] == "CPU_USAGE")
                {
                    float cpu_usage = 0;
                    try
                    {
                        cpu_usage = s_cpuCounter.NextValue();
                        //We need to do this in order to get the correct value for the counter
                        //See: http://stackoverflow.com/questions/278071/how-to-get-the-cpu-usage-in-c
                        System.Threading.Thread.Sleep(1000);
                        //Value is percent usage of CPU
                        cpu_usage = s_cpuCounter.NextValue();
                    }
                    catch (Exception)
                    {
                        cpu_usage = -1;
                    }

                    if (cpu_usage >= 0)
                        infoParams[i] = Convert.ToUInt16(Math.Round(cpu_usage)).ToString() + '%';
                    else
                        infoParams[i] = "null";
                }
            }

            data = Encoding.ASCII.GetBytes(Globals.SerializeInfoParams(infoParams));
            return data;
        }

        /**
         * SendHostInfo - send a Udp message with data about local host (get by GetDataToSend())
         * @param ipAddress - IP address send message to
         * @param portSendData - port to send 
         */
        static void SendHostInfo(String ipAddress, int portSendData)
        {
            IPAddress _ipAddress = null;
            Socket sock = null;
            IPEndPoint iep1 = null;

            try
            {
                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " SendHostInfo: " + ipAddress.ToString() + " port: " + portSendData.ToString(), Globals.s_logFileName);
                _ipAddress = IPAddress.Parse(ipAddress);
                sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
                iep1 = new IPEndPoint(_ipAddress, portSendData);

                sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.Broadcast, 1);
                sock.SendTo(GetDataToSend(), iep1);
                sock.Close();

            }
            catch (Exception e)
            {
                Log("SendHostInfo EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }

            _ipAddress = null;
            sock = null;
            iep1 = null;
        }

        /**
         * SaveValidHosts - save in GLLegacyDistccHosts.txt hosts list (GLLEGACY_DISTCC_USE_HOSTS) and if distcc is on or off (GLLEGACY_DISTCC) 
         */
        static void SaveValidHosts()
        {
            //Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " SaveValidHosts", Globals.s_logFileName);
            Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " SaveValidHosts: " + s_nValidHosts.ToString() + " StaticHosts " + Globals.s_distccStaticHostList, Globals.s_logFileName);
            if ( Globals.s_distccUse == 1 )
            {
                String str = Globals.s_distccStaticHostList;
                if (s_nValidHosts != 0)
                {
                    str += " ";
                    for (int i = 0; i < m_Hosts.Length; i++)
                        if (m_Hosts[i].m_valid)
                            str += m_Hosts[i].m_hostName + " ";
                }
                if (!string.IsNullOrEmpty(str))
                {
                    String errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_USE, "true", Globals.GetFullPathToDistccHostsBatchScript(), false);
                    errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_USE_HOSTS, str, Globals.GetFullPathToDistccHostsBatchScript(), true);
                }
                else
                {
                    String errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_USE, "false", Globals.GetFullPathToDistccHostsBatchScript(), false);
                    errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_USE_HOSTS, str, Globals.GetFullPathToDistccHostsBatchScript(), true);
                }
            }
            else
            {
                String errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_USE, "false", Globals.GetFullPathToDistccHostsBatchScript(), false);
                errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_USE_HOSTS, "", Globals.GetFullPathToDistccHostsBatchScript(), true);
            }
        }

        /**
         * SetInvalidHost - is a callback function called by a timer that set a host as invalid (if the host doesn't respond)
         * @param state keep the name of host to be set as invalid
         */
        static void SetInvalidHost(Object state)
        {
            s_mutex.WaitOne();         
            int i;
            s_nValidHosts--;
            Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " 1.Host invalid: " + (string)state + " s_nValidHosts: " + s_nValidHosts.ToString(), Globals.s_logFileName);
            //if (s_nValidHosts < 0) s_nValidHosts = 0;

            for ( i = 0; i < m_Hosts.Length; i++)
            {
                if (m_Hosts[i].m_hostName == (string)state)
                {
                    m_Hosts[i].m_valid = false;
                    //m_Hosts[i].m_timerDeleteHost.Dispose();
                    m_Hosts[i].m_timerDeleteHost = null;
                    TimerCallback callback1 = new TimerCallback(NewRequest);
                    string IP = Globals.GetIP(m_Hosts[i].m_hostName);
                    if (!string.IsNullOrEmpty(IP))
                    {
                        m_Hosts[i].m_timerSendRequest.Dispose();
                        m_Hosts[i].m_timerSendRequest = new System.Threading.Timer(callback1, IP, Globals.s_timeSendRequest, Globals.s_timeSendRequest);
                    }
                    else
                    {
                        Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " ERROR !!! IP null." , Globals.s_logFileName);
                    }
                }
            }
            SaveValidHosts();
            s_mutex.ReleaseMutex();
        }

        /**
         * NewRequest - is a callback function called by a timer to send a request to a host (send 5 Udp messages to be sure one will reach the destination)
         * @param state keep the IP of the host
         */
        static void NewRequest(Object state)
        {
            //String[] infoParams = new String[Globals.s_infoParams.Length];
            IPAddress _ipAddress = null;
            Socket sock = null;
            IPEndPoint iep1 = null;
            try
            {
                _ipAddress = IPAddress.Parse((string)state);
                sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
                iep1 = new IPEndPoint(_ipAddress, Globals.s_discoverPort);
                string hostname = Dns.GetHostName();
                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " NewRequest to hostname: " + (string)state, Globals.s_logFileName);
                byte[] data = Encoding.ASCII.GetBytes(hostname);
                sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.Broadcast, 1);
                sock.SendTo(data, iep1);
                for (int i = 0; i < 5; i++)
                {
                    //Log(DateTime.Now.ToString("HH:mm:ss tt ") + i + " NewRequest to hostname: " + (string)state, Globals.s_logFileName);
                    sock.SendTo(data, iep1);
                    Thread.Sleep(1000);
                }
                sock.Close();
                data = null;
            }
            catch (Exception e)
            {
                Log("NewRequest EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }
            _ipAddress = null;
            sock = null;
            iep1 = null;
        }


        /**
         * DiscoverDistccHostsWorkerThread - is the thread that receive udp messages
         *                                 - if the message is from a host from our list and it is server and it has our distcc drive in its lists of distcc drives 
         *                                 will reset its timer or set it as valid (if if was invalid)
         *                                 - set timers for delete and new request for host that message was sent from 
         */
        static void DiscoverDistccHostsWorkerThread()
        {
            try
            {
                s_sockService = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                s_sockService.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);

                //s_iep = new IPEndPoint(IPAddress.Any, Globals.s_dataPort);
                //s_sock.Bind(s_iep);
                //string IP = Globals.s_IPs[Globals.s_nIPUsed].ToString();

                IPAddress _ipAddress = null;
                if (string.IsNullOrEmpty(Globals.s_distccIPUsed))
                    _ipAddress = IPAddress.Parse(Globals.GetLocalIP());
                else
                    _ipAddress = IPAddress.Parse(Globals.s_distccIPUsed.ToString());
                if (_ipAddress != null)
                {
                    s_iep = new IPEndPoint(_ipAddress, Globals.s_dataPortService);
                    //s_iep = new IPEndPoint(IPAddress.Loopback, Globals.s_dataPort);
                }
                else
                {
                    Log("DiscoverDistccHostsWorkerThread You do not have an ip address!!!", Globals.s_logFileName);
                    s_sockService = null;
                }
                s_sockService.Bind(s_iep);


                while (true)
                {
                        if (m_threadStopRequest == 1)
                            return;

                        if (s_sockService != null && s_sockService.Available > 0)
                        {
                            //Log("DiscoverDistccHostsWorkerThread s_sock.Available" + s_sock1.Available.ToString(), Globals.s_logFileName);
                            EndPoint ep = (EndPoint)s_iep;

                            byte[] data = new byte[1024];
                            int recv = s_sockService.ReceiveFrom(data, ref ep);
                            string stringData = Encoding.ASCII.GetString(data, 0, recv);

                            //Log("ListenForData stringData: " + stringData, Globals.s_logFileName);
                            //s_sock1.Close();

                            String[] param = Globals.DeSerializeInfoParams(stringData);

                            string hostname = param[0];
                            //Log("ListenForData myname: " + Dns.GetHostName(), Globals.s_logFileName);
                            //Log("ListenForData hostname: " + hostname + " " + param[2], Globals.s_logFileName);
                            if (Dns.GetHostName() != hostname) //avoid to respond to localhost
                            {
                                //Log("ListenForData request received: " + stringData + " from: " + ep.ToString(), Globals.s_logFileName);

                                string temp = ep.ToString();
                                string ipAddress = temp.Substring(0, temp.IndexOf(':'));
                                if (string.IsNullOrEmpty(ipAddress))
                                {
                                    Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " ..ERROR !!! ipAddress null.", Globals.s_logFileName);
                                    continue;
                                }
                                int i;
                                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " ListenForData: " + ipAddress + " " + stringData, Globals.s_logFileName);

                                if ( m_Hosts != null ) 
                                for (i = 0; i < m_Hosts.Length; i++)
                                {
                                    //Log("ListenForData1 m_Hosts[i].m_hostName, m_Hosts[i].m_desc, hostname: " + m_Hosts[i].m_hostName + " " + m_Hosts[i].m_valid.ToString() + " " + hostname, Globals.s_logFileName);
                                    if (m_Hosts[i].m_hostName == hostname)
                                    {
                                        if (!m_Hosts[i].m_valid)
                                        {
                                            ///Log("ListenForData m_Hosts[i].m_desc, m_Hosts[i].m_desc, active: " + param[2] + " drives: " + param[3] + " hostname: "  + hostname, Globals.s_logFileName);
                                            //test if host has discc and a virtual drive that i selected
                                            if (param[2] == "1" && param[3].IndexOf(Globals.s_distccDriveSelected) >= 0 )
                                            {
                                                //Log("Host valid: " + m_Hosts[i].m_hostName, Globals.s_logFileName);
                                                //s_mutex.WaitOne();
                                                m_Hosts[i].m_valid = true;                                                
                                                s_nValidHosts++;
                                                SaveValidHosts();
                                                TimerCallback callback1 = new TimerCallback(NewRequest);
                                                m_Hosts[i].m_timerSendRequest.Dispose();
                                                m_Hosts[i].m_timerSendRequest = new Timer(callback1, ipAddress, Globals.s_timeSendRequest, 0);

                                                TimerCallback callback2 = new TimerCallback(SetInvalidHost);
                                                if( m_Hosts[i].m_timerDeleteHost != null )
                                                    m_Hosts[i].m_timerDeleteHost.Dispose();
                                                m_Hosts[i].m_timerDeleteHost = new Timer(callback2, hostname, Globals.s_timeSendRequest + Globals.s_timeDeleteHost, 0);
                                                //s_mutex.ReleaseMutex();
                                                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " Reset time for valid host: " + m_Hosts[i].m_hostName, Globals.s_logFileName);
                                                
                                            }
                                            else
                                            {
                                            }
                                        }
                                        else
                                        {
                                            //host valid
                                            if (param[2] == "1" && param[3].IndexOf(Globals.s_distccDriveSelected) >= 0)
                                            {
                                                TimerCallback callback1 = new TimerCallback(NewRequest);
                                                m_Hosts[i].m_timerSendRequest.Dispose();
                                                m_Hosts[i].m_timerSendRequest = new Timer(callback1, ipAddress, Globals.s_timeSendRequest, 0);
                                                TimerCallback callback2 = new TimerCallback(SetInvalidHost);
                                                if( m_Hosts[i].m_timerDeleteHost != null )//for sure
                                                    m_Hosts[i].m_timerDeleteHost.Dispose();
                                                m_Hosts[i].m_timerDeleteHost = new Timer(callback2, hostname, Globals.s_timeSendRequest + Globals.s_timeDeleteHost, 0);
                                                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " Reset time for valid host: " + m_Hosts[i].m_hostName, Globals.s_logFileName);
                                            }
                                            else
                                            {
                                                //host was valid and now is invalid
                                                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " 2.Host invalid: " + m_Hosts[i].m_hostName, Globals.s_logFileName);
                                                //s_mutex.WaitOne();
                                                m_Hosts[i].m_valid = false;                                                
                                                s_nValidHosts--;
                                                SaveValidHosts();
                                                TimerCallback callback1 = new TimerCallback(NewRequest);
                                                m_Hosts[i].m_timerSendRequest.Dispose();
                                                m_Hosts[i].m_timerSendRequest = new Timer(callback1, ipAddress, Globals.s_timeSendRequest, Globals.s_timeSendRequest);

                                                //TimerCallback callback2 = new TimerCallback(SetInvalidHost);
                                                if( m_Hosts[i].m_timerDeleteHost != null )
                                                    m_Hosts[i].m_timerDeleteHost.Dispose();
                                                //m_Hosts[i].m_timerDeleteHost = new Timer(callback2, hostname, Globals.s_timeSendRequest + Globals.s_timeDeleteHost, 0);
                                                //s_mutex.ReleaseMutex();
                                                Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " Reset time for invalid host: " + m_Hosts[i].m_hostName, Globals.s_logFileName);
                                            }
                                        }
                                        break;
                                    }                                   
                                }

                                data = null;
                            }
                        }
                        Thread.Sleep(1000);
                    }
               
            }
            catch (Exception e)
            {
                Log("DiscoverDistccHostsWorkerThread EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }

             
        }


        /**
        * ListenForDiscoverSignal - on the main thread listen on Globals.s_discoverPort for discover udp messages (are send by GLLegacyInstaller)
        *                         - it call SendHostInfo to send info about local host to sender on Globals.s_discoverPort and Globals.s_dataPortService
        */
        static void ListenForDiscoverSignal()
        {
            Socket sock = null;
            IPEndPoint iep = null;
            //EndPoint ep = null;
            byte[] data = new byte[1024];

            try
            {
                sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
                
                IPAddress _ipAddress = null;
                if (string.IsNullOrEmpty(Globals.s_distccIPUsed))
                {
                    _ipAddress = IPAddress.Parse(Globals.GetLocalIP());
                }
                else
                {
                    _ipAddress = IPAddress.Parse(Globals.s_distccIPUsed.ToString());
                }


                iep = new IPEndPoint(_ipAddress, Globals.s_discoverPort); //or IPAddress.Any
                sock.Bind(iep);
                //Log("ListenForDiscoverSignal sock.Available: " + sock.Available, Globals.s_logFileName);
                //if (sock.Available > 0)
                {
                    // Creates an IpEndPoint to capture the identity of the sending host.
                    IPEndPoint sender = new IPEndPoint(IPAddress.Any, Globals.s_discoverPort);
                    EndPoint senderRemote = (EndPoint)sender;

                    int recv = sock.ReceiveFrom(data, ref senderRemote);
                    string stringData = Encoding.ASCII.GetString(data, 0, recv);
                    //String[] stringData = Globals.DeSerializeInfoParams(Encoding.ASCII.GetString(data, 0, recv));
                    //Log("ListenForDiscoverSignal stringData: " + stringData, Globals.s_logFileName);

                    sock.Close();

                    string hostname = Dns.GetHostName();
                    //Log("ListenForDiscoverSignal hostname: " + hostname, Globals.s_logFileName);
                    if (hostname != stringData) //avoid to respond to localhost  //just for test
                    {
                        Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " ListenForDiscoverSignal and request received: " + stringData + " from: " + senderRemote.ToString(), Globals.s_logFileName);
                        //if(stringData.Length > 1)
                        //  Log(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss tt") + " ListenForDiscoverSignal and request received: " + stringData[1] + " from: " + ep.ToString(), Globals.s_logFileName);

                        string temp = senderRemote.ToString();
                        //Log("stringData.Length: " + stringData.Length, Globals.s_logFileName);                    
                        //if(stringData.Length > 1)
                        //    ipAddress = stringData[1];
                        //else
                        string ipAddress = temp.Substring(0, temp.IndexOf(':'));

                        //if (Globals.s_distccServer != 0)
                        SendHostInfo(ipAddress, Globals.s_dataPort);
                        SendHostInfo(ipAddress, Globals.s_dataPortService);
                    }
                }

               // iep = new IPEndPoint(_ipAddress, Globals.s_dataPort);
               // sock.Bind(iep);

            }
            catch(Exception e)
            {
                Log("ListenForDiscoverSignal EXCEPTION: " + e.ToString() + "\r\n Callstack: \r\n" + e.StackTrace.ToString(), Globals.s_logFileName);
            }

            if (iep != null)
            {
                iep = null;
            }

            if (data != null)
            {
                data = null;
            }

            if (sock != null)
            {               
                sock = null;
            }
        }


    }

            //class for host
    public class DisccHost
    {
        /**
        * m_params - string array with data about host
        */
        public String[] m_params = null;

        /**
        * m_hostName - string with name of the string
        */
        public String m_hostName = "";

        /**
        * m_valid - bool true if the host respond to request and is server
        */
        public bool m_valid = false;
        /**
        * m_timerSendRequest - Timer for sending a new request to host
        */
        public Timer m_timerSendRequest = null;
        /**
        * m_timerDeleteHost - Timer for setting this host invalid if doesn't respond to request
        */
        public Timer m_timerDeleteHost = null;
        /**
        * DisccHost - set the name of the host and set it as invalid
        *@param hostName - the name to be set
        */
        public DisccHost(String hostName)
        {
            m_hostName  = hostName;
            m_valid     = false;
        }
    }

}
