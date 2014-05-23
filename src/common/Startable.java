package common;

/**
 * An interface that every piece of code that can be started by the Starter
 * should implement. 
 */
public interface Startable {
	/**
	 * Starts the module that implements this interface.
	 * 
	 * All implementations of Startable must implement a no-arg constructor.
	 * This start method should return when the module it represents is completely
	 * ready to use.
	 * 
	 * @returns The module's main thread if it starts any, or null otherwise.
	 */
	public Thread start(Stack stack);
}
