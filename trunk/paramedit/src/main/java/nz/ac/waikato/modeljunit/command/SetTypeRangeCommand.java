package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;
import nz.ac.waikato.modeljunit.storytest.parse.TypeRange;

public class SetTypeRangeCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final CalcTable mTable;
   private final int mColumn;
   private final TypeRange mValue;
   private final TypeRange mOldValue;

/**
Basic constructor for SetValueCommand
*/
   public SetTypeRangeCommand(CalcTable Table, int Column, TypeRange newr)
   {
      mTable = Table;
      mColumn = Column;
      mValue = newr;
      mOldValue = mTable.getTypeRange(mColumn);
   }
   
   public void execute()
   {
      // System.out.println("execute");
      mTable.setTypeRange(mColumn, mValue);
   }
   
   public void undo()
   {
      super.undo();
      mTable.setTypeRange(mColumn, mOldValue);
   }
   
   public String getName()
   {
      return "Set Value";
   }
}
