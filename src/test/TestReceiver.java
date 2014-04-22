package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TestReceiver {

	public static void main(String[] args) throws FileNotFoundException {
		//InputStream is = null;
		InputStream is = new FileInputStream(new File("/telpparport"));
		printByte(23);
		while (true) {
			int intRead = 0;
			int oldRead = 0;
			try {
				intRead = is.read() ;
				if(intRead!=oldRead){
					System.out.print(intRead+"   =   ");
					printByte(intRead);
					oldRead = intRead;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
		
	}
	
	private static void printByte(int byteInt){
		for(int i=7;i>=0;i--){
			System.out.print((byteInt>>>i) & 1);
		}
		System.out.print("\n");
	}
}
