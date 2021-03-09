#if USE_IN_APP_BILLING && SAMSUNG_STORE
package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import java.lang.reflect.Method;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.GAME_ACTIVITY_NAME;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;


public class SamsungHelper extends IABTransaction
{
	SET_TAG("InAppBilling");

	private static String mCurrentId = null;
	private static AServerInfo mServerInfo;
	private static Device mDevice;
	public static boolean isRestoreTransaction = false;
	public static boolean isRepurchasedItem = false;
	static XPlayer mXPlayer;
	static SamsungHelper mThis;

	private String mPrefDataCenter = null; //preferred data center, must be obtained from eve.

	SamsungHelper(AServerInfo si)
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
		INFO(TAG, "[sendConfirmation]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[sendConfirmation]");
		xsendConfirmation();
	}
	
	private void launchSamsungIAB()
	{
		///////// Samsung IAP v2
		Intent intent = new Intent(SUtils.getContext(), SamsungIAB2Activity.class );
		intent.putExtra("SamsungItemId", GetSamsungItemId());
		intent.putExtra("ItemGroupId", GetGroupId());
		INFO(TAG, "SamsungItemId["+GetSamsungItemId()+"] ItemGroupId["+GetGroupId()+"]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "SamsungItemId["+GetSamsungItemId()+"] ItemGroupId["+GetGroupId()+"]");
		SUtils.getContext().startActivity( intent );
		/////////////////////////
	}

	@Override
	public boolean requestTransaction(String id)
	{
		INFO(TAG, "[requestTransaction]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[requestTransaction] ");
		mCurrentId = id;
		isRestoreTransaction = false;
		isRepurchasedItem = false;
		
	#if ITEMS_STORED_BY_GAME
		INFO(TAG, "Items stored by game, will not request data center to eve!");
		launchSamsungIAB();
	#else
		JDUMP(TAG,mPrefDataCenter);
		if(!TextUtils.isEmpty(mPrefDataCenter))
		{
			launchSamsungIAB();
		} else 
		{
			//request data center from eve, then launch samsung IAB
			requestDataCenter();
		}
	#endif
		
		return(true);
	}

	private void requestDataCenter()
	{
		IABRequestHandler request = IABRequestHandler.getInstance();

		StringBuffer url = new StringBuffer(InAppBilling.GET_STR_CONST(IAB_EVE_URL));
		url.append("config/");
		url.append(GET_CLIENT_ID());//isma temp
		url.append("/datacenters");

		request.doRequestByGet(url.toString(), null, new IABCallBack()
		{
			public void runCallBack(Bundle bundle)
			{
				int result = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
				String response = bundle.getString(IABRequestHandler.KEY_RESPONSE);
				DBG(TAG,"Datacenter request finished!");
				JDUMP(TAG,result);
				JDUMP(TAG,response);

				if (result == IABRequestHandler.SUCCESS_RESULT && !TextUtils.isEmpty(response))
				{
					//process response JSON
					try{
						JSONArray jArray = new JSONArray(response);
						for(int i = 0; i < jArray.length(); i++)
						{
							JSONObject obj = jArray.getJSONObject(i);
							if(obj.getBoolean("preferred"))
							{
								mPrefDataCenter = obj.getString("name");
								break;
							}

						}

						if(!TextUtils.isEmpty( mPrefDataCenter ) ) 
						{
							INFO(TAG, "Preferred data center is: " + mPrefDataCenter);
							launchSamsungIAB();
							return;
						}
					}catch(Exception e)
					{
						DBG_EXCEPTION(e);
					}

				}
				
				{
					//return fail
					ERR(TAG, "Could not retrieve information from eve");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Could not retrieve information from eve");
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
					}catch(Exception ex ) {ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());}
					ERR(TAG, "purchaseValidation STATE_FAIL END");
				}
			}
		});


	}

	
	
	@Override
    public boolean restoreTransactions()
    {
        INFO(TAG, "[restoreTransactions]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[restoreTransactions]");
		mCurrentId = getServerInfo().getSelectedItem();
		isRestoreTransaction = true;
		isRepurchasedItem = false;

		if(GetGroupId() != null)
		{
			///////// Samsung IAP v2
			Intent intent = new Intent(SUtils.getContext(), SamsungIAB2Activity.class );
			intent.putExtra("ItemGroupId", GetGroupId());
			SUtils.getContext().startActivity( intent );
			/////////////////////////
		}
		else
		{
			ERR(TAG, "***** Restore Transactions Failed: Profile is missing *****");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Restore Transactions Failed: Profile is missing.");
			IABResultCallBack(IAB_BUY_FAIL);
		}
		return(true);
	}	
	
	public static String GetContentId()
	{
		return (mCurrentId);
	}
	
	public static String GetSamsungItemId()
	{
		return(getServerInfo().getBillingAttribute(GetContentId(),"samsung_item_id"));
	}
	
	public static String GetGroupId()
	{
		return(getServerInfo().getBillingAttribute(GetContentId(), "group_id"));
	}

	public static void IABResultCallBack(int IABResult)
	{
		INFO(TAG, "[IABResultCallBack]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[IABResultCallBack]");
		DBG(TAG, "IABResult: " + IABResult);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "IABResult: " + IABResult);
		
		Bundle bundle = new Bundle();
		byte[] emptyarray = new String().getBytes();
		
		if(isRestoreTransaction)
		{
			if(IABResult == IAB_BUY_OK && !isRepurchasedItem)
			{
				mThis.sendConfirmation();
			} else 
			{
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
				
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM), emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(GetContentId()!=null)?GetContentId().getBytes():emptyarray);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
										
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IABResult);
				
				DBG(TAG, "IABOperation: OP_FINISH_RESTORE_TRANS");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "IABOperation: OP_FINISH_RESTORE_TRANS");
				
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
			}
		}
		else
		{
			if(IABResult == IAB_BUY_OK && !isRepurchasedItem)
			{
				PurchaseInfo pi = new PurchaseInfo(InAppBilling.mItemID, SamsungIAB2Activity.getPurchaseId(), SamsungIAB2Activity.getPaymentId());
				mThis.purchaseValidation(pi);
			} else 
			{

				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetContentId()!=null)?GetContentId().getBytes():emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(GetContentId()!=null)?GetContentId().getBytes():emptyarray);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
										
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IABResult);
				DBG(TAG, "IABOperation: OP_FINISH_BUY_ITEM");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "IABOperation: OP_FINISH_BUY_ITEM");

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
			}
		}
		
		isRestoreTransaction = false;
		
		
	}
	
	public static void IABResultCallBack(int IABResult, int thirdPartyError, String thirdPartyErrorString)
	{
		INFO(TAG, "[IABResultCallBack]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[IABResultCallBack]");
		DBG(TAG, "IABResult: " + IABResult);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "IABResult: " + IABResult, "thirdPartyError: " + thirdPartyError, "thirdPartyErrorString: "+ thirdPartyErrorString);
		
		Bundle bundle = new Bundle();
		byte[] emptyarray = new String().getBytes();
		
		if(isRestoreTransaction)
		{
			if(IABResult == IAB_BUY_OK && !isRepurchasedItem)
			{
				mThis.sendConfirmation();
			} else 
			{
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
				
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM), emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(GetContentId()!=null)?GetContentId().getBytes():emptyarray);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
										
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IABResult);
				
				DBG(TAG, "IABOperation: OP_FINISH_RESTORE_TRANS");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "IABOperation: OP_FINISH_RESTORE_TRANS");
				
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
			}
		}
		else
		{
			if(IABResult == IAB_BUY_OK && !isRepurchasedItem)
			{
				PurchaseInfo pi = new PurchaseInfo(InAppBilling.mItemID, SamsungIAB2Activity.getPurchaseId(), SamsungIAB2Activity.getPaymentId());
				mThis.purchaseValidation(pi);
			} else 
			{

				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetContentId()!=null)?GetContentId().getBytes():emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(GetContentId()!=null)?GetContentId().getBytes():emptyarray);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(thirdPartyErrorString!=null)?thirdPartyErrorString.getBytes():null);				
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), thirdPartyError);
										
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IABResult);
				DBG(TAG, "IABOperation: OP_FINISH_BUY_ITEM");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "IABOperation: OP_FINISH_BUY_ITEM");

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
			}
		}
		
		isRestoreTransaction = false;
		
		
	}

	@Override
	public void showDialog(final int idx, final String id) {}
													
	private static boolean mIsConfirmInProgress = false;
	
	static void xsendConfirmation()
	{
		INFO(TAG,"[xsendConfirmation]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[xsendConfirmation]");
		
		if (mServerInfo == null)
			mServerInfo = SamsungHelper.getServerInfo();
		if (mIsConfirmInProgress)
			return;
		new Thread( new Runnable()
		{
			public void run()
			{
				Looper.prepare();
				boolean finish = false;
				mIsConfirmInProgress = true;
				do
				{
					try
					{
						DBG(TAG,"SendIABNotification");
						XPlayer mXplayer = new XPlayer(new Device(mServerInfo));
						mXplayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
						mXplayer.sendIABNotification(0, InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME), InAppBilling.mOrderID);
						
						long time = 0;
						while (!mXplayer.handleIABNotification())
						{
							try {
								Thread.sleep(50);
							} catch (Exception exc) {}
							
							if((System.currentTimeMillis() - time) > 1500)
							{
								DBG(TAG, "[sendIABNotification] Waiting for response");
								time = System.currentTimeMillis();
							}
						}

						if(XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
						{
							finish = true;
							DBG(TAG, "Response Confirmation Success");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Response Confirmation Success");
							String TxId = XPlayer.getLastEComTxId();
							XPlayer.clearLastEComTxId();
						}
						else 
						{
							ERR(TAG,"Response Confirmation Fail");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Response Confirmation Fail");
						}
						mIsConfirmInProgress = false;
					}catch(Exception exd)
					{
						mIsConfirmInProgress = false;
						DBG_EXCEPTION(exd);
						WARN(TAG,"An error has occurred "+exd.getMessage());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "An error has occurred "+exd.getMessage());
					}
				}
				while(!finish);
				Looper.loop();
			}
		}
	#if !RELEASE_VERSION
		,"Thread-Confirmation"
	#endif
		).start();
	}
	
	private static boolean validationFailed = false;
	
	private void purchaseValidation(final PurchaseInfo purchase)
	{
		INFO(TAG, "[purchaseValidation]");

		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();
		final AServerInfo si			= getServerInfo();
		Device device 					= new Device();
		StringBuffer sb 				= new StringBuffer();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		validationFailed = false;
		
		try{si.searchForDefaultBillingMethod(purchase.getItemID());}
		catch(Exception e){DBG_EXCEPTION(e);}
		
		sb.append("game="+device.getDemoCode());
		sb.append("&content_id="+reqHandler.encodeString(purchase.getItemID()));
		sb.append("&price="+reqHandler.encodeString(si.getGamePrice()));
		sb.append("&money_id="+reqHandler.encodeString(si.getMoneyBilling()));
		sb.append("&security_code="+SUtils.getLManager().getRandomCodeNumber());
		sb.append("&purchaseId="+purchase.getOrderID());

		String url = mServerInfo.getURLbilling();
		String qry = sb.toString();
		String headers = getJSONHeaders(device);
		
		LOGGING_APPEND_REQUEST_PARAM(url, "PUT", "Samsung purchaseValidation");
		LOGGING_APPEND_REQUEST_PAYLOAD(qry, "Samsung purchaseValidation");
		LOGGING_APPEND_REQUEST_HEADERS(headers, "Samsung purchaseValidation");
		LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("Samsung purchaseValidation"));
			
		reqHandler.doRequestByPut(url, qry, headers, new IABCallBack()
		{
			public void runCallBack(Bundle bundle)
			{
				DBG(TAG, "purchaseValidation request callback");
				int result = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
				String response = bundle.getString(IABRequestHandler.KEY_RESPONSE);
				
				DBG(TAG, "purchaseValidation server result: "+result);
				DBG(TAG, "purchaseValidation server response: "+response);
				
				if (result == IABRequestHandler.SUCCESS_RESULT && !TextUtils.isEmpty(response))
				{
					LOGGING_APPEND_RESPONSE_PARAM(response, "Samsung purchaseValidation");
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("Samsung purchaseValidation"));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Samsung purchaseValidation: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("Samsung purchaseValidation") +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO("Samsung purchaseValidation");
					
					String strCode	= "";
					String eComTxId	= "";
					
					try{
						org.json.JSONObject jObject = new org.json.JSONObject(response);
						strCode = jObject.getString("code");
						eComTxId = jObject.getString("transaction_id");
					}catch(org.json.JSONException exception){}
					
					DBG(TAG, "purchaseValidation code: "+strCode);
					DBG(TAG, "purchaseValidation eComTxId: "+eComTxId);
					
					if (!TextUtils.isEmpty(strCode) && bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE)==201)
					{
						try
						{
							int scrity = Integer.parseInt(strCode);
							if (SUtils.getLManager().isValidCode(scrity))
							{
								LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Valid Code!" );
								LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Validation successfully from Ecommerce!" );
								DBG(TAG, "** Validation successfully from Ecommerce! **");
								JDUMP(TAG, purchase.getStoreCertificate());
								bundle.clear();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
								
								byte[] emptyarray = new String().getBytes();
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(purchase.getItemID()!=null)?purchase.getItemID().getBytes():emptyarray);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_NOTIFY_ID),(purchase.getOrderID()!=null)?purchase.getOrderID().getBytes():emptyarray);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(eComTxId!=null)?eComTxId.getBytes():emptyarray);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(purchase.getStoreCertificate()!=null)?purchase.getStoreCertificate().getBytes():emptyarray);
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);		

								try{
									Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
									Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
									bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
								}catch(Exception ex ) {
									ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
								}
								
								InAppBilling.saveLastItem(STATE_SUCCESS);
								InAppBilling.clear();
								DBG(TAG, "purchaseValidation request callback END");
								return;
							}
							else
								ERR(TAG,"Invalid code from Ecommerce");
						}
						catch (Exception e) 
						{
							ERR(TAG, e.getMessage());
							DBG_EXCEPTION(e);
						}
					}

					ERR(TAG, "Validation Failed from Ecommerce!");
					validationFailed = true;
				}
				else if(result == IABRequestHandler.INVALID_RESULT && !TextUtils.isEmpty(response) && response.contains("host"))
				{
					WARN(TAG, "Server connection error!");
					int code = bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for purchaseValidation failed with error code: "+code );
					
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
					}catch(Exception ex ) {ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());}
					
					new Thread( new Runnable()
					{
						public void run()
						{
							try { Thread.sleep(5000); }catch(Exception e) {}
							purchaseValidation(purchase);
						}
					}
				#if !RELEASE_VERSION
					,"Validation_retry"
				#endif
					).start();
				}
				else
				{
					ERR(TAG, "purchaseValidation INVALID_RESULT");
					validationFailed = true;
				}
				
				if(validationFailed)
				{
					ERR(TAG, "purchaseValidation STATE_FAIL");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "purchaseValidation STATE_FAIL" );
					InAppBilling.saveLastItem(STATE_FAIL);
					InAppBilling.clear();
					
					//Not a valid response
					bundle.clear();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
					
					byte[] emptyarray = new String().getBytes();
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(purchase.getItemID()!=null)?purchase.getItemID().getBytes():emptyarray);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
						
					try{
						Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
						Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
						bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
					}catch(Exception ex ) {ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());}
					ERR(TAG, "purchaseValidation STATE_FAIL END");
				}
			}
		});
	}

	
	private String getJSONHeaders(Device device)
	{
		JSONObject jObj = new JSONObject();
		try
		{
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_USR_AGENT),device.getUserAgent());//"User-Agent"
		
		#if (HDIDFV_UPDATE != 2)	
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),device.getIMEI());//"x-up-gl-imei"
		#endif
		#if HDIDFV_UPDATE
			DBG("I_S"+HDIDFV_UPDATE, device.getHDIDFV());
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV());//"x-up-gl-hdidfv"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID());//"x-up-gl-gldid"
		#endif
		
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI),GGI);//"x-up-gl-ggi"
			
			String value = InAppBilling.GET_GGLIVE_UID(); //"x-up-gl-acnum"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM),value);
			
			value = GET_CLIENT_ID(); //"x-up-gl-fed-client-id"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID),value);
			
			value = InAppBilling.GET_CREDENTIALS(); //"x-up-gl-fed-credentials"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS),value);
			
			jObj.put("x-up-gl-subno",device.getLineNumber());//"x-up-gl-subno"

			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_FED_DATACENTER), mPrefDataCenter);
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ANON_CREDS),InAppBilling.GET_ANONYMOUS_CREDENTIAL());//"x-up-gl-anon-credentials"			

			
			return jObj.toString();
		} catch (JSONException e) 
		{
			ERR(TAG, e.getMessage());
			DBG_EXCEPTION(e);
		}
		return null;
	}
	
	static class PurchaseInfo
	{	
		public PurchaseInfo (String item, String order, String payment)
		{
			itemID 		= item;
			orderID 	= order;
			paymentID	= payment;
		}
		
		private String itemID = null;
		private String orderID = null;
		private String paymentID = null;
		
		public String getItemID() 		{ return itemID; }
		public String getOrderID() 		{ return orderID; }
		public String getPaymentID() 	{ return paymentID; }


		public String getStoreCertificate()
		{
			String storeCert = null;
			JSONObject jObj = new JSONObject();
			if(!TextUtils.isEmpty(orderID) && !TextUtils.isEmpty(paymentID))
			{
				try
				{
					jObj.put(InAppBilling.GET_STR_CONST(IAB_SAMSUNG_PURCHASE_ID),orderID);//"purchase_id"
					jObj.put(InAppBilling.GET_STR_CONST(IAB_SAMSUNG_PAYMENT_ID),paymentID);//"payment_id"
					storeCert = jObj.toString();
				}catch(JSONException e) {}
			}
			return storeCert;
		}
	}
}
#endif
