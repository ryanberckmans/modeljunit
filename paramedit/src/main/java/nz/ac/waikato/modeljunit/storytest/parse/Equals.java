/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest.parse;

/**
 * @author root
 *
 */
public class Equals
  extends Arithmetic
{
  Equals(Function f, Function s)
  {
    super(f, s);
  }
  
  public double calculate(int row)
  {
    return mFirst.calculate(row) == mSecond.calculate(row) ? 1 : 0;
  }
}