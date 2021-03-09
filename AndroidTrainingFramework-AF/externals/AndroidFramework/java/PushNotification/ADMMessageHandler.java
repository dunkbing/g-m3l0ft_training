#if (SIMPLIFIED_PN && AMAZON_STORE)
package APP_PACKAGE.PushNotification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Set;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazon.device.messaging.ADMConstants;
import com.amazon.device.messaging.ADMMessageHandlerBase;
import com.amazon.device.messaging.ADMMessageReceiver;

import APP_PACKAGE.PushNotification.PN_UTILS_CLASS;
import APP_PACKAGE.PushNotification.Prefs;
import APP_PACKAGE.R;

/**
 * The ADMMessageHandler class receives messages sent by ADM via the MessageAlertReceiver receiver.
 */
public class ADMMessageHandler extends ADMMessageHandlerBase 
{
    /** Tag for logs. */
	SET_TAG("PushNotification");
	
	static Vector<String> mIds;

    /**
	 * The MessageAlertReceiver class listens for messages from ADM and forwards them to the 
	 * ADMMessageHandler class.
	 */
    public static class MessageAlertReceiver extends ADMMessageReceiver
    {
        public MessageAlertReceiver()
        {
            super(ADMMessageHandler.class);
        }
    }

    /**
	* Class constructor.
	*/
    public ADMMessageHandler()
    {
        super(ADMMessageHandler.class.getName());
		
		if(mIds == null)
			mIds = new Vector<String>();
    }
	
    /**
	* Class constructor, including the className argument.
	* 
	* @param className The name of the class.
	*/
    public ADMMessageHandler(final String className) 
    {
        super(className);
    }

    @Override
    protected void onRegistered(final String registrationId) 
    {
        DBG(TAG, "[onRegistered]");
        DBG(TAG, registrationId);
		
		if(registrationId != null && registrationId.length() > 0)
		{
			PN_UTILS_CLASS.setTokenReady();
			PN_UTILS_CLASS.nativeSendRegistrationData(registrationId);
			PN_UTILS_CLASS.AddEndpointRecordToDB(registrationId);
		}	
    }

    @Override
    protected void onUnregistered(final String registrationId) 
    {
        DBG(TAG, "[onUnregistered]");
		DBG(TAG, registrationId);
    }
	
	@Override
    protected void onRegistrationError(final String string)
    {
        DBG(TAG, "[onRegistrationError]");
        DBG(TAG, "Error: " + string);
    }
	
	@Override
    protected void onMessage(Intent intent)
    {
        DBG(TAG, "[onMessage]");
		DBG(TAG, "intent = " + intent.toString());
		
		Bundle intentExtras = null;
		JSONObject jObject = null;
		Context mContext = null;
		
		if(PN_UTILS_CLASS.sContextReference != null)
			mContext = PN_UTILS_CLASS.sContextReference.get();//jog-harri : error build amazon
		else
			mContext = this;//Use receiver's context when the application was killed from App Manager
		
		try 
		{
		intentExtras = intent.getExtras();
		}catch (Exception e) {ERR(TAG, "Error creating Bundle: " + e.toString());}
		
		if(intentExtras == null)
		{
			WARN(TAG, "Invalid Notification info (no extras)");
			return;
		}else
		{
			try 
			{
				jObject = new JSONObject(intentExtras.get("message").toString());
				String id = jObject.getString("id");
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
		
		boolean enabled = Prefs.isEnabled(mContext);
		
		DBG(TAG, "************** NOTIFICATION INCOMING **************");
		if (enabled) 
		{
			#if !RELEASE_VERSION
			/////////////////////////// DEBUG /////////////////////////
			Set<String> keys = intentExtras.keySet();  
			Iterator<String> iterate = keys.iterator();  
			while (iterate.hasNext()) {
				String key = iterate.next();  
				DBG(TAG, "Key: " + key + " = [" + intentExtras.get(key)+"]");
			}  
			////////////////////////////////////////////////////////////
			#endif
			
			Bundle extras = new Bundle();
			
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
				
			}catch (JSONException e) {ERR(TAG, "Error parsing data " + e.toString());}
			
			
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
						title 	= (mContext).getString(R.string.app_name);
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
					Intent launchIntent = PN_UTILS_CLASS.getLaunchIntent(mContext, body, type, url, extras);
					PN_UTILS_CLASS.generateNotification(mContext, body, title, type, launchIntent, extras);
					
					try{
						if(jObject != null)
						{
							Intent intent_broadcast = new Intent(PN_UTILS_CLASS.PN_BROADCAST_FILTER);
							if(intent_broadcast != null)
							{
								intent_broadcast.putExtra(PN_UTILS_CLASS.PN_GET_BROADCAST, jObject.toString());
								mContext.sendBroadcast(intent_broadcast);
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
}
#endif
