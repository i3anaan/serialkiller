package web;

class StatsHandler extends ServiceHandler {
    @Override
    public String getPath() {
        return "/stats/";
    }

    @Override
    public Response handleRequest(Request request) {
        return new TemplateResponse("Stats.");
    }
}
