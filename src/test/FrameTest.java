package test;

import static org.junit.Assert.*;
import link.Frame;

import org.junit.Test;

import util.Bytes;

public class FrameTest {

	@Test
	public void testAdditionZero() {
		Frame frame = new Frame();
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
	}
	
	@Test
	public void testAdditionOne() {
		Frame frame = new Frame();
		assertEquals(0,frame.getByte());
		frame.add((byte)1);
		assertEquals(-128,frame.getByte());
		frame.add((byte)1);
		assertEquals(-64,frame.getByte());
		frame.add((byte)1);
		assertEquals(-32,frame.getByte());
		frame.add((byte)1);
		assertEquals(-16,frame.getByte());
		frame.add((byte)1);
		assertEquals(-8,frame.getByte());
		frame.add((byte)1);
		assertEquals(-4,frame.getByte());
		frame.add((byte)1);
		assertEquals(-2,frame.getByte());
		frame.add((byte)1);
		assertEquals(-1,frame.getByte());
		frame.add((byte)1);
		assertEquals(-1,frame.getByte());
	}
	
	@Test
	public void testAdditionMixed() {
		Frame frame = new Frame((byte)-1,1);
		System.out.println(Bytes.format((byte)-1));
		assertEquals(-1,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(127,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(63,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(31,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(15,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(7,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(3,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(1,frame.getByte());
		assertEquals(1,frame.nextBit());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		assertEquals(0,frame.nextBit());
		frame.add((byte)0);
		assertEquals(0,frame.getByte());
		assertEquals(0,frame.nextBit());
	}

}
