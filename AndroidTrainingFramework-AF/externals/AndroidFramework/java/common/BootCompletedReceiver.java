#if (USE_MARKET_INSTALLER || GOOGLE_STORE_V3)
package APP_PACKAGE;

import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Encrypter;
import APP_PACKAGE.GLUtils.DefReader;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import android.text.TextUtils;

public class BootCompletedReceiver extends BroadcastReceiver 
{
	SET_TAG("BootCompleted");
	
	@Override
	public void onReceive(Context context, Intent intent) {
		DBG(TAG, "onReceive");

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			DBG(TAG, "onReceive ACTION_BOOT_COMPLETED");
			try{
				if(SUtils.getContext() == null)
					SUtils.setContext(context);
				//try to read the IGP and serialKey at boot
				SUtils.getInjectedIGP();
				SUtils.getInjectedSerialKey();
		
				SUtils.release(context);
			}catch(Exception e) {
				ERR(TAG, "Error saving injected IGP and SerialKey"+e);
			}
		}
	}
}
#endif