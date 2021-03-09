#if USE_IN_APP_BILLING
#define GES(id)		StringEncrypter.getString(id) //get encrypted string
#define GRS(id)		SUtils.getContext().getString(id) //get raw string
package APP_PACKAGE.iab;




import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import APP_PACKAGE.billing.common.DefReader;
import APP_PACKAGE.billing.common.StringEncrypter;
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


#if USE_INSTALLER
import APP_PACKAGE.installer.GameInstaller;
#endif

public class InAppBilling
{
	SET_TAG("InAppBilling");
	
	public static ServerInfo mServerInfo;

	
	static int mTransactionStep = 0;
	
	static void save(int state)
	{
		ERR(TAG,"Save Transaction info: "+state);
		
		StringEncrypter sc = new StringEncrypter(GES(IAB_STR_ENCODE_KEY), GES(IAB_STR_6_PASSES));

		String values[] = new String[3];
		
		values[0] = new Boolean(true).toString();					//save true for pending
				values[1] = new Boolean(false).toString();
				String dummy0 = values[1] + GES(IAB_STR_SEPARATOR) +  sc.getValue()+ GES(IAB_STR_SEPARATOR) + values[0];
				values[2] = new Boolean(false).toString();
				String dummy1 = values[2] + GES(IAB_STR_SEPARATOR) +  sc.getValue() + GES(IAB_STR_SEPARATOR) + values[1];

		String raw = values[0] + GES(IAB_STR_SEPARATOR) + values[2] + GES(IAB_STR_SEPARATOR) + values[1];	//encode true + false + false
				String dummy2 = values[0] + GES(IAB_STR_SEPARATOR) +  sc.getValue() + GES(IAB_STR_SEPARATOR) + values[0];

		
		{
			SUtils.setPreference(GES(IAB_STR_BOOLEAN_PENDING), sc.encrypt(raw), GES(IAB_STR_PREF_NAME));
			
		}
	{
		SUtils.setPreference(GES(IAB_STR_ITEM_LIST), 	sc.encrypt(mReqList!=null?mReqList:GRS(IAB_STR_EMPTY)),			GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_ITEM), 		sc.encrypt(mItemID!=null?mItemID:GRS(IAB_STR_EMPTY)), 			GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_CHARID), 		sc.encrypt(mCharId!=null?mCharId:GRS(IAB_STR_EMPTY)), 			GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_CHARREGION), 	sc.encrypt(mCharRegion!=null?mCharRegion:GRS(IAB_STR_EMPTY)),	GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_STATE), 		sc.encrypt(state+GRS(IAB_STR_EMPTY)), 							GES(IAB_STR_PREF_NAME));
		

		long currentTime = 0;
		
		if(state == WAITING_FOR_BOKU || state == WAITING_FOR_GAMELOFT || state == WAITING_FOR_PSMS || state == WAITING_FOR_SHENZHOUFU)
		{
			currentTime = System.currentTimeMillis();
		}
		
		SUtils.setPreference(GES(IAB_STR_WAITTING_TIME),	currentTime+GRS(IAB_STR_EMPTY), 							GES(IAB_STR_PREF_NAME));
		
		//String time = currentTime+GRS(IAB_STR_EMPTY);
		INFO(TAG, "Saving pending time: "+currentTime);
		//JDUMP(TAG,time);
		
	#if BOKU_STORE || USE_MTK_SHOP_BUILD || USE_UMP_R3_BILLING || SHENZHOUFU_STORE || ENABLE_IAP_PSMS_BILLING
		//SUtils.setPreference(GES(IAB_STR_GOOGLE_RESPONSE), 	GES(IAB_STR_TRUE),					GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_ORDER_ID), 	sc.encrypt(mOrderID!=null?mOrderID:GRS(IAB_STR_EMPTY)), 	GES(IAB_STR_PREF_NAME));
	#endif
		//SUtils.setPreference(GES(IAB_STR_ITEM),mItemID, GES(IAB_STR_PREF_NAME));
	}	
		mTransactionStep = state;
	}
	
//     public static void GoogleAnalyticsTrackTransaction()
//     {
//     #if USE_GOOGLE_ANALYTICS_TRACKING
//         if(mItemID == null)
//             return;
//         if(mServerInfo == null)
//             return; 
//         if(mServerInfo.getCurrentBilling() == null)
//             return;
// 
//         try
//         {
//             cItem item = mServerInfo.getItemById(mItemID);
//             String sPrice = mServerInfo.getGamePrice ();
// 		    String sAmount = item.getAttributeByName("amount");
//     	
//             float fAmount = 1.0f;
// 		    float fPriceInMillis = Float.valueOf(sPrice) * 1000000;
//     		
//     	
// 		    Item.Builder analyticsItemBuilderObj = new Item.Builder(mItemID, mServerInfo.getItemUID(), (long)fPriceInMillis, (long)fAmount);
// 		    analyticsItemBuilderObj.setProductCategory(item.getType());
//     	
// 		    Item purchasedItem = analyticsItemBuilderObj.build();
//     	
// 		    Transaction.Builder analyticsTransactionBuilderObj = new Transaction.Builder(mOrderID, (long)fPriceInMillis);
// 		    analyticsTransactionBuilderObj.setAffiliation(mServerInfo.getStringCurrencyValue());
//     	
// 		    Transaction transactionObj = analyticsTransactionBuilderObj.build();
// 		    transactionObj.addItem(purchasedItem);
// 
// 		    GoogleAnalyticsTracker.trackTransaction(transactionObj);
//         }
//         catch(Exception e)
//         {
//         }
// 	#endif //USE_GOOGLE_ANALYTICS_TRACKING
//     }   
	
	static boolean IsInternetAvaliable()
	{
		//is reachable?
		try{
			URL aURL = new URL("http://www.google.com");
			HttpURLConnection conn = (HttpURLConnection) aURL.openConnection();
			conn.setConnectTimeout(3*1000);
			conn.connect();
		}catch(Exception e){	return false; }
		
		return true;
	}
	
	static void saveLastItem(int lastState)
	{
		ERR(TAG,"Save Last Item info: "+lastState);
		
		//													  1 passes for validate
		StringEncrypter sc = new StringEncrypter(GES(IAB_STR_ENCODE_KEY), GES(IAB_STR_1_PASSES));

		String values[] = new String[3];
		
		values[0] = new Boolean(true).toString();					//save true for pending
				values[1] = new Boolean(false).toString();
				String dummy0 = values[1] +  GES(IAB_STR_SEPARATOR) +  sc.getValue()+  GES(IAB_STR_SEPARATOR) + values[0];
				values[2] = new Boolean(false).toString();
				String dummy1 = values[2] +  GES(IAB_STR_SEPARATOR) +  sc.getValue() +  GES(IAB_STR_SEPARATOR) + values[1];

		String raw = mItemID +  GES(IAB_STR_SEPARATOR) + values[2] +  GES(IAB_STR_SEPARATOR) + lastState;	//encode itemID + false + lastState
				String dummy2 = values[0] +  GES(IAB_STR_SEPARATOR) +  sc.getValue() +  GES(IAB_STR_SEPARATOR) + values[0];
		
		SUtils.setPreference(GES(IAB_STR_LAST_ITEM), sc.encrypt(raw), GES(IAB_STR_PREF_NAME));
	}
	
	static String getLastItem()
	{
		//ERR(TAG,"Get last Item info");
		try
		{
			StringEncrypter sc = new StringEncrypter(GES(IAB_STR_ENCODE_KEY), GES(IAB_STR_1_PASSES));
			
			String raw = SUtils.getPreferenceString(GES(IAB_STR_LAST_ITEM), GES(IAB_STR_PREF_NAME));
			String decodeOut = sc.decrypt(raw);
			String values[] = decodeOut.split(GES(IAB_STR_SEPARATOR));
			//Boolean b = new Boolean(values[0] /*&& !values[1] && !values[2]*/ );	//SUtils.getPreferenceBoolean(GES(IAB_STR_BOOLEAN_PENDING), new Boolean(false), GES(IAB_STR_PREF_NAME));
			
			//ERR(TAG,"Get last Item info: "+values[0]);
			return values[0];
		}catch(Exception e) {	/*DBG_EXCEPTION(e);*/ }
		
		//ERR(TAG,"Get last Item info: null");
		return null;
		//ERR(TAG,"Load Transaction info, loaded? "+b);
	}
	
	
	static int getLastState()
	{
		//ERR(TAG,"Get last State info");
		try
		{
			StringEncrypter sc = new StringEncrypter(GES(IAB_STR_ENCODE_KEY), GES(IAB_STR_1_PASSES));
			
			String raw = SUtils.getPreferenceString(GES(IAB_STR_LAST_ITEM), GES(IAB_STR_PREF_NAME));
			String decodeOut = sc.decrypt(raw);
			String values[] = decodeOut.split(GES(IAB_STR_SEPARATOR));
			//Boolean b = new Boolean(values[0] /*&& !values[1] && !values[2]*/ );	//SUtils.getPreferenceBoolean(GES(IAB_STR_BOOLEAN_PENDING), new Boolean(false), GES(IAB_STR_PREF_NAME));
			int res = Integer.parseInt(values[2]);
			//ERR(TAG,"Get last State info: "+res);
			return res;
		}catch(Exception e) {	/*DBG_EXCEPTION(e);*/	}

		//ERR(TAG,"Get last State info: 0");
		return 0;
		//ERR(TAG,"Load Transaction info, loaded? "+b);
	}
	
	static void saveNoGoogleReponse()
	{
		DBG(TAG, "saveNoGoogleReponse");
		SUtils.setPreference(GES(IAB_STR_GOOGLE_RESPONSE), 	GES(IAB_STR_FALSE),	GES(IAB_STR_PREF_NAME));
	}
	
	static boolean isGoogleResponse()
	{
		
		//String raw = SUtils.getPreferenceString(GES(IAB_STR_LAST_ITEM), GES(IAB_STR_PREF_NAME));
		String raw = SUtils.getPreferenceString(GES(IAB_STR_GOOGLE_RESPONSE), GES(IAB_STR_PREF_NAME));
		//INFO(TAG, "isGoogleResponse "+raw);
		
		boolean b = false;
		try
		{
			Boolean t = new Boolean(raw);
			INFO(TAG, "isGoogleResponse ? "+t);
			return t;
		}catch(Exception e){ 	DBG_EXCEPTION(e);	}
			
		INFO(TAG, "isGoogleResponse ? "+b);
		return b;
	}
	
	static void load()
	{
		ERR(TAG,"Load Transaction info");
																	//6 passes for modificated values
		StringEncrypter sc = new StringEncrypter(GES(IAB_STR_ENCODE_KEY), GES(IAB_STR_6_PASSES_LOAD));
		String raw = SUtils.getPreferenceString(GES(IAB_STR_BOOLEAN_PENDING), GES(IAB_STR_PREF_NAME));
		
		Boolean b;
		//we have old pending save
		
		try
		{
			String decodeOut = sc.decrypt(raw);
			String values[] = decodeOut.split(GES(IAB_STR_SEPARATOR));
			INFO(TAG,"Decrypting values "+raw+ " found: "+values[0]);
			b = new Boolean(values[0] /*&& !values[1] && !values[2]*/ );	//SUtils.getPreferenceBoolean(GES(IAB_STR_BOOLEAN_PENDING), new Boolean(false), GES(IAB_STR_PREF_NAME));
		}catch(Exception e)
		{
			//ERR(TAG, "Error decrypting encoded value");
			b = new Boolean(false);
		}
		
		if(b /*&& !values[1]*/)
		{
			//String a = ;
			mReqList = sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_ITEM_LIST),GES(IAB_STR_PREF_NAME)));
		//JDUMP(TAG, mReqList);
			//String f = ;
			mItemID = sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_ITEM), GES(IAB_STR_PREF_NAME)));
		//JDUMP(TAG, mItemID);
			//String c = ;
			mCharId = sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_CHARID), GES(IAB_STR_PREF_NAME)));
		//JDUMP(TAG, mCharId);
			//String d = ;
			mCharRegion = sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_CHARREGION), GES(IAB_STR_PREF_NAME)));
		//JDUMP(TAG, mCharRegion);
			
			//String e = ;
			mTransactionStep = Integer.parseInt(sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_STATE), GES(IAB_STR_PREF_NAME))));
			
			
			mOrderID	= sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_ORDER_ID), GES(IAB_STR_PREF_NAME)));
			mNotifyId	= sc.decrypt(SUtils.getPreferenceString(GES(IAB_STR_NOTIFYID), GES(IAB_STR_PREF_NAME)));
			//for tests //if(mServerInfo != null)
			mServerInfo.searchForDefaultBillingMethod(mItemID);
			
			
			//sc = null;
			// JDUMP(TAG, mReqList);
			// JDUMP(TAG, mItemID);
			// JDUMP(TAG, mCharId);
			// JDUMP(TAG, mCharRegion);
			// JDUMP(TAG, mTransactionStep);
			//String itemID = SUtils.getPreferenceString(GES(IAB_STR_ITEM),mItemID, GES(IAB_STR_PREF_NAME));
		}
	}
	
	public static int GetState()
	{
		int state = SUtils.getPreferenceInt(GES(IAB_STR_STATE),0, GES(IAB_STR_PREF_NAME));
		INFO(TAG, "GetState: "+state);
		return state;
	}
	static boolean isPending()
	{
		try
		{
			StringEncrypter sc = new StringEncrypter(GES(IAB_STR_ENCODE_KEY), GES(IAB_STR_1_PASSES));
			String raw = SUtils.getPreferenceString(GES(IAB_STR_BOOLEAN_PENDING), GES(IAB_STR_PREF_NAME));
			//JDUMP(TAG,raw);
			String decodeOut = sc.decrypt(raw);
			String values[] = decodeOut.split(GES(IAB_STR_SEPARATOR));
			//JDUMP(TAG, values[0]);
			Boolean b = new Boolean(values[0]);
			ERR(TAG,"Is Transaction Pending? "+b);
			return b;
		}catch(Exception e)
		{
			//ERR(TAG, "Error decrypting encoded value");
		}
		//Boolean b = SUtils.getPreferenceBoolean(GES(IAB_STR_BOOLEAN_PENDING), new Boolean(false), GES(IAB_STR_PREF_NAME));
		
		//ERR(TAG,"Is Transaction Pending Exception? false");
		return false;
	}
	
	static void clear()
	{
		INFO(TAG,"Clear Transaction info");
		Boolean b = new Boolean(false);
		Boolean c = new Boolean(true);
		SUtils.setPreference(GES(IAB_STR_BOOLEAN_PENDING), 	GRS(IAB_STR_FALSE), GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_ITEM_LIST),		GRS(IAB_STR_EMPTY), GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_ITEM),				GRS(IAB_STR_EMPTY), GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_CHARID),			GRS(IAB_STR_EMPTY), GES(IAB_STR_PREF_NAME));
															//dummy true value
		SUtils.setPreference(GES(IAB_STR_CHARREGION),		c.toString(), 		GES(IAB_STR_PREF_NAME));
		SUtils.setPreference(GES(IAB_STR_STATE),			0, 					GES(IAB_STR_PREF_NAME));
															//dummy false value
		SUtils.setPreference(GES(IAB_STR_ITEM_ID),			b.toString(), 		GES(IAB_STR_PREF_NAME));
		
		SUtils.setPreference(GES(IAB_STR_GOOGLE_RESPONSE),	b.toString(), 		GES(IAB_STR_PREF_NAME));
	}
	
#if GOOGLE_STORE_V3	|| BAZAAR_STORE || YANDEX_STORE

	public static void c()
	{
		if (mFRServiceNameList.isEmpty())
		{
			d();
			mMKTStatusHk = MKT_FREEDOM_RUNNING;
			return;
		}
		ActivityManager manager = (ActivityManager)((Activity)SUtils.getContext()).getSystemService(Context.ACTIVITY_SERVICE);
		mMKTStatusHk = mMKTStatus;
			
		if (manager.getRunningServices(Integer.MAX_VALUE).isEmpty())
		{
			mMKTStatusHk = MKT_FREEDOM_RUNNING;
			return;
		}
			
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
		{
			String hash = IABRequestHandler.getInstance().SHA256(service.service.getClassName()+Salt);
			if (mFRServiceNameList.contains(hash)) 
			{
				mMKTStatusHk = MKT_FREEDOM_RUNNING;
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Freedom detected");
			}
		}
	}
	static ArrayList<String> mFRServiceNameList = new ArrayList<String>();
	private static String Salt = null;
	static void d ()//Get the service name list
	{
		DBG(TAG, "[Get srvc name List]");
		String URL = GET_STR_CONST(IAB_URL_HK_SERVICE);
		if (mFRServiceNameList.isEmpty())
		{
			LOGGING_APPEND_REQUEST_PARAM(URL, "GET", "Banned service list");
			LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("Banned service list"));
			IABRequestHandler.getInstance().doRequestByGet(URL, null, new IABCallBack()
			{
				public void runCallBack(Bundle bundle)
				{
					DBG(TAG, "[Get srvc name List] Callback");
					int response = bundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
					if (response == IABRequestHandler.SUCCESS_RESULT)
					{
						String dString = bundle.getString(IABRequestHandler.KEY_RESPONSE);
						JDUMP(TAG,dString);

						LOGGING_APPEND_RESPONSE_PARAM(dString, "Banned service list");
						LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("Banned service list"));
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Banned service list: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("Banned service list") +" seconds" );
						LOGGING_REQUEST_REMOVE_REQUEST_INFO("Banned service list");
						
						JSONObject jObj		= null;
						JSONArray jArray 	= null;
						
						try {
							jObj = new JSONObject(dString);
							jArray = jObj.optJSONArray(GET_STR_CONST(IAB_SERVICE_KEY));
							
							for (int i = 0; i < jArray.length(); i++)
							{
								JDUMP(TAG,jArray.getString(i));
								mFRServiceNameList.add(jArray.getString(i));
							}
						} catch (JSONException e) {
							
						}
					
					}
				#if USE_IN_APP_GLOT_LOGGING
					else
					{
						try {
							
							int code = bundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for Banned service list failed with error code: "+code );
						} catch (Exception e) {}
					}
				#endif
				}
			});
		}
	}
	
	private static String epk 			= null;
	private static final String CPK 	= AMP_EPK;
	private static final String KEY		= GGC_GAME_CODE;
	private static final String DKEY	= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0apk+aD46nv4t2utni2rYmToO0pFHM9UlqohWyCvaYQo18ElxTRyPLN5cHpyypZltSPNx+QnlZYV9aM/CVoKgTH58djTrnIok8YABn1tz89nHR9wsP5Akz5js3E2AaiJLWm/HsAuERH3pLg/oHoWTBBr0+iXuWUGeLH0KATN/6STKLNZLpdXyHXyQayUzFdSuYKOOnxHVJX79ty5kluCqRlSpfHHOd93lKi6L8+p92RB9pwcXeEjjHfAw7EGzdS7slkE1na/L75TcoBNhQxG9RdvZfnXfWnHJ07n+051qlVwF7FXgbpHrnOpC9uJOFjrSJa+VM25pQfymmzoR6LEDwIDAQAB";
	
	public static String gk()
	{
		if (epk == null)
		{
			epk = DefReader.getInstance().getPlainDef(CPK, KEY);
		}
		if (epk == null)//return debug key
			epk = DKEY;
		
		return epk;
	}
	
#endif
	
	
	static String mReqList 		= null;
	static String mItemID 		= null;
    static String mLastItemIdFromList = null;//Used for SKT restore transactions initialization
	static String mCharId 		= null;
	static String mCharRegion 	= null;
	static String mOrderID 		= null; //Also used as Transaction id for boku or Token Id for Amazon
	static String mNotifyId		= null; //google notification

	public static void init(Context context)
	{
		SUtils.setContext(context);
		if (isIAPInitialized)
			return;
		nativeInit(context);
		isIAPInitialized = true;
		// //debug
		// save(1, true);
		// load();
		// //uncomment for clean pending transactions on init. only for DEBUG
		// clear();
		
		//JDUMP(TAG, isGoogleResponse());
		
	#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
		new Thread( new Runnable()
		{
			public void run()
			{
				try { Thread.sleep(2*1000); }catch(Exception e) {}
				d();//Fill Service name list
			}
		}
		#if !RELEASE_VERSION
			,"FillSNList"
		#endif
			).start();
		Salt = GET_STR_CONST(IAB_SALT_KEY);
	#endif

		if (mServerInfo == null)
		{
			INFO(TAG, "Creating server info");
			mServerInfo = new ServerInfo();
		}
	#if GOOGLE_STORE_V3 || AMAZON_STORE || BAZAAR_STORE || YANDEX_STORE
		if (mMKTStatus == MKT_UNKNOWN)
		{
			((Activity)SUtils.getContext()).runOnUiThread(new Runnable () 
			{
				public void run () 
				{	
					try 
					{
						handleOperations(OP_IAB_IS_SUPPORTED);
					} catch (Exception e) {
						DBG_EXCEPTION(e);
					}
				}
			});	
		}
	#endif 
	}
	
	public static int getTotalItems()
	{
		if (mServerInfo != null)
		{
			if (mReqList.equals(""))
				return mServerInfo.getTotalItems();
			else
				return mServerInfo.getTotalItemsByList(mReqList);
		}
		return 0;
	}
	private static IABCallBack mRequestListCB = new IABCallBack()
	{
		public void runCallBack(Bundle bundle)
		{
			DBG(TAG,"RequestListCB");
			try
			{
				isRequestingItemList = false;
				boolean dlImages = false;
				byte[] urlA;
				String img_url;
				int nit = getTotalItems();
				
				URL url = null;
				java.net.HttpURLConnection http = null;
				
				InputStream is = null;
				java.io.DataInputStream din = null;
				java.io.FileOutputStream fOut = null;
				
				//Verifying if the attribute image still is a url of the image...
				for (int i = 0; i < nit && !dlImages; i ++)
				{
					urlA = mServerInfo.getAttributeByNameIdx(mReqList, GET_STR_CONST(IAB_IMAGE), 0);
					img_url = (urlA == null)?null:new String(urlA);
					if (img_url != null && img_url.indexOf(GET_STR_CONST(IAB_HTTP)) != -1)
					{
						dlImages = true;
					}
				}
				//(nit == 0) means the items aren't downloaded yet, 
				//(dlImages == true) means that all or some image will be downloaded if it doesn't exist
				if (dlImages)
				{
					if (nit > 0)
					{	
						isProfileReady = true;
					}
					
					for (int i = 0; i < nit; i ++)
					{
						urlA = mServerInfo.getAttributeByNameIdx(mReqList, GET_STR_CONST(IAB_IMAGE), i);
						img_url = (urlA == null)?null:new String(urlA);
						//verifying if the value for the atribute image still is an http url, if true means 
						//that we have to download the image if it doesn't exist or it isn't the same
						if (img_url != null && img_url.indexOf(GET_STR_CONST(IAB_HTTP)) != -1)
						{
							DBG(TAG,img_url);
							String idname = new String(mServerInfo.getItemIdByIdx(mReqList, i));
                            mLastItemIdFromList = idname;//for SKT restore transactions
							//String imgName = idname +"_"+img_url.substring(img_url.lastIndexOf("/")+1);
							String imgName = "imgiab"+idname +".bin";
							String imgNameFP = getSDFolder() + "/" +imgName;
							INFO(TAG,imgNameFP); //the new name of the image
							
							try{
								url = new URL(img_url);
								http = (java.net.HttpURLConnection)url.openConnection();
								http.setConnectTimeout(DL_IMAGE_TIME_OUT);
								http.setRequestMethod("GET");	
								http.setRequestProperty("Connection", "close");
								
								java.io.File file = new java.io.File(imgNameFP);
								int flsize = 0;
								if (!file.exists())
								{
									java.io.File parent = new java.io.File(file.getParent());
									parent.mkdirs();
									parent = null;					
									file.createNewFile();
								}
								else
								{
									flsize = (int)file.length();
								}
								file = null;
								if (http.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) 
								{
									int  imgsize = http.getContentLength();
									if (imgsize == 1)
									{
										DBG(TAG,"Error getting image size");
										continue;
									}
									if (imgsize == flsize)
									{
										DBG(TAG,"File already exist");
										//File exist and it's the same as the one in the server,
										//we don't need to downloaded it again, we just replace its atribute name with the new name
										//to avoid checking again
										mServerInfo.replaceAttributeByNameIdx(mReqList, GET_STR_CONST(IAB_IMAGE), imgName, i);
										continue;
									}
										
									DBG(TAG,"Image server size = "+imgsize);
									DBG(TAG,"HTTP RESPONSE CODE RECEIVED = "+http.getResponseCode());
									synchronized (http) {
										is = http.getInputStream();
										din = new java.io.DataInputStream(is);
										fOut = new java.io.FileOutputStream(imgNameFP);
										int readSize = 0;
										
										//downloading image
										while(readSize < imgsize)
										{
											int section  = imgsize - readSize;
											if(section > DL_IMAGE_BUFFER_SIZE)
												section = DL_IMAGE_BUFFER_SIZE;		
											byte[] tmpByte = new byte[section];
											din.readFully(tmpByte);
											fOut.write(tmpByte);
											fOut.flush();
											readSize += section;
											tmpByte = null;
										}
										fOut.close();
										fOut = null;
										din.close();
										din = null;
										is.close();
										is = null;
										
										url = null;
										http = null;
										//replaceng its atribute name with the new name (removing the url with the saved name)
										//to avoid checking again
										mServerInfo.replaceAttributeByNameIdx(mReqList, GET_STR_CONST(IAB_IMAGE), imgName, i);
									}
								}
							}catch(java.net.UnknownHostException unknown) {
								DBG("Tracking","No internet avaliable");
							}
							catch(Exception e){	DBG_EXCEPTION(e);}
							
							
						}
					}
				}
				bundle = new Bundle();
				bundle.putInt(GET_STR_CONST(IAB_OPERATION), OP_FINISH_GET_LIST);
				
				bundle.putByteArray(GET_STR_CONST(IAB_ITEM),(mItemID!=null)?mItemID.getBytes():null);//trash value
				bundle.putByteArray(GET_STR_CONST(IAB_LIST),(mReqList!=null)?mReqList.getBytes():null);
				bundle.putByteArray(GET_STR_CONST(IAB_CHAR_REGION),(mCharRegion!=null)?mCharRegion.getBytes():null);//trash value
				bundle.putByteArray(GET_STR_CONST(IAB_CHAR_ID),(mCharId!=null)?mCharId.getBytes():null);//trash value
				
				bundle.putInt(GET_STR_CONST(IAB_INDEX), 1);//trash value
				bundle.putInt(GET_STR_CONST(IAB_RESULT), 0);//trash value
			
				try{
					Class myClass = Class.forName(GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
					Method mMethod = myClass.getMethod(GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
					bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
				}catch(Exception ex ) {
					ERR(TAG,"Error invoking reflex method "+ex.getMessage());
				}
				
			
			#if BOKU_STORE || USE_MTK_SHOP_BUILD || USE_UMP_R3_BILLING || SHENZHOUFU_STORE || ENABLE_IAP_PSMS_BILLING
				if(isPending())
				{
				
					load();
					INFO(TAG,"Waiting for pending transaction(after retry list) step = "+mTransactionStep);
					if(nit > 0)
					{
					#if BOKU_STORE || USE_MTK_SHOP_BUILD || SHENZHOUFU_STORE || ENABLE_IAP_PSMS_BILLING
						if (mIABTrans == null) //boku doesn't initialize like google does....
							mIABTrans = new GLOFTHelper(mServerInfo);
						mIABTrans.sendConfirmation();
					#endif	
					}
				}
			#endif	
			} catch (Exception e) {
				DBG_EXCEPTION(e);
			}
		}
	};
	
	static boolean isProfileReady = false;
	static int mOperationId;
	static boolean isIAPInitialized = false;
	static boolean isRequestingItemList = false;
	private static synchronized boolean handleOperations(int msg)
	{
		mOperationId = msg;
		if (msg == OP_RETRIEVE_LIST)
		{
			DBG(TAG, "msg.what == OP_RETRIEVE_LIST");
			int nit = getTotalItems();
			if (nit == 0 && !isRequestingItemList)
			{
				DBG(TAG, "OP_RETRIEVE_LIST is requesting item list");
				isRequestingItemList = true;
				mServerInfo.getUpdateProfileInfo(mRequestListCB);
			}
			else
			{
				mRequestListCB.runCallBack(null);
			}
			DBG(TAG, "msg.what == OP_RETRIEVE_LIST end");
			return true;
		}
		else if (msg == OP_TRANSACTION)
		{
			DBG(TAG, "msg.what == OP_TRANSACTION");
			try
			{
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "OP_TRANSACTION Start");
				if (mIABTrans == null)
				{
				#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
					mIABTrans = new GMPHelper(mServerInfo);
				#endif //GOOGLE_STORE_V3

				#if SKT_STORE
					mIABTrans = new SKTHelper(mServerInfo);
				#endif //SKT_STORE
				
				#if PANTECH_STORE
					mIABTrans = new PantechHelper(mServerInfo);
				#endif //PANTECH_STORE
				
				#if KT_STORE
				  mIABTrans = new KTHelper(mServerInfo);
				#endif  //KT_STORE
				
				#if GAMELOFT_SHOP
					mIABTrans = new GLOFTHelper(mServerInfo);
				#endif//GAMELOFT_SHOP
				
				#if AMAZON_STORE
					mIABTrans = new AMZHelper(mServerInfo);
				#endif //AMAZON_STORE
				
				#if VZW_STORE
					mIABTrans = new VZWHelper(mServerInfo);
				#endif//VZW_STORE
				
				#if SAMSUNG_STORE
					mIABTrans = new SamsungHelper(mServerInfo);
				#endif//SAMSUNG_STORE
								
				#if VXINYOU_STORE
					mIABTrans = new VxinyouHelper(mServerInfo);
				#endif//VXINYOU_STORE

				#if ZTE_STORE
					mIABTrans = new ZTEHelper(mServerInfo);
				#endif//ZTE_STORE
				
				#if ATET_STORE
					mIABTrans = new ATETHelper(mServerInfo);
				#endif

				#if HUAWEI_STORE
					mIABTrans = new HuaweiHelper(mServerInfo);
				#endif//HUAWEI_STORE
				
				}
			
			#if GOOGLE_STORE_V3 || BOKU_STORE || USE_UMP_R3_BILLING || SHENZHOUFU_STORE || BAZAAR_STORE || YANDEX_STORE
				//clear(); //only for testing please remove it
				if(isPending())
				{
				
					INFO(TAG,"Waitting for pending transaction");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Waitting for pending transaction");
					load();
					JDUMP(TAG,mTransactionStep);
					if(mTransactionStep == WAITING_FOR_GOOGLE || mTransactionStep == WAITING_FOR_BOKU || mTransactionStep == WAITING_FOR_GAME || mTransactionStep == WAITING_FOR_PSMS || mTransactionStep == WAITING_FOR_SHENZHOUFU)
					{
					#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
						mIABTrans.showDialog(GMPHelper.DIALOG_WAITTING_CONFIRMATION, mItemID);
					#elif USE_UMP_R3_BILLING
						mIABTrans.showDialog(GLOFTHelper.DIALOG_PENDING_PSMS, mItemID);
					// #elif SHENZHOUFU_STORE
						// mIABTrans.showDialog(GLOFTHelper.DIALOG_PENDING_SHENZHOUFU, mItemID);
					#elif BOKU_STORE || USE_MTK_SHOP_BUILD || SHENZHOUFU_STORE || ENABLE_IAP_PSMS_BILLING
						if( mTransactionStep == WAITING_FOR_SHENZHOUFU )
							mIABTrans.showDialog(GLOFTHelper.DIALOG_PENDING_SHENZHOUFU, mItemID);
						else
							mIABTrans.showDialog(mTransactionStep == WAITING_FOR_PSMS?GLOFTHelper.DIALOG_PENDING_PSMS:GLOFTHelper.DIALOG_PURCHASE_RESULT_PENDING, mItemID);
					#endif
					}
					return true;
				}
			#endif
				mServerInfo.searchForDefaultBillingMethod(mItemID);

			#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
				c();
				#if BAZAAR_STORE
				if (mMKTStatusHk == MKT_FREEDOM_RUNNING || mMKTStatus != MKT_SUPPORTED || !mServerInfo.getBillingType().equals(GET_STR_CONST(IAB_BAZAAR)))
				#elif YANDEX_STORE
				if (mMKTStatusHk == MKT_FREEDOM_RUNNING || mMKTStatus != MKT_SUPPORTED || !mServerInfo.getBillingType().equals(GET_STR_CONST(IAB_YANDEX)))
				#else
				if (mMKTStatusHk == MKT_FREEDOM_RUNNING || mMKTStatus != MKT_SUPPORTED || !mServerInfo.getBillingType().equals(GET_STR_CONST(IAB_GOOGLE)))
				#endif
				{
					if (mMKTStatus == MKT_NO_SUPPORTED || !mServerInfo.getBillingType().equals(GET_STR_CONST(IAB_GOOGLE)))
					{
						mIABTrans.showDialog(GMPHelper.DIALOG_BILLING_NOT_SUPPORTED_ID, mItemID);
					}	
					else if (mMKTStatusHk == MKT_FREEDOM_RUNNING || mMKTStatus == MKT_CANNOT_CONNECT)
					{	
						mIABTrans.showDialog(GMPHelper.DIALOG_CANNOT_CONNECT_ID, mItemID);
					}
				}
				else
			#endif
				if (mIABTrans.requestTransaction(mItemID))
				{
					DBG(TAG, "Buying callback enabled");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Transaction started successfully");
				#if BOKU_STORE	
					mBuyItemCallbackEnable = true;
				#endif
				}else
				{
					INFO(TAG, "Sync Fail at buying");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Cannot start transaction");
			
				#if GAMELOFT_SHOP
					mIABTrans.showDialog(GLOFTHelper.DIALOG_CANNOT_CONNECT_ID, mItemID);
				#endif
					
				}
			} catch (Exception e) {
				DBG_EXCEPTION(e);
			}
			DBG(TAG, "msg.what == OP_TRANSACTION end");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "OP_TRANSACTION End");
			return true;
		}
		else if (msg == OP_IAB_IS_SUPPORTED)
		{
			try
			{
				INFO(TAG, "OP_IAB_IS_SUPPORTED");
				if (mIABTrans == null)
				{
				#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
					mIABTrans = new GMPHelper(mServerInfo);
				#elif AMAZON_STORE
					mIABTrans = new AMZHelper(mServerInfo);
				#endif
				}
			#if AMAZON_STORE
				mMKTStatus = MKT_SUPPORTED;
			#endif
				if (!mIABTrans.isBillingSupported())
				{
					ERR(TAG, "Store not supported");
				}
			} catch (Exception e) {
				DBG_EXCEPTION(e);
			}
			return true;
		}
	#if GOOGLE_STORE_V3 || BOKU_STORE	|| AMAZON_STORE || BAZAAR_STORE || YANDEX_STORE || ATET_STORE || HUAWEI_STORE
		else if(msg == OP_IAB_SHOW_DIALOG)
		{
		#if BOKU_STORE
			mIABTrans.showDialog(GLOFTHelper.DIALOG_PURCHASE_RESULT_PENDING, mItemID);
		#endif
			mBuyItemCallbackEnable = false;
			return true;
		}
		else if(msg ==  OP_SEND_CONFIRMATION)
		{
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "OP_SEND_CONFIRMATION Start");
		#if GOOGLE_STORE_V3 ||  VZW_STORE || SAMSUNG_STORE || BAZAAR_STORE || YANDEX_STORE || ATET_STORE || HUAWEI_STORE
			mIABTrans.sendNotifyConfirmation(mNotifyId);
		#endif

			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "OP_SEND_CONFIRMATION End");
			return true;
		}
	#else //GOOGLE_STORE_V3 || BOKU_STORE	|| AMAZON_STORE
		else if(msg ==  OP_SEND_CONFIRMATION)
		{
// 		    InAppBilling.GoogleAnalyticsTrackTransaction();
			return true;
		}
	#endif //GOOGLE_STORE_V3 || BOKU_STORE	|| AMAZON_STORE
		else if(msg == OP_RESTORE_TRANS)
		{
			DBG(TAG, "msg == OP_RESTORE_TRANS");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "OP_RESTORE_TRANS Start");
	#if GOOGLE_STORE_V3 || AMAZON_STORE || SKT_STORE || SAMSUNG_STORE || KT_STORE || GAMELOFT_SHOP || BAZAAR_STORE || YANDEX_STORE
			if (mServerInfo == null)
				mServerInfo = new ServerInfo();
				
			if (mIABTrans == null)
				{
				#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
					mIABTrans = new GMPHelper(mServerInfo);
				#endif //GOOGLE_STORE_V3
				
				#if AMAZON_STORE
					mIABTrans = new AMZHelper(mServerInfo);
				#endif //AMAZON_STORE

				#if SKT_STORE
					mIABTrans = new SKTHelper(mServerInfo);
				#endif //SKT_STORE
				
				#if SAMSUNG_STORE
					mIABTrans = new SamsungHelper(mServerInfo);
				#endif//SAMSUNG_STORE
				
				#if KT_STORE
					mIABTrans = new KTHelper(mServerInfo);
				#endif //KT_STORE
				
				#if GAMELOFT_SHOP
					mIABTrans = new GLOFTHelper(mServerInfo);
				#endif//GAMELOFT_SHOP

				#if ATET_STORE
					mIABTrans = new ATETHelper(mServerInfo);
				#endif
				
				}
		#if SKT_STORE || SAMSUNG_STORE || KT_STORE
				mServerInfo.searchForDefaultBillingMethod(mLastItemIdFromList);
		#endif
#if GAMELOFT_SHOP
			mIABTrans.restoreOnlineTransactions();
#endif//GAMELOFT_SHOP
		
			mIABTrans.restoreTransactions();
	#endif
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "OP_RESTORE_TRANS End");
		return true;
		}
		return false;
	}
		

	private static IABTransaction mIABTrans;
	
	static boolean mBuyItemCallbackEnable = false;
#if GOOGLE_STORE_V3 || AMAZON_STORE || BAZAAR_STORE || YANDEX_STORE
	public static final int MKT_UNKNOWN		    = -1;
	public static final int MKT_SUPPORTED 		= 0;
	public static final int MKT_NO_SUPPORTED	= 1;
	public static final int MKT_CANNOT_CONNECT	= 2;
	public static final int MKT_FREEDOM_RUNNING	= 3;
	public static int mMKTStatus 				= MKT_UNKNOWN;
	public static int mMKTStatusHk  			= MKT_UNKNOWN;
#endif //#if GOOGLE_STORE_V3 || AMAZON_STORE	
	
	public static Bundle getData(Bundle bundle)
	{
	
		
		final int op 		= bundle.getInt(GET_STR_CONST(IAB_OPERATION));
		
		
		if(op != OP_IAB_SHOW_DIALOG && 
		   op != OP_IAB_LAST_RESULTS &&
		   op != OP_GET_ID_VALUES &&
		   op != OP_GET_TYPE_VALUES &&
		   op != OP_GET_ITEM_ATTS &&
		   op != OP_GET_BILLING_ATTS &&
		   op != OP_GET_SHOP_ATTS &&
		   op != OP_GET_TYPE_WUID &&
		   op != OP_GET_ATT_WUID &&
		   op != OP_GET_BATT_WUID &&
		   op != OP_RESTORE_TRANS &&
		   op != OP_GET_COMMON_HEADERS)
		{
			if (op == OP_TRANSACTION)
				mItemID 	= bundle.getString(GET_STR_CONST(IAB_ITEM));
			
			if (op == OP_RETRIEVE_LIST)
				mReqList 	= bundle.getString(GET_STR_CONST(IAB_LIST));
			
			if (op == OP_SEND_CONFIRMATION)
				mNotifyId 	= bundle.getString(GET_STR_CONST(IAB_NOTIFY_ID));
		}

		

			if (op == OP_RETRIEVE_LIST ||
				op == OP_TRANSACTION ||
				op == OP_IAB_IS_SUPPORTED ||
				op == OP_IAB_SHOW_DIALOG ||
				op == OP_RESTORE_TRANS ||
				op == OP_SEND_CONFIRMATION )
			{
				new Thread(
					new Runnable() {
						public void run() {
							try 
							{
								//new Handler(new MyHandlerCallback()).sendEmptyMessage(op);
								handleOperations(op);
							} catch (Exception e) {
								DBG_EXCEPTION(e);
							}
						}
					}
				#if !RELEASE_VERSION
				,"Thread-Operations-"+op
				#endif
					).start();
			}
		bundle = getDataAid(op, bundle);
		return bundle;
	}
	
	private static Bundle getDataAid(int op, Bundle bundle)
	{
		if(op == OP_IAB_LAST_RESULTS)
		{

			//String id = mItemID;
			String id = getLastItem();
			String Desc = GRS(IAB_STR_EMPTY);
			if (id != null)
			{
				cItem item = mServerInfo.getItemById(id);
				if (item != null)
				{
					Desc = item.getAttributeByName(GET_STR_CONST(IAB_NAME));
				}
			}
			bundle.putByteArray(GET_STR_CONST(IAB_RESULT),(Desc!=null)?Desc.getBytes():null);
			bundle.putInt(GET_STR_CONST(IAB_STATUS), getLastState());
		}
		else if (op == OP_GET_ID_VALUES)
		{
			int idx = bundle.getInt(GET_STR_CONST(IAB_INDEX));
			if (idx >= 0)
			{
				byte value[] = mServerInfo.getItemIdByIdx(mReqList, idx);
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		}
		else if (op == OP_GET_TYPE_VALUES)
		{
			int idx = bundle.getInt(GET_STR_CONST(IAB_INDEX));
			if (idx >= 0)
			{
				byte value[] = mServerInfo.getItemTypeByIdx(mReqList, idx);
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		}
		else if (op == OP_GET_ITEM_ATTS)
		{
			String att = bundle.getString(GET_STR_CONST(IAB_NAME));
			int idx = bundle.getInt(GET_STR_CONST(IAB_INDEX));
			if (att!= null && idx >= 0)
			{
				byte value[] = mServerInfo.getAttributeByNameIdx(mReqList, att, idx);
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		}
		else if (op == OP_GET_BILLING_ATTS)
		{
			String att = bundle.getString(GET_STR_CONST(IAB_NAME));
			int idx = bundle.getInt(GET_STR_CONST(IAB_INDEX));
			if (att!= null && idx >= 0)
			{
				byte value[] = mServerInfo.getBillingAttByNameIdx(mReqList, att, idx);
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		}
		else if (op == OP_GET_SHOP_ATTS)
		{	
			String att = bundle.getString(GET_STR_CONST(IAB_NAME));
			if (att!= null)
			{
				byte value[] = null;
				if (att.equals(GET_STR_CONST(IAB_PROMO_DESCRIPTION)))
					value = mServerInfo.getPromoDescription();
				else if	(att.equals(GET_STR_CONST(IAB_PROMO_END_TIME)))
					value = mServerInfo.getPromoEndTime();
				else if	(att.equals(GET_STR_CONST(IAB_PROMO_SERVER_TIME)))
					value = mServerInfo.getPromoServerTime();
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		
		}
		else if (op == OP_GET_TYPE_WUID)
		{	
			String uid = bundle.getString(GET_STR_CONST(IAB_UID));
			if (uid != null)
			{
				byte value[] = null;
				
				String tmp = mServerInfo.getItemType(uid);
				value = (tmp == null)?null:tmp.getBytes();
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		}
		else if (op == OP_GET_ATT_WUID)
		{	
			String att = bundle.getString(GET_STR_CONST(IAB_NAME));
			String uid = bundle.getString(GET_STR_CONST(IAB_UID));
			if (att!= null && uid != null)
			{
				byte value[] = null;
				
				String tmp = mServerInfo.getItemAttribute(uid, att);
				value = (tmp == null)?null:tmp.getBytes();
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		}
		else if (op == OP_GET_BATT_WUID)
		{	
			String att = bundle.getString(GET_STR_CONST(IAB_NAME));
			String uid = bundle.getString(GET_STR_CONST(IAB_UID));
			if (att!= null && uid != null)
			{
				byte value[] = null;
				
				String tmp = mServerInfo.getBillingAttribute(uid, att);
				value = (tmp == null)?null:tmp.getBytes();
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			}
		}
		else if (op == OP_GET_COMMON_HEADERS)
		{	
			try{
				JSONObject jObj		= new JSONObject();
				JSONObject jHdrs	= new JSONObject();
				JSONObject jQry		= new JSONObject();
				
				Device device = new Device();
				byte value[] = null;
				
				jHdrs.put("x-up-gl-acnum",GET_GGLIVE_UID());
				jHdrs.put("x-up-gl-subno",device.getLineNumber());
			#if (HDIDFV_UPDATE != 2)
				jHdrs.put("x-up-gl-imei",device.getIMEI());
			#endif
			#if HDIDFV_UPDATE
				DBG("I_S"+HDIDFV_UPDATE, device.getHDIDFV());
				jHdrs.put("x-up-gl-hdidfv",device.getHDIDFV());
				jHdrs.put("x-up-gl-gldid",device.getGLDID());
			#endif
				jHdrs.put("x-up-gl-sim-operator",device.getSimOperator());
				jHdrs.put("x-up-gl-sim-operator-name",device.getSimOperatorName());
				jHdrs.put("x-up-gl-sim-country-iso",device.getSimCountryIso());
				jHdrs.put("x-up-gl-network-operator",device.getNetworkOperator());
				jHdrs.put("x-up-gl-network-operator-name",device.getNetworkOperatorName());
				jHdrs.put("x-up-gl-network-country-iso",device.getNetworkCountryIso());
				jHdrs.put("x-up-gl-is-network-roaming",Boolean.toString(device.getIsRoaming()));
				jObj.put("scc",Integer.toString(device.createUniqueCode()));
				jObj.put("lang",device.getLocale());
				
				String values 	= mServerInfo.getShopProfile().getVLimitsURL();
				if (!TextUtils.isEmpty(values))
				{
					String url 		= values.substring(0, values.indexOf("?"));
					String portal 	= values.substring(values.indexOf("=")+1,values.length());
					
					String qry 	= values.substring(values.indexOf("?")+1,values.length());
					do
					{
						String aname  = qry.substring(0,qry.indexOf("="));
						String avalue = qry.substring(qry.indexOf("=")+1,(qry.indexOf("&")>=0?qry.indexOf("&"):qry.length()));
						jQry.put(aname,avalue);
						qry = qry.substring((qry.indexOf("&")>=0?qry.indexOf("&")+1:qry.length()),qry.length());
					}while(!TextUtils.isEmpty(qry));
					
					JDUMP(TAG, url);
					JDUMP(TAG, portal);
					jObj.put("url",url);
					jObj.put("portal",portal);
				}
				else
					ERR(TAG, "The 'limits_validation_url' field is missing in the content feed");
				
				jObj.put("headers",jHdrs);
				jObj.put("queryData",jQry);
				value = jObj.toString().getBytes();
				JDUMP(TAG,jObj.toString());
				bundle.putByteArray(GET_STR_CONST(IAB_RESULT),value);
			} catch(Exception ex) {DBG_EXCEPTION(ex);}
		}else if (op == OP_CLOSE_WAIT_DIALOG)
		{	
			DBG(TAG, "msg.what == OP_CLOSE_WAIT_DIALOG");
			closeBackgroundDialog();
		}
		return bundle;
	}
	
	private static String getSDFolder() 
	{
	#if USE_INSTALLER
		String result = SUtils.getPreferenceString("SDFolder",GameInstaller.mPreferencesName);
		if (result != "")
			return result;
		else
	#endif
		return SD_FOLDER;
	}
	
	//GET_STR_CONST|GET_FQCN|GET_GGLIVE_UID|GET_CREDENTIALS|GET_FED_DATA_CENTER
	public static String a(int op, int idx)
	{
		Bundle bundle = new Bundle();
		
		bundle.putInt("O", op);
		bundle.putInt("I", idx);
		bundle = nativeSendData(bundle);
		if (bundle != null)
			return bundle.getString("R");
		return null;
	}
	
	//LOGGING_LOG_INFO
	public static String c(int op, int level, int status, String data)
	{
		Bundle bundle = new Bundle();
		bundle.putInt("O", op);
		bundle.putInt("L", level);
		bundle.putInt("S", status);
		
		if (data != null) 
			bundle.putByteArray(GET_STR_CONST(IAB_NAME), data.getBytes());
		
		bundle = nativeSendData(bundle);
		if (bundle != null)
			return bundle.getString("R");
		return null;
	}
	
	public static void closeBackgroundDialog()
	{
	#if GAMELOFT_SHOP && USE_IAP_BG_SCREEN
		if(waiting_dialog != null)
		{
			waiting_dialog.dismiss();
			waiting_dialog = null;
		}
	#endif
	}
	
	static IABDialog waiting_dialog = null;
	public static void showBackgroundDialog()
	{
	#if GAMELOFT_SHOP && USE_IAP_BG_SCREEN
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				final int idstr 	= R.string.IAB_PURCHASE_IN_PROGRESS;
				waiting_dialog = IABDialog.createDialog(((Activity)SUtils.getContext()), IABDialog.GLD_WAIT_MESSAGE, idstr);
				waiting_dialog.setButtonVisibility(IABDialog.BUTTON_NEUTRAL,View.INVISIBLE);
				waiting_dialog.setCanceledOnTouchOutside(false);
				waiting_dialog.setCancelable(false);
				waiting_dialog.show();	
			}
		});
	#endif
	}
	
	public static native void nativeInit(Context context);
	public static native void nativeSetContext(Context context);
	public static native void nativeSetIABObject(InAppBilling obj);
	public static native Bundle nativeSendData(Bundle bundle);
}

#undef GES(id)
#undef GRS(id)
#endif
