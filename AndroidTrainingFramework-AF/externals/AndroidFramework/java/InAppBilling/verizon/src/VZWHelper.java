#if USE_IN_APP_BILLING && VZW_STORE
package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Method;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;


public class VZWHelper extends IABTransaction
{
	SET_TAG("InAppBilling");

	private static String mCurrentId = null;
	private static boolean isTransactionPending;
	
	public static final int DIALOG_PURCHASE_SUCCESS			 	= 1;
	public static final int DIALOG_PURCHASE_FAIL				= 2;
	
	private static AServerInfo mServerInfo;
	private static Device mDevice;
	static XPlayer mXPlayer;
	static VZWHelper mThis;

	VZWHelper(AServerInfo si)
	{
		super(si);
		mDevice = new Device(si);
		mXPlayer = new XPlayer(mDevice);
		mServerInfo = si;
		mThis = this;
	}
	
	static AServerInfo getServerInfo()
	{
		return mThis.mServerInfo;
	}
	
	@Override
	public void sendConfirmation()
	{
		DBG(TAG, "[sendConfirmation]");
		VZWIABActivity.sendConfirmation();
	}
	
	@Override
	public boolean requestTransaction(String id) 
	{
		DBG(TAG, "[requestTransaction]");
		#if VZW_SCM	
		if(!isTransactionPending)
		#endif
		{
			mCurrentId = id;
			VZWIABActivity.LaunchVZWBilling(getServerInfo());
		}
		return(true);
	}
	
	public static String GetContentId()
	{
		return (mCurrentId);
	}
	
	static String GetItemName()
	{
		return(getServerInfo().getItemAttribute(GetContentId(), "name"));
	}
	
	static String GetItemDescription()
	{
		return(getServerInfo().getItemAttribute(GetContentId(), "description"));
	}
	
	static String GetItemPrice()
	{
		String price = null;
		cBilling tmpBilling = InAppBilling.mServerInfo.getItemById(GetContentId()).getDefaultBilling();
		
		if (tmpBilling != null)
			price = tmpBilling.getAttributeByName("formatted_price");
		
		if(price != null && !price.equals(""))
          	return (price);
		else
			return (getServerInfo().getItemPriceFmt());
	}
	
	public static String GetGamePrice()
	{
		return(getServerInfo().getGamePrice());
	}
	
	public static String GetOfferType()
	{
		return(getServerInfo().getItemAttribute(GetContentId(), "offer_type"));
	}
	
	public static String GetItemId()
	{
		return(getServerInfo().getItemAttribute(GetContentId(), "item_id"));
	}
	
	static String GetItemPPP()
	{
		return(getServerInfo().getItemAttribute(GetContentId(), "ppp_id"));
	}

#if VZW_SCM	
	public static void ProcessTransactionSCM()
	{
		DBG(TAG, "[ProcessTransactionSCM]");
		new Thread(new Runnable() 
		{
			public void run() 
			{
				DBG(TAG, "*************************************");
				isTransactionPending = true;
				SCMTransaction vt = new SCMTransaction(mXPlayer);
				String scm_id = GetItemId();
				String pppID = GetItemPPP();
				String price = getServerInfo().getGamePrice();
				vt.processTransaction(scm_id, pppID, price);
				isTransactionPending = false;
				DBG(TAG, "SCMTransaction Status = "+vt.getStatus());
				DBG(TAG, "*************************************");
				
				if (vt.getStatus() != SCMTransaction.STATUS_SUCCESS)
					myshowDialog(DIALOG_PURCHASE_FAIL);
				else
					IABResultCallBack(IAB_BUY_OK);
			}
		}).start();	
	}
#endif

	public static void IABResultCallBack(int IABResult)
	{
		DBG(TAG, "[IABResultCallBack]");
		Bundle bundle = new Bundle();
		bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
								
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetContentId()!=null)?GetContentId().getBytes():null);
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(GetContentId()!=null)?GetContentId().getBytes():null);//trash value
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
								
		bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
		bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IABResult);
		
		try
		{
			Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
			Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
			bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
		}
		catch(Exception ex )
		{
			ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
		}
		
		if(IABResult == IAB_BUY_OK)
			mThis.sendConfirmation();
			
#if VZW_SCM
		SCMTransaction.isRetryConnection = false;
		if(IABResult == IAB_BUY_FAIL)
			SCMTransaction.enableWiFiConnection();
#endif	
			
	}
	
	public static void myshowDialog(final int idx) 
	{
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				try
				{
					DBG(TAG,"ShowDialog" + idx);
					
					Dialog dialog = null;
					DialogInterface.OnClickListener FAIL = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							IABResultCallBack(IAB_BUY_FAIL);
						}
					};
					DialogInterface.OnClickListener OK = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							IABResultCallBack(IAB_BUY_OK);
						}
					};
					
					switch (idx) 
					{
						case DIALOG_PURCHASE_SUCCESS:
								dialog = createDialogSUCCESS(R.string.IAB_PURCHASE_ITEM_SUCCESS_TITLE, R.string.IAB_PURCHASE_ITEM_SUCCESS, OK);
						break;
						case DIALOG_PURCHASE_FAIL:
							#if VZW_SCM
							if(SCMTransaction.mStatus == SCMTransaction.STATUS_ERROR_NO_DATA_CONNECTION)
								dialog = createPurchaseFailDialog(R.string.IAB_NETWORK_ERROR_TITLE, R.string.IAB_NETWORK_ERROR_NO_DATA_CONNECTION, FAIL);
							else
							#endif
								dialog = createPurchaseFailDialog(R.string.IAB_PURCHASE_ITEM_FAILURE_TITLE, R.string.IAB_PURCHASE_ITEM_FAILURE, FAIL);
						break;
					}
					
					if (dialog != null)
					{
						dialog.setOwnerActivity((Activity)SUtils.getContext());
						dialog.show();
					}	
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
				}
			}
		});	
	}
	
	static private Dialog createDialogSUCCESS(int titleId, int messageId, DialogInterface.OnClickListener dil) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		
			builder.setTitle(titleId)
			.setIcon(R.drawable.iconiab)
			.setMessage(messageId)
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					IABResultCallBack(IAB_BUY_OK);
				}
			})
			.setPositiveButton(R.string.IAB_SKB_OK, dil);
		return builder.create();
	}
	
	static private Dialog createPurchaseFailDialog(int titleId, int messageId, DialogInterface.OnClickListener dil) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());

			builder.setTitle(titleId)
			.setIcon(R.drawable.iconiab)
			.setMessage(messageId)
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					IABResultCallBack(IAB_BUY_FAIL);
				}
			})
			.setPositiveButton(R.string.IAB_SKB_RETRY, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					new Thread(new Runnable() {
						public void run() {
						#if VZW_SCM
							SCMTransaction.isRetryConnection = true;
                			ProcessTransactionSCM();
						#endif
						}
						}).start();
					}
				});
        	builder.setNegativeButton(R.string.IAB_SKB_CANCEL, dil);
		return builder.create();
	}
	
	@Override
	public void showDialog(final int idx, final String id) {}
}
#endif
