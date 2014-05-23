package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Observable;

import javax.naming.SizeLimitExceededException;

import common.Stack;
import common.Startable;
import log.LogMessage;
import log.Logger;
import network.NetworkLayer;
import network.Packet;
import network.Payload;
import application.message.*;

/**
 * Main ApplicationLayer class, responsible for writing and
 * reading payload data and in turn delivering them to either
 * the NetworkLayer or the application.
 * 
 * @author msbruning
 *
 */
public class ApplicationLayer extends Observable implements Runnable, Startable{

	/** NetworkLayer that this ApplicationLayer communicates with */
	private NetworkLayer networkLayer;

	/** The Logger object used by this layer to send log messages to the web interface */
	private static Logger logger;

	/** byte value of a chat flag */
	private static final byte chatCommand = 'C';

	/** byte value of a fileOffer flag */
	private static final byte fileOfferCommand = 'F';

	/** byte value of a  fileAccept flag */
	private static final byte fileAcceptCommand = 'A';

	/** byte value of a fileTransfer flag */
	private static final byte fileTransferCommand = 'S';

	public ApplicationLayer(){
		
	}


	/**
	 * Reads payload by checking the payload for commands
	 * @param data
	 * @return
	 * @throws CommandNotFoundException 
	 */
	public void readPayload(Payload p){

		try {

			// Retrieves command char from payload
			char command = getCommand(p.data);

			// Chat message
			if(command == 'C'){
				// Creates a new chat message object and notifies the gui
				ChatMessage cm = new ChatMessage(p.address, p.data);
				setChanged();
				notifyObservers(cm);

			}
			// Request to transfer file
			else if(command == 'F'){

				FileOfferMessage fm = new FileOfferMessage(p.address, p.data);
				setChanged();
				notifyObservers(fm);

				ApplicationLayer.getLogger().debug("Received FileOffer: " + p.toString() + ".");
			}
			// Accept file transfer
			else if(command == 'A'){
				FileAcceptMessage fm = new FileAcceptMessage(p.address, p.data);
				//TODO start file transfer
				//byte[] send = readFile(rootDir + "/downloads");

				ApplicationLayer.getLogger().debug("Accepted File Transfer: " + p.toString() + ".");

			}
			// Transfer file
			else if(command == 'S'){
				FileTransferMessage fm = new FileTransferMessage(p.address, p.data);
				//Writes the file to requested path

				//TODO call gui to request filepath
				//writeFile(fm.getFileBytes(), (rootDir + "/downloads"));

				ApplicationLayer.getLogger().debug("Started File Transfer: " + p.toString() + ".");

			}
			else{
				// TODO find ways to catch invalid commands in a more refined manner
				throw new CommandNotFoundException(String.format("command: %c", command));

			}
		} catch (CommandNotFoundException e) {
			ApplicationLayer.getLogger().debug("CommandNotFoundException on: " + p.toString() + ".");
			e.printStackTrace();
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
	 * 
	 */
	public void readFile(String strFilePath){

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
		// TODO remove
		byte destination = 1;
		Payload p = new Payload(data, destination);
		
		try {
			networkLayer.send(p);
		} catch (SizeLimitExceededException e) {
			ApplicationLayer.getLogger().debug("Size Limit Exceeded! " + p.toString() + ".");
			e.printStackTrace();
		}
	}

	/**
	 * Reads a chat message from the chat application
	 * and converts it into a byte array
	 * @param String containing nickname
	 * @param String containing chat message
	 * @return byte array containing payload for a chat message
	 */

	public byte[] writeChatMessage(String nickname, String message){
		// convert String to UTF-8		
		byte nullbyte = (byte) 0;
		byte[] nick;
		byte[] msg;
		byte[] data = null;
		
		try {
			nick = nickname.getBytes("UTF-8");
			msg = message.getBytes("UTF-8");


			// create new byte[] with minimum length needed
			data = new byte[nick.length + 2 + msg.length];

			// connoctate byte arrays into a new byte array
			data[0] = chatCommand;
			System.arraycopy(nick, 0, data, 1, nick.length);
			data[nick.length+1] = nullbyte;
			System.arraycopy(msg, 0, data, (nick.length+2), msg.length);

		} catch (UnsupportedEncodingException e) {
			ApplicationLayer.getLogger().critical("UTF-8 is not supported: WTF?!?!?!" + ".");
			e.printStackTrace();
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
				readPayload(p);
				ApplicationLayer.getLogger().debug("Received Payload: " + p.toString() + ".");
				return; // We are done.

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


	@Override
	public Thread start(Stack stack) {
		networkLayer = stack.networkLayer;
		return null;
	}
}
