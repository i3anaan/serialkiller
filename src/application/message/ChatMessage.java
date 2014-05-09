package application.message;

import java.util.Arrays;

public class ChatMessage extends ApplicationLayerMessage {

	//Private variables
	private final String nickname;
	private final String message;
	
	
	/**
	 * Representation for all chat messages in the Application layer,
	 * it contains the nickname of the sender, the message and the original payload
	 * @param data The payload of this message
	 */
	public ChatMessage(byte[] data) {
		super(data);
		// Nickname used by the message sender, this is found by using the fact
		// that nul bytes terminate strings
			nickname = data.toString();
			int l = nickname.length();
			
		// Message part of the payload
			byte [] payload = Arrays.copyOfRange(data, l, data.length);
			message = payload.toString();
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
