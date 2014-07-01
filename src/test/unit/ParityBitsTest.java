package test.unit;

import static org.junit.Assert.*;
import link.angelmaker.codec.ParityBitsCodec;

import org.junit.Test;

import com.google.common.base.Optional;

import util.BitSet2;

public class ParityBitsTest  {
	@Test
	public void testEncode() {
		ParityBitsCodec codec = new ParityBitsCodec();
		assertEquals(new BitSet2("0000000000"), codec.encode(new BitSet2("00000000")));
		assertEquals(new BitSet2("0000000101"), codec.encode(new BitSet2("00000001")));
		assertEquals(new BitSet2("1000000110"), codec.encode(new BitSet2("10000001")));
		assertEquals(new BitSet2("1111000000"), codec.encode(new BitSet2("11110000")));
		
		assertEquals(new BitSet2("01010101000101010100"), codec.encode(new BitSet2("0101010101010101")));
	}
	
	@Test
	public void testDecode() {
		ParityBitsCodec codec = new ParityBitsCodec();
		assertEquals(Optional.of(new BitSet2("00000000")), codec.decode(new BitSet2("0000000000")));
		assertEquals(Optional.of(new BitSet2("00000001")), codec.decode(new BitSet2("0000000101")));
		assertEquals(Optional.of(new BitSet2("10000001")), codec.decode(new BitSet2("1000000110")));
		assertEquals(Optional.of(new BitSet2("11110000")), codec.decode(new BitSet2("1111000000")));
		
		assertEquals(Optional.of(new BitSet2("0101010101010101")), codec.decode(new BitSet2("01010101000101010100")));
		
		assertEquals(Optional.absent(), codec.decode(new BitSet2("0000000011")));
	}

}
