package test.unit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Charsets;

import application.message.*;

public class ApplicationMessageTest {

	private final static String label ="<html>4evr \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Connexx0rred with tha bestest proto implementation \u2605\u2605\u2605 SerialKiller \u2605\u2605\u2605 <font color=#ff0000>Brought to you by Squeamish, i3anaan, TheMSB, jjkester</font> \u00AF\\_(\u30C4)_/\u00AF - Regards to all our friends: Jason, Jack, Patrick, Ghostface, Jigsaw, Hannibal, John and Sweeney \u0F3C \u1564\uFEFF\u25D5\u25E1\u25D5\uFEFF \u0F3D\uFEFF\u1564\uFEFF <font color=#009900>Smoke weed every day #420 \u0299\u029F\u1D00\u1D22\u1D07 \u026A\u1D1B</font> --- Send warez 2 <a href='https://sk.twnc.org/'>sk.twnc.org</a>, complaints to /dev/null --- The more the merrier: serial killer = best killer --- Word of the day: hacksaw \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Thanks for taking the time to receive this message, 4evr out.";
	private byte host = (byte)0;
	private byte[] fileOfferArray = "O\000\000\000\001TestFile.txt".getBytes(Charsets.UTF_8);
	private byte[] fileAcceptArray = "A\000\000\000\001TestFile.txt".getBytes(Charsets.UTF_8);
	private byte[] fileTransferArray = "S\000\000\000\001TestFile.txt\000TEST".getBytes(Charsets.UTF_8);
	private byte[] chatMessageArray = "CHenk\000hoi".getBytes(Charsets.UTF_8);
	private byte[] whoisArray = "W".getBytes(Charsets.UTF_8);
	private byte[] IdentificationResponseArray = ("I" + label).getBytes(Charsets.UTF_8);
	
	
	@Test
	public void testFileOffer(){
		String path = "herp/derp/TestFile.txt";
		
		FileOfferMessage fom1 = new FileOfferMessage(host, path);
		FileOfferMessage fom2 = new FileOfferMessage(host, fileOfferArray);
		
		//test outgoing
		assertEquals("TestFile.txt", fom1.getFileName());
		assertEquals(host, fom1.getAddress());
		//test incoming
		assertEquals(fileOfferArray, fom2.getPayload());
		assertEquals(host, fom2.getAddress());
		assertEquals("TestFile.txt", fom2.getFileName());
		assertEquals(1, fom2.getFileSize());
	}
	
	@Test
	public void testFileAccept(){
		FileAcceptMessage fam = new FileAcceptMessage(host, fileOfferArray);
		
		assertArrayEquals(fileAcceptArray, fam.getPayload());
	}
	
	@Test
	public void testFileTransfer(){
		
		FileTransferMessage ftm1 = new FileTransferMessage(host, fileTransferArray);
		FileTransferMessage ftm2 = new FileTransferMessage(host, fileOfferArray, fileTransferArray);
		
		assertEquals("TestFile.txt", ftm1.getFileName());
		assertArrayEquals("TEST".getBytes(Charsets.UTF_8), ftm1.getFileBytes());
		
		assertEquals(ftm1.getFileName(), ftm2.getFileName());
		assertEquals(ftm1.getFileSize(), ftm2.getFileSize());
		assertArrayEquals(ftm1.getFileBytes(), ftm2.getFileBytes());
	}
	
	@Test
	public void testChatMessage(){
		ChatMessage cm = new ChatMessage(host, chatMessageArray);
		
		assertEquals(host, cm.getAddress());
		assertEquals("Henk", cm.getNickname());
		assertEquals("hoi", cm.getMessage());
		assertArrayEquals(chatMessageArray, cm.getPayload());
	}
	
	@Test
	public void testIdentificationRequestMessage(){
		IdentificationRequestMessage irm = new IdentificationRequestMessage(host);
		
		assertEquals(host, irm.getAddress());
		assertArrayEquals(whoisArray, irm.getPayload());
	}
	
	@Test
	public void testIdentificationResponseMessage(){
		IdentificationResponseMessage irm1 = new IdentificationResponseMessage(host, null, label.getBytes(Charsets.UTF_8));
		
		assertArrayEquals(IdentificationResponseArray, irm1.getPayload());
	}
	
	
}
