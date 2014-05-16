package web;

import stats.Stats;
import util.Environment;

import java.util.Date;

/**
 * A simple ServiceHandler that displays a table with known Stats-package
 * counters.
 */
class StatusHandler extends ServiceHandler {
    @Override
    public String getPath() {
        return "/status/";
    }

    @Override
    public Response handleRequest(Request request) {
        Response r = new TemplateResponse();

        r.out.append("<h2>Status</h2>");

        r.out.append("<h3>Environment</h3>");

        r.out.append("<table>");
        row(r, "Working directory", Environment.getWorkingDir());
        row(r, "Java version", Environment.getJavaVersion());
        row(r, "Date/time", new Date());
        row(r, "Git branch", Environment.getGitBranch());
        row(r, "Git commit", Environment.getGitCommit());
        r.out.append("</table>");

        r.out.append("<h3>Counters</h3>");

        r.out.append("<table>");
        for (String counter : Stats.getCounters()) {
            row(r, counter, Stats.getValue(counter));
        }
        r.out.append("</table>");

        return r;
    }

    private void row(Response r, Object key, Object value) {
        r.out.append(String.format("<tr><td>%s &nbsp;</td><td>%s</td></tr>", key, value));
    }
}
