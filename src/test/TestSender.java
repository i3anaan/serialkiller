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
		
		Byte[] test = {0,-1,85,-86,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; 
		//00110110
		//00100110
		//00101110
		//00111110
		
		//00000000
		//11111111
		//01010101
		//10101010
		
		//System.out.println(test.length);
		for(int i=0;i<test.length;i++){
			os.write(test[i]);
			os.flush();
		}
		
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
