package nz.ac.waikato.modeljunit.timing.examples;

import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.VerboseListener;
import nz.ac.waikato.modeljunit.timing.Time;
import nz.ac.waikato.modeljunit.timing.TimedFsmModel;
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
  public int getNextTimeIncrement(Random ran)
  {
    return 1 + ran.nextInt(200);
  }

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    Tester tester = new RandomTester(new SimpleTimedLight());
    tester.addListener(new VerboseListener());
    tester.generate(20);
  }
}
