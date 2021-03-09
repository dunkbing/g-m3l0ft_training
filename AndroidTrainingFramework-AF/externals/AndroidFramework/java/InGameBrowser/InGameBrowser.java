package APP_PACKAGE;

import java.io.*;
import java.util.*;


import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import org.json.*;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.*;
import android.net.*;
import android.net.http.SslError;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.*;
import android.view.View.OnTouchListener;
import android.webkit.*;
import android.widget.*;

import APP_PACKAGE.GLUtils.*;

public class InGameBrowser extends Activity implements OnTouchListener {
	
	private WebView mWebView = null;
	private RelativeLayout mView = null;
	private ImageButton mBackButton, mForwardButton, mReloadButton, mCloseButton = null; 
	private static String INGAMEBROWSER_URL = "";
	private static String INGAMEBROWSER_POST_TEMPLATE = "from=FROM&op=OPERATOR&country=COUNTRY&lg=LANG&udid=UDID&androidid=ANDROID_ID&hdidfv=HDIDFV&game_ver=VERSION&d=DEVICE&f=FIRMWARE&anonymous=ANONYMOUS_ACCOUNT&fbid=FACEBOOK_ID&gliveusername=GLIVE_USERNAME&googleid=GOOGLEID&clientid=CLIENT_ID&os=android";
	private static String INGAMEBROWSER_POST_TEMPLATE_UPDATE_RATE = "from=FROM&op=OPERATOR&country=COUNTRY&lg=LANG&udid=UDID&androidid=ANDROID_ID&hdidfv=HDIDFV&game_ver=VERSION&d=DEVICE&f=FIRMWARE&anonymous=ANONYMOUS_ACCOUNT&fbid=FACEBOOK_ID&gliveusername=GLIVE_USERNAME&googleid=GOOGLEID&clientid=CLIENT_ID&os=android&game=GAME_NAME";
	
	private static String REDIR_URL = "";
	
	private static String FORUM_PARAMS_TEMPLATE = "from=FROM&op=OPERATOR&ctg=FORUM&game_ver=VERSION&lg=LANG&country=COUNTRY&d=DEVICE&f=FIRMWARE&udid=UDID&hdidfv=HDIDFV&androidid=ANDROID_ID&clientid=CLIENT_ID";
	
	private static String CHECK_UNREAD_NEWS_NUMBER = "";
	private static String SAVE_NEWS_ID = "";
	private static String DISPLAY_NEWS = "";
	
	public static int languageIndex = 0;
	public static String anonymousAccount = "";
	public static String facebookID = "";
	public static String gliveAccount = "";	
	public static String googleAccount = "";	

	public static String clientID = CLIENTID;

	private static boolean m_IGB_initialized = false;
	
	private static int unreadNewsNumber = -1;
	private static int _lastUnreadNewsIndex = -1;

	private static UnreadNewsChangedCallback s_callback = null;
	
	public static native void nativeUnreadNewsChangedCallback(int unreadNewsNumber);
	
	public interface UnreadNewsChangedCallback {
		public void onUnreadNewsChanged(int unreadNewsNumber);
	}
		
	private static String[] TXT_IGB_LANGUAGES = { "EN", "FR", "DE", "IT", "SP", "JP", "KR", "CN", "BR", "RU", "TR", "AR", "TH", "ID", "VI", "ZT" };
	
	private int[] TXT_LOADING = { 
			R.string.IGB_LOADING_EN,
			R.string.IGB_LOADING_FR, 
			R.string.IGB_LOADING_DE,
			R.string.IGB_LOADING_IT, 
			R.string.IGB_LOADING_SP,
			R.string.IGB_LOADING_JP, 
			R.string.IGB_LOADING_KR,
			R.string.IGB_LOADING_CN, 
			R.string.IGB_LOADING_BR,
			R.string.IGB_LOADING_RU, 
			R.string.IGB_LOADING_TR,
			R.string.IGB_LOADING_AR, 
			R.string.IGB_LOADING_TH, 
			R.string.IGB_LOADING_ID,
			R.string.IGB_LOADING_VI,
			R.string.IGB_LOADING_ZT
		};

	private int[] TXT_NET_ERROR = { 
			R.string.IGB_NET_ERROR_EN,
			R.string.IGB_NET_ERROR_FR, 
			R.string.IGB_NET_ERROR_DE,
			R.string.IGB_NET_ERROR_IT, 
			R.string.IGB_NET_ERROR_SP,
			R.string.IGB_NET_ERROR_JP, 
			R.string.IGB_NET_ERROR_KR,
			R.string.IGB_NET_ERROR_CN, 
			R.string.IGB_NET_ERROR_BR,
			R.string.IGB_NET_ERROR_RU, 
			R.string.IGB_NET_ERROR_TR,
			R.string.IGB_NET_ERROR_AR, 
			R.string.IGB_NET_ERROR_TH,
			R.string.IGB_NET_ERROR_ID,
			R.string.IGB_NET_ERROR_VI,
			R.string.IGB_NET_ERROR_ZT
		};

	private int[] TXT_OK = { 
			R.string.IGB_OK_EN, 
			R.string.IGB_OK_FR,
			R.string.IGB_OK_DE, 
			R.string.IGB_OK_IT, 
			R.string.IGB_OK_SP,
			R.string.IGB_OK_JP, 
			R.string.IGB_OK_KR, 
			R.string.IGB_OK_CN,
			R.string.IGB_OK_BR, 
			R.string.IGB_OK_RU, 
			R.string.IGB_OK_TR,
			R.string.IGB_OK_AR,
			R.string.IGB_OK_TH,
			R.string.IGB_OK_ID,
			R.string.IGB_OK_VI,
			R.string.IGB_OK_ZT
		};


	public static void SetBaseUrl(String url)
    {
        if (url != null && !url.equals(""))
        {
            if (!url.endsWith("/"))
        		url += "/";

            INGAMEBROWSER_URL = url + "redir/ingamebrowser.php";
            REDIR_URL = url + "redir/?";
            CHECK_UNREAD_NEWS_NUMBER = url + "redir/ingamenews.php?action=checkNews&last-id=LAST_ID";
            SAVE_NEWS_ID = url + "redir/ingamenews.php?action=saveNews&last-id=LAST_ID";
            DISPLAY_NEWS = url + "redir/ingamenews.php?action=displayNews";

            m_IGB_initialized = true;
        }
    }
	
	
	private static String solveTemplate (String template) {
		String result = template;
		
		if (template != null) {
			if (languageIndex < 0 || languageIndex >= TXT_IGB_LANGUAGES.length)
				languageIndex = 0;
			
			result = result.replaceAll("FROM", GGC_GAME_CODE)
					.replaceAll("OPERATOR", GGC_GAME_OPERATOR)
					.replaceAll("COUNTRY", Locale.getDefault().getCountry())
					.replaceAll("LANG", TXT_IGB_LANGUAGES[languageIndex])
				#if (HDIDFV_UPDATE == 2)
					.replaceAll("UDID", Device.getSerial())
				#else
					.replaceAll("UDID", Device.getDeviceId())
				#endif
					.replaceAll("ANDROID_ID", Device.getAndroidId())
				#if HDIDFV_UPDATE 
					.replaceAll("HDIDFV", Device.getHDIDFV())
				#else
					.replaceAll("&hdidfv=HDIDFV", "")
				#endif
					.replaceAll("VERSION", GAME_VERSION_NAME_LETTER)
					.replaceAll("DEVICE", (Build.MANUFACTURER + "_" + Build.MODEL))
					.replaceAll("FIRMWARE", Build.VERSION.RELEASE)
					.replaceAll("ANONYMOUS_ACCOUNT", anonymousAccount)
					.replaceAll("FACEBOOK_ID", facebookID)
					.replaceAll("GLIVE_USERNAME", gliveAccount)
					.replaceAll("GOOGLEID", googleAccount)
					.replaceAll("GAME_NAME", GGC_GAME_CODE)
					.replaceAll("CLIENT_ID", clientID)
					.replaceAll(" ", "");
		} else return "";
		
		return result;
	}

	private String CTG_TYPE = "NONE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!m_IGB_initialized) {
            finish();
            return;
        }

		gIsRunning = true;
		
		setContentView(R.layout.activity_in_game_browser);
		
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		SCR_W = display.getWidth();
		SCR_H = display.getHeight();
		
		RelativeLayout mWebLayout = ((RelativeLayout)findViewById(R.id.ingamebrowser_webview));
		mWebView = new WebView1(this);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(false);
		mWebView.getSettings().setBuiltInZoomControls(false);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.setWebViewClient(new IGBWebViewClient());
		
	#if (TARGET_API_LEVEL_INT >= 21)
		if (Build.VERSION.SDK_INT >= 21) {
			mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
	#endif
		
		mWebLayout.addView(mWebView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
				
		mBackButton = ((ImageButton)findViewById(R.id.ingamebrowser_backbutton));
		mForwardButton = ((ImageButton)findViewById(R.id.ingamebrowser_forwardbutton));
		mReloadButton = ((ImageButton)findViewById(R.id.ingamebrowser_refreshbutton));
		mCloseButton = ((ImageButton)findViewById(R.id.ingamebrowser_closebutton));
		
		mBackButton.setOnTouchListener(this);
		mForwardButton.setOnTouchListener(this);
		mReloadButton.setOnTouchListener(this);
		mCloseButton.setOnTouchListener(this);
		
		String extra_url = getIntent().getStringExtra("extra_url");
		String post_data = solveTemplate(INGAMEBROWSER_POST_TEMPLATE);		
		
		if (extra_url != null) {
			if (extra_url.equals("forum")) {
				CTG_TYPE = "FORUM";
				String forum_url = REDIR_URL + "data=" + Encrypter.crypt(solveTemplate(FORUM_PARAMS_TEMPLATE)) + "&enc=1";
				DBG("INGAMEBROWSER", "Opening forum with URL: " + forum_url);
				mWebView.loadUrl(forum_url);
			} else if(extra_url.equals("news")) {
				CTG_TYPE = "NEWS";
				DBG("INGAMEBROWSER", "Opening news: " + DISPLAY_NEWS);
				DBG("INGAMEBROWSER", "with POST DATA: " + post_data);		
				mWebView.postUrl(DISPLAY_NEWS, EncodingUtils.getBytes(post_data, "BASE64"));
			} else {			
				DBG("INGAMEBROWSER", "extra_url is not null!!!!!!! using: " + extra_url);
				DBG("INGAMEBROWSER", "with POST DATA: " + post_data);		
				mWebView.postUrl(extra_url, EncodingUtils.getBytes(post_data, "BASE64"));
			}
		} else {
			CTG_TYPE = "SUPPORT";
			int banType = getIntent().getIntExtra("ban_type", -1);
			if (banType >= 0) {
				post_data += ("&extra_14=" + banType);
				DBG("INGAMEBROWSER", "extra_url is null!!!!!!! using: " + (INGAMEBROWSER_URL + "?ctg=BANNED"));
				DBG("INGAMEBROWSER", "with POST DATA: " + post_data);
				mWebView.postUrl((INGAMEBROWSER_URL + "?ctg=BANNED"), EncodingUtils.getBytes(post_data, "BASE64"));
			} else {
				DBG("INGAMEBROWSER", "extra_url is null!!!!!!! using: " + (INGAMEBROWSER_URL + "?ctg=SUPPORT"));
				DBG("INGAMEBROWSER", "with POST DATA: " + post_data);
				mWebView.postUrl((INGAMEBROWSER_URL + "?ctg=SUPPORT"), EncodingUtils.getBytes(post_data, "BASE64"));
			}
		}
		
		mView = ((RelativeLayout)findViewById(R.id.ingamebrowser_mview));
		
		final View decorView = getWindow().getDecorView();
		decorView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHight = rect.bottom - rect.top;
                int height = decorView.getHeight();
                boolean hide = (double)displayHight / height > 0.8 ;
				
				if (!hide) {
					isKeyBoardShown = true;
					cursor.setVisibility(View.INVISIBLE);
				} else {
					isKeyBoardShown = false;
					cursor.setVisibility(View.VISIBLE);
				}
			}
		});
		
		overlay = new AbsoluteLayout(InGameBrowser.this);
		overlay.setBackgroundColor(0);
		mView.addView(overlay, AbsoluteLayout.LayoutParams.FILL_PARENT, AbsoluteLayout.LayoutParams.FILL_PARENT);			
		cursor = new ImageView(InGameBrowser.this);
		cursor.setImageResource(R.drawable.content_cursor);
		AbsoluteLayout.LayoutParams cursor_params = new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, SCR_W/2, SCR_H/2);
		overlay.addView(cursor, cursor_params);
		overlay.setVisibility(View.GONE);
		
		m_SelectedInputDevice = findBySource(InputDevice.SOURCE_JOYSTICK);
		if (m_SelectedInputDevice != null) {
			overlay.setVisibility(View.VISIBLE);
		}
		
		new Thread() {
			public void run() {
				while(gIsRunning) {
					m_SelectedInputDevice = findBySource(InputDevice.SOURCE_JOYSTICK);
					runOnUiThread(new Runnable() { public void run() 
					{
						if(m_SelectedInputDevice != null) {
							if (overlay != null) {
								try {
									overlay.setVisibility(View.VISIBLE);
								} catch(Exception e) { } 
							}
						} else {
							if (overlay != null) {
								try {
									overlay.setVisibility(View.GONE);
								} catch(Exception e) { } 
							}
						}
					}});
					
					try { Thread.sleep(10000); } catch(Exception e) { } 
				}
			}
		}.start();		
		
		new Thread() {
			public void run() {
				while (gIsRunning) {
					if (Math.abs(leftJoystickX) >= 0.3 || Math.abs(leftJoystickY) >= 0.3 || Math.abs(rightJoystickX) >= 0.3 || Math.abs(rightJoystickY) >= 0.3) {
						runOnUiThread(new Runnable() { public void run () {
							if (cursor != null) {
								float new_x = cursor.getX() + (leftJoystickX + rightJoystickX) * 10.0f;
								float new_y = cursor.getY() + (leftJoystickY + rightJoystickY) * 10.0f;
								cursor.setX(new_x);
								cursor.setY(new_y);
								
								if (new_x > SCR_W - (cursor.getWidth() / 2)) {
									cursor.setX(SCR_W - (cursor.getWidth() / 2));
								} 										
								if (new_x < 0 - (cursor.getWidth() / 2)) {
									cursor.setX(0 - (cursor.getWidth() / 2));
								}
								if (new_y > SCR_H - (cursor.getHeight() / 2)) {
									cursor.setY(SCR_H - (cursor.getHeight() / 2));
								} 
								if (new_y < 0 - (cursor.getHeight() / 2)) {
									cursor.setY(0 - (cursor.getHeight() / 2));
								} 
							}
						}});
					}
						
					try { Thread.sleep(30); } catch(Exception e) { }
				}
			}
		}.start();
	}
	
	private AbsoluteLayout overlay = null;
	private ImageView cursor = null;
	private InputDevice m_SelectedInputDevice = null;
	private float dpadX = 0;
	private float dpadY = 0;
	private float leftJoystickX = 0;
	private float leftJoystickY = 0;
	private float rightJoystickX = 0;
	private float rightJoystickY = 0;
	public static boolean gIsRunning = false;
	private int SCR_W = 0;
	private int SCR_H = 0;
	private boolean isKeyBoardShown = false;
	
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
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:			
			case KeyEvent.KEYCODE_BUTTON_X:
			case KeyEvent.KEYCODE_BUTTON_Y:
			case KeyEvent.KEYCODE_BUTTON_SELECT:
			case KeyEvent.KEYCODE_BUTTON_START:
			case KeyEvent.KEYCODE_BUTTON_L1:
			case KeyEvent.KEYCODE_BUTTON_R1:				
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					InGameBrowser.this.onKeyDown(event.getKeyCode(), event);
				else if ((event.getAction() == KeyEvent.ACTION_UP))
					InGameBrowser.this.onKeyUp(event.getKeyCode(), event);			
			}
			
			return super.dispatchKeyEventPreIme(event);
		}
	}
	
	@Override
    public boolean onGenericMotionEvent(MotionEvent event) 
    {
		if (!isKeyBoardShown) {			
			leftJoystickX = (float) (event.getAxisValue(MotionEvent.AXIS_X)); 
            leftJoystickY = (float) (event.getAxisValue(MotionEvent.AXIS_Y)); 
			rightJoystickX = (float) (event.getAxisValue(MotionEvent.AXIS_Z)); 
			rightJoystickY = (float) (event.getAxisValue(MotionEvent.AXIS_RZ));
            dpadX = (float) (event.getAxisValue(MotionEvent.AXIS_HAT_X));
            dpadY = (float) (event.getAxisValue(MotionEvent.AXIS_HAT_Y));
			
			float leftTrigger = (float) (event.getAxisValue(MotionEvent.AXIS_BRAKE)); 
            float rightTrigger = (float) (event.getAxisValue(MotionEvent.AXIS_GAS)); 
			if (leftTrigger >= 0.7 || rightTrigger >= 0.7) {
				simulateTouchEvent();
			}
			
			if (dpadX != 0 || dpadY != 0) {
				leftJoystickX = 0;
				leftJoystickY = 0;
				rightJoystickX = 0;
				rightJoystickY = 0;
			}
			
			if (dpadY != 0) {
				if(mWebView != null) {
					if (dpadY == 1.0)
						mWebView.pageDown(false);
					else
						mWebView.pageUp(false);
				}
			}
			
			return true;
		}
		return super.onGenericMotionEvent(event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (!isKeyBoardShown) {
			switch(keyCode)
			{		
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_BUTTON_B:
				// if (m_SelectedInputDevice != null && event.getDevice() == m_SelectedInputDevice) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					if (mWebView != null) {
						if(imm.hideSoftInputFromWindow(mWebView.getWindowToken(), 0))
							return true;
					}
					onBackPressed();
					return true;
				// } else {
					// return super.onKeyUp(keyCode, event);
				// }
			case KeyEvent.KEYCODE_BUTTON_A:
			case KeyEvent.KEYCODE_BUTTON_X:
			case KeyEvent.KEYCODE_BUTTON_Y:
			case KeyEvent.KEYCODE_BUTTON_SELECT:
			case KeyEvent.KEYCODE_BUTTON_START:
			case KeyEvent.KEYCODE_BUTTON_L1:
			case KeyEvent.KEYCODE_BUTTON_R1:
				if (m_SelectedInputDevice != null && event.getDevice() == m_SelectedInputDevice) {
					return true;
				} else {
					return super.onKeyUp(keyCode, event);
				}
			default:
				return super.onKeyUp(keyCode, event);
			}
		}
		else {
			switch(keyCode)
			{		
			case KeyEvent.KEYCODE_BUTTON_B:
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (mWebView != null) {
					if(imm.hideSoftInputFromWindow(mWebView.getWindowToken(), 0))
						return true;
				}
				onBackPressed();
				return true;
			default:
				return super.onKeyUp(keyCode, event);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (!isKeyBoardShown) {
			float new_pos;
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_BUTTON_A:
			case KeyEvent.KEYCODE_BUTTON_L1:
			case KeyEvent.KEYCODE_BUTTON_R1:
				simulateTouchEvent();
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				new_pos = cursor.getY() + 10.0f;
				if (new_pos <= SCR_H - cursor.getHeight()) {
					cursor.setY(new_pos);
				} else {
					mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PAGE_DOWN));
					mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PAGE_DOWN));
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				new_pos = cursor.getY() - 10.0f;
				if (new_pos >= 0) {
					cursor.setY(new_pos);
				} else {
					mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PAGE_UP));
					mWebView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PAGE_UP));
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				new_pos = cursor.getX() - 10.0f;
				if (new_pos >= 0) {
					cursor.setX(new_pos);
				} 
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				new_pos = cursor.getX() + 10.0f;
				if (new_pos <= SCR_W - cursor.getWidth()) {
					cursor.setX(new_pos);
				} 		
				return true;				
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private InputDevice findBySource(int sourceType) {
		int[] ids = InputDevice.getDeviceIds();
		// Return the first matching source we find
		for (int i = 0; i < ids.length; i++) {
			InputDevice dev = InputDevice.getDevice(ids[i]);
			int sources = dev.getSources();

			if ((sources & ~InputDevice.SOURCE_CLASS_MASK & sourceType) != 0) {
				if (!dev.getName().contains("ats_input")) {
				#if TARGET_API_LEVEL_INT>15
					if (Build.VERSION.SDK_INT > 15) {
						if(!dev.getDescriptor().contains("ats_input")) {
							return dev;
						}
					}
				#endif
					return dev;
				}
			}
		}
		return null;
	}
	
	private void simulateTouchEvent() 
	{
		try {
			// Obtain MotionEvent object
			long downTime = SystemClock.uptimeMillis();
			long eventTime = SystemClock.uptimeMillis() + 100;
			float x = cursor.getX() + (cursor.getWidth() / 2);
			float y = cursor.getY() + (cursor.getHeight() / 2);
			// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
			int metaState = 0;
			MotionEvent motionEvent = MotionEvent.obtain(
				downTime, 
				eventTime, 
				MotionEvent.ACTION_DOWN, 
				x, 
				y, 
				metaState
			);

			// Dispatch touch event to view
			mView.dispatchTouchEvent(motionEvent);
			
			motionEvent = MotionEvent.obtain(
				downTime, 
				eventTime, 
				MotionEvent.ACTION_UP, 
				x, 
				y, 
				metaState
			);

			// Dispatch touch event to view
			mView.dispatchTouchEvent(motionEvent);
		} catch(Exception e) { }
	}
	
	private static void ShowBrowserWithCtg(String i_templateUrl, String i_templatePost, String i_ctg)
	{
		String url = i_templateUrl + solveTemplate(i_templatePost) + "&ctg=" + i_ctg;
		DBG("IGB", "URL: " +url);

		if (i_ctg.contains("UPDATE") || i_ctg.contains("GAME_REVIEW"))
		{
			InGameBrowser.openBrowser(url);
		}
		else 
		{
			InGameBrowser.showInGameBrowserWithUrl(url);
		}
	}
	
	public static void ShowCustomerCare(int lang, String i_anonymousAccount, String i_facebookID, String i_googleAccount, boolean isBanned, int banType)
	{
		InGameBrowser.languageIndex 	= lang;
	
		InGameBrowser.anonymousAccount 	= i_anonymousAccount;
		InGameBrowser.facebookID 		= i_facebookID;
		InGameBrowser.googleAccount 	= i_googleAccount;
	
		if(!isBanned)
		{
			InGameBrowser.showCustomerCare();
		}
		else if(banType != -1) // ban CC with type
		{
			showCustomerCareWithBANType(banType);
		}
		else //Banned CC,
		{	
			ShowBrowserWithCtg(REDIR_URL, INGAMEBROWSER_POST_TEMPLATE, "BANNED");
		}
	}
	
	public static void ShowTermsOfUse(int lang)
	{
		InGameBrowser.languageIndex 	= lang;
		ShowBrowserWithCtg(REDIR_URL, INGAMEBROWSER_POST_TEMPLATE, "TERMS");
	}
	
	public static void ShowPrivacyPolicy(int lang)
	{
		InGameBrowser.languageIndex 	= lang;
		ShowBrowserWithCtg(REDIR_URL, INGAMEBROWSER_POST_TEMPLATE, "PRIVACY");
	}
	
	public static void RedirectNewVersionScreen()
	{
		// ShowBrowserWithCtg(REDIR_URL, INGAMEBROWSER_POST_TEMPLATE, "UPDATE");
		ShowBrowserWithCtg(REDIR_URL, INGAMEBROWSER_POST_TEMPLATE_UPDATE_RATE, "UPDATE");
	}
	
	public static void RedirectRateThisApp()
	{
		// ShowBrowserWithCtg(REDIR_URL, INGAMEBROWSER_POST_TEMPLATE, "GAME_REVIEW");
		ShowBrowserWithCtg(REDIR_URL, INGAMEBROWSER_POST_TEMPLATE_UPDATE_RATE, "GAME_REVIEW");
	}

	public static void SetGameLanguage(String language) 
	{
		int index = Arrays.asList(TXT_IGB_LANGUAGES).indexOf(language);
		if (index != -1) 
		{
			languageIndex = index;
		}
	}
	
	public static void showCustomerCare() {
		try {
			DBG("INGAMEBROWSER", "showCustomerCare()");
			Intent i = new Intent(SUtils.getContext(), InGameBrowser.class);
			SUtils.getContext().startActivity(i);
		} catch (Exception e) { }
	}

	public static void showCustomerCareWithBANType(int banType) {
		try {
			DBG("INGAMEBROWSER", "showCustomerCareWithBANType: " + banType);
			Intent i = new Intent(SUtils.getContext(), InGameBrowser.class);
			i.putExtra("ban_type", banType);
			SUtils.getContext().startActivity(i);
		} catch (Exception e) { }
	}
	
	public static void showInGameBrowserWithUrl(String url) { 
		try {
			DBG("INGAMEBROWSER", "Starting with URL: " + url);
			Intent i = new Intent(SUtils.getContext(), InGameBrowser.class);
			i.putExtra("extra_url", url);
			SUtils.getContext().startActivity(i);
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
	}
	
	public static void showForum(int language) {
		if(language < 0)
			language = 0;
		
		// Set the language before starting forum
		languageIndex = language;
	
		try {
			DBG("INGAMEBROWSER", "showForum()");
			Intent i = new Intent(SUtils.getContext(), InGameBrowser.class);
			i.putExtra("extra_url", "forum");
			SUtils.getContext().startActivity(i);
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
	}
	
	public static void showNews(int language, String i_anonymousAccount, String i_facebookID, String i_googleAccount) {
		if(language < 0)
			language = 0;
		
		// Set the language before showing the news
		languageIndex = language;
		InGameBrowser.anonymousAccount 	= i_anonymousAccount;
		InGameBrowser.facebookID 		= i_facebookID;
		InGameBrowser.googleAccount 	= i_googleAccount;
		
		try {
			DBG("INGAMEBROWSER", "showNews()");
			Intent i = new Intent(SUtils.getContext(), InGameBrowser.class);
			i.putExtra("extra_url", "news");
			SUtils.getContext().startActivity(i);
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
	}
	
	public static void refreshUnreadNewsNumber(String i_anonymousAccount, String i_facebookID, String i_googleAccount) {
		DBG("INGAMEBROWSER", "refreshUnreadNewsNumber()");
		InGameBrowser.anonymousAccount 	= i_anonymousAccount;
		InGameBrowser.facebookID 		= i_facebookID;
		InGameBrowser.googleAccount 	= i_googleAccount;
		
		new Thread() {
			public void run() {
				String lastNewsIndex = getLastNewsIndex();		
				String serverUrl = CHECK_UNREAD_NEWS_NUMBER.replace("LAST_ID", lastNewsIndex);
				String postData = solveTemplate(INGAMEBROWSER_POST_TEMPLATE);
				
				DBG("INGAMEBROWSER", "connecting to " + serverUrl + "\nwith post data:\n" + postData);
				
				String response = postData(serverUrl, postData);				
				try {
					JSONObject json = new JSONObject("" + response);
					saveJsonData(json, false);
				} catch (JSONException e) { 
					DBG_EXCEPTION(e);
				}
				
				DBG("INGAMEBROWSER", "response received: " + response);
			}
		}.start();
	}
	
	private static void refreshUnreadNewsNumberInternal() {
		DBG("INGAMEBROWSER", "refreshUnreadNewsNumberInternal()");
		new Thread() {
			public void run() {
				String serverUrl = SAVE_NEWS_ID.replace("LAST_ID", getLastNewsIndex());
				String postData = solveTemplate(INGAMEBROWSER_POST_TEMPLATE);
				
				DBG("INGAMEBROWSER", "connecting to " + serverUrl + "\nwith post data:\n" + postData);
				
				String response = postData(serverUrl, postData);				
				try {
					JSONObject json = new JSONObject("" + response);
					saveJsonData(json, true);
				} catch (JSONException e) { 
					DBG_EXCEPTION(e);
				}
				
				DBG("INGAMEBROWSER", "response received: " + response);
			}
		}.start();
	}
	
	private static void saveJsonData(JSONObject json, boolean saveNumber) {
		boolean success = json.optBoolean("success", false);		
		if (success) {
			if (saveNumber) {
				String lastNewsIndex = json.optString("current-id", "-1");
				saveLastNewsIndex(lastNewsIndex);
			}
				
			unreadNewsNumber = json.optInt("unread", -1);			
			try {
				nativeUnreadNewsChangedCallback(unreadNewsNumber);
			} catch (UnsatisfiedLinkError e) { }
			
			if (s_callback != null) {
				s_callback.onUnreadNewsChanged(unreadNewsNumber);
			}
		}
	}
		
	public static void setUnreadNewsChangedCallback(UnreadNewsChangedCallback callback) {
		s_callback = callback;
	}
	
	public static int getUnreadNewsNumber() {
		return unreadNewsNumber;
	}
	
	private static void saveLastNewsIndex(String lastNewsIndex) {
		try {
			SharedPreferences settings = SUtils.getContext().getSharedPreferences("InGameBrowser_lastNewsIndex", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("LAST_NEWS_INDEX", lastNewsIndex);
			editor.commit();
		} catch(Exception e) { DBG_EXCEPTION(e); }
	}
		
	private static String getLastNewsIndex() {
		try {
			SharedPreferences settings = SUtils.getContext().getSharedPreferences("InGameBrowser_lastNewsIndex", 0);
			return settings.getString("LAST_NEWS_INDEX", "-1");
		} catch(Exception e) { DBG_EXCEPTION(e); }
		
		return "-1";
	}
	
	private static String postData(String url, String params) {
		String result = null;
		BufferedReader stream_in = null;    	
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			
			String[] _params = params.split("[&]");
			
			for (String _param : _params) {
				int idx = _param.indexOf("=");
				if (idx != -1 && idx != _param.length()-1)
				{
					String name = _param.substring(0, idx);
					String value = _param.substring(idx+1);
					nameValuePairs.add(new BasicNameValuePair(name, value));
				}
				else if (idx == _param.length()-1)
				{
					String name = _param.substring(0, idx);
					nameValuePairs.add(new BasicNameValuePair(name, ""));
				}
			}

			DBG("INGAMEBROWSER", "Making post at: " + url + "\n" + nameValuePairs.toString());

	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    
			HttpResponse response = httpclient.execute(httppost);
			
			stream_in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer buffer = new StringBuffer("");
            String line = "";
            while ((line = stream_in.readLine()) != null)
            {
            	buffer.append(line + "\n");
            }
            stream_in.close();
            result = buffer.toString();
            
		} catch (Exception e) { 
			e.printStackTrace();
			result = null;
		}
		
		return result;
	}
	
	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();			
		} else {
			exit();
		}
	}
	
	@Override
	public void onDestroy() {
		if (CTG_TYPE.equals("NEWS"))
			refreshUnreadNewsNumberInternal();

		if (mWebView != null) {
			try {
				if (Build.VERSION.SDK_INT >= 11)				
					mWebView.onPause();
				mWebView.loadUrl("about:blank");
				mWebView.destroyDrawingCache();
				mWebView.destroy();
				mWebView = null;
			} catch (Exception e) { }
		}
		
		super.onDestroy();
	}

	@Override
	public void onPause() {		
		DBG("INGAMEBROWSER", "onPause()");
		
		if (mWebView != null) {
			DBG("INGAMEBROWSER", "mWebview onPause()");
			if (Build.VERSION.SDK_INT >= 11)				
				mWebView.onPause();
		}
		
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();				
			
		if (mWebView != null) {
			DBG("INGAMEBROWSER", "mWebview onResume()");
			if (Build.VERSION.SDK_INT >= 11)				
				mWebView.onResume();
		}
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		int action = event.getAction();
		switch (action) 
		{
		case MotionEvent.ACTION_DOWN:
			((ImageButton)view).setBackgroundColor(Color.parseColor("#a3a3a3"));
			return true;

		case MotionEvent.ACTION_UP:
			if (!(x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight())) {
				handleClick(view.getId());
			} 			
			((ImageButton)view).setBackgroundColor(Color.TRANSPARENT);
			return true;
			
		case MotionEvent.ACTION_MOVE:
			if (x < 0 || x > view.getWidth() || y < 0 || y > view.getHeight())
				((ImageButton)view).setBackgroundColor(Color.TRANSPARENT);
			else
				((ImageButton)view).setBackgroundColor(Color.parseColor("#a3a3a3"));
			return true;
		default:
			return false;
		}
	}

	private void handleClick(int id) {
		switch(id) {
		case R.id.ingamebrowser_backbutton:
			mWebView.goBack();
			break;
		case R.id.ingamebrowser_forwardbutton:
			mWebView.goForward();
			break;
		case R.id.ingamebrowser_refreshbutton:
			mWebView.reload();
			break;			
		case R.id.ingamebrowser_closebutton:
			finish();
			break;
		}
	}

	private void exit() {
		gIsRunning = false;
		finish();
	}
	
	private ProgressDialog progress_loading = null;

	private void showProgress(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				try {
					hideProgress();
					progress_loading = ProgressDialog.show(
							InGameBrowser.this, null, message);
					progress_loading.setCancelable(true);
				} catch (Exception e) {
				}
			}
		});
	}

	private void showProgressLoading() {
		showProgress(getString(TXT_LOADING[languageIndex]));
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
	
	private static void openBrowser(String url) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		DBG("INGAMEBROWSER", "openBrowser: " + url);

		if (i != null) {
			DBG("INGAMEBROWSER", "openBrowser: intent not null");
			if (SUtils.getContext().getPackageManager().queryIntentActivities(i,
					PackageManager.MATCH_DEFAULT_ONLY).size() != 0) {
				DBG("INGAMEBROWSER", "openBrowser: activity found for intent");
				SUtils.getContext().startActivity(i);
			} else {
				DBG("INGAMEBROWSER", "openBrowser: no activity found for intent");
			}
		} else {
			DBG("INGAMEBROWSER", "openBrowser: intent is null");
		}
	}

	private void openAmazon(String url) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		DBG("INGAMEBROWSER", "openAmazon: " + url);

		if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() != 0) {
			DBG("INGAMEBROWSER", "openAmazon: activity found for intent");
			startActivity(i);
			return;
		} else {
			DBG("INGAMEBROWSER", "openAmazon: no activity found for intent");
		}

		if (url.contains("www.amazon.com")) {
			try {
				DBG("INGAMEBROWSER", "openAmazon: amzn:// failed, trying http://");
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

		DBG("INGAMEBROWSER", "openGooglePlay: " + url);

		if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() != 0) {
			DBG("INGAMEBROWSER", "openGooglePlay: activity found for intent");
			startActivity(i);
			return;
		} else {
			DBG("INGAMEBROWSER", "openGooglePlay: no activity found for intent");
		}

		try {
			DBG("INGAMEBROWSER", "openGooglePlay: market:// failed, trying http://play.google.com");
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(url.replaceAll(
					"market://details?",
					"https://play.google.com/store/apps/details?")
					.replaceAll("market://search?", "https://play.google.com/store/search?")));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		} catch (Exception e) {
		}
	}

	private void openSkt(String productId) {
		Intent i = getPackageManager().getLaunchIntentForPackage("com.skt.skaf.A000Z00040");

		DBG("INGAMEBROWSER", "openSkt: " + productId);

		if (i != null) {
			DBG("INGAMEBROWSER", "openSkt: intent is not null");
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			i.setClassName("com.skt.skaf.A000Z00040", "com.skt.skaf.A000Z00040.A000Z00040");
			i.setAction("COLLAB_ACTION");
			i.putExtra("com.skt.skaf.COL.URI", ("PRODUCT_VIEW/" + productId + "/0").getBytes());
			i.putExtra("com.skt.skaf.COL.REQUESTER", "A000Z00040");
		} else {
			DBG("INGAMEBROWSER", "openSkt: intent is null, opening web page");
			i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://tsto.re/" + productId));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		startActivity(i);
	}
	
	class IGBWebViewClient extends WebViewClient {
		@Override
		public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			DBG("INGAMEBROWSER", "======= onPageStarted =======\n" + url);
			showProgressLoading();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			DBG("INGAMEBROWSER", "======= onPageFinished ========\n" + url);
			hideProgress();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			DBG("INGAMEBROWSER", "======= shouldOverrideUrlLoading =======\n" + url);
			
			if (url.startsWith("play:")) {
				try {
					String package_name = url.replace("play:", "").split("[?]")[0];
					DBG("INGAMEBROWSER", "***************app to be launched: " + package_name);

					PackageManager manager = getPackageManager();
					Intent intent = manager.getLaunchIntentForPackage(package_name);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					startActivity(intent);
				} catch (Exception e) {
					DBG_EXCEPTION(e);
				}
				return true;
			}
			else if(url.startsWith("mailto:")) {
		        try { 
		        	Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
    				startActivity(intent);
    			} catch(Exception e) {
					DBG_EXCEPTION(e);
				} 
		        return true;
		    }
			else if (url.startsWith("goto:")) 
			{					
				//av TODO: wtf??????? => call splashScreen func with goto..
				//CLASS_NAME.m_sInstance.splashScreenFunc(url.replace("goto:", "")); // This calls the method public void splashScreenFunc(String name) from the game's main activity class (CLASS_NAME). Make sure to implement it.								
				return true;
			}							
			else if (url.equals("exit:") || url.equals("unavailable:")) {
				exit();
				return true;
			} else if (url.startsWith("market://")) {
				openGooglePlay(url);
				return true;
			} else if (url.startsWith("amzn://")) {
				openAmazon(url);
				return true;
			} else if (url.contains("www.amazon.com")) {
				openAmazon(url.replaceAll("http://", "amzn://").replaceAll("https://", "amzn://"));
				return true;
			} else if (url.startsWith("skt:")) {
				openSkt(url.replaceAll("skt:", ""));
				return true;
			} else if (url.startsWith("link:")) {
				String link = url.replaceAll("link:", "");
				if (link.contains("www.amazon.com")) {
					openAmazon(link.replaceAll("http://", "amzn://").replaceAll("https://", "amzn://"));
					return true;
				}
				openBrowser(link);
				return true;
			} else if (url.contains("/redir/")) {
				if (url.contains("type=SKTMARKET")) {
					view.loadUrl(url.replaceAll("&pp=1", ""));
					return true;
				}
				return false;
			} else {
				//openBrowser(url);
				return false;
			}
		}

		@Override
		public void onReceivedError(final WebView view, int errorCode, final String description, String failingUrl) {
			runOnUiThread(new Runnable() {
				public void run() {
					view.setVisibility(View.INVISIBLE);
					try {
						AlertDialog dialog = new AlertDialog.Builder(
								InGameBrowser.this)
								.setPositiveButton(
										getString(TXT_OK[languageIndex]),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												exit();
											}
										})
								.setOnCancelListener(
										new DialogInterface.OnCancelListener() {
											public void onCancel(
													DialogInterface dialog) {
												exit();
											}
										})
								.setMessage(
										getString(TXT_NET_ERROR[languageIndex]))
								.create();
						dialog.setCanceledOnTouchOutside(false);
						dialog.show();
					} catch (Exception e) {
					}
				}
			});
		}
	}
}
