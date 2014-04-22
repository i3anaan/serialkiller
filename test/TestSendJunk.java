import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TestSendJunk {
	public static void main(String[] args) throws IOException {
		OutputStream os = new FileOutputStream(new File("/telpparport"));
		
		while (true) {
			for (int i = 0; i < Byte.MAX_VALUE; i++) {
				os.write(i);
			}
		}
	}
}
