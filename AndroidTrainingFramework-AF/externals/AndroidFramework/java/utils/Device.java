package APP_PACKAGE.GLUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.webkit.WebView;
import android.util.Log;
import android.provider.Settings.Secure;

import android.text.TextUtils;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_BILLING
import APP_PACKAGE.billing.common.AServerInfo;
#endif
import APP_PACKAGE.GLUtils.SUtils;

#if USE_INSTALLER
import APP_PACKAGE.installer.GameInstaller;
#endif

#if USE_DATA_SHARING
import APP_PACKAGE.DataSharing;
#endif

import APP_PACKAGE.PackageUtils.JNIBridge;

public class Device
{
	public static final String[][] ISO3Languages =
	{
		{"eng", "en"},
		{"fra", "fr"},
		{"deu", "de"},
		{"esl", "es"},
		{"spa", "es"},
		{"ita",	"it"},
		{"jpn", "jp"},
		{"por", "br"},
		{"por",	"pt"}
	};
    public static final boolean USE_TRACKING = false;
    public static final boolean CHECK_SMS_SERVER_ASNWER = false;
    public static final boolean USE_3_MINUTES_SCAN = true;

	private static TelephonyManager mDeviceInfo 	= null;
	private static String IMEI		 				= null;
	private static String userAgent 				= null;
    private static String networkOperator			= null;
    private static String networkOperatorName		= null;
    private static String simOperator				= null;
    private static String simOperatorName			= null;
    private static String lineNum					= null;
    private static String networkCountryIso			= null;
    private static String simCountryIso				= null;
    private static boolean isRoaming;
    private final String profileType 				= GL_PROFILE_TYPE; //This must not be hardcode here, please set it at config.bat
	private static String HDIDFV					= null;
	private static String HDIDFVVersion				= null;
	private static String GoogleAdId				= null;
	
	#if ENABLE_GOOGLE_AD_ID
	//status of GoogleAdId
	public final static int GAID_STATUS_UNINITIALIZED 				= -1;
	public final static int GAID_STATUS_SUCCESSFUL 					= 0;
	public final static int GAID_STATUS_USERDISABLED 				= 1;
	public final static int GAID_STATUS_GPS_UNAVAILABLE 			= 2;
	public final static int GAID_STATUS_NOT_FINISHED_LOADING 		= 3;
	public final static int GAID_STATUS_GPS_AVAILABLE_WITH_ERROR 	= 4;
	public final static int GAID_STATUS_AD_ID_DISABLED 				= 5;
	private static int      GoogleAdIdStatus						= GAID_STATUS_UNINITIALIZED;
	#else
	public final static int GAID_STATUS_NOT_AVAILABLE	 			= -2;
	private static int      GoogleAdIdStatus						= GAID_STATUS_NOT_AVAILABLE;
	#endif
	
    // ************************   MODIFY THIS CODE FOR YOUR GAME DEMO CODE********************************
	

 	// private static String demoCode = null;
#if USE_IGP_CODE_FROM_FILE	
	private static String demoCode = null;
#else
	#if !USE_HEP_EXT_IGPINFO
	public static final String demoCode = GL_DEMO_CODE; //This must not be hardcode here, please set it at config.bat
	#else
	public static String demoCode = GL_DEMO_CODE;
	#endif //USE_HEP_EXT_IGPINFO
#endif //USE_IGP_CODE_FROM_FILE
#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	private AServerInfo mServerInfo;
#endif

#if USE_BILLING || USE_INSTALLER || USE_TRACKING_FEATURE_INSTALLER || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_PSS
	private static Carrier carrier;
#endif
	private static WebView wb = null;

	//1 Normal Billing
	//2 WAP, Tickets
	//3	MRC
	public final String VERSION  = GL_BILLING_VERSION;//This must not be hardcode here, please set it at config.bat

	private static final int MAX_UNLOCK_CODE = 9999;
	private static final int MIN_UNLOCK_CODE = 1111;
	public static final int SMS_UNLOCK_KEY = 53412;

    /** To recognize the wifi state when launching the wrapper */
	public static WifiManager 		m_wifiManager;

	static ConnectivityManager mConnectivityManager = null;
	WifiLock mWifiLock;
	private static String m_lang;
	private static int mUniqueCode;
	private String mUserID = "";
	
#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	public Device(AServerInfo mServerValues)
	{
		this();
		mServerInfo = mServerValues;
	}
#endif //#if USE_BILLING

	public Device()
	{
		InitDeviceValues();
	}	

	public static String getGLDID()
	{
		#if (HDIDFV_UPDATE == 2)
			return "hdidfv="+getHDIDFV()+" ";
		#else
			return "hdidfv="+getHDIDFV()+" imei="+getDeviceIMEI()+" mac="+getMacAddress()+" aid="+getAndroidId()+" serialNo="+getSerial()+" ";
		#endif
	
	}
	
	public static String getAndroidId()
	{
		if(SUtils.getContext() == null)
		{
			ERR("Device", "SUtils.getContext() == null");
			ERR("Device", "You must call SUtils.setContext() first");
			return "";
		}
		String AndroidID = "";
		AndroidID = Secure.getString(SUtils.getContext().getContentResolver(), Secure.ANDROID_ID);
		if (AndroidID == null || AndroidID.equals("9774d56d682e549c"))
			return "";
		if(AndroidID.length() >= 15)
			return AndroidID;
			
		return "";
	}
	
	public static String getMacAddress()
	{
		if(SUtils.getContext() == null)
		{
			ERR("Device", "SUtils.getContext() == null");
			ERR("Device", "You must call SUtils.setContext() first");
			return "";
		}
		try
		{
			WifiManager wifi_manager = (WifiManager)SUtils.getContext().getSystemService(Context.WIFI_SERVICE);
			if (wifi_manager == null)
				return "";
				
			WifiInfo wifi_inf = wifi_manager.getConnectionInfo();
			if (wifi_inf == null)
				return "";
				
			String mac_address = wifi_inf.getMacAddress();
			if (mac_address == null || mac_address.length() == 0)
				return "";
		
			return mac_address;
		}
		catch(Exception exc)
		{
		}
		return "";
	}
	
	public static String getSerial()
	{
		String serial = "";
		#if !BUILD_FOR_FIRMWARE_1_6
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			 serial = android.os.Build.SERIAL;
		}
		#endif
		if ((serial == null) || (serial.length() < 5) || (serial.equals("unknown")))
		{
			serial = getSerialNo();
		}			
		return serial;
	}


	public static String getSerialNo()
	{
		String serial = "";
		try 
		{
			Class<?> c = Class.forName("android.os.SystemProperties");
			java.lang.reflect.Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.serialno");
			if ((serial == null) || (serial.length() < 5) || (serial.equals("unknown")))
				return "";
			return serial;
		} catch (Exception e) { serial = ""; }
		
		return "";
	}

	public static String getDeviceIMEI()
	{
		String Imei = "";
		#if (HDIDFV_UPDATE != 2 || GAMELOFT_SHOP == 1)
		try
		{
			if(SUtils.getContext() == null)
			{
				ERR("Device", "SUtils.getContext() == null");
				ERR("Device", "You must call SUtils.setContext() first");
				return "";
			}
			TelephonyManager deviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
			Imei = deviceInfo.getDeviceId();
			if (Imei !=null)
				return Imei;				
		}
		catch (Exception e)	{}
		#endif
		return "";
	}
	
	public static String getDeviceId()
	{
		String model = Build.MODEL;
		String device = Build.DEVICE;
		boolean isWifi = IsWifiEnable();
		if(isWifi && model != null && device != null)
		{
			return d1();
		}
		else
		{
			isWifi = false;
			model = null;
			device = null;
			return d1();
		}
	}
	
	public static String getHDIDFVVersion()
	{
		#if HDIDFV_UPDATE
		if(!TextUtils.isEmpty(HDIDFVVersion))
			return HDIDFVVersion;
		#endif
		return "";
	}
	
	public synchronized static String getHDIDFV()
	{
	#if HDIDFV_UPDATE
		if(!TextUtils.isEmpty(HDIDFV))
			return HDIDFV;
		HDIDFV = "";
	
	#if USE_DATA_SHARING
		HDIDFV = DataSharing.getSharedValue("HDIDFV");
		try
		{
			if(!TextUtils.isEmpty(HDIDFV))
				HDIDFV = Encrypter.decrypt(HDIDFV).replaceAll(" ", "");				
		}
		catch(Exception e) 
		{
			HDIDFV = "";
		}
		
		if(!TextUtils.isEmpty(HDIDFV))
		{
			DBG("Device", "Using HDIDFV from DataSharing "+HDIDFV);
			HDIDFVVersion = DataSharing.getSharedValue("HDIDFVVersion");
			DBG("Device", "Using HDIDFVVersion from DataSharing "+HDIDFVVersion);
			return HDIDFV;
		}
	#endif

		DBG("Device","HDIDFV not present, let's generate a new one");

		#if !USE_SPECIFIC_GENERATOR_NAME
		System.loadLibrary("generator");
		#else
		System.loadLibrary("generator"+GGC_GAME_CODE);
		#endif
		
		
		HDIDFV = com.gameloft.android.hdidfv.HDIDFV.getHDIDFV();
		#if (HDIDFV_UPDATE == 1)
		Log.d("HEI","8100");
		#else
		Log.d("HEI","8200");
		#endif
		HDIDFVVersion = com.gameloft.android.hdidfv.HDIDFV.getHDIDFVVersion();
	#if USE_DATA_SHARING
		if(!TextUtils.isEmpty(HDIDFV))
		{
			DBG("Device", "Saving HDIDFV into DataSharing: "+HDIDFV);
			DataSharing.setSharedValue("HDIDFV", Encrypter.crypt(HDIDFV));
			DataSharing.setSharedValue("HDIDFVVersion", HDIDFVVersion);
		}
	#endif
		if(!TextUtils.isEmpty(HDIDFV))
		{
			return HDIDFV;
		}
	#endif
		return "";
	}
	
	public static int getGoogleAdIdStatus()
	{
		return GoogleAdIdStatus;
	}

	public static void initGoogleAdId()
	{
		#if ENABLE_GOOGLE_AD_ID
		if(GoogleAdIdStatus == GAID_STATUS_UNINITIALIZED)
		{
			GoogleAdIdStatus = GAID_STATUS_NOT_FINISHED_LOADING;
			new Thread()
			{
				public void run()
				{
					try {
						DBG("Device","Retrieving Google AdId");
						com.google.android.gms.ads.identifier.AdvertisingIdClient.Info adInfo = null;
						adInfo = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(SUtils.getContext());
						
						if(adInfo.isLimitAdTrackingEnabled())
						{
							//Checkbox selected in GoogleSettings/Ads
							GoogleAdIdStatus = GAID_STATUS_AD_ID_DISABLED;
							GoogleAdId = "";
						}
						else
						{
							GoogleAdIdStatus = GAID_STATUS_SUCCESSFUL;
							GoogleAdId = adInfo.getId();
						}
					} catch (com.google.android.gms.common.GooglePlayServicesNotAvailableException gpsnae) {
						GoogleAdIdStatus = GAID_STATUS_GPS_UNAVAILABLE;
						GoogleAdId = "";
						DBG_EXCEPTION(gpsnae);
					} catch (Exception e) {
						GoogleAdIdStatus = GAID_STATUS_GPS_AVAILABLE_WITH_ERROR;
						GoogleAdId = "";
						DBG_EXCEPTION(e);
					}
					DBG("Device","Google AdId = "+GoogleAdId);
				}
			}.start();
		}
		#endif
	}		
	
	public static String getGoogleAdId()
	{
		#if ENABLE_GOOGLE_AD_ID
			if(TextUtils.isEmpty(GoogleAdId)) 
				return "";
			return GoogleAdId;
		#else
			return "";
		#endif
	}
	
	public void setUserID(String s)
	{
		mUserID = s;
	}
	
	public String getUserID()
	{
		return mUserID;
	}

	public static boolean IsWifiEnable()
	{
		m_wifiManager = (WifiManager) SUtils.getContext().getSystemService(Context.WIFI_SERVICE);
        return (m_wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
	}

	public void DisableWifi()
	{
		m_wifiManager.setWifiEnabled(false);
	}

	public void EnableWifi()
	{
		m_wifiManager.setWifiEnabled(true);
	}
	public boolean IsWifiDisabling()
	{
		return (m_wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING);
	}
	public boolean IsWifiEnabling()
	{
		return (m_wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING);
	}

	public boolean IsConnectionReady()
	{
		State mobile = mConnectivityManager.getNetworkInfo(mConnectivityManager.TYPE_MOBILE).getState();

		if (mobile == State.CONNECTED)
		{
			DBG("Device","Phone Data Connection READY!!!");
			return (true);
		}

		DBG("Device","Phone Data Connection HANDSHAKING!!!");

			return (false);
	}

	private static void InitDeviceValues()
	{
		DBG("Device","Init google Id");
		initGoogleAdId();//init GoogleAdId
		if(mConnectivityManager == null)
			mConnectivityManager = (ConnectivityManager) SUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		setDeviceInfo();

		String simState = "";
		try{
			simState = getSimState();
		}catch(Exception e){simState = "SIM_ERROR_UNKNOWN";}
		
		if (IMEI == null)
			IMEI = Device.getDeviceId();

		if (networkOperator == null)
			networkOperator = getNetworkOperator();
		if (networkOperator.trim().length() == 0)
			networkOperator = simState;

		if (networkOperatorName == null)
			networkOperatorName = getNetworkOperatorName();
		if (networkOperatorName.trim().length() == 0)
			networkOperatorName = simState;
		

		if (simOperator == null)
			simOperator = mDeviceInfo.getSimOperator();
		if (simOperator.trim().length() == 0)
			simOperator = simState;

		if (simOperatorName == null)
			simOperatorName = ValidateStringforURL(mDeviceInfo.getSimOperatorName());
		if (simOperatorName.trim().length() == 0)
			simOperatorName = simState;
		
		if(lineNum==null || lineNum.equals("00"))
			lineNum = getLineNumber();

		if (networkCountryIso == null)
			getNetworkCountryIso();

		if (simCountryIso == null)
			simCountryIso = mDeviceInfo.getSimCountryIso();

		isRoaming			= mDeviceInfo.isNetworkRoaming();
		mUniqueCode			= createUniqueCode();
#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
		Locale mLocale = Locale.getDefault();
		m_lang = getLanguage(mLocale.getISO3Language());
#endif

		//Save the user agent
		try{
			if (userAgent==null)
			{
				Activity a = (Activity) SUtils.getContext();
				a.runOnUiThread(new Runnable () {
					public void run ()
					{
						try
						{
							wb = new WebView(SUtils.getContext());
							userAgent = wb.getSettings().getUserAgentString();
							wb = null;
						} catch (Exception e) { userAgent = "GL_EMU_001"; DBG_EXCEPTION(e); }
					} }
				);
			}
		}catch(ClassCastException ex) {}
			
		
#if USE_BILLING || USE_INSTALLER || USE_TRACKING_FEATURE_INSTALLER || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_PSS
		carrier = new Carrier();
#endif
		#if USE_HEP_EXT_IGPINFO
		demoCode = SUtils.getIGPInfo()[0];//IGP code
		#endif //USE_HEP_EXT_IGPINFO
	}

	private static String getLanguage(String lang) {
		for(int i = 0; i < ISO3Languages.length; i++) {
			if(lang.compareToIgnoreCase(ISO3Languages[i][0]) == 0) {
				return ISO3Languages[i][1];
			}
		}
		return "en";
	}

	public static String ValidateStringforURL(String value)
	{
		try 
		{
			value = URLEncoder.encode(value,"UTF-8");
		} catch (Exception e) {}
		return value;
	}

	/*	
		return the device's sim state.
	*/
	public static String getSimState()
	{
		String simState = "";
		switch (mDeviceInfo.getSimState())
		{
			case TelephonyManager.SIM_STATE_ABSENT:
				simState = "SIM_ABSENT";
				break;
			case TelephonyManager.SIM_STATE_PUK_REQUIRED:
				simState = "SIM_PUK_REQUIRED";
				break;
			case TelephonyManager.SIM_STATE_PIN_REQUIRED:
				simState = "SIM_PIN_REQUIRED";
				break;
			default:
				simState = "SIM_ERROR_UNKNOWN";
				break;
		}
		// DBG("Device", "getSimState ="+simState+"=");
		return simState;
	}
	
	public static void setDeviceInfo()
	{
		if( mDeviceInfo == null )
		{
			try{
				mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
			}catch(Exception e){mDeviceInfo = null;}
		}
	}
	
	public String getIMEI()							{	return IMEI;								}

	public static String getUserAgent()				{	return userAgent;							}

	/*
	* @Return the array of bytes from userAgent
	* @call from native id mUserAgent
	*/
	public static byte[] f ()
	{
		return getUserAgent().getBytes();
	}
	
	public String getBillingVersion()				{	return VERSION;								}
	
	/*	
		return the device's network operator.
		previously was   public String getNetworkOperator()				{	return networkOperator;						}
	*/
	public static String getNetworkOperator()				
	{
		String simState = "SIM_ERROR_UNKNOWN";
		if (networkOperator == null)
		{
			try{
				setDeviceInfo();
				simState = getSimState();
				networkOperator = mDeviceInfo.getNetworkOperator();
			}catch(Exception e){networkOperator = null; simState = "SIM_ERROR_UNKNOWN";}
		}
		if (networkOperator == null || networkOperator.trim().length() == 0)
			networkOperator = simState;
		// DBG("Device", "getNetworkOperator ="+networkOperator+"=");
		return networkOperator;						
	}	
	
	/*	
		return the device's network operator name.
		previously was   public String getNetworkOperatorName()				{	return networkOperatorName;						}
	*/
	public static String getNetworkOperatorName()
	{
		String simState = "SIM_ERROR_UNKNOWN";
		if (networkOperatorName == null)
		{
			try{
				setDeviceInfo();
				simState = getSimState();
				networkOperatorName = ValidateStringforURL(mDeviceInfo.getNetworkOperatorName());
			}catch(Exception e){networkOperator = null; simState = "SIM_ERROR_UNKNOWN";}
		}
		if (networkOperatorName == null || networkOperatorName.trim().length() == 0)
			networkOperatorName = simState;
		// DBG("Device", "getNetworkOperatorName ="+networkOperatorName+"=");
		return networkOperatorName;
	}

	public String getSimOperator()					{	return simOperator;							}

	public String getSimOperatorName()				{	return simOperatorName;						}

	/*	
		return the device's line number.
		return "00" if an error is present, or if the SIM card is absent 
		previously was   public String getLineNumber()					{	return lineNum;								}
	*/
	public static String getLineNumber()
	{
		#if (HDIDFV_UPDATE != 2 || GAMELOFT_SHOP == 1)
		if(lineNum == null)
		{
			try{
				setDeviceInfo();
				lineNum	= mDeviceInfo.getLine1Number();

				if(lineNum == null || lineNum.equals(""))
					lineNum="00";
			}catch(Exception e){lineNum="00";}
		}
		// DBG("Device", "getLineNumber ="+lineNum+"=");
		#else
			lineNum = "00";
		#endif
		return lineNum;
	}

	/*	
		return the device's network country ISO.
		previously was   public String getNetworkCountryIso()			{ 	return networkCountryIso;					}
	*/
	public static String getNetworkCountryIso()
	{
		if (networkCountryIso == null)
		{
			try{
				setDeviceInfo();
				networkCountryIso = mDeviceInfo.getNetworkCountryIso();
			}catch(Exception e){networkCountryIso = null;}
		}
		// DBG("Device", "getNetworkCountryIso ="+networkCountryIso+"=");
		return networkCountryIso;
	}

	//returns the country Iso of the installed SIM
	public String getSimCountryIso()				{ 	return simCountryIso;						}

	//returns the Phone Model of the device
	public static String getPhoneModel()			{	return ValidateStringforURL(Build.MODEL);	}

	//returns the Device name of the device
	public static String getDevice()				{	return ValidateStringforURL(Build.DEVICE);	}
	
	//returns the Phone Model of the device
	public static String getPhoneDevice()			{	return ValidateStringforURL(Build.DEVICE);	}
	
	//returns the Product of the device
	public static String getPhoneProduct()			{	return ValidateStringforURL(Build.PRODUCT);	}

	//returns the Manufacturer of the device
	public static String getPhoneManufacturer()		{	return ValidateStringforURL(Build.MANUFACTURER);	}

	//returns the Firmware of the device
	public static String getDeviceFirmware()		{	return Build.VERSION.RELEASE;	}
	
	public static String getDemoCode()						
	{	

#if !USE_IGP_CODE_FROM_FILE
		return demoCode;
#else

		if(demoCode != null)
		{
			return demoCode;
		}	
		else
		{
			String tmp = SUtils.ReadFile(APP_PACKAGE.R.raw.nih);
			boolean showError = false;
			if(TextUtils.isEmpty(tmp)) 
			{
				showError = true;
			} else
			{
				demoCode = DefReader.getInstance().getPlainDef(tmp, GGC_GAME_CODE);
				if(!TextUtils.isEmpty(demoCode))
				{
					return demoCode;
				} else 
				{
					showError = true;
				}

			}

			if(showError)
			{
				Runnable runnable = new Runnable()
				{
					public void run()
					{
						AlertDialog dialog = new AlertDialog.Builder(SUtils.getContext()).setTitle("Error")
							.setMessage("Can't read IGP from file")
							.setCancelable(false)
							.setPositiveButton(
								"OK",
								new android.content.DialogInterface.OnClickListener() {
									public void onClick(
										android.content.DialogInterface dialog,
										int which) 
									{
										//kill process
										int pid = android.os.Process.myPid();
										android.os.Process.killProcess(pid); 
									}
								}).create();

						dialog.setCanceledOnTouchOutside(false);
						dialog.show();
					}
				};

				SUtils.runOnUiThread(runnable);

			}
		}

		ERR("DEMOCODE","getDemoCode something is really wrongs: [" + demoCode+"]");
		
		return null; 
#endif //USE_IGP_CODE_FROM_FILE
	}


	public boolean getIsRoaming()					{	return isRoaming;							}

#if USE_BILLING || USE_INSTALLER || USE_TRACKING_FEATURE_INSTALLER || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_PSS
	public Carrier getCarrier()						{	return carrier;								}
#endif

	public String getProfileType()					{	return profileType;							}
#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	public AServerInfo getServerInfo()				{	return mServerInfo;							}

	public String getLocale()						{	return m_lang; 								}

	public void setServerInfo(AServerInfo o) 		{ 	mServerInfo = o; 							}
#endif
	public static int createUniqueCode()
    {
    	double rnd = new Random().nextDouble();
        int randomNumber = (int)(((MAX_UNLOCK_CODE - MIN_UNLOCK_CODE + 1) * rnd ) + MIN_UNLOCK_CODE );

        return randomNumber;
    }

	public static int getUniqueCode ()
	{
		return mUniqueCode;
	}

	/*
	* @set the value for mUniqueCode from Native
	* @call from native id mSetUniqueCode
	*/
	public static void e (int code)
	{
		ERR("Device","Code "+ code);
		mUniqueCode = code;
	}

	/*
	* @Return the array of bytes from the version
	* @call from native id mGameVersion
	*/
	public static byte[] d ()
	{
		return GAME_VERSION_NAME.getBytes();
	}

	public static void init ()
	{
		InitDeviceValues();
	}
	private static byte[] nulo = {0};

	/*
	* @Return the array of bytes from the IMEI
	* @call from native id mIMEI
	*/
	public static byte[] a ()
	{
		if (IMEI == null)
			return nulo;
		return IMEI.getBytes();
	}

	/*
	* Return the array of bytes from the OperatorName
	* @call from native id mOperatorName
	*/
	public static byte[] b ()
	{
		if (networkOperatorName == null)
			return nulo;
		return networkOperatorName.getBytes();
	}

	/*
	* Return the array of bytes from the lineNum
	* @call from native id mLineNumber
	*/
	public static byte[] c ()
	{
		if (lineNum == null)
			return nulo;
		return lineNum.getBytes();
	}

	/*
	* Return the array of bytes from the HostName
	* @call from native id Device Model + _ + Device Product
	*/
	public static byte[] getHostName ()
	{
	    String HostName = android.os.Build.MODEL + "_" + android.os.Build.PRODUCT;
		DBG("Device ","HostName "+HostName);
		return HostName.getBytes();
	}
#if USE_RT_CONFIG
	private static Hashtable<String, Boolean> mRTConfigs = new Hashtable<String, Boolean>();

	/*
	 * Return the value of the specific config param, if not exist return false
	 * first call will fill the config hashtable with the settings found in "rtConfig.bin" file if exist, located at SD_FOLDER
	 * @param config the string name of the config you're asking for.
	 */
	public static boolean isRTConfigActived(String config)
	{
		if (mRTConfigs.isEmpty())
		{

			File fconfig = new File(SUtils.getPreferenceString("SDFolder", SD_FOLDER, GAME_ACTIVITY_NAME_STR + "Prefs") + "/"+ "rtConfig.bin");
			if (!fconfig.exists())
			{
				mRTConfigs.put("NoSettings", true);
				return false;
			}
			String temp = SUtils.ReadFile(fconfig.getAbsolutePath());
			if (temp == null)
			{
				mRTConfigs.put("NoSettings", true);
				return false;
			}
			String atemp[] = temp.split("\r\n");
			boolean value;
			String key;
			String sets[];
			for (int i=0; i < atemp.length; i++)
			{
				sets = atemp[i].split("=");
				if (sets != null && sets.length == 2)
				{
					key = sets[0];
					value = sets[1].trim().equals("1");
					mRTConfigs.put(key, value);
				}
			}
		}
		if (mRTConfigs.containsKey(config))
		{
			return Boolean.valueOf(mRTConfigs.get(config));
		}
		return false; //default value in case the key aren't registered
	}
#endif //#if USE_RT_CONFIG
	
	public synchronized static String d1() 
	{
		if(SUtils.getContext() == null)
		{
			ERR("Device", "SUtils.getContext() == null");
			ERR("Device", "You must call SUtils.setContext() first");
			return null;
		}
		
		String deviceID = null;

		#if (HDIDFV_UPDATE != 2 || GAMELOFT_SHOP == 1)
		/* For commercial phones, using the IMEI */
		DBG("Device", "Try using IMEI");
		TelephonyManager deviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		deviceID = deviceInfo.getDeviceId();	
		if(deviceID != null)
			return deviceID;
		#endif
		
		/* For Android 2.3 and above, using SERIAL */
	#if !BUILD_FOR_FIRMWARE_1_6
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			DBG("Device", "Try using SERIAL, Android 2.3");
			deviceID = android.os.Build.SERIAL;
			if(deviceID != null && deviceID != "unknown")
				return deviceID;
		}
	#endif
		/* Some other devices have SERIAL, try to read it */
		try 
		{
			DBG("Device", "Try using SERIAL");
			Class<?> c = Class.forName("android.os.SystemProperties");
			java.lang.reflect.Method get = c.getMethod("get", String.class);
			deviceID = (String) get.invoke(c, "ro.serialno");
			if(deviceID != null && deviceID.length() > 0 && deviceID != "unknown")
				return deviceID;
		} catch (Exception ignored) {}

		
		/* If device has ANDROID_ID, use it */
		DBG("Device", "Try using ANDROID_ID");
		deviceID = Secure.getString(SUtils.getContext().getContentResolver(), Secure.ANDROID_ID);
		if(deviceID != null && deviceID.length() > 0)
				return deviceID;
						
		/* Final solution, try to use UUID */
		DBG("Device", "Try using UUID");
		String uuidPath = SUtils.getPreferenceString("SDFolder", SD_FOLDER, GAME_ACTIVITY_NAME_STR + "Prefs") + "/.nomedia";	
		deviceID = SUtils.ReadFile(uuidPath);
		if (deviceID == null || deviceID.length() == 0)
		{
			deviceID = UUID.randomUUID().toString();
			deviceID = deviceID.replaceAll("-", "");			
			SUtils.WriteFile(uuidPath, deviceID);
			try
			{
				File uuidFile = new File(uuidPath);
				if(uuidFile.exists())
					uuidFile.setReadOnly();
			} catch (Exception ex){}
		}
		
        return deviceID;
    }
	
	//start of library APIs
	
	// Get the system uptime in milliseconds
	public static long getSystemUpTimeMillis () {
		return android.os.SystemClock.elapsedRealtime ();
	}
		
	// Get the name of the device with format "MANUFACTURER MODEL"
	public static String getDeviceName() {//check another name
		return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL; 
	}

	//already implemented in Device.java
	// public static String getDeviceFirmware () {
		// return android.os.Build.VERSION.RELEASE;
	// }
	
	// Get the device language in format "xx". Will return "" if any error is present
	public static String retrieveDeviceLanguage () {
		String deviceLanguage = "";
		try {
			if (SUtils.getContext() != null) {
				deviceLanguage = SUtils.getContext().getResources().getConfiguration().locale.getLanguage();
			}
		} catch (Exception ex) { 
			deviceLanguage = "";
		}
		if (TextUtils.isEmpty(deviceLanguage))
		{
			try{
				deviceLanguage = Locale.getDefault().getLanguage();
			} catch (Exception ex) { 
				deviceLanguage = "";
			}
		}
		if (TextUtils.isEmpty(deviceLanguage))
			deviceLanguage = "";
			
		if(Locale.getDefault().toString().equals("pt_BR"))
			deviceLanguage = "br";

		return deviceLanguage;
	}
	
	// Get the device region in format "xx_YY" (lang_COUNTRY). Will return "" if any error is present
	public static String retrieveDeviceRegion() {
		Locale theLocale;
		String deviceRegion = "";
		try {
			if (SUtils.getContext() != null) {
				theLocale = SUtils.getContext().getResources().getConfiguration().locale;
				deviceRegion = theLocale.getLanguage () + "_" + theLocale.getCountry();
			}
		} catch (Exception ex) {
			deviceRegion = "";
		}
		if (TextUtils.isEmpty(deviceRegion))
		{
			try {
				theLocale = java.util.Locale.getDefault();
				deviceRegion = theLocale.getLanguage() + "_" + theLocale.getCountry();
			}catch (Exception ex) {
				deviceRegion = "";
			}
		}
		if (TextUtils.isEmpty(deviceRegion))
			deviceRegion = "";
		return deviceRegion;
	}
	
	// Get the device country in format "XX". Will return "" if any error is present
	public static String retrieveDeviceCountry () {
		String deviceCountry = "";
		if (SUtils.getContext() != null)
		{
			try {
				deviceCountry = ((TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getSimCountryIso();//returns the SIM's country
			} catch (Exception e) {
				deviceCountry = "";
			}
			if (TextUtils.isEmpty(deviceCountry))
			{
				try{
					deviceCountry = SUtils.getContext().getResources().getConfiguration().locale.getCountry();
				} catch (Exception e) {
					deviceCountry = "";
				}
			}
		}
		if (TextUtils.isEmpty(deviceCountry))
		{
			try{
				deviceCountry = Locale.getDefault().getCountry();
			}catch (Exception ex) { 
				deviceCountry = "";
			}
		}
		if (TextUtils.isEmpty(deviceCountry))
			deviceCountry = "";
		return deviceCountry;
	}
	
	// Get the carrier from the device
	public static String retrieveDeviceCarrier () {
		String deviceCarrier = "";
		try {
			if (SUtils.getContext() != null) {
				deviceCarrier = ((TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getSimOperator();
			}
		} catch (Exception e) {
			deviceCarrier = "";
		}
		if (TextUtils.isEmpty(deviceCarrier))
			deviceCarrier = "";
		return deviceCarrier;
	}

	// Returns the CPU serials
	public static String retrieveCPUSerial () {
		String cpuSerial = null;
		if (new File("/proc/cpuinfo").exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
				String aLine;
				while ((aLine = br.readLine()) != null) {
					if(aLine.toLowerCase().contains("serial"))
					{
						String[] serialLine = aLine.split(":");
						if(serialLine.length == 2)
						{
							if (!serialLine[1].replace ("0", "").trim().equals (""))//check if Serial is only 0s and return "" 
							{
								cpuSerial = serialLine[1];
							}
						}
					}
				}
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				cpuSerial = "";
			}
		}
		if (TextUtils.isEmpty(cpuSerial))
			cpuSerial = "";
		return cpuSerial.trim();
	}
	//end of library APIs
}
