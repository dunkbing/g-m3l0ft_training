#if USE_IN_APP_BILLING && SAMSUNG_STORE
package APP_PACKAGE.iab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sec.android.iap.IAPConnector;
import APP_PACKAGE.R;
import APP_PACKAGE.iab.ErrorVO;
import APP_PACKAGE.iab.SamsungHelper;

public class SamsungIAB2Utils
{

	// Debugging TAG
    // ========================================================================    
	SET_TAG("InAppBilling");
	// ========================================================================    
	
    
    // IAP Signature Hashcode
    // ========================================================================
    public static final int     IAP_SIGNATURE_HASHCODE   = 0x7a7eaf4b;
    // ========================================================================

	
    // Billing Response Code
    // ========================================================================
    public static final int     IAP_RESPONSE_RESULT_OK                  = 0;
    public static final int     IAP_RESPONSE_RESULT_UNAVAILABLE         = 2;
    // ========================================================================


    // Bindle Key
    // ========================================================================
    public static final String  KEY_NAME_THIRD_PARTY_NAME = "THIRD_PARTY_NAME";
    public static final String  KEY_NAME_STATUS_CODE      = "STATUS_CODE";
    public static final String  KEY_NAME_ERROR_STRING     = "ERROR_STRING";
    public static final String  KEY_NAME_IAP_UPGRADE_URL  = "IAP_UPGRADE_URL";
    public static final String  KEY_NAME_ITEM_GROUP_ID    = "ITEM_GROUP_ID";
    public static final String  KEY_NAME_ITEM_ID          = "ITEM_ID";
    public static final String  KEY_NAME_RESULT_LIST      = "RESULT_LIST";
    public static final String  KEY_NAME_RESULT_OBJECT    = "RESULT_OBJECT";
    // ========================================================================

	
    // Item Type
    // ========================================================================
    public static final String ITEM_TYPE_CONSUMABLE                     = "00";
    public static final String ITEM_TYPE_NON_CONSUMABLE                 = "01";
    public static final String ITEM_TYPE_SUBSCRIPTION                   = "02";
    public static final String ITEM_TYPE_ALL                            = "10";
    // ========================================================================

	
    // IAP Name
    // ========================================================================
    public static final String  IAP_PACKAGE_NAME = "com.sec.android.iap";
    public static final String  IAP_SERVICE_NAME = "com.sec.android.iap.service.iapService";
    // ========================================================================


    // Define request code for IAPService.
    // ========================================================================
    public static final int   REQUEST_CODE_IS_IAP_PAYMENT                  = 1;
    public static final int   REQUEST_CODE_IS_ACCOUNT_CERTIFICATION        = 2;
    // ========================================================================
    
	
    // Define status code passed to application 
    // ========================================================================
    final public static int IAP_ERROR_NONE                         = 0;
    final public static int IAP_PAYMENT_IS_CANCELED                = 1;
    final public static int IAP_ERROR_INITIALIZATION               = -1000;
    final public static int IAP_ERROR_NEED_APP_UPGRADE             = -1001;
    final public static int IAP_ERROR_COMMON                       = -1002;
    final public static int IAP_ERROR_ALREADY_PURCHASED            = -1003;
    final public static int IAP_ERROR_WHILE_RUNNING                = -1004;
    final public static int IAP_ERROR_PRODUCT_DOES_NOT_EXIST       = -1005;
    final public static int IAP_ERROR_CONFIRM_INBOX                = -1006;
    // ========================================================================

    public static final java.util.Map <Integer,Integer> SamsungErrorCodes;
    static {
        java.util.Hashtable<Integer,Integer> tmp = new java.util.Hashtable<Integer,Integer>();
        tmp.put(IAP_ERROR_NONE, 0);
        tmp.put(IAP_PAYMENT_IS_CANCELED, -5001);
        tmp.put(IAP_ERROR_INITIALIZATION, -5002);
        tmp.put(IAP_ERROR_NEED_APP_UPGRADE, -5003);
        tmp.put(IAP_ERROR_COMMON, -5004);
        tmp.put(IAP_ERROR_ALREADY_PURCHASED, -5005);
        tmp.put(IAP_ERROR_WHILE_RUNNING, -5006);
        tmp.put(IAP_ERROR_PRODUCT_DOES_NOT_EXIST, -5007);
        tmp.put(IAP_ERROR_CONFIRM_INBOX, -5008);

        SamsungErrorCodes = java.util.Collections.unmodifiableMap(tmp);
    }

    // ========================================================================
	public 	static final int FIRST_ITEM_TO_DISPLAY			= 1;
	public 	static final int MAX_ITEMS_TO_DISPLAY			= 100;
    private static final int HONEYCOMB_MR1 					= 12;
	private static final int FLAG_INCLUDE_STOPPED_PACKAGES	= 32;
	 // ========================================================================
	
	private IAPConnector        mIapConnector             = null;
    private ServiceConnection   mServiceConn              = null;

    Context                     mContext;
    public boolean              mIsBind                   = false;
	public boolean 				misAppInstallerRunning 	  = false;

    // 0 : COMMERCIAL MODE
    // 1 : TEST MODE
    // ========================================================================
    int                         mMode                     = 1;
    // ========================================================================

	
    
	
	/**
	 * SamsungIAB2Utils
	 * constructor
	 * @param _context
	 */
    public SamsungIAB2Utils( Context _context )
    {
        mContext = _context.getApplicationContext();
    }

    
	/**
	 * IAP TEST MODE : 1
	 * IAP COMMERCIAL MODE : 0
	 * (Default : 1)
	 * 
	 * @param _mode
	 */
    public void setMode( int _mode )
    {
        mMode = _mode;
    }
    
    
	/**
	 * SamsungAccount Authentication
	 * @param _activity
	 */
    public void startAccountActivity(final Activity _activity)
    {
        INFO(TAG, "[startAccountActivity]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[startAccountActivity]");
		ComponentName com = new ComponentName( "com.sec.android.iap", "com.sec.android.iap.activity.AccountActivity" );
        Intent intent = new Intent();
        intent.setComponent( com );
        _activity.startActivityForResult(intent, REQUEST_CODE_IS_ACCOUNT_CERTIFICATION);
    }
    
    
	/**
	* Go to page of SamsungApps in order to install IAP
	* @param _activity
	*/
    public void installIapPackage(final Activity _activity)
    {
        INFO(TAG, "[installIapPackage]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[installIapPackage]");
		misAppInstallerRunning = true;
		Runnable OkBtnRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                // Link of SamsungApps for IAP install
                // ============================================================
                Uri iapDeepLink = Uri.parse("samsungapps://ProductDetail/com.sec.android.iap");
                // ============================================================
                
                Intent intent = new Intent();
                intent.setData(iapDeepLink);

                // If android OS version is more HoneyComb MR1,
                // add flag FLAG_INCLUDE_STOPPED_PACKAGES
                // ============================================================
                if( Build.VERSION.SDK_INT >= HONEYCOMB_MR1 )
                {
                    intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | 
                                     Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                                     FLAG_INCLUDE_STOPPED_PACKAGES );
                }
                // ============================================================
                else
                {
                    intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | 
                                     Intent.FLAG_ACTIVITY_CLEAR_TOP );
                }
				
				boolean isActivityLaunched = true;
				SamsungHelper.IABResultCallBack(IAB_BUY_CANCEL);
					
				try{mContext.startActivity( intent );}
				catch( ActivityNotFoundException e )
				{
					ERR(TAG,"ActivityNotFoundException: " + e.getMessage());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "ActivityNotFoundException: " + e.getMessage());
					isActivityLaunched = false;
					showInstallPackageError(_activity);
				}
				
				if(isActivityLaunched)
					_activity.finish();
            }
        };
        
        showIapDialog( _activity,
                       _activity.getString(R.string.IAP_SAMSUNG_DIALOG_TITLE),
                       _activity.getString(R.string.IAP_SAMSUNG_CLIENT_NOT_INSTALLED),
                       true,
                       OkBtnRunnable,
					   true);
    }
    
	/**
	 * Show Installer Error Dialog.
	 * @param _title
	 * @param _message
	 */
	public void showInstallPackageError(final Activity _activity)
	{
		INFO(TAG, "[showInstallPackageError]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[showInstallPackageError]");
		Runnable OkBtnRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				_activity.finish();
			}
		};
			
		showIapDialog(_activity, _activity.getString(R.string.IAP_SAMSUNG_DIALOG_TITLE), _activity.getString(R.string.IAP_SAMSUNG_APP_STORE_NOT_INSTALLED), true, OkBtnRunnable, false);
	}
	
    /**
	 * Verifies if IAP package is installed
	 * @param _context
	 */
	public boolean isInstalledIapPackage(Context _context)
    {
		INFO(TAG, "[isInstalledIapPackage]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[isInstalledIapPackage]");
        PackageManager pm = _context.getPackageManager();
		boolean result = true;
		misAppInstallerRunning = false;
        
        try
        {
            pm.getApplicationInfo( IAP_PACKAGE_NAME, PackageManager.GET_META_DATA );
        }
        catch( NameNotFoundException e )
        {
			ERR(TAG, "NameNotFoundException: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "NameNotFoundException: " + e.getMessage());
            result = false;
        }
		
		DBG(TAG, "isInstalledIapPackage? " + result);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "isInstalledIapPackage? " + result);
		return result;
    }

    
     /**
	 * Verifies if IAP package is valid
	 * @param _context
	 */
	public boolean isValidIapPackage(Context _context)
    {
        INFO(TAG, "[isValidIapPackage]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[isValidIapPackage]");
		boolean result = true;
        
        try
        {
            Signature[] sigs = _context.getPackageManager().getPackageInfo(
                                    IAP_PACKAGE_NAME,
                                    PackageManager.GET_SIGNATURES ).signatures;
            
            if( sigs[0].hashCode() != SamsungIAB2Utils.IAP_SIGNATURE_HASHCODE )
            {
                result = false;
            }
        }
        catch( Exception e )
        {
            result = false;
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
        }
        
		DBG(TAG, "isValidIapPackage? " + result);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "isValidIapPackage? " + result);
        return result;
    }
    
    
	/**
	 * bind to IAPService
	 *  
	 * @param _listener The listener that receives notifications
	 * when bindIapService method is finished.
	 */
    public void bindIapService( final OnIapBindListener _listener )
    {
        INFO(TAG, "[bindIapService]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[bindIapService]");
		
		// exit If already bound 
        // ====================================================================
        if( true == mIsBind )
        {
            _listener.onBindIapFinished( IAP_RESPONSE_RESULT_OK );
			return;
        }
        // ====================================================================

        // Connection to IAP service
        // ====================================================================
        mServiceConn = new ServiceConnection()
        {
            @Override
            public void onServiceDisconnected(ComponentName _name)
            {
                DBG(TAG, "IAP Service Disconnected...");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO, "IAP Service Disconnected...");
                mIapConnector = null;
            }

            @Override
            public void onServiceConnected(ComponentName _name, IBinder _service)
            {
                mIapConnector = IAPConnector.Stub.asInterface( _service );

                if( mIapConnector != null && _listener != null )
                {
                    mIsBind = true;
                    _listener.onBindIapFinished(IAP_RESPONSE_RESULT_OK);
                }
                else
                {
                    mIsBind = false;
                    _listener.onBindIapFinished(IAP_RESPONSE_RESULT_UNAVAILABLE);
                }
            }
        };
        // ====================================================================
        
		
        // bind to IAPService
        // ====================================================================
		Intent serviceIntent = new Intent(IAP_SERVICE_NAME);
        serviceIntent.setPackage(IAP_PACKAGE_NAME);
        mContext.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        // ====================================================================
    }
    
    
	/**
	 * Process IAP initialization by calling init() interface in IAPConnector
	 * @return ErrorVO
	 */
    public ErrorVO init()
    {
        ErrorVO errorVO = new ErrorVO();
        try
        {
            Bundle bundle = mIapConnector.init( mMode );
            if( null != bundle )
            {
                errorVO.setErrorCode(bundle.getInt( SamsungIAB2Utils.KEY_NAME_STATUS_CODE));
                errorVO.setErrorString(bundle.getString( SamsungIAB2Utils.KEY_NAME_ERROR_STRING));         
                errorVO.setExtraString(bundle.getString( SamsungIAB2Utils.KEY_NAME_IAP_UPGRADE_URL));
            }
        }
        catch( RemoteException e )
		{
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
		}
        return errorVO;
    }
	
    
	/**
	 * Load list of item by calling getItemList() method in IAPConnector
	 * 
	 * @param _itemGroupId
	 * @param _startNum
	 * @param _endNum
	 * @param _itemType
	 * @return Bundle
	 */
	public Bundle getItemList(String _itemGroupId, String _itemType)
	{
		INFO(TAG, "[getItemList]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[getItemList]");
		
		return getItemList(_itemGroupId, 1, 15, _itemType);
	}

	public Bundle getItemList(String _itemGroupId, int _startNum, int _endNum, String _itemType)
	{
		Bundle itemList = null;
		try
		{
			itemList = mIapConnector.getItemList( mMode, mContext.getPackageName(), _itemGroupId, _startNum, _endNum, _itemType );
		}
		catch( RemoteException e )
		{
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
		}
		return itemList;
	}

    
	/**
	 * Call getItemInboxList() method in IAPConnector
	 * to load List of purchased item
	 * 
	 * @param _itemGroupId
	 * @param _startNum
	 * @param _endNum
	 * @param _startDate
	 * @param _endDate
	 * @return Bundle
	 */
	public Bundle getItemsInbox(String _itemGroupId, int _firstItemToDisplay, int _maxItemsToDisplay)
    {
        INFO(TAG, "[getItemsInbox]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[getItemsInbox]");
		Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return getItemsInbox( _itemGroupId, _firstItemToDisplay, _maxItemsToDisplay, "20130101", sdf.format(d));
    }
	
    public Bundle getItemsInbox(String _itemGroupId, int _startNum, int _endNum, String _startDate, String _endDate)
    {
		Bundle purchaseItemList = null;
        try
        {
            purchaseItemList = mIapConnector.getItemsInbox(mContext.getPackageName(), _itemGroupId, _startNum, _endNum, _startDate, _endDate);
        }
        catch( RemoteException e ){
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
		}
		
        return purchaseItemList;
    }

    
	/**
	 * call PaymentMethodListActivity in IAP in order to process payment
	 * @param _activity
	 * @param _requestCode
	 * @param _itemGroupId
	 * @param _itemId
	 */
    public void startPurchase(Activity _activity, int _requestCode, String _itemGroupId, String _itemId)
    {
        INFO(TAG, "[startPurchase]");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "[startPurchase]");
		try
        {
            Bundle bundle = new Bundle();
            bundle.putString(SamsungIAB2Utils.KEY_NAME_THIRD_PARTY_NAME, mContext.getPackageName());	DBG(TAG, "PackageName: " + mContext.getPackageName());
            bundle.putString(SamsungIAB2Utils.KEY_NAME_ITEM_GROUP_ID, _itemGroupId);					DBG(TAG, "GroupId " + _itemGroupId);
            bundle.putString(SamsungIAB2Utils.KEY_NAME_ITEM_ID, _itemId);								DBG(TAG, "ItemId " + _itemId);
            
            ComponentName com = new ComponentName( "com.sec.android.iap", "com.sec.android.iap.activity.PaymentMethodListActivity" );
            Intent intent = new Intent( Intent.ACTION_MAIN );
            intent.addCategory( Intent.CATEGORY_LAUNCHER );
            intent.setComponent( com );
            intent.putExtras( bundle );
			
            _activity.startActivityForResult(intent, _requestCode);
        }
        catch( Exception e ){
			ERR(TAG,"Exception: " + e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Exception: " + e.getMessage());
		}
    }

    
	/**
	 * Show Dialog
	 * @param _title
	 * @param _message
	 */
    public void showIapDialog(final Activity _activity, String _title, String _message, final boolean _finishActivity, final Runnable _onClickRunable, final boolean _showCancelButton)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder( _activity );
        
        alert.setTitle( _title );
        alert.setMessage( _message );
        
        alert.setPositiveButton(R.string.IAP_SAMSUNG_OK_BUTTON, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, int which)
            {
                if(_onClickRunable != null)
                    _onClickRunable.run();
                
                dialog.dismiss();
                
                if(_finishActivity && !misAppInstallerRunning)
                    _activity.finish();
            }
        } );
		
		if(_showCancelButton)
		{
			alert.setNegativeButton(R.string.IAP_SAMSUNG_CANCEL_BUTTON, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick( DialogInterface dialog, int which)
				{
					dialog.dismiss();
					SamsungHelper.IABResultCallBack(IAB_BUY_CANCEL);
					
					if(_finishActivity)
						_activity.finish();
				}
			} );
		}
        
        if(SamsungHelper.isRepurchasedItem)
        {
            alert.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
					SamsungHelper.IABResultCallBack(IAB_BUY_OK);
					_activity.finish();
                }
            });
        }
		
		if(_finishActivity)
        {
            alert.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
					SamsungHelper.IABResultCallBack(IAB_BUY_CANCEL);
					_activity.finish();
                }
            });
        }
            
        alert.show();
    }
    
	/**
	 * Unbind from IAPService when you are done with activity. 
	 */
    public void dispose()
    {
        if( mServiceConn != null )
        {
            if( mContext != null )
                mContext.unbindService( mServiceConn );
            
            mServiceConn  = null;
            mIapConnector = null;
        }
    }

    
	/**
	 * Interface definition for a callback to be invoked
	 * when bindIapService method has been finished.
	 */
    public interface OnIapBindListener
    {
		/**
		 * Callback method to be invoked
		 * when bindIapService() method has been finished.
		 * @param result
		 */
        public void onBindIapFinished( int result );
    }
}
#endif