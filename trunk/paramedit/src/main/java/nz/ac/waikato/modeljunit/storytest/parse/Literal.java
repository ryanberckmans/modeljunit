/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest.parse;

import nz.ac.waikato.modeljunit.storytest.CalcTable;

/**
 * @author root
 *
 */
public class Literal
  implements Function
{
private final double mValue;

Literal(double value)
{
  mValue = value;
}

public double calculate(int row)
{
  return mValue;
}
}
