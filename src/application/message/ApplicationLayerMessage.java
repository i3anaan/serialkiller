package application.message;

public class ApplicationLayerMessage implements Message {

	// Private variables
	private final byte adress;
	private final byte[] payload;
	
	/**
	 * Main ApplicationLayerMessage class, it is the base type for
	 * any message that is sent to the ApplicationLayer
	 * The payload of the message is defined at construction and cannot be changed
	 * @param data The payload for this message
	 */
	public ApplicationLayerMessage(byte adress, byte[] data){
		this.adress = adress;
		this.payload = data;
	}
	
	@Override
	public byte[] getPayload() {
		return payload;
	}
	
	@Override
	public byte getAdress() {
		return adress;
	}

}
