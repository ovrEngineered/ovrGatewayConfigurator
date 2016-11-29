package com.ovrengineered.ui.serialPortSelector;

import javax.swing.JPanel;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.fazecast.jSerialComm.SerialPort;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a utility class for displaying standard configuration options associated
 * with opening and closing a serial port (using the RXTX libraries). 
 * 
 * @author Christopher Armenio
 */
public class SerialPortSelector extends JPanel
{
	private static final String DISCONNECTED_STRING = "Connect";
	private static final String CONNECTED_STRING = "Disconnect";
	
	
	private JButton btnConnect;
	private JComboBox<Integer> cmbBaudRate;
	
	
	private SerialPort serialPort = null;
	private SerialPortDropdown serialPortDropdown;
	private List<SerialPortChangeListener> changeListeners = new ArrayList<SerialPortChangeListener>();
	
	
	/**
	 * Creates the serial port selector
	 */
	public SerialPortSelector()
	{
		setBorder(new TitledBorder(null, "Serial Port Configuration:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		serialPortDropdown = new SerialPortDropdown();
		add(serialPortDropdown, "2, 2, fill, default");
		
		cmbBaudRate = new JComboBox<Integer>(new Integer[] {9600, 115200});
		add(cmbBaudRate, "4, 2, fill, default");
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if( isConnected() )
				{
					// we are disconnecting
					SerialPortSelector.this.serialPort.closePort();
					SerialPortSelector.this.serialPort = null;
					btnConnect.setText(DISCONNECTED_STRING);
					serialPortDropdown.setEnabled(true);
					cmbBaudRate.setEnabled(true);
					
					// notify our listeners
					for( int i = 0; i < changeListeners.size(); i++ )
					{
						changeListeners.get(i).onClose();
					}
				}
				else
				{
					// we are connecting
					SerialPortSelector.this.serialPort = serialPortDropdown.getSelectedItem();
					if( SerialPortSelector.this.serialPort == null )
					{
						JOptionPane.showMessageDialog(null, "No serial port selected", "Error - cannot open serial port", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// if we made it here, we have a good identifier...now try to open it
					if( SerialPortSelector.this.serialPort.openPort() )
					{
						SerialPortSelector.this.serialPort.setComPortParameters((Integer)cmbBaudRate.getSelectedItem(), 8, 1, 0);
					}
					else
					{
						JOptionPane.showMessageDialog(null, "SerialPort Error", "Error - cannot open serial port", JOptionPane.ERROR_MESSAGE);
						SerialPortSelector.this.serialPort = null;
						return;
					}
					
					// if we made it here, we're open
					serialPortDropdown.setEnabled(false);
					cmbBaudRate.setEnabled(false);
					btnConnect.setText(CONNECTED_STRING);
					
					// notify our listeners
					for( int i = 0; i < changeListeners.size(); i++ )
					{
						changeListeners.get(i).onOpen(SerialPortSelector.this.serialPort);
					}
				}
			}
		});
		add(btnConnect, "6, 2, default, bottom");
	}

	
	/**
	 * @return true if the serial port is current connected and open
	 */
	public boolean isConnected()
	{
		return this.btnConnect.getText().equals(CONNECTED_STRING);
	}
	
	
	/**
	 * @return the currently connected/open serial port or null if not open/connected
	 */
	public SerialPort getConnectedSerialPort()
	{
		return this.serialPort;
	}
	
	
	/**
	 * Adds a change listener that is notified on changes to the serial port status
	 * 
	 * @param spclIn the listener to add
	 */
	public void addChangeListener(SerialPortChangeListener spclIn)
	{
		this.changeListeners.add(spclIn);
	}
}
