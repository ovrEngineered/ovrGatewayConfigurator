package com.ovrengineered.ovrGatewayConfigurator;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import com.fazecast.jSerialComm.SerialPort;
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;

public class SerialTtyConnector implements TtyConnector
{
	private SerialPort serialPort = null;
	private ReentrantLock lock = new ReentrantLock();
	
	
	public void setSerialPort(SerialPort spIn)
	{
		this.serialPort = spIn;
	}
	
	
	public void startCommand()
	{
		this.lock.lock();
		System.out.println(">>commandStarted");
	}
	
	
	public void stopCommand()
	{
		this.lock.unlock();
		System.out.println("<<commandStopped");
	}
	
	
	public String readLine()
	{
		String retVal = "";
		
		this.lock.lock();
		try
		{
			while(true)
			{
				byte[] buffer = new byte[1];
				
				int numBytesRead = this.serialPort.readBytes(buffer, buffer.length);
				if( numBytesRead == -1 ) return null;
				else if( numBytesRead == 0 ) continue;
				
				retVal += new String(buffer);
				int index_lineEnd = -1;
				if( ((index_lineEnd = retVal.indexOf("\r\n")) > 0) || ((index_lineEnd = retVal.indexOf("\n")) > 0) )
				{
					retVal = retVal.substring(0, index_lineEnd);
					break;
				}
			}
		}
		finally
		{
			this.lock.unlock();
		}
		
		return retVal;
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
		int retVal = 0;
	
		this.lock.lock();
		System.out.printf(">>read: %d\n", lengthIn);
		try
		{
			byte[] rxBytes = new byte[lengthIn];
			
			// try to read our bytes
			while( this.serialPort.isOpen() )
			{
				if( (retVal = this.serialPort.readBytes(rxBytes, (long)lengthIn)) > 0 ) break;
			}
			
			// convert bytes to chars
			for( int i = 0; i < retVal; i++ )
			{
				buffIn[i] = (char)rxBytes[i];
			}
		}
		finally
		{
			this.lock.unlock();
		}
		System.out.println("<<read");
		return retVal;
	}

	
	@Override
	public void write(byte[] bytes) throws IOException
	{
		System.out.println(">>write");
		int bytesWritten = 0;
		do
		{
//			byte[] bytesToWrite = Arrays.copyOfRange(bytes, bytesWritten, Math.min(bytes.length, bytesWritten+128));
			byte[] bytesToWrite = Arrays.copyOfRange(bytes, bytesWritten, bytes.length);
			int retVal = this.serialPort.writeBytes(bytesToWrite, bytesToWrite.length);
			if( retVal < 0 ) throw new IOException();
			bytesWritten += retVal;
		}while( bytesWritten < bytes.length );
		System.out.println("<<write");
	}
	

	@Override
	public void write(String string) throws IOException
	{
		write(string.getBytes(StandardCharsets.UTF_8));
	}
}
