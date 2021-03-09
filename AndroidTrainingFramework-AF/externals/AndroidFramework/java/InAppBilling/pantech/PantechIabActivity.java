#if USE_IN_APP_BILLING

package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.pantech.app.appsplay.iabl.client.CheckerCallback;
import com.pantech.app.appsplay.iabl.client.ContentData;
import com.pantech.app.appsplay.iabl.client.util.DebugPrint;
import com.pantech.app.appsplay.iabl.IABLConnector;
import com.pantech.app.appsplay.iabl.IABLConnectorException;

import java.lang.reflect.Method;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Locale;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;


public final class PantechIabActivity extends Activity implements CheckerCallback {

    SET_TAG("PantechIabActivity");

	public static final byte[] SALT = new byte[] { 78, -23, 123, -45, -3, -7, 8, 32, 89, 92, -99, -62, 44, -16, 74, -121, -39, -48, 59, 10 };

	public static final String BASE64_PUBLIC_KEY = 	InAppBilling.GET_STR_CONST(PANTECH_PUBLIC_KEY);

    private static String IID = "";
    private static String uid = "";
	private static AServerInfo mServerInfo = null;

    // Not needed at the moment
    private static String serverIP = "";
    private static int serverPort = -1;

    private boolean purchaseInProgrese = false;
    private boolean init = false;
    private boolean errorOcurred = false;
	private boolean needTofinishinAfterInteraction = false;

    public static boolean LaunchPantechBilling(String item, String IID, AServerInfo si) {
        PantechIabActivity.uid = item;
        PantechIabActivity.IID = IID;
		PantechIabActivity.mServerInfo = si;
		
		try {
            Intent i = new Intent();
            String packageName = SUtils.getContext().getPackageName();
            i.setClassName(packageName, packageName + ".iab.PantechIabActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			SUtils.getContext().startActivity(i);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		DBG(TAG, "onCreate");
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
		DBG(TAG, "onDestroy");
		IABLConnector.destory();
	}
	/*@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	DBG(TAG,"onConfigurationChanged Landscape "+ newConfig.orientation);
			Intent i = new Intent(this, PantechIabActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(i);
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	DBG(TAG,"onConfigurationChanged portrait "+ newConfig.orientation);
	    }
	    else 
	    {
	    	DBG(TAG,"onConfigurationChanged user"+ newConfig.orientation);
	    }
	}*/
	
    private void performPurchaseRequest() {
		
		DBG(TAG, "performPurchaseRequest "+IID);
		IABLConnector.requestPurchase(IID,ContentData.PURCHASE_TYPE_SYNC, this);
    }

	// public static final CharSequence PREDEFINED_PURCHASED = "com.pantech.app.appsplay.purchased";
	// public static final CharSequence PREDEFINED_NOTPURCHASEDITEM = "com.pantech.app.appsplay.notpurchaseditem";
	// public static final CharSequence PREDEFINED_ITEMPAYLIST = "com.pantech.app.appsplay.itempaylist";
	// public static final CharSequence PREDEFINED_ITEMLIST = "com.pantech.app.appsplay.itemlist";
	// public static final CharSequence PREDEFINED_INVALID_ITEM = "com.pantech.app.appsplay.invaliditem";
	// public static final CharSequence PREDEFINED_INVALID_PACKAGENAME = "com.pantech.app.appsplay.invalidpackagename";
    
	private void setupBilling() 
	{
        DBG(TAG, "Pantech Init");
		IABLConnector.init(this, BASE64_PUBLIC_KEY, SALT);
    }

	@Override
	public void successPurchase(final ContentData a_oContentData)
	{
		JDUMP(TAG, a_oContentData.getIID());
		JDUMP(TAG, a_oContentData.getOrderId());
		sendConfirmation();
	}	
	
	@Override
	public void resultTransactionList(ArrayList<ContentData> a_arTransactionList)
	{
		INFO(TAG, "resultTransactionList");
	}
	
	@Override
	public void resultSellingList(ArrayList<ContentData> a_arSellingList)
	{
		INFO(TAG, "resultSellingList");
	}

	@Override
	public void cancelPurchase()
	{
		INFO(TAG, "cancelPurchase");
		//Informing the game that the buy item proccess is complete and canceled.
		Bundle bundle = new Bundle();
		bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

		bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 2);//trash value
		bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);
		try{
			Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
			Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
			bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
		}catch(Exception ex ) {
			ERR(TAG,"Error invoking reflex method "+ex.getMessage());
		}
		finish();
	}
	
    // ERROR_SERVER_FAILURE("Server failure.")
    // ERROR_INVALID_PACKAGE_NAME("Package is not register O&M.")
    // ERROR_INVALID_ITEMID("Request item_id is invalid.")
    // ERROR_NOT_PURCHASED_ITEM("No Purchased Item.")
    // ERROR_NOT_PURCHASED_APP("No Purchased App.")
    // ERROR_NOT_LOGIN("User have to sign-in to AppsPlay.")
    // ERROR_NETWORK_CONNECT_PROBLEM("Network Error.")
    // ERROR_VERIFY_FAIL("Signature verify failed.")
    // ERROR_INVALID_PUBLIC_KEY("Supplied public key is invalid.")
    // ERROR_INVALID_PRIVATE_KEY("Not Register private key.")
    // ERROR_CREATE_SIGNATURE("Signature creation error.")
    // ERROR_NOT_FIND_APPSPLAY("User have to download AppsPlay application.")
    // ERROR_NOT_INSTALLED_PACKAGENAME("The package not installed.")
    // ERROR_NOT_MATCH_DATA("RequestData And ResponseData do not match.")
    // ERROR_JSON_PARSE_ERROR("Json Parsing Error.")
    // ERROR_UNKNOWN_RESPONSE_CODE("Unknown response code.")
    // ERROR_REMOTE_EXCEPTION("Service binding error.")
    // CHECK_IN_PROGRESS("A previous check request is already in progress. Only one request is allowed at a time."),
	@Override
	public void applicationError(CheckErrorCode a_nErrorCode)
	{
		JDUMP(TAG, a_nErrorCode.getMessage());
		
		if (a_nErrorCode == CheckErrorCode.ERROR_NOT_FIND_APPSPLAY)
		{
			showCustomDialog(R.string.IAB_PANTECH_NO_APPSPLAY);
		}
		else if (a_nErrorCode == CheckErrorCode.ERROR_NOT_LOGIN)
		{
			showCustomDialog(R.string.IAB_PANTECH_NO_LOGIN);
		}
		else
		{
			showCustomDialog(R.string.IAB_PANTECH_ERROR);
			#if !RELEASE_VERSION
				StringBuilder b = new StringBuilder();
				b.append("Error: ").append(a_nErrorCode.getMessage());
				android.widget.Toast.makeText(this , b, android.widget.Toast.LENGTH_LONG).show();
			#endif
		}
		//Informing the game that the buy item proccess is complete and fail.
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
			ERR(TAG,"Error invoking reflex method "+ex.getMessage());
		}
		finish();
	}
	
	@Override
	public boolean dispatchKeyEvent(android.view.KeyEvent event)
	{
		JDUMP(TAG, event.toString());
		return true;
	}
	private void showCustomDialog(final int idMsg) 
	{
		runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				//final Dialog dialog = new Dialog(SUtils.getContext(), R.style.PayPalModal);
				final Dialog dialog = new Dialog(SUtils.getContext());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.iab_dialog_single_button);
				dialog.setCancelable(false);

				TextView textViewName = (TextView) dialog.findViewById(R.id.textView1);
				textViewName.setText(getString(idMsg));
				

				Button btnOK = (Button) dialog.findViewById(R.id.OkButton);
				btnOK.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
  			}
  		});
		finish();
    }
	
	private static boolean mIsConfirmInProgress = false;
	private static final int DELAY_TIME_FOR_GL_RETRY = 	30*1000;
	private void sendConfirmation()
	{
		DBG(TAG, "sendConfirmation");
		if (mIsConfirmInProgress)
			return;
		//InAppBilling.save(WAITING_FOR_GAMELOFT);
		//InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);	
	#if ITEMS_STORED_BY_GAME
		
		Bundle bundle = new Bundle();
		bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
		
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
		bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
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
		
		//InAppBilling.saveLastItem(STATE_SUCCESS);
		//InAppBilling.clear();
		
	#endif //#if ITEMS_STORED_BY_GAME
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
					mXplayer.sendIABNotification(0, InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME));

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
						
						finish = true;
						mIsConfirmInProgress = false;
					#if !ITEMS_STORED_BY_GAME
						//InAppBilling.saveLastItem(STATE_SUCCESS);
						//InAppBilling.clear();
						
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
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
					#endif	//#if !ITEMS_STORED_BY_GAME
						finish();
					}
				}catch(Exception exd)
				{
					DBG("Tracking","No internet avaliable");
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
