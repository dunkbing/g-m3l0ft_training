
package APP_PACKAGE.PushNotification;

import android.content.Context;
import android.content.res.Resources;

import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import android.app.Notification;
import APP_PACKAGE.R;

public class PushTheme 
{
	SET_TAG("PushNotification");
	
	public static Integer text_color 	= android.R.color.white;
	public static float text_size 		= 11;
	public static int icon 				= 0;
	// public static int big_icon 			= 0;
	public static int layout			= 0;
	
	public static final String COLOR_SEARCH_RECURSE_TIP 	= "SOME_SAMPLE_TEXT";
	
	
	public static void init(Context context)
	{
		if(icon == 0)
			extractColors(context);

		initCustoms(context);
	}
	
	public static Integer 	getTextColor() 	{ return text_color;	}
	public static float		getTextSize() 	{ return text_size; 	}
	public static int		getIcon()		{ return icon;			}
	// public static int		getBigIcon()	{ return big_icon;		}
	public static int 		getLayout()		{ return layout;		}
	
	
	static void initCustoms(Context context)
	{
		Resources res = context.getResources();
		
		if(PN_UTILS_CLASS.useCustomIcon)
		{
			if(PN_UTILS_CLASS.CustomIconName != null)
				icon = res.getIdentifier(PN_UTILS_CLASS.CustomIconName, "drawable", context.getPackageName());
			
			if(icon == 0)
			{
				WARN(TAG, "No custom icon found on drawable folder, setting default custom icon instead");
				icon = res.getIdentifier("pn_custom_icon", "drawable", context.getPackageName());
			}
		}
		else
			icon = res.getIdentifier("pn_icon", "drawable", context.getPackageName());
		
		layout = res.getIdentifier("custom_notification_layout", "layout", context.getPackageName());
		
		if(icon == 0)
			icon = R.drawable.icon;
			
		JDUMP(TAG, icon);
		JDUMP(TAG, layout);
	}
	
	
	
	static boolean recurseGroup(Context context, ViewGroup gp)
	{
		//DBG(TAG, "[recurseGroup]");
		final int count = gp.getChildCount();
	    for (int i = 0; i < count; ++i)
	    {
	        if (gp.getChildAt(i) instanceof TextView)
	        {
	        	final TextView text = (TextView) gp.getChildAt(i);
	            final String szText = text.getText().toString();
	            if (COLOR_SEARCH_RECURSE_TIP.equals(szText))
	            {
	                text_color = text.getTextColors().getDefaultColor();
	                text_size = text.getTextSize();
	                DisplayMetrics metrics = new DisplayMetrics();
	                WindowManager systemWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
	                systemWM.getDefaultDisplay().getMetrics(metrics);
	                text_size /= metrics.scaledDensity;
	                text_size -= 2;
					
	                DBG(TAG, "theme text color = " + text_color);
	                DBG(TAG, "theme text size = " + text_size);
	                return true;
	            }
	        }
	        else if (gp.getChildAt(i) instanceof ViewGroup)
	            return recurseGroup(context, (ViewGroup) gp.getChildAt(i));
	    }
	    return false;
	}

	static void extractColors(final Context context)
	{
		//DBG(TAG, "get theme info");
		//SUtils.runOnUiThread( new Runnable() { public void run() {
			try
			{
				Notification ntf = new Notification();
				ntf.setLatestEventInfo(context, COLOR_SEARCH_RECURSE_TIP, "Utest", null);
				LinearLayout group = new LinearLayout(context);
				ViewGroup event = (ViewGroup) ntf.contentView.apply(context, group);
				if(!recurseGroup(context, event))
					text_color = android.R.color.white;
				group.removeAllViews();
			}
			catch (Exception e)
			{
				DBG_EXCEPTION(e);
				text_color = android.R.color.white;
			}
		//}});
	}
}