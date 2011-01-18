
package nz.ac.waikato.modeljunit.gui;

import java.net.URL;
import java.net.URLClassLoader;

/*
 * ClassFileLoader.java
 * @author rong ID : 1005450 15th Aug 2007
 */
public class ClassFileLoader extends ClassLoader
{
  private static ClassFileLoader m_loader;

  private ClassFileLoader()
  {
  }

  public static ClassFileLoader getInstance()
  {
    if (m_loader == null)
      m_loader = new ClassFileLoader();
    return m_loader;
  }

  public static int runtime = 0;

  public Class<?> loadClass(String classname)
  {
    // http://forum.java.sun.com/thread.jspa?threadID=568853&messageID=2815072
    // that thread explained why i have to add file://C in front of strPL
    // java doc does not explain :(
    String strPL = "file://" + Parameter.getPackageLocation();
    String strPN = Parameter.getPackageName();
    if (strPN != null && strPN.length() > 0 && strPN.charAt(strPN.length() - 1) != '.')
      strPN = strPN + ".";

    System.out.println("**** Loading: PL: " + strPL);
    System.out.println("**** Loading: PN: " + strPN);

    Class<?> modelclass = null;
    // Create the class loader by using the given URL
    if (strPL != null && strPL.length() > 0) {
      try {
        modelclass = URLClassLoader.newInstance(new URL[]{new URL(strPL)})
	    .loadClass(strPN + classname);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (modelclass == null) {
	System.out.println("NULL model class was returned!!!");
    }
    return modelclass;
  }
}
