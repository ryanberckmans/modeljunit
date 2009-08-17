package nz.ac.waikato.modeljunit.examples.gsm;

public class SimFile
{
  public SimCard.File_Type type;
  public SimCard.F_Name name;
  public String data;
  public SimCard.Permission perm_read;
  public SimFile parent;   // null means this is the root file (MF).
  
  public SimFile(
      SimCard.File_Type type0,
      SimCard.F_Name name0,
      String data0,
      SimCard.Permission perm0,
      SimFile parent0)
  {
    type = type0;
    name = name0;
    data = data0;
    perm_read = perm0;
    parent = parent0;
  }
}
