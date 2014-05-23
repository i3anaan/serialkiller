package link.jack;

import java.util.ArrayList;
import java.util.Arrays;

import util.BitSet2;

import java.util.concurrent.ArrayBlockingQueue;

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

	/*
	 * public FlaggedFrame(ArrayBlockingQueue<Byte> queue,char useless) {
	 * //System
	 * .out.println(Thread.currentThread().getId()+"  Making FlaggedFrame from = "
	 * +Arrays.toString(queue.toArray(new Byte[0]))); Unit[] units = new
	 * Unit[Frame.PAYLOAD_UNIT_COUNT]; for (int i = 0; i < units.length &&
	 * !queue.isEmpty(); i++) { try { units[i] = new PureUnit(queue.take()); }
	 * catch (InterruptedException e) { e.printStackTrace(); } } this.payload =
	 * new Frame(units); //this.units = units;
	 * //System.out.println("payload done:  "+Arrays.toString(payload.units)); }
	 */

	public FlaggedFrame(ArrayBlockingQueue<Unit> queue) {
		// System.out.println(Thread.currentThread().getId()+"  Making FlaggedFrame from = "+Arrays.toString(queue.toArray(new
		// Byte[0])));
		Unit[] units = new Unit[Frame.PAYLOAD_UNIT_COUNT];
		for (int i = 0; i < units.length && !queue.isEmpty(); i++) {
			try {
				units[i] = queue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.payload = new Frame(units);
		// this.units = units;
		// System.out.println("payload done:  "+Arrays.toString(payload.units));
	}

	public FlaggedFrame(BitSet2 bits) {
		ArrayList<Unit> units = new ArrayList<Unit>();
		// Only put higher level data (can still contain data) in array.
		// Do not store miniFrame flags, only reacht on those.

		if (JackTheRipper.UNIT_IN_USE instanceof PureUnit) {
			for (int i = 0; i < bits.length() - 8; i = i + 9) {
				PureUnit unit = new PureUnit(Bytes.fromBitSet(bits, i),
						bits.get(i + 8));
				if (unit.isDataOrFill()
						&& units.size() < Frame.PAYLOAD_UNIT_COUNT) {
					units.add(unit);
				} else {
					// TODO other flags detected;
				}
			}
		}else if(JackTheRipper.UNIT_IN_USE instanceof HammingUnit){
			for (int i = 0; i < bits.length()-7; i = i + 8) {
				HammingUnit unit = new HammingUnit(bits.get(i, i+8),JackTheRipper.HC);
				if (units.size() < Frame.PAYLOAD_UNIT_COUNT) {
					units.add(unit);
				} else {
					// TODO other flags detected;
				}
			}
		}
		payload = new Frame(units.toArray(new Unit[0]));
	}

	/**
	 * Returns the full bit sequence (Flags and stuffings are left in).
	 * 
	 * @return
	 */
	public BitSet2 getDataBitSet() {
		// System.out.println("Units in flaggedFrame: "+Arrays.toString(units));
		BitSet2 result = new BitSet2();
		for (Unit u : payload.units) { // Should make this better;
			// System.out.println(u.asBitSet());
			result = BitSet2.concatenate(result, u.fullAsBitSet());
		}
		return result;
	}

	public Frame getPayload() {
		return payload;
	}

	public String toString() {
		return Arrays.toString(this.payload.units);
	}
}
