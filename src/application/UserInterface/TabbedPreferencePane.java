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

	/** The field containing the chosen username */
	private		JTextField 	nameField;
	
	JCheckBox block = new JCheckBox();

	/** The Application GUI parent of this Component */
	private		GUI			gui;


	public TabbedPreferencePane(GUI gui, String name, boolean modal)
	{
		super(gui, name, modal);
		this.gui = gui;
		this.addWindowListener(new WindowAdapter() {
			@Override
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

		// Create a tabbed pane
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab( "User", userPanel );
		tabbedPane.addTab( "File Transfer", transferPanel );
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
		nameField.setText(gui.getPreferences().get("USERNAME", ""));
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

		// Setting for ignoring all file offers
		JLabel blockLabel = new JLabel( "Ignore all file offers:" );
		blockLabel.setBounds( 10, 60, 150, 20 );
		transferPanel.add(blockLabel);
		//JCheckBox block = new JCheckBox();
		
		//Check the box if the setting has been enabled already
		if(gui.getPreferences().get("TRANSFERIGNORE", "").equals("true")){
			block.setSelected(true);
		}
		block.setBounds( 160, 60, 150, 20 );
		transferPanel.add(block);

		// Save preferences
		JButton save = new JButton("Save");
		save.setBounds( 10, 105, 150, 20 );
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.getPreferences().put("TRANSFERIGNORE", (""+block.isSelected()));
				dispose();
			}
		});
		transferPanel.add( save );

	}

}