package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

public class AddColumnCommand
   extends AbstractUndoableCommand
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private final CalcTable mTable;
   private final int mColumn;
/**
Basic constructor for SetValueCommand
*/
   public AddColumnCommand(CalcTable Table, int Column)
   {
      mTable = Table;
      mColumn = Column;
   }
   
   public AddColumnCommand(CalcTable Table)
   {
      this(Table, Table.columns());
   }
   
   public void execute()
   {
      mTable.addColumn("", mColumn);
   }
   
   public void undo()
   {
      super.undo();
      mTable.removeColumn(mColumn);
   }
   
   public String getName()
   {
      return "Add Column";
   }
}
