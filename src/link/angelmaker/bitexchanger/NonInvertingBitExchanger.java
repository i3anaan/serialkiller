package link.angelmaker.bitexchanger;

/**
 * An extension of the simpleBitExchanger. This BitExchanger uses a different
 * mechanic of adapting and extracting bits. This will never invert both bits on
 * the line. This is done as the real cable sets the 2 lines non-atomic. This
 * effect will thus never create an error using this BitExchanger.
 * 
 * Speed is equal to SimpleBitExchanger, making this BitExchanger better than
 * SimpleBitExchanger in almost any situation.
 * 
 * @author I3anaan
 * 
 */
public class NonInvertingBitExchanger extends SimpleBitExchanger {

	/**
	 * Only toggles the lock bit if the data does not create a change compared
	 * to the previous byte sent.
	 */
	@Override
	public byte adaptBitToPrevious(byte previousByte, boolean nextData) {
		if (nextData == ((previousByte & 1) == 1)) {
			return (byte) (((previousByte & 2) ^ 2) | (nextData ? 1 : 0));
		} else {
			return (byte) ((previousByte & 2) | (nextData ? 1 : 0));
		}

	}

	@Override
	public String toString() {
		return "NonInvertingBitExchanger";
	}
}
