package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

public class CreateCalcTableCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final CalcTable mTable;
   private final int mRow;
/**
Basic constructor for SetValueCommand
*/
   public CreateCalcTableCommand(CalcTable Table, int Row)
   {
      mTable = Table;
      mRow = Row;
   }
   
   public CreateCalcTableCommand(CalcTable Table)
   {
      this(Table, Table.rows());
   }
   
   public void execute()
   {
      mTable.addRow(mRow);
   }
   
   public void undo()
   {
      super.undo();
      mTable.removeRow(mRow);
   }
   
   public String getName()
   {
      return "Add Row";
   }
}
