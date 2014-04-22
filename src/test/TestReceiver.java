package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TestReceiver {

	public static void main(String[] args) throws FileNotFoundException {
		InputStream is = new FileInputStream(new File("/telpparport"));

		while (true) {
			String intRead;
			String oldRead = "";
			try {
				intRead = is.read() + "";
				if(!intRead.equals("0") && !intRead.equals(oldRead)){
					System.out.println(intRead);
					oldRead = intRead;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
