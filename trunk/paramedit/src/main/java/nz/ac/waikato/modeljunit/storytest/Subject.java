package nz.ac.waikato.modeljunit.storytest;

public interface Subject
{
   public void registerObserver(Observer o);
   
   public void deregisterObserver(Observer o);
   
   public void inform();
}
