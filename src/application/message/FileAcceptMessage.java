package application.message;

public class FileAcceptMessage extends FileMessage {

	/** byte value of a  fileAccept flag */
	private static final byte fileAcceptCommand = 'A';
	
	public FileAcceptMessage(byte address, byte[] data) {
		super(address, writeData(data));
		
	}

	/**
	 * Writes the fileAccept flag into the data and
	 * calls the super constructor
	 * @param original data
	 * @return rewritten data
	 */
	private static byte[] writeData(byte[] data){
		data[0] = fileAcceptCommand;
		return data;
	}
}
