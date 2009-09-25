
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
 * An extension of the Model class that supports timed models.
 *
 * @author Scott Thompson
 */
public class TimedModel extends Model
{

  /** The seed that is used for the default Random object for timeouts. */
  public static final long TIMEOUT_SEED = 12345L;

  /** The single Time annotation. */
  private Field time_;

  /** The Timeouts of the FSM. */
  private ArrayList<Field> timeouts_;

  /** When not equal to -1, this is the timeout we are about to take. */
  private int chosenTimeout_;
  
  /** When chosenTimeout_ != -1, this is the number of its action. */
  private int chosenTimeoutAction_;

  /**
   * Random generator for internal use. Used to decide whether to do a time
   * increment or a timeout next.
   */
  private Random rand;

  /** The probability of timing-out, rather than choosing a random action. */
  private double timeoutProbability = 0.25;


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
          throw new FsmException("@Time field " + field.getName()
              + " must be of type int.");
        }
      }
      else if (field.isAnnotationPresent(Timeout.class)) {
        if (verifyTimeout(field, fsm)) {
          timeouts_.add(field);
        }
      }
    }
    if (time_ == null) {
      throw new FsmException("No @Time field in TimedFsmModel " + fsm.getName());
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
      throw new FsmException("Timeout field " + field.getName()
          + "-Timeouts must be of type int");
      //return false;
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
    throw new FsmException("Timeout: " + field.getName()
        + "-No corresponding action was found");
    //return false;
  }

  /**
   * This strengthens all guards so that after a timeout has fired,
   * all other guards are disabled.
   */
  @Override
  public int enabled(int index)
  {
    if (chosenTimeout_ >= 0 && index != chosenTimeoutAction_) {
        return 0;
    }
    return super.enabled(index);
  }

  @Override
  public void doReset(String reason)
  {
    // reset @Time and @Timeout fields.
    setTime(0);
    for (int i = 0; i < timeouts_.size(); i++) {
      setTimeoutValue(i, TimedFsmModel.TIMEOUT_DISABLED);
    }
    super.doReset(reason);
    chosenTimeout_ = -1;
    chosenTimeoutAction_ = -1;
    fsmState_ = fsmModel_.getState();
  };

  @Override
  public boolean doAction(int index)
  {
    if (!isEnabled(index)) {
      return false;
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

    if (chosenTimeout_ >= 0) {
      // the chosen timeout was the only one whose guard was enabled.
      assert index == chosenTimeoutAction_;
      // reset that timeout if the action has not set it again.
      if (getTimeoutValue(chosenTimeout_) <= startTime) {
        setTimeoutValue(chosenTimeout_, TimedFsmModel.TIMEOUT_DISABLED);
      }
      chosenTimeout_ = -1;
      chosenTimeoutAction_ = -1;
    }

    Object newState = fsmModel_.getState();

    // now either increment the time or do the lowest timeout
    if (rand.nextDouble() > timeoutProbability) {
      // try to increment the time
      if (!incrementTime()) {
        // we could not increment the time, so we must have a timeout to do
        chooseLowestTimeout();
      }
    }
    else {
      // try to do the lowest timeout
      if (!chooseLowestTimeout()) {
        // no timeouts to do, so increment the time
        incrementTime();
      }
    }
    // reset ALL timeouts that are now in the past.
    int now = getTime();
    for (int i = 0; i < timeouts_.size(); i++) {
      int timeout = getTimeoutValue(i);
      if (0 <= timeout && timeout < now) {
        setTimeoutValue(i, TimedFsmModel.TIMEOUT_DISABLED);
      }
    }
    if (!newState.equals(fsmModel_.getState())) {
      // changing the time caused a state change
      String failmsg = "Model problem detected after action "
          + m.getName()
          + " - state changed from "
          + fsmState_ + " to " + fsmModel_.getState()
          + " when only time changed.  "
          + "Use Timeouts to control time dependant states.";
      throw new FsmException(failmsg);
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
   * @param value Must be zero or greater.
   */
  public void setTime(int value)
  {
    if (value < 0) {
      throw new IllegalArgumentException("setTime(" + value + ") is illegal");
    }
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
    int lowest = getLowestTimeout();
    int currTime = getTime();
    int increment = ((TimedFsmModel) fsmModel_).getNextTimeIncrement(rand);
    if (increment <= 0) {
      createTestFailure("Invalid time increment: " + increment
          + ". All time increments must be greater than zero.",
          "getNextTimeIncrement");
    }

    if (lowest < 0) {
      // no timeouts set
      setTime(currTime + increment);
      return true;
    }
    else {
      int limit = getTimeoutValue(lowest);
      if (currTime + increment < limit) {
        // The increment will not go past the lowest timeout
        setTime(currTime + increment);
        return true;
      }
      else {
        // incrementing time will take us up to or past a timeout
        return false;
      }
    }
  }

  /**
   * Gets the timeout that will expire next in the model.
   * If no timeouts are set then -1 is returned.
   * If more than one timeout will expire at exactly the same time,
   * one is chosen arbitrarily (in some fixed priority order for each model).
   *
   * @return the number of the first enabled timeout, else -1.
   */
  public int getLowestTimeout()
  {
    int now = getTime();
    int lowestTimeout = Integer.MAX_VALUE;
    int lowest = -1;
    try {
      // find the lowest timeout first
      for (int i = 0; i < timeouts_.size(); i++) {
        Field field = timeouts_.get(i);
        int value = field.getInt(getModel());
        if (value > 0 && value >= now) {  // TODO: allow timeouts at time 0?
          // TODO: check its guard as well.
          // timer is set
          if (value < lowestTimeout) {
            lowestTimeout = value;
            lowest = i;
          }
        }
      }
    }
    catch (IllegalAccessException ex) {
      throw new FsmException("@Timeout fields must be public");
    }
    return lowest;
  }

  /**
   * Gets the value of the lowest enabled timeout. Returns TIMEOUT_DISABLED if
   * no timeouts are set.
   *
   * @return next timeout value.
   */
  public int getLowestTimeoutValue()
  {
    int lowest = getLowestTimeout();
    if (lowest < 0) {
      return TimedFsmModel.TIMEOUT_DISABLED;
    }
    else {
      try {
        return timeouts_.get(lowest).getInt(getModel());
      }
      catch (IllegalAccessException e) {
        return TimedFsmModel.TIMEOUT_DISABLED;  // or we could throw exceptions
      }
    }
  }

  /**
   * Sets the model time variable to the first enabled timeout.
   * If several timeouts are enabled at exactly the same time,
   * one of them is chosen non-deterministically (using a priority
   * order that is arbitrary, but fixed for each model). 
   *
   * @return true if some timeout has been chosen.
   */
  private boolean chooseLowestTimeout()
  {
    int lowest = getLowestTimeout();

    if (lowest < 0) {
      return false;
    }

    // set the time field to the timeout value
    setTime(getTimeoutValue(lowest));
    chosenTimeout_ = lowest;
    chosenTimeoutAction_ = getActionNumber(getTimeoutAction(lowest));
    return true;
  }

  /** @return the number of @Timeout fields in the timed model. */
  public int getNumTimeouts() {
    return timeouts_.size();
  }

  /**
   * @param timeoutIndex a number from 0 up to getNumTimeouts() - 1.
   * @return the name of a @Timeout field.
   */
  public String getTimeoutName(int timeoutIndex)
  {
    if (timeouts_ == null || timeoutIndex >= timeouts_.size())
      return "";

    return timeouts_.get(timeoutIndex).getName();
  }

  /**
   * Gets the name of the action that will be taken when this timeout expires.
   * @param timeoutIndex 0 .. getNumTimeouts() - 1.
   * @return the name of the associated @Action method.
   */
  public String getTimeoutAction(int timeoutIndex) {
    Timeout to = timeouts_.get(timeoutIndex).getAnnotation(Timeout.class);
    return to.value();
  }

  /**
   * @param timeoutIndex a number from 0 up to getNumTimeouts() - 1.
   * @return the current value of the @Timeout field.
   */
  public int getTimeoutValue(int timeoutIndex)
  {
    Field field = timeouts_.get(timeoutIndex);
    try {
      return field.getInt(getModel());
    }
    catch (IllegalArgumentException e) {
      throw new FsmException("bad @Timeout field: " + field.getName(), e);
    }
    catch (IllegalAccessException e) {
      throw new FsmException("cannot read @Timeout field: " + field.getName(), e);
    }
  }

  /**
   * @param timeoutIndex a number from 0 up to getNumTimeouts() - 1.
   * @param the new value of the @Timeout field.
   */
  public void setTimeoutValue(int timeoutIndex, int value)
  {
    Field field = timeouts_.get(timeoutIndex);
    try {
      field.setInt(getModel(), value);
    }
    catch (IllegalArgumentException e) {
      throw new FsmException("bad @Timeout field: " + field.getName(), e);
    }
    catch (IllegalAccessException e) {
      throw new FsmException("cannot write to @Timeout field: " + field.getName(), e);
    }
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
