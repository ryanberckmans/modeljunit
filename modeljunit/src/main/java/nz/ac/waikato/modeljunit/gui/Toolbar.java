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

/** The toolbar class for ModelJUnit.
 *
 * Holds a reference to the parent window so as to enable button actions. 
 *
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/
public class Toolbar extends JPanel
{
   private ModelJUnitGUI mParent;

   public Toolbar(ModelJUnitGUI parent) {
      super();
      mParent = parent;
      this.setLayout(new FlowLayout(FlowLayout.LEFT));
      buildGUI();
   }

   public void buildGUI() {
      JButton b = new JButton("New",createImageIcon("/images/New16.gif", "New Project"));
      this.add(b);

      b.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               mParent.showProjectDialog(null);
            }
         }
      );
      
      b = new JButton("Open", createImageIcon("/images/Open16.gif", "Open Project"));
      this.add(b);

      b.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               mParent.displayProjectFileChooser(true);
            }
         }
      );

      b = new JButton("Save", createImageIcon("/images/Save16.gif", "Save Project"));
      this.add(b);

      b.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               mParent.saveProject();
            }
         }
      );


      b = new JButton("Animate Model", createImageIcon("/images/Animate16.gif", "Animate Model"));
      this.add(b);

      b.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               mParent.displayAnimateWindow();
            }
         }
      );
      
      String[] configurations = {"Configuration 1", "Configuration 2", "Configuration 3"};
      JComboBox combo = new JComboBox(configurations);

      this.add(combo);

      b = new JButton("Generate Tests", createImageIcon("/images/Play16.gif", "Run Configuration"));

      this.add(b);

      b.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               mParent.runModel();
	       //TestExeModel.runTestAuto();

            }
         }
      );


      b = new JButton("View Tests",createImageIcon("/images/Results16.gif", "View Results") );
      this.add(b);

      b.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
               mParent.displayResultsWindow();
            }
         }
      );

   }

   /** Returns an ImageIcon, or null if the path was invalid. */
   protected ImageIcon createImageIcon(String path,
                                           String description) {
      java.net.URL imgURL = getClass().getResource(path);
      System.out.println("imgURL: "+imgURL);
      if (imgURL != null) {
          return new ImageIcon(imgURL, description);
      } else {
          System.err.println("Couldn't find file: " + path);
          return null;
      }
   }

}
