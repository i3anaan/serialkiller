package web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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

        // Add a short introductory paragraph
        r.out.append("<h2>Logs</h2>");

        // Build it!
        r.out.append("<pre>");
        for (LogMessage lm : messages) {
            r.out.append(lm);
            r.out.append('\n');
        }
        r.out.append("</pre>");

        return r;
    }
}
