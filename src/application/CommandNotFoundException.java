package application;

/**
 * Exception class for handling unknown or invalid command that are sent with
 * payload messages.
 * 
 * @author msbruning
 *
 */
public class CommandNotFoundException extends Exception {

	public CommandNotFoundException() {
		// TODO Auto-generated constructor stub
	}

	public CommandNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CommandNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public CommandNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
