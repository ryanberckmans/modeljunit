package nz.ac.waikato.modeljunit.command;

import nz.ac.waikato.modeljunit.storytest.StoryTest;
import nz.ac.waikato.modeljunit.storytest.StoryTestInterface;

public class RemoveCalcTableCommand
   extends AbstractUndoableCommand
{
   public static final long serialVersionUID = 1;
   
   private final StoryTest mStory;
   private final StoryTestInterface mTable;
   private final int mIndex;
/**
Basic constructor for SetValueCommand
*/
   public RemoveCalcTableCommand(StoryTest story, StoryTestInterface Table)
   {
      mStory = story;
      mTable = Table;
      mIndex = story.getComponents().indexOf(Table);
   }
   
   public void execute()
   {
      mStory.remove(mIndex);
   }
   
   public void undo()
   {
      super.undo();
      mStory.add(mIndex, mTable);
   }
   
   public String getName()
   {
      return "Add Component";
   }
}
