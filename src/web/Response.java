package web;

class Response {
    public StringBuilder out;
    public HttpCode code;
    public ContentType contentType;

    public enum HttpCode {
        Ok("200 OK"),
        Created("201 Created"),
        Accepted("202 Accepted"),
        NoContent("204 No Content"),
        MovedPermanently("301 Moved Permanently"),
        MovedTemporarily("302 Moved Temporarily"),
        NotModified("304 Not Modified"),
        BadRequest("400 Bad Request"),
        Unauthorized("401 Unauthorized"),
        Forbidden("403 Forbidden"),
        NotFound("404 Not Found"),
        InternalServerError("500 Internal Server Error"),
        NotImplemented("501 Not Implemented"),
        BadGateway("502 Bad Gateway"),
        ServiceUnavailable("503 Service Unavailable");

        private final String repr;
        HttpCode(String repr) { this.repr = repr; }
    }

    public enum ContentType {
        textPlain("text/plain"),
        textHtml("text/html");

        private final String repr;
        ContentType(String repr) { this.repr = repr; }
    }

    public Response() {
        this("");
    }

    public Response(String s) {
        out = new StringBuilder(s);
        code = HttpCode.Ok;
        contentType = ContentType.textHtml;
    }

    protected void buildHeader(StringBuilder resp) {
        resp.append(String.format("HTTP/1.0 %s\r\n", code.repr));
        resp.append(String.format("Content-Type: %s\r\n", contentType.repr));
        resp.append("\r\n");
    }

    protected void buildContent(StringBuilder resp) {
        resp.append(out);
    }

    public String asString() {
        StringBuilder resp = new StringBuilder();
        buildHeader(resp);
        buildContent(resp);
        return resp.toString();
    }
}
