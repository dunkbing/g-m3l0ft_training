#if USE_IN_APP_BILLING && VZW_STORE
package APP_PACKAGE.iab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Dialog;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.Button;
import java.lang.reflect.Method;

import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.iab.VZWHelper;
import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.R;
#if VZW_MTX
import APP_PACKAGE.iab.MTXTransaction;
#endif

public class VZWIABActivity extends Activity implements OnClickListener
{
	//get encrypted string
	#define GES(id)		SUtils.getContext().getString(id)

	SET_TAG("InAppBilling");
	private static AServerInfo mServerInfo = null;

	// Labels for our UI display
	private TextView itemLabel;
	private TextView descriptionLabel;
	private TextView totalpriceLabel;
	
#if VZW_MTX
	private static final String VZW_BILLING_METHOD = "verizon_microtx";
#else
	private static final String VZW_BILLING_METHOD = "verizon_scm";
#endif

	Button okButton;
	Button buyButton;

	private boolean errorOcurred = false;
	//private boolean purchaseInProgrese = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		StartVZWBilling();
	}

	@Override
	protected void onStart() {
		DBG(TAG,"onStart");
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	}
	
	@Override
	protected void onDestroy() {
		DBG(TAG,"onDestroy");		
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		DBG(TAG,"onPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		DBG(TAG,"onResume");
		super.onResume();
	}

	@Override
    protected void onStop() {
		DBG(TAG,"onStop");
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
    }

	@Override
	protected void onRestart()
	{
		DBG(TAG,"onRestart");
		super.onRestart();
	}
	
	public boolean mhasFocus = false;
	public void onWindowFocusChanged(boolean hasFocus)
	{
		mhasFocus = hasFocus;
		DBG(TAG,"onWindowFocusChanged "+hasFocus);
		super.onWindowFocusChanged(hasFocus);
	}

	static boolean LaunchVZWBilling(AServerInfo si) 
	{
		INFO(TAG, "[LaunchVZWBilling]");
		INFO(TAG, "ContentId: "+VZWHelper.GetContentId());
		INFO(TAG, "ItemId: "+VZWHelper.GetItemId());
		mServerInfo = si;
		Intent i = new Intent();
		String packageName = SUtils.getContext().getPackageName();
		i.setClassName(packageName,  InAppBilling.GET_FQCN(IAB_CLASS_NAME_VZW_IAB_ACTIVITY));
		VZWHelper.getServerInfo().setBillingMethod(VZW_BILLING_METHOD);
		SUtils.getContext().startActivity(i);
		return true;
	}

	public boolean StartVZWBilling()
	{
		try 
		{
			INFO(TAG, "[StartVZWBilling]");
			requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
			setContentView(R.layout.iab_dialog_vzwpayment);

			// Populate the current view
			itemLabel = (TextView)findViewById(R.id.txtVItemInfo);
			itemLabel.setText(VZWHelper.GetItemName());

			descriptionLabel = (TextView) findViewById(R.id.txtVItemDescription);
			descriptionLabel.setText("- " + VZWHelper.GetItemDescription());

			totalpriceLabel = (TextView) findViewById(R.id.txtVPriceTotal);
			totalpriceLabel.setText(VZWHelper.GetItemPrice());

			buyButton = (Button) findViewById(R.id.BuyButton);
			buyButton.setOnClickListener(this);
		} catch (Exception e) 
		{
			ERR(TAG, "Error "+e.getMessage());
			return false;//TODO: put error code here
		}
		
		INFO(TAG, " END StartVZWBilling");
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		INFO(TAG, " --------- [onKeyDown]");
		super.onKeyDown(keyCode, event);
		switch(keyCode) {
			case KeyEvent.KEYCODE_BACK:
				WARN(TAG, " --------- [KEYCODE_BACK]");
				VZWHelper.IABResultCallBack(IAB_BUY_CANCEL);
			return true;
		} return false;
    }
	
	@Override
	public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
		android.graphics.Rect dialogBounds = new android.graphics.Rect();
		getWindow().getDecorView().getHitRect(dialogBounds);
		if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
			//touch outside dialog
			return false;
		}
		else
			return super.dispatchTouchEvent(ev);
	}
	
	public void onClick(View v) {
		if(v == buyButton) {
			INFO(TAG, " --------- [onClick]");
			finish();
			performPurchaseRequest();
		}
	}

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
		}
	};

	private void performPurchaseRequest()
	{
		DBG(TAG, "[performPurchaseRequest]");
	#if VZW_MTX
		Intent purchaseContentIntent = new Intent(VZWIABActivity.this, MTXTransaction.class);
		startActivity(purchaseContentIntent);
	#else
		VZWHelper.ProcessTransactionSCM();
	#endif
						
	}
													
	private static boolean mIsConfirmInProgress = false;
	
	static void sendConfirmation()
	{
		DBG(TAG,"[sendConfirmation]");
		
		if (mServerInfo == null)
			mServerInfo = VZWHelper.getServerInfo();
		if (mIsConfirmInProgress)
			return;
		
		new Thread( new Runnable()
		{
			public void run()
			{
				Looper.prepare();
				boolean finish = false;
				mIsConfirmInProgress = true;
				do
				{
					try
					{
						INFO(TAG,"SendIABNotification for: InAppBilling.mCharId: " + InAppBilling.mCharId + " InAppBilling.mCharRegion: " + InAppBilling.mCharRegion);
						XPlayer mXplayer = new XPlayer(new Device(mServerInfo));
						mXplayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
						mXplayer.sendIABNotification(0, InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME), InAppBilling.mOrderID);
						
						long time = 0;
						while (!mXplayer.handleIABNotification())
						{
							Thread.sleep(50);
							if((System.currentTimeMillis() - time) > 1500)
							{
								DBG(TAG, "[sendIABNotification]Waiting for response");
								time = System.currentTimeMillis();
							}
						}
						
						if(XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
						{
							finish = true;
							mIsConfirmInProgress = false;
						}
					}
					catch(Exception exd)
					{
						DBG_EXCEPTION(exd);
						DBG(TAG,"No internet avaliable");
					}
				}
				while(!finish);
				Looper.loop();
			}
		}
	#if !RELEASE_VERSION
		,"Thread-Confirmation"
	#endif
		).start();
	}
}
#endif //#if USE_IN_APP_BILLING && VZW_STORE
