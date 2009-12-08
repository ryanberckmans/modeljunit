/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest.parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

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
  
  private final Function mFunc;
  
  private final boolean mIsEditable;
  
  private final String mString;
  
  public TypeRange(Tuple tup, int column, CalcTable calc, Function func, boolean editable, String string) throws ParseException
  {
    mString = string;
    mFunc = func;
    mIsEditable = editable;
    mCalc = calc;
    mColumn = column;
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
  
  public double calculateRow(int row)
  {
    return mFunc.calculate(row);
  }
  
  public boolean isEditable()
  {
    return mIsEditable;
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
    if (isEditable()) {
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
    } else {
      return mString;
    }
  }
}
