#if USE_IN_APP_BILLING


//get encrypted string
#define GES(id)		StringEncrypter.getString(id)
//get raw string
#define GRS(id)		SUtils.getContext().getString(id)



package APP_PACKAGE.iab;


//import APP_PACKAGE.iab.Base64;

import APP_PACKAGE.GLUtils.LowProfileListener;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


import APP_PACKAGE.billing.common.AModelActivity;
import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.billing.common.UserInfo;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.webkit.JavascriptInterface;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.graphics.Bitmap;
import android.inputmethodservice.InputMethodService;
#if ADS_USE_FLURRY
import APP_PACKAGE.Flurry.GLFlurry;
#endif

import java.util.HashMap;
import android.provider.Settings.Secure;
import org.json.JSONException;
import org.json.JSONObject;

public class InAppBillingActivity extends AModelActivity implements Runnable {
	
	public TelephonyManager mDeviceInfo;
	SET_TAG("InAppBillingActivity");
	
	static final int RETRY_SLEEP_TIME		= 1000;
	static final int RETRY_MAX				= 3;
	static int retryCount = 0;

	private  boolean mAndroidBStarted 	= false;

	AssetManager mAssetManager;
	 
	public final int IAB_STATE_GET_FULL_VERSION_QUESTION				= 0;
	public final int IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS			= 1;
	public final int IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST	= 2;
	public final int IAB_STATE_PB_TRANSACTION_FAILED					= 3;
	public final int IAB_STATE_PB_TRANSACTION_SUCCESS				= 4;
	public final int IAB_STATE_MANUAL_UNLOCK							= 5;
	public final int IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW			= 6;
	public final int IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST	= 7;
	public final int IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN				= 8;
	public final int IAB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT		= 9;
	public final int IAB_STATE_CC_TRANSACTION_FAILED					= 10;
	public final int IAB_STATE_CC_TRANSACTION_SUCCESS				= 11;
	public final int IAB_STATE_CC_CREATE_ACCOUNT						= 12;
	public final int IAB_STATE_CC_PLEASE_WAIT_PURCHASE_PROGRESS		= 13;
	public final int IAB_STATE_CREATE_NEW_ACCOUNT_EMAIL_EXISTS		= 14;
	public final int IAB_STATE_CC_CREATE_NEW_ACCOUNT_PORTRAIT		= 15;
	public final int IAB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA			= 16;
	public final int IAB_STATE_CREDIT_CARD_EXPIRED					= 17;
	public final int IAB_STATE_ENTER_UNLOCK_CODE						= 18;
	public final int IAB_STATE_ENTER_UNLOCK_CODE_PROGRESS			= 19;
	public final int IAB_STATE_ENTER_UNLOCK_CODE_SUCCESS				= 20;
	public final int IAB_STATE_ENTER_UNLOCK_CODE_FAILED				= 21;
	public final int IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD			= 22;
	public final int IAB_STATE_PAY_WO_ACCOUNT						= 23;
	public final int IAB_STATE_PAY_WO_ACCOUNT_REQUEST				= 24;
	public final int IAB_STATE_CC_LOGIN_BUY_NOW						= 25;
	public final int IAB_STATE_CC_LOGIN_BUY_NOW_REQUEST				= 26;
	public final int IAB_STATE_CC_USERBILL_BUY_NOW					= 27;
	public final int IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST			= 28;
	public final int IAB_STATE_CC_CREATE_ACCOUNT_BUY_NOW				= 29;
	public final int IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW = 30;
	public final int IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST = 31;
	public final int IAB_STATE_CC_FORGOT_PASSWORD					= 32;
	public final int IAB_STATE_CC_FORGOT_PASSWORD_REQUEST			= 33;
	public final int IAB_STATE_CC_FORGOT_PASSWORD_RESULT				= 34;
	public final int IAB_STATE_NO_DATA_CONNECTION_DETECTED			= 35;
	public final int IAB_STATE_GAME_PURCHASED						= 36;
	public final int IAB_STATE_INITIALIZE							= 37;
	public final int IAB_STATE_VERIFYING_PURCHASE					= 38;
	public final int IAB_STATE_WAP_BILLING							= 39;
	public final int IAB_STATE_PAYPAL								= 40;
	public final int IAB_STATE_PREPRARE_WAP_DATA_CONNECTION 			= 41;
	public final int IAB_STATE_READ_GAME_CODE						= 42;
	public final int IAB_STATE_NO_PROFILE_DETECTED					= 43;
	public final int IAB_STATE_SHENZHOUFU_WARNING_MESSAGE			= 44;
	public final int IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW				= 45;
	public final int IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_REQUEST		= 46;
	public final int IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_FAIL		= 47;
	public final int IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_SUCCESS		= 48;
	public final int IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED		= 49;
	public final int IAB_STATE_FINALIZE 							= 50;
	
	public final int SUBSTATE_INIT = 1;
	public final int SUBSTATE_WAITING = 2;
	public final int SUBSTATE_HANDLING = 3;
	
	private final String mStates[] ={
			"IAB_STATE_GET_FULL_VERSION_QUESTION",//0
			"IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS",//1
			"IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST",//2
			"IAB_STATE_PB_TRANSACTION_FAILED",//3
			"IAB_STATE_PB_TRANSACTION_SUCCESS",//4
			"IAB_STATE_MANUAL_UNLOCK",//5
			"IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW",//6
			"IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST",//7
			"IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN",//8
			"IAB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT",//9
			"IAB_STATE_CC_TRANSACTION_FAILED",//10
			"IAB_STATE_CC_TRANSACTION_SUCCESS",//11
			"IAB_STATE_CC_CREATE_ACCOUNT",//12
			"IAB_STATE_CC_PLEASE_WAIT_PURCHASE_PROGRESS",//13
			"IAB_STATE_CREATE_NEW_ACCOUNT_EMAIL_EXISTS",//14
			"IAB_STATE_CC_CREATE_NEW_ACCOUNT_PORTRAIT",//15
			"IAB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA",//16
			"IAB_STATE_CREDIT_CARD_EXPIRED",//17
			"IAB_STATE_ENTER_UNLOCK_CODE",//18
			"IAB_STATE_ENTER_UNLOCK_CODE_PROGRESS",//19
			"IAB_STATE_ENTER_UNLOCK_CODE_SUCCESS",//20
			"IAB_STATE_ENTER_UNLOCK_CODE_FAILED",//21
			"IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD",//22
			"IAB_STATE_PAY_WO_ACCOUNT",//23
			"IAB_STATE_PAY_WO_ACCOUNT_REQUEST",//24
			"IAB_STATE_CC_LOGIN_BUY_NOW",//25
			"IAB_STATE_CC_LOGIN_BUY_NOW_REQUEST",//26
			"IAB_STATE_CC_USERBILL_BUY_NOW",//27
			"IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST",//28
			"IAB_STATE_CC_CREATE_ACCOUNT_BUY_NOW",//29
			"IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW",//30
			"IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST",//31
			"IAB_STATE_CC_FORGOT_PASSWORD",//32
			"IAB_STATE_CC_FORGOT_PASSWORD_REQUEST",//33
			"IAB_STATE_CC_FORGOT_PASSWORD_RESULT",//34
			"IAB_STATE_NO_DATA_CONNECTION_DETECTED",//35
			"IAB_STATE_GAME_PURCHASED",//36
			"IAB_STATE_VERIFYING_PURCHASE",//37
			"IAB_STATE_INITIALIZE",//38
			"IAB_STATE_WAP_BILLING",//39
			"IAB_STATE_PAYPAL",//40
			"IAB_STATE_PREPRARE_WAP_DATA_CONNECTION",//41
			"IAB_STATE_READ_GAME_CODE",//42
			"IAB_STATE_NO_PROFILE_DETECTED",//43
			"IAB_STATE_SHENZHOUFU_WARNING_MESSAGE",//44
			"IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW",//45
			"IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_REQUEST",//46
			"IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_FAIL",//47
			"IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_SUCCESS",//48
			"IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED",//49
			"IAB_STATE_FINALIZE",//50
			};
	
	
	private String mGameName = null;
	// instead of hardcoding
	private String mGamePrice = null;   //real value must be taken from server.
	private String mTCSText = null;
	
	public  int mSubstate;
	public  int mPrevSubstate; 

	//public  int mState;
	public  int mPrevState;

	private long m_timeOut = 0;

	WifiManager	mWifiManager;
	ConnectivityManager mConnectivityManager;
	WifiLock		mWifiLock;
	
	private boolean m_bWasWifiEnabled	= false;
	private boolean mVerifyPurchase		= false;
    GLOFTHelper mHelperDevice;
	
	int mStatus;
	
	boolean mBillingStarted;
	int mState, mOldState, mTryAgainState;
	
	int mTitle, mMessage, mErrorMessage;
	int mButtons[];

	public static final int NO_ACTION		= -1;
	
	Device mDevice;
	//Model mBilling;
	//public XPlayer xplayer;
	public static AServerInfo mServerInfo;
	boolean m_bIsSingleBill = false;
	String mPromoCode = null;
	
	private Bundle mWapErrBundle = null;

	#if SHENZHOUFU_STORE
	String[] mPrepaidCardType;
	String shenzhoufuErrorString;
	// = new String[]{
		// getString(R.string.IAB_SHENZHOUFU_LAYOUT_PREPAID_CARD_TYPE_CHINA_MOBILE), 
		// getString(R.string.IAB_SHENZHOUFU_LAYOUT_PREPAID_CARD_TYPE_CHINA_UNICOM), 
		// getString(R.string.IAB_SHENZHOUFU_LAYOUT_PREPAID_CARD_TYPE_CHINA_TELECOM), 
	// };	
	public String[] mPrepaidCardMobileDenominations = new String[]{"10","20","30","50","100","300","500"};
	public String[] mPrepaidCardUnicomDenominations = new String[]{"20","30","50","100","300","500"};
	public String[] mPrepaidCardTelecomDenominations = new String[]{"20","30","50","100"};
	
	ArrayAdapter adapterType;
	ArrayAdapter adapterDenom;
	Spinner sCardType;
	Spinner sCardDenom;
	AlertDialog.Builder mShenzhoufuPurchaseDialog;
	#endif
	
	boolean mb_waitingActivationWord = false;
	boolean mb_isNextActivated = false;
	
	WebView webView = null;
	String MAGIC_WORD="PURCHASE_ID_";
	//String MAGIC_WORD="Noticias";
	AlertDialog.Builder mPurchaseDialog;
	AlertDialog.Builder mDialog;
	
	
	HashMap<String,String> map = new HashMap<String,String>();
	
	class MyWebViewClient extends WebViewClient 
	{
		ProgressDialog dialog = null;
		InAppBillingActivity mBActivity = null;
		public MyWebViewClient(InAppBillingActivity iab)
		{
			super();
			mBActivity = iab;
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			DBG(TAG, "shouldOverrideUrlLoading "+url);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "shouldOverrideUrlLoading "+url);
			if(map != null && map.size() != 0 )
			{
				DBG(TAG, "Reload webView with headers");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Reload webView with headers");
				view.loadUrl(url, map);
			}
			else
				view.loadUrl(url);
			return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap  favicon)
		{
			DBG(TAG, "onPageStarted "+url);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onPageStarted "+url);
			if(dialog == null)
			{
				dialog = new ProgressDialog(InAppBillingActivity.this);
			}
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(getString(R.string.IAB_LOADING));
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
		public void onLoadResource(WebView view, String url)
		{
#if USE_ALIPLAY_WAP			
			if(url.contains("common_check_code.htm"))
			{
				Button bt = (Button) findViewById(R.id.bt_ly_wap_billing_exit);
				bt.setVisibility(bt.INVISIBLE);
			}
			else
			{
				Button bt = (Button) findViewById(R.id.bt_ly_wap_billing_exit);
				bt.setVisibility(bt.VISIBLE);				
			}
#endif
			//only for docomo
			if ( (mDevice.getSimOperatorName()!= null && mDevice.getSimOperatorName().toLowerCase().contains("docomo"))
			|| (mDevice.getNetworkOperatorName()!= null && mDevice.getNetworkOperatorName().toLowerCase().contains("docomo")) )
			{
				if(url.contains("btn_cancel"))
				{
					Button bt = (Button) findViewById(R.id.bt_ly_wap_billing_exit);
					bt.setVisibility(bt.VISIBLE);
				}
				
				// disable x button when buy is complete			
				if(url.contains("btn_funcnext"))
				{
					Button bt = (Button) findViewById(R.id.bt_ly_wap_billing_exit);
					bt.setVisibility(bt.INVISIBLE);		
				}
			}
		}
		
		@Override
		public void onPageFinished(WebView view, String url)
		{
			DBG(TAG, "onPageFinished "+url);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onPageFinished "+url);
				try
				{
					if(dialog == null)
					{
						dialog = new ProgressDialog(InAppBillingActivity.this);
					}
					else
					{
						dialog.dismiss();
						dialog = null;
					}

					//in case there's an error, get the string from the webview
					if( url.contains("v=1.2") && (url.contains("&error=") || url.contains("?error=")) && (mServerInfo.getBillingType().equals("wap_other") || mServerInfo.getBillingType().equals("paypal")))
					{
						if(mWapErrBundle != null)
							mWapErrBundle.clear();
						mWapErrBundle  = null;
						webView.loadUrl("javascript:window.HTMLOUT.processWebViewResult(document.getElementsByTagName('body')[0].innerHTML);");
					} else
					{
						//if no error, do the normal flow
						mBActivity.setWAPNextButtonVisibility();
					}
				}
				catch (Exception ex)
				{
					DBG_EXCEPTION(ex);
				}
		}
		
		@Override
    	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
    		WARN(TAG, "onReceivedError "+failingUrl);
			WARN(TAG, "errorCode="+errorCode);
			WARN(TAG, "description="+description);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_ERROR, "onReceivedError "+failingUrl);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_ERROR, "errorCode="+errorCode);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_ERROR, "description="+description);
			super.onReceivedError(view, errorCode, description, failingUrl);
			
			if(mServerInfo.getBillingType().equals("wap_other"))
			{
				webView.loadUrl("about:blank");
				if(dialog != null)
				{
					dialog.dismiss();
					dialog = null;
				}
				if(mDialog!=null)
				{
					mDialog.setCancelable(false)
					.setPositiveButton(getString(R.string.IAB_SKB_OK), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							setState(IAB_STATE_FINALIZE);
							
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
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
					}
					);
					
					AlertDialog alert = mDialog.create();
					alert.setTitle(getString(R.string.IAB_TRANSACTION_FAILED));
					alert.setMessage(getString(R.string.IAB_NETWORK_ERROR_TITLE));
					alert.show();
				}
			}
    	}
		
		@Override
		public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error){
			WARN(TAG, "onReceivedSslError");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_ERROR, "onReceivedSslError");
			//super.onReceivedSslError(view, handler, error);
			handler.proceed();
		}
	}

	class MyJavaScriptInterface
	{
		InAppBillingActivity mBActivity = null;
		public MyJavaScriptInterface(InAppBillingActivity iab)
		{
			mBActivity = iab;
		}

		@JavascriptInterface
		public void processWebViewResult(final String html)
		{
			INFO(TAG, "processed html:\n"+html);
			try
			{
				mWapErrBundle = new Bundle();
				mWapErrBundle.clear();

				JSONObject jobj = new JSONObject(html);

				String value = jobj.has("message")?jobj.getString("message"):null;
				if(value != null)
					mWapErrBundle.putString(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_ERR_MESSAGE),value);

				value = jobj.has("next_transaction_time")?jobj.getString("next_transaction_time"):null;
				if(value != null)
					mWapErrBundle.putString(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_NEXT_TRANS_TIME),value);

				value = jobj.has("seconds_before_next_transaction")?jobj.getString("seconds_before_next_transaction"):null;
				if(value != null)
					mWapErrBundle.putInt(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_SECONDS_BEFORE_TRANS),Integer.parseInt(value));

				value = jobj.has("code")?jobj.getString("code"):null;
				if(value != null)
				{
					Integer error = GlShopErrorCodes.WapOtherErrorCodes.get(Integer.parseInt(value));
					int error_code = Integer.parseInt(value);
					if (error != null)
					{
						error_code = GlShopErrorCodes.WapOtherErrorCodes.get(error_code);
					}
					mWapErrBundle.putInt(InAppBilling.GET_STR_CONST(IAB_WAP_BUNDLE_ERR_CODE), error_code);//todo: obtain from json
				}

				((Activity)SUtils.getContext()).runOnUiThread(new Runnable () 
				{
					public void run () 
					{
						mb_waitingActivationWord = false;
						mb_isNextActivated = true;
						
						webView.clearHistory();
						
						GLOFTHelper.setWAPID(GetWAPPurchaseID());
						GLOFTHelper.setWAPTxID(GetWAPTransactionID());
						GLOFTHelper.setWAPErrorBundle(GetWapErrorBundle());
						GLOFTHelper.GetInstance().ValidateWAPPurchase();
						setState(IAB_STATE_FINALIZE);
					}
				});
				
			}catch(JSONException jsone)
			{
				DBG_EXCEPTION(jsone);
				mWapErrBundle = null;
			}
		}
    }


	public InAppBillingActivity() {
		//SUtils.setContext(this);
		InitDeviceInfo();
	}
		
	public void showConfirm()
	{
		/*Context mContext = SUtils.getContext();
		Dialog dialog = new Dialog(mContext);

		dialog.setContentView(R.layout.custom_dialog);
		dialog.setTitle("Custom Dialog");

		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText("Hello, this is a custom dialog!");
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.android);*/
	}	
	
	public boolean canInterrupt = false;
	public void run() {
		DBG(TAG,"Android Billing...");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Android Billing...");
		//Looper.prepare();
		setState(IAB_STATE_INITIALIZE);
		//showConfirm();
		
		while (mState != IAB_STATE_FINALIZE) 
		{
			canInterrupt = false;
			long time = System.currentTimeMillis();
			
			try{
			update();
			}catch(Exception EX){finish();}
			time = 10;
			if(time>0) {
				try { 
					Thread.sleep(time);
				}	catch(Exception e){};
			}
			canInterrupt = true;
		}
		ERR(TAG,"FINILIZED!!!!!!!!");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "FINILIZED!!!!!!!!");
		mAssetManager = null;
		finish();
	}
	
	//******************************************************************
    private static final int DIALOG_YES_NO_MESSAGE = 1;
    private static final int DIALOG_YES_NO_LONG_MESSAGE = 2;
    private static final int DIALOG_LIST = 3;
    private static final int DIALOG_PROGRESS = 4;
    private static final int DIALOG_SINGLE_CHOICE = 5;
    private static final int DIALOG_MULTIPLE_CHOICE = 6;
    private static final int DIALOG_TEXT_ENTRY = 7;

    private static final int MAX_PROGRESS = 100;
    
	//******************************************************************
	public void update ()
	{
		switch(mState)
		{
			case IAB_STATE_INITIALIZE:

				INFO(TAG, "GLOFTHelper.GetBillingType()="+GLOFTHelper.GetBillingType());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "GLOFTHelper.GetBillingType()="+GLOFTHelper.GetBillingType());
				if (GLOFTHelper.GetBillingType()==GLOFTHelper.BT_CREDIT_CARD)
				{
					setState(IAB_STATE_CC_CREATE_ACCOUNT);
				}
				else if (GLOFTHelper.GetBillingType()==GLOFTHelper.BT_WAP_BILLING)
				{
					setState(IAB_STATE_WAP_BILLING);
				}
				else if (GLOFTHelper.GetBillingType()==GLOFTHelper.BT_WAP_PAYPAL_BILLING)
				{
					setState(IAB_STATE_PAYPAL);
				}
				#if SHENZHOUFU_STORE
				else if (GLOFTHelper.GetBillingType()==GLOFTHelper.BT_SHENZHOUFU_BILLING)
					{
						setState(IAB_STATE_SHENZHOUFU_WARNING_MESSAGE);
					}
				#endif
			break;
				
			
			case IAB_STATE_VERIFYING_PURCHASE:

			break;
			
			case IAB_STATE_PREPRARE_WAP_DATA_CONNECTION:
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
				if(TIMED_OUT)
				{
					ERR(TAG,"Timed out while preparing wap data connection!");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Timed out while preparing wap data connection!");
				  	setState(IAB_STATE_NO_DATA_CONNECTION_DETECTED);
				}
				else
				{
				  	setState(IAB_STATE_WAP_BILLING);
				}
				TIMED_OUT = false;
			break;

			// case IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST:
					// updatePayOnBill();
			// break;
			
            case IAB_STATE_PAY_WO_ACCOUNT_REQUEST:
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
			#if SHENZHOUFU_STORE
            case IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{
					ProcessTransactionShenzhoufu(GLOFTHelper.GetItemId());
					mSubstate = SUBSTATE_WAITING;
            	}
        	break;
			#endif
            case IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST:
            case IAB_STATE_CC_LOGIN_BUY_NOW_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{	
					if(mBilling == null)
					{
						mBilling = new Model(this, mDevice);
						if (addNewCCToUser)
							mBilling.sendAddCardBillRequest();
						else
							mBilling.sendLoginRequest();
						mSubstate = SUBSTATE_WAITING;
					}
            	}
        	break;
        	
            case IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST:
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
        
		
            case IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST:
            	if (mSubstate == SUBSTATE_INIT)
            	{	
					if(mBilling == null)
					{
						mBilling = new Model(this, mDevice);
						if (addNewCCToUser)
							mBilling.sendAddCardBillRequest();
						else 
							mBilling.sendNewUserBillRequest();
						mSubstate = SUBSTATE_WAITING;
					}
            	}
        	break;
        	
            case IAB_STATE_CC_FORGOT_PASSWORD_REQUEST:
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
        	
			//update for KDDI
            case IAB_STATE_WAP_BILLING:
				if(mSubstate == SUBSTATE_INIT)
				{
					mSubstate = SUBSTATE_HANDLING;
					if(mServerInfo.getBillingType().equals("wap_other"))
					{
						try{
							String verify = md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft");
							String httpResponseString = getTimeStampRequest(mServerInfo.getURLbilling());
							if (httpResponseString == null)
							{
								if(mDialog!=null)
								{
									((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
									{
									public void run()
									{
									mSubstate = SUBSTATE_WAITING;//wait to finish
									mDialog.setCancelable(false)
									.setPositiveButton(getString(R.string.IAB_SKB_OK), new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int id)
										{
											setState(IAB_STATE_FINALIZE);
											
											Bundle bundle = new Bundle();
											bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
																
											bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
											bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
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
									}
									);
									
									AlertDialog alert = mDialog.create();
									alert.setTitle(getString(R.string.IAB_TRANSACTION_FAILED));
									alert.setMessage(getString(R.string.IAB_NETWORK_ERROR_TITLE));
									alert.show();
									}});
								}
								return;
							}
							httpResponseString = httpResponseString.substring(2, httpResponseString.length()-2);
							MEncoder encode = new MEncoder((md5(verify)).substring(0,16), verify.substring(6,verify.length())+httpResponseString);
							String uID = "IAPOK" + Secure.getString(SUtils.getContext().getContentResolver(), Secure.ANDROID_ID);
							String uandroid = MEncoder.bytesToHex( encode.encrypt(uID) );

							DBG(TAG, "Timestamp:"+httpResponseString);
							DBG(TAG, "Android ID:"+uID);
							DBG(TAG, "Complete verify (MD5):"+verify);
							DBG(TAG, "Vector:"+verify.substring(0,16));
							DBG(TAG, "Security Key:"+(verify.substring(6,verify.length())+httpResponseString));
							#if HDIDFV_UPDATE
							DBG(TAG, "Adding " + InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV) + " to webView headers:"+mDevice.getHDIDFV());
							#endif
							DBG(TAG, "Adding uandroid to webView headers:"+uandroid);							

							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Timestamp:"+httpResponseString);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Android ID:"+uID);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Complete verify (MD5):"+verify);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Vector:"+verify.substring(0,16));
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Security Key:"+(verify.substring(6,verify.length())+httpResponseString));							
							#if HDIDFV_UPDATE
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding hdidfv to webView headers:"+mDevice.getHDIDFV());
							#endif
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding uandroid to webView headers:"+uandroid);							
 							
							map.put("uandroid",uandroid);							
							#if HDIDFV_UPDATE
							map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),mDevice.getHDIDFV());
							#endif
						}catch(Exception e){};
					}
					// Adding common header
					DBG(TAG, "Adding imei and x-up-gl-imei to webView headers:"+mDevice.getIMEI());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding imei and x-up-gl-imei to webView headers:"+mDevice.getIMEI());
					map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),mDevice.getIMEI());

					DBG(TAG, "Adding x-up-gl-ggi to webView headers:"+GGI);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-ggi to webView headers:"+GGI);
					map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI), ""+GGI);
					
					XPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
					if (XPlayer.mGLLiveUid != null)
					{
						DBG(TAG, "Adding x-up-gl-acnum to webView headers:\'" + XPlayer.mGLLiveUid + "\'");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-acnum to webView headers:\'" + XPlayer.mGLLiveUid + "\'");
						map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM), XPlayer.mGLLiveUid);
					}
					if (XPlayer.mUserCreds != null)
					{
						DBG(TAG, "Adding x-up-gl-fed-credentials to webView headers:\'" + XPlayer.mUserCreds + "\'");
						map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS), XPlayer.mUserCreds);
						DBG(TAG, "Adding x-up-gl-fed-client-id to webView headers:\'" + CLIENTID + "\'");
						map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID), CLIENTID);

						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-fed-credentials to webView headers:\'" + XPlayer.mUserCreds + "\'");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-fed-client-id to webView headers:\'" + CLIENTID + "\'");
					}
					XPlayer.setDataCenter(InAppBilling.GET_FED_DATA_CENTER());
					if (XPlayer.mDataCenter != null && !android.text.TextUtils.isEmpty(XPlayer.mDataCenter))
					{
							DBG(TAG, "Adding x-up-gl-fed-datacenter to webView headers:\'" + XPlayer.mDataCenter + "\'");
							map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_FED_DATACENTER), XPlayer.mDataCenter);
							
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-fed-datacenter to webView headers:\'" + XPlayer.mDataCenter + "\'");
					}

				}
				if(mSubstate == SUBSTATE_HANDLING)
				{
					mSubstate = SUBSTATE_WAITING;
					goToLayout(R.layout.iab_layout_wap_billing);
				}
            break;


            case IAB_STATE_PAYPAL:
			{
				if(mSubstate == SUBSTATE_INIT)
				{
					mSubstate = SUBSTATE_HANDLING;
					if(mServerInfo.getBillingType().equals("wap_paypal"))
					{
						try{
							String verify = md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft");
							String httpResponseString = getTimeStampRequest(mServerInfo.getURLbilling());
							if (httpResponseString == null)
							{
								if(mDialog!=null)
								{
									((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
									{
									public void run()
									{
									mSubstate = SUBSTATE_WAITING;//wait to finish
									mDialog.setCancelable(false)
									.setPositiveButton(getString(R.string.IAB_SKB_OK), new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int id)
										{
											setState(IAB_STATE_FINALIZE);
											
											Bundle bundle = new Bundle();
											bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
																
											bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
											bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
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
									}
									);
									
									AlertDialog alert = mDialog.create();
									alert.setTitle(getString(R.string.IAB_TRANSACTION_FAILED));
									alert.setMessage(getString(R.string.IAB_NETWORK_ERROR_TITLE));
									alert.show();
									}});
								}
								return;
							}
							httpResponseString = httpResponseString.substring(2, httpResponseString.length()-2);
							MEncoder encode = new MEncoder((md5(verify)).substring(0,16), verify.substring(6,verify.length())+httpResponseString);
							String uID = "IAPOK" + Secure.getString(SUtils.getContext().getContentResolver(), Secure.ANDROID_ID);
							String uandroid = MEncoder.bytesToHex( encode.encrypt(uID) );

							DBG(TAG, "Timestamp:"+httpResponseString);
							DBG(TAG, "Android ID:"+uID);
							DBG(TAG, "Complete verify (MD5):"+verify);
							DBG(TAG, "Vector:"+verify.substring(0,16));
							DBG(TAG, "Security Key:"+(verify.substring(6,verify.length())+httpResponseString));
							DBG(TAG, "Adding imei and x-up-gl-imei to webView headers:"+mDevice.getIMEI());
							#if HDIDFV_UPDATE
							DBG(TAG, "Adding x-up-gl-hdidfv to webView headers:"+mDevice.getHDIDFV());
							#endif
							DBG(TAG, "Adding uandroid to webView headers:"+uandroid);
							DBG(TAG, "Adding x-up-gl-ggi to webView headers:"+GGI);

							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Timestamp:"+httpResponseString);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Android ID:"+uID);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Complete verify (MD5):"+verify);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Vector:"+verify.substring(0,16));
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Security Key:"+(verify.substring(6,verify.length())+httpResponseString));
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding imei to webView headers:"+mDevice.getIMEI());
							#if HDIDFV_UPDATE
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding hdidfv to webView headers:"+mDevice.getHDIDFV());
							#endif
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding uandroid to webView headers:"+uandroid);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-ggi to webView headers:"+GGI);


 							map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),mDevice.getIMEI());//x-up-gl-imei
							map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI), ""+GGI);//x-up-gl-ggi
							map.put("uandroid",uandroid);
							#if HDIDFV_UPDATE
							map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),mDevice.getHDIDFV());//x-up-gl-hdidfv
							#endif
							
							XPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
							if (XPlayer.mGLLiveUid != null)
							{
								DBG(TAG, "Adding x-up-gl-acnum to webView headers:\'" + XPlayer.mGLLiveUid + "\'");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-acnum to webView headers:\'" + XPlayer.mGLLiveUid + "\'");
								map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM), XPlayer.mGLLiveUid);//x-up-gl-acnum
							}
							if (XPlayer.mUserCreds != null)
							{
								DBG(TAG, "Adding x-up-gl-fed-credentials to webView headers:\'" + XPlayer.mUserCreds + "\'");
								map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS), XPlayer.mUserCreds); //x-up-gl-fed-credentials
								DBG(TAG, "Adding x-up-gl-fed-client-id to webView headers:\'" + CLIENTID + "\'");
								map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID), CLIENTID);//x-up-gl-fed-client-id

								LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-fed-credentials to webView headers:\'" + XPlayer.mUserCreds + "\'");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-fed-client-id to webView headers:\'" + CLIENTID + "\'");
							}
							XPlayer.setDataCenter(InAppBilling.GET_FED_DATA_CENTER());
							if (XPlayer.mDataCenter != null && !android.text.TextUtils.isEmpty(XPlayer.mDataCenter))
							{
									DBG(TAG, "Adding x-up-gl-fed-datacenter to webView headers:\'" + XPlayer.mDataCenter + "\'");
									map.put(InAppBilling.GET_STR_CONST(IAB_HEADER_FED_DATACENTER), XPlayer.mDataCenter);
									
									LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Adding x-up-gl-fed-datacenter to webView headers:\'" + XPlayer.mDataCenter + "\'");
							}

						}catch(Exception e){};
					}
				}
				if(mSubstate == SUBSTATE_HANDLING)
				{
					mSubstate = SUBSTATE_WAITING;
					goToLayout(R.layout.iab_layout_wap_billing);
				}
			}
            break;
            
            default:
			break;
		}
	}
	
	protected void handleBackKey()
	{
		if(
			  (mState == IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST)
			||(mState == IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST)
			||(mState == IAB_STATE_PAY_WO_ACCOUNT_REQUEST)
			||(mState == IAB_STATE_CC_LOGIN_BUY_NOW_REQUEST)
			||(mState == IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST)
			||(mState == IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST)
			||(mState == IAB_STATE_CC_FORGOT_PASSWORD_REQUEST)
			#if SHENZHOUFU_STORE
			||(mState == IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_REQUEST)
			#endif
			)
		{
			return;
		}
		
		if (mState == IAB_STATE_CC_FORGOT_PASSWORD_RESULT)
		{
			  setState(IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
		  	  return;
		}
		if (mState == IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN)
		{
			mUserInfo = null;
		}

		switch(mCurrentLayout)
    	{
        	//Back Buttons for all states
        	case R.layout.iab_layout_create_new_account_email_exists:
        		setState(mPrevState);
        	break;
        	
        	case R.layout.iab_layout_forgot_password:
            	setState(IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
            break;
        	
        	case R.layout.iab_layout_login_wrong_email_password:
        	case R.layout.iab_layout_login:
        	//case R.layout.iab_layout_pay_wo_account:
        	case R.layout.iab_layout_credit_card_expired:
        		setState(IAB_STATE_CC_CREATE_ACCOUNT);
        	break;
        		
        	case R.layout.iab_layout_tcs:
        		goToLayout(mPrevLayout);
        	break;
			
			#if SHENZHOUFU_STORE
			case R.layout.iab_layout_shenzhoufu_enter_data:
			case R.layout.iab_layout_shenzhoufu_warning:
			#endif
			case R.layout.iab_layout_create_new_account_portrait:
			case R.layout.iab_layout_create_new_account:
			case R.layout.iab_layout_wap_billing:
				//disable back key when exit button is hidden
				if (mCurrentLayout == R.layout.iab_layout_wap_billing){
					Button bt = (Button) findViewById(R.id.bt_ly_wap_billing_exit);
					if (bt != null && bt.getVisibility() == View.INVISIBLE)
						break;
				}
				setState(IAB_STATE_FINALIZE);
			#if !USE_PHD_PSMS_BILL_FLOW
				if( InAppBilling.mServerInfo.getItemById(InAppBilling.mItemID).getBillingOptsCount() == 1 && 
					(mServerInfo.getBillingType().equals("wap_other") || mServerInfo.getBillingType().equals("CC") || mServerInfo.getBillingType().equals("wap_paypal")) )
			#endif
				{
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
										
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
					
					if (mCurrentLayout == R.layout.iab_layout_wap_billing)
					{
						int error = (GlShopErrorCodes.WapOtherErrorCodes.get(GlShopErrorCodes.WAP_OTHER_ERROR_USER_CANCEL)).intValue();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), error);
					}


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
				#if !USE_PHD_PSMS_BILL_FLOW
				else
				((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
						{
						String message;
						public void run ()
						{
							try
							{
								CustomizeDialog customizeDialog = new CustomizeDialog(SUtils.getContext(),1);
								customizeDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
								{
									public void onCancel(DialogInterface dialog)
									{
										Bundle bundle = new Bundle();
										bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
															
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
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
				#endif
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
            	setState(IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
            break;
        	
        	case R.id.bt_ly_login_wrong_email_password_back:
        	case R.id.bt_ly_login_back:
        	//case R.id.bt_ly_pay_wo_account_back:
        	case R.id.bt_ly_credit_card_expired_back:
				mUserInfo = null;
        		setState(IAB_STATE_CC_CREATE_ACCOUNT);
    	}
	}
	
	
	public void setState(int state)
    {
    	/*DBG(TAG,"****************************************");
		DBG(TAG,"Setting State to: "+mStates[state]);
		DBG(TAG,"****************************************");*/
		
		switch(state)
    	{
    		
    		case IAB_STATE_GET_FULL_VERSION_QUESTION:
            	//goToLayout(R.layout.iab_layout_get_full_version_question);
    		break;
    		
    		case IAB_STATE_VERIFYING_PURCHASE:
    			m_timeOut = System.currentTimeMillis();
    			//goToLayout(R.layout.iab_layout_please_wait_purchase_progress);
    		break;
    		
    		case IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS:
    			m_timeOut = System.currentTimeMillis();
    			//goToLayout(R.layout.iab_layout_please_wait_purchase_progress);
    		break;
    		
    		case IAB_STATE_PB_TRANSACTION_FAILED:
    				//goToLayout(R.layout.iab_layout_transaction_failed);
    		break;
    		
    		case IAB_STATE_PB_TRANSACTION_SUCCESS:
					//goToLayout(R.layout.iab_layout_thanks_for_the_purchase);
    		break;
    		
    		case IAB_STATE_CC_CREATE_ACCOUNT:
    			m_bIsSingleBill=false;
    			/*if (!(mServerInfo.getBillingType().equals("CC")))
    				mServerInfo.setBillingMethod("CC");*/
    			
    			mGamePrice = mServerInfo.getItemPriceFmt();		
    			mTCSText = mServerInfo.getTNCString();
				mTCSText = getStringFormated(mTCSText,"<currency>", "");
				mTCSText = getStringFormated(mTCSText,"<price>",mGamePrice);
    			
    			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            	//getRequestedOrientation();
            	goToLayout(R.layout.iab_layout_create_new_account);
    		break;
			#if SHENZHOUFU_STORE
			case IAB_STATE_SHENZHOUFU_WARNING_MESSAGE:
    			m_bIsSingleBill=false;
    			mGamePrice = mServerInfo.getItemPriceFmt();		
    			mTCSText = "";
            	goToLayout(R.layout.iab_layout_shenzhoufu_warning);
    		break;
			case IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW:
    			m_bIsSingleBill=false;
    			mGamePrice = mServerInfo.getItemPriceFmt();		
    			mTCSText = "";
            	goToLayout(R.layout.iab_layout_shenzhoufu_enter_data);
    		break;
			case IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED:
    			goToLayout(R.layout.iab_layout_shenzhoufu_transaction_failed);
        	break;
			#endif
			
    		case IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN:
    			goToLayout(R.layout.iab_layout_login);
    		break;
    		
    		case IAB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT:
    			//goToLayout(R.layout.iab_layout_pay_wo_account);
    		break;
    		
    		case IAB_STATE_CC_TRANSACTION_FAILED:
    			goToLayout(R.layout.iab_layout_cc_transaction_failed);
        	break;
    		
    		case IAB_STATE_CC_TRANSACTION_SUCCESS:
				Bundle bundle = new Bundle();
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
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
				ERR(TAG,"GOING TO FINALIZE!!!!!!!!");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "CC TANSACTION SUCCES - GOING TO FINALIZE");
				mState = IAB_STATE_FINALIZE;
        	break;
    		
    		case IAB_STATE_CC_PLEASE_WAIT_PURCHASE_PROGRESS:
    			goToLayout(R.layout.iab_layout_cc_please_wait_purchase_progress);
        	break;
    		
    		case IAB_STATE_CREATE_NEW_ACCOUNT_EMAIL_EXISTS:
    			goToLayout(R.layout.iab_layout_create_new_account_email_exists);
            break;
        		
        	case IAB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA:
        		goToLayout(R.layout.iab_layout_create_new_account_wrong_data);
            break;
                
        	case IAB_STATE_CREDIT_CARD_EXPIRED:
        		goToLayout(R.layout.iab_layout_credit_card_expired);
            break;
        	
        	case IAB_STATE_WAP_BILLING:
        	case IAB_STATE_PAYPAL:
        	    mSubstate = SUBSTATE_INIT;
//         		goToLayout(R.layout.iab_layout_wap_billing);        		
        	break;
        	
            case IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD:
            	goToLayout(R.layout.iab_layout_login_wrong_email_password);
            break;
            
            case IAB_STATE_CC_FORGOT_PASSWORD:
            break;
            case IAB_STATE_PAY_WO_ACCOUNT:
            case IAB_STATE_CC_LOGIN_BUY_NOW:
            case IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW:
            case IAB_STATE_CC_USERBILL_BUY_NOW:
            case IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW:
            	goToLayout(R.layout.iab_layout_cc_please_wait_purchase_progress);
            break;
            case IAB_STATE_NO_DATA_CONNECTION_DETECTED:
            	//goToLayout(R.layout.iab_layout_no_data_connection_detected);
			break;
            case IAB_STATE_NO_PROFILE_DETECTED:
            	//goToLayout(R.layout.iab_layout_no_profile_detected);
            break;
    		default:
    		break;
    	}
		
		
		if (	(mState !=IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST)
			  &&(mState !=IAB_STATE_PAY_WO_ACCOUNT_REQUEST)
			  &&(mState !=IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST)
			  &&(mState !=IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST)
			  &&(mState !=IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST)
			  &&(mState !=IAB_STATE_CC_LOGIN_BUY_NOW_REQUEST)
			  &&(mState !=IAB_STATE_CREDIT_CARD_EXPIRED)
			  &&(mState !=IAB_STATE_CC_FORGOT_PASSWORD_RESULT)
			  &&(mState !=IAB_STATE_CC_FORGOT_PASSWORD_REQUEST)
			  #if SHENZHOUFU_STORE
			  &&(mState !=IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_REQUEST)
			  #endif
			  )
			{
		
				if (state != mState)
					mPrevState = mState;
			}
		
		
		if (		(state ==IAB_STATE_PAY_WO_ACCOUNT_REQUEST)
				  ||(state ==IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST)
				  ||(state ==IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST)
				  ||(state ==IAB_STATE_CC_LOGIN_BUY_NOW_REQUEST)
				  ||(state ==IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST))
		{
			goToLayout(R.layout.iab_layout_cc_please_wait_purchase_progress);
		}
		#if SHENZHOUFU_STORE
		if (state ==IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_REQUEST)
		{
			goToLayout(R.layout.iab_layout_shenzhoufu_please_wait_purchase_progress);
		}
		#endif
		//Screen for SMS and HTTP Billing
		if(state ==IAB_STATE_PLEASE_WAIT_PURCHASE_PROGRESS_REQUEST)
		{
			m_timeOut = System.currentTimeMillis();
			//goToLayout(R.layout.iab_layout_please_wait_purchase_progress);
		}

		if(mState == IAB_STATE_FINALIZE)
			return;
		
		mState = state;
		mSubstate = SUBSTATE_INIT;
		
		switch(mState)
    	{
    		case IAB_STATE_INITIALIZE:
    			m_timeOut = System.currentTimeMillis();
    			//goToLayout(R.layout.iab_layout_please_wait_purchase_progress);
    		break;
    		
    		case IAB_STATE_GAME_PURCHASED:
    			//goToLayout(R.layout.iab_layout_thanks_for_the_purchase);
    		break;
    		case IAB_STATE_READ_GAME_CODE:
    			//goToLayout(R.layout.iab_layout_enter_gamecode);
    		break;
    	}
    }
	
	private void ShowIncorrectFormData()
	{              
		if(!mUserInfo.isValidCardHolder() && IsVisible(R.id.lblName))
			setTextViewNewText( R.id.lblName, R.string.IAB_CC_NAME, Color.RED);
		if(!mUserInfo.isValidCardNumber() && IsVisible(R.id.lblCardNumber))
			setTextViewNewText( R.id.lblCardNumber, R.string.IAB_CC_CARD_NUMBER, Color.RED);
		if(!mUserInfo.isValidExpirationDate() && IsVisible(R.id.lblExpiration))
		{
			setTextViewNewText( R.id.lblExpiration, R.string.IAB_CC_EXPIRATION_DATE, Color.RED);
			setTextViewNewText( R.id.lblExpirationHelp, R.string.IAB_CC_EXPIRATION_DATE_FORMAT, Color.RED);
		}
		if(!mUserInfo.isValidSecurityCode() && IsVisible(R.id.lblSecureCode))
			setTextViewNewText( R.id.lblSecureCode, R.string.IAB_CC_SECURE_CODE, Color.RED);
		if(!mUserInfo.isValidEmail() && IsVisible(R.id.lblEmail))
			setTextViewNewText( R.id.lblEmail, R.string.IAB_CC_EMAIL, Color.RED);
		if(!mUserInfo.isValidPassword() && IsVisible(R.id.lblPassword))
			setTextViewNewText( R.id.lblPassword, R.string.IAB_CC_PASSWORD, Color.RED);
			
		
		#if SHENZHOUFU_STORE
		if(!mUserInfo.isValidSehnzhoufuCardNumber() && IsVisible(R.id.lblShenzhoufuCardNumber))
			setTextViewNewText( R.id.lblShenzhoufuCardNumber, R.string.IAB_SHENZHOUFU_PREPAID_CARD_NUMBER, Color.RED);
		if(!mUserInfo.isValidShenzhoufuCardPassword() && IsVisible(R.id.lblCardPassword))
			setTextViewNewText( R.id.lblCardPassword, R.string.IAB_SHENZHOUFU_PREPAID_CARD_PASSWORD, Color.RED);
		#endif
			
	}
	private void ResetIncorrectDataLabel()
	{
		if(IsVisible(R.id.lblName))
			setTextViewNewText( R.id.lblName, R.string.IAB_CC_NAME, Color.BLACK);
		if(IsVisible(R.id.lblCardNumber))
			setTextViewNewText( R.id.lblCardNumber, R.string.IAB_CC_CARD_NUMBER, Color.BLACK);
		if(IsVisible(R.id.lblExpiration))
		{
			setTextViewNewText( R.id.lblExpiration, R.string.IAB_CC_EXPIRATION_DATE, Color.BLACK);
			setTextViewNewText( R.id.lblExpirationHelp, R.string.IAB_CC_EXPIRATION_DATE_FORMAT, Color.BLACK);
		}
		if(IsVisible(R.id.lblSecureCode))
			setTextViewNewText( R.id.lblSecureCode, R.string.IAB_CC_SECURE_CODE, Color.BLACK);
		if(IsVisible(R.id.lblEmail))
			setTextViewNewText( R.id.lblEmail, R.string.IAB_CC_EMAIL, Color.BLACK);
		if(IsVisible(R.id.lblPassword))
			setTextViewNewText( R.id.lblPassword, R.string.IAB_CC_PASSWORD, Color.BLACK);
		
		#if SHENZHOUFU_STORE
		if(IsVisible(R.id.lblShenzhoufuCardNumber))
			setTextViewNewText( R.id.lblShenzhoufuCardNumber, R.string.IAB_SHENZHOUFU_PREPAID_CARD_NUMBER, Color.BLACK);
		if(IsVisible(R.id.lblCardPassword))
			setTextViewNewText( R.id.lblCardPassword, R.string.IAB_SHENZHOUFU_PREPAID_CARD_PASSWORD, Color.BLACK);
		#endif
	}
	
	ImageView ivEmailIcon = null;
	private boolean addNewCCToUser = false;
	private boolean userAlreadyExist = false;
	private boolean isValidatingEmail = false;
	private String mCheckedEmail = null;
	protected OnFocusChangeListener onEmailFocusChange = new OnFocusChangeListener()
	{
		public void onFocusChange (View v, boolean hasFocus)
		{
			EditText tv = (EditText) v;
			String email = tv.getText().toString();
			JDUMP(TAG, hasFocus);
			INFO(TAG, "onEmailFocusChange email "+email);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onEmailFocusChange - hasFocus:"+hasFocus);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onEmailFocusChange email - " + email);
			
			if (!hasFocus && !isValidatingEmail)
			{
				checkIfEmailExist(email);
			}
		}
	};
	
	OnEditorActionListener onEmailEditorchange = new OnEditorActionListener()
	{
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
			JDUMP(TAG, actionId);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onEmailEditorchange actionId - " + actionId);
            if(	actionId == EditorInfo.IME_ACTION_NEXT ||
				actionId == EditorInfo.IME_ACTION_DONE	|| 
				actionId == EditorInfo.IME_ACTION_GO	&& 
				!isValidatingEmail
				) 
			{
				isValidatingEmail = true;
				EditText tv = (EditText) v;
				String email = tv.getText().toString();
				JDUMP(TAG, email);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onEmailEditorchange email - " + email);
				checkIfEmailExist(email);
            }
            return false;
        }
    };
	
	public void checkIfEmailExist(final String email)
	{
		if (mUserInfo == null)
		{
			mUserInfo = new UserInfo();
			mCheckedEmail = null;
		}	
		if (mCheckedEmail != null && email.equals(mCheckedEmail))
		{
			return;
		}
		mUserInfo.setEmail(email);
		addNewCCToUser = false;
		userAlreadyExist = false;
		if (mUserInfo.isValidEmail())
		{
			new Thread(
				new Runnable() {
					public void run() {
						try 
						{	
							
							XPlayer mXplayer = new XPlayer(new Device(mServerInfo));
							mXplayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
							mXplayer.sendEmailExistRequest(email);
							
							long time = 0;
							while (!mXplayer.handleEmailExistRequest() && mState != IAB_STATE_FINALIZE)
							{
								try {
									Thread.sleep(50);
								} catch (Exception exc) {}
								
								if((System.currentTimeMillis() - time) > 1500)
								{
									time = System.currentTimeMillis();
								}
							}
							if (mState == IAB_STATE_FINALIZE)
								return;
							mCheckedEmail = email;
							Runnable newIcon = null;
							isValidatingEmail = false;
							if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
							{
								newIcon = new Runnable()
								{
									public void run()
									{
										ivEmailIcon.setImageResource(R.drawable.iab_mail_icon_ok);
										ivEmailIcon.setVisibility(ImageView.VISIBLE);
									}
								}; 
							}
							else if (XPlayer.getLastErrorCode() == XPlayer.CC_EMAIL_ALREADY_EXIST_WITH_NO_CC)
							{
								INFO(TAG, "Mail Exist But No CC");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Mail Exist But No CC");
								addNewCCToUser = true;
								newIcon = new Runnable()
								{
									public void run()
									{
										ivEmailIcon.setImageResource(R.drawable.iab_mail_icon_ok);
										ivEmailIcon.setVisibility(ImageView.VISIBLE);
									}
								};
							}
							else if (XPlayer.getLastErrorCode() == XPlayer.CC_EMAIL_ALREADY_EXIST_WITH_CC)
							{
								INFO(TAG, "Mail Exist with CC");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Mail Exist with CC");
								userAlreadyExist = true;
								newIcon = new Runnable()
								{
									public void run()
									{
										SaveFormData(mCurrentLayout);
										setState(IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
										
										android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
										imm.hideSoftInputFromWindow(null,0); 
										//ivEmailIcon.setImageResource(R.drawable.iab_mail_icon_alert);
										//ivEmailIcon.setVisibility(ImageView.VISIBLE);
									}
								};
							}
							runOnUiThread(newIcon);
						} catch (Exception e) {
							DBG_EXCEPTION(e);
						}
					}
				}
			#if !RELEASE_VERSION
			,"Thread-EmailExist"
			#endif
				).start();
		}else {
			mCheckedEmail = null;
			runOnUiThread(new Runnable()
			{
				public void run()
				{
					ivEmailIcon.setVisibility(ImageView.GONE);
				}
			});
		}
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
            	case R.id.bt_ly_login_wrong_email_password_buy_now:
            		SaveFormData(mCurrentLayout);
            		ResetIncorrectDataLabel();
            		if (mUserInfo.isValidEmail() && mUserInfo.isValidPassword())
            			setState(IAB_STATE_CC_LOGIN_WRONG_EMAIL_PASSWORD_BUY_NOW_REQUEST);
            		else
            			ShowIncorrectFormData();
            	break;
            	
            	            	
            	case R.id.bt_ly_login_buy_now:
                	SaveFormData(mCurrentLayout);
                	ResetIncorrectDataLabel();
                	if (mUserInfo.isValidEmail() && mUserInfo.isValidPassword())
                		//setState(IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST);
                		setState(IAB_STATE_CC_LOGIN_BUY_NOW_REQUEST);
						
                	else
                		ShowIncorrectFormData();
            	break;
            	
                
                //Terms and Conditions Buttons
                case R.id.bt_ly_create_new_account_tcs:
                case R.id.bt_ly_create_new_account_wrong_data_tcs:
                case R.id.bt_ly_credit_card_expired_tcs:
                //case R.id.bt_ly_get_full_version_question_tcs:
                case R.id.bt_ly_login_tcs:
                case R.id.bt_ly_login_wrong_email_password_tcs:
                //case R.id.bt_ly_pay_wo_account_tcs:
                case R.id.bt_ly_forgot_password_tcs:
                	goToLayout(R.layout.iab_layout_tcs);
                break;
                case R.id.bt_ly_tcs_back:
                	goToLayout(mPrevLayout);
                break;

                //case R.id.bt_ly_transaction_failed_ccard:
                //case R.id.bt_ly_enter_unlock_code_failed_ccard:
                case R.id.bt_ly_create_new_account_email_exists_create_account:
                	setState(IAB_STATE_CC_CREATE_ACCOUNT);
                break;
                
				//Inform to the game that the purchase has been canceled
				case R.id.bt_ly_create_new_account_exit:
                case R.id.bt_ly_create_new_account_email_exists_exit:
                case R.id.bt_ly_create_new_account_wrong_data_exit:
                case R.id.bt_ly_credit_card_expired_exit:
				case R.id.bt_ly_wap_billing_exit:
				case R.id.bt_ly_login_exit:
                case R.id.bt_ly_login_wrong_email_password_exit:
				case R.id.bt_ly_forgot_password_exit:
				case R.id.bt_ly_tcs_exit:
				#if SHENZHOUFU_STORE
				case R.id.bt_ly_shenzhoufu_warning_back:
				case R.id.bt_ly_shenzhoufu_enter_data_exit:
				case R.id.bt_ly_shenzhoufu_transaction_failed_ok:
				#endif
			    //case R.id.bt_ly_transaction_failed_exit:
				//case R.id.bt_ly_cc_transaction_failed_no:

				Bundle bundle = new Bundle();
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
												
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

				if (buttonId == R.id.bt_ly_wap_billing_exit) {
					int error = (GlShopErrorCodes.WapOtherErrorCodes.get(GlShopErrorCodes.WAP_OTHER_ERROR_USER_CANCEL)).intValue();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), error);
				}

				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
				#if SHENZHOUFU_STORE
				if( buttonId == R.id.bt_ly_shenzhoufu_transaction_failed_ok && shenzhoufuErrorString.equals(getString(R.string.IAB_PURCHASE_ITEM_FAILURE_VERIFICATION_FAILED)))
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_PENDING);
				else
				#endif
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
					
					
				//The Play Buttons :P
                //case R.id.bt_ly_cc_thanks_for_the_purchase_play_game:
                //case R.id.bt_ly_thanks_for_the_purchase_play:
                //Exit (X) Buttons                	
                //case R.id.bt_ly_get_full_version_question_cancel:
                //case R.id.bt_ly_get_full_version_question_exit:
                //case R.id.bt_ly_thanks_for_the_purchase_exit:
                //case R.id.bt_ly_cc_thanks_for_the_purchase_exit:
                /*case R.id.bt_ly_enter_unlock_code_exit:
                case R.id.bt_ly_enter_unlock_code_failed_exit:
                case R.id.bt_ly_enter_unlock_code_success_play:
                case R.id.bt_ly_enter_unlock_code_success_exit:
                case R.id.bt_ly_pay_wo_account_exit:
                case R.id.bt_ly_no_data_connection_detected_exit:
                case R.id.bt_ly_no_data_connection_detected_cancel:*/
                	setState(IAB_STATE_FINALIZE);
                break;
				#if SHENZHOUFU_STORE
				case R.id.bt_ly_shenzhoufu_warning_buy:
                	setState(IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW);
                break;
				case R.id.bt_ly_shenzhoufu_enter_data_continue:
					SaveFormData(mCurrentLayout);
					ResetIncorrectDataLabel();
            		if (mUserInfo.isValidSehnzhoufuCardNumber() && mUserInfo.isValidShenzhoufuCardPassword())
					{
						boolean checkBoxChecked = ((CheckBox) findViewById(R.id.cbSaveData)).isChecked();
						if(checkBoxChecked)
							SUtils.getLManager().SecureShenzhoufuUserValues(mUserInfo);
						else
							SUtils.getLManager().ClearShenzhoufuUserValues();
						AlertDialog alert = mShenzhoufuPurchaseDialog.create();
						alert.show();
					}
            		else
            			ShowIncorrectFormData();
                break;
				#endif
				#if SHENZHOUFU_STORE
				case R.id.bt_ly_shenzhoufu_transaction_failed_no:
				// case R.id.bt_ly_shenzhoufu_transaction_failed_ok:
				#endif
				case R.id.bt_ly_cc_transaction_failed_no:
				setState(IAB_STATE_FINALIZE);
				#if USE_PHD_PSMS_BILL_FLOW
				Bundle bundleCC = new Bundle();
				bundleCC.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
									
				bundleCC.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
				bundleCC.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
				bundleCC.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
				bundleCC.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
									
				bundleCC.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
				bundleCC.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);
										
										
				try{
					Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
					Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
					bundleCC = (Bundle)mMethod.invoke(null, (new Object[]{bundleCC}));
				}
				catch(Exception ex )
				{
					ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
				}
				#else
				((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
						{
						String message;
						public void run ()
						{
							try
							{
								CustomizeDialog customizeDialog = new CustomizeDialog(SUtils.getContext(),1);
								customizeDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
								{
									public void onCancel(DialogInterface dialog)
									{
										Bundle bundle = new Bundle();
										bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
															
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
										bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
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
								customizeDialog.show();
							}
							catch (Exception e)
							{
								DBG_EXCEPTION(e);
							}
						}
						});
				#endif
				break;
				
				
				/*bundle = new Bundle();
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
				}*/					
					//setState(IAB_STATE_FINALIZE);
					//mHelperDevice.showDialog(GLOFTHelper.DIALOG_SELECT_BILLING_METHOD, GLOFTHelper.GetItemId());
               // break;
                
                //Retry Purchase Button
                case R.id.bt_ly_cc_transaction_failed_yes:
				#if SHENZHOUFU_STORE
                case R.id.bt_ly_shenzhoufu_transaction_failed_yes:
				#endif
                	setState(mPrevState);
                break;
                
                //Back Buttons for all states
                case R.id.bt_ly_create_new_account_email_exists_back:
                case R.id.bt_ly_credit_card_expired_back:
                case R.id.bt_ly_login_back:
                case R.id.bt_ly_login_wrong_email_password_back:
                //case R.id.bt_ly_pay_wo_account_back:
                case R.id.bt_ly_forgot_password_back:
                	handleBack(buttonId);
                break;

                //Create new Account Buttons
                case R.id.bt_ly_create_new_account_buy_now:
                case R.id.bt_ly_create_new_account_wrong_data_buy_now:
                	SaveFormData(mCurrentLayout);
                	ResetIncorrectDataLabel();
                	if (mUserInfo.isValidFormInfo())
                		setState(IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST);
                	else
                		ShowIncorrectFormData();
                	break;
                
                case R.id.bt_ly_credit_card_expired_buy_now:
                	SaveFormData(mCurrentLayout);
                	ResetIncorrectDataLabel();
                	if(m_bIsSingleBill)
                	{
                		if (mUserInfo.isValidEmail() && mUserInfo.isValidCreditCard())
                			setState(IAB_STATE_PAY_WO_ACCOUNT_REQUEST);
                		else
                			ShowIncorrectFormData();
                	}
                	else
                	{
                		if (mUserInfo.isValidFormInfo())
                			setState(IAB_STATE_CREATE_NEW_ACCOUNT_BUY_NOW_REQUEST);
                		else
                			ShowIncorrectFormData();
                	}
                	break;
                	
                	
                case R.id.bt_ly_create_new_account_login:
                case R.id.bt_ly_create_new_account_email_exists_login:	
                	SaveFormData(mCurrentLayout);
                	setState(IAB_STATE_CREATE_NEW_ACCOUNT_LOGIN);
                break;
                
                /*case R.id.bt_ly_create_new_account_pay_wo_account:
                	setState(IAB_STATE_CREATE_NEW_ACCOUNT_PAY_WO_ACCOUNT);
                break;*/
                case R.id.bt_ly_login_forgot_password:
                case R.id.bt_ly_login_wrong_email_password_forgot_password:
                	SaveFormData(mCurrentLayout);
                	goToLayout(R.layout.iab_layout_forgot_password);
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
	                	setState(IAB_STATE_CC_FORGOT_PASSWORD_REQUEST);
                	}
				break;
                /*case R.id.bt_ly_no_data_connection_detected_retry:
                	try
                	{
                		mvalue = mServerInfo.getBillingType().equals("WAP");
                	}catch(Exception ex)
                	{
                		mvalue = false;
                	}
                	if(mvalue)
    				{
    					setState(IAB_STATE_PREPRARE_WAP_DATA_CONNECTION);
    				}
                	else
                	{
                		setState(IAB_STATE_INITIALIZE);
                	}
                break;*/
                
                default:                	
                    break;
            }
        }

    };
	 

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		DBG(TAG,"InAppBillingActivity onCreate");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "InAppBillingActivity onCreate");

		super.onCreate(savedInstanceState);
	
		DBG(TAG,"MANUFACTURER: "+android.os.Build.MANUFACTURER);
		DBG(TAG,"MODEL: "+android.os.Build.MODEL);
		DBG(TAG,"PRODUCT: "+android.os.Build.PRODUCT);
		DBG(TAG,"CPU_ABI: "+android.os.Build.CPU_ABI);
		DBG(TAG,"TAGS: "+android.os.Build.TAGS);

		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "MANUFACTURER: "+android.os.Build.MANUFACTURER);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "MODEL: "+android.os.Build.MODEL);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "PRODUCT: "+android.os.Build.PRODUCT);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "CPU_ABI: "+android.os.Build.CPU_ABI);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "TAGS: "+android.os.Build.TAGS);
		
		int IAB_CONTINUE = R.string.IAB_CONTINUE;
		int IAB_SKB_CONTINUE = R.string.IAB_SKB_CONTINUE;
		if ( (mDevice.getSimOperatorName()!= null && mDevice.getSimOperatorName().toLowerCase().contains("docomo")) ||
		(mDevice.getNetworkOperatorName()!= null && mDevice.getNetworkOperatorName().toLowerCase().contains("docomo")) ){
			//special strings for DOCOMO
			IAB_CONTINUE = R.string.IAB_DOCOMO_CONTINUE;
			IAB_SKB_CONTINUE = R.string.IAB_SKB_OK;
		}
	
		mPurchaseDialog = new AlertDialog.Builder(this);
		mPurchaseDialog.setMessage(getString(IAB_CONTINUE))
		.setCancelable(false)
		.setPositiveButton(getString(IAB_SKB_CONTINUE), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				GLOFTHelper.setWAPID(GetWAPPurchaseID());
				GLOFTHelper.setWAPTxID(GetWAPTransactionID());
				GLOFTHelper.setWAPErrorBundle(GetWapErrorBundle());
				GLOFTHelper.GetInstance().ValidateWAPPurchase();
				setState(IAB_STATE_FINALIZE);
			}
		}
		);
		#if SHENZHOUFU_STORE
		mShenzhoufuPurchaseDialog = new AlertDialog.Builder(this);
		mShenzhoufuPurchaseDialog.setMessage(getString(R.string.IAB_SHENZHOUFU_LAYOUT_DIALOG_MESSAGE))
		.setTitle(getString(R.string.IAB_SHENZHOUFU_LAYOUT_DIALOG_TITLE))
		.setCancelable(false)
		.setPositiveButton(getString(IAB_SKB_CONTINUE), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				setState(IAB_STATE_SHENZHOUFU_LOGIN_BUY_NOW_REQUEST);
			}
		})
		.setNegativeButton(getString(R.string.IAB_SKB_CANCEL), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
			}
		});
		#endif
		
		mDialog = new AlertDialog.Builder(this);
		
		LowProfileListener.ActivateImmersiveMode(this);
    }

    public boolean onKeyDown(final int keyCode, KeyEvent event)
    {
		LowProfileListener.onKeyDown(this, keyCode);
        return super.onKeyDown(keyCode,event);
    }
	
    private Bundle GetWapErrorBundle()
    {
    	Bundle bundle = null;
    	String urlView = webView.getUrl();
    	if((urlView.indexOf("&error=") > 0 || urlView.indexOf("?error=") > 0 ) && urlView.contains("v=1.2"))
    	{
    		bundle = mWapErrBundle;
    	}

    	return bundle;
    }

    
	private String GetWAPPurchaseID()
	{
		DBG(TAG,"URL="+webView.getUrl());
		String purchaseID = webView.getUrl().substring(webView.getUrl().indexOf("id=")+3,webView.getUrl().length());
		if(purchaseID.contains("&"))
		{
			purchaseID = purchaseID.substring(0,purchaseID.indexOf("&"));
		}
		DBG(TAG,"PURCHASE ID="+purchaseID);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "URL="+webView.getUrl());
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "ID="+purchaseID);
		return (purchaseID);
	}
	
	private String GetWAPTransactionID()
	{
		DBG(TAG,"URL="+webView.getUrl());
		String transactionID = "";
		if(webView.getUrl().contains("trxid="))
		{
			transactionID = webView.getUrl().substring(webView.getUrl().indexOf("trxid=")+6,webView.getUrl().length());
			if(transactionID.contains("&"))
			{
				transactionID = transactionID.substring(0,transactionID.indexOf("&"));
			}
		}
		DBG(TAG,"TRANSACTION ID="+transactionID);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "TRXID="+transactionID);
		return (transactionID);
	}
	
	private void InitDeviceInfo()
    {
		try{
		mServerInfo = GLOFTHelper.getServerInfo();
		mDevice = GLOFTHelper.GetDevice();
		mHelperDevice = new GLOFTHelper(mServerInfo);
		}catch(Exception EX){finish();}
    }

	@Override
	protected void onStart() {
		DBG(TAG,"onStart");
		DBG(TAG,"mAndroidBStarted: " + mAndroidBStarted);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onStart");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "mAndroidBStarted: " + mAndroidBStarted);
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		//APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onStartSession(this);
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
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onDestroy");
		//SUtils.release();
		mHelperDevice = null;
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		DBG(TAG,"onPause");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		DBG(TAG,"onResume");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onResume");
		super.onResume();
	}

	@Override
    protected void onStop() {
		DBG(TAG,"onStop");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onStop");
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		//APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onEndSession(this);
	#endif
    }

	@Override
	protected void onRestart()
	{
		DBG(TAG,"onRestart");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onRestart");
		super.onRestart();
		
		/*if( (mCurrentLayout == R.layout.iab_layout_enter_unlock_code)
		  //||(mCurrentLayout == R.layout.iab_layout_get_full_version_question)
		  //||(mCurrentLayout == R.layout.iab_layout_no_data_connection_detected)
		  //||(mCurrentLayout == R.layout.iab_layout_no_profile_detected)
		  //||(mCurrentLayout == R.layout.iab_layout_please_wait_purchase_progress)
		  //||(mCurrentLayout == R.layout.iab_layout_thanks_for_the_purchase)
		  ||(mCurrentLayout == R.layout.iab_layout_transaction_failed))
		  {
			DBG(TAG,"RESTARTING LAYOUT: "+GetLayoutName(mCurrentLayout));
			goToLayout(mCurrentLayout);
		  }*/
	}
	
	public boolean mhasFocus = false;
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus)
			LowProfileListener.ActivateImmersiveMode(this);
		mhasFocus = hasFocus;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	mCurrentOrientation = Configuration.ORIENTATION_LANDSCAPE;
	        DBG(TAG,"onConfigurationChanged Landscape "+ newConfig.orientation);
	        LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onConfigurationChanged Landscape "+ newConfig.orientation);
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	mCurrentOrientation = Configuration.ORIENTATION_PORTRAIT;
	    	DBG(TAG,"onConfigurationChanged portrait"+ newConfig.orientation);
	    	LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onConfigurationChanged portrait"+ newConfig.orientation);
	    }
	    else 
	    {
	    	mCurrentOrientation = newConfig.orientation;
	    	DBG(TAG,"onConfigurationChanged user"+ newConfig.orientation);
	    	LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onConfigurationChanged user"+ newConfig.orientation);
	    }
	    if (mSavedLayoutToDisplay != 0)
	    	goToLayout(mSavedLayoutToDisplay);
	}
	
	protected boolean canGoBack()
	{
		if( (mPrevState!= IAB_STATE_NO_DATA_CONNECTION_DETECTED)
		&& 	(mState !=IAB_STATE_NO_DATA_CONNECTION_DETECTED)
		&& 	(mState !=IAB_STATE_NO_PROFILE_DETECTED)
		&& 	(mState !=IAB_STATE_PB_TRANSACTION_SUCCESS)
		&& 	(mState !=IAB_STATE_CC_TRANSACTION_SUCCESS)
		&& 	(mState !=IAB_STATE_INITIALIZE)
		&& 	(mState !=IAB_STATE_GAME_PURCHASED)
		//&& 	(mState !=IAB_STATE_CC_CREATE_ACCOUNT)
		|| 	((mCurrentLayout == R.layout.iab_layout_tcs)))
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
		
		case R.layout.iab_layout_create_new_account_portrait:
			return "iab_layout_create_new_account_portrait";
		#if SHENZHOUFU_STORE
		case R.layout.iab_layout_shenzhoufu_warning:
			return "iab_layout_shenzhoufu_warning";
				
		case R.layout.iab_layout_shenzhoufu_enter_data:
			return "iab_layout_shenzhoufu_enter_data";
			
		case R.layout.iab_layout_shenzhoufu_please_wait_purchase_progress:
			return "iab_layout_shenzhoufu_please_wait_purchase_progress";

		case R.layout.iab_layout_shenzhoufu_transaction_failed:
			return "iab_layout_shenzhoufu_transaction_failed";
		#endif
		/*case R.layout.iab_layout_get_full_version_question:
			return "iab_layout_get_full_version_question";
		
		case R.layout.iab_layout_please_wait_purchase_progress:
			return "iab_layout_please_wait_purchase_progress";*/
		
		//case R.layout.iab_layout_thanks_for_the_purchase:
			//return "iab_layout_thanks_for_the_purchase";
		
		/*case R.layout.iab_layout_transaction_failed:
			return "iab_layout_transaction_failed";*/
		
		case R.layout.iab_layout_cc_please_wait_purchase_progress:
			return "iab_layout_cc_please_wait_purchase_progress";
			
		/*case R.layout.iab_layout_cc_thanks_for_the_purchase:
			return "iab_layout_cc_thanks_for_the_purchase";*/
			
		case R.layout.iab_layout_cc_transaction_failed:
			return "iab_layout_cc_transaction_failed";
			
		case R.layout.iab_layout_create_new_account_email_exists:
			return "iab_layout_create_new_account_email_exists";
			
		case R.layout.iab_layout_create_new_account_wrong_data:
			return "iab_layout_create_new_account_wrong_data";
			
		case R.layout.iab_layout_credit_card_expired:
			return "iab_layout_credit_card_expired";
			
		case R.layout.iab_layout_login:
			return "iab_layout_login";
			
		case R.layout.iab_layout_login_wrong_email_password:
			return "iab_layout_login_wrong_email_password";
		
		//case R.layout.iab_layout_pay_wo_account:
			//return "iab_layout_pay_wo_account";
			
		case R.layout.iab_layout_tcs:
			return "iab_layout_tcs";
			
		/*case R.layout.iab_layout_enter_unlock_code:
			return "iab_layout_enter_unlock_code";*/
			
		case R.layout.iab_layout_forgot_password:
			return "iab_layout_forgot_password";
			
		/*case R.layout.iab_layout_no_data_connection_detected:
			return "iab_layout_no_data_connection_detected";
			
		case R.layout.iab_layout_no_profile_detected:
			return "iab_layout_no_profile_detected";*/
			
		//case R.layout.main:
			//return "main";
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
    	LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Billing: goToLayout("+GetLayoutName(layoutId)+")");
    	setButtonList(new ArrayList<Button>());
        getButtonList().clear();
		final InAppBillingActivity THIZ = this;
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{
	    		try 
	    		{       
					if (android.os.Build.VERSION.SDK_INT >= 11/*android.os.Build.VERSION_CODES.HONEYCOMB*/) 
					{
						/*mSavedLayoutToDisplay = 0;
						if (
							(layoutId == R.layout.iab_layout_create_new_account_portrait && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
						  ||(layoutId == R.layout.iab_layout_create_new_account_portrait && mCurrentOrientation == ActivityInfo.SCREEN_ORIENTATION_USER)
							)
						{
							mSavedLayoutToDisplay = layoutId; 
							Thread.sleep(10);
							return;
						}*/
						Thread.sleep(2000);
					}
					setContentView(layoutId);
					if (mCurrentLayout != R.layout.iab_layout_tcs)
						mPrevLayout = mCurrentLayout;
					mCurrentLayout = layoutId;
	    			
	    			TextView lbl;
	    			EditText text;
	    			Button ccbutton;
	    			
	    			/*if (mCurrentOrientation== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
	    				DBG(TAG,"Changing Layout on landscape mode!!!!!");*/
	    			
	    			switch (layoutId)
	    	        {
	    				/*case R.layout.iab_layout_enter_gamecode:
	    				{
	    					getButtonList().add((Button) findViewById(R.id.bt_ly_enter_gamecode_ok));
	    				}
	    				break;
	    				
	    	        	case R.layout.iab_layout_please_wait_purchase_progress:
	    	        	{
	    	        		if(mState == IAB_STATE_INITIALIZE)
	    	        		{
	    	        			lbl = (TextView) findViewById(R.id.lblMessagePleaseWait);
	    	        			lbl.setText(GRS(IAB_STR_EMPTY));
	    	        		}
	    	        		boolean value;
	                    	try
	                    	{
	                    		value = mServerInfo.getBillingType().equals("WAP");
	                    	}catch(Exception ex)
	                    	{
	                    		value = false;
	                    	}
	    	        		if (value || trickDebugScreen)
	    	        		{
	    	        			lbl = (TextView) findViewById(R.id.lblMessagePleaseWait);
	    	        			lbl.setText(getString(R.string.IAB_WAP_BILLING_VERIFYING));
	    	        		}
	    	        	}
	    	        	break;*/
	    	        	
	    	        	/*case R.layout.iab_layout_get_full_version_question:
	    	            {
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_get_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_get_full_version_question_cancel));
    	            	
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo);
    	    				lbl.setText(getStringFormated(R.string.IAB_GET_FULL_VERSION_QUESTION,"{GAME_NAME}",mGameName));
    	    				
    	    				lbl = (TextView) findViewById(R.id.lblMessageInfo2);
    	    				lbl.setText(getStringFormated(R.string.IAB_GET_FULL_VERSION_CHARGE_APPLY,"{PRICE}",mGamePrice));
    	    				
    	    				Button bt;
	    		    		bt = (Button) findViewById(R.id.bt_ly_get_full_version_question_get_now);
	    	            }
	    	            break;*/
	    	        	/*case R.layout.iab_layout_thanks_for_the_purchase:
	    	            {
	    	            	DBG(TAG,"WE ARE ON THE THANKS FOR PURCHASE LAYOUT");
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_thanks_for_the_purchase_play));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_thanks_for_the_purchase_exit));
	    	            	
	    	            	if(mState == IAB_STATE_GAME_PURCHASED)
	    	            	{
	    	            		DBG(TAG,"Changing messange to the screen for purchased message");
	    	            		lbl = (TextView) findViewById(R.id.lblMessageInfo);
	    	    				lbl.setText(getString(R.string.IAB_ALREADY_PURCHASED));
	    	            	}
	    	            }
	    	            break;*/
	    	            
	    	            /*case R.layout.iab_layout_transaction_failed:
	    	            {
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_retry));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_ccard));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_transaction_failed_exit));
	    	            	if(!Device.USE_CC_PROFILE)
	    	            	{
	    	            		ccbutton = (Button) findViewById(R.id.bt_ly_transaction_failed_ccard);
	    	            		ccbutton.setVisibility(View.INVISIBLE);
	    	            	}
	    	            }
	    	            break;*/
	    	            
	    	            case R.layout.iab_layout_create_new_account_portrait:
	    	            case R.layout.iab_layout_create_new_account:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_login));
	    	            	//getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_pay_wo_account));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_tcs));
	    	            	addDateValidation();
							
							text = (EditText) findViewById(R.id.etEmail);
							text.setOnFocusChangeListener(onEmailFocusChange);
							text.setOnEditorActionListener(onEmailEditorchange);
							
							ivEmailIcon = (ImageView) findViewById(R.id.ivEmailIcon);
	    	            	
							lbl = (TextView) findViewById(R.id.lblDialogTop);
							String str = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_LAYOUT,"{ITEM}",new GLOFTHelper(mServerInfo).GetItemDescription());
							str = getStringFormated(str,"{PRICE}",mServerInfo.getItemPriceFmt());
	    	            	lbl.setText(str);
	    	            	fillBottomMsgInfo();
	    	            break;
						#if SHENZHOUFU_STORE
	    	            case R.layout.iab_layout_shenzhoufu_enter_data:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_shenzhoufu_enter_data_exit));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_shenzhoufu_enter_data_continue));
	    	            	
							lbl = (TextView) findViewById(R.id.lblDialogTop);
							String stra = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_LAYOUT,"{ITEM}",new GLOFTHelper(mServerInfo).GetItemDescription());
							stra = getStringFormated(stra,"{PRICE}",mServerInfo.getItemPriceFmt());
	    	            	lbl.setText(stra);
							
							if(mPrepaidCardType == null)
								 mPrepaidCardType = new String[]{
									getString(R.string.IAB_SHENZHOUFU_LAYOUT_PREPAID_CARD_TYPE_CHINA_MOBILE), 
									getString(R.string.IAB_SHENZHOUFU_LAYOUT_PREPAID_CARD_TYPE_CHINA_UNICOM), 
									getString(R.string.IAB_SHENZHOUFU_LAYOUT_PREPAID_CARD_TYPE_CHINA_TELECOM), 
								};
							sCardType = (Spinner) findViewById(R.id.spCardType);
							adapterType = new ArrayAdapter(SUtils.getContext(),R.layout.iab_layout_shenzhoufu_spinner_style, mPrepaidCardType);
							adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							sCardType.setAdapter(adapterType);
							
							sCardDenom = (Spinner) findViewById(R.id.spCardDenomination);
							adapterDenom = new ArrayAdapter(SUtils.getContext(),R.layout.iab_layout_shenzhoufu_spinner_style, mPrepaidCardMobileDenominations);//mobile as default
							adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							sCardDenom.setAdapter(adapterDenom);
							
							sCardType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
								@Override
								public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
									DBG(TAG,"onItemSelected for card type "+position);
									LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onItemSelected for card type "+position);
									if(position == 0)
										adapterDenom = new ArrayAdapter(SUtils.getContext(),R.layout.iab_layout_shenzhoufu_spinner_style, mPrepaidCardMobileDenominations);//mobile as default
									if(position == 1)
										adapterDenom = new ArrayAdapter(SUtils.getContext(),R.layout.iab_layout_shenzhoufu_spinner_style, mPrepaidCardUnicomDenominations);//mobile as default
									if(position == 2)
										adapterDenom = new ArrayAdapter(SUtils.getContext(),R.layout.iab_layout_shenzhoufu_spinner_style, mPrepaidCardTelecomDenominations);//mobile as default
									adapterDenom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
									sCardDenom.setAdapter(adapterDenom);
									
									String cardDenomination = SUtils.getLManager().GetCardDenomination();
									if(cardDenomination != null && !cardDenomination.equals("null") && !cardDenomination.equals(""))
									{
										int denomPosition = adapterDenom.getPosition(cardDenomination);
										try{
											// sCardDenom.setSelection(Integer.parseInt(cardDenomination),false);
											sCardDenom.setSelection(denomPosition,false);
										}catch(Exception e){sCardDenom.setSelection(0,false);}
									}
								}

								@Override
								public void onNothingSelected(AdapterView<?> parentView) {
								}
							});
	    	            break;

	    	            case R.layout.iab_layout_shenzhoufu_warning:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_shenzhoufu_warning_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_shenzhoufu_warning_buy));
							
							lbl = (TextView) findViewById(R.id.tvShenzhoufuInfo);
							String strShen = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_LAYOUT,"{ITEM}",new GLOFTHelper(mServerInfo).GetItemDescription());
							strShen = getStringFormated(strShen,"{PRICE}",mServerInfo.getItemPriceFmt());
							strShen += "\n\n\n" +getString(R.string.IAB_SHENZHOUFU_LAYOUT_WARNING_MESSAGE);
	    	            	lbl.setText(strShen);
	    	            break;
						#endif
	    	            case R.layout.iab_layout_create_new_account_wrong_data:
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
	    	            case R.layout.iab_layout_create_new_account_email_exists:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_login));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_create_account));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_create_new_account_email_exists_exit));
	    	            	//fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            /*case R.layout.iab_layout_pay_wo_account:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_pay_wo_account_exit));
	    	            	addDateValidation();
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;*/
	    	            case R.layout.iab_layout_login:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_forgot_password));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_exit));
							
							if (userAlreadyExist)
							{
								lbl = (TextView) findViewById(R.id.lblFormTop);
								lbl.setText(getString(R.string.IAB_CC_SHORT_EMAIL_EXIST));
	    	            	}
							
							lbl = (TextView) findViewById(R.id.lblDialogTop);
							
							String str1 = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_LAYOUT,"{ITEM}",new GLOFTHelper(mServerInfo).GetItemDescription());
							str1 = getStringFormated(str1,"{PRICE}",mServerInfo.getItemPriceFmt());
	    	            	lbl.setText(str1);
							//fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.iab_layout_login_wrong_email_password:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_forgot_password));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_login_wrong_email_password_exit));
	    	            	lbl = (TextView) findViewById(R.id.lblDialogTop);
							String str2 = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_LAYOUT,"{ITEM}",new GLOFTHelper(mServerInfo).GetItemDescription());
							str2 = getStringFormated(str2,"{PRICE}",mServerInfo.getItemPriceFmt());
	    	            	lbl.setText(str2);
							//fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.iab_layout_credit_card_expired:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_buy_now));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_credit_card_expired_exit));
	    	            	addDateValidation();
	    	            	//fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            /*case R.layout.iab_layout_cc_thanks_for_the_purchase:
	    	            	//getButtonList().add((Button) findViewById(R.id.bt_ly_cc_thanks_for_the_purchase_play_game));
	    	            	//getButtonList().add((Button) findViewById(R.id.bt_ly_cc_thanks_for_the_purchase_exit));
	    	            	fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;*/
	    	            case R.layout.iab_layout_cc_transaction_failed:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_cc_transaction_failed_yes));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_cc_transaction_failed_no));
	    	            	//fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
						#if SHENZHOUFU_STORE
	    	            case R.layout.iab_layout_shenzhoufu_transaction_failed:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_yes));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_no));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_ok));
							
							if(shenzhoufuErrorString != null && !shenzhoufuErrorString.equals("")){
								if(shenzhoufuErrorString.equals(getString(R.string.IAB_PURCHASE_ITEM_FAILURE_VERIFICATION_FAILED)))
								{
									((TextView) findViewById(R.id.lblFormTop)).setText(shenzhoufuErrorString);
									((TextView) findViewById(R.id.lblFormQuestion)).setText(getString(R.string.IAB_PURCHASE_ITEM_FAILURE_VERIFICATION_INTERRUPTED));
								}
								else
									((TextView) findViewById(R.id.lblFormQuestion)).setText(shenzhoufuErrorString);
								((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_yes)).setVisibility(View.GONE);
								((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_no)).setVisibility(View.GONE);
								((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_ok)).setVisibility(View.VISIBLE);
							}
							else
							{
								((TextView) findViewById(R.id.lblFormQuestion)).setText(getString(R.string.IAB_TRANSACTION_FAILED_RETRY));
								((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_yes)).setVisibility(View.VISIBLE);
								((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_no)).setVisibility(View.VISIBLE);
								((Button) findViewById(R.id.bt_ly_shenzhoufu_transaction_failed_ok)).setVisibility(View.GONE);
							}
	    	            break;
						#endif
	    	            case R.layout.iab_layout_cc_please_wait_purchase_progress:
	    	            	{
	    	            		lbl = (TextView) findViewById(R.id.lblAccountCreated);
	    	            		lbl.setVisibility(View.INVISIBLE);
	    	            	}
	    	            	//fillGameDetails();
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.iab_layout_forgot_password:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_ok));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_tcs));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_forgot_password_exit));
	    	            	//fillGameDetails();
							lbl = (TextView) findViewById(R.id.lblDialogTop);
							String str3 = getStringFormated(R.string.IAB_PURCHASE_ITEM_CONFIRMATION_CC_LAYOUT,"{ITEM}",new GLOFTHelper(mServerInfo).GetItemDescription());
							str3 = getStringFormated(str3,"{PRICE}",mServerInfo.getItemPriceFmt());
	    	            	lbl.setText(str3);
	    	            	fillBottomMsgInfo();
	    	            break;
	    	            case R.layout.iab_layout_tcs:
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_tcs_back));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_tcs_exit));
	    	            	lbl = (TextView) findViewById(R.id.tvTCSInfo);
	    	            	lbl.setText(mTCSText);
	    	            break;
	    	           /* case R.layout.iab_layout_no_data_connection_detected:
	    	            	boolean value;
	                    	try
	                    	{
	                    		value = mServerInfo.getBillingType().equals("WAP");
	                    	}catch(Exception ex)
	                    	{
	                    		value = false;
	                    	}
	    	            	if(value)
	    	            	{
	    	            		lbl = (TextView) findViewById(R.id.lblMessageInfo2);
	    	            		lbl.setText(getString(R.string.IAB_ACTIVATE_CONNECTION));
	    	            	}
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_cancel));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_retry));
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_exit));
	    	            break;
	    	            case R.layout.iab_layout_no_profile_detected:	    	            	
	    	            	getButtonList().add((Button) findViewById(R.id.bt_ly_no_data_connection_detected_exit));
	    	            	lbl = (TextView) findViewById(R.id.lblMessageInfo2);
	    	            	//lbl.setText(mServerInfo.getUnlockProfile().getNoProfileMsg(mDevice.getLocale()));
						break;*/
						
						case R.layout.iab_layout_wap_billing:
							webView = (WebView) findViewById(R.id.webview);
							webView.getSettings().setJavaScriptEnabled(true);
							webView.addJavascriptInterface(new MyJavaScriptInterface(THIZ), "HTMLOUT");
							if(mServerInfo.getBillingType().equals("wap_other") && map != null && map.size() != 0 )
							{
								DBG(TAG,"Loading webview with headers");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Loading webview with headers");
								webView.loadUrl(mServerInfo.getURLbilling()+"?ggi="+GGI+"&gliveid="+InAppBilling.GET_GGLIVE_UID()+"&contentid="+InAppBilling.mItemID+"&verify="+(md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"))+"&d="+SUtils.GetSerialKey()+"&v=1.2", map);
	   						} else if(mServerInfo.getBillingType().equals("wap_paypal") && map != null && map.size() != 0 )
	   						{
								DBG(TAG,"Loading webview wap paypal with headers");
								LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Loading webview wap paypal with headers");
	   							webView.loadUrl(mServerInfo.getURLbilling()+"?ggi="+GGI+"&gliveid="+InAppBilling.GET_GGLIVE_UID()+"&contentid="+InAppBilling.mItemID+"&verify="+(md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"))+"&d="+SUtils.GetSerialKey()+"&v=1.2", map);
	   						}
							else if(map != null && map.size() != 0)
							{
								DBG(TAG,"Loading webview with headers");
								webView.loadUrl(mServerInfo.getURLbilling()+"?ggi="+GGI+"&gliveid="+InAppBilling.GET_GGLIVE_UID()+"&contentid="+InAppBilling.mItemID+"&imei="+mDevice.getIMEI()+
								#if HDIDFV_UPDATE
								"&hdidfv="+mDevice.getHDIDFV()+
								#endif
								"&verify="+(md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"))+"&d="+SUtils.GetSerialKey(), map);
							}
							else
								webView.loadUrl(mServerInfo.getURLbilling()+"?ggi="+GGI+"&gliveid="+InAppBilling.GET_GGLIVE_UID()+"&contentid="+InAppBilling.mItemID+"&imei="+mDevice.getIMEI()+
								#if HDIDFV_UPDATE
								"&hdidfv="+mDevice.getHDIDFV()+
								#endif
								"&verify="+(md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"))+"&d="+SUtils.GetSerialKey());
							
							Button bt_exit = (Button) findViewById(R.id.bt_ly_wap_billing_exit);
							bt_exit.setVisibility(View.VISIBLE);
							DBG(TAG,"MD5 Values = "+(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"));
							DBG(TAG,"MD5 = "+(mServerInfo.getURLbilling()+"?ggi="+GGI+"&gliveid="+InAppBilling.GET_GGLIVE_UID()+"&contentid="+InAppBilling.mItemID+"&imei="+mDevice.getIMEI()+"&verify="+(md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"))));
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "MD5 Values = "+(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"));
							LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "MD5 = "+(mServerInfo.getURLbilling()+"?ggi="+GGI+"&gliveid="+InAppBilling.GET_GGLIVE_UID()+"&contentid="+InAppBilling.mItemID+"&imei="+mDevice.getIMEI()+"&verify="+(md5(GGI+"_"+InAppBilling.GET_GGLIVE_UID()+"_"+InAppBilling.mItemID+"_"+mDevice.getIMEI()+"_gameloft"))));
							webView.setWebViewClient(new MyWebViewClient(THIZ));
							webView.requestFocus(View.FOCUS_DOWN);
							
							Button bt;
							getButtonList().add((Button) findViewById(R.id.bt_ly_wap_billing_exit));
							getButtonList().add((Button) findViewById(R.id.bt_ly_wap_billing_next));

							bt = (Button) findViewById(R.id.bt_ly_wap_billing_next);
							bt.setVisibility(bt.INVISIBLE);
							mb_waitingActivationWord = true;
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
	    	            ERR("InAppBillingActivity",e.getMessage());
	    	        }
	    		} catch (Exception e) {
	    			ERR("InAppBillingActivity",e.getMessage());
	    		}
	    	}

			private void fillBottomMsgInfo() {
				TextView lbl;
				lbl = (TextView) findViewById(R.id.lblBottomMsg);
				lbl.setText(getString(R.string.IAB_CC_GL_PEACE_OF_MIND)+ " "+
						getString(R.string.IAB_CC_GL_PEACE_OF_MIND2)+ " "+
						getString(R.string.IAB_CC_EMAIL_PRIVACY));
			}


			private void fillGameDetails() 
			{
				mGamePrice = mServerInfo.getItemPriceFmt();		
    			mTCSText = mServerInfo.getTNCString();
				
				TextView lbl;
				lbl = (TextView) findViewById(R.id.lblGameName);
				lbl.setText(mServerInfo.getItemAttribute(InAppBilling.mItemID,"name"));
			}
    	});
    }

	public void setWAPNextButtonVisibility()
    {
    	this.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{	
        		webView = (WebView) findViewById(R.id.webview);
				String urlView = webView.getUrl();
        		
        		if((webView.findAll(MAGIC_WORD)!=0 || ( urlView != null && (urlView.indexOf("result=successful&id=") > 0 || urlView.indexOf("&error=") > 0 || urlView.indexOf("?error=") > 0)) ) && !mb_isNextActivated)
        		{
	            	DBG(TAG, "MAGIC_WORD found");
	            	LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "MAGIC_WORD found");
	            	mb_waitingActivationWord = false;
            		mb_isNextActivated = true;

	            	//Reset the current pointer selection
	            	webView.clearHistory();
	            	//webView.clearCache(true);
	            	//webView.setVisibility(WebView.INVISIBLE);
	            	
	            	/*Button bt;
	            	bt = (Button) findViewById(R.id.bt_ly_wap_billing_next);
	            	bt.setVisibility(0);*/
	            	
	        		AlertDialog alert = mPurchaseDialog.create();
	        		//alert.setTitle(mGameName);
	        		//alert.setIcon(R.drawable.icon);
	        		alert.show();
					DBG(TAG, "mPurchaseDialog shown");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "mPurchaseDialog shown");
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
		{
			#if SHENZHOUFU_STORE
			Spinner spinerReader = (Spinner)findViewById(R.id.spCardType);
			if(spinerReader != null)
			{
				String cardType = SUtils.getLManager().GetCardType();
				if(cardType != null && !cardType.equals("null"))
					try{
						spinerReader.setSelection(Integer.parseInt(cardType),false);
					}catch(Exception e){spinerReader.setSelection(0,false);}
			}
			spinerReader = (Spinner)findViewById(R.id.spCardDenomination);
			if(spinerReader != null)
			{
				String cardDenomination = SUtils.getLManager().GetCardDenomination();
				if(cardDenomination != null && !cardDenomination.equals("null") && !cardDenomination.equals(""))
				{
					int denomPosition = adapterDenom.getPosition(cardDenomination);
					try{
						// spinerReader.setSelection(Integer.parseInt(cardDenomination),false);
						spinerReader.setSelection(denomPosition,false);
					}catch(Exception e){spinerReader.setSelection(0,false);}
				}
			}
			EditText textReader = (EditText)findViewById(R.id.etCardNumber);
			if (textReader != null)
			{
				String cardNumber = SUtils.getLManager().GetCardNumber();
				if(cardNumber != null && !cardNumber.equals("null"))
					textReader.setText(cardNumber);
			}
			//password missing
			
			CheckBox checkBox = (CheckBox)findViewById(R.id.cbSaveData);
			if (checkBox != null)
			{
				if(SUtils.getLManager().GetCheckBoxChecked())
					checkBox.setChecked(true);
				else
					checkBox.setChecked(false);
			}
			
			#endif
			return;
		}
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
		
		#if SHENZHOUFU_STORE
		Spinner spinerReader = (Spinner)findViewById(R.id.spCardType);
		if (spinerReader != null && mUserInfo.getCardType() != null)
		{
			try{
				spinerReader.setSelection(Integer.parseInt(mUserInfo.getCardType()),false);
			}catch(Exception e){spinerReader.setSelection(0,false);}
		}
		
		spinerReader = (Spinner)findViewById(R.id.spCardDenomination);
		if (spinerReader != null  && mUserInfo.getCardDenomination() != null)
		{
			int denomPosition = adapterDenom.getPosition(mUserInfo.getCardDenomination());
			try{
				// spinerReader.setSelection(Integer.parseInt(mUserInfo.getCardDenomination()),false);
				spinerReader.setSelection(denomPosition,false);
			}catch(Exception e){spinerReader.setSelection(0,false);}
		}
			
		reader = (EditText)findViewById(R.id.etCardNumber);
		if (reader != null && mUserInfo.getShenzhoufuCardNumber() != null)
			reader.setText(mUserInfo.getShenzhoufuCardNumber());
			
		CheckBox checkBox = (CheckBox)findViewById(R.id.cbSaveData);
		if (checkBox != null)
		{
			if(SUtils.getLManager().GetCheckBoxChecked())
				checkBox.setChecked(true);
			else
				checkBox.setChecked(false);
		}
		#endif
	}
	
	private void SaveFormData(int currentForm)
	{
		if (mUserInfo == null)
			mUserInfo = new UserInfo();
		EditText reader;
		DBG(TAG,"Saving data for: "+GetLayoutName(currentForm));
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Saving data for: "+GetLayoutName(currentForm));
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
			
			/*reader = (EditText)findViewById(R.id.etUnlockCode);
			if (reader != null)
				mPromoCode=reader.getText().toString();*/
		
			#if SHENZHOUFU_STORE
			Spinner spinerReader = (Spinner)findViewById(R.id.spCardType);
			if (spinerReader != null)
				mUserInfo.setCardType(spinerReader.getSelectedItemPosition()+"");
				
			spinerReader = (Spinner)findViewById(R.id.spCardDenomination);
			if (spinerReader != null)
				// mUserInfo.setCardDenomination(spinerReader.getSelectedItemPosition()+"");
				mUserInfo.setCardDenomination(spinerReader.getSelectedItem().toString());
				
			reader = (EditText)findViewById(R.id.etCardNumber);
			if (reader != null)
				mUserInfo.setShenzhoufuCardNumber(reader.getText().toString());
				
			reader = (EditText)findViewById(R.id.etCardPassword);
			if (reader != null)
				mUserInfo.setCardPassword(reader.getText().toString());
			#endif
	}


	
	
	public void updateBillingResult(boolean success, int message)
	{
		DBG(TAG,"updateBillingResult()");
		
		DBG(TAG,"success= "+success+" message"+this.getString(message));
		
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "updateBillingResult()");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "success= "+success+" message"+this.getString(message));
		
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
			
			setState(IAB_STATE_PB_TRANSACTION_SUCCESS);
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
			setState(IAB_STATE_PB_TRANSACTION_FAILED);

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
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "updateCCLogin()");
		
		if(success)
		{	
			switch(message)
			{
				case XPlayer.CC_LOGIN_SUCCESS_USER_EXIST_WITH_CREDIT_CARD:
					DBG(TAG,"CC_LOGIN_SUCCESS_USER_EXIST_WITH_CREDIT_CARD");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "CC_LOGIN_SUCCESS_USER_EXIST_WITH_CREDIT_CARD");
					setState(IAB_STATE_CC_USERBILL_BUY_NOW_REQUEST);
				break;
				
				case XPlayer.CC_LOGIN_SUCCESS_USER_EXIST_BUT_NO_CREDIT_CARD:
					DBG(TAG,"CC_LOGIN_SUCCESS_USER_EXIST_BUT_NO_CREDIT_CARD");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "CC_LOGIN_SUCCESS_USER_EXIST_BUT_NO_CREDIT_CARD");
					setState(IAB_STATE_CREDIT_CARD_EXPIRED);
				break;
				
				default:
					DBG(TAG,"ERROR RESULTANT IS :"+message);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ERROR RESULTANT IS :"+message);
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
			}
		}
		else
		{
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_GENERIC");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_LOGIN_INVALID_USERNAME_OR_PASSWORD:
					DBG(TAG,"CC_ERROR_LOGIN_INVALID_USERNAME_OR_PASSWORD");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_LOGIN_INVALID_USERNAME_OR_PASSWORD");
					setState(IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD);
				break;
				
				case XPlayer.CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE:
					DBG(TAG,"CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE");
					setState(IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD);
				break;
				default:
					DBG(TAG,"Error CCLogin Billing "+message);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error CCLogin Billing "+message);
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
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
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "updateCCUserBill()");
		
		if(success)
		{	
			SUtils.getLManager().SecureUserValues(mUserInfo);
			setState(IAB_STATE_CC_TRANSACTION_SUCCESS);
		}else
		{
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_GENERIC");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_USERNAME:
					DBG(TAG,"CC_ERROR_PBC_INVALID_USERNAME");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_INVALID_USERNAME");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_REFERENCE:
					DBG(TAG,"CC_ERROR_PBC_INVALID_REFERENCE");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_INVALID_REFERENCE");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_USER_NOT_EXIST:
					DBG(TAG,"CC_ERROR_PBC_USER_NOT_EXIST");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_USER_NOT_EXIST");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_USER_WITH_CC_ALREADY_REGISTERED:
					DBG(TAG,"CC_ERROR_PBC_USER_WITH_CC_ALREADY_REGISTERED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_USER_WITH_CC_ALREADY_REGISTERED");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_USER_DONT_HAS_A_CC_REGISTERED:
					DBG(TAG,"CC_ERROR_PBC_USER_DONT_HAS_A_CC_REGISTERED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_USER_DONT_HAS_A_CC_REGISTERED");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_TOO_MANY_CARDS_FOR_USER:
					DBG(TAG,"CC_ERROR_PBC_TOO_MANY_CARDS_FOR_USER");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_TOO_MANY_CARDS_FOR_USER");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_CARD_NUMBER:
					DBG(TAG,"CC_ERROR_PBC_INVALID_CARD_NUMBER");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_INVALID_CARD_NUMBER");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_PURCHASE_FORMAT:
					DBG(TAG,"CC_ERROR_PBC_INVALID_PURCHASE_FORMAT");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_INVALID_PURCHASE_FORMAT");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_PURCHASE_NOT_FOUND:
					DBG(TAG,"CC_ERROR_PBC_PURCHASE_NOT_FOUND");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_PURCHASE_NOT_FOUND");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_PURCHASE_IS_NOT_PAYBOX:
					DBG(TAG,"CC_ERROR_PBC_PURCHASE_IS_NOT_PAYBOX");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_PURCHASE_IS_NOT_PAYBOX");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_PURCHASE_ALREADY_REFUNDED:
					DBG(TAG,"CC_ERROR_PBC_PURCHASE_ALREADY_REFUNDED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_PURCHASE_ALREADY_REFUNDED");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_INVALID_PARAMETER:
					DBG(TAG,"CC_ERROR_PBC_INVALID_PARAMETER");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_INVALID_PARAMETER");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_PBC_TRANSACTION_NOT_ALLOWED:
					DBG(TAG,"CC_ERROR_PBC_TRANSACTION_NOT_ALLOWED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_PBC_TRANSACTION_NOT_ALLOWED");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
			    
				default:
					DBG(TAG,"Error CCUserBill Billing "+message);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error CCUserBill Billing "+message);
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
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
	 * request has been initiated with <code>sendNewUserBillRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCNewUserBill(boolean success, int message)
	{
		DBG(TAG,"updateCCNewUserBill()");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "updateCCNewUserBill()");

		if(success)
		{	
			SUtils.getLManager().SecureNewUserValues(mUserInfo);
			setState(IAB_STATE_CC_TRANSACTION_SUCCESS);
		}else
		{
			//ERR(TAG,"DELETE THIS DEBUG PROCEDURE");
			//SUtils.getLManager().SecureNewUserValues(mUserInfo);
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_GENERIC");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				case XPlayer.CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_NICKNAME_UNAVAILABLE:
					DBG(TAG,"CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED");
					goToLayout(R.layout.iab_layout_create_new_account_email_exists);
				break;
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_EMAIL:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_PASSWORD:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_CARD_NUMBER:
				case XPlayer.CC_ERROR_USER_CREATED_INVALID_CARD_NUMBER:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_CCV_MISSING_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_USER_CREATED_CCV_MISSING_INVALID_EXP_DATE:
					DBG(TAG,"CC_ERROR_WRONG_DATA");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_WRONG_DATA");
					setState(IAB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA);
				break;
				case XPlayer.CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE://User Created Invalid Credit Card
				case XPlayer.CC_ERROR_INVALID_CREDIT_CARD_VALUE:	
				case XPlayer.CC_ERROR_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_INVALID_CCV:	
					DBG(TAG,"CC_ERROR_WRONG_CARD_INFO");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_WRONG_CARD_INFO");
					setState(IAB_STATE_CREDIT_CARD_EXPIRED);
				break;
				default:
					DBG(TAG,"Error CCNewUserBill Billing "+message);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error CCNewUserBill Billing "+message);
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
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
	 * request has been initiated with <code>sendAddCardBillRequest()</code>, this
	 * @param success
	 * @param message
	 */
	@Override 
	public void updateCCAddCardBill(boolean success, int message)
	{
		DBG(TAG,"updateCCAddCardBill()");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "updateCCAddCardBill()");

		if(success)
		{	
			JDUMP(TAG, mUserInfo);
			JDUMP(TAG, mUserInfo.getEmail());
			JDUMP(TAG, mUserInfo.getPassword());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "mUserInfo:"+mUserInfo);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "mUserInfo email:"+mUserInfo.getEmail());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "mUserInfo pass:"+mUserInfo.getPassword());
			SUtils.getLManager().SecureNewUserValues(mUserInfo);
			setState(IAB_STATE_CC_TRANSACTION_SUCCESS);
		}else
		{
			//ERR(TAG,"DELETE THIS DEBUG PROCEDURE");
			//SUtils.getLManager().SecureNewUserValues(mUserInfo);
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_GENERIC");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				case XPlayer.CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_NICKNAME_UNAVAILABLE:
					DBG(TAG,"CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED");
					goToLayout(R.layout.iab_layout_create_new_account_email_exists);
				break;
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_EMAIL:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_PASSWORD:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_INVALID_CARD_NUMBER:
				case XPlayer.CC_ERROR_USER_CREATED_INVALID_CARD_NUMBER:
				case XPlayer.CC_ERROR_USER_NOT_CREATED_CCV_MISSING_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_USER_CREATED_CCV_MISSING_INVALID_EXP_DATE:
					DBG(TAG,"CC_ERROR_WRONG_DATA");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_WRONG_DATA");
					setState(IAB_STATE_CREATE_NEW_ACCOUNT_WRONG_DATA);
				break;
				case XPlayer.CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE://User Created Invalid Credit Card
				case XPlayer.CC_ERROR_INVALID_CREDIT_CARD_VALUE:	
				case XPlayer.CC_ERROR_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_INVALID_CCV:
					DBG(TAG,"CC_ERROR_WRONG_CARD_INFO");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_WRONG_CARD_INFO");
					setState(IAB_STATE_CREDIT_CARD_EXPIRED);
				break;
				case XPlayer.CC_ADDING_CARD_INVALID_PASSWORD:
					DBG(TAG,"IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD");
					setState(IAB_STATE_LOGIN_WRONG_EMAIL_PASSWORD);
				break;
				default:
					DBG(TAG,"Error updateCCAddCardBill Billing "+message);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error updateCCAddCardBill Billing "+message);
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
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
	 *  This method is called from Model with a response from server, after such a
	 * request has been initiated with <code>sendSingleBillRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCSingleBill(boolean success, int message)
	{
		DBG(TAG,"updateCCSingleBill()");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "updateCCSingleBill()");

		if(success)
		{	
			setState(IAB_STATE_CC_TRANSACTION_SUCCESS);
		}else
		{
			switch(message)
			{
				case XPlayer.CC_ERROR_GENERIC:
					DBG(TAG,"CC_ERROR_GENERIC");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_GENERIC");
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
				break;
				
				case XPlayer.CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE://User Created Invalid Credit Card
				case XPlayer.CC_ERROR_INVALID_CREDIT_CARD_VALUE:	
				case XPlayer.CC_ERROR_INVALID_EXP_DATE:
				case XPlayer.CC_ERROR_INVALID_CCV:	
					DBG(TAG,"CC_ERROR_WRONG_CARD_INFO");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "CC_ERROR_WRONG_CARD_INFO");
					setState(IAB_STATE_CREDIT_CARD_EXPIRED);
				break;
				
				default:
					DBG(TAG,"Error CCNewUserBill Billing "+message);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error CCNewUserBill Billing "+message);
					setState(IAB_STATE_CC_TRANSACTION_FAILED);
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
	 * request has been initiated with <code>sendForgotPasswordRequest()</code>, this
	 * @param success
	 * @param message
	 */
	public void updateCCForgotPassword(boolean success, int message)
	{
		DBG(TAG,"updateCCForgotPassword()");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "updateCCForgotPassword()");
		setProgressBarVisibility(R.id.ProgressBarFP, ProgressBar.INVISIBLE);
		if(success)
		{	
			setTextViewNewText( R.id.lblForgotPassword, R.string.IAB_CC_RECOVERY_PASSWORD_SUCCES, -1);
		}else
		{
			setTextViewNewText( R.id.lblForgotPassword, R.string.IAB_CC_RECOVERY_PASSWORD_FAIL, Color.RED);
			setButtonVisibility(R.id.bt_ly_forgot_password_ok, Button.VISIBLE);
			DBG(TAG,"Error CCForgotPassword "+message);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error CCForgotPassword "+message);
		}

		setButtonVisibility(R.id.bt_ly_forgot_password_back, Button.VISIBLE);
    	setButtonVisibility(R.id.bt_ly_forgot_password_tcs, Button.VISIBLE);
    	setButtonVisibility(R.id.bt_ly_forgot_password_exit, Button.VISIBLE);
    	setState(IAB_STATE_CC_FORGOT_PASSWORD_RESULT);
    	if(mBilling != null)
		{
			mBilling.release();
			mBilling = null;
		}
	}
	
	public String getTimeStampRequest(String URL) 
	{
		XPlayer mXPlayer = new XPlayer(new Device(mServerInfo));
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
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
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "TimeStamp request failed");
			timeStampResponseString = null;//System.currentTimeMillis()+"";
		}
		else
		{
			DBG(TAG,"TimeStamp request success = "+XPlayer.getLastErrorCodeString());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "TimeStamp request success = "+XPlayer.getLastErrorCodeString());
			timeStampResponseString = XPlayer.getLastErrorCodeString();
		}
		return timeStampResponseString; 
	}
	
	#if SHENZHOUFU_STORE
	public void ProcessTransactionShenzhoufu(String id) 
	{
		String mCurrentId = id;
		XPlayer mXPlayer = new XPlayer(new Device(mServerInfo));
		mXPlayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
		mXPlayer.sendShenzhoufuPurchaseRequest(mCurrentId,mUserInfo.getCardType(),mUserInfo.getCardDenomination(),mUserInfo.getShenzhoufuCardNumber(),mUserInfo.getCardPassword());
		while (!mXPlayer.handleShenzhoufuPurchaseRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		
		if(XPlayer.getLastErrorCode()!=XPlayer.ERROR_NONE)
		{
			if(!XPlayer.getLastErrorCodeString().equals(GLOFTHelper.GetInstance().SUCESS_CODE) && !XPlayer.getLastErrorCodeString().equals(GLOFTHelper.GetInstance().SUCESS_PENDING_CODE))
				//ALL ERRORS HERE!!!
				DBG(TAG,"Shenzhoufu transaction failed "+GLOFTHelper.GetInstance().DIALOG_PURCHASE_FAIL+"   "+id);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Shenzhoufu transaction failed "+GLOFTHelper.GetInstance().DIALOG_PURCHASE_FAIL+"   "+id);
				shenzhoufuErrorString = "";
				if(XPlayer.getLastErrorCode() == -107)
					shenzhoufuErrorString = getString(R.string.IAB_PURCHASE_ITEM_FAILURE_INSUFFICIENT_BALANCE_SHENZHOUFU);
				setState(IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED);
		}
		else
		{
			DBG(TAG,"Shenzhoufu transaction success "+GLOFTHelper.GetInstance().SUCESS_CODE+"   "+id);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Shenzhoufu transaction success "+GLOFTHelper.GetInstance().SUCESS_CODE+"   "+id);

			//start Billing result request
			InAppBilling.mItemID = mCurrentId;
			InAppBilling.saveLastItem(WAITING_FOR_SHENZHOUFU);
			InAppBilling.mOrderID = XPlayer.getLastErrorCodeString();
			InAppBilling.save(WAITING_FOR_SHENZHOUFU);
			
			SUtils.getLManager().SaveUserPaymentType(LManager.SHENZHOUFU_PAYMENT);
			
			int MAX_RETRY = 5;
			int current_attempt = 1;
			boolean keep_verifing = true;
			final int BILLING_SUCCESS = 0;
			final int BILLING_SUCCESS_OFFLINE = 1;
			final int BILLING_PENDING = 2;
			final int BILLING_FAILURE = 3;
			String transactionID=XPlayer.getLastErrorCodeString();//transaction id
			
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
						shenzhoufuErrorString = "";
						shenzhoufuErrorString = getString(R.string.IAB_PURCHASE_ITEM_FAILURE_VERIFICATION_FAILED);
						setState(IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED);
					}
					else if( XPlayer.getLastErrorCodeString().equals(mXPlayer.GetResultValue(BILLING_PENDING)) ||
						XPlayer.getLastErrorCodeString().equals("FAILURE"))
					{
						DBG(TAG,"Retry due PENDING or FAIL");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Retry due PENDING or FAIL");
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
							setState(IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED);
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
														
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);//trash value
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
						ERR(TAG,"GOING TO FINALIZE!!!!!!!!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "ProcessTransactionShenzhoufu - GOING TO FINALIZE!");
						setState(IAB_STATE_FINALIZE);
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
							setState(IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED);
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
						setState(IAB_STATE_SHENZHOUFU_TRANSACTION_FAILED);
					}
					try {
					Thread.sleep(5000);
					} catch (Exception exc) {}
				}
			}
		}
	}
	#endif
	long m_phonedatatimeout=0;
	boolean TIMED_OUT = false;
	long PHONE_DATA_TIME_OUT = 25000;
	
}

#undef GES(id)
#undef GRS(id)
#endif
