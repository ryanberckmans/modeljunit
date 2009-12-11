package nz.ac.waikato.modeljunit;

import nz.ac.waikato.modeljunit.examples.SimpleSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * QuickTester is similar to RandomTester, but remembers unexplored
 * paths for later exploration.  That is, it keeps a cache of any actions
 * with true guards that are seen but not taken during a random walk.
 * It remembers the exact path that made the guard true, so that it can
 * re-traverse that path later, to explore that transition.  The
 * cache size has a maximum limit so that memory is not exhausted on huge models.
 */
public class QuickTester extends RandomTester {

    /** How far should we explore? */
    protected int maxDepth_ = 10;
  
    /** The maximum explored memory of each generated test */
    protected int maxMemory_ = 1000;
  
    /** Some unexplored states and transitions that need to be done */
    protected Map<Unexplored, Path> todo_ = new HashMap<Unexplored, Path>();
    
    /** ALL transitions that have already been visited. */
    protected Map<Object, BitSet> done_;

    /**
     * The current test sequence that we are exploring.
     * (so <code>sequence_.size()</code> is the current length of the path.
     * This is never null.  We only add onto the end of it, and never clear it.
     */
    ArrayList<Transition> sequence_ = new ArrayList<Transition>();

    /** If non-null, this is the Unexplored transition that we are trying to reach */
    private Unexplored currentGoal_;

    /**
     * The path that we are currently following to get to our current goal.
     * This is null if and only if currentGoal_ is null.
     */
    private Path currentPath_;

    public QuickTester(Model model) {
      super(model);
      model_.addListener(new RecordTestSequence());
    }

    public QuickTester(FsmModel fsm) {
      super(fsm);
      model_.addListener(new RecordTestSequence());
    }

    /** This automatically adds transitions to our internal sequence. */
    private class RecordTestSequence extends AbstractListener {

        @Override
        public String getName() {
            return "RecordTestSequence";
        }

        @Override
        public void doneTransition(int action, Transition tr) {
            System.out.println("DEBUG: did " + tr);
            sequence_.add(tr);
        }
    }

    /**
     * Maybe remember a transition that has not been taken.
     * It records the current test sequence and the current depth.
     *
     * @param state     the starting state of the transition
     * @param actionNum the number of an action (with a true guard)
     */
    protected void addTodo(Object state, int actionNum) {
      if (todo_.size() < maxMemory_) {
        todo_.put(new Unexplored(state, actionNum), new Path(sequence_));
      }
    }

    public int getMaxDepth_() {
        return maxDepth_;
    }

    public void setMaxDepth_(int maxDepth) {
        this.maxDepth_ = maxDepth;
    }

    public int getMaxMemory_() {
        return maxMemory_;
    }

    public void setMaxMemory_(int maxMemory) {
        this.maxMemory_ = maxMemory;
    }
    
    @Override
    public int generate() {
        final int taken;
        if (currentGoal_ == null) {
            // let RandomTester continue a random walk
            // TODO: reset if our test sequence is longer than maxDepth
            taken = super.generate();
        } else {
            final int currLength = sequence_.size();
            final int pathLength = currentPath_.getDepth();
            final int tryAction;
            if (currLength == pathLength) {
                // we've reached the desired depth, so try to take our goal transition.
                tryAction = currentGoal_.getAction();
                currentGoal_ = null;
                currentPath_ = null;
            } else {
                Transition tr = currentPath_.getTransitions().get(currLength);
                tryAction = model_.getActionNumber(tr.getAction());
            }
            if (model_.doAction(tryAction)) {
                taken = currentGoal_.getAction();
            } else {
                // TODO: show some kind of warning, because the model is non-deterministic
                taken = -1;  // or continue with a random walk?
            }
        }
        if (taken < 0) {
            // start a new test sequence (must leave old one unchanged)
            sequence_ = new ArrayList<Transition>();
            // should we try one of the todo_ paths?
            // TODO: make the probability user-settable, rather than always 50%.
            if (todo_.size() > 0 && rand_.nextBoolean()) {
                currentGoal_ = todo_.keySet().iterator().next();
                currentPath_ = todo_.get(currentGoal_);
                todo_.remove(currentGoal_);
            }
        } else {
            // TODO: find all true guards and add them to todo_
            //      (unless they are already in done_)
        }
        return taken;
    }

    public static void main(String[] args) {
        QuickTester qt = new QuickTester(new SimpleSet());
        qt.addListener(new VerboseListener());
        qt.generate(20);
    }
}
