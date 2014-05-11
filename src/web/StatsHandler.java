package web;

import stats.Stats;

class StatsHandler extends ServiceHandler {
    @Override
    public String getPath() {
        return "/stats/";
    }

    @Override
    public Response handleRequest(Request request) {
        Response r = new TemplateResponse();

        r.out.append("<table>");
        for (String counter : Stats.getCounters()) {
            String tpl = "<tr><td>%s</td><td>%d</td></tr>";
            String msg = String.format(tpl, counter, Stats.getValue(counter));
            r.out.append(msg);
        }
        r.out.append("</table>");

        return r;
    }
}