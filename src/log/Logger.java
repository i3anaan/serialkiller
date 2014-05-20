package log;

import log.LogMessage.Severity;
import log.LogMessage.Subsystem;

/**
 * A connection to a central LogService. There is only one LogService, but every
 * class or subsystem can have its own Logger. Saves users from having to give
 * their Subsystem identifier all the time.
 */
public class Logger {
    /** The subsystem that 'owns' this logger. */
    private Subsystem subsys;

    /** The upstream LogService instance. Log messages are delivered here. */
    private LogService logService;

    /** Construct a new logger for an unknown subsystem. */
    public Logger() {
        this(Subsystem.UNKNOWN);
    }

    /** Construct a new logger for the given subsystem. */
    public Logger(Subsystem subsystem) {
        subsys = subsystem;
        logService = LogService.getInstance();
    }

    public void debug(String message) {
        log(Severity.DEBUG, message);
    }

    public void info(String message) {
        log(Severity.INFO, message);
    }

    public void notice(String message) {
        log(Severity.NOTICE, message);
    }

    public void warning(String message) {
        log(Severity.WARNING, message);
    }

    public void error(String message) {
        log(Severity.ERROR, message);
    }

    public void critical(String message) {
        log(Severity.CRITICAL, message);
    }

    public void alert(String message) {
        log(Severity.ALERT, message);
    }

    public void emerg(String message) {
        log(Severity.EMERG, message);
    }

    public void omg(String message) {
        log(Severity.OMG, message);
    }

    public void wtf(String message) {
        log(Severity.WTF, message);
    }

    public void bbq(String message) {
        log(Severity.BBQ, message);
    }

    /** Submit a new log message to the upstream LogService. */
    public void log(Severity sev, String message) {
        logService.add(new LogMessage(subsys, sev, message));
    }

    public String toString() {
        return "Logger";
    }
}
