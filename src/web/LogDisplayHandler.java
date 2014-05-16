package web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

import log.LogMessage;
import log.LogService;

/**
 * A ServiceHandler that displays log lines.
 */
class LogDisplayHandler extends ServiceHandler {
    private static final int LIMIT = 1000;

    @Override
    public String getPath() {
        return "/logs/";
    }

    @Override
    public Response handleRequest(Request request) {
        Response r = new TemplateResponse();

        // Get the LIMIT newest logs
        LogService logger = LogService.getInstance();
        List<LogMessage> allMessages = logger.getMessages();
        List<LogMessage> reverse = Lists.reverse(allMessages);
        Iterable<LogMessage> messages = Iterables.limit(reverse, LIMIT);
        
        // Add some styles.
        puts(r, "<style>");
        puts(r, "  .DEBUG { color: #95a5a6; }");
        puts(r, "  .INFO, .NOTICE { color: #2980b9; }");
        puts(r, "  .WARNING { color: #f39c12; }");
        puts(r, "  .ERROR { color: #d35400; }");
        puts(r, "  .CRITICAL, .ALERT, .EMERG { color: #c0392b; }");
        puts(r, "  .OMG, .WTF, .BBQ { background-color: red; color: white; text-decoration: underline; font-size: 20px; font-family: 'Comic Sans MS', fantasy; }");
        puts(r, "</style>");

        // Add a short introductory paragraph
        puts(r, "<h2>Logs</h2>");

        // Build it!
        puts(r, "<table>");
        for (LogMessage lm : messages) {
        	puts(r, "<tr class='%s %s'>", lm.getSeverity(), lm.getSubsystem());
        	puts(r, "<td>[%s]</td>", lm.getSeverity());
        	puts(r, "<td>[%s]</td>", new Date(lm.getTimestamp()));
        	puts(r, "<td>[%s]</td>", lm.getSubsystem());
        	puts(r, "<td>[%s]</td>", lm.getThread());
        	puts(r, "<td>%s</td>", lm.getMessage());
        	puts(r, "</tr>");
        }
        puts(r, "</table>");

        return r;
    }
    
    private void puts(Response r, String tpl, Object... x) {
    	r.out.append(String.format(tpl, x));
    }
}
