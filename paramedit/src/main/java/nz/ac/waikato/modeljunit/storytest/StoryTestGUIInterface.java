package nz.ac.waikato.modeljunit.storytest;

import nz.ac.waikato.modeljunit.command.UndoInterface;

public interface StoryTestGUIInterface
{
   public StoryTestInterface getStoryTestInterface();
   
   public UndoInterface getUndoInterface();
   
   public StoryTestGUIInterface getStoryParent();
   
   public void requestSuggestions(StoryTestInterface story);
}
