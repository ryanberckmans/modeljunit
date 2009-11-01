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

import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import nz.ac.waikato.modeljunit.coverage.CoverageHistory;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.examples.FSM;

public class AllRoundTesterTest extends TestCase
{
  /** This tests a random walk, plus TransitionCoverage metric with history.*/
  public static void testAllRoundWalk()
  {
    AllRoundTester tester = new AllRoundTester(new FSM());
    CoverageHistory metric = new CoverageHistory(new TransitionCoverage(), 1);
    //tester.addListener(new VerboseListener());
    tester.addCoverageMetric(metric);
    tester.setLoopTolerance(1);
    tester.setRandom(new Random(3));
    tester.generate(10);
    int coverage = metric.getCoverage();
    Assert.assertEquals("Loop-limited Greedy Random Walk", tester.getName());
    Assert.assertEquals("This tester limits another tester (Greedy Random Walk)" +
        " so that it goes around loops a maximum number of times" +
        " (once by default).", tester.getDescription());
    Assert.assertEquals(1, tester.getLoopTolerance());
    Assert.assertEquals(5, coverage);
    Assert.assertEquals(5, metric.getMaximum());
    List<Integer> hist = metric.getHistory();
    Assert.assertNotNull(hist);
    Assert.assertEquals("Incorrect history size.", 15, hist.size());
    Assert.assertEquals(new Integer(0), hist.get(0));
    Assert.assertEquals(new Integer(coverage), hist.get(hist.size() - 1));

    // we print this just for interest
    System.out.println("Action coverage: " + metric.getPercentage());
    System.out.print("History: ");
    for (Integer cov : metric.getHistory())
      System.out.print(cov + ", ");
    System.out.println();

    metric.clear();
    hist = metric.getHistory();
    Assert.assertNotNull(hist);
    Assert.assertEquals("History not reset.", 1, hist.size());
    Assert.assertEquals(new Integer(0), hist.get(0));
  }
}
