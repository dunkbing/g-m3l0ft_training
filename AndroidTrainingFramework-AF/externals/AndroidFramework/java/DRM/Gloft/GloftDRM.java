package APP_PACKAGE.DRM.Gloft;

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

import APP_PACKAGE.GLUtils.Device;

public class GloftDRM{

	final String GAME_CODE				= "gamecode";
	final String PREF_NAME				= "GLoft";
	final int MAX_FREE					= 2;
	
	
	public final int VALID_LICENSE	 			= 0;
	public final int INVALID_LICENSE 			= 1;
	public final int VALIDATION_SERVER_REQUIRED = 2;
	
	private final String	PRODUCT_ID			= GL_PRODUCT_ID; //This must not be hardcode here, please set it at config.bat
	//private final String	SERVER_PIRACY		= "http://confirmation.gameloft.com/partners/android/validate_key.php?key=#KEY#&product=#PRODUCT_ID#&imei=#ID#";
	private final String	SERVER_PIRACY		= "https://secure.gameloft.com/partners/android/validate_key.php?key=#KEY#&product=#PRODUCT_ID#&imei=#ID#";
	
	private Context mContext;
	int mLicenseStatus;
	int mFreeCount;
	
	
	String mDeviceID;
	String mSerialKey;
	SharedPreferences mSettings;
	StringEncrypter mEncrypter;
	Random mRandom;
	
	public GloftDRM(Context context)
	{
		mContext = context;
		
		mRandom 	= new Random(System.currentTimeMillis());
		mDeviceID 	= Device.getDeviceId();
		mEncrypter 	= new StringEncrypter(GAME_CODE+mDeviceID);
		mSettings 	= mContext.getSharedPreferences(PREF_NAME, 0);
		
		initSettings();
	}
	
	
	public void addFreeCount()
	{
		if(mLicenseStatus != VALID_LICENSE)
		{
			mFreeCount++;
			saveSettings();
		}
	}
		
	private void decryptFreeCount()
	{
	
		//d("decryptFreeCount: ");
		String decode = mSettings.getString("gl_l", null);
		
		
		d(" toDecode: "+decode);
		try
		{
			if(decode != null)
			{
				decode = mEncrypter.decrypt(decode);
				
				String[] values = decode.split("#");
				
				// for(int i=0;i<values.length;i++)
					// d(" values: "+values[i]);
					
				if( (values.length == 3) && (values[1].compareTo(mDeviceID) == 0) )
				{
					mFreeCount = Integer.parseInt(values[2]);
					d(" freeCount: "+mFreeCount);
				}else
				{
					mFreeCount = MAX_FREE;
					d("invalid save, freeCount:"+MAX_FREE);
				}
			}
		} catch(Exception e) {	d(e); }
	}
	
	private String encryptFreeCount()
	{
		d("encryptFreeCount: ");
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + mFreeCount;
		d("   toEncode:"+encode);
		encode = mEncrypter.encrypt(encode);
		d("   Encoded:"+encode);

		return encode;
	}
	
	private String encryptKey()
	{
		d("encryptKey: ");
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + mRandom.nextInt()+"#" + mSerialKey;
		d("   toEncode:"+encode);
		encode = mEncrypter.encrypt(encode);
		d("   Encoded:"+encode);

		return encode;
	}
	
	private String decryptKey()
	{
		String decode = mSettings.getString("gl_d", null);
		String value="";
		try
		{
			if(decode != null)
			{
				decode = mEncrypter.decrypt(decode);
				
				String[] values = decode.split("#");
				
					
				if( (values.length == 4) && (values[1].compareTo(mDeviceID) == 0) )
				{
					value =  values[3];
				}else
				{
					value = "";
					//d("invalid save, freeCount:"+MAX_FREE);
				}
				d(" key: "+value);
			}
		} catch(Exception e) {	d(e); }
		
		
		return value;
	}
	
	private String getEncryptString()
	{
		String str = mEncrypter.encrypt(mRandom.nextLong()+"#"+mFreeCount+"#"+ mRandom.nextLong());
		//d("getEncryptString: "+str);
		return str;
	}
	
	private String encryptServer()
	{
		String str = mEncrypter.encrypt(SERVER_PIRACY);
		d("encryptServer: "+str);
		
		return str;
	}
	
	private void decryptServer()
	{
		String decode = mSettings.getString("gl_s", null);
		if(decode != null)
		{
			decode = mEncrypter.decrypt(decode);
		}
		
		d("decryptServer: "+decode);
	}
	
	private void saveSettings()
	{
	
		
		
		
		d("saveSettings");
		
		SharedPreferences.Editor editor = mSettings.edit();
	
		editor.putString("gl_a", getEncryptString());
		editor.putString("gl_b", getEncryptString());
		editor.putString("gl_c", getEncryptString());
		editor.putString("gl_d", encryptKey());
		editor.putString("gl_e", getEncryptString());
		editor.putString("gl_f", getEncryptString());
		editor.putString("gl_g", getEncryptString());
		editor.putString("gl_h", getEncryptString());
		editor.putString("gl_i", getEncryptString());
		editor.putString("gl_j", getEncryptString());
		editor.putString("gl_k", getEncryptString());
		editor.putString("gl_l", encryptFreeCount());
		editor.putString("gl_m", getEncryptString());
		editor.putString("gl_n", getEncryptString());
		editor.putString("gl_o", getEncryptString());
		editor.putString("gl_p", getEncryptString());
		editor.putString("gl_q", getEncryptString());
		editor.putString("gl_r", getEncryptString());
		editor.putString("gl_s", encryptServer());
		editor.putString("gl_t", getEncryptString());
		
		editor.commit();
		
		
		//encryptServer();
		decryptServer();
		
		//d("END saveSettings");
	}
	
	private void initSettings()
	{
		//d("initSettings");
		
		String tmp = mSettings.getString("gl_a", null);
		if(tmp == null)
		{
			//init for first time
			mFreeCount = 0;
			mSerialKey = PRODUCT_ID;
			saveSettings();
		} else
		{
			decryptFreeCount();
		}
	}
	
	
	
	public boolean checkLicense()
	{
		d("checkLicense");
		mLicenseStatus 	= INVALID_LICENSE;
		
	
		byte[] fileData = getRawResource(R.raw.serialkey);
		if(fileData != null)
		{
			mSerialKey = new String(fileData);
			d( "key " + mSerialKey);
			
			String dkey = decryptKey();
			
			//key was validated on server.
			if(dkey.compareTo(mSerialKey) == 0)
			{
				mLicenseStatus = VALID_LICENSE;
			}
			//for fist time we need to validate on server
			else if(dkey.compareTo(PRODUCT_ID) == 0)
			{
				try
				{
					String serverURL = SERVER_PIRACY.replace("#KEY#",mSerialKey).replace("#PRODUCT_ID#",PRODUCT_ID).replace("#ID#",mDeviceID);
					d( "serverURL " + serverURL);
											
					URL url = new URL(serverURL);
					HttpURLConnection http = (HttpURLConnection)url.openConnection();
					http.setRequestMethod("GET");	http.getResponseCode();           
					InputStream is = http.getInputStream();
					
					byte[] buf = new byte[512];
					is.read(buf);
					String responseBody = new String(buf);
					d( "responseBody " + responseBody);
					
					boolean license = (responseBody.substring(0,2).compareTo("OK") == 0);
					
					if(license)	//server return a valid key
					{
						d( "License Valid Saving");
						
						saveSettings();
						mLicenseStatus = VALID_LICENSE;
					}else 		//this key is already register or another error
					{
						d( "License Invalid Saving");
						
						mSerialKey 		= mDeviceID + mRandom.nextInt();
						mLicenseStatus 	= INVALID_LICENSE;
					}
				}catch(java.net.UnknownHostException unknown)
				{
					d( "Server unreachable");
					mSerialKey 		= PRODUCT_ID;
					mLicenseStatus 	= VALIDATION_SERVER_REQUIRED;
				}
				catch(Exception e){		d(e);	}
			}
		}
		//serial key not found
		else
		{
			d( "Serialkey not found, Invalid Saving");
			mSerialKey 		= mDeviceID + mRandom.nextInt();
			mLicenseStatus 	= INVALID_LICENSE;
		}
		
		return (mLicenseStatus == VALID_LICENSE);
	}
	
	
	private boolean isLicenseFail()
	{
		return  (mLicenseStatus == INVALID_LICENSE);//||;
	}
	public boolean canPlayFree()
	{
		//d("canPlayFree");
		return ((mFreeCount < MAX_FREE) && !isLicenseFail());
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
			case INVALID_LICENSE:
			//case LicenseAuthenticator.LICENSE_VALIDATION_FAILED:
				res = R.string.INVALID_LICENSE;
				break;
			
			case VALIDATION_SERVER_REQUIRED:
			//case LicenseAuthenticator.LICENSE_VALIDATION_FAILED:
				res = R.string.SERVER_VALIDATE_REQUIRED;
				break;
				
			// default:
			// {
				// res = R.string.gl_GENERAL_ERROR;

				// break;
			// }
		}
		
		return res;		
	}
	
	
	
	public byte[] getRawResource(int id)
	{
		try
		{
			InputStream mIS = mContext.getResources().openRawResource(id);
			int resLength = mIS.available();
			byte[]temp = new byte[resLength];
			mIS.read(temp, 0, resLength);
			mIS.close();
			mIS = null;
			return temp;
		}
		catch (Exception e) {
			d(e);
			return null;
		}
	}
	

	public static boolean DEBUG = !false && (RELEASE_VERSION == 0);	
	private static void d(String msg)
	{
		if(DEBUG)
		{
			DBG("DRM",msg);
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
