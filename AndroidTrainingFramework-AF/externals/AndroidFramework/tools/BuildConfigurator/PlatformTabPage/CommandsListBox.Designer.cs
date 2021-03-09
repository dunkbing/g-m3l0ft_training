namespace PlatformTabPage
{
    partial class CommandsListBox
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(CommandsListBox));
            this.messageList = new System.Windows.Forms.RichTextBox();
            this.SuspendLayout();
            // 
            // messageList
            // 
            this.messageList.AccessibleDescription = "ViewCommands";
            this.messageList.BackColor = System.Drawing.SystemColors.ControlLight;
            this.messageList.Dock = System.Windows.Forms.DockStyle.Fill;
            this.messageList.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.messageList.Location = new System.Drawing.Point(0, 0);
            this.messageList.Name = "messageList";
            this.messageList.ReadOnly = true;
            this.messageList.ScrollBars = System.Windows.Forms.RichTextBoxScrollBars.Vertical;
            this.messageList.Size = new System.Drawing.Size(661, 116);
            this.messageList.TabIndex = 0;
            this.messageList.Text = "";
            // 
            // CommandsListBox
            // 
            this.AccessibleDescription = "ViewCommandsForm";
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(661, 116);
            this.Controls.Add(this.messageList);
            this.ForeColor = System.Drawing.SystemColors.ControlText;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "CommandsListBox";
            this.ShowInTaskbar = false;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Load += new System.EventHandler(this.CommandsListBox_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.RichTextBox messageList;

    }
}