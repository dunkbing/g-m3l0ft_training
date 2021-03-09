#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Build;
import android.telephony.SmsManager;

import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.R;

import APP_PACKAGE.billing.common.XXTEA;
import APP_PACKAGE.billing.common.Crockford;
import APP_PACKAGE.billing.common.SMSUtils;

public class SMS {
	
SET_TAG("InAppBilling");
	
	public final static int STATUS_INIT 		= 0;
	public final static int STATUS_WAITING 		= 1;
	public final static int STATUS_COMPLETE		= 2;
	public final static int STATUS_FAIL			= 3;
	
	private static int mState					= STATUS_INIT;
	private static int mSentStatus				= STATUS_INIT;
	private static int mDeliveredStatus			= STATUS_INIT;
	
	
	private static Device	mDevice;
	private static int mMessageID;
	
	
//#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
	private final String         TYPE_GAME	             			= "12";
	
	private final String         SMS_FORMAT_INAPP_WORD              = "INAPP";
	private final String         SMS_FORMAT_UNLOCK_WORD             = "UNLOCK";
	
	private final String         PSMS_FORMAT_VERSION           		= "V012";
	private final String         SMS_FORMAT_VERSION                  = "V010";
	static String				 PSMS_TID;

//#endif//USE_MTK_SHOP_BUILD
	
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
	private String getFormatUnlockWord(String type)
	#else
	private String getFormatUnlockWord() 
	#endif
	{
		#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
		return (type.equals(TYPE_GAME)?SMS_FORMAT_UNLOCK_WORD:SMS_FORMAT_INAPP_WORD);
		#else
		return SMS_FORMAT_UNLOCK_WORD;
		#endif
	}

	public String getFormatVersion()
	{
		if(GLOFTHelper.getServerInfo().getBillingType().equals(InAppBilling.GET_STR_CONST(IAB_PSMS)))
			return PSMS_FORMAT_VERSION;
		else
			return SMS_FORMAT_VERSION;
	}
    
	public SMS(Device device)
	{
		mDevice 			= device;
				
		
		mState				= STATUS_INIT;
		mSentStatus			= STATUS_INIT;
		mDeliveredStatus	= STATUS_INIT;
	}
	
	public void release()
	{
	
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
		((Activity)SUtils.getContext()).unregisterReceiver(mBroadcastDelivered);
		((Activity)SUtils.getContext()).unregisterReceiver(mBroadcastSent);
#endif
		mBroadcastDelivered = null;
		mBroadcastSent		= null;
		
		mDevice 			= null;
		
		mState				= STATUS_INIT;
		mSentStatus			= STATUS_INIT;
		mDeliveredStatus	= STATUS_INIT;
		mMessageID			= 0;
	}
	
	public boolean isCompleted()
	{
		return (mState == STATUS_COMPLETE);
	}
	public boolean isFail()
	{
		return (mState == STATUS_FAIL);
	}
	
	public int getMessageID()
	{
		return mMessageID;
	}
	
	public static String getServerNumber()
	{
		return ""+GLOFTHelper.getServerInfo().getServerNumber();
	}
	
	public void SendPurchaseMessage(String mContentID, String FM_SMS_ID)
	{
	
		String message;
		String BLANK_SPACE = " ";
		#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
		PSMS_TID=FM_SMS_ID;
		#endif

		
		if(GLOFTHelper.getServerInfo().getAlias().length()==0)
		{
			//Adding [ALIAS]
		#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
			message = getFormatUnlockWord(GLOFTHelper.getServerInfo().getPSMSType());
		#else		
			message = getFormatUnlockWord();
		#endif
		}
		else
		{
			//Adding [INAPP]
		#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
			message = GLOFTHelper.getServerInfo().getAlias() + BLANK_SPACE + getFormatUnlockWord(GLOFTHelper.getServerInfo().getPSMSType());
		#else
			message = GLOFTHelper.getServerInfo().getAlias() + BLANK_SPACE + getFormatUnlockWord();
		#endif
		}
		
		//Adding [VERSION]
		message += BLANK_SPACE + getFormatVersion();
		//Adding [IGP_CODE]
		message += BLANK_SPACE + mDevice.getDemoCode();
		//Adding [GENERATED_CODE]
		message += BLANK_SPACE + SUtils.getLManager().getRandomCodeNumber();
		//Adding [PLATFORM_ID]
		message += BLANK_SPACE + GLOFTHelper.getServerInfo().getPlatformId();
		//Adding [PROFILE_ID]
		message += BLANK_SPACE + GLOFTHelper.getServerInfo().getProfileId();
		//Adding [LANGUAGE_ID]
		message += BLANK_SPACE + GLOFTHelper.getServerInfo().getLangId();
		//Adding [TYPE]
		message += BLANK_SPACE + GLOFTHelper.getServerInfo().getPSMSType();
		//Adding [CONTENT_ID]
		message += BLANK_SPACE +""+mContentID;
		//Adding [IMEI]
		message += BLANK_SPACE + mDevice.getIMEI();
		//Adding [FM_SMS_ID]
		message += BLANK_SPACE + FM_SMS_ID;
		//Adding [DOWNLOAD_CODE]
		if(SUtils.GetSerialKey()!=null)
			message += BLANK_SPACE + SUtils.GetSerialKey();
			
		
		
		INFO(TAG,"SMS send: "+message + " to "+  GLOFTHelper.getServerInfo().getServerNumber());
		SUtils.getLManager().setServerNumber(""+GLOFTHelper.getServerInfo().getServerNumber());
		sendSMS(GLOFTHelper.getServerInfo().getServerNumber(), message);
	}
	
	public void SendPurchaseMessageEncrypted(String mContentID, String FM_SMS_ID)
	{
		String message = "";
		String BLANK_SPACE = " ";
		#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
		PSMS_TID=FM_SMS_ID;
		#endif
		String binaryData= SMSUtils.SMS_ENCRYPTED_HEADER;


		//Adding [ALIAS]
		if(GLOFTHelper.getServerInfo().getAlias().length()!=0) //There Alias Information in profile received. must add it to message
		{
		#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
			message = GLOFTHelper.getServerInfo().getAlias() + BLANK_SPACE;// + getFormatUnlockWord(GLOFTHelper.getServerInfo().getPSMSType());
		#else
			String operation = getFormatUnlockWord();
		#endif
		}

		message += SMSUtils.SMS_TITLE_MESSAGE + BLANK_SPACE;// add text: GameloftOrder HQ request to make message more friendly to user.
		//Adding [INAPP]
		#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
		String operation = getFormatUnlockWord(GLOFTHelper.getServerInfo().getPSMSType());
		#else
		String operation = getFormatUnlockWord();
		#endif

		if (operation == "INAPP")
			binaryData += SMSUtils.SMS_ENCRYPTED_HEADER_INAPP;
		else if(operation == "UNLOCK")
			binaryData += SMSUtils.SMS_ENCRYPTED_HEADER_UNLOCK;

		
		//Adding [VERSION]
		//message += BLANK_SPACE + getFormatVersion();
		//Adding [IGP_CODE]
		//message += BLANK_SPACE + mDevice.getDemoCode();
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_GAMECODE, mDevice.getDemoCode());
		//Adding [GENERATED_CODE]
		//message += BLANK_SPACE + SUtils.getLManager().getRandomCodeNumber();
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_RANDOMCODE, String.valueOf(SUtils.getLManager().getRandomCodeNumber()));
		//Adding [PLATFORM_ID]
		//message += BLANK_SPACE + GLOFTHelper.getServerInfo().getPlatformId();
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_PLATFORMID, GLOFTHelper.getServerInfo().getPlatformId());
		//Adding [PROFILE_ID]
		//message += BLANK_SPACE + GLOFTHelper.getServerInfo().getProfileId();
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_PROFILEID, GLOFTHelper.getServerInfo().getProfileId());
		//Adding [LANGUAGE_ID]
		//message += BLANK_SPACE + GLOFTHelper.getServerInfo().getLangId();
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_LANGUAGE, GLOFTHelper.getServerInfo().getLangId());
		//Adding [TYPE]
		//message += BLANK_SPACE + GLOFTHelper.getServerInfo().getPSMSType();
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_REQUESTTYPE, GLOFTHelper.getServerInfo().getPSMSType());
		//Adding [CONTENT_ID]
		//message += BLANK_SPACE +""+mContentID;
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_CONTENTID, mContentID);
		//Adding [IMEI]
		//message += BLANK_SPACE + mDevice.getIMEI();
		String imei_operation = SMSUtils.getIMEIType(mDevice.getIMEI());
		if (imei_operation == null)	imei_operation="111111";//
		binaryData += SMSUtils.generateBinaryString(imei_operation, mDevice.getIMEI());
		//Adding [FM_SMS_ID]
		//message += BLANK_SPACE + FM_SMS_ID;
		binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_FMSMSID, FM_SMS_ID);
		//Adding [DOWNLOAD_CODE]
		if(SUtils.GetSerialKey()!="null"){
			//message += BLANK_SPACE + SUtils.GetSerialKey();
			binaryData += SMSUtils.generateBinaryString(SMSUtils.ENCRYPTION_DOWNLOADCODE, SUtils.GetSerialKey());
		}
			
		DBG(TAG, binaryData.length() + ":"+binaryData);
		
		//pad string
		if (binaryData.length()%8 !=0)
		{
			do{
				binaryData+="0";
			}while(binaryData.length()%8 != 0);
			
		}
		DBG(TAG, binaryData.length() + ":"+binaryData);
		
		//Encrypt
		XXTEA encrypter = new XXTEA();
		String ENCRYPTION_KEY = encrypter.getKey();
		byte[] key = encrypter.GetByteArrayFromHexString(ENCRYPTION_KEY);
		byte[] contentSubject = SMSUtils.BinaryStringToByteArray(binaryData);
		
		byte[] encrypted_data = encrypter.XXTEA_Encrypt(contentSubject, key);
		
		Crockford encoder = new Crockford();
		message += new String(encoder.encode32(encrypted_data));
		
		INFO(TAG,"SMS send: "+message + " to "+  GLOFTHelper.getServerInfo().getServerNumber());
		SUtils.getLManager().setServerNumber(""+GLOFTHelper.getServerInfo().getServerNumber());
		sendSMS(GLOFTHelper.getServerInfo().getServerNumber(), message);
	}

	
	private BroadcastReceiver mBroadcastSent;
	private BroadcastReceiver mBroadcastDelivered;
    private void sendSMS(String phoneNumber, String message)
    {      
    	INFO(TAG,"SMS sendSMS()");
    	
    	final String SENT 		= "SMS_SENT";
    	final String DELIVERED 	= "SMS_DELIVERED";
    	
        PendingIntent sentPI 		= PendingIntent.getBroadcast(((Activity)SUtils.getContext()), 0, new Intent(SENT), 0);
        PendingIntent deliveredPI 	= PendingIntent.getBroadcast(((Activity)SUtils.getContext()), 0, new Intent(DELIVERED), 0);
        
//#if USE_MTK_SHOP_BUILD
		try
		{
			INFO(TAG,"SMS Setting mBroadcastSent ON");
			mBroadcastSent = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					INFO(TAG,"SMS Evaluating mSentStatus");
					mSentStatus = getResultCode();
					switch (mSentStatus)
					{
						case Activity.RESULT_OK:
							INFO(TAG,"SMS RESULT_OK");
							mState=STATUS_COMPLETE;
							//SUtils.setPreference(LManager.PREFERENCES_GAME_MESSAGE_SEND,true, LManager.PREFERENCES_NAME);
						break;
						case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						case SmsManager.RESULT_ERROR_NO_SERVICE:
						case SmsManager.RESULT_ERROR_NULL_PDU:
						case SmsManager.RESULT_ERROR_RADIO_OFF:
						default:
							mState=STATUS_FAIL;
						break;
					}
					INFO(TAG,"SMS Evaluating mSentStatus="+mSentStatus);
				}
			};
			((Activity)SUtils.getContext()).registerReceiver(mBroadcastSent, new IntentFilter(SENT));
			INFO(TAG,"SMS mBroadcastSent REGISTRED");
        }
		catch(Exception mBroadcastSentEX){ERR(TAG,"SMS mBroadcastSent ERROR: "+mBroadcastSentEX);}
		
        /*try
		{
			INFO(TAG,"SMS Setting mBroadcastDelivered ON");
			mBroadcastDelivered = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
				{
					INFO(TAG,"SMS Waiting for a mDeliveredStatus");
					mDeliveredStatus = getResultCode();
					
					switch (mDeliveredStatus)
					{
						case Activity.RESULT_OK:
							INFO(TAG,"SMS SMS sent");
							break;
						case Activity.RESULT_CANCELED:
							ERR(TAG, "SMS BILLING_ERRORTEXT_SMS_CANT_SEND");
							break;					    
					}
				}
			};
			((Activity)SUtils.getContext()).registerReceiver(mBroadcastDelivered, new IntentFilter(DELIVERED));
			INFO(TAG,"SMS mBroadcastDelivered REGISTRED");
		}
		catch(Exception mBroadcastDeliveredEX){ERR(TAG,"SMS mBroadcastDelivered ERROR: "+mBroadcastDeliveredEX);}
		*/
//#endif    	
		
		
        SmsManager sms = SmsManager.getDefault();
        INFO(TAG,"SMS GOING TO SENT SMS!!!!!! "+phoneNumber);
		
		mState = STATUS_INIT;
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
	


	public void SendUMPR3PurchaseMessage(String mContentID, String FM_SMS_ID, int target)
	{
		INFO(TAG,"SMS send: "+XPlayer.getUMPMO1() + " to "+  GLOFTHelper.getServerInfo().getShortCode(1)+" target="+target);

		
		if(target==1)
			sendSMS(GLOFTHelper.getServerInfo().getShortCode(1), XPlayer.getUMPMO1());
		else if(target==2)
			sendSMS(GLOFTHelper.getServerInfo().getShortCode(2), XPlayer.getUMPMO2());
		else
			INFO(TAG,"SMS WRONG TARGET SET");

		mState = STATUS_INIT;			
	}	
	
}	
#endif
