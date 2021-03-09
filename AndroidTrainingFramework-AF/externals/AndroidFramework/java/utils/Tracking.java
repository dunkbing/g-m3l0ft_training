package APP_PACKAGE.GLUtils;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import android.util.Log;

import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Encrypter;
#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
import APP_PACKAGE.installer.IReferrerReceiver;
#endif

public class Tracking
{
	#if USE_ANDROID_TV_IGPCODE
		private static String GAME_CODE 		= GGC_GAME_CODE_TV; //This must not be hardcode here, please set it at config.bat
	#else
		private static String GAME_CODE 		= GGC_GAME_CODE; //This must not be hardcode here, please set it at config.bat
	#endif
	private static String VERSION_NAME		= GAME_VERSION_NAME_LETTER;
	private static String IGP_VERSION 		= "2.1";	
	private static int TIMEOUT				= 1500;
	
	public static final int TYPE_LAUNCH_INSTALLER	= 1;
	public static final int TYPE_LAUNCH_GAME	= 2;
	public static final int TYPE_LAUNCH_INSTALLER_MAIN	= 3;


	private static int mTrackingFlags=0;
	public static final int TRACKING_FLAG_GAME_ON_STOP 				= 0x00000001;
	public static final int TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST	= 0x00000010;


	
	// Servers URLS
	#if HDIDFV_UPDATE
	private static String ON_LAUNCH_GAME 	= "http://ingameads.gameloft.com/redir/hdloading.php?game=#GAME#&country=#COUNTRY#&lg=#LANG#&ver=#IGP_VERSION#&device=#DEVICE#&f=#FIRMWARE#&udid=#ID#&hdidfv=#HDIDFV#&androidid=#ANDROID_ID#&g_ver=#VERSION#&line_number=#LINE_NUMBER#&google_adid=#GOOGLE_ADID#&google_optout=#GOOGLE_OPTOUT#";
	#else
	private static String ON_LAUNCH_GAME 	= "http://ingameads.gameloft.com/redir/hdloading.php?game=#GAME#&country=#COUNTRY#&lg=#LANG#&ver=#IGP_VERSION#&device=#DEVICE#&f=#FIRMWARE#&udid=#ID#&androidid=#ANDROID_ID#&g_ver=#VERSION#&line_number=#LINE_NUMBER#";
	#endif
	
	static String mIMEI;
	#if HDIDFV_UPDATE
	static String mHDIDFV;
	#endif
	static String mAndroidID;
	static String mLang;
	static String mCountry;
	static String mDevice;
	static String mFirmware;
	static String lineNum;
	
	public static void init()
	{
		#if (HDIDFV_UPDATE == 2)
		mIMEI 			= Device.getSerial();
		#else
		mIMEI 			= Device.getDeviceId();
		#endif
		#if HDIDFV_UPDATE
		mHDIDFV			= Device.getHDIDFV();
		DBG("A_S"+HDIDFV_UPDATE, Device.getHDIDFV());
		#endif
		mAndroidID		= android.provider.Settings.Secure.getString(SUtils.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		if (mAndroidID == null)
			mAndroidID = "null";
		mDevice 		= android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL;
		mFirmware 		= android.os.Build.VERSION.RELEASE;
		
		lineNum	= Device.getLineNumber();
//		Device.getGoogleAdId(); //make sure we have it available when we need it
		Device.initGoogleAdId();// for ACPv2 use explicit initialization
	}
	
	public static void onLaunchGame()
	{
		onLaunchGame(TYPE_LAUNCH_GAME, "");
	}	
	
	public static void onLaunchGame(final int type)
	{
		onLaunchGame(type, "");
	}
	
	public static void onLaunchGame(final int type, final String additionalParameters)
	{
		//launch request on thread to avoid blocking thread.
		new Thread( new Runnable()
		{
			public void run()
			{
			  mLock.lock();
				try
				{
					Thread.sleep(250);
					String serverURL = buildURL(ON_LAUNCH_GAME) + additionalParameters;
					#if (USE_MARKET_INSTALLER || GOOGLE_STORE_V3)
						if((SUtils.getContext().getApplicationInfo().flags & (android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
						{
							serverURL += "&appType=2";
						} else if((SUtils.getContext().getApplicationInfo().flags & (android.content.pm.ApplicationInfo.FLAG_SYSTEM )) != 0)
						{
							serverURL += "&appType=1";
						}
						else {
							serverURL += "&appType=3";
						}
						
						if(!android.text.TextUtils.isEmpty(SUtils.getInjectedIGP()))
						{
							serverURL += "&injected_igp="+SUtils.getInjectedIGP();
						}
						if(!android.text.TextUtils.isEmpty(SUtils.getInjectedSerialKey()))
						{
							serverURL += "&d="+SUtils.getInjectedSerialKey();
						}
						
					#endif
#if USE_INSTALLER
					switch(type)
					{
						case TYPE_LAUNCH_INSTALLER:
						case TYPE_LAUNCH_INSTALLER_MAIN:
							serverURL += "&check=1";
						break;
						case TYPE_LAUNCH_GAME:
						{
							if(! testFlags(TRACKING_FLAG_GAME_ON_STOP | TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST) )
							{
								DBG("Tracking","cannot track TYPE_LAUNCH_GAME, must set flags TRACKING_FLAG_GAME_ON_STOP and TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST");
								clearFlags();
								mLock.unlock();
								return;
							}
							clearFlags();
							serverURL += "&check=2";
						}
						break;
					}
#endif


#if (USE_INSTALLER)
	
					String LI_ver = SUtils.getPreferenceString("trc_LI_ver", "0.0.0", GAME_CODE);
					
					if (LI_ver.compareTo(VERSION_NAME) != 0)
					{
						DBG("Tracking","New version detected, Install Referrer and LaunchInstaller can be sent again");
						SUtils.removePreference("trc_SentIR", GAME_CODE);
						SUtils.removePreference("trc_SentLI", GAME_CODE);
					}

	
					if(type != TYPE_LAUNCH_GAME)
					{
	#if (USE_MARKET_INSTALLER || GOOGLE_STORE_V3)				
						if(SUtils.getPreferenceBoolean("trc_SentIR", false, GAME_CODE))
						{
							DBG("Tracking","Tracker IR already sent");
							mLock.unlock();
							return;
						}
          				
	  					String info = IReferrerReceiver.getReferrer(SUtils.getContext());
	  					if(info != null && info.compareTo("") != 0)
	  					{
	    					if(!SUtils.getPreferenceBoolean("trc_SentIR", false, GAME_CODE))
	    					   serverURL+="&action=InstallReferrer";
	  						serverURL += "&"+info;
	  					}
	            		

			            if(type == TYPE_LAUNCH_INSTALLER_MAIN)
			            {
			                if(info == null || info.compareTo("") == 0)
			                {
			                    DBG("Tracking","InstallReferrer not found, can't send TYPE_LAUNCH_INSTALLER_MAIN");
			                    mLock.unlock();
			                    return;
			                }
			            }
	#endif            
			            if(type == TYPE_LAUNCH_INSTALLER)
			            {
	#if (USE_MARKET_INSTALLER || GOOGLE_STORE_V3)
			                if(info == null || info.compareTo("") == 0)
	#endif
			                {
			                    if(SUtils.getPreferenceBoolean("trc_SentLI", false, GAME_CODE))
			                    {
			                        DBG("Tracking","Tracker TYPE_LAUNCH_INSTALLER already sent");
			                        mLock.unlock();
			                        return;
			                    }
			                }
			            }
         			}
#elif !USE_INSTALLER
         			if( type == TYPE_LAUNCH_GAME )
         			{
         				DBG("Tracking","onLaunchGame type is launch game, no installer");
     					if(! testFlags(TRACKING_FLAG_GAME_ON_STOP | TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST) )
						{
							DBG("Tracking","cannot track TYPE_LAUNCH_GAME, must set flags TRACKING_FLAG_GAME_ON_STOP and TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST");
							clearFlags();
							mLock.unlock();
							return;
						}
						clearFlags();
						DBG("Tracking","tracking TYPE_LAUNCH_GAME, flags are OK");

         				serverURL += "&check=2";

         			#if GOOGLE_STORE_V3
         				String info = IReferrerReceiver.getReferrer(SUtils.getContext());
	  					if(info != null && info.compareTo("") != 0)
	  					{
	    					if(!SUtils.getPreferenceBoolean("trc_SentIR", false, GAME_CODE))
	    					   serverURL+="&action=InstallReferrer";
	  						serverURL += "&"+info;
	  					}
	  				#endif
         			}
#endif

         			serverURL+="&enc=1";
					DBG("Tracking","onLaunchGame() serverURL: " + serverURL);
					String serverURLEncoded = "";
					try {
						String urlStr = serverURL;
						URL newUrl = new URL(urlStr);
						java.net.URI uri = new java.net.URI(newUrl.getProtocol(), newUrl.getUserInfo(), newUrl.getHost(), newUrl.getPort(), newUrl.getPath(), newUrl.getQuery(), newUrl.getRef());
						newUrl = uri.toURL();
						serverURLEncoded = newUrl.toString();
					} catch (Exception e) {
						serverURLEncoded = serverURL;
					}
					serverURL = serverURLEncoded;
					DBG("Tracking","onLaunchGame() serverURL after encoding: " + serverURL);
					
					
					URL url = new URL(serverURL);
					HttpURLConnection http = (HttpURLConnection)url.openConnection();
					http.setConnectTimeout(TIMEOUT);
					http.setRequestMethod("GET");	
					http.setRequestProperty("Connection", "close");
					int response = http.getResponseCode();     
					if (response == HttpURLConnection.HTTP_OK)
						DBG("Tracking","success ");
					else
					{
						DBG("Tracking","error "+response);
					}
					
#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
					if((type == TYPE_LAUNCH_INSTALLER || type == TYPE_LAUNCH_INSTALLER_MAIN ) && serverURL.contains("action=InstallReferrer"))
					{
					   SUtils.setPreference("trc_SentIR", true, GAME_CODE);
					   SUtils.setPreference("trc_SentLI", true, GAME_CODE);
					   
					   SUtils.setPreference("trc_LI_ver", VERSION_NAME, GAME_CODE);
					}
#endif
					if((type == TYPE_LAUNCH_INSTALLER || type == TYPE_LAUNCH_INSTALLER_MAIN ) && !serverURL.contains("action=InstallReferrer"))
					{
					   SUtils.setPreference("trc_SentLI", true, GAME_CODE);
					   SUtils.setPreference("trc_LI_ver", VERSION_NAME, GAME_CODE);
					}

				}catch(java.net.UnknownHostException unknown)
				{
					DBG("Tracking","No internet avaliable");
				}
				catch(Exception e){	DBG_EXCEPTION(e);}
				mLock.unlock();
			}
		}
		).start();
	}

private static final java.util.concurrent.locks.ReentrantLock mLock = new java.util.concurrent.locks.ReentrantLock();

#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
	public static void SendInstallReferrer()
	{
		//launch request on thread to avoid blocking thread.
		new Thread( new Runnable()
		{
			public void run()
			{
			   mLock.lock();
				try
				{
					String serverURL = buildURL(ON_LAUNCH_GAME);
					String info = IReferrerReceiver.getReferrer(SUtils.getContext());
					if(info == null || info.compareTo("") == 0)
					{
					    DBG("Tracking","Wait 3 seconds for IReferrerReceiver - SendInstallReferrer");
					    try {
  							Thread.sleep(3000);
  						} catch (Exception exc) {}
						info = IReferrerReceiver.getReferrer(SUtils.getContext());
					}
					if(info != null && info.compareTo("") != 0)
					{
						serverURL+="&enc=1";
						serverURL+="&action=InstallReferrer&"+info;
						DBG("Tracking","Referrer info: "+info);
					}else
					{
						DBG("Tracking","Referrer info not found");
						mLock.unlock();
						return;
					}
					
					DBG("Tracking","serverURL " + serverURL);
					//send tracker info only once
					if(SUtils.getPreferenceBoolean("trc_SentIR", false, GAME_CODE))
					{
						DBG("Tracking","This tracker was already send");
						mLock.unlock();
						return;
					}
					
					URL url = new URL(serverURL);
					HttpURLConnection http = (HttpURLConnection)url.openConnection();
					http.setConnectTimeout(TIMEOUT);
					http.setRequestMethod("GET");	
					http.setRequestProperty("Connection", "close");
					int response = http.getResponseCode();         
					if (response == HttpURLConnection.HTTP_OK)
						DBG("Tracking","Referrer sent successfully ");
					else
					{
						DBG("Tracking","error "+response);
					}
					
					SUtils.setPreference("trc_SentIR", true, GAME_CODE);
				}catch(java.net.UnknownHostException unknown)
				{
					DBG("Tracking","No internet avaliable");
				}
				catch(Exception e){	DBG_EXCEPTION(e);}
				mLock.unlock();
			}
		}
		).start();
	}
#endif
	private static String buildURL(String server)
	{
        String tmpIMEI = Encrypter.crypt(mIMEI); 
		String tmpLineNum = Encrypter.crypt(lineNum); 
		Locale locale 	= Locale.getDefault();
		mLang 			= locale.getLanguage();
		mCountry 		= locale.getCountry();
		
		int iGOptOutStatus = Device.getGoogleAdIdStatus();
		if(iGOptOutStatus != 0)
			iGOptOutStatus = 1;
		String googleOptOutStatus = Integer.toString(iGOptOutStatus);
		#if HDIDFV_UPDATE
		String tmpHDIDFV = mHDIDFV; 
		return server.replace("#GAME#",GAME_CODE).replace("#COUNTRY#",mCountry).replace("#LANG#",mLang).replace("#VERSION#", VERSION_NAME).replace("#DEVICE#",mDevice).replace("#FIRMWARE#",mFirmware).replace("#ID#",tmpIMEI).replace("#HDIDFV#",tmpHDIDFV).replace("#ANDROID_ID#", mAndroidID).replace("#IGP_VERSION#",IGP_VERSION).replace("#LINE_NUMBER#",tmpLineNum).replace("#GOOGLE_ADID#", Device.getGoogleAdId()).replace("#GOOGLE_OPTOUT#", googleOptOutStatus ).replace(" ",""); 
		#else
		return server.replace("#GAME#",GAME_CODE).replace("#COUNTRY#",mCountry).replace("#LANG#",mLang).replace("#VERSION#", VERSION_NAME).replace("#DEVICE#",mDevice).replace("#FIRMWARE#",mFirmware).replace("#ID#",tmpIMEI).replace("#ANDROID_ID#", mAndroidID).replace("#IGP_VERSION#",IGP_VERSION).replace("#LINE_NUMBER#",tmpLineNum).replace("#GOOGLE_ADID#", Device.getGoogleAdId()).replace("#GOOGLE_OPTOUT#", googleOptOutStatus ).replace(" ","");   
		#endif
	}


	// call this function with flag TRACKING_FLAG_GAME_ON_STOP when game onStop is called
	// and with flag TRACKING_FLAG_GAME_ON_WINDOW_FOUCS_LOST when onWindowsFocusChanged(false) is executed.
	public static void setFlag(int flag)
	{
		mTrackingFlags |= flag;
	}

	public static boolean testFlags(int flag)
	{
		return (mTrackingFlags & flag) == flag;
	}

	private static void clearFlags()
	{
		mTrackingFlags = 0;
	}

}

