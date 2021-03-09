#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.util.Log;

import android.app.Activity;
import APP_PACKAGE.GLUtils.SUtils;
import android.telephony.TelephonyManager;
import android.content.Context;


public class KTHelper extends IABTransaction 
{
	SET_TAG("InAppBilling");
    public KTHelper(ServerInfo si) {
        super(si);
    }
	static boolean result;
	static boolean finishLaunch;
    @Override
    public boolean requestTransaction(final String id) {
		final String appId = mServerInfo.getAIDBilling();
        final String productId = mServerInfo.getPIDBilling();
		result = true;
		finishLaunch = false;
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				result = KtIabActivity.LaunchKTBilling(id, appId, productId, mServerInfo);
				finishLaunch = true;
			}
		});	
		int stepts = 10000;
		while (!finishLaunch && --stepts > 0)
		{
			try {
					Thread.sleep(1);
			} catch (Exception exc) {}
		}
		return (result && stepts > 0);
    }

    @Override
    public void showDialog(int idx, String id) {}
	
	@Override
    public boolean restoreTransactions()
	{
		INFO(TAG, "Restore Transactions started");
		final String appId = mServerInfo.getAIDBilling();
        final String productId = mServerInfo.getPIDBilling();
		
		if(appId != null && productId != null && IsValidSIM())
		{
			((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
			{
				public void run ()
				{
					KtIabActivity.LaunchKTRestore(null, appId, productId, mServerInfo);
				}
			});
		}
		else
		{
			if(!IsValidSIM())
			{
				ERR(TAG, "***** Restore Transactions Failed: SIM card is missing *****");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "***** Restore Transactions Failed: SIM card is missing *****");
			}	
			else
			{
				ERR(TAG, "***** Restore Transactions Failed: Profile is missing *****");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "***** Restore Transactions Failed: Profile is missing *****");
			}	
		}
		return true;
	}
	
	private boolean IsValidSIM()
	{
		TelephonyManager mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);

			if(mDeviceInfo.getPhoneType() == mDeviceInfo.PHONE_TYPE_CDMA)
				return (true);
			else
				return (mDeviceInfo.getSimState() == mDeviceInfo.SIM_STATE_READY);		
	}

}
#endif
