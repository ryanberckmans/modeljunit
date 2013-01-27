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

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;

/**
 * A container class for projects.
 * 
 * Designed to contain preferences and configuration information, as well as logic for saving/opening projects from
 * files.
 * 
 * The static setInstance/getInstance methods give access to the singleton instance, which is the current project.
 * 
 * @author Gian Perrone <gian@waikato.ac.nz>
 **/

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Project {
    private String mProjectName; 
    private String mPackageLocation;
    /** Class name, includes the Package and the name of the class. */
    private String mClassName;
    /** The name of the file the project was loaded from. */
    private File mFile;
    /** Time when this project was last modified. */
    private Date mLastModified;
    private File mModelFile;
    /** This should be part of the configuration. */
    private int mAlgorithm;
    private int mWalkLength;

    @XmlTransient
    private boolean mSaved;
    @XmlTransient 
    private ClassLoader mModelClassLoader;
    @XmlTransient
    private ArrayList<Method> mArrayMethod = new ArrayList<Method>();
    @XmlTransient
    private IAlgorithmParameter mAlgo;
    @XmlTransient
    private Class<?> mModelClass;
    @XmlTransient
    private FsmModel mModelObject;
    /** The path to the jar file that contains the model, or {@link nz.ac.waikato.modeljunit.gui.ModelJUnitGUI#BUILTIN} for an example model. */
    
    /**
     * Should only be used by JAXB, when loading a project.
     */
    public Project() {
        mFile = null;
        mSaved = false;
        mProjectName = "untitled";
        mModelFile = null;
    }
    
    /** Create a new (empty) project, untitled and unsaved.  
     * @param jarName The jar file containing the model or {@link nz.ac.waikato.modeljunit.gui.ModelJUnitGUI#BUILTIN}
     * @param className Full class name of the model
     **/
    public Project(String jarName, String className) {
        this();
        setPackageLocation(jarName);
        setClassName(className);
    }

    public void setModelClassLoader(ClassLoader cl) {
        if (mModelClassLoader != null) {
            throw new IllegalStateException("Cannot set mModelClassLoader twice.");
        } else {
            mModelClassLoader = cl;
        }
    }

    public ClassLoader getModelClassLoader() {
        return mModelClassLoader;
    }

    /**
     * Sets the class name and loads that class and an instance of it.
     * @param cName
     */
    public void setClassName(String cName) {
        if (mClassName != null) {
            throw new IllegalStateException("Cannot set mClassName twice.");
        } else {
            mClassName = cName;
            if (getModelClassLoader() == null) {
                throw new IllegalStateException("No class loader or jar file specified.");
            }
            try {
                Class<?> clazz = getModelClassLoader().loadClass(cName);
                if (clazz == null) {
                    throw new RuntimeException("Error loading model " + cName);
                }
                setModelClass(clazz);
                nz.ac.waikato.modeljunit.FsmModel model = (nz.ac.waikato.modeljunit.FsmModel) getModelClass().newInstance();
                if (model == null) {
                    throw new RuntimeException("Error instantiating model " + cName); 
                }
                setModelObject(model);
                int actionNumber = 0;
                for (Method method : getModelClass().getMethods()) {
                    if (method.isAnnotationPresent(Action.class)) {
                        actionNumber++;
                        addMethod(method);
                    }
                }
                System.out.println("Added "+actionNumber+" actions.");
            } catch (ClassCastException ex) {
                ErrorMessage.DisplayErrorMessage("Wrong class (ClassCastException", "Please select FsmModel class."
                                + "\n Error in TestExeModel::loadModelClassFromFile: " + ex.getLocalizedMessage());
            } catch (InstantiationException ie) {
                ErrorMessage.DisplayErrorMessage("Model not initialized (InstantiationException)",
                                "Can not initialize model." + "\n Error in TestExeModel::loadModelClassFromFile: "
                                                + ie.getLocalizedMessage());
            } catch (IllegalAccessException iae) {
                ErrorMessage.DisplayErrorMessage("Cannot access model (IllegalAccessException)",
                                "Can not access model class." + "\n Error in TestExeModel::loadModelClassFromFile: "
                                                + iae.getLocalizedMessage());
            } catch (ClassNotFoundException ex) {
                ErrorMessage.DisplayErrorMessage("Cannot find model class (ClassNotFoundException)",
                                "Can not access model class." + "\n Error in TestExeModel::loadModelClassFromFile: "
                                                + ex.getLocalizedMessage());
            }
        }
    }
    
    public String getClassName() {
        return mClassName;
    }

    public void setAlgorithm(IAlgorithmParameter algo) {
        mAlgo = algo;
    }
    
    public IAlgorithmParameter getAlgo() {
        return mAlgo;
    }


    /** The path to the top-level package directory of the model. */
    public String getPackageLocation() {
        return mPackageLocation;
    }

    /** Set the path to the top-level package directory of the model. */
    public void setPackageLocation(String location) {
        System.out.println("SetPackageLocation to " + location);
        mPackageLocation = location;
        if (location.equals(ModelJUnitGUI.BUILTIN)) {
            mModelClassLoader = this.getClass().getClassLoader();
        } else {
            try {
                mModelClassLoader = URLClassLoader.newInstance(new URL[] { new URL(location) });
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot create class loader for jar file: " + location, e);
            }
        }
    }

    public void setModelClass(Class<?> mModelClass) {
        this.mModelClass = mModelClass;
    }

    public void setModelObject(FsmModel mModelObject) {
        this.mModelObject = mModelObject;
    }

    public Class<?> getModelClass() {
        return mModelClass;
    }

    public FsmModel getModelObject() {
        return mModelObject;
    }

    public boolean isModelLoaded() {
        if (mModelClass == null || mModelObject == null)
            return false;
        return true;
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

    // Add an action method into list
    public void addMethod(Method m) {
        mArrayMethod.add(m);
    }

    public int getMethodCount() {
        return mArrayMethod.size();
    }

    public void reset() {
        mArrayMethod.clear();
        //resetModelToNull();
    }
    
    public File getModelFile() {
        return mModelFile;
    }

    public void setModelFile(File m) {
        mModelFile = m;
        modify();
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

    /** Set a filename for the project to save to. **/
    public void setFileName(File file) {
        mFile = file;
    }

    /** Get the current filename for the project. **/
    public File getFileName() {
        return mFile;
    }

    public String getVersion() {
        return ModelJUnitGUI.MODELJUNIT_VERSION;
    }

    public void setVersion(String version) {
        if (!version.equals(ModelJUnitGUI.MODELJUNIT_VERSION)) {
            System.err.println("WARNING:  This project file was made using a different version of ModelJUnit, and may not load correctly.");
        }
    }

    public boolean[] getCoverageOptions() {
        return Parameter.getCoverageOption();
    }

    public void setCoverageOptions(boolean[] options) {
        Parameter.setCoverageOption(options);
    }

    public boolean getFailureVerbosity() {
        return Parameter.getFailureVerbosity();
    }

    public void setFailureVerbosity(boolean verbosity) {
        Parameter.setFailureVerbosity(verbosity);
    }

    public int getAlgorithm() {
        return mAlgorithm;
    }

    public void setAlgorithm(int algorithm) {
        mAlgorithm = algorithm;
    }

    public double getResetProbability() {
        return Parameter.getResetProbability();
    }

    public void setResetProbability(double prob) {
        Parameter.setResetProbability(prob);
    }

    public int getWalkLength() {
        return mWalkLength;
    }

    public void setWalkLength(int len) {
        mWalkLength = len;
    }

    public boolean getGenerateGraph() {
        return Parameter.getGenerateGraph();
    }

    public void setGenerateGraph(boolean generate) {
        Parameter.setGenerateGraph(generate);
    }

    /**
     * Save the project state to the currently set filename.
     * 
     * A null filename will result in an exception. Checking that the file is writable must be done prior to calling
     * this method.
     * 
     * @return true if writing the file succeeded, false otherwise
     **/
    public boolean save() {
        if (getFileName() == null)
            throw new RuntimeException("Filename for current project is null");

        try {
            FileOutputStream fo = new FileOutputStream(getFileName());
            JAXBContext context = JAXBContext.newInstance(Project.class);

            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            m.marshal(this, fo);

            fo.close();
        } catch (Exception e) {
            System.err.println("Project Save Failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static Project load(File file) {
        if (file == null)
            throw new RuntimeException("Invalid or missing project filename");

        Project result = null;

        try {
            JAXBContext context = JAXBContext.newInstance(Project.class);

            Unmarshaller m = context.createUnmarshaller();

            result = (Project) m.unmarshal(file);
            
            if (result == null)
                throw new RuntimeException("Error:  Could not load project from file");
        } catch (Exception e) {
            System.err.println("Project Load Failed: " + e.getMessage());
            return null;
        }

        return result;
    }

    @Override
    public String toString() {
        return "*** PROJECT NAME: " + getName() + "\n" +
                        "*** FILENAME: " + getFileName() + "\n" + 
                        "*** MODEL FILE: " + getModelFile() + "\n" + 
                        "*** VERSION: " + getVersion() + "\n" + 
                        "*** RESET PROBABILITY: " + getResetProbability() + "\n" + 
                        "*** WALK LENGTH: " + getWalkLength() + "\n" + 
                        "*** FAILURE VERBOSITY: " + getFailureVerbosity() + "\n" + 
                        "*** GENERATE GRAPH: " + getGenerateGraph() + "\n" +  
                        "*** HASHCODE: " + this.hashCode() + "\n" + 
                        "*** MODEL OBJECT: " + getModelObject() + "\n" +
                        "*** CLASS NAME: " + getClassName() + "\n" +
                        "*** CLASS LOADER: " + getModelClassLoader() + "\n" + 
                        "*** METHODS: " + mArrayMethod.size() + "\n"; 
    }
}
