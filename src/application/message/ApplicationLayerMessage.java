package application.message;

public class ApplicationLayerMessage implements Message {

	// Private variables
	private final byte address;
	private final byte[] payload;
	
	/**
	 * Main ApplicationLayerMessage class, it is the base type for
	 * any message that is sent to the ApplicationLayer
	 * The payload of the message is defined at construction and cannot be changed
	 * @param data The payload for this message
	 */
	public ApplicationLayerMessage(byte address, byte[] data){
		this.address = address;
		this.payload = data;
		
	}
	
	@Override
	public byte[] getPayload() {
		return payload;
	}
	
	@Override
	public byte getAddress() {
		return address;
	}

}
