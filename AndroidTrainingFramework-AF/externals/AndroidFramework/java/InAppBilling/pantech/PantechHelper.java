#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.util.Log;

import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

import android.app.Dialog;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.os.Bundle;
import java.lang.reflect.Method;

public class PantechHelper extends IABTransaction 
{
	SET_TAG("InAppBilling");
  private static String mCurrentId = null;
    public PantechHelper(ServerInfo si) {
        super(si);
    }
	static boolean result;
	static boolean finishLaunch;
    @Override
    public boolean requestTransaction(final String id) {
		DBG(TAG, "requestTransaction");
        mCurrentId = id;
        final String IId = mServerInfo.getIIDBilling();
		result = true;
		finishLaunch = false;
		((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				result = PantechIabActivity.LaunchPantechBilling(id, IId, mServerInfo);
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
}
#endif
