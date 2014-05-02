package application;

import java.io.UnsupportedEncodingException;

public class ReceivingApplicationLayer {

	public ReceivingApplicationLayer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		
		boolean loop_continue = true;
		ApplicationLayer al = new ApplicationLayer();
		while(loop_continue){
			
			String nickname = "CHenk";
			String message = "Je bent een klootzak";
			byte nullbyte = (byte)0;
			
			byte[] nick = nickname.getBytes("UTF-8");
			byte[] msg = message.getBytes("UTF-8");
			
			byte[] data = new byte[nick.length + 1 + msg.length];
			System.arraycopy(nick, 0, data, 0, nick.length);
			data[nick.length+1] = nullbyte;
			System.arraycopy(msg, 0, data, (nick.length+1), msg.length);
			System.out.println(new String(data, "UTF-8"));
			
			
			
			try {
				al.readPayload(data);
			} catch (CommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

	}

}
