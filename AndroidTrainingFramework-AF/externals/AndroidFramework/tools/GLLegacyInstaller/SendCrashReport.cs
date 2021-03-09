using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Globalization;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using System.Diagnostics;
using Microsoft.Win32;

namespace Utils
{
    public partial class SendCrashReport : Form
    {
        string stackTrace;

        /**
        * SendCrashReport - send crash report
        * @param crashMessage - a string with crash message
        * @param stack - a string with the stack
        **/
        public SendCrashReport(string crashMessage, string stack)
        {
            InitializeComponent();
            message.Text = "version:" + Globals.s_version + "\r\n" + crashMessage + "\r\n\r\n\r\n" + stack;
            AlignComponents();
            stackTrace = stack;
            this.TopMost = true;
        }

        /**
        * CenterComponent - center a component
        * @param c is a control
        **/
        public void CenterComponent(Control c)
        {
            c.Location = new Point(this.Size.Width / 2 - c.Size.Width / 2, c.Location.Y);
        }

        /**
        * AlignComponents - align components
        **/
        public void AlignComponents()
        {
            CenterComponent(LogoPicture);
            CenterComponent(info);
            CenterComponent(message);
            
        }

        private void Cancel_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        /**
        * GetOSName - get OS name
        * returns a string with OS name
        **/
        public static string GetOSName()
        {
            OperatingSystem osInfo = Environment.OSVersion;
            string osName = "UNKNOWN";

            switch (osInfo.Platform)
            {
                case PlatformID.Win32Windows:
                    {
                        switch (osInfo.Version.Minor)
                        {
                            case 0:
                                osName = "Windows 95";
                                break;

                            case 10:
                                if (osInfo.Version.Revision.ToString() == "2222A")
                                    osName = "Windows 98 Second Edition";
                                else
                                    osName = "Windows 98";
                                break;

                            case 90:
                                osName = "Windows Me";
                                break;
                        }
                        break;
                    }

                case PlatformID.Win32NT:
                    {
                        switch (osInfo.Version.Major)
                        {
                            case 3:
                                osName = "Windows NT 3.51";
                                break;

                            case 4:
                                osName = "Windows NT 4.0";
                                break;

                            case 5:
                                if (osInfo.Version.Minor == 0)
                                    osName = "Windows 2000";
                                else if (osInfo.Version.Minor == 1)
                                    osName = "Windows XP";
                                else if (osInfo.Version.Minor == 2)
                                    osName = "Windows Server 2003";
                                break;

                            case 6:
                                if (osInfo.Version.Minor == 0)
                                    osName = "Windows Vista";
                                else if (osInfo.Version.Minor == 1)
                                    osName = "Windows 7";
                                else if (osInfo.Version.Minor == 2)
                                    osName = "Windows Server 2008";
                                break;
                        }
                        break;
                    }
            }

            if (osInfo.ServicePack != "")
            {
                //Append it to the OS name.  i.e. "Windows XP Service Pack 3"
                osName += " " + osInfo.ServicePack;
            }

            if (Is64BitOperatingSystem())
                osName += " " + "64-bit";
            else
                osName += " " + "32-bit";

            return osName;
        }

        /**
        * GetNETVersion - get .NET version from registry
        * returns a string with .NET version
        **/
        private string GetNETVersion()
        {
            RegistryKey installed_versions = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\NET Framework Setup\NDP");
            if (installed_versions != null)
            {
                string[] version_names = installed_versions.GetSubKeyNames();
                //version names start with 'v', eg, 'v3.5' which needs to be trimmed off before conversion
                double Framework = Convert.ToDouble(version_names[version_names.Length - 1].Remove(0, 1), CultureInfo.InvariantCulture);
                //int SP = Convert.ToInt32(installed_versions.OpenSubKey(version_names[version_names.Length - 1]).GetValue("SP", 0));
                return version_names[version_names.Length - 1].ToString();
            }
            return "";
        }

        /**
        * Is64BitOperatingSystem - The function determines whether the current operating system is a 
        *                          64-bit operating system.
        * returns true if the operating system is 64-bit; otherwise, it returns false.
        **/
        public static bool Is64BitOperatingSystem()
        {
            if (IntPtr.Size == 8)  // 64-bit programs run only on Win64
            {
                return true;
            }
            else  // 32-bit programs run on both 32-bit and 64-bit Windows
            {
                // Detect whether the current process is a 32-bit process 
                // running on a 64-bit system.
                bool flag;
                return ((DoesWin32MethodExist("kernel32.dll", "IsWow64Process") &&
                    IsWow64Process(GetCurrentProcess(), out flag)) && flag);
            }
        }

        /**
        * DoesWin32MethodExist - the function determins whether a method exists in the export 
        *                        table of a certain module.
        * @param moduleName - the name of the module                          
        * @param methodName - the name of the method                           
        * returns true if the method specified by methodName exists in the export table of the module specified by moduleName.
        **/
        static bool DoesWin32MethodExist(string moduleName, string methodName)
        {
            IntPtr moduleHandle = GetModuleHandle(moduleName);
            if (moduleHandle == IntPtr.Zero)
            {
                return false;
            }
            return (GetProcAddress(moduleHandle, methodName) != IntPtr.Zero);
        }

        [DllImport("kernel32.dll")]
        static extern IntPtr GetCurrentProcess();

        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        static extern IntPtr GetModuleHandle(string moduleName);

        [DllImport("kernel32", CharSet = CharSet.Auto, SetLastError = true)]
        static extern IntPtr GetProcAddress(IntPtr hModule,
            [MarshalAs(UnmanagedType.LPStr)]string procName);

        [DllImport("kernel32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        static extern bool IsWow64Process(IntPtr hProcess, out bool wow64Process);

        /**
        * SendReport_Click - process Send Report button click
        *                  - send am email with error in attachment
        * @param sender an object - not used
        * @param e an EventArgs - not used
        **/
        private void SendReport_Click(object sender, EventArgs e)
        {           
            string[] words = stackTrace.Split('\n');
            //string email = string.Format("mailto:World-ITConsulting@gameloft.com?subject=GLLegacyInstaller Crash&Body=" + message + "%0d%0A%");

            CultureInfo ci = CultureInfo.InstalledUICulture;
            //email += "%0d%0A" + "OS Language: " + ci.DisplayName;
            StreamWriter stream = new StreamWriter("C:\\Error.log", false);

            stream.Write("OS Name: " + GetOSName());
            stream.Write("\nOS Language: " + ci.DisplayName);
            stream.Write("\n.NET Version: " + GetNETVersion());

            RegistryKey RegKey = Registry.LocalMachine.OpenSubKey("HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0");
            Object cpuSpeed = RegKey.GetValue("~MHz");
            Object cpuType = RegKey.GetValue("VendorIdentifier");
            stream.Write("\nProcessor " +  cpuType.ToString() +" running at " + cpuSpeed.ToString() + " MHz.\n");

            foreach (string w in words)
            {
                stream.Write("\n" + w);
            }
            stream.Close();

            MAPI mapi = new MAPI();
            mapi.AddAttachment("C:\\Error.log");
            mapi.AddRecipientTo("cristian.vasile@gameloft.com;World-ITConsulting@gameloft.com");
            mapi.SendMailDirect("GLLegacyInstaller Crash", message.ToString());

            File.Delete("C:\\Error.log");
            //Process.Start(email);
            this.Close();
        }
    }
}
