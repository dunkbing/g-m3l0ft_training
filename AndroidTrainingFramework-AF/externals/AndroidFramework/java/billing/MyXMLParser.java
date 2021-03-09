package APP_PACKAGE.billing;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

#if USE_BILLING_FOR_CHINA
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
#endif

import APP_PACKAGE.GLUtils.Carrier;

public class MyXMLParser extends DefaultHandler {

	boolean currentElement = false;
	boolean readingLanguages = false;
	boolean readingNoprofileMsg = false;
	String noProfileLang = "";
	String currentValue = null;
	public Profile mProfile = null;
	public Carrier mCarrier = null;
	private UnlockProfile mUnlockProfile = null;
	private boolean addingMRCprofile = false;
	private boolean addingHTTPprofile = false;
	private boolean addingSMSprofile = false;
	private boolean addingCCprofile = false;
	private boolean addingWAPprofile = false;
	private boolean addingWAPOTHERprofile = false;
	private boolean addingBOKUprofile = false;
#if USE_BILLING_FOR_CHINA	
	private boolean addingIPXprofile = false;
	private boolean addingUMPprofile = false;
#endif

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

	/** Called when tag starts ( ex:- <name>US $4,99 T-Mobile HD+</name> 
	 * -- <name> )*/
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (currentElement)
			currentValue = "";
		currentElement = true;
		if (localName.equals("unlock_profiles"))
		{
			mUnlockProfile = new UnlockProfile();
		}
		else if (localName.equals("profiles") ||
			localName.equals("cc_profiles") ||
			localName.equals("wo_profiles") ||
			localName.equals("wap_profiles")||
			localName.equals("http_profiles")||
			localName.equals("http_sgs_profiles")
#if USE_BILLING && USE_BOKU_FOR_BILLING		
			|| localName.equals("boku_profiles")
#endif		
		#if USE_BILLING_FOR_CHINA
			|| localName.equals("ump_profiles")
			|| localName.equals("ipx_profiles")
		#endif
				)
		{
			if (mUnlockProfile == null) //Offline Version
				mUnlockProfile = new UnlockProfile();
			if (mUnlockProfile.getProfileList() == null)
			{
				mUnlockProfile.setProfileList(new ArrayList<Profile>());
			}
			// reset 
			addingMRCprofile = false;
			addingHTTPprofile = false;
			addingSMSprofile = false;
			addingCCprofile = false;
			addingWAPprofile = false;
			addingWAPOTHERprofile = false;
			addingBOKUprofile = false;
		#if USE_BILLING_FOR_CHINA
			addingIPXprofile = false;
			addingUMPprofile = false;
		#endif
			
			if (localName.equals("cc_profiles")) {
				addingCCprofile = true;
			} else if (localName.equals("wo_profiles")) {
				addingWAPOTHERprofile = true;
			} else if (localName.equals("profiles")) {
				addingSMSprofile = true;
			} else if (localName.equals("http_profiles")) {
				addingHTTPprofile = true;
			} else if (localName.equals("http_sgs_profiles")) {
				addingMRCprofile = true;
			} else if (localName.equals("wap_profiles")) {
				addingWAPprofile = true;
			}
#if USE_BILLING && USE_BOKU_FOR_BILLING
			else if (localName.equals("boku_profiles")) {
				addingBOKUprofile = true;
			}
#endif
		#if USE_BILLING_FOR_CHINA
			else if (localName.equals("ipx_profiles")) {
				addingIPXprofile = true;
			}
			else if (localName.equals("ump_profiles")) {
				addingUMPprofile = true;
			}
		#endif
			
		} else if (localName.equals("profile")) {
			/** Get attribute id */
			mProfile = new Profile();
			String attr = attributes.getValue("id");
			try {
				mProfile.setId(Integer.parseInt(attr));
			} catch (NumberFormatException e) {	mProfile.setId(-1);	}
			mProfile.setCCprofile(addingCCprofile);
			mProfile.setHTTPprofile(addingHTTPprofile);
			mProfile.setMRCprofile(addingMRCprofile);
			mProfile.setSMSprofile(addingSMSprofile);
			mProfile.setWAPprofile(addingWAPprofile);
			mProfile.setWAPOTHERprofile(addingWAPOTHERprofile);
#if USE_BILLING && USE_BOKU_FOR_BILLING
			mProfile.setBOKUprofile(addingBOKUprofile);
#endif
		#if USE_BILLING_FOR_CHINA
			mProfile.setIPXprofile(addingIPXprofile);
			mProfile.setUMPprofile(addingUMPprofile);
		#endif
		} else if (localName.equals("carrier")) {
			/** Get attribute name */
			mCarrier = new Carrier();
			String attr = attributes.getValue("name");
			mCarrier.setName(attr);
		} else if (localName.equals("languages")) {
			readingLanguages = true;
		} else if (localName.equals("tnc")) {
			/** Get attribute value */
			String attr = attributes.getValue("value");
			if (attr == null)
				attr = attributes.getValue("lan");
			mProfile.getLang_strings().setTnc(attr);
		} else if (localName.equals("currency")) {
			/** Get attribute value */
			String attr = attributes.getValue("value");
			if (attr != null)
				mProfile.setCurrency_value(Integer.parseInt(attr));
		} else if (localName.equals("no_profile_message")) {
			readingNoprofileMsg = true;
			noProfileLang = attributes.getValue("lan");			
		}
		
	}

	/** Called when tag closing ( ex:- <name>US $4,99 T-Mobile HD+</name> 
	 * -- </name> )*/
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		currentElement = false;
		currentValue = currentValue.trim();
		if (localName.equals("profile")) {
			/** Add mProfile to profileList cause its done reading; */ 
			mUnlockProfile.getProfileList().add(mProfile);
		} else if(localName.equals("name")) {
			mProfile.setName(currentValue);
		} else if(localName.equals("status")) {
			mProfile.setStatus(currentValue);
		} else if(localName.equals("country")) {
			mProfile.setCountry(currentValue);
		} else if(localName.equals("mcc")) {
			String mcc[] = currentValue.split("\\|");
			for (int i = 0; i < mcc.length; i++)
				mProfile.getMcc().add(mcc[i]);
		} else if(localName.equals("mnc")) {
			if (currentValue.length()>0)
			{
				String mnc[] = currentValue.split("\\|");
				for (int i = 0; i < mnc.length; i++)
					mCarrier.getMnc().add(mnc[i]);
			}
		} else if (localName.equals("carrier")) {
			/** Add mCarrier to mProfile cause it. done reading; */ 
			mProfile.getCarrier().add(mCarrier);
		} else if (localName.equals("type")) {
			mProfile.setType(Integer.parseInt(currentValue));
		} 
		/** Languages Tag begin */
		else if (localName.equals("languages")) {
			readingLanguages = false;
		} 
		else if (localName.equals("tnc")) {
			mProfile.getLang_strings().setTnc(currentValue);
		} else if (localName.equals("buyscreen")) {
			mProfile.getLang_strings().setBuyscreen(currentValue);
		} else if (localName.equals("ompd_ed_sc")) {
			mProfile.getLang_strings().setOmpd_ed_sc(currentValue);
		} else if (localName.equals("ompb_sc")) {
			mProfile.getLang_strings().setOmpb_sc(currentValue);
		} else if (localName.equals("ompb_ed_mc")) {
			mProfile.getLang_strings().setOmpb_ed_mc(currentValue);
		} else if (localName.equals("ompb_mcc")) {
			mProfile.getLang_strings().setOmpb_mcc(currentValue);
		} else if (localName.equals("support")) {
			mProfile.getLang_strings().setSupport(currentValue);
		} else if (localName.equals("double_optin_2")) {
			mProfile.getLang_strings().setDouble_option_Text_2(currentValue);
		} else if (localName.equals("double_optin") && readingLanguages) {
			mProfile.getLang_strings().setDouble_option(currentValue);
		}
		/** Languages Tag end */
		else if (localName.equals("delivery-type")) {
			mProfile.setDelivery_type(currentValue);
		} else if (localName.equals("game_price")) {
			mProfile.setGame_price((currentValue));
		} else if (localName.equals("double_optin")) {
			mProfile.setDouble_option(Integer.parseInt(currentValue));
		} else if (localName.equals("server_number")) {
			mProfile.setServer_number(currentValue);
		} else if (localName.equals("currency")) {
			mProfile.setCurrency_symbol(currentValue);
		} else if (localName.equals("alias")) {
			mProfile.setAlias(currentValue);
		} else if (localName.equals("url_billing")) {
			mProfile.setUrl_billing(currentValue);
		//MRC BEGIN
		} else if (localName.equals("helpnunsub")) {
			mProfile.setMRCBuyInfo(currentValue);
		} else if (localName.equals("helpunsubppd")) {
			mProfile.setBuyInfo(currentValue);
		} else if (localName.equals("discount")) {
			mProfile.setMRCDiscount(currentValue);
		} else if (localName.equals("details")) {
			mProfile.setMRCDetails(currentValue);
		//MRC END
		} else if (localName.equals("country_id")) {
			try {
				mUnlockProfile.setCountry_id(Integer.parseInt(currentValue));
			} catch (NumberFormatException e) 
			{
				mUnlockProfile.setCountry_id(-1);
			}
		} else if (localName.equals("operator_id")) {
			try {
				mUnlockProfile.setOperator_id(Integer.parseInt(currentValue));
			} catch (NumberFormatException e) 
			{
				mUnlockProfile.setOperator_id(-1);
			}
		} else if (localName.equals("platform_id")) {
			try {
				mUnlockProfile.setPlatform_id(Integer.parseInt(currentValue));
			} catch (NumberFormatException e) 
			{
				mUnlockProfile.setPlatform_id(-1);
			}	
		} else if (localName.equals("promo_code")) {
			try {
				mUnlockProfile.setPromoCode(Integer.parseInt(currentValue));
			} catch (NumberFormatException e) 
			{
				mUnlockProfile.setPromoCode(-1);
			}	
		} else if (localName.equals("billing_type")) {
			mUnlockProfile.setBilling_type(currentValue);
		}
		else if (localName.equals("promo_code_url")) {
			mUnlockProfile.setPromoCode_URL(currentValue);
		}else if (localName.equals("no_profile_message")) {
			if (readingNoprofileMsg) {
				mUnlockProfile.setNoProfileMsg(noProfileLang, currentValue);
				readingNoprofileMsg = false;
			}
		}
#if USE_BILLING && USE_BOKU_FOR_BILLING		
		else if (localName.equals("service_id")) {
			mUnlockProfile.setServiceId(currentValue);
		}
		else if (localName.equals("price_with_taxes")) {
			mUnlockProfile.setPriceWithTaxes(currentValue);
		}
		else if (localName.equals("formated_price")) {
			mUnlockProfile.setFormatedPrice(currentValue);
		}
#endif	
	#if USE_BILLING_FOR_CHINA
		// UMP profile
		else if (localName.equals("game_price_ump")) {
			mProfile.setGame_price((currentValue));
		}
		else if (localName.equals("shortcode1")) {
			mProfile.setUmpMessageAddress1((currentValue));
		}
		else if (localName.equals("shortcode2")) {
			mProfile.setUmpMessageAddress2((currentValue));
		}
		else if (localName.equals("ump_message")) {
			mProfile.setUmpMessage((currentValue));
		}
		else if (localName.equals("ump_get_transid")) {
			mProfile.setUmp_get_transid((currentValue));
		}
		else if (localName.equals("ump_useable")) {
			mProfile.setUmp_useable((currentValue));
		}
		else if (localName.equals("merid")) {
			mProfile.setUmpMerid((currentValue));
		}
		else if (localName.equals("goodsid")) {
			mProfile.setUmpGoodsid((currentValue));
		}
		else if (localName.equals("goods_amount")) {
			mProfile.setUmpGoodsAmount((currentValue));
		}
		else if (localName.equals("ump_get_billing_result")) {
			mProfile.setUmp_get_billing_result((currentValue));
		}
		// IPX profile
		else if (localName.equals("game_price_ipx")) {
			mProfile.setGame_price((currentValue));
		}
		else if (localName.equals("url_lookup")) {
			mProfile.setIpxUrlLookup((currentValue));
		}
		else if (localName.equals("Username")) {
			mProfile.setIpxUserName((currentValue));
		}
		else if (localName.equals("password")) {
			mProfile.setIpxPassword((currentValue));
		}
		else if (localName.equals("ipx_product")) {
			mProfile.setIpxProductID((currentValue));
		}
		else if (localName.equals("pincode")) {
			mProfile.setIpxPinCode((currentValue));
		}
		else if (localName.equals("ipx_channel")) {
			mProfile.setIpxChannel((currentValue));
		}
	#endif
		currentValue = "";
	}

	/** Called to get tag characters ( ex:- <name>US $4,99 T-Mobile HD+</name> 
	 * -- to get (US $4,99 T-Mobile HD+) Characters ) */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (currentElement) {
			String value = new String(ch, start, length);
			if (value.equals("\n"))
				value = "";
			currentValue += value;
			//currentElement = false;
		}
	}

#if USE_BILLING_FOR_CHINA	
    /**
     * parse profile for IPX lookup
     * @param parser: xml return form lookup
     * @param profileRefer: profile IPX to update infomation
     * @throws XmlPullParserException
     * @throws IOException
     */
	
	public void parseIPXpull(XmlPullParser parser, Profile profileRefer)
								throws XmlPullParserException, java.io.IOException {
		int parserEvent = parser.getEventType();
		IPXItem item = null;
		while (parserEvent != XmlPullParser.END_DOCUMENT) {

			if (parserEvent == XmlPullParser.START_TAG) {
				String name = parser.getName();
				if (name.equals("messagingMode")) {
					profileRefer.setIpxMessagingMode(Integer.parseInt(parser.nextText()));
				} else if (name.equals("TransactionId")) {
					profileRefer.setIpxTransactionId(parser.nextText());
				} else if (name.equals("responseCode")) {
					profileRefer.setIpxResponseCode(Integer.parseInt(parser.nextText()));

					// billing element
				} else if (name.equals("billing")) {
					item = new IPXItem();
				} else if (name.equals("shortcode")) {
					item.setShortCode(parser.nextText());
				} else if (name.equals("keyword")) {
					item.setKeyword(parser.nextText());
				} else if (name.equals("price")) {
					item.setPrice(Integer.parseInt(parser.nextText()));
				} else if (name.equals("count")) {
					item.setCount(Integer.parseInt(parser.nextText()));
				}
			} else if (parserEvent == XmlPullParser.END_TAG) {
				String name = parser.getName();
				if (name.equals("billing") && item != null) {
					int count = item.getCount();
					for (int i = 0; i < count; i++) {
						profileRefer.getIPXItemList().add(item);
					}
					item = null;
				}
			}

			parserEvent = parser.next();
		}
	}
#endif
}
