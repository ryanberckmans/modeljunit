package nz.ac.waikato.modeljunit.storytest;

import javax.swing.JScrollPane;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import javax.swing.Action;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;

import nz.ac.waikato.modeljunit.command.Command;
import nz.ac.waikato.modeljunit.command.CreateCalcTableCommand;
import nz.ac.waikato.modeljunit.command.UndoInterface;
import nz.ac.waikato.modeljunit.command.UndoThing;

import java.io.File;
import java.util.Arrays;


public class StoryTestPanel
	extends JFrame
   implements Observer, StoryTestGUIInterface
{
   public static final long serialVersionUID = 1;
   
   private final UndoInterface mUndo;
   private final StoryTest mStory;
   private final StoryTestGUIVisitor mVisitor;
   private final StoryTestSuggestionVisitor mSuggestionVisitor;
   private final Container mView;
   private final JScrollPane mTopPane;
   private final JScrollPane mBottompane;
   private final JFileChooser mChooser;
   private final JPopupMenu mPopup;
   private StoryTestGUIInterface mParent = null;
   
   public StoryTestPanel(StoryTest story, StoryTestGUIVisitor visitor,
                         StoryTestSuggestionVisitor suggestionVisitor)
   {
      super();
      mChooser = new JFileChooser();
      JMenuBar mbar = new JMenuBar();
      JMenu menu = new JMenu("File");
      menu.add(new JMenuItem(new SaveAction()));
      menu.add(new JMenuItem(new LoadAction()));
      mbar.add(menu);
      mView = new JPanel();
      mView.setLayout(new BoxLayout(mView, BoxLayout.Y_AXIS));
      mUndo = new UndoThing();
      mStory = story;
      mStory.registerObserver(this);
      mVisitor = visitor;
      mSuggestionVisitor = suggestionVisitor;
      mStory.registerObserver(this);
      for (StoryTestInterface sti : mStory.getComponents()) {
         Component comp = (Component)mVisitor.visit(sti, this);
         mView.add(comp);
      }
      JScrollPane spane = new JScrollPane(mView);
      mTopPane = spane;
      mBottompane = new JScrollPane();
      JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitpane.setTopComponent(spane);
      splitpane.setBottomComponent(mBottompane);
      splitpane.resetToPreferredSizes();
      setJMenuBar(mbar);
      add(splitpane);
      pack();
      mPopup = new JPopupMenu();
      mPopup.add(new AddCalcTableAction());
      spane.addMouseListener(new MyMouseListener());
   }
   
   public StoryTestInterface getStoryTestInterface()
   {
      return mStory;
   }
   
   public StoryTestGUIInterface getStoryParent()
   {
      return mParent;
   }
   
   public void requestSuggestions(StoryTestInterface sti)
   {
      Component sug = (Component)mSuggestionVisitor.visit(sti, this);
      if (sug != null) {mBottompane.setViewportView(sug);}
   }
   
   public UndoInterface getUndoInterface()
   {
      return mUndo;
   }
   
   public void update()
   {
      mView.removeAll();
      for (StoryTestInterface sti : mStory.getComponents()) {
        Component comp = (Component)mVisitor.visit(sti, this);
        mView.add(comp);
      }
   }
   
   public static void main(String args[])
   {
      String[] strings = new String[] {"a","b","c"};
      CalcTable tab = new CalcTable("Table1", Arrays.asList(strings));
      tab.setValue(0,0,"1");
      tab.setValue(0,1,"2");
      tab.setValue(0,2,"3");
      tab.addRow();
      StoryTest story = new StoryTest();
      story.add(tab);
      story.add(tab);
      StoryTestSuggestionVisitor sugvisitor =
         new StoryTestSuggestionVisitor(DefaultStoryTestGUIFactory.INSTANCE);
      StoryTestGUIVisitor visitor =
         new StoryTestGUIVisitor(DefaultStoryTestGUIFactory.INSTANCE,
                                 sugvisitor);
      JFrame frame = (JFrame) visitor.visit(story, null);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setSize(400, 400);
      frame.setVisible(true);
   }
   
   private class AddCalcTableAction
     extends AbstractAction
     implements Action
   {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;

     public AddCalcTableAction()
     {
        super("Add CalcTable");
     }
     
     public void actionPerformed(ActionEvent e)
     {
       Command command = new CreateCalcTableCommand(mStory,
                                                    new CalcTable());
       getUndoInterface().execute(command);
     }
   }
   
   private class SaveAction
      extends AbstractAction
      implements Action
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public SaveAction()
      {
         super("Save");
      }
      
      public void actionPerformed(ActionEvent e)
      {         
         int returnVal = mChooser.showSaveDialog(StoryTestPanel.this);
         if(returnVal == JFileChooser.APPROVE_OPTION) {
           try{
            File saveto = mChooser.getSelectedFile();
            FileOutputStream fos = new FileOutputStream(saveto);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mStory);
            oos.close();
            } catch (Exception ex) {
             ex.printStackTrace();
           }
         }
      }
   }
   
   private class LoadAction
      extends AbstractAction
      implements Action
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public LoadAction()
      {
         super("Load");
      }
      
      public void actionPerformed(ActionEvent e)
      {         
         int returnVal = mChooser.showOpenDialog(StoryTestPanel.this);
         if(returnVal == JFileChooser.APPROVE_OPTION) {
           try {
              File loadfrom = mChooser.getSelectedFile();
              FileInputStream fis = new FileInputStream(loadfrom);
              ObjectInputStream ois = new ObjectInputStream(fis);
              StoryTest st = (StoryTest)ois.readObject();
              JFrame frame = (JFrame) mVisitor.visit(st, null);
              frame.setSize(400, 400);
              frame.setVisible(true);
              ois.close();
           } catch (Exception ex) {
              ex.printStackTrace();
           }
         }
      }
   }
   
   private class MyMouseListener
     extends MouseAdapter
    {
       public void mousePressed( MouseEvent e )
       {
          checkForTriggerEvent( e );
       }
    
       public void mouseReleased( MouseEvent e )
       { checkForTriggerEvent( e ); } 
    
       private void checkForTriggerEvent( MouseEvent e )
       {
          if ( e.isPopupTrigger() ) 
             mPopup.show( e.getComponent(),
                             e.getX(), e.getY() );
       }
    }
}
