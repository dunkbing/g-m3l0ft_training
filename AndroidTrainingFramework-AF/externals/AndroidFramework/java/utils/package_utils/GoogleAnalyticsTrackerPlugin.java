package APP_PACKAGE.PackageUtils;

#if USE_GOOGLE_ANALYTICS_TRACKING

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import APP_PACKAGE.utils.GoogleAnalyticsTracker;
import APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver;

public class GoogleAnalyticsTrackerPlugin implements IPluginEventReceiver
{
	private Activity	m_MainActivityRef = null;
	public void onPluginStart(Activity mainActivity, ViewGroup vg)
	{
		m_MainActivityRef = mainActivity;
		GoogleAnalyticsTracker.Init(m_MainActivityRef);
	}
	
	public void onPreNativePause() { }
	public void onPostNativePause() { 
		#if GA_AUTO_ACTIVITY_TRACKING
	        APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(m_MainActivityRef);
        #endif //GA_AUTO_ACTIVITY_TRACKING
		
	}
	
	public void onPreNativeResume() { }
	public void onPostNativeResume() { 
	    #if GA_AUTO_ACTIVITY_TRACKING
            APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(m_MainActivityRef);
        #endif //GA_AUTO_ACTIVITY_TRACKING
	}
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) { return false; }
}

#endif