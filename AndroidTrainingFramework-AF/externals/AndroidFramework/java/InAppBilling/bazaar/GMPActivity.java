#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.billing.common.Base64;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

public class GMPActivity extends Activity
{
    SET_TAG("InAppBilling");

    private static String mSku = "";
	private static AServerInfo mServerInfo = null;
    
	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = 1357;
        
    private boolean init = false;
    private boolean errorOcurred = false;
	private int mOperation = -1;
	private String mItemId = null;
	
	GMPUtils mGMPUtils;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		DBG(TAG, "onCreate");
		init = false;
		android.content.Intent sender = getIntent();
		if (GMPHelper.getInstance() == null)
		{
			ERR(TAG, "Error GMPHelper is null, no purchase request was launched");
			finish();
		}
		else if (sender.getExtras() != null)
		{
			Bundle b 	= sender.getExtras();
			mItemId	 	= b.getString(GMPUtils.KEY_ITEM_ID);
			mOperation 	= b.getInt(GMPUtils.KEY_OPERATION);
			mGMPUtils = GMPUtils.getInstance();
			mServerInfo = GMPHelper.getInstance().getServerInfo();
			//GMPUtils.setContext(this);
		}
		else
		{
			ERR(TAG, "Not a valid request, The intent has not a bundle");
			finish();
		}
	}
    @Override
    protected void onResume() {
        super.onResume();
		DBG(TAG, "onResume");
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!init) 
				{
					init = true;
					if (mOperation == OP_TRANSACTION)
					{
						performPurchaseRequest();
					}	
					/* else if (mOperation == OP_RESTORE_TRANS)
					{
						performRestoreRequest();
					} */
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
	}
	@Override
	protected void onStop()
	{
		super.onStop();
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
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
		DBG(TAG, "onConfigurationChanged");
		JDUMP(TAG, mItemId);
		
	}
    private void performPurchaseRequest() 
	{
		DBG(TAG, "performPurchaseRequest ");
		String sku = mServerInfo.getPIDBilling();
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Starting request for item["+sku+"]");
		if (TextUtils.isEmpty(sku)) //must not
			sku = mItemId;
			
		JDUMP(TAG, mItemId);
		JDUMP(TAG, sku);
		String payload = Base64.encode(mItemId.getBytes());
		
		mGMPUtils.launchPurchaseRequest(this, sku, RC_REQUEST, mPurchaseFinishedCB, payload);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		DBG(TAG,  "onActivityResult(" + requestCode + "," + resultCode + "," + data);
		if (!mGMPUtils.handleActivityResult(requestCode, resultCode, data)) 
		{	
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	IABCallBack mPurchaseFinishedCB = new IABCallBack()
	{
		public void runCallBack(Bundle bundle)
		{
			GMPResult result = new GMPResult(bundle.getInt(GMPUtils.KEY_RSLT_RESPONSE), bundle.getString(GMPUtils.KEY_RSLT_MESSAGE));
			JDUMP(TAG, result.getResponse());
			JDUMP(TAG, result.getMessage());
			String PurchaseJson = bundle.getString(GMPUtils.KEY_PCHS_JSON);
			String Signature = bundle.getString(GMPUtils.KEY_PCHS_SIGNATURE);
			JDUMP(TAG,PurchaseJson);
			JDUMP(TAG,Signature);
						
			PurchaseInfo purchase = null;
			if (result.isOwned())
			{
				GMPUtils utils = GMPUtils.getInstance();
				GMPHelper.getInstance().setItemToRestore(InAppBilling.mItemID);
				utils.launchQueryPurchases(((Activity)SUtils.getContext()),GMPHelper.getInstance().mUOwnThisItemCB);
			}
			else if (result.isFailure()) 
			{
				InAppBilling.saveLastItem(STATE_FAIL);
				InAppBilling.clear();
				
				ERR(TAG,"Error purchasing: " + result);
				LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Error purchasing: " + result);
				bundle.clear();
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
			else
			{
				try{
					INFO(TAG, "Purchase successful.");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Purchase successful.");
					purchase = new PurchaseInfo(PurchaseJson, Signature);
					InAppBilling.c();
					if (InAppBilling.mMKTStatusHk != InAppBilling.MKT_FREEDOM_RUNNING)
					{					
						GMPHelper.getInstance().sendIABNotification(purchase);
					}
					else
					{
						InAppBilling.saveLastItem(STATE_FAIL);
						InAppBilling.clear();
						
						bundle.clear();
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
						GMPUtils utils = GMPUtils.getInstance();
						utils.launchConsumeProductRequest(((Activity)SUtils.getContext()), purchase, null);
					}
				}
				catch (JSONException e)
				{
					DBG_EXCEPTION(e);
				}
			}
			finish();
		}
	};
}
#endif //USE_IN_APP_BILLING
