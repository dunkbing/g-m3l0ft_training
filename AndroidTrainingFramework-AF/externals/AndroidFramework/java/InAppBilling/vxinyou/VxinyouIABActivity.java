#if USE_IN_APP_BILLING && VXINYOU_STORE
package APP_PACKAGE.iab;

import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import com.google.gson.Gson;

import com.vxinyou.box.sdk.LoginResult;
import com.vxinyou.box.sdk.PayResult;
import com.vxinyou.box.sdk.Result;
import com.vxinyou.box.sdk.VxinyouLog;
import com.vxinyou.box.sdk.VxinyouLogin;
import com.vxinyou.box.sdk.VxinyouPay;
import com.vxinyou.box.sdk.VxinyouRecord;
import com.vxinyou.box.sdk.server.aidl.ICommCallBack;
import com.vxinyou.box.sdk.server.aidl.IRecordCallBack;
import com.vxinyou.boxclient.common.util.SystemManage;

import APP_PACKAGE.iab.VxinyouHelper.VxinyouHelperConst;
import APP_PACKAGE.GLUtils.XPlayer;

public class VxinyouIABActivity extends Activity
{
	SET_TAG("InAppBilling");
	
	private static String mVxinyouPrice = null;
	private static String mbillno 		= null;
	
	private static VxinyouPay mVxinyouPay;
	private static VxinyouLogin mVxinyouLogin;
	private static VxinyouRecord mVxinyouRecord;
	public static VxinyouIABActivity mInstance;
	
	private static final int PAYMENT_SUCCESS = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		INFO(TAG, "[onCreate]");
		
		mInstance = this;
		Bundle extras = getIntent().getExtras();
		
        if(extras != null)
        	mVxinyouPrice = extras.getString("PRICE");

        if(mVxinyouPrice != null && !TextUtils.isEmpty(mVxinyouPrice))
        {
			mVxinyouLogin = new VxinyouLogin(VxinyouIABActivity.this);
			mVxinyouPay = new VxinyouPay(VxinyouIABActivity.this);
			startApp();
			mVxinyouPay.registerPayNotify(new ICommCallBack.Stub() {// Stand-alone monitor payment status notification
				@Override
				public int payPush(int status, String message) throws RemoteException 
				{
					DBG(TAG, "registerPayNotify status:" + status + "registerPayNotify message:" + message);
					return 0; // Return 0 for success has been received
				}
			});
			startLogin();
		}
		else
		{
		    ERR(TAG,"Can not initialize store with price [" + mVxinyouPrice + "]");
        	VxinyouHelper.IABResultCallBack(IAB_BUY_FAIL);
        	finish();
		}
	}
	
	private void startLogin()
	{
		INFO(TAG, "[startLogin]");
		try{
			Thread mThread = new Thread() {
				@Override
				public void run() {
					long time = System.currentTimeMillis();
					String checkKey = XPlayer.md5(VxinyouHelperConst.STR_APP_ID + time + VxinyouHelperConst.STR_APP_KEY);
					DBG(TAG, "checkKey=" + checkKey);
					
					LoginResult mLoginResult = mVxinyouLogin.login(time, checkKey);
					
					if (mLoginResult != null)
					{
						DBG(TAG, "authorizationCode:" + mLoginResult.authorizationCode + " status:" + mLoginResult.status + " message:" + mLoginResult.message);
						
						if(mLoginResult.authorizationCode != null)
							VxinyouHelper.GetAccessToken(mLoginResult.authorizationCode, time);
						else
						{
							ERR(TAG, "LoginResult failed!");
							VxinyouHelper.IABResultCallBack(IAB_BUY_FAIL);
							finish();
						}
					}
					else
					{
						ERR(TAG, "LoginResult null!");
						VxinyouHelper.IABResultCallBack(IAB_BUY_FAIL);
						finish();
					}
				}
			};
			mThread.start();
		}catch(Exception ex)
		{
			DBG_EXCEPTION(ex);
			VxinyouHelper.IABResultCallBack(IAB_BUY_FAIL);
			mInstance.finish();
		}
	}
	
	public static void startPayment(String billno)
	{
		try{
			mbillno = billno;
			Thread thread = new Thread() {
				@Override
				public void run() {
					Pay mPay = new Pay();
					mPay.payamt = Float.valueOf(mVxinyouPrice);
					mPay.billno = mbillno;
					mPay.appname = GAME_NAME_STR;
					mPay.appserverid = "serverqqq";
					mPay.appservername = "Service 1";
					
					Gson mGson = new Gson();
					String payJson = mGson.toJson(mPay);
					PayResult mPayResult = mVxinyouPay.pay(payJson);
					
					if(mPayResult != null)
					{
						DBG(TAG,"PayResult message:" + mPayResult.message + " PayResult status:" + mPayResult.status);
						
						if(mPayResult.status == PAYMENT_SUCCESS)
							VxinyouHelper.IABResultCallBack(IAB_BUY_OK);
						else
							VxinyouHelper.IABResultCallBack(IAB_BUY_FAIL);
							
						mInstance.finish();
					}
					else
					{
						ERR(TAG, "PayResult null!");
						VxinyouHelper.IABResultCallBack(IAB_BUY_FAIL);
						mInstance.finish();
					}
				}
			};
			thread.start();
		}catch(Exception ex)
		{
			DBG_EXCEPTION(ex);
			VxinyouHelper.IABResultCallBack(IAB_BUY_FAIL);
			mInstance.finish();
		}
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		if (mVxinyouPay != null)
			mVxinyouPay.unRegisterPayNotify();
		exitApp();
	}
	
	private void startApp() 
	{
		INFO(TAG, "[startApp]");
		VxinyouLog mVxinyouLog = new VxinyouLog(this);
		mVxinyouLog.startApp();
	}

	private void exitApp() 
	{
		INFO(TAG, "[exitApp]");
		VxinyouLog mVxinyouLog = new VxinyouLog(this);
		mVxinyouLog.exitApp();
	}
	
	class Response 
	{
		public int rstate;
		public List<Data> data;
	}

	class Data 
	{
		public String appid;
		public String appserverid;
		public String billno;
		public long time;
		public String appuserid;
		public String appusername;
		public float amt;
		public int currenttype;
		public String prcid;
		public String prcname;
	}

	static class Pay 
	{
		public String billno;
		public float payamt;
		public String appname;
		public String appserverid;
		public String appservername;
	}
}
#endif
