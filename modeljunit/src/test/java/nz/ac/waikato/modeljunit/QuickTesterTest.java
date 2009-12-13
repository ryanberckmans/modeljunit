package nz.ac.waikato.modeljunit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import nz.ac.waikato.modeljunit.coverage.CoverageMetric;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.examples.SimpleSet;

import org.junit.Before;
import org.junit.Test;

/**
 *  Unit tests for the quick tester.
 */
public class QuickTesterTest {
  protected QuickTester tester;
  protected List<Transition> path;
  protected CoverageMetric transitions;

  @Before
  public void setUp()   {
    tester = new QuickTester(new SimpleSet());
    transitions = tester.addCoverageMetric(new TransitionCoverage());
    path = new ArrayList<Transition>();
    tester.addListener(new AbstractListener() {
      private Object lastState = "";
      public String getName()
      {
        return "path listener";
      }
      @Override
      public void doneTransition(int action, Transition tr)
      {
        path.add(tr);
        lastState = tr.getEndState();
        //System.out.println(path.size());
      }
      @Override
      public void doneReset(String reason, boolean testing)
      {
        Object initial = model_.getCurrentState();
        path.add(new Transition(lastState, "reset", initial));
        lastState = initial;
      }
    }
    );
  }
  
  @Test
  public void testMaxDepth() {
    tester.setMaxDepth(1);
    assertThat(tester.getMaxDepth(), is(1));
    tester.setMaxDepth(10);
    assertThat(tester.getMaxDepth(), is(10));
  }
  
  @Test
  public void testResetProbability() {
    tester.setResetProbability(0.01);
    assertThat(tester.getResetProbability(), is(0.01));
    tester.setResetProbability(0.05);
    assertThat(tester.getResetProbability(), is(0.05));
  }
  
  @Test
  public void testQuickWalk1() {
    tester.generate(20);
    assertThat(path.size(), is(20));
    assertThat(path.get(0).toString(), is("(FF, removeS1, FF)"));
    assertThat(path.get(1).toString(), is("(FF, removeS2, FF)"));
    assertThat(path.get(2).toString(), is("(FF, addS2, FT)"));
    assertThat(path.get(3).toString(), is("(FT, addS2, FT)"));
    assertThat(path.get(4).toString(), is("(FT, addS1, TT)"));
    assertThat(path.get(5).toString(), is("(TT, removeS2, TF)"));
    assertThat(path.get(6).toString(), is("(TF, addS2, TT)"));
    assertThat(path.get(7).toString(), is("(TT, removeS1, FT)"));
    assertThat(path.get(8).toString(), is("(FT, removeS1, FT)"));
    assertThat(path.get(9).toString(), is("(FT, removeS2, FF)"));
    assertThat(path.get(10).toString(), is("(FF, reset, FF)"));
    assertThat(path.get(11).toString(), is("(FF, removeS1, FF)"));
    assertThat(path.get(12).toString(), is("(FF, removeS2, FF)"));
    assertThat(path.get(13).toString(), is("(FF, addS2, FT)"));
    assertThat(path.get(14).toString(), is("(FT, removeS1, FT)"));
    assertThat(path.get(15).toString(), is("(FT, addS2, FT)"));
    assertThat(path.get(16).toString(), is("(FT, addS1, TT)"));
    assertThat(path.get(17).toString(), is("(TT, addS2, TT)"));
    assertThat(path.get(18).toString(), is("(TT, removeS2, TF)"));
    assertThat(path.get(19).toString(), is("(TF, addS1, TF)"));
    assertThat(transitions.getCoverage(), is(12));
  }
  
  @Test
  public void testQuickWalk2() {
    
    tester.generate(40);
    assertThat(path.size(), is(40));
    assertThat(path.get(0).toString(), is("(FF, removeS1, FF)"));
    assertThat(path.get(1).toString(), is("(FF, removeS2, FF)"));
    assertThat(path.get(2).toString(), is("(FF, addS2, FT)"));
    assertThat(path.get(3).toString(), is("(FT, addS2, FT)"));
    assertThat(path.get(4).toString(), is("(FT, addS1, TT)"));
    assertThat(path.get(5).toString(), is("(TT, removeS2, TF)"));
    assertThat(path.get(6).toString(), is("(TF, addS2, TT)"));
    assertThat(path.get(7).toString(), is("(TT, removeS1, FT)"));
    assertThat(path.get(8).toString(), is("(FT, removeS1, FT)"));
    assertThat(path.get(9).toString(), is("(FT, removeS2, FF)"));
    assertThat(path.get(10).toString(), is("(FF, reset, FF)"));
    assertThat(path.get(11).toString(), is("(FF, removeS1, FF)"));
    assertThat(path.get(12).toString(), is("(FF, removeS2, FF)"));
    assertThat(path.get(13).toString(), is("(FF, addS2, FT)"));
    assertThat(path.get(14).toString(), is("(FT, removeS1, FT)"));
    assertThat(path.get(15).toString(), is("(FT, addS2, FT)"));
    assertThat(path.get(16).toString(), is("(FT, addS1, TT)"));
    assertThat(path.get(17).toString(), is("(TT, addS2, TT)"));
    assertThat(path.get(18).toString(), is("(TT, removeS2, TF)"));
    assertThat(path.get(19).toString(), is("(TF, addS1, TF)"));
    assertThat(path.get(20).toString(), is("(TF, removeS1, FF)"));
    assertThat(path.get(21).toString(), is("(FF, reset, FF)"));
    assertThat(path.get(22).toString(), is("(FF, removeS1, FF)"));
    assertThat(path.get(23).toString(), is("(FF, removeS2, FF)"));
    assertThat(path.get(24).toString(), is("(FF, addS2, FT)"));
    assertThat(path.get(25).toString(), is("(FT, removeS1, FT)"));
    assertThat(path.get(26).toString(), is("(FT, addS2, FT)"));
    assertThat(path.get(27).toString(), is("(FT, addS1, TT)"));
    assertThat(path.get(28).toString(), is("(TT, addS2, TT)"));
    assertThat(path.get(29).toString(), is("(TT, removeS2, TF)"));
    assertThat(path.get(30).toString(), is("(TF, addS2, TT)"));
    assertThat(path.get(31).toString(), is("(TT, addS1, TT)"));
    assertThat(path.get(32).toString(), is("(TT, reset, FF)"));
    assertThat(path.get(33).toString(), is("(FF, removeS1, FF)"));
    assertThat(path.get(34).toString(), is("(FF, removeS2, FF)"));
    assertThat(path.get(35).toString(), is("(FF, addS2, FT)"));
    assertThat(path.get(36).toString(), is("(FT, removeS2, FF)"));
    assertThat(path.get(37).toString(), is("(FF, addS1, TF)"));
    assertThat(path.get(38).toString(), is("(TF, removeS2, TF)"));
    assertThat(path.get(39).toString(), is("(TF, reset, FF)"));
    assertThat(transitions.getCoverage(), is(16));
  }
}
