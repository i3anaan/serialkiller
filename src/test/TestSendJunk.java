package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lpt.Lpt;

public class TestSendJunk {
	public static void main(String[] args) throws IOException {
		if(args.length==1){
		
			OutputStream os = new FileOutputStream(new File("/telpparport"));
			InputStream is = new FileInputStream(new File("/telpparport"));
			
			switch(args[0]){
			case "spam":
				while (true) {
					for (int i = 0; i < Byte.MAX_VALUE; i++) {
						os.write(i);
						os.flush();
					}
				}
				break;
			case "sendint":
				os.write(123);
				os.flush();
				break;
			case "receiveint":
				String intRead = is.read() +"";
				System.out.println(intRead);
				break;
			default:
				System.out.println("default");
				break;
			}
		}
	}
}
