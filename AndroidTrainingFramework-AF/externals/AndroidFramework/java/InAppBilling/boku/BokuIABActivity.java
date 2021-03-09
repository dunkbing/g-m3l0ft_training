#if USE_IN_APP_BILLING && BOKU_STORE
package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Dialog;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import java.lang.reflect.Method;
/*import java.util.Locale;
import java.lang.Thread;*/

import com.boku.mobile.api.IntentProvider;
import com.boku.mobile.api.Transaction;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;

import APP_PACKAGE.billing.common.StringEncrypter;
import APP_PACKAGE.iab.GLOFTHelper;

import APP_PACKAGE.R;

#if ADS_USE_FLURRY
import APP_PACKAGE.Flurry.GLFlurry;
#endif
	
public class BokuIABActivity extends Activity
{

//get encrypted string
#define GES(id)		StringEncrypter.getString(id)
//get raw string
#define GRS(id)		SUtils.getContext().getString(id)

	SET_TAG("BokuActivity");
	
	private static AServerInfo mServerInfo = null;
	private static String mItemId = null;

    private boolean purchaseInProgress = false;
    //private boolean init = false;
    //private boolean errorOcurred = false;
	//private boolean needTofinishinAfterInteraction = false;

	private static final int BOKU_PAYMENT_ID = 0x476c6674;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		StartBokuBilling();
	}
	@Override
	protected void onStop()
	{
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		//APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onEndSession(this);
	#endif
	}
	@Override
	protected void onStart()
	{
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		//APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onStartSession(this);
	#endif
	}
	
    static boolean LaunchBokuBilling(String item, AServerInfo si) 
	{
		mItemId = item;
		mServerInfo = si;
        Intent i = new Intent();
        String packageName = SUtils.getContext().getPackageName();
        i.setClassName(packageName,  InAppBilling.GET_FQCN(IAB_CLASS_NAME_BOKU_IAB_ACTIVITY));
		mServerInfo.setBillingMethod(InAppBilling.GET_STR_CONST(IAB_BOKU));
		SUtils.getContext().startActivity(i);
        return true;
    }

    public boolean StartBokuBilling() {
		try {
			INFO(TAG, "LaunchBokuBilling ");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Start Boku Billing");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] MERCHANT_ID:"+InAppBilling.GET_STR_CONST(IAB_GAMELOFT));
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] API_KEY:"+InAppBilling.GET_STR_CONST(BOKU_API_KEY));
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] SERVICE_ID:"+mServerInfo.getItemServiceID());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] CURRENCY:"+mServerInfo.getStringCurrencyValue());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] PRODUCT_DESCRIPTION:"+mServerInfo.getItemAttribute(mItemId, InAppBilling.GET_STR_CONST(IAB_NAME)));
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] SUB_MERCHANT_NAME:"+SUtils.getContext().getString(R.string.app_name));
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] PRICE_INC_SALES_TAX:"+mServerInfo.getItemPriceWithTaxes());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "[Boku init info] PARAM:"+mItemId);

			Intent i = IntentProvider.newIntent(this);
			i.putExtra(Transaction.MERCHANT_ID, InAppBilling.GET_STR_CONST(IAB_GAMELOFT));
			i.putExtra(Transaction.API_KEY, InAppBilling.GET_STR_CONST(BOKU_API_KEY));
			i.putExtra(Transaction.SERVICE_ID,  mServerInfo.getItemServiceID());
			i.putExtra(Transaction.CURRENCY, mServerInfo.getStringCurrencyValue());
			i.putExtra(Transaction.PRODUCT_DESCRIPTION, mServerInfo.getItemAttribute(mItemId, InAppBilling.GET_STR_CONST(IAB_NAME)));
			i.putExtra(Transaction.SUB_MERCHANT_NAME, SUtils.getContext().getString(R.string.app_name));
			i.putExtra(Transaction.PRICE_INC_SALES_TAX, mServerInfo.getItemPriceWithTaxes());
			i.putExtra(Transaction.PARAM, mItemId);
			startActivityForResult(i, /*PANEL_ID*/BOKU_PAYMENT_ID);
        } catch (Exception e) {
			ERR(TAG, "Error "+e.getMessage());
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Can not start boku billing:"+e.getMessage());
            return false;//TODO: put error code here
        }
		INFO(TAG, "LaunchBokuBilling 2");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "LaunchBokuBilling 2");
        return true;
    }
#if !RELEASE_VERSION
	//#define USE_DEBUG_TOAST
#endif
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		INFO(TAG, "onActivityResult requestCode "+requestCode);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "onActivityResult requestCode "+requestCode);
		if (requestCode == BOKU_PAYMENT_ID) 
		{
			String bokuResultCode = data.getStringExtra(Transaction.RESULT_CODE);
			String bokuResultMessage = data.getStringExtra(Transaction.RESULT_MESSAGE);
			String bokuTrxId = data.getStringExtra(Transaction.BOKU_TRANSACTION_ID);
			
			JDUMP(TAG, bokuResultCode);
			JDUMP(TAG, bokuResultMessage);
			JDUMP(TAG, bokuTrxId);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "bokuResultCode "+bokuResultCode);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "bokuResultMessage "+bokuResultMessage);
			LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO, "bokuTrxId "+bokuTrxId);
			if (resultCode == Activity.RESULT_OK) 
			{
			#if USE_DEBUG_TOAST
				StringBuilder b = new StringBuilder();
				b.append("Result Code: ").append(bokuResultCode).append("\n");
				b.append("Result Message: ").append(bokuResultMessage).append("\n");
				b.append("Trx Id: ").append(bokuTrxId).append("\n");
				b.append("Activity.OK");
				Toast.makeText(this , b, Toast.LENGTH_LONG).show();
			#endif
			
				int rc=Integer.parseInt(bokuResultCode);
				switch (rc)
				{
					case BOKU_RESULT_CODE_PENDING:
					case BOKU_RESULT_CODE_P_BACK_TRANS_IN_PROGRESS:
					{
						INFO(TAG,"Pending ........");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Pending ........");
						InAppBilling.mOrderID = bokuTrxId;
						InAppBilling.save(WAITING_FOR_BOKU);
						InAppBilling.saveLastItem(WAITING_FOR_BOKU);
						
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
						
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 3);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_PENDING);
						
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"Boku: Error invoking reflex method "+ex.getMessage());
						}
						GLOFTHelper.GetInstance().showDialog(GLOFTHelper.DIALOG_PURCHASE_RESULT_PENDING, InAppBilling.mItemID);
						sendConfirmation();
					}
					break;	
					case BOKU_RESULT_CODE_SUCCESSFUL:
						InAppBilling.mOrderID = bokuTrxId;
						InAppBilling.save(WAITING_FOR_GAMELOFT);
						InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);
						sendConfirmation();
						break;
					default:
					{
						Bundle bundle = new Bundle();
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
						
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
						bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
						
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
						bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), (rc==BOKU_RESULT_CODE_P_BACK_CANCEL)?IAB_BUY_CANCEL:IAB_BUY_FAIL);
						
						try{
							Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
							Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
							bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						}catch(Exception ex ) {
							ERR(TAG,"Boku: Error invoking reflex method "+ex.getMessage());
						}
						
						break;
					}
				}
			} 
			else if (resultCode == Activity.RESULT_CANCELED) 
			{
			#if USE_DEBUG_TOAST
				String s = "There was a problem with the Activity";
				Toast.makeText(/*getApplicationContext()*/this, s, Toast.LENGTH_LONG).show();
			#endif
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
					ERR(TAG,"Boku: Error invoking reflex method "+ex.getMessage());
				}
				
			}
			finish();
		}
	}
	
	#define DELAY_TIME_FOR_GL_RETRY (1*60*1000)
#if RELEASE_VERSION	
	#define MAX_WAIT_TIME_FOR_PENDING 	(24*60*60*1000) //24 horas
#else	
	#define MAX_WAIT_TIME_FOR_PENDING 	(10*60*1000)
#endif

	static int [] DELAY_BETWEEN_PENDING_RETRYS = {	1*60*1000,
													3*60*1000,
													15*60*1000,
													30*60*1000,
													60*60*1000
												 };
	#define PENDING_DELAYS_NUM_OF_ELEMENTS 	4 //Total of elements in previous array
													
	private static boolean mIsConfirmInProgress = false;
	static void sendConfirmation()
	{
		if (mServerInfo == null)
			mServerInfo = GLOFTHelper.getServerInfo();
		if (mIsConfirmInProgress)
			return;
		//InAppBilling.save(WAITING_FOR_GAMELOFT);
		//InAppBilling.saveLastItem(WAITING_FOR_GAMELOFT);
		
	#if ITEMS_STORED_BY_GAME
		if (InAppBilling.mTransactionStep == WAITING_FOR_GAMELOFT)
		{
			Bundle bundle = new Bundle();
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
			
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
			
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_OK);
			SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
				
				
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
				int pendingRetry = 0;
				do 
				{
					try
					{
						INFO(TAG,"SendIABNotification for: InAppBilling.mCharId: "+InAppBilling.mCharId+" InAppBilling.mCharRegion: "+InAppBilling.mCharRegion);
						LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "SendIABNotification for: InAppBilling.mCharId: "+InAppBilling.mCharId+" InAppBilling.mCharRegion: "+InAppBilling.mCharRegion);
						XPlayer mXplayer = new XPlayer(new Device(mServerInfo));
						mXplayer.setUserCreds(InAppBilling.GET_GGLIVE_UID(),InAppBilling.GET_CREDENTIALS());
						mXplayer.sendIABNotification(0, InAppBilling.GET_STR_CONST(IAB_VALIDATION_ACTION_NAME), InAppBilling.mOrderID);

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
						if (InAppBilling.mTransactionStep == WAITING_FOR_BOKU && XPlayer.getLastErrorCode() == XPlayer.IAB_FAILURE_PENDING_ORDER_ID)
						{
							if (InAppBilling.IsInternetAvaliable())
							{
								String strTime = SUtils.getPreferenceString(GES(IAB_STR_WAITTING_TIME), GES(IAB_STR_PREF_NAME));
								if(strTime != null)
								{
									long savedTime = Long.parseLong(strTime);
									long currentTime = System.currentTimeMillis();
									
									if (currentTime - savedTime > MAX_WAIT_TIME_FOR_PENDING)
									{
										DBG(TAG, "Timmer ends, clearing pending status");
										LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Timmer ends, clearing pending status");
										InAppBilling.saveLastItem(STATE_FAIL);
										InAppBilling.mTransactionStep = 0;
										InAppBilling.clear();
										ShowDialog(IAB_BUY_FAIL);
										mIsConfirmInProgress = false;
										finish = true;
									}
									else 
									{
										JDUMP(TAG,DELAY_BETWEEN_PENDING_RETRYS[pendingRetry]);
										LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Delay bewteen pending retries:"+DELAY_BETWEEN_PENDING_RETRYS[pendingRetry]);
										try { 
											Thread.sleep(DELAY_BETWEEN_PENDING_RETRYS[pendingRetry++]);
										} catch (Exception exc) {}
										if (pendingRetry == PENDING_DELAYS_NUM_OF_ELEMENTS)
											--pendingRetry;
										continue;
									}
								}else
								{
									DBG(TAG,"no time information found");
									LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "no time information found");
								}
							}
						}else if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE 
						#if !ITEMS_STORED_BY_GAME
							|| 	XPlayer.getLastErrorCode() == XPlayer.IAB_FAILURE_INVALID_ORDER_ID
						#endif
							)
						{
							//Informing the game that the buy item proccess is completed and success.
							finish = true;
							mIsConfirmInProgress = false;
							InAppBilling.saveLastItem((XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)?STATE_SUCCESS:STATE_FAIL);
							InAppBilling.clear();
						
						#if ITEMS_STORED_BY_GAME
							if (InAppBilling.mTransactionStep == WAITING_FOR_BOKU)
						#endif //#if !ITEMS_STORED_BY_GAME
							{
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
							
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(InAppBilling.mItemID!=null)?InAppBilling.mItemID.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(InAppBilling.mReqList!=null)?InAppBilling.mReqList.getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
								
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 0);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)?IAB_BUY_OK:IAB_BUY_FAIL);
								SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
									
									
								try{
									Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
									Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
									bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
								}catch(Exception ex ) {
									ERR(TAG,"D: Error invoking reflex method "+ex.getMessage());
								}
							#if !ITEMS_STORED_BY_GAME	
								if(!InAppBilling.mBuyItemCallbackEnable || (XPlayer.getLastErrorCode() != XPlayer.ERROR_NONE))
								{
									ShowDialog((XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)?IAB_BUY_OK:IAB_BUY_FAIL);
								}
							#endif
							}	
						}
						else 
						{
							ERR(TAG,"Response Confirmation Fail");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Response Confirmation Fail");
							//InAppBilling.save(WAITING_FOR_GAMELOFT);
						}

					}catch(Exception exd)
					{
						DBG_EXCEPTION(exd);
						DBG(TAG,"No internet avaliable");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "No internet avaliable");
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
	
	public static void ShowDialog(final int result)
	{
	
		new Thread( new Runnable()
		{
			public void run()
			{
				Looper.prepare();
				String msg = null;
				if(result == IAB_BUY_OK)
				{
					msg = SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING_SUCCESSFUL);
					SUtils.getLManager().SaveUserPaymentType(LManager.CARRIER_PAYMENT);
				}else if(result == IAB_BUY_FAIL)
				{
					msg = SUtils.getContext().getString(R.string.IAB_PURCHASE_ITEM_PURCHASE_PENDING_UNSUCCESSFUL);
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(SUtils.getContext());
				builder.setTitle(R.string.app_name)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, null);
				Dialog dialog = builder.create();
				if (dialog != null)
				{
					dialog.setOwnerActivity((Activity)SUtils.getContext());
					dialog.show();
				}
				//Toast.makeText(SUtils.getContext(), msg, Toast.LENGTH_SHORT).show();
				Looper.loop();
			}
		}
	#if !RELEASE_VERSION
		,"Thread-DialogAfterPending"
	#endif	
		).start();
	}
	
	
	//Boku Success Result Code
	private final int BOKU_RESULT_CODE_SUCCESSFUL		= 0;
	
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
}
#endif //#if USE_IN_APP_BILLING && BOKU_STORE
