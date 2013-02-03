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
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.FsmModel;
import nz.ac.waikato.modeljunit.RandomTester;

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
@XmlAccessorType(XmlAccessType.NONE)
public class Project implements Cloneable {    
    private String mProjectName;
    
    /** The path to the jar file that contains the model, or {@link nz.ac.waikato.modeljunit.gui.ModelJUnitGUI#BUILTIN} for an example model. */
    private String mPackageLocation;
    
    /** Class name, includes the Package and the name of the class. */
    private String mClassName;
    
    /** This should be part of the configuration. */
    @XmlElement(name = "algorithm")
    private int mAlgorithm;
    
    @XmlElement(name = "walkLength")
    private int mWalkLength;
    
    /**
     * Testing parameters, dot graph
     **/
    @XmlElement(name = "generateGraph")
    private boolean mGenerateGraph;
    
    /**
     * Test generation verbosity whether user wants show verbosity or not
     */
    @XmlElement(name = "verbosity")
    private boolean mVerbosity = true;

    /**
     * Test failure verbosity
     * 
     */
    @XmlElement(name = "failureVerbosity")
    private boolean mFailureVerbosity;
    
    /**
     * Algorithm name When user select new algorithm from GUI, this value will be changed.
     */
    @XmlElement(name = "algorithmName")
    private String mAlgorithmName;
    
    /**
     * Transition Coverage options 0. State coverage 1. Transition coverage 2. Transition pair coverage 3. Action
     * coverage
     */
    @XmlElement(name = "coverageOption")
    private boolean[] mCoverageOption = new boolean[Parameter.NUM_COVERAGE];
    
    @XmlElement(name = "resetProbability")
    private double mResetProbability = RandomTester.DEFAULT_RESET_PROBABILITY;
    
    @XmlTransient//(name = "algorithmParameters")
    private IAlgorithmParameter mAlgo;
    
    @XmlTransient
    private File mModelFile;
    /** The name of the file the project was loaded from. */
    @XmlTransient
    private File mFile;
    /** Time when this project was last modified. */
    @XmlTransient
    private Date mLastModified;
    @XmlTransient
    private boolean mSaved;
    @XmlTransient 
    private ClassLoader mModelClassLoader;
    @XmlTransient
    private ArrayList<Method> mArrayMethod = new ArrayList<Method>();
    @XmlTransient
    private Class<?> mModelClass;
    @XmlTransient
    private FsmModel mModelObject;
    
    /**
     * Should only be used by JAXB, when loading a project.
     */
    public Project() {
//        mFile = null;
//        mSaved = false;
//        mProjectName = "untitled";
//        mModelFile = null;
//        mWalkLength = 10;
    }
    
    /** Create a new (empty) project, untitled and unsaved.  
     * @param jarName The jar file containing the model or {@link nz.ac.waikato.modeljunit.gui.ModelJUnitGUI#BUILTIN}
     * @param className Full class name of the model
     **/
    public Project(String jarName, String className) {
        this();
        mWalkLength = 10;
        mProjectName = "untitled";
        mCoverageOption[0] = true;
        mCoverageOption[1] = true;
        setPackageLocation(jarName);
        setClassName(className);
    }

    /** The path to the top-level package directory of the model. */
    @XmlElement
    public String getPackageLocation() {
        return mPackageLocation;
    }
    
    /** Set the path to the top-level package directory of the model. */
    public void setPackageLocation(String location) {
        mPackageLocation = location;
        if (mClassName != null) {
            createModel();
        }
    }

    protected void createModel() {
        if (mPackageLocation.equals(ModelJUnitGUI.BUILTIN)) {
            mModelClassLoader = this.getClass().getClassLoader();
        } else {
            try {
                mModelClassLoader = URLClassLoader.newInstance(new URL[] { new URL(mPackageLocation) });
            } catch (MalformedURLException e) {
                throw new RuntimeException("Cannot create class loader for jar file: " + mPackageLocation, e);
            }
        }
        try {
            Class<?> clazz = mModelClassLoader.loadClass(mClassName);
            if (clazz == null) {
                throw new RuntimeException("Error loading model " + mClassName);
            }
            setModelClass(clazz);
            nz.ac.waikato.modeljunit.FsmModel model = (nz.ac.waikato.modeljunit.FsmModel) getModelClass().newInstance();
            if (model == null) {
                throw new RuntimeException("Error instantiating model " + mClassName); 
            }
            setModelObject(model);
            for (Method method : getModelClass().getMethods()) {
                if (method.isAnnotationPresent(Action.class)) {
                    addMethod(method);
                }
            }
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
    
    @XmlElement
    public String getClassName() {
        return mClassName;
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
            if (mPackageLocation != null) {
                createModel();
            }
        }
    }
    
    public IAlgorithmParameter getAlgo() {
        return mAlgo;
    }
    
    public void setAlgorithm(IAlgorithmParameter algo) {
        mAlgo = algo;
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

    public boolean[] getCoverageOption() {
        return mCoverageOption;
    }

    public void setCoverageOption(boolean[] options) {
        mCoverageOption = options;
    }

    public boolean getVerbosity() {
        return mVerbosity;
    }

    public void setVerbosity(boolean verb) {
        mVerbosity = verb;
    }
    
    public boolean getFailureVerbosity() {
        return mFailureVerbosity;
    }

    public void setFailureVerbosity(boolean verb) {
        mFailureVerbosity = verb;
    }
    
    public String getAlgorithmName() {
        return mAlgorithmName;
    }

    public void setAlgorithmName(String algName) {
        mAlgorithmName = algName;
    }

    public int getAlgorithm() {
        return mAlgorithm;
    }

    public void setAlgorithm(int algorithm) {
        mAlgorithm = algorithm;
    }

    public double getResetProbability() {
        return mResetProbability;
    }

    /**
     * Reset probability
     * 
     * Set the probability of doing a reset during random walks. Note that the average length of each test sequence will
     * be roughly proportional to the inverse of this probability.
     * 
     * If this is set to 0.0, then resets will only be done when we reach a dead-end state (no enabled actions). This
     * means that if the FSM contains a loop that does not have a path back to the initial state, then the random walks
     * may get caught in that loop forever. For this reason, a non-zero probability is recommended.
     */
    public void setResetProbability(double probability) {
        mResetProbability = probability;
    }

    public int getWalkLength() {
        return mWalkLength;
    }

    public void setWalkLength(int len) {
        mWalkLength = len;
    }

    public boolean getGenerateGraph() {
        return mGenerateGraph;
    }

    public void setGenerateGraph(boolean generate) {
        mGenerateGraph = generate;
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
        } catch (JAXBException e) {
            ErrorMessage.DisplayErrorMessage("Save Failed", "Project Save Failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            ErrorMessage.DisplayErrorMessage("Save Failed", "Project Save Failed: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static Project load(File file) throws JAXBException {
        if (file == null)
            throw new RuntimeException("Invalid or missing project filename");

        Project result = null;
        JAXBContext context = JAXBContext.newInstance(Project.class);

        Unmarshaller m = context.createUnmarshaller();
        result = (Project) m.unmarshal(file);
        if (result == null) {
            throw new RuntimeException("Error:  Could not load project from file");
        }
        return result;
    }

    @Override
    public String toString() {
        return "*** PROJECT NAME: " + getName() + "\n" +
                        "*** PACKAGE LOCATION: " + getPackageLocation() + "\n" +
                        "*** FILENAME: " + getFileName() + "\n" + 
                        "*** MODEL FILE: " + getModelFile() + "\n" + 
                        "*** VERSION: " + getVersion() + "\n" +
                        "*** ALGORITHM: " + getAlgorithm() + "\n" + 
                        "*** ALGORITHM NAME: " + getAlgorithmName() + "\n" + 
                        "*** ALGORITHM OBJECT: " + getAlgo() + "\n" + 
                        "*** RESET PROBABILITY: " + getResetProbability() + "\n" + 
                        "*** WALK LENGTH: " + getWalkLength() + "\n" + 
                        "*** TEST GENERATION VERBOSITY: " + getVerbosity() + "\n" + 
                        "*** FAILURE VERBOSITY: " + getFailureVerbosity() + "\n" + 
                        "*** GENERATE GRAPH: " + getGenerateGraph() + "\n" +  
                        "*** HASHCODE: " + this.hashCode() + "\n" + 
                        "*** MODEL OBJECT: " + getModelObject() + "\n" +
                        "*** CLASS NAME: " + getClassName() + "\n" +
                        "*** CLASS LOADER: " + mModelClassLoader + "\n" + 
                        "*** COVERAGE OPTIONS: " + Arrays.toString(getCoverageOption()) + "\n" + 
                        "*** METHODS: " + mArrayMethod.size() + "\n"; 
    }

    @Override
    public Project clone() {
        try {
            Project project = (Project) super.clone();
            project.mCoverageOption = project.mCoverageOption.clone();
            return project;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning project", e);
        }
    }
}
