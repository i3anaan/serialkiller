package network;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Base handler class for network layer handlers.
 */
public abstract class Handler implements Runnable {
    private static final int QUEUE_SIZE = 64;
    private Thread t;
    private boolean run;

    /** Queue for data travelling to the network layer. Is the same queue for
     * every handler. No data should be taken out of this queue. */
    protected ArrayBlockingQueue<Packet> in;

    /** Queue for data travelling out of the network layer. Every Handler has
     * its own queue. Writing to this queue is not recommended, but all data
     * should be taken out if a handler has a responsibility of handling
     * outgoing data.
     */
    protected ArrayBlockingQueue<Packet> out;

    /**
     * Handles setup for new Handler subclass instances.
     * @param parent The parent NetworkLayer instance.
     */
    public Handler(NetworkLayer parent) {
        this.in = parent.queue;
        this.out = new ArrayBlockingQueue<Packet>(QUEUE_SIZE, true);

        t = new Thread(this);
    }

    /**
     * Method that actually handles the packets in the queues. This method gets
     * called in a loop as long as the thread runs.
     */
    public abstract void handle();

    /**
     * Starts the handler.
     */
    public void start() {
        run = true;
        t.start();
    }

    /**
     * Stops the handler. Running operations will be fully executed before the
     * thread stops.
     */
    public void stop() {
        run = false;
        try {
            t.join();
        } catch (InterruptedException e) {
            // TODO: Log this exception
        }
    }

    @Override
    public void run() {
        while (run) {
            handle();
        }
    }

}
