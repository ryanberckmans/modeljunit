              -----------------------------------
                    Welcome to ParamEdit
              -----------------------------------

  ParamEdit is a tool for editing test parameters and suggesting additional parameter values.

  It provides several heuristics (including PairWise and MC/DC) for analyzing
  a set of input tuples and suggesting missing/extra values. 

  It is an open source tool, released under the GNU GPL license.
  It is in a very experimental state and is greatly lacking in documentation.

Compilation

  To compile ParamEdit you need Maven 3.0 or higher from maven.apache.org.
  Then you can run the following Maven command in this ModelJUnit directory
  (which should contain this README.txt file, a pom.xml file, and directories
  called jdsl and src). 

+-----------------+
    mvn install
+-----------------+

  This will compile ParamEdit, run its unit tests, and create a
  *.jar files in the <<<target>>> directory.  The *.jar with dependencies
  file is all you need to include ParamEdit in your own projects,
  or to run the GUI interface of ModelJUnit.

  However, if you want to view or edit the source code of ParamEdit,
  it is useful to set up an Eclipse project for it.  To do this, run
  the following Maven command:

+----------------------+
  mvn eclipse:eclipse -DdownloadSources=true
+----------------------+

Documentation

  Maven can also generate Javadocs.  Run this command

+----------------------+
  mvn javadoc:javadoc
+----------------------+

  then point your web browser at <<<target/site/apidocs/index.html>>>.
  Start by reading the "description" section in the
  nz.ac.waikato.modeljunit package.

Using ParamEdit

  Double-click on the paramedit-*-with-dependencies.jar file to run
  the ParamEdit GUI.  You should see an upper panel containing the input
  table and a lower panel where additional input tuples will be suggested.
  The first row of the input table defines the column
  names and the second line defines the types of those columns.
  The following rows are the input tuples that you have chosen to
  be inputs for your test. 

  Choosing one of the commands in the 'Suggestion' menu will display
  additional tuples in the suggestion panel.  (TODO: also display
  the rationale for each suggestion.)  If you like one of these
  suggestions, you can move it up to the top (input) panel by right-clicking
  and using the 'Add Suggestion' menu entry.  Then rerun the Suggestion command.

  There are examples of the input files in the paramedit/src/test/resources folder.

  BUG: the File/Save and File/Load commands do not work yet.
       Currently they try to use object serialization...]
  [TODO: make them read and write files in the same format as the example input files?
         See AbstractSuggestionStrategyTest.readTableIn(...)
  ]
         

