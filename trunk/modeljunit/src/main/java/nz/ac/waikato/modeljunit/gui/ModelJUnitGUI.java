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

   public ModelJUnitGUI() {
      mProject = new Project();
      buildGUI();
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

      PanelAbstract visualisation = new PanelJUNGVisualisation();
      visualisation.setPreferredSize(new Dimension(630,430));

      mAppWindow.getContentPane().add(visualisation, BorderLayout.CENTER);

      // Add status panel
      JPanel statuspanel = new JPanel();
      JTextArea statustext = new JTextArea();
      statustext.setEditable(false);
      statuspanel.add(statustext);
      statuspanel.setPreferredSize(new Dimension(750,90));

      mAppWindow.getContentPane().add(statuspanel, BorderLayout.PAGE_END);

      mAppWindow.pack();
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
          //m_modelInfo1.setText("Model:   "+cName);
          //m_modelInfo2.setText("Path:     "+Parameter.getPackageLocation());
          //m_modelInfo3.setText("Actions: "+actionNumber + " actions were loaded.");
          //m_gui.newModel(); // tell the other panels about the new model
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
      JFrame animate = new JFrame("Animator - ModelJUnit");
      animate.setMinimumSize(new Dimension(760,500));
      animate.setVisible(true);
   }
   
   /** Display the window that shows coverage metrics for models. **/
   public void displayCoverageWindow() {
      JFrame coverage = new JFrame("Coverage - ModelJUnit");
      PanelCoverage pc = PanelCoverage.getInstance();
      coverage.setMinimumSize(new Dimension(760,500));
      coverage.add(pc);
      coverage.setVisible(true);
   }

   /** Display the window that shows test results. **/
   public void displayResultsWindow() {
      JFrame results = new JFrame("Results - ModelJUnit");
      results.setMinimumSize(new Dimension(760,500));
      PanelResultViewer prv = PanelResultViewer.getResultViewerInstance();
      results.add(prv);
      results.setVisible(true);
   }

   public static void main(String[] args) {
      ModelJUnitGUI gui = new ModelJUnitGUI();

      new Thread(gui).start();
   }
}
