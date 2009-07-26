package nz.ac.waikato.modeljunit.storytest;

import java.util.List;

public class DefaultSuggestion
   implements Suggestion
{
   private final List<String> mSuggestion;
   
   public DefaultSuggestion(List<String> suggestion)
   {
      mSuggestion = suggestion;
   }
   
   public List<String> getFields()
   {
      //TODO might make this unmodifiable
      return mSuggestion;
   }
   
   public boolean equals(Object o)
   {
      if (!(o instanceof Suggestion)) {return false;}
      List<String> sug = ((Suggestion)o).getFields();
      return mSuggestion.equals(sug);
   }
   
   public int hashCode()
   {
      return mSuggestion.hashCode();
   }
}
