package link.angelmaker.codec;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Optional;

import util.BitSet2;

public class ParityBitsTest  {
	@Test
	public void testEncode() {
		assertEquals(new BitSet2("0000000000"), ParityBitsCodec.encode(new BitSet2("00000000")));
		assertEquals(new BitSet2("0000000101"), ParityBitsCodec.encode(new BitSet2("00000001")));
		assertEquals(new BitSet2("1000000110"), ParityBitsCodec.encode(new BitSet2("10000001")));
		assertEquals(new BitSet2("1111000000"), ParityBitsCodec.encode(new BitSet2("11110000")));
		
		assertEquals(new BitSet2("01010101000101010100"), ParityBitsCodec.encode(new BitSet2("0101010101010101")));
	}
	
	@Test
	public void testDecode() {
		assertEquals(Optional.of(new BitSet2("00000000")), ParityBitsCodec.decode(new BitSet2("0000000000")));
		assertEquals(Optional.of(new BitSet2("00000001")), ParityBitsCodec.decode(new BitSet2("0000000101")));
		assertEquals(Optional.of(new BitSet2("10000001")), ParityBitsCodec.decode(new BitSet2("1000000110")));
		assertEquals(Optional.of(new BitSet2("11110000")), ParityBitsCodec.decode(new BitSet2("1111000000")));
		
		assertEquals(Optional.of(new BitSet2("0101010101010101")), ParityBitsCodec.decode(new BitSet2("01010101000101010100")));
		
		assertEquals(Optional.absent(), ParityBitsCodec.decode(new BitSet2("0000000011")));
	}

}
