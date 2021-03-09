package APP_PACKAGE.DRM.Verizon;

// VERSION 1.0.0

// import APP_PACKAGE.Billing.Config;
// import APP_PACKAGE.Billing.Config;
////import APP_PACKAGE.Billing.SUtils;
import APP_PACKAGE.R;
import java.util.Random;
import android.util.Log;

public final class XPlayer
{
	boolean mShopAvaliable;
	int mKey;
	
	public XPlayer ()
	{
		getConnectionValues();
		whttp = new HTTP(this);
		
		if (ForceContentType == null)
		{
			ForceContentType = "";
		} else {
			ForceContentType = ForceContentType.trim();
		}
	}
	
	
	public void setKey(int key)
	{
		mKey = key;
	}
	private final int SMS_UNLOCK_KEY	= 53412;
	private final int MAX_UNLOCK_CODE 	= 9999;
    private final int MIN_UNLOCK_CODE 	= 1111;
	public int createUniqueCode()
    {
    	double rnd = new Random().nextDouble();
        mKey = (int)(((MAX_UNLOCK_CODE - MIN_UNLOCK_CODE + 1) * rnd ) + MIN_UNLOCK_CODE );

        return mKey;
    }
	
	public boolean isValidCode(int unlock_code)
	{
		return(unlock_code == (mKey ^ SMS_UNLOCK_KEY));
	}

	/**
	 * To get from the resources all the values that are necessary to the connection establishment.
	 * */
	void getConnectionValues() {
		
		//HTTP.X_UP_SUBNO = x_up_subno;
		
		ForceContentType = null;
		
		if (url == null || url.length() == 0)
		{
			return;
		} else {
			//d("[VALUE] URL: " + url);
		}

		// if (HTTP.X_UP_SUBNO != null)
		// {
			// //d("[WARNING] This values should not be set manually.");
		// } 
		
		url = url.trim();
	}
	
	/**
	 * Cancels the ongoing request
	 */
	public void cancel()
	{
		if (ENABLE_TIMEOUT)
		{
			callstarttime = 0;
		}
		whttp.cancel();
	}

	/**
	 * Cleans up the memory allocated during the last server request.
	 * <p>
	 * Use this every time a request completes (it finishes being pending - check with <code>getLastError()</code>) to free up some memory.
	 */
	public void cleanup()
	{
		if (ENABLE_TIMEOUT)
		{
			callstarttime = 0;
		}
		whttp.cleanup();
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
	
	/**
	 * 
	 * <p>
	 * Use <code>handleValidateLicense()</code> to handle the request afterwards.
	 * <p>
	 */
	//private int mValidationMode = -1;
	
	public void sendIsShopAvaliableRequest()
	{
		//mValidationMode = validationMode;

		mShopAvaliable = true;
		whttp.cancel();
		String tmp = buildQuery();
		//tmp = encodeQuery(tmp);

		//url = "http://confirmation.gameloft.com/tryandbuy/verizon/checkdrm.php";
		url = "https://secure.gameloft.com/tryandbuy/verizon/checkdrm.php";
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
	    whttp.sendByGet(url, tmp);
	}
	
	/**
	 * This Function Validates the Previous Purchase of the game
	 * 
	 * @return <code>true</code>if the game was billed previously or <code>false</code> if not
	 */
	public boolean handleIsShopAvaliable()
	{
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError) return true;
		String tmpHR="";
		d("Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
		    if (whttp.m_response.indexOf("|") == -1) {
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		    d("XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE);
			d("Value for response on tmpHR ="+tmpHR);
			
			
			// tmpHR = getValue(whttp.m_response, 1);
			// d("Value for response on tmpHR ="+tmpHR);
			
			// tmpHR = getValue(whttp.m_response, 2);
			// d("Value for response on tmpHR ="+tmpHR);

			// try 
			// {
				// if (tmpHR.equals(FAILURE_RESULT)) 
				// {
				    // lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				    // d("Last error lastErrorCode: " + lastErrorCode);
				    // return true; 
				// }
			// } 
			// catch (NumberFormatException e) 
			// {
				// lastErrorCode = ERROR_BAD_RESPONSE;
				// tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				// if (tmpHR.contains("PB"))
				// {
					// try {
						// tmpHR = tmpHR.substring(2,tmpHR.length());
						// lastErrorCode = Integer.parseInt(tmpHR);
					// } catch (NumberFormatException ex)
					// {
						// //do nothing already has the ERROR_BAD_RESPONSE set
					// }
					
				// }
				// return true; //not handle by this request        	 
			// }

			//if (tmpHR.equals(SUCCESS_RESULT)) 
			{
			    if (isValidCode((Integer.parseInt(tmpHR))))
			    {
					
					mShopAvaliable = (Boolean.parseBoolean(getValue(whttp.m_response, TRANSACTION_RESULT)));
					
			    	d("Evaluating Random Code Sent :"+mKey);
			    	d("Evaluating Unlock Key :"+SMS_UNLOCK_KEY);
			    	d("Evaluating "+mKey+" ^ "+SMS_UNLOCK_KEY+" = "+(mKey ^ SMS_UNLOCK_KEY));
			    	d("Server Response = "+mShopAvaliable);
			    	//d("VALID CODE!!!");
					
					
			    	
			    	
		    		lastErrorCode = ERROR_NONE;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
					d("Server Response = "+mShopAvaliable);
			    }
				d("Server Response = "+mShopAvaliable);
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
	/**
	 * Builds the query to be requested to the server.
	 * @return The query.
	 * */
	private String buildQuery() {
	    String tmp = "b=isshopenabled|"+mKey;
		
		d("Query: "+tmp);
        //switch (validationMode) {
			// case PURCHASE_HTTP:
				// //d("Purchasing by HTTP");
				// tmp = "b=ppdwap|"+device.getDemoCode()+"|"+device.getServerInfo().getGamePrice()+"|"+SUtils.getLManager().getRandomCodeNumber();
			// break;
			// case PURCHASE_CC_LOGIN:
				// //d("Build Query Login");
				// tmp = 	"b=" + TAG_CC_LOGIN + "|" + mUserInfo.getEmail() +
						// "|" + mUserInfo.getPassword() + "|" + SUtils.getLManager().getRandomCodeNumber();
			// break;
			// case PURCHASE_CC_USERBILL:
				// //d("Build Query User Bill");
				 // tmp = 	"b=" + TAG_CC_USERBILL + "|" + device.getDemoCode()+ "|" + device.getServerInfo().getCurrencyValue() +
						// "|" + device.getServerInfo().getGamePrice() + "|" + mUserInfo.getEmail() + "|" + mUserInfo.getPassword() + 
						// "|" + SUtils.getLManager().getRandomCodeNumber();
			// break;
			// case PURCHASE_CC_NEWUSERBILL:
				// //d("Build Query New User Bill");
				// tmp = 	"b=" + TAG_CC_NEWUSERBILL + "|" + device.getDemoCode()+ "|" + device.getServerInfo().getCurrencyValue() +
						// "|" + device.getServerInfo().getGamePrice() + "|" + mUserInfo.getEmail() + "|" + mUserInfo.getPassword() + 
						// "|" + mUserInfo.getCardNumber() + "|" + mUserInfo.getExpirationDate() + 
						// "|" + mUserInfo.getSecurityCode() + "|" + SUtils.getLManager().getRandomCodeNumber();
			// break;
			// case PURCHASE_CC_SINGLEBILL:
				// //d("Build Query SingleBill");
				// tmp = 	"b=" + TAG_CC_SINGLEBILL + "|" + device.getDemoCode()+ "|" + device.getServerInfo().getCurrencyValue() +
						// "|" + device.getServerInfo().getGamePrice() + "|" + mUserInfo.getEmail() + "|" + mUserInfo.getCardNumber() + 
						// "|" + mUserInfo.getExpirationDate() + "|" + mUserInfo.getSecurityCode() + "|" + SUtils.getLManager().getRandomCodeNumber();
			// break;
			// case PURCHASE_CC_FORGOTPW:
				// //d("Build Query Forgot Password");
				 // tmp = 	"b=" + TAG_CC_FORGOTPW + "|" + mUserInfo.getEmail() + "|" + SUtils.getLManager().getRandomCodeNumber();
			// break;
        //}
        //d("String Query before encoding: " + tmp);
        return tmp;
	}

	/**
	 * Encodes a query.
	 * @param query The query to be encoded.
	 * @return String The query encoded.
	 * */
	private String encodeQuery(String query) {
	    String tmpBlob = Encoder.String2Blob(query);
	    return tmpBlob;
	}
	/**
	 * Takes the string that represents the game price in float format, and returns 
	 * the same price in integer format. e.g.: 6,99 -> 699
	 * @param price String with the price to format.
	 * @return String with the price formated.  
	 * */
	private String toIntegerFormat(String price) {
	    char [] aux = price.toCharArray();
	    price = "";
	    for (int i = 0 ; i < aux.length ; ++i) {
	        if (aux[i] != '.' && aux[i] != ',') 
	            price = price + aux[i]; 
	    }
	    return price;
	}
	
	/**
	 * Looks around the <code>errorMeesages</code> array trying to match the last error code. If gets it, 
	 * returns the message associated with that error code, if not it returns "Error Unknown".
	 * @return String with the last error message.
	 * */
	public static String getLastErrorMessage() {
	    /*for (int i = 0; i < errorMessages.length; i++) {
            if (lastErrorCode == errorMessages[i].errorCode) {
                return theActivity.getString(errorMessages[i].strId);
            }
        }/**/
	    return "";
	}
	/**
	 * This method should be just used when creating a simulated connection (For testing the application without depending of the connection
	 * to the server).
	 * */
	public static void setLastErrorMessage(int error) 
	{
		lastErrorCode = error; 
	}
	
	/**
	 * Looks around the <code>errorMessages</code> array trying to match the last error code. If gets it, 
	 * returns the message Id associated with that error code, if not it returns "Error Unknown".
	 * @return int with the last error message Id.
	 * */
	public static int getLastErrorMessageId() {
		// for (int i = 0; i < errorMessages.length; i++) {
			// if (lastErrorCode == errorMessages[i].errorCode) {
				// return errorMessages[i].strId;
			// }
		// }
		// return R.string.AB_TRANSACTION_FAILED;
		
		return 0;
	}

	/**
	 * Returns the last error code.
	 * @return int With the last error code.
	 * */
	public static int getLastErrorCode () {
		return lastErrorCode;
	}

	//*************************************************************************************************************
	//* CONSTANTS
	//*************************************************************************************************************
	
	/**
	 * Enables or disables the outside ("software") timeout mechanism.&nbsp;
	 * Use this only on phones with timeout problems (some Samsung phones).
	 * <p>
	 * On most phones the connection timeouts if an answer is not received
	 * within a certain time of request start. However, on some phones, the
	 * connection thread hangs indefinitely, so we must implement an outside
	 * timeout mechanism.
	 * <p>
	 * If set to <code>true</code>, the timeout mechanism is enabled and the
	 * server requests timeout after <code>CONN_TIMEOUT</code> milliseconds.
	 *
	 * @see #CONN_TIMEOUT
	 */
	public static final boolean ENABLE_TIMEOUT = true;

	/**
	 * The time in milliseconds after which a HTTP server request is timed
	 * out if a response is not received.
	 * <p>
	 * This is used only if <code>ENABLE_TIMEOUT</code> is set to
	 * <code>true</code>.
	 *
	 * @see #ENABLE_TIMEOUT
	 */
	public static final int CONN_TIMEOUT = 5000;

	/**
	 * Sets the cancel operation to either interrupt or not the connection
	 * in progress.
	 * <p>
	 * On some phones, if the current connection is stopped on 
	 * <code>cancel</code>, the game crashes.
	 * <p>
	 * To avoid this, the cancel operation must not interrupt the
	 * communication and the thread should be left to finish naturally.
	 * Also, if another connection must be performed, the application should
	 * wait for the previous one to finish.
	 * <p>
	 * All this can be done by setting this constant to <code>true</code>.
	 * <p>
	 * It is recommended to set this variable <code>true</code> on the
	 * following phones: Sanyo MIDP1, LG MIDP 1
	 * <p>
	 * It is highly recommended to be set it to <code>false</code> if not
	 * needed otherwise.
	 */
	public static final boolean HTTP_NO_CANCEL = false;

	/**
	 * Sets whether to use HTTP <b>POST</b> requests instead of <b>GET</b>
	 * requests or not.
	 * <p>
	 * Some phones have problems if the HTTP GET request is too big.
	 * Normally they should support more than 500 characters per request,
	 * but some Nokia phones do not work with more than 120.
	 * <p>
	 * On those phones, it is necessary to use HTTP POST requests instead of
	 * GET ones.
	 * <p>
	 * Set this to <code>true</code> only on those phones (Nokia 6225 and
	 * related phones, some Samsung - ex.D500).
	 */
	public static final boolean USE_HTTP_POST = false;
	
	/** Offset to get the transaction result from the server response. */
	private static final byte TRANSACTION_RESULT = 0;
	
	/** Offset to get the transaction UNLOCK Code from Server*/
	private static final byte TRANSACTION_UNLOCK_CODE = 1;
	
	/** Offset to get the transaction date from the server response. */
    private static final byte TRANSACTION_DATE = 1;
    
	/**
     * Initialization value.
     * <p>
     * Getting this error means that actually no requests have been yet made
     * since the MIDlet started so there is no last error.
     */
    public static final int ERROR_INIT = -100;

    /**
     * No connection to server.
     * <p>
     * Either there is no network available or the server cannot be
     * contacted for some reason.
     */
    public static final int ERROR_CONNECTION = -2;

    /** Result is pending.&nbsp;Transaction with server is ongoing. */
    public static final int ERROR_PENDING = -1;

    /** The request finished successfully. */
    public static final int ERROR_NONE = 0;

    /** The uuid is missing from the request. */
    public static final int ERROR_NO_UUID = 1;

    /** There is no phone number in the register request */
    public static final int ERROR_NO_PHONE_NUMBER = 25;

    /** Programming error on emulator: no phone id. */
    public static final int ERROR_NO_CLIENT_ID = 26;

    /**
     * The server response has incorrect format.
     * <p>
     * Make sure the request is correct. If it is a leader board request,
     * make sure the number of supplemental data needed match the requested
     * leader board
     */
    public static final int ERROR_BAD_RESPONSE = 40;
	
    public static final int NO_USER_ID_IN_HEADER        = 10001;
    // public static final int NO_PRODUCT_ID_AND_PRICE     = 10002;
    // public static final int PRICE_NOT_DEFINED_AT_ATT    = 10003;
    // public static final int COULD_NOT_CONNECT_TO_ATT    = 10004;
    // public static final int NO_RESPONSE_FROM_ATT        = 10005;
    // public static final int NOT_SUBSCRIBED				= 10009;
    // public static final int NO_USER_FOUND               = 40010;
    // public static final int PARENTAL_CONTROL            = 40030;
    // public static final int OVER_SPENDING               = 40040;
    // public static final int BUCKET_EMPTY                = 40041;
    // public static final int ALREADY_SUBSCRIBED          = 42020;
    // public static final int NOT_SUPPORTED               = 63490;
    public static final int UNKNOWN_ERROR               = 99999;
    
    
  //Credit Card response codes							
    public static final int CC_LOGIN_SUCCESS_USER_EXIST_WITH_CREDIT_CARD 			= 000001;
    public static final int CC_LOGIN_SUCCESS_USER_EXIST_BUT_NO_CREDIT_CARD 			= 000002;
    public static final int CC_LOGIN_ERROR_UNKNOWN_FAILURE				 			= 000000;
    
  // Common Api Errors
    public static final int CC_ERROR_GENERIC										= 000000;//Generic Error
 
    
    
/*HTTP ERRORS
# 00001: Failure, the security code provided in the GET is not compliant.
# 00002: Failure, the user ID is not found in the headers.
# 00003: Failure, the b parameter missing from the GET.
# 00004: Failure, the request is invalid.
# 00005: Failure, the payment process has failed.
# 00006: Failure, Invalid Pricepoint.
---------------------------------------------------------------------------
# 10001: Success, the current date has been correctly displayed.
# 10002: Success, the game had already been purchased.
# 10003: Success, the game has been purchased correctly. 
*/
    
 // Security Errors
    public static final int CC_ERROR_SECURITY_CODE_INVALID							= 999999;//Security Code Invalid
    
    
    
    
    //*************************************************************************************************************
	//* VARIABLES
	//*************************************************************************************************************
    
    /** Used to know if the operation (under connection) was done successfully or not. */
    public static long dateFromServer = 0;
    
    /** To force the connection content type to certain type. */
	public static String ForceContentType = null;

	/** The Gameloft Server URL - it is read from the game's .jad file. */
	private String url;
	
	/** To know if the real or the test values are going to be saved in the preferences file */
	//public static boolean useTestValues = true;

	/** The HTTP object used for low level server communication. */ 
	protected static HTTP whttp;

	/** The time the current request started - used for timeout implementation.
	 *  Use only in conjunction with ENABLE_TIMEOUT.*/
	public static long callstarttime;
	
	// /** To manage all the error messages. */
    // public static Error [] errorMessages = {
        // //TO UPDATE
    	// /*new Error (NO_USER_ID_IN_HEADER, R.string.BILLING_ERRORTEXT_NO_USER_ID_IN_HEADER),
        // new Error (NO_PRODUCT_ID_AND_PRICE, R.string.BILLING_ERRORTEXT_NO_PRODUCT_ID),
        // new Error (PRICE_NOT_DEFINED_AT_ATT, R.string.BILLING_ERRORTEXT_PRICE_NOT_DEFINED),
        // new Error (COULD_NOT_CONNECT_TO_ATT, R.string.BILLING_ERRORTEXT_COULD_NOT_CONNECT),
        // new Error (NO_RESPONSE_FROM_ATT, R.string.BILLING_ERRORTEXT_NO_RESPONSE),
        // new Error (NOT_SUBSCRIBED, R.string.BILLING_ERRORTEXT_NOT_SUBSCRIBED),
        // new Error (NO_USER_FOUND, R.string.BILLING_ERRORTEXT_USER_NOT_FOUND),
        // new Error (PARENTAL_CONTROL, R.string.BILLING_ERRORTEXT_PARENTAL_CONTROL),
        // new Error (OVER_SPENDING, R.string.BILLING_ERRORTEXT_OVER_SPENDING),
        // new Error (BUCKET_EMPTY, R.string.BILLING_ERRORTEXT_BUCKET_EMPTY),
        // new Error (ALREADY_SUBSCRIBED, R.string.BILLING_ERRORTEXT_ALREADY_SUBSCRIBED),
        // new Error (UNKNOWN_ERROR, R.string.BILLING_ERRORTEXT_UNKNOWN),
        // new Error (NOT_SUPPORTED, R.string.BILLING_ERRORTEXT_NOT_SUPPORTED),
        // new Error (ERROR_NONE, R.string.BILLING_ERRORTEXT_NONE),
        // new Error (ERROR_CONNECTION, R.string.BILLING_ERRORTEXT_COULD_NOT_CONNECT),
        // new Error (ERROR_PENDING, R.string.BILLING_ERRORTEXT_PENDING),
        // new Error (ERROR_NO_CLIENT_ID, R.string.BILLING_ERRORTEXT_NO_USER_ID_IN_HEADER),
        // new Error (ERROR_BAD_RESPONSE, R.string.BILLING_ERRORTEXT_NO_RESPONSE),
        // new Error (ERROR_INIT, R.string.BILLING_ERRORTEXT_INITIATING),*/
        // };
	
	/** Last error code : if 0 , success , if no, the error code. */ 
	private static int lastErrorCode;
	
	//**************************************************************************
	//  Inner Classes.
	//**************************************************************************
	
	public static class Error {
	    private int errorCode;
	    private int strId;
	    
	    public Error (int code, int strId) {
	        this.setErrorCode(code);
	        this.setErrorMessageId(strId);
	    }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorMessageId(int strId) {
            this.strId = strId;
        }
        public int getErrorMessageId() {
            return strId;
        }
	    
	}
	
	
	public static boolean DEBUG = VerizonDRM.DEBUG && true;	
	private static void d(String msg)
	{
		if(DEBUG)
		{
			DBG("VerizonDRM",msg);
		}
	}
	private static void d(Exception e)
	{
		if(DEBUG)
		{
			DBG_EXCEPTION(e);
		}
	}
	
}