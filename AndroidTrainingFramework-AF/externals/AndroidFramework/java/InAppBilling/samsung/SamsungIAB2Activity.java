#if USE_IN_APP_BILLING && SAMSUNG_STORE
package APP_PACKAGE.iab;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import APP_PACKAGE.R;
import APP_PACKAGE.iab.SamsungHelper;
import APP_PACKAGE.iab.SamsungIAB2Utils;
import APP_PACKAGE.iab.ErrorVO;
import APP_PACKAGE.iab.ItemVO;
import APP_PACKAGE.iab.PurchaseVO;
import APP_PACKAGE.iab.VerificationVO;

public class SamsungIAB2Activity extends Activity
{
	
	// Debugging TAG
    // ========================================================================
	SET_TAG("InAppBilling");
	// ========================================================================
	
	
	// Communication helper between IAPService and Application
    // ========================================================================
    private SamsungIAB2Utils  	mSamsungIAB2Utils  = null;
    // ========================================================================
	
	
	//ProgressDialog for waiting message
	// ========================================================================
	private ProgressDialog		mProgressDialog   = null;
	// ========================================================================
	
	
	// AsyncTask for Get Item List
    // ========================================================================
    private GetItemListTask   	mGetItemListTask   = null;
	// ========================================================================
	
	
	// AsyncTask for Purchase Item
	// ========================================================================
	private PurchaseItemTask     mPurchaseItemTask = null;
	// ========================================================================
	
	
	// AsyncTask for Restore Transaction
	// ========================================================================
	private RestoreTransactionTask     mRestoreTransactionTask = null;
	// ========================================================================
	
	
	// Verify payment result by server
    // ========================================================================
    private VerifyClientToServerTask mVerifyClientToServerTask = null;
    // ========================================================================
	
	
	//Item information
	// ========================================================================
	private String		mItemId       	= null;
	private String  	mItemGroupId    = null;
	static String  		mPurchaseId    	= null;
	static String 		mPaymentId		= null;

	
	/* Item Type
	 *  Consumable      		: 00
	 *  Non Consumable  	: 01
	 *  Subscription     	: 02
	 *  All             		: 10
	 */
	// ========================================================================
	
	//Get PurchaseId from billing receipt.
	// ========================================================================
	public static String getPurchaseId() {return mPurchaseId;}
    // ========================================================================
	//Get PurchaseId from billing receipt.
	// ========================================================================
    public static String getPaymentId() {return mPaymentId;}
	// ========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		INFO(TAG, "[onCreate]");
		super.onCreate(savedInstanceState);
		mPurchaseId = null;
		mPaymentId = null;

        //    Store Item Id and Group Id passed by Intent
        // ====================================================================
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            mItemId 		= extras.getString("SamsungItemId");	DBG(TAG, "SamsungItemId: " + mItemId);
			mItemGroupId 	= extras.getString("ItemGroupId");		DBG(TAG, "ItemGroupId: " + mItemGroupId);
        }
        // ====================================================================
        
		
        //    Create SamsungIAB2Utils Instance
        // ====================================================================
        mSamsungIAB2Utils = new SamsungIAB2Utils(this);
        // ====================================================================
        
		
        //    SamsungAccount authentication process
        // ====================================================================
        if(mSamsungIAB2Utils.isInstalledIapPackage(this))
        {
            // If IAP package installed in your device is valid
            // ----------------------------------------------------------------
            if(mSamsungIAB2Utils.isValidIapPackage(this))
			{
                mProgressDialog = showProgressDialog( this );
				mSamsungIAB2Utils.startAccountActivity( this );
			}
            // ----------------------------------------------------------------
            // If IAP package installed in your device is not valid
            // ----------------------------------------------------------------            
            else
			{
				ERR(TAG, "IAP Application installed in your device is not valid!!");
				
				if(SamsungHelper.isRestoreTransaction)
					ResultRestoreCallBack(IAB_BUY_CANCEL, null, true);
				else
					ResultCallBack(IAB_BUY_CANCEL, true);
			}
            // ----------------------------------------------------------------  
        }
        else
			mSamsungIAB2Utils.installIapPackage( this );
        // ====================================================================
    }
    
    
	/**
	 * InitGetItemListService()
	 * 
	 * bind IAPService. If IAPService properly bound,
	 * GetItemListTask() method is called to get item list.
	 */
    public void InitGetItemListService()
    {
		INFO(TAG, "[InitGetItemListService]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[InitGetItemListService]");
		
		// TEST SUCESS MODE:	1
		// TEST FAIL MODE: 	         -1
		// COMMERCIAL MODE:         0
		// ====================================================================
		#if RELEASE_VERSION
			mSamsungIAB2Utils.setMode( 0 );
		#else
			mSamsungIAB2Utils.setMode( 1 );
		#endif
		// ====================================================================
		
		
		//    bind IAPService
		// ====================================================================
		mSamsungIAB2Utils.bindIapService(new SamsungIAB2Utils.OnIapBindListener()
		{
			@Override
			public void onBindIapFinished( int result )
			{
				//    If successfully bound IAPService
				// ============================================================
				if (result == SamsungIAB2Utils.IAP_RESPONSE_RESULT_OK)
				{
					// Get item list task
					// --------------------------------------------------------
					GetItemListTask();
					// --------------------------------------------------------
				}
				// ============================================================
				//    If IAPService is not bound correctly
				// ============================================================
				else
				{
					ERR(TAG, "In-app Purchase Service Bind Failed.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "In-app Purchase Service Bind Failed.");
					ResultCallBack(IAB_BUY_CANCEL, true);
				}
				// ============================================================
			}
		});
		// ====================================================================
    }
	
	
	 /**
	 * InitPurchaseService() 
	 * 
	 * bind IAPService. If IAPService properly bound,
	 * InitPurchaseTask() method is called to purchase item.
	 */
	public void InitPurchaseService()
    {
		INFO(TAG, "[InitPurchaseService]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[InitPurchaseService]");
		
		// TEST SUCESS MODE:	1
		// TEST FAIL MODE: 	         -1
		// COMMERCIAL MODE:         0
        // ====================================================================
		#if RELEASE_VERSION
			mSamsungIAB2Utils.setMode( 0 );
		#else
			mSamsungIAB2Utils.setMode( 1 );
		#endif
        // ====================================================================
        
		
        //    bind to IAPService
        // ====================================================================
        mSamsungIAB2Utils.bindIapService(new SamsungIAB2Utils.OnIapBindListener()
        {
            @Override
            public void onBindIapFinished( int result )
            {
                //    If successfully bound IAPService
                // ============================================================
                if ( result == SamsungIAB2Utils.IAP_RESPONSE_RESULT_OK )
                {
					// Purchase item task
                    // --------------------------------------------------------
					PurchaseItemTask();
                    // --------------------------------------------------------
                }
                // ============================================================
                //    If IAPService is not bound correctly
                // ============================================================
                else
                {
					ERR(TAG, "In-app Purchase Service Bind failed.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "In-app Purchase Service Bind failed.");
					ResultCallBack(IAB_BUY_CANCEL, true);
                }
                // ============================================================
            }
        });
        // ====================================================================
    }
	
	
	/**
	 * InitRestoreTransactionService
	 * 
	 * bind IAPService. If IAPService properly bound,
	 * InitPurchaseTask() method is called to purchase item.
	 */
    public void InitRestoreTransactionService()
    {
        INFO(TAG, "[InitRestoreTransactionService]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[InitRestoreTransactionService]");
		
		// TEST SUCESS MODE:	1
		// TEST FAIL MODE: 	         -1
		// COMMERCIAL MODE:         0
		// ====================================================================
		#if RELEASE_VERSION
			mSamsungIAB2Utils.setMode( 0 );
		#else
			mSamsungIAB2Utils.setMode( 1 );
		#endif
		// ====================================================================
        
		
        //    bind to IAPService
        // ====================================================================
        mSamsungIAB2Utils.bindIapService(new SamsungIAB2Utils.OnIapBindListener()
        {
            @Override
            public void onBindIapFinished( int result )
            {
                //    If successfully bound IAPService
                // ============================================================
                if ( result == SamsungIAB2Utils.IAP_RESPONSE_RESULT_OK )
                {
                    // Restore Transaction Task
                    // --------------------------------------------------------
                    RestoreTransactionTask();
                    // --------------------------------------------------------
                }
                // ============================================================
                //    If IAPService is not bound correctly
                // ============================================================
                else
                {
					ERR(TAG, "In-app Purchase Service Bind failed.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "In-app Purchase Service Bind failed.");
					ResultCallBack(IAB_BUY_CANCEL, true);
                }
                // ============================================================
            }
        });
        // ====================================================================
    }
    
    
	/**
	 * Execute GetItemListTask
	 */
    private void GetItemListTask()
    {
        INFO(TAG, "[GetItemListTask]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[GetItemListTask]");
		try
        {
            if( mGetItemListTask != null && mGetItemListTask.getStatus() != Status.FINISHED )
                mGetItemListTask.cancel( true );

            mGetItemListTask = new GetItemListTask( this );
            mGetItemListTask.execute();
        }
        catch( RejectedExecutionException e )
        {
            ERR(TAG, "RejectedExecutionException: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "RejectedExecutionException: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
        catch( Exception e )
        {
            ERR(TAG, "Exception: " + e.getMessage() );
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
    }

	/**
	 * AsyncTask to load a list of items
	 */
    private class GetItemListTask extends AsyncTask<String, Object, Boolean>
    {
        private Context           mContext          = null;
        private ArrayList<ItemVO> mMoreItemVOList   = null;
        private ErrorVO           mErrorVO          = new ErrorVO();
        
        public GetItemListTask( Context _context )
        {
            mContext = _context;
        }
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mMoreItemVOList = new ArrayList<ItemVO>();
        }

        @Override
        protected Boolean doInBackground( String... params )
        {
            try
            {
                // call getItemList() method of IAPService
                // ============================================================
                Bundle bundle = mSamsungIAB2Utils.getItemList( mItemGroupId, SamsungIAB2Utils.ITEM_TYPE_ALL );
                // ============================================================
                
                // status Code, error String, extra String
                // save status code, error string android extra String.
                // ============================================================
                mErrorVO.setErrorCode( bundle.getInt(SamsungIAB2Utils.KEY_NAME_STATUS_CODE));
                mErrorVO.setErrorString( bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING));              
                mErrorVO.setExtraString( bundle.getString(SamsungIAB2Utils.KEY_NAME_IAP_UPGRADE_URL));
                // ============================================================
                
                if( mErrorVO.getErrorCode() == SamsungIAB2Utils.IAP_ERROR_NONE )
                {
                    ArrayList<String> itemStringList = bundle.getStringArrayList(SamsungIAB2Utils.KEY_NAME_RESULT_LIST);
                    
                    if( itemStringList != null )
                    {
                        for( String itemString : itemStringList )
                        {
							ItemVO itemVO = new ItemVO( itemString );
                            mMoreItemVOList.add( itemVO );
							
							DBG(TAG, "**********************************");
							DBG(TAG, "ITEM ID: " + itemVO.getItemId());
							DBG(TAG, "ITEM TYPE: " + itemVO.getType());
							DBG(TAG, "ITEM NAME: " + itemVO.getItemName());
							DBG(TAG, "ITEM PRICE: " + itemVO.getItemPrice());
							DBG(TAG, "ITEM PRICE STR: " + itemVO.getItemPriceString());
							DBG(TAG, "ITEM DESC: " + itemVO.getItemDesc());
							DBG(TAG, "**********************************");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "ITEM ID: " + itemVO.getItemId() +" ITEM TYPE: " + itemVO.getType());
                        }
                    }
                    else
					{
                        WARN(TAG, "Bundle Value 'RESULT_LIST' is null" );
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Bundle Value 'RESULT_LIST' is null");
					}
                }
                else
                {
					if( !TextUtils.isEmpty( bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING)))
					{
						WARN(TAG, "GetItemListTask MESSAGE: " + bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING));
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "GetItemListTask MESSAGE: " + bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING).replaceAll("\n"," "));
					}
                    return false;
                }
            }
            catch( Exception e )
            {
				ERR(TAG,"GetItemListTask Exception: " + e.getMessage());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "GetItemListTask Exception: " + e.getMessage());
                return false;
            }
            
            return true;
        }

        @Override
        protected void onPostExecute( Boolean result )
        {
            if(result)
            {
                try
                {
                    // If there is Item List passed from IAPService
                    // ========================================================
                    if( mMoreItemVOList != null && mMoreItemVOList.size() > 0)
                    {
						DBG(TAG, "Item list OK!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Item list OK!");
						InitPurchaseService();
                    }
                    // ========================================================
                    // If there is no Item List passed from IAPService
                    // ========================================================
                    else
                    {
                        WARN(TAG, "Item list EMPTY!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Item list EMPTY!");
						if( false == TextUtils.isEmpty(mErrorVO.getErrorString()))
						{
							WARN(TAG, "Message: " + mErrorVO.getErrorString());
							LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Message: " + mErrorVO.getErrorString().replaceAll("\n"," "));
						}
							
						ResultCallBack(IAB_BUY_CANCEL, true);
                    }
                    // ========================================================
                }
                catch( Exception e )
                {
					ERR(TAG,"Exception: " + e.getMessage());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
					ResultCallBack(IAB_BUY_CANCEL, true);
                }             
            }
            else
            {
                // dismiss progress dialog for loading Item List
				// ================================================================
				dismissProgressDialog( mProgressDialog );
				// ================================================================
				
				//    If the IAP application needs to be upgraded
                // ============================================================
                if(mErrorVO.getErrorCode() == SamsungIAB2Utils.IAP_ERROR_NEED_APP_UPGRADE)
                {
					DBG(TAG, "Samsung In-App Purchase upgrade required message");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Samsung In-App Purchase upgrade required message");
					Runnable OkBtnRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Intent intent = new Intent();
                            intent.setData(Uri.parse(mErrorVO.getExtraString()));
                            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
							ResultCallBack(IAB_BUY_CANCEL, false);

                            try
                            {
                                startActivity(intent);
                            }
                            catch( ActivityNotFoundException e )
                            {
								ERR(TAG,"ActivityNotFoundException: " + e.getMessage());
								LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ActivityNotFoundException: " + e.getMessage());
                            }
                        }
                    };
                    
                    mSamsungIAB2Utils.showIapDialog(SamsungIAB2Activity.this,
                             getString( R.string.IAP_SAMSUNG_DIALOG_TITLE), 
                             getString( R.string.IAP_SAMSUNG_UPGRADE_REQUIRED),
                             true,
                             OkBtnRunnable,
							 true);
                }
                // ============================================================
                //    If the IAPService failed to initialize
                // ============================================================
                else
                {
					if( false == TextUtils.isEmpty( mErrorVO.getErrorString()))
					{
						ERR(TAG, "GetItemList ERROR: " + mErrorVO.getErrorString());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "GetItemList ERROR: " + mErrorVO.getErrorString().replaceAll("\n"," "));
						#if !RELEASE_VERSION
						Toast.makeText(SamsungIAB2Activity.this, mErrorVO.getErrorString(), Toast.LENGTH_SHORT ).show();
						#endif
					}
					ResultCallBack(IAB_BUY_CANCEL, true);
                }
            }
        }
    }
    
    
	/**
	 * Execute PurchaseItemTask
	 */
    private void PurchaseItemTask()
    {
        INFO(TAG, "[PurchaseItemTask]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[PurchaseItemTask]");
		try
        {
            if( mPurchaseItemTask != null && mPurchaseItemTask.getStatus() != Status.FINISHED )
                mPurchaseItemTask.cancel( true );

            mPurchaseItemTask = new PurchaseItemTask();
            mPurchaseItemTask.execute();
        }
        catch( RejectedExecutionException e )
        {
            ERR( TAG, "RejectedExecutionException: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "RejectedExecutionException: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
        catch( Exception e )
        {
            ERR( TAG, "Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
    }
    
    
	/**
	 * AsyncTask for purchase item
	 */
    private class PurchaseItemTask  extends AsyncTask<String, Object, Boolean>
    {
        private ErrorVO mErrorVO = new ErrorVO();
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground( String... params )
        {
            try
            {
                // Initialize IAPService by calling init() method of IAPService
                // ============================================================
                mErrorVO = mSamsungIAB2Utils.init();
                // ============================================================
                return true;
            }
            catch( Exception e )
            {
                ERR(TAG,"Exception: " + e.getMessage());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute( Boolean result )
        {
            //    PurchaseItemTask returned true
            // ================================================================
            if( true == result )
            {
                //    If initialization is completed successfully
                // ============================================================
                if( mErrorVO.getErrorCode() == SamsungIAB2Utils.IAP_ERROR_NONE )
                {
					DBG(TAG, "*** Samsung Account Authentication was successful ***");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Samsung Account Authentication was successful");
                    //    go to PaymentMethodListActivity
                    // --------------------------------------------------------
					mSamsungIAB2Utils.startPurchase(SamsungIAB2Activity.this, SamsungIAB2Utils.REQUEST_CODE_IS_IAP_PAYMENT, mItemGroupId, mItemId);
                    // --------------------------------------------------------
                }
                // ============================================================
                //    If the IAP package needs to be upgraded
                // ============================================================
                else if( mErrorVO.getErrorCode() == SamsungIAB2Utils.IAP_ERROR_NEED_APP_UPGRADE )
                {
                    DBG(TAG, "Samsung In-App Purchase upgrade required message");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Samsung In-App Purchase upgrade required message");
					
					//    dismiss progress dialog for IAPService Initialization
					// ================================================================
					dismissProgressDialog( mProgressDialog );
					// ================================================================
				
					Runnable OkBtnRunnable = new Runnable()
                    {
                        
                        @Override
                        public void run()
                        {
                            Intent intent = new Intent();
                            intent.setData(Uri.parse( mErrorVO.getExtraString()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							ResultCallBack(IAB_BUY_CANCEL, true);

                            try
                            {
                                SamsungIAB2Activity.this.startActivity(intent);
                            }
                            catch( ActivityNotFoundException e )
                            {
								ERR(TAG,"Exception: " + e.getMessage());
								LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
                            }
                        }
                    };
                    
                    mSamsungIAB2Utils.showIapDialog(SamsungIAB2Activity.this,
                             getString(R.string.IAP_SAMSUNG_DIALOG_TITLE),
                             getString(R.string.IAP_SAMSUNG_UPGRADE_REQUIRED),
                             true,
                             OkBtnRunnable,
							 true);
                }
				else
				{
					if( false == TextUtils.isEmpty( mErrorVO.getErrorString()))
					{
						ERR(TAG, "PurchaseItemTask ERROR: " + mErrorVO.getErrorString());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "PurchaseItemTask ERROR: " + mErrorVO.getErrorString().replaceAll("\n"," "));
						#if !RELEASE_VERSION
						Toast.makeText(SamsungIAB2Activity.this, mErrorVO.getErrorString(), Toast.LENGTH_SHORT ).show();
						#endif
					}
					ResultCallBack(IAB_BUY_CANCEL, true);
				}
                // ============================================================
            }
            // ================================================================
            // PurchaseItemTask returned false
            // ================================================================
            else
            {
				if( false == TextUtils.isEmpty( mErrorVO.getErrorString()))
				{
					ERR(TAG, "PurchaseItemTask Init ERROR: " + mErrorVO.getErrorString());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "PurchaseItemTask Init ERROR: " + mErrorVO.getErrorString().replaceAll("\n"," "));
					#if !RELEASE_VERSION
					Toast.makeText(SamsungIAB2Activity.this, mErrorVO.getErrorString(), Toast.LENGTH_SHORT ).show();
					#endif
				}
				ResultCallBack(IAB_BUY_CANCEL, true);
            }
            // ================================================================
        }
    }
	
	 /**
	 * Execute RestoreTransactionTask
	 */
    private void RestoreTransactionTask()
    {
        INFO(TAG, "[RestoreTransactionTask]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[RestoreTransactionTask]");
		try
        {
            if ( mRestoreTransactionTask != null && mRestoreTransactionTask.getStatus() != Status.FINISHED )
                mRestoreTransactionTask.cancel( true );

            mRestoreTransactionTask = new RestoreTransactionTask(this);
            mRestoreTransactionTask.execute();
        }
        catch( RejectedExecutionException e )
        {
            ERR( TAG, "RejectedExecutionException: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "RejectedExecutionException: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
        catch( Exception e )
        {
            ERR( TAG, "Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
    }
    
    
	/**
	 * AsyncTask for Restore Transaction
	 */
    private class RestoreTransactionTask extends AsyncTask<String, Object, Boolean>
    {
        private Context 			mContext         = null;
        private ArrayList<InBoxVO>  mMoreInboxVOList = null;
		private ErrorVO           	mErrorVO         = new ErrorVO();
		private int 				mFirstItemToDisplay	 	= SamsungIAB2Utils.FIRST_ITEM_TO_DISPLAY;
		private int 				mMaxItemsToDisplay		= SamsungIAB2Utils.MAX_ITEMS_TO_DISPLAY;
        
        public RestoreTransactionTask(Context _context)
        {
            mContext = _context;
        }
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mMoreInboxVOList = new ArrayList<InBoxVO>();
        }

        @Override
        protected Boolean doInBackground( String... params )
        {
            try
            {
                // Initialize IAPService by calling init() method of IAPService
                // ============================================================
                mErrorVO = mSamsungIAB2Utils.init();
                // ============================================================
				
				if( mErrorVO.getErrorCode() == SamsungIAB2Utils.IAP_ERROR_NONE )
                {
					while(mFirstItemToDisplay > 0)
					{
						// Call getItemsInbox() method of IAPService
						// ============================================================
						Bundle bundle = mSamsungIAB2Utils.getItemsInbox( mItemGroupId, mFirstItemToDisplay, mFirstItemToDisplay + mMaxItemsToDisplay);
						// ============================================================
						
						// status Code, error String, extra String
						// save status code, error string android extra String.
						// ============================================================
						mErrorVO.setErrorCode( bundle.getInt(SamsungIAB2Utils.KEY_NAME_STATUS_CODE));
						mErrorVO.setErrorString( bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING));              
						mErrorVO.setExtraString( bundle.getString(SamsungIAB2Utils.KEY_NAME_IAP_UPGRADE_URL));
						// ============================================================
						
						if ( bundle.getInt( mSamsungIAB2Utils.KEY_NAME_STATUS_CODE ) == mSamsungIAB2Utils.IAP_ERROR_NONE )
						{
							ArrayList<String> purchaseItemStringList = bundle.getStringArrayList( mSamsungIAB2Utils.KEY_NAME_RESULT_LIST );
							ArrayList<cItem> items = InAppBilling.mServerInfo.getShopProfile().getItemList();
						
							if( purchaseItemStringList != null && !(purchaseItemStringList.isEmpty()))
							{
								for( String itemString : purchaseItemStringList )
								{
									InBoxVO inboxVO = new InBoxVO( itemString );
									DBG(TAG, "** SAMSUNG ID: " + inboxVO.getItemId() + " ITEMS PURCHASED: "+mFirstItemToDisplay);
									LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "SAMSUNG ID: " + inboxVO.getItemId() + " ITEMS PURCHASED: "+mFirstItemToDisplay);
									mFirstItemToDisplay++;
									
									if(inboxVO.getType().equalsIgnoreCase(SamsungIAB2Utils.ITEM_TYPE_NON_CONSUMABLE) || inboxVO.getType().equalsIgnoreCase(SamsungIAB2Utils.ITEM_TYPE_SUBSCRIPTION))
									{
										for (int x = 0; x < items.size(); x++)
										{
											cItem tmpItem = items.get(x);
											cBilling tmpBilling = tmpItem.getDefaultBilling();
											
											if (inboxVO.getItemId().equalsIgnoreCase(tmpBilling.getAttributeByName("samsung_item_id")))
											{
												DBG(TAG, "**********************************");
												DBG(TAG, "ITEM ID: " + tmpItem.getId());
												DBG(TAG, "ITEM SAMSUNG ID: " + inboxVO.getItemId());
												DBG(TAG, "ITEM TYPE: " + inboxVO.getType());
												DBG(TAG, "ITEM NAME: " + inboxVO.getItemName());
												DBG(TAG, "ITEM PRICE STR: " + inboxVO.getItemPriceString());
												DBG(TAG, "ITEM PURCHASE DATE: " + inboxVO.getPurchaseDate());
												DBG(TAG, "**********************************");
												LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "ITEM ID: " + tmpItem.getId()+ " ITEM SAMSUNG ID: " + inboxVO.getItemId()+ " ITEM TYPE: " + inboxVO.getType());
												mMoreInboxVOList.add( inboxVO );
												ResultRestoreCallBack(IAB_BUY_OK, tmpItem.getId(), false, inboxVO.getPaymentId());
											}
										}
									}
								}
							}
							else
							{
								mFirstItemToDisplay = -1;
								WARN(TAG, "Bundle Value 'RESULT_LIST' is null" );
								LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Bundle Value 'RESULT_LIST' is null");
							}
						}
						else
						{
							mFirstItemToDisplay = -1;
							if( !TextUtils.isEmpty( bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING)))
							{
								WARN(TAG, "RestoreTransactionTask MESSAGE: " + bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING));
								LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "RestoreTransactionTask MESSAGE: " + bundle.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING).replaceAll("\n"," "));
							}
							return false;
						}
					}
				}
				else
				{
					if( !TextUtils.isEmpty( mErrorVO.getErrorString()))
					{
						WARN(TAG, "RestoreTransactionTask Init MESSAGE: " + mErrorVO.getErrorString());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "RestoreTransactionTask Init MESSAGE: " + mErrorVO.getErrorString().replaceAll("\n"," "));
					}
					return false;
				}
            }
            catch ( Exception e )
            {
				ERR(TAG,"RestoreTransactionTask Exception: " + e.getMessage());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "RestoreTransactionTask Exception: " + e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute( Boolean result )
        {
            if( true == result )
            {
                try
                {
                    // If there is list of purchased items
                    // ========================================================
                    if( mMoreInboxVOList != null && mMoreInboxVOList.size() > 0 )
                    {
						DBG(TAG, "Restore Transaction Completed!!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Restore Transaction Completed!!");
						dismissProgressDialog(mProgressDialog);
						finish();
                    }
                    // ========================================================
                    // If there is no list of purchased items
                    // ========================================================
                    else
                    {
                        WARN(TAG, "Empty list!!");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Empty list!!");
						ResultRestoreCallBack(IAB_BUY_CANCEL, null, true);
                    }
                    // ========================================================
                }
                catch ( Exception e )
                {
					ERR(TAG,"Exception: " + e.getMessage());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
					ResultRestoreCallBack(IAB_BUY_CANCEL, null, true);
                }             
            }
            else
            {
				// dismiss progress dialog for loading Item List
				// ================================================================
				dismissProgressDialog( mProgressDialog );
				// ================================================================
				
				//    If the IAP application needs to be upgraded
                // ============================================================
                if(mErrorVO.getErrorCode() == SamsungIAB2Utils.IAP_ERROR_NEED_APP_UPGRADE)
                {
					DBG(TAG, "Samsung In-App Purchase upgrade required message");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Samsung In-App Purchase upgrade required message");
					Runnable OkBtnRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Intent intent = new Intent();
                            intent.setData(Uri.parse(mErrorVO.getExtraString()));
                            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
							ResultRestoreCallBack(IAB_BUY_CANCEL, null, true);

                            try
                            {
                                startActivity(intent);
                            }
                            catch( ActivityNotFoundException e )
                            {
								ERR(TAG,"ActivityNotFoundException: " + e.getMessage());
								LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ActivityNotFoundException: " + e.getMessage());
                            }
                        }
                    };
                    
                    mSamsungIAB2Utils.showIapDialog(SamsungIAB2Activity.this,
                             getString( R.string.IAP_SAMSUNG_DIALOG_TITLE), 
                             getString( R.string.IAP_SAMSUNG_UPGRADE_REQUIRED),
                             true,
                             OkBtnRunnable,
							 true);
                }
                // ============================================================
                //    If the IAPService failed to initialize
                // ============================================================
                else
				{
					if( false == TextUtils.isEmpty( mErrorVO.getErrorString()))
					{
						ERR(TAG, "RestoreTransactionTask ERROR: " + mErrorVO.getErrorString());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "RestoreTransactionTask ERROR: " + mErrorVO.getErrorString().replaceAll("\n"," "));
						#if !RELEASE_VERSION
						Toast.makeText(SamsungIAB2Activity.this, mErrorVO.getErrorString(), Toast.LENGTH_SHORT ).show();
						#endif
					}
					ResultRestoreCallBack(IAB_BUY_CANCEL, null, true);
				}
            }
        }
    }
	
    
    private void VerifyClientToServerTask( PurchaseVO _purchaseVO )
    {
        INFO(TAG, "[VerifyClientToServerTask]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[VerifyClientToServerTask]");
		try
        {
            if( mVerifyClientToServerTask != null && mVerifyClientToServerTask.getStatus() != Status.FINISHED )
                mVerifyClientToServerTask.cancel( true );

            mVerifyClientToServerTask = new VerifyClientToServerTask( _purchaseVO );
            mVerifyClientToServerTask.execute();
        }
        catch( RejectedExecutionException e )
        {
            ERR( TAG, "RejectedExecutionException: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "RejectedExecutionException: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
        catch( Exception e )
        {
            ERR( TAG, "Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
			ResultCallBack(IAB_BUY_CANCEL, true);
        }
    }
    
    
	/**
	 * verify purchased result
	 * ※ For a more secure transaction we recommend to verify from your server to IAP server.
	 */
    private class VerifyClientToServerTask  extends AsyncTask<Void, Void, Boolean>
    {
        PurchaseVO       mPurchaseVO      = null;
        VerificationVO   mVerificationVO  = null;
        
        public VerifyClientToServerTask( PurchaseVO _purchasedVO )
        {
            mPurchaseVO = _purchasedVO;
        }
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            
            if( mPurchaseVO == null ||
                true == TextUtils.isEmpty( mPurchaseVO.getVerifyUrl() ) ||
                true == TextUtils.isEmpty( mPurchaseVO.getPurchaseId() ) ||
                true == TextUtils.isEmpty( mPurchaseVO.getPaymentId() ) )
            {
                this.cancel( true );
            }
        }
        
        
        @Override
        protected void onCancelled()
        {
			WARN(TAG,"VerifyClientToServerTask Cancelled");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "VerifyClientToServerTask Cancelled");
			ResultCallBack(IAB_BUY_CANCEL, true);
            super.onCancelled();
        }
        

        @Override
        protected Boolean doInBackground( Void... params )
        {
            try
            {
                StringBuffer strUrl = new StringBuffer();
                strUrl.append( mPurchaseVO.getVerifyUrl() );
                strUrl.append( "&purchaseID=" + mPurchaseVO.getPurchaseId() );
                
                DBG(TAG,"VerifyClientToServerUrl :" + strUrl);
                
                int     retryCount  = 0;
                String  strResponse = null;
                
                do
                {
                    strResponse = getHttpGetData( strUrl.toString(), 10000, 10000);
                    retryCount++;
                }
                while( retryCount < 3 && true == TextUtils.isEmpty(strResponse));
                
                
                if(strResponse == null || TextUtils.isEmpty( strResponse))
                {
                    return false;
                }
                else
                {
                    mVerificationVO = new VerificationVO( strResponse );
                    
                    if( mVerificationVO != null &&
                        true == "true".equals( mVerificationVO.getmStatus() ) &&
                        true == mPurchaseVO.getPaymentId().equals( mVerificationVO.getmPaymentId() ) )
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            }
            catch( Exception e )
            {
				ERR(TAG,"Exception: " + e.getMessage());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute( Boolean result )
        {
            if(result)
            {				
				DBG(TAG, "Payment success \n-itemId : " + mPurchaseVO.getItemId() + "\n-paymentId : " + mPurchaseVO.getPaymentId());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Payment success \n-itemId : " + mPurchaseVO.getItemId() + "\n-paymentId : " + mPurchaseVO.getPaymentId());
				ResultCallBack(IAB_BUY_OK, true);
            }
            else
            {				
				ERR(TAG, "Payment is not valid!! \n-ItemId : " + mPurchaseVO.getItemId() + "\n-paymentId : " + mPurchaseVO.getPaymentId());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Payment is not valid!! \n-ItemId : " + mPurchaseVO.getItemId() + "\n-paymentId : " + mPurchaseVO.getPaymentId());
				ResultCallBack(IAB_BUY_FAIL, true);
            }
        }
        
        private String getHttpGetData(final String _strUrl, final int    _connTimeout, final int    _readTimeout)
        {
            String                  strResult       = null;
            URLConnection           con             = null;
            HttpURLConnection       httpConnection  = null;
            BufferedInputStream     bis             = null; 
            ByteArrayOutputStream   buffer          = null;
            
            try 
            {
				LOGGING_APPEND_REQUEST_PARAM(_strUrl, "GET", "Samsung verify purchased");
				LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("Samsung verify purchased"));
                URL url = new URL( _strUrl );
                con = url.openConnection();
                con.setConnectTimeout(10000);
                con.setReadTimeout(10000);
                
                httpConnection = (HttpURLConnection)con;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
                  
                int responseCode = httpConnection.getResponseCode();

                if( responseCode == 200 )
                {
                    bis = new BufferedInputStream( httpConnection.getInputStream(),
                                                   4096 );
    
                    buffer = new ByteArrayOutputStream( 4096 );             
            
                    byte [] bData = new byte[ 4096 ];
                    int nRead;
                    
                    while( ( nRead = bis.read( bData, 0, 4096 ) ) != -1 )
                    {
                        buffer.write( bData, 0, nRead );
                    }
                    
                    buffer.flush();
                    
                    strResult = buffer.toString();
					LOGGING_APPEND_RESPONSE_PARAM(strResult, "Samsung verify purchased");
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("Samsung verify purchased"));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Samsung verify purchased: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("Samsung verify purchased") +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO("Samsung verify purchased");
                }
            } 
            catch( Exception e ) 
            {
                ERR(TAG,"Exception: " + e.getMessage());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
            }
            finally
            {
                if( bis != null )
                {
                    try { bis.close(); } catch (Exception e) {}
                }
                
                if( buffer != null )
                {
                    try { buffer.close(); } catch (IOException e) {}
                }
                con = null;
                httpConnection = null;
           }
            
           return strResult;
        }
    }
    
    
	/**
	 * show progress dialog
	 * 
	 * @param _context
	 */
    public ProgressDialog showProgressDialog(Context _context)
    {
        return ProgressDialog.show(_context, "", getString(R.string.IAP_SAMSUNG_WAITING), true);
    }

    
	/**
	 * dismiss progress dialog
	 * 
	 * @param progressDialog
	 */
    public void dismissProgressDialog(ProgressDialog _progressDialog)
    {
        try
        {
            if(null != _progressDialog && _progressDialog.isShowing())
                _progressDialog.dismiss();
        }
        catch( Exception e )
        {
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
        }
    }
	
	
	/**
	 * ResultCallBack
	 * 
	 * @param _result
	 * @param _finishActivity
	 */
    public void ResultCallBack(int _result, final boolean _finishActivity)
    {
		INFO(TAG,"[ResultCallBack]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[ResultCallBack]");
		
		SamsungHelper.IABResultCallBack(_result);
		dismissProgressDialog(mProgressDialog);
			
		try
        {	
			if(_finishActivity)
				finish();
        }
        catch( Exception e )
        {
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
        }
    }
	
	/**
	 * ResultCallBack
	 * 
	 * @param _result
	 * @param _thirdPartyError
	 * @param _thirdPartyErrorString
	 * @param _finishActivity
	 */
    public void ResultCallBack(int _result, int _thirdPartyError, String _thirdPartyErrorString, final boolean _finishActivity)
    {
		INFO(TAG,"[ResultCallBack] _thirdPartyErrorString");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[ResultCallBack]");
		


        SamsungHelper.IABResultCallBack(_result, _thirdPartyError, _thirdPartyErrorString);
		dismissProgressDialog(mProgressDialog);
			
		try
        {	
			if(_finishActivity)
				finish();
        }
        catch( Exception e )
        {
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
        }
    }
	
	/**
	 * ResultCallBack
	 * 
	 * @param _result
	 * @param _finishActivity
	 */
    public void ResultRestoreCallBack(int _result, String _itemID, final boolean _finishActivity)
    {
    	ResultRestoreCallBack(_result, _itemID, _finishActivity, null);
    }

    public void ResultRestoreCallBack(int _result, String _itemID, final boolean _finishActivity, final String paymentId)
    {
		INFO(TAG,"[ResultRestoreCallBack]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[ResultRestoreCallBack]");
		
		SamsungHelper.isRestoreTransaction = false;
		DBG(TAG,"Restored Item ID: " + _itemID);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "Restored Item ID: " + _itemID);
		
		Bundle callBackbundle = new Bundle();
		byte[] emptyarray = new String().getBytes();

		if(paymentId != null)
		{
			String storeCert = null;
			JSONObject jObj = new JSONObject();
			if(!TextUtils.isEmpty(paymentId))
			{
				try
				{
					jObj.put(InAppBilling.GET_STR_CONST(IAB_SAMSUNG_PAYMENT_ID),paymentId);//"payment_id"
					storeCert = jObj.toString();
				}catch(JSONException e) {
					ERR(TAG,"An error occurred generating store_certificate for restores. " + e.toString() );
					storeCert = "";
				}
				callBackbundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(storeCert!= null)?storeCert.getBytes():emptyarray);
			}
		}

		callBackbundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
		callBackbundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(_itemID!=null)?_itemID.getBytes():emptyarray);
		callBackbundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():emptyarray);//trash value
		callBackbundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():emptyarray);
		callBackbundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():emptyarray);
		callBackbundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
		callBackbundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), _result);
			
		try
		{
			Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
			Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
			callBackbundle = (Bundle)mMethod.invoke(null, (new Object[]{callBackbundle}));

			if(_finishActivity)
			{
				dismissProgressDialog(mProgressDialog);
				finish();
			}
        }
        catch(Exception e)
        {
			ERR(TAG,"Exception: " + e.getMessage());
        }
    }
	
	
	/**
	 * Treat result of SamsungAccount Authentication and IAPService 
	 */
    @Override
    protected void onActivityResult(int _requestCode, int _resultCode, Intent _intent)
    {
        switch ( _requestCode )
        {
            //    Treat result of IAPService
            // ================================================================
            case SamsungIAB2Utils.REQUEST_CODE_IS_IAP_PAYMENT:
            {
                if( null == _intent )
                {
                    break;
                }
                
                Bundle extras         = _intent.getExtras();
                
                String itemId         = "";
                String thirdPartyName = "";

                // payment success   : 0
                // payment cancelled : 1
                // ============================================================
                int statusCode        = 1;
                // ============================================================
                
                String errorString    = "";
                PurchaseVO purchaseVO = null;
                
				
                //    If there is bundle passed from IAP
                // ------------------------------------------------------------
                if ( null != extras )
                {
                    thirdPartyName = extras.getString(SamsungIAB2Utils.KEY_NAME_THIRD_PARTY_NAME);
                    statusCode = extras.getInt(SamsungIAB2Utils.KEY_NAME_STATUS_CODE);
                    errorString = extras.getString(SamsungIAB2Utils.KEY_NAME_ERROR_STRING);
                    itemId = extras.getString(SamsungIAB2Utils.KEY_NAME_ITEM_ID);
                    purchaseVO = new PurchaseVO( extras.getString(SamsungIAB2Utils.KEY_NAME_RESULT_OBJECT)); //isma, DEBUG THIS JSON
                }
                // ------------------------------------------------------------
                //    If there is no bundle passed from IAP
                // ------------------------------------------------------------
                else
				{
					ERR(TAG, "The payment was not processed successfully");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "The payment was not processed successfully");
				}	
                // ------------------------------------------------------------
                

                //    If payment was not cancelled
                // ------------------------------------------------------------
                if( RESULT_OK == _resultCode )
                {
                    //    if Payment succeed
                    // --------------------------------------------------------
                    if( statusCode == SamsungIAB2Utils.IAP_ERROR_NONE )
                    {
                        // Not longer needed because purchase validation is done through eCommerce server
						//VerifyClientToServerTask( purchaseVO );
						
						mPurchaseId = purchaseVO.getPurchaseId();
						mPaymentId 	= purchaseVO.getPaymentId();
						ResultCallBack(IAB_BUY_OK, true);
                    }
                    // ============================================================
					//    If the item was already purchased
					// ============================================================
					else if( statusCode == SamsungIAB2Utils.IAP_ERROR_ALREADY_PURCHASED )
					{
						//    dismiss progress dialog for IAPService Initialization
						// ================================================================
						dismissProgressDialog( mProgressDialog );
						// ================================================================
						
						SamsungHelper.isRepurchasedItem = true;
						
						Runnable OkBtnRunnable = new Runnable()
						{
							@Override
							public void run()
							{
								ResultCallBack(IAB_BUY_OK, true);
								finish();
							}
						};
						
						mSamsungIAB2Utils.showIapDialog(SamsungIAB2Activity.this,
								 getString( R.string.IAP_SAMSUNG_DIALOG_TITLE), 
								 getString( R.string.IAP_SAMSUNG_ERROR_ALREADY_PURCHASED),
								 false,
								 OkBtnRunnable,
								 false);
					}
					// --------------------------------------------------------
                    //    Payment failed 
                    // --------------------------------------------------------
					else
                    {
						ERR(TAG, "Payment error: \n-itemId : " + itemId + 
                                     "\n-thirdPartyName : " + thirdPartyName + 
                                     "\n-statusCode : " + statusCode +
                                     "\n-errorString : " + errorString);
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Payment error: \t-itemId : " + itemId + 
																					 ",\t-thirdPartyName : " + thirdPartyName + 
																					 ",\t-statusCode : " + statusCode +
																					 ",\t-errorString : " + errorString);

                        int error_code = SamsungIAB2Utils.SamsungErrorCodes.containsKey(statusCode)?SamsungIAB2Utils.SamsungErrorCodes.get(statusCode):statusCode;
                        ResultCallBack(IAB_BUY_FAIL, error_code, errorString, true);
                    }
                    // --------------------------------------------------------
                }
                // ------------------------------------------------------------
                //    If payment was cancelled
                // ------------------------------------------------------------
                else if( RESULT_CANCELED == _resultCode )
                {
					WARN(TAG, "Payment Cancelled, StatusCode: [" + statusCode + "] Message: " + errorString);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "Payment Cancelled, StatusCode: [" + statusCode + "] Message: " + errorString);
                    int error_code = SamsungIAB2Utils.SamsungErrorCodes.containsKey(statusCode)?SamsungIAB2Utils.SamsungErrorCodes.get(statusCode):statusCode;
					ResultCallBack(IAB_BUY_CANCEL, error_code, errorString, true);
                }
                // ------------------------------------------------------------
                break;
            }
            // ================================================================
            

            //    Treat result of SamsungAccount authentication
            // ================================================================
            case SamsungIAB2Utils.REQUEST_CODE_IS_ACCOUNT_CERTIFICATION:
            {
                //    If SamsungAccount authentication is succeed 
                // ------------------------------------------------------------
                if( RESULT_OK == _resultCode )
                {
                    // initialize IAPService
                    // --------------------------------------------------------
					if(SamsungHelper.isRestoreTransaction)
					{
						// Go to Restore Transaction Task
						InitRestoreTransactionService();
					}
					else
					{
					#if RELEASE_VERSION
						// Go to Purchase Task
						InitPurchaseService();
					#else
						//Retireve Item List before calling Purchase Task for debugging
						InitGetItemListService();
					#endif
					}
                    // --------------------------------------------------------
                }
                // ------------------------------------------------------------
                //    If SamsungAccount authentication is cancelled
                // ------------------------------------------------------------
                else if( RESULT_CANCELED == _resultCode )
                {
					ERR(TAG, "SamsungAccount authentication error");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "SamsungAccount authentication error");
					
					if(SamsungHelper.isRestoreTransaction)
						ResultRestoreCallBack(IAB_BUY_CANCEL, null, true);
					else
						ResultCallBack(IAB_BUY_CANCEL, true);
                }
                // ------------------------------------------------------------
                break;
            }
            // ================================================================
        }
    }
    
    
    @Override
    protected void onDestroy()
    {
        INFO(TAG,"[onDestroy]");
        super.onDestroy();

        // unbound IAPService
        // ====================================================================
        if( mSamsungIAB2Utils != null )
        {
            mSamsungIAB2Utils.dispose();
        }
        // ====================================================================
        
        if( mPurchaseItemTask != null )
        {
            if( mPurchaseItemTask.getStatus() != Status.FINISHED )
            {
                mPurchaseItemTask.cancel( true );
            }
        }

        if( mGetItemListTask != null )
        {
            if ( mGetItemListTask.getStatus() != Status.FINISHED )
            {
                mGetItemListTask.cancel( true );
            }
        }
        
        if( mVerifyClientToServerTask != null )
        {
            if ( mVerifyClientToServerTask.getStatus() != Status.FINISHED )
            {
                mVerifyClientToServerTask.cancel( true );
            }
        }
    }
	
	@Override
	protected void onStop() 
	{
		INFO(TAG, "[onStop]");
		dismissProgressDialog(mProgressDialog);
		super.onStop();
	}
}
#endif