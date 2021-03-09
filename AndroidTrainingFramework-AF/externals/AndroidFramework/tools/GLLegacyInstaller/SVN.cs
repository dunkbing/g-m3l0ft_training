using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;
using Utils;

namespace GLLegacyInstaller.SVN
{
    class SVNCommands
    {
        public static bool isTortoiseProc = true;
        public static string CommandLine;
        public static string CommandArgs;
        public static string Username = "";
        public static string Password = "";
        public static bool InteractiveLaunch = true;


        public static string SvnBin = "svn.exe";
        public static string SvnProc = "TortoiseProc.exe";

        static string ERROR_NOT_FOUND           = "E000000";
        static string ERROR_CERTIFICATE_FAILED  = "E175002";
        static string ERROR_ACCESS_FORBIDEN     = "E175013";
        static string ERROR_AUTHENTICATION      = "E170001";
        static string ERRR_NOT_WORKING_COPY     = "E155007";
        static string ERROR_UNFINISHED_OP       = "E155037";


        public static int ERR_WARNING   = -1;
        public static int ERR_NOERROR   = 0;
        public static int ERR_UNKNOWN   = 1;
        public static int ERR_NOT_FOUND = 2;
        public static int ERR_FORBIDEN  = 3;
        public static int ERR_AUTHENTICATION = 4;
        public static int ERR_WORKINGCOPY    = 5;
        public static int ERR_UNFINISHED     = 6;
        public static int ERR_CERTIFICATE_FAILED = 7;



        public static int FindError(string line)
        {
            string[] toks = line.Split(new char[] { ' ', '\t' }, StringSplitOptions.RemoveEmptyEntries);
            if (toks.Length > 1)
            {
                if (toks[1][0] == 'w' || line.IndexOf("warning") >= 0)
                    return ERR_WARNING;
            }
            else
                return ERR_NOERROR;

            if (line.IndexOf(ERROR_NOT_FOUND) >= 0)
                return ERR_NOT_FOUND;
            if (line.IndexOf(ERROR_CERTIFICATE_FAILED) >= 0)
            {
                if (Password == "" && Username == "")
                    return ERR_AUTHENTICATION;
                else
                    return ERR_CERTIFICATE_FAILED;
            }
            else if (line.IndexOf(ERROR_ACCESS_FORBIDEN) >= 0)
                return ERR_FORBIDEN;
            else if (line.IndexOf(ERROR_AUTHENTICATION) >= 0)
                return ERR_AUTHENTICATION;
            else if (line.IndexOf(ERRR_NOT_WORKING_COPY) >= 0)
                return ERR_WORKINGCOPY;
            else if (line.IndexOf(ERROR_UNFINISHED_OP) >= 0)
                return ERR_UNFINISHED;

            return ERR_UNKNOWN;
        }

        public static void SetSVNBin(string path)
        {
            SvnBin = path;
        }

        public static string Cleanup(string path)
        {
            return "cleanup --non-interactive --trust-server-cert " + path;
        }

        public static string UpdateToRevision(string path, int revision)
        {
            string rez;
            if (isTortoiseProc)
            {
                if (revision > 0)
                    rez = "/command:update " + " /path:" + path + " /revision:" + revision + " /closeonend:1";
                else
                    rez = "/command:update " + " /path:" + path + " /closeonend:1";
            }
            else
            {
                if (revision > 0)
                    rez = " update -r" + revision + " " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert " + path;
                else
                    rez = " update " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert " + path;
            }
            return rez;
        }
        public static string CheckoutToRevision(string url, string path, int revision)
        {
            string rez;
            if (isTortoiseProc)
            {
                if (revision > 0)
                    rez = "/command:checkout " + "/url:" + url + " /path:" + path + " /revision:" + revision + " /closeonend:1";
                else
                    rez = "/command:checkout " + "/url:" + url + " /path:" + path + " /closeonend:1";
            }
            else
            {
                if (revision > 0)
                    rez = " checkout -r" + revision + " " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert " + url + " " + path;
                else
                    rez = " checkout " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert " + url + " " + path;
            }
            return rez;
        }
        public static string Revert(string path)
        {
            return " revert -R --non-interactive --trust-server-cert " + path;
        }
        public static string GetRepositoryInfo(string url)
        {
            return " info " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert " + url;

        }
        public static string GetCertificateQuestion(string url)
        {
            //return " info --config-option config:auth:store-auth-creds=yes " + "--username " + Username + " --password " + Password + " --trust-server-cert " + url;
            return " info --config-option config:auth:store-auth-creds=yes " + "--username " + Username + " --password " + Password + " " + url;
        }
        public static string GetLog(string url, int revision)
        {
            string rez;
            if (revision > 0)
                rez = " log" + " -r" + revision + " " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert " + url;
            else
                rez = " log " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert " + url;

            return rez;
        }
        public static string GetLog(string url, int[] revision)
        {
            string rez = " log " + "--username " + Username + " --password " + Password + " --non-interactive --trust-server-cert ";
            for (int i = 0; i < revision.Length; i++)
                rez = rez + "-r" + revision[i] + " ";
            rez = rez + url;
            return rez;
        }
        public static string GetStatus(string path)
        {
            return " status " + path;
        }



        public static void StartProcess(String svnAddress, String path, bool checkout, int revision)
        {
            if(checkout)
                CommandArgs = SVNCommands.CheckoutToRevision(svnAddress, path, revision);
            else
                CommandArgs = SVNCommands.UpdateToRevision(path, revision);
                
            
            if (isTortoiseProc)
                CommandLine = SVNCommands.SvnProc;
            else
                CommandLine = SVNCommands.SvnBin;

            ProcessStartInfo psi = new ProcessStartInfo();
            //psi.WorkingDirectory = WorkingFolder;
            if (InteractiveLaunch)
            {
                psi.RedirectStandardOutput = false;
                psi.RedirectStandardInput = false;
                psi.RedirectStandardError = false;
                psi.CreateNoWindow = false;
                psi.UseShellExecute = true;
            }
            else
            {


                psi.RedirectStandardOutput = true;
                psi.RedirectStandardInput = true;
                psi.RedirectStandardError = true;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
            }
            psi.FileName = CommandLine;
            psi.Arguments = CommandArgs;
            //psi.Verb = "runas";

            string command = CommandLine + " " + CommandArgs;

            if (Password != null && Password != "")
                command = command.Replace(Password, "****");

            //Globals.executedCommands.Add(command);
            //LibScreen.AddConsoleLine("command: " + command);

            Process pr;
            try
            {
                pr = Process.Start(psi);
            }
            catch (Exception e)
            {
                String errMsg = "Process.Start: " + "\r\n" + e.ToString();
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

            if (InteractiveLaunch)
            {
                pr.WaitForExit();
                return;
            }

            int errorcode = pr.ExitCode;
            if (errorcode != 0)
                MessageBox.Show("tortoise svn has returned with an error!");
        }
    }

}
