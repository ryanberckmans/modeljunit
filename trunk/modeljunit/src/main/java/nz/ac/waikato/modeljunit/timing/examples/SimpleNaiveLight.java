package nz.ac.waikato.modeljunit.timing.examples;

import java.io.FileNotFoundException;
import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;
import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.VerboseListener;
import nz.ac.waikato.modeljunit.coverage.CoverageMetric;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;


/**
 * This models a simple light switch that turns off automatically
 * after 60 seconds.
 * 
 * The model is written in an old-fashioned way, using just the
 * non-real-time features of ModelJUnit, so it illustrates how
 * real-time models can be written even if the framework did not
 * explicitly support time or timeouts.  For an easier and better
 * way of specifying this real-time system, see the
 * <code>SimpleTimedLight</code> model.
 * 
 * @author marku
 */
@Deprecated
public class SimpleNaiveLight implements FsmModel
{
  private Random ran = new Random(12345L);
  private boolean on = false;
  private int time = 0;
  private int offTimeout = -1;

  @Override
  public Object getState() {
    return new TimedState(on ? "On" : "Off", time);
  }

  @Override public void reset(boolean testing) {
    on = false;
    time = 0;
    offTimeout = -1;
  }

  public boolean pushButtonGuard() { return time != offTimeout; }
  @Action public void pushButton() {
    on = true;  // turn the light on
    offTimeout = time + 60;
    time += 3;
  }

  public boolean autoOffGuard() { return time == offTimeout; }
  @Action public void autoOff() {
    on = false;
    time += 1;
    offTimeout = -1;
  }

  public boolean delayGuard() { return time != offTimeout; }
  @Action public void delay() {
    time += 1 + ran.nextInt(60);
    //System.out.println("     time := " + time + " but offTimeout=" + offTimeout);
    if (offTimeout >= 0 && time > offTimeout) {
      time = offTimeout; // don't go past the timeout
    }
  }

  /**
   * An example of generating tests from the above model.
   * @param args
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException {
    final int experiments = 100;
    SimpleNaiveLight fsm = new SimpleNaiveLight();
    Tester tester = new RandomTester(fsm);
    //tester.addListener(new VerboseListener());
    //tester.addListener(new VerboseListener());
    GraphListener graph = tester.buildGraph();
    System.out.println("FSM has " + graph.getGraph().numVertices() + " states and "
        + graph.getGraph().numEdges() + " transitions");
    CoverageMetric metric = tester.addCoverageMetric(new TransitionCoverage());
    for (double prob = 0.1; prob < 0.99; prob += 0.1) {
      double totalSteps = 0.0;
      int totalTime = 0; // sum of all the totalTimes.
      for (int seed = 0; seed < experiments; seed++) {
        int testSeqTime = 0;
        int lastTime = 0; // the time after the last transition
        metric.clear();
        tester.reset();
        tester.setRandom(new Random(seed));
        int count = 0;
        while (metric.getPercentage() < 100.0) {
          tester.generate(1);
          if (fsm.time > lastTime) {
            lastTime = fsm.time;
          } else {
            testSeqTime += lastTime;
            //System.out.println("added " + lastTime + " secs");
            lastTime = 0;
          }
          count++;
        }
        testSeqTime += lastTime;
        //System.out.println("finally added " + lastTime + " secs");
        totalSteps += count;
        totalTime += testSeqTime;
        //System.out.println("Seed=" + seed + " steps=" + count + " time=" + testSeqTime);
      }
      System.out.println(String.format("%.2f", prob) + "," + totalSteps / experiments
          + "," + (double) totalTime / experiments);
    }
    System.out.println("FSM has " + graph.getGraph().numVertices() + " states and "
        + graph.getGraph().numEdges() + " transitions");
  }
}
