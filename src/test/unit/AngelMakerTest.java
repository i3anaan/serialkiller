package test.unit;

import static org.junit.Assert.*;

import java.util.Arrays;

import link.angelmaker.AngelMaker;
import link.angelmaker.bitexchanger.DummyBitExchanger;

import org.junit.Test;

import phys.diag.VirtualPhysicalLayer;

public class AngelMakerTest {

	@Test
	public void testWithDummyBitExchanger() {
		//TODO cycle through all possible combinations?
		AngelMaker am = new AngelMaker(new VirtualPhysicalLayer(),null,null,new DummyBitExchanger());
		
		for(int i : new int[]{1,2,3,5,10,50}){
			for(byte b = Byte.MIN_VALUE;b<Byte.MAX_VALUE;b++){
				byte byteUsed = b;
				byte[] arr = new byte[i];
				for(int a=0;a<i;a++){
					arr[a] = byteUsed;
					byteUsed++;
				}
				am.sendFrame(arr);
				//System.out.println(Arrays.toString(arr));
				byte[] received = am.readFrame();
				//TODO Let AngelMaker return same frame, or let layer on top worry about that?
				//System.out.println(Arrays.toString(received));
				assertArrayEquals(arr,received);
			}
		}
		
		
	}

}
