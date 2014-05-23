package application;

import java.io.UnsupportedEncodingException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import application.UserInterface.GUI;

/**
 * Test Layer to see if the ApplicationLayer is capable of handling chat messages in a 
 * correct manner
 * @author msbruning
 *
 */
public class ReceivingApplicationLayer {

	public ReceivingApplicationLayer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, fall back to cross-platform
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ex) {

			}
		}

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		

		boolean loop_continue = true;
		
		
		ApplicationLayer al = new ApplicationLayer(null);
		GUI gui = new GUI(al);
		al.addObserver(gui);
		boolean doOnce = true;
//		while(loop_continue){
//
//			
//			//byte[] data = getChatMsg();
//			if(doOnce){
//			byte[] data = null;
//			try {
//				data = getFileMsg();
//			} catch (UnsupportedEncodingException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			try {
//				al.readPayload(data);
//				doOnce = false;
//			} catch (CommandNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			}
//
//		}
		
        
		    }
		});
	}
	
	// Get a chat msg test
	public static byte[] getChatMsg() throws UnsupportedEncodingException{
		String nickname = "CHenk";
		String message = "Je bent een held";
		byte nullbyte = (byte)0;

		byte[] nick = nickname.getBytes("UTF-8");
		byte[] msg = message.getBytes("UTF-8");

		byte[] data = new byte[nick.length + 1 + msg.length];
		System.arraycopy(nick, 0, data, 0, nick.length);
		data[nick.length+1] = nullbyte;
		System.arraycopy(msg, 0, data, (nick.length+1), msg.length);

		return data;
	}
	
	public static byte[] getFileMsg() throws UnsupportedEncodingException{
		
		String filesize = "F1111";
		String name = "JonSkeetBible.txt";

		byte[] nick = filesize.getBytes("UTF-8");
		byte[] msg = name.getBytes("UTF-8");

		byte[] data = new byte[nick.length + msg.length];
		System.arraycopy(nick, 0, data, 0, nick.length);
		System.arraycopy(msg, 0, data, nick.length, msg.length);

		return data;
	}

}


