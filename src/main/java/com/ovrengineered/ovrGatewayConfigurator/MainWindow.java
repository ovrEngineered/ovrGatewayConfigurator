package com.ovrengineered.ovrGatewayConfigurator;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import com.fazecast.jSerialComm.SerialPort;
import com.jediterm.terminal.ui.JediTermWidget;
import com.ovrengineered.ui.serialPortSelector.SerialPortChangeListener;
import com.ovrengineered.ui.serialPortSelector.SerialPortSelector;
import javax.swing.JTabbedPane;


public class MainWindow implements SerialPortChangeListener
{

	private JFrame frame;
	
	private JediTermWidget jt;
	private SerialTtyConnector stc = new SerialTtyConnector();
	private JTabbedPane tabbedPane;
	private ConfigurationPanel configurationPanel;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
					
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @wbp.parser.entryPoint
	 */
	public MainWindow()
	{
		initialize();
	}

	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 827, 485);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SerialPortSelector serialPortSelector = new SerialPortSelector();
		serialPortSelector.addChangeListener(this);
		frame.getContentPane().add(serialPortSelector, BorderLayout.NORTH);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		configurationPanel = new ConfigurationPanel();
		tabbedPane.addTab("Configuration", null, configurationPanel, null);
		
		this.jt = new JediTermWidget(80, 100, new OvrSettingsProvider());
		tabbedPane.addTab("Serial Console", null, jt, null);
	}

	
	@Override
	public void onOpen(SerialPort spIn)
	{
		spIn.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
		
		stc.setSerialPort(spIn);
		
		this.jt.setTtyConnector(this.stc);
		this.jt.start();
		
		DeviceManager.getSingleton().setSerialConnector(this.stc);
		this.configurationPanel.setEnabled(true);
	}

	
	@Override
	public void onClose()
	{
		this.jt.stop();	
		this.stc.setSerialPort(null);
		this.configurationPanel.setEnabled(false);
		DeviceManager.getSingleton().setSerialConnector(null);
	}

}
