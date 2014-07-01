package link.angelmaker.codec;

import util.BitSet2;
import util.encoding.HammingCode;

import com.google.common.base.Optional;

/**
 * Codec that makes use of the HammingCode class.
 * Applies HammingCode to the given data.
 * @author I3anaan
 *
 */
public class HammingCodec implements Codec{

	private HammingCode codec;
	
	public HammingCodec(int dataBitCount){
		this.codec = new HammingCode(dataBitCount);
	}
	
	@Override
	public BitSet2 encode(BitSet2 input) throws IllegalArgumentException {
		if(input.length()%codec.dataBitCount==0){
			BitSet2 encoded = new BitSet2();
			for(int i=0;i<input.length();i=i+codec.dataBitCount){
				encoded.addAtEnd(codec.encode(input.get(i,i+codec.dataBitCount)));
			}
			return encoded;
		}else{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Optional<BitSet2> decode(BitSet2 input) throws IllegalArgumentException {
		if(input.length()%codec.encodedBitCount==0){
			BitSet2 decoded = new BitSet2();
			for(int i=0;i<input.length();i=i+codec.encodedBitCount){
				if(!codec.hasError(input.get(i,i+codec.encodedBitCount))){	
					decoded.addAtEnd(codec.decode(input.get(i,i+codec.encodedBitCount)));
				}else{
					return Optional.absent();
				}
			}
			return Optional.of(decoded);
		}else{
			throw new IllegalArgumentException();
		}
	}
}
