package web;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import network.NetworkLayer;
import network.RoutingTable;

class RoutingTableHandler extends ServiceHandler {
	@Override
	public String getPath() {
		return "/routes/";
	}

	@Override
	public Response handleRequest(Request request) {
        Response r = new TemplateResponse();
        r.out.append("<h2>Routes</h2>");
        
        try {
			RoutingTable rt = new RoutingTable(NetworkLayer.ROUTING_PATH);

			
        	r.out.append("<h3>Graph</h3>");
	        r.out.append("<img src='" + rt.toGraphUri() + "'>");
	        
	        r.out.append("<h3>Configuration</h3>");
	        r.out.append("<pre>");
	        r.out.append(Files.toString(new File(NetworkLayer.ROUTING_PATH), Charsets.UTF_8));
	        r.out.append("</pre>");
		} catch (IOException e) {
			r.out.append(e);
		}
        
        return r;
	}
}
