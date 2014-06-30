package web;

import java.io.File;
import java.io.IOException;
import link.angelmaker.AngelMaker;
import link.angelmaker.nodes.Node;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import common.Graph;

public class SendingGraphHandler extends ServiceHandler {

	@Override
	public String getPath() {
		return "/sendinggraph/";
	}

	@Override
	public Response handleRequest(Request request) {
		Response r = new TemplateResponse();

		r.out.append("<h2>Sending Node Graph LinkLayer</h2>");
		AngelMaker am = AngelMaker.getInstanceOrNull();
		if(am!=null){
			Node sendingNode = am.getCurrentSendingNode();
	        Graph.makeImage(Graph.getFullGraphForNode(sendingNode, true),"sending_node_graph");
	        String img;
			try {
				img = Files.toString(new File("sending_node_graph.svg"), Charsets.UTF_8);
				r.out.append(img);
			} catch (IOException e) {
				e.printStackTrace();
				r.out.append("<p>No graph available, input error.</p>");
			}
		}else{
			r.out.append("<p>No graph available, no AngelMaker instance.</p>");
		}
        return r;
	}

}
