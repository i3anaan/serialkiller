package common;

public abstract class Layer {
	protected Layer down;
	
	/** Returns a string representation of this Layer and the ones below it. */
	public String toString() {
		return getClass().getSimpleName() + (down == null ? "" : "/" + down.toString());
	}

	/** Callback for any layer-specific startup behaviour. */
	public void start() {
	}
}
