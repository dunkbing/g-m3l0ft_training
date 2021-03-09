package APP_PACKAGE.PackageUtils;

//Context requirement by listeners and such
import android.content.Context;

// Intent requirement for IGP
import android.content.Intent;

//Context required by AndroidUtils to keep an Activity weak ref
import android.app.Activity;

//IntentFilter required by Internet Status Functions - registering NetworkStateReceiver
import android.content.IntentFilter;

// Bundle for shared preferences
import android.os.Bundle;

// Build version codes and such
import android.os.Build.*;

// Included view
import android.view.View;

// Included window manager for screen flags
import android.view.WindowManager;

// Required for launching the browser
import android.net.Uri;

//Enviroment needed for paths
import android.os.Environment;

// For logging
import android.util.Log;

//AssetManager needed to retrieve assets from apk
import android.content.res.AssetManager;

// Required here for IMEI fetching
import android.telephony.TelephonyManager;

//KeyEvent required for onKeyUp/onKeyDown dispatch
import android.view.KeyEvent;

//MotionEvent required for onMotionEvent dispatch
import android.view.MotionEvent;

//File needed for paths
import java.io.File;

//DataInputStream needed to retrieve assets from apk
import java.io.DataInputStream;

//IOException needed to retrieve assets from apk
import java.io.IOException;

//used to keep weak refs
import java.lang.ref.WeakReference;

//used for asynchronous http requests
import android.os.AsyncTask;

//used to get apk path:
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;

//used for getting the inch size of the display
import android.util.DisplayMetrics;


// Data sharing, Device and SUtils for gaia purposes
import APP_PACKAGE.DataSharing;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.GLConstants;
import APP_PACKAGE.GLUtils.Device;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

#if USE_VIRTUAL_KEYBOARD
import APP_PACKAGE.GLUtils.VirtualKeyboard;
#endif // USE_VIRTUAL_KEYBOARD

// Splash Screen Activity for Welcome Screen
#if USE_WELCOME_SCREEN
import APP_PACKAGE.SplashScreenActivity;
#endif // USE_WELCOME_SCREEN

// Plugin for Ad Server
#if USE_ADS_SERVER
import APP_PACKAGE.PackageUtils.AdServerPlugin;
#endif // USE_ADS_SERVER

// IGP
#if USE_IGP_FREEMIUM
import APP_PACKAGE.IGPFreemiumActivity;
#endif // USE_IGP_FREEMIUM

// Installer
#if USE_INSTALLER
import APP_PACKAGE.installer.GameInstaller;
#endif // USE_INSTALLER


public class AndroidUtils
{

    /***************Constants and Members*************/
    private static WeakReference<Activity>		s_MainActivityRef  	= null;

    private static UtilsNetworkStateReceiver    sNetworkReceiver 	= new UtilsNetworkStateReceiver();
	private static UtilsBatteryStateReceiver    sBatteryReceiver = new UtilsBatteryStateReceiver();
	
    /***********End of Constants and Members**********/

    public static Context GetContext()
    {
        return SUtils.getContext();
    }
	
	public static AssetManager GetAssetManager()
    {
        return s_MainActivityRef.get().getAssets();
    }
	
    /*********************************************************************************************/
    /*******************************OS Functions called from Activity*****************************/

    public static void SetActivityRef(Activity actvity)
    {
        if(s_MainActivityRef == null)
        {
            s_MainActivityRef = new WeakReference<Activity>(actvity);   
			SUtils.setContext((Context)actvity);
		
			Device.init();
        }
        else
        {
            //av TODO: put an ugly message here :)
        }
    }
	
	public static void MinimizeApplication()
	{
		((APP_PACKAGE.MainActivity)s_MainActivityRef.get()).MinimizeApplication();
	}
	
	public static void ExitApplication(boolean restart)
	{
		((APP_PACKAGE.MainActivity)s_MainActivityRef.get()).ExitApplication(restart);
	}

    public static void onPreNativePause()
    {
		//considering this is called from the Main Activity only, it is safe to assume that the context is the main activity:
		Context ctx = (Context)s_MainActivityRef.get();
		
        ctx.unregisterReceiver(sNetworkReceiver);
		ctx.unregisterReceiver(sBatteryReceiver);
    }

    public static void onPause()
    {
        JNIBridge.NativeOnPause();//this is needed by Android Utils 
                                  //av TODO => to check if it's better to have AndroidUtils called directly
    }

    public static void onPostNativePause()
    {		
		//SUtils.setContext(null);
    }

	public static void onPreNativeResume()
    {
		//considering this is called from the Main Activity only, it is safe to assume that the context is the main activity:
		SUtils.setContext((Context)s_MainActivityRef.get());
    }

    public static void onResume()
    {
        JNIBridge.NativeOnResume();
    }

    public static void onPostNativeResume()
    {
		//considering this is called from the Main Activity only, it is safe to assume that the context is the main activity:
		Context ctx = (Context)s_MainActivityRef.get();
		
		// Network intents
        ctx.registerReceiver(sNetworkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		
		// Battery intents		
		ctx.registerReceiver(sBatteryReceiver, UtilsBatteryStateReceiver.getIntentFilter());
    }

    /****************************END OS Functions called from Activity****************************/
	
	
	/****Initialization functions****/
	public static int initCheckConnectionType()
	{
		if(s_MainActivityRef == null)
		{
			return 0; /// Equivalent of NO_CONNECTIVITY
		}
		
		return UtilsNetworkStateReceiver.CheckConnectionType();
	}
	
	public static void initBatteryInfo()
	{
		if(s_MainActivityRef == null)
			return;
			
		UtilsBatteryStateReceiver.SetInitialBatteryState(s_MainActivityRef.get());
	}	
	/**End Internet Status Functions*****/


	/*********** Android OS Free Disk Space *******************/
	
	public static long GetDiskFreeSpace(String path)
	{
		return SUtils.getFreeSpace(path);
	}

    /***********Android retrieve standard OS paths*************/

    public static String RetrieveSDCardPath()
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String RetrieveObbPath()
    {
        #if GOOGLE_MARKET_DOWNLOAD
        return SUtils.getContext().getObbDir().getAbsolutePath();
        #else
        return "NA"; // return Not Available
        #endif
    }

    public static String RetrieveDataPath()
    {
#if USE_INSTALLER 
		String result = SUtils.getPreferenceString("SDFolder", GameInstaller.mPreferencesName);
		if (result != "")
		{
			return result;
		}
#endif
		return SUtils.getContext().getExternalFilesDir(null).getAbsolutePath(); 
    }

    public static String RetrieveSavePath()
    {
        return SUtils.getContext().getFilesDir().getAbsolutePath();
    }

    public static String RetrieveTempPath()
    {
        return SUtils.getContext().getCacheDir().getAbsolutePath();
    }

    /***********************End OS Paths*****************************/
	
	
    public static int[] retrieveBarrels ()
    {
        return SUtils.retrieveBarrels();
    }


    /*****************Retrieve file from asset folder in apk ****************/
    public static byte[] GetAssetAsString(String path)
    {
        try 
		{
            if(path.startsWith("."))
			{
                path = path.substring(1);
			}

            DataInputStream is = new DataInputStream(SUtils.getContext().getAssets().open(path, AssetManager.ACCESS_STREAMING));
            
            if(is != null) 
			{
                byte[] buffer = new byte[is.available()];
                is.readFully(buffer);
                is.close();
                return buffer;
            }
        }
		catch(IOException e) 
		{
            e.printStackTrace();
        }
        return null;
    }
    
    /*****************Retrieve file from asset folder in apk ****************/
	
	/*****************Retrieve the meta-data value for the specified "key" ****************/
	public static String GetMetaDataValue(String name)
	{
		return SUtils.getMetaDataValue(name);
	}
	/*****************Retrieve the meta-data value for the specified "key" ****************/
	
	/*****************Unzip a file from path into dest ****************/
	public static boolean GenericUnzipArchive(String path, String destination)
	{
		return SUtils.genericUnzipArchive(path, destination);
	}
	/*****************Unzip a file from path into dest ****************/
	
	/*****************Delete the specified file in path ****************/	
	public static void DeleteFile(String path)
	{
		SUtils.deleteFile(path);
	}
	/*****************Delete the specified file in path ****************/		
	
	/*****************Removes the specified directory in path and all subdirectories in side ****************/	
	/***** Used for small folders *****/
	public static boolean RemoveDirectoryRecursively(String path)
	{
		return SUtils.removeDirectoryRecursively(path);
	}
	/*****************Removes the specified directory in path and all subdirectories in side ****************/	
	
	/*************** Shared Preference Methods ****************/
	public static void SavePreferenceInt(String key, String pName, int value) 
	{
		SUtils.setPreference(key, value, pName);
	}
	
	public static void SavePreferenceLong(String key, String pName, long value) 
	{
		SUtils.setPreference(key, value, pName);
	}
	
	public static void SavePreferenceBool(String key, String pName, boolean value) 
	{
		SUtils.setPreference(key, value, pName);
	}
	
	public static void SavePreferenceString(String key, String pName, String value) 
	{
		SUtils.setPreference(key, value, pName);
	}
	
	public static int GetPreferenceInt(String key, String pName, int defValue)
	{
		return SUtils.getPreferenceInt(key, defValue, pName);
	}
	
	public static long GetPreferenceLong(String key, String pName, long defValue)
	{
		return SUtils.getPreferenceLong(key, defValue, pName);
	}
	
	public static boolean GetPreferenceBool(String key, String pName, boolean defValue)
	{
		return SUtils.getPreferenceBoolean(key, defValue, pName);
	}
	
	public static String GetPreferenceString(String key, String pName, String defValue)
	{
		return SUtils.getPreferenceString(key, defValue, pName);
	}
	
	public static void RemovePreference(String key, String pName)
	{
		SUtils.removePreference(key, pName);
	}
	
	/*************** End of Shared Preference Methods ****************/
	
	/*************** Cannot Go Back At This Stage ********************/
	public static void ShowCannotGoBack()
	{
		SUtils.showCantGoBackPopup(300);
	}
	
	
	/*************** Welcome Screen Method ****************/
	public static void ShowWelcomeScreen(int language)
	{
#if USE_WELCOME_SCREEN
		SplashScreenActivity.cacheAndStart(language);
#else
		ERR("ACP_LOGGER", "Trying to launch Welcome Screen when USE_WELCOME_SCREEN is 0");
#endif // USE_WELCOME_SCREEN
	}

	/*************** HTTP Execute Asynchronously ****************/

	static class HttpAsyncTask extends AsyncTask<String, Integer, Integer>
	{
		protected Integer doInBackground(String... urls) 
		{		
			int count = urls.length;
			int succeeded = 0;

			for (int i = 0; i < count; i++)
			{		
				try 
				{
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet(urls[i]);
					HttpResponse response = client.execute(request);	

					INFO("ACP_LOGGER", "Executed following url: " + urls[i]);
					succeeded ++;
				} 
				catch (Exception e) 
				{ 
					e.printStackTrace();
					DBG_EXCEPTION(e);
				}	
			}
			return succeeded;
		}
	}

	public static void HttpExecuteAsync(String url)
	{	
		try
		{				
			INFO("ACP_LOGGER", "Creating async task for url: " + url);
			new HttpAsyncTask().execute(url);
		} 
		catch (Exception e) 
		{ 
			e.printStackTrace();
			DBG_EXCEPTION(e);
		}		
		finally 
		{ }
	}
	
	/*************** Launch Browser ****************/
	public static boolean LaunchBrowser(String url)
	{
		if(url == null || url.length() <= 0)
			return false;

		try
		{
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			s_MainActivityRef.get().startActivity(i);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/*************** Video Player ****************/
	public static boolean LaunchVideoPlayer(String url)
	{
#if USE_VIDEO_PLAYER
		return VideoPlayerPlugin.LaunchVideo(url);
#else
		ERR("ACP_LOGGER", "Trying to launch video " + url + " with USE_VIDEO_PLAYER (0)");
		return false;
#endif // USE_VIDEO_PLAYER
	}

	public static int IsVideoCompleted()
	{
#if USE_VIDEO_PLAYER
		return APP_PACKAGE.MyVideoView.isVideoCompleted();
#else
		return 1;
#endif
	}
	
	/*************** Virtual keyboard handling ****************/
#if USE_VIRTUAL_KEYBOARD
	public static void ShowKeyboard(String initialText, int kbType, int enterFunc, int fullscreen)
	{
		VirtualKeyboard.ShowKeyboard(initialText, kbType, enterFunc, fullscreen);
	}
	
	public static void HideKeyboard()
	{
		VirtualKeyboard.HideKeyboard();
	}
	
	public static boolean IsKeyboardVisible()
	{
		return VirtualKeyboard.isKeyboardVisible();
	}
	
	public static String GetVKeyboardText()
	{
		return VirtualKeyboard.GetVirtualKeyboardText();		
	}
	
	public static void SetVKeyboardText(String text)
	{
		VirtualKeyboard.SetKeyboardText(text);
	}
#endif // USE_VIRTUAL_KEYBOARD
	
	
	/***************** User Location getters *****************/
	
	public final static int USER_LOCATION_NOT_AVAILABLE = -2;
	
	public static void EnableUserLocation()
	{
		#if ENABLE_USER_LOCATION
		LocationPlugin.EnableUserLocation();
		#endif
	}
	
	public static void DisableUserLocation()
	{
		#if ENABLE_USER_LOCATION
		LocationPlugin.DisableUserLocation();
		#endif
	}
	
	public static int getUserLocationStatus()
	{
#if ENABLE_USER_LOCATION
		return LocationPlugin.getUserLocationStatus();
#else
		return USER_LOCATION_NOT_AVAILABLE;
#endif
	}
	
	public static double getUserLocationLatitude()
	{
#if ENABLE_USER_LOCATION
		return LocationPlugin.getUserLocationLatitude();
#else
		return 0;
#endif
	}
	
	public static double getUserLocationLongitude()
	{
#if ENABLE_USER_LOCATION
		return LocationPlugin.getUserLocationLongitude();
#else
		return 0;
#endif
	}
		
	public static float getUserLocationAccuracy()
	{
#if ENABLE_USER_LOCATION
		return LocationPlugin.getUserLocationAccuracy();
#else
		return 0;
#endif
	}
	
	public static String getUserLocationTime()
	{
#if ENABLE_USER_LOCATION
		return LocationPlugin.getUserLocationTime();
#else
		return "0";
#endif
	}
	/***************** End of User Location getters *****************/
	
	/***************** Hw Identifiers getters *****************/
	public static String GetAndroidID()
	{
		return Device.getAndroidId();
	}
	
	public static String GetSerial()
	{
		return Device.getSerial();
	}
	
	public static String GetCPUSerial()
	{
		return Device.retrieveCPUSerial();
	}
	
	public static String GetDeviceManufacturer()
	{
		return Device.getPhoneManufacturer();
	}
	
	public static String GetDeviceModel()
	{
		return Device.getPhoneModel();
	}
	
	public static String GetPhoneProduct()
	{
		return Device.getPhoneProduct();
	}

	public static String GetPhoneDevice()
	{
		return Device.getPhoneDevice();
	}
	
	public static String GetFirmware()
	{
		return Device.getDeviceFirmware();
	}
	
	public static String GetMacAddress()
	{
		return Device.getMacAddress();
	}

    /**
	 * Get IMEI. 
	 * Returns empty string if it doesn't have the appropiate permission
	 */
    public static String GetDeviceIMEI()
	{
		String Imei = "";
		try
		{
			Imei = ((TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
			
			if( Imei == null) 
			{
				Imei = "";
			}
			
			return Imei;
		}
		catch (Exception e)	
        {
            return "";
        }
	}
	
	public static String GetHDIDFV()
	{
		return Device.getHDIDFV();
	}
	
	public static float GetXDpi()
	{
		DisplayMetrics dm = new DisplayMetrics();
		s_MainActivityRef.get().getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		return dm.xdpi;	
	}
	
	public static float GetYDpi()
	{
		DisplayMetrics dm = new DisplayMetrics();
		s_MainActivityRef.get().getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		return dm.ydpi;
	}
	
	/***************** End of Hw Identifiers getters *****************/
	/***************** Software Identifiers getters *****************/
	public static String GetCarrierAgent()
	{
		return Device.retrieveDeviceCarrier();
	}
	
	public static String GetCountry()
	{
		return Device.retrieveDeviceCountry();
	}
	
	public static String GetDeviceLanguage()
	{
		return Device.retrieveDeviceLanguage();
	}
	
	public static String GetUserAgent()
	{
		return Device.getUserAgent();
	}
	
	public static String GetApkPath()
	{
		String apkFilePath = "";
		ApplicationInfo appInfo = null;
		PackageManager packMgmr = SUtils.getContext().getPackageManager();
		
		try 
		{
			appInfo = packMgmr.getApplicationInfo(STR_APP_PACKAGE, 0);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		apkFilePath = appInfo.sourceDir;
		return apkFilePath;
	}
	
	
	public static String GetGoogleAdId()
	{
		return Device.getGoogleAdId();
	}
	
	public static int GetGoogleAdIdStatus()
	{
		return Device.getGoogleAdIdStatus();
	}
	/***************** End of Software Identifiers getters *****************/
	/***************** Game Specific Identifiers getters *****************/
	public static String GetDefaultIGP()
	{
#if USE_HEP_EXT_IGPINFO
		return SUtils.getIGPInfo()[0];
#else
		return GGC_GAME_CODE;
#endif
	}
	
	public static String GetGameName()
	{
		return SUtils.getGameName();
	}
	
	public static String GetInjectedIGP()
	{
		return SUtils.getInjectedIGP();
	}
	
	public static String GetInjectedSerialKey()
	{
		return SUtils.getInjectedSerialKey();
	}
	/***************** End of Specific Identifiers getters *****************/
	
	/**************** Orientation Function ********************************/
	
	public static void SetOrientation(boolean lock)
	{
		((APP_PACKAGE.MainActivity)s_MainActivityRef.get()).SetOrientationLock(lock);
	}
	
	/************** Screen ON/Off*********************/
	public static void SetKeepScreenOn(boolean keepScreen)
	{
		((APP_PACKAGE.MainActivity)s_MainActivityRef.get()).SetKeepScreenOn(keepScreen);
	}
	
#if ACP_UT
	//to be called from Unit Tests
	public static void InjectKeys(final int keyEventCode) 
	{
		new Thread
		(
			new Runnable() 
			{ 
				public void run() 
				{
					new android.app.Instrumentation().sendKeyDownUpSync(keyEventCode);
				}
			}
		).start();
	}
#endif //ACP_UT
	
	/***************** Amazon Kindle Bar Utilities *************************/
#if AMAZON_STORE

	public static final int AMAZON_FLAG_NOSOFTKEYS = 0x80000000;
	private static final int FLAG_SUPER_FULLSCREEN = AMAZON_FLAG_NOSOFTKEYS | WindowManager.LayoutParams.FLAG_FULLSCREEN;
		
		public static void FullScreenToggleShowBar()
		{
			
			s_MainActivityRef.get().runOnUiThread(new Runnable()
			{
					@Override
					public void run()
					{
						if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) //Newer Kindle devices  (API 16 and up)
						{
							View view = s_MainActivityRef.get().getWindow().getDecorView().findViewById(android.R.id.content).getRootView();
							view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
						}
						else //Kindle 1st and 2nd Gen
						{
							s_MainActivityRef.get().getWindow().clearFlags(FLAG_SUPER_FULLSCREEN); 
							s_MainActivityRef.get().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						}
					}
				}
			);
		}

		public static void FullScreenToggleHideBar()
		{
			s_MainActivityRef.get().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) //Newer Kindle devices (API 16 and up)
					{
						View view = s_MainActivityRef.get().getWindow().getDecorView().findViewById(android.R.id.content).getRootView();
						view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
					}
					else //Kindle 1st and 2nd Gen
					{
						s_MainActivityRef.get().getWindow().addFlags(FLAG_SUPER_FULLSCREEN); 
						s_MainActivityRef.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 
					}
				}
			});
		}
		
#endif //AMAZON_STORE		
	
    /************Native Interface to package_utils****/
    


    /******Ending native interface****/
}
