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
	 */
	public String readPayload(byte[] data){
		// retrieves the first byte from the payload and converts it to a Char
		// then it is checked if the Char is a valid command and if so which one
		byte check = data[0];
		String commandString = Byte.toString(check);
		char command = commandString.charAt(0);

		// Send Chat message
		if(command == 'C'){
			// Creates a new chat message object
			ChatMessage cm = new ChatMessage(data);
			
		}
		// Send request to transfer file
		if(command == 'F'){

		}
		// Accept file transfer
		if(command == 'A'){

		}
		// Transfer file
		if(command == 'S'){

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
}
