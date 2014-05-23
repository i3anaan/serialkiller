package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;


/**
 * Main Chat Panel 'controller' that holds all of the future
 * chat panels. It holds the home panel by default
 * @author msbruning
 *
 */
public class TabbedChatPanel extends JPanel{

	// Class variables
	private		JTabbedPane tabbedPane;
	private		ArrayList<String> tabIndex;
	
	// private variables
	private GUI gui;
	
	
	public TabbedChatPanel(GUI gu) {
		super();
		gui = gu;
		
		setBackground( Color.gray );
		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		this.add(topPanel);
		
		JPanel homePanel = new JPanel();
		
		// Create a tabbed pane
				tabbedPane = new JTabbedPane();
				tabIndex.add("Home");
				tabbedPane.addTab( "Home", homePanel );
				topPanel.add( tabbedPane, BorderLayout.CENTER );
		
	}
	
	public void addChatPanel(String hostName){
		
		JPanel newPanel = new ChatPanel(gui);
		tabIndex.add(hostName);
		tabbedPane.addTab(hostName, newPanel);
		
	}
	
	public void removeChatPanel(String hostName){
		
		int index = tabIndex.indexOf(hostName);
		tabIndex.remove(index);
		tabbedPane.remove(index);
		
	}
	
	public void parseMessage(){
		
	}
	
//	@Override
//	public void addMessage(final String name1, final int host, final String msg) {
//		taMessages.append("<" + name1 + " @" + host + "> " + msg + "\n");
//
//	}


}
