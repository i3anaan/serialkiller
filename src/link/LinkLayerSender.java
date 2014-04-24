package link;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import phys.LptHardwareLayer;

public class LinkLayerSender {

	public static void main(String[] args) throws IOException {
		BufferedReader conInput = new BufferedReader(new InputStreamReader(System.in));
        LinkLayer linkLayer = new AckLinkLayer(new LptHardwareLayer());

		String textInput;

		textInput = conInput.readLine();
		while (true) {
			if (textInput != null) {
				linkLayer.sendByte((byte) Integer.parseInt(textInput));
				textInput = conInput.readLine();
			}
		}
	}
}
