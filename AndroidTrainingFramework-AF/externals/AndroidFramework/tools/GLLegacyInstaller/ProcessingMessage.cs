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
    public partial class ProcessingMessage : Form
    {
        public static Point s_size = new Point(338, 250);
        public ProcessingMessage()
        {
            InitializeComponent();
            this.Width = s_size.X;
            this.Height = s_size.Y;
        }

        private void ProcessingMessage_Load(object sender, EventArgs e)
        {

        }
    }
}
