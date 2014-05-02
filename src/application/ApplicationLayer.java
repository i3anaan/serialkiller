package application;

import java.util.Arrays;

import application.message.*;

/*
 * Main ApplicationLayer, interpets and writes payload data
 */
public class ApplicationLayer {

	public ApplicationLayer(){

	}


	/**
	 * Reads payload by checking the payload for commands
	 * @param data
	 * @return
	 * @throws CommandNotFoundException 
	 */
	public String readPayload(byte[] data) throws CommandNotFoundException{
		// Retrieves command char from payload
		char command = getCommand(data);

		// Send Chat message
		if(command == 'C'){
			// Creates a new chat message object
			ChatMessage cm = new ChatMessage(data);
			System.out.println("Nickname: " +cm.getNickname()+"\n");
			System.out.println("Message: " +cm.getMessage()+"\n");
			
		}
		// Send request to transfer file
		else if(command == 'F'){

		}
		// Accept file transfer
		else if(command == 'A'){

		}
		// Transfer file
		else if(command == 'S'){

		}
		else{
			// TODO find ways to catch invalid commands in a more refined manner
			throw new CommandNotFoundException(String.format("command: %c", command));
			
		}

		String d = new String(data);
		return d;
	}

	/**
	 * Converts a HexString into a byte array
	 * @param s String to be converted
	 * @return b byte array
	 */
	public static byte[] writePayload(String s){

		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;

	}
	
	/**
	 * Retrieves the first byte from the payload and converts it to a Char.
	 * @param data
	 * @return The Char representing the command in this payload
	 */
	private char getCommand(byte[] data){
		
		//byte check = data[0];
		//String commandString = Byte.toString(check);
		//char command = commandString.charAt(0);
		
		return (char)data[0];
	}
}
