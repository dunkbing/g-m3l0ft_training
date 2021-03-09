#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class PurchaseInfo {
	String mOrderId;
	String mPackageName;
	String mSku;
	long mPurchaseTime;
	int mPurchaseState;
	String mDeveloperPayload;
	String mToken;
	String mOriginalJson;
	String mSignature;


	public PurchaseInfo()
	{
	}

	public PurchaseInfo(String jsonPurchaseInfo, String signature) throws JSONException {
		mOriginalJson 		= jsonPurchaseInfo;
		JSONObject o 		= new JSONObject(mOriginalJson);
		mOrderId 			= o.optString("orderId");
		mPackageName 		= o.optString("packageName");
		mSku 				= o.optString("productId");
		mPurchaseTime 		= o.optLong("purchaseTime");
		mPurchaseState 		= o.optInt("purchaseState");
		mDeveloperPayload 	= o.optString("developerPayload");
		mToken 				= o.optString("token", o.optString("purchaseToken"));
		mSignature = signature;
	}

	public String getOrderId() { return mOrderId; }
	public String getPackageName() { return mPackageName; }
	public String getSku() { return mSku; }
	public long getPurchaseTime() { return mPurchaseTime; }
	public int getPurchaseState() { return mPurchaseState; }
	public String getDeveloperPayload() { return mDeveloperPayload; }
	public String getToken() { return mToken; }
	public String getOriginalJson() { return mOriginalJson; }
	public String getSignature() { return mSignature; }

	@Override
	public String toString()
	{
		if (!TextUtils.isEmpty(mOriginalJson)) 
			return mOriginalJson; 
		
		//Used for consume products when there isn't the original Json.
		try{
			JSONObject o = new JSONObject();
			o.put("productId", mSku);
			o.put("token", mToken);
			return o.toString();
		}
		catch (JSONException e) { DBG_EXCEPTION(e);}
		return "";
	}
}
#endif //USE_IN_APP_BILLING