package application.message;

import com.google.common.base.Charsets;

import network.Payload;

public class IdentificationMessage extends ApplicationLayerMessage {
	private String ident;
	
	private static final String OUR_ID = ("I<html>4evr \u00B4\u00AF`\u00B7."     +
	"\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Connexx0rred with tha bestest "     +
	"proto implementation \u2605\u2605\u2605 SerialKiller \u2605\u2605\u2605"    +
	"<font color=#ff0000>Brought to you by Squeamish, i3anaan, TheMSB, "         +
	"jjkester</font> \u00AF\\_(\u30C4)_/\u00AF - Regards to all our friends: "   +
	"Jason, Jack, Patrick, Ghostface, Jigsaw, Hannibal, John and Sweeney \u0F3C" +
	"\u1564\uFEFF\u25D5\u25E1\u25D5\uFEFF \u0F3D\uFEFF\u1564\uFEFF <font color=" +
	"#009900>Smoke weed every day #420 \u0299\u029F\u1D00\u1D22\u1D07 \u026A"    +
	"\u1D1B</font> --- Send warez 2 <a href='https://sk.twnc.org/'>sk.twnc.org"  +
	"</a>, complaints to /dev/null --- The more the merrier: serial killer = "   +
	"best killer --- Word of the day: hacksaw \u00B4\u00AF`\u00B7.\u00B8\u00B8." +
	"\u00B7\u00B4\u00AF`\u00B7. Thanks for taking the time to receive this "     +
	"message, 4evr out.");
	
	public IdentificationMessage(byte address) {
		super(address);
		ident = OUR_ID;
		setData(ident.getBytes(Charsets.UTF_8));
	}

	public IdentificationMessage(Payload payload) {
		super(payload);
		ident = new String(payload.data, 1, payload.data.length - 1, Charsets.UTF_8);
	}

	public String getIdentifier() {
		return ident;
	}
}
