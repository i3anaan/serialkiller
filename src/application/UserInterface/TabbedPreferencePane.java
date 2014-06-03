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
	/** The tabbedPane containing all of the individual preference panels */
	private		JTabbedPane tabbedPane;
	
	/** Preference panel containing options for user customization */
	private		JPanel		userPanel;
	
	/** Preference panel containing options for network transfers */
	private		JPanel		transferPanel;
	
	/** Preference panel containing options for audio settings */
	private		JPanel		audioPanel;
	
	/** The field containing the chosen username */
	private		JTextField 	nameField;
	
	/** The Application GUI parent of this Component */
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
		tabbedPane.addTab( "User", userPanel );
		tabbedPane.addTab( "File Transfer", transferPanel );
		tabbedPane.addTab( "Sound", audioPanel );
		topPanel.add( tabbedPane, BorderLayout.CENTER );
	}

	/** Method to create the userPanel part of the preference pane */
	public void createUserPage()
	{
		userPanel = new JPanel();
		userPanel.setLayout( null );

		// Set a username that is displayed with all chat messages
		JLabel label1 = new JLabel( "Username:" );
		label1.setBounds( 10, 15, 150, 20 );
		userPanel.add( label1 );

		nameField = new JTextField();
		nameField.setBounds( 10, 35, 150, 25 );
		userPanel.add( nameField );

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
		userPanel.add( save );
	}

	
	/** Method to create the transferPanel part of the preference pane */
	public void createFileTransferPage()
	{
		transferPanel = new JPanel();
		transferPanel.setLayout(null );

		// Default location to download files to
		JLabel pathLabel = new JLabel( "Default file path:" );
		pathLabel.setBounds( 10, 15, 150, 20 );
		transferPanel.add(pathLabel);
		JTextField defPathField = new JTextField();
		defPathField.setBounds( 10, 35, 300, 20 );
		transferPanel.add( defPathField );

		// Setting for ignoring all file offers
		JLabel blockLabel = new JLabel( "Ignore all file offers:" );
		blockLabel.setBounds( 10, 60, 150, 20 );
		transferPanel.add(blockLabel);
		JCheckBox block = new JCheckBox();
		block.setBounds( 160, 60, 150, 20 );
		transferPanel.add(block);

		// Save preferences
		JButton save = new JButton("Save");
		save.setBounds( 10, 105, 150, 20 );
		transferPanel.add( save );

	}

	/** Method to create the audioPanel part of the preference pane */
	public void createSoundPage()
	{
		audioPanel = new JPanel();
		audioPanel.setLayout(null );

		// Setting for ignoring all sounds
		JLabel muteLabel = new JLabel( "Mute all sounds:" );
		muteLabel.setBounds( 10, 60, 150, 20 );
		audioPanel.add(muteLabel);
		JCheckBox mute = new JCheckBox();
		mute.setBounds( 160, 60, 150, 20 );
		audioPanel.add(mute);
	}

}