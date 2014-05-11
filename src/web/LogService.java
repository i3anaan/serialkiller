package web;

import stats.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogService {
	private static LogService instance;
	
	private static final LogMessage.Severity LEVEL = LogMessage.Severity.DEBUG;
	
	private List<LogMessage> messages;
	
	private LogService() {
		/* Private constructor to disallow construction */
		
		messages = Collections.synchronizedList(new ArrayList<LogMessage>());
	}
	
	public static LogService getInstance() {
		if (instance == null) instance = new LogService();
		return instance;
	}
	
	public void add(LogMessage msg) {
		if (msg.getSeverity().ordinal() >= LEVEL.ordinal()) {
			System.out.println(msg.toString());
            Stats.hit("log.messagesLogged");
			messages.add(msg);
		}
	}

    public List<LogMessage> getMessages() {
        return messages;
    }
}
