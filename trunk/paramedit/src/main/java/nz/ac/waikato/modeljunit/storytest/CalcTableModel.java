package nz.ac.waikato.modeljunit.storytest;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A wrapper around a CalcTable to use it in JTable
 */
public class CalcTableModel
   extends AbstractTableModel
   implements TableModel, Observer
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   /** The backing table */
   private final CalcTable mTable;
   
   /**
    * Constructs a basic CalcTableModel using table
    */
   public CalcTableModel(CalcTable table)
   {
      mTable = table;
      mTable.registerObserver(this);
   }
   
   public Class<String> getColumnClass(int column)
   {
      return String.class;
   }
   
   public int getRowCount()
   {
      return mTable.rows() + 2;
   }
   
   public int getColumnCount()
   {
      return mTable.columns();
   }
   
   public boolean isCellEditable(int row, int column)
   {
      return mTable.isEditable(column);
   }
   
   public void setValueAt(String aValue, int row, int column)
   {
      System.out.println("for some reason this isn't called at the moment");
      assert(false);
      System.out.println("set value" + row + "," + column);
      mTable.setValue(row, column, aValue);
   }
   
   public String getValueAt(int row, int column)
   {
      return row == 0 ? mTable.getColumnHeader(column) :
             row == 1 ? mTable.getTypeRange(column).toString() :
                        mTable.getValue(row - 2, column);
   }
   
   public String getColumnName(int column)
   {
      return mTable.getColumnHeader(column);
   }
   
   public void update()
   {
      fireTableStructureChanged();
   }
}
