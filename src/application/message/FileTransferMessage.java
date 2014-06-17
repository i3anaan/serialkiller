package application.message;

import java.util.Arrays;

import com.google.common.primitives.Bytes;

public class FileTransferMessage extends FileMessage {
	private byte[] fileBytes;

	public FileTransferMessage(byte address, byte[] data) {
		super(address, data);

		//TODO start from nullbyte!!!!
		setFileBytes(data);

		System.out.println(new String(data));


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
		byte[] datapart = Arrays.copyOfRange(data, 5, data.length-1);
		fileBytes = Arrays.copyOfRange(datapart, Bytes.indexOf(data, (byte)'\0'), (data.length - Bytes.indexOf(data, (byte)0)));

	}

}


