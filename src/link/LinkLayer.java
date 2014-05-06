package link;



import common.Layer;

/**
 * Basic interface for implementations of link layers. This interface specifies
 * the basic API to ensure compatibility independent of the type of link layer
 * that is used.
 */
public abstract class LinkLayer extends Layer {
	
	public abstract void sendFrame(byte[] frame);
	
	/**
	 * Blocking
	 * @return
	 */
	public abstract byte[] readFrame();
	
}
