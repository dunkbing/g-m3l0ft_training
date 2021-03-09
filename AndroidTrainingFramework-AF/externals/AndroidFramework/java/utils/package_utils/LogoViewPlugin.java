package APP_PACKAGE.PackageUtils;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.View;
import APP_PACKAGE.PackageUtils.LogoView;

public class LogoViewPlugin 
	implements APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver
{
	private LogoView	m_LogoView = null;
	private Activity	m_MainActivityRef = null;
	private ViewGroup	m_ViewGroupRef = null;
	private static LogoViewPlugin	s_GodIHateKeepingStaticReferencesToMyself = null;
	
	public void onPluginStart(Activity activity, ViewGroup viewGroup)
	{
		m_MainActivityRef = activity;
		m_ViewGroupRef = viewGroup;	
		s_GodIHateKeepingStaticReferencesToMyself = this;
	}
	
	public void onPreNativePause() { }
	public void onPostNativePause() { }
	
	public void onPreNativeResume() { }
	public void onPostNativeResume() { }
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	   return false; 
	}
	
	private void iShowLogo(int resId, int width, int height)
	{
		if(m_LogoView != null)
		{
			android.util.Log.e("ACP_LOGGER", "Error showing logo. Are you sure you haven't forgot to Hide the previous Logo?");
			return;
		}
		
		m_LogoView = new LogoView(m_MainActivityRef, resId, width, height); 
		
		m_MainActivityRef.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{	
				m_ViewGroupRef.addView(m_LogoView);
				m_LogoView.requestFocus();
			}
		});
	}
	
	private void iCloseLogo()
	{
		if(m_LogoView == null)
		{
			android.util.Log.i("ACP_LOGGER", "Logo already closed");
			return;
		}
		
		m_MainActivityRef.runOnUiThread(new Runnable () 
    	{
	    	public void run () 
	    	{
				m_LogoView.setVisibility(View.GONE);
				m_ViewGroupRef.removeViewInLayout(m_LogoView);
				m_LogoView.setImageResource(0);
				m_LogoView = null;
			}
		});
	}
	
	public static void ShowLogo(int resId, int width, int height)
	{
		if(resId <= 0)
		{
			resId = APP_PACKAGE.R.drawable.gameloft_logo;
		}
		
		s_GodIHateKeepingStaticReferencesToMyself.iShowLogo(resId, width, height);
	}
	
	public static void CloseLogo()
	{
		s_GodIHateKeepingStaticReferencesToMyself.iCloseLogo();
	}
}
