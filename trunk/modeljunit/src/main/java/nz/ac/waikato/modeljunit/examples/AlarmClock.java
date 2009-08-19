
package nz.ac.waikato.modeljunit.examples;

import java.io.FileNotFoundException;
import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.timing.Time;
import nz.ac.waikato.modeljunit.timing.TimedFsmModel;
import nz.ac.waikato.modeljunit.timing.Timeout;

public class AlarmClock implements TimedFsmModel
{

  /** The time that the alarm will go off. (-1 == not set) */
  @Timeout("alarmOn")
  public int alarmTimer;

  /** The current time of the clock */
  @Time
  public int currentTime;

  /** Whether the alarm is currently sounding */
  private boolean alarmActive;

  private Random rand = new Random();

  @Override
  public Object getState()
  {
    if (alarmActive) {
      return "Alarm";
    }
    else if (alarmTimer == -1) {
      return "Not Set";
    }
    else {
      return "Alarm Set";
    }
  }

  @Override
  public void reset(boolean testing)
  {
    currentTime = 0;
    alarmTimer = -1;
    alarmActive = false;
  }

  /** 
   * Guard for the setAlarm action, only allows the action to occur
   * if the alarm is not currently set
   * @return true if the alarm is not set, otherwise false
   */
  public boolean setAlarmGuard()
  {
    return alarmTimer == -1 && !alarmActive;
  }

  @Action
  public void setAlarm()
  {
    //Set the alarm to 10 time units in the future
    alarmTimer = currentTime + 10;
    printAction("setAlarm");
  }

  public boolean cancelAlarmGuard()
  {
    return alarmTimer != -1;
  }

  @Action
  public void cancelAlarm()
  {
    alarmTimer = -1;
    printAction("cancelAlarm");
  }

  public boolean alarmOnGuard()
  {
    return alarmTimer == currentTime;
  }

  @Action
  public void alarmOn()
  {
    currentTime = alarmTimer;
    alarmActive = true;
    alarmTimer = -1;
    printAction("alarmOn");
  }

  public boolean alarmOffGuard()
  {
    return alarmActive;
  }

  @Action
  public void alarmOff()
  {
    alarmActive = false;
    printAction("alarmOff");
  }

  public int getNextTimeIncrement()
  {
    return 1 + rand.nextInt(2);
  }

  public void printAction(String action)
  {
    System.out.println("Time: " + currentTime + " Action: " + action
        + " New State: " + getState());
  }

  public static void main(String[] args)
  {
    Tester tester = new GreedyTester(new AlarmClock());
    tester.setRandom(new Random());
    tester.generate(100);
    GraphListener listener = tester.buildGraph();
    try {
      listener.printGraphDot("AlarmClock.dot");
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
