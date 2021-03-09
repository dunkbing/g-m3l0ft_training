using System.Windows.Forms;

namespace GLLegacyInstaller
{
    partial class MainWindow
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainWindow));
            this.m_button_Next = new System.Windows.Forms.Button();
            this.m_button_Prev = new System.Windows.Forms.Button();
            this.menuStrip = new System.Windows.Forms.MenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.helpToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.installerDocsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.aboutToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.label2 = new System.Windows.Forms.Label();
            this.m_version = new System.Windows.Forms.Label();
            this.m_startTimer = new System.Windows.Forms.Timer(this.components);
            this.m_TAB_SVNOperations = new System.Windows.Forms.TabPage();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.pictureBox2 = new System.Windows.Forms.PictureBox();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.lbReleaseRev = new System.Windows.Forms.Label();
            this.linkLabelDevelopment = new System.Windows.Forms.LinkLabel();
            this.label7 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.radioButtonDevelopment = new System.Windows.Forms.RadioButton();
            this.linkLabelRelease = new System.Windows.Forms.LinkLabel();
            this.radioButtonRelease = new System.Windows.Forms.RadioButton();
            this.label5 = new System.Windows.Forms.Label();
            this.TortoiseSVN = new System.Windows.Forms.Button();
            this.label3 = new System.Windows.Forms.Label();
            this.m_browse = new System.Windows.Forms.Button();
            this.m_textBoxPath = new System.Windows.Forms.TextBox();
            this.m_RB_Update = new System.Windows.Forms.RadioButton();
            this.m_RB_CheckOut = new System.Windows.Forms.RadioButton();
            this.m_TAB_CheckTools = new System.Windows.Forms.TabPage();
            this.buttonInstallerSetup = new System.Windows.Forms.Button();
            this.m_detectingProgress = new System.Windows.Forms.ProgressBar();
            this.m_button_Refresh = new System.Windows.Forms.Button();
            this.m_gridTools = new SourceGrid.Grid();
            this.m_TAB_Initialize = new System.Windows.Forms.TabPage();
            this.label1 = new System.Windows.Forms.Label();
            this.m_loadingProgress = new System.Windows.Forms.ProgressBar();
            this.m_TAB = new System.Windows.Forms.TabControl();
            this.m_TAB_DISTCC = new System.Windows.Forms.TabPage();
            this.splitContainer1 = new System.Windows.Forms.SplitContainer();
            this.m_groupBoxSHosts = new System.Windows.Forms.GroupBox();
            this.m_gridIPs = new SourceGrid.Grid();
            this.checkShowServers = new System.Windows.Forms.CheckBox();
            this.buttonAddIP = new System.Windows.Forms.Button();
            this.ipAddressControl = new IPAddressControlLib.IPAddressControl();
            this.buttonRescan = new System.Windows.Forms.Button();
            this.pictureBox3 = new System.Windows.Forms.PictureBox();
            this.numericUpDown = new System.Windows.Forms.NumericUpDown();
            this.comboBoxLetterDrive = new System.Windows.Forms.ComboBox();
            this.labelVirtualDrive = new System.Windows.Forms.Label();
            this.labelDiscoverTime = new System.Windows.Forms.Label();
            this.m_checkBoxDISTCCUse = new System.Windows.Forms.CheckBox();
            this.m_groupBoxHosts = new System.Windows.Forms.GroupBox();
            this.m_gridAvailableHosts = new SourceGrid.Grid();
            this.m_groupBoxServer = new System.Windows.Forms.GroupBox();
            this.textBoxJOBS = new System.Windows.Forms.TextBox();
            this.labelCORES = new System.Windows.Forms.Label();
            this.label17 = new System.Windows.Forms.Label();
            this.label16 = new System.Windows.Forms.Label();
            this.label11 = new System.Windows.Forms.Label();
            this.labelDISTCCStatus = new System.Windows.Forms.Label();
            this.label14 = new System.Windows.Forms.Label();
            this.m_checkBoxDISTCCServer = new System.Windows.Forms.CheckBox();
            this.groupBoxGLLS = new System.Windows.Forms.GroupBox();
            this.comboBoxNetAddr = new System.Windows.Forms.ComboBox();
            this.buttonOpenLogFile = new System.Windows.Forms.Button();
            this.buttonReinstallService = new System.Windows.Forms.Button();
            this.buttonRestartService = new System.Windows.Forms.Button();
            this.labelVersion = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.labelServiceStatus = new System.Windows.Forms.Label();
            this.label12 = new System.Windows.Forms.Label();
            this.label13 = new System.Windows.Forms.Label();
            this.m_TAB_Sumup = new System.Windows.Forms.TabPage();
            this.groupBox5 = new System.Windows.Forms.GroupBox();
            this.labelGLLegacyServiceStatus1 = new System.Windows.Forms.Label();
            this.labelGLLegacyServiceStatus = new System.Windows.Forms.Label();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.label10 = new System.Windows.Forms.Label();
            this.labelTotalTools = new System.Windows.Forms.Label();
            this.labelInvalidTools = new System.Windows.Forms.Label();
            this.label18 = new System.Windows.Forms.Label();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.labelSHostsNumber1 = new System.Windows.Forms.Label();
            this.labelSHostsNumber = new System.Windows.Forms.Label();
            this.labelDiscoverTimeS = new System.Windows.Forms.Label();
            this.labelDiscoverTime1 = new System.Windows.Forms.Label();
            this.label15 = new System.Windows.Forms.Label();
            this.labelDistccdStatus = new System.Windows.Forms.Label();
            this.labelDriveSelected = new System.Windows.Forms.Label();
            this.labelDistccdStatus1 = new System.Windows.Forms.Label();
            this.label19 = new System.Windows.Forms.Label();
            this.labelDriveSelected1 = new System.Windows.Forms.Label();
            this.pictureBoxUseHosts = new System.Windows.Forms.PictureBox();
            this.pictureBoxServer = new System.Windows.Forms.PictureBox();
            this.labelHostsNumber1 = new System.Windows.Forms.Label();
            this.labelHostsNumber = new System.Windows.Forms.Label();
            this.labelGLLegacyDistccHostsPath = new System.Windows.Forms.Label();
            this.labelGLLegacyDistccEnvPath = new System.Windows.Forms.Label();
            this.labelGLLegacyInstallerEnvPath = new System.Windows.Forms.Label();
            this.buttonDistccHosts = new System.Windows.Forms.Button();
            this.buttonDistccEnv = new System.Windows.Forms.Button();
            this.buttonEnv = new System.Windows.Forms.Button();
            this.m_distccServerCheck = new System.Windows.Forms.Timer(this.components);
            this.m_distccHostFileCheck = new System.Windows.Forms.Timer(this.components);
            this.folderPicker = new System.Windows.Forms.FolderBrowserDialog();
            this.labelAndroidWarning = new System.Windows.Forms.Label();
            this.toolTip1 = new System.Windows.Forms.ToolTip(this.components);
            this.pictureBox1 = new System.Windows.Forms.PictureBox();
            this.menuStrip.SuspendLayout();
            this.m_TAB_SVNOperations.SuspendLayout();
            this.groupBox1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).BeginInit();
            this.groupBox2.SuspendLayout();
            this.m_TAB_CheckTools.SuspendLayout();
            this.m_TAB_Initialize.SuspendLayout();
            this.m_TAB.SuspendLayout();
            this.m_TAB_DISTCC.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer1)).BeginInit();
            this.splitContainer1.Panel1.SuspendLayout();
            this.splitContainer1.Panel2.SuspendLayout();
            this.splitContainer1.SuspendLayout();
            this.m_groupBoxSHosts.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox3)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDown)).BeginInit();
            this.m_groupBoxHosts.SuspendLayout();
            this.m_groupBoxServer.SuspendLayout();
            this.groupBoxGLLS.SuspendLayout();
            this.m_TAB_Sumup.SuspendLayout();
            this.groupBox5.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.groupBox3.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxUseHosts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxServer)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
            this.SuspendLayout();
            // 
            // m_button_Next
            // 
            this.m_button_Next.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.m_button_Next.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.m_button_Next.Location = new System.Drawing.Point(777, 606);
            this.m_button_Next.Name = "m_button_Next";
            this.m_button_Next.Size = new System.Drawing.Size(99, 36);
            this.m_button_Next.TabIndex = 6;
            this.m_button_Next.Text = "Next";
            this.m_button_Next.UseVisualStyleBackColor = true;
            this.m_button_Next.Click += new System.EventHandler(this.Next_Click);
            // 
            // m_button_Prev
            // 
            this.m_button_Prev.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.m_button_Prev.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.m_button_Prev.Location = new System.Drawing.Point(12, 606);
            this.m_button_Prev.Name = "m_button_Prev";
            this.m_button_Prev.Size = new System.Drawing.Size(88, 36);
            this.m_button_Prev.TabIndex = 5;
            this.m_button_Prev.Text = "Close";
            this.m_button_Prev.UseVisualStyleBackColor = true;
            this.m_button_Prev.Click += new System.EventHandler(this.Prev_Click);
            // 
            // menuStrip
            // 
            this.menuStrip.BackColor = System.Drawing.SystemColors.Control;
            this.menuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fileToolStripMenuItem,
            this.helpToolStripMenuItem});
            this.menuStrip.Location = new System.Drawing.Point(0, 0);
            this.menuStrip.Name = "menuStrip";
            this.menuStrip.Size = new System.Drawing.Size(883, 24);
            this.menuStrip.TabIndex = 1;
            this.menuStrip.Text = "menuStrip";
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.exitToolStripMenuItem});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(37, 20);
            this.fileToolStripMenuItem.Text = "File";
            // 
            // exitToolStripMenuItem
            // 
            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            this.exitToolStripMenuItem.Size = new System.Drawing.Size(92, 22);
            this.exitToolStripMenuItem.Text = "Exit";
            this.exitToolStripMenuItem.Click += new System.EventHandler(this.exitToolStripMenuItem_Click);
            // 
            // helpToolStripMenuItem
            // 
            this.helpToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.installerDocsToolStripMenuItem,
            this.aboutToolStripMenuItem});
            this.helpToolStripMenuItem.Name = "helpToolStripMenuItem";
            this.helpToolStripMenuItem.Size = new System.Drawing.Size(44, 20);
            this.helpToolStripMenuItem.Text = "Help";
            // 
            // installerDocsToolStripMenuItem
            // 
            this.installerDocsToolStripMenuItem.Name = "installerDocsToolStripMenuItem";
            this.installerDocsToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.installerDocsToolStripMenuItem.Text = "Installer Docs";
            this.installerDocsToolStripMenuItem.Click += new System.EventHandler(this.installerDocsToolStripMenuItem_Click);
            // 
            // aboutToolStripMenuItem
            // 
            this.aboutToolStripMenuItem.Name = "aboutToolStripMenuItem";
            this.aboutToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.aboutToolStripMenuItem.Text = "About";
            this.aboutToolStripMenuItem.Click += new System.EventHandler(this.AboutToolStripMenuItem_Click);
            // 
            // label2
            // 
            this.label2.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.label2.AutoSize = true;
            this.label2.Font = new System.Drawing.Font("Microsoft Sans Serif", 15.75F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label2.Location = new System.Drawing.Point(775, 77);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(96, 25);
            this.label2.TabIndex = 3;
            this.label2.Text = "Installer";
            // 
            // m_version
            // 
            this.m_version.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.m_version.AutoSize = true;
            this.m_version.Font = new System.Drawing.Font("Microsoft Sans Serif", 14.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.m_version.Location = new System.Drawing.Point(796, 102);
            this.m_version.Name = "m_version";
            this.m_version.Size = new System.Drawing.Size(65, 24);
            this.m_version.TabIndex = 4;
            this.m_version.Text = "vx.x.x";
            // 
            // m_startTimer
            // 
            this.m_startTimer.Interval = 1000;
            this.m_startTimer.Tick += new System.EventHandler(this.StartTimer_Tick);
            // 
            // m_TAB_SVNOperations
            // 
            this.m_TAB_SVNOperations.Controls.Add(this.groupBox1);
            this.m_TAB_SVNOperations.Location = new System.Drawing.Point(4, 22);
            this.m_TAB_SVNOperations.Name = "m_TAB_SVNOperations";
            this.m_TAB_SVNOperations.Padding = new System.Windows.Forms.Padding(3);
            this.m_TAB_SVNOperations.Size = new System.Drawing.Size(875, 423);
            this.m_TAB_SVNOperations.TabIndex = 2;
            this.m_TAB_SVNOperations.Text = "SVN Operations >";
            this.m_TAB_SVNOperations.UseVisualStyleBackColor = true;
            // 
            // groupBox1
            // 
            this.groupBox1.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.groupBox1.Controls.Add(this.pictureBox2);
            this.groupBox1.Controls.Add(this.groupBox2);
            this.groupBox1.Controls.Add(this.label5);
            this.groupBox1.Controls.Add(this.TortoiseSVN);
            this.groupBox1.Controls.Add(this.label3);
            this.groupBox1.Controls.Add(this.m_browse);
            this.groupBox1.Controls.Add(this.m_textBoxPath);
            this.groupBox1.Controls.Add(this.m_RB_Update);
            this.groupBox1.Controls.Add(this.m_RB_CheckOut);
            this.groupBox1.Location = new System.Drawing.Point(8, 57);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(859, 264);
            this.groupBox1.TabIndex = 1;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "SandBox";
            // 
            // pictureBox2
            // 
            this.pictureBox2.Image = ((System.Drawing.Image)(resources.GetObject("pictureBox2.Image")));
            this.pictureBox2.Location = new System.Drawing.Point(524, 210);
            this.pictureBox2.Name = "pictureBox2";
            this.pictureBox2.Size = new System.Drawing.Size(51, 48);
            this.pictureBox2.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBox2.TabIndex = 9;
            this.pictureBox2.TabStop = false;
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.lbReleaseRev);
            this.groupBox2.Controls.Add(this.linkLabelDevelopment);
            this.groupBox2.Controls.Add(this.label7);
            this.groupBox2.Controls.Add(this.label6);
            this.groupBox2.Controls.Add(this.radioButtonDevelopment);
            this.groupBox2.Controls.Add(this.linkLabelRelease);
            this.groupBox2.Controls.Add(this.radioButtonRelease);
            this.groupBox2.Location = new System.Drawing.Point(175, 19);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(671, 133);
            this.groupBox2.TabIndex = 8;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Choose release or development";
            // 
            // lbReleaseRev
            // 
            this.lbReleaseRev.AutoSize = true;
            this.lbReleaseRev.Location = new System.Drawing.Point(287, 49);
            this.lbReleaseRev.Name = "lbReleaseRev";
            this.lbReleaseRev.Size = new System.Drawing.Size(93, 13);
            this.lbReleaseRev.TabIndex = 12;
            this.lbReleaseRev.Text = "Release Revision:";
            // 
            // linkLabelDevelopment
            // 
            this.linkLabelDevelopment.AutoSize = true;
            this.linkLabelDevelopment.Location = new System.Drawing.Point(118, 91);
            this.linkLabelDevelopment.Name = "linkLabelDevelopment";
            this.linkLabelDevelopment.Size = new System.Drawing.Size(64, 13);
            this.linkLabelDevelopment.TabIndex = 11;
            this.linkLabelDevelopment.TabStop = true;
            this.linkLabelDevelopment.Text = "svn address";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(31, 109);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(349, 13);
            this.label7.TabIndex = 10;
            this.label7.Text = "(this is the development, and probably, UNSTABLE GLLegacy SandBox)";
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(31, 49);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(232, 13);
            this.label6.TabIndex = 9;
            this.label6.Text = "(this is the official STABLE GLLegacy SandBox)";
            // 
            // radioButtonDevelopment
            // 
            this.radioButtonDevelopment.AutoSize = true;
            this.radioButtonDevelopment.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.radioButtonDevelopment.ForeColor = System.Drawing.Color.Salmon;
            this.radioButtonDevelopment.Location = new System.Drawing.Point(15, 89);
            this.radioButtonDevelopment.Name = "radioButtonDevelopment";
            this.radioButtonDevelopment.Size = new System.Drawing.Size(86, 17);
            this.radioButtonDevelopment.TabIndex = 1;
            this.radioButtonDevelopment.Text = "development";
            this.radioButtonDevelopment.UseVisualStyleBackColor = true;
            // 
            // linkLabelRelease
            // 
            this.linkLabelRelease.AutoSize = true;
            this.linkLabelRelease.Location = new System.Drawing.Point(118, 31);
            this.linkLabelRelease.Name = "linkLabelRelease";
            this.linkLabelRelease.Size = new System.Drawing.Size(64, 13);
            this.linkLabelRelease.TabIndex = 6;
            this.linkLabelRelease.TabStop = true;
            this.linkLabelRelease.Text = "svn address";
            // 
            // radioButtonRelease
            // 
            this.radioButtonRelease.AutoSize = true;
            this.radioButtonRelease.Checked = true;
            this.radioButtonRelease.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.radioButtonRelease.ForeColor = System.Drawing.Color.Green;
            this.radioButtonRelease.Location = new System.Drawing.Point(15, 29);
            this.radioButtonRelease.Name = "radioButtonRelease";
            this.radioButtonRelease.Size = new System.Drawing.Size(66, 17);
            this.radioButtonRelease.TabIndex = 0;
            this.radioButtonRelease.TabStop = true;
            this.radioButtonRelease.Text = "release";
            this.radioButtonRelease.UseVisualStyleBackColor = true;
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(90, 203);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(282, 13);
            this.label5.TabIndex = 7;
            this.label5.Text = "(this must be a path, from your cmputer, to an empty folder)";
            // 
            // TortoiseSVN
            // 
            this.TortoiseSVN.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.TortoiseSVN.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.TortoiseSVN.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.TortoiseSVN.Image = global::GLLegacyInstaller.Properties.Resources.TortoiseSVN;
            this.TortoiseSVN.ImageAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.TortoiseSVN.Location = new System.Drawing.Point(581, 220);
            this.TortoiseSVN.Name = "TortoiseSVN";
            this.TortoiseSVN.Size = new System.Drawing.Size(265, 27);
            this.TortoiseSVN.TabIndex = 5;
            this.TortoiseSVN.Text = "Process using";
            this.TortoiseSVN.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.TortoiseSVN.UseVisualStyleBackColor = true;
            this.TortoiseSVN.Click += new System.EventHandler(this.TortoiseSVN_Click);
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(26, 183);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(61, 13);
            this.label3.TabIndex = 4;
            this.label3.Text = "Local Path:";
            // 
            // m_browse
            // 
            this.m_browse.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.m_browse.Location = new System.Drawing.Point(821, 177);
            this.m_browse.Name = "m_browse";
            this.m_browse.Size = new System.Drawing.Size(25, 23);
            this.m_browse.TabIndex = 3;
            this.m_browse.Text = "...";
            this.m_browse.UseVisualStyleBackColor = true;
            this.m_browse.Click += new System.EventHandler(this.SVNBrowse_Click);
            // 
            // m_textBoxPath
            // 
            this.m_textBoxPath.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_textBoxPath.Location = new System.Drawing.Point(93, 180);
            this.m_textBoxPath.Name = "m_textBoxPath";
            this.m_textBoxPath.Size = new System.Drawing.Size(722, 20);
            this.m_textBoxPath.TabIndex = 2;
            // 
            // m_RB_Update
            // 
            this.m_RB_Update.AutoSize = true;
            this.m_RB_Update.Enabled = false;
            this.m_RB_Update.Location = new System.Drawing.Point(29, 87);
            this.m_RB_Update.Name = "m_RB_Update";
            this.m_RB_Update.Size = new System.Drawing.Size(60, 17);
            this.m_RB_Update.TabIndex = 1;
            this.m_RB_Update.Text = "Update";
            this.m_RB_Update.UseVisualStyleBackColor = true;
            // 
            // m_RB_CheckOut
            // 
            this.m_RB_CheckOut.AutoSize = true;
            this.m_RB_CheckOut.Checked = true;
            this.m_RB_CheckOut.Location = new System.Drawing.Point(29, 64);
            this.m_RB_CheckOut.Name = "m_RB_CheckOut";
            this.m_RB_CheckOut.Size = new System.Drawing.Size(73, 17);
            this.m_RB_CheckOut.TabIndex = 0;
            this.m_RB_CheckOut.TabStop = true;
            this.m_RB_CheckOut.Text = "CheckOut";
            this.m_RB_CheckOut.UseVisualStyleBackColor = true;
            // 
            // m_TAB_CheckTools
            // 
            this.m_TAB_CheckTools.Controls.Add(this.buttonInstallerSetup);
            this.m_TAB_CheckTools.Controls.Add(this.m_detectingProgress);
            this.m_TAB_CheckTools.Controls.Add(this.m_button_Refresh);
            this.m_TAB_CheckTools.Controls.Add(this.m_gridTools);
            this.m_TAB_CheckTools.Location = new System.Drawing.Point(4, 22);
            this.m_TAB_CheckTools.Name = "m_TAB_CheckTools";
            this.m_TAB_CheckTools.Padding = new System.Windows.Forms.Padding(3);
            this.m_TAB_CheckTools.Size = new System.Drawing.Size(875, 423);
            this.m_TAB_CheckTools.TabIndex = 1;
            this.m_TAB_CheckTools.Text = "CheckTools >";
            this.m_TAB_CheckTools.UseVisualStyleBackColor = true;
            // 
            // buttonInstallerSetup
            // 
            this.buttonInstallerSetup.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.buttonInstallerSetup.Location = new System.Drawing.Point(450, 8);
            this.buttonInstallerSetup.Name = "buttonInstallerSetup";
            this.buttonInstallerSetup.Size = new System.Drawing.Size(126, 23);
            this.buttonInstallerSetup.TabIndex = 12;
            this.buttonInstallerSetup.Text = "Open Config file (XML)";
            this.buttonInstallerSetup.UseVisualStyleBackColor = true;
            this.buttonInstallerSetup.Click += new System.EventHandler(this.buttonInstallerSetup_Click);
            // 
            // m_detectingProgress
            // 
            this.m_detectingProgress.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.m_detectingProgress.Location = new System.Drawing.Point(705, 9);
            this.m_detectingProgress.Name = "m_detectingProgress";
            this.m_detectingProgress.Size = new System.Drawing.Size(162, 22);
            this.m_detectingProgress.TabIndex = 10;
            // 
            // m_button_Refresh
            // 
            this.m_button_Refresh.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.m_button_Refresh.Location = new System.Drawing.Point(582, 8);
            this.m_button_Refresh.Name = "m_button_Refresh";
            this.m_button_Refresh.Size = new System.Drawing.Size(118, 23);
            this.m_button_Refresh.TabIndex = 1;
            this.m_button_Refresh.Text = "Detect Tools Again";
            this.m_button_Refresh.UseVisualStyleBackColor = true;
            this.m_button_Refresh.Click += new System.EventHandler(this.Refresh_Click);
            // 
            // m_gridTools
            // 
            this.m_gridTools.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_gridTools.AutoStretchColumnsToFitWidth = true;
            this.m_gridTools.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.m_gridTools.EnableSort = false;
            this.m_gridTools.Location = new System.Drawing.Point(3, 40);
            this.m_gridTools.Name = "m_gridTools";
            this.m_gridTools.OptimizeMode = SourceGrid.CellOptimizeMode.ForRows;
            this.m_gridTools.SelectionMode = SourceGrid.GridSelectionMode.Row;
            this.m_gridTools.Size = new System.Drawing.Size(872, 383);
            this.m_gridTools.TabIndex = 0;
            this.m_gridTools.TabStop = true;
            this.m_gridTools.ToolTipText = "";
            // 
            // m_TAB_Initialize
            // 
            this.m_TAB_Initialize.Controls.Add(this.label1);
            this.m_TAB_Initialize.Controls.Add(this.m_loadingProgress);
            this.m_TAB_Initialize.Location = new System.Drawing.Point(4, 22);
            this.m_TAB_Initialize.Name = "m_TAB_Initialize";
            this.m_TAB_Initialize.Padding = new System.Windows.Forms.Padding(3);
            this.m_TAB_Initialize.Size = new System.Drawing.Size(875, 423);
            this.m_TAB_Initialize.TabIndex = 0;
            this.m_TAB_Initialize.Text = "Initialize >";
            this.m_TAB_Initialize.UseVisualStyleBackColor = true;
            // 
            // label1
            // 
            this.label1.Anchor = System.Windows.Forms.AnchorStyles.Top;
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(401, 155);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(93, 13);
            this.label1.TabIndex = 1;
            this.label1.Text = "Loading settings...";
            // 
            // m_loadingProgress
            // 
            this.m_loadingProgress.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_loadingProgress.Cursor = System.Windows.Forms.Cursors.Default;
            this.m_loadingProgress.Location = new System.Drawing.Point(219, 181);
            this.m_loadingProgress.Name = "m_loadingProgress";
            this.m_loadingProgress.Size = new System.Drawing.Size(438, 23);
            this.m_loadingProgress.Style = System.Windows.Forms.ProgressBarStyle.Continuous;
            this.m_loadingProgress.TabIndex = 0;
            // 
            // m_TAB
            // 
            this.m_TAB.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_TAB.Controls.Add(this.m_TAB_Initialize);
            this.m_TAB.Controls.Add(this.m_TAB_CheckTools);
            this.m_TAB.Controls.Add(this.m_TAB_DISTCC);
            this.m_TAB.Controls.Add(this.m_TAB_SVNOperations);
            this.m_TAB.Controls.Add(this.m_TAB_Sumup);
            this.m_TAB.Location = new System.Drawing.Point(0, 155);
            this.m_TAB.Name = "m_TAB";
            this.m_TAB.SelectedIndex = 0;
            this.m_TAB.Size = new System.Drawing.Size(883, 449);
            this.m_TAB.TabIndex = 0;
            this.m_TAB.Selecting += new System.Windows.Forms.TabControlCancelEventHandler(this.m_TAB_Selecting);
            // 
            // m_TAB_DISTCC
            // 
            this.m_TAB_DISTCC.Controls.Add(this.splitContainer1);
            this.m_TAB_DISTCC.Controls.Add(this.groupBoxGLLS);
            this.m_TAB_DISTCC.Location = new System.Drawing.Point(4, 22);
            this.m_TAB_DISTCC.Name = "m_TAB_DISTCC";
            this.m_TAB_DISTCC.Size = new System.Drawing.Size(875, 423);
            this.m_TAB_DISTCC.TabIndex = 3;
            this.m_TAB_DISTCC.Text = "Set DISTCC  >";
            this.m_TAB_DISTCC.UseVisualStyleBackColor = true;
            // 
            // splitContainer1
            // 
            this.splitContainer1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.splitContainer1.Location = new System.Drawing.Point(-4, 45);
            this.splitContainer1.Name = "splitContainer1";
            // 
            // splitContainer1.Panel1
            // 
            this.splitContainer1.Panel1.BackColor = System.Drawing.Color.White;
            this.splitContainer1.Panel1.Controls.Add(this.m_groupBoxSHosts);
            this.splitContainer1.Panel1.Controls.Add(this.checkShowServers);
            this.splitContainer1.Panel1.Controls.Add(this.buttonAddIP);
            this.splitContainer1.Panel1.Controls.Add(this.ipAddressControl);
            this.splitContainer1.Panel1.Controls.Add(this.buttonRescan);
            this.splitContainer1.Panel1.Controls.Add(this.pictureBox3);
            this.splitContainer1.Panel1.Controls.Add(this.numericUpDown);
            this.splitContainer1.Panel1.Controls.Add(this.comboBoxLetterDrive);
            this.splitContainer1.Panel1.Controls.Add(this.labelVirtualDrive);
            this.splitContainer1.Panel1.Controls.Add(this.labelDiscoverTime);
            this.splitContainer1.Panel1.Controls.Add(this.m_checkBoxDISTCCUse);
            this.splitContainer1.Panel1.Controls.Add(this.m_groupBoxHosts);
            // 
            // splitContainer1.Panel2
            // 
            this.splitContainer1.Panel2.BackColor = System.Drawing.Color.White;
            this.splitContainer1.Panel2.Controls.Add(this.m_groupBoxServer);
            this.splitContainer1.Panel2.Controls.Add(this.m_checkBoxDISTCCServer);
            this.splitContainer1.Size = new System.Drawing.Size(871, 375);
            this.splitContainer1.SplitterDistance = 594;
            this.splitContainer1.TabIndex = 5;
            // 
            // m_groupBoxSHosts
            // 
            this.m_groupBoxSHosts.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.m_groupBoxSHosts.Controls.Add(this.m_gridIPs);
            this.m_groupBoxSHosts.Enabled = false;
            this.m_groupBoxSHosts.Location = new System.Drawing.Point(12, 249);
            this.m_groupBoxSHosts.MaximumSize = new System.Drawing.Size(400, 122);
            this.m_groupBoxSHosts.Name = "m_groupBoxSHosts";
            this.m_groupBoxSHosts.Size = new System.Drawing.Size(400, 122);
            this.m_groupBoxSHosts.TabIndex = 29;
            this.m_groupBoxSHosts.TabStop = false;
            this.m_groupBoxSHosts.Text = "DISTCC Static Hosts";
            // 
            // m_gridIPs
            // 
            this.m_gridIPs.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_gridIPs.BackgroundImageLayout = System.Windows.Forms.ImageLayout.None;
            this.m_gridIPs.EnableSort = true;
            this.m_gridIPs.Location = new System.Drawing.Point(3, 18);
            this.m_gridIPs.Name = "m_gridIPs";
            this.m_gridIPs.OptimizeMode = SourceGrid.CellOptimizeMode.ForRows;
            this.m_gridIPs.SelectionMode = SourceGrid.GridSelectionMode.Cell;
            this.m_gridIPs.Size = new System.Drawing.Size(391, 98);
            this.m_gridIPs.TabIndex = 28;
            this.m_gridIPs.TabStop = true;
            this.m_gridIPs.ToolTipText = "";
            // 
            // checkShowServers
            // 
            this.checkShowServers.AccessibleDescription = "";
            this.checkShowServers.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.checkShowServers.AutoSize = true;
            this.checkShowServers.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.checkShowServers.Location = new System.Drawing.Point(423, 61);
            this.checkShowServers.Name = "checkShowServers";
            this.checkShowServers.Size = new System.Drawing.Size(129, 17);
            this.checkShowServers.TabIndex = 5;
            this.checkShowServers.Text = "Show only servers";
            this.toolTip1.SetToolTip(this.checkShowServers, "DISTCC means distributed compiling. This feature is used to compile faster the An" +
        "droid builds.\r\nSome of the files will be distributed to other computers to be co" +
        "mpiled!");
            this.checkShowServers.UseVisualStyleBackColor = true;
            this.checkShowServers.CheckedChanged += new System.EventHandler(this.checkShowServers_CheckedChanged);
            // 
            // buttonAddIP
            // 
            this.buttonAddIP.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.buttonAddIP.Location = new System.Drawing.Point(420, 304);
            this.buttonAddIP.Name = "buttonAddIP";
            this.buttonAddIP.Size = new System.Drawing.Size(58, 23);
            this.buttonAddIP.TabIndex = 26;
            this.buttonAddIP.Text = "Add IP";
            this.buttonAddIP.UseVisualStyleBackColor = true;
            this.buttonAddIP.Click += new System.EventHandler(this.buttonIPs_Click);
            // 
            // ipAddressControl
            // 
            this.ipAddressControl.AllowInternalTab = false;
            this.ipAddressControl.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.ipAddressControl.AutoHeight = true;
            this.ipAddressControl.BackColor = System.Drawing.SystemColors.Window;
            this.ipAddressControl.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.ipAddressControl.Cursor = System.Windows.Forms.Cursors.IBeam;
            this.ipAddressControl.ForeColor = System.Drawing.SystemColors.ControlText;
            this.ipAddressControl.Location = new System.Drawing.Point(420, 278);
            this.ipAddressControl.MinimumSize = new System.Drawing.Size(87, 20);
            this.ipAddressControl.Name = "ipAddressControl";
            this.ipAddressControl.ReadOnly = false;
            this.ipAddressControl.Size = new System.Drawing.Size(137, 20);
            this.ipAddressControl.TabIndex = 27;
            this.ipAddressControl.Text = "...";
            this.toolTip1.SetToolTip(this.ipAddressControl, "Add the IP of a host you want tu use.");
            // 
            // buttonRescan
            // 
            this.buttonRescan.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.buttonRescan.Location = new System.Drawing.Point(420, 208);
            this.buttonRescan.Name = "buttonRescan";
            this.buttonRescan.Size = new System.Drawing.Size(58, 23);
            this.buttonRescan.TabIndex = 20;
            this.buttonRescan.Text = "Rescan";
            this.buttonRescan.UseVisualStyleBackColor = true;
            this.buttonRescan.Click += new System.EventHandler(this.buttonRescan_Click);
            // 
            // pictureBox3
            // 
            this.pictureBox3.Image = global::GLLegacyInstaller.Properties.Resources.iconBusy;
            this.pictureBox3.Location = new System.Drawing.Point(15, 0);
            this.pictureBox3.Name = "pictureBox3";
            this.pictureBox3.Size = new System.Drawing.Size(35, 31);
            this.pictureBox3.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pictureBox3.TabIndex = 10;
            this.pictureBox3.TabStop = false;
            this.pictureBox3.Visible = false;
            // 
            // numericUpDown
            // 
            this.numericUpDown.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.numericUpDown.Location = new System.Drawing.Point(499, 164);
            this.numericUpDown.Maximum = new decimal(new int[] {
            120,
            0,
            0,
            0});
            this.numericUpDown.Minimum = new decimal(new int[] {
            1,
            0,
            0,
            0});
            this.numericUpDown.Name = "numericUpDown";
            this.numericUpDown.Size = new System.Drawing.Size(44, 20);
            this.numericUpDown.TabIndex = 16;
            this.toolTip1.SetToolTip(this.numericUpDown, "Time in minutes");
            this.numericUpDown.Value = new decimal(new int[] {
            5,
            0,
            0,
            0});
            // 
            // comboBoxLetterDrive
            // 
            this.comboBoxLetterDrive.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.comboBoxLetterDrive.Enabled = false;
            this.comboBoxLetterDrive.FormattingEnabled = true;
            this.comboBoxLetterDrive.Location = new System.Drawing.Point(423, 131);
            this.comboBoxLetterDrive.Name = "comboBoxLetterDrive";
            this.comboBoxLetterDrive.Size = new System.Drawing.Size(157, 21);
            this.comboBoxLetterDrive.TabIndex = 0;
            // 
            // labelVirtualDrive
            // 
            this.labelVirtualDrive.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.labelVirtualDrive.AutoSize = true;
            this.labelVirtualDrive.Enabled = false;
            this.labelVirtualDrive.Location = new System.Drawing.Point(420, 104);
            this.labelVirtualDrive.Name = "labelVirtualDrive";
            this.labelVirtualDrive.Size = new System.Drawing.Size(97, 13);
            this.labelVirtualDrive.TabIndex = 1;
            this.labelVirtualDrive.Text = "Virtual Letter Drive:";
            // 
            // labelDiscoverTime
            // 
            this.labelDiscoverTime.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.labelDiscoverTime.AutoSize = true;
            this.labelDiscoverTime.Location = new System.Drawing.Point(423, 167);
            this.labelDiscoverTime.Name = "labelDiscoverTime";
            this.labelDiscoverTime.Size = new System.Drawing.Size(75, 13);
            this.labelDiscoverTime.TabIndex = 17;
            this.labelDiscoverTime.Text = "DiscoverTime:";
            // 
            // m_checkBoxDISTCCUse
            // 
            this.m_checkBoxDISTCCUse.AccessibleDescription = "";
            this.m_checkBoxDISTCCUse.AutoSize = true;
            this.m_checkBoxDISTCCUse.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.m_checkBoxDISTCCUse.Location = new System.Drawing.Point(123, 13);
            this.m_checkBoxDISTCCUse.Name = "m_checkBoxDISTCCUse";
            this.m_checkBoxDISTCCUse.Size = new System.Drawing.Size(173, 17);
            this.m_checkBoxDISTCCUse.TabIndex = 3;
            this.m_checkBoxDISTCCUse.Text = "Use DISTCC Online Hosts";
            this.toolTip1.SetToolTip(this.m_checkBoxDISTCCUse, "DISTCC means distributed compiling. This feature is used to compile faster the An" +
        "droid builds.\r\nSome of the files will be distributed to other computers to be co" +
        "mpiled!");
            this.m_checkBoxDISTCCUse.UseVisualStyleBackColor = true;
            this.m_checkBoxDISTCCUse.CheckedChanged += new System.EventHandler(this.CheckBoxDISTCCUse_CheckedChanged);
            // 
            // m_groupBoxHosts
            // 
            this.m_groupBoxHosts.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_groupBoxHosts.Controls.Add(this.m_gridAvailableHosts);
            this.m_groupBoxHosts.Enabled = false;
            this.m_groupBoxHosts.Location = new System.Drawing.Point(7, 43);
            this.m_groupBoxHosts.Name = "m_groupBoxHosts";
            this.m_groupBoxHosts.Size = new System.Drawing.Size(405, 200);
            this.m_groupBoxHosts.TabIndex = 1;
            this.m_groupBoxHosts.TabStop = false;
            this.m_groupBoxHosts.Text = "DISTCC Hosts list";
            // 
            // m_gridAvailableHosts
            // 
            this.m_gridAvailableHosts.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_gridAvailableHosts.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.m_gridAvailableHosts.EnableSort = true;
            this.m_gridAvailableHosts.ImeMode = System.Windows.Forms.ImeMode.Alpha;
            this.m_gridAvailableHosts.Location = new System.Drawing.Point(6, 18);
            this.m_gridAvailableHosts.Name = "m_gridAvailableHosts";
            this.m_gridAvailableHosts.OptimizeMode = SourceGrid.CellOptimizeMode.ForRows;
            this.m_gridAvailableHosts.SelectionMode = SourceGrid.GridSelectionMode.Cell;
            this.m_gridAvailableHosts.Size = new System.Drawing.Size(393, 176);
            this.m_gridAvailableHosts.TabIndex = 0;
            this.m_gridAvailableHosts.TabStop = true;
            this.m_gridAvailableHosts.ToolTipText = "";
            // 
            // m_groupBoxServer
            // 
            this.m_groupBoxServer.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.m_groupBoxServer.Controls.Add(this.textBoxJOBS);
            this.m_groupBoxServer.Controls.Add(this.labelCORES);
            this.m_groupBoxServer.Controls.Add(this.label17);
            this.m_groupBoxServer.Controls.Add(this.label16);
            this.m_groupBoxServer.Controls.Add(this.label11);
            this.m_groupBoxServer.Controls.Add(this.labelDISTCCStatus);
            this.m_groupBoxServer.Controls.Add(this.label14);
            this.m_groupBoxServer.Enabled = false;
            this.m_groupBoxServer.Location = new System.Drawing.Point(13, 43);
            this.m_groupBoxServer.Name = "m_groupBoxServer";
            this.m_groupBoxServer.Size = new System.Drawing.Size(252, 284);
            this.m_groupBoxServer.TabIndex = 2;
            this.m_groupBoxServer.TabStop = false;
            this.m_groupBoxServer.Text = "DISTCC Host settings";
            // 
            // textBoxJOBS
            // 
            this.textBoxJOBS.Location = new System.Drawing.Point(149, 120);
            this.textBoxJOBS.Name = "textBoxJOBS";
            this.textBoxJOBS.Size = new System.Drawing.Size(44, 20);
            this.textBoxJOBS.TabIndex = 15;
            this.textBoxJOBS.TextChanged += new System.EventHandler(this.textBoxJOBS_TextChanged);
            // 
            // labelCORES
            // 
            this.labelCORES.AutoSize = true;
            this.labelCORES.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelCORES.Location = new System.Drawing.Point(146, 165);
            this.labelCORES.Name = "labelCORES";
            this.labelCORES.Size = new System.Drawing.Size(46, 13);
            this.labelCORES.TabIndex = 14;
            this.labelCORES.Text = "not set";
            // 
            // label17
            // 
            this.label17.AutoSize = true;
            this.label17.Location = new System.Drawing.Point(55, 165);
            this.label17.Name = "label17";
            this.label17.Size = new System.Drawing.Size(62, 13);
            this.label17.TabIndex = 13;
            this.label17.Text = "CPU Cores:";
            // 
            // label16
            // 
            this.label16.AutoSize = true;
            this.label16.Location = new System.Drawing.Point(9, 123);
            this.label16.Name = "label16";
            this.label16.Size = new System.Drawing.Size(123, 13);
            this.label16.TabIndex = 11;
            this.label16.Text = "distccd.exe Server jobs: ";
            // 
            // label11
            // 
            this.label11.AutoSize = true;
            this.label11.Location = new System.Drawing.Point(67, 202);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(125, 13);
            this.label11.TabIndex = 17;
            this.label11.Text = "(0=default, Cores*2=max)";
            // 
            // labelDISTCCStatus
            // 
            this.labelDISTCCStatus.AutoSize = true;
            this.labelDISTCCStatus.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelDISTCCStatus.Location = new System.Drawing.Point(146, 50);
            this.labelDISTCCStatus.Name = "labelDISTCCStatus";
            this.labelDISTCCStatus.Size = new System.Drawing.Size(46, 13);
            this.labelDISTCCStatus.TabIndex = 7;
            this.labelDISTCCStatus.Text = "not set";
            // 
            // label14
            // 
            this.label14.AutoSize = true;
            this.label14.Location = new System.Drawing.Point(9, 50);
            this.label14.Name = "label14";
            this.label14.Size = new System.Drawing.Size(137, 13);
            this.label14.TabIndex = 6;
            this.label14.Text = "distccd.exe Server  Status :";
            // 
            // m_checkBoxDISTCCServer
            // 
            this.m_checkBoxDISTCCServer.AutoSize = true;
            this.m_checkBoxDISTCCServer.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.m_checkBoxDISTCCServer.Location = new System.Drawing.Point(25, 13);
            this.m_checkBoxDISTCCServer.Name = "m_checkBoxDISTCCServer";
            this.m_checkBoxDISTCCServer.Size = new System.Drawing.Size(216, 17);
            this.m_checkBoxDISTCCServer.TabIndex = 4;
            this.m_checkBoxDISTCCServer.Text = "Enable DISTCC Host (aka server)";
            this.toolTip1.SetToolTip(this.m_checkBoxDISTCCServer, resources.GetString("m_checkBoxDISTCCServer.ToolTip"));
            this.m_checkBoxDISTCCServer.UseVisualStyleBackColor = true;
            this.m_checkBoxDISTCCServer.CheckedChanged += new System.EventHandler(this.CheckBoxDISTCCServer_CheckedChanged);
            // 
            // groupBoxGLLS
            // 
            this.groupBoxGLLS.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.groupBoxGLLS.Controls.Add(this.comboBoxNetAddr);
            this.groupBoxGLLS.Controls.Add(this.buttonOpenLogFile);
            this.groupBoxGLLS.Controls.Add(this.buttonReinstallService);
            this.groupBoxGLLS.Controls.Add(this.buttonRestartService);
            this.groupBoxGLLS.Controls.Add(this.labelVersion);
            this.groupBoxGLLS.Controls.Add(this.label4);
            this.groupBoxGLLS.Controls.Add(this.labelServiceStatus);
            this.groupBoxGLLS.Controls.Add(this.label12);
            this.groupBoxGLLS.Controls.Add(this.label13);
            this.groupBoxGLLS.Location = new System.Drawing.Point(9, 5);
            this.groupBoxGLLS.Name = "groupBoxGLLS";
            this.groupBoxGLLS.Size = new System.Drawing.Size(858, 38);
            this.groupBoxGLLS.TabIndex = 30;
            this.groupBoxGLLS.TabStop = false;
            this.groupBoxGLLS.Text = "GLLegacyService";
            // 
            // comboBoxNetAddr
            // 
            this.comboBoxNetAddr.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.comboBoxNetAddr.FormattingEnabled = true;
            this.comboBoxNetAddr.Location = new System.Drawing.Point(662, 11);
            this.comboBoxNetAddr.Name = "comboBoxNetAddr";
            this.comboBoxNetAddr.Size = new System.Drawing.Size(136, 21);
            this.comboBoxNetAddr.TabIndex = 22;
            this.comboBoxNetAddr.SelectedIndexChanged += new System.EventHandler(this.comboBoxNetAddr_SelectedIndexChanged);
            // 
            // buttonOpenLogFile
            // 
            this.buttonOpenLogFile.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.buttonOpenLogFile.Location = new System.Drawing.Point(471, 11);
            this.buttonOpenLogFile.MaximumSize = new System.Drawing.Size(94, 23);
            this.buttonOpenLogFile.Name = "buttonOpenLogFile";
            this.buttonOpenLogFile.Size = new System.Drawing.Size(94, 23);
            this.buttonOpenLogFile.TabIndex = 21;
            this.buttonOpenLogFile.Text = "Open Log File";
            this.buttonOpenLogFile.UseVisualStyleBackColor = true;
            this.buttonOpenLogFile.Click += new System.EventHandler(this.buttonOpenLogFile_Click);
            // 
            // buttonReinstallService
            // 
            this.buttonReinstallService.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.buttonReinstallService.Location = new System.Drawing.Point(379, 11);
            this.buttonReinstallService.MaximumSize = new System.Drawing.Size(75, 23);
            this.buttonReinstallService.Name = "buttonReinstallService";
            this.buttonReinstallService.Size = new System.Drawing.Size(75, 23);
            this.buttonReinstallService.TabIndex = 20;
            this.buttonReinstallService.Text = "Reinstall";
            this.buttonReinstallService.UseVisualStyleBackColor = true;
            this.buttonReinstallService.Click += new System.EventHandler(this.buttonReinstallService_Click);
            // 
            // buttonRestartService
            // 
            this.buttonRestartService.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.buttonRestartService.Location = new System.Drawing.Point(283, 11);
            this.buttonRestartService.MaximumSize = new System.Drawing.Size(75, 23);
            this.buttonRestartService.Name = "buttonRestartService";
            this.buttonRestartService.Size = new System.Drawing.Size(75, 23);
            this.buttonRestartService.TabIndex = 16;
            this.buttonRestartService.Text = "Restart";
            this.buttonRestartService.UseVisualStyleBackColor = true;
            this.buttonRestartService.Click += new System.EventHandler(this.ButtonRestartService_Click);
            // 
            // labelVersion
            // 
            this.labelVersion.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.labelVersion.AutoSize = true;
            this.labelVersion.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelVersion.Location = new System.Drawing.Point(214, 16);
            this.labelVersion.Name = "labelVersion";
            this.labelVersion.Size = new System.Drawing.Size(46, 13);
            this.labelVersion.TabIndex = 19;
            this.labelVersion.Text = "not set";
            // 
            // label4
            // 
            this.label4.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(164, 16);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(48, 13);
            this.label4.TabIndex = 18;
            this.label4.Text = "Version :";
            // 
            // labelServiceStatus
            // 
            this.labelServiceStatus.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.labelServiceStatus.AutoSize = true;
            this.labelServiceStatus.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelServiceStatus.Location = new System.Drawing.Point(80, 16);
            this.labelServiceStatus.Name = "labelServiceStatus";
            this.labelServiceStatus.Size = new System.Drawing.Size(46, 13);
            this.labelServiceStatus.TabIndex = 5;
            this.labelServiceStatus.Text = "not set";
            // 
            // label12
            // 
            this.label12.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.label12.AutoSize = true;
            this.label12.Location = new System.Drawing.Point(589, 16);
            this.label12.Name = "label12";
            this.label12.Size = new System.Drawing.Size(73, 13);
            this.label12.TabIndex = 3;
            this.label12.Text = "Net address : ";
            // 
            // label13
            // 
            this.label13.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.label13.AutoSize = true;
            this.label13.Location = new System.Drawing.Point(38, 16);
            this.label13.Name = "label13";
            this.label13.Size = new System.Drawing.Size(46, 13);
            this.label13.TabIndex = 4;
            this.label13.Text = " Status :";
            // 
            // m_TAB_Sumup
            // 
            this.m_TAB_Sumup.Controls.Add(this.groupBox5);
            this.m_TAB_Sumup.Controls.Add(this.groupBox4);
            this.m_TAB_Sumup.Controls.Add(this.groupBox3);
            this.m_TAB_Sumup.Controls.Add(this.labelGLLegacyDistccHostsPath);
            this.m_TAB_Sumup.Controls.Add(this.labelGLLegacyDistccEnvPath);
            this.m_TAB_Sumup.Controls.Add(this.labelGLLegacyInstallerEnvPath);
            this.m_TAB_Sumup.Controls.Add(this.buttonDistccHosts);
            this.m_TAB_Sumup.Controls.Add(this.buttonDistccEnv);
            this.m_TAB_Sumup.Controls.Add(this.buttonEnv);
            this.m_TAB_Sumup.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.m_TAB_Sumup.Location = new System.Drawing.Point(4, 22);
            this.m_TAB_Sumup.Name = "m_TAB_Sumup";
            this.m_TAB_Sumup.Padding = new System.Windows.Forms.Padding(3);
            this.m_TAB_Sumup.Size = new System.Drawing.Size(875, 423);
            this.m_TAB_Sumup.TabIndex = 4;
            this.m_TAB_Sumup.Text = "Sum Up";
            this.m_TAB_Sumup.UseVisualStyleBackColor = true;
            // 
            // groupBox5
            // 
            this.groupBox5.Controls.Add(this.labelGLLegacyServiceStatus1);
            this.groupBox5.Controls.Add(this.labelGLLegacyServiceStatus);
            this.groupBox5.Location = new System.Drawing.Point(438, 6);
            this.groupBox5.Name = "groupBox5";
            this.groupBox5.Size = new System.Drawing.Size(397, 81);
            this.groupBox5.TabIndex = 31;
            this.groupBox5.TabStop = false;
            this.groupBox5.Text = "GLLegacyService";
            // 
            // labelGLLegacyServiceStatus1
            // 
            this.labelGLLegacyServiceStatus1.AutoSize = true;
            this.labelGLLegacyServiceStatus1.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelGLLegacyServiceStatus1.Location = new System.Drawing.Point(95, 40);
            this.labelGLLegacyServiceStatus1.Name = "labelGLLegacyServiceStatus1";
            this.labelGLLegacyServiceStatus1.Size = new System.Drawing.Size(60, 20);
            this.labelGLLegacyServiceStatus1.TabIndex = 23;
            this.labelGLLegacyServiceStatus1.Text = "Status:";
            // 
            // labelGLLegacyServiceStatus
            // 
            this.labelGLLegacyServiceStatus.AutoSize = true;
            this.labelGLLegacyServiceStatus.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelGLLegacyServiceStatus.Location = new System.Drawing.Point(201, 40);
            this.labelGLLegacyServiceStatus.Name = "labelGLLegacyServiceStatus";
            this.labelGLLegacyServiceStatus.Size = new System.Drawing.Size(62, 20);
            this.labelGLLegacyServiceStatus.TabIndex = 24;
            this.labelGLLegacyServiceStatus.Text = "Status";
            // 
            // groupBox4
            // 
            this.groupBox4.Controls.Add(this.label10);
            this.groupBox4.Controls.Add(this.labelTotalTools);
            this.groupBox4.Controls.Add(this.labelInvalidTools);
            this.groupBox4.Controls.Add(this.label18);
            this.groupBox4.Location = new System.Drawing.Point(29, 6);
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size(375, 81);
            this.groupBox4.TabIndex = 30;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "Tools";
            // 
            // label10
            // 
            this.label10.AutoSize = true;
            this.label10.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label10.ForeColor = System.Drawing.Color.MediumSeaGreen;
            this.label10.Location = new System.Drawing.Point(37, 22);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(176, 20);
            this.label10.TabIndex = 13;
            this.label10.Text = "Total Tools Installed:";
            // 
            // labelTotalTools
            // 
            this.labelTotalTools.AutoSize = true;
            this.labelTotalTools.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelTotalTools.Location = new System.Drawing.Point(237, 22);
            this.labelTotalTools.Name = "labelTotalTools";
            this.labelTotalTools.Size = new System.Drawing.Size(97, 20);
            this.labelTotalTools.TabIndex = 14;
            this.labelTotalTools.Text = "Total Tools";
            // 
            // labelInvalidTools
            // 
            this.labelInvalidTools.AutoSize = true;
            this.labelInvalidTools.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelInvalidTools.Location = new System.Drawing.Point(237, 52);
            this.labelInvalidTools.Name = "labelInvalidTools";
            this.labelInvalidTools.Size = new System.Drawing.Size(111, 20);
            this.labelInvalidTools.TabIndex = 16;
            this.labelInvalidTools.Text = "Not Installed";
            // 
            // label18
            // 
            this.label18.AutoSize = true;
            this.label18.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label18.ForeColor = System.Drawing.Color.Coral;
            this.label18.Location = new System.Drawing.Point(37, 52);
            this.label18.Name = "label18";
            this.label18.Size = new System.Drawing.Size(116, 20);
            this.label18.TabIndex = 15;
            this.label18.Text = "Not Installed:";
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.labelSHostsNumber1);
            this.groupBox3.Controls.Add(this.labelSHostsNumber);
            this.groupBox3.Controls.Add(this.labelDiscoverTimeS);
            this.groupBox3.Controls.Add(this.labelDiscoverTime1);
            this.groupBox3.Controls.Add(this.label15);
            this.groupBox3.Controls.Add(this.labelDistccdStatus);
            this.groupBox3.Controls.Add(this.labelDriveSelected);
            this.groupBox3.Controls.Add(this.labelDistccdStatus1);
            this.groupBox3.Controls.Add(this.label19);
            this.groupBox3.Controls.Add(this.labelDriveSelected1);
            this.groupBox3.Controls.Add(this.pictureBoxUseHosts);
            this.groupBox3.Controls.Add(this.pictureBoxServer);
            this.groupBox3.Controls.Add(this.labelHostsNumber1);
            this.groupBox3.Controls.Add(this.labelHostsNumber);
            this.groupBox3.Location = new System.Drawing.Point(29, 93);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(806, 172);
            this.groupBox3.TabIndex = 29;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "Distcc";
            // 
            // labelSHostsNumber1
            // 
            this.labelSHostsNumber1.AutoSize = true;
            this.labelSHostsNumber1.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F);
            this.labelSHostsNumber1.Location = new System.Drawing.Point(48, 123);
            this.labelSHostsNumber1.Name = "labelSHostsNumber1";
            this.labelSHostsNumber1.Size = new System.Drawing.Size(157, 17);
            this.labelSHostsNumber1.TabIndex = 31;
            this.labelSHostsNumber1.Text = "Number of Static Hosts:";
            // 
            // labelSHostsNumber
            // 
            this.labelSHostsNumber.AutoSize = true;
            this.labelSHostsNumber.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold);
            this.labelSHostsNumber.Location = new System.Drawing.Point(241, 123);
            this.labelSHostsNumber.Name = "labelSHostsNumber";
            this.labelSHostsNumber.Size = new System.Drawing.Size(156, 17);
            this.labelSHostsNumber.TabIndex = 32;
            this.labelSHostsNumber.Text = "Static Hosts Number";
            // 
            // labelDiscoverTimeS
            // 
            this.labelDiscoverTimeS.AutoSize = true;
            this.labelDiscoverTimeS.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold);
            this.labelDiscoverTimeS.Location = new System.Drawing.Point(241, 140);
            this.labelDiscoverTimeS.Name = "labelDiscoverTimeS";
            this.labelDiscoverTimeS.Size = new System.Drawing.Size(94, 17);
            this.labelDiscoverTimeS.TabIndex = 30;
            this.labelDiscoverTimeS.Text = "Drive Letter";
            // 
            // labelDiscoverTime1
            // 
            this.labelDiscoverTime1.AutoSize = true;
            this.labelDiscoverTime1.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F);
            this.labelDiscoverTime1.Location = new System.Drawing.Point(48, 140);
            this.labelDiscoverTime1.Name = "labelDiscoverTime1";
            this.labelDiscoverTime1.Size = new System.Drawing.Size(142, 17);
            this.labelDiscoverTime1.TabIndex = 29;
            this.labelDiscoverTime1.Text = "Discover Time (min.):";
            // 
            // label15
            // 
            this.label15.AutoSize = true;
            this.label15.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label15.Location = new System.Drawing.Point(47, 35);
            this.label15.Name = "label15";
            this.label15.Size = new System.Drawing.Size(200, 20);
            this.label15.TabIndex = 17;
            this.label15.Text = "Use DISTCC Online Hosts:";
            // 
            // labelDistccdStatus
            // 
            this.labelDistccdStatus.AutoSize = true;
            this.labelDistccdStatus.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold);
            this.labelDistccdStatus.Location = new System.Drawing.Point(678, 88);
            this.labelDistccdStatus.Name = "labelDistccdStatus";
            this.labelDistccdStatus.Size = new System.Drawing.Size(54, 17);
            this.labelDistccdStatus.TabIndex = 26;
            this.labelDistccdStatus.Text = "Status";
            // 
            // labelDriveSelected
            // 
            this.labelDriveSelected.AutoSize = true;
            this.labelDriveSelected.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold);
            this.labelDriveSelected.Location = new System.Drawing.Point(241, 88);
            this.labelDriveSelected.Name = "labelDriveSelected";
            this.labelDriveSelected.Size = new System.Drawing.Size(94, 17);
            this.labelDriveSelected.TabIndex = 28;
            this.labelDriveSelected.Text = "Drive Letter";
            // 
            // labelDistccdStatus1
            // 
            this.labelDistccdStatus1.AutoSize = true;
            this.labelDistccdStatus1.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F);
            this.labelDistccdStatus1.Location = new System.Drawing.Point(453, 88);
            this.labelDistccdStatus1.Name = "labelDistccdStatus1";
            this.labelDistccdStatus1.Size = new System.Drawing.Size(100, 17);
            this.labelDistccdStatus1.TabIndex = 25;
            this.labelDistccdStatus1.Text = "Distccd status:";
            // 
            // label19
            // 
            this.label19.AutoSize = true;
            this.label19.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label19.Location = new System.Drawing.Point(451, 35);
            this.label19.Name = "label19";
            this.label19.Size = new System.Drawing.Size(251, 20);
            this.label19.TabIndex = 18;
            this.label19.Text = "Enable DISTCC Host (aka server):";
            // 
            // labelDriveSelected1
            // 
            this.labelDriveSelected1.AutoSize = true;
            this.labelDriveSelected1.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F);
            this.labelDriveSelected1.Location = new System.Drawing.Point(48, 88);
            this.labelDriveSelected1.Name = "labelDriveSelected1";
            this.labelDriveSelected1.Size = new System.Drawing.Size(104, 17);
            this.labelDriveSelected1.TabIndex = 27;
            this.labelDriveSelected1.Text = "Drive Selected:";
            // 
            // pictureBoxUseHosts
            // 
            this.pictureBoxUseHosts.Image = global::GLLegacyInstaller.Properties.Resources.check;
            this.pictureBoxUseHosts.Location = new System.Drawing.Point(253, 22);
            this.pictureBoxUseHosts.Name = "pictureBoxUseHosts";
            this.pictureBoxUseHosts.Size = new System.Drawing.Size(51, 48);
            this.pictureBoxUseHosts.TabIndex = 19;
            this.pictureBoxUseHosts.TabStop = false;
            // 
            // pictureBoxServer
            // 
            this.pictureBoxServer.Image = global::GLLegacyInstaller.Properties.Resources.check;
            this.pictureBoxServer.Location = new System.Drawing.Point(708, 23);
            this.pictureBoxServer.Name = "pictureBoxServer";
            this.pictureBoxServer.Size = new System.Drawing.Size(51, 48);
            this.pictureBoxServer.TabIndex = 20;
            this.pictureBoxServer.TabStop = false;
            // 
            // labelHostsNumber1
            // 
            this.labelHostsNumber1.AutoSize = true;
            this.labelHostsNumber1.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F);
            this.labelHostsNumber1.Location = new System.Drawing.Point(48, 106);
            this.labelHostsNumber1.Name = "labelHostsNumber1";
            this.labelHostsNumber1.Size = new System.Drawing.Size(177, 17);
            this.labelHostsNumber1.TabIndex = 21;
            this.labelHostsNumber1.Text = "Number of Selected Hosts:";
            // 
            // labelHostsNumber
            // 
            this.labelHostsNumber.AutoSize = true;
            this.labelHostsNumber.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold);
            this.labelHostsNumber.Location = new System.Drawing.Point(241, 106);
            this.labelHostsNumber.Name = "labelHostsNumber";
            this.labelHostsNumber.Size = new System.Drawing.Size(110, 17);
            this.labelHostsNumber.TabIndex = 22;
            this.labelHostsNumber.Text = "Hosts Number";
            // 
            // labelGLLegacyDistccHostsPath
            // 
            this.labelGLLegacyDistccHostsPath.AutoSize = true;
            this.labelGLLegacyDistccHostsPath.Location = new System.Drawing.Point(202, 382);
            this.labelGLLegacyDistccHostsPath.Name = "labelGLLegacyDistccHostsPath";
            this.labelGLLegacyDistccHostsPath.Size = new System.Drawing.Size(231, 20);
            this.labelGLLegacyDistccHostsPath.TabIndex = 12;
            this.labelGLLegacyDistccHostsPath.Text = "GLLegacyDistccHosts.bat path";
            // 
            // labelGLLegacyDistccEnvPath
            // 
            this.labelGLLegacyDistccEnvPath.AutoSize = true;
            this.labelGLLegacyDistccEnvPath.Location = new System.Drawing.Point(202, 335);
            this.labelGLLegacyDistccEnvPath.Name = "labelGLLegacyDistccEnvPath";
            this.labelGLLegacyDistccEnvPath.Size = new System.Drawing.Size(216, 20);
            this.labelGLLegacyDistccEnvPath.TabIndex = 11;
            this.labelGLLegacyDistccEnvPath.Text = "GLLegacyDistccEnv.bat path";
            // 
            // labelGLLegacyInstallerEnvPath
            // 
            this.labelGLLegacyInstallerEnvPath.AutoSize = true;
            this.labelGLLegacyInstallerEnvPath.Location = new System.Drawing.Point(202, 288);
            this.labelGLLegacyInstallerEnvPath.Name = "labelGLLegacyInstallerEnvPath";
            this.labelGLLegacyInstallerEnvPath.Size = new System.Drawing.Size(228, 20);
            this.labelGLLegacyInstallerEnvPath.TabIndex = 10;
            this.labelGLLegacyInstallerEnvPath.Text = "GLLegacyInstallerEnv.bat path";
            // 
            // buttonDistccHosts
            // 
            this.buttonDistccHosts.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.buttonDistccHosts.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.buttonDistccHosts.ImageAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.buttonDistccHosts.Location = new System.Drawing.Point(29, 377);
            this.buttonDistccHosts.Name = "buttonDistccHosts";
            this.buttonDistccHosts.Size = new System.Drawing.Size(157, 27);
            this.buttonDistccHosts.TabIndex = 8;
            this.buttonDistccHosts.Text = "GLLegacyDistccHosts";
            this.buttonDistccHosts.UseVisualStyleBackColor = true;
            this.buttonDistccHosts.Click += new System.EventHandler(this.buttonDistccHosts_Click);
            // 
            // buttonDistccEnv
            // 
            this.buttonDistccEnv.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.buttonDistccEnv.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.buttonDistccEnv.ImageAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.buttonDistccEnv.Location = new System.Drawing.Point(29, 330);
            this.buttonDistccEnv.Name = "buttonDistccEnv";
            this.buttonDistccEnv.Size = new System.Drawing.Size(157, 27);
            this.buttonDistccEnv.TabIndex = 7;
            this.buttonDistccEnv.Text = "GLLegacyDistccEnv";
            this.buttonDistccEnv.UseVisualStyleBackColor = true;
            this.buttonDistccEnv.Click += new System.EventHandler(this.buttonDistccEnv_Click);
            // 
            // buttonEnv
            // 
            this.buttonEnv.BackgroundImageLayout = System.Windows.Forms.ImageLayout.Center;
            this.buttonEnv.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.buttonEnv.ImageAlign = System.Drawing.ContentAlignment.MiddleRight;
            this.buttonEnv.Location = new System.Drawing.Point(29, 283);
            this.buttonEnv.Name = "buttonEnv";
            this.buttonEnv.Size = new System.Drawing.Size(157, 27);
            this.buttonEnv.TabIndex = 6;
            this.buttonEnv.Text = "GLLegacyInstallerEnv";
            this.buttonEnv.UseVisualStyleBackColor = true;
            this.buttonEnv.Click += new System.EventHandler(this.buttonEnv_Click);
            // 
            // m_distccServerCheck
            // 
            this.m_distccServerCheck.Interval = 2000;
            this.m_distccServerCheck.Tick += new System.EventHandler(this.TimerCheckDistccServerStatus);
            // 
            // m_distccHostFileCheck
            // 
            this.m_distccHostFileCheck.Interval = 2000;
            this.m_distccHostFileCheck.Tick += new System.EventHandler(this.TimerCheckDistccHostFileStatus);
            // 
            // labelAndroidWarning
            // 
            this.labelAndroidWarning.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.labelAndroidWarning.AutoSize = true;
            this.labelAndroidWarning.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelAndroidWarning.ForeColor = System.Drawing.Color.Firebrick;
            this.labelAndroidWarning.Location = new System.Drawing.Point(268, 613);
            this.labelAndroidWarning.Name = "labelAndroidWarning";
            this.labelAndroidWarning.Size = new System.Drawing.Size(323, 17);
            this.labelAndroidWarning.TabIndex = 7;
            this.labelAndroidWarning.Text = "This tab is active just for Android platform. ";
            this.labelAndroidWarning.Visible = false;
            // 
            // toolTip1
            // 
            this.toolTip1.AutoPopDelay = 5000;
            this.toolTip1.InitialDelay = 1000;
            this.toolTip1.ReshowDelay = 500;
            this.toolTip1.ShowAlways = true;
            // 
            // pictureBox1
            // 
            this.pictureBox1.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.pictureBox1.Image = global::GLLegacyInstaller.Properties.Resources.LogoGLLegacy;
            this.pictureBox1.Location = new System.Drawing.Point(4, 27);
            this.pictureBox1.Name = "pictureBox1";
            this.pictureBox1.Size = new System.Drawing.Size(879, 122);
            this.pictureBox1.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.pictureBox1.TabIndex = 2;
            this.pictureBox1.TabStop = false;
            // 
            // MainWindow
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(883, 645);
            this.Controls.Add(this.m_version);
            this.Controls.Add(this.labelAndroidWarning);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.m_button_Next);
            this.Controls.Add(this.pictureBox1);
            this.Controls.Add(this.m_TAB);
            this.Controls.Add(this.m_button_Prev);
            this.Controls.Add(this.menuStrip);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MainMenuStrip = this.menuStrip;
            this.MinimumSize = new System.Drawing.Size(899, 683);
            this.Name = "MainWindow";
            this.Text = "GLLegacyInstaller";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainWindow_FormClosing);
            this.Load += new System.EventHandler(this.GLLegacyInstaller_Load);
            this.menuStrip.ResumeLayout(false);
            this.menuStrip.PerformLayout();
            this.m_TAB_SVNOperations.ResumeLayout(false);
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox2)).EndInit();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.m_TAB_CheckTools.ResumeLayout(false);
            this.m_TAB_Initialize.ResumeLayout(false);
            this.m_TAB_Initialize.PerformLayout();
            this.m_TAB.ResumeLayout(false);
            this.m_TAB_DISTCC.ResumeLayout(false);
            this.splitContainer1.Panel1.ResumeLayout(false);
            this.splitContainer1.Panel1.PerformLayout();
            this.splitContainer1.Panel2.ResumeLayout(false);
            this.splitContainer1.Panel2.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer1)).EndInit();
            this.splitContainer1.ResumeLayout(false);
            this.m_groupBoxSHosts.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox3)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.numericUpDown)).EndInit();
            this.m_groupBoxHosts.ResumeLayout(false);
            this.m_groupBoxServer.ResumeLayout(false);
            this.m_groupBoxServer.PerformLayout();
            this.groupBoxGLLS.ResumeLayout(false);
            this.groupBoxGLLS.PerformLayout();
            this.m_TAB_Sumup.ResumeLayout(false);
            this.m_TAB_Sumup.PerformLayout();
            this.groupBox5.ResumeLayout(false);
            this.groupBox5.PerformLayout();
            this.groupBox4.ResumeLayout(false);
            this.groupBox4.PerformLayout();
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxUseHosts)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBoxServer)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.MenuStrip menuStrip;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem helpToolStripMenuItem;
        private System.Windows.Forms.Button m_button_Next;
        private System.Windows.Forms.Button m_button_Prev;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label m_version;
        private System.Windows.Forms.Timer m_startTimer;
        public System.Windows.Forms.TabPage m_TAB_SVNOperations;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.Button TortoiseSVN;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Button m_browse;
        private System.Windows.Forms.TextBox m_textBoxPath;
        public System.Windows.Forms.RadioButton m_RB_Update;
        public System.Windows.Forms.RadioButton m_RB_CheckOut;
        public System.Windows.Forms.TabPage m_TAB_CheckTools;
        public System.Windows.Forms.ProgressBar m_detectingProgress;
        private System.Windows.Forms.Button m_button_Refresh;
        public SourceGrid.Grid m_gridTools;
        public System.Windows.Forms.TabPage m_TAB_Initialize;
        private System.Windows.Forms.Label label1;
        public System.Windows.Forms.ProgressBar m_loadingProgress;
        public System.Windows.Forms.TabControl m_TAB;
        private System.Windows.Forms.ToolStripMenuItem aboutToolStripMenuItem;
        private System.Windows.Forms.LinkLabel linkLabelRelease;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.RadioButton radioButtonDevelopment;
        private System.Windows.Forms.RadioButton radioButtonRelease;
        private System.Windows.Forms.LinkLabel linkLabelDevelopment;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.PictureBox pictureBox2;
        private System.Windows.Forms.TabPage m_TAB_DISTCC;
        private System.Windows.Forms.CheckBox m_checkBoxDISTCCServer;
        private System.Windows.Forms.CheckBox m_checkBoxDISTCCUse;
        private System.Windows.Forms.GroupBox m_groupBoxServer;
        private System.Windows.Forms.SplitContainer splitContainer1;
        private System.Windows.Forms.Label labelVirtualDrive;
        private System.Windows.Forms.ComboBox comboBoxLetterDrive;
        private System.Windows.Forms.Label labelServiceStatus;
        private System.Windows.Forms.Label label13;
        private System.Windows.Forms.Label label12;
        private System.Windows.Forms.Label labelDISTCCStatus;
        private System.Windows.Forms.Label label14;
        private System.Windows.Forms.TextBox textBoxJOBS;
        private System.Windows.Forms.Label labelCORES;
        private System.Windows.Forms.Label label17;
        private System.Windows.Forms.Label label16;
        private System.Windows.Forms.GroupBox groupBoxGLLS;
        private System.Windows.Forms.Button buttonRestartService;
        private System.Windows.Forms.Timer m_distccServerCheck;
        private System.Windows.Forms.Timer m_distccHostFileCheck;
        private System.Windows.Forms.Label label11;
        private System.Windows.Forms.FolderBrowserDialog folderPicker;
        private System.Windows.Forms.Label lbReleaseRev;
        private System.Windows.Forms.ToolStripMenuItem installerDocsToolStripMenuItem;
        private System.Windows.Forms.PictureBox pictureBox3;
        //private System.Windows.Forms.CheckBox[] m_checkBoxPlatforms;
        private System.Windows.Forms.Label labelAndroidWarning;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label labelVersion;
        private System.Windows.Forms.TabPage m_TAB_Sumup;
        private System.Windows.Forms.Button buttonDistccHosts;
        private System.Windows.Forms.Button buttonDistccEnv;
        private System.Windows.Forms.Button buttonEnv;
        private System.Windows.Forms.Label labelGLLegacyDistccHostsPath;
        private System.Windows.Forms.Label labelGLLegacyDistccEnvPath;
        private System.Windows.Forms.Label labelGLLegacyInstallerEnvPath;
        private System.Windows.Forms.Label labelInvalidTools;
        private System.Windows.Forms.Label label18;
        private System.Windows.Forms.Label labelTotalTools;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.Label label15;
        private System.Windows.Forms.Label label19;
        private System.Windows.Forms.PictureBox pictureBoxUseHosts;
        private System.Windows.Forms.PictureBox pictureBoxServer;
        private System.Windows.Forms.Label labelGLLegacyServiceStatus1;
        private System.Windows.Forms.Label labelHostsNumber;
        private System.Windows.Forms.Label labelHostsNumber1;
        private System.Windows.Forms.Label labelGLLegacyServiceStatus;
        private System.Windows.Forms.Label labelDistccdStatus;
        public System.Windows.Forms.Label labelDistccdStatus1;
        private System.Windows.Forms.Label labelDriveSelected;
        private System.Windows.Forms.Label labelDriveSelected1;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.GroupBox groupBox4;
        private System.Windows.Forms.Button buttonInstallerSetup;
        private ToolTip toolTip1;
        private Label labelDiscoverTime;
        private NumericUpDown numericUpDown;
        private GroupBox groupBox5;
        private Label labelDiscoverTimeS;
        private Label labelDiscoverTime1;
        private Button buttonRescan;
        private Button buttonReinstallService;
        private Button buttonOpenLogFile;
        private ComboBox comboBoxNetAddr;
        private CheckBox checkShowServers;
        private IPAddressControlLib.IPAddressControl ipAddressControl;
        private Button buttonAddIP;
        private GroupBox m_groupBoxHosts;
        public SourceGrid.Grid m_gridAvailableHosts;
        private GroupBox m_groupBoxSHosts;
        public SourceGrid.Grid m_gridIPs;
        private Label labelSHostsNumber1;
        private Label labelSHostsNumber;
        private PictureBox pictureBox1;
    }
}

