package nz.ac.waikato.modeljunit.storytest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoryTest
   extends AbstractSubject
   implements StoryTestInterface
{
   /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private List<StoryTestInterface> mComponents;
   
   public StoryTest()
   {
      mComponents = new ArrayList<StoryTestInterface>();
   }
   
   public void add(int index, StoryTestInterface si)
   {
      mComponents.add(index, si);
      inform();
   }
   
   public void add(StoryTestInterface si)
   {
      add(mComponents.size(), si);
   }
   
   public void remove(int index)
   {
      mComponents.remove(index);
      inform();
   }
   
   public List<StoryTestInterface> getComponents()
   {
      return Collections.unmodifiableList(mComponents);
   }
   
   public StoryTestInterface getComponent(int index)
   {
      return mComponents.get(index);
   }
   
   public Object accept(StoryTestVisitor<?> visitor, Object other)
   {
      return visitor.visit(this, other);
   }
}
