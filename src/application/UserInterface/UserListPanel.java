package application.UserInterface;

import java.awt.BorderLayout;

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
	
	public UserListPanel(GUI gu) {
		super();
		gui = gu;
		this.setLayout(new BorderLayout());
		// History Field
				userList = new JTextArea("", 15, 15);
				userList.setEditable(false);
				userList.setLineWrap(true);
				
				JScrollPane taScroll = new JScrollPane(userList, 
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

				this.add(taScroll, BorderLayout.CENTER);	
	}

}
