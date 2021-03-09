#if SIMPLIFIED_PN && !AMAZON_STORE && !USE_NOKIA_API
package APP_PACKAGE;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;

import java.util.Set;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import APP_PACKAGE.PushNotification.PN_UTILS_CLASS;
import APP_PACKAGE.PushNotification.Prefs;

/**
 * GCMIntentService responsible for handling GCM messages.
 */
 
public class GCMIntentService extends GCMBaseIntentService {

	SET_TAG("PushNotification");
	
	static Vector<String> mIds;

    public GCMIntentService() {
        super(PN_UTILS_CLASS.SENDER_ID);
		
		if(mIds == null)
			mIds = new Vector<String>();
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        INFO(TAG, "[onRegistered] registrationId = " + registrationId);
		
		if(registrationId != null && registrationId.length() > 0)
		{
			PN_UTILS_CLASS.setTokenReady();
			PN_UTILS_CLASS.nativeSendRegistrationData(registrationId);
			PN_UTILS_CLASS.AddEndpointRecordToDB(registrationId);
		}
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        INFO(TAG, "[onUnregistered] registrationId = " + registrationId);
		
        if (GCMRegistrar.isRegisteredOnServer(context)) {
			INFO(TAG, "Unregister callback");
        } else {
            INFO(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        INFO(TAG, "[onMessage]");
		
		Bundle intentExtras = intent.getExtras();
		//JSONObject jObject = null;
		
		if(intentExtras == null)
		{
			WARN(TAG, "Invalid Notification info (no extras)");
			return;
		}else
		{
			try 
			{
				//jObject = new JSONObject(intentExtras.get("data").toString());
				//String id = jObject.getString("id");
				String id = intentExtras.getString("id");
				DBG(TAG, "Notification Id: " + id);
				
				if(id == null)
					return;
				
				if(mIds.contains(id))
				{
					//DBG(TAG, "Skipping same notification");
					return;
				}else
				{
					if(mIds.size() > 5)
						mIds.clear();
					mIds.add(id);
				}
			}catch (Exception e) {ERR(TAG, "Error creating JSONObject " + e.toString());}
		}
		
		boolean enabled = Prefs.isEnabled(context);
		
		DBG(TAG, "************** NOTIFICATION INCOMING **************");
		if (enabled) 
		{
			Bundle extras = intentExtras;
			
			/////////////////////////// DEBUG /////////////////////////
			Set<String> keys = intentExtras.keySet();  
			Iterator<String> iterate = keys.iterator();  
			JSONObject jObjectdata = new JSONObject();
			while (iterate.hasNext()) {
				String key = iterate.next();  
				DBG(TAG, "Key: " + key + " = [" + intentExtras.get(key)+"]");
				try{jObjectdata.put(key, intentExtras.get(key));}catch(Exception e){DBG_EXCEPTION(e);}
			}  
			////////////////////////////////////////////////////////////
			
			/*Bundle extras = new Bundle();
			
			try 
			{
				if(jObject != null)
				{
					INFO(TAG, "Creating new bundle from JSON data");
					Iterator<String> jIterate = jObject.keys();
					
					while (jIterate.hasNext()) {
						String jKey = jIterate.next();  
						DBG(TAG, "jKey: " + jKey + " = [" + PN_UTILS_CLASS.decodeString(jObject.getString(jKey)) + "]");
						extras.putString(jKey, PN_UTILS_CLASS.decodeString(jObject.getString(jKey)));
					}
				}
				
			}catch (JSONException e) {ERR(TAG, "Error parsing data " + e.toString());}*/
			
			
			if (extras != null && !extras.isEmpty()) 
			{
				String title 	= extras.getString("subject");
				String username = extras.getString("reply_to");
				String secTitle	= extras.getString("title");
				String type 	= extras.getString("type");
				String body 	= extras.getString("body");
				String url 		= extras.getString("url");
		
				if(title == null || title.length() == 0)
				{
					if(secTitle != null && secTitle.length() >0)
						title = secTitle;
					else
						title 	= context.getString(R.string.app_name);
				}
		
				if (type != null && body != null)
				{
				
					if(PN_UTILS_CLASS.isTypeBlock(type))
					{
						DBG(TAG, "This type notification is actually block");
						return;
					}
					
					if(type.equals(PN_UTILS_CLASS.PN_TYPE_PLAY) && username != null)
					{
						extras.putString("friend_id", username);
						DBG(TAG, "Request for playing sent by: "+username);
					}
					
					if(extras.containsKey("customIcon"))
					{
						PN_UTILS_CLASS.useCustomIcon = true;
						PN_UTILS_CLASS.CustomIconName = extras.getString("customIcon");
						DBG(TAG, "Setting a custom icon: [" + PN_UTILS_CLASS.CustomIconName + "]");
					}	
					else
					{
						PN_UTILS_CLASS.useCustomIcon = false;
						PN_UTILS_CLASS.CustomIconName = null;
					}
					
					// REMOTE IMAGES
					if(extras.containsKey("image"))
					{
						PN_UTILS_CLASS.useCustomImage = true;
						PN_UTILS_CLASS.CustomImageName = extras.getString("image");
					}
					else
					{
						PN_UTILS_CLASS.useCustomImage = false;
						PN_UTILS_CLASS.CustomImageName = null;
					}
					
					//CUSTOM SOUNDS
					if(extras.containsKey("sound"))
					{
						PN_UTILS_CLASS.useCustomSound = true;
						PN_UTILS_CLASS.CustomSoundName = extras.getString("sound");
					}
					else
					{
						PN_UTILS_CLASS.useCustomSound = false;
						PN_UTILS_CLASS.CustomSoundName = null;
					}
					
					/**
					* Issues a notification to inform the user that server has sent a message.
					*/
					Intent launchIntent = PN_UTILS_CLASS.getLaunchIntent(context, body, type, url, extras);
					PN_UTILS_CLASS.generateNotification(context, body, title, type, launchIntent, extras);
					
					try{
						if(jObjectdata != null)
						{
							Intent intent_broadcast = new Intent(PN_UTILS_CLASS.PN_BROADCAST_FILTER);
							if(intent_broadcast != null)
							{
								intent_broadcast.putExtra(PN_UTILS_CLASS.PN_GET_BROADCAST, jObjectdata.toString());
								sendBroadcast(intent_broadcast);
							}
						}
					} catch (Exception e) {ERR(TAG,"Error sending broadcast: " + e);}
				}else  
				{
					WARN(TAG, "Invalid push content type: " + type + " body: " + body);
				}
			}else  
			{
				WARN(TAG, "Invalid push no extras");
			}
		}else
		{
			DBG(TAG, "Notifications are disabled");
		}
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        INFO(TAG, "[onDeletedMessages] " + total);
    }

    @Override
    public void onError(Context context, String errorId) {
        INFO(TAG, "[onError] " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        INFO(TAG, "[onRecoverableError] " + errorId);
		
        return super.onRecoverableError(context, errorId);
    }
}
#endif
