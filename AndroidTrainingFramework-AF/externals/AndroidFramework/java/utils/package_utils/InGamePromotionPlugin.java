package APP_PACKAGE.PackageUtils;

#if USE_IGP_FREEMIUM

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import APP_PACKAGE.GLUtils.GLConstants;
import APP_PACKAGE.IGPFreemiumActivity;
import APP_PACKAGE.PackageUtils.JNIBridge;
import APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver;

public class InGamePromotionPlugin implements IPluginEventReceiver
{
#if USE_IGP_REWARDS 
	public class RetrieveItemsListener implements IGPFreemiumActivity.RetrieveItemsListener
	{
		String 	giftmessage = "";
		int 	realamount 	= 0;
		String 	realType 	= "";
					
		public void onMessageReceived(String title, String message)
		{
			giftmessage = giftmessage + message;
			if (realamount != 0 && !realType.equals(""))
			{
				JNIBridge.NativeSetReward(realamount, realType, giftmessage);
				
				realamount = 0;
				giftmessage = "";
			}
		}
					
		public void onItemReceived(int id, String type, int amount, int isInstall, String destGameCode, String creation_date) 
		{
			realamount += amount;
			realType = type;
			if (!giftmessage.equals(""))
			{
				JNIBridge.NativeSetReward(realamount, realType, giftmessage);
				
				realamount = 0;
				giftmessage = "";
			}
		}
					
		public void onRetrieveItemFailed(int error_code, String error_message) 
		{
			// An error occured while cheking for items. See error_code and error_message strings for details. Will also be called if no items were earned.
		}
	}
#endif

	// Static reference to activity
	private static Activity s_mainActivity = null;
#if USE_IGP_REWARDS 
	private static RetrieveItemsListener localListener = null;
#endif
	
	public void onPluginStart(Activity activity, ViewGroup surface)
	{
		s_mainActivity = activity;
	#if USE_IGP_REWARDS 
		localListener = new RetrieveItemsListener();
	#endif //USE_IGP_REWARDS
	}
	
	public void onPreNativePause() { }
	public void onPostNativePause() { }
	
	public void onPreNativeResume() { }
	public void onPostNativeResume() { }
	
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	    if (requestCode == GLConstants.IGP_ACTIVITY_NUMBER)
        {
			JNIBridge.NativeOnIGPClosed();
			return true;
        }
	
		return false; 
	}
	
#if USE_IGP_REWARDS 
	public static void retrieveItems(int language, String gamecode)
	{
		IGPFreemiumActivity.retrieveItems(language, gamecode, localListener);
	}
#endif
	
	public static boolean launchIGP(int language, boolean isPortrait)
	{
		if(s_mainActivity == null)
		{
			return false;
		}
	
		if(language < 0)
		{
			// If you fluxed up the language somehow, it's ok... we'll fix it for you.
			language = 0;
		}
		
		Intent igpIntent = new Intent(s_mainActivity, IGPFreemiumActivity.class);
		
		igpIntent.putExtra("language", language);
		igpIntent.putExtra("isPortrait", isPortrait);
		
		s_mainActivity.startActivityForResult(igpIntent, GLConstants.IGP_ACTIVITY_NUMBER);
		return true;
	}
}

#endif // USE_IGP_FREEMIUM