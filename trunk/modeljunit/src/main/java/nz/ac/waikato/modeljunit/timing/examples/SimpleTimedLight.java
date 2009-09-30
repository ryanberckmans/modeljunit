package nz.ac.waikato.modeljunit.timing.examples;

import java.io.FileNotFoundException;
import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.VerboseListener;
import nz.ac.waikato.modeljunit.coverage.CoverageMetric;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.timing.Time;
import nz.ac.waikato.modeljunit.timing.TimedFsmModel;
import nz.ac.waikato.modeljunit.timing.TimedModel;
import nz.ac.waikato.modeljunit.timing.Timeout;

/**
 * This models a simple light switch that turns off automatically
 * after 60 seconds.
 * 
 * The model is written using the real-time features of ModelJUnit,
 * and illustrates the recommended way of specifying a simple real-time
 * system.
 * 
 * @author marku
 */
public class SimpleTimedLight implements TimedFsmModel
{
  private boolean on = false;
  @Time public int time = 0;
  @Timeout("autoOff") public int offTimeout;

  @Override
  public Object getState() {
    return on ? "On" : "Off"; //new TimedState(on ? "On" : "Off", time);
  }

  @Override public void reset(boolean testing) {
    on = false;
  }

  @Action public void pushButton() {
    on = true;  // turn the light on
    offTimeout = time + 60; // enable the timeout
  }

  public boolean autoOffGuard() { return time == offTimeout; }
  @Action public void autoOff() {
    on = false;
  }

  @Override
  public int getNextTimeIncrement(Random ran) {
    return 1 + ran.nextInt(60);
  }

  /**
   * An example of generating tests from the above model.
   * @param args
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException
  {
    final int experiments = 100;
    TimedModel model = new TimedModel(new SimpleTimedLight());
    Tester tester = new RandomTester(model);
    //tester.addListener(new VerboseListener());
    double origProb = model.getTimeoutProbability();
    model.setTimeoutProbability(0.3); // while exploring the FSM
    GraphListener graph = tester.buildGraph();
    model.setTimeoutProbability(origProb);
    graph.printGraphDot("SimpleTimedLight.dot");
    System.out.println("FSM has " + graph.getGraph().numVertices() + " states and "
        + graph.getGraph().numEdges() + " transitions");
    CoverageMetric metric = tester.addCoverageMetric(new TransitionCoverage());
    for (double prob = 0.1; prob < 0.99; prob += 0.1) {
      model.setTimeoutProbability(prob);
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
          if (model.getTime() > lastTime) {
            lastTime = model.getTime();
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
