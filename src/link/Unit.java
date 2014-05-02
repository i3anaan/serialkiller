package link;

public class Unit {

	public boolean isSpecial;
	public byte b;
	
	public static final int FLAG_FILLER_DATA = 1;
	
	public Unit(byte b){
		this.b = b;
		isSpecial = false;
	}
	
	public Unit(byte b, boolean special){
		this.b = b;
		this.isSpecial = special;
	}
	
	public Unit(int type){
		this.isSpecial = true;
		if(type==FLAG_FILLER_DATA){
			this.b = FLAG_FILLER_DATA;
		}
	}
}
