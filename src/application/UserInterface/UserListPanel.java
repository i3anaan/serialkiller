package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel for chat application that contains
 * a visual list of all chat users that the application is aware of
 */

public class UserListPanel extends JPanel implements ListSelectionListener{

	// Private variables
	private GUI gui;
	private JTextArea userList;
	
	/** Map containing a list of hosts mapped to their hostNames */
	private Map<Integer, String> hostMap;
	/** Visual list containing a list of hostnames that have been mapped to their hosts */
	private JList hostList;
	
	public UserListPanel(GUI gu, Collection<Byte> hostCollection) {
		super();
		gui = gu;
		
		Object[] hosts = hostCollection.toArray();
		this.setLayout(new BorderLayout());
		this.setMinimumSize((new Dimension(100, 600)));
		this.setPreferredSize((new Dimension(100, 600)));
		// History Field
		JList jlist = new JList(hosts);

				userList = new JTextArea("", 15, 15);
				userList.setEditable(false);
				userList.setLineWrap(true);
				
				JScrollPane taScroll = new JScrollPane(jlist, 
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				this.add(taScroll, BorderLayout.CENTER);	
				
				
				ListSelectionListener listSelectionListener = new ListSelectionListener() {
				      public void valueChanged(ListSelectionEvent listSelectionEvent) {
				        if (!listSelectionEvent.getValueIsAdjusting()) {
				          JList list = (JList) listSelectionEvent.getSource();
				          int selections[] = list.getSelectedIndices();
				          List selectionValues = list.getSelectedValuesList();
				          for (int i = 0, n = selections.length; i < n; i++) {
				            if (i == 0) {
				              
				            }
				            String hostName =  String.valueOf(list.getSelectedValue());
				            gui.getChatPanel().addChatPanel(hostName, (byte) list.getSelectedValue());
				          }
				        }
				      }
				    };
				    jlist.addListSelectionListener(listSelectionListener);
	}

	/** Finds the hostName belonging to a specified host */
	public String findHostName(int address){
		return hostMap.get(address);
	}
	
	@Override
	public void valueChanged(final ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (lsm.isSelectionEmpty()) {
			System.out.println("emtpy!");
		} else {
			// Find out which indexes are selected.
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {
					System.out.println("selected something!");
				}
			}
		}

	}
}
