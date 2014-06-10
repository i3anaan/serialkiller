package common;

import java.io.File;

import util.BitSet2;
import link.angelmaker.AngelMaker;
import link.angelmaker.nodes.FrameCeptionNode;
import link.angelmaker.nodes.Node;

public class Graph {
	private static int graphID;
	private static final String baseColor = "Lavender";
	private static final String correctColor = "PaleGreen";
	private static final String inCorrectColor = "LightCoral";
	private static final String notFullColor = "Lavender";
	private static final String readyColor = "Orchid";
	
	private static final String shape = "box";
	private static final String style = "filled";
	private static final String fontName = "arial";
	
	public static void main(String[] args){
		//Make example graph.
		Node node = new FrameCeptionNode<Node>(null, 3);
		BitSet2 bs = new BitSet2();
		for(int i=0;i<150;i++){
			bs.addAtEnd(Math.random()>0.5);
		}
		node.giveOriginal(bs);
		System.out.println(getFullGraphForNode(node, true));
		
		String type = "png";
		File out = new File("graphTest." + type);
		GraphViz gv = new GraphViz();
		gv.writeGraphToFile( gv.getGraph( getFullGraphForNode(node, true), type ), out );
	}
	
	
	
	public static String getNodeStyle(){
		return "node[shape=\""+shape+"\",fillcolor=\""+baseColor+"\",style=\""+style+"\",fontname=\""+fontName+"\"];";
	}
	
	public static void makeImage(String graph){
		makeImage(graph,"graphTest");
	}
	public static void makeImage(String graph,String name){
		String type = "png";
		File out = new File(name+"." + type);
		GraphViz gv = new GraphViz();
		gv.writeGraphToFile( gv.getGraph( graph, type ), out );
	}
	
	public static String getFullGraphForNode(Node node,boolean sendingNode){
		StringBuilder sb = new StringBuilder();
		sb.append("digraph status{"+"\n");
		sb.append(getNodeStyle()+"\n");
		sb.append(getLegend(sendingNode)+"\n");
		sb.append(getNodeSubGraph(node, sendingNode)+"\n");
		sb.append("role[label=\""+(sendingNode ? "Sending" : "Receveiving")+"\"];filler->role[style=\"invis\"]; role->0;\n}");
		return sb.toString();
	}
	
	public static String getLegend(boolean sendingNode){
		String s = "subgraph cluster_legend{"
				+ "\n\tcorrect[label=\""+"Full and correct"+"\",fillcolor=\""+correctColor+"\"];"
				+ "\n\tincorrect[label=\""+"Full but incorrect"+"\",fillcolor=\""+inCorrectColor+"\"];"
				+ "\n\tflag[label=\""+"Ready, but not full"+"\",fillcolor=\""+readyColor+"\"];"
				+ "\n\tincomplete[label=\""+"Not full or ready"+"\",fillcolor=\""+notFullColor+"\"];"
				+ "\n\tfiller[style=\"invis\"];"
				+ "\n\ttext[label=\""+(sendingNode ? "Class\\nOriginal\\nConverted\\nState" : "Class\\nConverted\\nOriginal\\nState")+"\",fillcolor=\""+notFullColor+"\"];"
				+ "\n\t};";
		return s;
		
	}
	
	public static String getNodeSubGraph(Node base, boolean sendingNode){
		graphID=0;
		StringBuilder sb = new StringBuilder();
		sb.append("subgraph nodes{");
		addGraphVertice(sb, base, graphID, sendingNode);
		sb.append("\n\t};");
		return sb.toString();
	}
	
	public static void addGraphVertice(StringBuilder sb,Node base, int parentID,boolean sendingNode){
		if(base.getParent()!=null){
			graphID++;
			if(sendingNode){
				sb.append(parentID+ "->"+graphID+";");
			}else{
				sb.append(graphID+ "->"+parentID+";");
			}
		}
		String label = base.getClass().getSimpleName()
				+"\\n"+(sendingNode ? base.getOriginal() : base.getConverted())
				+"\\n"+(sendingNode ? base.getConverted() : base.getOriginal())
				+"\\n"+base.getStateString();
		String colorToUse = base.isFull() ? (base.isCorrect() ? correctColor : inCorrectColor) : notFullColor;
		if(colorToUse.equals(notFullColor) && base.isReady()){
			colorToUse = readyColor;
		}
		sb.append("\n\t"+graphID+"[label=\""+label+"\",fillcolor=\""+colorToUse+"\"];");
		
		if(base instanceof Node.Internal){
			int thisID = graphID;
			for(Node n : ((Node.Internal)base).getChildNodes()){
				addGraphVertice(sb,n, thisID,sendingNode);
			}
		}
	}
}