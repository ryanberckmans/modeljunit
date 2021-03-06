package nz.ac.waikato.modeljunit.storytest;

import java.awt.Component;
import javax.swing.JPanel;

public class StoryTestSuggestionVisitor
   implements StoryTestVisitor<Component>
{
   public StoryTestGUIFactory mFactory;
   
   private SuggestionStrategyFactory mSuggestionStrategyFactory =
     GuessSuggestionStrategyFactory.INSTANCE;
   
   public StoryTestSuggestionVisitor(StoryTestGUIFactory factory)
   {
      mFactory = factory;
   }
   
   public void setSuggestionStrategyFactory(SuggestionStrategyFactory factory)
   {
     mSuggestionStrategyFactory = factory;
   }
   
   public Component visit(StoryTestInterface story, Object other)
   {
      return (Component)story.accept(this, other);
   }
   
   public Component visit(CalcTable table, Object other)
   {
      System.out.println("CalcTable");
      StoryTestGUIInterface parent = (StoryTestGUIInterface)other;
      return (Component)mFactory.createCalcTableSuggestionComponent(table, parent, mSuggestionStrategyFactory);
   }
   
   public Component visit(StoryTest story, Object other)
   {
      System.out.println("StoryTest");
      return (Component)(new JPanel());
   }
}
