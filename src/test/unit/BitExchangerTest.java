package test.unit;

import static org.junit.Assert.*;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.ConsistentDuplexBitExchanger;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.BlockingAMManagerServer;

import org.junit.Test;

import phys.diag.NullPhysicalLayer;
import phys.diag.VirtualPhysicalLayer;
import util.BitSet2;
import util.Bytes;

public class BitExchangerTest {
	
	@Test
	public void testSimpleBitExchanger(){
		SimpleBitExchanger e = new SimpleBitExchanger(new NullPhysicalLayer(), new BlockingAMManagerServer());
		for(int i=0;i<4;i++){
			for(int b=0;b<2;b++){
				System.out.println("CurrentState:"+Bytes.format((byte)i)+"\tEncoding:"+(b==1?"1":"0")+"\tResult:"+Bytes.format(e.adaptBitToPrevious(((byte)i),b==1)));
				assertTrue((b==1)==e.extractBitFromInput(e.adaptBitToPrevious(((byte)i),b==1)));
			}
		}
	}
	
	@Test
	public void testBitExchanger() {
		VirtualPhysicalLayer vpla, vplb;
		int bitAmount = 1000;
		
		vpla = new VirtualPhysicalLayer();
		vplb = new VirtualPhysicalLayer();
		
		vpla.connect(vplb);
		vplb.connect(vpla);
		AMManager managerA = new BlockingAMManagerServer();
		AMManager managerB = new BlockingAMManagerServer();
		BitExchanger beA = new SimpleBitExchanger(vpla, managerA);
		BitExchanger beB = new SimpleBitExchanger(vplb,managerB);
		managerA.setExchanger(beA);
		managerB.setExchanger(beB);
		managerA.enable();
		managerB.enable();
		
		BitSet2 send = new BitSet2();
		for(int bit = 0;bit<bitAmount;bit++){
			send.addAtEnd(Math.random()>0.5);
		}
		beA.sendBits(send);
		beB.sendBits(send);
		BitSet2 receivedA = new BitSet2();
		while(receivedA.length()<bitAmount){
			receivedA = BitSet2.concatenate(receivedA, beA.readBits());
			if(receivedA.length()>0){
			}
		}
		receivedA = receivedA.get(0,bitAmount);
		BitSet2 receivedB = new BitSet2();
		while(receivedB.length()<bitAmount){
			BitSet2 read = beB.readBits();
			receivedB = BitSet2.concatenate(receivedB, read);
			if(read.length()>0){
			}
		}
		receivedB = receivedB.get(0,bitAmount);
		System.out.println("Original:\t"+send);
		System.out.println("A:\t\t"+receivedA);
		System.out.println("B:\t\t"+receivedB);
		assertEquals(receivedA,receivedB);
		assertEquals(send,receivedA);
	}

}
