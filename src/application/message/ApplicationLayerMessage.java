package application.message;

/**
 * Abstract superclass for all application-layer messages (which are network-
 * layer payloads). 
 */
public abstract class ApplicationLayerMessage {
	/**
	 * TPP address.
	 * 
	 * For incoming messages, this is the sender's address; for outgoing
	 * messages, this is the destination address.
	 */
	private final byte address;
	
	/** Payload. The exact meaning depends on the message type. Immutable. */
	private final byte[] payload;
	
	/** Construct an ApplicationLayerMessage. */
	public ApplicationLayerMessage(byte address, byte[] data){
		this.address = address;
		this.payload = data;
	}
	
	/** Returns the message payload. */
	public byte[] getPayload() {
		return payload;
	}
	
	/** Returns the message address. */
	public byte getAddress() {
		return address;
	}

}
