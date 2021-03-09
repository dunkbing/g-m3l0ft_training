namespace PlatformTabPage
{
    partial class UserControl1
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
            this.pictureBox = new System.Windows.Forms.PictureBox();
            this.labelGridENV = new System.Windows.Forms.Label();
            this.splitContainer = new System.Windows.Forms.SplitContainer();
            this.gridENV = new SourceGrid.Grid();
            this.gridCONFIG = new SourceGrid.Grid();
            this.labelGridCONFIG = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox)).BeginInit();
            this.splitContainer.Panel1.SuspendLayout();
            this.splitContainer.Panel2.SuspendLayout();
            this.splitContainer.SuspendLayout();
            this.SuspendLayout();
            // 
            // pictureBox
            // 
            this.pictureBox.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.pictureBox.Location = new System.Drawing.Point(530, 0);
            this.pictureBox.Name = "pictureBox";
            this.pictureBox.Size = new System.Drawing.Size(115, 115);
            this.pictureBox.TabIndex = 0;
            this.pictureBox.TabStop = false;
            // 
            // labelGridENV
            // 
            this.labelGridENV.AutoSize = true;
            this.labelGridENV.Font = new System.Drawing.Font("Microsoft Sans Serif", 14.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelGridENV.Location = new System.Drawing.Point(2, 94);
            this.labelGridENV.Name = "labelGridENV";
            this.labelGridENV.Size = new System.Drawing.Size(141, 24);
            this.labelGridENV.TabIndex = 1;
            this.labelGridENV.Text = "Name of grid1";
            // 
            // splitContainer
            // 
            this.splitContainer.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.splitContainer.Location = new System.Drawing.Point(3, 121);
            this.splitContainer.Name = "splitContainer";
            // 
            // splitContainer.Panel1
            // 
            this.splitContainer.Panel1.Controls.Add(this.gridENV);
            // 
            // splitContainer.Panel2
            // 
            this.splitContainer.Panel2.Controls.Add(this.gridCONFIG);
            this.splitContainer.Size = new System.Drawing.Size(639, 198);
            this.splitContainer.SplitterDistance = 313;
            this.splitContainer.TabIndex = 2;
            // 
            // gridENV
            // 
            this.gridENV.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.gridENV.EnableSort = true;
            this.gridENV.Location = new System.Drawing.Point(3, 3);
            this.gridENV.Name = "gridENV";
            this.gridENV.OptimizeMode = SourceGrid.CellOptimizeMode.ForRows;
            this.gridENV.SelectionMode = SourceGrid.GridSelectionMode.Cell;
            this.gridENV.Size = new System.Drawing.Size(307, 192);
            this.gridENV.TabIndex = 0;
            this.gridENV.TabStop = true;
            this.gridENV.ToolTipText = "";
            // 
            // gridCONFIG
            // 
            this.gridCONFIG.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.gridCONFIG.EnableSort = true;
            this.gridCONFIG.Location = new System.Drawing.Point(3, 3);
            this.gridCONFIG.Name = "gridCONFIG";
            this.gridCONFIG.OptimizeMode = SourceGrid.CellOptimizeMode.ForRows;
            this.gridCONFIG.SelectionMode = SourceGrid.GridSelectionMode.Cell;
            this.gridCONFIG.Size = new System.Drawing.Size(316, 192);
            this.gridCONFIG.TabIndex = 0;
            this.gridCONFIG.TabStop = true;
            this.gridCONFIG.ToolTipText = "";
            // 
            // labelGridCONFIG
            // 
            this.labelGridCONFIG.AutoSize = true;
            this.labelGridCONFIG.Font = new System.Drawing.Font("Microsoft Sans Serif", 14.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.labelGridCONFIG.Location = new System.Drawing.Point(327, 94);
            this.labelGridCONFIG.Name = "labelGridCONFIG";
            this.labelGridCONFIG.Size = new System.Drawing.Size(141, 24);
            this.labelGridCONFIG.TabIndex = 3;
            this.labelGridCONFIG.Text = "Name of grid2";
            // 
            // UserControl1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.labelGridCONFIG);
            this.Controls.Add(this.splitContainer);
            this.Controls.Add(this.labelGridENV);
            this.Controls.Add(this.pictureBox);
            this.Name = "UserControl1";
            this.Size = new System.Drawing.Size(645, 322);
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox)).EndInit();
            this.splitContainer.Panel1.ResumeLayout(false);
            this.splitContainer.Panel2.ResumeLayout(false);
            this.splitContainer.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        public System.Windows.Forms.PictureBox pictureBox;
        public System.Windows.Forms.SplitContainer splitContainer;
        public SourceGrid.Grid gridENV;
        public SourceGrid.Grid gridCONFIG;
        public System.Windows.Forms.Label labelGridENV;
        public System.Windows.Forms.Label labelGridCONFIG;
    }
}
