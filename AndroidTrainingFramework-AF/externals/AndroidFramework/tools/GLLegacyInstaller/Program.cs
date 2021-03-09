using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using System.Runtime.InteropServices;
//using System.Security.Principal;
using System.Reflection;
using Utils;


namespace GLLegacyInstaller
{
    static public class Program
    {
        [DllImport("kernel32.dll")]
        private static extern bool AllocConsole();

        [DllImport("kernel32.dll")]
        private static extern bool AttachConsole(int dwProcessId);
        private const int ATTACH_PARENT_PROCESS = -1;

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool FreeConsole();
        

        /**
        * Main - the main entry point for the application.
        */
        [STAThread]
        static int Main(string[] args)
        {
            int exitCode = 0;

            if (args.Length > 0)
            {
                Globals.s_settingsFile = args[0];

                for (int i = 1; i < args.Length; i++)
                {
                    if (args[i] == "--runAsAdmin")
                    {
                        // runs with the same arguments plus flag mentioning the main action performing   
                        args = args.Where(w => w != args[i]).ToArray(); 
                        
				        var info = new ProcessStartInfo(
					        Assembly.GetEntryAssembly().Location,
					        String.Join(" ", args))
				        {
					        //RedirectStandardError = true,
					        //RedirectStandardOutput = true,
					        //UseShellExecute = false, // don't create a console window
					        UseShellExecute = true,
					        Verb = "runas", // indicates to eleavate privileges
				        };

				        var process = new Process
				        {
					        EnableRaisingEvents = true, // enable WaitForExit()
					        StartInfo = info
				        };

				        // common method to act with output stream - write to calling process output
				        //DataReceivedEventHandler actionWrite = (sender, e) => { Console.WriteLine(e.Data); };
				        //process.ErrorDataReceived += actionWrite;
				        //process.OutputDataReceived += actionWrite;

				        process.Start();
				        //process.BeginOutputReadLine();
				        //process.BeginErrorReadLine();
				        process.WaitForExit(); // sleep calling process thread until evoked process exit
                        return exitCode;
                    }

                    if (args[i] == "--console")
                        Globals.s_workInConsole = true;
                    else if (args[i] == "--only-check-tools")
                        Globals.s_onlyCheckTools = true;
                    else if ((args[i] == "--help") || (args[i] == "-h") || (args[i] == "?") || (args[i] == "/?"))
                    {
                        Globals.s_workInConsole = true;
                        Globals.s_showHelp = true;
                    }

                    //else if (args[i].StartsWith("--platforms="))
                    //{
                    //    int iStart = args[i].IndexOf('=') + 1;
                    //    int iEnd = args[i].Length;
                    //    Globals.s_platformsCommandLine = args[i].Substring(iStart, iEnd - iStart);
                    //    if (Globals.s_platformsCommandLine.ToUpper() != "ALL" && Globals.s_platformsCommandLine.ToUpper() != "NONE")
                    //    {
                    //        string[] str = Globals.s_platformsCommandLine.Split('+');
                    //        for (int j = 0; j < str.Length; j++)
                    //            Globals.s_platformsInView.Add(str[j].ToUpper());
                    //    }
                    //}

                    else
                    {
                        Globals.s_bErrorInParams = true;
                        Globals.s_strErrorMsg = "\nInvalid parameter: " + args[i] + "\r\n";
                        break;
                    }
                }
            }


            
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.ThrowException);
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);
            
            AppDomain currentDomain = AppDomain.CurrentDomain;
            currentDomain.UnhandledException += new UnhandledExceptionEventHandler(errorHandlerMain);
            Application.ThreadException += new System.Threading.ThreadExceptionEventHandler(errorHandlerThread);

            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            
  

            MainWindow mw = new MainWindow();
            if (Globals.s_workInConsole)
            {
                // redirect console output to parent process;
                // must be before any calls to Console.WriteLine()
                if(!AttachConsole(ATTACH_PARENT_PROCESS))
                {
                    AllocConsole();
                }


                Console.WriteLine("\n----------------------------------------");

                Console.WriteLine("" + System.AppDomain.CurrentDomain.FriendlyName + " v" + Globals.s_version);
                Console.WriteLine("Copyright (c) Gameloft Romania 2013");
                Console.WriteLine("help: https://docs.gameloft.org/gllegacy-installer/");
                Console.WriteLine("email to: cristian.vasile@gameloft.com, World-ITConsulting@gameloft.com\n");

                /*WindowsIdentity identity = WindowsIdentity.GetCurrent();
                WindowsPrincipal principal = new WindowsPrincipal(identity);

                if (principal.IsInRole(WindowsBuiltInRole.Administrator))
                {
                    Console.WriteLine("\nYou are Administrator");
                }
                else
                    Console.WriteLine("\nYou are NOT Administrator");*/

                //System.Diagnostics.Stopwatch sw = new System.Diagnostics.Stopwatch();
                //sw.Start();
                if (Globals.s_bErrorInParams)
                {
                    Console.WriteLine(Globals.s_strErrorMsg);
                    Globals.s_showHelp = true;
                }

                if (Globals.s_showHelp)
                {
                    Console.WriteLine(Globals.s_helpMessage);                    
                }
                else
                {
                    if (mw.LoadSettings())
                    {
                        //Console.WriteLine("time = " + sw.ElapsedMilliseconds + " ms");

                        mw.m_toolList.DetectToolsInstallPath(); //to add param string with platforms = ""
                        //Console.WriteLine("time detect = " + sw.ElapsedMilliseconds + " ms");
                        if (mw.m_toolList.HasInvalidRequiredInstallPath())
                        {
                            //Console.WriteLine("ERROR: There are some invalid paths!");
                            exitCode = 1;
                        }
                        else
                        {
                            //Console.WriteLine("All required paths are ok!");
                        }
                        //Console.WriteLine("time invalid = " + sw.ElapsedMilliseconds + " ms");
                    }
                }

                Console.WriteLine("----------------------------------------");

                //sw.Stop();
                //Console.WriteLine("time final = " + sw.ElapsedMilliseconds + " ms");
                //Console.ReadKey();

                FreeConsole();

                mw.Close();
            }
            else
            {
                /*WindowsIdentity identity = WindowsIdentity.GetCurrent();
                WindowsPrincipal principal = new WindowsPrincipal(identity);
                
                if (principal.IsInRole(WindowsBuiltInRole.Administrator))
                {
                    MessageBox.Show("You are Administrator", "No Admin", MessageBoxButtons.OK, MessageBoxIcon.Exclamation);
                }
                else
                    MessageBox.Show("You are not Administrator", "No Admin", MessageBoxButtons.OK, MessageBoxIcon.Exclamation);*/

                Application.Run(mw);
            }

            return exitCode;
        }


       
       delegate DialogResult sendCrashDelegate(string type, string message, string stackTrace, bool big);

        public static DialogResult sendCrash(string type, string message, string stackTrace, bool big)
        {
            SendCrashReport popUp = new SendCrashReport(message, stackTrace);
            popUp.LogoPicture.Image = global::GLLegacyInstaller.Properties.Resources.LogoGLLegacy;

            popUp.FormBorderStyle = FormBorderStyle.FixedDialog;

            return popUp.ShowDialog(MainWindow.s_instance);
        }

        public static void errorHandlerMain(object sender, UnhandledExceptionEventArgs args)
        {
            Exception e = (Exception)args.ExceptionObject;

            DialogResult result = sendCrash("CrashReport", e.Message, e.StackTrace, false);

           
            Application.Exit();   
           
        }

        public static void errorHandlerThread(object sender, System.Threading.ThreadExceptionEventArgs e)
        {

            DialogResult result = sendCrash("CrashReport", e.Exception.Message, e.Exception.StackTrace, false);


            Application.Exit();   

        }
    }
}
