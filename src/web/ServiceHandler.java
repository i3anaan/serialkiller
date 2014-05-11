package web;

/** Abstract superclass for all service handlers. */
abstract class ServiceHandler {
    public abstract String getPath();

    public abstract Response handleRequest(Request request);
}
