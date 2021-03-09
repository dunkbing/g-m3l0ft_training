#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.os.Bundle;
import android.util.Log;

import APP_PACKAGE.billing.common.Constants;
import APP_PACKAGE.billing.common.AModel;
import APP_PACKAGE.billing.common.AModelActivity;
import APP_PACKAGE.iab.InAppBilling;
import APP_PACKAGE.GLUtils.Config;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
//import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

public class Model extends AModel implements Config, Constants
{
	// *************************************************************************************************************
	// * VARIABLES
	// *************************************************************************************************************

	//public static String 	x_up_calling_line_id_from_sim = null;
	//public static boolean    successResult = false;
	SET_TAG("IAP_MODEL");


	public Model(AModelActivity activity, Device device)
	{
		mDevice 			= device;
		mBillingActivity 	= activity;
		
		successResult = false;
	}

	/**
	 * Handles the response from the server. Depending on which purchase type
	 * has been selected, it takes the decision of persisting on value type or
	 * the other, referring to purchase or subscription type.
	 * */
	@Override
	public void onValidationHandled()
	{
		DBG(TAG,"Model onValidationHandled");
		LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "Model onValidationHandled");
		
		if (xPlayer != null && xPlayer.handleValidateLicense()) {
			
			DBG(TAG,"onValidationHandled INSIDE CONDITION");
			
			switch(mPurchaseType)
			{
				case PURCHASE_HTTP:
					DBG(TAG,"PURCHASE_HTTP");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "PURCHASE_HTTP");
					mBillingActivity.updateBillingResult(successResult, XPlayer.getLastErrorMessageId());			
				break;
				case PURCHASE_CC_LOGIN:
					DBG(TAG,"onValidationHandled Login");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onValidationHandled Login");
					mBillingActivity.updateCCLogin(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_USERBILL:
					DBG(TAG,"onValidationHandled UserBill");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onValidationHandled UserBill");
					mBillingActivity.updateCCUserBill(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_NEWUSERBILL:
					DBG(TAG,"onValidationHandled New UserBill");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onValidationHandled New UserBill");
					mBillingActivity.updateCCNewUserBill(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_ADDCARDBILL:
					DBG(TAG,"onValidationHandled Add Card Bill");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onValidationHandled Add Card Bill");
					mBillingActivity.updateCCAddCardBill(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_SINGLEBILL:
					DBG(TAG,"onValidationHandled SingleBill");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onValidationHandled SingleBill");
					mBillingActivity.updateCCSingleBill(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_FORGOTPW:
					DBG(TAG,"onValidationHandled Forgot Password");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "onValidationHandled Forgot Password");
					mBillingActivity.updateCCForgotPassword(successResult, XPlayer.getLastErrorCode());
				break;
			}
			successResult = false;
		}
	}

}

#endif