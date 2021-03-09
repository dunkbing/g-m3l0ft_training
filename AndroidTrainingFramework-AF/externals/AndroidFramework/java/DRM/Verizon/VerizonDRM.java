package APP_PACKAGE.DRM.Verizon;

import APP_PACKAGE.R;
import com.verizon.vcast.apps.LicenseAuthenticator;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Calendar;
import java.util.Random;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import APP_PACKAGE.GLUtils.Device;

public class VerizonDRM {

	final String GAME_CODE				= GGC_GAME_CODE;
	final String PREF_NAME				= "VRZ";
	final int MAX_FREE					= 2;
	
	
	// ERROR_CONTENT_HANDLER 
          // V CAST Apps is not installed on user's device or there is a problem connecting to it. 
	// ERROR_GENERAL 
          // General error occurred. 
	// ERROR_ILLEGAL_ARGUMENT 
          // The keyword supplied is empty or null. 
	// ERROR_SECURITY 
          // License validation is not authorized, likely due to missing permissions in the calling app's AndroidManifest.xml file. 
	// ERROR_UNABLE_TO_CONNECT_TO_CDS 
          // V CAST Apps was unable to connect to the Content Delivery Server. 
	// ITEM_NOT_FOUND 
          // This application is no longer available in the V CAST Apps catalog for sale. 
	// LICENSE_NOT_FOUND 
          // License is either expired or user has never purchased this application. 
	// LICENSE_OK 
          // License for this application was successfully validated. 
	// LICENSE_TRIAL_OK 
          // License for this application was successfully validated. 
	// LICENSE_VALIDATION_FAILED 
          // License is either expired or user has never purchased this application. 
	static final int TEST_VALUE				= LicenseAuthenticator.LICENSE_OK;
	
	final boolean ENABLE_SHOP_AVALIABLE_TEST = true;
	// static final int RELEASE_DATE_YEAR		= 2011;
	// static final int RELEASE_DATE_MONTH		= 1;
	// static final int RELEASE_DATE_DAY		= 1;
	
	private Context mContext;
	LicenseAuthenticator mVerizonLicense 	= null;
	int mLicenseStatus;
	int mFreeCount;
	
	
	String mDeviceID;
	SharedPreferences mSettings;
	StringEncrypter mEncrypter;
	Random mRandom;
	XPlayer mXplayer;
	public VerizonDRM(Context context)
	{
		mContext = context;
		
		mVerizonLicense = new LicenseAuthenticator( mContext ); 
		mRandom 	= new Random(System.currentTimeMillis());			
		mDeviceID 	= Device.getDeviceId();
		mEncrypter 	= new StringEncrypter(GAME_CODE+mDeviceID);
		mSettings 	= mContext.getSharedPreferences(PREF_NAME, 0);
		mXplayer 	= new XPlayer();
		
		initSettings();
	}
	
	public void addFreeCount()
	{
		if(mLicenseStatus != LicenseAuthenticator.LICENSE_OK && mLicenseStatus != LicenseAuthenticator.LICENSE_TRIAL_OK)
		{
			mFreeCount++;
			saveSettings();
		}
	}
	
	
	private String getEncrypt(String value)
	{
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + value;
		encode = mEncrypter.encrypt(encode);
		
		//d("encrypt: "+value+"-"+encode);
		return encode;
	}
	private String getEncrypt(int value)
	{
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + value;
		encode = mEncrypter.encrypt(encode);
		//d("encrypt: "+value+"-"+encode);

		return encode;
	}
	
	private String getValue(String key, String errorValue)
	{

		String decode = mSettings.getString(key , errorValue);
		
		try
		{
			if(decode != errorValue)
			{
				decode = mEncrypter.decrypt(decode);
				String[] values = decode.split("#");

				if( (values.length == 3) && (values[1].compareTo(mDeviceID) == 0) )
				{
					decode = values[2];
				}
			}
		} catch(Exception e) {	d(e); }
		
		return decode;
	}
	
	private int getValue(String key, int errorValue)
	{

		String decode = mSettings.getString(key , null);
		int ret = errorValue;

		try
		{
			if(decode != null)
			{
				decode = mEncrypter.decrypt(decode);
				String[] values = decode.split("#");
					
				if( (values.length == 3) && (values[1].compareTo(mDeviceID) == 0) )
				{
					ret = Integer.parseInt(values[2]);
				}
			}
		} catch(Exception e) {	d(e); }
		
		return ret;
	}
	
	private String encryptFreeCount()
	{
		return getEncrypt(mFreeCount);
	}
	private int decryptFreeCount()
	{
		return getValue("vzw_l",MAX_FREE);
	}
	
	private String encryptKey()
	{
		return getEncrypt(mKey);
	}
	private int decryptKey()
	{
		return getValue("vzw_u", -1);
	}
	
	private String getEncryptString()
	{
		String str = mEncrypter.encrypt(mRandom.nextLong()+"#"+mFreeCount+"#"+ mRandom.nextLong());
		//d("getEncryptString: "+str);
		return str;
	}
	
	private void saveSettings()
	{
		d("saveSettings");
		
		SharedPreferences.Editor editor = mSettings.edit();
	
		editor.putString("vzw_a", getEncryptString());
		editor.putString("vzw_b", getEncryptString());
		editor.putString("vzw_c", getEncryptString());
		editor.putString("vzw_d", getEncryptString());
		editor.putString("vzw_e", getEncryptString());
		editor.putString("vzw_f", getEncryptString());
		editor.putString("vzw_g", getEncryptString());
		editor.putString("vzw_h", getEncryptString());
		editor.putString("vzw_i", getEncryptString());
		editor.putString("vzw_j", getEncryptString());
		editor.putString("vzw_k", getEncryptString());
		editor.putString("vzw_l", encryptFreeCount());
		
		editor.putString("vzw_m", getEncryptString());
		editor.putString("vzw_n", getEncryptString());
		editor.putString("vzw_o", getEncryptString());
		editor.putString("vzw_p", getEncryptString());
		editor.putString("vzw_q", getEncryptString());
		editor.putString("vzw_r", getEncryptString());
		editor.putString("vzw_s", getEncryptString());
		editor.putString("vzw_t", getEncryptString());
		editor.putString("vzw_u", encryptKey());
		
		editor.commit();
		
		
		//d("END saveSettings");
	}
	
	int mKey;
	private void initSettings()
	{
		//d("initSettings");
		
		String tmp = mSettings.getString("vzw_q", null);
		if(tmp == null)
		{
			mFreeCount = 0;
			mKey = mXplayer.createUniqueCode();
			saveSettings();
		} else
		{
			mFreeCount	= decryptFreeCount();
			mKey 		= decryptKey();
			mXplayer.setKey(mKey);
		}
	}
	public boolean checkLicense()
	{
		
#if TEST_VERIZON_DRM
		mLicenseStatus = mVerizonLicense.checkTestLicense(GAME_CODE, TEST_VALUE);
#else
		//enable only for final build
		mLicenseStatus = mVerizonLicense.checkLicense(GAME_CODE);
#endif 
		
		d("license status: "+mLicenseStatus);
		return (mLicenseStatus == LicenseAuthenticator.LICENSE_OK || mLicenseStatus == LicenseAuthenticator.LICENSE_TRIAL_OK);
	}
	
	private boolean isShopAvaliable()
	{
	
		if(ENABLE_SHOP_AVALIABLE_TEST)
		{
			try {
				mXplayer.sendIsShopAvaliableRequest();
				
				while(!mXplayer.handleIsShopAvaliable());
				
				d("shop is avaliable: "+mXplayer.mShopAvaliable);
				return mXplayer.mShopAvaliable;
				
			} catch (Exception e) {	d(e);	} 
			
			d("shop is not avaliable");
			return false;
		}
		
		return true;
	}
	
	private boolean isLicenseFail()
	{
		return  (mLicenseStatus == LicenseAuthenticator.ITEM_NOT_FOUND) || (mLicenseStatus == LicenseAuthenticator.LICENSE_NOT_FOUND)
			|| (mLicenseStatus == LicenseAuthenticator.LICENSE_VALIDATION_FAILED);
	}
	public boolean canPlayFree()
	{
		//d("canPlayFree");
		//return ((mFreeCount < MAX_FREE) || !isShopAvaliable()) && !isLicenseFail();
		return false;
	}
	
	public int getLicenseStatus()
	{
		//d("getLicenseStatus");
		return mLicenseStatus;
	}
	
	public int getStringErrorID()
	{
		int res = 0;
		switch(mLicenseStatus)
		{
			case LicenseAuthenticator.ITEM_NOT_FOUND:
				res = R.string.VZW_ITEM_NOT_FOUND;
				break;
			case LicenseAuthenticator.LICENSE_NOT_FOUND:
			case LicenseAuthenticator.LICENSE_VALIDATION_FAILED:
				res = R.string.INVALID_LICENSE;
				break;
			
			// case LicenseAuthenticator.ERROR_CONTENT_HANDLER:
			// case LicenseAuthenticator.ERROR_UNABLE_TO_CONNECT_TO_CDS:
			// if(!canPlayFree())
				// {
					// res = R.string.VZW_SERVER_VALIDATION_REQUIRED;
				// }else
					// res = R.string.VZW_GENERAL_ERROR;
				// break;
			// case LicenseAuthenticator.ERROR_GENERAL:
			// case LicenseAuthenticator.ERROR_ILLEGAL_ARGUMENT:
			// case LicenseAuthenticator.ERROR_SECURITY:
			default:
			{
				
					res = R.string.VZW_GENERAL_ERROR;
				
				
				break;
			}
		}
		
		return res;		
	}
	


	public static boolean DEBUG = !false && (RELEASE_VERSION == 0);	
	private static void d(String msg)
	{
		if(DEBUG)
		{
			DBG("VerizonDRM",msg);
		}
	}
	private static void d(Exception e)
	{
		if(DEBUG)
		{
			DBG_EXCEPTION(e);
		}
	}
}
