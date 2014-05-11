package web;

/** Represents a single log message. Immutable. */
public class LogMessage {
	public enum Subsystem {
		APPLICATION, NETWORK, LINK, PHYS, TUNNEL, WEB;
	}

	public enum Severity {
		DEBUG, INFO, NOTICE, WARNING, ERROR, CRITICAL, ALERT, EMERG, OMG, WTF, BBQ;
	}

	private Subsystem subsystem;
	private Severity severity;
	private long timestamp;
	private String message;

	public LogMessage(Subsystem sys, Severity sev, String msg) {
		super();
		this.subsystem = sys;
		this.severity = sev;
		this.message = msg;
		this.timestamp = System.currentTimeMillis();
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
				getSeverity(),
				new java.util.Date(getTimestamp()), 
				getSubsystem(), 
				getMessage());
	}
}
