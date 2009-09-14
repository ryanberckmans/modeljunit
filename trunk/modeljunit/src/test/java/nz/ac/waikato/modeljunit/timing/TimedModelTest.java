
package nz.ac.waikato.modeljunit.timing;

import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmException;
import nz.ac.waikato.modeljunit.timing.examples.PhoneModel;

public class TimedModelTest
{

  @Test
  public void testLoadModel()
  {
    TimedModel model = new TimedModel(new PhoneModel());

    //make sure the Time and 2 Timeouts were read properly
    assertEquals(0, model.getTime());
    assertEquals("dialTimeout", model.getTimeoutName(0));
    assertEquals("hangUpTimeout", model.getTimeoutName(1));
  }

  @Test(expected = FsmException.class)
  public void testLoadBadModel()
  {
    new TimedModel(new MissingTimeFSM());
  }

  @Test(expected = FsmException.class)
  public void testInvalidTimeouts()
  {
    new TimedModel(new BogusFSM());
  }

  @Test
  public void testReset()
  {
    //TODO
  }

  @Test
  public void testIncrementTime()
  {
    SimpleTimedFSM simpleFSM = new SimpleTimedFSM();
    TimedModel model = new TimedModel(simpleFSM);

    assertEquals(0, model.getTime());
    simpleFSM.action1Timer = 55;
    simpleFSM.action2Timer = 60;
    simpleFSM.action3Timer = 65;

    assertTrue(model.incrementTime());
    assertTrue(model.getTime() >= 1);
    assertTrue(model.getTime() <= 10);

    model.setTime(50);
    if (model.incrementTime()) {
      //time must be less than the timeout value
      assertTrue(model.getTime() >= 51);
      assertTrue(model.getTime() < 55);
    }

    model.setTime(54);
    assertFalse(model.incrementTime());
    assertEquals(54, model.getTime());

  }

  @Test
  public void testGetTime()
  {
    PhoneModel phoneFSM = new PhoneModel();
    TimedModel model = new TimedModel(phoneFSM);

    assertEquals(0, phoneFSM.now);
    assertEquals(0, model.getTime());

    phoneFSM.now = 10;
    assertEquals(10, phoneFSM.now);
    assertEquals(10, model.getTime());

    model.setTime(11);
    assertEquals(11, model.getTime());
    assertEquals(model.getTime(), phoneFSM.now);
  }

  @Test
  public void testTimeoutsBasic()
  {
    SimpleTimedFSM FSM = new SimpleTimedFSM();
    TimedModel model = new TimedModel(FSM);

    assertEquals(5, FSM.action1Timer);
    assertEquals(0, FSM.now);

    assertTrue(model.doAction(model.getActionNumber("action3")));
    assertTrue(FSM.getState().equals("3"));

    assertFalse(FSM.now > 5);

    if (FSM.now == 5) {
      //must do timeout
      assertFalse(model.doAction(model.getActionNumber("action2")));
      assertFalse(model.doAction(model.getActionNumber("action3")));
      assertTrue(model.doAction(model.getActionNumber("action1")));

      assertTrue(FSM.getState().equals("31"));
    }

    //now set it up so two timeouts are at same time and we are timed out
    FSM.action1Timer = 20;
    FSM.action2Timer = 20;
    FSM.now = 19;
    assertTrue(model.doAction(model.getActionNumber("action3")));
    assertEquals(FSM.action1Timer, FSM.now);

    //action1 should occur first because it is declared first
    assertFalse(model.doAction(model.getActionNumber("action2")));
    assertTrue(model.doAction(model.getActionNumber("action1")));
    assertEquals(FSM.action2Timer, FSM.now);
    assertFalse(model.doAction(model.getActionNumber("action1")));
    assertTrue(model.doAction(model.getActionNumber("action2")));
    assertTrue(FSM.now > 20);
  }

  @Test
  public void testGetLowestTimeout()
  {
    SimpleTimedFSM simpleFSM = new SimpleTimedFSM();
    TimedModel model = new TimedModel(simpleFSM);

    simpleFSM.action1Timer = 20;
    simpleFSM.action2Timer = 10;
    simpleFSM.action3Timer = 15;

    assertEquals("action2Timer", model.getLowestTimeout().getName());

    simpleFSM.action1Timer = 0;
    simpleFSM.action2Timer = 0;
    simpleFSM.action3Timer = 0;

    assertNull(model.getLowestTimeout());
  }
}


/**
 * Simple FSM for testing. Note: with the current implementation of this class
 * it actually has a an infinite number of states. I just wanted a class where I
 * could see exactly what the sequence of states has been.
 *
 * @author Scott
 */
class SimpleTimedFSM implements TimedFsmModel
{

  private String state = "";

  private Random rand = new Random(12345);

  @Time public int now;

  @Timeout("action1") public int action1Timer;

  @Timeout("action2") public int action2Timer;

  @Timeout("action3") public int action3Timer;

  @Action public void action1()
  {
    state += "1";
    action1Timer = -1;
  }

  @Action public void action2()
  {
    state += "2";
    action2Timer = -1;
  }

  @Action public void action3()
  {
    state += "3";
    action3Timer += 10;
  }

  @Override public Object getState()
  {
    return state;
  }

  @Override public void reset(boolean testing)
  {
    action1Timer = 5;
    action2Timer = 30;
    action3Timer = 10;
    state = "";
  }

  @Override public int getNextTimeIncrement(Random ran)
  {
    return 1 + rand.nextInt(10);
  }

}

/** Claims to be a timed model, but has no @Time field. */
class MissingTimeFSM implements TimedFsmModel
{
  // no @Time field

  @Override
  public int getNextTimeIncrement(Random ran)
  {
    return 1;
  }

  @Override
  public Object getState()
  {
    return "s0";
  }

  @Action public void action() {
  }

  @Override
  public void reset(boolean testing)
  {
  }
}

class BogusFSM implements TimedFsmModel
{

  private String state = "";

  @Timeout("")
  public int t1;

  @Timeout("action2")
  protected int t2;

  @Timeout("action3")
  public float t3;

  @Override
  public int getNextTimeIncrement(Random ran)
  {
    return 0;
  }

  @Override
  public Object getState()
  {
    return state;
  }

  @Override
  public void reset(boolean testing)
  {
    // TODO Auto-generated method stub

  }

  @Action
  public void action2()
  {
  }

  @Action
  public void action3()
  {
  }
}
