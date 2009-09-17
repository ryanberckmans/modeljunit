package nz.ac.waikato.modeljunit.storytest;

import java.awt.Component;

public interface StoryTestGUIFactory
{
   public Component createCalcTableComponent(CalcTable table,
                                             StoryTestGUIInterface parent);
   
   public Component createStoryTestComponent(StoryTest story,
                                             StoryTestGUIVisitor visitor,
                                             StoryTestSuggestionVisitor svisitor);
   
   public Component createCalcTableSuggestionComponent(CalcTable calc,
                                                       StoryTestGUIInterface par,
                                                       SuggestionStrategyFactory factory);
}
