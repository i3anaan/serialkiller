package test.unit;

import static org.junit.Assert.*;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.bitexchanger.HighSpeedBitExchanger;
import link.angelmaker.bitexchanger.NonInvertingBitExchanger;
import link.angelmaker.bitexchanger.SimpleBitExchanger;
import link.angelmaker.manager.AMManager;
import link.angelmaker.manager.MemoryRetransmittingManager;
import link.angelmaker.manager.NullAMManager;

import org.junit.Test;

import phys.diag.NullPhysicalLayer;
import phys.diag.VirtualPhysicalLayer;
import util.BitSet2;

public class BitExchangerTest {
	
	public static final String PARSED = "1110001101000111111111100001000001010110001000100001010010010100110111011000001110010000101011011010010101000010011110000001001011011001001110111101011111010101001010100010110100100100111011101100001111110101110101100111101111110111000001101100111000110000111110010001000010110010110101100100011010010110110101110001001100001111100000111000000001001010111011010100010100001000000000001100011111010111010011011110001110100001010001110100011101110011000101101010001011010000110000100010110010110100011110101001011000011100001010101011111010111000000111000100101101111101011010001000000100110011100010101011010100110000001111100011010100001001100100110110110100001101110101011101111000110100010000111011100100000000111100111001111001011011011010111010000101110011100011010100011111001011001010011101000010111100110111000111101000001110001011101110010100101111000001010100111010011010110010010001010010110110001110011110101111011100111110100001011001001100000111011000111101001110010111111010110010001110";
	public static final String BITERROR = "0011100111000000101110001000110101101001010000011010100001100111101110110011000001110110001000011110000010100110000110000111100100110110001110111111111011001101011111110000010010010001110000101110110010000110111100000110101000010000011001010100101000000000010101101011110101010111101101100010101011111000100111001001111110110000100101110011110011010000101010010011000101101010010111100010111101110000110101111010110110100000110100100011110110010011010001110000111111101100001111011000010001001110100111001101111010010101000011111001001010110100100000101100101110100010011100000101111110010101110110111111001011010010010010010010011110010110010100100011000010001111010011111110011100111111010010100001011100111000000101010101111010101000100010001110100011010011010111010001001101101100011110001111000001000010010011010101001101110010000100111111101100111010101111100111100000000110111011111010110010000001001111111000011100111110010111111101111100100001111111001010111111110110011011010000001110100111";

	
	@Test
	public void testSimpleBitExchanger(){
		SimpleBitExchanger e = new SimpleBitExchanger();
		e.givePhysicalLayer(new NullPhysicalLayer());
		e.giveAMManager(new NullAMManager());
		e.enable();
		
		for(int i=0;i<4;i++){
			for(int b=0;b<2;b++){
				boolean[] arr = e.extractBitFromInput((byte)i,e.adaptBitToPrevious(((byte)i),b==1));
				assertEquals(b==1,arr[0]);
			}
		}
	}
	
	@Test
	public void testNonInvertingBitExchanger(){
		NonInvertingBitExchanger e = new NonInvertingBitExchanger();
		e.givePhysicalLayer(new NullPhysicalLayer());
		e.giveAMManager(new NullAMManager());
		e.enable();
		
		for(int i=0;i<4;i++){
			for(int b=0;b<2;b++){
				boolean[] arr = e.extractBitFromInput((byte)i,e.adaptBitToPrevious(((byte)i),b==1));
				assertEquals(b==1,arr[0]);
			}
		}
	}
	

	public void testHighSpeedBitExchangerInAction() {
		try{
		VirtualPhysicalLayer vplA, vplB;
		int bitAmount = 255;
		vplA = new VirtualPhysicalLayer();
		vplB = new VirtualPhysicalLayer();

		vplA.connect(vplB);
		vplB.connect(vplA);

		AMManager managerA = new NullAMManager();
		AMManager managerB = new NullAMManager();

		BitExchanger beA = new HighSpeedBitExchanger();
		beA.givePhysicalLayer(vplA);
		beA.giveAMManager(managerA);

		BitExchanger beB = new HighSpeedBitExchanger();
		beB.givePhysicalLayer(vplB);
		beB.giveAMManager(managerB);

		managerA.setExchanger(beA);
		managerB.setExchanger(beB);

		managerA.enable();
		managerB.enable();

		//System.out.println("Building BitSet2 to send");
		BitSet2 send = new BitSet2(PARSED).get(0,bitAmount);
		assertEquals(bitAmount,send.length());
		//System.out.println("Done building BitSet2 to send");
		//System.out.println("Handing Bits to BitExchanger");
		beA.sendBits((BitSet2)send.clone());
		beB.sendBits((BitSet2)send.clone());

		//System.out.println("Enabling BitExchangers");
		beA.enable();
		beB.enable();

		//System.out.println("Start reading A.");
		BitSet2 receivedA = new BitSet2();
		while(receivedA.length()<bitAmount){
			BitSet2 read = beA.readBits();
			//System.out.println("A received bits: "+read);
			receivedA = BitSet2.concatenate(receivedA, read);

		}
		receivedA = receivedA.get(0,bitAmount);

		BitSet2 receivedB = new BitSet2();
		while(receivedB.length()<bitAmount){
			receivedB = BitSet2.concatenate(receivedB, beB.readBits());
			//System.out.println("B received bits");
		}
		receivedB = receivedB.get(0,bitAmount);

		System.out.println("Original:\t"+send);
		System.out.println("A:\t\t"+receivedA);
		System.out.println("B:\t\t"+receivedB);
		assertEquals(receivedA,receivedB);
		assertEquals(send,receivedA);
		}catch(IncompatibleModulesException e){
			fail();
		}
	}

}
