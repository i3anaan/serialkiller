package web;

import stats.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A singleton class that manages a synchronized list of LogMessage instances.
 */
public class LogService {
    /** The minimum level of log messages we want to log. */
    private static final LogMessage.Severity LEVEL = LogMessage.Severity.DEBUG;

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
        if (msg.getSeverity().ordinal() >= LEVEL.ordinal()) {
            Stats.hit("log.messagesLogged");
            messages.add(msg);
        }
    }

    /** Return an immutable view of all log messages. */
    public List<LogMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
