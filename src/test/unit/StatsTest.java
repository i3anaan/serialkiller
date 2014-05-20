package test.unit;

import org.junit.Test;
import stats.Stats;

import static org.junit.Assert.*;

/**
 * Tests for the stats package.
 */
public class StatsTest {
    @Test
    public void testHit() {
        Stats.hit("test.hit");
        assertEquals(1, Stats.getValue("test.hit"));
    }

    @Test
    public void testDiff() {
        Stats.hit("test.diff", 10);
        assertEquals(10, Stats.getValue("test.diff"));
        Stats.hit("test.diff", 10);
        assertEquals(20, Stats.getValue("test.diff"));
        Stats.hit("test.diff", -20);
        assertEquals(0, Stats.getValue("test.diff"));
    }

    @Test
    public void testNonexistent() {
        assertEquals(0, Stats.getValue("test.nil"));
    }

    @Test
    public void testGetCounters() {
        Stats.hit("test.get");
        assertTrue(Stats.getCounters().contains("test.get"));
    }
}
