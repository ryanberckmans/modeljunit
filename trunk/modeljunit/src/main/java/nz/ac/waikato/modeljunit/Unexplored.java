package nz.ac.waikato.modeljunit;

/**
 * Each Unexplored object represents one transition that has not yet been explored.
 * These objects are typically created during a traversal of a model, when we
 * notice an action that has a true guard, and we have never taken that action
 * from the current state.
 */
public class Unexplored {

    private Object state_;
    
    private int actionNum_;

    /**
     * Create an object that remembers a transition that we have not yet taken.
     *
     * @param state   the starting state of the transition
     * @param action  the number of the action that we should take
     */
    public Unexplored(Object state, int action) {
        this.state_ = state;
        this.actionNum_ = action;
    }

    public Object getState() {
      return state_;
    }

    public int getAction() {
      return actionNum_;
    }

    @Override
    public int hashCode() {
      return state_.hashCode() ^ actionNum_;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other instanceof Unexplored) {
        Unexplored unexplored = (Unexplored) other;
        return unexplored.getState().equals(state_)
            && unexplored.getAction() == actionNum_;
      }
      return false;
    }
    
    @Override
    public String toString() {
      return "(" + state_.toString() + ", " + actionNum_ + ")";
    }
}
