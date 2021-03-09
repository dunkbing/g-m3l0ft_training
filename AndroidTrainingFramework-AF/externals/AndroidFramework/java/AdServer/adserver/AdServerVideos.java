package APP_PACKAGE;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;

import org.apache.http.protocol.HTTP;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.media.*;
import android.media.MediaPlayer.*;
import android.net.*;
import android.net.wifi.*;
import android.net.http.*;
import android.os.*;
import android.provider.Settings.Secure;
import android.telephony.*;
import android.util.*;
import android.view.*;
import android.view.View.OnKeyListener;
import android.webkit.WebSettings.*;
import android.webkit.*;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;

import APP_PACKAGE.GLUtils.*;

public class AdServerVideos extends Activity 
{
	private WebView mWebView = null;
	private RelativeLayout mView = null;
	private RelativeLayout mVideoContainer = null;
	private VideoView mVideoView = null;
	
	private boolean isVideoPlaying = false;
	
	private String STRING_OFFERWALL_SCRIPT = "https://ingameads.gameloft.com/redir/freecash/ro_view.php";
	private String STRING_DIRECTVIDEO_SCRIPT = "https://ingameads.gameloft.com/redir/freecash/rv_view.php";
	private String STRING_CINEMA_SCRIPT = "https://ingameads.gameloft.com/redir/freecash/cinema_view.php";
	#if HDIDFV_UPDATE
	private String STRING_PARAMS_TEMPLATE = "from=GAME_CODE&lg=LANG&country=COUNTRY&udid=UDID&hdidfv=HDIDFV&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=GAME_VER&conn=CONNSPEED&igp_rev=1004&os=android";
	#else
	private String STRING_PARAMS_TEMPLATE = "from=GAME_CODE&lg=LANG&country=COUNTRY&udid=UDID&androidid=ANDROIDID&d=DEVICE&f=FIRMWARE&game_ver=GAME_VER&conn=CONNSPEED&igp_rev=1004&os=android";
	#endif
	
	private String game_code = "";
	private String game_ver = "";
	private String lang = "EN";
	
	private boolean isBackPressed = false;
	
	private String getTranslation(String input)
	{
		if (input.equals("loading"))
		{
			if (lang.equals("EN"))
				return getString(R.string.ADS_LOADING_EN, this);
			else if (lang.equals("FR"))
				return getString(R.string.ADS_LOADING_FR, this);
			else if (lang.equals("DE"))
				return getString(R.string.ADS_LOADING_DE, this);
			else if (lang.equals("IT"))
				return getString(R.string.ADS_LOADING_IT, this);
			else if (lang.equals("SP"))
				return getString(R.string.ADS_LOADING_SP, this);
			else if (lang.equals("JP"))
				return getString(R.string.ADS_LOADING_JP, this);
			else if (lang.equals("KR"))
				return getString(R.string.ADS_LOADING_KR, this);
			else if (lang.equals("CN"))
				return getString(R.string.ADS_LOADING_CN, this);
			else if (lang.equals("BR"))
				return getString(R.string.ADS_LOADING_BR, this);
			else if (lang.equals("RU"))
				return getString(R.string.ADS_LOADING_RU, this);
			else if (lang.equals("TR"))
				return getString(R.string.ADS_LOADING_TR, this);
			else if (lang.equals("AR"))
				return getString(R.string.ADS_LOADING_AR, this);
			else if (lang.equals("TH"))
				return getString(R.string.ADS_LOADING_TH, this);
			else if (lang.equals("ID"))
				return getString(R.string.ADS_LOADING_ID, this);
			else if (lang.equals("VI"))
				return getString(R.string.ADS_LOADING_VI, this);
			else if (lang.equals("ZT"))
				return getString(R.string.ADS_LOADING_ZT, this);
			else
				return getString(R.string.ADS_LOADING_EN, this);
		}
		return null;
	}
	
	private String SolveTemplate(String input)
	{
		String serial = Device.getSerial();
		
		String result = input
				.replaceAll("GAME_CODE", game_code)
				.replaceAll("LANG", lang)
				.replaceAll("COUNTRY", Locale.getDefault().getCountry())
			#if (HDIDFV_UPDATE == 2)
				.replaceAll("UDID", serial)
			#else
				.replaceAll("UDID", Device.getDeviceId())
			#endif
			#if HDIDFV_UPDATE
				.replaceAll("HDIDFV", Device.getHDIDFV())
			#endif
				.replaceAll("ANDROIDID", Device.getAndroidId())
				.replaceAll("DEVICE", GetDevice())
				.replaceAll("FIRMWARE", GetFirmware())
				.replaceAll("GAME_VER", game_ver)
				.replaceAll("CONNSPEED", Connectivity.isConnectedFast(this) ? "fast" : "slow")
				.replaceAll(" ", "");
		#if HDIDFV_UPDATE
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#endif
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
		result += "&width=" + display.getWidth();
		result += "&height=" + display.getHeight();
		
		DBG("ADSERVER_VIDEOS", "============ Unencrypted query:\n" + result);
		
		return ("data=" + Encrypter.crypt(result) + "&enc=1");
	}
			
	String GetDevice()
	{
		return (Build.MANUFACTURER + "_" + Build.MODEL).replaceAll(" ", "");
	}
	
	String GetFirmware()
	{
		return Build.VERSION.RELEASE;
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
					AdServerVideos.this.onKeyDown(event.getKeyCode(), event);
				else if ((event.getAction() == KeyEvent.ACTION_UP))
					AdServerVideos.this.onKeyUp(event.getKeyCode(), event);
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
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		SUtils.setContext(this);
		
		setContentView(R.layout.activity_offer_wall);
		
		mView = (RelativeLayout) findViewById(R.id.mView);
		mView.setBackgroundColor(Color.TRANSPARENT);

		RelativeLayout mWebLayout = (RelativeLayout) findViewById(R.id.mWebView);
		mWebView = new WebView1(this);
		mWebView.setBackgroundColor(Color.argb(1, 255, 255, 255));
		mWebView.setVerticalScrollbarOverlay(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAppCacheEnabled(false);	
		mWebView.getSettings().setSupportZoom (false);
		mWebView.getSettings().setDefaultTextEncodingName(HTTP.UTF_8);
		mWebView.getSettings().setLightTouchEnabled(true);
		mWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		mWebView.addJavascriptInterface(new JSInterface(this, mView), "Android");
		
	#if (TARGET_API_LEVEL_INT >= 21)
		if (Build.VERSION.SDK_INT >= 21) {
			mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
	#endif
		
		if (Build.VERSION.SDK_INT >= 19) {
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
		}
		
	#if TARGET_API_LEVEL_INT>16		
		if (Build.VERSION.SDK_INT >= 17)
			mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
	#endif
		
		mWebView.setWebViewClient(new glWebClient());
		mWebView.setWebChromeClient(new android.webkit.WebChromeClient() 
		{
			public boolean onConsoleMessage(android.webkit.ConsoleMessage cm) {
				DBG("JS_Console", cm.messageLevel().toString() + ": " + cm.message() + " -- From line " + cm.lineNumber());
				return true;
			}
		});
		
		mWebLayout.addView(mWebView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		
		String action = getIntent().getStringExtra("action");
		game_code = getIntent().getStringExtra("game_code");
		game_ver = getIntent().getStringExtra("game_ver");
		lang = getIntent().getStringExtra("lang");
		
		if (action != null)
		{
			String url = null;
			if (action.equals("offers"))
				url = STRING_OFFERWALL_SCRIPT + "?" + SolveTemplate(STRING_PARAMS_TEMPLATE);				
			else if (action.equals("direct"))
				url = STRING_DIRECTVIDEO_SCRIPT + "?" + SolveTemplate(STRING_PARAMS_TEMPLATE);
			else if (action.equals("cinema"))
				url = STRING_CINEMA_SCRIPT + "?" + SolveTemplate(STRING_PARAMS_TEMPLATE);
			
			if (url != null)
				mWebView.loadUrl(url);
		}
		else
		{
			exit();
		}
	}
	
	private Bitmap fetchImage(String urlstr)
	{
	    try
	    {
	        URL url = new URL(urlstr);

	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream is = connection.getInputStream();
	        Bitmap img = BitmapFactory.decodeStream(is);
	        return img;
	    }
	    catch (MalformedURLException e)
	    {
	    	DBG_EXCEPTION(e);
	        DBG("ADSERVER_VIDEOS" , "RemoteImageHandler: fetchImage passed invalid URL: " + urlstr ) ;
	    }
	    catch (IOException e)
	    {
	    	DBG_EXCEPTION(e);
			DBG("ADSERVER_VIDEOS" , "RemoteImageHandler: fetchImage IO exception: " + e ) ;
	    }
	    return null;
	}
	
	public void exit()
	{
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
			try {
				mWebView.loadUrl("javascript:onHide()");
				if (Build.VERSION.SDK_INT >= 11)				
					mWebView.onPause();
				// mWebView.pauseTimers();
				mWebView.loadUrl("about:blank");
				mView.removeAllViews();
				mWebView.destroyDrawingCache();
				mWebView.destroy();
				mWebView = null;
			} catch (Exception e) { }
		}
		
		finish();
	}
	
	@Override
	public void onPause() {
		if (isVideoPlaying && mVideoView != null) {
			try {
				mVideoView.suspend();
			} catch (Exception e) { }
		}
		
		DBG("ADSERVER_VIDEOS", "onPause()");
		
		if (mWebView != null) {
			DBG("ADSERVER_VIDEOS", "mWebview onPause()");
			mWebView.loadUrl("javascript:onPause()");
			if (Build.VERSION.SDK_INT >= 11)				
				mWebView.onPause();
			// mWebView.pauseTimers();
		}
		
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		
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
					if (Build.VERSION.SDK_INT >= 11)				
						mWebView.onResume();
					// mWebView.resumeTimers();
					mWebView.loadUrl("javascript:onResume()");
				}
			}});
			
		}}.start();
	}
	
	@Override
	public void onDestroy()
	{
		AdServer.wasAdServerVideosLoaded = true;
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed()
	{
		if (!isVideoPlaying )
		{	
			if (isBackPressed && (mWebView != null)) {
				mWebView.loadUrl("javascript:onBackPressed()");
			} else {
				exit();
			}
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{		
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_BUTTON_B:
			onBackPressed();
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
	
	class JSInterface
	{
		Context context;
		ViewGroup root;
		
		JSInterface(Context ctx, ViewGroup main)
		{
			context = ctx;
			root = main;			
		}
		
		#if TARGET_API_LEVEL_INT>16
		@JavascriptInterface
		#endif
		public void playVideo(final String url, final String endHandler, final String exitVideo, final String buttonUrl)
		{
			DBG("ADSERVER_VIDEOS", "======= playVideo =======\n" + url + "\n" + endHandler + "\n" + exitVideo + "\n" + buttonUrl);
			
			if (!isVideoPlaying)
			{
				final Bitmap closeButton = fetchImage(buttonUrl);
				
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						isVideoPlaying = true;
						
						mVideoContainer = new RelativeLayout(context);
						mVideoContainer.setBackgroundColor(Color.BLACK);
						setContentView(mVideoContainer);
						
						mVideoView = new VideoView(context);						
						
			            LayoutParams l_params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		            	l_params.addRule(RelativeLayout.CENTER_IN_PARENT);
			            
			            mVideoContainer.addView(mVideoView, l_params);
			            
			            final ProgressDialog pd = ProgressDialog.show(AdServerVideos.this, null, getTranslation("loading"));
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
	
	class glWebClient extends WebViewClient
	{
		ProgressDialog dialog = null;
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			DBG("ADSERVER_VIDEOS", "======= onPageStarted =======\n" + url);
			
			try {
				if (dialog != null)
				{
					dialog.dismiss();
					dialog = null;
					System.gc();
				}
				
				dialog = ProgressDialog.show(AdServerVideos.this, null, getTranslation("loading"));
				dialog.setCancelable(true);
			} catch (Exception e) { }
		}
		
		@Override
		public void onPageFinished(WebView view, String url)
		{
			AdServer.wasAdServerVideosLoaded = true;
			DBG("ADSERVER_VIDEOS", "======= onPageFinished ========\n" + url);
			isBackPressed = false;
			view.loadUrl("javascript:window.Android.checkBackPressed(isBackPressed)");
			try {
				if (dialog != null) {
					dialog.dismiss();
				}
			} catch (Exception e) { }
		}
		
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
		{
			handler.proceed();
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
		{
			AdServer.wasAdServerVideosError = true;
			exit();
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			DBG("ADSERVER_VIDEOS", "======= shouldOverrideUrlLoading =======\n" + url);
			if (url.equals("exit:"))
			{
				if (dialog != null)
				{
					dialog.dismiss();
				}
				exit();
				return true;
			}
			else if (url.equals("unavailable:"))
			{
				if (dialog != null)
				{
					dialog.dismiss();
				}
				AdServer.wasAdServerVideosError = true;
				exit();
				return true;
			}
			else if (url.startsWith("market://") || url.startsWith("amzn://") || url.contains("www.amazon.com") || url.contains("play.google.com"))
			{
				OpenBrowser(url);
				return true;
			}
			else if (url.startsWith("link:"))
			{
				OpenBrowser(url.replace("link:", ""));
				return true;
			}
			else if(url.startsWith("play:"))
			{
				try {
					String package_name = url.replace("play:", "").split("[?]")[0];
					DBG("ADSERVER_VIDEOS", "***************app to be launched: " + package_name);

					PackageManager manager = getPackageManager();
					Intent intent = manager.getLaunchIntentForPackage(package_name);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					startActivity(intent);
				} catch (Exception e) {
					DBG_EXCEPTION(e);
					if (INSTALL_URL_IF_GAME_NOT_INSTALLED != null) {
						DBG("ADSERVER_VIDEOS", "The game was not found on the device, proceeding to download page");
						view.loadUrl(INSTALL_URL_IF_GAME_NOT_INSTALLED);
						INSTALL_URL_IF_GAME_NOT_INSTALLED = null;
					}
				}
				return true;
			}
			else if((url.contains("ingameads.gameloft.com/redir/?from") || url.contains("ingameads.gameloft.com/redir/index.php?from")) && !url.contains("ctg=PLAY"))
			{
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
				return true;
			}
			else if(url.contains("ctg=PLAY"))
			{
				INSTALL_URL_IF_GAME_NOT_INSTALLED = url.replace("t=bundle", "t=game").replace("ctg=PLAY", "ctg=FRINSTALL&old_ctg=PLAY");

				if (url.contains("type=SKTMARKET") && url.contains("&pp=1")) 
				{
					INSTALL_URL_IF_GAME_NOT_INSTALLED = INSTALL_URL_IF_GAME_NOT_INSTALLED.replaceAll("&pp=1", "");

					view.loadUrl(url.replaceAll("&pp=1", ""));
					return true;
				}
			}

			return false;						
		}
	}

	private String INSTALL_URL_IF_GAME_NOT_INSTALLED = null;
	
	private void OpenBrowser(String url)
	{
		try
		{
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(i);
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
	
	@SuppressWarnings("unused")
	private static class Connectivity {

	    /**
	     * Get the network info
	     * @param context
	     * @return
	     */
	    public static NetworkInfo getNetworkInfo(Context context){
	        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        return cm.getActiveNetworkInfo();
	    }

	    /**
	     * Check if there is any connectivity
	     * @param context
	     * @return
	     */
	    public static boolean isConnected(Context context){
	        NetworkInfo info = Connectivity.getNetworkInfo(context);
	        return (info != null && info.isConnected());
	    }

	    /**
	     * Check if there is any connectivity to a Wifi network
	     * @param context
	     * @param type
	     * @return
	     */
	    public static boolean isConnectedWifi(Context context){
	        NetworkInfo info = Connectivity.getNetworkInfo(context);
	        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	    }

	    /**
	     * Check if there is any connectivity to a mobile network
	     * @param context
	     * @param type
	     * @return
	     */
	    public static boolean isConnectedMobile(Context context){
	        NetworkInfo info = Connectivity.getNetworkInfo(context);
	        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	    }

	    /**
	     * Check if there is fast connectivity
	     * @param context
	     * @return
	     */
	    public static boolean isConnectedFast(Context context){
	        NetworkInfo info = Connectivity.getNetworkInfo(context);
	        return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(),info.getSubtype()));
	    }

	    /**
	     * Check if the connection is fast
	     * @param type
	     * @param subType
	     * @return
	     */
	    public static boolean isConnectionFast(int type, int subType){
	        if(type==ConnectivityManager.TYPE_WIFI){
	            return true;
	        }else if(type==ConnectivityManager.TYPE_MOBILE){
	            switch(subType){
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
	            /*
	             * Above API level 7, make sure to set android:targetSdkVersion 
	             * to appropriate level to use these
	             */
	            case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11 
	                return true; // ~ 1-2 Mbps
	            case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
	                return true; // ~ 5 Mbps
	            case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
	                return true; // ~ 10-20 Mbps
	            case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
	                return false; // ~25 kbps 
	            case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
	                return true; // ~ 10+ Mbps
	            // Unknown
	            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
	            default:
	                return false;
	            }
	        }else{
	            return false;
	        }
	    }
	}
}
