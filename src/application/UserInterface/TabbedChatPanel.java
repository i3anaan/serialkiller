package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.google.common.collect.HashBiMap;


/**
 * Main Chat Panel 'controller' that holds all of the future
 * chat panels. It holds the home panel by default
 * @author msbruning
 *
 */
public class TabbedChatPanel extends JPanel{

	// Class variables
	private		ClosableTabbedPane	 	tabbedPane;
	private		HashBiMap<String, JPanel> 	tabIndex;

	// private variables
	private GUI gui;
	
	/** Indicates if this components has finished initializing */
	private boolean isReady;


	public TabbedChatPanel(GUI gu) {
		super();
		gui = gu;
		tabIndex = HashBiMap.create();

		setLayout( new BorderLayout() );
		setBackground( Color.gray );

		Image image = new ImageIcon("logo.png").getImage();
		BackgroundPanel topPanel = new BackgroundPanel(image);
		topPanel.setLayout( new BorderLayout() );
		this.add(topPanel);

		// Create a tabbed pane
		tabbedPane = new ClosableTabbedPane() {
			public boolean tabAboutToClose(int tabIndex) {
				String tab = tabbedPane.getTabTitleAt(tabIndex);
				removeChatPanel(tab);
				return true;
			}
		};
		topPanel.add(tabbedPane, BorderLayout.CENTER);

		// Set component to ready
		isReady = true;
	}

	/**
	 * adds a tab for a host with the specified hostName
	 * @param name of the host
	 * @param address of the host
	 */
	public void addChatPanel(String hostName, byte address){

		if(tabIndex.get(hostName) == null) {

			JPanel newPanel = new ChatPanel(gui, address);
			tabIndex.put(hostName, newPanel);
			tabbedPane.addTab(hostName, newPanel);
		}

	}

	/**
	 * removes a tab belonging to a host with the specified hostName
	 * @param name of the host
	 */
	public void removeChatPanel(String hostName){

		if(tabIndex.get(hostName) != null){
			tabIndex.remove(hostName);
		}
	}

	/**
	 * Method to get the name of the host related to the active panel
	 * @return hostName
	 */
	public String getActiveHost(){
		return tabIndex.inverse().get(tabbedPane.getSelectedComponent());
	}
	
	/**
	 * Method to change the HostName of an already active tab.
	 * @param address of the host
	 * @Param new name for the host
	 */
	public void setHostName(byte address, String oldName, byte[] newName){
		if(tabIndex.get(oldName) != null) {
			//TODO check if this method works
			tabbedPane.setTitleAt(address, (String.valueOf(address) + ": " +new String(newName)));
		}
	}

	/**
	 * Parses a chat message to the tab belonging to the specified host
	 * @param nickName of the user
	 * @param address of the user
	 * @param chat message to be parsed
	 */
	public void parseMessage(String nickName, Byte address, String message){
		String hostName = gui.getUserList().findHostName(address);
		int adr = address;
		
		ChatPanel cp = (ChatPanel) tabIndex.get(hostName);
		// If no tab is active create a new tab and parse
		if(cp == null){
			addChatPanel(hostName, address);
			((ChatPanel) tabIndex.get(hostName)).addMessage(nickName, adr, message);
		}
		// Else just parse and (cp != null)
		else{
				cp.addMessage(nickName, adr, message);
		}
	}
	
	/** 
	 * Method determine if this component has finished initializing
	 * @return boolean
	 */
	public boolean isReady(){
		return isReady;
	}

}
