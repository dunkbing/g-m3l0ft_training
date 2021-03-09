using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Security.Permissions;
using System.Diagnostics;
using Microsoft.Win32;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using System.Drawing;
using System.Runtime.InteropServices;   


namespace Utils
{
    public partial class Globals
    {
        /// <summary>
        /// Creates a relative path from one file or folder to another.
        /// </summary>
        /// <param name="fromPath">Contains the directory that defines the start of the relative path.</param>
        /// <param name="toPath">Contains the path that defines the endpoint of the relative path.</param>
        /// <param name="dontEscape">Boolean indicating whether to add uri safe escapes to the relative path</param>
        /// <returns>The relative path from the start directory to the end path.</returns>
        /// <exception cref="ArgumentNullException"></exception>
        public static String MakeRelativePath(String fromPath, String toPath)
        {
            if (String.IsNullOrEmpty(fromPath)) throw new ArgumentNullException("fromPath");
            if (String.IsNullOrEmpty(toPath)) throw new ArgumentNullException("toPath");

            string doubleSeparator = "" + Path.DirectorySeparatorChar + Path.DirectorySeparatorChar;

            fromPath = fromPath.Replace('/', Path.DirectorySeparatorChar);
            while(fromPath.IndexOf(doubleSeparator) >= 0)
            {
                fromPath = fromPath.Replace(doubleSeparator, "" + Path.DirectorySeparatorChar);
            }
            if (fromPath[fromPath.Length - 1] != Path.DirectorySeparatorChar)
                fromPath += Path.DirectorySeparatorChar;

            toPath = toPath.Replace('/', Path.DirectorySeparatorChar);
            while(toPath.IndexOf(doubleSeparator) >= 0)
            {
                toPath = toPath.Replace(doubleSeparator, "" + Path.DirectorySeparatorChar);
            }
            if (toPath[toPath.Length - 1] != Path.DirectorySeparatorChar)
                toPath += Path.DirectorySeparatorChar;

            

            Uri fromUri = new Uri(fromPath);
            Uri toUri = new Uri(toPath);

            Uri relativeUri = fromUri.MakeRelativeUri(toUri);
            fromUri = null;
            toUri = null;
            String relativePath = Uri.UnescapeDataString(relativeUri.ToString());

            return relativePath.Replace('/', Path.DirectorySeparatorChar);
        }

        public static bool IsValidFilename(string testName)
        {
            if(testName.Length <= 0) return false;

            string regexString = "[";
            foreach(char c in Path.GetInvalidPathChars())
            {
                regexString += c;
            }
            regexString += "]";

            Regex containsABadCharacter = new Regex(regexString);

            if (containsABadCharacter.IsMatch(testName))
            {
                containsABadCharacter = null;
                return false;
            }

            // Check for drive
            string pathRoot = Path.GetPathRoot(testName);
            if (Directory.GetLogicalDrives().Contains(pathRoot))
            {
                // etc
            }

            // other checks for UNC, drive-path format, etc

            containsABadCharacter = null;
            return true;
        }

        public static String ReplaceSeparators(String fromPath)
        {
            string doubleSeparator = "" + Path.DirectorySeparatorChar + Path.DirectorySeparatorChar;

            fromPath = fromPath.Replace('/', Path.DirectorySeparatorChar);
            while (fromPath.IndexOf(doubleSeparator) >= 0)
            {
                fromPath = fromPath.Replace(doubleSeparator, "" + Path.DirectorySeparatorChar);
            }

            return fromPath;
        }

        public static byte[] GetBytes(int value)
        {
            byte[] intBytes = BitConverter.GetBytes(value);
            if (BitConverter.IsLittleEndian)
                Array.Reverse(intBytes);

            return intBytes;
        }

        public static int GetIntFromString(String str)
        {
            try
            {
                return Convert.ToInt32(str);
            }
            catch (FormatException e)
            {
                Console.WriteLine("ERROR: EXCEPTION - Input string is not a sequence of digits.");
                Console.WriteLine(e.ToString());
                return 0;
            }
            catch (OverflowException e)
            {
                Console.WriteLine("ERROR: EXCEPTION - The number cannot fit in an Int32.");
                Console.WriteLine(e.ToString());
                return 0;
            }
        }



        /// <summary>
        /// Return the value form an registry key.
        /// </summary>
        /// <param name="FullRegEntry">The registry entry. Ex: "SYSTEM\\CurrentControlSet\\services\\GLLegacyService" </param>
        /// <param name="string">the key name, or string "(Default)" </param>
        /// <returns>the registry key value, or null.</returns>
        public static string GetRegValue(string FullRegEntry, string key)
        {
            RegistryKey rk = Registry.LocalMachine.OpenSubKey(FullRegEntry);
            if (rk == null)
            {
                return null;
            }

            string rez = null;
            if (key == "(Default)")
                rez = (string)rk.GetValue(null);
            else
                rez = (string)rk.GetValue(key);
            rk.Close();

            return rez;
        }


        /// <summary>
        /// Return the value form an software registry key.
        /// </summary>
        /// <param name="FullRegEntry">The registry entry. Ex: "\\CurrentControlSet\\services\\GLLegacyService" </param>
        /// <param name="string">the key name, or string "(Default)" </param>
        /// <returns>the registry key value, or null.</returns>
        public static string GetSoftwareRegValue(string RegEntry, string key)
        {
            string fullRegEntry32 = "SOFTWARE/" + RegEntry;
            fullRegEntry32 = fullRegEntry32.Replace('/', '\\');
            string fullRegEntry64 = "SOFTWARE/Wow6432Node/" + RegEntry;
            fullRegEntry64 = fullRegEntry64.Replace('/', '\\');
            RegistryKey rk = Registry.LocalMachine.OpenSubKey(fullRegEntry32);
            if (rk == null)
                rk = Registry.LocalMachine.OpenSubKey(fullRegEntry64);
            if (rk == null)
            {
                return null;
            }
            string rez = null;
            if (key == "(Default)")
                rez = (string)rk.GetValue(null);
            else
                rez = (string)rk.GetValue(key);
            rk.Close();
            return rez;

        }


        public static bool DriveLetterExists(char c)
        {
            String d = c + ":\\";
           
            bool ret = false;
            System.IO.DriveInfo[] drives = System.IO.DriveInfo.GetDrives();
            foreach (var drive in drives)
            {
                string driveName = drive.Name; // C:\, E:\, etc:\

                //System.IO.DriveType driveType = drive.DriveType;
                if (d.ToUpper() == driveName /*&& ( driveType == System.IO.DriveType.CDRom ||
                                        driveType == System.IO.DriveType.Fixed || 
                                        driveType == System.IO.DriveType.Network ||
                                        driveType == System.IO.DriveType.Ram ||
                                        driveType == System.IO.DriveType.Removable ||
                                        driveType == System.IO.DriveType.NoRootDirectory )*/
                                                                                            )
                {
                    ret = true;
                    break;
                }
            }
            return ret;
        }

        private static System.Collections.IDictionary s_envVars = null;

        public static string GetEnvironmentVar(string name)
        {
            if (s_envVars == null)
                s_envVars = Environment.GetEnvironmentVariables();

            return s_envVars[name] as string;

            /*string value = null;
            try
            {
                //EnvironmentPermission perm = new EnvironmentPermission(EnvironmentPermissionAccess.Read, name);

                //perm.Demand();
                value = System.Environment.GetEnvironmentVariable(name, EnvironmentVariableTarget.User);
            }
            catch (Exception e)
            {
                Debug.Assert(false, e.ToString());
            };

            return value;*/
        }



        static EnvironmentPermission m_perm = null;
        //[PrincipalPermission(SecurityAction.Demand, Role = @"BUILTIN\Administrators")]
        public static String AddEnvironmentVar(string name, string value)
        {
            String errorMessage = null;
            
            try
            {
                if (m_perm == null)
                {
                    m_perm = new EnvironmentPermission(EnvironmentPermissionAccess.Write, name);

                    if (m_perm != null)
                        m_perm.Demand();
                }
            }
            catch (System.Security.SecurityException se)
            {
                errorMessage = "SecurityException at SetEnvironmentVariable: " + name + " value: " + value + "\r\n" + se.ToString();
            }
            ///////////////Check if environment variable exists and is identical then return 
            bool envVarChange = false;
            try
            {
                string envVal = System.Environment.GetEnvironmentVariable(name, EnvironmentVariableTarget.User);
                if (envVal != value)
                    envVarChange = true;
            }
            catch (Exception e) 
            {
                envVarChange = true;
            };

            if (envVarChange == false) return errorMessage;
            //////////////If environment variable is not, or value a different, then setEnvironmentVariable with the newer value
            try
            {
                // reactivated the system variable -- //c78 temporary deactivated

                System.Environment.SetEnvironmentVariable(name, value, EnvironmentVariableTarget.User);
                //c78 used only the save to file.    
            }
            catch (Exception e)
            {
                errorMessage = "Exception at SetEnvironmentVariable: " + name + " value: " + value + "\r\n" + e.ToString();
            };
        

            return errorMessage;
        }

        public static String AddEnvironmentVar(string name, string value, string outputScript, bool append)
        {
            string returnCode = AddEnvironmentVar(name, value);

            WriteEnvironmentVarBatchScript(name, value, outputScript, append);

            return returnCode;
        }

        
        public static void WriteEnvironmentVarBatchScript(string name, string value, string outputScript, bool append)
        {
            if (!string.IsNullOrEmpty(outputScript))
            {
                StreamWriter str = new StreamWriter(outputScript, append);
                if (!append)
                {
                    str.WriteLine("rem file generated by GLLegacyInstaller");
                }
                str.WriteLine("set " + name + "=" + value);
                str.Close();
                str = null;
            }
        }


        public static void ResetEnvironmentVarBatchScript(string outputScript)
        {
            StreamWriter str = new StreamWriter(outputScript, false);
            str.WriteLine("rem do not modify this file by hand! all changes will be overwrited!");
            str.Close();
            str = null;
        }

        
        public static String AddToEnvironmentPath(string value)
        {
            String errorMessage = null;

            
            try
            {
                /*
                string goodVal = value.Replace('/', '\\');

                string path = System.Environment.GetEnvironmentVariable("PATH", EnvironmentVariableTarget.User);
                
                if (path != null)
                {
                    string[] toks = path.Split(';');
                    //bool bFound = false;
                    for (int i = 0; i < toks.Length; i++)
                    {
                        if (toks[i].ToUpper().Replace('/', '\\') == goodVal.ToUpper())
                            return errorMessage;
                    }

                    path = goodVal + ';' + path;
                }
                else
                {
                    path = goodVal + ';';
                }

                if (m_perm == null)
                {
                    m_perm = new EnvironmentPermission(EnvironmentPermissionAccess.Write, "PATH");
                    if (m_perm != null)
                        m_perm.Demand();
                }
                */

                //System.Environment.SetEnvironmentVariable("PATH", path, EnvironmentVariableTarget.User);
                WriteEnvironmentVarBatchScript("PATH", value + ";%PATH%", GetFullPathToInstallerEnvBatchScript(), true);
            }
            catch (Exception e)
            {
                errorMessage = "Exception at SetEnvironmentVariable: PATH value: " + value + "\r\n" + e.ToString();
            }

            return errorMessage;
        }

        /// <summary>
        /// Get the path and file to GLLegacyTemp.txt file
        /// </summary>
        /// <returns>string</returns>
        public static string GetFullPathToDistccTempFile()
        {
            //string str = Environment.GetEnvironmentVariable("temp", EnvironmentVariableTarget.Machine);
            String str = Globals.GetRegValue("SYSTEM\\CurrentControlSet\\services\\GLLegacyService", "ImagePath");
            if (str != null)
            {
                str = str.Replace('"', ' ');
                str = str.Trim();
                return Path.Combine(Path.GetDirectoryName(str), "GLLegacyDisccTemp.txt");
            }
            else
                return str;
           
        }

        /// <summary>
        /// Get the path and file to GLLegacyDistccHosts.bat file
        /// </summary>
        /// <returns>string</returns>
        public static string GetFullPathToDistccHostsBatchScript()
        {            
            String str = Environment.GetEnvironmentVariable("WINDIR");
            return Path.Combine(str, "GLLegacyDistccHosts.bat");
        }

        /// <summary>
        /// Get the path and file to GLLegacyDistccEnv.bat file
        /// </summary>
        /// <returns>string</returns>
        public static string GetFullPathToDistccEnvBatchScript()
        {
            return Path.Combine(System.IO.Path.GetTempPath(), "GLLegacyDistccEnv.bat");
        }

        /// <summary>
        /// Get the path and file to GLLegacyInstallerEnv.bat file
        /// </summary>
        /// <returns>string</returns>
        public static string GetFullPathToInstallerEnvBatchScript()
        {
            return Path.Combine(System.IO.Path.GetTempPath(), "GLLegacyInstallerEnv.bat");
        }


        /**
         * This will search entire String str for matches like  $ENVIRONMENT_VARIABLE$ , and replace them with the corresponding value of the ENVIRONMENT_VARIABLE.
         */
        public static String ReplaceEnvironmentVariables(String str)
        {
            if(str == null) return "";
            if(str.Length == 0) return str;

            if (str.IndexOf('$') == -1) return str;

            Match match = Regex.Match(str, @"\$\([ A-Za-z0-9\-_\.]*\)");

            if (match.Success)
            {
                // Finally, we get the Group value and display it.
                for(int i=0; i < match.Groups.Count; i++)
                {
                    string key = match.Groups[i].Value;
                    String keyStripped = key.Substring(2, key.Length - 3);
                    String value = GetEnvironmentVar(keyStripped);
                    if(value != null)
                        str = str.Replace(key, value);

                }
            }

            if (str == null) 
                return "";

            return str;
        }

        /// <summary>
        /// Gets IP addresses of the local computer
        /// </summary>
        public static string GetIP(String hostName)
        {
            String errorMessage = null;
            string _IP = null;
            try
            {
                // Resolves a host name or IP address to an IPHostEntry instance.
                // IPHostEntry - Provides a container class for Internet host address information. 
                System.Net.IPHostEntry _IPHostEntry = System.Net.Dns.GetHostEntry(hostName);

                if (_IPHostEntry == null)
                    return _IP;
                // IPAddress class contains the address of a computer on an IP network. 
                foreach (System.Net.IPAddress _IPAddress in _IPHostEntry.AddressList)
                {
                    // InterNetwork indicates that an IP version 4 address is expected 
                    // when a Socket connects to an endpoint
                    if (_IPAddress.AddressFamily.ToString() == "InterNetwork")
                    {
                        _IP = _IPAddress.ToString();
                    }
                }
                
            }
            catch (Exception e)
            {
                errorMessage = "Exception at GetIP: hostName: " + hostName + "\r\n" + e.ToString();
            }
            return _IP;
        }


        /// <summary>
        /// Gets IP addresses of the local computer
        /// </summary>
        public static string GetLocalIP()
        {
            string _IP = null;

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
                    _IP = _IPAddress.ToString();
                }
            }
            return _IP;
        }


    }







    public class MessageBoxEx
    {
        private static IWin32Window _owner;
        private static HookProc _hookProc;
        private static IntPtr _hHook;

        public static DialogResult Show(string text)
        {
            Initialize();
            return MessageBox.Show(text);
        }

        public static DialogResult Show(string text, string caption)
        {
            Initialize();
            return MessageBox.Show(text, caption);
        }

        public static DialogResult Show(string text, string caption, MessageBoxButtons buttons)
        {
            Initialize();
            return MessageBox.Show(text, caption, buttons);
        }

        public static DialogResult Show(string text, string caption, MessageBoxButtons buttons, MessageBoxIcon icon)
        {
            Initialize();
            return MessageBox.Show(text, caption, buttons, icon);
        }

        public static DialogResult Show(string text, string caption, MessageBoxButtons buttons, MessageBoxIcon icon, MessageBoxDefaultButton defButton)
        {
            Initialize();
            return MessageBox.Show(text, caption, buttons, icon, defButton);
        }

        public static DialogResult Show(string text, string caption, MessageBoxButtons buttons, MessageBoxIcon icon, MessageBoxDefaultButton defButton, MessageBoxOptions options)
        {
            Initialize();
            return MessageBox.Show(text, caption, buttons, icon, defButton, options);
        }

        public static DialogResult Show(IWin32Window owner, string text)
        {
            _owner = owner;
            Initialize();
            return MessageBox.Show(owner, text);
        }

        public static DialogResult Show(IWin32Window owner, string text, string caption)
        {
            _owner = owner;
            Initialize();
            return MessageBox.Show(owner, text, caption);
        }

        public static DialogResult Show(IWin32Window owner, string text, string caption, MessageBoxButtons buttons)
        {
            _owner = owner;
            Initialize();
            return MessageBox.Show(owner, text, caption, buttons);
        }

        public static DialogResult Show(IWin32Window owner, string text, string caption, MessageBoxButtons buttons, MessageBoxIcon icon)
        {
            _owner = owner;
            Initialize();
            return MessageBox.Show(owner, text, caption, buttons, icon);
        }

        public static DialogResult Show(IWin32Window owner, string text, string caption, MessageBoxButtons buttons, MessageBoxIcon icon, MessageBoxDefaultButton defButton)
        {
            _owner = owner;
            Initialize();
            return MessageBox.Show(owner, text, caption, buttons, icon, defButton);
        }

        public static DialogResult Show(IWin32Window owner, string text, string caption, MessageBoxButtons buttons, MessageBoxIcon icon, MessageBoxDefaultButton defButton, MessageBoxOptions options)
        {
            _owner = owner;
            Initialize();
            return MessageBox.Show(owner, text, caption, buttons, icon,
                                   defButton, options);
        }

        public delegate IntPtr HookProc(int nCode, IntPtr wParam, IntPtr lParam);

        public delegate void TimerProc(IntPtr hWnd, uint uMsg, UIntPtr nIDEvent, uint dwTime);

        public const int WH_CALLWNDPROCRET = 12;

        public enum CbtHookAction : int
        {
            HCBT_MOVESIZE = 0,
            HCBT_MINMAX = 1,
            HCBT_QS = 2,
            HCBT_CREATEWND = 3,
            HCBT_DESTROYWND = 4,
            HCBT_ACTIVATE = 5,
            HCBT_CLICKSKIPPED = 6,
            HCBT_KEYSKIPPED = 7,
            HCBT_SYSCOMMAND = 8,
            HCBT_SETFOCUS = 9
        }

        [DllImport("user32.dll")]
        private static extern bool GetWindowRect(IntPtr hWnd, ref Rectangle lpRect);

        [DllImport("user32.dll")]
        private static extern int MoveWindow(IntPtr hWnd, int X, int Y, int nWidth, int nHeight, bool bRepaint);

        [DllImport("User32.dll")]
        public static extern UIntPtr SetTimer(IntPtr hWnd, UIntPtr nIDEvent, uint uElapse, TimerProc lpTimerFunc);

        [DllImport("User32.dll")]
        public static extern IntPtr SendMessage(IntPtr hWnd, int Msg, IntPtr wParam, IntPtr lParam);

        [DllImport("user32.dll")]
        public static extern IntPtr SetWindowsHookEx(int idHook, HookProc lpfn, IntPtr hInstance, int threadId);

        [DllImport("user32.dll")]
        public static extern int UnhookWindowsHookEx(IntPtr idHook);

        [DllImport("user32.dll")]
        public static extern IntPtr CallNextHookEx(IntPtr idHook, int nCode, IntPtr wParam, IntPtr lParam);

        [DllImport("user32.dll")]
        public static extern int GetWindowTextLength(IntPtr hWnd);

        [DllImport("user32.dll")]
        public static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int maxLength);

        [DllImport("user32.dll")]
        public static extern int EndDialog(IntPtr hDlg, IntPtr nResult);

        [StructLayout(LayoutKind.Sequential)]
        public struct CWPRETSTRUCT
        {
            public IntPtr lResult;
            public IntPtr lParam;
            public IntPtr wParam;
            public uint message;
            public IntPtr hwnd;
        } ;

        static MessageBoxEx()
        {
            _hookProc = new HookProc(MessageBoxHookProc);
            _hHook = IntPtr.Zero;
        }

        private static void Initialize()
        {
            if (_hHook != IntPtr.Zero)
            {
                throw new NotSupportedException("multiple calls are not supported");
            }

            if (_owner != null)
            {
                _hHook = SetWindowsHookEx(WH_CALLWNDPROCRET, _hookProc, IntPtr.Zero, AppDomain.GetCurrentThreadId());
            }
        }

        private static IntPtr MessageBoxHookProc(int nCode, IntPtr wParam, IntPtr lParam)
        {
            if (nCode < 0)
            {
                return CallNextHookEx(_hHook, nCode, wParam, lParam);
            }

            CWPRETSTRUCT msg = (CWPRETSTRUCT)Marshal.PtrToStructure(lParam, typeof(CWPRETSTRUCT));
            IntPtr hook = _hHook;

            if (msg.message == (int)CbtHookAction.HCBT_ACTIVATE)
            {
                try
                {
                    CenterWindow(msg.hwnd);
                }
                finally
                {
                    UnhookWindowsHookEx(_hHook);
                    _hHook = IntPtr.Zero;
                }
            }

            return CallNextHookEx(hook, nCode, wParam, lParam);
        }

        private static void CenterWindow(IntPtr hChildWnd)
        {
            Rectangle recChild = new Rectangle(0, 0, 0, 0);
            bool success = GetWindowRect(hChildWnd, ref recChild);

            int width = recChild.Width - recChild.X;
            int height = recChild.Height - recChild.Y;

            Rectangle recParent = new Rectangle(0, 0, 0, 0);
            success = GetWindowRect(_owner.Handle, ref recParent);

            Point ptCenter = new Point(0, 0);
            ptCenter.X = recParent.X + ((recParent.Width - recParent.X) / 2);
            ptCenter.Y = recParent.Y + ((recParent.Height - recParent.Y) / 2);


            Point ptStart = new Point(0, 0);
            ptStart.X = (ptCenter.X - (width / 2));
            ptStart.Y = (ptCenter.Y - (height / 2));

            ptStart.X = (ptStart.X < 0) ? 0 : ptStart.X;
            ptStart.Y = (ptStart.Y < 0) ? 0 : ptStart.Y;

            int result = MoveWindow(hChildWnd, ptStart.X, ptStart.Y, width,
                                    height, false);

        }

    }
}
