package common;

public abstract class Layer {
	protected Layer down;
	
	/** Sends a single byte in a layer-defined way. */
	public abstract void sendByte(byte data);

	/** Reads a byte in a layer-defined way. */
	public abstract byte readByte();
	
	/** Returns a string representation of this Layer and the ones below it. */
	public String toString() {
		return getClass().getSimpleName() + (down == null ? "" : "/" + down.toString());
	}
}
