package APP_PACKAGE;

import java.io.*;
import java.net.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.*;
import android.net.http.*;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

public class AdServerInterstitial extends Activity
{
	private WebView mWebView = null;
	private RelativeLayout mView = null;
	
	private String ADS_INTERSTITIAL_URL = null;	
	
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
					AdServerInterstitial.this.onKeyDown(event.getKeyCode(), event);
				else if ((event.getAction() == KeyEvent.ACTION_UP))
					AdServerInterstitial.this.onKeyUp(event.getKeyCode(), event);
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
	
	
	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int SCR_H = display.getHeight();
		int SCR_W = display.getWidth();
		
		String deviceType = Build.MANUFACTURER + "_" + Build.MODEL;
		
		if (Build.VERSION.SDK_INT == 11 || Build.VERSION.SDK_INT == 12) {
			SCR_H -= 48;
		} else if (deviceType.toLowerCase().contains("kindle")) {
			SCR_H -= 20;
		}
		
		Bundle extras = getIntent().getExtras();
		ADS_INTERSTITIAL_URL = extras.getString("interstitial_url");
		if (ADS_INTERSTITIAL_URL == null) {
			ADS_INTERSTITIAL_URL = "http://www.google.com";
		}
		
		int orientation = extras.getInt("orientation", -10);
		if (orientation != -10)
			setRequestedOrientation(orientation);
		
		mView = new RelativeLayout(this);
		mWebView = new WebView1(this);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setBackgroundColor(0);
		mWebView.setInitialScale(100);
		mWebView.setWebViewClient(new glWebViewClient());
		mWebView.setWebChromeClient(new glWebChromeClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAppCacheEnabled(false);		
		mWebView.getSettings().setSupportZoom (false);
		mWebView.getSettings().setBuiltInZoomControls(false);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(false);
		mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		
	#if (TARGET_API_LEVEL_INT >= 21)
		if (Build.VERSION.SDK_INT >= 21) {
			mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
	#endif
		
		mWebView.setVisibility(View.INVISIBLE);
		mView.addView(mWebView, RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		setContentView(mView);
		
		mWebView.loadUrl(ADS_INTERSTITIAL_URL);
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
	
	private void startApp(String package_name)
	{
		try
		{
			Intent i = getPackageManager().getLaunchIntentForPackage(package_name);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			startActivity(i);
		}
		catch(Exception e) 
		{
			showToastNotification("Application is not installed");
			DBG("ADSERVER", "Exception occured when launching package " + package_name + " : " + e.getMessage());
		}
	}
	
	private void startBrowser(String url)
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
	
	private Intent getSktIntent(Context context, String strProductID)
	{
		PackageManager pm = context.getPackageManager();
		Intent intent = null;

		if(pm != null)
		{
			DBG("IGP_FREEMIUM","pm is not null");
			intent = pm.getLaunchIntentForPackage("com.skt.skaf.A000Z00040");
		}

		if(intent != null)
		{
			DBG("IGP_FREEMIUM","intent is not null");
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.setClassName("com.skt.skaf.A000Z00040", "com.skt.skaf.A000Z00040.A000Z00040");
			intent.setAction("COLLAB_ACTION");
			intent.putExtra("com.skt.skaf.COL.URI", ("PRODUCT_VIEW/" + strProductID + "/0").getBytes());
			intent.putExtra("com.skt.skaf.COL.REQUESTER", "A000Z00040");
		}

		return intent;
	}
	
	private String getHttpResponse(String urlString)  throws IOException
	{
		InputStream in = null;
		int response = -1;
		String str = "";

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
				while((ch = in.read()) != -1) {
					str += ((char)ch);
				}
			}
		}
		catch (Exception e)
		{
			throw new IOException("Error connecting: " + e.getMessage());
		}
		System.gc();
		
		return str;
	}
	
	private void showToastNotification(final String message)
    {
    	runOnUiThread(new Runnable() { public void run()
    	{
			DBG("ADSERVER", "+++++++++++ showToastNotification: " + message);
	    	Toast.makeText(AdServerInterstitial.this, message, Toast.LENGTH_SHORT).show();
    	}});
    }
	
	private void closeScreen()
	{
		runOnUiThread(new Runnable() { public void run()
		{
			try {
				mWebView.destroy();
				mWebView = null;
				mView.removeAllViews();
			} catch(Exception e) { }
			System.gc();
			finish();
		}});
	}
	
	class glWebViewClient extends WebViewClient
	{
		boolean hasError = false;
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			DBG("ADSERVER", "*************** shouldOverrideUrlLoading()********************");
			DBG("ADSERVER", "*************** url: " + url);
			
			if (url.startsWith("exit:"))
			{
				closeScreen();
				return true;
			}
			else if(url.startsWith("play:"))
			{
				startApp(url.split("[?]")[0].replace("play:", ""));
				closeScreen();
				return true;
			}
			else if(url.startsWith("goto:"))
			{
				AdServer.handleGotoString(url.replace("goto:", ""));
				closeScreen();
				return true;
			}		
			else if(url.startsWith("link:"))
			{
				startBrowser(url.replace("link:", ""));
				closeScreen();
				return true;
			}		
			else if(url.startsWith("market://"))
			{
				startMarketApplication(url);		
				closeScreen();
				return true;			
			}
			else if(url.startsWith("amzn://"))
			{
				startBrowser(url);
				closeScreen();
				return true;
			}
			else if(url.startsWith("vnd.youtube:"))
			{				
				startYoutube(url);
				closeScreen();
				return true;
			}
			else if(url.startsWith("skt:"))
			{
				final String f_url = url;
				new Thread(new Runnable() { public void run()
				{					
					Intent intent = getSktIntent(AdServerInterstitial.this, f_url.replace("skt:", ""));
					if(intent != null) {
						startActivity(intent);
					} else {
						showToastNotification("T store application not installed...");
					}
					closeScreen();
				}}).start();
				return true;
			}
			else if(url.startsWith("http://ingameads.gameloft.com/redir/") && url.contains("from=") && !url.contains("t=bundle") && !url.contains("op=SKTS"))
			{
				startBrowser(url);
				closeScreen();				
				return true;
			}
					
			view.loadUrl(url);
			return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			DBG("ADSERVER", "*************** onPageStarted()********************");
			DBG("ADSERVER", "*************** url: " + url);
		}
		
		@Override
		public void onPageFinished(WebView view, String url)
		{
			DBG("ADSERVER", "*************** onPageFinished()********************");
			DBG("ADSERVER", "*************** url: " + url);
			
			if (hasError) {
				closeScreen();
			} else {
				try {
					view.setVisibility(View.VISIBLE);
				} catch (Exception e) {}
			}
			
			hasError = false;			
		}
		
		@Override
		public void onReceivedError (WebView view, int errorCode, String description, String failingUrl)
		{
			hasError = true;
			
			DBG("ADSERVER", "***************onReceivedError: " + description);
		}
		
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
		{
			handler.proceed();
		}
	}
	
	class glWebChromeClient extends WebChromeClient
	{
		public boolean onConsoleMessage(android.webkit.ConsoleMessage cm) {
			DBG("Ads Console", cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
			return true;
		}
	}
}