package APP_PACKAGE.PackageUtils.Dispatchers;

import android.content.Intent;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;

import APP_PACKAGE.PackageUtils.PluginSystem.PluginEventDispatcher;
import APP_PACKAGE.PackageUtils.AndroidUtils;

public class SignalDispatcher
{
	private PluginEventDispatcher m_PluginEventDispatcher;
	

	public SignalDispatcher()
	{
		m_PluginEventDispatcher = new PluginEventDispatcher();
	}
	
	public void StartDispatcher(Activity mainActivity, ViewGroup viewGroup)
	{
        AndroidUtils.SetActivityRef(mainActivity);

		m_PluginEventDispatcher.LoadPlugins(mainActivity, viewGroup);
	}
	
	
    public void OnActivityResult(int requestCode, int resultCode, Intent data)
    {
        m_PluginEventDispatcher.onActivityResult(requestCode, resultCode, data);
    }
	
	public void OnPause()
    {  
        AndroidUtils.onPreNativePause();
        m_PluginEventDispatcher.onPreNativePause();
    
        AndroidUtils.onPause();

		m_PluginEventDispatcher.onPostNativePause();
        AndroidUtils.onPostNativePause();
    }

    public void OnResume()
    {
        AndroidUtils.onPreNativeResume();
		m_PluginEventDispatcher.onPreNativeResume();
    
        AndroidUtils.onResume();
        
		m_PluginEventDispatcher.onPostNativeResume();
        AndroidUtils.onPostNativeResume();
    }
}


