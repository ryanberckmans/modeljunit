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
    protected Map<Object, BitSet> done_ = new HashMap<Object, BitSet>();;

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

    protected GraphListener graph_;
    
    /**
     * True if the path has been visited before
     */
    private boolean isRevisit;
    
    public QuickTester(Model model) {
      super(model);
      model_.addListener(new RecordTestSequence());
      graph_ = (GraphListener) model_.addListener("graph");
    }

    public QuickTester(FsmModel fsm) {
      super(fsm);
      model_.addListener(new RecordTestSequence());
      graph_ = (GraphListener) model_.addListener("graph");
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

    public int getMaxDepth() {
        return maxDepth_;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth_ = maxDepth;
    }
    
    @Override
    public int generate() {
        if (sequence_.size() >= maxDepth_) {
            // Reset - test sequence is longer than maxDepth
            return resetAction();
        }
        if (isRevisit) {
            // The path has been visited before
            return revisit();
        } 
        if (currentGoal_ != null) {
            return visitCurrentGoal();
        }

        int taken;
        Object origin = model_.getCurrentState();
        addTodoMap(origin);
        taken = getRandomAction(origin);
        if (taken == -1) {
            // All actions going out from this state has been explored, 
            return resetAction();
        } 
        
        if (currentPath_ == null) {
            currentPath_ = new Path(sequence_);
        } else {
            currentPath_.setTransitions(sequence_);
            currentPath_.setDepth(sequence_.size());
        }
        
        currentGoal_ = new Unexplored(origin, taken);
        return visitCurrentGoal();
    }
    
    /**
     * Do the action in the current goal from the origin state,
     * then remove current goal from the todo_ map and add this
     * action to done_ map.
     * 
     * @return    action taken
     */
    private int visitCurrentGoal() {
        Object origin = model_.getCurrentState();
        int tryAction= currentGoal_.getAction();
        Unexplored exp = currentGoal_;
        currentGoal_ = null;
        if (model_.doAction(tryAction)) {
            todo_.remove(exp);
            done_.get(origin).set(tryAction);
            return tryAction;
        } else {
            // TODO: show some kind of warning, because the model is non-deterministic
            reset();
            return -1;  // or continue with a random walk?
        }
    }
    
    /**
     * Add the unexplored action from the state to todo_ map
     * and set all the bits in done_ map for this state to falsetest sequence is longer than maxDepth
     * as initial.
     * 
     * @param state   State that is evaluated the unexplored actions
     */
    public void addTodoMap(Object state) {
        BitSet doneSet = done_.get(state);
        if (doneSet == null) {
            // The state has never been visited before
            doneSet = graph_.getTodo(state);
            for (int i = 0; i < doneSet.length(); i++) {
                addTodo(state, i);
            }
            
            doneSet.flip(0, doneSet.length());
            done_.put(state, doneSet);
        } 
    }

    /**
     * Returns a random unexplored action for the state from todo_ map.
     * 
     * @param state
     * @return
     */
    public int getRandomAction(Object state) {
        int nTrans = model_.getNumActions();
        BitSet tried = done_.get(state);
        int index = rand_.nextInt(nTrans);
        
        while (tried.cardinality() < nTrans) {
            while (tried.get(index)) {
                index = rand_.nextInt(nTrans);
            }
            tried.set(index); 
            return index;
        }
        
        return -1;
    }
    
    /**
     * Revisit the path being stored in the currentPath until it matches the currentGoal.
     * 
     * @return
     */
    public int revisit() {
        int actionNum = -1;
        Transition tran = currentPath_.getTransitions().get(sequence_.size());
        if (tran.getEndState().equals(currentGoal_.getState())) {
            isRevisit = false;
        }
        
        actionNum = model_.getActionNumber(tran.getAction());
        if (!model_.doAction(actionNum)) {
            actionNum = -1;
        }
        
        return actionNum;
    }
    
    private int resetAction() {
        sequence_ = new ArrayList<Transition>();
        // should we try one of the todo_ paths?
        if (todo_.size() > 0 && rand_.nextBoolean()) {    
            // There are more unexplored in todo_, get the first one from the map
            currentGoal_ = todo_.keySet().iterator().next();
            currentPath_ = todo_.get(currentGoal_);
            isRevisit = true;
            model_.doReset("Forced");
            return -1;
        } else {
            if (sequence_.size() >= maxDepth_) {
              // Test sequence is longer than maxDepth
              model_.doReset("Forced");
              currentGoal_ = null;
              currentPath_ = null;
            } else {
              // All other reset cases
              reset();
            }
            return -1;
        }
    }
    
    @Override
    public void reset() {
        super.reset();
        clear();
    }
    
    public void clear() {
        graph_.clearDoneTodo();
        currentGoal_ = null;
        currentPath_ = null;
        todo_ = new HashMap<Unexplored, Path>();
        done_ = new HashMap<Object, BitSet>();
        sequence_ = new ArrayList<Transition>();
    }
    
    public static void main(String[] args) {
        QuickTester qt = new QuickTester(new SimpleSet());
        qt.addListener(new VerboseListener());
        qt.generate(20);
    }
}
