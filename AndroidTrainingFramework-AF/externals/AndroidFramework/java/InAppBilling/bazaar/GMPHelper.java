#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.app.Dialog;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

#if USE_GOOGLE_ANALYTICS_TRACKING
import APP_PACKAGE.utils.GoogleAnalyticsTracker;
import APP_PACKAGE.utils.GoogleAnalyticsConstants;
#endif

import APP_PACKAGE.billing.common.Base64;
import APP_PACKAGE.GLUtils.Encrypter;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;


public class GMPHelper extends IABTransaction
{
	SET_TAG("InAppBilling");

	public static final int DIALOG_CANNOT_CONNECT_ID 			= 1;
	public static final int DIALOG_BILLING_NOT_SUPPORTED_ID 	= 2;
	public static final int DIALOG_WAITTING_CONFIRMATION 		= 3;
	
	private static final String SUCCESS_RESULT = "T7WxMl1MuYnllpIJnNJtoFD1ENkvNoVcrGXq7CvZ1Oo=";
	private static final String FAILURE_RESULT = "RS4zxSt6TWQHptj38oDsSlD1ENkvNoVcrGXq7CvZ1Oo=";
	
	private static final int RESULT_STR_POS 	= 0;
	private static final int RESULT_CODE_POS 	= 1;
	private static final int SECURITY_CODE_POS 	= 2;
	private static final int ECOM_TX_ID_POS 	= 3;
	
	//Used for You already own this item error
	private String mItemToRestore = null;
	public void setItemToRestore(String value)
	{
		mItemToRestore = value;
	}
	
    public GMPHelper(ServerInfo si) 
	{
        super(si);
		mThis = this;
		
		GMPUtils utils = GMPUtils.getInstance();
		utils.setContext(((Activity)SUtils.getContext()));
		utils.startService(mStartUpCallBack);
		
    }
	
	public ServerInfo getServerInfo()
	{
		return (ServerInfo)mServerInfo;
	}
	
	private static GMPHelper mThis;
	
	public static GMPHelper getInstance()
	{
		return mThis;
	}
	
	@Override
	public void sendNotifyConfirmation(String value)
	{
		INFO(TAG, "Consuming product with Bazaar from Game");
		JDUMP(TAG, value);
		
		if (TextUtils.isEmpty(value))
		{
			return;
		}
		String Token = null;
		String OrderID = null;
		if (value.contains("|"))
		{
			String array[] = value.split("\\|");
			OrderID		= array[0]; 
			Token 	= array[1];
		}
		else
		{
			OrderID	= value;
		}
		JDUMP(TAG,Token);
		JDUMP(TAG,OrderID);
		//Consuming with Bazaar
		if (!TextUtils.isEmpty(Token))
		{
			PurchaseInfo pi = new PurchaseInfo();
			pi.mSku 		= InAppBilling.mItemID;
			pi.mToken 		= Token;
			GMPUtils utils = GMPUtils.getInstance();
			utils.launchConsumeProductRequest(((Activity)SUtils.getContext()), pi, null);
		}
		
		//Gameloft Confirmation
		sendOrderIdConfirmation(OrderID);
		
		//Clearing the pending state
		InAppBilling.saveLastItem(STATE_SUCCESS);
		InAppBilling.clear();
	}
	
	public void sendOrderIdConfirmation(final String orderId)
	{
		if (TextUtils.isEmpty(orderId))
			return;
		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();
		StringBuffer sb 				= new StringBuffer();
		
		sb.append("b=");
		sb.append(InAppBilling.GET_STR_CONST(IAB_ITEM_DELIVERED_ACTION_NAME));	sb.append("|");
		sb.append(reqHandler.encodeString(orderId));	sb.append("|");
		sb.append("5"); //portal ID for Bazaar store is 5
				
		String url = InAppBilling.GET_STR_CONST(IAB_URL_ITEM_DELIVERED);
		String qry = sb.toString();
		//String headers = getJSONHeaders(device);
		
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "Item Delivered Confirmation");
		LOGGING_APPEND_REQUEST_PAYLOAD(qry, "Item Delivered Confirmation");
		LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("Item Delivered Confirmation"));
		
		reqHandler.getInstance().doRequestByGet(url, qry, new IABCallBack()
		{
			public void runCallBack(Bundle bundle)
			{
				DBG(TAG, "[Gameloft Order Confirmation] Callback");
				int response = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
				if (response == IABRequestHandler.SUCCESS_RESULT)
				{
					//Do nothing
						String result = bundle.getString(IABRequestHandler.KEY_RESPONSE);
						JDUMP(TAG,result);

						LOGGING_APPEND_RESPONSE_PARAM(result, "Item Delivered Confirmation");
						LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("Item Delivered Confirmation"));
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Item Delivered Confirmation: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("Item Delivered Confirmation") +" seconds" );
						LOGGING_REQUEST_REMOVE_REQUEST_INFO("Item Delivered Confirmation");
				}
				else
				{
					int code = bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for Item Delivered Confirmation failed with error code: "+code );
						
					new Thread( new Runnable()
						{
							public void run()
							{
								try { Thread.sleep(5000); }catch(Exception e) {}
								sendOrderIdConfirmation(orderId);
							}
						}
					#if !RELEASE_VERSION
						,"confirm_retry"
					#endif
						).start();
				}
			
			}
		});
	}
	
    @Override
    public boolean requestTransaction(final String id) 
	{
		return LaunchActivity(OP_TRANSACTION, id);
    }

	public boolean LaunchActivity(int op, String itemId) 
	{
		try {
            Intent i = new Intent();			
            String packageName = ((Activity)SUtils.getContext()).getPackageName();
            i.setClassName(packageName, packageName + InAppBilling.GET_STR_CONST(IAB_CLASS_NAME_GMP3_IAB_ACTIVITY));
			i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			Bundle b = new Bundle();
			b.putString(GMPUtils.KEY_ITEM_ID, itemId);
			b.putInt(GMPUtils.KEY_OPERATION, op);
			i.putExtras(b);
			((Activity)SUtils.getContext()).startActivity(i);
        } catch (Exception e) {
            return false;//TODO: put error code here
        }
        return true;
    }
	
	@Override
    public void showDialog(int idx, final String itemId) 
	{
		if (idx == DIALOG_WAITTING_CONFIRMATION)
			showPendingDialog(itemId);
		else 
			showErrorDialog(idx, itemId);
	}
	
	private void showPendingDialog(final String itemId) 
	{
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				final int idstr 	= R.string.waitting_confirm;
				final int idtittle 	= R.string.pending_transaction;
				final IABDialog pending_dialog = IABDialog.createDialog(((Activity)SUtils.getContext()), IABDialog.GLD_SINGLE_BUTTON, idstr);
				pending_dialog.setTittle(idtittle);
				pending_dialog.setButton(IABDialog.BUTTON_NEUTRAL,R.string.iab_ok, new View.OnClickListener() {
					public void onClick(View v) {
						pending_dialog.dismiss();
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						byte[] emptyarray = new String().getBytes();
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(itemId!=null)?itemId.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(itemId!=null)?itemId.getBytes():emptyarray);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
						
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_PENDING);

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
				pending_dialog.show();	
			}
		});	
	}
	
    private void showErrorDialog(int idx, final String itemId) 
	{
		final int idstr = (idx == DIALOG_CANNOT_CONNECT_ID)?R.string.cannot_connect_message:R.string.billing_not_supported_message;
		final int idtittle = (idx == DIALOG_CANNOT_CONNECT_ID)?R.string.cannot_connect_title:R.string.billing_not_supported_title;
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				final IABDialog confirm_invalid_store_dialog = IABDialog.createDialog(((Activity)SUtils.getContext()), IABDialog.GLD_DOUBLE_BUTTON, idstr);
				confirm_invalid_store_dialog.setTittle(idtittle);
				confirm_invalid_store_dialog.setButton(IABDialog.BUTTON_NEGATIVE,R.string.iab_cancel, new View.OnClickListener() {
					public void onClick(View v) {
						GMPUtils utils = GMPUtils.getInstance();
						//utils.setContext(((Activity)SUtils.getContext()));
						utils.mSetupDone = false;
						utils.startService(mStartUpCallBack);
						try{Thread.sleep(100);}catch(Exception exd){}
					
						confirm_invalid_store_dialog.dismiss();
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						byte[] emptyarray = new String().getBytes();
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(itemId!=null)?itemId.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(itemId!=null)?itemId.getBytes():emptyarray);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
						
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
			
				confirm_invalid_store_dialog.setButton(IABDialog.BUTTON_POSITIVE,R.string.iab_retry, new View.OnClickListener() {
					public void onClick(View v) {
						confirm_invalid_store_dialog.dismiss();
						
						INFO(TAG, "isBillingSupported()");
						
						GMPUtils utils = GMPUtils.getInstance();
						//utils.setContext(((Activity)SUtils.getContext()));
						utils.mSetupDone = false;
						utils.startService(mStartUpCallBack);
						
						try{Thread.sleep(2000);}catch(Exception exd){}
						INFO(TAG, "isBillingSupported() after sleep");
						InAppBilling.c();
						if (InAppBilling.mMKTStatusHk == InAppBilling.MKT_FREEDOM_RUNNING || InAppBilling.mMKTStatus != InAppBilling.MKT_SUPPORTED) 
						{
						#if USE_GOOGLE_ANALYTICS_TRACKING
							if (InAppBilling.mMKTStatusHk == InAppBilling.MKT_FREEDOM_RUNNING)
								GoogleAnalyticsTracker.trackEvent(GoogleAnalyticsConstants.Category.InAppBilling, GoogleAnalyticsConstants.Action.GoogleTransaction, "df2v3", null);
						#endif
							if (InAppBilling.mMKTStatus == InAppBilling.MKT_NO_SUPPORTED)
									showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID, InAppBilling.mItemID);
								else if (InAppBilling.mMKTStatusHk == InAppBilling.MKT_FREEDOM_RUNNING ||
										 InAppBilling.mMKTStatus == InAppBilling.MKT_CANNOT_CONNECT)
									showDialog(DIALOG_CANNOT_CONNECT_ID, InAppBilling.mItemID);
						}
						else 
						{
							requestTransaction(InAppBilling.mItemID);
						}
					}
				});
				confirm_invalid_store_dialog.show();	
			}
		});	
	}

    @Override
    public boolean restoreTransactions()    
	{
		INFO(TAG, "Restore Transactions started");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Restore Transactions started");
		GMPUtils utils = GMPUtils.getInstance();
		utils.launchQueryPurchases(((Activity)SUtils.getContext()),mRestoreTransactionsCB);
	   return true;
    }
	
	IABCallBack mStartUpCallBack = new IABCallBack()
	{
		public void runCallBack(Bundle bundle)
		{
			GMPResult result = new GMPResult(bundle.getInt(GMPUtils.KEY_RSLT_RESPONSE), bundle.getString(GMPUtils.KEY_RSLT_MESSAGE));
			JDUMP(TAG, result.getResponse());
			JDUMP(TAG, result.getMessage());
			
			if (result.isSuccess())
			{
				GMPUtils utils = GMPUtils.getInstance();
				if (InAppBilling.mMKTStatus != InAppBilling.MKT_SUPPORTED)
					utils.launchQueryPurchases(((Activity)SUtils.getContext()),mToConsumeItemsCB);
				
				InAppBilling.mMKTStatus = InAppBilling.MKT_SUPPORTED;
			} else {
				InAppBilling.mMKTStatus = InAppBilling.MKT_CANNOT_CONNECT;
			}
		}
	};
	
	IABCallBack mToConsumeItemsCB = new IABCallBack()
	{
		public void runCallBack(Bundle bundle)
		{
			GMPResult result = new GMPResult(bundle.getInt(GMPUtils.KEY_RSLT_RESPONSE), bundle.getString(GMPUtils.KEY_RSLT_MESSAGE));
			JDUMP(TAG, result.getResponse());
			JDUMP(TAG, result.getMessage());
			String PurchaseJson = bundle.getString(GMPUtils.KEY_PCHS_JSON);
			String Signature = bundle.getString(GMPUtils.KEY_PCHS_SIGNATURE);
			JDUMP(TAG,PurchaseJson);
			JDUMP(TAG,Signature);
			
			PurchaseInfo purchase = null;
			
			if (result.isSuccess())
			{		
				try{
					
					
					if (!InAppBilling.isProfileReady)
					{	
						DBG(TAG, "The profile is not ready, trying to retrieve the item list...");
						Bundle bdle = new Bundle();
						bdle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_RETRIEVE_LIST);
						bdle.putString(InAppBilling.GET_STR_CONST(IAB_LIST),"");
						InAppBilling.getData(bdle);
					}
					
					boolean isOnlineReady = true;
				#if !ITEMS_STORED_BY_GAME
					String acnum = InAppBilling.GET_GGLIVE_UID();
					String creds = InAppBilling.GET_CREDENTIALS();
					if (TextUtils.isEmpty(acnum) && TextUtils.isEmpty(creds))
						isOnlineReady = false; 	
				#endif	
					if (!InAppBilling.isProfileReady || !isOnlineReady)
					{
						JDUMP(TAG, InAppBilling.isProfileReady);
						JDUMP(TAG, isOnlineReady);
						final IABCallBack mCB = this;
						int retries = bundle.getInt("retries",0);
						JDUMP(TAG, retries);
						bundle.putInt("retries",++retries);
						final Bundle bdl = new Bundle(bundle);
						new Thread( new Runnable()
						{
							public void run()
							{
								DBG(TAG,"Waiting to retry item delivery");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Waiting to retry item delivery, no items retrieved yet...");
								try { Thread.sleep(5*1000); }catch(Exception e) {}
								mCB.runCallBack(bdl);
							}
						}
						#if !RELEASE_VERSION
							,"Pending_notifys"
						#endif
							).start();
							
						InAppBilling.c();	
						return;
					}
					
					

					
					purchase = new PurchaseInfo(PurchaseJson, Signature);
					String mItemId = null;
					
					ServerInfo si = getServerInfo();
					if (TextUtils.isEmpty(purchase.getDeveloperPayload()))
					{
						mItemId = si.getItemIdByUID(purchase.getSku());
					}
					else
					{
						mItemId = new String(Base64.decode(purchase.getDeveloperPayload()));
					}
					

					InAppBilling.c();
					if (InAppBilling.mMKTStatusHk == InAppBilling.MKT_FREEDOM_RUNNING) 
					{
						ERR(TAG,"Freedom running detected, ignoring item delivered");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Freedom running detected, ignoring item delivered");
						return; 
					}
					
					if (!TextUtils.isEmpty(mItemId))
					{
						InAppBilling.mItemID = mItemId;
						InAppBilling.mNotifyId = purchase.getToken();
						
					
						String managed = si.getItemAttribute(mItemId, InAppBilling.GET_STR_CONST(IAB_MANAGED));
						JDUMP(TAG,managed);
						if (!TextUtils.isEmpty(managed) && managed.equals("0") && !TextUtils.isEmpty(purchase.getToken())) 
						{
							INFO(TAG, "Resending pending owned item to notification");
							sendIABNotification(purchase);
						}
					}
					
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
				}
			}
		}
	};
	
	IABCallBack mRestoreTransactionsCB = new IABCallBack()
	{
		public void runCallBack(Bundle bundle)
		{
			GMPResult result = new GMPResult(bundle.getInt(GMPUtils.KEY_RSLT_RESPONSE), bundle.getString(GMPUtils.KEY_RSLT_MESSAGE));
			JDUMP(TAG, result.getResponse());
			JDUMP(TAG, result.getMessage());
			String PurchaseJson = bundle.getString(GMPUtils.KEY_PCHS_JSON);
			String Signature = bundle.getString(GMPUtils.KEY_PCHS_SIGNATURE);
			JDUMP(TAG,PurchaseJson);
			JDUMP(TAG,Signature);
			
			PurchaseInfo purchase = null;
			
			int retries = 0;
			if (result.isSuccess())
			{		
				try{
					
					purchase = new PurchaseInfo(PurchaseJson, Signature);
					String mItemId = null;
					
					ServerInfo si = getServerInfo();
					
					mItemId = si.getItemIdByUID(purchase.getSku());
					
					InAppBilling.c();
					if (InAppBilling.mMKTStatusHk == InAppBilling.MKT_FREEDOM_RUNNING) 
					{
						ERR(TAG,"Freedom running detected, ignoring item delivered");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Freedom running detected, ignoring item delivered");
						return; 
					}
					
					if (!TextUtils.isEmpty(mItemId))
					{
						InAppBilling.mItemID = mItemId;
						
						String managed = si.getItemAttribute(mItemId, InAppBilling.GET_STR_CONST(IAB_MANAGED));
						JDUMP(TAG,managed);
						if (!TextUtils.isEmpty(managed) && managed.equals("1")) 
						{
							INFO(TAG, "Restore item sku '"+mItemId+"'");
							bundle.clear();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
							byte[] emptyarray = new String().getBytes();
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():emptyarray);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);//trash value
							
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 2); //trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), 3); ////trash value
								
								
							try{
								Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
								Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
								bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
							}catch(Exception ex ) {
								ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
							}
						}
						/*else if (!TextUtils.isEmpty(managed) && managed.equals("0") && !TextUtils.isEmpty(purchase.getToken())) 
						{
							INFO(TAG, "Resending pending owned item to notification");
							LOGGING_LOG_INFO(IABConst.IAP_LOG_TYPE_WARNING, IABConst.IAP_LOG_STATUS_INFO, "Resending pending owned item to notification");
							sendIABNotification(purchase);
						}*/
					}
					
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
				}
			}
		}
	};
	
	private String getValue(String src, int pos)
	{
		if (!TextUtils.isEmpty(src) && pos >= 0 && src.contains("|"))
		{
			String a[] = src.split("\\|");
			if (pos+1 > a.length)
				return null;
			return a[pos];
		}
		return null;
	}
	
	IABCallBack mUOwnThisItemCB = new IABCallBack()
	{
		public void runCallBack(Bundle bundle)
		{
			GMPResult result = new GMPResult(bundle.getInt(GMPUtils.KEY_RSLT_RESPONSE), bundle.getString(GMPUtils.KEY_RSLT_MESSAGE));
			JDUMP(TAG, result.getResponse());
			JDUMP(TAG, result.getMessage());
			String PurchaseJson = bundle.getString(GMPUtils.KEY_PCHS_JSON);
			String Signature = bundle.getString(GMPUtils.KEY_PCHS_SIGNATURE);
			JDUMP(TAG,PurchaseJson);
			JDUMP(TAG,Signature);
			
			PurchaseInfo purchase = null;
			
			int retries = 0;
			if (result.isSuccess())
			{		
				try{
					
					purchase = new PurchaseInfo(PurchaseJson, Signature);
					String mItemId = null;
					
					ServerInfo si = getServerInfo();
					
					mItemId = si.getItemIdByUID(purchase.getSku());
					
					InAppBilling.c();
					if (InAppBilling.mMKTStatusHk == InAppBilling.MKT_FREEDOM_RUNNING) 
					{
						ERR(TAG,"Freedom running detected, Consuming item to avoid delivering...");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Freedom running detected, Consuming item to avoid delivering...");
						GMPUtils utils = GMPUtils.getInstance();
						utils.launchConsumeProductRequest(((Activity)SUtils.getContext()), purchase, null);
						return; 
					}
					
					if (!TextUtils.isEmpty(mItemId))
					{
						InAppBilling.mItemID = mItemId;
						
						String managed = si.getItemAttribute(mItemId, InAppBilling.GET_STR_CONST(IAB_MANAGED));
						JDUMP(TAG,managed);
						JDUMP(TAG,mItemToRestore);
						JDUMP(TAG,mItemId);
						if (!TextUtils.isEmpty(mItemToRestore) && mItemId.equals(mItemToRestore))
						{
							mItemToRestore = null;
							if (!TextUtils.isEmpty(managed) && managed.equals("1")) 
							{
								INFO(TAG, "Restore item sku '"+mItemId+"'");
								bundle.clear();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
								byte[] emptyarray = new String().getBytes();
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():emptyarray);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);//trash value
							
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 2); //trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), 3); ////trash value
								
								
								try{
									Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
									Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
									bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
								}catch(Exception ex ) {
									ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
								}
							}	
							else if (!TextUtils.isEmpty(managed) && managed.equals("0") && !TextUtils.isEmpty(purchase.getToken())) 
							{
								INFO(TAG, "Resending pending owned item to notification");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Resending pending owned item to notification");
								sendIABNotification(purchase);
							}
						}
						else
						{
							INFO(TAG, "Item filtered, must not be delivered when 'You already own this item' is received");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Item filtered, must not be delivered when 'You already own this item' is received");
						}
					}
					
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
				}
			}
		}
	};
	public String getJSONHeaders(Device device)
	{
		JSONObject jObj = new JSONObject();
		try
		{
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI),GGI);//"x-up-gl-ggi"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GAMECODE),GL_DEMO_CODE);//"x-up-gl-gamecode"
			
			String value = InAppBilling.GET_GGLIVE_UID(); //"x-up-gl-acnum"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM),value);
			
			value = InAppBilling.GET_CREDENTIALS(); //"x-up-gl-fed-credentials"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS),value);
			
			value = GET_CLIENT_ID(); //"x-up-gl-fed-client-id"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID),value);
		#if (HDIDFV_UPDATE != 2)	
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),device.getIMEI());//"x-up-gl-imei"
		#endif
		#if HDIDFV_UPDATE
			DBG("I_S"+HDIDFV_UPDATE, device.getHDIDFV());
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV());//"x-up-gl-hdidfv"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID());//"x-up-gl-gldid"
		#endif
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_USR_AGENT),device.getUserAgent());//"User-Agent"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_BUILD_MDL),android.os.Build.MODEL);//"x-android-os-build-model"
			
			jObj.put("Accept", InAppBilling.GET_STR_CONST(IAB_ECOM_API_VERSION));//"application/com.gameloft.ecomm.crm-v1.1+json"
			
			return jObj.toString();
		} catch (JSONException e) 
		{
			ERR(TAG, e.getMessage());
			DBG_EXCEPTION(e);
		}
		return null;
	}
	
	public void sendIABNotification(final PurchaseInfo purchase)//Send Gameloft Notification
	{
		//InAppBilling.save(WAITING_FOR_GAMELOFT);
		//InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);

		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();
		final ServerInfo si					= getServerInfo();
		Device device 					= new Device();
		StringBuffer sb 				= new StringBuffer();
		
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		try
		{
			InAppBilling.mItemID = new String(Base64.decode(purchase.getDeveloperPayload()));
			
			si.searchForDefaultBillingMethod(InAppBilling.mItemID);
		}catch(Exception e)
		{
			ERR(TAG,e.getMessage());
			DBG_EXCEPTION(e);
		}
		#if BAZAAR_STORE
		//URL: https://iap.gameloft.com/freemium/bazaar/transactions/
		//PAYLOAD: game=GTHF&content=3971&price=1.99&money=1&response_data=YYYYYYYY
		sb.append("game=" + device.getDemoCode());
		sb.append("&content=" + reqHandler.encodeString(InAppBilling.mItemID));
		sb.append("&price=" + reqHandler.encodeString(si.getGamePrice()));
		sb.append("&money=" + reqHandler.encodeString(si.getMoneyBilling()));
		sb.append("&response_data=" + reqHandler.encodeString(purchase.mOriginalJson));
		//sb.append(reqHandler.encodeString(purchase.getOrderId())); sb.append("&");
		sb.append("&security_code=" + SUtils.getLManager().getRandomCodeNumber());
		//sb.append(reqHandler.encodeString(purchase.getToken()));sb.append("&");
		//sb.append(InAppBilling.GET_STR_CONST(IAB_CONFIRM_ITEM_DELIVERED));
		#else
		sb.append("b=");
		sb.append(InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME));	sb.append("|");
		sb.append(reqHandler.encodeString(InAppBilling.mItemID));	sb.append("|");
		sb.append(reqHandler.encodeString(si.getGamePrice())); 	sb.append("|");
		sb.append(reqHandler.encodeString(si.getMoneyBilling())); 	sb.append("|");
		sb.append(SUtils.getLManager().getRandomCodeNumber()); 	sb.append("|");
		sb.append(reqHandler.encodeString(purchase.getOrderId())); sb.append("|");
		sb.append(reqHandler.encodeString(purchase.getToken()));sb.append("&");
		sb.append(InAppBilling.GET_STR_CONST(IAB_CONFIRM_ITEM_DELIVERED));
		#endif
		
		#if USE_IAP_VALIDATION_BETA_SERVER && ITEMS_STORED_BY_GAME == 0
		sb.append("&beta=1");
		#endif
				
		String url = si.getURLbilling();		
		String qry = sb.toString();
		String headers = getJSONHeaders(device);
		
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "ContentPurchase");
		LOGGING_APPEND_REQUEST_PAYLOAD(qry, "ContentPurchase");
		LOGGING_APPEND_REQUEST_HEADERS(headers, "ContentPurchase");
		LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("ContentPurchase"));

			INFO(TAG, "[SendConfirmation Request]...");
			
			reqHandler.doRequestByPut(url, qry, headers, new IABCallBack()
			{
				public void runCallBack(Bundle bundle)
				{
					DBG(TAG, "[SendConfirmation Request] Callback");
					int response = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
					if (response == IABRequestHandler.SUCCESS_RESULT)
					{
						//"x-cc-log-id" String for Suppor Ticket
						String result = bundle.getString(IABRequestHandler.KEY_RESPONSE);
						JDUMP(TAG,result);

						LOGGING_APPEND_RESPONSE_PARAM(result, "ContentPurchase");
						LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("ContentPurchase"));
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "ContentPurchase: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("ContentPurchase") +" seconds" );
						LOGGING_REQUEST_REMOVE_REQUEST_INFO("ContentPurchase");
						
//						#if BAZAAR_STORE
						String strResponse	= result;
						String strScrity	= "";
						try{
							org.json.JSONObject jObject = new org.json.JSONObject(result);
							strScrity = jObject.getString("code");
						}catch(org.json.JSONException exception){}
						String eComTxId		= "";
//						#else //the way google infrastructure responds, leaving here just as informative code
//						String strResponse	= getValue(result,RESULT_STR_POS);
//						String strCode		= getValue(result,RESULT_CODE_POS);
//						String strScrity	= getValue(result,SECURITY_CODE_POS);
//						String eComTxId		= getValue(result,ECOM_TX_ID_POS);
//						#endif
						String managed = si.getItemAttribute(InAppBilling.mItemID, InAppBilling.GET_STR_CONST(IAB_MANAGED));
						JDUMP(TAG,managed);
						
						#if BAZAAR_STORE
						if (!TextUtils.isEmpty(strResponse) && bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE)==201)
						#else
						if (!TextUtils.isEmpty(strResponse) && !Encrypter.crypt(strResponse).equals(FAILURE_RESULT))
						#endif
						{
							try{
								int scrity = Integer.parseInt(strScrity);
								if (SUtils.getLManager().isValidCode(scrity))
								{
									LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Valid Nonce Code!" );
									#if BAZAAR_STORE
									if (!TextUtils.isEmpty(strResponse) &&  bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE)==201)
									#else
									if (!TextUtils.isEmpty(strResponse) && Encrypter.crypt(strResponse).equals(SUCCESS_RESULT))
									#endif
									{
										LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Succes validation from Ecommerce!" );
										
										bundle.clear();
										bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
										byte[] emptyarray = new String().getBytes();
							
										String OrderNotify = null;
										if (!TextUtils.isEmpty(managed) && managed.equals("1"))
											OrderNotify = purchase.getOrderId();
										else
											OrderNotify = purchase.getOrderId() +"|"+purchase.getToken();
										JDUMP(TAG,OrderNotify);
										
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():emptyarray);
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_NOTIFY_ID),OrderNotify.getBytes());
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(eComTxId!=null)?eComTxId.getBytes():emptyarray);
										bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 2);//trash value
										bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
											
										try{
											Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
											Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
											bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
										}catch(Exception ex ) {
											ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
										}
										return;
									}
								
								}
							}catch (Exception e) 
							{
								ERR(TAG,e.getMessage());
								DBG_EXCEPTION(e);
							}
						}
						
						if (!TextUtils.isEmpty(managed) && managed.equals("0"))
						{
							GMPUtils utils = GMPUtils.getInstance();
							purchase.mSku 		= InAppBilling.mItemID;
							utils.launchConsumeProductRequest(((Activity)SUtils.getContext()), purchase, null);
						}
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Failure at the validation from Ecommerce!" );
						InAppBilling.saveLastItem(STATE_FAIL);
						InAppBilling.clear();
						//Not a valid response
						bundle.clear();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
						byte[] emptyarray = new String().getBytes();
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
							
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
						}
					}
					else
					{
						INFO(TAG, "An error with the server conection occurred");

						int code = bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for ContentPurchase failed with error code: "+code );
						
						bundle.clear();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
						byte[] emptyarray = new String().getBytes();
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_PENDING);
							
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
						}
						
						new Thread( new Runnable()
						{
							public void run()
							{
								try { Thread.sleep(5000); }catch(Exception e) {}
								sendIABNotification(purchase);
							}
						}
					#if !RELEASE_VERSION
						,"Validation_retry"
					#endif
						).start();
					}
				}
			});
	}
	
}
#endif //USE_IN_APP_BILLING