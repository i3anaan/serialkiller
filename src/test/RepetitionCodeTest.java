package test;

import static org.junit.Assert.*;

import java.util.Arrays;

import link.RepetitionCode;

import org.junit.Test;

public class RepetitionCodeTest {

	@Test
	public void testRepeatBytes() {
		
		assertEquals(3,RepetitionCode.REPETITION_AMOUNT);
		byte[] ba0 = new byte[]{1,1,1};
		assertEquals(RepetitionCode.REPETITION_AMOUNT,ba0.length);
		assertArrayEquals(ba0,RepetitionCode.repeatBytes(new byte[]{1}));
		byte[] ba1 = new byte[]{1,1,1,2,2,2,3,3,3};
		assertArrayEquals(ba1,RepetitionCode.repeatBytes(new byte[]{1,2,3}));
		byte[] ba2 = new byte[]{1,1,1,4,4,4,-3,-3,-3};
		assertArrayEquals(ba2,RepetitionCode.repeatBytes(new byte[]{1,4,-3}));
	}
	
	@Test
	public void testCheckCorrect() {
		
		assertEquals(3,RepetitionCode.REPETITION_AMOUNT);
		byte[] ba0 = new byte[]{1,1,1};
		assertEquals(RepetitionCode.REPETITION_AMOUNT,ba0.length);
		assertArrayEquals(new byte[]{1},RepetitionCode.extractData(ba0));
		byte[] ba1 = new byte[]{1,1,1,2,2,2,3,3,3};
		assertArrayEquals(new byte[]{1,2,3},RepetitionCode.extractData(ba1));
		byte[] ba2 = new byte[]{1,1,1,4,4,4,-3,-3,-3};
		assertArrayEquals(new byte[]{1,4,-3},RepetitionCode.extractData(ba2));
		byte[] ba3 = new byte[]{1,1,4,4,4,4,4,-3,-3};
		assertArrayEquals(new byte[]{1,4,-3},RepetitionCode.extractData(ba3));
		byte[] ba4 = new byte[]{1,1,4,0,5,1,4,-3,-3};
		assert(RepetitionCode.extractData(ba4)!=null);
		byte[] ba5 = new byte[]{1,1,5,1,4,-3,-3};
		assertArrayEquals(null,RepetitionCode.extractData(ba5));
		byte[] ba6 = new byte[]{1,2,3,4,5,6,7,8,9,6,3,65,2,5,8,2,5};
		assertArrayEquals(ba6,RepetitionCode.extractData(RepetitionCode.repeatBytes(ba6)));
	}
	
	
	public void testCheckCorrectDifferentAmount(){
		byte[] ba0 = new byte[]{1,1,1,1,1,1,1,1,5,5,5,5,5,5,5,5}; //8
		assertEquals(RepetitionCode.REPETITION_AMOUNT*2,ba0.length);
		assertArrayEquals(new byte[]{1,5},RepetitionCode.extractData(ba0));
		byte[] ba1 = new byte[]{1,1,1,1,3,3,3,3,5,5,5,5,5,5,5,5}; //8
		assertArrayEquals(null,RepetitionCode.extractData(ba1));
		
		assertArrayEquals(ba0,RepetitionCode.repeatBytes(new byte[]{1,5}));
	}

}
