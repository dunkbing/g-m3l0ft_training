#if USE_IN_APP_BILLING

package APP_PACKAGE.iab;

import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;


import APP_PACKAGE.GAME_ACTIVITY_NAME;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.Encrypter;


import com.amazon.inapp.purchasing.BasePurchasingObserver;
import com.amazon.inapp.purchasing.Item;
import com.amazon.inapp.purchasing.ItemDataResponse;
import com.amazon.inapp.purchasing.Offset;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.Receipt;
import com.amazon.inapp.purchasing.SubscriptionPeriod;



import APP_PACKAGE.R;

public class AMZHelper extends IABTransaction
{
	SET_TAG("InAppBilling");
	private IABPurchaseObserver mIABPurchaseObserver;
	private String mCurrentId = null;
	
	public static final int DIALOG_BILLING_NOT_SUPPORTED_ID 	= 1;
	public static final int DIALOG_WAITTING_CONFIRMATION	 	= 2;

	// IAB Helper error codes
	public static final int AMZHELPER_ERROR_BASE	= -2000;	
	public static final int AMZHELPER_UNKNOWN_ERROR	= -2001;
	public static final int AMZHELPER_INVALID_SKU 	= -2002;

	
	//Used for You already own this item error
	private String mItemToRestore = null;
	public void setItemToRestore(String value)
	{
		mItemToRestore = value;
	}
	public String getItemToRestore()
	{
		return  mItemToRestore;
	}
	
	public AMZHelper(ServerInfo si)
	{
		super(si);
		mIABPurchaseObserver = new IABPurchaseObserver(si, this);
	}
	
	@Override
	public boolean isBillingSupported()
	{
		DBG(TAG, "Registering IABPurchaseObserver");
		PurchasingManager.registerObserver(mIABPurchaseObserver);
		return true;
	}
	
	/*@Override
	public void sendConfirmation()
	{
		mIABPurchaseObserver.sendConfirmation();
	}*/
	
	@Override
	public boolean restoreTransactions()
	{
		INFO(TAG, ">>>>> [restoreTransactions] <<<<<");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "start restoreTransactions");
		PurchasingManager.initiatePurchaseUpdatesRequest(Offset.BEGINNING);
		return true;
	}
	
	@Override
	public boolean requestTransaction(String id) 
	{
		mCurrentId = id;
		String pid = mServerInfo.getItemUID();
		if (TextUtils.isEmpty(pid)) //must not
			pid = id;
		INFO(TAG, "requestTransaction Product \'" + pid+"\'");	
		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "requestTransaction Product \'" + pid+"\'");
		PurchasingManager.initiatePurchaseRequest(pid);
		return true;
	}

	@Override
	public void showDialog(final int idx, final String id) 
	{	
		GAME_ACTIVITY_NAME.getActivityContext().runOnUiThread(new Runnable ()
		{
			public void run ()
			{
				try
				{
					INFO(TAG,"ShowDialog"+idx);
					Dialog dialog = null;
					DialogInterface.OnClickListener dil = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{
							//Informing the game that the buy item proccess is complete and fail.
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
							
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(id!=null)?id.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(mCurrentId!=null)?mCurrentId.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 2);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), (idx==DIALOG_WAITTING_CONFIRMATION)?IAB_BUY_PENDING:IAB_BUY_FAIL);
								
								
							try{
								Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
								Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
								bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
							}catch(Exception ex ) {
								ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
							}
						}
					};
					switch (idx) {
						case DIALOG_BILLING_NOT_SUPPORTED_ID:
							dialog = createDialog(R.string.billing_not_supported_title, R.string.billing_not_supported_message, dil);
						break;
						case DIALOG_WAITTING_CONFIRMATION:
							dialog = createDialog(R.string.pending_transaction, R.string.waitting_confirm, dil);
						break;
					}
					if (dialog != null)
					{
						dialog.setOwnerActivity((Activity)SUtils.getContext());
						dialog.show();
					}	
				}
				catch (Exception e)
				{
						DBG_EXCEPTION(e);
				}
			}
		});	
	}

	private Dialog createDialog(int titleId, int messageId, DialogInterface.OnClickListener dil) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
		builder.setTitle(titleId)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setMessage(messageId)
		.setCancelable(false)
		.setPositiveButton(android.R.string.ok, dil);
		
		return builder.create();
	}
}


/**
 * A {@link PurchaseObserver} is used to get callbacks when Amazon client sends
 * messages to this application so that we can update the UI.
 */
class IABPurchaseObserver extends BasePurchasingObserver 
{
	private ServerInfo mServerInfo = null;
	SET_TAG("InAppBilling");
	private AMZHelper mAMZHpr = null;
	private String mAmzUserId = null;
	private String mStoreCertificate = null;
	private boolean m_isSandboxMode = true;

	/** The server functions for HTTP communication. */ 
	private static final String SUCCESS_RESULT = "T7WxMl1MuYnllpIJnNJtoFD1ENkvNoVcrGXq7CvZ1Oo=";

	public IABPurchaseObserver(ServerInfo si, AMZHelper hpr) 
	{
		super(GAME_ACTIVITY_NAME.getActivityContext());
		mServerInfo = si;
		mAMZHpr = hpr;
	}

	@Override
	public void onSdkAvailable(boolean isSandboxMode) {
		DBG(TAG,"isSandboxMode: "+ isSandboxMode);
		m_isSandboxMode = isSandboxMode;
		/*if (!isSandboxMode) {
			InAppBilling.mMKTStatus = InAppBilling.MKT_SUPPORTED;
		} else {
			InAppBilling.mMKTStatus = InAppBilling.MKT_NO_SUPPORTED;
		}*/
	}

	@Override 
	public void onPurchaseResponse(PurchaseResponse purchaseResponse)
	{
        JDUMP(TAG, purchaseResponse.getPurchaseRequestStatus());
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "getPurchaseRequestStatus "+ purchaseResponse.getPurchaseRequestStatus());
		Receipt receipt = purchaseResponse.getReceipt();
		mStoreCertificate = null;

		if (null != receipt && purchaseResponse.getPurchaseRequestStatus() == PurchaseResponse.PurchaseRequestStatus.SUCCESSFUL ) 
		{
			JDUMP(TAG, receipt.getSku());
			JDUMP(TAG, receipt.getItemType());
			JDUMP(TAG, receipt.getPurchaseToken());
			JDUMP(TAG, purchaseResponse.getUserId());
			
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "getSku() "+ receipt.getSku());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "getItemType() "+ receipt.getItemType());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "getPurchaseToken() "+ receipt.getPurchaseToken());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "getUserId() "+ purchaseResponse.getUserId());
						
			mAmzUserId = purchaseResponse.getUserId();
			String itemId = mServerInfo.getItemIdByUID(receipt.getSku());
			StringBuffer sb = new StringBuffer(receipt.getPurchaseToken());
			if (m_isSandboxMode)
			{
				sb.append("_");
				sb.append(System.currentTimeMillis());
			}
			addToPurchasedQueue( new PurchaseInfo(null,itemId, sb.toString()) );
		}
		else if (purchaseResponse.getPurchaseRequestStatus() == PurchaseResponse.PurchaseRequestStatus.ALREADY_ENTITLED ) 
		{
			mAMZHpr.setItemToRestore(InAppBilling.mItemID);
			DBG(TAG, "start restoreTransactions at ALREADY_ENTITLED response code");
			LOGGING_LOG_INFO(IABConst.IAP_LOG_TYPE_VERBOSE, IABConst.IAP_LOG_STATUS_INFO, "start restoreTransactions at ALREADY_ENTITLED response code");
			PurchasingManager.initiatePurchaseUpdatesRequest(Offset.BEGINNING);
		}
		else // The charge failed on the server.
		{
			InAppBilling.saveLastItem(STATE_FAIL);
			InAppBilling.clear();
			{
				//Informing the game that the buy item proccess is complete and fail
				Bundle bundle = new Bundle();
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
				
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
				

				//Error code
				int code = (purchaseResponse.getPurchaseRequestStatus() == PurchaseResponse.PurchaseRequestStatus.INVALID_SKU)?AMZHelper.AMZHELPER_INVALID_SKU:AMZHelper.AMZHELPER_UNKNOWN_ERROR;
				String message =  getResponseDesc(code);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(message!=null)?message.getBytes():null);				
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), code);


				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
				
				try{
					Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
					Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
					bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
				}catch(Exception ex ) {
					ERR(TAG,"B: Error invoking reflex method "+ex.getMessage());
				}
			}
		}
	}
	
	@Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) 
	{
		JDUMP(TAG, purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus());
		mStoreCertificate = null;

		for (Receipt receipt : purchaseUpdatesResponse.getReceipts())
		{	
			if (InAppBilling.mOperationId == OP_RESTORE_TRANS)
			{
				INFO(TAG,"***  Sending item info to the game >>> \t Item UID: " + mServerInfo.getItemIdByUID(receipt.getSku()) + "\t Sku: " + receipt.getSku() + "\t ItemType: " + receipt.getItemType());
				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Sending item info to the game >>> \t Item UID: " + mServerInfo.getItemIdByUID(receipt.getSku()) + "\t Sku: " + receipt.getSku() + "\t ItemType: " + receipt.getItemType());

				mAmzUserId = purchaseUpdatesResponse.getUserId();
				StringBuffer sb = new StringBuffer(receipt.getPurchaseToken());
				if(m_isSandboxMode)
				{
					sb.append("_");
					sb.append(System.currentTimeMillis());
				}
				setStoreCertificate(sb.toString(), mAmzUserId);
				JDUMP(TAG, getStoreCertificate());


				Bundle bundle = new Bundle();
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_RESTORE_TRANS);
				
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(mServerInfo.getItemIdByUID(receipt.getSku())!=null)?mServerInfo.getItemIdByUID(receipt.getSku()).getBytes():null);
				// bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_SKU),(receipt.getSku()!=null)?receipt.getSku().getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);//trash value
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(getStoreCertificate()!=null)?getStoreCertificate().getBytes():null);
				bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_SHOP),(InAppBilling.GET_STR_CONST(IAB_AMAZON).getBytes()));
				
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 2);//trash value
				bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);//trash value
				
				try{
					Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
					Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
					bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
				}catch(Exception ex ) {
					ERR(TAG,"E: Error invoking reflex method "+ex.getMessage());
				}
			} else if(InAppBilling.mOperationId == OP_TRANSACTION)
			{
				String mItemId = mServerInfo.getItemIdByUID(receipt.getSku());
				JDUMP(TAG,mItemId);
				JDUMP(TAG,mAMZHpr.getItemToRestore());

				if (!TextUtils.isEmpty(mAMZHpr.getItemToRestore()) && mItemId.equals(mAMZHpr.getItemToRestore()))
				{
					mAMZHpr.setItemToRestore(null);
					mAmzUserId = purchaseUpdatesResponse.getUserId();
					StringBuffer sb = new StringBuffer(receipt.getPurchaseToken());
					if(m_isSandboxMode)
					{
						sb.append("_");
						sb.append(System.currentTimeMillis());
					}
					setStoreCertificate(sb.toString(), mAmzUserId);
					JDUMP(TAG, getStoreCertificate());

					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
					// bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_SKU),(receipt.getSku()!=null)?receipt.getSku().getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_NOTIFY_ID),(InAppBilling.mNotifyId!=null)?InAppBilling.mNotifyId.getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(getStoreCertificate()!=null)?getStoreCertificate().getBytes():null);
					bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_SHOP),(InAppBilling.GET_STR_CONST(IAB_AMAZON).getBytes()));

					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
						
						
					try{
						Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
						Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
						bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
					}catch(Exception ex ) {
						ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
					}
					InAppBilling.saveLastItem(STATE_SUCCESS);
					InAppBilling.clear();
				}
			}
		}	
    }

    private void setStoreCertificate (String token, String user)
	{
		JSONObject jObj = new JSONObject();
		try
		{
			jObj.put(InAppBilling.GET_STR_CONST(IAB_USER_ID),user);//"user_id"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_PURCHASE_TOKEN),token);//"purchase_token"
			mStoreCertificate = jObj.toString();
		}catch(JSONException e) {}
	}
	
	private String getStoreCertificate ()
	{
		return mStoreCertificate;
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
        String[] iabhelper_msgs = ("0:OK/-2001:Store failed transaction.(unknown error)/" +
                                   "-2002:Invalid SKU received").split("/");

        if (code <= AMZHelper.AMZHELPER_ERROR_BASE) {
            int index = AMZHelper.AMZHELPER_ERROR_BASE - code;
            if (index >= 0 && index < iabhelper_msgs.length) 
				return iabhelper_msgs[index];
			else
				return String.valueOf(code) + ":Unknown AMZ Helper Error";
        }
		else 
			return String.valueOf(code) + ":Unknown AMZ Helper Error";
	#else
		return "";
	#endif	
    }
	
	private static Hashtable<String, PurchaseInfo> mPurchaseQueue = new Hashtable<String, PurchaseInfo>();
	private static java.util.Vector<String> mPurchaseQueueNotifiedIds = new java.util.Vector<String>();
	private static Thread mPurchaseThread = null;
	public synchronized void addToPurchasedQueue (PurchaseInfo purchase)
	{
		DBG(TAG,"addToPurchasedQueue " +purchase.getOrderID());
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "addToPurchasedQueue " +purchase.getOrderID());
		boolean needANewThread = mPurchaseQueue.isEmpty();
		if(!mPurchaseQueueNotifiedIds.contains(purchase.getOrderID()))
		{
			mPurchaseQueue.put(purchase.getOrderID(),purchase);
			mPurchaseQueueNotifiedIds.add(purchase.getOrderID());
		}
		else
		{
			DBG(TAG,"addToPurchasedQueue DONT ADD IT AGAIN");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "addToPurchasedQueue DONT ADD IT AGAIN");
		}	
		if (mPurchaseThread == null || needANewThread)
		{
			mPurchaseThread = null;
			mPurchaseThread = new Thread( new Runnable() {
				public void run()
				{
					Enumeration <String> list;
					String key = null;
					while (!mPurchaseQueue.isEmpty())
					{
						if (!mIsConfirmInProgress)
						{
							list = mPurchaseQueue.keys();
							key = list.nextElement();
							PurchaseInfo pinfo = mPurchaseQueue.get(key);
							DBG(TAG,"addToPurchasedQueue sending confirmation ");
							JDUMP(TAG,pinfo.getItemID());
							JDUMP(TAG,pinfo.getOrderID());
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "addToPurchasedQueue sending confirmation ");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "pinfo.getItemID() "+pinfo.getItemID());
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "pinfo.getOrderID() "+pinfo.getOrderID());
							//InAppBilling.mItemID 	= pinfo.getItemID();
							InAppBilling.mOrderID = pinfo.getOrderID();
      				
							sendConfirmation(pinfo);
							mPurchaseQueue.remove(key);
						}
						else
						{
							DBG(TAG,"addToPurchasedQueue waiting for a confirmation");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "addToPurchasedQueue waiting for a confirmation");
						}
						try{
							Thread.sleep(DELAY_TIME_FOR_PENDING);
						}
						catch(Exception ex){};
					}
				}
			}	
		#if !RELEASE_VERSION
			,"Thread-PurchaseQueue"
		#endif
			);
			mPurchaseThread.start();	
		}
	}


	
	private static final int DELAY_TIME_FOR_PENDING = 	5*1000;
	private static final int DELAY_TIME_FOR_GL_RETRY = 	5*1000;

	private boolean mIsConfirmInProgress = false;
	public void sendConfirmation(final PurchaseInfo pinfo)
	{
		if (mIsConfirmInProgress)
			return;
		InAppBilling.save(WAITING_FOR_GAMELOFT);
		InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);

		Device device = new Device(mServerInfo);
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		INFO(TAG,"SendIABNotification for: InAppBilling.mItemID: "+pinfo.getItemID());
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "SendIABNotification for: InAppBilling.mItemID: "+pinfo.getItemID());
		mServerInfo.searchForDefaultBillingMethod(pinfo.getItemID());

		IABRequestHandler reqHandler 	= IABRequestHandler.getInstance();

		String url = mServerInfo.getURLbilling();
		String qry = null;
		String headers = null;
		StringBuffer sb = new StringBuffer();
		sb.append("b=" + InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME));
		sb.append("|" + reqHandler.encodeString( mServerInfo.getSelectedItem() ));
		sb.append("|" + reqHandler.encodeString( mServerInfo.getGamePrice() ));
		sb.append("|" + reqHandler.encodeString( mServerInfo.getMoneyBilling()));
		sb.append("|" +  SUtils.getLManager().getRandomCodeNumber());
		sb.append("|" + reqHandler.encodeString( pinfo.getOrderID()));
		sb.append("|" + reqHandler.encodeString( mAmzUserId));

		// qry = reqHandler.encodeString(sb.toString());
		qry = sb.toString();

		headers = getJsonHeaders(device);
		mIsConfirmInProgress = true;

		reqHandler.doRequestByGet(url, qry, headers, new IABCallBack()
		{
			public void runCallBack(Bundle cBundle)
			{
				try
				{
					mIsConfirmInProgress = false;

					setStoreCertificate(pinfo.getOrderID(), mAmzUserId);
					JDUMP(TAG,getStoreCertificate());
					int result = cBundle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
					int iHttpResp = cBundle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
					String responseStr = cBundle.getString(IABRequestHandler.KEY_RESPONSE);
					
					DBG(TAG, "purchaseValidation server result: " + result);
					DBG(TAG, "purchaseValidation server response: " + responseStr);
					if (result == IABRequestHandler.SUCCESS_RESULT && !TextUtils.isEmpty(responseStr))
					{
						//Informing the game that the buy item proccess is completed and success.
						String trResult = getValue(responseStr, 0);
						int errCode = Integer.parseInt(getValue(responseStr, 1));

						if (!TextUtils.isEmpty(trResult) && Encrypter.crypt(trResult).equals(SUCCESS_RESULT)) 
						{
							//success result from server
							int valCode = Integer.parseInt(getValue(responseStr, 2));
							String eCommTxId = getValue(responseStr, 3);
							if (SUtils.getLManager().isValidCode(valCode))
							{
								InAppBilling.saveLastItem(STATE_SUCCESS);
								InAppBilling.clear();
								
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
							
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(pinfo.getItemID()!=null)?pinfo.getItemID().getBytes():null);
								// bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_SKU),(pinfo.getSku()!=null)?pinfo.getSku().getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(eCommTxId!=null)?eCommTxId.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(getStoreCertificate()!=null)?getStoreCertificate().getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_SHOP),(InAppBilling.GET_STR_CONST(IAB_AMAZON).getBytes()));

								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);

								try{
									Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
									Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
									bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
								}catch(Exception ex ) {
									ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
								}

								return;
							}
						}
						//something went wrong during validation										
						{
							ERR(TAG,"Error while processing server response");
							InAppBilling.saveLastItem(STATE_FAIL);
							InAppBilling.clear();
							
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(pinfo.getItemID()!=null)?pinfo.getItemID().getBytes():null);
							// bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_SKU),(pinfo.getSku()!=null)?pinfo.getSku().getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							// bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(getStoreCertificate()!=null)?getStoreCertificate().getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_SHOP),(InAppBilling.GET_STR_CONST(IAB_AMAZON).getBytes()));

							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(responseStr!=null)?responseStr.getBytes():null);				
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), errCode);

							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);

							try{
								Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
								Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
								bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
							}catch(Exception ex ) {
								ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
							}
							return;
						}
					}

					{
						ERR(TAG,"Response Confirmation Fail");
						
						InAppBilling.saveLastItem(STATE_FAIL);
						InAppBilling.clear();

						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Response Confirmation Fail");
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
					
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(pinfo.getItemID()!=null)?pinfo.getItemID().getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
						
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(responseStr!=null)?responseStr.getBytes():null);				
						// bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), errCode);

						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(getStoreCertificate()!=null)?getStoreCertificate().getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_SHOP),(InAppBilling.GET_STR_CONST(IAB_AMAZON).getBytes()));

						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
							
						
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
						}
						//InAppBilling.save(WAITING_FOR_GAMELOFT);
					}

				}catch(Exception exd)
				{
					DBG_EXCEPTION(exd);
					DBG(TAG,"An Exception has occurred, Error Message:"+exd.getMessage());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "An Exception has occurred, Error Message:"+exd.getMessage());
				}
			}
		});
	}


	private String getJsonHeaders(Device device)
	{
		JSONObject jObj = new JSONObject();
		try
		{

		#if (HDIDFV_UPDATE != 2)	
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),device.getIMEI());//"x-up-gl-imei"
		#endif
		#if HDIDFV_UPDATE
			DBG("I_S"+HDIDFV_UPDATE, device.getHDIDFV());
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV());//"x-up-gl-hdidfv"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID());//"x-up-gl-gldid"
		#endif

			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_BUILD_MDL),android.os.Build.MODEL);//"x-android-os-build-model"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI),GGI);// x-up-gl-ggi
			jObj.put("x-up-gl-subno",device.getLineNumber());// x-up-gl-subno
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ANON_CREDS),InAppBilling.GET_ANONYMOUS_CREDENTIAL());// x-up-gl-anon-credentials
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_USR_AGENT),device.getUserAgent());// user-agent

			String value = InAppBilling.GET_GGLIVE_UID(); //"x-up-gl-acnum"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM),value);// x-up-gl-acnum

			value = InAppBilling.GET_CREDENTIALS(); //"x-up-gl-fed-credentials"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS),value);

			
			value = GET_CLIENT_ID(); //"x-up-gl-fed-client-id"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID),value);

			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GAMECODE),device.getDemoCode());
			jObj.put("Accept", InAppBilling.GET_STR_CONST(IAB_ECOM_API_VERSION));

			return jObj.toString();
		} catch (JSONException e) 
		{
			ERR(TAG, e.getMessage());
			DBG_EXCEPTION(e);
		}
		return null;
	}

	/**
	 * Splits the src string using the character "|" as target and finds the value according to pos.
	 * e.g.: src = a|hello|b|goodbye| pos = 1. Returns hello.
	 * @param src The String used as source.
	 * @param pos The position in src that I want to get.
	 * @return String with the values corresponding to pos in src.
	 * */
	private String getValue(String src, int pos)
	{
		int start = 0;
		int ini_pos = pos;
		int end = src.indexOf('|', start + 1);

		while (pos > 0)
		{
			if (start == -1)
			{
				return null;
			}
			start = end;
			end = src.indexOf('|', start + 1);
			pos--;
		}
		if (start == -1)
		{
			return null;
		}
		if (end == -1)
		{
			end = src.length();
		}
		if (ini_pos > 0)
		{
			start++;
		}
		if (start == end)
		{
			return "";
		}
		if (start > end)
		{
			return null;
		}
		try
		{
			char[] buf = new char[end - start];
			src.getChars(start, end, buf, 0);
			String value = new String(buf);
			return value;
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	

/*
	public void xsendConfirmation(final PurchaseInfo pinfo)
	{
		if (mIsConfirmInProgress)
			return;
		InAppBilling.save(WAITING_FOR_GAMELOFT);
		InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);
		
		new Thread( new Runnable()
		{
			public void run()
			{
				boolean finish = false;
				mIsConfirmInProgress = true;
			#if ITEMS_STORED_BY_GAME	
				int retryTimes = 3;
			#endif
				do 
				{
					try
					{
						INFO(TAG,"SendIABNotification for: InAppBilling.mItemID: "+pinfo.getItemID());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "SendIABNotification for: InAppBilling.mItemID: "+pinfo.getItemID());
						mServerInfo.searchForDefaultBillingMethod(pinfo.getItemID());


						XPlayer mXplayer = new XPlayer(new Device(mServerInfo));
						mXplayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
						mXplayer.sendIABNotification(0, InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME), pinfo.getOrderID(), mAmzUserId);

						long time = 0;
						while (!mXplayer.handleIABNotification())
						{
							try {
								Thread.sleep(50);
							} catch (Exception exc) {}
							
							if((System.currentTimeMillis() - time) > 1500)
							{
								DBG(TAG, "[sendIABNotification]Waiting for response");
								time = System.currentTimeMillis();
							}
						}
						if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE 
							|| XPlayer.getLastErrorCode() == XPlayer.IAB_FAILURE_INVALID_ORDER_ID)
						{
							//Informing the game that the buy item proccess is completed and success.
							finish = true;
							mIsConfirmInProgress = false;

							InAppBilling.saveLastItem((XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)?STATE_SUCCESS:STATE_FAIL);
							InAppBilling.clear();

							String TxId = XPlayer.getLastEComTxId();
							XPlayer.clearLastEComTxId();

							setStoreCertificate(pinfo.getOrderID(), mAmzUserId);
							JDUMP(TAG, getStoreCertificate());
							
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(pinfo.getItemID()!=null)?pinfo.getItemID().getBytes():null);
							// bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_SKU),(pinfo.getSku()!=null)?pinfo.getSku().getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ECOM_TX_ID),(TxId!=null)?TxId.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_STORE_CERT),(getStoreCertificate()!=null)?getStoreCertificate().getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_SHOP),(InAppBilling.GET_STR_CONST(IAB_AMAZON).getBytes()));

							if(XPlayer.getLastErrorCode() != XPlayer.ERROR_NONE)
							{
								String message = "eCommError" + XPlayer.getLastErrorCodeString();
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(message!=null)?message.getBytes():null);				
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), XPlayer.getLastErrorCode());
							}
							
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)?IAB_BUY_OK:IAB_BUY_FAIL);
								
								
							try{
								Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
								Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
								bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
							}catch(Exception ex ) {
								ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
							}
						
						}
						else 
						{
							ERR(TAG,"Response Confirmation Fail");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Response Confirmation Fail");
						#if !ITEMS_STORED_BY_GAME
							finish = true;
							Bundle bundle = new Bundle();
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(pinfo.getItemID()!=null)?pinfo.getItemID().getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
							
							String message = "eCommError: " + XPlayer.getLastErrorCodeString();
							bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_TP_ERROR),(message!=null)?message.getBytes():null);				
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_TP_ERROR_CODE), XPlayer.getLastErrorCode());


							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
							bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_FAIL);
								
							
							try{
								Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
								Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
								bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
							}catch(Exception ex ) {
								ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
							}
						#endif
							//InAppBilling.save(WAITING_FOR_GAMELOFT);
						}

					}catch(Exception exd)
					{
						DBG_EXCEPTION(exd);
						DBG(TAG,"An Exception has occurred, Error Message:"+exd.getMessage());
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "An Exception has occurred, Error Message:"+exd.getMessage());
					}
					try{
						Thread.sleep(DELAY_TIME_FOR_GL_RETRY);
					}catch(Exception ex){};
				}while (!finish
			#if ITEMS_STORED_BY_GAME	
					&& --retryTimes >= 0
			#endif
					);

				#if ITEMS_STORED_BY_GAME
					if(retryTimes < 0 )
					{
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
					
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(pinfo.getItemID()!=null)?pinfo.getItemID().getBytes():null);
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
					}
				#endif
					mIsConfirmInProgress = false;
			}
		}
	#if !RELEASE_VERSION
		,"Thread-Confirmation"
	#endif
		).start();
	}
*/
	
class PurchaseInfo
{

	public PurchaseInfo (String notify, String item, String order)
	{
		notifyID = notify;
		itemID = item;
		orderID = order;
		this.sku = null;
	}

	public PurchaseInfo (String notify, String item, String order, String sku)
	{
		notifyID = notify;
		itemID = item;
		orderID = order;
		this.sku = sku;
	}
	
	private String notifyID = null;
	private String itemID = null;
	private String orderID = null;
	private String sku = null;
	
	public String getNofifyID() { return notifyID; }
	public void setNofifyID(String value) { notifyID = value;}
	
	public String getItemID() 	{ return itemID; }
	public void setItemID(String value) 	{ itemID = value; }	
	
	public String getOrderID() 	{ return orderID; }
	public void setOrderID(String value) 	{ orderID = value; }

	public String getSku()	{ return sku; }
	public void  setSku(String value)	{ sku = value; }
}	

}
#endif //USE_IN_APP_BILLING