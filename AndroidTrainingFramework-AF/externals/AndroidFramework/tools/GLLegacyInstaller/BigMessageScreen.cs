using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace GLLegacyInstaller
{
    public partial class BigMessageScreen : Form
    {
        public BigMessageScreen()
        {
            InitializeComponent();
        }
		
		
        public void SetMessage(string msg)
        {
            msg = msg.Replace("\n", "\r\n");
            Message.Text = msg;
        }
        public void SetType(string type)
        {
            TypeLabel.Text = type;
            MessagePicture.Image = null;
            if (type == "Error" || type == "ERROR")
            {
                OkButton.Enabled = false;
                MessagePicture.Image = global::GLLegacyInstaller.Properties.Resources.ErrorImage;
            }
            else if (type == "Warning" || type == "WARNING")
            {
                TypeLabel.ForeColor = Color.DarkGoldenrod;
                MessagePicture.Image = global::GLLegacyInstaller.Properties.Resources.WarningImage;
            }
            else if (type == "Good")
            {
                TypeLabel.ForeColor = Color.Green;
                OkButton.Enabled = true;
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
    }
}
