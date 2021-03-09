package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.billing.common.StringEncrypter;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Encrypter;
import APP_PACKAGE.R;


public class GLOFTHelper extends IABTransaction
{
	SET_TAG("InAppBilling");
	
	private Handler mHandler;
	private static String mCurrentId = null;
	
	//get encrypted string
#define GES(id)		StringEncrypter.getString(id)
//get raw string
#define GRS(id)		SUtils.getContext().getString(id)

	
	public static final int DIALOG_CANNOT_CONNECT_ID 			= 1;
	public static final int DIALOG_CONFIRMATION_HTTP_WAP	 	= 2;
	public static final int DIALOG_WAITTING_CONFIRMATION	 	= 3;
	public static final int DIALOG_PURCHASE_SUCCESS			 	= 4;
	public static final int DIALOG_PURCHASE_FAIL				= 5;
	public static final int DIALOG_PURCHASE_SUCCESS_PENDING		= 6;
	public static final int DIALOG_CONFIRMATION_CC				= 7;
	public static final int DIALOG_SELECT_BILLING_METHOD		= 8;
	public static final int DIALOG_PURCHASE_RESULT_PENDING		= 9;
	public static final int DIALOG_TERMS_AND_CONDITIONS			= 10;
	public static final int DIALOG_VERIFY_WAP_PURCHASE			= 11;
	public static final int DIALOG_PENDING_PSMS					= 12;
	public static final int DIALOG_PURCHASE_FAIL_PSMS			= 13;
	public static final int DIALOG_UMP_R3_PAYMENT_INFO			= 14;
	public static final int DIALOG_UMP_R3_CONFIRM_PAYMENT		= 15;
	public static final int DIALOG_UMP_R3_PAYMENT_SUCCESS		= 16;
	public static final int DIALOG_UMP_R3_PAYMENT_FAILURE		= 17;
	// #if SHENZHOUFU_STORE
	public static final int DIALOG_PENDING_SHENZHOUFU			= 18;
	public static final int DIALOG_PENDING_PURCHASE_FAIL_SHENZHOUFU	= 19;
	// #endif
#if USE_PHD_PSMS_BILL_FLOW
	public static final int DIALOG_PURCHASE_CONFIRMATION				= 20;
#endif
	
	public static final int BT_CREDIT_CARD						= 0;
	public static final int BT_CREDIT_CARD_PREVIOUS_PURCHASE	= 1;
	public static final int BT_WAP_BILLING						= 2;
	#if SHENZHOUFU_STORE
	public static final int BT_SHENZHOUFU_BILLING				= 3;
	#endif
	public static final int BT_WAP_PAYPAL_BILLING				= 4;
	
	final static String TYPE_VISA 			= "VISA";
	final static String IS_VISA 			= "4";
	final static String TYPE_MASTERCARD	= "MASTERCARD";
	final static String IS_MASTERCARD		= "5";
	final static String CC_PREFIX			= " xxx-";
	
	final int TYPE_CHOOSE_PAYMENT = 0;
	final int TYPE_CHANGE_PAYMENT = 1;
	final int TYPE_FIRST_PAYMENT  = 2;
		
	final String HTTP_BILLING		= InAppBilling.GET_STR_CONST(IAB_HTTP);		//"http";
	final String WAP_BILLING		= InAppBilling.GET_STR_CONST(IAB_WAP);		//"wap";
	final String CC_BILLING			= InAppBilling.GET_STR_CONST(IAB_CC);		//"cc";
	final String BOKU_BILLING		= InAppBilling.GET_STR_CONST(IAB_BOKU);		//"boku";
	final String PAYPAL_BILLING		= InAppBilling.GET_STR_CONST(IAB_PAYPAL);	//"paypal";
	#if SHENZHOUFU_STORE
	final String SHENZHOUFU_BILLING		= InAppBilling.GET_STR_CONST(IAB_SHENZHOUFU);	//"shenzhoufu";
	#endif
	final String SMS_BILLING		= InAppBilling.GET_STR_CONST(IAB_SMS);		//"sms";
	final String PSMS_BILLING		= InAppBilling.GET_STR_CONST(IAB_PSMS);		//"psms";
	final String UMP_R3_BILLING		= InAppBilling.GET_STR_CONST(IAB_UMP_R3);	//"ump";
	
	
	final String BOKU_STRING		= InAppBilling.GET_STR_CONST(IAB_BOKU);				//"Boku";
	final String PAYPAL_STRING		= InAppBilling.GET_STR_CONST(IAB_PAYPAL);				//"Paypal";
	#if SHENZHOUFU_STORE
	final String SHENZHOUFU_STRING	= InAppBilling.GET_STR_CONST(IAB_SHENZHOUFU);				//"Shenzhoufu";
	#endif
	
	final String SUCESS_PENDING_CODE	= "10010"; /* Success, the content has been purchased correctly, however the Online servers have failed to add the content to the user's account.
												      An automatic mail has been sent to the team so that they can manually add the content as soon as possible.*/
	final String SUCESS_CODE			= "20004"; //Success, the content has been purchased correctly, and notification to OBB done correctly (if necessary)
	
	private final String SUCCESS_RESULT = "T7WxMl1MuYnllpIJnNJtoFD1ENkvNoVcrGXq7CvZ1Oo=";

	
	
	private static int mBillingType;
	
	private static Device mDevice;
	static XPlayer mXPlayer;
	static GLOFTHelper mThis;
	private static String mWapID="";
	private static String mWapTxID="";
	private static Bundle mWapErrBundle=null;
	private ProgressDialog dSendingSms = null;
	private static String SMS_TID="";
	
	GLOFTHelper(AServerInfo si)
	{
		super(si);
		mDevice = new Device(si);
		mXPlayer = new XPlayer(mDevice);
		//mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		mThis = this;
	}
	
	static void setWAPID(String value)
	{
	    mWapID = value;
	}
	
	public static String getWAPID()
	{
		return (mWapID);
	}

	static void setWAPTxID(String value)
	{
	    mWapTxID = value;
	}
	
	public static String getWAPTxID()
	{
		return (mWapTxID);
	}

	static void setWAPErrorBundle(Bundle bundle)
	{
		mWapErrBundle = bundle;
	}

	public static Bundle getWAPErrorBundle()
	{
		return (mWapErrBundle);
	}
	
	static void setSMS_TID(String value)
	{
	    SMS_TID = value;
	}
	
	public static String getSMS_TID()
	{
		return (SMS_TID);
	}	
	
	static GLOFTHelper GetInstance()
	{
		return(mThis);
	}
	static XPlayer GetXPlayer()
	{
		return (mXPlayer);
	}
	
	static Device GetDevice()
	{
		return(mDevice);
	}
	
	static AServerInfo getServerInfo()
	{
		return mThis.mServerInfo;
	}
	
	
#if BOKU_STORE || USE_MTK_SHOP_BUILD || USE_UMP_R3_BILLING || SHENZHOUFU_STORE || ENABLE_IAP_PSMS_BILLING
	@Override
	public void sendConfirmation()
	{

	    if(InAppBilling.mTransactionStep == WAITING_FOR_GAMELOFT)
		{
			setSMS_TID(InAppBilling.mOrderID);
			mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
			CheckR3PurchaseResult();
		}
		else if (InAppBilling.mTransactionStep == WAITING_FOR_PSMS)
		{
			mCurrentId = InAppBilling.mItemID;
			mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
			#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
			sendPSMSConfirmation(InAppBilling.mOrderID);
			#endif
		}
		#if SHENZHOUFU_STORE
	    else if(InAppBilling.mTransactionStep == WAITING_FOR_SHENZHOUFU)
		{
			mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
			mServerInfo.setBillingMethod(SHENZHOUFU_BILLING);
			mCurrentId = InAppBilling.mItemID;
			ProcessTransactionShenzhoufu(InAppBilling.mOrderID);
		}		
		#endif
		#if BOKU_STORE	
		else
		{
			BokuIABActivity.sendConfirmation();
		}	
		#endif
	}
#endif
	
	@Override
	public boolean requestTransaction(String id) 
	{
		mCurrentId = id;
		INFO(TAG, "requestTransaction Product \'" + mCurrentId+"\'");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "requestTransaction Product \'" + mCurrentId+"\'");
		//if(mServerInfo.setBillingMethod("cc"))//Switch to other billing profile
		if (mXPlayer.getDevice().getServerInfo() == null)
			mXPlayer.getDevice().setServerInfo(mServerInfo);
		if(mServerInfo.searchForDefaultBillingMethod(mCurrentId))//Default Billing Method
		{
			
			((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
				{
				String message;
				public void run ()
				{
					try
					{
					
					#if USE_PHD_PSMS_BILL_FLOW
						String billingPref = InAppBilling.mServerInfo.getItemById(mCurrentId).getType_pref();
						DBG(TAG, "requestTransaction call billingPref: " + billingPref);
						DBG(TAG, "requestTransaction call mItemID: " + InAppBilling.mItemID);
						
						if(billingPref.length() > 0)
						{
							if (billingPref.equals(HTTP_BILLING) || billingPref.equals(SMS_BILLING) || billingPref.equals(PSMS_BILLING))
							{
								//ProcessTransactionHTTP(InAppBilling.mItemID);
								showDialog(DIALOG_PURCHASE_CONFIRMATION, "");
								return;
							}
						}				
					#endif
	
						int billing_count = InAppBilling.mServerInfo.getItemById(mCurrentId).getBillingOptsCount();
						if(billing_count == 1)
						{
							INFO(TAG,"requestTransaction billing type is: \'" + mServerInfo.getBillingType() +"\'");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "requestTransaction billing type is: \'" + mServerInfo.getBillingType() +"\'");
							if (mServerInfo.getBillingType().equals("wap_other"))
							{
								ProcessTransactionWAP(InAppBilling.mItemID);
								return;
							} else if(mServerInfo.getBillingType().equals("wap_paypal"))
							{
								ProcessTransactionWAPPaypal(InAppBilling.mItemID);
								return;
							}else if (mServerInfo.getBillingType().equals(CC_BILLING))
							{
								ProcessTransactionCC(InAppBilling.mItemID);
								return;
							}
						}
						CustomizeDialog customizeDialog;

						if(SUtils.getLManager().GetUserPaymentType()!=-1)
							customizeDialog = new CustomizeDialog(SUtils.getContext(),TYPE_CHOOSE_PAYMENT);
						else 
							customizeDialog = new CustomizeDialog(SUtils.getContext(),TYPE_FIRST_PAYMENT);
						
						customizeDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
						{
							public void onCancel(DialogInterface dialog)
							{
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
													
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
								
								int error = (GlShopErrorCodes.WapOtherErrorCodes.get(GlShopErrorCodes.WAP_OTHER_ERROR_USER_CANCEL)).intValue();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), error);
													
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
						customizeDialog.show();
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
				});
			
			/*if (mServerInfo.getBillingType().equals(HTTP_BILLING))
			{
				INFO(TAG, "Showing Confirmation by HTTP/WAP");	
				//showDialog(DIALOG_CONFIRMATION_HTTP_WAP,id);
				((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
				{
				String message;
				public void run ()
				{
					try
					{
						CustomizeDialog customizeDialog = new CustomizeDialog(SUtils.getContext(),TYPE_FIRST_PAYMENT);
						customizeDialog.show();
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
				});
			}
			else if (mServerInfo.getBillingType().equals(WAP_BILLING))
			{
				INFO(TAG, "Showing Confirmation by HTTP/WAP");	
				showDialog(DIALOG_CONFIRMATION_HTTP_WAP,id);
			}
			else 
			{
				INFO(TAG, "Showing Confirmation by Other Method");
				//showDialog(DIALOG_SELECT_BILLING_METHOD,id);
				((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
				{
				String message;
				public void run ()
				{
					try
					{
						CustomizeDialog customizeDialog = new CustomizeDialog(SUtils.getContext(),TYPE_CHOOSE_PAYMENT);
						customizeDialog.show();
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
				});
			}*/
		}//Default Billing Method
		else//Could not retrive a default billing method
		{
			return (false);
		}
		
		return(true);
	}
	
//	restoreOnlineTransactions() - Restore list of managed contents that the user has already purchased (only valid purchases). Only for contents with Online delivery.
/*
		Response: JSON with status and message
		Success response Code 200
			Possible Messages:
				- "Content delivered!"
				- "There are no online contents for your request"
		Error Response Code 400
			Possible Messages:
			   - "Invalid game parameter. No corresponding product found"
			   - "Missing mandatory parameter"
			   - "Validation of OnlineInfo failed for content:XXXXX"
*/
		public boolean restoreOnlineTransactions ()
	{
		INFO(TAG, ">>>>> [restoreOnlineTransactions] <<<<<");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "start restoreOnlineTransactions");
		
		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();
		Device device 					= new Device();
		StringBuffer sb = new StringBuffer();
		
		sb.append(InAppBilling.GET_STR_CONST(IAB_GAME));sb.append("="); sb.append(GGC_GAME_CODE);sb.append("&"); //game (mandatory)
		sb.append(InAppBilling.GET_STR_CONST(IAB_IMEI));sb.append("=");	sb.append(device.getIMEI());sb.append("&");
		
		String value = InAppBilling.GET_GGLIVE_UID(); //"acnum"
		if (TextUtils.isEmpty(value)) value = "0";
		sb.append(InAppBilling.GET_STR_CONST(IAB_ACNUM));sb.append("=");	sb.append(value);sb.append("&");


		sb.append(InAppBilling.GET_STR_CONST(IAB_CLIENT_ID));sb.append("="); sb.append(CLIENTID);sb.append("&"); //CLIENTID (mandatory)
		
		value = InAppBilling.GET_CREDENTIALS(); //"fed_credentials" (mandatory)
		if (TextUtils.isEmpty(value))
		{
			ERR(TAG, "The value [" + InAppBilling.GET_STR_CONST(IAB_FEDERATION_CREDENTIALS) + "]" + "is mandatory and not available");
			return true;
		}
		value = URLEncoder.encode(value);
		sb.append(InAppBilling.GET_STR_CONST(IAB_FEDERATION_CREDENTIALS));sb.append("=");sb.append(value);sb.append("&");
		
		value = InAppBilling.GET_FED_DATA_CENTER(); //"dc" (mandatory)
		if (TextUtils.isEmpty(value))
		{
			ERR(TAG, "The value [" + InAppBilling.GET_STR_CONST(IAB_FEDERATION_DATA_CENTER) + "]" + "is mandatory and not available");
			return true;
		}
		sb.append(InAppBilling.GET_STR_CONST(IAB_FEDERATION_DATA_CENTER));sb.append("=");sb.append(value);
		
		
		String url = InAppBilling.GET_STR_CONST(IAB_URL_GL_ONLINE_RESTORE_SERVICE);
		String qry = sb.toString();
		
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "GLShop-OnlineRestore");
		LOGGING_APPEND_REQUEST_PAYLOAD(qry, "GLShop-OnlineRestore");
		LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("GLShop-OnlineRestore"));
		
		String jsonHeaders = null;
		JSONObject jObj = new JSONObject();
		#if HDIDFV_UPDATE
		try
		{
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV());//"x-up-gl-hdidfv"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID());//"x-up-gl-gldid"
			jsonHeaders = jObj.toString();
		} catch (JSONException e) 
		{
			ERR(TAG, e.getMessage());
			DBG_EXCEPTION(e);
		}
		#endif
		
		IABRequestHandler.getInstance().doRequestByGet(url, qry, jsonHeaders, new IABCallBack()
		{
			public void runCallBack(Bundle bundle)
			{
				DBG(TAG, "[restoreOnlineTransactions] Callback");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[restoreOnlineTransactions] Callback");
				int response_result = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
				if (response_result == IABRequestHandler.SUCCESS_RESULT)
				{
					String dString = bundle.getString(IABRequestHandler.KEY_RESPONSE);
					try
					{
						JSONObject jsonResponse = new JSONObject(dString);
						int response_code = Integer.parseInt(jsonResponse.getString("code"));
						String response_message = jsonResponse.getString("message");
						DBG(TAG, "[Restore Online Transaction] response: " + response_code);
						DBG(TAG, "[Restore Online Transaction] message: " + response_message);
					}catch(JSONException ex)
					{
						ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
					}
					
					LOGGING_APPEND_RESPONSE_PARAM(dString, "GLShop-OnlineRestore");
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("GLShop-OnlineRestore"));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "GLShop-OnlineRestore: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("GLShop-OnlineRestore") +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO("GLShop-OnlineRestore");
				}
			#if USE_IN_APP_GLOT_LOGGING
				else
				{
					try {
						
						int code = bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
						ERR(TAG,"Waiting for GLShop-Restore failed with error code: "+code );
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for GLShop-Restore failed with error code: "+code );
					} catch (Exception e) {}
				}
			#endif
			}
		});
		
		return true;
	}
	

	
	public boolean restoreTransactions ()
	{
		INFO(TAG, ">>>>> [restoreTransactions] <<<<<");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "start restoreTransactions");
		
		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();
		
		Device device 					= new Device();
		StringBuffer sb = new StringBuffer();
		
		sb.append(InAppBilling.GET_STR_CONST(IAB_GAME));sb.append("="); sb.append(GGC_GAME_CODE);sb.append("&");
		sb.append(InAppBilling.GET_STR_CONST(IAB_IMEI));sb.append("=");	sb.append(device.getIMEI());sb.append("&");
		
		String value = InAppBilling.GET_GGLIVE_UID(); //"acnum"
			if (TextUtils.isEmpty(value)) value = "0";
		sb.append(InAppBilling.GET_STR_CONST(IAB_ACNUM));sb.append("=");	sb.append(value);sb.append("&");
		
		value = InAppBilling.GET_CREDENTIALS(); //"fedcred"
			if (TextUtils.isEmpty(value)) value = "0";
		sb.append(InAppBilling.GET_STR_CONST(IAB_FEDCRED));sb.append("=");sb.append(value);
		
		String url = InAppBilling.GET_STR_CONST(IAB_URL_GL_RESTORE_SERVICE);
		String qry = sb.toString();
		
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "GLShop-Restore");
		LOGGING_APPEND_REQUEST_PAYLOAD(qry, "GLShop-Restore");
		LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("GLShop-Restore"));
		
		String jsonHeaders = null;
		JSONObject jObj = new JSONObject();
		#if HDIDFV_UPDATE
		try
		{
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV());//"x-up-gl-hdidfv"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID());//"x-up-gl-gldid"
			jsonHeaders = jObj.toString();
		} catch (JSONException e) 
		{
			ERR(TAG, e.getMessage());
			DBG_EXCEPTION(e);
		}
		#endif
		
		IABRequestHandler.getInstance().doRequestByGet(url, qry, jsonHeaders, new IABCallBack()
		{
			public void runCallBack(Bundle bundle)
			{
				DBG(TAG, "[restoreTransactions] Callback");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[restoreTransactions] Callback");
				int response = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
				if (response == IABRequestHandler.SUCCESS_RESULT)
				{
					String dString = bundle.getString(IABRequestHandler.KEY_RESPONSE);

					LOGGING_APPEND_RESPONSE_PARAM(dString, "GLShop-Restore");
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("GLShop-Restore"));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "GLShop-Restore: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("GLShop-Restore") +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO("GLShop-Restore");
					
					try {
						int lidx = TextUtils.indexOf(dString,"[");
						int ridx = TextUtils.indexOf(dString,"]");
						
						if (lidx >=0 && ridx > 0 && (ridx - lidx) > 4)//Content Ids came in the form[XXXX]
						{
							String ruids = TextUtils.substring(dString, lidx+1, ridx);
							String [] auids = TextUtils.split(ruids, ",");
							byte[] emptyarray = new String().getBytes();
							
							for (int i = 0; i < auids.length; i++)
							{
								INFO(TAG, "Restoring item id = "+auids[i]);
								LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Restoring item id = "+auids[i]);
								String ItemId = auids[i];
								bundle.clear();
								
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(ItemId!=null)?ItemId.getBytes():emptyarray);
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
						}
					} catch (Exception e) {
						DBG_EXCEPTION(e);
					}
				
				}
			#if USE_IN_APP_GLOT_LOGGING
				else
				{
					try {
						
						int code = bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
						ERR(TAG,"Waiting for GLShop-Restore failed with error code: "+code );
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for GLShop-Restore failed with error code: "+code );
					} catch (Exception e) {}
				}
			#endif
			}
		});
		
		return true;
	}
	
	public static String GetItemId()
	{
	  return (mCurrentId);
	}
	
	static String GetItemDescription()
	{
		return(mThis.mServerInfo.getItemAttribute(mCurrentId,"name"));
	}
	
	static String GetItemPrice()
	{
		return(mThis.mServerInfo.getItemPriceFmt());
	}
	
	static String getStringFormated(int index, String rx, String rp)
	{
		return getStringFormated(SUtils.getContext().getString(index), rx, rp);
	}

	static String getStringFormated(String src, String rx, String rp)
	{
		String result = src;
		if (rx == null)
		{
			rx = "{SIZE}";
		}
		
		if (rx.equals("{PRICE}"))
			result = result.replace(rx, rp);
		else
			result = result.replace(rx, rp);
		
		return result;
	}
	
	public void ProcessTransactionSMS(final String id)
	{
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		mXPlayer.setDataCenter(InAppBilling.GET_FED_DATA_CENTER());

		new Thread( new Runnable()
		{
			public void run ()
			{
			  try
			  {
				DBG(TAG,"ProcessTransactionSMS Started");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "ProcessTransactionSMS Started");

				int MAX_RETRY = 5;
				int MAX_TOKEN_RETRY = 3;
				int current_attempt = 1;
				int current_token_attempt = 1;
				boolean keep_verifing = true;
				boolean got_token = false;
				String FM_SMS_ID="";
				
				final int BILLING_SUCCESS = 0;
				final int BILLING_SUCCESS_OFFLINE = 1;
				final int BILLING_PENDING = 2;
				final int BILLING_FAILURE = 3;


				
				DBG(TAG,"Waiting for Ecommerce Token");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Waiting for Ecommerce Token");
				while((!got_token) && (current_token_attempt<=MAX_TOKEN_RETRY))
				{
					//Retrieve Token from Ecommerce
					mXPlayer.sendPSMSTokenRequest(mCurrentId);
					DBG(TAG,"Waiting for token response on attempt: "+current_token_attempt);
					while (!mXPlayer.handlePSMSTokenRequest())
					{
						try {
							Thread.sleep(50);
						} catch (Exception exc) {}
					} 
					
					if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
					{
						try {
							Thread.sleep(2000);
						} catch (Exception exc) {}
						current_token_attempt++;

						if(current_token_attempt>MAX_TOKEN_RETRY)
						{
							DBG(TAG,"MAX TOKEN attempts reached (current_token_attempt="+current_token_attempt+", finished with error!");
							DBG(TAG,"XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "MAX TOKEN attempts reached (current_token_attempt="+current_token_attempt+", finished with error!");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
							
							//// Error codes ////
							int errorCode = (GlShopErrorCodes.SMSRegisterMOErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
							String errorCodeString = XPlayer.getLastErrorCodeString();
							ERR(TAG,"*** errorCode: " + errorCode);
							ERR(TAG,"*** errorCodeString: " + errorCodeString);
			
							showDialog(DIALOG_PURCHASE_FAIL,id,errorCode,errorCodeString);
						}
					}
					else
					{
						DBG(TAG,"XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
						got_token = true;
						FM_SMS_ID=XPlayer.getLastErrorCodeString();
					}
				}//while(!got_token)

				
				DBG(TAG,"Finished with got_token="+got_token);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Finished with got_token="+got_token);
				if(got_token)
				{
					//Send SMS				
					SMS mSMS = new SMS(mDevice);
					if (android.os.Build.VERSION.SDK_INT >= 19)
						mSMS.SendPurchaseMessageEncrypted(GetItemId(),FM_SMS_ID);
					else
						mSMS.SendPurchaseMessage(GetItemId(),FM_SMS_ID);

					DBG(TAG,"Sending SMS STARTED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Sending SMS STARTED");
					while(!mSMS.isCompleted() && !mSMS.isFail()){}

					if(mSMS.isFail())
					{
						//// Error codes ////
						int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(GlShopErrorCodes.SMS_CHECK_MESSAGE_FAILED)).intValue();
						String errorCodeString = null;//error sending SMS
						ERR(TAG,"*** errorCode: " + errorCode);
						ERR(TAG,"*** errorCodeString: " + errorCodeString);
							
						showDialog(DIALOG_PURCHASE_FAIL,id,errorCode,errorCodeString);
					}
					else
					{					
					while((keep_verifing) && (current_attempt<=MAX_RETRY))
					{
						DBG(TAG,"Waiting for checkbilling response on attempt: "+current_attempt);
						
						mXPlayer.sendPSMSPurchaseRequest(mCurrentId,FM_SMS_ID);
						
						while (!mXPlayer.handlePSMSPurchaseRequest())
						{
							try {
								Thread.sleep(50);
							} catch (Exception exc) {}
						}
						
						DBG(TAG,"Response Received for checkbilling on attempt: "+current_attempt);
						
						if((XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)&&(XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT))
						{
							keep_verifing = false;
							DBG(TAG,"FAILURE RECEIVED finished with error!");
							WARN(TAG,"XPlayer.getLastErrorCode: "+XPlayer.getLastErrorCode());
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "FAILURE RECEIVED finished with error, code:"+XPlayer.getLastErrorCode());
							try {
								Thread.sleep(2000);
								} catch (Exception exc) {}
								
							//// Error codes ////
							int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
							String errorCodeString = XPlayer.getLastErrorCodeString();
							ERR(TAG,"*** errorCode: " + errorCode);
							ERR(TAG,"*** errorCodeString: " + errorCodeString);
							
							showDialog(DIALOG_PURCHASE_FAIL,id,errorCode,errorCodeString);
						}
						else if (XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT)
						{
							if(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING)))
							{
								DBG(TAG,"Retry due PENDING");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Retry due PENDING");
								current_attempt++;
								if(current_attempt>MAX_RETRY)
								{
									DBG(TAG,"MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
									LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
									keep_verifing = false;
									
									//// Error codes ////
									int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
									String errorCodeString = XPlayer.getLastErrorCodeString();
									ERR(TAG,"*** errorCode: " + errorCode);
									ERR(TAG,"*** errorCodeString: " + errorCodeString);
							
									showDialog(DIALOG_PURCHASE_FAIL,id,errorCode,errorCodeString);
								}
								try {
								Thread.sleep(2000);
								} catch (Exception exc) {}
							}
							else if(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)) ||
									XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS_OFFLINE)))
							{
								DBG(TAG,"Finished with SUCCESS");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Finished with SUCCESS");
								keep_verifing = false;
								
								String TxId = XPlayer.getLastEComTxId();
								XPlayer.clearLastEComTxId();
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
													
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
													
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
								SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
														
														
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
						}
					}//while(keep_verifing)
					}
				}
			  }
			  catch(Exception EX)
			  {}
			}
		}
	#if !RELEASE_VERSION
		,"Thread-SilentSMSTransaction"
	#endif
		).start();
		
	}

#if USE_UMP_R3_BILLING
public void ProcessTransactionUMP_R3(final String id)
{
	showDialog(DIALOG_UMP_R3_PAYMENT_INFO,id);
}
#endif	
	
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING

	public void sendPSMSBillingConfirmation()
	{
		DBG(TAG,"STARTING CONFIRMATION!!!");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "sendPSMSBillingConfirmation - STARTING CONFIRMATION!");
			try
			{
				new Thread( new Runnable()
				{
					public void run ()
					{
						int confirm_counter=0;
						final int MAX_CONFIRM_REACHED = 3;
						boolean isConfirmed =false;

						mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());

						while((!isConfirmed) && (confirm_counter<MAX_CONFIRM_REACHED))
						{
							mXPlayer.sendPSMSConfirmationRequest(InAppBilling.mItemID, InAppBilling.mOrderID,Encrypter.decrypt(SUCCESS_RESULT).toLowerCase());
							while (!mXPlayer.handlePSMSConfirmationRequest())
							{
								try {
									Thread.sleep(50);
								} catch (Exception exc) {}
							} 
							
							if(mXPlayer.getLastErrorCode()!=mXPlayer.ERROR_NONE)
							{
								try {
									Thread.sleep(2000);
								} catch (Exception exc) {}
								confirm_counter++;

								if(confirm_counter>=MAX_CONFIRM_REACHED)
								{
									DBG(TAG,"COONFIRMATION NOT PERFORMED!");
									DBG(TAG,"MAX Confirmations reached");
									DBG(TAG,"XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
									LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "COONFIRMATION NOT PERFORMED!");
									LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX Confirmations reached");
									LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
									isConfirmed = true;
									setSMSResult("");
								}
							}
							else
							{
								DBG(TAG,"COONFIRMATION DONE!");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "CONFIRMATION DONE!");
								isConfirmed = true;
								setSMSResult("");
								
							}
						}//while(!isConfirmed)
					}
				}
				#if !RELEASE_VERSION
				,"Thread-PSMSConfirmation"
				#endif
				).start();
			}catch(Exception ex){}
	}

	public void setSMSResult(String value)
	{
		SUtils.getLManager().SetSMSResult(value);
	}
	
	public String getSMSResult()
	{
		return (SUtils.getLManager().GetSMSResult());
	}

	public void ProcessTransactionPSMS(final String id)
	{
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		mXPlayer.setDataCenter(InAppBilling.GET_FED_DATA_CENTER());

		new Thread( new Runnable()
		{
			public void run ()
			{
			  try
			  {
				DBG(TAG,"ProcessTransactionPSMS Started");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "ProcessTransactionPSMS Started");

				int MAX_RETRY = 4;
				int MAX_TOKEN_RETRY = 3;
				int current_attempt = 1;
				int current_token_attempt = 1;
				boolean keep_verifing = true;
				boolean got_token = false;
				String FM_SMS_ID="";
				
				final int BILLING_SUCCESS = 0;
				final int BILLING_SUCCESS_OFFLINE = 1;
				final int BILLING_PENDING = 2;
				final int BILLING_FAILURE = 3;


				
				DBG(TAG,"Waiting for Ecommerce Token");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Waiting for Ecommerce Token");
				while((!got_token) && (current_token_attempt<=MAX_TOKEN_RETRY))
				{
					//Retrieve Token from Ecommerce
					mXPlayer.sendPSMSTokenRequest(mCurrentId);
					DBG(TAG,"Waiting for token response on attempt: "+current_token_attempt);
					while (!mXPlayer.handlePSMSTokenRequest())
					{
						try {
							Thread.sleep(50);
						} catch (Exception exc) {}
					} 
					
					if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
					{
						try {
							Thread.sleep(2000);
						} catch (Exception exc) {}
						current_token_attempt++;

						if(current_token_attempt>MAX_TOKEN_RETRY)
						{
							DBG(TAG,"MAX TOKEN attempts reached (current_token_attempt="+current_token_attempt+", finished with error!");
							DBG(TAG,"XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "MAX TOKEN attempts reached (current_token_attempt="+current_token_attempt+", finished with error!");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
							
							//// Error codes ////
							int errorCode = (GlShopErrorCodes.SMSRegisterMOErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
							String errorCodeString = XPlayer.getLastErrorCodeString();
							ERR(TAG,"*** errorCode: " + errorCode);
							ERR(TAG,"*** errorCodeString: " + errorCodeString);
			
							showDialog(DIALOG_PURCHASE_FAIL,id,errorCode,errorCodeString);
						}
					}
					else
					{
						DBG(TAG,"XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
						got_token = true;
						FM_SMS_ID=XPlayer.getLastErrorCodeString();
					}
				}//while(!got_token)
				
				
				DBG(TAG,"Finished with got_token="+got_token);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Finished with got_token="+got_token);
				if(got_token)
				{
					//Send SMS				
					SMS mSMS = new SMS(mDevice);
#if	USE_PHD_PSMS_BILL_FLOW
					mSMS.SendPurchaseMessageEncrypted(GetItemId(),FM_SMS_ID);
#else
					if (android.os.Build.VERSION.SDK_INT >= 19)
						mSMS.SendPurchaseMessageEncrypted(GetItemId(),FM_SMS_ID);
					else
						mSMS.SendPurchaseMessage(GetItemId(),FM_SMS_ID);
#endif
					DBG(TAG,"Sending SMS STARTED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Sending SMS STARTED");
					while(!mSMS.isCompleted() && !mSMS.isFail()){
						try{ Thread.sleep(10);}catch(Exception ex){}
					}

					if (mSMS.isFail())
					{
						//// Error codes ////
						int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(GlShopErrorCodes.SMS_CHECK_MESSAGE_FAILED)).intValue();
						String errorCodeString = null;//error sending SMS
						ERR(TAG,"*** errorCode: " + errorCode);
						ERR(TAG,"*** errorCodeString: " + errorCodeString);
			
						showDialog(DIALOG_PURCHASE_FAIL_PSMS,id,errorCode,errorCodeString);
						keep_verifing = false;
					} else
					{
							
						InAppBilling.mOrderID = FM_SMS_ID;
						InAppBilling.save(WAITING_FOR_PSMS);
						InAppBilling.saveLastItem(WAITING_FOR_PSMS);
					}
					while((keep_verifing) && (current_attempt<=MAX_RETRY))
					{
						DBG(TAG,"Waiting for checkbilling response on attempt: "+current_attempt);
						mXPlayer.sendPSMSPurchaseRequest(mCurrentId,FM_SMS_ID);
						
						while (!mXPlayer.handlePSMSPurchaseRequest())
						{
							try {
								Thread.sleep(50);
							} catch (Exception exc) {}
						}
						
						DBG(TAG,"Response Received for checkbilling on attempt: "+current_attempt);						
						if((XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)&&(XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT))
						{
							keep_verifing = false;
							DBG(TAG,"FAILURE RECEIVED finished with error!");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "FAILURE RECEIVED finished with error, code:"+XPlayer.getLastErrorCode());
							try {
								Thread.sleep(2000);
								} catch (Exception exc) {}
							
							InAppBilling.saveLastItem(STATE_FAIL);
							InAppBilling.mTransactionStep = 0;
							InAppBilling.clear();							

							//// Error codes ////
							int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
							String errorCodeString = XPlayer.getLastErrorCodeString();
							ERR(TAG,"*** errorCode: " + errorCode);
							ERR(TAG,"*** errorCodeString: " + errorCodeString);
							showDialog(DIALOG_PURCHASE_FAIL_PSMS,id,errorCode,errorCodeString);
						}
						else if (XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT)
						{
							if(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING)))
							{
								DBG(TAG,"Retry due PENDING");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Retry due PENDING");
								current_attempt++;
								if(current_attempt>MAX_RETRY)
								{
									DBG(TAG,"MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
									LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
									keep_verifing = false;
									showDialog(DIALOG_PENDING_PSMS,id);
									Bundle bundle = new Bundle();
									bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
														
									bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
									bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
									bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
									bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
									
									//// Error codes ////
									int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
									String errorCodeString = XPlayer.getLastErrorCodeString();
									ERR(TAG,"*** errorCode: " + errorCode);
									ERR(TAG,"*** errorCodeString: " + errorCodeString);
									
									bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), (errorCode));
									bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(errorCodeString!=null)?errorCodeString.getBytes():null);
									/////////////////////
														
									bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
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
									
									sendPSMSConfirmation(FM_SMS_ID);
								}
								try {
								Thread.sleep(3000);
								} catch (Exception exc) {}
							}
							else if(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)) ||
									XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS_OFFLINE)) ||
									getSMSResult().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)))
							{
								//Send a MT received confirmation to GLoft
								if(getSMSResult().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)))
								{
									sendPSMSBillingConfirmation();
								}
								
								DBG(TAG,"Finished with SUCCESS");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Finished with SUCCESS");
								keep_verifing = false;
								InAppBilling.saveLastItem(STATE_SUCCESS);
								InAppBilling.clear();
								setSMSResult("");
								
								String TxId = XPlayer.getLastEComTxId();
								XPlayer.clearLastEComTxId();

								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
													
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
													
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
								SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
														
														
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
						}
					}
				}
			  }
			  catch(Exception EX)
			  {}
			}
		}
	#if !RELEASE_VERSION
		,"Thread-PSMSTransaction"
	#endif
		).start();
		
	}
	#define DELAY_TIME_FOR_GL_RETRY (1*60*1000)
#if RELEASE_VERSION	
	#define MAX_WAIT_TIME_FOR_PENDING 	(24*60*60*1000) //24 horas
#else	
	#define MAX_WAIT_TIME_FOR_PENDING 	(3*60*1000)
#endif

	static int [] DELAY_BETWEEN_PENDING_RETRYS = {	1*60*1000,
													3*60*1000,
													6*60*1000,
													10*60*1000,
													15*60*1000,
													30*60*1000,
													60*60*1000
												 };
	#define PENDING_DELAYS_LAST_POSS 	6 //Total of elements in previous array
													
	private static boolean mIsConfirmInProgress = false;
	private void sendPSMSConfirmation(final String FM_SMS_ID)
	{
		
		if (mServerInfo == null)
			mServerInfo = GLOFTHelper.getServerInfo();
		if (mIsConfirmInProgress)
			return;
	
		new Thread( new Runnable()
		{
			public void run()
			{
				//Looper.prepare();
				boolean finish = false;
				mIsConfirmInProgress = true;
				int pendingRetry = 0;
				final int BILLING_SUCCESS = 0;
				final int BILLING_SUCCESS_OFFLINE = 1;
				final int BILLING_PENDING = 2;
				final int BILLING_FAILURE = 3;
				
				do 
				{
					try
					{
						INFO(TAG,"sendPSMSPurchaseRequest for: InAppBilling.mCharId: "+InAppBilling.mCharId+" InAppBilling.mCharRegion: "+InAppBilling.mCharRegion);
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "sendPSMSPurchaseRequest for: InAppBilling.mCharId: "+InAppBilling.mCharId+" InAppBilling.mCharRegion: "+InAppBilling.mCharRegion);
						
						mXPlayer.sendPSMSPurchaseRequest(mCurrentId,FM_SMS_ID);

						long time = 0;
						while (!mXPlayer.handlePSMSPurchaseRequest())
						{
							try {
								Thread.sleep(50);
							} catch (Exception exc) {}
							
							if((System.currentTimeMillis() - time) > 1500)
							{
								DBG(TAG, "[sendPSMSPurchaseRequest]Waiting for response");
								time = System.currentTimeMillis();
							}
						}
						if (XPlayer.getLastErrorCode() == XPlayer.ERROR_INIT)
						{
							try {
								Thread.sleep(50);
							} catch (Exception exc) {}
						}
						else if (XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)) ||
								 XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS_OFFLINE)) ||
								 getSMSResult().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)))
						{
							//Send a MT received confirmation to GLoft
							if(getSMSResult().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)))
							{
								sendPSMSBillingConfirmation();
							}

							//Informing the game that the buy item proccess is completed and success.
							finish = true;
							mIsConfirmInProgress = false;
							InAppBilling.saveLastItem(STATE_SUCCESS);
							InAppBilling.clear();
							setSMSResult("");
							{
								String TxId = XPlayer.getLastEComTxId();
								XPlayer.clearLastEComTxId();

								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
							
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
								
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
								SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
									
								try{
									Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
									Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
									bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
								}catch(Exception ex ) {
									ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
								}
							}	
						}
						else if (InAppBilling.mTransactionStep == WAITING_FOR_PSMS && XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING)))
						{
							if (InAppBilling.IsInternetAvaliable())
							{
								String strTime = SUtils.getPreferenceString(GES(IAB_STR_WAITTING_TIME), GES(IAB_STR_PREF_NAME));
								if(strTime != null)
								{
									long savedTime = Long.parseLong(strTime);
									long currentTime = System.currentTimeMillis();
									
									if (currentTime - savedTime > MAX_WAIT_TIME_FOR_PENDING)
									{
										DBG(TAG, "Timmer ends, clearing pending status");
										LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Timmer ends, clearing pending status");
										InAppBilling.saveLastItem(STATE_FAIL);
										InAppBilling.mTransactionStep = 0;
										InAppBilling.clear();
										
										//// Error codes ////
										int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
										String errorCodeString = XPlayer.getLastErrorCodeString();
										ERR(TAG,"*** errorCode: " + errorCode);
										ERR(TAG,"*** errorCodeString: " + errorCodeString);
							
										showDialog(DIALOG_PURCHASE_FAIL_PSMS, GetItemId(),errorCode,errorCodeString);
										mIsConfirmInProgress = false;
										finish = true;
									}
									else 
									{
										JDUMP(TAG,DELAY_BETWEEN_PENDING_RETRYS[pendingRetry]);
										try { 
											Thread.sleep(DELAY_BETWEEN_PENDING_RETRYS[pendingRetry++]);
										} catch (Exception exc) {}
										if (pendingRetry == PENDING_DELAYS_LAST_POSS)
											--pendingRetry;
										continue;
									}
								}else
								{
									DBG(TAG,"no time information found");
									LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Timmer ends, clearing pending status");
								}
							}
						}
						else if (XPlayer.getLastErrorCode() != XPlayer.ERROR_NONE)
						{
							DBG(TAG, "Timmer ends, clearing pending status");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Timmer ends, clearing pending status");
							InAppBilling.saveLastItem(STATE_FAIL);
							InAppBilling.mTransactionStep = 0;
							InAppBilling.clear();

							//// Error codes ////
							int errorCode = (GlShopErrorCodes.SMSCheckErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
							String errorCodeString = XPlayer.getLastErrorCodeString();
							ERR(TAG,"*** errorCode: " + errorCode);
							ERR(TAG,"*** errorCodeString: " + errorCodeString);
							showDialog(DIALOG_PURCHASE_FAIL_PSMS, GetItemId(),errorCode,errorCodeString);
							mIsConfirmInProgress = false;
							finish = true;
						}

					}catch(Exception exd)
					{
						DBG_EXCEPTION(exd);
						DBG(TAG,"No internet avaliable");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "No internet avaliable");
					}
					try{
						Thread.sleep(DELAY_TIME_FOR_GL_RETRY);
					}catch(Exception ex){};
				}while (!finish);
				//Looper.loop();
			}
		}
	#if !RELEASE_VERSION
		,"Thread-PSMSConfirmation"
	#endif
		).start();
	}

#endif//USE_MTK_BUILD_SHOP
	
	public void ProcessTransactionHTTP(String id) 
	{
		//mCurrentId = id;
				
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());

		mXPlayer.setDataCenter(InAppBilling.GET_FED_DATA_CENTER());
		mXPlayer.sendHTTPPurchaseRequest(mCurrentId);
		
		while (!mXPlayer.handleHTTPPurchaseRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		
		if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
		{
			int errorCode = (GlShopErrorCodes.PhoneBillErrorCodes.get(XPlayer.getLastErrorCode())).intValue();
			String errorCodeString = XPlayer.getLastErrorCodeString();
			ERR(TAG,"*** errorCode: " + errorCode);
			ERR(TAG,"*** errorCodeString: " + errorCodeString);
		
			if(!XPlayer.getLastErrorCodeString().equals(SUCESS_CODE) && !XPlayer.getLastErrorCodeString().equals(SUCESS_PENDING_CODE))
				//ALL ERRORS HERE!!!
				showDialog(DIALOG_PURCHASE_FAIL,id,errorCode,errorCodeString);
		}
		else
		{
			if(XPlayer.getLastErrorCodeString().equals(SUCESS_PENDING_CODE))
			{
				showDialog(DIALOG_PURCHASE_SUCCESS_PENDING,id);
				SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
			}
			else
			{
					String TxId = XPlayer.getLastEComTxId();
					XPlayer.clearLastEComTxId();
					
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
										
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
					SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
											
											
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
		}

		if(GLOFTHelper.GetXPlayer().getDevice().getServerInfo().getIs3GOnly().equals("1"))
			GLOFTHelper.GetInstance().restoreWifiConnection();
	}
	
	public void ProcessTransactionCC(String id)
	{
		INFO(TAG, "Init Activity for CC");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Init Activity for CC");
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		if(SUtils.getLManager().PreviousPurchase())
		{
			new Thread(new Runnable() {
							public void run() {
								ProcessTransactionCCPreviousPurchase(GetItemId());
							}
							}).start();
		}
		else
		{
			SetBillingType(BT_CREDIT_CARD);
			StartBillingActivity();
		}
	}
	
	public void ProcessTransactionCCPreviousPurchase(String id) 
	{
		
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		mXPlayer.sendCCPurchaseRequest(mCurrentId);
		
		while (!mXPlayer.handleCCPurchaseRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		
		if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
		{
			if(!XPlayer.getLastErrorCodeString().equals(SUCESS_CODE)&& !XPlayer.getLastErrorCodeString().equals(SUCESS_PENDING_CODE))
				//ALL ERRORS HERE!!!
				showDialog(DIALOG_PURCHASE_FAIL,id);
		}
		else
		{
			if(XPlayer.getLastErrorCodeString().equals(SUCESS_PENDING_CODE))
			{
				showDialog(DIALOG_PURCHASE_SUCCESS_PENDING,id);
				SUtils.getLManager().SaveUserPaymentType(LManager.CREDIT_CARD_PAYMENT);
			}
			else
			{
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
										
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
					SUtils.getLManager().SaveUserPaymentType(LManager.CREDIT_CARD_PAYMENT);
											
											
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
		}
		
	}
	
	public void ProcessTransactionWAP(String id)
	{
	  INFO(TAG, "Init Activity for WAP");
	  LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Init Activity for WAP");
	  SetBillingType(BT_WAP_BILLING);
	  StartBillingActivity();
	}

	public void ProcessTransactionWAPPaypal(String id)
	{
		INFO(TAG, "Init Activity for WAP Paypal");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Init Activity for WAP Paypal");
		SetBillingType(BT_WAP_PAYPAL_BILLING);
	  	StartBillingActivity();
	}

	
	private void SetBillingType(int type)
	{
		mBillingType = type;
	}
	
	static int GetBillingType()
	{
		return (mBillingType);
	}
	
	private void StartBillingActivity()
	{
		try
		{
			Intent intentBilling= new Intent(SUtils.getContext(), InAppBillingActivity.class);
			//((Activity)SUtils.getContext()).startActivity(intentBilling);
			SUtils.getContext().startActivity(intentBilling);

		}catch(Exception EX){}
	}

#if BOKU_STORE	
	public boolean ProcessTransactionBOKU(String id)
	{
		return(BokuIABActivity.LaunchBokuBilling(id, getServerInfo()));
	}
#endif

#if SHENZHOUFU_STORE

	private Dialog createShenzhoufuDialog(int titleId, String message, DialogInterface.OnClickListener dil, boolean isCancelable, int btnTextId)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		
			builder.setTitle(titleId)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(message)
			.setCancelable(isCancelable)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					new Thread(new Runnable() {
					public void run() {
						ProcessTransactionShenzhoufu(InAppBilling.mOrderID);
					}
					}).start();
				}
			})
			.setPositiveButton(btnTextId, dil);
		return builder.create();
	}
	
	private Dialog createShenzhoufuPendingPurchaseFailDialog(int titleId, int messageId, DialogInterface.OnClickListener dil) 
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
			})
			.setPositiveButton(R.string.IAB_SKB_OK, dil);
		return builder.create();
	}
	
	
	public boolean startShenzhoufuTransaction(String id)
	{
		INFO(TAG, "Init Activity for Shenzhoufu");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Init Activity for Shenzhoufu");
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());

		SetBillingType(BT_SHENZHOUFU_BILLING);
		StartBillingActivity();
		return false;
	}
	
	public void ProcessTransactionShenzhoufu(String orderId) 
	{
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());

				int MAX_RETRY = 5;
				int current_attempt = 1;
				boolean keep_verifing = true;
				final int BILLING_SUCCESS = 0;
				final int BILLING_SUCCESS_OFFLINE = 1;
				final int BILLING_PENDING = 2;
				final int BILLING_FAILURE = 3;
				String transactionID=orderId;//transaction id
				
				//start Billing result request
				InAppBilling.mItemID = mCurrentId;
				InAppBilling.saveLastItem(WAITING_FOR_SHENZHOUFU);
				InAppBilling.mOrderID = transactionID;
				InAppBilling.save(WAITING_FOR_SHENZHOUFU);
				
				while((keep_verifing) && (current_attempt<=MAX_RETRY))
				{
					DBG(TAG,"Waiting for check billing result  on attempt: "+current_attempt);
					mXPlayer.sendShenzhoufuBillingResultRequest(mCurrentId,transactionID);
					
					while (!mXPlayer.handleShenzhoufuBillingResultRequest())
					{
						try {
							Thread.sleep(50);
						} catch (Exception exc) {}
					}
					
					DBG(TAG,"Response Received for check billing result on attempt: "+current_attempt);		

					if (XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT)
					{
						if( XPlayer.getLastErrorCode() == XPlayer.ERROR_CONNECTION)
						{
							DBG(TAG,"Error due to CONNECTION problem");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error due to CONNECTION problem");
							keep_verifing = false;
							current_attempt = MAX_RETRY + 1;
							// showDialog(DIALOG_PENDING_SHENZHOUFU,mCurrentId);
							
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
															
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_PENDING);
							// bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);

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
						else if( XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING)) ||
							XPlayer.getLastErrorCodeString().equals("FAILURE"))
						{
							DBG(TAG,"Retry due PENDING or FAIL");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Retry due PENDING or FAIL");
							current_attempt++;
							if(current_attempt>MAX_RETRY)
							{
								DBG(TAG,"MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
								keep_verifing = false;
								
								//if the max attempst is reached, set as FAIL
								InAppBilling.saveLastItem(STATE_FAIL);
								InAppBilling.mTransactionStep = 0;
								InAppBilling.clear();							
								// showDialog(DIALOG_PENDING_PURCHASE_FAIL_SHENZHOUFU,mCurrentId);
								
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
																
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);

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
							try {
							Thread.sleep(5000);
							} catch (Exception exc) {}
						}
						else if(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS)) ||
								XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_SUCCESS_OFFLINE)) )
						{
							DBG(TAG,"Finished with SUCCESS");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Finished with SUCCESS");
							keep_verifing = false;
							InAppBilling.saveLastItem(STATE_SUCCESS);
							InAppBilling.clear();

							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
												
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
							SUtils.getLManager().SaveUserPaymentType(LManager.SHENZHOUFU_PAYMENT);

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
						else //All other responses
						{
							current_attempt++;
							if(current_attempt>MAX_RETRY)
							{
								DBG(TAG,"MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
								keep_verifing = false;
								
								//if the max attempts is reached, set as FAIL
								InAppBilling.saveLastItem(STATE_FAIL);
								InAppBilling.mTransactionStep = 0;
								InAppBilling.clear();							
								// showDialog(DIALOG_PENDING_PURCHASE_FAIL_SHENZHOUFU,mCurrentId);
								
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
																
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);

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
							try {
							Thread.sleep(5000);
							} catch (Exception exc) {}
						}
					}
					else //All other responses
						{
							current_attempt++;
							if(current_attempt>MAX_RETRY)
							{
								DBG(TAG,"MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
								keep_verifing = false;
								
								//if the max attempts is reached, set as FAIL
								InAppBilling.saveLastItem(STATE_FAIL);
								InAppBilling.mTransactionStep = 0;
								InAppBilling.clear();							
								// showDialog(DIALOG_PENDING_PURCHASE_FAIL_SHENZHOUFU,mCurrentId);
								
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
																
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);

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
							try {
							Thread.sleep(5000);
							} catch (Exception exc) {}
						}
				}
	}
	
#endif

	@Override
	public void showDialog(final int idx, final String id) 
	{
		showDialog(idx, id, XPlayer.ERROR_INIT, null);
	}
	
	public void showDialog(final int idx, final String id, final int errorCode, final String errorCodeString)
	{
		
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			String message;
			public void run ()
			{
				try
				{
					INFO(TAG,"ShowDialog"+idx);
					Dialog dialog = null;
					DialogInterface.OnClickListener dil = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
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
					};
						DialogInterface.OnClickListener FAILl = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							
							//// Error codes ////
							WARN(TAG,"*** Adding error codes (showDialog) ***");
							WARN(TAG,"*** errorCode: " + errorCode);
							WARN(TAG,"*** errorCodeString: " + errorCodeString);
							
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), (errorCode));
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(errorCodeString!=null)?errorCodeString.getBytes():null);
							/////////////////////
							
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
													
													
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
					};
					DialogInterface.OnClickListener OKl = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
												
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
													
													
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
					};
					
						DialogInterface.OnClickListener PENDINGl = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(id!=null)?id.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
												
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
					};
					
					DialogInterface.OnClickListener SUCCESSPENDING = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(id!=null)?id.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
												
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
													
													
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
					};
					
					DialogInterface.OnClickListener CONFIRM_INFO = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							SendUMP_SMS1(id);
						}
					};

					#if SHENZHOUFU_STORE
					DialogInterface.OnClickListener CONFIRM_SHENZHOUFU_PENDING = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							// startShenzhoufuTransaction(id);
							new Thread(new Runnable() {
							public void run() {
								ProcessTransactionShenzhoufu(id);
							}
							}).start();
							// ProcessTransactionShenzhoufu(InAppBilling.mOrderID);
						}
					};
					#endif
					
					DialogInterface.OnClickListener CONFIRM_PAYMENT = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							SendUMP_SMS2(id);
						}
					};

					DialogInterface.OnClickListener PAYMENT_FINISH = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							InAppBilling.saveLastItem(STATE_SUCCESS);
							InAppBilling.clear();
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(id!=null)?id.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
												
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
													
													
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
					};

					switch (idx) {
						case DIALOG_CANNOT_CONNECT_ID:
							dialog = createDialog(R.string.IAB_NETWORK_ERROR_TITLE, R.string.IAB_NETWORK_ERROR, dil, false,false);
						break;
						
						case DIALOG_PURCHASE_FAIL:
						#if USE_PHD_PSMS_BILL_FLOW
							dialog = createPurchaseFailDialog(R.string.IAB_PURCHASE_ITEM_FAILURE_TITLE, R.string.IAB_PURCHASE_ITEM_FAILURE_TRY_AGAIN, FAILl, errorCode, errorCodeString);
						#else
							dialog = createPurchaseFailDialog(R.string.IAB_PURCHASE_ITEM_FAILURE_TITLE, R.string.IAB_PURCHASE_ITEM_FAILURE, FAILl, errorCode, errorCodeString);
						#endif
						break;
						
						case DIALOG_PURCHASE_SUCCESS:
							dialog = createDialog(R.string.IAB_PURCHASE_ITEM_SUCCESS_TITLE, R.string.IAB_PURCHASE_ITEM_SUCCESS, OKl, false,false);
						break;
						
						case DIALOG_CONFIRMATION_HTTP_WAP:
							//{QUANTITY} {ITEM} {PRICE}
							message = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_HTTP_WAP,"{ITEM}",mServerInfo.getItemAttribute(id,"name"));
							message = getStringFormated(message,"{PRICE}",mServerInfo.getItemPriceFmt());
							dialog = createYESNODialog(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_HTTP_WAP_TITLE, message, dil, false);
						break;
						
						case DIALOG_CONFIRMATION_CC:
							message = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_LAYOUT,"{ITEM}",mServerInfo.getItemAttribute(id,"name"));
							message = getStringFormated(message,"{PRICE}",mServerInfo.getItemPriceFmt());
							dialog = createYESNODialog(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_TITLE, message, dil, false);
						break;
						
						case DIALOG_PURCHASE_SUCCESS_PENDING:
							dialog = createDialogSUCCESS(R.string.IAB_PURCHASE_ITEM_SUCCESS_PENDING_TITLE, R.string.IAB_PURCHASE_ITEM_SUCCESS_PENDING, SUCCESSPENDING);
						break;
						
						case DIALOG_PURCHASE_RESULT_PENDING:
							dialog = createPendingDialog(R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING_TITLE, R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING, PENDINGl,id);
						break;
						
						case DIALOG_TERMS_AND_CONDITIONS:
							String mTCSText = mServerInfo.getTNCString();
							mTCSText = getStringFormated(mTCSText,"<currency>", "");
							mTCSText = getStringFormated(mTCSText,"<price>",mServerInfo.getItemPriceFmt());
							dialog = createTnCDialog(R.string.IAB_SKB_TCS, mTCSText);
						break;
						case DIALOG_VERIFY_WAP_PURCHASE:
							dialog = createValidateWapDialog();
						break;
					#if USE_PHD_PSMS_BILL_FLOW
						case DIALOG_PURCHASE_CONFIRMATION:
							dialog = createConfirmationDialog(errorCode, errorCodeString);
						break;
					#endif	
					#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
						case DIALOG_PENDING_PSMS:
							dialog = createDialog(R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING_TITLE, R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING_PSMS, PENDINGl, false,false);
						break;
						
						case DIALOG_PURCHASE_FAIL_PSMS:
							dialog = createPurchaseFailDialog(R.string.IAB_PURCHASE_ITEM_FAILURE_TITLE, R.string.IAB_PURCHASE_ITEM_FAILURE_PSMS, FAILl, errorCode, errorCodeString);
						break;
					#endif
					#if SHENZHOUFU_STORE
						case DIALOG_PENDING_SHENZHOUFU:
							mServerInfo.setBillingMethod(InAppBilling.GET_STR_CONST(IAB_SHENZHOUFU));
							// dialog = createDialog(R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING_TITLE, R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING_SHENZHOUFU, PENDINGl, false,false);
							dialog = createShenzhoufuDialog(R.string.IAB_SHENZHOUFU_PENDING_TITLE, SUtils.getContext().getString(R.string.IAB_SHENZHOUFU_CONFIRM_PENDING_TRANSACTION), CONFIRM_SHENZHOUFU_PENDING,true,R.string.IAB_SKB_OK);
						break;
						case DIALOG_PENDING_PURCHASE_FAIL_SHENZHOUFU:
							dialog = createShenzhoufuPendingPurchaseFailDialog(R.string.IAB_PURCHASE_ITEM_FAILURE_TITLE, R.string.IAB_SHENZHOUFU_PENDING_TRANSACTION_FAILED, FAILl);
							// dialog = createShenzhoufuPendingPurchaseFailDialog(R.string.IAB_SHENZHOUFU_PENDING_TITLE, SUtils.getContext().getString(R.string.IAB_SHENZHOUFU_CONFIRM_PENDING_TRANSACTION), CONFIRM_SHENZHOUFU_PENDING,true,R.string.IAB_SKB_OK);
						break;
					#endif
					#if USE_UMP_R3_BILLING
						case DIALOG_UMP_R3_PAYMENT_INFO:
							mServerInfo.setBillingMethod(InAppBilling.GET_STR_CONST(IAB_UMP_R3));
							String mPIString = getStringFormated(R.string.IAB_UMP_R3_PRODUCT_NAME,"{PRODUCT_NAME}",GetItemDescription()+"\n");
							mPIString+=getStringFormated(R.string.IAB_UMP_R3_PRICE,"{PRICE}",mServerInfo.getItemPriceFmt());
							mPIString+=SUtils.getContext().getString(R.string.IAB_UMP_R3_CUSTOMER_SERVICE);
							mPIString+=getStringFormated(R.string.IAB_UMP_R3_CONFIRMATION_TEXT_1,"{SHORTCODE1}",mServerInfo.getShortCode(1));
							
							dialog = createUMPDialog(R.string.IAB_UMP_R3_PAYMENT_INFO, mPIString, CONFIRM_INFO,true,R.string.IAB_UMP_R3_BTN_CONFIRM_INFO);
						break;
						
						case DIALOG_UMP_R3_CONFIRM_PAYMENT:
							mServerInfo.setBillingMethod(InAppBilling.GET_STR_CONST(IAB_UMP_R3));
							String mCPString = getStringFormated(R.string.IAB_UMP_R3_CONFIRMATION_TEXT_2,"{SHORTCODE2}",mServerInfo.getShortCode(2));
							mCPString+=getStringFormated(R.string.IAB_UMP_R3_PRODUCT_NAME,"{PRODUCT_NAME}",GetItemDescription()+"\n");
							mCPString+=getStringFormated(R.string.IAB_UMP_R3_PRICE,"{PRICE}",mServerInfo.getItemPriceFmt());
							mCPString+=SUtils.getContext().getString(R.string.IAB_UMP_R3_CUSTOMER_SERVICE);
							
							//IAB_UMP_R3_CONFIRMATION_TEXT_2
							dialog = createUMPDialog(R.string.IAB_UMP_R3_CONFIRM_PAYMENT, mCPString, CONFIRM_PAYMENT,true,R.string.IAB_UMP_R3_CONFIRM_PAYMENT);
						break;
						
						case DIALOG_UMP_R3_PAYMENT_SUCCESS:
							mServerInfo.setBillingMethod(InAppBilling.GET_STR_CONST(IAB_UMP_R3));
							String mPSString = SUtils.getContext().getString(R.string.IAB_UMP_R3_SUCESS_TEXT_THANKS);
							
							String mPSStringtmp = getStringFormated(R.string.IAB_UMP_R3_SUCESS_TEXT,"{PRODUCT_NAME}",GetItemDescription());
							mPSString+=getStringFormated(mPSStringtmp,"{PRICE}",mServerInfo.getItemPriceFmt());
							mPSString+=SUtils.getContext().getString(R.string.IAB_UMP_R3_CUSTOMER_SERVICE);
							//IAB_UMP_R3_SUCESS_TEXT
							dialog = createUMPDialog(R.string.IAB_UMP_R3_PAYMENT_FINISHED, mPSString, PAYMENT_FINISH,false,R.string.IAB_SKB_OK);
						break;
						
						case DIALOG_UMP_R3_PAYMENT_FAILURE:
							dialog = createPurchaseFailDialog(R.string.IAB_PURCHASE_ITEM_FAILURE_TITLE, R.string.IAB_UMP_R3_PAYMENT_FAILURE, FAILl);
						break;
					#endif
					}
					if (dialog != null)
					{
						//dialog.setOwnerActivity((Activity)SUtils.getContext());
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
	
	private String FormatCC()
	{
		String CC_TYPE;
		String CCN = SUtils.getLManager().GetUserCC();
		
		if(CCN.equals(""))
		{
			return(SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_ACCOUNT));
		}
		else
		{
			if(CCN.substring(0,1).equals(IS_MASTERCARD))
				CC_TYPE = TYPE_MASTERCARD;
		else if(CCN.substring (0,1).equals(IS_VISA))
			CC_TYPE = TYPE_VISA;
		else
			CC_TYPE = SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_ACCOUNT);
		}
						
		return (""+CC_TYPE+CC_PREFIX+SUtils.getLManager().GetUserCCLastNumbers());
	}
	
	static String ReturnFormatCC()
	{
		String CC_TYPE;
		String CCN = SUtils.getLManager().GetUserCC();
		
		if(CCN.equals(""))
		{
			return(SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_ACCOUNT));
		}
		else
		{
			if(CCN.substring(0,1).equals(IS_MASTERCARD))
				CC_TYPE = TYPE_MASTERCARD;
		else if(CCN.substring (0,1).equals(IS_VISA))
			CC_TYPE = TYPE_VISA;
		else
			CC_TYPE = SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_ACCOUNT);
		}
						
		return (""+CC_TYPE+CC_PREFIX+SUtils.getLManager().GetUserCCLastNumbers());
	}

	private Dialog createDialog(int titleId, int messageId, DialogInterface.OnClickListener dil, boolean yesno, boolean isretry) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		int positiveButtonIndex;
		
		if(isretry)
			positiveButtonIndex = R.string.IAB_SKB_RETRY;
		else
			positiveButtonIndex = android.R.string.ok;
		
		if(yesno)
		{
			builder.setTitle(titleId)
			.setIcon(android.R.drawable.stat_sys_warning)
			.setMessage(messageId)
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
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
			})
			.setPositiveButton(positiveButtonIndex, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					new Thread(new Runnable() {
						public void run() {
							if (mServerInfo.getBillingType().equals(HTTP_BILLING))
							{
								ProcessTransactionHTTP(InAppBilling.mItemID);
							}
							else if (mServerInfo.getBillingType().equals(WAP_BILLING))
							{
								ProcessTransactionWAP(InAppBilling.mItemID);
							}
							else if (mServerInfo.getBillingType().equals(SMS_BILLING))
							{
								ProcessTransactionSMS(InAppBilling.mItemID);
							}
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
							else if (mServerInfo.getBillingType().equals(PSMS_BILLING))
							{
								ProcessTransactionPSMS(InAppBilling.mItemID);
							}
#endif				
#if SHENZHOUFU_STORE
							else if (mServerInfo.getBillingType().equals(SHENZHOUFU_BILLING))
							{
								startShenzhoufuTransaction(InAppBilling.mItemID);
							}
#endif							
						}
						}).start();
					}
				})
				.setNegativeButton(android.R.string.cancel, dil);
				
		}else
		{
			builder.setTitle(titleId)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(messageId)
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
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
			})
			.setPositiveButton(android.R.string.ok, dil);
		}
		return builder.create();
	}
	
	private Dialog createValidateWapDialog() 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		builder.setMessage(R.string.IAB_CONTINUE)
		.setCancelable(false)
		.setPositiveButton(R.string.IAB_SKB_CONTINUE, new DialogInterface.OnClickListener()
		{
		public void onClick(DialogInterface dialog, int which) {new Thread(new Runnable() {
		public void run()
		{
		
		}}).start();}
		});
		return builder.create();
	}
	
	public void ValidateWAPPurchase()
	{
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		mXPlayer.setPurchaseID(getWAPID());
		mXPlayer.sendWAPValidationRequest(mCurrentId);
		
		while (!mXPlayer.handleWAPValidationRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		ERR(TAG,"*************method "+XPlayer.getLastErrorCode());
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "*************method "+XPlayer.getLastErrorCode());
		
		if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
		{
			if(!XPlayer.getLastErrorCodeString().equals(SUCESS_CODE) && !XPlayer.getLastErrorCodeString().equals(SUCESS_PENDING_CODE))
			{
				Bundle wapBundle = getWAPErrorBundle();
				if(wapBundle!=null && wapBundle.size()>0)
				{
					//if wapBundle not null, then there was an error on eComm that was returned on the WAP page
					
					{
						INFO(TAG,"Adding wap error info");
						//error is because of limits
						int wapErrCode = wapBundle.getInt(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_ERR_CODE));
						int wapErrSecsToTrans = wapBundle.getInt(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_SECONDS_BEFORE_TRANS));
						
						String wapErrStr = wapBundle.getString(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_ERR_MESSAGE));
						String wapErrNextTransTime = wapBundle.getString(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_NEXT_TRANS_TIME));
						setWAPErrorBundle(null);

						JDUMP(TAG,wapErrSecsToTrans);
						JDUMP(TAG,wapErrNextTransTime);
						JDUMP(TAG,wapErrCode);
						JDUMP(TAG,wapErrStr);

						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
						

						//from wapBundle
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_SECONDS_BEFORE_TRANS), (wapErrSecsToTrans));
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_NEXT_TRANS_TIME),(wapErrNextTransTime!=null)?wapErrNextTransTime.getBytes():null);
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), (wapErrCode));
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(wapErrStr!=null)?wapErrStr.getBytes():null);

						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
												
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
						if (mServerInfo.getBillingType().equals("wap_other"))
						   SUtils.getLManager().SaveUserPaymentType(LManager.CREDIT_CARD_PAYMENT);
						else if(mServerInfo.getBillingType().equals("wap_paypal"))
							SUtils.getLManager().SaveUserPaymentType(LManager.PAYPAL_PAYMENT);
						else
						   SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);

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


				
				} else
				{
					//ALL ERRORS HERE!!!
					showDialog(DIALOG_PURCHASE_FAIL,GetItemId());
				}
			}
		}
		else
		{
		#if ITEMS_STORED_BY_GAME
			if(GLOFTHelper.GetXPlayer().getDevice().getServerInfo().getIs3GOnly().equals("1"))
				GLOFTHelper.GetInstance().restoreWifiConnection();
		#endif
			
			String TxId = getWAPTxID();
			setWAPTxID("");
			
			Bundle bundle = new Bundle();
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
									
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
			if (mServerInfo.getBillingType().equals("wap_other"))
			   SUtils.getLManager().SaveUserPaymentType(LManager.CREDIT_CARD_PAYMENT);
			else if(mServerInfo.getBillingType().equals("wap_paypal"))
				SUtils.getLManager().SaveUserPaymentType(LManager.PAYPAL_PAYMENT);
			else
			   SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);

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
	}
	
	private Dialog createTnCDialog(int titleId, String message) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		builder.setTitle(titleId)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setMessage(message)
		.setCancelable(true)
		.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
				}
			})
		.setPositiveButton(R.string.IAB_SKB_OK, new DialogInterface.OnClickListener()
		{
		public void onClick(DialogInterface dialog, int which) {new Thread(new Runnable() {public void run() {}	}).start();}
		});
		return builder.create();
	}
	
	private Dialog createDialogSUCCESS(int titleId, int messageId, DialogInterface.OnClickListener dil) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		
			builder.setTitle(titleId)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(messageId)
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
										
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
											
											
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
			})
			.setPositiveButton(android.R.string.ok, dil);
		return builder.create();
	}
	
	private Dialog createPendingDialog(int titleId, int messageId, DialogInterface.OnClickListener dil, final String ID) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		
			builder.setTitle(titleId)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(messageId)
			.setCancelable(true)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(ID!=null)?ID.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
										
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
			})
			.setPositiveButton(android.R.string.ok, dil);
		return builder.create();
	}
	
#ifdef USE_UMP_R3_BILLING

	public void SendUMP_SMS1(final String id)
	{
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		
    	((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
    		@Override
            public void run()
			{
    			dSendingSms = ProgressDialog.show(SUtils.getContext(),SUtils.getContext().getString(R.string.IAB_UMP_R3_PLEASE_WAIT),SUtils.getContext().getString(R.string.IAB_UMP_R3_SENDING_SMS), true);
			}
        });		

		new Thread( new Runnable()
		{
			public void run ()
			{
			  try
			  {
				DBG(TAG,"SendUMP_SMS1 Started");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "SendUMP_SMS1 Started");

				int MAX_RETRY = 5;
				int MAX_TOKEN_RETRY = 3;
				int current_attempt = 1;
				int current_token_attempt = 1;
				boolean keep_verifing = true;
				boolean got_tid = false;
				
				final int BILLING_SUCCESS = 0;
				final int BILLING_SUCCESS_OFFLINE = 1;
				final int BILLING_PENDING = 2;
				final int BILLING_FAILURE = 3;


				
				DBG(TAG,"Waiting for Transaction ID...");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Waiting for Transaction ID...");
				while((!got_tid) && (current_token_attempt<=MAX_TOKEN_RETRY))
				{
					//Retrieve Transaction ID from Ecommerce
					mXPlayer.sendUMPR3TIDRequest(mCurrentId);
					DBG(TAG,"Waiting for Transaction ID response on attempt: "+current_token_attempt);
					while (!mXPlayer.handleUMPR3TIDRequest())
					{
						try {
							Thread.sleep(50);
						} catch (Exception exc) {}
					} 
					
					if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
					{
						try {
							Thread.sleep(2000);
						} catch (Exception exc) {}
						current_token_attempt++;

						if(current_token_attempt>MAX_TOKEN_RETRY)
						{
							DBG(TAG,"MAX TRANSACTION ID attempts reached (current_attempt="+current_token_attempt+", finished with error!");
							DBG(TAG,"XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "MAX TRANSACTION ID attempts reached (current_attempt="+current_token_attempt+", finished with error!");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
							((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
							{
								@Override
								public void run()
								{
									dSendingSms.dismiss();
									dSendingSms = null;
								}
							});
							showDialog(DIALOG_PURCHASE_FAIL,id);
						}
					}
					else
					{
						DBG(TAG,"XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
						got_tid = true;
						setSMS_TID(XPlayer.getLastErrorCodeString());
						DBG(TAG,"XPlayer.getUMPMO1()="+XPlayer.getUMPMO1());
						DBG(TAG,"XPlayer.getUMPMO2()="+XPlayer.getUMPMO2());
						
						DBG(TAG,"Finished / Is a valid Transaction ID? ="+got_tid);
						DBG(TAG,"Transaction ID="+getSMS_TID());

						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "XPlayer.getUMPMO1()="+XPlayer.getUMPMO1());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "XPlayer.getUMPMO2()="+XPlayer.getUMPMO2());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Finished / Is a valid Transaction ID? ="+got_tid);
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Transaction ID="+getSMS_TID());


						//Send SMS				
						SMS mSMS = new SMS(mDevice);
						mSMS.SendUMPR3PurchaseMessage(GetItemId(),getSMS_TID(),1);

						DBG(TAG,"Sending UMP R3 SMS1 STARTED");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Sending UMP R3 SMS1 STARTED");
						while(!mSMS.isCompleted() && !mSMS.isFail()){}

						if(mSMS.isFail())
						{
							((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
							{
								@Override
								public void run()
								{
									dSendingSms.dismiss();
									dSendingSms = null;
								}
							});
							showDialog(DIALOG_PURCHASE_FAIL,id);
						}
						else
						{

							((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
							{
								@Override
								public void run()
								{
									dSendingSms.dismiss();
									dSendingSms = null;
								}
							});
							showDialog(DIALOG_UMP_R3_CONFIRM_PAYMENT,GetItemId());
						}
					}
				}//while(!got_tid)
			  }
			  catch(Exception EX)
			  {}
			}
		}
	#if !RELEASE_VERSION
		,"Thread-SendUMP_SMS1"
	#endif
		).start();
		
	}

	public void SendUMP_SMS2(final String id)
	{
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		
    	((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
    		@Override
            public void run()
			{
    			dSendingSms = ProgressDialog.show(SUtils.getContext(),SUtils.getContext().getString(R.string.IAB_UMP_R3_PLEASE_WAIT),SUtils.getContext().getString(R.string.IAB_UMP_R3_SENDING_SMS), true);
			}
        });		

		new Thread( new Runnable()
		{
			public void run ()
			{
			  try
			  {
				DBG(TAG,"SendUMP_SMS2 Started");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "SendUMP_SMS2 Started");

				int MAX_RETRY = 6;
				int current_attempt = 1;
				boolean keep_verifing = true;
				
				final int BILLING_SUCCESS = 0;
				final int BILLING_SUCCESS_OFFLINE = 1;
				final int BILLING_PENDING = 2;
				final int BILLING_FAILURE = 3;

				

				//Send SMS				
				SMS mSMS = new SMS(mDevice);
				mSMS.SendUMPR3PurchaseMessage(GetItemId(),getSMS_TID(),2);

				DBG(TAG,"SendUMPR3PurchaseMessage STARTED");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "SendUMPR3PurchaseMessage STARTED");
				while(!mSMS.isCompleted() && !mSMS.isFail()){}
				DBG(TAG,"SendUMPR3PurchaseMessage FINISHED");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "SendUMPR3PurchaseMessage FINISHED");

				if(mSMS.isFail())
				{
					((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
					{
						@Override
						public void run()
						{
							dSendingSms.dismiss();
							dSendingSms = null;
						}
					});
					showDialog(DIALOG_PURCHASE_FAIL,id);
				}
				else
				{
			 		//HERE UDPATE
					InAppBilling.mItemID=mCurrentId;
					InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);
					InAppBilling.mOrderID=getSMS_TID();
					InAppBilling.save(WAITING_FOR_GAMELOFT);
					//END UPDATE					
					while((keep_verifing) && (current_attempt<=MAX_RETRY))
					{
						DBG(TAG,"Waiting for ump_get_billing_result response on attempt: "+current_attempt);
						
						mXPlayer.sendUMPR3GetBillingResultRequest(mCurrentId,getSMS_TID());
						
						while (!mXPlayer.handleUMPR3GetBillingResultRequest())
						{
							try {
								Thread.sleep(50);
							} catch (Exception exc) {}
						}
						
						DBG(TAG,"Response Received for ump_get_billing_result on attempt: "+current_attempt);						
						DBG(TAG,"XPlayer.getLastErrorCodeString()= "+XPlayer.getLastErrorCodeString());						
						
						if((XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)&&(XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT)&& !(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING))))
						{
							keep_verifing = false;
							DBG(TAG,"FAILURE RECEIVED finished with error!");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "FAILURE RECEIVED finished with error, code:"+XPlayer.getLastErrorCode());
							try {
								Thread.sleep(5000);
								} catch (Exception exc) {}
								
							((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
							{
								@Override
								public void run()
								{
									dSendingSms.dismiss();
									dSendingSms = null;
								}
							});
							InAppBilling.saveLastItem(STATE_FAIL);
							InAppBilling.clear();
							showDialog(DIALOG_PURCHASE_FAIL,id);
						}
						else if (XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT)
						{
							if(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING)))
							{
								DBG(TAG,"Retry due PENDING");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Retry due PENDING");
								current_attempt++;
								if(current_attempt>MAX_RETRY)
								{
									DBG(TAG,"MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
									LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
									keep_verifing = false;
									((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
									{
										@Override
										public void run()
										{
											dSendingSms.dismiss();
											dSendingSms = null;
										}
									});
									InAppBilling.saveLastItem(STATE_FAIL);
									InAppBilling.clear();
									showDialog(DIALOG_PURCHASE_FAIL,id);
								}
								try {
								Thread.sleep(5000);
								} catch (Exception exc) {}
							}
							else if(XPlayer.getLastErrorCodeString().equals(SUCCESS_RESULT))
							{
								DBG(TAG,"Finished with SUCCESS");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Finished with SUCCESS");
								keep_verifing = false;
								((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
								{
									@Override
									public void run()
									{
										dSendingSms.dismiss();
										dSendingSms = null;
									}
								});
								showDialog(DIALOG_UMP_R3_PAYMENT_SUCCESS,id);								
							}
						}
					}//while(keep_verifing)
				}
			  }
			  catch(Exception EX)
			  {}
			}
		}
	#if !RELEASE_VERSION
		,"Thread-SendUMP_SMS2"
	#endif
		).start();
		
	}


	public void CheckR3PurchaseResult()
	{

		DBG(TAG,"CheckR3Purchase Started");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "CheckR3Purchase Started");

		int MAX_RETRY = 2;
		int current_attempt = 1;
		boolean keep_verifing = true;
		
		final int BILLING_SUCCESS = 0;
		final int BILLING_SUCCESS_OFFLINE = 1;
		final int BILLING_PENDING = 2;
		final int BILLING_FAILURE = 3;		
		
		while((keep_verifing) && (current_attempt<=MAX_RETRY))
		{
			DBG(TAG,"Waiting for CheckR3Purchase response on attempt: "+current_attempt);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Waiting for CheckR3Purchase response on attempt: "+current_attempt);
			
			mXPlayer.sendUMPR3GetBillingResultRequest(InAppBilling.mItemID,getSMS_TID());
			
			while (!mXPlayer.handleUMPR3GetBillingResultRequest())
			{
				try {
					Thread.sleep(50);
				} catch (Exception exc) {}
			}
			
			DBG(TAG,"Response Received for CheckR3Purchase on attempt: "+current_attempt);						
			DBG(TAG,"XPlayer.getLastErrorCodeString()= "+XPlayer.getLastErrorCodeString());						

			if((XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)&&(XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT)&& !(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING))))
			{
				keep_verifing = false;
				DBG(TAG,"FAILURE RECEIVED on CheckR3Purchase!");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "FAILURE RECEIVED on CheckR3Purchase, code:" + XPlayer.getLastErrorCode()+", code string:"+XPlayer.getLastErrorCodeString());
				try {
					Thread.sleep(3000);
					} catch (Exception exc) {}
					
				InAppBilling.saveLastItem(STATE_FAIL);
				InAppBilling.clear();
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
					ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
				}
			}
			else if (XPlayer.getLastErrorCode()!=XPlayer.ERROR_INIT)
			{
				if(XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING)))
				{
					DBG(TAG,"Retry due PENDING");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Retry due PENDING");
					current_attempt++;
					if(current_attempt>MAX_RETRY)
					{
						DBG(TAG,"MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "MAX attempts reached (current_attempt="+current_attempt+", finished with error!");
						keep_verifing = false;
						InAppBilling.saveLastItem(STATE_FAIL);
						InAppBilling.clear();
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
							ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
						}

					}
					try {
					Thread.sleep(5000);
					} catch (Exception exc) {}
				}
				else if(XPlayer.getLastErrorCodeString().equals(SUCCESS_RESULT))
				{
					DBG(TAG,"Finished with SUCCESS");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Finished with SUCCESS");
					keep_verifing = false;
					InAppBilling.saveLastItem(STATE_SUCCESS);
					InAppBilling.clear();
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
					
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
						
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
					SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
							
					try{
						Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
						Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
						bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
					}catch(Exception ex ) {
						ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
					}
							
				}
			}
		}//while(keep_verifing)
	}
	
	private Dialog createUMPDialog(int titleId, String message, DialogInterface.OnClickListener dil, boolean isCancelable, int btnTextId)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		
			builder.setTitle(titleId)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(message)
			.setCancelable(isCancelable)
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
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
			})
			.setPositiveButton(btnTextId, dil);
		return builder.create();
	}
#endif
	private Dialog createPurchaseFailDialog(int titleId, int messageId, DialogInterface.OnClickListener dil) 
	{
		return createPurchaseFailDialog(titleId, messageId, dil, XPlayer.ERROR_INIT, null);
	}
	private Dialog createPurchaseFailDialog(int titleId, int messageId, DialogInterface.OnClickListener dil, final int errorCode, final String errorCodeString)
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
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							
					//// Error codes ////
					WARN(TAG,"*** Adding error codes (createPurchaseFailDialog) ***");
					WARN(TAG,"*** errorCode: " + errorCode);
					WARN(TAG,"*** errorCodeString: " + errorCodeString);
					
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), (errorCode));
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(errorCodeString!=null)?errorCodeString.getBytes():null);
					/////////////////////
					
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
			})
			.setPositiveButton(R.string.IAB_SKB_RETRY, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					new Thread(new Runnable() {
						public void run() {
							if (mServerInfo.getBillingType().equals(HTTP_BILLING))
							{
								ProcessTransactionHTTP(InAppBilling.mItemID);
							}
							else if (mServerInfo.getBillingType().equals(WAP_BILLING))
							{
								ProcessTransactionWAP(InAppBilling.mItemID);
							}
							else if (mServerInfo.getBillingType().equals(SMS_BILLING))
							{
								ProcessTransactionSMS(InAppBilling.mItemID);
							}
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
							else if (mServerInfo.getBillingType().equals(PSMS_BILLING))
							{
								ProcessTransactionPSMS(InAppBilling.mItemID);
							}
#endif							
#if USE_UMP_R3_BILLING
							else if (mServerInfo.getBillingType().equals(UMP_R3_BILLING))
							{
								ProcessTransactionUMP_R3(InAppBilling.mItemID);
							}							
#endif
#if SHENZHOUFU_STORE
							else if (mServerInfo.getBillingType().equals(SHENZHOUFU_BILLING))
							{
								ProcessTransactionShenzhoufu(InAppBilling.mOrderID);
							}							
#endif
							else if (mServerInfo.getBillingType().equals(CC_BILLING))
							{
								ProcessTransactionCC(InAppBilling.mItemID);
							}
							else if (mServerInfo.getBillingType().equals("wap_other"))
							{
								ProcessTransactionWAP(InAppBilling.mItemID);
							}
							else if (mServerInfo.getBillingType().equals("wap_paypal"))
							{
								ProcessTransactionWAPPaypal(InAppBilling.mItemID);
							}
						}
						}).start();
					}
				});
        	int billing_count = InAppBilling.mServerInfo.getItemById(mCurrentId).getBillingOptsCount();
			String billingPref = InAppBilling.mServerInfo.getItemById(mCurrentId).getType_pref();

#if USE_PHD_PSMS_BILL_FLOW
			if (billing_count > 1 && (!(billingPref.equals(HTTP_BILLING) || billingPref.equals(SMS_BILLING) || billingPref.equals(PSMS_BILLING))))
#else
        	if (billing_count > 1)
#endif
        	{
    			builder.setNeutralButton(R.string.IAB_BTN_OTHER, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				new Thread(new Runnable() {
					public void run() {
						//showDialog(DIALOG_SELECT_BILLING_METHOD,GetItemId());
						((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
						{
						String message;
						public void run ()
						{
							try
							{
								CustomizeDialog customizeDialog = new CustomizeDialog(SUtils.getContext(),TYPE_CHANGE_PAYMENT);
								customizeDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
								{
									public void onCancel(DialogInterface dialog)
									{
										Bundle bundle = new Bundle();
										bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
															
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
															
										//// Error codes ////
										WARN(TAG,"*** Adding error codes (createPurchaseFailDialog) ***");
										WARN(TAG,"*** errorCode: " + errorCode);
										WARN(TAG,"*** errorCodeString: " + errorCodeString);
										
										bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), (errorCode));
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(errorCodeString!=null)?errorCodeString.getBytes():null);
										/////////////////////
										
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
								customizeDialog.show();
							}
							catch (Exception e)
							{
								DBG_EXCEPTION(e);
							}
						}
						});
					}
					}).start();
				}
			});
		}
		else //only one biiling option
		{
			builder.setNegativeButton(R.string.IAB_SKB_CANCEL, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Bundle bundle = new Bundle();
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
				
				//// Error codes ////
				WARN(TAG,"*** Adding error codes (createPurchaseFailDialog) ***");
				WARN(TAG,"*** errorCode: " + errorCode);
				WARN(TAG,"*** errorCodeString: " + errorCodeString);
				
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), (errorCode));
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(errorCodeString!=null)?errorCodeString.getBytes():null);
				/////////////////////
										
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

		}
			//.setNegativeButton(android.R.string.cancel, dil);
		return builder.create();
	}
	
	private Dialog 	createConfirmationDialog(final int errorCode, final String errorCodeString)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		
		builder
		.setMessage(mServerInfo.getPurchaseConfirmString())
		.setCancelable(true)
		.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				Bundle bundle = new Bundle();
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
									
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GetItemId()!=null)?GetItemId().getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
				
				//// Error codes ////
				WARN(TAG,"*** Adding error codes (createConfirmationDialog) ***");
				WARN(TAG,"*** errorCode: " + errorCode);
				WARN(TAG,"*** errorCodeString: " + errorCodeString);
				
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), (errorCode));
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(errorCodeString!=null)?errorCodeString.getBytes():null);
				/////////////////////
									
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
		})
		.setNegativeButton(mServerInfo.getPurchaseLaterString(), null)
		.setPositiveButton(mServerInfo.getPurchaseOkString(),null)
		.setNeutralButton(mServerInfo.getTNC_Title_Shop(), null);
		
		final AlertDialog alertDialog = builder.create();
		
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialog) 
			{
				android.widget.Button buttonPositive, buttonNegative, buttonNeutral=null;

				buttonPositive 	= alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				buttonNegative 	= alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				buttonNeutral 	= alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

				if(mServerInfo.getFlowType().equals("2") || mServerInfo.getFlowType().equals("5"))
				{
					buttonPositive.setBackgroundResource(R.drawable.iab_psms_button_selector);
					buttonNegative.setBackgroundResource(R.drawable.iab_psms_button_selector);
					buttonNeutral.setBackgroundResource(R.drawable.iab_psms_button_selector);
					
					buttonPositive.setTextColor(android.graphics.Color.BLACK);
					buttonNegative.setTextColor(android.graphics.Color.BLACK);
					buttonNeutral.setTextColor(android.graphics.Color.BLACK);
				}
				
				buttonPositive.setAllCaps(false);
				buttonNegative.setAllCaps(false);
				buttonNeutral.setAllCaps(false);

				buttonPositive.setTextSize(12);
				buttonNegative.setTextSize(12);
				buttonNeutral.setTextSize(12);
				
				buttonPositive.setOnClickListener(new View.OnClickListener() 
				{
					@Override
					public void onClick(View view)
					{
						alertDialog.dismiss();
						InAppBilling.closeBackgroundDialog();
						InAppBilling.showBackgroundDialog();
						
						new Thread(new Runnable() {
							public void run() {
								mServerInfo.searchForDefaultBillingMethod(InAppBilling.mItemID);
								if(mServerInfo.getIs3GOnly().equals("1"))
									PrepareDataConnection();

								if (mServerInfo.getBillingType().equals(HTTP_BILLING))
								{
									ProcessTransactionHTTP(InAppBilling.mItemID);
								}
								else if (mServerInfo.getBillingType().equals(SMS_BILLING))
								{
									ProcessTransactionSMS(InAppBilling.mItemID);
								}
								else if (mServerInfo.getBillingType().equals(PSMS_BILLING))
								{
									if (mServerInfo.getBillingAttribute(InAppBilling.mItemID,InAppBilling.GET_STR_CONST(IAB_PSMS_BILL_TYPE)).equals(InAppBilling.GET_STR_CONST(IAB_SILENT)))
									{
										ProcessTransactionSMS(InAppBilling.mItemID);
									}
									else
									{
									#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
										ProcessTransactionPSMS(InAppBilling.mItemID);
									#endif	
									}						
								}
								else if (mServerInfo.getBillingType().equals(WAP_BILLING))
								{
									ProcessTransactionWAP(InAppBilling.mItemID);
								}
							}
							}).start();
					}
				});
				
				buttonNegative.setOnClickListener(new View.OnClickListener() 
				{
					@Override
					public void onClick(View view)
					{
						alertDialog.dismiss();

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

				buttonNeutral.setOnClickListener(new View.OnClickListener() 
				{
					@Override
					public void onClick(View view)
					{
						showDialog(DIALOG_TERMS_AND_CONDITIONS, GetItemId());
					}
				});
			}
		});

		return alertDialog;
	}

	private Dialog createYESNODialog(int titleId, String message, DialogInterface.OnClickListener dil, boolean isretry) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());

		int positiveButtonIndex;
		
		if(isretry)
			positiveButtonIndex = R.string.IAB_SKB_RETRY;
		else
			positiveButtonIndex = android.R.string.ok;
		
		    if (titleId == R.string.IAB_PURCHASE_ITEM_CONFIRMATION_HTTP_WAP_TITLE)
			{
				builder.setTitle(titleId)
				.setIcon(R.drawable.iconiab)
				.setMessage(message)
				.setCancelable(true)
				.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					public void onCancel(DialogInterface dialog)
					{
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
				})
				.setPositiveButton(positiveButtonIndex, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							public void run() {
								if (mServerInfo.getBillingType().equals(HTTP_BILLING))
								{
									ProcessTransactionHTTP(InAppBilling.mItemID);
								}
								else if (mServerInfo.getBillingType().equals(SMS_BILLING))
								{
									ProcessTransactionSMS(InAppBilling.mItemID);
								}
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
								else if (mServerInfo.getBillingType().equals(PSMS_BILLING))
								{
									ProcessTransactionPSMS(InAppBilling.mItemID);
								}
#endif
								else if (mServerInfo.getBillingType().equals(WAP_BILLING))
								{
									ProcessTransactionWAP(InAppBilling.mItemID);
								}
							}
							}).start();
						}
					})
					.setNegativeButton(android.R.string.cancel, dil);
			}
			else
			{
				INFO(TAG, "Executing CC");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Executing CC");
				builder.setTitle(titleId)
				.setIcon(R.drawable.iconiab)
				.setMessage(message)
				.setCancelable(true)
				.setOnCancelListener(new DialogInterface.OnCancelListener()
				{
					public void onCancel(DialogInterface dialog)
					{
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
				})
				.setPositiveButton(positiveButtonIndex, new DialogInterface.OnClickListener()
				{
						public void onClick(DialogInterface dialog, int which)
						{
								ProcessTransactionCC(GetItemId());
						}
				})
					.setNegativeButton(android.R.string.cancel, dil);
			}
		return builder.create();
	}
	
	int mBillingSelection;
	private void SetBillingSelection(int value)
	{
		mBillingSelection = value;
	}
	
	private int GetBillingSelection()
	{
		return (mBillingSelection);
	}
	
	/*private Dialog createSelectBillingMethodDialog(final CharSequence[] items) 
	{
	
	}*/
	private Dialog createSelectBillingMethodDialog(final CharSequence[] items) 
	{
		SetBillingSelection(0);
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		
		INFO(TAG, "Executing Select Billing Method Dialog");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Executing Select Billing Method Dialog");
		builder.setTitle(R.string.IAB_PURCHASE_ITEM_SELECT_BILLING_METHOD_TITLE)
		.setIcon(R.drawable.iconiab)
		.setCancelable(true)
		.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
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
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				if(//Change this for a strings.xml value
				(items[GetBillingSelection()].toString().indexOf(SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_CREDIT_CARD))>=0)
				||(items[GetBillingSelection()].toString().indexOf(SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_ACCOUNT))>=0)
				||(items[GetBillingSelection()].toString().indexOf(TYPE_MASTERCARD)>=0)
				||(items[GetBillingSelection()].toString().indexOf(TYPE_VISA)>=0)
				)
				{
					if(mServerInfo.setBillingMethod(CC_BILLING))
						ProcessTransactionCC(GetItemId());
				}

			#if BOKU_STORE	
				if(items[GetBillingSelection()].equals(BOKU_STRING))
				{
					if(mServerInfo.setBillingMethod(BOKU_BILLING))
						ProcessTransactionBOKU(GetItemId());
				}
			#endif	
			#if SHENZHOUFU_STORE
				if(items[GetBillingSelection()].equals(SHENZHOUFU_STRING))
				{
					if(mServerInfo.setBillingMethod(SHENZHOUFU_BILLING))
						startShenzhoufuTransaction(GetItemId());
				}
			#endif	
				
				if(items[GetBillingSelection()].equals(SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_ADD_NEW_CREDIT_CARD)))
				{
						SUtils.getLManager().AddNewCreditCard();
						ProcessTransactionCC(GetItemId());
				}
			}
		})
		.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int item)
			{
				SetBillingSelection(item);
				//Toast.makeText(SUtils.getContext(), items[item], Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
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
		return builder.create();
	}

	
	private static boolean m_bWasWifiEnabled = false;
	private boolean TIMED_OUT = false;
	private long m_phonedatatimeout = 0;
	private long PHONE_DATA_TIME_OUT = 25000;
	
	public boolean PrepareDataConnection()
	{
		TIMED_OUT = false;
			if(mDevice.IsWifiEnable())
			{
				m_bWasWifiEnabled = true;
				mDevice.DisableWifi();
				m_phonedatatimeout = System.currentTimeMillis();
			}
				
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "PrepareDataConnection - Disabling wifi");
			while(mDevice.IsWifiDisabling())
			{
				DBG(TAG,"Disabling Wi-Fi");
				try
				{
					Thread.sleep(50);
				} catch (Exception exc){}				
			}
			while (!mDevice.IsConnectionReady() && !TIMED_OUT)
			{
				DBG(TAG,"Waiting for transference on Phone Data Service");
				DBG(TAG,"m_phonedatatimeout="+m_phonedatatimeout);						
				DBG(TAG,"System.currentTimeMillis() - m_phonedatatimeout="+(System.currentTimeMillis() - m_phonedatatimeout));
					
				if (System.currentTimeMillis() - m_phonedatatimeout > PHONE_DATA_TIME_OUT)
					TIMED_OUT = true;
				try
				{
					Thread.sleep(50);
				} catch (Exception exc){}
			}
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "PrepareDataConnection is timed out:"+TIMED_OUT);
		return (TIMED_OUT);
	}
	public void restoreWifiConnection()
	{
		if(m_bWasWifiEnabled)
		{
			mDevice.EnableWifi();
			m_bWasWifiEnabled = false;
			/*while (mDevice.IsWifiEnabling())
			{
				DBG(TAG,"ENABLING Wi-Fi!!!");
			}*/
		}
	}
}
