package nz.ac.waikato.modeljunit.storytest;

import java.util.List;

public abstract class AbstractSuggestionStrategy
   extends AbstractSubject
   implements Observer, Subject, SuggestionStrategy
{
   private final CalcTable mCalc;
   
   public AbstractSuggestionStrategy(CalcTable calc)
   {
      mCalc = calc;
      mCalc.registerObserver(this);
   }
   
   public abstract List<Suggestion> getSuggestions();
   
   public CalcTable getCalcTable()
   {
      return mCalc;
   }
}
