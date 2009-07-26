package nz.ac.waikato.modeljunit.command;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import nz.ac.waikato.modeljunit.storytest.AbstractSubject;

public class UndoThing
   extends AbstractSubject
   implements UndoInterface
{
   private UndoManager mUndo;
   
   public UndoThing()
   {
      super();
      mUndo = new UndoManager();
   }
   
   /**
    * Executes the Command c and add it to the UndoManager if it is Undoable
    */
   public void execute(Command c)
   {
      c.execute();
      if (c instanceof UndoableEdit) {
         mUndo.addEdit((UndoableEdit)c);
      }
      inform();
   }
   
   public void undo()
   {
      mUndo.undo();
      inform();
   }
   
   public void redo()
   {
      mUndo.redo();
      inform();
   }
   
   public boolean canUndo()
   {
      return mUndo.canUndo();
   }
   
   public boolean canRedo()
   {
      return mUndo.canRedo();
   }
}
