package nz.ac.waikato.modeljunit.coverage;

import static org.junit.Assert.*;

import nz.ac.waikato.modeljunit.Transition;

import org.junit.Test;

/**
 * Tests the abstract coverage metric, and can be subclassed to test some other metrics.
 * 
 * Subclasses should override at least {@link #createCoverage() createCoverage}
 * and {@link #getItem(Transition) getItem} (which maps a transition into the item we want to cover).
 *
 * @author Mark.Utting
 *
 */
public class AbstractCoverageTest {

    public static final Transition TRANS_AB = new Transition("A", "ab", "B");
    public static final Transition TRANS_BA = new Transition("B", "ba", "A");
    public static final Transition TRANS_NEVER = new Transition("Y", "junk", "Z"); // never covered
    
    protected class TestCoverage extends AbstractCoverage {
        
        @Override
        public void doneTransition(int action, Transition tr) {
            this.incrementItem(tr);
        }
        
        @Override
        public String getDescription() {
            return "Description";
        }

        @Override
        public String getName() {
            return "Name";
        }
        
    }
    
    /** @return the system under test. */
    protected AbstractCoverage createCoverage() {
        return new TestCoverage();
    }

    /**
     * Extract the coverage object of interest.
     * 
     * @param tr a Transition
     * @return the part of the transition that we want to cover.
     */
    protected Object getItem(Transition tr) {
        return tr;
    }

    @Test
    public void testEmpty() {
        AbstractCoverage coverage = createCoverage();
        assertEquals(0, coverage.getCoverage());
        assertEquals(-1, coverage.getMaximum());
        assertEquals(0.0, coverage.getPercentage(), 0.001);
    }

    @Test
    public void testCoverage() {
        checkCoverage(2, 1);
    }

    public void checkCoverage(int ab, int ba) {
        AbstractCoverage coverage = createCoverage();
        shortWalk(coverage);
        assertEquals(2, coverage.getCoverage());
        assertEquals(Integer.valueOf(ab), coverage.getDetails().get(getItem(TRANS_AB)));
        assertEquals(Integer.valueOf(ba), coverage.getDetails().get(getItem(TRANS_BA)));
        assertEquals(-1, coverage.getMaximum());
        assertEquals(100.0 * 2 / (2 + 100.0), coverage.getPercentage(), 0.001);
        for (int i = 0; i < 50; i++) {            
            shortWalk(coverage);
        }
        assertEquals(2, coverage.getCoverage());
        assertEquals(Integer.valueOf(ab * 51), coverage.getDetails().get(getItem(TRANS_AB)));
        assertEquals(Integer.valueOf(ba * 51), coverage.getDetails().get(getItem(TRANS_BA)));
        assertEquals(null, coverage.getDetails().get(getItem(TRANS_NEVER)));
        coverage.addItem(getItem(TRANS_NEVER));
        assertEquals(Integer.valueOf(0), coverage.getDetails().get(getItem(TRANS_NEVER)));
        assertEquals(-1, coverage.getMaximum());
        assertEquals(100.0 * 2 / (2 + 100.0), coverage.getPercentage(), 0.001);
        coverage.clear();
        assertEquals(0, coverage.getCoverage());
        assertEquals(Integer.valueOf(0), coverage.getDetails().get(getItem(TRANS_AB)));
        assertEquals(Integer.valueOf(0), coverage.getDetails().get(getItem(TRANS_BA)));
        assertEquals(-1, coverage.getMaximum());
        assertEquals(0.0, coverage.getPercentage(), 0.001);
    }

    protected void shortWalk(AbstractCoverage coverage) {
        coverage.doneReset("forced", true);
        coverage.doneTransition(0, TRANS_AB);
        coverage.doneTransition(0, TRANS_BA);
        coverage.doneTransition(0, TRANS_AB);
    }

}
