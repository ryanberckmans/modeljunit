package nz.ac.waikato.modeljunit.storytest;

public class MCDCSuggestionStrategyFactory
   implements SuggestionStrategyFactory
{
   public static MCDCSuggestionStrategyFactory INSTANCE = 
      new MCDCSuggestionStrategyFactory();
   
   private MCDCSuggestionStrategyFactory(){}
   
   public MCDCSuggestionStrategy createSuggestionStrategy(CalcTable table)
   {
      return new MCDCSuggestionStrategy(table);
   }
}
