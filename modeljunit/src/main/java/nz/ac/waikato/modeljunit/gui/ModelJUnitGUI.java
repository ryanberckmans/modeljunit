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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;

import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.Model;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.CoverageHistory;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;
import nz.ac.waikato.modeljunit.gui.visualisaton.PanelJUNGVisualisation;

/**
 * The main ModelJUnit GUI class.
 * 
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/
public class ModelJUnitGUI implements Runnable {
    public static final String BUILTIN = "Builtin Examples";

    public static final String MODELJUNIT_VERSION = "2.0";

    private JFrame mAppWindow;
    private String mAppWindowTitle = "ModelJUnit - Untitled*";

    private Project mProject;

    private static Model mModel;
    private PanelJUNGVisualisation mVisualisation;
    private PanelCoverage mCoverage;
    private PanelResultViewer mResultViewer;
    private PanelTestDesign mTestDesign;
    private PanelEfficiencyGraph mEfficiencyGraphs;
    private PanelAnimator mAnimator;
    private JDialog mSplash;

    private boolean mGraphCurrent;


    public PanelResultViewer getResultViewer() {
        return mResultViewer;
    }

    /**
     * This creates the persistent panels for ModelJUnit (except the animation panel, which is transient). All panels
     * are subclasses of PanelAbstract, which handles model-change notifications etc. When each panel is displayed, it
     * is put into a transient frame.
     */
    public ModelJUnitGUI() {
        this(true);
    }

    public ModelJUnitGUI(boolean isDisplaySplashWindow) {
        initialize();
        buildGUI();

        if (isDisplaySplashWindow) {
            displaySplashWindow();
        } else {
            mAppWindow.setVisible(true);
            mAppWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    public PanelJUNGVisualisation getVisualisation() {
        return mVisualisation;
    }

    private void initialize() {
        //TODO: Make these panels non-static
        mProject = new Project(BUILTIN, "nz.ac.waikato.modeljunit.examples.FSM");
        mGraphCurrent = false;

        mAnimator = new PanelAnimator(this); 
        mVisualisation = new PanelJUNGVisualisation();
        mCoverage = new PanelCoverage(this);
        mResultViewer = new PanelResultViewer();
        mTestDesign = new PanelTestDesign(this);
        mEfficiencyGraphs = new PanelEfficiencyGraph();
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
        toolbar.setPreferredSize(new Dimension(750, 40));

        mAppWindow.getContentPane().add(toolbar, BorderLayout.PAGE_START);

        // Add Info Panel:
        JPanel infopanel = new JPanel();
        infopanel.setPreferredSize(new Dimension(120, 550));

        //mAppWindow.getContentPane().add(infopanel, BorderLayout.LINE_START);

        // Add visualiser

        mVisualisation.setPreferredSize(new Dimension(630, 430));

        mAppWindow.getContentPane().add(mVisualisation, BorderLayout.CENTER);

        // Add status panel
        JPanel statuspanel = new JPanel();
        JTextArea statustext = new JTextArea();
        statustext.setEditable(false);
        statuspanel.add(statustext);
        statuspanel.setPreferredSize(new Dimension(750, 90));

        mAppWindow.getContentPane().add(statuspanel, BorderLayout.PAGE_END);

        mAppWindow.pack();
    }

    public void displayAboutWindow() {
        final JDialog about = new JDialog(mAppWindow, "About ModelJUnit", true);
        about.add(new JLabel("<html><h1>ModelJUnit v2.0-beta1</h1></html>"), BorderLayout.PAGE_START);
        about.add(new JLabel("Copyright (c) 2009 The University of Waikato"), BorderLayout.CENTER);
        JButton btn = new JButton("OK");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                about.setVisible(false);
            }
        });
        about.add(btn, BorderLayout.PAGE_END);
        about.pack();
        about.setLocationRelativeTo(null);
        about.setVisible(true);
    }

    public void displaySplashWindow() {
        mSplash = new JDialog(mAppWindow, "Welcome to ModelJUnit", true);
        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());

        mSplash.add(pane, BorderLayout.CENTER);
        mSplash.add(new JLabel("<html><h1>&nbsp;Welcome to ModelJUnit</h1></html>"), BorderLayout.PAGE_START);
        mSplash.setResizable(false);
        mSplash.setMinimumSize(new Dimension(400, 545));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.CENTER;

        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 0;
        c.fill = GridBagConstraints.BOTH;

        JButton but = new JButton("New Project");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mSplash.setVisible(false);
                showProjectDialog(null);
            }
        });

        pane.add(but, c);

        c.gridx = 1;
        c.gridy = 0;
        c.ipady = 0;

        pane.add(new JLabel("<html><em>Create a new empty ModelJUnit project</em><html>"), c);

        c.gridx = 0;
        c.gridy = 1;
        c.ipady = 0;

        but = new JButton("Open Project");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mSplash.setVisible(false);
                displayProjectFileChooser(true);
            }
        });

        pane.add(but, c);

        c.gridx = 1;
        c.gridy = 1;
        c.ipady = 0;

        pane.add(new JLabel("<html><em>Open an existing ModelJUnit project</em><html>"), c);

        c.gridx = 0;
        c.gridy = 2;
        c.ipady = 0;
        c.ipadx = 50;
        c.gridwidth = 2;

        pane.add(new JLabel("<html><em>Or double-click on any of the ModelJUnit examples below:</em><html>"), c);

        c.gridx = 0;
        c.gridy = 3;
        c.ipady = 200;
        c.gridwidth = 2;

        final DefaultListModel exampleModel = new DefaultListModel();
        final JList examples = new JList(exampleModel);

        for (int i = 0; i < ExampleModels.EXAMPLE_MODELS.length; i++) {
            String[] example = ExampleModels.EXAMPLE_MODELS[i].split(":");
            exampleModel.addElement(example[1] + " - " +  example[2]);
        }

        pane.add(new JScrollPane(examples), c);

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = examples.locationToIndex(e.getPoint());
                    String example = ExampleModels.EXAMPLE_MODELS[index];
                    example = example.split(":")[0];
                    mSplash.setVisible(false);
                    loadModel(BUILTIN, example);
                    boolean[] coverage = { true, true, false, false, false };
                    mProject.setCoverageOption(coverage);
                    mTestDesign.newModel();
                }
            }
        };

        examples.addMouseListener(mouseListener);

        mSplash.pack();

        mSplash.setLocationRelativeTo(null);

        mSplash.setVisible(true);
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

    //Find out when this is being called
    public void run() {
        mAppWindow.setVisible(true);
        mAppWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);

        Project pr = new Project(BUILTIN, "nz.ac.waikato.modeljunit.examples.FSM");
        pr.setName("Test Project");
        pr.setFileName(new File("test.mju"));
        //pr.save();
    }

    /**
     * Display a file chooser and returns the string of the selected file.
     * 
     * @return String of jar path or null if user cancelled
     **/
    public String displayFileChooser() {
        // ------------ Open model from class file --------------
        FileChooserFilter javaFileFilter = new FileChooserFilter("jar", "Compiled JAR Files");
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(Parameter.getModelChooserDirectory()));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Select JAR File");
        chooser.addChoosableFileFilter(javaFileFilter);
        int option = chooser.showOpenDialog(mAppWindow);

        if (option == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();

            Parameter.setModelChooserDirectory(f.getParent());
            return f.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Loads a model. If the model is successfully loaded, then a new project is created and becomes the current project.
     * 
     * We assume these are inside the current .jar file or package, so the example model can be loaded using the current
     * class loader.
     * 
     * @param className
     *            the short name (without "nz.ac.waikato.modeljunit.examples") of the model to load.
     */
    public void loadModel(String jarURL, String className) {
        mProject = new Project(jarURL, className);
        if (className != null) {
            displayNewModel(className);
        }
    }

    public void displayNewModel(String cName) {
        //Change the title to the filename if model is not an example
        if (mProject.getPackageLocation().equals(BUILTIN)) {
            mProject.setName(cName);
        }
        setTitle("ModelJUnit: " + mProject.getName());

        //Tester tester = new Tester(TestExeModel.getModelObject());
        mModel = new Model(mProject.getModelObject());

        mGraphCurrent = false;
        newModel(); // tell the other panels about the new model
        //Automatically generate tests for the selected model
        runModel();
    }

    public void displayProjectFileChooser(boolean opening) {
        String fileExt = "mju";
        FileChooserFilter javaFileFilter = new FileChooserFilter(fileExt, "ModelJUnit Project Files");
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(Parameter.getModelChooserDirectory()));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (opening)
            chooser.setDialogTitle("Select ModelJUnit Project File for Opening");
        else
            chooser.setDialogTitle("Select Location to Save ModelJUnit Project");
        chooser.addChoosableFileFilter(javaFileFilter);
        int option = 0;

        if (opening)
            option = chooser.showOpenDialog(mAppWindow);
        else
            option = chooser.showSaveDialog(mAppWindow);

        if (option == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String wholePath = f.getAbsolutePath();
            Parameter.setModelChooserDirectory(f.getParent());

            if (opening) {
                try {
                    mProject = Project.load(f);
                    if (mProject.getModelFile() != null) {
                        Parameter.setModelChooserDirectory(mProject.getModelFile().getParent());
                    }
                    mProject.setName(f.getAbsolutePath());
                    displayNewModel(mProject.getClassName());
                } catch (JAXBException ex) {
                    String msg = ex.getLocalizedMessage();
                    ErrorMessage.DisplayErrorMessage("Load Error", "Error loading MJU file: \n"
                                    + (msg != null ? msg : "File is corrupt."));   
                }
            } else {
                // Saving the project
                if (!wholePath.endsWith("." + fileExt)) {
                    wholePath += "." + fileExt;
                    f = new File(wholePath);
                }
                mProject.setFileName(f);
                String fName = f.getAbsolutePath();
                mProject.setName(fName);
                setTitle("ModelJUnit: " + fName);
            }
        }
    }

    public void showProjectDialog(Project project) {
        ProjectDialog pd;
        if (project == null)
            pd = new ProjectDialog(this);
        else
            pd = new ProjectDialog(this, project);

        pd.pack();

        pd.setLocationRelativeTo(null);

        pd.setVisible(true);
    }

    public void saveProject() {
        if (mProject.getFileName() == null) {
            displayProjectFileChooser(false);
        }

        mProject.save();
    }

    /** Display the window that permits animation of models. **/
    public void displayAnimateWindow() {
        // if there is no model loaded, throw an error:
        if (mModel == null) {
            ErrorMessage.DisplayErrorMessage("No Model", "Error: no model loaded");
            return;
        }

        JFrame animate = new JFrame("Animator - ModelJUnit");
        animate.setPreferredSize(new Dimension(760, 500));
        mAnimator.newModel();

        // Add the action history, which the animator supplies.
        JScrollPane scroll = new JScrollPane(mAnimator.getActionHistoryList());

        JPanel labelPanel = new JPanel();

        labelPanel.add(new JLabel("<html><h1>" + mModel.getModelName() + "</h1></html>"), BorderLayout.PAGE_START);
        labelPanel.add(mAnimator.getStateLabel(), BorderLayout.PAGE_END);

        animate.add(labelPanel, BorderLayout.PAGE_START);
        animate.add(mAnimator, BorderLayout.CENTER);

        animate.add(scroll, BorderLayout.LINE_END);

        animate.add(mAnimator.getResetButton(), BorderLayout.PAGE_END);

        animate.pack();
        animate.setVisible(true);
    }

    /** Display the window that shows coverage metrics for models. **/
    public void displayCoverageWindow() {
        JFrame coverage = new JFrame("Coverage - ModelJUnit");
        coverage.setMinimumSize(new Dimension(760, 500));
        coverage.add(mCoverage, BorderLayout.CENTER);
        coverage.add(mCoverage.getProgress(), BorderLayout.PAGE_END);
        coverage.setVisible(true);
    }

    /** Display the window that shows test results. **/
    public void displayResultsWindow() {
        JFrame results = new JFrame("Results - ModelJUnit");
        results.setMinimumSize(new Dimension(760, 500));
        results.add(mResultViewer);
        results.setVisible(true);
    }

    /** Display the efficiency graph window **/
    public void displayEfficiencyGraphs() {
        JFrame efficiencyGraphs = new JFrame("Efficiency Graphs - ModelJUnit");
        efficiencyGraphs.setMinimumSize(new Dimension(760, 500));
        efficiencyGraphs.add(mEfficiencyGraphs, BorderLayout.CENTER);
        efficiencyGraphs.add(mEfficiencyGraphs.getProgress(), BorderLayout.PAGE_END);
        efficiencyGraphs.setVisible(true);
        mEfficiencyGraphs.runClass(mProject);
    }

    public Project getProject() {
        return mProject;
    }

    /**
     * Replaces the main instance of Project. Only to be used by PanelTestDesign's OK button listener!
     * @param project
     */
    public void setProject(Project project) {
        mProject = project.clone();
    }
    
    public void newModel() {
        mVisualisation.newModel();
        mCoverage.newModel();
        mResultViewer.newModel();
        mTestDesign.newModel();
        mEfficiencyGraphs.newModel();
    }

    public void displayAlgorithmPane() {
        JFrame dialog = new JFrame("Edit Configuration");
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mTestDesign, mTestDesign.getCodeView());
        splitPane.setDividerLocation(400);

        dialog.add(splitPane);
        dialog.pack();
        dialog.setVisible(true);
        mTestDesign.newModel();
    }

    public void buildGraphGUI() {
        if (mGraphCurrent)
            return;

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

    private void runClass() {
        // Draw line chart in coverage panel
        if (mTestDesign.isLineChartDrawable()) {
            mCoverage.clearCoverages();
            int[] stages = mCoverage.computeStages(mProject.getWalkLength());

            mTestDesign.initializeTester(0);
            Tester tester = TestExeModel.getTester(0);
            /* tester.buildGraph();*/
            // Leave the coverage window hidden until manually opened from the View menu
            // displayCoverageWindow();
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
                //System.out.println("DEBUG: Progress: " + stages[i] + "/" + mProject.getWalkLength());
                mCoverage.setProgress(stages[i], mProject.getWalkLength());
                // Update the line chart and repaint
                mCoverage.addStateCoverage((int) coverage[0].getPercentage());
                mCoverage.addTransitionCoverage((int) coverage[1].getPercentage());
                mCoverage.addTransitionPairCoverage((int) coverage[2].getPercentage());
                mCoverage.addActionCoverage((int) coverage[3].getPercentage());
                mCoverage.redrawGraph();
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
        TestExeModel.runTestAuto(this);
        // Finish the visualisation panel. This effectively starts the animation.    
        mVisualisation.updateGUI(true);
    }

    public void runModel() {
        if (mModel == null)
            return;

// NOTE: runClass cannot be run in the background, because it directly updates GUI objects.
//        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
//            public String doInBackground() {
//                runClass();
//                return "";
//            }
//        };
//        
//        worker.execute();
        
        runClass();

        /*CoverageHistory hist = new CoverageHistory(new TransitionCoverage(), 1);
        tester.addCoverageMetric(hist);
        tester.addListener("verbose");
        while (hist.getPercentage() < 99.0)
          tester.generate();
        System.out.println("Transition Coverage ="+hist.toString());
        System.out.println("History = "+hist.toCSV());*/
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new ModelJUnitGUI());
    }
}
