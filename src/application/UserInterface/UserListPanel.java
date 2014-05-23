package application.UserInterface;

import java.awt.BorderLayout;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Panel for chat application that contains
 * a visual list of all chat users that the application is aware of
 */

public class UserListPanel extends JPanel{

	// Private variables
	private GUI gui;
	private JTextArea userList;
	
	/** Map containing a list of hosts mapped to their hostNames */
	private Map<Integer, String> hostMap;
	/** Visual list containing a list of hostnames that have been mapped to their hosts */
	private JList hostList;
	
	public UserListPanel(GUI gu) {
		super();
		gui = gu;
		this.setLayout(new BorderLayout());
		// History Field
		JList jlist = new JList();
				userList = new JTextArea("", 15, 15);
				userList.setEditable(false);
				userList.setLineWrap(true);
				
				JScrollPane taScroll = new JScrollPane(userList, 
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				this.add(taScroll, BorderLayout.CENTER);	
	}

	/** Finds the hostName belonging to a specified host */
	public String findHostName(int address){
		return hostMap.get(address);
	}
}
