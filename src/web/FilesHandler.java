package web;

/**
 * A ServiceHandler that allows the sending and receiving of files.
 */
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
