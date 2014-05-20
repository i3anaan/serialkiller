package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.*;

import application.*;
import application.message.ChatMessage;
import application.message.FileOfferMessage;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;


public class GUI extends JFrame implements ActionListener, ItemListener, Observer {

	// private variables
	private		Preferences 		prefs; 					
	private 	ApplicationLayer 	apl;
	private 	ChatPanel 			cp; 					
	private 	UserListPanel 		ulp;
	private		JMenuBar 			menuBar;
	private		JMenu 				menu, submenu;

	public GUI(ApplicationLayer al){
		super("G.A.R.G.L.E.");	
		// Initialize initial variables
		apl 				= al;
		prefs 				= Preferences.userNodeForPackage(getClass());
		cp 					= new ChatPanel(this);
		ulp 				= new UserListPanel(this);
		
		// Layout main application Window
		this.setLayout(new BorderLayout());
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(800, 600));

		buildBarMenu();
		buildChatMenu();

		addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				e.getWindow().dispose();
			}
			public void windowClosed(final WindowEvent e) {
				System.exit(0);
			}
		}
				);

		pack();
		validate();
		setVisible(true);

	}
	
	private void buildBarMenu(){
		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();

		// Create a menu
		JMenu menu = new JMenu("File");

		menu.setHorizontalTextPosition(SwingConstants.CENTER);
		menu.setVerticalTextPosition(SwingConstants.BOTTOM);

		menuBar.add(menu);

		// SendFile Item
		JMenuItem sendFileItem = new JMenuItem("Send File");
		sendFileItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
				// Demonstrate "Open" dialog:
				int rVal = c.showOpenDialog(GUI.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					// TODO call ApplicationLayer to send chosen file
				}
				if (rVal == JFileChooser.CANCEL_OPTION) {

				}
			}
		});
		// Exit item
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				System.exit(0);
			}
		});
		// Options item
		JMenuItem optionItem = new JMenuItem("Options");

		optionItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				buildOptionMenu();
			}
		});
		// RoutingTable item
		JMenuItem routeItem = new JMenuItem("Set RoutingTable");

		routeItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
				// Demonstrate "Open" dialog:
				int rVal = c.showOpenDialog(GUI.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					// TODO call network layer to load new routing table
				}
				if (rVal == JFileChooser.CANCEL_OPTION) {
					// TODO maybe do something, don't think we should
				}
			}
		});
		menu.add(sendFileItem);
		//menu.add(routeItem);
		menu.add(optionItem);
		menu.add(exitItem);

		this.setJMenuBar(menuBar);

	}

	/**
	 * Build an option menu dialog, will modify application
	 * preferences and save them when requested
	 */
	private void buildOptionMenu(){
		TabbedPreferencePane tpp = new TabbedPreferencePane(GUI.this, "Preferences", true);	
		tpp.setVisible(true);
	}

	private void buildChatMenu() {

		this.add(cp, BorderLayout.CENTER);
		this.add(ulp, BorderLayout.EAST);

	}
//	DEPRICIATED
//	/**
//	 * Method to be called for saving files when a file transfer
//	 * request is received
//	 * @return the path to save the file to
//	 */
//	public String saveFile(){
//
//		int choice = JOptionPane.showConfirmDialog(GUI.this, "You are being offered a file, accept?", "File Offer",
//				JOptionPane.YES_NO_OPTION);
//
//		if (choice == JOptionPane.YES_OPTION){
//			System.out.println("yes");
//			JFileChooser c = new JFileChooser();
//			// Demonstrate "Open" dialog:
//			int rVal = c.showOpenDialog(GUI.this);
//			if (rVal == JFileChooser.APPROVE_OPTION) {
//				File mostRecentOutputDirectory = c.getSelectedFile();
//				prefs.put("LAST_OUTPUT_DIR", mostRecentOutputDirectory.getAbsolutePath());
//				
//			}
//			if (rVal == JFileChooser.CANCEL_OPTION) {
//				// TODO maybe do something, don't think we should
//				System.exit(0);
//			}
//
//		}else{
//			System.out.println("no");
//		}
//		return null;
//
//	}
	
	/**
	 * Method to be called for saving files when a file transfer
	 * request is received, returns null when file offer is refused
	 * @param name of sender
	 * @param name of file
	 * @param size of file in bytes
	 * @return path to save file to
	 */
	private String saveFile(String senderName, String fileName, int fileSize){
		
		FileOfferDialog fod = new FileOfferDialog(GUI.this, senderName, fileName, fileSize);
		fod.setVisible(true);
		
		String rval = fod.getValue();
		
		return rval;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub


	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Observable o, Object arg) {


		if(arg instanceof ChatMessage){
			cp.addMessage(((ChatMessage) arg).getNickname(), ((ChatMessage) arg).getAddress(),((ChatMessage) arg).getMessage());
		}
		else if(arg instanceof FileOfferMessage){
			// TODO 1: play sound
			// TODO 2: parse system message
			cp.addMessage("FILE OFFER", ((FileOfferMessage) arg).getAddress(), ((FileOfferMessage) arg).getFileName() + " | File Size: " + ((FileOfferMessage) arg).getFileSize() + " bytes");
			saveFile("DEBUG", ((FileOfferMessage) arg).getFileName(), ((FileOfferMessage) arg).getFileSize());
		}


	}
}
