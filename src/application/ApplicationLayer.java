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
import com.google.common.primitives.Ints;

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

	/** Cache containing all the hosts we are offering files to, mapped to the FileOfferPayload we made them. */
	private Cache<String, String> fileOfferCache;

	/** Cache containing all the hosts that are offering files to us, mapped to the FileOfferPayload they made us. */
	private Cache<String, String> offeredFileCache;

	/** Initial bytes for all commands. */
	private static final byte CHAT_CMD = 'C';
	private static final byte FILE_OFFER_CMD = 'F';
	private static final byte FILE_ACCEPT_CMD = 'A';
	private static final byte FILE_TRANSFER_CMD = 'S';
	private static final byte WHOIS_REQUEST_CMD = 'W';
	private static final byte WHOIS_RESPONSE_CMD = 'I';

	/** Our identification string. */
	private final static String label ="<html>4evr \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Connexx0rred with tha bestest proto implementation \u2605\u2605\u2605 SerialKiller \u2605\u2605\u2605 <font color=#ff0000>Brought to you by Squeamish, i3anaan, TheMSB, jjkester</font> \u00AF\\_(\u30C4)_/\u00AF - Regards to all our friends: Jason, Jack, Patrick, Ghostface, Jigsaw, Hannibal, John and Sweeney \u0F3C \u1564\uFEFF\u25D5\u25E1\u25D5\uFEFF \u0F3D\uFEFF\u1564\uFEFF <font color=#009900>Smoke weed every day #420 \u0299\u029F\u1D00\u1D22\u1D07 \u026A\u1D1B</font> --- Send warez 2 <a href='https://sk.twnc.org/'>sk.twnc.org</a>, complaints to /dev/null --- The more the merrier: serial killer = best killer --- Word of the day: hacksaw \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Thanks for taking the time to receive this message, 4evr out.";

	/** Our ID as an array of bytes. */
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
			case CHAT_CMD:
				// Received a chat message.
				// Create a new chat message object and notifies the GUI
				ChatMessage cm = new ChatMessage(p.address, p.data);
				setChanged();
				notifyObservers(cm);
				break;

			case FILE_OFFER_CMD: 
				// Received a file transfer offer.
				FileOfferMessage offer = new FileOfferMessage(p.address, p.data);

				//TODO 01 BUILD CACHE HERE?

				setChanged();
				notifyObservers(offer);
				logger.debug("Received FileOffer: " + p + "From host: " + p.address);
				break;

			case FILE_ACCEPT_CMD:
				// Someone accepted our file offer.
				FileAcceptMessage accept = new FileAcceptMessage(p.address, p.data);

				// Get key from FileMessage
				String key = (accept.getAddress() + accept.getFileName());

				// Check if fileOffer exists and if so send it
				String ftp = fileOfferCache.getIfPresent(key);

				if (ftp != null){
					byte[] fileTransferData = writeTransferMessage(p.data, ftp);

					Payload transfer = new Payload(fileTransferData, p.address);

					try {
						networkLayer.send(transfer);
					} catch (SizeLimitExceededException e) {
						logger.warning("Size Limit Exceeded:" + p + ".");
						e.printStackTrace();
					}

					logger.debug("Started File Transfer for: " + accept.getFileName() + ": " + p);
				} else {
					logger.warning("Accept for unoffered file received - someone asked us for a file we don't have");
				}

				break;

			case FILE_TRANSFER_CMD:
				// Someone is sending us a file. Write the file to our disk.
				FileTransferMessage fm = new FileTransferMessage(p.address, p.data);

				// Check if file was offered before
				String offerKey = (fm.getAddress() + "-" + fm.getFileSize() + "-" + fm.getFileName()).trim();
				String offerPath = offeredFileCache.getIfPresent(offerKey);
				if (offerPath != null) {
					writeFile(fm, offerPath);
				} else{
					logger.warning("Host " + fm.getAddress() + " sent us " + offerKey + " which we didn't accept!");
				}

				// TODO Investigate if current handling by GUI is desired
				logger.debug("Receiving File Transfer: " + p);
				setChanged();
				notifyObservers(fm);
				break;

			case WHOIS_REQUEST_CMD:
				// Someone is requesting our identification
				byte[] data = new byte[1 + identification.length];
				data[0] = WHOIS_RESPONSE_CMD;
				System.arraycopy(identification, 0, data, 1, identification.length);

				try {
					networkLayer.send(new Payload(data, p.address));
				} catch (SizeLimitExceededException e) {
					logger.warning("Size Limit Exceeded:" + p + ".");
					e.printStackTrace();
				}
				break;

			case WHOIS_RESPONSE_CMD:
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
			logger.error("CommandNotFoundException on: " + p);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException in ApplicationLayer: " + e);
			e.printStackTrace();
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
	public void writeFileOffer(String strFilePath, byte destination) {
		// Split the string and retrieve only the filename in bytes
		String[] nameParts = strFilePath.split("/");
		String fileName = nameParts[nameParts.length -1];
		byte[] byteName = fileName.getBytes(Charsets.UTF_8);

		// Experimental line : FileSize
		int fileSize = (int) new File(strFilePath).length();
		byte[] byteFileSize = Ints.toByteArray(fileSize);

		// Form the data byte array for the offer payload
		byte[] data = new byte[5 + byteName.length];
		data[0] = FILE_OFFER_CMD;
		System.arraycopy(byteFileSize, 0, data, 1, 4);
		System.arraycopy(byteName, 0, data, 5, byteName.length);

		System.out.println("byteFileSize : " + byteFileSize[0] + "-" + byteFileSize[1] + "-" + byteFileSize[2] + "-" + byteFileSize[3]);
		System.out.println("index 1 = " + data[1]);
		System.out.println("index 2 = " + data[2]);
		String key = destination + fileName;
		fileOfferCache.put(key, strFilePath);

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
		data[0] = CHAT_CMD;
		System.arraycopy(nick, 0, data, 1, nick.length);
		data[nick.length+1] = 0;
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
	 * Builds the payload for a fileTransferMessage
	 * @param payload of the acceptMessage
	 * @param path of the file to transfer
	 * @return payload of fileTransferMessage
	 * @throws IOException 
	 */
	public byte[] writeTransferMessage(byte[] data, String ftp) throws IOException {
		byte[] fileData = Files.toByteArray(new File(ftp));

		// Use the data from the offer to form the filetransfer data
		byte[] fileTransferData = new byte[data.length + 1 + fileData.length];
		
		fileTransferData[0] = FILE_TRANSFER_CMD;
		System.arraycopy(data, 1, fileTransferData, 1, (data.length - 1) );
		fileTransferData[data.length] = 0;
		System.arraycopy(fileData, 0, fileTransferData, data.length + 1, fileData.length);
	
		return fileTransferData;
	}

	/**
	 * Method to write files from fileTransfers to our
	 * disk.
	 * @param FileTransferMessage
	 * @param Path to write to
	 */
	public void writeFile(FileTransferMessage fm, String path) {
		try {
			Files.write(fm.getFileBytes(), new File(path));
		}catch (IOException e) {
			logger.warning("Caught IOException " + e + " while handling fileMessage " + fm);
			// Do nothing else (i.e. drop the payload).
		}
		
		logger.debug("Started File Transfer: " + fm);
	}

	/** accepts a file offer and sends a fileAcceptMessage */
	public void acceptFileOffer(FileOfferMessage fm, String filePath) {
		String key = (fm.getAddress() + "-" + fm.getFileSize() + "-" + fm.getFileName()).trim();
		offeredFileCache.put(key, filePath);
		System.out.println("PUT KEY: " + key.hashCode());

		byte[] data = fm.getPayload();
		data[0] = FILE_ACCEPT_CMD;

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

			// Incoming payload
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
				data[0] = WHOIS_REQUEST_CMD;
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
	public Collection<Byte> getHosts() {
		return getHosts(false);
	}

	/**
	 * Method to retrieve the address of this host.
	 * @return the address of this host.
	 */
	public Byte getHost() {
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
