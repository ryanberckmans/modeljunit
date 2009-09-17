package nz.ac.waikato.modeljunit.storytest;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.TreeSet;

public class PairWiseSuggestionStrategy
   extends AbstractSuggestionStrategy
   implements Observer, Subject, SuggestionStrategy
{
   private final List<Suggestion> mSuggestions;
   
   public PairWiseSuggestionStrategy(CalcTable calc)
   {
      super(calc);
      mSuggestions = new ArrayList<Suggestion>();
      MCDC();
   }
   
   private void MCDC()
   {
      mSuggestions.clear();
      CalcTable calc = getCalcTable();
      List<Set<String>> possible = new ArrayList<Set<String>>(calc.columns());
      for (int c = 0; c < calc.columns(); c++) {
        if (calc.isResult(c)) {possible.add(null); continue;}
        possible.add(new TreeSet<String>());
        for (int r = 0; r < calc.rows(); r++) {
           possible.get(c).add(calc.getValue(r, c));
        }
      }
      pairwise(new int[2], 0, possible);
   }
   
   private void pairwise(int[] perm, int depth, List<Set<String>> possible)
   {
      if (depth >= perm.length) {
        Set<List<String>> covered = new HashSet<List<String>>();
        for (int r = 0; r < getCalcTable().rows(); r++) {
          List<String> conf = new ArrayList<String>(perm.length);
          for (int ip = 0; ip < perm.length; ip++) {
            int p = perm[ip];
            conf.add(getCalcTable().getValue(r, p));
          }
          covered.add(conf);
        }
        List<String> workingset = new ArrayList<String>(perm.length);
        for (int i = 0; i < perm.length; i++) {
          workingset.add("");
        }
        perms(perm, 0, possible, covered, workingset);
      } else {
        int i = depth > 0 ? perm[depth - 1] + 1: 0;
        for (; i < getCalcTable().columns(); i++) {
          if (!getCalcTable().isResult(i)) {
            perm[depth] = i;
            pairwise(perm, depth + 1, possible);
          }
        }
      }
   }
   
   private void perms(int[] perm, int depth, List<Set<String>> possible,
                      Set<List<String>> covered, List<String> working)
   {
     if (depth >= perm.length) {
       if (!covered.contains(working)) {
         List<String> sug = new ArrayList<String>();
         for (int i = 0; i < getCalcTable().columns(); i++) {
           sug.add("--");
         }
         for (int i = 0; i < working.size(); i++) {
           sug.set(perm[i], working.get(i));
         }
         mSuggestions.add(new DefaultSuggestion(sug));
       }
     } else {
       int permdepth = perm[depth];
       for (String s : possible.get(permdepth)) {
         working.set(depth, s);
         perms(perm, depth + 1, possible, covered, working);
       }
     }
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
