package com.ovrengineered.ovrGatewayConfigurator;


import javax.swing.JPanel;
import java.awt.BorderLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class ConfigurationPanel extends JPanel
{
	private static final String TXT_UNKNOWN = "<unknown>";
	private static final String TXT_YES = "YES";
	private static final String TXT_NO = "NO";
	
	
	private final JLabel lblGatewayUuidValue;
	private final JLabel lblIsRegisteredWithAwsValue;
	private final JLabel lblAppHasCachedCredentialsValue;
	private final JLabel lblAreCredentialsSetValue;

	private final JButton btnDeleteFromAws;
	private final JButton btnRegisterWithAws;
	private final JButton btnRefresh;
	
	private String deviceUuid = null;
	private CertAndKeyPair certAndKeyPair = null;
	private final JButton btnProgramCredentials;
	

	/**
	 * Create the panel.
	 */
	public ConfigurationPanel()
	{
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel_form = new JPanel();
		add(panel_form, BorderLayout.CENTER);
		panel_form.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("right:default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblGatewayUuid = new JLabel("Gateway UUID:");
		panel_form.add(lblGatewayUuid, "2, 2");
		
		lblGatewayUuidValue = new JLabel(TXT_UNKNOWN);
		panel_form.add(lblGatewayUuidValue, "4, 2");
		
		JLabel lblIsRegisteredWithAws = new JLabel("Is registered w/ AWS:");
		panel_form.add(lblIsRegisteredWithAws, "2, 4");
		
		lblIsRegisteredWithAwsValue = new JLabel(TXT_UNKNOWN);
		panel_form.add(lblIsRegisteredWithAwsValue, "4, 4");
		
		JLabel lblAppHasCachedCredentials = new JLabel("App has cached credentials:");
		panel_form.add(lblAppHasCachedCredentials, "2, 6");
		
		lblAppHasCachedCredentialsValue = new JLabel("<unknown>");
		panel_form.add(lblAppHasCachedCredentialsValue, "4, 6");
		
		JLabel lblAreCredentialsSet = new JLabel("Are credentials set on device:");
		panel_form.add(lblAreCredentialsSet, "2, 8");
		
		lblAreCredentialsSetValue = new JLabel(TXT_UNKNOWN);
		panel_form.add(lblAreCredentialsSetValue, "4, 8");
		
		JPanel panel_buttons = new JPanel();
		add(panel_buttons, BorderLayout.SOUTH);
		panel_buttons.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_buttons_right = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_buttons_right.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_buttons.add(panel_buttons_right, BorderLayout.EAST);
		
		btnDeleteFromAws = new JButton("Delete From AWS");
		btnDeleteFromAws.setEnabled(false);
		btnDeleteFromAws.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if( AwsManager.getSingleton().deleteThing(ConfigurationPanel.this.deviceUuid) )
				{
					JOptionPane.showMessageDialog(null, "Thing deleted successfully", "AWS Success", JOptionPane.INFORMATION_MESSAGE);
					ConfigurationPanel.this.refresh();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Cannot delete thing", "AWS Error", JOptionPane.ERROR_MESSAGE);
					ConfigurationPanel.this.refresh();
				}
			}
		});
		
		btnProgramCredentials = new JButton("Program Credentials");
		btnProgramCredentials.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if( DeviceManager.getSingleton().sendCertAndKeyPair(ConfigurationPanel.this.certAndKeyPair) )
				{
					JOptionPane.showMessageDialog(null, "Credentials programmed successfully", "Device Success", JOptionPane.INFORMATION_MESSAGE);
					ConfigurationPanel.this.refresh();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Programming credentials failed", "Device Error", JOptionPane.ERROR_MESSAGE);
					ConfigurationPanel.this.refresh();
				}
			}
		});
		btnProgramCredentials.setEnabled(false);
		panel_buttons_right.add(btnProgramCredentials);
		panel_buttons_right.add(btnDeleteFromAws);
		
		btnRegisterWithAws = new JButton("Register With AWS");
		btnRegisterWithAws.setEnabled(false);
		btnRegisterWithAws.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if( AwsManager.getSingleton().registerThing(ConfigurationPanel.this.deviceUuid) )
				{
					JOptionPane.showMessageDialog(null, "Thing created successfully", "AWS Success", JOptionPane.INFORMATION_MESSAGE);
					ConfigurationPanel.this.refresh();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Cannot create thing", "AWS Error", JOptionPane.ERROR_MESSAGE);
					ConfigurationPanel.this.refresh();
				}
			}
		});
		panel_buttons_right.add(btnRegisterWithAws);
		
		JPanel panel_buttons_left = new JPanel();
		panel_buttons.add(panel_buttons_left, BorderLayout.WEST);
		
		btnRefresh = new JButton("Refresh");
		btnRefresh.setEnabled(false);
		btnRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ConfigurationPanel.this.refresh();
			}
		});
		panel_buttons_left.add(btnRefresh);
	}
	
	
	public void setEnabled(boolean enabledIn)
	{
		if( enabledIn )
		{
			this.deviceUuid = null;
			this.certAndKeyPair = null;
			
			this.btnDeleteFromAws.setEnabled(false);
			this.btnRefresh.setEnabled(true);
			this.btnRegisterWithAws.setEnabled(false);
			this.btnProgramCredentials.setEnabled(false);
		}
		else
		{
			this.deviceUuid = null;
			this.certAndKeyPair = null;
			
			this.lblGatewayUuidValue.setText(TXT_UNKNOWN);
			this.lblIsRegisteredWithAwsValue.setText(TXT_UNKNOWN);
			this.lblAppHasCachedCredentialsValue.setText(TXT_UNKNOWN);
			this.lblAreCredentialsSetValue.setText(TXT_UNKNOWN);
			
			this.btnDeleteFromAws.setEnabled(false);
			this.btnRefresh.setEnabled(false);
			this.btnRegisterWithAws.setEnabled(false);
			this.btnProgramCredentials.setEnabled(false);
		}
	}
	
	
	private void refresh()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// disable ui interactions for a minute
				ConfigurationPanel.this.btnDeleteFromAws.setEnabled(false);
				ConfigurationPanel.this.btnRefresh.setEnabled(false);
				ConfigurationPanel.this.btnRegisterWithAws.setEnabled(false);
				ConfigurationPanel.this.btnProgramCredentials.setEnabled(false);
				
				String uuid = DeviceManager.getSingleton().getUuid();
				ConfigurationPanel.this.lblGatewayUuidValue.setText((uuid != null) ? uuid : TXT_UNKNOWN);
				
				// make sure we got a uuid
				if( uuid == null )
				{
					// didn't get a UUID, therefore we can try again (refresh)
					// but can't register / delete
					ConfigurationPanel.this.btnRefresh.setEnabled(true);
				}
				
				ConfigurationPanel.this.deviceUuid = uuid;
				
				boolean isRegistered = AwsManager.getSingleton().isThingRegistered(uuid);
				ConfigurationPanel.this.lblIsRegisteredWithAwsValue.setText(isRegistered ? TXT_YES : TXT_NO);
				
				ConfigurationPanel.this.certAndKeyPair = PersistenceManager.getSingleton().getCachedCertAndKeyPairForDevice(uuid);
				ConfigurationPanel.this.lblAppHasCachedCredentialsValue.setText((ConfigurationPanel.this.certAndKeyPair != null) ? TXT_YES : TXT_NO);
				
				Boolean areCredentialsSet = DeviceManager.getSingleton().areCredentialsSet();
				ConfigurationPanel.this.lblAreCredentialsSetValue.setText((areCredentialsSet != null) ? 
																		  (areCredentialsSet ? TXT_YES : TXT_NO) : 
																		  TXT_UNKNOWN);
				
				// if we made it to here, we're good to do anything
				ConfigurationPanel.this.btnDeleteFromAws.setEnabled(isRegistered);
				ConfigurationPanel.this.btnRefresh.setEnabled(true);
				ConfigurationPanel.this.btnRegisterWithAws.setEnabled(!isRegistered);
				ConfigurationPanel.this.btnProgramCredentials.setEnabled(ConfigurationPanel.this.certAndKeyPair != null);
			}
		}).start();
	}

}
