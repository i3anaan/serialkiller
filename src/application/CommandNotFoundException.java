package application;

/**
 * Exception class for handling unknown or invalid command that are sent with
 * payload messages.
 * 
 * @author msbruning
 */
public class CommandNotFoundException extends Exception {

	public CommandNotFoundException() {
		
	}

	public CommandNotFoundException(String message) {
		super(message);
		
	}

	public CommandNotFoundException(Throwable cause) {
		super(cause);
		
	}

	public CommandNotFoundException(String message, Throwable cause) {
		super(message, cause);
		
	}
}
