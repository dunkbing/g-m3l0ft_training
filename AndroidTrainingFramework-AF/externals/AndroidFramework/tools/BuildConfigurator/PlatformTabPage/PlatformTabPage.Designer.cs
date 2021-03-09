namespace PlatformTabPage
{
    partial class PlatformTabPage
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

        #region Component Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
			this.components = new System.ComponentModel.Container();
			System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(PlatformTabPage));
			this.labelGridENVFile = new System.Windows.Forms.Label();
			this.splitContainer = new System.Windows.Forms.SplitContainer();
			this.textBoxENV = new System.Windows.Forms.TextBox();
			this.buttonENVNotepad = new System.Windows.Forms.Button();
			this.buttonENVFileSave = new System.Windows.Forms.Button();
			this.GridEnvOptions = new SourceGrid.Grid();
			this.textBoxCFG = new System.Windows.Forms.TextBox();
			this.buttonCFGFileNotepad = new System.Windows.Forms.Button();
			this.labelGridCFGFile = new System.Windows.Forms.Label();
			this.buttonCFGFileSave = new System.Windows.Forms.Button();
			this.textBoxFilter = new System.Windows.Forms.TextBox();
			this.GridConfigOptions = new SourceGrid.Grid();
			this.labelConfigsFolder = new System.Windows.Forms.TextBox();
			this.checkX86 = new System.Windows.Forms.CheckBox();
			this.checkARM = new System.Windows.Forms.CheckBox();
			this.buttonInstall = new System.Windows.Forms.Button();
			this.radioButtonDebug = new System.Windows.Forms.RadioButton();
			this.buttonClean = new System.Windows.Forms.Button();
			this.radioButtonRelease = new System.Windows.Forms.RadioButton();
			this.groupBox2 = new System.Windows.Forms.GroupBox();
			this.buttonOpenConfigs = new System.Windows.Forms.Button();
			this.groupBox3 = new System.Windows.Forms.GroupBox();
			this.buttonOpenGenerated = new System.Windows.Forms.Button();
			this.labelGeneratedFolder = new System.Windows.Forms.TextBox();
			this.timerRefresh = new System.Windows.Forms.Timer(this.components);
			this.buttonBuild = new System.Windows.Forms.Button();
			this.panel1 = new System.Windows.Forms.Panel();
			this.pictureBox = new System.Windows.Forms.PictureBox();
			((System.ComponentModel.ISupportInitialize)(this.splitContainer)).BeginInit();
			this.splitContainer.Panel1.SuspendLayout();
			this.splitContainer.Panel2.SuspendLayout();
			this.splitContainer.SuspendLayout();
			this.groupBox2.SuspendLayout();
			this.groupBox3.SuspendLayout();
			this.panel1.SuspendLayout();
			((System.ComponentModel.ISupportInitialize)(this.pictureBox)).BeginInit();
			this.SuspendLayout();
			// 
			// labelGridENVFile
			// 
			this.labelGridENVFile.AutoSize = true;
			this.labelGridENVFile.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.labelGridENVFile.Location = new System.Drawing.Point(155, 16);
			this.labelGridENVFile.Name = "labelGridENVFile";
			this.labelGridENVFile.Size = new System.Drawing.Size(73, 15);
			this.labelGridENVFile.TabIndex = 1;
			this.labelGridENVFile.Text = "setEnv.bat";
			// 
			// splitContainer
			// 
			this.splitContainer.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.splitContainer.ImeMode = System.Windows.Forms.ImeMode.Alpha;
			this.splitContainer.Location = new System.Drawing.Point(0, 276);
			this.splitContainer.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.splitContainer.Name = "splitContainer";
			// 
			// splitContainer.Panel1
			// 
			this.splitContainer.Panel1.Controls.Add(this.textBoxENV);
			this.splitContainer.Panel1.Controls.Add(this.buttonENVNotepad);
			this.splitContainer.Panel1.Controls.Add(this.buttonENVFileSave);
			this.splitContainer.Panel1.Controls.Add(this.labelGridENVFile);
			this.splitContainer.Panel1.Controls.Add(this.GridEnvOptions);
			// 
			// splitContainer.Panel2
			// 
			this.splitContainer.Panel2.Controls.Add(this.textBoxCFG);
			this.splitContainer.Panel2.Controls.Add(this.buttonCFGFileNotepad);
			this.splitContainer.Panel2.Controls.Add(this.labelGridCFGFile);
			this.splitContainer.Panel2.Controls.Add(this.buttonCFGFileSave);
			this.splitContainer.Panel2.Controls.Add(this.textBoxFilter);
			this.splitContainer.Panel2.Controls.Add(this.GridConfigOptions);
			this.splitContainer.Size = new System.Drawing.Size(1053, 616);
			this.splitContainer.SplitterDistance = 392;
			this.splitContainer.SplitterWidth = 6;
			this.splitContainer.TabIndex = 2;
			// 
			// textBoxENV
			// 
			this.textBoxENV.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.textBoxENV.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.textBoxENV.Location = new System.Drawing.Point(10, 464);
			this.textBoxENV.Multiline = true;
			this.textBoxENV.Name = "textBoxENV";
			this.textBoxENV.ReadOnly = true;
			this.textBoxENV.Size = new System.Drawing.Size(379, 47);
			this.textBoxENV.TabIndex = 5;
			// 
			// buttonENVNotepad
			// 
			this.buttonENVNotepad.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonENVNotepad.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
			this.buttonENVNotepad.Location = new System.Drawing.Point(10, 11);
			this.buttonENVNotepad.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.buttonENVNotepad.Name = "buttonENVNotepad";
			this.buttonENVNotepad.Size = new System.Drawing.Size(65, 25);
			this.buttonENVNotepad.TabIndex = 4;
			this.buttonENVNotepad.Text = "Edit";
			this.buttonENVNotepad.UseVisualStyleBackColor = true;
			this.buttonENVNotepad.Click += new System.EventHandler(this.buttonENVNotepad_Click);
			// 
			// buttonENVFileSave
			// 
			this.buttonENVFileSave.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonENVFileSave.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
			this.buttonENVFileSave.Location = new System.Drawing.Point(81, 11);
			this.buttonENVFileSave.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.buttonENVFileSave.Name = "buttonENVFileSave";
			this.buttonENVFileSave.Size = new System.Drawing.Size(68, 25);
			this.buttonENVFileSave.TabIndex = 3;
			this.buttonENVFileSave.Text = "Save";
			this.buttonENVFileSave.UseVisualStyleBackColor = true;
			this.buttonENVFileSave.Click += new System.EventHandler(this.buttonENVSave_Click);
			// 
			// GridEnvOptions
			// 
			this.GridEnvOptions.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.GridEnvOptions.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
			this.GridEnvOptions.EnableSort = true;
			this.GridEnvOptions.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.GridEnvOptions.Location = new System.Drawing.Point(10, 44);
			this.GridEnvOptions.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.GridEnvOptions.Name = "GridEnvOptions";
			this.GridEnvOptions.OptimizeMode = SourceGrid.CellOptimizeMode.ForRows;
			this.GridEnvOptions.SelectionMode = SourceGrid.GridSelectionMode.Cell;
			this.GridEnvOptions.Size = new System.Drawing.Size(379, 413);
			this.GridEnvOptions.TabIndex = 0;
			this.GridEnvOptions.TabStop = true;
			this.GridEnvOptions.ToolTipText = "";
			this.GridEnvOptions.VScrollPositionChanged += new SourceGrid.ScrollPositionChangedEventHandler(this.gridENVFile_VScrollPositionChanged);
			this.GridEnvOptions.HScrollPositionChanged += new SourceGrid.ScrollPositionChangedEventHandler(this.gridENVFile_HScrollPositionChanged);
			this.GridEnvOptions.Click += new System.EventHandler(this.gridENVFile_Click);
			this.GridEnvOptions.MouseUp += new System.Windows.Forms.MouseEventHandler(this.gridENVFile_MouseUp);
			// 
			// textBoxCFG
			// 
			this.textBoxCFG.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.textBoxCFG.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.textBoxCFG.Location = new System.Drawing.Point(11, 464);
			this.textBoxCFG.Multiline = true;
			this.textBoxCFG.Name = "textBoxCFG";
			this.textBoxCFG.Size = new System.Drawing.Size(620, 47);
			this.textBoxCFG.TabIndex = 6;
			// 
			// buttonCFGFileNotepad
			// 
			this.buttonCFGFileNotepad.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonCFGFileNotepad.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
			this.buttonCFGFileNotepad.Location = new System.Drawing.Point(11, 11);
			this.buttonCFGFileNotepad.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.buttonCFGFileNotepad.Name = "buttonCFGFileNotepad";
			this.buttonCFGFileNotepad.Size = new System.Drawing.Size(58, 25);
			this.buttonCFGFileNotepad.TabIndex = 5;
			this.buttonCFGFileNotepad.Text = "Edit";
			this.buttonCFGFileNotepad.UseVisualStyleBackColor = true;
			this.buttonCFGFileNotepad.Click += new System.EventHandler(this.buttonCONFIGNotepad_Click);
			// 
			// labelGridCFGFile
			// 
			this.labelGridCFGFile.AutoSize = true;
			this.labelGridCFGFile.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.labelGridCFGFile.Location = new System.Drawing.Point(149, 16);
			this.labelGridCFGFile.Name = "labelGridCFGFile";
			this.labelGridCFGFile.Size = new System.Drawing.Size(70, 15);
			this.labelGridCFGFile.TabIndex = 3;
			this.labelGridCFGFile.Text = "config.bat";
			// 
			// buttonCFGFileSave
			// 
			this.buttonCFGFileSave.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonCFGFileSave.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
			this.buttonCFGFileSave.Location = new System.Drawing.Point(75, 11);
			this.buttonCFGFileSave.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.buttonCFGFileSave.Name = "buttonCFGFileSave";
			this.buttonCFGFileSave.Size = new System.Drawing.Size(68, 25);
			this.buttonCFGFileSave.TabIndex = 5;
			this.buttonCFGFileSave.Text = "Save";
			this.buttonCFGFileSave.UseVisualStyleBackColor = true;
			this.buttonCFGFileSave.Click += new System.EventHandler(this.buttonCONFIGSave_Click);
			// 
			// textBoxFilter
			// 
			this.textBoxFilter.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.textBoxFilter.Location = new System.Drawing.Point(405, 13);
			this.textBoxFilter.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.textBoxFilter.Name = "textBoxFilter";
			this.textBoxFilter.Size = new System.Drawing.Size(226, 22);
			this.textBoxFilter.TabIndex = 9;
			this.textBoxFilter.TextChanged += new System.EventHandler(this.textBoxFilter_TextChanged);
			// 
			// GridConfigOptions
			// 
			this.GridConfigOptions.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.GridConfigOptions.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
			this.GridConfigOptions.Cursor = System.Windows.Forms.Cursors.Arrow;
			this.GridConfigOptions.EnableSort = true;
			this.GridConfigOptions.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.GridConfigOptions.Location = new System.Drawing.Point(11, 44);
			this.GridConfigOptions.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.GridConfigOptions.Name = "GridConfigOptions";
			this.GridConfigOptions.OptimizeMode = SourceGrid.CellOptimizeMode.ForRows;
			this.GridConfigOptions.SelectionMode = SourceGrid.GridSelectionMode.Cell;
			this.GridConfigOptions.Size = new System.Drawing.Size(620, 413);
			this.GridConfigOptions.TabIndex = 0;
			this.GridConfigOptions.TabStop = true;
			this.GridConfigOptions.ToolTipText = "";
			this.GridConfigOptions.VScrollPositionChanged += new SourceGrid.ScrollPositionChangedEventHandler(this.scrollVertical);
			this.GridConfigOptions.HScrollPositionChanged += new SourceGrid.ScrollPositionChangedEventHandler(this.scrollHorizontal);
			this.GridConfigOptions.Click += new System.EventHandler(this.gridCFGFile_Click);
			this.GridConfigOptions.MouseUp += new System.Windows.Forms.MouseEventHandler(this.gridCFGFile_MouseUp);
			// 
			// labelConfigsFolder
			// 
			this.labelConfigsFolder.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.labelConfigsFolder.BackColor = System.Drawing.SystemColors.ButtonHighlight;
			this.labelConfigsFolder.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.labelConfigsFolder.Location = new System.Drawing.Point(76, 24);
			this.labelConfigsFolder.Name = "labelConfigsFolder";
			this.labelConfigsFolder.ReadOnly = true;
			this.labelConfigsFolder.Size = new System.Drawing.Size(658, 20);
			this.labelConfigsFolder.TabIndex = 3;
			this.labelConfigsFolder.Text = "AndroidFrameworkConfig/configs/project";
			// 
			// checkX86
			// 
			this.checkX86.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.checkX86.AutoSize = true;
			this.checkX86.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.checkX86.Location = new System.Drawing.Point(565, 46);
			this.checkX86.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.checkX86.Name = "checkX86";
			this.checkX86.Size = new System.Drawing.Size(46, 19);
			this.checkX86.TabIndex = 12;
			this.checkX86.Text = "x86";
			this.checkX86.UseVisualStyleBackColor = true;
			// 
			// checkARM
			// 
			this.checkARM.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.checkARM.AutoSize = true;
			this.checkARM.Checked = true;
			this.checkARM.CheckState = System.Windows.Forms.CheckState.Checked;
			this.checkARM.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.checkARM.Location = new System.Drawing.Point(566, 11);
			this.checkARM.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.checkARM.Name = "checkARM";
			this.checkARM.Size = new System.Drawing.Size(48, 19);
			this.checkARM.TabIndex = 11;
			this.checkARM.Text = "arm";
			this.checkARM.UseVisualStyleBackColor = true;
			// 
			// buttonInstall
			// 
			this.buttonInstall.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.buttonInstall.Enabled = false;
			this.buttonInstall.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonInstall.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
			this.buttonInstall.Location = new System.Drawing.Point(641, 42);
			this.buttonInstall.Name = "buttonInstall";
			this.buttonInstall.Size = new System.Drawing.Size(104, 31);
			this.buttonInstall.TabIndex = 10;
			this.buttonInstall.Text = "Install";
			this.buttonInstall.UseVisualStyleBackColor = true;
			this.buttonInstall.Click += new System.EventHandler(this.buttonInstall_Click);
			// 
			// radioButtonDebug
			// 
			this.radioButtonDebug.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.radioButtonDebug.AutoSize = true;
			this.radioButtonDebug.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.radioButtonDebug.Location = new System.Drawing.Point(483, 46);
			this.radioButtonDebug.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.radioButtonDebug.Name = "radioButtonDebug";
			this.radioButtonDebug.Size = new System.Drawing.Size(60, 19);
			this.radioButtonDebug.TabIndex = 8;
			this.radioButtonDebug.Text = "debug";
			this.radioButtonDebug.UseVisualStyleBackColor = true;
			// 
			// buttonClean
			// 
			this.buttonClean.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.buttonClean.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonClean.ImageAlign = System.Drawing.ContentAlignment.MiddleLeft;
			this.buttonClean.Location = new System.Drawing.Point(641, 5);
			this.buttonClean.Name = "buttonClean";
			this.buttonClean.Size = new System.Drawing.Size(104, 31);
			this.buttonClean.TabIndex = 9;
			this.buttonClean.Text = "Clean";
			this.buttonClean.UseVisualStyleBackColor = true;
			this.buttonClean.Click += new System.EventHandler(this.buttonClean_Click);
			// 
			// radioButtonRelease
			// 
			this.radioButtonRelease.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.radioButtonRelease.AutoSize = true;
			this.radioButtonRelease.Checked = true;
			this.radioButtonRelease.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.radioButtonRelease.Location = new System.Drawing.Point(484, 11);
			this.radioButtonRelease.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.radioButtonRelease.Name = "radioButtonRelease";
			this.radioButtonRelease.Size = new System.Drawing.Size(66, 19);
			this.radioButtonRelease.TabIndex = 7;
			this.radioButtonRelease.TabStop = true;
			this.radioButtonRelease.Text = "release";
			this.radioButtonRelease.UseVisualStyleBackColor = true;
			// 
			// groupBox2
			// 
			this.groupBox2.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.groupBox2.Controls.Add(this.buttonOpenConfigs);
			this.groupBox2.Controls.Add(this.labelConfigsFolder);
			this.groupBox2.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.groupBox2.Location = new System.Drawing.Point(291, 14);
			this.groupBox2.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.groupBox2.Name = "groupBox2";
			this.groupBox2.Padding = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.groupBox2.Size = new System.Drawing.Size(748, 62);
			this.groupBox2.TabIndex = 11;
			this.groupBox2.TabStop = false;
			this.groupBox2.Text = "Config files";
			// 
			// buttonOpenConfigs
			// 
			this.buttonOpenConfigs.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonOpenConfigs.Location = new System.Drawing.Point(12, 22);
			this.buttonOpenConfigs.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.buttonOpenConfigs.Name = "buttonOpenConfigs";
			this.buttonOpenConfigs.Size = new System.Drawing.Size(58, 24);
			this.buttonOpenConfigs.TabIndex = 5;
			this.buttonOpenConfigs.Text = "Open";
			this.buttonOpenConfigs.UseVisualStyleBackColor = true;
			this.buttonOpenConfigs.Click += new System.EventHandler(this.buttonENVFileBrowse_Click);
			// 
			// groupBox3
			// 
			this.groupBox3.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.groupBox3.Controls.Add(this.buttonOpenGenerated);
			this.groupBox3.Controls.Add(this.labelGeneratedFolder);
			this.groupBox3.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.groupBox3.Location = new System.Drawing.Point(291, 84);
			this.groupBox3.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.groupBox3.Name = "groupBox3";
			this.groupBox3.Padding = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.groupBox3.Size = new System.Drawing.Size(748, 62);
			this.groupBox3.TabIndex = 12;
			this.groupBox3.TabStop = false;
			this.groupBox3.Text = "Generated configs";
			// 
			// buttonOpenGenerated
			// 
			this.buttonOpenGenerated.Font = new System.Drawing.Font("Microsoft Sans Serif", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonOpenGenerated.Location = new System.Drawing.Point(12, 22);
			this.buttonOpenGenerated.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.buttonOpenGenerated.Name = "buttonOpenGenerated";
			this.buttonOpenGenerated.Size = new System.Drawing.Size(58, 24);
			this.buttonOpenGenerated.TabIndex = 7;
			this.buttonOpenGenerated.Text = "Open";
			this.buttonOpenGenerated.UseVisualStyleBackColor = true;
			this.buttonOpenGenerated.Click += new System.EventHandler(this.buttonOpenGenerated_Click);
			// 
			// labelGeneratedFolder
			// 
			this.labelGeneratedFolder.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.labelGeneratedFolder.BackColor = System.Drawing.SystemColors.ButtonHighlight;
			this.labelGeneratedFolder.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.labelGeneratedFolder.Location = new System.Drawing.Point(75, 24);
			this.labelGeneratedFolder.Name = "labelGeneratedFolder";
			this.labelGeneratedFolder.ReadOnly = true;
			this.labelGeneratedFolder.Size = new System.Drawing.Size(659, 20);
			this.labelGeneratedFolder.TabIndex = 3;
			this.labelGeneratedFolder.Text = "AndroidFrameworkConfig/configs/generated";
			// 
			// timerRefresh
			// 
			this.timerRefresh.Interval = 300;
			this.timerRefresh.Tick += new System.EventHandler(this.timerRefresh_Tick);
			// 
			// buttonBuild
			// 
			this.buttonBuild.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.buttonBuild.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
			this.buttonBuild.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(40)))), ((int)(((byte)(120)))), ((int)(((byte)(180)))), ((int)(((byte)(255)))));
			this.buttonBuild.Enabled = false;
			this.buttonBuild.Font = new System.Drawing.Font("Microsoft Sans Serif", 11.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.buttonBuild.Location = new System.Drawing.Point(23, 4);
			this.buttonBuild.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.buttonBuild.Name = "buttonBuild";
			this.buttonBuild.Size = new System.Drawing.Size(427, 68);
			this.buttonBuild.TabIndex = 6;
			this.buttonBuild.Text = "Start Build";
			this.buttonBuild.UseVisualStyleBackColor = false;
			this.buttonBuild.Click += new System.EventHandler(this.buttonBuild_Click);
			// 
			// panel1
			// 
			this.panel1.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
			this.panel1.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
			this.panel1.BackColor = System.Drawing.Color.Transparent;
			this.panel1.Controls.Add(this.checkX86);
			this.panel1.Controls.Add(this.buttonBuild);
			this.panel1.Controls.Add(this.checkARM);
			this.panel1.Controls.Add(this.buttonClean);
			this.panel1.Controls.Add(this.buttonInstall);
			this.panel1.Controls.Add(this.radioButtonRelease);
			this.panel1.Controls.Add(this.radioButtonDebug);
			this.panel1.Location = new System.Drawing.Point(280, 179);
			this.panel1.Name = "panel1";
			this.panel1.Size = new System.Drawing.Size(774, 77);
			this.panel1.TabIndex = 13;
			// 
			// pictureBox
			// 
			this.pictureBox.BackgroundImage = ((System.Drawing.Image)(resources.GetObject("pictureBox.BackgroundImage")));
			this.pictureBox.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
			this.pictureBox.InitialImage = null;
			this.pictureBox.Location = new System.Drawing.Point(10, 21);
			this.pictureBox.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
			this.pictureBox.Name = "pictureBox";
			this.pictureBox.Size = new System.Drawing.Size(264, 230);
			this.pictureBox.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
			this.pictureBox.TabIndex = 0;
			this.pictureBox.TabStop = false;
			this.pictureBox.Click += new System.EventHandler(this.pictureBox_Click);
			// 
			// PlatformTabPage
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.Controls.Add(this.panel1);
			this.Controls.Add(this.groupBox3);
			this.Controls.Add(this.groupBox2);
			this.Controls.Add(this.pictureBox);
			this.Controls.Add(this.splitContainer);
			this.Font = new System.Drawing.Font("Microsoft Sans Serif", 9.75F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			this.Margin = new System.Windows.Forms.Padding(10);
			this.Name = "PlatformTabPage";
			this.Padding = new System.Windows.Forms.Padding(10);
			this.Size = new System.Drawing.Size(1059, 800);
			this.Load += new System.EventHandler(this.PlatformTabPage_Load);
			this.splitContainer.Panel1.ResumeLayout(false);
			this.splitContainer.Panel1.PerformLayout();
			this.splitContainer.Panel2.ResumeLayout(false);
			this.splitContainer.Panel2.PerformLayout();
			((System.ComponentModel.ISupportInitialize)(this.splitContainer)).EndInit();
			this.splitContainer.ResumeLayout(false);
			this.groupBox2.ResumeLayout(false);
			this.groupBox2.PerformLayout();
			this.groupBox3.ResumeLayout(false);
			this.groupBox3.PerformLayout();
			this.panel1.ResumeLayout(false);
			this.panel1.PerformLayout();
			((System.ComponentModel.ISupportInitialize)(this.pictureBox)).EndInit();
			this.ResumeLayout(false);

        }

        #endregion

        public System.Windows.Forms.PictureBox pictureBox;
        public System.Windows.Forms.SplitContainer splitContainer;
        public SourceGrid.Grid GridEnvOptions;
        public SourceGrid.Grid GridConfigOptions;
        public System.Windows.Forms.Label labelGridENVFile;
        public System.Windows.Forms.Label labelGridCFGFile;
        public System.Windows.Forms.TextBox labelConfigsFolder;
        public System.Windows.Forms.Button buttonENVFileSave;
        public System.Windows.Forms.Button buttonCFGFileSave;
        public System.Windows.Forms.Button buttonENVNotepad;
        public System.Windows.Forms.Button buttonCFGFileNotepad;
        private System.Windows.Forms.Button buttonBuild;
        private System.Windows.Forms.RadioButton radioButtonDebug;
        private System.Windows.Forms.RadioButton radioButtonRelease;
        private System.Windows.Forms.TextBox textBoxFilter;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.GroupBox groupBox3;
        public System.Windows.Forms.TextBox labelGeneratedFolder;
		private System.Windows.Forms.Timer timerRefresh;
        private System.Windows.Forms.TextBox textBoxENV;
        private System.Windows.Forms.TextBox textBoxCFG;
        private System.Windows.Forms.Button buttonClean;
        private System.Windows.Forms.Button buttonInstall;
        private System.Windows.Forms.CheckBox checkARM;
        private System.Windows.Forms.CheckBox checkX86;
        private System.Windows.Forms.Button buttonOpenConfigs;
        private System.Windows.Forms.Button buttonOpenGenerated;
        private System.Windows.Forms.Panel panel1;
    }
}
