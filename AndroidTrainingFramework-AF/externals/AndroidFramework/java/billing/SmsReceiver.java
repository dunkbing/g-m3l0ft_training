package APP_PACKAGE.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import APP_PACKAGE.R;
import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.GLUtils.Carrier;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Device;

public class SmsReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		
			SUtils.setContext(context);
		
		DBG("Billing","SmsReceiver onReceive");
        
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        //String str = "";
        String Unlock_Msg = null;
        if (bundle != null)
        {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];      
			
           DBG("Billing","SMS DEBUG - Messages ="+msgs.length);
            
            boolean wasValidCode = false;
            
            for (int i=0; i<msgs.length; i++)
            {
            	DBG("Billing","SMS DEBUG - Analizyng message number ="+i);
            	
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            	
                DBG("Billing","SMS DEBUG - Analizyng remitent number = "+msgs[i].getOriginatingAddress());
                //if(msgs[i].getOriginatingAddress().equals(Carrier.getSMSNumber()))
                /*if(
                	(msgs[i].getOriginatingAddress().equals(SUtils.getLManager().getServerNumber()))
                 || (msgs[i].getServiceCenterAddress().equals(SUtils.getLManager().getServerNumber()))
                 //|| (msgs[i].getOriginatingAddress().equalsIgnoreCase("PIXOFUN"))
                  )*/

                	Unlock_Msg = msgs[i].getMessageBody().toString();
                	DBG("Billing","SMS DEBUG - Message from server number found, Server Number is ="+SUtils.getLManager().getServerNumber());
                	DBG("Billing","SMS DEBUG - Unlock_Msg retracted from this item ("+i+") is: "+Unlock_Msg);

                if(Device.CHECK_SMS_SERVER_ASNWER)
                {
	                if(msgs[i].getOriginatingAddress().equals(SUtils.getLManager().getServerNumber()))
	                {
	                    if (SUtils.getLManager().readUnlockCode(Unlock_Msg))
	            		{
	                       	DBG("Billing","VALID CODE!!!");
	           				SUtils.setPreference(LManager.PREFERENCES_GAME_MESSAGE_SEND,false, LManager.PREFERENCES_NAME);
	           				try
	           				{
	           				SMS.handleValidateLicense(true, R.string.AB_THANKS_FOR_THE_PURCHASE);
	           				SUtils.getLManager().TrackingPurchaseSuccess(1);
	           				wasValidCode=true;
	           				}
	           				catch(Exception vex)
	           				{
	           					DBG("Billing","APP not running but VALID CODE FOUND!!!");
	           				}
	            		}
	                }
            	}
                else
                {
                        if (SUtils.getLManager().readUnlockCode(Unlock_Msg))
                		{
                           	DBG("Billing","VALID CODE!!!");
           					SUtils.setPreference(LManager.PREFERENCES_GAME_MESSAGE_SEND,false, LManager.PREFERENCES_NAME);
               				try
               				{
               				SMS.handleValidateLicense(true, R.string.AB_THANKS_FOR_THE_PURCHASE);
               				SUtils.getLManager().TrackingPurchaseSuccess(1);
               				wasValidCode=true;
               				}
               				catch(Exception vex)
               				{
               					DBG("Billing","APP not running but VALID CODE FOUND!!!");
               				}
                		}
                }
            }
			
            //TO UPDATE READ FROM MESSAGE
            //check for valid response
            /*if (SUtils.getLManager().readUnlockCode(Unlock_Msg))
    		{
               	DBG("Billing","VALID CODE!!!");
   				SUtils.getLManager().setPreference(LManager.PREFERENCES_GAME_MESSAGE_SEND,false);
   				try
   				{
   				SMS.handleValidateLicense(true, R.string.AB_THANKS_FOR_THE_PURCHASE);
   				SUtils.getLManager().TrackingPurchaseSuccess(1);
   				}
   				catch(Exception vex)
   				{
   					DBG("Billing","APP not running but VALID CODE FOUND!!!");
   				}
    		}
            else*/
            if(!Device.USE_3_MINUTES_SCAN)
            {
	            if(!wasValidCode)
	            {
	            	DBG("Billing","ERROR INVALID CODE!!!");
	   				try
	   				{
	   	            	SMS.handleValidateLicense(false, R.string.AB_TRANSACTION_FAILED);
	   	            	//SUtils.getLManager().TrackingPurchaseFailed(1);
	   				}
	   				catch(Exception fex)
	   				{
	   					DBG("Billing","APP not running and INVALID CODE FOUND!!!");
	   				}
	
	            }
            }
        }
        else
        {
        	DBG("Billing","bundle==null");
        }
	}
}