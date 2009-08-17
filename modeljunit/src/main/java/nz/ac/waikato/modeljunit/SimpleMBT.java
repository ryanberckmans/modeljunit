/**
 * An example of an extremely simple MBT tool.
 */
package nz.ac.waikato.modeljunit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nz.ac.waikato.modeljunit.examples.SimpleSet;

/**
 * This tool generates tests by doing a random walk of the
 * @Action methods in a given model.  You could also modify it
 * to use all public methods, or all methods that follow a certain
 * naming convention, if you prefer.
 *
 * @author marku
 *
 */
public class SimpleMBT {
  protected FsmModel model_;
  protected List<Method> methods_ = new ArrayList<Method>();
  public static final double RESET_PROBABILITY = 0.01;
  protected static final Object[] NO_ARGS = new Object[0];
  protected Random ran_ = new Random(Tester.FIXEDSEED);

  SimpleMBT(FsmModel model) {
    this.model_ = model;
    for (Method m : model.getClass().getMethods()) {
      if (m.getAnnotation(Action.class) != null) {
        methods_.add(m);
      }
    }
  }

  /** Generate a random test sequence of length 1.
   *  @return the name of the action done, or "reset".
   */
  public String generate() throws Exception {
    if (ran_.nextDouble() < RESET_PROBABILITY) {
      model_.reset(true);
      return "reset";
    } else {
      int i = ran_.nextInt(methods_.size());
      methods_.get(i).invoke(model_, NO_ARGS);
      return methods_.get(i).getName();
    }
  }

  public static void main(String[] args) throws Exception {
    FsmModel model = new SimpleSet();
    SimpleMBT tester = new SimpleMBT(model);
    for (int length = 0; length < 100; length++) {
      System.out.println(tester.generate() + ": " + model.getState());
    }
  }
}
