package link.angelmaker.bitexchanger;

/*
 * */
/**
 * This BitExchanger is (assuming fully random data to send) 66% faster then the
 * SimpleBitExchanger. It does this by sending an extra bit of data when the
 * first bit of data already causes a change. The other side can also detect
 * when the first bit of data already causes a change, and then treat the second
 * bit as another data bit. When the first data bit does not change the byte
 * compared to the previous sent byte, the extra bit acts as clock signal and
 * simply causes change.
 * 
 * This BitExchanger can invert both lines at the same time, and can also drop 2
 * bits at once (instead of 1), making this BitExchanger more error prone.
 * 
 * The byte is constructed in the following way:
 * 000000XD
 * D = data bit, always contains 1 data bit
 * X = extra bit, can be clock bit or extra data bit, depending on previousByte received and databit D in this byte.
 * @author I3anaan
 * 
 */
public class HighSpeedBitExchanger extends SimpleBitExchanger {

	/**
	 * Will call getNextBitToSend() itself when it needs an extra bit to send 2 bits of data.
	 */
	@Override
	public byte adaptBitToPrevious(byte previousByte, boolean nextData) {
		if ((previousByte & 1) == (nextData ? 1 : 0)) {
			// no change, use extra bit as clock bit.
			return (byte) ((nextData ? 1 : 0) | ((previousByte & 2) ^ 2));
		} else {
			// Already changes, use extra bit as data bit
			return (byte) ((nextData ? 1 : 0) | (getNextBitToSend() ? 2 : 0));
		}
	}
	/**
	 * @param input
	 *            The byte read from the physical layer.
	 * @return The data bit this byte represents.
	 */
	@Override
	public boolean[] extractBitFromInput(byte previousByte, byte input) {
		if ((previousByte & 1) == (input & 1)) {
			// no change, used extra bit as clock bit.
			boolean[] arr = new boolean[1];
			arr[0] = (input & 1) == 1;
			return arr;
		} else {
			// Already changes, use extra bit as data bit
			boolean[] arr = new boolean[2];
			arr[0] = (input & 1) == 1;
			arr[1] = (input & 2) == 2;
			return arr;
		}
	}

	@Override
	public String toString() {
		return "HighSpeedBitExchanger";
	}

}
