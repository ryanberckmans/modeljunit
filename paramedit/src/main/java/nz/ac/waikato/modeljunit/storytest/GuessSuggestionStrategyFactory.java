package nz.ac.waikato.modeljunit.storytest;

public class GuessSuggestionStrategyFactory
   implements SuggestionStrategyFactory
{
   public static GuessSuggestionStrategyFactory INSTANCE = 
      new GuessSuggestionStrategyFactory();
   
   private GuessSuggestionStrategyFactory(){}
   
   public GuessSuggestionStrategy createSuggestionStrategy(CalcTable table)
   {
      return new GuessSuggestionStrategy(table);
   }
}
