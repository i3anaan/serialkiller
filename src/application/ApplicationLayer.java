package application;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Observable;

import javax.naming.SizeLimitExceededException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import common.Stack;
import common.Startable;
import log.LogMessage;
import log.Logger;
import network.NetworkLayer;
import network.Payload;
import application.message.*;

/**
 * Main ApplicationLayer class, responsible for writing and reading payload 
 * data and in turn delivering them to the NetworkLayer below or the 
 * application above.
 */
public class ApplicationLayer extends Observable implements Runnable, Startable {

	/** NetworkLayer that this ApplicationLayer communicates with. */
	private NetworkLayer networkLayer;

	/** The Logger object used by this layer. */
	private static Logger logger;

	/** The main thread of this class instance. */
	private Thread thread;

	/** byte value of a chat flag */
	private static final byte chatCommand = 'C';

	/** byte value of a fileOffer flag */
	private static final byte fileOfferCommand = 'F';

	/** byte value of a  fileAccept flag */
	private static final byte fileAcceptCommand = 'A';

	/** byte value of a fileTransfer flag */
	private static final byte fileTransferCommand = 'S';
	
	public ApplicationLayer() {
		// Construct our own thread
		thread = new Thread(this);
		thread.setName("APL " + this.hashCode());
		
		logger = new Logger(LogMessage.Subsystem.APPLICATION);
	}

	/**
	 * Reads payload by checking the payload for commands
	 * @param p  The input data as a Payload (addressed byte array) object.
	 */
	public void readPayload(Payload p) {
		try {
			byte command = p.getCommand();
			
			switch(command) {
			case chatCommand:
				// Received a chat message.
				// Create a new chat message object and notifies the GUI
				ChatMessage cm = new ChatMessage(p.address, p.data);
				setChanged();
				notifyObservers(cm);
				break;
				
			case fileOfferCommand: 
				// Received a file transfer offer.
				FileOfferMessage offer = new FileOfferMessage(p.address, p.data);
				setChanged();
				notifyObservers(offer);
				logger.debug("Received FileOffer: " + p);
				break;
			
			case fileAcceptCommand:
				// Someone accepted our file offer.
				// TODO Make sure we actually offered this file.
				// TODO Actually send the file we offered.
				FileAcceptMessage accept = new FileAcceptMessage(p.address, p.data);
				readFile("test/bunny.txt");
				logger.debug("Accepted File Transfer for file " + accept.getFileName() + ": " + p);
				break;

			case fileTransferCommand:
				// Someone is sending us a file. Write the file to our disk.
				FileTransferMessage fm = new FileTransferMessage(p.address, p.data);

				//TODO call GUI to request file path
				Files.write(fm.getFileBytes(), new File("test/received.bin"));

				logger.debug("Started File Transfer: " + p);
				break;
			
			default:
				// TODO find ways to catch invalid commands in a more refined manner
				throw new CommandNotFoundException(String.format("command: %c", command));
			}
		} catch (IOException e) {
			logger.warning("Caught IOException " + e + " while handling payload " + p);
			/* Do nothing else (i.e. drop the payload). */
		} catch (CommandNotFoundException e) {
			logger.debug("CommandNotFoundException on: " + p);
			e.printStackTrace();
		}

	}

	/**
	 * Reads a file from local directory and sends it to a host on the network.
	 * 
	 * @requires file size < 2GB
	 * @param Path of the file to be read
	 * 
	 */
	public void readFile(String strFilePath) throws IOException {
		byte[] data = Files.toByteArray(new File(strFilePath));

		// TODO Add a file transfer flag.
		// TODO Make it possible to actually set the destination.
		byte destination = 1;
		Payload p = new Payload(data, destination);

		try {
			networkLayer.send(p);
		} catch (SizeLimitExceededException e) {
			logger.debug("Size Limit Exceeded! " + p.toString() + ".");
			e.printStackTrace();
		}
	}

	/**
	 * Reads a chat message from the chat application and converts it to a byte
	 * array.
	 * 
	 * @param String containing nickname
	 * @param String containing chat message
	 * @param byte   the destination of the chat message as an address
	 */
	public void writeChatMessage(String nickname, String message, byte destination) {
		byte nullbyte = 0;

		byte[] nick = nickname.getBytes(Charsets.UTF_8);
		byte[] msg = message.getBytes(Charsets.UTF_8);

		// create new byte[] with minimum length needed
		byte[] data = new byte[nick.length + 2 + msg.length];

		// concatenate byte arrays into a new byte array
		data[0] = chatCommand;
		System.arraycopy(nick, 0, data, 1, nick.length);
		data[nick.length+1] = nullbyte;
		System.arraycopy(msg, 0, data, (nick.length+2), msg.length);

		try {
			networkLayer.send(new Payload(data, destination));
		} catch (SizeLimitExceededException e) {
			logger.warning("Size Limit Exceeded:" + ".");
			e.printStackTrace();
			/* Drop the message. */
		}
	}

	@Override
	public void run() {
		// TODO shut down cleanly
		boolean run = true;

		// Read all payload objects in the queue.
		while (run) {
			Payload p = networkLayer.read();

			// incoming payload
			readPayload(p);
			logger.debug("Received Payload: " + p.toString() + ".");
		}
		
		logger.warning("ApplicationLayer stopped.");
	}

	/**
	 * Method to retrieve a listing of hosts from the networkLayer
	 * for the application
	 * @return collection of hosts
	 */
	public Collection<Byte> getHosts() {
		return networkLayer.hosts();
	}

	@Override
	public Thread start(Stack stack) {
		networkLayer = stack.networkLayer;
		logger.info(networkLayer.toString());
		logger.info("ApplicationLayer started.");
		return thread;
	}
}
