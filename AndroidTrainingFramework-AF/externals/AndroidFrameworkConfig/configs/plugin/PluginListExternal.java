package APP_PACKAGE.PackageUtils.PluginSystem;

public class PluginListExternal
{
	public static String[] list = 
	{
        //add your plugin full path here (package + classname)
        //Example: 
        //"com.plugintest.PluginTest",
		
		//"com.gameloft.GLSocialLib.SocialPlugin",
		//"com.gameloft.gameoptions.GameOptions",
		//STR_APP_PACKAGE + ".SendInfoPlugin",
		
		#if USE_IN_APP_BILLING_CRM //for IAP lib
		//STR_APP_PACKAGE + ".iab.InAppBillingPlugin",
		#else
		//STR_APP_PACKAGE + ".iab.InAppBillingLegacyPlugin",
		#endif //USE_IN_APP_BILLING_CRM
	};
}
