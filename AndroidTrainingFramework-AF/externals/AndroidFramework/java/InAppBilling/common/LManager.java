#if USE_IN_APP_BILLING || USE_BILLING
package APP_PACKAGE.billing.common;
import java.util.Date;
import java.util.Random;

import android.util.Log;
import APP_PACKAGE.GLUtils.Config;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.R;

public class LManager 
{
    
	
	//*************************************************************************************************************
	//* CONSTANTS
	//*************************************************************************************************************
    // Application Preferences (Records)
    public static final String			PREFERENCES_NAME 						= Config.GGC+"BInfo";
    // To know if the game is unlocked or not.
    private final String			PREFERENCES_GAME_UNLOCKED 				= "PREFERENCES_GAME_UNLOCKED";
    // If there has been a time change on the device, it will be necessary to validate the subscription with the server. 
    private final String			PREFERENCES_NEED_VALIDATION_ON_SERVER	= "PREFERENCES_NEED_VALIDATION_ON_SERVER";

    // The game unlock code
	private final String			PREFERENCES_GAME_UNLOCK_CODE 			= "PREFERENCES_GAME_UNLOCK_CODE";

	//The random code to send
	private final String 			PREFERENCES_GAME_RANDOM_CODE 			= "PREFERENCES_GAME_RANDOM_CODE";

	// The game sent flag for messages
	public static final String			PREFERENCES_GAME_MESSAGE_SEND 			= "PREFERENCES_GAME_MESSAGE_SEND";
	
	private final String			PREFERENCES_GAME_SERVER_NUMBER 			= "PREFERENCES_GAME_SERVER_NUMBER";

	private final String			PREFERENCES_USER_ISVALID 				= "PREFERENCES_USER_IDVALID";
	private final String			PREFERENCES_USER_CC 					= "PREFERENCES_USER_CC";
	private final String			PREFERENCES_USER_CC_LAST_NUMBERS 		= "PREFERENCES_USER_CC_LAST_NUMBERS";
	private final String			PREFERENCES_USER_EMAIL 					= "PREFERENCES_USER_EMAIL";
	private final String			PREFERENCES_USER_PASSWORD 				= "PREFERENCES_USER_PASSWORD";
	
	#if SHENZHOUFU_STORE
	private final String			PREFERENCES_SHENZHOUFU_CHECKBOX_CHECKED			= "PREFERENCES_SHENZHOUFU_CHECKBOX_CHECKED";
	private final String			PREFERENCES_SHENZHOUFU_CARD_TYPE 				= "PREFERENCES_SHENZHOUFU_CARD_TYPE";
	private final String			PREFERENCES_SHENZHOUFU_CARD_DENOMINATION	 	= "PREFERENCES_SHENZHOUFU_CARD_DENOMINATION";
	private final String			PREFERENCES_SHENZHOUFU_CARD_NUMBER 				= "PREFERENCES_SHENZHOUFU_CARD_NUMBER";
	private final String			PREFERENCES_SHENZHOUFU_CARD_PASSWORD 			= "PREFERENCES_SHENZHOUFU_CARD_PASSWORD";
	#endif
	
	private final String			PREFERENCES_USER_LAST_PAYMENT 			= "PREFERENCES_USER_LAST_PAYMENT";
	
	public static final int			CARRIER_PAYMENT							= 0;
	public static final int			PAYPAL_PAYMENT							= 1;
	public static final int			CREDIT_CARD_PAYMENT						= 2;
	#if SHENZHOUFU_STORE
	public static final int			SHENZHOUFU_PAYMENT						= 3;
	#endif

	//MRC STUFF
	public final int FULL_VERSION		= 1;
	public final int SUBSCRIPTION		= 2;
#if USE_BILLING && USE_BOKU_FOR_BILLING
	public static final int FULL_VERSION_BOKU	= 3;
	public static final int BOKU_UNLOCKED		= 0;
	public static final int BOKU_LOCKED			= 1;
#endif
	
	private final int MRC_SERVER_ERROR	= -1;
	private final int MRC_NOT_VALID		= 0;
	private final int MRC_VALID			= 1;
	private final int MRC_NO_ACTIVE		= 0;
	private final int MRC_ACTIVE			= 1;
	private final int MAX_PLAY_COUNT		= 3;

	private final String PREFERENCES_FULL_LICENSE		= "PREFERENCES_FULL_LICENSE";
#if USE_BILLING && USE_BOKU_FOR_BILLING
	public static final String PREFERENCES_FULL_LICENSE_BOKU    = "PREFERENCES_FULL_LICENSE_BOKU";
	public static final String PREFERENCES_BOKU_GAME_PRICE    	= "PREFERENCES_BOKU_GAME_PRICE";
	public static final String PREFERENCES_BOKU_MONEY_ID   		= "PREFERENCES_BOKU_MONEY_ID";
#endif

#if USE_BILLING && USE_BILLING_FOR_CHINA
	public static final String PREFERENCES_SMS_IPX_MONEY		= "PREFERENCES_SMS_IPX_MONEY";
	public static final String PREFERENCES_SMS_IPX_COUNT		= "PREFERENCES_SMS_IPX_COUNT";
	public static final String PREFERENCES_SMS_UMP_PENDING		= "PREFERENCES_SMS_UMP_PENDING";
#endif
	private final String PREFERENCES_MRC_ACTIVE			= "PREFERENCES_MRC_ACTIVE";
	private final String PREFERENCES_MRC_OFFLINE_COUNT	= "PREFERENCES_MRC_COUNT";
	private final String PREFERENCES_MRC_VALID			= "PREFERENCES_MRC_VALID";
	private final String PREFERENCES_MRC_LICENSE		= "PREFERENCES_MRC_LICENSE";
	
	private final String LICENSE_KEY					= "$JS6&GJH5$3%H&4@KECVF$56$Y$N792$&44O8B";
	SET_TAG("LManager");

#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
	private final String PREFERENCES_SMS_RESULT			= "PREFERENCES_MTKMT";
#endif	
					
	//*************************************************************************************************************
	//* ATTRIBUTES
	//*************************************************************************************************************
    
    private static Device mDevice;
	private XPlayer mXPlayer;
	private StringEncrypter mEncrypter = null;
	public static boolean isUsingTracking = false;

	
	
    //*************************************************************************************************************
    //* METHODS
    //*************************************************************************************************************
	public LManager ()
	{
		if (mDevice == null)
			mDevice = new Device();
		if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());;
	}
	
	/**
	 * Returns the Device IMEI or HDIDFV, depending the selected configuration, the IMEI was used at first as a seed for the encryption proccess.
	 * */
	public String getIMEI_HDIDVF()
	{	
	#if (HDIDFV_UPDATE == 2)
		DBG("I_S"+HDIDFV_UPDATE, mDevice.getHDIDFV());
		return mDevice.getHDIDFV();
	#elif (HDIDFV_UPDATE == 1) && !(GOOGLE_STORE_V3)
		return mDevice.getIMEI();
	#elif (HDIDFV_UPDATE == 1)
		DBG("I_S"+HDIDFV_UPDATE, mDevice.getHDIDFV());
		return mDevice.getHDIDFV();
	#else
		return mDevice.getIMEI();
	#endif
	}
	/**
	 * Returns the current date in milliseconds.
	 * */
	public long today() {
	    return new Date().getTime();
	}
#if USE_BILLING
	 /**
	  * Reset the game as DEMO status due failure on MRC license validation or expiration
	  */
	 public void lockGame()
	 {
		SUtils.setPreference(PREFERENCES_GAME_UNLOCKED, encryptType(LOCKED), PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_FULL_LICENSE, "NULL", PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_MRC_OFFLINE_COUNT,encryptFreeCount(0), PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_MRC_LICENSE, "NULL", PREFERENCES_NAME);
	 }
	 
    /**
     * Determines if the game must be launched in FULL or DEMO mode.
     * @return true if the game must be launched in DEMO mode, false otherwise
     */
    public boolean isDemo()
    {
   		//rmv mDevice = new Device();

    	if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());

    	return !isUnlocked();
    }
	
	/**
     * Depending on the purchase level (FULL PURCHASE, or SUBSCRIPTION), this method will allow
     * to know the value of the current purchased status and the license validation.
     * @return PURCHASE_LEVEL (FULL_VERSION,SUBSCRIPTION);
     */
	
	public boolean isUnlocked() 
    {
		
		if(decryptType() == FULL_VERSION)
    	{
    		DBG("Billing","FULL VERSION FOUND CHECKING FULL LICENSE");
    		DBG("Billing","getValue(PREFERENCES_FULL_LICENSE)="+getValue(PREFERENCES_FULL_LICENSE));
    		DBG("Billing","getIMEI_HDIDVF()+LICENSE_KEY="+getIMEI_HDIDVF()+LICENSE_KEY);
    		
    		if(getValue(PREFERENCES_FULL_LICENSE).equals(""+getIMEI_HDIDVF()+""+LICENSE_KEY))
    		{
    			DBG("Billing","FULL LICENSE VALID");
    			return (true);
    		}
    		else
    		{
    			DBG("Billing","NO VALID FULL LICENSE FOUND");
    			return (false);
    		}
    	}
    	else if(decryptType() == SUBSCRIPTION)
    	{
    		DBG("Billing","MRC SUBSCRIPTION VERSION FOUND");	
    		return (ValidateSubscription());
    	}
    	else
    	{
    		DBG("Billing","NO FULL OR MRC LICENSE FOUND");
    		return(false);
    	}
    }

	public boolean ValidateSubscription()
	{
		boolean isValid = false;
		int mResponse = MRC_SERVER_ERROR;
		
		DBG("Billing","Validating MRC Subscription...");
		mResponse = ValidateServerSubscription();
		
		DBG("Billing","Response value after validation on server: "+mResponse);
		if(mResponse == MRC_SERVER_ERROR)//Not possible to reach our server
		{
			DBG("Billing","Response shows a server error, looking for a LOCAL license");
			isValid = ValidateLocalSubscription();//Goto validate the Local License
		}
		else
		{
			DBG("Billing","Sending back the current demo status");
			isValid = (mResponse == MRC_VALID);//Validate the Server Response
		}
		return (isValid);
	}
	
	public int ValidateServerSubscription()
	{
   		//rmv mDevice = new Device();
		
		mXPlayer = new XPlayer(mDevice);
		mXPlayer.sendValidationRequest();
		
		while (!mXPlayer.handleMRCValidationRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		switch(XPlayer.getLastErrorCode())
		{
			case MRC_VALID:
				DBG("Billing", "MRC SUSCRIPTION VALIDATED BY SERVER");
				saveUnlockGame(SUBSCRIPTION);
				return (MRC_VALID);
			
			case MRC_NOT_VALID:
				DBG("Billing", "MRC SUSCRIPTION DENIED BY SERVER");
				lockGame();
				return (MRC_NOT_VALID);
			
			case MRC_SERVER_ERROR:
				return (MRC_SERVER_ERROR);
		}

		return (MRC_SERVER_ERROR);
	}
	
	public boolean ValidateLocalSubscription()
	{
		boolean isValid = false;
		
		DBG("Billing","Looking for a VALID LOCAL license");
		if(getValue(PREFERENCES_MRC_LICENSE).equals(""+getIMEI_HDIDVF()+""+LICENSE_KEY))
		{
			DBG("Billing","LOCAL License FOUND!");
			
			//int gamesAllowedCounter = getPreferenceInt(PREFERENCES_GAME_RANDOM_CODE, -1);
			int gamesAllowedCounter = decryptFreeCount();
			
			if(gamesAllowedCounter < 3)
			{
				
				SUtils.setPreference(PREFERENCES_MRC_OFFLINE_COUNT,encryptFreeCount(++gamesAllowedCounter),PREFERENCES_NAME);
				DBG("Billing","Actual game counter is: "+gamesAllowedCounter);
				isValid= true;
			}
			else
			{
				DBG("Billing","NO MORE OFFLINE GAMES ALLOWED VALIDATION NEEDED!!!");
				isValid= false;
			}
		}
		else
		{
			DBG("Billing","NO LOCAL LICENSE WAS FOUND!!!");
			isValid= false;
		}
		return (isValid);
	}

	public String GenerateLicense()
	{
		DBG("Billing","Encrypted License="+getEncrypt(""+getIMEI_HDIDVF()+""+LICENSE_KEY));
		return(getEncrypt(""+getIMEI_HDIDVF()+""+LICENSE_KEY));
	}
	
	/**
     * This Method will validate the game if this was previously purchased from any method
     * @return <code>true</code> if this demo has been purchased previously
     * or <code>false</code> if the game never has been purchased
     */
    public boolean ValidateGame()
    {
		
    	//rmv mDevice = new Device();
		mXPlayer = new XPlayer(mDevice);
		
    	if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
	
	#if USE_BILLING_FOR_CHINA
		// check purchase for IPX
		String server = "https://secure.gameloft.com/unlock_cn/validation.php";
		String query = "method=check"+"&imei="+mDevice.getIMEI()+"&product="+GL_PRODUCT_ID+"&sign="+XPlayer.hmacSha1("_gloft_cn_2012","methodcheckimei"+mDevice.getIMEI()+"product"+GL_PRODUCT_ID);
		mXPlayer.sendRequest(server, query);
		while (!mXPlayer.handleRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		}
		String tmpHR = mXPlayer.getWHTTP().m_response;
		if(tmpHR != null && tmpHR.contains("success"))
		{
			DBG("Billing", "THIS IS A FULL VERSION PREVIOUSLY BILLED");
			saveUnlockGame(FULL_VERSION);
			return true;
		}
		else
		{
			DBG("Billing", "THIS IS NOT A FULL VERSION!!!!");
		}
	#endif
		mXPlayer.sendValidationRequest();
		while (!mXPlayer.handleValidationRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		if((mDevice.getBillingVersion().equals("3")) && (XPlayer.getLastErrorCode() == MRC_VALID))
		{
			DBG("Billing", "THIS IS A FULL VERSION(MRC)PREVIOUSLY BILLED");
			saveUnlockGame(SUBSCRIPTION);
			return true;
		}
		else if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{		
			DBG("Billing", "THIS IS A FULL VERSION PREVIOUSLY BILLED");
			saveUnlockGame(FULL_VERSION);
			return true;
		}
		else
		{
			DBG("Billing", "THIS IS NOT A FULL VERSION!!!!");
			return false;
		}
    }
    
    public boolean ValidatePromoCode(String mPromoCode, String URL)
    {
		
    	//rmv mDevice = new Device();
		mXPlayer = new XPlayer(mDevice);
		
		mXPlayer.sendPromoCodeValidationRequest(mPromoCode,URL);
		while (!mXPlayer.handlePromoCodeValidationRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
			//DBG("Billing", "[Validating FULL VERSION GAME]Waiting for response...");
		} 
		DBG("Billing", "XPlayer.getLastErrorCode() = "+XPlayer.getLastErrorCode());
		if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{		
			DBG("Billing", "THIS PROMO CODE IS VALID");
			saveUnlockGame(FULL_VERSION);
			return true;
		}
		else
		{
			DBG("Billing", "THIS IS AN INVALID PROMO CODE");
			return false;
		}
    }
	
	public boolean isServerValidationRequired()
	{
	    return SUtils.getPreferenceBoolean(PREFERENCES_NEED_VALIDATION_ON_SERVER, true, PREFERENCES_NAME);
	}

	public void saveUnlockGame(int UNLOCK_TYPE)
	{
		//rmv mDevice = new Device();

		if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
		
		if(UNLOCK_TYPE == FULL_VERSION)
		{
			DBG("Billing","Setting FULL LICENSE");
			SUtils.setPreference(PREFERENCES_GAME_UNLOCKED, encryptType(FULL_VERSION), PREFERENCES_NAME);
			SUtils.setPreference(PREFERENCES_FULL_LICENSE, GenerateLicense(), PREFERENCES_NAME);
			DBG("Billing","FULL LICENSE SUCCESSFULLY STORED");
		}
		else if(UNLOCK_TYPE == SUBSCRIPTION)
		{
			DBG("Billing","Setting MRC LICENSE");
			SUtils.setPreference(PREFERENCES_GAME_UNLOCKED, encryptType(SUBSCRIPTION), PREFERENCES_NAME);
			DBG("Billing","Resetting offline games counter");
			SUtils.setPreference(PREFERENCES_MRC_OFFLINE_COUNT,encryptFreeCount(0), PREFERENCES_NAME);
			DBG("Billing","Generating License");
			SUtils.setPreference(PREFERENCES_MRC_LICENSE, GenerateLicense(), PREFERENCES_NAME);
			DBG("Billing","MRC LICENSE SUCCESSFULLY STORED");
		}
	}
	
	
#endif //#if USE_BILLING	
#if GAMELOFT_SHOP	

	#if SHENZHOUFU_STORE
	public void SecureShenzhoufuUserValues(UserInfo mUserInfo)
	{
		SUtils.setPreference(PREFERENCES_SHENZHOUFU_CHECKBOX_CHECKED, true, PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_SHENZHOUFU_CARD_TYPE, getEncrypt(mUserInfo.getCardType()), PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_SHENZHOUFU_CARD_DENOMINATION, getEncrypt(mUserInfo.getCardDenomination()), PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_SHENZHOUFU_CARD_NUMBER, getEncrypt(mUserInfo.getShenzhoufuCardNumber()),PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_SHENZHOUFU_CARD_PASSWORD, getEncrypt(mUserInfo.getCardPassword()),PREFERENCES_NAME);
	}
	
	public void ClearShenzhoufuUserValues()
	{
		SUtils.removePreference(PREFERENCES_SHENZHOUFU_CHECKBOX_CHECKED, PREFERENCES_NAME);
		SUtils.removePreference(PREFERENCES_SHENZHOUFU_CARD_TYPE, PREFERENCES_NAME);
		SUtils.removePreference(PREFERENCES_SHENZHOUFU_CARD_DENOMINATION, PREFERENCES_NAME);
		SUtils.removePreference(PREFERENCES_SHENZHOUFU_CARD_NUMBER, PREFERENCES_NAME);
		SUtils.removePreference(PREFERENCES_SHENZHOUFU_CARD_PASSWORD, PREFERENCES_NAME);
	}
	
	public boolean GetCheckBoxChecked()
	{
		return(SUtils.getPreferenceBoolean(PREFERENCES_SHENZHOUFU_CHECKBOX_CHECKED, false, PREFERENCES_NAME));
	}
	
	public String GetCardType()
	{
		return(getValue(PREFERENCES_SHENZHOUFU_CARD_TYPE));
	}

	public String GetCardDenomination()
	{
		return(getValue(PREFERENCES_SHENZHOUFU_CARD_DENOMINATION));
	}

	public String GetCardNumber()
	{
		return(getValue(PREFERENCES_SHENZHOUFU_CARD_NUMBER));
	}

	public String GetCardPassword()
	{
		return(getValue(PREFERENCES_SHENZHOUFU_CARD_PASSWORD));
	}
	#endif

	public void SecureNewUserValues(UserInfo mUserInfo)
	{
		SUtils.setPreference(PREFERENCES_USER_ISVALID, true, PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_USER_CC, getEncrypt(mUserInfo.getCardNumber()), PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_USER_CC_LAST_NUMBERS, getEncrypt(mUserInfo.getLastCardNumbers()), PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_USER_EMAIL, getEncrypt(mUserInfo.getEmail()),PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_USER_PASSWORD, getEncrypt(mUserInfo.getPassword()),PREFERENCES_NAME);
	}
	
	public void SaveUserPaymentType(int PAYMENT_TYPE)
	{
		SUtils.setPreference(PREFERENCES_USER_LAST_PAYMENT, PAYMENT_TYPE, PREFERENCES_NAME);
	}

	public int GetUserPaymentType()
	{
		return(SUtils.getPreferenceInt(PREFERENCES_USER_LAST_PAYMENT, -1, PREFERENCES_NAME));
	}
	
	public void SecureUserValues(UserInfo mUserInfo)
	{
		SUtils.setPreference(PREFERENCES_USER_ISVALID, true, PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_USER_EMAIL, getEncrypt(mUserInfo.getEmail()),PREFERENCES_NAME);
		SUtils.setPreference(PREFERENCES_USER_PASSWORD, getEncrypt(mUserInfo.getPassword()),PREFERENCES_NAME);
	}
	
	public void AddNewCreditCard()
	{
		SUtils.setPreference(PREFERENCES_USER_ISVALID, false, PREFERENCES_NAME);
	}
	
	public boolean PreviousPurchase()
	{
		return(SUtils.getPreferenceBoolean(PREFERENCES_USER_ISVALID, false, PREFERENCES_NAME));
	}

	public String GetUserCC()
	{
		return(getValue(PREFERENCES_USER_CC));
	}

	public String GetUserCCLastNumbers()
	{
		return(getValue(PREFERENCES_USER_CC_LAST_NUMBERS));
	}

	public String GetUserName()
	{
		return(getValue(PREFERENCES_USER_EMAIL));
	}

	public String GetUserPassword()
	{
		return(getValue(PREFERENCES_USER_PASSWORD));
	}
#endif //#if GAMELOFT_SHOP	
	public String getEncrypt(String value)
	{
		String encode = new Random(System.currentTimeMillis()).nextLong() + "#" + getIMEI_HDIDVF() + "#" + value;
		encode = mEncrypter.encrypt(encode);
		
		return encode;
	}
	
	public String getEncrypt(int value)
	{
		//if(mEncrypter==null)
			mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
			
		String encode = new Random(System.currentTimeMillis()).nextLong() + "#" + getIMEI_HDIDVF() + "#" + value;
		encode = mEncrypter.encrypt(encode);

		return encode;
	}
	
	public String getValue(String key)
	{
		//if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
			
		String decode = SUtils.getPreferenceString(key,PREFERENCES_NAME);
		try
		{
			if(decode != "")
			{
				decode = mEncrypter.decrypt(decode);
				String[] values = decode.split("#");
				if( (values.length == 3) && (values[1].compareTo(getIMEI_HDIDVF()) == 0) )
				{
					decode = values[2];
				}
			}
		} catch(Exception e) {	ERR(TAG,"Error occurred while getting Value for "+key+" Error ="+e.toString()); }
		
		return decode;
	}
	
	public int getValue(String key, int errorValue)
	{
		//if(mEncrypter==null)
			mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
			
		String decode = SUtils.getPreferenceString(key,PREFERENCES_NAME);
		
		int ret = errorValue;

		try
		{
			if(decode != "")
			{
				decode = mEncrypter.decrypt(decode);
				String[] values = decode.split("#");
					
				if( (values.length == 3) && (values[1].compareTo(getIMEI_HDIDVF()) == 0) )
				{
					ret = Integer.parseInt(values[2]);
				}
			}
		} catch(Exception e) {	ERR(TAG,"Error occurred while getting Value for "+key+" with errorValue of "+errorValue+" Error ="+e.toString()); }
		
		return ret;
	}
	
	public String encryptFreeCount(int value)
	{
		return getEncrypt(value);
	}
	
	public int decryptFreeCount()
	{
		return getValue(PREFERENCES_MRC_OFFLINE_COUNT, -1);
	}
	
	public String encryptType(int value)
	{
		return getEncrypt(value);
	}
	
	public int decryptType()
	{
		return getValue(PREFERENCES_GAME_UNLOCKED, -1);
	}
    
    /**
     * This Method Allows you to Register when the Billing has complete successfully the game purchase
     * @return <code>true</code> if success or <code>false</code> if could not perform this operation
     */
    public boolean TrackingPurchaseSuccess(int mthisValidationMode)
    {
    	if(!Config.USE_TRACKING_BILLING)
    		return true;
    	
		isUsingTracking = true;
		
    	//rmv mDevice = new Device();
		mXPlayer = new XPlayer(mDevice);
		
		mXPlayer.sendTrackingPurchaseSucessRequest(mthisValidationMode);
		while (!mXPlayer.handleTrackingPurchaseSucessRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		DBG("Billing", "XPlayer.getLastErrorCode()="+XPlayer.getLastErrorCode());
		if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{		
			DBG("Billing", "Tracking for Purchase Success successfully recorded");
			isUsingTracking = false;
			return true;
		}
		else
		{
			DBG("Billing", "Failing at the recording process of Purchase Success");
			isUsingTracking = false;
			return false;
		}
    }
    
    /**
     * This Method Allows you to Register when the Billing has fail attempting the game purchase
     * @param BType Is the Billing Type used HTTP,SMS or Credit Card(CC)
     * @param ResultCode This is the Result Code from the Billing Response in a Failure Case
     * @return <code>true</code> if success or <code>false</code> if could not perform this operation
     */
    public boolean TrackingPurchaseFailed(int mthisValidationMode)
    {
    	if(!Config.USE_TRACKING_BILLING)
    		return true;
    	
    	isUsingTracking = true;
    	//rmv mDevice = new Device();
		mXPlayer = new XPlayer(mDevice);
		
		mXPlayer.sendTrackingPurchaseFailedRequest(mthisValidationMode);
		while (!mXPlayer.handleTrackingPurchaseFailedRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
		} 
		if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{		
			DBG("Billing", "Tracking for Failed Purchase successfully recorded");
			isUsingTracking = false;
			return true;
		}
		else
		{
			DBG("Billing", "Failing at the recording process of Failed Purchase");
			isUsingTracking = false;
			return false;
		}
    }
    
    /*
     * Tracking Options END
     * */

	/*public void setServerValidation(boolean validation)
	{
		
	}*/
	
	
#if USE_BILLING && USE_BOKU_FOR_BILLING
	public void saveNeedSendNotificationToServer(int value, String price, String money_id)
	{
		mDevice = new Device();

		if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
		SUtils.setPreference(PREFERENCES_FULL_LICENSE_BOKU, getEncrypt(value), PREFERENCES_NAME);
		if(value == BOKU_LOCKED)
		{
			SUtils.setPreference(PREFERENCES_BOKU_GAME_PRICE, getEncrypt(price), PREFERENCES_NAME);
			SUtils.setPreference(PREFERENCES_BOKU_MONEY_ID, getEncrypt(money_id), PREFERENCES_NAME);
		}
	}
	
	public void saveNeedSendNotificationToServer(int value, String price, int money_id)
	{
		mDevice = new Device();

		if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
		SUtils.setPreference(PREFERENCES_FULL_LICENSE_BOKU, getEncrypt(value), PREFERENCES_NAME);
		if(value == BOKU_LOCKED)
		{
			SUtils.setPreference(PREFERENCES_BOKU_GAME_PRICE, getEncrypt(price), PREFERENCES_NAME);
			SUtils.setPreference(PREFERENCES_BOKU_MONEY_ID, getEncrypt(money_id), PREFERENCES_NAME);
		}
	}
	
	public boolean needSendNotificationToServer()
	{
		mDevice = new Device();

		if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
		if(getValue(PREFERENCES_FULL_LICENSE_BOKU).equals("" + BOKU_LOCKED))
			return true;
		else
			return false;
	}
	
	public String getSavedGamePrice()
	{
		mDevice = new Device();

		if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
			
		return getValue(PREFERENCES_BOKU_GAME_PRICE);
	}
	
	public int getSavedMoneyID()
	{
		mDevice = new Device();

		if(mEncrypter==null)
    		mEncrypter 	= new StringEncrypter(Device.getDemoCode()+getIMEI_HDIDVF());
			
		return getValue(PREFERENCES_BOKU_MONEY_ID,-1);
	}
#endif
	
	public void setServerNumber(String value)
	{
		SUtils.setPreference(PREFERENCES_GAME_SERVER_NUMBER, value, PREFERENCES_NAME);
	}

	
	public String getServerNumber()
	{	
		return (SUtils.getPreferenceString(PREFERENCES_GAME_SERVER_NUMBER,PREFERENCES_NAME));
	}
	
	public void setRandomCodeNumber(int value)
	{
		SUtils.setPreference(PREFERENCES_GAME_RANDOM_CODE, value,PREFERENCES_NAME);
	}


	/**
	 * This method returns the value of our random validation code
	 * and reads it from the system preferences register
	 * @return A integer value which refers to the random code that we generate
	 */
	public int getRandomCodeNumber()
	{	
		return (SUtils.getPreferenceInt(PREFERENCES_GAME_RANDOM_CODE, -1, PREFERENCES_NAME));
	}
	
	public void setUnlockCodeNumber(int value)
	{
		SUtils.setPreference(PREFERENCES_GAME_UNLOCK_CODE, value, PREFERENCES_NAME);
	}


	public int getUnlockCodeNumber()
	{
		return (SUtils.getPreferenceInt(PREFERENCES_GAME_UNLOCK_CODE, -1, PREFERENCES_NAME));
	}
	
	
	/**
	 * This method evaluates the current value on SMS, HTTP or CC, and then
	 * makes a match between our code + key, and the answer
	 * @param unlock_code This is the code returned from SMS, HTTP, or CC
	 * @return This method returns a <code>true</code> value for a invalid code or a<code>false</code> value for a valid code
	 * @see #getRandomCodeNumber()
	 */
	public boolean isValidCode(int unlock_code)
	{
		return(unlock_code == (getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
	}

	/**
	 * This method evaluates the SMS string received and then
	 * makes a match between our code + key, and the answer
	 * @param unlock_string This is the string returned from the SMS
	 * @return This method returns a <code>true</code> value for a invalid code or a<code>false</code> value for a valid code
	 */
	public boolean readUnlockCode(String unlock_string )
	{
		String unlock_strCode;
		int unlock_code=-1;
		int start_code=-1;
		int end_code=-1;

		try
		{
		start_code = unlock_string.indexOf("(");
		end_code = unlock_string.indexOf(")");
		
		unlock_strCode = unlock_string.substring(start_code+1, end_code);
		unlock_code = Integer.parseInt(unlock_strCode);
		
		DBG(TAG,"Unlock Code Found on String = "+unlock_strCode+" from Message: "+unlock_string);
		}
		catch(Exception e)
		{
			DBG(TAG,"Error Reading Message"+e.toString());
			return(false);
		}
        
		if(isValidCode(unlock_code))
		{
			setUnlockCodeNumber(unlock_code);
			return (true);
		}
		else
		{
			return (false);
		}
	}

#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING

	public boolean readMTKUnlockCode(String PSMS_TID, String unlock_string)
	{
		int unlock_code=-1;

		int FM_SMS_ID=1;
		int UNLOCK_CODE=2;

		//Check if is this the SMS for the current Transaction ID
		if(!getContentValue(unlock_string,FM_SMS_ID).equals(PSMS_TID))
			return(false);
		
		try
		{
		unlock_code = Integer.parseInt(getContentValue(unlock_string,UNLOCK_CODE));
		
		DBG(TAG,"Unlock Code Found on String = "+unlock_code+" from Message: "+unlock_string);
		}
		catch(Exception e)
		{
			DBG(TAG,"Error Reading Message"+e.toString());
			return(false);
		}
        
		if(isValidCode(unlock_code))
		{
			setUnlockCodeNumber(unlock_code);
			return (true);
		}
		else
		{
			return (false);
		}
	}
	
	public boolean isInAppSMS(String sms_content)
	{
		int GLTAG=0;
		String GLTAGSTR= "GLINAPP";
		
		return (getContentValue(sms_content,GLTAG).equals(GLTAGSTR));
	}
	
	private String getContentValue(String src, int pos)
	{
		int start = 0;
		int ini_pos = pos;
//NOTE: in the case of Android 4.4 and above, the SMS-MT can be seen, the following 2 lines compensate the start to begin in the first parameter.
		start = src.indexOf(' '); //position in the first parameterif not found returns -1,
		start++; //start is in the 1st parameter, or in position 0.
		
	
		int end = src.indexOf('/', start + 1);

		while (pos > 0)
		{
			if (start == -1)
			{
				return null;
			}
			start = end;
			end = src.indexOf('/', start + 1);
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
	
	public void SetSMSResult(String value)
	{
		SUtils.setPreference(PREFERENCES_SMS_RESULT, value, PREFERENCES_NAME);
	}

	public String GetSMSResult()
	{
		return (SUtils.getPreferenceString(PREFERENCES_SMS_RESULT,"",PREFERENCES_NAME));
	}

#endif
	
	/**
	 * This method evaluates the SMS string received and then
	 * makes a match between our code + key, and the answer
	 * @param unlock_string This is the string returned from the SMS
	 * @return This method returns a <code>true</code> value for a invalid code or a<code>false</code> value for a valid code
	 */
	public boolean ContainsUnlockCode(String unlock_string )
	{
		String unlock_strCode;
		int unlock_code=-1;
		int start_code=-1;
		int end_code=-1;

		DBG(TAG,"SMS DEBUG Reading a message Unlock String Message = "+unlock_string);
		
		try
		{
		start_code = unlock_string.indexOf("(");
		end_code = unlock_string.indexOf(")");
		
		unlock_strCode = unlock_string.substring(start_code+1, end_code);
		unlock_code = Integer.parseInt(unlock_strCode);
		
		DBG(TAG,"SMS DEBUG Unlock Code Found on String = "+unlock_strCode+" from Message: "+unlock_string);
		}
		catch(Exception e)
		{
			DBG(TAG,"SMS DEBUG Error Reading Message"+e.toString());
			return(false);
		}
        
		if(unlock_code!=-1)
		{
			return (true);
		}
		else
		{
			return (false);
		}
	}
	
	/**
     * Takes care of saving all the info related with the user subscription/purchase in the
     * record store.
     * It acts depending of the purchase type that has been chosen.
     * @param needValidation If the license needed validation from the server (cause by a date violation or a subscription expiration)
     * @param purchaseType Indicates the type of purchase that has been selected by the user.
     * @param alreadySubscribed If the server sent the error response of the user already subscribed to the game asked.
     * @param date A valid date.
     * */
	 public void persistSubscriptionResults(boolean needValidation, int purchaseType, boolean alreadySubscribed, long date)
	 {
		 if (alreadySubscribed)
        {
            SUtils.setPreference(PREFERENCES_GAME_UNLOCKED, UNLOCKED, PREFERENCES_NAME);
			SUtils.setPreference(PREFERENCES_NEED_VALIDATION_ON_SERVER, false, PREFERENCES_NAME);
        }
        else
        {

                {
                    SUtils.setPreference(PREFERENCES_GAME_UNLOCKED, UNLOCKED, PREFERENCES_NAME);

					SUtils.setPreference(PREFERENCES_NEED_VALIDATION_ON_SERVER, false, PREFERENCES_NAME);

                }
        }
        debugSavedValues();
    }
	
	public void debugSavedValues()
    {
		//DBG("Billing","PREFERENCES_NAME:						" + PREFERENCES_NAME);
    	//DBG("Billing","PREFERENCES_GAME_UNLOCKED:				" + getPreferenceInt(PREFERENCES_GAME_UNLOCKED,-666));
    	//DBG("Billing","PREFERENCES_NEED_VALIDATION_ON_SERVER: " + getPreferenceBoolean(PREFERENCES_NEED_VALIDATION_ON_SERVER,false));
    }

	//*************************************************************************************************************
	//* CONSTANTS
	//*************************************************************************************************************

	/** Value to be saved in RMS in case of buying the game */
    private final long			UNLOCKED_FOREVER                    = -999;
    /** Values that determines that the game has been unlocked */
    private final int				UNLOCKED                            = 1;
    /** Values that determines that the game has been unlocked */
    private final int				LOCKED                            	= 0;
    /** The days that the subscription lasts. */
    private final int             SUBSCRIPTION_DAYS                   = 31;
    /** Determines the first day of purchase. */
    private final long			FIRST_DAY                           = 0L;
    /** To store how many milliseconds there are in one day. */
    private final long            MILLISECONDS_PER_DAY                = 1000 * 60 * 60 * 24; // 86400000
    /** The days that the subscription lasts. */
    private final long			SUBSCRIPTION_TIME                   = 2678400000L; 
    /** Value used to determine an invalid date */
    private final byte            DATE_INVALID                        = -1;
    /** Value used to determine an error in the date */
    private final byte            DATE_ERROR                        	= DATE_INVALID;
    /** Value used to determine an valid date */
    private final byte            DATE_OK								= 0;
}
#endif //#if USE_IN_APP_BILLING || USE_BILLING