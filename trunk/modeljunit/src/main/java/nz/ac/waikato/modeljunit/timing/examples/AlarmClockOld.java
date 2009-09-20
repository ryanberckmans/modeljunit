package nz.ac.waikato.modeljunit.timing.examples;

import java.io.FileNotFoundException;
import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;
import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.Tester;

/**
 * This is the alarm clock model implemented without using the timing framework
 * 
 * @author Scott Thompson
 */
public class AlarmClockOld implements FsmModel
{

  /** The time that the alarm will go off. (-1 == not set) */
  private int alarmTime;

  /** The current time of the clock */
  private int currentTime;

  /** Whether the alarm is currently sounding */
  private boolean alarmActive;

  private Random rand = new Random();

  @Override
  public Object getState()
  {
    if (alarmActive) {
      return "Alarm";
    }
    else if (alarmTime == -1) {
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
    alarmTime = -1;
    alarmActive = false;
  }

  /**
   * Guard for the setAlarm action, only allows the action to occur if the alarm
   * is not currently set
   * 
   * @return true if the alarm is not set, otherwise false
   */
  public boolean setAlarmGuard() { return alarmTime == -1 && !alarmActive; }
  @Action public void setAlarm()
  {
    //Set the alarm to 10 time units in the future
    alarmTime = currentTime + 10;
    printAction("setAlarm");
    currentTime += getNextTimeIncrement();
  }

  public boolean cancelAlarmGuard() { return alarmTime != -1 && !alarmDue(); }
  @Action public void cancelAlarm()
  {
    alarmTime = -1;
    printAction("cancelAlarm");
    currentTime += getNextTimeIncrement();
  }

  public boolean alarmOnGuard() { return alarmDue(); }
  @Action public void alarmOn()
  {
    currentTime = alarmTime;
    alarmActive = true;
    alarmTime = -1;
    printAction("alarmOn");
    currentTime += getNextTimeIncrement();
  }

  public boolean alarmOffGuard() { return alarmActive; }
  @Action public void alarmOff()
  {
    alarmActive = false;
    printAction("alarmOff");
    currentTime += getNextTimeIncrement();
  }

  public boolean alarmDue()
  {
    //return true if alarm is set, expired and not active
    return alarmTime != -1 && alarmTime <= currentTime && !alarmActive;
  }

  public int getNextTimeIncrement()
  {
    return 1 + rand.nextInt(20);
  }

  public void printAction(String action)
  {
    System.out.println("Time: " + currentTime + " Action: " + action
        + " New State: " + getState());
  }

  public static void main(String[] args)
  {
    Tester tester = new GreedyTester(new AlarmClockOld());
    System.out.println("------------------------");
    tester.setRandom(new Random());
    tester.generate(100);
    GraphListener listener = tester.buildGraph();
    try {
      listener.printGraphDot("OriginalAlarmClock.dot");
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
