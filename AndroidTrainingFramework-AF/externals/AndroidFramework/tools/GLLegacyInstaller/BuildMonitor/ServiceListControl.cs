using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using Utils;
using System.Windows.Forms;

namespace BuildMonitor
{
    
    public class HostInfoInternal
    {
        public int hashCode;
        public String[] data;

        public override bool Equals(System.Object obj)
        {
            // If parameter is null return false.
            if (obj == null)
            {
                return false;
            }

            // If parameter cannot be cast to Point return false.
            HostInfoInternal h2 = obj as HostInfoInternal;
            if ((System.Object)h2 == null)
            {
                return false;
            }

            // Return true if the fields match:
            return Equals(h2);
        }


        public bool Equals(HostInfoInternal h2)
        {
            return this.hashCode == h2.hashCode;
        }
    }

    public class ServiceListControl
    {
        private List<String> m_columnNames = null;
        private DataGridView m_serviceGrid = null;
        //private Hashtable       m_services = new Hashtable();
        private List<HostInfoInternal> m_services = new List<HostInfoInternal>();

        public ServiceListControl(DataGridView grid)
        {
            m_serviceGrid = grid;

            if (this.m_serviceGrid != null)
            {
                m_columnNames = new List<String>  { "Last Response" };
                m_columnNames.AddRange(Globals.s_infoParams);

                foreach (String columnName in m_columnNames)
                {
                    DataGridViewColumn newColumn = new DataGridViewTextBoxColumn();
                    newColumn.HeaderText = columnName;
                    newColumn.Name = columnName;
                    m_serviceGrid.Columns.Add(newColumn);
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
            //Search the host name column for a match
            int rowIndex = 0;

            while (m_serviceGrid[1, rowIndex].Value.GetHashCode() != info.hashCode
                    && rowIndex < m_serviceGrid.RowCount)
            {
                rowIndex++;
            }

            if (rowIndex < m_serviceGrid.RowCount)
                SetRowData(rowIndex, info);
        }

        private void NewRow(HostInfoInternal info)
        {
            m_services.Add(info);
            m_serviceGrid.Rows.Add();
            
            SetRowData(m_services.Count - 1, info);
        }

        private void SetRowData(int rowIdx, HostInfoInternal info)
        {
            int columnIndex = 0;
            m_serviceGrid[columnIndex, rowIdx].Value = DateTime.Now.ToString("HH:mm:ss tt");
            columnIndex++;

            foreach (String value in info.data)
            {
                if (columnIndex < m_serviceGrid.ColumnCount)
                {
                    m_serviceGrid[columnIndex, rowIdx].Value = value;
                    columnIndex++;
                }
            }

            m_services[rowIdx] = info;
        }
    }

    
}
