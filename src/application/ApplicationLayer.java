package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;

import application.message.*;

/*
 * Main ApplicationLayer, interpets and writes payload data
 */
public class ApplicationLayer extends Observable {

	public ApplicationLayer(){

	}


	/**
	 * Reads payload by checking the payload for commands
	 * @param data
	 * @return
	 * @throws CommandNotFoundException 
	 */
	public void readPayload(byte[] data) throws CommandNotFoundException{
		// Retrieves command char from payload
		char command = getCommand(data);

		// Send Chat message
		if(command == 'C'){
			// Creates a new chat message object
			ChatMessage cm = new ChatMessage(data);
			//TODO call gui to parse chat message
			System.out.println("Nickname: " +cm.getNickname()+"\n");
			System.out.println("Message: " +cm.getMessage()+"\n");
			setChanged();
			notifyObservers(cm);

		}
		// Send request to transfer file
		else if(command == 'F'){

			FileOfferMessage fm = new FileOfferMessage(data);
			System.out.println("FileOfferMessage: -----------");
			System.out.println("FileSize: "+fm.getFileSize());
			System.out.println("FileName: "+fm.getFileName()+ "\n");
			//TODO call gui and notify of file offer
		}
		// Accept file transfer
		else if(command == 'A'){
			FileAcceptMessage fm = new FileAcceptMessage(data);
			System.out.println("FileAcceptMessage: -----------");
			System.out.println("FileSize: "+fm.getFileSize());
			System.out.println("FileName: "+fm.getFileName()+"\n");
			
			//TODO call networkLayer and send data for wrapping
			byte[] send = readFile("/c/derp/");
			
		}
		// Transfer file
		else if(command == 'S'){
			FileTransferMessage fm = new FileTransferMessage(data);
			System.out.println("FileTransferMessage: -----------");
			System.out.println(fm.getFileBytes().length + " bytes \n");
			//Writes the file to requested path
			
			//TODO call gui to request filepath
			writeFile(fm.getFileBytes(), "/c/derp/");

		}
		else{
			// TODO find ways to catch invalid commands in a more refined manner
			throw new CommandNotFoundException(String.format("command: %c", command));

		}
	}

	/**
	 * Converts a HexString into a byte array
	 * @param s String to be converted
	 * @return b byte array
	 */
	public static byte[] writePayload(String s){

		
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;

	}

	/**
	 * Retrieves the first byte from the payload and converts it to a Char.
	 * @param data
	 * @return The Char representing the command in this payload
	 */
	private char getCommand(byte[] data){

		return (char)data[0];
	}

	/**
	 * Writes data from a byte array as a file to the given path
	 * @param Byte array to write from
	 * @param Path to write to
	 * @return True if successfull
	 */
	private boolean writeFile(byte[] data, String strFilePath){

		boolean result = false;
		try {
			FileOutputStream fos = new FileOutputStream(strFilePath);
			fos.write(data);
			fos.close();
			result = true;
		}
		catch(FileNotFoundException ex)   {
			System.out.println("FileNotFoundException : " + ex);
		}
		catch(IOException ioe)  {
			System.out.println("IOException : " + ioe);
		}

		return result;
	}

	/**
	 * Reads a file from local directory
	 * as long as the filesize is less than 2GB
	 * and wraps it as a payload.
	 * 
	 * @requires filesize < 2GB
	 * @param Path of the file to be read
	 * @return byte array
	 */
	public byte[] readFile(String strFilePath){

		File file = new File(strFilePath);
		byte[] data = new byte[strFilePath.length()];

		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(data);
			fileInputStream.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		}
		catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}

		return data;
	}
	
	/**
	 * Reads a chat message from the chat application
	 * and converts it into a byte array
	 * @param String containing chat message
	 * @return byte array containing chat message
	 */
	public byte[] writeChatMessage(String s){
		
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	
	}
}
