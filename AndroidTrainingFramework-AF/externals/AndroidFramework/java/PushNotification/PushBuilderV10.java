package APP_PACKAGE.PushNotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import APP_PACKAGE.R;

public class PushBuilderV10 extends PushBuilder
{
	public PushBuilderV10(Context c)
	{
		super(c);
		DBG(TAG, "Using Notification builder v10");
	}
	
	@Override
	public Notification build() 
	{
		Notification notification;
		notification = new Notification(PushTheme.getIcon(), title, when);
		notification.setLatestEventInfo(context, title, message, intent);
		
		if(autocancel)
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		//apply dont disturb policy
		if(!DontDisturbPolicy.isDontDisturbeTime(context))
		{
			DBG(TAG, "set all defaults");
			notification.defaults |= Notification.DEFAULT_ALL;
		}else
		{
			DBG(TAG, "using dont disturb");
		}
		
		return notification;
	}
	
	
	
}