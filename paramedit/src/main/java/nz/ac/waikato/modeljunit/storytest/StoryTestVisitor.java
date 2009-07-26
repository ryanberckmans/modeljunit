package nz.ac.waikato.modeljunit.storytest;

public interface StoryTestVisitor<T>
{
   public T visit(StoryTestInterface story, Object other);
   
   public T visit(CalcTable table, Object other);
   
   public T visit(StoryTest story, Object other);
}
