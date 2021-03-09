#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
package APP_PACKAGE.iab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.database.Cursor;
import android.content.ContentValues;
import android.net.Uri;

import APP_PACKAGE.R;
import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.billing.common.StringEncrypter;
import APP_PACKAGE.GLUtils.Carrier;
import APP_PACKAGE.GLUtils.Config;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Device;

//get encrypted string
#define GES(id)		StringEncrypter.getString(id)
//get raw string
#define GRS(id)		SUtils.getContext().getString(id)

public class SmsReceiver extends BroadcastReceiver
{
	SET_TAG("InAppBilling");
	private static final String BS 		= "X/UdzsidlhgyU1XvCAmFlFD1ENkvNoVcrGXq7CvZ1Oo=";
    private static final String BF 		= "wX4NLAtn5Jp0o/qCUQHvXVD1ENkvNoVcrGXq7CvZ1Oo=";
    public static final String			PREFERENCES_NAME 						= Config.GGC+"BInfo";
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
	private final String PREFERENCES_SMS_RESULT			= "PREFERENCES_MTKMT";
#endif
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		
		//this stops notifications to others
		this.abortBroadcast();
		if (SUtils.getContext() == null)
		{
			SUtils.setContext(context);
		}	
		
		StringEncrypter sc = new StringEncrypter(GES(IAB_STR_ENCODE_KEY), GES(IAB_STR_6_PASSES_LOAD));
		SMS.PSMS_TID = sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_ORDER_ID), GES(IAB_STR_PREF_NAME)));
		JDUMP(TAG,SMS.PSMS_TID);
		
		DBG(TAG,"SmsReceiver onReceive");
        LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "SmsReceiver onReceive");
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        //String str = "";
        String Unlock_Msg = null;
        if (bundle != null)
        {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];      
			
           	DBG(TAG,"SMS DEBUG - Messages ="+msgs.length);
            LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "SMS DEBUG - Messages ="+msgs.length);

            boolean wasValidCode = false;
            
            for (int i=0; i<msgs.length; i++)
            {
            	DBG(TAG,"SMS DEBUG - Analizyng message number ="+i);
            	
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            	
                DBG(TAG,"SMS DEBUG - Analizyng remitent number = "+msgs[i].getOriginatingAddress());
              	
				Unlock_Msg = msgs[i].getMessageBody().toString();
               	DBG(TAG,"SMS DEBUG - Unlock_Msg retracted from this item ("+i+") is: "+Unlock_Msg);
				if(SUtils.getLManager().isInAppSMS(Unlock_Msg))
               	{
					DBG(TAG,"SMS COMES FROM GLOFT");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "SMS COMES FROM GLOFT");
					if (SUtils.getLManager().readMTKUnlockCode(SMS.PSMS_TID,Unlock_Msg))
					{
						DBG(TAG,"VALID CODE FROM SMS!!!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "VALID CODE FROM SMS!");
						try
						{
							DBG(TAG,"SAVING SMS RECEPTION");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "SAVING SMS RECEPTION");
							SUtils.setPreference(PREFERENCES_SMS_RESULT, BS, PREFERENCES_NAME);
							wasValidCode=true;
						}
						catch(Exception vex){}
					}
               	}
				else
				{
					//continue the normal process of sms and will get alert and reaches inbox
					DBG(TAG,"NOT GLOFT INAPP SMS");
					this.clearAbortBroadcast();
				}
            }
        }
        else
        {
        	DBG(TAG,"bundle==null");
        	LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "bundle==null");
        }
	}

}
#endif