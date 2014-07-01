package link.angelmaker.codec;

import util.BitSet2;

import com.google.common.base.Optional;

/**
 * A Codec that is simply a combination of other codecs.
 * When encoding the data get encoded by each given Codec subsequently.
 * Decoding is done in inverse order of Codecs.
 * @author I3anaan
 *
 */
public class MixedCodec implements Codec {

	private Codec[] codecs;
	
	public MixedCodec(Codec[] codecs){
		this.codecs = codecs;
	}
	
	@Override
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		BitSet2 out = input;
		for(int i=0;i<codecs.length;i++){
			out = codecs[i].encode(out);
		}
		return out;
	}

	@Override
	public Optional<BitSet2> decode(BitSet2 input)
			throws IllegalArgumentException {
		BitSet2 out = input;
		for(int i=codecs.length-1;i>=0 && out!=null;i--){
			Optional<BitSet2> decoded = codecs[i].decode(out);
			if(decoded.isPresent()){
				out = decoded.get();
			}else{
				out = null;
			}
		}
		if(out!=null){
			return Optional.of(out);
		}else{
			return Optional.absent();
		}
	}

}
