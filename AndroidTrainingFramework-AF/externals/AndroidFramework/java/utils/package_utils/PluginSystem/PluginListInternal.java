package APP_PACKAGE.PackageUtils.PluginSystem;

public class PluginListInternal
{
	public static String[] list = 
	{
        //add your plugin full path here (package + classname)
        //Example: 
		//"com.plugintest.PluginTest",
		
        #if SIMPLIFIED_PN
		STR_APP_PACKAGE + ".PushNotification.PushNotificationPlugin",
		#endif //SIMPLIFIED_PN
		
		#if USE_VIRTUAL_KEYBOARD
		STR_APP_PACKAGE + ".PackageUtils.VirtualKeyboardPlugin",
		#endif // USE_VIRTUAL_KEYBOARD
		
		#if USE_ADS_SERVER
		STR_APP_PACKAGE + ".PackageUtils.AdServerPlugin",
		#endif // USE_ADS_SERVER
		
		#if USE_IGP_FREEMIUM
		STR_APP_PACKAGE + ".PackageUtils.InGamePromotionPlugin",
		#endif // USE_IGP_FREEMIUM
		
		#if USE_VIDEO_PLAYER
		STR_APP_PACKAGE + ".PackageUtils.VideoPlayerPlugin",
		#endif // USE_VIDEO_PLAYER
		
		#if USE_HID_CONTROLLER
		STR_APP_PACKAGE + ".GLUtils.controller.HidControllerPlugin",
		#endif //USE_HID_CONTROLLER
		
		#if USE_IN_APP_BILLING
		#if !USE_IN_APP_BILLING_CRM //for legacy IAP
		STR_APP_PACKAGE + ".iab.InAppBillingLegacyPlugin",
		#endif //!USE_IN_APP_BILLING_CRM
		#endif //USE_IN_APP_BILLING
		
		#if USE_GOOGLE_ANALYTICS_TRACKING
		STR_APP_PACKAGE + ".PackageUtils.GoogleAnalyticsTrackerPlugin",
		#endif
		
		#if ENABLE_USER_LOCATION
		STR_APP_PACKAGE + ".PackageUtils.LocationPlugin",
		#endif
		
		STR_APP_PACKAGE + ".PackageUtils.LogoViewPlugin",
		
		STR_APP_PACKAGE + ".GLUtils.TrackingPlugin",
		
	};
	
}

