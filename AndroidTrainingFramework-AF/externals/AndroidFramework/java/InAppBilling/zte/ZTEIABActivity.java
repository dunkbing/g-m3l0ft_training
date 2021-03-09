#if USE_IN_APP_BILLING && ZTE_STORE
package APP_PACKAGE.iab;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class ZTEIABActivity extends Activity
{
	SET_TAG("InAppBilling");
	private static 	String mZTEProductID 	 = null;
	private static 	String mbillno 		     = null;
	private static 	ZTEIABActivity mInstance = null;
	public static 	ZTEIABActivity getInstance() {return mInstance;}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		INFO(TAG, "[onCreate]");
		
		mInstance = this;
		registerReceiver();
		ZTEHelper.OrderRequest();
	}
	
	public void setOrderMessage(String partnerId, String productId, String orderId, String subject, String total_fee, String gameName, String productName) {
		INFO(TAG, "[setOrderMessage]");
		Intent intent = new Intent();
		intent.putExtra("partnerId", partnerId);
		intent.putExtra("productId", orderId);
		intent.putExtra("subject", subject);
		intent.putExtra("total_fee", total_fee);
		intent.putExtra("gameName", gameName);
		intent.putExtra("productName", productName);
		DBG(TAG, "[partnerId]= "+partnerId+" [productId]= "+productId+" [orderId]= "+orderId+" [subject]= "+subject+" [total_fee]= "+total_fee+" [gameName]= "+gameName+" [productName]= "+productName);
		start(intent);
	}

	private void start(Intent intent) {
		try{
			intent.setAction(CommonUtils.ACTION_NAME);
			intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			startActivityForResult(intent, 5);
		}catch(Exception ex)
		{
			ERR(TAG, "ZTE IAP Service not available!!!");
			DBG_EXCEPTION(ex);
			ZTEHelper.IABResultCallBack(IAB_BUY_FAIL);
			mInstance.finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		INFO(TAG, "[onDestroy]");
		unRegisterReceiver();
	}
	
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonUtils.GET_RESULT_CODE_TRUE);
		registerReceiver(mGetServiceMessge, filter);
	}

	private void unRegisterReceiver(){
		unregisterReceiver(mGetServiceMessge);
	}
	
	public BroadcastReceiver mGetServiceMessge = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			INFO(TAG, "onReceive");
			
			String action = intent.getAction();
			DBG(TAG, "   ---     action:" + action);
			
			if (action == CommonUtils.GET_RESULT_CODE_TRUE) {
				boolean boolenString = intent.getBooleanExtra("boolean", false);
				String partnerId = intent.getStringExtra(CommonUtils.PARTNERID);
				String productId = intent.getStringExtra(CommonUtils.PRODUCTID);
				
				DBG(TAG, "   ---     boolenString:" + boolenString);
				DBG(TAG, "   ---     partnerId:" + partnerId);
				DBG(TAG, "   ---     productId:" + productId);
				
				if(boolenString)
					ZTEHelper.IABResultCallBack(IAB_BUY_OK);
				else
					ZTEHelper.IABResultCallBack(IAB_BUY_FAIL);
					
				mInstance.finish();
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			DBG(TAG, "---------resultCode------" + resultCode + "---requestCode----------" + requestCode + data.getBooleanExtra("boolean", false));
		} else {
			DBG(TAG, "---------resultCode------" + resultCode + "---requestCode----------" + requestCode);
		}
	}
	
	public class CommonUtils 
	{
	//////////////////////////
		public final static String ACTION_NAME = "com.example.payment.activity.PaymentWindowActivity";
		public final static String GET_RESULT_CODE_TRUE = "GET_RESULT_CODE_TRUE";
		public static final String PARTNERID = "partnerId";
		public static final String PRODUCTID = "productId";
	///////////////////////////
	}
}
#endif
