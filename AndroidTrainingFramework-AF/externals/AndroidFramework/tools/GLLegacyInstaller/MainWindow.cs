using System;
using System.Reflection;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing; 
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using System.Xml;
using System.Threading;
using GLLegacyInstaller.SVN;
using System.Collections;
using Utils;
using System.Net.Sockets;
using System.Net;
using System.ServiceProcess;
using System.Diagnostics;
using GLLegacyInstaller;
using System.Timers;
using Microsoft.Win32;
using System.Security.Principal;

namespace GLLegacyInstaller
{
    public partial class MainWindow : Form
    {

        public static MainWindow s_instance = null;

        const String c_XML_root                     = "InstallerSetup";
        const String c_str_Sandbox                  = "Sandbox";
        const String c_str_Sandbox_urlRelease       = "urlRelease";
        const String c_str_Sandbox_urlDevelopment   = "urlDevelopment";
        const String c_str_Sandbox_releaseRev       = "releaseRevision";
        const String c_str_platforms                = "platforms";        

        public String m_SVNSandBoxUrlRelease        = "";
        public String m_SVNSandBoxUrlDevelopment    = "";
        public int m_SVNSandBoxReleaseRevision = 0;
        int m_currentTab = -1;
        public ToolsList m_toolList = null;
        public int m_nValidTools = 0;
        public int m_nTools = 0;
        
        Thread m_threadDisccDiscover = null;
        static int m_threadStopRequest = 0;
        public HostsList m_hostsList = null;

        static Socket s_sock = null;
        static IPEndPoint s_iep = null;
        static bool s_Show_VersionMessage = false;
        static bool s_Show_VirtualDriveMessage = false;
        System.Threading.Timer m_discoverTimer = null;

        static Thread s_threadProcessingMsg = null;
        static bool s_threadProcessingMsgStopRequest = false;
        static Point s_threadProcessingMsgPosition = new Point(0,0);

        /**
         * ShowProcessingMsg - start processing message thread
         **/
        static void ShowProcessingMsg()
        {
            s_instance.Refresh();

            if (s_threadProcessingMsg == null)
            {
                s_threadProcessingMsgPosition = new Point(s_instance.Left + s_instance.Width / 2 - ProcessingMessage.s_size.X / 2, s_instance.Top + s_instance.Height / 2 - ProcessingMessage.s_size.Y / 2);
                s_threadProcessingMsg = new Thread(new ThreadStart(ShowProcessingMsgWorkerThread));
                s_threadProcessingMsg.Start();
            }
        }

        /**
         * HideProcessingMsg - hide the ProcessingMessage window
         **/
        static void HideProcessingMsg()
        {
            if (s_threadProcessingMsg != null)
            {
                s_threadProcessingMsgStopRequest = true;
            }

            s_instance.Refresh();
        }

        /**
         * ShowProcessingMsgWorkerThread - show the ProcessingMessage window
         **/
        static void ShowProcessingMsgWorkerThread()
        {
            ProcessingMessage processingMessage = new ProcessingMessage();
            processingMessage.Location = s_threadProcessingMsgPosition;
            processingMessage.Show();
            while (true)
            {
                if (s_threadProcessingMsgStopRequest)
                {
                    s_threadProcessingMsgStopRequest = false;
                    processingMessage.Close();
                    s_threadProcessingMsg = null;
                    break;
                }

                processingMessage.Refresh();
                Thread.Sleep(50);
            }

            processingMessage = null;
        }


        /**
        * MainWindow - constructor, initialize main window tabs and buttons
        **/
        public MainWindow()
        {
            InitializeComponent();

            s_instance = this;

            m_version.Text = "v" + Globals.s_version;

            m_toolList = new ToolsList(this.m_gridTools);

            //remove other tabs - let only the tools tab
            //if(Globals.s_onlyCheckTools)
            {
                m_TAB.TabPages.Remove(m_TAB_SVNOperations);
            }
            //
            
            for (int i = 0; i < m_TAB.TabCount; i++)
            {
                m_TAB.TabPages[i].Enabled = false;
            }

            m_TAB.Enabled = false;
            m_button_Next.Enabled = false;
            m_button_Prev.Enabled = false;

            GoToNext();
        }

        /**
        * proCompletePath - combine a given path with current directory drive
        *              ex. If this project root is c:\prj and drivePath is bin\recycle\trash.txt then this will return c:\prj\recycle\trash.txt
        *              otherwise if drivePath is c:\BlueLeaf\poison.txt then this will return the same
        * @param drivePath - a string with a path
        * returns a string with combined path 
        **/
        public string proCompletePath(string drivePath) 
        { 
            if (Path.IsPathRooted(drivePath))
                return drivePath;
            
            string path = Path.Combine(System.IO.Directory.GetCurrentDirectory(), drivePath);
            if (Directory.Exists(path) || File.Exists(path))
                return path;

            path = Path.Combine(Path.GetDirectoryName(Application.ExecutablePath), drivePath);

            return path;            
        }

        /**
        * ShowMessage - display a BigMessageScreen with a given message and type
        * @param msg - a string with the message to be displayed
        * @param type -  a string with type of the message
        **/
        public static void ShowMessage(string msg, string type)
        {
            BigMessageScreen ms = new BigMessageScreen();
            ms.SetMessage(msg);
            ms.SetType(type);
            ms.ShowDialog();
            ms = null;
        }

        /**
        * LoadSettings - read Globals.s_settingsFile (InstallerSetup.xml) and load all settings
        * return true if load was succesful, else return false
        **/
        public bool LoadSettings()
        {            
            if (Globals.s_bErrorInParams)
            {
                m_startTimer.Stop();
                ShowMessage(Globals.s_strErrorMsg + Globals.s_helpMessage, "Error");
                return false;
            }

            Console.WriteLine("Loading Settings...");

            Globals.s_distccNameNDK.Clear();
            Globals.s_distccXMLDrives.Clear();
            Globals.s_distccNDKEnvVar.Clear();
            Globals.s_myEnvars.Clear();

            Globals.LoadVariableVars();

            string xml = proCompletePath(Globals.s_settingsFile);

            if(!File.Exists(xml))
            {
                m_startTimer.Stop();
                ShowMessage("The config file " + Globals.s_settingsFile + " is missing!\r\n\r\n" + Globals.s_helpMessage + "\r\n\r\n", "Error");
                return false;
            }
            bool configCorrectVersion = true;
            bool error = false;
            XmlReader reader = null;
            try
            {
                reader = XmlReader.Create(xml);
                if (reader == null)
                {
                    MessageBox.Show("Loading settings file " + Globals.s_settingsFile + " encountered an error!");
                    return false;
                }

                while (reader.Read())
                {
                    // Only detect start elements.
                    if (reader.IsStartElement())
                    {
                        //Console.WriteLine("reader.Name=" + reader.Name);

                        // Get element name and switch on it.
                        switch (reader.Name)
                        {
                            case c_XML_root:

                                break;
                            case "Installer":

                                string version = reader["version"];
                                if (version != Globals.s_version)
                                {
                                    configCorrectVersion = false;
                                }
                                break;
                            case c_str_Sandbox:
                                m_SVNSandBoxUrlRelease = reader[c_str_Sandbox_urlRelease];
                                m_SVNSandBoxUrlDevelopment = reader[c_str_Sandbox_urlDevelopment];
                                try
                                {
                                    m_SVNSandBoxReleaseRevision = int.Parse(reader[c_str_Sandbox_releaseRev]);
                                }
                                catch (Exception ex)
                                {
                                    m_SVNSandBoxReleaseRevision = 0;
                                }
                                linkLabelRelease.Text = m_SVNSandBoxUrlRelease;
                                linkLabelDevelopment.Text = m_SVNSandBoxUrlDevelopment;
                                break;

                            case c_str_platforms:
                                if (reader.IsStartElement())
                                {
                                    while (reader.Read())
                                    {
                                        if (!reader.IsStartElement() && reader.Name == c_str_platforms)
                                            break;
                                        if (reader.IsStartElement())
                                        {
                                            Globals.s_platforms.Add(reader.Name);
                                        }
                                    }
                                }
                                break;
                            default:
                                m_toolList.Load(reader);
                                break;
                        }

                    }

                }
            }
            catch(Exception e)
            {
                error = true;
            }

            // close reader
            reader.Close();

            if (error)
            {
                m_startTimer.Stop();
                BigMessageScreen dlg = new BigMessageScreen();
                dlg.SetMessage("Syntax error in InstallerSetup.xml!!!\n" + "Do you want to want to open InstallerSetup.xml to edit it?\r\n");
                dlg.SetType("Warning");
                dlg.TopMost = true;
                DialogResult dlgres = dlg.ShowDialog();
                dlg = null;
                if (dlgres == DialogResult.OK)
                {
                    Globals.s_strErrorMsg = "";
                    Globals.s_InvalidDrivesLetter = "";
                    Globals.s_InvalidPlatformForTool = "";
                    Globals.s_bErrorInParams = false;
                    Globals.s_bErrorInInstallerSetup = false;
                    OpenInstallerSetupAndReload();
                    return true;
                }
                if (m_currentTab == 1)
                {
                    GoToPrev();
                    m_button_Next.Enabled = false;
                    this.m_loadingProgress.Value = 0;
                }
                return false;
            }

            Globals.DISTCC_LoadDisccTempFile(false);

            if (Globals.s_platformsCommandLine.ToUpper() == "ALL")
            {
                Globals.s_platformsInView.AddRange(Globals.s_platforms);
                Globals.s_platformsCommandLine = "";
            }
            

            if (configCorrectVersion == false)
            {
                Globals.s_bErrorInInstallerSetup = true;
                Globals.s_strErrorMsg = "---Wrong installer version in InstallerSetup.xml. \r\n Please change installer version.\r\n\n";
            }


            if (Globals.s_platforms.Count == 0)
            {
                Globals.s_bErrorInInstallerSetup = true;
                Globals.s_strErrorMsg += "---No platforms found in " + Globals.s_settingsFile+". Please edit " + Globals.s_settingsFile + " \r\n\n";
            }
            else
            {

            }

            foreach (string s in Globals.s_platformsInView)
            {
                bool bValidPlatform = false;
                foreach (string p in Globals.s_platforms)
                {
                    if (s.ToUpper() == p.ToUpper())
                    {
                        bValidPlatform = true;
                        break;
                    }
                }
                if (!bValidPlatform)
                {
                    Globals.s_bErrorInParams = true;
                    Globals.s_strErrorMsg += "---Invalid platform: " + s.ToString() + " as parameter.\r\n Valid platforms are edited in " + Globals.s_settingsFile + " under the <platforms> node. (Use '+' as separator for platforms.)\r\n\n";
                    break;
                }
            }
            
            if (!Globals.s_bValidDrivesLetter)
            {
                Globals.s_bErrorInInstallerSetup = true;
                Globals.s_strErrorMsg += Globals.s_InvalidDrivesLetter;
            }

            if (!Globals.s_bValidPlatformForTool)
            {
                Globals.s_bErrorInInstallerSetup = true;
                Globals.s_strErrorMsg += Globals.s_InvalidPlatformForTool;
            }

            if (!string.IsNullOrEmpty(Globals.s_strErrorMsg))
            {
                if (!Globals.s_workInConsole)
                {
                    m_startTimer.Stop();
                    if( Globals.s_bErrorInInstallerSetup )
                    {
                        //ShowMessage(Globals.s_strErrorMsg, "Error");
                        BigMessageScreen dlg = new BigMessageScreen();
                        dlg.SetMessage(Globals.s_strErrorMsg + "Do you want to want to open InstallerSetup.xml to edit it?\r\n");
                        dlg.SetType("Warning");
                        dlg.TopMost = true;
                        DialogResult dlgres = dlg.ShowDialog();
                        dlg = null;
                        if (dlgres == DialogResult.OK)
                        {
                            Globals.s_strErrorMsg = "";
                            Globals.s_InvalidDrivesLetter = "";
                            Globals.s_InvalidPlatformForTool = "";
                            Globals.s_bErrorInParams = false;
                            Globals.s_bErrorInInstallerSetup = false;
                            OpenInstallerSetupAndReload();
                        }
                        else
                            Environment.Exit(1);
                    }
                    else
                        if (Globals.s_bErrorInParams)
                        {
                            ShowMessage(Globals.s_strErrorMsg, "Error");
                            Environment.Exit(1);
                        }

                }
                else
                {
                    Console.WriteLine(Globals.s_strErrorMsg);
                    return false;
                }
            }

            m_toolList.RefreshGridCellValues();

            Console.WriteLine("LoadSettings... OK");
            Console.WriteLine("Elevated privileges gained={0}", new WindowsPrincipal(WindowsIdentity.GetCurrent()).IsInRole(WindowsBuiltInRole.Administrator));

            return true;
        }



        /**
        * SetButtonsText - set main window button text depending of current tab 
        **/
        private void SetButtonsText()
        {
            if (m_currentTab == 0)
            {
                m_button_Prev.Text = "Close";
                m_button_Next.Text = "Next >";
            }
            else if (m_currentTab == 1) //this is a special case (because the tab 1 is Check Tools)
            {
                m_button_Prev.Text = "< Prev";
                m_button_Next.Text = "Apply >";
            }
            else if (m_currentTab >= (m_TAB.TabCount - 1))
            {
                m_button_Prev.Text = "< Prev";
                m_button_Next.Text = "Close";
            }
            else if (m_currentTab > 0)
            {
                m_button_Prev.Text = "< Prev";
                m_button_Next.Text = "Next >";
            }
        }


        public void deleteButtonClick(object sender, EventArgs e)
        {
            int i = 1;
            while (i < m_gridIPs.RowsCount)
            {
                if (m_gridIPs[i, 1].ToString() == "True")
                    m_gridIPs.Rows.Remove(i);
                else
                    i++;
            }
        }

        /**
        * ExecuteTABEnter - make initializations when enter in a tab
        * @param tab - a int with the tab index 
        * returns a boolean that is true if all was initialized ok, false if an error occurred
        **/
        private bool ExecuteTABEnter(int tab)
        {
            if (m_TAB.TabPages[m_currentTab] == m_TAB_DISTCC)
            {
                Globals.s_notepad = Globals.GetEnvironmentVar("GLLEGACY_NOTEPAD") + Path.DirectorySeparatorChar + "notepad++.exe";
                if (!File.Exists(Globals.s_notepad))
                {
                    Globals.s_notepad = "notepad.exe";
                }

                #region getIPs                

                if (!Globals.s_bAreInitIPs)
                {
                    bool bIsOKs_distccIPUsed = false; 
                    // Resolves a host name or IP address to an IPHostEntry instance.
                    // IPHostEntry - Provides a container class for Internet host address information. 
                    System.Net.IPHostEntry _IPHostEntry = System.Net.Dns.GetHostEntry(System.Net.Dns.GetHostName());

                    // IPAddress class contains the address of a computer on an IP network. 
                    foreach (System.Net.IPAddress _IPAddress in _IPHostEntry.AddressList)
                    {
                        // InterNetwork indicates that an IP version 4 address is expected 
                        // when a Socket connects to an endpoint
                        if (_IPAddress.AddressFamily.ToString() == "InterNetwork")
                        {
                            Globals.s_IPs.Add(_IPAddress.ToString());
                            if (string.Equals(_IPAddress.ToString(), Globals.s_distccIPUsed.ToString(), StringComparison.Ordinal))
                                bIsOKs_distccIPUsed = true;
                        }
                    }
                    if (!bIsOKs_distccIPUsed)
                        Globals.s_distccIPUsed = Globals.s_IPs[0].ToString();
                    Globals.s_bAreInitIPs = true;
                }
                
                #endregion
                #region check_gllegacyservice_version
                ServiceController ctl = GetAndUpdateGLLegacyServiceStatusLabel();
                //if (ctl != null)
                //{
                  //  Globals.s_versionServiceRunning = GetVersionGLLegacyService();
                    //string InstallPath = (string)Registry.GetValue(@"HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\GLLegacyService", "ImagePath", null);
                    /*if (InstallPath != null)
                    {
                        String str = InstallPath.Substring(1, InstallPath.Length - 2);
                        String ver = "";
                        if (File.Exists(str))
                        {
                            FileVersionInfo versionInfo = FileVersionInfo.GetVersionInfo(str);
                            Globals.s_versionServiceRunning = versionInfo.ProductVersion.Substring(0, 6);
                            labelVersion.Text = Globals.s_versionServiceRunning;
                        }
                    }*/
                    if( Globals.s_versionServiceRunning=="0" )
                    {
                        //GLlegacyService.exe not found, so delete it
                        DeleteGLLegacyService();
                        UninstallGLLegacyService();
                    }
               // }
                #endregion

                m_hostsList = new HostsList(this.m_gridAvailableHosts);
                m_hostsList.RefreshGridStructure();

                #region create Static Hosts list
                m_gridIPs.Rows.Clear();
                m_gridIPs.Columns.Clear();
                m_gridIPs.ColumnsCount = 2;
                m_gridIPs.FixedRows = 1;
                m_gridIPs.Rows.Insert(0);

                
                m_gridIPs.Columns[0].MinimalWidth = 100;
                m_gridIPs.Columns[0].MaximalWidth = 200;

                m_gridIPs.Columns[1].MinimalWidth = 70;
                m_gridIPs.Columns[1].MaximalWidth = 100;

                m_gridIPs[0, 0] = new SourceGrid.Cells.ColumnHeader("Host IP");
                Button b = new Button();
                b.Click += new EventHandler(deleteButtonClick);
                SourceGrid.Cells.Button sb = new SourceGrid.Cells.Button(b);
                SourceGrid.Cells.Controllers.Button cb = new SourceGrid.Cells.Controllers.Button();
                cb.Executed += new EventHandler(this.deleteButtonClick);
                sb.AddController(cb);
                sb.Value = "Delete";
                //sb.ToolTipText = inclFile;
                m_gridIPs[0, 1] = sb;

                String[] hosts = null;

                if (!string.IsNullOrEmpty(Globals.s_distccStaticHostList))
                {
                    hosts = Globals.s_distccStaticHostList.Split(' ');

                    for (int i = 0; i < hosts.Length; i++)
                    {
                        m_gridIPs.Rows.Insert(i + 1);
                        m_gridIPs[i + 1, 0] = new SourceGrid.Cells.Cell(hosts[i], typeof(string));
                        m_gridIPs[i + 1, 0].Editor.EditableMode = SourceGrid.EditableMode.None;
                        // m_gridIPs[gridRows, 0].ColumnSpan = 4;
                        m_gridIPs[i + 1, 0].View = new SourceGrid.Cells.Views.Cell();

                        m_gridIPs[i + 1, 1] = new SourceGrid.Cells.CheckBox("", false);
                        m_gridIPs[i + 1, 1].View = new SourceGrid.Cells.Views.CheckBox();

                    }
                }

                #endregion
                    m_checkBoxDISTCCUse.Checked = (Globals.s_distccUse == 1);
                m_checkBoxDISTCCServer.Checked = (Globals.s_distccServer == 1);
                checkShowServers.Checked = Globals.s_bShowOnlyServers;
                numericUpDown.Value = (Globals.s_timeDiscover < 1) ? 5 : (Globals.s_timeDiscover > 120) ? 120 : Globals.s_timeDiscover;

                //if (Globals.IsPlatformSelected(Globals.s_strAndroid))
                {
                    m_checkBoxDISTCCServer.Enabled = true;
                    m_checkBoxDISTCCUse.Enabled = true;

                    //Globals.DISTCC_LoadDisccTempFile(false);

                    #region fill_comboBox_with_drive_letters
                    comboBoxLetterDrive.Items.Clear();
                    String tmp = null;
    
                    for (int i = 0; i < Globals.s_distccDrives.Count; i++)
                    {
                        if (!string.IsNullOrEmpty((string)Globals.s_distccDrives[i]))
                        {
                            comboBoxLetterDrive.Items.Add((string)Globals.s_distccDrives[i] + " - " + Globals.s_distccNameNDK[i]);
                            if ((string)Globals.s_distccDrives[i] == Globals.s_distccDriveSelected)
                            {
                                tmp = Globals.s_distccDrives[i].ToString() + " - " + Globals.s_distccNameNDK[i];
                            }
                        }
                    }


                    if (!string.IsNullOrEmpty(Globals.s_distccDriveSelected))
                        comboBoxLetterDrive.Text = tmp;
                    #endregion

                    #region fill_comboBox_with_IPs
                    comboBoxNetAddr.Items.Clear();
 
                    String selectedText = "";
                    foreach (string IP in Globals.s_IPs)
                    {
                        IPAddress mask = Globals.GetSubnetMask(IPAddress.Parse(IP));
                        if (mask!=null)
                        {
                            String net = FormatNetMask(IP, mask.ToString());
                            comboBoxNetAddr.Items.Add(net);
                            if (IP == Globals.s_distccIPUsed)
                                selectedText = net;
                        }
                        else
                            Globals.s_bNetError = true;
                    }
                    if (String.IsNullOrEmpty(Globals.s_distccIPUsed))
                        comboBoxNetAddr.SelectedIndex = 0;
                    else
                        comboBoxNetAddr.Text = selectedText;

                    if (Globals.s_bNetError)
                        MessageBox.Show("Net Error!!!", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    #endregion

                    m_groupBoxHosts.Enabled = (Globals.s_distccUse == 1);
                    comboBoxLetterDrive.Enabled = (Globals.s_distccUse == 1);
                    labelVirtualDrive.Enabled = (Globals.s_distccUse == 1);
                    labelDiscoverTime.Enabled = (Globals.s_distccUse == 1);
                    numericUpDown.Enabled = (Globals.s_distccUse == 1);
                    buttonRescan.Enabled = (Globals.s_distccUse == 1);
                    checkShowServers.Enabled = (Globals.s_distccUse == 1);

                    m_groupBoxSHosts.Enabled = (Globals.s_distccUse == 1);
                    ipAddressControl.Enabled = (Globals.s_distccUse == 1);
                    buttonAddIP.Enabled = (Globals.s_distccUse == 1);
                    m_gridIPs.Enabled = (Globals.s_distccUse == 1);

                    m_gridAvailableHosts.VScrollBar.Visible = true;
                    m_gridAvailableHosts.HScrollBar.Visible = true;                    
                    m_groupBoxServer.Enabled = (Globals.s_distccServer == 1);    
                    if (!this.pictureBox3.Visible)
                        EnableDISTCCUse(m_checkBoxDISTCCUse.Checked);
                    EnableDISTCCServer(m_checkBoxDISTCCServer.Checked);
                    groupBoxGLLS.Enabled = true;                    
                    //DiscoverDistccHostsStart();
                }
                //else
                //{
                //    labelAndroidWarning.Visible = true;
                //    m_checkBoxDISTCCServer.Enabled = false;
                //    m_checkBoxDISTCCUse.Enabled = false;
                //    checkShowServers.Enabled = false;
                //    m_groupBoxHosts.Enabled = false;

                //    m_groupBoxSHosts.Enabled = false;
                //    ipAddressControl.Enabled = false;
                //    buttonAddIP.Enabled = false;
                //    m_gridIPs.Enabled = false;


                //    comboBoxLetterDrive.Enabled = false;
                //    labelVirtualDrive.Enabled = false;
                //    labelDiscoverTime.Enabled = false;
                //    numericUpDown.Enabled = false;
                //    buttonRescan.Enabled = false;

                //    m_groupBoxServer.Enabled = false;
                //    groupBoxGLLS.Enabled = false;

                //}
            }
            else if (m_TAB.TabPages[m_currentTab] == m_TAB_Sumup)
            {
                m_distccHostFileCheck.Start();
                this.labelGLLegacyDistccEnvPath.Text = Globals.GetFullPathToDistccEnvBatchScript();
                this.labelGLLegacyInstallerEnvPath.Text = Globals.GetFullPathToInstallerEnvBatchScript();
                this.labelTotalTools.Text = m_nTools.ToString();
                this.labelInvalidTools.Text = (m_nTools-m_nValidTools).ToString();
                this.pictureBoxUseHosts.Image = (Globals.s_distccUse == 0) ? global::GLLegacyInstaller.Properties.Resources.uncheck:global::GLLegacyInstaller.Properties.Resources.check;
                this.pictureBoxServer.Image = (Globals.s_distccServer == 0) ? global::GLLegacyInstaller.Properties.Resources.uncheck:global::GLLegacyInstaller.Properties.Resources.check;

                this.labelHostsNumber1.Visible = (Globals.s_distccUse == 0) ? false : true;
                this.labelHostsNumber.Visible = (Globals.s_distccUse == 0) ? false : true;
                this.labelSHostsNumber1.Visible = (Globals.s_distccUse == 0) ? false : true;
                this.labelSHostsNumber.Visible = (Globals.s_distccUse == 0) ? false : true;
                this.labelDriveSelected1.Visible = (Globals.s_distccUse == 0) ? false : true;
                this.labelDriveSelected.Visible = (Globals.s_distccUse == 0) ? false : true;
                this.labelDiscoverTime1.Visible = (Globals.s_distccUse == 0) ? false : true;
                this.labelDiscoverTimeS.Visible = (Globals.s_distccUse == 0) ? false : true;
                
                if (Globals.s_distccUse == 1)
                {
                    this.labelHostsNumber.Text = (Globals.s_hostsWanted==null)?"0":Globals.s_hostsWanted.Length.ToString();
                    this.labelSHostsNumber.Text = (this.m_gridIPs.RowsCount-1).ToString();
                    this.labelDriveSelected.Text = Globals.s_distccDriveSelected + ":";
                    this.labelDiscoverTimeS.Text = Globals.s_timeDiscover.ToString();
                }


                this.labelDistccdStatus1.Visible = (Globals.s_distccServer == 0) ? false : true;
                this.labelDistccdStatus.Visible = (Globals.s_distccServer == 0) ? false : true;
                //this.labelGLLegacyServiceStatus1.Visible = (Globals.s_distccServer == 0) ? false : true;
                //this.labelGLLegacyServiceStatus.Visible = (Globals.s_distccServer == 0) ? false : true;                

                //if (Globals.s_distccServer == 1)
                {
                    Process[] pname = Process.GetProcessesByName(Globals.s_distccdProcessName);
                    this.labelDistccdStatus.Text = (pname.Length == 0)?"Stopped":"Running";

                    ServiceController ctl = ServiceController.GetServices().Where(s => s.ServiceName == "GLLegacyService").FirstOrDefault();
                    this.labelGLLegacyServiceStatus.Text = (ctl == null) ? "Not installed" : ctl.Status.ToString();
                }
                
            }
            
            return true;
        }

        /**
        * ExecuteTABExit -save data when exit from a tab
        * @param tab - a int with the tab index 
        * returns a boolean that is true if all was saved well, false if an error occurred
        **/
        private bool ExecuteTABExit(int tab)
        {
            if (m_TAB.TabPages[m_currentTab] == m_TAB_CheckTools)
            {
                //return true;// for test
                bool hasInvalidPaths = false;
                bool error = false;
                int iNDK = 0;

                Globals.s_distccDrives.Clear();
                Globals.s_distccDrives.AddRange(Globals.s_distccXMLDrives); 

                Tool rm = null;
                SourceGrid.Grid grid = MainWindow.s_instance.m_gridTools;

                m_nTools = grid.RowsCount-1;
                m_nValidTools = 0;

                for (int i = 1; i < grid.RowsCount; i++)
                {
                    int ID = (int)(grid[i, 0].Value);

                    rm = (Tool)MainWindow.s_instance.m_toolList.Get(ID);

                    if (!rm.m_valid)
                    {                        
                        if (rm.m_required)
                        {
                            hasInvalidPaths = true;
                            error = true;
                            BigMessageScreen ms = new BigMessageScreen();
                            ms.SetMessage(rm.m_toolName + "\n\n" + rm.GetErrors());
                            ms.SetType("Error");
                            if (ms.ShowDialog() != DialogResult.OK) error = true;
                            ms = null;

                            break;
                        }
                        else
                        {
                            //this an invalid optional ndk, so put s_distccServerDrive on ""
                            if (rm.m_VirtualDrive != "")
                            {
                                for (int j = 0; j < Globals.s_distccNameNDK.Count; j++)
                                {
                                    if (Globals.s_distccNameNDK[j].ToString() == rm.m_toolName)
                                    {
                                        Globals.s_distccDrives[j] = "";
                                        break;
                                    }
                                }
                            }
                            iNDK++;
                        }
                        continue;
                    }
                    else
                    {
                        m_nValidTools++;
                        //only NDK tools have virtual drive
                        if (rm.m_VirtualDrive != "")
                        {
                            iNDK++;
                            if (rm.m_required && Globals.s_distccDriveSelected == null)
                                Globals.s_distccDriveSelected = rm.m_VirtualDrive;
                        }
                    }
                }

                //if (!Globals.IsPlatformSelected(Globals.s_strAndroid))
                //    DiscoverDistccHostsStop();
                         
                if (!error)
                {
                    UpdateEnvironment dlg = new UpdateEnvironment();
                    bool returnValue = (dlg.ShowDialog() == DialogResult.OK);
                    dlg = null;
                    return returnValue;
                }
                else
                    return false;
            }
            else if (m_TAB.TabPages[m_currentTab] == m_TAB_DISTCC)
            {
                labelAndroidWarning.Visible = false;
                if ((Globals.s_distccServer == 1) && !CheckDISTCCServerRequirements()) return false;
                
                if (!CheckComboDriveLetter()) return false;

                //check if letters changed to restart GLLegacyService
                /*bool restart = false;
                if (Globals.s_distccServer == 1)
                {
                    foreach (string d in Globals.s_distccDrives)
                    {
                        if (!Globals.s_distccXMLDrives.Contains(d))
                        {
                            restart = true;
                            break;
                        }
                    }
                }*/

                
                ShowProcessingMsg();

                if (Globals.b_restartService)
                        RestartService();
               // Stopwatch stopwatch = new Stopwatch();
               // stopwatch.Start();
                //return returnValue;
                Globals.s_timeDiscover = (int)this.numericUpDown.Value;
                
                //WriteEnvVarAndCopyBatchFiles();
                WriteDistccEnvAndCopyBatchFile();

                //stopwatch.Stop();
                //SendErrorMessage("Time it took = {0}" + stopwatch.Elapsed);
                //SendErrorMessage("How many writeenvvarcopy... functions calls happened ");
                HideProcessingMsg();
                if (m_SVNSandBoxReleaseRevision > 0)
                    lbReleaseRev.Text = "Release Revision: " + m_SVNSandBoxReleaseRevision.ToString();
                else
                {
                    lbReleaseRev.Text = "";
                    lbReleaseRev.Visible = false;
                }
            }
            else
                m_distccHostFileCheck.Stop();


            return true;
        }


        /**
        * GoToNext - go to next ab when next is pressed
        * returns true if the current tab was changed
        **/
        private bool GoToNext()
        {
            int temp = m_currentTab;

            m_currentTab++;
            if (m_currentTab >= m_TAB.TabCount)
            {
                this.Close();
                return false;
            }

            SetButtonsText();


            m_TAB.SelectedIndex = m_currentTab;
            

            for(int i = 0; i < m_TAB.TabCount; i++)
            {                
                m_TAB.TabPages[i].Enabled = (m_currentTab == i);
            }

            m_TAB.Refresh();

            return (temp != m_currentTab);
        }

        /**
        * GoToPrev - go to previous tab when back is pressed        
        **/
        private void GoToPrev()
        {
            m_currentTab--;
            if (m_currentTab < 0)
            {
                this.Close();
            }

            SetButtonsText();
            labelAndroidWarning.Visible = false;

            m_TAB.SelectedIndex = m_currentTab;


            for (int i = 0; i < m_TAB.TabCount; i++)
            {
                m_TAB.TabPages[i].Enabled = (m_currentTab == i);
            }

            m_TAB.Refresh();
        }


        /**
        * Prev_Click - process previous button click (m_button_Prev)
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void Prev_Click(object sender, EventArgs e)
        {
            GoToPrev();
        }

        /**
        * Next_Click - process next button click (m_button_Nex)
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void Next_Click(object sender, EventArgs e)
        {
            if (ExecuteTABExit(m_currentTab))
            {
                if(GoToNext())
                {
                    this.Refresh();

                    ExecuteTABEnter(m_currentTab);
                }
            }
            

        }

        /**
        * Refresh_Click - process refresh button click (m_button_Refresh)
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void Refresh_Click(object sender, EventArgs e)
        {
            m_toolList.DetectToolsInstallPath();
            m_toolList.ApplyDetectToolsInstallPath();
        }

        /**
        * Refresh_Click - process load button click
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void GLLegacyInstaller_Load(object sender, EventArgs e)
        {
            m_startTimer.Enabled = true;
            m_startTimer.Start();
        }

        /**
        * StartTimer_Tick - call by timer after a second, initialize the tools tab
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void StartTimer_Tick(object sender, EventArgs e)
        {
            m_startTimer.Stop();
            if (LoadSettings())
            {
                m_toolList.DetectToolsInstallPath();
                m_toolList.ApplyDetectToolsInstallPath();

                m_TAB.Enabled = true;
                m_button_Next.Enabled = true;
                m_button_Prev.Enabled = true;

                //int i = 0;
                //int gap = 70;
                //if (Globals.s_platforms.Count!=0)
                //    gap = 400 / Globals.s_platforms.Count;
                //m_checkBoxPlatforms = new CheckBox[Globals.s_platforms.Count];

                //foreach( string s in Globals.s_platforms )
                //{
                //    m_checkBoxPlatforms[i] = new CheckBox();
                //    m_checkBoxPlatforms[i].Visible = true;
                //    m_checkBoxPlatforms[i].Text = s;
                //    m_checkBoxPlatforms[i].Left = 3 + i * gap;
                //    m_checkBoxPlatforms[i].Top = 11;
                //    m_checkBoxPlatforms[i].Size = new System.Drawing.Size(65, 17);

                //    foreach (string p in Globals.s_platformsInView)
                //    {
                //        if (s.ToUpper() == p.ToUpper())
                //            m_checkBoxPlatforms[i].Checked = true;
                //    }

                //    this.m_TAB_CheckTools.Controls.Add(m_checkBoxPlatforms[i]);
                //    m_checkBoxPlatforms[i].CheckedChanged += new System.EventHandler(this.CheckBoxPlatformChanged);
                //    i++;
                //}

                //m_startTimer.Stop();

                GoToNext();
            }
            else
            {
                m_button_Prev.Enabled = true;
            }
            m_toolList.RefreshGridContent();
            m_toolList.RefreshGridCellValues();

        }

        /**
        * m_TAB_Selecting - event handler for tab selection
        * @param sender an object - not used
        * @param e an TabControlCancelEventArgs, current tab
        **/
        private void m_TAB_Selecting(object sender, TabControlCancelEventArgs e)
        {
            if (m_currentTab != e.TabPageIndex) m_TAB.SelectedIndex = m_currentTab;
        }


        /**
        * TortoiseSVN_Click - event handler for TortoiseSVN click
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void TortoiseSVN_Click(object sender, EventArgs e)
        {
            if (m_textBoxPath.Text.Trim() == "")
            {
                MessageScreen ms = new MessageScreen();
                ms.SetMessage("The path '" + m_textBoxPath.Text + "' is wrong!");
                ms.SetType("Error");
                ms.ShowDialog();
                ms = null;
                return;
            }

            String url = "";
            int rev = -1;
            if (radioButtonRelease.Checked && m_SVNSandBoxReleaseRevision > 0)
                rev = m_SVNSandBoxReleaseRevision;

            if (radioButtonRelease.Checked)
                url = m_SVNSandBoxUrlRelease;
            else if (radioButtonDevelopment.Checked)
                url = m_SVNSandBoxUrlDevelopment;

            if(m_RB_CheckOut.Checked)
                SVNCommands.StartProcess(url, m_textBoxPath.Text, true, rev);
            else if(m_RB_Update.Checked)
                SVNCommands.StartProcess(url, m_textBoxPath.Text, false, rev);

            
        }

        /**
        * SVNBrowse_Click - event handler for SVNBrowse click
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void SVNBrowse_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog dialog = new FolderBrowserDialog();

            dialog.Description = "Select an empty folder for to CheckOut";
            dialog.ShowNewFolderButton = false;
            dialog.RootFolder = Environment.SpecialFolder.MyComputer;
               
            if (dialog.ShowDialog() == DialogResult.OK)
            {
                m_textBoxPath.Text = dialog.SelectedPath;
            }
            dialog = null;
        }

        /**
        * AboutToolStripMenuItem_Click - event handler for aboutToolStripMenuItem click
        *                              - shows about dialog 
        * @param sender an object - not used
        * @param e an EventArgs - not used
       **/
        private void AboutToolStripMenuItem_Click(object sender, EventArgs e)
        {
            About about = new About();
            about.ShowDialog();
            about = null;
        }


        void WriteDistccEnvAndCopyBatchFile()
        {
            if (m_hostsList != null)
                m_hostsList.GetWantedHosts();


            string hosts = "";
            for (int i = 1; i < this.m_gridIPs.RowsCount; i++)
            {
                if (hosts != "")
                    hosts += " ";
                hosts += (string)(this.m_gridIPs[i, 0].Value);
            }

            Globals.s_distccStaticHostList = hosts;

            Globals.DISTCC_SaveDistccTempFile();
            Globals.DISTCC_WriteEnvVar();

            String str = Globals.GetRegValue("SYSTEM\\CurrentControlSet\\services\\GLLegacyService", "ImagePath");
            if (str != null)
            {
                str = str.Replace('"', ' ');
                str = str.Trim();

                string dst = Path.Combine(Path.GetDirectoryName(str), Path.GetFileName(Globals.GetFullPathToDistccEnvBatchScript()));
                System.IO.File.Copy(Globals.GetFullPathToDistccEnvBatchScript(), dst, true);
            }
        }


        /**
        * WriteEnvVarAndCopyBatchFiles - saves environment variables and distcc temp file and copy batch files
        **/
        void WriteEnvVarAndCopyBatchFile()
        {
            Globals.DISTCC_WriteEnvVar();
            UpdateEnvironment dlg = new UpdateEnvironment();
            bool returnValue = (dlg.ShowDialog() == DialogResult.OK);
            dlg = null;
            //if (Globals.s_distccServer == 1)
            //{
                String str = Globals.GetRegValue("SYSTEM\\CurrentControlSet\\services\\GLLegacyService", "ImagePath");
                if (str != null)
                {
                    str = str.Replace('"', ' ');
                    str = str.Trim();

                    //string dst = Path.Combine(Path.GetDirectoryName(str), Path.GetFileName(Globals.GetFullPathToDistccEnvBatchScript()));
                    //System.IO.File.Copy(Globals.GetFullPathToDistccEnvBatchScript(), dst, true);

                    string dst = Path.Combine(Path.GetDirectoryName(str), Path.GetFileName(Globals.GetFullPathToInstallerEnvBatchScript()));
                    System.IO.File.Copy(Globals.GetFullPathToInstallerEnvBatchScript(), dst, true);
                }
            //}

        }

        /**
        * RestartService - stop GLLegacyService and restart it 
        */
        void RestartService()
        {
            Globals.b_restartService = false;
            
            //WriteEnvVarAndCopyBatchFiles();
            WriteDistccEnvAndCopyBatchFile();

            try
            {
                ServiceController ctl = GetAndUpdateGLLegacyServiceStatusLabel();
                if (ctl != null)
                {
                    if (ctl.Status == ServiceControllerStatus.Running)
                    {
                        ctl.Stop();
                        Thread.Sleep(2000);
                    }

                    StopDISTCCServer();

                    ctl.Start();
                    Thread.Sleep(1000);

                    ctl = GetAndUpdateGLLegacyServiceStatusLabel();
                }
            }
            catch (Exception ex)
            {

            }
        }


        /**
        * ButtonRestartService_Click - event handler for button restart service click
        *                            - call RestartService()
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void ButtonRestartService_Click(object sender, EventArgs e)
        {
            ShowProcessingMsg();
            RestartService();
            HideProcessingMsg();
        }

        /**
        * GetAndUpdateDISTCCStatusLabel - get the distcc status and display it (stopped/running)
        * return a Process array with distcc processes 
        **/
        Process[] GetAndUpdateDISTCCStatusLabel()
        {
            Process[] pname = Process.GetProcessesByName(Globals.s_distccdProcessName);
            if (pname.Length == 0)
                labelDISTCCStatus.Text = "Stopped";
            else
                labelDISTCCStatus.Text = "Running";

            return pname;
        }

        /**
        * GetAndUpdateGLLegacyServiceStatusLabel - get GLLegacy status and display it (not installed/running)
        * returns ServiceController, GLLegacyService controller 
        **/
        ServiceController GetAndUpdateGLLegacyServiceStatusLabel()
        {
            ServiceController ctl = ServiceController.GetServices().Where(s => s.ServiceName == "GLLegacyService").FirstOrDefault();

            if (ctl == null)
            {
                labelServiceStatus.Text = "Not installed";
                buttonRestartService.Enabled = false;
                buttonOpenLogFile.Enabled = false;
                buttonReinstallService.Text = "Install";
                comboBoxNetAddr.Enabled = false;
            }
            else
            {
                labelServiceStatus.Text = ctl.Status.ToString();
                Globals.s_versionServiceRunning = GetVersionGLLegacyService();
                buttonRestartService.Enabled = true;
                buttonOpenLogFile.Enabled = true;
                buttonReinstallService.Text = "Reinstall";
                comboBoxNetAddr.Enabled = true;
            }

            return ctl;
        }


        /**
        * StopDISTCCServer - get distcc processes and kill them
        **/
        private void StopDISTCCServer()
        {
            //Globals.s_batchScript = Path.GetDirectoryName(Application.ExecutablePath) + "\\" + Globals.s_batchScript;
            //Globals.WriteEnvVar();

            Process[] pname = GetAndUpdateDISTCCStatusLabel();
            while (pname.Length > 0)
            {
                try
                {
                    pname[0].Kill();
                    Thread.Sleep(10);
                }
                catch(Exception)
                {

                }
                
                pname = GetAndUpdateDISTCCStatusLabel();
            }            
        }

        /**
        * GetVersionGLLegacyService - get version of GLLegacyService from registry
        * return a string with the version
        **/
        public string GetVersionGLLegacyService()
        {
            string InstallPath = (string)Registry.GetValue(@"HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\GLLegacyService", "ImagePath", null);
            if (InstallPath != null)
            {
                String str = InstallPath.Substring(1, InstallPath.Length - 2);
                if (File.Exists(str))
                {
                    FileVersionInfo versionInfo = FileVersionInfo.GetVersionInfo(str);
                    Globals.s_versionServiceRunning = versionInfo.ProductVersion;
                    this.labelVersion.Text = Globals.s_versionServiceRunning;
                    return Globals.s_versionServiceRunning;
                }
                return "0";
            }
            return "";
       }

        /**
        * DeleteGLLegacyService - delete GLLegacyService service
        **/
        public static void DeleteGLLegacyService()
        {
            ShowProcessingMsg();

            Process p = new Process();

            p.StartInfo.FileName = "sc.exe";
            p.StartInfo.Verb = "runas";
            //string path = Path.Combine(Path.GetDirectoryName(Application.ExecutablePath), "service");
            p.StartInfo.Arguments = "delete GLLegacyService";
            p.StartInfo.UseShellExecute = true;
            p.StartInfo.CreateNoWindow = true;
            p.Start();
            p.WaitForExit();
            p = null;
            ServiceController ctl;
            do
            {
                ctl = ServiceController.GetServices().Where(s => s.ServiceName == "GLLegacyService").FirstOrDefault();
                Thread.Sleep(1000);
            } while (ctl != null);
            
            HideProcessingMsg();
        }

        /**
        * InstallGLLegacyService - install GLLegacyService
        **/
        public static void InstallGLLegacyService()
        {
            string cmd = "GLLegacyServiceSetup.msi";
            System.Diagnostics.Process proc = new System.Diagnostics.Process();
            proc.StartInfo.WorkingDirectory = Path.Combine(Path.GetDirectoryName(Application.ExecutablePath), "service");
            proc.StartInfo.FileName = cmd;
            proc.StartInfo.RedirectStandardError = false;
            proc.StartInfo.RedirectStandardInput = false;
            proc.StartInfo.RedirectStandardOutput = false;
            proc.StartInfo.CreateNoWindow = true;
            proc.StartInfo.UseShellExecute = true;
            proc.StartInfo.Arguments = "";
            proc.Start();
            proc.WaitForExit();
            proc = null;
        }

        /**
        * UninstallGLLegacyService - uninstall GLLegacyService
        **/
        public static void UninstallGLLegacyService()
        {
            string strUninstall = GetGLLegacyServiceUninstallString();
            if (!string.IsNullOrEmpty(strUninstall))
            {
                System.Diagnostics.Process FProcess = new System.Diagnostics.Process();
                string temp = "/x{" + strUninstall.Split("/".ToCharArray())[1].Split("I{".ToCharArray())[2] + " /qn";
                //replacing with /x with /i would cause another popup of the application uninstall
                FProcess.StartInfo.FileName = strUninstall.Split("/".ToCharArray())[0];
                FProcess.StartInfo.Arguments = temp;
                FProcess.StartInfo.UseShellExecute = false;
                FProcess.Start();
                FProcess.WaitForExit();
                FProcess = null;
            }
        }

        /**
        * UninstallGLLegacyService - set GLLegacyService uninstall string from registry
        * return a string with GLLegacyService uninstall string 
        **/
        public static string GetGLLegacyServiceUninstallString()
        {
            string uninstall = "";
            try
            {
                string key = @"SOFTWARE\Microsoft\Windows\CurrentVersion\Installer\UserData\S-1-5-18\Products\";
                RegistryKey regkey = Registry.LocalMachine.OpenSubKey(key);

                if( regkey != null)
                {
                    string[] Names = regkey.GetSubKeyNames();
                    
                    for (int i = 0; i < Names.Length; i++)
                    {
                        Microsoft.Win32.RegistryKey FTemp = regkey.OpenSubKey(Names[i]).OpenSubKey("InstallProperties");
                        if (FTemp != null && FTemp.GetValue("DisplayName").ToString() == "GLLegacyService")
                        {
                            object obj = FTemp.GetValue("UninstallString");
                            if (obj == null)
                                uninstall = "";
                            else
                                uninstall = obj.ToString();
                            i = Names.Length;
                        }
                    }
                }
                
            }
            catch (System.Exception ex)
            {
            	
            }
            return uninstall;
        }

        /**
        * CheckAndInstallGLLegacyService - check if GLLegacyService is running and if it required version
        * return a boolean, true of GLLegacyService was installed successfully, false if not
        **/
        private bool CheckAndInstallGLLegacyService()
        {
            ServiceController ctl = GetAndUpdateGLLegacyServiceStatusLabel();
            if (ctl == null)
            {
                UninstallGLLegacyService();
                if (!string.IsNullOrEmpty(GetGLLegacyServiceUninstallString()))
                {
                    MessageScreen d = new MessageScreen(this);
                    d.SetMessage("Couldn't uninstall GLLegacyService.");
                    d.SetType("ERROR");
                    d.TopMost = true;
                    DialogResult dres = d.ShowDialog();
                    d = null;
                    return false;
                }

                MessageScreen dlg = new MessageScreen(this);
                dlg.SetMessage("GLLegacyService is not installed!! \r\n Do you want to install it now ?");
                dlg.SetType("WARNING");
                dlg.TopMost = true;
                DialogResult dlgres = dlg.ShowDialog();
                dlg = null;
                if (dlgres == DialogResult.OK)
                {
                    InstallGLLegacyService();

                    //try to detect again if is installed or not.
                    ctl = GetAndUpdateGLLegacyServiceStatusLabel();
                    if (ctl == null)
                        return false;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                //check the version of GLLegacyService
                /*string InstallPath = (string)Registry.GetValue(@"HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\GLLegacyService", "ImagePath", null);
                if (InstallPath != null)
                {
                    String str = InstallPath.Substring(1, InstallPath.Length - 2);
                    String ver="";
                    if (File.Exists(str))
                    {
                        FileVersionInfo versionInfo = FileVersionInfo.GetVersionInfo(str);
                        ver = versionInfo.ProductVersion.Substring(0, 6);*/
                        //Version version = Assembly.GetEntryAssembly().GetName().Version;
                        var versionRunning = new Version(Globals.s_versionServiceRunning);
                        var version = new Version(Globals.s_versionService);

                        var result = versionRunning.CompareTo(version);
                        if (result >= 0)
                            return true;

                        if (Globals.s_bNewVersionQuestion)
                            return true;
                        Globals.s_bNewVersionQuestion = true;
                        MessageScreen dlg = new MessageScreen(this);
                        dlg.SetMessage("Your GLLegacyService is old (" + Globals.s_versionServiceRunning + ")!! \r\n Do you want to install " + Globals.s_versionService + " version now ?");
                        dlg.SetType("WARNING");
                        dlg.TopMost = true;
                        DialogResult dlgres = dlg.ShowDialog();
                        dlg = null;
                        if (dlgres == DialogResult.OK)
                        {
                            //uninstall the older version
                            try
                            {
                                if (ctl.CanStop)
                                    ctl.Stop();
                                ctl.Close();
                                ctl = null;
                            }
                            catch (System.Exception ex)
                            {
                                ctl = null;
                            }

                            //sc delete GLLegacyService
                            DeleteGLLegacyService();

                            UninstallGLLegacyService();
                            InstallGLLegacyService();
                            //try to detect again if is installed or not.
                            ctl = GetAndUpdateGLLegacyServiceStatusLabel();
                            if (ctl == null)
                                return false;                        
                        }
                        else
                            return true;
                }

            return true;
        }

        /**
        * EnableDISTCCServer - process distcc server state changed
        * @param enable, a boolean true if Distcc server was enabled, false if it was disabled
        **/
        private void EnableDISTCCServer(bool enable)
        {
            Globals.s_distccServer = (enable?1:0);

            if (enable)
            {
                if(!CheckAndInstallGLLegacyService())
                {
                    m_checkBoxDISTCCServer.Checked = false;
                    return;
                }
                ShowProcessingMsg();
                //save TempFile for Globals.s_distccServer is enabled and the service has to start discc
                Globals.DISTCC_SaveDistccTempFile();
                StartGLLegacyService(true);
 
                Globals.s_distccServerTemp = Path.Combine(Path.GetDirectoryName(Application.ExecutablePath), "temp");

                //labelNetAddr.Text = Globals.s_distccServerNetAddr;
                textBoxJOBS.Text = Globals.s_distccServerJobs.ToString();
                labelCORES.Text = Environment.ProcessorCount.ToString();


                m_distccServerCheck.Start();
                HideProcessingMsg();
            }
            else
            {
                Globals.DISTCC_SaveDistccTempFile();
                m_distccServerCheck.Stop();
             }
        }

        /**
        * StartGLLegacyService - start GLLegacyService
        * @param bNeedStop, a boolean true if StartGLLegacyService need to stopped before starting
        **/
        public void StartGLLegacyService(bool bNeedStop)
        {
            ServiceController ctl = GetAndUpdateGLLegacyServiceStatusLabel();
            try
            {
                if (ctl != null)
                {

                    if (ctl.Status == ServiceControllerStatus.Running)
                    {
                        if (bNeedStop)
                        {
                            ctl.Stop();
                            Thread.Sleep(2000);
                        }
                        else
                            return;
                    }
                    StopDISTCCServer();

                    ctl.Start();
                    Thread.Sleep(1000);


                    ctl = GetAndUpdateGLLegacyServiceStatusLabel();
                    GetAndUpdateDISTCCStatusLabel();
                }
            }
            catch (Exception e)
            {

            }
        }

        /**
        * EnableDISTCCUse - process distcc use status change  
        * @param enable, a boolean true if distcc use was enabled, false if was disabled
        **/
        private void EnableDISTCCUse(bool enable)
        {
            Globals.s_distccUse = (enable ? 1 : 0);

            if (enable)
            {
                ShowProcessingMsg();
                //try to start GLLegacyService
                if (!CheckAndInstallGLLegacyService())
                {
                    m_checkBoxDISTCCUse.Checked = false;
                    return;
                }

                StartGLLegacyService( false );

                DiscoverDistccHostsStart();
                HideProcessingMsg();
            }
            else
            {                
                DiscoverDistccHostsStop();                
            }
            
        }

        /**
        * CheckBoxDISTCCServer_CheckedChanged - event handler for m_checkBoxDISTCCServer.CheckedChanged 
        * @param sender an object - keep the check box status
        * @param e an EventArgs - not used
        **/
        private void CheckBoxDISTCCServer_CheckedChanged(object sender, EventArgs e)
        {
            //if (Globals.IsPlatformSelected(Globals.s_strAndroid))
            {
                m_groupBoxServer.Enabled = ((CheckBox)sender).Checked;
                EnableDISTCCServer(((CheckBox)(sender)).Checked);
            }
        }


        /**
        * CheckBoxDISTCCUse_CheckedChanged - event handler for m_checkBoxDISTCCUse.CheckedChanged
        * @param sender an object - keep the check box status
        * @param e an EventArgs - not used
        **/
        private void CheckBoxDISTCCUse_CheckedChanged(object sender, EventArgs e)
        {
            //if (Globals.IsPlatformSelected(Globals.s_strAndroid))
            {
                m_groupBoxHosts.Enabled = ((CheckBox)sender).Checked;
                comboBoxLetterDrive.Enabled = ((CheckBox)sender).Checked;
                labelVirtualDrive.Enabled = ((CheckBox)sender).Checked;
                labelDiscoverTime.Enabled = ((CheckBox)sender).Checked;
                numericUpDown.Enabled = ((CheckBox)sender).Checked;
                buttonRescan.Enabled = ((CheckBox)sender).Checked;
                checkShowServers.Enabled = ((CheckBox)sender).Checked;

                m_groupBoxSHosts.Enabled = ((CheckBox)sender).Checked;
                ipAddressControl.Enabled = ((CheckBox)sender).Checked;
                buttonAddIP.Enabled = ((CheckBox)sender).Checked;
                m_gridIPs.Enabled = ((CheckBox)sender).Checked;

                EnableDISTCCUse(((CheckBox)sender).Checked);
            }
        }

        /**
        * CheckBoxPlatformChanged - event handler for m_checkBoxPlatforms[i].CheckedChanged
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void CheckBoxPlatformChanged(object sender, EventArgs e)
        {
            //Globals.s_platformsInView.Clear();
            //for (int i = 0; i < m_checkBoxPlatforms.Length; i++)
            //{
            //    if (m_checkBoxPlatforms[i].Checked)
            //        Globals.s_platformsInView.Add(m_checkBoxPlatforms[i].Text);
            //}
                        
            //m_toolList.RefreshGridContent();
            //m_toolList.RefreshGridCellValues();
        }

        string FormatNetMask(string ip, string netMask)
        {
            int a = 0;
            string[] formattedMask = new string[] { "0", "0", "0", "0" };
            int i = 0;

            string[] bufferIP = ip.Split('.');

            string[] bufferMask = netMask.Split('.');
            foreach(string str in bufferMask)
            {
                if (str == "255")
                {
                    formattedMask[i] = bufferIP[i];
                    i++;
                    a += 8;
                }
            }

            return formattedMask[0] + "." + formattedMask[1] + "." + formattedMask[2] + "." + formattedMask[3] + "/" + a.ToString();
        }


        /**
        * textBoxJOBS_TextChanged - event for text changed for textBoxJOBS
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void textBoxJOBS_TextChanged(object sender, EventArgs e)
        {
            Globals.s_distccServerJobs = Globals.GetIntFromString(textBoxJOBS.Text);
            if (Globals.s_distccServerJobs < 0)
            {
                Globals.s_distccServerJobs = 0;
                textBoxJOBS.Text = Globals.s_distccServerJobs.ToString();
            }

            if(Globals.s_distccServerJobs > (Environment.ProcessorCount*2))
            {
                Globals.s_distccServerJobs = (Environment.ProcessorCount * 2);
                textBoxJOBS.Text = Globals.s_distccServerJobs.ToString();
            }

        }

        /**
         * comboBoxLetterDrive_SelectedIndexChanged - event handler for selection changed for combo box letter drive
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void comboBoxLetterDrive_SelectedIndexChanged(object sender, EventArgs e)
        {
            Globals.s_distccDriveSelected = comboBoxLetterDrive.Text;
        }

        /**
         * CheckComboDriveLetter - check combo drive letter if is valid
         * returns true if drive selected is valid, else return false
         **/
        private bool CheckComboDriveLetter()
        {
            if (comboBoxLetterDrive.SelectedIndex != -1)
            {
                char temp = comboBoxLetterDrive.Text[0];

                bool bIsAValidDrive = false;
                foreach (string value in Globals.s_distccDrives)
                //for (int i = 0; i < Globals.s_NumberOfNDKs; i++)
                {
                    if (!string.IsNullOrEmpty(value))
                        if (temp == value[0])
                        {
                            bIsAValidDrive = true;
                            break;
                        }
                }

                if (!bIsAValidDrive)
                {
                    MessageScreen dlg = new MessageScreen(this);
                    dlg.SetMessage("Drive Letter '" + temp + "' is not valid!! \r\n Please choose a valid letter from drop down list!");
                    dlg.SetType("WARNING");
                    dlg.TopMost = true;
                    DialogResult dlgres = dlg.ShowDialog();
                    dlg = null;
                    comboBoxLetterDrive.SelectedIndex = 0;
                    return false;
                }
                Globals.s_distccDriveSelected = temp.ToString();
                Globals.s_distccDriveSelected.ToUpper();
            }
            return true;
        }

        /**
         * comboBoxLetterDrive_TextUpdate - event handler for test update for combo box letter drive
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void comboBoxLetterDrive_TextUpdate(object sender, EventArgs e)
        {
            if (!CheckComboDriveLetter())
                return;

            Globals.s_distccDriveSelected = comboBoxLetterDrive.Text;
        }

        /**
         * CheckDISTCCServerRequirements - check if GLLegacyService is running
         * returns false if GLLegacyService is not running or is not installed, true if it is running
         */
        bool CheckDISTCCServerRequirements()
        {
            ServiceController ctl = GetAndUpdateGLLegacyServiceStatusLabel();
            if (ctl != null)
            {
                if (ctl.Status != ServiceControllerStatus.Running)
                {
                    MessageScreen dlg = new MessageScreen(this);
                    dlg.SetMessage("The GLLegacyService does not work!! Go back and press Restart button.\r\n");
                    dlg.SetType("WARNING");
                    dlg.TopMost = true;
                    DialogResult dlgres = dlg.ShowDialog();
                    dlg = null;
                    return false;
                }
            }
            else
            {
                MessageScreen dlg = new MessageScreen(this);
                dlg.SetMessage("The GLLegacyService isn't installed!! \r\n");
                dlg.SetType("WARNING");
                dlg.TopMost = true;
                DialogResult dlgres = dlg.ShowDialog();
                dlg = null;
                return false;
            }

            return true;
        }

        
        private void DiscoverDistccHostsStart()
        {
            this.pictureBox3.Visible = true;

            if (m_discoverTimer == null)
            {
                TimerCallback callback = new TimerCallback(SendDiscoverSignal);
                m_discoverTimer = new System.Threading.Timer(callback, 0, 10000, 10000);
            }

            if ((m_threadDisccDiscover == null) && (s_sock == null))
            {
                try
                {
                    s_sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
                    s_sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);

                    IPAddress _ipAddress = null;
                    if(string.IsNullOrEmpty(Globals.s_distccIPUsed))
                        _ipAddress = IPAddress.Parse(Globals.GetLocalIP());
                    else
                        _ipAddress = IPAddress.Parse(Globals.s_distccIPUsed);

                    if (_ipAddress != null)
                    {
                        s_iep = new IPEndPoint(_ipAddress, Globals.s_dataPort);
                    }
                    else 
                    {
                        MessageBox.Show("You do not have an ip address!!!");
                        s_sock = null;
                        return;
                    }
                    s_sock.Bind(s_iep);

                }
                catch (Exception e)
                {
                    s_sock = null;
                   
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

                m_threadDisccDiscover = new Thread(new ThreadStart(DiscoverDistccHostsWorkerThread));

            }

            try
            {
                if (m_threadDisccDiscover.ThreadState != System.Threading.ThreadState.Running)
                    m_threadDisccDiscover.Start();
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
            }
        }



       /**
        * SendDiscoverSignal- will send UDP (broadcast) messages to inform all the computers in the network 
        *                   - about the availability of this computer.
        *                   - This function is used as a callback for a timer.
        **/
        static void SendDiscoverSignal(Object state)
        {
            Socket sock = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);

            IPAddress ip = null;
            if (string.IsNullOrEmpty(Globals.s_distccIPUsed))
                ip = IPAddress.Parse(Globals.GetLocalIP());
            else
                ip = IPAddress.Parse(Globals.s_distccIPUsed);
            sock.Bind(new IPEndPoint(ip,Globals.s_discoverPort));
            
            string hostname = Dns.GetHostName();
            byte[] data = Encoding.ASCII.GetBytes(hostname);
            sock.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.Broadcast, 1);
            sock.SendTo(data, new IPEndPoint(IPAddress.Broadcast, Globals.s_discoverPort));
            

            sock.Close();
            sock = null;            
        }

        /**
         * DiscoverDistccHostsStop - stops the thread that discovers hosts
         **/
        private void DiscoverDistccHostsStop()
        {
            this.pictureBox3.Visible = false;
            if (m_discoverTimer != null)
            {
                m_discoverTimer.Dispose();
                m_discoverTimer = null;
            }

            if (m_threadDisccDiscover != null)
            {
                m_threadStopRequest = 1;
                m_threadDisccDiscover.Join();
                m_threadStopRequest = 0;
            }

            if (s_sock != null)
            {
                s_sock.Close();
                s_sock = null;
            }

            s_iep = null;
            m_threadDisccDiscover = null;
        }

        /**
         * DiscoverDistccHostsWorkerThread - receive messages from hosts and add it in host list
         **/
        static void DiscoverDistccHostsWorkerThread()
        {
            while(true)
            {
                if (m_threadStopRequest == 1)
                    return;
                 
                try
                {

                    if (s_sock.Available > 0)
                    {
                        EndPoint ep = (EndPoint)s_iep;

                        byte[] data = new byte[1024];
                        int recv = s_sock.ReceiveFrom(data, ref ep);
                        string stringData = Encoding.ASCII.GetString(data, 0, recv);

                        if (MainWindow.s_instance.m_hostsList != null)
                        {
                            String[] param = Globals.DeSerializeInfoParams(stringData);
                            Host h = new Host(-1, param);
                            MainWindow.s_instance.m_hostsList.Add(h);
                            

                            h = null;
                        }
                        data = null;
                    }

                }
                catch(Exception e)
                {
                    String errMsg = e.StackTrace.ToString();
                    
                    Console.WriteLine(errMsg);
                }

                Thread.Sleep(1000);
            }
        }


        /**
         * exitToolStripMenuItem_Click - event handler for click on exitToolStripMenuItem
         * @param sender an object - not used
         * @param e an FormClosingEventArgs - not used
         **/
        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        /**
         * MainWindow_FormClosing - event handler for main window closing
         *                        - stop distcc host discovery
         * @param sender an object - not used
         * @param e an FormClosingEventArgs - not used
         **/
        private void MainWindow_FormClosing(object sender, FormClosingEventArgs e)
        {
            DiscoverDistccHostsStop();
        }



        /**
         * TimerCheckDistccServerStatus - event handler m_distccServerCheck timer
         *                              - check distcc server and GLLegacyService status
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void TimerCheckDistccServerStatus(object sender, EventArgs e)
        {
            GetAndUpdateDISTCCStatusLabel();
            GetAndUpdateGLLegacyServiceStatusLabel();
        }


        /**
         *TimerCheckDistccHostFileStatus - event handler m_distccHostFileCheck timer
         *                              - check distcc host file availability (GLLegacyDistccHosts.bat)
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void TimerCheckDistccHostFileStatus(object sender, EventArgs e)
        {
            string path = Globals.GetFullPathToDistccHostsBatchScript();
            if (File.Exists(path))
            {
                this.buttonDistccHosts.Enabled = true;
                this.labelGLLegacyDistccHostsPath.Text = path;
            }
            else
            {
                this.buttonDistccHosts.Enabled = false;
                this.labelGLLegacyDistccHostsPath.Text = "Not available yet. Please wait DISTCC settings to become ready for use.";
            }

        }

        /**
         * ButtonInstallToolsClick - event handler for click on install button
         *                         - call InstallMissingTools()
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void ButtonInstallToolsClick(object sender, EventArgs e) 
        {
            InstallMissingTools();
        }

        public Bitmap GetToolBitmap(String toolName)
        {
            Bitmap img = global::GLLegacyInstaller.Properties.Resources.NDKLogo;
            switch (toolName.ToLower())
            {
                case "python 2.7 with pil":
                    img = global::GLLegacyInstaller.Properties.Resources.PythonLogo; break;
                case "Java sdk (aka jdk)":
                    img = global::GLLegacyInstaller.Properties.Resources.JDKLogo; break;
                case "apache-ant":
                    img = global::GLLegacyInstaller.Properties.Resources.AntLogo; break;
                case "android sdk":
                    img = global::GLLegacyInstaller.Properties.Resources.ADTLogo; break;
                case "android-ndk":
                    img = global::GLLegacyInstaller.Properties.Resources.NDKLogo; break;
                case "cygwin":
                    img = global::GLLegacyInstaller.Properties.Resources.CygwinLogo; break;
            }
            return img;
        }
        /**
         * InstallMissingTools - call IfNeededExecuteScriptTool for every tool
         *                     - install pyton and Java SDK first if needed 
         **/
        private void InstallMissingTools() 
        {
            for (int i = 0; i < m_toolList.GetCount(); ++i)
            {
                Tool temptool = (Tool)m_toolList.Get(i);
                if (temptool.m_toolName.ToLower() == "Python 2.7 with pil".ToLower())
                    if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.PythonLogo) != true) return;
                if (temptool.m_toolName.ToLower() == "Java SDK (aka JDK)".ToLower())
                    if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.JDKLogo) != true) return;
            }
            for (int i = 0; i < m_toolList.GetCount(); ++i)
            {
                Tool temptool = (Tool)m_toolList.Get(i);
                if (temptool.m_toolName.ToLower() == "TortoiseSVN".ToLower())
                {
                    if (temptool.m_valid == false)
                    {
                        MessageScreen erdlg = new MessageScreen(this);
                        erdlg.SetMessage("Please get and install TurtoiseSVN from http://tortoisesvn.net/.\r\n");
                        erdlg.SetType("Warning");
                        erdlg.TopMost = true;
                        DialogResult erdlgres = erdlg.ShowDialog();
                        erdlg = null;
                        continue;
                    }

                }

                if (string.IsNullOrEmpty(temptool.m_InstallScriptPath) == true) continue; //|| temptool.m_required != true

                Bitmap img = GetToolBitmap( temptool.m_toolName );
                
                if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, img) != true) return;

                /*if (temptool.m_toolName.ToLower() == "apache-ant".ToLower())
                {
                    if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.AntLogo) != true) return;
                } 
                else if (temptool.m_toolName.ToLower() == "Android SDK".ToLower())
                {
                    if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.ADTLogo) != true) return;
                }
                else if (temptool.m_toolName.ToLower() == "android-ndk".ToLower())
                {
                    if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.NDKLogo) != true) return;
                }
                else if (temptool.m_toolName.ToLower() == "Cygwin".ToLower())
                {
                    if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.CygwinLogo) != true) return;
                }
                else if (temptool.m_toolName.ToLower() == "notepad++".ToLower())
                {
                    IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.NDKLogo);
                }
                else
                {
                    if (IfNeededExecuteScriptTool(ref temptool, temptool.m_toolName, global::GLLegacyInstaller.Properties.Resources.NDKLogo) != true) return;
                }*/
            }            
        }

        /**
         * StartScriptToolProcess - start a process to execute a batch
         * @param filePath a string for batch file with path
         * @param arguments a string with arguments for process
         * returns an int with error code
         **/
        private int StartScriptToolProcess(String filePath, String arguments) 
        {
            System.Diagnostics.Process proc = new System.Diagnostics.Process();
            proc.StartInfo.FileName = filePath;
            proc.StartInfo.RedirectStandardError = false;
            proc.StartInfo.RedirectStandardInput = false;
            proc.StartInfo.RedirectStandardOutput = false;
            proc.StartInfo.CreateNoWindow = true;
            proc.StartInfo.UseShellExecute = true;
            proc.StartInfo.Arguments = arguments;
            proc.Start();
            proc.WaitForExit();
            int errorCode = proc.ExitCode;
            proc = null;
            return errorCode;
        }

        /**
         * IfNeededExecuteScriptTool - show a message with tool to be installed
         *                           - execute a script for install a tool
         * @param t a ref Tool - tool to be installed
         * @param msg a string - message with name of the tool o be displayed in message box
         * @param icon an Image - icon to be set to message box
         * returns a bool it installation was successful
         **/
        public bool IfNeededExecuteScriptTool(ref Tool t, string msg, Image icon, bool bForce = false)
        {
            String filePath = proCompletePath(t.m_InstallScriptPath);
            if (bForce || (File.Exists(filePath) && t.m_valid == false && t.IsUsedTool()))
            {
                InstallAnnouncement inst = new InstallAnnouncement(this);
                inst.SetMessage(msg);
                inst.SetIcon(icon);
                DialogResult diag = inst.ShowDialog();
                if (diag == DialogResult.OK) {
                    int erorrCode = StartScriptToolProcess(filePath, inst.GetFolder());
                    //inst = null;
                    if (erorrCode != 0)
                    {
                        MessageScreen erdlg = new MessageScreen(this);
                        erdlg.SetMessage(t.m_toolName + " Script install process has failed. \r\n Check for error messages issued by the script.\n");
                        erdlg.SetType("Error");
                        erdlg.TopMost = true;
                        DialogResult erdlgres = erdlg.ShowDialog();
                        erdlg = null;
                        return false;
                    }
                    else
                    {
                        //t.m_path = inst.GetFolder();
                        InstallPathBase installPath = new InstallPathBase(inst.GetFolder(), "");
                        t.m_installPaths.Insert(0, installPath);
                        m_toolList.RefreshGridCellValues();
                        t.m_path = inst.GetFolder();
                        t.m_valid = t.IsInstallPathValid(inst.GetFolder());
                        m_toolList.DetectToolsInstallPath();
                        m_toolList.ApplyDetectToolsInstallPath();
                        return true;
                    }

                }
                if (diag == DialogResult.Cancel) {
                    return true;
                }
            }
            else if (File.Exists(filePath) == false)
            {
                BigMessageScreen erdlg = new BigMessageScreen();
                erdlg.SetMessage("\n\n\n\t" + t.m_toolName + " auto installer script is missing from the specified path:\n\n " + filePath + " \n\r\n\t directed from:\n\n " + System.IO.Directory.GetCurrentDirectory() + Path.DirectorySeparatorChar + "InstallerSetup.xml. \r\n\n\t Verify if script exist where it should and the specified path is right. \r\n\n\t If InstallerSetup.xml is modified, restart GLLegacyInstaller.\r\n");
                erdlg.SetType("Error");
                erdlg.TopMost = true;
                DialogResult erdlgres = erdlg.ShowDialog();
                erdlg = null;
                return false;
            }
            return true;
        }

        /**
         * SendErrorMessage - show a MessageScreen (a dialog)
         * @param what  - a string to be displayed by dialog
         **/
        private void SendErrorMessage(String what) 
        {
            MessageScreen erdlg = new MessageScreen(this);
            erdlg.SetMessage(what);
            erdlg.SetType("Warning");
            erdlg.TopMost = true;
            DialogResult erdlgres = erdlg.ShowDialog();
            erdlg = null;       
        }


        /**
         * installerDocsToolStripMenuItem_Click - event handler for click on installerDocsToolStripMenuItem button
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void installerDocsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Process.Start("https://docs.gameloft.org/gllegacy-installer/");
        }

        /**
         * buttonEnv_Click - event handler for click on buttonEnv button
         *                 - open GLLegacyInstallerEnv.bat in text editor
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void buttonEnv_Click(object sender, EventArgs e)
        {
            Process.Start(Globals.s_notepad, Globals.GetFullPathToInstallerEnvBatchScript());
        }

        /**
         * buttonDistccEnv_Click - event handler for click on buttonDistccEnv
                                 - open GLLegacyDistccEnv.bat in text editor
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void buttonDistccEnv_Click(object sender, EventArgs e)
        {
            Process.Start(Globals.s_notepad, Globals.GetFullPathToDistccEnvBatchScript());
        }

        /**
         * buttonDistccHosts_Click  - event handler for click on buttonDistccHosts button
         *                          - open GLLegacyDistccHosts.bat in text editor
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void buttonDistccHosts_Click(object sender, EventArgs e)
        {
            Process.Start(Globals.s_notepad, Globals.GetFullPathToDistccHostsBatchScript());
        }

        /**
         * buttonInstallerSetup_Click - event handler for click on buttonInstallerSetup button
         *                            - call OpenInstallerSetupAndReload(); 
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void buttonInstallerSetup_Click(object sender, EventArgs e)
        {
            OpenInstallerSetupAndReload();
        }

        public void OpenInstallerSetupAndReload()
        {
            //Process.Start("notepad.exe", proCompletePath(Globals.s_settingsFile));
            System.Diagnostics.Process proc = new System.Diagnostics.Process();
            //proc.StartInfo.WorkingDirectory = Path.GetDirectoryName("notepad.exe");
            proc.StartInfo.FileName = "notepad.exe";         
            proc.StartInfo.Arguments = proCompletePath(Globals.s_settingsFile);
            proc.Start();
            proc.WaitForExit();

            Tool temptoop = (Tool)m_toolList.Get(0);
            if (temptoop != null)
                temptoop.ResetID();
            m_toolList.RemoveAll();
            m_toolList = null;
            m_toolList = new ToolsList(this.m_gridTools);
            Globals.s_platforms.Clear();
            //Globals.s_platformsInView.Clear();
            if (LoadSettings())
            {
                m_toolList.DetectToolsInstallPath();
                m_toolList.ApplyDetectToolsInstallPath();
            }
            m_toolList.RefreshGridContent();
            m_toolList.RefreshGridCellValues();
        }

        /**
         * buttonRescan_Click - event handler for click on buttonRescan button
         *                    - if was a change in host list ask user if want to save it and delete hosts from windows (for refill it)
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void buttonRescan_Click(object sender, EventArgs e)
        {
            if (m_hostsList.m_bSelectionChanged)
            {
                MessageScreen dlg = new MessageScreen(this);
                dlg.SetMessage("Selection changed. Save changes before rescan?");
                dlg.SetType("WARNING");
                dlg.TopMost = true;
                DialogResult dlgres = dlg.ShowDialog();
                dlg = null;
                if (dlgres == DialogResult.OK)
                {
                    //WriteEnvVarAndCopyBatchFiles();
                    WriteDistccEnvAndCopyBatchFile();
                }
            }
            m_hostsList = new HostsList(this.m_gridAvailableHosts);
            m_hostsList.RefreshGridStructure();
        }

        /**
         * buttonReinstallService_Click - event handler for click on buttonReinstallService button
         *                              - delete GLLegacyService, uninstall it and reinstall it
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void buttonReinstallService_Click(object sender, EventArgs e)
        {
            MessageScreen dlg = new MessageScreen(this);
            ServiceController ctl = GetAndUpdateGLLegacyServiceStatusLabel();
            if (ctl == null)
            {
                dlg.SetMessage("Are you sure you want to install GLLegacyService?");
            }
            else
            {
                dlg.SetMessage("Are you sure you want to reinstall GLLegacyService?");
            }
            dlg.SetType("WARNING");
            dlg.TopMost = true;
            DialogResult dlgres = dlg.ShowDialog();
            dlg = null;
            if (dlgres == DialogResult.OK)
            {
                if (ctl != null)
                {
                    if (Globals.s_distccUse == 1)
                    {
                        DiscoverDistccHostsStop();
                    }
                    //uninstall the older version
                    try
                    {
                        if (ctl.CanStop)
                            ctl.Stop();
                        ctl.Close();
                        ctl = null;
                    }
                    catch (Exception ex)
                    {
  
                    }
                    //sc delete GLLegacyService
                    DeleteGLLegacyService();
                    UninstallGLLegacyService();
                }

                InstallGLLegacyService();
                StartGLLegacyService(false);
                //try to detect again if is installed or not.
                ctl = GetAndUpdateGLLegacyServiceStatusLabel();
                if (Globals.s_distccUse == 1 && ctl != null)
                {
                    DiscoverDistccHostsStart();
                }

            }
        }

        /**
         * buttonOpenLogFile_Click - event handler for click on buttonOpenLogFil button
         *                         - open GLLegacyService log file in text editor 
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void buttonOpenLogFile_Click(object sender, EventArgs e)
        {
            Process.Start(Globals.s_notepad, Globals.GetFullPathToGLLegacyServiceLogFile());
        }

        /**
         * comboBoxNetAddr_SelectedIndexChanged - event handler for selection changed on net address combo box
         *                                      - stop discover hosts thread and restart it
         * @param sender an object - not used
         * @param e an EventArgs - not used
         **/
        private void comboBoxNetAddr_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (Globals.s_distccServerNetAddr != comboBoxNetAddr.Text)
            {
                Globals.s_distccServerNetAddr = comboBoxNetAddr.Text;
                Globals.s_distccIPUsed = Globals.s_IPs[comboBoxNetAddr.SelectedIndex].ToString();
                if (Globals.s_distccUse == 1)
                {
                    DiscoverDistccHostsStop();
                    m_hostsList = new HostsList(this.m_gridAvailableHosts);
                    m_hostsList.RefreshGridStructure();
                    DiscoverDistccHostsStart();
                }
            }
        }

        /**
         * checkShowServers_CheckedChanged - event handler for checked changed on show servers check box
         *                                 - refresh grid content
         * @param sender an object - used to get the new state (check or not check)
         * @param e an EventArgs - not used
         **/
        private void checkShowServers_CheckedChanged(object sender, EventArgs e)
        {
            Globals.s_bShowOnlyServers = ((CheckBox)sender).Checked;
            if (Globals.s_distccUse == 1)
            {
                m_hostsList.RefreshGridStructure();
                m_hostsList.RefreshGridContent();
            }
        }

        private void buttonIPs_Click(object sender, EventArgs e)
        {
           string ipString = ipAddressControl.ToString();
           ipAddressControl.Clear();
           int gridRows = m_gridIPs.RowsCount;
           m_gridIPs.Rows.Insert(gridRows);
           m_gridIPs[gridRows, 0] = new SourceGrid.Cells.Cell(ipString, typeof(string));
           m_gridIPs[gridRows, 0].Editor.EditableMode = SourceGrid.EditableMode.None;
          // m_gridIPs[gridRows, 0].ColumnSpan = 4;
           m_gridIPs[gridRows, 0].View = new SourceGrid.Cells.Views.Cell();

           m_gridIPs[gridRows, 1] = new SourceGrid.Cells.CheckBox("", false);
           m_gridIPs[gridRows, 1].View = new SourceGrid.Cells.Views.CheckBox();
        }

    }



}
