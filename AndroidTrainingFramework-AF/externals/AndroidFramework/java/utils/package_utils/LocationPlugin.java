package APP_PACKAGE.PackageUtils;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.util.Log;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import APP_PACKAGE.PackageUtils.JNIBridge;
import APP_PACKAGE.GLUtils.SUtils;


public class LocationPlugin 
	implements APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver
{
	private static Activity	m_MainActivityRef = null;	
	
	public void onPluginStart(Activity activity, ViewGroup viewGroup)
	{
		m_MainActivityRef = activity;
	}
	
	public void onPreNativePause() 
	{ 
		unregisterLocationListener();
	}
	
	public void onPostNativePause() { }
	
	public void onPreNativeResume() { }
	
	public void onPostNativeResume() 
	{ 
		registerLocationListener();
	}
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	   return false; 
	}
		
	
	public final static int USER_LOCATION_UNINITIALIZED			= -1;
	public final static int USER_LOCATION_ENABLED				= 0;
	public final static int USER_LOCATION_DISABLED 				= 1;
	public final static int USER_LOCATION_ERROR 				= 2;
	
	private static int      UserLocationStatus					= USER_LOCATION_UNINITIALIZED;
	private static double	UserLocationLatitude				= 0;
	private static double   UserLocationLongitude				= 0;
	private static float    UserLocationAccuracy				= 0;
	private static String 	UserLocationTime					= "0";

	
	public static int getUserLocationStatus()
	{
		if(UserLocationStatus == USER_LOCATION_UNINITIALIZED)
			initUserLocation();
		return UserLocationStatus;
	}
	
	public static double getUserLocationLatitude()
	{
		return UserLocationLatitude;
	}
	
	public static double getUserLocationLongitude()
	{
		return UserLocationLongitude;
	}
		
	public static float getUserLocationAccuracy()
	{
		return UserLocationAccuracy;
	}
	
	public static String getUserLocationTime()
	{
		return UserLocationTime;
	}

	public static void resetLocationValues()
	{
		UserLocationLatitude				= 0;
		UserLocationLongitude				= 0;
		UserLocationAccuracy				= 0;
		UserLocationTime					= "0";
	}

	private static LocationListener locationListener = null;
	private static LocationManager mgr = null;
	private static boolean isListenerRegistered = false;
	private static boolean isListenerEnabled = false;
	
	public static void EnableUserLocation()
	{
		DBG("Location", "Enable User Location");
		isListenerEnabled = true;
		m_MainActivityRef.runOnUiThread(new Runnable() {
			public void run() 
			{
				initUserLocation();
			}
		});
	}
	
	public static void DisableUserLocation()
	{
		DBG("Location", "Disable User Location");
		unregisterLocationListener();
		isListenerEnabled = false;
		resetLocationValues();
		UserLocationStatus = USER_LOCATION_UNINITIALIZED;
		JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
	}
	
	public static void registerLocationListener()
	{
		if(isListenerEnabled)
		{
			if(mgr != null && locationListener != null && !isListenerRegistered)
			{
				DBG("Location", "Registering Location Listener");
				mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300 * 1000, 0, locationListener);
				isListenerRegistered = true;
			}
		}
		else
		{
			DBG("Location", "USER LOCATION IS DISABLED");
		}
	}
	
	public static void unregisterLocationListener()
	{
		if(isListenerEnabled)
		{
			if(mgr != null && locationListener != null && isListenerRegistered)
			{
				DBG("Location", "Unregistering Location Listener");
				mgr.removeUpdates(locationListener);
				isListenerRegistered = false;
			}
		}
		else
		{
			DBG("Location", "USER LOCATION IS DISABLED");
		}
	}
	
	public static String initUserLocation()//return "lat, long"
	{
		if(isListenerEnabled)
		{
			if(UserLocationStatus == USER_LOCATION_UNINITIALIZED)
			{
				try{
					mgr = (LocationManager) SUtils.getContext().getSystemService(Context.LOCATION_SERVICE);
					if(mgr != null)
					{
						//register the listener and keep listening forever
						locationListener = new LocationListener() {
							public void onLocationChanged(Location location) 
							{
								DBG("Location", "onLocationChanged");
								UserLocationStatus = USER_LOCATION_ENABLED;
								if(location != null)
								{
									UserLocationLatitude = location.getLatitude();
									UserLocationLongitude = location.getLongitude();
									UserLocationAccuracy = location.getAccuracy();
									UserLocationTime = String.valueOf((System.currentTimeMillis() - location.getTime()));

									DBG("Location", "location.getLatitude() = " + location.getLatitude());
									DBG("Location", "location.getLongitude() = " + location.getLongitude());
									DBG("Location", "location.getAccuracy() = " + location.getAccuracy());
								}
								else
								{
									resetLocationValues();
								}
								JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
							}

							public void onStatusChanged(String provider, int status, android.os.Bundle extras) 
							{
								DBG("Location", "onStatusChanged");
								if(status ==  	android.location.LocationProvider.OUT_OF_SERVICE || status ==  	android.location.LocationProvider.TEMPORARILY_UNAVAILABLE)
								{
									UserLocationStatus = USER_LOCATION_ERROR;
									resetLocationValues();
								}
								else
								{
									UserLocationStatus = USER_LOCATION_ENABLED;
								}
								JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
							}

							public void onProviderEnabled(String provider) 
							{
								DBG("Location", "onProviderEnabled");
								UserLocationStatus = USER_LOCATION_ENABLED;
								Location coarseLocation = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
								if (coarseLocation != null)
								{
									UserLocationLatitude	= coarseLocation.getLatitude();
									UserLocationLongitude	= coarseLocation.getLongitude();
									UserLocationAccuracy	= coarseLocation.getAccuracy();
									UserLocationTime		= String.valueOf(System.currentTimeMillis() - coarseLocation.getTime());
								
									DBG("Location", "last known location getLatitude() = " + UserLocationLatitude);
									DBG("Location", "last known location getLongitude() = " + UserLocationLongitude);
									DBG("Location", "last known location getAccuracy() = " + UserLocationAccuracy);
									DBG("Location", "last known location getTime() = " + UserLocationTime);
								}
								else
								{
									resetLocationValues();
								}
								JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
							}

							public void onProviderDisabled(String provider) 
							{
								DBG("Location", "onProviderDisabled");
								UserLocationStatus = USER_LOCATION_DISABLED;
								resetLocationValues();
								JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
							}
						};
											
						registerLocationListener();
						
						boolean network_enabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
						if(network_enabled)
						{
							DBG("Location", "LocationManager Provider is enabled");
							UserLocationStatus = USER_LOCATION_ENABLED;
							Location coarseLocation = mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
							if( coarseLocation != null )
							{
								UserLocationLatitude	= coarseLocation.getLatitude();
								UserLocationLongitude	= coarseLocation.getLongitude();
								UserLocationAccuracy	= coarseLocation.getAccuracy();
								UserLocationTime		= String.valueOf(System.currentTimeMillis() - coarseLocation.getTime());
							
								DBG("Location", "last known location getLatitude() = " + UserLocationLatitude);
								DBG("Location", "last known location getLongitude() = " + UserLocationLongitude);
								DBG("Location", "last known location getAccuracy() = " + UserLocationAccuracy);
								DBG("Location", "last known location getTime() = " + UserLocationTime);
							}
							else
							{
								resetLocationValues();
							}
							JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
						}
						else
						{
							DBG("Location", "LocationManager Provider is disabled");
							UserLocationStatus = USER_LOCATION_DISABLED;
							resetLocationValues();
							JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
						}
					}
					else
					{
						DBG("Location", "LocationManager Provider is disabled");
						UserLocationStatus = USER_LOCATION_ERROR;
						resetLocationValues();
						JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
					}
				}
				catch(Exception e)
				{
					DBG("Location", "LocationManager error = " + e);
					UserLocationStatus = USER_LOCATION_ERROR;
					resetLocationValues();
					JNIBridge.SetUserLocation(UserLocationStatus, UserLocationLatitude, UserLocationLongitude, UserLocationAccuracy, UserLocationTime);
				}
			}
		}
		else
		{
			DBG("Location", "USER LOCATION IS DISABLED");
		}
		return UserLocationLatitude + ", " + UserLocationLongitude;
	}
	
	
}
