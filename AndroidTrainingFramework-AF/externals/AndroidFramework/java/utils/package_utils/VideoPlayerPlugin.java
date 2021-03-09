package APP_PACKAGE.PackageUtils;

#if USE_VIDEO_PLAYER

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;

import APP_PACKAGE.GLUtils.GLConstants;
import APP_PACKAGE.MyVideoView;
import APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver;

//av TODO -- View from Thor instead of MyVideoView
public class VideoPlayerPlugin implements IPluginEventReceiver
{
	// Static reference to activity
	private static Activity s_mainActivity = null;
	
	public void onPluginStart(Activity activity, ViewGroup surface)
	{
		s_mainActivity = activity;
	}
	
	public void onPreNativePause() { }
	public void onPostNativePause() { }
	
	public void onPreNativeResume() { }
	public void onPostNativeResume() { }
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	    if (requestCode == GLConstants.INTRO_VIDEO_ACTIVITY_NUMBER)
        {
			// Notify someone video is done ?
			return true;
        }
	
		return false; 
	}
	
	public static boolean LaunchVideo(String videoPath)
	{	
		try
		{
			MyVideoView.IsSoundOn = true;
			Intent videoIntent = new Intent(s_mainActivity, MyVideoView.class);		
			videoIntent.putExtra("video_name", videoPath);
	
			s_mainActivity.startActivityForResult(videoIntent, GLConstants.INTRO_VIDEO_ACTIVITY_NUMBER);
			    
			return true;
		}
        catch(Exception e)
        {
			Log.e("ACP_LOGGER", "Cannot launch video: " + videoPath);
			Log.e("ACP_LOGGER", "exception=" + e.toString());
			
			return false;
        }
	}

}

#endif // USE_VIDEO_PLAYER