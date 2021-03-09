using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using Utils;

namespace BuildConfigurator
{
    static class Program
    {
        [DllImport("kernel32.dll")]
        private static extern bool AllocConsole();

        [DllImport("kernel32.dll")]
        private static extern bool AttachConsole(int dwProcessId);
        private const int ATTACH_PARENT_PROCESS = -1;

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool FreeConsole();

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main(string[] args)
        {
            string typeOfBuild = "release";
            string architecture = "arm"; 

            Globals.s_platforms.Add("ANDROID");


            if (args.Length > 0)
            {
                for (int i = 0; i < args.Length; i++)
                {
                    if (args[i] == "--console")
                    {
                        Globals.s_workInConsole = true;
                    }

                    else if ((args[i] == "--help") || (args[i] == "-h") || (args[i] == "?") || (args[i] == "/?"))
                    {
                        Globals.s_workInConsole = true;
                        Globals.s_showHelp = true;
                    }

                    else if (args[i].Contains("-type="))
                    {
                        if (args[i].Contains("release"))
                        {
                            typeOfBuild = "release";
                        }
                        else if (args[i].Contains("debug"))
                        {
                            typeOfBuild = "debug";
                        }
                    }

                    else if (args[i].Contains("-arch="))
                    {
                        if (args[i].Contains("arm"))
                        {
                            architecture = "arm";
                        }
                        else if (args[i].Contains("x86"))
                        {
                            architecture = "x86";
                        }
                        else if (args[i].Contains("all"))
                        {
                            architecture = "all";
                        }
                    }

                    else
                    {                        
                        //Globals.s_gameSpecificPath = args[i];   
                    }
                }

            }


            #region HandleGeneralFailure
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.ThrowException);
            Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);

            AppDomain currentDomain = AppDomain.CurrentDomain;
            currentDomain.UnhandledException += new UnhandledExceptionEventHandler(errorHandlerMain);
            Application.ThreadException += new System.Threading.ThreadExceptionEventHandler(errorHandlerThread);
            #endregion


            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            BuildConfigurator bc = new BuildConfigurator();

            if (Globals.s_workInConsole)
            {
                // redirect console output to parent process;
                // must be before any calls to Console.WriteLine()
                if (!AttachConsole(ATTACH_PARENT_PROCESS))
                {
                   // AllocConsole();
                }


                Console.WriteLine("\n----------------------------------------");
                Console.WriteLine("" + System.AppDomain.CurrentDomain.FriendlyName + " " + Globals.s_version);
                Console.WriteLine("Copyright (c) Gameloft Romania 2013");
                Console.WriteLine("https://docs.gameloft.org/gllegacy-how-to-compile-sandbox/");
                Console.WriteLine("email to: mariacristina.mihet@gameloft.com, cristian.vasile@gameloft.com, World-ITConsulting@gameloft.com");

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
                    if (bc.LoadSettings())
                    {
                        PlatformTabPage.PlatformTabPage ptb = bc.platformTabPage;

                        //String fname = ptb.BUILD_FileName;
                        //ptb.GenerateBUILDFile(fname);
                        //fname = ptb.HEADER_FileName;
                        //ptb.GenerateHEADERFile(fname, Globals.s_workInConsole);

                        ptb.Build(typeOfBuild, architecture, false);
                    }

                }

                Console.WriteLine("----------------------------------------");

                //sw.Stop();
                //Console.WriteLine("time final = " + sw.ElapsedMilliseconds + " ms");
                //Console.ReadKey();

                FreeConsole();

                bc.Close();
            }
            else
            {
                Application.Run(bc);
            }
        }

        


        #region HandleGeneralFailure

        delegate DialogResult sendCrashDelegate(string type, string message, string stackTrace, bool big);

        public static DialogResult sendCrash(string type, string message, string stackTrace, bool big)
        {
            SendCrashReport popUp = new SendCrashReport(message, stackTrace);
            popUp.LogoPicture.Image = global::BuildConfiguratorV2.Properties.Resources.LogoGLLegacy;

            popUp.FormBorderStyle = FormBorderStyle.FixedDialog;

            return popUp.ShowDialog(BuildConfigurator.s_instance);
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
        #endregion

    }
}
