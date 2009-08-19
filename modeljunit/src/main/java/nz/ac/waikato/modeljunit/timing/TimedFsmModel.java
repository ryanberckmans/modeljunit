/**
 * 
 */

package nz.ac.waikato.modeljunit.timing;

import nz.ac.waikato.modeljunit.FsmModel;

/**
 * @author Scott.
 *	
 * Interface for creating Finite State Machines that use the
 * real-time features of ModelJUnit such as a time variable
 * and timeout variables with timeout actions.
 * 
 * @see Timeout
 * @see Time
 *
 */
public interface TimedFsmModel extends FsmModel
{

  /**
   * Function used to get the amount of time to move forward
   * the next time that the time is incremented. The framework 
   * will check whether this increment would advance the time
   * past any enabled timeout(s) and handles execution of the timeout(s).
   *
   * @return an integer value greater than zero.
   */
  public int getNextTimeIncrement();
}
