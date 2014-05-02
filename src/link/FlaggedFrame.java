package link;

/**
 * Serves as a header surrounding the normal Frame.
 * @author I3anaan
 *
 */
public class FlaggedFrame extends Frame{
	Frame payload;
	
	public static final int FULL_SIZE = 80;
	
	public FlaggedFrame(){
		this.payload = new Frame();
		this.dataStored = new BitSet(FULL_SIZE);
	}
	
	public FlaggedFrame(Frame payload){
		this.payload = payload;
		
	}

}
