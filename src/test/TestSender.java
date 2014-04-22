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

	public static void main(String[] args) throws IOException{
		OutputStream os = new FileOutputStream(new File("/telpparport"));
		BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
		
		Byte[] test = {54};
		System.out.println(test.length);
		os.write(test[0]);
		os.flush();
		
		/*try {
			is.readLine();
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			try {
				is.readLine();
				os.write(Integer.parseInt(is.readLine()));
				os.write
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/
	}
	
}
