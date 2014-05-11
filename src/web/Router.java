package web;

import java.util.ArrayList;
import java.util.List;

class Router {
	private List<Class<? extends ServiceHandler>> handlers;
	
	public Router() {
		handlers = new ArrayList<Class<? extends ServiceHandler>>();
	}
	
	public ServiceHandler route(Request r) {
		for (Class<? extends ServiceHandler> klass : handlers) {
			try {
				ServiceHandler sh = klass.newInstance();
				if (r.getPath().equals(sh.getPath())) return sh;
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		
		return new ErrorServiceHandler();
	}
	
	public void register(Class<? extends ServiceHandler> klass) {
		handlers.add(klass);
	}
}
