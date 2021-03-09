using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace GLLegacyInstaller
{
    public class CheckPathBase
    {
        const String k_checkType_folderExist = "folderExist";
        const String k_checkType_fileExist = "fileExist";
        const String k_checkType_fileContains = "fileContains";

        String m_name = "";
        String m_path = "";
        String m_checkType = k_checkType_folderExist; //fileExist or folderExist
        String m_value = ""; //value to check if k_checkType_fileContains is used

        String m_error = null;

        public CheckPathBase(String name, String path, String checkType, String value)
        {
            m_name = name;
            m_path = path;
            m_checkType = checkType;
            m_value = value;
        }

        public virtual bool IsPathValid(String path)
        {

            int i = m_path.LastIndexOf("$InstallPath$") + "$InstallPath$".Length + 1;
            String folderName = m_path.Substring(i, m_path.Length - i);

            string convertedPath = m_path.Replace("$InstallPath$", path);

            if (m_checkType == k_checkType_folderExist)
            {
                bool pass = Directory.Exists(convertedPath);
                if (pass)
                {
                    m_error = "";
                }
                else
                {
                    //m_error += "\t Folder  [" +  convertedPath + "]   not found!\n";
                    m_error += "\t Folder [ " + folderName + " ] not found!\n";    
                }

                return pass;
            }
            else if (m_checkType == k_checkType_fileExist)
            {
                bool pass = File.Exists(convertedPath);
                if (pass)
                {
                    m_error = "";
                }
                else
                {
                    //m_error += "\t File [" +  convertedPath + "]  not found!\n";
                    m_error += "\t File [ " + folderName + " ]  not found!\n";
                }

                return pass;
            }
            else if (m_checkType == k_checkType_fileContains)
            {
                if (!File.Exists(convertedPath))
                {
                    //m_error += "\t File [" +  convertedPath + "]  not found!\n";
                    m_error += "\t File [ " + folderName + " ] not found!\n";
                    return false;
                }

                using (StreamReader sr = new StreamReader(convertedPath))
                {
                    String content = sr.ReadLine();
                    if (!content.Contains(m_value))
                    {
                        //m_error += "\t The file  [" +  convertedPath + "]  does not contains '" + m_value + "' \n!!";
                        m_error += m_name  + ":\t The file [ " + folderName + " ] does not contains '" + m_value + "'!!";
                        return false;
                    }
                }
                m_error = "";

                return true;
            }

            return false;
        }

        public String GetLastError()
        {
            String err = m_error;
            m_error = "";
            return err;
        }
    }
}
