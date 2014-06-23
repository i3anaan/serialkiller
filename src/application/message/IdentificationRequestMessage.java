package application.message;

public class IdentificationRequestMessage extends IdentificationMessage {

	/** byte value of a WHOIS request flag */
	private static final byte WHOISrequestCommand = 'W';
	
	public IdentificationRequestMessage(byte address) {
		super(address,writeData());
	}

	/**
	 * Writes the data for a WHOIS request
	 * @param data of the WHOIS
	 * @return payload for the IdentificationRequestMessage
	 */
	private static byte[] writeData(){
		byte[] data = new byte[1];
		data[0] = WHOISrequestCommand;
		
		return data;
	}
}
