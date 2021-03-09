package APP_PACKAGE.utils;

public final class GoogleAnalyticsConstants
{
    public static final class Category
	{
		public static final String LaunchInstaller 	= "Launch Installer";
		public static final String StartDownload 	= "Start Download";
		public static final String FinishDownload 	= "Finish Download";
		public static final String Installer		= "Installer";
		public static final String InAppBilling		= "InAppBilling";
		public static final String Configuration	= "Configuration";
    }
	public static final class Action
	{
		public static final String WifiOnly 		= "Wifi Only";
		public static final String Wifi3G 			= "Wifi & 3G";
		public static final String RunningApps 		= "Running Applications";
		public static final String InstalledApps	= "Installed Applications";
		public static final String KernelVersion	= "Kernel Version";
		public static final String glRenderer		= "GL_RENDERER";
		public static final String glVendor			= "GL_VENDOR";
		public static final String glVersion		= "GL_VERSION";
		public static final String glExtensions		= "GL_EXTENSION";
		public static final String SdCard			= "SDcard";
		public static final String ScreenType		= "Screen Type";
		public static final String ScreenDensity	= "Screen Density";
		public static final String GoogleTransaction	= "Google Transaction";
		public static final String HDIDFV           = "HDIDFV";
    }
	public static final class Label
	{
		public static final String ThroughWifi 		= "Wifi";
		public static final String Through3G 		= "3G";
		public static final String WifiON	 		= "Wifi On";
		public static final String WifiOFF 			= "Wifi Off";
		public static final String BeforeInstall	= "Before Install";
		public static final String AfterInstall		= "After Install";
		public static final String DownloadTiming	= null;
		public static final String TransactionTiming	= null;
    }
	public static final class Value
	{
    }
	public static final class Name
	{
		public static final String InstallerDownloadTiming = "Total Download Time";
		public static final String GooglePurchaseTiming = "Total Transaction Time";
		public static final String GooglePurchaseErrorTiming = "Total Error Transaction Time";
	}
}

