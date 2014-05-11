package web;

class IndexHandler extends ServiceHandler {
	public String getPath() {
		return "/";
	}

	@Override
	public Response handleRequest(Request request) {
		Response r = new TemplateResponse();
		r.out.append("Index.");
		return r;
	}
}
