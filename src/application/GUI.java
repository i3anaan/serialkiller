package application;


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
import application.UserInterface.*;
//import application.UserInterface.GUI;
import application.message.ChatMessage;
import application.message.FileOfferMessage;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class GUI extends JFrame implements ActionListener, ItemListener, Observer {

	private JPanel cp = new JPanel();
	private JPanel ulp = new JPanel();
	private static GUI				 gui;
	
	// Menu Bar Elements
	JMenuBar menuBar;
	JMenu menu, submenu;

	public GUI(){
		super();
		gui = this;
		
		
		
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

	public static void main(final String[] args) {
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
		new GUI();
		
		
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
				saveFile(); // Put whatever here
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
				
				options();
				
			}

			
		});
		menu.add(sendFileItem);
		menu.add(optionItem);
		menu.add(exitItem);

		this.setJMenuBar(menuBar);
	}

	private void buildChatMenu() {

		this.add(cp, BorderLayout.CENTER);
		this.add(ulp, BorderLayout.EAST);

	}

	private void options() {
		// UN COMMENT TO TEST
		//TabbedPreferencePane tpp = new TabbedPreferencePane(gui, "Preferences", true);
		//tpp.setVisible(true);
		
	}
	/**
	 * Method to be called for saving files when a file transfer
	 * request is received
	 * @return the path to save the file to
	 */
	public String saveFile(){

		int choice = JOptionPane.showConfirmDialog(GUI.this, "You are being offered a file, accept?", "File Offer",
				JOptionPane.YES_NO_OPTION);

		if (choice == JOptionPane.YES_OPTION){
			System.out.println("yes");
			JFileChooser c = new JFileChooser();

			int rVal = c.showOpenDialog(GUI.this);
			if (rVal == JFileChooser.APPROVE_OPTION) {

			}
			if (rVal == JFileChooser.CANCEL_OPTION) {

			}

		}else{
			System.out.println("no");
		}
		return null;

	}

	public void save2(){
		JFileChooser c = new JFileChooser();


		int rVal = c.showOpenDialog(GUI.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			System.exit(0);

		}
		if (rVal == JFileChooser.CANCEL_OPTION) {
			System.exit(0);

		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}


}