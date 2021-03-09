#if GAMELOFT_SHOP || USE_BILLING
package APP_PACKAGE.billing.common;

import android.os.Bundle;
import android.util.Log;

import APP_PACKAGE.GLUtils.Config;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
//import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

public abstract class AModel
{
	// *************************************************************************************************************
	// * VARIABLES
	// *************************************************************************************************************

	//public static String 	x_up_calling_line_id_from_sim = null;
	public static boolean    successResult = false;
	SET_TAG("AMODEL");
	
	protected static int mPurchaseType;
	protected static AModelActivity mBillingActivity;
	protected static Device	mDevice;
	
	AModel mBilling;
	
	/** Object to manage the connection. */
	protected static XPlayer xPlayer;
	
	
	/**
	* Release static variables
	* */
	
	public void release()
	{
		xPlayer = null;
		mDevice = null;
	
		mBillingActivity = null;
	}

	/**
	 * The implementation must handle the response from the server. Depending on which purchase type
	 * has been selected, it takes the decision of persisting on value type or
	 * the other, referring to purchase or subscription type.
	 * */
	public abstract void onValidationHandled();


	/**
	 * Checks the username and password and returns SUCCESS only if account exists.
	 */	

	public void sendLoginRequest() {
		
		DBG(TAG,"sendLoginRequest()");

		xPlayer = new XPlayer(mDevice, this);
		xPlayer.setUserInfo(mBillingActivity.getUserInfo());
		mPurchaseType	= Constants.PURCHASE_CC_LOGIN;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_CC_LOGIN);
	}
	/**
	 * Bills a registered user with a CC on the database
	 */	
	public void sendUserBillRequest() {
		
		DBG(TAG,"sendUserBillRequest()");

		xPlayer = new XPlayer(mDevice, this);
		xPlayer.setUserInfo(mBillingActivity.getUserInfo());
		mPurchaseType	= Constants.PURCHASE_CC_USERBILL;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_CC_USERBILL);
	}
	/**
	 * Creates a new MyAccount user, adds the specified credit card and tries to bill
	 */	
	public void sendNewUserBillRequest() {
		
		DBG(TAG,"sendNewUserBillRequest()");

		xPlayer = new XPlayer(mDevice, this);
		xPlayer.setUserInfo(mBillingActivity.getUserInfo());
		mPurchaseType	= Constants.PURCHASE_CC_NEWUSERBILL;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_CC_NEWUSERBILL);
	}
	/**
	 * Adds the specified credit card to the user, and tries to bill
	 */	
	public void sendAddCardBillRequest() {
		
		DBG(TAG,"sendNewUserBillRequest()");

		xPlayer = new XPlayer(mDevice, this);
		xPlayer.setUserInfo(mBillingActivity.getUserInfo());
		mPurchaseType	= Constants.PURCHASE_CC_ADDCARDBILL;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_CC_ADDCARDBILL);
	}
	/**
	 * Bills an user as Anonymous without storing the credit card or the user data. Email is required for invoice
	 */	
	public void sendSingleBillRequest() {
		
		DBG(TAG,"sendUserBillRequest()");

		xPlayer = new XPlayer(mDevice, this);
		xPlayer.setUserInfo(mBillingActivity.getUserInfo());
		mPurchaseType	= Constants.PURCHASE_CC_SINGLEBILL;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_CC_SINGLEBILL);
	}
	/**
	 * Sends an email to user to initiate the password recovery process.
	 */	
	public void sendForgotPasswordRequest() {
		
		DBG(TAG,"sendForgotPasswordRequest()");

		xPlayer = new XPlayer(mDevice, this);
		xPlayer.setUserInfo(mBillingActivity.getUserInfo());
		mPurchaseType	= Constants.PURCHASE_CC_FORGOTPW;
		xPlayer.sendValidationViaServer(Constants.PURCHASE_CC_FORGOTPW);
	}
	public void buyFullVersion() { }
	public void MRCFullVersion() { } 	
	public void buyFullSMS() { }	
	public void FailSMSbyTime() { }

}

#endif //#if GAMELOFT_SHOP || USE_BILLING