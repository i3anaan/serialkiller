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

	/** The append line for the chat */
	public  JTextField	myMessage;

	/** The area where all messages are appended to */
	public  JTextArea   taMessages;

	/** The Application GUI parent of this Component */
	private GUI gui;

	/** The host address of the chat partner */
	private byte address;


	public ChatPanel(GUI gu, byte address) {
		super();
		gui = gu;
		this.address = address;
		this.setLayout(new BorderLayout());

		// Append Field
		myMessage = new JTextField("");
		this.add(myMessage, BorderLayout.SOUTH);
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
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			if(!myMessage.getText().equals("")){
				String username = gui.getPreferences().get("USERNAME", "");
				//TODO find a proper hostID for ourselves
				addMessage(username, 6666,myMessage.getText());
				gui.getApplicationLayer().writeChatMessage(username, myMessage.getText(), address);
				myMessage.setText("");
			}
		}

	}



}
