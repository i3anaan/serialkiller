package test.unit;

import static org.junit.Assert.*;
import link.jack.HammingUnit;
import link.jack.PureUnit;

import org.junit.Test;

public class UnitTest {

	PureUnit pu = PureUnit.getDummy();
	HammingUnit hu = HammingUnit.getDummy();
	
	@Test
	public void testFlags() {
		
		assertTrue (hu.getFlag(HammingUnit.FLAG_FILLER_DATA).isSpecial());
		assertTrue (hu.getFlag(HammingUnit.FLAG_FILLER_DATA).isFiller());
		assertTrue (hu.getFlag(HammingUnit.FLAG_END_OF_FRAME).isEndOfFrame());
		assertTrue (!hu.getFlag(HammingUnit.FLAG_END_OF_FRAME).isFiller());
		assertTrue (hu.getFlag(HammingUnit.FLAG_DUMMY).isSpecial());
		assertTrue (!hu.getFlag(HammingUnit.FLAG_DUMMY).isFiller());
		assertTrue (pu.getFlag(PureUnit.FLAG_FILLER_DATA).isSpecial());
		assertTrue (pu.getFlag(PureUnit.FLAG_FILLER_DATA).isFiller());
		assertTrue (pu.getFlag(PureUnit.FLAG_END_OF_FRAME).isEndOfFrame());
		assertTrue (!pu.getFlag(PureUnit.FLAG_END_OF_FRAME).isFiller());
		assertTrue (pu.getFlag(PureUnit.FLAG_DUMMY).isSpecial());
		assertTrue (!pu.getFlag(PureUnit.FLAG_DUMMY).isFiller());
	}
	
	@Test
	public void testTranslate(){
		PureUnit rpu = pu.getRandomUnit();
		HammingUnit rhu = hu.getRandomUnit();
		assertEquals(rpu,pu.constructFromBitSet(rpu.serializeToBitSet()));
		assertEquals(rhu,hu.constructFromBitSet(rhu.serializeToBitSet()));
	}
	
	
	@Test
	public void testMisc(){
		assertTrue(pu.getSerializedBitCount()==9);
		assertTrue(pu.getSerializedBitCount()==pu.serializeToBitSet().length());
		assertTrue(hu.getSerializedBitCount()==8);
		assertTrue(hu.getSerializedBitCount()==hu.serializeToBitSet().length());
	}
}
