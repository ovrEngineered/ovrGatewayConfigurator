/**
 * 
 */
package com.ovrengineered.ovrGatewayConfigurator;

import java.io.Serializable;

/**
 * @author christopherarmenio
 *
 */
public class CertAndKeyPair implements Serializable
{
	private static final long serialVersionUID = 5669492159683721610L;

	
	private final String certificatePem;
	private final String privateKey;
	
	
	public CertAndKeyPair(String certificatePemIn, String privateKeyIn)
	{
		this.certificatePem = certificatePemIn;
		this.privateKey = privateKeyIn;
	}
	
	
	public byte[] getCertificatePem()
	{
		byte[] certBytes = this.certificatePem.getBytes();
		byte[] retVal = new byte[certBytes.length+1];
		System.arraycopy(certBytes, 0, retVal, 0, certBytes.length);
		retVal[retVal.length-1] = 0;
		return retVal;
	}
	
	
	public byte[] getPrivateKey()
	{
		byte[] keyBytes = this.privateKey.getBytes();
		byte[] retVal = new byte[keyBytes.length+1];
		System.arraycopy(keyBytes, 0, retVal, 0, keyBytes.length);
		retVal[retVal.length-1] = 0;
		return retVal;
	}
}
