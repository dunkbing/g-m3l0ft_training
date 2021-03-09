using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using System.Xml;
using GLLegacyInstaller;
using System.Threading;
using System.Diagnostics;
using BuildConfiguratorV2;
using System.Media;
using Utils;
using Microsoft.Win32;


namespace BuildConfigurator
{
	public partial class BuildConfigurator : Form
	{
		SoundPlayer soundPlayer;

		static public BuildConfigurator s_instance = null;


		const String c_XML_root = "BuildConfigurator";
		const String c_str_Commons = "Commons";
		const String c_str_GLLegacyInstaller = "GLLegacyInstaller";
		const String c_str_TrackingTestTool = "TrackingTestTool";
		const String c_str_ProjectSettings = "ProjectSettings";
		const String c_str_Notepad = "Notepad";
		const String c_str_Diff = "Diff";
		const String c_str_Platforms = "Platforms";
		const String c_str_Platform = "Platform";
		const String c_str_Platform_Name = "name";


		public bool IsPlatformSelected(string platform)
		{
			foreach (string p in Globals.s_platforms)
			{
				if (p.ToUpper() == platform.ToUpper())
				{
					return true;
				}
			}
			return false;
		}

		public BuildConfigurator()
		{
			if (s_instance == null)
			{
				s_instance = this;
			}

			Globals.labelNoBuilds = new TextBox();
			Globals.labelNoBuilds.AutoSize = true;
			Globals.labelNoBuilds.BackColor = System.Drawing.SystemColors.Control;
			Globals.labelNoBuilds.BorderStyle = BorderStyle.None;
			Globals.labelNoBuilds.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
			Globals.labelNoBuilds.Location = new System.Drawing.Point(1000, 30);
			Globals.labelNoBuilds.Name = "label2";
			Globals.labelNoBuilds.Size = new System.Drawing.Size(67, 15);
			Globals.labelNoBuilds.TextAlign = HorizontalAlignment.Right;
			Globals.labelNoBuilds.TabIndex = 14;
			Globals.labelNoBuilds.Text = "0 builds";
			Globals.labelNoBuilds.Anchor = (System.Windows.Forms.AnchorStyles)(System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right);
			
			InitializeComponent();

			tabControlMain.SelectedIndex = 1;

			Globals.stars[0] = star08;
			Globals.stars[1] = star09;
			Globals.stars[2] = star10;
			Globals.stars[3] = star11;
			Globals.stars[4] = star12;
			Globals.stars[5] = star13;
			Globals.stars[6] = star14;
			Globals.stars[7] = star15;

			m_version.Text = Globals.s_version;
			labelVersion.Text = Globals.s_version;

			soundPlayer = new SoundPlayer();
			this.Controls.Add(Globals.labelNoBuilds);
			Globals.labelNoBuilds.BringToFront();


			const string userRoot = "HKEY_CURRENT_USER";
			const string subkey = "BuildConfigurator";
			const string keyName = userRoot + "\\" + subkey;
			int val = 0;

			try
			{
				val = (int)Registry.GetValue(keyName, "", 0);
			}
			catch (Exception)
			{
				Registry.SetValue(keyName, "", 0, RegistryValueKind.DWord);
			}
			Globals.labelNoBuilds.Text = val + " builds";
			Utils.Globals.AddStars(val);

			val = 0;
			try
			{
				val = (int)Registry.GetValue(keyName, "SoundMute", 0);
			}
			catch (Exception)
			{
				Registry.SetValue(keyName, "SoundMute", 0, RegistryValueKind.DWord);
			}

			if (val == 1)
			{
				Utils.Globals.isSoundEnabled = false;
				buttonIsSoundMute.Visible = true;
				buttonIsSoundEnabled.Visible = false;
			}
		}


		public bool LoadSettings()
		{
			Console.WriteLine("\nLoadSettings...");

			// AF/tools/BuildConfigurator/bin/BuildConfigurator.exe
			string currentFileName = Process.GetCurrentProcess().Modules[0].FileName;

			// AF/tools/BuildConfigurator/bin
			string currentPath = Path.GetDirectoryName(currentFileName);

			// AF/tools/BuildConfigurator
			Globals.s_buildConfiguratorPath = Path.GetFullPath(currentPath + "\\..");

			// AF/tools
			Globals.s_afToolsPath = Path.GetFullPath(Globals.s_buildConfiguratorPath + "\\..");

			// AF
			Globals.s_androidFrameworkPath = Path.GetFullPath(Globals.s_afToolsPath + "\\..");

			this.Text = Globals.s_androidFrameworkPath;
			Globals.s_afConfigPath = Path.GetFullPath(Globals.s_androidFrameworkPath + "\\..\\AndroidFrameworkConfig");
			Globals.s_gameSpecificPath = Globals.s_afConfigPath;
			//Environment.SetEnvironmentVariable("ANDROID_FRAMEWORK_CONFIG", Globals.s_gameSpecificPath, EnvironmentVariableTarget.Process);
			//Environment.SetEnvironmentVariable("ANDROID_FRAMEWORK_CONFIG", Globals.s_gameSpecificPath, EnvironmentVariableTarget.Process);


			// AF/tools/GLLegacyInstaller/bin/GLLegacyInstaller.exe
			Globals.s_installerPath = Path.GetFullPath(Path.Combine(Globals.s_afToolsPath, "GLLegacyInstaller", "bin", "GLLegacyInstaller.exe"));
			Globals.s_installerCommand = Globals.s_installerPath + " InstallerSetup.xml";
			// TODO: check installer setup thingy

			// AF/tools/TrackingTestTool/LibSpy.exe
			Globals.s_trackingToolPath = Path.GetFullPath(Path.Combine(Globals.s_afToolsPath, "TrackingTestTool", "LibSpy.exe"));
			Globals.s_TrackingTestToolCommand = Globals.s_trackingToolPath;

			// AF/tools/Notepad/notepad++.exe
			Globals.s_notepad = Path.GetFullPath(Path.Combine(Globals.s_afToolsPath, "Notepad", "notepad++.exe"));

			// AF/tools/DDMS/ddms_windows.bat
			Globals.s_ddmsPath = Path.GetFullPath(Path.Combine(Globals.s_afToolsPath, "DDMS", "ddms_windows.bat"));

			// AF/tools/svn18/TortoiseProc.exe
			Globals.s_diff = Path.GetFullPath(Path.Combine(Globals.s_afToolsPath, "svn18", "TortoiseProc.exe"));

			// AF/tools/svn18/TortoiseProc.exe
			Globals.s_configureToolPath = Path.GetFullPath(Path.Combine(Globals.s_afToolsPath, "configureTools.bat"));

			if (!File.Exists(Globals.s_diff))
			{
				Globals.s_diff = null;
			}

			PlatformTabPage.PlatformTabPage.s_notepad = Globals.s_notepad;
			PlatformTabPage.PlatformTabPage.s_diff = Globals.s_diff;


			Globals.s_projectName = "Project";
			LoadPlatform();

			Console.WriteLine("LoadSettings...OK");

			return true;
		}


		private void LoadPlatform()
		{
			//TabPage tc = new TabPage("Android");
			//this.tabControlGame.TabPages.Add(tc);

			PlatformTabPage.PlatformTabPage ptb = platformTabPage;// new PlatformTabPage.PlatformTabPage();
			ptb.PlatformName = "Android";

			ptb.m_PlatformColor = Color.Olive;

			ptb.RUN_Command = Globals.s_ddmsPath;
			ptb.RUN_Command_Label = "DDMS";
			ptb.CHECK_Tools = Globals.s_configureToolPath;

			//ptb.StartBuildImage = global::BuildConfiguratorV2.Properties.Resources.RunConsole;
			ptb.OPEN_Solution = Globals.ReplaceEnvironmentVariables("$(VCSLN)");

			ptb.ENV_FileName = Path.Combine(Globals.s_gameSpecificPath, "configs", "project", "setEnv.bat");
			ptb.CONFIG_FileName = Path.Combine(Globals.s_gameSpecificPath, "config.bat");
			ptb.BUILD_FileName = Path.Combine(Globals.s_gameSpecificPath, "configs", "generated", "buildConfigH.bat");

			ptb.MAKE_Command = Path.Combine(Globals.s_androidFrameworkPath, "make.bat");
			ptb.INSTALL_Command = Path.Combine(Globals.s_androidFrameworkPath, "install.bat");
			ptb.CLEAN_Command = Path.Combine(Globals.s_androidFrameworkPath, "clean.bat");

			ptb.HEADER_FileName = Path.Combine(Globals.s_afConfigPath, "configs", "generated", "config_Android.h");

			ptb.labelConfigsFolder.Text = Path.Combine(Globals.s_gameSpecificPath, "configs", "project");
			ptb.labelGeneratedFolder.Text = Path.Combine(Globals.s_gameSpecificPath, "configs", "generated");

			ptb.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top
																| System.Windows.Forms.AnchorStyles.Bottom)
																| System.Windows.Forms.AnchorStyles.Left)
																| System.Windows.Forms.AnchorStyles.Right)));
			ptb.AutoSize = false;
			ptb.Location = new System.Drawing.Point(0, 0);
			//ptb.Size = tc.Size;           
		}

		private void BuildConfigurator_Load(object sender, EventArgs e)
		{
			LoadSettings();
		}

		private void buttonStartGLLegacyInstaller_Click(object sender, EventArgs e)
		{
			try
			{
				string cmd = Globals.s_installerCommand;
				string command = cmd;
				string args = "";
				int index = cmd.IndexOf(" ");
				if (index > 0)
				{
					command = cmd.Substring(0, index);
					args = cmd.Substring(index, cmd.Length - index);
				}


				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = Globals.s_gameSpecificPath;
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = args;



				proc.Start();
				//proc.WaitForExit();

			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}


		private void buttonStartTrackingTestTool_Click(object sender, EventArgs e)
		{
			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = Globals.s_gameSpecificPath;
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = Globals.s_TrackingTestToolCommand;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.UseShellExecute = true;

				//proc.StartInfo.Arguments = args;

				proc.Start();
				//proc.WaitForExit();

			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}

		}

		private void BuildConfigurator_FormClosed(object sender, FormClosedEventArgs e)
		{
			//foreach(TabPage platform in tabControlGame.TabPages)
			//{
			PlatformTabPage.PlatformTabPage ptb = platformTabPage;// (PlatformTabPage.PlatformTabPage)platform.Controls[0];

			for (int i = 0; i < ptb.m_fileWasChanged.Length; i++)
			{
				bool changes = ptb.m_fileWasChanged[i];
				if (changes)
				{
					ptb.SaveWithConfirmation(i);
				}
			}
			//}
		}

		private void exitToolStripMenuItem_Click(object sender, EventArgs e)
		{
			this.Close();
		}

		private void buttonConfigFileBrowse_Click(object sender, EventArgs e)
		{
			string command = "Explorer.exe";
			string args = Globals.s_gameSpecificPath;

			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				//proc.EnableRaisingEvents = false;
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = args;

				proc.Start();

			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}


		void tabControlMain_SelectedIndexChanged(object sender, System.EventArgs e)
		{
			if (tabControlMain.SelectedIndex > 0)
			{
				if (Utils.Globals.isSoundEnabled)
				{
					try
					{
						soundPlayer.SoundLocation = Path.Combine(Path.GetDirectoryName(Process.GetCurrentProcess().MainModule.FileName), "StartApplication.wav");
						soundPlayer.Play();
					}
					catch (Exception)
					{ }
				}
			}
		}


		private void aboutToolStripMenuItem_Click(object sender, EventArgs e)
		{
			About about = new About();
			about.ShowDialog();
		}

		private void openDocsToolStripMenuItem_Click(object sender, EventArgs e)
		{
			Process.Start("https://docs.gameloft.org/gllegacy-how-to-compile-sandbox/");
		}

		private void tutorialToolStripMenuItem_Click(object sender, EventArgs e)
		{
			Tutorial tt = new Tutorial();
			tt.Show(this);
		}

		private void button1_Click(object sender, EventArgs e)
		{
			buttonIsSoundMute.Visible = true;
			buttonIsSoundEnabled.Visible = false;
			Utils.Globals.isSoundEnabled = false;

			const string userRoot = "HKEY_CURRENT_USER";
			const string subkey = "BuildConfigurator";
			const string keyName = userRoot + "\\" + subkey;
			try
			{
				Registry.SetValue(keyName, "SoundMute", 1, RegistryValueKind.DWord);
			}
			catch (Exception)
			{ }
		}

		private void button2_Click(object sender, EventArgs e)
		{
			buttonIsSoundEnabled.Visible = true;
			buttonIsSoundMute.Visible = false;
			Utils.Globals.isSoundEnabled = true;

			const string userRoot = "HKEY_CURRENT_USER";
			const string subkey = "BuildConfigurator";
			const string keyName = userRoot + "\\" + subkey;
			try
			{
				Registry.SetValue(keyName, "SoundMute", 0, RegistryValueKind.DWord);
			}
			catch (Exception)
			{ }

		}



	}
}
