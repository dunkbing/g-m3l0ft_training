using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using SourceGrid;
using System.Threading;
using System.Diagnostics;
using System.Media;
using Utils;
using Microsoft.Win32;


namespace PlatformTabPage
{


	public partial class PlatformTabPage : UserControl
	{
		SoundPlayer soundPlayer;
		string 		outputPath;

		public static String s_notepad = "notepad++.exe";
		public static String s_diff = "TortoiseProc.exe";

		private String m_make_command = "make.bat";
		private String m_clean_command = "clean.bat";
		private String m_open_solution = "";
		private String m_run_command = "";
		private String m_run_command_label = "";
		public String m_check_tools = "";
		private String m_install_command = "";


		private static String s_str_backupEXT = ".backup";
		private static String s_str_buildConfiguration = "$(BUILD_CONFIGURATION)";
		private static String s_str_release_version = "RELEASE_VERSION";
		public Color m_PlatformColor;
		List<string> m_executedCommands = new List<string>();

		//String m_workingDir = null;
		String m_baseDir = null;
		String m_PlatformName = null;
		String m_commandToGenerateConfigH = null;
		String m_stringToFind = "";

		enum INPUT_FILE_TYPE
		{
			ENV = 0,
			CFG = 1
		};

		SourceGrid.Cells.Views.Cell disableView;
		SourceGrid.Cells.Views.Cell normalView;
		SourceGrid.Cells.Views.CheckBox checkBoxDisableView;
		SourceGrid.Cells.Views.CheckBox checkBoxView;
		SourceGrid.Cells.Views.ComboBox comboBoxDisableView;
		String[] fileContent;
		List<int>[] nodeTabs;
		List<int>[] parentNode;
		List<bool>[] disabledNode;

		List<int>[] gridLines;
		List<string>[] lineType;
		public List<string>[] mSettingsLineList;
		public bool[] m_fileWasChanged;
		bool[] m_fileWasChangedOutside;
		DateTime[] m_fileTimeStamp;
		int[] m_beginGameSpecific;
		Point m_obedientScrollCFG;
		Point m_obedientScrollENV;
		Position m_SelCellPosCFG;
		Position m_SelCellPosENV;
		//CommandsListBox m_CommandListBox;

		private void InitializeComponentEx()
		{			
			mSettingsLineList = new List<string>[2];
			m_obedientScrollCFG = new Point(0, 0);
			m_obedientScrollENV = new Point(0, 0);
			m_SelCellPosCFG = new Position(0, 0);
			m_SelCellPosENV = new Position(0, 0);

			mSettingsLineList[(int)INPUT_FILE_TYPE.ENV] = new List<string>();
			mSettingsLineList[(int)INPUT_FILE_TYPE.CFG] = new List<string>();

			/******************************   grid views   *************************************/
			disableView = new SourceGrid.Cells.Views.Cell();
			disableView.BackColor = Color.Silver;

			checkBoxDisableView = new SourceGrid.Cells.Views.CheckBox();
			checkBoxDisableView.CheckBoxAlignment = DevAge.Drawing.ContentAlignment.TopLeft;
			checkBoxDisableView.BackColor = Color.Silver;

			comboBoxDisableView = new SourceGrid.Cells.Views.ComboBox();
			comboBoxDisableView.BackColor = Color.Silver;

			normalView = new SourceGrid.Cells.Views.Cell();
			normalView.BackColor = Color.White;

			checkBoxView = new SourceGrid.Cells.Views.CheckBox();
			checkBoxView.CheckBoxAlignment = DevAge.Drawing.ContentAlignment.TopLeft;
			checkBoxView.BackColor = Color.White;

			fileContent = new String[2];
			m_fileWasChanged = new bool[2];
			m_fileWasChanged[(int)INPUT_FILE_TYPE.ENV] = false;
			m_fileWasChanged[(int)INPUT_FILE_TYPE.CFG] = false;


			m_fileWasChangedOutside = new bool[2];
			m_fileWasChangedOutside[(int)INPUT_FILE_TYPE.ENV] = false;
			m_fileWasChangedOutside[(int)INPUT_FILE_TYPE.CFG] = false;


			m_fileTimeStamp = new DateTime[2];

			m_beginGameSpecific = new int[2];
			m_beginGameSpecific[(int)INPUT_FILE_TYPE.ENV] = -1;
			m_beginGameSpecific[(int)INPUT_FILE_TYPE.CFG] = -1;


			nodeTabs = new List<int>[2];
			nodeTabs[(int)INPUT_FILE_TYPE.ENV] = new List<int>();
			nodeTabs[(int)INPUT_FILE_TYPE.CFG] = new List<int>();

			parentNode = new List<int>[2];
			parentNode[(int)INPUT_FILE_TYPE.ENV] = new List<int>();
			parentNode[(int)INPUT_FILE_TYPE.CFG] = new List<int>();

			disabledNode = new List<bool>[2];
			disabledNode[(int)INPUT_FILE_TYPE.ENV] = new List<bool>();
			disabledNode[(int)INPUT_FILE_TYPE.CFG] = new List<bool>();

			gridLines = new List<int>[2];
			gridLines[(int)INPUT_FILE_TYPE.ENV] = new List<int>();
			gridLines[(int)INPUT_FILE_TYPE.CFG] = new List<int>();

			lineType = new List<string>[2];
			lineType[(int)INPUT_FILE_TYPE.ENV] = new List<string>();
			lineType[(int)INPUT_FILE_TYPE.CFG] = new List<string>();
		}


		public PlatformTabPage()
		{
			InitializeComponent();
			InitializeComponentEx();

			timerRefresh.Enabled = true;
			timerRefresh.Start();
			soundPlayer = new SoundPlayer();
		}


		/**
		 * This is the path and filename to the setEnv.bat file .
		 * This file contains all the settings that are computer dependent (ex: paths to installed tools)
		 */
		public String ENV_FileName
		{
			get
			{
				return env_FileName;
			}
			set
			{
				env_FileName = value;

				bool fileExists = ((value != null) && (value != "") && (File.Exists(value)));

				//this.labelPathENVFile.Text = value;
				//this.labelGridENVFile.Text = Path.GetFileName(value);

				//this.GridEnvOptions.Enabled = fileExists;
				//this.GridConfigOptions.Selection.FocusStyle = FocusStyle.RemoveSelectionOnLeave;
				//this.buttonENVNotepad.Enabled = fileExists;
				//this.textBoxENV.Enabled = fileExists;
				////this.buttonENVFileDiff1.Enabled = fileExists;
				////this.buttonENVFileDiff2.Enabled = fileExists;
				//this.buttonENVFileSave.Enabled = fileExists;

				//this.buttonENVFileBrowse.Enabled = fileExists;
				////this.buttonENVFileOpen.Enabled = fileExists;

				if (fileExists)
				{
					LoadFile((int)INPUT_FILE_TYPE.ENV, value);
					ParseFileAndFillGrid((int)INPUT_FILE_TYPE.ENV, m_stringToFind);
				}

			}
		}

		string env_FileName;


		/**
		 * This is the path and filename to the config.bat file .
		 * This file contains all the settings that are game specific.
		 */
		public String CONFIG_FileName
		{
			get
			{
				return config_FileName;
			}
			set
			{
				config_FileName = value;

				bool fileExists = ((value != null) && (value != "") && (File.Exists(value)));

				//this.labelPathCONFIGFile.Text = value;
				this.labelGridCFGFile.Text = Path.GetFileName(value);


				this.GridConfigOptions.Enabled = fileExists;
				this.GridConfigOptions.Selection.FocusStyle = FocusStyle.RemoveSelectionOnLeave;
				this.buttonCFGFileNotepad.Enabled = fileExists;
				this.textBoxCFG.Enabled = fileExists;
				//this.buttonCONFIGFileDiff1.Enabled = fileExists;
				//this.buttonCFGFileDiff2.Enabled = fileExists;
				this.buttonCFGFileSave.Enabled = fileExists;

				//this.buttonCONFIGFileBrowse.Enabled = fileExists;
				//this.buttonCONFIGFileOpen.Enabled = fileExists;


				if (fileExists)
				{
					LoadFile((int)INPUT_FILE_TYPE.CFG, value);
					ParseFileAndFillGrid((int)INPUT_FILE_TYPE.CFG, m_stringToFind);
				}


			}
		}

		string config_FileName;

		public String PlatformName
		{
			get
			{
				return m_PlatformName;
			}
			set
			{
				m_PlatformName = value;
			}
		}

		/**
		* 
		*/
		public String BUILD_FileName
		{
			get
			{
				return build_FileName;
			}
			set
			{
				build_FileName = value;
			}
		}

		string build_FileName;


		/**
		* 
		*/
		public String MAKE_Command
		{
			get
			{
				return this.m_make_command;
			}
			set
			{
				if (!string.IsNullOrEmpty(value))
				{
					this.m_make_command = value;
					this.buttonBuild.Enabled = true;
				}
				else
				{
					this.buttonBuild.Enabled = false;
				}
			}
		}


		/**
		* 
		*/
		public String INSTALL_Command
		{
			get
			{
				return this.m_install_command;
			}
			set
			{
				if (!string.IsNullOrEmpty(value))
				{
					this.m_install_command = value;
					this.buttonInstall.Enabled = true;
				}
				else
				{
					this.buttonInstall.Enabled = false;
				}
			}
		}

		/**
		* 
		*/
		public String CLEAN_Command
		{
			get
			{
				if (this.m_clean_command == "none" || this.m_clean_command.Length == 0)
				{
					buttonClean.Enabled = false;
				}
				else
				{
					buttonClean.Enabled = true;
				}
				return this.m_clean_command;
			}
			set
			{
				if (value != null)
					this.m_clean_command = value;
				if (this.m_clean_command == "none" || this.m_clean_command.Length == 0)
				{
					buttonClean.Enabled = false;
				}
				else
				{
					buttonClean.Enabled = true;
				}
			}
		}

		/**
		* 
		*/
		public String OPEN_Solution
		{
			get
			{
				return this.m_open_solution;
			}
			set
			{
				if (value != null)
					this.m_open_solution = value;
			}
		}


		/**
		 * this is the working directory where the console will be opened and where the build commands will be executed.
		 */
		public String RUN_Command
		{
			get
			{
				return this.m_run_command;
			}
			set
			{
				if (value != null)
					this.m_run_command = value;
			}

		}

		/**
		 * this is the working directory where the console will be opened and where the build commands will be executed.
		 */
		public String RUN_Command_Label
		{
			get
			{
				return this.m_run_command_label;
			}
			set
			{
				if (value != null)
					this.m_run_command_label = value;
			}

		}

		/**
		 * this is the working directory where the checktools.bat will be executed.
		 */
		public String CHECK_Tools
		{
			get
			{
				return this.m_check_tools;
			}
			set
			{
				if (value != null)
					this.m_check_tools = value;
			}

		}

		/**
		 * 
		 */
		public String HEADER_FileName
		{
			get
			{
				return header_FileName;
			}
			set
			{
				header_FileName = value;
			}
		}

		string header_FileName;


		/**
		 * This is an Image that represent the platform.
		 * Must be a logo for this platform.
		 */
		public Image PlatformImage
		{
			get
			{
				return this.pictureBox.Image;
			}
			set
			{
				this.pictureBox.Image = value;
			}
		}


		/**
		 * This parameter represent the path and file for a batch script (*.bat) that will
		 * generate the config*.h file.
		 */
		public String CommandToGenerateConfigH
		{
			get
			{
				return this.m_commandToGenerateConfigH;
			}
			set
			{
				this.m_commandToGenerateConfigH = value;
			}
		}


		private void timerRefresh_Tick(object sender, EventArgs e)
		{

			#region Check_If_files_was_changed_outside
			String fname = ENV_FileName;
			CheckFileForChanges((int)INPUT_FILE_TYPE.ENV, fname);
			if (m_fileWasChanged[(int)INPUT_FILE_TYPE.ENV])
			{
				//buttonENVSave.Enabled = true;
				//buttonENVFileDiff1.Enabled = true;
				//buttonENVFileDiff2.Enabled = true;
				//textBoxENV.Enabled = true;
				labelGridENVFile.ForeColor = Color.Red;
				labelConfigsFolder.ForeColor = Color.Red;

				if (buttonENVFileSave.ForeColor == Color.Red)
					buttonENVFileSave.ForeColor = Color.Black;
				else
					buttonENVFileSave.ForeColor = Color.Red;
			}
			else
			{
				//buttonENVFileDiff2.Enabled = false;
				labelGridENVFile.ForeColor = Color.Black;
				labelConfigsFolder.ForeColor = Color.Black;
				buttonENVFileSave.ForeColor = Color.Black;
			}


			fname = CONFIG_FileName;
			CheckFileForChanges((int)INPUT_FILE_TYPE.CFG, fname);
			if (m_fileWasChanged[(int)INPUT_FILE_TYPE.CFG])
			{
				labelGridCFGFile.ForeColor = Color.Red;

				if (buttonCFGFileSave.ForeColor == Color.Red)
					buttonCFGFileSave.ForeColor = Color.Black;
				else
					buttonCFGFileSave.ForeColor = Color.Red;
			}
			else
			{
				labelGridCFGFile.ForeColor = Color.Black;
				buttonCFGFileSave.ForeColor = Color.Black;
			}
			#endregion
		}

		private void textBoxFilter_TextChanged(object sender, EventArgs e)
		{
			m_stringToFind = ((TextBox)sender).Text;
			if (this.GridEnvOptions.Enabled == true)
			{
				UpdateFileContent((int)INPUT_FILE_TYPE.ENV);
				ParseFileAndFillGrid((int)INPUT_FILE_TYPE.ENV, m_stringToFind);
				this.GridEnvOptions.Redim(this.GridConfigOptions.RowsCount, this.GridConfigOptions.ColumnsCount);
			}

			if (this.GridConfigOptions.Enabled == true)
			{
				UpdateFileContent((int)INPUT_FILE_TYPE.CFG);
				ParseFileAndFillGrid((int)INPUT_FILE_TYPE.CFG, m_stringToFind);
				this.GridConfigOptions.Redim(this.GridConfigOptions.RowsCount, this.GridConfigOptions.ColumnsCount);
			}
		}

		private void buttonOpenConsole_Click(object sender, EventArgs e)
		{
			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(Globals.s_gameSpecificPath);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = "cmd.exe";
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = "";


				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				//suspendedInputMessage.Show(this);                
				proc.Start();
				//proc.WaitForExit();
				//suspendedInputMessage.Close();
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}


		public void Build(string typeOfBuild, string architecture, bool pause)
		{
			GenerateBUILDFile(BUILD_FileName);


			if (checkARM.Checked == false && checkX86.Checked == false)
			{
				MessageBox.Show("Please select at least one architecture (arm or x86)!", "BuildConfigurator - Warning", MessageBoxButtons.OK, MessageBoxIcon.Warning);
				return;
			}

			if (m_fileWasChanged[(int)INPUT_FILE_TYPE.ENV])
			{
				SaveWithConfirmation((int)INPUT_FILE_TYPE.ENV);
			}
			if (m_fileWasChanged[(int)INPUT_FILE_TYPE.CFG])
			{
				SaveWithConfirmation((int)INPUT_FILE_TYPE.CFG);
			}


			string cmd = "";
			if (!Path.IsPathRooted(m_make_command))
				cmd = m_baseDir + m_make_command;
			else
				cmd = m_make_command;

			string command = cmd;
			string args = "";
			int index = cmd.IndexOf(" ");
			if (index > 0)
			{
				command = cmd.Substring(0, index);
				args = cmd.Substring(index, cmd.Length - index);
			}

			if (!File.Exists(command))
			{
				MessageBox.Show
					(
					  "The script file: \n\n '" + command + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			args += " " + typeOfBuild;
			args += " " + architecture;

			if (!pause)
			{
				args += " nopause";
			}

			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();

				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.Arguments = args;
				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				if (Globals.s_workInConsole)
				{
					outputPath = Path.Combine(Globals.s_androidFrameworkPath, "build_output.txt");
					try 
					{
						File.Delete(outputPath);
					}
					catch (Exception)
					{ }
					
					proc.StartInfo.RedirectStandardOutput = true;
					proc.StartInfo.RedirectStandardError = true;
					proc.StartInfo.UseShellExecute = false;
					proc.OutputDataReceived += proc_OutputDataReceived;
					proc.ErrorDataReceived += proc_OutputDataReceived;
				}
				else
				{
					proc.StartInfo.RedirectStandardOutput = false;
					proc.StartInfo.UseShellExecute = true;
				}

				proc.Start();

				if (Globals.s_workInConsole)
				{
					proc.BeginOutputReadLine();
					proc.WaitForExit();
				}
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}


		void proc_OutputDataReceived(object sender, DataReceivedEventArgs outLine)
		{
			Console.WriteLine(outLine.Data);
			try
			{
				File.AppendAllText(outputPath, outLine.Data + "\n");
			}
			catch (Exception)
			{ }
		}


		private void buttonBuild_Click(object sender, EventArgs e)
		{
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
				try
				{
					Registry.SetValue(keyName, "", 0, RegistryValueKind.DWord);
				}
				catch (Exception) { }
			}

			try
			{
				val++;
				Registry.SetValue(keyName, "", val, RegistryValueKind.DWord);
				Utils.Globals.labelNoBuilds.Text = val + " builds";

				Utils.Globals.AddStars(val);
			}
			catch (Exception)
			{ }


			String fname = BUILD_FileName;
			GenerateBUILDFile(fname);

			if (Utils.Globals.isSoundEnabled)
			{
				try
				{
					soundPlayer.SoundLocation = Path.Combine(Path.GetDirectoryName(Process.GetCurrentProcess().MainModule.FileName), "StartBuild.wav");
					soundPlayer.Play();
				}
				catch (Exception)
				{ }
			}

			string typeOfBuild = ((radioButtonRelease.Checked == true) ? "release" : "debug");
			string architecture = "arm";

			if (checkARM.Checked == true && checkX86.Checked == true)
				architecture = "all";
			else if (checkARM.Checked == true)
				architecture = "arm";
			else if (checkX86.Checked == true)
				architecture = "x86";

			Build(typeOfBuild, architecture, true);
		}

		private void SaveENV()
		{
			String fname = ENV_FileName;
			SaveFile((int)INPUT_FILE_TYPE.ENV, fname);
		}

		private void buttonENVSave_Click(object sender, EventArgs e)
		{
			SaveENV();
		}

		private void SaveCONFIG()
		{
			String fname = CONFIG_FileName;
			SaveFile((int)INPUT_FILE_TYPE.CFG, fname);
			UpdateFileContent((int)INPUT_FILE_TYPE.CFG);

			//fname = BUILD_FileName;
			//GenerateBUILDFile(fname);

			//fname = HEADER_FileName;
			//GenerateHEADERFile(fname, false);
		}

		private void buttonCONFIGSave_Click(object sender, EventArgs e)
		{
			SaveCONFIG();
		}


		public void SaveWithConfirmation(int fileType)
		{

			String message = "";

			if (fileType == (int)INPUT_FILE_TYPE.ENV)
			{
				message = "File \n\n '" + ENV_FileName + "'\n\n is not saved. Save it ?";
			}
			else if (fileType == (int)INPUT_FILE_TYPE.CFG)
			{
				message = "File \n\n '" + CONFIG_FileName + "'\n\n is not saved. Save it ?";
			}

			DialogResult result = MessageBox.Show(this, message, "Save ?", MessageBoxButtons.YesNo, MessageBoxIcon.Question);
			if (result == DialogResult.Yes)
			{
				if (fileType == (int)INPUT_FILE_TYPE.ENV)
				{
					SaveENV();
				}
				else if (fileType == (int)INPUT_FILE_TYPE.CFG)
				{
					SaveCONFIG();
				}
			}
		}


		private void buttonENVNotepad_Click(object sender, EventArgs e)
		{
			String fname = ENV_FileName;
			NotepadEdit((int)INPUT_FILE_TYPE.ENV, fname);
		}

		private void buttonCONFIGNotepad_Click(object sender, EventArgs e)
		{
			String fname = CONFIG_FileName;
			NotepadEdit((int)INPUT_FILE_TYPE.CFG, fname);
		}

		private void buttonENVDiff_Click(object sender, EventArgs e)
		{
			String fname = ENV_FileName;
			BackupFile((int)INPUT_FILE_TYPE.ENV, fname);
			Diff(fname, fname + s_str_backupEXT);
		}

		private void buttonCONFIGDiff_Click(object sender, EventArgs e)
		{
			if (s_diff == null)
			{
				MessageBox.Show
					(
					  "The path to Diff tool (aka TortoiseProc.exe) is wrong, or the tool is not installed! \n use GLLegacyInstaller to install the needed tools!",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			String fname = CONFIG_FileName;
			BackupFile((int)INPUT_FILE_TYPE.CFG, fname);
			Diff(fname, fname + s_str_backupEXT);
		}


		private void buttonENVFileBrowse_Click(object sender, EventArgs e)
		{
			String fname = ENV_FileName;
			OpenExplorer(Path.GetDirectoryName(fname));
		}

		private void buttonOpenGenerated_Click(object sender, EventArgs e)
		{
			String fname = BUILD_FileName;
			OpenExplorer(Path.GetDirectoryName(fname));
		}


		void OpenExplorer(String folderName)
		{
			if (!Directory.Exists(folderName))
			{
				MessageBox.Show
					(
					  "Folder: \n" + folderName + "\n does not exist!",
					  "Message",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);

				return;
			}

			//Console.WriteLine("WorkingDirectory " + m_workingDir);

			string command = "Explorer.exe";
			string args = folderName;

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

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();

			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}

		void LoadFile(int fileType, string fname)
		{
			StreamReader reader = new StreamReader(fname);
			if (reader != null)
			{
				fileContent[(int)fileType] = reader.ReadToEnd();

				m_fileTimeStamp[(int)fileType] = File.GetLastWriteTime(fname);

				m_fileWasChanged[fileType] = false;
				m_fileWasChangedOutside[fileType] = false;

				reader.Close();
			}
		}

		void SaveFile(int fileType, string fname)
		{
			if (!Globals.IsValidFilename(Path.GetFileName(fname)))
			{
				MessageBox.Show
					(
					  "The file: \n\n '" + fname + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			int lowp = -1;
			for (int l = mSettingsLineList[fileType].Count - 1; l > 0; l--)
			{
				if (mSettingsLineList[fileType][l] == "")
				{
					lowp = l;
				}
				else
				{
					break;
				}
			}
			if (lowp > 0)
			{
				lowp += 1;
				mSettingsLineList[fileType].RemoveRange(lowp, (mSettingsLineList[fileType].Count - lowp));
			}


			using (StreamWriter writer = new StreamWriter(fname))
			{
				foreach (string line in mSettingsLineList[fileType])
				{
					writer.WriteLine(line);
				}

				m_fileWasChanged[fileType] = false;
			}

			m_fileTimeStamp[(int)fileType] = File.GetLastWriteTime(fname);
		}

		void BackupFile(int fileType, string fname)
		{
			if (!Globals.IsValidFilename(Path.GetFileName(fname)))
			{
				MessageBox.Show
					(
					  "The file: \n\n '" + fname + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			using (StreamWriter writer = new StreamWriter(fname + s_str_backupEXT))
			{
				foreach (string line in mSettingsLineList[fileType])
				{
					writer.WriteLine(line);
				}
			}
		}

		void NotepadEdit(int fileType, String fname)
		{
			if (!Globals.IsValidFilename(Path.GetFileName(fname)))
			{
				MessageBox.Show
					(
					  "The file: \n\n '" + fname + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}


			string command = s_notepad;
			string args = fname;

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

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();

			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}

		void Diff(String fname1, String fname2)
		{
			//Console.WriteLine("WorkingDirectory " + m_workingDir);

			string command = s_diff;
			string args = "/command:diff /path:\"" + fname1 + "\" /path2:\"" + fname2 + "\"";

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

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}

		public void GenerateBUILDFile(String fname)
		{
			/// Only for config i will do this
			int fileType = (int)INPUT_FILE_TYPE.CFG;

			if (fileContent[fileType] == null) return;


			//before the build file is generated, i must create ca backup file for the existing file.
			if (File.Exists(fname))
			{
				if (File.Exists(fname + s_str_backupEXT))
					File.Delete(fname + s_str_backupEXT);

				File.Copy(fname, fname + s_str_backupEXT);
			}

			if (m_stringToFind != "")
				ParseFileAndFillGrid(fileType, "");

			Grid grid = this.GridConfigOptions;

			using (StreamWriter writer = new StreamWriter(fname))
			{
				for (int i = m_beginGameSpecific[fileType]; (i >= 0) && (i < grid.RowsCount); i++)
				{

					if (grid[i, 0].ColumnSpan > 1)
						continue;

					String value = "";
					if (grid[i, 2].View.GetType() == typeof(SourceGrid.Cells.Views.CheckBox))
						value = "(" + ((grid[i, 2].Value.ToString() == "True") ? 1 : 0) + ")";
					else
						value = "" + grid[i, 2].Value + "";

					writer.WriteLine("@echo #ifdef " + grid[i, 1].Value + "                          >> %ANDROID_CONFIG_FILE%");
					writer.WriteLine("@echo #undef " + grid[i, 1].Value + "                          >> %ANDROID_CONFIG_FILE%");
					writer.WriteLine("@echo #endif //" + grid[i, 1].Value + "                        >> %ANDROID_CONFIG_FILE%");
					writer.WriteLine("@echo #define " + grid[i, 1].Value + " " + value + "			 >> %ANDROID_CONFIG_FILE%");
					writer.WriteLine("@echo //                                                       >> %ANDROID_CONFIG_FILE%");
					writer.WriteLine("@echo //                                                       >> %ANDROID_CONFIG_FILE%");
					writer.WriteLine("@echo //                                                       >> %ANDROID_CONFIG_FILE%");
				}
				writer.WriteLine("\n\n");
			}



			if (m_stringToFind != "")
				ParseFileAndFillGrid(fileType, m_stringToFind);
		}

		public void GenerateHEADERFile(String fname, bool workInConsole)
		{
			//before the build file is generated, i must create ca backup file for the existing file.
			if (File.Exists(fname))
			{
				if (File.Exists(fname + s_str_backupEXT))
					File.Delete(fname + s_str_backupEXT);

				File.Copy(fname, fname + s_str_backupEXT);
			}

			if (!Globals.IsValidFilename(Path.GetFileName(fname)))
			{
				MessageBox.Show
					(
					  "The file: \n\n '" + fname + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			if (radioButtonRelease.Checked)
				Globals.AddEnvironmentVar(s_str_release_version, "1");
			else
				Globals.AddEnvironmentVar(s_str_release_version, "0");


			//Console.WriteLine("WorkingDirectory " + m_workingDir);            

			string cmd = "";
			if (!Path.IsPathRooted(m_make_command))
				cmd = m_baseDir + m_make_command;
			else
				cmd = m_make_command;

			string command = cmd;
			string args = "";
			int index = cmd.IndexOf(" ");
			if (index > 0)
			{
				command = cmd.Substring(0, index);
				args = cmd.Substring(index, cmd.Length - index);
			}

			args += " config all no_pause";

			if (!File.Exists(command))
			{
				MessageBox.Show
					(
					  "The script file: \n\n '" + command + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			try
			{
				SuspendedInput suspendedInputMessage = null;
				if (!workInConsole)
				{
					suspendedInputMessage = new SuspendedInput();
					Point p = new Point(this.ParentForm.Left + this.ParentForm.Width / 2 - suspendedInputMessage.Width / 2, this.ParentForm.Top + this.ParentForm.Height / 2 - suspendedInputMessage.Height / 2);
					suspendedInputMessage.Location = p;
				}

				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				//proc.EnableRaisingEvents = false;
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = false;
				proc.StartInfo.UseShellExecute = false;

				proc.StartInfo.Arguments = args;

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				if (!workInConsole)
				{
					suspendedInputMessage.Show(this);
					suspendedInputMessage.SetMessage("Saving Config.Bat, \r\n Please wait...");
				}
				proc.Start();
				proc.WaitForExit();
				if (!workInConsole)
				{
					suspendedInputMessage.Close();
				}
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}


		private void CheckFileForChanges(int fileType, string fname)
		{
			if (!Globals.IsValidFilename(Path.GetFileName(fname)))
			{
				return;
			}
			if (File.Exists(fname) == false) return;
			if (fileContent[fileType] != null)
			{
				DateTime timeStamp = File.GetLastWriteTime(fname);
				if ((!m_fileWasChangedOutside[fileType]) && timeStamp != m_fileTimeStamp[fileType])
				{
					timerRefresh.Stop();

					m_fileWasChangedOutside[fileType] = true;

					String message = "File " + fname + " changed outside! \n\n Reload ?";
					if (m_fileWasChanged[fileType])
					{
						message += "\n\n You will loose your changes!!!";
					}

					DialogResult result = MessageBox.Show(message, "BuildConfigurator - Warning", MessageBoxButtons.YesNo, MessageBoxIcon.Warning, MessageBoxDefaultButton.Button1, MessageBoxOptions.DefaultDesktopOnly);
					if (result == DialogResult.Yes)
					{
						LoadFile(fileType, fname);
						ParseFileAndFillGrid(fileType, m_stringToFind);

						fname = BUILD_FileName;
						GenerateBUILDFile(fname);

					}
					else
					{
						m_fileWasChanged[fileType] = true;
					}

					timerRefresh.Start();
				}
			}
		}

		private void UpdateFileContent(int tabIndex)
		{
			String[] tempFileContent = new String[2];
			foreach (String ln in mSettingsLineList[tabIndex])
			{
				tempFileContent[tabIndex] += ln + Environment.NewLine;
			}
			fileContent = tempFileContent;
		}

		private void ParseFileAndFillGrid(int tabIndex, string filter)
		{
			Grid grid = null;

			if ((int)INPUT_FILE_TYPE.ENV == tabIndex)
				grid = this.GridEnvOptions;
			else if ((int)INPUT_FILE_TYPE.CFG == tabIndex)
				grid = this.GridConfigOptions;


			if (nodeTabs[tabIndex] != null)
				nodeTabs[tabIndex].Clear();
			if (parentNode[tabIndex] != null)
				parentNode[tabIndex].Clear();
			if (disabledNode[tabIndex] != null)
				disabledNode[tabIndex].Clear();
			if (gridLines[tabIndex] != null)
				gridLines[tabIndex].Clear();


			PopupMenu menuController = new PopupMenu();
			menuController.context = this;

			ValueChangedEvent evClass = new ValueChangedEvent();
			evClass.context = this;

			grid.Rows.Clear();
			grid.Columns.Clear();

			grid.Controller.AddController(evClass);

			grid.BorderStyle = BorderStyle.FixedSingle;

			grid.ColumnsCount = 4;
			grid.FixedRows = 1;
			grid.Rows.Insert(0);

			SourceGrid.Cells.Views.Cell boldView = new SourceGrid.Cells.Views.Cell();
			boldView.Font = new Font(grid.Font, FontStyle.Bold | FontStyle.Underline);

			SourceGrid.Cells.Views.CheckBox checkBoxView = new SourceGrid.Cells.Views.CheckBox();
			//checkBoxView.Border =new DevAge.Drawing.RectangleBorder();
			//checkBoxView.AnchorArea = new DevAge.Drawing.AnchorArea(DevAge.Drawing.ContentAlignment.TopLeft, true);
			checkBoxView.CheckBoxAlignment = DevAge.Drawing.ContentAlignment.TopLeft;
			//checkBoxView.BackColor = Color.Blue;

			SourceGrid.Cells.Views.Cell groupView = new SourceGrid.Cells.Views.Cell();
			groupView.BackColor = Color.FromArgb(255, 220, 220, 220);

			SourceGrid.Cells.Views.Cell chapterView = new SourceGrid.Cells.Views.Cell();
			chapterView.Font = new Font(grid.Font, FontStyle.Bold);
			chapterView.BackColor = Color.FromArgb(255, 220, 220, 220);
			//chapterView.AnchorArea = new DevAge.Drawing.AnchorArea(DevAge.Drawing.ContentAlignment.MiddleCenter, false);

			grid[0, 0] = new SourceGrid.Cells.ColumnHeader("Line");
			grid[0, 1] = new SourceGrid.Cells.ColumnHeader("Key");
			grid[0, 2] = new SourceGrid.Cells.ColumnHeader("Value");
			grid[0, 3] = new SourceGrid.Cells.ColumnHeader("Notes");

			grid.Columns[0].MinimalWidth = 20;
			grid.Columns[0].MaximalWidth = 50;

			grid.Columns[1].MinimalWidth = 100;
			grid.Columns[1].MaximalWidth = 300;

			grid.Columns[2].MinimalWidth = 100;
			grid.Columns[2].MaximalWidth = 300;

			grid.Columns[3].MinimalWidth = 100;
			grid.Columns[3].MaximalWidth = 500;


			SourceGrid.Cells.Controllers.ToolTipText toolTipController = new SourceGrid.Cells.Controllers.ToolTipText();
			toolTipController.BackColor = Color.White;
			toolTipController.ForeColor = Color.Black;
			toolTipController.IsBalloon = false;

			int r = 1;
			if (mSettingsLineList[tabIndex] != null)
				mSettingsLineList[tabIndex].Clear();
			try
			{

				if (fileContent != null)
				{
					string line;
					string nodeLine;
					string curNotes = "";
					//string curChapter = "";
					bool skipNext = false, skipGroup = false, bold = false, group = false, nextIsCheck = false, nextIsCombo = false;
					int iLineNr = 0;
					int nodeNr = 0;
					//List<string> listChoices;
					string[] choices = { };

					//while ((line = reader[tabIndex].ReadLine()) != null)
					foreach (var ln in fileContent[tabIndex].Split(new string[] { Environment.NewLine }, StringSplitOptions.None))
					{
						line = ln;
						iLineNr++;
						mSettingsLineList[tabIndex].Add(line); // Add to list.
						//Console.WriteLine(line); // Write to console.
						char[] trimArgs = { ' ', '\t' };

						nodeLine = line;
						line = line.Trim(trimArgs);

						string[] words = line.Split(' ');

						if (words.Length == 0) continue;

						if (words[0] == "rem")
						{
							if (words[1] == "@note")
								curNotes += line.Substring(line.IndexOf("@note") + 5) + " ";
							else if (words[1] == "@hide")
								skipNext = true;
							else if (words[1] == "@hide_group")
								skipGroup = true;
							else if (words[1] == "@nohide")
								skipGroup = false;
							else if (words[1] == "@bold")
								bold = true;
							else if (words[1] == "@group")
								group = true;
							else if (words[1] == "@endgroup")
								group = false;
							else if (words[1] == "@chapter")
							{
								grid.Rows.Insert(r);
								grid[r, 0] = new SourceGrid.Cells.Cell(line.Substring(line.IndexOf("@chapter") + 8), typeof(string));
								grid[r, 0].Editor.EditableMode = SourceGrid.EditableMode.None;
								grid[r, 0].ColumnSpan = 4;
								grid[r, 0].View = chapterView;
								r++;
							}
							else if (words[1] == "@check")
								nextIsCheck = true;
							else if (words[1] == "@combo")
							{
								nextIsCombo = true;
								choices = line.Substring(line.IndexOf("@combo") + 6).Split('|');
								for (int i = 0; i < choices.Length; i++)
								{
									choices[i] = choices[i].Trim();
								}
							}
							else if (words[1] == "@include")
							{
								string inclNm = line.Substring(line.IndexOf("@include") + 8) + " ";
								const char quote = '\"';
								bool quoteCase = false;
								for (int c = 0; c < inclNm.Length; c++)
								{
									if (char.Equals(inclNm[c], quote))
									{
										quoteCase = true;
										break;
									}
								}
								string[] vals;
								if (quoteCase == true)
									vals = inclNm.Split(quote);
								else
									vals = inclNm.Split(' ');

								int f = 0;
								string label = "", inclFile = "";
								foreach (string v in vals)
								{
									if (quoteCase == true)
									{
										v.TrimStart();
										v.TrimEnd();
									}
									if (v.Length > 1 && f == 0)
									{
										label = v;
										f++;
									}
									else if (v.Length > 1 && f == 1)
									{
										inclFile = v;
										f++;
										break;
									}
								}
								bool bShouldSkip = false;
								if (filter != null)
								{
									if (label.IndexOf(filter, 0, label.Length, StringComparison.OrdinalIgnoreCase) == -1 &&
										inclFile.IndexOf(filter, 0, inclFile.Length, StringComparison.OrdinalIgnoreCase) == -1)
									{
										bShouldSkip = true;
									}
								}

								if (!bShouldSkip)
								{
									grid.Rows.Insert(r);
									grid[r, 0] = new SourceGrid.Cells.Cell(iLineNr.ToString(), typeof(string));
									grid[r, 0].Editor.EditableMode = SourceGrid.EditableMode.None;
									grid[r, 0].ColumnSpan = 1;//1
									grid[r, 1] = new SourceGrid.Cells.Cell(label, typeof(string));
									grid[r, 1].Editor.EditableMode = SourceGrid.EditableMode.None;
									grid[r, 1].AddController(menuController);
									grid[r, 3] = new SourceGrid.Cells.Cell();////
									Button b = new Button();
									b.Click += new EventHandler(includeButtonClick);
									SourceGrid.Cells.Button sb = new SourceGrid.Cells.Button(b);
									SourceGrid.Cells.Controllers.Button cb = new SourceGrid.Cells.Controllers.Button();
									cb.Executed += new EventHandler(this.includeButtonClick);
									sb.AddController(cb);
									sb.Value = Path.GetFileName(inclFile);
									sb.ToolTipText = inclFile;
									grid[r, 2] = sb;

									r++;
								}
							}
							else if ((words[1] == "@beginGameSpecific"))
							{
								grid.Rows.Insert(r);
								grid[r, 0] = new SourceGrid.Cells.Cell("=================== GAME SPECIFIC ADDED FLAGS ===================", typeof(string));
								grid[r, 0].Editor.EditableMode = SourceGrid.EditableMode.None;
								grid[r, 0].ColumnSpan = 4;
								grid[r, 0].View = chapterView;
								r++;

								m_beginGameSpecific[tabIndex] = r;
							}
						}



						if ((words.Length > 1) && (words[0] == "set"))
						{

							if (skipNext)
							{
								skipNext = false;
								continue;
							}
							if (skipGroup)
								continue;


							int iEq = words[1].IndexOf('=');
							if (iEq > 0)
							{
								string key = words[1].Substring(0, iEq);
								string value = words[1].Substring(iEq + 1);
								for (int w = 2; w < words.Length; w++)
									value += " " + words[w];

								bool bShouldSkip = false;
								if (filter != null)
								{
									if (key.IndexOf(filter, 0, key.Length, StringComparison.OrdinalIgnoreCase) == -1 &&
										value.IndexOf(filter, 0, value.Length, StringComparison.OrdinalIgnoreCase) == -1)
									{
										if (curNotes != null && curNotes.Length > 0)
										{
											if (curNotes.IndexOf(filter, 0, curNotes.Length, StringComparison.OrdinalIgnoreCase) == -1)
												bShouldSkip = true;
										}
										else bShouldSkip = true;
									}
								}

								if (bShouldSkip)
								{
									nextIsCheck = false;
									nextIsCombo = false;
									bold = false;
									curNotes = "";

									continue;
								}

								int lastParent = 0;
								if (nodeNr > 0)
									lastParent = nodeTabs[tabIndex].LastIndexOf(getNumberOfTabs(nodeLine));
								nodeTabs[tabIndex].Add(getNumberOfTabs(nodeLine));
								gridLines[tabIndex].Add(r);


								/* -----  seting the parent for every option in config.bat  --------------*/

								int len = disabledNode[tabIndex].Count;
								/*
								if (nodeNr == 0)
									parentNode[tabIndex].Add(-1);
								else
								{
									if (nodeTabs[tabIndex].ElementAt(nodeNr) == nodeTabs[tabIndex].ElementAt(nodeNr - 1))
										parentNode[tabIndex].Add(parentNode[tabIndex].ElementAt(nodeNr - 1));
									else if (nodeTabs[tabIndex].ElementAt(nodeNr) > nodeTabs[tabIndex].ElementAt(nodeNr - 1))
										parentNode[tabIndex].Add(nodeNr - 1);
									else
										parentNode[tabIndex].Add(parentNode[tabIndex].ElementAt(lastParent));

								}
								*/

								/*------------------------------------------------------------------*/

								grid.Rows.Insert(r);



								grid[r, 0] = new SourceGrid.Cells.Cell(iLineNr, typeof(string));
								grid[r, 1] = new SourceGrid.Cells.Cell(key, typeof(string));
								grid[r, 1].AddController(menuController);

								if (nextIsCheck)
								{
									nextIsCheck = false;
									grid[r, 2] = new SourceGrid.Cells.CheckBox("", (value == "1"));
									grid[r, 2].View = checkBoxView;
									lineType[tabIndex].Add("checkBox");
								}
								else if (nextIsCombo)
								{
									nextIsCombo = false;

									SourceGrid.Cells.Editors.ComboBox cbEditor = new SourceGrid.Cells.Editors.ComboBox(typeof(string));
									cbEditor.EditableMode = SourceGrid.EditableMode.Focus | SourceGrid.EditableMode.SingleClick | SourceGrid.EditableMode.AnyKey;
									cbEditor.StandardValues = choices;

									grid[r, 2] = new SourceGrid.Cells.Cell(value, cbEditor);
									grid[r, 2].View = SourceGrid.Cells.Views.ComboBox.Default;
									grid[r, 2].ToolTipText = value;
									grid[r, 2].AddController(toolTipController);
									lineType[tabIndex].Add("comboBox");
								}
								else
								{
									grid[r, 2] = new SourceGrid.Cells.Cell(value, typeof(string));
									grid[r, 2].ToolTipText = value;
									grid[r, 2].AddController(toolTipController);
									lineType[tabIndex].Add("cell");
								}


								grid[r, 3] = new SourceGrid.Cells.Cell(curNotes, typeof(string));
								grid[r, 3].ToolTipText = curNotes;
								grid[r, 3].AddController(toolTipController);


								if (group)
								{
									grid[r, 0].View = groupView;
									//grid[r, 1].View = groupView;
									//grid[r, 2].View = groupView;
									//grid[r, 3].View = groupView;                                
								}
								if (bold)
								{
									bold = false;
									//grid[r, 0].View = boldView;
									grid[r, 1].View = boldView;
									grid[r, 2].View = boldView;
									grid[r, 3].View = boldView;
								}


								grid[r, 0].Editor.EditableMode = SourceGrid.EditableMode.None;
								grid[r, 1].Editor.EditableMode = SourceGrid.EditableMode.None;
								grid[r, 3].Editor.EditableMode = SourceGrid.EditableMode.None;
								//Model.EnableEdit = false;
								curNotes = "";
								r++;

							}
							nodeNr++;
						}

					}
				}
			}
			catch (Exception ex)
			{

				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());

				//DialogResult result = sendCrash("CrashReport", ex.Message, ex.StackTrace, false);
			}
			grid.AutoSizeCells();

		}

		public void includeButtonClick(object sender, EventArgs e)
		{
			try
			{
				SourceGrid.CellContext tempC = (SourceGrid.CellContext)sender;
				SourceGrid.Cells.Button tempB = (SourceGrid.Cells.Button)tempC.Cell;
				if (tempB.ToolTipText.Length < 1)
				{
					MessageBox.Show("ERROR, no file is specified in config.bat at @include");
				}
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(m_baseDir);
				proc.StartInfo.FileName = s_notepad;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = false;
				proc.StartInfo.UseShellExecute = false;
				const char quote = '\"';
				string arg = tempB.ToolTipText;
				arg.Trim();
				if (arg[0] != quote)
				{
					arg = quote + arg;
				}
				if (arg[arg.Length - 1] != quote)
				{
					arg += quote.ToString();
				}
				proc.StartInfo.Arguments = arg;
				proc.Start();
			}
			catch (Exception)
			{
				MessageBox.Show("ERROR, Cannot perform action.");
			}

		}

		private void disableField(int index, int tab)
		{
			Grid grid = null;

			if ((int)INPUT_FILE_TYPE.ENV == tab)
				grid = this.GridEnvOptions;
			else if ((int)INPUT_FILE_TYPE.CFG == tab)
				grid = this.GridConfigOptions;

			if (parentNode[tab].ElementAt(index) == -1)
			{
				disabledNode[tab].Add(false);
			}
			else
			{
				if (disabledNode[tab].ElementAt(parentNode[tab].ElementAt(index)) || grid[gridLines[tab].ElementAt(parentNode[tab].ElementAt(index)), 2].ToString() == "False")
					disabledNode[tab].Add(true);
				else
					disabledNode[tab].Add(false);
			}

		}

		public void updateFields(int tab)
		{
			Grid grid = null;

			if ((int)INPUT_FILE_TYPE.ENV == tab)
				grid = this.GridEnvOptions;
			else if ((int)INPUT_FILE_TYPE.CFG == tab)
				grid = this.GridConfigOptions;

			disabledNode[tab].Clear();
			int len = nodeTabs[tab].Count;

			for (int i = 0; i < len; i++)
			{
				//                disableField(i, tab);
			}

			for (int i = 0; i < len; i++)
			{

				if (disabledNode[tab].ElementAt(i))
				{
					grid[gridLines[tab].ElementAt(i), 2].Editor.EnableEdit = false;
					if (lineType[tab].ElementAt(i) == "checkBox")
						grid[gridLines[tab].ElementAt(i), 2].View = checkBoxDisableView;

					if (lineType[tab].ElementAt(i) == "comboBox")
						grid[gridLines[tab].ElementAt(i), 2].View = comboBoxDisableView;

					if (lineType[tab].ElementAt(i) == "cell")
						grid[gridLines[tab].ElementAt(i), 2].View = disableView;

					for (int j = 0; j < 4; j++)
						if (j != 2)
							grid[gridLines[tab].ElementAt(i), j].View = disableView;
				}
				else
				{
					grid[gridLines[tab].ElementAt(i), 2].Editor.EnableEdit = true;
					if (lineType[tab].ElementAt(i) == "checkBox")
						grid[gridLines[tab].ElementAt(i), 2].View = checkBoxView;
					if (lineType[tab].ElementAt(i) == "comboBox")
						grid[gridLines[tab].ElementAt(i), 2].View = SourceGrid.Cells.Views.ComboBox.Default;
					if (lineType[tab].ElementAt(i) == "cell")
						grid[gridLines[tab].ElementAt(i), 2].View = normalView;

					for (int j = 0; j < 4; j++)
						if (j != 2)
							grid[gridLines[tab].ElementAt(i), j].View = normalView;
				}
			}
			if (len > 0)
				grid.AutoSizeCells();

		}

		private int getNumberOfTabs(string line)
		{
			int nrTabs = 0;
			int nrSpaces = 0;
			if (line != null)
			{
				char[] text = line.ToCharArray();
				int cursor = 0;
				while (cursor < line.Length)
				{
					if (text[cursor] == '\t')
						nrTabs++;
					else if (text[cursor] == ' ')
						nrSpaces++;
					else
						break;
					cursor++;
				}

			}

			nrTabs += nrSpaces / 4;

			return nrTabs;
		}

		private void buttonBUILDFileDiff_Click(object sender, EventArgs e)
		{
			String fname = BUILD_FileName;
			Diff(fname, fname + s_str_backupEXT);
		}


		private void buttonHEADERFileDiff_Click(object sender, EventArgs e)
		{
			String fname = HEADER_FileName;
			Diff(fname, fname + s_str_backupEXT);
		}


		String ReplateConfiguration(String str)
		{
			return str.Replace(s_str_buildConfiguration, radioButtonRelease.Checked ? radioButtonRelease.Text : radioButtonDebug.Text);
		}


		private void buttonInstall_Click(object sender, EventArgs e)
		{
			string cmd = "";
			if (!Path.IsPathRooted(m_install_command))
				cmd = m_baseDir + m_install_command;
			else
				cmd = m_install_command;

			string command = cmd;
			string args = "";

			if (!File.Exists(command))
			{
				MessageBox.Show
					(
					  "The script file: \n\n '" + command + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			args = ((radioButtonRelease.Checked == true) ? "release" : "debug");
			args += " " + "pause_at_end";

			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = args;

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}

		}



		private void buttonClean_Click(object sender, EventArgs e)
		{
			DialogResult result = MessageBox.Show(this, "Proceed with ireversible clean operation?", "Clean", MessageBoxButtons.YesNo, MessageBoxIcon.Question);
			if (result == DialogResult.No)
			{
				return;
			}

			string cmd = "";
			if (!Path.IsPathRooted(m_clean_command))
				cmd = m_baseDir + m_clean_command;
			else
				cmd = m_clean_command;

			string command = cmd;
			string args = "";
			int index = cmd.IndexOf(" ");
			if (index > 0)
			{
				command = cmd.Substring(0, index);
				args = cmd.Substring(index, cmd.Length - index);
			}

			if (!File.Exists(command))
			{
				MessageBox.Show
					(
					  "The script file: \n\n '" + command + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}

			args = " full pause_at_end";

			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = args;

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}

		private void buttonOpenSolution_Click(object sender, EventArgs e)
		{
			string cmd = "";
			if (!Path.IsPathRooted(m_open_solution))
				cmd = m_baseDir + m_open_solution;
			else
				cmd = m_open_solution;

			string command = cmd;
			string args = "";
			int index = cmd.IndexOf(" ");
			if (index > 0)
			{
				command = cmd.Substring(0, index);
				args = cmd.Substring(index, cmd.Length - index);
			}

			if (!File.Exists(command))
			{
				MessageBox.Show
					(
					  "The script file: \n\n '" + command + "' \n\n does not exist.",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				return;
			}
			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = true;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = args;

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}

		private void scrollVertical(object sender, ScrollPositionChangedEventArgs e)
		{
			if (m_obedientScrollCFG.X > 0 && m_obedientScrollCFG.Y > 0 && this.GridConfigOptions.Enabled == true)
				this.GridConfigOptions.CustomScrollPosition = m_obedientScrollCFG;

		}
		private void scrollHorizontal(object sender, ScrollPositionChangedEventArgs e)
		{

			if (this.GridConfigOptions.Enabled == true)
			{
				Position p = this.GridConfigOptions.MouseCellPosition;
				Position pref = new Position(-1, -1);
				if (p == pref)
				{
					m_obedientScrollCFG.X = this.GridConfigOptions.CustomScrollPosition.X;

				}
				else
				{
					this.GridConfigOptions.CustomScrollPosition = m_obedientScrollCFG;
				}
			}

		}

		private void gridENVFile_HScrollPositionChanged(object sender, ScrollPositionChangedEventArgs e)
		{
			if (this.GridEnvOptions.Enabled == true)
			{
				Position p = this.GridEnvOptions.MouseCellPosition;
				Position pref = new Position(-1, -1);
				if (p == pref)
				{
					m_obedientScrollENV.X = this.GridEnvOptions.CustomScrollPosition.X;

				}
				else
				{
					this.GridEnvOptions.CustomScrollPosition = m_obedientScrollENV;
				}
			}
		}

		private void gridENVFile_VScrollPositionChanged(object sender, ScrollPositionChangedEventArgs e)
		{
			if (m_obedientScrollENV.X > 0 && m_obedientScrollENV.Y > 0 && this.GridEnvOptions.Enabled == true)
				this.GridEnvOptions.CustomScrollPosition = m_obedientScrollENV;
		}


		private void gridCFGFile_Click(object sender, EventArgs e)
		{
			GridConfigOptions.Select();
			m_SelCellPosCFG = GridConfigOptions.Selection.ActivePosition;
			//gridCFGFile.Selection.SelectCell(m_SelCellPosCFG, true);
			//try
			//{
			//    System.Windows.Forms.MouseEventArgs me = (System.Windows.Forms.MouseEventArgs)e;
			//    MessageBox.Show(me.Button.ToString());
			//}
			//catch (Exception ex)
			//{
			//    MessageBox.Show("ERROR");
			//}
		}

		private void gridCFGFile_MouseUp(object sender, MouseEventArgs e)
		{
			try
			{
				Position p = new Position(m_SelCellPosCFG.Row, GridConfigOptions.ColumnsCount - 1);
				String description = GridConfigOptions.GetCell(p).ToString();
				if (p.Row > 0 && p.Column > 0 && description.Length > 0 && m_SelCellPosCFG.Column != 2)
					textBoxCFG.Text = description;
				else
				{
					textBoxCFG.Text = GridConfigOptions.GetCell(m_SelCellPosCFG).ToString();
				}
			}
			catch (Exception)
			{ }
		}

		private void gridENVFile_Click(object sender, EventArgs e)
		{
			GridEnvOptions.Select();
			m_SelCellPosENV = GridEnvOptions.Selection.ActivePosition;
			//gridENVFile.Selection.SelectCell(m_SelCellPosENV, true);
		}

		private void gridENVFile_MouseUp(object sender, MouseEventArgs e)
		{
			try
			{
				Position p = new Position(m_SelCellPosENV.Row, GridEnvOptions.ColumnsCount - 1);
				if (p.Row > 0 && p.Column > 0)
					textBoxENV.Text = GridEnvOptions.GetCell(p).ToString();
			}
			catch (Exception)
			{ }
		}

		private void PlatformTabPage_Load(object sender, EventArgs e)
		{
			//this.buttonRun.Text = RUN_Command_Label;
		}

		private void buttonRun_Click(object sender, EventArgs e)
		{
			string cmd = "";
			if (!Path.IsPathRooted(m_run_command))
				cmd = m_baseDir + m_run_command;
			else
				cmd = m_run_command;

			string command = cmd;
			string args = "";
			int index = cmd.IndexOf(" ");
			if (index > 0)
			{
				command = cmd.Substring(0, index);
				args = cmd.Substring(index, cmd.Length - index);
			}

			if (!File.Exists(command))
			{
				MessageBox.Show
					(
					  "Your intending to run an unexistent file at: \n'" + command + "'",
					  "Error",
					  MessageBoxButtons.OK,
					  MessageBoxIcon.Error
					);
				//m_run_command = ""; 
				return;
			}

			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = false;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = args;

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}

		private void buttonCheckTools_Click(object sender, EventArgs e)
		{
			string cmd = "";
			if (!Path.IsPathRooted(m_check_tools))
				cmd = m_baseDir + m_check_tools;
			else
				cmd = m_check_tools;

			string command = cmd;
			string args = " pause_at_end";

			if (!File.Exists(command))
			{
				MessageBox.Show
					(
						"You are trying to run an inexistent file at: \n'" + command + "'",
						"Error",
						MessageBoxButtons.OK,
						MessageBoxIcon.Error
					);
				return;
			}

			try
			{
				System.Diagnostics.Process proc = new System.Diagnostics.Process();
				proc.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(command);
				Console.WriteLine("WorkingDirectory " + proc.StartInfo.WorkingDirectory);
				proc.StartInfo.FileName = command;
				proc.StartInfo.RedirectStandardError = false;
				proc.StartInfo.RedirectStandardInput = false;
				proc.StartInfo.RedirectStandardOutput = false;
				proc.StartInfo.CreateNoWindow = false;
				proc.StartInfo.UseShellExecute = true;

				proc.StartInfo.Arguments = args;

				m_executedCommands.Add(proc.StartInfo.FileName + " " + proc.StartInfo.Arguments);

				proc.Start();
				//Console.WriteLine(proc.StandardOutput.ReadToEnd());
				//proc.WaitForExit();
			}
			catch (Exception ex)
			{
				Console.WriteLine("Exception Occurred :{0},{1}", ex.Message, ex.StackTrace.ToString());
			}
		}

		private void pictureBox_Click(object sender, EventArgs e)
		{

		}

		private void buttonHEADERFileOpen_Click(object sender, EventArgs e)
		{
			Process.Start(s_notepad, HEADER_FileName);
		}

		private void buttonBUILDFileOpen_Click(object sender, EventArgs e)
		{
			Process.Start(s_notepad, BUILD_FileName);
		}

		private void buttonCONFIGFileOpen_Click(object sender, EventArgs e)
		{
			Process.Start(s_notepad, CONFIG_FileName);
		}

		private void buttonENVFileOpen_Click(object sender, EventArgs e)
		{
			Process.Start(s_notepad, ENV_FileName);
		}

	}

	public class PopupMenu : SourceGrid.Cells.Controllers.ControllerBase
	{
		public PlatformTabPage context;
		ContextMenu menu = new ContextMenu();

		Grid grid = null;
		int row;

		public PopupMenu()
		{
			menu.MenuItems.Add("Copy", new EventHandler(MenuCopy_Click));
		}

		public override void OnMouseUp(SourceGrid.CellContext sender, MouseEventArgs e)
		{
			base.OnMouseUp(sender, e);

			if (sender.Grid == context.GridEnvOptions)
			{
				grid = context.GridEnvOptions;
			}
			else if (sender.Grid == context.GridConfigOptions)
			{
				grid = context.GridConfigOptions;
			}
			context.GridEnvOptions.Selection.ResetSelection(false);
			context.GridConfigOptions.Selection.ResetSelection(false);

			sender.Grid.Selection.SelectCell(sender.Position, true);


			row = sender.Position.Row;

			if (e.Button == MouseButtons.Right)
			{
				menu.Show(sender.Grid, new Point(e.X, e.Y));
			}
		}

		private void MenuCopy_Click(object sender, EventArgs e)
		{
			string txt = grid[row, 1].ToString();
			Clipboard.SetText(txt);
		}

	}




	public class ValueChangedEvent : SourceGrid.Cells.Controllers.ControllerBase
	{
		public PlatformTabPage context;

		public override void OnValueChanged(SourceGrid.CellContext sender, EventArgs e)
		{
			Grid grid = null;

			int fileType = 0;

			if (sender.Grid == context.GridEnvOptions)
			{
				fileType = 0;
				grid = context.GridEnvOptions;
			}
			else if (sender.Grid == context.GridConfigOptions)
			{
				fileType = 1;
				grid = context.GridConfigOptions;
			}


			base.OnValueChanged(sender, e);

			int lineNr = int.Parse(grid[sender.Position.Row, 0].ToString());

			//string val = "Value of cell {0} is '{1}' lineVal={2}";
			//MessageBox.Show(sender.Grid, string.Format(val, sender.Position, sender.Value, lineNr));
			string value = grid[sender.Position.Row, 2].ToString();

			if (value == "True")
				value = "1";

			else if (value == "False")
				value = "0";

			context.m_fileWasChanged[fileType] = true;
			//context.updateFields(curTab);
			context.mSettingsLineList[fileType][lineNr - 1] = "set " + grid[sender.Position.Row, 1].ToString() + "=" + value;
		}
		public override void OnFocusEntered(SourceGrid.CellContext sender, EventArgs e)
		{
			base.OnFocusEntered(sender, e);

			//context.textBox3.Text = context.grid[curTab][sender.Position.Row, sender.Position.Column].ToString();
		}
	}






}
