package nz.ac.waikato.modeljunit.storytest;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.lang.NumberFormatException;
import java.util.Arrays;

/**
 * The Guess Suggestion strategy.
 * 
 * First the Parameters of the table are ordered, such that booleans are dealt with first then integers.
 * The algorithm takes as arguments a set of rows R and a parameter p to split upon,
 * and a current guess g of what covers these rows. The algorithm splits R into groups
 * of rows R' such that each group in R' has the same value for the parameter p.
 * From here every set of rows s in R' is enumerated.  If every row in s agrees on
 * the result then the current guess for the set s is added to the set of suggestions G.
 * Otherwise this set of rows s is fed back into the algorithm with its guess and
 * to be split on the next parameter.  For each set s the guess g is updated so as to
 * guess that for every value of p less than s's value of p but greater than the
 * previous's value of p the result will be the same.
 * 
 * If this algorithm gets to the end and finds two rows with identical parameters
 * but different results then it adds a contradiction suggestion.
 *
 * The algorithm starts with the entire table being fed in, with splitting on
 * the first parameter and a guess that every parameter doesn't care about anything else.
 *
 * Next is the pruning stage which takes the set of suggestions generated
 * and in instances where two suggestions are identical except for one parameter,
 * then these two parameters are attempted to be merged, if they happen to be
 * contiguous with one another. For example one says p goes from 5...7 and the
 * other says p goes from 7...15.
 *
 * @author Simon Ware
 */
public class GuessSuggestionStrategy
   extends AbstractSuggestionStrategy
   implements Observer, Subject, SuggestionStrategy
{   
   private final List<Suggestion> mSuggestions;
   private List<SortedSet<Double>> mBottoms = null;
   private List<SortedSet<Double>> mTops = null;
   private static final Result CONTRADICTION = new Result("contradiction");
   
   public GuessSuggestionStrategy(CalcTable calc)
   {
      super(calc);
      mSuggestions = new ArrayList<Suggestion>();
      guess();
   }

   /*private void guess()
   {
      System.out.println("guess");
      mSuggestions.clear();
      int result = -1;
      CalcTable calc = getCalcTable();
      if (calc.rows() == 0) {return;}
      System.out.println(calc.toString());
      for (int c = 0; c < calc.columns(); c++) {
         if (calc.isResult(c)) {
            result = c; break;
         }
      }
      if (result == -1) {
         return;
      }
      boolean[] columnsdone = new boolean[calc.columns()];
      // TODO make this work for multiple results columns
      int[] columnorder = new int[calc.columns() - 1];
      boolean[] isBoolean = new boolean[calc.columns()];
      int count = 0;
      for (int c = 0; c < calc.columns(); c++) {
         if (!calc.isResult(c) && !columnsdone[c] && calc.getType(c) != null) {
            if (calc.getType(c).equals(Boolean.class)) {
               columnsdone[c] = true; columnorder[count] = c;
               isBoolean[c] = true;
               count++;
            }
         }
      }
      for (int c = 0; c < calc.columns(); c++) {
         if (!calc.isResult(c) && !columnsdone[c] && calc.getType(c) != null) {
            if (calc.getType(c).equals(Double.class)) {
               columnsdone[c] = true; columnorder[count] = c;
               count++;
            }
         }
      }
      List<Integer> rows = new ArrayList<Integer>(calc.rows());
      for (int i = 0; i < calc.rows(); i++) {
         if (!calc.getRow(i).contains("")) {
            rows.add(i);
         }
      }
      List<Tuple> guesses = new ArrayList<Tuple>(calc.rows());
      List<String> partialguess = new ArrayList<String>();
      for (int c = 0; c < calc.columns(); c++) {partialguess.add("...");}
      sift(columnorder, result, rows, isBoolean, guesses, 0, partialguess);
      System.out.println("guesses:" + guesses.size());
      guesses = merge(guesses);
      for (Tuple tup : guesses) {
         if (tup != null) {
            List<String> guess = tup.mGuess;
            int[] irows = new int[tup.mRows.size()];
            for (int i = 0; i < tup.mRows.size(); i++) {
               irows[i] = tup.mRows.get(i);
            }
            if (!guess.get(result).equals(CONTRADICTION)) {
               mSuggestions.add(new RowsSuggestion(guess, irows, getCalcTable()));
            } else {
               mSuggestions.add(new RowsContradiction(guess, irows, getCalcTable()));
            }
         }
      }
   }
   
   private boolean rowIsEquation(List<String> row)
   {
     
     return false;
   }
   
   private List<Tuple> merge(List<Tuple> guesses)
   {
      List<Integer> stack = new ArrayList<Integer>(guesses.size());
      for (int i = 0; i < guesses.size(); i++) {stack.add(i);}
      while(!stack.isEmpty()) {
         int guess = stack.remove(stack.size() - 1);
         Tuple tup1 = guesses.get(guess);
         if (tup1 == null) {continue;}
         List<String> guess1 = tup1.mGuess;
         guess:
         for (int g = 0; g < guesses.size(); g++) {
            if (g == guess) {continue;}
            Tuple tup2 = guesses.get(g);
            if (tup2 == null) {continue;}
            List<String> guess2 = tup2.mGuess;
            int diff = -1;
            System.out.println(guess1);
            System.out.println(guess2);
            for (int c = 0; c < guess1.size(); c++) {
               if (!guess1.get(c).equals(guess2.get(c))) {
                  if (diff == -1) {
                     diff = c;
                  } else {
                     continue guess;
                  }
               }
            }
            System.out.println(diff);
            if (diff == -1) {
               guesses.set(g, null);
               tup1.mRows.addAll(tup2.mRows);
            } else {
               String newparam = null;
               String[] range1 = guess1.get(diff).split("\\.\\.\\.");
               String[] range2 = guess2.get(diff).split("\\.\\.\\.");
               System.out.println(Arrays.toString(range1));
               System.out.println(Arrays.toString(range2));
               System.out.println();
               if (range1.length == 0) {
                  newparam = guess2.get(diff);
               } else if (range2.length == 0) {
                  newparam = guess1.get(diff);
               } else {
                  if (range1.length == 1) {
                     if (guess1.get(diff).startsWith("\\.\\.\\.")) {
                        range1 = new String[] {"", range1[0]};
                     } else {
                        range1 = new String[] {range1[0], ""};
                     }
                  }
                  if (range2.length == 1) {
                     if (guess1.get(diff).startsWith("\\.\\.\\.")) {
                        range2 = new String[] {"", range2[0]};
                     } else {
                        range2 = new String[] {range2[0], ""};
                     }
                  }
                  if (range1[0].equals(range2[1])) {
                     newparam = range2[0] + "..." + range1[1];
                  } else if (range1[1].equals(range2[0])) {
                     newparam = range1[0] + "..." + range2[1];
                  } else {
                     continue;
                  }
               }
               stack.add(guess);
               guess1.set(diff, newparam);
               tup1.mRows.addAll(tup2.mRows);
               guesses.set(g, null);
            }
         }
      }
      return guesses;
   }*/
   
   private void guess()
   {
      // System.out.println("guess");
      mSuggestions.clear();
      int result = -1;
      CalcTable calc = getCalcTable();
      if (calc.rows() == 0) {return;}
      // System.out.println(calc.toString());
      for (int c = 0; c < calc.columns(); c++) {
         if (calc.isResult(c)) {
            result = c; break;
         }
      }
      if (result == -1) {
         return;
      }
      boolean[] columnsdone = new boolean[calc.columns()];
      // TODO make this work for multiple results columns
      int[] columnorder = new int[calc.columns() - 1];
      boolean[] isBoolean = new boolean[calc.columns()];
      int count = 0;
      for (int c = 0; c < calc.columns(); c++) {
         if (!calc.isResult(c) && !columnsdone[c] && calc.getType(c) != null) {
            if (calc.getType(c).equals(Boolean.class)) {
               columnsdone[c] = true; columnorder[count] = c;
               isBoolean[c] = true;
               count++;
            }
         }
      }
      for (int c = 0; c < calc.columns(); c++) {
         if (!calc.isResult(c) && !columnsdone[c] && calc.getType(c) != null) {
            if (calc.getType(c).equals(Double.class)) {
               columnsdone[c] = true; columnorder[count] = c;
               count++;
               
            }
         }
      }
      mBottoms = new ArrayList<SortedSet<Double>>();
      mTops = new ArrayList<SortedSet<Double>>();
      for (int c = 0; c < calc.columns(); c++) {
        mTops.add(new TreeSet<Double>());
        mBottoms.add(new TreeSet<Double>());
        double[][] arr = calc.getTypeRange(c).getRanges();
        for (int i = 0; i < arr.length; i++) {
          mBottoms.get(c).add(arr[i][0]);
          mTops.get(c).add(arr[i][1]);
        }
      }
      List<Integer> rows = new ArrayList<Integer>(calc.rows());
      List<Guess> preguess = new ArrayList<Guess>();
      for (int i = 0; i < calc.rows(); i++) {
         if (calc.getRow(i).contains("")) {continue;}
         Guess guess = Guess.getGuess(calc.getRow(i), result, isBoolean);
         if (guess == null) {
           rows.add(i);
         } else {
           preguess.add(guess);
         }
      }
      System.out.println("preguess: " +preguess);
      for (Guess guess: preguess) {
        Iterator<Integer> it = rows.iterator();
        while (it.hasNext()) {
          int r = it.next();
          if (guess.conforms(calc.getRow(r), result)) {
            it.remove();
          }
        }
      }
      List<Tuple> guesses = new ArrayList<Tuple>(calc.rows());
      Guess partialguess = new Guess(calc.columns());
      sift(columnorder, result, rows, isBoolean, guesses, 0, partialguess);
      System.out.println("guesses:" + guesses.size());
      guesses = merge(guesses);
      for (Tuple tup : guesses) {
         if (tup != null) {
            Guess guess = tup.mGuess;
            int[] irows = new int[tup.mRows.size()];
            for (int i = 0; i < tup.mRows.size(); i++) {
               irows[i] = tup.mRows.get(i);
            }
            if (!guess.get(result).equals(CONTRADICTION)) {
               mSuggestions.add(new RowsSuggestion(guess.toStrings(), irows, getCalcTable()));
            } else {
               mSuggestions.add(new RowsContradiction(guess.toStrings(), irows, getCalcTable()));
            }
         }
      }
   }
   
   private boolean rowIsEquation(List<String> row)
   {
     
     return false;
   }
   
   private List<Tuple> merge(List<Tuple> guesses)
   {
      List<Integer> stack = new ArrayList<Integer>(guesses.size());
      for (int i = 0; i < guesses.size(); i++) {stack.add(i);}
      while(!stack.isEmpty()) {
         int guess = stack.remove(stack.size() - 1);
         Tuple tup1 = guesses.get(guess);
         if (tup1 == null) {continue;}
         Guess guess1 = tup1.mGuess;
         guess:
         for (int g = 0; g < guesses.size(); g++) {
            if (g == guess) {continue;}
            Tuple tup2 = guesses.get(g);
            if (tup2 == null) {continue;}
            Guess guess2 = tup2.mGuess;
            int diff = -1;
            System.out.println(guess1);
            System.out.println(guess2);
            for (int c = 0; c < guess1.size(); c++) {
               if (!guess1.get(c).equals(guess2.get(c))) {
                  if (diff == -1) {
                     diff = c;
                  } else {
                     continue guess;
                  }
               }
            }
            System.out.println(diff);
            if (diff == -1) {
               guesses.set(g, null);
               tup1.mRows.addAll(tup2.mRows);
            } else {
               Bound newparam = null;
               /*String[] range1 = guess1.get(diff).split("\\.\\.\\.");
               String[] range2 = guess2.get(diff).split("\\.\\.\\.");
               System.out.println(Arrays.toString(range1));
               System.out.println(Arrays.toString(range2));*/
               System.out.println();
               newparam = guess1.get(diff).merge(guess2.get(diff));
               if (newparam != null) {
                 stack.add(guess);
                 guess1.set(diff, newparam);
                 tup1.mRows.addAll(tup2.mRows);
                 guesses.set(g, null);
               }
            }
         }
      }
      return guesses;
   }
   

   public List<Suggestion> getSuggestions()
   {
      return mSuggestions;
   }
   
   public void update()
   {
      guess();
      inform();
   }
   
   
   
   private void siftdouble(int[] columnorder, int resultcolumn, List<Integer> rows, boolean[] isBoolean,
                           List<Tuple> guesses, int depth, Guess currentguess)
   {
      SortedMap<Double, Triple> rowmap = new TreeMap<Double, Triple>();
      int index = columnorder[depth];
      for (Integer row : rows) {
         String result = getCalcTable().getValue(row, resultcolumn);
         System.out.println(row);
         try {
            Double value = new Double(getCalcTable().getValue(row, index));
            Triple trip = rowmap.get(value);
            if (trip == null) {
               List<Integer> list = new ArrayList<Integer>();
               trip = new Triple(list, result);
               rowmap.put(value, trip);
            }
            trip.mList.add(row);
            trip.allsame = trip.allsame && trip.mResult.equals(result);
            System.out.println("mResult:" + trip.mResult + "\tresult" + result);
            System.out.println("allsame:" + trip.allsame);
         } catch (NumberFormatException exception) {
            exception.printStackTrace();
         }
      }
      Double[] values = new Double[rowmap.size()];
      Triple[] triples = new Triple[rowmap.size()];
      int i = 0;
      for (Double value : rowmap.keySet()) {
         Triple trip = rowmap.get(value);
         values[i] = value;
         triples[i] = trip;
         i++;
      }
      for (i = 0; i < values.length; i++) {
        System.out.println("****");
        System.out.println("****");
        System.out.println("Sign");
        System.out.println("****");
        System.out.println("****");
         Triple trip = i >= triples.length ? null : triples[i];
         /*if (last != -1 && (trip == null || !trip.allsame || !trip.mResult.equals(triples[last].mResult))) {
            List<String> guess = new ArrayList<String>(currentguess);
            String previous = getStringValue(last - 1, values);
            String to = getStringValue(i - 1, values);
            guess.set(index, previous + "..." + to);
            guess.set(resultcolumn, triples[last].mResult);
            guesses.add(guess);
            last = -1;
         }
         if (trip == null) {break;}
         if (last != -1 && trip.allsame) {continue;}
         if (trip.allsame) {last = i; continue;}*/
         Guess guess = new Guess(currentguess);
         Double previous = getDoubleValue(i -1, values);
         Double to = getDoubleValue(i, values);
         //Double next = getDoubleValue(i + 1, values);
         System.out.println("bots:" + mBottoms.get(index) + " head:" + mBottoms.get(index).headSet(to) + " to:" + to);
         if (!mBottoms.get(index).headSet(to).isEmpty()) {
           double prev2 = mBottoms.get(index).headSet(to).last();
           System.out.println("prev:" + previous + " prev2:" + prev2);
           if (previous < prev2) {
             previous = prev2;
           }
         }
         System.out.println("tops:" + mTops.get(index) + " head:" + mTops.get(index).tailSet(previous) + " prev:" + to);
         if (!mTops.get(index).tailSet(previous).isEmpty()) {
           double next2 = mTops.get(index).tailSet(previous).first();
           System.out.println("next:" + to + " next2:" + next2);
           if (next2 < to) {
             to = next2;
           }
         }
         guess.set(index, new BoundDouble(previous, to));
         if (!trip.allsame) {
            sift(columnorder, resultcolumn, trip.mList, isBoolean, guesses, depth + 1, guess);
         } else {
            guess.set(resultcolumn, new Result(trip.mResult));
            guesses.add(new Tuple(guess, trip.mList));
         }
      }
         /*List<String> guess = new ArrayList<String>(currentguess);
         prevTrip = prevTrip == null ? trip : prevTrip;
         guess.set(index, previous + "..." + value);
         if (trip.allsame) {
            guess.set(resultcolumn, trip.mResult);
            guesses.add(guess);
         } else {
            sift(columnorder, resultcolumn, trip.mList, isBoolean, guesses, depth + 1, guess);
         }
         prevprev = previous;
         previous = (value + 1) + "";
      guesses.get(guesses.size() - 1).set(index, prevprev + "...");*/
   }
   
   private void siftboolean(int[] columnorder, int resultcolumn, List<Integer> rows, boolean[] isBoolean,
                            List<Tuple> guesses, int depth, Guess currentguess)
    {
      List<Integer> trues = new ArrayList<Integer>();
      List<Integer> falses = new ArrayList<Integer>();
      int index = columnorder[depth];
      String falseres = null;
      String trueres = null;
      boolean allfalse = true;
      boolean alltrue = true;
      for (int row : rows) {
        String result = getCalcTable().getValue(row, resultcolumn);
        if (getCalcTable().getValue(row, index).equals("T")) {
          trueres = trueres == null ? result : trueres;
          alltrue = alltrue && trueres.equals(result);
          trues.add(row);
        } else {
          falseres = falseres== null ? result : falseres;
          allfalse = allfalse && falseres.equals(result);
          falses.add(row);
        }
      }
      Guess trueguess = new Guess(currentguess);
      Guess falseguess = new Guess(currentguess);
      if (!falses.isEmpty()) {trueguess.set(index, new BoundBoolean(true));}
      if (!trues.isEmpty()) {falseguess.set(index, new BoundBoolean(false));}
      if (!trues.isEmpty()) {
        if (alltrue) {
          trueguess.set(resultcolumn, new Result(trueres));
          guesses.add(new Tuple(trueguess, trues));
          return;
        } else {
          sift(columnorder, resultcolumn, trues, isBoolean, guesses, depth + 1, trueguess);
        }
      }
      if (!falses.isEmpty()) {
        if (allfalse) {
          falseguess.set(resultcolumn, new Result(falseres));
          guesses.add(new Tuple(falseguess, falses));
        return;
        } else {
          sift(columnorder, resultcolumn, falses, isBoolean, guesses, depth + 1, falseguess);
        }
      }
    }
    
    private void sift(int[] columnorder, int resultcolumn, List<Integer> rows, boolean[] isBoolean,
                      List<Tuple> guesses, int depth, Guess currentguess)
    {
      if (depth >= columnorder.length) {
        Guess guess = new Guess(currentguess);
        guess.set(resultcolumn, CONTRADICTION);
        guesses.add(new Tuple(guess, rows));
        return;
      }
        if(isBoolean[columnorder[depth]]) {
        siftboolean(columnorder, resultcolumn, rows, isBoolean, guesses, depth, currentguess);
      } else {
        siftdouble(columnorder, resultcolumn, rows, isBoolean, guesses, depth, currentguess);
      }
    }
/*   
    private void siftdouble(int[] columnorder, int resultcolumn, List<Integer> rows, boolean[] isBoolean,
        List<Tuple> guesses, int depth, List<String> currentguess)
{
SortedMap<Double, Triple> rowmap = new TreeMap<Double, Triple>();
int index = columnorder[depth];
for (Integer row : rows) {
String result = getCalcTable().getValue(row, resultcolumn);
System.out.println(row);
try {
Double value = new Double(getCalcTable().getValue(row, index));
Triple trip = rowmap.get(value);
if (trip == null) {
List<Integer> list = new ArrayList<Integer>();
trip = new Triple(list, result);
rowmap.put(value, trip);
}
trip.mList.add(row);
trip.allsame = trip.allsame && trip.mResult.equals(result);
System.out.println("mResult:" + trip.mResult + "\tresult" + result);
System.out.println("allsame:" + trip.allsame);
} catch (NumberFormatException exception) {
exception.printStackTrace();
}
}
Double[] values = new Double[rowmap.size()];
Triple[] triples = new Triple[rowmap.size()];
int i = 0;
for (Double value : rowmap.keySet()) {
Triple trip = rowmap.get(value);
values[i] = value;
triples[i] = trip;
i++;
}
for (i = 0; i < values.length; i++) {
Triple trip = i >= triples.length ? null : triples[i];
List<String> guess = new ArrayList<String>(currentguess);
String previous = getStringValue(i -1, values);
String to = getStringValue(i, values);
guess.set(index, previous + "..." + to);
if (!trip.allsame) {
sift(columnorder, resultcolumn, trip.mList, isBoolean, guesses, depth + 1, guess);
} else {
guess.set(resultcolumn, trip.mResult);
guesses.add(new Tuple(guess, trip.mList));
}
}
}

private void siftboolean(int[] columnorder, int resultcolumn, List<Integer> rows, boolean[] isBoolean,
List<Tuple> guesses, int depth, List<String> currentguess)
{
List<Integer> trues = new ArrayList<Integer>();
List<Integer> falses = new ArrayList<Integer>();
int index = columnorder[depth];
String falseres = null;
String trueres = null;
boolean allfalse = true;
boolean alltrue = true;
for (int row : rows) {
String result = getCalcTable().getValue(row, resultcolumn);
if (getCalcTable().getValue(row, index).equals("T")) {
trueres = trueres == null ? result : trueres;
alltrue = alltrue && trueres.equals(result);
trues.add(row);
} else {
falseres = falseres== null ? result : falseres;
allfalse = allfalse && falseres.equals(result);
falses.add(row);
}
}
List<String> trueguess = new ArrayList<String>(currentguess);
List<String> falseguess = new ArrayList<String>(currentguess);
if (!falses.isEmpty()) {trueguess.set(index, "T");}
if (!trues.isEmpty()) {falseguess.set(index, "F");}
if (!trues.isEmpty()) {
if (alltrue) {
trueguess.set(resultcolumn, trueres);
guesses.add(new Tuple(trueguess, trues));
return;
} else {
sift(columnorder, resultcolumn, trues, isBoolean, guesses, depth + 1, trueguess);
}
}
if (!falses.isEmpty()) {
if (allfalse) {
falseguess.set(resultcolumn, falseres);
guesses.add(new Tuple(falseguess, falses));
return;
} else {
sift(columnorder, resultcolumn, falses, isBoolean, guesses, depth + 1, falseguess);
}
}
}

private void sift(int[] columnorder, int resultcolumn, List<Integer> rows, boolean[] isBoolean,
   List<Tuple> guesses, int depth, List<String> currentguess)
{
if (depth >= columnorder.length) {
List<String> guess = new ArrayList<String>(currentguess);
guess.set(resultcolumn, CONTRADICTION);
guesses.add(new Tuple(guess, rows));
return;
}
if(isBoolean[columnorder[depth]]) {
siftboolean(columnorder, resultcolumn, rows, isBoolean, guesses, depth, currentguess);
} else {
siftdouble(columnorder, resultcolumn, rows, isBoolean, guesses, depth, currentguess);
}
}*/
    
    private Double getDoubleValue(int index, Double[] values)
    {
       if (index < 0) {
          return Double.NEGATIVE_INFINITY;
       } else if (index + 1 >= values.length) {
          return Double.POSITIVE_INFINITY;
       }
       return values[index];
    }

   private String getStringValue(int index, Double[] values)
   {
      if (index < 0 || index + 1 >= values.length) {
         return "";
      }
      return values[index].toString();
   }
   
   private class Triple
   {
      final List<Integer> mList;
      final String mResult;
      boolean allsame = true;
      
      Triple(List<Integer> list, String result)
      {
         mList = list;
         mResult = result;
      }
   }
   
   private class Tuple
   {
      final Guess mGuess;
      final List<Integer> mRows;
      
      Tuple(Guess guess, List<Integer> rows)
      {
         mGuess = guess;
         mRows = rows;
      }
   }
   
   private static class Guess
     extends AbstractList<Bound>
   {
     private final Bound[] mBounds;
     
     Guess(int size)
     {
       mBounds = new Bound[size];
       Arrays.fill(mBounds, DontCare.INSTANCE);
     }
     
     Guess(Guess guess)
     {
       mBounds = Arrays.copyOf(guess.mBounds, guess.size());
     }
     
     public int size()
     {
       return mBounds.length;
     }
     
     public Bound set(int index, Bound element)
     {
       Bound t = mBounds[index];
       mBounds[index] = element;
       return t;
     }
     
     public Bound get(int index)
     {
       return mBounds[index];
     }
     
     public List<String> toStrings()
     {
       List<String> strings = new ArrayList<String>(size());
       for (int i = 0; i < mBounds.length; i++) {
         strings.add(mBounds[i].toString());
       }
       return strings;
     }
     
     public boolean conforms(List<String> row, int result)
     {
       System.out.println(row);
       System.out.println(this);
       for (int i = 0; i < row.size(); i++) {
         if (result == i) {continue;}
         if (!mBounds[i].inBounds(row.get(i))) {
           return false;
         }
         System.out.println("inbounds");
       }
       System.out.println("conforms");
       return true;
     }
     
     public static Guess getGuess(List<String> row, int resultcol, boolean isBoolean[])
     {
       boolean contained = false;
       Guess res = new Guess(row.size());
       for (int i = 0; i < row.size(); i++) {
         if (i == resultcol) {res.set(i, new Result(row.get(i))); continue;}
         Bound b = isBoolean[i] ? BoundBoolean.parse(row.get(i)) : BoundDouble.parse(row.get(i));
         if (b == null) {return null;}
         if (b == DontCare.INSTANCE) {contained = true;}
         res.set(i, b);
       }
       return contained ? res : null;
     }
   }
   
   private interface Bound
   {
     public boolean inBounds(Object o);
     
     public Bound merge(Bound b);
   }
   
   private static class Result
     implements Bound
   {
     private final String mResult;
     
     Result(String result)
     {
       mResult = result;
     }
     
     public boolean inBounds(Object o)
     {
       return o.equals(mResult);
     }
     
     public Bound merge(Bound b)
     {
       return null;
     }
     
     public int hashCode()
     {
       return mResult.toString().hashCode();
     }
     
     public boolean equals(Object o)
     {
       if (! (o instanceof Result)) {return false;}
       Result res = (Result) o;
       return mResult.equals(res.mResult);
     }
     
     public String toString()
     {
       return mResult;
     }
   }
   
   private static class DontCare
     implements Bound
   {
     static final DontCare INSTANCE = new DontCare();
     
     private DontCare()
     {
     }
     
     public boolean inBounds(Object o)
     {
       return true;
     }
     
     public Bound merge(Bound b)
     {
       return INSTANCE;
     }
     
     public String toString()
     {
       return "...";
     }
   }
   
   private static class BoundBoolean
     implements Bound
   {
     private final boolean mValue;
     
     private BoundBoolean(boolean value)
     {
       mValue = value;
     }
     
     public static Bound parse(String string)
     {
       if (string.equals("...")) {return DontCare.INSTANCE;}
       if (string.equals("T")) {
         return new BoundBoolean(true);
       } else if (string.equals("F")) {
         return new BoundBoolean(false);
       }
       return null;
     }
     
     public boolean inBounds(Object value)
     {
       if (value instanceof String) {
         String st = (String) value;
         value = st.equals("T") ? true : value;
         value = st.equals("F") ? false : value;
       }
       if (value instanceof Boolean) {
         boolean temp = (Boolean) value;
         return mValue == temp;
       }
       return false;
     }
     
     public Bound merge(Bound b)
     {
       if (b instanceof DontCare) {return b;}
       if (!(b instanceof BoundBoolean)) {return null;}
       BoundBoolean bb = (BoundBoolean) b;
       if (bb.mValue != mValue) {
         return DontCare.INSTANCE;
       }
       return null;
     }
     
     public int hashCode()
     {
       //TODO do better hashCode
       return toString().hashCode();
     }
     
     public boolean equals(Object o)
     {
       if (!(o instanceof BoundBoolean)) {return false;}
       BoundBoolean bb = (BoundBoolean)o;
       return bb.toString().equals(toString());
     }
     
     public String toString()
     {
       return mValue ? "T" : "F";
     }
   }
   
   private static class BoundDouble
     implements Bound
   {
     private final double mLower;
     private final double mUpper;
     
     private BoundDouble(double l, double u)
     {
       mLower = l;
       mUpper = u;
     }
     
     public static Bound parse(String string)
     {
       if (string.equals("...")) {return DontCare.INSTANCE;}
       try {
         double l = Double.parseDouble(string);
         double u = l;
         return new BoundDouble(l, u);
       } catch (NumberFormatException nfe) {
       }
       return null;
     }
     
     public boolean inBounds(Object value)
     {
       System.out.println("value: " +value);
       if (value instanceof String) {
         String st = (String) value;
         try {
           value = new Double(st);
         } catch (NumberFormatException nfe) {
           return false;
         }
       }
       System.out.println("value2: " +value);
       if (value instanceof Double) {
         double temp = (Double) value;
         if (mLower != mUpper) {
           return mLower < temp && temp <= mUpper;
         } else {
           return mLower == temp;
         }
       }
       return false;
     }
     
     public Bound merge(Bound b)
     {
       // System.out.println("merge");
       if (b instanceof DontCare) {return b;}
       // System.out.println("merge2");
       if (!(b instanceof BoundDouble)) {return null;}
       BoundDouble bd = (BoundDouble) b;
       // System.out.println(this + " vs " + bd);
       if (bd.mLower == mUpper) {
         return new BoundDouble(mLower, bd.mUpper);
       } else if (mLower == bd.mUpper) {
         return new BoundDouble(bd.mLower, mUpper);
       }
       // System.out.println(this + " vs " + bd);
       return null;
     }
     
     public int hashCode()
     {
       //TODO do better hashCode
       return (int)(mLower * mUpper);
     }
     
     public boolean equals(Object o)
     {
       if (!(o instanceof BoundDouble)) {return false;}
       BoundDouble bd = (BoundDouble)o;
       return mLower == bd.mLower && mUpper == bd.mUpper;
     }
     
     public String toString()
     {
       if (mLower == mUpper) {return "" + mUpper;}
       String res = "";
       res = mLower == Double.NEGATIVE_INFINITY? res : res + mLower;
       res += "...";
       res = mUpper == Double.POSITIVE_INFINITY? res : res + mUpper;
       return res;
     }
   }
}
