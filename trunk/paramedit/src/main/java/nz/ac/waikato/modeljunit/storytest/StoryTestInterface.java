package nz.ac.waikato.modeljunit.storytest;

import java.io.Serializable;

public interface StoryTestInterface
   extends Subject, Serializable
{
   public Object accept(StoryTestVisitor<?> visitor, Object other);
   
   public String toHTML();
}
