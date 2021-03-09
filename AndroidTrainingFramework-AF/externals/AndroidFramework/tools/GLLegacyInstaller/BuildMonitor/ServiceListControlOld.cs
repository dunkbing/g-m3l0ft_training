using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using Utils;

namespace BuildMonitor
{

    

    public class ServiceListControlOld
    {
        private SourceGrid.Grid m_serviceGrid = null;
        //private Hashtable       m_services = new Hashtable();
        private List<HostInfoInternal> m_services = new List<HostInfoInternal>();

        public ServiceListControlOld(SourceGrid.Grid grid)
        {
            m_serviceGrid = grid;

            if (this.m_serviceGrid != null)
            {
                m_serviceGrid.Rows.Insert(0);
                m_serviceGrid.Redim(1, Globals.s_infoParams.Length + 1);

                //Add the last update column header
                var cell = new SourceGrid.Cells.ColumnHeader("Last Response");
                m_serviceGrid[0, 0] = cell;
                cell.View = new SourceGrid.Cells.Views.ColumnHeader();
                cell.View.TextAlignment = DevAge.Drawing.ContentAlignment.MiddleCenter;
                //cell.ResizeEnabled = true;
                cell.ToolTipText = "Time last update was recorded";
                m_serviceGrid.Columns[0].AutoSizeMode = SourceGrid.AutoSizeMode.EnableAutoSizeView;
                m_serviceGrid.Columns[0].MinimalWidth = 50;
                m_serviceGrid.Columns[0].MaximalWidth = 200;


                int i = 1;
                foreach (String columnName in Globals.s_infoParams)
                {
                    m_serviceGrid[0, i] = new SourceGrid.Cells.ColumnHeader(columnName);
                    m_serviceGrid[0, i].View = new SourceGrid.Cells.Views.ColumnHeader();

                    //m_serviceGrid[0, column].View.BackColor = Color.SteelBlue;
                    //m_serviceGrid[0, column].View.ForeColor = Color.Black;
                    m_serviceGrid[0, i].View.TextAlignment = DevAge.Drawing.ContentAlignment.MiddleCenter;

                    m_serviceGrid.Columns[i].MinimalWidth = 50;
                    m_serviceGrid.Columns[i].MaximalWidth = 500;

                    i++;
                }
            }
        }

        public delegate void AddRow_delegate(int key, String[] info);

        public void AddRow(int key, String[] info)
        {
            if (this.m_serviceGrid.InvokeRequired)
            {
                AddRow_delegate d = new AddRow_delegate(AddRow);
                this.m_serviceGrid.BeginInvoke(d, key, info);
            }
            else
            {
                HostInfoInternal infoInternal = new HostInfoInternal();
                infoInternal.hashCode = key;
                infoInternal.data = info;
                if (m_services.Contains(infoInternal))
                {
                    UpdateRow(infoInternal);
                }
                else
                {
                    NewRow(infoInternal);
                }
            }
        }

        private void UpdateRow(HostInfoInternal info)
        {
            int rowIndex = m_services.IndexOf(info);
            SetRowData(rowIndex, info);
        }

        private void NewRow(HostInfoInternal info)
        {
            m_services.Add(info);
            m_serviceGrid.Rows.Insert(m_services.Count);

            for (int i = 0; i < m_serviceGrid.ColumnsCount; i++)
            {
                m_serviceGrid[m_services.Count, i] = new SourceGrid.Cells.Cell();
            }

            SetRowData(m_services.Count - 1, info);
        }

        private void SetRowData(int rowIdx, HostInfoInternal info)
        {
            rowIdx++;
            int columnIndex = 0;
            m_serviceGrid[rowIdx, columnIndex].Value = DateTime.Now.ToString("HH:mm:ss tt");
            columnIndex++;

            foreach (String value in info.data)
            {
                if (columnIndex < m_serviceGrid.ColumnsCount)
                {
                    m_serviceGrid[rowIdx, columnIndex].Value = value;
                    columnIndex++;
                }
            }

            m_services[rowIdx - 1] = info;
        }
    }

    
}
