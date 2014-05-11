package web;

/**
 * A ServiceHandler that shows an error message.
 */
class ErrorServiceHandler extends ServiceHandler {
    @Override
    public String getPath() {
        return "/500";
    }

    @Override
    public Response handleRequest(Request request) {
        Response r = new TemplateResponse();
        r.code = Response.HttpCode.InternalServerError;
        r.out.append("An error occurred. Please check the logs for details.");
        return r;
    }

}
