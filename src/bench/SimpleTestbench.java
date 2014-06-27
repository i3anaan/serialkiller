package bench;

import link.*;
import link.angelmaker.AngelMaker;
import phys.diag.BitErrorPhysicalLayer;
import phys.diag.DelayPhysicalLayer;
import phys.diag.VirtualPhysicalLayer;

/**
 * A simple test bench application for testing layers of the SerialKiller stack.
 */
public class SimpleTestbench {
	public static final int BYTES_PER_CHAR = 8;
	private class SeqThread extends Thread {
		private FrameLinkLayer down;

		public SeqThread(FrameLinkLayer down) {
			this.down = down;
		}

		public void run() {
			while(true){
				for (byte i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
					//TODO why did this need to be '<=' instead of just '<' ?
					down.sendFrame(new byte[]{(byte)(i)});
				}
			}
		}
	}
	private class CheckThread extends Thread {
		private PacketFrameLinkLayer down;

		public CheckThread(PacketFrameLinkLayer down) {
			this.down = down;
		}

		public void run() {
			while(true){
				int correct = 0;
				byte i = Byte.MIN_VALUE;
				while(true) {
					byte[] bytes = down.readFrame();					
					if(bytes.length>0){
						int j;
						
						for (j = 0; j < bytes.length; j++) {
							if(i==Byte.MIN_VALUE){
								System.out.print("\n");
							}
							if (bytes[j] == i) {
								//System.out.printf("[OK] expected %d got %d\n", i, bytes[j]);
								correct++;
							} else {
								//System.out.printf("[!!] expected %d got %d\n", i, bytes[j]);
							}
							if((i+256)%BYTES_PER_CHAR==BYTES_PER_CHAR-1){
								if(correct==BYTES_PER_CHAR){
									System.out.print('.');
								}else{
									System.out.print('X');
									//System.out.println("Correct = "+correct);
								}
								correct = 0;
							}
							i++;
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		new SimpleTestbench().run();
	}

	public void run() {
		System.out.println("SimpleTestbench");
		System.out.println("===============");
		System.out.println();
		
		VirtualPhysicalLayer vpla, vplb;
		
		vpla = new VirtualPhysicalLayer();
		vplb = new VirtualPhysicalLayer();
		
		vpla.connect(vplb);
		vplb.connect(vpla);

		PacketFrameLinkLayer a = new AngelMaker(new DelayPhysicalLayer(vpla),null,null,null);
		PacketFrameLinkLayer b = new AngelMaker(new DelayPhysicalLayer(vplb),null,null,null);
		
		System.out.println("STACK A: " + a);
		System.out.println("STACK B: " + a);
		System.out.println();
				
		Thread et = new CheckThread(a);
		Thread st = new SeqThread(b);
		
		et.start();
		st.start();
	}
}
