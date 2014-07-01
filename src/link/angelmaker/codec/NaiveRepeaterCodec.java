package link.angelmaker.codec;

import util.BitSet2;

import com.google.common.base.Optional;

/**
 * Utilizes repetition code.
 * Simply repeats every bit the given amount of times.
 * When decoding it expects every bit to appear the same amount of times in a row.
 * If not, it is considered incorrect.
 * 
 * No attempt to correct is made (if for example 4 out of 5 say 1, it is most likely a 1).
 * @author I3anaan
 *
 */
public class NaiveRepeaterCodec implements Codec{

	private int repeatCount = 2;
	
	public NaiveRepeaterCodec(int repeatCount){
		this.repeatCount = repeatCount;
	}
	
	
	@Override
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		if(input.length()%8==0){
			BitSet2 out = new BitSet2();
			for(int i=0;i<input.length();i++){
				for(int r=0;r<repeatCount;r++){
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
		if(input.length()%(8*repeatCount)==0){
			BitSet2 out = new BitSet2();
			for(int i=0;i<input.length();i=i+repeatCount){
				for(int r=1;r<repeatCount;r++){
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
