package nz.ac.waikato.modeljunit.examples.gsm;

import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
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
    RandomTester tester = new GreedyTester(model); //new RandomTester(model);
    tester.setResetProbability(0.01);
    GraphListener graph = tester.buildGraph(100);
    //tester.addListener("Verbose", new VerboseListener(tester.getModel()));
    tester.addCoverageMetric(new TransitionCoverage());
    tester.addCoverageMetric(new StateCoverage());
    for (int m = 1; m < 100; m++) {
    	tester.generate(1 * 100 * 1000);
    	System.out.println("\n" + (m * 100) + "K todo=" + graph.numTodo());
        tester.printCoverage();
    }
  }

  /** In case we want to run this JUnit test from the command line. */
  public static void main(String[] args) throws Exception
  {
    GSM11ImplTest test = new GSM11ImplTest();
    test.setUp();
    test.testGSM11();
  }
}
