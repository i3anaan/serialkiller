package web;

abstract class ServiceHandler {
	public abstract String getPath();
	public abstract Response handleRequest(Request request);
}
