package APP_PACKAGE.PushNotification;

import APP_PACKAGE.PushNotification.Prefs;
import android.content.Context;
import java.util.Calendar;

public class DontDisturbPolicy 
{
	SET_TAG("PushNotification");
	
	static final boolean DEFAULT_ENABLE = true;
	static final int START_TIME 		= 23 * 60; 	// 11 PM (23 HOURS). times in minutes
	static final int END_TIME 			= 8 * 60;  	// 8 AM (8 HOURS). times in minutes
	

	static void setDontDisturbEnable(Context context, boolean enabled)
	{
		Prefs.setDontDisturbEnable(context, enabled);
	}
	
	static boolean isDontDisturbEnable(Context context)
	{
		return Prefs.isDontDisturbEnable(context);
	}
	
	static boolean isDontDisturbeTime(Context context)
	{
		boolean dontDisturbEnabled = Prefs.isDontDisturbEnable(context);
		
		if(dontDisturbEnabled)
		{
			Calendar c 	= Calendar.getInstance(); 
			int hours 	= c.get(Calendar.HOUR_OF_DAY);
			int minutes = c.get(Calendar.MINUTE);

			int minutes_of_day = (hours*60) + (minutes);
	
			if(minutes_of_day <= END_TIME || minutes_of_day >= START_TIME)
			{
				DBG(TAG, "Don't disturb time");
				return true;
			}
		} 
		else 
		{
			DBG(TAG, "Don't disturb is disabled");
		}
		
		return false;
	}
}