/**
 * The TestingParameter class includes all the value of PanelTestDesign,
 * Application can store these parameters into file or load them from file
 */

package nz.ac.waikato.modeljunit.gui;

import java.awt.Color;

import javax.xml.bind.annotation.XmlRootElement;

import nz.ac.waikato.modeljunit.RandomTester;

/*
 * Parameter.java
 * @author rong ID : 1005450 13th Aug 2007
 * 
 * TODO: Move the model-related parameters into the Project class,
 * so that they are saved, and turn the remaining parameters
 * (to do with test generation) into a serializable, non-singleton
 * configuration class, so that it becomes possible to have multiple
 * configurations within a project.
 */

@XmlRootElement
public class Parameter {
    public static final String DEFAULT_DIRECTORY = System.getProperty("user.dir");
    
    // Number of coverage options
    public static final int NUM_COVERAGE = 5;

    /**
     * Test case class name
     */
    private static String m_strTestCaseVariableName;

    public static String getTestCaseVariableName() {
        return m_strTestCaseVariableName;
    }

    public static void setTestCaseVariableName(String name) {
        m_strTestCaseVariableName = name;
    }
    
    /**
     * The path to the directory where the .class model files are read from.
     */
    private static String m_strModelChooserDirectory;

    /**
     * The absolute path to the directory that contains the .class model files. Note that this includes the package
     * directories.
     * 
     * @return
     */
    public static String getModelChooserDirectory() {
        if (m_strModelChooserDirectory == null) {
            m_strModelChooserDirectory = DEFAULT_DIRECTORY;
        }
        return m_strModelChooserDirectory;
    }

    /**
     * Set the absolute path (including the package directories) to the .class model files.
     * 
     * @param directory
     */
    public static void setModelChooserDirectory(String directory) {
        m_strModelChooserDirectory = directory;
    }

    /*
     * When user open a file selection dialog the default location is
     * 0. To use last time directory, 1. To use default path, the default
     * path is the location that the application running.
     */
    private static int m_nFileChooserOpenMode;

    public static int getFileChooserOpenMode() {
        return m_nFileChooserOpenMode;
    }

    public static void setFileChooserOpenMode(int mode) {
        m_nFileChooserOpenMode = mode;
    }

    /**
     * The function will test whether a model was loaded and whether a algorithm was selected, if both been done, return
     * true otherwise return false
     * 
     * @return if user can use the model to test return true otherwise return false
     */
    public static boolean isTestRunnable(Project mProject, boolean bShowErrMsg) {
        if (mProject.getClassName() == null || !mProject.isModelLoaded() || mProject.getClassName().length() == 0) {
            if (bShowErrMsg)
                ErrorMessage.DisplayErrorMessage("NO MODEL", "No model loaded!");
            return false;
        }
        return true;
    }

    // 0.State coverage color
    // 1.Transition coverage color
    // 2.Transition pair coverage color
    // 3.Action coverage color
    private static Color[] m_colorLine;

    public static void setCoverageLineColors(Color[] color) {
        m_colorLine = color;
    }

    // Lazy initialization the line color
    public static Color[] getCoverageLineColors() {
        if (m_colorLine == null) {
            m_colorLine = new Color[4];
            m_colorLine[0] = Color.BLACK;
            m_colorLine[1] = Color.RED;
            m_colorLine[2] = Color.GREEN;
            m_colorLine[3] = Color.BLUE;
        }
        return m_colorLine;
    }

    //----------------------Override toString----------------------
    public String toString() {
        return "Model Chooser Directory: " + m_strModelChooserDirectory + 
                        ", File Chooser Open Mode: " + m_nFileChooserOpenMode;
    }
}
