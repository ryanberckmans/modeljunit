package nz.ac.waikato.modeljunit.coverage;

import static org.junit.Assert.*;

import nz.ac.waikato.modeljunit.Transition;

import org.junit.Test;

public class StateCoverageTest extends AbstractCoverageTest {
    
    /** @return the system under test. */
    protected AbstractCoverage createCoverage() {
        return new StateCoverage();
    }

    @Override
    protected Object getItem(Transition tr) {
        return tr.getEndState();
    }
    
    @Test
    public void testName() {
        assertEquals("state coverage", createCoverage().getName());
    }

    @Test
    public void testDescription() {
        assertEquals("The number of different FSM states visited.", createCoverage().getDescription());
    }
    
    @Override
    @Test
    public void testCoverage() {
        checkCoverage(2, 2);
    }

}
