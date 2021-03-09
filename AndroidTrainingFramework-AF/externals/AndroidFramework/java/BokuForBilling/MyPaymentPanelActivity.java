#if USE_BILLING && USE_BOKU_FOR_BILLING
package APP_PACKAGE.billing;

import android.os.Bundle;
import com.boku.mobile.android.PaymentPanelActivity;
import android.content.Intent;


public class MyPaymentPanelActivity extends PaymentPanelActivity
{
	public MyPaymentPanelActivity()
	{
		super();
	}
	
	@Override
    public void onCreate(Bundle bundle) {
		if(APP_PACKAGE.CLASS_NAME.m_sInstance == null) 		
		{
			Intent intent = new Intent(this, APP_PACKAGE.CLASS_NAME.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		}
		super.onCreate(bundle);
		// this.runOnUiThread(new Runnable ()
		// {
			// public void run ()
			// {
				// setContentView(R.layout.boku);
			// }
		// });
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		// this.runOnUiThread(new Runnable ()
		// {
			// public void run ()
			// {
				// setContentView(R.layout.boku);
			// }
		// });
	}
}
#endif //USE_BILLING && USE_BOKU_FOR_BILLING
