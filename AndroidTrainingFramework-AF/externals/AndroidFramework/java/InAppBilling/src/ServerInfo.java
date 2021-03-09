package APP_PACKAGE.iab;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import APP_PACKAGE.GLUtils.Carrier;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.R;

public class ServerInfo extends AServerInfo
{

	private IABXMLParser myXMLParser;

	SET_TAG("InAppBilling");
	
	/**
	 * Constructor
	 */
	public ServerInfo () 
	{
		try {
			/** Handling XML */
			spf = SAXParserFactory.newInstance();
			sp = spf.newSAXParser();
			xr = sp.getXMLReader();
			
			myXMLParser = new IABXMLParser();
			xr.setContentHandler(myXMLParser);
			
			mDevice = new Device();
			mXPlayer = new XPlayer(mDevice);
		} catch (Exception e) {
			ERR(TAG,"XML Server Parsing Exception = " + e);
		}
	}
	private String mCurrentSelectedBilling = null;
	private String mCurrentSelectedItem = null;
	private String mCurrentNounce = null;
	
	private ShopProfile mShopProfile = null;
	

	/**
	 * @return the mUnlockProfile
	 */
	public ShopProfile getShopProfile() {
		return mShopProfile;	}

	/**
	 * @param ShopProfile the ShopProfile to set
	 */
	public void setShopProfile(ShopProfile shopProfile) {
		this.mShopProfile = shopProfile;
	}
	
	/**
	 * @return the url for the Content Feed
	 */
	public String getFeedURL()
	{
		String url = 
		#if GOOGLE_STORE_V3	
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_GOOGLE); //"https://secure.gameloft.com/freemium/fm_profiles.php";
		#elif VZW_STORE
			#if VZW_MTX
				InAppBilling.GET_STR_CONST(IAB_URL_PFL_VERIZON_MTX); //"https://secure.gameloft.com/freemium/fm_profiles_vzw_microtx.php";
			#else
				InAppBilling.GET_STR_CONST(IAB_URL_PFL_VERIZON); //"https://secure.gameloft.com/freemium/fm_profiles_vzw_scm.php";
			#endif
		#elif SAMSUNG_STORE
				InAppBilling.GET_STR_CONST(IAB_URL_PFL_SAMSUNG); //"http://iap.gameloft.com/freemium/fm_profiles_samsung.php";
		#elif BOKU_STORE || GAMELOFT_SHOP
			#if OPTUS_STORE
				InAppBilling.GET_STR_CONST(IAB_URL_PFL_OTPUS); //"https://secure.gameloft.com/freemium/fm_profiles_optus.php";
			#else
				InAppBilling.GET_STR_CONST(IAB_URL_PFL_GLSHOP); //"https://secure.gameloft.com/freemium/fm_profiles_shop.php";
			#endif
		#elif SKT_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_SKT); //"https://secure.gameloft.com/freemium/fm_profiles_skt.php";
		#elif KT_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_KT); //"https://secure.gameloft.com/freemium/fm_profiles_kt.php";
		#elif AMAZON_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_AMAZON); //"https://secure.gameloft.com/freemium/fm_profiles_amazon.php";
		#elif PANTECH_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_PANTECH); //"https://secure.gameloft.com/freemium/fm_profiles_pantech.php";
		#elif BAZAAR_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_BAZAAR); //"https://iap.gameloft.com/freemium/fm_profiles_bazaar.php";
		#elif VXINYOU_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_VXINYOU); //"https://secure.gameloft.com/freemium/fm_profiles_xinyou.php";
		#elif YANDEX_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_YANDEX); //"https://secure.gameloft.com/freemium/fm_profiles_yandex.php";
		#elif HUAWEI_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_HUAWEI); //" https://secure.gameloft.com/freemium/fm_profiles_huaweitv.php";		
		#elif ZTE_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_ZTE); //"https://secure.gameloft.com/freemium/fm_profiles_zte.php";
		#elif ATET_STORE
			InAppBilling.GET_STR_CONST(IAB_URL_PFL_ATET); //""https://secure.gameloft.com/freemium/fm_profiles_atet.php"";
        #else
            #error No shop selected. Please define a shop and set it to 1 (e.g.: GOOGLE_STORE_V3, SAMSUNG_STORE, GAMELOFT_SHOP, etc).
		#endif //Else option doesn't exists, at least one of the previous listed before must be active if (USE_IN_APP_BILLING==1)
		
		return url;
	}
	/**
	 * @return the query to sent to eCom
	 */
	public String getDeviceQuery(Device device)
	{
		Locale locale = Locale.getDefault();
		StringBuffer sb = new StringBuffer();
		sb.append("game="+device.ValidateStringforURL(device.getDemoCode()));
		sb.append("&network_country_ISO="+device.ValidateStringforURL(device.getNetworkCountryIso()));
		sb.append("&network_operator="+device.ValidateStringforURL(device.getNetworkOperator()));
		sb.append("&network_operator_name="+device.getNetworkOperatorName());
		sb.append("&sim_country_iso="+device.ValidateStringforURL(device.getSimCountryIso()));
		sb.append("&sim_operator="+device.ValidateStringforURL(device.getSimOperator()));
		sb.append("&sim_operator_name="+device.getSimOperatorName());
		sb.append("&line_number="+device.ValidateStringforURL(device.getLineNumber()));
		sb.append("&is_network_roaming="+device.getIsRoaming());
		sb.append("&android_build_device="+device.getDevice());
		sb.append("&android_build_model="+device.getPhoneModel());
		sb.append("&supports_sms=1");
		sb.append("&supports_wapother=1");
	#if !USE_PHD_PSMS_BILL_FLOW
		sb.append("&supportswap=1");
		sb.append("&supports_wappp=1");
	#endif
		sb.append("&game_version="+GAME_VERSION_NAME);
		sb.append("&lang="+device.ValidateStringforURL(locale.getLanguage().toLowerCase()));
		sb.append("&locale="+device.ValidateStringforURL(locale.toString().toLowerCase()));
		//sb.append("&testprofile=1");
	#if GAMELOFT_SHOP
		sb.append("&d="+SUtils.GetSerialKey());//Serial Key Parameter
	#endif
	#if SHENZHOUFU_STORE
		sb.append("&supports_shenzhoufu=1");
	#endif
	#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
		sb.append("&supports_psms=1");
	#endif
	#if USE_UMP_R3_BILLING
		sb.append("&supports_ump=1");
		#if (HDIDFV_UPDATE != 2)	
			sb.append("&imei="+device.getIMEI());
		#endif	
	#endif
	#if USE_PHD_PSMS_BILL_FLOW || SAMSUNG_STORE
		sb.append("&v=1.1");
	#endif

		return sb.toString();
	}
	/**
	 * @return the a JSON String representation for the headers to sent to eCom
	 */
	public String getJSONHeaders(Device device)
	{
		JSONObject jObj = new JSONObject();
		try
		{
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GGI),GGI);//"x-up-gl-ggi"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GAMECODE),Device.getDemoCode());//"x-up-gl-gamecode"
			
			String value = InAppBilling.GET_GGLIVE_UID(); //"x-up-gl-acnum"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_ACNUM),value);
			
			value = InAppBilling.GET_CREDENTIALS(); //"x-up-gl-fed-credentials"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CREDS),value);
			
			value = GET_CLIENT_ID(); //"x-up-gl-fed-client-id"
			if (TextUtils.isEmpty(value)) value = "0";
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_CLIENT_ID),value);
		#if (HDIDFV_UPDATE != 2)	
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_IMEI),device.getIMEI());//"x-up-gl-imei"
		#endif
		#if HDIDFV_UPDATE
			DBG("I_S"+HDIDFV_UPDATE, device.getHDIDFV());
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_HDIDFV),device.getHDIDFV());//"x-up-gl-hdidfv"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_GLDID),device.getGLDID());//"x-up-gl-gldid"
		#endif
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_USR_AGENT),device.getUserAgent());//"User-Agent"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_BUILD_MDL),android.os.Build.MODEL);//"x-android-os-build-model"
			

			mCurrentNounce = InAppBilling.GET_NEW_NOUNCE();
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_APP),InAppBilling.GET_APP_HDER());//"X-App"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_APP_VERSION),InAppBilling.GET_VERSION_HDER());//"X-App-Version"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_APP_PRODUCT_ID),InAppBilling.GET_PRODUCT_ID_HDER());//"X-App-Product-Id"
			jObj.put(InAppBilling.GET_STR_CONST(IAB_HEADER_APP_NOUNCE), mCurrentNounce);//"X-App-Nounce"
			
			return jObj.toString();
		} catch (JSONException e) 
		{
			ERR(TAG, e.getMessage());
			DBG_EXCEPTION(e);
		}
		return null;
	}
	
	/**
	 * Used to read the updated profile info from the server, and it calls the callback function if is not null.
	 */
	public void getUpdateProfileInfo (final IABCallBack cb)
	{
		Device device	= new Device();
		String url		= getFeedURL();
		String qry		= getDeviceQuery(device);
		String headers	= getJSONHeaders(device);
		
		LOGGING_APPEND_REQUEST_PARAM(url, "GET", "Content Feed request");
		LOGGING_APPEND_REQUEST_PAYLOAD(qry, "Content Feed request");
		LOGGING_LOG_REQUEST(IAP_LOG_TYPE_INFO, LOGGING_REQUEST_GET_INFO("Content Feed request"));
		IABRequestHandler reqHandler = IABRequestHandler.getInstance();
		final String nounce = mCurrentNounce;
		reqHandler.getInstance().doRequestByGet(url, qry, headers, new IABCallBack()
		{
			public void runCallBack(Bundle bdle)
			{
				DBG(TAG, "[Content Feed request] Callback");
				int response = bdle.getInt(IABRequestHandler.KEY_REQUEST_RESULT);
				if (response == IABRequestHandler.SUCCESS_RESULT)
				{
					String result 	= bdle.getString(IABRequestHandler.KEY_RESPONSE);
					String sheaders = bdle.getString(IABRequestHandler.KEY_HEADERS);
					String hash 	= null;
					try
					{
						JSONObject jObj = new JSONObject(sheaders);
						hash = jObj.getString("X-InApp-Hash");
						JDUMP(TAG, hash);
					} catch (JSONException e) {}

					LOGGING_APPEND_RESPONSE_PARAM(result, "Content Feed request");
					LOGGING_LOG_RESPONSE(IAP_LOG_TYPE_INFO, LOGGING_RESPONSE_GET_INFO("Content Feed request"));
					LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO, "Content Feed request: "+ LOGGING_REQUEST_GET_TIME_ELAPSED("Content Feed request") +" seconds" );
					LOGGING_REQUEST_REMOVE_REQUEST_INFO("Content Feed request");
					mCurrentSelectedBilling = null;					
					
					//Checking MD5 Hash code begin
					Bundle bundle = new Bundle();
					bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_CHECK_MD5_HASH);
					byte[] emptyarray = new String().getBytes();
					JDUMP(TAG, result.length());
					bundle.putByteArray("data",(result!=null)?result.getBytes():emptyarray);
					bundle.putByteArray("nounce",(nounce!=null)?nounce.getBytes():emptyarray);
					bundle.putByteArray("hash",(hash!=null)?hash.getBytes():emptyarray);
					int testResult = -1;
					try{
						Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
						Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
						bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
						testResult = bundle.getInt("R");
						JDUMP(TAG, testResult);
					}
					catch(Exception ex )
					{
						ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
					}
					//Checking MD5 Hash code end
					
					if (!TextUtils.isEmpty(result) && testResult == 0)
					{
						InputSource is = new InputSource(new ByteArrayInputStream(result.getBytes()));
						parseXML(is);
					}	
				}
				else
				{
					int code = bdle.getInt(IABRequestHandler.KEY_HTTP_RESPONSE);
					LOGGING_LOG_INFO(IAP_LOG_TYPE_ERROR, IAP_LOG_STATUS_ERROR, "Waiting for Content Feed request failed with error code: "+code );
				}
				cb.runCallBack(null);
			}
		});
	}
	
	/**
	 * Used to read the updated profile info from the server <BR>This function waits until get a response from the server.
	 */
	public boolean getUpdateProfileInfo ()
	{
		String url = getFeedURL();
		mXPlayer.sendIABProfileRequest(url);
		
		long time = 0;
		while (!mXPlayer.handleIABProfileRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
			
			if(System.currentTimeMillis() - time > 1500)
			{
				DBG(TAG, "[sendProfileRequest]Waiting for response");
				time = System.currentTimeMillis();
			}
				
		} 
		if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{		
			mCurrentSelectedBilling = null;
			InputSource is = new InputSource(new ByteArrayInputStream(XPlayer.getWHTTP().m_response.getBytes()));
			parseXML(is);
			return true;
		}
		return false;
	}
	/**
	 * Used to read the profiles from the InputSource given.
	 * @param InputSource the current InputSource to use
	 */
	private void parseXML (InputSource isrc)
	{
		try {
			xr.parse(isrc);
			mShopProfile = myXMLParser.getShopProfile();
		#if !RELEASE_VERSION	
			String responseA[] = mShopProfile.toString().split("\n");//DDMS cuts large strings 
			for (int i = 0; i<responseA.length; i ++) {
				INFO(TAG,responseA[i]);
			}
		#endif
		} catch (Exception e) {
			ERR("InAppBilling","parseXML Parsing Exception = " + e);
		}
	}
	/**
	 * @return true if the billing default is found to the itemId requested.
	 * @info use after <code>getUpdateProfileInfo</code>, if not found.
	 * @param itemId the item Id to set as the default item.
	 * @see #getUpdateProfileInfo()
	 */
	@Override
	public boolean searchForDefaultBillingMethod (String itemId)
	{
		if (mShopProfile != null && itemId != null)
		{
			mCurrentSelectedItem = itemId;
			cItem item = mShopProfile.getItemById(itemId);
			if (item != null)
			{
				mCurrentSelectedBilling = item.getType_pref();
				if (mCurrentSelectedBilling.length() > 0)
					return true;
			}
		}
		return false;
	}

	/**
	 * @return the value of the attribute to search or null if not found.
	 * @param itemId the item Id to search for, attribute the attribute name to search for
	 * @see #getUpdateProfileInfo()
	 */
	@Override
	public String getItemAttribute (String itemId, String attribute)
	{
		if (mShopProfile != null && itemId != null)
		{
			cItem item = mShopProfile.getItemById(itemId);
			if (item != null)
				return  item.getAttributeByName(attribute);
		}
		return (null);
	}
	
	/**
	 * @return the value of the attribute to search or null if not found.
	 * @param itemId the item Id to search for, attribute the attribute name to search for
	 * @see #getUpdateProfileInfo()
	 */
	@Override 
	public String getBillingAttribute (String itemId, String attribute)
	{
		if (mShopProfile != null && itemId != null)
		{
			cBilling billing = mShopProfile.getItemById(itemId).getDefaultBilling();
			if (billing != null)
				return  billing.getAttributeByName(attribute);
		}
		return (null);
	}
	
	/**
	 * @return the type of the item, or null if not found.
	 * @param itemId the item Id to search for
	 * @see #getUpdateProfileInfo()
	 */
	public String getItemType (String itemId)
	{
		if (mShopProfile != null && itemId != null)
		{
			return mShopProfile.getItemById(itemId).getType();
		}
		return (null);
	}
	
	/**
	 * @return true if a selected Item is defined
	 * @info use after <code>getUpdateProfileInfo</code> and <code>searchForDefaultBillingMethod</code>.
	 * @see #getUpdateProfileInfo(), searchForDefaultBillingMethod()
	 */
	private boolean isAItemSelected()
	{
		if (mCurrentSelectedItem == null)
		{
			ERR(TAG, "The selected item is not defined, use searchForDefaultBillingMethod");
		}
		else 
			return true;
		
		return false;
	}	
	/**
	 * @return true if a Billing Method is defined
	 * @info use after <code>getUpdateProfileInfo</code> and <code>searchForDefaultBillingMethod</code> and/or <code>setBillingMethod</code>.
	 * @see #getUpdateProfileInfo(), searchForDefaultBillingMethod(), setBillingMethod()
	 */
	private boolean isABillingMethodSelected()
	{
		if (mCurrentSelectedBilling == null)
		{
			ERR(TAG, "The selected billing method is not defined, use searchForDefaultBillingMethod/setBillingMethod");
		}
		else if (isAItemSelected())
			return true;
		
		return false;
	}
	
	/**
	 * @return true if the profile is found using the billingType String parameter, and set mCurrentSelectedBilling with the found profile name.
	 * @info use after <code>getUpdateProfileInfo</code>, if not found set mCurrentSelectedBilling with null.
	 * @see #getUpdateProfileInfo()
	 * @param profileNeeded
	 */
	@Override
	public boolean setBillingMethod(String billingType)
	{
		mCurrentSelectedBilling = null;
		if (isAItemSelected() && billingType !=  null)
		{
			if (getBillingByType(mCurrentSelectedItem, billingType) != null)
			{
				mCurrentSelectedBilling = billingType;
				return true;
			}
		}
		return false;
	}
	

	/**
	 * @return the current selected item id
	 * @info You must call <code>getUpdateProfileInfo</code> and <code>searchForDefaultBillingMethod</code> and/or <code>setBillingMethod</code> before using this method.
	 * @see #getUpdateProfileInfo(), searchForDefaultBillingMethod()
	 */
	@Override
	public String getSelectedItem ()
	{	
		return mCurrentSelectedItem;
	}
	/**
	 * @return the item price if mCurrentSelectedBilling != null
	 * @info You must call <code>getUpdateProfileInfo</code> and <code>searchForDefaultBillingMethod</code> and/or <code>setBillingMethod</code> before using this method.
	 * @see #getUpdateProfileInfo(), searchForDefaultBillingMethod(), setBillingMethod()
	 */
	//public String getItemPrice ()
	public String getGamePrice () //it will be rename to getItemPrice
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PRICE));
			}
		}
		return null;
	}
	/**
	 * @return the game price + Symbol if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 */
	@Override
	public String getItemPriceFmt ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
        		java.util.Currency japan = java.util.Currency.getInstance(java.util.Locale.JAPAN);
        		
				if (billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_CURRENCY)).equals(japan.getCurrencyCode()))
					return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_CURRENCY))+" "+billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PRICE));
				else
          			return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PRICE))+" "+billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_CURRENCY));
			}
		}
		return null;
	}
	/**
	 * @return the game price + Symbol if mCurrentSelectedBilling != null or null if not, this is the decoded symbol to ascii chars.
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 */
	public String getMenuGamePriceFmt ()
	{
		String price_string = getItemPriceFmt();
		if (price_string != null)
			return StringCurrencytoChar(price_string);
		
		return null;
	}
	
	/**
	 * @return the language value if exist and if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 */
	public String getLanguage ()
	{
		if (mShopProfile != null)
			return (mShopProfile.getLangValue());
			
		return null;
	}
	/**
	 * @return the tnc string if exist and if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 */
	public String getTNCString ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_TNC));
			}
		}
		return null;
	}
	
	/**
	 * @return the flow_type string if exist and if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 */
	public String getFlowType ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PSMS_FLOW_TYPE));
			}
		}
		return null;
	}
	
	/**
	 * @return the type string if exist and if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 */
	public String getProfileType ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName("type");
			}
		}
		return null;
	}

	/**
	 * @return the country id
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getCountryId ()
	{
		if (mShopProfile == null)
			return null;
			
		return 	mShopProfile.getCountryId();
	}

	/**
	 * @return the operator id
	 * @info You must call code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getOperatorId ()
	{
		if (mShopProfile == null)
			return null;
			
		return 	mShopProfile.getOperatorId();
	}

	/**
	 * @return the platformId
	 * @info You must call code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getPlatformId ()
	{
		if (mShopProfile == null)
			return null;
		return mShopProfile.getPlatformId();
	}
	
		/**
	 * @return the langId
	 * @info You must call code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override  
	public String getLangId ()
	{
		if (mShopProfile == null)
			return null;
		return mShopProfile.getLangId();
	}
	
	/**
	 * @return the currency Value if mCurrentSelectedBilling != null or null if not.
	 * @info use after <code>getUpdateProfileInfo</code> and <code>searchForDefaultBillingMethod</code> and/or <code>setBillingMethod</code>.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override 
	public String getStringCurrencyValue ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_CURRENCY));
			}
		}
		return null;
	}

	/**
	 * @return the url billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getURLbilling ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_URL));
			}
		}
		return null;
	}	
	#if SHENZHOUFU_STORE
	/**
	 * @return the url billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override 
	public String getShenzhoufuURLbilling ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_SHENZHOUFU_URL));
			}
		}
		return null;
	}	
	
	/**
	 * @return the url billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override 
	public String getShenzhoufuURLresult ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_SHENZHOUFU_RESULT_URL));
			}
		}
		return null;
	}
	#endif
	/**
	 * @return the Money billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override
	public String getMoneyBilling()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_MONEY));
			}
		}
		return null;
	}	
	
	/**
	 * @return the is3GOlnly value if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override
	public String getIs3GOnly()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_IS_3G_ONLY));
			}
		}
		return null;
	}
	
	/**
	 * @return the server Alias value.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getAlias()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_ALIAS));
			}
		}
		return null;
	}
	
	
	/**
	 * @return the server ServerNumber value.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getServerNumber()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_SHORTCODE));
			}
		}
		return null;
	}


	public String getShortCode(int target)
	{
		String strtarget = InAppBilling.GET_STR_CONST(IAB_SHORTCODE)+target;
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(strtarget);
			}
		}
		return null;
	}	
	
	
	public String getUMPTIdURL()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_UMP_TID_URL));
			}
		}
		return null;
	}

	public String getUMPBillingURL()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_UMP_BILLING_URL));
			}
		}
		return null;
	}		

	
	/**
	 * @return the server ProfileId value.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getProfileId()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_BILLING_PROFILE_ID));
			}
		}
		return null;
	}
	
	/**
	 * @return the server PSMSType value.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override 
	public String getPSMSType()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PSMS_TYPE));
			}
		}
		return null;
	}

	/**
	 * @return the aid value for skt billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override
	public String getAIDBilling()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_AID));
			}
		}
		return null;
	}	
	
	/**
	 * @return the pid value for skt billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override 
	public String getPIDBilling()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PID));
			}
		}
		return null;
	}

	/**
	 * @return the iid value for pantech billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override 
	public String getIIDBilling()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_IID));
			}
		}
		return null;
	}
	
	/**
	 * @return the UID billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override
	public String getItemUID ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_UID));
			}
		}
		return null;
	}
	/**
	 * @return the ServiceID (Used in boku) billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	public String getItemServiceID ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_SERVICE_ID));
			}
		}
		return null;
	}	
	/**
	 * @return the PriceIncludedTaxes(Used in boku) billing if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */
	@Override
	public String getItemPriceWithTaxes ()
	{
		if (isABillingMethodSelected())
		{
			cBilling billing = getCurrentBilling();
			if (billing != null) //must not but...
			{
				return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PRICE_INC_TAXES));
			}
		}
		return null;
	}
	/**
	 * @return the billing type if mCurrentSelectedBilling != null or null if not.
	 * @info You must call <code>searchForDefaultBillingMethod</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForDefaultBillingMethod(String)
	 */

	public String getBillingType ()
	{
		return mCurrentSelectedBilling;
	}
	
	/**
	 * @return true if mCurrentSelectedBilling != null, which means a profile is currently selected
	 * @info You must call <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 */
	public boolean isProfileSelected()
	{
		return (isABillingMethodSelected());
	}
	
	
	/**
	 * This Method allows to convert the Special Currency Chars to a HTML Code
	 * @return The original string but formated with the replaced codes
	 * @param String value this is the string to evaluate
	 */
	public String StringCurrencytoChar(String value)
	{
		try
		{
		if (value.contains("€"))
		{
			value = value.replaceAll("€", "&#8364");
		}
		if (value.contains("£"))
		{
			value = value.replaceAll("£", "&#163");
		}
		if (value.contains("$"))
		{
			value = value.replaceAll("$", "&#36");
		}
		}catch(Exception ex)
		{
			
		}
		
		return (value);
	}
	
	/**
	 * @return the numer of items found in the profile using the list String parameter, and return 0 if found no items for the list
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param list
	*/
	public int getTotalItemsByList (String list)
	{
		if (mShopProfile == null)
			return 0;
		//DBG(TAG, mShopProfile.toString());
		ArrayList<cItem> itemList = mShopProfile.getItemList(list);
		//DBG(TAG, "getTotalItemsByList "+ (itemList == null?itemList:itemList.size()));
		if (itemList != null)
		{
			DBG(TAG, "getTotalItemsByList "+ itemList.size());
			return itemList.size();
		}
		return 0;
	}
	
	/**
	 * @return the total numer of items found in the profile, return 0 if found no items 
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param list
	*/
	public int getTotalItems ()
	{
		if (mShopProfile == null)
			return 0;
		return mShopProfile.getTotalItems();
	}
	
	/**
	 * @return the item's bytes of an attribute value using the list String parameter, name of the attribute 
	 * to search and the idx of the item ,return null if found no items for the list or neither the item attribute name
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param list, name, idx
	*/
	public byte[] getAttributeByNameIdx (String list, String name, int idx)
	{
		if (mShopProfile == null)
			return null;
		
		ArrayList<cItem> itemList = mShopProfile.getItemList(list);
		if (itemList != null && idx >= 0 && idx < itemList.size())
		{
			
			String value = itemList.get(idx).getAttributeByName(name);
			if (value == null)
				DBG (TAG, "getAttributeByNameIdx value not found "+name);
			//else 
				//DBG (TAG, "item ID "+idx +" value\'"+value+"\'\n"+itemList.get(idx).toString());
			if (value != null)
				return value.getBytes();
		}
		return null;
	}

	/**
	 * @return the item's bytes of an billing attribute value using the list String parameter, name of the attribute 
	 * to search and the idx of the item ,return null if found no items for the list or neither the item attribute name
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param list, name, idx
	*/
	public byte[] getBillingAttByNameIdx (String list, String name, int idx)
	{
		if (mShopProfile == null)
			return null;
		ArrayList<cItem> itemList = mShopProfile.getItemList(list);
		if (itemList != null && idx >= 0 && idx < itemList.size())
		{
			String value = null;
			cBilling billing = itemList.get(idx).getDefaultBilling();
			if (billing != null)
			{
				value = billing.getAttributeByName(name);
				if (value == null)
				{
					DBG (TAG, "getAttributeByNameIdx value not found "+name);
				}
				if (value != null)
					return value.getBytes();
			}
		}
		return null;
	}	
	
	/**
	 * @replace the item's value of an attribute using the list String parameter, name of the attribute 
	 * to search and the idx of the item ,return null if found no items for the list or neither the item attribute name
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param list, name, idx
	*/
	
	public void replaceAttributeByNameIdx (String list, String name, String newvalue, int idx)
	{
		if (mShopProfile == null)
			return;
		
		ArrayList<cItem> itemList = mShopProfile.getItemList(list);
		if (itemList != null && idx >= 0 && idx < itemList.size())
		{
			String id = itemList.get(idx).getId();
			itemList.get(idx).addAttribute(name, newvalue);
			if (id != null)
				mShopProfile.replaceItemAtributeValue(id, name, newvalue);
		}
	}


	public void replaceBillingAttributeByNameIdx (String list, String billingType, String name, String newValue, int idx)
	{
		if (mShopProfile == null)
			return;

		String paramList = (list==null)?"":list;
		
		ArrayList<cItem> itemList = mShopProfile.getItemList(paramList);
		if (itemList != null && idx >= 0 && idx < itemList.size())
		{
			cBilling billing = null;
			if(billingType=="")
				billing = itemList.get(idx).getDefaultBilling();
			else 
				billing = itemList.get(idx).getBillingByType(billingType);

			if(billing != null)
				billing.addAttribute(name, newValue);
		}
	}

	
	/**
	 * @return the item's bytes of its ID value using the list String parameter, 
	 * and the idx of the item ,return null if found no items for the list or neither the item idx
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param list, name, idx
	*/
	public byte[] getItemIdByIdx (String list, int idx)
	{
		if (mShopProfile == null)
			return null;
		ArrayList<cItem> itemList = mShopProfile.getItemList(list);
		if (itemList != null && idx >= 0 && idx < itemList.size())
		{	
			String value = itemList.get(idx).getId();
			if (value == null)
				DBG (TAG, "getItemIdByIdx value not found "+idx);
			//else 
				//DBG (TAG, "item ID "+idx +" valueID\'"+value+"\'\n"+itemList.get(idx).toString());
			if (value != null)
				return value.getBytes();
		}
		return null;
	}

	/**
	 * @return the item's bytes of its Type value using the list String parameter, 
	 * and the idx of the item ,return null if found no items for the list or neither the item idx
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param list, name, idx
	*/
	public byte[] getItemTypeByIdx (String list, int idx)
	{
		if (mShopProfile == null)
			return null;
		
		ArrayList<cItem> itemList = null; 
		itemList =  mShopProfile.getItemList(list);
			
		if (itemList != null && idx >= 0 && idx < itemList.size())
		{	
			String value = itemList.get(idx).getType();
			if (value == null)
				DBG (TAG, "getItemTypeByIdx value not found "+idx);
			if (value != null)
				return value.getBytes();
		}
		return null;
	}	
	
	/**
	 * @return the cItem instance if exist the item's uid
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param uid
	*/
	public cItem getItemById(String id)
	{
		if (mShopProfile == null || id == null)
			return null;
		return (mShopProfile.getItemById(id));
	}
	
	/**
	 * @Return the String ItemId given the uid(sample uid = "com.gameloft.mmorpg.1cash")
	 * @param value the uid value
	 */
	public String getItemIdByUID(String uid)
	{
		if (mShopProfile == null || uid == null)
			return null;
		return mShopProfile.getItemIdByUID(uid);

	}
	
	/**
	 * @Return the String ItemId given the uid(sample uid = "com.gameloft.mmorpg.1cash")
	 * @param value the uid value
	 */
	public String getItemIdByPID(String pid)
	{
		if (mShopProfile == null || pid == null)
			return null;
		return mShopProfile.getItemIdByPID(pid);

	}
	
	/**
	 * @return the cBilling instance, if exist the item's uid and the billing type for the given item
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param id
	*/
	public cBilling getBillingByType(String id, String type)
	{
		if (mShopProfile == null || id == null)
			return null;
			
		cItem item = mShopProfile.getItemById(id);
		if (item != null)
			return (item.getBillingByType(type));
		
		return null;
	}

	/**
	 * @return the cBilling instance, of the current selected billing method and item
	 * @info use after <code>getUpdateProfileInfo</code>, 
	 * @see #getUpdateProfileInfo()
	 * @param uid
	*/
	public cBilling getCurrentBilling()
	{
		return getBillingByType(mCurrentSelectedItem, mCurrentSelectedBilling);
	}
	
	/**
	 * @Return the String byte array for the Promo Description
	 */
	public byte[] getPromoDescription()
	{
		if (mShopProfile == null)
			return null;
		return mShopProfile.getPromoDescription().getBytes();

	}
	
	/**
	 * @Return the String byte array for the Promo EndTime
	 */
	public byte[] getPromoEndTime()
	{
		if (mShopProfile == null)
			return null;
		return mShopProfile.getPromoEndTime().getBytes();

	}
	
	/**
	 * @Return the String byte array for the Promo Description
	 */
	public byte[] getPromoServerTime()
	{
		if (mShopProfile == null)
			return null;
		return mShopProfile.getPromoServerTime().getBytes();

	}
	
	public String getPurchaseConfirmString()
	{
		cBilling billing = getCurrentBilling();
		if (billing != null)
		{
			return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PSMS_PURCHASE_TXT_CONFIRM));
		}
		
		return null;
	}
	
	public String getPurchaseOkString()
	{
		cBilling billing = getCurrentBilling();
		if (billing != null)
		{
			return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PSMS_PURCHASE_BTN_OK));
		}
		
		return null;
	}
	
	public String getPurchaseLaterString()
	{
		cBilling billing = getCurrentBilling();
		if (billing != null) 
		{
			return billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PSMS_PURCHASE_BTN_LATER));
		}
		
		return null;
	}
	public String getTNC_Title()
	{

		if (mShopProfile == null)
			return null;
		return mShopProfile.getTNCTitle();
	}
	public String getTNC_Title_Shop()
	{
		if (mShopProfile == null)
			return null;
		return mShopProfile.getTNCTitleShop();
	}

}