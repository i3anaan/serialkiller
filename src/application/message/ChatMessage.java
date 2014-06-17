package application.message;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;

import network.Payload;

/**
 * Representation for all chat messages in the Application layer,
 * it contains the nickname of the sender, the message and the original payload
 */
public class ChatMessage extends ApplicationLayerMessage {
	//Private variables
	private final String nickname;
	private final String message;
	
	private static final Charset UTF = Charsets.UTF_8;

	public ChatMessage(Payload payload) {
		super(payload);
		byte[] data = payload.data;
		int nick_end = Bytes.indexOf(data, (byte)'\0');
		
		nickname = new String(data, 1, nick_end - 1, UTF);
		message  = new String(data, nick_end + 1, data.length - nick_end - 1, UTF);
	}
	
	public ChatMessage(byte destination, String nickname, String message) {
		super(destination);
		this.nickname = nickname;
		this.message = message;
		setData(String.format("C%s\0%s", nickname, message).getBytes(UTF));
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
