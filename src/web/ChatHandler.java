package web;

/**
 * A ServiceHandler that allows sending and receiving chat messages.
 */
class ChatHandler extends ServiceHandler {
    @Override
    public String getPath() {
        return "/chat/";
    }

    @Override
    public Response handleRequest(Request request) {
        return new TemplateResponse("<h2>Chat</h2>");
    }
}
