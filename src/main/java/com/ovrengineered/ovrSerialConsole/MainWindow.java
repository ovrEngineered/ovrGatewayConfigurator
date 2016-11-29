package com.ovrengineered.ovrSerialConsole;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.fazecast.jSerialComm.SerialPort;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.ovrengineered.ui.serialPortSelector.SerialPortChangeListener;
import com.ovrengineered.ui.serialPortSelector.SerialPortSelector;


public class MainWindow implements SerialPortChangeListener
{

	private JFrame frame;
	
	private JediTermWidget jt;
	private SerialTtyConnector stc = new SerialTtyConnector();
	

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
		
		this.jt = new JediTermWidget(80, 100, new OvrSettingsProvider());
		frame.getContentPane().add(this.jt, BorderLayout.CENTER);
	}

	
	@Override
	public void onOpen(SerialPort spIn)
	{
		spIn.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
		stc.setSerialPort(spIn);
		
		this.jt.setTtyConnector(this.stc);
		this.jt.start();
	}

	
	@Override
	public void onClose()
	{
		this.jt.stop();
		
		stc.setSerialPort(null);
	}

}
