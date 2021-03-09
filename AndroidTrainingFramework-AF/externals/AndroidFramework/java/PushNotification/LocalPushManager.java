#if SIMPLIFIED_PN
package APP_PACKAGE.PushNotification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import java.lang.String;
import java.util.Calendar;
import java.util.Set;
import java.util.Iterator;

import APP_PACKAGE.GLUtils.SUtils;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalPushManager {

	SET_TAG("PushNotification");
	public static final int 		ALARM_DELAY_LIMIT 	= 24*24*60*60; 	//Alarm delay must be less than 24 days otherwise will be triggered immediately (OS limitation)
	public static final int 		ALARM_MAX_INDEX 	= 100; 			//Max index for alarms
	public static final String 		ALARM_ID_HEADER 	= "PN_LID_";
	public static final String 		ALARM_MAX_ID 		= "PN_AID";
	public static final String 		KEY_SPACER 			= "|key|";
	
	static void Init(Context context)
    {
		if(SUtils.getContext() == null)
			SUtils.setContext(context);
		Init();
	}
	
	static void Init()
    {
    	INFO(TAG, "[Init]");
		Context	s_Context = SUtils.getContext();
		if (s_Context != null)
		{
			DBG(TAG, "Context Ready");
			SharedPreferences prefs = Prefs.get(s_Context);
		}
    }
	
	static int getSlot()
	{
		Context	s_Context = SUtils.getContext();
		SharedPreferences prefs = Prefs.get(s_Context);
		int index = 0;
		while(prefs.contains(ALARM_ID_HEADER + index)){index++;}
		return index;
	}

	static String SetAlarm(Bundle dataBundle, int delay)
	{
		int id = getSlot();
		INFO(TAG, "[SetAlarm]");
		DBG(TAG, "[ID]: " + id + " [delay]: "+delay);
		
		Context	s_Context = SUtils.getContext();
		SharedPreferences prefs = Prefs.get(s_Context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(ALARM_MAX_ID, id);
		editor.commit();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, delay);
		
		String packageName = s_Context.getPackageName();
		dataBundle.putString(PN_UTILS_CLASS.KEY_LOCAL_ID, ALARM_ID_HEADER + id);
		Intent intent = new Intent(s_Context, LocalPushReceiver.class);
		intent.putExtra(packageName + ".alarm_content", dataBundle);
		PendingIntent sender = PendingIntent.getBroadcast(s_Context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		AlarmManager am = (AlarmManager) s_Context.getSystemService(s_Context.ALARM_SERVICE);
	#if SIMPLIFIED_PN
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ((long)(delay*1000)), sender);
	#else
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	#endif
		
		String sDelay= Integer.toString(delay);
		SaveAlarmInfo((ALARM_ID_HEADER + id), sDelay, dataBundle);
		return (ALARM_ID_HEADER + id);
	}
	
	public static void SaveAlarmInfo(String alarmID, String delay, Bundle dataBundle)
	{
		INFO(TAG, "[SaveAlarmInfo]");
		String extras = "";
		Set<String> keys = dataBundle.keySet();
		Iterator<String> iterate = keys.iterator();
		
		if (dataBundle != null) 
		{
			/////////////////////////// GET BUNDLE DATA /////////////////////////
			while (iterate.hasNext()) {
				String key = iterate.next();
				if(key != null && dataBundle.getString(key) != null && dataBundle.getString(key).length() > 0)
					extras += KEY_SPACER + key + "=" + dataBundle.getString(key);
			}
			extras += (KEY_SPACER + "delay=" + delay);
			////////////////////////////////////////////////////////////
		}
		else
			DBG(TAG, "No bundle info");
			
		WARN(TAG, "Saved: [" + extras + "]");
		Context	s_Context = SUtils.getContext();
		SharedPreferences prefs = Prefs.get(s_Context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(alarmID, extras);
        editor.commit();
	}
	
	public static void LoadAlarmInfo()
	{
		INFO(TAG, "[LoadAlarmInfo]");
		Context	s_Context = SUtils.getContext();
		SharedPreferences prefs = Prefs.get(s_Context);
		
		Bundle dataBundle;
		String sKey 	= "";
		String sValue 	= "";
		String sExpTime = "";
		String alarmid = "";
		
		int alarmCounter, start, end, delay;
		start = end = 0;
        alarmCounter = prefs.getInt(ALARM_MAX_ID, 0);

		DBG(TAG, "alarmCounter = [" + alarmCounter +"]");
		while (alarmCounter >= 0)
		{
			if(prefs.contains(ALARM_ID_HEADER + alarmCounter))
			{
				DBG(TAG, "AlarmID [" + ALARM_ID_HEADER + alarmCounter +"] found");
				String content = prefs.getString(ALARM_ID_HEADER + alarmCounter, null);
				dataBundle = new Bundle();
				delay = 0;
				
				while (end < content.length())
				{
					start = content.indexOf(KEY_SPACER);
					end = content.indexOf("=", start);
					sKey = content.substring(start + KEY_SPACER.length(), end);
					content = content.substring(end + 1);
					start = end + 1;
					 
					if(content.contains(KEY_SPACER))
						end = content.indexOf(KEY_SPACER);
					else
						end = content.length();
					 
					sValue = content.substring(0, end);
					
					if("delay".equals(sKey))
						delay = Integer.parseInt(sValue);
					else if("schedule_time".equals(sKey))
						sExpTime = sValue;
					else if(PN_UTILS_CLASS.KEY_LOCAL_ID.equals(sKey))
						alarmid = sValue;
						
					dataBundle.putString(sKey, sValue);
				}
				
				if(!isExpired(sExpTime))
				{
					DBG(TAG, "Setting Alarm: "+alarmid +", "+sExpTime+", "+delay);
					SetRestoredAlarm(alarmid, alarmCounter, dataBundle, delay);
				}
				else
					ERR(TAG, "Alarm Expired: "+alarmid +", "+sExpTime+", "+delay);
				
			}
			alarmCounter--;
		}
	}
	
	static void SetRestoredAlarm(String alarmID, int alarmCounter, Bundle dataBundle, int delay)
	{
		int id = getSlot();
		INFO(TAG, "[SetRestoredAlarm]");
		
		Context	s_Context = SUtils.getContext();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, delay);
		
		String packageName = s_Context.getPackageName();
		dataBundle.putString(PN_UTILS_CLASS.KEY_LOCAL_ID, alarmID);
		Intent intent = new Intent(s_Context, LocalPushReceiver.class);
		intent.putExtra(packageName + ".alarm_content", dataBundle);
		PendingIntent sender = PendingIntent.getBroadcast(s_Context, alarmCounter, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		AlarmManager am = (AlarmManager) s_Context.getSystemService(s_Context.ALARM_SERVICE);
	#if SIMPLIFIED_PN
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + ((long)(delay*1000)), sender);
	#else
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	#endif
		
		String sDelay= Integer.toString(delay);
		SaveAlarmInfo(alarmID, sDelay, dataBundle);
	}
	
	public static void TriggerAlarm(String alarmID)
	{
		INFO(TAG, "[TriggerAlarm]");
		DBG(TAG, "Looking for: ["+alarmID+"]");
		try
		{
			Context	s_Context = SUtils.getContext();
			if (s_Context != null)
			{
				SharedPreferences prefs = Prefs.get(s_Context);
				Bundle dataBundle;
				String sKey 	= "";	String sValue 	= "";
				String sExpTime = "";	String sAlarmid = "";
				int start = 0; int end = 0;

				if(prefs.contains(alarmID))
				{
					DBG(TAG, "Alarm found!!");
					String content = prefs.getString(alarmID, null);
					dataBundle = new Bundle();
					
					while (end < content.length())
					{
						start = content.indexOf(KEY_SPACER);
						end = content.indexOf("=", start);
						sKey = content.substring(start + KEY_SPACER.length(), end);
						content = content.substring(end + 1);
						start = end + 1;
						 
						if(content.contains(KEY_SPACER))
							end = content.indexOf(KEY_SPACER);
						else
							end = content.length();
						 
						sValue = content.substring(0, end);
						
						if("schedule_time".equals(sKey))
							sExpTime = sValue;
						else if(PN_UTILS_CLASS.KEY_LOCAL_ID.equals(sKey))
							sAlarmid = sValue;
							
						dataBundle.putString(sKey, sValue);
					}
					
					if(!isExpired(sExpTime))
					{
						DBG(TAG, "Setting Alarm: " + sExpTime);
						String packageName = s_Context.getPackageName();
						int alarmIndex = Integer.parseInt(alarmID.substring(ALARM_ID_HEADER.length()));
						Intent intent = new Intent(s_Context, LocalPushReceiver.class);
						intent.putExtra(packageName + ".alarm_content", dataBundle);
						PendingIntent sender = PendingIntent.getBroadcast(s_Context, alarmIndex, intent, PendingIntent.FLAG_UPDATE_CURRENT);
						AlarmManager am = (AlarmManager) s_Context.getSystemService(s_Context.ALARM_SERVICE);
						am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), sender);
					}
					else
					{
						ERR(TAG, "Alarm Expired: " + sExpTime);
						LocalPushManager.CancelAlarm(alarmID);
						if(SimplifiedAndroidUtils.mPushDBHandler == null)
						{
							SimplifiedAndroidUtils.mPushDBHandler = new PushDBHandler(s_Context);
							SimplifiedAndroidUtils.mPushDBHandler.open();
						}
						SimplifiedAndroidUtils.mPushDBHandler.deletePNRecord(PushSQLiteHelper.COLUMN_PNID, alarmID);
						SimplifiedAndroidUtils.BackupDatabase();
					}
				}
				else
					ERR(TAG, "Alarm Not Found!!");
			}
		} catch (Exception e) {ERR(TAG,"Error Triggering Alarm: " + e);}
	}
	
	public static void CancelAlarm(String ID)
	{
		INFO(TAG, "[CancelAlarm]");
		DBG(TAG, "Alarm ID: ["+ID+"]");
		Context	s_Context = SUtils.getContext();
		int AlarmID = Integer.parseInt(ID.substring(ALARM_ID_HEADER.length()));
		Intent intent = new Intent(s_Context, LocalPushReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(s_Context, AlarmID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) s_Context.getSystemService(s_Context.ALARM_SERVICE);
		am.cancel(sender);
		
		SharedPreferences prefs = Prefs.get(s_Context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(ID);
		editor.commit();
	}
	
	public static void CancelAll()
	{
		INFO(TAG, "[CancelAll]");
		Context	s_Context = SUtils.getContext();
		SharedPreferences prefs = Prefs.get(s_Context);
		SharedPreferences.Editor editor = prefs.edit();
		
		Intent intent = new Intent(s_Context, LocalPushReceiver.class);
		int alarmCounter = ALARM_MAX_INDEX;
		String ID;
		
		while (alarmCounter >= 0)
		{
			ID = ALARM_ID_HEADER + alarmCounter;
			if(prefs.contains(ID))
			{
				DBG(TAG, "Local notification removed [" + ID +"]");
				PendingIntent sender = PendingIntent.getBroadcast(s_Context, alarmCounter, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager am = (AlarmManager) s_Context.getSystemService(s_Context.ALARM_SERVICE);
				am.cancel(sender);
				editor.remove(ID);
			}
			alarmCounter--;
		}
		editor.putInt(ALARM_MAX_ID, 0);
		editor.commit();
	}
	
	static boolean isExpired(String expDate)
	{
		try{
			SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy", java.util.Locale.getDefault());
			Date targetDate =  df.parse(expDate);
			return (targetDate.before(new Date()));
		} catch (Exception e){ERR(TAG,"Exception: " + e); return true;}
	}
	
	static String SecondsToHours(int seconds)
	{
		long days,hours,minutes;
		long restday,resthour,restmin;
				
		days = seconds/(24*60*60);
		restday = seconds%(24*60*60);
		
		hours = restday/(60*60);
		resthour = restday%(60*60);
		
		minutes = resthour/60;
		restmin = resthour%60;
		
		return(days + " Days " + hours + " Hours " + minutes + " Minutes " + restmin + " Seconds");
	}
}
#endif