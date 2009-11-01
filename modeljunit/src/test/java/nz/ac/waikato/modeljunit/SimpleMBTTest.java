package nz.ac.waikato.modeljunit;

import static org.junit.Assert.*;

import nz.ac.waikato.modeljunit.examples.FSM;

import org.junit.Test;

public class SimpleMBTTest 
{
  @Test	
  public void testMBTTest() {
	SimpleMBT tester = new SimpleMBT(new FSM());
	try {
	  String action = tester.generate();
	  assertTrue(action.startsWith("action") || action.startsWith("reset"));
	} catch (Exception e) {
	  e.printStackTrace();
	}
  }
}
