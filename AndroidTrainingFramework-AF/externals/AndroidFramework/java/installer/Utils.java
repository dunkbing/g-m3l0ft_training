
package APP_PACKAGE.installer;
//package com.gameloft.android.GloftAsphalt5.asphalt5.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.util.Log;
import android.os.Build;
import android.content.SharedPreferences;

class TimePass {
	
	public Vector<Long> time;
	private boolean ended;
	public static HashMap<String, String> m_hmGetFile;
	public static boolean needsToResetDownloadedFile = false;
	
	/**
	 * Timer's constructor based on an initial time
	 * @param tm the initial time of the constructor
	 */
	public TimePass(Long tm) {
		time = new Vector<Long>();
		time.add(tm);
		ended = false;
	}
	
	/**
	 * Adds a start trigger to the timer
	 * @param tm the trigger's time
	 */
	public void add(Long tm) {
		if (!ended)
		{
			long startTime = time.lastElement();
			time.remove(time.size() - 1);
			time.add(tm - startTime);
			ended = true;
		}
		else
		{
			time.add(tm);
			ended = false;
		}
	}
	
	/**
	 * Clears a timer's content
	 */
	public void clear() {
		time.clear();
	}
	
	/**
	 * Get's the timer's average time and timing for each different timer.
	 * @param showTimers if true, it will show the timmer's triggers, not only the average and total time
	 * @return the timer's average time and timing for each different timer.
	 */
	public String timersToString(boolean showTimers) {
		String str = "";
		Long totalTime = 0L;
		
		for (int i = 0; i < time.size(); i++)
		{
			totalTime += time.elementAt(i);
			if (showTimers)
				str += "\t" + time.elementAt(i) + "\n";
		}
		
		return (time.size() > 1 ? "(" + time.size() + ") <Average: " + totalTime / time.size() + "> " : "") + "<Total Time: " + totalTime + ">\n" + str;
	}
	
	/**
	 * Get's the timer's average time and timing for each different timer.
	 * @param key the name of the timer
	 * @param start the start potition from which the triggers will be outputed
	 * @return the timer's average time and timing for each different timer.
	 */
	public String timersToString(int start, int count) {
		String str = "";
		Long totalTime = 0L;
		
		for (int i = 0; i < time.size(); i++)
		{
			totalTime += time.elementAt(i);
			if (i >= start && i < start + count)
				str += "\t" + time.elementAt(i) + "\n";
		}
		
		return (time.size() > 1 ? "(" + time.size() + ") <Average: " + totalTime / time.size() + "> " : "") + "<Total Time: " + totalTime + "> " + "[" + start + ":" + (start + count) + "]" + "\n" + str;
	}
}

public class Utils {


	static HashMap<String, TimePass> mTimers = new HashMap<String, TimePass>();
	
	/**
	 * Adds a trigger start to the timer
	 * @param key the name of the timer
	 * @param tag the trigger's tag
	 */
	public static void triggerStartTimer(String key)
	{
		if(mTimers.containsKey(key))
		{
			TimePass timePass = mTimers.get(key);
			timePass.add(System.currentTimeMillis());
		}
		else
			mTimers.put(key, new TimePass(System.currentTimeMillis()));
	}
	
	/**
	 * Adds a trigger end to the timer
	 * @param key the name of the timer
	 * @param tag the trigger's tag
	 */
	public static void triggerEndTimer(String key)
	{
		if(mTimers.containsKey(key))
		{
			TimePass timePass = mTimers.get(key);
			timePass.add(System.currentTimeMillis());
		}
	}
	
	/**
	 * Outputs a timer's data
	 * @param key the name of the timer
	 * @param showAllTriggers true if the output will contain all the timer's triggers
	 */
	public static void showTimer(String key, boolean showAllTriggers)
	{
		if(mTimers.containsKey(key)) {
			TimePass timePass = mTimers.get(key);
			DBG("GI_Timer", key + " " + timePass.timersToString(showAllTriggers));
		}
	}
	
	/**
	 * Outputs a timer's data
	 * @param key the name of the timer
	 * @param start the start potition from which the triggers will be outputed
	 * @param count the number of triggers outputed  
	 */
	public static void showTimer(String key, int start, int count)
	{
		if(mTimers.containsKey(key)) {
			TimePass timePass = mTimers.get(key);
			DBG("GI_Timer", key + " " + timePass.timersToString(start, count));
		}
	}
	
	/**
	 * Outputs all the timers
	 * @param showAllTriggers true if the output will contain all the timer's triggers
	 */
	public static void showAllTimers(boolean showAllTriggers)
	{
		Iterator<Entry<String, TimePass>> it = mTimers.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, TimePass> timer = (Map.Entry<String, TimePass>)it.next();
			TimePass timePass = timer.getValue();
			
			DBG("GI_Timer", timer.getKey() + " " + timePass.timersToString(showAllTriggers));
		}
	}

	public static void showDialog(Context context, String text, int time){
		Toast.makeText(context , text, time).show();
	}
	public static void showDialog(Context context, String text){
		Toast.makeText( context, text, Toast.LENGTH_SHORT).show();
	}




	public static void windowFullScreen(Activity activity)
	{
		if(null != activity)
		{
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
	}
	
    /**
     * Functions to get/set preferences
     */
	 
    /**
     * Set a Shared Preference from the application
	 * @param preferencesName The name of the preferences file.
     * @param key the key in a String representation
     * @param value the value to store in an Object instance
     */
    public static void setPreference(Context currentContext, String preferencesName, String key, Object value)
    {
    	SharedPreferences settings = currentContext.getSharedPreferences(preferencesName, 0);
        SharedPreferences.Editor editor = settings.edit();

        if (value instanceof String)
        {
			//GLDebug.INFO("INFO", "setPreference String: " + value);
            editor.putString(key, (String) value);
        }
        else if (value instanceof Integer)
        {
			//GLDebug.INFO("INFO", "setPreference Integer: " + value);
            editor.putInt(key, (Integer) value);
        }
        else if (value instanceof Boolean)
        {
			//GLDebug.INFO("INFO", "setPreference Boolean: " + value);
            editor.putBoolean(key, (Boolean) value);
        }
        else if(value instanceof Long)
        {
			//GLDebug.INFO("INFO", "setPreference Long: " + value);
        	editor.putLong(key, (Long)value);
        }
        editor.commit();
    }

    /**
     * To read a string from the preferences file.
	 * @param preferencesName The name of the preferences file.
     * @param key The key that identifies the preferences file.
     * */
    public static String getPreferenceString(Context currentContext, String preferencesName, String key)
    {
        SharedPreferences settings = currentContext.getSharedPreferences(preferencesName, 0);
        String res = settings.getString(key, "");
        return res;

    }

    /**
     * To read an int from the preferences file.
	 * @param preferencesName The name of the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * */
    public static int getPreferenceInt(Context currentContext, String preferencesName, String key, int defaultValue)
    {
        SharedPreferences settings = currentContext.getSharedPreferences(preferencesName, 0);
        int res = settings.getInt(key, defaultValue);
        return res;
    }

    /**
     * To read a long from the preferences file.
	 * @param preferencesName The name of the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * */
    public static boolean getPreferenceBoolean(Context currentContext, String preferencesName, String key, boolean defaultValue)
    {
        SharedPreferences settings = currentContext.getSharedPreferences(preferencesName, 0);
        boolean res = settings.getBoolean(key, defaultValue);
        return res;
    }
    
    /**
     * To read a long from the preferences file.
	 * @param preferencesName The name of the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * */
    public static long getPreferenceLong(Context currentContext, String preferencesName, String key, long defaultValue)
    {
        SharedPreferences settings = currentContext.getSharedPreferences(preferencesName, 0);
        Long res = settings.getLong(key, defaultValue);
        return res; 
    }
	
	  /**
     * To read a long from the preferences file.
	 * @param preferencesName The name of the preferences file.
     * @param key The key that identifies the preferences file.
     * @param defaultValue The default value to be returned if the preference does not exist.
     * */
    public static boolean getPreferenceExists(Context currentContext, String preferencesName, String key)
    {
        SharedPreferences settings = currentContext.getSharedPreferences(preferencesName, 0);
        return settings.contains(key);
    }
	
    /**
     * Clear a Shared Preference from the application
	 * @param preferencesName The name of the preferences file.
     */
    public static void clearPreference(Context currentContext, String preferencesName)
    {
    	SharedPreferences settings = currentContext.getSharedPreferences(preferencesName, 0);
        SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		editor.commit();
	}
		public synchronized static void markAsSaved(APP_PACKAGE.installer.utils.PackFile file)
	{
		try
		{
			if (!hasBeenDownloaded(file, false))
			{
				java.io.FileWriter fstream = new java.io.FileWriter(GameInstaller.sd_folder +  "/" + "d_o_w_n_l_o_a_d_e_d.txt",true);
				java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
				String file_to_write = file.getFolder() + file.getName();
				out.write(file_to_write + "\n");
				out.close();
				TimePass.m_hmGetFile.put(file_to_write, file_to_write);
			}
		}
		catch (Exception e) {DBG_EXCEPTION(e);}
	}
	public synchronized static boolean hasBeenDownloaded(APP_PACKAGE.installer.utils.PackFile file, boolean validResource)
	{
		
		try
		{
			boolean result = TimePass.m_hmGetFile.containsKey(file.getFolder() + file.getName());
			return result;
		}
		catch (Exception e) {}
		return false;
	}
	public synchronized static void markAsNotDownloaded(APP_PACKAGE.installer.utils.PackFile file)
	{
		try
		{
			TimePass.m_hmGetFile.remove(file.getFolder() + file.getName());
		}
		catch (Exception e) {DBG_EXCEPTION(e);}
		TimePass.needsToResetDownloadedFile = true;
	}
	public synchronized static void requestResetDownloadedFile()
	{
		if(!TimePass.needsToResetDownloadedFile)
		{
			return;
		}
		try
		{
			java.io.FileWriter fstream = new java.io.FileWriter(GameInstaller.sd_folder +  "/" + "d_o_w_n_l_o_a_d_e_d.txt",false);//false will create the file again
			java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
				
			for (Map.Entry<String,String> entry : TimePass.m_hmGetFile.entrySet()) {
				out.write(entry.getKey() + "\n");
			}
			out.close();
		}
		catch (Exception e) {DBG_EXCEPTION(e);}
		TimePass.needsToResetDownloadedFile = false;
	}
	
	public static String getSplitName(String file)
	{
		if (!file.contains(".split_"))
			return "";
		try
		{
			return file.substring(0, file.lastIndexOf('.'));
		}
		catch (Exception e)
		{
			return "";
		}
	}
	public static int getSplitNumber(String file)
	{
		if (!file.contains(".split_"))
			return -1;
		try
		{
			return Integer.parseInt(file.substring(file.lastIndexOf('_') + 1));
		}
		catch (Exception e)
		{
			return -1;
		}
	}
}
