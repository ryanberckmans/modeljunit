/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest.parse;

import java.text.ParseException;
import java.util.Arrays;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

/**
 * @author root
 *
 */
public class Parser
{

  static void debugPrintln(String msg) {
      // System.out.println(msg);
  }

  static void debugPrint(String msg) {
      // System.out.print(msg);
  }

  static Tuple2 D(String string, int depth) throws ParseException
  {
    debugPrintln("D:" + depth);
    int tint = depth;
    boolean decimal = false;
    if (string.length() <= depth || !(string.charAt(tint) >= '0' && string.charAt(tint) <= '9')) {
      debugPrintln(string);
      for (int i = 0; i < depth; i++) {
        debugPrint(" ");
      }
      debugPrintln("^");
      throw new ParseException("Expected Digit", depth);
    }
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
    debugPrintln("R:" + depth);
    Tuple2 tup2 = D(string, depth);
    depth = tup2.mDepth;
    double first = tup2.mDouble;
    double second = tup2.mDouble;
    debugPrintln("R:" + depth);
    if (string.startsWith("...", depth)) {
      depth += 3;
      debugPrintln("R:" + depth);
      tup2 = D(string, depth);
      depth = tup2.mDepth;
      second = tup2.mDouble;
    }
    tup.mRange.add(new double[]{first, second});
    debugPrintln("" + tup.mRange);
    debugPrintln(string);
    for (int i = 0; i < depth; i++) {
        debugPrint(" ");
    }
    debugPrintln("^");
    debugPrintln("R:" + depth);
    return depth;
  }
  
  static int O(String string, int depth, Tuple tup) throws ParseException
  {
    debugPrintln("O:" + depth);
    depth = R(string, depth, tup);
    if (!string.startsWith(",", depth)) {return depth;}
    depth++;
    depth = O(string, depth, tup);
    debugPrintln(string);
    for (int i = 0; i < depth; i++) {
        debugPrint(" ");
    }
    debugPrintln("^");
    return depth;
  }
  
  static int S(String string, int depth, Tuple tup) throws ParseException
  {
    debugPrintln("S:" + depth);
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
    debugPrintln(string);
    for (int i = 0; i < depth; i++) {
        debugPrint(" ");
    }
    debugPrintln("^");
    return depth;
  }
  
  private static Variable var(String string, CalcTable calc, Tuple2 tup) throws ParseException
  {
    String check = string.toLowerCase();
    int depth = tup.mDepth;
    if (!(check.charAt(depth) >= 'a' && check.charAt(depth) <= 'z')) {
      throw new ParseException("Expected Alpha", depth);
    }
    depth++;
    while (true) {
      if (check.length() <= depth) {break;}
      if (!((check.charAt(depth) >= 'a' && check.charAt(depth) <= 'z') ||
             check.charAt(depth) >= '0' && check.charAt(depth) <= '9')) {
        break;
      }
      depth++;
    }
    debugPrintln(tup.mDepth + "," + depth);
    debugPrintln(tup.mDepth + "," + (depth - tup.mDepth));
    String name = string.substring(tup.mDepth, depth);
    debugPrintln(name);
    tup.mDepth = depth;
    return new Variable(name, calc);
  }
  
  private static Function factor(String string, CalcTable calc, Tuple2 tup) throws ParseException
  {
    int depth = tup.mDepth;
    if (depth < string.length() && string.charAt(depth) == '(') {
      depth++;
      tup.mDepth = depth;
      Function e = exp(string, calc, tup);
      depth = tup.mDepth;
      if (depth < string.length() && string.charAt(depth) == ')') {
        throw new ParseException("expected closing bracket", depth);
      }
      depth++;
      tup.mDepth = depth;
      return e;
    }
    try {
      Tuple2 tup2 = D(string, tup.mDepth);
      depth = tup2.mDepth;
      tup.mDepth = depth;
      return new Literal(tup2.mDouble);
    } catch (ParseException pe) {
    }
    return var(string, calc, tup);
  }
  
  private static Function term(String string, CalcTable calc, Tuple2 tup) throws ParseException
  {
    Function func1 = factor(string, calc, tup);
    int depth = tup.mDepth;
    debugPrintln("TERM");
    debugPrintln(string);
    for (int i = 0; i < depth; i++) {
        debugPrint(" ");
    }
    debugPrintln("^");
    if (string.length() <= depth) {return func1;}
    if (string.charAt(depth) == '*' || string.charAt(depth) == '/') {
      char mod = string.charAt(depth);
      depth++;
      tup.mDepth = depth;
      Function func2 = term(string, calc, tup);
      if (mod == '*') {
        return new Product(func1, func2);
      } else {
        return new Division(func1, func2);
      }
    }
    return func1;
  }
  
  private static Function exp(String string, CalcTable calc, Tuple2 tup) throws ParseException
  {
    Function func1 = term(string, calc, tup);
    int depth = tup.mDepth;
    debugPrintln("EXP");
    debugPrintln(string);
    for (int i = 0; i < depth; i++) {
        debugPrint(" ");
    }
    debugPrintln("^");
    if (string.length() <= depth) {return func1;}
    if (string.charAt(depth) == '+' || string.charAt(depth) == '-') {
      debugPrintln("plus | minus");
      char mod = string.charAt(depth);
      depth++;
      tup.mDepth = depth;
      Function func2 = exp(string, calc, tup);
      if (mod == '+') {
        return new Plus(func1, func2);
      } else {
        return new Minus(func1, func2);
      }
    }
    return func1;
  }
  
  public static Function form(String string, CalcTable calc, Tuple2 tup) throws ParseException
  {
    Function func1 = exp(string, calc, tup);
    int depth = tup.mDepth;
    if (string.length() <= depth) {return func1;}
    if (string.charAt(depth) == '=' || string.charAt(depth) == '<' || string.charAt(depth) == '>') {
      char mod = string.charAt(depth);
      depth++;
      boolean eq = false;
      if (string.charAt(depth) == '=') {
        eq = true;
        depth++;
      }
      tup.mDepth = depth;
      Function func2 = exp(string, calc, tup);
      if (mod == '=') {
        return new Equals(func1, func2);
      } else if (mod == '<'){
        return eq ? new LessThanEquals(func1, func2) : new LessThan(func1, func2);
      } else {
        return eq ? new GreaterThanEquals(func1, func2) : new GreaterThan(func1, func2);
      }
    }
    if (string.charAt(depth) == '/' || string.charAt(depth) == '<' && string.charAt(depth + 1) == '=') {
      depth += 2;
      tup.mDepth = depth;
      Function func2 = exp(string, calc, tup);
      return new NotEquals(func1, func2);
    }
    return func1;
  }
  
  public static TypeRange parseFunction(String string, int column, CalcTable calc) throws ParseException
  {
    string = string.replaceAll(" +", "");
    Tuple2 tup = new Tuple2();
    tup.mDepth = 0;
    Function func = form(string, calc, tup);
    Tuple tuple = new Tuple();
    tuple.mRange = Arrays.asList(new double[][] {{Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}});
    return new TypeRange(tuple, column, calc, func, false, string);
  }
  
  public static TypeRange parseTypeRange(String string, int column, CalcTable calc) throws ParseException
  {
    string = string.replaceAll(" +", "");
    Tuple tup = new Tuple();
    int depth = 0;
    try {
      depth = S(string, depth, tup);
    } catch(ParseException pe) {
      depth = O(string, depth, tup);
    }
    debugPrintln(string);
    for (int i = 0; i < depth; i++) {
        debugPrint(" ");
    }
    debugPrintln("^");
    if (depth != string.length()) {
      throw new ParseException("Not all matched", depth); 
    }
    return new TypeRange(tup, column, calc, new Variable(calc.getColumnName(column), calc),true, string);
  }
}
