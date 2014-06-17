package application.message;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Representation for all chat messages in the Application layer,
 * it contains the nickname of the sender, the message and the original payload
 * @author msbruning
 *
 */
public class ChatMessage extends ApplicationLayerMessage {

	//Private variables
	private final String nickname;
	private final String message;


	/**
	 * @param message payload
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
