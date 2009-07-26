package nz.ac.waikato.modeljunit.storytest;

public class Tuple<S1, S2>
{
   final S1 s1;
   final S2 s2;
   
   public Tuple(S1 ps1, S2 ps2)
   {
      s1 = ps1;
      s2 = ps2;
   }
}