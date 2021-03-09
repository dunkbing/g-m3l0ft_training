#if USE_TRACKING_FEATURE_INSTALLER //|| USE_TRACKING_FEATURE_BILLING
package APP_PACKAGE.installer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import APP_PACKAGE.GLUtils.Config;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.R;


public class Tracker 
{
	// Application Preferences (Records)
    public static final String          PREFERENCES_NAME                    	= Config.GGC+"TInfo";
	public static final String 			TRACKING_LAUNCH_INSTALLER_BASE 			= "Launchinstaller";
	public static final String 			TRACKING_DOWNLOAD_START_BASE 			= "Downloadstart";
	public static final String 			TRACKING_DOWNLOAD_FINISH_BASE 			= "Downloadfinish";
#if USE_TRACKING_UNSUPPORTED_DEVICE	
	public static final String 			TRACKING_UNSUPPORTED_DEVICE 			= "UnsupportedDevice";
#endif	
	//public static final String 			TRACKING_INSTALL_REFERRER 				= "InstallReferrer";
	
	public static final String 			WIFI_ONLY_PREFIX	 					= "A";
	public static final String 			WIFI_3G_PREFIX							= "B";
	public static final String 			DNL_WIFI_PREFIX							= "Wifi";
	public static final String 			DNL_3G_PREFIX							= "3G";
	public static final String 			NO_WIFI_PREFIX							= "NOWifi";
	
	public static final int 			WIFI_3G 								= 0; 
	public static final int 			WIFI_ONLY 								= 1;
	public static final int 			WIFI_3G_ORANGE_IL 						= 2;
	
	static Device mDevice;
	static XPlayer mXPlayer;
	public static boolean isUsingTracking = false;
  
#if USE_TRACKING_FEATURE_INSTALLER
   
	private static String mUserID = "";
	public static final int OPTION_LAUNCH_INSTALLER 	= 0;
	public static final int OPTION_DOWNLOAD_START	 	= 1;
	public static final int OPTION_DOWNLOAD_FINISH	 	= 2;
#if USE_TRACKING_UNSUPPORTED_DEVICE	
	public static final int OPTION_UNSUPPORTED_DEVICE	= 3;
#endif
	//public static final int OPTION_INSTALL_REFERRER		= 4;
	
	public static final Object lock = new Object();

	 /**
     * build the query to use with sendInstallerTrackingOptions
     * @param wifiMode: A boolean value representing the installer flow that is used(true => wifiOnly Mode, false => wifi/3g Mode).
     * @param type: An int value representing the tracking to send, valid optios are:OPTION_LAUNCH_INSTALLER, OPTION_DOWNLOAD_START,OPTION_DOWNLOAD_FINISH.
     * @param selected_3g: a boolean representing the network selected for the download(true => 3g, false=> wifi) used only when wifiMode=false
     * @param wifiAlive: a boolean representing the wifi network status (true => Wifi Available, false=> Wifi not available) used only when type=OPTION_LAUNCH_INSTALLER
     * @param url: a string representing the url send to tracker actually used only when type=OPTION_INSTALL_REFERRER
     */
	private static String getQryRequestType (int wifiMode, int type, boolean selected_3g, boolean wifiAlive, String url)
	{
		String qry = "&action=";
		switch (type)
		{
			case OPTION_LAUNCH_INSTALLER:
				qry += TRACKING_LAUNCH_INSTALLER_BASE;
				qry += wifiMode == WIFI_ONLY ? WIFI_ONLY_PREFIX:WIFI_3G_PREFIX;
				qry += !wifiAlive?NO_WIFI_PREFIX:"";
			break;
			case OPTION_DOWNLOAD_START:
				qry += TRACKING_DOWNLOAD_START_BASE;
				if (wifiMode == WIFI_ONLY)
					qry += WIFI_ONLY_PREFIX;
				else
				{	
					qry += WIFI_3G_PREFIX;
					qry += selected_3g?DNL_3G_PREFIX:DNL_WIFI_PREFIX;
				}
			break;
			case OPTION_DOWNLOAD_FINISH:
				qry += TRACKING_DOWNLOAD_FINISH_BASE;
				if (wifiMode == WIFI_ONLY)
					qry += WIFI_ONLY_PREFIX;
				else
				{	
					qry += WIFI_3G_PREFIX;
					qry += selected_3g?DNL_3G_PREFIX:DNL_WIFI_PREFIX;
				}
			break;
#if USE_TRACKING_UNSUPPORTED_DEVICE	
			case OPTION_UNSUPPORTED_DEVICE:
				qry += TRACKING_UNSUPPORTED_DEVICE;
			break;	
#endif
/*
			case OPTION_INSTALL_REFERRER:
				qry += TRACKING_INSTALL_REFERRER;
				qry += "&" + url;
			break;
*/
		}
		return qry;
	}
	
	public static void launchInstallerTracker(int wifiMode,  boolean wifiAlive)
	{
		sendInstallerTrackingOptions (wifiMode, OPTION_LAUNCH_INSTALLER, false, wifiAlive, "");
	}
    
	public static void downloadStartTracker(int wifiMode, boolean selected_3g)
	{
		sendInstallerTrackingOptions (wifiMode, OPTION_DOWNLOAD_START, selected_3g, false, "");
	}
    
	public static void downloadFinishTracker(int wifiMode, boolean selected_3g)
	{
		sendInstallerTrackingOptions (wifiMode, OPTION_DOWNLOAD_FINISH, selected_3g, false, "");
	}
	
#if USE_TRACKING_UNSUPPORTED_DEVICE	
	public static void UnsupportedDeviceTracker()
	{
		sendInstallerTrackingOptions ( 0, OPTION_UNSUPPORTED_DEVICE, false, false, "");
	}
#endif

/*
#if USE_MARKET_INSTALLER
	public static void SendInstallReferrer()
	{
		String url = IReferrerReceiver.getReferrer(SUtils.getContext());
		
		if(url != null & url.compareTo("") != 0)
		{
			DBG("Tracker","Referrer info: "+url);
			sendInstallerTrackingOptions ( 0, OPTION_INSTALL_REFERRER, false, false, url);
		}else
		{
			DBG("Tracker","Referrer info not found");
		}
		
	}
#endif
*/
	/**
     * Send a tracking request to the gameloft server
     * @param wifiMode: A boolean value representing the installer flow that is used(true => wifiOnly Mode, false => wifi/3g Mode).
     * @param type: An int value representing the tracking to send, valid optios are:OPTION_LAUNCH_INSTALLER, OPTION_DOWNLOAD_START,OPTION_DOWNLOAD_FINISH.
     * @param selected_3g: a boolean representing the network selected for the download(true => 3g, false=> wifi) used only when wifiMode=false
     * @param wifiAlive: a boolean representing the wifi network status (true => Wifi Available, false=> Wifi not available) used only when type=OPTION_LAUNCH_INSTALLER
     */
	public static void sendInstallerTrackingOptions (final int wifiMode, final int type, final boolean selected_3g, final boolean wifiAlive, final String url)
	{
	
		new Thread() {
			public void run() {
				synchronized(lock)
	            {
					String qry = getQryRequestType(wifiMode, type, selected_3g, wifiAlive, url);
					DBG("Tracker", "sendInstallerTrackingOptions qry ["+ qry + "] wifiMode [" + wifiMode + "] type [" + type + "] selected_3g [" + selected_3g + "] wifiAlive [" + wifiAlive+"]");

				
					///////////////////////// 	One hit trackers
					if (type == OPTION_LAUNCH_INSTALLER && wifiAlive && SUtils.getPreferenceBoolean(qry, false, PREFERENCES_NAME))
					{
						DBG("Tracker","this tracker was allready sent ["+qry+"]" );
						return;
					}
					
					// if (type == OPTION_INSTALL_REFERRER && SUtils.getPreferenceBoolean(qry, false, PREFERENCES_NAME))
					// {
						// DBG("Tracker","this tracker was allready sent ["+qry +"]");
						// return;
					// }
					///////////////////////// 	
					
					
					mDevice = new Device();
					mDevice.setUserID(mUserID);
		        	mXPlayer = new XPlayer(mDevice);
					
					/*
					//install referrer will be on other URL, yes, one more url for tracking :S
					if (type == OPTION_INSTALL_REFERRER)
					{
						mXPlayer.sendInstallerTrackingInstallReferrer(qry);
						while (!mXPlayer.handleInstallerTrackingInstallReferrer())
						{
							try {
								Thread.sleep(100);
							} catch (Exception exc) {}
						}
					}
					else*/
					{
						mXPlayer.sendInstallerTrackingOptionsRequest(qry);
						while (!mXPlayer.handleInstallerTrackingOptionsRequest())
						{
							try {
								Thread.sleep(100);
							} catch (Exception exc) {}
						}
					}
					
		        	if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		        	{
		        		DBG("Tracker", "tracking for query ["+ qry +"] successfully recorded");
		            	SUtils.setPreference(qry, true, PREFERENCES_NAME);
		        	}
		        }
				mXPlayer = null;
			}
		}.start();
	}

	public static void setUserID(String uid)
	{	
		mUserID = uid;
	}
#endif //#if USE_TRACKING_FEATURE_INSTALLER  

#if USE_TRACKING_FEATURE_BILLING
	//TODO Move this file to utils, and add the billing trakers here
#endif
}


#endif //#if USE_TRACKING_FEATURE_INSTALLER