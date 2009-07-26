package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

public class SetValueCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final CalcTable mTable;
   private final int mRow;
   private final int mColumn;
   private final String mValue;
   private final String mOldValue;

/**
Basic constructor for SetValueCommand
*/
   public SetValueCommand(CalcTable Table, int Row, int Column, String Value,
                          String OldValue)
   {
      mTable = Table;
      mRow = Row;
      mColumn = Column;
      mValue = Value;
      mOldValue = OldValue;
   }
   
   public void execute()
   {
      System.out.println("execute");
      mTable.setValue(mRow, mColumn, mValue);
   }
   
   public void undo()
   {
      super.undo();
      mTable.setValue(mRow, mColumn, mOldValue);
   }
   
   public String getName()
   {
      return "Set Value";
   }
}
