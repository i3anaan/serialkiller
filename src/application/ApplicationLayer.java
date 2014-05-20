package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;

import log.LogMessage;
import log.Logger;
import network.NetworkLayer;
import network.Packet;
import application.message.*;

/**
 * Main ApplicationLayer class, responsible for writing and
 * reading payload data and in turn delivering them to either
 * the NetworkLayer or the application.
 * 
 * @author msbruning
 *
 */
public class ApplicationLayer extends Observable {

	/** NetworkLayer that this ApplicationLayer communicates with */
	private NetworkLayer networkLayer;
	/** The Logger object used by this layer to send log messages to the web interface */
	private Logger logger;
	
	public ApplicationLayer(NetworkLayer nl){
		this.networkLayer = nl;
	}


	/**
	 * Reads payload by checking the payload for commands
	 * @param data
	 * @return
	 * @throws CommandNotFoundException 
	 */
	public void readPayload(byte adress, byte[] data) throws CommandNotFoundException{
		// Retrieves command char from payload
		char command = getCommand(data);

		// Chat message
		if(command == 'C'){
			// Creates a new chat message object and notifies the gui
			ChatMessage cm = new ChatMessage(adress, data);
			setChanged();
			notifyObservers(cm);

		}
		// Request to transfer file
		else if(command == 'F'){

			FileOfferMessage fm = new FileOfferMessage(data);
			setChanged();
			notifyObservers(fm);
		}
		// Accept file transfer
		else if(command == 'A'){
			FileAcceptMessage fm = new FileAcceptMessage(data);
			//TODO start file transfer
			byte[] send = readFile(rootDir + "/downloads");
			
		}
		// Transfer file
		else if(command == 'S'){
			FileTransferMessage fm = new FileTransferMessage(data);
			//Writes the file to requested path
			
			//TODO call gui to request filepath
			writeFile(fm.getFileBytes(), (rootDir + "/downloads"));

		}
		else{
			// TODO find ways to catch invalid commands in a more refined manner
			throw new CommandNotFoundException(String.format("command: %c", command));

		}
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
	// TODO maybe add nickname as parameter
	public byte[] writeChatMessage(String s){
		
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	
	}
	
	 @Override
	    public void run() {

	        boolean run = true;

	        // Read payloads in the queue.
	        while (run) {
	            try {
	                Payload p = networkLayer.read();

	                // incoming payload
	                	readPayload(p.adress, p.data);
	                    ApplicationLayer.getLogger().debug("Received payload: " + p.toString() + ".");
	                    return; // We are done.

	                sendPacket(p);
	            } catch (InterruptedException e) {
	                // Exit gracefully.
	                run = false;
	            }
	        }
	        ApplicationLayer.getLogger().warning("ApplicationLayer stopped.");

	    }
	    
	 	/** Returns the Logger object for this ApplicationLayer */
	    public static Logger getLogger() {
	        if (logger == null) {
	            logger = new Logger(LogMessage.Subsystem.NETWORK);
	        }
	        return logger;
	    }
}
