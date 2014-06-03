package starter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

import application.ApplicationLayer;
import application.UserInterface.GUI;
import common.Stack;
import common.Startable;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import phys.diag.NullPhysicalLayer;
import util.Environment;
import web.WebService;
import network.NetworkLayer;
import network.tpp.TPPNetworkLayer;
import link.BittasticLinkLayer;
import link.LinkLayer;
import link.BufferStufferLinkLayer;
import link.jack.JackTheRipper;
import log.LogMessage;
import log.Logger;

/**
 * The Starter is responsible for starting complete instances of the
 * Serialkiller stack, including the graphical interface and the web monitor
 * service if required.
 */
public class Starter extends JFrame implements ActionListener {
	private Logger log;
	
	// Options for all the combo boxes.
	private String swingOptions[] = {"Yes", "No"};
	private Class<?> applicationLayers[] = {ApplicationLayer.class};
	private Class<?> networkLayers[] = {TPPNetworkLayer.class};
	private Class<?> linkLayers[] = {BittasticLinkLayer.class, BufferStufferLinkLayer.class,JackTheRipper.class};
	private Class<?> physLayers[] = {NullPhysicalLayer.class, LptHardwareLayer.class, LptErrorHardwareLayer.class};
	private String webOptions[] = {"Yes", "No"};
	
	// Combo boxes for all the options.
	private JComboBox swingCombo;
	private JComboBox appCombo;
	private JComboBox netCombo; 
	private JComboBox linkCombo;
	private JComboBox physCombo;
	private JComboBox webCombo;
	
	// Main buttons.
	private JButton start;
	private JButton quit;
	
	// Collection of threads managed by the starter.
	private ArrayList<Thread> threads;
	
	/** Main entry point. This is the mainest main of all the mains. */
	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, fall back to cross-platform
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception ex) {

			}
		}
		new Starter().run();
	}
	
	public Starter() {
        log = new Logger(LogMessage.Subsystem.STARTER);
        threads = new ArrayList<Thread>();
        
        // Build the combo's.
        swingCombo = combo(swingOptions);
        appCombo = combo(applicationLayers);
        netCombo = combo(networkLayers);
        linkCombo = combo(linkLayers);
        physCombo = combo(physLayers);
        webCombo = combo(webOptions);

		setLayout(new BorderLayout());
		
		// Put a descriptive label at the top.
		String label = String.format("SerialKiller version %s. Pick your favorite implementations below.", Environment.getGitCommit());
		JLabel topLabel = new JLabel(label);
		topLabel.setBorder(new EmptyBorder(12, 12, 12, 12));
		add(topLabel, BorderLayout.NORTH);
		
		// Put the main form in the center.
		JPanel form = new JPanel();
		form.setLayout(new GridLayout(0, 2, 12, 12));
		form.add(label("Start Swing application?"));
		form.add(swingCombo);
		form.add(label("Application layer implementation"));
		form.add(appCombo);
		form.add(label("Network layer implementation"));
		form.add(netCombo);
		form.add(label("Link layer implementation"));
		form.add(linkCombo);
		form.add(label("Physical layer implementation"));
		form.add(physCombo);
		form.add(label("Start web service?"));
		form.add(webCombo);
		form.setBorder(new EmptyBorder(12, 12, 12, 12));
		add(form, BorderLayout.CENTER);
		
		// Put the main buttons at the bottom.
		JPanel buttons = new JPanel();
		start = button("Start");
		quit = button("Quit");
		buttons.add(start);
		buttons.add(quit);
		add(buttons, BorderLayout.SOUTH);
		
		// We've added everything we want.
		pack();
	}

	/** Magically have the dialog appear out of thin air. */
	public void run() {
		setVisible(true);
	}
	
	/** Does the actual stack-starting magic. */
	private void startStack() {
		log.info("Starter starting stack.");
		log.debug("How many stacks would a stack starter start if a stack starter could start stacks?");
		
		Stack stack = new Stack(this);
		
		// Instantiate and start the stack, bottom-up.
		try {
			Class<?> physClass = physLayers[physCombo.getSelectedIndex()];
			stack.physLayer = (PhysicalLayer)physClass.newInstance();
			log.debug("Got physical layer implementation.");
			
			Class<?> linkClass = linkLayers[linkCombo.getSelectedIndex()];
			stack.linkLayer = (LinkLayer)linkClass.newInstance();
			log.debug("Got link layer implementation.");
			
			Class<?> netClass = networkLayers[netCombo.getSelectedIndex()];
			stack.networkLayer = (NetworkLayer)netClass.newInstance();
			log.debug("Got network layer implementation.");
			
			Class<?> appClass = applicationLayers[appCombo.getSelectedIndex()];
			stack.applicationLayer = (ApplicationLayer)appClass.newInstance();
			log.debug("Got application layer implementation.");
			
			((Startable)stack.physLayer).start(stack);
			((Startable)stack.linkLayer).start(stack);
			((Startable)stack.networkLayer).start(stack);
			((Startable)stack.applicationLayer).start(stack);
			
			if (webCombo.getSelectedIndex() == 0) {
				stack.webService = new WebService(8080);
				new Thread(stack.webService).start();
			}
			
			log.info("Got instances.");
			
			startGUI(stack);
			
			
			// Disable all controls except the Quit button.
			swingCombo.setEnabled(false);
			appCombo.setEnabled(false);
			netCombo.setEnabled(false);
			linkCombo.setEnabled(false);
			physCombo.setEnabled(false);
			webCombo.setEnabled(false);
			start.setEnabled(false);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			// This should not happen at all ever.
			log.emerg(e.toString());
		}
	}
	
	/** Make a new JButton and add ourselves as the ActionListener. */
	private JButton button(String label) {
		JButton out = new JButton(label);
		out.addActionListener(this);
		return out;
	}
	
	/** Make a new JLabel. */
	private JLabel label(String label) {
		return new JLabel(label);
	}
	
	/** Make a new JComboBox and add ourselves as the ActionListener. */
	private JComboBox combo(Object[] items) {
		return new JComboBox(items);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(start)) {
			startStack();
		} else if (event.getSource().equals(quit)) {
			System.exit(0);
		} else {
			log.info("Received unknown action " + event);
		}
	}

	public void restart() {
		log.warning("Starter received a request to restart.");
		
		for (Thread t : threads) {
			t.interrupt();
			log.info("Interrupted and joining thread " + t);
			try {
				t.join();
			} catch (InterruptedException e) {
				log.warning("Interrupted ourselves while waiting for " + t);
			}
		}
		
		startStack();
	}
	
	public void startGUI(final Stack stack){
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	GUI gui = new GUI(stack.applicationLayer);
		    }
		});
	}
}
