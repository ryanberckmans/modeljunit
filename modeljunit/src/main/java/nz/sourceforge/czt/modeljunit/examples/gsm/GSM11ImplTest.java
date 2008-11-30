package nz.ac.waikato.modeljunit.examples.gsm;

import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.VerboseListener;
import nz.ac.waikato.modeljunit.coverage.CoverageMetric;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import junit.framework.TestCase;

public class GSM11ImplTest extends TestCase
{

  protected void setUp() throws Exception
  {
    super.setUp();
  }

  public void testGSM11()
  {
    SimCard model = new SimCard(new SimCardAdaptor());
    RandomTester tester = new RandomTester(model);
    tester.setResetProbability(0.01);
    //tester.buildGraph(1000000);
    //tester.addListener("Verbose", new VerboseListener(tester.getModel()));
    CoverageMetric trans = new TransitionCoverage();
    tester.addListener(trans);
    tester.generate(10000);
    System.out.println("Transition coverage = "+trans.toString());
  }

  /** In case we want to run this JUnit test from the command line. */
  public static void main(String[] args) throws Exception
  {
    GSM11ImplTest test = new GSM11ImplTest();
    test.setUp();
    test.testGSM11();
  }
}
