package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

public class DeleteColumnCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final CalcTable mTable;
   private final int mColumn;
   private final String mName;
   private final String[] mValues;
   
   /**
    *Basic constructor for SetValueCommand
    */
   public DeleteColumnCommand(CalcTable Table, int Column)
   {
      mTable = Table;
      mColumn = Column;
      mName = Table.getColumnHeader(mColumn);
      mValues = new String[mTable.rows()];
      for(int row = 0; row < mValues.length; row++) {
          mValues[row] = mTable.getValue(row, mColumn);
      }
   }
   
   public DeleteColumnCommand(CalcTable Table)
   {
      this(Table, Table.columns() - 1);
   }
   
   public void execute()
   {
      mTable.removeColumn(mColumn);
   }
   
   public void undo()
   {
      super.undo();
      mTable.addColumn(mName, mColumn);
      for(int row = 0; row < mValues.length; row++) {
          mTable.setValue(row, mColumn, mValues[row]);
      }
   }
   
   public String getName()
   {
      return "Delete Column";
   }
}
