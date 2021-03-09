using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using Utils;

namespace GLLegacyInstaller
{
    public class InstallPathBase
    {
        protected String m_installPath = "";
        protected String m_addToPath = "";

        public InstallPathBase(String installPath, String addToPath)
        {
            m_installPath = installPath;
            m_addToPath = addToPath;
        }
        public InstallPathBase(String addToPath)
        {
            m_addToPath = addToPath;
        }

        public virtual void Refresh()
        {

        }

        // Get directory name for install path
        public virtual String GetInstallPath()
        {

            String path = string.IsNullOrEmpty(m_installPath) ? Directory.GetCurrentDirectory() : m_installPath;

            if (!string.IsNullOrEmpty(m_addToPath)) // VCS
            {
                if (m_addToPath[0] == '\\')
                    path = path + m_addToPath;
                else
                    path = Path.Combine(path, m_addToPath);
            }

            //if (!Path.IsPathRooted(path))
            //{
            //    path = Directory.GetCurrentDirectory() + Path.DirectorySeparatorChar + path;
            //}

            try
            {
                System.IO.DirectoryInfo info = new DirectoryInfo(path);
                if (File.Exists(path))
                    path = info.Parent.FullName;
                else
                    path = info.FullName;

                info = null;
            }
            catch(Exception )
            {
                //Console.WriteLine("The path is bad: " + path);
            }

            return path;
        }
    }


    public class InstallPathEnv: InstallPathBase
    {
        String m_envVar = null;

        public InstallPathEnv(String envVar, String addToPath)
            : base(addToPath)
        {
            m_envVar = envVar;
            Refresh();
        }

        public override void Refresh()
        {
            string path = Globals.GetEnvironmentVar(m_envVar);
            if (string.IsNullOrEmpty(path))
            {
                path = (string)Globals.s_myEnvars[m_envVar];
            }
            if (path != null)           
                m_installPath = path;
        }
    }


    public class InstallPathReg: InstallPathBase
    {
        String m_regEntry = null;
        String m_key = null;

        public InstallPathReg(String regEntry, String key, String addToPath)
            : base(addToPath)
        {
            m_regEntry = regEntry;
            m_key = key;
            Refresh();
        }

        public override void Refresh()
        {
            string path = Globals.GetSoftwareRegValue(m_regEntry, m_key);
            if (path != null)
                m_installPath = path;

        }
    }

}


