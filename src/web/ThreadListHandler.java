package web;

import java.util.Set;

class ThreadListHandler extends ServiceHandler {

	@Override
	public String getPath() {
		return "/threads/";
	}

	@Override
	public Response handleRequest(Request request) {
        Response r = new TemplateResponse();
        
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        
        puts(r, "<h2>Threads</h2>");
        
        puts(r, "<table>");
        for (Thread t : threadSet) {
        	puts(r, "<tr>");
        	puts(r, "<td>%s</td><td>%s</td>", t.getId(), t.getName());
        	StackTraceElement[] elems = t.getStackTrace();
        	
        	if (elems.length > 0) {
        		puts(r, "<td>%s</td>", elems[0].getClassName());
        		puts(r, "<td>%s</td>", elems[0].getMethodName());
        		puts(r, "<td>%s</td>", elems[0].getLineNumber());
        	} else {
        		puts(r, "<td colspan='3'></td>");
        	}
        	puts(r, "</tr>");
        }
        puts(r, "</table>");

        return r;
	}

    private void puts(Response r, String tpl, Object... x) {
    	r.out.append(String.format(tpl, x));
    }
}
