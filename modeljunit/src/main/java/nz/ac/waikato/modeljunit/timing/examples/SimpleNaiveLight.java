package nz.ac.waikato.modeljunit.timing.examples;

import java.io.FileNotFoundException;
import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;
import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.VerboseListener;


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
  }

  public boolean delayGuard() { return time < offTimeout; }
  @Action public void delay() {
    time += 1 + ran.nextInt(300);
    System.out.println("     time := " + time + " but offTimeout=" + offTimeout);
    if (offTimeout != 0 && time > offTimeout) {
      time = offTimeout; // don't go past the timeout
    }
  }

  /**
   * An example of generating tests from the above model.
   * @param args
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException {
    Tester tester = new RandomTester(new SimpleNaiveLight());
    tester.addListener(new VerboseListener());
    GraphListener graph = tester.buildGraph();
    tester.generate(50);
    graph.printGraphDot("SimpleNaiveLight.dot");
  }
}
