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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.*;

import common.Stack;
import common.Startable;
import application.*;
import application.message.ChatMessage;
import application.message.FileOfferMessage;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

import log.LogMessage;
import log.Logger;


public class GUI extends JFrame implements ActionListener, ItemListener, Observer{

	/** The Logger object used by this layer to send log messages to the web interface */
	private static Logger logger;

	// private variables
	private		Preferences 		prefs; 					
	private 	ApplicationLayer 	apl;
	private 	ChatPanel 			cp; 					
	private 	UserListPanel 		ulp;

	public GUI(ApplicationLayer applicationLayer){
		super("G.A.R.G.L.E.");
		apl = applicationLayer;
		
		// Setup Observer/Observable relation
		apl.addObserver(this);
		

		// Initialize initial variables
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
		
		GUI.getLogger().warning("GUI started.");

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
				// Open dialog:
				int rVal = c.showOpenDialog(GUI.this);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					apl.readFile(c.getSelectedFile().getAbsolutePath());
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

		menu.add(sendFileItem);
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

	/** Returns the Logger object for this GUI */
	public static Logger getLogger() {
		if (logger == null) {
			logger = new Logger(LogMessage.Subsystem.APPLICATION);
		}
		return logger;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, fall back to cross-platform
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ex) {

			}
		}
	}
}
