package nz.ac.waikato.modeljunit.command;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.AbstractUndoableEdit;

public abstract class AbstractUndoableCommand
   extends AbstractUndoableEdit
   implements UndoableEdit, Command
{
	public static final long serialVersionUID = 1;
	
   public String getPresentationName()
   {
      return getName();
   }
   
   public void redo()
   {
      super.redo();
      execute();
   }
}
