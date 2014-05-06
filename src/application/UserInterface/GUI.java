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

import java.awt.event.*;


public class GUI extends JFrame implements ActionListener, ItemListener {

	// private variables
	
	// local elements
	private ChatPanel cp = new ChatPanel();
	private UserListPanel ulp = new UserListPanel();
	private JTextField	myMessage;
	private JTextArea   taMessages;
	
	public GUI(){
		super("G.A.R.G.L.E.");	
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
}
