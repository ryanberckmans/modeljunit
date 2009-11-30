/**
 Copyright (C) 2007 Mark Utting
 This file is part of the CZT project.

 The CZT project contains free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 The CZT project is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with CZT; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package nz.ac.waikato.modeljunit;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import nz.ac.waikato.modeljunit.coverage.CoverageMetric;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.examples.SimpleSet;
import junit.framework.TestCase;

/**
 *  Unit tests for the lookahead tester.
 * @author marku
 *
 */
public class LookaheadTesterTest extends TestCase
{
  protected LookaheadTester tester;
  protected List<Transition> path;
  protected CoverageMetric transitions;

  public void setUp()
  {
    tester = new LookaheadTester(new SimpleSet());
    transitions = tester.addCoverageMetric(new TransitionCoverage());
    path = new ArrayList<Transition>();
    tester.addListener(new AbstractListener() {
      private Object lastState = "";
      public String getName()
      {
        return "path listener";
      }
      @Override
      public void doneTransition(int action, Transition tr)
      {
        path.add(tr);
        lastState = tr.getEndState();
        //System.out.println(path.size());
      }
      @Override
      public void doneReset(String reason, boolean testing)
      {
        Object initial = model_.getCurrentState();
        path.add(new Transition(lastState, "reset", initial));
        lastState = initial;
      }
    }
    );
  }
  
  public void testDepth()
  {
    tester.setDepth(10);
    assertEquals(10, tester.getDepth());
    tester.setDepth(1);
    assertEquals(1, tester.getDepth());
  }

  public void testNewActionValue() {
    tester.setNewActionValue(10);
    assertEquals(10, tester.getNewActionValue());
  }
  
  public void testNewTransValue() {
    tester.setNewTransValue(10);
    assertEquals(10, tester.getNewTransValue());
  }
  
  public void testGraph() throws FileNotFoundException
  {
    Tester test = new GreedyTester(new SimpleSet());
    GraphListener graph = (GraphListener) test.addListener("graph");
    test.buildGraph();
    graph.printGraphDot("simpleset.dot");
  }

  public void testGenerate1()
  {
    tester.setDepth(1);
    tester.setNewActionValue(tester.getNewTransValue());
    tester.generate(17);
    // impressive: this covers all 16 transitions with a walk of just 17.
    assertEquals(17, path.size());
    assertEquals("(FF, addS1, TF)", path.get(0).toString());
    assertEquals("(TF, addS1, TF)", path.get(1).toString());
    assertEquals("(TF, addS2, TT)", path.get(2).toString());
    assertEquals("(TT, addS1, TT)", path.get(3).toString());
    assertEquals("(TT, addS2, TT)", path.get(4).toString());
    assertEquals("(TT, removeS1, FT)", path.get(5).toString());
    assertEquals("(FT, addS1, TT)", path.get(6).toString());
    assertEquals("(TT, removeS2, TF)", path.get(7).toString());
    assertEquals("(TF, removeS1, FF)", path.get(8).toString());
    assertEquals("(FF, addS2, FT)", path.get(9).toString());
    assertEquals("(FT, addS2, FT)", path.get(10).toString());
    assertEquals("(FT, removeS1, FT)", path.get(11).toString());
    assertEquals("(FT, removeS2, FF)", path.get(12).toString());
    assertEquals("(FF, removeS1, FF)", path.get(13).toString());
    assertEquals("(FF, removeS2, FF)", path.get(14).toString());
    assertEquals("(FF, reset, FF)", path.get(15).toString());
    assertEquals("(FF, reset, FF)", path.get(16).toString());
    assertEquals(15, transitions.getCoverage());
  }

  public void testGenerate2()
  {
    tester.setDepth(3);
    tester.generate(19);
    //
    assertEquals(19, path.size());
    assertEquals("(FF, addS1, TF)", path.get(0).toString());
    assertEquals("(TF, addS2, TT)", path.get(1).toString());
    assertEquals("(TT, removeS1, FT)", path.get(2).toString());
    assertEquals("(FT, removeS2, FF)", path.get(3).toString());
    assertEquals("(FF, addS2, FT)", path.get(4).toString());
    assertEquals("(FT, addS1, TT)", path.get(5).toString());
    assertEquals("(TT, addS1, TT)", path.get(6).toString());
    assertEquals("(TT, addS2, TT)", path.get(7).toString());
    assertEquals("(TT, removeS2, TF)", path.get(8).toString());
    assertEquals("(TF, addS1, TF)", path.get(9).toString());
    assertEquals("(TF, removeS1, FF)", path.get(10).toString());
    assertEquals("(FF, removeS1, FF)", path.get(11).toString());
    assertEquals("(FF, removeS2, FF)", path.get(12).toString());
    assertEquals("(FF, addS1, TF)", path.get(13).toString());
    assertEquals("(TF, removeS2, TF)", path.get(14).toString());
    assertEquals("(TF, addS2, TT)", path.get(15).toString());
    assertEquals("(TT, removeS1, FT)", path.get(16).toString());
    assertEquals("(FT, addS2, FT)", path.get(17).toString());
    assertEquals("(FT, removeS1, FT)", path.get(18).toString());
    assertEquals(16, transitions.getCoverage());
  }

  public void testMaxLength()
  {
    tester.setDepth(3);
    tester.setMaxLength(4);
    tester.generate(30);
    //
    assertEquals("(FF, addS1, TF)", path.get(0).toString());
    assertEquals("(TF, addS2, TT)", path.get(1).toString());
    assertEquals("(TT, removeS1, FT)", path.get(2).toString());
    assertEquals("(FT, removeS2, FF)", path.get(3).toString());

    assertEquals("(FF, reset, FF)", path.get(4).toString());
    assertEquals("(FF, addS2, FT)", path.get(5).toString());
    assertEquals("(FT, addS1, TT)", path.get(6).toString());
    assertEquals("(TT, addS1, TT)", path.get(7).toString());
    assertEquals("(TT, addS2, TT)", path.get(8).toString());

    assertEquals("(TT, reset, FF)", path.get(9).toString());
    assertEquals("(FF, removeS1, FF)", path.get(10).toString());
    assertEquals("(FF, removeS2, FF)", path.get(11).toString());
    assertEquals("(FF, addS1, TF)", path.get(12).toString());
    assertEquals("(TF, addS1, TF)", path.get(13).toString());

    assertEquals("(TF, reset, FF)", path.get(14).toString());
    assertEquals("(FF, addS2, FT)", path.get(15).toString());
    assertEquals("(FT, addS2, FT)", path.get(16).toString());
    assertEquals("(FT, removeS1, FT)", path.get(17).toString());
    assertEquals("(FT, addS1, TT)", path.get(18).toString());

    assertEquals("(TT, reset, FF)", path.get(19).toString());
    assertEquals("(FF, addS1, TF)", path.get(20).toString());
    assertEquals("(TF, removeS1, FF)", path.get(21).toString());
    assertEquals("(FF, addS1, TF)", path.get(22).toString());
    assertEquals("(TF, removeS2, TF)", path.get(23).toString());

    assertEquals("(TF, reset, FF)", path.get(24).toString());
    assertEquals("(FF, addS2, FT)", path.get(25).toString());
    assertEquals("(FT, addS1, TT)", path.get(26).toString());
    assertEquals("(TT, removeS2, TF)", path.get(27).toString());
    assertEquals("(TF, reset, FF)", path.get(28).toString());

    assertEquals(16, transitions.getCoverage());
    assertEquals(4, tester.getMaxLength());
  }
  
  public void testConstructor() {
    LookaheadTester tester = new LookaheadTester(new Model(new SimpleSet()));
    assertEquals("Lookahead Walk", tester.getName());
    assertEquals("At each state, this test generator looks ahead through all" +
        " the known paths, upto a fixed DEPTH, to find the path" +
        " that leads to an Action or a Transition that has not yet" +
        " been explored.  Can be exponentially slow if DEPTH is set" +
        " too large.", tester.getDescription());
  }
}
