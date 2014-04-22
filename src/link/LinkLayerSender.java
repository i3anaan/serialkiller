package link;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lpt.Lpt;

public class LinkLayerSender {

	public static void main(String[] args) throws IOException {
		BufferedReader conInput = new BufferedReader(new InputStreamReader(
				System.in));
		SingleDirectionLinkLayer linkLayer = new SingleDirectionLinkLayer(new Lpt());

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
