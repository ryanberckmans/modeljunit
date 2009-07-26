package nz.ac.waikato.modeljunit.storytest;

import java.util.List;

public interface SuggestionStrategy
   extends Observer, Subject
{
   public List<Suggestion> getSuggestions();
   
   public CalcTable getCalcTable();
}
