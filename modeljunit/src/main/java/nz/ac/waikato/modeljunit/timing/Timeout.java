
package nz.ac.waikato.modeljunit.timing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Timeouts. The parameter provided is the name of the action
 * method that will be called when the timeout expires.
 *
 * For example, a timeout is declared as:
 *
 * <pre>
 * &#064;Timeout(&quot;myAction&quot;)
 * public int myTimer;
 * </pre>
 *
 * The action <code>myAction</code> will be triggered when the timeout
 * expires. Timeouts follow the following conventions:
 *
 * <pre>
 * value == 0 or -1                     == Not Set
 * value &gt; 0 &amp;&amp; value &lt; current time    == Set
 * value &gt; current time                 == Expired
 * </pre>
 *
 * @author Scott Thompson
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Timeout {
  String value();
}
