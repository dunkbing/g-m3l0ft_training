#if SIMPLIFIED_PN
package APP_PACKAGE.PushNotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushDeleteReceiver extends BroadcastReceiver {
	SET_TAG("PushNotification");

	@Override
	public final void onReceive(Context context, Intent intent)
	{
		INFO(TAG, "onReceive");
		PN_UTILS_CLASS.LaunchPNClearStack(context, intent);
	}
}
#endif