package APP_PACKAGE.iab;

import android.support.v4.app.Fragment;
import com.skplanet.dodo.IapPlugin;
import com.skplanet.dodo.IapResponse;

import APP_PACKAGE.iab.pdu.Response;
import APP_PACKAGE.iab.helper.ConverterFactory;
import APP_PACKAGE.iab.cItem;
import APP_PACKAGE.iab.InAppBilling;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

public class CommandFragment extends Fragment
{
	private IapPlugin mPlugin;
	SET_TAG("InAppBilling");
	private SktIabActivity mActivityInstance;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPlugin = IapPlugin.getPlugin(activity, SktIabActivity.library_server);
		mActivityInstance = (SktIabActivity)activity;
		
		if (!requestWholeAuthItem())
		{
			if (mActivityInstance!= null && mActivityInstance.pDialog != null)
				mActivityInstance.pDialog.dismiss();
		}
    }
	
	@Override
    public void onDetach() {
        super.onDetach();
        mPlugin.exit();
    }
	
	private boolean requestWholeAuthItem()
	{
		String json = "{";
		json += "\"method\":\"whole_auth_item\",\"param\":";
		json += "{" + "\"appid\":\"" + SktIabActivity.appID + "\",\"product_id\":[]}";
		json += "}";
		DBG(TAG, "request json="+json);
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "request json="+json);
		Bundle req = mPlugin.sendCommandRequest(json,
			new IapPlugin.RequestCallback() {
				
				@Override
                public void onResponse(IapResponse data)
				{
					DBG(TAG, "onResponse");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "requestWholeAuthItem::onResponse");
					if (mActivityInstance.pDialog != null)
						mActivityInstance.pDialog.dismiss();
					
					if (data == null || data.getContentLength() == 0) {
                        // TODO Unusual error
						WARN(TAG, "data null or data length is 0");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "data null or data length is 0");
                        return;
                    }
					
					Response response = ConverterFactory.getConverter().fromJson(
                                data.getContentToString());

                    #if !RELEASE_VERSION
					StringBuffer sb = new StringBuffer("onResponse() \n");

                    sb.append("From:" + data.getContentToString())
                        .append("\n")
                        .append("To:" + response.toString());
                    
					DBG(TAG, sb.toString());
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, sb.toString());
					#endif
					//	process items
					List<Response.Product> products = response.result.product;
					ArrayList<cItem> items = InAppBilling.mServerInfo.getShopProfile().getItemList();
					
					if (products!=null)
					{
						for (int x=0;x<products.size();x++)
						{
							String id = products.get(x).id;
							INFO(TAG, id);
							for (int i=0; i<items.size(); i++)
							{
								cItem tmpItem = items.get(i);
								cBilling tmpBilling = tmpItem.getDefaultBilling();
								
								if (id.equalsIgnoreCase(tmpBilling.getAttributeByName("pid")))
								{
									//validate if is managed
									if (tmpItem.getAttributeByName("managed").equalsIgnoreCase("1"))
									{
										INFO(TAG, "We have one managed item: "+tmpItem.getId());
										LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "We have one managed item: "+tmpItem.getId());
										
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
								}
							}
						}
					}
					//mUiHandler.obtainMessage(100, sb.toString()).sendToTarget();
					mActivityInstance.finish();
				}
				
				@Override
				public void onError(String reqid, String errcode, String errmsg)
				{
					ERR(TAG, "onError");
					ERR(TAG, "[" + errcode + "] " + errmsg);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "onError: [" + errcode + "] " + errmsg);
					if (mActivityInstance.pDialog != null)
						mActivityInstance.pDialog.dismiss();
				}
			});
		
		if (req == null) {
            // TODO request failure
			ERR(TAG, "command request failed");
            return false;
        }
		
		String id = req.getString(IapPlugin.EXTRA_REQUEST_ID);
        if (id == null || id.length() == 0) {
            // TODO request failure
			ERR(TAG, "requestID is empty");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "requestID is empty");
            return false;
        }
		return true;
	}
}