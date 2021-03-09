#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.util.Log;

import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

import android.app.Dialog;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.os.Bundle;
import java.lang.reflect.Method;
import android.telephony.TelephonyManager;
import android.content.Context;

public class SKTHelper extends IABTransaction 
{
	SET_TAG("InAppBilling");

	private static String mCurrentId = null;
    public SKTHelper(ServerInfo si) {
        super(si);
    }
	static boolean result;
	static boolean finishLaunch;
    @Override
    public boolean requestTransaction(final String id) {
        mCurrentId = id;
        final String appId = mServerInfo.getAIDBilling();
        final String productId = mServerInfo.getPIDBilling();
		result = true;
		finishLaunch = false;
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				
				//Custom dialog.
				final Dialog dialog = new Dialog(((Activity)SUtils.getContext()));
				dialog.setContentView(R.layout.iab_skt_dialog);
				dialog.setCancelable(false);
				dialog.setTitle(((Activity)SUtils.getContext()).getString(R.string.IAB_SKT_CONFIRM_DIALOG_TITLE));
				//  item name
				TextView textViewName = (TextView) dialog.findViewById(R.id.textView_item_name);
				String name = ((Activity)SUtils.getContext()).getString(R.string.IAB_SKT_CONFIRM_DIALOG_ITEM_NAME).replace("{NAME}", mServerInfo.getItemAttribute(id,"name"));
				textViewName.setText(name);
				//  item price
				TextView textViewPrice = (TextView) dialog.findViewById(R.id.textView_item_price);
				String price = ((Activity)SUtils.getContext()).getString(R.string.IAB_SKT_CONFIRM_DIALOG_ITEM_PRICE).replace("{PRICE}", mServerInfo.getItemPriceFmt());
				textViewPrice.setText(price);

				//  add listener
				Button buttonOK = (Button) dialog.findViewById(R.id.buttonYES);
				buttonOK.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						result = SktIabActivity.LaunchSKTBilling(id, appId, productId, mServerInfo);
						finishLaunch = true;
					}
				});
				
				Button buttonCancel = (Button) dialog.findViewById(R.id.buttonNO);
				buttonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
							dialog.dismiss();
							result = false;
							finishLaunch = true;
							  
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
												
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);
																	
																	
							try{
								Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
								Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
								bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
							}
							catch(Exception ex )
							{
								ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
							}
						}
					});

				dialog.show();
  			}
  		});	
		int stepts = 10000;
		while (!finishLaunch && --stepts > 0)
		{
			try {
					Thread.sleep(1);
			} catch (Exception exc) {}
		}
		return (result && stepts > 0);
    }

    @Override
    public void showDialog(int idx, String id) {}

    @Override
    public boolean restoreTransactions()
    {
        INFO(TAG, "Restore Transactions started");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Restore Transactions started");
        final String appId = mServerInfo.getAIDBilling();
        final String productId = mServerInfo.getPIDBilling();
		
		if(appId != null && productId != null && IsValidSIM())
		{
			((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
			{
				public void run ()
				{
					SktIabActivity.LaunchSKTRestore(null, appId, productId, mServerInfo);
				}
			});
		}
		else
		{
			if(!IsValidSIM())
			{
				ERR(TAG, "***** Restore Transactions Failed: SIM card is missing *****");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "***** Restore Transactions Failed: SIM card is missing *****");
			}	
			else
			{
				ERR(TAG, "***** Restore Transactions Failed: Profile is missing *****");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "***** Restore Transactions Failed: Profile is missing *****");
			}	
			
			//Informing the game that the restore transactions process has failed.
			Bundle bundle = new Bundle();
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
			try{
				Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
				Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
				bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
			}catch(Exception ex ) {
				ERR(TAG,"Error invoking reflex method "+ex.getMessage());
			}
		}
			
       return true;
    }
	
	private boolean IsValidSIM()
	{
		TelephonyManager mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);

			if(mDeviceInfo.getPhoneType() == mDeviceInfo.PHONE_TYPE_CDMA)
				return (true);
			else
				return (mDeviceInfo.getSimState() == mDeviceInfo.SIM_STATE_READY);		
	}
    
    public static String GetItemId()
    {
      return (mCurrentId);
    }

}
#endif
