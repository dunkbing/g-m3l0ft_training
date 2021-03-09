#if SIMPLIFIED_PN
package APP_PACKAGE.PushNotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;
import java.util.Set;
import java.util.Iterator;
import org.json.JSONObject;
import APP_PACKAGE.R;

public class LocalPushReceiver extends BroadcastReceiver {
	SET_TAG("PushNotification");

	//final static String BOOT_EVENT = "android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
		DBG(TAG, "onReceive ");
		try 
		{
			LocalPushManager.Init(context);
			
			if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
			{
				WARN(TAG, "Boot Completed: " + STR_APP_PACKAGE);
				Boolean isDDMSIntent = false;
				try{
					Bundle extras = intent.getExtras();
					if(extras != null && extras.containsKey(PushSQLiteHelper.DDMS_TOOL_FLAG) && (extras.getString(PushSQLiteHelper.DDMS_TOOL_FLAG)).equals("1"))
					{
						WARN(TAG, "** DDMS Tool Detected **");
						isDDMSIntent = true;
						LocalPushManager.TriggerAlarm(extras.getString(PushSQLiteHelper.COLUMN_PNID));
						return;
					}
				} catch (Exception e) {ERR(TAG,"Exception: " + e); if(isDDMSIntent) return;}
				LocalPushManager.LoadAlarmInfo();
				return;
			}
			#if SIMPLIFIED_PN
			boolean enabled = true;
			#else
			boolean enabled = Prefs.isEnabled(context);
			#endif
			
			DBG(TAG, "************** NOTIFICATION INCOMING **************");
			if(enabled)
			{
				Bundle bundle = intent.getExtras();
				JSONObject jObject = new JSONObject();
				
				if (bundle != null)
				{
					String body = null;
					String subject = null;
					String type = null;
					String alarmID = null;
					String url = null;
					
					String packageName = context.getPackageName();
					Bundle PNdataBundle = bundle.getBundle(packageName + ".alarm_content");
					
					if (PNdataBundle != null) 
					{
						/////////////////////////// GET BUNDLE DATA /////////////////////////
						Set<String> keys = PNdataBundle.keySet();  
						Iterator<String> iterate = keys.iterator();
						
						while (iterate.hasNext()) {
							String key = iterate.next();

								if(key.equals(PN_UTILS_CLASS.KEY_BODY))
								{
									body = PNdataBundle.getString(PN_UTILS_CLASS.KEY_BODY);
								}else if(key.equals(PN_UTILS_CLASS.KEY_SUBJECT))
								{
									subject = PNdataBundle.getString(PN_UTILS_CLASS.KEY_SUBJECT);
								}else if(key.equals(PN_UTILS_CLASS.KEY_TYPE))
								{
									type = PNdataBundle.getString(PN_UTILS_CLASS.KEY_TYPE);
								}else if(key.equals(PN_UTILS_CLASS.KEY_URL))
								{
									url = PNdataBundle.getString(PN_UTILS_CLASS.KEY_URL);
								}else if(key.equals(PN_UTILS_CLASS.KEY_LOCAL_ID))
								{
									alarmID = PNdataBundle.getString(PN_UTILS_CLASS.KEY_LOCAL_ID);
								}
								
								#if !RELEASE_VERSION
								/////////////////////////// DEBUG ///////////////////////// 
								DBG(TAG, "Key: " + key + " = [" + PNdataBundle.getString(key)+"]");
								#endif
								
								try{
								if(PNdataBundle.getString(key) != null)
									jObject.put(key, PNdataBundle.getString(key));
								} catch(Exception e){DBG_EXCEPTION(e);}
						}
						////////////////////////////////////////////////////////////
					} 
					else
					{
						DBG(TAG, "No bundle info");
						return;
					}
					
					if (type != null && body != null)
					{
					
						if(PN_UTILS_CLASS.isTypeBlock(type))
						{
							DBG(TAG, "This type notification is actually block");
							return;
						}
						
						if(subject == null || subject.length() == 0)
							subject = context.getString(R.string.app_name);

						if(PNdataBundle.containsKey("customIcon"))
						{
							PN_UTILS_CLASS.useCustomIcon = true;
							PN_UTILS_CLASS.CustomIconName = PNdataBundle.getString("customIcon");
							DBG(TAG, "Setting a custom icon: [" + PN_UTILS_CLASS.CustomIconName + "]");
						}	
						else
						{
							PN_UTILS_CLASS.useCustomIcon = false;
							PN_UTILS_CLASS.CustomIconName = null;
						}
						
						// REMOTE IMAGES
						if(PNdataBundle.containsKey("image"))
						{
							PN_UTILS_CLASS.useCustomImage = true;
							PN_UTILS_CLASS.CustomImageName = PNdataBundle.getString("image");
						}
						else
						{
							PN_UTILS_CLASS.useCustomImage = false;
							PN_UTILS_CLASS.CustomImageName = null;
						}
						
						//CUSTOM SOUNDS
						if(PNdataBundle.containsKey("sound"))
						{
							PN_UTILS_CLASS.useCustomSound = true;
							PN_UTILS_CLASS.CustomSoundName = PNdataBundle.getString("sound");
						}
						else
						{
							PN_UTILS_CLASS.useCustomSound = false;
							PN_UTILS_CLASS.CustomSoundName = null;
						}
						
						Intent launchIntent = PN_UTILS_CLASS.getLaunchIntent(context, body, type, url, PNdataBundle);
						PN_UTILS_CLASS.generateNotification(context, body, subject, type, launchIntent, PNdataBundle);
						
						try{
							if(alarmID != null && alarmID.length() > 0)
							{
								LocalPushManager.CancelAlarm(alarmID);
								if(SimplifiedAndroidUtils.mPushDBHandler == null)
								{
									SimplifiedAndroidUtils.mPushDBHandler = new PushDBHandler(context);
									SimplifiedAndroidUtils.mPushDBHandler.open();
								}
								SimplifiedAndroidUtils.mPushDBHandler.deletePNRecord(PushSQLiteHelper.COLUMN_PNID, alarmID);
								SimplifiedAndroidUtils.BackupDatabase();
							}
							if(jObject != null)
							{
								Intent intent_broadcast = new Intent(PN_UTILS_CLASS.PN_BROADCAST_FILTER);
								if(intent_broadcast != null)
								{
									intent_broadcast.putExtra(PN_UTILS_CLASS.PN_GET_BROADCAST, jObject.toString());
									context.sendBroadcast(intent_broadcast);
								}
							}
						} catch (Exception e) {ERR(TAG,"Error sending broadcast: " + e);}
					} else {WARN(TAG, "Invalid push content type: " + type + " body: " + body);}
				}
			} else //disabled
			{
				DBG(TAG, "Notifications are disabled");
			}
		} catch (Exception e) {		DBG_EXCEPTION(e);	}
	}
}
#endif