package nz.ac.waikato.modeljunit.storytest;

import java.util.ArrayList;
import java.util.List;

public class DefaultStoryTestGUIFactory
   implements StoryTestGUIFactory
{
   public static DefaultStoryTestGUIFactory INSTANCE = 
      new DefaultStoryTestGUIFactory();
   
   private DefaultStoryTestGUIFactory(){}
   
   public CalcTablePanel createCalcTableComponent(CalcTable table,
                                                  StoryTestGUIInterface parent)
   {
      return new CalcTablePanel(table, parent);
   }
   
   public StoryTestPanel createStoryTestComponent(StoryTest story,
                                                  StoryTestGUIVisitor visitor,
                                                  StoryTestSuggestionVisitor svisitor)
   {
      return new StoryTestPanel(story, visitor, svisitor);
   }
   
   public CalcTableSuggestionPanel createCalcTableSuggestionComponent(CalcTable table, StoryTestGUIInterface parent)
   {
      List<SuggestionStrategy> strats = new ArrayList<SuggestionStrategy>();
      strats.add(new GuessSuggestionStrategy(table));
      //strats.add(new MCDCSuggestionStrategy(table));
      //JTable jtable = new JTable(new SuggestionsTableModel(strats, table));
      CalcTableSuggestionPanel mPan = new CalcTableSuggestionPanel(table,
                                                                   strats,
                                                                   parent);
      return mPan;
   }
}
