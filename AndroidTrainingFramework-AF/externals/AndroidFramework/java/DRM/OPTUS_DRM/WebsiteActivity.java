#if USE_OPTUS_DRM
package com.msap.store.drm.android;

import android.app.*;
import android.content.*;
import android.os.*;
import android.webkit.*;
import android.util.Log;

import com.msap.store.drm.android.util.*;

/**
 * This class implements a web browser activity that opens the portal of 
 * the license server for license requests.
 * @author Edison Chan
 */
@SuppressWarnings("unused")
public final class WebsiteActivity extends Activity {
	public static final String ACTIVITY_RESULT_STATUS = "com.msap.store.drm.android.WebsiteActivity.status";
	public static final String ACTIVITY_RESULT_DATA = "com.msap.store.drm.android.WebsiteActivity.data";
	
	private WebView webview = null;
	private WebSettings settings = null;
	private CheckerWebsiteProxy client = null;
	private String url = null;
	private String request = null;
	private String response = null;
	
	/**
	 * This method initialize a webview to open the license server website, and let
	 * the user work on licensing in a web environment.
	 * @param savedInstanceState data used for resurrecting destroyed activity.
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			Intent request = this.getIntent();
			String url = request.getStringExtra("url");
			String data = request.getStringExtra("data");
		
			if (url != null && data != null) {
				this.url = url;
				this.webview = new WebView(this);
				this.settings = this.webview.getSettings();
				this.client = new CheckerWebsiteProxy();
			
				this.request = data; //HexEncoding.encode(data.getBytes("UTF-8"));
				this.response = null;

				this.setContentView(this.webview);
				this.settings.setJavaScriptEnabled(true);
				this.settings.setAllowFileAccess(false);
				this.settings.setSupportZoom(false);
				this.settings.setPluginsEnabled(false);
				this.settings.setUserAgent(0);
				this.webview.addJavascriptInterface(this.client, "LicenseChecker");

				this.webview.loadUrl(url);
			} else {
				this.finish();
			}
		} catch (Exception ex) {
			this.finish();
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	}
	
	/**
	 * Finish the activity. Before finishing it we will set up the return
	 * value as well.
	 */
	public void finish() {
		Intent result = new Intent();
		
		if (this.response != null) {
			result.putExtras(this.getIntent());
			result.putExtra(ACTIVITY_RESULT_STATUS, true);
			result.putExtra(ACTIVITY_RESULT_DATA, this.response);
		} else {
			result.putExtras(this.getIntent());
			result.putExtra(ACTIVITY_RESULT_STATUS, false);
		}

	
		this.setResult(RESULT_OK, result);
		super.finish();
	}
	
	/**
	 * This class allows the website to control the webview via Javascript.
	 * @author Edison Chan
	 */
	private class CheckerWebsiteProxy {
		/**
	   * Enable javascript to get the request from the DRM library.
	   * @return hex encoded request data
	   */
		public final String getRequestData() {
			try {
				byte[] tmp1 = WebsiteActivity.this.request.getBytes("UTF-8");
				String tmp2 = HexEncoding.encode(tmp1);

				DBG("WebsiteActivity", "Exporting reqest data to JS");
				DBG("WebsiteActivity", WebsiteActivity.this.request);
				DBG("WebsiteActivity", tmp2);

				return tmp2;
			} catch (Exception ex) {
				return "";
			}
		}

		/**
		 * Enable javascript to set the result of the website activity.
		 * @param data hex encoded response data.
		 */
		public final void setResponse(String data) {
			try {
				byte[] tmp1 = HexEncoding.decode(data);
				String tmp2 = new String(tmp1, "UTF-8");

				DBG("WebsiteActivity", "Setting response data from JS");
				DBG("WebsiteActivity", data);
				DBG("WebsiteActivity", tmp2);

				WebsiteActivity.this.response = tmp2;
			} catch (Exception ex) {
				WebsiteActivity.this.response = null;
			}
		}

		/**
		 * Enable javascript to close the browser and end this activity.
		 */
		public final void end() {
			final WebsiteActivity activity = WebsiteActivity.this;
	
			DBG("WebsiteActivity", "Finishing the activity from JS...");
	
			activity.runOnUiThread(new Runnable() {
				public void run() {
					activity.finish();
				}
			});
		}
	}
}

#endif	//USE_OPTUS_DRM
