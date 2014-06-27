package application;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import javax.naming.SizeLimitExceededException;

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

	/** Cash containing all the hosts we are offering files, mapped to the path of the FileOffer we made them */
	private Cache<String, String> fileOfferCache;

	/** Cash containing all the hosts that are offering files, mapped to the FileOffer they made us */
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
				ChatMessage cm = new ChatMessage(p.address, p.data);
				setChanged();
				notifyObservers(cm);
				break;

			case fileOfferCommand: 
				// Received a file transfer offer.
				FileOfferMessage offer = new FileOfferMessage(p.address, p.data);
				setChanged();
				notifyObservers(offer);
				logger.debug("Received FileOffer: " + p + "From host: " + p.address);
				break;

			case fileAcceptCommand:
				// Someone accepted our file offer.
				FileAcceptMessage accept = new FileAcceptMessage(p.address, p.data);

				// Check if fileOffer exists and if so send it
				String key = (accept.getAddress() + accept.getFileName());
				String ftp = fileOfferCache.getIfPresent(key);

				// There was a file offer present for this transfer
				if(ftp != null){
					// Build fileTransferMessage
					byte[] fileData = Files.toByteArray(new File(ftp));
					FileTransferMessage ftm = new FileTransferMessage(p.address, p.data, fileData);

					try {
						networkLayer.send(new Payload(ftm.getPayload(), p.address));
					} catch (SizeLimitExceededException e) {
						logger.warning("Size Limit Exceeded:" + ftm.getFileName() + ".");
						e.printStackTrace();
					}
					logger.debug("Started File Transfer for: " + ftm.getFileName() + ": " + p);
				}
				else{
					logger.warning("Host: "+ p.address +" tried to transfer a file from us we never offered!");
				}
				break;

			case fileTransferCommand:
				// Someone is sending us a file. Write the file to our disk.
				FileTransferMessage fm = new FileTransferMessage(p.address, p.data);

				//check if file was offered before
				String offerKey = (fm.getAddress() + "-" + fm.getFileSize() + "-" + fm.getFileName()).trim();
				
				if(offeredFileCache.getIfPresent(offerKey) != null){
					writeFile(fm, offeredFileCache.getIfPresent(offerKey));
					logger.info("Host: " + fm.getAddress() + " is transfering us " + fm.getFileName());
					setChanged();
					notifyObservers(fm);
				}else{
					
					logger.debug("Host: " + fm.getAddress() + " had no such file offer!");
				}
				break;

			case WHOISrequestCommand:
				// Someone is requesting our identification
				IdentificationResponseMessage irm = new IdentificationResponseMessage(p.address, p.data, identification);

				try {
					networkLayer.send(new Payload(irm.getPayload(), p.address));
				} catch (SizeLimitExceededException e) {
					logger.warning("Size Limit Exceeded:" + p + ".");
					e.printStackTrace();
				}
				break;

			case WHOISresponseCommand:
				// Someone is responding to our identification request
				IdentificationResponseMessage idResponse = new IdentificationResponseMessage(p.address, p.data);
				setChanged();
				notifyObservers(idResponse);			
				break;

			default:
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
		logger.debug("Writing file to disk: " + fm);
	}

	/**
	 * Creates a new fileOfferMessage, adds it to out fileOfferCache 
	 * and then sends it.
	 * @requires file size < 2GB
	 * @param file path of the offered file
	 * @param destination to send to
	 */
	public void sendFileOffer(String strFilePath, byte destination){
		FileOfferMessage fom = new FileOfferMessage(destination, strFilePath);

		String key = destination + fom.getFileName();
		fileOfferCache.put(key, strFilePath);
		
		try {
			networkLayer.send(new Payload(fom.getPayload(), destination));
		} catch (SizeLimitExceededException e) {
			logger.debug("Size Limit Exceeded! " + fom.getFileName() + ".");
			e.printStackTrace();
		}
	}
	
	/** accepts a file offer and sends a fileAcceptMessage */
	public void sendFileAccept(FileOfferMessage fm, String filePath){
		FileAcceptMessage fam = new FileAcceptMessage(fm.getAddress(), fm.getPayload());
		
		String key = (fm.getAddress() + "-" + fm.getFileSize() + "-" + fm.getFileName()).trim();
		offeredFileCache.put(key, filePath);
		
		try {
			networkLayer.send(new Payload(fam.getPayload(), fam.getAddress()));
		} catch (SizeLimitExceededException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new ChatMessage using the provided parameters and sends it
	 * @param String containing nickname
	 * @param String containing chat message
	 * @param byte   the destination of the chat message as an address
	 */
	public void sendChatMessage(String nickname, String message, byte destination) {
		ChatMessage cm = new ChatMessage(destination, nickname, message);
		
		try {
			networkLayer.send(new Payload(cm.getPayload(), destination));
		} catch (SizeLimitExceededException e) {
			logger.warning("Size Limit Exceeded:" + ".");
			e.printStackTrace();
			/* Drop the message. */
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
				try {
					networkLayer.send(new Payload(new IdentificationRequestMessage(h).getPayload(), h));
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

	@Override
	public Thread start(Stack stack) {
		networkLayer = stack.networkLayer;

		logger.info(networkLayer.toString());
		logger.info("ApplicationLayer started.");

		thread.start();

		return thread;
	}
}