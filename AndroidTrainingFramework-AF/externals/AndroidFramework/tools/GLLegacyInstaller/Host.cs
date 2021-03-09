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
    public class Host
    {
        //static private int s_id = 0;

        //public int m_ID = 0;

        public String[] m_params = null;

        //public String m_hostName    = "";
        public bool m_valid         = false;
        public String m_desc        = "";
       
        public Host(int ID, String[] param)
        {
            m_params = param;
        }
    }
}
