package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.StoryTest;
import nz.ac.waikato.modeljunit.storytest.StoryTestInterface;

public class CreateCalcTableCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final StoryTestInterface mTable;
   private final StoryTest mStory;
   private final int mIndex;
/**
Basic constructor for SetValueCommand
*/
   public CreateCalcTableCommand(StoryTest story, StoryTestInterface Table, int index)
   {
      mStory = story;
      mTable = Table;
      mIndex = index;
   }
   
   public CreateCalcTableCommand(StoryTest story, StoryTestInterface Table)
   {
      this(story, Table, story.getComponents().size());
   }
   
   public void execute()
   {
      mStory.add(mIndex, mTable);
   }
   
   public void undo()
   {
      super.undo();
      mStory.remove(mIndex);
   }
   
   public String getName()
   {
      return "Add Component";
   }
}
