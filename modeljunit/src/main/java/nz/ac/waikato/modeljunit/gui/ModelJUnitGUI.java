/**
Copyright (C) 2009 ModelJUnit Project
This file is part of the ModelJUnit project.

The ModelJUnit project contains free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

The ModelJUnit project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ModelJUnit; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package nz.ac.waikato.modeljunit.gui;

import javax.swing.*;

import java.awt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.regex.Matcher;

import java.lang.reflect.Method;

import nz.ac.waikato.modeljunit.gui.visualisaton.PanelJUNGVisualisation;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.Model;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.GreedyTester;

import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.CoverageHistory;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;

import org.objectweb.asm.ClassReader;

/** The main ModelJUnit GUI class.
 *
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/
public class ModelJUnitGUI implements Runnable
{
   private JFrame mAppWindow;
   private String mAppWindowTitle = "ModelJUnit - Untitled*";

   private Project mProject;

   private static Model mModel;
   private PanelJUNGVisualisation mVisualisation;
   private PanelCoverage mCoverage;
   private PanelResultViewer mResultViewer;
   private PanelTestDesign mTestDesign;

   private boolean mGraphCurrent;

   public ModelJUnitGUI() {
      mProject = new Project();
      mGraphCurrent = false;

      mVisualisation = PanelJUNGVisualisation.getGraphVisualisationInstance();
      mCoverage = PanelCoverage.getInstance();
      mResultViewer = PanelResultViewer.getResultViewerInstance();
      mTestDesign = PanelTestDesign.getTestDesignPanelInstance(this); 

      buildGUI();
      
      displaySplashWindow();
   }

   /** Construct an application window. **/
   public void buildGUI() {
      mAppWindow = new JFrame(mAppWindowTitle);
      // For now - set the default close action.
      //TODO: Change this to hook into a confirmation dialogue.
      mAppWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      // Add the elements to the GUI:
      // * MenuBar (Top)
      // * Toolbar (Top)
      // * Info Panel (Left)
      // * Visualizer (Right-Middle)
      // * Status panel (Bottom)

      // Add menu bar:
      mAppWindow.setJMenuBar(new ModelJUnitMenuBar(this));

      // Add tool bar:
      JPanel toolbar = new Toolbar(this);
      toolbar.setPreferredSize(new Dimension(750,40));

      mAppWindow.getContentPane().add(toolbar, BorderLayout.PAGE_START);
      
      // Add Info Panel:
      JPanel infopanel = new JPanel();
      infopanel.setPreferredSize(new Dimension(120,550));

      //mAppWindow.getContentPane().add(infopanel, BorderLayout.LINE_START);
      
      // Add visualiser

            mVisualisation.setPreferredSize(new Dimension(630,430));

      mAppWindow.getContentPane().add(mVisualisation, BorderLayout.CENTER);

      // Add status panel
      JPanel statuspanel = new JPanel();
      JTextArea statustext = new JTextArea();
      statustext.setEditable(false);
      statuspanel.add(statustext);
      statuspanel.setPreferredSize(new Dimension(750,90));

      mAppWindow.getContentPane().add(statuspanel, BorderLayout.PAGE_END);

      mAppWindow.pack();
   }

   public void displaySplashWindow() {
      JDialog splash = new JDialog(mAppWindow, "Welcome to ModelJUnit", true);
      JPanel pane = new JPanel();
      pane.setLayout(new GridBagLayout());

      splash.add(pane,BorderLayout.CENTER);
      splash.add(new JLabel("<html><h1>Welcome to ModelJUnit</h1></html>"), BorderLayout.PAGE_START);

      GridBagConstraints c = new GridBagConstraints();

      c.gridx = 0;
      c.gridy = 0;
      c.ipady = 50;
      c.fill = GridBagConstraints.HORIZONTAL;

      pane.add(new JButton("(New Icon)"), c);

      c.gridx = 1;
      c.gridy = 0;
      c.ipady = 0;

      pane.add(new JLabel("<html><em>Create a new empty ModelJUnit project</em><html>"), c);

      c.gridx = 0;
      c.gridy = 1;
      c.ipady = 50;

      pane.add(new JButton("(Open Icon)"), c);

      c.gridx = 1;
      c.gridy = 1;
      c.ipady = 0;

      pane.add(new JLabel("<html><em>Open an existing ModelJUnit project</em><html>"), c);

      c.gridx = 0;
      c.gridy = 2;
      c.ipady = 50;

      pane.add(new JButton("(Example icon)"), c);

      c.gridx = 1;
      c.gridy = 2;
      c.ipady = 0;

      pane.add(new JLabel("<html><em>Choose from one of the ModelJUnit examples below:</em><html>"), c);

 
      c.gridx = 0;
      c.gridy = 3;
      c.ipady = 60;
      c.gridwidth = 2;

      pane.add(new JTextArea(),c);

      splash.pack();
      splash.setVisible(true);
   }

   public void setTitle(String title) {
      mAppWindowTitle = title;
      mAppWindow.setTitle(title);
   }

   public String getTitle() {
      return mAppWindowTitle;
   }

   public JFrame getFrame() {
      return mAppWindow;
   }

   public void run() {
      mAppWindow.setVisible(true);

      Project pr = new Project();
      pr.setName("Test Project");
      pr.setFileName(new File("test.project"));
      pr.setProperty("foobar",new Integer(123));
      pr.setProperty("test","hello, world");
      Project.save(pr);
   }

   /** Display a file chooser and load the model.
    *
    * This needs to be broken up into two routines so that any
    * model-related logic can be called without displaying the file
    * chooser.  This is so that we can reload a model when opening
    * a project.
    **/
   public void displayFileChooser()
   {
    // ------------ Open model from class file --------------
    FileChooserFilter javaFileFilter = new FileChooserFilter("class",
        "Java class Files");
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File(Parameter.getModelChooserDirectory()));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogTitle("Select Model File");
    chooser.addChoosableFileFilter(javaFileFilter);
    int option = chooser.showOpenDialog(mAppWindow);

    if (option == JFileChooser.APPROVE_OPTION) {
      String errmsg = null;  // null means no errors yet
      File f = chooser.getSelectedFile();
      String wholePath = f.getAbsolutePath();
      Parameter.setModelChooserDirectory(f.getParent());

      // Use ASM to read the package and class name from the .class file
      try {
        ClassReader reader = new ClassReader(new FileInputStream(f));
        String internalName = reader.getClassName();
        int slash = internalName.lastIndexOf('/');
        String className = internalName.substring(slash+1);
        String packageName = "";
        String classPath = "";
        if (slash >= 0) {
          packageName = internalName.substring(0, slash).replaceAll("/", ".");
        }
        //System.out.println("f.absolutePath="+f.getAbsolutePath());
        //System.out.println("internalName="+internalName);
        //System.out.println("className="+className);
        //System.out.println("packageName="+packageName);

        // now calculate the classpath for this .class file.
        String sep = Matcher.quoteReplacement(File.separator);
        String ignore = ("/"+internalName+".class").replaceAll("/", sep);
        //System.out.println("ignore="+ignore);
        if (wholePath.endsWith(ignore)) {
          classPath = wholePath.substring(0, wholePath.lastIndexOf(ignore));
          //System.out.println("MU: classPath="+classPath);
        }
        else {
          errmsg = "Error calculating top of package from: "+wholePath;
        }

        // Load model from file and initialize the model object
        int actionNumber = 0;
        if (errmsg == null) {
          Parameter.setModelPath(wholePath);
          Parameter.setClassName(className);
          Parameter.setPackageName(packageName);
          Parameter.setPackageLocation(classPath);
          if (TestExeModel.loadModelClassFromFile()) {
            Class<?> testcase = TestExeModel.getModelClass();
            for (Method method : testcase.getMethods()) {
              if (method.isAnnotationPresent(Action.class)) {
                actionNumber++;
                TestExeModel.addMethod(method);
                System.out.println("Added method #"+actionNumber);
              }
            }
          }
          else {
            errmsg = "Invalid model class: no @Action methods.";
          }
        }
        if (errmsg == null) {
          // We have successfully loaded a new model
          //initializeTester(0);
          //initializeTester(1);
          //m_butExternalExecute.setEnabled(true);
          String cName = Parameter.getPackageName()+"."+Parameter.getClassName();
          setTitle("ModelJUnit: " + cName);

          //Tester tester = new Tester(TestExeModel.getModelObject());
          Model mod = new Model(TestExeModel.getModelObject());

          ModelJUnitGUI.setModel(mod);

          mGraphCurrent = false;
         // buildGraphGUI();

          //m_modelInfo1.setText("Model:   "+cName);
          //m_modelInfo2.setText("Path:     "+Parameter.getPackageLocation());
          //m_modelInfo3.setText("Actions: "+actionNumber + " actions were loaded.");
          newModel(); // tell the other panels about the new model
        }
      }
      catch (IOException ex) {
        errmsg = "Error reading .class file: "+ex.getLocalizedMessage();
      }
      if (errmsg != null) {
        ErrorMessage.DisplayErrorMessage("Error loading model", errmsg);
        TestExeModel.resetModelToNull();
        Parameter.setModelPath("");
        Parameter.setClassName("");
        Parameter.setPackageName("");
        Parameter.setPackageLocation("");
        //m_modelInfo1.setText(" ");
        //m_modelInfo2.setText(MSG_NO_MODEL);
        //m_modelInfo3.setText(" ");
        // TODO: could call m_gui.newModel() here too? (To reset all panels)
      }
    }
  }

   public void showProjectDialog(Project project) {
      ProjectDialog pd;
      if(project == null) pd = new ProjectDialog(this);
      else pd = new ProjectDialog(this, project);

      pd.setVisible(true);
   }

   /** Display the window that permits animation of models. **/
   public void displayAnimateWindow() {
      // if there is no model loaded, throw an error:
      if(getModel() == null) {
         System.err.println("Error: no model loaded");
         //XXX: throw up a dialog box.
         return;
      }

      JFrame animate = new JFrame("Animator - ModelJUnit");
      animate.setPreferredSize(new Dimension(760,500));
      PanelAnimator pa = PanelAnimator.getInstance();
      pa.newModel();
      
      // Add the action history, which the animator supplies.
      JScrollPane scroll = new JScrollPane(pa.getActionHistoryList());

      JPanel labelPanel = new JPanel();

      labelPanel.add(new JLabel("<html><h1>"+getModel().getModelName()+"</h1></html>"),BorderLayout.PAGE_START);
      labelPanel.add(pa.getStateLabel(),BorderLayout.PAGE_END);

      animate.add(labelPanel, BorderLayout.PAGE_START);
      animate.add(pa, BorderLayout.CENTER);

      animate.add(scroll, BorderLayout.LINE_END);

      animate.add(pa.getResetButton(), BorderLayout.PAGE_END);

      animate.pack();
      animate.setVisible(true);
   }
   
   /** Display the window that shows coverage metrics for models. **/
   public void displayCoverageWindow() {
      JFrame coverage = new JFrame("Coverage - ModelJUnit");
      coverage.setMinimumSize(new Dimension(760,500));
      coverage.add(mCoverage, BorderLayout.CENTER);
      coverage.add(mCoverage.getProgress(), BorderLayout.PAGE_END);
      coverage.setVisible(true);
   }

   /** Display the window that shows test results. **/
   public void displayResultsWindow() {
      JFrame results = new JFrame("Results - ModelJUnit");
      results.setMinimumSize(new Dimension(760,500));
      results.add(mResultViewer);
      results.setVisible(true);
   }

   public static void setModel(Model model) {
      mModel = model;
   }

   public static Model getModel() {
      return mModel;
   }

   public void newModel() {
      mVisualisation.newModel();
      mCoverage.newModel();
      mResultViewer.newModel();
      mTestDesign.newModel();
   }

   public void displayAlgorithmPane() {
      JDialog dialog = new JDialog(mAppWindow,"Edit Configuration",true);
      dialog.getContentPane().add(mTestDesign, BorderLayout.LINE_START);
      dialog.getContentPane().add(new JScrollPane(mTestDesign.getCodeView()), BorderLayout.LINE_END);
      dialog.pack();
      dialog.setVisible(true);
   }

   public void buildGraphGUI() {
      if(mGraphCurrent) return;

     /* JDialog dialog = new JDialog(mAppWindow,"Graph building in progress",true);
      dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      dialog.getContentPane().add(new JLabel("ModelJUnit is currently building a graph from your model.\nThis may take a few seconds."));
      dialog.pack();
      dialog.setVisible(true);*/

      mCoverage.getProgress().setIndeterminate(true);

      Tester tester = TestExeModel.getTester(0);
      tester.buildGraph(); 

      mCoverage.getProgress().setIndeterminate(false);

      mGraphCurrent = true; 

      /*dialog.setVisible(false);*/
  }

  private void runClass()
  {
    // Draw line chart in coverage panel
    if (mTestDesign.isLineChartDrawable()) {
      mCoverage.clearCoverages();
      int[] stages = mCoverage.computeStages(TestExeModel.getWalkLength());

      mTestDesign.initializeTester(0);
      Tester tester = TestExeModel.getTester(0);
     /* tester.buildGraph();*/
      displayCoverageWindow();
      buildGraphGUI();

      CoverageHistory[] coverage = new CoverageHistory[TestExeModel.COVERAGE_NUM];
      coverage[0] = new CoverageHistory(new StateCoverage(), 1);
      coverage[1] = new CoverageHistory(new TransitionCoverage(), 1);
      coverage[2] = new CoverageHistory(new TransitionPairCoverage(), 1);
      coverage[3] = new CoverageHistory(new ActionCoverage(), 1);
      tester.addCoverageMetric(coverage[0]);
      tester.addCoverageMetric(coverage[1]);
      tester.addCoverageMetric(coverage[2]);
      tester.addCoverageMetric(coverage[3]);
      // Run test several times to draw line chart
      for (int i = 0; i < stages.length; i++) {
        tester.generate(stages[0]);
	System.out.println("Progress: " + stages[i] + "/" + TestExeModel.getWalkLength());
        mCoverage.setProgress(stages[i],TestExeModel.getWalkLength());
        // Update the line chart and repaint
        mCoverage.addStateCoverage((int) coverage[0].getPercentage());
        mCoverage.addTransitionCoverage((int) coverage[1].getPercentage());
        mCoverage.addTransitionPairCoverage((int) coverage[2].getPercentage());
        mCoverage.addActionCoverage((int) coverage[3].getPercentage());
        mCoverage.redrawGraph();
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    // To reset tester, it solve the problem that coverage matrix incorrect.
    mTestDesign.initializeTester(0);
    //reset the visualisation panel
    mVisualisation.resetRunTimeInformation();
    //Try to fully explore the complete graph before running the test explorations
    Tester tester = TestExeModel.getTester(0);
    GraphListener graph = tester.buildGraph();
    mVisualisation.showEmptyExploredGraph(graph);

    // Clear the information in Result viewer text area
    mResultViewer.resetRunTimeInformation();

    // Run test and display test output
    TestExeModel.runTestAuto();
    // Finish the visualisation panel. This effectively starts the animation.    
    mVisualisation.updateGUI(true);
   }

   public void runModel() {
      if(mModel == null) return;

      SwingWorker<String,String> worker = new SwingWorker<String,String>() {
         public String doInBackground() {
            runClass();
            return "";
         }
      };
     
      worker.execute();

      //runClass();
 
      /*CoverageHistory hist = new CoverageHistory(new TransitionCoverage(), 1);
      tester.addCoverageMetric(hist);
      tester.addListener("verbose");
      while (hist.getPercentage() < 99.0)
        tester.generate();
      System.out.println("Transition Coverage ="+hist.toString());
      System.out.println("History = "+hist.toCSV());*/
   }


   public static void main(String[] args) {
      ModelJUnitGUI gui = new ModelJUnitGUI();

      new Thread(gui).start();
   }
}
