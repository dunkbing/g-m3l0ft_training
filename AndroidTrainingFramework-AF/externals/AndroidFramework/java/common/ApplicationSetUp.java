#if USE_GOOGLE_ANALYTICS_TRACKING
package APP_PACKAGE;

import APP_PACKAGE.GLUtils.SUtils;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.os.Bundle;

public class ApplicationSetUp extends BroadcastReceiver 
{
	public static String APP_TAG = "ApplicationSetUp";
	public static String GA_DEBUG = "GA_DEBUG";
	public static String TRUE = "true";
	public static String FALSE = "false";
	
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		DBG(APP_TAG, "Receiving a command");
		Bundle extras = intent.getExtras();
		
		if (extras != null)
		{
			DBG(APP_TAG, "  IT HAS EXTRAS");
			String state = extras.getString("GA_DEBUG");
			if (state != null)
			{
				DBG(APP_TAG, "Receiving GA_DEBUG: " + state);
				SharedPreferences settings = context.getSharedPreferences(APP_TAG, 0);
				SharedPreferences.Editor editor = settings.edit();
				
				editor.putString("GA_DEBUG", state);
				editor.commit();
			}
		}
	}
}
#endif