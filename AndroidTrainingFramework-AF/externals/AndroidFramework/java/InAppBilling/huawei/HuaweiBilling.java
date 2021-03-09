#if USE_IN_APP_BILLING && HUAWEI_STORE
package APP_PACKAGE.iab;

import java.lang.reflect.Method;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.content.Context;
import org.json.JSONObject;
import org.json.JSONException;


import com.huawei.dsm.sdk.DsmSdk;
import com.huawei.dsm.sdk.bean.GetUserTokenRequest;
import com.huawei.dsm.sdk.bean.GetUserTokenResponse;
import com.huawei.dsm.sdk.bean.PpvOrderRequest;
import com.huawei.dsm.sdk.bean.PpvOrderResponse;

import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.iab.ServerInfo;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.billing.common.Base64;
import APP_PACKAGE.billing.common.Base64DecoderException;


public class HuaweiBilling
{
	SET_TAG("InAppBilling");

	
	private static DsmSdk dsm;

	private static ServerInfo mServerInfo = null;
	private static Device mDevice	= null;
	

	private String mAppId		= null; //obtain from server info
	private String mPrivateKey	= null;
	
	
	//obtain this from request to login
	private String mUserToken;
	private String mUserId;
	
	
	private String mCurrentId; //content ID from feed
	private String mObjectId; //from feed
	private String mProductId;//from feed
	private String mPrice; //from feed, used for json headers only

	private String mPurchaseId; //from eComm at ORDER URL request



	private static final int JSON_HEADERS_VALIDATE_USER_URL = 0;
	private static final int JSON_HEADERS_ORDER_URL 		= 1;
	private static final int JSON_HEADERS_CHECK_URL 		= 2;

	
	private static final int HUAWEI_OP_SUCCESS = 0;


	private static HuaweiBilling mThis = null;


	public static HuaweiBilling getInstance()
	{
		if(mThis == null)
		{
			mThis = new HuaweiBilling(); 
			return mThis;
		}
		return mThis;
	}

	public static void init(Context ctx, ServerInfo si)
	{
		mServerInfo = si;
		mDevice = new Device(mServerInfo);
		dsm = new DsmSdk(ctx);
	}


	private HuaweiBilling()
	{
		//avoid object construction
	}



	public boolean requestTransaction(String id)
	{
		mCurrentId	= id;

		mAppId		= mServerInfo.getShopProfile().getAppId();

		mProductId  = mServerInfo.getBillingAttribute(mCurrentId, "product_id");

		mObjectId 	= mServerInfo.getBillingAttribute(mCurrentId, "object_id");
		
		mPrice 		= mServerInfo.getGamePrice();

		
		String encAppKey = mServerInfo.getShopProfile().getAppKey();
		
		
		if(TextUtils.isEmpty(mPrivateKey))
		{
			DBG(TAG,"initializing private key");
			try{
				mPrivateKey	= new String(Base64.decode(encAppKey.getBytes()));
			}catch(Base64DecoderException bde){
				DBG_EXCEPTION(bde);
				mPrivateKey = null;
			}
		}


		INFO(TAG, "[requestTransaction] purchasing content id[" + mCurrentId + "] with product Id:[" + mProductId + "]");
        INFO(TAG, "[requestTransaction] initializing with appId [" +  mAppId +"] and pk[" + mPrivateKey + "]");

        if(TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mPrivateKey))
        {
        	ERR(TAG,"[requestTransaction] Can't attempt loggin to Huawei, wrong params!");
        	IABResultCallBack(IAB_BUY_FAIL);
        	return false;
        }

        huaweiLogin(mAppId, mPrivateKey);
        return true;
	}


	/*
		* Do the purchase process, this is:
		* 1. Huawei login
		* 2. Verify token with eComm
		* 3. Request order Id
		* 4. Call Huawei purchase API
		* 5. Confirm result with eComm
		* 6. Deliver Item to game (add transaction)
		* 7. Burn order (This is done at HuaweiHelper class)
	*/

	//1. login using huawei api, this gets token and user Id.
	private void huaweiLogin(String appId, String privateKey)
	{
		INFO(TAG,"[huaweiLogin] started");

	#if !RELEASE_VERSION
		INFO(TAG,"[huaweiLogin] Id:["  + appId + "]" );
		INFO(TAG,"[huaweiLogin] val:["  + privateKey + "]" );
	#endif
		GetUserTokenRequest tokenRequest = new GetUserTokenRequest();
		tokenRequest.setApplicationId(appId);
		tokenRequest.setPrivateKey(privateKey);
		tokenRequest.setExtendInfo("extendInfo");

		
		GetUserTokenResponse usrToken = dsm.getUserToken(tokenRequest);



		if(usrToken != null && usrToken.getCode() == HUAWEI_OP_SUCCESS) //0 indicates success, see Huawei documentation
		{
			mUserId = usrToken.getUserId();
			mUserToken = usrToken.getUserToken();
			if(!TextUtils.isEmpty(mUserId) && !TextUtils.isEmpty(mUserToken))
			{
				INFO(TAG,"[huaweiLogin] Successfully obtained Huawei token!");
				verifyUserToken();
				return;
			} else
			{
				ERR(TAG,"[huaweiLogin] Error, code is: " + usrToken.getCode());
			}
		}
	#if !RELEASE_VERSION
		if(usrToken != null) { 
			ERR(TAG, "[huaweiLogin] Err code is:" + usrToken.getCode());
			ERR(TAG, "[huaweiLogin] Err desc is:" + usrToken.getResultDescription());
			ERR(TAG, "[huaweiLogin] Err tokn is:" + usrToken.getUserToken());
			ERR(TAG, "[huaweiLogin] Err full is:" + usrToken.toString());	
		}
	#endif
		INFO(TAG,"[huaweiLogin] Error");

		IABResultCallBack(IAB_BUY_FAIL);
		
	}


	//2. verify the token obtained from Huawei with eComm
	private void verifyUserToken()
	{
		INFO(TAG, "[verifyUserToken]");
		
		try 
		{
			// mLoginTime = time;
			String jsonHeaders = getJsonHeaders(mDevice, JSON_HEADERS_VALIDATE_USER_URL);
			
			if(!TextUtils.isEmpty(jsonHeaders))
			{
				String urlToken = mServerInfo.getBillingAttribute(mCurrentId, "url_verify_user");
				
				DBG(TAG, "[verifyUserToken] url: " + urlToken);
				DBG(TAG, "[verifyUserToken] jsonHeaders: " + jsonHeaders);
				
				IABRequestHandler reqHandler = IABRequestHandler.getInstance();
				
				
				reqHandler.doRequestByGet(urlToken, null, jsonHeaders, new IABCallBack()
				{
					public void runCallBack(Bundle bundle)
					{
						int result = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
						String response = bundle.getString(IABRequestHandler.KEY_RESPONSE);

						DBG(TAG, "[verifyUserToken] result: " + result);
						if (result == IABRequestHandler.SUCCESS_RESULT && (response.contains("success") || response.contains("SUCCESS")  ) )
						{
							DBG(TAG, "[verifyUserToken] response: " + response);
							
							requestPurchaseId();
						}
						else
							IABResultCallBack(IAB_BUY_FAIL);
					}
				});
			}
			else
			{
				ERR(TAG,"Can not initiate purchase, an error occured while initializing JsonHeaders");
				IABResultCallBack(IAB_BUY_FAIL);
			}
		}catch(Exception ex){DBG_EXCEPTION(ex);IABResultCallBack(IAB_BUY_FAIL);}
	}

	// 3. request a purchase Id to call Huawei purchase API
	private void requestPurchaseId()
	{
		INFO(TAG, "[requestPurchaseId]");
							
		try{
			String urlOrder = mServerInfo.getBillingAttribute(mCurrentId, InAppBilling.GET_STR_CONST(IAB_URL));
			String jsonHeaders = getJsonHeaders(mDevice, JSON_HEADERS_ORDER_URL);
			
			if(!TextUtils.isEmpty(jsonHeaders))
			{
				DBG(TAG, "[requestPurchaseId] url: " + urlOrder);
				DBG(TAG, "[requestPurchaseId] jsonHeaders: " + jsonHeaders);
				
				IABRequestHandler reqHandler = IABRequestHandler.getInstance();
				reqHandler.doRequestByGet(urlOrder, null, jsonHeaders, new IABCallBack()
				{
					public void runCallBack(Bundle bundle)
					{
						int result = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
						DBG(TAG, "[requestPurchaseId] result: " + result);

						if (result == IABRequestHandler.SUCCESS_RESULT)
						{
							String response = bundle.getString(IABRequestHandler.KEY_RESPONSE);
							DBG(TAG, "[requestPurchaseId] response: " + response);
							
							mPurchaseId = getValue(response,1);
							if(TextUtils.isEmpty(mPurchaseId))
							{
								ERR(TAG,"Could not obtain PurchaseId from eCommerce");
								IABResultCallBack(IAB_BUY_FAIL);
							}
							else
							{
								startHuaweiPayment();
							}
						}
						else
							IABResultCallBack(IAB_BUY_FAIL);
					}
				});
			}
			else
			{
				ERR(TAG,"Can not initiate purchase, an error occured while initializing JsonHeaders");
				IABResultCallBack(IAB_BUY_FAIL);
			}
		}
		catch(Exception ex){DBG_EXCEPTION(ex);IABResultCallBack(IAB_BUY_FAIL);}
	}


	// 4. Call Huawei purchase API
	private void startHuaweiPayment()
	{
		INFO(TAG,"[startHuaweiPayment]");
		
		String notifyUrl = mServerInfo.getBillingAttribute(mCurrentId, "url_notify");
		JDUMP(TAG,mPurchaseId);
		JDUMP(TAG,mObjectId);
		JDUMP(TAG,mProductId);
		JDUMP(TAG,notifyUrl);

		
		PpvOrderRequest ppvOrderRequest = new PpvOrderRequest();

		ppvOrderRequest.setApplicationId(mAppId); //from feed 
		ppvOrderRequest.setPrivateKey(mPrivateKey); //from feed

		ppvOrderRequest.setUserId(mUserId);			//from Huawei UserTokenResponse
		ppvOrderRequest.setUserToken(mUserToken);	//from Huawei UserTokenResponse
		ppvOrderRequest.setObjectId(mObjectId); 	//from feed
		ppvOrderRequest.setProductId(mProductId);	//from feed
		ppvOrderRequest.setNotificationUrl(notifyUrl);	//from feed
		ppvOrderRequest.setOrderMode("1");			//constant
		ppvOrderRequest.setCurrencyType("CNY");		//constant
		ppvOrderRequest.setExternalTransactionId(mPurchaseId); //from eCommerce, (requestPurchaseId()  call)
    	

		PpvOrderResponse ppvOrderResponse = dsm.ppvOrder(ppvOrderRequest);

		if(ppvOrderResponse.getCode() == HUAWEI_OP_SUCCESS)
		{
			INFO(TAG,"Successfully purchased at Huawei!");
			JDUMP(TAG,ppvOrderResponse.toString());
			JDUMP(TAG,ppvOrderResponse.getResultDescription());
			IABResultCallBack(IAB_BUY_OK);
		} else
		{
			ERR(TAG,"Purchased failed at Huawei!");
			JDUMP(TAG,ppvOrderResponse.getResultDescription());
			JDUMP(TAG,ppvOrderResponse.getCode());
			JDUMP(TAG,ppvOrderResponse.toString());
			IABResultCallBack(IAB_BUY_FAIL);
		}
	}


	/*
		* 5. Confirm result with eComm
		* 6. Deliver Item to game (add transaction)
	*/
	void IABResultCallBack(int IABResult)
	{
		if(IABResult == IAB_BUY_OK)
		{
			INFO(TAG,"[validatePurchase] Huawei Payment succeed!");
			String url = mServerInfo.getBillingAttribute(mCurrentId, "url_check");
			String jsonHeaders = getJsonHeaders(mDevice, JSON_HEADERS_CHECK_URL);

			if(!TextUtils.isEmpty(jsonHeaders))
			{
				DBG(TAG, "[validatePurchase] url: " + url);
				DBG(TAG, "[validatePurchase] jsonHeaders: " + jsonHeaders);
				
				IABRequestHandler reqHandler = IABRequestHandler.getInstance();
				reqHandler.doRequestByGet(url, null, jsonHeaders, new IABCallBack()
				{
					public void runCallBack(Bundle cBundle)
					{
						int result = cBundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
						DBG(TAG, "[validatePurchase] result: " + result);
						boolean confirmationFailed = true;
						if (result == IABRequestHandler.SUCCESS_RESULT)
						{
							String response = cBundle.getString(IABRequestHandler.KEY_RESPONSE);
							DBG(TAG, "[validatePurchase] response: " + response);
							// response SUCCESS|<partner_order_id>|<transactoinId>
							if(response.contains("SUCCESS"))
							{
								String partnerOrderId = getValue(response, 1);
								String TxId = getValue(response, 2);
								
								INFO(TAG,"eComm partnerOrderId:" + partnerOrderId);
								INFO(TAG,"eComm TxId:" + TxId);
								JDUMP(TAG,InAppBilling.mItemID);
								
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
								
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_NOTIFY_ID),partnerOrderId!=null?partnerOrderId.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null); //isma TODO
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
								
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
								

								try
								{
									Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
									Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
									bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
									confirmationFailed = false;
								}
								catch(Exception ex )
								{
									ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
								}
								
								InAppBilling.saveLastItem(STATE_SUCCESS);
								InAppBilling.clear();
								return;
							}
						}

						if(confirmationFailed)
						{
							ERR(TAG, "[validatePurchase] End with errors");
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
							
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);

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

							InAppBilling.saveLastItem(STATE_FAIL);
							InAppBilling.clear();
						}
					} //runCallback END
				});
			}

		} else {
			ERR(TAG,"Huawei Payment Failed!");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Huawei Payment Failed!");
			
			Bundle bundle = new Bundle();
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
			
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
			
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IABResult);

			try {
				Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
				Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
				bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
			}
			catch(Exception ex ){ERR(TAG,"Error invoking reflex method "+ex.getMessage());}

		}

	}


	private String getJsonHeaders(Device device, int type)
	{
		String jsonHeaders = null;
		JSONObject jObj = new JSONObject();
		
		try{
			if(type == JSON_HEADERS_VALIDATE_USER_URL)
			{
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_APP_ID), mAppId);//"x-up-gl-appid"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_USER_TOKEN), mUserToken);//"x-up-gl-usertoken"
				

				String value = mUserToken + "_" + mAppId + "_gameloft_china_huaweitv";
				String sign = XPlayer.md5(value);
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GL_SIGN), sign);//"x-up-gl-sign"
				jsonHeaders = jObj.toString();

				INFO(TAG,"value before sign:" + value);
			}
			else if(type == JSON_HEADERS_ORDER_URL)
			{
				String value = null;
				
				value = InAppBilling.GET_GGLIVE_UID();
				if (TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM),value);
				
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CONTENT_ID),mCurrentId);
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_PRICE),mPrice);
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),device.getIMEI());
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI),GGI);	
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV());
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID());
				
				value = GET_CLIENT_ID();
				if (TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID),value);

				value = InAppBilling.GET_CREDENTIALS();
				if (TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS),value);

				jsonHeaders = jObj.toString();
			} 
			else if(type == JSON_HEADERS_CHECK_URL)
			{
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_APP_ID),mAppId); //"x-up-gl-appid"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GL_PURCHASE),mPurchaseId);//"x-up-gl-purchase"
				
				String value = mPurchaseId + "_" + mAppId + "_gameloft_china_huaweitv";
				String sign = XPlayer.md5(value);
				jObj.put("x-up-gl-sign",sign); INFO(TAG,"pre - x-up-gl-sign: " + value);
				jsonHeaders = jObj.toString();
			}
		}
		catch(JSONException jsone){DBG_EXCEPTION(jsone);}
		return jsonHeaders;
	}

	/**
	 * Splits the src string using the character "|" as target and finds the value according to pos.
	 * e.g.: src = a|hello|b|goodbye| pos = 1. Returns hello.
	 * @param src The String used as source.
	 * @param pos The position in src that I want to get.
	 * @return String with the values corresponding to pos in src.
	 * */
	private static String getValue(String src, int pos)
	{
		int start = 0;
		int ini_pos = pos;
		int end = src.indexOf('|', start + 1);

		while (pos > 0)
		{
			if (start == -1)
				return null;
				
			start = end;
			end = src.indexOf('|', start + 1);
			pos--;
		}
		
		if (start == -1)
			return null;

		if (end == -1)
			end = src.length();

		if (ini_pos > 0)
			start++;

		if (start == end)
			return "";

		if (start > end)
			return null;
		
		try{
			char[] buf = new char[end - start];
			src.getChars(start, end, buf, 0);
			String value = new String(buf);
			return value;
		}catch (IndexOutOfBoundsException e){DBG_EXCEPTION(e);}
		return null;
	}

}
#endif
