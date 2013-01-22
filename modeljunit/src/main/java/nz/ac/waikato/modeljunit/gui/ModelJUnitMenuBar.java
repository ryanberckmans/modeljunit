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

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.*;

/**
 * The application window Menu Bar.
 * 
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/
public class ModelJUnitMenuBar extends JMenuBar {
    private ModelJUnitGUI mParent;

    private JMenu mFile = new JMenu("File");
    private JMenu mEdit = new JMenu("Edit");
    private JMenu mView = new JMenu("View");
    private JMenu mRun = new JMenu("Run");
    private JMenu mHelp = new JMenu("Help");

    /**
     * Construct a new Menu bar with appropriate action listener hooks.
     * 
     * @param parent
     *            The ModelJUnitGUI frame to which this menu will be attached.
     */
    public ModelJUnitMenuBar(ModelJUnitGUI parent) {
        super();

        mParent = parent;

        // Populate the "File" menu:
        JMenuItem item = new JMenuItem("New Project");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        mFile.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.showProjectDialog(null);
            }
        });

        item = new JMenuItem("Open Project...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        mFile.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayProjectFileChooser(true);
            }
        });

        mFile.addSeparator();

        item = new JMenuItem("Save Project");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        mFile.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.saveProject();
            }
        });

        item = new JMenuItem("Save Project As...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK));
        mFile.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayProjectFileChooser(false);
                mParent.saveProject();
            }
        });

        mFile.addSeparator();

        //TODO: implement "recently opened projects"
        mFile.add(new JMenuItem("Recent Item 1"));
        mFile.add(new JMenuItem("Recent Item 2"));
        mFile.add(new JMenuItem("Recent Item 3"));
        mFile.add(new JMenuItem("Recent Item 4"));

        mFile.addSeparator();

        item = new JMenuItem("Quit");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        mFile.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        this.add(mFile);

        // Populate the "Edit" menu:
        item = new JMenuItem("Preferences...");
        mEdit.add(item);

        this.add(mEdit);

        // Populate the "View" menu:
        item = new JMenuItem("Animate Window");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK));
        mView.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayAnimateWindow();
            }
        });

        item = new JMenuItem("View Tests Window");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK));
        mView.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayResultsWindow();
            }
        });

        item = new JMenuItem("Coverage Window");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK));
        mView.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayCoverageWindow();
            }
        });

        this.add(mView);

        // Populate the "Run" menu:
        item = new JMenuItem("Test Configuration...");
        mRun.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayAlgorithmPane();
            }
        });

        item = new JMenuItem("Generate Tests");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        mRun.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.runModel();
                //TestExeModel.runTestAuto();

            }
        });

        item = new JMenuItem("Efficiency Graphs");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        mRun.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayEfficiencyGraphs();
            }
        });

        mRun.addSeparator();

        // ----------------- Examples ------------------
        for (int i = 0; i < ExampleModels.EXAMPLE_MODELS.length; i++) {
            final String modelName = ExampleModels.EXAMPLE_MODELS[i];
            item = new JMenuItem(modelName.split(":")[0]);
            item.setToolTipText(modelName.split(":")[1]);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mParent.loadExampleModel(modelName.split(":")[0]);
                }
            });
            mRun.add(item);
        }

        this.add(mRun);

        // Populate the "Help" menu:

        item = new JMenuItem("About ModelJUnit...");
        mHelp.add(item);

        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mParent.displayAboutWindow();
            }
        });

        this.add(mHelp);
    }
}
