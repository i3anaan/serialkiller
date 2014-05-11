package web;

class Request {
	private String method;
	private String path;
	
	public Request(String method, String path) {
		super();
		this.method = method;
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}
}
