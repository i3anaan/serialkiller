package application.message;

import java.io.File;
import java.nio.ByteBuffer;

import com.google.common.base.Charsets;

public class FileOfferMessage extends FileMessage {

	/** byte value of a fileOffer flag */
	private static final byte fileOfferCommand = 'F';
	
	/**
	 * Constructor for incoming fileOffers
	 * @param address of the sender
	 * @param data containing the fileOffer
	 */
	public FileOfferMessage(byte address, byte[] data) {
		super(address, data);
	}

	/**
	 * Constructor for outgoing fileOffers
	 * @param address to send to
	 * @param filePath of the file we are offering
	 */
	public FileOfferMessage(byte address, String filePath){
		super(address, writeData(filePath));

	}

	/**
	 * Method that writes the data for a fileOffer
	 * message to a byte array
	 * @param strFilePath path of the offered file
	 * @return byte array containing the file offer
	 */
	private static byte[] writeData(String strFilePath){

		// Split the string and retrieve only the filename in bytes
		File f = new File(strFilePath);
        String fileName = f.getName();
		byte[] byteName = fileName.getBytes(Charsets.UTF_8);

		// FileSize
		long fileSize = (new File(strFilePath).length());
		byte[] byteFileSize = ByteBuffer.allocate(4).putInt((int) fileSize).array();

		// Form the data byte array for the offer payload
		byte[] data = new byte[5 + byteName.length];
		data[0] = fileOfferCommand;
		System.arraycopy(byteFileSize, 0, data, 1, 4);
		System.arraycopy(byteName, 0, data, 5, byteName.length);

		return data;
	}

}


