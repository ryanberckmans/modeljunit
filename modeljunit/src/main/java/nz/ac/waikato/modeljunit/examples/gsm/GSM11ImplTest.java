package nz.ac.waikato.modeljunit.examples.gsm;

import java.util.Random;

import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.StopOnFailureListener;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import junit.framework.TestCase;

public class GSM11ImplTest extends TestCase
{
  private long seed = 1;//Tester.FIXEDSEED;
  private int numTests = 100 * 1000;
  
  protected void setUp() throws Exception
  {
    super.setUp();
  }

  public void testGSM11()
  {
    SimCard model = new SimCard(new SimCardAdaptor());
    RandomTester tester = new RandomTester(model);
    tester.setRandom(new Random(seed));
    System.out.println(numTests + " tests, with seed = " + seed);
    tester.setResetProbability(0.01);
    //GraphListener graph = tester.buildGraph(0);
    //tester.addListener("Verbose", new VerboseListener(tester.getModel()));
    tester.addListener(new StopOnFailureListener());
    tester.addCoverageMetric(new TransitionCoverage());
    tester.addCoverageMetric(new StateCoverage());
    int test = 0;
    for (; test < numTests; test++) {
    	tester.generate(1);
    }
    System.out.println("After " + test + " tests");// ", todo=" + graph.numTodo());
    tester.printCoverage();
  }

  /** In case we want to run this JUnit test from the command line. */
  public static void main(String[] args) throws Exception
  {
    GSM11ImplTest test = new GSM11ImplTest();
    if (args.length > 0) {
      int curr = 0;
      while (curr < args.length - 1) {
        if (args[curr].equals("--seed") && curr + 1 < args.length) {
          test.seed = Long.parseLong(args[curr + 1]);
          curr += 2;
        } else if (args[curr].equals("--numtests") && curr + 1 < args.length) {
          test.numTests = Integer.parseInt(args[curr + 1]);
          curr += 2;
        } else {
          break;
        }
      }
      if (curr != args.length) {
        System.out.println("Usage: --seed NNN --numtests NNNNNN");
        System.exit(1);
      }
    }
    test.setUp();
    test.testGSM11();
  }
}
