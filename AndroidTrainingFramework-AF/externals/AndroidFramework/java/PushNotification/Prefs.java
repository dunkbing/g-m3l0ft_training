#if SIMPLIFIED_PN
/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package APP_PACKAGE.PushNotification;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
	
	static final String ENABLE_NOTIFICATION 		= "enablePushNotification";
	static final String ENABLE_NOTIFICATION_LOCAL 	= "pn_local_enable";
	static final String ENABLE_NOTIFICATION_REMOTE 	= "pn_remote_enable";
	
	static final String ENABLE_DONT_DISTURB 		= "pn_dont_disturbe_enable";
	static final String ENABLE_DONT_DISTURB_START	= "pn_dont_disturbe_start";
	static final String ENABLE_DONT_DISTURB_END		= "pn_dont_disturbe_end";
	
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("GLPN", 0);
    }
	
	public static void init(Context context)
	{
		SharedPreferences prefs = get(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		if(!prefs.contains(ENABLE_NOTIFICATION))
		{
			editor.putBoolean(ENABLE_NOTIFICATION,true);
		}
		
		if(!prefs.contains(ENABLE_NOTIFICATION_LOCAL))
		{
			editor.putBoolean(ENABLE_NOTIFICATION_LOCAL,true);
		}
		
		if(!prefs.contains(ENABLE_NOTIFICATION_REMOTE))
		{
			editor.putBoolean(ENABLE_NOTIFICATION_REMOTE,true);
		}
		
		if(!prefs.contains(ENABLE_DONT_DISTURB))
		{
			editor.putBoolean(ENABLE_DONT_DISTURB, DontDisturbPolicy.DEFAULT_ENABLE);
			editor.putInt(ENABLE_DONT_DISTURB_START, DontDisturbPolicy.START_TIME);
			editor.putInt(ENABLE_DONT_DISTURB_END, DontDisturbPolicy.END_TIME);
		}
		
		editor.commit();
	}
	
	public static boolean isEnabled(Context context)
	{
		SharedPreferences settings = get(context);
		return settings.getBoolean(ENABLE_NOTIFICATION, true);
	}
	
	public static boolean isEnableLocal(Context context)
	{
		SharedPreferences settings = get(context);
		return settings.getBoolean(ENABLE_NOTIFICATION_LOCAL, true);
	}
	
	public static boolean isEnableRemote(Context context)
	{
		SharedPreferences settings = get(context);
		return settings.getBoolean(ENABLE_NOTIFICATION_REMOTE, true);
	}
	
	public static void setEnabled(Context context, boolean enabled)
	{
		SharedPreferences prefs = get(context);
        SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENABLE_NOTIFICATION, enabled);
		editor.commit();
	}
	
	public static void setEnabledLocal(Context context, boolean enabled)
	{
		SharedPreferences prefs = get(context);
        SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENABLE_NOTIFICATION_LOCAL, enabled);
		editor.commit();
	}
	
	public static void setEnabledRemote(Context context, boolean enabled)
	{
		SharedPreferences prefs = get(context);
        SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENABLE_NOTIFICATION_REMOTE, enabled);
		editor.commit();
	}
	
	public static void setDontDisturbEnable(Context context, boolean enabled)
	{
		SharedPreferences prefs = get(context);
        SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ENABLE_DONT_DISTURB, enabled);
		editor.commit();
	}
	
	public static boolean isDontDisturbEnable(Context context)
	{
		SharedPreferences settings = get(context);
		return settings.getBoolean(ENABLE_DONT_DISTURB, DontDisturbPolicy.DEFAULT_ENABLE);
	}
	
	public static int getDontDisturbStartTime(Context context)
	{
		SharedPreferences settings = get(context);
		return settings.getInt(ENABLE_DONT_DISTURB_START, DontDisturbPolicy.START_TIME);
	}
	
	public static int getDontDisturbEndTime(Context context)
	{
		SharedPreferences settings = get(context);
		return settings.getInt(ENABLE_DONT_DISTURB_END, DontDisturbPolicy.END_TIME);
	}
}
#endif
