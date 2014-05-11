package stats;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility class for keeping track of system-wide statistics.
 */
public final class Stats {
    private Stats() {
        /* Disallow construction. */
    }

    /** Maps from a counter description (as a String) to an actual count. */
    private static HashMap<String, Integer> counts = null;

    /** Increase the named counter by 1. */
    public static synchronized void hit(String counter) {
        hit(counter, 1);
    }

    /** Increase the named counter by the given (possibly negative) amount. */
    public static synchronized void hit(String counter, int diff) {
        if (counts == null) {
            counts = new HashMap<String, Integer>();
        }

        if (counts.containsKey(counter)) {
            counts.put(counter, counts.get(counter) + diff);
        } else {
            counts.put(counter, diff);
        }
    }

    /** Return a Set of counters. */
    public static synchronized Set<String> getCounters() {
        return new TreeSet<String>(counts.keySet());
    }

    /** Return the current value for the specified counter. */
    public static synchronized int getValue(String counter) {
        return counts.get(counter);
    }
}
