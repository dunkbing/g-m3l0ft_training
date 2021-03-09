package APP_PACKAGE;

import java.io.*;
import java.net.*;
import java.util.*;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.*;
import android.net.wifi.*;
import android.net.http.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings.*;
import android.webkit.*;
import android.widget.RelativeLayout;

import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.Encrypter;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.PackageUtils.*;

public class SplashScreenActivity extends Activity
{
	public static int 		currentLanguage = 0;
	public static boolean 	gIsRunning = false;
	private static boolean 	gIsShowing = false;
	
	private Display 		display;	
	public 	RelativeLayout 	mView;
	public  WebView 		mWebView;
	
	static int 				SCR_W = 800;
	static int 				SCR_H = 480;
	static int				webHeight;
	static int				webWidth;
		
	public static String 	K_LINK_TOPDEALS = "";
	#if HDIDFV_UPDATE
	public static String 	K_LINK_TOPDEALS_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/splashscreen_view.php?from=FROM&country=COUNTRY&lg=LANG&udid=UDIDPHONE&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&igp_rev=1005&os=android";
	public static String 	K_LINK_HOCSCREEN_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/hocscreen.php?from=FROM&country=COUNTRY&lg=LANG&udid=UDIDPHONE&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&igp_rev=1005&os=android";
	#else
	public static String 	K_LINK_TOPDEALS_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/splashscreen_view.php?from=FROM&country=COUNTRY&lg=LANG&udid=UDIDPHONE&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&igp_rev=1005&os=android";
	public static String 	K_LINK_HOCSCREEN_TEMPLATE = "https://ingameads.gameloft.com/redir/ads/hocscreen.php?from=FROM&country=COUNTRY&lg=LANG&udid=UDIDPHONE&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&igp_rev=1005&os=android";
	#endif
	
	public static String [] TXT_IGP_LANGUAGES =
	{
		"EN",
		"FR",
		"DE",
		"IT",
		"SP",
		"JP",
		"KR",
		"CN",
		"BR",
		"RU",
		"TR", 
		"AR", 
		"TH", 
		"ID", 
		"VI", 
		"ZT"
	};
	
	private static boolean isPau = false;
	
	public static void setIsPAU(boolean bIsPAU)
	{
		isPau = bIsPAU;
	}
	
	public SplashScreenActivity()
	{		
		SUtils.setContext((Context)this);
	}
	
	@Override
	public void onDestroy()
	{
		DBG("SPLASH_SCREEN", "********* onDestroy(): activity finished ********");
		super.onDestroy();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
// 	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
// 		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
// 	#endif
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
// 	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
// 		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
// 	#endif
	}
	
	class WebView1 extends WebView {

		public WebView1(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchKeyEventPreIme(KeyEvent event) {
			switch(event.getKeyCode())
			{		
			case KeyEvent.KEYCODE_BUTTON_A:
			case KeyEvent.KEYCODE_BUTTON_B:
			case KeyEvent.KEYCODE_BUTTON_X:
			case KeyEvent.KEYCODE_BUTTON_Y:
			case KeyEvent.KEYCODE_BUTTON_SELECT:
			case KeyEvent.KEYCODE_BUTTON_START:
			case KeyEvent.KEYCODE_BUTTON_L1:
			case KeyEvent.KEYCODE_BUTTON_R1:
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					SplashScreenActivity.this.onKeyDown(event.getKeyCode(), event);
				else if ((event.getAction() == KeyEvent.ACTION_UP))
					SplashScreenActivity.this.onKeyUp(event.getKeyCode(), event);
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:			
			default:
				return super.dispatchKeyEventPreIme(event);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		if (AndroidUtils.GetContext() == null)
		{
			closeSplash();
			return;
		}
		
		if (gIsRunning) {
			finish();
		}		
		
		gIsRunning = true;
		
		Intent sender = getIntent();
		int lang = lang = sender.getExtras().getInt("language", currentLanguage);
		
		if(lang < 0 || lang >= TXT_IGP_LANGUAGES.length)
			lang = 0;
		
		boolean wasCached = sender.getExtras().getBoolean("wasCached", false);
		String filePath = sender.getExtras().getString("filePath");
		
		display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		SCR_H = display.getHeight();
		SCR_W = display.getWidth();
		
		mView = new RelativeLayout(this);
		
		mWebView = new WebView1(this);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setBackgroundColor(0);
		mWebView.setInitialScale(100);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAppCacheEnabled(false);		
		mWebView.getSettings().setSupportZoom (false);
		mWebView.getSettings().setBuiltInZoomControls(false);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		
	#if (TARGET_API_LEVEL_INT >= 21)
		if (Build.VERSION.SDK_INT >= 21) {
			mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
	#endif
	
		mWebView.setWebViewClient(new glWebViewClient());
		mWebView.setWebChromeClient(new android.webkit.WebChromeClient() 
		{
			public boolean onConsoleMessage(android.webkit.ConsoleMessage cm) {
				DBG("Web Console", cm.message() + " -- From line " + cm.lineNumber() + " of page.");
				return true;
			}
		});
		
		mWebView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
		
		int max_width = Math.round(0.8f * SCR_W);
		int max_height = Math.round(0.835f * SCR_H);
		
		webWidth = webHeight = 0;
		
		float golden_number = 1520.0f / 1008.0f;
		
		if (SCR_W < SCR_H)
			golden_number = 1008.0f / 1520.0f;
		
		while(webWidth <= max_width && webHeight <= max_height)
		{
			webHeight++;
			webWidth = Math.round(webHeight * golden_number);
		}
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(webWidth, webHeight);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		mView.addView(mWebView, lp);
		
		setContentView(mView);
		
		if (wasCached && filePath != null)
		{
			mWebView.loadUrl("file://" + filePath);
		}
		else
		{
			show(lang);
		}
	}
	
	public static int isActive()
	{
		if (gIsRunning)
			return 1;
		else
			return 0;
	}
	
	public static void SetWSLanguage(int lang)
	{
		currentLanguage = lang;
		
		if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
			currentLanguage = 0;		
	}
	
	void show(int lang)
	{
		currentLanguage = lang;
		
		if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
			currentLanguage = 0;
				
		String UDID = Device.getDeviceId(); 
		
		String country = java.util.Locale.getDefault().getCountry();
		String deviceType = Build.MANUFACTURER+"_"+Build.MODEL;
		String deviceFW = Build.VERSION.RELEASE;
		
		K_LINK_TOPDEALS = K_LINK_TOPDEALS_TEMPLATE
							.replace("VERSION", GAME_VERSION_NAME_LETTER)
							.replace("LANG", TXT_IGP_LANGUAGES[currentLanguage])
							.replace("COUNTRY", country)
							#if USE_ANDROID_TV_IGPCODE
							.replace("FROM", GGC_GAME_CODE_TV)
							#else
							.replace("FROM", GGC_GAME_CODE)
							#endif
							
							.replace("DEVICE", deviceType)
							.replace("FIRMWARE", deviceFW)
							.replace("ANDROIDID", Device.getAndroidId())
							#if (HDIDFV_UPDATE == 1)
							.replace("UDIDPHONE", UDID)
							.replace("HDIDFV", Device.getHDIDFV())
							#elif (HDIDFV_UPDATE == 2)
							.replace("UDIDPHONE", Device.getSerial())
							.replace("HDIDFV", Device.getHDIDFV())
							#else
							.replace("UDIDPHONE", UDID)							
							#endif
							.replaceAll(" ", "");
		#if HDIDFV_UPDATE
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#endif
		K_LINK_TOPDEALS += ("&width=" + webWidth);
		K_LINK_TOPDEALS += ("&height=" + webHeight);
		
	#if USE_IN_GAME_BROWSER
		K_LINK_TOPDEALS += "&ingamebrowser=1";
	#endif		
		K_LINK_TOPDEALS += (isPau ? "&is_pau=1" : "&is_pau=0");
	
		DBG("SPLASH_SCREEN", "+++++++++++++++++ Loading Welcome Screen: " + K_LINK_TOPDEALS);
		DBG("SPLASH_SCREEN", "+++++++++++++++++ Encrypting query parameters......");
		String [] split_url = K_LINK_TOPDEALS.split("[?]");
		K_LINK_TOPDEALS = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
		DBG("SPLASH_SCREEN", "+++++++++++++++++ Encrypted URL: " + K_LINK_TOPDEALS);			
		
		mWebView.loadUrl(K_LINK_TOPDEALS);
	}
	
	public static void cacheAndStart(final int lang)
	{
		startSplash(lang);
	}
	
	public static void startSplash(final int lang)
	{
		currentLanguage = lang;
		
		if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
			currentLanguage = 0;
				
		String UDID = Device.getDeviceId(); 
		
		DBG("SPLASH_SCREEN", "UDID = " + UDID);
		
		String country = java.util.Locale.getDefault().getCountry();
		DBG("SPLASH_SCREEN","device settings detected..3");
		String deviceType = Build.MANUFACTURER+"_"+Build.MODEL;
		String deviceFW = Build.VERSION.RELEASE;
		Display display = ((WindowManager) SUtils.getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();		
		
		SCR_H = display.getHeight();
		SCR_W = display.getWidth();
		
		int max_width = Math.round(0.8f * SCR_W);
		int max_height = Math.round(0.835f * SCR_H);
		
		webWidth = webHeight = 0;
		
		float golden_number = 1520.0f / 1008.0f;
		
		while(webWidth <= max_width && webHeight <= max_height)
		{
			webHeight++;
			webWidth = Math.round(webHeight * golden_number);
		}
		
		K_LINK_TOPDEALS = K_LINK_TOPDEALS_TEMPLATE
							.replace("VERSION", GAME_VERSION_NAME_LETTER)
							.replace("LANG", TXT_IGP_LANGUAGES[currentLanguage])
							.replace("COUNTRY", country)
							#if USE_ANDROID_TV_IGPCODE
							.replace("FROM", GGC_GAME_CODE_TV)
							#else
							.replace("FROM", GGC_GAME_CODE)
							#endif							
							.replace("DEVICE", deviceType)
							.replace("FIRMWARE", deviceFW)
							.replace("ANDROIDID", Device.getAndroidId())
							#if (HDIDFV_UPDATE == 1)
							.replace("UDIDPHONE", UDID)
							.replace("HDIDFV", Device.getHDIDFV())							
							#elif (HDIDFV_UPDATE == 2)
							.replace("UDIDPHONE", Device.getSerial())				
							.replace("HDIDFV", Device.getHDIDFV())	
							#else
							.replace("UDIDPHONE", UDID)
							#endif
							.replaceAll(" ", "");
		#if (HDIDFV_UPDATE == 1)
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#endif
		K_LINK_TOPDEALS += ("&width=" + webWidth);
		K_LINK_TOPDEALS += ("&height=" + webHeight);
		K_LINK_TOPDEALS += "&dl=1";

	#if USE_IN_GAME_BROWSER
		K_LINK_TOPDEALS += "&ingamebrowser=1";
	#endif
		
		K_LINK_TOPDEALS += (isPau ? "&is_pau=1" : "&is_pau=0");
				
		DBG("SPLASH_SCREEN", "+++++++++++++++++ Caching Welcome Screen: " + K_LINK_TOPDEALS);
		DBG("SPLASH_SCREEN", "+++++++++++++++++ Encrypting query parameters......");
		String [] split_url = K_LINK_TOPDEALS.split("[?]");
		K_LINK_TOPDEALS = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
		DBG("SPLASH_SCREEN", "+++++++++++++++++ Encrypted URL: " + K_LINK_TOPDEALS);			
		
		new Thread(new Runnable()
		{
			public void run()
			{
				String extStorage = "";
				boolean isExtStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
				
				Intent intent = new Intent(SUtils.getContext(), SplashScreenActivity.class);
				intent.putExtra("language", lang);				
				
				if (isExtStorageAvailable)
				{
					if (Build.VERSION.SDK_INT >= 8) {
						extStorage = SUtils.getContext().getExternalFilesDir(null) + "/welcome";
					} else {
						extStorage = Environment.getDownloadCacheDirectory() + "/welcome";
					}
					
					HTMLResourceDownloader htmlDownloader = new HTMLResourceDownloader(K_LINK_TOPDEALS, extStorage, "welcome.html");
					htmlDownloader.parseAndDownload();					
					if(!htmlDownloader.anyProblem())
					{
						DBG("SPLASH_SCREEN", ":::::::: No problem occured while caching. Open Welcome Screen.");
						intent.putExtra("filePath", extStorage + "/welcome.html");
						intent.putExtra("wasCached", true);						
						SUtils.getContext().startActivity(intent);
					}
					else
					{
						DBG("SPLASH_SCREEN", ":::::::: Problem occured while caching. Welcome Screen will not be displayed.");
					}
				}
				else
				{
					DBG("SPLASH_SCREEN", ":::::::: External storage is not mounted. Starting without cache.");
					SUtils.getContext().startActivity(intent);
				}
				
			}
		}).start();		
	}
	
	private static native void splashScreenFuncGLOT(String url);
	
	private static boolean isDownloadInProgress = false;
			 
	public static void downloadWS(String idArrayString)
	{
		if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
			currentLanguage = 0;
		
		downloadWS(idArrayString, currentLanguage);
	}
	
	public static void downloadWS(final String idArrayString, final int langIndex)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				DBG("SPLASH_SCREEN", ":::::::: downloadWS for IDs: " + idArrayString + " and lang " + langIndex);
				if (isDownloadInProgress)
				{
					DBG("SPLASH_SCREEN", ":::::::: Waiting for all other downloads to finish...");
					while (isDownloadInProgress) {
						try { Thread.sleep(100); } catch (Exception e) { }
					} 
					DBG("SPLASH_SCREEN", ":::::::: Done.");	
				}
				
				isDownloadInProgress = true;
				currentLanguage = langIndex;
				
				if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
					currentLanguage = 0;
		
				String[] idsArray = idArrayString.replaceAll(" ", "").split(",");
				
				for (String wsId : idsArray)			
				{				
					String UDID = Device.getDeviceId(); 
					
					String country = java.util.Locale.getDefault().getCountry();
					String deviceType = Build.MANUFACTURER+"_"+Build.MODEL;
					String deviceFW = Build.VERSION.RELEASE;
					Display display = ((WindowManager) SUtils.getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();		
					SCR_H = display.getHeight();
					SCR_W = display.getWidth();
					webWidth = (int)(0.8 * SCR_W);
					webHeight = (int)(0.835 * SCR_H);		
					
					K_LINK_TOPDEALS = K_LINK_TOPDEALS_TEMPLATE
										.replace("VERSION", GAME_VERSION_NAME_LETTER)
										.replace("LANG", TXT_IGP_LANGUAGES[currentLanguage])
										.replace("COUNTRY", country)
										#if USE_ANDROID_TV_IGPCODE
										.replace("FROM", GGC_GAME_CODE_TV)
										#else
										.replace("FROM", GGC_GAME_CODE)
										#endif																
										.replace("DEVICE", deviceType)
										.replace("FIRMWARE", deviceFW)
										.replace("ANDROIDID", Device.getAndroidId())
										#if (HDIDFV_UPDATE == 1)
										.replace("UDIDPHONE", UDID)
										.replace("HDIDFV", Device.getHDIDFV())
										#elif (HDIDFV_UPDATE == 2)
										.replace("UDIDPHONE", Device.getSerial())
										.replace("HDIDFV", Device.getHDIDFV())
										#else
										.replace("UDIDPHONE", UDID)										
										#endif
										.replaceAll(" ", "");
					#if HDIDFV_UPDATE
					DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
					#endif
					K_LINK_TOPDEALS += ("&width=" + webWidth);
					K_LINK_TOPDEALS += ("&height=" + webHeight);
					K_LINK_TOPDEALS += ("&id=" + wsId);
					K_LINK_TOPDEALS += "&dl=1";

				#if USE_IN_GAME_BROWSER
					K_LINK_TOPDEALS += "&ingamebrowser=1";
				#endif
					
					K_LINK_TOPDEALS += (isPau ? "&is_pau=1" : "&is_pau=0");
							
					DBG("SPLASH_SCREEN", "+++++++++++++++++ Saving Welcome Screen to disk: " + K_LINK_TOPDEALS);
					String [] split_url = K_LINK_TOPDEALS.split("[?]");
					K_LINK_TOPDEALS = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
					DBG("SPLASH_SCREEN", "+++++++++++++++++ Encrypted URL: " + K_LINK_TOPDEALS);			
			
					String extStorage = "";
					boolean isExtStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);								
					
					if (isExtStorageAvailable)
					{
						if (Build.VERSION.SDK_INT >= 8) {
							extStorage = SUtils.getContext().getExternalFilesDir(null) + "/WelcomeScreens/" + wsId + "/" + TXT_IGP_LANGUAGES[currentLanguage];
						} else {
						#if USE_ANDROID_TV_IGPCODE
						extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE_TV + "/" + wsId + "/" + TXT_IGP_LANGUAGES[currentLanguage];
						#else
						extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE + "/" + wsId + "/" + TXT_IGP_LANGUAGES[currentLanguage];
						#endif
							
						}
						
						try
						{
							downloadFile(K_LINK_TOPDEALS, extStorage, "welcome.html");					
							DBG("SPLASH_SCREEN", ":::::::: Download complete...");							
						}
						catch (MalformedURLException e)
						{
							DBG("SPLASH_SCREEN", ":::::::: Welcome Screen not available, \"exit:\" redirect occured. Download failed.");
						}
						catch(Exception e)
						{
							DBG_EXCEPTION(e);				
							DBG("SPLASH_SCREEN", ":::::::: Welcome Screen not downloaded. Problem occured while downloading: " + e.getMessage());
						}
					}
					else
					{
						DBG("SPLASH_SCREEN", ":::::::: Welcome Screen not downloaded. External storage is not mounted.");
					}					
				}
				isDownloadInProgress = false;
			}
		}).start();	
	}
	
	private static void saveEtag(String etag, String directory) {
		DBG("SPLASH_SCREEN", "saveEtag: " + etag + " in directory: " + directory);
		FileWriter writer = null;
		try {
			writer = new FileWriter(new File(directory, "saved.etags"), false);
			writer.write(etag);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		} 
	}
	
	private static String getEtag(String directory) {
		FileReader reader = null;
		String etag = null;
    	try {
    		reader = new FileReader(new File(directory, "saved.etags"));
    		BufferedReader bReader = new BufferedReader(reader);
   		  	etag = bReader.readLine();
			reader.close();
    	} catch(Exception e) {	
			DBG_EXCEPTION(e);
			etag = null;
		} 
		DBG("SPLASH_SCREEN", "getEtag from directory: " + directory + " returned " + etag);	
    	return etag;
	}
	
	public static int isWSReady(String wsId)//default language EN 
	{
		if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
			currentLanguage = 0;
		
		return isWSReady(wsId, currentLanguage);
	}
	
	public static int isWSReady(String wsId, int langIndex)
	{
		DBG("SPLASH_SCREEN", ":::::::: isWSReady with ID:" + wsId + " and lang " + langIndex);
		String extStorage = "";
		boolean isExtStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		
		if (isExtStorageAvailable)
		{
			if (Build.VERSION.SDK_INT >= 8) {
				extStorage = SUtils.getContext().getExternalFilesDir(null) + "/WelcomeScreens/" + wsId + "/" + TXT_IGP_LANGUAGES[langIndex];
			} else {
			#if USE_ANDROID_TV_IGPCODE
				extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE_TV + "/" + wsId + "/" + TXT_IGP_LANGUAGES[langIndex];
			#else
				extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE + "/" + wsId + "/" + TXT_IGP_LANGUAGES[langIndex];
			#endif
				
			}
			
			File wsFolder = new File(extStorage);
			
			boolean isReady = wsFolder.exists() && (wsFolder.list() != null) && (wsFolder.list().length != 0);
			DBG("SPLASH_SCREEN", ":::::::: Returned " + isReady);
			if(isReady)
				return 1;
			else
				return 0;
			//return isReady;
		}
		else
		{
			DBG("SPLASH_SCREEN", ":::::::: External storage is not mounted. Returned false");
			//return false;
			return 0;
		}
	}
	
	public static void showLocalWS(String wsId)//default language EN 
	{
		if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
			currentLanguage = 0;
		
		showLocalWS(wsId, currentLanguage);
	}
	
	public static void showLocalWS(final String wsId, final int langIndex)
	{
		currentLanguage = langIndex;
		
		if(currentLanguage < 0 || currentLanguage >= TXT_IGP_LANGUAGES.length)
			currentLanguage = 0;
		
		if (gIsShowing) {
			return;
		}		
		gIsShowing = true;
		
		new Thread(new Runnable()
		{
			public void run()
			{
				DBG("SPLASH_SCREEN", ":::::::: showLocalWS with ID:" + wsId + " and lang:" + langIndex);
				if (isDownloadInProgress)
				{
					DBG("SPLASH_SCREEN", ":::::::: Waiting for all downloads to finish...");
					while(isDownloadInProgress) {
						try { Thread.sleep(50); } catch (Exception e) { }
					}
					DBG("SPLASH_SCREEN", ":::::::: Done.");	
				}
				
				if (isWSReady(wsId, langIndex) == 0)
				{
					DBG("SPLASH_SCREEN", ":::::::: Welcome screen not found, starting download...");
					downloadWS(wsId, langIndex);
					do {
						try { Thread.sleep(100); } catch (Exception e) { }
					}
					while(isDownloadInProgress);
				}
				
				String extStorage = "";
				boolean isExtStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
				
				if (isExtStorageAvailable)
				{
					if (Build.VERSION.SDK_INT >= 8) {
						extStorage = SUtils.getContext().getExternalFilesDir(null) + "/WelcomeScreens/" + wsId + "/" + TXT_IGP_LANGUAGES[langIndex];
					} else {
					#if USE_ANDROID_TV_IGPCODE
						extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE_TV + "/" + wsId + "/" + TXT_IGP_LANGUAGES[langIndex];
					#else
						extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE + "/" + wsId + "/" + TXT_IGP_LANGUAGES[langIndex];
					#endif
						
					}
					
					if ((new File(extStorage + "/welcome.html")).exists())
					{
						DBG("SPLASH_SCREEN", ":::::::: File was found. Showing downloaded welcome screen.");
						Intent intent = new Intent(SUtils.getContext(), SplashScreenActivity.class);
						intent.putExtra("language", currentLanguage);
						intent.putExtra("filePath", extStorage + "/welcome.html");
						intent.putExtra("wasCached", true);
						SUtils.getContext().startActivity(intent);						
					}
					else
					{			
						DBG("SPLASH_SCREEN", ":::::::: File not found. Welcome Screen not displayed");
					}
				}
				else
				{
					DBG("SPLASH_SCREEN", ":::::::: External storage is not mounted. Welcome Screen not displayed");
				}
				
				gIsShowing = false;
			}
		}).start();
	}
		
	public static void deleteWS(String idArrayString)
	{
		String[] idsArray = idArrayString.replaceAll(" ", "").split(",");
		
		DBG("SPLASH_SCREEN", ":::::::: deleteWS with IDs: " + idArrayString);
		
		for (String wsId : idsArray)
		{
			DBG("SPLASH_SCREEN", ":::::::: deleting ID: " + wsId);
			String extStorage = "";
			boolean isExtStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
			
			if (isExtStorageAvailable)
			{
				if (Build.VERSION.SDK_INT >= 8) {
					extStorage = SUtils.getContext().getExternalFilesDir(null) + "/WelcomeScreens/" + wsId;
				} else {
					#if USE_ANDROID_TV_IGPCODE
					extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE_TV + "/" + wsId;
					#else
					extStorage = Environment.getDownloadCacheDirectory() + "/WelcomeScreens/" + GGC_GAME_CODE + "/" + wsId;
					#endif
				}
				
				deleteDirWithSubdirs(new File(extStorage));
			}
			else
			{
				DBG("SPLASH_SCREEN", ":::::::: External storage is not mounted. Welcome Screen not deleted");
			}
		}
	}
	
	static void downloadFile(String remoteUrl, String localDir, String fileName) throws Exception
	{
		File localDirectory = new File(localDir);
		
		HttpURLConnection conn = null;
		BufferedInputStream in = null; 
		BufferedOutputStream out = null;
		try
		{	
			URL url = new URL(remoteUrl);
			conn = (HttpURLConnection)url.openConnection();	
			
			String etag = getEtag(localDir);
			if (etag != null)
				conn.setRequestProperty("If-None-Match", etag);
			
			int response = conn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK)
			{
				deleteDirWithSubdirs(localDirectory);
				localDirectory.mkdirs();
				
				String _etag = conn.getHeaderField("Etag");
				if (_etag != null)
					saveEtag(_etag, localDir);
		
				in = new BufferedInputStream(conn.getInputStream());
				out = new BufferedOutputStream(new FileOutputStream(localDir + "/" + fileName));
					
				int i;
				while ((i = in.read()) != -1) 
				{
					out.write(i);
				}
			}
			else {
				DBG("SPLASH_SCREEN", "Download aborted. HTTP response code is " + String.valueOf(response));
			}
		} 
		catch (Exception e) 
		{
			deleteDirWithSubdirs(localDirectory);			
			throw e;
		}
		finally
		{
			if (out != null) {
				out.flush();
				out.close();
			}
			if (in != null) {
				in.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	static void deleteDirWithSubdirs(File path)
	{
		try
		{
			if (path.isDirectory()) 
			{
				String[] children = path.list();
				for (int i = 0; i < children.length; i++) 
				{
					deleteDirWithSubdirs(new File(path, children[i]));
				}
			}
			final File to = new File(path.getAbsolutePath() + System.currentTimeMillis());
			path.renameTo(to);
			to.delete();
		}
		catch(Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}

#if USE_HOC_SCREEN
	public static void getHocScreen(final int lang, final HocScreenListener listener)
	{
		new Thread(new Runnable() { public void run()
		{
			String result = "null";
			
			String request = K_LINK_HOCSCREEN_TEMPLATE
						.replace("VERSION", GAME_VERSION_NAME_LETTER)
						.replace("LANG", TXT_IGP_LANGUAGES[lang])
						.replace("COUNTRY", java.util.Locale.getDefault().getCountry())
						#if USE_ANDROID_TV_IGPCODE
						.replace("FROM", GGC_GAME_CODE_TV)
						#else
						.replace("FROM", GGC_GAME_CODE)
						#endif
						
						.replace("DEVICE", Build.MANUFACTURER + "_" + Build.MODEL)
						.replace("FIRMWARE", Build.VERSION.RELEASE)
						.replace("ANDROIDID", Device.getAndroidId())
						#if (HDIDFV_UPDATE == 1)
						.replace("UDIDPHONE", Device.getDeviceId())
						.replace("HDIDFV", Device.getHDIDFV())
						#elif (HDIDFV_UPDATE == 2)
						.replace("UDIDPHONE", Device.getSerial())
						.replace("HDIDFV", Device.getHDIDFV())
						#else
						.replace("UDIDPHONE", Device.getDeviceId())
						#endif
						.replaceAll(" ", "");		
			#if HDIDFV_UPDATE 
			DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
			#endif
			DBG("SPLASH_SCREEN", "hoc screen request" + request);
			
			String response = getHttpResponse(request);
			DBG("SPLASH_SCREEN", "hoc screen respone: " + response);
			
			if (response != null) 
			{
				result = response;
			}
			else
			{
				result = "null";
			}
			
			listener.onResult(result);
		}}).start();
	}
	
	public interface HocScreenListener
	{
		public void onResult(String result);
	}
#endif
	
	void closeSplash()
	{
	#if USE_WELCOME_SCREEN_CRM
		splashScreenFuncGLOT("quit");
	#endif
		try
		{
			SplashScreenActivity.gIsRunning = false;
			finish();
		} catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{		
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_BUTTON_B:
			closeSplash();
			return true;
		case KeyEvent.KEYCODE_BUTTON_A:
			try {
				if (mWebView != null) {
					mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
					mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
				}
			} catch (Exception e) { }
			return true;
		case KeyEvent.KEYCODE_BUTTON_X:
		case KeyEvent.KEYCODE_BUTTON_Y:
		case KeyEvent.KEYCODE_BUTTON_SELECT:
		case KeyEvent.KEYCODE_BUTTON_START:
		case KeyEvent.KEYCODE_BUTTON_L1:
		case KeyEvent.KEYCODE_BUTTON_R1:
			return true;
		default:
			return super.onKeyUp(keyCode, event);
		}
	}
	
	private void startYoutube(String url) 
	{ 
		// default youtube app 
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); 
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY); 
		if (list.size() == 0) 
		{ 
			// default youtube app not present or doesn't conform to the standard we know 
			// use the web browser
			i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + url.replace("vnd.youtube:", "")));					
		} 
		startActivity(i);
	}

	private void startMarketApplication(String url) 
	{ 
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY); 
		if (list.size() == 0) 
		{ 
			// default market app not present or doesn't conform to the standard we know 
			// use the web browser
			i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/" + url.replace("market://", "")));					
		}		
		startActivity(i);
	}
	
	private void startAmazonStore(String url) 
	{ 
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY); 
		if (list.size() == 0) 
		{ 
			// default app not present or doesn't conform to the standard we know 
			// use the web browser
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace("amzn://", "http://")));					
		}		
		startActivity(i);
	}	
	
	public void LaunchPackage(String url)
	{	
		try
		{	
			Intent intent = new Intent(Intent.ACTION_MAIN);
			PackageManager manager = getPackageManager();
			intent = manager.getLaunchIntentForPackage(url);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			startActivity(intent);
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	}
	private String GetRedirectUrl(String url)
	{
		String redirect_url = null;
		HttpURLConnection httpConn = null;
		try {
			httpConn = (HttpURLConnection) (new URL(url)).openConnection();
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(false);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			redirect_url = httpConn.getHeaderField("Location");
		} catch (MalformedURLException e) {
			redirect_url = null;
		} catch (Exception e) {
			redirect_url = null;
		} finally {
			if (httpConn != null)
				httpConn.disconnect();
		}
		System.gc();
		
		return redirect_url;
	}
	
	private class glWebViewClient extends WebViewClient
	{
		boolean hasError = false;
		
		public void OpenBrowser(String url)
		{
			if(url == null || url.length() <= 0)
				return;

			try
			{
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
			}
			catch (Exception e)
			{
				DBG_EXCEPTION(e);
			}
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			DBG("SPLASH_SCREEN", "*************** shouldOverrideUrlLoading()********************");
			DBG("SPLASH_SCREEN", "*************** url: " + url + " ********************");
			
			if(url.startsWith("play:"))
			{
			#if USE_WELCOME_SCREEN_CRM
				splashScreenFuncGLOT(url);
			#endif
				LaunchPackage(url.replace("play:", "").split("[?]")[0]);
				closeSplash();
			}

			else if(url.startsWith("link:"))
			{
			#if USE_WELCOME_SCREEN_CRM
				splashScreenFuncGLOT(url);
			#endif
				OpenBrowser(url.replace("link:", ""));
				closeSplash();
			}
			
			else if (url.startsWith("exit:"))
			{
				closeSplash();
			}
			
			else if(url.startsWith("goto:"))
			{
				// This calls the method
				//		public void splashScreenFunc(String name)
				// from the game's main activity class (CLASS_NAME)
				// Make sure to implement it.				
				try
				{
				#if USE_WELCOME_SCREEN_CRM
					splashScreenFuncGLOT(url);
				#endif					
					JNIBridge.NativeSplashScreenFunc(url.replace("goto:", ""));
				}
				catch(Exception e)
				{
					DBG_EXCEPTION(e);
					DBG("SPLASH_SCREEN", "************************* \n ****** FAILED TO CALL METHOD splashScreenFunc FROM CLASS_NAME. IS IT IMPLEMENTED? **********");
				}
				closeSplash();
			}
		
		#if USE_IN_GAME_BROWSER
			else if (url.startsWith("browser:")) {
				InGameBrowser.showInGameBrowserWithUrl(url.replace("browser:", ""));
				closeSplash();
			}
		#endif
		
			else if(url.contains("ingameads.gameloft.com/redir/ads/splashscreen_click"))
			{
				view.loadUrl(url);			
			}
			
			else if(url.startsWith("market://"))
			{
			#if USE_WELCOME_SCREEN_CRM
				splashScreenFuncGLOT(url);
			#endif
				startMarketApplication(url);		
				closeSplash();		
			}
			else if (url.startsWith("amzn://"))
			{
			#if USE_WELCOME_SCREEN_CRM
				splashScreenFuncGLOT(url);
			#endif
				startAmazonStore(url);
				closeSplash();
			}
			else if(url.contains("ingameads.gameloft.com/redir/?from") && !url.contains("ctg=PLAY"))
			{
			#if USE_WELCOME_SCREEN_CRM
				splashScreenFuncGLOT(url);
			#endif
			
			#if USE_HEP_IGP_PORTAL
				String[] paramStrings = url.split("&");
				String newUrl = paramStrings[0];
				for(int i = 1; i < paramStrings.length; i++)
				{
					if(paramStrings[i].startsWith("op="))				
					{
						#if USE_HEP_EXT_IGPINFO
						newUrl =  newUrl + "&op=" + IGPPortal;
						#else
						newUrl =  newUrl + "&op=" + GGC_GAME_OPERATOR;	
						#endif //USE_HEP_EXT_IGPINFO
					}
					else
						newUrl =  newUrl + ("&"+paramStrings[i]);
				}
				url = newUrl;					
			#endif //USE_HEP_IGP_PORTAL
				final String f_url = url;
				new Thread(new Runnable()
				{
					public void run()
					{
						String redirect_url = GetRedirectUrl(f_url);
						if (redirect_url == null)
							redirect_url = f_url;
							
						OpenBrowser(redirect_url.replace("link:", ""));
					}
				}).start();
				closeSplash();				
			}
			else if (url.startsWith("vnd.youtube:"))
			{
			#if USE_WELCOME_SCREEN_CRM
				splashScreenFuncGLOT(url);
			#endif
				startYoutube(url);
				closeSplash();
			}
			else
			{
			#if IGP_SKT
				if(!url.contains("SKTMARKET") || url.contains("gameinformation") || url.contains("index.php")) // Open link without containing SKTMARKET
			#endif
				{
					view.loadUrl(url);
				}
			}
			
			return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			DBG("SPLASH_SCREEN", "*************** onPageStarted()********************");
			DBG("SPLASH_SCREEN", "*************** url: " + url + " ********************");
		}
		
		@Override
		public void onPageFinished(WebView view, String url)
		{
			DBG("SPLASH_SCREEN", "*************** onPageFinished()********************");
			DBG("SPLASH_SCREEN", "*************** url: " + url + " ********************");
			
			if (hasError)
			{
				closeSplash();
			}
			
			hasError = false;			
		}
		
		@Override
		public void onReceivedError (WebView view, int errorCode, String description, String failingUrl)
		{
			hasError = true;
			
			DBG("SPLASH_SCREEN", "***************onReceivedError: ***************" + description);
		}
		
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
		{
			handler.proceed();
		}
	};
	
	public static String getHttpResponse(String RequestUrl)
    {
    	String response_text = null;
		BufferedReader stream_in = null;    	
		try
		{
			DBG("SPLASH_SCREEN", "+++++++++++++++++ Encrypting query parameters......");
			String [] split_url = RequestUrl.split("[?]");
			String FinalRequestUrl = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
			DBG("SPLASH_SCREEN", "+++++++++++++++++ Executing encrypted request: " + FinalRequestUrl);
			
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
}