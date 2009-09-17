package nz.ac.waikato.modeljunit.storytest;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.lang.NumberFormatException;

public class MCDCSuggestionStrategy
   extends AbstractSuggestionStrategy
   implements Observer, Subject, SuggestionStrategy
{
   private final List<Suggestion> mSuggestions;
   
   public MCDCSuggestionStrategy(CalcTable calc)
   {
      super(calc);
      mSuggestions = new ArrayList<Suggestion>();
      MCDC();
   }
   
   private void MCDC()
   {
      CalcTable calc = getCalcTable();
      Set<Suggestion> alreadythier = new HashSet<Suggestion>();
      for (int r = 0; r < calc.rows(); r++) {
         List<String> thing = new ArrayList<String>(calc.getRow(r));
         for (int c = 0; c < calc.columns(); c++) {
            if (calc.isResult(c)) {thing.set(c, "?");}
         }
         alreadythier.add(new DefaultSuggestion(thing));
      }
      Set<Suggestion> suggestionset = new HashSet<Suggestion>(alreadythier);
      List<Suggestion> suggestions = new ArrayList<Suggestion>();
      for (int c = 0; c < calc.columns(); c++) {
         if (calc.isResult(c)) {continue;}
         for (int r = 0; r < calc.rows(); r++) {
            try {
               int value = Integer.parseInt(calc.getValue(r, c));
               List<String> possible1 = new ArrayList<String>(calc.getRow(r));
               List<String> possible2 = new ArrayList<String>(calc.getRow(r));
               possible1.set(c, ((value - 1) + ""));
               possible2.set(c, ((value + 1) + ""));
               for (int c2 = 0; c2 < calc.columns(); c2++) {
                  if (calc.isResult(c2)) {
                     possible1.set(c2, "?"); possible2.set(c2, "?");
                  }
               }
               Suggestion suggestion1 = new DefaultSuggestion(possible1);
               Suggestion suggestion2 = new DefaultSuggestion(possible2);
               if (alreadythier.add(suggestion1)
                   && alreadythier.add(suggestion2)) {
                  if (suggestionset.add(suggestion1)) {suggestions.add(suggestion1);}
                  if (suggestionset.add(suggestion2)) {suggestions.add(suggestion2);}
               }
            } catch (NumberFormatException ex) {}
         }
      }
      mSuggestions.clear();
      mSuggestions.addAll(suggestions);
   }
   
   public List<Suggestion> getSuggestions()
   {
      return mSuggestions;
   }
   
   public void update()
   {
      MCDC();
      inform();
   }
}
