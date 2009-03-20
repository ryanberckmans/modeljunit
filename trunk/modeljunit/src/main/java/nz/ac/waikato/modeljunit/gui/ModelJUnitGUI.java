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

import nz.ac.waikato.modeljunit.gui.visualisaton.PanelJUNGVisualisation;

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
      mAppWindow.setJMenuBar(new ModelJUnitMenuBar());

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

   public void run() {
      mAppWindow.setVisible(true);

      Project pr = new Project();
      pr.setName("Test Project");
      pr.setFileName(new File("test.project"));
      pr.setProperty("foobar",new Integer(123));
      pr.setProperty("test","hello, world");
      Project.save(pr);
   }

   public static void main(String[] args) {
      ModelJUnitGUI gui = new ModelJUnitGUI();

      new Thread(gui).start();
   }
}
