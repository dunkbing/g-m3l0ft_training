package APP_PACKAGE.utils;

import APP_PACKAGE.GLUtils.SUtils;

import APP_PACKAGE.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.HitBuilders;

import android.content.Context;
import android.app.Activity;
import java.util.HashMap;

public class GoogleAnalyticsTracker
{	
	private	static 	HashMap<String, Long>	startingTimes			= new HashMap<String, Long>();
	private	static	String					TAG_NAME				= "GoogleAnalyticsTracking";
	
	private static Tracker mTracker = null;  
	public static Context appContext = null;

	public static void Init(Context context)
	{
		DBG(TAG_NAME, "init GOOGLE ANALYTICS");
		appContext = context;
		getTracker();
	}
	
    synchronized public static Tracker getTracker() {  
		if (mTracker == null) {  
			DBG(TAG_NAME, "generating mTracker");
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(appContext);  
			mTracker = analytics.newTracker(R.xml.analytics);  
		}  
		DBG(TAG_NAME, "return mTracker");
		return mTracker;  
    }  
	
	public static void activityStart(Activity activity)
	{
		try
		{
			GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
		}
		catch (Exception e)
		{
			ERR(TAG_NAME, e.getMessage());
		}
	}
	
	public static void activityStop(Activity activity)
	{
		try
		{
			GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
		}
		catch (Exception e)
		{
			ERR(TAG_NAME, e.getMessage());
		}
	}
	
	private static String getKey(String Category, String Name, String Label)
	{
		try
		{
			StringBuilder key = new StringBuilder("");
			
			key.append(Category);
			key.append("_");
			if (Name != null)
				key.append(Name);
			else
				key.append("null");
			key.append("_");
			if (Label != null)
				key.append(Label);
			else
				key.append("null");
			
			return key.toString();
		}
		catch (Exception e)
		{
			ERR(TAG_NAME, e.getMessage());
		}
		return "";
	}
	
	public static void trackEvent(String category, String action, String label, Long value)
	{
		try
		{
			DBG(TAG_NAME, "trackEvent(" + category + ", " + action + ", " + label + ", " + (value!=null ? value : "null"));
			if(mTracker == null)
			{
				DBG(TAG_NAME, "mTracker is null");
				return;
			}
			if(value == null)
				mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
			else
				mTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).setValue(value).build());
		}
		catch (Exception e)
		{
			ERR(TAG_NAME, e.getMessage());
		}
	}
	
	public static void startTimingTracking(String Category, long time, String Name, String Label)
	{
		try
		{
			DBG(TAG_NAME, "startTimingTracking(" + Category + ", " + time + ", " + (Name!=null ? Name : "null") + ", " + (Label!=null ? Label : "null"));
			startingTimes.put(getKey(Category, Name, Label), time);
		}
		catch (Exception e)
		{
			ERR(TAG_NAME, e.getMessage());
		}
	}
	public static void stopTimingTracking(String Category, long time, String Name, String Label)
	{
		try
		{
			DBG(TAG_NAME, "stopTimingTracking(" + Category + ", " + time + ", " + (Name!=null ? Name : "null") + ", " + (Label!=null ? Label : "null"));
			String key = getKey(Category, Name, Label);
			long timing = new Long(0);
			
			if (startingTimes.containsKey(key))
			{
				timing = time - startingTimes.get(key);
			}
			
			timing += SUtils.getPreferenceLong(key, new Long(0), TAG_NAME);
			
			SUtils.setPreference(key, new Long(timing), TAG_NAME);
		}
		catch (Exception e)
		{
			ERR(TAG_NAME, e.getMessage());
		}
	}
	public static void sendTimingTracking(String Category, long time, String Name, String Label)
	{
		try
		{
			if(mTracker == null)
			{
				DBG(TAG_NAME, "mTracker is null");
				return;
			}
		
			stopTimingTracking(Category, time, Name, Label);
			String key = getKey(Category, Name, Label);
			long timing = SUtils.getPreferenceLong(key, new Long(0), TAG_NAME);
			DBG(TAG_NAME, "sendTimingTracking(" + Category + ", " + timing + ", " + (Name!=null ? Name : "null") + ", " + (Label!=null ? Label : "null"));
			if (timing != 0)
				mTracker.send(new HitBuilders.TimingBuilder().setCategory(Category).setValue(timing).setVariable(Name).setLabel(Label).build());
			SUtils.setPreference(key, new Long(0), TAG_NAME);
		}
		catch (Exception e)
		{
			ERR(TAG_NAME, e.getMessage());
		}
	}
	
	// removing trackTransaction, IAP doesn't use this function.
    // public static void trackTransaction(Transaction myTrans)
    // {
		// try
		// {
			// //not needed anymore
			// // googleAnalyticsTracker.trackTransaction(myTrans); // Track the transaction.
		// }
		// catch (Exception e)
		// {
			// ERR(TAG_NAME, e.getMessage());
		// }
    // }
}
