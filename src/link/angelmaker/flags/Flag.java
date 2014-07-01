package link.angelmaker.flags;

import util.BitSet2;

/**
 * Interface for bitstuffing flags.
 * The actual (bit) flag is stored in a BitSet2.
 * The Flag class can stuff and unstuff bits, to prevent the flag from occuring in the stuffed data.
 * Unstuffing stuffed data gives the original data.
 * @author I3anaan
 *
 */
public interface Flag {
	public void stuff(BitSet2 bits);
	public void unStuff(BitSet2 bits);
	public BitSet2 getFlag();
}
