package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.collect.HashBiMap;

/**
 * Panel for chat application that contains
 * a visual list of all chat users that the application is aware of
 */

public class UserListPanel extends JPanel{

	/** The Application GUI parent of this Component */
	private GUI gui;

	/** Indicates if this components has finished initializing */
	private boolean isReady;

	/** Map containing a list of hosts mapped to their hostNames */
	private HashBiMap<Byte, String> hostMap;

	/** DefaultListModel containing all of the hostNames in the JList */
	DefaultListModel nicknames = new DefaultListModel();

	/** Visual list containing a list of hostnames that have been mapped to their hosts */
	private JList hostList;


	public UserListPanel(GUI gu, Collection<Byte> hostCollection) {
		super();
		gui = gu;

		hostMap = HashBiMap.create();
		for (Byte h : hostCollection) {
			hostMap.put(h, String.format("%d", h));
			nicknames.addElement(Byte.toString(h));
		}

		this.setLayout(new BorderLayout());
		this.setMinimumSize((new Dimension(100, 600)));
		this.setPreferredSize((new Dimension(100, 600)));
		// History Field
		hostList = new JList(nicknames);

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
						hostList.clearSelection();
					}
				}
			}
		};
		hostList.addListSelectionListener(listSelectionListener);
		isReady = true;
	}

	/** Finds the hostName belonging to a specified host */
	public String findHostName(int address){
		return hostMap.get((byte)address);

	}

	/** Finds the address key belonging to the hostName value */
	public byte findValueAddress(String hostName){
		try {
			Byte hostAddr = Byte.valueOf(hostName);
			if (hostMap.containsValue(hostName)) {
				return hostMap.inverse().get(hostName);
			} else {
				return hostAddr;
			}
		} catch (NumberFormatException e) {
			return hostMap.inverse().get(hostName);
		}
	}

	/** Updates the local hostMap and hostList with a new hostName belonging to an address */
	public void setHostName(byte address, byte[] hostName){

		for (int i = 0; i < hostMap.size(); i++){
			if(nicknames.get(i).equals(hostMap.get(address)))
				nicknames.set(i, String.valueOf(address) + ": " +new String(hostName));
		}
		hostMap.put(address, String.valueOf(address) + ": " + new String(hostName));


	}

	/** 
	 * Method determine if this component has finished initializing
	 * @return boolean
	 */
	public boolean isReady(){
		return isReady;
	}
}
