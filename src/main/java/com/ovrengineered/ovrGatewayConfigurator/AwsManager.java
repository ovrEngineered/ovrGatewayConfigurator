/**
 * 
 */
package com.ovrengineered.ovrGatewayConfigurator;

import java.util.prefs.Preferences;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClientBuilder;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyResult;
import com.amazonaws.services.iot.model.AttachThingPrincipalRequest;
import com.amazonaws.services.iot.model.AttachThingPrincipalResult;
import com.amazonaws.services.iot.model.CertificateStatus;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.amazonaws.services.iot.model.CreateThingRequest;
import com.amazonaws.services.iot.model.CreateThingResult;
import com.amazonaws.services.iot.model.DeleteCertificateRequest;
import com.amazonaws.services.iot.model.DeleteCertificateResult;
import com.amazonaws.services.iot.model.DeleteThingRequest;
import com.amazonaws.services.iot.model.DeleteThingResult;
import com.amazonaws.services.iot.model.DescribeCertificateRequest;
import com.amazonaws.services.iot.model.DescribeThingRequest;
import com.amazonaws.services.iot.model.DescribeThingResult;
import com.amazonaws.services.iot.model.DetachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.DetachPrincipalPolicyResult;
import com.amazonaws.services.iot.model.DetachThingPrincipalRequest;
import com.amazonaws.services.iot.model.DetachThingPrincipalResult;
import com.amazonaws.services.iot.model.ListThingPrincipalsRequest;
import com.amazonaws.services.iot.model.ListThingPrincipalsResult;
import com.amazonaws.services.iot.model.ResourceNotFoundException;
import com.amazonaws.services.iot.model.UpdateCertificateRequest;
import com.amazonaws.services.iot.model.UpdateCertificateResult;

/**
 * @author christopherarmenio
 *
 */
public class AwsManager
{
	private static AwsManager SINGLETON = new AwsManager();
	private static String NEW_THING_POLICY_NAME = "OpenAccess";
	
	
	private final AWSIot iot;
	
	
	private AwsManager()
	{
		// make sure our credentials are set appropriately
		AWSCredentials credentials = null;
		try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
		
		
		this.iot = AWSIotClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	}
	
	
	public boolean isThingRegistered(String thingUuidIn)
	{
		boolean retVal = false;
		try
		{
			retVal = (this.iot.describeThing(new DescribeThingRequest().withThingName(AwsManager.thingNameFromUuid(thingUuidIn))) != null);
		}
		catch( Exception e ) { }
		
		return retVal;
	}
	
	
	public boolean registerThing(String thingUuidIn)
	{
		String thingName = AwsManager.thingNameFromUuid(thingUuidIn);
		
		boolean retVal = false;
		try
		{
			System.out.printf("Creating thing '%s'...\n", thingName);
			CreateThingResult result_ctr = this.iot.createThing(new CreateThingRequest().withThingName(thingName));
			if( result_ctr == null ) return false;
			
			System.out.printf("Creating keys and certificat...\n");
			CreateKeysAndCertificateResult result_ckcr = this.iot.createKeysAndCertificate(new CreateKeysAndCertificateRequest());
			if( result_ckcr == null ) return false;
			
			System.out.printf("Attaching policy '%s' with certificate...\n", NEW_THING_POLICY_NAME);
			AttachPrincipalPolicyResult result_appr = this.iot.attachPrincipalPolicy(new AttachPrincipalPolicyRequest().withPolicyName(NEW_THING_POLICY_NAME).withPrincipal(result_ckcr.getCertificateArn()));
			if( result_appr == null ) return false;
			
			System.out.printf("Attaching certificate to thing...\n");
			AttachThingPrincipalResult result_atpr = this.iot.attachThingPrincipal(new AttachThingPrincipalRequest().withPrincipal(result_ckcr.getCertificateArn()).withThingName(thingName));
			if( result_atpr == null ) return false;
			
			System.out.printf("Activating certificate...\n");
			UpdateCertificateResult result_ucr = this.iot.updateCertificate(new UpdateCertificateRequest().withCertificateId(result_ckcr.getCertificateId()).withNewStatus(CertificateStatus.ACTIVE));
			if( result_ucr == null ) return false;
			
			System.out.printf("Caching keys locally...\n");
			if( !PersistenceManager.getSingleton().cacheCertAndKeyPairForDevice(thingUuidIn, new CertAndKeyPair(result_ckcr.getCertificatePem(), result_ckcr.getKeyPair().getPrivateKey())) ) return false;
			
			retVal = true;
		}
		catch( Exception e )
		{
			System.err.printf("error: '%s'\n", e.getMessage());
		}
		
		return retVal;
	}
	
	
	public boolean deleteThing(String thingUuidIn)
	{
		String thingName = AwsManager.thingNameFromUuid(thingUuidIn);
		
		boolean retVal = false;
		try
		{
			System.out.printf("Listing thing principals for thing '%s'...\n", thingName);
			ListThingPrincipalsResult result_ltpr = this.iot.listThingPrincipals(new ListThingPrincipalsRequest().withThingName(thingName));
			if( (result_ltpr == null) || (result_ltpr.getPrincipals() == null) || (result_ltpr.getPrincipals().size() <= 0) ) return false;
			
			String principalId = result_ltpr.getPrincipals().get(0);
			String certificateId = principalId.substring(principalId.length()-64);
			
			System.out.printf("Deactivating certificate '%s'...\n", certificateId);
			UpdateCertificateResult result_ucr = this.iot.updateCertificate(new UpdateCertificateRequest().withCertificateId(certificateId).withNewStatus(CertificateStatus.INACTIVE));
			if( result_ucr == null ) return false;
			
			System.out.printf("Detaching policy '%s' from certificate\n", NEW_THING_POLICY_NAME);
			DetachPrincipalPolicyResult result_dppr = this.iot.detachPrincipalPolicy(new DetachPrincipalPolicyRequest().withPrincipal(principalId).withPolicyName(NEW_THING_POLICY_NAME));
			if( result_dppr == null ) return false;
			
			System.out.printf("Detaching certificate from thing...\n");
			DetachThingPrincipalResult result_dtpr = this.iot.detachThingPrincipal(new DetachThingPrincipalRequest().withPrincipal(principalId).withThingName(thingName));
			if( result_dtpr == null ) return false;
			
			System.out.printf("Deleting certificate '%s'...\n", certificateId);
			DeleteCertificateResult result_dcr = this.iot.deleteCertificate(new DeleteCertificateRequest().withCertificateId(certificateId));
			if( result_dcr == null ) return false;
			
			System.out.printf("Deleting thing...\n");
			DeleteThingResult result_dtr = this.iot.deleteThing(new DeleteThingRequest().withThingName(thingName));
			if( result_dtr == null ) return false;
			
			retVal = true;
		}
		catch( Exception e )
		{
			System.err.printf("error: '%s'\n", e.getMessage());
		}
		
		return retVal;
	}
	
	
	public static AwsManager getSingleton()
	{		
		return SINGLETON;
	}
	
	
	private static String thingNameFromUuid(String uuidIn)
	{
		return String.format("gateway-%s", uuidIn);
	}
}
