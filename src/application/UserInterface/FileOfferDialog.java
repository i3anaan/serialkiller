package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.EventListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FileOfferDialog extends Dialog implements EventListener{

	/** file offer reject value to indicate a file offer has been rejected */
	public static final String FILEOFFER_REJECT = null;
	
	/** Path to save the offered file to or null in case of rejection */
	private		String 		result = null;
	
	private		String		fileName;
	
	/** Preferences element for this application */
	private 	Preferences prefs;
	
	/** Panel containing all the elements of this dialog */
	private		JPanel		panel1;
	
	/** TextField displaying the value of result */
	private		JTextField  field = new JTextField();
	
	/**
	 * Dialog to confirm a file transfer
	 * @param gui parent
	 * @param file sender
	 * @param file name
	 * @param file size
	 */
	public FileOfferDialog(GUI gui,String sender, String name, int size) {
		super(gui, "File Offer", true);
		prefs = Preferences.userNodeForPackage(this.getClass());
		fileName = name;
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				e.getWindow().dispose();
			}
		});
		

		setSize( 400, 200 );
		setMinimumSize( new Dimension( 400, 200 ) );
		setMaximumSize( new Dimension( 400, 200 ) );
		setBackground( Color.gray );

		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		this.add(topPanel);
		
		panel1 = new JPanel();
		panel1.setLayout( null );

		// FileInfo
		JLabel labelSender 	= new JLabel("Sender:");
		labelSender.setBounds( 10, 5, 150, 20 );
		panel1.add( labelSender );
		JLabel lsender 		= new JLabel(sender);
		lsender.setBounds( 100, 5, 250, 20 );
		panel1.add( lsender );
		JLabel labelName 	= new JLabel("File Name:");
		labelName.setBounds( 10, 20, 150, 20 );
		panel1.add( labelName );
		JLabel lname 		= new JLabel(name);
		lname.setBounds( 100, 20, 250, 20 );
		panel1.add( lname );
		JLabel labelSize 	= new JLabel("File Size:");
		labelSize.setBounds( 10, 35, 150, 20 );
		panel1.add( labelSize );
		JLabel lsize 		= new JLabel(Integer.toString(size) + " Bytes");
		lsize.setBounds( 100, 35, 250, 20 );
		panel1.add( lsize );
		JSeparator sep = new JSeparator();
		sep.setBounds( 10, 55, 380, 20 );
		panel1.add( sep );
		
		
		// FilePath
		JLabel label1 = new JLabel( "File Path:" );
		label1.setBounds( 10, 65, 150, 20 );
		panel1.add( label1 );
		field.setText(prefs.get("LAST_OUTPUT_DIR", ""));
		field.setBounds( 10, 85, 250, 25 );
		panel1.add( field );

		// Cancel Transfer
		JButton cancel = new JButton("Cancel");
		cancel.setBounds( 10, 130, 100, 20 );
		cancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				result = null;
				FileOfferDialog.this.dispose();
			}
		});
		panel1.add( cancel );
		
		// Browse Directory
		JButton browse = new JButton("Browse");
		browse.setBounds( 280, 85, 100, 20 );
		browse.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jFileChooser = new JFileChooser(prefs.get("LAST_OUTPUT_DIR", ""));
				jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int rVal = jFileChooser.showDialog(FileOfferDialog.this, "Select");
				if (rVal == JFileChooser.APPROVE_OPTION) {
					File lastOutputDir = jFileChooser.getSelectedFile();
					prefs.put("LAST_OUTPUT_DIR", lastOutputDir.getAbsolutePath());
					field.setText(String.format("%s/%s",prefs.get("LAST_OUTPUT_DIR", ""), fileName));
				}
			}
		});
		panel1.add( browse );
				
		// Save File
		JButton save = new JButton("Save");
		save.setBounds( 280, 130, 100, 20 );
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				result = field.getText();
				FileOfferDialog.this.dispose();
			}
		});
		panel1.add( save );
		
		topPanel.add(panel1);
		
		
	}
	
	/**
	 * This method shows the current value of the current
	 * return value
	 * @return null or path value
	 */
	public String getValue(){
		return result;
	}
}
