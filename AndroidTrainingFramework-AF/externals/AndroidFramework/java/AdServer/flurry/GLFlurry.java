#if ADS_USE_FLURRY

package APP_PACKAGE.Flurry;

import com.flurry.android.FlurryAgent;
import com.flurry.android.Constants;

import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;

public class GLFlurry
{
	public static byte MALE = Constants.MALE;
	public static byte FEMALE = Constants.FEMALE;
	
	public static void onStartSession(Activity context)
	{
		FlurryAgent.setLogEnabled(RELEASE_VERSION==0);
		FlurryAgent.setReportLocation(ADS_FLURRY_USE_LOCATION==1);
		//FlurryAgent.setContinueSessionMillis(1500);
		FlurryAgent.onStartSession(context, ADS_FLURRY_ID);
	}
	
	public static void onEndSession(Activity context)
	{
		FlurryAgent.onEndSession(context);
	}
	
	public static void setUseHttps(boolean useHttps)
	{
		FlurryAgent.setUseHttps(useHttps);
	}
	
	// actually function FlurryAgent.setUserID(userid) is not found.
	// public static void setUserID(String userid)
	// {
		// FlurryAgent.setUserID(userid);
	// }
	
	public static void setAge(int age)
	{
		FlurryAgent.setAge(age);
	}
	
	public static void setGender(byte gender)
	{
		FlurryAgent.setGender(gender);
	}
	
	public static void logEvent(String eventId)
	{
		FlurryAgent.logEvent(eventId);
	}
	
	public static void logEvent(String eventId, boolean timed)
	{
		FlurryAgent.logEvent(eventId, timed);
	}
	
	public static void logEvent(String eventId, Bundle parameters)
	{
		Set<String> keys 			= parameters.keySet();  
		Iterator<String> iterate 	= keys.iterator();
		HashMap<String, String> map = new HashMap<String, String>();
		
		while (iterate.hasNext()) {
			String key = iterate.next();
			map.put(key, (String)parameters.get(key));
		}
			
		FlurryAgent.logEvent(eventId, map);
	}
	
	public static void logEvent(String eventId, Bundle parameters,  boolean timed)
	{
		Set<String> keys 			= parameters.keySet();  
		Iterator<String> iterate 	= keys.iterator();
		HashMap<String, String> map = new HashMap<String, String>();
		
		while (iterate.hasNext()) {
			String key = iterate.next();
			map.put(key, (String)parameters.get(key));
		}
		
		FlurryAgent.logEvent(eventId, map, timed);
	}
}
#endif
		