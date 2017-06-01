/**
 * 
 */
package com.ovrengineered.ovrGatewayConfigurator;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author christopherarmenio
 *
 */
public class DeviceManager
{
	private static DeviceManager SINGLETON = new DeviceManager();
	private static long CREDS_TIMEOUT_MS = 5000;
	
	
	private SerialTtyConnector serialPort = null;
	
	
	private DeviceManager()
	{
	}
	
	
	public void setSerialConnector(SerialTtyConnector stcIn)
	{
		this.serialPort = stcIn;
	}
	
	
	public String getUuid()
	{
		if( this.serialPort == null ) return null;
		
		String retVal = null;
		try
		{
			this.serialPort.startCommand();
			
			// send the command
			String command = "gw_getUuid";
			if( !this.sendCommand(command) ) return null;
		
			// short delay
			try{ Thread.sleep(1000); } catch( InterruptedException e) {}
			
			// read back the response
			retVal = this.readResponseToCommand(command);
		}
		finally
		{
			this.serialPort.stopCommand();
		}
		
		return retVal;
	}
	
	
	public Boolean areCredentialsSet()
	{
		if( this.serialPort == null ) return null;
		
		Boolean retVal = null;
		try
		{
			this.serialPort.startCommand();

			// send the command
			String command = "mqtt_areCredsSet";
			if( !this.sendCommand(command) ) return null;
			
			// short delay
			try{ Thread.sleep(1000); } catch(InterruptedException e) {}
			
			// read back the response
			String response_str = this.readResponseToCommand(command);
			if( (response_str != null) && response_str.equals("YES") ) retVal = true;
			else retVal = false;
		}
		finally
		{
			this.serialPort.stopCommand();
		}
		
		return retVal;
	}
	
	
	public boolean sendCertAndKeyPair(CertAndKeyPair cakpIn)
	{
		if( this.serialPort == null ) return false;
		
		try
		{
			this.serialPort.startCommand();
		
			// first, we need to clear our stored creds
			System.out.println("Clearing stored credentials...");
			if( !this.sendCommand("mqtt_clearCreds") )
			{
				return false;
			}
			Boolean yesOrNo = this.waitForYesOrNo();
			if( (yesOrNo == null) || (!yesOrNo) )
			{
				return false;
			}
			
			// short delay
			try{ Thread.sleep(3000); } catch(InterruptedException e) {}
			
			// first, we need to send the command, followed by the bytes
			System.out.println("Sending server root certificate...");
			if( !this.sendCommand("mqtt_setSrvCert") )
			{
				return false;
			}
			byte[] serverRootCertBytes = this.getServerRootCertBytes();
			System.out.printf("Server cert is %d bytes...\n", serverRootCertBytes.length);
			if( (serverRootCertBytes == null) || !this.sendBytes(serverRootCertBytes) )
			{
				return false;
			}
			yesOrNo = this.waitForYesOrNo();
			if( (yesOrNo == null) || (!yesOrNo) )
			{
				return false;
			}
			
			// first, we need to send the command, followed by the bytes
			System.out.print("Sending client certificate...");
			if( !this.sendCommand("mqtt_setClCert") )
			{
				return false;
			}
			byte[] clientCertBytes = cakpIn.getCertificatePem();
			System.out.printf("Client cert is %d bytes...\n", clientCertBytes.length);
			if( (clientCertBytes == null) || !this.sendBytes(clientCertBytes) )
			{
				return false;
			}
			yesOrNo = this.waitForYesOrNo();
			if( (yesOrNo == null) || (!yesOrNo) )
			{
				return false;
			}
			
			// first, we need to send the command, followed by the bytes
			System.out.println("Sending client private key...");
			if( !this.sendCommand("mqtt_setPrvKey") )
			{
				return false;
			}
			byte[] privateKeyBytes = cakpIn.getPrivateKey();
			System.out.printf("Private key is %d bytes...\n", privateKeyBytes.length);
			if( (privateKeyBytes == null) || !this.sendBytes(privateKeyBytes) )
			{
				return false;
			}
			yesOrNo = this.waitForYesOrNo();
			if( (yesOrNo == null) || (!yesOrNo) )
			{
				return false;
			}
		}
		finally
		{
			this.serialPort.stopCommand();
		}
		
		return true;
	}
	
	
	public static DeviceManager getSingleton()
	{
		return SINGLETON;
	}
	
	
	private boolean sendCommand(String commandIn)
	{
		return this.sendBytes((commandIn + "\n").getBytes());
	}
	
	
	private boolean sendBytes(byte[] bytesIn)
	{
		boolean retVal = false;
		try
		{
			this.serialPort.write(bytesIn);
			retVal = true;
		}
		catch(IOException e) { }
		
		return retVal;
	}
	
	
	private String readResponseToCommand(String commandIn)
	{
		String retVal = null;
		
		// read the echo first
		for( int i = 0; i < 10; i++ )
		{
			String echo = this.serialPort.readLine();
			if( echo == null ) continue;
			
			System.out.printf("echo: '%s'\n", echo);
			if( echo.endsWith(commandIn) ) break;
			
			try{ Thread.sleep(100); } catch(InterruptedException e) { }
		}
		
		// next line should _probably_ be the result
		retVal = this.serialPort.readLine();
		System.out.printf("got: '%s'\n", retVal);
		
		return retVal;
	}
	
	
	private Boolean waitForYesOrNo()
	{
		long startTime_ms = System.currentTimeMillis();
		while(true)
		{
			String currLine = this.serialPort.readLine();
			System.out.printf("gotline: '%s'\n", currLine);
			if( (currLine != null) && currLine.equals("YES") ) return true;
			else if( (currLine != null) && currLine.equals("NO") ) return false;
			
			if( (System.currentTimeMillis() - startTime_ms) > CREDS_TIMEOUT_MS ) break;
		}
		
		return null;
	}
	
	
	private byte[] getServerRootCertBytes()
	{
		byte[] retVal = null;
		
		URL url = this.getClass().getResource("/serverRootCert.pem");
		try
		{
			Path path = Paths.get(url.toURI());
			byte[] certBytes = Files.readAllBytes(path);
			
			// add the null termination since we're sending to a 'c' device
			retVal = new byte[certBytes.length+1];
			System.arraycopy(certBytes, 0, retVal, 0, certBytes.length);
			retVal[retVal.length-1] = 0;
		}
		catch( Exception e ) { }
		
		return retVal;
	}
}
