package link.angelmaker.nodes;

import javax.xml.bind.DataBindingException;

import link.angelmaker.AngelMaker;
import util.BitSet2;

public class FillablePureNode extends PureNode implements Node.Fillable {

	private boolean isFiller;
	private boolean fillerSet;
	private String fillerString;
	
	public FillablePureNode(Node parent, int dataBitCount) {
		super(parent, dataBitCount);
		fillerString = "1";
		for(int i=0;i<dataBitCount;i++){
			fillerString = fillerString+"1";
		}
	}

	@Override
	public BitSet2 getConverted(){
		return BitSet2.concatenate(new BitSet2(new boolean[]{isFiller}), getOriginal());
	}
	
	@Override
	public BitSet2 giveConverted(BitSet2 bits){
		if(fillerSet==false && bits.length()>0){
			isFiller = bits.get(0);
			bits = bits.get(1,bits.length());
			fillerSet = true;
		}
		return super.giveConverted(bits);
		
	}
	
	@Override
	public BitSet2 getOriginal(){
		if(isFiller){
			return super.getOriginal();
		}else{
			return new BitSet2();
		}
	}
	
	@Override
	public Node getFiller(){
		Node node = new FillablePureNode(parent, dataBitCount);
		node.giveConverted(new BitSet2(fillerString));
		return node;
	}
	
	@Override
	public boolean isFiller() {
		return isFiller;
	}

}
