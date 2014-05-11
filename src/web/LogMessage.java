package web;

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

    /** Create a new log message with the given properties. */
    public LogMessage(Subsystem sys, Severity sev, String msg) {
        subsystem = sys;
        severity = sev;
        message = msg;
        timestamp = System.currentTimeMillis();
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

    public String getMessage() {
        return message;
    }

    public String toString() {
        return String.format("[%s] [%s] [%s] %s",
                severity,
                new Date(timestamp),
                subsystem,
                message);
    }

    public enum Subsystem {
        APPLICATION, NETWORK, LINK, PHYS, TUNNEL, WEB, UNKNOWN
    }

    public enum Severity {
        DEBUG, INFO, NOTICE, WARNING, ERROR, CRITICAL, ALERT, EMERG, OMG, WTF, BBQ
    }
}
