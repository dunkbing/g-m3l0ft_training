#if USE_BILLING
package APP_PACKAGE.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Build;
import android.telephony.SmsManager;

import APP_PACKAGE.billing.common.XXTEA;
import APP_PACKAGE.billing.common.Crockford;
import APP_PACKAGE.billing.common.SMSUtils;

import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.billing.common.AModelActivity;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

public class SMS {
	SET_TAG("Billing");
	
	public final static int STATUS_INIT 		= 0;
	public final static int STATUS_WAITING 		= 1;
	public final static int STATUS_COMPLETE		= 2;
	
	private static int mState					= STATUS_INIT;
	private static int mSentStatus				= STATUS_INIT;
	private static int mDeliveredStatus			= STATUS_INIT;
	
	
	private static AModelActivity mBillingActivity;
	private static Device	mDevice;
	
	private static int mMessageID;


	private final String         SMS_FORMAT_UNLOCK_WORD              = "UNLOCK";
	private String getFormatUnlockWord() 
	{
		return SMS_FORMAT_UNLOCK_WORD;
	}

	private final String         SMS_FORMAT_VERSION                  = "V008";
	public String getFormatVersion()
	{
		return SMS_FORMAT_VERSION;
	}
    
    public SMS(AModelActivity activity, Device device)
	{
		mDevice 			= device;
		mBillingActivity 	= activity;
		
		
		mState				= STATUS_INIT;
		mSentStatus			= STATUS_INIT;
		mDeliveredStatus	= STATUS_INIT;
	}
	
	public void release()
	{
	
		mBillingActivity.unregisterReceiver(mBroadcastDelivered);
		mBillingActivity.unregisterReceiver(mBroadcastSent);
		mBroadcastDelivered = null;
		mBroadcastSent		= null;
		
		mDevice 			= null;
		mBillingActivity 	= null;
		
		mState				= STATUS_INIT;
		mSentStatus			= STATUS_INIT;
		mDeliveredStatus	= STATUS_INIT;
		mMessageID			= 0;
	}
	
	public boolean isCompleted()
	{
		return (mState == STATUS_COMPLETE);
	}
	
	public int getMessageID()
	{
		return mMessageID;
	}
	public static void handleValidateLicense(boolean sucess, int msgID)
	{
		DBG("Billing","SMS end process: "+sucess+ " msgID: "+msgID);
		mState = STATUS_COMPLETE;
		mMessageID = msgID;
		
		if(sucess)
		{
			DBG("Billing","SUCCESS ON handleValidateLicense");
			SUtils.getLManager().saveUnlockGame(1);
		}
		Model.successResult = sucess;
		mBillingActivity.getModelBilling().onValidationHandled();
	}
	
	public static String getServerNumber()
	{
		return ""+mDevice.getServerInfo().getServerNumber();
	}
	
	private String getPlatformId()
	{
		return "6686";
	}
	
	public void sendUnlockMessage()
	{
	
		String message = "";
		String BLANK_SPACE = " ";
		String binaryData = "";

	if (Build.VERSION.SDK_INT >= 19) //if it's Android KIT KAT, use V17 for message format (Encrypted message)
	{
		binaryData = SMSUtils.SMS_ENCRYPTED_HEADER; //header for the binary format specification, with this we tell it is encrypted. normally should send v17 but with value 10 0000 it takes it as v17

		//Adding [ALIAS]
		if(mDevice.getServerInfo().getAlias().length()!=0) //There Alias Information in profile received. must add it to message
		{
			message = mDevice.getServerInfo().getAlias() + BLANK_SPACE;
		}
		message += SMSUtils.SMS_TITLE_MESSAGE + BLANK_SPACE;// add text: GameloftOrder HQ request to make message more friendly to user.
		
		//Adding [INAPP] or [UNLOCK]
			binaryData += SMSUtils.SMS_ENCRYPTED_HEADER_UNLOCK; //assign the binary code for UNLOCK, since in this class only Try and Buy is used, just the value Unlock is added.

			// *Fix: random code is -1 when the first launch billing
		SUtils.getLManager().setRandomCodeNumber(mDevice.createUniqueCode());
		
		//message += BLANK_SPACE + getFormatVersion();//normally assigned, in this type of encrypted SMS format (v17) there's no need, was specified in the first header.
		//[Add Game Code to the message] Parameter Type: 0. Game IGP Code. 4 alphanumeric characters converted from base 36 to binary. Parameter value length = 21 bits.
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_GAMECODE, mDevice.getDemoCode());
		//[Add random code to the message] Parameter Type: 1. Random code. 4 digits stored as 14 bits.
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_RANDOMCODE, String.valueOf(SUtils.getLManager().getRandomCodeNumber()));
		//[Add Platform_ID] Parameter Type 2. Platform ID. Integer stored as 15 bits
		if(mDevice.getServerInfo().getPlatformId() != null && !mDevice.getServerInfo().getPlatformId().equals("-1"))
			binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_PLATFORMID, mDevice.getServerInfo().getPlatformId());
		else
			binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_PLATFORMID, getPlatformId());
		//[Add Profile ID] Parameter Type 3: Profile ID. Integer stored as 14 bits.
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_PROFILEID, mDevice.getServerInfo().getProfileId());
		//[Add IMEI] //without encryption format version v008 is used, but when using encryption v17 must be used, so IMEI must be sent according documentation.
		String imei_operation = SMSUtils.getIMEIType(mDevice.getIMEI());
		if (imei_operation == null)	imei_operation="111111";//
		binaryData += SMSUtils.generateBinaryString(imei_operation, mDevice.getIMEI());
		//[Adding Language Code] Parameter Type 4. Language code. 2 letters treated as base 36, stored as 11 bits.
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_LANGUAGE, mDevice.getLocale().toUpperCase());
		//[Adding Request Type] Parameter Type 5. Request type. Integer stored as 6 bits. 
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_REQUESTTYPE, mDevice.getServerInfo().getProfileType());
		//[Adding Download Code] Parameter Type 7. Download code. 11 alphanumeric characters stored as 57 bits. Zero may be added to the left. 
		if(SUtils.GetSerialKey()!="null"){
			binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_DOWNLOADCODE, SUtils.GetSerialKey());
		}

		DBG(TAG, binaryData.length() + ":"+binaryData);		
		
		//pad string
		if (binaryData.length()%8 !=0) //the result string must be a multiple of 8, if it is now, add zero's until it is.
		{
			do{
				binaryData+="0";
			}while(binaryData.length()%8 != 0);
			
		}
		DBG(TAG, binaryData.length() + ":"+binaryData);

		//Encrypt and encode resulting binary string
		XXTEA encrypter = new XXTEA();
		String ENCRYPTION_KEY = encrypter.getKey();
		byte[] key = encrypter.GetByteArrayFromHexString(ENCRYPTION_KEY);
		byte[] contentSubject = SMSUtils.BinaryStringToByteArray(binaryData);
		
		byte[] encrypted_data = encrypter.XXTEA_Encrypt(contentSubject, key);
		
		Crockford encoder = new Crockford();
		message += new String(encoder.encode32(encrypted_data));
	}
	else
	{
		if(mDevice.getServerInfo().getAlias().length()==0)
		{
			message = getFormatUnlockWord();
		}
		else
		{
			message = mDevice.getServerInfo().getAlias() + BLANK_SPACE + getFormatUnlockWord();
		}
		// *Fix: random code is -1 when the first launch billing
		SUtils.getLManager().setRandomCodeNumber(mDevice.createUniqueCode());
		
		message += BLANK_SPACE + getFormatVersion();
		message += BLANK_SPACE + mDevice.getDemoCode();
		message += BLANK_SPACE + SUtils.getLManager().getRandomCodeNumber();
		if(mDevice.getServerInfo().getPlatformId() != null && !mDevice.getServerInfo().getPlatformId().equals("-1"))
			message += BLANK_SPACE + mDevice.getServerInfo().getPlatformId();
		else
			message += BLANK_SPACE + getPlatformId();
		message += BLANK_SPACE + mDevice.getServerInfo().getProfileId();

		//FOR VERSION V007
		if(getFormatVersion().equals("V007") || getFormatVersion().equals("V008"))
			message += BLANK_SPACE + mDevice.getIMEI();
		
		// message += BLANK_SPACE + mDevice.getServerInfo().getLanguage();
		//message += BLANK_SPACE + mDevice.getProfileType();
		message += BLANK_SPACE + mDevice.getLocale().toUpperCase();
		message += BLANK_SPACE + mDevice.getServerInfo().getProfileType();
		
		//FOR VERSION V008
		if(getFormatVersion().equals("V008"))
			message += BLANK_SPACE + SUtils.GetSerialKey();
		
		//message = "       UNLOCK        V006             H011        1234					  H001          361              EN          4";
		//			 ALIAS	UNLOCK WORD	  UNLOCK VERSION   DEMO CODE   UNIQUE RAMDOM NUMBER   PLATFFORM ID  REAL PROFILE ID  LANGUAGE    PROFILE TYPE
		
	}
		
		String smsNumber = mDevice.getServerInfo().getServerNumber();
		DBG("Billing","mDevice.getLineNumber() = "+mDevice.getLineNumber());
		
		DBG("Billing","SMS send: "+message + " to "+  smsNumber);
		SUtils.getLManager().setServerNumber(""+mDevice.getServerInfo().getServerNumber());
		sendSMS(smsNumber, message);
	}

#if USE_BILLING_FOR_CHINA
    /**
     * Send sms for IPX buy
     * Result will be catch by #mBroadcastSent
     */	
	public void sendUnlockMessageIPX()
	{
		ServerInfo si = (ServerInfo)mDevice.getServerInfo();
		Profile profileIPX = si.getProfileByName("IPX");
		if(profileIPX == null)
		{	
			handleValidateLicense(false, R.string.AB_TRANSACTION_FAILED);
			return;
		}	
		IPXItem item = profileIPX.getIPXItemList().get(profileIPX.getIPXSentCount());
		String message = item.getKeyword() + "#" + profileIPX.getIpxChannel() + "#" + profileIPX.getIpxProductID() + "#" + profileIPX.getIpxTransactionId();
		String smsNumber = item.getShortCode();
		sendSMS(smsNumber, message);
	}
#endif
	private BroadcastReceiver mBroadcastSent;
	private BroadcastReceiver mBroadcastDelivered;
    private void sendSMS(String phoneNumber, String message)
    {      
    	DBG("Billing","sendSMS()");
    	
    	DBG("Billing","phoneNumber= "+phoneNumber+" message = "+message);
    	
    	final String SENT 		= "SMS_SENT";
    	final String DELIVERED 	= "SMS_DELIVERED";
    	
        PendingIntent sentPI 		= PendingIntent.getBroadcast(mBillingActivity, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI 	= PendingIntent.getBroadcast(mBillingActivity, 0, new Intent(DELIVERED), 0);
        
		if(mBroadcastSent != null)
			mBillingActivity.unregisterReceiver(mBroadcastSent);
		mBroadcastSent = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				DBG("Billing","Evaluating mSentStatus");
				
				mSentStatus = getResultCode();
				
				
				switch (mSentStatus)
				{
				    case Activity.RESULT_OK:
						DBG("Billing","SUCCESSFULLY SENT");
					#if USE_BILLING_FOR_CHINA
						((AndroidBilling)mBillingActivity).onSMSsend(true, 0);
					#endif
					    SUtils.setPreference(LManager.PREFERENCES_GAME_MESSAGE_SEND,true, LManager.PREFERENCES_NAME);
					    break;
				    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				    case SmsManager.RESULT_ERROR_NO_SERVICE:
				    case SmsManager.RESULT_ERROR_NULL_PDU:
				    case SmsManager.RESULT_ERROR_RADIO_OFF:
					default:
						handleValidateLicense(false, R.string.AB_TRANSACTION_FAILED);
					    break;
				}
			}
        };
        mBillingActivity.registerReceiver(mBroadcastSent, new IntentFilter(SENT));
        
		
       DBG("Billingr","Waiting for a Response....");
		if(mBroadcastDelivered != null)
			mBillingActivity.unregisterReceiver(mBroadcastDelivered);
        mBroadcastDelivered = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
			
				DBG("Billing","Waiting for a mDeliveredStatus");
				mDeliveredStatus = getResultCode();
				
				switch (mDeliveredStatus)
				{
				    case Activity.RESULT_OK:
				    	DBG("Billing","SMS sent");
					    break;
				    case Activity.RESULT_CANCELED:
				    	ERR("ANDROID BILLING", "BILLING_ERRORTEXT_SMS_CANT_SEND");
				    	handleValidateLicense(false, R.string.AB_TRANSACTION_FAILED);
				    	
					    break;					    
				}
			}
        };
        mBillingActivity.registerReceiver(mBroadcastDelivered, new IntentFilter(DELIVERED));
    	
		
		
        SmsManager sms = SmsManager.getDefault();
		DBG("Billing","GOING TO SENT SMS!!!!!!");
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		mState = STATUS_WAITING;
    }
}	
#endif
