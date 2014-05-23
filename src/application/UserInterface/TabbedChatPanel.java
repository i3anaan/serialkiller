package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;


/**
 * Main Chat Panel 'controller' that holds all of the future
 * chat panels. It holds the home panel by default
 * @author msbruning
 *
 */
public class TabbedChatPanel extends JPanel{

	// Class variables
	private		JTabbedPane tabbedPane;
	private		Map<String, JPanel> tabIndex;
	
	// private variables
	private GUI gui;
	
	
	public TabbedChatPanel(GUI gu) {
		super();
		gui = gu;
		tabIndex = new HashMap<String,JPanel>();
		
		this.setLayout( new BorderLayout() );
		setBackground( Color.gray );
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		this.add(topPanel);
		
		
		JPanel homePanel = new JPanel();
		homePanel.setLayout(new BorderLayout());
		
		// Create a tabbed pane
				tabbedPane = new JTabbedPane();
				tabIndex.put("Home", homePanel);
				tabbedPane.addTab( "Home", homePanel );
				topPanel.add( tabbedPane, BorderLayout.CENTER );
		
	}
	
	/**
	 * adds a tab for a host with the specified hostName
	 * @param name of the host
	 */
	public void addChatPanel(String hostName, byte address){
		
		JPanel newPanel = new ChatPanel(gui, address);
		tabIndex.put(hostName, newPanel);
		tabbedPane.addTab(hostName, newPanel);
		
	}
	
	/**
	 * removes a tab belonging to a host with the specified hostName
	 * @param name of the host
	 */
	public void removeChatPanel(String hostName){
		
		ChatPanel removePanel = (ChatPanel) tabIndex.get(hostName);
		tabIndex.remove(removePanel);
		tabbedPane.remove(removePanel);
		
	}
	
	/**
	 * Parses a chat message to the tab belonging to the specified host
	 * @param nickName of the user
	 * @param address of the user
	 * @param chat message to be parsed
	 */
	public void parseMessage(String nickName, int address, String message){
		String hostName = gui.getUserList().findHostName(address);
		ChatPanel cp = (ChatPanel) tabIndex.get(hostName);
		cp.addMessage(nickName, address, message);
		
	}
	



}
