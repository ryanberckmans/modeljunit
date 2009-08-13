package nz.ac.waikato.modeljunit.storytest;

import java.util.List;

public class RowsSuggestion
   implements Suggestion
{
   private final List<String> mSuggestion;
   private final int[] mRows;
   private final CalcTable mCalc;
   
   public RowsSuggestion(List<String> suggestion, int[] rows, CalcTable calc)
   {
      mSuggestion = suggestion;
      mRows = rows;
      mCalc = calc;
   }
   
   public List<String> getFields()
   {
      //TODO might make this unmodifiable
      return mSuggestion;
   }
   
   public void selected()
   {
      mCalc.setHighlighted(mRows);
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
