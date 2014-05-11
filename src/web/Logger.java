package web;

import web.LogMessage.Subsystem;

public class Logger {
	private LogMessage.Subsystem subsys;
	private LogService log;

	public Logger(Subsystem subsys) {
		super();
		this.subsys = subsys;
		this.log = LogService.getInstance();
	}
	
	public void debug(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.DEBUG, message));
	}
	
	public void info(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.INFO, message));
	}
	
	public void notice(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.NOTICE, message));
	}
	
	public void warning(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.WARNING, message));
	}
	
	public void error(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.ERROR, message));
	}
	
	public void critical(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.CRITICAL, message));
	}
	
	public void alert(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.ALERT, message));
	}
	
	public void emerg(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.EMERG, message));
	}
	
	public void omg(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.OMG, message));
	}
	
	public void wtf(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.WTF, message));
	}
	
	public void bbq(String message) {
		this.log.add(new LogMessage(this.subsys, LogMessage.Severity.BBQ, message));
	}
}
