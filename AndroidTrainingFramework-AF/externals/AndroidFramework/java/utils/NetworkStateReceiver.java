package APP_PACKAGE.GLUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
// import com.gameloft.glf.GL2JNILib;

public class NetworkStateReceiver extends BroadcastReceiver {

	SET_TAG("NetworkStateReceiver");
	public static boolean androidOSInitialized = false;
	// public static final int CONNECTIVITY_OFF = 0;
	// public static final int CONNECTIVITY_ON = 1;
	
	@Override
    public void onReceive(Context context, Intent intent) {
		DBG(TAG,"Network connectivity change");
		// if(intent.getExtras()!=null) {
			// NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			// if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
				// DBG(TAG,"Network "+ni.getTypeName()+" connected");
				// if(androidOSInitialized)
				// {
					// setNetworkState(CONNECTIVITY_ON);
				// }
			// }
		// }
		// if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
			// DBG(TAG,"There's no network connectivity");
			// if(androidOSInitialized)
			// {
				// setNetworkState(CONNECTIVITY_OFF);
			// }
		// }
		//the network status has changed, check in SUtils if we have connection and the type of the current connection
		if(androidOSInitialized)
			setConnectionType(SUtils.CheckConnectionType());//set the connection type even if is not connected (NO_CONNECTIVITY)
	}
	//public static native void setNetworkState(int state);
	public static native void setConnectionType(int connectionType);
}
