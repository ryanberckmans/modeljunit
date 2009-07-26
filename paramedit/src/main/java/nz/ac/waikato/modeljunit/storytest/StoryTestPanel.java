package nz.ac.waikato.modeljunit.storytest;

import javax.swing.JScrollPane;

import nz.ac.waikato.modeljunit.command.UndoThing;
import nz.ac.waikato.modeljunit.command.UndoInterface;
import javax.swing.JFrame;
import java.util.Arrays;
import javax.swing.WindowConstants;
import java.awt.Component;
import javax.swing.JPanel;
import java.awt.Container;
import javax.swing.JSplitPane;

public class StoryTestPanel
	extends JSplitPane
   implements Observer, StoryTestGUIInterface
{
   public static final long serialVersionUID = 1;
   
   private final UndoInterface mUndo;
   private final StoryTest mStory;
   private final StoryTestGUIVisitor mVisitor;
   private final StoryTestSuggestionVisitor mSuggestionVisitor;
   private final Container mView;
   private final JScrollPane mBottompane;
   private StoryTestGUIInterface mParent = null;
   
   public StoryTestPanel(StoryTest story, StoryTestGUIVisitor visitor,
                         StoryTestSuggestionVisitor suggestionVisitor)
   {
      super(JSplitPane.VERTICAL_SPLIT);
      mView = new JPanel();
      mUndo = new UndoThing();
      mStory = story;
      mVisitor = visitor;
      mSuggestionVisitor = suggestionVisitor;
      mStory.registerObserver(this);
      for (StoryTestInterface sti : mStory.getComponents()) {
         Component comp = (Component)mVisitor.visit(sti, this);
         mView.add(comp);
      }
      JScrollPane spane = new JScrollPane(mView);
      mBottompane = new JScrollPane();
      setTopComponent(spane);
      setBottomComponent(mBottompane);
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
      System.out.println("implement StoryTestPanel's observer interface");
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
      JFrame frame = new JFrame("frame");
      frame.add((Component)visitor.visit(story, null));
      frame.pack();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
