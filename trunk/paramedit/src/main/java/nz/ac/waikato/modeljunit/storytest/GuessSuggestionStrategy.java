package nz.ac.waikato.modeljunit.storytest;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.lang.NumberFormatException;

public class GuessSuggestionStrategy
   extends AbstractSuggestionStrategy
   implements Observer, Subject, SuggestionStrategy
{   
   private final List<Suggestion> mSuggestions;
   
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
         if (!calc.isResult(c) && !columnsdone[c]) {
            if (calc.getType(c).equals(Boolean.class)) {
               columnsdone[c] = true; columnorder[count] = c;
               isBoolean[c] = true;
               count++;
            }
         }
      }
      for (int c = 0; c < calc.columns(); c++) {
         if (!calc.isResult(c) && !columnsdone[c]) {
            if (calc.getType(c).equals(Double.class)) {
               columnsdone[c] = true; columnorder[count] = c;
               count++;
            }
         }
      }
      List<List<String>> rows = new ArrayList<List<String>>(calc.rows());
      for (int i = 0; i < calc.rows(); i++) {
         if (!calc.getRow(i).contains("")) {
            rows.add(calc.getRow(i));
         }
      }
      List<List<String>> guesses = new ArrayList<List<String>>(calc.rows());
      List<String> partialguess = new ArrayList<String>();
      for (int c = 0; c < calc.columns(); c++) {partialguess.add("-");}
      sift(columnorder, result, rows, isBoolean, guesses, 0, partialguess);
      System.out.println("guesses:" + guesses.size());
      for (List<String> guess : guesses) {
         mSuggestions.add(new DefaultSuggestion(guess));
      }
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
   
   private void siftdouble(int[] columnorder, int resultcolumn, List<List <String>> rows, boolean[] isBoolean,
                           List<List<String>> guesses, int depth, List<String> currentguess)
   {
      SortedMap<Double, Triple> rowmap = new TreeMap<Double, Triple>();
      int index = columnorder[depth];
      for (List<String> row : rows) {
         String result = row.get(resultcolumn);
         System.out.println(row);
         try {
            Double value = new Double(row.get(index));
            Triple trip = rowmap.get(value);
            if (trip == null) {
               List<List<String>> list = new ArrayList<List<String>>();
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
      int last = -1;
      for (i = 0; i <= values.length; i++) {
         Triple trip = i >= triples.length ? null : triples[i];
         if (last != -1 && (trip == null || !trip.allsame || !trip.mResult.equals(triples[last].mResult))) {
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
         if (trip.allsame) {last = i; continue;}
         List<String> guess = new ArrayList<String>(currentguess);
         String previous = getStringValue(i -1, values);
         String to = getStringValue(i, values);
         guess.set(index, previous + "..." + to);
         guess.set(resultcolumn, trip.mResult);
         sift(columnorder, resultcolumn, trip.mList, isBoolean, guesses, depth + 1, guess);
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
      final List<List<String>> mList;
      final String mResult;
      boolean allsame = true;
      
      Triple(List<List<String>> list, String result)
      {
         mList = list;
         mResult = result;
      }
   }
   
   private void siftboolean(int[] columnorder, int resultcolumn, List<List <String>> rows, boolean[] isBoolean,
                            List<List<String>> guesses, int depth, List<String> currentguess)
   {
      List<List<String>> trues = new ArrayList<List<String>>();
      List<List<String>> falses = new ArrayList<List<String>>();
      int index = columnorder[depth];
      String falseres = null;
      String trueres = null;
      boolean allfalse = true;
      boolean alltrue = true;
      for (List<String> row : rows) {
         String result = row.get(resultcolumn);
         if (row.get(index).equals("T")) {
            trueres = trueres == null ? result : trueres;
            alltrue = alltrue && trueres.equals(result);
            trues.add(row);
         } else {
            falseres = trueres == null ? result : trueres;
            allfalse = alltrue && falseres.equals(result);
            falses.add(row);
         }
      }
      List<String> trueguess = new ArrayList<String>(currentguess);
      List<String> falseguess = new ArrayList<String>(currentguess);
      trueguess.set(index, "T");
      falseguess.set(index, "F");
      if (alltrue) {
         trueguess.set(resultcolumn, trueres);
         guesses.add(trueguess);
         return;
      } else {
         siftdouble(columnorder, resultcolumn, trues, isBoolean, guesses, depth + 1, trueguess);
      }
      if (allfalse) {
         falseguess.set(resultcolumn, falseres);
         guesses.add(falseguess);
         return;
      } else {
         siftdouble(columnorder, resultcolumn, falses, isBoolean, guesses, depth + 1, falseguess);
      }
   }
   
   private void sift(int[] columnorder, int resultcolumn, List<List <String>> rows, boolean[] isBoolean,
                     List<List<String>> guesses, int depth, List<String> currentguess)
   {
      if (depth >= columnorder.length) {
         List<String> guess = new ArrayList<String>(currentguess);
         guess.set(resultcolumn, "contradicts");
         guesses.add(guess);
         return;
      }
      if(isBoolean[columnorder[depth]]) {
         siftboolean(columnorder, resultcolumn, rows, isBoolean, guesses, depth, currentguess);
      } else {
         siftdouble(columnorder, resultcolumn, rows, isBoolean, guesses, depth, currentguess);
      }
   }
}
