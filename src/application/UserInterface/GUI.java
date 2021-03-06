package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;

import log.LogMessage;
import log.Logger;
import application.ApplicationLayer;
import application.message.*;


public class GUI extends JFrame implements Observer{

	/** The Logger object used by this layer to send log messages to the web interface */
	private static Logger logger;
	/** The preferences object used by this application */
	private		Preferences 		prefs; 
	/** The application layer this application communicates with */
	private 	ApplicationLayer 	apl;
	/** The container for all the ChatPanel Tabs*/
	private 	TabbedChatPanel 	cp; 			
	/** The list of hostNames we can communicate with */
	private 	UserListPanel 		ulp;
	private JMenuItem sendFileItem;

	public GUI(ApplicationLayer applicationLayer){
		super("G.A.R.G.L.E.");
		apl = applicationLayer;

		// Initialize initial variables
		prefs 				= Preferences.userNodeForPackage(getClass());
		cp					= new TabbedChatPanel(this);
		ulp 				= new UserListPanel(this, loadHostList());

		// Layout main application Window
		this.setLayout(new BorderLayout());
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(800, 600));

		buildBarMenu();
		buildChatMenu();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				e.getWindow().dispose();
			}
			@Override
			public void windowClosed(final WindowEvent e) {
				System.exit(0);
			}
		});

		// Start observable relation and make GUI ready for interaction
		Start();
		pack();
		validate();
		setVisible(true);

		GUI.getLogger().info("GUI started.");
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
		sendFileItem = new JMenuItem("Send File");
		sendFileItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
				// Open dialog:
				if(cp.getActiveHost() != null){
					int rVal = c.showOpenDialog(GUI.this);
					if (rVal == JFileChooser.APPROVE_OPTION) {
						apl.sendFileOffer(c.getSelectedFile().getAbsolutePath(), ulp.findValueAddress(cp.getActiveHost()));
					}
				}
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
		// Exit item
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
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

	/**
	 * getter for the UserListPanel containing a list of all the hosts
	 * @return UserLisPanel
	 */
	public UserListPanel getUserList(){
		return ulp;
	}

	/**
	 * getter for the main chat panel in this application
	 * @return TabbedChatPanel
	 */
	public TabbedChatPanel getChatPanel(){
		return cp;
	}

	/**
	 * getter for the Preferences object belonging to this application
	 * @return preferences
	 */
	public Preferences getPreferences(){
		return prefs;
	}

	/**
	 * getter for the ApplicationLayer being used by this application
	 * @return ApplicationLayer
	 */
	public ApplicationLayer getApplicationLayer(){
		return apl;
	}

	/**
	 * Returns the Logger object for this GUI
	 * @return Logger object
	 */
	public static Logger getLogger() {
		if (logger == null) {
			logger = new Logger(LogMessage.Subsystem.APPLICATION);
		}
		return logger;
	}

	/**
	 * Loads a collection holding a list of hosts into the GUI.
	 * @return the collection of hosts
	 */
	public Collection<Byte> loadHostList(){
		return apl.getHosts();
	}

	/**
	 * Method to retrieve the address of this host.
	 * @return the address of this host.
	 */
	public Byte getHost(){
		return apl.getHost();
	}

	// Starter of the GUI
	private void Start(){
		// Setup Observer/Observable relation
		apl.addObserver(this);
		apl.getHosts(true);
	}
	// Update Event Methods

	@Override
	public void update(Observable o, Object arg) {
		// Chat message has been received
		if(arg instanceof ChatMessage){
			cp.parseMessage(((ChatMessage) arg).getNickname(), ((ChatMessage) arg).getAddress(),((ChatMessage) arg).getMessage());
		}
		// File offer has been received
		else if(arg instanceof FileOfferMessage){
			// If IgnoreFileTransfer option has been set the offer will be ignored
			if(!(this.getPreferences().get("TRANSFERIGNORE", "").equals("true"))){
				String filePath = saveFile(ulp.findHostName(((FileOfferMessage) arg).getAddress()), ((FileOfferMessage) arg).getFileName(), ((FileOfferMessage) arg).getFileSize());
				if(filePath != null){
					apl.sendFileAccept((FileOfferMessage) arg, filePath);
					cp.parseMessage(new Date().toString(), ((FileOfferMessage) arg).getAddress(), String.format("Accepted file offer for: %s (%d bytes)", ((FileOfferMessage) arg).getFileName(), ((FileOfferMessage) arg).getFileSize()));
				}
			}
		}
		// WHOIS response has been received
		else if(arg instanceof IdentificationMessage){
			//TODO test if this method works
			//cp.setHostName(((IdentificationMessage) arg).getAddress(), ulp.findHostName(((IdentificationMessage) arg).getAddress()), ((IdentificationMessage) arg).getPayload());
			ulp.setHostName(((IdentificationMessage) arg).getAddress(), ((IdentificationMessage) arg).getPayload());
		}
		else if(arg instanceof FileTransferMessage){
			cp.parseMessage(new Date().toString(), ((FileTransferMessage) arg).getAddress(), String.format("Completed file transfer for: %s (%d bytes)", ((FileTransferMessage) arg).getFileName(), ((FileTransferMessage) arg).getFileSize()));
		}
	}
}