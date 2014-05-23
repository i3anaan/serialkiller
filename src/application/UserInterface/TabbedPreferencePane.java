package application.UserInterface;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

public class TabbedPreferencePane
extends 	Dialog
{
	private		JTabbedPane tabbedPane;
	private		JPanel		panel1;
	private		JPanel		panel2;
	private		JPanel		panel3;
	private		JTextField 	nameField;
	private		GUI			gui;


	public TabbedPreferencePane(GUI gui, String name, boolean modal)
	{
		super(gui, name, modal);
		this.gui = gui;
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				e.getWindow().dispose();
			}
		});


		setSize( 400, 300 );
		setMinimumSize( new Dimension( 400, 300 ) );
		setMaximumSize( new Dimension( 400, 300 ) );
		setBackground( Color.gray );

		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		this.add(topPanel);

		// Create the tab pages
		createUserPage();
		createFileTransferPage();
		createSoundPage();

		// Create a tabbed pane
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab( "User", panel1 );
		tabbedPane.addTab( "File Transfer", panel2 );
		tabbedPane.addTab( "Sound", panel3 );
		topPanel.add( tabbedPane, BorderLayout.CENTER );
	}

	public void createUserPage()
	{
		panel1 = new JPanel();
		panel1.setLayout( null );

		// Set a username that is displayed with all chat messages
		JLabel label1 = new JLabel( "Username:" );
		label1.setBounds( 10, 15, 150, 20 );
		panel1.add( label1 );

		nameField = new JTextField();
		nameField.setBounds( 10, 35, 150, 25 );
		panel1.add( nameField );

		// Save preferences
		JButton save = new JButton("Save");
		save.setBounds( 10, 80, 150, 20 );
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.getPreferences().put("USERNAME", nameField.getText());
				dispose();
			}
		});
		panel1.add( save );
	}

	public void createFileTransferPage()
	{
		panel2 = new JPanel();
		panel2.setLayout(null );

		// Default location to download files to
		JLabel pathLabel = new JLabel( "Default file path:" );
		pathLabel.setBounds( 10, 15, 150, 20 );
		panel2.add(pathLabel);
		JTextField defPathField = new JTextField();
		defPathField.setBounds( 10, 35, 300, 20 );
		panel2.add( defPathField );

		// Setting for ignoring all file offers
		JLabel blockLabel = new JLabel( "Ignore all file offers:" );
		blockLabel.setBounds( 10, 60, 150, 20 );
		panel2.add(blockLabel);
		JCheckBox block = new JCheckBox();
		block.setBounds( 160, 60, 150, 20 );
		panel2.add(block);

		// Save preferences
		JButton save = new JButton("Save");
		save.setBounds( 10, 105, 150, 20 );
		panel2.add( save );

	}

	public void createSoundPage()
	{
		panel3 = new JPanel();
		panel3.setLayout(null );

		// Setting for ignoring all sounds
		JLabel muteLabel = new JLabel( "Mute all sounds:" );
		muteLabel.setBounds( 10, 60, 150, 20 );
		panel3.add(muteLabel);
		JCheckBox mute = new JCheckBox();
		mute.setBounds( 160, 60, 150, 20 );
		panel3.add(mute);
	}

}