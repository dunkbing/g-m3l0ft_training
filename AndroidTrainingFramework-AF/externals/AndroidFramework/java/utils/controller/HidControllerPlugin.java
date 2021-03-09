#if USE_HID_CONTROLLER
package APP_PACKAGE.GLUtils.controller;

import android.view.View;

//OS
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.view.ViewGroup;


public class HidControllerPlugin 
	implements APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver
{
	public void onPluginStart(Activity activity, ViewGroup vg)
	{
		
	}
	
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		return false;
	}
	
	public void onPreNativePause()
	{
		StandardHIDController.PauseControllerListener();
	}

	public void onPostNativePause()
	{
	}

	public void onPreNativeResume()
	{
		
	}
	
	public void onPostNativeResume()
	{
		StandardHIDController.ResumeControllerListener();
	}    
}

#endif //USE_HID_CONTROLLER