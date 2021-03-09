#if USE_IN_APP_BILLING

package APP_PACKAGE.iab;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Locale;
import java.lang.Thread;
import java.util.ArrayList;
import android.view.View;
import android.widget.TextView;

import com.kt.olleh.inapp.KTInAppActivity;
import com.kt.olleh.inapp.OnInAppListener;
import com.kt.olleh.inapp.net.ResDIApproveDown;
import com.kt.olleh.inapp.net.ResDIBuy;
import com.kt.olleh.inapp.net.ResDIStatus;
import com.kt.olleh.inapp.net.Response;
import android.app.AlertDialog;
import android.content.DialogInterface;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;


public final class KtIabActivity extends KTInAppActivity implements OnInAppListener, Runnable {

    SET_TAG("KtIabActivity");

    private static String appID = "";
    private static String productID = "";
    private static String uid = "";
	private static AServerInfo mServerInfo = null;

    // Not needed at the moment
    private static String serverIP = "";
    private static int serverPort = -1;

    private boolean purchaseInProgrese = false;
    private boolean init = false;
    private boolean errorOcurred = false;
	private boolean needTofinishinAfterInteraction = false;
	private static boolean requestRestoreTransaction = false;

    public static boolean LaunchKTBilling(String item, String appId, String productId, AServerInfo si) {
        KtIabActivity.uid = item;
        KtIabActivity.appID = appId;
        KtIabActivity.productID = productId;
		KtIabActivity.mServerInfo = si;
        //KtIabActivity.productID = "0900159881";
		
		try {
            Intent i = new Intent();
            String packageName = SUtils.getContext().getPackageName();
            i.setClassName(packageName, packageName + ".iab.KtIabActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			SUtils.getContext().startActivity(i);			
        } catch (Exception e) {
            return false;//TODO: put error code here
        }
        return true;
    }
	
	public static boolean LaunchKTRestore (String item, String appId, String productId, AServerInfo si) {
		requestRestoreTransaction = true;
		LaunchKTBilling(item, appId, productId, si);
		return true;
	}
	
	java.util.Queue<cBilling> mqueue;
	boolean isRestoring = false;
	@Override
	public void run() {
		try {
			while(!mqueue.isEmpty() || isRestoring)
			{
				if (!isRestoring)
				{
					isRestoring = true;
					cBilling tmpBilling = mqueue.poll();
					DBG(TAG, "[queue] getItemStatus("+appID+", "+ tmpBilling.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PID)) +")");
					getItemStatus(appID, tmpBilling.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PID)));
				}
				Thread.sleep(50);
			}
			DBG(TAG, "[queue] finishing activity");
			finish();
		} catch (Exception ex) {}
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
					if(requestRestoreTransaction)
					{
						performRestoreTransactions();
					}else{
						performPurchaseRequest();
					}
				}
			}
			});
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
	public void onDestroy()
	{
		super.onDestroy();
		requestRestoreTransaction = false;
		DBG(TAG, "onDestroy");
	}

	@Override
	public void OnResultPurchase( String tr_id, String app_id, String di_id)
	{
		DBG( TAG, "OnResultPurchase" );
		if (purchaseInProgrese){
			DBG(TAG,"tr_id = "+tr_id);
			purchaseInProgrese = false;
			
			//Informing the game that the buy item proccess is complete and Success.
			sendConfirmation();	
		}
		// If we get this far, something went really wrong.
        errorOcurred = true;
	}
	
	@Override
	public void OnResultAPI( String api, Response data )
	{
		DBG( TAG, "OnResultAPI api = " + api );
		if ( api.equalsIgnoreCase("getItemStatus"))
		{
			DBG( TAG, "mTr_id=" + data.mTr_id);
			DBG( TAG, "mCode=" + data.mCode);
			DBG( TAG, "mReason=" + data.mReason);
			
			ResDIStatus mResItem = (ResDIStatus)data;
			String mUseLimtCnt = mResItem.getUseLimtCnt();
			String mDownLimtCnt = mResItem.getDownLimtCnt();
			String product_ID = mResItem.getDiId();
			DBG( TAG, "mDi_id-getDiId=" + product_ID);
			DBG( TAG, "mUseLimtCnt=" + mUseLimtCnt);
			DBG( TAG, "mDownLimtCnt=" + mDownLimtCnt);
			
			// find item id
			cItem tmpItem = null;
			ArrayList<cItem> items = InAppBilling.mServerInfo.getShopProfile().getItemList();
			for (int x=0; x<items.size(); x++){
				cItem item = items.get(x);
				cBilling tmpBilling = item.getDefaultBilling();
				if (tmpBilling.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PID)).equalsIgnoreCase(product_ID)){
					DBG( TAG, "Item ID found=" + item.getId());
					tmpItem = item;
					break;
				}
			}
			
			if (tmpItem != null)
			{
				if (mUseLimtCnt.equalsIgnoreCase("9999"))
				{
					DBG( TAG, "Item "+tmpItem+" restored");
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
					
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(tmpItem.getId()!=null)?tmpItem.getId().getBytes():null);
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
				}
			}else
				ERR( TAG, "tmpItem can't be found");
			
			isRestoring = false;
		}
	}
	
	@Override
	public void OnError( String errorCode, String msg )
	{
		DBG( TAG, "OnError errorCode = " + errorCode + " msg = " + msg );
		if (requestRestoreTransaction){
			DBG( TAG, "Error while restoring");
			//finish();
			isRestoring = false;
		}else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.IAB_TRANSACTION_FAILED));
			builder.setMessage(msg)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
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
			});
			AlertDialog alert = builder.create();
			alert.show();

			errorOcurred = true;
			purchaseInProgrese = false;
		}
	}
	
	@Override
	public void OnResultFileURL( String arg0, String arg1 ) {}
	
	@Override
	public void OnResultOLDAPI( String arg0, String arg1 ) {
		DBG( TAG, "OnResultOLDAPI arg0 = " + arg0 + " arg1 = " + arg1 );
	}

    private void setUpView() {
        //setContentView(R.layout.iab_layout_skt_info);
		//TextView tv = new TextView(this);
		//tv.setText("Touch screen to continue");
		//tv.setTextColor(0xffffffff);
		//setContentView(tv);
    }

    private void performPurchaseRequest() {
		DBG(TAG, "performPurchaseRequest appID = "+appID + " productID = "+productID);
		purchase( appID, productID );
		purchaseInProgrese = true;
    }
	
	private void performRestoreTransactions() {
		DBG(TAG, "performRestoreTransactions");
		mqueue = new java.util.LinkedList<cBilling>();
		ArrayList<cItem> items = InAppBilling.mServerInfo.getShopProfile().getItemList();
		//	save last item to restore
		for (int x=0; x<items.size(); x++){
			cItem tmpItem = items.get(x);
			cBilling tmpBilling = tmpItem.getDefaultBilling();
			if (tmpItem.getAttributeByName("managed").equalsIgnoreCase("1"))
			{
				mqueue.add(tmpBilling);
			}
		}
		
		new Thread(this).start();
	}

    private void setupBilling() {
		/*IAPLibSetting setting = new IAPLibSetting();
        setting.AppID = appID;
        setting.BP_IP = serverIP;
        setting.BP_Port = serverPort;
        setting.ClientListener = this;

        IAPLibInit(setting);*/
		init( this );//register InAppListener
        DBG(TAG, "KT Init");
		INFO("KT IAP","==============KT IAP Version : "+getVersionInfo()+"================");
    }

	private int focusCount = 0;
    @Override
		public void onWindowFocusChanged(boolean hasFocus) {
			super.onWindowFocusChanged(hasFocus);
			/*DBG(TAG, "onWindowFocusChanged: "+hasFocus);

			if (errorOcurred && hasFocus && focusCount == 0) {//FIX: ugly hack, must be replaced with full UI
				focusCount++;
				return;
			}
			
			boolean active = SKTStateHelper.isDialogActive();
			DBG(TAG, "onWindowFocusChanged: 1");
			if (hasFocus && ((errorOcurred && focusCount > 0) || !active)) {

				if (errorOcurred) {
					//Informing the game that the buy item proccess is complete and fail.
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 2);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);

					try{
						Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
						Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
						bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
					}catch(Exception ex ) {
						ERR(TAG,"Error invoking reflex method "+ex.getMessage());
					}
				}
				else {DBG(TAG, "onWindowFocusChanged: 5");
					//Informing the game that the buy item proccess is complete and cancel by user.
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);

					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);

					try{
						Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
						Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
						bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
					}catch(Exception ex ) {
						ERR(TAG,"Error invoking reflex method "+ex.getMessage());
					}

				}

				errorOcurred = false;
				needTofinishinAfterInteraction = true;
				DBG(TAG, "We had finished with errors");
				this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						DBG(TAG, "Finishing");
						finish();
					}
				});
			}*/
		}	
	
	

    /*static final class KTStateHelper {
        public static boolean isDialogActive() {
            //return isConnect();
			return purchaseInProgrese;
        }
    }*/

    // Not used callbacks at the moment
	public void onJuminNumberDlgCancel() {
	}
	
	public void onDlgError() {
	}
	
	public void onDlgPurchaseCancel() {
	}
	
//    public void onItemAuthInfo(ItemAuthInfo info) {
//    }

//    public void onItemUseQuery(ItemUse arg0) {
//    }

    public void onTokenReceive(byte[] arg0) {
    }

//    public void onWholeQuery(ItemAuth[] arg0) {
//    }

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
