package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

public class SetColumnNameCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final CalcTable mTable;
   private final int mColumn;
   private final String mValue;
   private final String mOldValue;

/**
Basic constructor for SetValueCommand
*/
   public SetColumnNameCommand(CalcTable Table, int Column, String Value,
                               String OldValue)
   {
      mTable = Table;
      mColumn = Column;
      mValue = Value;
      mOldValue = OldValue;
   }
   
   public void execute()
   {
      System.out.println("execute");
      mTable.setColumnHeader(mColumn, mValue);
   }
   
   public void undo()
   {
      super.undo();
      mTable.setColumnHeader(mColumn, mOldValue);
   }
   
   public String getName()
   {
      return "Set Value";
   }
}
