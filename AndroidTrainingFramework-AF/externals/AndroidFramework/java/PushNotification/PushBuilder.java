package APP_PACKAGE.PushNotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import APP_PACKAGE.R;

public abstract class PushBuilder
{
	SET_TAG("PushNotification");
	
	Context context;
	String message;
	String title;
	PendingIntent intent;
	PendingIntent deleteIntent;
	int icon;
	long when;
	boolean autocancel;
	int stackCounter;
	String[] stackMessages;
	
	public PushBuilder(Context c)
	{
		context = c;
		
		message = null;
		title = null;
		intent = null;
		deleteIntent = null;
		
		icon = 0;
		when = 0;
		autocancel = true;
		stackCounter = 0;
		stackMessages = null;
	}
	
	public void setMessage(String msg) 	{ message = msg; }
	public void setTitle(String ttl) 	{ title = ttl; }
	public void setIcon(int i) 			{ icon = i; }
	public void setTime(long time)		{ when = time; }
	public void setAutoCancel(boolean t){ autocancel = t; }
	public void setContentIntent(PendingIntent i) { intent = i;}
	public void setDeleteIntent (PendingIntent i) { deleteIntent = i;}
	public void setStackCounter(int sc) { stackCounter = sc;}
	public void setStackMessages(String[] sm) { stackMessages = sm;}
	
	public abstract Notification build(); 

	public static PushBuilder getBuilder(Context context)
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)			//API Level 10-
			return new PushBuilderV10(context);
		else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)	//API Level 11+
			return new PushBuilderV11(context);
		else																//API Level 16+
			return new PushBuilderV16(context);
	}
}