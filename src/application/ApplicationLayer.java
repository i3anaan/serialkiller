package application;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.naming.SizeLimitExceededException;
import javax.swing.Timer;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
	private Logger logger;

	/** The main thread of this class instance. */
	private Thread thread;

	//TODO update documentation
	/** Cash containing all the hosts we are offering files, mapped to the FileOfferPayload we made them */
	private Cache<String, String> fileOfferCache;

	/** Cash containing all the hosts that are offering files, mapped to the FileOfferPayload they made us */
	//TODO fix description
	private Cache<String, String> offeredFileCache;

	/** byte value of a chat flag */
	private static final byte chatCommand = 'C';

	/** byte value of a fileOffer flag */
	private static final byte fileOfferCommand = 'F';

	/** byte value of a  fileAccept flag */
	private static final byte fileAcceptCommand = 'A';

	/** byte value of a fileTransfer flag */
	private static final byte fileTransferCommand = 'S';

	/** byte value of a WHOIS request flag */
	private static final byte WHOISrequestCommand = 'W';

	/** byte value of a WHOIS response flag */
	private static final byte WHOISresponseCommand = 'I';

	// Identification label for WHOIS requests.
	private final static String label ="<html>4evr \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Connexx0rred with tha bestest proto implementation \u2605\u2605\u2605 SerialKiller \u2605\u2605\u2605 <font color=#ff0000>Brought to you by Squeamish, i3anaan, TheMSB, jjkester</font> \u00AF\\_(\u30C4)_/\u00AF - Regards to all our friends: Jason, Jack, Patrick, Ghostface, Jigsaw, Hannibal, John and Sweeney \u0F3C \u1564\uFEFF\u25D5\u25E1\u25D5\uFEFF \u0F3D\uFEFF\u1564\uFEFF <font color=#009900>Smoke weed every day #420 \u0299\u029F\u1D00\u1D22\u1D07 \u026A\u1D1B</font> --- Send warez 2 <a href='https://sk.twnc.org/'>sk.twnc.org</a>, complaints to /dev/null --- The more the merrier: serial killer = best killer --- Word of the day: hacksaw \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Thanks for taking the time to receive this message, 4evr out.";

	/** byte value of our WHOIS Identification String */
	private final static byte[] identification = label.getBytes(Charsets.UTF_8);

	/** byte value of nullbyte */
	private final int nullbyte = 0;

	public ApplicationLayer() {
		// Construct our own thread
		thread = new Thread(this);
		thread.setName("APL " + this.hashCode());

		// Construct our fileOfferCache
		fileOfferCache = CacheBuilder.newBuilder()
				// No more than 42 file offers outstanding.
				.maximumSize(42)
				// All file offers expire after 30 minutes.
				.expireAfterWrite(30, TimeUnit.MINUTES)
				.build();

		// Construct our offeredFileCache
		offeredFileCache = CacheBuilder.newBuilder()
				.maximumSize(42)
				.expireAfterWrite(30, TimeUnit.MINUTES)
				.build();



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

				//TODO 01 BUILD CACHE HERE?

				setChanged();
				notifyObservers(offer);
				logger.debug("Received FileOffer: " + p + "From host: " + p.address);
				break;

			case fileAcceptCommand:
				// Someone accepted our file offer.
				FileAcceptMessage accept = new FileAcceptMessage(p.address, p.data);

				//Get key from FileMessage
				String key = (accept.getAddress() + accept.getFileName());

				// Check if fileOffer exists and if so send it
				String ftp = fileOfferCache.getIfPresent(key);

				//DEBUG LINE
				//				System.out.println("Present? : " + ftp);
				//
				//				for (String loopkey : fileOfferCache.asMap().keySet()) {
				//					System.out.println("Base key: " + key + "Test1");
				//					System.out.println("A key: " +loopkey + "Test2");
				//					System.out.println(loopkey.equals(key));
				//					
				//				}


				if(ftp != null){
					//System.out.println("DEBUG REACHED");
					//TODO FIX STRING KEY IN CACHE, REPLEACE IT WITH A MAP?
					//TODO TEST NEW METHOD, WHEN WORKING REMOVE readFile METHOD

					//Load the file from directory into byte array
					byte[] fileData = Files.toByteArray(new File(ftp));

					// Use the data from the offer to form the filetransfer data
					byte[] fileTransferData = new byte[p.data.length + 1 + fileData.length];
					fileTransferData[0] = fileTransferCommand;
					System.arraycopy(p.data, 0, fileTransferData, 1, (p.data.length -1) );
					System.arraycopy(fileData, 0, fileTransferData, p.data.length, fileData.length);

					Payload transfer = new Payload(fileTransferData, p.address);

					try {
						networkLayer.send(transfer);
					} catch (SizeLimitExceededException e) {
						logger.warning("Size Limit Exceeded:" + p + ".");
						e.printStackTrace();
					}

					logger.debug("Started File Transfer for: " + accept.getFileName() + ": " + p);
				}
				else if(ftp == null){
					//THIS MEANS THERE WAS NO SUCH OFFER PRESENT
					System.out.println("FRP NULL, NO SUCH OFFER PRESENT" );
				}


				break;

			case fileTransferCommand:
				// Someone is sending us a file. Write the file to our disk.
				FileTransferMessage fm = new FileTransferMessage(p.address, p.data);

				//check if file was offered before
				String offerKey = ("" + fm.getAddress() + "-" + fm.getFileSize() + "-" + fm.getFileName());
				if(offeredFileCache.getIfPresent(offerKey) != null){
					System.out.println("FILE OFFERED DETECTED!!!");
					writeFile(fm, offeredFileCache.getIfPresent(offerKey));
				}else{
					System.out.println("FILE OFFERED NOT DETECTED");
					logger.debug("Host: " + fm.getAddress() + " Had no such file offer!");
				}

				//TODO 02 investigate if current handling by GUI is desired
				logger.debug("Receiving File Transfer: " + p);
				setChanged();
				notifyObservers(fm);
				break;

			case WHOISrequestCommand:
				// Someone is requesting our identification

				byte[] data = new byte[1 + identification.length];
				data[0] = WHOISresponseCommand;
				System.arraycopy(identification, 0, data, 1, identification.length);

				try {
					networkLayer.send(new Payload(data, p.address));
				} catch (SizeLimitExceededException e) {
					logger.warning("Size Limit Exceeded:" + p + ".");
					e.printStackTrace();
				}
				break;

			case WHOISresponseCommand:
				// Someone is responding to our identification request

				//Get WHOIS without command byte
				byte[] identificationResponseData = new byte[p.data.length-1];
				System.arraycopy(p.data, 1, identificationResponseData, 0, p.data.length-1);

				//Parse WHOIS to GUI
				IdentificationMessage idResponse = new IdentificationMessage(p.address, identificationResponseData);
				setChanged();
				notifyObservers(idResponse);
				break;


			default:
				// TODO 03 find ways to catch invalid commands in a more refined manner
				throw new CommandNotFoundException(String.format("command: %c", command));
			}
		}  catch (CommandNotFoundException e) {
			logger.debug("CommandNotFoundException on: " + p);
			e.printStackTrace();
		} catch (IOException e1) {
			logger.debug("Requested file not found on local directory: " + p);
			e1.printStackTrace();
		}

	}

	/**
	 * Reads a filepath from local directory and places it in our file offer queue.
	 * A file offer is then sent to the destination host.
	 * 
	 * @requires file size < 2GB
	 * @param strFilePath
	 * @param destination
	 */
	public void writeFileOffer(String strFilePath, byte destination){

		// Split the string and retrieve only the filename in bytes
		String[] nameParts = strFilePath.split("/");
		String fileName = nameParts[nameParts.length -1];
		byte[] byteName = fileName.getBytes(Charsets.UTF_8);

		// Experimental line : FileSize
		long fileSize = (new File(strFilePath).length());
		byte[] byteFileSize = ByteBuffer.allocate(4).putInt((int) fileSize).array();

		// Form the data byte array for the offer payload
		byte[] data = new byte[5 + byteName.length];
		data[0] = fileOfferCommand;
		System.arraycopy(byteFileSize, 0, data, 1, 4);
		System.arraycopy(byteName, 0, data, 5, byteName.length);

		String key = destination + fileName;
		fileOfferCache.put(key, strFilePath);
		System.out.println("OFFERED KEY: " + key);

		// Put the offer in a new payload and send it
		Payload offer = new Payload(data, destination);

		try {
			networkLayer.send(offer);
		} catch (SizeLimitExceededException e) {
			logger.debug("Size Limit Exceeded! " + offer.toString() + ".");
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

	/**
	 * Method to write files from fileTransfers to our
	 * disk.
	 * @param FileTransferMessage
	 * @param Path to write to
	 */
	public void writeFile(FileTransferMessage fm, String path){

		try {
			Files.write(fm.getFileBytes(), new File(path));
		}catch (IOException e) {
			logger.warning("Caught IOException " + e + " while handling fileMessage " + fm);
			/* Do nothing else (i.e. drop the payload). */
		}
		//logger.debug("Started File Transfer: " + fm);
	}

	/** accepts a file offer and sends a fileAcceptMessage */
	public void acceptFileOffer(FileOfferMessage fm, String filePath){

		String key = ("" + fm.getAddress() + "-" + fm.getFileSize() + "-" + fm.getFileName());
		offeredFileCache.put(key, filePath);

		byte[] data = fm.getPayload();
		data[0] = fileAcceptCommand;

		try {
			networkLayer.send(new Payload(data, fm.getAddress()));
		} catch (SizeLimitExceededException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void run() {
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
	 * for the application, upon receiving the list of hosts the applicationLayer
	 * automatically sends a WHOIS request to all hosts listed.
	 * @return collection of hosts
	 */
	public Collection<Byte> getHosts(boolean whois) {
		Collection<Byte> hostCollection = networkLayer.hosts();

		if(whois){
			// Send a WHOIS for each host in the collection
			for (Byte h : hostCollection) {
				byte[] data = new byte[1];
				data[0] = WHOISrequestCommand;
				try {
					networkLayer.send(new Payload(data, h));
					logger.debug("WHOIS Request Sent to: " + h + ".");
				} catch (SizeLimitExceededException e) {
					logger.warning("WHOIS Request Size Limit Exceeded:" + h + ".");
					e.printStackTrace();
				}
			}
		}
		return hostCollection;
	}
	
	/**
	 * getHosts with whois defaulted to false
	 * @return collection of hosts
	 */
	public Collection<Byte> getHosts(){
		return getHosts(false);
	}

	/**
	 * Method to retrieve the address of this host.
	 * @return the address of this host.
	 */
	public Byte getHost(){
		return networkLayer.host();
	}


	/**
	 * CURRENTLY DEPRECIATED
	 * Sends out a WHOIS message to all hosts in our hostCollection after
	 * a given delay in seconds.
	 * @param delay
	 */
	public void sendWHOIS(int delay){

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		// Create a runnable that we can execute
		final Runnable WHOIS = new Runnable() {
			public void run() {
				// Get the collection of all the hosts so we can WHOIS them
				Collection<Byte> hostCollection = getHosts();

				// Send a WHOIS for each host in the collection
				for (Byte h : hostCollection) {
					byte[] data = new byte[1];
					//					System.out.println("DEBUG--------- Value of data = " + data);
					//					System.out.println(h);
					data[0] = WHOISrequestCommand;
					try {
						networkLayer.send(new Payload(data, h));
						logger.debug("WHOIS Request Sent to: " + h + ".");
					} catch (SizeLimitExceededException e) {
						logger.warning("WHOIS Request Size Limit Exceeded:" + h + ".");
						e.printStackTrace();
					}
				}
			}
		};

		ScheduledFuture<?> scheduledWHOIS = scheduler.schedule(WHOIS, delay, TimeUnit.SECONDS);
		//		System.out.println("DONEDONEDONEDONE ------ 101010100101 ");
		//scheduledWHOIS.cancel(false);
	}

	@Override
	public Thread start(Stack stack) {
		networkLayer = stack.networkLayer;

		logger.info(networkLayer.toString());
		logger.info("ApplicationLayer started.");

		thread.start();

		return thread;
	}
}
