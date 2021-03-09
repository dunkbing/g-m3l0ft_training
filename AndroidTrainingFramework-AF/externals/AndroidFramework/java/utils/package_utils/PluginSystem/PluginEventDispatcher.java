package APP_PACKAGE.PackageUtils.PluginSystem;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.app.Activity;
import android.view.ViewGroup;
import java.util.ArrayList;


public class PluginEventDispatcher 
{
	private ArrayList<IPluginEventReceiver>	    m_arrPluginSignalReceiver = null;

    private ArrayList<String>                   m_arrStrLoadedPlugins	  = null;

    private void PrintLoadedPlugins()
    {
        DBG("ACP_LOGGER", "Plugins loaded:");
        for(int i = 0; i < m_arrStrLoadedPlugins.size(); i++)
        {
            DBG("ACP_LOGGER", m_arrStrLoadedPlugins.get(i));
        }
    }

	public PluginEventDispatcher() 
	{
		if(m_arrPluginSignalReceiver == null /*&& m_arrPluginInputDispacher == null*/)
        {
			m_arrPluginSignalReceiver = new ArrayList<IPluginEventReceiver>();
            m_arrStrLoadedPlugins = new ArrayList<String>();
        }
		else
		{
			Log.e("ACP_LOGGER", "Error: Trying to load multiple dispatchers");
		}
	}

    private void LoadPlugins(Activity mainActivityRef, ViewGroup viewGroup, String[] list)
    {
        for(int i = 0; i < list.length; i++)
        {
            String className = list[i];
            
            Class<?> c = null;
            Object t = null;

        	try
        	{
                c = Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                Log.e("ACP_LOGGER", "Error: Cannot find class " + className);
                e.printStackTrace();
				Log.e("ACP_LOGGER", "We will try to load it now " + className);
				c = null;
            }
			
			if(c == null)
			{
				try
				{
					c = ClassLoader.getSystemClassLoader().loadClass(className);
				}
				catch (ClassNotFoundException e)
				{
					Log.e("ACP_LOGGER", "Error: Cannot load class " + className);
					e.printStackTrace();
					Log.e("ACP_LOGGER", "Continue to next Plugin");
					continue;
				}
			}

            try
            {
	    		t = c.newInstance();
            }
            catch (Exception e) 
            {
                Log.e("ACP_LOGGER", "Error: Cannot instantiate class " + className + " . Check stack trace bellow.");
                e.printStackTrace();
				continue;
            }

	    	if(t instanceof IPluginEventReceiver)
	    	{
                IPluginEventReceiver recv = (IPluginEventReceiver)t;
                RegisterSignalReceiver(recv);
                recv.onPluginStart(mainActivityRef, viewGroup);
				DBG("ACP_LOGGER", "Plugin " + className + " added");
				continue;
			}
            else
            {
                Log.e("ACP_LOGGER", "Error: Class " + className + " is not of type IPluginEventReceiver. Plugin Load failed.");
				continue;
            }
    	}
    }
	
	public void LoadPlugins(Activity mainActivityRef, ViewGroup viewGroup)
	{       
        LoadPlugins(mainActivityRef, viewGroup, PluginListInternal.list);
        LoadPlugins(mainActivityRef, viewGroup, PluginListExternal.list);

        PrintLoadedPlugins();
	}

    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        for(int i = 0; i < m_arrPluginSignalReceiver.size(); i++)
		{
			if(m_arrPluginSignalReceiver.get(i).onActivityResult(requestCode, resultCode, data))
            {
                return true;
            }
		}

        return false;
    }

    public void onPreNativePause()
	{
		for(int i = 0; i < m_arrPluginSignalReceiver.size(); i++)
		{
			m_arrPluginSignalReceiver.get(i).onPreNativePause();
		}
	}

    public void onPostNativePause()
	{
		for(int i = 0; i < m_arrPluginSignalReceiver.size(); i++)
		{
			m_arrPluginSignalReceiver.get(i).onPostNativePause();
		}
	}
	 
	public void onPreNativeResume()
    {
        for(int i = 0; i < m_arrPluginSignalReceiver.size(); i++)
		{
			m_arrPluginSignalReceiver.get(i).onPreNativeResume();
		}
    }

    public void onPostNativeResume()
    {
        for(int i = 0; i < m_arrPluginSignalReceiver.size(); i++)
		{
			m_arrPluginSignalReceiver.get(i).onPostNativeResume();
		}
    }

	public void RegisterSignalReceiver(IPluginEventReceiver cls)
	{
        String pluginName = cls.getClass().getName();

        if(m_arrStrLoadedPlugins.contains(pluginName) == false)
        {
		    m_arrPluginSignalReceiver.add(cls);
            m_arrStrLoadedPlugins.add(pluginName);
        }
        else
        {
            Log.e("ACP_LOGGER", "Class already registered as a receiver: " + pluginName);
            Log.e("ACP_LOGGER", "Check your Plugin List config.");
        }
	}
}


