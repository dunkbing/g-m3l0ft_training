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
    public partial class SuspendedInput : Form
    {
        public SuspendedInput()
        {
            InitializeComponent();
        }
        public void SetMessage(String msg)
        {
            label1.Text = msg;
        }
    }
}
