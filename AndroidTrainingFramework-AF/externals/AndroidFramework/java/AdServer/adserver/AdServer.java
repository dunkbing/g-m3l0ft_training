package APP_PACKAGE;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import javax.net.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.*;

import android.app.*;
import android.app.ProgressDialog.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.*;
import android.net.wifi.*;
import android.net.http.*;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;

#if ADS_USE_TAPJOY
import com.tapjoy.*;
#endif

#if ADS_USE_ADCOLONY
import com.jirbo.adcolony.*;
#endif

#if ADS_USE_CHARTBOOST
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
#endif

#if ADS_USE_FLURRY
import com.flurry.android.*;
#endif

#if USE_HAS_OFFERS_TRACKING
import com.mobileapptracker.MobileAppTracker;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
#endif

import APP_PACKAGE.GLUtils.Encrypter;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;

public class AdServer 
{
	private	static AdServer			s_classInstance = null;
	public 	static String			GameVersion = GAME_VERSION_NAME_LETTER;
	public 	static String			GameloftGameCode = GGC_GAME_CODE;
	
	public 	static String[] 		TXT_LANGUAGE_LIST = { "EN", "FR", "DE", "IT", "SP", "JP", "KR", "CN", "BR", "RU", "TR", "AR", "TH", "ID", "VI", "ZT" };
	
	public 	static int[] TXT_OK = 	
		{ 
			R.string.ADS_OK_EN,
			R.string.ADS_OK_FR,
			R.string.ADS_OK_DE,
			R.string.ADS_OK_IT,
			R.string.ADS_OK_SP,
			R.string.ADS_OK_JP,
			R.string.ADS_OK_KR,
			R.string.ADS_OK_CN,
			R.string.ADS_OK_BR,
			R.string.ADS_OK_RU,
			R.string.ADS_OK_TR,
			R.string.ADS_OK_AR,
			R.string.ADS_OK_TH,
			R.string.ADS_OK_ID,
			R.string.ADS_OK_VI,
			R.string.ADS_OK_ZT
		};
		
	public 	static int[] TXT_NETERROR = 	
		{ 
			R.string.ADS_NETERROR_EN,
			R.string.ADS_NETERROR_FR,
			R.string.ADS_NETERROR_DE,
			R.string.ADS_NETERROR_IT,
			R.string.ADS_NETERROR_SP,
			R.string.ADS_NETERROR_JP,
			R.string.ADS_NETERROR_KR,
			R.string.ADS_NETERROR_CN,
			R.string.ADS_NETERROR_BR,
			R.string.ADS_NETERROR_RU,
			R.string.ADS_NETERROR_TR,
			R.string.ADS_NETERROR_AR,
			R.string.ADS_NETERROR_TH,
			R.string.ADS_NETERROR_ID,
			R.string.ADS_NETERROR_VI,
			R.string.ADS_NETERROR_ZT
		};
	
	public 	static int[] TXT_NOFREECASH = 	
		{ 
			R.string.ADS_NOFREECASH_EN,
			R.string.ADS_NOFREECASH_FR,
			R.string.ADS_NOFREECASH_DE,
			R.string.ADS_NOFREECASH_IT,
			R.string.ADS_NOFREECASH_SP,
			R.string.ADS_NOFREECASH_JP,
			R.string.ADS_NOFREECASH_KR,
			R.string.ADS_NOFREECASH_CN,
			R.string.ADS_NOFREECASH_BR,
			R.string.ADS_NOFREECASH_RU,
			R.string.ADS_NOFREECASH_TR,
			R.string.ADS_NOFREECASH_AR,
			R.string.ADS_NOFREECASH_TH,
			R.string.ADS_NOFREECASH_ID,
			R.string.ADS_NOFREECASH_VI,
			R.string.ADS_NOFREECASH_ZT
		};
	
	public 	static int[] TXT_LOADING = 	
		{ 
			R.string.ADS_LOADING_EN,
			R.string.ADS_LOADING_FR,
			R.string.ADS_LOADING_DE,
			R.string.ADS_LOADING_IT,
			R.string.ADS_LOADING_SP,
			R.string.ADS_LOADING_JP,
			R.string.ADS_LOADING_KR,
			R.string.ADS_LOADING_CN,
			R.string.ADS_LOADING_BR,
			R.string.ADS_LOADING_RU,
			R.string.ADS_LOADING_TR,
			R.string.ADS_LOADING_AR,
			R.string.ADS_LOADING_TH,
			R.string.ADS_LOADING_ID,
			R.string.ADS_LOADING_VI,
			R.string.ADS_LOADING_ZT
		};
	
	public static boolean wasAdServerVideosError = false;
	public static boolean wasAdServerVideosLoaded = false;
	
	public 	static int 				currentLanguage = 0;
	
	public static boolean			isYumeReady = false;
	
	#if HDIDFV_UPDATE
	public 	String 			AD_REQUEST_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads_server.php?game_code=GAME_CODE&udid=UDID&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&country=COUNTRY&lg=LANGUAGE&game_ver=GAMEVERSION&os=android&igp_rev=1005";
	#else
	public 	String 			AD_REQUEST_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads_server.php?game_code=GAME_CODE&udid=UDID&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&country=COUNTRY&lg=LANGUAGE&game_ver=GAMEVERSION&os=android&igp_rev=1005";
	#endif
	public 	String 			AD_REQUEST_URL = "";
	#if HDIDFV_UPDATE
	public 	String			AD_REQUEST_FREE_CASH_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads_server.php?game_code=GAME_CODE&udid=UDID&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&country=COUNTRY&lg=LANGUAGE&game_ver=GAMEVERSION&os=android&igp_rev=1005&freecash=1";
	#else
	public 	String			AD_REQUEST_FREE_CASH_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads_server.php?game_code=GAME_CODE&udid=UDID&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&country=COUNTRY&lg=LANGUAGE&game_ver=GAMEVERSION&os=android&igp_rev=1005&freecash=1";
	#endif
	public 	String			AD_REQUEST_FREE_CASH_URL = "";
	#if HDIDFV_UPDATE
	public  String			AD_LINK_VIDEO_CAP = "https://ingameads.gameloft.com/redir/ads_capping.php?game=GAME_CODE&udid=UDID&hdidfv=HDIDFV&androidid=ANDROIDID&igp_rev=1005";
	#else
	public  String			AD_LINK_VIDEO_CAP = "https://ingameads.gameloft.com/redir/ads_capping.php?game=GAME_CODE&udid=UDID&androidid=ANDROIDID&igp_rev=1005";
	#endif
	#if HDIDFV_UPDATE
	public 	String			GAMELOFT_AD_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/ads_server_view.php?from=GAME_CODE&country=COUNTRY&lg=LANGUAGE&udid=UDID&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&game_ver=GAMEVERSION&os=android&igp_rev=1005";
	#else
	public 	String			GAMELOFT_AD_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/ads_server_view.php?from=GAME_CODE&country=COUNTRY&lg=LANGUAGE&udid=UDID&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&game_ver=GAMEVERSION&os=android&igp_rev=1005";
	#endif
	public 	String			GAMELOFT_AD_URL = "";
	#if HDIDFV_UPDATE
	public	String			GAMELOFT_INTERSTITIAL_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/interstitial_view.php?from=FROM&country=COUNTRY&lg=LANG&udid=UDIDPHONE&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&igp_rev=1005&os=android";
	#else
	public	String			GAMELOFT_INTERSTITIAL_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/interstitial_view.php?from=FROM&country=COUNTRY&lg=LANG&udid=UDIDPHONE&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&igp_rev=1005&os=android";
	#endif
	public	String			GAMELOFT_INTERSTITIAL_URL = "";
	
	private boolean			isAdLoading = false;	
	private boolean			isInterstitialLoading = false;	
	private boolean			canShowAd = false;
	public 	String			currentAdType = "none";	
	public	boolean			showFreeCash = false;
	
	private WebView 		glAdView = null;	
	
	private boolean			shouldHideBanner = true;

#if ADS_USE_TAPJOY
	public 	String			TapjoyAppId = ADS_TAPJOY_APP_ID;
	public 	String			TapjoySecretKey = ADS_TAPJOY_SECRET_KEY;
	private RelativeLayout	tapjoyAdView = null;
#endif
	
#if ADS_USE_ADCOLONY
	public 	String			AdCoAppId = ADS_ADCO_APP_ID;
	public	String			AdCoZoneId1 = ADS_ADCO_ZONE_ID_1;
	public	String			AdCoZoneId2 = ADS_ADCO_ZONE_ID_2;
	//public	boolean			isCashManagedLocally = false;	// Used for AdColony. Change this to false if virtual currency is not managed in-game	
#endif
	
#if ADS_USE_YUME
	private YuMeInterface 	yumeSDKInterface = null;
	private boolean 		bIsYuMeInitialized = false;
	private boolean 		bIsYuMePrefetch = false;
	
	public String			YuMeServerUrl = ADS_YUME_SERVER_URL;
	public String			YuMeDomainId = ADS_YUME_DOMAIN_ID;
#endif

#if ADS_USE_CHARTBOOST
	private Chartboost		CBHandle = null;
	public 	String			CBAppId = ADS_CHARTBOOST_APP_ID;
	public 	String			CBAppSig = ADS_CHARTBOOST_APP_SIG;
#endif

#if ADS_USE_FLURRY
	String flurryApiKey = ADS_FLURRY_ID;
	String flurryInterstitialSpace = ADS_FLURRY_INTERSTITIAL_ADSPACE;
	String flurryOfferwallSpace = ADS_FLURRY_FREECASH_ADSPACE;
	
	boolean flurryInterstitialIsReady = false;
	boolean flurryOfferwallIsReady = false;	
#endif
	
	public 	LayoutParams	lpBanner = null;
	
	private final Activity	myActivity;
	private ViewGroup		mView = null;
		
	private	Thread			showBannerThread = null;
	
	public static final int TOP_CENTER = 0;
	public static final int TOP_LEFT = 1;
	public static final int TOP_RIGHT = 2;
	public static final int BOTTOM_CENTER = 3;
	public static final int BOTTOM_LEFT = 4;
	public static final int	BOTTOM_RIGHT = 5;
	
	public static final int BANNER_SMALL = -1;
	public static final int BANNER_LARGE = -2;
	
	public int				bannerPosition = TOP_CENTER;
	public int				bannerSize = BANNER_SMALL;
	
	public boolean 			bInterstitialResponse = false;
	public boolean			bInterstitialSuccess = false;
	
	public boolean			isActive;
	
	Display display;
	DisplayMetrics metrics;
	
	private static boolean	isPAU = false;
	private static boolean	wasPAUConfigRead = false;
	private static boolean	shouldCheckPAUBanners = false;
	private static boolean	shouldCheckPAUInterstitials = false;
	
	#if HDIDFV_UPDATE
	private static String	ADS_PAU_CONFIG_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads_server.php?game_code=GAME_CODE&udid=UDIDPHONE&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&lg=LANGUAGE&game_ver=GAMEVERSION&check_pau=1&os=android";
	#else
	private static String	ADS_PAU_CONFIG_URL_TEMPLATE = "https://ingameads.gameloft.com/redir/ads_server.php?game_code=GAME_CODE&udid=UDIDPHONE&androidid=ANDROIDID&d=DEVICE_NAME&f=FIRMWARE&lg=LANGUAGE&game_ver=GAMEVERSION&check_pau=1&os=android";
	#endif


	#if USE_HAS_OFFERS_TRACKING
	private static String ACTION_COMPLETE_TRACK_URL = "http://ingameads.gameloft.com/redir/hdsdktracker.php?game=GAME_CODE&udid=UDID&androidid=ANDROIDID&hdidfv=HDIDFV&action=ACTION_TRACKED&action_id=ACTION_TRACK_ID";		
	public MobileAppTracker mobileAppTracker = null;
	#endif
	
	public static void setIsPAU(boolean bIsPAU)
	{
		DBG("ADSERVER", "setIsPAU: " + bIsPAU);
	#if USE_WELCOME_SCREEN
		SplashScreenActivity.setIsPAU(bIsPAU);
	#endif
		isPAU = bIsPAU;		
		if (isPAU) {
			new Thread() {	
				public void run() {
					setPAUBooleans();
				}
			}.start();
		}
	}
	
	private static void setPAUBooleans()
	{
		DBG("ADSERVER", "setPAUBooleans()");
		
		if (currentLanguage < 0 || currentLanguage >= TXT_LANGUAGE_LIST.length)
			currentLanguage = 0;
		
		String ADS_PAU_CONFIG_URL = 
			ADS_PAU_CONFIG_URL_TEMPLATE
				.replaceAll("GAME_CODE", GameloftGameCode)
				.replaceAll("ANDROIDID", Device.getAndroidId())
				#if (HDIDFV_UPDATE == 2)
				.replaceAll("UDIDPHONE", Device.getSerial())
				#else
				.replaceAll("UDIDPHONE", Device.getDeviceId())				
				#endif
				#if HDIDFV_UPDATE
				.replaceAll("HDIDFV", Device.getHDIDFV())
				#endif
				.replaceAll("DEVICE_NAME", Build.MANUFACTURER + "_" + Build.MODEL)
				.replaceAll("FIRMWARE", Build.VERSION.RELEASE)
				.replaceAll("COUNTRY", java.util.Locale.getDefault().getCountry())
				.replaceAll("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage])
				.replaceAll("GAMEVERSION", GameVersion)
				.replaceAll(" ", "");
		#if HDIDFV_UPDATE
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#endif
		DBG("ADSERVER", "PAU request: " + ADS_PAU_CONFIG_URL);
		String response = getHttpResponse(ADS_PAU_CONFIG_URL);
		DBG("ADSERVER", "PAU response: " + response);
		
		if (response != null)
		{
			try
			{
				JSONObject root = new JSONObject(response);
				int banners = root.getInt("banners");
				int interstitials = root.getInt("interstitials");
				
				shouldCheckPAUBanners = (banners == 1);
				shouldCheckPAUInterstitials = (interstitials == 1);
				wasPAUConfigRead = true;
			}
			catch (Exception e)
			{
				DBG_EXCEPTION(e);
				wasPAUConfigRead = false;
				shouldCheckPAUBanners = false;
				shouldCheckPAUInterstitials = false;
			}
		}
		else
		{
			wasPAUConfigRead = false;		
			shouldCheckPAUBanners = false;
			shouldCheckPAUInterstitials = false;
		}
	}
	
	public AdServer(Activity activity)
	{
		s_classInstance = this;
		this.myActivity = activity;	
		
		if(SUtils.getContext() == null) {
			SUtils.setContext((Context)activity);	
		}
	}
	
	public AdServer(Activity activity, int extra)
	{
		this(activity);
		
		if (extra >= 0 && extra <= 5)
			bannerPosition = extra;	
		else
			bannerPosition = TOP_CENTER;	
		
		if (extra == BANNER_SMALL || extra == BANNER_LARGE)
			bannerSize = extra;
		else 
			bannerSize = BANNER_SMALL;
	}
	
	public AdServer(Activity activity, int position, int adsize)
	{
		this(activity, position);
		
		if (adsize == BANNER_SMALL || adsize == BANNER_LARGE)
			bannerSize = adsize;
		else 
			bannerSize = BANNER_SMALL;
	}

	public void onDestroy() 
    {
	#if ADS_USE_YUME
		/* de-initialize the YuMe SDK */
		yumeSDKInterface.deInitYuMeSDK();
		
		/* yumeSDKInterface clean-up */
		yumeSDKInterface.cleanUp();
	#endif
    }

    public void onResume() 
    {
		isActive = true;
	
	#if ADS_USE_TAPJOY
		TapjoyConnect.getTapjoyConnectInstance().appResume();
	#endif
	#if ADS_USE_ADCOLONY
		AdColony.resume(myActivity);
	#endif
	#if USE_HAS_OFFERS_TRACKING
		if (mobileAppTracker != null)
		{
			// Get source of open for app re-engagement
	        mobileAppTracker.setReferralSources(myActivity);
	        // MAT will not function unless the measureSession call is included
	        mobileAppTracker.measureSession();
	    }
	#endif
    }

    public void onPause() 
    {
		isActive = false;
		
	#if ADS_USE_TAPJOY
		TapjoyConnect.getTapjoyConnectInstance().appPause();
	#endif
	#if ADS_USE_ADCOLONY
		AdColony.pause();
	#endif

    }
	
	public void onStart()
	{
	#if ADS_USE_CHARTBOOST
		CBHandle.onStart(myActivity);
	#endif
	#if ADS_USE_FLURRY
		if (mView == null)
			mView = new RelativeLayout(myActivity);
		FlurryAgent.onStartSession(myActivity, flurryApiKey);
		FlurryAds.fetchAd(myActivity, flurryInterstitialSpace, mView, FlurryAdSize.FULLSCREEN);
		FlurryAds.fetchAd(myActivity, flurryOfferwallSpace, mView, FlurryAdSize.FULLSCREEN);
	#endif
	}
	
	public void onStop()
	{
	#if ADS_USE_CHARTBOOST
		CBHandle.onStop(myActivity);
	#endif
	#if ADS_USE_FLURRY
		FlurryAgent.onEndSession(myActivity);
	#endif
	}
	
	public boolean onBackPressed()
	{
	#if ADS_USE_CHARTBOOST
		return CBHandle.onBackPressed();
	#else
		return false;
	#endif
	}
	
	public boolean isAdLoading()
	{
		return isAdLoading;
	}
	
	public boolean isInterstitialLoading()
	{
		return isInterstitialLoading;
	}
	
	public static void handleGotoString(String name)
	{
		//av TODO
		//CLASS_NAME.m_sInstance.splashScreenFunc(name);
	}
	
	public Context getAppContext() {
		return (Context)myActivity;
	}
	
	class WebView1 extends WebView {

		public WebView1(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchKeyEventPreIme(KeyEvent event) {
			if ((event.getAction() == KeyEvent.ACTION_UP) && event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A && this.isFocused()) {
				try {
					this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
					this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));				
				} catch (Exception e) { }
			}

			return super.dispatchKeyEventPreIme(event);
		}
	}
	
#if USE_HAS_OFFERS_TRACKING
	public static void trackInstall(int isUpdate)
	{
		DBG("ADSERVER", "================ trackInstall() " + isUpdate);
		if (s_classInstance != null)
		{
			if (isUpdate != 0)
			{
				trackHasOffersSession(true);	
				s_classInstance.trackActionCompleteForID("HASOFFERS: " + MAT_ADVERTISER_ID + ";" + MAT_CONVERSION_KEY + ";", "HASOFFERS_UPDATE");
			}
			else
			{
				trackHasOffersSession(false);			
				s_classInstance.trackActionCompleteForID("HASOFFERS: " + MAT_ADVERTISER_ID + ";" + MAT_CONVERSION_KEY + ";", "HASOFFERS_INSTALL");
			}
		}
	}
	
	public static void trackOpen()
	{
		DBG("ADSERVER", "================ trackOpen() ");
		if (s_classInstance != null && isSesssionTracked)
		{
			if (s_classInstance.mobileAppTracker != null)
			{
				s_classInstance.mobileAppTracker.measureAction("open");
				s_classInstance.trackActionCompleteForID("HASOFFERS: " + MAT_ADVERTISER_ID + ";" + MAT_CONVERSION_KEY + ";", "HASOFFERS_OPEN");
			}
		}
	}
	
	public static void trackActionComplete(final String action)
	{
		DBG("ADSERVER", "================ trackActionComplete() ");
		if (s_classInstance != null && isSesssionTracked)
		{
			if (s_classInstance.mobileAppTracker != null)
			{
				s_classInstance.mobileAppTracker.measureAction(action);
				s_classInstance.trackActionCompleteForID(action, "HASOFFERS_ACTION_COMPLETE");
			}
		}
	}
	
	private static void trackHasOffersSession(boolean isExisting)
	{
		DBG("ADSERVER", "================ trackHasOffersSession() ");
		if (s_classInstance.mobileAppTracker == null)
			isSesssionTracked = false;
			
		if (!isSesssionTracked)
		{
			if (s_classInstance != null)
			{				
				MobileAppTracker.init(s_classInstance.myActivity.getApplicationContext(), MAT_ADVERTISER_ID, MAT_CONVERSION_KEY);
				s_classInstance.mobileAppTracker = MobileAppTracker.getInstance();
				// s_classInstance.mobileAppTracker.setDebugMode(true);
				// s_classInstance.mobileAppTracker.setAllowDuplicates(true);
				s_classInstance.mobileAppTracker.setReferralSources(s_classInstance.myActivity); // Get referral package or url if it exists			
				s_classInstance.mobileAppTracker.setExistingUser(isExisting);
				s_classInstance.mobileAppTracker.measureSession();
				isSesssionTracked = true;

				s_classInstance.mobileAppTracker.setAndroidId(Device.getAndroidId());					
				s_classInstance.mobileAppTracker.setDeviceId(Device.getDeviceId());

				new Thread(new Runnable() {
					@Override 
					public void run() {
					// See sample code at http://developer.android.com/google/play-services/id.html
						try {
							Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(s_classInstance.myActivity.getApplicationContext());
							s_classInstance.mobileAppTracker.setGoogleAdvertisingId(adInfo.getId(), adInfo.isLimitAdTrackingEnabled());
						} catch (IOException e) {
							// Unrecoverable error connecting to Google Play services (e.g.,
							// the old version of the service doesn't support getting AdvertisingId).
						} catch (GooglePlayServicesNotAvailableException e) {
							// Google Play services is not available entirely.
						} catch (GooglePlayServicesRepairableException e) {
							// Encountered a recoverable error connecting to Google Play services.
						} catch (NullPointerException e) {
							// getId() is sometimes null
						}
					}
				}).start();
			}
		}
	}
	
	private static boolean isSesssionTracked = false;	
	
	private static void trackActionCompleteForID(final String id, final String action)
	{
		if (s_classInstance != null)
		{
			new Thread()
			{
				public void run()
				{
					String response = s_classInstance.getHttpResponse(s_classInstance.solveTemplate(ACTION_COMPLETE_TRACK_URL).replace("ACTION_TRACKED", action).replace("ACTION_TRACK_ID", id));
					DBG("ADSERVER", "hdsdktracker response: " + response);
				}
			}.start();		
		}
	}
#endif
	
	public String solveTemplate(String template)
	{
		String UDID = Device.getDeviceId();				
		String deviceName = Build.MANUFACTURER + "_" + Build.MODEL;
		String deviceFW = Build.VERSION.RELEASE;
		
		String result = template;
		result = result.replace("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage]);
		result = result.replace("COUNTRY", java.util.Locale.getDefault().getCountry());
		result = result.replace("GAME_CODE", GameloftGameCode);
		result = result.replace("ANDROIDID", Device.getAndroidId());
		#if (HDIDFV_UPDATE == 2)
		result = result.replace("UDID", Device.getSerial());
		#else
		result = result.replace("UDID", UDID);
		#endif
		#if HDIDFV_UPDATE
		result = result.replace("HDIDFV", Device.getHDIDFV());
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#endif
		result = result.replace("DEVICE_NAME", deviceName);
		result = result.replace("FIRMWARE", deviceFW);
		result = result.replace("GAMEVERSION", GameVersion);
		result = result.replaceAll(" ", "");
		
		return result;
	}
	
	public void InitAds(ViewGroup mainView)
    {
		mView = mainView;
		isActive = true;
		String providers = "";
	#if ADS_USE_ADCOLONY
		providers += " AdColony ";
		// Setup AdColony
		
		boolean isAmazon = false;
	#if USE_AMAZONMP
		isAmazon = true;
	#endif
		String store = isAmazon ? "amazon" : "google";
	
	#if (HDIDFV_UPDATE == 2)
		AdColony.setCustomID("hdidfv:" + Device.getHDIDFV());
	#else
		AdColony.setCustomID("udid:" + Device.getDeviceId());
	#endif		
		AdColony.configure(myActivity, "version:" + GameVersion + ",store:" + store, AdCoAppId, AdCoZoneId1, (AdCoZoneId2.replaceAll(" ", "").equals("") ? AdCoZoneId1 : AdCoZoneId2));
		// DBG("ADSERVER", "device id = " + AdColony.getDeviceID());
		AdColony.addV4VCListener(AdCoVCListener);
	#endif
		
	#if ADS_USE_TAPJOY	
		providers += " Tapjoy ";
		// Setup Tapjoy
		#if RELEASE_VERSION
		TapjoyLog.enableLogging(false);
		#else
		TapjoyLog.enableLogging(true);
		#endif
		
		TapjoyConnect.requestTapjoyConnect(myActivity.getApplicationContext(), TapjoyAppId, TapjoySecretKey);
	#if (HDIDFV_UPDATE == 2)
		TapjoyConnect.getTapjoyConnectInstance().setUserID("hdidfv:" + Device.getHDIDFV());
	#else
		TapjoyConnect.getTapjoyConnectInstance().setUserID("udid:" + Device.getDeviceId());
	#endif
		TapjoyConnect.getTapjoyConnectInstance().setVideoCacheCount(2);     // If there is a concern about bandwidth usage or storage space on the device, you can set the number of maximum cached videos
		TapjoyConnect.getTapjoyConnectInstance().setVideoNotifier(tapjoyVideoNotifier);
		TapjoyConnect.getTapjoyConnectInstance().cacheVideos();
		TapjoyConnect.getTapjoyConnectInstance().enableDisplayAdAutoRefresh(false);		
	#endif
		
	#if ADS_USE_YUME
		providers += " YuMe ";
		/* create the YuMeSDKInterface object */
        yumeSDKInterface = YuMeInterface.getYuMeSDKInterface();
        
        /* set the Main view handle in YuMeSDKInterface object */
        yumeSDKInterface.setHomeView(myActivity);
        
        /* initialize the YuMeSDK */
 		bIsYuMeInitialized = yumeSDKInterface.initYuMeSDK(YuMeServerUrl, YuMeDomainId);
 	    if(bIsYuMeInitialized) {
	    	INFO("ADSERVER", "YuMe SDK Initialized Successfully.");
    	} else {
    		ERR("ADSERVER", "Error Initializing YuMe SDK.");
    	}
		
		bIsYuMePrefetch = yumeSDKInterface.prefetchAd();
		if(!bIsYuMePrefetch) {
			ERR("ADSERVER", "Error Prefetching Ad.");
		}
	#endif
	
	#if ADS_USE_CHARTBOOST
		CBHandle = Chartboost.sharedChartboost();
		CBHandle.onCreate(myActivity, CBAppId, CBAppSig, CBDelegate);
		CBHandle.startSession();
		CBHandle.cacheInterstitial();
	#endif
	
	#if ADS_USE_FLURRY
		FlurryAds.setAdListener(flurryAdListener);
		
	#endif
		
		DBG("ADSERVER", "Ads providers:" + providers);
				
		// Setup WebView for Gameloft ads
		glAdView = new WebView1(myActivity.getApplicationContext());
		glAdView.getSettings().setJavaScriptEnabled(true);
		glAdView.setWebViewClient(new adWebViewClient());
		glAdView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		glAdView.setBackgroundColor(Color.TRANSPARENT);
		
	#if (TARGET_API_LEVEL_INT >= 21)
		if (Build.VERSION.SDK_INT >= 21) {
			glAdView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
	#endif
	
		if (Build.VERSION.SDK_INT >= 14)  //fix for banner appearing half and half
			glAdView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);        
		
		glAdView.setOnTouchListener(new View.OnTouchListener() {

		    public boolean onTouch(View v, MotionEvent event) {
		      return (event.getAction() == MotionEvent.ACTION_MOVE);
		    }
		  });

		// Setup layout
		display = ((WindowManager) myActivity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		DBG("ADSERVER", "++++++++++++++++++++ metrics density: " + metrics.density);
		
		if (bannerSize == BANNER_SMALL)
			lpBanner = new RelativeLayout.LayoutParams((int) (320 * metrics.density), (int) (50 * metrics.density));
		else
			lpBanner = new RelativeLayout.LayoutParams((int) (448 * metrics.density), (int) (70 * metrics.density));
		
		switch(bannerPosition)
		{
		case TOP_CENTER:
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lpBanner.addRule(RelativeLayout.CENTER_HORIZONTAL);
			break;
		case TOP_LEFT:				
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			break;
		case TOP_RIGHT:
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			break;
		case BOTTOM_CENTER:
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lpBanner.addRule(RelativeLayout.CENTER_HORIZONTAL);
			break;
		case BOTTOM_LEFT:
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			break;
		case BOTTOM_RIGHT:
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lpBanner.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			break;
		}
		
		try
		{		
			// Ad Views added to main ViewGroup, hidden
			mainView.addView(glAdView, lpBanner);
			glAdView.setVisibility(View.GONE);
			
		#if ADS_USE_TAPJOY
			tapjoyAdView = new RelativeLayout(myActivity);
			tapjoyAdView.setVisibility(View.GONE);
			mainView.addView(tapjoyAdView, lpBanner);	
		#endif
		
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
		
		if (bannerSize == BANNER_LARGE)
			GAMELOFT_AD_URL_TEMPLATE = GAMELOFT_AD_URL_TEMPLATE + "&width=448";
    }
	
	/** 
	 * Displays the ad banner.
	 * @param mainView The activity's main ViewGroup, to which the banner is attached.
	 * */	
	public void ShowBanner(ViewGroup mainView)
	{
		if (currentLanguage < 0 || currentLanguage >= TXT_LANGUAGE_LIST.length)
			currentLanguage = 0;
		
		if (!isAdLoading)
		{    	
			HideBanner(mainView);
			isAdLoading = true;
			showBannerThread = new Thread(new ShowBannerThread(mainView));
			showBannerThread.start();
		}
	}
	
	class ShowBannerThread implements Runnable
	{
		// The activity's main ViewGroup, to which the banner is attached.
		private ViewGroup MainView;
		
		ShowBannerThread(ViewGroup mainView)
		{
			MainView = mainView; 
		}
		
		public void run()
		{
			boolean show = true;
			if (isPAU)
			{
				if (!wasPAUConfigRead)
					setPAUBooleans();
				
				if (wasPAUConfigRead)
				{
					if (shouldCheckPAUBanners)
						show = false;
				}
				else
					show = false;
			}
			
			if (show)
			{
				shouldHideBanner = false;
				
				String UDID = Device.getDeviceId();
				
				String deviceName = Build.MANUFACTURER + "_" + Build.MODEL;
				String deviceFW = Build.VERSION.RELEASE;
		
				AD_REQUEST_URL = AD_REQUEST_URL_TEMPLATE.replace("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage]);
				AD_REQUEST_URL = AD_REQUEST_URL.replace("COUNTRY", java.util.Locale.getDefault().getCountry());
				AD_REQUEST_URL = AD_REQUEST_URL.replace("GAME_CODE", GameloftGameCode);
				AD_REQUEST_URL = AD_REQUEST_URL.replace("ANDROIDID", Device.getAndroidId());
				#if (HDIDFV_UPDATE == 2)
				AD_REQUEST_URL = AD_REQUEST_URL.replace("UDID", Device.getSerial());
				#else
				AD_REQUEST_URL = AD_REQUEST_URL.replace("UDID", UDID);
				#endif
				#if HDIDFV_UPDATE
				AD_REQUEST_URL = AD_REQUEST_URL.replace("HDIDFV", Device.getHDIDFV());
				DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
				#endif
				AD_REQUEST_URL = AD_REQUEST_URL.replace("DEVICE_NAME", deviceName);
				AD_REQUEST_URL = AD_REQUEST_URL.replace("FIRMWARE", deviceFW);
				AD_REQUEST_URL = AD_REQUEST_URL.replace("GAMEVERSION", GameVersion);
				AD_REQUEST_URL = AD_REQUEST_URL.replaceAll(" ", "");
				AD_REQUEST_URL += (isPAU ? "&is_pau=1" : "&is_pau=0");
				
				GAMELOFT_AD_URL = GAMELOFT_AD_URL_TEMPLATE.replace("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage]);
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("COUNTRY", java.util.Locale.getDefault().getCountry());
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("GAME_CODE", GameloftGameCode);
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("ANDROIDID", Device.getAndroidId());
				#if (HDIDFV_UPDATE == 2)
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("UDID", Device.getSerial());
				#else
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("UDID", UDID);
				#endif
				#if HDIDFV_UPDATE
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("HDIDFV", Device.getHDIDFV());
				DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
				#endif
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("DEVICE_NAME", deviceName);
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("FIRMWARE", deviceFW);
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replace("GAMEVERSION", GameVersion);
				GAMELOFT_AD_URL = GAMELOFT_AD_URL.replaceAll(" ", "");		
				GAMELOFT_AD_URL += (isPAU ? "&is_pau=1" : "&is_pau=0");
				
			#if USE_IN_GAME_BROWSER
				GAMELOFT_AD_URL += "&ingamebrowser=1";
			#endif
				
				// Requests response from Gameloft servers regarding which type of ads to display, 
				// then displays ad banners in the order specified by the response
				DBG("ADSERVER", "********************|| ad request: ||******************** " + AD_REQUEST_URL);
				String adresponse = getHttpResponse(AD_REQUEST_URL);
				
				if (adresponse != null)
				{ 
					DBG("ADSERVER", "********************|| ad response: ||******************** " + adresponse);
					
					String [] adresponse_array = adresponse.split("\\|");
									
					for (int i = 0; i < adresponse_array.length; i++)
					{
						DBG("ADSERVER", "********************|| Attempt to load: " + adresponse_array[i]);
						if (ShowBannerByType(adresponse_array[i]))
						{		    			
							break;
						}
					}
				}
				else DBG("ADSERVER", "********************|| Http response: ||******************** no response ");
			}
			else
			{
				DBG("ADSERVER", "PAU: Banners disabled");
			}
			
			isAdLoading = false;
		}
		
		/**
		 * Attempts to display an ad banner.
		 * @param type The type of ad banner to be displayed. Valid values are "GAMELOFT", "TAPJOY"
		 * @return true if the operation was successful, false if the operation failed.
		 */
		public boolean ShowBannerByType(String type)
	    {
	    	boolean result = false;
	    	
	    	if	(type.equals("GAMELOFT")) {
	    		result = ShowGameloftBanner();
	    	}
		#if ADS_USE_TAPJOY
	    	else if (type.equals("TAPJOY")) {	
	    		result = ShowTapjoyBanner();
	    	}
		#endif
	    	else result = false;
	    	
			if (result) 
				DBG("ADSERVER", "********************|| ad type loaded: ||******************** " + type);
			
	    	return result;
	    }
	    
		/**
		 * Displays a Gameloft banner. 
	     * @return true if successful, false if not successful.
		 */
		private boolean ShowGameloftBanner() 
		{
			try
			{
				canShowAd = false;
				currentAdType = "GAMELOFT_LOADING";
				
				DBG("ADSERVER", "+++++++++++++++++ GAMELOFT_AD_URL = " + GAMELOFT_AD_URL);
				DBG("ADSERVER", "+++++++++++++++++ Encrypting query parameters......");
				String [] split_url = GAMELOFT_AD_URL.split("[?]");
				GAMELOFT_AD_URL = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
				DBG("ADSERVER", "+++++++++++++++++ GAMELOFT_AD_URL encrypted: " + GAMELOFT_AD_URL);	
				
				final String f_url = GAMELOFT_AD_URL;
				glAdView.post(new Runnable()
				{
					public void run() {
						glAdView.loadUrl(f_url);
					}
				});
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
				
				if (canShowAd && !shouldHideBanner)
				{
					glAdView.post(new Runnable() { public void run() 
					{
						glAdView.setVisibility(View.VISIBLE);		
					}});	

					currentAdType = "GAMELOFT";
					return true;
				}
				else {
					currentAdType = "none";
					return false;
				}
			}
			catch(Exception e)
			{
				DBG_EXCEPTION(e);
			}
			currentAdType = "none";
			return false;
		}
	
	#if ADS_USE_TAPJOY
	    /**
	     * Attempt to display a Tapjoy banner.
	     * @return true if successful, false if not successful.
	     */
	    private boolean ShowTapjoyBanner() 
		{
			canShowAd = false;
			myActivity.runOnUiThread(new Runnable() { public void run() 
			{
				TapjoyConnect.getTapjoyConnectInstance().getDisplayAd(myActivity, tapjoyNotifier);	
			}});
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
			
			if (canShowAd && !shouldHideBanner)
			{
				if (tapjoyAdView != null)
				{
					MainView.post(new Runnable() { public void run() 
					{						
						tapjoyAdView.setVisibility(View.VISIBLE);
					}});
					currentAdType = "TAPJOY";
					return true;
				}
				else return false;
			}			
			else return false;
		}
	#endif
	}

	/** 
	 * Hides the currently displayed ad banner.
	 * @param mainView The activity's main ViewGroup, to which the ad banners are attached.
	 */
	public void HideBanner(final ViewGroup mainView)
	{
		shouldHideBanner = true;
		
		try {
			showBannerThread.interrupt();
		} catch (Exception e) {}
		
		if (!isAdLoading)
		{    	
			myActivity.runOnUiThread(new Runnable() { public void run() 
			{
				glAdView.loadData("", "text/html", null); //load empty page so javascript doesn't continue to run in the background
				glAdView.setVisibility(View.GONE); //hide webview
				
			#if ADS_USE_TAPJOY
				if (tapjoyAdView != null)
				{
					try {
						tapjoyAdView.removeAllViews();
						tapjoyAdView.setVisibility(View.GONE);
					} catch(Exception e) {}
				}
			#endif
			}});
			
			currentAdType = "none";
		}
	}
	
	public static void staticShowInterstitialWithTags(String tags) {
		if (s_classInstance != null) {
			s_classInstance.ShowInterstitialWithTags(tags);
		}
	}
	
	public void ShowInterstitialWithTags(String tags)
	{
		ShowInterstitial("&tags=" + tags);
	}
	
	public void ShowInterstitial()
	{
		ShowInterstitial(null);
	}
	
	private void ShowInterstitial(final String extras)
	{
		if (currentLanguage < 0 || currentLanguage >= TXT_LANGUAGE_LIST.length)
			currentLanguage = 0;
		
		if (!isInterstitialLoading)
		{
			isInterstitialLoading = true;
			new Thread( new Runnable() 
			{
				public void run()
				{
					boolean show = true;
					if (isPAU)
					{
						if (!wasPAUConfigRead)
							setPAUBooleans();
						
						if (wasPAUConfigRead)
						{
							if (shouldCheckPAUInterstitials)
								show = false;
						}
						else
							show = false;
					}
					
					if (show)
					{				
						String UDID = Device.getDeviceId();
						
						String deviceName = Build.MANUFACTURER + "_" + Build.MODEL;
						String deviceFW = Build.VERSION.RELEASE;
						String country = java.util.Locale.getDefault().getCountry();
				
						AD_REQUEST_URL = AD_REQUEST_URL_TEMPLATE.replace("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage]);
						AD_REQUEST_URL = AD_REQUEST_URL.replace("COUNTRY", java.util.Locale.getDefault().getCountry());
						AD_REQUEST_URL = AD_REQUEST_URL.replace("GAME_CODE", GameloftGameCode);
						AD_REQUEST_URL = AD_REQUEST_URL.replace("ANDROIDID", Device.getDeviceId());
						#if (HDIDFV_UPDATE == 2)
						AD_REQUEST_URL = AD_REQUEST_URL.replace("UDID", Device.getSerial());
						#else
						AD_REQUEST_URL = AD_REQUEST_URL.replace("UDID", UDID);
						#endif
						#if HDIDFV_UPDATE
						AD_REQUEST_URL = AD_REQUEST_URL.replace("HDIDFV", Device.getHDIDFV());
						DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
						#endif
						AD_REQUEST_URL = AD_REQUEST_URL.replace("DEVICE_NAME", deviceName);
						AD_REQUEST_URL = AD_REQUEST_URL.replace("FIRMWARE", deviceFW);
						AD_REQUEST_URL = AD_REQUEST_URL.replace("GAMEVERSION", GameVersion);
						AD_REQUEST_URL = AD_REQUEST_URL.replaceAll(" ", "");
						AD_REQUEST_URL += "&interstitials=1";
						AD_REQUEST_URL += (isPAU ? "&is_pau=1" : "&is_pau=0");
						
						if (extras != null)
						{
							AD_REQUEST_URL += extras;
						}
						
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL_TEMPLATE;
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("VERSION", GameVersion);
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("LANG", TXT_LANGUAGE_LIST[currentLanguage]);
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("COUNTRY", country);
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("FROM", GameloftGameCode);
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("DEVICE", deviceName);
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("FIRMWARE", deviceFW);
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("ANDROIDID", Device.getDeviceId());
						#if (HDIDFV_UPDATE == 2)
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("UDIDPHONE", Device.getSerial());
						#else
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("UDIDPHONE", UDID);
						#endif
						#if HDIDFV_UPDATE
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replace("HDIDFV", Device.getHDIDFV());
						DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
						#endif
						GAMELOFT_INTERSTITIAL_URL = GAMELOFT_INTERSTITIAL_URL.replaceAll(" ", "");
						GAMELOFT_INTERSTITIAL_URL += (isPAU ? "&is_pau=1" : "&is_pau=0");
						
						Display display = ((WindowManager) myActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
						int SCR_H = display.getHeight(), SCR_W = display.getWidth();
						
						if (Build.VERSION.SDK_INT == 11 || Build.VERSION.SDK_INT == 12) {
							SCR_H -= 48;
						} else if (deviceName.toLowerCase().contains("kindle")) {
							SCR_H -= 20;
						}
						
						GAMELOFT_INTERSTITIAL_URL += "&width=" + SCR_W + "&height=" + SCR_H;
						
						// Requests response from Gameloft servers regarding which type of interstitials to display, 
						// then displays interstitials in the order specified by the response
						DBG("ADSERVER", "********************|| interstitial request: " + AD_REQUEST_URL);
						String adresponse = getHttpResponse(AD_REQUEST_URL);
						
						if (adresponse != null && !adresponse.equals("0"))
						{ 
							DBG("ADSERVER", "********************|| interstitial response: " + adresponse);
							
							String [] adresponse_array = adresponse.replace("INTERSTITIALS:", "").split("\\|");
											
							for (int i = 0; i < adresponse_array.length; i++)
							{
								DBG("ADSERVER", "********************|| Attempt to load: " + adresponse_array[i]);
								if (ShowInterstitialByType(adresponse_array[i]))
								{		    			
									break;
								}
							}
						}
						else DBG("ADSERVER", "********************|| Http response: " + adresponse);
					}
					else
					{
						DBG("ADSERVER", "PAU: interstitials disabled");
					}
					
					isInterstitialLoading = false;
				}				
			}).start();
		}
	}
	
	private boolean ShowInterstitialByType(String type)
	{
		if (type.equals("GAMELOFT"))
		{
			DBG("ADSERVER", "+++++++++++++++++ GAMELOFT_INTERSTITIAL_URL = " + GAMELOFT_INTERSTITIAL_URL);
			DBG("ADSERVER", "+++++++++++++++++ Encrypting query parameters......");
			String [] split_url = GAMELOFT_INTERSTITIAL_URL.split("[?]");
			String GAMELOFT_INTERSTITIAL_URL = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
			DBG("ADSERVER", "+++++++++++++++++ GAMELOFT_INTERSTITIAL_URL encrypted: " + GAMELOFT_INTERSTITIAL_URL);	
			
			Intent i = new Intent(myActivity, AdServerInterstitial.class);
			i.putExtra("interstitial_url", GAMELOFT_INTERSTITIAL_URL);
			i.putExtra("orientation", (myActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			myActivity.startActivity(i);
			
			DBG("ADSERVER", "+++++++++++++++++ Loaded interstitial: GAMELOFT");
			
			return true;
		}
	#if ADS_USE_YUME
		else if (type.equals("YUME"))
		{
			if (bIsYuMeInitialized && bIsYuMePrefetch && isYumeReady)
			{
				myActivity.startActivity(new Intent(myActivity, YuMeActivity.class));
				DBG("ADSERVER", "+++++++++++++++++ Loaded interstitial: YUME");
				return true;
			}
			else
			{
				DBG("ADSERVER", "+++++++++++++++++ Failed to load interstitial: YUME");
				return false;
			}
		}
	#endif		
	#if ADS_USE_CHARTBOOST
		else if (type.equals("CHARTBOOST"))
		{
			myActivity.runOnUiThread(new Runnable() { public void run()
			{
				CBHandle.showInterstitial();
			}});
			DBG("ADSERVER", "+++++++++++++++++ Loaded interstitial: CHARTBOOST");
			return true;
		}
	#endif
	#if ADS_USE_TAPJOY
		else if (type.equals("TAPJOY"))
		{
			TapjoyConnect.getTapjoyConnectInstance().getFullScreenAd(tapjoyFSANotifier);
			bInterstitialResponse = false;
			bInterstitialSuccess = false;
			while (!bInterstitialResponse)
			{
				try {
					Thread.sleep(50);
				} catch(Exception e) { }
			}
			if (bInterstitialSuccess)
			{
				DBG("ADSERVER", "+++++++++++++++++ Loaded interstitial: TAPJOY");
				return true;
			}
			else
			{
				DBG("ADSERVER", "+++++++++++++++++ Interstitial failed: TAPJOY");
				return false;
			}
		}
	#endif
	#if ADS_USE_FLURRY
		if (type.equals("FLURRY"))
		{
			if (isActive && flurryInterstitialIsReady)
			{
				DBG("ADSERVER", "********************|| Showing free cash: Flurry");
				FlurryAds.displayAd(myActivity, flurryInterstitialSpace, mView);
				return true;
			}
			else
			{
				DBG("ADSERVER", "********************|| Flurry interstitial not available (App is active:" + isActive + "; Interstitial was loaded:" + flurryInterstitialIsReady);
				return false;
			}
		}
	#endif	
	
		return false;
	}

	/**
	 * Queries the Gameloft server regarding which Free Cash offers should be displayed. 
	 * Sets showFreeCash to false if the HTTP response is null, or to true, otherwise.
	 * @param mainView The activity's main ViewGroup, to which the ad banners are attached.
	 */
	public void GetFreeCash()
	{
		if (currentLanguage < 0 || currentLanguage >= TXT_LANGUAGE_LIST.length)
			currentLanguage = 0;
		
		new Thread(new Runnable() { public void run() 
		{
			String deviceName = Build.MANUFACTURER + "_" + Build.MODEL;
			String deviceFW = Build.VERSION.RELEASE;
		
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL_TEMPLATE.replace("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage]);
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("COUNTRY", java.util.Locale.getDefault().getCountry());
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("GAME_CODE", GameloftGameCode);
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("ANDROIDID", Device.getAndroidId());
			#if (HDIDFV_UPDATE == 2)
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("UDID", Device.getSerial());
			#else
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("UDID", Device.getDeviceId());
			#endif
			#if HDIDFV_UPDATE
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("HDIDFV", Device.getHDIDFV());
			DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
			#endif
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("DEVICE_NAME", deviceName);
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("FIRMWARE", deviceFW);
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("GAMEVERSION", GameVersion);
			AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replaceAll(" ", "");
			
			AD_REQUEST_FREE_CASH_URL += (isPAU ? "&is_pau=1" : "&is_pau=0");
		
			DBG("ADSERVER", "********************|| Checking free cash...");					
			DBG("ADSERVER", "********************|| free cash request: ||******************** " + AD_REQUEST_FREE_CASH_URL);
			String response = getHttpResponse(AD_REQUEST_FREE_CASH_URL);		
			
			DBG("ADSERVER", "********************|| free cash response: ||******************** " + response);
						
			if (response != null && !response.equals("") && !response.equals("0")) {
				showFreeCash = true;
			}
			else {
				showFreeCash = false;
				DBG("ADSERVER", "********************|| no free cash ");
			}			
			
		}}).start();
	}
	
	public void OpenFreeCash()
	{
		if (currentLanguage < 0 || currentLanguage >= TXT_LANGUAGE_LIST.length)
			currentLanguage = 0;
		
		//if (showFreeCash) 
		{
			new Thread(new Runnable() 
			{ 
				public void run()
				{
					String deviceName = Build.MANUFACTURER + "_" + Build.MODEL;
					String deviceFW = Build.VERSION.RELEASE;
				
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL_TEMPLATE.replace("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage]);
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("COUNTRY", java.util.Locale.getDefault().getCountry());
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("GAME_CODE", GameloftGameCode);
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("ANDROIDID", Device.getAndroidId());
					#if (HDIDFV_UPDATE == 2)
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("UDID", Device.getSerial());
					#else
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("UDID", Device.getDeviceId());
					#endif
					#if HDIDFV_UPDATE
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("HDIDFV", Device.getHDIDFV());
					DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
					#endif
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("DEVICE_NAME", deviceName);
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("FIRMWARE", deviceFW);
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replace("GAMEVERSION", GameVersion);
					AD_REQUEST_FREE_CASH_URL = AD_REQUEST_FREE_CASH_URL.replaceAll(" ", "");
					
					AD_REQUEST_FREE_CASH_URL += (isPAU ? "&is_pau=1" : "&is_pau=0");
					
					DBG("ADSERVER", "********************|| Loading free cash...");					
					DBG("ADSERVER", "********************|| free cash request: ||******************** " + AD_REQUEST_FREE_CASH_URL);
					String response = getHttpResponse(AD_REQUEST_FREE_CASH_URL);		
					
					DBG("ADSERVER", "********************|| free cash response: ||******************** " + response);
					
					boolean wasAnyAvailable = false;
							
					if (response != null && !response.equals("") && !response.equals("0")) 
					{
						showFreeCash = true;			
						final String _response = response.replace("FREE:", "");
						
						//myActivity.runOnUiThread(new Runnable() { public void run()
						{
							String [] response_array = _response.split("\\|");
							
							for (int i = 0; i < response_array.length; i++)
							{
								wasAnyAvailable = ShowFreeCashByType(response_array[i]);
								if (wasAnyAvailable)
								{		    			
									break;
								}
							}
						}
						//});
					}
					else 
					{
						showFreeCash = false;
						DBG("ADSERVER", "********************|| no free cash available for: ||******************** " + response);
					}
					
					if (response == null)
					{
						myActivity.runOnUiThread(new Runnable() { public void run() {
							try {
								AlertDialog dialog = new AlertDialog.Builder(myActivity)
										.setPositiveButton(myActivity.getString(TXT_OK[currentLanguage]), null)
										.setMessage(myActivity.getString(TXT_NETERROR[currentLanguage]))
										.create();
								dialog.setCancelable(true);
								dialog.setCanceledOnTouchOutside(false);
								dialog.show();
							} catch (Exception e) {
							}
						}});
					}
					else 
					{
						if (!wasAnyAvailable) 
						{
							myActivity.runOnUiThread(new Runnable() { public void run() {
								try {
									AlertDialog dialog = new AlertDialog.Builder(myActivity)
											.setPositiveButton(myActivity.getString(TXT_OK[currentLanguage]), null)
											.setMessage(myActivity.getString(TXT_NOFREECASH[currentLanguage]))
											.create();
									dialog.setCancelable(true);
									dialog.setCanceledOnTouchOutside(false);
									dialog.show();
								} catch (Exception e) {
								}
							}});
						}
					}
				}
				
				
			}).start();	
		}
	}
	
	private boolean ShowFreeCashByType(String type)
	{
		DBG("ADSERVER", "********************|| Attempt to load free cash: " + type);
		
		if (type.equals("GL_VIDEOSWALL"))
		{
			Intent i = new Intent(myActivity, AdServerVideos.class);
			i.putExtra("action", "offers");
			i.putExtra("game_code", GameloftGameCode);
			i.putExtra("game_ver", GameVersion);
			i.putExtra("lang", TXT_LANGUAGE_LIST[currentLanguage]);
			myActivity.startActivity(i);
			
			long started = System.currentTimeMillis();
			wasAdServerVideosLoaded = false;
			wasAdServerVideosError = false;
			while(!wasAdServerVideosLoaded) {
				//wait
				try { Thread.sleep(100); } catch(Exception e) { }				
			}
			if (wasAdServerVideosError)
				return false;
			else
				return true;
		}
		
		if (type.equals("GL_DIRECTVIDEOS"))
		{
			Intent i = new Intent(myActivity, AdServerVideos.class);
			i.putExtra("action", "direct");
			i.putExtra("game_code", GameloftGameCode);
			i.putExtra("game_ver", GameVersion);
			i.putExtra("lang", TXT_LANGUAGE_LIST[currentLanguage]);
			myActivity.startActivity(i);
			
			long started = System.currentTimeMillis();
			wasAdServerVideosLoaded = false;
			wasAdServerVideosError = false;
			while(!wasAdServerVideosLoaded) {
				//wait
				try { Thread.sleep(100); } catch(Exception e) { }				
			}
			if (wasAdServerVideosError)
				return false;
			else
				return true;
		}
					
	#if ADS_USE_TAPJOY
		if (type.equals("TAPOFFERS") || type.equals("TAPVIDEO"))
		{
			DBG("ADSERVER", "********************|| Showing free cash: Tapjoy");
			myActivity.runOnUiThread(new Runnable() { public void run() {
				TapjoyConnect.getTapjoyConnectInstance().showOffers();
			}});
			return true;
		}
	#endif
	#if ADS_USE_ADCOLONY
		if (type.equals("ADCOLONY"))
		{
			final AdColonyV4VCAd ad = new AdColonyV4VCAd();
			if (ad.isReady()) 
			{
				DBG("ADSERVER", "********************|| Showing free cash: AdColony");
				myActivity.runOnUiThread(new Runnable() { public void run() {
					ad.withListener(AdCoVideoListener).show();
				}});
				return true;
			}
			else
			{
				DBG("ADSERVER", "********************|| AdColony video not avalable");
				return false;
			}
		}
	#endif
	#if ADS_USE_FLURRY
		if (type.equals("FLURRY"))
		{
			if (isActive && flurryOfferwallIsReady)
			{
				DBG("ADSERVER", "********************|| Showing free cash: Flurry");
				myActivity.runOnUiThread(new Runnable() { public void run() {
					FlurryAds.displayAd(myActivity, flurryOfferwallSpace, mView);
				}});
				return true;
			}
			else
			{
				DBG("ADSERVER", "********************|| Flurry free cash not available (App is active:" + isActive + "; Free cash was loaded:" + flurryOfferwallIsReady);
				return false;
			}
		}
	#endif	
	
		DBG("ADSERVER", "********************|| Free cash not supported: " + type);
		return false;
	}
	
	public static void OpenGameloftCinema(String game_code, String game_ver)
	{
		Intent i = new Intent(SUtils.getContext(), AdServerVideos.class);
		i.putExtra("action", "cinema");
		i.putExtra("game_code", game_code);
		i.putExtra("game_ver", game_ver);
		SUtils.getContext().startActivity(i);
	}
	
	public boolean CheckCinemaAvailable()
	{
		if (currentLanguage < 0 || currentLanguage >= TXT_LANGUAGE_LIST.length)
			currentLanguage = 0;
		
		String deviceName = Build.MANUFACTURER + "_" + Build.MODEL;
		String deviceFW = Build.VERSION.RELEASE;

		AD_REQUEST_URL = AD_REQUEST_URL_TEMPLATE.replace("LANGUAGE", TXT_LANGUAGE_LIST[currentLanguage]);
		AD_REQUEST_URL = AD_REQUEST_URL.replace("COUNTRY", java.util.Locale.getDefault().getCountry());
		AD_REQUEST_URL = AD_REQUEST_URL.replace("GAME_CODE", GameloftGameCode);
		AD_REQUEST_URL = AD_REQUEST_URL.replace("ANDROIDID", Device.getAndroidId());
		#if (HDIDFV_UPDATE == 2)
		AD_REQUEST_URL = AD_REQUEST_URL.replace("UDID", Device.getSerial());
		#else
		AD_REQUEST_URL = AD_REQUEST_URL.replace("UDID", Device.getDeviceId());
		#endif
		#if HDIDFV_UPDATE
		AD_REQUEST_URL = AD_REQUEST_URL.replace("HDIDFV", Device.getHDIDFV());
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#endif
		AD_REQUEST_URL = AD_REQUEST_URL.replace("DEVICE_NAME", deviceName);
		AD_REQUEST_URL = AD_REQUEST_URL.replace("FIRMWARE", deviceFW);
		AD_REQUEST_URL = AD_REQUEST_URL.replace("GAMEVERSION", GameVersion);
		AD_REQUEST_URL = AD_REQUEST_URL.replaceAll(" ", "");
		AD_REQUEST_URL += "&cinema=1";
		AD_REQUEST_URL += (isPAU ? "&is_pau=1" : "&is_pau=0");
		
		DBG("ADSERVER", "********************|| Loading free cash...");					
		DBG("ADSERVER", "********************|| free cash request: ||******************** " + AD_REQUEST_URL);
		String response = getHttpResponse(AD_REQUEST_URL);		
		
		DBG("ADSERVER", "********************|| free cash response: ||******************** " + response);
		
		if (response != null)
		{
			if (!response.equals("0"))
				return true;
		}
		return false;
	}
	
	private class adWebViewClient extends WebViewClient 
	{
		boolean hasError = false;
	
		@Override
		public void onPageFinished(WebView view, String url) 
		{			
			if (currentAdType == "GAMELOFT_LOADING")
			{
				if (hasError == true) {
					canShowAd = false;
				}
				else 
					canShowAd = true;
				
				try {
					showBannerThread.interrupt();
				} catch (Exception e) {
				}
			}	
			
			hasError = false;
		}
		
		@Override
		public void onReceivedError (WebView view, int errorCode, String description, String failingUrl)
		{
			hasError = true;
			if (currentAdType == "GAMELOFT")
				view.setVisibility(View.GONE);
			
			DBG("ADSERVER", "**********************|| adWebViewClient onReceivedError: ||**********************" + description);
		}
		
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
		{
			handler.proceed();
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, final String url) 
		{
			DBG("ADSERVER", "++++++++++++++++ shouldOverrideUrlLoading: " + url);
			try 
			{		
				if (url.startsWith("goto:"))
				{		
					//av TODO
					//CLASS_NAME.m_sInstance.splashScreenFunc(url.replace("goto:", "")); // This calls the method public void splashScreenFunc(String name) from the game's main activity class (CLASS_NAME). Make sure to implement it.								
				}
			#if USE_IN_GAME_BROWSER
				else if (url.startsWith("browser:")) 
				{
					InGameBrowser.showInGameBrowserWithUrl(url.replace("browser:", ""));
				}
			#endif
				else if (url.startsWith("skt:"))
				{
					DBG("ADSERVER", "++++++++++++++++ SKT URL DETECTED, TRYING TO REDIRECT: " + url);
					
					Intent intent = myActivity.getPackageManager().getLaunchIntentForPackage("com.skt.skaf.A000Z00040");
					String strProductID = url.replace("skt:", "");
					
					if (intent != null)
					{
						DBG("ADSERVER", "++++++++++++++++ LAUNCH INTENT NOT NULL... TRYING TO START T-STORE ACTIVITY...");
						
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						intent.setClassName("com.skt.skaf.A000Z00040", "com.skt.skaf.A000Z00040.A000Z00040" );
						intent.setAction("COLLAB_ACTION");
						intent.putExtra("com.skt.skaf.COL.URI", ("PRODUCT_VIEW/" + strProductID + "/0").getBytes());
						intent.putExtra("com.skt.skaf.COL.REQUESTER", "A000Z00040");
						
						myActivity.startActivity(intent);
					}
					else
					{
						DBG("ADSERVER", "++++++++++++++++ LAUNCH INTENT NULL... T-STORE APP IS NOT INSTALLED ON DEVICE... PLEASE CHECK...");
						Toast.makeText(myActivity, "T store not found...", 0).show();
					}
				}
				else if (url.contains("ingameads.gameloft.com/redir/"))
				{
					view.loadUrl(url);
				}
				else if (url.startsWith("link:"))
				{
					myActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace("link:", ""))));
				}
				else
				{
					myActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				}
			}
			catch (Exception e) {
				DBG("ADSERVER", "Exception occured: " + e.getMessage());
				DBG_EXCEPTION(e);
				hasError = true;
			}
			return true;
		}
	}
	
#if ADS_USE_ADCOLONY
	private void updateCash(int amount, String type, String source)
	{
		//av TODO
		//CLASS_NAME.UpdateCashWithAmount(amount, type, source); // must be implemented to handle currency gains from free cash
	}
#endif
	
#if ADS_USE_ADCOLONY	
	private AdColonyAdListener AdCoVideoListener = new AdColonyAdListener()
	{
		public void onAdColonyAdStarted(AdColonyAd ad)
		{
			DBG("ADSERVER", "ADCOLONY: VIDEO STARTED");
		}
		public void onAdColonyAdAttemptFinished(AdColonyAd ad)
		{
			DBG("ADSERVER", "ADCOLONY: VIDEO FINISHED");
		}
	};
	
	private AdColonyV4VCListener AdCoVCListener = new AdColonyV4VCListener() 
	{
		@Override
		public void onAdColonyV4VCReward (AdColonyV4VCReward reward)		
		{
			//DBG("ADSERVER", "ADCOLONY V4VC: success: " + success + " ; name: " + name + " ; amount: " + amount);
			if (reward.success()) 
			{
				DBG("ADSERVER", "****************| ADCOLONY V4VC: Earned " + reward.amount() + " " + reward.name());
				
				#if HDIDFV_UPDATE
				final String RequestUrl = AD_LINK_VIDEO_CAP.replace("GAME_CODE", GameloftGameCode)
					#if (HDIDFV_UPDATE == 2)
						.replace("UDID", Device.getSerial())
					#else
						.replace("UDID", Device.getDeviceId())
					#endif
						.replace("HDIDFV", Device.getHDIDFV())
						.replace("ANDROIDID", Device.getAndroidId());
					
				DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
				#else
				final String RequestUrl = AD_LINK_VIDEO_CAP.replace("GAME_CODE", GameloftGameCode).replace("UDID", Device.getDeviceId()).replace("ANDROIDID", Device.getAndroidId());;
				#endif
				
				new Thread(new Runnable() { public void run() 
				{
					try {
						DBG("ADSERVER", "++++++++++++++++ video capping script executed: " + RequestUrl);
						String response = getHttpResponse(RequestUrl);
						DBG("ADSERVER", "++++++++++++++++ video capping response: " + response);
					} catch (Exception e) { 
						DBG("ADSERVER", "+++++++++++++++++ exception when executing script: " + e);
						e.printStackTrace();
					}						
				}}).start();
				
				#if !ADCOLONY_SERVERSIDE_REWARDS
					updateCash(reward.amount(), reward.name(), "ADCOLONY");
				#endif
			}
			else
			{
				DBG("ADSERVER", "****************| ADCOLONY V4VC: Unexpected error, can't earn virtual currency.");
			}			
		}				
	};
#endif

#if ADS_USE_TAPJOY
    private TapjoyDisplayAdNotifier tapjoyNotifier = new TapjoyDisplayAdNotifier() 
    {			
		public void getDisplayAdResponseFailed(String error) 
		{
			canShowAd = false;
			DBG("ADSERVER", "***************************TAPJOY FAIL, error: " + error);
			try {
				showBannerThread.interrupt();
			} catch (Exception e) {
			}
		}
		
		public void getDisplayAdResponse(View adView) 
		{
			canShowAd = true;
			final View newAdView = adView;
			myActivity.runOnUiThread(new Runnable() {
				public void run() {
					tapjoyAdView.removeAllViews();
					
					int desired_width = lpBanner.width;
					int desired_height = lpBanner.height;
					int ad_width = newAdView.getLayoutParams().width;
					
					newAdView.setLayoutParams(new LayoutParams(desired_width, desired_height));
					((WebView) newAdView).setInitialScale((int)FloatMath.floor(((float) desired_width / ad_width) * 100));
					
					if (Build.VERSION.SDK_INT >= 19) {
						((WebView) newAdView).getSettings().setUseWideViewPort(true);
						((WebView) newAdView).getSettings().setLoadWithOverviewMode(true);
					}
					
					tapjoyAdView.setBackgroundColor(0);
					tapjoyAdView.addView(newAdView);
				}
			});
			try {
				showBannerThread.interrupt();
			} catch (Exception e) {
			}
		}
	};

	TapjoyFullScreenAdNotifier tapjoyFSANotifier = new TapjoyFullScreenAdNotifier() 
	{		
		public void getFullScreenAdResponse() 
		{
			bInterstitialResponse = true;
			if (isActive)
			{
				bInterstitialSuccess = true;
				TapjoyConnect.getTapjoyConnectInstance().showFullScreenAd();
			}
			else 
			{
				bInterstitialSuccess = false;
				DBG("ADSERVER", "TAPJOY FULLSCREEN AD ERROR: isActive is false");
			}
		}
		
		public void getFullScreenAdResponseFailed(int statusCode) 
		{
			bInterstitialResponse = true;
			bInterstitialSuccess = false;
			String fullscreenAdErrorCode = "";
			switch (statusCode) {
				case TapjoyFullScreenAdStatus.STATUS_NETWORK_ERROR:
					fullscreenAdErrorCode = "FULLSCREEN AD ERROR: Network error";
					break;
				case TapjoyFullScreenAdStatus.STATUS_NO_ADS_AVAILABLE:
					fullscreenAdErrorCode = "FULLSCREEN AD ERROR: No ads available";
					break;
				case TapjoyFullScreenAdStatus.STATUS_SERVER_ERROR:
					fullscreenAdErrorCode = "FULLSCREEN AD ERROR: Server error";
					break;
			}
			DBG("ADSERVER", "TAPJOY FULLSCREEN AD ERROR: " + fullscreenAdErrorCode);
		}		
	};
	
	private TapjoyVideoNotifier tapjoyVideoNotifier = new TapjoyVideoNotifier() 
	{
		public void videoError(int statusCode) 
		{
			String videoErrorCode = "";
			switch (statusCode) {
				case TapjoyVideoStatus.STATUS_MEDIA_STORAGE_UNAVAILABLE:
					videoErrorCode = "VIDEO ERROR: No SD card or external media storage mounted on device";
					break;
				case TapjoyVideoStatus.STATUS_NETWORK_ERROR_ON_INIT_VIDEOS:
					videoErrorCode = "VIDEO ERROR: Network error on init videos";
					break;
				case TapjoyVideoStatus.STATUS_UNABLE_TO_PLAY_VIDEO:
					videoErrorCode = "VIDEO ERROR: Error playing video";
					break;
			}
			DBG("ADSERVER", "TAPJPOY VIDEO ERROR: " + videoErrorCode);				
		}
		
		public void videoStart() 
		{ 
			DBG("ADSERVER", "TAPJOY VIDEO START"); 
		}

		public void videoComplete() 
		{ 
			DBG("ADSERVER", "TAPJOY VIDEO COMPLETE");
		}
	};
#endif

#if ADS_USE_CHARTBOOST
	private ChartboostDelegate CBDelegate = new ChartboostDelegate() 
	{
		/*
		 * Chartboost delegate methods
		 * 
		 * Implement the delegate methods below to finely control Chartboost's behavior in your app
		 * 
		 * Minimum recommended: shouldDisplayInterstitial()
		 */
	
		
		/* 
		 * shouldDisplayInterstitial(String location)
		 *
		 * This is used to control when an interstitial should or should not be displayed
		 * If you should not display an interstitial, return NO
		 *
		 * For example: during gameplay, return NO.
		 *
		 * Is fired on:
		 * - showInterstitial()
		 * - Interstitial is loaded & ready to display
		 */
		@Override
		public boolean shouldDisplayInterstitial(String location) {
			return true;
		}
		
		/*
		 * shouldRequestInterstitial(String location)
		 * 
		 * This is used to control when an interstitial should or should not be requested
		 * If you should not request an interstitial from the server, return NO
		 *
		 * For example: user should not see interstitials for some reason, return NO.
		 *
		 * Is fired on:
		 * - cacheInterstitial()
		 * - showInterstitial() if no interstitial is cached
		 * 
		 * Notes: 
		 * - We do not recommend excluding purchasers with this delegate method
		 * - Instead, use an exclusion list on your campaign so you can control it on the fly
		 */
		@Override
		public boolean shouldRequestInterstitial(String location) {
			return true;
		}
		
		/*
		 * didCacheInterstitial(String location)
		 * 
		 * Passes in the location name that has successfully been cached
		 * 
		 * Is fired on:
		 * - cacheInterstitial() success
		 * - All assets are loaded
		 * 
		 * Notes:
		 * - Similar to this is: cb.hasCachedInterstitial(String location) 
		 * Which will return true if a cached interstitial exists for that location
		 */
		@Override
		public void didCacheInterstitial(String location) {
			INFO("ADSERVER", "CHARTBOOST INTERSTITIAL '" + location + "' CACHED");
		}

		/*
		 * didFailToLoadInterstitial(String location)
		 * 
		 * This is called when an interstitial has failed to load for any reason
		 * 
		 * Is fired on:
		 * - cacheInterstitial() failure
		 * - showInterstitial() failure if no interstitial was cached
		 * 
		 * Possible reasons:
		 * - No network connection
		 * - No publishing campaign matches for this user (go make a new one in the dashboard)
		 */
		@Override
		public void didFailToLoadInterstitial(String location) {
			INFO("ADSERVER", "CHARTBOOST INTERSTITIAL '" + location + "' FAILED TO LOAD");
		}

		/*
		 * didDismissInterstitial(String location)
		 *
		 * This is called when an interstitial is dismissed
		 *
		 * Is fired on:
		 * - Interstitial click
		 * - Interstitial close
		 *
		 * #Pro Tip: Use the delegate method below to immediately re-cache interstitials
		 */
		@Override
		public void didDismissInterstitial(String location) {			
			// Immediately re-caches an interstitial
			CBHandle.cacheInterstitial(location);			
			INFO("ADSERVER", " CHARTBOOST INTERSTITIAL '" + location + "' DISMISSED, PRECACHING ANOTHER ONE");
		}

		/*
		 * didCloseInterstitial(String location)
		 *
		 * This is called when an interstitial is closed
		 *
		 * Is fired on:
		 * - Interstitial close
		 */
		@Override
		public void didCloseInterstitial(String location) {
			
		}

		/*
		 * didClickInterstitial(String location)
		 *
		 * This is called when an interstitial is clicked
		 *
		 * Is fired on:
		 * - Interstitial click
		 */
		@Override
		public void didClickInterstitial(String location) {
		
		}

		/*
		 * didShowInterstitial(String location)
		 *
		 * This is called when an interstitial has been successfully shown
		 *
		 * Is fired on:
		 * - showInterstitial() success
		 */
		@Override
		public void didShowInterstitial(String location) {
			INFO("ADSERVER", "CHARTBOOST INTERSTITIAL '" + location + "' SHOWN");
		}

		/*
		 * More Apps delegate methods
		 */
		
		/*
		 * shouldDisplayLoadingViewForMoreApps()
		 *
		 * Return NO to prevent the pretty More-Apps loading screen
		 *
		 * Is fired on:
		 * - showMoreApps()
		 */
		@Override
		public boolean shouldDisplayLoadingViewForMoreApps() {
			return true;
		}

		/*
		 * shouldRequestMoreApps()
		 * 
		 * Return NO to prevent a More-Apps page request
		 *
		 * Is fired on:
		 * - cacheMoreApps()
		 * - showMoreApps() if no More-Apps page is cached
		 */
		@Override
		public boolean shouldRequestMoreApps() {
			return true;
		}

		/*
		 * shouldDisplayMoreApps()
		 * 
		 * Return NO to prevent the More-Apps page from displaying
		 *
		 * Is fired on:
		 * - showMoreApps() 
		 * - More-Apps page is loaded & ready to display
		 */
		@Override
		public boolean shouldDisplayMoreApps() {
			return true;
		}

		/*
		 * didFailToLoadMoreApps()
		 * 
		 * This is called when the More-Apps page has failed to load for any reason
		 * 
		 * Is fired on:
		 * - cacheMoreApps() failure
		 * - showMoreApps() failure if no More-Apps page was cached
		 * 
		 * Possible reasons:
		 * - No network connection
		 * - No publishing campaign matches for this user (go make a new one in the dashboard)
		 */
		@Override
		public void didFailToLoadMoreApps() {
			
		}

		/*
		 * didCacheMoreApps()
		 * 
		 * Is fired on:
		 * - cacheMoreApps() success
		 * - All assets are loaded
		 */
		@Override
		public void didCacheMoreApps() {
			
		}

		/*
		 * didDismissMoreApps()
		 *
		 * This is called when the More-Apps page is dismissed
		 *
		 * Is fired on:
		 * - More-Apps click
		 * - More-Apps close
		 */
		@Override
		public void didDismissMoreApps() {
			
		}

		/*
		 * didCloseMoreApps()
		 *
		 * This is called when the More-Apps page is closed
		 *
		 * Is fired on:
		 * - More-Apps close
		 */
		@Override
		public void didCloseMoreApps() {
			
		}

		/*
		 * didClickMoreApps()
		 *
		 * This is called when the More-Apps page is clicked
		 *
		 * Is fired on:
		 * - More-Apps click
		 */
		@Override
		public void didClickMoreApps() {
			
		}

		/*
		 * didShowMoreApps()
		 *
		 * This is called when the More-Apps page has been successfully shown
		 *
		 * Is fired on:
		 * - showMoreApps() success
		 */
		@Override
		public void didShowMoreApps() {
			
		}

		/*
		 * shouldRequestInterstitialsInFirstSession()
		 *
		 * Return false if the user should not request interstitials until the 2nd startSession()
		 * 
		 */
		@Override
		public boolean shouldRequestInterstitialsInFirstSession() {
			return true;
		}
	};
#endif
	
#if ADS_USE_FLURRY
	private FlurryAdListener flurryAdListener = new FlurryAdListener() 
	{
		public void spaceDidReceiveAd(String adSpace) {
			// called when the ad has been prepared, ad can be displayed:
			if (adSpace.equals(flurryInterstitialSpace)) {
				flurryInterstitialIsReady = true;
			}
			else if (adSpace.equals(flurryOfferwallSpace)) {
				flurryOfferwallIsReady = true;
			}
		}		

		public void spaceDidFailToReceiveAd(String adSpace) {
			if (adSpace.equals(flurryInterstitialSpace)) {
				flurryInterstitialIsReady = false;
			}
			else if (adSpace.equals(flurryOfferwallSpace)) {
				flurryOfferwallIsReady = false;
			}
		}
		
		public void onRendered(String adSpace) {
			
		}
		
		public void onAdClicked(String adSpace) {
			
		}

		public void onAdClosed(String adSpace) {
			
		}

		public void onAdOpened(String adSpace) {
			
		}

		public void onApplicationExit(String adSpace) {
			
		}

		public void onRenderFailed(String adSpace) {
			
		}

		public void onVideoCompleted(String adSpace) {
			
		}

		public boolean shouldDisplayAd(String adSpace, FlurryAdType type) {
			return true;
		}	
	};
#endif
	
	/** 
	 * Executes the script at the URL and returns the HTTP response. 
	 * @param RequestUrl The URL address where the script is executed.
	 * @return The HTTP response, as String, or null if the operation failed.
	 */
	public static String getHttpResponse(String RequestUrl)
    {
    	String response_text = null;
		BufferedReader stream_in = null;    	
		try
		{
			DBG("ADSERVER", "+++++++++++++++++ Encrypting query parameters......");
			String [] split_url = RequestUrl.split("[?]");
			String FinalRequestUrl = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
			DBG("ADSERVER", "+++++++++++++++++ Executing encrypted request: " + FinalRequestUrl);
			
			HttpClient client= new DefaultHttpClient();
			HttpGet request = new HttpGet(FinalRequestUrl);
			HttpResponse response = client.execute(request);			
			stream_in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer buffer = new StringBuffer("");
            String line = "";
            while ((line = stream_in.readLine()) != null)
            {
            	buffer.append(line);
            }
            stream_in.close();
            response_text = buffer.toString();
		} catch (Exception e) { 
			DBG_EXCEPTION(e);
		}		
		finally 
		{
            if (stream_in != null) 
            {
                try 
                {
                	stream_in.close();
                } 
                catch (Exception e) { 
                	DBG_EXCEPTION(e);
                }
            }
		}
		
		return response_text;
    }
};