package nz.ac.waikato.modeljunit.storytest;

public interface StoryTestInterface
   extends Subject
{
   public Object accept(StoryTestVisitor<?> visitor, Object other);
}
