using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.IO;
using System.Windows.Forms;
using System.Diagnostics;
using System.Threading;
using GLLegacyInstaller;
using Utils;


namespace GLLegacyInstaller
{
    public partial class UpdateEnvironment : Form
    {
        static bool s_reportWasSent = false;
        Exception m_ex = null;

        public UpdateEnvironment()
        {
            InitializeComponent();

            backgroundWorker.WorkerSupportsCancellation = true;
            backgroundWorker.WorkerReportsProgress = true;


            //Tool rm = (Tool)MainWindow.s_instance.m_toolList.Get(100);
            //rm.UpdateEnvVariables();



            AlignComponents();
        }


        public void setProgressBar()
        {
            m_progressBar.Value++;   
        }

        
        public void AlignComponents()
        {
            CenterComponent(LegacyLogo);
            CenterComponent(InfoLabel);            
            CenterComponent(cancel);

            m_progressBar.Value = 0;
        }
        public void CenterComponent(Control c)
        {
            c.Location = new Point(this.Size.Width / 2 - c.Size.Width / 2, c.Location.Y);
        }
        

        private void cancel_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
        }





        private void backgroundWorker_DoWork(object sender, DoWorkEventArgs e)
        {
            try
            {
                BackgroundWorker worker = sender as BackgroundWorker;

                SourceGrid.Grid grid = MainWindow.s_instance.m_gridTools;

                m_progressBar.Maximum = grid.RowsCount - 1;

                Globals.ResetEnvironmentVarBatchScript(Globals.GetFullPathToInstallerEnvBatchScript());

                /*for (int i = 1; i < grid.RowsCount; i++)
                {
                    if ((worker.CancellationPending == true))
                    {
                        e.Cancel = true;
                        break;
                    }
                    else
                    {
                        // Perform a time consuming operation and report progress.
                        System.Threading.Thread.Sleep(20);
                        worker.ReportProgress((i));
                    }


                    int ID = (int)(grid[i, 0].Value);

                    Tool rm = (Tool)MainWindow.s_instance.m_toolList.Get(ID);

                    rm.UpdateEnvVariables();
                }*/
                //update environment variable for all tools, not only valid one
                for (int i = 0; i < MainWindow.s_instance.m_toolList.GetCount(); ++i) 
                {
                    Tool temptool = (Tool)MainWindow.s_instance.m_toolList.Get(i);
                    temptool.UpdateEnvVariables();
                }

                    #region set GLLEGACY_NDK_HOME
                    //write in GLLegacyInstallerEnv.bat "set GLLEGACY_NDK_HOME=%GLLEGACY_DISTCC_DRIVE_SELECTED%"

                    //if (Globals.IsPlatformSelected(Globals.s_strAndroid))
                    {
                        StreamWriter str = new StreamWriter(Globals.GetFullPathToInstallerEnvBatchScript(), true);
                        
                        //foreach ( string value in Globals.s_distccDrives )
                        if (!string.IsNullOrEmpty(Globals.s_distccDriveSelected))
                        {
                            int index = 0;
                            if (Globals.s_distccDrives.Contains(Globals.s_distccDriveSelected))
                                index = Globals.s_distccDrives.IndexOf(Globals.s_distccDriveSelected);
                                /*for (k = 0; k < Globals.s_distccDrives.Count; k++)
                                if (Globals.s_distccDrives[k].ToString() == Globals.s_distccDriveSelected)
                                    break;*/
                            str.WriteLine("set " + Globals.s_envVar_GLLEGACY_NDK_HOME + "=%" + Globals.s_distccNDKEnvVar[index] + "%");
                        }
                        
                       
                        str.Close();
                        str = null;
                    }
                #endregion
            }
            catch(Exception ex)
            {
                m_ex = ex;
                String errMsg = "backgroundWorker_DoWork: " + "\r\n";
                errMsg += ex.StackTrace.ToString();
                if (Globals.s_workInConsole)
                {
                    Console.WriteLine(errMsg);
                }
                else
                {
                    MessageBox.Show(this, errMsg, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }

            }
        }

        private void backgroundWorker_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            m_progressBar.Value = e.ProgressPercentage;
        }

        private void backgroundWorker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            this.DialogResult = DialogResult.OK;

            if (m_ex != null && !s_reportWasSent)
            {
                s_reportWasSent = true;
                Program.sendCrash("UpdateEnvironment Thread Exception", m_ex.Message, m_ex.StackTrace.ToString(), false);
            }

            //this.Close();
        }

        private void UpdateEnvironment_Load(object sender, EventArgs e)
        {
            SourceGrid.Grid grid = MainWindow.s_instance.m_gridTools;
            m_progressBar.Maximum = grid.RowsCount - 1;

            backgroundWorker.RunWorkerAsync();
        }

      

    }
}
