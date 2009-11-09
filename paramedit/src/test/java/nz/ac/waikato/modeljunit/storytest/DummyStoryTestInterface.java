/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest;

import nz.ac.waikato.modeljunit.command.UndoInterface;
import nz.ac.waikato.modeljunit.command.UndoThing;

/**
 * @author root
 *
 */
public class DummyStoryTestInterface
    implements StoryTestGUIInterface
{
  UndoInterface mUndoInterface = new UndoThing();
  /* (non-Javadoc)
   * @see nz.ac.waikato.modeljunit.storytest.StoryTestGUIInterface#getStoryParent()
   */
  public StoryTestGUIInterface getStoryParent()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see nz.ac.waikato.modeljunit.storytest.StoryTestGUIInterface#getStoryTestInterface()
   */
  public StoryTestInterface getStoryTestInterface()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see nz.ac.waikato.modeljunit.storytest.StoryTestGUIInterface#getUndoInterface()
   */
  public UndoInterface getUndoInterface()
  {
    // TODO Auto-generated method stub
    return mUndoInterface;
  }

  /* (non-Javadoc)
   * @see nz.ac.waikato.modeljunit.storytest.StoryTestGUIInterface#requestSuggestions(nz.ac.waikato.modeljunit.storytest.StoryTestInterface)
   */
  public void requestSuggestions(StoryTestInterface story)
  {
    // TODO Auto-generated method stub

  }

}
