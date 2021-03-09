#if USE_BILLING
package APP_PACKAGE.billing;

import android.os.Bundle;
import android.util.Log;

import APP_PACKAGE.billing.common.AModel;
import APP_PACKAGE.billing.common.AModelActivity;
import APP_PACKAGE.billing.common.Constants;
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
	SET_TAG("BILLING_MODEL");


	private static SMS mSMS;
	
	public Model(AModelActivity activity, Device device)
	{
		mDevice 			= device;
		mBillingActivity 	= activity;
		
		successResult = false;
	}
	
	/**
	* Release static variables
	* */
	
	public void release()
	{
		super.release();
		
		if(mSMS != null)
			mSMS.release();
			
		mSMS	= null;
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
		
		
		if (xPlayer != null && xPlayer.handleValidateLicense()) {
			
			DBG(TAG,"onValidationHandled INSIDE CONDITION");
			
			switch(mPurchaseType)
			{
				case VALIDATE_PURCHASE:
					DBG(TAG,"VALIDATE_PURCHASE");
					mBillingActivity.updateValidationResult(successResult, XPlayer.getLastErrorMessageId());	
				break;
				case PURCHASE_HTTP:
					DBG(TAG,"PURCHASE_HTTP");
					mBillingActivity.updateBillingResult(successResult, XPlayer.getLastErrorMessageId());			
				break;
				case PURCHASE_MRC:
					DBG(TAG,"PURCHASE_MRC");
					mBillingActivity.updateBillingResult(successResult, XPlayer.getLastErrorMessageId());			
				break;
				case PURCHASE_CC_LOGIN:
					DBG(TAG,"onValidationHandled Login");
					mBillingActivity.updateCCLogin(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_USERBILL:
					DBG(TAG,"onValidationHandled UserBill");
					mBillingActivity.updateCCUserBill(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_NEWUSERBILL:
					DBG(TAG,"onValidationHandled New UserBill");
					mBillingActivity.updateCCNewUserBill(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_SINGLEBILL:
					DBG(TAG,"onValidationHandled SingleBill");
					mBillingActivity.updateCCSingleBill(successResult, XPlayer.getLastErrorCode());
				break;
				case PURCHASE_CC_FORGOTPW:
					DBG(TAG,"onValidationHandled Forgot Password");
					mBillingActivity.updateCCForgotPassword(successResult, XPlayer.getLastErrorCode());
				break;
			}
			successResult = false;
		}else if (mSMS != null && mSMS.isCompleted())
		{
			mBillingActivity.updateBillingResult(successResult, mSMS.getMessageID());
			successResult = false;
		}
	}
	
	/**
	 * Called when the user confirms to pay to unlock the full version
	 */
	@Override
	public void buyFullVersion()
	{
		DBG(TAG,"buyFullVersion()");

		xPlayer = new XPlayer(mDevice, this);
		mPurchaseType	= Constants.PURCHASE_HTTP;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_HTTP);
	}
	
	/**
	 * Called when the user confirms to pay to unlock the full version
	 */
	@Override
	public void MRCFullVersion()
	{
		DBG(TAG,"buyFullVersion()");

		xPlayer = new XPlayer(mDevice, this);
		mPurchaseType	= Constants.PURCHASE_MRC;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_MRC);
	}

	
	@Override
	public void buyFullSMS()
	{
		DBG(TAG,"buyFullSMS()");
		mSMS = new SMS(mBillingActivity,mDevice);
		mPurchaseType	= Constants.PURCHASE_HTTP;
		mSMS.sendUnlockMessage();
	}
	
	@Override
	public void FailSMSbyTime()
	{
		mSMS.handleValidateLicense(false, R.string.AB_TRANSACTION_FAILED);
	}

#if USE_BILLING_FOR_CHINA
	/**
	 * Called when the user buy with IPX method
	 */	
	public void buyFullIPX()
	{
		DBG(TAG,"buyFullIPX()");
		mSMS = new SMS(mBillingActivity,mDevice);
		mPurchaseType	= Constants.PURCHASE_HTTP;
		mSMS.sendUnlockMessageIPX();
	}
#endif
	
	/**
	 * Checks the username and password and returns SUCCESS only if account exists.
	 */	




}

#endif