package link;

public class Splitter extends LinkLayer{

	//Ontvangt data
	//Split dit in frames
	//Stuurt frames 1 voor 1
	//Herhaalt frame als fout gaat.
	//Levert pas als data volledig is ontvangen.
	
	//echange frame
	////Dit frame bevat al flags als end-of-frame
	//lees frame + extra flags
	////Check of extra flags goed zijn.
	////Resend frame als nodig is.
	
	
	
	@Override
	public void sendFrame(byte[] frame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] readFrame() {
		// TODO Auto-generated method stub
		return null;
	}
}
