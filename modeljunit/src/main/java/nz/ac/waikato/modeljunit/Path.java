package nz.ac.waikato.modeljunit;

public class Path {

    private Transition[] transitions_;
    
    private int depth_;

    public Transition[] getTransitions() {
      return transitions_;
    }

    public void setTransitions(Transition[] transitions) {
      this.transitions_ = transitions;
    }

    public int getDepth() {
      return depth_;
    }

    public void setDepth(int depth) {
      this.depth_ = depth;
    }
    
    @Override
    public int hashCode() {
      return transitions_.hashCode() ^ depth_;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other instanceof Path) {
        Path path = (Path) other;
        return path.getTransitions().equals(transitions_)
            && path.getDepth() == depth_;
      }
      return false;
    }
    
    @Override
    public String toString() {
      return "(" + transitions_.toString() + ", " + String.valueOf(depth_) + ")";
    }
}
