package APP_PACKAGE.GLUtils;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.View;
import APP_PACKAGE.GLUtils.Tracking;

public class TrackingPlugin 
	implements APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver
{
#if USE_INSTALLER
	public boolean m_bInitLaunchGame = false;
#endif
	
	public void onPluginStart(Activity activity, ViewGroup viewGroup)
	{
		#if !USE_INSTALLER
			Tracking.init();
		#else
			m_bInitLaunchGame = true;
		#endif
	}
	
	public void onPreNativePause() { }
	public void onPostNativePause() { }
	
	public void onPreNativeResume() { }

	public void onPostNativeResume() 
	{
	#if USE_INSTALLER
		if(!m_bInitLaunchGame)
		{
	#endif
			Tracking.setFlag(Tracking.TRACKING_FLAG_GAME_ON_STOP | Tracking.TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST);			
			Tracking.onLaunchGame(Tracking.TYPE_LAUNCH_GAME);
	#if USE_INSTALLER
		}
		else 
		{
			m_bInitLaunchGame = false;
		}
	#endif
	}
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	   return false; 
	}
}
