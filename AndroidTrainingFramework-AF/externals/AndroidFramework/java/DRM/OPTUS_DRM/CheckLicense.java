////////////////////////////////////////////////////////////////////////////////////////////////////////////
//<--Australia DRM-->
#if USE_OPTUS_DRM
package com.msap.store.drm.android;

import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.view.KeyEvent;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.projects.optusjg.gameloft.*;

// #if USE_INSTALLER
// import APP_PACKAGE.installer.GameInstaller;
// #else	//USE_INSTALLER
import APP_PACKAGE.CLASS_NAME;
// #endif	USE_INSTALLER/

public class CheckLicense extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (!isTaskRoot()) // [A] Prevent App restart when open or 
		{
			DBG("OPTUS", "!isTaskRoot");
			finish();
			return;
		}
		DBG("OPTUS", "<<==PHONG BUI==>>	CheckLicense::onCreate");
        Intent intent = new Intent(this, SimpleCheckerActivity.class);
        startActivityForResult(intent, 1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {       
		if (resultCode == Activity.RESULT_OK && requestCode == 1) {
			boolean complete = data.getBooleanExtra(GLNCheckerActivity.ACTIVITY_RESULT_STATUS, false);
			
			if (complete) {
				DBG("OPTUS", "<<==PHONG BUI==>>	CheckLicense::onActivityResult true");
// #if USE_INSTALLER
				// Intent intent = new Intent(this, GameInstaller.class);
// #else
				Intent intent = new Intent(this, CLASS_NAME.class);
// #endif			
				startActivity(intent);	
			}  
			else 
			{
				DBG("OPTUS", "<<==PHONG BUI==>>	CheckLicense::onActivityResult false");
			}
			
			finish();
		}
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK) || (keyCode == KeyEvent.KEYCODE_SEARCH)) 
		{
	       return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}

#endif	//USE_OPTUS_DRM
