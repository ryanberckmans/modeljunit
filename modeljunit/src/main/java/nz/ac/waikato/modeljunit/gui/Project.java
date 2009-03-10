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

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

/** A container class for projects.
 *
 * Designed to contain preferences and configuration information,
 * as well as logic for saving/opening projects from files.
 *
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/
public @XmlRootElement class Project
{
   private String mProjectName;
   private Map<String,Object> mConfiguration;
   private File mFile;
   private boolean mSaved;
   private Date mLastModified;

   /** Create a new (empty) project, untitled and unsaved **/
   public Project() {
      mFile = null;
      mSaved = false;
      mProjectName = "untitled";
      mConfiguration = new HashMap<String,Object>();
   }

   /** Update the project name **/
   public void setName(String name) {
      mProjectName = name;
      modify();
   }

   /** Get the project name **/
   public String getName() {
      return mProjectName;
   }

   /** Set the "modified" flag to indicate that the project is unsaved. **/
   public void modify() {
      mSaved = false;
      mLastModified = new Date();
   }

   /** Read the modified flag. **/
   public boolean isModified() {
      return !mSaved;
   }

   public Date getLastModified() {
      return mLastModified;
   }

   public void setLastModified(Date lastModified) {
      mLastModified = lastModified;
   }

   /** Set a configuration value. **/
   public void setProperty(String key,Object value) {
      mConfiguration.put(key,value);
   }

   /** Get a configuration value **/
   public Object getProperty(String key) {
      return mConfiguration.get(key);
   }

   /** Set a filename for the project to save to. **/
   public void setFileName(File file) {
      mFile = file;
   }

   /** Get the current filename for the project. **/
   public File getFileName() {
      return mFile;
   }

   /** Save the project state to the currently set filename. 
    *  
    *  A null filename will result in an exception.  Checking that the file is
    *  writable must be done prior to calling this method.
    *
    *  @return true if writing the file succeeded, false otherwise
    **/
   public static boolean save(Project project) {
      if(project == null) throw new RuntimeException("Cannot save a null project");
      if(project.getFileName() == null) throw new RuntimeException("Filename for current project is null");
     
      try {
         JAXBContext context = JAXBContext.newInstance(Project.class);

         Marshaller m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 

         m.marshal(project, System.out);
      } catch(Exception e) {
         System.err.println("Project Save Failed: " + e.getMessage());
         return false;
      }

      return true;
   }
 
}
