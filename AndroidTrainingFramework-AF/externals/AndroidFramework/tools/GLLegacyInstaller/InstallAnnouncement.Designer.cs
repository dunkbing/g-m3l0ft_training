namespace GLLegacyInstaller
{
    partial class InstallAnnouncement
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(InstallAnnouncement));
            this.nextBT = new System.Windows.Forms.Button();
            this.prevBT = new System.Windows.Forms.Button();
            this.InstallToolLB = new System.Windows.Forms.Label();
            this.toolPathTB = new System.Windows.Forms.TextBox();
            this.folderLA = new System.Windows.Forms.Label();
            this.folderBrowser = new System.Windows.Forms.FolderBrowserDialog();
            this.pickFolderBT = new System.Windows.Forms.Button();
            this.infoLB = new System.Windows.Forms.Label();
            this.iconPB = new System.Windows.Forms.PictureBox();
            ((System.ComponentModel.ISupportInitialize)(this.iconPB)).BeginInit();
            this.SuspendLayout();
            // 
            // nextBT
            // 
            this.nextBT.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.nextBT.Location = new System.Drawing.Point(458, 88);
            this.nextBT.Name = "nextBT";
            this.nextBT.Size = new System.Drawing.Size(100, 25);
            this.nextBT.TabIndex = 0;
            this.nextBT.Text = "Proceed";
            this.nextBT.UseVisualStyleBackColor = true;
            this.nextBT.Click += new System.EventHandler(this.nextBT_Click);
            // 
            // prevBT
            // 
            this.prevBT.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.prevBT.Location = new System.Drawing.Point(12, 88);
            this.prevBT.Name = "prevBT";
            this.prevBT.Size = new System.Drawing.Size(101, 25);
            this.prevBT.TabIndex = 1;
            this.prevBT.Text = "Close";
            this.prevBT.UseVisualStyleBackColor = true;
            this.prevBT.Click += new System.EventHandler(this.prevBT_Click);
            // 
            // InstallToolLB
            // 
            this.InstallToolLB.AutoSize = true;
            this.InstallToolLB.Location = new System.Drawing.Point(66, 9);
            this.InstallToolLB.Name = "InstallToolLB";
            this.InstallToolLB.Size = new System.Drawing.Size(120, 13);
            this.InstallToolLB.TabIndex = 2;
            this.InstallToolLB.Text = "Select Instalation Folder";
            this.InstallToolLB.TextAlign = System.Drawing.ContentAlignment.TopCenter;
            // 
            // toolPathTB
            // 
            this.toolPathTB.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.toolPathTB.CausesValidation = false;
            this.toolPathTB.Location = new System.Drawing.Point(69, 54);
            this.toolPathTB.Name = "toolPathTB";
            this.toolPathTB.Size = new System.Drawing.Size(383, 20);
            this.toolPathTB.TabIndex = 3;
            this.toolPathTB.TextChanged += new System.EventHandler(this.toolPathTB_TextChanged);
            // 
            // folderLA
            // 
            this.folderLA.AutoSize = true;
            this.folderLA.Location = new System.Drawing.Point(12, 57);
            this.folderLA.Name = "folderLA";
            this.folderLA.Size = new System.Drawing.Size(51, 13);
            this.folderLA.TabIndex = 4;
            this.folderLA.Text = "Location:";
            // 
            // folderBrowser
            // 
            this.folderBrowser.Description = "Specify a location in your thinking box";
            this.folderBrowser.HelpRequest += new System.EventHandler(this.folderBrowser_HelpRequest);
            // 
            // pickFolderBT
            // 
            this.pickFolderBT.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.pickFolderBT.BackColor = System.Drawing.Color.PapayaWhip;
            this.pickFolderBT.Location = new System.Drawing.Point(458, 54);
            this.pickFolderBT.Name = "pickFolderBT";
            this.pickFolderBT.Size = new System.Drawing.Size(100, 22);
            this.pickFolderBT.TabIndex = 5;
            this.pickFolderBT.Text = "Browse";
            this.pickFolderBT.UseVisualStyleBackColor = false;
            this.pickFolderBT.Click += new System.EventHandler(this.pickFolderBT_Click);
            // 
            // infoLB
            // 
            this.infoLB.AutoSize = true;
            this.infoLB.Location = new System.Drawing.Point(66, 38);
            this.infoLB.Name = "infoLB";
            this.infoLB.Size = new System.Drawing.Size(370, 13);
            this.infoLB.TabIndex = 7;
            this.infoLB.Text = "Please enter below the path where the tool will be installed or click \"Browse\".";
            this.infoLB.UseWaitCursor = true;
            // 
            // iconPB
            // 
            this.iconPB.Location = new System.Drawing.Point(15, 9);
            this.iconPB.Name = "iconPB";
            this.iconPB.Size = new System.Drawing.Size(42, 42);
            this.iconPB.TabIndex = 6;
            this.iconPB.TabStop = false;
            this.iconPB.Visible = false;
            // 
            // InstallAnnouncement
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(572, 123);
            this.Controls.Add(this.infoLB);
            this.Controls.Add(this.iconPB);
            this.Controls.Add(this.pickFolderBT);
            this.Controls.Add(this.folderLA);
            this.Controls.Add(this.toolPathTB);
            this.Controls.Add(this.InstallToolLB);
            this.Controls.Add(this.prevBT);
            this.Controls.Add(this.nextBT);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "InstallAnnouncement";
            this.ShowInTaskbar = false;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "GLLegacy Auto Installer";
            this.Load += new System.EventHandler(this.InstallAnnouncement_Load);
            ((System.ComponentModel.ISupportInitialize)(this.iconPB)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button nextBT;
        private System.Windows.Forms.Button prevBT;
        private System.Windows.Forms.Label InstallToolLB;
        private System.Windows.Forms.TextBox toolPathTB;
        private System.Windows.Forms.Label folderLA;
        private System.Windows.Forms.FolderBrowserDialog folderBrowser;
        private System.Windows.Forms.Button pickFolderBT;
        private System.Windows.Forms.PictureBox iconPB;
        private System.Windows.Forms.Label infoLB;
    }
}