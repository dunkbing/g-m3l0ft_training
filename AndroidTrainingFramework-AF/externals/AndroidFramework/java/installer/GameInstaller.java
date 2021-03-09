#if USE_INSTALLER
package APP_PACKAGE.installer;


import android.view.MotionEvent;
import android.app.AlertDialog;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import APP_PACKAGE.GLUtils.GLConstants;
#if VERIZON_DRM
import APP_PACKAGE.DRM.Verizon.VerizonDRM;
#elif GLOFT_DRM
import APP_PACKAGE.DRM.Gloft.GloftDRM;
#elif TMOBILE_DRM
import APP_PACKAGE.DRM.TMobile.TMobileDRM;
#elif ORANGE_DRM
import APP_PACKAGE.d;
import APP_PACKAGE.n;
#endif

#if ADS_USE_FLURRY
import APP_PACKAGE.Flurry.GLFlurry;
#endif

//#if AUTO_UPDATE_HEP
//import android.app.AlertDialog;
//#endif
#if USE_BEAM
import APP_PACKAGE.BeamSender;
#endif
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.os.Looper;
import android.util.Log;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.telephony.TelephonyManager;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.Iterator;
import android.util.Pair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.Build;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.provider.Settings;
import android.widget.ProgressBar;
import android.net.Uri;
import java.text.DecimalFormat;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Locale;
import android.content.pm.PackageManager;
import java.net.HttpURLConnection;

import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.ZipFile;
import APP_PACKAGE.GLUtils.DefReader;

import APP_PACKAGE.installer.utils.CRC;
import APP_PACKAGE.installer.utils.GiSettings;
import APP_PACKAGE.installer.utils.HttpClient;
import APP_PACKAGE.installer.utils.PackFile;
import APP_PACKAGE.installer.utils.PackFileReader;

import android.view.WindowManager;

#if USE_TRACKING_FEATURE_INSTALLER
import APP_PACKAGE.installer.utils.Tracker;
#endif

//#if USE_DOWNLOAD_MANAGER
import APP_PACKAGE.installer.utils.Downloader;
import APP_PACKAGE.installer.utils.Defs;

//#endif

#if USE_GAME_TRACKING || USE_MARKET_INSTALLER || GOOGLE_STORE_V3
import APP_PACKAGE.GLUtils.Tracking;
#endif

import APP_PACKAGE.installer.utils.DataInputStreamCustom;
import APP_PACKAGE.installer.utils.ZipInputStreamCustom;
import APP_PACKAGE.installer.utils.DownloadComponent;
import APP_PACKAGE.R;

#if GOOGLE_MARKET_DOWNLOAD
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import java.util.zip.CRC32;
import java.io.RandomAccessFile;
import APP_PACKAGE.installer.utils.SimpleDownload;
import android.provider.Settings.Secure;
#endif
#if GOOGLE_DRM
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;
import android.provider.Settings.Secure;
#endif

#if GOOGLE_DRM || GOOGLE_MARKET_DOWNLOAD
//import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
#endif

import APP_PACKAGE.GLUtils.Encrypter;
import android.widget.RemoteViews;

import android.os.Handler;


import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.Manifest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Enumeration;
import java.util.zip.*;

#if USE_GOOGLE_ANALYTICS_TRACKING
import APP_PACKAGE.utils.GoogleAnalyticsTracker;
import APP_PACKAGE.utils.GoogleAnalyticsConstants;
#endif
import APP_PACKAGE.GLUtils.LowProfileListener;
import java.util.HashMap;

public class GameInstaller extends Activity implements Runnable
//#if USE_DOWNLOAD_MANAGER
, Defs
//#endif
{
#if GOOGLE_DRM || GOOGLE_MARKET_DOWNLOAD
	public static String mKey = null;
	public static final byte[] SALT = new byte[] 
	{
		-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64,	89
	};
	public static final long GOOGLE_GRACE_PERIOD = 14*24*60*60;
	
	private static String epk 			= null;
	private static final String DKEY	= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0apk+aD46nv4t2utni2rYmToO0pFHM9UlqohWyCvaYQo18ElxTRyPLN5cHpyypZltSPNx+QnlZYV9aM/CVoKgTH58djTrnIok8YABn1tz89nHR9wsP5Akz5js3E2AaiJLWm/HsAuERH3pLg/oHoWTBBr0+iXuWUGeLH0KATN/6STKLNZLpdXyHXyQayUzFdSuYKOOnxHVJX79ty5kluCqRlSpfHHOd93lKi6L8+p92RB9pwcXeEjjHfAw7EGzdS7slkE1na/L75TcoBNhQxG9RdvZfnXfWnHJ07n+051qlVwF7FXgbpHrnOpC9uJOFjrSJa+VM25pQfymmzoR6LEDwIDAQAB";
	private static final String CPK 	= AMP_EPK;
	private static final String KEY		= GGC_GAME_CODE;
	
	private String getKey()
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
#if GOOGLE_DRM
	private LicenseCheckerCallback mLicenseCheckerCallback;
	private LicenseChecker mChecker;
	private AESObfuscator obfuscator;
	GDRMPolicy aPolicy;
	private boolean bIsNetworkOK = false;
#endif

#if GOOGLE_DRM || GOOGLE_MARKET_DOWNLOAD
	private AlertDialog.Builder alertDialog;
	private ProgressDialog dialog;
#endif

	public static boolean sbStarted = false;
	public static boolean s_isPauseGame = false;
	public static GameInstaller m_sInstance = null;

	public static String mPreferencesName = GAME_ACTIVITY_NAME_STR + "Prefs";

	private int mCurrentVersion			= -1;			// The latest version available for this device
	private boolean mIsGenericBuild		= true;			// Whether the url point to a real version or the generic one
	private boolean mIsOverwrittenLink	= false;		// Whether the url point to a real version or the generic one

	private static boolean sUpdateAPK	=  ((USE_MARKET_INSTALLER == 0) && (GOOGLE_MARKET_DOWNLOAD == 0));			// On an update for shop, the first thing to check is the availability of a new apk

	public static String m_portalCode = "";

#if VERIZON_DRM
	protected VerizonDRM mVerizonLicense = null;
#elif GLOFT_DRM
	protected GloftDRM mGloftLicense = null;
#elif TMOBILE_DRM
	protected TMobileDRM mTMobileLicense = null;
	public final int SUBSTATE_INIT_TMOBILE     = -1;
	public final int SUBSTATE_WAITING_TMOBILE  = SUBSTATE_INIT_TMOBILE + 1;
	public final int SUBSTATE_PROCESS_TMOBILE  = SUBSTATE_WAITING_TMOBILE + 1;
	public int mTMobileSubstate;
#elif ORANGE_DRM
	protected d mOrangeLicense = null;
	private boolean propertiesChanged = false;
#endif
	
#if USE_INSTALLER_SLIDESHOW
	private int lastDisplayedIndex = 0;
	private ImageView slideShowContainer = null;
	private	java.util.List<Integer> slideShowImageIds;
	#if USE_INSTALLER_SLIDESHOW_TEXTS
	private TextView slideShowText = null;
	private	java.util.List<Integer> slideShowTextsIds;
	private int slideShowTextsColor = android.graphics.Color.parseColor("#000000");
	#endif
#endif

	public static String SaveFolder = null;
	private String INFO_FILE_NAME 	= "/pack.info";
	
	private boolean mbLicense 				= false;
	private int miLicenseMessage				= 0;
	public static TelephonyManager mDeviceInfo;

	private final boolean DEBUG 			= !true;
#if USE_HEP_ANTIPIRACY
	private final boolean HAS_PIRACY_TEST 	= true;
#else
	private final boolean HAS_PIRACY_TEST 	= (VERIZON_DRM == 1) || (GOOGLE_DRM == 1) || (GLOFT_DRM == 1) || (TMOBILE_DRM == 1) || (ORANGE_DRM == 1) ||(GOOGLE_MARKET_DOWNLOAD == 1);
#endif //USE_HEP_ANTIPIRACY

	private final boolean HAS_AUTO_UPDATE	= (USE_UPDATE_VERSION == 1);
#if AUTO_UPDATE_HEP
	boolean test_version = (USE_AUTO_UPDATE_HEP_GOLD == 0);  //add the &test to the url request. When true &test=1.
	//public final static String UPDATE_HEP_URL = "http://confirmation.gameloft.com/partners/android/update_check_hep.php";
    public final static String UPDATE_HEP_URL = "https://secure.gameloft.com/partners/android/update_check_hep.php";
	
	//Unknown Sources setting validation
	boolean canInstallFromOtherSources;
	AlertDialog alert_dialog;
	AlertDialog.Builder alert_builder;
  
	//public final int DATE_TO_UPDATE_NEW_VERSION = 1;  //each day check for updates
//#else
	//public final int DATE_TO_UPDATE_NEW_VERSION = 7;
#endif
	public final int DATE_TO_UPDATE_NEW_VERSION = UPDATE_NEW_VERSION_DAYS;

	private final int WIFI_3G = 0; 
	private final int WIFI_ONLY = 1;
	private final int WIFI_3G_ORANGE_IL = 2;
	
	private int WIFI_MODE = WIFI_3G;
	

	private boolean isWifiSettingsOpen = false;

	private final int BUFFER_SIZE			= 32 * 1024;
	
	private final int NOTIFICATION_ID 		= 7176;
	NotificationManager nm;

	private final String GAME_ACTIVITY				= STR_APP_PACKAGE+"."+GAME_ACTIVITY_NAME_STR;

	private String SERVER_URL = "";
	
	private ArrayList<DownloadComponent> mDownloadComponents = new ArrayList<DownloadComponent>();;

	public static String sd_folder = "";
	public static String DATA_PATH = "";

//#if ENABLE_DOWNLOAD_NATIVE	
	public static String LIBS_PATH = "/libs/";
	public static boolean s_files_changed = false;
//#endif
	
	Vector<PackFile> mPackFileInfo = null;
	Vector<PackFile> mRequiredResources = new Vector<PackFile>();
	Vector<PackFile> mOfflineResources = new Vector<PackFile>();
	long mRequiredSize = 0;
	long mbAvailable = 0;
	long mbRequired = 0;
#if GOOGLE_MARKET_DOWNLOAD
	long mbAvailableObb = 0;
	long mbRequiredObb = 0;
	long mbAvailableExtra = 0;
	long mbRequiredExtra = 0;
#endif
	boolean mRequestAll = false;
	long mTotalSize = 0;
	long mDownloadedSize = 0;
	long mTotalSizeUnzipCheck = 0;
	
	DecimalFormat formatter;

	final int STATUS_NO_ERROR					=  0;
	final int STATUS_NETWORK_UNAVALIABLE 		= -1;
	final int STATUS_FILE_NOT_FOUND				= -2;
	final int STATUS_NO_EXTERNAL_STORAGE_FOUND	= -3;
	final int STATUS_ERROR						= -4;
	final int STATUS_DOWNLOADING				= -5;

	private int mStatus = STATUS_NO_ERROR;	

	private  boolean mInstallerStarted 	= false;
	private final int GL_LOGO_DELAY = DEBUG? 100:3000;
	private final int WAITING_FOR_WIFI_IN_BACKGROUND_DELAY = 30000;
	private long mLogoTimeElapsed = 0;
	private long mWaitingForWifiTimeElapsed = 0;
	AssetManager mAssetManager;
	HttpClient	mClientHTTP;
	
	boolean isUnzipingInterrupted = false;

#if USE_SIMPLIFIED_INSTALLER
	boolean hasUserConfirmedDataUsage = false;
	private int k_downloadLimit = -1; //size in MB
#endif

	public final int GI_STATE_CHECK_PIRACY 						= 0;
	public final int GI_STATE_PIRACY_ERROR 						= 1;
	public final int GI_STATE_CHECK_ERRORS						= 2;
	public final int GI_STATE_GAMELOFT_LOGO						= 3;
	public final int GI_STATE_SOLVE_ERROR						= 4;
//#if !USE_SIMPLIFIED_INSTALLER
	public final int GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION	= 5;
	public final int GI_STATE_FIND_WIFI 						= 6;
	public final int GI_STATE_WAITING_FOR_WIFI					= 7;
	public final int GI_STATE_CONFIRM_WAITING_FOR_WIFI			= 8;
//#endif
	public final int GI_STATE_DOWNLOAD_FILES_QUESTION			= 9;
//#if !USE_SIMPLIFIED_INSTALLER
	public final int GI_STATE_CONFIRM_3G						= 10;
	public final int GI_STATE_NO_DATA_CONNECTION_FOUND			= 11;
//#endif
	public final int GI_STATE_DOWNLOAD_FILES					= 12;
	public final int GI_STATE_DOWNLOAD_FILES_SUCCESFUL			= 13;
	public final int GI_STATE_DOWNLOAD_FILES_ERROR				= 14;
#if !USE_SIMPLIFIED_INSTALLER
	public final int GI_STATE_DOWNLOAD_ANYTIME					= 19;
#endif
	public final int GI_STATE_VERIFYING_CHECKSUM				= 20;
	public final int GI_STATE_FINALIZE 							= 21;

	public final int GI_STATE_INIT_UPDATE_VERSION 				= 23;
	public final int GI_STATE_SEND_REQUEST 						= 24;
	public final int GI_STATE_WAIT_REPONSE						= 25;
	public final int GI_STATE_RECEIVE_REQUEST 					= 26;
	public final int GI_STATE_CONFIRM_UPDATE_VERSION    		= 27;
	public final int GI_STATE_RETRY_UPDATE_VERSION    			= 28;
	public final int GI_STATE_NO_NEW_VERSION    				= 29;
	public final int GI_STATE_UPDATE_GET_NEW_VERSION			= 30;

	// Market Installer state
	public final int GI_STATE_DEVICE_NOT_SUPPORTED					= 31;
    public final int GI_STATE_CHECK_JP_HD_SUBCRIPTION				= 32;
    public final int GI_STATE_CHECK_JP_HD_SUBCRIPTION_NO_NETWORK	= 33;
	#if AUTO_UPDATE_HEP
	public final int GI_STATE_UPDATE_FINISH			= 40;
	#endif
	
	public final int GI_STATE_UNZIP_DOWNLOADED_FILES				= 41;
	
	public final int GI_STATE_NO_GP_ACCOUNT_DETECTED				= 42;

	public final int SUBSTATE_SHOW_LAYOUT 					= -1;
	public final int SUBSTATE_INIT		 					= 0;
	public final int SUBSTATE_PROCESS	 					= SUBSTATE_INIT + 1;
	public final int SUBSTATE_DOWNLOAD_CREATE_CLIENT 		= SUBSTATE_PROCESS + 1;
	public final int SUBSTATE_CHECK_MASTER_FILE				= SUBSTATE_DOWNLOAD_CREATE_CLIENT + 1;
	public final int SUBSTATE_DOWNLOAD_DOWNLOAD_FILE		= SUBSTATE_CHECK_MASTER_FILE + 1;
	public final int SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION = SUBSTATE_DOWNLOAD_DOWNLOAD_FILE + 1;

	// Download manager sub states
	public final int SUBSTATE_DOWNLOAD_MANAGER_INIT 		= SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION + 1;
	public final int SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE = SUBSTATE_DOWNLOAD_MANAGER_INIT + 1;

	// TODO: Market place sub states
	public final int SUBSTATE_DOWNLOAD_CHECK_SPACE			= SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE + 1;
	public final int SUBSTATE_DOWNLOAD_LAUNCH				= SUBSTATE_DOWNLOAD_CHECK_SPACE + 1;
	public final int SUBSTATE_CHECK_LATEST_VERSION			= SUBSTATE_DOWNLOAD_LAUNCH + 1;
	
	public final static int LAYOUT_CHECKING_REQUIRED_FILES				= 0;
	public final static int LAYOUT_CONFIRM_3G							= 1;
	public final static int LAYOUT_CONFIRM_UPDATE						= 2;
	public final static int LAYOUT_CONFIRM_WAITING_FOR_WIFI				= 3;
	public final static int LAYOUT_DOWNLOAD_ANYTIME						= 4;
	public final static int LAYOUT_DOWNLOAD_FILES						= 5;
	public final static int LAYOUT_DOWNLOAD_FILES_CANCEL_QUESTION		= 7;
	public final static int LAYOUT_DOWNLOAD_FILES_ERROR					= 8;
	public final static int LAYOUT_DOWNLOAD_FILES_NO_WIFI_QUESTION		= 9;
	public final static int LAYOUT_DOWNLOAD_FILES_QUESTION				= 10;
#if USE_JP_HD_SUBSCRIPTION
	public final static int LAYOUT_JP_HD_SUBSCRIPTION					= 11;
	public final static int LAYOUT_JP_HD_SUBSCRIPTION_NO_NETWORK		= 12;
#endif
	public final static int LAYOUT_LICENSE_INFO							= 13;
	public final static int LAYOUT_LOGO									= 14;
#if USE_MARKET_INSTALLER || GOOGLE_MARKET_DOWNLOAD
	public final static int LAYOUT_MKP_DEVICE_NOT_SUPPORTED				= 15;
#endif
	public final static int LAYOUT_NO_DATA_CONNECTION_FOUND				= 16;
	public final static int LAYOUT_RETRY_UPDATE_VERSION					= 17;
	public final static int LAYOUT_SD_SPACE_INFO						= 18;
	public final static int LAYOUT_SEARCHING_FOR_NEW_VERSION			= 19;
	public final static int LAYOUT_SEARCHING_FOR_WIFI					= 20;
	public final static int LAYOUT_SUCCESS_DOWNLOADED					= 21;
	public final static int LAYOUT_VERIFYING_FILES						= 22;
	public final static int LAYOUT_WAITING_FOR_WIFI						= 23;
	public final static int LAYOUT_BLACK								= 24;
#if ORANGE_DRM
	public final static int LAYOUT_ORANGE_DRM							= 25;
#endif
#if TMOBILE_DRM
	public final static int LAYOUT_CHECKING_LICENSE						= 26;
#endif
	public final static int LAYOUT_UNZIP_FILES							= 27;
	public final static int LAYOUT_UNZIP_FILES_CANCEL_QUESTION			= 28;
	
	public final static int LAYOUT_NO_GP_ACCOUNT_DETECTED				= 29;

	public static Downloader m_pDownloader;
	public static long m_iDownloadedSize;

	public static long m_iRealRequiredSize;
	public int mSubstate;
	public int mPrevSubstate;
	public int mNumberOfNeededFiles = 0;
	public String mReqRes;
	public int currentDownloadFile;

	public int mState;
	public int mPrevState;
	public int m_nLogoStep;

	public int NO_ACTION		= 0;
	public int ACTION_NO		= 1;
	public int ACTION_YES		= 2;
	public int ACTION_CANCEL	= 3;

	public final int ERROR_NO_SD					= 0;
	public final int ERROR_NO_ENOUGH_SPACE			= 1;
	public final int ERROR_FILES_NOT_VALID			= 2;
	public final int ERROR_NEED_FILE_CHECKSUM_TEST	= 3;

#if USE_MARKET_INSTALLER || !GOOGLE_MARKET_DOWNLOAD
	public final int ERROR_NO_NATIVE				= 4;
#endif

	public int[] errorPresent =
	{
		0, //ERROR_NO_SD
		0, //ERROR_NO_ENOUGH_SPACE
		0, //ERROR_FILES_NOT_VALID
		0, //ERROR_NEED_FILE_CHECKSUM_TEST
#if USE_MARKET_INSTALLER || GOOGLE_MARKET_DOWNLOAD
		0, //ERROR_NO_NATIVE
#endif
	};

	public void setErrorPresent(int err, int value)
	{
		if(value > 0)
			DBG("GameInstaller","error for "+err);
		errorPresent [err] = value;
	}
	
	public void resetErrorPresent()
	{
		for(int i = 0; i < errorPresent.length; i++)
		{
			errorPresent [i] = 0;
		}
	}

	public boolean getErrorPresent(int err)
	{
		return (errorPresent[err] == 1);
	}
	
	private int currentNetUsed = -1;
	
	WifiManager	mWifiManager;
	ConnectivityManager mConnectivityManager;
	WifiLock		mWifiLock;
	WakeLock 		mWakeLock;
	
	public boolean wasWifiActivatedByAPP = false;
	public boolean wasWifiDeActivatedByAPP = false;
	
	public GameInstaller()
	{		
		currentDownloadFile = 0;
		SUtils.setContext((Context)this);
	}
	public static void startGame()
	{
		sbStarted = true;
		m_sInstance.finishSuccess();
	}
	
	/* returns true if files have been modified, otherwise false */
	public boolean unZip(String fileName, String file_path, String folderName, String strOutputPath, boolean overwrite)
	{
		boolean result = false;
		String dstFolder = strOutputPath + "/" + folderName + "/";
		String final_file = file_path + "/" + fileName;
		DBG("SUtils", "Unzipping file "+final_file+" to "+strOutputPath);	
		try 
		{			
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(final_file);
			Enumeration enumeration = zipFile.entries();
		
			while (enumeration.hasMoreElements() && !isUnzipingInterrupted)
			{
				ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();			
				
				String dstFileName = null;
							
				if(zipEntry.getName().endsWith(".so"))
				{
					dstFileName = GameInstaller.LIBS_PATH + zipEntry.getName();					
					GameInstaller.addNativeLib(zipEntry.getName());
				}
				else				
				{
					dstFileName = dstFolder + zipEntry.getName();
				}
				
				DBG("SUtils","Extracting: " + dstFileName);
				
				File dstFile = new File(dstFileName);	

				//Create folder if not exists
				File parent = new File(dstFile.getParent());
				DBG("SUtils","Creating folder "+parent.getName());
				parent.mkdirs();				
				parent = null;	

				if(dstFile.exists() && !dstFile.isDirectory())
				{
					if(overwrite && (dstFile.length() != zipEntry.getSize()))
					{
						DBG("SUtils","Unzipping: file already exist replacing" );
						dstFile.delete();
					}
					else
					{
						mCurrentProgress += dstFile.length()/1024;
						updateProgressBar();
						continue;
					}
				}
				
				
				/*if(!dstFile.isDirectory())
					dstFile.createNewFile();
				else
					continue;*/
					
				if(dstFileName.endsWith("/"))
				{
					DBG("SUtils","directory: "+dstFileName);
					continue;
				}
				
				result = true;
				
				dstFile = null;			

				BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));

				int size;
				byte[] buffer = new byte[16*1024];
				
				
				FileOutputStream fos = new FileOutputStream(dstFileName);	

				if(dstFileName.endsWith(".so"))
				{
					GameInstaller.makeLibExecutable(dstFileName);
				}			
				BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

				while ((size = bis.read(buffer, 0, buffer.length)) != -1 && !isUnzipingInterrupted) {
					bos.write(buffer, 0, size);
					
					mCurrentProgress += size/1024;
					updateProgressBar();
					
				}
				updateProgressBar();
		
				DBG("SUtils","Done Extracting: " + dstFileName);

				bos.flush();
				bos.close();
				fos.close();
				bis.close();
			}
			//File zip = new File(dstFolder + fileName);
			//zip.delete();
		} catch (IOException e){
			DBG_EXCEPTION(e);
			return result;
		} catch (Exception e){
			DBG_EXCEPTION(e);
			return result;
		}
	
		return result;
	}
	
	public boolean unZipCheck(String[] fileNames, String file_path, String folderName, String strOutputPath, boolean overwrite)
	{
		boolean result = true;
		String dstFolder = strOutputPath + "/" + folderName + "/";
		for(int i=0;i<3;i++)
		{
			String final_file = file_path + "/" + fileNames[i];
			DBG("SUtils", "Unzipping check file "+final_file+" to "+strOutputPath);
			try 
			{			
				java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(final_file);
				Enumeration enumeration = zipFile.entries();
			
				while (enumeration.hasMoreElements())
				{
					ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();			
					
					String dstFileName = null;
								
					if(zipEntry.getName().endsWith(".so"))
					{
						dstFileName = GameInstaller.LIBS_PATH + zipEntry.getName();
						GameInstaller.addNativeLib(zipEntry.getName());
					}
					else
					{
						dstFileName = dstFolder + zipEntry.getName();
					}
					
					DBG("SUtils","Checking: " + dstFileName);
					
					File dstFile = new File(dstFileName);	

					//Create folder if not exists
					File parent = new File(dstFile.getParent());
					DBG("SUtils","Creating folder "+parent.getName());
					parent.mkdirs();				
					parent = null;	
					
					if(dstFileName.endsWith("/"))
					{
						DBG("SUtils","directory: "+dstFileName);
						continue;
					}

					boolean invalidCRC = false;
					//if(!dstFile.isDirectory())
					{
						mCurrentProgress ++;
					}
					
					if(overwrite)
						invalidCRC = !CRC.isValidChecksum(dstFile.getAbsolutePath(), zipEntry.getCrc());
						
					//DBG("GameInstaller", "CRC for unzip: " + invalidCRC + " " + dstFile.getAbsolutePath() + " " );		
					
					if(!dstFile.isDirectory())
					{
						if(dstFile.exists())
						{				
							if( (dstFile.length() != zipEntry.getSize()) || invalidCRC )
							{
								DBG("GameInstaller", i+" File "+zipEntry.getName()+" is different");
								boolean found = false;
								try
								{
									int j=i+1;
									if(j<2)
									{
										String final_file2 = file_path + "/" + fileNames[j];
										DBG("GameInstaller","second file: "+final_file2);
										java.util.zip.ZipFile zipFile2 = new java.util.zip.ZipFile(final_file2);
										Enumeration enumeration2 = zipFile2.entries();
											
										while (enumeration2.hasMoreElements())
										{
											ZipEntry zipEntry2 = (ZipEntry) enumeration2.nextElement();	
											if(zipEntry.getName().equals(zipEntry2.getName()))
												found = true;
										}
									}
									
								} catch (Exception e){DBG("GameInstaller","Exception checking for the second srchive");}
								if(!found)
								{
									DBG("GameInstaller","Unzipping: file already exist replacing" );
									dstFile.delete();
									result = false;
								}
								else
								{
									DBG("GameInstaller","File is found in the second archive" );
								}
							}
						}
						else
						{
							DBG("GameInstaller", i+" unzipcheck: file "+ dstFile.getAbsolutePath() +" don't exists");
							result = false;
						}
					}
					
					updateProgressBar();
				}
			
			} catch (IOException e){
				DBG_EXCEPTION(e);
				return result;
			} catch (Exception e){
				DBG_EXCEPTION(e);
				return result;
			}
		}
		return result;
	}
	
	public boolean unZipCheck(String fileName, String file_path, String folderName, String strOutputPath, boolean overwrite)
	{
		boolean result = true;
		String dstFolder = strOutputPath + "/" + folderName + "/";
		String final_file = file_path + "/" + fileName;
		DBG("SUtils", "Unzipping check file "+final_file+" to "+strOutputPath);
		try 
		{			
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(final_file);
			Enumeration enumeration = zipFile.entries();
		
			while (enumeration.hasMoreElements())
			{
				ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();			
				
				String dstFileName = null;
							
				if(zipEntry.getName().endsWith(".so"))
				{
					dstFileName = GameInstaller.LIBS_PATH + zipEntry.getName();
					GameInstaller.addNativeLib(zipEntry.getName());
				}
				else
				{
					dstFileName = dstFolder + zipEntry.getName();
				}
				
				DBG("SUtils","Checking: " + dstFileName);
				
				File dstFile = new File(dstFileName);	

				//Create folder if not exists
				File parent = new File(dstFile.getParent());
				DBG("SUtils","Creating folder "+parent.getName());
				parent.mkdirs();				
				parent = null;	
				
				if(dstFileName.endsWith("/"))
				{
					DBG("SUtils","directory: "+dstFileName);
					continue;
				}

				boolean invalidCRC = false;
				//if(!dstFile.isDirectory())
				{
					mCurrentProgress += dstFile.length();
				}
				
				if(overwrite)
					invalidCRC = !CRC.isValidChecksum(dstFile.getAbsolutePath(), zipEntry.getCrc());
					
				DBG("GameInstaller", "CRC for unzip: " + invalidCRC + " " + dstFile.getAbsolutePath() + " " );		
				
				if(!dstFile.isDirectory())
				{
					if(dstFile.exists())
					{
						if( (dstFile.length() != zipEntry.getSize()) || invalidCRC )
						{
							DBG("SUtils","Unzipping: file already exist replacing" );
							dstFile.delete();
							result = false;
						}
					}
					else
					{
						DBG("GameInstaller", "file "+ dstFile.getAbsolutePath() +" don't exists");
						result = false;
					}
				}
				
				updateProgressBar();
			}
		
		} catch (IOException e){
			DBG_EXCEPTION(e);
			return result;
		} catch (Exception e){
			DBG_EXCEPTION(e);
			return result;
		}
		return result;
	}
	
	public void unpackAzm(String filename, String strOutputPath)
	{
	try
	{

		String pack_info = strOutputPath+"/pack.info";
		
		DBG("SUtils","packinfo: "+pack_info);
		DataInputStream DIStream = new DataInputStream(new FileInputStream(filename));
		PackFileReader configRead = new PackFileReader(this);
		
		int packLength = DIStream.readInt();
			
			FileOutputStream fOut = new FileOutputStream(pack_info);
			int readSize = 0;
			while(readSize < packLength)
			{
				DBG("GameInstaller", "getRequiredResourcesValues(), reading packet...");
				int section  = packLength - readSize;
				if(section > BUFFER_SIZE)
					section = BUFFER_SIZE;
				byte[] tmpByte = new byte[section];
				DIStream.readFully(tmpByte);
				fOut.write(tmpByte);
				fOut.flush();
				readSize += section;
				tmpByte = null;
			}
			fOut.close();
			fOut = null;
			
			Vector<PackFile> fileInfo = configRead.read(pack_info);
            //long split_size = configRead.getSplitSize();
           // System.out.println("        Split Size: " + split_size);
            //int length1 = (int)new File(packInfo).length();
            //mDIStream.skipBytes(length1+4);
            int len = fileInfo.size();
            DBG("SUtils","The length of the files: "+len);
            PackFile currentFileData = null;
            java.io.DataOutputStream currentFile = null;
            byte[] buffer = new byte[1*1024*1024];
			
			DataInputStreamCustom dic = new DataInputStreamCustom(DIStream);
			
			int FILE_READ_SIZE = 32 * 1024;
			byte[] m_temp = new byte[FILE_READ_SIZE];
            for (int i = 0; i < len; i++) {
			
				if(isUnzipingInterrupted)
					break;
					
				currentFileData = fileInfo.elementAt(i);
                //String folder = strOutputPath + currentFileData.getFolder() + "\\";

				File file = new File(strOutputPath + "/" + currentFileData.getZipName());

				dic.setMax(currentFileData.getZipLength());
				ZipInputStreamCustom m_zin = new ZipInputStreamCustom(dic);
				ZipEntry zipEntry = m_zin.getNextEntry();
				
				int n;
				int nn = 0;
				String folder = currentFileData.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/");
				String final_path = strOutputPath + "/" + folder + "/" +zipEntry.getName();
				
				String path;
				if (final_path.endsWith("/"))
				path = final_path;
			else
				path = new File(final_path).getParent();
			
			// Create the path and .nomedia file
			if(path != null)
			{
				File parent = new File(path);
				if (!parent.exists())
				{
					parent.mkdirs();
					parent = null;
				}
			}
				
				//final_path.replace(".\\\\", "/");
				DBG("SUtils", "Unzipping "+final_path+"...");
				// Native libs go on device
				if(final_path.endsWith(".so"))
				{
					final_path = GameInstaller.LIBS_PATH + currentFileData.getName();
					GameInstaller.addNativeLib(currentFileData.getName());
				}
				FileOutputStream fos = new FileOutputStream(final_path);	

				if(final_path.endsWith(".so"))
				{
					GameInstaller.makeLibExecutable(final_path);
				}
				BufferedOutputStream bos = new BufferedOutputStream(fos, m_temp.length);
				while ((n = m_zin.read(m_temp, 0, FILE_READ_SIZE)) > -1)
				{
					nn += n;
					bos.write(m_temp, 0, n);
				}
				mCurrentProgress += nn/1024;
				
				if (!bIsPaused)
				{
					this.runOnUiThread(new Runnable ()
					{
						public void run ()
						{
							try
							{
								ProgressBar bar = (ProgressBar)findViewById(R.id.data_downloader_linear_progress_bar);
								if (bar != null)
								{
									bar.setProgress(mCurrentProgress);
									//DBG("GameInstaller", "downloaded size: "+((int)(mDownloadedSize / 1024))+" prog: "+mCurrentProgress+"total size: "+mTotalSize);
								}
								
							}
							catch (Exception e)
							{
								DBG_EXCEPTION(e);
							}
						}
					});
				}
				
		
				
				bos.flush();
				bos.close();
				fos.close();
				
				m_zin.closeEntry();
				dic.skipToMax();
				dic.resetPos();
				
				DBG("SUtils", "done unpacking file: "+zipEntry.getName());
				
			}
		}catch(Exception e){DBG_EXCEPTION(e);DBG("SUtils", "Exception extracting amz");}
	}
	
	public static String marketPath = android.os.Environment.getExternalStorageDirectory() + "/Android/obb/" + STR_APP_PACKAGE;
	
	private void setPath(long mbrequired)
	{
		DBG("GameInstaller", "Setup paths with mbrequired: "+mbrequired);
		if (sd_folder.equals("")) //!!here just check for extra
		{
			for (Pair<String, Long> aux : pathAndSizes)
			{
				DBG("GameInstaller", "Check " + aux.first + " : " + mbrequired +  " of " + aux.second + " avaialable.");
				if (aux.second >= mbrequired)
				{
					sd_folder = aux.first;
					SUtils.setPreference("SDFolder", sd_folder, mPreferencesName);
					DATA_PATH = sd_folder + "/";
					DBG("GameInstaller", "sd_folder has been changed: " + sd_folder);
					break;
				}
			}
		}
	}

	private int getRequiredResourcesSize()
	{
		int size = 0;
		for(DownloadComponent component : mDownloadComponents)
		{
			size += component.getRequiredResourcesSize();
		}
		return size;
	}
	private int getInfoFilesLength()
	{
		int length = 0;
		for(DownloadComponent component : mDownloadComponents)
		{
			length += component.getInfoFileLength();
		}
		
		return length;
	}
	private int validateFiles()
	{
		DBG("GameInstaller","validateFiles()");
	
		int total_size = 0;
		if (mState == GI_STATE_VERIFYING_CHECKSUM)
		{
			ProgressBar bar = (ProgressBar)findViewById(R.id.data_downloader_linear_progress_bar);
			
			for(DownloadComponent component : mDownloadComponents)
			{
				total_size += component.getInfoFileLength();
			}
			total_size += mOfflineResources.size();
			
			if(bar != null)
				bar.setMax((int)(total_size));
			
			DBG("GameInstaller" ,"verifying files progress bar set to "+total_size);
		}
		
		
							
		int result = 0;
		mTotalSize = 0;
		mDownloadedSize = 0;
		mbRequired = 0;
		m_iRealRequiredSize = 0;
		mCurrentProgress = 0;
		
		long mb_required_extra = 0;
		for(DownloadComponent component : mDownloadComponents)
		{
			DBG("GameInstaller" ,"component to veirfy:" +component.getName());
			if(component.validateFiles(mRequestAll, this) == 1)
			{
				m_iRealRequiredSize += component.getRealRequiredSize();
				result = 1;
			}
			
			mTotalSize += component.getTotalSize();
			mDownloadedSize += component.getDownloadedSize();
			
			 ArrayList<String> sos = component.getSoFiles();
			if(sos != null)
			{
				for (String so : sos) // [A] Libraries were added multiple times
				{
					addNativeLib(so);
				}
				//sNativeLibs.addAll(component.getSoFiles());
			}
			//if(!component.getName().startsWith("patch") && !component.getName().startsWith("main"))//if we have an extra file	
				//setPath(component.getMBRequiredJustArhive());
			if(!component.fixedPath())
			{
				DBG("GameInstaller", "component with name: "+component.getName()+" hasn't fixed path");
				mb_required_extra += component.getMBRequired();
			}
				
			
		}
		
		DBG("GameInstaller","total size: "+mTotalSize+" downloaded size: "+mDownloadedSize+" progress: "+mCurrentProgress);
		
		if (mState == GI_STATE_VERIFYING_CHECKSUM)
		{
			for(int i = 0; i < mOfflineResources.size(); i++)
			//for(PackFile file : mOfflineResources)
			{
				String res_name = mOfflineResources.get(i).getName();
				//DBG("GameInstaller", "offline res: "+res_name);
				boolean found = false;
				/*for(DownloadComponent component : mDownloadComponents)
				{
					DBG("GameInstaller", "component name:"+component.getName());
				}*/
				for(DownloadComponent component : mDownloadComponents)
				{
					if(component.haveFile(res_name))
					{
						DBG("GameInstaller", "found equal for comp: "+component.getName());
						found = true;
						break;
					}
				}
				if(!found)
				{
					DBG("GameInstaller","prepare to delete file"+res_name);
					String path = "";
					if(res_name.startsWith("main") || res_name.startsWith("patch"))
						path = marketPath + mOfflineResources.get(i).getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") +"/"+ mOfflineResources.get(i).getName();
					else
						path = DATA_PATH +  mOfflineResources.get(i).getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") +"/"+ mOfflineResources.get(i).getName();
					
					File file = new File(path);
					if(file.exists())
					{
						DBG("GameInstaller","deleting file"+res_name);
						file.delete();
					}
				}
				mCurrentProgress ++;
				updateProgressBar();
			}
			
		}
		
		if(mb_required_extra > 0)
			setPath(mb_required_extra);
		
		return result;
	}

	private void createNoMedia(String path)
	{
		DBG("GameInstaller", "createNoMedia()");
		if(path == null)
			return;
		
		try
		{
			File parent = new File(path);
			if (!parent.exists())
			{
				parent.mkdirs();
				parent = null;
			}
			File nomedia = new File(path + "/.nomedia");
			if (!nomedia.exists())
				nomedia.createNewFile();
			nomedia = null;
		} catch (Exception e){}
	}

	private boolean isValidResourceVersion()
	{
//#if RELEASE_VERSION
		File file = new File(DATA_PATH);
		if (!file.exists())
			return false;

		boolean requestAll = true;
		String currentVersion = readVersion();
		if (currentVersion != null && currentVersion.length() > 0 && currentVersion.compareTo(GAME_VERSION_CODE) == 0)
		{
			requestAll = false;
		}
		if (requestAll == true)
		{
			SUtils.setPreference("ZipHasCRCtest", true, mPreferencesName);
		}
		return requestAll;
//#else
		//Not calculate checksum if debug version, save us a lot of time
	//return false;
//#endif
	}

	private String readVersion()
	{
		String saveFileName = SaveFolder + "/prefs/gl_ver";
		return SUtils.ReadFile(saveFileName);
	}
	
	private void saveVersion()
	{
		saveVersion(GAME_VERSION_CODE);
	}
	private void saveVersion(String version)
	{
		try
		{
			String saveFileName = SaveFolder + "/prefs/gl_ver";
			File pref = new File(saveFileName);
			
			if (!pref.exists())
			{
				File parent = new File(pref.getParent());
				parent.mkdirs();
				parent = null;
			}
			else
			{
				pref.delete();
			}
			SUtils.WriteFile(saveFileName, version);
		}
		catch (Exception ex)
		{
			DBG_EXCEPTION(ex);
		}
	}

	public DataInputStream mDIStream;
	private boolean getDataStream(String url)
	{
		DBG("GameInstaller","getdatastream url = : " + url);
		InputStream in = null;
		try
		{
			if(mClientHTTP == null)
			{
				mClientHTTP = new HttpClient();
			}
			else
			{
				mClientHTTP.close();
			}
			
			in = mClientHTTP.getInputStream(url);
			
			if(in == null)
			{
				mStatus = STATUS_FILE_NOT_FOUND;
				ERR_TOAST(ToastMessages.GameInstaller_getDataStream.StatusFileNotFound);
				
				return false;
			}
			
			if (mDIStream != null)
			{
				mDIStream.close();
				mDIStream = null;
			}
			
			mDIStream = new DataInputStream(in);
			
			return true;
		}
		catch (SocketTimeoutException ste)
		{
			ERR_TOAST(ToastMessages.GameInstaller_getDataStream.SocketTimeoutException);
			mClientHTTP.incrementConnectionTimeout();
		}
		catch (FileNotFoundException fnf)
		{
			ERR_TOAST(ToastMessages.GameInstaller_getDataStream.FileNotFoundException);
			DBG_EXCEPTION(fnf);
			mStatus = STATUS_FILE_NOT_FOUND;
		}
		catch (Exception e)
		{
			ERR_TOAST(ToastMessages.GameInstaller_getDataStream.Exception);
			DBG_EXCEPTION(e);
			mStatus = STATUS_NETWORK_UNAVALIABLE;
		}
		destroyObjects();
		return false;
	}

	FileOutputStream out = null;
	int mDownloadSize = 0;
	int mCurrentSize = 0;
	static long slLastIndex = 0;
	
	PackFile mCurrPackFile = null;
	int mCurrFileSize = 0;

	public void destroyObjects()
	{
		try
		{
			if(mDIStream != null)
			{
				mDIStream.close();
				mDIStream = null;
			}
			if(out != null)
			{
				out.close();
				out = null;
			}
			if(mClientHTTP != null)
			{
				mClientHTTP.close();
			}
		}
		catch(Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}


	public boolean canInterrupt = false;
	public boolean checkUpdate = false;
	public String deviceName;
#if USE_JP_HD_SUBSCRIPTION    
    public boolean checkJpHdSubcription = false;
#endif


	private boolean hasDownloadLimit()
	{
		DBG("GameInstaller", "hasDownloadLimit");
#if USE_GOOGLE_TV_BUILD
		return false;
#else


	#if !USE_SIMPLIFIED_INSTALLER
		return true;
	#else
		
		if( k_downloadLimit == -1 )//default, don't ask
			return false;

		if( k_downloadLimit == 0 )//if limit is 0, always ask
			return true;
		
		DBG("GameInstaller", "hasDownloadLimit real required size in MB >>:"+ (int)(m_iRealRequiredSize >> 20 ));
		if(m_iRealRequiredSize > 0)
		{
			int mb = (int)((m_iRealRequiredSize >> 20));
			DBG("GameInstaller", "hasDownloadLimit mb>"+k_downloadLimit+"?" + (mb >= k_downloadLimit));
			return (mb >= k_downloadLimit);
		}
		return false;
	#endif

#endif
	}

	private void setDataServerUrl()
	{
		DBG("GameInstaller", "Using Dynamic Place URL");
		SERVER_URL = getDataServerUrl(-1); //dummy param
		//SERVER_URL = "http://dl.gameloft.com/partners/androidmarket/d.cdn_test.php?model=GT-I9000&device=GT-I9000&product=1533&version=1.0.0&portal=gl_shop";
		//SERVER_URL = "http://dl.gameloft.com/partners/androidmarket/d.cdn_test.php?model=LG-SU660&device=star&product=302&version=1.6.4&portal=google_market";
		DBG("GameInstaller", "SERVER_URL: " + SERVER_URL);
	}

	private void sendHDIDFVtracker()
	{
	#if HDIDFV_UPDATE
		boolean HDIDFV_sent = SUtils.getPreferenceBoolean("HDIDFV_sent", false, mPreferencesName);
		
		if (!HDIDFV_sent)
		{		
			String hdidfv = Device.getHDIDFV();
			
			SUtils.setPreference("HDIDFV_sent", true, mPreferencesName);
							
	#if USE_GOOGLE_ANALYTICS_TRACKING
				GoogleAnalyticsTracker.trackEvent(
					GoogleAnalyticsConstants.Category.Configuration, 
					GoogleAnalyticsConstants.Action.HDIDFV,
					hdidfv, null);
	#endif
		
		}
	#endif
	}


#if USE_GOOGLE_TV_BUILD
	private int wifiHardwareResult = -1; //do not modify this variable in other than here
	private boolean isWifiHardwareAvailable()
	{
		if (wifiHardwareResult == -1)
		{
			if ( getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI) )
			{
				wifiHardwareResult = 1;
			} else 
			{
				wifiHardwareResult = 0;
			}
		}

		return wifiHardwareResult == 1;
	}
#endif

	public void run()
	{
		long enteringLoopTime;
		long loopSleep = 1000 / 20;
		
		DBG("GameInstaller","sbStarted: " + sbStarted);
		DBG("GameInstaller","mState != STATE_FINALIZE: " + (mState != GI_STATE_FINALIZE));

		Looper.prepare();
		
		mState = GI_STATE_CHECK_PIRACY;
		mSubstate = 0;
#if TMOBILE_DRM
		mTMobileSubstate = SUBSTATE_INIT_TMOBILE;
#endif
		mStatus = STATUS_NO_ERROR;
		launchGame = true;
		mbStartDownloaded = false;
		formatter = new DecimalFormat("#,##0.00");
		
		sendHDIDFVtracker();
		
		while (mState != GI_STATE_FINALIZE && !sbStarted)
		{
			canInterrupt = false;
			enteringLoopTime = System.currentTimeMillis();
			
			// Reduce the CPU usage while in the background
			if(bIsPaused)
			{
					if((mState != GI_STATE_DOWNLOAD_FILES && mState != GI_STATE_VERIFYING_CHECKSUM) 
					|| (mState == GI_STATE_DOWNLOAD_FILES && (mSubstate != SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE
							&& mSubstate != SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION
			))
			)
					{
						try{Thread.sleep(50);}catch(Exception e){}
						continue;
					}
					
					if(mState == GI_STATE_DOWNLOAD_FILES && mSubstate == SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE) 
					{
						try{Thread.sleep(100);}catch(Exception e){}
					}
				
			}
		#if ORANGE_DRM
		if(mState == GI_STATE_PIRACY_ERROR)
		{
			continue;
		}
		#endif
			update();
			if(mState == GI_STATE_DOWNLOAD_FILES && mSubstate == SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE) 
			{
				if (System.currentTimeMillis() - enteringLoopTime == 0)
					try{Thread.sleep(loopSleep);}catch(Exception e){}
				else
					try{Thread.sleep(loopSleep / (System.currentTimeMillis() - enteringLoopTime));}catch(Exception e){}
			}
			else
			{
				try{Thread.sleep(20);}catch(Exception e){}
			}
			canInterrupt = true;
		}	
		
		if(mStatus == STATUS_NO_ERROR && launchGame)
		{
		
		DBG("GameInstaller", "No errors, launching game");
		
	#if USE_GOOGLE_ANALYTICS_TRACKING
		if (isValidResourceVersion())
		{
			setSDFolder();
			int count = 0;
			for (Pair<String, Long> element : sdAndSizes)
			{
				GoogleAnalyticsTracker.trackEvent(
					GoogleAnalyticsConstants.Category.Configuration, 
					GoogleAnalyticsConstants.Action.SdCard + count,
					GoogleAnalyticsConstants.Label.AfterInstall, element.second);
				DBG("GoogleAnalyticsTracking", "After download size " + (count++) + ": " + element.first + " " +element.second);
			}
		}
	#endif
			
			
		#if VERIZON_DRM
			mVerizonLicense.addFreeCount();
		#endif
			
		File sofile = new File(GameInstaller.LIBS_PATH+"/libsampleSandBox.so");
		DBG("GameInstaller","size of the so: "+sofile.length());
		
			int nativeLibsLoaded = 0;
			try
			{
				if(sNativeLibs.size() > 0 ) {
					for(String lib : sNativeLibs)	{
						DBG("GameInstaller", "Loading library: " + lib);
						nativeLibsLoaded++;
						System.load(LIBS_PATH + lib);
					}
				}
			}
			catch (Exception e)
			{
				DBG_EXCEPTION(e);
			}
			if (nativeLibsLoaded == 0)
		
			{
				DBG("GameInstaller", "Loading it's own library: " + SO_LIB_FILE);
				System.loadLibrary(SO_LIB_FILE);
			}
		sofile = new File(GameInstaller.LIBS_PATH+"/libsampleSandBox.so");
		DBG("GameInstaller","size of the so: "+sofile.length());
			if(this.getApplicationContext().getPackageManager().checkPermission(Manifest.permission.CHANGE_WIFI_STATE,STR_APP_PACKAGE) == PackageManager.PERMISSION_GRANTED)
			{
				DBG("GameInstaller", "Reconfiguring wifi...");
				if (wasWifiActivatedByAPP)
					mWifiManager.setWifiEnabled(false);
				else if(wasWifiDeActivatedByAPP)
					mWifiManager.setWifiEnabled(true);
			}
			// Make sure .nomedia always in the data folder			
			createNoMedia(DATA_PATH);
#if USE_GAME_TRACKING
		Tracking.setFlag(Tracking.TRACKING_FLAG_GAME_ON_STOP | Tracking.TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST);
	#if USE_HEP_EXT_IGPINFO
		String igpInfo[] = APP_PACKAGE.IGPInfo.getIGPInfo();
		Tracking.onLaunchGame(Tracking.TYPE_LAUNCH_GAME, "&IGPcode=" + igpInfo[0]); //Producer Request
	#else
		Tracking.onLaunchGame(Tracking.TYPE_LAUNCH_GAME);
	#endif
#endif

			
			
#if GOOGLE_DRM
			this.runOnUiThread(new Runnable ()
			{
				public void run ()
				{
					try
					{
						setContentView(R.layout.gi_main);
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
			});
				
			getPublicKey(); 
			mKey = getKey();
			doStuff();
			return;
#else
			sbStarted = true;
			//Wait for the Installer get focus before start the game
			while(s_isPauseGame) { try{Thread.sleep(100);}catch(Exception e) {}  }
#endif		
		}

	
		
		DBG("GameInstaller","ending installer with status: " + mStatus);
		
		
		
		if(
		#if !SKIP_DATA_DOWNLOADER
			mStatus == STATUS_NO_ERROR && 
		#endif
			launchGame)
		{
			String sd_folder = SUtils.getPreferenceString("SDFolder", "", mPreferencesName);
			if(sd_folder.equals(""))
			{
				setPath(0);
				createNoMedia(sd_folder);
				SaveDateLastUpdate(sd_folder);				
			}
			finishSuccess();
		}
		else
			finishFail();

	}
	
	public boolean launchGame = true;
	public int mStillHasError = 0;
	public int wifiStep = 0;
	public int netLookupRetry = 0;
	public final int NET_RETRY_MAX = DEBUG?3:30;
	public boolean mbStartDownloaded = false;
	public boolean firstCheckFinished = false;
	public boolean firstTimeHere = true;
	
	private long getZipRealSpace(java.util.zip.ZipFile zipFile)
	{
		Enumeration enumeration = zipFile.entries();
		long total_size = 0;
		while (enumeration.hasMoreElements())
		{
			ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
			total_size += zipEntry.getSize();
		}
		
		return total_size;
	}
	private boolean checkUnzipSpace()
	{
			String sd_folder = SUtils.getPreferenceString("SDFolder", "", mPreferencesName);
			
			if(sd_folder.equals(""))
			{
				DBG("GameInstaller","sd foler null");
				long total_components_space = 0;
				for(DownloadComponent component : mDownloadComponents)
				{	
					try
					{
						String input_path;
						if(component.mServerFilename.startsWith("main") || component.mServerFilename.startsWith("patch"))
							input_path = marketPath;
						else
							input_path = DATA_PATH;
						//unZip(component.mServerFilename, marketPath, "", DATA_PATH, true);
						String final_file = input_path + "/" + component.mServerFilename;
						java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(final_file);
						total_components_space += getZipRealSpace(zipFile);
					}catch(Exception ex){}
				}
				if(total_components_space > 0)
					setPath(total_components_space / 1048576);
			}
			
			sd_folder = SUtils.getPreferenceString("SDFolder", "", mPreferencesName);
			
			File path;
			if (sd_folder.contains(STR_APP_PACKAGE))
				path = new File(sd_folder.substring(0, sd_folder.indexOf(STR_APP_PACKAGE)));
			else if (sd_folder == SD_FOLDER)
				path = new File("/sdcard/");
			else
				path = new File(sd_folder);
				
			if (!path.exists())
				path.mkdirs();
				
			DBG("GameInstaller","the path: "+path);
			StatFs stat = new StatFs(path.getAbsolutePath());
			long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
			mbAvailable = (int)(bytesAvailable / 1048576);
			
		long total_size = 0;
		long total_size_of_archive = 0;
		for(DownloadComponent component : mDownloadComponents)
		{	
		if(component.mServerFilename.equals(""))
			continue;
		try
		{
			String input_path;
			if(component.mServerFilename.startsWith("main") || component.mServerFilename.startsWith("patch"))
				input_path = marketPath;
			else
				input_path = DATA_PATH;
			//unZip(component.mServerFilename, marketPath, "", DATA_PATH, true);
			String final_file = input_path + "/" + component.mServerFilename;
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(final_file);
			Enumeration enumeration = zipFile.entries();
		
			while (enumeration.hasMoreElements())
			{
				mTotalSizeUnzipCheck ++;
				ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();	
				
				File file = new File(DATA_PATH+"/"+zipEntry.getName());
				
				if(!file.isDirectory())
				if(file.exists())
				{
					long size_required = zipEntry.getSize() - file.length();
					if(size_required>0)
						total_size += size_required;
				}
				else
					total_size += zipEntry.getSize();
					
				total_size_of_archive += zipEntry.getSize();
				
				DBG("GameInstaller", "zip size: "+total_size+" available: "+bytesAvailable+ " total size: " + total_size_of_archive);
			}
			}catch(IOException ex){DBG_EXCEPTION(ex);DBG("GameInstaller", "Exception cheking installing space");}
			
		}
		if(total_size >= bytesAvailable)
		{
			mTotalSize = total_size_of_archive;
			DBG("GameInstaller", "smaller");
			mbRequired = (int)(total_size / 1048576);
			return false;
		}
		
		mTotalSize = total_size_of_archive;
		
		return true;
	}
	
	public void update()
	{
		
		if (mSubstate == SUBSTATE_SHOW_LAYOUT)
		{
			if(mState == GI_STATE_DOWNLOAD_FILES)
			{
			
		#if !USE_SIMPLIFIED_INSTALLER
				if (mPrevState == GI_STATE_DOWNLOAD_FILES_QUESTION || mPrevState == GI_STATE_CONFIRM_3G 
			#if USE_MARKET_INSTALLER || GOOGLE_MARKET_DOWNLOAD
					|| mPrevState == GI_STATE_DEVICE_NOT_SUPPORTED
			#endif
				)
		#else
				if (mPrevState == GI_STATE_DOWNLOAD_FILES_QUESTION || mPrevState == GI_STATE_VERIFYING_CHECKSUM || mPrevState == SUBSTATE_DOWNLOAD_CHECK_SPACE)
		#endif
				{
					mNumberOfNeededFiles = mRequiredResources.size();				
					mSubstate = SUBSTATE_DOWNLOAD_MANAGER_INIT;
				}
				else
				{
					DBG("GameInstaller", "substate at the begining before = "+mSubstate);
					if(mSubstate != SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION)
						mSubstate = SUBSTATE_INIT;
						
					DBG("GameInstaller", "substate at the begining = "+mSubstate);
				}
			}
			else
			{
				DBG("GameInstaller", "SUBSTATE_INIT2");
				if(mSubstate != SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION)
				mSubstate = SUBSTATE_INIT;
				try { Thread.sleep(50); } catch(Exception e){};
			}
			return;
		}
		switch(mState)
		{
			case GI_STATE_CHECK_PIRACY:
			{
				if (!HAS_PIRACY_TEST)
				{
					INFO("DRM", "Doesn't have piracy test!");
					setState(GI_STATE_CHECK_ERRORS);
				}
				else if (firstTimeHere)
				{
					firstTimeHere = false;
					DBG("DRM", "Checking piracy test...");
					updateCheckPiracy();
				}
			}
			break;
			case GI_STATE_PIRACY_ERROR:
			{
				// Listener Added at setLayout
			}
			break;
			#if USE_SIMPLIFIED_INSTALLER
			case GI_STATE_DOWNLOAD_FILES_QUESTION:
				/*if(currentNetUsed != ConnectivityManager.TYPE_MOBILE)
				{
					currentNetUsed = ConnectivityManager.TYPE_WIFI;
					if(!isWifiAlive())
					{
						ERR_TOAST(ToastMessages.GameInstallerNoWifi.DownloadFilesQuestionYes);
						//setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
						return;
					}
				}
				}
				else
				{
					//Wifi enable after checking using 3G, should show error message
					if(isWifiAlive())
					{
						currentNetUsed = ConnectivityManager.TYPE_WIFI;
						ERR_TOAST(ToastMessages.GameInstallerSingleFileDownload.WifiEnabledWhenUsing3G);
						setState(GI_STATE_DOWNLOAD_FILES_ERROR);
						return;
					}
				}*/
				
		#if USE_GOOGLE_TV_BUILD
			// ethernet is preffered by GTV
				if (isEthernetAlive())
					currentNetUsed = ConnectivityManager.TYPE_MOBILE;
				else
					currentNetUsed = ConnectivityManager.TYPE_WIFI;
		#else
				if (isWifiAlive())
						currentNetUsed = ConnectivityManager.TYPE_WIFI;
				else
					currentNetUsed = ConnectivityManager.TYPE_MOBILE;
		#endif

			#if USE_TRACKING_FEATURE_INSTALLER
				if(!mbStartDownloaded)
				{
					Tracker.downloadStartTracker(WIFI_MODE, currentNetUsed == ConnectivityManager.TYPE_MOBILE);
				#if USE_GOOGLE_ANALYTICS_TRACKING
					GoogleAnalyticsTracker.trackEvent(
						//GoogleAnalyticsConstants.Category.Installer, 
						GoogleAnalyticsConstants.Category.StartDownload, 
						WIFI_MODE == WIFI_ONLY ? GoogleAnalyticsConstants.Action.WifiOnly : GoogleAnalyticsConstants.Action.Wifi3G,
						currentNetUsed == ConnectivityManager.TYPE_MOBILE ? GoogleAnalyticsConstants.Label.Through3G : GoogleAnalyticsConstants.Label.ThroughWifi, null);
				#endif
					mbStartDownloaded = true;
				}
			#endif
				// Create .nomedia and data folder
				createNoMedia(DATA_PATH);
				
				mStatus = STATUS_NO_ERROR;
				setState(GI_STATE_DOWNLOAD_FILES);
				
				#if USE_INSTALLER_SLIDESHOW && USE_INSTALLER_SLIDESHOW_TEXTS
				goToLayout(R.layout.data_downloader_linear_progressbar_layout_v2, LAYOUT_DOWNLOAD_FILES);
				#else
				goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_DOWNLOAD_FILES);
				#endif
				break;
			#endif
			case GI_STATE_CHECK_ERRORS:
			{
				if (!firstCheckFinished)
				{
					firstCheckFinished = true;
					
					if(SERVER_URL.equals(""))
						setDataServerUrl();
					
					initDownloadComponents(SERVER_URL);
					
					
					resetErrorPresent();
					
					String data = SUtils.getOverriddenSetting(DATA_PATH + "qaTestingConfigs.txt", "SKIP_VALIDATION");
					#if GOOGLE_MARKET_DOWNLOAD
					if(data == null)
					{
						data = SUtils.getOverriddenSetting(marketPath + "/qaTestingConfigs.txt", "SKIP_VALIDATION");
					}
					#endif
				#if ACP_UT	
					if(true)
				#else
					if(data != null && data.equals("1"))
				#endif
					{
						DBG("GameInstaller", "Skiping data validation");
					#if GOOGLE_MARKET_DOWNLOAD && UNZIP_DOWNLOADED_FILES
						//check and replace obb and patch names 
						String mainFile = SUtils.getOverriddenSetting(DATA_PATH + "qaTestingConfigs.txt", "MAIN_FILE_NAME");
						String patchFile = SUtils.getOverriddenSetting(DATA_PATH + "qaTestingConfigs.txt", "PATCH_FILE_NAME");
						if(mainFile == null && patchFile == null)
						{
							mainFile = SUtils.getOverriddenSetting(marketPath + "/qaTestingConfigs.txt", "MAIN_FILE_NAME");
							patchFile = SUtils.getOverriddenSetting(marketPath + "/qaTestingConfigs.txt", "PATCH_FILE_NAME");
						}
						DBG("GameInstaller","main: "+mainFile+" patch: "+patchFile);
						if(mainFile!=null && !mainFile.equals(""))
							mDownloadComponents.add(new DownloadComponent("", mainFile, GAME_VERSION_NAME, mainFile, 0, true));
						if(patchFile!=null && !patchFile.equals(""))
							mDownloadComponents.add(new DownloadComponent("", patchFile, GAME_VERSION_NAME, patchFile, 0, true));
						
						if(checkUnzipSpace())
						{
							setState(GI_STATE_UNZIP_DOWNLOADED_FILES);
						}
						else
					#endif
						{
							setState(GI_STATE_FINALIZE);
							mStatus = STATUS_NO_ERROR;
							checkUpdate = true;
							launchGame = true;
						}
						return;
					}
					
					readInstallerResource();
					pushHashMapResource();
					
				#if USE_MARKET_INSTALLER
					setErrorPresent(ERROR_NO_NATIVE, 0); //test to see if we downloaded a version before
				#endif
					
					currentDownloadFile = 0;
					
					DBG("GameInstaller","Checking to see if we go to download state... ");
				
					if(getInfoFilesLength() <= 0)
					{
					#if GOOGLE_DRM && !GOOGLE_MARKET_DOWNLOAD
						GoogleDrmCheck();
					#elif GLOFT_DRM
						GloftDrmCheck();
					#else
						DBG("GameInstaller","info file length empty. Go to logo");
						setState(GI_STATE_GAMELOFT_LOGO);
					#endif
					}
					else
					{
						DBG("GameInstaller","Let's see if the installer should get serious!");
						setErrorPresent(ERROR_NO_SD, hasSDCard()); //test for SD CARD
						setErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST, isValidResourceVersion()?1:0);
						setErrorPresent(ERROR_FILES_NOT_VALID, validateFiles()); //test for files needed
						if(getErrorPresent(ERROR_FILES_NOT_VALID))
							setErrorPresent(ERROR_NO_ENOUGH_SPACE, checkSDAvailable()); //test for available space
					
						if(getErrorPresent(ERROR_NO_SD)
							|| getErrorPresent(ERROR_NO_ENOUGH_SPACE)
							|| getErrorPresent(ERROR_FILES_NOT_VALID)
							|| getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
						{
						#if GOOGLE_DRM && !GOOGLE_MARKET_DOWNLOAD
							GoogleDrmCheck();
						#elif GLOFT_DRM
							GloftDrmCheck();
						#else
							DBG("GameInstaller", "Well it will get serious. Starting the logo.");
							setState(GI_STATE_GAMELOFT_LOGO);
						#endif
						}
						else
						{
						#if GOOGLE_MARKET_DOWNLOAD && UNZIP_DOWNLOADED_FILES
							if(checkUnzipSpace())
								setState(GI_STATE_UNZIP_DOWNLOADED_FILES);
							else
								setState(GI_STATE_SOLVE_ERROR);
						#else
							DBG("GameInstaller", "Everything is fine");
							setState(GI_STATE_FINALIZE); //Everything is fine
						#endif
						}
					}
				}
			}
			break;
			#if AUTO_UPDATE_HEP
			case GI_STATE_UPDATE_FINISH:
			{
			DBG("GameInstaller","GI_STATE_UPDATE_FINISH");
			#if !GOOGLE_MARKET_DOWNLOAD
				if(getInfoFilesLength() <= 0)
				{
				//#if !USE_SIMPLIFIED_INSTALLER
					if(!isWifiAlive())
					{
						ERR_TOAST(ToastMessages.GameInstallerNoWifi.HEPUpdateFinishPackFileNoWifi);

						if(hasDownloadLimit())
						setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
						else
						setState(GI_STATE_DOWNLOAD_FILES);
					}
					else if(isWifiAlive() && !canReach(SERVER_URL))
					{
						ERR_TOAST(ToastMessages.GameInstallerCantReachServer.HEPUpdateFinishPackFileCantReachServer);
						setState(GI_STATE_DOWNLOAD_FILES_ERROR);
					}
					else
				//#endif
						setState(GI_STATE_DOWNLOAD_FILES);
				}
				else 
				#endif
				if (getErrorPresent(ERROR_NO_SD) || getErrorPresent(ERROR_NO_ENOUGH_SPACE))
				{
					setState(GI_STATE_SOLVE_ERROR);
				}
				else if(getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
				{
					setState(GI_STATE_VERIFYING_CHECKSUM);
				}
				else
				{
					// Force update required files in server
				//#if !USE_SIMPLIFIED_INSTALLER
					if(!isWifiAlive())
					{
						ERR_TOAST(ToastMessages.GameInstallerNoWifi.HEPUpdateFinishNoWifi);
						if(hasDownloadLimit())
							setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
						else
							setState(GI_STATE_DOWNLOAD_FILES);
					}
					else if(isWifiAlive() && !canReach(SERVER_URL))
					{
						ERR_TOAST(ToastMessages.GameInstallerCantReachServer.HEPUpdateFinishCantReachServer);
						setState(GI_STATE_DOWNLOAD_FILES_ERROR);
					}
					else
				//#endif
						setState(GI_STATE_DOWNLOAD_FILES);
				}
			}
			break;
			#endif
			case GI_STATE_GAMELOFT_LOGO:
			{	
				if (System.currentTimeMillis() - mLogoTimeElapsed > GL_LOGO_DELAY)
				{
					DBG("GameInstaller","GI_STATE_GAMELOFT_LOGO");
					s_files_changed = true;
				#if AUTO_UPDATE_HEP
					//the code below need to be in state GI_STATE_UPDATE_FINISH, to continue after checking update
					setState(GI_STATE_INIT_UPDATE_VERSION);
				#else
				#if !GOOGLE_MARKET_DOWNLOAD
					//if(mPackFileInfo == null || mPackFileInfo.size() <= 0)
					if(getInfoFilesLength() <= 0)
					{
					//#if !USE_SIMPLIFIED_INSTALLER
						if(!isWifiAlive())
						{
							ERR_TOAST(ToastMessages.GameInstallerNoWifi.GLoftLogoPackFileNoWifi);
							if(hasDownloadLimit())
								setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
							else
								setState(GI_STATE_DOWNLOAD_FILES);
						}
						else if(isWifiAlive() && !canReach(SERVER_URL))
						{
							DBG("GameInstaller","No res.Cannot reach server!");
							ERR_TOAST(ToastMessages.GameInstallerCantReachServer.GLoftLogoPackFileCantReachServer);
							setState(GI_STATE_DOWNLOAD_FILES_ERROR);
						}
						else
					//#endif
						{
							DBG("GameInstaller","GI_STATE_DOWNLOAD_FILES");
							setState(GI_STATE_DOWNLOAD_FILES);
						}
					}
					else 
				#endif
					if (getErrorPresent(ERROR_NO_SD) || getErrorPresent(ERROR_NO_ENOUGH_SPACE))
					{
						setState(GI_STATE_SOLVE_ERROR);
					}
					else if(getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
					{
						setState(GI_STATE_VERIFYING_CHECKSUM);
					}
					else
					{
					//#if !USE_SIMPLIFIED_INSTALLER
						// Force update required files in server
						if(!isWifiAlive())
						{
							ERR_TOAST(ToastMessages.GameInstallerNoWifi.GLoftLogoNoWifi);
							if(hasDownloadLimit())
								setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
							else
								setState(GI_STATE_DOWNLOAD_FILES);
						}
						else if(isWifiAlive() && !canReach(SERVER_URL))
						{
							DBG("GameInstaller","Cannot reach server!");
							ERR_TOAST(ToastMessages.GameInstallerCantReachServer.GLoftLogoCantReachServer);
							setState(GI_STATE_DOWNLOAD_FILES_ERROR);
						}
						else
					//#endif
						{
						DBG("GameInstaller","GI_STATE_DOWNLOAD_FILES with gef");
							setState(GI_STATE_DOWNLOAD_FILES);
						}
					}
				#endif
				}
			}
			break;
			case GI_STATE_VERIFYING_CHECKSUM:
			{
				DBG("GameInstaller", "GI_STATE_VERIFYING_CHECKSUM:");
				mRequestAll = true;
				//#if GOOGLE_MARKET_DOWNLOAD
				if (getRequiredResourcesValues())
				{
					DBG("GameInstaller", "readInstallerResources");
					readInstallerResource();
				}
				//#endif
				int res = validateFiles();
				if (getRequiredResourcesSize() > 0)
				{
					DBG("GameInstaller", "Required " + mRequiredResources.size()+ " resources.");
					mRequestAll = false; // for retry downloading state
					
					//saveVersion();

					setErrorPresent(ERROR_NO_SD, hasSDCard()); //test for SD CARD
					setErrorPresent(ERROR_FILES_NOT_VALID, res);
					setErrorPresent(ERROR_NO_ENOUGH_SPACE, checkSDAvailable()); 
					
					if(getErrorPresent(ERROR_NO_SD) || getErrorPresent(ERROR_NO_ENOUGH_SPACE))
					{
						DBG("GameInstaller", "(getErrorPresent(ERROR_NO_SD) || getErrorPresent(ERROR_NO_ENOUGH_SPACE))");
						setState(GI_STATE_SOLVE_ERROR);
					}
				#if USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
					else if (mIsGenericBuild && getErrorPresent(ERROR_NO_NATIVE))
					{
						DBG("GameInstaller", "(mIsGenericBuild && getErrorPresent(ERROR_NO_NATIVE)) verify checksum");
						ERR_TOAST(ToastMessages.MarkerInstallerChecks.VerifyingCheckSum);
						setState(GI_STATE_DEVICE_NOT_SUPPORTED);
					}
				#endif
					else
					{
						DBG("GameInstaller", "(mIsGenericBuild && getErrorPresent(ERROR_NO_NATIVE)) -> else");

						#if USE_SIMPLIFIED_INSTALLER
						if(!isWifiAlive() && hasDownloadLimit() && !hasUserConfirmedDataUsage)
						{
							setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
						} 
						else 
						{
							setState(GI_STATE_DOWNLOAD_FILES_QUESTION);
						}
						#else
						setState(GI_STATE_DOWNLOAD_FILES_QUESTION);//ghio
						#endif
					}
				}
				else
				{
					DBG("GameInstaller", "No required resources found.Going to finalize/unzipping state");
					saveVersion();
				#if GOOGLE_MARKET_DOWNLOAD && UNZIP_DOWNLOADED_FILES
					if(checkUnzipSpace())
						setState(GI_STATE_UNZIP_DOWNLOADED_FILES);
					else
						setState(GI_STATE_SOLVE_ERROR);
				#else
					setState(GI_STATE_FINALIZE);
				#endif
				}
			}
			break;
			case GI_STATE_SOLVE_ERROR:
			{
				// Listener Added at setLayout
			}
			break;
		//#if !USE_SIMPLIFIED_INSTALLER
			case GI_STATE_FIND_WIFI:
				if( (WIFI_MODE == WIFI_3G || WIFI_MODE == WIFI_3G_ORANGE_IL) && this.getApplicationContext().getPackageManager().checkPermission(Manifest.permission.CHANGE_WIFI_STATE,STR_APP_PACKAGE) == PackageManager.PERMISSION_GRANTED)
				{
					int res = activateWifi();

					if(m_bDownloadBackground)
					{
						if(res > 0)
						{
							DBG("GameInstaller","internet found start to download");
							removeWifiListener();
						}
						else if(res <0)
						{
							DBG("GameInstaller","no internet found");
							setState(GI_STATE_WAITING_FOR_WIFI);
						}
					}
					else
					{
						if(res <0)
						{
							if (mPrevState == GI_STATE_CONFIRM_WAITING_FOR_WIFI)
								setState(GI_STATE_WAITING_FOR_WIFI);
							else
								setState(GI_STATE_CONFIRM_WAITING_FOR_WIFI);
						}
					}
				}
				else
				{
					if(isWifiSettingsOpen && isWifiAlive())
					{
						setState(GI_STATE_DOWNLOAD_FILES);
						isWifiSettingsOpen = false;
					}
				}
			break;
			case GI_STATE_WAITING_FOR_WIFI:
			{
				if (mhasFocus && System.currentTimeMillis() - mWaitingForWifiTimeElapsed > WAITING_FOR_WIFI_IN_BACKGROUND_DELAY)
				{
					this.moveTaskToBack(true);
				}
			}
			break;
			case GI_STATE_CONFIRM_3G:
			{
				//Listener Added at setLayout
			}
			break;
			case GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION:
			{
				if (mSubstate == SUBSTATE_INIT)
				{
				#if USE_TRACKING_FEATURE_INSTALLER
					//Tracker.launchInstallerTracker(WIFI_MODE, false);//george removed this as it sends once in logo state
					if (WIFI_MODE == WIFI_3G || WIFI_MODE == WIFI_3G_ORANGE_IL)
					{
						setButtonVisibility(R.id.data_downloader_no, true);
						setBarVisibility(R.id.data_downloader_progress_bar, false);
					}
				#endif
					mSubstate = SUBSTATE_PROCESS;
				}
			}
			break;
		//#endif
			case GI_STATE_DOWNLOAD_FILES:
			{
				if (mSubstate == SUBSTATE_INIT)
				{
					mCurrentProgress = 0;
					mCurrentSize = 0;
					mSubstate = SUBSTATE_DOWNLOAD_CREATE_CLIENT;
				}
				else
				{
					updateDownloadFiles();
					#if USE_INSTALLER_SLIDESHOW
					float currentDownload, totalDownload;
					currentDownload = (float)(((mDownloadedSize / 1024.0) + mCurrentProgress) / 1024.0);
					totalDownload = (float)(mTotalSize / 1048576.0);
					if (currentDownload > totalDownload)
						currentDownload = totalDownload;
					
					int indexToDisplay = 0;
					
					if(totalDownload != 0)
						indexToDisplay = (int)((currentDownload / totalDownload) * slideShowImageIds.size());
					if(indexToDisplay >= slideShowImageIds.size())
						indexToDisplay = slideShowImageIds.size() - 1;

					if(indexToDisplay != lastDisplayedIndex)
					{
						lastDisplayedIndex = indexToDisplay;
						this.runOnUiThread(new Runnable ()
						{
							public void run ()
							{
								slideShowContainer = (ImageView)findViewById(R.id.data_downloader_main_layout_image);
								
								#if USE_INSTALLER_SLIDESHOW_TEXTS
								slideShowText = (TextView)findViewById(R.id.data_downloader_main_layout_text);
								if(slideShowText != null)
									slideShowText.setTextColor(slideShowTextsColor);
								#endif
								
								if(slideShowContainer != null)
								{
									slideShowContainer.setVisibility(View.VISIBLE);
									changeRandomBackground(lastDisplayedIndex);
								}
							}
						});
					}
					#endif
				}
			}
			break;
			case GI_STATE_DOWNLOAD_FILES_SUCCESFUL:
			{
				if (mSubstate == SUBSTATE_INIT)
				{
					#if USE_TRACKING_FEATURE_INSTALLER
					if(mbStartDownloaded)
					{
						Tracker.downloadFinishTracker(WIFI_MODE, currentNetUsed == ConnectivityManager.TYPE_MOBILE);
					#if USE_GOOGLE_ANALYTICS_TRACKING
						GoogleAnalyticsTracker.trackEvent(
							//GoogleAnalyticsConstants.Category.Installer, 
							GoogleAnalyticsConstants.Category.FinishDownload, 
							WIFI_MODE == WIFI_ONLY ? GoogleAnalyticsConstants.Action.WifiOnly : GoogleAnalyticsConstants.Action.Wifi3G,
							currentNetUsed == ConnectivityManager.TYPE_MOBILE ? GoogleAnalyticsConstants.Label.Through3G : GoogleAnalyticsConstants.Label.ThroughWifi, null);
					#endif
						#if !USE_SIMPLIFIED_INSTALLER && !UNZIP_DOWNLOADED_FILES
						setButtonVisibility(R.id.data_downloader_yes, true);
						setBarVisibility(R.id.data_downloader_progress_bar, false);
						#endif
						mbStartDownloaded = false;
						releaseWifiLock();
					}
					#endif
					mSubstate = SUBSTATE_PROCESS;
				}
				else if (mSubstate == SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION)
				{
					#if USE_TRACKING_FEATURE_INSTALLER
					if(mbStartDownloaded)
					{
						Tracker.downloadFinishTracker(WIFI_MODE, currentNetUsed == ConnectivityManager.TYPE_MOBILE);
					#if USE_GOOGLE_ANALYTICS_TRACKING
						GoogleAnalyticsTracker.trackEvent(
							//GoogleAnalyticsConstants.Category.Installer, 
							GoogleAnalyticsConstants.Category.FinishDownload, 
							WIFI_MODE == WIFI_ONLY ? GoogleAnalyticsConstants.Action.WifiOnly : GoogleAnalyticsConstants.Action.Wifi3G,
							currentNetUsed == ConnectivityManager.TYPE_MOBILE ? GoogleAnalyticsConstants.Label.Through3G : GoogleAnalyticsConstants.Label.ThroughWifi, null);
					#endif
						mbStartDownloaded = false;
					}
					#endif
				}
				#if GOOGLE_MARKET_DOWNLOAD && UNZIP_DOWNLOADED_FILES
					if(checkUnzipSpace())
						setState(GI_STATE_UNZIP_DOWNLOADED_FILES);
					else
						setState(GI_STATE_SOLVE_ERROR);
				#else
				#if USE_SIMPLIFIED_INSTALLER
					launchGame = true;
					mStatus = STATUS_NO_ERROR;
					setState(GI_STATE_FINALIZE);
				#endif
				#endif
			}
			break;
			case GI_STATE_UNZIP_DOWNLOADED_FILES:
			{
				boolean hasBeenModified = false;
				DBG("GameInstaller", "GI_STATE_UNZIP_DOWNLOADED_FILES");
				mCurrentProgress = 0;
				String extrafilename = SUtils.getPreferenceString("ExtraFile", "", mPreferencesName);
				String mainName = SUtils.getPreferenceString("mainFileName", "", "ExpansionPrefs");
				String patchName = SUtils.getPreferenceString("patchFileName", "", "ExpansionPrefs");
				boolean overwrite = SUtils.getPreferenceBoolean("ZipHasCRCtest", false, mPreferencesName);
				boolean result = true;
				
				String zip_files[] = new String[3];
				for(DownloadComponent component : mDownloadComponents)
				{
					if(component.mServerFilename.startsWith("main"))
					{
						zip_files[0] = component.mServerFilename;
						DBG("GameInstaller", zip_files[0]);
					}else
					if(component.mServerFilename.startsWith("patch"))
					{
						zip_files[1] = component.mServerFilename;
						DBG("GameInstaller", zip_files[1]);
					}
					else
					{
						zip_files[2] = component.mServerFilename;
						DBG("GameInstaller", zip_files[2]);
					}
				}
				
				
				if (overwrite)
					goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_VERIFYING_FILES);
					
					result &= unZipCheck(zip_files, marketPath, "", DATA_PATH, overwrite);
					
					/*for(DownloadComponent component : mDownloadComponents)
					{
						if(component.mServerFilename.startsWith("main"))
						{
							result &= unZipCheck(component.mServerFilename, marketPath, "", DATA_PATH, overwrite);
						}
					}
					
					for(DownloadComponent component : mDownloadComponents)
					{
						if(component.mServerFilename.startsWith("patch"))
						{	
							result &= unZipCheck(component.mServerFilename, marketPath, "", DATA_PATH, overwrite);
						}
					}
					
					for(DownloadComponent component : mDownloadComponents)
					{
						if(!component.mServerFilename.startsWith("main") && !component.mServerFilename.startsWith("patch"))
						{
							result &= unZipCheck(component.mServerFilename, DATA_PATH, "", DATA_PATH, overwrite);
						}
					}*/
					

				
				DBG("GameInstaller", "result of unzip check: "+result);
				if(!result)
				{
					mCurrentProgress = 0;
					
					goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_UNZIP_FILES);
					
					for(DownloadComponent component : mDownloadComponents)
					{
						if(!component.mServerFilename.startsWith("main") && !component.mServerFilename.startsWith("patch"))
						{
							result = unZip(component.mServerFilename, DATA_PATH, "", DATA_PATH, false);
							hasBeenModified = hasBeenModified || result;
						}
					}
					
					for(DownloadComponent component : mDownloadComponents)
					{
						if(component.mServerFilename.startsWith("patch"))
						{	
							result = unZip(component.mServerFilename, marketPath, "", DATA_PATH, false);
							hasBeenModified = hasBeenModified || result;
						}
					}
					
					for(DownloadComponent component : mDownloadComponents)
					{
						if(component.mServerFilename.startsWith("main"))
						{
							result = unZip(component.mServerFilename, marketPath, "", DATA_PATH, false);
							hasBeenModified = hasBeenModified || result;
						}
					}
					
					
				}
					
				/*
				if(isUnzipingInterrupted)
				{
					goToLayout(LAYOUT_UNZIP_FILES_CANCEL_QUESTION);
				}
				else*/
				if (!isUnzipingInterrupted)
				{
				//#if !USE_SIMPLIFIED_INSTALLER
				//if(hasBeenModified)
				//	setState(GI_STATE_DOWNLOAD_FILES_SUCCESFUL);
				//else
				//	setState(GI_STATE_FINALIZE);
				//#else
					DBG("GameInstaller", "unzip checking done");
					SUtils.setPreference("ZipHasCRCtest", false, mPreferencesName);
					launchGame = true;
					mStatus = STATUS_NO_ERROR;
					setState(GI_STATE_FINALIZE);
				//#endif
				}
			}
			break;
			case GI_STATE_INIT_UPDATE_VERSION:
			case GI_STATE_SEND_REQUEST:
			case GI_STATE_WAIT_REPONSE:
			case GI_STATE_CONFIRM_UPDATE_VERSION:
			case GI_STATE_RETRY_UPDATE_VERSION:
			case GI_STATE_NO_NEW_VERSION:
			case GI_STATE_UPDATE_GET_NEW_VERSION:
				updateNewVersionStates();
			default:
			break;
		}
	}
#if GOOGLE_MARKET_DOWNLOAD
	private static void deleteExtraFile(int version)
	{
		String extraFileName = SUtils.getPreferenceString("ExtraFile", "", mPreferencesName);
		if (extraFileName.equals(""))
			return;
		try
		{
			File file = new File(sd_folder + "/" + extraFileName);
			if (file.exists())
			{
				file.delete();
				SUtils.setPreference("ExtraFile", "", mPreferencesName);
				SUtils.setPreference("ExtraFileSize", 0, mPreferencesName);
			}
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
#endif
	private void updateDownloadFiles()
	{
		//DBG("GameInstaller", "updateDownloadFiles substate: "+mSubstate);
		switch(mSubstate)
		{
			case SUBSTATE_DOWNLOAD_CREATE_CLIENT:
			{
				acquireWifiLock();
				
				mClientHTTP		= new HttpClient();
				mStatus			= STATUS_NO_ERROR;
				
				mSubstate = SUBSTATE_CHECK_LATEST_VERSION;
				DBG("GameInstaller","mSubstate = SUBSTATE_CHECK_LATEST_VERSION");
			
				goToLayout(R.layout.data_downloader_progressbar_layout, LAYOUT_CHECKING_REQUIRED_FILES);
				
			} break;
//#if USE_MARKET_INSTALLER || USE_DYNAMIC_DOWNLOAD_LINK || GOOGLE_MARKET_DOWNLOAD
			case SUBSTATE_CHECK_LATEST_VERSION:
			{
				DBG("GameInstaller", "SUBSTATE_CHECK_LATEST_VERSION");
				if (SERVER_URL.equals(""))
				{
					setDataServerUrl();
				}
				if(mIsOverwrittenLink)
				{
				#if USE_MARKET_INSTALLER
					setErrorPresent(ERROR_NO_NATIVE, 0);
				#endif
					mCurrentVersion = 1;
					mIsGenericBuild = false;
					
					for(DownloadComponent component : mDownloadComponents)
					{
						component.UpdateVersion();
						SUtils.setPreference("CurrentVersion"+component.getName(), mCurrentVersion, mPreferencesName);
						SUtils.setPreference("IsGenericBuild"+component.getName(), mIsGenericBuild, mPreferencesName);
					}
						
					mSubstate = SUBSTATE_CHECK_MASTER_FILE;
				}
				else 
				{
					for(DownloadComponent component : mDownloadComponents)
					{
						if(component.requestForNewVersion())
						{
							component.handleRequestForNewVersion();
							
							if(component.getVersionInt() != SUtils.getPreferenceInt("CurrentVersion"+component.getName(), 0, mPreferencesName))
							{
								component.UpdateVersion();
								SUtils.setPreference("CurrentVersion"+component.getName(), component.getVersionInt(), mPreferencesName);
								SUtils.setPreference("IsGenericBuild"+component.getName(), component.isGenericBuild(), mPreferencesName);
							}
						}
					}
					#if USE_MARKET_INSTALLER
						setErrorPresent(ERROR_NO_NATIVE, 0);
					#endif
						mSubstate = SUBSTATE_CHECK_MASTER_FILE;
				}
				
			} break;
//#endif
			case SUBSTATE_CHECK_MASTER_FILE:
			{
				DBG("GameInstaller", "SUBSTATE_CHECK_MASTER_FILE");
				if(getDataStream(SERVER_URL))
				{
					if (getRequiredResourcesValues())
					{
						readInstallerResource();
						mSubstate = SUBSTATE_DOWNLOAD_CHECK_SPACE;
					}
					else
					{
					#if USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
						if (mIsGenericBuild && getErrorPresent(ERROR_NO_NATIVE))
						{
							ERR_TOAST(ToastMessages.MarkerInstallerChecks.ResourceValuesError);
							setState(GI_STATE_DEVICE_NOT_SUPPORTED);
						}
						else
					#endif
						{
							ERR_TOAST(ToastMessages.GameInstallerChecks.ResourceValuesError);
							setState(GI_STATE_DOWNLOAD_FILES_ERROR);
						}
					}
				}
				else
				{
					ERR_TOAST(ToastMessages.GameInstallerChecks.DataStreamError);
					setState(GI_STATE_DOWNLOAD_FILES_ERROR);
					if(mClientHTTP != null)
					{
						mClientHTTP.close();
						mClientHTTP = null;
					}
				}
			} break;
		
			case SUBSTATE_DOWNLOAD_CHECK_SPACE:
			{
				currentDownloadFile = 0;
				setErrorPresent(ERROR_NO_SD,hasSDCard()); //test for SD CARD
				setErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST, isValidResourceVersion()?1:0);
				setErrorPresent(ERROR_FILES_NOT_VALID, validateFiles()); //test for files needed
				if(getErrorPresent(ERROR_FILES_NOT_VALID))
					setErrorPresent(ERROR_NO_ENOUGH_SPACE, checkSDAvailable()); //test for available space
				
			#if USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
				/*if (mIsGenericBuild && getErrorPresent(ERROR_NO_NATIVE))
				{
					DBG("GameInstaller", "mIsGenericBuild && getErrorPresent(ERROR_NO_NATIVE) substate download check space");
					ERR_TOAST(ToastMessages.MarkerInstallerChecks.CheckSpace);
					setState(GI_STATE_DEVICE_NOT_SUPPORTED);
				}
				else*///george
			#else
			#if !GOOGLE_MARKET_DOWNLOAD
				//if(mPackFileInfo == null || mPackFileInfo.size() <= 0)
				if(getInfoFilesLength() <= 0)
				{
					ERR_TOAST(ToastMessages.GameInstallerChecks.PackInfoNull);
					DBG("GameInstaller", "mPackFileInfo == null || mPackFileInfo.size() <= 0");
					setState(GI_STATE_DOWNLOAD_FILES_ERROR);
				}
				else 
			#endif
			#endif
				if(getErrorPresent(ERROR_NO_SD) || getErrorPresent(ERROR_NO_ENOUGH_SPACE))
				{
					DBG("GameInstaller", "getErrorPresent(ERROR_NO_SD) || getErrorPresent(ERROR_NO_ENOUGH_SPACE)");
					setState(GI_STATE_SOLVE_ERROR);
				}
				else if(getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
				{
					DBG("GameInstaller", "getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST)");
					setState(GI_STATE_VERIFYING_CHECKSUM);
				}
				else if (getErrorPresent(ERROR_FILES_NOT_VALID))
				{
					if(getRequiredResourcesSize() <= 0)
					{
						ERR_TOAST(ToastMessages.GameInstallerChecks.FilesNotValid);
						DBG("GameInstaller", "getErrorPresent(ERROR_FILES_NOT_VALID) -> mRequiredResources.size() <= 0");
						setState(GI_STATE_DOWNLOAD_FILES_ERROR);
					}
					else
					{
						DBG("GameInstaller", "getErrorPresent(ERROR_FILES_NOT_VALID) -> else");
						
						#if USE_SIMPLIFIED_INSTALLER
						if(!isWifiAlive() && hasDownloadLimit() && !hasUserConfirmedDataUsage)
						{
							setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
						} 
						else 
						#endif
						{
							setState(GI_STATE_DOWNLOAD_FILES_QUESTION);
						}
					}
				}
				else
				{
					DBG("GameInstaller", "getErrorPresent(ERROR_NO_SD) || getErrorPresent(ERROR_NO_ENOUGH_SPACE) -> else");
					mSubstate = SUBSTATE_DOWNLOAD_LAUNCH;
				}
			} break;
		
			case SUBSTATE_DOWNLOAD_LAUNCH:
				DBG("GameInstaller", "SUBSTATE_DOWNLOAD_LAUNCH");
				for(DownloadComponent component : mDownloadComponents)
				{
					if(component.getVersionInt() != SUtils.getPreferenceInt("CurrentVersion"+component.getName(), 0, mPreferencesName))
					{
						component.UpdateVersion();
						SUtils.setPreference("CurrentVersion"+component.getName(), component.getVersionInt(), mPreferencesName);
						SUtils.setPreference("IsGenericBuild"+component.getName(), component.isGenericBuild(), mPreferencesName);
					}
				}
				SaveDateLastUpdate(DATA_PATH);
				setState(GI_STATE_DOWNLOAD_FILES_SUCCESFUL);
		//#endif
			
				DBG("GameInstaller","Success download, opening app: "+this.getClass().getName());
			break;
			case SUBSTATE_DOWNLOAD_MANAGER_INIT:
			{
				acquireWifiLock();
				
				DBG("GameInstaller","URL = " + SERVER_URL);
				DBG("GameInstaller","SD_FOLDER = " + DATA_PATH);
				DBG("GameInstaller","sd_folder = " + sd_folder);

				// Start download timing
			#if USE_GOOGLE_ANALYTICS_TRACKING
				if (m_iRealRequiredSize > 0)
				GoogleAnalyticsTracker.startTimingTracking(
					GoogleAnalyticsConstants.Category.Installer,
					System.currentTimeMillis(),
					GoogleAnalyticsConstants.Name.InstallerDownloadTiming,
					GoogleAnalyticsConstants.Label.DownloadTiming);
			#endif	
			
				for(DownloadComponent component : mDownloadComponents)
				{
				//a little hack till I think of smth else
					if(component.getName().startsWith("patch") || component.getName().startsWith("main"))
						component.StartDownload(marketPath);
					else
						component.StartDownload(sd_folder);
			
			}
				for(DownloadComponent component : mDownloadComponents)
				{
					if(component.getVersionInt() != SUtils.getPreferenceInt("CurrentVersion"+component.getName(), 0, mPreferencesName))
					{
						component.UpdateVersion();
						SUtils.setPreference("CurrentVersion"+component.getName(), component.getVersionInt(), mPreferencesName);
						SUtils.setPreference("IsGenericBuild"+component.getName(), component.isGenericBuild(), mPreferencesName);
					}
				}
			//#endif
				mSubstate = SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE;
				break;
			}

			case SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE:
			case SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION:
			{
			#if USE_GOOGLE_TV_BUILD
				if(((currentNetUsed == ConnectivityManager.TYPE_WIFI) && !isWifiAlive())
				|| ((currentNetUsed == ConnectivityManager.TYPE_MOBILE) && !isEthernetAlive()))
				{
					DBG("GameInstaller","Downloading error: Internet connection lost");
					for(DownloadComponent component : mDownloadComponents)
					{
						component.cancelDownload();
					}

					// Stop download timing
				#if USE_GOOGLE_ANALYTICS_TRACKING
					if (m_iRealRequiredSize > 0)
					GoogleAnalyticsTracker.stopTimingTracking(
						GoogleAnalyticsConstants.Category.Installer,
						System.currentTimeMillis(),
						GoogleAnalyticsConstants.Name.InstallerDownloadTiming,
						GoogleAnalyticsConstants.Label.DownloadTiming);
				#endif

					ERR_TOAST(ToastMessages.GameInstallerNoWifi.DownloadManagerNoWifi);
					setState(GI_STATE_DOWNLOAD_FILES_ERROR);

				}
			#else
				if(((currentNetUsed == ConnectivityManager.TYPE_WIFI) && !isWifiAlive())
				|| ((currentNetUsed == ConnectivityManager.TYPE_MOBILE) && (isWifiAlive() || !isOtherNetAlive())))
				{
					DBG("GameInstaller","Downloading error: Internet connection lost");
					//currentNetUsed = ConnectivityManager.TYPE_WIFI;
					for(DownloadComponent component : mDownloadComponents)
					{
						component.cancelDownload();
					}
					
					// Stop download timing
				#if USE_GOOGLE_ANALYTICS_TRACKING
					if (m_iRealRequiredSize > 0)
					GoogleAnalyticsTracker.stopTimingTracking(
						GoogleAnalyticsConstants.Category.Installer,
						System.currentTimeMillis(),
						GoogleAnalyticsConstants.Name.InstallerDownloadTiming,
						GoogleAnalyticsConstants.Label.DownloadTiming);
				#endif

					ERR_TOAST(ToastMessages.GameInstallerNoWifi.DownloadManagerNoWifi);
					
					if(mSubstate == SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION)//download has been interrupted in cancel screen, restart the download process.
						mSubstate = SUBSTATE_INIT;//set substate to init to avoid errors
					setState(GI_STATE_DOWNLOAD_FILES_ERROR);
					return;
				}
			#endif
				for(DownloadComponent component : mDownloadComponents)
				{
					component.Update();
				}

				m_iDownloadedSize = 0;
				for(DownloadComponent component : mDownloadComponents)
				{
					m_iDownloadedSize += component.getDownloadedSizeForDownloader();
				}

				mCurrentProgress = (int)(m_iDownloadedSize >> 10);
				//DBG("DownloadComponent", "progress:" +mCurrentProgress +" total: "+mTotalSize);

				boolean iscompleted = true;
				for(DownloadComponent component : mDownloadComponents)
				{
					if(!component.isDownloadCompleted())
						iscompleted = false;
				}
				if(iscompleted)
				{			
				#if USE_GOOGLE_ANALYTICS_TRACKING
					// end download timing + sending it
					if (m_iRealRequiredSize > 0)
					GoogleAnalyticsTracker.sendTimingTracking(
						GoogleAnalyticsConstants.Category.Installer,
						System.currentTimeMillis(),
						GoogleAnalyticsConstants.Name.InstallerDownloadTiming,
						GoogleAnalyticsConstants.Label.DownloadTiming);
				#endif
					DBG("GameInstaller","Downloading completed!");
						setState(GI_STATE_DOWNLOAD_FILES_SUCCESFUL);
				}
				boolean isfailed = false;
				for(DownloadComponent component : mDownloadComponents)
				{
					if(component.isDownloadFailed())
					{
					//if(!canReach(component.getServerUrl()))
						//{
							isfailed = true;
						#if USE_GOOGLE_ANALYTICS_TRACKING
							// Stop download timing
							if (m_iRealRequiredSize > 0)
							GoogleAnalyticsTracker.stopTimingTracking(
								GoogleAnalyticsConstants.Category.Installer,
								System.currentTimeMillis(),
								GoogleAnalyticsConstants.Name.InstallerDownloadTiming,
								GoogleAnalyticsConstants.Label.DownloadTiming);
						#endif
					}
				}
				if(isfailed)
				{
					ERR_TOAST(ToastMessages.Downloader.Failed);
					setState(GI_STATE_DOWNLOAD_FILES_ERROR);
					DBG("GameInstaller","Downloading failed!");
				}
				break;
			}
		}
#if !USE_SIMPLIFIED_INSTALLER
		if(
	
		m_bDownloadBackground && 
	
		!mhasFocus && mState == GI_STATE_DOWNLOAD_FILES_ERROR)
		{
			DBG("GameInstaller","Error: downloading in background, send to waitting for Wi-fi");
			currentDownloadFile = 0;
			validateFiles();
			destroyObjects();
			setState (GI_STATE_WAITING_FOR_WIFI);
		}
		#endif
		updateProgressBar();
		updateProgressText();
	}

	private void acquireWifiLock()
	{
		if (currentNetUsed != ConnectivityManager.TYPE_MOBILE)
		{
		#if !USE_SIMPLIFIED_INSTALLER
			mIsAirplaneOn = isAirplaneModeOn(this);
		#endif
			if(mWifiLock == null)
				mWifiLock =  mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Installer");
			
			if (!mWifiLock.isHeld())
			{
				mWifiLock.acquire();
			}
			
			if(mWakeLock == null)
			{
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Installer_PowerLock");
			}
			
			if (!mWakeLock.isHeld())
			{
				mWakeLock.acquire();
			}
			
		}
	}
	
	private void releaseWifiLock()
	{
		if (currentNetUsed != ConnectivityManager.TYPE_MOBILE)
		{
			DBG("GameInstaller", "releaseWifiLock()");
			if (mWifiLock != null)
			{
				if(mWifiLock.isHeld())
					mWifiLock.release();
				mWifiLock = null;
			}
			
			if(mWakeLock != null)
			{
				if (mWakeLock.isHeld())
				{
					mWakeLock.release();
				}
				mWakeLock = null;
			}
		}
	}
	
	public int mCurrentProgress = 0;
	public void updateProgressBar()
	{
		if ((mState != GI_STATE_DOWNLOAD_FILES && mState != GI_STATE_VERIFYING_CHECKSUM && mState != GI_STATE_UNZIP_DOWNLOADED_FILES) || mSubstate == SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION)
			return;
			
		if(mState == GI_STATE_DOWNLOAD_FILES 
		&& mSubstate != SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE
		)
			return;
		
		if (!bIsPaused)
		{
			this.runOnUiThread(new Runnable ()
			{
				public void run ()
				{
					try
					{
						
						ProgressBar bar = (ProgressBar)findViewById(R.id.data_downloader_linear_progress_bar);
						if (bar != null)
							if (mState == GI_STATE_VERIFYING_CHECKSUM || mState == GI_STATE_UNZIP_DOWNLOADED_FILES)
								bar.setProgress(mCurrentProgress);
							else
								bar.setProgress(((int)(mDownloadedSize / 1024)) + mCurrentProgress);
					
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
			});
		}
	}
	
	private void updateProgressText()
	{
		if (mState != GI_STATE_DOWNLOAD_FILES)
			return;
		if (mState == GI_STATE_DOWNLOAD_FILES &&
			!(mSubstate == SUBSTATE_DOWNLOAD_MANAGER_DOWNLOAD_FILE || (mSubstate == SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION && bIsPaused))
			)
		{
			return;
		}
		
		float currentDownload, totalDownload;
		
		//DBG("GameInstaller","downloadedsize: "+(mDownloadedSize / 1024.0)+" progress: "+mCurrentProgress);
		currentDownload = (float)(((mDownloadedSize / 1024.0) + mCurrentProgress) / 1024.0);
		totalDownload = (float)(mTotalSize / 1048576.0);
		
		if (currentDownload > totalDownload)
			currentDownload = totalDownload;
		
		
		final String downloadText = getString(R.string.DOWNLOADING).replace("{SIZE}", "" + formatter.format(currentDownload)).replace("{TOTAL_SIZE}", "" + formatter.format(totalDownload));
		
		if (!bIsPaused && mSubstate != SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION)
		{
			this.runOnUiThread(new Runnable ()
			{
				public void run ()
				{
					try
					{
						TextView text = (TextView) findViewById(R.id.data_downloader_progress_text);
						if (text != null)
						{
							text.setVisibility(View.VISIBLE);
							text.setText(downloadText);
						}
					}
					catch(Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
			});
		}
		else if (bIsPaused)
		{
			showNotification(GI_STATE_DOWNLOAD_FILES, downloadText, (int)(mTotalSize/1024+1), ((int)(mDownloadedSize / 1024)) + mCurrentProgress);
		}
	}
	
	
	private void setButtonVisibility(final int id, final boolean visible) 
	{
		this.runOnUiThread(new Runnable () 
			{
				public void run () 
				{
					try 
					{        
						Button button = (Button) findViewById(id);
						android.widget.FrameLayout frame = (android.widget.FrameLayout) button.getParent();
						frame.setVisibility(visible?View.VISIBLE:View.GONE);
					} catch (Exception e) {
						DBG_EXCEPTION(e);
					}
				}
			});
	}
	
	private void setBarVisibility(final int id, final boolean visible) 
	{
		this.runOnUiThread(new Runnable () 
			{
				public void run () 
				{
					try 
					{        
						ProgressBar bar = (ProgressBar)findViewById(id);
						bar.setVisibility(visible?View.VISIBLE:View.GONE);
					} catch (Exception e) {
						DBG_EXCEPTION(e);
					}
				}
			});
	}
	
	private void updateNewVersionStates()
	{
		switch(mState)
		{
			case GI_STATE_INIT_UPDATE_VERSION:
				DBG("GameInstaller", "STATE_INIT_UPDATE_VERSION " + sUpdateAPK);
				if (SERVER_URL.equals(""))
					setDataServerUrl();
				if (!sUpdateAPK)
				{
					DBG("GameInstaller", "checkUpdate: "+checkUpdate+" isUpdatingNewVersion: "+isUpdatingNewVersion);
					if(
					//#if USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
					//	getErrorPresent(ERROR_NO_NATIVE) || 
					//#endif
						(!checkUpdate && !isUpdatingNewVersion && CheckDayToUpdateNewVersion()))
						//do test again
						
					{
						//do test again
						checkUpdate = true;
						DBG("GameInstaller", "CheckDayToUpdateNewVersion ===============");
						sbStarted			 = false;
						mStatus				 = STATUS_NO_ERROR;
						isUpdatingNewVersion = true;
						setState(GI_STATE_SEND_REQUEST);
					}
					else
					{
						checkUpdate = true;
						launchGame = true;
						setState(GI_STATE_FINALIZE);
					}
				}
			#if !USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
				if (sUpdateAPK)
				{
					isEnoughInternalSpace();
					DBG("GameInstaller", "GI_STATE_INIT_UPDATE_VERSION, launchGame = " + launchGame + " checkUpdate = " + checkUpdate + " isUpdatingNewVersion = " + isUpdatingNewVersion);
					#if AUTO_UPDATE_HEP
					if(launchGame && !checkUpdate && !isUpdatingNewVersion && isNetworkAvailable())
					#else
					if(launchGame && !checkUpdate && !isUpdatingNewVersion && CheckDayToUpdateNewVersion())
					#endif
					 {
						DBG("GameInstaller", "GI_STATE_INIT_UPDATE_VERSION, GI_STATE_SEND_REQUEST");
						sbStarted = false;
						mDevice = new Device();
						mXPlayer = new XPlayer(mDevice);
						isUpdatingNewVersion = true;
						setState(GI_STATE_SEND_REQUEST);
					 }
					 else
					 {
					#if AUTO_UPDATE_HEP
						if (mPrevState == GI_STATE_GAMELOFT_LOGO)
						{
							//do test again
							setErrorPresent(ERROR_NO_SD, hasSDCard()); //test for SD CARD
							setErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST, isValidResourceVersion()?1:0);
							setErrorPresent(ERROR_FILES_NOT_VALID, validateFiles()); //test for files needed
							
							setState(GI_STATE_UPDATE_FINISH);
							checkUpdate = true;
							return;
						}
						else
							setState(GI_STATE_FINALIZE);
					#else
					//#if !USE_DYNAMIC_DOWNLOAD_LINK && !USE_MARKET_INSTALLER
					//	checkUpdate = true;
					//	launchGame = true;
					//	setState(GI_STATE_FINALIZE);
					//	return;
					//#else
						sUpdateAPK = checkUpdate = isUpdatingNewVersion = false;
						setState(GI_STATE_INIT_UPDATE_VERSION);
						return;
					//#endif
					#endif
					 }
					 checkUpdate = true;
				 }
			#endif
			break;
			case GI_STATE_SEND_REQUEST:
			//#if USE_MARKET_INSTALLER || USE_DYNAMIC_DOWNLOAD_LINK || GOOGLE_MARKET_DOWNLOAD
				DBG("GameInstaller","GI_STATE_SEND_REQUEST");
				if (!sUpdateAPK)
				{
					DBG("GameInstaller","Checking for data update...");
					//mClientHTTP = new HttpClient();
					
					String updateServer = null;
					/*if(mIsOverwrittenLink)
						updateServer = SERVER_URL;
					else
						updateServer = SERVER_URL + "&head=1";*///geroge - deocamdata lasam asa
					boolean success = true;
					for(DownloadComponent component : mDownloadComponents)
					{
						if(!component.requestForNewVersion())
							success = false;
					}
					//if(getDataStream(updateServer))
					if(success)
					{
						setState(GI_STATE_WAIT_REPONSE);
					}
					else
					{
						DBG("GameInstaller", "GI_STATE_SEND_REQUEST failed");
						if (!getDataStream(SERVER_URL + "&head=1") 
						//#if GOOGLE_MARKET_DOWNLOAD
						&& SUtils.hasConnectivity() == 1
						//#endif
						)
						{
							DBG("GameInstaller", "After GI_STATE_CHECK_ERRORS, setState(GI_STATE_FINALIZE); ");
							mStatus = STATUS_NO_ERROR;
							setState(GI_STATE_FINALIZE); //Everything is fine
						}
						else
						{
							DBG("GameInstaller", "go to GI_STATE_RETRY_UPDATE_VERSION");
							setState(GI_STATE_RETRY_UPDATE_VERSION);
						}
					}
				}
			//#endif
			#if !USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
			//TODO: fix define for enable/disable autoupdate only with gloft shop
			//#if GLOFT_DRM
				if (sUpdateAPK)
				{
				#if AUTO_UPDATE_HEP
					String key;
					try
					{
						String build_number = android.os.Build.FINGERPRINT;
						build_number = formatFINGERPRINT(build_number);
						key = "igpcode=" + Device.getDemoCode() + "&bn=" + build_number + "&lvl=" + Build.VERSION.SDK_INT + "&ver=" + SUtils.getVersionInstalled() + ((test_version)?"&test=1":"&test=0");
						DBG("GameInstaller",key);
					}
					catch(Exception e)
					{
						DBG("GameInstaller","GI_STATE_SEND_REQUEST error "+e.getMessage());
						key = "";
					}
					String URL = "https://secure.gameloft.com/partners/android/update_check_hep.php";
				#else
					String key = "key=" + SUtils.ReadFile(R.raw.serialkey);
					String URL = "https://secure.gameloft.com/partners/android/update_check.php";
				#endif
					mXPlayer.sendRequestNewVersion(URL,key);
					setState(GI_STATE_WAIT_REPONSE);
				}
			#endif
			break;
			case GI_STATE_WAIT_REPONSE:
				if (!sUpdateAPK)
				{
				// we have the default version, but a new version now exists
				// a newer version exists on the server
					boolean update = false;
					for(DownloadComponent component : mDownloadComponents)
					{
						component.handleRequestForNewVersion();
						
						if(!component.isGenericBuild() && SUtils.getPreferenceBoolean("IsGenericBuild"+component.getName(), false, mPreferencesName))
						{
							DBG("GameInstaller", "we have the default version, but a new version now exists");
							update = true;
						}
						else if(component.getVersionInt() > SUtils.getPreferenceInt("CurrentVersion"+component.getName(), 0, mPreferencesName))
						{
							update = true;
						}
					}
					if(update)
						setState(GI_STATE_CONFIRM_UPDATE_VERSION);
					else
					{
						DBG("GameInstaller", "Latest version already installed.");
						#if GOOGLE_MARKET_DOWNLOAD && UNZIP_DOWNLOADED_FILES
							if(checkUnzipSpace())
								setState(GI_STATE_UNZIP_DOWNLOADED_FILES);
							else
								setState(GI_STATE_SOLVE_ERROR);
						#else
							SaveDateLastUpdate(DATA_PATH);
						#if !AUTO_UPDATE_HEP
							launchGame = true;
							mStatus = STATUS_NO_ERROR;
							setState(GI_STATE_FINALIZE);
						#else
							setState(GI_STATE_NO_NEW_VERSION);
						#endif
						#endif
					}

				}
			#if !USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
				if (sUpdateAPK)
				{
					while (!mXPlayer.handleRequestNewVersion())
					{
						try {
							Thread.sleep(50);
						} catch (Exception exc) {}
						DBG("GameInstaller", "[Validating FULL VERSION GAME]Waiting for response...");
					}

					if (mXPlayer.str_response != null)
					{
						if(mXPlayer.str_response.contains("Error: No live release") || mXPlayer.str_response.contains("Error: No updates found"))
						{
						#if !AUTO_UPDATE_HEP
							launchGame = true;
							mStatus = STATUS_NO_ERROR;
							sUpdateAPK = launchGame = checkUpdate = isUpdatingNewVersion = false;
							setState(GI_STATE_INIT_UPDATE_VERSION);
							return;
						#else
							setState(GI_STATE_NO_NEW_VERSION);
							break;
						#endif
						}
						String local_version = "";
						String server_version = "";
						try
						{
							local_version = SUtils.ReadFile(R.raw.infoversion).trim();
							DBG("GameInstaller", "===========local_version =(" + local_version + ")");
							server_version = GetCurrentVersion(mXPlayer.str_response).trim();
							DBG("GameInstaller", "===========server_version =(" + server_version + ")");
						}
						catch(Exception exc)
						{
							isUpdatingNewVersion = false;
						}
						if(local_version.compareTo(server_version) == 0)
						{
						#if !AUTO_UPDATE_HEP
							//launchGame = true;
							mStatus = STATUS_NO_ERROR;
							//setState(GI_STATE_FINALIZE);
							sUpdateAPK = launchGame = checkUpdate = isUpdatingNewVersion = false;
							setState(GI_STATE_INIT_UPDATE_VERSION);
							return;
						#else
							setState(GI_STATE_NO_NEW_VERSION);
						#endif
						}
						else
							setState(GI_STATE_CONFIRM_UPDATE_VERSION);
					}
					else
					{
						setState(GI_STATE_RETRY_UPDATE_VERSION);
					}
				}
			#endif
			break;
			case GI_STATE_CONFIRM_UPDATE_VERSION:
				launchGame = true;
			break;
			case GI_STATE_RETRY_UPDATE_VERSION:
				#if AUTO_UPDATE_HEP
				if (sUpdateAPK)
				{
					//do test again
					setErrorPresent(ERROR_NO_SD, hasSDCard()); //test for SD CARD
					setErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST, isValidResourceVersion()?1:0);
					setErrorPresent(ERROR_FILES_NOT_VALID, validateFiles()); //test for files needed
					
					if (getErrorPresent(ERROR_FILES_NOT_VALID) || getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
					{
						setState(GI_STATE_UPDATE_FINISH);
					}else
						setState(GI_STATE_FINALIZE);
				}
				#endif
				
			break;
			case GI_STATE_NO_NEW_VERSION:
			#if AUTO_UPDATE_HEP
				if (sUpdateAPK)
				{
					//do test again
					setErrorPresent(ERROR_NO_SD, hasSDCard()); //test for SD CARD
					setErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST, isValidResourceVersion()?1:0);
					setErrorPresent(ERROR_FILES_NOT_VALID, validateFiles()); //test for files needed
					//continue flow
					if (getErrorPresent(ERROR_FILES_NOT_VALID) || getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
						setState(GI_STATE_UPDATE_FINISH);
					else
						setState(GI_STATE_FINALIZE);
				}
			#endif
			break;
			case GI_STATE_UPDATE_GET_NEW_VERSION:
			//#if USE_MARKET_INSTALLER || USE_DYNAMIC_DOWNLOAD_LINK || GOOGLE_MARKET_DOWNLOAD
				if (!sUpdateAPK)
					setState(GI_STATE_DOWNLOAD_FILES);
			//#endif
			#if !USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
				if (sUpdateAPK)
				{
					try
					{
						DBG("GameInstaller", "GI_STATE_CONFIRM_UPDATE_VERSION Url =  ("+ GetUrl(mXPlayer.str_response)+ ")");
						Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(GetUrl(mXPlayer.str_response)));
						((Context)this).startActivity(i);
						launchGame = false;
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
					setState(GI_STATE_FINALIZE);
				}
			#endif
			break;
			default:
			break;
		}
		if (!isUpdatingNewVersion)
			setState(this.GI_STATE_FINALIZE);
	}
	
#if USE_HEP_ANTIPIRACY
	private void updateCheckPiracy()
	{
			String supportedModels = HEP_SUPPORTED_MODELS;
			String supportedManufactures = HEP_SUPPORTED_MANUFACTURER;
			String[] arrModels = supportedModels.split(",");
			String[] arrManufactures = supportedManufactures.split(",");
			String Manufacture = Build.MANUFACTURER;
			String Model = Build.MODEL;
			
			DBG("GameInstaller", "------------- Build.MANUFACTURER: " + Manufacture +" -----------------");
			DBG("GameInstaller", "------------- Build.MODEL: " + Model +" -----------------");
			DBG("GameInstaller", "------------- supportedManufactures: " + supportedManufactures +" -----------------");
			DBG("GameInstaller", "------------- supportedModels: " + supportedModels +" -----------------");
				
			boolean bSupportDevice = false;
			int len=arrModels.length;
			if(arrManufactures.length < len)
				len = arrManufactures.length;
			for(int i=0; i<len; i++)
				if(arrModels[i].trim().equals(Model) && arrManufactures[i].trim().equals(Manufacture))
				{
					bSupportDevice = true;
					break;
				}			
					
			if(bSupportDevice == false)
			{
				mbLicense = false;
				miLicenseMessage = R.string.HEP_DEVICE_INVALID;
				// GLDebug.INFO("DRM", "HEP Antyprivacy --- Device is invalid");
				ERR_TOAST(ToastMessages.GameInstallerPiracyError.HEPInvalidDevice);
				setState(GI_STATE_PIRACY_ERROR);
			}
			else		
				setState(GI_STATE_CHECK_ERRORS);
	}
#else
	private void updateCheckPiracy()
	{
	#if VERIZON_DRM
		mbLicense = mVerizonLicense.checkLicense();

		if(!mbLicense)
		{
			mbLicense = mVerizonLicense.canPlayFree();

			if(mbLicense)
			{
				setState(GI_STATE_CHECK_ERRORS);
			}
			else
			{
				miLicenseMessage = mVerizonLicense.getStringErrorID();
				ERR_TOAST(ToastMessages.GameInstallerPiracyError.VerizonLicenceError);
				setState(GI_STATE_PIRACY_ERROR);
			}
		}
		else
		{
			setState(GI_STATE_CHECK_ERRORS);
		}
	#elif TMOBILE_DRM
		if (mTMobileSubstate == SUBSTATE_INIT_TMOBILE)
		{
			goToLayout(R.layout.data_downloader_progressbar_layout, LAYOUT_CHECKING_LICENSE);
			if(!mTMobileLicense.isAirplaneModeOn())
			{
				mTMobileLicense.InitMobileNetwork();
				mTMobileSubstate = SUBSTATE_WAITING_TMOBILE;			
				//try { Thread.sleep(50); } catch(Exception e){};
			}
			else
				mTMobileSubstate = SUBSTATE_PROCESS_TMOBILE;			
		} else if (mTMobileSubstate == SUBSTATE_WAITING_TMOBILE)
		{
			mTMobileLicense.updateTimer(500);
			try { 	Thread.sleep(500); 	} 	catch(Exception e){};
			
			if(!mTMobileLicense.isWaitting())
				mTMobileSubstate = SUBSTATE_PROCESS_TMOBILE;
		}
		else if (mTMobileSubstate == SUBSTATE_PROCESS_TMOBILE)
		{
			if(mTMobileLicense.canCheck())
			{
				mbLicense = mTMobileLicense.checkLicense();
				if(!mTMobileLicense.isAirplaneModeOn())
					mTMobileLicense.RestoreNetworkState();
			}
			else
				mbLicense = mTMobileLicense.checkLicenseSaved();

			if(!mbLicense)
			{
				mbLicense = mTMobileLicense.canPlayFree();

				if(mbLicense)
				{
					mTMobileLicense.addFreeCount();
					setState(GI_STATE_CHECK_ERRORS);
				}
				else
				{
					miLicenseMessage = mTMobileLicense.getStringErrorID();
					ERR_TOAST(ToastMessages.GameInstallerPiracyError.TMobileLicenceError);
					setState(GI_STATE_PIRACY_ERROR);
				}
			}
			else
			{
				setState(GI_STATE_CHECK_ERRORS);
			}
			mTMobileLicense.addCheckCount();
		}
	#elif ORANGE_DRM
		mbLicense = mOrangeLicense.checkLicense();
		DBG("OrangeDRM", mbLicense);
		if (!mbLicense)
		{
			ERR_TOAST(ToastMessages.GameInstallerPiracyError.TMobileLicenceError);
			setState(GI_STATE_PIRACY_ERROR);
		}
		else
		{
			setState(GI_STATE_CHECK_ERRORS);
		}
	#elif GOOGLE_MARKET_DOWNLOAD
	
	String data = SUtils.getOverriddenSetting(DATA_PATH + "qaTestingConfigs.txt", "SKIP_VALIDATION");
	if(data == null)
	{
		data = SUtils.getOverriddenSetting(marketPath + "/qaTestingConfigs.txt", "SKIP_VALIDATION");
	}
	
#if ACP_UT	
	if(true)
#else
	if(data != null && data.equals("1"))
#endif

	{
		DBG("GameInstaller","SKIP_VALIDATION, skipping GoogleDRM.");
		setState(GI_STATE_CHECK_ERRORS);
	}
	else
	{
	for(DownloadComponent component : mDownloadComponents)
	{
		if(component.getName().equals(""))
			component.setImportance(0);
	}
	
	String currentVersion = readVersion();
	
	long currentTimeSec = System.currentTimeMillis() / 1000;
	long lastTimeConnectSec = SUtils.getPreferenceLong("TimeStamp", 0, "ExpansionPrefs");
	boolean isGEFinit = !SUtils.getPreferenceString("mainFileName", "", "ExpansionPrefs").equals("") || !SUtils.getPreferenceString("patchFileName", "", "ExpansionPrefs").equals("");
	DBG("GameInstaller","is gef init: " + isGEFinit);
	if(isGEFinit)
	{
		String mainFile = SUtils.getPreferenceString("mainFileName", "", "ExpansionPrefs");
		String patchFile = SUtils.getPreferenceString("patchFileName", "", "ExpansionPrefs");
		DBG("GameInstaller","main: "+mainFile+" patch: "+patchFile);
		if(!mainFile.equals(""))
			mDownloadComponents.add(new DownloadComponent("", mainFile, GAME_VERSION_NAME, mainFile, 0, true));
		if(!patchFile.equals(""))
			mDownloadComponents.add(new DownloadComponent("", patchFile, GAME_VERSION_NAME, patchFile, 0, true));
			
		readInstallerResource();
	}
	
	DBG("GameInstaller", "grace period has passed? "+ ((currentTimeSec - lastTimeConnectSec) >= GOOGLE_GRACE_PERIOD) + " currenttime: "+currentTimeSec+" last time: "+lastTimeConnectSec);
	
	if( !isGEFinit || (currentTimeSec - lastTimeConnectSec) >= GOOGLE_GRACE_PERIOD || validateFiles() != 0)
	{
	
	if(checkAccountsNum() == 0) {
		setState(GI_STATE_NO_GP_ACCOUNT_DETECTED);
	} else {
		#if HDIDFV_UPDATE
		final com.google.android.vending.licensing.APKExpansionPolicy aep = new com.google.android.vending.licensing.APKExpansionPolicy(this, new com.google.android.vending.licensing.AESObfuscator(SALT, STR_APP_PACKAGE, Device.getHDIDFV()));
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#else
		final com.google.android.vending.licensing.APKExpansionPolicy aep = new com.google.android.vending.licensing.APKExpansionPolicy(this, new com.google.android.vending.licensing.AESObfuscator(SALT, STR_APP_PACKAGE, Secure.getString(this.getContentResolver(), Secure.ANDROID_ID)));
		#endif
		aep.resetPolicy();
		System.setProperty("https.keepAlive", "false");
		System.setProperty("http.keepAlive", "false");
		final com.google.android.vending.licensing.LicenseChecker checker = new com.google.android.vending.licensing.LicenseChecker(this, aep, getKey());
		checker.checkAccess(new com.google.android.vending.licensing.LicenseCheckerCallback()
		{
			@Override
			public void allow(int reason)
			{
				DBG("DRM", "allow(int reason) reason: " + reason);
				
				SUtils.setPreference("TimeStamp", (System.currentTimeMillis() / 1000), "ExpansionPrefs");
				
				
				Iterator itr = mDownloadComponents.iterator();
				while(itr.hasNext())
				{
					DownloadComponent cc = (DownloadComponent)(itr.next());
					if(cc.getName().startsWith("patch") || cc.getName().startsWith("main"))
					{
						DBG("GameInstaller","removing component with name: "+cc.getName());
						itr.remove();
					}
				}
				DBG("DownloadComponent","google urls no: "+aep.getExpansionURLCount());
				
				if(aep.getExpansionURLCount() > 0)
				{
					DBG("DownloadComponent","Clearing preferences for main and patch");
					SUtils.removePreference("mainFileName", "ExpansionPrefs");
					SUtils.removePreference("patchFileName", "ExpansionPrefs");
				}
				
				for (int i = 0 ; i < aep.getExpansionURLCount(); i++)
				{
					
					String name = "";
					String version = "";
					DBG("DownloadComponent","google file name: "+aep.getExpansionFileName(i));
					DBG("DownloadComponent","google file size: "+aep.getExpansionFileSize(i));
					Pattern p = Pattern.compile("(\\w+).(\\d{1,3})");
					try{
						Matcher m = p.matcher(aep.getExpansionFileName(i));
						if(m.find())
						{
							name = aep.getExpansionFileName(i);
							version = m.group(2);
						}
						
						SUtils.setPreference(m.group(1)+"FileName", aep.getExpansionFileName(i), "ExpansionPrefs");
					}catch(Exception e)
					{
						DBG("GameInstaller", "Error: " + e.getMessage());
					}
					long size = aep.getExpansionFileSize(i);
					
					boolean found = false;
					for(DownloadComponent component : mDownloadComponents)
					{
						
						if(component.getName().equals(name))
						{
							DBG("DownloadComponent","found "+component.getName()+"with size: "+size); 
							found = true;
							component.setServerUrl(aep.getExpansionURL(i));
							component.setServerFileSize(size);
							component.setServerFileName(aep.getExpansionFileName(i));
							component.setVersion(version);
							component.setFixedPath(true);
						}
					}
					if(!found)
					mDownloadComponents.add(new DownloadComponent(aep.getExpansionURL(i),name,version, aep.getExpansionFileName(i),size, true));
					DBG("GameInstaller","google url: "+aep.getExpansionURL(i));

				}
				setState(GI_STATE_CHECK_ERRORS);
			}
			@Override
			public void dontAllow(int reason)
			{
				DBG("DRM", "dontAllow because of the reason: " + reason);
				switch (reason)
				{
				case com.google.android.vending.licensing.Policy.NOT_LICENSED:
					showDialog(3);
					break;
				case com.google.android.vending.licensing.Policy.RETRY:
				case com.google.android.vending.licensing.Policy.LICENSED:
					showDialog(5);
					break;
				}
			}
			@Override
			public void applicationError(int errorCode)
			{
				DBG("DRM", "applicationError " + errorCode);
				showDialog(4);
			}
		});
		}
	}
	else
	{
		DBG("GameInstaller","Not connecting to google");
		setState(GI_STATE_CHECK_ERRORS);
	}
  }
	
	#else
		setState(GI_STATE_CHECK_ERRORS);
	#endif
	}
	
#if GOOGLE_MARKET_DOWNLOAD || GOOGLE_DRM
	private int checkAccountsNum()
	{
		int accNum = 1;//default
		try{
			android.accounts.AccountManager am = android.accounts.AccountManager.get(this.getApplicationContext());
			accNum = am.getAccountsByType("com.google").length;
		}catch(Exception e){ 
			// ERR("GameInstaller","AccountManager exception "+e);
			accNum = 1;
		}
		return accNum;//return 1, if you don't have the GET_ACCOUNTS permission, an Exception will occur, but the checking process needs to continue
	}
#endif

#if GLOFT_DRM

private void GloftDrmCheck()
{
	mbLicense = mGloftLicense.checkLicense();
	if(!mbLicense)
	{
		mbLicense = mGloftLicense.canPlayFree();
	
		if(mbLicense)
		{
			DBG("DRM", "GLOFT_DRM: free play");
			mGloftLicense.addFreeCount();			
			setState(GI_STATE_GAMELOFT_LOGO);
		}
		else
		{
			DBG("DRM", "GLOFT_DRM: invalid license");
			miLicenseMessage = mGloftLicense.getStringErrorID();
			ERR_TOAST(ToastMessages.GameInstallerPiracyError.GloftLicenceError);
			setState(GI_STATE_PIRACY_ERROR);
		}
	}
	else
	{
		DBG("DRM", "GLOFT_DRM: valid license");
		setState(GI_STATE_GAMELOFT_LOGO);
	}
}
#endif
#if GOOGLE_DRM
	//for java only
	private void GoogleDrmCheck()
	{
		#if HDIDFV_UPDATE
		DBG("DRM","GOOGLE_DRM: deviceID: " + Device.getHDIDFV());
		#else
		DBG("DRM","GOOGLE_DRM: deviceID: " + Device.getDeviceId());
		#endif
		if(checkAccountsNum() == 0) {
			setState(GI_STATE_NO_GP_ACCOUNT_DETECTED);
		} else {		
			#if HDIDFV_UPDATE
			mChecker = new LicenseChecker(this, new JOnlyDRMPolicy(this, new AESObfuscator(SALT, getPackageName(), Device.getHDIDFV())), getKey());
			DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
			#else
			mChecker = new LicenseChecker(this, new JOnlyDRMPolicy(this, new AESObfuscator(SALT, getPackageName(), Device.getDeviceId())), getKey());
			#endif
			long startTime = System.currentTimeMillis();
			GoogleLCC googleLCC = new GoogleLCC();
			mChecker.checkAccess(googleLCC);
			while ((!googleLCC.checkFinished) && (System.currentTimeMillis() - startTime < 4000))
			{
				DBG("DRM","GOOGLE_DRM: waiting...");
				try { Thread.sleep(100); } catch (Exception e) {}
				
			}
			mChecker = null;
		}
	}
	private class GoogleLCC implements LicenseCheckerCallback 
	{
		public boolean checkFinished = false;
		
//		Possible allow parameterReason Values:
//		LICENSED = 256;
//		NOT_LICENSED = 561;
//		RETRY = 291;
		public void allow(int paramReason) 
		{
			DBG("DRM","GOOGLE_DRM: allow(): " + paramReason);
			setState(GI_STATE_GAMELOFT_LOGO);
			checkFinished = true;
		}
	
//		Possible dontAllow parameterReason Values:
//		LICENSED = 256;
//		NOT_LICENSED = 561;
//		RETRY = 291;
		public void dontAllow(int paramReason)
		{
			DBG("DRM","GOOGLE_DRM: dontAllow(): " + paramReason);
			if (SUtils.hasConnectivity() == 0)
			{
				DBG("DRM","GOOGLE_DRM: Network problem");
				showDialog(5);
			}
			else
			{
				DBG("DRM","GOOGLE_DRM: invalid license");
				showDialog(3);
			}
				
			checkFinished = true;
		}
		//possible error codes (as int):
//		ERROR_INVALID_PACKAGE_NAME = 1;
// 		ERROR_NON_MATCHING_UID = 2;
//		ERROR_NOT_MARKET_MANAGED = 3;
// 		ERROR_CHECK_IN_PROGRESS = 4;
//  	ERROR_INVALID_PUBLIC_KEY = 5;
//  	ERROR_MISSING_PERMISSION = 6;
		public void applicationError(int errorCode)
		{
			DBG("DRM","GOOGLE_DRM: error: " + errorCode);
			
			checkFinished = true;
			showDialog(4);
		}
	}
#endif

#endif //USE_HEP_ANTIPIRACY
	public int getWifiMode()
	{
	//return WIFI_3G;
	#if USE_GOOGLE_TV_BUILD
		DBG("GameInstaller", "getWifiMode WIFI_3G will be used fot GTV");
		return WIFI_3G;
	#elif FORCE_WIFI_ONLY_MODE
		DBG("GameInstaller", "getWifiMode Override in configuration batch");
		return WIFI_ONLY;
	#else
		DBG("GameInstaller", "getWifiMode request");

		String ovMode = ovWifiMode();
		if (ovMode != null)
		{

			DBG("GameInstaller","File qaWifiOnlyMode.txt found with "+ovMode);
			DBG("GameInstaller","File qaWifiOnlyMode.txt found with "+ovMode.equals("WIFI_3G"));
			
			if (ovMode.equals("WIFI_ONLY") || ovMode.equals("TRUE"))
			{
				DBG("GameInstaller","QA Overriding WifiMode: WIFI_ONLY");
				#if USE_SIMPLIFIED_INSTALLER
				if((mDeviceInfo.getPhoneType() != mDeviceInfo.PHONE_TYPE_CDMA) && (mDeviceInfo.getSimState()  == mDeviceInfo.SIM_STATE_ABSENT || mDeviceInfo.getSimState()  == mDeviceInfo.SIM_STATE_UNKNOWN))
				{
					//no sim detected
					k_downloadLimit = -1;
				}
				else
				{
					k_downloadLimit = 0;
				}
				#endif
				return WIFI_ONLY;
			}
			else if (ovMode.equals("WIFI_3G") || ovMode.equals("FALSE"))
			{
				DBG("GameInstaller","QA Overriding WifiMode: WIFI_3G");
				return WIFI_3G;
			}
			else if (ovMode.equals("WIFI_3G_ORANGE_IL"))
			{
				DBG("GameInstaller","QA Overriding WifiMode: WIFI_3G_ORANGE_IL");
				return WIFI_3G_ORANGE_IL;
			}
		}
		
		if((mDeviceInfo.getPhoneType() != mDeviceInfo.PHONE_TYPE_CDMA) && (mDeviceInfo.getSimState()  == mDeviceInfo.SIM_STATE_ABSENT || mDeviceInfo.getSimState()  == mDeviceInfo.SIM_STATE_UNKNOWN))
		{
			DBG("GameInstaller","No sim found. Overriding WifiMode: WIFI_ONLY");
			return WIFI_ONLY;
		}
		
		mDevice = new Device();
		mXPlayer = new XPlayer(mDevice);

		mXPlayer.sendWifiModeRequest();

		while (!mXPlayer.handleWifiModeRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		}
		
		if(mXPlayer.getWHTTP().m_response == null)
		{
			DBG("GameInstaller", "mXPlayer.getWHTTP().m_response is null, return WIFI_3G");
			return WIFI_3G;
		}
		
		DBG("GameInstaller", mXPlayer.getWHTTP().m_response);
		if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{
			#if USE_SIMPLIFIED_INSTALLER
			if(mXPlayer.getWHTTP().m_response.contains("|"))
			{
				try{
					k_downloadLimit = Integer.parseInt(mXPlayer.getWHTTP().m_response.split("\\|")[1]);//get the download limit Parameter
					DBG("GameInstaller", "Download limit from WifiMode request="+k_downloadLimit);
				} catch (Exception e){
					k_downloadLimit = -1;
				}
			}
			#endif
			if (mXPlayer.getWHTTP().m_response.contains("WIFI_ONLY"))
			{
				DBG("GameInstaller", "WifiMode is WIFI_ONLY");
				#if USE_SIMPLIFIED_INSTALLER
				k_downloadLimit = 0;
				#endif
				return WIFI_ONLY;
			}
			if (mXPlayer.getWHTTP().m_response.contains("WIFI_3G"))
			{
				DBG("GameInstaller", "WifiMode is WIFI_3G");
				return WIFI_3G;
			}
			if (mXPlayer.getWHTTP().m_response.contains("WIFI_3G_ORANGE_IL"))
			{
				DBG("GameInstaller", "WifiMode is WIFI_3G_ORANGE_IL");
				return WIFI_3G_ORANGE_IL;
			}
		}
		DBG("GameInstaller", "WifiMode is WIFI_3G");
		return WIFI_3G;
	#endif //#if FORCE_WIFI_ONLY_MODE
	}

	public String ovWifiMode()
	{
		String data = SUtils.getOverriddenSetting(DATA_PATH + "qaTestingConfigs.txt", "WIFI_MODE");
		DBG("GameInstaller", DATA_PATH + "qaTestingConfigs.txt");
		
		#if GOOGLE_MARKET_DOWNLOAD
		if(data == null)
		{
			data = SUtils.getOverriddenSetting(marketPath + "/qaTestingConfigs.txt", "WIFI_MODE");
		}
		#endif
		
		if(data != null)
		{
			return data;
		}
		else
			return null;
	}


#if USE_GOOGLE_TV_BUILD
	private boolean isEthernetAlive()
	{
		boolean check = false;
		if (mConnectivityManager != null && mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET) != null)
			check = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).isConnected();

		return check;

	}

	private boolean isAnyNetworkAlive()
	{
		NetworkInfo[] info = mConnectivityManager.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				DBG("GameInstaller","Checking this net: " + info[i].toString());
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					DBG("GameInstaller","This net is connected: " + info[i].toString());
					return true;
				}
			}
		} else
			DBG("GameInstaller","no get all NetworkInfo");

		DBG("GameInstaller","No internet could be found by other meanings");
		return false;
	}
#endif

	NetworkInfo mNetInfo = null;
	public boolean isWifiAlive()
	{
		#if USE_GOOGLE_TV_BUILD
		if(!isWifiHardwareAvailable())
			return false;
		#endif
		boolean check = false;
		if (mConnectivityManager != null && mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null)
			check = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();


		return mWifiManager.isWifiEnabled() && check;
	}	
	
	public int activateWifi()
	{
		int result = 0;
		switch(wifiStep)
		{
			case 0:
			{
				if(!mWifiManager.isWifiEnabled())
				{
					mWifiManager.setWifiEnabled(true);
					wifiStep--;
				}
			} break;

			case 1:
			{
				if(mWifiLock == null)
				{
					mWifiLock =  mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Installer");
					wifiStep--;
				}
			} break;

			case 2:
			{
				if (!mWifiLock.isHeld())
				{
					mWifiLock.acquire();
					wifiStep--;
				}
			} break;

			case 3:
			{
				if(mWifiManager.getConnectionInfo() == null)
				{
					wifiStep--;
					netLookupRetry++;
					try{ Thread.sleep(1000); } catch (Exception ex) {DBG_EXCEPTION(ex); }
					if(netLookupRetry > NET_RETRY_MAX)
					{
						result = -1;
						if(mClientHTTP != null){
							mClientHTTP.close();
							mClientHTTP = null;
						}
					}
				}
				else
				{
					netLookupRetry = 0;
				}
			} break;

			case 4:
			{
				if(!isWifiAlive())
				{
					wifiStep--;
					netLookupRetry++;
					try{Thread.sleep(1000);}catch(Exception e){}
					if(netLookupRetry > NET_RETRY_MAX)
					{
						if(mNetInfo == null)
						{
							result = -1;
							if(mClientHTTP != null)
							{
								mClientHTTP.close();
								mClientHTTP = null;
							}
						}
						else
						{
							result = -1;
						}

						wifiStep = -1;
						netLookupRetry = 0;
					}
				}
				else
				{
					netLookupRetry = -1;
					wifiStep = 0;
					currentNetUsed = ConnectivityManager.TYPE_WIFI;
					setState(GI_STATE_DOWNLOAD_FILES);
					wasWifiActivatedByAPP = true;
					result = 1;
				}
			} break;
		}
		wifiStep++;
		return result;
	}

	public boolean isOtherNetAlive()
	{

#if USE_GOOGLE_TV_BUILD
		if(isWifiHardwareAvailable())
		{
			mNetInfo = mConnectivityManager.getActiveNetworkInfo();		
			if(mNetInfo == null || (mNetInfo.getType() == ConnectivityManager.TYPE_WIFI) || !mNetInfo.isConnected())//!canReach(SERVER_URL))
			{
				DBG("GameInstaller","Current active newtwork is WIFI and it is not connected, mNetInfo is null:"+(mNetInfo == null));
				return false;
			}
		}

		if(isEthernetAlive())
		{
			DBG("GameInstaller","Ethernet is alive");
			return true;
		}
		
		DBG("GameInstaller","No WIFI or Ethernet detected, check by other meanings");
		return isAnyNetworkAlive();
#else
		if(WIFI_MODE == WIFI_ONLY)
			return false;
		
		mNetInfo = mConnectivityManager.getActiveNetworkInfo();		
		if(mNetInfo == null || (mNetInfo.getType() == ConnectivityManager.TYPE_WIFI) || !mNetInfo.isConnected())//!canReach(SERVER_URL))
			return false;

		return true;
#endif
	}

	public static Boolean isReached = null;
	public boolean canReach(final String address)
	{	
		long startTime = System.currentTimeMillis();
		
		new Thread()
		{
			public void run()
			{
			URL aURL;
			java.net.HttpURLConnection mConn;
				try
				{
					 aURL = new URL(address);
					mConn = (java.net.HttpURLConnection)aURL.openConnection();
					mConn.connect();
					isReached = Boolean.TRUE;
					if (mConn != null)
					{
						mConn.disconnect();
						mConn = null;
					}
					if (aURL != null)
						aURL = null;
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
					isReached = Boolean.FALSE;
					mConn = null;
					aURL = null;
				}
			}
		}.start();
		
		while (isReached == null && (System.currentTimeMillis() - startTime < 2000));
		
		if (isReached == null)
			isReached = false;
		
		return isReached;
	}

	private Device mDevice;
	private XPlayer mXPlayer;
	private boolean isUpdatingNewVersion = false;
	private PackFileReader loadPack;	
	private GiSettings mGiSettings;
	private boolean mIsAirplaneOn = false;
	
	private void initOfflineResources()
	{
		int totalNumberOfFiles = 0;
		File srcDir = new File(SaveFolder);
		String mFiles[] = srcDir.list();
		DBG("GameInstaller" , "initOfflineResources");
		for (int i = 0; i < mFiles.length; i++)
        {
			if (mFiles[i].startsWith("pack") && mFiles[i].endsWith(".info"))
			{
			try
			{
				DBG("GameInstaller" ,"Found "+mFiles[i]);
				PackFileReader loadPack = new PackFileReader(this);
				Vector<PackFile> 	vec	= loadPack.read(SaveFolder+"/"+mFiles[i]);
				mOfflineResources.addAll(vec);
			}catch(Exception ex){DBG_EXCEPTION(ex);}
			}
		}
		
	/*	for(int j=0;j<mOfflineResources.size();j++)
		{
			DBG("GameInstaller" ,"offline res: "+mOfflineResources.get(j).getName());
		}*/
		
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		DBG("GameInstaller", "Installer version: " + INSTALLER_VERSION);
		DBG("GameInstaller","onCreate");

		super.onCreate(savedInstanceState);
		
		#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		GoogleAnalyticsTracker.Init(APP_PACKAGE.MainActivity.getActivityContext());
		#endif
		
		#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
		SUtils.checkNewVersionInstalled();//check for a new version installed
		//SUtils.getInjectedIGP();//initialize embed info for Google
		//SUtils.getInjectedSerialKey();//initialize embed info for Google
		#endif		
		
		SaveFolder = SUtils.getSaveFolder();
		LIBS_PATH = SaveFolder+LIBS_PATH;
		INFO_FILE_NAME = SaveFolder+INFO_FILE_NAME;
		Intent sender = getIntent();
	#if FORCE_EXTERNAL_SD_MEEP
		setSDFolder();
	#endif
		if (SUtils.getPreferenceString("SDFolder", "", mPreferencesName).equals("") 
		#if FORCE_EXTERNAL_SD_MEEP
			|| SUtils.getPreferenceString("SDFolder", "", mPreferencesName).equals(SD_FOLDER)
		#endif
		)
		{
			sd_folder = setSDFolder();
			SUtils.setPreference("SDFolder", sd_folder, mPreferencesName);
		}
		else 
			sd_folder = SUtils.getPreferenceString("SDFolder", "", mPreferencesName);
		
		DATA_PATH = sd_folder + "/";
		
		/*InputStream databaseInputStream = getResources().openRawResource(R.raw.gamedata);
		FileOutputStream out;
		try {
			out = new FileOutputStream(new File(sd_folder + "/shit.jar"));
			byte[] buf = new byte[1024];
			  
			  int len;
			 
			 while ((len = databaseInputStream.read(buf)) > 0){
			  
			  out.write(buf, 0, len);
			 
			 }
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			DBG_EXCEPTION(e1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			DBG_EXCEPTION(e);
		}*/
		
		DBG("GameInstaller", "sd_folder:" + sd_folder);
		if (sender != null && sender.getExtras() != null)
		{
			boolean isAppTerminated = sender.getExtras().getBoolean("finishGame");
			if (isAppTerminated)
			{
				finishFail();
				return;
			}
		}
		
		handler = new Handler(Looper.getMainLooper());

		android.widget.RelativeLayout mView = new android.widget.RelativeLayout(this);
		android.widget.RelativeLayout.LayoutParams lpGame = new android.widget.RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,
				android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
		lpGame.addRule(android.widget.RelativeLayout.CENTER_VERTICAL); lpGame.addRule(android.widget.RelativeLayout.CENTER_HORIZONTAL);
		mView.addView(new ProgressBar(this, null, android.R.attr.progressBarStyleLarge) , lpGame);
		mView.setBackgroundColor(android.graphics.Color.parseColor("#000000"));
		setContentView(mView);


		m_portalCode = PORTAL_CODE;
	
		mWifiManager 			= (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mDeviceInfo 			= (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		mConnectivityManager 	= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mGiSettings = new GiSettings();
		mGiSettings.readSettings(this);
	#if USE_BEAM
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			BeamSender b = new BeamSender();
			b.init(this);
		}
	#endif
		initOfflineResources();
		
		//setDataServerUrl();
				
		if(isValidResourceVersion() || mIsOverwrittenLink)
		{
			DBG("GameInstaller", "Wrong data version, remove the old pack.info");
			File srcDir = new File(SaveFolder);
			String mFiles[] = srcDir.list();
			DBG("GameInstaller" , "isValidResourceVersion");
			for (int i = 0; i < mFiles.length; i++)
			{
				if (mFiles[i].startsWith("pack") && mFiles[i].endsWith(".info"))
				{
				try
				{
					DBG("GameInstaller" ,"Found "+mFiles[i]);
					File file = new File(SaveFolder+"/"+mFiles[i]);
					if(file.exists())
					{
						DBG("GameInstaller","deleting info file named: "+mFiles[i]);
						file.delete();
					}
				}catch(Exception ex){DBG_EXCEPTION(ex);}
				}
			}
		}
		
		
		
#if USE_HEP_PACKINFO
		copyInfoFromSDtoDevice();
#endif		
		

		mStatus = STATUS_NO_ERROR;
		launchGame = false;

	#if VERIZON_DRM
		mVerizonLicense = new VerizonDRM( this );
	#elif GLOFT_DRM
		mGloftLicense = new GloftDRM( this );
	#elif TMOBILE_DRM
		mTMobileLicense = new TMobileDRM( this );
	#elif ORANGE_DRM
		mOrangeLicense = new d( this );
	#endif

	#if USE_GAME_TRACKING || USE_MARKET_INSTALLER || GOOGLE_STORE_V3
		Tracking.init();
	#endif
	#if !USE_SIMPLIFIED_INSTALLER
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		mAirPlaneReceiver = new BroadcastReceiver() {
			  @Override
			  public void onReceive(Context context, Intent intent) {
					DBG("GameInstaller", "ACTION_AIRPLANE_MODE_CHANGED state changed");
					if(!mIsAirplaneOn || isAirplaneModeOn(context))
					{
						if(!mIsAirplaneOn)
							mIsAirplaneOn = true;
						
						releaseWifiLock();
					}
			  }
		};
		
		registerReceiver(mAirPlaneReceiver, intentFilter);
	#endif

		
		m_sInstance = this;
		
	#if AUTO_UPDATE_HEP
		try{
		    alert_builder = new AlertDialog.Builder(this);
    		alert_builder.setTitle(getString(R.string.UNKNOWN_SOURCES_DIALOG_TITLE));
    		alert_builder.setMessage(getString(R.string.UNKNOWN_SOURCES_DIALOG_MESSAGE)).setCancelable(false);
    		alert_builder.setPositiveButton(getString(R.string.UNKNOWN_SOURCES_DIALOG_OPTION1), new android.content.DialogInterface.OnClickListener(){
    
    		public void onClick(android.content.DialogInterface dialog, int which) {
				try{
					startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS) ,0);
				}catch(Exception e){
					startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS) ,0 );
				}
				dialog.cancel();
    		}
    		})
    		.setNegativeButton(getString(R.string.UNKNOWN_SOURCES_DIALOG_OPTION2), new android.content.DialogInterface.OnClickListener() {
				public void onClick(android.content.DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
    		
    		alert_dialog = alert_builder.create();
    		alert_builder = null;
		}catch(Exception e){DBG("GameInstaller","onCreate Error "+e.getMessage());}	
	#endif
	
		String data = SUtils.getOverriddenSetting(DATA_PATH + "qaTestingConfigs.txt", "OUTPUT_ACP_REVISION");
		#if GOOGLE_MARKET_DOWNLOAD
		if(data == null)
		{
			data = SUtils.getOverriddenSetting(marketPath + "/qaTestingConfigs.txt", "OUTPUT_ACP_REVISION");
		}
		#endif
		
	#if ACP_UT	
		if(true)
	#else
		if(data != null && data.equals("1"))
	#endif
		{
			DBG("GameInstaller","OUTPUT_ACP_REVISION=1 --- "+INSTALLER_REVISION+" ---");
			if(INSTALLER_REVISION != null && !INSTALLER_REVISION.equals("") && !INSTALLER_REVISION.equals("0"))
				showToast("ACP Revision", INSTALLER_REVISION);
			else
				showToast("ACP Revision", "Android Core Package not versioned");
			
		}
		#if USE_INSTALLER_SLIDESHOW
		createBgList();
		#endif
		
		LowProfileListener.ActivateImmersiveMode(this);
	}
	
	#if USE_INSTALLER_SLIDESHOW
	private void changeRandomBackground(int indexToDisplay)
    {
		#if !USE_INSTALLER_SLIDESHOW_TEXTS		
		((RelativeLayout)findViewById(R.id.data_downloader_main_layout)).setBackgroundColor(0xFF000000);
		#endif
		if(slideShowContainer != null)
		{
			slideShowContainer.setImageResource(slideShowImageIds.get(indexToDisplay));
			android.view.animation.Animation inAnim = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
			slideShowContainer.startAnimation(inAnim);
		}
		#if USE_INSTALLER_SLIDESHOW_TEXTS
		if(slideShowText != null)
			slideShowText.setText(slideShowTextsIds.get(indexToDisplay));
		#endif
    }
	
	private void createBgList()
	{
		slideShowImageIds = new ArrayList<Integer>();
		#if USE_INSTALLER_SLIDESHOW_TEXTS
		slideShowTextsIds = new ArrayList<Integer>();
		#endif

		for(int i = 1; i <= USE_INSTALLER_SLIDESHOW_IMAGES; i++)
		{
			slideShowImageIds.add(getResources().getIdentifier("data_downloader_slideshow_image_"+i, "drawable", getApplicationContext().getPackageName()));
		}
		
		#if USE_INSTALLER_SLIDESHOW_TEXTS
		for(int i = 1; i <= USE_INSTALLER_SLIDESHOW_IMAGES; i++)
		{
			slideShowTextsIds.add(getResources().getIdentifier("data_downloader_slideshow_text_00"+i, "string", getApplicationContext().getPackageName()));
		}
		#endif
		
	}
	#endif
	
	private final int TEXT_TYPE_PVRT 	= 0;
	private final int TEXT_TYPE_ATC 	= 1;
	private final int TEXT_TYPE_DXT 	= 2;
	private final int TEXT_TYPE_ETC 	= 3;
	
	Vector<Pair<String,Long>> pathAndSizes;
	Vector<Pair<String,Long>> sdAndSizes;
	
	private long checkAvailableSpace(String path)
	{
		try
		{
			File sdcard = new File (path+"/");
			boolean folderHasBeenCreated = false;
			if (!sdcard.exists())
			{
				folderHasBeenCreated = true;
				sdcard.mkdirs();
			}
			StatFs stat = new StatFs(path);
			long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
			mbAvailable = (int)(bytesAvailable / 1048576);
			if (folderHasBeenCreated || sdcard.list().length == 0)
			{
				if (sdcard.getAbsolutePath().endsWith("/files") || sdcard.getAbsolutePath().endsWith("/files/"))
				{
					sdcard.delete();
					sdcard = new File(sdcard.getAbsolutePath().substring(0, sdcard.getAbsolutePath().lastIndexOf("/files")));
					sdcard.delete();
				}
				else
				{
					sdcard.delete();
				}
			}
			setErrorPresent(ERROR_NO_SD, 1);
		}
		catch (Exception e)
		{
			return 0;
		}
		DBG("GameInstaller", "Path: " + path);
		return mbAvailable;
	}
	
	private Vector<String> getPaths()
	{
		DBG("GameInstaller", "setting the paths...");
		Vector<String> somePaths = new Vector<String>();
	#if !BUILD_FOR_FIRMWARE_1_6
		try
		{
			File external = getExternalFilesDir(null);
			if (external != null)
			{
				somePaths.add(external.getAbsolutePath());
				if (external.exists() || external.list().length == 0)
				{
					external.delete();
					external = new File(external.getAbsolutePath().substring(0, external.getAbsolutePath().lastIndexOf("/files")));
					external.delete();
				}
			}
		}
		catch (Exception e) { }
	#endif
		
		try
		{
			java.io.FileInputStream fstream = new java.io.FileInputStream("/proc/mounts");
			java.io.DataInputStream in = new java.io.DataInputStream(fstream);
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(in));
			
			String strLine;
			while ((strLine = br.readLine()) != null)
			{
				if ((strLine.contains("/mnt/sdcard") || strLine.contains("/storage/sdcard")) && !strLine.contains("android_secure"))
				{
					strLine = strLine.substring(strLine.indexOf(' ') + 1);
					somePaths.add(strLine.substring(0, strLine.indexOf(' ')) + "/Android/data/" + STR_APP_PACKAGE + "/files");
				}
			}
			br.close();
			in.close();
		}
		catch (Exception e) {}
		
		//somePaths.add(SD_FOLDER);
		
		return somePaths;
	}
	
	private String setSDFolder()
	{
		DBG("GameInstaller", "setSDFolder()");
		String result = "";
	#if !BUILD_FOR_FIRMWARE_1_6
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
		{
			try
			{
				Vector<String> somePaths = new Vector<String>();
			#if FORCE_EXTERNAL_SD_MEEP
				somePaths.add(EXTERNAL_SD_LOCATION + "/Android/data/" + STR_APP_PACKAGE + "/files");
				somePaths.add("/mnt/sdcard/Android/data/" + STR_APP_PACKAGE + "/files");
			#else
				//somePaths.add(SD_FOLDER);
				somePaths = getPaths();
			#endif
				
				for (String path : somePaths)
				{
					File file = new File(path);
					if (file.exists() && file.list().length > 0)
					{
						result = path;
						break;
					}
				}
				pathAndSizes = new Vector<Pair<String,Long>>();
				sdAndSizes = new Vector<Pair<String,Long>>();
				boolean test = true;
				for (String path : somePaths)
				{
					pathAndSizes.add(new Pair<String, Long>(path, checkAvailableSpace(path)));
					test = true;
					for (Pair<String,Long> sd : sdAndSizes)
					{
						if (sd.second.equals(checkAvailableSpace(path)))
						{
							test = false;
							break;
						}
					}
					if (test
					#if FORCE_EXTERNAL_SD_MEEP
						&& !(path.contains(EXTERNAL_SD_LOCATION) && (checkAvailableSpace(path) == 0))
					#endif
					)
						sdAndSizes.add(new Pair<String, Long>(path, checkAvailableSpace(path)));
				}
				#if FORCE_EXTERNAL_SD_MEEP
				int testExternalSD = 0;
				for (Pair<String,Long> sd : sdAndSizes)
				{
					if (sd.first.contains(EXTERNAL_SD_LOCATION) || sd.first.contains("/mnt/sdcard"))
					{
						testExternalSD++;
					}
				}
				
				pathAndSizes.remove(1);
				if (testExternalSD < 2)
				{
					pathAndSizes.clear();
					sdAndSizes.clear();
				}

				for (Pair<String,Long> sd : sdAndSizes)
				{
					if (sd.first.contains("/mnt/sdcard/"))
					{
						break;
					}
				}
			#endif
				if (result.equals(""))
					return "";
				else
					return result;
			} catch (Exception e) { DBG_EXCEPTION(e); }
			if (result.equals(""))
				return SD_FOLDER;
			else
				return result;
		}
		else
	#endif
		{
			return SD_FOLDER;
		}
	}
	
	private static String getSDFolder()
	{
		return sd_folder;
	}
	
	public String getDataServerUrl(int type)
	{
		String url 		= null;
		String dataURL 	= null;
		

		String sd_folder = SUtils.getPreferenceString("SDFolder", "", mPreferencesName);
		DBG("GameInstaller","qatestconfigs path: "+sd_folder);
		
		dataURL = SUtils.getOverriddenSetting(sd_folder + "/qaTestingConfigs.txt", "DATA_LINK");
		
		#if GOOGLE_MARKET_DOWNLOAD
		if(dataURL == null)
		{
			dataURL = SUtils.getOverriddenSetting(marketPath + "/qaTestingConfigs.txt", "DATA_LINK");
		}
		#endif

		if(dataURL != null)
		{
			DBG("GameInstaller","url overwritten");
			mIsOverwrittenLink = true;
			url = dataURL;
		}
		else
		{
			mIsOverwrittenLink = false;

			dataURL = SUtils.ReadFile(R.raw.data);
			int bg=-1, end=-1;
			
			bg  = dataURL.indexOf("DYNAMIC:")+8;
			end = dataURL.indexOf('\r', bg);
			if (end==-1)
				end = dataURL.length();
			url = dataURL.substring(bg, end);
			
			url += 
				"?model=" + Device.getPhoneModel() + 
				"&device=" + Device.getPhoneDevice() + 
				"&product=" + GL_PRODUCT_ID + 
				"&version=" + GAME_VERSION_NAME;
			
			url += "&portal=" + m_portalCode;
			
			url = url.replaceAll("\\s+", "%20");
			DBG("GameInstaller", "The build url: " + url);
		}		
		return url;
	}

	public String getRealSizeNeddedString(String title)
	{
		return title.replace("$", "" + ((mRequiredSize / 1048576) + 1));
	}

	public int hasSDCard()
	{
		DBG("GameInstaller", "hasSDCard()");
		String state = Environment.getExternalStorageState();
	#if FORCE_EXTERNAL_SD_MEEP
		if (sdAndSizes != null)
			for (Pair<String,Long> sd : sdAndSizes)
			{
				if (sd.first.contains(EXTERNAL_SD_LOCATION))
				{
					DBG("GameInstaller", "hasSDCard() return 0");
					return 0;
				}
			}
			DBG("GameInstaller", "hasSDCard() return 1");
		return 1;
	#else
		
		if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
			return 0;
		return 1;
	#endif
	}

	public int checkSDAvailable()
	{
		DBG("GameInstaller", "checkSDAvailable()");

		mbRequired = 0;
				
		StatFs stat = null;
		StatFs statObb = null;
		try
		{
			String sd_folder = SUtils.getPreferenceString("SDFolder", "", mPreferencesName);
			File path;
			if (sd_folder.contains(STR_APP_PACKAGE))
				path = new File(sd_folder.substring(0, sd_folder.indexOf(STR_APP_PACKAGE)));
			else if (sd_folder == SD_FOLDER)
				path = new File("/sdcard/");
			else
				path = new File(sd_folder);
				
			if (!path.exists())
				path.mkdirs();
				
			DBG("GameInstaller","the path: "+path);
			stat = new StatFs(path.getAbsolutePath());
			long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
			mbAvailable = (int)(bytesAvailable / 1048576);
			
			// Fix for removing SD card while installer initializes  (Sony reported as a problem)
			if (mbAvailable == 0 && !getErrorPresent(ERROR_NO_SD))
				setErrorPresent(ERROR_NO_SD, hasSDCard());
				
			#if GOOGLE_MARKET_DOWNLOAD
			if (marketPath.contains(STR_APP_PACKAGE))
				path = new File(marketPath.substring(0, marketPath.indexOf(STR_APP_PACKAGE)));
			else if (marketPath == SD_FOLDER)
				path = new File("/sdcard/");
			else
				path = new File(marketPath);
				
			if (!path.exists())
				path.mkdirs();
				
			DBG("GameInstaller","the path for obb: "+path);
			statObb = new StatFs(path.getAbsolutePath());
			bytesAvailable = (long)statObb.getBlockSize() * (long)statObb.getAvailableBlocks();
			mbAvailableObb = (int)(bytesAvailable / 1048576);		
			#endif

		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
			mbAvailable = 0;
		#if GOOGLE_MARKET_DOWNLOAD
			mbAvailableObb = 0;
		#endif
			//return 1;
		}		
		#if GOOGLE_MARKET_DOWNLOAD
		DBG("GameInstaller","first " + "Normal " + mbRequired + "/" + mbAvailable + " " + "; OBB " + mbRequiredObb + "/" + mbAvailableObb);
		mbRequiredObb = 0;
		#endif
		for(DownloadComponent component : mDownloadComponents)
		{
			#if GOOGLE_MARKET_DOWNLOAD
			//a little hack till I make time for this
			//2 years later, I found this awseome comment
			if(component.getName().startsWith("main") || component.getName().startsWith("patch"))
			{
				mbRequiredObb += component.getMBRequiredJustArhive();
			}
			else
			#endif
			mbRequired += component.getMBRequired();
		}
		if (mbRequired > 0)
			mbRequired += EXTRA_SPACE_ON_SD;
		#if GOOGLE_MARKET_DOWNLOAD	
		/*if (mbRequiredObb > 0)
			mbRequiredObb += EXTRA_SPACE_ON_SD; [ALI] - it's not important */ 
			
		DBG("GameInstaller","first " + "Normal " + mbRequired + "/" + mbAvailable + " " + "; OBB " + mbRequiredObb + "/" + mbAvailableObb);
		if(stat != null && statObb != null)
		{
			if(stat.getBlockSize() == statObb.getBlockSize() &&
				stat.getAvailableBlocks() == statObb.getAvailableBlocks() &&
				stat.getBlockCount() == statObb.getBlockCount()
				)
			{
				DBG("GameInstaller","same sd " + "Normal " + mbRequired + "/" + mbAvailable + " " + "; OBB " + mbRequiredObb + "/" + mbAvailableObb);
				mbRequired += mbRequiredObb;
				
				if(mbRequired >= mbAvailable)
					return 1;
			}
			else
			{
				DBG("GameInstaller","different sd " + "Normal " + mbRequired + "/" + mbAvailable + " " + "; OBB " + mbRequiredObb + "/" + mbAvailableObb);
				if((mbAvailable >0) && (mbRequired >= mbAvailable))
				{
					mbRequiredObb = 0;
					return 1;
				}
				else
				if(mbRequiredObb >= mbAvailableObb)
				{
					mbRequired = mbRequiredObb;
					return 1;
				}
			}
		}else
		{
			mbRequired = mbRequiredObb;
		}
		#else
		DBG("GameInstaller", "Required: " + mbRequired + " / Avaialble: " + mbAvailable);
		if(mbAvailable <= mbRequired)
			return 1;
		#endif
		return 0;
	}
	
	private void finishSuccess()
	{
		DBG("GameInstaller","finish success");
		setResult(GLConstants.ACTIVITY_RESULT_OK);
		finish();
	}
	
	private void finishFail()
	{
		DBG("GameInstaller","finish fail");
		setResult(GLConstants.ACTIVITY_RESULT_FAIL);
		finish();
	}
	
	@Override
	protected void onStart()
	{
		DBG("GameInstaller","onStart");
		DBG("GameInstaller","mInstallerStarted: " + mInstallerStarted);
		super.onStart();
		
		#if ADS_USE_FLURRY
			GLFlurry.onStartSession(this);
		#endif
#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#if RELEASE_VERSION
		boolean setting = SUtils.getPreferenceString(APP_PACKAGE.ApplicationSetUp.GA_DEBUG, APP_PACKAGE.ApplicationSetUp.FALSE, APP_PACKAGE.ApplicationSetUp.APP_TAG).equals(APP_PACKAGE.ApplicationSetUp.TRUE) ? true : false;
		DBG("GameInstaller", "GA been set to: " + setting);
		if(setting)
			com.google.android.gms.analytics.GoogleAnalytics.getInstance(this).getLogger().setLogLevel(com.google.android.gms.analytics.Logger.LogLevel.VERBOSE);
	#endif
		GoogleAnalyticsTracker.Init(this.getApplicationContext());
#endif
		if (!mInstallerStarted)
		{
			mInstallerStarted = true;
			mAssetManager = getAssets();
			new Thread(this).start();
		}
	}
	@Override
	protected void onDestroy()
	{
		DBG("GameInstaller","onDestroy");
		super.onDestroy();
		
		if(mClientHTTP != null)
		{
			mClientHTTP.close();
			mClientHTTP = null;
		}
		
		releaseWifiLock();
	#if !USE_SIMPLIFIED_INSTALLER
		if(mAirPlaneReceiver != null)
		{
			unregisterReceiver(mAirPlaneReceiver);
			mAirPlaneReceiver = null;
		}
		
		m_sInstance = null;
	#endif
		mAssetManager = null;
		
#if GOOGLE_DRM
		if(mChecker != null)
			mChecker.onDestroy();
		mChecker = null;
#endif
		if(!sbStarted)
		{
			finishFail();
		}
	}

	public static boolean bIsPaused = false;
	@Override
	protected void onPause() {
		DBG("GameInstaller","onPause");
		super.onPause();
		bIsPaused = true;
		cancelDialog();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		DBG("GameInstaller", "onActivityResult() " + "RequestCode: " + requestCode + " Result: " + (resultCode == GLConstants.ACTIVITY_RESULT_OK ? "ACTIVITY_RESULT_OK" : "ACTIVITY_RESULT_FAIL"));
	}
	
	@Override
	protected void onResume()
	{
		if(mCurrentLayout != -1)
			goToLayout(mCurrentLayoutId, mCurrentLayout);
		DBG("GameInstaller","onResume");
		super.onResume();
		bIsPaused = false;
		if((WIFI_MODE == WIFI_ONLY) && isWifiSettingsOpen && mState != GI_STATE_PIRACY_ERROR)
		{
			if(isWifiAlive())
			{
				setState(GI_STATE_DOWNLOAD_FILES);
			}
			isWifiSettingsOpen = false;
		}
		#if ORANGE_DRM
		if (mState == GI_STATE_PIRACY_ERROR && propertiesChanged)
		{
			setState(GI_STATE_CHECK_PIRACY);
			propertiesChanged = false;
		}
		#endif

		cancelNotification();
		
		formatter = new DecimalFormat("#,##0.00");//restart the formatter for the downloadstring
		
	}

	@Override
	protected void onStop()
	{
		DBG("GameInstaller","onStop");
		super.onStop();
	#if ADS_USE_FLURRY
		GLFlurry.onEndSession(this);
	#endif
	
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	}

	@Override
	protected void onRestart()
	{
		DBG("GameInstaller","onRestart");
		super.onRestart();
	}

	public boolean mhasFocus = false;
	public void onWindowFocusChanged(boolean hasFocus)
	{
		if(hasFocus)
			LowProfileListener.ActivateImmersiveMode(this);
		mhasFocus = hasFocus;
		s_isPauseGame = !hasFocus;
		mWaitingForWifiTimeElapsed = System.currentTimeMillis();
		DBG("GameInstaller", "onWindowFocusChanged with focus: "+mhasFocus);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		LowProfileListener.onKeyDown(this, keyCode);
		
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
			|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
			|| keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			return false;
		}	
		UpdateKeyController(keyCode);
		return true;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) && event.getRepeatCount() == 0)
		{
			backButtonAction();
	        return true;
	    }
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
			|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
			|| keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			return false;
		}
		return true;
	}
	
	
	private static 	long 		startTime 			= 0;
	private static 	int 		leftTapCount 			= 0;
	private	static 	int 		rightTapCount		= 0;
	private static 	int 		TAP_COUNT_MAX 		= 3;
	private static	int 		m_toastSize 		= 0;
	private static 	int 		m_delayTime 		= 1500;
	private static	int			m_toastExtra		= 20;
	private static 	Object 		m_objectToastLock 	= new Object();
	private static 	AlertDialog m_Dialog;
	private static 	String 		m_errorMessage 		= "";
	private	static 	String 		m_prevErrorMessage 	= "";
	private static 	boolean		statePressA			= false;
	private static 	boolean		statePressB			= false;
	private static 	boolean		statePressC			= false;
	private static 	long		pack_biggestFile	= -1;
	private static	int			pack_NoFiles		= -1;
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (m_toastSize == 0 && m_toastSize == 0)
		{
			m_toastSize = 25 + Integer.parseInt(getString(R.dimen.gi_tittle_bar_size_h).replaceAll("[\\D]+[^.]", "")) + m_toastExtra;
		}
		
		int action = event.getAction();
		
		android.view.Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		
		#if (USE_LOW_PROFILE_MENU == 0)
		#if TARGET_API_LEVEL_INT >= 19 //android 4.4
		if (Build.VERSION.SDK_INT >= 19)//android 4.4
		{
			if(!android.view.ViewConfiguration.get(this.getApplicationContext()).hasPermanentMenuKey())
			{
				//immersive mode is ON and we don't have Permanent menu keys, so we should get the real size of the screen
				try { 
					android.util.DisplayMetrics point = new android.util.DisplayMetrics();
					display.getRealMetrics(point);
					if(point.widthPixels > width) //real size should be greater than the size with immersive mode.
						width = point.widthPixels;
				} catch (Exception e) {
					width = display.getWidth();//exception will use the original width
				}
			}
		}
		#endif
		#endif
				
		if (event.getX() < m_toastSize && event.getY() < m_toastSize)
		{
			switch(action)
			{
			case MotionEvent.ACTION_UP:
				if (leftTapCount == 0 || System.currentTimeMillis() - startTime < m_delayTime)
				{
					leftTapCount++;
					startTime = System.currentTimeMillis();
					
					if (!statePressA && !statePressB && !statePressC)
						statePressA = true;
					else if (statePressA && statePressB && !statePressC)
						statePressC = true;
					else
					{
						statePressB = statePressC = false;
						statePressA = true;
					}
					
					if (leftTapCount == TAP_COUNT_MAX)
					{
						leftTapCount = 0;
				
						if ((m_prevErrorMessage != ""
								&& (mState == GI_STATE_PIRACY_ERROR 
									|| mState == GI_STATE_DOWNLOAD_FILES_ERROR
								//#if !USE_SIMPLIFIED_INSTALLER
									|| mState == GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION
								//#endif
									|| mState == GI_STATE_DEVICE_NOT_SUPPORTED)))
						{
							StringBuilder message = new StringBuilder();
							m_Dialog = new android.app.AlertDialog.Builder(this).setNeutralButton("Close", null).create();
							
							message.append("Configuration: " + getConfigurationCode());
							//message.append("\nInstallation Path: "+ DATA_PATH);
						
							message.append("\nDevice: " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE);
							message.append("\nGame: " + getString(R.string.app_name) + " " + VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_BUILD);
							message.append("\nError:" + m_prevErrorMessage);
						
							m_Dialog.setMessage(message.toString());
							m_Dialog.setTitle("Installer version " + INSTALLER_VERSION);
							m_Dialog.show();
							
							return true;
						}
					}
				}
				else
				{
					leftTapCount = 0;
				}
				break;
			}
		}
		else if (event.getX() < width && event.getX() > width - m_toastSize && event.getY() < m_toastSize)
		{
			leftTapCount = 0;
			switch(action)
			{
			case MotionEvent.ACTION_UP:
				if (System.currentTimeMillis() - startTime < m_delayTime)
				{
					if (statePressA && !statePressB)
					{
						startTime = System.currentTimeMillis();
						statePressB = true;
					}
					else if (statePressA && statePressB && statePressC)
					{
						statePressA = statePressB = statePressC = false;
						m_Dialog = new android.app.AlertDialog.Builder(this).setNeutralButton("Close", null).create();
						StringBuilder message = new StringBuilder();
						message.append("Configuration: " + getConfigurationCode());
						message.append("\nInstallation Path: "+ DATA_PATH);
					#if GOOGLE_MARKET_DOWNLOAD
						message.append("\nGEF Installation Path: "+ android.os.Environment.getExternalStorageDirectory() + "/Android/obb/" + STR_APP_PACKAGE);
					#endif
						message.append("\nBiggest file: " + (pack_biggestFile != -1 ? ((new DecimalFormat("#,##0.00")).format((pack_biggestFile>>10)/1024.0) + " MB") : ""));
						message.append("\nNumber of files: " + (pack_NoFiles != -1 ? pack_NoFiles : ""));
						m_Dialog.setMessage(message.toString());
						m_Dialog.setTitle("Installer version " + INSTALLER_VERSION);
						m_Dialog.show();
						return true;
					}
					else
					{
						statePressA = statePressB = statePressC = false;
					}
				}
				else
				{
					statePressA = statePressB = statePressC = false;
					rightTapCount = 0;
				}
			}
		}
		else
		{
			statePressA = statePressB = statePressC = false;
			rightTapCount = 0;
		}
		return false;
	}
	public void cancelDialog()
	{
		if (m_Dialog != null)
			m_Dialog.cancel();
	}
	public static void addErrorNumber(int number)
	{
		synchronized (m_objectToastLock)
		{
			if (!m_errorMessage.contains("" + number))
				m_errorMessage += " " + number;
		}
	}
	private static void clearErrorHistory()
	{
		m_prevErrorMessage = m_errorMessage;
		m_errorMessage = "";
	}
	private String getConfigurationCode()
	{
		String result = "";
		#if USE_UPDATE_VERSION
			result += "1";
		#else
			result += "0";
		#endif
		
		#if USE_MARKET_INSTALLER
			result += "1";
		#else
			result += "0";
		#endif
		
		#if GOOGLE_MARKET_DOWNLOAD
			result += "1";
		#else
			result += "0";
		#endif
		
		#if ORANGE_DRM
			result += "C";
		#elif USE_OPTUS_DRM
			result += "B";
		#elif USE_LGW_DRM
			result += "A";
		#elif TMOBILE_DRM
			result += "9";
		#elif USE_SAMSUNG_DRM
			result += "8";
		#elif USE_HEP_ANTIPIRACY
			result += "7";
		#elif GOOGLE_DRM
			result += "6";
		#elif USE_KT_DRM
			result += "5";
		#elif USE_LGU_DRM
			result += "4";
		#elif USE_SKT_DRM
			result += "3";
		#elif VERIZON_DRM
			result += "2";
		#elif GLOFT_DRM
			result += "1";
		#else
			result += "0";
		#endif
		
		switch (WIFI_MODE)
		{
		case WIFI_ONLY:
			result += "0";
			break;
		case WIFI_3G:
			result += "1";
			break;
		case WIFI_3G_ORANGE_IL:
			result += "2";
			break;
		default:
			result += "x";
			break;
			
		}
		
		result += "-" + PORTAL_CODE;
		result += "-" + GL_PRODUCT_ID;
		
		return result;
	}

	BroadcastReceiver mAirPlaneReceiver = null;
	BroadcastReceiver wifiChangeListener = null;
	public void initWifiListener()
	{
		m_bDownloadBackground = true;
		if(wifiChangeListener == null)
		{
			m_bSkipFirstScan	= true;
			changeWiFiListener((Context)this);
		}
	}

	public void removeWifiListener()
	{
		if(wifiChangeListener != null)
		{
			this.unregisterReceiver(wifiChangeListener);
			wifiChangeListener = null;
		}
	}

	public boolean m_bDownloadBackground = false;
	public boolean m_bScanningWifi = false;
	public boolean m_bSkipFirstScan	= true; //used to skip scan when minimize the app

	//method to listen for changes in the connection to a wifi access point
	public void changeWiFiListener(Context context)
	{
		wifiChangeListener = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				if(m_bSkipFirstScan)
				{
					m_bSkipFirstScan = false;
					return;
				}

				WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
				if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action))
				{
					int wifiState = wifi.getWifiState();
					if(wifiState == WifiManager.WIFI_STATE_ENABLED && mState != GI_STATE_FIND_WIFI)
					{
						setState(GI_STATE_FIND_WIFI);
					}
					else
					{
						DBG("GameInstaller","wifi scanning in progress ");
					}
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		context.registerReceiver(wifiChangeListener, filter);
	 }
//#endif

	private void setState(int state)
	{
		if (state == GI_STATE_PIRACY_ERROR ||
			state == GI_STATE_DOWNLOAD_FILES_ERROR ||
		//#if !USE_SIMPLIFIED_INSTALLER
			state == GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION ||
		//#endif
			state == GI_STATE_DEVICE_NOT_SUPPORTED)
				clearErrorHistory();
		
		switch(state)
		{
			case GI_STATE_PIRACY_ERROR:
			#if ORANGE_DRM
				goToLayout(R.layout.banana_main, LAYOUT_ORANGE_DRM);
			#else
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_LICENSE_INFO);
			#endif
			break;
			case GI_STATE_GAMELOFT_LOGO:
				mLogoTimeElapsed = System.currentTimeMillis();
				
				goToLayout(R.layout.gi_layout_logo, LAYOUT_LOGO);
				if (SERVER_URL.equals(""))
					setDataServerUrl();
				DBG("GameInstaller", "SERVER_URL=" + SERVER_URL);
			#if USE_GAME_TRACKING || USE_MARKET_INSTALLER || GOOGLE_STORE_V3
				#if USE_HEP_EXT_IGPINFO
					String igpInfo[] = APP_PACKAGE.IGPInfo.getIGPInfo();
					Tracking.onLaunchGame(Tracking.TYPE_LAUNCH_INSTALLER, "&IGPcode=" + igpInfo[0]); //Producer Request
				#else
					Tracking.onLaunchGame(Tracking.TYPE_LAUNCH_INSTALLER);
				#endif
			#endif
				WIFI_MODE = getWifiMode();
			#if USE_TRACKING_FEATURE_INSTALLER
				Tracker.launchInstallerTracker(WIFI_MODE, isWifiAlive());
			#if USE_GOOGLE_ANALYTICS_TRACKING
				GoogleAnalyticsTracker.trackEvent(
					//GoogleAnalyticsConstants.Category.Installer, 
					GoogleAnalyticsConstants.Category.LaunchInstaller, 
					WIFI_MODE == WIFI_ONLY ? GoogleAnalyticsConstants.Action.WifiOnly : GoogleAnalyticsConstants.Action.Wifi3G,
					isWifiAlive() ? GoogleAnalyticsConstants.Label.WifiON : GoogleAnalyticsConstants.Label.WifiOFF, null);
			#endif
			#endif
			break;
			case GI_STATE_SOLVE_ERROR:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_SD_SPACE_INFO);
			break;
		//#if !USE_SIMPLIFIED_INSTALLER
			case GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION:
				goToLayout(R.layout.data_downloader_progressbar_layout, LAYOUT_DOWNLOAD_FILES_NO_WIFI_QUESTION);
			break;
			case GI_STATE_FIND_WIFI:
				PackageManager packageManager = this.getApplicationContext().getPackageManager();
				
				if((WIFI_MODE == WIFI_3G || WIFI_MODE == WIFI_3G_ORANGE_IL) && (packageManager.checkPermission(Manifest.permission.CHANGE_WIFI_STATE,STR_APP_PACKAGE) == PackageManager.PERMISSION_GRANTED))
				{
					goToLayout(R.layout.data_downloader_progressbar_layout, LAYOUT_SEARCHING_FOR_WIFI);
				}
				else
				{
					try
					{
						//PackageManager packageManager = this.getApplicationContext().getPackageManager();
						Intent i = null;
						if(m_portalCode.equals("amazon"))
							i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
						else
							i = new Intent(Settings.ACTION_WIFI_SETTINGS);

						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
						isWifiSettingsOpen=true;
					}
					catch (Exception e) {
						DBG_EXCEPTION(e);
					}
				}

			break;
			case GI_STATE_CONFIRM_WAITING_FOR_WIFI:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_CONFIRM_WAITING_FOR_WIFI);
			break;
			case GI_STATE_WAITING_FOR_WIFI:
				initWifiListener();
				mWaitingForWifiTimeElapsed = System.currentTimeMillis();
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_WAITING_FOR_WIFI);
			break;
		//#endif
			case GI_STATE_UNZIP_DOWNLOADED_FILES:
				//goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_UNZIP_FILES);
				break;
			case GI_STATE_DOWNLOAD_FILES_QUESTION:
			#if !USE_SIMPLIFIED_INSTALLER
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_DOWNLOAD_FILES_QUESTION);
			#else

			#endif
			break;
		#if !USE_SIMPLIFIED_INSTALLER
			case GI_STATE_CONFIRM_3G:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_CONFIRM_3G);
			break;
			case GI_STATE_NO_DATA_CONNECTION_FOUND:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_NO_DATA_CONNECTION_FOUND);
			break;
		#endif
			case GI_STATE_DOWNLOAD_FILES_SUCCESFUL:
			#if !USE_SIMPLIFIED_INSTALLER && !UNZIP_DOWNLOADED_FILES
				goToLayout(R.layout.data_downloader_progressbar_layout, LAYOUT_SUCCESS_DOWNLOADED);
			#endif
				if(
				#if !USE_SIMPLIFIED_INSTALLER
				m_bDownloadBackground ||
				#endif
				 !mhasFocus)
				{
					if (!mhasFocus)
					{
						showNotification(GI_STATE_DOWNLOAD_FILES_SUCCESFUL, "", 0, 0);
						DBG("GameInstaller","GI_STATE_DOWNLOAD_FILES_SUCCESFUL notification");
					}
					else
					{
						Intent i = getIntent();
						try {
							i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
							i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							this.startActivity(i);
						}catch(Exception e){}
					}
				}
				if (this.getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
					saveVersion();
			break;
		//#endif
			case GI_STATE_DOWNLOAD_FILES_ERROR:
				if(mCurrentLayout != LAYOUT_DOWNLOAD_ANYTIME)
				{
					goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_DOWNLOAD_FILES_ERROR);
					new Thread()
					{
						public void run()
						{
						for(DownloadComponent component : mDownloadComponents)
						{
							component.StopDownload();
						}
						#if USE_GOOGLE_ANALYTICS_TRACKING
							// Stop download timing
							if (m_iRealRequiredSize > 0)
							GoogleAnalyticsTracker.stopTimingTracking(
								GoogleAnalyticsConstants.Category.Installer,
								System.currentTimeMillis(),
								GoogleAnalyticsConstants.Name.InstallerDownloadTiming,
								GoogleAnalyticsConstants.Label.DownloadTiming);
						#endif
						}
					}.start();

					if(
				#if !USE_SIMPLIFIED_INSTALLER
					m_bDownloadBackground ||
				#endif
					!mhasFocus)
					{
						if (!mhasFocus)
						{
							showNotification(GI_STATE_DOWNLOAD_FILES_ERROR, "", 0, 0);
						}
						else
						{
							Intent i = getIntent();
							try {
								i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
								i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
								i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								this.startActivity(i);
							}catch(Exception e){}
						}
					}
				}
			break;
		#if !USE_SIMPLIFIED_INSTALLER
			case GI_STATE_DOWNLOAD_ANYTIME:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_DOWNLOAD_ANYTIME);
			break;
		#endif
			case GI_STATE_NO_GP_ACCOUNT_DETECTED:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_NO_GP_ACCOUNT_DETECTED);
			break;
			case GI_STATE_FINALIZE:
				DBG("GameInstaller", "GI_STATE_FINALIZE");

				if (!checkUpdate && launchGame && HAS_AUTO_UPDATE)
				{
					DBG("GameInstaller", "GI_STATE_FINALIZE, setState(GI_STATE_INIT_UPDATE_VERSION)");
                    setState(GI_STATE_INIT_UPDATE_VERSION);
					return;
				}
        #if USE_JP_HD_SUBSCRIPTION                
                if (!checkJpHdSubcription && launchGame)
                {
                    DBG("GameInstaller", "GI_STATE_FINALIZE, USE_JP_HD_SUBSCRIPTION");
                    checkJpHdSubscription();
                    return;
                }
        #endif //USE_JP_HD_SUBSCRIPTION                                
			break;
			case GI_STATE_VERIFYING_CHECKSUM:
			#if !USE_SIMPLIFIED_INSTALLER
				goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_VERIFYING_FILES);
			#endif
			break;
			case GI_STATE_SEND_REQUEST:
				goToLayout(R.layout.data_downloader_progressbar_layout, LAYOUT_SEARCHING_FOR_NEW_VERSION);
			break;
			case GI_STATE_CONFIRM_UPDATE_VERSION:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_CONFIRM_UPDATE);
			break;
			case GI_STATE_RETRY_UPDATE_VERSION:
			#if !AUTO_UPDATE_HEP	
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_RETRY_UPDATE_VERSION);
			#endif
			break;
			case GI_STATE_NO_NEW_VERSION:
			break;
		#if USE_MARKET_INSTALLER || GOOGLE_MARKET_DOWNLOAD
			case GI_STATE_DEVICE_NOT_SUPPORTED:
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_MKP_DEVICE_NOT_SUPPORTED);
			break;
		#endif
        #if USE_JP_HD_SUBSCRIPTION
            case GI_STATE_CHECK_JP_HD_SUBCRIPTION:
                goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_JP_HD_SUBSCRIPTION);
            break;
            case GI_STATE_CHECK_JP_HD_SUBCRIPTION_NO_NETWORK:
                goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_JP_HD_SUBSCRIPTION_NO_NETWORK);
            break;
        #endif //USE_JP_HD_SUBSCRIPTION
			default:
			break;
		}
		mPrevState = mState;
		mState = state;
		DBG("gameInstaller","the substate at the ending: "+mSubstate);
		if(mSubstate != SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION)
			mSubstate = SUBSTATE_SHOW_LAYOUT;
#if TMOBILE_DRM
		mTMobileSubstate = SUBSTATE_INIT_TMOBILE;
#endif
	}

	/** The list that contains all the buttons of the current view */
	private ArrayList<Button>       buttonList;
	
	/** Index Manager to Controller Key pad **/
	private int IndexController			= -1;

	public String GetLayoutName(int x)
	{
		switch (x) {

		case LAYOUT_CONFIRM_3G:
			return "LAYOUT_CONFIRM_3G";
		case LAYOUT_CONFIRM_WAITING_FOR_WIFI:
			return "LAYOUT_CONFIRM_WAITING_FOR_WIFI";
		case LAYOUT_DOWNLOAD_ANYTIME:
			return "LAYOUT_DOWNLOAD_ANYTIME";
		case LAYOUT_NO_GP_ACCOUNT_DETECTED:
			return "LAYOUT_NO_GP_ACCOUNT_DETECTED";
		case LAYOUT_DOWNLOAD_FILES:
			return "LAYOUT_DOWNLOAD_FILES";
		case LAYOUT_DOWNLOAD_FILES_CANCEL_QUESTION:
			return "LAYOUT_DOWNLOAD_FILES_CANCEL_QUESTION";
		case LAYOUT_DOWNLOAD_FILES_ERROR:
			return "LAYOUT_DOWNLOAD_FILES_ERROR";
		case LAYOUT_DOWNLOAD_FILES_NO_WIFI_QUESTION:
			return "LAYOUT_DOWNLOAD_FILES_NO_WIFI_QUESTION";
		case LAYOUT_DOWNLOAD_FILES_QUESTION:
			return "LAYOUT_DOWNLOAD_FILES_QUESTION";
		case LAYOUT_LICENSE_INFO:
			return "LAYOUT_LICENSE_INFO";
		case LAYOUT_LOGO:
			return "LAYOUT_LOGO";
		case LAYOUT_NO_DATA_CONNECTION_FOUND:
			return "LAYOUT_NO_DATA_CONNECTION_FOUND";
		case LAYOUT_SD_SPACE_INFO:
			return "LAYOUT_SD_SPACE_INFO";
		case LAYOUT_VERIFYING_FILES:
			return "LAYOUT_VERIFYING_FILES";
		case LAYOUT_SEARCHING_FOR_WIFI:
			return "LAYOUT_SEARCHING_FOR_WIFI";
		case LAYOUT_SUCCESS_DOWNLOADED:
			return "LAYOUT_SUCCESS_DOWNLOADED";
		case LAYOUT_WAITING_FOR_WIFI:
			return "LAYOUT_WAITING_FOR_WIFI";
		case LAYOUT_BLACK:
			return "LAYOUT_MAIN";
		case LAYOUT_SEARCHING_FOR_NEW_VERSION:
			return "LAYOUT_SEARCHING_FOR_NEW_VERSION";
		case LAYOUT_CONFIRM_UPDATE:
			return "LAYOUT_CONFIRM_UPDATE";
		case LAYOUT_RETRY_UPDATE_VERSION:
			return "LAYOUT_RETRY_UPDATE_VERSION";
		case LAYOUT_CHECKING_REQUIRED_FILES:
			return "LAYOUT_CHECKING_REQUIRED_FILES";
	#if USE_MARKET_INSTALLER || GOOGLE_MARKET_DOWNLOAD
		case LAYOUT_MKP_DEVICE_NOT_SUPPORTED:
			return "LAYOUT_MKP_DEVICE_NOT_SUPPORTED";
	#endif
    #if USE_JP_HD_SUBSCRIPTION
        case LAYOUT_JP_HD_SUBSCRIPTION:
            return "LAYOUT_JP_HD_SUBSCRIPTION";   
        case LAYOUT_JP_HD_SUBSCRIPTION_NO_NETWORK:
            return "LAYOUT_JP_HD_SUBSCRIPTION_NO_NETWORK";             
    #endif //USE_JP_HD_SUBSCRIPTION
		case LAYOUT_UNZIP_FILES:
            return "LAYOUT_UNZIP_FILES";
		}
		return "Unknown Layout(" + x + ")";
	}

	private int mSavedLayoutToDisplay = 0;
	private int mCurrentLayout = -1;
	private int mCurrentLayoutId = 0;
	private int mPrevLayout = 0;
	public void goToLayout(final int layoutId, final int layoutState)
	{
		DBG("GameInstaller","Wrapper: goToLayout("+GetLayoutName(layoutState)+") " + layoutId);
		setButtonList(new ArrayList<Button>());
		getButtonList().clear();
		final Context mcontext = this.getApplicationContext();
		this.runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				try
				{
					mCurrentLayoutId = layoutId;
					setContentView(layoutId);
					if(layoutState != mCurrentLayout)
					{
					mPrevLayout = mCurrentLayout;
					mCurrentLayout = layoutState;
					}
					TextView text;
					Button button;
					switch (layoutState)
					{
						case LAYOUT_CHECKING_REQUIRED_FILES:
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_yes, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
						#if !USE_SIMPLIFIED_INSTALLER
							text.setText(getString(R.string.CHECKING_REQUIRED_FILES,mcontext));
						#else
							text.setVisibility(View.GONE);
						#endif
							break;
						case LAYOUT_DOWNLOAD_FILES_NO_WIFI_QUESTION:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.DOWNLOAD_FILES_NO_WIFI_QUESTION,mcontext));
							setBarVisibility(R.id.data_downloader_progress_bar, false);
							if(WIFI_MODE == WIFI_ONLY)
							{
								button = (Button) findViewById(R.id.data_downloader_no);
								button.setText(getString(R.string.LATER,mcontext));
								
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.ERROR_NO_WIFI_DETECTED_MINIMAL,mcontext));
							}
							else if (WIFI_MODE == WIFI_3G)
							{
								button = (Button) findViewById(R.id.data_downloader_no);
							#if USE_OPTUS_DRM
								button.setText(getString(R.string.USE_OPTUS,mcontext));
							#else
								button.setText(getString(R.string.USE_3G,mcontext));
							#endif

								button = (Button) findViewById(R.id.data_downloader_yes);
								button.setText(getString(R.string.USE_WIFI,mcontext));
							}
							else if (WIFI_MODE == WIFI_3G_ORANGE_IL)
							{
								button = (Button) findViewById(R.id.data_downloader_no);
								button.setText(getString(R.string.USE_3G_ORANGE_IL,mcontext));
								
								text = (TextView) findViewById(R.id.data_downloader_main_text);
							#if USE_OPTUS_DRM
								text.setText(getString(R.string.DOWNLOAD_FILES_NO_WIFI_QUESTION_OPTUS,mcontext));
							#else
								text.setText(getString(R.string.DOWNLOAD_FILES_NO_WIFI_QUESTION_3G_ORANGE_IL,mcontext));
							#endif

								button = (Button) findViewById(R.id.data_downloader_yes);
								button.setText(getString(R.string.USE_WIFI,mcontext));
							}
						}
						break;
						case LAYOUT_DOWNLOAD_FILES_QUESTION:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							int mb = (int)((m_iRealRequiredSize >> 20)+1);
							text.setText(getStringFormated(R.string.DOWNLOAD_FILES_QUESTION,null,""+mb));
							//text.setText(getString(R.string.DOWNLOAD_FAIL,mcontext));
						}
						break;
					#if ORANGE_DRM
						case LAYOUT_ORANGE_DRM:
							getButtonList().add((Button) findViewById(R.id.banana_button_quit));
							getButtonList().add((Button) findViewById(R.id.banana_button_settings));
							
							button = (Button) findViewById(R.id.banana_button_quit);
							button.setText(n.QUIT);
							
							View frame = (View) findViewById(R.id.banana_button_settings_frame);
							frame.setVisibility(View.GONE);
							
							switch(mOrangeLicense.getErrorCode())
							{
							case d.FIRST_WIFI:
							case d.NO_NETWORK_CONNECTION:
								button = (Button) findViewById(R.id.banana_button_settings);
								button.setText(n.SETTINGS);
								frame.setVisibility(View.VISIBLE);
								
								button = (Button) findViewById(R.id.banana_button_quit);
								button.setText(n.CANCEL);
								
								text = (TextView) findViewById(R.id.errorMessage);
								text.setText(n.NO_NETWORK_CONNECTION);
								break;
							case d.TIMEOUT:
								text = (TextView) findViewById(R.id.errorMessage);
								text.setText(n.TIMEOUT);
								break;
							case d.OTHER_ERROR:
								text = (TextView) findViewById(R.id.errorMessage);
								text.setText(n.OTHER_ERROR);
								break;
							case d.SERVER_INTERNAL_ERROR:
								text = (TextView) findViewById(R.id.errorMessage);
								text.setText(n.SERVER_INTERNAL_ERROR);
								break;
							case d.SERVER_BAD_PARAMETER:
								text = (TextView) findViewById(R.id.errorMessage);
								text.setText(n.SERVER_BAD_PARAMETER);
								break;
							case d.SERVER_BAD_CONTENT:
								text = (TextView) findViewById(R.id.errorMessage);
								text.setText(n.SERVER_BAD_CONTENT);
								break;
							case d.SERVER_USER_NOT_ALLOWED:
								text = (TextView) findViewById(R.id.errorMessage);
								text.setText(n.SERVER_USER_NOT_ALLOWED);
								break;
							}
							break;
					#endif
					#if TMOBILE_DRM
						case LAYOUT_CHECKING_LICENSE:
							setButtonVisibility(R.id.data_downloader_yes, false);
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.VALIDATING_LICENSE,mcontext));
							break;
					#endif
						case LAYOUT_LICENSE_INFO:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getStringFormated(miLicenseMessage,"{GAME_NAME}",getString(R.string.app_name)));
#if USE_HEP_ANTIPIRACY		
							if (miLicenseMessage == R.string.HEP_DEVICE_INVALID)
							{	
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getStringFormated(miLicenseMessage,"{DEVICE_NAMES}",HEP_SUPPORTED_DEVICE_NAMES));
							}
#endif //USE_HEP_ANTIPIRACY
						}
						break;
						case LAYOUT_SD_SPACE_INFO:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							
						//#if GOOGLE_MARKET_DOWNLOAD
						//	long size_required = (mbRequiredExtra != 0) ? mbRequiredExtra : mbRequiredObb;
						//	long size_available = (mbAvailableExtra != 0) ? mbAvailableExtra : mbAvailableObb;
						//#else
							long size_required = mbRequired;
							long size_available = mbAvailable;
						//#endif
							
							long total_size_required = getErrorPresent(ERROR_NO_SD) ? size_required : (size_required - size_available);
							int err_message = (boolean)(getErrorPresent(ERROR_NO_SD)) ? 
							#if FORCE_EXTERNAL_SD_MEEP
								R.string.MEEP_NO_SD
							#else
								R.string.NO_EXTERNAL_STORAGE_FOUND
							#endif
								: R.string.NO_ENOUGH_SPACE_AVAILABLE;
							
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getStringFormated(err_message,null,""+total_size_required));
						/*	if (getErrorPresent(ERROR_NO_SD))
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getStringFormated(R.string.NO_EXTERNAL_STORAGE_FOUND,null,""+mbRequired));
							}
							else
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getStringFormated(R.string.NO_ENOUGH_SPACE_AVAILABLE,null,""+(mbRequired - mbAvailable)));
							}
							*/
						}
						break;
						case LAYOUT_SEARCHING_FOR_NEW_VERSION:
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_yes, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
						#if !USE_SIMPLIFIED_INSTALLER
							text.setText(getString(R.string.STATE_SEND_REQUEST,mcontext));
						#else
							text.setVisibility(View.GONE);
						#endif
							break;
						case LAYOUT_SEARCHING_FOR_WIFI:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_cancel));
							setButtonVisibility(R.id.data_downloader_yes, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.WAIT_WHILE_ACTIVATING_WIFI,mcontext));
						}
						break;
						case LAYOUT_WAITING_FOR_WIFI:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							button = (Button) findViewById(R.id.data_downloader_no);
							button.setText(getString(R.string.CANCEL,mcontext));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							if (WIFI_MODE == WIFI_3G_ORANGE_IL)
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.ERROR_NO_WIFI_DETECTED_3G_ORANGE_IL,mcontext));
							}
							else
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.ERROR_NO_WIFI_DETECTED_2,mcontext));
							}
						}
						break;
						case LAYOUT_CONFIRM_WAITING_FOR_WIFI:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.RETRY_WIFI,mcontext));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							button = (Button) findViewById(R.id.data_downloader_no);
							button.setText(getString(R.string.CARRIER,mcontext));
							getButtonList().add((Button) findViewById(R.id.data_downloader_cancel));
							button = (Button) findViewById(R.id.data_downloader_cancel);
							button.setText(getString(R.string.CANCEL,mcontext));
							if(WIFI_MODE == WIFI_3G_ORANGE_IL)
							{
								button = (Button) findViewById(R.id.data_downloader_no);
								button.setText(getString(R.string.USE_3G_ORANGE_IL,mcontext));
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.ERROR_NO_WIFI_DETECTED_3G_ORANGE_IL,mcontext));
							}
							else
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.ERROR_NO_WIFI_DETECTED,mcontext));
							}
						}
						break;
						case LAYOUT_CONFIRM_3G:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							if(WIFI_MODE == WIFI_3G_ORANGE_IL)
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.DOWNLOAD_FILES_THROUGH_CARRIER_QUESTION_3G_ORANGE_IL,mcontext));
							}
							else
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.DOWNLOAD_FILES_THROUGH_CARRIER_QUESTION, mcontext) + "\n" + getString(R.string.DOWNLOAD_FILES_THROUGH_CARRIER_EXTRA, mcontext));
							}
						}
						break;
						case LAYOUT_NO_DATA_CONNECTION_FOUND:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.ERROR_NO_CARRIER_DATA_DETECTED,mcontext));
						}
						break;
						case LAYOUT_UNZIP_FILES:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_cancel));
							setButtonVisibility(R.id.data_downloader_yes, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							ProgressBar bar = (ProgressBar)findViewById(R.id.data_downloader_linear_progress_bar); 
							bar.setMax((int)(mTotalSize/1024+1));//using KBytes
							
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(R.string.EXTRACTING);
							text = (TextView) findViewById(R.id.data_downloader_progress_text);
							text.setVisibility(View.INVISIBLE);
						}break;
						case LAYOUT_DOWNLOAD_FILES:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_cancel));
							setButtonVisibility(R.id.data_downloader_yes, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							ProgressBar bar = (ProgressBar)findViewById(R.id.data_downloader_linear_progress_bar); 
							bar.setMax((int)(mTotalSize/1024+1));//using KBytes
							
							text = (TextView) findViewById(R.id.data_downloader_main_text);
						#if USE_SIMPLIFIED_INSTALLER
							text.setText(getString(R.string.DOWNLOADING_SIMPLE,mcontext));
						#else
							text.setText(getString(R.string.DONT_TURN_OFF_YOUR_PHONE,mcontext));
						#endif
							text = (TextView) findViewById(R.id.data_downloader_progress_text);
							text.setVisibility(View.INVISIBLE);

						#if USE_INSTALLER_SLIDESHOW
							#if !USE_INSTALLER_SLIDESHOW_TEXTS		
								((RelativeLayout)findViewById(R.id.data_downloader_main_layout)).setBackgroundColor(0xFF000000);
							#endif
							slideShowContainer = (ImageView)findViewById(R.id.data_downloader_main_layout_image);
							if(slideShowContainer!=null)
							{
								slideShowContainer.setVisibility(View.VISIBLE);
								slideShowContainer.setImageResource(slideShowImageIds.get(lastDisplayedIndex));
							}
							#if USE_INSTALLER_SLIDESHOW_TEXTS
							((android.widget.LinearLayout)findViewById(R.id.data_downloader_main_layout_image_background)).setBackgroundResource(R.drawable.data_downloader_slideshow_background);
							slideShowText = (android.widget.TextView)findViewById(R.id.data_downloader_main_layout_text);
							if(slideShowText!=null)
							{
								slideShowText.setTextColor(slideShowTextsColor);
								slideShowText.setText(slideShowTextsIds.get(lastDisplayedIndex));
							}
							#endif
						#endif
							
							if(WIFI_MODE == WIFI_ONLY)
							{
								text.setText(getString(R.string.DONT_TURN_OFF_YOUR_PHONE_WIFI,mcontext));
							}
							else
							{
								if (currentNetUsed == ConnectivityManager.TYPE_WIFI)
								{
									text = (TextView) findViewById(R.id.data_downloader_main_text);
									int mb = (int)((m_iRealRequiredSize >> 20)+1);
								#if !USE_SIMPLIFIED_INSTALLER
									text.setText(getStringFormated(R.string.DONT_TURN_OFF_YOUR_PHONE_WIFI,null,""+mb));
								#endif
								}
								else
								{
									text = (TextView) findViewById(R.id.data_downloader_main_text);
									//text.setText(getString(R.string.DONT_TURN_OFF_YOUR_PHONE_CARRIER_NETWORK, mcontext));
								}
							}
					
						}
						break;
						case LAYOUT_VERIFYING_FILES:
						{
							if(mState == GI_STATE_UNZIP_DOWNLOADED_FILES)
							{
								ProgressBar bar = (ProgressBar)findViewById(R.id.data_downloader_linear_progress_bar);
								bar.setMax((int)(mTotalSizeUnzipCheck+1));
								DBG("GameInstaller", "The progress bar for unzip: "+mTotalSizeUnzipCheck);
							}
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_yes, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.VERIFYING,mcontext));
						}
						break;
						case LAYOUT_SUCCESS_DOWNLOADED:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							setBarVisibility(R.id.data_downloader_progress_bar, false);
							if (mState == GI_STATE_NO_NEW_VERSION)
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.STATE_NO_NEW_VERSION,mcontext));
							}
							else
							{

							#if USE_GOOGLE_TV_BUILD
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.DOWNLOAD_SUCCESSFULLY_NO_TOUCH,mcontext));
							#else
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.DOWNLOAD_SUCCESSFULLY,mcontext));
							#endif
							}
						
						}
						break;
						case LAYOUT_DOWNLOAD_FILES_ERROR:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
						#if USE_SIMPLIFIED_INSTALLER
							if (!canReach(SERVER_URL) && mCurrentProgress == 0)
							{
								text.setText(getString(R.string.NO_INTERNET_CONNECTION_FOUND,mcontext));
								button = (Button) findViewById(R.id.data_downloader_yes);
								button.setText(getString(R.string.RETRY_CONNECTION,mcontext));
								button = (Button) findViewById(R.id.data_downloader_no);
								button.setText(getString(R.string.CANCEL,mcontext));
							}
							else
						#endif
								text.setText(getString(R.string.DOWNLOAD_FAIL,mcontext));
						}
						break;
						case LAYOUT_DOWNLOAD_FILES_CANCEL_QUESTION:
						case LAYOUT_UNZIP_FILES_CANCEL_QUESTION:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.DOWNLOAD_FILE_CANCEL_QUESTION,mcontext));
						}
						break;
						case LAYOUT_DOWNLOAD_ANYTIME:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							setButtonVisibility(R.id.data_downloader_no, false);
							setButtonVisibility(R.id.data_downloader_cancel, false);
						#if USE_GOOGLE_TV_BUILD
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.DOWNLOAD_ANYTIME_MESSAGE_MINIMAL,mcontext));
						#else
							if(WIFI_MODE == WIFI_ONLY || mPrevLayout == LAYOUT_UNZIP_FILES_CANCEL_QUESTION)
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.DOWNLOAD_ANYTIME_MESSAGE_MINIMAL,mcontext));
							}
							else if (WIFI_MODE == WIFI_3G_ORANGE_IL)
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.DOWNLOAD_ANYTIME_MESSAGE_3G_ORANGE_IL,mcontext));
							}
							else
							{
								text = (TextView) findViewById(R.id.data_downloader_main_text);
								text.setText(getString(R.string.DOWNLOAD_ANYTIME_MESSAGE,mcontext));
							}
						#endif
							if(layoutState != mCurrentLayout)
							cancelNotification();
						}
						break;
						case LAYOUT_NO_GP_ACCOUNT_DETECTED:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							setButtonVisibility(R.id.data_downloader_no, false);
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.NO_GP_ACCOUNT_DETECTED,mcontext));

						}
						break;
						case LAYOUT_CONFIRM_UPDATE:
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.STATE_CONFIRM_UPDATE,mcontext));
							
							if(mXPlayer != null && mXPlayer.update_is_mandatory)
								text.setText(getString(R.string.STATE_MANDATORY_UPDATE,mcontext));
							
						break;
						case LAYOUT_RETRY_UPDATE_VERSION:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.STATE_CONNECT_FAIL,mcontext));
						}
						break;
					#if USE_MARKET_INSTALLER || GOOGLE_MARKET_DOWNLOAD
						case LAYOUT_MKP_DEVICE_NOT_SUPPORTED:
						{
						#if USE_TRACKING_UNSUPPORTED_DEVICE
						if(layoutState != mCurrentLayout)
							Tracker.UnsupportedDeviceTracker();
						#endif
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							setButtonVisibility(R.id.data_downloader_no, false);
							setButtonVisibility(R.id.data_downloader_cancel, false);
						#if !USE_MKP_GOOGLE_WAY
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.MKP_DEVICE_NOT_SUPPORTED_CODE_DOWNLOAD));
						#else
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.MKP_DEVICE_NOT_SUPPORTED_GOOGLE_WAY));
						#endif
						}
						break;
					#endif
					#if USE_JP_HD_SUBSCRIPTION
						case LAYOUT_JP_HD_SUBSCRIPTION:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							getButtonList().add((Button) findViewById(R.id.data_downloader_no));
							button.setText(getString(R.string.NO,mcontext));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.JP_DH_SUBSCRIPTION));
						}
						break;
						case LAYOUT_JP_HD_SUBSCRIPTION_NO_NETWORK:
						{
							getButtonList().add((Button) findViewById(R.id.data_downloader_yes));
							button = (Button) findViewById(R.id.data_downloader_yes);
							button.setText(getString(R.string.OK,mcontext));
							setButtonVisibility(R.id.data_downloader_cancel, false);
							setButtonVisibility(R.id.data_downloader_no, false);
							text = (TextView) findViewById(R.id.data_downloader_main_text);
							text.setText(getString(R.string.JP_DH_SUBSCRIPTION_NO_NETWORK));
						}
						break;
					#endif //USE_JP_HD_SUBSCRIPTION
						default:
						break;
					}
					// Add buttons listeners
					try
					{
						ResetControllerIndex();
						for (Button but : getButtonList())
						{
							but.setOnClickListener(btnOnClickListener);
						}
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
					setState(GI_STATE_FINALIZE);
				}
			}
		});
	}

	/**
	 * Set the current button list
	 * @param buttonList
	 */
	public void setButtonList(ArrayList<Button> buttonList)
	{
		this.buttonList = buttonList;
	}

	/**
	 * returns the current button list
	 * @return
	 */
	public ArrayList<Button> getButtonList()
	{
		return buttonList;
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
			
			buttonsAction(buttonId);
		}

	};
	
	public void buttonsAction(int buttonId)
	{
		switch (mCurrentLayout)
		{
		
	#if !USE_SIMPLIFIED_INSTALLER
		case LAYOUT_CONFIRM_3G:
			if (buttonId == R.id.data_downloader_yes)
			{
				if(mWifiManager.isWifiEnabled() && (this.getApplicationContext().getPackageManager().checkPermission(Manifest.permission.CHANGE_WIFI_STATE,STR_APP_PACKAGE) == PackageManager.PERMISSION_GRANTED))
				{
					mWifiManager.setWifiEnabled(false);
					try { Thread.sleep(50); } catch(Exception e){};
					wasWifiDeActivatedByAPP = true;
				}
				
				if(isOtherNetAlive())
				{
					currentNetUsed = ConnectivityManager.TYPE_MOBILE;
					setState(GI_STATE_DOWNLOAD_FILES);
				}
				else
				{
					setState(GI_STATE_NO_DATA_CONNECTION_FOUND);
				}
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			}
			break;
	#endif
		case LAYOUT_CONFIRM_UPDATE:
			if (buttonId == R.id.data_downloader_yes)
			{
				//Force verify data after update
			#if AUTO_UPDATE_HEP
				try{
					canInstallFromOtherSources = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
				}catch(android.provider.Settings.SettingNotFoundException sne){
					canInstallFromOtherSources=true;
				}
				if (!canInstallFromOtherSources && alert_dialog!=null)
					alert_dialog.show();
				else
			#endif
				{
				saveVersion("0.0.1");
				setState(GI_STATE_UPDATE_GET_NEW_VERSION);
				}
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				if(mXPlayer != null && mXPlayer.update_is_mandatory)
				{
					launchGame = false;
					setState(GI_STATE_FINALIZE);
				}
				else
				{
					#if AUTO_UPDATE_HEP
					//do test again
					setErrorPresent(ERROR_NO_SD, hasSDCard()); //test for SD CARD
					setErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST, isValidResourceVersion()?1:0);
					setErrorPresent(ERROR_FILES_NOT_VALID, validateFiles()); //test for files needed
				
					if (getErrorPresent(ERROR_FILES_NOT_VALID) || getErrorPresent(ERROR_NEED_FILE_CHECKSUM_TEST))
					{
						setState(GI_STATE_UPDATE_FINISH);
					}
					else
					#endif
					setState(GI_STATE_FINALIZE);
				}
			}
			break;
	#if !USE_SIMPLIFIED_INSTALLER
		case LAYOUT_CONFIRM_WAITING_FOR_WIFI:
			if (buttonId == R.id.data_downloader_yes)
			{
				setState(GI_STATE_FIND_WIFI);
			}
			else if (buttonId == R.id.data_downloader_no)//carier network
			{
				if(mWifiManager.isWifiEnabled() && (this.getApplicationContext().getPackageManager().checkPermission(Manifest.permission.CHANGE_WIFI_STATE,STR_APP_PACKAGE) == PackageManager.PERMISSION_GRANTED))
				{
					mWifiManager.setWifiEnabled(false);
					try { Thread.sleep(50); } catch(Exception e){};
					wasWifiDeActivatedByAPP = true;
				}
				
				if(isOtherNetAlive())
				{
					currentNetUsed = ConnectivityManager.TYPE_MOBILE;
					setState(GI_STATE_DOWNLOAD_FILES);
				}
				else
				{
					setState(GI_STATE_NO_DATA_CONNECTION_FOUND);
				}
			}
			else if (buttonId == R.id.data_downloader_cancel)
			{
				if(mClientHTTP != null)
				{
					mClientHTTP.close();
					mClientHTTP = null;
				}
				launchGame = false;
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			}
			break;
		case LAYOUT_DOWNLOAD_ANYTIME:
			if (buttonId == R.id.data_downloader_yes)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
				finishFail();
			}
			break;
	#endif
		case LAYOUT_NO_GP_ACCOUNT_DETECTED:
			if (buttonId == R.id.data_downloader_yes)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
				finishFail();
			}
			break;	
		case LAYOUT_DOWNLOAD_FILES:
			if (buttonId == R.id.data_downloader_cancel)
			{
				if (mState != GI_STATE_DOWNLOAD_FILES)//Just confirm
					return;
				mPrevSubstate = mSubstate;
				mSubstate = SUBSTATE_DOWNLOAD_FILE_CANCEL_QUESTION;
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_DOWNLOAD_FILES_CANCEL_QUESTION);
			}
			break;
		case LAYOUT_UNZIP_FILES:
			if (buttonId == R.id.data_downloader_cancel)
			{
				goToLayout(R.layout.data_downloader_buttons_layout, LAYOUT_UNZIP_FILES_CANCEL_QUESTION);
			}
			break;
		case LAYOUT_UNZIP_FILES_CANCEL_QUESTION:
			if (buttonId == R.id.data_downloader_yes)
			{
				isUnzipingInterrupted = true;
			#if !USE_SIMPLIFIED_INSTALLER
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			#else
				setState(GI_STATE_FINALIZE);
				finishFail();
			#endif
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_UNZIP_FILES);
			}
			break;
		case LAYOUT_DOWNLOAD_FILES_CANCEL_QUESTION:
			if (buttonId == R.id.data_downloader_yes)
			{
				new Thread()
				{
					public void run()
					{
						for(DownloadComponent component : mDownloadComponents)
						{
							component.StopDownload();
						}
						#if USE_GOOGLE_ANALYTICS_TRACKING
						// Stop download timing
						if (m_iRealRequiredSize > 0)
						GoogleAnalyticsTracker.stopTimingTracking(
							GoogleAnalyticsConstants.Category.Installer,
							System.currentTimeMillis(),
							GoogleAnalyticsConstants.Name.InstallerDownloadTiming,
							GoogleAnalyticsConstants.Label.DownloadTiming);
						#endif
					}
				}.start();
			#if !USE_SIMPLIFIED_INSTALLER
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			#else
				launchGame = false;
				setState(GI_STATE_FINALIZE);
				finishFail();
			#endif
				try
				{
					launchGame = false;
					destroyObjects();
					
					//Delete the downloading file (not complete) to save space in sdcard
					PackFile packFile = mRequiredResources.get(currentDownloadFile);
					String fileName = DATA_PATH + packFile.getFolder().replace(".\\\\", "").replace(".\\", "").replace("\\", "/") + "/" + packFile.getZipName(); 
					
					File tmpFile = new File(fileName);
					if (tmpFile.exists())
					{
						tmpFile.delete();
					}
				}
				catch(Exception ex)
				{
					DBG_EXCEPTION(ex);
				}
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				if (mState != GI_STATE_DOWNLOAD_FILES)//Just confirm
					return;
				mSubstate = mPrevSubstate;
				#if USE_INSTALLER_SLIDESHOW && USE_INSTALLER_SLIDESHOW_TEXTS
				goToLayout(R.layout.data_downloader_linear_progressbar_layout_v2, LAYOUT_DOWNLOAD_FILES);
				#else
				goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_DOWNLOAD_FILES);
				#endif
			}
			break;
		case LAYOUT_DOWNLOAD_FILES_ERROR:
			if (buttonId == R.id.data_downloader_yes)
			{
				currentDownloadFile = 0;
				destroyObjects();
			//#if !USE_SIMPLIFIED_INSTALLER
				if(!isWifiAlive())
				{
					ERR_TOAST(ToastMessages.GameInstallerNoWifi.DownloadFilesErrorYes);
					if(hasDownloadLimit())
						setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
					else
					{
					//setState(GI_STATE_DOWNLOAD_FILES);
					if(WIFI_MODE == WIFI_3G)
						setState(GI_STATE_DOWNLOAD_FILES);
					else
						setState(GI_STATE_FIND_WIFI);				
					}
				}
				else
			//#endif
				{
					DBG("GameInstaller","go to download files");
					setState(GI_STATE_DOWNLOAD_FILES);
				}
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				if(mClientHTTP != null)
				{
					mClientHTTP.close();
					mClientHTTP = null;
				}
				launchGame = false;
			#if !USE_SIMPLIFIED_INSTALLER
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			#else
				setState(GI_STATE_FINALIZE);
			#endif
			}
			break;
	//#if !USE_SIMPLIFIED_INSTALLER
		case LAYOUT_DOWNLOAD_FILES_NO_WIFI_QUESTION:
			if (buttonId == R.id.data_downloader_yes)
			{
				setState(GI_STATE_FIND_WIFI);
			}
			else if (buttonId == R.id.data_downloader_no)//carrier network button
			{
				if(WIFI_MODE == WIFI_ONLY)
				{
				#if !USE_SIMPLIFIED_INSTALLER
					setState(GI_STATE_DOWNLOAD_ANYTIME);
				#else
					launchGame = false;
					setState(GI_STATE_FINALIZE);
				#endif
				}
				else
				{
				#if !USE_GOOGLE_TV_BUILD
					if(mWifiManager.isWifiEnabled() && (this.getApplicationContext().getPackageManager().checkPermission(Manifest.permission.CHANGE_WIFI_STATE,STR_APP_PACKAGE) == PackageManager.PERMISSION_GRANTED))
					{
						mWifiManager.setWifiEnabled(false);
						try { Thread.sleep(50); } catch(Exception e){};
						wasWifiDeActivatedByAPP = true;
					}
				#endif
					
					if(isOtherNetAlive())
					{
						currentNetUsed = ConnectivityManager.TYPE_MOBILE;
					#if USE_SIMPLIFIED_INSTALLER
						hasUserConfirmedDataUsage = true;
						if(hasDownloadLimit())
							setState(GI_STATE_DOWNLOAD_FILES);
						else
							setState(GI_STATE_DOWNLOAD_FILES_QUESTION);
					#else
						setState(GI_STATE_DOWNLOAD_FILES);
					#endif
					}
					else
					{
						#if !USE_SIMPLIFIED_INSTALLER
							setState(GI_STATE_NO_DATA_CONNECTION_FOUND);
						#else
							Intent i = null;
							i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(i);
							isWifiSettingsOpen=true;
						#endif
					}
				}
			}
			else if( buttonId == R.id.data_downloader_cancel)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
			}
			break;
			#if !USE_SIMPLIFIED_INSTALLER
		case LAYOUT_DOWNLOAD_FILES_QUESTION:
			if (buttonId == R.id.data_downloader_yes)
			{
			#if !USE_GOOGLE_TV_BUILD
				if(currentNetUsed != ConnectivityManager.TYPE_MOBILE)
				{
					currentNetUsed = ConnectivityManager.TYPE_WIFI;
					if(!isWifiAlive())
					{
						ERR_TOAST(ToastMessages.GameInstallerNoWifi.DownloadFilesQuestionYes);
						if(hasDownloadLimit())
							setState(GI_STATE_DOWNLOAD_FILES_NO_WIFI_QUESTION);
						else
							setState(GI_STATE_DOWNLOAD_FILES);
						return;
					}
				}
				else
				{

					//Wifi enable after checking using 3G, should show error message
					if(isWifiAlive())
					{
						currentNetUsed = ConnectivityManager.TYPE_WIFI;
						ERR_TOAST(ToastMessages.GameInstallerSingleFileDownload.WifiEnabledWhenUsing3G);
						setState(GI_STATE_DOWNLOAD_FILES_ERROR);
						return;
					}
				
				}
			#else
				DBG("GameInstaller", "Click yes in LAYOUT_DOWNLOAD_FILES_QUESTION:" + currentNetUsed);
				if(isEthernetAlive())
				{
					currentNetUsed = ConnectivityManager.TYPE_MOBILE;
					// setState(GI_STATE_DOWNLOAD_FILES);
				} else if (isWifiAlive())
				{
					currentNetUsed = ConnectivityManager.TYPE_WIFI;
					// setState(GI_STATE_DOWNLOAD_FILES);
				} else 
				{
					// no connection detected
					DBG("GameInstaller", "No internet connection found, send to wait for wifi or Ethernet connection");
					setState(GI_STATE_DOWNLOAD_FILES_ERROR); 
					return;
				}
			#endif
				
			#if USE_TRACKING_FEATURE_INSTALLER
				if(!mbStartDownloaded)
				{
					Tracker.downloadStartTracker(WIFI_MODE, currentNetUsed == ConnectivityManager.TYPE_MOBILE);
				#if USE_GOOGLE_ANALYTICS_TRACKING
					GoogleAnalyticsTracker.trackEvent(
						//GoogleAnalyticsConstants.Category.Installer, 
						GoogleAnalyticsConstants.Category.StartDownload, 
						WIFI_MODE == WIFI_ONLY ? GoogleAnalyticsConstants.Action.WifiOnly : GoogleAnalyticsConstants.Action.Wifi3G,
						currentNetUsed == ConnectivityManager.TYPE_MOBILE ? GoogleAnalyticsConstants.Label.Through3G : GoogleAnalyticsConstants.Label.ThroughWifi, null);
				#endif
					mbStartDownloaded = true;
				}
			#endif
				
				// Create .nomedia and data folder
				createNoMedia(DATA_PATH);
				
				mStatus = STATUS_NO_ERROR;
				setState(GI_STATE_DOWNLOAD_FILES);
				#if USE_INSTALLER_SLIDESHOW && USE_INSTALLER_SLIDESHOW_TEXTS
				goToLayout(R.layout.data_downloader_linear_progressbar_layout_v2, LAYOUT_DOWNLOAD_FILES);
				#else				
				goToLayout(R.layout.data_downloader_linear_progressbar_layout, LAYOUT_DOWNLOAD_FILES);
				#endif
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			}
			break;
	#endif
	#if USE_JP_HD_SUBSCRIPTION
		case LAYOUT_JP_HD_SUBSCRIPTION:
			if (buttonId == R.id.data_downloader_yes)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
			}
			break;
		case LAYOUT_JP_HD_SUBSCRIPTION_NO_NETWORK:
			if (buttonId == R.id.data_downloader_yes)
			{
				AdsWebViewClient av = new AdsWebViewClient();
				String url = "http://wapshop.gameloft.com/ezweb/android/monthly_regist.php";
				DBG("GameInstaller", "HD_SUBCRIPTION: " + url);
				av.OpenBrowser(url);
				
				launchGame = false;
				setState(GI_STATE_FINALIZE);
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
			}
			break;
	#endif //USE_JP_HD_SUBSCRIPTION
		case LAYOUT_LICENSE_INFO:
			if (buttonId == R.id.data_downloader_yes)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
				finishFail();
			}
			break;
		//case LAYOUT_LOGO:
	#if USE_MARKET_INSTALLER
		case LAYOUT_MKP_DEVICE_NOT_SUPPORTED:
			if (buttonId == R.id.data_downloader_yes)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
			}
			break;
	#endif
	#if !USE_SIMPLIFIED_INSTALLER
		case LAYOUT_NO_DATA_CONNECTION_FOUND:
			if (buttonId == R.id.data_downloader_yes)
			{
				setState(GI_STATE_FIND_WIFI);
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				if(mClientHTTP != null)
				{
					mClientHTTP.close();
					mClientHTTP = null;
				}
				launchGame = false;
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			}
			break;
	#endif
		case LAYOUT_RETRY_UPDATE_VERSION:
			if (buttonId == R.id.data_downloader_yes)
			{
				setState(GI_STATE_SEND_REQUEST);
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				mStatus = STATUS_NO_ERROR;
				setState(GI_STATE_FINALIZE);
			}
			break;
		case LAYOUT_SD_SPACE_INFO:
			if (buttonId == R.id.data_downloader_yes)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
				finishFail();
			}
			break;
	#if !USE_SIMPLIFIED_INSTALLER
		//case LAYOUT_SEARCHING_FOR_NEW_VERSION:
		case LAYOUT_SEARCHING_FOR_WIFI:
			if (buttonId == R.id.data_downloader_cancel)
			{
				if(mClientHTTP != null)
				{
					mClientHTTP.close();
					mClientHTTP = null;
				}
				launchGame = false;
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			}
			break;
		case LAYOUT_SUCCESS_DOWNLOADED:
			if (buttonId == R.id.data_downloader_yes)
			{
				launchGame = true;
				mStatus = STATUS_NO_ERROR;
				setState(GI_STATE_FINALIZE);
			}
			break;
		//case LAYOUT_VERIFYING_FILES:
		case LAYOUT_WAITING_FOR_WIFI:
			if (buttonId == R.id.data_downloader_yes)
			{
				moveTaskToBack(true);
			}
			else if (buttonId == R.id.data_downloader_no)
			{
				m_bDownloadBackground = false;
				launchGame = false;
				setState(GI_STATE_DOWNLOAD_ANYTIME);
			}
			break;
		//case LAYOUT_BLACK:
	#endif
	#if ORANGE_DRM
		case LAYOUT_ORANGE_DRM:
			if (buttonId == R.id.banana_button_settings)
			{
				Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				startActivity(i);
				setContentView(R.layout.gi_main);
				propertiesChanged = true;
			}
			else if(buttonId == R.id.banana_button_quit)
			{
				launchGame = false;
				setState(GI_STATE_FINALIZE);
				finishFail();
			}
			break;
	#endif
		}
	}
	
	private void backButtonAction()
	{
		switch (mCurrentLayout) 
		{
	#if !USE_SIMPLIFIED_INSTALLER
		case LAYOUT_CONFIRM_3G: // Action similar to pressing NO
			buttonsAction(R.id.data_downloader_no);
			break;
	#endif
		case LAYOUT_CONFIRM_UPDATE:
			buttonsAction(R.id.data_downloader_no); // Action similar to pressing NO
			break;
	#if !USE_SIMPLIFIED_INSTALLER
		case LAYOUT_CONFIRM_WAITING_FOR_WIFI:// Action similar to pressing CANCEL
			buttonsAction(R.id.data_downloader_cancel);
			break;
		case LAYOUT_DOWNLOAD_ANYTIME: // Action similar to pressing OK
			buttonsAction(R.id.data_downloader_yes);
			break;
	#endif
		case LAYOUT_NO_GP_ACCOUNT_DETECTED: // Action similar to pressing OK
			buttonsAction(R.id.data_downloader_yes);
			break;	
	
		case LAYOUT_UNZIP_FILES:
			buttonsAction(R.id.data_downloader_cancel);
			break;
		case LAYOUT_DOWNLOAD_FILES:
			buttonsAction(R.id.data_downloader_cancel);
			break;
		case LAYOUT_DOWNLOAD_FILES_CANCEL_QUESTION: // Action similar to pressing NO
			buttonsAction(R.id.data_downloader_no);
			break;
		case LAYOUT_DOWNLOAD_FILES_ERROR: // Action similar to pressing NO
			buttonsAction(R.id.data_downloader_no);
			break;
		case LAYOUT_DOWNLOAD_FILES_NO_WIFI_QUESTION: // Only for WIFI - similar to LATER
			#if !USE_SIMPLIFIED_INSTALLER
			if (WIFI_MODE == WIFI_ONLY)
				buttonsAction(R.id.data_downloader_no);
			#else
			buttonsAction(R.id.data_downloader_cancel);
			#endif
			break;
		case LAYOUT_DOWNLOAD_FILES_QUESTION: // Action similar to pressing NO
			buttonsAction(R.id.data_downloader_no);
			break;
	#if USE_JP_HD_SUBSCRIPTION
		case LAYOUT_JP_HD_SUBSCRIPTION: // Action similar to pressing NO
			buttonsAction(R.id.data_downloader_no);
			break;
		case LAYOUT_JP_HD_SUBSCRIPTION_NO_NETWORK: // Action similar to pressing OK
			buttonsAction(R.id.data_downloader_yes);
			break;
	#endif
		case LAYOUT_LICENSE_INFO: // Action similar to pressing OK
			buttonsAction(R.id.data_downloader_yes);
			break;
	#if USE_MARKET_INSTALLER || GOOGLE_MARKET_DOWNLOAD
		case LAYOUT_MKP_DEVICE_NOT_SUPPORTED: // Action similar to pressing OK
			buttonsAction(R.id.data_downloader_yes);
			break;
	#endif
		case LAYOUT_NO_DATA_CONNECTION_FOUND: // Action similar to pressing NO
			buttonsAction(R.id.data_downloader_no);
			break;
		case LAYOUT_RETRY_UPDATE_VERSION: // Action similar to pressing NO
			buttonsAction(R.id.data_downloader_no);
			break;
		case LAYOUT_SD_SPACE_INFO: // Action similar to pressing OK
			buttonsAction(R.id.data_downloader_yes);
			break;
		case LAYOUT_SEARCHING_FOR_NEW_VERSION:
			break;
		case LAYOUT_SEARCHING_FOR_WIFI: // Action similar to pressing CANCEL
			buttonsAction(R.id.data_downloader_cancel);
			break;
		case LAYOUT_SUCCESS_DOWNLOADED: // Action similar to pressing OK
			buttonsAction(R.id.data_downloader_yes);
			break;
		case LAYOUT_WAITING_FOR_WIFI: // Action similar to pressing CANCEL
			buttonsAction(R.id.data_downloader_no);
			break;
	#if ORANGE_DRM
		case LAYOUT_ORANGE_DRM: // Action similar to pressing CANCEL
			buttonsAction(R.id.banana_button_quit);
			break;
	#endif
		case LAYOUT_CHECKING_REQUIRED_FILES:
		case LAYOUT_BLACK:
		case LAYOUT_LOGO:
		case LAYOUT_VERIFYING_FILES:
			break;
		}
	}

	public String getStringFormated(int index, String rx, String rp)
	{
		String result = getString(index);
		if (rx == null)
		{
			rx = "{SIZE}";
		}
		result = result.replace(rx, rp);
		return result;
	}

	Notification notif;
	PendingIntent contentIntentNotification;
	private long notificationLastUpdate = 0;
	private long notificationUpdateDelay = 1000;
	private Handler handler;
		
	private void initializeNotification()
	{
		if (nm == null)
			nm = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
		Intent game = new Intent(this, APP_PACKAGE.MainActivity.class);
		game.setAction(Intent.ACTION_MAIN);
		game.addCategory(Intent.CATEGORY_LAUNCHER);
		
		contentIntentNotification = PendingIntent.getActivity(this, 0, game, 0);
		
		notif = new Notification();
		notif.icon = R.drawable.installer_icon;
		notif.when = System.currentTimeMillis();
		notif.contentIntent = contentIntentNotification;
	}
	void showToast(String text)
	{
		showToast(null, text);
	}
	void showToast(final String title, final String text)
	{
		handler.post(new Runnable() 
		{
			public void run()
			{
				android.view.LayoutInflater inflater = getLayoutInflater();
				android.view.View layout = inflater.inflate(R.layout.gi_layout_download_toast_message, (android.view.ViewGroup) findViewById(R.id.toast_layout));
				android.widget.TextView textView = (android.widget.TextView) layout.findViewById(R.id.data_downloader_toast_message);
				textView.setText(text);
				if (title != null)
				{
					textView = (android.widget.TextView) layout.findViewById(R.id.data_downloader_toast_title);
					textView.setText(title);
				}
				android.widget.Toast toast = new android.widget.Toast(getApplicationContext());
				
				toast.setGravity(android.view.Gravity.CENTER, 0, 0);
				toast.setDuration(android.widget.Toast.LENGTH_LONG);
				toast.setView(layout);
				toast.show();
			}
		});
	}
	void showNotification(int state, String text, int max, int value)
	{
		try
		{
			if (state !=  GI_STATE_DOWNLOAD_FILES || System.currentTimeMillis() - notificationLastUpdate > notificationUpdateDelay)
			{
				if (notif == null)
					initializeNotification();
				
				switch(state)
				{
					case GI_STATE_DOWNLOAD_FILES_ERROR:
						notif.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.gi_notification_message);
						notif.contentView.setTextViewText(R.id.txDownloading_notif, getString(R.string.NOTIFY_MESSAGE_FAIL));
						notif.flags = Notification.FLAG_AUTO_CANCEL;
						showToast(getString(R.string.NOTIFY_MESSAGE_FAIL));
						break;
				
					case GI_STATE_DOWNLOAD_FILES_SUCCESFUL:
						notif.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.gi_notification_message);
						notif.contentView.setTextViewText(R.id.txDownloading_notif, getString(R.string.NOTIFY_MESSAGE_OK));
						notif.flags = Notification.FLAG_AUTO_CANCEL;
						showToast(getString(R.string.NOTIFY_MESSAGE_OK));
						break;
				
					case GI_STATE_DOWNLOAD_FILES:
						notif.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.gi_notification_progress_bar);
						notif.contentView.setProgressBar(R.id.notification_progress, max, value, false);
						notif.contentView.setTextViewText(R.id.txDownloading_notif, text);
						notif.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
						break;
				}
				
				notif.contentView.setImageViewResource(R.id.ImageView_notif, R.drawable.icon);
				notif.contentView.setTextViewText(R.id.title_notif,  getString(R.string.app_name));
				
				// Send the notification
				
				notificationLastUpdate = System.currentTimeMillis();
				nm.notify(NOTIFICATION_ID, notif);
			}
		}
		catch(Exception e) {}
	}

	void cancelNotification()
	{
		nm = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

#if USE_JP_HD_SUBSCRIPTION    
    void checkJpHdSubscription()
    {
        DBG("GameInstaller", "GI_STATE_FINALIZE, setState(GI_STATE_CHECK_JP_HD_SUBCRIPTION)");
        checkJpHdSubcription = true;   
        String ticket = SUtils.ReadFile(R.raw.serialkey);
        String imei = Device.getDeviceId();
        String requestJpHDSubcription = null;

        ticket = Encrypter.crypt(ticket); 
        ticket = "ticket=" + ticket;
        imei = Encrypter.crypt(imei);
        imei = "imei=" + imei;
                
        requestJpHDSubcription = ticket + "&" + imei+"&enc=1";

        //String requestJpHDSubcription = "";
        DBG("GameInstaller",requestJpHDSubcription);
       
        mDevice = new Device();
		mXPlayer = new XPlayer(mDevice);
        mXPlayer.sendTicketAndImeiRequest(requestJpHDSubcription);
          
        while (!mXPlayer.handleTicketAndImeiRequest())
        {
            try {
                Thread.sleep(50);
            } catch (Exception exc) {}
            DBG("GameInstaller", "[Validating FULL VERSION GAME]Waiting for response...");
        }
        
        DBG("GameInstaller","mXPlayer.str_response = " + mXPlayer.str_response);
        if (mXPlayer.str_response != null)
        {
            if (mXPlayer.str_response.equals("OK"))
            {
                DBG("GameInstaller", "GI_STATE_CHECK_JP_HD_SUBCRIPTION is OK");
                setState(GI_STATE_FINALIZE);
            }
            else
            {
                DBG("GameInstaller", "GI_STATE_CHECK_JP_HD_SUBCRIPTION is NOK");
                setState(GI_STATE_CHECK_JP_HD_SUBCRIPTION);
            }
        }
        else
        {
            DBG("GameInstaller", "GI_STATE_CHECK_JP_HD_SUBCRIPTION is no Network");
            setState(GI_STATE_CHECK_JP_HD_SUBCRIPTION_NO_NETWORK);
        }
    }
#endif //USE_JP_HD_SUBSCRIPTION
#if !USE_SIMPLIFIED_INSTALLER
	/// Check airplane mode
	public boolean isAirplaneModeOn( Context currentContext )
	{		
		boolean isAirplaneOn = Settings.System.getInt(currentContext.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		DBG("GameInstaller", "isAirplaneModeOn = " + isAirplaneOn);
		return isAirplaneOn;
	}
#endif
	public boolean SaveDateLastUpdate(String path)
	{
		long seconds = System.currentTimeMillis() / 1000;
		/*if (sd_folder == "")
		{
			sd_folder = pathAndSizes.elementAt(0).first;
			DATA_PATH = sd_folder + "/";
			SUtils.setPreference("SDFolder", sd_folder, mPreferencesName);
		}*///george
		String data = String.format("%d/", seconds);
		DBG("GameInstaller", "SaveDateLastUpdate data = "+data);
		SUtils.WriteFile(path + "InsTime", data);
		return true;
	}

	public boolean CheckDayToUpdateNewVersion()
	{
		DBG("GameInstaller", "CheckDayToUpdateNewVersion()");
		File f = new File(DATA_PATH + "InsTime");
		if(!f.exists())
		{
			DBG("GameInstaller", "CheckDayToUpdateNewVersion(), SaveDateLastUpdate");
			SaveDateLastUpdate(DATA_PATH);
			return false;
		}

		String data = SUtils.ReadFile(DATA_PATH + "InsTime");
		DBG("GameInstaller", "CheckDayToUpdateNewVersion(), data = " + data + " " + (60 * 60 * 24 * DATE_TO_UPDATE_NEW_VERSION));
		
		String temp[] = data.split("/");
		if(temp[0].equals(""))
		{
			DBG("GameInstaller", "CheckDayToUpdateNewVersion(), error reading the file. Save date again");
			SaveDateLastUpdate(DATA_PATH);
			return false;
		}
		long seconds = Long.parseLong(temp[0]);
		long curSeconds = System.currentTimeMillis() / 1000;

		if(temp.length > 2) //old verion of auto update (dd/mm/yyyy)
		{
			DBG("GameInstaller", "CheckDayToUpdateNewVersion(),  SaveDateLastUpdate(); false");
			SaveDateLastUpdate(DATA_PATH);
			return false;
		}
		else if ((curSeconds - seconds) >= (60 * 60 * 24 * DATE_TO_UPDATE_NEW_VERSION))
		{
			DBG("GameInstaller", "CheckDayToUpdateNewVersion(), SaveDateLastUpdate(); true");
			SaveDateLastUpdate(DATA_PATH);
			return true;
		}
		else if (curSeconds - seconds < 0)
		{
			DBG("GameInstaller", "CheckDayToUpdateNewVersion(),  SaveDateLastUpdate(); false 2");
			SaveDateLastUpdate(DATA_PATH);
			return false;
		}
		return false;
	}

	public String GetUrl(String str_response)
	{
		String kq = "";
		int i = str_response.indexOf("http");
		kq = str_response.substring(i);
		return kq;
	}

	public String GetCurrentVersion(String str_response)
	{
		String kq = "3.1.4";
		try
		{
			String tmp[] = { "" };
			if (!sUpdateAPK)
				tmp = str_response.split("SERVER_URL");
		#if !USE_MARKET_INSTALLER && !GOOGLE_MARKET_DOWNLOAD
			if (sUpdateAPK)
				tmp = str_response.split("DOWNLOAD_URL");
		#endif
			String tmp2[] = tmp[0].split("VERSION_AVAILABLE");
			kq = tmp2[1].substring(2, tmp2[1].length()-2);
		} catch(Exception ex){}
		return kq;
	}

	public boolean isEnoughInternalSpace()
	{
		File data = Environment.getDataDirectory();
		StatFs stat = new StatFs(data.getPath());
		long mbAvailable = (stat.getBlockSize() * stat.getAvailableBlocks()) / (long)1048576;
		DBG("GameInstaller","********MB AVAILABLE: " + mbAvailable);
		return true;
	}

	public void readInstallerResource()
	{
		DBG("GameInstaller","MANUFACTURER: "+Build.MANUFACTURER);
		DBG("GameInstaller","MODEL: "+Build.MODEL);
		DBG("GameInstaller","PRODUCT: "+Build.PRODUCT);
		DBG("GameInstaller","CPU_ABI: "+Build.CPU_ABI);
		DBG("GameInstaller","TAGS: "+Build.TAGS);
		DBG("GameInstaller","CARRIER: "+Device.getNetworkOperatorName());
		deviceName = Build.MANUFACTURER+Build.MODEL;
		
		pack_biggestFile = -1;
		pack_NoFiles = 0;
		
		for(DownloadComponent component : mDownloadComponents)
		{
			component.readResources(this);
			pack_NoFiles += component.getFilesNo();
			if(pack_biggestFile < component.getBiggestFileSize())
				pack_biggestFile = component.getBiggestFileSize();
		}
	}
#if USE_HEP_PACKINFO
	public void copyInfoFromSDtoDevice()
	{
		try
		{
			File inInfoPack = new File(INFO_FILE_NAME);
			if(!inInfoPack.exists())
			{
				SUtils.WriteFile(INFO_FILE_NAME, SUtils.ReadFileByte(R.raw.pack));
			}
			inInfoPack = null;
		} catch(Exception e) {/*DBG_EXCEPTION(e);*/}
	}
#endif

	private void initDownloadComponents(String url)
	{
	#if USE_MDL
		try{
			//url = "http://dl.gameloft.com/partners/androidmarket/d.cdn_test.php?product=148&version=103&portal=google_market&model=ADR6300&device=inc";
			InputStream in = (new HttpClient()).getInputStream(url);
			BufferedReader d = new BufferedReader(new InputStreamReader(in));
				
			Pattern p = Pattern.compile("(.*?)\\((.*?)\\)=(.*?)");

			if(d != null)
			{
				String line = d.readLine();
				while(line != null)
				{
					try{
						Matcher m = p.matcher(line);	
						if(m.matches()) {	
							DownloadComponent component = new DownloadComponent(m.group(3), m.group(1));
							#if GOOGLE_MARKET_DOWNLOAD
							component.setImportance(0);
							#endif
							mDownloadComponents.add(component);
						}
					}catch(Exception e)
					{
						DBG("GameInstaller", "Error: " + e.getMessage());
					}
					line = d.readLine();
				}
			}
		}catch(Exception ex){DBG_EXCEPTION(ex);}
		
	#else
		DownloadComponent component = new DownloadComponent(url, "");
		#if GOOGLE_MARKET_DOWNLOAD
		component.setImportance(0);

		#endif
		mDownloadComponents.add(component);
	#endif
	}
	private int mInfoFileLength = 0;
	private boolean getRequiredResourcesValues()
	{
		
		DBG("GameInstaller","getRequiredResourcesValues()");
		
		boolean isok = true;
		
		for(DownloadComponent component : mDownloadComponents)
		{
			if(!component.getRequiredResourcesValues(false))
			{
				isok = false;
				DBG("GameInstaller","false for "+component.getName());
			}
		}
		
		return isok;

	}
	
	public void pushHashMapResource()
	{
		try
		{
			if(TimePass.m_hmGetFile == null)
			{
				TimePass.m_hmGetFile = new HashMap<String, String>();
				DBG("GameInstaller", "Create HashMap");
			
				//DBG("DownloadComponent", "WE'RE LOOKING FOR DOWNLOADED IN: "+GameInstaller.sd_folder);
				java.io.FileInputStream fstream = new java.io.FileInputStream(GameInstaller.sd_folder + "/" + "d_o_w_n_l_o_a_d_e_d.txt");
				java.io.DataInputStream in = new java.io.DataInputStream(fstream);
				java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(in));
				String strLine;
				while ((strLine = br.readLine()) != null)
				{
					//DBG( "GameInstaller" ," strLine " + strLine);
					TimePass.m_hmGetFile.put(strLine, strLine);
				}
				in.close();
			}
		}
		catch (Exception e) {}
	}
	

	private static ArrayList<String> sNativeLibs = new ArrayList<String>();
	public static void addNativeLib(String name)
	{
		if(!GameInstaller.sNativeLibs.contains(name))
		{
			DBG("GameInstaller","added native lib: "+name);
			GameInstaller.sNativeLibs.add(name);
		}
	}
	
	public static void makeLibExecutable(String libName) 
	{
		//Enable executable permission
#if !BUILD_FOR_FIRMWARE_1_6
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			(new File(libName)).setExecutable(true);
		}
		else
#endif
		{
			try
			{
				Runtime.getRuntime().exec("/system/bin/chmod u+x " + GameInstaller.LIBS_PATH);
				Runtime.getRuntime().exec("/system/bin/chmod u+x " + libName);
			}
			catch (Exception e)
			{
				DBG_EXCEPTION(e);
			}
		}	
	}
		

#if GOOGLE_DRM
	//native+java drm
	public void doStuff()
	{
		DBG("Game","doStuff()");
		
		if(checkAccountsNum() == 0) {
			setState(GI_STATE_NO_GP_ACCOUNT_DETECTED);
		} else {
			this.runOnUiThread(new Runnable ()
			{
				public void run ()
				{
					try
					{
						showLoading();
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
			});
			
			bIsNetworkOK = (SUtils.hasConnectivity() == 0) ? false : true;
			initNative();
			prepareChecker();
			mChecker.checkAccess(mLicenseCheckerCallback);
		}
	}
	
	private void prepareChecker()
	{
		// Try to use more data here. ANDROID_ID is a single point of attack.
		#if HDIDFV_UPDATE
		String deviceId = Device.getHDIDFV();
		DBG("Google DRM","prepareChecker(), deviceId = " + deviceId);
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#else
		String deviceId = Device.getDeviceId();
		DBG("Google DRM","prepareChecker(), deviceId = " + deviceId);
		#endif
		
		// Library calls this when it's done.
		if(mLicenseCheckerCallback == null)
		{
			mLicenseCheckerCallback = new MyLicenseCheckerCallback();
		}
			
		// Construct the LicenseChecker with a policy.
		if(obfuscator == null)
		{
			obfuscator = new AESObfuscator(SALT, getPackageName(), deviceId);
		}
		
		if(mChecker == null)
		{
			aPolicy= new GDRMPolicy(this,obfuscator);
			mChecker = new LicenseChecker(this, aPolicy, getKey());
		}
	}
	

	
	private class MyLicenseCheckerCallback implements LicenseCheckerCallback 
	{

//		Possible allow parameterReason Values:
//		LICENSED = 256;
//		NOT_LICENSED = 561;
//		RETRY = 291;
		public void allow(int paramReason) 
		{
			DBG("MyLicenseCheckerCallback","allow(int): " + paramReason);
			if (isFinishing()) 
			{
				// Don't update UI if Activity is finishing.
				// Or is checking
				return;
			}
			
			destroyLoading();
			DBG("MyLicenseCheckerCallback","nativeStart()");
			nativeStart();
		}

//		Possible dontallow parameterReason Values:
//		LICENSED = 256;
//		NOT_LICENSED = 561;
//		RETRY = 291;
		public void dontAllow(int paramReason) 
		{
			DBG("MyLicenseCheckerCallback","dontAllow(int)" + paramReason);
			if (isFinishing()) 
			{
				// Don't update UI if Activity is finishing.
				
				return;
			}
			
			DBG("MyLicenseCheckerCallback","not allowing");
			// Should not allow access. In most cases, the app should assume
			// the user has access unless it encounters this. If it does,
			// the app should inform the user of their unlicensed ways
			// and then either shut down the app or limit the user to a
			// restricted set of features.
			// In this example, we show a dialog that takes the user to Market.
			
			if (!bIsNetworkOK) 
			{
				DBG("LicenseCheck","NETWORK FAILED");
				destroyLoading();
				showDialog(2);
				return;
			}
			destroyLoading();
			showDialog(0);
		}
		//possible error codes (as int):
//		ERROR_INVALID_PACKAGE_NAME = 1;
// 		ERROR_NON_MATCHING_UID = 2;
//		ERROR_NOT_MARKET_MANAGED = 3;
// 		ERROR_CHECK_IN_PROGRESS = 4;
//  	ERROR_INVALID_PUBLIC_KEY = 5;
//  	ERROR_MISSING_PERMISSION = 6;
		public void applicationError(int errorCode) 
		{
			DBG("MyLicenseCheckerCallback","error: " + errorCode);
			destroyLoading();
			showDialog(1);
		}		
	}	
#endif
#if GOOGLE_DRM || GOOGLE_MARKET_DOWNLOAD
	protected Dialog onCreateDialog(final int id) 
	{
		if(alertDialog == null)
		{
			alertDialog = new AlertDialog.Builder(this);
			alertDialog.setCancelable(false);
			alertDialog.setOnKeyListener (new DialogInterface.OnKeyListener()
			{
				public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
				{
					return true;
				}
			});
		}
			
		switch(id)
		{
			case 0://invalid license
			case 1://Application error
			case 3:
			case 4:
				alertDialog.setTitle(getString(R.string.UNLICENSED_DIALOG_TITLE, this))
				.setMessage(getString(R.string.UNLICENSED_DIALOG_BODY, this))
				.setPositiveButton(
					#if GOOGLE_DRM
					getString(R.string.BUY_BUTTON, this)
					#else
					getString(R.string.GET_THE_GAME,this)
					#endif
					, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						dismissDialog(id);
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=" + getPackageName()));
						destroyLoading();
						DBG("LicenseCheck","getPackageName()......." +  getPackageName());
						startActivity(marketIntent);
						finishFail();
					}
				})
				.setNegativeButton(getString(R.string.CANCEL, this), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						dismissDialog(id);
					#if TEST_GOOGLE_DRM
						if (id == 3 || id == 4)
							setState(GI_STATE_GAMELOFT_LOGO);
						else
							startGame();
					#else
						finishFail();
					#endif
					}
				});
				break;
			case 2://server or network error
			case 5:
				alertDialog.setTitle(getString(R.string.NETWORK_ERROR_TITLE, this))
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(getString(R.string.NETWORK_ERROR_BODY, this))
				.setPositiveButton(getString(R.string.RETRY, this), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						removeDialog(id);
						#if TEST_GOOGLE_DRM
							if (id == 5)
								setState(GI_STATE_GAMELOFT_LOGO);
							else
								startGame();
						#else
							if (id == 5)
							{
							#if GOOGLE_MARKET_DOWNLOAD
								updateCheckPiracy();
							#else
								GoogleDrmCheck();
							#endif
							}
							else
							{
							#if GOOGLE_DRM
								doStuff();
							#endif
							
							}
						#endif
					}
				})
				.setNegativeButton(getString(R.string.CANCEL, this), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						dismissDialog(id);
					#if TEST_GOOGLE_DRM
						if (id == 5)
							setState(GI_STATE_GAMELOFT_LOGO);
						else
							startGame();
					#else
						finishFail();
					#endif
					}
				});
				break;
		}
		return alertDialog.create();
	}
	
	protected void onPrepareDialog(final int id, Dialog d) {}
	
	private void showLoading()
	{
		if(dialog == null)
		{
			dialog = new ProgressDialog(this);
			dialog.setCancelable (false);
			dialog.setOnKeyListener (new DialogInterface.OnKeyListener()
			{
				public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event)
				{
					return true;
				}
			});
		}
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(getString(R.string.CHECKING_LICENSE, this));		
		dialog.show();
	}
	
	private void destroyLoading()
	{
		this.runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				try
				{
					if(dialog != null)
					{
						dialog.dismiss();
						dialog = null;
					}
				}
				catch (Exception e)
				{
					DBG_EXCEPTION(e);
				}
			}
		});
		
	}
#endif
#if GOOGLE_DRM
	
	public static native void initNative();
	public static native void nativeStart();
	public native void getPublicKey();
#endif

#if AUTO_UPDATE_HEP
	public boolean isNetworkAvailable()
	{
		if (isWifiAlive())
			return true;
		else if(isOtherNetAlive())
			return true;
		return false;
	}
	/*
	* Strip and removes / and : from the string. Server support up to 50 characters
	*/
	public String formatFINGERPRINT(String fingerprint){
		if (fingerprint == null || fingerprint.length() <1)//invalid fingerprint
		  return "UnknownFingerPrint";
		int first_strip_index = fingerprint.indexOf(":");
		String result=null;
		try{
			if (first_strip_index != -1 && first_strip_index < fingerprint.length()-1){
				result = fingerprint.substring(first_strip_index +1 );
				result = result.replaceAll("[^a-zA-Z 0-9.]+","");
				DBG("GameInstaller", "fingerprint="+result);
				result = java.net.URLEncoder.encode(result.trim(), "UTF-8");
			}else{
				result = fingerprint.replaceAll("[^a-zA-Z 0-9.]+","");
				result = java.net.URLEncoder.encode(fingerprint.trim(), "UTF-8");
			}
		}catch(Exception e){
			result = null;
			DBG("GameInstaller","Cant format parameter sting fingerprint for autoupdate");
		}
		if (result != null)
			return result;
		else
			return fingerprint;
	}
#endif//AUTO_UPDATE_HEP
	void UpdateKeyController(int keyCode)
	{
		try
		{
			int sizeArrayButtons = getButtonList().size();
			if(sizeArrayButtons > 0)
			{
				if(keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
				{
					if((++this.IndexController) >= sizeArrayButtons)
					{
						this.IndexController = sizeArrayButtons - 1;
					}
					setButtonFocus(getButtonList().get(this.IndexController));
				} else if(keyCode == KeyEvent. KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
				{
					if((--this.IndexController) < 0)
					{
						this.IndexController = 0;
					}
					setButtonFocus(getButtonList().get(this.IndexController));
				} else if(keyCode == KeyEvent.KEYCODE_BUTTON_A)
				{	
					if((this.IndexController >= 0) && (this.IndexController < sizeArrayButtons))
					{
						 pressButton(getButtonList().get(this.IndexController));
					}
				}
			}
		}
		catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
	
	}
	
	void ResetControllerIndex()
	{
		this.IndexController = -1;
	}
	
	private void setButtonFocus(final Button button) 
	{
		this.runOnUiThread(new Runnable () 
			{
				public void run () 
				{
					try 
					{        
						button.setFocusableInTouchMode(true);
						button.requestFocus();
					} catch (Exception e) {
						DBG_EXCEPTION(e);
					}
				}
			});
	}
	
	private void pressButton(final Button button) 
	{
		this.runOnUiThread(new Runnable () 
			{
				public void run () 
				{
					try 
					{        
						button.performClick();
					} catch (Exception e) {
						DBG_EXCEPTION(e);
					}
				}
			});
	}
}
#endif
