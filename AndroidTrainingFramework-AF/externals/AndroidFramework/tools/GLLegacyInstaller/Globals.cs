using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Security.Permissions;
using System.Diagnostics;
using Microsoft.Win32;
using System.Drawing;
using GLLegacyInstaller;

namespace Utils
{
    partial class Globals
    {
        static public String s_version = "1.3.1";
        static public bool s_bNewVersionQuestion = false;

        static public bool s_workInConsole = false;
        static public bool s_onlyCheckTools = false;
        static public bool s_showHelp = false;

        static public String s_settingsFile = "InstallerSetup.xml";
        static public String s_platformsCommandLine = "";
        static public bool   s_bErrorInParams = false;
        static public bool   s_bErrorInInstallerSetup = false;
        static public string s_strErrorMsg = "";
        /**
         * this is the tool used to edit the files.
        **/
        static public String s_notepad = null;


        

        public static Image s_warningIcon = global::GLLegacyInstaller.Properties.Resources.WarningImage;
        public static Image s_errorIcon = global::GLLegacyInstaller.Properties.Resources.ErrorImage;
       // public static Image s_testIcon = global::GLLegacyInstaller.Properties.Resources.PythonLogo;


        static public String s_helpMessage =    "Usage:\r\n" +
                                                System.AppDomain.CurrentDomain.FriendlyName + " <xml_file_with_settings> [options1] [options2] ... [optionsX]\r\n" +
                                                "<xml_file_with_settings> -> is the full path and filename to an xml file type with settings for installer.\r\n" +
                                                "Options:\r\n" +
                                                "--help, -h, ?, /? -> will display this help\r\n" +
                                                "--console -> this will open and display information in command prompt\r\n" +
                                                "--only-check-tools -> this will display only the tab page that check the tools. No other tabs will be displayed.\r\n" +
                                                "--platforms=All or --platforms=None or ex. --platforms=Win32+Android+iOS -> One or more platforms declared in <xml_filewith_setings> under the <platforms> node. This will check the tools for selected platforms. \r\n" +
                                                "--runAsAdmin -> this will run GLLegacyInstaller as administrator" +
                                                "";

    }
}
