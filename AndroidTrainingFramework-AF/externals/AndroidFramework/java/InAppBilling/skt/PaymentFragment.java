package APP_PACKAGE.iab;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.lang.reflect.Method;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.Fragment;
import android.view.View;
import android.app.Activity;
import android.util.Log;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.skplanet.dodo.IapPlugin;
import com.skplanet.dodo.IapResponse;

import APP_PACKAGE.iab.pdu.Response;
import APP_PACKAGE.iab.helper.ConverterFactory;
import APP_PACKAGE.iab.pdu.VerifyReceipt;

public class PaymentFragment extends Fragment
{

	private String mRequestId;
    private IapPlugin mPlugin;
	SET_TAG("InAppBilling");
	private SktIabActivity mActivityInstance;
	
	private Handler mUiHandler = new Handler()
	{
		@Override
        public void handleMessage(Message msg) {
            DBG(TAG, "handleMessage [" + msg.what + "]");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "handleMessage [" + msg.what + "]");
			if (msg.what == 100) {
                DBG(TAG, "handleMessage :"+ ((String) msg.obj));
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "handleMessage :"+ ((String) msg.obj));
            }
            else if (msg.what == 101) {
                final Response response = (Response) msg.obj;
                JSONObject json = new JSONObject();
                try {
                    json.put("appid", SktIabActivity.appID.toUpperCase());
                    json.put("txid", response.result.txid);
                    json.put("signdata", response.result.receipt);
                } catch (JSONException e) {
                    ERR(TAG, "Failed while composing json data for verification receipt.");	
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Failed while composing json data for verification receipt.");
					e.printStackTrace();
                }

                //ReceiptConfirm rc = new ReceiptConfirm();
                //rc.execute((String) json.toString());
				
				//	GL server validation
				SktIabActivity.signData = response.result.receipt;
				mActivityInstance.sendConfirmation(response.result.txid);
            } else if (msg.what == 200) {
                DBG(TAG, "handleMessage" + ((String) msg.obj));
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "handleMessage" + ((String) msg.obj));
            }
			mActivityInstance.finish();
        }
	};
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DBG(TAG,"onAttach");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onAttach");
        mPlugin = IapPlugin.getPlugin(activity, SktIabActivity.library_server);
		mActivityInstance = (SktIabActivity)activity;
		
		if (!requestPayment())
		{
			WARN(TAG, "requestPayment error");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "requestPayment error");
		}	
    }

    @Override
    public void onDetach() {
        super.onDetach();
        DBG(TAG,"onDetach");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onDetach");
        mPlugin.exit();
    }
	
	private boolean requestPayment()
	{
		String params = "product_id=" + SktIabActivity.productID;
		//	APID
		params += "&appid=" + SktIabActivity.appID;
		//	Product name (optional)
		params +=  "&product_name=";
		//	tid (optional)
		params += "&tid=";
		//	Application server information (optional)
		params += "&bpinfo=";
		DBG(TAG, "requestPayment params="+params);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "requestPayment params="+params);
		//mUiHandler.obtainMessage(100, params).sendToTarget();
		
		Bundle req = mPlugin.sendPaymentRequest(params,
			new IapPlugin.RequestCallback()
			{
				@Override
                public void onResponse(IapResponse data)
				{
					DBG(TAG, "onResponse");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onResponse");
					if (data == null || data.getContentLength() <= 0)
					{
						// TODO Unusual error
						ERR(TAG, "onResponse() response data is null");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "onResponse() response data is null");
						//Informing the game that the buy item proccess is complete and fail.
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
						mActivityInstance.finish();
						return;
					}
					
					Response response = ConverterFactory.getConverter().fromJson(data.getContentToString());
					
					if (response == null)
					{
						// TODO invalid response data
                        ERR(TAG, "onResponse() invalid response data");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "onResponse() invalid response data");
						
						//Informing the game that the buy item proccess is complete and fail.
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
						mActivityInstance.finish();
                        return;
					}
					
					#if !RELEASE_VERSION
					StringBuffer sb = new StringBuffer("onResponse() \n");
					sb.append("From:" + data.getContentToString())
							.append("\n")
							.append("To:" + response.toString());
					INFO(TAG, sb.toString());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, sb.toString());
					#endif
					//mUiHandler.obtainMessage(100, sb.toString()).sendToTarget();
					
					// response.result.code
					if (!response.result.code.equals("0000")) {
						DBG(TAG, "purchase failed.");
						DBG(TAG, "[" + response.result.code + "] " + response.result.message);
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "purchase failed: ["+  response.result.code+ "]"  + response.result.message);
						
						//Informing the game that the buy item proccess is complete and fail.
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
						if (response.result.code.equals("9100"))
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);
						else
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"Error invoking reflex method "+ex.getMessage());
						}
						mActivityInstance.finish();
						return;
					}
					
					mUiHandler.obtainMessage(101, response).sendToTarget();
				}
				
				@Override
				public void onError(String reqid, String errcode, String errmsg)
				{
					// TODO Error occurred
					ERR(TAG, "onError() identifier:" + reqid + " code:" + errcode + " msg:" + errmsg);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "onError() identifier:" + reqid + " code:" + errcode + " msg:" + errmsg);
					
					//Informing the game that the buy item proccess is complete and fail.
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
					mActivityInstance.finish();
				}
			});
		
		if (req == null) {
            // TODO request failure
            ERR(TAG, "req == null");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "req == null");
			return false;
		}
		
		mRequestId = req.getString(IapPlugin.EXTRA_REQUEST_ID);
        if (mRequestId == null || mRequestId.length() == 0) {
            // TODO request failure
			ERR(TAG, "mRequestId == null");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "mRequestId == null");
            return false;
        }
		
		return true;
	}
}