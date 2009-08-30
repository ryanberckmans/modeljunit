package nz.ac.waikato.modeljunit.command;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import nz.ac.waikato.modeljunit.storytest.AbstractSubject;
import nz.ac.waikato.modeljunit.storytest.Observer;

public class UndoThing
   extends AbstractSubject
   implements UndoInterface
{
   private UndoManager mUndo;
   private final UndoAction mUA;
   private final RedoAction mRA;
   
   public UndoThing()
   {
      super();
      mUndo = new UndoManager();
      mUA = new UndoAction();
      mRA = new RedoAction();
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
   
   public Action getUndoAction()
   {
     return mUA;
   }
   
   public Action getRedoAction()
   {
     return mRA;
   }
   
   private class UndoAction
     extends AbstractAction
     implements Action, Observer
  {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
  
     public UndoAction()
     {
        super("Undo");
        registerObserver(this);
        update();
     }
     
     public void update()
     {
        setEnabled(canUndo());
     }
     
     public void actionPerformed(ActionEvent e)
     {
        System.out.println("undo");
        if (canUndo()) {undo();}
     }
  }
  
  private class RedoAction
     extends AbstractAction
     implements Action, Observer
  {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
  
     public RedoAction()
     {
        super("Redo");
        registerObserver(this);
        update();
     }
     
     public void update()
     {
        setEnabled(canRedo());
     }
     
     public void actionPerformed(ActionEvent e)
     {
        if (canRedo()) {redo();}
     }
  }
}
