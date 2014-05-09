package application.UserInterface;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.*;

import application.*;
import application.message.ChatMessage;

import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;


public class GUI extends JFrame implements ActionListener, ItemListener, Observer {

	// private variables
	private ApplicationLayer apl;
	
	// local elements
	private ChatPanel cp = new ChatPanel(this);
	private UserListPanel ulp = new UserListPanel(this);
	private JTextField	myMessage;
	private JTextArea   taMessages;
	
	public GUI(ApplicationLayer al){
		super("G.A.R.G.L.E.");	
		this.apl = al;
		this.setLayout(new BorderLayout());
		
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(800, 600));
		
		buildChatMenu();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
				e.getWindow().dispose();
			}
			public void windowClosed(final WindowEvent e) {
				System.exit(0);
			}
		}
				);

		pack();
		validate();
		setVisible(true);
		
	}
	
	private void buildChatMenu() {
		
		this.add(cp, BorderLayout.CENTER);
		this.add(ulp, BorderLayout.EAST);

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Observable o, Object arg) {
		
		if(arg instanceof ChatMessage){
			cp.addMessage(((ChatMessage) arg).getNickname(), ((ChatMessage) arg).getMessage());
		}
		
		
	}
}
