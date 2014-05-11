package web;

class FilesHandler extends ServiceHandler {
    @Override
    public String getPath() {
        return "/files/";
    }

    @Override
    public Response handleRequest(Request request) {
        return new TemplateResponse("Files.");
    }
}
