import javax.swing.JFrame;
import javax.swing.JLabel;

import com.google.common.base.Charsets;

public class SwingTest extends JFrame {
	private final static String label="<html>4evr \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Connexx0rred with tha bestest proto implementation \u2605\u2605\u2605 SerialKiller \u2605\u2605\u2605 <font color=#ff0000>Brought to you by Squeamish, i3anaan, TheMSB, jjkester</font> \u00AF\\_(\u30C4)_/\u00AF - Regards to all our friends: Jason, Jack, Patrick, Ghostface, Jigsaw, Hannibal, John and Sweeney \u0F3C \u1564\uFEFF\u25D5\u25E1\u25D5\uFEFF \u0F3D\uFEFF\u1564\uFEFF <font color=#009900>Smoke weed every day #420 \u0299\u029F\u1D00\u1D22\u1D07 \u026A\u1D1B</font> --- Send warez 2 <a href='https://sk.twnc.org/'>sk.twnc.org</a>, complaints to /dev/null --- The more the merrier: serial killer = best killer --- Word of the day: hacksaw \u00B4\u00AF`\u00B7.\u00B8\u00B8.\u00B7\u00B4\u00AF`\u00B7. Thanks for taking the time to receive this message, 4evr out.";
	public static void main(String[] args) {
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		new SwingTest().run();
		
		System.out.println(SwingTest.label.getBytes(Charsets.UTF_8).length);
	}

	private void run() {
		// TODO Auto-generated method stub
		add(new JLabel(label));
		pack();
		this.setSize(400, 400);
		setVisible(true);
	}

}
