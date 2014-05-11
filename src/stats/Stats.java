package stats;

import java.util.HashMap;
import java.util.TreeSet;

public class Stats {
    private Stats() {
        /* Disallow construction. */
    }

    private static HashMap<String, Integer> counts;

    public synchronized static void hit(String counter) {
        hit(counter, 1);
    }

    public synchronized static void hit(String counter, int diff) {
        if (counts == null) counts = new HashMap<String, Integer>();

        if (counts.containsKey(counter)) {
            counts.put(counter, counts.get(counter) + diff);
        } else {
            counts.put(counter, 0);
        }
    }

    public static java.util.Set<String> getCounters() {
        return new TreeSet<String>(counts.keySet());
    }

    public static int getValue(String counter) {
        return counts.get(counter);
    }
}
