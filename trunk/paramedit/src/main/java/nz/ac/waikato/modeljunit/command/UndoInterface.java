package nz.ac.waikato.modeljunit.command;

import javax.swing.Action;

import nz.ac.waikato.modeljunit.storytest.Subject;

public interface UndoInterface
   extends Subject
{
   /**
    * Executes the Command c and add it to the UndoManager if it is Undoable
    */
   public void execute(Command c);
   
   public void undo();
   
   public void redo();
   
   public boolean canUndo();
   
   public boolean canRedo();
   
   public Action getUndoAction();
   
   public Action getRedoAction();
}
