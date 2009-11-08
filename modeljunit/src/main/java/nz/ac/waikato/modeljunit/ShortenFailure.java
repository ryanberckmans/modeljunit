
package nz.ac.waikato.modeljunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import nz.ac.waikato.jdsl.graph.algo.IntegerDijkstraPathfinder;
import nz.ac.waikato.jdsl.graph.api.Edge;
import nz.ac.waikato.jdsl.graph.api.EdgeIterator;
import nz.ac.waikato.jdsl.graph.api.InspectableGraph;
import nz.ac.waikato.jdsl.graph.api.Vertex;
import nz.ac.waikato.jdsl.graph.api.VertexIterator;

/**
 * This class can minimize (shorten) a failing test sequence.
 * It uses two algorithms for minimization (CutCycles algorithm, Beeline algorithm). 
 * <p>
 * It requires the used tester and TestFailureException to be passed before
 * starting the minimization process, and returns the new shortened failing
 * test sequence as a TestFailureException.
 * </p>
 * @author Nadin J. Wadi
 */
public class ShortenFailure
{

  private boolean _sameState;

  private boolean _sameAction;

  private TestFailureException _failure;

  private Tester _tester;

  private List<Transition> _oldFailureSequence;

  private List<Transition> _failureSequence;

  private HashMap<String, ArrayList<Integer>> _cyclesHashMap;

  /**
   * Constructor method to create ShortenFailure object.
   * @param tester, used for running the model.
   * @param failure, the thrown exception.
   */
  public ShortenFailure(Tester tester, TestFailureException failure)
  {
    //make sure passed attributes are not null
    Assert.assertNotNull(tester);
    Assert.assertNotNull(failure);

    _cyclesHashMap = new HashMap<String, ArrayList<Integer>>();

    _sameState = true;
    _sameAction = true;

    _failure = failure; // to get the failure path
    _oldFailureSequence = new ArrayList<Transition>();
    _oldFailureSequence.addAll(failure.getSequence().subList(0,
        failure.getSequence().size()));

    _failureSequence = new ArrayList<Transition>();
    _failureSequence.addAll(failure.getSequence().subList(0,
        failure.getSequence().size()));

    _tester = tester; // to run the new path and check its validation
  }

  /**
   * Whether to consider the last state of the original failing test sequence in the shortening process.
   * @param value true means we want the same last state of the original failing test sequence, and
   * false means the shorten failing test sequence can have a different last state.
   */
  public void setSameState(boolean value)
  {
    _sameState = value;
  }

  public boolean getSameState()
  {
    return _sameState;
  }

  /**
   * Whether to consider the last action of the original failing test sequence in the shortening process.
   * @param value true means we want the same last action of the original failing test sequence, and
   * false means the shorten failing test sequence can have a different last action.
   */
  public void setSameAction(boolean value)
  {
    _sameAction = value;
  }

  public boolean getSameAction()
  {
    return _sameAction;
  }

  /**
   * Returns an exception representing the shorten failing test sequence.
   * @return exception with the new failing test sequence.
   */
  public TestFailureException getFailure()
  {
    _failure.setSequence(_failureSequence);
    return _failure;
  }

  /**
   * Returns the original failing test sequence.
   * @return list of transition of the original failing test sequence.
   */
  public List<Transition> getOldFailureSequence()
  {
    return _oldFailureSequence;
  }

  /**
   * This method uses the Beeline algorithm for shortening failing test, which is build on Harry Robinson idea.
   * <p>The method chooses two random point in the current failing test sequence, get the associated 
   * transitions and chooses the first state of the first transition and the end state of the second transition,
   * then finds the shortest path using "Dijkstra" algorithm,and re-executes the new failing test sequence to check for the
   * success of the shortening process.</p>
   * @param graph is the graph of the model.
   * @return true if the shortening process was successful, otherwise return false. 
   */
  public boolean beelineShorten(InspectableGraph graph) // shorten once
  {
    Assert.assertNotNull(graph);

    if (_failureSequence.isEmpty())
      return false;
    //implementation of the bee-line algorithm.
    int toIndex = getRandomNumber(_failureSequence.size());
    int fromIndex = getRandomNumber(toIndex);

    Object source = _failureSequence.get(fromIndex).getStartState();
    Object dest = _failureSequence.get(toIndex).getEndState();

    //path finder finds the shortest path
    List<Transition> subList = getShortestPath(graph, getVertex(graph, source),
        getVertex(graph, dest));
    List<Transition> newList = getRefinedSequence(_failureSequence, subList,
        fromIndex, toIndex);

    if (areListsEqual(_failureSequence, newList))
      return false;
    List<Transition> resultList = reRunTestSequence(newList);
    if (resultList != null) {

      _failureSequence = resultList;
      return true; //success 
    }
    return false;

  }

  /**
   * This method applies the Beeline algorithm for the original failing test sequence.
   * It tries shortening the failing test sequence for at least a number equal to the given "effort" value.
   * @param graph is the graph of the model
   * @param effort is the number of failing tries before the shortening process stops. 
   */
  public void beelineShortest(InspectableGraph graph, int effort)
  {
    Assert.assertNotNull(graph);
    int counter = 0;
    while (counter < effort) {
      boolean success = beelineShorten(graph);
      if (!success)
        counter++;
      else
        counter = 0;
    }
  }

  /**
   * This method uses the CutCycles algorithm for shortening the current failing test sequence.
   * <p>The method finds all the cycles associated with each state in the current failing test sequence, then 
   * it randomly chooses one state and randomly cuts off one of its cycles, then re-executes the new failing test sequence to check for the
   * success of the shortening process.</p>
   * @return true if the shortening process was successful, otherwise return false.
   */
  public boolean cutCycle()
  {
    if (_failureSequence.isEmpty())
      return false;

    _cyclesHashMap = findAllSequenceCycles(_failureSequence);
    ArrayList<Integer> cyclesIndexList = new ArrayList<Integer>();

    int stateIndexNo = getRandomNumber(_failureSequence.size());
    String keyObject = _failureSequence.get(stateIndexNo).getStartState()
        .toString();
    cyclesIndexList = _cyclesHashMap.get(keyObject);

    if (cyclesIndexList.size() <= 1)//means no cycles
      return false; //found no cycles in the previous number of iterations.
    int toIndex = getRandomNumber(cyclesIndexList.size());
    int fromIndex = getRandomNumber(toIndex);

    List<Transition> newList = getRefinedSequence(_failureSequence,
        cyclesIndexList.get(fromIndex), cyclesIndexList.get(toIndex));

    if (areListsEqual(_failureSequence, newList))
      return false;
    List<Transition> resultList = reRunTestSequence(newList);
    if (resultList != null) {

      _failureSequence = resultList;
      return true; //success 
    }
    return false;
  }

  /**
   * This method applies the CutCycles algorithm for the original failing test sequence.
   * It tries shortening the failing test sequence for at least a number equal to the given "effort" value.
   * @param effort is the number of failing tries before the shortening process stops. 
   */
  public void cutCycles(int effort)
  {
    int counter = 0;
    while (counter < effort) {
      boolean success = cutCycle();
      if (!success)
        counter++;
      else
        counter = 0;
    }
  }

  //failSeq -> got the failure (sequence is valid)
  //null -> did not produce the failure (sequence is invalid)
  private List<Transition> reRunTestSequence(List<Transition> sequence)
  {
    if (sequence == null || sequence.isEmpty())
      return null;
    Model model = _tester.getModel();
    Object currState = null;
    int currActionNo = -1;
    try {
      model.doReset();
      for (int i = 0; i < sequence.size(); i++) {
        String actionName = sequence.get(i).getAction();
        currState = model.getCurrentState();
        currActionNo = model.getActionNumber(actionName);
        boolean accepted = model.doAction(currActionNo);
        if (accepted == false)
          return null;

        if (!model.getCurrentState().equals(sequence.get(i).getEndState()))
          return null;
      }
      //same state
      assert model.getCurrentState().equals(_failure.getState());
      //do the final same action
      String actionName = _failure.getActionName();//action when failure happened
      currState = model.getCurrentState();
      currActionNo = model.getActionNumber(actionName);
      model.doAction(currActionNo);
      return null; // because we are expecting a failure
    }
    catch (TestFailureException e) {
      boolean correctAction = currActionNo == model.getActionNumber(_failure
          .getActionName());
      boolean correctState = currState.equals(_failure.getState());
      //			return correctAction && correctState // found the same error
      //				|| !_sameAction && !_sameState   // user doesn't care which error we find
      //				|| correctAction == _sameAction && correctState == _sameState;

      // using implication
      if ((!_sameAction || correctAction) && (!_sameState || correctState)) {
        // found a good failure
        List<Transition> failSeq = new ArrayList<Transition>();
        failSeq.addAll(e.getSequence().subList(0, e.getSequence().size()));
        return failSeq;
      }
      return null;
    }

  }

  //get the vertex of the given state. 
  public Vertex getVertex(InspectableGraph graph, Object state)
  {
    VertexIterator itr = graph.vertices();
    while (itr.hasNext()) {
      Vertex v = itr.nextVertex();
      if (v.element().equals(state))
        return v;
    }
    return null;
  }

  //check whether the two given lists are equal or not.
  public boolean areListsEqual(List<Transition> l1, List<Transition> l2)
  {
    if (l1 == null || l2 == null)
      return false;
    if (l1.size() != l2.size())
      return false;
    for (int i = 0; i < l1.size(); i++) {
      if (!l1.get(i).equals(l2.get(i)))
        return false;
    }
    return true;
  }

  // return a random number from 0 to the given number.
  public int getRandomNumber(int toNumber)
  {
    if (toNumber == 0)//not negative or 0
      return toNumber;

    Random rand = new Random();
    int randNo = rand.nextInt(toNumber);
    return randNo;
  }

  /**
   * The method uses "IntegerDijkstraPathfinder" algorithm to find the
   * shortest path between the two given vertices.
   * @param graph is the model graph
   * @param source start vertex for the path
   * @param dest end vertex for the path
   * @return the shortest path between the "source" vertex and the "dest" vertex
   */
  public List<Transition> getShortestPath(InspectableGraph graph,
      Vertex source, Vertex dest)
  {
    List<Transition> path = new ArrayList<Transition>();
    PathFinder finder = new PathFinder();
    finder.execute(graph, source, dest);
    if (!finder.pathExists())
      return path;
    else {
      EdgeIterator itr = finder.reportPath();
      while (itr.hasNext()) {
        Edge e = itr.nextEdge();
        Object s = graph.origin(e).element();
        Object d = graph.destination(e).element();
        String action = (String) e.element();
        Transition trans = new Transition(s, action, d);
        path.add(trans);
      }
      return path;
    }
  }

  //Returns a list after cutting off part of the list (fromIndex to toIndex). 
  public List<Transition> getRefinedSequence(List<Transition> toRefine,
      int fromIndex, int toIndex)
  {
    Assert.assertNotNull(toRefine);
    Assert.assertTrue(fromIndex >= 0 && fromIndex < toRefine.size());
    Assert.assertTrue(toIndex >= 0 && toIndex <= toRefine.size());

    List<Transition> refined = new ArrayList<Transition>();
    refined.addAll(toRefine.subList(0, fromIndex));
    refined.addAll(toRefine.subList(toIndex, toRefine.size()));
    return refined;
  }

  //Returns a list after cutting off part of the list (fromIndex to toIndex) and replacing it with the "subList". 
  public List<Transition> getRefinedSequence(List<Transition> toRefine,
      List<Transition> subList, int fromIndex, int toIndex)
  {
    Assert.assertNotNull(toRefine);
    Assert.assertTrue(fromIndex >= 0 && fromIndex < toRefine.size());
    Assert.assertTrue(toIndex >= 0 && toIndex < toRefine.size());

    List<Transition> refined = new ArrayList<Transition>();
    //cut
    refined.addAll(toRefine.subList(0, fromIndex));

    refined.addAll(toRefine.subList((toIndex + 1), toRefine.size()));

    //add the new bit
    refined.addAll(fromIndex, subList);
    return refined;
  }

  //Returns a hashmap of each state with all its associated cycles. 
  public HashMap<String, ArrayList<Integer>> findAllSequenceCycles(
      List<Transition> sequence)
  {
    Assert.assertNotNull(sequence);

    HashMap<String, ArrayList<Integer>> table = new HashMap<String, ArrayList<Integer>>();

    for (int i = 0; i < sequence.size(); i++) {
      Transition t = sequence.get(i);
      Object startState = t.getStartState();
      addToHashTable(table, startState, i);
    }
    //deal with last transition if it has a self-loop transition
    Transition lastTrans = sequence.get(sequence.size() - 1);
    if (lastTrans.getStartState().equals(lastTrans.getEndState()))
      addToHashTable(table, lastTrans.getStartState(), sequence.size());
    return table;
  }

  public void addToHashTable(HashMap<String, ArrayList<Integer>> table,
      Object keyObject, int positionToAdd)
  {
    Assert.assertNotNull(keyObject);
    Assert.assertTrue(positionToAdd >= 0);

    ArrayList<Integer> value = new ArrayList<Integer>();
    if (table.containsKey(keyObject.toString()))
      value = table.get(keyObject);

    if (!value.contains(positionToAdd))
      value.add(positionToAdd);
    table.put(keyObject.toString(), value);
  }


  //a class extend the abstract class IntegerDijkstraPathfinder
  private class PathFinder extends IntegerDijkstraPathfinder
  {
    @Override
    protected int weight(Edge e)
    {
      return 0;
    }
  }

}
