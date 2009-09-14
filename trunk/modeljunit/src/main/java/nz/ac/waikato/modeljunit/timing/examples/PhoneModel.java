package nz.ac.waikato.modeljunit.timing.examples;

import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.timing.Time;
import nz.ac.waikato.modeljunit.timing.TimedFsmModel;
import nz.ac.waikato.modeljunit.timing.Timeout;

/**
 * A simple example class to test the TimedModel class.
 * In this model keys can be pressed at any time. If there
 * is a delay of more than 10 seconds between key presses then the system
 * attempts to dial the number. If more than 7 digits have been entered then it
 * can call the number, otherwise dialing fails. If a call is successfully
 * started then exactly 60 seconds later the phone will be hung up.
 * 
 * @author Scott Thompson
 */
public class PhoneModel implements TimedFsmModel
{
  /*The current time*/
  @Time
  public int now;

  @Timeout("dial")
  public int dialTimeout = 0;

  @Timeout("hangUp")
  public int hangUpTimeout = 0;

  /*The number of key presses*/
  private int keysPressed = 0;

  /*Whether the phone is calling someone.*/
  private boolean calling = false;

  public PhoneModel()
  {
  }

  public boolean pressKeyGuard()
  {
    return !calling;
  }

  /**
   * Action for pressing a key. Action is enabled as long as the phone is not
   * currently calling someone.
   */
  @Action
  public void pressKey()
  {
    keysPressed++;
    dialTimeout = now + 10; //timeout set for 10 seconds in the future
  }

  public boolean dialGuard()
  {
    return !calling;
  }

  /**
   * Action for dialling the number.
   */
  @Action
  public void dial()
  {
    if (keysPressed >= 7) {
      calling = true;
      //set the hangUpTimeout to expire in 60 seconds
      hangUpTimeout = now + 60;
    }
    else {
      keysPressed = 0; //reset the phone
    }
  }

  /**
   * Guard for hanging up a call. This is only ever done through a timeout so
   * the guard returns false unless the timer for hanging up has expired.
   * 
   * @return
   */
  public boolean hangUpGuard()
  {
    return calling && hangUpTimeout == now;
  }

  @Action
  public void hangUp()
  {
    calling = false;
    keysPressed = 0;
    hangUpTimeout = -1;
  }

  @Override
  public String getState()
  {
    if (keysPressed == 0) {
      return "IDLE";
    }
    else {
      if (calling)
        return "CALLING";
      else
        return "DIALING";
    }

  }

  @Override
  public void reset(boolean testing)
  {
    keysPressed = 0;
    dialTimeout = 0;

  }

  @Override
  public int getNextTimeIncrement(Random ran)
  {
    // Constant increments of two time units
    return 2;
  }
}
