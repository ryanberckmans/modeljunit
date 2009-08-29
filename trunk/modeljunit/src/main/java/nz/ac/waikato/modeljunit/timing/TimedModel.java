
package nz.ac.waikato.modeljunit.timing;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmException;
import nz.ac.waikato.modeljunit.FsmModel;
import nz.ac.waikato.modeljunit.Model;
import nz.ac.waikato.modeljunit.TestFailureException;
import nz.ac.waikato.modeljunit.Transition;

import junit.framework.Assert;

/**
 * An extension of the model class which supports timed models.
 *
 * @author Scott Thompson
 */
public class TimedModel extends Model
{

  /** The seed that is used for the default Random object for timeouts. */
  public static final long TIMEOUT_SEED = 12345L;

  /** The Timeouts of the FSM. */
  private ArrayList<Field> timeouts_;

  /** The single Time annotation. */
  private Field time_;

  private String timeoutAction = null;

  /**
   * Random generator for internal use. Used to decide whether to do a time
   * increment or a timeout next.
   */
  private Random rand;

  /** The probability of timing-out, rather than choosing a random action. */
  private double timeoutProbability = 0.5;


  /**
   * Create a timed model from a real-time extended FSM model.
   *
   * @param model the real-time EFSM object.
   */
  public TimedModel(TimedFsmModel model)
  {
    super(model);
    rand = new Random(TIMEOUT_SEED);
  }

  /**
   * Sets the Random generator that is used to decide whether timeouts
   * should be taken.  This is typically set to the same Random object
   * that is used for other test generation decisions.
   *
   * @see nz.ac.waikato.modeljunit.Tester
   *
   * @param the non-null Random object that will be used from now on.
   */
  public void setRandom(Random rand)
  {
    if (rand == null) {
      throw new IllegalArgumentException("Random parameter must be non-null");
    }
    this.rand = rand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadModelClass(Class<? extends FsmModel> fsm)
  {
    super.loadModelClass(fsm);

    // check that fsm is actually a realtime model.
    try {
      fsm.asSubclass(TimedFsmModel.class);
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("not a TimedFsmModel: " + fsm);
    }

    if (timeouts_ == null) {
      timeouts_ = new ArrayList<Field>();
    }

    for (Field field : fsm.getFields()) {
      if (field.isAnnotationPresent(Time.class)) {
        if (time_ != null) {
          throw new FsmException("multiple @Time fields in model: " + fsm.getName());
        }
        if (field.getType() == int.class) {
          time_ = field;
        }
        else {
          printWarning("@Time field " + field.getName()
              + "should be of type int.");
        }
      }
      else if (field.isAnnotationPresent(Timeout.class)) {
        if (verifyTimeout(field, fsm)) {
          timeouts_.add(field);
        }
      }
    }
  }

  /**
   * Verifies that a Timeout is valid in a given FSM. It must be an integer and
   * the action name provided as an annotation must correspond to and Action
   * method.
   *
   * @param field the Timeout to verify
   * @param fsm the FSM the the Timeout exists in
   * @return true if the Timeout is valid, false otherwise
   */
  private boolean verifyTimeout(Field field, Class<?> fsm)
  {
    // Timeouts must be integers
    if (field.getType() != int.class) {
      printWarning("Timeout: " + field.getName()
          + "-Timeouts must be of type int");
      return false;
    }

    // Action name must correspond to an action method
    // TODO: relax this to allow it to be a non-action void method?
    //       Or allow the @Action to have a 'timeout-only' flag?
    String actionName = field.getAnnotation(Timeout.class).value();
    for (Method method : fsm.getMethods()) {
      if (method.isAnnotationPresent(Action.class)) {
        if (method.getName().equals(actionName)) {
          return true;
        }
      }
    }

    // no corresponding action
    printWarning("Timeout: " + field.getName()
        + "-No corresponding action was found");
    return false;
  }

  /**
   * The strengthens all guards to ensure that only timeout actions are
   * enabled after a timeout has fired.
   */
  @Override
  public int enabled(int index)
  {
    if (timeoutAction != null) {
      if (getActionName(index).equals(timeoutAction)) {
        return super.enabled(index);
      }
      else
        return 0;
    }
    return super.enabled(index);
  }

  @Override
  public void doReset(String reason)
  {
    super.doReset(reason);
    timeoutAction = null;
    fsmState_ = fsmModel_.getState();
  };

  @Override
  public boolean doAction(int index)
  {
    if (!isEnabled(index)) {
      return false;
    }

    if (timeoutAction != null) {
      // this assumes that there is only one timeout enabled?
      // TODO: generalize this to allow any timeout action to be chosen.
      assert getActionName(index).equals(timeoutAction);
      timeoutAction = null;
    }

    int startTime = getTime();
    Method m = fsmActions_.get(index);
    try {
      notifyStartAction(fsmState_, index, m.getName());
      m.invoke(fsmModel_, VOID_ARGS);
    }
    catch (InvocationTargetException ex) {
      String failmsg = "failure in action " + m.getName() + " from state "
          + this.fsmState_ + " due to " + ex.getCause();
      createTestFailure(failmsg, m.getName());
    }
    catch (IllegalAccessException ex) {
      Assert.fail("Model Error: Non-public actions? " + ex);
      return false;
    }

    Object newState = fsmModel_.getState();

    // now either increment the time or do the lowest timeout
    if (rand.nextDouble() > timeoutProbability) {
      // try to increment the time
      if (!incrementTime()) {
        // we cound't increment the time so we must have a timeout to do
        doLowestTimeout();
      }
    }
    else {
      // try to do the lowest timeout
      if (!doLowestTimeout()) {
        // no timeouts to do so increment the time
        incrementTime();
      }
    }
    if (!newState.equals(fsmModel_.getState())) {
      // changing the time caused a state change
      String failmsg = "Failure in action "
          + m.getName()
          + " from state "
          + fsmState_
          + ". Model state should not change "
          + "when changing the time. Use Timeouts to control time dependant states.";
      createTestFailure(failmsg, m.getName());
    }

    Transition done = new TimedTransition(startTime, fsmState_, m.getName(),
        newState);
    fsmSequence_.add(done);
    fsmState_ = newState;
    Assert.assertNotNull("Model Error: getState() must be non-null", fsmState_);

    notifyDoneTransition(index, done);
    return true;
  }

  /**
   * TODO: Change TestFailureException to FsmException in case where changing
   * time causes a state change
   * TODO: move this up to parent Model class and/or add a
   * TestFailureException(Model) constructor.
   *
   * @param failmsg
   * @param actionName
   */
  private void createTestFailure(String failmsg, String actionName)
  {
    TestFailureException failure = new TestFailureException(failmsg);
    failure.setActionName(actionName);
    failure.setModel(this.fsmModel_);
    failure.setModelName(this.getModelName());
    failure.setSequence(this.fsmSequence_);
    failure.setState(this.fsmState_);
    notifyFailure(failure);
  }

  /**
   * Gets the current Time of the model. Uses reflection to retrieve the Time
   * value in the FSM object.
   *
   * @return time or -1 if there is no Time field in the model.
   */
  public int getTime()
  {
    if (time_ != null) {
      try {
        return time_.getInt(getModel());
      }
      catch (Exception ex) {
        System.out.println(ex.getMessage());
      }
    }
    return -1;
  }

  /**
   * Sets the Time field in the timed FSM model.
   *
   * @param value
   */
  public void setTime(int value)
  {
    try {
      time_.setInt(getModel(), value);
    }
    catch (Exception ex) {
      throw new FsmException("error setting @Time field in model "
          + getModel().getClass().getName() + " - make sure it is public",
          ex);
    }
  }

  /**
   * Increment the current time by a model-specified amount.
   * The <code>getNextTimeIncrement</code> method of the real-time model
   * is used to choose the desired time advance, then this is truncated
   * to the first timeout if there are any timeouts enabled.
   *
   * @see TimedFsmModel
   *
   * @return true if the time was advanced, otherwise false
   */
  public boolean incrementTime()
  {
    Field lowest = getLowestTimeout();
    int currTime = getTime();
    int increment = ((TimedFsmModel) fsmModel_).getNextTimeIncrement();
    if (increment <= 0) {
      createTestFailure("Invalid time increment: " + increment
          + ". All time increments must be greater than zero.", "tick");
    }

    try {
      if (lowest == null) {
        // no timeouts set
        setTime(currTime + increment);
        return true;
      }
      else {
        int maxTime = lowest.getInt(getModel()) - 1;
        if (currTime + increment <= maxTime) {
          // The increment will not go past the lowest timeout
          setTime(currTime + increment);
          return true;
        }
        else {
          // incrementing time will take us past a timeout
          return false;
        }
      }
    }
    catch (IllegalAccessException ex) {
      throw new FsmException("error trying to increment time in model "
          + getModel().getClass().getName(), ex);
    }
  }

  /**
   * Gets the timeout that will expire next in the model. If no timeouts are set
   * then null is returned
   *
   * @return first timeout field, or null
   */
  public Field getLowestTimeout()
  {
    int lowestTimeout = Integer.MAX_VALUE;
    Field lowest = null;
    try {
      // find the lowest timeout first
      for (Field field : timeouts_) {
        int value = field.getInt(getModel());
        if (value > 0) {
          // timer is set
          if (value < lowestTimeout) {
            lowestTimeout = value;
            lowest = field;
          }
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return lowest;
  }

  /**
   * Gets the value of the lowest enabled timeout. Returns Integer.MIN_VALUE if
   * no timeouts are set.
   *
   * @return next timeout value.
   */
  public int getLowestTimeoutValue()
  {
    Field lowest = getLowestTimeout();
    if (lowest == null) {
      return Integer.MIN_VALUE;
    }
    else {
      try {
        return lowest.getInt(getModel());
      }
      catch (Exception e) {
        e.printStackTrace();
        return Integer.MIN_VALUE;
      }

    }
  }

  private boolean doLowestTimeout()
  {
    Field lowest = getLowestTimeout();

    if (lowest == null)
      return false;

    // set the time to the timeout value
    try {
      setTime(lowest.getInt(getModel()));
      timeoutAction = lowest.getAnnotation(Timeout.class).value();
    }
    catch (Exception e) {
      throw new FsmException("error getting/setting @Time field of model "
          + getModel().getClass().getName(), e);
    }
    return true;
  }

  public String getTimeoutName(int index)
  {
    if (timeouts_ == null || index >= timeouts_.size())
      return "";

    return timeouts_.get(index).getName();
  }

  /**
   * The probability of some timeout being taken.
   *
   * @return a probability in the range 0.0L to 1.0L.
   */
  public double getTimeoutProbability()
  {
    return timeoutProbability;
  }

  /**
   * Sets the probability of some timeout being taken when one or more
   * timeouts are enabled.
   *
   * @param value the new timeout probability
   */
  public void setTimeoutProbability(double value)
  {
    timeoutProbability = value;
  }
}
