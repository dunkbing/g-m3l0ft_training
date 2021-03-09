#if USE_IN_APP_BILLING && ATET_STORE
package APP_PACKAGE.iab;

import java.lang.reflect.Method;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;

public class ATETHelper extends IABTransaction
{

	private static String mCurrentId = null;
	private static String mExOrderNo = null;
	private static String mUrlCheck = null;
	private final static String mAtetAppid = ATET_APPID; //this must be defined in config.bat
	private final String mAtetAppkey = ATET_APPKEY; //this must be defined in config.bat
	private final String URL_ORDER = "https://secure.gameloft.com/freemium/atet_cn/index.php?game=";

	private static final int JSON_HEADERS_URL_ORDER = 0;
	private static final int JSON_HEADERS_URL_CHECK = 1;


	private static Device mDevice;

	public ATETHelper(ServerInfo si)
	{
		super(si);
		mDevice = new Device(si);
	}

	@Override
	public boolean requestTransaction(String id)
	{
		INFO(TAG, "[requestTransaction]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[requestTransaction]");
		mCurrentId = id;
		// mServerInfo.searchForDefaultBillingMethod(mCurrentId);

		INFO(TAG, "[requestTransaction], ATET payment with id: " + mCurrentId);
		
		String jsonHeaders = getJsonHeaders(mDevice,JSON_HEADERS_URL_ORDER);
		String url = URL_ORDER + Device.ValidateStringforURL(mDevice.getDemoCode());
		if(TextUtils.isEmpty(jsonHeaders))
		{
			ERR(TAG,"Can not initiate purchase, an error occured while initializing JsonHeaders");
			IABResultCallBack(IAB_BUY_FAIL);
			return false;
		}


		IABRequestHandler reqHandler = IABRequestHandler.getInstance();
		reqHandler.doRequestByGet(url, null, jsonHeaders, new IABCallBack()
		{
			public void runCallBack(Bundle bundle)
			{
				int result = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
				if (result == IABRequestHandler.SUCCESS_RESULT)
				{
					String response = bundle.getString(IABRequestHandler.KEY_RESPONSE);

					mExOrderNo = getValue(response, 1);
					
					// mExOrderNo = response.substring( idx + 1, response.length());
					INFO(TAG,"Response is: [" + response + "] and Retrieved exOrderNo:[" + mExOrderNo + "]");

					//get url_check
					mUrlCheck = mServerInfo.getBillingAttribute(mCurrentId,"url_check");

					Intent intent = new Intent(SUtils.getContext(), AtetIABActivity.class);
					intent.putExtra(ATETHelperConst.STR_NOTIFY_URL, mServerInfo.getBillingAttribute(mCurrentId, "url_notify") );
					intent.putExtra(ATETHelperConst.STR_APP_ID, mAtetAppid);
					intent.putExtra(ATETHelperConst.STR_WARESID, Integer.parseInt(mServerInfo.getBillingAttribute(mCurrentId, "waresid") ) );
					intent.putExtra(ATETHelperConst.STR_EXORDERNO, mExOrderNo);
					intent.putExtra(ATETHelperConst.STR_PRICE, mServerInfo.getGamePrice());
					intent.putExtra(ATETHelperConst.STR_APPKEY, mAtetAppkey);

					SUtils.getContext().startActivity( intent );
			


					
				} else 
				{
					IABResultCallBack(IAB_BUY_FAIL);
				}
			}
		});


		return true;
	}


	private static String getJsonHeaders(Device device, int type)
	{
		String jsonHeaders = null;

		JSONObject jObj = new JSONObject();
		try
		{
			if(type == JSON_HEADERS_URL_ORDER)
			{
				String value =  null;

				value = InAppBilling.GET_GGLIVE_UID();
				if(TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM),value); //"x-up-gl-acnum"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CONTENT_ID),mCurrentId); //"x-up-gl-contentid"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),device.getIMEI()); //"x-up-gl-imei"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI),GGI); //"x-up-gl-ggi"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV()); //"x-up-gl-hdidfv"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID()); //"x-up-gl-gldid"
				
				value = GET_CLIENT_ID();
				if (TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID),value); //"x-up-gl-fed-client-id"

				value = InAppBilling.GET_CREDENTIALS(); 
				if (TextUtils.isEmpty(value)) value = "0";
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS),value); //"x-up-gl-fed-credentials"

				jsonHeaders = jObj.toString();
			} else if(type == JSON_HEADERS_URL_CHECK)
			{
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_APP_ID),mAtetAppid);//"x-up-gl-appid"
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GL_PURCHASE),mExOrderNo);//"x-up-gl-purchase"
				
				String value = mExOrderNo + "_" + mAtetAppid + InAppBilling.GET_STR_CONST(IAB_ATET_SALT_KEY); //"_gameloft_china_atet"
				String sign = XPlayer.md5(value);
				jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GL_SIGN),sign); INFO(TAG,"pre - x-up-gl-sign: " + value);//"x-up-gl-sign"
				jsonHeaders = jObj.toString();
			}

				
		}catch(JSONException jsone)
		{
			DBG_EXCEPTION(jsone);
			jsonHeaders = null;
		}
		INFO(TAG,"generated jsonHeaders:" + jsonHeaders);

		return jsonHeaders;
	}




	static void IABResultCallBack(int IABResult)
	{
		if(IABResult == IAB_BUY_OK)
		{
			//Payment succeed at ATET , must call gameloft URL Check and deliver the item
			INFO(TAG, "ATET Payment Success! Item:" + InAppBilling.mItemID + " - calling url_check");

			String url = mUrlCheck;
			String jsonHeaders = getJsonHeaders(mDevice, JSON_HEADERS_URL_CHECK);

			IABRequestHandler reqHandler = IABRequestHandler.getInstance();
			reqHandler.doRequestByGet(url, null, jsonHeaders, new IABCallBack()
			{
				public void runCallBack(Bundle cBbundle)
				{
					int result = cBbundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
					boolean confirmationFailed = true;
					if (result == IABRequestHandler.SUCCESS_RESULT)
					{
						String response = cBbundle.getString(IABRequestHandler.KEY_RESPONSE);

						if(response.contains("SUCCESS"))
						{
							String partnerOrderId = getValue(response, 1);
							String TxId = getValue(response, 2);
							
							DBG(TAG,"Payment completeley succeed:" + response);
							INFO(TAG,"eComm partnerOrderId:" + partnerOrderId);
							INFO(TAG,"eComm TxId:" + TxId);
							
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
						} else 
						{
							ERR(TAG,"ATET payment succeed but failed on eComm: [" + response + "]");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ATET Payment succeed but url_check failed:" + response);
						}
					} else
					{
						ERR(TAG,"ATET Payment succeed but url_check failed!");

						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ATET Payment succeed but url_check failed");
					}
				
					if(confirmationFailed == true)
					{
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

				}
			});
		}
		else 
		{
			ERR(TAG,"ATET Payment fail on ATET side");

			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ATET Payment fail on ATET side");
			
			Bundle bundle = new Bundle();
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
			
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
			
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
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
		}
	}


	@Override
	public void sendNotifyConfirmation(String notifyId) 
	{
		DBG(TAG,"[sendNotifyConfirmation] notifyId:" + notifyId);
		if (TextUtils.isEmpty(notifyId))
		{
			return;
		}
		String orderId = notifyId;
		sendOrderIdConfirmation(orderId);
	}

	private void sendOrderIdConfirmation(final String orderId)
	{
		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();
		StringBuffer sb = new StringBuffer();

		sb.append("b=");
		sb.append(InAppBilling.GET_STR_CONST(IAB_ITEM_DELIVERED_ACTION_NAME));	sb.append("|");
		sb.append(reqHandler.encodeString(orderId));	sb.append("|");
		sb.append("17"); //ATET is portalID 17
				
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
					//Do nothing
					String result = bundle.getString(IABRequestHandler.KEY_RESPONSE);
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
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for Item Delivered Confirmation failed with error code: "+code );
					ERR(TAG,"[Gameloft Order Confirmation] Fail");
						
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
	public boolean restoreTransactions() {
		ERR(TAG, "ATET API does not have restoreTransactions!");
		return true;
	};


	private static String GetContentId()
	{
		return (mCurrentId);
	}

	@Override
	public void showDialog(int idx, String id){}


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
			{
				return null;
			}
			start = end;
			end = src.indexOf('|', start + 1);
			pos--;
		}
		if (start == -1)
		{
			return null;
		}
		if (end == -1)
		{
			end = src.length();
		}
		if (ini_pos > 0)
		{
			start++;
		}
		if (start == end)
		{
			return "";
		}
		if (start > end)
		{
			return null;
		}
		try
		{
			char[] buf = new char[end - start];
			src.getChars(start, end, buf, 0);
			String value = new String(buf);
			return value;
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	interface ATETHelperConst
	{
		public static final String STR_NOTIFY_URL =	"notifyurl";
		public static final String STR_APP_ID = 	"appid";
		public static final String STR_WARESID = 	"waresid";
		public static final String STR_EXORDERNO = 	"exorderno";
		public static final String STR_PRICE = 		"price";
		public static final String STR_APPKEY = 	"appkey";
	}

}


#endif