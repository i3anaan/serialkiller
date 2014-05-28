package link.angelmaker.nodes;

/**
 * A Fun Frame node, to show how powerful ANGEL_MAKER is.
 * @author I3anaan
 *
 * @param <N>
 */
public class FrameCeptionNode<N extends Node> extends FrameNode<N> {
	public FrameCeptionNode(Node parent,int count){
		super(parent,10);
		if(count>0){
			for(int i=0;i<nodes.length;i++){
				nodes[i] = new FrameCeptionNode<Node>(this, count-1);
			}
		}
	}
}
