package nz.ac.waikato.modeljunit;

import java.util.BitSet;
import java.util.Map;

public class QuickTester extends RandomTester {

    /** How far should we explore? */
    protected int maxDepth_ = 3;
  
    /** The maximum explored memory of each generated test */
    protected int maxMemory_ = 1000;
  
    /** Some unexplored states and transitions that need to be done */
    protected Map<Unexplored, Path> todo_;
    
    /** ALL transitions that have already been visited. */
    protected Map<Object, BitSet> done_;
    
    protected GraphListener graph_;
  
    
    public QuickTester(Model model) {
      super(model);
      graph_ = (GraphListener) model_.addListener("graph");
    }

    public QuickTester(FsmModel fsm) {
      super(fsm);
      graph_ = (GraphListener) model_.addListener("graph");
    }

//    /** Maybe remember a transition that has not been taken. */
//    protected void addTodo(Object state, int actionNum, int depth, ArrayList<Transition> sequence) {
//      if (todo_.size() < maxMemory_) {
//        todo_.put(new Unexplored(state, actionNum), new Path(depth, sequence));
//      }
//    }

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
      
      /* TODO
       * 1. do random walk
       * 2. add the unexplored path to todo_
       */
      
      return 0;
    }
}
