package web;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import link.angelmaker.AngelMaker;
import link.angelmaker.nodes.Node;
import common.Graph;

public class ReceivingGraphHandler extends ServiceHandler {

	@Override
	public String getPath() {
		return "/receivinggraph/";
	}

	@Override
	public Response handleRequest(Request request) {
		Response r = new TemplateResponse();
		r.out.append("<h2>Receiving Node Graph LinkLayer</h2>");
		AngelMaker am = AngelMaker.getInstanceOrNull();
		if(am!=null){
			Node receivingNode = am.getCurrentReceivingNode();
	        Graph.makeImage(Graph.getFullGraphForNode(receivingNode, false),"receiving_node_graph");
	        String img;
			try {
				img = Files.toString(new File("receiving_node_graph.svg"), Charsets.UTF_8);
				r.out.append(img);
				//r.out.append("<style>svg{max-width:100%;}</style>");

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
