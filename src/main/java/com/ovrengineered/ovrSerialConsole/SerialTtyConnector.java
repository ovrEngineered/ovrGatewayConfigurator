package com.ovrengineered.ovrSerialConsole;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fazecast.jSerialComm.SerialPort;
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;

public class SerialTtyConnector implements TtyConnector
{

	private SerialPort serialPort = null;
	
	
	public void setSerialPort(SerialPort spIn)
	{
		this.serialPort = spIn;
	}
	
	
	@Override
	public boolean init(Questioner arg0)
	{
		return this.isConnected();
	}
	
	
	@Override
	public void close()
	{
		if( this.serialPort != null ) serialPort.closePort();
	}

	
	@Override
	public String getName()
	{
		return "SerialPort";
	}

	
	@Override
	public boolean isConnected()
	{
		return (this.serialPort != null) && this.serialPort.isOpen();
	}

	
	@Override
	public void resize(Dimension termSize, Dimension pixelSize)
	{
	}

	
	@Override
	public int waitFor() throws InterruptedException
	{
		return 0;
	}
	
	
	@Override
	public int read(char[] buffIn, int offsetIn, int lengthIn) throws IOException
	{
		byte[] rxBytes = new byte[lengthIn];
		
		// try to read our bytes
		int retVal = 0;
		while( this.serialPort.isOpen() )
		{
			if( (retVal = this.serialPort.readBytes(rxBytes, (long)lengthIn)) > 0 ) break;
		}
		
		// convert bytes to chars
		for( int i = 0; i < retVal; i++ )
		{
			buffIn[i] = (char)rxBytes[i];
		}
		
		return retVal;
	}

	
	@Override
	public void write(byte[] bytes) throws IOException
	{
		this.serialPort.writeBytes(bytes, bytes.length);
	}
	

	@Override
	public void write(String string) throws IOException
	{
		write(string.getBytes(StandardCharsets.UTF_8));
	}
}
