package link;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lpt.Lpt;

public class LinkLayerSender {

	public static void main(String[] args) {
		BufferedReader conInput = new BufferedReader(new InputStreamReader(
				System.in));
		LinkLayer linkLayer = new LinkLayer(new Lpt());

		while (true) {
			String textInput;
			try {
				textInput = conInput.readLine();
				if (textInput != null) {
					linkLayer.sendByte((byte) Integer.parseInt(textInput));
					textInput = conInput.readLine();
				}
			} catch (IOException e) {
			}

		}
	}
}
