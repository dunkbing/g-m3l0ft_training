using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Security.Permissions;
using System.Diagnostics;
using Microsoft.Win32;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Windows.Forms;
using System.ServiceProcess;


namespace Utils
{
    partial class Globals
    {
        //GLLegacyService version
        public static String s_versionService = "1.1.7";
        //version of GLLegacyService installed
        public static String s_versionServiceRunning = "";
        //distcc process name
        public static String s_distccdProcessName = "distccd";
        //GLLegacySrvice log file name
        public static String s_logFileName = "GLLegacyService.log";

        //port number - discover messages will be sent to
        public static int s_discoverPort = 9050;
        //port number - data will be sent to GLLegacyInstaler
        public static int s_dataPort = 9051;
        //port number - data will be sent to GLLegacyService
        public static int s_dataPortService = 9052;

        //send request time in miliseconds
        public static int s_timeSendRequest = 0;
        //time for delete host, for set a timer
        public static int s_timeDeleteHost = 0;
        //milliseconds in one minute
        public static int s_ms = 60000;
        //discover time set in GLLegasyInstaller in minutes
        public static int s_timeDiscover = 5; 

        public static bool s_bValidDrivesLetter = true;
        public static String s_InvalidDrivesLetter = "";
        public static bool s_bValidPlatformForTool = true;
        public static String s_InvalidPlatformForTool = "";

        public static String s_envVar_DISTCC_USE            = "GLLEGACY_DISTCC";
        public static String s_envVar_DISTCC_USE_HOSTS      = "GLLEGACY_DISTCC_USE_HOSTS";
        public static String s_envVar_DISTCC_SERVER         = "GLLEGACY_DISTCC_SERVER";
        //public static String s_envVar_NDK_HOME_DRIVE        = "GLLEGACY_NDK_HOME_DRIVE_";
        public static String s_envVar_DISTCC_DRIVE_SELECTED = "GLLEGACY_DISTCC_DRIVE_SELECTED";
        public static String s_envVar_DISTCC_SERVER_JOBS    = "GLLEGACY_DISTCC_SERVER_JOBS";
        public static String s_envVar_DISTCC_SERVER_TEMP    = "GLLEGACY_DISTCC_SERVER_TEMP";
        public static String s_envVar_DISTCC_SERVER_NETADDR = "GLLEGACY_DISTCC_SERVER_NETADDR";
        public static String s_envVar_DISTCC_SERVER_SCRIPT  = "GLLEGACY_DISTCC_SERVER_SCRIPT";

        //public static String s_envVar_GLLEGACY_CYGWIN_BIN   = "GLLEGACY_CYGWIN_BIN";
        public static String s_envVar_GLLEGACY_NDK_HOME     = "GLLEGACY_NDK_HOME";

        //int 1/0 if distcc is used or not
        public static int s_distccUse = 0;
        //string with name of hosts to e used by distcc
        public static string s_distccUseHosts = "";
        //string with name of hosts selected by user to be used by distcc
        public static string s_distccWantedHosts = "";
        //array of string with name of hosts selected by user to be used by distcc
        public static String[] s_hostsWanted = null;

        //string array with distcc static hosts list
        public static string s_distccStaticHostList = "";
        //array of distcc static hosts list
        //public static String[] s_hostsStaticWanted = null;
        
        //string array with distcc drives read from xml file
        public static ArrayList s_distccXMLDrives = new ArrayList();
        //string array with distcc drives (saved by GLLegacyInstallers and load by GLLegacyService )
        public static ArrayList s_distccDrives = new ArrayList();
        //string array with distcc names of NDK
        public static ArrayList s_distccNameNDK = new ArrayList();
        //string array with names of environment variables for each NDK path
        public static ArrayList s_distccNDKEnvVar = new ArrayList();
        //string with distcc drive selected for use
        public static String s_distccDriveSelected = null;
        public static bool b_restartService = false;

        //string array with platforms name
        public static ArrayList s_platforms = new ArrayList();
        //string array with platforms selected in view by user
        public static ArrayList s_platformsInView = new ArrayList();

        //int 1/0 if distcc server is used or not
        public static int s_distccServer = 0;
        //int for number of distcc server jobs
        public static int s_distccServerJobs = 0;
        //string for distcc server 
        public static string s_distccServerTemp = "";
        //string for net address used by GLLegacyService
        public static string s_distccServerNetAddr = "";
        //string for IP used by GLLegacyService
        public static string s_distccIPUsed = "";
        //string for distcc server script
        public static string s_distccServerScript = "startDistccDaemon.bat";
        static public string s_strAndroid = "ANDROID";

        //string array with local IPS
        public static ArrayList s_IPs = new ArrayList();
        //boolean to know if local IPS list is initialized
        public static bool s_bAreInitIPs = false;
        //public static int s_nIPUsed = 0;

        //public static string s_cygwinBin = "";
        public static string s_ndkHome = "";

        //boolean used in interface to show only hosts that are server
        public static bool s_bShowOnlyServers = true;

        //boolean that show if there is a net error
        public static bool s_bNetError = false;

        public static System.Collections.IDictionary s_myEnvars = new Dictionary<string, string>();
        
        
        

        public static Char s_paramSep = '|';
        //string array with parameters need for a host
        public static String[] s_infoParams = new String[] { "HOST", 
                                                             "VER",   
                                                             "SERVER",
                                                             "DRIVE",
                                                             "CORES",
                                                             "CPU_USAGE"
                                                             };

        /**
        * SerializeInfoParams - This will serialize a table of strings. The strings will be separated by a separator declared in s_paramSep
        *@ param infoParams - This must be a table with strings
        *returns - a string with all the values from table
        **/
        public static String SerializeInfoParams(string[] infoParams)
        {
            String temp = "";
            for (int i = 0; i < infoParams.Length; i++)
            {
                temp += infoParams[i] + s_paramSep;
            }

            return temp;
        }

        /**
        * DeSerializeInfoParams - This will de-serialize a string. The elements from the string must be separated by a separator declared in s_paramSep
        * @param serializedStr - this is the string that must be deserialized
        */
        public static String[] DeSerializeInfoParams(String serializedStr)
        {
            return serializedStr.Split(s_paramSep);
        }


        /**
        * IsPlatformSelected - test if platform given as param is selected in view (s_platformsInView)
        * @param platform - string with platform name
        * returns true if platform is selected in view, else returns false 
        */
        public static bool IsPlatformSelected(string platform)
        {
            foreach (string p in Globals.s_platformsInView)
                if (p.ToUpper() == platform)
                    return true;
            return false;
        }


        /**
        * DISTCC_ReadEnvVar - Read the main DISTCC environment variables.
        */
        public static void DISTCC_ReadEnvVar()
        {
            String value = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_USE);
            if (value != null)
            {
                Globals.s_distccUse = Globals.GetIntFromString(value);
            }


            Globals.s_distccUseHosts = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_USE_HOSTS);


            value = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER);
            if (value != null)
            {
                Globals.s_distccServer = Globals.GetIntFromString(value);
            }

            String drive = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_DRIVE_SELECTED);
            Globals.s_distccDriveSelected = drive;

            value = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_JOBS);
            if (value != null)
            {
                Globals.s_distccServerJobs = Globals.GetIntFromString(value);
            }

            ///This values are generated, so do not need to read them here. To read them use DISTCC_ReadEnvVarEx
            //Globals.s_distccServerTemp = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_TEMP);
            //Globals.s_distccServerNetAddr = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_NETADDR);
            //Globals.s_distccServerScript = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_SCRIPT);
            ///This values are generated, so do not need to read them here. To read them use DISTCC_ReadEnvVarEx


            //Globals.s_cygwinBin = Globals.GetEnvironmentVar(Globals.s_envVar_GLLEGACY_CYGWIN_BIN);

            Globals.s_ndkHome = Globals.GetEnvironmentVar(Globals.s_envVar_GLLEGACY_NDK_HOME);
        }


        /**
        * DISTCC_ReadEnvVarEx - Read all the DISTCC environment variables, including the variables that are generated!
        */
        public static void DISTCC_ReadEnvVarEx()
        {
            DISTCC_ReadEnvVar();

            Globals.s_distccServerTemp = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_TEMP);
            Globals.s_distccServerNetAddr = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_NETADDR);
            //Globals.s_distccServerScript = Globals.GetEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_SCRIPT);
        }



        /**
        * DISTCC_WriteEnvVar - Write all the DISTCC environment variables, including the variables that are generated!
        */
        public static void DISTCC_WriteEnvVar()
        {
            Globals.ResetEnvironmentVarBatchScript(GetFullPathToDistccEnvBatchScript());

            String errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_SERVER, Globals.s_distccServer.ToString(), GetFullPathToDistccEnvBatchScript(), true);

            for (int i = 0; i < Globals.s_distccDrives.Count; i++)            
            {
                errorMessage = Globals.AddEnvironmentVar(Globals.s_distccNDKEnvVar[i]+"_DRIVE", (string)Globals.s_distccDrives[i], GetFullPathToDistccEnvBatchScript(), true);
            }

            errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_DRIVE_SELECTED, (Globals.s_distccUse==1)?Globals.s_distccDriveSelected:"", GetFullPathToDistccEnvBatchScript(), true);

            errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_JOBS, Globals.s_distccServerJobs.ToString(), GetFullPathToDistccEnvBatchScript(), true);


            errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_TEMP, Globals.s_distccServerTemp, GetFullPathToDistccEnvBatchScript(), true);


            errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_NETADDR, Globals.s_distccServerNetAddr, GetFullPathToDistccEnvBatchScript(), true);


            //errorMessage = Globals.AddEnvironmentVar(Globals.s_envVar_DISTCC_SERVER_SCRIPT, Globals.s_distccServerScript, GetFullPathToDistccEnvBatchScript(), true);
            //only if the GLLegacyService is installed call GLLegacyDistccHosts.bat
            ServiceController ctl = ServiceController.GetServices().Where(s => s.ServiceName == "GLLegacyService").FirstOrDefault();
            if (ctl != null)
            {
                StreamWriter stream = new StreamWriter(GetFullPathToDistccEnvBatchScript(), true);
                stream.WriteLine("call %WINDIR%\\GLLegacyDistccHosts.bat");
                stream.Close();
                stream = null;
            }
        }


        public static void LoadVariableVars()
        {
            System.Collections.IDictionary envVars = null;
            envVars = Environment.GetEnvironmentVariables();

            String temp = GetFullPathToInstallerEnvBatchScript();
            if (!string.IsNullOrEmpty(temp))
            {
                if (File.Exists(temp))
                {
                    StreamReader str = new StreamReader(temp);
                    String line = str.ReadLine();
                    line = str.ReadLine();
                    while(!string.IsNullOrEmpty(line)) 
                    {
                        if (line.IndexOf("set") == 0)
                        {
                            line = line.Substring(4);
                            int index = line.IndexOf('=');
                            string name = line.Substring(0, index);
                            string value = line.Substring(index + 1, line.Length - index - 1);
                            //string envVal = System.Environment.GetEnvironmentVariable(name, EnvironmentVariableTarget.User);
                            if (name != "PATH" && !Globals.s_myEnvars.Contains(name))
                            {
                                Globals.s_myEnvars.Add(name, value);
                            }
                            //string returnCode = AddEnvironmentVar(name, value);
                        }
                        line = str.ReadLine();
                    }

                    str.Close();
                    str = null;
                }
            }
        }

        /**
        * DISTCC_SaveDisccTempFile - Save the distcc temp file(GLLegacyDisccTemp.txt) with:
        *                              - host wanted list
        *                              - use or not distcc (s_distccUse) (1/0)
        *                              - use or not distcc server (s_distccServer) (1/0)
        *                              - number of distcc drives
        *                              - list of distcc drives
        *                              - distcc drive selected
        *                              - discover time
        *                              - IP used
        *                              
        */
        public static void DISTCC_SaveDistccTempFile()
        {
            String temp = GetFullPathToDistccTempFile();
            if (!string.IsNullOrEmpty(temp))
            {
                //s_mutex.WaitOne();
                StreamWriter str = new StreamWriter(temp, false);
                if (Globals.s_distccUse == 1)
                    str.WriteLine(Globals.s_distccWantedHosts);
                else
                    str.WriteLine("");
                str.WriteLine(Globals.s_distccUse.ToString());
                str.WriteLine(Globals.s_distccServer.ToString());
                str.WriteLine(Globals.s_distccDrives.Count.ToString());

                foreach( string value in Globals.s_distccDrives )
                //for (int i = 0; i < Globals.s_distccDrives.Count; i++)
                    str.WriteLine(value);
                
                str.WriteLine(Globals.s_distccDriveSelected);
                str.WriteLine(Globals.s_timeDiscover);
               // str.WriteLine(Globals.s_distccServerNetAddr);
                str.WriteLine(Globals.s_distccIPUsed);


                str.WriteLine(Globals.s_distccStaticHostList);

                str.Close();
                str = null;

                if (!string.IsNullOrEmpty(Globals.s_distccWantedHosts))
                    Globals.s_hostsWanted = Globals.s_distccWantedHosts.Split(' ');
                else
                    Globals.s_hostsWanted = null;
            }
        }

        /**
         * DISTCC_LoadDisccTempFile - load the distcc temp file(GLLegacyDisccTemp.txt) with
         * @param bLoadServerDrives - bolean that say if to load the list with distcc drives
         */
        public static void DISTCC_LoadDisccTempFile(bool bLoadServerDrives)
        {
            String temp = GetFullPathToDistccTempFile();
            if (!string.IsNullOrEmpty(temp))
            {
                if (File.Exists(temp))
                {
                    StreamReader str = new StreamReader(temp);
                    Globals.s_distccWantedHosts = str.ReadLine();
                    Globals.s_distccUse = Globals.GetIntFromString(str.ReadLine());
                    Globals.s_distccServer = Globals.GetIntFromString(str.ReadLine());
                    
                    String tmp = null;
                    int iNumberOfNDKs = Globals.GetIntFromString(str.ReadLine());
                    //foreach (string value in Globals.s_distccDrives)
                    if (bLoadServerDrives)
                        Globals.s_distccDrives.Clear();
                    for (int i = 0; i < iNumberOfNDKs; i++)
                    {
                        tmp = str.ReadLine();
                        if (bLoadServerDrives)
                            Globals.s_distccDrives.Add(tmp);
                    }

                    tmp = str.ReadLine();
                    //for (int i = 0; i < Globals.s_distccDrives.Length; i++)
                    foreach (string value in Globals.s_distccDrives)
                    {
                        if (tmp == value)
                        {
                            Globals.s_distccDriveSelected = tmp;
                            break;
                        }
                    }
                    s_timeDiscover = Globals.GetIntFromString(str.ReadLine());
                    s_timeSendRequest = s_timeDiscover*Globals.s_ms;
                    s_timeDeleteHost = s_timeSendRequest;
                    tmp = str.ReadLine();
                    if( !string.IsNullOrEmpty(tmp) )
                        Globals.s_distccIPUsed = tmp;

                    Globals.s_distccStaticHostList = str.ReadLine();

                    str.Close();
                    str = null;

                    if (!string.IsNullOrEmpty(Globals.s_distccWantedHosts))
                        Globals.s_hostsWanted = Globals.s_distccWantedHosts.Split(' ');
                    else
                        Globals.s_hostsWanted = null;

                   // if (!string.IsNullOrEmpty(Globals.s_distccStaticHostList))
                    //    Globals.s_hostsStaticWanted = Globals.s_distccStaticHostList.Split(' ');
                    //else
                    //    Globals.s_hostsStaticWanted = null;
                }
            }
        }

        /**
        * GetSubnetMask - This will calculate the subnet mask for an ip address
        * @param address- this is an ip address
        * returns an IPAddress that means the netmask
        */
        public static IPAddress GetSubnetMask(IPAddress address)
        {
            foreach (NetworkInterface adapter in NetworkInterface.GetAllNetworkInterfaces())
            {
                foreach (UnicastIPAddressInformation unicastIPAddressInformation in adapter.GetIPProperties().UnicastAddresses)
                {
                    if (unicastIPAddressInformation.Address.AddressFamily == AddressFamily.InterNetwork)
                    {
                        if (address.Equals(unicastIPAddressInformation.Address))
                        {
                            return unicastIPAddressInformation.IPv4Mask;
                        }
                    }
                }
            }
            throw new ArgumentException(string.Format("Can't find subnetmask for IP address '{0}'", address));
        }
       
        /**
        * GetFullPathToGLLegacyServiceLogFile - Get the path and file to GLLegacyService.log file
        * returns a string with the path
        */ 
        public static string GetFullPathToGLLegacyServiceLogFile()
        {
            //string str = Environment.GetEnvironmentVariable("temp", EnvironmentVariableTarget.Machine);
            String str = Globals.GetRegValue("SYSTEM\\CurrentControlSet\\services\\GLLegacyService", "ImagePath");
            if (str != null)
            {
                str = str.Replace('"', ' ');
                str = str.Trim();                
            }
            else
                str = "C:\\";
            
            return Path.Combine(Path.GetDirectoryName(str), Globals.s_logFileName);
        }
    }
}
