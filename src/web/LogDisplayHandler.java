package web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

class LogDisplayHandler extends ServiceHandler {
    private static final int LIMIT = 1000;
    @Override
    public String getPath() {
        return "/logs/";
    }

    @Override
    public Response handleRequest(Request request) {
        Response r = new TemplateResponse();

        Iterable<LogMessage> messages = Iterables.limit(Lists.reverse(LogService.getInstance().getMessages()), LIMIT);

        r.out.append(String.format("<p>Last %d log lines, newest first</p>", LIMIT));

        r.out.append("<pre>");
        for (LogMessage lm : messages) {
            r.out.append(lm.toString() + "\n");
        }
        r.out.append("</pre>");

        return r;
    }
}
