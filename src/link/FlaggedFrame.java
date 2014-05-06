package link;

import java.util.ArrayList;
import java.util.Arrays;
import util.BitSet2;
import java.util.concurrent.ArrayBlockingQueue;

import util.BitSets;
import util.ByteArrays;
import util.Bytes;

/**
 * Serves as a header surrounding the normal Frame.
 * 
 * @author I3anaan
 * 
 */
public class FlaggedFrame extends Frame {

	public static final int FLAGGED_FRAME_UNIT_COUNT = Frame.PAYLOAD_UNIT_COUNT;
	Frame payload;

	public FlaggedFrame() {
		this.payload = new Frame();
	}

	public FlaggedFrame(Frame payload) {
		this.payload = payload;
	}

	public FlaggedFrame(ArrayBlockingQueue<Byte> queue) {
		//System.out.println(Thread.currentThread().getId()+"  Making FlaggedFrame from = "+Arrays.toString(queue.toArray(new Byte[0])));
		Unit[] units = new Unit[Frame.PAYLOAD_UNIT_COUNT];
		for (int i = 0; i < units.length && !queue.isEmpty(); i++) {
			try {
				units[i] = new Unit(queue.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.payload = new Frame(units);
		//this.units = units;
		//System.out.println("payload done:  "+Arrays.toString(payload.units));
	}

	public FlaggedFrame(BitSet2 bits) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		// Only put data or fill bytes in this arraylist.
		// Only react on flags, dont store them in this.

		for (int i = 0; i < bits.length() - 8; i = i + 9) {
			Unit unit = new Unit(Bytes.fromBitSet(bits, i), bits.get(i + 8));
			if (unit.isDataOrFill() && units.size() < Frame.PAYLOAD_UNIT_COUNT) {
				units.add(unit);
			} else {
				// TODO other flags detected;
			}
		}
		payload = new Frame(units.toArray(new Unit[0]));
		//this.units = units.toArray(new Unit[0]);
		//System.out.println("Build from received bitset: "+payload+"  Bitset: "+bits+"   Array: "+Arrays.toString(units.toArray(new Unit[0])));
	}

	/**
	 * Returns the full bit sequence (Flags and stuffings are left in).
	 * 
	 * @return
	 */
	public BitSet2 getBitSet() {
		//System.out.println("Units in flaggedFrame: "+Arrays.toString(units));
		BitSet2 result = new BitSet2();
		for (Unit u : payload.units) { //Should make this better;
			//System.out.println(u.asBitSet());
			result = BitSets.concatenate(result, u.asBitSet());
		}
		return result;
	}

	public Frame getPayload() {
		return payload;
	}
	
	public String toString(){
		return Arrays.toString(this.payload.units);
	}
}
