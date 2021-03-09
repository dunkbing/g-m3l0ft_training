package APP_PACKAGE.PackageUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class UtilsNetworkStateReceiver extends BroadcastReceiver 
{
    /***********Internet Type Constants******************/
    public final static int NO_CONNECTIVITY 			= 0;
	public final static int CONNECTIVITY_WIFI 			= 1;
	public final static int CONNECTIVITY_BLUETOOTH		= 2;
	public final static int CONNECTIVITY_DUMMY 			= 3;
	public final static int CONNECTIVITY_ETHERNET 		= 4;
	public final static int CONNECTIVITY_WIMAX 			= 5;
	public final static int CONNECTIVITY_2G 			= 6;
	public final static int CONNECTIVITY_3G 			= 7;
	public final static int CONNECTIVITY_4G 			= 8;
	public final static int CONNECTIVITY_UNKNOWN		= 9;
    /***********End of Internet Type Constants**************/

	@Override
    public void onReceive(Context context, Intent intent) 
    {
	    JNIBridge.SetConnectionType(CheckConnectionType());//set the connection type even if is not connected (NO_CONNECTIVITY)
	}
	
	/**
	 * Method to check the connection type currently available.
	 * Used by this class to send the information
	 * regarding connection to the native side of the code.
	 */
	public static int CheckConnectionType()
    {
		Context ctx = AndroidUtils.GetContext();
		if(ctx == null || hasConnectivity() == 0)
		{
			return NO_CONNECTIVITY;
		}
		int connectionType = CONNECTIVITY_UNKNOWN;
        
		ConnectivityManager mConnectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetInfo = mConnectivityManager.getActiveNetworkInfo();
		int networkType = mNetInfo.getType();
		
		switch (networkType) 
		{
			case ConnectivityManager.TYPE_BLUETOOTH:
			{
				connectionType = CONNECTIVITY_BLUETOOTH;
				break;
			}
			case ConnectivityManager.TYPE_DUMMY:
			{
				connectionType = CONNECTIVITY_DUMMY;
				break;
			}
			case ConnectivityManager.TYPE_ETHERNET:
			{
				connectionType = CONNECTIVITY_ETHERNET;
				break;
			}
			case ConnectivityManager.TYPE_WIFI:
			{
				connectionType = CONNECTIVITY_WIFI;
				break;
			}
			case ConnectivityManager.TYPE_WIMAX:
			{
				connectionType = CONNECTIVITY_WIMAX;
				break;
			}
		}
		
		if( connectionType == CONNECTIVITY_UNKNOWN )
		{
			TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			int mobileNetworkType = mTelephonyManager.getNetworkType();
			
			switch (mobileNetworkType) 
			{
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
				{
					connectionType = CONNECTIVITY_2G;
					break;
				}
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
				{
					connectionType = CONNECTIVITY_3G;
					break;
				}
				case TelephonyManager.NETWORK_TYPE_LTE:
				{
					connectionType = CONNECTIVITY_4G;
					break;
				}
			}
		}
        if(connectionType == CONNECTIVITY_DUMMY)
		{
			connectionType = CONNECTIVITY_UNKNOWN;//use connection Unknown for Dummy.
		}
        return connectionType;
    }
	
	/**
	 * Function used to check if the current activity has 
	 * a connection available.
	 */
	public static int hasConnectivity()
    {
		Context ctx = AndroidUtils.GetContext();
        ConnectivityManager mConnectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mConnectivityManager == null)
            return 0;
			
        NetworkInfo mNetInfo = mConnectivityManager.getActiveNetworkInfo();
        if(mNetInfo == null)
            return 0;
				
        return mNetInfo.isConnected() ? 1 : 0;
    }
}
