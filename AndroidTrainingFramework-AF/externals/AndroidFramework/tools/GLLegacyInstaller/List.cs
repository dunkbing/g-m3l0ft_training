using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using System.Drawing;
using System.Windows.Forms;
using System.Xml;

namespace GLLegacyInstaller
{
    public class RList
    {
        public SourceGrid.Grid m_grid = null;
        public int m_gridRows = 0;

        public ValueChangedEventBase m_controller = null;

        protected Hashtable m_objects = new Hashtable();
        //protected Dictionary<int, Tool> m_objects = new Dictionary<int, Tool>();


        public RList(SourceGrid.Grid grid)
        {
            this.m_grid = grid;
        }

        public virtual void SetValueController(ValueChangedEventBase valueController)
        {
            m_controller = valueController;
        }

        public virtual SourceGrid.Grid GetGrid()
        {
            return m_grid;
        }

        public virtual Object Get(int ID)
        {
            return (Object)(m_objects[ID]);
        }

        public virtual Object Get(String ID)
        {
            return (Object)(m_objects[ID]);
        }

        public virtual int GetCount()
        {
            return m_objects.Count;
        }

        public virtual IDictionaryEnumerator GetEnumerator()
        {
            return m_objects.GetEnumerator();
        }

        public virtual void RefreshGridStructure()
        {

        }

        public virtual void Save(XmlWriter writer)
        {

        }
        public virtual void Load(XmlReader reader)
        {

        }

        public virtual void Export(System.IO.FileStream file)
        {

        }

        public virtual void RemoveSelected()
        {
            int[] selectedRows = m_grid.Selection.GetSelectionRegion().GetRowsIndex();

            int length = selectedRows.Length;

            if (length == 0)
            {
                MessageBox.Show("There is no selected item!", "No item selected",
                                MessageBoxButtons.OK, MessageBoxIcon.Exclamation);

                return;
            }

            for (int i = length-1; i >= 0; i--)
            {
                int ID = (int)(m_grid[selectedRows[i], 1].Value);

                m_objects.Remove(ID);

                m_grid.Rows.Remove(selectedRows[i]);

            }
        }

        public virtual int[] GetSelected()
        {
            
            int[] selectedRows = m_grid.Selection.GetSelectionRegion().GetRowsIndex();
            int[] relectedModules = new int[selectedRows.Length];

            for (int i = 0; i < selectedRows.Length; i++ )
            {
                int ID = (int)(m_grid[selectedRows[i], 1].Value);

                relectedModules[i] = ID;
            }

            return relectedModules;
        }

        public void CreateColumnHeader(int column, String name)
        {
            if (this.m_grid != null)
            {
                m_grid[0, column] = new SourceGrid.Cells.ColumnHeader(name);
                m_grid[0, column].View = new SourceGrid.Cells.Views.ColumnHeader();
                m_grid[0, column].View.BackColor = Color.SteelBlue;
                m_grid[0, column].View.ForeColor = Color.Black;
                m_grid[0, column].View.TextAlignment = DevAge.Drawing.ContentAlignment.MiddleCenter;
            }
        }

        public void CreateColumnHeader(int column, String name, int minimalWidth, int maximalWidth)
        {
            if (this.m_grid != null)
            {
                CreateColumnHeader(column, name);

                m_grid.Columns[column].MinimalWidth = minimalWidth;
                m_grid.Columns[column].MaximalWidth = maximalWidth;
            }
        }

        public virtual void RefreshGridCellValues()
        {

        }
        public virtual void RefreshGridContent()
        {

        }

    }

    public class ValueChangedEventBase : SourceGrid.Cells.Controllers.ControllerBase
    {
        public RList m_list = null;

        public ValueChangedEventBase(RList list)
        {
            m_list = list;
        }
    }

    /// <summary>
    /// Customized View to draw a rounded background
    /// </summary>
    class RoundView : SourceGrid.Cells.Views.Cell
    {
        public RoundView(System.Drawing.Color color)
        {
            TextAlignment = DevAge.Drawing.ContentAlignment.MiddleCenter;
            base.Background = new BackVisualElement(color);
            Border = DevAge.Drawing.RectangleBorder.NoBorder;
            ImageAlignment = DevAge.Drawing.ContentAlignment.MiddleCenter;
        }

      /*  public double Round
        {
            get { return mBackground.Round; }
            set { mBackground.Round = value; }
        }*/

        //private BackVisualElement mBackground = new BackVisualElement(System.Drawing.Color color);

        private class BackVisualElement : DevAge.Drawing.VisualElements.VisualElementBase
        {
            #region Constuctor
            /// <summary>
            /// Default constructor
            /// </summary>
            public BackVisualElement( System.Drawing.Color color)
            {
                mColor = color;
            }

            /// <summary>
            /// Copy constructor
            /// </summary>
            /// <param name="other"></param>
            public BackVisualElement(BackVisualElement other)
                : base(other)
            {
                Round = other.Round;
            }
            #endregion
            /// <summary>
            /// Clone
            /// </summary>
            /// <returns></returns>
            public override object Clone()
            {
                return new BackVisualElement(this);
            }

            private double mRound = 0.5;
            System.Drawing.Color mColor;
            public double Round
            {
                get { return mRound; }
                set { mRound = value; }
            }

            protected override void OnDraw(DevAge.Drawing.GraphicsCache graphics, RectangleF area)
            {
                DevAge.Drawing.RoundedRectangle rounded = new DevAge.Drawing.RoundedRectangle(Rectangle.Round(area), Round);
                using (System.Drawing.Drawing2D.LinearGradientBrush brush = new System.Drawing.Drawing2D.LinearGradientBrush(area, mColor, Color.WhiteSmoke, System.Drawing.Drawing2D.LinearGradientMode.Vertical))
                {
                    graphics.Graphics.FillRegion(brush, new Region(rounded.ToGraphicsPath()));
                }


                //Border
                DevAge.Drawing.Utilities.DrawRoundedRectangle(graphics.Graphics, rounded, Pens.Olive);

            }

            protected override SizeF OnMeasureContent(DevAge.Drawing.MeasureHelper measure, SizeF maxSize)
            {
                return SizeF.Empty;
            }
        }
    }
}
