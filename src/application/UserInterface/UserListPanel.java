package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.collect.HashBiMap;

/**
 * Panel for chat application that contains
 * a visual list of all chat users that the application is aware of
 */

public class UserListPanel extends JPanel{

	// Private variables
	private GUI gui;
	private JTextArea userList;

	/** Map containing a list of hosts mapped to their hostNames */
	private HashBiMap<Byte, String> hostMap;
	/** Visual list containing a list of hostnames that have been mapped to their hosts */
	private JList<Object> hostList;
	

	public UserListPanel(GUI gu, Collection<Byte> hostCollection) {
		super();
		gui = gu;

		hostMap = HashBiMap.create();
		for (Byte h : hostCollection) {
			hostMap.put(h, String.format("%d", h));
		}

		this.setLayout(new BorderLayout());
		this.setMinimumSize((new Dimension(100, 600)));
		this.setPreferredSize((new Dimension(100, 600)));
		// History Field
		Collection<String> nicknames = hostMap.values();
		hostList = new JList<Object>(nicknames.toArray());

		userList = new JTextArea("", 15, 15);
		userList.setEditable(false);
		userList.setLineWrap(true);

		JScrollPane taScroll = new JScrollPane(hostList, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.add(taScroll, BorderLayout.CENTER);	


		ListSelectionListener listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				
				if (!e.getValueIsAdjusting()) {
					int selection[] = hostList.getSelectedIndices();
					for (int i = 0; i< selection.length; i++) {
						String hostName =  String.valueOf(hostList.getSelectedValue());
						gui.getChatPanel().addChatPanel(hostName, findValueAddress((String) hostList.getSelectedValue()));
					}
				}
			}
		};
		hostList.addListSelectionListener(listSelectionListener);
	}

	/** Finds the hostName belonging to a specified host */
	public String findHostName(int address){
		return hostMap.get(address);

	}

	/** Finds the address key belonging to the hostName value */
	public byte findValueAddress(String name){
		return hostMap.inverse().get(name);
		
	}
	
	/** Updates the local hostMap with a new hostName belonging to an address */
	public void setHostName(byte address, byte[] hostName){
		hostMap.put(address, new String(hostName));
		
	}
}
