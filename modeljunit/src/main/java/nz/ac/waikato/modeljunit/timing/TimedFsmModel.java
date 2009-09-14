/**
 *
 */

package nz.ac.waikato.modeljunit.timing;

import java.util.Random;

import nz.ac.waikato.modeljunit.FsmModel;

/**
 * Interface for Real-time Finite State Machine models.
 * These models can use the real-time features of ModelJUnit,
 * such as a time variable and timeout variables with timeout
 * actions.  In addition to the usual <code>getState</code>
 * and <code>reset</code> methods, a real-time model must 
 * define a <code>getNextTimeIncrement</code> method that
 * chooses how much to advance time by between each action.
 *
 * @author Scott Thompson.
 *
 * @see Timeout
 * @see Time
 *
 */
public interface TimedFsmModel extends FsmModel {

  /** This value indicates that a timeout is not enabled. */
  public static final int TIMEOUT_DISABLED = -1;

  /**
   * Get the amount of time to move forward the next time
   * that the time is advanced.  The framework will check whether this
   * increment would advance the time past any enabled timeout(s), and
   * will automatically execute one of those timeouts rather than a
   * normal action when that happens.
   * 
   * @param ran can be used to choose random time distributions
   *
   * @return an integer value greater than zero.
   */
  public int getNextTimeIncrement(Random ran);
}
