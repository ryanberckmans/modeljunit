              -----------------------------------
                    Welcome to ModelJUnit
              -----------------------------------

  ModelJUnit is a Java library that extends JUnit to support model-based
  testing.  It is an open source tool, released under the GNU GPL license.

  ModelJUnit allows you to write simple FSM or EFSM models as Java classes,
  then generate tests from those models and measure various model coverage
  metrics.  The principles behind ModelJUnit are described in Sections 5.2
  and 5.3 of our book: {{{http://www.cs.waikato.ac.nz/~marku/mbt} Mark Utting
  and Bruno Legeard: <Practical Model-Based Testing: A Tools Approach>,
  Elsevier 2007}}.  The following tutorial chapter is the most comprehensive
  introduction to ModelJUnit:

  Mark Utting.
  "How to design extended finite state machine test models in Java."
  In Justyna Zander, Ina Schieferdecker, and Pieter J. Mosterman,
  editors, Model-Based Testing for Embedded Systems, chapter 6, pages
  147-170. CRC Press, Taylor and Francis Group, Boca Raton, FL, 2012.  

  This is version 2.3 of ModelJUnit.  Note that it uses the annotations
  feature of Java 5.0, so requires JDK 1.5 or higher to run.  JDK 1.7 is
  recommended.

  (But note that the GUI tests currently require a JDK 1.6,
  due to a bug in the Uispec4j libraries.  So, if you want to run
  the nz.ac.waikato.modeljunit.gui tests, you should first change your
  default JRE (see Eclipse Preferences / Java / Installed JREs)
  to a Java 1.6.x JRE.)


Compilation

  To compile ModelJUnit you need Maven 3.0 or higher from maven.apache.org.
  Then you can run the following Maven command in this ModelJUnit directory
  (which should contain this README.txt file, a pom.xml file, and directories
  called jdsl and src). 

+-----------------+
    mvn install
+-----------------+

  This will compile ModelJUnit, run its unit tests, and create a
  modeljunit.jar file in the <<<target>>> directory.  This modeljunit.jar
  file is all you need to include ModelJUnit in your own projects,
  or to run the GUI interface of ModelJUnit.

  However, if you want to view or edit the source code of ModelJUnit,
  it is useful to set up an Eclipse project for it.  To do this, run
  the following Maven command:

+----------------------+
  mvn eclipse:eclipse -DdownloadSources=true
+----------------------+

Documentation

  Maven can also generate Javadocs for ModelJUnit.  Run this command

+----------------------+
  mvn javadoc:javadoc
+----------------------+

  then point your web browser at <<<target/site/apidocs/index.html>>>.
  Start by reading the "description" section in the
  nz.ac.waikato.modeljunit package.

  These Java docs are also available from
  {{{http://www.cs.waikato.ac.nz/~marku/mbt/modeljunit} here}}.


Using ModelJUnit

  Double-click on the modeljunit.jar file to run the ModelJUnit GUI.
  (If you've built ModelJUnit yourself, use the '-with-dependencies'
  .jar file in the <<<target>>> directory.)
  You can immediately experiment with generating tests from any of the
  example models.  The source code for these example models is in the
  <<<src/main/java/nz/ac/modeljunit/examples>>>
  directory of this distribution.

  To load your own model, you should write your model class so that
  it implements the <<<nz.ac.waikato.modeljunit.FsmModel>>> interface.
  It is recommended to add a simple <<<main>>> method like the following,
  and then export your model as a Runnable Jar File.

+----------------------+
  public static void main(String args[])
  {
    // create your model and a test generation algorithm
    Tester tester = new RandomTester(new YourModel());

    // ask to print the generated tests
    tester.addListener("verbose");

    // generate a small test suite of 20 steps
    tester.generate(20);
  }
+----------------------+

  Then you can execute your .jar file to generate tests, or you can
  run the ModelJUnit GUI, create a new project and load in your .jar
  file to explore your model and experiment with different test generation
  strategies and options.  Note that the 'Test Configuration' dialogue has
  a right-hand panel that shows you a Java 'main' method to set up and run
  your current configuration.

