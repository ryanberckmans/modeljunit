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
public class PairWiseSuggestionStrategyTest
    extends AbstractSuggestionStrategyTest
{
  public final static String PATH = System.getProperty("user.dir") + "/src/test/resources/";
  public final static PairWiseSuggestionStrategyFactory FACTORY = PairWiseSuggestionStrategyFactory.INSTANCE;
  
  public void testEmpty()
    throws IOException, FileNotFoundException
  {
    compareValues(PATH + "EmptyTable.txt", PATH + "Empty.txt", FACTORY);
  }
  
  public void testStandard()
    throws IOException, FileNotFoundException
  {
    compareValues(PATH + "StandardTable.txt", PATH + "Empty.txt", FACTORY);
  }

  // TODO: figure out more thorough input data and output data for this test!
  //       Why do the RandPair lines start with '--' rather than a pipe character?
  public void testRand()
    throws IOException, FileNotFoundException
  {
   compareValues(PATH + "randtable.txt", PATH + "RandPair.txt", FACTORY);
  }

}
