#if SIMPLIFIED_PN

package APP_PACKAGE.PushNotification;

import android.view.View;

//OS
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.view.ViewGroup;

public class PushNotificationPlugin 
	implements APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver
{
	public void onPluginStart(Activity mainActivity, ViewGroup viewGroup)
	{
		SimplifiedAndroidUtils.Init((Context)mainActivity);
	}
	
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		return false;
	}
	
	public void onPreNativePause()
	{
	}

	public void onPostNativePause()
	{
	}

	public void onPreNativeResume()
	{
		
	}
	
	public void onPostNativeResume()
	{
	}    
}

#endif //SIMPLIFIED_PN