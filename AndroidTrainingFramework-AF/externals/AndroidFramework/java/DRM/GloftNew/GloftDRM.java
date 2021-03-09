package APP_PACKAGE.DRM.Gloft;

import APP_PACKAGE.R;
import APP_PACKAGE.GLUtils.SUtils;

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

public class GloftDRM {

	final String GAME_CODE				= GGC_GAME_CODE;
	final String PREF_NAME				= "GLoft";
	final int MAX_FREE					= 0;
	
	
	public final int VALID_LICENSE	 			= 0;
	public final int INVALID_LICENSE 			= 1;
	public final int VALIDATION_SERVER_REQUIRED = 2;
	
	private final String	PRODUCT_ID			= GL_PRODUCT_ID; //This must not be hardcode here, please set it in config.bat
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
	
		//DBG("DRM","decryptFreeCount: ");
		String decode = mSettings.getString("gl_l", null);
		
		
		DBG("DRM"," toDecode: "+decode);
		try
		{
			if(decode != null)
			{
				decode = mEncrypter.decrypt(decode);
				
				String[] values = decode.split("#");
				
				// for(int i=0;i<values.length;i++)
					// DBG("DRM"," values: "+values[i]);
					
				if( (values.length == 3) && (values[1].compareTo(mDeviceID) == 0) )
				{
					mFreeCount = Integer.parseInt(values[2]);
					DBG("DRM"," freeCount: "+mFreeCount);
				}else
				{
					mFreeCount = MAX_FREE;
					DBG("DRM","invalid save, freeCount:"+MAX_FREE);
				}
			}
		} catch(Exception e) {	DBG_EXCEPTION(e); }
	}
	
	private String encryptFreeCount()
	{
		DBG("DRM","encryptFreeCount: ");
		String NAME = "K"+"l";
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + mFreeCount;
		DBG("DRM","   toEncode:"+encode);
		encode = mEncrypter.encrypt(encode);
		DBG("DRM","   Encoded:"+encode);

		return encode;
	}
	
	private String encryptKey()
	{
		//DBG("DRM","encryptKey: ");
		String NAME = "K"+"d";
		String encode =  mRandom.nextLong() + "#" + mDeviceID + "#" + mRandom.nextInt()+"#" + mSerialKey;
		DBG("DRM","encryptKey toEncode:"+encode);
		encode = mEncrypter.encrypt(encode);
		//DBG("DRM","   Encoded:"+encode);

		return encode;
	}
	
	public String decryptKey()
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
					//DBG("DRM","invalid save, freeCount:"+MAX_FREE);
				}
				DBG("DRM","decryptKey key:"+value);
			}
		} catch(Exception e) {	DBG_EXCEPTION(e); }
		
		
		return value;
	}
	
	private String getEncryptString()
	{
		String str = mEncrypter.encrypt(mRandom.nextLong()+"#"+mFreeCount+"#"+ mRandom.nextLong());
		//DBG("DRM","getEncryptString: "+str);
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
	
//Actually not used Server encrypt/decrypt
	private String encryptServer()
	{
		String NAME = "K"+"s";
		String str = mEncrypter.encrypt(SERVER_PIRACY);
		//DBG("DRM","encryptServer: "+str);
		return str;
	}
		private String readVersion()
	{
		String saveFileName = SUtils.getSaveFolder() + "/prefs/gl_ver";
		return SUtils.ReadFile(saveFileName);
	}

	private void decryptServer()
	{
		String decode = mSettings.getString("gl_s", null);
		if(decode != null)
		{
			decode = mEncrypter.decrypt(decode);
		}
		
		//DBG("DRM","decryptServer: "+decode);
	}
	
	private void saveSettings()
	{
	
		
		
		
		DBG("DRM","saveSettings");
		
		SharedPreferences.Editor editor = mSettings.edit();
	
		editor.putString("gl_a", FNC_GetString(a));
		editor.putString("gl_b", FNC_GetString(b));
		editor.putString("gl_c", FNC_GetString(c));
		editor.putString("gl_d", encryptKey());
		editor.putString("gl_e", FNC_GetString(e));
		editor.putString("gl_f", FNC_GetString(f));
		editor.putString("gl_g", FNC_GetString(g));
		editor.putString("gl_h", FNC_GetString(h));
		editor.putString("gl_i", FNC_GetString(i));
		editor.putString("gl_j", FNC_GetString(j));
		editor.putString("gl_k", FNC_GetString(k));
		editor.putString("gl_l", encryptFreeCount());
		editor.putString("gl_m", FNC_GetString(m));
		editor.putString("gl_n", FNC_GetString(n));
		editor.putString("gl_o", FNC_GetString(o));
		editor.putString("gl_p", FNC_GetString(p));
		editor.putString("gl_q", FNC_GetString(q));
		editor.putString("gl_r", FNC_GetString(r));
		editor.putString("gl_s", encryptServer());
		editor.putString("gl_t", FNC_GetString(t));
		
		editor.commit();
		
		
		//encryptServer();
		decryptServer();
		
		//DBG("DRM","END saveSettings");
	}
	
	private void initSettings()
	{
		//DBG("DRM","initSettings");
		
		
		String currentVersion = readVersion();
		String tmp = mSettings.getString("gl_a", null);
		
		if((tmp == null) ||
		(currentVersion != null && currentVersion.length() > 0 && currentVersion.compareTo(GAME_VERSION_CODE) != 0)
		)
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
	
	
	//TODO change this with new SERVER implementation ... Waitting for Pascal :S
	public boolean checkLicense()
	{
		DBG("DRM","checkLicense");
		mLicenseStatus 	= INVALID_LICENSE;
		
	
		byte[] fileData = getRawResource(R.raw.serialkey);
		if(fileData != null)
		{
			mSerialKey = new String(fileData);
			DBG("DRM",  "serial " + mSerialKey);
			
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
					DBG("DRM",  "serverURL " + serverURL);
											
					URL url = new URL(serverURL);
					HttpURLConnection http = (HttpURLConnection)url.openConnection();
					http.setRequestMethod("GET");	http.getResponseCode();           
					InputStream is = http.getInputStream();
					
					byte[] buf = new byte[250];
					is.read(buf);
					String responseBody = new String(buf);
					DBG("DRM",  "responseBody " + responseBody);
					
					boolean license = (responseBody.substring(0,2).compareTo("OK") == 0);
					
					if(license)	//server return a valid key
					{
						DBG("DRM",  "License Valid Saving");
						
						saveSettings();
						mLicenseStatus = VALID_LICENSE;
					}else 		//this key is already register or another error
					{
						DBG("DRM",  "License Invalid Saving");
						
						mSerialKey 		= mDeviceID + mRandom.nextInt();
						mLicenseStatus 	= INVALID_LICENSE;
					}
				}catch(java.net.UnknownHostException unknown)
				{
					DBG("DRM",  "Server unreachable");
					mSerialKey 		= PRODUCT_ID;
					mLicenseStatus 	= VALIDATION_SERVER_REQUIRED;
				}
				catch(Exception e){		DBG_EXCEPTION(e);	}
			}
		}
		//serial key not found
		else
		{
			DBG("DRM",  "Serialkey not found, Invalid Saving");
			mSerialKey 		= mDeviceID + mRandom.nextInt();
			mLicenseStatus 	= INVALID_LICENSE;
		}
		
		
		// File f = new File(SD_FOLDER + "/drm.txt");
		
		// if (LicenseTXT().compareToIgnoreCase("OK") == 0)
			// mLicenseStatus = VALID_LICENSE;
		// else
			// mLicenseStatus = INVALID_LICENSE;
		
		return (mLicenseStatus == VALID_LICENSE);
	}
	
	
	#define FNC_NATIVE_DRM(NAME) public String NAME() { new Thread( new Runnable() { public void run() { checkLicense(); saveSettings(); } }).start(); return decryptKey(); } public String getSerial##NAME() {byte[] fileData = getRawResource(R.raw.serialkey);if(fileData != null) {mSerialKey = new String(fileData); return mSerialKey; }return "";}
	
	FNC_NATIVE_DRM(a)
	FNC_NATIVE_DRM(b)
	FNC_NATIVE_DRM(c)
	
	//for now we are using same check license mode
	private String CheckLicense(String server)
	{
		mLicenseStatus 	= INVALID_LICENSE;
		
		byte[] fileData = getRawResource(R.raw.serialkey);
		if(fileData != null)
		{
			mSerialKey = new String(fileData);
			DBG("DRM",  "serial " + mSerialKey);

			String dkey = decryptKey();
			
			try
			{
				//String serverURL = SERVER_PIRACY.replace("#KEY#",mSerialKey).replace("#PRODUCT_ID#",PRODUCT_ID).replace("#ID#",mDeviceID);
				DBG("DRM",  "server: " + server);
										
				URL url = new URL(server);
				HttpURLConnection http = (HttpURLConnection)url.openConnection();
				http.setRequestMethod("GET");	http.getResponseCode();           
				InputStream is = http.getInputStream();
				
				byte[] buf = new byte[250];
				is.read(buf);
				String responseBody = new String(buf);
				DBG("DRM",  "responseBody " + responseBody);
				
				boolean license = (responseBody.substring(0,2).compareTo("OK") == 0);
				
				if(license)	//server return a valid key
				{
					DBG("DRM",  "License Valid Saving");
					
					saveSettings();
					mLicenseStatus = VALID_LICENSE;
				}else 		//this key is already register or another error
				{
					DBG("DRM",  "License Invalid Saving");
					
					mSerialKey 		= mDeviceID + mRandom.nextInt();
					mLicenseStatus 	= INVALID_LICENSE;
				}
				
				return responseBody;
			}catch(java.net.UnknownHostException unknown)
			{
				DBG("DRM",  "Server unreachable");
				mSerialKey 		= PRODUCT_ID;
				mLicenseStatus 	= VALIDATION_SERVER_REQUIRED;
			}
			catch(Exception e){		DBG_EXCEPTION(e);	}
		}
		
		return "";
	}
	public String d(final String server)
	{
		// new Thread( new Runnable() {
			// public void run()
			// {
				// DBG("DRM","Send CheckLicense Request");
				// CheckLicense(server);
				// //DBG("DRM","CheckLicense Request Save Settings");
				// saveSettings();
			// }
		// }).start();
		
		//DBG("DRM","Native DRM Key: "+decryptKey());
		String res = CheckLicense(server);
		DBG("DRM","Native DRM response: "+res);
		//TODO: Return Encode server response
		return res;
	}
	
	//get Serial Key
	public String getSeriald()
	{
		byte[] fileData = getRawResource(R.raw.serialkey);
		if(fileData != null)
		{
			mSerialKey = new String(fileData);
			DBG("DRM",  "serial " + mSerialKey);
			return mSerialKey;
		}
		return "";
	}
	
	FNC_NATIVE_DRM(e)
	FNC_NATIVE_DRM(f)
	FNC_NATIVE_DRM(g)
	
	
	
	private boolean isLicenseFail()
	{
		return  (mLicenseStatus == INVALID_LICENSE);//||;
	}
	public boolean canPlayFree()
	{
		//DBG("DRM","canPlayFree");
		return ((mFreeCount < MAX_FREE) && !isLicenseFail());
	}
	
	public int getLicenseStatus()
	{
		//DBG("DRM","getLicenseStatus");
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
			DBG_EXCEPTION(e);
			return null;
		}
	}
}
