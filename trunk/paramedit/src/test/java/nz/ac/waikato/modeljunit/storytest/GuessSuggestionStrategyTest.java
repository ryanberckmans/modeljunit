/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author root
 *
 */
public class GuessSuggestionStrategyTest
    extends AbstractSuggestionStrategyTest
{
  public final static String PATH = System.getProperty("user.dir") + "/src/test/resources/";
  public final static GuessSuggestionStrategyFactory FACTORY = GuessSuggestionStrategyFactory.INSTANCE;
  
  public void testEmpty()
    throws IOException, FileNotFoundException
  {
    compareValues(PATH + "EmptyTable.txt", PATH + "Empty.txt", FACTORY);
  }
  
  public void testStandard()
    throws IOException, FileNotFoundException
  {
    compareValues(PATH + "StandardTable.txt", PATH + "StandardGuess.txt", FACTORY);
  }

  // TODO: figure out better inputs for this test.
  //       (Every input table seems to generate no suggestions!)
  public void testRand()
    throws IOException, FileNotFoundException
  {
   compareValues(PATH + "randtable.txt", PATH + "RandGuess.txt", FACTORY);
  }
}
