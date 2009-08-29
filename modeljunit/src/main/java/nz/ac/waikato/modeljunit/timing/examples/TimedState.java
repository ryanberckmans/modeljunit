package nz.ac.waikato.modeljunit.timing.examples;

/**
 * This class is useful for the <code>getState</code> objects of timed models.
 * It remembers the time associated with each state, so that traces can
 * display times.  But it ignores that time for the purposes of equality
 * between states, so that an FSM can be constructed based on just the
 * state values.
 * 
 * This class could be generic in the type of the state parameter,
 * but most models return strings from getState, so we have left
 * this class simple and non-generic for the moment.
 *
 * @author marku
 */
public class TimedState {
  private String state;
  private int time;
  public TimedState(String state, int time) {
    this.state = state;
    this.time = time;
  }

  public String toString() {
    return state + "@" + time;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TimedState && ((TimedState)obj).state.equals(state);
  }

  @Override
  public int hashCode() {
    return state.hashCode();
  }
}