package nz.ac.waikato.modeljunit.storytest;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.lang.NumberFormatException;
import java.util.Arrays;

public class GuessSuggestionStrategy
   extends AbstractSuggestionStrategy
   implements Observer, Subject, SuggestionStrategy
{   
   private final List<Suggestion> mSuggestions;
   private static final String CONTRADICTION = "contradiction";
   
   public GuessSuggestionStrategy(CalcTable calc)
   {
      super(calc);
      mSuggestions = new ArrayList<Suggestion>();
      guess();
   }

   private void guess()
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
      final List<String> mGuess;
      final List<Integer> mRows;
      
      Tuple(List<String> guess, List<Integer> rows)
      {
         mGuess = guess;
         mRows = rows;
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
   }
}
