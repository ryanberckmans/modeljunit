package nz.ac.waikato.modeljunit;

import java.util.ArrayList;
import java.util.List;

/**
 * Each Path object represents a particular point in a test sequence.
 * That is, it comprises a pointer to a sequence of transitions (which may grow to
 * contain more transitions, but existing entries must not be modified or cleared),
 * plus an integer (<code>Depth</code>) that records a particular position along that sequence.
 *
 * Depth can range from 0 up to the length of the sequence.
 */
public class Path {

    private List<Transition> transitions_;
    
    private int depth_;

    /**
     * Creates an object that remembers a point in the given test sequence.
     * This takes a snapshot of the current length of <code>transitions_</code>.
     * @param transitions the current test sequence.
     */
    public Path(List<Transition> transitions) {
        this.transitions_ = transitions;
        this.depth_ = transitions.size();
    }

    public List<Transition> getTransitions() {
      return transitions_;
    }

    public void setTransitions(List<Transition> transitions) {
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
