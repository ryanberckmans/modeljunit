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
import java.awt.event.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A dialogue to create or edit a ModelJUnit project, including selection of a class file for the SUT.
 * 
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/
@SuppressWarnings("serial")
public class ProjectDialog extends JDialog {
    private ModelJUnitGUI mParent;
    private JLabel mModelJARName;
    private JLabel mModelInfoLabel;
    private JTextField mModelClassName;
    private JButton createButton;

    public ProjectDialog(ModelJUnitGUI parent, Project... project) {
        super(parent.getFrame(), "Edit ModelJUnit Project", true);
        mParent = parent;
        mModelJARName = new JLabel("(none selected)");
        mModelInfoLabel = new JLabel(" Please enter the location of the class file (e.g: package.MyModel): ");
        mModelClassName = new JTextField();
        createButton = new JButton("Load");
        constructGUI();
    }

    public void constructGUI() {
        setPreferredSize(new Dimension(800, 250));

        GridLayout gridLayout = new GridLayout(0, 1);
        setLayout(gridLayout);

        add(new JLabel("<html><h1>&nbsp;New ModelJUnit Project</h1></html>"));

        JPanel fileSelectPanel = new JPanel();

        fileSelectPanel.add(new JLabel("Model JAR File:"), BorderLayout.PAGE_START);
        fileSelectPanel.add(mModelJARName, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse...");

        fileSelectPanel.add(browseButton, BorderLayout.PAGE_END);

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String jarName = mParent.displayFileChooser();

                mModelJARName.setText("<html><em>" + jarName + "&nbsp;</em></html>");
                pack();
                if (jarName != null) {
                    Parameter.setPackageLocation(jarName);
                    mModelInfoLabel.setEnabled(true);
                    mModelClassName.setEnabled(true);
                    createButton.setEnabled(true);
                }
            }
        });

        add(fileSelectPanel);

        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        GridLayout panelGridLayout = new GridLayout(0, 1);
        panelGridLayout.setVgap(3);
        infoPanel.setLayout(panelGridLayout);

        infoPanel.add(mModelInfoLabel, BorderLayout.NORTH);
        infoPanel.add(mModelClassName, BorderLayout.SOUTH);

        add(infoPanel);

        mModelInfoLabel.setEnabled(false);
        mModelClassName.setEnabled(false);
        createButton.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                //XXX: Create project object and notify parent

                String packagePath = mModelClassName.getText();
                packagePath = packagePath.replace(".class", "").replace("/", ".").replace("\\", ".").trim();
                Parameter.setClassName(packagePath);
                String classPath = Parameter.getPackageLocation()/*.replace(".jar", "") + "/" + Parameter.getClassName()*/;
                System.out.println("DEBUG: Full model path: " + classPath);
                File f = new File(classPath);
                String errmsg = null;
                try {
                    errmsg = mParent.loadModel(f, Parameter.getClassName());
                } catch (FileNotFoundException e1) {
                    System.out.println("ERROR: " + errmsg);
                } catch (IOException e1) {
                    System.out.println("ERROR: " + errmsg);
                }
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);

        add(buttonPanel);

        pack();
    }

}
