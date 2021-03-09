#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.util.*;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.util.*;

import APP_PACKAGE.R;

/**
 * This class shows a help message to users for Optus DRM libraries.
 * @author Edison Chan
 */
public class OptusGamesHelpActivity extends Activity {
	private TextView textview;
	private EventListener listener;
	private String csurl;
	
	/**
	 * Called to initialize the help activity.
	 * @param savedInstanceState saved state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.drmlib_optusjg_help);
		
		try {
			this.textview = (TextView) this.findViewById(R.id.drmlib_optusjg_layout_maintext);
			this.listener = new EventListener();

			String temp = this.getResources().getString(R.string.drmlib_optusjg_message_help);
			Spanned text = AltHtml.fromHtml(temp, this.listener);

			this.textview.setText(text);
			this.textview.setMovementMethod(LinkMovementMethod.getInstance());

			try {
				String csurlkey = "com.msap.store.drm.android.projects.optusjg.support_url";
				PackageManager packman = getPackageManager();
				ApplicationInfo appinfo = packman.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
				Bundle metadata = appinfo.metaData;
				//DBG("OptusGamesHelpActivity", "url="+url + ";csurl=" + csurl);
				if (metadata.containsKey(csurlkey)) {
					this.csurl = metadata.getString(csurlkey);
				}
			} catch (Exception ex) {
				this.csurl = null;
			}
			//VTN add button exit
			Button btnExit = (Button) findViewById(R.id.help_exit);
			btnExit.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            		DBG("OPTUS", "----------- Exit help ---------------");
						moveTaskToBack(true);
						System.exit(0);
	            		finish();
	            }
	        });
		} catch (Exception ex) {
			super.finish();
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
	 * This class listens and handles events occuring in the GLNHelperActivity
	 * class.
	 * @author Edison Chan
	 */
	class EventListener implements AltHtml.OnClickListener {
		/**
		 * Handles clicks on the links on the help screen.
		 * @param view View where the link is clicked.
		 * @param url URL of the link clicked.
		 */
		public void onLinkClick(View view, String url) {
			String csurl = OptusGamesHelpActivity.this.csurl;
			DBG("OptusGamesHelpActivity", "url="+url + ";csurl=" + csurl);
			if (url.equals("action:cs") && csurl != null) {
				DBG("action:cs", "url="+url);
				Uri uri = Uri.parse(csurl);
				Context context = OptusGamesHelpActivity.this;
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);

				intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
				DBG("action:cs", "url="+url);
				context.startActivity(intent);
			}
		}
	}
}

#endif	//USE_OPTUS_DRM
