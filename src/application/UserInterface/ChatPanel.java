package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Panel for chat application that contains
 * the chat history and new append line.
 * @author msbruning
 *
 */
public class ChatPanel extends JPanel implements KeyListener, UIMessage{

	// Class variables
	public  JTextField	myMessage;
	public  JTextArea   taMessages;
	
	// private variables
	private GUI gui;
	
	
	public ChatPanel(GUI gu) {
		super();
		gui = gu;
		this.setLayout(new BorderLayout());
		
		// Append Field
		myMessage = new JTextField("");
		this.add(myMessage, BorderLayout.SOUTH);
		//myMessage.setEditable(false);
		myMessage.addKeyListener(this);
		
		
		// History Field
		taMessages = new JTextArea("", 15, 50);
		taMessages.setEditable(false);
		taMessages.setLineWrap(true);
		
		JScrollPane taScroll = new JScrollPane(taMessages, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.add(taScroll, BorderLayout.CENTER);	
		
	}
	
	@Override
	public void addMessage(final String name1, final int host, final String msg) {
		taMessages.append("<" + name1 + " @" + host + "> " + msg + "\n");

	}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			// call Application Layer to send message
			// temp debug line
			String username = gui.getPreferences().get("USERNAME", "");
			addMessage(username, 6666,myMessage.getText());
			myMessage.setText("");
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
