package application.message;

import java.util.Arrays;

import com.google.common.base.Charsets;

/**
 * Representation for all chat messages in the Application layer,
 * it contains the nickname of the sender, the message and the original payload
 * @author msbruning
 *
 */
public class ChatMessage extends ApplicationLayerMessage {

	/** byte value of a chat flag */
	private static final byte chatCommand = 'C';
	
	/** byte value of nullbyte */
	private static final char nullbyte = '\0';
	
	//Private variables
	private final String nickname;
	private final String message;


	/**
	 * Constructor for incoming chatMessages
	 * @param address of the sender
	 * @param data containing the chatMessage
	 */
	public ChatMessage(byte address, byte[] data) {
		super(address, data);
		// Nickname used by the message sender, this is found by using the fact
		// that nul bytes terminate strings
		int i;
		for(i = 0; i<data.length; i++){
			if(data[i] == '\0')
				break;
		}
		nickname = new String(data, 1, i-1);
		int l = nickname.length() + 2;

		// Message part of the payload
		byte [] payload = Arrays.copyOfRange(data, l, data.length);
		message = new String(payload);
	}
	
	/**
	 * Constructor for outgoing chatMessages
	 * @param address to send to
	 * @param nickname we are using
	 * @param message we want to send
	 */
	public ChatMessage(byte address, String nickname, String message){
		super(address, writeData(nickname, message));
		this.nickname = nickname;
		this.message = message;
	}
	
	/**
	 * Combines a nickname and message into chatMessage data.
	 * @param nickname we are using
	 * @param message we are sending
	 * @return data containing the chatMessage
	 */
	private static byte[] writeData(String nickname, String message){
		
		byte[] nick = nickname.getBytes(Charsets.UTF_8);
		byte[] msg = message.getBytes(Charsets.UTF_8);

		// create new byte[] with minimum length needed
		byte[] data = new byte[nick.length + 2 + msg.length];

		// concatenate byte arrays into a new byte array
		data[0] = chatCommand;
		System.arraycopy(nick, 0, data, 1, nick.length);
		data[nick.length+1] = nullbyte;
		System.arraycopy(msg, 0, data, (nick.length+2), msg.length);
		
		return data;
	}

	/**
	 * Returns the given nickname of the sender
	 * @return nickname
	 */
	public String getNickname(){
		return nickname;
	}

	/**
	 * Returns the contents of the chat message
	 * @return message
	 */
	public String getMessage(){
		return message;
	}

}
