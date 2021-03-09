package APP_PACKAGE.PackageUtils;

#if USE_ADS_SERVER

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;

import APP_PACKAGE.AdServer;
import APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver;


// TODO -- Add onDestroy
public class AdServerPlugin implements IPluginEventReceiver
{
	// Static reference to ad server
	private static AdServer    adServer = null;
	private static ViewGroup appSurface = null;

	public void onPluginStart(Activity activity, ViewGroup surface)
	{
		// Create the ad server view with the initial values
		adServer = new AdServer(activity, AdServer.BOTTOM_CENTER, AdServer.BANNER_SMALL);
		adServer.InitAds(surface);
		
		// Set the surface
		appSurface = surface;
	}
	
	public void onPreNativePause() { }
	
	public void onPostNativePause()
	{
		adServer.onPause();
	}
	
	public void onPreNativeResume()
	{
		adServer.onResume();
	}
	
	public void onPostNativeResume() { }
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) { return false; }
	
/////////////////////////////////////////////////////
// Ad Server Methods
/////////////////////////////////////////////////////
	// Set Ad Server properties
	public static void SetBannerProperties(int size, int position)
	{
		adServer.bannerSize     = size;
		adServer.bannerPosition = position;
	}
	
	// Show the banner
	public static boolean ShowBanner(int language)
	{
		if(adServer != null && appSurface != null)
		{
			adServer.currentLanguage = language;
			adServer.ShowBanner(appSurface);
			return true;
		}
		else
		{			
			Log.e("ACP_LOGGER", "AdServerPlugin::ShowBanner - adServer or appSurface are null.");
			return false;
		}
	}
	
	// Hide the banner
	public static boolean HideBanner()
	{
		if(adServer != null && appSurface != null)
		{			
			adServer.HideBanner(appSurface);
			return true;
		}
		else
		{
			Log.e("ACP_LOGGER", "AdServerPlugin::HideBanner - adServer or appSurface are null.");
			return false;
		}
	}
	
	// Show interstitial
	public static void ShowInterstitial(int language)
	{
		adServer.currentLanguage = language;
		adServer.ShowInterstitial();
	}
	
	// Show interstitial with tag
	public static void ShowInterstitialWithTags(int language, String tagsString)
	{
		adServer.currentLanguage = language;
		adServer.ShowInterstitialWithTags(tagsString);
	}
	
	// Load free cash
	public static void LoadFreeCash(int language)
	{
		adServer.currentLanguage = language;
		adServer.GetFreeCash();
	}
	
	// Is Free Cash Ready
	public static boolean IsFreeCashReady()
	{		
		return adServer.showFreeCash;
	}
	
	// Show Free Cash
	public static void ShowFreeCash(int language)
	{
		if(adServer.showFreeCash)
		{
			adServer.currentLanguage = language;
			adServer.OpenFreeCash();
		}
	}
	
	// Set is PAU
	// Call this to notify the AdServer that this is a paying user and should not receive ads
	public static void SetIsPAU(boolean bIsPAU)
	{
		adServer.setIsPAU(bIsPAU);
	}
	
	public static String AdsHttpResponseToUrl (String i_url)
	{
		return adServer.getHttpResponse(i_url);
	}
}		
	
#endif // USE_ADS_SERVER