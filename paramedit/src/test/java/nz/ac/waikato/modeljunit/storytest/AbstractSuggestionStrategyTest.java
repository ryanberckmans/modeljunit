/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author root
 *
 */
public abstract class AbstractSuggestionStrategyTest
    extends TestCase
{ 
  public void compareValues(String table, String suggestions, SuggestionStrategyFactory factory)
    throws FileNotFoundException, IOException
  {
    CalcTable calctable = readCalcTable(table);
    Set<List<String>> expectedSuggestions = readSuggestionSet(suggestions);
    SuggestionStrategy strat = factory.createSuggestionStrategy(calctable);
    Set<List<String>> actualSuggestions = getSuggestionSet(strat);
    Set<List<String>> tex = new HashSet<List<String>>(expectedSuggestions);
    Set<List<String>> tac = new HashSet<List<String>>(actualSuggestions);
    tex.removeAll(actualSuggestions);
    tac.removeAll(expectedSuggestions);
//    System.out.println("expected");
//    System.out.println(tex);
//    System.out.println("actual");
//    System.out.println(tac);
    assertEquals(expectedSuggestions, actualSuggestions);
  }

  public static List<List<String>> readTableIn(BufferedReader reader)
    throws IOException
  {
    List<List<String>> rows = new ArrayList<List<String>>();
    while (reader.ready()) {
      String line = reader.readLine();
      String[] vals = line.split("\\|");
      //System.out.println(Arrays.toString(vals));
      if (vals.length == 1 && vals[0].equals("")) {continue;}
      rows.add(Arrays.asList(vals));
    }
    return rows;
  }
  
  public static CalcTable createTable(List<List<String>> rows)
  {
    CalcTable res = new CalcTable("table", rows.get(0));
    res.removeRow(0);
    for (int i = 1; i < rows.size(); i++) {
      res.addRow(rows.get(i));
    }
    return res;
  }
  
  public static List<List<String>> readFromFile(String file)
    throws IOException, FileNotFoundException
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    return readTableIn(reader);
  }
  
  public static Set<List<String>> readSuggestionSet(String file)
    throws IOException, FileNotFoundException
  {
    return new HashSet<List<String>>(readFromFile(file));
  }
  
  public static CalcTable readCalcTable(String file)
    throws IOException, FileNotFoundException
  {
    return createTable(readFromFile(file));
  }
  
  public static Set<List<String>> getSuggestionSet(SuggestionStrategy strat)
  {
    List<Suggestion> suggestions = strat.getSuggestions();
    Set<List<String>> res = new HashSet<List<String>>();
    for (Suggestion sug : suggestions) {
      res.add(sug.getFields());
    }
    return res;
  }
}
