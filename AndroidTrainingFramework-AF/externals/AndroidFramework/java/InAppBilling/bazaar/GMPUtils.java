package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;
import APP_PACKAGE.GLUtils.SUtils;

public class GMPUtils
{
	SET_TAG("InAppBilling");
#if !RELEASE_VERSION
	#define CHECKSETUPDONE(x) checkSetupDone(x)
	#define FLAGSTARTASYNC(x) flagStartAsync(x)
#else
	#define CHECKSETUPDONE(x) checkSetupDone(" ")
	#define FLAGSTARTASYNC(x) flagStartAsync(" ")
#endif
	// Is setup done?
	boolean mSetupDone = false;

	// Is an asynchronous operation in progress?
	// (only one at a time can be in progress)
	boolean mAsyncInProgress = false;

	// (for logging/debugging)
	// if mAsyncInProgress == true, what asynchronous operation is in progress?
	String mAsyncOperation = "";

	// Context we were passed during initialization
	Context mContext;

	// Connection to the service
	IInAppBillingService mService;
	
	ServiceConnection mServiceConn;

	// The request code used to launch purchase flow
	int mRequestCode;

	// Billing response codes
	public static final int BILLING_RESPONSE_RESULT_OK = 0;
	public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
	public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
	public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
	public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
	public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
	public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
	public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

	// IAB Helper error codes
	public static final int IABHELPER_ERROR_BASE = -1000;
	public static final int IABHELPER_REMOTE_EXCEPTION = -1001;
	public static final int IABHELPER_BAD_RESPONSE = -1002;
	public static final int IABHELPER_VERIFICATION_FAILED = -1003;
	public static final int IABHELPER_SEND_INTENT_FAILED = -1004;
	public static final int IABHELPER_USER_CANCELLED = -1005;
	public static final int IABHELPER_UNKNOWN_PURCHASE_RESPONSE = -1006;
	public static final int IABHELPER_MISSING_TOKEN = -1007;
	public static final int IABHELPER_UNKNOWN_ERROR = -1008;

	// Keys for the responses from InAppBillingService
	public static final String RESPONSE_CODE = "RESPONSE_CODE";
	public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
	public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
	public static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
	public static final String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
	public static final String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
	public static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
	public static final String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
	public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";
	
	// Bundle key params
	final static String KEY_ITEM_SKU 		= "a1";
	final static String KEY_ITEM_ID 		= "a2";
	final static String KEY_OPERATION		= "a3";
	final static String KEY_RSLT_RESPONSE	= "a4";
	final static String KEY_RSLT_MESSAGE	= "a5";
	final static String KEY_PAYLOAD			= "a6";
	final static String KEY_PCHS_JSON		= "a7";
	final static String KEY_PCHS_SIGNATURE	= "a8";
	final static String KEY_REQUEST_CODE	= "a9";
	final static String KEY_PRODUCTS_ARRAY	= "b1";
	
	

	// Item type: in-app item
	public static final String ITEM_TYPE_INAPP = "inapp";

	// some fields on the getSkuDetails response bundle
	public static final String GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
	public static final String GET_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";

	private static GMPUtils mThis = null;
	public GMPUtils()
	{
		mThis = this;
	}
	
	public GMPUtils(Context context)
	{
		this();
		mContext = context;//.getApplicationContext();
	}
	
	public static GMPUtils getInstance()
	{
		if (mThis == null)
		{
			mThis = new GMPUtils();
		}
		return mThis;
	}
	
	public void setContext(Context context)
	{
		mContext = context;//.getApplicationContext();
	}
	
	class InitializeService extends IABAsyncTask
	{
		IABCallBack mCB = null;
		public InitializeService(IABCallBack cb) { mCB = cb; }
		
		protected Integer doInBackground(final Bundle bundle) 
		{
			bundle.clear();
			// If already set up, can't do it again.
			if (mSetupDone) throw new IllegalStateException("IAB helper is already set up.");

			// Connection to IAB service
			DBG(TAG,"Starting in-app billing setup.");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Starting in-app billing setup.");
			mServiceConn = new ServiceConnection() {
				@Override
				public void onServiceDisconnected(ComponentName name) {
					DBG(TAG,"Billing service disconnected.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Billing service disconnected.");
					InAppBilling.mMKTStatus = InAppBilling.MKT_CANNOT_CONNECT;
					mService = null;
				}

				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					DBG(TAG,"Billing service connected.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Billing service connected.");
					mService = IInAppBillingService.Stub.asInterface(service);
					String packageName = mContext.getPackageName();
					try {
						DBG(TAG,"Checking for in-app billing 3 support.");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Checking for in-app billing 3 support.");
						int response = mService.isBillingSupported(3, packageName, ITEM_TYPE_INAPP);
						if (response != BILLING_RESPONSE_RESULT_OK) {
							bundle.putInt(KEY_RSLT_RESPONSE,response);
						#if !RELEASE_VERSION	
							bundle.putString(KEY_RSLT_MESSAGE, "Error checking for billing v3 support.");
						#endif
						}
						else
						{
							bundle.putInt(KEY_RSLT_RESPONSE,response);
						#if !RELEASE_VERSION	
							bundle.putString(KEY_RSLT_MESSAGE, "Setup successful.");
						#endif
							DBG(TAG,"In-app billing version 3 supported for " + packageName);
							LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "In-app billing version 3 supported for " + packageName);
							mSetupDone = true;
						}
					}
					catch (RemoteException e) 
					{
						bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_REMOTE_EXCEPTION);
					#if !RELEASE_VERSION	
						bundle.putString(KEY_RSLT_MESSAGE, "RemoteException while setting up in-app billing.");
					#endif
						DBG_EXCEPTION(e);
					}
					if (mCB != null)
					{
						mCB.runCallBack(bundle);
					}
				}
			};
			Intent serviceIntent = new Intent("ir.cafebazaar.pardakht.InAppBillingService.BIND");
			
			if (!((Activity)SUtils.getContext()).getPackageManager().queryIntentServices(serviceIntent, 0).isEmpty())
			{
				boolean result = ((Activity)SUtils.getContext()).bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
				if(result == false) {
					InAppBilling.mMKTStatus = InAppBilling.MKT_NO_SUPPORTED;
				}
			}
			else
			{
				InAppBilling.mMKTStatus = InAppBilling.MKT_NO_SUPPORTED;
			}
				
			return new Integer(0);
		}
	}
	
	public void startService(IABCallBack cb)
	{
		DBG(TAG, "[startService]");
		//LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "[startService]");
		Bundle bundle = new Bundle();
		new InitializeService(cb).execute(bundle, mContext);
	}
	
	
	class PurchaseRequest extends IABAsyncTask
	{
		IABCallBack mCB = null;
		public PurchaseRequest(IABCallBack cb) { mCB = cb; }
		
		protected Integer doInBackground(final Bundle bundle) 
		{
			String sku 			= bundle.getString(KEY_ITEM_SKU);
			int requestCode 	= bundle.getInt(KEY_REQUEST_CODE);
			String devPayload 	= bundle.getString(KEY_PAYLOAD);
			
			try
			{
				CHECKSETUPDONE("launchPurchaseFlow");
				FLAGSTARTASYNC("launchPurchaseFlow");
			}
			catch(Exception exc)
			{
				DBG(TAG,"Cannot start request purchase, another AsyncTask is in place");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Cannot start request purchase, another AsyncTask is in place");
				
				Bundle bndle = new Bundle();
				if (mCB != null)
				{	
					bndle.putInt(KEY_RSLT_RESPONSE,IABHELPER_UNKNOWN_ERROR);
				#if !RELEASE_VERSION
					bndle.putString(KEY_RSLT_MESSAGE, "Cannot start request purchase, another AsyncTask is in place");
				#endif
					mCB.runCallBack(bndle);
				}
				else
				{					
					bndle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
					byte[] emptyarray = new String().getBytes();
					bndle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():emptyarray);
					bndle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():emptyarray);//trash value
					bndle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);//trash value
					bndle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);//trash value
					
					bndle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
					bndle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);

					try{
						Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
						Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
						bndle = (Bundle)mMethod.invoke(null, (new Object[]{bndle}));
					}
					catch(Exception ex )
					{
						ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
					}
				}
				return new Integer(0);
			}
			
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "launchPurchaseFlow");
			
			bundle.clear();
			try {
				DBG(TAG,"Constructing buy intent for " + sku);
				Bundle buyIntentBundle = mService.getBuyIntent(3, mContext.getPackageName(), sku, ITEM_TYPE_INAPP, devPayload);
				int response = getResponseCodeFromBundle(buyIntentBundle);
				if (response != BILLING_RESPONSE_RESULT_OK) {
					ERR(TAG,"Unable to buy item, Error response: " + getResponseDesc(response));

					bundle.putInt(KEY_RSLT_RESPONSE,response);
				#if !RELEASE_VERSION
					bundle.putString(KEY_RSLT_MESSAGE, "Unable to buy item.");
				#endif
				}
				else
				{

					PendingIntent pendingIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);
					DBG(TAG,"Launching buy intent for " + sku + ". Request code: " + requestCode);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Launching buy intent for " + sku + ". Request code: " + requestCode);
					mRequestCode = requestCode;
					mPurchaseCallback = mCB;
					((Activity)mContext).startIntentSenderForResult(pendingIntent.getIntentSender(),
												   requestCode, new Intent(),
												   Integer.valueOf(0), Integer.valueOf(0),
												   Integer.valueOf(0));
					flagEndAsync();
					return new Integer(0);
				}
			}
			catch (SendIntentException e) {
				ERR(TAG,"SendIntentException while launching purchase flow for sku " + sku);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "SendIntentException while launching purchase flow for sku " + sku);
				DBG_EXCEPTION(e);

				bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_SEND_INTENT_FAILED);
				#if !RELEASE_VERSION
					bundle.putString(KEY_RSLT_MESSAGE, "Failed to send intent.");
				#endif
			}
			catch (RemoteException e) {
				ERR(TAG,"RemoteException while launching purchase flow for sku " + sku);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "RemoteException while launching purchase flow for sku " + sku);
				DBG_EXCEPTION(e);

				bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_REMOTE_EXCEPTION);
				#if !RELEASE_VERSION
					bundle.putString(KEY_RSLT_MESSAGE, "Remote exception while starting purchase flow");
				#endif
				
			}
			
			if (mCB != null)
			{
				flagEndAsync();
				mCB.runCallBack(bundle);
			}
			flagEndAsync();	
			return new Integer(0);
		}
	}
	// The Callback registered on launchPurchaseRequest, which we have to call back when
    // the purchase finishes
    IABCallBack mPurchaseCallback;
	/**
     * Initiate the UI checkout for an in-app purchase. Call this method to initiate an in-app purchase,
     * which will involve bringing up the Google Play screen. The calling activity will be paused while
     * the user interacts with Google Play, and the result will be delivered via the activity's
     * {@link android.app.Activity#onActivityResult} method, at which point you must call
     * this object's {@link #handleActivityResult} method to continue the purchase flow. This method
     * MUST be called from the UI thread of the Activity.
     *
     * @param context The calling activity.
     * @param sku The sku of the item to purchase.
     * @param requestCode A request code (to differentiate from other responses --
     *     as in {@link android.app.Activity#startActivityForResult}).
     * @param cb The callback to notify when the purchase process finishes
     * @param payload Extra data (developer payload), which will be returned with the purchase data
     *     when the purchase completes. This extra data will be permanently bound to that purchase
     *     and will always be returned when the purchase is queried.
     */
	public void launchPurchaseRequest(Activity context, String sku, int requestCode, IABCallBack cb, String payload)
	{
		DBG(TAG, "[launchPurchaseRequest]");
		Bundle bundle = new Bundle();
		bundle.putString(KEY_ITEM_SKU, sku);
		bundle.putString(KEY_PAYLOAD, payload);
		bundle.putInt(KEY_REQUEST_CODE, requestCode);
		mContext = context;
		new PurchaseRequest(cb).execute(bundle, context);
	}
	
	/**
	 * Handles an activity result that's part of the purchase flow in in-app billing. If you
	 * are calling {@link #launchPurchaseRequest}, then you must call this method from your
	 * Activity's {@link android.app.Activity@onActivityResult} method. This method
	 * MUST be called from the UI thread of the Activity.
	 *
	 * @param requestCode The requestCode as you received it.
	 * @param resultCode The resultCode as you received it.
	 * @param data The data (Intent) as you received it.
	 * @return Returns true if the result was related to a purchase flow and was handled;
	 *     false if the result was not related to a purchase, in which case you should
	 *     handle it normally.
	 */
	public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != mRequestCode) return false;

		CHECKSETUPDONE("handleActivityResult");

		// end of async purchase operation
		flagEndAsync();
		
		Bundle bundle = new Bundle();
		if (data == null) {
			ERR(TAG,"Null data in IAB activity result.");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Null data in IAB activity result.");
			bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_BAD_RESPONSE);
		#if !RELEASE_VERSION
			bundle.putString(KEY_RSLT_MESSAGE, "Null data in IAB result");
		#endif
			if (mPurchaseCallback != null) mPurchaseCallback.runCallBack(bundle);
			return true;
		}

		int responseCode = getResponseCodeFromIntent(data);
		String purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
		String dataSignature = data.getStringExtra(RESPONSE_INAPP_SIGNATURE);

		DBG(TAG,"Bazaar purchase result "+responseCode+" : "+resultCode);
		if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
			DBG(TAG,"Successful resultcode from purchase activity.");
			DBG(TAG,"Purchase data: " + purchaseData);
			DBG(TAG,"Data signature: " + dataSignature);
			DBG(TAG,"Extras: " + data.getExtras());
			
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Successful resultcode from purchase activity.");
			
			if (purchaseData == null || dataSignature == null) {
				ERR(TAG,"BUG: either purchaseData or dataSignature is null.");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "BUG: either purchaseData or dataSignature is null.");
				DBG(TAG,"Extras: " + data.getExtras().toString());
				bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_UNKNOWN_ERROR);
			#if !RELEASE_VERSION
				bundle.putString(KEY_RSLT_MESSAGE, "IAB returned null purchaseData or dataSignature");
			#endif
				if (mPurchaseCallback != null) mPurchaseCallback.runCallBack(bundle);
				return true;
			}

			PurchaseInfo purchase = s.vp(purchaseData, dataSignature);

			// Verify signature (Security.verifyPurchase) 
			if (purchase == null)
			{
				ERR(TAG,"Purchase signature verification FAILED");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Purchase signature verification FAILED");
				bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_VERIFICATION_FAILED);
			#if !RELEASE_VERSION
				bundle.putString(KEY_RSLT_MESSAGE, "Signature verification failed");
			#endif
				if (mPurchaseCallback != null) mPurchaseCallback.runCallBack(bundle);
				return true;
			}
			else
			{
				if (mPurchaseCallback != null) 
				{
					bundle.putInt(KEY_RSLT_RESPONSE,BILLING_RESPONSE_RESULT_OK);
				#if !RELEASE_VERSION
					bundle.putString(KEY_RSLT_MESSAGE, "Success");
				#endif
					bundle.putString(GMPUtils.KEY_PCHS_JSON, purchase.getOriginalJson());
					bundle.putString(GMPUtils.KEY_PCHS_SIGNATURE, purchase.getSignature());
					DBG(TAG,"Purchase signature successfully verified.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Purchase signature successfully verified.");
					mPurchaseCallback.runCallBack(bundle);
					return true;
				}
			}
			
		}
		else if (resultCode == Activity.RESULT_OK) {
			// result code was OK, but in-app billing response was not OK.
			DBG(TAG, "Bazaar Activity.RESULT_OK");
			DBG(TAG,"Result code was OK but in-app billing response was not OK: " + getResponseDesc(responseCode));
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Result code was OK but in-app billing response was not OK: " + getResponseDesc(responseCode));
			if (mPurchaseCallback != null)
			{
				bundle.putInt(KEY_RSLT_RESPONSE,responseCode);
			#if !RELEASE_VERSION
				bundle.putString(KEY_RSLT_MESSAGE, "Problem purchashing item.");
			#endif
				mPurchaseCallback.runCallBack(bundle);
			}
		}
		else if (resultCode == Activity.RESULT_CANCELED) {
			DBG(TAG,"Purchase canceled - Response: " + getResponseDesc(responseCode));
			LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Purchase canceled - Response: " + getResponseDesc(responseCode));
			bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_USER_CANCELLED);
			#if !RELEASE_VERSION
				bundle.putString(KEY_RSLT_MESSAGE, "User canceled.");
			#endif
			if (mPurchaseCallback != null) mPurchaseCallback.runCallBack(bundle);
		}
		else {
			ERR(TAG,"Purchase failed. Result code: " + Integer.toString(resultCode)
					+ ". Response: " + getResponseDesc(responseCode));
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Purchase failed. Result code: " + Integer.toString(resultCode)
					+ ". Response: " + getResponseDesc(responseCode));		
			bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_UNKNOWN_PURCHASE_RESPONSE);
		#if !RELEASE_VERSION
			bundle.putString(KEY_RSLT_MESSAGE, "Unknown purchase response.");
		#endif
			if (mPurchaseCallback != null) mPurchaseCallback.runCallBack(bundle);
		}
		return true;
	}
	
   /**
     * Consumes a given in-app product. Consuming can only be done on an item
     * that's owned, and as a result of consumption, the user will no longer own it.
     * This method may block or take long to return. Do not call from the UI thread.
     * For that, see {@link #consumeAsync}.
     *
     * @param itemInfo The PurchaseInfo that represents the item to consume.
     * @throws GMPException if there is a problem during consumption.
     */
    void consume(PurchaseInfo itemInfo) throws GMPException {
        CHECKSETUPDONE("consume");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Consuming started");
        try {
            String token 	= itemInfo.getToken();
            String sku 		= itemInfo.getSku();
            if (token == null || token.equals("")) {
               ERR(TAG,"Can't consume "+ sku + ". No token.");
			   LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Can't consume "+ sku + ". No token.");
               throw new GMPException(IABHELPER_MISSING_TOKEN, "PurchaseInfo is missing token for sku: "
                   + sku + " " + itemInfo);
            }

            DBG(TAG,"Consuming sku: " + sku + ", token: " + token);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Consuming sku: " + sku + ", token: " + token);
            int response = mService.consumePurchase(3, mContext.getPackageName(), token);
            if (response == BILLING_RESPONSE_RESULT_OK) {
               DBG(TAG,"Successfully consumed sku: " + sku);
			   LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Successfully consumed sku: " + sku);
            }
            else {
               ERR(TAG,"Error consuming consuming sku " + sku + ". " + getResponseDesc(response));
			   LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error consuming consuming sku " + sku + ". " + getResponseDesc(response));
               throw new GMPException(response, "Error consuming sku " + sku);
            }
        }
        catch (RemoteException e) {
            throw new GMPException(IABHELPER_REMOTE_EXCEPTION, "Remote exception while consuming. PurchaseInfo: " + itemInfo, e);
        }
    }
	
	class ConsumeProducts extends IABAsyncTask
	{
		IABCallBack mCB = null;
		public ConsumeProducts(IABCallBack cb) { mCB = cb; }
		
		protected Integer doInBackground(final Bundle bundle) 
		{
			try
			{
				FLAGSTARTASYNC("consume");
			}
			catch(Exception exc)
			{
				new Thread( new Runnable()
				{
					public void run()
					{
						DBG(TAG,"Cannot consume the item, another AsyncTask is in place, retrying after 5 seconds");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Cannot consume the item, another AsyncTask is in place, retrying after 5 seconds");
						try { Thread.sleep(5*1000); }catch(Exception e) {}
						new ConsumeProducts(mCB).execute(bundle, ((Activity)SUtils.getContext()));
					}
				}
				#if !RELEASE_VERSION
					,"Retry_ConsumeProducts"
				#endif
					).start();
				
				return new Integer(0);
			}
			
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Consuming async task");	
			try
			{
				JSONObject jobj = new JSONObject(bundle.getString(KEY_PRODUCTS_ARRAY));
				JSONArray jarray = jobj.optJSONArray(KEY_PRODUCTS_ARRAY);
				bundle.clear();
				if (jarray == null || jarray.length() == 0)
				{
					ERR(TAG,"Invalid Purchase Info array to consume");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Invalid Purchase Info array to consume");	
					bundle.putInt(KEY_RSLT_RESPONSE,IABHELPER_UNKNOWN_ERROR);
				#if !RELEASE_VERSION
					bundle.putString(KEY_RSLT_MESSAGE, "Invalid Purchase Info array to consume");
				#endif
					flagEndAsync();	
					if (mCB != null) mCB.runCallBack(bundle);
					return new Integer(0);
				}
				int i = 0;
				JDUMP(TAG,jobj.toString());
				//for (i = 0; i < jarray.length(); i++)//Only handles one at a time
				{
					PurchaseInfo purchase = new PurchaseInfo(jarray.getJSONObject(i).toString(), null);
					
					try{
						consume(purchase);
						bundle.putInt(KEY_RSLT_RESPONSE,BILLING_RESPONSE_RESULT_OK);
					#if !RELEASE_VERSION
						bundle.putString(KEY_RSLT_MESSAGE, "Successful consume of sku " + purchase.getSku());
					#endif
					}
					catch (GMPException e) 
					{
						DBG_EXCEPTION(e);
						GMPResult result = e.getResult();
						bundle.putInt(KEY_RSLT_RESPONSE, result.getResponse());
					#if !RELEASE_VERSION
						bundle.putString(KEY_RSLT_MESSAGE, result.getMessage());
					#endif
					}
				}
			}catch (JSONException e) { DBG_EXCEPTION(e);}
			flagEndAsync();	
			if (mCB != null) mCB.runCallBack(bundle);
			return new Integer(0);
		}
	}

	public void launchConsumeProductRequest(Activity context, PurchaseInfo pi, IABCallBack cb)
	{
		DBG(TAG, "[launchConsumeProductRequest]");
		try
		{
			JDUMP(TAG,pi.toString());
			ServerInfo si = GMPHelper.getInstance().getServerInfo();
			String managed = si.getItemAttribute(pi.getSku(), InAppBilling.GET_STR_CONST(IAB_MANAGED));
			
			if (TextUtils.isEmpty(managed))
			{
				managed = si.getItemAttribute(si.getItemIdByUID(pi.getSku()), InAppBilling.GET_STR_CONST(IAB_MANAGED));
			}
			
			if (!TextUtils.isEmpty(managed) && managed.equals("0") && !TextUtils.isEmpty(pi.getToken()))
			{	
				JSONObject pijson = new JSONObject(pi.toString());
				JSONObject jobj = new JSONObject();
				jobj.put(KEY_PRODUCTS_ARRAY, pijson);
				jobj.accumulate(KEY_PRODUCTS_ARRAY, pijson);
				
				Bundle bundle = new Bundle();
				bundle.putString(KEY_PRODUCTS_ARRAY, jobj.toString());
				new ConsumeProducts(cb).execute(bundle, context);
			}
			else if (!TextUtils.isEmpty(managed) && managed.equals("1"))
			{
				INFO(TAG, "Item cannot be consumed becouse is a no consumable one.");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Item cannot be consumed becouse is a no consumable one.");
			}
			
		}catch (JSONException e) { DBG_EXCEPTION(e);}
	}
	
	
	class QueryPurchases extends IABAsyncTask
	{
		IABCallBack mCB = null;
		public QueryPurchases(IABCallBack cb) { mCB = cb; }
		
		protected Integer doInBackground(final Bundle bundle) 
		{
			try
			{
				CHECKSETUPDONE("queryPurchases");
				FLAGSTARTASYNC("queryPurchases");
			}
			catch(Exception exc)
			{
				new Thread( new Runnable()
				{
					public void run()
					{
						DBG(TAG,"Another QueryPurchase is in place, retrying after 30 seconds");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Another QueryPurchase is in place, retrying after 30 seconds");
						try { Thread.sleep(5*1000); }catch(Exception e) {}
						GMPUtils utils = GMPUtils.getInstance();
						utils.launchQueryPurchases(((Activity)SUtils.getContext()),mCB);
					}
				}
				#if !RELEASE_VERSION
					,"Retry_QueryPurchases"
				#endif
					).start();
				
				return new Integer(0);
			}
			
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "queryPurchases async task");
			DBG(TAG,"Querying owned items...");
			
			boolean verificationFailed = false;
			String continueToken = null;
			bundle.clear();
			do {
				DBG(TAG,"Calling getPurchases with continuation token: " + continueToken);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Calling getPurchases with continuation token: " + continueToken);	
				Bundle ownedItems = null;
				try
				{
					ownedItems = mService.getPurchases(3, mContext.getPackageName(), ITEM_TYPE_INAPP, continueToken);
				}catch (RemoteException e)
				{
					ERR(TAG,"Error Remote Exception at getPurchase service request");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error Remote Exception at getPurchase service request");	
					DBG_EXCEPTION(e);
					flagEndAsync();
					return new Integer(0);
				}

				int response = getResponseCodeFromBundle(ownedItems);
				DBG(TAG,"Owned items response: " + String.valueOf(response));
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Owned items response: " + String.valueOf(response));	
				if (response != BILLING_RESPONSE_RESULT_OK) {
					DBG(TAG,"getPurchases() failed: " + getResponseDesc(response));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "getPurchases() failed: " + getResponseDesc(response));	
					flagEndAsync();
					return new Integer(0);
				}
				if (!ownedItems.containsKey(RESPONSE_INAPP_ITEM_LIST) || 
					!ownedItems.containsKey(RESPONSE_INAPP_PURCHASE_DATA_LIST) || 
					!ownedItems.containsKey(RESPONSE_INAPP_SIGNATURE_LIST)) 
				{
					ERR(TAG,"Bundle returned from getPurchases() doesn't contain required fields.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Bundle returned from getPurchases() doesn't contain required fields.");	
					flagEndAsync();
					return new Integer(0);
				}

				ArrayList<String> ownedSkus = ownedItems.getStringArrayList(RESPONSE_INAPP_ITEM_LIST);
				ArrayList<String> purchaseDataList = ownedItems.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
				ArrayList<String> signatureList = ownedItems.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST);

				for (int i = 0; i < purchaseDataList.size(); ++i) 
				{
					String purchaseData = purchaseDataList.get(i);
					String signature = signatureList.get(i);
					String sku = ownedSkus.get(i);
					
					PurchaseInfo purchase = s.vp(purchaseData, signature);

					// Verify signature (Security.verifyPurchase) 
					if (purchase == null)
					{	
						ERR(TAG,"Purchase signature verification **FAILED**. Not adding item.");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Purchase signature verification **FAILED**. Not adding item.");	
						JDUMP(TAG,purchaseData);
						JDUMP(TAG,signature);
						verificationFailed = true;
					}
					else
					{
						DBG(TAG,"Sku is owned: " + sku);
/*						
						try{
							consume(purchase);//temporal
						}
						catch (GMPException e) 
						{
							DBG_EXCEPTION(e);
						}
*/
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Sku is owned: " + sku);	
						
						if (TextUtils.isEmpty(purchase.getToken())) {
							ERR(TAG,"BUG: empty/null token!");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "BUG: empty/null token!");
							DBG(TAG,"Purchase data: " + purchaseData);
						}

						bundle.putInt(KEY_RSLT_RESPONSE,BILLING_RESPONSE_RESULT_OK);
					#if !RELEASE_VERSION
						bundle.putString(KEY_RSLT_MESSAGE, "Success");
					#endif
						bundle.putString(GMPUtils.KEY_PCHS_JSON, purchaseData);
						bundle.putString(GMPUtils.KEY_PCHS_SIGNATURE, signature);
						if (mCB != null) mCB.runCallBack(bundle);
					}
				}
				continueToken = ownedItems.getString(INAPP_CONTINUATION_TOKEN);
				DBG(TAG,"Continuation token: " + continueToken);
			} while (!TextUtils.isEmpty(continueToken));

			flagEndAsync();
			return new Integer(0);
		}
	}

	public void launchQueryPurchases(Activity context, IABCallBack cb)
	{
		DBG(TAG, "[launchQueryPurchases]");
		new QueryPurchases(cb).execute(new Bundle(), context);
	}
	/**
     * Returns a human-readable description for the given response code.
     *
     * @param code The response code
     * @return A human-readable string explaining the result code.
     *     It also includes the result code numerically.
     */
    public String getResponseDesc(int code) 
	{
	#if !RELEASE_VERSION
        String[] iab_msgs = ("0:OK/1:User Canceled/2:Unknown/" +
                "3:Billing Unavailable/4:Item unavailable/" +
                "5:Developer Error/6:Error/7:Item Already Owned/" +
                "8:Item not owned").split("/");
        String[] iabhelper_msgs = ("0:OK/-1001:Remote exception during initialization/" +
                                   "-1002:Bad response received/" +
                                   "-1003:Purchase signature verification failed/" +
                                   "-1004:Send intent failed/" +
                                   "-1005:User cancelled/" +
                                   "-1006:Unknown purchase response/" +
                                   "-1007:Missing token/" +
                                   "-1008:Unknown error").split("/");

        if (code <= IABHELPER_ERROR_BASE) {
            int index = IABHELPER_ERROR_BASE - code;
            if (index >= 0 && index < iabhelper_msgs.length) return iabhelper_msgs[index];
            else return String.valueOf(code) + ":Unknown IAB Helper Error";
        }
        else if (code < 0 || code >= iab_msgs.length)
            return String.valueOf(code) + ":Unknown";
        else
            return iab_msgs[code];
	#else
		return "";
	#endif	
    }
	
	// Checks that setup was done; if not, throws an exception.
	void checkSetupDone(String operation) {
		if (!mSetupDone) {
			ERR(TAG,"Illegal state for operation (" + operation + "): GMPUtils is not set yet.");
			throw new IllegalStateException("IAB helper is not set yet. Can't perform operation: " + operation);
		}
	}

	// Workaround to bug where sometimes response codes come as Long instead of Integer
	int getResponseCodeFromBundle(Bundle b) {
		Object o = b.get(RESPONSE_CODE);
		if (o == null) {
			DBG(TAG,"Bundle with null response code, assuming OK (known issue)");
			return BILLING_RESPONSE_RESULT_OK;
		}
		else if (o instanceof Integer) return ((Integer)o).intValue();
		else if (o instanceof Long) return (int)((Long)o).longValue();
		else {
			ERR(TAG,"Unexpected type for bundle response code.");
			ERR(TAG,o.getClass().getName());
			throw new RuntimeException("Unexpected type for bundle response code: " + o.getClass().getName());
		}
	}

	// Workaround to bug where sometimes response codes come as Long instead of Integer
	int getResponseCodeFromIntent(Intent i) {
		Object o = i.getExtras().get(RESPONSE_CODE);
		if (o == null) {
			ERR(TAG,"Intent with no response code, assuming OK (known issue)");
			return BILLING_RESPONSE_RESULT_OK;
		}
		else if (o instanceof Integer) return ((Integer)o).intValue();
		else if (o instanceof Long) return (int)((Long)o).longValue();
		else {
			ERR(TAG,"Unexpected type for intent response code.");
			ERR(TAG,o.getClass().getName());
			throw new RuntimeException("Unexpected type for intent response code: " + o.getClass().getName());
		}
	}

	void flagStartAsync(String operation) {
		if (mAsyncInProgress) throw new IllegalStateException("Can't start new async operation (" +
				operation + ") because another async operation(" + mAsyncOperation + ") is in progress.");
		mAsyncOperation = operation;
		mAsyncInProgress = true;
		DBG(TAG,"Starting async operation: " + operation);
	}

	void flagEndAsync() {
		DBG(TAG,"Ending async operation: " + mAsyncOperation);
		mAsyncOperation = "";
		mAsyncInProgress = false;
	}
	
}