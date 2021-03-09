using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using Utils;


namespace GLLegacyInstaller
{
    public partial class MessageScreen : Form
    {
        public MessageScreen()
        {
            InitializeComponent();

            Globals.s_warningIcon = global::BuildConfiguratorV2.Properties.Resources.WarningImage;
            Globals.s_errorIcon = global::BuildConfiguratorV2.Properties.Resources.ErrorImage;
        }

        public MessageScreen(Form parent)
        {
            InitializeComponent();
            parent.Show();
            this.Owner = parent;
        }
        public void SetMessage(string msg)
        {
            Message.Text=msg;
            Message.Location = new System.Drawing.Point(this.Width / 2 - Message.Width / 2, Message.Location.Y);
        }
        public void SetType(string type)
        {
            TypeLabel.Text = type;
            MessagePicture.Image=null;
            if (type == "Error" || type == "ERROR")
            {
                OkButton.Enabled = false;
                //MessagePicture.Image = global::GLLegacyInstaller.Properties.Resources.ErrorImage;
                MessagePicture.Image = Globals.s_errorIcon;
            }
            else if (type=="Warning"|| type =="WARNING")
            {
                TypeLabel.ForeColor = Color.DarkGoldenrod;
                //MessagePicture.Image = global::GLLegacyInstaller.Properties.Resources.WarningImage;
                MessagePicture.Image = Globals.s_warningIcon;
            }
        }

        private void BackButton_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private void OkButton_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.OK;
            this.Close();
        }

        private void MessageScreen_Load(object sender, EventArgs e)
        {

        }

        
    }
}
