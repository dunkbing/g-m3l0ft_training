#if USE_IN_APP_BILLING

package APP_PACKAGE.iab;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.app.ProgressDialog;


import java.lang.reflect.Method;
import java.util.Locale;
import java.lang.Thread;
import java.util.ArrayList;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;


public final class SktIabActivity extends FragmentActivity{

	SET_TAG("InAppBilling");

    static String appID = "";
    static String productID = "";
    private static String uid = "";
	public static String signData;
	private static AServerInfo mServerInfo = null;

    // Not needed at the moment
    private static String serverIP = "";
    private static int serverPort = -1;

    private boolean purchaseInProgrese = false;
    private boolean init = false;
    private boolean errorOcurred = false;
	private boolean needTofinishinAfterInteraction = false;
	private static boolean requestRestoreTransaction = false;
	
	#if TEST_SKT_STORE
	public static final String library_server = "development";
	#else
	public static final String library_server = "release";
	#endif
	
	static ProgressDialog pDialog;

    public static boolean LaunchSKTBilling(String item, String appId, String productId, AServerInfo si) {
        SktIabActivity.uid = item;
        SktIabActivity.appID = appId;
        SktIabActivity.productID = productId;
		SktIabActivity.mServerInfo = si;
        //SktIabActivity.productID = "0900159881";
		
		try {
            Intent i = new Intent();
            String packageName = ((Activity)SUtils.getContext()).getPackageName();
            i.setClassName(packageName, packageName + ".iab.SktIabActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			((Activity)SUtils.getContext()).startActivity(i);
        } catch (Exception e) {
            return false;//TODO: put error code here
        }
        return true;
    }
	
	public static boolean LaunchSKTRestore(String item, String appId, String productId, AServerInfo si) {
		requestRestoreTransaction = true;
		LaunchSKTBilling(item, appId, productId, si);
        return true;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		DBG(TAG, "onCreate["+library_server+"]");
        setupBilling();
	}

    @Override
    protected void onResume() {
        super.onResume();
		DBG(TAG, "onResume");

		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!init) {
					init = true;
						if(requestRestoreTransaction)
						{
							performRestoreTransactions();
						}
						else
							performPurchaseRequest();
				}
			}
			});
    }

		@Override
	protected void onPause() {
		super.onPause();
		DBG(TAG, "onPause");
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		DBG(TAG, "onStart");
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
		DBG(TAG, "onStop");
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();		
		DBG(TAG, "onRestart");
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		requestRestoreTransaction = false;
		DBG(TAG, "onDestroy");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	DBG(TAG,"onConfigurationChanged Landscape "+ newConfig.orientation);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onConfigurationChanged Landscape "+ newConfig.orientation );
			Intent i = new Intent(this, SktIabActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			this.onPause();
			startActivity(i);
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	DBG(TAG,"onConfigurationChanged portrait "+ newConfig.orientation);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onConfigurationChanged portrait "+ newConfig.orientation);
	    }
	    else 
	    {
	    	DBG(TAG,"onConfigurationChanged user"+ newConfig.orientation);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onConfigurationChanged user"+ newConfig.orientation);
	    }
	}
	
    private void performPurchaseRequest() 
	{
		INFO(TAG, "performPurchaseRequest");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "performPurchaseRequest");
		
		FragmentManager m = getSupportFragmentManager();
		PaymentFragment payment = (PaymentFragment) m.findFragmentByTag("paymentView");
		
		FragmentTransaction transction = m.beginTransaction();
		if (payment != null) {
			transction.detach(payment);
		}
		
		if (payment == null) {
			transction.add(/*R.id.container,*/ new PaymentFragment(), "paymentView");
		} else {
			transction.attach(payment);
		}
		transction.commit();
    }
	
	private void performRestoreTransactions()
	{
		pDialog = ProgressDialog.show(((Activity)SUtils.getContext()), 
		((Activity)SUtils.getContext()).getString(R.string.IAB_SKT_RESTORING_TRANSACTIONS_TITLE),
		((Activity)SUtils.getContext()).getString(R.string.IAB_SKT_RESTORING_TRANSACTIONS));
		FragmentManager m = getSupportFragmentManager();
		CommandFragment payment = (CommandFragment) m.findFragmentByTag("CommandView");
		
		FragmentTransaction transction = m.beginTransaction();
		if (payment != null) {
			transction.detach(payment);
		}
		
		if (payment == null) {
			transction.add(/*R.id.container,*/ new CommandFragment(), "commandView");
		} else {
			transction.attach(payment);
		}
		transction.commit();
	}

    private void setupBilling() {
		
    }

	private static boolean mIsConfirmInProgress = false;
	private static final int DELAY_TIME_FOR_GL_RETRY = 	30*1000;
	
	public void sendConfirmation(final String transactionId)
	{
		DBG(TAG, "sendConfirmation");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "sendConfirmation");
		if (mIsConfirmInProgress)
			return;
		//InAppBilling.save(WAITING_FOR_GAMELOFT);
		//InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);	
		new Thread( new Runnable()
		{
			public void run()
			{
				Looper.prepare();
				boolean finish = false;
				mIsConfirmInProgress = true;
			#if ITEMS_STORED_BY_GAME	
				int retryTimes = 3;
			#endif
				do 
				{
				try
				{
					XPlayer mXplayer = new XPlayer(new Device(mServerInfo));
					mXplayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
					mXplayer.sendIABNotification(0, InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME), transactionId);

					while (!mXplayer.handleIABNotification())
					{
						try {
							Thread.sleep(50);
						} catch (Exception exc) {}
						DBG(TAG, "[sendIABNotification]Waiting for response");
					} 
					if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
					{
						//Informing the game that the buy item proccess is completed and success.
						DBG(TAG, "Sending notify to the game about a success transaction");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Sending notify to the game about a success transaction");
						finish = true;
						mIsConfirmInProgress = false;
						//InAppBilling.saveLastItem(STATE_SUCCESS);
						//InAppBilling.clear();
						
						String TxId = XPlayer.getLastEComTxId();
						XPlayer.clearLastEComTxId();
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
						
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
													
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
						}
						finish();
					}
					else if (XPlayer.getLastErrorCode() != XPlayer.ERROR_NONE)
					{
						DBG(TAG, "Sending notify to the game about a fail transaction");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Sending notify to the game about a fail transaction");
						finish = true;
						mIsConfirmInProgress = false;
						//InAppBilling.saveLastItem(STATE_SUCCESS);
						//InAppBilling.clear();
						
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
						
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
													
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
						}
						finish();
					}
				}catch(Exception exd)
				{
					DBG(TAG,"An error has occurred, error: "+exd.getMessage());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "An error has occurred, error: "+exd.getMessage() );
				}
					try{
						Thread.sleep(DELAY_TIME_FOR_GL_RETRY);
					}catch(Exception ex){};
				}while (!finish
			#if ITEMS_STORED_BY_GAME	
					&& --retryTimes >= 0
			#endif
					);
				Looper.loop();
			}
		}
	#if !RELEASE_VERSION
		,"Thread-Confirmation"
	#endif
		).start();
	}
}
#endif //USE_IN_APP_BILLING
