/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest.parse;

/**
 * @author root
 *
 */
public abstract class Arithmetic
  implements Function
{
  protected final Function mFirst;
  protected final Function mSecond;
  
  public Arithmetic(Function first, Function second)
  {
    mFirst = first;
    mSecond = second;
  }
}
