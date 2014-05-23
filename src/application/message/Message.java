package application.message;

public interface Message {

	
	/**
	 * Returns the payload of this message
	 * @return byte[] payload
	 */
	public byte[] getPayload();
	
	/**
	 * Returns the sender of this message
	 * @return byte adress;
	 */
	public byte getAddress();
}
