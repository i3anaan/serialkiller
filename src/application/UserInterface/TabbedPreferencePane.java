package application.UserInterface;

import java.awt.*;

import javax.swing.*;

class TabbedPreferencePane
		extends 	JFrame
{
	private		GUI			gui;
	private		JTabbedPane tabbedPane;
	private		JPanel		panel1;
	private		JPanel		panel2;
	private		JPanel		panel3;


	public TabbedPreferencePane(GUI gui)
	{
		this.gui = gui;
		
		setTitle( "GARGLE Preferences" );
		setSize( 400, 300 );
		setMinimumSize( new Dimension( 400, 300 ) );
		setMaximumSize( new Dimension( 400, 300 ) );
		setBackground( Color.gray );

		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		getContentPane().add( topPanel );

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

		JTextField field = new JTextField();
		field.setBounds( 10, 35, 150, 20 );
		panel1.add( field );

		// Save preferences
		JButton save = new JButton("Save");
		save.setBounds( 10, 80, 150, 20 );
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
		panel3.setLayout( new GridLayout( 3, 2 ) );

		panel3.add( new JLabel( "Field 1:" ) );
		panel3.add( new TextArea() );
		panel3.add( new JLabel( "Field 2:" ) );
		panel3.add( new TextArea() );
		panel3.add( new JLabel( "Field 3:" ) );
		panel3.add( new TextArea() );
	}

    // Main method to get things started
	public static void main( String args[] )
	{
		// Create an instance of the test application
		TabbedPreferencePane mainFrame	= new TabbedPreferencePane(null);
		mainFrame.setVisible( true );
	}
}