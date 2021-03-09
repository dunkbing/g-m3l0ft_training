#if USE_IN_APP_BILLING && ATET_STORE
package APP_PACKAGE.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.iapppay.mpay.ifmgr.SDKApi;
import com.iapppay.mpay.ifmgr.IPayResultCallback;
import com.iapppay.mpay.ifmgr.IAccountExCallback;
import com.iapppay.mpay.tools.PayRequest;

import APP_PACKAGE.iab.ATETHelper.ATETHelperConst;

public class AtetIABActivity extends Activity
{
	SET_TAG("InAppBilling");


	private String appkey = null;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		// this.setContentView(R.layout.welcome); 


		Bundle extras = getIntent().getExtras();
		String params = null;
		String appid = null;

        if(extras != null)
        {
        	appid = extras.getString(ATETHelperConst.STR_APP_ID);
        	appkey = extras.getString(ATETHelperConst.STR_APPKEY); INFO(TAG, "appkey = " + appkey); 
        	
        	PayRequest payRequest = new PayRequest();
			payRequest.addParam("notifyurl", extras.getString(ATETHelperConst.STR_NOTIFY_URL)); INFO(TAG, "notifyURL = " + extras.getString("notifyurl"));
			payRequest.addParam("appid",  appid);												INFO(TAG, "appid = " + extras.getString("appid"));
			payRequest.addParam("waresid",  extras.getInt(ATETHelperConst.STR_WARESID) );		INFO(TAG, "waresid = " + extras.getInt("waresid"));
			payRequest.addParam("exorderno",  extras.getString(ATETHelperConst.STR_EXORDERNO));	INFO(TAG, "exorderno = " + extras.getString("exorderno"));
			payRequest.addParam("price",  extras.getString(ATETHelperConst.STR_PRICE));			INFO(TAG, "price = " + extras.getString("price"));
			params = payRequest.genSignedOrdingParams(appkey);
        }

        if(TextUtils.isEmpty(appid) || TextUtils.isEmpty(params) )
        {
        	ERR(TAG,"Can not initialize ATET store with appid[" + appid + "] and params [" + params + "]");
        	ATETHelper.IABResultCallBack(IAB_BUY_FAIL);
        	finish();
        } else 
        {
        	//data OK, initialize purchase
        	try
        	{
	        	DBG(TAG, "Initializing ATET API");
	        	SDKApi.init(this, SDKApi.LANDSCAPE, appid);

	        	
	        	INFO(TAG,"requestTransaction - Start Pay using ATET API with params:" + params);
	        	SDKApi.startPay(AtetIABActivity.this, params, new AtetPaymentCallback(appid, params, false));
	        } catch(Exception e)
	        {
	        	DBG(TAG, "requestTransaction - An Exception occurred while initializing payment");
	        	DBG_EXCEPTION(e);
	        	ATETHelper.IABResultCallBack(IAB_BUY_FAIL);
	        	finish();
	        }
        }
	}


	class AtetPaymentCallback implements IPayResultCallback 
	{
		private String appid = null;
		private String params = null;
		private boolean attemptLogin = false;

		public AtetPaymentCallback(String appid, String params, boolean attemptLogin)
		{
			super();
			this.appid = appid;
			this.params = params;
			this.attemptLogin = attemptLogin;
		}

		@Override
		public void onPayResult(int resultCode, String signValue, String resultInfo) {
			INFO(TAG,"onPayResult - ATET callback, result code is: " + resultCode);
			if (SDKApi.PAY_SUCCESS== resultCode) {
				DBG(TAG,"signValue = " + signValue);
				
				if (signValue == null) {
					// //finish();
					ERR(TAG,"sign value is null");
				} 
				
				if (PayRequest.isLegalSign(signValue, appkey)) {
					DBG(TAG, "Payment Succeed");
					ATETHelper.IABResultCallBack(IAB_BUY_OK);
				} else {
					ERR(TAG,"Payment succeed, but Signature verification failed");
				}

			} else if (SDKApi.PAY_CANCEL== resultCode) {
				// user cancelled
				ERR(TAG,"Cancelled by user");
				ATETHelper.IABResultCallBack(IAB_BUY_CANCEL);
			} else if (SDKApi.PAY_HANDLING== resultCode) {
				
				//the order is processing! (IAB_BUY_PENDING) ???
				ERR(TAG,"Order is processing");
				ATETHelper.IABResultCallBack(IAB_BUY_PENDING);
			} else if (SDKApi.PAY_FAIL == resultCode) {

				ERR(TAG,"Order fail - temporaly, send to account login screen?" + attemptLogin);

				// ATETHelper.IABResultCallBack(IAB_BUY_FAIL);
				if(attemptLogin)
				{
					SDKApi.loginUI(AtetIABActivity.this, appid, new AtetLoginCallback(appid,params), false);
					return;
				} else 
				{
					ATETHelper.IABResultCallBack(IAB_BUY_FAIL);
				}

			} else 
			{
				ERR(TAG,"Order failed, other reason");
				ATETHelper.IABResultCallBack(IAB_BUY_FAIL);
			}

			finish();
		}
	}


	class AtetLoginCallback implements IAccountExCallback
	{
		private String appid = null;
		private String params = null;
		public AtetLoginCallback(String appid, String params)
		{
			super();
			this.appid = appid;
			this.params = params;
		}

		@Override
		public void onCallBack(int retCode, String username, long uid)
		{
			DBG(TAG,"AtetLoginCallback - return code is:" + retCode);
			if(retCode == IAccountExCallback.RETCODE_SUCCESS)
			{
				DBG(TAG, "AtetLoginCallback - Launch Purchase after login");
				SDKApi.startPay(AtetIABActivity.this, params, new AtetPaymentCallback(appid, params, false));
			} else if(retCode ==  IAccountExCallback.RETCODE_FAIL)
			{
				ERR(TAG,"AtetLoginCallback - Could not login into ATET account");
			} else 
			{
				ERR(TAG,"AtetLoginCallback - could not loging, finish activity");
				ATETHelper.IABResultCallBack(IAB_BUY_FAIL);
				finish();
			}
			
		}
	}




}

#endif