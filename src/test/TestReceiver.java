package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TestReceiver {

	InputStream is = new FileInputStream(new File("/telpparport"));
	
	while(true){
		String intRead = is.read() +"";
		System.out.println(intRead);
		break;
	}
}
