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
public class MCDCSuggestionStrategyTest
    extends AbstractSuggestionStrategyTest
{
  public final static String PATH = System.getProperty("user.dir") + "/src/test/resources/";
  public final static MCDCSuggestionStrategyFactory FACTORY = MCDCSuggestionStrategyFactory.INSTANCE;
  
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

  public void testRand()
    throws IOException, FileNotFoundException
  {
   compareValues(PATH + "MCDCRandTable.txt", PATH + "RandMCDC.txt", FACTORY);
  }

}
