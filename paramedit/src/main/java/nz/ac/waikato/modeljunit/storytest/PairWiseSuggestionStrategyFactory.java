package nz.ac.waikato.modeljunit.storytest;

public class PairWiseSuggestionStrategyFactory
   implements SuggestionStrategyFactory
{
   public static PairWiseSuggestionStrategyFactory INSTANCE = 
      new PairWiseSuggestionStrategyFactory();
   
   private PairWiseSuggestionStrategyFactory(){}
   
   public PairWiseSuggestionStrategy createSuggestionStrategy(CalcTable table)
   {
      return new PairWiseSuggestionStrategy(table);
   }
}
