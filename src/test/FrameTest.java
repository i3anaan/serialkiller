package test;

import static org.junit.Assert.*;

import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;

import link.FlaggedFrame;
import link.Frame;
import link.FrameSizeTooSmallException;
import link.Unit;

import org.junit.Test;

import util.BitSet2;
import util.Bytes;

public class FrameTest {

	
	@Test
	public void testConstructors() throws FrameSizeTooSmallException{
		Frame f = new Frame();
		for(int i=0;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			assert(f.getUnit(i).isFiller());
		}
		assertNull(f.getUnit(Frame.PAYLOAD_UNIT_COUNT+1));
		
		BitSet2 bs0 = new BitSet2();
		BitSet2 bs1 = new BitSet2();
		BitSet2 bs2 = new BitSet2();
		
		bs0.set(0,79,true);
		bs1.set(0,80,true);
		bs2.set(0,12,true);
		boolean exception = false;
		
		Frame f0 = new Frame(bs0);
		Frame f1;
		try{
			f1 = new Frame(bs1);
			
			
		}catch(FrameSizeTooSmallException e){
			exception = true;
		}
		assert(exception);
		Frame f2 = new Frame(bs2);
		
		for(int i=0;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			assert(f0.getUnit(i).b==(byte)-1);
		}
		assert(f2.getUnit(0).b==(byte)-1);
		for(int i=2;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			assert(f2.getUnit(i).isFiller());
		}		
	}
	
	@Test
	public void testFlaggedFrameConstructor(){
		ArrayBlockingQueue<Byte> arr0 = new ArrayBlockingQueue<Byte>(10);
		ArrayBlockingQueue<Byte> arr1 = new ArrayBlockingQueue<Byte>(10);
		ArrayBlockingQueue<Byte> arr2 = new ArrayBlockingQueue<Byte>(10);
		ArrayBlockingQueue<Byte> arr3 = new ArrayBlockingQueue<Byte>(10);
		arr2.add((byte)44);
		arr3.add((byte)44);
		arr3.add((byte)44);
		for(int i=0;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			arr0.add((byte)i);
			arr1.add((byte)22);
		}
		
		FlaggedFrame ff0 = new FlaggedFrame(arr0);
		FlaggedFrame ff1 = new FlaggedFrame(arr1);
		FlaggedFrame ff2 = new FlaggedFrame(arr2);
		FlaggedFrame ff3 = new FlaggedFrame(arr3);
		
		assert(ff2.getPayload().getUnit(0).b==(byte)44);
		for(int i=0;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			assert(ff0.getPayload().getUnit(i).b == (byte)i);
			assert(ff1.getPayload().getUnit(i).b==(byte)22);
			if(i!=0){
				assert(ff1.getPayload().getUnit(i).isFiller());
			}
		}
		
		assertNotEquals(ff2.getBitSet(), ff3.getBitSet());
		System.out.println(ff2.getBitSet());
		System.out.println(ff3.getBitSet());
		FlaggedFrame ff4 = new FlaggedFrame(ff3.getBitSet());
		assertEquals(ff3.getBitSet(),ff4.getBitSet());
		FlaggedFrame ff5 = new FlaggedFrame(ff2.getBitSet());
		assertEquals(ff2.getBitSet(),ff5.getBitSet());		
	}
}
