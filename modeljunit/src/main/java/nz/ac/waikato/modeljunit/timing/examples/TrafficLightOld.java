package nz.ac.waikato.modeljunit.timing.examples;

import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;
import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.Tester;

public class TrafficLightOld implements FsmModel
{

  /** Whether the pedestrian button has been pressed */
  private boolean buttonPressed;

  private STATE state;

  /**
   * The length of time that both lights are red in between changing who can use
   * the crossing
   */
  private static final int SAFETY_PERIOD = 5;

  /**
   * The minimum time that the light must stay green for cars. This ensures that
   * traffic can flow freely through the intersection without constantly being
   * interrupted by pedestrians.
   */
  private static final int MINIMUM_TRAFFIC_PERIOD = 20;

  /**
   * The length of time that the light stays green for pedestrians while they
   * cross.
   */
  private static final int CROSSING_TIME = 10;

  /**
   * This timer controls the minimum 20 second period in which the light must be
   * green for cars.When the timer expires the minimumWaitElapsed action will be
   * called
   */
  private int trafficFlowTimer = 0;

  /**
   * This timer controls the period in which the light is green for pedestrians
   * to cross.
   */
  private int crossingTimer = 0;

  /**
   * Timer to control the safety period where neither pedestrians or cars can
   * start crossing
   */
  private int safetyTimer = 0;

  /**
   * The current time of the model
   */
  private int now = 0;

  /**
   * Random generator for generating time increments.
   */
  private Random rand = new Random();


  public enum STATE {
    CARS_HOLD,   //period while the light is held green even if the button gets pressed
    CARS,        //regular state for allowing cars to move through
    WAIT_CARS,   //safety period after cars have been crossing
    PEDESTRIANS, //period where pedestrians cross
    WAIT_PEDS    //safety period after pedestrians cross
  }

  @Override
  public Object getState()
  {
    return state;
  }

  @Override
  public void reset(boolean testing)
  {
    now = 1;
    buttonPressed = false;
    crossingTimer = 0;
    safetyTimer = 0;

    state = STATE.CARS_HOLD;
    trafficFlowTimer = now + MINIMUM_TRAFFIC_PERIOD;

  }

  public boolean pedestrianArriveGuard()
  { return !trafficFlowTimedOut() && !safetyTimedOut(); }
  @Action public void pedestrianArrive()
  {
    if (state == STATE.PEDESTRIANS) {
      //they can walk right through
      return;
    }
    //In all other states they must press the button and wait
    buttonPressed = true;

    if (state == STATE.CARS) {
      state = STATE.WAIT_CARS;
      safetyTimer = now + SAFETY_PERIOD;
    }

    incrementTime();
  }

  /**
   * Guard for the safetyPeriodEnd action. We only want this action to occur
   * when the safetyTimer is set indicating that we are in a safety period
   * 
   * @return
   */
  public boolean finishSafetyPeriodGuard() { return safetyTimer > 0; }
  @Action public void finishSafetyPeriod()
  {
    //set the time to the exact finish of the safety period
    now = safetyTimer;

    //clear the timer
    safetyTimer = 0;
    if (state == STATE.WAIT_CARS) {
      //we now move to the state where pedestrians can cross
      state = STATE.PEDESTRIANS;
      //set the timer for when the light should finish
      crossingTimer = now + CROSSING_TIME;

      buttonPressed = false;
    }
    else {
      assert (state == STATE.WAIT_PEDS);
      //move to the state where the light must remain green for cars
      state = state.CARS_HOLD;
      //set the timer for when this period ends
      trafficFlowTimer = now + MINIMUM_TRAFFIC_PERIOD;
    }

    incrementTime();
  }

  public boolean minimumWaitElapsedGuard() { return trafficFlowTimer > 0; }
  @Action public void minimumWaitElapsed()
  {
    now = trafficFlowTimer;
    trafficFlowTimer = 0;

    if (buttonPressed) {
      //the pedestrian button has been pressed.
      //go to the safety period so we can let the pedestrians cross
      //in a moment
      state = STATE.WAIT_CARS;
      safetyTimer = now + SAFETY_PERIOD;
    }
    else {
      state = STATE.CARS;
    }

    incrementTime();
  }

  public boolean finishPedestrianPeriodGuard() { return crossingTimer > 0; }
  @Action public void finishPedestrianPeriod()
  {
    now = crossingTimer;
    crossingTimer = 0;
    state = STATE.WAIT_PEDS;
    safetyTimer = now + SAFETY_PERIOD;

    incrementTime();
  }

  public boolean crossingTimedOut()
  {
    return (crossingTimer > 0 && now >= crossingTimer);
  }

  public boolean safetyTimedOut()
  {
    return (safetyTimer > 0 && now >= safetyTimer);
  }

  public boolean trafficFlowTimedOut()
  {
    return (trafficFlowTimer > 0 && now >= trafficFlowTimer);
  }

  public static void main(String[] args)
  {
    Tester tester = new GreedyTester(new TrafficLightOld());
    tester.addListener("verbose");
    GraphListener graphListener = tester.buildGraph();
    try {
      graphListener.printGraphDot("OriginalTrafficLight.dot");
    }
    catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

  /**
   * Take random steps of between 1 and 10 time units (inclusive)
   */
  public void incrementTime()
  {
    now += rand.nextInt(10) + 1;
  }
}
