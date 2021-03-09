package APP_PACKAGE.billing;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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

	private MyXMLParser myXMLParser;

	SET_TAG("Billing");
	
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
			
			myXMLParser = new MyXMLParser();
			xr.setContentHandler(myXMLParser);
			
			mDevice = new Device();
			mXPlayer = new XPlayer(mDevice);
		} catch (Exception e) {
			ERR("Billing","XML Server Parsing Exception = " + e);
		}
			}
	private int mCurrentSelectedProfile = -1;
	private UnlockProfile mUnlockProfile = null;

	/**
	 * @return the mUnlockProfile
	 */
	public UnlockProfile getUnlockProfile() {
		return mUnlockProfile;	}

	/**
	 * @param mUnlockProfile the mUnlockProfile to set
	 */
	public void setUnlockProfile(UnlockProfile unlockProfile) {
		this.mUnlockProfile = unlockProfile;
	}
	
	/**
	 * Used to read the updated profile info from the server <BR>This function waits until get a response from the server.
	 * @see #getOfflineProfilesInfo(Context)
	 */
	public boolean getUpdateProfileInfo ()
	{
		mXPlayer.sendProfileRequest();
		while (!mXPlayer.handleProfileRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
			DBG("Billing", "[sendProfileRequest]Waiting for response");
		} 
		if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{		
			mCurrentSelectedProfile = -1;
			InputSource is = new InputSource(new ByteArrayInputStream(XPlayer.getWHTTP().m_response.getBytes()));
			parseXML(is);
		#if USE_BILLING_FOR_CHINA
			if(mUnlockProfile.getBilling_type().equals("IPX"))
			{
				// set paid money for ipx
				Profile ipxProfile = getProfileByName("IPX");
				int ipxProfileIndex = mUnlockProfile.getProfileList().indexOf(ipxProfile);
				int paidMoney = SUtils.getPreferenceInt(SUtils.getLManager().PREFERENCES_SMS_IPX_MONEY, 0, SUtils.getLManager().PREFERENCES_NAME);
				mUnlockProfile.getProfileList().get(ipxProfileIndex).setIPXPaidMoney(paidMoney);
				getIPXLookup();
			}
		#endif
			searchForDefaultProfile();
			return true;
		}
		return false;
	}
	/**
	 * Used to read the offline profiles info when you don't have Internet connection
	 * @see #getUpdateProfileInfo()
	 * @see #searchForProfile()
	 * @see #searchForProfile(String, String, String)
	 */
	public void getOfflineProfilesInfo (Context context)
	{
		try {
			/** Handling XML */
			parseXML(new InputSource(context.getResources().openRawResource(R.raw.hdprofiles)));
			searchForProfile();
		} catch (Exception e) {
			ERR("Billing","parseXMLFileOffline Parsing Exception = " + e);
		}
	}
	/**
	 * Used to read the SMS offline profiles info for a specific carrier
	 * @see #getUpdateProfileInfo()
	 * @see #searchForProfile()
	 * @see #searchForProfile(String, String, String)
	 */
	/*public void getSMSOfflineProfilesInfo (Context context)
	{
		try {
			parseXML(new InputSource(context.getResources().openRawResource(R.raw.carriersms)));
			mCurrentSelectedProfile = 0;
		} catch (Exception e) {
			ERR("Billing","parseXMLFileOffline Parsing Exception = " + e);
		}
	}*/
	/**
	 * Used to read the HTTP offline profiles info for a specific carrier
	 * @see #getUpdateProfileInfo()
	 * @see #searchForProfile()
	 * @see #searchForProfile(String, String, String)
	 */
	/*public void getHTTPOfflineProfilesInfo (Context context)
	{
		try {
			parseXML(new InputSource(context.getResources().openRawResource(R.raw.carrierhttp)));
			mCurrentSelectedProfile = 0;
		} catch (Exception e) {
			ERR("Billing","parseXMLFileOffline Parsing Exception = " + e);
		}
	}*/
	/**
	 * Used to read the profiles from the InputSource given.
	 * @param InputSource the current InputSource to use
	 */
	private void parseXML (InputSource isrc)
	{
		try {
			xr.parse(isrc);
			mUnlockProfile = myXMLParser.getUnlockProfile();
		} catch (Exception e) {
			ERR("Billing","parseXML Parsing Exception = " + e);
		}
	}
	/**
	 * @return true if the profile is found using the current Billing_type info, and set mCurrentSelectedProfile with the found profile index.
	 * @info use after <code>getUpdateProfileInfo</code>, if not found set mCurrentSelectedProfile with 0 if there is at least one profile found.
	 * @see #getUpdateProfileInfo()
	 */
	public boolean searchForDefaultProfile ()
	{
		int profileId;
		Profile profile = null;
		if(mUnlockProfile == null || mUnlockProfile.getProfileList() == null)
			return false;
		if (mUnlockProfile.getProfileList().size() > 0)
			mCurrentSelectedProfile = 0;
		for (profileId = 0; profileId < mUnlockProfile.getProfileList().size(); profileId ++)
		{
			profile = mUnlockProfile.getProfileList().get(profileId);
			if (mUnlockProfile.getBilling_type().equals("HTTP") && profile.isHTTPprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			} else if (mUnlockProfile.getBilling_type().equals("CC") && profile.isCCprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			} else if (mUnlockProfile.getBilling_type().equals("SMS") && profile.isSMSprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			} 
			#if ENABLE_IAP_PSMS_BILLING
			else if (mUnlockProfile.getBilling_type().equals("PSMS") && profile.isSMSprofile())
			{
				DBG("Billing", "Entered IF for PSMS: (mUnlockProfile.getBilling_type().equals(PSMS) && profile.isSMSprofile())");
				mCurrentSelectedProfile = profileId;
				return true;
			} 
			#endif
			else if (mUnlockProfile.getBilling_type().equals("WAP") && profile.isWAPprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			} else if (mUnlockProfile.getBilling_type().equals("WO") && profile.isWAPOTHERprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			}
#if USE_BILLING && USE_BOKU_FOR_BILLING
			 else if (mUnlockProfile.getBilling_type().equals("BOKU") && profile.isBOKUprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			}
#endif
		#if USE_BILLING_FOR_CHINA
			else if (mUnlockProfile.getBilling_type().equals("IPX") && profile.isIPXprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			}
			else if (mUnlockProfile.getBilling_type().equals("UMP") && profile.isUMPprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			}
		#endif
		}
		return false;
	}
	/**
	 * @return true if the profile is found using the profileNeeded String parameter, and set mCurrentSelectedProfile with the found profile index.
	 * @info use after <code>getUpdateProfileInfo</code>, if not found set mCurrentSelectedProfile with -1.
	 * @see #getUpdateProfileInfo()
	 * @param profileNeeded
	 */
	@Override
	public boolean searchForAditionalProfile (String profileNeeded)
	{
		int profileId;
		Profile profile = null;
		//mCurrentSelectedProfile = -1;
		for (profileId = 0; profileId < mUnlockProfile.getProfileList().size(); profileId ++)
		{
			profile = mUnlockProfile.getProfileList().get(profileId);
			if (profileNeeded.equals("HTTP") && profile.isHTTPprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			} else if (profileNeeded.equals("MRC") && profile.isMRCprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			} else if (profileNeeded.equals("CC") && profile.isCCprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;			
			} else if (profileNeeded.equals("WO") && profile.isWAPOTHERprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			} else if (profileNeeded.equals("SMS") && profile.isSMSprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			}
		#if USE_BILLING_FOR_CHINA
			else if (profileNeeded.equals("UMP") && profile.isUMPprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			}else if (profileNeeded.equals("IPX") && profile.isIPXprofile())
			{
				mCurrentSelectedProfile = profileId;
				return true;
			}
		#endif
		}
		return false;
	}
	/**
	 * @return the size of billing types
	 */
	public int getProfileListSize ()
	{
		if(mUnlockProfile.getProfileList() == null)
			return 0;
		else
			return mUnlockProfile.getProfileList().size();
	}
	/**
	 * @return true if the profile is found, and set mCurrentSelectedProfile with the found profile index.
	 * @info use after <code>getOfflineProfilesInfo</code>
	 * @sample <code>searchForProfile("us","310260","T-Mobile")</code> <BR>"3100260" (mcc=310,mnc=260)
	 * @see #getOfflineProfilesInfo()
	 * @see #searchForProfile()
	 */
	public boolean searchForProfile ()
	{
		return searchForProfile(mDevice.getSimCountryIso(), mDevice.getSimOperator(), mDevice.getSimOperatorName());
	}
	/**
	 * @return true if the profile is found, and set mCurrentSelectedProfile with the found profile index.
	 * @info use after <code>getOfflineProfilesInfo</code>
	 * @sample <code>searchForProfile("us","310260","T-Mobile")</code> <BR>"3100260" (mcc=310,mnc=260)
	 * @see #getOfflineProfilesInfo()
	 * @see #searchForProfile()
	 */
	public boolean searchForProfile (String countryIso, String simOperator, String operatorName)
	{	
		if (countryIso == null || countryIso.length() == 0 ||
			simOperator == null || simOperator.length() == 0 ||
			operatorName == null || operatorName.length() == 0 )
		{
			mCurrentSelectedProfile = -1;
			return false;
		}
		int profileId;
		int carrierId;
		Profile profile = null;
		Carrier carrier = null;
		String mcc = simOperator.substring(0, 3);
		String mnc = simOperator.substring(3, simOperator.length());
		for (profileId = 0; profileId < mUnlockProfile.getProfileList().size(); profileId ++)
		{
			profile = mUnlockProfile.getProfileList().get(profileId);
			if (profile.getCountry().contains(countryIso.toUpperCase()))
			{
				if (profile.getMcc().contains(mcc))
				{	
					for (carrierId = 0; carrierId < profile.getCarrier().size(); carrierId ++ )
					{
						carrier = profile.getCarrier().get(carrierId);
						if (carrier.getName().toUpperCase().contains(operatorName.toUpperCase()) && carrier.getMnc().contains(mnc))
						{
							mCurrentSelectedProfile = profileId;
							return true;
						}
					}
				} else
					continue;
			}

		}
		return false;
	}
	/**
	 * @return true if mCurrentSelectedProfile >= 0 and the selected profile support SMS
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public boolean suportSMS ()
	{
		if (mCurrentSelectedProfile <0 )
			return false;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).isSMSprofile();
	}
	/**
	 * @return true if mCurrentSelectedProfile >= 0 and the selected profile support double option
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public boolean suportDoubleOption ()
	{
		if (mCurrentSelectedProfile <0 )
			return false;
		return (mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getDouble_option() == 1);
	}
	/**
	 * @return the game price if mCurrentSelectedProfile >= 0 or 0.0f if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getGamePrice ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;//0.0f;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getGame_price();
	}
	/**
	 * @return the game price + Symbol if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getGamePriceFmt ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;//0.0f;
		return  mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getGamePriceFmt();
	}
	/**
	 * @return the game price + Symbol if mCurrentSelectedProfile >= 0 or null if not, this is the decoded symbol to ascii chars.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getMenuGamePriceFmt ()
	{
		String price_string;

		if (mCurrentSelectedProfile <0 )
			price_string=null;//0.0f;
		
		price_string = mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getGamePriceFmt();
		
		price_string = StringCurrencytoChar(price_string);
		return (price_string);
	}
	/**
	 * @return the internal profile Id if mCurrentSelectedProfile >= 0 or -1 if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getProfileId ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return ""+mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getId();
	}
	/**
	 * @return the server number if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	@Override 
	public String getServerNumber ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getServer_number();
	}
	/**
	 * @return the promoCode if mCurrentSelectedProfile >= 0 or -1 if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	@Override 
	public int getPromoCode ()
	{
		if (mCurrentSelectedProfile <0 )
			return -1;
		return mUnlockProfile.getPromoCode();
	}
	/**
	 * @return the promoCode URL if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	@Override 
	public String getPromoCodeURL()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getPromoCode_URL();
	}	
	/**
	 * @return the alias value if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 */
	@Override 
	public String getAlias ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getAlias();
	}
	/**
	 * @return the language value if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * * @see #getUpdateProfileInfo()
	 */
	public String getLanguage ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getLanguage();
	}
	/**
	 * @return the tnc string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getTNCString ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getTnc();
	}
		/**
	 * @return the type string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getProfileType ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return ""+mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getType();
	}
	/**
	 * @return the buyscreen string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getBuyScreen ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getBuyscreen();
	}
	/**
	 * @return the ompd_ed_sc string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getOMPD_ed_sc ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getOmpd_ed_sc();
	}
	/**
	 * @return the ompb_ed_mc string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getOMPB_ed_mc ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getOmpb_ed_mc();
	}
	/**
	 * @return the ompb_mcc string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getOMPB_mcc ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getOmpb_mcc();
	}
	/**
	 * @return the support string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getSupportString ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getSupport();
	}
	/**
	 * @return the double option string if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getDoubleOptionString ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getDouble_option();
	}
	
	/**
	 * @return the double option string if exist and if mCurrentSelectedProfile >= 0 or null if not this string is decoding the unicode to char 
	 * character of currency.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getMenuDoubleOptionString ()
	{
		String formated_string;

		if (mCurrentSelectedProfile <0 )
			formated_string=null;//0.0f;
		
		formated_string = mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getDouble_option();
		
		formated_string = StringCurrencytoChar(formated_string);
		
		return (formated_string);
	}
	/**
	 * @return the double option string 2 if exist and if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getDoubleOptionString_2()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getDouble_option_text_2();
	}
	/**
	 * @return the double option string 2 if exist and if mCurrentSelectedProfile >= 0 or null if not, This is the decode unicode currency symbol to a ascci chars.
	 * @info You must call <code>searchForProfile</code> or <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #searchForProfile()
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
	public String getMenuDoubleOptionString_2()
	{
		String formated_string;

		if (mCurrentSelectedProfile <0 )
			formated_string=null;//0.0f;
		
		formated_string = mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getLang_strings().getDouble_option_text_2();
		
		formated_string = StringCurrencytoChar(formated_string);
		
		return (formated_string);
	}
	/**
	 * @return the country id if mCurrentSelectedProfile >= 0 or -1 if not.
	 * @info You must call <code>searchForAditionalProfile</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForAditionalProfile(String)
	 */
	public int getCountryId ()
	{
		if (mCurrentSelectedProfile <0 )
			return -1;
		return mUnlockProfile.getCountry_id();
	}
	/**
	 * @return the operator id if mCurrentSelectedProfile >= 0 or -1 if not.
	 * @info You must call <code>searchForAditionalProfile</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForAditionalProfile(String)
	 */
	public int getOperatorId ()
	{
		if (mCurrentSelectedProfile <0 )
			return -1;
		return mUnlockProfile.getOperator_id();
	}
	/**
	 * @return the platform_id if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForAditionalProfile</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForAditionalProfile(String)
	 */
	public String getPlatformId ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return ""+mUnlockProfile.getPlatform_id();
	}
	/**
	 * @return the currency Value if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForAditionalProfile</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForAditionalProfile(String)
	 */
	@Override
	public String getStringCurrencyValue ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getCurrency_value()+"";
	}
	@Override
	public int getIntegerCurrencyValue ()
	{
		if (mCurrentSelectedProfile <0 )
			return -1;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getCurrency_value();
	}
	/**
	 * @return the currency Symbol if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForAditionalProfile</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForAditionalProfile(String)
	 */
	public String getCurrencySymbol ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getCurrency_symbol();
	}
	/**
	 * @return the url billing if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForAditionalProfile</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForAditionalProfile(String)
	 */
	public String getURLbilling ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getUrl_billing();
	}
	/**
	 * @return the billing type if mCurrentSelectedProfile >= 0 or null if not.
	 * @info You must call <code>searchForAditionalProfile</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getUpdateProfileInfo()
	 * @see #searchForAditionalProfile(String)
	 */
	public String getBillingType ()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getBilling_type();
	}
	/**
	 * @return true if mCurrentSelectedProfile >= 0, which means the profile currently selected
	 * @info You must call <code>getOfflineProfilesInfo</code> or <code>getUpdateProfileInfo</code> before using this method.
	 * @see #getOfflineProfilesInfo(Context)
	 * @see #getUpdateProfileInfo()
	 */
#if USE_BILLING && USE_BOKU_FOR_BILLING
	public String getServiceId()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getServiceId();
	}
	
	public String getPriceWithTaxes()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getPriceWithTaxes();
	}
#endif
#if USE_BILLING_FOR_CHINA
	/**
	 * Get Profile by the name provide 
	 * @param profileNeeded EX: SMS HTTP UMP IPX...
	 * @return Profile for name or null if not found
	 */		
	public Profile getProfileByName(String profileNeeded)
	{
		int profileId;
		Profile profile = null;
		if(mUnlockProfile == null || mUnlockProfile.getProfileList() == null)
			return null;
		for (profileId = 0; profileId < mUnlockProfile.getProfileList().size(); profileId ++)
		{
			profile = mUnlockProfile.getProfileList().get(profileId);
			if (profileNeeded.equals("HTTP") && profile.isHTTPprofile())
			{
				return profile;
			} else if (profileNeeded.equals("CC") && profile.isCCprofile())
			{
				return profile;
			} else if (profileNeeded.equals("SMS") && profile.isSMSprofile())
			{
				return profile;
			} else if (profileNeeded.equals("WAP") && profile.isWAPprofile())
			{
				return profile;
			} else if (profileNeeded.equals("WO") && profile.isWAPOTHERprofile())
			{
				return profile;
			}
		#if USE_BILLING && USE_BOKU_FOR_BILLING
			else if (profileNeeded.equals("BOKU") && profile.isBOKUprofile())
			{
				return profile;
			}
		#endif
		#if USE_BILLING_FOR_CHINA
			else if (profileNeeded.equals("IPX") && profile.isIPXprofile())
			{
				return profile;
			}
			else if (profileNeeded.equals("UMP") && profile.isUMPprofile())
			{
				return profile;
			}
		#endif
		}
		return null;
	}
	
	public String getUmpMessageAddress1()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getUmpMessageAddress1();
	}
	
	public String getUmpMessageAddress2()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getUmpMessageAddress2();
	}
	
	public String getUmpMessage()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getUmpMessage();
	}
	
	public String getIpxUserName()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getIpxUserName();
	}
	
	public String getIpxPassword()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getIpxPassword();
	}
	
	public String getIpxUrlLookup()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getIpxUrlLookup();
	}
	
	public String getIpxProductID()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getIpxProductID();
	}
	
	public String getIpxPinCode()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getIpxPinCode();
	}
	
	public String getIpxChannel()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getIpxChannel();
	}
	
    /**
     * Get information from IPX lookup profile
      * @return TRUE if success, FALSE if can not connect to server or profile not avaiable
     */	
	public boolean getIPXLookup()
	{
		DBG("ServerInfo", "getIPXLookup() " );
		Profile IPXProfile = getProfileByName("IPX");
		if(IPXProfile == null)
		{
			DBG("ServerInfo", "IPX profile not present");
			return false;
		}
		int IPXMoneyRemain = Integer.parseInt(IPXProfile.getGame_price()) - IPXProfile.getIPXPaidMoney();
		String hostAdd = IPXProfile.getIpxUrlLookup();
		String query = "?IPAddress=NULL" + "&Username=" + IPXProfile.getIpxUserName() + "&Password=" + IPXProfile.getIpxPassword() + "&ProductId=" + IPXProfile.getIpxProductID() + "&PinCode=" + IPXProfile.getIpxPinCode() + "&TotalPrice=" + IPXMoneyRemain;
		
		mXPlayer.sendRequest(hostAdd, query);
		while (!mXPlayer.handleRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {DBG(TAG,"getIPXLookup() exception");}
			DBG(TAG, "[sendIPXLookupRequest]Waiting for response");
		} 
		if (XPlayer.getLastErrorCode() == XPlayer.ERROR_NONE)
		{
			int profileId = mUnlockProfile.getProfileList().indexOf(IPXProfile);
			try {				
				java.io.InputStream is = new ByteArrayInputStream(XPlayer.getWHTTP().m_response.getBytes());
				org.xmlpull.v1.XmlPullParser parser = android.util.Xml.newPullParser();
				parser.setInput(is, "utf-8");
				myXMLParser.parseIPXpull(parser, IPXProfile);
				mUnlockProfile.getProfileList().set(profileId, IPXProfile);
				mUnlockProfile.getProfileList().get(profileId).setIPXSentCount(0);
			}catch (Exception e) {}
			return true;
		}
		return false;
	}
	
    /**
     * Get current active profile
     * @return Profile
     */	
	public Profile getCurrentProfileSelected()
	{
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile);
	}
#endif

	public boolean isProfileSelected()
	{
		return (mCurrentSelectedProfile >= 0);
	}
	
	public String getMRCBuyInfo()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getMRCBuyInfo();
	}	
	
	public String getMRCDiscount()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getMRCDiscount();
	}
	
	public String getMRCDetails()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getMRCDetails();
	}
	
	public String getBuyInfo()
	{
		if (mCurrentSelectedProfile <0 )
			return null;
		return mUnlockProfile.getProfileList().get(mCurrentSelectedProfile).getBuyInfo();
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
}