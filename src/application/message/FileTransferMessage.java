package application.message;

import java.util.Arrays;

import com.google.common.primitives.Bytes;

public class FileTransferMessage extends FileMessage {

	/** byte value of a fileTransfer flag */
	private static final byte fileTransferCommand = 'S';
	
	/** byte value of nullbyte */
	private static final char nullbyte = '\0';
	
	// Private variables
	private byte[] fileBytes;

	/**
	 * Constructor for incoming fileTransferMessages
	 * @param address of the host sending to us
	 * @param data of the fileTransfer
	 */
	public FileTransferMessage(byte address, byte[] data) {
		super(address, data);
		setFileBytes(data);
	}

	/**
	 * Constructor for outgoing fileTransferMessages
	 * @param address to send to
	 * @param messageData containing the original offer
	 * @param fileData of the file we are sending
	 */
	public FileTransferMessage(byte address, byte[] messageData, byte[] fileData){
		super(address, writeData(messageData, fileData));
	}

	/**
	 * Method that writes the data for a fileTransfer
	 * message to a byte array
	 * @param data containing the original offer
	 * @param the file data we are sending
	 */
	private static byte[] writeData(byte[] data, byte[] fileData){

		// Use the data from the offer to form the fileTransfer data
		byte[] fileTransferData = new byte[data.length + 1 + fileData.length];

		fileTransferData[0] = fileTransferCommand;
		System.arraycopy(data, 1, fileTransferData, 1, (data.length - 1) );
		fileTransferData[data.length] = nullbyte;
		System.arraycopy(fileData, 0, fileTransferData, data.length + 1, fileData.length);

		return fileTransferData;
	}

	/**
	 * Returns the byte array containing all of the bytes
	 * of the transfered file
	 * @return Bytes from file
	 */
	public byte[] getFileBytes(){
		return fileBytes;
	}

	/**
	 * Sets the bytes containing the file data in
	 * the private variable;
	 * @param payload data to read from
	 */
	private void setFileBytes(byte[] data){
		byte[] datapart = Arrays.copyOfRange(data, 5, data.length);
		fileBytes = Arrays.copyOfRange(datapart, Bytes.indexOf(datapart, (byte)'\0') + 1, (datapart.length));

	}

}


