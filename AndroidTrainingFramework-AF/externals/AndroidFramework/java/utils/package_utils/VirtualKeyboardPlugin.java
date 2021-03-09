package APP_PACKAGE.PackageUtils;

#if USE_VIRTUAL_KEYBOARD

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import APP_PACKAGE.GLUtils.VirtualKeyboard;
import APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver;

public class VirtualKeyboardPlugin implements IPluginEventReceiver
{
	public void onPluginStart(Activity mainActivity, ViewGroup vg)
	{
		// This is the way the virtual keyboard initializes		
		new VirtualKeyboard(mainActivity, vg);
	}
	
	public void onPreNativePause() 
	{ 
		if(VirtualKeyboard.isKeyboardVisible())
		{
			VirtualKeyboard.HideKeyboard();
		}
	}
	public void onPostNativePause() { }
	
	public void onPreNativeResume() { }
	public void onPostNativeResume() { }
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) { return false; }
}

#endif