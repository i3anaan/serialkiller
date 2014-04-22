package test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class TestSender {

	public static void main(String[] args) throws FileNotFoundException{
		OutputStream os = new FileOutputStream(new File("/telpparport"));
		BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
		
		while(true){
			try {
				is.readLine();
				os.write(Integer.parseInt(is.readLine()));
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
}
