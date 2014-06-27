package log;

import stats.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A singleton class that manages a synchronized list of LogMessage instances.
 */
public class LogService {
    /** The minimum level of log messages we want to log. */
    private static LogMessage.Severity level = LogMessage.Severity.DEBUG;

    /** The singleton instance. */
    private static LogService instance;

    /** The main data store. */
    private List<LogMessage> messages;

    /** Build a new LogService. Called by getInstance(). */
    private LogService() {
        /* Private constructor to disallow construction */
        messages = Collections.synchronizedList(new ArrayList<LogMessage>());
    }

    /** Return the LogService singleton. */
    public static synchronized LogService getInstance() {
        if (instance == null) {
            instance = new LogService();
        }

        return instance;
    }

    /** Adds a new LogMessage. */
    public void add(LogMessage msg) {
        if (msg.getSeverity().ordinal() >= level.ordinal()) {
            Stats.hit("log.messagesLogged");
            
            synchronized (System.out) {
            	System.out.println(msg);
                System.out.flush();
            }

            messages.add(msg);
        }
    }

    /** Return an immutable view of all log messages. */
    public List<LogMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    /** Returns the current logging level. */
	public static LogMessage.Severity getLevel() {
		return level;
	}

	/** Sets the logging level. */
	public static void setLevel(LogMessage.Severity level) {
		LogService.level = level;
	}
    
    
}
