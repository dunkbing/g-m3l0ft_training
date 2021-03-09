using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.IO;

namespace Utils
{
    public partial class Globals
    {
         /**
         * the program version.
         **/
        public static String s_version = "v3.0.0";

        static public bool s_workInConsole = false;
        static public bool s_showHelp = false;
        static public String s_platformsCommandLine = "";
        public static ArrayList s_platforms = new ArrayList();
        static public bool s_bErrorInParams = false;
        static public string s_strErrorMsg = "";

        static public String s_helpMessage = "\r\nUsage:\r\n\r\n" +
                                        System.AppDomain.CurrentDomain.FriendlyName + " <game_specific_path> [options1] [options2] ... \r\n" +
                                        "<game_specific_path> -> full path to the android game specific folder.\r\n\r\n" +
                                        "Options:\r\n\r\n" +
                                        "--help, -h, ?, /? -> will display this help\r\n\r\n" +
                                        "--console -> will build the project without requiring user interaction\r\n\r\n" +
                                        "--type=<type_of_build> -> combo with --console option, ignored otherwise.\r\n" + 
                                                "\tchoose between: debug | release\r\n\r\n" +
                                        "--arch=<architecture> -> combo with --console option, ignored otherwise.\r\n" +
                                                "\tchoose between: arm | x86 | all\r\n\r\n" +
                                        "";

        //
        // ----------------------------------- initial paths

        /**
         * android framework base path
         **/
        public static String s_androidFrameworkPath = null;

        /**
         * android framework base path
         **/
        public static String s_afConfigPath = null;

        /**
         * android game specific path
         **/
        public static String s_gameSpecificPath = null;

        /**
         * tools path, inside android framework
         **/
        public static String s_afToolsPath = null;

        /**
         * build configurator path, inside tools
         **/
        public static String s_buildConfiguratorPath = null;

        /**
         * build configurator path, inside tools
         **/
        public static String s_installerPath = null;

        /**
         * build configurator path, inside tools
         **/
        public static String s_ddmsPath = null;

        /**
         * tracking tool path, inside tools
         **/
        public static String s_trackingToolPath = null;

        /**
         * configure tool path, inside tools
         **/
        public static String s_configureToolPath = null;

        // ----------------------------------- end initial paths
        //


        /**
         * This is the default name for the config file.
         **/
        //public static String s_cfgFile = "BuildConfigurator.xml";


        /**
         * This is the path location for the s_settingsFile (aka BuildConfigurator.xml).
         **/
        //public static String s_cfgPath = "";

        
        /**
         * This is the warning image used for the warning mesages.
        **/
        public static Image s_warningIcon = null;


        /**
         * This is the error image used for the error mesages.
        **/
        public static Image s_errorIcon = null;


        /**
         * This is the command that must be executed to open the GLLegacyInstaller.
         * Can be a relative/full path to an executable or batch script.
         */
        static public String s_installerCommand = null;

        /**
         * This is the command that must be executed to open the TrackingTestTool.
         * Can be a relative/full path to an executable or batch script.
         */
        static public String s_TrackingTestToolCommand = null;

        /**
         * This is the project name. Will be displayed in the app name.
         */
        static public String s_projectName = null;


        /**
         * this is the tool used to edit the files.
        **/
        static public String s_notepad = null;


        /**
         * this is the tool used by the Diff operation.
        **/
        static public String s_diff = null;
	}
}
