#if USE_IN_APP_BILLING && HUAWEI_STORE
package APP_PACKAGE.iab;

import java.lang.reflect.Method;


import android.os.Bundle;
import android.text.TextUtils;
import android.content.Context;
import android.content.Intent;

import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;

public class HuaweiHelper extends IABTransaction
{	
	private static String mCurrentId 		= null;

	private static HuaweiHelper mThis;
	

	public HuaweiHelper(ServerInfo si)
	{
		super(si);
		mThis	= this;
	}

	public static HuaweiHelper getInstance()
	{
		return mThis;
	}

	
	@Override
	public boolean restoreTransactions(){ERR(TAG, "HUAWEI API does not support restoreTransactions!");return true;};

	@Override
	public void showDialog(int idx, String id){}

	@Override
	public boolean requestTransaction(String id)
	{
		INFO(TAG, "[requestTransaction]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[requestTransaction]");
		
		mCurrentId = id;
		
		HuaweiBilling billing = HuaweiBilling.getInstance();
		
		billing.init(SUtils.getContext(),(ServerInfo)mServerInfo);
		return billing.requestTransaction(mCurrentId);
		
	}
	


	@Override
	public void sendNotifyConfirmation(String notifyId) 
	{
		INFO(TAG,"[sendNotifyConfirmation]");
		DBG(TAG,"[sendNotifyConfirmation] notifyId: " + notifyId);
		
		if (TextUtils.isEmpty(notifyId))
			return;

		sendOrderIdConfirmation(notifyId);
	}

	private void sendOrderIdConfirmation(final String orderId)
	{
		INFO(TAG,"[sendOrderIdConfirmation]");
		
		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();
		StringBuffer sb = new StringBuffer();

		sb.append("b=");
		sb.append(InAppBilling.GET_STR_CONST(IAB_ITEM_DELIVERED_ACTION_NAME));	sb.append("|");
		sb.append(reqHandler.encodeString(orderId));	sb.append("|");
		sb.append("19"); //Huawei is portalID 19
				
		String url = InAppBilling.GET_STR_CONST(IAB_URL_ITEM_DELIVERED);
		String qry = sb.toString();

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
					String result = bundle.getString(IABRequestHandler.KEY_RESPONSE);
					DBG(TAG,"[sendOrderIdConfirmation] result: " + result);
					
					JDUMP(TAG,result);
					LOGGING_APPEND_RESPONSE_PARAM(result, "Item Delivered Confirmation");
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("Item Delivered Confirmation"));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Item Delivered Confirmation: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("Item Delivered Confirmation") +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO("Item Delivered Confirmation");
					DBG(TAG,"[Gameloft Order Confirmation] success");
				}
				else
				{
					int code = bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
					ERR(TAG,"[sendOrderIdConfirmation] code: " + code);
					ERR(TAG,"[Gameloft Order Confirmation] Fail");
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

	
}


#endif