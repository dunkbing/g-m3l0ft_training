package APP_PACKAGE.installer;

// #if USE_MARKET_INSTALLER
// import APP_PACKAGE.installer.utils.Tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;

public class IReferrerReceiver 
#if USE_GOOGLE_ANALYTICS_TRACKING
extends com.google.android.apps.analytics.AnalyticsReceiver
#else
extends BroadcastReceiver 
#endif
{

	static final String REFERRER 				= "referrer";
	static final String REQUIRE_SEND_REFERRER 	= "rsend_referrer";
	@Override
	public void onReceive(Context context, Intent intent)
	{
		DBG("IReferrerReceiver", "onReceive");
	#if USE_GOOGLE_ANALYTICS_TRACKING
		super.onReceive(context, intent);
		new com.google.android.gms.analytics.CampaignTrackingReceiver().onReceive(context, intent);
	#endif
		String url = intent.getStringExtra(REFERRER);
		String referrer = getReferrer(url);
		
		DBG("IReferrerReceiver", "url: " + url);
		DBG("IReferrerReceiver", "referrer: " + referrer);
		
		SharedPreferences settings = context.getSharedPreferences(REQUIRE_SEND_REFERRER, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		if(referrer != null)
		{
			DBG("IReferrerReceiver", "Saving Referrer Info: " + referrer);
			//HashMap<String, String> map = getMap(referrer);
			
			//save pending send value
			editor.putString(REFERRER, referrer);
			editor.commit();
		}else
		{
			DBG("IReferrerReceiver", "Referrer info not found on URL: " + url);
			editor.putString(REFERRER, "");
			editor.commit();
		}
		
	#if USE_HAS_OFFERS_TRACKING
		DBG("IReferrerReceiver", "Sending HasOffers tracker");
		try {
			com.mobileapptracker.Tracker receiver = new com.mobileapptracker.Tracker();
			receiver.onReceive(context, intent);
		} catch (Exception e) { DBG_EXCEPTION(e); }
	#endif
	
	/*	TapjoyReferralTracker class was removed from the Tapjoy SDK
	#if USE_ADS_SERVER
		DBG("IReferrerReceiver", "Sending TapjoyReferralTracker");
		com.tapjoy.TapjoyReferralTracker  receiver = new com.tapjoy.TapjoyReferralTracker();
		receiver.onReceive(context, intent);
	#endif
	*/
	}
	
	public static String getReferrer(Context context)
	{
		if(context != null)
		{
			SharedPreferences settings = context.getSharedPreferences(REQUIRE_SEND_REFERRER, 0);
			return settings.getString(REFERRER, "");
		}
		
		return "";
	}
	
	
	private String getReferrer(String url) 
	{
		String referrer = null;
		
		try {
			URI uri = new URI(url);
			JDUMP("IReferrerReceiver", uri);
			referrer = uri.getQuery().split("referrer=")[1];
		} catch (Exception e) {  }
		
		//some markets are not sending referrer= on url. look for utm_source value
		if(referrer == null)
		{
			DBG("IReferrerReceiver", "referrer not found");
			if(url.contains("utm_source") || (url.indexOf("utm_source")>-1))
			{
				DBG("IReferrerReceiver", "utm_source found: "+referrer);
				referrer = url;
			}
		}
		return referrer;
	}
	
	
	static final String BROADCAST_INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";
	
	public static void sendBroadcastIntent(Context context) {
    	Intent i = new Intent(BROADCAST_INSTALL_REFERRER);
    	DBG("IReferrerReceiver", "sendReferrer");
    	String id = context.getPackageName();
    	i.setPackage(id);
		String referrer = "utm_source=source+test&utm_medium=medium+test&utm_term=term+test&utm_content=content+test&utm_campaign=campaign+test";
    	i.putExtra(REFERRER, referrer);
    	context.sendBroadcast(i);
    }
}
// #endif