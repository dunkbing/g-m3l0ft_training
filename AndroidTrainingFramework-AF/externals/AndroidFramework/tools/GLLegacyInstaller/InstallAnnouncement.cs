using System;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using GLLegacyInstaller;


namespace GLLegacyInstaller
{
    public partial class InstallAnnouncement : Form
    {
        private String m_FolderPath;
        private String m_toolName;
        public InstallAnnouncement()
        {
            InitializeComponent();
            nextBT.Enabled = false;
        }
        public InstallAnnouncement(Form parent)
        {
            InitializeComponent();
            parent.Show();
            this.Owner = parent;
            nextBT.Enabled = false;
        }
        public void SetMessage(String msg)
        {
            InstallToolLB.Text = "Install Tool " + msg;
            m_toolName = msg;
        }
        public void SetIcon(Image pic)
        {
            iconPB.Image = pic;
            iconPB.Visible = true;
            iconPB.Enabled = true;
        }
        public void SetIcon(bool state)
        {
            iconPB.Visible = state;
        }
        public String GetFolder()
        {
            return m_FolderPath;
        }
        private void nextBT_Click(object sender, EventArgs e)
        {
            string path = "";
            int index = toolPathTB.Text.IndexOf(@":\");
            if (index == -1)
                path = Directory.GetCurrentDirectory();
            else
                if (index != 1)
                {
                    MessageBox.Show("The path is not valid. Please enter a valid folder!\n", "Invalid folder",
                                MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }
            char[] invalidPathChars = Path.GetInvalidPathChars();
            string invalidChars = "";

            foreach (char c in invalidPathChars)
            {
                if (toolPathTB.Text.IndexOf(c) != -1)
                    invalidChars += c.ToString();
            }

            /*if (!string.IsNullOrEmpty(path))
                if (toolPathTB.Text.IndexOf(":") != -1)
                    invalidChars += ':';*/

            if (!string.IsNullOrEmpty(invalidChars))
            {
                MessageBox.Show("The path has invalid chars: \"" + invalidChars + "\" . Please enter a valid folder!\n", "Invalid folder",
                            MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            if (!Directory.Exists(toolPathTB.Text))
            {
                BigMessageScreen dlg = new BigMessageScreen();
                dlg.SetMessage("Selected folder\r\n" + path + toolPathTB.Text + "\r\ndoesn't exists!\r\nDo you want to create it?");
                dlg.SetType("WARNING");
                dlg.TopMost = true;
                DialogResult dlgres = dlg.ShowDialog();
                dlg = null;
                if (dlgres == DialogResult.OK)
                {
                    try
                    {
                        Directory.CreateDirectory(toolPathTB.Text);
                    }
                    catch (Exception ex)
                    {
                        //String errMsg = "CreateDirectory" + "\r\n" + ex.Message;
                        MessageBox.Show(ex.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return;
                    }
                    /*if (!Directory.Exists(toolPathTB.Text))
                    {
                        MessageBox.Show("Could not create directory!\n", "Directory creation failed",
                                   MessageBoxButtons.OK, MessageBoxIcon.Error);

                        return;
                    }*/
                }
                else
                    return;
            }
            else
            {
                if (!(System.IO.Directory.GetDirectories(toolPathTB.Text).Length == 0 && System.IO.Directory.GetFiles(toolPathTB.Text, "*", SearchOption.AllDirectories).Length == 0))
                {
                    MessageScreen dlg = new MessageScreen(this);
                    dlg.SetMessage("Selected folder is not empty! Are you sure you want to install there?");
                    dlg.SetType("WARNING");
                    dlg.TopMost = true;
                    DialogResult dlgres = dlg.ShowDialog();
                    dlg = null;
                    if (dlgres != DialogResult.OK)
                    {
                        return;
                    }
                }
            }
            m_FolderPath = Path.Combine(path, toolPathTB.Text);
            this.DialogResult = DialogResult.OK;
            this.Close();
            
        }
        private void prevBT_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private void InstallAnnouncement_Load(object sender, EventArgs e){}
        private void folderBrowser_HelpRequest(object sender, EventArgs e) {}

        private void pickFolderBT_Click(object sender, EventArgs e) 
        {
            DialogResult dr = folderBrowser.ShowDialog();
            if (dr == DialogResult.OK)
            {
                m_FolderPath = folderBrowser.SelectedPath + "\\" + m_toolName;
                if (m_FolderPath.Length > 3)
                {
                    nextBT.Enabled = true;
                    toolPathTB.Text = m_FolderPath;
                }
                else
                {
                    toolPathTB.Text = "Cannot install directly to drive root.";
                }
            }
//            else {
//                nextBT.Enabled = false;
//            }
        }

        private void toolPathTB_TextChanged(object sender, EventArgs e)
        {
            if (string.IsNullOrEmpty(toolPathTB.Text))
                nextBT.Enabled = false;
            else
                nextBT.Enabled = true;
        }
    }
}
