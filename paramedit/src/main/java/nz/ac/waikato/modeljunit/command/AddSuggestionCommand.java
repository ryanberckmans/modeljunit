package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.CalcTable;
import nz.ac.waikato.modeljunit.storytest.Suggestion;

public class AddSuggestionCommand
   extends AbstractUndoableCommand
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private final CalcTable mTable;
   private final Suggestion mSuggestion;
   /**
    * Basic constructor for SetValueCommand
    */
   public AddSuggestionCommand(CalcTable Table, Suggestion suggestion)
   {
      mTable = Table;
      mSuggestion = suggestion;
   }
   
   public void execute()
   {
      mTable.addRow(mTable.rows(), mSuggestion.getFields());
   }
   
   public void undo()
   {
      super.undo();
      mTable.removeRow(mTable.rows() - 1);
   }
   
   public String getName()
   {
      return "Add Suggestion";
   }
}
