package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;
import java.util.Arrays;

public class DeleteRowCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final CalcTable mTable;
   private final int mRow;
   private final String[] mValues;
/**
Basic constructor for SetValueCommand
*/
   public DeleteRowCommand(CalcTable Table, int Row)
   {
      mTable = Table;
      mRow = Row;
      mValues = new String[mTable.columns()];
      for(int column = 0; column < mValues.length; column++) {
          mValues[column] = mTable.getValue(mRow, column);
      }
   }
   
   public DeleteRowCommand(CalcTable Table)
   {
      this(Table, Table.rows() - 1);
   }
   
   public void execute()
   {
      mTable.removeRow(mRow);
   }
   
   public void undo()
   {
      super.undo();
      mTable.addRow(mRow, Arrays.asList(mValues));
   }
   
   public String getName()
   {
      return "Delete Row";
   }
}
