package application;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import application.UserInterface.GUI;

/**
 * This main class creates and handles all of the threaded components
 * of the application
 */


public class Main {

	
	public Main(ApplicationLayer apl){
		
		//ApplicationLayer apl = new ApplicationLayer();
		GUI gui = new GUI(apl);
		apl.addObserver(gui);
	}
//	
//	public static void main(final String[] args) {
//		try {
//			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//				if ("Nimbus".equals(info.getName())) {
//					UIManager.setLookAndFeel(info.getClassName());
//					break;
//				}
//			}
//		} catch (Exception e) {
//			// If Nimbus is not available, fall back to cross-platform
//			try {
//				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//			} catch (Exception ex) {
//
//			}
//		}
//		Main main = new Main();
//		
//	}
	
}
