package link;

public class RepetitionCode{// TODO iets anders extenden?

	public static final int REPETITION_AMOUNT = 3;
	protected LinkLayer downll;

	public RepetitionCode(LinkLayer ll) {
		this.downll = ll;
	}

	/**
	 * Encodes the frame using repetition. Each byte is sent REPETITION_AMOUNT
	 * times, then the next byte is sent REPETITION_AMOUNT times, etc.
	 */
	@Override
	public void sendFrame(byte[] frame) {
		downll.sendFrame(repeatBytes(frame));
	}

	public static byte[] repeatBytes(byte[] frame){
		byte[] encodedFrame = new byte[frame.length * REPETITION_AMOUNT];
		for (int l = 0; l < frame.length; l++) {
			for (int i = 0; i < REPETITION_AMOUNT; i++) {
				encodedFrame[l * REPETITION_AMOUNT + i] = frame[l];
			}
		}
		return encodedFrame;
	}


	@Override
	public byte[] readFrame() {
		while (true) {
			byte[] receivedFrame = downll.readFrame();
			byte[] receivedData = extractData(receivedFrame);
			if (receivedData!=null) {
				return receivedData;
			}
		}
	}

	/**
	 * 
	 * @param receivedFrame
	 * @return	A byte[] if it can extract one, else returns 0;
	 */
	public static byte[] extractData(byte[] receivedFrame) {
		boolean isStillCorrect = true;
		if (receivedFrame.length % REPETITION_AMOUNT == 0) {
			byte[] receivedData = new byte[receivedFrame.length
					/ REPETITION_AMOUNT];
			for (int l = 0; l < receivedFrame.length && isStillCorrect; l = l
					+ REPETITION_AMOUNT) {
				int[] occurence = new int[8];
				for (int i = 0; i < REPETITION_AMOUNT && isStillCorrect; i++) {
					System.out.println("l = "+l+"   i = "+i);
					byte b = receivedFrame[l + i];
					for (int a = 0; a < 8 && isStillCorrect; a++) {
						occurence[a] = occurence[a] + (((b >> a) & 1) * 2) - 1;
					}
				}
				byte realByte = 0;
				for (int b = 0; b < 8 && isStillCorrect; b++) {
					if (occurence[b] > 0) {
						realByte = (byte) (realByte | (1 << b));
					} else if (occurence[b] < 0) {
						// Set bit b to 0 = do nothing;
					} else {
						isStillCorrect = false;
						//System.out.println("No majority on bit!");
					}
				}
				receivedData[l / REPETITION_AMOUNT] = realByte;
			}
			if(isStillCorrect){
				return receivedData;
			}
		} else {
			//System.out.println("Wrong amount of bytes!  "+receivedFrame.length%3);
			// Received wrong amount of bytes.
			// Not a multitude of REPETITION_AMOUNT.
		}
		return null;
	}

}