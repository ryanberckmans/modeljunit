package nz.ac.waikato.modeljunit.storytest;

import java.util.List;
import java.util.ArrayList;

public class AbstractSubject
   implements Subject
{
   private final List<Observer> mObservers = new ArrayList<Observer>();
   
   public void registerObserver(Observer o)
   {
      mObservers.add(o);
   }
   
   public void deregisterObserver(Observer o)
   {
      mObservers.remove(o);
   }
   
   public void inform()
   {
      for (Observer o: mObservers) {
         o.update();
      }
   }
}
