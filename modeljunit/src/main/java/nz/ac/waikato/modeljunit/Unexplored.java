package nz.ac.waikato.modeljunit;

public class Unexplored {

    private Object state_;
    
    private String action_;

    public Object getState() {
      return state_;
    }

    public void setState_(Object state) {
      this.state_ = state;
    }

    public String getAction() {
      return action_;
    }

    public void setAction_(String action) {
      this.action_ = action;
    }
    
    @Override
    public int hashCode() {
      return state_.hashCode() ^ action_.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
      if (other instanceof Unexplored) {
        Unexplored unexplored = (Unexplored) other;
        return unexplored.getState().equals(state_)
            && unexplored.getAction().equals(action_);
      }
      return false;
    }
    
    @Override
    public String toString() {
      return "(" + state_.toString() + ", " + action_ + ")";
    }
}
