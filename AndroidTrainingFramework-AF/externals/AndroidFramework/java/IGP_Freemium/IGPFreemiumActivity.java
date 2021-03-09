package APP_PACKAGE;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.media.*;
import android.media.MediaPlayer.*;
import android.net.*;
import android.net.http.SslError;
import android.os.*;
import android.provider.Settings.Secure;
import android.net.wifi.WifiManager;
import android.telephony.*;
import android.util.*;
import android.view.*;
import android.view.View.OnKeyListener;
import android.webkit.*;
import android.webkit.WebSettings.*;
import android.widget.*;
import android.widget.RelativeLayout.*;

#if ADS_USE_FLURRY
import APP_PACKAGE.Flurry.GLFlurry;
#endif

import APP_PACKAGE.GLUtils.Encrypter;

import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;

public class IGPFreemiumActivity extends Activity
{
	public static boolean gIsRunning = false;
	public static boolean sb_isFocus = false;
	private int currentLanguage = 0;
	private boolean isPortrait = false;		
	private boolean bHasGoBack = false;

	#if FULL_SCREEN_IGP
	private RelativeLayout mVideoContainer = null;
	private VideoView mVideoView = null;
	private boolean isVideoPlaying = false;
	private boolean isBackPressed = false;
	#endif
	
	private int SCR_W = 800;
	private int SCR_H = 480;
	
	#if USE_HEP_EXT_IGPINFO
		public String IGPCode ;
		public String IGPPortal ;
	#endif //USE_HEP_EXT_IGPINFO
	
	#if HDIDFV_UPDATE
	private String K_LINK_IGP_TEMPLATE = "https://ingameads.gameloft.com/redir/freemium/hdfreemium.php?from=GAME_CODE&country=COUNTRY_DETECTED&lg=LANG&udid=UDIDPHONE&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&os=android"
	#else
	private String K_LINK_IGP_TEMPLATE = "https://ingameads.gameloft.com/redir/freemium/hdfreemium.php?from=GAME_CODE&country=COUNTRY_DETECTED&lg=LANG&udid=UDIDPHONE&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=VERSION&os=android"
	#endif

		#if FULL_SCREEN_IGP
		+ "&igp_rev=1006&conn=CONNSPEED";
		#else
		+ "&igp_rev=1005";
		#endif
	
	private String K_LINK_IGP = "";//"https://ingameads.gameloft.com/redir/android/index.php?from=TEST&lg=EN&udid=TEST&d=Samsung_S&f=2.2.1&ver=1.1.1&country=US";
	private String K_LINK_BACK = "https://signal-back.com";
	private String K_GAMEINFORMATIONS_PAGE = "https://ingameads.gameloft.com/redir/android/index.php?page=gameinformation";
	private String K_GETIT_PAGE = "https://ingameads.gameloft.com/redir/?from=";
	private String INSTALL_URL_IF_GAME_NOT_INSTALLED = null;

	private int [] TXT_LOADING =
	{
		R.string.IGP_LOADING_EN,
		R.string.IGP_LOADING_FR,
		R.string.IGP_LOADING_DE,
		R.string.IGP_LOADING_IT,
		R.string.IGP_LOADING_SP,
		R.string.IGP_LOADING_JP,
		R.string.IGP_LOADING_KR,
		R.string.IGP_LOADING_CN,
		R.string.IGP_LOADING_BR,
		R.string.IGP_LOADING_RU,
		R.string.IGP_LOADING_TR,
		R.string.IGP_LOADING_AR,
		R.string.IGP_LOADING_TH,
		R.string.IGP_LOADING_ID,
		R.string.IGP_LOADING_VI,
		R.string.IGP_LOADING_ZT
	};
	
	private int [] TXT_NET_ERROR =
	{
		R.string.IGP_NET_ERROR_EN,
		R.string.IGP_NET_ERROR_FR,
		R.string.IGP_NET_ERROR_DE,
		R.string.IGP_NET_ERROR_IT,
		R.string.IGP_NET_ERROR_SP,
		R.string.IGP_NET_ERROR_JP,
		R.string.IGP_NET_ERROR_KR,
		R.string.IGP_NET_ERROR_CN,
		R.string.IGP_NET_ERROR_BR,
		R.string.IGP_NET_ERROR_RU,
		R.string.IGP_NET_ERROR_TR,
		R.string.IGP_NET_ERROR_AR,
		R.string.IGP_NET_ERROR_TH,
		R.string.IGP_NET_ERROR_ID,
		R.string.IGP_NET_ERROR_VI,
		R.string.IGP_NET_ERROR_ZT
	};
	
	private int [] TXT_OK =
	{
		R.string.IGP_OK_EN,
		R.string.IGP_OK_FR,
		R.string.IGP_OK_DE,
		R.string.IGP_OK_IT,
		R.string.IGP_OK_SP,
		R.string.IGP_OK_JP,
		R.string.IGP_OK_KR,
		R.string.IGP_OK_CN,
		R.string.IGP_OK_BR,
		R.string.IGP_OK_RU,
		R.string.IGP_OK_TR,
		R.string.IGP_OK_AR,
		R.string.IGP_OK_TH,
		R.string.IGP_OK_ID,
		R.string.IGP_OK_VI,
		R.string.IGP_OK_ZT
	};
	
	private int [] IMG_LANDSCAPE = 
	{
		R.drawable.window_en,
		R.drawable.window_fr,
		R.drawable.window_de,
		R.drawable.window_it,
		R.drawable.window_sp,
		R.drawable.window_jp,
		R.drawable.window_kr,
		R.drawable.window_cn,
		R.drawable.window_br,
		R.drawable.window_ru,
		R.drawable.window_tr,
		R.drawable.window_en,
		R.drawable.window_en,
		R.drawable.window_en,
		R.drawable.window_en,
		R.drawable.window_cn
	};
	
	private int [] IMG_PORTRAIT = 
	{
		R.drawable.window_portrait_en,
		R.drawable.window_portrait_fr,
		R.drawable.window_portrait_de,
		R.drawable.window_portrait_it,
		R.drawable.window_portrait_sp,
		R.drawable.window_portrait_jp,
		R.drawable.window_portrait_kr,
		R.drawable.window_portrait_cn,
		R.drawable.window_portrait_br,
		R.drawable.window_portrait_ru,
		R.drawable.window_portrait_tr,
		R.drawable.window_portrait_en,
		R.drawable.window_portrait_en,
		R.drawable.window_portrait_en,
		R.drawable.window_portrait_en,
		R.drawable.window_portrait_cn
	};

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

	private Display display;
	private RelativeLayout mView;
	private WebView mWebView;
	private RelativeLayout mLayout;
	private ImageButton mCloseButton;

	public IGPFreemiumActivity()
	{		
		SUtils.setContext((Context)this);
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
					IGPFreemiumActivity.this.onKeyDown(event.getKeyCode(), event);
				else if ((event.getAction() == KeyEvent.ACTION_UP))
					IGPFreemiumActivity.this.onKeyUp(event.getKeyCode(), event);
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
	protected void onStop()
	{
		super.onStop();
// 	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
// 		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
// 	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onEndSession(this);
	#endif
	}
	@Override
	protected void onStart()
	{
		super.onStart();
// 	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
// 		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
// 	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onStartSession(this);
	#endif
	}
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	
		//av TODO: Check why the commented code was needed:
		// if (CLASS_NAME.m_sInstance == null)
		// {
			// OnBackKeyReleased();
			// return;
		// }
		
		display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
		Intent sender = getIntent();
		int lang = 0;
		#if USE_ANDROID_TV_IGPCODE
			String gamecode = GGC_GAME_CODE_TV;
		#else
			String gamecode = GGC_GAME_CODE; // long.huynhthanh - Edit
		#endif

		if (sender.getExtras() != null)
		{
			lang = sender.getExtras().getInt("language");
			if (lang < 0 || lang >= TXT_IGP_LANGUAGES.length)
				lang = 0;
			isPortrait = sender.getExtras().getBoolean("isPortrait");
			
			gamecode = sender.getExtras().getString("gamecode");
			if(gamecode == null)
				gamecode = GGC_GAME_CODE;
			#if USE_ANDROID_TV_IGPCODE
			gamecode = GGC_GAME_CODE_TV;
			#endif
			DBG("IGP_FREEMIUM", "++++++++++++++++ getExtras():");
			DBG("IGP_FREEMIUM", "++++++++++++++++ isPortrait: " + isPortrait);
			DBG("IGP_FREEMIUM", "++++++++++++++++ lang: " + lang);
				
			if (isPortrait == true)
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);				
				SCR_H = display.getHeight();
				SCR_W = display.getWidth();		
			}
			else
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);	
				SCR_H = display.getHeight();
				SCR_W = display.getWidth();			
			}
		}
		else
		{
			DBG("IGP_FREEMIUM", "++++++++++++++++ no extras bundle on intent");
			lang = currentLanguage;
			isPortrait = false;
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);	
			SCR_H = display.getHeight();
			SCR_W = display.getWidth();		
		}		
		
		mView = new RelativeLayout(this);
		mWebView = new WebView1(this);

	#if !FULL_SCREEN_IGP		
		mLayout = new RelativeLayout(this);
		mCloseButton = new ImageButton(this);
	#endif
	
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAppCacheEnabled(false);		
		mWebView.getSettings().setSupportZoom (false);
		mWebView.getSettings().setBuiltInZoomControls(false);
		mWebView.getSettings().setDefaultTextEncodingName("utf-8");		
		mWebView.getSettings().setLightTouchEnabled(true);
		mWebView.getSettings().setLoadsImagesAutomatically(true);
		mWebView.setWebChromeClient(new IgpWebChromeClient());
		mWebView.setWebViewClient(new IgpWebViewClient());

	#if FULL_SCREEN_IGP
		mWebView.getSettings().setPluginState(PluginState.ON);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(false);
		mWebView.setBackgroundColor(Color.argb(1, 255, 255, 255));
		mWebView.setVerticalScrollbarOverlay(true);
		mWebView.addJavascriptInterface(new IgpJsInterface(this, mView), "Android");
	#else
		mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY); 
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setBackgroundColor(0);
	#endif
		
		if (Build.VERSION.SDK_INT >= 19) {
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
		}
		
	#if (TARGET_API_LEVEL_INT >= 21)
		if (Build.VERSION.SDK_INT >= 21) {
			mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
	#endif
		
	#if (TARGET_API_LEVEL_INT >= 17)
		if (Build.VERSION.SDK_INT >= 17)
			mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
	#endif
		
	#if USE_IGP_REWARDS
		mWebView.addJavascriptInterface(new IGPInterface(), "JSInterface");
	#endif
	
		setContentView(mView);

#if USE_AUTO_ORIENTATION_SENSOR
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
 			this.setRequestedOrientation(USE_AUTO_ORIENTATION_SENSOR_TYPE);
 			this.getRequestedOrientation();
		}
#endif

		if(lang < 0 || lang > TXT_IGP_LANGUAGES.length)
			lang = 0;
		#if USE_HEP_EXT_IGPINFO
			String igpInfo[] = IGPInfo.getIGPInfo();
			IGPCode = igpInfo[0];
			IGPPortal = igpInfo[1];
			DBG("IGP_Freemium", "IGPCode = " + IGPCode + "IGPPortal=" +IGPPortal + "Game_Code= " + igpInfo[2]);	
		#endif //USE_HEP_EXT_IGPINFO
		#if USE_HEP_EXT_IGPINFO
		startIGP(lang, IGPCode);
		//startIGP(lang, "L3HP");
		#else
		startIGP(lang, gamecode);
		//startIGP(lang, "L3HP");
		#endif //USE_HEP_EXT_IGPINFO
		gIsRunning = true;
	}

	private void unbindDrawables(View view) 
	{
		if (view != null)
		{
			if (view.getBackground() != null) 
			{
				try {
					view.getBackground().setCallback(null);
					view.setBackgroundResource(0);
				} catch (Exception e) { 
					DBG_EXCEPTION(e); 
				}
			}
			
			if (view instanceof ImageButton)
			{
				try {
					if (((ImageButton) view).getDrawable() != null)
					{
						((ImageButton) view).getDrawable().setCallback(null);
						((ImageButton) view).setImageResource(0);
					}
				} catch (Exception e) { 
					DBG_EXCEPTION(e); 
				}
			}
			
			if (view instanceof ViewGroup) 
			{
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i));
				}
				try {
					((ViewGroup) view).removeAllViews();
				} catch (Exception e) { 
					DBG_EXCEPTION(e); 
				}
			}
		}
    }
	
	@Override
	protected void onDestroy()
	{		
		unbindDrawables(mView);
		System.gc();
		
		super.onDestroy();
	}
	
	public void onWindowFocusChanged(boolean focus)
	{
		sb_isFocus = focus;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if( keyCode == KeyEvent.KEYCODE_MENU)
		{
			event.startTracking();
			return true;
		}

		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{		
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_BUTTON_B:
			#if FULL_SCREEN_IGP
			if (!isVideoPlaying )
			{
				if (isBackPressed && (mWebView != null)) {
					mWebView.loadUrl("javascript:onBackPressed()");
					return true;
				} else {
					OnBackKeyReleased();
					return true;
				}
			}
			#endif
			OnBackKeyReleased();
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

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			return false;
		}

		return true;
	}
	
	public void OnBackKeyReleased()
	{
		try
		{
			IGPFreemiumActivity.gIsRunning = false;
			
		#if FULL_SCREEN_IGP				
			if (isVideoPlaying && mVideoView != null)
			{
				try {
					isVideoPlaying = false;
					setContentView(mView);
					mVideoView.stopPlayback();
					mVideoView = null;
				} catch (Exception e) { }
			}
			
			if (mWebView != null) {
				runOnUiThread(new Runnable() { public void run() {
					try {
						// if (!mWebView.getUrl().contains("hdfreemium.php"))
							// mWebView.loadUrl("about:blank");
						mView.removeView(mWebView);
						mWebView.destroy();
						mWebView.destroyDrawingCache();
						mWebView = null;
					
					} catch (Exception e) { }
				}});
			}			
		#endif
		
			finish();
		} catch (Exception ex){}
	}

	@Override
	public void onPause() {
	#if FULL_SCREEN_IGP	
		if (isVideoPlaying && mVideoView != null) {
			try {
				mVideoView.suspend();
			} catch (Exception e) { }
		}
		
		if (mWebView != null) {
			mWebView.loadUrl("javascript:onPause()");
			mWebView.onPause();
			mWebView.pauseTimers();
		}
	#endif	
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();		
	#if FULL_SCREEN_IGP	
		new Thread() { public void run() { 
			KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		
			while(km.inKeyguardRestrictedInputMode()) {
				//wait
			}
			
			runOnUiThread(new Runnable() { public void run() {
				if (isVideoPlaying && mVideoView != null) {
					try {
						mVideoView.resume();
					} catch (Exception e) { }
				}
			
				if (mWebView != null) {
					mWebView.onResume();
					mWebView.resumeTimers();
					mWebView.loadUrl("javascript:onResume()");
				}
			}});
			
		}}.start();
	#endif
	}
	

	void startIGP(int lang, String game_code)
	{
		DBG("IGP_FREEMIUM","Starting igp..");
		sb_isFocus = true;
		
		currentLanguage = lang;
		String UDID = Device.getDeviceId();
		
		String country = java.util.Locale.getDefault().getCountry();
		String deviceType = Build.MANUFACTURER+"_"+Build.MODEL;
		String deviceFW = Build.VERSION.RELEASE;
		
		DBG("IGP_FREEMIUM", "UDID = " + UDID);
			
		DBG("IGP_FREEMIUM","device settings detected..");
		K_LINK_IGP = K_LINK_IGP_TEMPLATE.replace("LANG", TXT_IGP_LANGUAGES[currentLanguage]);
		K_LINK_IGP = K_LINK_IGP.replace("GAME_CODE", game_code);
		K_LINK_IGP = K_LINK_IGP.replace("COUNTRY_DETECTED", country);
		K_LINK_IGP = K_LINK_IGP.replace("ANDROIDID", Device.getAndroidId());
		#if (HDIDFV_UPDATE == 1)
		K_LINK_IGP = K_LINK_IGP.replace("UDIDPHONE", UDID);
		K_LINK_IGP = K_LINK_IGP.replace("HDIDFV", Device.getHDIDFV());
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#elif (HDIDFV_UPDATE == 2)
		K_LINK_IGP = K_LINK_IGP.replace("UDIDPHONE", Device.getSerial());
		K_LINK_IGP = K_LINK_IGP.replace("HDIDFV", Device.getHDIDFV());
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());		
		#else
		K_LINK_IGP = K_LINK_IGP.replace("UDIDPHONE", UDID);
		#endif
		K_LINK_IGP = K_LINK_IGP.replace("DEVICE", deviceType);
		K_LINK_IGP = K_LINK_IGP.replace("FIRMWARE", deviceFW);
		K_LINK_IGP = K_LINK_IGP.replace("VERSION", GAME_VERSION_NAME_LETTER);
		K_LINK_IGP = K_LINK_IGP.replace("CONNSPEED", isConnectedFast() ? "fast" : "slow");
		K_LINK_IGP = K_LINK_IGP.replaceAll(" ", "");
		
	#if USE_IGP_REWARDS
		K_LINK_IGP += "&rewards=1";
	#endif

	#if FULL_SCREEN_IGP
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		K_LINK_IGP += "&width=" + display.getWidth();
		K_LINK_IGP += "&height=" + display.getHeight();
	#endif	
		
		DBG("IGP_FREEMIUM", "+++++++++++++++++ K_LINK_IGP = " + K_LINK_IGP);
		DBG("IGP_FREEMIUM", "+++++++++++++++++ Encrypting query parameters......");
		String [] split_url = K_LINK_IGP.split("[?]");
		K_LINK_IGP = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
		DBG("IGP_FREEMIUM", "+++++++++++++++++ K_LINK_IGP encrypted: " + K_LINK_IGP);
	

		if (Build.VERSION.SDK_INT == 11 || Build.VERSION.SDK_INT == 12) {
			SCR_H -= 48;
		} else if (deviceType.toLowerCase().contains("kindle")) {
			SCR_H -= 20;
		}
	
	#if FULL_SCREEN_IGP
		mView.addView(mWebView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
	#else
		boolean isTablet = !mWebView.getSettings().getUserAgentString().toLowerCase().contains("mobile");
		DBG("IGP_FREEMIUM","isTablet = " + isTablet);
		
		int REF_W = (isPortrait ? 598 : 912);
		int REF_H = (isPortrait ? 912 : 598);
		
		float scale_x = (float) SCR_W / REF_W;
		float scale_y = (float) SCR_H / REF_H;
		
		int MAX_W = SCR_W - (int)(scale_x * 30);
		int MAX_H = SCR_H - (int)(scale_y * 30);
		
		if (isTablet)
		{
			if ((int)(SCR_W * 0.67) > REF_W)
				MAX_W = (int)(SCR_W * 0.67);
			else
				MAX_W = REF_W;
			if ((int)(SCR_H * 0.67) > REF_H)
				MAX_H = (int)(SCR_H * 0.67);
			else
				MAX_H = REF_H;
		}
		
		int WIN_W = 0, WIN_H = 0; float scale = 0;
		
		while(((scale + 0.01f) * REF_W <= MAX_W) && ((scale + 0.01f) * REF_H <= MAX_H))
		{
			scale += 0.01f;
			WIN_W = (int)(scale * REF_W);
			WIN_H = (int)(scale * REF_H);
		}
		
		DBG("IGP_FREEMIUM","SCR_W = " + SCR_W + "; SCR_H = " + SCR_H);
		DBG("IGP_FREEMIUM","WIN_W = " + WIN_W + "; WIN_H = " + WIN_H);
		DBG("IGP_FREEMIUM","scale = " + scale);
				
		//background
		if (isPortrait)
			mLayout.setBackgroundResource(IMG_PORTRAIT[currentLanguage]);
		else
			mLayout.setBackgroundResource(IMG_LANDSCAPE[currentLanguage]);
			
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WIN_W, WIN_H);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mView.addView(mLayout, params);	
		
		//webView
		if (!isPortrait)
		{
			params = new RelativeLayout.LayoutParams(WIN_W - (int)(scale * 48), WIN_H - (int)(scale * 112));
			params.setMargins((int)(scale * 24), (int)(scale * 93), (int)(scale * 24), (int)(scale * 19));
			mWebView.setPadding((int)(scale * 24), (int)(scale * 93), (int)(scale * 24), (int)(scale * 19));
		}
		else
		{
			params = new RelativeLayout.LayoutParams(WIN_W - (int)(scale * 48), WIN_H - (int)(scale * 112));
			params.setMargins((int)(scale * 24), (int)(scale * 93), (int)(scale * 24), (int)(scale * 19));
			mWebView.setPadding((int)(scale * 24), (int)(scale * 93), (int)(scale * 24), (int)(scale * 19));		
		}
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mLayout.addView(mWebView, params);
		
		//mCloseButton
		mCloseButton.setBackgroundColor(0);
		mCloseButton.setImageResource(R.drawable.close_but);
		mCloseButton.setScaleType(ImageButton.ScaleType.FIT_XY);
		mCloseButton.setPadding(0, 0, 0, 0);
		mCloseButton.setId(9876);
		
		mCloseButton.setOnTouchListener(new View.OnTouchListener() 
		{
			public boolean onTouch(View view, MotionEvent event) 
			{
				float x = event.getX();
				float y = event.getY();
				int action = event.getAction();
				switch (action) 
				{
				case MotionEvent.ACTION_DOWN:
					((ImageButton)view).setColorFilter(Color.argb(100, 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_ATOP);
					return true;

				case MotionEvent.ACTION_UP:
					if (x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight())
					{
						((ImageButton)view).setColorFilter(Color.argb(0, 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_ATOP);
						return true;
					}
					OnBackKeyReleased();
					return true;
					
				case MotionEvent.ACTION_MOVE:
					if (x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight())
						((ImageButton)view).setColorFilter(Color.argb(0, 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_ATOP);
					else
						((ImageButton)view).setColorFilter(Color.argb(100, 0, 0, 0), android.graphics.PorterDuff.Mode.SRC_ATOP);
					return true;
				default:
					return false;
				}
			}		
		});

		if (!isPortrait)
		{
			params = new RelativeLayout.LayoutParams((int)(scale * 74), (int)(scale * 66));
			params.setMargins(0, (int)(scale * 15) , (int)(scale * 13), 0);
		}
		else
		{
			params = new RelativeLayout.LayoutParams((int)(scale * 69), (int)(scale * 62));
			params.setMargins(0, (int)(scale * 17) , (int)(scale * 8), 0);
		}
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		mLayout.addView(mCloseButton, params);
	#endif
		
		mWebView.loadUrl(K_LINK_IGP);		
	}
	
#if USE_IGP_REWARDS	
	private class IGPInterface
	{
	#if TARGET_API_LEVEL_INT>16
		@JavascriptInterface
	#endif
		public void getRewardsAvailable(String html)
		{
			DBG("IGP_FREEMIUM", "getRewardsAvailable: \"" + html + "\"");
			try {
				SharedPreferences settings = getSharedPreferences("IGP_Prefs", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("REWARDS_AVAILABLE", html);
				editor.commit();
			} catch(Exception e) { DBG_EXCEPTION(e); }
		}
	
	#if TARGET_API_LEVEL_INT>16
		@JavascriptInterface
	#endif
		public void getRewardsUser(String html)
		{
			DBG("IGP_FREEMIUM", "getRewardsUser: \"" + html + "\"");
			try {
				SharedPreferences settings = getSharedPreferences("IGP_Prefs", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("REWARDS_USER", html);
				editor.commit();
			} catch(Exception e) { DBG_EXCEPTION(e); }
		}
	
	#if TARGET_API_LEVEL_INT>16
		@JavascriptInterface
	#endif	
		public void getInstalledGames(String html)
		{
			DBG("IGP_FREEMIUM", "getInstalledGames: \"" + html + "\"");
			try {
				SharedPreferences settings = getSharedPreferences("IGP_Prefs", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("INSTALLED_GAMES", html);
				editor.commit();
			} catch(Exception e) { DBG_EXCEPTION(e); }
		}
	}
	
	public static String getRewardsAvailable()
	{
		try {
			SharedPreferences settings = SUtils.getContext().getSharedPreferences("IGP_Prefs", 0);
			return settings.getString("REWARDS_AVAILABLE", "");
		} catch(Exception e) {
			DBG_EXCEPTION(e);
			return "";
		}
	}
	
	public static String getRewardsUser()
	{
		try {
			SharedPreferences settings = SUtils.getContext().getSharedPreferences("IGP_Prefs", 0);
			return settings.getString("REWARDS_USER", "");
		} catch(Exception e) {
			DBG_EXCEPTION(e);
			return "";
		}
	}
	
	public static String getInstalledGames()
	{
		try {
			SharedPreferences settings = SUtils.getContext().getSharedPreferences("IGP_Prefs", 0);
			return settings.getString("INSTALLED_GAMES", "");
		} catch(Exception e) {
			DBG_EXCEPTION(e);
			return "";
		}
	}
#endif
	
	public interface RetrieveItemsListener
	{
		public void onMessageReceived(String title, String message);
		
		public void onItemReceived(int id, String type, int amount, int isInstall, String destGameCode, String creation_date);
		
		public void onRetrieveItemFailed(int error_code, String error_message);
	}
	
	public static void retrieveItems(final int language, final String game_code, final RetrieveItemsListener listener)
	{
		new Thread(new Runnable() { public void run()
		{
			try
			{
				String SCRIPT_URL = "https://ingameads.gameloft.com/redir/rewards.php?action=retrieveItems" 
					+ "&game_code=" + game_code 
					+ "&game_ver=" + GAME_VERSION_NAME_LETTER 
					+ "&lang=" + TXT_IGP_LANGUAGES[language] 	
					+ "&androidid=" + Device.getAndroidId()
				#if (HDIDFV_UPDATE == 1)
					+ "&user=" + Device.getDeviceId()
					+ "&hdidfv=" + Device.getHDIDFV();
					DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
				#elif (HDIDFV_UPDATE == 2)
					+ "&user=" + Device.getSerial()
					+ "&hdidfv=" + Device.getHDIDFV();
					DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
				#else
					+ "&user=" + Device.getDeviceId();
				#endif
				
				DBG("IGP_FREEMIUM", "++++++++++ Rewards: Script URL : " + SCRIPT_URL);
				
				String RESPONSE_STRING = getHttpResponse(SCRIPT_URL);
				
				DBG("IGP_FREEMIUM", "++++++++++ Rewards: Script response : " + RESPONSE_STRING);
				
				if (RESPONSE_STRING != null)
				{
					JSONObject jObject = new JSONObject(RESPONSE_STRING);
					int status = jObject.getInt("status");
					
					if (status == 0)
					{
						JSONArray items = jObject.getJSONArray("items");
						for (int i = 0; i < items.length(); i++)
						{
							JSONObject item = items.getJSONObject(i);
							
							int id = item.getInt("id");
							String type = item.getString("type");
							int amount = item.getInt("amount");
							int isInstall = item.getInt("isInstall");
							String destGameCode = item.getString("destGameCode");
							String creation = item.getString("creation");							
							listener.onItemReceived(id, type, amount, isInstall, destGameCode, creation);
						}
						
						if (items.length() != 0)
						{
							JSONObject message = jObject.getJSONObject("message");						
							String message_title = message.getString("title");
							String message_content = message.getString("content");						
							listener.onMessageReceived(message_title, message_content);	
						}
						else
						{
							listener.onRetrieveItemFailed(status, "No items retrieved.");
						}
					}
					else
					{
						String message = jObject.getString("message");
						listener.onRetrieveItemFailed(status, message);
					}						
				}
				else 
				{
					listener.onRetrieveItemFailed(-1, "Response is null. Check your network connection.");
				}
			}
			catch(Exception e)
			{
				DBG_EXCEPTION(e);
				listener.onRetrieveItemFailed(-1, "Exception occured: " + e.getMessage());
			}
			
			System.gc();
		}}).start();
	}
	
	private static String getHttpResponse(String RequestUrl)
    {
    	String response_text = null;
		BufferedReader stream_in = null;    	
		try {
			DBG("IGP_FREEMIUM", "+++++++++++++++++ Encrypting query parameters......");
			String [] split_url = RequestUrl.split("[?]");
			String FinalRequestUrl = split_url[0] + "?data=" + Encrypter.crypt(split_url[1]) + "&enc=1";
			DBG("IGP_FREEMIUM", "+++++++++++++++++ Executing encrypted request: " + FinalRequestUrl);
			
			HttpClient client= new DefaultHttpClient();
			HttpGet request = new HttpGet(RequestUrl);
			HttpResponse response = client.execute(request);			
			stream_in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer buffer = new StringBuffer("");
            String line = "";
            while ((line = stream_in.readLine()) != null) {
            	buffer.append(line);
            }
            stream_in.close();
            response_text = buffer.toString();
		} catch (Exception e) { 
			DBG_EXCEPTION(e);
		} finally {
            if (stream_in != null) {
                try {
                	stream_in.close();
                } 
                catch (Exception e) { 
                	DBG_EXCEPTION(e);
                }
            }
		}		
		return response_text;
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

	
#if FULL_SCREEN_IGP	
	class IgpJsInterface {
		Context context;
		ViewGroup root;

		IgpJsInterface(Context ctx, ViewGroup main) {
			context = ctx;
			root = main;
		}

		#if TARGET_API_LEVEL_INT>16
		@JavascriptInterface
		#endif
		public void playVideo(final String url, final String endHandler, final String exitVideo, final String buttonUrl)
		{
			DBG("IGP_FREEMIUM", "======= playVideo =======\n" + url + "\n" + endHandler + "\n" + exitVideo + "\n" + buttonUrl);
			
			if (!isVideoPlaying)
			{
				final Bitmap closeButton = fetchImage(buttonUrl);
				
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						isVideoPlaying = true;
						
						mVideoContainer = new RelativeLayout(context);
						setContentView(mVideoContainer);
						
						mVideoView = new VideoView(context);						
						
			            LayoutParams l_params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		            	l_params.addRule(RelativeLayout.CENTER_IN_PARENT);
			            
			            mVideoContainer.addView(mVideoView, l_params);
			            
			            final ProgressDialog pd = ProgressDialog.show(IGPFreemiumActivity.this, null, getString(TXT_LOADING[currentLanguage]));
						pd.setCancelable(true);
			            
						mVideoView.setMediaController(null);
			            mVideoView.setVideoURI(Uri.parse(url));
			            mVideoView.requestFocus();		
			            			            
			            mVideoView.setOnPreparedListener(new OnPreparedListener()
			            {	
			            	public void onPrepared(MediaPlayer mp)
			                {                  
			            		//progressDialog.dismiss();
			            		if (pd.isShowing())
			            			pd.dismiss();
			            		mVideoView.start();
			                }
			            });  
			            mVideoView.setOnErrorListener(new OnErrorListener() 
			            {							
							@Override
							public boolean onError(MediaPlayer mp, int what, int extra) 
							{
								if (pd.isShowing())
			            			pd.dismiss();
								isVideoPlaying = false;
								setContentView(mView);
								mVideoView = null;
								mWebView.loadUrl("javascript:" + exitVideo + "()");
								return true;
							}
						});
			            mVideoView.setOnCompletionListener(new OnCompletionListener() 
			            {					
							@Override
							public void onCompletion(MediaPlayer mp) 
							{
								if (pd.isShowing())
			            			pd.dismiss();
								isVideoPlaying = false;
								setContentView(mView);
								mWebView.loadUrl("javascript:" + endHandler + "()");
							}
						});
			            mVideoView.setOnKeyListener(new OnKeyListener() 
			            {							
							@Override
							public boolean onKey(View v, int keyCode, KeyEvent event) 
							{
								if (keyCode == KeyEvent.KEYCODE_BACK)
								{
									isVideoPlaying = false;
									setContentView(mView);
									mVideoView.stopPlayback();
									mVideoView = null;
									mWebView.loadUrl("javascript:" + exitVideo + "()");
									return true;
								}
								return false;
							}
						});
			            
			            if (closeButton != null)
			            {
			            	ImageButton skipButton = new ImageButton(context);
			            	skipButton.setImageBitmap(closeButton);
			            	skipButton.setBackgroundColor(0);
			            	skipButton.setScaleType(ImageButton.ScaleType.FIT_XY);
			            	skipButton.setPadding(0, 0, 0, 0);
			            	
			            	int size = 70;
			            	size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources().getDisplayMetrics());
			            	
			            	LayoutParams params = new LayoutParams(size, size);
			            	params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			            	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			            	
			            	mVideoContainer.addView(skipButton, params);			            	
			            	
			            	skipButton.setOnTouchListener(new View.OnTouchListener() 
			        		{
			        			public boolean onTouch(View view, MotionEvent event) 
			        			{
			        				float x = event.getX();
			        				float y = event.getY();
			        				int action = event.getAction();
			        				switch (action) 
			        				{
			        				case MotionEvent.ACTION_DOWN:
			        					((ImageButton)view).setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
			        					return true;

			        				case MotionEvent.ACTION_UP:
			        					if (x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight())
			        					{
			        						((ImageButton)view).setColorFilter(Color.argb(0, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
			        						return true;
			        					}
			        					//MOD 
			        					isVideoPlaying = false;
										setContentView(mView);
										//mVideoView = null;
			        					mWebView.loadUrl("javascript:" + exitVideo + "()");
			        					return true;
			        					
			        				case MotionEvent.ACTION_MOVE:
			        					if (x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight())
			        						((ImageButton)view).setColorFilter(Color.argb(0, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
			        					else
			        						((ImageButton)view).setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
			        					return true;
			        				default:
			        					return false;
			        				}
			        			}		
			        		});
			            }
					}
				});		
			}			
		}
		
		#if TARGET_API_LEVEL_INT>16
		@JavascriptInterface
		#endif
		public void checkBackPressed(String var) {
			if (var != null) {
				if (var.equals("true")) {
					isBackPressed = true;
					return;
				}
			}
			
			isBackPressed = false;
		}
	}
#endif

	class IgpWebChromeClient extends WebChromeClient {
		@Override
		public boolean onConsoleMessage(ConsoleMessage cm) {
			DBG("IGP_JSCONSOLE", cm.messageLevel().toString() + ": " + cm.message() + " -- From line " + cm.lineNumber());
			return true;
		}
	}
	
	private void showToast(final String message) {
    	runOnUiThread(new Runnable() { public void run() {
	    	Toast.makeText(IGPFreemiumActivity.this, message, Toast.LENGTH_SHORT).show();
    	}});
    }

	class IgpWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			DBG("IGP_FREEMIUM", "======= onPageStarted =======\n" + url);
			showProgressLoading();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			DBG("IGP_FREEMIUM", "======= onPageFinished ========\n" + url);
			hideProgress();			
		
		#if FULL_SCREEN_IGP
			view.loadUrl("javascript:window.Android.checkBackPressed(isBackPressed)");
		#endif
		
		#if USE_IGP_REWARDS
			view.loadUrl("javascript:window.JSInterface.getRewardsAvailable(REWARDS_AVAILABLE)");
			view.loadUrl("javascript:window.JSInterface.getRewardsUser(REWARDS_USER)");
			view.loadUrl("javascript:window.JSInterface.getInstalledGames(REWARDS_GAMES_INSTALLED)");
		#endif
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			DBG("IGP_FREEMIUM", "======= shouldOverrideUrlLoading =======\n" + url);
			
			if (url.startsWith("play:")) {
				try {
					String package_name = url.replace("play:", "").split("[?]")[0];
					DBG("IGP_FREEMIUM", "***************app to be launched: " + package_name);

					PackageManager manager = getPackageManager();
					Intent intent = manager.getLaunchIntentForPackage(package_name);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					startActivity(intent);
				} catch (Exception e) {
					DBG_EXCEPTION(e);
					if (INSTALL_URL_IF_GAME_NOT_INSTALLED != null) {
						DBG("IGP_FREEMIUM", "The game was not found on the device, proceeding to download page");
						// openBrowser(INSTALL_URL_IF_GAME_NOT_INSTALLED);
						view.loadUrl(INSTALL_URL_IF_GAME_NOT_INSTALLED);
						INSTALL_URL_IF_GAME_NOT_INSTALLED = null;
					}
				}
				return true;
			}			
			else if (url.equals("exit:") || url.equals("unavailable:")) {
				OnBackKeyReleased();
				return true;
			} else if (url.startsWith("market://")) {
				openGooglePlay(url);
				return true;
			} else if (url.startsWith("amzn://")) {
				openAmazon(url);
				return true;
			} else if (url.contains("www.amazon.com")) {
				openAmazon(url.replaceAll("http://", "amzn://")
								.replaceAll("https://", "amzn://"));
				return true;
			} else if (url.startsWith("skt:")) {
				openSkt(url.replaceAll("skt:", ""));
				return true;
			} else if (url.startsWith("link:")) {
				String link = url.replaceAll("link:", "");
				if (link.contains("www.amazon.com")) {
					openAmazon(link.replaceAll("http://", "amzn://")
									.replaceAll("https://", "amzn://"));
					return true;
				}
				openBrowser(link);
				return true;
			} else if (url.contains("ingameads.gameloft.com/redir/")) {			
				if (url.contains("ctg=PLAY")) {
					INSTALL_URL_IF_GAME_NOT_INSTALLED = url.replace("t=bundle", "t=game").replace("ctg=PLAY", "ctg=FRINSTALL&old_ctg=PLAY");
				}				
				if (url.contains("type=SKTMARKET") && url.contains("&pp=1")) {
					if (url.contains("ctg=PLAY")) {
						INSTALL_URL_IF_GAME_NOT_INSTALLED = INSTALL_URL_IF_GAME_NOT_INSTALLED.replaceAll("&pp=1", "");
					}
					view.loadUrl(url.replaceAll("&pp=1", ""));
					return true;
				}
				
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
				
				return false;
			} 
		#if VERIZON_DRM
			else if (url.indexOf("&pp=1") != -1)
			{
				final String f_url = url;
				new Thread ( new Runnable() { public void run()
				{
					try {
						OpenBrowser(GetHttpConnection(f_url));						
					}
					catch (IOException ex) {
						DBG("IGP_FREEMIUM","Error can't get data from server::: "+ex.getMessage());
					}
				}}).start();
				return true;
			}
		#endif			
			else {
				openBrowser(url);
				return true;
			}
		}

		@Override
		public void onReceivedError(final WebView view, int errorCode, final String description, String failingUrl) {
			runOnUiThread(new Runnable() { public void run() {
				view.setVisibility(View.INVISIBLE);
				try {
					AlertDialog dialog = new AlertDialog.Builder(IGPFreemiumActivity.this)
							.setPositiveButton(getString(TXT_OK[currentLanguage]), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									OnBackKeyReleased();
								}
							})
							.setOnCancelListener(new DialogInterface.OnCancelListener() {
								public void onCancel(DialogInterface dialog) {
									OnBackKeyReleased();
								}
							})
							.setMessage(getString(TXT_NET_ERROR[currentLanguage]))
							.create();
					dialog.setCanceledOnTouchOutside(false);
					dialog.show();
				} catch (Exception e) {
				}
			}});
		}
		
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}	
	
	#if VERIZON_DRM
		private String GetHttpConnection(String urlString)  throws IOException
		{
			InputStream in = null;
			int response = -1;
			String str="";

			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();

			if (!(conn instanceof HttpURLConnection))
				throw new IOException("Not an HTTP connection");

			try
			{
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setAllowUserInteraction(false);
				httpConn.setInstanceFollowRedirects(true);
				httpConn.setRequestMethod("GET");
				httpConn.connect();

				response = httpConn.getResponseCode();
				if (response == HttpURLConnection.HTTP_OK)
				{
					in = httpConn.getInputStream();
					int ch;
					while((ch=in.read())!=-1)
					{
						str+=((char)ch);
					}
				}
			}
			catch (Exception ex)
			{
				throw new IOException("Error connecting...");
			}
			return str;
		}
	#endif

	}
	
	private ProgressDialog progress_loading = null;

	private void showProgress(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				try {
					hideProgress();
					progress_loading = ProgressDialog.show(IGPFreemiumActivity.this, null, message);
					progress_loading.setCancelable(true);
				} catch (Exception e) {
				}
			}
		});
	}

	private void showProgressLoading() {
		showProgress(getString(TXT_LOADING[currentLanguage]));
	}

	private void hideProgress() {
		runOnUiThread(new Runnable() {
			public void run() {
				try {
					if (progress_loading != null)
						progress_loading.dismiss();
					progress_loading = null;
					System.gc();
				} catch (Exception e) {
				}
			}
		});
	}

	private void openBrowser(String url) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		DBG("IGP_FREEMIUM", "openBrowser: " + url);

		if (i != null) {
			DBG("IGP_FREEMIUM", "openBrowser: intent not null");
			if (getPackageManager().queryIntentActivities(i,
					PackageManager.MATCH_DEFAULT_ONLY).size() != 0) {
				DBG("IGP_FREEMIUM", "openBrowser: activity found for intent");
				startActivity(i);
			} else {
				DBG("IGP_FREEMIUM", "openBrowser: no activity found for intent");
			}
		} else {
			DBG("IGP_FREEMIUM", "openBrowser: intent is null");
		}
	}

	private void openAmazon(String url) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		DBG("IGP_FREEMIUM", "openAmazon: " + url);

		if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() != 0) {
			DBG("IGP_FREEMIUM", "openAmazon: activity found for intent");
			startActivity(i);
			return;
		} else {
			DBG("IGP_FREEMIUM", "openAmazon: no activity found for intent");
		}

		if (url.contains("www.amazon.com")) {
			try {
				DBG("IGP_FREEMIUM", "openAmazon: amzn:// failed, trying http://");
				i = new Intent(Intent.ACTION_VIEW, Uri.parse(url.replaceAll("amzn://", "http://")));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			} catch (Exception e) {
			}
		}
	}

	private void openGooglePlay(String url) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		DBG("IGP_FREEMIUM", "openGooglePlay: " + url);

		if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() != 0) {
			DBG("IGP_FREEMIUM", "openGooglePlay: activity found for intent");
			startActivity(i);
			return;
		} else {
			DBG("IGP_FREEMIUM", "openGooglePlay: no activity found for intent");
		}

		try {
			DBG("IGP_FREEMIUM", "openGooglePlay: market:// failed, trying http://play.google.com");
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(
				url.replaceAll("market://details", "https://play.google.com/store/apps/details")
				.replaceAll("market://search", "https://play.google.com/store/search")));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} catch (Exception e) {
		}
	}

	private void openSkt(String productId) {
		Intent i = getPackageManager().getLaunchIntentForPackage("com.skt.skaf.A000Z00040");

		DBG("IGP_FREEMIUM", "openSkt: " + productId);

		if (i != null) {
			DBG("IGP_FREEMIUM", "openSkt: intent is not null");
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.setClassName("com.skt.skaf.A000Z00040", "com.skt.skaf.A000Z00040.A000Z00040");
			i.setAction("COLLAB_ACTION");
			i.putExtra("com.skt.skaf.COL.URI", ("PRODUCT_VIEW/" + productId + "/0").getBytes());
			i.putExtra("com.skt.skaf.COL.REQUESTER", "A000Z00040");
		} else {
			DBG("IGP_FREEMIUM", "openSkt: intent is null, opening web page");
			i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://tsto.re/" + productId));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		startActivity(i);
	}

	private Bitmap fetchImage(String urlstr) {
		try {
			URL url = new URL(urlstr);

			HttpURLConnection connection = (HttpURLConnection) url .openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream is = connection.getInputStream();
			Bitmap img = BitmapFactory.decodeStream(is);
			return img;
		} catch (MalformedURLException e) {
			DBG_EXCEPTION(e);
		} catch (IOException e) {
			DBG_EXCEPTION(e);
		}
		return null;
	}
	
	public boolean isConnectedFast() {
		NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (info != null && info.isConnected()) {
			int type = info.getType();
			int subType = info.getSubtype();

			if (type == ConnectivityManager.TYPE_WIFI) {
				return true;
			} else if (type == ConnectivityManager.TYPE_MOBILE) {
				switch (subType) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					return false; // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return false; // ~ 14-64 kbps
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return false; // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					return true; // ~ 400-1000 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					return true; // ~ 600-1400 kbps
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return false; // ~ 100 kbps
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					return true; // ~ 2-14 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPA:
					return true; // ~ 700-1700 kbps
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					return true; // ~ 1-23 Mbps
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return true; // ~ 400-7000 kbps
					// Unknown
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
					return false;
				default:
					return checkConnectionOther(subType);
				}
			} else {
				return false;
			}

		}
		return false;
	}

	private boolean checkConnectionOther(int subType) {
		if (Build.VERSION.SDK_INT >= 8) {
			if (subType == TelephonyManager.NETWORK_TYPE_IDEN) {
				return false; // ~25 kbps
			}
		}
		if (Build.VERSION.SDK_INT >= 9) {
			if (subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
				return true; // ~ 5 Mbps
			}
		}
		if (Build.VERSION.SDK_INT >= 11) {
			if (subType == TelephonyManager.NETWORK_TYPE_EHRPD) {
				return true; // ~ 1-2 Mbps
			} else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {
				return true; // ~ 10+ Mbps
			}
		}
		if (Build.VERSION.SDK_INT >= 13) {
			if (subType == TelephonyManager.NETWORK_TYPE_HSPAP) {
				return true; // ~ 10-20 Mbps
			}
		}
		return false;
	}
}
