#if !USE_IN_APP_BILLING_CRM

package APP_PACKAGE.iab;


//OS
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;


public class InAppBillingLegacyPlugin 
	implements APP_PACKAGE.PackageUtils.PluginSystem.IPluginEventReceiver
{

	public void onPluginStart(Activity mainActivity, ViewGroup viewGroup)
	{
		APP_PACKAGE.iab.InAppBilling.init((Context)mainActivity);
	}
	
	
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		return false;
	}
	
	public void onPreNativePause()
	{
	}

	public void onPostNativePause()
	{
	}

	public void onPreNativeResume()
	{
		
	}
	
	public void onPostNativeResume()
	{
	}    
}

#endif //!USE_IN_APP_BILLING_CRM
