
package nz.ac.waikato.modeljunit.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;
import nz.ac.waikato.modeljunit.Model;
import nz.ac.waikato.modeljunit.QuickTester;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.VerboseListener;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.CoverageHistory;
import nz.ac.waikato.modeljunit.coverage.CoverageMetric;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;
import nz.ac.waikato.modeljunit.gui.visualisaton.VisualisationListener;


/*
 * TODO: This class plays several roles, which need to be separated.
 * 1. Loading the FsmModel class and keeping track of some statistics.
 *    (eg. m_arrayMethod).
 * 2. Recording (statically!) the current test generation algorithm.
 * 3. Running the automatic test generation.
 */
public class TestExeModel
{
  /** There are four coverages, state, transition, transition pair and action.
   *  TODO: remove this constant and make the COVERAGE_MATRIX extensible.
   */
  public static final int COVERAGE_NUM = 4;

  public static final String[] COVERAGE_MATRIX = {"State coverage",
      "Transition coverage", "Transition pair coverage", "Action coverage"};

  /** TODO: move into a configuration/Parameter class. */
  private static int m_nWalkLength;

  public static void setWalkLength(int length)
  {
    m_nWalkLength = length;
  }

  public static int getWalkLength()
  {
    return m_nWalkLength;
  }

  private static ClassLoader m_modelClassLoader;
  
  //-----------------------Run the test------------------------
  // private static Class<FsmModel> m_modelClass;
  private static Class<?> m_modelClass;

  private static FsmModel m_modelObject;

  public static Class<?> getModelClass()
  {
    return m_modelClass;
  }

  public static FsmModel getModelObject()
  {
    return m_modelObject;
  }

  public static boolean isModelLoaded()
  {
    if (m_modelClass == null || m_modelObject == null)
      return false;
    return true;
  }

  /**
   * If user loaded an invalid model class, the model class and model object
   * have to be reset to null.
   * */
  public static void resetModelToNull()
  {
    m_modelClass = null;
    m_modelObject = null;
  }

  /** Tries to load an instance of the current model class from a .class file.
   * Before this is called, the class loader must be set.
   *
   * TODO: move this to a more appropriate class.
   *
   * @return true iff successful load with no errors
   */
  public static boolean loadModelClassFromFile()
  {
    assert m_modelClassLoader != null;
    try {
      m_modelClass = m_modelClassLoader.loadClass(Parameter.getClassName());
      m_modelObject = (nz.ac.waikato.modeljunit.FsmModel) m_modelClass.newInstance();
      int actionNumber = 0;
      for (Method method : m_modelClass.getMethods()) {
        if (method.isAnnotationPresent(Action.class)) {
          actionNumber++;
          TestExeModel.addMethod(method);
        }
      }
//          System.out.println("Added "+actionNumber+" actions.");
      return true;
    }
    catch (ClassCastException ex) {
      ErrorMessage.DisplayErrorMessage(
          "Wrong class (ClassCastException",
          "Please select FsmModel class."
              + "\n Error in TestExeModel::loadModelClassFromFile: "
              + ex.getLocalizedMessage());
    }
    catch (InstantiationException ie) {
      ErrorMessage.DisplayErrorMessage(
          "Model not initialized (InstantiationException)",
          "Can not initialize model."
              + "\n Error in TestExeModel::loadModelClassFromFile: "
              + ie.getLocalizedMessage());
    }
    catch (IllegalAccessException iae) {
      ErrorMessage.DisplayErrorMessage(
          "Cannot access model (IllegalAccessException)",
          "Can not access model class."
              + "\n Error in TestExeModel::loadModelClassFromFile: "
              + iae.getLocalizedMessage());
    } catch (ClassNotFoundException ex) {
      ErrorMessage.DisplayErrorMessage(
          "Cannot find model class (ClassNotFoundException)",
          "Can not access model class."
              + "\n Error in TestExeModel::loadModelClassFromFile: "
              + ex.getLocalizedMessage());
    }
    return false;
  }

  /**
   * TODO: eliminate this.  Instead, instantiate the appropriate 
   * Tester subclass on-the-fly each time we want to generate tests.
   * 
   * The array of tester object
   * Using array because we need to separate several tester for different panel.
   * m_tester[0]. For automatically run testing.
   * m_tester[1]. For manually run testing.  (NO LONGER USED)
   **/
  private static Tester[] m_tester = new Tester[2];

  public static void setTester(Tester tester, int idx)
  {
    System.out.println("Changed tester["+idx+"] from "
        +m_tester[idx]
        +" to "+tester);

    m_tester[idx] = tester;
  }

  public static Tester getTester(int idx)
  {
    return m_tester[idx];
  }

  private static IAlgorithmParameter m_algo;

  public static void setAlgorithm(IAlgorithmParameter algo)
  {
    m_algo = algo;
  }

  private static ArrayList<Method> m_arrayMethod = new ArrayList<Method>();

  // Add an action method into list
  public static void addMethod(Method m)
  {
    m_arrayMethod.add(m);
  }

  public static ArrayList<Method> getMethodList()
  {
    return m_arrayMethod;
  }

  public static void reset() {
     m_arrayMethod.clear();
     //resetModelToNull();
  }

  /** Generate and execute tests automatically.
   *  This is called when the user presses the run button.
   *  
   *  TODO: this should almost all disappear and be done
   *  by the current algorithm (Tester subclass) and a configuration
   *  object.
   */
  public static void runTestAuto()
  {
    String output = new String();
    // Redirect the system.out to result viewer text area component
    PrintStream ps = System.out; //Backup the System.out for later restore
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //System.setOut(new PrintStream(baos, true));
    // Run algorithm
    m_algo.runAlgorithm(0);
    if (m_tester[0] instanceof RandomTester) {
      RandomTester tester = (RandomTester) m_tester[0];
      tester.setResetProbability(Parameter.getResetProbability());
    }
    // Set up coverage matrix to check the test result
    boolean[] bCoverage = Parameter.getCoverageOption();
    // Generate graph
    if (bCoverage[0] || bCoverage[1] || bCoverage[2] || bCoverage[3])
      m_tester[0].buildGraph();
    CoverageMetric[] coverage = new CoverageMetric[COVERAGE_NUM];

    if (bCoverage[0]) {
      coverage[0] = new StateCoverage();
      m_tester[0].addCoverageMetric(coverage[0]);
    }

    if (bCoverage[1]) {
      coverage[1] = new TransitionCoverage();
      m_tester[0].addCoverageMetric(coverage[1]);
    }

    if (bCoverage[2]) {
      coverage[2] = new TransitionPairCoverage();
      m_tester[0].addCoverageMetric(coverage[2]);
    }
    if (bCoverage[3]) {
      coverage[3] = new ActionCoverage();
      m_tester[0].addCoverageMetric(coverage[3]);
    }

    StringBuffer verbose = new StringBuffer();
    StringWriter sw = new StringWriter();
    if (Parameter.getVerbosity()) {
      VerboseListener vl = new VerboseListener();
      m_tester[0].addListener(vl);
      m_tester[0].addListener(new VisualisationListener());
    }
    // Redirect model's output to string
    Model md = m_tester[0].getModel();
    Writer defWriter = md.getOutput();
    md.setOutput(sw);
    // This writer updates the test results panel.
    // TODO: move this class into PanelResultViewer.
    Writer newWriter = new Writer()
    {
      PanelResultViewer panel = PanelResultViewer.getResultViewerInstance();

      @Override
      public void close() throws IOException
      {
      }

      @Override
      public void flush() throws IOException
      {
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException
      {
        StringBuffer str = new StringBuffer();
        for (int i = off; i < off + len; i++) {
          str.append(cbuf[i]);
        }
        panel.updateRunTimeInformation(str.toString());
      }
    };
    md.setOutput(newWriter);

    for (int i = 0; i < COVERAGE_NUM; i++) {
      if (bCoverage[i])
        coverage[i].clear();
    }
    
    if (m_tester[0] instanceof QuickTester) {
      QuickTester tester = (QuickTester) m_tester[0];
      tester.clear();
    }
    
    // Generate tests
    System.err.println("Generating "+m_nWalkLength+" tests with "+m_tester[0]);
    m_tester[0].generate(m_nWalkLength);

    // Print out generated model coverage metrics
    for (int metric = 0; metric < COVERAGE_NUM; metric++) {
      if (bCoverage[metric]) {
        try {
          newWriter.write(TestExeModel.COVERAGE_MATRIX[metric] + " = "
              + coverage[metric].toString() + "\n");
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    // Reset model's output to default value
    md.setOutput(defWriter);

    // Recover System.out
    output = baos.toString();
    System.out.println(output);
    // Restore system.out to default value.
    System.setOut(ps);
    verbose.append(output);
  }

  public static ClassLoader getModelClassLoader() {
    return m_modelClassLoader;
  }

  public static void setModelClassLoader(ClassLoader m_modelClassLoader) {
    TestExeModel.m_modelClassLoader = m_modelClassLoader;
  }
}
