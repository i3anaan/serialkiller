package test.unit;

import java.nio.charset.Charset;

import network.Payload;

import org.junit.Test;
import static org.junit.Assert.*;

import application.message.*;

import com.google.common.base.Charsets;

public class AppLayerMessageTest {
	private static final byte one = 1;
	private static final Charset UTF = Charsets.UTF_8;

	@Test
	public void testPayloadEquals() {
		Payload a = new Payload("Hello".getBytes(UTF), one);
		Payload b = new Payload("Hello".getBytes(UTF), one);
		assertEquals(a, b);
	}
	
	@Test
	public void testChatMessage() {
		ChatMessage a = new ChatMessage(new Payload("CHenk\000hoi".getBytes(UTF), one));
		ChatMessage b = new ChatMessage(one, "Henk", "hoi");
		
		assertTrue(a.isInbound());
		assertTrue(b.isOutbound());
		
		assertPayloadEquals(a.getPayload(), b.getPayload());
	}
	
	@Test
	public void testFileMessage() {
		FileMessage a = new FileMessage(new Payload("F\000\000\000\001e".getBytes(UTF), one));
		FileMessage b = new FileMessage(one, (byte) 'F', 1, "e");
		
		assertTrue(a.isInbound());
		assertTrue(b.isOutbound());
		
		assertPayloadEquals(a.getPayload(), b.getPayload());
	}
	
	@Test
	public void testTransferMessage() {
		Payload p = new Payload("S\000\000\000\004test.txt\000TEST".getBytes(UTF), one);
		
		FileTransferMessage a = new FileTransferMessage(p);
		FileTransferMessage b = new FileTransferMessage(one, 4, "test.txt", "TEST".getBytes(UTF));
		
		assertTrue(a.isInbound());
		assertTrue(b.isOutbound());
		
		assertPayloadEquals(a.getPayload(), b.getPayload());
	}
	
	@Test
	public void TestIdentMessage() {
		assertTrue(new IdentificationMessage((byte) 10).getPayload().data[0] == 'I');
	}
	
	private static void assertPayloadEquals(Payload a, Payload b) {
		assertArrayEquals(a.data, b.data);
		assertEquals(a.address, b.address);
	}

}
