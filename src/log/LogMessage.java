package log;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

import java.util.Date;

/**
 * Represents a single log message. Immutable.
 */
public class LogMessage {
    /** Origin of this LogMessage, i.e. the subsystem that caused the log. */
    private Subsystem subsystem;

    /** Severity of this message, ranging from DEBUG to BBQ. */
    private Severity severity;

    /** Moment this message was logged as a currentTimeMillis timestamp. */
    private long timestamp;

    /** The actual log message. */
    private String message;
    
    /** The thread that logged the message. */
    private String thread;

    /** Create a new log message with the given properties. */
    public LogMessage(Subsystem sys, Severity sev, String msg) {
        subsystem = sys;
        severity = sev;
        message = msg;
        timestamp = System.currentTimeMillis();
        thread = Thread.currentThread().getName();
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public Severity getSeverity() {
        return severity;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    public String getThread() {
    	return thread;
    }

    public String getMessage() {
        return message;
    }

    public String getEscapedMessage() {
        Escaper escaper = HtmlEscapers.htmlEscaper();
        return escaper.escape(getMessage());
    }

    @Override
	public String toString() {
        return String.format("[%-8s] [%s] [%4s] [%s] %s",
                severity,
                new Date(timestamp),
                subsystem,
                thread,
                message);
    }

    public enum Subsystem {
        APPLICATION, NETWORK, LINK, PHYS, TUNNEL, WEB, STARTER, UNKNOWN
    }

    public enum Severity {
        DEBUG, INFO, NOTICE, WARNING, ERROR, CRITICAL, ALERT, EMERG, OMG, WTF, BBQ
    }
}
