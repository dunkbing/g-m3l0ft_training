package APP_PACKAGE.DRM.TMobile;

import APP_PACKAGE.R;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Calendar;
import java.util.Random;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.io.DataInputStream;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import APP_PACKAGE.GLUtils.Device;

import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.ConnectivityManager;

public class TMobileDRM {

	final String GAME_CODE = GGC_GAME_CODE;
	final String PREF_NAME = "TMobile";
	final int MAX_FREE     = 49;
	final int MAX_CHECK    = 8;
	
	
	public final int VALID_LICENSE	 			      = 0;
	public final int INVALID_LICENSE 			      = 1;
	public final int VALIDATION_SERVER_REQUIRED = 2;
	
	private String MOBILE			    = "";
	private String SERVER_PIRACY	= "";
	
	
	private Context mContext;
	int mLicenseStatus;
	int mFreeCount;
	int mCheckCount;
	int mLicenseStatusSaved;
	
	
	String mDeviceID;
	SharedPreferences mSettings;
	StringEncrypter mEncrypter;
	Random mRandom;

	public final int NETWORK_TIMEOUT			= 50*1000;
	public long sTimeOut;
	//boolean s_bisMobileNetworkReady				= false;
	WifiManager m_wifiManager					= null;
	boolean s_bwasWifiEnable					= false;
	ConnectivityManager mConnectivityManager	= null;

	public TMobileDRM(Context context)
	{
		mContext = context;
		
		mRandom 	    = new Random(System.currentTimeMillis());
		mDeviceID 	  = Device.getDeviceId();
		mEncrypter 	  = new StringEncrypter(GAME_CODE+mDeviceID);
		mSettings 	  = mContext.getSharedPreferences(PREF_NAME, 0);
		MOBILE = Device.getLineNumber();
		// To prevent SIM problems
		// if(MOBILE == null){
		if(MOBILE.equals("00")){
			MOBILE = "0000000000";
		}
		//trunk mobile number to 10 digits. avoid international roaming digits.
		if(MOBILE.length() > 10)
        {
          MOBILE = MOBILE.substring(MOBILE.length() - 10, MOBILE.length());
        }
		
		SERVER_PIRACY = mContext.getString(R.string.TMO_LICENSE);
		
		initSettings();
	}

	public void updateTimer(long time)
	{
		sTimeOut -= time;
	}
	public boolean isWaitting()
	{
		return (IsMobileNetworkReady() == 0 && sTimeOut > 0);
	}
	/// Check airplane mode
	public boolean isAirplaneModeOn()
	{		
		boolean isAirplaneOn = Settings.System.getInt(mContext.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		DBG("TMobileDRM", "Is Airplane Mode On? = " + isAirplaneOn);
		return isAirplaneOn;
	}

	public void InitMobileNetwork()
	{
		DBG("TMobileDRM", "Waitting for Mobile Network");
		sTimeOut = NETWORK_TIMEOUT;
		
		m_wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE); 
		if((m_wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED))
		{
			s_bwasWifiEnable = true;
			m_wifiManager.setWifiEnabled(false);
		}
	}

	int IsMobileNetworkReady()
	{
		if(IsMobileNetwork())
		{
			return 1;
		}
		return 0;
	}

	public boolean IsMobileNetwork()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		//DBG("TMobileDRM", "Is Mobile Network On ? " + (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED));
		return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED);
	}

	public void RestoreNetworkState()
	{
		if(s_bwasWifiEnable)
		{
			DBG("TMobileDRM", "Restore Network State");
			m_wifiManager.setWifiEnabled(true);
			s_bwasWifiEnable = false;
		}
	}

	public void addFreeCount()
	{
		if(mLicenseStatus != VALID_LICENSE && mLicenseStatus != INVALID_LICENSE)
		{
			mFreeCount++;
			saveSettings();
		}
	}

	public void addCheckCount()
	{
		mCheckCount++;
		if(mCheckCount > MAX_CHECK || isLicenseFail() || isLicenseFailNetwork())
			mCheckCount = 0;
		saveSettings();
	}

	public void debug(final String server)
	{
		String res = server;
		DBG("TMobileDRM", "Debug: " + res);
	}
		
	private void decryptFreeCount()
	{
// 		DBG("TMobileDRM", "Decrypt Free Count: ");
		String decode = mSettings.getString("tmo_l", null);
		
// 		DBG("TMobileDRM", "To Decode: " + decode);
		try
		{
			if(decode != null)
			{
				decode = mEncrypter.decrypt(decode);
				
				String[] values = decode.split("#");
				
// 				for(int i=0; i<values.length; i++)
// 				  DBG("TMobileDRM", "values: " + values[i]);
					
				if( (values.length == 3 || values.length == 4 || values.length == 5 || values.length == 6) && (values[1].compareTo(mDeviceID) == 0) )
				{
					mFreeCount = Integer.parseInt(values[2]);
					DBG("TMobileDRM", "freeCount: " + mFreeCount);
					mCheckCount = Integer.parseInt(values[3]);
					DBG("TMobileDRM", "checkCount: " + mCheckCount);
					mLicenseStatusSaved = Integer.parseInt(values[4]);
					DBG("TMobileDRM", "mLicenseStatus: " + mLicenseStatusSaved);
				}else
				{
					mFreeCount = MAX_FREE;
					DBG("TMobileDRM", "Invalid Save, freeCount: " + MAX_FREE);
					mCheckCount = MAX_CHECK;
					DBG("TMobileDRM", "Invalid Save, checkCount: " + MAX_CHECK);
				}
			}
		} catch(Exception e) {	DBG_EXCEPTION(e); }
	}
	
	private String encryptFreeCount()
	{
// 		DBG("TMobileDRM", "Encrypt Free Count: ");
		String NAME = "K"+"l";
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + mFreeCount + "#" + mCheckCount + "#" + mLicenseStatusSaved/* + "#" + mNoNetwork*/;
// 		DBG("TMobileDRM", "To Encode: " + encode);
		encode = mEncrypter.encrypt(encode);
// 		DBG("TMobileDRM", "Encoded: " + encode);

		return encode;
	}
	
	private String encryptKey()
	{
// 		DBG("TMobileDRM", "Encrypt Key: ");
		String NAME = "K"+"d";
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + mRandom.nextInt();
// 		DBG("TMobileDRM", "Encrypt Key To Encode: " + encode);
		encode = mEncrypter.encrypt(encode);
// 		DBG("TMobileDRM", "Encoded: " + encode);

		return encode;
	}
	
	public String decryptKey()
	{
		String decode = mSettings.getString("tmo_d", null);
		String value="";
		try
		{
			if(decode != null)
			{
				decode = mEncrypter.decrypt(decode);
				
				String[] values = decode.split("#");
				
					
				if( (values.length == 3) && (values[1].compareTo(mDeviceID) == 0) )
				{
					value =  values[2];
				}else
				{
					value = "";
// 					DBG("TMobileDRM", "Invalid Save, freeCount: " + MAX_FREE);
				}
// 				DBG("TMobileDRM", "Decrypt Key - Key: " + value);
			}
		} catch(Exception e) {	DBG_EXCEPTION(e); }
		
		
		return value;
	}
	
	private String getEncryptString()
	{
		String str = mEncrypter.encrypt(mRandom.nextLong()+"#"+mFreeCount+"#"+ mRandom.nextLong()+"#"+mCheckCount+"#"+ mRandom.nextLong()+"#"+mLicenseStatusSaved+"#"+ mRandom.nextLong()/*+"#"+mNoNetwork+"#"+ mRandom.nextLong()*/);
// 		DBG("TMobileDRM", "Get Encrypt String: " + str);
		return str;
	}
	
	//Test, create some dummy functions
	#define DEF_GetString(NAME)	private String getEncryptString##NAME() { String str = mEncrypter.encrypt(mRandom.nextLong()+"#"+mFreeCount+"#"+ mRandom.nextInt()+"#"+mRandom.nextInt()); return str; }
		DEF_GetString(a)
		DEF_GetString(b)
		DEF_GetString(c)
		DEF_GetString(d)
		DEF_GetString(e)
		DEF_GetString(f)
		DEF_GetString(g)
		DEF_GetString(h)
		DEF_GetString(i)
		DEF_GetString(j)
		DEF_GetString(k)
		DEF_GetString(l)
		DEF_GetString(m)
		DEF_GetString(n)
		DEF_GetString(o)
		DEF_GetString(p)
		DEF_GetString(q)
		DEF_GetString(r)
		DEF_GetString(s)
		DEF_GetString(t)
	#define FNC_GetString(NAME) getEncryptString##NAME()
	
	private String encryptServer()
	{
		String NAME = "K"+"s";
		String str = mEncrypter.encrypt(SERVER_PIRACY);
// 		DBG("TMobileDRM", "Encrypt Server: " + str);
		return str;
	}
	
	private void decryptServer()
	{
		String decode = mSettings.getString("tmo_s", null);
		if(decode != null)
		{
			decode = mEncrypter.decrypt(decode);
		}
// 		DBG("TMobileDRM", "Decrypt Server: " + decode);
	}
	
	private void saveSettings()
	{
		DBG("TMobileDRM", "Save Settings");
		
		SharedPreferences.Editor editor = mSettings.edit();
	
		editor.putString("tmo_a", FNC_GetString(a));
		editor.putString("tmo_b", FNC_GetString(b));
		editor.putString("tmo_c", FNC_GetString(c));
		editor.putString("tmo_d", encryptKey());
		editor.putString("tmo_e", FNC_GetString(e));
		editor.putString("tmo_f", FNC_GetString(f));
		editor.putString("tmo_g", FNC_GetString(g));
		editor.putString("tmo_h", FNC_GetString(h));
		editor.putString("tmo_i", FNC_GetString(i));
		editor.putString("tmo_j", FNC_GetString(j));
		editor.putString("tmo_k", FNC_GetString(k));
		editor.putString("tmo_l", encryptFreeCount());
		editor.putString("tmo_m", FNC_GetString(m));
		editor.putString("tmo_n", FNC_GetString(n));
		editor.putString("tmo_o", FNC_GetString(o));
		editor.putString("tmo_p", FNC_GetString(p));
		editor.putString("tmo_q", FNC_GetString(q));
		editor.putString("tmo_r", FNC_GetString(r));
		editor.putString("tmo_s", encryptServer());
		editor.putString("tmo_t", FNC_GetString(t));
		
		editor.commit();

		decryptServer();
		
// 		DBG("TMobileDRM", "END Save Settings");
	}
	
	private void initSettings()
	{
		DBG("TMobileDRM", "Init Settings");
		
		String tmp = mSettings.getString("tmo_a", null);
		if(tmp == null)
		{
			//init for first time
			mFreeCount = 0;
			mCheckCount = 0;
			mLicenseStatusSaved = -1;
			saveSettings();
		} else
		{
			decryptFreeCount();
		}
	}

	public boolean checkLicenseSaved()
	{
		DBG("TMobileDRM", "Check License Saved");
		if(mLicenseStatusSaved == VALID_LICENSE || mLicenseStatusSaved == INVALID_LICENSE)
			mFreeCount = 0;
		return (mLicenseStatusSaved == VALID_LICENSE);
	}
	
	//TODO change this with new SERVER implementation ... Waitting for Pascal :S
	public boolean checkLicense()
	{
		DBG("TMobileDRM", "Check License");
		mLicenseStatus 	= INVALID_LICENSE;
	
		try
		{
			//String serverURL = SERVER_PIRACY.replace("{M}",MOBILE).replace("{E}",TMO_ITEM_ID);	//did you miss configure TMO_ITEM_ID?
			String serverURL = SERVER_PIRACY.replace("{E}",TMO_ITEM_ID);	//did you miss configure TMO_ITEM_ID?
			DBG("TMobileDRM", "Server URL: " + serverURL);
											
			URL url = new URL(serverURL);
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");	http.getResponseCode();           
			InputStream is = http.getInputStream();
					
			byte[] buf = new byte[250];
			is.read(buf);
			String responseBody = new String(buf);
			DBG("TMobileDRM", "Response Body: " + responseBody);
					
			boolean license = (responseBody.substring(0,1).compareTo("1") == 0);
					
			if(license)	//server return a valid key
			{
				DBG("TMobileDRM", "License Valid Saving");
				saveSettings();
				mLicenseStatus = VALID_LICENSE;
			}else 		//this key is already register or another error
			{
				DBG("TMobileDRM", "License Invalid Saving");
				mLicenseStatus 	= INVALID_LICENSE;
			}
		}catch(java.net.UnknownHostException unknown)
		{
			DBG("TMobileDRM", "Server unreachable - Unknown Host Exception");
			mLicenseStatus 	= VALIDATION_SERVER_REQUIRED;
		}catch(java.net.SocketException unknown)
		{
			DBG("TMobileDRM", "Server unreachable - Socket Exception");
			mLicenseStatus 	= VALIDATION_SERVER_REQUIRED;
		}catch(java.net.SocketTimeoutException unknown)
		{
			DBG("TMobileDRM", "Server unreachable - Socket Timeout Exception");
			mLicenseStatus 	= VALIDATION_SERVER_REQUIRED;
		}
		catch(Exception e)
    {
    	DBG_EXCEPTION(e);
			DBG("TMobileDRM", "Server unreachable - Exception");
			mLicenseStatus 	= VALIDATION_SERVER_REQUIRED;
    }

    DBG("TMobileDRM", "mLicenseStatus: " + mLicenseStatus);
    if(mLicenseStatus != VALIDATION_SERVER_REQUIRED)
    {
      mLicenseStatusSaved = mLicenseStatus;
		  saveSettings();
		}
    DBG("TMobileDRM", "mLicenseStatusSaved: " + mLicenseStatusSaved);

		return (mLicenseStatus == VALID_LICENSE);
	}
	
	private boolean isLicenseFail()
	{
		return  (mLicenseStatusSaved == INVALID_LICENSE);//||;
	}

	private boolean isLicenseFailNetwork()
	{
		return  (mLicenseStatus == VALIDATION_SERVER_REQUIRED);//||;
	}

	public boolean canPlayFree()
	{
		DBG("TMobileDRM", "Can Play Free: " + ((mFreeCount < MAX_FREE) && !isLicenseFail() && mLicenseStatusSaved != -1));
		return ((mFreeCount < MAX_FREE) && !isLicenseFail() && mLicenseStatusSaved != -1);
	}

	public boolean canCheck()
	{
		DBG("TMobileDRM", "Can Check: " + ((mCheckCount == 0 || mCheckCount == 4) && (!isLicenseFail() || !isLicenseFailNetwork())));
		return ((mCheckCount == 0 || mCheckCount == 4) && (!isLicenseFail() || !isLicenseFailNetwork()));
	}
	
	public int getLicenseStatus()
	{
// 		DBG("TMobileDRM", "Get License Status");
		return mLicenseStatus;
	}
	
	public int getStringErrorID()
	{
		int res = 0;
		switch(mLicenseStatus)
		{
			case INVALID_LICENSE:
				res = R.string.INVALID_LICENSE;
				break;
			
			case VALIDATION_SERVER_REQUIRED:
				res = R.string.TMO_SERVER_VALIDATE_REQUIRED;
				break;
		}
		
		return res;		
	}
	
}
