/**
 * 
 */
package com.ovrengineered.ovrGatewayConfigurator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import com.amazonaws.services.iot.model.KeyPair;
import com.amazonaws.util.Base64;

/**
 * @author christopherarmenio
 *
 */
public class PersistenceManager
{
	private static final String PROPERTY_FILE_STRING = System.getProperty("user.home") + File.separator + "ovrGatewayConfigurator" + File.separator + "creds.properties";
	private static final PersistenceManager SINGLETON = new PersistenceManager();
	
	
	private PersistenceManager()
	{
		
	}
	
	
	public boolean cacheCertAndKeyPairForDevice(String deviceUuidIn, CertAndKeyPair keyPairIn)
	{
		Properties properties = this.getPropertiesFromFile();
		if( properties == null ) return false;
		
		String keyPairString = this.certAndKeyPairToString(keyPairIn);
		if( keyPairString == null ) return false;
		properties.setProperty(deviceUuidIn, keyPairString);
		
		return this.storePropertiesToFile(properties);
	}
	
	
	public CertAndKeyPair getCachedCertAndKeyPairForDevice(String deviceUuidIn)
	{
		if( deviceUuidIn == null ) return null;
		
		Properties properties = this.getPropertiesFromFile();
		if( properties == null ) return null;
		
		String keyPairString = properties.getProperty(deviceUuidIn);
		if( keyPairString == null ) return null;
		
		return this.certAndKeyPairFromString(keyPairString);
	}
	
	
	private Properties getPropertiesFromFile()
	{
		Properties retVal = new Properties();
		
		try
		{
			File propertyFile = new File(PROPERTY_FILE_STRING);
			
			// will do nothing if already exists
			propertyFile.getParentFile().mkdirs();
			propertyFile.createNewFile();
			
			FileInputStream fis = new FileInputStream(propertyFile);
			retVal.load(fis);
			fis.close();
		}
		catch( Exception e )
		{
			retVal = null;
		}
		return retVal;
	}
	
	
	private boolean storePropertiesToFile(Properties propertiesIn)
	{
		if( propertiesIn == null ) return false;
		
		boolean retVal = false;
		try
		{
			File propertyFile = new File(PROPERTY_FILE_STRING);
			FileOutputStream fos = new FileOutputStream(propertyFile);
			propertiesIn.store(fos, "ovrBeacon Gateway Cached Credential File");
			fos.close();
			retVal = true;
		}
		catch( Exception e ) { }
		
		return retVal;
	}
	
	
	private String certAndKeyPairToString(CertAndKeyPair keyPairIn)
	{
		String retVal = null;
		
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(keyPairIn);
			oos.flush();
			
			retVal = new String(Base64.encode(baos.toByteArray()));
			
			oos.close();
			baos.close();
		}
		catch( Exception e ) { }
		
		return retVal;
	}
	
	
	private CertAndKeyPair certAndKeyPairFromString(String stringIn)
	{
		CertAndKeyPair retVal = null;
		
		try
		{
			ByteArrayInputStream baos = new ByteArrayInputStream(Base64.decode(stringIn));
			ObjectInputStream ois = new ObjectInputStream(baos);
			
			Object retVal_raw = ois.readObject();
			if( retVal_raw instanceof CertAndKeyPair ) retVal = (CertAndKeyPair)retVal_raw;
			
			ois.close();
			baos.close();
		}
		catch( Exception e ) { }
		
		return retVal;
	}
	
	
	public static PersistenceManager getSingleton()
	{
		return SINGLETON;
	}
}
