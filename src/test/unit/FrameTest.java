package test.unit;

import static org.junit.Assert.*;

import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;

import link.jack.FlaggedFrame;
import link.jack.Frame;
import link.jack.HammingUnit;
import link.jack.JackTheRipper;
import link.jack.PureUnit;
import link.jack.Unit;

import org.junit.Test;

import util.BitSet2;
import util.Bytes;

public class FrameTest {

	
	@Test
	public void testConstructors(){
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
		Frame f1 = new Frame(bs1);
		Frame f2 = new Frame(bs2);
		
		for(int i=0;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			assert(f0.getUnit(i).getByte()==(byte)-1);
		}
		assert(f2.getUnit(0).getByte()==(byte)-1);
		for(int i=2;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			assert(f2.getUnit(i).isFiller());
		}		
	}
	
	@Test
	public void testFlaggedFrameConstructorHammingUnit(){
		BitSet2 bs0 = new BitSet2(new boolean[]{true,true,false,true});
		BitSet2 bs1 = new BitSet2(new boolean[]{false,true,false,false});
		BitSet2 bs2 = new BitSet2(new boolean[]{true,true,true,true});
		
		ArrayBlockingQueue<Unit> arr0 = new ArrayBlockingQueue<Unit>(Frame.PAYLOAD_UNIT_COUNT);
		ArrayBlockingQueue<Unit> arr1 = new ArrayBlockingQueue<Unit>(Frame.PAYLOAD_UNIT_COUNT);
		ArrayBlockingQueue<Unit> arr2 = new ArrayBlockingQueue<Unit>(Frame.PAYLOAD_UNIT_COUNT);
		ArrayBlockingQueue<Unit> arr3 = new ArrayBlockingQueue<Unit>(Frame.PAYLOAD_UNIT_COUNT);
		arr2.add(new HammingUnit(bs0,JackTheRipper.HC));
		arr3.add(new HammingUnit(bs0,JackTheRipper.HC));
		arr3.add(new HammingUnit(bs0,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs0,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs1,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs2,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs0,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs1,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs2,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs0,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs1,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs2,JackTheRipper.HC));
		arr0.add(new HammingUnit(bs2,JackTheRipper.HC));
		arr1.add(new HammingUnit(bs2,JackTheRipper.HC));
		for(int i=1;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			arr1.add(new HammingUnit(HammingUnit.FLAG_FILLER_DATA,JackTheRipper.HC));
		}
		
		FlaggedFrame ff0 = new FlaggedFrame(arr0);
		FlaggedFrame ff2 = new FlaggedFrame(arr2);
		FlaggedFrame ff3 = new FlaggedFrame(arr3);
		
		assert(ff2.getPayload().getUnit(0).getByte()==new HammingUnit(bs0,JackTheRipper.HC).getByte());
		for(int i=0;i<10;i=i+3){
			assert(ff0.getPayload().getUnit(i).getByte() == new HammingUnit(bs0,JackTheRipper.HC).getByte());
			assert(ff0.getPayload().getUnit(i+1).getByte() == new HammingUnit(bs1,JackTheRipper.HC).getByte());
			assert(ff0.getPayload().getUnit(i+2).getByte() == new HammingUnit(bs2,JackTheRipper.HC).getByte());
			assert(ff0.getPayload().getUnit(i).getByte() != (byte)-1);
			assert(ff0.getPayload().getUnit(i+1).getByte() != (byte)-1);
			assert(ff0.getPayload().getUnit(i+2).getByte() != (byte)-1);
		}
		for(int i=1;i<Frame.PAYLOAD_UNIT_COUNT;i++){
			assert(ff0.getPayload().getUnit(i).isFiller());
		}
		
		assertNotEquals(ff2.getDataBitSet(), ff3.getDataBitSet());
		FlaggedFrame ff4 = new FlaggedFrame(ff3.getDataBitSet());
		assertEquals(ff3.getDataBitSet(),ff4.getDataBitSet());
		FlaggedFrame ff5 = new FlaggedFrame(ff2.getDataBitSet());
		assertEquals(ff2.getDataBitSet(),ff5.getDataBitSet());	
	}
}
