using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace PlatformTabPage
{
    public partial class CommandsListBox : Form
    {
        private List<String> m_Text;
        private Color        m_Color;
        private String       m_WorkingDir;

        public CommandsListBox(Form parent)
        {
            InitializeComponent();
            parent.Show();
            this.Owner = parent;
            m_WorkingDir = null;
            m_Color = SystemColors.HotTrack;
        }
        public Color Color {
            get {
                return m_Color;
            }
            set {
                m_Color = value;
                Update(true);
            }
        }
        public List<String> MessageText { 
            get{
                return m_Text;
            }
            set {
                m_Text = value;
                Update(false);
            }
        }

        public void Update(bool force) 
        {
            string incomingText = "";
            for(int i=0; i<m_Text.Count; ++i)
                incomingText += m_Text[i];
            string messageListText = messageList.Text;

            if (force==false && compareLastNLettersString(incomingText, messageListText, incomingText.Length - 1) == true) return;

            //at this stage the update portion begins
            messageList.ResetText();
            
            Color revBack = messageList.SelectionBackColor;
            Color revSel = messageList.SelectionColor;
            Font revFont = messageList.SelectionFont;

            messageList.SelectionFont = new Font("Arial", 20);
            messageList.SelectionColor = m_Color;
            messageList.AppendText("Commands History");
            messageList.AppendText(Environment.NewLine);
            if (m_WorkingDir != null && m_WorkingDir.Length > 0 && m_Text.Count > 0)
            {
                messageList.SelectionFont = new Font("Arial", 12);
                messageList.SelectionColor = m_Color;
                messageList.AppendText("    Working directory: ");
                messageList.SelectionColor = revSel;
                messageList.SelectionFont = revFont;
                messageList.AppendText(m_WorkingDir + Environment.NewLine);
            }
            if (m_Text.Count < 1)
            {
                messageList.SelectionFont = revFont;
                messageList.SelectionColor = revSel;
                messageList.AppendText("  No commands issued yet.");
                messageList.AppendText(Environment.NewLine);
                return;
            }

            Font ft = messageList.SelectionFont;
            Font ftl = new Font("Arial", 10);
            for (int i = 0; i < m_Text.Count-1; ++i) {
                messageList.SelectionFont = ftl;
                messageList.SelectionColor = m_Color;
                messageList.AppendText("  " + i.ToString() + ":  ");
                messageList.SelectionFont = revFont;
                messageList.SelectionColor = revSel;
                messageList.AppendText(m_Text[i]);
                messageList.AppendText(Environment.NewLine);
            }

            messageList.SelectionFont = ftl;
            messageList.SelectionColor = m_Color;
            messageList.AppendText("  " + (m_Text.Count - 1).ToString() + ":  ");
            messageList.SelectionFont = new Font(messageList.Font, FontStyle.Bold); ;
            messageList.SelectionColor = revSel;
            messageList.AppendText(m_Text[m_Text.Count - 1]);
            messageList.AppendText(Environment.NewLine);
            messageList.ScrollToCaret();
        }

        private bool compareLastNLettersString(String s1, String s2, int n) {
            if (n == 0 || s1 == null || s2 == null || s1.Length == 0 || s2.Length == 0) return false;
            int a = s1.Length - 1;
            int b = s2.Length - 1;
            int f = 0;
            while (true) {
                if (a <= 0 || n<=0) break;
                if (b <= 0) break; 
                while (char.IsLetter(s1[a]) == false && a >= 0) a--;
                while (char.IsLetter(s2[b]) == false && b >= 0) b--;
                
                if (s1[a] != s2[b])
                {
                    f++;
                    break;
                }
                else {
                    n--;
                    b--;
                    a--;
                }
            }
            if (f == 0) return true;
            else return false;
        }

        private void CommandsListBox_Load(object sender, EventArgs e)
        {

        }
    }
}
