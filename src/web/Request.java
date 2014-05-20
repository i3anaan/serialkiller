package web;

/** Represents a single request to a ServiceHandler. */
class Request {
    private String method;
    private String path;

    /** Create a new Request. */
    Request(String requestMethod, String requestPath) {
        method = requestMethod;
        path = requestPath;
    }

    /** Returns the method (i.e. "GET" or "POST") for this Request. */
    public String getMethod() {
        return method;
    }

    /** Returns the path (i.e. "/") for this Request. */
    public String getPath() {
        return path;
    }
}
