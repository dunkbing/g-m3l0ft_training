using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.Windows.Forms;
using System.Collections;
using System.IO;
using Utils;

namespace GLLegacyInstaller
{
    //class for Tool
    public class Tool
    {
        static private int s_id = 0;

        /**
        * m_ID - IF of the tool
        */
        public int m_ID = 0;
        /**
        * m_toolName - string with the name of the tool
        */
        public String m_toolName    = "";
        /**
        * m_required - bool true if the tool is required
        */
        public bool m_required      = false;
        /**
        * m_minVersion - string with the minimal version required
        */
        public String m_minVersion  = "";
        /**
        * m_version - string with the tool version
        */
        public String m_version = "";
        /**
        * m_path - string with the path of the tool
        */
        public String m_path        = "";
        /**
        * m_detectedPath - string with detected path
        */
        public String m_detectedPath = "";
        /**
        * m_downloadURL - string with URL for download of the tool
        */
        public String m_downloadURL = "";
        /**
        * m_valid - bool true if tool is valid
        */
        public bool m_valid = false;
        /**
        * m_desc - string with tool description
        */
        public String m_desc = "";
        /**
        * m_InstallScriptPath - string with path of script for install the tool
        */
        public String m_InstallScriptPath = "";
        /**
        * m_VirtualDrive - string with virtual drive, if needed, for tool
        */
        public String m_VirtualDrive = "";
        /**
        * m_platforms - ArrayList eith the platforms that require this tool
        */
        public ArrayList m_platforms = new ArrayList();
        /**
        * m_AllowSpaceInPath - bool true if this tool allow space in path
        */
        public bool m_AllowSpaceInPath = false;
        /**
        * m_installPaths - list with possible install paths
        */
        public List<InstallPathBase> m_installPaths = new List<InstallPathBase>();
        /**
        * m_checkPaths - list with checks for every possible path
        */
        public List<CheckPathBase> m_checkPaths = new List<CheckPathBase>();

        /**
        * m_AddEnvToPATH - string with environment to add
        */
        public String m_AddEnvToPATH = null;

        /**
        * m_downloadURL - Hashtable with environment variables
        */
        public Hashtable m_environmentVar = new Hashtable();
        /**
        * m_errors - string with errors
        */
        String m_errors = null;

        /**
        * ResetID - reset tool ID    
        **/
        public void ResetID()
        {
            s_id = 0;
        }

        /**
        * Tool - tool constructor
        * @param ID - ID of tool
        * @param toolName - name of tool
        **/
        public Tool(int ID, String toolName)
        {
            if (ID == -1)
            {
                m_ID = s_id++;
            }
            else
            {
                m_ID = ID;

                if (s_id <= ID) s_id = ID + 1;
            }

            m_toolName = toolName;
        }


        /**
        * UpdateEnvVariables - Update environment variables
        **/
        public void UpdateEnvVariables()
        {

            foreach (DictionaryEntry entry in m_environmentVar)
            {
                String key = (String)entry.Key;
                String value = (String)entry.Value;

                if (key == null || key.Length == 0) continue;
                
                value = value.Replace("$InstallPath$", m_path);

                value = Globals.ReplaceSeparators(value);

                while (value.Length > 0 && ((value[value.Length - 1] == '\\') || (value[value.Length - 1] == '/')))
                {
                    value = value.Substring(0, value.Length - 1);
                }

                //save tool even have null path
                //if (value == null || value.Length == 0) continue;

                String errorMessage = Globals.AddEnvironmentVar(key, value, Globals.GetFullPathToInstallerEnvBatchScript(), true);

                if (errorMessage != null)
                {
                    if (Globals.s_workInConsole)
                    {
                        Console.WriteLine(errorMessage);
                    }
                    else
                    {
                        MessageBox.Show(errorMessage, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                }
            }

            if (m_AddEnvToPATH != null)
            {
                String value = m_AddEnvToPATH;

                value = value.Replace("$InstallPath$", m_path);

                value = Globals.ReplaceSeparators(value);

                String errorMessage = Globals.AddToEnvironmentPath(value);

                if (errorMessage != null)
                {
                    if (Globals.s_workInConsole)
                    {
                        Console.WriteLine(errorMessage);
                    }
                    else
                    {
                        MessageBox.Show(errorMessage, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                }
            }
        }


        /**
        * DetectToolInstallPath - Detect tool install path
        **/
        public void DetectToolInstallPath()
        {
            //System.Diagnostics.Stopwatch sw = new System.Diagnostics.Stopwatch();
            //sw.Start();
           // ResetErrors();
            //m_ErrorLines.Clear();
            //m_errors = null;            
            for(int i = 0; i < m_installPaths.Count; i++)
            {
                m_installPaths[i].Refresh();

               String path = m_installPaths[i].GetInstallPath();

                //path = Path.GetDirectoryName(path);

               m_errors += "\n\nScrutinizing" + i.ToString() + ":\n";

                m_valid = IsInstallPathValid(path);

                if (m_valid)
                {
                    //Console.WriteLine("detect finished in " + sw.ElapsedMilliseconds + " ms");
                    //test
                    m_detectedPath = path;
                    m_valid = IsVersionValid(path);
                    if (m_valid)
                        return;
                }
            }

            m_valid = false;
        }

        /**
        * IsUsedTool - check if tool is used
        * returns a boolean that is true if the tool is used by any platform
        **/
        public bool IsUsedTool()
        {
            bool bUsedTool = true;

            //bool bUsedTool = false;
            //if (m_platforms.Count == 0)
            //    bUsedTool = true;
            //else
            //{
            //    for (int i = 0; i < Globals.s_platformsInView.Count; i++)
            //    {
            //        for (int j = 0; j < m_platforms.Count; j++)
            //        {
            //            if ((string)m_platforms[j] == Globals.s_platformsInView[i].ToString().ToUpper())
            //            {
            //                bUsedTool = true;
            //                break;
            //            }
            //        }
            //        if (bUsedTool)
            //            break;
            //    }
            //}

            return bUsedTool;
        }

        /**
        * IsVersionValid - check if tool has a valid version
        * param path - a string with the path of tool
        * returns a boolean that is true if the tool if tool has a valid version
        **/
        public bool IsVersionValid(String path)
        {
            string versionFile = path + "\\VersionForGLLegacy.txt";
            if (!File.Exists(versionFile))
            {
                m_version = "";
                return true;
            }

            StreamReader str = new StreamReader(versionFile);
            m_version = str.ReadLine();
            str.Close();

            if (string.IsNullOrEmpty(m_version))

                return true;
            if (string.IsNullOrEmpty(m_minVersion))
                return true;
            var minVersion = new Version(m_minVersion);
            var version = new Version(m_version);

            var result = minVersion.CompareTo(version);

            if (result > 0)
                return false;
            return true;
        }

        /**
        * IsInstallPathValid - check if tool has a valid path
        * param path - a string with the path of tool
        * returns a boolean that is true if the tool has a valid path
        **/
        public bool IsInstallPathValid(String path)
        {
            bool valid = true;
            bool everyThingIsFine = true;
            m_errors = "";
            for (int j = 0; j < m_checkPaths.Count; j++)
            {
                bool pass = m_checkPaths[j].IsPathValid(path);

                String lastError = m_checkPaths[j].GetLastError();

                if ( path != null && !m_AllowSpaceInPath && path.IndexOf(" ") != -1)
                {
                    m_errors += "\tSpace not allowed in path!!!\n";
                    everyThingIsFine = false;
                    valid = false;
                }

                if (lastError != null)
                {
                    if (lastError.Length > 2)
                    {
                        everyThingIsFine = false;
                        m_errors += "   " + lastError;
                    }
                }
                if (!pass)
                {
                    valid = false;
                }
                                               
            }
            if (everyThingIsFine) 
            {
                if (!IsVersionValid(path))
                {
                    valid = false;
                    m_errors += "\n\tYour version (" + m_version + ") is older than expected (" + m_minVersion + ").";
                }
                else
                    m_errors = "\t\t\tThis tool works. "; 
                    if(!string.IsNullOrEmpty(m_version))
                        m_errors += "Your version is " + m_version +".";
            }
            return valid;
        }

        /**
        * ResetErrors - set the string errors on null
        **/
        public void ResetErrors()
        {
            m_errors = null;
        }

        /**
        * GetErrors - get errors
        * returns a string with errors 
        **/
        public String GetErrors()
        {
           //String errmsg = "Tool not found. Please enter the path where it is installed.";
           
            /*if( !m_valid )
            {
                if (!string.IsNullOrEmpty(m_path))
                    errmsg = "Tool not found to " + m_path + ". Please enter the correct path where it is installed.";
                return errmsg;
            }*/
            
            return m_errors;
        }

    }
}
