package APP_PACKAGE.PushNotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.widget.RemoteViews;
import APP_PACKAGE.R;


import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.net.Uri;

public class PushBuilderV11 extends PushBuilder
{
	public PushBuilderV11(Context c)
	{
		super(c);
		DBG(TAG, "Using Notification builder v11");
	}
	
	
	public Notification build()
	{
		Notification.Builder builder = new Notification.Builder(context);
		builder
		.setContentTitle(title)
		.setContentText(message)
		.setSmallIcon(R.drawable.pn_status_icon)
		.setWhen (when)
		.setContentIntent(intent)
		.setTicker(title)
		.setAutoCancel(autocancel);
			
		//apply don't disturb policy
		if(!DontDisturbPolicy.isDontDisturbeTime(context))
		{
			if(PN_UTILS_CLASS.useCustomSound && PN_UTILS_CLASS.CustomSoundName != null)
			{
				try 
				{
					WARN(TAG, "Custom Sound Detected!");
					int resId = context.getResources().getIdentifier(PN_UTILS_CLASS.CustomSoundName, "raw", context.getPackageName());
					if(resId > 0)
					{
						Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/raw/" + PN_UTILS_CLASS.CustomSoundName);
						builder.setSound(uri);
						builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
						INFO(TAG, "Setting custom sound: " + uri);
					}
					else
					{
						builder.setDefaults(Notification.DEFAULT_ALL);
						ERR(TAG, "Custom sound not found! [" + PN_UTILS_CLASS.CustomSoundName + "] Setting default values");
					}
				} catch (Exception e) {builder.setDefaults(Notification.DEFAULT_ALL); ERR(TAG, "Exception: "+e); e.printStackTrace();}
			}
			else
				builder.setDefaults(Notification.DEFAULT_ALL);
		}
		else
		{
			WARN(TAG, "Don't Disturb Time! Setting Sounds/Vibration OFF");
		}
		
		Bitmap image = BitmapFactory.decodeResource(context.getResources(), PushTheme.getIcon());
		builder.setLargeIcon(image);
		
		if(stackCounter > 1)
			builder.setNumber(stackCounter);
		
		if(deleteIntent != null)
			builder.setDeleteIntent(deleteIntent);
			
		return builder.getNotification();
	}
}