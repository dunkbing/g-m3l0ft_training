#if USE_BILLING && USE_BOKU_FOR_BILLING
package APP_PACKAGE.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;


import java.lang.reflect.Method;
import com.boku.mobile.api.IntentProvider;
import com.boku.mobile.api.Transaction;

import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.billing.ServerInfo;
import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.R;


public class BokuActivity extends Activity
{

    SET_TAG("BokuActivity");

	private static AServerInfo mServerInfo = null;
	private static String mItemId = null;

    private boolean purchaseInProgress = false;
	private ProgressDialog dialog = null;

	private static final int BOKU_PAYMENT_ID = 1234;
	
	//Boku Success Result Code
	private final int BOKU_RESULT_CODE_SUCCESSFUL		= 0;
	
	public static boolean sendToServerSuccessfully = false;
	
	public static boolean gIsRunning = false;
	

	
	//Boku Failure Results Code
	private final int BOKU_RESULT_CODE_LIMIT_EXCEEDED			= 1;	//Throttle limit exceeded
	private final int BOKU_RESULT_CODE_INTERNAL_ERROR			= 2;	//Internal error
	private final int BOKU_RESULT_CODE_INSUFFICIENT_FUNDS		= 3;	//Insufficient funds
	private final int BOKU_RESULT_CODE_CUSTOMER_BLOCKED			= 4;	//Customer Blocked
	private final int BOKU_RESULT_CODE_EXTERNAL_BILLING_FAIL	= 5 ;	//External billing failure
	private final int BOKU_RESULT_CODE_ANTI_SPAM				= 7;	//Anti-spam: transaction rejected
	private final int BOKU_RESULT_CODE_REGULATORY_LIMIT			= 11;	//Regulatory spend limit reached
	private final int BOKU_RESULT_CODE_MERCHANT_LIMIT			= 12;	//merchant spend limit reached
	private final int BOKU_RESULT_CODE_INVALID_PANEL			= 13;	//Invalid payment panel style specified
	private final int BOKU_RESULT_CODE_MARKET_UNAVAILABLE		= 17;	//Market currently unavailable
	private final int BOKU_RESULT_CODE_MISS_INVALID_CMD			= 20;	//Missing or invalid 'cmd=' value
	private final int BOKU_RESULT_CODE_MISS_INVALID_NET_CODE	= 25;	//Missing or invalid network code
	private final int BOKU_RESULT_CODE_CONDITIONS_NO_ACEPTED	= 26;	//Conditions not accepted
	private final int BOKU_RESULT_CODE_INVALID_SIGNATURE		= 28;	//Invalid signature
	private final int BOKU_RESULT_CODE_UNSUPPORTED_PRICE_POINT	= 29;	//Unsupported price point
	private final int BOKU_RESULT_CODE_MISS_INVALID_PRICE		= 31;	//Invalid or missing price
	private final int BOKU_RESULT_CODE_BAD_BIN_CREDENTIALS		= 32;	//Bad bind credentials
	private final int BOKU_RESULT_CODE_MISS_INVALID_CURRENCY	= 33;	//Invalid or missing currency code
	private final int BOKU_RESULT_CODE_MISS_INVALID_SERVICE		= 34;	//Invalid or missing service ID
	private final int BOKU_RESULT_CODE_INTERNAL_ERROR_2			= 35;	//Internal Error
	private final int BOKU_RESULT_CODE_MISS_INVALID_COUNTRY		= 36;	//Invalid or missing country code
	private final int BOKU_RESULT_CODE_INVALID_DYN_PRICE_MODE	= 37;	//Invalid dynamic pricing mode
	private final int BOKU_RESULT_CODE_INVALID_DYN_MATCH		= 38;	//Invalid dynamic-match
	private final int BOKU_RESULT_CODE_MISS_INVALID_DEVIATION	= 39;	//Invalid or missing dynamic-deviation
	private final int BOKU_RESULT_CODE_MISS_INVALID_POLYCY		= 40;	//Invalid or missing dynamic-deviation-polycy
	private final int BOKU_RESULT_CODE_NO_PAYMENT_SOLUTION		= 41;	//No payment solution available
	private final int BOKU_RESULT_CODE_COUNTRY_NOT_AVAILABLE	= 42;	//Country not available on requested service
	private final int BOKU_RESULT_CODE_INVALID_REQUEST			= 43;	//Invalid request
	private final int BOKU_RESULT_CODE_INVALID_LANGUAGE			= 48;	//Invalid language/locale code
	private final int BOKU_RESULT_CODE_UNSUPPORTED_LANGUAGE		= 49;	//Unsupported language code for country/locale
	private final int BOKU_RESULT_CODE_FEATURE_DISABLE			= 50;	//Feature disable for account
	private final int BOKU_RESULT_CODE_INVALID_ROW_REF			= 60;	//Invalid row-ref value
	private final int BOKU_RESULT_CODE_SERVICE_NOT_SUPPORTED	= 86;	//Service not supported on network
	private final int BOKU_RESULT_CODE_MISSING_INVALID_USER		= 91;	//Missing or invalid user parameters
	private final int BOKU_RESULT_CODE_BOKU_UNDER_MAINTENANCE	= 99;	//BOKU undergoing maintenance
	
	//Boku pending Result Code	
	private final int BOKU_RESULT_CODE_PENDING					= 200;	//Transaction is pending
	
	//Boku client Result Codes
	
	private final int BOKU_RESULT_CODE_P_BACK_TRANS_IN_PROGRESS	= 301;	//User pressed back key while transaction is progress
	private final int BOKU_RESULT_CODE_P_BACK_CANCEL			= 302;	//User pressed "Cancel" or Back key before starting transaction
	private final int BOKU_RESULT_CODE_NETWORK_ERROR			= 303;	//Network error(e.g. no data connection)
	private final int BOKU_RESULT_CODE_SIM_ERROR				= 304;	//SIM error(e.g. problem reading SIM card)
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(APP_PACKAGE.CLASS_NAME.m_sInstance == null) 		
		{
			gIsRunning = false;
			ReturnToGame();
			return;
		}
		StartBokuBilling();
		gIsRunning = true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyWaiting();
		gIsRunning = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public boolean StartBokuBilling() {
		try {
			//Intent i = IntentProvider.newIntent(this);
			Intent i = new Intent(this, MyPaymentPanelActivity.class);
			ServerInfo mySI = (ServerInfo)mServerInfo;
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra(Transaction.MERCHANT_ID, GAMELOFT);
			i.putExtra(Transaction.API_KEY, BOKU_API_KEY);
			i.putExtra(Transaction.SERVICE_ID,  mySI.getServiceId());
			i.putExtra(Transaction.CURRENCY, mySI.getCurrencySymbol());
			i.putExtra(Transaction.PRODUCT_DESCRIPTION, SUtils.getContext().getString(R.string.app_name));
			i.putExtra(Transaction.SUB_MERCHANT_NAME, SUtils.getContext().getString(R.string.app_name));
			i.putExtra(Transaction.PRICE_INC_SALES_TAX, mySI.getPriceWithTaxes());
			i.putExtra(Transaction.PARAM, "");
			
			
			//used for sebug
			// i.putExtra(Transaction.DEBUG_LOGS, !true);
			///i.putExtra(Transaction.OFFLINE_DEMO_MODE, true);
			
			startActivityForResult(i, BOKU_PAYMENT_ID);
        } catch (Exception e) {
			ERR(TAG, "Error "+e.getMessage());
            return false;//TODO: put error code here
        }
		return true;
    }
	
	public static boolean LaunchBokuBilling(String item, AServerInfo si) 
	{
		if(si!=null)
		{
			mItemId = item;
			mServerInfo = si;
			Intent i = new Intent();
			String packageName = SUtils.getContext().getPackageName();
			i.setClassName(packageName, packageName + CLASS_NAME_BOKU_ACTIVITY);
			SUtils.getContext().startActivity(i);
			return true;
		}
		return false;
    }
	
	private void showWaiting(){
		 if(dialog == null)
			dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(getString(R.string.WAITING));
		dialog.show();
	}
	
	private void destroyWaiting(){
		if(dialog!=null)
		{
			dialog.dismiss();
            dialog =null;
		}
	}
	
	public static void sendFullVersionNotificationToServer()
	{
		XPlayer mXplayer;
		if(mServerInfo!=null)
			mXplayer = new XPlayer(new Device(mServerInfo));
		else
			mXplayer = new XPlayer(new Device());
		mXplayer.sendBOKUPurchaseRequest();
		while (!mXplayer.handleBOKUPurchaseRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		}
		if(sendToServerSuccessfully)
		{
			SUtils.getLManager().saveNeedSendNotificationToServer(SUtils.getLManager().BOKU_UNLOCKED,"",-1);
			sendToServerSuccessfully = false;
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode==BOKU_PAYMENT_ID)
		{
			gIsRunning = false;
			if (resultCode == Activity.RESULT_OK)
			{
				try
				{
					String bokuResultCode = data.getStringExtra(Transaction.RESULT_CODE);
					int rc=Integer.parseInt(bokuResultCode);
					String bokuResultMessage = data.getStringExtra(Transaction.RESULT_MESSAGE);
					if(rc==BOKU_RESULT_CODE_SUCCESSFUL)
					{
						// Unlock the game
						SUtils.getLManager().saveUnlockGame(SUtils.getLManager().FULL_VERSION);
						SUtils.getLManager().saveNeedSendNotificationToServer(SUtils.getLManager().BOKU_LOCKED,mServerInfo.getGamePrice(),mServerInfo.getStringCurrencyValue());
						showWaiting();
						
						//add record to GL Server
						new Thread(new Runnable() {
							public void run() {
								sendFullVersionNotificationToServer();
								ReturnToGame();
							}
						}).start();
						
						return;
					}
				}
				catch(Exception e){}
			}
			
			ReturnToGame();
		}
	}
	
	private void ReturnToGame()
	{
		Intent intent = new Intent(this, APP_PACKAGE.CLASS_NAME.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
		return;
	}
}
#endif //USE_BILLING && USE_BOKU_FOR_BILLING
