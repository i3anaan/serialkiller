package web;

class ChatHandler extends ServiceHandler {
    @Override
    public String getPath() {
        return "/chat/";
    }

    @Override
    public Response handleRequest(Request request) {
        return new TemplateResponse("Chat.");
    }
}
