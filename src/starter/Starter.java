package starter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import phys.DummyPhysicalLayer;
import phys.LptErrorHardwareLayer;
import phys.LptHardwareLayer;
import phys.PhysicalLayer;
import util.Environment;
import web.WebService;

import network.NetworkLayer;

import application.ApplicationLayer;
import application.GUI;

import link.BittasticLinkLayer;
import link.BufferStufferLinkLayer;
import log.LogMessage;
import log.Logger;

public class Starter extends JFrame implements ActionListener {
	private Logger log;
	
	private String swingOptions[] = {"Yes", "No"};
	private Class<?> applicationLayers[] = {ApplicationLayer.class};
	private Class<?> networkLayers[] = {NetworkLayer.class};
	private Class<?> linkLayers[] = {BittasticLinkLayer.class, BufferStufferLinkLayer.class};
	private Class<?> physLayers[] = {LptHardwareLayer.class, LptErrorHardwareLayer.class, DummyPhysicalLayer.class};
	private String webOptions[] = {"Yes", "No"};
	
	private JComboBox swingCombo;
	private JComboBox appCombo;
	private JComboBox netCombo;
	private JComboBox linkCombo;
	private JComboBox physCombo;
	private JComboBox webCombo;
	
	private JButton start;
	private JButton quit;
	
	public static void main(String[] args) {
		new Starter().run();
	}
	
	public Starter() {
        log = new Logger(LogMessage.Subsystem.STARTER);
        swingCombo = combo(swingOptions);
        appCombo = combo(applicationLayers);
        netCombo = combo(networkLayers);
        linkCombo = combo(linkLayers);
        physCombo = combo(physLayers);
        webCombo = combo(webOptions);

		setLayout(new BorderLayout());
		
		String label = String.format("SerialKiller version %s. Pick your favorite implementations below.", Environment.getGitCommit());
		JLabel topLabel = new JLabel(label);
		topLabel.setBorder(new EmptyBorder(12, 12, 12, 12));
		add(topLabel, BorderLayout.NORTH);
		
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
		
		JPanel buttons = new JPanel();
		start = button("Start");
		quit = button("Quit");
		buttons.add(start);
		buttons.add(quit);
		add(buttons, BorderLayout.SOUTH);
		
		pack();
	}

	public void run() {
		setVisible(true);
	}
	
	private void startStack() {
		log.info("Starter starting stack.");
		log.debug("How many stacks would a stack starter start if a stack starter could start stacks?");
		
		// Instantiate and start the stack, bottom-up.
		try {
			log.info("Starting physical layer.");
			Class<?> physClass = physLayers[physCombo.getSelectedIndex()];
			log.info("Got class " + physClass);
			PhysicalLayer phy = (PhysicalLayer)physClass.newInstance();
			log.info("Got instance.");
			phy.start();
			log.info("Started " + phy);
			
			log.info("Starting application layer.");
			ApplicationLayer al = new ApplicationLayer();
			
			swingCombo.setEnabled(false);
			appCombo.setEnabled(false);
			netCombo.setEnabled(false);
			linkCombo.setEnabled(false);
			physCombo.setEnabled(false);
			webCombo.setEnabled(false);
			start.setEnabled(false);
			
			// Start the web service, if requested.
			if (webCombo.getSelectedIndex() == 0) {
				new Thread(new WebService(8080)).start();
			}
			
			// Start the Swing app, if requested.
			if (swingCombo.getSelectedIndex() == 0) {
				GUI gui = new GUI();
				al.addObserver(gui);
			}
		} catch (InstantiationException e) {
			log.emerg(e.toString());
		} catch (IllegalAccessException e) {
			log.emerg(e.toString());
		}
	}
	
	private JButton button(String label) {
		JButton out = new JButton(label);
		out.addActionListener(this);
		return out;
	}
	
	private JLabel label(String label) {
		return new JLabel(label);
	}
	
	private JComboBox combo(Object[] items) {
		return new JComboBox(items);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("Start")) {
			startStack();
		} else if (event.getActionCommand().equals("Quit")) {
			System.exit(0);
		} else {
			log.info("Received unknown action " + event);
		}
	}
}
