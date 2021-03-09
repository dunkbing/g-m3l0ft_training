using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using Utils;


namespace GLLegacyInstaller
{
    public partial class About : Form
    {
        public About()
        {
            InitializeComponent();
            LogoPicture.Location = new Point(this.Size.Width / 2 - LogoPicture.Size.Width/2,LogoPicture.Location.Y);
            VersionLabel.Text = "Installer Version : " + Globals.s_version;
            VersionLabel.Location = new Point(this.Size.Width / 2 - VersionLabel.Size.Width / 2, VersionLabel.Location.Y);
            ContactLabel.Location = new Point(this.Size.Width / 2 - ContactLabel.Size.Width / 2, ContactLabel.Location.Y);
            Contact1.Location = new Point(this.Size.Width / 2 - Contact1.Size.Width / 2, Contact1.Location.Y);
            OkButton.Location = new Point(this.Size.Width / 2 - OkButton.Size.Width / 2, OkButton.Location.Y);
        }

        private void OkButton_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void Contact1_Click(object sender, EventArgs e)
        {

        }

        private void About_Load(object sender, EventArgs e)
        {

        }

        private void VersionLabel_Click(object sender, EventArgs e)
        {

        }

        private void Contact1_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            System.Diagnostics.Process.Start(@"mailto:World-ITConsulting@gameloft.com?subject=GLLecacy" + VersionLabel.Text);
        }

       
    }
}
