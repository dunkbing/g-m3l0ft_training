package APP_PACKAGE.PushNotification;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Notification.BigTextStyle;
import android.app.Notification.BigPictureStyle;
import android.app.Notification.InboxStyle;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import APP_PACKAGE.R;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.net.Uri;

public class PushBuilderV16 extends PushBuilder
{
	public PushBuilderV16(Context c)
	{
		super(c);
		DBG(TAG, "Using Notification builder v16");
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
		
		if(PN_UTILS_CLASS.useCustomImage && PN_UTILS_CLASS.CustomImageName != null && RemoteImageManager.GetLocalAsset(context))
		{
			WARN(TAG, "Setting Remote Image!");
			Notification.BigPictureStyle n_bigPictureStyle = new Notification.BigPictureStyle();
			n_bigPictureStyle.setBigContentTitle(title);
			n_bigPictureStyle.setSummaryText(message);
			n_bigPictureStyle.bigPicture(RemoteImageManager.GetAsset());
			builder.setStyle(n_bigPictureStyle);
		}
		else
		{
		if(stackMessages != null)
		{
			Notification.InboxStyle n_inboxStyle = new Notification.InboxStyle();
			n_inboxStyle.setBigContentTitle(title);
			for (int i = stackMessages.length - 1; i >= 0; i--)
			{
				n_inboxStyle.addLine(stackMessages[i]);
			}
			builder.setStyle(n_inboxStyle);
		}
		else
		{
			Notification.BigTextStyle n_bigTextStyle = new Notification.BigTextStyle();
			n_bigTextStyle.setBigContentTitle(title);
			n_bigTextStyle.bigText(message);
			builder.setStyle(n_bigTextStyle);
		}
		}
			
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
		
		return builder.build();
	}
}