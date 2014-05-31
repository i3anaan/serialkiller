package test.unit;

import static org.junit.Assert.*;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.ConsistentDuplexBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;

import org.junit.Test;

import phys.diag.VirtualPhysicalLayer;
import util.BitSet2;

public class BitExchangerTest {

	@Test
	public void testBitExchanger() {
		VirtualPhysicalLayer vpla, vplb;
		
		vpla = new VirtualPhysicalLayer();
		vplb = new VirtualPhysicalLayer();
		
		vpla.connect(vplb);
		vplb.connect(vpla);
		AMManager managerA = new BlockingAMManagerServer();
		AMManager managerB = new BlockingAMManagerServer();
		BitExchanger beA = new ConsistentDuplexBitExchanger(vpla, managerA);
		BitExchanger beB = new ConsistentDuplexBitExchanger(vplb,managerB);
		managerA.setExchanger(beA);
		managerB.setExchanger(beB);
		managerA.enable();
		managerB.enable();
		
		BitSet2 send = new BitSet2();
		for(int bit = 0;bit<300;bit++){
			send.addAtEnd(Math.random()>0.5);
		}
		//System.out.println("Sending: "+send);
		System.out.println(send);
		beA.sendBits(send);
		beB.sendBits(send);
		BitSet2 received = new BitSet2();
		while(received.length()<300){
			received = BitSet2.concatenate(received, beB.readBits());
			if(received.length()>0){

				//System.out.println("Send:\t"+send);
				//System.out.println("Rcved:\t"+received);
			}
		}
		
		//System.out.println("Send:\t"+send);
		//System.out.println("Rcved:\t"+received);
	}

}
