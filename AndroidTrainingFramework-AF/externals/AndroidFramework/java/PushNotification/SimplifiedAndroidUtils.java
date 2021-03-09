#if SIMPLIFIED_PN
package APP_PACKAGE.PushNotification;

import org.json.JSONObject;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.Set;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.lang.String;
import java.lang.ref.WeakReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.os.Bundle;
import android.os.Build;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Activity;
import android.net.Uri;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.ActivityNotFoundException;
#if AMAZON_STORE
import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.ADMConstants;
import com.amazon.device.messaging.development.ADMManifest;
import java.lang.reflect.Method;
#else
#if USE_NOKIA_API
import com.nokia.push.PushRegistrar;
#else
import com.google.android.gms.gcm.GoogleCloudMessaging;
#endif //USE_NOKIA_API
import android.content.pm.PackageInfo;
import java.sql.Timestamp;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
#if ENABLE_OLD_GCM_API
import com.google.android.gcm.GCMRegistrar;
#endif //ENABLE_OLD_GCM_API
#endif //AMAZON_STORE
import APP_PACKAGE.PushNotification.Prefs;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.CLASS_NAME;
import APP_PACKAGE.R;

#if USE_INSTALLER
import APP_PACKAGE.installer.GameInstaller;
#endif
//////////////////////////////////////////////////////////////
// SimplifiedAndroidUtils
//////////////////////////////////////////////////////////////	

public class SimplifiedAndroidUtils
{
	SET_TAG("ACP_PN");
	
	#if USE_NOKIA_API
	public static final String SENDER_ID 			= "hermespn";//  same value for all gloft games.
	#else
	public static final String SENDER_ID 			= "108176907654";//  same value for all gloft games.
	#endif
	private static final String GMM_PACKAGE_NAME 	= STR_APP_PACKAGE;
    private static final String GMM_CLASS_NAME 	 	= STR_APP_PACKAGE + ACTIVITY_NAME_DOT;
	
	//notification types
    public static final String PN_TYPE_PLAY 	 	= "play";
    public static final String PN_TYPE_URL 	 		= "url";
    public static final String PN_TYPE_INFO 	 	= "info";
    public static final String PN_TYPE_LAUNCH 	 	= "launch";
    public static final String PN_TYPE_PROMO 	 	= "igpcode";
	
	private static boolean mGameLaunched			= false;
	private static boolean autoStartGame 			= false;
	public static boolean useCustomIcon 			= false;
	public static boolean useCustomSound 			= false;
	public static boolean useCustomImage 			= false;

	public static String CustomIconName 			= null;
	public static String CustomSoundName 			= null;
	public static String CustomImageName 			= null;
	static SimplifiedAndroidUtils mThis				= null;
	public static PushDBHandler mPushDBHandler		= null;
	
#if AMAZON_STORE
	static final String TRANSPORT 					= "adm";
#else
	#if USE_NOKIA_API
	static final String TRANSPORT 					= "nnapi";
	#else
	static final String TRANSPORT 					= "gcm";
	GoogleCloudMessaging gcmInstance 				= null;
	#endif
#endif
	//notification content
	static final String KEY_SUBJECT 				= "subject";
	static final String KEY_USERNAME 				= "username";
	static final String KEY_TYPE 					= "type";
	static final String KEY_BODY 					= "body";
	static final String KEY_URL 					= "url";
	
	//local notification id key
	static final String KEY_LOCAL_ID				= "lID";
	public static Bundle mData;
	public static WeakReference<Activity> 			sContextReference	=	null;
	
	
	static final String KeyGroupBaseString			= "LocalPNType_";
	
//////////////////////////////////////////////////////////////
// Preferences values
//////////////////////////////////////////////////////////////	

	final static String PREF_NOTIFICATION_ID		= "notificationID";
	final static String PREF_PN_STACK_ID			= "PN_stackID_";
	final static String PREF_PN_STACK_MSG			= "PN_stackMSG_";
	final static String PREF_REG_ID 				= "PN_regId";
    final static String PREF_APP_VERSION 			= "PN_appVersion";
    final static String PREF_EXPIRATION_TIME 		= "PN_regId_ExpirationTimeMs";
	final static long 	REG_EXPIRY_TIME_MS 			= 1000*3600*24*7; //Default lifespan (7 days) until it is considered expired.

	// Bundle data shared to native
	static final String EXTRA_DATA_BUNDLE 			= "pn_data_bundle";
	static final String EXTRA_AUTO_START_GAME 		= "pn_goto_multiplayer";
	static final String EXTRA_PN_LAUCH_GAME			= "pn_launch_game";
	static final String EXTRA_PN_LIB_INTENT			= "pn_lib_intent";
	static final String EXTRA_PN_GROUP_ID			= "pn_group_ID";
	static final int MAX_NOTIFICATION_ON_BAR		= 32;
	static final int PN_LAUNCH_GROUP				= 100;
	static final int PN_LAUNCH_GROUP_LOCAL			= 101;
	static final int PN_INFO_GROUP					= 200;
	static final int PN_INFO_GROUP_LOCAL			= 201;
	static final String[] SupportedTransportList 	= {"none", "gcm", "adm", "nnapi"};//sync this list with /native/utils/helpers.h, NotificationTransportType
	static final boolean mIsFirmwareBefore22 		= (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO);
	public static boolean isTypeBlock(String type) { return false;}
	public static void setTokenReady() {}
	
	public static final String PN_GET_BROADCAST 	= "get_broadcast_push";
	public static final String PN_BROADCAST_FILTER 	= STR_APP_PACKAGE + "_pushbroadcast";
	private static final String PN_ACTION_BROADCAST = PN_ACTION_RECEIVER;
	private static final String PN_DELETE_BROADCAST = PN_DELETE_RECEIVER;
	private BroadcastReceiver mReceiver;
	static final String APP_DATA_FOLDER 			= android.os.Environment.getExternalStorageDirectory() + "/Android/data/" + STR_APP_PACKAGE;
	

//////////////////////////////////////////////////////////////////////////////////////////
// Notification style
//////////////////////////////////////////////////////////////////////////////////////////	

	static void initTheme(Context c)
	{
		PushTheme.init(c);
	}
	
//////////////////////////////////////////////////////////////////////////////////////////
// Events
//////////////////////////////////////////////////////////////////////////////////////////

	public static void Init(Context context)
	{
		INFO(TAG,"[Init] ");
		sContextReference = new WeakReference<Activity>((Activity)context);
		
		if(mThis == null) {mThis = new SimplifiedAndroidUtils();}
	#if ENABLE_OLD_GCM_API
		WARN(TAG, "*Using old GCM Api!!*");
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
	#endif
	
	#if USE_NOKIA_API
		// Make sure the device has the proper dependencies.
		WARN(TAG, "Using Nokia API");
		if(isNokiaSupported())
		{
		try{
		PushRegistrar.checkDevice(context);
		PushRegistrar.checkManifest(context);
		}catch (Exception e) {ERR(TAG,"System checking error!!"); DBG_EXCEPTION(e);}
		}
	#endif
	
		DBG(TAG, "Checking values");
		DBG(TAG, "[Device ID]: "+ Device.getDeviceId());
		DBG(TAG, "[Build.VERSION.SDK_INT]: "+ Build.VERSION.SDK_INT);
		DBG(TAG, "[CLIENTID]: "+ CLIENTID);
	
		LocalPushManager.Init();
		
		//save first time push notifications as enabled
		Prefs.init(context);
		
		//get colors from system values
		initTheme(context);
		
		mGameLaunched = true;
		initWithIntent(((Activity)context).getIntent());
		
		//Registering BroadCast Receiver for Push Notification Callback
		IntentFilter intentFilter = new IntentFilter(PN_BROADCAST_FILTER);
        mThis.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Extract PN data from intent
                String received_push_data = intent.getStringExtra(PN_GET_BROADCAST);
				
				//Sending PN data to game
				nativeSendPNData(received_push_data);
				
                //Log PN data
                DBG(TAG, "Received Push Notification from BroadCast: "+ received_push_data);
            }
        };
        //registering our receiver
        context.registerReceiver(mThis.mReceiver, intentFilter);
	
		//Creating DataBase Handler
		if(mPushDBHandler == null)
		{
			mPushDBHandler = new PushDBHandler(context);
			mPushDBHandler.open();
			BackupDatabase();
		}
	}
	
	public static void onNewIntent(Intent intent)
	{
		boolean enabled = Prefs.isEnabled(sContextReference.get());
		
		mGameLaunched = false;
		if(enabled)
		{
			INFO(TAG, "[onNewIntent] "+intent);
			initWithIntent(intent);
		}
		
		JDUMP(TAG, mGameLaunched);
	}
	
	static void initWithIntent(Intent intent)
	{
		INFO(TAG, "[initWithIntent] " + intent);
		if(intent != null)
		{
			Bundle extras = intent.getExtras();
			//DBG(TAG, ">> EXTRA_PN_LIB_INTENT flag: " + extras.getBoolean(EXTRA_PN_LIB_INTENT, false));
			
			if(extras != null && extras.getBoolean(EXTRA_PN_LIB_INTENT, false))
			{
					////////////////////////////////////////////////////////////
					autoStartGame 	= extras.getBoolean(EXTRA_AUTO_START_GAME, false);
					//copy data bundle from game activity
					mData = new Bundle();
					
					Bundle data = extras.getBundle(EXTRA_DATA_BUNDLE);
					if(data != null)
					{
						mData.putAll(data);
					}
					
					//save info, notification resume the game
					mData.putInt(EXTRA_PN_LAUCH_GAME, mGameLaunched? 1: 0);
					mData.putInt(EXTRA_AUTO_START_GAME, autoStartGame?1: 0);
					
					//remove data from game intent
					extras.remove(EXTRA_AUTO_START_GAME);
					extras.remove(EXTRA_DATA_BUNDLE);
					
					//Look for Group Id header to clear stack
					int group = extras.getInt(EXTRA_PN_GROUP_ID, -1);
					WARN(TAG, "groupID: " +group);
					if(group > -1)
						ClearStack(sContextReference.get(), group);
					
					JDUMP(TAG, mData);
					JDUMP(TAG, mGameLaunched);
					JDUMP(TAG, autoStartGame);
			} else {WARN(TAG, "EXTRA_PN_LIB_INTENT not found!" );}
		} else {WARN(TAG, "Invalid intent" );}
	}
	
	public static String IsAppLaunchedFromPN()
	{
		INFO(TAG, "[IsAppLaunchedFromPN]" );
		return getMessagePayload(mData);
	}
	
	public static String getMessagePayload(Bundle messageData)
	{
		String messagePayload = "";
		if (messageData != null) 
		{
			try
			{
				/// Get Bundle Data ///
				Set<String> keys = messageData.keySet();  
				Iterator<String> iterate = keys.iterator();
				JSONObject jObject = new JSONObject();
				
				while (iterate.hasNext())
				{
					String key = iterate.next();
					DBG(TAG, "Key: " + key + " = [" + messageData.get(key)+"]");
					if(key.equals(EXTRA_PN_LAUCH_GAME))
					{
						if(messageData.getInt(key) == 1)
							DBG(TAG, "*** App Launched from PN***" );
						else
							DBG(TAG, "*** App Resumed from PN***" );
					}
					jObject.put(key, messageData.get(key));
				}
				
				messagePayload = jObject.toString();
			}
			catch(Exception e){DBG_EXCEPTION(e);}
		}
		else
			DBG(TAG, "Message payload not found!" );
		
		//Clear message payload after informing to game.
		mData = null;
		
		return messagePayload;
	}
	
	public static void LaunchAppFromPN(Context context, Intent intent)
	{
		INFO(TAG, "[LaunchAppFromPN]" );
		try{
			Intent notificationIntent = new Intent();
			notificationIntent.setClassName(GMM_PACKAGE_NAME, GMM_CLASS_NAME);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			notificationIntent.replaceExtras(intent);
			mGameLaunched = true;
			
			if(sContextReference != null && sContextReference.get() != null)//activity exists
			{
				WARN(TAG, "*Resuming current activity, no new instance created*");
				notificationIntent.setAction(Intent.ACTION_MAIN);
				notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				mGameLaunched = false;
				initWithIntent(intent);
			}

			context.startActivity(notificationIntent);
		} catch (Exception e) {DBG_EXCEPTION(e);}
	}
	
	public static void LaunchPNClearStack(Context context, Intent intent)
	{
		INFO(TAG, "[LaunchPNClearStack]" );
		try{
			Bundle extras = intent.getExtras();
			if(extras != null)
			{
				int group = extras.getInt(EXTRA_PN_GROUP_ID, -1);
				if(group > -1)
					ClearStack(context, group);
				return;
			}
		} catch (Exception e) {DBG_EXCEPTION(e);}
	}
	
	////////////////////// Called when app is not running using receiver context ////////////////////////////////
    public static Intent getLaunchIntent(Context context, String message, String type, String url, Bundle extras) 
	{
		Intent intent = null;
		
		if(type.equals(PN_TYPE_URL) || type.equals(PN_TYPE_PROMO))
		{
			DBG(TAG, "*url* type intent");
			if(!isEmptyOrNull(url))
			{
				try
				{
					if(type.equals(PN_TYPE_PROMO)) 
					{
					if(SUtils.getContext() == null)
						SUtils.setContext(context);
				#if (HDIDFV_UPDATE == 2)
					url = (url + "&udid=" + Device.getSerial());
					url = (url + "&hdidfv=" + Device.getHDIDFV());
					url = (url + "&androidid=" + Device.getAndroidId());
				#elif (HDIDFV_UPDATE == 1)
					url = (url + "&udid=" + Device.getDeviceId());
					url = (url + "&hdidfv=" + Device.getHDIDFV());
					url = (url + "&androidid=" + Device.getAndroidId());
				#else
					url = (url + "&udid=" + Device.getDeviceId());
				#endif
					Locale local 	= Locale.getDefault();
					url = (url + "&ver=" + GAME_VERSION_NAME_LETTER);
					url = (url + "&d=" + encodeString(android.os.Build.MODEL));
					url = (url + "&f=" + encodeString(Device.getDeviceFirmware()));
					url = (url + "&game_ver=" + GAME_VERSION_NAME_LETTER);
					url = (url + "&country=" + encodeString(Device.getNetworkCountryIso()));
					url = (url + "&lg=" + encodeString(local.getLanguage()));
					INFO(TAG, "*Promo URL* : "+url);
					}
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
					return intent;
				}catch(Exception e)
				{
					WARN(TAG, "Invalid URI: "+url);
				}
			} else
			{
				WARN(TAG, "No URL info found : "+url);
			}
			intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		else if(type.equals(PN_TYPE_PLAY) || type.equals(PN_TYPE_LAUNCH))
		{
			DBG(TAG, "*play/launch* type intent");
			
			intent = new Intent(PN_ACTION_BROADCAST);
			
			if(extras != null)
			{
				intent.putExtra(EXTRA_DATA_BUNDLE, extras);
			}
				
			if(type.equals(PN_TYPE_PLAY))
			{
				intent.putExtra(EXTRA_AUTO_START_GAME, true);
			}
		}
		else
		{
			DBG(TAG, "*info* type intent");
			intent = new Intent(PN_DELETE_BROADCAST);
			if(extras != null)
				intent.putExtra(EXTRA_DATA_BUNDLE, extras);
		}
        return intent;
    }

	public static void generateNotification(Context context, String msg, String title, String type, Intent intent, Bundle extras) 
	{
		DBG(TAG, "Creating notification");
		long when = System.currentTimeMillis();
		
		int notificationID = 0;
		int stackCounter = 0;
		int groupID = -1;
		String[] stackMessages = null;
		Intent deleteIntent = null;
		
		String sgroupID = extras.getString(EXTRA_PN_GROUP_ID); //Only Local PNs should contain this header
		if (!isEmptyOrNull(sgroupID))
		{try {groupID = Integer.parseInt(sgroupID);}catch(Exception e){ERR(TAG, "Exception: "+ e);}}
		
		SharedPreferences settings = Prefs.get(context);
		
		if((type.equals(PN_TYPE_LAUNCH) || type.equals(PN_TYPE_INFO)) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) // LAUNCH and INFO eligible for stacking (API Level 11+)
		{
			if(groupID == -1) //Missing groupID means Online PN
			{
				WARN(TAG, "*Stacking Online Groups*");
				if(type.equals(PN_TYPE_LAUNCH))
					groupID = PN_LAUNCH_GROUP; // Using default groupID for LAUNCH type (Online PN)
				else
					groupID = PN_INFO_GROUP; // Using default groupID for INFO type (Online PN)
			}
			else
			{
				WARN(TAG, "*Stacking Local Groups*");
				if(type.equals(PN_TYPE_LAUNCH))
					groupID = PN_LAUNCH_GROUP_LOCAL; // Using default groupID for LAUNCH type (Local PN)
				else
					groupID = PN_INFO_GROUP_LOCAL; // Using default groupID for INFO type (Local PN)
			}
			
			notificationID = (MAX_NOTIFICATION_ON_BAR) + groupID; // Using offset for multiple PN groups
			stackCounter = settings.getInt(PREF_PN_STACK_ID.concat(Integer.toString(notificationID)), 1); // Gets stack counter for given notificationID
			if(stackCounter == 0)
			{
				stackCounter = MAX_NOTIFICATION_ON_BAR - 1;
				ERR(TAG, "MAX_NOTIFICATION_ON_BAR Limit reached!!");
			}
			stackMessages = new String[stackCounter];
			stackMessages[stackCounter - 1] = msg;// Last slot for the current msg
				
			//Get the previous messages
			for(int i = 0; i < stackCounter - 1; i++)
			{
				String key = (PREF_PN_STACK_MSG.concat(Integer.toString(notificationID))).concat("_" + Integer.toString(i + 1));
				stackMessages[i] = settings.getString(key, "");
			}
			
			deleteIntent = new Intent(PN_DELETE_BROADCAST);
			deleteIntent.putExtra(EXTRA_PN_GROUP_ID, groupID); // Adding groupID to delete intent for clearing stack when PN is dismissed from notification bar
			
			intent.putExtra(EXTRA_PN_GROUP_ID, groupID); // Adding groupID to intent for clearing stack when the game is launched from PN
			
			WARN(TAG, "GroupID: " + groupID + " StackID: " + notificationID + " StackCounter: " + stackCounter);
		}
		else
			notificationID = settings.getInt(PREF_NOTIFICATION_ID, 0); // Allows multiple notifications
		
		initTheme(context);
		intent.putExtra(EXTRA_PN_LIB_INTENT, true);
	
		PushBuilder pb = PushBuilder.getBuilder(context);
		pb.setTitle(title);
		pb.setMessage(msg);
		pb.setTime(when);
		pb.setAutoCancel(true);
		
		if(stackCounter > 1)
			pb.setStackCounter(stackCounter);
			
		if(stackMessages != null)
			pb.setStackMessages(stackMessages);
			
		if(deleteIntent != null)
			pb.setDeleteIntent(PendingIntent.getBroadcast(context, notificationID, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		
		if(type.equals(PN_TYPE_PLAY) || type.equals(PN_TYPE_LAUNCH) || type.equals(PN_TYPE_INFO)) // These types require game launching through broadcast class
			pb.setContentIntent(PendingIntent.getBroadcast(context, notificationID, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		else
			pb.setContentIntent(PendingIntent.getActivity(context, notificationID, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		
		Notification notification = pb.build();
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notificationID, notification);

		SharedPreferences.Editor editor = settings.edit();
		if((type.equals(PN_TYPE_LAUNCH) || type.equals(PN_TYPE_INFO)) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) // LAUNCH and INFO eligible for stacking (API Level 11+)
		{
			editor.putString(PREF_PN_STACK_MSG.concat(Integer.toString(notificationID)).concat("_" + Integer.toString(stackCounter % MAX_NOTIFICATION_ON_BAR)), msg);
			editor.putInt(PREF_PN_STACK_ID.concat(Integer.toString(notificationID)), ++stackCounter % MAX_NOTIFICATION_ON_BAR);
		}
		else
			editor.putInt(PREF_NOTIFICATION_ID, ++notificationID % MAX_NOTIFICATION_ON_BAR);
		
		editor.commit();
	}
	
	public static void ShowAppDetailsSettings()
	{
		// From API Level 9 (Android 2.3)
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			try {
				//Open the specific App Info page:
				Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.setData(Uri.parse("package:" + STR_APP_PACKAGE));
				sContextReference.get().startActivity(intent);
				
			} catch ( ActivityNotFoundException e ) {
				//Open the generic Apps page:
				Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
				sContextReference.get().startActivity(intent);
			}
		}
		else
			WARN(TAG,"Feature available for API Level 9 (Android 2.3) and higher.");
	}

//////////////////////////////////////////////////////////////////////////////////////////
// Endpoint Management
//////////////////////////////////////////////////////////////////////////////////////////

	public static int GetDeviceToken(int transportID)
	{
		if (isEmptyOrNull(GetRegistrationID()))
		{
			if(SupportedTransportList[transportID].equals(TRANSPORT))
			{
			#if AMAZON_STORE
				requestADMToken();
			#else
				#if USE_NOKIA_API
				requestNokiaToken();
				#else
				requestGoogleToken();
				#endif
			#endif
			}
			else
			{
				ERR(TAG, "Invalid Transport!");
				return 1;
			}
		}
		else
		{
			nativeSendRegistrationData(GetRegistrationID());
			AddEndpointRecordToDB(GetRegistrationID());
		}
		return 0;
	}
	
	public static String GetRegistrationID()
	{
		String regId = null;
#if AMAZON_STORE
		if(isADMSupported())
		{
			final ADM mADM = new ADM(sContextReference.get());
			regId = mADM.getRegistrationId();
		}
#else
	#if USE_NOKIA_API
		if(isNokiaSupported())
			regId = PushRegistrar.getRegistrationId(sContextReference.get());
	#else
		#if ENABLE_OLD_GCM_API
		regId = GCMRegistrar.getRegistrationId(sContextReference.get());
		#else
		regId = getGCMRegistrationId(sContextReference.get());
		#endif
	#endif
#endif
		if ( !isEmptyOrNull(regId) ) 
		{
			DBG(TAG, "[Registration ID (Endpoint)]: " + regId);
			return regId;
		} else {
			ERR(TAG, "Registration ID (Endpoint) NOT found");
			return "";
		}
	}

#if AMAZON_STORE
	private static void requestADMToken()
    {
        INFO(TAG,"[requestADMToken]");
		if(!isADMSupported())
		{
			WARN(TAG,"ADM Token request cancelled");
			WARN(TAG,"ADM service is not available for this device");
			WARN(TAG,"Only local notification will be available.");
		}
		else
		{
			ADMManifest.checkManifestAuthoredProperly(sContextReference.get());
			DBG(TAG, "ADM Manifest permission: " + ADMManifest.getReceiverPermission(sContextReference.get()));
			
			String regId = GetRegistrationID();
			
			if ( isEmptyOrNull(regId) )
			{
				final ADM mADM = new ADM(sContextReference.get());
				mADM.startRegister();
				WARN(TAG, "Requesting Token to Amazon");
			}
			else 
			{
				setTokenReady();
				DBG(TAG,"Amazon Token Ready");
			}
		}
    }
	
	private static boolean isADMSupported()
	{
		boolean isSupported = false;
		try
		{
			Class myClass = Class.forName("com.amazon.device.messaging.ADM");
			Method mMethod = myClass.getMethod("isSupported", (Class[]) null);
			
			if(!isEmptyOrNull(mMethod.toString()))
			{
				final ADM mADM = new ADM(sContextReference.get());
				isSupported = mADM.isSupported();
				DBG(TAG,"ADM service supported? ["+isSupported+"]");
			}
			else
				WARN(TAG,"ADM Not supported for this device!!");
				
			return (isSupported);
		}
		catch(Exception ex ) 
		{
			ERR(TAG,"Error invoking ADM library: [" + ex.getMessage() + "]");
			ERR(TAG,"ADM Not supported for this device!!");
			return (isSupported);
		}
	}
#else
	#if USE_NOKIA_API
	private static void requestNokiaToken()
	{
		INFO(TAG,"[requestNokiaToken]");
		if(!isNokiaSupported())
		{
			WARN(TAG,"Nokia Token request cancelled");
			WARN(TAG,"Nokia service is not available for this device");
			WARN(TAG,"Only local notification will be available.");
		}
		else
		{
			String regId = GetRegistrationID();
			
			if (isEmptyOrNull(regId))
			{
				WARN(TAG,"[Requesting Nokia Token...]");
				PushRegistrar.register(sContextReference.get(), SENDER_ID);
			} 
			else 
			{
				setTokenReady();
				DBG(TAG,"Nokia Token Ready!");
			}
		}
	}
	
	private static boolean isNokiaSupported()
	{
		PackageManager packageManager = sContextReference.get().getPackageManager();
		try{packageManager.getPackageInfo("com.nokia.pushnotifications.service", 0);}
		catch(Exception ex) 
		{
			ERR(TAG,"Error invoking Nokia library: [" + ex.getMessage() + "]");
			ERR(TAG,"Nokia API not supported for this device!!");
			return (false);
		}
		return(true);
	}
	#else
	private static void requestGoogleToken()
	{
		INFO(TAG,"[requestGoogleToken]");
		
		if(mIsFirmwareBefore22)
		{
			WARN(TAG,"Google Message Service only available for API level 8 (FROYO) and higher.");
			WARN(TAG,"Only local notification will be available.");
		}
		else 
		{
			String regId = GetRegistrationID();
			
			if ( isEmptyOrNull(regId) )
			{
				WARN(TAG,"[Requesting GCM Token...]");
			#if ENABLE_OLD_GCM_API
				GCMRegistrar.register(sContextReference.get(), SENDER_ID);
			#else
				if(mThis == null) {mThis = new SimplifiedAndroidUtils();}
				mThis.RequestGCMToken();
			#endif
			} 
			else 
			{
				setTokenReady();
				DBG(TAG,"GCM Token Ready!");
			}
		}
	}

	private void RequestGCMToken()
	{
		INFO(TAG, "[RequestGCMToken]");
		new AsyncRequestGCMToken().execute(new Bundle());
	}
	
	//Request GCM Token Async
	class AsyncRequestGCMToken extends AsyncTask
	{
		protected Integer doInBackground(Bundle params)
		{
			try 
			{
				if(gcmInstance == null) {gcmInstance = GoogleCloudMessaging.getInstance(sContextReference.get());}
				String regid = gcmInstance.register(SENDER_ID);
				setGCMRegistrationId(sContextReference.get(), regid);
			}
			catch(Exception ex){ERR(TAG,"GCM Task Error:" + ex);}
			return new Integer(0);
		}
	}
	
	public static void setGCMRegistrationId(Context context, String regId)
	{
		INFO(TAG, "[setGCMRegistrationId]");
		final SharedPreferences prefs = Prefs.get(context);
		int appVersion = getAppVersion(context);
		long expirationTime = System.currentTimeMillis() + REG_EXPIRY_TIME_MS;
		
		DBG(TAG, "App version: " + appVersion);
		DBG(TAG, "Expiry date: " + new Timestamp(expirationTime));
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_REG_ID, regId);
		editor.putInt(PREF_APP_VERSION, appVersion);
		editor.putLong(PREF_EXPIRATION_TIME, expirationTime);
		editor.commit();
	}
	
	private static String getGCMRegistrationId(Context context)
	{
		INFO(TAG, "[getGCMRegistrationId]");
		final SharedPreferences prefs = Prefs.get(context);
		String registrationId = prefs.getString(PREF_REG_ID, "");
		if (isEmptyOrNull(registrationId))
		{
			WARN(TAG, "RegistrationId not found.");
			return "";
		}
	
		// check if App was updated; if so, it must clear registration id to
		// avoid a race condition if GCM sends a message
		int registeredVersion = prefs.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion || isRegistrationExpired())
		{
			ERR(TAG, "App version changed or registrationId expired.");
			return "";
		}
		return registrationId;
	}
	
	private static int getAppVersion(Context context) 
	{
		try{
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		}catch(NameNotFoundException e){ERR(TAG, "getAppVersion error: " + e);return 0;}
	}

	private static boolean isRegistrationExpired() 
	{
		final SharedPreferences prefs = Prefs.get(sContextReference.get());
		long expirationTime = prefs.getLong(PREF_EXPIRATION_TIME, -1);
		return (System.currentTimeMillis() > expirationTime);
	}
	#endif //USE_NOKIA_API
#endif //AMAZON_STORE
   
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Messages Management
/////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	public static int SendMessage(Bundle dataBundle, String delay, int groupType)
	{
		INFO(TAG, "[SendMessage]");
		String response = null;
		int PNdelay = 0;

		if(dataBundle != null && !isEmptyOrNull(delay) && groupType >= 0)
		{		
			try {PNdelay = Integer.parseInt(delay);}catch(Exception e) {ERR(TAG, "Exception: "+ e); return 1;}
			
			if (PNdelay > 0 && PNdelay < LocalPushManager.ALARM_DELAY_LIMIT)
			{
				DBG(TAG, "[delay]: "+ LocalPushManager.SecondsToHours(PNdelay));
				
				String pnType = dataBundle.getString(KEY_TYPE);
				if(!isEmptyOrNull(pnType))
				{		
					if(pnType.equals(PN_TYPE_LAUNCH))
						groupType += PN_LAUNCH_GROUP_LOCAL;
					else if(pnType.equals(PN_TYPE_INFO))
						groupType += PN_INFO_GROUP_LOCAL;
				}
					
				dataBundle.putString(EXTRA_PN_GROUP_ID, Integer.toString(groupType)); // Adding header for stack control
				response = LocalPushManager.SetAlarm(dataBundle, PNdelay);
				DBG(TAG,"*** [Push ID]: "+ response);
				AddPNMessageIDToPreferences(response, Integer.toString(groupType));
				AddPNRecordToDB(dataBundle, Integer.toString(groupType), response);
			} else 	{ERR(TAG, "Alarm delay must be less than 24 days otherwise will be triggered immediately (OS limitation)");}
		} else 		{ERR(TAG, "Malformed PN data");}
		
		if(isEmptyOrNull(response))
			return 1;
		else
			return 0;
	}
	
	public static int DeleteMessageGroup(int groupType)
	{
		List<String> ls = null;		

		if(GetPNMessageIdForType(Integer.toString(PN_LAUNCH_GROUP_LOCAL + groupType)) != null)
		{
			ls = GetPNMessageIdForType(Integer.toString(PN_LAUNCH_GROUP_LOCAL + groupType));
			groupType += PN_LAUNCH_GROUP_LOCAL;
			WARN(TAG, "PN_LAUNCH_GROUP_LOCAL found");
		}
		else if(GetPNMessageIdForType(Integer.toString(PN_INFO_GROUP_LOCAL + groupType)) != null)
		{
			ls = GetPNMessageIdForType(Integer.toString(PN_INFO_GROUP_LOCAL + groupType));
			groupType += PN_INFO_GROUP_LOCAL;
			WARN(TAG, "PN_INFO_GROUP_LOCAL found");
		}
		else
			ls = GetPNMessageIdForType(Integer.toString(groupType));
		
		if(ls == null)
			return 1;
		
		for(String ID : ls)
		{
			try
			{
				if(!isEmptyOrNull(ID) && ID.startsWith(LocalPushManager.ALARM_ID_HEADER))
					LocalPushManager.CancelAlarm(ID);
				else
				{
					ERR(TAG, "Invalid ID!");
					return 1;
				}
			} catch (Exception e) {ERR(TAG, "Exception: " + e);return 1;}
		}
		
		ClearGroup(groupType);
		ClearStack(sContextReference.get(), groupType);
		
			if(mPushDBHandler != null)
			{
				mPushDBHandler.deletePNRecord(PushSQLiteHelper.COLUMN_GROUP, Integer.toString(groupType));
				BackupDatabase();
			}
		return 0;
	}
	
	private static void ClearStack(Context context, int groupID)
	{
		INFO(TAG, "[ClearStack]");
		if(context != null)
		{
			String SharedPrefKey = PREF_PN_STACK_ID.concat(Integer.toString(MAX_NOTIFICATION_ON_BAR + groupID));
			SharedPreferences prefs = Prefs.get(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(SharedPrefKey, 1);
			editor.commit();
			DBG(TAG, "groupID = " + groupID);
			DBG(TAG, "stackID = " + SharedPrefKey);
			ClearStackMessages(context, groupID);
		}
		else
		{
			ERR(TAG,"Invalid context!!");
		}
	}
	
	private static void ClearStackMessages(Context context, int groupID)
	{
		INFO(TAG, "[ClearStackMessages]");
		SharedPreferences prefs = Prefs.get(context);
		SharedPreferences.Editor editor = prefs.edit();
		
		for(int i = 0; i < MAX_NOTIFICATION_ON_BAR + 1; i++)
		{
			String SharedPrefKey = (PREF_PN_STACK_MSG.concat(Integer.toString(MAX_NOTIFICATION_ON_BAR + groupID))).concat("_" + Integer.toString(i + 1));
			editor.putString(SharedPrefKey, null);
		}
		editor.commit();
	}
	
	private static void ClearGroup(int groupType)
	{
		String SharedPrefKey = KeyGroupBaseString + Integer.toString(groupType);
		SharedPreferences prefs = Prefs.get(sContextReference.get());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SharedPrefKey, null);
		editor.commit();
		GetPNMessageIdForType(Integer.toString(groupType));
	}
	
	private static void AddPNMessageIDToPreferences(String messageID, String groupType)
	{
		List<String> ls = GetPNMessageIdForType(groupType);
		if(ls == null)
			ls = new ArrayList<String>();
		ls.add(messageID);
		
		String finalResult = "";
		for(String s : ls)
		{
			finalResult = finalResult + s + "|";
		}
		
		String SharedPrefKey = KeyGroupBaseString + groupType;
		SharedPreferences prefs = Prefs.get(sContextReference.get());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SharedPrefKey, finalResult);
		editor.commit();
	}
	
	private static List<String> GetPNMessageIdForType(String groupType)
	{
		String SharedPrefKey = KeyGroupBaseString + groupType;
		SharedPreferences prefs = Prefs.get(sContextReference.get());
		String fullMessageListForType = prefs.getString(SharedPrefKey, null);
		
		if(fullMessageListForType == null)
			return null;
		
		StringTokenizer st = new StringTokenizer(fullMessageListForType, "|");
		List<String> listString = new ArrayList<String>();
		while (st.hasMoreTokens()) 
		{
			String current = st.nextToken(); 
			listString.add(current);
		}
		return listString;
	}
	
	private static void AddPNRecordToDB(Bundle dataBundle, String groupType, String messageID)
	{
		String creation = dataBundle.getString(PushSQLiteHelper.COLUMN_CREATION);
		String schedule = dataBundle.getString(PushSQLiteHelper.COLUMN_SCHEDULE);
		String type 	= dataBundle.getString(KEY_TYPE);
		String content 	= dataBundle.getString(KEY_BODY);
		
		if(mPushDBHandler == null)
		{
			mPushDBHandler = new PushDBHandler(sContextReference.get());
			mPushDBHandler.open();
		}
		
		if(mPushDBHandler != null && !isEmptyOrNull(creation) && !isEmptyOrNull(schedule) && !isEmptyOrNull(groupType) && !isEmptyOrNull(type) && !isEmptyOrNull(content) && !isEmptyOrNull(messageID))
		{
			ContentValues dbValues = new ContentValues();
			dbValues.put(PushSQLiteHelper.COLUMN_PNID,		messageID);		DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_PNID+"]: "		+messageID);
			dbValues.put(PushSQLiteHelper.COLUMN_CREATION,	creation);		DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_CREATION+"]: "	+creation);
			dbValues.put(PushSQLiteHelper.COLUMN_SCHEDULE,	schedule);		DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_SCHEDULE+"]: "	+schedule);
			dbValues.put(PushSQLiteHelper.COLUMN_GROUP,		groupType);		DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_GROUP+"]: "		+groupType);
			dbValues.put(PushSQLiteHelper.COLUMN_TYPE,		type);			DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_TYPE+"]: "		+type);
			dbValues.put(PushSQLiteHelper.COLUMN_CONTENT, 	content);		DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_CONTENT+"]: "	+content);
			
			mPushDBHandler.createPNRecord(dbValues);
			BackupDatabase();
		}
		else
			ERR(TAG, "Unable to create DB record, Invalid information!");
	}
	
	public static void AddEndpointRecordToDB(String endpoint)
	{	
		if(mPushDBHandler == null)
		{
			mPushDBHandler = new PushDBHandler(sContextReference.get());
			mPushDBHandler.open();
		}
		
		if(mPushDBHandler != null && !isEmptyOrNull(endpoint))
		{
			ContentValues dbValues = new ContentValues();
			dbValues.put(PushSQLiteHelper.COLUMN_ID, "1");
		#if AMAZON_STORE
			dbValues.put(PushSQLiteHelper.COLUMN_TRANSPORT,	"adm");		DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_TRANSPORT+"]: adm");
		#else
			#if USE_NOKIA_API
			dbValues.put(PushSQLiteHelper.COLUMN_TRANSPORT,	"nnapi");	DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_TRANSPORT+"]: nnapi");
			#else
			dbValues.put(PushSQLiteHelper.COLUMN_TRANSPORT,	"gcm");		DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_TRANSPORT+"]: gcm");
			#endif
		#endif
			dbValues.put(PushSQLiteHelper.COLUMN_REGID,		endpoint);	DBG(TAG,"*** PNdb["+PushSQLiteHelper.COLUMN_REGID+"]: "	+endpoint);
			mPushDBHandler.updateEndpointRecord(dbValues);
			BackupDatabase();
		}
		else
			ERR(TAG, "Unable to create DB record, Invalid information!");
	}
	
	public static void BackupDatabase()
    {
		try
		{
			DBG(TAG, "SD Folder: " + getSDFolder());
			File dataFolder = new File(getSDFolder());
			
			if(!dataFolder.exists())
				dataFolder.mkdirs();
				
			if(dataFolder.exists())
			{
				String databasePath = SAVE_FOLDER + "/databases/" + PushSQLiteHelper.DATABASE_NAME;
				DBG(TAG, "DataBase path: " + databasePath);
				File dbFile = new File(databasePath);
				if(dbFile.exists())
				{
					FileInputStream fis = new FileInputStream(dbFile);
					
					//Open the empty db as the output stream
					OutputStream output = new FileOutputStream(getSDFolder() + "/" + PushSQLiteHelper.DATABASE_NAME);
					
					//transfer bytes from the inputfile to the outputfile
					byte[] buffer = new byte[1024];
					int length;
					
					while ((length = fis.read(buffer))>0)
					{
						output.write(buffer, 0, length);
					}

					output.flush();
					output.close();
					fis.close();
				}
				else
					ERR(TAG, "DataBase file not found!");
			}
			else
				ERR(TAG, "APP_DATA_FOLDER not found!");
				
		}catch(IOException IOE){DBG_EXCEPTION(IOE);}
    }
	
	private static String getSDFolder()
	{
#if USE_INSTALLER
		String result = SUtils.getPreferenceString("SDFolder",GameInstaller.mPreferencesName);
		if (result != "")
			return result;
		else
#endif
			return SD_FOLDER;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Enable/Disable Push Notifications
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void SetEnable(boolean  enable) 
	{
        SetEnable(sContextReference.get(), enable);
	}
	
	public static void SetEnable(Context context, boolean enable) 
	{
		Prefs.setEnabled(context, enable);
	}
	
	public static boolean IsEnable() 
	{
		return Prefs.isEnabled(sContextReference.get());
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Dont Disturb
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void SetDontDisturbEnable(boolean enabled)
	{
		DontDisturbPolicy.setDontDisturbEnable(sContextReference.get(), enabled);
	}
	
	public static boolean IsDontDisturbEnable()
	{
		return DontDisturbPolicy.isDontDisturbEnable(sContextReference.get());
	}
	
	static boolean IsDontDisturbeTime()
	{
		return DontDisturbPolicy.isDontDisturbeTime(sContextReference.get());
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// get values and status
/////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	private static boolean isEmptyOrNull(String value)
	{
		return (value == null) || (value.length() == 0);
	}
	
	static String encodeString(String data)
	{
		try {data = URLEncoder.encode(data, "UTF-8");}
		catch (Exception e) {DBG_EXCEPTION(e);}
		return data;
	}
	
	public static String decodeString(String data)
	{
		try {data = URLDecoder.decode(data, "UTF-8");}
		catch (Exception e) { DBG_EXCEPTION(e);}
		return data;
	}
	
//////////////////////////////////////////////////////////////////////////////////////////
// natives
//////////////////////////////////////////////////////////////////////////////////////////

	public native static void nativeInit();
	public native static void nativeSendPNData(String data);
	public native static void nativeSendRegistrationData(String data);
}
#endif
