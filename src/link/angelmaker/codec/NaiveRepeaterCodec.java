package link.angelmaker.codec;

import util.BitSet2;

import com.google.common.base.Optional;

public class NaiveRepeaterCodec implements Codec{

	public static final int REPEAT_COUNT = 2;
	
	
	@Override
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		if(input.length()%8==0){
			BitSet2 out = new BitSet2();
			for(int i=0;i<input.length();i++){
				for(int r=0;r<REPEAT_COUNT;r++){
					out.addAtEnd(input.get(i));
				}
			}
			return out;
		}else{
			throw new IllegalArgumentException();
		}
		
	}

	@Override
	public Optional<BitSet2> decode(BitSet2 input)
			throws IllegalArgumentException {
		if(input.length()%(8*REPEAT_COUNT)==0){
			BitSet2 out = new BitSet2();
			for(int i=0;i<input.length();i=i+REPEAT_COUNT){
				for(int r=1;r<REPEAT_COUNT;r++){
					if(input.get(i)!=input.get(i+r)){
						return Optional.absent();
					}
				}
				out.addAtEnd(input.get(i));
			}
			return Optional.of(out);
		}else{
			throw new IllegalArgumentException();
		}
	}

}
