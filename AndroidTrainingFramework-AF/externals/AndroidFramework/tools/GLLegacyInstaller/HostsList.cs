using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing;
using System.Collections;
using System.Xml;
using System.Windows.Forms;
using System.IO;
using Utils;
using System.Threading;


namespace GLLegacyInstaller
{
    public class HostsList: RList
    {
        //private Mutex m_mutex = new Mutex();
        public bool m_bSelectionChanged = false;
        const String m_str_NO = "No";
        //const String m_str_hostName = "host";


        CellBackColorAlternate m_normalView = new CellBackColorAlternate(Color.Khaki, Color.DarkKhaki);
        CellBackColorAlternate m_linkView = new CellBackColorAlternate(Color.Khaki, Color.DarkKhaki);
        CellBackColorAlternate m_errorView = new CellBackColorAlternate(Color.Red, Color.Red);
        CellBackColorAlternate m_warningView = new CellBackColorAlternate(Color.Orange, Color.Orange);


        public HostsList(SourceGrid.Grid grid)
            : base(grid)
        {
            RefreshGridStructure();                   
            grid.Click += new System.EventHandler(this.Grid_Click);
            m_linkView.ForeColor = Color.Blue;
        }


        public delegate void RefreshGridStructure_callback();
        public override void RefreshGridStructure()
        {
            if (this.m_grid != null)
            {
                if (this.m_grid.InvokeRequired)
                {
                    RefreshGridStructure_callback d = new RefreshGridStructure_callback(RefreshGridStructure);
                    this.m_grid.BeginInvoke(d);
                    d = null;
                }
                else
                {
                    this.m_grid.Rows.Clear();
                    this.m_grid.Columns.Clear();

                    SetValueController(new HostValueChangedEvent(this));

                    this.m_grid.ColumnsCount = 2 + Globals.s_infoParams.Length;
                    this.m_grid.Rows.Insert(0);
                    this.m_grid.FixedRows = 1;
                    this.m_grid.FixedColumns = 1;


                    CreateColumnHeader(0, m_str_NO, 23, 23);
                    CreateColumnHeader(1, "USE", 25, 120);
                    for (int j = 0; j < Globals.s_infoParams.Length; j++)
                    {
                        int size = 40;
                        if (Globals.s_infoParams[j] == "HOST")
                            size = 75;
                        else if (Globals.s_infoParams[j] == "SERVER")
                            size = 55;

                        CreateColumnHeader(2 + j, Globals.s_infoParams[j], size, 120);
                    }

                    this.m_grid.Refresh();
                }
            }
        }


        public override void Save(XmlWriter writer)
        {
        }

        public override void Load(XmlReader reader)
        {
            
        }

        public void GetWantedHosts()
        {
            String hosts="";
            //starts from 1 because the 0 is for column header
            for (int i = 1; i < m_grid.RowsCount; i++)
            {
                bool b = (bool)this.m_grid[i, 1].Value;
                if (b)
                {
                    if(hosts!="") 
                        hosts += " ";                
                    hosts += (string)(this.m_grid[i, 2].Value);
                }
            }
            if (m_bSelectionChanged)
                Globals.s_distccWantedHosts = hosts;
        }


        public delegate void RefreshGridCellValues_callback();
        public override void RefreshGridCellValues()
        {
            if (this.m_grid != null)
            {
                if (this.m_grid.InvokeRequired)
                {
                    RefreshGridCellValues_callback d = new RefreshGridCellValues_callback(RefreshGridCellValues);
                    this.m_grid.BeginInvoke(d);
                    d = null;
                }
                else
                {
                    //starts from 1 because the 0 is for column header
                    for (int i = 1; i < m_grid.RowsCount; i++) 
                    {
                        String ID = (string)(this.m_grid[i, 2].Value);//

                        if (ID == null) continue;
                        Host rm = (Host)Get(ID);
                        if (Globals.s_bShowOnlyServers && rm.m_params[2].ToString() != "1")
                            continue;
                        this.m_grid[i, 0].View = m_normalView;


                        for (int j = 0; j < Globals.s_infoParams.Length; j++)
                        {
                            this.m_grid[i, 2 + j].View = m_normalView;
                            this.m_grid[i, 2 + j].Value = rm.m_params[j];
                        }         
                    }

                    m_grid.Refresh();
                }
            }
            
        }

        

        public override void RefreshGridContent()
        {
            RefreshGridStructure();

            foreach (DictionaryEntry entry in m_objects)
            {
                Host rm = (Host)entry.Value;

                if (Globals.s_bShowOnlyServers && rm.m_params[2].ToString() != "1")
                    continue;
                AddToGrid(rm);
            }
        }

        public void Add(Host rm)
        {
            int size = m_objects.Count;

            m_objects[rm.m_params[0]] = rm;

            if (size != m_objects.Count)
            {
                if (Globals.s_bShowOnlyServers && rm.m_params[2].ToString() != "1")
                    return;
                AddToGrid(rm);
            }
            else
                RefreshGridCellValues();
        }

        public delegate int AddToGrid_callback(Host rm);

        int AddToGrid(Host rm)
        {
            int gridRows = 0;
            
            if (this.m_grid != null)
            {
                if (this.m_grid.InvokeRequired)
                {
                    AddToGrid_callback d = new AddToGrid_callback(AddToGrid);
                    this.m_grid.BeginInvoke(d, rm);
                    d = null;
                }
                else
                {
                    gridRows = this.m_grid.RowsCount;

                    this.m_grid.Rows.Insert(gridRows);

                    this.m_grid[gridRows, 0] = new SourceGrid.Cells.Cell(gridRows);   //ID
                    this.m_grid[gridRows, 1] = new SourceGrid.Cells.CheckBox();   //checkbox

                    if (Globals.s_hostsWanted != null)
                    {
                        for (int i = 0; i < Globals.s_hostsWanted.Length; i++)
                        {
                            if (Globals.s_hostsWanted[i] == rm.m_params[0])
                            {
                                this.m_grid[gridRows, 1].Value = true;
                                break;
                            }
                        }
                    }
                    for (int j = 0; (j < Globals.s_infoParams.Length) && (j < rm.m_params.Length); j++)
                            this.m_grid[gridRows, 2+j] = new SourceGrid.Cells.Cell(rm.m_params[j]);    //m_str_toolName

                }

            }

            return gridRows;
        }


        private void Grid_Click(object sender, EventArgs e)
        {
            m_grid.Select();
            SourceGrid.Position nCellPos = m_grid.Selection.ActivePosition;
            if (nCellPos.Column == 1)
                m_bSelectionChanged = true;
        }
    }

    public class HostValueChangedEvent : ValueChangedEventBase
    {
       
        public HostValueChangedEvent(HostsList list):base(list)
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

            String ID = (String)(grid[(int)(sender.Position.Row), 1].Value);

            Host rm = (Host)(m_list.Get(ID));


            switch (sender.Position.Column)
            {
                
                case 1:

                    break;

            };
            
            
            grid.Refresh();
        }
    }
}
