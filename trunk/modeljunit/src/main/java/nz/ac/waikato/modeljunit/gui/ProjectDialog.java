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
import java.io.FileInputStream;
import java.io.IOException;

/** A dialogue to create or edit a ModelJUnit project, including selection of a class file for the SUT.
 *
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/
public class ProjectDialog extends JDialog
{
   ModelJUnitGUI mParent;

   public ProjectDialog(ModelJUnitGUI parent) {
         super(parent.getFrame(), "New ModelJUnit Project", true);
         mParent = parent;
         constructGUI();
   }

   public ProjectDialog(ModelJUnitGUI parent, Project project) {
         super(parent.getFrame(), "Edit ModelJUnit Project", true);
         mParent = parent;
         constructGUI();
   }


   public void constructGUI() {
      setPreferredSize(new Dimension(400,250));
   //   setMinimumSize(new Dimension(350,500));

      GridLayout gridLayout = new GridLayout(0,1);
      setLayout(gridLayout);

      add(new JLabel("<html><h1>New ModelJUnit Project</h1></html>"));

      JPanel fileSelectPanel = new JPanel();

      fileSelectPanel.add(new JLabel("Model Class File:"));
      fileSelectPanel.add(new JLabel("<html><em>(none selected)</em></html>"));

      JButton browseButton = new JButton("Browse...");

      fileSelectPanel.add(browseButton);

      browseButton.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               mParent.displayFileChooser();
               //XXX: Read from the parent the details of the class and update
            }
         }
      );

      add(fileSelectPanel);

      JPanel infoPanel = new JPanel();
      infoPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 
      infoPanel.add(new JLabel("This will be an area with information."), BorderLayout.PAGE_END);

      add(infoPanel);

      JPanel buttonPanel = new JPanel();
      JButton cancelButton = new JButton("Cancel");
      JButton createButton = new JButton("Create");

      cancelButton.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               setVisible(false);
            }
         }
      );

      createButton.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               setVisible(false);
               //XXX: Create project object and notify parent
            }
         }
      );

      buttonPanel.add(cancelButton);
      buttonPanel.add(createButton);

      add(buttonPanel);
 
      pack();
   }

}
