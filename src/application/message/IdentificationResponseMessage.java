package application.message;

public class IdentificationResponseMessage extends IdentificationMessage {

	/** byte value of a WHOIS response flag */
	private static final byte WHOISresponseCommand = 'I';
	
	/**
	 * Constructor for responses we make to WHOIS requests
	 * @param address to send to
	 * @param data of the original WHOIS
	 * @param identification we identify ourselves with
	 */
	public IdentificationResponseMessage(byte address, byte[] data, byte[] identification) {
		super(address,writeOutData(identification));
	}
	
	/**
	 * Constructor for Responses we receive to our WHOIS requests
	 * @param address of the responding host
	 * @param data containing their identification
	 */
	public IdentificationResponseMessage(byte address, byte[] data) {
		super(address,writeIncData(data));
	}
	
	

	/**
	 * Writes the data for a response to a WHOIS request
	 * @param identification of ourselves
	 * @return payload for the IdentificationResponseMessage
	 */
	private static byte[] writeOutData(byte[] identification){
		
		byte[] data = new byte[1 + identification.length];
		data[0] = WHOISresponseCommand;
		System.arraycopy(identification, 0, data, 1, identification.length);

		return data;
	}
	
	/**
	 * Writes the data for a WHOIS request
	 * @param data of the WHOIS
	 * @return payload for the IdentificationRequestMessage
	 */
	private static byte[] writeIncData(byte[] data){
		//Get WHOIS without command byte
		byte[] identificationResponseData = new byte[data.length-1];
		System.arraycopy(data, 1, identificationResponseData, 0, data.length-1);
		
		return identificationResponseData;
	}
}
