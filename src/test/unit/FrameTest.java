package test.unit;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import link.jack.FixingFrame;
import link.jack.SimpleFrame;
import link.jack.HammingUnit;
import link.jack.JackTheRipper;
import link.jack.Unit;

import org.junit.Test;

import util.BitSet2;

public class FrameTest {
	
	@Test
	public void testConstructors(){
		ArrayBlockingQueue<Unit> arr = new ArrayBlockingQueue<Unit>(1024);
		arr.add(JackTheRipper.UNIT_IN_USE.getRandomUnit());
		SimpleFrame f = new SimpleFrame(arr);
		for(int i=0;i<SimpleFrame.FRAME_UNIT_COUNT;i++){
			assertTrue(f.getUnits()[i]!=null);
		}
		
		arr = new ArrayBlockingQueue<Unit>(1024);
		ArrayList<Unit> arr2 = new ArrayList<Unit>();
		for(int i=0;i<SimpleFrame.FRAME_UNIT_COUNT;i++){
			Unit u = JackTheRipper.UNIT_IN_USE.getRandomUnit();
			arr2.add(u);
			arr.add(u);
		}
		f = new SimpleFrame(arr);
		for(int i=0;i<SimpleFrame.FRAME_UNIT_COUNT;i++){
			assertTrue(f.getUnits()[i]==arr2.get(i));
			assertTrue(f.getUnits()[i]!=null);
		}
		
		
		BitSet2 bs = new BitSet2();
		for(Unit u : arr2){
			bs = BitSet2.concatenate(bs, u.serializeToBitSet());
		}
		SimpleFrame f2 = new SimpleFrame(bs);
		for(int i = 0;i<SimpleFrame.FRAME_UNIT_COUNT;i++){
			assertTrue(f2.getUnits()[i]!=null);
			assertEquals(arr2.get(i),f2.getUnits()[i]);
		}
		
		
		bs = new BitSet2();
		bs= BitSet2.concatenate(bs, HammingUnit.getDummy().serializeToBitSet());
		f2 = new SimpleFrame(bs);
		for(int i = 0;i<SimpleFrame.FRAME_UNIT_COUNT;i++){
			assertTrue(f2.getUnits()[i]!=null);
		}
	}
	
	
	@Test
	public void testFixingFrame(){
		BitSet2 data = BitSet2.concatenate(FixingFrame.FLAG_END_OF_FRAME,BitSet2.concatenate(new BitSet2(new boolean[]{true,false,true,false,false,true,true}),FixingFrame.FLAG_END_OF_FRAME));
		assertTrue(data.contains(FixingFrame.FLAG_END_OF_FRAME));
		BitSet2 stuffedData = (BitSet2) data.clone();
		assertTrue(data.equals(stuffedData));
		stuffedData = FixingFrame.addBitStuffing(stuffedData);
		assertFalse(stuffedData.contains(FixingFrame.FLAG_END_OF_FRAME));
		stuffedData = FixingFrame.removeBitStuffing(stuffedData);
		assertTrue(stuffedData.equals(data));
		stuffedData = FixingFrame.addBitStuffing(stuffedData);
		stuffedData = FixingFrame.addEndFlag(stuffedData);
		assertTrue(stuffedData.contains(FixingFrame.FLAG_END_OF_FRAME));
	}
}
