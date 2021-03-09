using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.Collections;
using System.Xml;
using System.Windows.Forms;
using System.IO;
using System.ComponentModel;
using System.Runtime.InteropServices;
using Utils;


namespace GLLegacyInstaller
{
    public class ToolsList: RList
    {

        #region Win32
        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        private static extern bool DefineDosDevice(
            int dwFlags,
            string lpDeviceName,
            string lpTargetPath
            );

        [DllImport("Kernel32.dll")]
        internal static extern uint QueryDosDevice(string lpDeviceName, StringBuilder lpTargetPath, uint ucchMax);

        [DllImport("kernel32.dll", CharSet = CharSet.Auto)]
        private static extern int GetDriveType(
            string lpRootPathName
            );

        private const int DDD_RAW_TARGET_PATH = 0x00000001;
        private const int DDD_REMOVE_DEFINITION = 0x00000002;
        private const int DDD_EXACT_MATCH_ON_REMOVE = 0x00000004;

        private const int DRIVE_UNKNOWN = 0;
        private const int DRIVE_NO_ROOT_DIR = 1;
        private const int DRIVE_FIXED = 3;

        const string MAPPED_FOLDER_INDICATOR = @"\??\";
        #endregion // Win32

        const String m_str_tools = "tools";
        const String m_str_tool  = "tool";

        const String m_str_ID = "ID";
        const String m_str_toolName = "name";
        const String m_str_required = "required";
        const String m_str_virtual_drive = "virtual_drive";
        const String m_str_min_version = "minVersion";
        const String m_str_path = "path";
        const String m_str_browse = "browse";
        const String m_str_info = "info";
        const String m_str_downloadLink = "download";
        const String m_str_desc = "Desc";
        const String m_str_platforms = "platforms";
        const String m_str_AllowSpaceInPath = "AllowSpaceInPath";

        /*const String m_str_Win32        = "Win32";
        const String m_str_iOS          = "iOS";
        const String m_str_Android      = "Android";
        const String m_str_Win8         = "Win8";
        const String m_str_WinPhone8    = "WinPhone8";*/
        

        const String m_str_InstallPath   = "InstallPath";
        const String m_str_AddToPath     = "AddToPath";
        const String m_str_GetFromEnvVar = "GetFromEnvVar";
        const String m_str_RegEntry      = "RegEntry";
        const String m_str_Key           = "Key";
        const String m_str_value         = "value";
        const String m_str_InstallScript = "InstallScript";
        const String m_str_InstallScript_value = "value";

        const String m_str_Check        = "Check";
        const String m_str_Check_name   = "name";
        const String m_str_Check_file   = "file";
        const String m_str_CheckType    = "CheckType";
        const String m_str_Check_value  = "value";


        const String m_str_AddEnvPath   = "AddEnvPath";

        const String m_str_EnvironmentVar       = "EnvironmentVar";
        const String m_str_EnvironmentVar_name  = "name";
        

        const String m_str_link         = "link";

        const String m_str_Description  = "Description";


        CellBackColorAlternate m_normalView = new CellBackColorAlternate(Color.Khaki, Color.DarkKhaki);
        CellBackColorAlternate m_linkView = new CellBackColorAlternate(Color.Khaki, Color.DarkKhaki);
        CellBackColorAlternate m_errorView = new CellBackColorAlternate(Color.Red, Color.Red);
        CellBackColorAlternate m_warningView = new CellBackColorAlternate(Color.Orange, Color.Orange);

        enum COL
        {
            INDEX = 0,
            TOOLNAME,
            REQUIRED,
            PATH,
            BROWSE,
            INFO,
            INSTALL,
            DOWNLOAD,
            DESC
        };

        public ToolsList(SourceGrid.Grid grid )
            : base(grid)
        {
            RefreshGridStructure();

            m_linkView.ForeColor = Color.Blue;
        }

        public override void RefreshGridStructure()
        {
            if (this.m_grid != null)
            {
                this.m_grid.Rows.Clear();
                this.m_grid.Columns.Clear();

                SetValueController(new ToolValueChangedEvent(this));

                this.m_grid.ColumnsCount = 9;
                this.m_grid.Rows.Insert(0);
                this.m_grid.FixedRows = 1;
                this.m_grid.FixedColumns = 1;


                CreateColumnHeader((int)COL.INDEX, m_str_ID, 23, 23);
                CreateColumnHeader((int)COL.TOOLNAME, m_str_toolName, 130, 150);
                CreateColumnHeader((int)COL.REQUIRED, m_str_required, 50, 50);
                CreateColumnHeader((int)COL.PATH, m_str_path, 300, 500);
                CreateColumnHeader((int)COL.BROWSE, "browse", 60, 50);
                CreateColumnHeader((int)COL.INFO, "", 30, 30);
                CreateColumnHeader((int)COL.INSTALL, "", 50, 50);
                CreateColumnHeader((int)COL.DOWNLOAD, m_str_downloadLink, 120, 500);
                CreateColumnHeader((int)COL.DESC, m_str_desc, 210, 500);
                this.m_grid.Refresh();
            }
        }


        public override void Save(XmlWriter writer)
        {
            /*
            foreach (DictionaryEntry entry in m_objects)
            {
                Tool rm = (Tool)entry.Value;

                writer.WriteStartElement(m_str_tool);

                writer.WriteAttributeString(m_srt_ID,       rm.m_ID.ToString());
                writer.WriteAttributeString(m_str_toolName, rm.m_toolName);
                writer.WriteAttributeString(m_str_required, rm.m_required.ToString());
                writer.WriteAttributeString(m_str_path,     rm.m_path);
                writer.WriteAttributeString(m_str_downloadLink, rm.m_downloadURL);
                writer.WriteAttributeString(m_str_desc,     rm.m_desc);
                

                writer.WriteEndElement();
            }*/
        }




        bool m_foundToolsNode = false;
        Tool currentTool = null;

   
        public override void Load(XmlReader reader)
        {
            //if (reader)
            {
                // Only detect start elements.
                // Get element name and switch on it.
                switch (reader.Name)
                {
                    case m_str_tools:
                        if (reader.IsStartElement())
                        {
                            m_foundToolsNode = true;
                        }
                        else
                        {                                
                            m_foundToolsNode = false;
                            return;
                        }

                        break;

                    case m_str_tool:
                        if (reader.IsStartElement() && m_foundToolsNode)
                        {
                            currentTool = new Tool(-1, reader[m_str_toolName]);

                            String req = reader[m_str_required];

                            if (req != null)
                                currentTool.m_required = ((req.ToUpper() == "TRUE") || (req.ToUpper() == "YES") || (req == "1"));

                            #region read and test platform for tool
                            String platforms = reader[m_str_platforms];

                            if (platforms != null)
                            {
                                string[] str = platforms.Split('+');
                                foreach (string s in str)
                                {
                                    bool bValidPlatform = false;
                                    foreach (string p in Globals.s_platforms)
                                    {
                                        if (s.ToUpper() == p.ToUpper())
                                        {
                                            bValidPlatform = true;
                                            break;
                                        }
                                    }
                                    if ( bValidPlatform )
                                        currentTool.m_platforms.Add(s.ToUpper());
                                    else
                                    {
                                        Globals.s_bValidPlatformForTool = false;
                                        Globals.s_InvalidPlatformForTool += "---Invalid platform: " + s + " for " + currentTool.m_toolName + " tool.\r\n\n";
                                    }
                                }
                            }
                            #endregion

                            #region read and test virtual drive letter
                            String drive = reader[m_str_virtual_drive];
                            if (drive != null)
                            {
                                //try to unmap virtual drive
                                if (Globals.DriveLetterExists(drive[0]))
                                {
                                    StringBuilder volumeMap = new StringBuilder(1024);
                                    //String mappedVolumeName = "";
                                    QueryDosDevice(drive + ":", volumeMap, (uint)1024);
                                    if (volumeMap.ToString().StartsWith(MAPPED_FOLDER_INDICATOR) != true)
                                    {
                                        // It's a mapped drive, so return the mapped folder name
                                        //mappedVolumeName = volumeMap.ToString().Substring(4);
                                        Globals.s_InvalidDrivesLetter += "---Invalid virtual drive " + drive + ": for " + currentTool.m_toolName + ". It is a fixed drive.\r\nChange config file " + Globals.s_settingsFile + " .\r\n\n";
                                        Globals.s_bValidDrivesLetter = false;
                                    }
                                    else
                                    {
                                        //String mappedVolumeName = volumeMap.ToString().Substring(4);
                                        //if( mappedVolumeName.ToUpper().Contains("ANDROID") )
                                        //DefineDosDevice(2, drive + ":", null);
                                        /*else
                                        {
                                            Globals.s_InvalidDrivesLetter += "---Invalid virtual drive " + drive + ", mappedVolumeName: " + mappedVolumeName + " : for " + currentTool.m_toolName + ". It is a drive already used, not by android-ndk.\r\nChange config file " + Globals.s_settingsFile + " .\r\n\n"; ;
                                            Globals.s_bValidDrivesLetter = false;
                                        }*/
                                    }
                                }
                                else
                                    Globals.b_restartService = true;

                                //Globals.s_VirtualDrivers[Globals.s_currentNDK] = drive;
                                foreach (string d in Globals.s_distccXMLDrives)
                                {
                                    if (d.ToUpper() == drive.ToUpper())
                                    {
                                        Globals.s_InvalidDrivesLetter += "---Invalid virtual drive " + drive + ": for " + currentTool.m_toolName + ". It is a drive already assigned to another NDK.\r\nChange config file " + Globals.s_settingsFile + " .\r\n\n"; ;
                                        Globals.s_bValidDrivesLetter = false;
                                    }
                                }
                                currentTool.m_VirtualDrive = drive.ToUpper();
                                Globals.s_distccXMLDrives.Add(currentTool.m_VirtualDrive);
                                Globals.s_distccNameNDK.Add(currentTool.m_toolName);                                
                                //Globals.s_currentNDK++;
                            }
                            #endregion

                            //#region read minVersion
                            currentTool.m_minVersion = reader[m_str_min_version];
                            //#endregion
                            
                            //#region read minVersion
                            String str_allow_space = reader[m_str_AllowSpaceInPath];
                            if (str_allow_space!= null)
                                currentTool.m_AllowSpaceInPath = (str_allow_space.ToUpper() == "TRUE");
                            //#endregion

                            this.Add(currentTool);
                        }
                        break;


                    case m_str_InstallPath:
                        if (currentTool != null)
                        {
                            InstallPathBase installPath = null;

                            if(reader[m_str_GetFromEnvVar] != null)
                            {
                                installPath = new InstallPathEnv(reader[m_str_GetFromEnvVar], reader[m_str_AddToPath]);
                            }
                            else if (reader[m_str_RegEntry] != null)
                            {
                                installPath = new InstallPathReg(reader[m_str_RegEntry], reader[m_str_Key], reader[m_str_AddToPath]);
                            }
                            else if (reader[m_str_value] != null)
                            {
                                installPath = new InstallPathBase(reader[m_str_value], reader[m_str_AddToPath]);
                            }

                            if(installPath != null)
                                currentTool.m_installPaths.Add(installPath);
                        }
                        break;

                    case m_str_Check:
                        if (currentTool != null)
                        {
                            CheckPathBase checkPath = null;

                            if (reader[m_str_Check_name] != null && reader[m_str_Check_file] != null && reader[m_str_CheckType] != null)
                            {
                                checkPath = new CheckPathBase(reader[m_str_Check_name], reader[m_str_Check_file], reader[m_str_CheckType], reader[m_str_Check_value]);
                            }

                            if (checkPath != null)
                                currentTool.m_checkPaths.Add(checkPath);
                        }
                        break;

                    case m_str_AddEnvPath:
                        if (currentTool != null)
                        {
                            if (reader[m_str_value] != null)
                            {
                                currentTool.m_AddEnvToPATH = reader[m_str_value];
                            }                                
                        }
                        break;


                    case m_str_EnvironmentVar:
                        if (currentTool != null)
                        {
                            if (reader[m_str_EnvironmentVar_name] != null && reader[m_str_value] != null)
                            {
                                currentTool.m_environmentVar[reader[m_str_EnvironmentVar_name]] = reader[m_str_value];
                                if( !string.IsNullOrEmpty(currentTool.m_VirtualDrive) )
                                    Globals.s_distccNDKEnvVar.Add(reader[m_str_EnvironmentVar_name]);
                            }
                        }
                        break;

                    case m_str_link:
                        if (currentTool != null)
                        {
                            if (reader[m_str_value] != null)
                            {
                                currentTool.m_downloadURL = reader[m_str_value];
                            }
                        }
                        break;

                    case m_str_Description:
                        if (currentTool != null)
                        {
                            String str = reader.ReadElementContentAsString();
                            
                            if (str != null)
                            {
                                currentTool.m_desc = str.Trim();
                            }
                        }
                        break;
                    case m_str_InstallScript:
                        if (currentTool != null)
                        {
                            if (reader[m_str_value] != null)
                            {
                                currentTool.m_InstallScriptPath = reader[m_str_value];
                            }
                        }
                        break;


                    default:
                        if (!m_foundToolsNode)
                        {                            
                            currentTool = null;
                            return;
                        }
                        else
                        {
                        }
                        break;
                }
            }

        }
        
        public bool UpdatePath(int ID, String path)
        {
            Tool rm = (Tool)(Get(ID));
            if(rm != null)
            {
                if (rm.m_path != path)
                {
                    rm.m_path = path;

                    rm.m_valid = rm.IsInstallPathValid(path);

                    UpdateEnvironment dlg = new UpdateEnvironment();
                    bool returnValue = (dlg.ShowDialog() == DialogResult.OK);
                    dlg = null;

                    return true;
                }
            }

            return false;
        }
        /*
        public bool UpdatePosition(int ID, int X, int Y)
        {
            Tool rm = (Tool)(Get(ID));
            if (rm != null)
            {
                rm.m_x = X;
                rm.m_y = Y;

                return true;
            }

            return false;
        }

        public bool UpdateSize(int ID, int W, int H)
        {
            Tool rm = (Tool)(Get(ID));
            if (rm != null)
            {
                rm.m_w = W;
                rm.m_h = H;

                return true;
            }

            return false;
        }

        public bool UpdateDesc(int ID, String Desc)
        {
            Tool rm = (Tool)(Get(ID));
            if (rm != null)
            {
                rm.m_desc = Desc;

                return true;
            }

            return false;
        }


        public bool Update(int ID, int ImgID, int X, int Y, int W, int H, String Desc)
        {
            Tool rm = (Tool)(Get(ID));
            if (rm != null)
            {
                rm.m_imgID = ImgID;
                rm.m_x = X;
                rm.m_y = Y;
                rm.m_w = W;
                rm.m_h = H;
                rm.m_desc = Desc;

                return true;
            }

            return false;
        }*/

        public override void RefreshGridCellValues()
        {
            //starts from 1 because the 0 is for column header
            for (int i = 1; i < m_grid.RowsCount; i++)
            {
                int ID = (int)(this.m_grid[i, (int)COL.INDEX].Value);

                Tool rm = (Tool)Get(ID);


                this.m_grid[i, (int)COL.INDEX].View = m_normalView;
                this.m_grid[i, (int)COL.TOOLNAME].View = m_normalView;
                this.m_grid[i, (int)COL.REQUIRED].View = m_normalView;
                this.m_grid[i, (int)COL.PATH].View = m_normalView;
                //this.m_grid[i, 4].View = m_normalView;
                this.m_grid[i, (int)COL.DOWNLOAD].View = m_linkView;
                this.m_grid[i, (int)COL.DESC].View = m_normalView;

                if (!rm.m_valid)
                {
                    this.m_grid[i, (int)COL.INSTALL] = new SourceGrid.Cells.Button("Install");
                    SourceGrid.Cells.Controllers.Button buttonClickEvent = new SourceGrid.Cells.Controllers.Button();
                    buttonClickEvent.Executed += new EventHandler(InstallButton_Click);
                    this.m_grid[i, (int)COL.INSTALL].Controller.AddController(buttonClickEvent);

                    if (rm.m_required)
                        this.m_grid[i, (int)COL.PATH].View = m_errorView;
                    else
                        this.m_grid[i, (int)COL.PATH].View = m_warningView;
                }
                else
                {
                    this.m_grid[i, (int)COL.INSTALL] = new SourceGrid.Cells.Cell("");
                    this.m_grid[i, (int)COL.INSTALL].View = m_normalView;
                }



                this.m_grid[i, (int)COL.TOOLNAME].Value = rm.m_toolName;
                this.m_grid[i, (int)COL.REQUIRED].Value = rm.m_required;
                this.m_grid[i, (int)COL.PATH].Value = rm.m_path;

                if (!rm.m_valid)
                {
                    if (rm.m_required)
                    {
                        this.m_grid[i, (int)COL.BROWSE].View = new RoundView(Color.Red);
                        this.m_grid[i, (int)COL.INFO].View = new RoundView(Color.Red);
                    }
                    else
                    {
                        this.m_grid[i, (int)COL.BROWSE].View = new RoundView(Color.Orange);
                        this.m_grid[i, (int)COL.INFO].View = new RoundView(Color.Orange);
                    }
                }
                else
                {
                    this.m_grid[i, (int)COL.BROWSE].View = new RoundView(Color.MediumSeaGreen);
                    this.m_grid[i, (int)COL.INFO].View = new RoundView(Color.MediumSeaGreen);
                }

                this.m_grid[i, (int)COL.DOWNLOAD].Value = rm.m_downloadURL;
                this.m_grid[i, (int)COL.DESC].Value = rm.m_desc;
            }

            m_grid.Refresh();
        }

        public override void RefreshGridContent()
        {
            RefreshGridStructure();
            ArrayList list = new ArrayList();
            foreach( DictionaryEntry entry in m_objects )
                list.Add(entry.Key);
            list.Sort();
            foreach (int k in list)
            {
                Tool rm = (Tool)m_objects[k];

                AddToGrid(rm);                
            }
        }

        public void RemoveAll()
        {
            m_objects.Clear();
        }

        public void Add(Tool rm)
        {
            m_objects[rm.m_ID] = rm;
            AddToGrid(rm);
        }

        int AddToGrid(Tool rm)
        {
            int gridRows = 0;

            if (!rm.IsUsedTool())
                return gridRows;

            if (this.m_grid != null)
            {
                gridRows = this.m_grid.RowsCount;

                this.m_grid.Rows.Insert(gridRows);

                //this.m_grid[gridRows, 0] = new SourceGrid.Cells.RowHeader(gridRows.ToString());

                this.m_grid[gridRows, (int)COL.INDEX] = new SourceGrid.Cells.Cell(rm.m_ID);   //ID

                this.m_grid[gridRows, (int)COL.TOOLNAME] = new SourceGrid.Cells.Cell(rm.m_toolName);    //m_str_toolName

                this.m_grid[gridRows, (int)COL.REQUIRED] = new SourceGrid.Cells.Cell(rm.m_required);    //m_str_required
                //this.m_grid[gridRows, 3].Editor = new SourceGrid.Cells.Editors.TextBoxNumeric(typeof(int));
                //this.m_grid[gridRows, 3].AddController(m_controller);

                this.m_grid[gridRows, (int)COL.PATH] = new SourceGrid.Cells.Cell(rm.m_path);        //m_str_path
                this.m_grid[gridRows, (int)COL.PATH].Editor = new SourceGrid.Cells.Editors.TextBox(typeof(string));
                this.m_grid[gridRows, (int)COL.PATH].AddController(m_controller);

                if (rm.m_valid)
                {
                    this.m_grid[gridRows, (int)COL.INSTALL] = new SourceGrid.Cells.Cell("");
                    //this.m_grid[gridRows, 4].Image = Properties.Resources.browse.ToBitmap();
                }
                else
                {
                    this.m_grid[gridRows, (int)COL.INSTALL] = new SourceGrid.Cells.Button("Install");
                    SourceGrid.Cells.Controllers.Button buttonClickEvent = new SourceGrid.Cells.Controllers.Button();
                    buttonClickEvent.Executed += new EventHandler(InstallButton_Click);
                    this.m_grid[gridRows, (int)COL.INSTALL].Controller.AddController(buttonClickEvent);
                }


                this.m_grid[gridRows, (int)COL.BROWSE] = new SourceGrid.Cells.Button("");
                this.m_grid[gridRows, (int)COL.BROWSE].Image = Properties.Resources.browse.ToBitmap();


                SourceGrid.Cells.Controllers.Button buttonClickBrowseEvent = new SourceGrid.Cells.Controllers.Button();
                buttonClickBrowseEvent.Executed += new EventHandler(BrowseButton_Click);
                this.m_grid[gridRows, (int)COL.BROWSE].Controller.AddController(buttonClickBrowseEvent);

                this.m_grid[gridRows, (int)COL.INFO] = new SourceGrid.Cells.Button("info");

                if (rm.m_valid)
                {
                    this.m_grid[gridRows, (int)COL.BROWSE].View = new RoundView(Color.MediumSeaGreen);
                    this.m_grid[gridRows, (int)COL.INFO].View = new RoundView(Color.MediumSeaGreen); 
                }
                else
                {
                    if (rm.m_required)
                    {
                        this.m_grid[gridRows, (int)COL.BROWSE].View = new RoundView(Color.Red);
                        this.m_grid[gridRows, (int)COL.INFO].View = new RoundView(Color.Red);
                    }
                    else
                    {
                        this.m_grid[gridRows, (int)COL.BROWSE].View = new RoundView(Color.Orange);
                        this.m_grid[gridRows, (int)COL.INFO].View = new RoundView(Color.Orange);
                    }
                }
                SourceGrid.Cells.Controllers.Button infoClickEvent = new SourceGrid.Cells.Controllers.Button();                
                infoClickEvent.Executed += new EventHandler(Info_Click);
                this.m_grid[gridRows, (int)COL.INFO].Controller.AddController(infoClickEvent);


                this.m_grid[gridRows, (int)COL.DOWNLOAD] = new SourceGrid.Cells.Link(rm.m_downloadURL);//m_str_downloadLink
                SourceGrid.Cells.Controllers.Button linkClickEvent = new SourceGrid.Cells.Controllers.Button();
                linkClickEvent.Executed += new EventHandler(Link_Click);
                this.m_grid[gridRows, (int)COL.DOWNLOAD].Controller.AddController(linkClickEvent);

                this.m_grid[gridRows, (int)COL.DESC] = new SourceGrid.Cells.Cell(rm.m_desc);        //m_str_desc
            }

            return gridRows;
        }

        /**
          * Info_Click - open a message box with inforations about tool if it is working or not and the cause (if is not working)           
          * @param sender an object - used to get the row of tool
          * @param e an EventArgs - not used
          **/
        private void Info_Click(object sender, EventArgs e)
        {
            try
            {
                SourceGrid.CellContext context = (SourceGrid.CellContext)sender;

                int ID = (int)(this.m_grid[context.Position.Row, (int)COL.INDEX].Value);
                String Desc = this.m_grid[context.Position.Row, (int)COL.DESC].Value.ToString();

                Tool rm = (Tool)(Get(ID));
                if (rm == null || rm.GetErrors() == null) return;

                String Msg = Desc + "\n\n" + rm.GetErrors();
               // rm.ResetErrors();
                BigMessageScreen ms = new BigMessageScreen();
                ms.SetMessage(Msg);
                ms.SetType(rm.m_valid?"Good":"Error");
                ms.ShowDialog();
            }
            catch (Exception)
            {
                String errMsg = "Info_Click: " + "\r\n" + e.ToString();
                if (Globals.s_workInConsole)
                {
                    Console.WriteLine(errMsg);
                }
                else
                {
                    MessageBox.Show(errMsg, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }

        }

        /**
         * Link_Click - open the link                     
         * @param sender an object - used to get the row of link
         * @param e an EventArgs - not used
         **/
        private void Link_Click(object sender, EventArgs e)
        {
            try
            {
                SourceGrid.CellContext context = (SourceGrid.CellContext)sender;
                DevAge.Shell.Utilities.OpenFile(((SourceGrid.Cells.Link)context.Cell).Value.ToString());
            }
            catch (Exception)
            {
                String errMsg = "Link_Click: " + "\r\n" + e.ToString();
                if (Globals.s_workInConsole)
                {
                    Console.WriteLine(errMsg);
                }
                else
                {
                    MessageBox.Show(errMsg, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }

        }

        /**
         * InstallButton_Click - process INSTALL button click                     
         *                     - refresh the grid
         * @param sender an object - used to get the row of INSTALL button
         * @param e an EventArgs - not used
         **/
        private void InstallButton_Click(object sender, EventArgs e)
        {
            SourceGrid.CellContext context = (SourceGrid.CellContext)sender;
            int ID = (int)(this.m_grid[context.Position.Row, (int)COL.INDEX].Value);

            Tool rm = (Tool)(Get(ID));
            if (rm == null) return;

            if (rm.m_toolName.ToLower() == "TortoiseSVN".ToLower())
            {
                BigMessageScreen ms = new BigMessageScreen();
                ms.SetMessage("\n\n\n\tPlease get and install TurtoiseSVN from http://tortoisesvn.net/.\r\n");
                ms.SetType("Warning");
                ms.ShowDialog();
                ms = null;
                return;
            }

            if (rm.m_toolName.ToLower().Contains("visualstudio"))
            {
                BigMessageScreen ms = new BigMessageScreen();
                ms.SetMessage("\n\n\n\tPlease get and install Visual Studio from http://www.visualstudio.com/downloads/download-visual-studio-vs\n\n");
                ms.SetType("Warning");
                ms.ShowDialog();
                ms = null;
                return;
            }

            if (string.IsNullOrEmpty(rm.m_InstallScriptPath) == true)
            {
                    BigMessageScreen ms = new BigMessageScreen();
                    ms.SetMessage("\n\n\n\tPlease specify in XML the script to use for installing this tool.");
                    ms.SetType("Warning");
                    ms.ShowDialog();
                    ms = null;
            }

            Bitmap img = MainWindow.s_instance.GetToolBitmap(rm.m_toolName); 

            if ( MainWindow.s_instance.IfNeededExecuteScriptTool(ref rm, rm.m_toolName, img, true) != true ) 
                return;

        }

        /**
         * BrowseButton_Click - process BROWSE button click
         *                    - open a folder browse dialog to locate the folder where the lool is installed
         *                    - refresh the grid
         * @param sender an object - used to get the row of browse button
         * @param e an EventArgs - not used
         **/
        private void BrowseButton_Click(object sender, EventArgs e)
        {
            SourceGrid.CellContext context = (SourceGrid.CellContext)sender;
            int ID = (int)(this.m_grid[context.Position.Row, (int)COL.INDEX].Value);

            Tool rm = (Tool)(Get(ID));
            if (rm == null) return;
                          
            FolderBrowserDialog dialog = new FolderBrowserDialog();

            dialog.Description = "Select the folder where the tool is already installed.";
            dialog.ShowNewFolderButton = false;
            dialog.RootFolder = Environment.SpecialFolder.MyComputer;
            if (Directory.Exists(rm.m_path))
            {
                System.IO.DirectoryInfo info = new DirectoryInfo(rm.m_path);
                dialog.SelectedPath = info.FullName;
            }
                           
            if (dialog.ShowDialog() == DialogResult.OK)
            {
                if (UpdatePath(ID, dialog.SelectedPath))
                {
                    InstallPathBase installPath = new InstallPathBase(dialog.SelectedPath, "");
                    rm.m_installPaths.Insert(0, installPath);

                    RefreshGridCellValues();
                }

            }
        }

 

        public bool HasInvalidRequiredInstallPath()
        {
            foreach (DictionaryEntry entry in m_objects)
            {
                Tool rm = (Tool)entry.Value;

                if (!rm.IsUsedTool())
                    continue;

                if (!rm.m_valid)
                {
                    if (rm.m_required)
                    {
                        if (Globals.s_workInConsole)
                        {
                            Console.WriteLine("ERROR: Tool " + rm.m_toolName + " have an invalid path! (" + rm.m_detectedPath + ")");
                        }
                        return true;
                    }
                }
                else 
                {
                    if (Globals.s_workInConsole)
                    {
                        Console.WriteLine("Tool " + rm.m_toolName + ": " + rm.m_detectedPath);
                    }
                }
            }

            return false;
        }

        public bool HasInvalidInstallPath()
        {
            foreach (DictionaryEntry entry in m_objects)
            {
                Tool rm = (Tool)entry.Value;

                if (!rm.m_valid)
                {
                    return true;
                }
            }

            return false;
        }

        public void DetectToolsInstallPath()
        {
            if (m_objects.Count == 0) return;

            int value = 0;

            if(Globals.s_workInConsole)
            {
                MainWindow.s_instance.m_loadingProgress.Value = 0;
                MainWindow.s_instance.m_detectingProgress.Value = 0;
            }

            int step = 100 / m_objects.Count;

            //System.Diagnostics.Stopwatch sw = new System.Diagnostics.Stopwatch();
            //sw.Start();

            foreach (DictionaryEntry entry in m_objects)
            {
                Tool rm = (Tool)entry.Value;
                //Console.WriteLine("checking " + entry.Key + " " + rm.m_toolName);

                rm.ResetErrors();
                //Console.WriteLine("    reset = " + sw.ElapsedMilliseconds);
                rm.DetectToolInstallPath();
                //Console.WriteLine("    detect " + sw.ElapsedMilliseconds);

                value += step;

                if (Globals.s_workInConsole)
                    Console.Write("\rDetecting Tools ..." + value + "%");
                else
                {
                    MainWindow.s_instance.m_loadingProgress.Value = MainWindow.s_instance.m_detectingProgress.Value = value;
                    MainWindow.s_instance.Refresh();
                }


            }

            MainWindow.s_instance.m_loadingProgress.Value = 100;
            MainWindow.s_instance.m_detectingProgress.Value = 100;

            if (Globals.s_workInConsole)
                Console.WriteLine("\rDetecting Tools... " + MainWindow.s_instance.m_loadingProgress.Value.ToString() + "%\n");
        }

        public void ApplyDetectToolsInstallPath()
        {
            foreach (DictionaryEntry entry in m_objects)
            {
                Tool rm = (Tool)entry.Value;

                rm.m_path = rm.m_detectedPath;
            }

            RefreshGridCellValues();
        }


    }



    public class CellBackColorAlternate : SourceGrid.Cells.Views.Cell
    {
        public CellBackColorAlternate(Color firstColor, Color secondColor)
        {
            FirstBackground = new DevAge.Drawing.VisualElements.BackgroundSolid(firstColor);
            SecondBackground = new DevAge.Drawing.VisualElements.BackgroundSolid(secondColor);
        }

        private DevAge.Drawing.VisualElements.IVisualElement mFirstBackground;
        public DevAge.Drawing.VisualElements.IVisualElement FirstBackground
        {
            get { return mFirstBackground; }
            set { mFirstBackground = value; }
        }

        private DevAge.Drawing.VisualElements.IVisualElement mSecondBackground;
        public DevAge.Drawing.VisualElements.IVisualElement SecondBackground
        {
            get { return mSecondBackground; }
            set { mSecondBackground = value; }
        }

        protected override void PrepareView(SourceGrid.CellContext context)
        {
            base.PrepareView(context);

            if (Math.IEEERemainder(context.Position.Row, 2) == 0)
                Background = FirstBackground;
            else
                Background = SecondBackground;
        }
    }


    public class ToolValueChangedEvent : ValueChangedEventBase
    {
       
        public ToolValueChangedEvent(ToolsList list):base(list)
        {
            
        }

        public override void OnKeyDown(SourceGrid.CellContext sender, System.Windows.Forms.KeyEventArgs e)
        {
            sender.Grid.Refresh();
        }
       
        public override void OnEditEnded(SourceGrid.CellContext sender, EventArgs e)
        {
            base.OnEditEnded(sender, e);


            SourceGrid.Grid grid = m_list.GetGrid();

            int ID = (int)(grid[(int)(sender.Position.Row), 0].Value);

            Tool rm = (Tool)(m_list.Get(ID));


            switch (sender.Position.Column)
            {
                
                case 3:

                    String new_path = (string)(grid[(int)(sender.Position.Row), (int)(sender.Position.Column)].Value);

                    if (((ToolsList)m_list).UpdatePath(ID, new_path))
                    {
                        InstallPathBase installPath = new InstallPathBase(new_path, "");
                        rm.m_installPaths.Insert(0, installPath);

                        m_list.RefreshGridCellValues();
                    }
                    break;

            };
            
            
            grid.Refresh();
        }
    }
}
