package nz.ac.waikato.modeljunit.storytest;

import java.awt.Component;

public class StoryTestGUIVisitor
   implements StoryTestVisitor<Component>
{
   public StoryTestGUIFactory mFactory;
   public StoryTestSuggestionVisitor mSuggestionVisitor;
   
   public StoryTestGUIVisitor(StoryTestGUIFactory factory,
                              StoryTestSuggestionVisitor visitor)
   {
      mFactory = factory;
      mSuggestionVisitor = visitor;
   }
   
   public Component visit(StoryTestInterface story, Object parent)
   {
      return (Component)story.accept(this, parent);
   }
   
   public Component visit(CalcTable table, Object parent)
   {
      System.out.println("CalcTable");
      StoryTestGUIInterface par = (StoryTestGUIInterface)parent;
      return (Component)mFactory.createCalcTableComponent(table, par);
   }
   
   public Component visit(StoryTest story, Object parent)
   {
      System.out.println("StoryTest");
      return (Component)mFactory.createStoryTestComponent(story, this,
                                                          mSuggestionVisitor);
   }
}
