#if USE_IN_APP_BILLING && ZTE_STORE
package APP_PACKAGE.iab;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONObject;
import org.json.JSONException;
import java.lang.reflect.Method;

import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.iab.ZTEIABActivity;

public class ZTEHelper extends IABTransaction
{	
	private static String mProductId 	= null;
	private static String mProductName 	= null;
	private static String mProductDesc 	= null;
	private static String mProductPrice	= null;
	private static String mPurchaseId 	= null;
	
	private static String mUrlOrder 	= null;
	private static String mUrlCheck 	= null;
	
	private static Device mDevice;

	private static final int JSON_HEADERS_ORDER_URL = 0;
	private static final int JSON_HEADERS_CHECK_URL = 1;
	
	interface ZTEHelperConst
	{
		public static final String PARTNERID = ZTE_PARTNERID; //must be defined in config.bat
	}

	public ZTEHelper(ServerInfo si)
	{
		super(si);
		mDevice = new Device(si);
	}
	
	@Override
	public boolean restoreTransactions(){ERR(TAG, "ZTE API does not support restoreTransactions!");return true;};

	@Override
	public void showDialog(int idx, String id){}

	@Override
	public boolean requestTransaction(String id)
	{
		INFO(TAG, "[requestTransaction]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[requestTransaction]");
		
		try{
			mProductId = id;
			mProductName = mServerInfo.getItemAttribute(mProductId, InAppBilling.GET_STR_CONST(IAB_NAME));
			mProductDesc = mServerInfo.getItemAttribute(mProductId, InAppBilling.GET_STR_CONST(IAB_DESCRIPTION));
			mProductPrice = mServerInfo.getGamePrice();

			mUrlOrder = mServerInfo.getBillingAttribute(mProductId,"url");
			mUrlCheck = mServerInfo.getBillingAttribute(mProductId,"url_check");
			
			if(!TextUtils.isEmpty(ZTEHelperConst.PARTNERID) && mProductId != null && mProductName != null && mProductDesc != null && mProductPrice != null)
			{
				DBG(TAG, "Item: " + mProductId + " Name: " + mProductName + " Price: " + mProductPrice);
				Intent intent = new Intent(SUtils.getContext(), ZTEIABActivity.class);
				SUtils.getContext().startActivity(intent);
			}
			else
			{
				ERR(TAG,"Unable to initiate purchase, missing information!");
				IABResultCallBack(IAB_BUY_FAIL);
				return false;
			}
		}catch(Exception ex){DBG_EXCEPTION(ex);IABResultCallBack(IAB_BUY_FAIL);return false;}
		return true;
	}
	
	public static void OrderRequest()
	{
		INFO(TAG, "[OrderRequest]");
		
		try{
			String jsonHeaders = getJsonHeaders(mDevice, JSON_HEADERS_ORDER_URL);
			String qry = "";
			mPurchaseId = null;
			
			if(!TextUtils.isEmpty(jsonHeaders))
			{
				DBG(TAG, "[OrderRequest] url: " + mUrlOrder);
				DBG(TAG, "[OrderRequest] jsonHeaders: " + jsonHeaders);
				
				IABRequestHandler reqHandler = IABRequestHandler.getInstance();
				reqHandler.doRequestByGet(mUrlOrder, qry, jsonHeaders, new IABCallBack()
				{
					public void runCallBack(Bundle bundle)
					{
						int result = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
						DBG(TAG, "[OrderRequest] result: " + result);
						
						if (result == IABRequestHandler.SUCCESS_RESULT)
						{
							String response = bundle.getString(IABRequestHandler.KEY_RESPONSE);
							DBG(TAG, "[OrderRequest] response: " + response);
							
							try{
								JSONObject jObject = new JSONObject(response);
								mPurchaseId = jObject.getString("purchase_id");
								DBG(TAG, "[OrderRequest] purchase ID: " + mPurchaseId);
								
								if(mPurchaseId != null && !TextUtils.isEmpty(mPurchaseId))
									ZTEIABActivity.getInstance().setOrderMessage(ZTEHelperConst.PARTNERID, mProductId, mPurchaseId, mProductDesc, mProductPrice, GAME_NAME_STR, mProductName);
								else
									IABResultCallBack(IAB_BUY_FAIL);

							} catch(JSONException ex){DBG_EXCEPTION(ex);IABResultCallBack(IAB_BUY_FAIL);}
						}
						else
							IABResultCallBack(IAB_BUY_FAIL);
					}
				});
			}
			else
			{
				ERR(TAG,"Can not initiate purchase, an error occurred while initializing JsonHeaders");
				IABResultCallBack(IAB_BUY_FAIL);
			}
		}
		catch(Exception ex){DBG_EXCEPTION(ex);IABResultCallBack(IAB_BUY_FAIL);}
	}
	
	private static String getJsonHeaders(Device device, int type)
	{
		String jsonHeaders = null;
		JSONObject jObj = new JSONObject();
		
		try{
			if(type == JSON_HEADERS_ORDER_URL)
			{
				String value = null;
				
				value = InAppBilling.GET_GGLIVE_UID();
				if (TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM),value);

				jObj.put("x-up-gl-contentid",mProductId);
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
				
				value = InAppBilling.GET_ANONYMOUS_CREDENTIAL();
				if (TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ANON_CREDS),value);//"x-up-gl-anon-credentials"
				jsonHeaders = jObj.toString();
			} 
			else if(type == JSON_HEADERS_CHECK_URL)
			{
				jObj.put("x-up-gl-purchase",mPurchaseId);
				jObj.put("x-up-gl-partnerId",ZTEHelperConst.PARTNERID);
				String value = mPurchaseId + "_gameloft_china_zte";
				String sign = XPlayer.md5(value);
				jObj.put("x-up-gl-sign",sign); INFO(TAG,"pre - x-up-gl-sign: " + value);
				jsonHeaders = jObj.toString();
			}
		}
		catch(JSONException jsone){DBG_EXCEPTION(jsone);}
		return jsonHeaders;
	}

	static void IABResultCallBack(int IABResult)
	{
		INFO(TAG, "[IABResultCallBack]");
		
		if(IABResult == IAB_BUY_OK)
		{
			//Payment success at ZTE, must call Gameloft URL Check and deliver item
			DBG(TAG, "ZTE Payment Success! Item: " + InAppBilling.mItemID);

			String jsonHeaders = getJsonHeaders(mDevice, JSON_HEADERS_CHECK_URL);
			IABRequestHandler reqHandler = IABRequestHandler.getInstance();
			reqHandler.doRequestByGet(mUrlCheck, null, jsonHeaders, new IABCallBack()
			{
				public void runCallBack(Bundle cBbundle)
				{
					int result = cBbundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
					boolean confirmationFailed = true;
					
					if (result == IABRequestHandler.SUCCESS_RESULT)
					{
						String response = cBbundle.getString(IABRequestHandler.KEY_RESPONSE);
						DBG(TAG, "eComm Response: " + response);
						
						if(response.contains("SUCCESS"))
						{
							String partnerOrderId = getValue(response, 1);
							String transactoinId = getValue(response, 2);
							
							DBG(TAG,"Payment completely succeed:" + response);
							DBG(TAG,"eComm partnerOrderId:" + partnerOrderId);
							DBG(TAG,"eComm transactoinId:" + transactoinId);
							
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
							
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_NOTIFY_ID),partnerOrderId!=null?partnerOrderId.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(transactoinId!=null)?transactoinId.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);

							try{
								Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
								Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
								bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
								confirmationFailed = false;
							}
							catch(Exception ex){ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());}
							
							InAppBilling.saveLastItem(STATE_SUCCESS);
							InAppBilling.clear();
						}
						else 
						{
							ERR(TAG,"ZTE payment success but failed on eComm: [" + response + "]");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ZTE Payment success but url_check failed:" + response);
						}
					}
					else
					{
						ERR(TAG,"ZTE Payment success but url_check failed!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ZTE Payment success but url_check failed");
					}
				
					if(confirmationFailed)
					{
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
						}catch(Exception ex ){ERR(TAG,"Error invoking reflex method "+ex.getMessage());}

						InAppBilling.saveLastItem(STATE_FAIL);
						InAppBilling.clear();
					}
				}
			});
		}
		else 
		{
			ERR(TAG,"ZTE Payment Failed!");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ZTE Payment Failed!");
			
			Bundle bundle = new Bundle();
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
			
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
			
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IABResult);

			try{
				Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
				Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
				bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
				
				if(ZTEIABActivity.getInstance() != null)
					ZTEIABActivity.getInstance().finish();
			}
			catch(Exception ex ){ERR(TAG,"Error invoking reflex method "+ex.getMessage());}
		}
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
		sb.append("19"); //ZTE is portalID 19
				
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