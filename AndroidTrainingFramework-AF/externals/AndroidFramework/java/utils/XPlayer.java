#if USE_INSTALLER || USE_BILLING || USE_TRACKING_FEATURE_INSTALLER || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_PSS
package APP_PACKAGE.GLUtils;

// VERSION 1.0.0

import android.net.ConnectivityManager;
import android.util.Log;

import APP_PACKAGE.GLUtils.Encoder;
#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_BILLING
	import APP_PACKAGE.billing.common.Constants;
	import APP_PACKAGE.billing.common.UserInfo;
	import APP_PACKAGE.billing.common.AServerInfo;
	#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM && USE_IN_APP_GLOT_LOGGING)
		import APP_PACKAGE.iab.IABLogging;
	#endif
	#if GAMELOFT_SHOP || USE_BILLING
	import APP_PACKAGE.billing.common.AModel;
	#endif
	#if GAMELOFT_SHOP
	import APP_PACKAGE.iab.GLOFTHelper;
	#endif
#endif
	import java.security.MessageDigest;
	import java.security.NoSuchAlgorithmException;

import APP_PACKAGE.GLUtils.Carrier;
import APP_PACKAGE.GLUtils.Config;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.HTTP;
import APP_PACKAGE.GLUtils.SUtils;
import java.util.Locale;
import APP_PACKAGE.R;

#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
import APP_PACKAGE.installer.IReferrerReceiver;
#endif


public final class XPlayer implements Config
#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
, Constants
#endif
{
	static Device device;
#if USE_TRACKING_FEATURE_INSTALLER || USE_TRACKING_FEATURE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) || USE_BILLING
	//public final String URL_TRACKING = "http://confirmation.gameloft.com/php5/tryandbuy/notifications/";
	public final String URL_TRACKING = "https://secure.gameloft.com/tryandbuy/notifications/";
	
	public final String NEW_URL_TRACKING = "http://ingameads.gameloft.com/redir/hdloading.php";
#endif

#if USE_PSS
	// String PSSURL = "http://wapshop.gameloft.com/php5/us/hdplus_test/PSS/verizon_test/";
	//String PSSURL = "http://wapshop.gameloft.com/php5/us/hdplus_test/PSS/verizon/";
	String PSSURL = "http://wapshop.gameloft.com/us/hdplus/pss/verizon/index.php";
#endif

#if USE_INSTALLER || USE_PSS 
	public String str_response;
#endif

#if USE_INSTALLER
	public boolean update_is_mandatory = false;
#endif


#if USE_INSTALLER		
	//public final String URL_WIFI_MODE = "http://confirmation.gameloft.com/android/3g_carrier.php";
	public final String URL_WIFI_MODE = "https://secure.gameloft.com/android/3g_carrier.php";
#endif

#if USE_BILLING	|| (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	//User Info 
	UserInfo mUserInfo = null;
	/**
	 * @return the mUserInfo
	 */
	public UserInfo getUserInfo() {
		return mUserInfo;
	}

	/**
	 * @param mUserInfo the mUserInfo to set
	 */
	public void setUserInfo(UserInfo mUserInfo) {
		this.mUserInfo = mUserInfo;
	}
#if GAMELOFT_SHOP || USE_BILLING	
	public XPlayer (Device config, AModel mdlBilling)
	{
		this(config);
		whttp.setModel(mdlBilling);
	}
#endif
	
#endif //#if USE_BILLING
	public XPlayer (Device config)
	{
		device = config; 
		
		getConnectionValues();
		whttp = new HTTP();
		
		if (ForceContentType == null)
		{
			ForceContentType = "";
		} else {
			ForceContentType = ForceContentType.trim();
		}
	}

	public static final String md5(final String s)
	{
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
	 
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
	 
		} catch (NoSuchAlgorithmException e) {
			DBG_EXCEPTION(e);
		}
		return "";
	}

	public static final String hmacSha1(String key, String data)
	{
		 try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes();
			javax.crypto.spec.SecretKeySpec signingKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < rawHmac.length; i++) {
				String h = Integer.toHexString(0xFF & rawHmac[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
        } catch (Exception e) {
            DBG_EXCEPTION(e);
        }
		return "";
	}

	public static Device getDevice()
	{
		return device;
	}
	
	
#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	public String GetResultValue(int value)
	{
		switch(value)
		{
			case 0: return (BILLING_SUCCESS);
			
			case 1: return (BILLING_SUCCESS_OFFLINE);
			
			case 2: return (BILLING_PENDING);
				
			case 3: return (BILLING_FAILURE);
		}
		return("INVALID_VALUE");
	}

#endif

	/**
	 * To get from the resources all the values that are necessary to the connection establishment.
	 * */
	void getConnectionValues() {
		
		HTTP.X_UP_SUBNO = x_up_subno;
		
		ForceContentType = null; //TODO: See how to get this value.
		
		if (url == null || url.length() == 0)
		{
			return;
		}
		if (HTTP.X_UP_SUBNO != null)
		{
			DBG("XPlayer","[WARNING] This values should not be set manually.");
		} 
		
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
	 * Cingular Specific - Sends a request to retrieve the users license
	 * expiration time.&nbsp;This function is asynchronous and valid only for
	 * Cingular games.
	 * <p>
	 * Use <code>handleValidateLicense()</code> to handle the request afterwards.
	 * <p>
	 * This license validation is only for Cingular games. This is not supported
	 * for other operators so it shouldn't be used on them.
	 *
	 */
	private int mValidationMode = -1;
	
#if USE_BILLING	|| GAMELOFT_SHOP
	public void sendValidationViaServer(int validationMode)
	{
		mValidationMode = validationMode;

		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		tmp = buildQuery(validationMode);
		
		#if USE_BILLING
		//if(XPlayer.getDevice().VERSION > 1)
			tmp += "&version="+XPlayer.getDevice().VERSION;
		#endif
		
		/*if(validationMode != PURCHASE_HTTP
			&& validationMode < PURCHASE_CC_LOGIN)
			tmp = encodeQuery(tmp);*/

		url = device.getServerInfo().getURLbilling();
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "ValidationViaServer");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "ValidationViaServer");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "ValidationViaServer");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
	}
#endif //#if USE_BILLING

#if USE_BILLING && USE_BOKU_FOR_BILLING
	//https://webdoc-new.gameloft.org/index.php/TnB_Boku_Unlock_API
	public void sendBOKUPurchaseRequest()
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		//url = device.getServerInfo().getURLbilling();
		url = "https://secure.gameloft.com/tryandbuy/boku";
		AServerInfo serverInfo = device.getServerInfo();
		String gamePrice = "";
		int money_id;
		if(serverInfo!=null)
		{
			gamePrice += serverInfo.getGamePrice();
			money_id = serverInfo.getIntegerCurrencyValue();
		}
		else
		{
			gamePrice += SUtils.getLManager().getSavedGamePrice();
			money_id = 	SUtils.getLManager().getSavedMoneyID();
		}
		tmp = "b=gamepurchase|" + device.getDemoCode() + "|" + gamePrice + "|" + money_id + "|" + SUtils.getLManager().getRandomCodeNumber();
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		DBG("XPlayer", "sendBOKUPurchaseRequest tmp: " + tmp);
	    whttp.sendByGet(url, tmp);
	}
	
	public boolean handleBOKUPurchaseRequest()
	{
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		DBG("XPlayer", "whttp.m_bError...." + whttp.m_bError);
		if (whttp.m_bError) return true;
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    int index = whttp.m_response.indexOf("|");
			if(index>=0)
			{
				String tmp = whttp.m_response.substring(0,index);
				if("SUCCESS".equals(tmp))
				{
					DBG("XPlayer", "whttp.m_response...." + whttp.m_response);
					APP_PACKAGE.billing.BokuActivity.sendToServerSuccessfully = true;
				}
			}
		}
		return true;
	}
#endif
#if GAMELOFT_SHOP
	public void sendCCPurchaseRequest(String itemID)
	{
		//DBG("XPlayer","handleCCPurchaseRequest() BEGIN");
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		url = device.getServerInfo().getURLbilling();
		//tmp = "b=contentpurchase|"+itemID+"|"+device.getServerInfo().getGamePrice()+"|"+SUtils.getLManager().getRandomCodeNumber();
	
		tmp = "b=" + TAG_CC_USERBILL + "|" + GLOFTHelper.GetItemId()+ "|" + device.getServerInfo().getStringCurrencyValue() +
     				"|" + device.getServerInfo().getGamePrice() + "|" + SUtils.getLManager().GetUserName() + "|" + SUtils.getLManager().GetUserPassword() + 
     				"|" + SUtils.getLManager().getRandomCodeNumber();
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "CCPurchaseRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "CCPurchaseRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "CCPurchaseRequest");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
		//DBG("XPlayer","handleCCPurchaseRequest() END");
	}
	
	public boolean handleCCPurchaseRequest()
	{
	//DBG("XPlayer","handleCCPurchaseRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","handleCCPurchaseRequest Handled Ok. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE)))))
			    {
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE))));
			    	DBG("XPlayer","VALID CODE!!!");
			    	//AModel.successResult = true;
			    	
		    		lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = ERROR_NONE;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}

	public void sendUMPR3TIDRequest(String itemID)
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		url = device.getServerInfo().getUMPTIdURL();
		
		mMD5Sign=md5(device.getDemoCode()+"_"+device.getServerInfo().getGamePrice()+"_"+device.getServerInfo().getMoneyBilling()+"_"+itemID+"_"+mGLLiveGGI+"_"+mGLLiveUid+"_"+device.getIMEI()+"_R3_"+(System.currentTimeMillis() / 1000)+"_"+SUtils.GetSerialKey()+"_UMPGAMELOFT");

		tmp = "game="+device.getDemoCode()+"&content="+itemID+"&price="+device.getServerInfo().getGamePrice()+"&money="+device.getServerInfo().getMoneyBilling()+"&ggi="+mGLLiveGGI+"&gliveid="+mGLLiveUid+"&imei="+device.getIMEI()+"&umpversion=R3&timestamp="+(System.currentTimeMillis() / 1000)+"&d="+SUtils.GetSerialKey()+"&sign="+mMD5Sign;

		DBG("XPlayer","sendUMPR3TIDRequest is sending: "+tmp);

		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "UMPR3 TIDRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "UMPR3 TIDRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "UMPR3 TIDRequest");
	#else
	    whttp.sendByGet(url, tmp);	
	#endif
	}

	public boolean handleUMPR3TIDRequest()
	{
		DBG("XPlayer","handleUMPR3TIDRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","handleUMPR3TIDRequest Handled Ok. SUCCESS!");
			    
		    		lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
		    		UMP_MO1 = getValue(whttp.m_response, UMP_MO_1);
		    		UMP_MO2 = getValue(whttp.m_response, UMP_MO_2);
					lastErrorCode = ERROR_NONE;
					DBG("XPlayer","VALID transaction ID retrieved!");

				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;	
	}
	
	
	public void sendUMPR3GetBillingResultRequest(String itemID, String transactionId)
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		url = device.getServerInfo().getUMPBillingURL();
		
		mMD5Sign=md5(transactionId+"_"+device.getIMEI()+"_"+(System.currentTimeMillis() / 1000)+"_UMPGAMELOFT");

		tmp = "transactionid="+transactionId+"&timestamp="+(System.currentTimeMillis() / 1000)+"&imei="+device.getIMEI()+"&sign="+mMD5Sign;

		DBG("XPlayer","sendUMPR3GetBillingResultRequest is sending: "+tmp);

		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "UMPR3 GetBillingResultRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "UMPR3 GetBillingResultRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "UMPR3 GetBillingResultRequest");
	#else
	    whttp.sendByGet(url, tmp);	
	#endif
	}

	public boolean handleUMPR3GetBillingResultRequest()
	{
		DBG("XPlayer","handleUMPR3GetBillingResultRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    //lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCodeString = BILLING_PENDING;
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","handleUMPR3GetBillingResultRequest Handled Ok. SUCCESS!");
			    
					lastErrorCodeString = SUCCESS_RESULT;
					lastErrorCode = ERROR_NONE;
					DBG("XPlayer","Transaction Completed!");

				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;	
	}	
	
	
	public void sendPSMSTokenRequest(String itemID)
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		url = device.getServerInfo().getURLbilling();
		tmp = "b=registermo|"+device.getDemoCode()+"|"+SUtils.getLManager().getRandomCodeNumber()+"|"+device.getIMEI()+"|"+itemID+"|"+device.getServerInfo().getGamePrice()+"|"+device.getServerInfo().getMoneyBilling()+"|"+device.getServerInfo().getProfileId()+"|"+device.getServerInfo().getPlatformId()+"|"+device.getServerInfo().getLangId()+"|"+SUtils.GetSerialKey();
		DBG("XPlayer","tmp="+tmp);
		/*
		Example: GET /freemium/sms/?b=registermo|MMHP|12345|358313048823681|1023|0.99|1|31|1064|1 HTTP/1.0
		Headers
		Freemium API requires the account number and GGI to be provided through the following HTTP headers:
		Account Number: x-up-gl-acnum
		GGI: x-up-gl-ggi 
		*/

		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
	    LOGGING_APPEND_REQUEST_PARAM(url, "GET", "PSMS TokenRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "PSMS TokenRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "PSMS TokenRequest");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
	}
	
	
	public boolean handlePSMSTokenRequest()
	{
		DBG("XPlayer","handlePSMSTokenRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","handlePSMSTokenRequest Handled Ok. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE)))))
			    {
		    		lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = ERROR_NONE;
					DBG("XPlayer","VALID code for token");
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
					DBG("XPlayer","INVALID code for token");
			    }
				DBG("XPlayer","lastErrorCode="+lastErrorCode);
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;	
	}	

	public void sendPSMSPurchaseRequest(String itemID,String FM_SMS_ID)
	{
		String tmp;
		whttp.cancel();

		url = device.getServerInfo().getURLbilling();
	#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
		tmp = "b=checkbilling|"+FM_SMS_ID+"|"+device.getDemoCode()+"|"+SUtils.getLManager().getRandomCodeNumber()+"|"+device.getIMEI()+"|"+itemID+"|"+device.getServerInfo().getBillingType();
	#else		
		tmp = "b=checkbilling|"+FM_SMS_ID+"|"+device.getDemoCode()+"|"+SUtils.getLManager().getRandomCodeNumber()+"|"+device.getIMEI()+"|"+itemID;
	#endif

		//Example: GET /freemium/sms/?b=checkBill|MMHP|12345|358313048823681|1023 HTTP/1.0

		/*Format sent : checkBilling|<IGP_CODE>|<GENERATED_CODE>|<IMEI>|<CONTENT_ID>
		
		Response Format
		Format sent on success: SUCCESS|[success code]|[security code]|
		Format sent on failure: FAILURE|[failure code]|[security code]|*/
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "PSMS PurchaseRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "PSMS PurchaseRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "PSMS PurchaseRequest");
	#else
	    whttp.sendByGet(url, tmp);	
	#endif
	}
	
	public boolean handlePSMSPurchaseRequest()
	{
		DBG("XPlayer","handlePSMSPurchaseRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			tmpHR = Encrypter.crypt(tmpHR);
			try 
			{
				//if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				if (tmpHR != null && tmpHR.equals(BILLING_FAILURE)) 
				{   // There has been an error.
				    lastErrorCodeString = "FAILURE";
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","FAILURE last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				return true;
			}

			if (tmpHR != null && (tmpHR.equals(BILLING_SUCCESS) || tmpHR.equals(BILLING_SUCCESS_OFFLINE)))
			{
			   DBG("XPlayer","handlePSMSPurchaseRequest Handled Ok. SUCCESS!");
			   
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR)))))
			    {
		    		//lastErrorCodeString = "BILLING_SUCCESS";
					
					eComTxId = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR + 1);
					DBG("XPlayer","Transaction ID: "+eComTxId);
					
		    		lastErrorCodeString = BILLING_SUCCESS;
					lastErrorCode = ERROR_NONE;
					DBG("XPlayer","VALID CODE FOR BILLING_SUCCESS!");
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
					ERR("XPlayer","INVALID CODE FOR BILLING_SUCCESS!");
			    }
				DBG("XPlayer","lastErrorCode="+lastErrorCode);
				return true;
			}
			else if (tmpHR != null && tmpHR.equals(BILLING_PENDING))
			//else if (tmpHR != null && (tmpHR).equals("BILLING_PENDING")) 
			{
			   DBG("XPlayer","handlePSMSPurchaseRequest Handled Ok. PENDING!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR)))))
			    {
		    		//lastErrorCodeString = "BILLING_PENDING";
		    		lastErrorCodeString = BILLING_PENDING;
					lastErrorCode = ERROR_NONE;
					DBG("XPlayer","VALID CODE FOR BILLING_PENDING!");
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
					ERR("XPlayer","INVALID CODE FOR BILLING_PENDING!");
			    }
				DBG("XPlayer","lastErrorCode="+lastErrorCode);
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;	
	}


#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
	public void sendPSMSConfirmationRequest(String itemID,String FM_SMS_ID,String result)
	{
		String tmp;
		whttp.cancel();

		url = device.getServerInfo().getURLbilling();

		tmp = "b=confirmbilling|"+FM_SMS_ID+"|"+device.getDemoCode()+"|"+SUtils.getLManager().getRandomCodeNumber()+"|"+device.getIMEI()+"|"+itemID+"|"+result;

		//Example: GET /freemium/sms/?b=checkBill|MMHP|12345|358313048823681|1023 HTTP/1.0

		/*Format sent : confirmbilling|<fm_sms_id>|<igp_code>|<generated_code>|<imei>|<content_id>|<success or failure>
		
		Response Format
		Format sent on success: SUCCESS|[success code]|[security code]|
		Format sent on failure: FAILURE|[failure code]|[security code]|*/
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}

		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "PSMS ConfirmationRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "PSMS ConfirmationRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "PSMS ConfirmationRequest");
	#else
	    whttp.sendByGet(url, tmp);	
	#endif
	}
	
	public boolean handlePSMSConfirmationRequest()
	{
		DBG("XPlayer","handlePSMSConfirmationRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			tmpHR = Encrypter.crypt(tmpHR);
			try 
			{
				//if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				if (tmpHR != null && tmpHR.equals(BILLING_FAILURE)) 
				{   // There has been an error.
				    lastErrorCodeString = "FAILURE";
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","FAILURE last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				return true;
			}

			if (tmpHR != null && (tmpHR.equals(SUCCESS_RESULT)))
			{
			   DBG("XPlayer","handlePSMSConfirmationRequest Handled Ok. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR)))))
			    {
		    		lastErrorCodeString = BILLING_SUCCESS;
					lastErrorCode = ERROR_NONE;
					DBG("XPlayer","VALID CODE FOR BILLING_SUCCESS!");
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
					ERR("XPlayer","INVALID CODE FOR BILLING_SUCCESS!");
			    }
				DBG("XPlayer","lastErrorCode="+lastErrorCode);
				return true;
			}
			else
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;	
	}
#endif//USE_MTK_SHOP_BUILD
	
	public void sendHTTPPurchaseRequest(String itemID)
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		url = device.getServerInfo().getURLbilling();
		tmp = "b=contentpurchase|"+itemID+"|"+device.getServerInfo().getGamePrice()+"|"+SUtils.getLManager().getRandomCodeNumber();
		/*Format received : contentpurchase|[content id]|[price]|[security code]
		
		Response Format
		Format sent on success: SUCCESS|[success code]|[security code]|
		Format sent on failure: FAILURE|[failure code]|[security code]|*/
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "HTTP PurchaseRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "HTTP PurchaseRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "HTTP PurchaseRequest");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
	}
	
	public boolean handleHTTPPurchaseRequest()
	{
		DBG("XPlayer","handleHTTPPurchaseRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","handleHTTPPurchaseRequest Handled Ok. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE)))))
			    {
					eComTxId = getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE + 1);
					DBG("XPlayer","Transaction ID: "+eComTxId);
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE))));
			    	DBG("XPlayer","VALID CODE!!!");
			    	//AModel.successResult = true;
			    	
		    		lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = ERROR_NONE;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}

	#if SHENZHOUFU_STORE
	public void sendShenzhoufuPurchaseRequest(String itemID, String CARDTYPE, String CARDDENOM, String CARDNUMBER, String CARDPASSWORD)
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		
		url = device.getServerInfo().getShenzhoufuURLbilling();
		mMD5Sign=md5(device.getDemoCode()+"_"+device.getServerInfo().getGamePrice()+"_"+device.getServerInfo().getMoneyBilling()+"_"+itemID+"_"+mGLLiveGGI+"_"+mGLLiveUid+"_"+device.getIMEI()+"_"+CARDNUMBER+"_"+CARDPASSWORD+"_"+CARDDENOM+"_"+(System.currentTimeMillis() / 1000)+"_"+SUtils.GetSerialKey()+"_SHENZHOUFUGAMELOFT");
		tmp = 	"game="+device.getDemoCode()+"&content="+itemID+"&price="+device.getServerInfo().getGamePrice()+
				"&money="+device.getServerInfo().getMoneyBilling()+"&ggi="+mGLLiveGGI+"&gliveid="+mGLLiveUid+
				"&imei="+device.getIMEI()+"&timestamp="+(System.currentTimeMillis() / 1000)+"&d="+SUtils.GetSerialKey()+//"&channel="+""+
				"&cardnumber="+CARDNUMBER+"&cardpwd="+CARDPASSWORD+"&cardmoney="+CARDDENOM+"&cardtype="+CARDTYPE+"&sign="+mMD5Sign;
		/*
		Response Format
		Format sent on success: SUCCESS|[success code]
		Format sent on failure: FAILURE|[failure code]*/
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "Shenzhoufu PurchaseRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "Shenzhoufu PurchaseRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "Shenzhoufu PurchaseRequest");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
	}
	public boolean handleShenzhoufuPurchaseRequest()
	{
		DBG("XPlayer","handleShenzhoufuPurchaseRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
			DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","handleShenzhoufuPurchaseRequest Handled Ok. SUCCESS! "+getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
			    try{

			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	// DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR))));
			    	DBG("XPlayer","Server Response = "+getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
			    	DBG("XPlayer","VALID CODE!!!");
			    	
		    		lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);//make sure the response is an Integer
					lastErrorCode = ERROR_NONE;

				}catch(Exception e){lastErrorCode = ERROR_BAD_RESPONSE;}
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
	public void sendShenzhoufuBillingResultRequest(String itemID,String transactionID)
	{
		String tmp;
		whttp.cancel();

		url = device.getServerInfo().getShenzhoufuURLresult();

		mMD5Sign=md5(transactionID+"_"+device.getIMEI()+"_"+(System.currentTimeMillis() / 1000)+"_SHENZHOUFUGAMELOFT");
		tmp = "transactionid="+transactionID+"&timestamp="+(System.currentTimeMillis() / 1000)+"&imei="+device.getIMEI()+"&sign="+mMD5Sign;

		/*Response Format
		Format sent on success: SUCCESS
		Format sent on failure: FAILURE|[failure code]*/
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "Shenzhoufu BillingResultRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "Shenzhoufu BillingResultRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "Shenzhoufu BillingResultRequest");
	#else
	    whttp.sendByGet(url, tmp);	
	#endif
	}
	
	public boolean handleShenzhoufuBillingResultRequest()
	{
		DBG("XPlayer","handleShenzhoufuBillingResultRequest()");
		
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			tmpHR = Encrypter.crypt(tmpHR);
			try 
			{
				if (tmpHR != null && tmpHR.equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCodeString = "FAILURE";
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","FAILURE last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				return true;
			}

			if (tmpHR != null && (tmpHR.equals(SUCCESS_RESULT) || tmpHR.equals(BILLING_SUCCESS_OFFLINE)))
			{
				DBG("XPlayer","handleShenzhoufuBillingResultRequest Handled Ok. SUCCESS!");
				lastErrorCodeString = BILLING_SUCCESS;
				lastErrorCode = ERROR_NONE;
				DBG("XPlayer","VALID CODE FOR BILLING_SUCCESS!");
				DBG("XPlayer","lastErrorCode="+lastErrorCode);
				return true;
			}
			else if (tmpHR != null && tmpHR.equals(BILLING_PENDING))
			{
			   DBG("XPlayer","handleShenzhoufuBillingResultRequest Handled Ok. PENDING!");
				lastErrorCodeString = BILLING_PENDING;
				lastErrorCode = ERROR_NONE;
				DBG("XPlayer","VALID CODE FOR BILLING_PENDING!");
				DBG("XPlayer","lastErrorCode="+lastErrorCode);
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;	
	}
	
	
	
	#endif
	
	public void sendWAPValidationRequest(String itemID)
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		url = "https://secure.gameloft.com/freemium/wapbilling/validate.php";
		//https://secure.gameloft.com/freemium/wapbilling/validate.php?purchase_id=xxxxx
		tmp = "purchase_id="+GLOFTHelper.getWAPID();
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "WAP ValidationRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "WAP ValidationRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "WAP ValidationRequest");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
	}
	
	public boolean handleWAPValidationRequest()
	{
		DBG("XPlayer","handleWAPValidationRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","WAP Validtaion Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    /*if (whttp.m_response.indexOf("|") == -1)
			{
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}*/
		   //DBG("XPlayer","WAP Validtaion XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","WAP Validtaion Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCodeString = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","WAP Validtaion Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true;
			}

			DBG("XPlayer","*****************"+tmpHR);
			DBG("XPlayer","*****************tmpHR.equals(SUCCESS_RESULT)"+(tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)));
			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","WAP Validtaion handleWAPValidationRequest Handled Ok. SUCCESS!");
			   lastErrorCode = ERROR_NONE;
			   return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
	public void sendEmailExistRequest(String email)
	{
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		
		tmp = "b=checkemail|"+email+"|"+SUtils.getLManager().getRandomCodeNumber();
		
		String url = device.getServerInfo().getURLbilling();
		//String url = "https://secure.gameloft.com/freemium/cc_test/";
		//String url = "https://secure.gameloft.com/freemium/pivotal/";
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "EmailExistRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "EmailExistRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "EmailExistRequest");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
	}
	
	public boolean handleEmailExistRequest() {
		//DBG("XPlayer","handleIABNotification()");
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					mGLLiveUid = null;
					mGLLiveGGI = null;
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError) return true;
		String tmpHR="";
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
		    if (whttp.m_response.indexOf("|") == -1) {
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				JDUMP("Xplayer",tmpHR);
				JDUMP("Xplayer",Encrypter.crypt(tmpHR).equals(FAILURE_RESULT));
				JDUMP("Xplayer",Encrypter.crypt(tmpHR));
				JDUMP("Xplayer",FAILURE_RESULT);
				
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
					DBG("XPlayer","Last error lastErrorCode: " + lastErrorCode);
					return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("E"))
				{
					try {
						tmpHR = tmpHR.substring(1,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","License Handled Ok. SUCCESS!");
			    int mCodeIndex=-1;
			    
				mCodeIndex = TRANSACTION_UNLOCK_CODE - 1;
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, mCodeIndex)))))
			    {
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, mCodeIndex))));
			    	DBG("XPlayer","VALID CODE!!!");
		    		lastErrorCode = ERROR_NONE;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
#endif	
	
	private String buildBaseTracking()
	{
	    String tmp = null;

#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
		String referrer = IReferrerReceiver.getReferrer(SUtils.getContext());
		if(android.text.TextUtils.isEmpty(referrer))
			referrer= "";
		else 
			referrer = "&"+ referrer;
#endif		
	    tmp = "version=2&game="+device.getDemoCode()+
		"&network_country_ISO="+device.getNetworkCountryIso()+
		"&network_operator="+device.getNetworkOperator()+
		"&network_operator_name="+device.getNetworkOperatorName()+
		"&sim_country_iso="+device.getSimCountryIso()+
		"&sim_operator="+device.getSimOperator()+
		"&sim_operator_name="+device.getSimOperatorName()+
		//"&line_number="+device.getLineNumber()+
		"&is_network_roaming="+device.getIsRoaming()+
		"&android_build_device="+device.getDevice()+
		"&android_build_model="+device.getPhoneModel()+
#if GOOGLE_DRM		
		"&user_id=" + device.getUserID()+
#endif //GOOGLE_DRM
#if USE_MARKET_INSTALLER || GOOGLE_STORE_V3
		referrer +
#endif
	    "&d="+SUtils.GetSerialKey();//Serial Key Parameter
	    return tmp;
	}

#if USE_BILLING	|| (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	private String buildPurchaseSuccessTrackingQuery(int mthisValidationMode)
	{
		String tmp = buildBaseTracking();
        switch (mthisValidationMode)
        {
	        case 0:
	        	tmp += "&action=HTTPBillingSuccess";
	        break;
	        
	        case 1:
	        	tmp += "&action=PSMSBillingSuccess";
	        break;
	        
	        case 2:
	        case 3:
	        case 4:
	        	tmp += "&action=CCBillingSuccess";
	        break;
        }
        return tmp;
	}
	
	private String buildPurchaseErrorTrackingQuery(int mthisValidationMode)
	{
		String tmp = buildBaseTracking();

		switch (mthisValidationMode)
        {
	        case 0:
	        	tmp += "&action=HTTPBillingError|"+lastErrorCode;
	        break;
	        
	        case 1:
	        	tmp += "&action=PSMSBillingError";
	        break;
	        
	        case 2:
	        case 3:
	        case 4:
	        	tmp += "&action=CCBillingError|"+lastErrorCode;
	        break;
        }
        return tmp;
	}

	public void sendTrackingPurchaseSucessRequest(int mthisValidationMode)
	{
		String tmp;
		whttp.cancel();
		
		tmp = buildPurchaseSuccessTrackingQuery(mthisValidationMode);
		#if USE_BILLING
		//if(XPlayer.getDevice().VERSION > 1)
			tmp += "&version="+XPlayer.getDevice().VERSION;
		#endif
		url = URL_TRACKING;
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
	    whttp.sendByGet(url, tmp);
	}
	
	/**
	 * This Function tracks the info about the successfully game purchased
	 * 
	 * @return <code>true</code>if done or <code>false</code> if not finished
	 */
	public boolean handleTrackingPurchaseSucessRequest()
	{
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > 8000) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError)
			return true;
		
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			DBG("XPlayer","************************Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{ 
					lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			    lastErrorCode = ERROR_NONE;
			    return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
	public void sendTrackingPurchaseFailedRequest(int mthisValidationMode)
	{
		String tmp;
		whttp.cancel();
		
		tmp = buildPurchaseErrorTrackingQuery(mthisValidationMode);
		#if USE_BILLING
		//if(XPlayer.getDevice().VERSION > 1)
			tmp += "&version="+XPlayer.getDevice().VERSION;
		#endif
		url = URL_TRACKING;
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
	    whttp.sendByGet(url, tmp);
	}
	
	/**
	 * This Function tracks the info about the failure attempting a game purchase
	 * 
	 * @return <code>true</code>if done or <code>false</code> if not finished
	 */
	public boolean handleTrackingPurchaseFailedRequest()
	{
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > 8000) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError)
			return true;
		
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{ 
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{ 
				    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			    lastErrorCode = ERROR_NONE;
			    return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
	public void sendTimeStampPurchaseRequest(String URL)
	{
		DBG("XPlayer","sendTimeStampPurchaseRequest()");
		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		
		url = URL;
		tmp = "gettimestamprequest=1";
		/*
		Response Format
		Format sent on success: SUCCESS|[success code]
		Format sent on failure: FAILURE|[failure code]*/
		
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "TimeStampPurchaseRequest");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "TimeStampPurchaseRequest");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "TimeStampPurchaseRequest");
	#else
		whttp.sendByGet(url, tmp);
	#endif
	}

	public boolean handleTimeStampPurchaseRequest()
	{
		DBG("XPlayer","handleTimeStampPurchaseRequest()");
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
			lastErrorCodeString = whttp.m_response;
			lastErrorCode = ERROR_NONE;
			DBG("XPlayer","Last error lastErrorCodeString: " + lastErrorCodeString);
				    return true; 
		}

		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
#endif //#if USE_BILLING


#if USE_PSS
	//E-Commerce tracks the Codes that Verizon Server sends in an attempt of Purchase.		
	public void sendSavePurchaseRequest(String price, String code, String gameCode, String itemCode)
	{
		//String URL = "http://wapshop.gameloft.com/us/verizon/tnbunlockfake/";
// 		String URL = "http://wapshop.gameloft.com/php5/us/verizon_test/tnbunlockfake/";
		String validationQuery = "";

		whttp.cancel();
	
		int i = device.createUniqueCode();
		//if(SUtils.getLManager().getRandomCodeNumber()==-1)
			//SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());

		// Modified so that E-Commerce tracks the Codes that Verizon Server sends in an attempt of Purchase.		
		// validationQuery += "b=ppdwap|"+device.getDemoCode()+"|" + price +"|" + SUtils.getLManager().getRandomCodeNumber();
		validationQuery += "b=ppdwap|"+device.getDemoCode()+"|" + price +"|" + i +"|" +itemCode+"|" + code+"|"+gameCode+"|";
		
		DBG("XPlayer","uncoded query = "+validationQuery);
		String tmp_encoded=validationQuery.substring(0,2)+ encodeQuery(validationQuery.substring(2));
		validationQuery = tmp_encoded;
		//validationQuery = encodeQuery(validationQuery);

		DBG("PSS", "Value for Validation Query = "+validationQuery);
		lastErrorCode = ERROR_INIT;
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}

		whttp.sendByGet(PSSURL, validationQuery);
	}
	
	private void setErrorParams(String[] params)
	{
		Boolean f = new Boolean(false);
		params[0] = f.toString();
		params[2] = params[4] = FAILURE_RESULT;
		params[1] = params[3] = params[5] = lastErrorCode+"";
	}
	public boolean handleSavePurchaseRequest(String[] params)
	{
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT)
			{
				if (System.currentTimeMillis() - callstarttime > 20000)
				{
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					
					setErrorParams(params);
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError) return true;
		
		String tmpHR="";

		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
 		    if (whttp.m_response.indexOf("|") == -1)
 			{
 				// Decode the message.
 				whttp.m_response = Encoder.Blob2String(whttp.m_response);
 			}
			DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
		
			try 
			{   // The response format is: SUCCESS or FAILURE|Error Message
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				    DBG("XPlayer", "Last error lastErrorCode: " + lastErrorCode);
					setErrorParams(params);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{   // the server answered something funny
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				
				setErrorParams(params);
				return true; //not handle by this request        	 
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			    DBG("XPlayer", "The Game Purchase was successfully Saved on our Server!");
			    DBG("PSS", whttp.m_response);
	    		lastErrorCode = ERROR_NONE;
				
				String res = whttp.m_response.trim();
				
				int lid = 0;
				int hid = res.indexOf('|');
				int i = 0;
				
				Boolean t = new Boolean(true);
				params[i++] = t.toString();
				do
				{
					params[i] = res.substring(lid, hid);
					//JDUMP("PSS", params[i]);
					
					lid = hid+1;
					hid = res.indexOf('|', lid);
					i++;
				}while(hid > 0);
				
				params[i] = res.substring(lid);
				return true;
			}
		}
		// Should never hit this.
		lastErrorCode = ERROR_BAD_RESPONSE;
		setErrorParams(params);
		return true;
	}
#endif

#if USE_INSTALLER && !FORCE_WIFI_ONLY_MODE
	/**
	 * This Function  sent a request asking for the installer flow to follow (Wifi Only | Wifi/Carrier)
	*/
	public void sendWifiModeRequest()
    {
        String tmp;
        whttp.cancel();

        tmp = buildBaseTracking();
		tmp += "&return_allowed=1";
		tmp += "&http=1_0";
        DBG("GameInstaller", "sendWifiModeRequest");
        DBG("GameInstaller", "Qry " + tmp);

        url = URL_WIFI_MODE;

        lastErrorCode = ERROR_INIT;

        if (ENABLE_TIMEOUT)
        {
            callstarttime = System.currentTimeMillis();
        }
        whttp.sendByGet(url, tmp);
    }
	
	/**
	 * This Function handles the response from sendWifiModeRequest()
	 * @return <code>true</code>if done or <code>false</code> if not finished
	*/	
    public boolean handleWifiModeRequest()
    {
        if (whttp.isInProgress())
        {
            if (ENABLE_TIMEOUT)
            {
                if (System.currentTimeMillis() - callstarttime > 8000) {//This Function Validates the Previous Purchase
                    cancel();
                    lastErrorCode = ERROR_CONNECTION;
                    return true;
                }
            }
            return false;
        }

        if (whttp.m_bError)
            return true;

        String tmpHR="";
        DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
        if ((whttp.m_response != null) && (whttp.m_response != ""))
        {
            tmpHR = whttp.m_response;

            DBG("XPlayer","Value for response on tmpHR ="+tmpHR);

			// The response format could be: 
				//WIFI_ONLY or WIFI_3G or WIFI_3G_ORANGE_IL
			//or
				//WIFI_ONLY|XXX or WIFI_3G|XXX or WIFI_3G_ORANGE_IL|XXX
			if (tmpHR.contains("WIFI_ONLY") || tmpHR.contains("WIFI_3G")|| tmpHR.contains("WIFI_3G_ORANGE_IL"))
			{   
				lastErrorCode = ERROR_NONE;
				DBG("Tracker","Last error lastErrorCode: " + lastErrorCode);
				return true;
			}
            else
            {   // the server answered something else
                lastErrorCode = ERROR_BAD_RESPONSE;
                return true; //not handle by this request
            }
        }
        // Should never hit this.
        lastErrorCode = ERROR_BAD_RESPONSE;
        return true;
    }
#endif //#if USE_INSTALLER && !FORCE_WIFI_ONLY_MODE


#if USE_TRACKING_FEATURE_INSTALLER
    public void sendInstallerTrackingOptionsRequest(String qry)
    {
        String tmp;
        whttp.cancel();

        tmp = buildBaseTracking();
		tmp += qry;

        DBG("Tracker", "sendInstallerTrackingOptionsRequest");
        DBG("Tracker", "Qry " + tmp);

        url = URL_TRACKING;

        lastErrorCode = ERROR_INIT;

        if (ENABLE_TIMEOUT)
        {
            callstarttime = System.currentTimeMillis();
        }
        whttp.sendByGet(url, tmp);
    }
    public boolean handleInstallerTrackingOptionsRequest()
    {
        if (whttp.isInProgress())
        {
            if (ENABLE_TIMEOUT)
            {
                if (System.currentTimeMillis() - callstarttime > 8000) {//This Function Validates the Previous Purchase
                    cancel();
                    lastErrorCode = ERROR_CONNECTION;
                    return true;
                }
            }
            return false;
        }

        if (whttp.m_bError)
            return true;

        String tmpHR="";
        DBG("Tracker","Value for response on whttp.m_response ="+whttp.m_response);
        if ((whttp.m_response != null) && (whttp.m_response != ""))
        {
            tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);

            DBG("Tracker","Value for response on tmpHR ="+tmpHR);

            try
            {   // The response format is: SUCCESS or FAILURE|Error Message
                if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT))
                {   // There has been an error.
                    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
                    DBG("Tracker","Last error lastErrorCode: " + lastErrorCode);
                    return true;
                }
            }
            catch (NumberFormatException e)
            {   // the server answered something funny
                lastErrorCode = ERROR_BAD_RESPONSE;
                tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
                return true; //not handle by this request
            }

            if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT))
            {
                lastErrorCode = ERROR_NONE;
                return true;
            }
        }
        // Should never hit this.
        lastErrorCode = ERROR_BAD_RESPONSE;
        return true;
    }
	
	
	
	// added for send install referrer
	public void sendInstallerTrackingInstallReferrer(String qry)
    {
        String tmp;
        whttp.cancel();

        tmp = buildBaseTracking();
		tmp += qry;

        DBG("Tracker", "sendInstallerTrackingInstallReferrer");
        DBG("Tracker", "Qry [" + tmp+"]");

        url = NEW_URL_TRACKING;

        lastErrorCode = ERROR_INIT;

        if (ENABLE_TIMEOUT)
        {
            callstarttime = System.currentTimeMillis();
        }
        whttp.sendByGet(url, tmp);
    }
	// added for send install referrer
    public boolean handleInstallerTrackingInstallReferrer()
    {
        if (whttp.isInProgress())
        {
            if (ENABLE_TIMEOUT)
            {
                if (System.currentTimeMillis() - callstarttime > 8000) {//This Function Validates the Previous Purchase
                    cancel();
                    lastErrorCode = ERROR_CONNECTION;
                    return true;
                }
            }
            return false;
        }

        if (whttp.m_bError)
            return true;

        String tmpHR="";
        DBG("Tracker","Value for response on whttp.m_response ="+whttp.m_response);
        if ((whttp.m_response != null) && (whttp.m_response != ""))
        {
            tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);

            DBG("Tracker","Value for response on tmpHR ="+tmpHR);

            try
            {   // The response format is: SUCCESS or FAILURE|Error Message
                if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT))
                {   // There has been an error.
                    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
                    DBG("Tracker","Last error lastErrorCode: " + lastErrorCode);
                    return true;
                }
            }
            catch (NumberFormatException e)
            {   // the server answered something funny
                lastErrorCode = ERROR_BAD_RESPONSE;
                tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
                return true; //not handle by this request
            }

            if (tmpHR.equals(SUCCESS_RESULT))
            {
                lastErrorCode = ERROR_NONE;
                return true;
            }
        }
        // Should never hit this.
        lastErrorCode = ERROR_BAD_RESPONSE;
        return true;
    }
	
	
#endif //#if USE_TRACKING_FEATURE_INSTALLER

#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	
	#if USE_BILLING
	public void sendValidationRequest()
	{
		
		String URL = "https://secure.gameloft.com/wap/paybox/";

		String validationQuery = "";

		whttp.cancel();
		
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		
		validationQuery += "b=checkpurchase|"+device.getDemoCode()+"|"+SUtils.getLManager().getRandomCodeNumber();
		#if USE_BILLING
		//if(XPlayer.getDevice().VERSION > 1)
			validationQuery += "&version="+XPlayer.getDevice().VERSION;
		#endif
		DBG("XPlayer","Value for Validation Query = "+validationQuery);
		
		lastErrorCode = ERROR_INIT;
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}

		whttp.sendByGet(URL, validationQuery);
	}

	/**
	 * This Function Validates the Previous Purchase of the game
	 * 
	 * @return <code>true</code>if the game was billed previously or <code>false</code> if not
	 */
	public boolean handleValidationRequest()
	{
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > 8000) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError) return true;
		String tmpHR="";
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
		    if (whttp.m_response.indexOf("|") == -1) {
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{
				    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true; //not handle by this request        	 
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","Game Validated as FULL VERSION, OK. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, FULL_VERSION_VALIDATION_TRANSACTION_UNLOCK_CODE)))))
			    {
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, FULL_VERSION_VALIDATION_TRANSACTION_UNLOCK_CODE))));
			    	DBG("XPlayer","VALID CODE!!!");
			    	AModel.successResult = true;
			    	
		    		lastErrorCode = ERROR_NONE;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
			
			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_MRC_RESULT)) 
			{
			   DBG("XPlayer","Game Validated as MRC VERSION, OK. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, FULL_VERSION_VALIDATION_TRANSACTION_UNLOCK_CODE)))))
			    {
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, FULL_VERSION_VALIDATION_TRANSACTION_UNLOCK_CODE))));
			    	DBG("XPlayer","VALID CODE!!!");
			    	AModel.successResult = true;
			    	
		    		lastErrorCode = MRC_VALID;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
#if USE_BILLING_FOR_CHINA
	// simple http request
	public void sendRequest(String url, String query)
	{
		whttp.cancel();
		DBG("XPlayer","sendRequest() url = "+url);
		DBG("XPlayer","sendRequest() query = "+query);
		
		lastErrorCode = ERROR_INIT;
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}

		whttp.sendByGet(url, query);
	}
	
	public boolean handleRequest()
	{
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > 8000) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;

		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
			lastErrorCode = ERROR_NONE;
			if (whttp.m_response.contains("error"))
			{
				 lastErrorCode = ERROR_BAD_RESPONSE;
			}
		    return true;
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
#endif	

	private final int MRC_SERVER_ERROR = -1;
	private final int MRC_NOT_VALID = 0;
	private final int MRC_VALID = 1;

	/**
	 * This Function Validates the MRC Status of the game
	 * 
	 * @return <code>true</code>if the game has a valid license or <code>false</code> if not
	 */

	public boolean handleMRCValidationRequest()
	{
		lastErrorCode = MRC_SERVER_ERROR;
		
		if (whttp.isInProgress())
		{
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > 8000) {
					cancel();
					lastErrorCode = MRC_SERVER_ERROR;
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError) return true;
		String tmpHR="";
		
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		
		if ((whttp.m_response != null) && (whttp.m_response != ""))
		{
		    if (whttp.m_response.indexOf("|") == -1)
		    {
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		    
		    DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{
				   lastErrorCode = MRC_NOT_VALID;
				   DBG("XPlayer","Last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_MRC_RESULT)) 
			{
			   DBG("XPlayer","MRC Suscription validated as ACTIVE, OK. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, FULL_VERSION_VALIDATION_TRANSACTION_UNLOCK_CODE)))))
			    {
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, FULL_VERSION_VALIDATION_TRANSACTION_UNLOCK_CODE))));
			    	DBG("XPlayer","VALID CODE!!!");
			    	AModel.successResult = true;
			    	
		    		lastErrorCode = MRC_VALID;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		lastErrorCode = MRC_SERVER_ERROR;
		return true;
	}
	
	
	public void sendPromoCodeValidationRequest(String mPromoCode, String TICKET_URL)
	{
		
		#if USE_VOUCHERS
		String URL = "https://confirmation.gameloft.com/tryandbuy/xticket/index.php";
		#else
		String URL = TICKET_URL;
		#endif
/*			Requests format:
			ppticket|<IGP code>|<ticket>|<security hash>
			
			Requests examples:
			ppticket|H007|ABC123|9999
			
			Response format (same as ppdwap):
			SUCCESS|SuccessCode|SecurityCode
			FAILURE|ErrorCode
			
			Response examples:
			SUCCESS|10003|123345
			FAILURE|12345									*/

		String validationQuery = "";

		whttp.cancel();
	
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		
		//validationQuery += "b=checkpurchase|"+device.getDemoCode()+"|"+SUtils.getLManager().getRandomCodeNumber()+"&d="+SUtils.getLManager().GetSerialKey();
		validationQuery += "b=ppticket|"+device.getDemoCode()+"|"+mPromoCode+"|"+SUtils.getLManager().getRandomCodeNumber();
		#if USE_BILLING
		//if(XPlayer.getDevice().VERSION > 1)
			validationQuery += "&version="+XPlayer.getDevice().VERSION;
		#endif
		//SUtils.log("Value for Validation Query = "+validationQuery);
		
		lastErrorCode = ERROR_INIT;
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}

		whttp.sendByGet(URL, validationQuery);
	}
	
	public boolean handlePromoCodeValidationRequest()
	{
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > 5000) {//This Function Validates the Previous Purchase
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		
		if (whttp.m_bError) return true;
		String tmpHR="";
		//SUtils.log("Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
		    if (whttp.m_response.indexOf("|") == -1) {
				// Decode the message.
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		    //SUtils.log("XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			//SUtils.log("Value for response on tmpHR ="+tmpHR);
			
			try 
			{   // The response format is: SUCCESS or FAILURE|Error Message
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				    //SUtils.log("Last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{   // the server answered something funny
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true; //not handle by this request        	 
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			    //SUtils.log("Game Validated as FULL VERSION, OK. SUCCESS!");
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE)))))
			    {
			    	/*SUtils.log("Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	SUtils.log("Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	SUtils.log("Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	SUtils.log("Server Response = "+(Integer.parseInt(getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE))));
			    	SUtils.log("VALID CODE!!!");*/
			    	AModel.successResult = true;
			    	//dateFromServer = Long.parseLong(getValue(whttp.m_response, TRANSACTION_DATE));
			    	
		    		lastErrorCode = ERROR_NONE;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		// Should never hit this.
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	public void sendProfileRequest()
	{
		
		//String URL = "http://confirmation.gameloft.com/sms/unlock_profiles.php";
		String URL = "https://secure.gameloft.com/sms/unlock_profiles.php";
		//String URL = "https://secure.gameloft.com/sms/unlock_profiles_test.php";
		String profileQuery = "";

		whttp.cancel();
	
		profileQuery += "game="+device.getDemoCode()+
						"&network_country_ISO="+device.getNetworkCountryIso()+
						"&network_operator="+device.getNetworkOperator()+
						"&network_operator_name="+device.getNetworkOperatorName()+
						"&sim_country_iso="+device.getSimCountryIso()+
						"&sim_operator="+device.getSimOperator()+
						"&sim_operator_name="+device.getSimOperatorName()+
						"&line_number="+device.getLineNumber()+
						"&is_network_roaming="+device.getIsRoaming()+
						"&android_build_device="+device.getDevice()+
						"&android_build_model="+device.getPhoneModel()
						+"&supports_wapother=1"
					#if USE_VOUCHERS
						+"&supports_voucher=1"
					#endif
					#if USE_BILLING && USE_BOKU_FOR_BILLING	
						+"&supports_boku=1"
					#endif
					#if USE_BILLING && USE_BILLING_FOR_CHINA
						+"&supports_ump=1&supports_ipx=1&channel="+GL_CHANNEL
					#endif
						;
		#if BELL_USE_SECOND_LANG
		if (Locale.getDefault().getISO3Language().equals("FRA"))
			profileQuery +="&lang=fr";
		#endif
		#if USE_BILLING
		//if(XPlayer.getDevice().VERSION > 1)
			profileQuery += "&version="+XPlayer.getDevice().VERSION;
		#endif
		//Test for no profile
		//profileQuery ="game=G999&network_country_ISO=br&network_operator=72402&network_operator_name=Tim&sim_country_iso=br&sim_operator=72402&sim_operator_name=Tim&line_number=&is_network_roaming=true&android_build_device=passion&android_build_model=Nexus";
		//profileQuery="";
		//This is for double option
		//profileQuery="game=H011&network_country_ISO=&network_operator=&network_operator_name=&sim_country_iso=es&sim_operator=21407&sim_operator_name=&line_number=00&is_network_roaming=false&android_build_device=bravo&android_build_model=HTC Desire";
		lastErrorCode = ERROR_INIT;
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}

		
		whttp.sendByGet(URL, profileQuery);
	}
	/**
	 * Handles the profile request, after such a
	 * request has been initiated with <code>sendProfileRequest()</code>
	 * @see #sendProfileRequest()
	 * @return <code>true</code> if the server response has been already validated, <code>false</code> if not. 
	 */
	
	public boolean handleProfileRequest() {
		DBG("Billing","handleProfileRequest()");
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > 10000) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;

		DBG("Billing","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
			lastErrorCode = ERROR_NONE;
			if (whttp.m_response.contains("error"))
			{
				 lastErrorCode = ERROR_BAD_RESPONSE;
			}
		    return true;
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	#endif //#if USE_BILLING

#if USE_BILLING || GAMELOFT_SHOP	
	/**
	 * Builds the query to be requested to the server.
	 * @return The query.
	 * */
	private String buildQuery(int validationMode) {
	    String tmp = "";
		//JDUMP("XPlayer",(whttp.getModel() instanceof APP_PACKAGE.billing.Model));
		//JDUMP("XPlayer",(whttp.getModel() instanceof APP_PACKAGE.iab.Model));
		String contentDesc = 
		#if USE_BILLING && GAMELOFT_SHOP
			(whttp.getModel() instanceof APP_PACKAGE.billing.Model) ? device.getDemoCode():GLOFTHelper.GetItemId();
		#elif USE_BILLING
			device.getDemoCode();
		#else//if GAMELOFT_SHOP
			GLOFTHelper.GetItemId();
		#endif		
                switch (validationMode) {
        case PURCHASE_HTTP:
        	DBG("XPlayer","Purchasing by HTTP");
        	tmp = "b=ppdwap|"+device.getDemoCode()+"|"+device.getServerInfo().getGamePrice()+"|"+SUtils.getLManager().getRandomCodeNumber();
        break;
        case PURCHASE_MRC:
        	DBG("XPlayer","Purchasing MRC by HTTP");
        	tmp = "b=mrcsub|"+device.getDemoCode()+"|"+device.getServerInfo().getGamePrice()+"|"+SUtils.getLManager().getRandomCodeNumber();
        break;
        case PURCHASE_CC_LOGIN:
        	DBG("XPlayer","Build Query Login");
            tmp = 	"b=" + TAG_CC_LOGIN + "|" + mUserInfo.getEmail() +
            		"|" + mUserInfo.getPassword() + "|" + SUtils.getLManager().getRandomCodeNumber();
        break;
        case PURCHASE_CC_USERBILL:
        	DBG("XPlayer","Build Query User Bill");
        	 tmp = 	"b=" + TAG_CC_USERBILL + "|" + contentDesc + "|" + device.getServerInfo().getStringCurrencyValue() +
     				"|" + device.getServerInfo().getGamePrice() + "|" + mUserInfo.getEmail() + "|" + mUserInfo.getPassword() + 
     				"|" + SUtils.getLManager().getRandomCodeNumber();
        break;
        case PURCHASE_CC_NEWUSERBILL:
        	DBG("XPlayer","Build Query New User Bill");
            tmp = 	"b=" + TAG_CC_NEWUSERBILL + "|" + contentDesc + "|" + device.getServerInfo().getStringCurrencyValue() +
            		"|" + device.getServerInfo().getGamePrice() + "|" + mUserInfo.getEmail() + "|" + mUserInfo.getPassword() + 
            		"|" + mUserInfo.getCardNumber() + "|" + mUserInfo.getExpirationDate() + 
            		"|" + mUserInfo.getSecurityCode() + "|" + SUtils.getLManager().getRandomCodeNumber();
        break;
		case PURCHASE_CC_ADDCARDBILL:
        	DBG("XPlayer","Build Query Add Card Bill");
            tmp = 	"b=" + TAG_CC_ADDCARDBILL + "|" + contentDesc + "|" + device.getServerInfo().getStringCurrencyValue() +
            		"|" + device.getServerInfo().getGamePrice() + "|" + mUserInfo.getEmail() + "|" + mUserInfo.getPassword() + 
            		"|" + mUserInfo.getCardNumber() + "|" + mUserInfo.getExpirationDate() + 
            		"|" + mUserInfo.getSecurityCode() + "|" + SUtils.getLManager().getRandomCodeNumber();
        break;
        case PURCHASE_CC_SINGLEBILL:
        	DBG("XPlayer","Build Query SingleBill");
            tmp = 	"b=" + TAG_CC_SINGLEBILL + "|" + contentDesc + "|" + device.getServerInfo().getStringCurrencyValue() +
            		"|" + device.getServerInfo().getGamePrice() + "|" + mUserInfo.getEmail() + "|" + mUserInfo.getCardNumber() + 
            		"|" + mUserInfo.getExpirationDate() + "|" + mUserInfo.getSecurityCode() + "|" + SUtils.getLManager().getRandomCodeNumber();
        break;
        case PURCHASE_CC_FORGOTPW:
        	DBG("XPlayer","Build Query Forgot Password");
        	 tmp = 	"b=" + TAG_CC_FORGOTPW + "|" + mUserInfo.getEmail() + "|" + SUtils.getLManager().getRandomCodeNumber();
        break;
        }
       DBG("XPlayer","String Query before encoding: " + tmp);
        return tmp;
	}

	/**
	 * Handles the license validation request
	 * @see #handleValidateLicense()
	 * @return <code>true</code> if the server response has been already validated, <code>false</code> if not. 
	 */
	public boolean handleValidateLicense() {
		DBG("XPlayer","handleValidateLicense()");
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
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
		    if (whttp.m_response.indexOf("|") == -1) {
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB") || tmpHR.contains("L") || tmpHR.contains("U"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","License Handled Ok. SUCCESS!");
			    int mCodeIndex=-1;
			    
			    if (mValidationMode == PURCHASE_CC_FORGOTPW)
			    	mCodeIndex = TRANSACTION_UNLOCK_CODE-1;
				else
					mCodeIndex = TRANSACTION_UNLOCK_CODE;
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, mCodeIndex)))))
			    {
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber()+" ^ "+Device.SMS_UNLOCK_KEY+" = "+(SUtils.getLManager().getRandomCodeNumber() ^ Device.SMS_UNLOCK_KEY));
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, mCodeIndex))));
			    	DBG("XPlayer","VALID CODE!!!");
			    	AModel.successResult = true;
			    	
			    	if (mValidationMode == PURCHASE_CC_LOGIN)
			    		lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
			    	else
			    		lastErrorCode = ERROR_NONE;
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
#endif 	//#if USE_BILLING

#endif	#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
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
		Return the current carrier configuration.
	/**/
	public static Carrier getCarrier()
	{
		return device.getCarrier();
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
#if USE_BILLING || (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	public static int getLastErrorMessageId() {
		for (int i = 0; i < errorMessages.length; i++) {
			if (lastErrorCode == errorMessages[i].errorCode) {
				return errorMessages[i].strId;
			}
		}
	#if USE_BILLING
		return R.string.AB_TRANSACTION_FAILED;
	#else
		return R.string.IAB_TRANSACTION_FAILED;
	#endif
	}
#endif

	/**
	 * Returns the last error code.
	 * @return int With the last error code.
	 * */
	public static int getLastErrorCode () {
		return lastErrorCode;
	}
	public static String lastErrorCodeString = null;
	public static String getLastErrorCodeString()
	{
		if(lastErrorCodeString!=null)
			return (lastErrorCodeString);
		else
			return ("ERROR");
	}
	
	public static String eComTxId = null;
	public static void clearLastEComTxId()
	{
		eComTxId = null;
	}
	
	public static String getLastEComTxId()
	{
		if(eComTxId!=null && !eComTxId.equals(""))
			return eComTxId;
		else
			return null;
	}
	
	public static String mGLLiveUid = null;
	public static String mGLLiveGGI = null;
	public static String mUserCreds = null;
	public static String mDataCenter = null;
	public static void setUserCreds(String gglive_uid, String creds)
	{
		DBG("XPlayer","Setting values for gglive_uid="+gglive_uid+" ,gglive_creds="+creds);
		mGLLiveUid = gglive_uid;
		mGLLiveGGI = GGI;
		mUserCreds = creds;
	}
	public static void setDataCenter(String dataCenter)
	{
		DBG("XPlayer","Setting values for data_center="+dataCenter);
		mDataCenter = dataCenter;
	}
#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM) //new methods
		
		public static String UMP_MO1 = null;
		public static String UMP_MO2 = null;
		private static String mMD5Sign = null;
		
		public static String getUMPMO1()
		{
			if(UMP_MO1!=null)
				return (UMP_MO1);
			else
				return ("ERROR");
		}
		
		public static String getUMPMO2()
		{
			if(UMP_MO2!=null)
				return (UMP_MO2);
			else
				return ("ERROR");
		}	
	
		public static void setGGIUID(String ggi, String gglive_uid)
		{
			DBG("XPlayer","Setting values for ggi="+ggi+" and gglive_uid="+gglive_uid);
			mGLLiveUid = gglive_uid;
			mGLLiveGGI = ggi;
		}
		
		public static void setGGIUID(String ggi, String gglive_uid, String creds)
		{
			DBG("XPlayer","Setting values for ggi="+ggi+" ,gglive_uid="+gglive_uid+" ,gglive_creds="+creds);
			mGLLiveUid = gglive_uid;
			mGLLiveGGI = ggi;
			mUserCreds = creds;
		}
		
	#if GAMELOFT_SHOP
		public static void setPurchaseID(String mID)
		{
		    mPurchaseID = mID;
		}
	#endif
	
	public void sendIABProfileRequest(String url)
	{
	#if !RELEASE_VERSION	
		String TAG="XPlayer";
	#endif
	
		String profileQuery = "";
		whttp.cancel();
		Locale locale = Locale.getDefault();
		profileQuery += "game="+device.ValidateStringforURL(device.getDemoCode())+
						"&network_country_ISO="+device.ValidateStringforURL(device.getNetworkCountryIso())+
						"&network_operator="+device.ValidateStringforURL(device.getNetworkOperator())+
						"&network_operator_name="+device.getNetworkOperatorName()+
						"&sim_country_iso="+device.ValidateStringforURL(device.getSimCountryIso())+
						"&sim_operator="+device.ValidateStringforURL(device.getSimOperator())+
						"&sim_operator_name="+device.getSimOperatorName()+
						"&line_number="+device.ValidateStringforURL(device.getLineNumber())+
						"&is_network_roaming="+device.getIsRoaming()+
						"&android_build_device="+device.getDevice()+
						"&android_build_model="+device.getPhoneModel()+
					#if !USE_PHD_PSMS_BILL_FLOW
						"&supportswap=1"+
					#endif
						"&supports_sms=1"+
						"&supports_wapother=1"+
					#if !USE_PHD_PSMS_BILL_FLOW
						"&supports_wappp=1"+
					#endif
					//"&testprofile=1"+
					#if GAMELOFT_SHOP
						"&d="+SUtils.GetSerialKey()+//Serial Key Parameter
					#endif
					#if SHENZHOUFU_STORE
						"&supports_shenzhoufu=1"+
					#endif
					#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
						"&supports_psms=1"+
					#endif
					#if USE_UMP_R3_BILLING
						"&supports_ump=1"+
						"&imei="+device.getIMEI()+
					#endif
						"&game_version="+GAME_VERSION_NAME+
					#if USE_PHD_PSMS_BILL_FLOW
						"&v=1.1"+
					#endif
						"&lang="+device.ValidateStringforURL(locale.getLanguage().toLowerCase());
		lastErrorCode = ERROR_INIT;
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "Get Content Feed");
		LOGGING_APPEND_REQUEST_PAYLOAD(profileQuery, "Get Content Feed");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, profileQuery, "Get Content Feed");
	#else
		whttp.sendByGet(url, profileQuery);
	#endif
	}
	
	/**
	 * Handles the iab profile request, after such a
	 * request has been initiated with <code>sendIABProfileRequest()</code>
	 * @see #sendIABProfileRequest()
	 * @return <code>true</code> if the server response has been already validated, <code>false</code> if not. 
	 */
	
	public boolean handleIABProfileRequest() {
		//DBG("InAppBilling","handleProfileRequest()");
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > 10000) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		if (whttp.m_bError) return true;

		//DBG("InAppBilling","Value for response on whttp.m_response ="+whttp.m_response);
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
			lastErrorCode = ERROR_NONE;
			if (whttp.m_response.contains("error"))
			{
				 lastErrorCode = ERROR_BAD_RESPONSE;
			}
		    return true;
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	
	public void sendIABNotification(int validationMode, String action
								#if GOOGLE_STORE_V3 || BOKU_STORE || VZW_STORE || SAMSUNG_STORE || SHENZHOUFU_STORE || BAZAAR_STORE
									,String orderId
								#elif AMAZON_STORE	
									,String orderId, String AmzUid
								#elif SKT_STORE
									,String transactionId
								#endif	
									)
	{
		mValidationMode = validationMode;

		String tmp;
		whttp.cancel();
		SUtils.getLManager().setRandomCodeNumber(device.createUniqueCode());
		
		tmp = "b="+action+"|"+device.ValidateStringforURL(device.getServerInfo().getSelectedItem())+"|"+
			device.ValidateStringforURL(device.getServerInfo().getGamePrice())+"|"+device.ValidateStringforURL(device.getServerInfo().getMoneyBilling())+"|"+
			SUtils.getLManager().getRandomCodeNumber()
			#if BOKU_STORE
				+"|"+device.ValidateStringforURL(orderId)
			#elif AMAZON_STORE
				+"|"+device.ValidateStringforURL(orderId)+"|"+device.ValidateStringforURL(AmzUid)
			#elif SKT_STORE
				+"&txid="+device.ValidateStringforURL(transactionId)
				#if TEST_SKT_STORE
				+"&test_env=1"
				#else
				+"&test_env=0"
				#endif
			#endif
			;
		#if USE_IAP_VALIDATION_BETA_SERVER && ITEMS_STORED_BY_GAME == 0
			tmp+="&beta=1";
		#endif
		#if BAZAAR_STORE
			tmp+="&confirmdelivery=yes";

			if (!android.text.TextUtils.isEmpty(SUtils.getInjectedIGP()))
			{
				tmp+="&injected_igp="+SUtils.getInjectedIGP();
			}
			if (!android.text.TextUtils.isEmpty(SUtils.getInjectedSerialKey()))
			{
				tmp+="&d="+SUtils.getInjectedSerialKey();
			}
		#endif
		//tmp = "b=contentpurchase|"+"3997"+"|"+"0.99"+"|"+"1"+"|"+9999;//testing values
		
		url = device.getServerInfo().getURLbilling();
		lastErrorCode = ERROR_INIT;
		
		
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "ContentPurchase");
		LOGGING_APPEND_REQUEST_PAYLOAD(tmp, "ContentPurchase");
	#if USE_IN_APP_GLOT_LOGGING
		whttp.sendByGet(url, tmp, "ContentPurchase");
	#else
	    whttp.sendByGet(url, tmp);
	#endif
	}
	
	public boolean handleIABNotification() {
		//DBG("XPlayer","handleIABNotification()");
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					mGLLiveUid = null;
					mGLLiveGGI = null;
					mUserCreds = null;
					return true;
				}
			}
			return false;
		}
		mGLLiveUid = null;
		mGLLiveGGI = null;
		mUserCreds = null;
		
		if (whttp.m_bError) return true;
		String tmpHR="";
		if ((whttp.m_response != null) && (whttp.m_response != "")) {
		    if (whttp.m_response.indexOf("|") == -1) {
				whttp.m_response = Encoder.Blob2String(whttp.m_response);
			}
		   DBG("XPlayer","XPlayer: Decoded Server Response: " + whttp.m_response);
			
			tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT);
			
			DBG("XPlayer","Value for response on tmpHR ="+tmpHR);
			
			try 
			{
				if (tmpHR != null && Encrypter.crypt(tmpHR).equals(FAILURE_RESULT)) 
				{   // There has been an error.
				    lastErrorCode = Integer.parseInt(getValue(whttp.m_response, TRANSACTION_RESULT_ERROR));
				   DBG("XPlayer","Last error lastErrorCode: " + lastErrorCode);
				    return true; 
				}
			} 
			catch (NumberFormatException e) 
			{
				lastErrorCode = ERROR_BAD_RESPONSE;
				tmpHR = getValue(whttp.m_response, TRANSACTION_RESULT_ERROR);
				if (tmpHR.contains("PB"))
				{
					try {
						tmpHR = tmpHR.substring(2,tmpHR.length());
						lastErrorCode = Integer.parseInt(tmpHR);
					} catch (NumberFormatException ex)
					{
						//do nothing already has the ERROR_BAD_RESPONSE set
					}
					
				}
				return true;
			}

			if (tmpHR != null && Encrypter.crypt(tmpHR).equals(SUCCESS_RESULT)) 
			{
			   DBG("XPlayer","License Handled Ok. SUCCESS!");
			    int mCodeIndex=-1;
			    
				mCodeIndex = TRANSACTION_UNLOCK_CODE;
			    
			    if (SUtils.getLManager().isValidCode((Integer.parseInt(getValue(whttp.m_response, mCodeIndex)))))
			    {
			    	DBG("XPlayer","Evaluating Random Code Sent :"+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Evaluating Unlock Key :"+Device.SMS_UNLOCK_KEY);
			    	DBG("XPlayer","Evaluating "+SUtils.getLManager().getRandomCodeNumber());
			    	DBG("XPlayer","Server Response = "+(Integer.parseInt(getValue(whttp.m_response, mCodeIndex))));
			    	DBG("XPlayer","VALID CODE!!!");
		    		lastErrorCode = ERROR_NONE;

		    		eComTxId = getValue(whttp.m_response, TRANSACTION_UNLOCK_CODE + 1);
					DBG("XPlayer","Transaction ID: "+eComTxId);
			    }
			    else
			    {
			    	lastErrorCode = ERROR_BAD_RESPONSE;
			    }
				return true;
			}
		}
		lastErrorCode = ERROR_BAD_RESPONSE;
		return true;
	}
	public static String mPurchaseID = "";
#endif //#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
#if USE_INSTALLER
	public void sendRequestNewVersion(String URL, String validationQuery)
	{
		str_response  = null;
		//String URL = "http://confirmation.gameloft.com/partners/android/update_check.php";
		//String validationQuery = "key=0olFXVim5az";
		DBG("XPlayer","sendRequestNewVersion ="+URL);
		whttp.cancel();
		lastErrorCode = ERROR_INIT;
		if (ENABLE_TIMEOUT)
		{
			callstarttime = System.currentTimeMillis();
		}

		whttp.sendByGet(URL, validationQuery);
	}
	
	public boolean handleRequestNewVersion()
	{
	    str_response = null;
		if (whttp.isInProgress()) {
			if (ENABLE_TIMEOUT) {
				if (System.currentTimeMillis() - callstarttime > CONN_TIMEOUT_SMALL) {
					cancel();
					lastErrorCode = ERROR_CONNECTION;
					return true;
				}
			}
			return false;
		}
		DBG("XPlayer","Value for response on whttp.m_bError ="+whttp.m_bError);
		if (whttp.m_bError) return true;
		DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response);
		String tmpHR=whttp.m_response;
		

		String isMandatory = whttp.getResponseHeader("X-Mandatory");
		if(isMandatory != null && isMandatory.equals("1")) {
			update_is_mandatory = true;
		}

	#if AUTO_UPDATE_HEP
    		if (tmpHR.compareTo("-1")==0){
			DBG("XPlayer","Missing igpcode param.");
			return true;
		}else if (tmpHR.compareTo("-2")==0){
			DBG("XPlayer","Invalid igpcode param (must be GXXX)");
			return true;
		}else if (tmpHR.compareTo("-3")==0){
			DBG("XPlayer","Missing/invalid version param");
			return true;
		}else if (tmpHR.compareTo("-4")==0){
			DBG("XPlayer","Missing/invalid firmware version param");
			return true;
		}else if (tmpHR.compareTo("-5")==0){
			DBG("XPlayer","no igp info (IGP code not found in DB)");
			return true;
		}else if (tmpHR.compareTo("-6")==0){
			DBG("XPlayer","Unknown error -6");
			return true;
		}else if(tmpHR.compareTo("0")==0){
			DBG("XPlayer","No errors but no updates are available for the specified firmware");
			str_response = "Error: No live release";
			return true;
		}else if(tmpHR.contains("VERSION_AVAILABLE") && tmpHR.contains("DOWNLOAD_URL") )
		{
			DBG("XPlayer",tmpHR);
			str_response = tmpHR;
			return true;
		}else{
			DBG("XPlayer","Unknown error");
			return true;
		}
	#else
		if(tmpHR.contains("VERSION_AVAILABLE") && tmpHR.contains("DOWNLOAD_URL") )
		{
			str_response = tmpHR;
			return true;
		}
		else if(tmpHR.contains("Error: No live release"))
		{
			str_response = tmpHR;
			return true;
		}
		else if(tmpHR.contains("Error"))
		{
			return true;
		}
	
		
		// Should never hit this.
		lastErrorCode = ERROR_BAD_RESPONSE;
		return false;
	#endif
	}
#endif //#IF USE_INSTALLER	
#if USE_PSS
  public void sendRequestPSS_games_available(String URL, String validationQuery)
  {
    str_response  = null;
	DBG("XPlayer","sendRequestPSS_games_available ="+URL);
	whttp.cancel();
	lastErrorCode = ERROR_INIT;
	//if (ENABLE_TIMEOUT)
	{
		callstarttime = System.currentTimeMillis();
	}

	whttp.sendByGet(URL, validationQuery);
  }
  public boolean handleRequestPSS_games_available()
  {
    str_response = null;
	if (whttp.isInProgress()) {
		//if (ENABLE_TIMEOUT) 
		{
			if (System.currentTimeMillis() - callstarttime > 5000) {
				cancel();
				lastErrorCode = ERROR_CONNECTION;
				DBG("XPlayer", "handleRequestPSS_games_available timeout");
				return true;
			}
		}
		return false;
	}
	
    DBG("XPlayer","Value for response on whttp.m_bError ="+whttp.m_bError);
	if (whttp.m_bError) return true;
	DBG("XPlayer","Value for response on whttp.m_response ="+whttp.m_response+".");
	String tmpHR=whttp.m_response;
	str_response = tmpHR; 
    return true;  
  }
#endif	
	
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
	public static final int CONN_TIMEOUT = 60000;//20000; // 30 seconds
	/**
	 * The time in milliseconds after which a HTTP server request is timed
	 * out if a response is not received.
	 * <p>
	 * This is used only if <code>ENABLE_TIMEOUT</code> is set to
	 * <code>true</code>.
	 *
	 * @see #ENABLE_TIMEOUT
	 */
	public static final int CONN_TIMEOUT_SMALL = 15000;

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

	/** The server functions for HTTP communication. */ 
	private static final String SUCCESS_RESULT = "T7WxMl1MuYnllpIJnNJtoFD1ENkvNoVcrGXq7CvZ1Oo=";
	
	/** The server functions for HTTP communication. */ 
    private static final String FAILURE_RESULT = "RS4zxSt6TWQHptj38oDsSlD1ENkvNoVcrGXq7CvZ1Oo=";
    
#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	private static final String BILLING_SUCCESS 		= "X/UdzsidlhgyU1XvCAmFlFD1ENkvNoVcrGXq7CvZ1Oo=";
    private static final String BILLING_FAILURE 		= "wX4NLAtn5Jp0o/qCUQHvXVD1ENkvNoVcrGXq7CvZ1Oo=";
    private static final String BILLING_PENDING 		= "vmumwPhxuzoLGRoR4dvl9FD1ENkvNoVcrGXq7CvZ1Oo=";
    private static final String BILLING_SUCCESS_OFFLINE = "3JHvql5Lf+PtFSCKPZM28s1Z+25b0rLO39DqQFrYGtZQ9RDZLzaFXKxl6uwr2dTq";
#endif
    	
	/** The server functions for HTTP communication. */ 
	private static final String SUCCESS_MRC_RESULT = "TRkoKdbYEkMNHnxr3n5YFFD1ENkvNoVcrGXq7CvZ1Oo=";
	
	/** The server functions for HTTP communication. */ 
    public static final String TRUE_RESULT = "eBFyC3+q+/A2AYUKclS/w1D1ENkvNoVcrGXq7CvZ1Oo=";
	
	/** The server functions for HTTP communication. */ 
    public static final String FALSE_RESULT = "TzmUL0kURnAGxKcCxRCfrVD1ENkvNoVcrGXq7CvZ1Oo=";

	/** Offset to get the transaction result from the server response. */
	private static final byte TRANSACTION_RESULT = 0;
	
	/** Offset to get the transaction result error from the server response. */
	private static final byte TRANSACTION_RESULT_ERROR = 1;	
	
	/** Offset to get the transaction UNLOCK Code from Server*/
	private static final byte TRANSACTION_UNLOCK_CODE = 2;
	
	/** Offset to get the transaction UNLOCK Code from Server*/
	private static final byte FULL_VERSION_VALIDATION_TRANSACTION_UNLOCK_CODE = 1;
	
	/** Offset to get the transaction date from the server response. */
    private static final byte TRANSACTION_DATE = 1;
	
	private static final byte UMP_MO_1 = 2;
	private static final byte UMP_MO_2 = 3;
    
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
    
    public static final int NO_PRODUCT_ID_AND_PRICE     = 10002;
    
    public static final int PRICE_NOT_DEFINED_AT_ATT    = 10003;
    
    public static final int COULD_NOT_CONNECT_TO_ATT    = 10004;
    
    public static final int NO_RESPONSE_FROM_ATT        = 10005;
    
    public static final int NOT_SUBSCRIBED				= 10009;
    
    public static final int NO_USER_FOUND               = 40010;
    
    public static final int PARENTAL_CONTROL            = 40030;
    
    public static final int OVER_SPENDING               = 40040;
    
    public static final int BUCKET_EMPTY                = 40041;

    public static final int ALREADY_SUBSCRIBED          = 42020;
    
    public static final int NOT_SUPPORTED               = 63490;
    
    public static final int UNKNOWN_ERROR               = 99999;
    
    
  //Credit Card response codes							
    public static final int CC_LOGIN_SUCCESS_USER_EXIST_WITH_CREDIT_CARD 			= 000001;
    public static final int CC_LOGIN_SUCCESS_USER_EXIST_BUT_NO_CREDIT_CARD 			= 000002;
    public static final int CC_LOGIN_ERROR_UNKNOWN_FAILURE				 			= 000000;
    
  // Common Api Errors
    public static final int CC_ERROR_GENERIC										= 000000;//Generic Error
    public static final int CC_ERROR_LOGIN_INVALID_USERNAME_OR_PASSWORD				= 000001;//Login Failed / Account not found
    public static final int CC_ERROR_NO_CREDIT_CARD_ON_USER_PROFILE					= 000002;//No credit cards on user file
    public static final int CC_ERROR_INVALID_CREDIT_CARD_VALUE						= 000003;//CC Value Invalid
    public static final int CC_ERROR_INVALID_EXP_DATE								= 000004;//Expiracy Date Invalid
    public static final int CC_ERROR_INVALID_CCV									= 000005;//CCV Invalid
    
    public static final int CC_ERROR_USER_NOT_CREATED_EMAIL_ALREADY_REGISTERED		= 100001;//User not created: Email Already registered
    public static final int CC_ERROR_USER_NOT_CREATED_NICKNAME_UNAVAILABLE			= 100002;//User not created: Nickname unavailable
    public static final int CC_ERROR_USER_NOT_CREATED_INVALID_PASSWORD				= 100003;//User not created: Invalid Password 
    public static final int CC_ERROR_USER_NOT_CREATED_INVALID_EMAIL 				= 100004;//User not created: Invalid Email 
    public static final int CC_ERROR_USER_NOT_CREATED_INVALID_CARD_NUMBER			= 100005;//User not created: Invalid Card Number 
    public static final int CC_ERROR_USER_NOT_CREATED_CCV_MISSING_INVALID_EXP_DATE	= 100006;//User not created: Invalid Exp Date 
    public static final int CC_ERROR_USER_CREATED_INVALID_CARD_NUMBER		 		= 200005;//User created: Invalid Card Number 
    public static final int CC_ERROR_USER_CREATED_CCV_MISSING_INVALID_EXP_DATE		= 200006;//User created: Invalid Exp Date 

 // Paybox Side Errors
    public static final int CC_ERROR_PB_INVALID_CARD_HOLDER							= 100004;//PB100004    Invalid porteur (credit card)
    public static final int CC_ERROR_PB_INVALID_SITE								= 100006;//PB100006    Invalid site
    public static final int CC_ERROR_PB_INVALID_EXP_DATE							= 100008;//PB100008    Invalid expiration date
    public static final int CC_ERROR_PB_INVALID_OPERATION_TYPE						= 100009;//PB100009    Invalid operation type
    public static final int CC_ERROR_PB_INVALID_AMOUNT								= 100011;//PB100011    Invalid amount
    public static final int CC_ERROR_PB_INVALID_REFERENCE							= 100012;//PB100012    Invalid reference
    public static final int CC_ERROR_PB_INVALID_CCV									= 100020;//PB100020    Invalid CVV
     
 // Paybox Class Side Errors
    public static final int CC_ERROR_PBC_INVALID_USERNAME							= 900001;//PB900001    Invalid username
    public static final int CC_ERROR_PBC_INVALID_REFERENCE							= 900002;//PB900002    Invalid reference
    public static final int CC_ERROR_PBC_USER_NOT_EXIST								= 900003;//PB900003    GL user not present (in users table)
    public static final int CC_ERROR_PBC_USER_WITH_CC_ALREADY_REGISTERED			= 900004;//PB900004    GL user already has a paybox card registred
    public static final int CC_ERROR_PBC_USER_DONT_HAS_A_CC_REGISTERED				= 900005;//PB900005    GL user has NO paybox card registred (in users_plastic table)
    public static final int CC_ERROR_PBC_TOO_MANY_CARDS_FOR_USER					= 900021;//PB900021    Too many cards for the user
    public static final int CC_ERROR_PBC_INVALID_CARD_NUMBER						= 900022;//PB900022    No such card
    public static final int CC_ERROR_PBC_INVALID_PURCHASE_FORMAT					= 900023;//PB900023    Invalid purchase_id format
    public static final int CC_ERROR_PBC_PURCHASE_NOT_FOUND							= 900024;//PB900024    Purchase not found in gl database
    public static final int CC_ERROR_PBC_PURCHASE_IS_NOT_PAYBOX						= 900025;//PB900025    Purchase is not paybox
    public static final int CC_ERROR_PBC_PURCHASE_ALREADY_REFUNDED					= 900026;//PB900026    Already refunded
    public static final int CC_ERROR_PBC_INVALID_PARAMETER							= 900030;//PB900030    Invalid param
    public static final int CC_ERROR_PBC_TRANSACTION_NOT_ALLOWED					= 900157;//PB900157    Transaction not allowed (protection)
   
	public static final int CC_EMAIL_ALREADY_EXIST_WITH_CC							= 00001;//E00001 - 
	public static final int CC_EMAIL_ALREADY_EXIST_WITH_NO_CC						= 00002;//E00002 - 
	
	public static final int CC_ADDING_CARD_INVALID_PASSWORD							= 00001;//Invalid E-Mail and/or password.

	
#if (USE_IN_APP_BILLING && !USE_IN_APP_BILLING_CRM)
	public static final int IAB_SUCCES_NOTIFICATION								= 10003; //Success.
	public static final int IAB_SUCCES_OBB_FAILED								= 10010; //Notification to OBB failed. An email has been sent for manual handling.

	public static final int IAB_FAILURE											= 1; //00001 - Security check failed
	public static final int IAB_FAILURE_UNKNOWN									= 4; //00004 - Unknown request.
	public static final int IAB_FAILURE_OBB_NOTIFICATION_FAILED					= 5; //00005 - OBB Notification failed.
	public static final int IAB_FAILURE_GGI_MISSING								= 8; //00008 - GGI is missing from headers
	public static final int IAB_FAILURE_ACCOUNT_NUMBER_MISSING					= 9; //00009 - Account number is missing from headers (Use 0 on offline mode)
	public static final int IAB_FAILURE_INVALID_CONTENT							= 12; //00012 - OBB replied 'Invalid content'
	public static final int IAB_FAILURE_INVALID_ORDER_ID						= 13; //00013 - Google/Boku Checkout verification failed (Order ID is not valid).
	public static final int IAB_FAILURE_PENDING_ORDER_ID						= 14; //00013 - Boku this order is in pending state
	
#endif   
    
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

	public static HTTP getWHTTP()
	{
		return whttp;
	}
	
	/** The time the current request started - used for timeout implementation.
	 *  Use only in conjunction with ENABLE_TIMEOUT.*/
	public static long callstarttime;
	
	/** To manage all the error messages. */
    public static Error [] errorMessages = {
        //TO UPDATE
    	/*new Error (NO_USER_ID_IN_HEADER, R.string.BILLING_ERRORTEXT_NO_USER_ID_IN_HEADER),
        new Error (NO_PRODUCT_ID_AND_PRICE, R.string.BILLING_ERRORTEXT_NO_PRODUCT_ID),
        new Error (PRICE_NOT_DEFINED_AT_ATT, R.string.BILLING_ERRORTEXT_PRICE_NOT_DEFINED),
        new Error (COULD_NOT_CONNECT_TO_ATT, R.string.BILLING_ERRORTEXT_COULD_NOT_CONNECT),
        new Error (NO_RESPONSE_FROM_ATT, R.string.BILLING_ERRORTEXT_NO_RESPONSE),
        new Error (NOT_SUBSCRIBED, R.string.BILLING_ERRORTEXT_NOT_SUBSCRIBED),
        new Error (NO_USER_FOUND, R.string.BILLING_ERRORTEXT_USER_NOT_FOUND),
        new Error (PARENTAL_CONTROL, R.string.BILLING_ERRORTEXT_PARENTAL_CONTROL),
        new Error (OVER_SPENDING, R.string.BILLING_ERRORTEXT_OVER_SPENDING),
        new Error (BUCKET_EMPTY, R.string.BILLING_ERRORTEXT_BUCKET_EMPTY),
        new Error (ALREADY_SUBSCRIBED, R.string.BILLING_ERRORTEXT_ALREADY_SUBSCRIBED),
        new Error (UNKNOWN_ERROR, R.string.BILLING_ERRORTEXT_UNKNOWN),
        new Error (NOT_SUPPORTED, R.string.BILLING_ERRORTEXT_NOT_SUPPORTED),
        new Error (ERROR_NONE, R.string.BILLING_ERRORTEXT_NONE),
        new Error (ERROR_CONNECTION, R.string.BILLING_ERRORTEXT_COULD_NOT_CONNECT),
        new Error (ERROR_PENDING, R.string.BILLING_ERRORTEXT_PENDING),
        new Error (ERROR_NO_CLIENT_ID, R.string.BILLING_ERRORTEXT_NO_USER_ID_IN_HEADER),
        new Error (ERROR_BAD_RESPONSE, R.string.BILLING_ERRORTEXT_NO_RESPONSE),
        new Error (ERROR_INIT, R.string.BILLING_ERRORTEXT_INITIATING),*/
        };
	
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
}

#endif //#if USE_INSTALLER || USE_BILLING || USE_TRACKING_FEATURE_INSTALLER
