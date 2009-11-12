/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author root
 *
 */
public class TypeRange
{
  private static final double[][] DEFAULT = new double[][] {{Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}};
  
  private final Class<?> mType;
  
  private final int mColumn;
  
  private final boolean mTypeIsGuess;
  
  private final double[][] mRanges;
  
  private final CalcTable mCalc;
  
  public TypeRange(String parse, int column, CalcTable calc) throws ParseException
  {
    mCalc = calc;
    mColumn = column;
    Tuple tup = Parser.parse(parse);
    if (tup.mType == null) {
      mType = null;
      mTypeIsGuess = true;
    } else {
      mType = tup.mType;
      mTypeIsGuess = false;
    }
    if (tup.mRange.isEmpty()) {
      mRanges = DEFAULT;
    } else {
      double[][] ranges = new double[tup.mRange.size()][2];
      mRanges = tup.mRange.toArray(ranges);
    }
    System.out.println(Arrays.toString(mRanges));
  }
  
  public boolean isGuess()
  {
    return mTypeIsGuess;
  }
  
  public Class<?> getType()
  {
    return isGuess() ? mCalc.guessType(mColumn) : mType; 
  }
  
  public double[][] getRanges()
  {
    return mRanges;
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    if (getType() != null) {
      sb.append(getType().getSimpleName());
      if (isGuess()) {
        sb.append("?");
      }
    }
    if (getRanges() != DEFAULT) {
      sb.append(":");
      for (int i = 0; i < getRanges().length; i++) {
        if (i != 0) {sb.append(",");}
        sb.append(getRanges()[i][0]);
        if (getRanges()[i][0] != getRanges()[i][1]) {
          sb.append("...");
          sb.append(getRanges()[i][1]);
        }
      }
    }
    return sb.toString();
  }
  
  private static class Parser
  {
    
    static Tuple2 D(String string, int depth) throws ParseException
    {
      System.out.println("D:" + depth);
      int tint = depth;
      boolean decimal = false;
      if (string.length() <= depth || !(string.charAt(tint) >= '0' && string.charAt(tint) <= '9')) {
        System.out.println(string);
        for (int i = 0; i < depth; i++) {
          System.out.print(" ");
        }
        System.out.println("^");
        throw new ParseException("Expected Digit", depth);}
      while (tint < string.length()) {
        if (string.charAt(tint) >= '0' && string.charAt(tint) <= '9') {
          tint++; continue;
        } else if (!decimal && string.charAt(tint) == '.' &&
                   string.charAt(tint + 1) > '0' && string.charAt(tint + 1) < '9') {
          tint += 2; decimal = true; continue;
        }
        break;
      }
      double d = 0;
      try {
        d = Double.parseDouble(string.substring(depth, tint));
      } catch (NumberFormatException nfe) {
        nfe.printStackTrace(); throw new ParseException("Unexpected", depth);
      }
      Tuple2 tup2 = new Tuple2();
      tup2.mDouble = d;
      tup2.mDepth = tint;
      return tup2;
    }
    
    static int R(String string, int depth, Tuple tup) throws ParseException
    {
      System.out.println("R:" + depth);
      Tuple2 tup2 = D(string, depth);
      depth = tup2.mDepth;
      double first = tup2.mDouble;
      double second = tup2.mDouble;
      System.out.println("R:" + depth);
      if (string.startsWith("...", depth)) {
        depth += 3;
        System.out.println("R:" + depth);
        tup2 = D(string, depth);
        depth = tup2.mDepth;
        second = tup2.mDouble;
      }
      tup.mRange.add(new double[]{first, second});
      System.out.println(tup.mRange);
      System.out.println(string);
      for (int i = 0; i < depth; i++) {
        System.out.print(" ");
      }
      System.out.println("^");
      System.out.println("R:" + depth);
      return depth;
    }
    
    static int O(String string, int depth, Tuple tup) throws ParseException
    {
      System.out.println("O:" + depth);
      depth = R(string, depth, tup);
      if (!string.startsWith(",", depth)) {return depth;}
      depth++;
      depth = O(string, depth, tup);
      System.out.println(string);
      for (int i = 0; i < depth; i++) {
        System.out.print(" ");
      }
      System.out.println("^");
      return depth;
    }
    
    static int S(String string, int depth, Tuple tup) throws ParseException
    {
      System.out.println("S:" + depth);
      if (string.startsWith("Boolean", depth)) {
        depth += "Boolean".length();
        tup.mType = Boolean.class;
      } else if (string.startsWith("Double", depth)) {
        depth += "Double".length();
        tup.mType = Double.class;
        if (string.startsWith(":", depth)) {
          depth++;
          depth = O(string, depth, tup);
        }
      }
      System.out.println(string);
      for (int i = 0; i < depth; i++) {
        System.out.print(" ");
      }
      System.out.println("^");
      return depth;
    }
    
    static Tuple parse(String string) throws ParseException
    {
      string = string.replaceAll(" +", "");
      Tuple tup = new Tuple();
      int depth = 0;
      try {
        depth = S(string, depth, tup);
      } catch(ParseException pe) {
        depth = O(string, depth, tup);
      }
      System.out.println(string);
      for (int i = 0; i < depth; i++) {
        System.out.print(" ");
      }
      System.out.println("^");
      if (depth != string.length()) {
        throw new ParseException("Not all matched", depth); 
      }
      return tup;
    }
    
  }
  
  private static class Tuple
  {
    Class<?> mType = null;
    List<double[]> mRange = new ArrayList<double[]>();
  }
  
  private static class Tuple2
  {
    int mDepth = 0;
    double mDouble = 0;
  }
}
