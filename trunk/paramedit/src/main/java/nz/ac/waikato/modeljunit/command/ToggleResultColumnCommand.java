package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

public class ToggleResultColumnCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final CalcTable mTable;
   private final int mColumn;
/**
Basic constructor for SetValueCommand
*/
   public ToggleResultColumnCommand(CalcTable Table, int Column)
   {
      mTable = Table;
      mColumn = Column;
   }
   
   public void execute()
   {
      mTable.toggleResultColumn(mColumn);
   }
   
   public void undo()
   {
      super.undo();
      execute();
   }
   
   public String getName()
   {
      return "Toggle Result";
   }
}
