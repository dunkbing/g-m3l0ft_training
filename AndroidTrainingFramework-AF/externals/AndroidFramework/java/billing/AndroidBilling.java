#if USE_BILLING
package APP_PACKAGE.billing;

#if USE_HEP_EXT_IGPINFO
import APP_PACKAGE.*;
import android.content.IntentFilter;
#endif //USE_HEP_EXT_IGPINFO
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.InputStream;
import java.io.IOException;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import java.util.HashMap;
import android.provider.Settings.Secure;

import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;

import android.os.Bundle;
import android.os.Looper;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.text.Html;

import APP_PACKAGE.billing.common.AModelActivity;
import APP_PACKAGE.billing.common.UserInfo;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.R;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import android.webkit.WebView;
import android.webkit.WebViewClient;

#if USE_BOKU_FOR_BILLING
	import com.boku.mobile.api.IntentProvider;
	import com.boku.mobile.api.Transaction;
#endif
public class AndroidBilling extends AModelActivity implements Runnable
{
	SET_TAG("Billing");

	HashMap<String,String> map = new HashMap<String,String>();
	ProgressDialog dialog = null;
	class MyWebViewClientWO extends WebViewClient 
	{
		AndroidBilling mABActivity = null;
		
		public MyWebViewClientWO(AndroidBilling ab)
		{
			super();
			mABActivity = ab;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, android.graphics.Bitmap  favicon)
		{
			DBG(TAG, "onPageStarted "+url);
			if(dialog == null)
			{
				dialog = new ProgressDialog(AndroidBilling.this);
			}
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(getString(R.string.AB_ENTER_UNLOCK_CODE_PROGRESS));
			try
			{
				dialog.show();
			}
			catch(Exception ex)
			{
				DBG_EXCEPTION(ex);
			}
		}
	
		@Override
		public void onPageFinished(WebView view, String url)
		{
			DBG(TAG, "onPageFinished "+url);
				try
				{
					if(dialog == null)
					{
						dialog = new ProgressDialog(AndroidBilling.this);
					}
					else
						dismissDialog();

					mABActivity.setWAPNextButtonVisibilityWO();
				}
				catch (Exception ex)
				{
					DBG_EXCEPTION(ex);
				}
		}
		
		private void dismissDialog()
		{
			if(dialog != null)
       	 	{
   				dialog.dismiss();
       	 		dialog =null;
       	 	}
		}
	
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			DBG(TAG, "shouldOverrideUrlLoading "+url);
			if( map != null && map.size() != 0 )
			{
				DBG(TAG, "Reload webView with headers");
				view.loadUrl(url, map);
			}
			else
				view.loadUrl(url);
			return true;
		}
		@Override
		public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
			webView.loadUrl("about:blank");
			DBG(TAG, "onReceivedError "+failingUrl);
			DBG(TAG, "onReceivedError code:"+errorCode);
			DBG(TAG, "onReceivedError description:"+description);
			
			dismissDialog();
			
			if(mDialog!=null)
			{
				mDialog.setCancelable(false)
				.setPositiveButton(getString(R.string.SKB_OK), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						//No action stuff here :P
						setState(AB_STATE_FINALIZE);
					}
				}
				);
				
				AlertDialog alert = mDialog.create();
				// alert.setTitle(mTitle);
				alert.setMessage(getString(R.string.AB_ACTIVATE_WIFI));
				
				alert.setIcon(R.drawable.icon);
				alert.show();
			}
		}
				
		@Override
		public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error){
			DBG(TAG, "onReceivedSslError error:"+error.toString());
			// super.onReceivedSslError(view, handler, error);
			handler.proceed();
		}
	}
	
	
	class MyWebViewClient extends WebViewClient 
	{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			view.loadUrl(url);
			return true;
		}
	}
	
	public TelephonyManager mDeviceInfo;
	
	private final boolean DEBUG 			= true;

	static final int RETRY_SLEEP_TIME		= 1000;
	static final int RETRY_MAX				= 3;
	static int retryCount = 0;

	private  boolean mAndroidBStarted 	= false;

	AssetManager mAssetManager;
	 
	public final int AB_STATE_GET_FULL_VERSION_QUESTION				= 0;
	public final int AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS			= 1;
	public final int AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST	= 2;
	public final int AB_STATE_PB_TRANSACTION_FAILED					= 3;
	public final int AB_STATE_PB_TRANSACTION_SUCCESS				= 4;
	public final int AB_STATE_MANUAL_UNLOCK							= 5;
	public final int AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW			= 6;
	public final int AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST	= 7;
	public final int AB_STATE_CREATE_NEW_ACCOUNT_LOGIN				= 8;
	public final int AB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT		= 9;
	public final int AB_STATE_CC_TRANSACTION_FAILED					= 10;
	public final int AB_STATE_CC_TRANSACTION_SUCCESS				= 11;
	public final int AB_STATE_CC_CREATE_ACCOUNT						= 12;
	public final int AB_STATE_CC_PLEASE_WAIT_PURCHASE_PROGRESS		= 13;
	public final int AB_STATE_CREATE_NEW_ACCOUNT_EMAIL_EXISTS		= 14;
	public final int AB_STATE_CC_CREATE_NEW_ACCOUNT_PORTRAIT		= 15;
	public final int AB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA			= 16;
	public final int AB_STATE_CREDIT_CARD_EXPIRED					= 17;
	public final int AB_STATE_ENTER_UNLOCK_CODE						= 18;
	public final int AB_STATE_ENTER_UNLOCK_CODE_PROGRESS			= 19;
	public final int AB_STATE_ENTER_UNLOCK_CODE_SUCCESS				= 20;
	public final int AB_STATE_ENTER_UNLOCK_CODE_FAILED				= 21;
	public final int AB_STATE_LOGIN_WRONG_EMAIL_PASSWORD			= 22;
	public final int AB_STATE_PAY_WO_ACCOUNT						= 23;
	public final int AB_STATE_PAY_WO_ACCOUNT_REQUEST				= 24;
	public final int AB_STATE_CC_LOGIN_BUY_NOW						= 25;
	public final int AB_STATE_CC_LOGIN_BUY_NOW_REQUEST				= 26;
	public final int AB_STATE_CC_USERBILL_BUY_NOW					= 27;
	public final int AB_STATE_CC_USERBILL_BUY_NOW_REQUEST			= 28;
	public final int AB_STATE_CC_CREATE_ACCOUNT_BUY_NOW				= 29;
	public final int AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW = 30;
	public final int AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST = 31;
	public final int AB_STATE_CC_FORGOT_PASSWORD					= 32;
	public final int AB_STATE_CC_FORGOT_PASSWORD_REQUEST			= 33;
	public final int AB_STATE_CC_FORGOT_PASSWORD_RESULT				= 34;
	public final int AB_STATE_NO_DATA_CONNECTION_DETECTED			= 35;
	public final int AB_STATE_GAME_PURCHASED						= 36;
	public final int AB_STATE_INITIALIZE							= 37;
	public final int AB_STATE_VERIFYING_PURCHASE					= 38;
	public final int AB_STATE_WAP_BILLING							= 39;
	public final int AB_STATE_PAYPAL								= 40;
	public final int AB_STATE_PREPRARE_WAP_DATA_CONNECTION 			= 41;
	public final int AB_STATE_READ_GAME_CODE						= 42;
	public final int AB_STATE_NO_PROFILE_DETECTED					= 43;
	public final int AB_STATE_NO_SIMCARD_DETECTED					= 44;
	public final int AB_STATE_PLEASE_WAIT_PURCHASE_IPX_CONT			= 45;
	public final int AB_STATE_PREPARE_WAP_OTHER_BILLING				= 46;
	public final int AB_STATE_WAP_OTHER_BILLING						= 47;
	public final int AB_STATE_FINALIZE 								= 48;
	
	
	public final int SUBSTATE_INIT = 1;
	public final int SUBSTATE_WAITING = 2;
	public final int SUBSTATE_HANDLING = 3;
	
	private final String mStates[] ={
			"AB_STATE_GET_FULL_VERSION_QUESTION",//0
			"AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS",//1
			"AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST",//2
			"AB_STATE_PB_TRANSACTION_FAILED",//3
			"AB_STATE_PB_TRANSACTION_SUCCESS",//4
			"AB_STATE_MANUAL_UNLOCK",//5
			"AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW",//6
			"AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST",//7
			"AB_STATE_CREATE_NEW_ACCOUNT_LOGIN",//8
			"AB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT",//9
			"AB_STATE_CC_TRANSACTION_FAILED",//10
			"AB_STATE_CC_TRANSACTION_SUCCESS",//11
			"AB_STATE_CC_CREATE_ACCOUNT",//12
			"AB_STATE_CC_PLEASE_WAIT_PURCHASE_PROGRESS",//13
			"AB_STATE_CREATE_NEW_ACCOUNT_EMAIL_EXISTS",//14
			"AB_STATE_CC_CREATE_NEW_ACCOUNT_PORTRAIT",//15
			"AB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA",//16
			"AB_STATE_CREDIT_CARD_EXPIRED",//17
			"AB_STATE_ENTER_UNLOCK_CODE",//18
			"AB_STATE_ENTER_UNLOCK_CODE_PROGRESS",//19
			"AB_STATE_ENTER_UNLOCK_CODE_SUCCESS",//20
			"AB_STATE_ENTER_UNLOCK_CODE_FAILED",//21
			"AB_STATE_LOGIN_WRONG_EMAIL_PASSWORD",//22
			"AB_STATE_PAY_WO_ACCOUNT",//23
			"AB_STATE_PAY_WO_ACCOUNT_REQUEST",//24
			"AB_STATE_CC_LOGIN_BUY_NOW",//25
			"AB_STATE_CC_LOGIN_BUY_NOW_REQUEST",//26
			"AB_STATE_CC_USERBILL_BUY_NOW",//27
			"AB_STATE_CC_USERBILL_BUY_NOW_REQUEST",//28
			"AB_STATE_CC_CREATE_ACCOUNT_BUY_NOW",//29
			"AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW",//30
			"AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST",//31
			"AB_STATE_CC_FORGOT_PASSWORD",//32
			"AB_STATE_CC_FORGOT_PASSWORD_REQUEST",//33
			"AB_STATE_CC_FORGOT_PASSWORD_RESULT",//34
			"AB_STATE_NO_DATA_CONNECTION_DETECTED",//35
			"AB_STATE_GAME_PURCHASED",//36
			"AB_STATE_VERIFYING_PURCHASE",//37
			"AB_STATE_INITIALIZE",//38
			"AB_STATE_WAP_BILLING",//39
			"AB_STATE_PAYPAL",//40
			"AB_STATE_PREPRARE_WAP_DATA_CONNECTION",//41
			"AB_STATE_READ_GAME_CODE",//42
			"AB_STATE_NO_PROFILE_DETECTED",//43
			"AB_STATE_NO_SIMCARD_DETECTED",//44
			"AB_STATE_PLEASE_WAIT_PURCHASE_IPX_CONT",//45
			"AB_STATE_PREPARE_WAP_OTHER_BILLING",//46
			"AB_STATE_WAP_OTHER_BILLING",//47
			"AB_STATE_FINALIZE",//48
			};
	
	
	private String mGameName = "Asphalt 5"; // preprocess instead of hardcoding
	// private String mGameName = getString(R.string.app_name); //preprocess
	// instead of hardcoding
	private String mGamePrice = null;   //real value must be taken from server.
	private String mTCSText = null;
	
	public  int mSubstate;
	public  int mPrevSubstate; 

	//public  int mState;
	public  int mPrevState;

	public int m_nLogoStep;
	private long m_timeOut = 0;
	private int DELAY_TIMEOUT = 45000;
	private int FAIL_TIMEOUT = 180000;
	private int DELAY_REQUEST_TIMEOUT = 5000;
	private int DELAY_FOR_LOAD = 1000;
	
	public boolean isPaused = false;

	WifiManager	mWifiManager;
	ConnectivityManager mConnectivityManager;
	WifiLock		mWifiLock;
	
	private boolean m_bWasWifiEnabled	= false;
	private boolean mVerifyPurchase		= false;
	WebView webView = null;
	String MAGIC_WORD="wapunlockpurchaseattemptdone";
	//String MAGIC_WORD="Noticias";
	AlertDialog.Builder mPurchaseDialog;
	AlertDialog.Builder mDialog;
	
	public static void LaunchBilling()
	{
	  DBG("Billing","Launching billing "+SUtils.getContext());
		Intent TnBIntent = new Intent(SUtils.getContext(), AndroidBilling.class);
		SUtils.getContext().startActivity(TnBIntent);
	}
	
	private interface BUTTONS
	{
		public static final int OK_ONLY 	= 0;
		public static final int OK_NO 		= 1;
		public static final int OK_CANCEL 	= 2;
		public static final int RETRY_ONLY	= 3;
	}
	
	private boolean mbBillingMRC = false;
		public int mCheckTimes = 0;
	
	
	
	int mStatus;
	static final int STATUS_NO_ERROR					=  0;
	
	boolean mBillingStarted;
#if USE_HEP_EXT_IGPINFO
	public static boolean gIsRunning;
#endif //USE_HEP_EXT_IGPINFO	
	int mState, mOldState, mTryAgainState;
	
	public final int STATE_LOADING					= 0;
	public final int STATE_SELECT_PAY_MODE			= 1;
	public final int STATE_PAY_ON_BILL				= 2;
	public final int STATE_PAY_WITH_CREDIT_CARD		= 3;
	public final int STATE_PAY_SUCCESS				= 4;
	public final int STATE_PAY_ERROR				= 5;
	public final int STATE_CONFIRM_EXIT				= 6;
	public final int STATE_VALIDATING_LICENSE		= 7;
	public final int STATE_VALIDATE_SUCCESS			= 8;
	public final int STATE_FINALIZE					= 9;
	
	int mSelectPayModeButtons[] = {};
	int mButtonsYesNo[] 	= {};
	int mButtonsOK[]		= {};
	
	int mTitle, mMessage, mErrorMessage;
	int mButtons[];

	public static final int NO_ACTION		= -1;
	
	//BillingView mView;
	//device info for Billing
	Device mDevice;
	//Model mBilling;
	public XPlayer xplayer;
	ServerInfo mServerInfo;
	boolean m_bIsSingleBill = false;
	boolean mb_isNextActivated = false;
	boolean mb_waitingActivationWord = false;
	String mPromoCode = null;
	
	private ProgressDialog mProgressBar;  
	#define BOKU_API_KEY					"NvnTfQ4ZhF2ON6e3dOwbEbLdMZRsOjiQLqTWuza6PfWWITTkAgnZUm9cnfoJICg7513VZZGiQBPzYx5bB0azj6OLTUP63a96cfE0"
	
	
	public AndroidBilling() {
#if USE_HEP_EXT_IGPINFO	
		if(SUtils.getContext() == null)
#endif //USE_HEP_EXT_IGPINFO		
		SUtils.setContext(this);
	}
		
	public boolean canInterrupt = false;
	private static boolean isInBoku = false;
	public void run() {
		DBG(TAG,"Android Billing...");
		Looper.prepare();
		setState(AB_STATE_INITIALIZE);
		
		while (mState != AB_STATE_FINALIZE
	#if USE_BOKU_FOR_BILLING	
		&& !isInBoku
	#endif	
		) 
		{
			canInterrupt = false;
			long time = System.currentTimeMillis();
			
			update();
			time = 10;
			if(time>0) {
				try { 
					Thread.sleep(time);
				}	catch(Exception e){};
			}
			canInterrupt = true;
		}
		mAssetManager = null;
#if USE_HEP_EXT_IGPINFO		
		gIsRunning = false;
		// return to game after exit billing
		try {
		 #if USE_BOKU_FOR_BILLING	
			if(!isInBoku)
		#endif
			if (getPackageManager().getActivityInfo(new android.content.ComponentName(STR_APP_PACKAGE, STR_APP_PACKAGE + ACTIVITY_NAME_DOT), 0).launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE)
			{
				Intent intent = new Intent(AndroidBilling.this, APP_PACKAGE.CLASS_NAME.class);
				startActivity(intent);
			}
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			DBG_EXCEPTION(e);
		}
#endif //USE_HEP_EXT_IGPINFO
		isInBoku = false;		
		finish();
	}
	
	public boolean launchGame = true;
	public int mStillHasError = 0;

	public int wifiStep = 0;
	public int netLookupRetry = 0;
	boolean mDeviceInfoStarted=false;

	public void update ()
	{
		switch(mState)
		{
			case AB_STATE_INITIALIZE:

				InitDeviceInfo();
			#if USE_BILLING_FOR_CHINA
				boolean gamePurchased = false;
				// check purchased for IPX
				if(mServerInfo.getBillingType() != null && mServerInfo.getBillingType().equals("IPX") &&
					mServerInfo.getCurrentProfileSelected().getIPXSMSCount() != 0 &&
					mServerInfo.getCurrentProfileSelected().getIPXSentCount() == mServerInfo.getCurrentProfileSelected().getIPXSMSCount())
				{
					SUtils.getLManager().saveUnlockGame(1);
					notifyUnlock();
					gamePurchased = true;
				}
				// check purchased for UMP
				if(mServerInfo.getBillingType() != null && mServerInfo.getBillingType().equals("UMP") &&
					SUtils.getPreferenceString(SUtils.getLManager().PREFERENCES_SMS_UMP_PENDING, null, SUtils.getLManager().PREFERENCES_NAME) != null)
				{
					String lastOrderId = SUtils.getPreferenceString(SUtils.getLManager().PREFERENCES_SMS_UMP_PENDING, null, SUtils.getLManager().PREFERENCES_NAME);
					String checkServer = mServerInfo.getCurrentProfileSelected().getUmp_get_transid();
					String timestamp = System.currentTimeMillis()/1000 + "";
					String query = "transactionid="+lastOrderId+"&timestamp="+timestamp+"&imei="+mDevice.getIMEI()+"&sign="+XPlayer.md5(lastOrderId+"_"+mDevice.getIMEI()+"_"+timestamp+"_UMPGAMELOFT");
					
					XPlayer mXPlayer = new XPlayer(mDevice);
					mXPlayer.sendRequest(checkServer, query);
					while (!mXPlayer.handleRequest())
					{
						try {
							Thread.sleep(50);
						} catch (Exception exc) {}
						DBG("Billing", "[getUMPBillingResult]Waiting for response");
					}
					String tmpHR = mXPlayer.getWHTTP().m_response;
					if(tmpHR != null && tmpHR.contains("SUCCESS"))
					{
						DBG("Billing", "UMP R4 last unlock success");
						SUtils.getLManager().saveUnlockGame(1);
						gamePurchased = true;
					}
					else
					{
						DBG("Billing", "UMP R4 last unlock fail");
					}
					// remove pending transaction
					SUtils.setPreference(SUtils.getLManager().PREFERENCES_SMS_UMP_PENDING, null, SUtils.getLManager().PREFERENCES_NAME);
				}
				if(gamePurchased || SUtils.getLManager().ValidateGame())
			#else
				if(SUtils.getLManager().ValidateGame())
			#endif				
				{
					setState(AB_STATE_GAME_PURCHASED);
				}
				else
				{
				#if USE_BILLING_FOR_CHINA
					if(mServerInfo.getBillingType() != null && (mServerInfo.getBillingType().equals("IPX") || mServerInfo.getBillingType().equals("UMP")) && !isSIMReady())
					{
						DBG(TAG,"******No sim for china billing********");
						setState(AB_STATE_NO_SIMCARD_DETECTED);
					}
					else if(mServerInfo.getBillingType() != null && mServerInfo.getBillingType().equals("IPX") &&
							mServerInfo.getCurrentProfileSelected().getIPXSMSCount() == 0) // can not get lookup IPX
					{
						DBG(TAG,"******IPX lookup not avaiable********");
						setState(AB_STATE_NO_DATA_CONNECTION_DETECTED);
					}
					else
				#else
#if USE_SIM_ERROR_POPUP
					TelephonyManager mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
					if( mDeviceInfo.getSimState() == TelephonyManager.SIM_STATE_ABSENT || ( SUtils.isAirplaneModeOn() && SUtils.hasConnectivity() == 1 ) )
					{
						setState(AB_STATE_NO_SIMCARD_DETECTED);
					}
					else
#endif //USE_SIM_ERROR_POPUP
				#endif
					if (!mDevice.getServerInfo().isProfileSelected())
					{
						if (mServerInfo.getUnlockProfile().getBilling_type().equals("N/A"))
						{
							DBG(TAG,"******No profile for this operator********");							
							setState(AB_STATE_NO_PROFILE_DETECTED);
						}
						else
						{
							DBG(TAG,"******No connection********");
							setState(AB_STATE_NO_DATA_CONNECTION_DETECTED);
						}
					}
					#if USE_VOUCHERS
					else
					{
						DBG(TAG,"Going to AB_STATE_GET_FULL_VERSION_QUESTION");
						setState(AB_STATE_GET_FULL_VERSION_QUESTION);
					}
					#else
					else if(mDevice.getServerInfo().getBillingType().equals("CC"))	
					{	
						DBG(TAG,"Going to AB_STATE_CC_CREATE_ACCOUNT");
						#if USE_TNB_CC_PROFILE
							setState(AB_STATE_CC_CREATE_ACCOUNT);
						#else
							setState(AB_STATE_NO_PROFILE_DETECTED);
						#endif
					}
					else if(mDevice.getServerInfo().getBillingType().equals("WO"))	
					{	
						DBG(TAG,"Going to AB_STATE_PREPARE_WAP_OTHER_BILLING");
						#if USE_TNB_CC_PROFILE
							setState(AB_STATE_PREPARE_WAP_OTHER_BILLING);
						#else
							setState(AB_STATE_NO_PROFILE_DETECTED);
						#endif
						
					}
#if USE_BOKU_FOR_BILLING
					else if(mDevice.getServerInfo().getBillingType().equals("BOKU"))	
					{	
						BokuActivity.LaunchBokuBilling("",mDevice.getServerInfo());
						isInBoku = true;
					#if USE_HEP_EXT_IGPINFO
						gIsRunning = false;
					#endif
						return;
					}
#endif
					else
					{
						DBG(TAG,"Going to AB_STATE_GET_FULL_VERSION_QUESTION");
						setState(AB_STATE_GET_FULL_VERSION_QUESTION);
					}
					#endif//USE_VOUCHERS
				}
				break;
				
			
			case AB_STATE_VERIFYING_PURCHASE:
			if(SUtils.getLManager().ValidateGame() || trickDebugScreen)
			{
				
				//setState(AB_STATE_PB_TRANSACTION_SUCCESS);
				setState(AB_STATE_FINALIZE);
			}
			else
			{
				setState(AB_STATE_PB_TRANSACTION_FAILED);
			}
			if(m_bWasWifiEnabled)
			{
				mDevice.EnableWifi();
				m_bWasWifiEnabled = false;
				//DBG("ENABLING Wi-Fi!!!");
				while (mDevice.IsWifiEnabling())
				{
					//DBG("ENABLING Wi-Fi!!!");
				}
				DBG(TAG,"Wi-Fi IS NOW ENABLED!!!");
			}
			mVerifyPurchase = false;
			break;
			
			case AB_STATE_PREPRARE_WAP_DATA_CONNECTION:
				//ERR("ANDROID BILLING","WAP :P!!!!:P");
				if(mDevice.IsWifiEnable())
				{
					m_bWasWifiEnabled = true;
					mDevice.DisableWifi();
					m_phonedatatimeout = System.currentTimeMillis();
				}
				while(mDevice.IsWifiDisabling())
				{
					DBG(TAG,"Disabling Wi-Fi");
				}
				while (!mDevice.IsConnectionReady() && !TIMED_OUT)
				{
					DBG(TAG,"Waiting for transference on Phone Data Service");
					DBG(TAG,"m_phonedatatimeout="+m_phonedatatimeout);						
					DBG(TAG,"System.currentTimeMillis() - m_phonedatatimeout="+(System.currentTimeMillis() - m_phonedatatimeout));
					
					if (System.currentTimeMillis() - m_phonedatatimeout > PHONE_DATA_TIME_OUT)
						TIMED_OUT = true;
				}//HERE HERE HERE
				if(TIMED_OUT)
				{
				  setState(AB_STATE_NO_DATA_CONNECTION_DETECTED);
				}
				else
				{
				  setState(AB_STATE_WAP_BILLING);
				}
				TIMED_OUT = false;
			break;
			case AB_STATE_PREPARE_WAP_OTHER_BILLING:
				if(!mDevice.IsWifiEnable())
				{
					if(m_bWasWifiEnabled)
					{
						mDevice.EnableWifi();
						m_bWasWifiEnabled = false;
						//DBG("ENABLING Wi-Fi!!!");
						while (mDevice.IsWifiEnabling() || !hasConnectivity())
						{
							DBG(TAG,"ENABLING Wi-Fi!!!");
						}
						DBG(TAG,"Wi-Fi IS NOW ENABLED!!!");
						setState(AB_STATE_PREPARE_WAP_OTHER_BILLING);
					}
					else
						setState(AB_STATE_NO_DATA_CONNECTION_DETECTED);
				}
				else
				{
					// if(mDevice.getServerInfo().getBillingType().equals("WO"))
					// {
						boolean errorPresent = false;
						try{
							// md5(ppdwap_M900_4.99_1234567898765)
							// String itemPrice = mServerInfo.getGamePrice();
							String verify = md5("ppdwap_"+Device.getDemoCode()+"_"+mDevice.getServerInfo().getProfileId()+"_"+mDevice.getIMEI()+"_g4m3l0ft");
							DBG(TAG,"Verify string: "+"ppdwap_"+Device.getDemoCode()+"_"+mDevice.getServerInfo().getProfileId()+"_"+mDevice.getIMEI()+"_g4m3l0ft");
							
							String httpResponseString = getTimeStampRequest(mDevice.getServerInfo().getURLbilling());
							if (httpResponseString == null)
							{
								setState(AB_STATE_NO_DATA_CONNECTION_DETECTED);
								return;
							}
							httpResponseString = httpResponseString.substring(2, httpResponseString.length()-2);
							MEncoder encode = new MEncoder((md5(verify)).substring(0,16), verify.substring(6,verify.length())+httpResponseString);
							String uID = "tnbok_ppdwap_"+Device.getDemoCode()+"_"+mDevice.getServerInfo().getProfileId()+"_"+mDevice.getIMEI()+"_g4m3l0ft";
							String tnbid = MEncoder.bytesToHex( encode.encrypt(uID) );
  
							map.put("x-up-gl-imei",mDevice.getIMEI());
							map.put("x-up-gl-tnbid",tnbid);
							map.put("x-up-gl-verify",verify);
							#if HDIDFV_UPDATE
							map.put("x-up-gl-hdidfv",mDevice.getHDIDFV());
							#endif

  
							DBG(TAG, "Timestamp:"+httpResponseString);
							DBG(TAG, "Android ID:"+uID);
							DBG(TAG, "Complete verify (MD5):"+verify);
							DBG(TAG, "Vector:"+verify.substring(0,16));
							DBG(TAG, "Security Key:"+(verify.substring(6,verify.length())+httpResponseString));
							DBG(TAG, "Adding imei to webView headers:"+mDevice.getIMEI());
							#if HDIDFV_UPDATE
							DBG(TAG, "Adding hdidfv to webView headers:"+mDevice.getHDIDFV());
							#endif
							DBG(TAG, "Adding verify to webView headers:"+verify);
							DBG(TAG, "Adding tnbid to webView headers:"+tnbid);
						}catch(Exception e){
							errorPresent = true;
						};
					// }
					if(errorPresent)
						setState(AB_STATE_NO_DATA_CONNECTION_DETECTED);
					else
						setState(AB_STATE_WAP_OTHER_BILLING);
				}
					
			break;
			case AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST:
			case AB_STATE_PLEASE_WAIT_PURCHASE_IPX_CONT:
				if (System.currentTimeMillis() - m_timeOut > DELAY_TIMEOUT && mServerInfo.getBillingType().equals("SMS"))
				{
					setTextViewNewText( R.id.lblMessagePleaseWait, R.string.AB_SMS_SENT_WAIT_FOR_RESPONSE, -1);
				}
				if (System.currentTimeMillis() - m_timeOut > FAIL_TIMEOUT  && mServerInfo.getBillingType().equals("SMS"))
				{
					DBG(TAG,"Exiting the SMS Process!!!!");
					mBilling.FailSMSbyTime();
				}
			#if USE_BILLING_FOR_CHINA
				else if(System.currentTimeMillis() - m_timeOut > FAIL_TIMEOUT  && mServerInfo.getBillingType().equals("IPX"))
				{
					DBG(TAG,"Exiting the SMS IPX Process!!!!");
					mBilling.FailSMSbyTime();
				}
			#endif
				else
				{
					updatePayOnBill();
				}
			break;
			
            case AB_STATE_PAY_WO_ACCOUNT_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{
					if(mBilling == null)
					{
						mBilling = new Model(this, mDevice);
						mBilling.sendSingleBillRequest();
						mSubstate = SUBSTATE_WAITING;
					}
            	}
        	break;
        
            case AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST:
            case AB_STATE_CC_LOGIN_BUY_NOW_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{	
					if(mBilling == null)
					{
						mBilling = new Model(this, mDevice);
						mBilling.sendLoginRequest();
						mSubstate = SUBSTATE_WAITING;
					}
            	}
        	break;
        	
            case AB_STATE_CC_USERBILL_BUY_NOW_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{	
					if(mBilling == null)
					{
						mBilling = new Model(this, mDevice);
						mBilling.sendUserBillRequest();
						mSubstate = SUBSTATE_WAITING;
					}
            	}
			break;
        
		
            case AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{	
					if(mBilling == null)
					{
						mBilling = new Model(this, mDevice);
						mBilling.sendNewUserBillRequest();
						mSubstate = SUBSTATE_WAITING;
					}
            	}
        	break;
        	
            case AB_STATE_CC_FORGOT_PASSWORD_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{	
					if(mBilling == null)
					{
						mBilling = new Model(this, mDevice);
						mBilling.sendForgotPasswordRequest();
						mSubstate = SUBSTATE_WAITING;
					}
            	}
        	break;
        	
            case AB_STATE_ENTER_UNLOCK_CODE_PROGRESS:
            	if(SUtils.getLManager().ValidatePromoCode(mPromoCode,mDevice.getServerInfo().getPromoCodeURL()))
				{
            		DBG(TAG, "GOT TRUE FOR "+mPromoCode);
            		setState(AB_STATE_ENTER_UNLOCK_CODE_SUCCESS);
				}
            	else
            	{
            		DBG(TAG, "GOT FALSE FOR "+mPromoCode);
            		setState(AB_STATE_ENTER_UNLOCK_CODE_FAILED);
            	}
            break;
			
            case AB_STATE_WAP_BILLING:

            	if((mb_waitingActivationWord) && !(mb_isNextActivated))
            	{
            		setWAPNextButtonVisibility();
            	}
            break;
            
            default:
			break;
		}
	}
	
	private void ShowDialog(String mTitle,String mText,int mButtons)
	{
		switch(mButtons)
		{
		case BUTTONS.OK_ONLY:
			mDialog.setCancelable(false)
			.setPositiveButton(getString(R.string.SKB_OK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					//No action stuff here :P
				}
			}
			);
		break;
		
		case BUTTONS.OK_NO:
			mDialog.setCancelable(false)
			.setPositiveButton(getString(R.string.SKB_OK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					//Add action stuff here :P
				}
			}
			);
			mDialog.setNegativeButton(getString(R.string.SKB_NO), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					//Add action stuff here :P
				}
			}
			);
		break;
		
		case BUTTONS.OK_CANCEL:
			mDialog.setCancelable(true)
			.setPositiveButton(getString(R.string.SKB_OK), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					//Add action stuff here :P
				}
			}
			);
			mDialog.setNegativeButton(getString(R.string.SKB_CANCEL), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					//Add action stuff here :P
				}
			}
			);
		break;
		
		case BUTTONS.RETRY_ONLY:
			mDialog.setCancelable(false)
			.setPositiveButton(getString(R.string.SKB_RETRY), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					//No action stuff here :P
				}
			}
			);
		break;
		}
		
		AlertDialog alert = mDialog.create();		
		alert.setTitle(mTitle);
		alert.setMessage(mText);		
		
		alert.setIcon(R.drawable.icon);
		alert.show();
	}
	
	protected void handleBackKey()
	{
		if(
			  (mState == AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST)
			||(mState == AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST)
			||(mState == AB_STATE_PAY_WO_ACCOUNT_REQUEST)
			||(mState == AB_STATE_CC_LOGIN_BUY_NOW_REQUEST)
			||(mState == AB_STATE_CC_USERBILL_BUY_NOW_REQUEST)
			||(mState == AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST)
			||(mState == AB_STATE_CC_FORGOT_PASSWORD_REQUEST))
		{
			return;
		}
		
		  if (mState == AB_STATE_CC_FORGOT_PASSWORD_RESULT)
		  {
			  setState(AB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
		  	  return;
		  }
		  

		switch(mCurrentLayout)
    	{
        	//Back Buttons for all states
        	case R.layout.ab_layout_create_new_account_email_exists:
        		setState(mPrevState);
        	break;
        	
        	case R.layout.ab_layout_forgot_password:
            	setState(AB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
            break;
        	
        	case R.layout.ab_layout_login_wrong_email_password:
        	case R.layout.ab_layout_login:
        	case R.layout.ab_layout_pay_wo_account:
        	case R.layout.ab_layout_credit_card_expired:
        		setState(AB_STATE_CC_CREATE_ACCOUNT);
        	break;
        		
        	case R.layout.ab_layout_tcs:
        		goToLayout(mPrevLayout);
        	break;
    	}
	}

	public void handleBack(int buttonId)
	{
    	switch(buttonId)
    	{
        	//Back Buttons for all states
        	case R.id.bt_ly_create_new_account_email_exists_back:
        		setState(mPrevState);
        	break;
        	
        	case R.id.bt_ly_forgot_password_back:
            	setState(AB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
            break;
        	
        	case R.id.bt_ly_login_wrong_email_password_back:
        	case R.id.bt_ly_login_back:
        	case R.id.bt_ly_pay_wo_account_back:
        	case R.id.bt_ly_credit_card_expired_back:
        		setState(AB_STATE_CC_CREATE_ACCOUNT);
    	}
	}
	
	
	public void setState(int state)
    {
    	DBG(TAG,"Setting State to: "+mStates[state]);
		switch(state)
    	{
    		
    		case AB_STATE_GET_FULL_VERSION_QUESTION:
			#if USE_VOUCHERS
			goToLayout(R.layout.ab_layout_get_full_version_promo_curys_question);
			#else
            if(mDevice.getServerInfo().getPromoCode()==1)
            {
            	goToLayout(R.layout.ab_layout_get_full_version_promo_question);
            }
            else if(mDevice.getServerInfo().searchForAditionalProfile("MRC"))
            {
            	mbBillingMRC = true;
            	goToLayout(R.layout.ab_layout_get_full_version_mrc_question);
            }
			#if USE_BILLING_FOR_CHINA
				else if(mServerInfo.getBillingType().equals("UMP") || mServerInfo.getBillingType().equals("IPX"))
				{
					goToLayout(R.layout.ab_layout_get_full_version_china_question);
				}
			#endif
            else
            {
            	goToLayout(R.layout.ab_layout_get_full_version_question);
            }
			#endif
    		break;
    		
    		case AB_STATE_VERIFYING_PURCHASE:
    			m_timeOut = System.currentTimeMillis();//testing
    			goToLayout(R.layout.ab_layout_please_wait_purchase_progress);
    		break;
    		
    		case AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS:
    			m_timeOut = System.currentTimeMillis();//testing
    			goToLayout(R.layout.ab_layout_please_wait_purchase_progress);
    		break;
    		
    		case AB_STATE_PB_TRANSACTION_FAILED:
    			if(mbBillingMRC)
        			goToLayout(R.layout.ab_layout_mrc_transaction_failed);
    			else
    				goToLayout(R.layout.ab_layout_transaction_failed);
    		break;
    		
    		case AB_STATE_PB_TRANSACTION_SUCCESS:
				if((mDevice.getBillingVersion().equals("3")) && (mbBillingMRC))
				{
	    			goToLayout(R.layout.ab_layout_thanks_for_the_purchase_v3);
	    			DBG(TAG,"Going to Save MRC value");
	    			SUtils.getLManager().saveUnlockGame(2);
				}
	    		else
	    		{
	    			goToLayout(R.layout.ab_layout_thanks_for_the_purchase);
	    			SUtils.getLManager().saveUnlockGame(1);
	    		}
    		break;
    		
    		case AB_STATE_CC_CREATE_ACCOUNT:
    			m_bIsSingleBill=false;
    			if (!(mServerInfo.getBillingType().equals("CC")))
    				mServerInfo.searchForAditionalProfile("CC");
    			
    			mGamePrice = mServerInfo.getGamePriceFmt();		
    			mTCSText = mServerInfo.getTNCString();
    			
    			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            	getRequestedOrientation();
            	goToLayout(R.layout.ab_layout_create_new_account_portrait);
    		break;
    		
    		case AB_STATE_CREATE_NEW_ACCOUNT_LOGIN:
    			goToLayout(R.layout.ab_layout_login);
    		break;
    		
    		case AB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT:
    			goToLayout(R.layout.ab_layout_pay_wo_account);
    		break;
    		
    		case AB_STATE_CC_TRANSACTION_FAILED:
    			goToLayout(R.layout.ab_layout_cc_transaction_failed);
        	break;
    		
    		case AB_STATE_CC_TRANSACTION_SUCCESS:
			SUtils.getLManager().saveUnlockGame(1);
    			goToLayout(R.layout.ab_layout_cc_thanks_for_the_purchase);
        	break;
    		
    		case AB_STATE_CC_PLEASE_WAIT_PURCHASE_PROGRESS:
    			goToLayout(R.layout.ab_layout_cc_please_wait_purchase_progress);
        	break;
    		
    		case AB_STATE_CREATE_NEW_ACCOUNT_EMAIL_EXISTS:
    			goToLayout(R.layout.ab_layout_create_new_account_email_exists);
            break;
        		
        	case AB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA:
        		goToLayout(R.layout.ab_layout_create_new_account_wrong_data);
            break;
                
        	case AB_STATE_CREDIT_CARD_EXPIRED:
        		goToLayout(R.layout.ab_layout_credit_card_expired);
            break;
        	
        	case AB_STATE_ENTER_UNLOCK_CODE:
        		goToLayout(R.layout.ab_layout_enter_unlock_code);
        	break;
        	
        	case AB_STATE_WAP_BILLING:
        		goToLayout(R.layout.ab_layout_wap_billing);        		
        	break;        	
					
			case AB_STATE_PREPARE_WAP_OTHER_BILLING:
				goToLayout(R.layout.ab_layout_wap_other_preparing);
			break;
			
        	case AB_STATE_WAP_OTHER_BILLING:
        		goToLayout(R.layout.ab_layout_wap_other_billing);        		
        	break;
        	
        	case AB_STATE_ENTER_UNLOCK_CODE_PROGRESS:
    			m_timeOut = System.currentTimeMillis();//testing
    			goToLayout(R.layout.ab_layout_please_wait_purchase_progress);
        	break;

        	case AB_STATE_ENTER_UNLOCK_CODE_SUCCESS:
        		goToLayout(R.layout.ab_layout_enter_unlock_code_success);
            break;
            	
        	case AB_STATE_ENTER_UNLOCK_CODE_FAILED:
        		#if USE_VOUCHERS
				goToLayout(R.layout.ab_layout_enter_unlock_code_failed_curys);
				#else
        		goToLayout(R.layout.ab_layout_enter_unlock_code_failed);
				#endif
        	break;
                    
            case AB_STATE_LOGIN_WRONG_EMAIL_PASSWORD:
            	goToLayout(R.layout.ab_layout_login_wrong_email_password);
            break;
            
            case AB_STATE_CC_FORGOT_PASSWORD:
            break;
            case AB_STATE_PAY_WO_ACCOUNT:
            case AB_STATE_CC_LOGIN_BUY_NOW:
            case AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW:
            case AB_STATE_CC_USERBILL_BUY_NOW:
            case AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW:
            	goToLayout(R.layout.ab_layout_cc_please_wait_purchase_progress);
            break;
            case AB_STATE_NO_DATA_CONNECTION_DETECTED:
            	goToLayout(R.layout.ab_layout_no_data_connection_detected);
			break;
            case AB_STATE_NO_SIMCARD_DETECTED:
#if USE_SIM_ERROR_POPUP

				goToLayout(R.layout.ab_layout_no_sim_detected);
            break;
#endif //USE_SIM_ERROR_POPUP
            case AB_STATE_NO_PROFILE_DETECTED:
				goToLayout(R.layout.ab_layout_no_profile_detected);
            break;
			case AB_STATE_PLEASE_WAIT_PURCHASE_IPX_CONT:
				goToLayout(R.layout.ab_layout_please_wait_purchase_progress);
			break;
    		default:
    		break;
    	}
		
		
		if (	(mState !=AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST)
			  &&(mState !=AB_STATE_PAY_WO_ACCOUNT_REQUEST)
			  &&(mState !=AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST)
			  &&(mState !=AB_STATE_CC_USERBILL_BUY_NOW_REQUEST)
			  &&(mState !=AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST)
			  &&(mState !=AB_STATE_CC_LOGIN_BUY_NOW_REQUEST)
			  &&(mState !=AB_STATE_CREDIT_CARD_EXPIRED)
			  &&(mState !=AB_STATE_CC_FORGOT_PASSWORD_RESULT)
			  &&(mState !=AB_STATE_CC_FORGOT_PASSWORD_REQUEST)
			  &&(mState !=AB_STATE_PLEASE_WAIT_PURCHASE_IPX_CONT))
			{
		
				if (state != mState)
					mPrevState = mState;
			}
		
		
		if (		(state ==AB_STATE_PAY_WO_ACCOUNT_REQUEST)
				  ||(state ==AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST)
				  ||(state ==AB_STATE_CC_USERBILL_BUY_NOW_REQUEST)
				  ||(state ==AB_STATE_CC_LOGIN_BUY_NOW_REQUEST)
				  ||(state ==AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST))
		{
			goToLayout(R.layout.ab_layout_cc_please_wait_purchase_progress);
		}
		
		
		//Screen for SMS and HTTP Billing
		if(state ==AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST)
		{
			m_timeOut = System.currentTimeMillis();
			goToLayout(R.layout.ab_layout_please_wait_purchase_progress);
		}

		mState = state;
		mSubstate = SUBSTATE_INIT;
		
		switch(mState)
    	{
    		case AB_STATE_INITIALIZE:
    			m_timeOut = System.currentTimeMillis();
    			goToLayout(R.layout.ab_layout_please_wait_purchase_progress);
    		break;
    		
    		case AB_STATE_GAME_PURCHASED:
    			goToLayout(R.layout.ab_layout_thanks_for_the_purchase);
    		break;
    		case AB_STATE_READ_GAME_CODE:
    			goToLayout(R.layout.ab_layout_enter_gamecode);
    		break;
    	}
    }
	
	public String getTimeStampRequest(String URL) 
	{
		XPlayer mXPlayer = new XPlayer(new Device(mServerInfo));
		mXPlayer.setUserCreds("","");//InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		mXPlayer.sendTimeStampPurchaseRequest(URL);
		String timeStampResponseString = System.currentTimeMillis()+"";
		while (!mXPlayer.handleTimeStampPurchaseRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
		{
			DBG(TAG,"TimeStamp request failed");
			timeStampResponseString = null;//System.currentTimeMillis()+"";
		}
		else
		{
			DBG(TAG,"TimeStamp request success = "+XPlayer.getLastErrorCodeString());
			timeStampResponseString = XPlayer.getLastErrorCodeString();
		}
		return timeStampResponseString; 
	}

	private void ShowIncorrectFormData()
	{              
		if(!mUserInfo.isValidCardHolder() && IsVisible(R.id.lblName))
			setTextViewNewText( R.id.lblName, R.string.AB_CC_NAME, Color.RED);
		if(!mUserInfo.isValidCardNumber() && IsVisible(R.id.lblCardNumber))
			setTextViewNewText( R.id.lblCardNumber, R.string.AB_CC_CARD_NUMBER, Color.RED);
		if(!mUserInfo.isValidExpirationDate() && IsVisible(R.id.lblExpiration))
		{
			setTextViewNewText( R.id.lblExpiration, R.string.AB_CC_EXPIRATION_DATE, Color.RED);
			setTextViewNewText( R.id.lblExpirationHelp, R.string.AB_CC_EXPIRATION_DATE_FORMAT, Color.RED);
		}
		if(!mUserInfo.isValidSecurityCode() && IsVisible(R.id.lblSecureCode))
			setTextViewNewText( R.id.lblSecureCode, R.string.AB_CC_SECURE_CODE, Color.RED);
		if(!mUserInfo.isValidEmail() && IsVisible(R.id.lblEmail))
			setTextViewNewText( R.id.lblEmail, R.string.AB_CC_EMAIL, Color.RED);
		if(!mUserInfo.isValidPassword() && IsVisible(R.id.lblPassword))
			setTextViewNewText( R.id.lblPassword, R.string.AB_CC_PASSWORD, Color.RED);
	}
	private void ResetIncorrectDataLabel()
	{
		if(IsVisible(R.id.lblName))
			setTextViewNewText( R.id.lblName, R.string.AB_CC_NAME, Color.BLACK);
		if(IsVisible(R.id.lblCardNumber))
			setTextViewNewText( R.id.lblCardNumber, R.string.AB_CC_CARD_NUMBER, Color.BLACK);
		if(IsVisible(R.id.lblExpiration))
		{
			setTextViewNewText( R.id.lblExpiration, R.string.AB_CC_EXPIRATION_DATE, Color.BLACK);
			setTextViewNewText( R.id.lblExpirationHelp, R.string.AB_CC_EXPIRATION_DATE_FORMAT, Color.BLACK);
		}
		if(IsVisible(R.id.lblSecureCode))
			setTextViewNewText( R.id.lblSecureCode, R.string.AB_CC_SECURE_CODE, Color.BLACK);
		if(IsVisible(R.id.lblEmail))
			setTextViewNewText( R.id.lblEmail, R.string.AB_CC_EMAIL, Color.BLACK);
		if(IsVisible(R.id.lblPassword))
			setTextViewNewText( R.id.lblPassword, R.string.AB_CC_PASSWORD, Color.BLACK);
	}
	
	
	public OnClickListener btnOnClickListener = new OnClickListener()
    {
        public void onClick(View v)
        {

			int buttonId;

            try
            {
                Button buttonPressed = (Button) v;
                buttonId = buttonPressed.getId();
            }
            catch (Exception e)
            {
                ImageButton buttonPressed = (ImageButton) v;
                buttonId = buttonPressed.getId();
            }

            switch (buttonId)
            {
            	/*case R.id.bt_ly_enter_gamecode_ok:
            	{
            		EditText reader = (EditText)findViewById(R.id.etGameCode);
        			if (reader != null)
        				Device.demoCode=reader.getText().toString();
        			setState(AB_STATE_INITIALIZE);
            	}            	
            	break;*/
            	
            	case R.id.bt_ly_login_wrong_email_password_buy_now:
            		SaveFormData(mCurrentLayout);
            		ResetIncorrectDataLabel();
            		if (mUserInfo.isValidEmail() && mUserInfo.isValidPassword())
            			setState(AB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST);
            		else
            			ShowIncorrectFormData();
            	break;
            	
            	case R.id.bt_ly_pay_wo_account_buy_now:
                	m_bIsSingleBill = true;
            		SaveFormData(mCurrentLayout);
            		ResetIncorrectDataLabel();
            		
            		if (mUserInfo.isValidEmail() && mUserInfo.isValidCreditCard())
                		setState(AB_STATE_PAY_WO_ACCOUNT_REQUEST);
                	else
                		ShowIncorrectFormData();	
                		
            	break;
            	
            	case R.id.bt_ly_login_buy_now:
                	SaveFormData(mCurrentLayout);
                	ResetIncorrectDataLabel();
                	if (mUserInfo.isValidEmail() && mUserInfo.isValidPassword())
                		setState(AB_STATE_CC_LOGIN_BUY_NOW_REQUEST);
                	else
                		ShowIncorrectFormData();
            	break;
            	
            	case R.id.bt_ly_get_full_version_question_get_now:
            		boolean value;
                	try
                	{
                		value = mDevice.getServerInfo().getBillingType().equals("WAP");
                	}catch(Exception ex)
                	{
                		value = false;
                	}
            		if(value)
    				{
    					setState(AB_STATE_PREPRARE_WAP_DATA_CONNECTION);
    				}
    				else
    				{
    					setState(AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST);
    					mbBillingMRC = false;
    					//ERR("ANDROID BILLING","NOT WAP!!! :/ !");
    				}
                break;
            	
            	case R.id.bt_ly_get_full_version_question_mrc_suscribe:
            		goToLayout(R.layout.ab_layout_get_full_version_mrc_confirmation);
            	break;
            	
            	case R.id.bt_ly_get_full_version_mrc_confirmation_pay:
            		setState(AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST);
            	break;
                
            	case R.id.bt_ly_enter_unlock_code_failed_retry:
            		#if USE_VOUCHERS
            		setState(AB_STATE_ENTER_UNLOCK_CODE);
					#else
            		setState(AB_STATE_GET_FULL_VERSION_QUESTION);
					#endif
            	break;
            	
            	case R.id.bt_ly_get_full_version_question_ticketcode:
            		setState(AB_STATE_ENTER_UNLOCK_CODE);
            	break;
            	
            	case R.id.bt_ly_wap_billing_next:
            		//trickDebugScreen=true;
            		setState(AB_STATE_VERIFYING_PURCHASE);
            	break;
            		
                
                //Terms and Conditions Buttons
                case R.id.bt_ly_create_new_account_tcs:
                case R.id.bt_ly_create_new_account_wrong_data_tcs:
                case R.id.bt_ly_credit_card_expired_tcs:
                case R.id.bt_ly_get_full_version_question_tcs:
                case R.id.bt_ly_login_tcs:
                case R.id.bt_ly_login_wrong_email_password_tcs:
                case R.id.bt_ly_pay_wo_account_tcs:
                case R.id.bt_ly_forgot_password_tcs:
                	goToLayout(R.layout.ab_layout_tcs);
                break;
                case R.id.bt_ly_tcs_back:
                	goToLayout(mPrevLayout);
                break;

                case R.id.bt_ly_cc_transaction_failed_no:
                case R.id.bt_ly_transaction_failed_ccard:
                case R.id.bt_ly_enter_unlock_code_failed_ccard:
                case R.id.bt_ly_create_new_account_email_exists_create_account:
                	setState(AB_STATE_CC_CREATE_ACCOUNT);
				//mPrevState = -1;
                break;
                case R.id.bt_ly_transaction_failed_ccard_wap_other:
					mb_waitingActivationWord = true;
					mb_isNextActivated = false;
					mServerInfo.getUnlockProfile().setBilling_type("WO");
					setState(AB_STATE_PREPARE_WAP_OTHER_BILLING);
				break;
                //The Play Buttons :P
                case R.id.bt_ly_cc_thanks_for_the_purchase_play_game:
                case R.id.bt_ly_thanks_for_the_purchase_play:
                //Exit (X) Buttons                	
                case R.id.bt_ly_get_full_version_question_cancel:
                case R.id.bt_ly_get_full_version_question_exit:
                case R.id.bt_ly_transaction_failed_exit:
                case R.id.bt_ly_thanks_for_the_purchase_exit:
                case R.id.bt_ly_cc_thanks_for_the_purchase_exit:
                case R.id.bt_ly_create_new_account_exit:
                case R.id.bt_ly_create_new_account_email_exists_exit:
                case R.id.bt_ly_create_new_account_wrong_data_exit:
                case R.id.bt_ly_credit_card_expired_exit:
                case R.id.bt_ly_enter_unlock_code_exit:
                case R.id.bt_ly_enter_unlock_code_failed_exit:
                case R.id.bt_ly_enter_unlock_code_success_play:
                case R.id.bt_ly_enter_unlock_code_success_exit:
                case R.id.bt_ly_login_exit:
                case R.id.bt_ly_login_wrong_email_password_exit:
                case R.id.bt_ly_pay_wo_account_exit:
                case R.id.bt_ly_tcs_exit:
                case R.id.bt_ly_forgot_password_exit:
                case R.id.bt_ly_no_data_connection_detected_exit:
                case R.id.bt_ly_no_data_connection_detected_cancel:
                case R.id.bt_ly_wap_billing_exit:
				case R.id.bt_ly_no_sim_detected_exit: //USE_SIM_ERROR_POPUP

                	setState(AB_STATE_FINALIZE);
                break;
#if USE_SIM_ERROR_POPUP
                case R.id.bt_ly_no_sim_detected_exit_wap_site:
						String l_url = "http://ingameads.gameloft.com/redir/?";
						l_url = l_url + "from=" + Device.getDemoCode();
						l_url = l_url + "&game=" + GGC_GAME_CODE;
						l_url = l_url + "&op=" + SHOP_OPERATOR_TARGET;
						l_url = l_url + "&game_type=DM&emb=1";
						Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(l_url));
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//should fix 5191259
					startActivity(i);
					setState(AB_STATE_FINALIZE);
				break;
#endif //USE_SIM_ERROR_POPUP
                case R.id.bt_ly_mrc_transaction_failed_full:
                    mServerInfo.searchForAditionalProfile("HTTP");
                	mbBillingMRC = false;

                case R.id.bt_ly_transaction_failed_retry:
                	boolean mvalue;
                	try
                	{
                		mvalue = mDevice.getServerInfo().getBillingType().equals("WAP");
                	}catch(Exception ex)
                	{
                		mvalue = false;
                	}
                	
                	if(mvalue)
                	{
    	            	mb_waitingActivationWord = true;
                		mb_isNextActivated = false;
                		setState(AB_STATE_WAP_BILLING);
                	}
                	else
                	{
                		setState(AB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST);
                	}
                break;
                
                //Retry Purchase Button
                case R.id.bt_ly_cc_transaction_failed_yes:
                	setState(mPrevState);
                break;
                
                //Back Buttons for all states
                case R.id.bt_ly_create_new_account_email_exists_back:
                case R.id.bt_ly_credit_card_expired_back:
                case R.id.bt_ly_login_back:
                case R.id.bt_ly_login_wrong_email_password_back:
                case R.id.bt_ly_pay_wo_account_back:
                case R.id.bt_ly_forgot_password_back:
                	handleBack(buttonId);
                break;

                //Create new Account Buttons
                case R.id.bt_ly_create_new_account_buy_now:
                case R.id.bt_ly_create_new_account_wrong_data_buy_now:
                	SaveFormData(mCurrentLayout);
                	ResetIncorrectDataLabel();
                	if (mUserInfo.isValidFormInfo())
                		setState(AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST);
                	else
                		ShowIncorrectFormData();
                	break;
                
                case R.id.bt_ly_credit_card_expired_buy_now:
                	SaveFormData(mCurrentLayout);
                	ResetIncorrectDataLabel();
                	if(m_bIsSingleBill)
                	{
                		if (mUserInfo.isValidEmail() && mUserInfo.isValidCreditCard())
                			setState(AB_STATE_PAY_WO_ACCOUNT_REQUEST);
                		else
                			ShowIncorrectFormData();
                	}
                	else
                	{
                		if (mUserInfo.isValidFormInfo())
                			setState(AB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST);
                		else
                			ShowIncorrectFormData();
                	}
                	break;
                	
                	
                case R.id.bt_ly_create_new_account_login:
                case R.id.bt_ly_create_new_account_email_exists_login:	
                	SaveFormData(mCurrentLayout);
                	setState(AB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
                break;
                
                case R.id.bt_ly_create_new_account_pay_wo_account:
                	setState(AB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT);
                break;
                case R.id.bt_ly_login_forgot_password:
                case R.id.bt_ly_login_wrong_email_password_forgot_password:
                	SaveFormData(mCurrentLayout);
                	goToLayout(R.layout.ab_layout_forgot_password);
                break;
                case R.id.bt_ly_forgot_password_ok:
                	SaveFormData(mCurrentLayout);
                	if (mUserInfo.isValidEmail())
                	{	
	                	setProgressBarVisibility(R.id.ProgressBarFP, ProgressBar.VISIBLE);
	                	setButtonVisibility(R.id.bt_ly_forgot_password_ok, Button.INVISIBLE);
	                	setButtonVisibility(R.id.bt_ly_forgot_password_back, Button.INVISIBLE);
	                	setButtonVisibility(R.id.bt_ly_forgot_password_tcs, Button.INVISIBLE);
	                	setButtonVisibility(R.id.bt_ly_forgot_password_exit, Button.INVISIBLE);
	                	setState(AB_STATE_CC_FORGOT_PASSWORD_REQUEST);
                	}
				break;
                case R.id.bt_ly_no_data_connection_detected_retry:
				
				    try
                	{
                		mvalue = mDevice.getServerInfo().getBillingType().equals("WO");
                	}catch(Exception ex)
                	{
                		mvalue = false;
                	}
					if(mvalue)
    				{
    					setState(AB_STATE_PREPARE_WAP_OTHER_BILLING);
    				}
                	else
					{
						try
						{
							mvalue = mDevice.getServerInfo().getBillingType().equals("WAP");
						}catch(Exception ex)
						{
							mvalue = false;
						}
						if(mvalue)
						{
							setState(AB_STATE_PREPRARE_WAP_DATA_CONNECTION);
						}
						else
						{
							setState(AB_STATE_INITIALIZE);
						}
					}
                break;
                
                case R.id.bt_ly_enter_unlock_code_get_it_now:
                	SaveFormData(mCurrentLayout);
               		setState(AB_STATE_ENTER_UNLOCK_CODE_PROGRESS);
                break;
                
                default:                	
                    break;
            }
        }

    };    
	 

	
	NetworkInfo mNetInfo = null;
	
#if USE_HEP_EXT_IGPINFO	
	public static boolean isRegisterSMSReceiver = false;	
	SmsReceiver UnlockReceiver = new SmsReceiver();
#endif //USE_HEP_EXT_IGPINFO	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		DBG(TAG,"AndroidBilling onCreate");
		
		super.onCreate(savedInstanceState);
		
	
		mStatus = STATUS_NO_ERROR;
#if USE_HEP_EXT_IGPINFO		
		gIsRunning = true;
		if(APP_PACKAGE.CLASS_NAME.m_sInstance == null) 		
		{
			Intent intent = new Intent(AndroidBilling.this, APP_PACKAGE.CLASS_NAME.class);
			startActivity(intent);
			gIsRunning = false;
			return;
		}
		if(isRegisterSMSReceiver == false)
		{
			IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
			registerReceiver(UnlockReceiver,intentFilter);
			isRegisterSMSReceiver = true;
		}
//		LManager.PREFERENCES_NAME                    	= IGPInfo.getIGPInfo()[2] + "BInfo";	
#endif //USE_HEP_EXT_IGPINFO
		mPurchaseDialog = new AlertDialog.Builder(this);
		mPurchaseDialog.setMessage(getString(R.string.AB_CONTINUE))
		.setCancelable(false)
		.setPositiveButton(getString(R.string.SKB_CONTINUE), new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int id) {
		// Action for 'Yes' Button
			//trickDebugScreen=true;
    		setState(AB_STATE_VERIFYING_PURCHASE);
		}
		}
		);
		
		
		//Common Dialog Box
		mDialog = new AlertDialog.Builder(this);

    }

    private void InitDeviceInfo()
    {
		mServerInfo = new ServerInfo();

		if (!mServerInfo.getUpdateProfileInfo())
		{
			mServerInfo.getOfflineProfilesInfo(this);
			//mServerInfo.getUnlockProfile().setBilling_type("NOTPROFILE");
			mServerInfo.getUnlockProfile().setBilling_type("SMS");
		}
		
		mDevice = new Device(mServerInfo);
		
		mGamePrice = mServerInfo.getGamePriceFmt();		
		mTCSText = mServerInfo.getTNCString();
		mGameName = getString(R.string.app_name);
		DBG(TAG,"***Reseting TimeOut for HTTP Validation***");
		m_timeOut = System.currentTimeMillis();
    }

	@Override
	protected void onStart() {
		DBG(TAG,"onStart");
		DBG(TAG,"mAndroidBStarted: " + mAndroidBStarted);
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
		if (!mAndroidBStarted) {
			mAndroidBStarted = true;
			mAssetManager = getAssets();
			new Thread(this).start();
		}
	}
	@Override
	protected void onDestroy() {
		DBG(TAG,"onDestroy");		
// #if !USE_HEP_EXT_IGPINFO
// 		SUtils.release();
// #endif //USE_HEP_EXT_IGPINFO
		super.onDestroy();
#if USE_HEP_EXT_IGPINFO		
		gIsRunning = false;
		// unregister sms receiver
		if(isRegisterSMSReceiver == true)
		{
			unregisterReceiver(UnlockReceiver);
			isRegisterSMSReceiver = false;
		}
#endif //USE_HEP_EXT_IGPINFO
	}
	
	@Override
	protected void onPause() {
		DBG(TAG,"onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		DBG(TAG,"onResume");
		super.onResume();
	}

	@Override
    protected void onStop() {
		DBG(TAG,"onStop");
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
    }

	@Override
	protected void onRestart()
	{
		DBG(TAG,"onRestart");
		super.onRestart();
		
		if( (mCurrentLayout == R.layout.ab_layout_enter_unlock_code)
		  ||(mCurrentLayout == R.layout.ab_layout_get_full_version_question)
		  ||(mCurrentLayout == R.layout.ab_layout_get_full_version_mrc_question)
		  ||(mCurrentLayout == R.layout.ab_layout_no_data_connection_detected)
		  ||(mCurrentLayout == R.layout.ab_layout_no_profile_detected)
		  ||(mCurrentLayout == R.layout.ab_layout_no_sim_detected)
		  ||(mCurrentLayout == R.layout.ab_layout_please_wait_purchase_progress)
		  ||(mCurrentLayout == R.layout.ab_layout_wap_other_preparing)
		  ||(mCurrentLayout == R.layout.ab_layout_thanks_for_the_purchase)
		  ||(mCurrentLayout == R.layout.ab_layout_thanks_for_the_purchase_v3)
		  ||(mCurrentLayout == R.layout.ab_layout_mrc_transaction_failed)
		  ||(mCurrentLayout == R.layout.ab_layout_transaction_failed))
		  {
			DBG(TAG,"RESTARTING LAYOUT: "+GetLayoutName(mCurrentLayout));
			goToLayout(mCurrentLayout);
		  }
	}
	
	public boolean mhasFocus = false;
	public void onWindowFocusChanged(boolean hasFocus)
	{
		mhasFocus = hasFocus;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	mCurrentOrientation = Configuration.ORIENTATION_LANDSCAPE;
	        DBG(TAG,"onConfigurationChanged Landscape "+ newConfig.orientation);
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	mCurrentOrientation = Configuration.ORIENTATION_PORTRAIT;
	    	DBG(TAG,"onConfigurationChanged portrait"+ newConfig.orientation);
	    }
	    else 
	    {
	    	mCurrentOrientation = newConfig.orientation;
	    	DBG(TAG,"onConfigurationChanged user"+ newConfig.orientation);
	    }
	    if (mSavedLayoutToDisplay != 0)
	    	goToLayout(mSavedLayoutToDisplay);
	}
	
	protected boolean canGoBack()
	{
		if( (mPrevState!= AB_STATE_NO_DATA_CONNECTION_DETECTED)
		&& 	(mState !=AB_STATE_NO_DATA_CONNECTION_DETECTED)
		&& 	(mState !=AB_STATE_NO_PROFILE_DETECTED)
		&& 	(mState !=AB_STATE_NO_SIMCARD_DETECTED)
		&& 	(mState !=AB_STATE_PB_TRANSACTION_SUCCESS)
		&& 	(mState !=AB_STATE_CC_TRANSACTION_SUCCESS)
		&& 	(mState !=AB_STATE_INITIALIZE)
		&& 	(mState !=AB_STATE_GAME_PURCHASED)
		&& 	(mState !=AB_STATE_CC_CREATE_ACCOUNT)
		|| 	((mCurrentLayout == R.layout.ab_layout_tcs)))
		{
			return (true);
		}
		else
		{
			return (false);
		}
	}
	

    
    public String GetLayoutName(int x)
	{
		switch (x) {
		
		case R.layout.ab_layout_create_new_account_portrait:
			return "ab_layout_create_new_account_portrait";
		
		case R.layout.ab_layout_get_full_version_mrc_question:
			return"ab_layout_get_full_version_mrc_question";

		case R.layout.ab_layout_get_full_version_mrc_confirmation:
			return"ab_layout_get_full_version_mrc_confirmation";
			
		case R.layout.ab_layout_get_full_version_question:
			return "ab_layout_get_full_version_question";
		
		case R.layout.ab_layout_please_wait_purchase_progress:
			return "ab_layout_please_wait_purchase_progress";

		case R.layout.ab_layout_wap_other_preparing:
			return "ab_layout_wap_other_preparing";
		
		case R.layout.ab_layout_thanks_for_the_purchase:
			return "ab_layout_thanks_for_the_purchase";
		
		case R.layout.ab_layout_thanks_for_the_purchase_v3:
			return "ab_layout_thanks_for_the_purchase_v3";
			
		case R.layout.ab_layout_mrc_transaction_failed:
			return "ab_layout_mrc_transaction_failed";
			
		case R.layout.ab_layout_transaction_failed:
			return "ab_layout_transaction_failed";
		
		case R.layout.ab_layout_cc_please_wait_purchase_progress:
			return "ab_layout_cc_please_wait_purchase_progress";
			
		case R.layout.ab_layout_cc_thanks_for_the_purchase:
			return "ab_layout_cc_thanks_for_the_purchase";
			
		case R.layout.ab_layout_cc_transaction_failed:
			return "ab_layout_cc_transaction_failed";
			
		case R.layout.ab_layout_create_new_account_email_exists:
			return "ab_layout_create_new_account_email_exists";
			
		case R.layout.ab_layout_create_new_account_wrong_data:
			return "ab_layout_create_new_account_wrong_data";
			
		case R.layout.ab_layout_credit_card_expired:
			return "ab_layout_credit_card_expired";
			
		case R.layout.ab_layout_login:
			return "ab_layout_login";
			
		case R.layout.ab_layout_login_wrong_email_password:
			return "ab_layout_login_wrong_email_password";
		
		case R.layout.ab_layout_pay_wo_account:
			return "ab_layout_pay_wo_account";
			
		case R.layout.ab_layout_tcs:
			return "ab_layout_tcs";
			
		case R.layout.ab_layout_enter_unlock_code:
			return "ab_layout_enter_unlock_code";
			
		case R.layout.ab_layout_forgot_password:
			return "ab_layout_forgot_password";
			
		case R.layout.ab_layout_no_data_connection_detected:
			return "ab_layout_no_data_connection_detected";
			
		case R.layout.ab_layout_enter_unlock_code_success:
			return "ab_layout_enter_unlock_code_success";
		case R.layout.ab_layout_enter_unlock_code_failed:
			return "ab_layout_enter_unlock_code_failed";
		case R.layout.ab_layout_no_profile_detected:
			return "ab_layout_no_profile_detected";
		case R.layout.ab_layout_no_sim_detected:
			return "ab_layout_no_sim_detected";
			
		case R.layout.main:
			return "main";
		}
		return "Unknown Layout(" + x + ")";
	}
    private int mCurrentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    private int mSavedLayoutToDisplay = 0;
    private int mCurrentLayout = 0;
    private int mPrevLayout = 0;
    boolean trickDebugScreen =false;
    public void goToLayout(final int layoutId) 
    {
    	DBG(TAG,"Billing: goToLayout("+GetLayoutName(layoutId)+")");
    	setButtonList(new ArrayList<Button>());
        getButtonList().clear();
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{
	    		try 
	    		{       
	    			mSavedLayoutToDisplay = 0;
	    			if (
	    				(layoutId == R.layout.ab_layout_create_new_account_portrait && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	    			  ||(layoutId == R.layout.ab_layout_create_new_account_portrait && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_USER)
	    				)
	    			{
	    				mSavedLayoutToDisplay = layoutId; 
	    				Thread.sleep(10);
	    				return;
	    			}
	    			
	    			else if(
		    				(layoutId == R.layout.ab_layout_enter_unlock_code && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_get_full_version_question && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_get_full_version_mrc_question && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_no_data_connection_detected && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_no_profile_detected && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
						  ||(layoutId == R.layout.ab_layout_no_sim_detected && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_please_wait_purchase_progress && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_wap_other_preparing && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_thanks_for_the_purchase && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_thanks_for_the_purchase_v3 && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_mrc_transaction_failed && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			  ||(layoutId == R.layout.ab_layout_transaction_failed && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		    			)
		    			{
		    				mSavedLayoutToDisplay = layoutId; 
		    				Thread.sleep(10);
		    				return;
		    			}
					setContentView(layoutId);
					if (mCurrentLayout != R.layout.ab_layout_tcs)
						mPrevLayout = mCurrentLayout;
					mCurrentLayout = layoutId;
	    			
	    			TextView lbl;
	    			EditText text;
	    			Button ccbutton;
	    			
	    			if (mCurrentOrientation== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	    				DBG(TAG,"Changing Layout on landscape mode!!!!!");
	    			
	    			switch (layoutId)
	    	        {
	    				case R.layout.ab_layout_enter_gamecode:
	    				{
	    					getButtonList().add((Button) findViewById(R.id.bt_ly_enter_gamecode_ok));
	    				}
	    				break;
	    				
	    	        	case R.layout.ab_layout_please_wait_purchase_progress:
	    	        	{
	    	        		if(mState == AB_STATE_INITIALIZE)
	    	        		{
	    	        			lbl = (TextView) findViewById(R.id.lblMessagePleaseWait);
	    	        			lbl.setText("");
	    	        		}
	    	        		if(mState == AB_STATE_ENTER_UNLOCK_CODE)
	    	        		{
	    	        			lbl = (TextView) findViewById(R.id.lblMessagePleaseWait);
	    	        			lbl.setText(getString(R.string.AB_ENTER_UNLOCK_CODE_PROGRESS));
	    	        		}
	    	        		boolean value;
	                    	try
	                    	{
	                    		value = mDevice.getServerInfo().getBillingType().equals("WAP");
	                    	}catch(Exception ex)
	                    	{
	                    		value = false;
	                    	}
	    	        		if (value || trickDebugScreen)
	    	        		{
	    	        			lbl = (TextView) findViewById(R.id.lblMessagePleaseWait);
	    	        			lbl.setText(getString(R.string.AB_WAP_BILLING_VERIFYING));
	    	        		}
						#if USE_BILLING_FOR_CHINA
							if(mState == AB_STATE_PLEASE_WAIT_PURCHASE_IPX_CONT)
							{
								lbl = (TextView) findViewById(R.id.lblMessagePleaseWait);
	    	        			String info = getString(R.string.AB_PURCHASE_IN_PROGRESS) + "\n" + getString(R.string.AB_PURCHASE_IPX_CONT_IN_PROGRESS);
								info = info.replace("{COUNT}", Integer.toString(mServerInfo.getCurrentProfileSelected().getIPXSentCount()));
								info = info.replace("{TOTAL}", Integer.toString(mServerInfo.getCurrentProfileSelected().getIPXSMSCount()));
								lbl.setText(info);
							}
						#endif
	    	        	}
	    	        	break;
	    	        	
	    	        	case R.layout.ab_layout_get_full_version_mrc_question:
	    	        	{
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_mrc_suscribe));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_get_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_tcs));
    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.AB_GET_FULL_VERSION_QUESTION,"{GAME_NAME}",mGameName));
    	    				
    	    				lbl = (TextView) findViewById(R.id.lblDiscount);
    	    				lbl.setText("-"+mServerInfo.getMRCDiscount());

    	    				Button bt;
	    		    		
    	    				bt = (Button) findViewById(R.id.bt_ly_get_full_version_question_mrc_suscribe);
	    		    		bt.setText(getStringFormated(R.string.AB_MRC_SUSCRIBE,"{PRICE}",mServerInfo.getGamePriceFmt()));
	    		    		
	    		    		bt = (Button) findViewById(R.id.bt_ly_get_full_version_question_get_now);
	    		    		bt.setText(getStringFormated(R.string.AB_MRC_FULL_PAYMENT,"{PRICE}",mGamePrice));
	    	        	}
	    	        	break;
	    	        	
	    	        	case R.layout.ab_layout_get_full_version_mrc_confirmation:
	    	        	{
	    	        		mTCSText = mServerInfo.getTNCString();
	    	        		
	    	        		getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_mrc_confirmation_pay));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_cancel));
    	            	
	    	            	String textInfo = getStringFormated(R.string.AB_MRC_MESSAGE,"{GAME}",mGameName);
	    	            	textInfo = getStringFormated(textInfo,"{PRICE}",mServerInfo.getGamePriceFmt());
	    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo2);
    	    				lbl.setText(mServerInfo.getMRCDetails());

	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(textInfo);
    	    				
    	    				lbl = (TextView) findViewById(R.id.lblDiscount);
    	    				lbl.setText("-"+mServerInfo.getMRCDiscount());
	    	        	}
	    	        	break;
	    	        	
	    	        	case R.layout.ab_layout_get_full_version_question:
	    	            {
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_get_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_cancel));
    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.AB_GET_FULL_VERSION_QUESTION,"{GAME_NAME}",mGameName));
    	    				
    	    				lbl = (TextView) findViewById(R.id.lblMessageInfo2);
    	    				lbl.setText(Html.fromHtml(getStringFormated(R.string.AB_GET_FULL_VERSION_CHARGE_APPLY,"{PRICE}",mGamePrice)));
    	    				
    	    				Button bt;
	    		    		bt = (Button) findViewById(R.id.bt_ly_get_full_version_question_get_now);

    	    				if(mServerInfo.getBuyScreen()!=null)
    	    				{
    	    					lbl.setText(Html.fromHtml("<b>" + mServerInfo.getBuyScreen()+ "</b>"));
    	    				}
    	    				if(mServerInfo.suportDoubleOption())
    	    				{
    	    					lbl.setText(mServerInfo.getDoubleOptionString_2());
    	    		    		bt.setText(getString(R.string.SKB_GET_NOW));
    	    				}
	    	            }
	    	            break;
					#if USE_BILLING_FOR_CHINA
						case R.layout.ab_layout_get_full_version_china_question:
						{
							getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_get_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_cancel));
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.AB_GET_FULL_VERSION_QUESTION,"{GAME_NAME}",mGameName));
    	    				
							lbl = (TextView) findViewById(R.id.lblMessageInfo2);
							if(mServerInfo.getBillingType().equals("UMP"))								
								lbl.setText(Html.fromHtml(getStringFormated(R.string.AB_GET_FULL_VERSION_UMP_INFO,"{PRICE}",mGamePrice)));
							else if(mServerInfo.getBillingType().equals("IPX"))
							{
								String buyInfo = getString(R.string.AB_GET_FULL_VERSION_IPX_INFO);
								buyInfo = buyInfo.replace("{PRICE}", mGamePrice);
								buyInfo = buyInfo.replace("{PAID}", Integer.toString(mServerInfo.getCurrentProfileSelected().getIPXPaidMoney()) + mServerInfo.getCurrencySymbol());
								int sendSMS = mServerInfo.getCurrentProfileSelected().getIPXSMSCount() - mServerInfo.getCurrentProfileSelected().getIPXSentCount();
								buyInfo = buyInfo.replace("{COUNT}", Integer.toString(sendSMS));
								lbl.setText(Html.fromHtml(buyInfo));
							}
    	    				
    	    				Button bt;
	    		    		bt = (Button) findViewById(R.id.bt_ly_get_full_version_question_get_now);
						}
						break;
					#endif

	    	        	case R.layout.ab_layout_get_full_version_promo_question:
	    	            {
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_get_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_ticketcode));
    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.AB_GET_FULL_VERSION_QUESTION,"{GAME_NAME}",mGameName));
    	    				
    	    				lbl = (TextView) findViewById(R.id.lblMessageInfo2);
    	    				lbl.setText(Html.fromHtml(getStringFormated(R.string.AB_GET_FULL_VERSION_CHARGE_APPLY,"{PRICE}",mGamePrice)));

    	    				Button bt;
	    		    		bt = (Button) findViewById(R.id.bt_ly_get_full_version_question_get_now);

    	    				if(mServerInfo.getBuyScreen()!=null)
    	    				{
    	    					lbl.setText(Html.fromHtml(mServerInfo.getBuyScreen()));
    	    				}
    	    				if(mServerInfo.suportDoubleOption())
    	    				{
    	    					lbl.setText(mServerInfo.getDoubleOptionString_2());
    	    		    		bt.setText(getString(R.string.SKB_GET_NOW));
    	    				}
	    	            }
	    	            break;
						#if USE_VOUCHERS
						case R.layout.ab_layout_get_full_version_promo_curys_question:
	    	            {
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_exit));
	    	            	//getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_get_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_ticketcode));
    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.AB_GET_FULL_VERSION_QUESTION,"{GAME_NAME}",mGameName));
    	    				
    	    				lbl = (TextView) findViewById(R.id.lblMessageInfo2);
    	    				lbl.setText(Html.fromHtml(getStringFormated(R.string.AB_GET_FULL_VERSION_CHARGE_APPLY,"{PRICE}",mGamePrice)));

    	    				Button bt;
	    		    		bt = (Button) findViewById(R.id.bt_ly_get_full_version_question_get_now);

    	    				if(mServerInfo.getBuyScreen()!=null)
    	    				{
    	    					lbl.setText(Html.fromHtml(mServerInfo.getBuyScreen()));
    	    				}
    	    				if(mServerInfo.suportDoubleOption())
    	    				{
    	    					lbl.setText(mServerInfo.getDoubleOptionString_2());
    	    		    		bt.setText(getString(R.string.SKB_GET_NOW));
    	    				}
	    	            }
	    	            break;
						#endif
	    	        	case R.layout.ab_layout_thanks_for_the_purchase_v3:
	    	        	{
	    	            	DBG(TAG,"WE ARE ON THE THANKS FOR PURCHASE VERSION 3 (MRC) LAYOUT");
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_thanks_for_the_purchase_play));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_thanks_for_the_purchase_exit));
	    	            	
    	            		lbl = (TextView) findViewById(R.id.lblPurchaseInfo);
    	    				
    	            		if(mbBillingMRC)
    	    					lbl.setText(mServerInfo.getMRCBuyInfo());
    	    				else
    	    					lbl.setText(mServerInfo.getBuyInfo());
	    	        	}
	    	        	break;
	    	        	case R.layout.ab_layout_thanks_for_the_purchase:
	    	            {
	    	            	DBG(TAG,"WE ARE ON THE THANKS FOR PURCHASE LAYOUT");
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_thanks_for_the_purchase_play));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_thanks_for_the_purchase_exit));
	    	            	
	    	            	if(mState == AB_STATE_GAME_PURCHASED)
	    	            	{
	    	            		DBG(TAG,"Changing messange to the screen for purchased message");
	    	            		lbl = (TextView) findViewById(R.id.lblMessageInfo);
	    	    				lbl.setText(getString(R.string.AB_ALREADY_PURCHASED));
	    	            	}
	    	            }
	    	            break;
	    	            
	    	            case R.layout.ab_layout_mrc_transaction_failed:
	    	            {
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_retry));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_mrc_transaction_failed_full));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_exit));
	    	            	
    	    				Button bt;
	    		    		bt = (Button) findViewById(R.id.bt_ly_mrc_transaction_failed_full);
	    		    		bt.setText(getStringFormated(R.string.AB_MRC_SUSCRIBE_RETRY_FULL_PAYMENT,"{PRICE}",mGamePrice));
	    	            }	
	    	            break;
	    	            
	    	            case R.layout.ab_layout_transaction_failed:
	    	            {
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_retry));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_ccard));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_ccard_wap_other));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_exit));
							#if !USE_TNB_CC_PROFILE
	    	            		ccbutton = (Button) findViewById(R.id.bt_ly_transaction_failed_ccard);
	    	            		ccbutton.setVisibility(View.INVISIBLE);
							#else
							if(!mServerInfo.searchForAditionalProfile("CC"))
								{
								ccbutton = (Button) findViewById(R.id.bt_ly_transaction_failed_ccard);
								ccbutton.setVisibility(View.INVISIBLE);
								}
							if(mServerInfo.searchForAditionalProfile("WO"))
							{
								if(mServerInfo.getProfileListSize() == 1)
									((Button) findViewById(R.id.bt_ly_transaction_failed_retry)).setVisibility(View.GONE);
								((Button) findViewById(R.id.bt_ly_transaction_failed_ccard)).setVisibility(View.GONE);
								ccbutton = (Button) findViewById(R.id.bt_ly_transaction_failed_ccard_wap_other);
								ccbutton.setVisibility(View.VISIBLE);
							}
							#endif
	    	            }
	    	            break;
	    	            case R.layout.ab_layout_enter_unlock_code:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_get_it_now));
	    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.AB_ENTER_UNLOCK_CODE_ASK_CODE,"{GAME_NAME}",mGameName));
	    	            break;
	    	            
	    	            
	    	            case R.layout.ab_layout_wap_billing:
	    	            webView = (WebView) findViewById(R.id.webview);
	            		webView.getSettings().setJavaScriptEnabled(true);
	            		//webView.loadUrl("http://www.google.com");
	            		//ERR("ANDROID BILLING", "Billing URL = mDevice.getServerInfo().getURLbilling()= "+mDevice.getServerInfo().getURLbilling());
	            		webView.loadUrl(mDevice.getServerInfo().getURLbilling()+"?msisdn="+mDevice.getLineNumber()+"&imei="+mDevice.getIMEI()+
						#if HDIDFV_UPDATE
							"&hdidfv="+mDevice.getHDIDFV()+
						#endif
						"&from=UNLOCK_&igagame="+Device.getDemoCode());

	            		webView.setWebViewClient(new MyWebViewClient());
	            		webView.requestFocus(View.FOCUS_DOWN);
	            		
	            		Button bt;
	    	            getButtonList().add((Button) findViewById(R.id.bt_ly_wap_billing_exit));
	            		getButtonList().add((Button) findViewById(R.id.bt_ly_wap_billing_next));

	            		bt = (Button) findViewById(R.id.bt_ly_wap_billing_next);
	    		    	bt.setVisibility(bt.INVISIBLE);
	    		    	mb_waitingActivationWord = true;
	            		break;	    	            
						
						case R.layout.ab_layout_wap_other_billing:
						webView = (WebView) findViewById(R.id.webview);
	            		webView.getSettings().setJavaScriptEnabled(true);
	            		
						if(map != null && map.size() != 0 )
						{
							DBG(TAG,"Loading webview with headers");
							webView.loadUrl(mDevice.getServerInfo().getURLbilling()+"?game="+Device.getDemoCode()+"&msisdn="+mDevice.getLineNumber()+"&imei="+mDevice.getIMEI()+"&from=UNLOCK_&igagame="+Device.getDemoCode()+"&b=ppdwap|"+Device.getDemoCode()+"|"+mDevice.getServerInfo().getProfileId(), map);
							// webView.loadUrl("https://confirmation.gameloft.com/wap/wp/?game=LLHM&msisdn="+mDevice.getLineNumber()+"&imei="+mDevice.getIMEI()+"&from=UNLOCK_&igagame="+Device.getDemoCode()+"&b=ppdwap|"+Device.getDemoCode()+"|"+mDevice.getServerInfo().getProfileId(), map);
							// webView.loadUrl(mDevice.getServerInfo().getURLbilling()+"?msisdn="+mDevice.getLineNumber()+"&imei="+mDevice.getIMEI()+"&from=UNLOCK_&igagame="+Device.getDemoCode(), map);
						}
						else
							webView.loadUrl(mDevice.getServerInfo().getURLbilling()+"?msisdn="+mDevice.getLineNumber()+"&imei="+mDevice.getIMEI()+
							#if HDIDFV_UPDATE
							"&hdidfv="+mDevice.getHDIDFV()+
							#endif
							"&from=UNLOCK_&igagame="+Device.getDemoCode());

	            		webView.setWebViewClient(new MyWebViewClientWO(AndroidBilling.this));
	            		webView.requestFocus(View.FOCUS_DOWN);
	            		
	            		Button btNext;
	    	            getButtonList().add((Button) findViewById(R.id.bt_ly_wap_billing_exit));
	            		getButtonList().add((Button) findViewById(R.id.bt_ly_wap_billing_next));

	            		btNext = (Button) findViewById(R.id.bt_ly_wap_billing_next);
	    		    	btNext.setVisibility(btNext.INVISIBLE);
	            		break;
	    	            
	    	            case R.layout.ab_layout_enter_unlock_code_success:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_success_play));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_success_exit));
	    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.AB_ENTER_UNLOCK_CODE_SUCCESS,"{GAME_NAME}",mGameName));
	    	            break;
	    	            
	    	            case R.layout.ab_layout_enter_unlock_code_failed:
	    	            	//if(ADD ERROR HANDLER HERE)//ALEX HERE
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_failed_ccard));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_failed_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_failed_retry));
	    	            	//lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				//lbl.setText(R.string.AB_ENTER_UNLOCK_CODE_FAIL_2);
    	    				//Button bt;
	    		    		//bt = (Button) findViewById(R.id.bt_ly_enter_unlock_code_failed_retry);
   	    		    		//bt.setText(getString(R.string.SKB_RETRY));
	    	            break;
	    	            
	    	            case R.layout.ab_layout_enter_unlock_code_failed_curys:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_failed_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_enter_unlock_code_failed_retry));
	    	            break;
	    	            case R.layout.ab_layout_create_new_account_portrait:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_login));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_pay_wo_account));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_tcs));
	    	            	addDateValidation();
	    	            	lbl = (TextView) findViewById(R.id.lblDialogTop);
	    	            	String str = getStringFormated(R.string.AB_CC_PURCHASE_WITH_CCARD,"{GAME_NAME}",mGameName);
	    	            	str = getStringFormated(str,"{PRICE}",mGamePrice);
							CharSequence styledText = Html.fromHtml(str);
	    	            	lbl.setText(styledText);
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_create_new_account_wrong_data:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_wrong_data_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_wrong_data_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_wrong_data_tcs));
	    	            	addDateValidation();
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            	if (XPlayer.getLastErrorCode() == XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_EMAIL)
	    	            	{	
	    	            		lbl = (TextView) findViewById(R.id.lblEmail);
	    	            		lbl.setTextColor(Color.RED);
	    	            	}
	    	            	if (XPlayer.getLastErrorCode() == XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_PASSWORD)
	    	            	{	
	    	            		lbl = (TextView) findViewById(R.id.lblPassword);
	    	            		lbl.setTextColor(Color.RED);
	    	            	}
	    	            	if (XPlayer.getLastErrorCode() == XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_CARD_NUMBER ||
	    	            			XPlayer.getLastErrorCode() == XPlayer.CC_ERROR_USER_CREATED_INVALID_CARD_NUMBER)
	    	            	{	
	    	            		lbl = (TextView) findViewById(R.id.lblCardNumber);
	    	            		lbl.setTextColor(Color.RED);
	    	            	}
	    	            	if (XPlayer.getLastErrorCode() == XPlayer.CC_ERROR_USER_NOT_CREATED_CCV_MISSING_INVALID_EXP_DATE ||
	    	            			XPlayer.getLastErrorCode() == XPlayer.CC_ERROR_USER_CREATED_CCV_MISSING_INVALID_EXP_DATE)
	    	            	{	
	    	            		lbl = (TextView) findViewById(R.id.lblExpiration);
	    	            		lbl.setTextColor(Color.RED);
	    	            		lbl = (TextView) findViewById(R.id.lblSecureCode);
	    	            		lbl.setTextColor(Color.RED);
	    	            	}	
	    	            break;
	    	            case R.layout.ab_layout_create_new_account_email_exists:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_login));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_create_account));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_exit));
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_pay_wo_account:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_exit));
	    	            	addDateValidation();
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_login:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_forgot_password));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_exit));
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_login_wrong_email_password:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_forgot_password));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_exit));
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_credit_card_expired:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_exit));
	    	            	addDateValidation();
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_cc_thanks_for_the_purchase:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_cc_thanks_for_the_purchase_play_game));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_cc_thanks_for_the_purchase_exit));
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_cc_transaction_failed:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_cc_transaction_failed_yes));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_cc_transaction_failed_no));
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_cc_please_wait_purchase_progress:
	    	            	{
	    	            		lbl = (TextView) findViewById(R.id.lblAccountCreated);//testing
	    	            		lbl.setVisibility(View.INVISIBLE);
	    	            	}
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_forgot_password:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_ok));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_exit));
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.ab_layout_tcs:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_tcs_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_tcs_exit));
	    	            	lbl = (TextView) findViewById(R.id.tvTCSInfo);
	    	            	lbl.setText(mTCSText);
	    	            break;
	    	            case R.layout.ab_layout_no_data_connection_detected:
	    	            	boolean value;
	                    	try
	                    	{
	                    		value = mDevice.getServerInfo().getBillingType().equals("WAP");
	                    	}catch(Exception ex)
	                    	{
	                    		value = false;
	                    	}
	    	            	if(value)
	    	            	{
	    	            		lbl = (TextView) findViewById(R.id.lblMessageInfo2);
	    	            		lbl.setText(getString(R.string.AB_ACTIVATE_CONNECTION));
	    	            	}
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_cancel));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_retry));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_exit));
							value = false;
							try
	                    	{
	                    		value = mDevice.getServerInfo().getBillingType().equals("WO");
	                    	}catch(Exception ex)
	                    	{
	                    		value = false;
	                    	}
							if(!value && mServerInfo.searchForAditionalProfile("WO"))
							{
								getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_ccard_wap_other));
								((Button) findViewById(R.id.bt_ly_transaction_failed_ccard_wap_other)).setVisibility(View.VISIBLE);
								((Button) findViewById(R.id.bt_ly_no_data_connection_detected_cancel)).setVisibility(View.GONE);
							}
	    	            break;
#if USE_SIM_ERROR_POPUP
						case R.layout.ab_layout_no_sim_detected:
						getButtonList().add((Button) findViewById(R.id.bt_ly_no_sim_detected_exit_wap_site));
                        getButtonList().add((Button) findViewById(R.id.bt_ly_no_sim_detected_exit));

						if( SUtils.isAirplaneModeOn() && SUtils.hasConnectivity() == 1 ) //hq request: if the airplanemode is on, but there is wifi connectivity redirect to Gameloft Shop
						{
							lbl = (TextView) findViewById(R.id.lblMessageInfo2);
							lbl.setText(getString(R.string.AB_NO_PROCESS_POPUP));
						}
						break;
#endif //USE_SIM_ERROR_POPUP
	    	            case R.layout.ab_layout_no_profile_detected:	    	            	
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_exit));
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo2);
						#if USE_BILLING_FOR_CHINA
							if(mState == AB_STATE_NO_SIMCARD_DETECTED)
							{
								TelephonyManager mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
								switch (mDeviceInfo.getSimState())
								{
								case TelephonyManager.SIM_STATE_ABSENT:
									lbl.setText(getString(R.string.AB_NO_SIMCARD));
									break;
								case TelephonyManager.SIM_STATE_PUK_REQUIRED:
									lbl.setText(getString(R.string.AB_SIMCARD_PUK_UNLOCK));
									break;
								case TelephonyManager.SIM_STATE_PIN_REQUIRED:
									lbl.setText(getString(R.string.AB_SIMCARD_PIN_UNLOCK));
									break;
								case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
									lbl.setText(getString(R.string.AB_SIMCARD_NETWORK_PIN_UNLOCK));
									break;
								default:
									lbl.setText(getString(R.string.AB_SIMCARD_UNKNOWN));
									break;
								}
							}
							else
						#endif
							{	
								if (mServerInfo.getUnlockProfile().getNoProfileMsg() != null)
									lbl.setText(mServerInfo.getUnlockProfile().getNoProfileMsg(mDevice.getLocale()));
							}
						break;
	    	            default:
	    	            break;
	    	        }
	    	        LoadFormData();
	    	        // Add buttons listeners
	    	        try 
	    	        {
	    	            for (Button but : getButtonList())
	    	            {
	    	                but.setOnClickListener(btnOnClickListener);
	    	            }
	    	        } catch (Exception e) {
	    	            ERR(TAG,e.getMessage());
	    	        }
	    		} catch (Exception e) {
	    			ERR(TAG,e.getMessage());
	    		}
	    	}

			private void fillBottomMsgInfo() {
				TextView lbl;
				lbl = (TextView) findViewById(R.id.lblBottomMsg);
				lbl.setText(getString(R.string.AB_CC_GL_PEACE_OF_MIND)+ " "+
						getString(R.string.AB_CC_GL_PEACE_OF_MIND2)+ " "+
						getString(R.string.AB_CC_EMAIL_PRIVACY));
			}


			private void fillGameDetails() 
			{
				TextView lbl;
				lbl = (TextView) findViewById(R.id.lblGameName);
				lbl.setText(mGameName);
				lbl = (TextView) findViewById(R.id.lblGamePrice);
				lbl.setText(Html.fromHtml(getStringFormated(R.string.AB_CC_GAME_PRICE,"{PRICE}",mGamePrice)));
				//lbl.setText("<b>" + mGamePrice + "</b>");
			}
    	});
    }

	private void setWAPNextButtonVisibility()
    {
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{	
        		webView = (WebView) findViewById(R.id.webview);
        		
        		if(webView.findAll(MAGIC_WORD)!=0 && !mb_isNextActivated)
        		{
	            	mb_waitingActivationWord = false;
            		mb_isNextActivated = true;

	            	//Reset the current pointer selection
	            	webView.clearHistory();
	            	webView.clearCache(true);
	            	webView.setVisibility(WebView.INVISIBLE);
	            	
	            	/*Button bt;
	            	bt = (Button) findViewById(R.id.bt_ly_wap_billing_next);
	            	bt.setVisibility(0);*/
	            	
	        		AlertDialog alert = mPurchaseDialog.create();
	        		alert.setTitle(mGameName);
	        		alert.setIcon(R.drawable.icon);
	        		alert.show();
           		}
	    	}
    	});
    }

	private void setWAPNextButtonVisibilityWO()
    {
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{	
        		webView = (WebView) findViewById(R.id.webview);
        		String urlView = webView.getUrl();
        		if( (
					webView.findAll("PURCHASE_ID")!=0 
					|| ( urlView != null && (urlView.indexOf("result=successful&id=") > 0 || urlView.indexOf("&error=") > 0 || urlView.indexOf("?error=") > 0)))
					&& !mb_isNextActivated)
        		{
	            	mb_waitingActivationWord = false;
            		mb_isNextActivated = true;

	            	//Reset the current pointer selection
	            	webView.clearHistory();
	            	webView.clearCache(true);
	            	webView.setVisibility(WebView.INVISIBLE);
	            	
	            	/*Button bt;
	            	bt = (Button) findViewById(R.id.bt_ly_wap_billing_next);
	            	bt.setVisibility(0);*/
	            
				//cancel
					if(webView.findAll("PURCHASE_ID_E00108") !=0 || ( urlView != null && (urlView.indexOf("&error=E00108") > 0 || urlView.indexOf("?error=E00108") > 0)))
						setState(AB_STATE_FINALIZE);
					else
					{
						AlertDialog alert = mPurchaseDialog.create();
						alert.setTitle(mGameName);
						alert.setIcon(R.drawable.icon);
						alert.show();
					}
           		}
	    	}
    	});
    }

	

	/**
	 * Used to fill the form data from the info stored previously in mUserInfo
	 */
	private void LoadFormData()
	{	
		if (mUserInfo == null)
			return;
		EditText reader;
		
		reader = (EditText)findViewById(R.id.etEmail);     
		if (reader != null && mUserInfo.getEmail() != null)
			reader.setText(mUserInfo.getEmail());
		
		reader = (EditText)findViewById(R.id.etPassword);
		if (reader != null && mUserInfo.getPassword() != null)
			reader.setText(mUserInfo.getPassword());
		
		reader = (EditText)findViewById(R.id.etName);
		if (reader != null && mUserInfo.getCardHolder() != null)
			reader.setText(mUserInfo.getCardHolder());
		
		reader = (EditText)findViewById(R.id.etCardNumber1);
		if (reader != null && mUserInfo.getCardNumber1() != null)
			reader.setText(mUserInfo.getCardNumber1());
		
		reader = (EditText)findViewById(R.id.etCardNumber2);
		if (reader != null && mUserInfo.getCardNumber2() != null)
			reader.setText(mUserInfo.getCardNumber2());
		
		reader = (EditText)findViewById(R.id.etCardNumber3);
		if (reader != null && mUserInfo.getCardNumber3() != null)
			reader.setText(mUserInfo.getCardNumber3());
		
		reader = (EditText)findViewById(R.id.etCardNumber4);
		if (reader != null && mUserInfo.getCardNumber4() != null)
			reader.setText(mUserInfo.getCardNumber4());
		
		reader = (EditText)findViewById(R.id.etExpirationMonth);
		if (reader != null && mUserInfo.getExpirationDateMonth() != null)
			reader.setText(mUserInfo.getExpirationDateMonth());
		
		reader = (EditText)findViewById(R.id.etExpirationYear);
		if (reader != null && mUserInfo.getExpirationDateYear() != null)
			reader.setText(mUserInfo.getExpirationDateYear());
		
		reader = (EditText)findViewById(R.id.etSecureCode);
		if (reader != null && mUserInfo.getSecurityCode() != null)
			reader.setText(mUserInfo.getSecurityCode());
	}
	
	private void SaveFormData(int currentForm)
	{
		if (mUserInfo == null)
			mUserInfo = new UserInfo();
		EditText reader;
		DBG(TAG,"Saving data for: "+GetLayoutName(currentForm));
			reader = (EditText)findViewById(R.id.etEmail);
			if (reader != null)
				mUserInfo.setEmail(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etPassword);
			if (reader != null)
				mUserInfo.setPassword(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etName);
			if (reader != null)
				mUserInfo.setCardHolder(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etCardNumber1);
			if (reader != null)
				mUserInfo.setCardNumber1(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etCardNumber2);
			if (reader != null)
				mUserInfo.setCardNumber2(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etCardNumber3);
			if (reader != null)
				mUserInfo.setCardNumber3(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etCardNumber4);
			if (reader != null)
				mUserInfo.setCardNumber4(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etExpirationMonth);
			if (reader != null)
				mUserInfo.setExpirationDateMonth(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etExpirationYear);
			if (reader != null)
				mUserInfo.setExpirationDateYear(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etSecureCode);
			if (reader != null)
				mUserInfo.setSecurityCode(reader.getText().toString());
			
			reader = (EditText)findViewById(R.id.etUnlockCode);
			if (reader != null)
				mPromoCode=reader.getText().toString();
		
	}


	
	
	
	public void updateValidationResult(boolean success, int message)
	{
		if(success)
		{
			setState(STATE_VALIDATE_SUCCESS);
			SUtils.getLManager().saveUnlockGame(1);
		}else
		{
			///check if fail for unreachable server
			if(message == R.string.AB_TRANSACTION_FAILED)
			{
				setError(message, STATE_VALIDATING_LICENSE);
				mStatus = message;
			} else
			{
				setState(STATE_SELECT_PAY_MODE);
			}
		}
		
		if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	
	public void updateBillingResult(boolean success, int message)
	{
		DBG(TAG,"updateBillingResult()");
		
		DBG(TAG,"success= "+success+" message"+this.getString(message));
		
		if(success)
		{
			if(m_bWasWifiEnabled)
			{
				mDevice.EnableWifi();
				m_bWasWifiEnabled = false;
				while (mDevice.IsWifiEnabling())
				{
					DBG(TAG,"ENABLING Wi-Fi!!!");
				}
			}
			
			setState(AB_STATE_PB_TRANSACTION_SUCCESS);
		    
			if(mDevice.getServerInfo().getBillingType().equals("SMS"))
			{
				DBG(TAG,"SMS SUCCESS TRACKING STARTED");
				SUtils.getLManager().TrackingPurchaseSuccess(1);
			}
			else
			{
				DBG(TAG,"HTTP SUCCESS TRACKING STARTED");
				SUtils.getLManager().TrackingPurchaseSuccess(0);
			}
			
			
			
		}else
		{
			if(m_bWasWifiEnabled)
			{
				mDevice.EnableWifi();
				m_bWasWifiEnabled = false;
				while (mDevice.IsWifiEnabling())
				{
					//DBG(TAG,"ENABLING Wi-Fi!!!");
				}
			}
			setError(message, STATE_PAY_ON_BILL);
			setState(AB_STATE_PB_TRANSACTION_FAILED);
		    
			
			
			if(mDevice.getServerInfo().getBillingType().equals("SMS"))
			{
				DBG(TAG,"SMS ERROR TRACKING STARTED");
				SUtils.getLManager().TrackingPurchaseFailed(1);
			}
			else
			{
				DBG(TAG,"HTTP ERROR TRACKING STARTED");
				SUtils.getLManager().TrackingPurchaseFailed(0);
			}
			
			mStatus = message;
		}
		
		//hideLoading();
		
		if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	/**
	 * This method is called from Model with a response from server, after such a
	 * request has been initiated with <code>sendLoginRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCLogin(boolean success, int message)
	{
		DBG(TAG,"updateCCLogin()");
		
		if(success)
		{	
			switch(message)
			{
				case XPlayer.CC_LOGIN_SUCCESS_USER_EXIST_WITH_CREDIT_CARD:
					DBG(TAG,"CC_LOGIN_SUCCESS_USER_EXIST_WITH_CREDIT_CARD");
					setState(AB_STATE_CC_USERBILL_BUY_NOW_REQUEST);
				break;
				
				case XPlayer.CC_LOGIN_SUCCESS_USER_EXIST_BUT_NO_CREDIT_CARD:
					DBG(TAG,"CC_LOGIN_SUCCESS_USER_EXIST_BUT_NO_CREDIT_CARD");
					setState(AB_STATE_CREDIT_CARD_EXPIRED);
				break;
				
				default:
					DBG(TAG,"ERROR RESULTANT IS :"+message);
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
			}
		}
		else
		{
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_LOGIN_INVALID_USERNAME_OR_PASSWORD:
					DBG(TAG,"CC_ERROR_LOGIN_INVALID_USERNAME_OR_PASSWORD");
					setState(AB_STATE_LOGIN_WRONG_EMAIL_PASSWORD);
				break;
				
				case XPlayer.CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE:
					DBG(TAG,"CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE");
					setState(AB_STATE_LOGIN_WRONG_EMAIL_PASSWORD);
				
				default:
					DBG(TAG,"Error CCLogin Billing "+message);
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
			}
		}
		if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	/**
	 * This method is called from Model with a response from server, after such a
	 * request has been initiated with <code>sendUserBillRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCUserBill(boolean success, int message)
	{
		DBG(TAG,"updateCCUserBill()");
		
		if(success)
		{	
			setState(AB_STATE_CC_TRANSACTION_SUCCESS);
			SUtils.getLManager().TrackingPurchaseSuccess(2);
		}else
		{
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_USERNAME:
					DBG(TAG,"CC_ERROR_PBC_INVALID_USERNAME");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_REFERENCE:
					DBG(TAG,"CC_ERROR_PBC_INVALID_REFERENCE");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_USER_NOT_EXIST:
					DBG(TAG,"CC_ERROR_PBC_USER_NOT_EXIST");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_USER_WITH_CC_ALREADY_REGISTERED:
					DBG(TAG,"CC_ERROR_PBC_USER_WITH_CC_ALREADY_REGISTERED");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_USER_DONT_HAS_A_CC_REGISTERED:
					DBG(TAG,"CC_ERROR_PBC_USER_DONT_HAS_A_CC_REGISTERED");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_TOO_MANY_CARDS_FOR_USER:
					DBG(TAG,"CC_ERROR_PBC_TOO_MANY_CARDS_FOR_USER");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_CARD_NUMBER:
					DBG(TAG,"CC_ERROR_PBC_INVALID_CARD_NUMBER");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_PURCHASE_FORMAT:
					DBG(TAG,"CC_ERROR_PBC_INVALID_PURCHASE_FORMAT");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_PURCHASE_NOT_FOUND:
					DBG(TAG,"CC_ERROR_PBC_PURCHASE_NOT_FOUND");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_PURCHASE_IS_NOT_PAYBOX:
					DBG(TAG,"CC_ERROR_PBC_PURCHASE_IS_NOT_PAYBOX");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_PURCHASE_ALREADY_REFUNDED:
					DBG(TAG,"CC_ERROR_PBC_PURCHASE_ALREADY_REFUNDED");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_PARAMETER:
					DBG(TAG,"CC_ERROR_PBC_INVALID_PARAMETER");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_TRANSACTION_NOT_ALLOWED:
					DBG(TAG,"CC_ERROR_PBC_TRANSACTION_NOT_ALLOWED");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
			    
				default:
					DBG(TAG,"Error CCUserBill Billing "+message);
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
			}
			SUtils.getLManager().TrackingPurchaseFailed(2);
		}
		if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	/**
	 * This method is called from Model with a response from server, after such a
	 * request has been initiated with <code>sendNewUserBillRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCNewUserBill(boolean success, int message)
	{
		DBG(TAG,"updateCCNewUserBill()");
		
		if(success)
		{	
			setState(AB_STATE_CC_TRANSACTION_SUCCESS);
			SUtils.getLManager().TrackingPurchaseSuccess(3);
		}else
		{
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				case XPlayer.CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_NICKNAME_UNAVAILABLE:
					DBG(TAG,"CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED");
					goToLayout(R.layout.ab_layout_create_new_account_email_exists);
				break;
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_EMAIL:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_PASSWORD:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_CARD_NUMBER:
				case XPlayer.CC_ERROR_USER_CREATED_INVALID_CARD_NUMBER:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_CCV_MISSING_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_USER_CREATED_CCV_MISSING_INVALID_EXP_DATE:
					DBG(TAG,"CC_ERROR_WRONG_DATA");
					setState(AB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA);
				break;
				case XPlayer.CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE://User Created Invalid Credit Card
				case XPlayer.CC_ERROR_INVALID_CREDIT_CARD_VALUE:	
				case XPlayer.CC_ERROR_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_INVALID_CCV:	
					DBG(TAG,"CC_ERROR_WRONG_CARD_INFO");
					setState(AB_STATE_CREDIT_CARD_EXPIRED);
				break;
				default:
					DBG(TAG,"Error CCNewUserBill Billing "+message);
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
			}
			SUtils.getLManager().TrackingPurchaseFailed(3);
		}
		if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	/**
	 *  This method is called from Model with a response from server, after such a
	 * request has been initiated with <code>sendSingleBillRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCSingleBill(boolean success, int message)
	{
		DBG(TAG,"updateCCSingleBill()");
		
		if(success)
		{	
			setState(AB_STATE_CC_TRANSACTION_SUCCESS);
			SUtils.getLManager().TrackingPurchaseSuccess(4);
		}else
		{
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE://User Created Invalid Credit Card
				case XPlayer.CC_ERROR_INVALID_CREDIT_CARD_VALUE:	
				case XPlayer.CC_ERROR_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_INVALID_CCV:	
					DBG(TAG,"CC_ERROR_WRONG_CARD_INFO");
					setState(AB_STATE_CREDIT_CARD_EXPIRED);
				break;
				
				default:
					DBG(TAG,"Error CCNewUserBill Billing "+message);
					setState(AB_STATE_CC_TRANSACTION_FAILED);
				break;
			}
			SUtils.getLManager().TrackingPurchaseFailed(4);
		}
		if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	/**
	 * This method is called from Model with a response from server, after such a
	 * request has been initiated with <code>sendForgotPasswordRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCForgotPassword(boolean success, int message)
	{
		DBG(TAG,"updateCCForgotPassword()");
		setProgressBarVisibility(R.id.ProgressBarFP, ProgressBar.INVISIBLE);
		if(success)
		{	
			setTextViewNewText( R.id.lblForgotPassword, R.string.AB_CC_RECOVERY_PASSWORD_SUCCES, -1);
		}else
		{
			setTextViewNewText( R.id.lblForgotPassword, R.string.AB_CC_RECOVERY_PASSWORD_FAIL, Color.RED);
			setButtonVisibility(R.id.bt_ly_forgot_password_ok, Button.VISIBLE);
			DBG(TAG,"Error CCForgotPassword "+message);
		}

		setButtonVisibility(R.id.bt_ly_forgot_password_back, Button.VISIBLE);
    	setButtonVisibility(R.id.bt_ly_forgot_password_tcs, Button.VISIBLE);
    	setButtonVisibility(R.id.bt_ly_forgot_password_exit, Button.VISIBLE);
    	setState(AB_STATE_CC_FORGOT_PASSWORD_RESULT);
    	if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	
	//show the id error message + try again? question
	public void setError(int message, int retryState)
	{	
	
		mState = STATE_PAY_ERROR;
		if(message != -1)				//if not recover previous state
			mErrorMessage = message;
		
		if(retryState != -1)			//if not recover previous state
			mTryAgainState = retryState;

		mMessage 	= mErrorMessage;
		mButtons	= mButtonsYesNo;
	}
	
	long m_phonedatatimeout=0;
	boolean TIMED_OUT = false;
	long PHONE_DATA_TIME_OUT = 25000;
	
	private void updatePayOnBill()
	{
				
		if (mSubstate == SUBSTATE_INIT)
		{
		
			if((mDevice.getServerInfo().getBillingType().equals("HTTP"))||(mbBillingMRC))
			{
				if(mBilling == null)
				{
					if(mDevice.IsWifiEnable())
					{
						m_bWasWifiEnabled = true;
						mDevice.DisableWifi();
						m_phonedatatimeout = System.currentTimeMillis();
					}
					while(mDevice.IsWifiDisabling())
					{
						DBG(TAG,"Disabling Wi-Fi");
					}
					while (!mDevice.IsConnectionReady() && !TIMED_OUT)
					{
						DBG(TAG,"Waiting for transference on Phone Data Service");
						DBG(TAG,"m_phonedatatimeout="+m_phonedatatimeout);						
						DBG(TAG,"System.currentTimeMillis() - m_phonedatatimeout="+(System.currentTimeMillis() - m_phonedatatimeout));
						
						if (System.currentTimeMillis() - m_phonedatatimeout > PHONE_DATA_TIME_OUT)
							TIMED_OUT = true;
					}
					TIMED_OUT = false;
					mBilling = new Model(this, mDevice);
					if(mbBillingMRC)
						mBilling.MRCFullVersion();
					else
					{
						mServerInfo.searchForAditionalProfile("HTTP");
						mBilling.buyFullVersion();
					}
					mSubstate = SUBSTATE_WAITING;
				}
			}else if(mDevice.getServerInfo().getBillingType().equals("SMS"))
			{
				if(mBilling == null)
				{
					mServerInfo.searchForAditionalProfile("SMS");
					mBilling = new Model(this, mDevice);
					mBilling.buyFullSMS();
					mSubstate = SUBSTATE_WAITING;
				}
			}
		#if USE_BILLING_FOR_CHINA
			else if(mDevice.getServerInfo().getBillingType().equals("UMP"))
			{
				if(UMP4Activity.requestOrderId(mDevice))
				{
					UMP4Activity.LaunchUMPBilling(this, mDevice);
					mSubstate = SUBSTATE_WAITING;
				}
				else
					setState(AB_STATE_NO_DATA_CONNECTION_DETECTED);
			}
			else if(mDevice.getServerInfo().getBillingType().equals("IPX"))
			{
				mBilling = new Model(this, mDevice);
				((Model)mBilling).buyFullIPX();
				mSubstate = SUBSTATE_WAITING;
			}
		#endif
		}
	}
	
#if USE_BILLING_FOR_CHINA
    /**
     * Call back function from SMS sent status
     * @param success TRUE: send/delivery success, FALSE: send/delivery fail
     * @param message: 0 -> Set status, 1 -> delivery status
     */
	
	public void onSMSsend(boolean success, int message)
	{
		DBG("Billing", "onSMSsend success="+success+" message="+message);
		if(success)
		{
			if(mServerInfo.getBillingType().equals("IPX") && message == 0 /* SENT SUCCESS */)
			{
				Profile profileIPX = mServerInfo.getCurrentProfileSelected();
				IPXItem item = profileIPX.getIPXItemList().get(profileIPX.getIPXSentCount());
				
				mServerInfo.getCurrentProfileSelected().addIPXPaidMoney(item.getPrice());
				mServerInfo.getCurrentProfileSelected().addIPXSentCount(1);
				if(mServerInfo.getCurrentProfileSelected().getIPXSentCount() == mServerInfo.getCurrentProfileSelected().getIPXSMSCount())
				{
					updateBillingResult(true, R.string.AB_THANKS_FOR_THE_PURCHASE);
					SUtils.getLManager().saveUnlockGame(1);
					notifyUnlock();
				}
				else
				{
					if(mBilling != null)
					{
						mBilling.release();
						mBilling = null;
					}
					if(mServerInfo.getCurrentProfileSelected().getIpxMessagingMode() == 0 /* ONE BY ONE */)
						setState(AB_STATE_GET_FULL_VERSION_QUESTION);
					else //1 /* CONTINUOUS */ 
					{						
						setState(AB_STATE_PLEASE_WAIT_PURCHASE_IPX_CONT);
					}
				}
			}
		}
	}
	
    /**
     * Notify to Gameloft server user unlock game to record this
	 * Notice: this function only use for IPX buy
     */	
	public void notifyUnlock()
	{
		DBG("Billing", "notifyUnlock()");
		XPlayer mXPlayer = new XPlayer(mDevice);
		String regAdd = "https://secure.gameloft.com/unlock_cn/validation.php";
		String query = "method=reg&imei="+mDevice.getIMEI()+"&product="+GL_PRODUCT_ID+"&sign="+XPlayer.hmacSha1("_gloft_cn_2012","methodregimei"+mDevice.getIMEI()+"product"+GL_PRODUCT_ID);
		mXPlayer.sendRequest(regAdd, query);
		while (!mXPlayer.handleRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
			DBG("Billing", "[notifyUnlock]Waiting for response");
		}
		String tmpHR = mXPlayer.getWHTTP().m_response;
		if(tmpHR != null && tmpHR.contains("success"))
		{
			DBG("Billing", "notifyUnlock success");
		}
		else
		{
			DBG("Billing", "notifyUnlock fail");
		}
	}
	
    /**
     * Check for the sim ready
     * @return boolean
     */	
	public static boolean isSIMReady()
	{
		TelephonyManager mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		if (mDeviceInfo.getSimState() == TelephonyManager.SIM_STATE_READY) 
		{
			return true;
		}
		return false;
	}
#endif

	public static boolean hasConnectivity()
	{
		ConnectivityManager mConnectivityManager     = (ConnectivityManager) SUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mConnectivityManager == null)
			return false;
        NetworkInfo mNetInfo = mConnectivityManager.getActiveNetworkInfo();
        if(mNetInfo == null)
			return false;

		if (mNetInfo.getState() == NetworkInfo.State.CONNECTED) {
			if (mNetInfo.getTypeName().equalsIgnoreCase("WIFI")) {
				DBG("Billing","Connected to WIFI?" + mNetInfo.isConnected());
				return mNetInfo.isConnected();
			} 
		}
		return false;
	}


}
#endif
