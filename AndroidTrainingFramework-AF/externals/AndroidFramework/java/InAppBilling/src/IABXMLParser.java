package APP_PACKAGE.iab;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IABXMLParser extends DefaultHandler
{
	SET_TAG("InAppBilling");
	
	boolean currentElement = false;
	boolean readingLanguages = false;
	String currentValue = null;
	String tmpValue = null;
	String currentText = null;
	
	public cItem mItem = null;
	public cBilling mBilling = null;
	
	private ShopProfile mSInfo = null;
	
	private boolean addingShopProfile = false;
	private boolean addingItems = false;
	private boolean addingBillingOpts = false;
	private boolean addingTexts = false;
	private boolean shopProfileAddingTexts = false;

	/**
	 * @return the msInfo
	 */
	public ShopProfile getShopProfile() 
	{
		return mSInfo;
	}
	
	@Override
	public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException 
	{
		String attr = null;
		if (currentElement)
			currentValue = "";
		currentElement = true;
		if (localName.equals("shop_info"))
		{
			mSInfo = new ShopProfile();
			addingShopProfile = true;
		} 
		else if (addingShopProfile)
		{
			if (localName.equals("country"))
			{
				attr = attributes.getValue("id");
				mSInfo.setCountryId(attr);
			}
			else if (localName.equals("operator"))
			{
				attr = attributes.getValue("id");
				mSInfo.setOperatorId(attr);
			}
			else if (localName.equals("product"))
			{
				attr = attributes.getValue("id");
				mSInfo.setProductId(attr);
			}
			else if (localName.equals("platform"))
			{
				attr = attributes.getValue("id");
				mSInfo.setPlatformId(attr);
			}
			else if (localName.equals("language"))
			{
				attr = attributes.getValue("id");
				mSInfo.setLangId(attr);
			}
			else if (localName.equals("texts"))
			{
				shopProfileAddingTexts = true;
			}
			else if (shopProfileAddingTexts)
			{
				if (localName.equals("text"))
				{
					currentText = attributes.getValue("key");
				}
			}
			
		}
		else if (localName.equals("content_list"))
		{
			addingItems = true;
		}
		else if (addingItems)
		{
			if (addingBillingOpts)
			{
				if (localName.equals("billing"))
				{
					mBilling = new cBilling();
					attr = attributes.getValue("type");
					mBilling.setBillingType(attr);
				}
				else if (localName.equals("texts"))
				{
					addingTexts = true;
				}
				else if(addingTexts)
				{
					if (localName.equals("text"))
					{
						tmpValue = attributes.getValue("key");
					}
				}
				
			}
			else if (localName.equals("content"))
			{
				mItem = new cItem();
				attr = attributes.getValue("id");
				mItem.setId(attr);
				attr = attributes.getValue("type");
				mItem.setType(attr);
			}
			else if (localName.equals("attribute")) 
			{
				tmpValue = attributes.getValue("name");
			}
			else if (localName.equals("billing_list")) 
			{
				addingBillingOpts = true;
			}
			
		}		
		
	}
	
	@Override
	public void endElement (String uri, String localName, String qName) throws SAXException 
	{
		currentElement = false;
		currentValue = currentValue.trim();
		if (addingShopProfile)
		{
			if (localName.equals("country"))
			{
				mSInfo.setCountryValue(currentValue.trim());
			}
			else if (localName.equals("operator"))
			{
				mSInfo.setOperatorValue(currentValue.trim());
			}
			else if (localName.equals("product"))
			{
				mSInfo.setProductValue(currentValue.trim());
			}
			else if (localName.equals("language"))
			{
				mSInfo.setLangValue(currentValue.trim());
			}
			else if (localName.equals("shop_info"))
			{
				addingShopProfile = false;
			} 
			//For Promo
			else if (localName.equals("promo_description"))
			{
				mSInfo.setPromoDescription(currentValue.trim());
			}
			else if (localName.equals("promo_endtime"))
			{
				mSInfo.setPromoEndTime(currentValue.trim());
			}
			else if (localName.equals("server_time"))
			{
				mSInfo.setPromoServerTime(currentValue.trim());
			}
			else if (localName.equals(InAppBilling.GET_STR_CONST(IAB_URL_LIMITS)))
			{
				mSInfo.setVLimitsURL(currentValue.trim());
			}
			else if (localName.equals("app_id"))
			{
				mSInfo.setAppId(currentValue.trim());
			}
			else if (localName.equals("app_key"))
			{
				mSInfo.setAppKey(currentValue.trim());
			}
			if(shopProfileAddingTexts)
			{
				if(localName.equals("texts"))
				{
					shopProfileAddingTexts = false;
				}
				else if (localName.equals("text"))
				{
					if (currentText.equals(InAppBilling.GET_STR_CONST(IAB_PSMS_TNC_TITLE)))
						mSInfo.setTNCTitle(currentValue);
					else if (currentText.equals(InAppBilling.GET_STR_CONST(IAB_PSMS_TNC_TITLE_SHOP)))
						mSInfo.setTNCTitleShop(currentValue);
					currentText = null;
				}
			}
			
		}
		else if (addingItems)
		{
			if (addingBillingOpts)
			{
				if (localName.equals("billing"))
				{
					mItem.addBilling(mBilling);
				}
				/*else if (localName.equals("price"))
				{
					mBilling.setPrice(currentValue.trim());
				}
				else if (localName.equals("currency"))
				{
					mBilling.setCurrency(currentValue.trim());
				}
				else if (localName.equals("tnc"))
				{
					mBilling.setTNC(currentValue.trim());
				}
				else if (localName.equals("url"))
				{
					mBilling.setURL(currentValue.trim());
				}
				else if (localName.equals("uid"))
				{
					mBilling.setUID(currentValue.trim());
				}
				else if (localName.equals("money"))
				{
					mBilling.setMoney(currentValue.trim());
				}
				else if (localName.equals("is_3g_only"))
				{
					mBilling.setIs3GOnly(currentValue.trim());
				}
				else if (localName.equals("aid"))
				{
					mBilling.setAID(currentValue.trim());
				}
				else if (localName.equals("pid"))
				{
					mBilling.setPID(currentValue.trim());
				}
				else if (localName.equals("service_id"))
				{
					mBilling.setServiceID(currentValue.trim());
				}
				else if (localName.equals("price_with_taxes"))
				{
					mBilling.setPriceIncludeTaxes(currentValue.trim());
				}
				else if (localName.equals("formated_price"))
				{
					mBilling.setFormatedPrice(currentValue.trim());
				}
				else if (localName.equals("shortcode")) 
				{
					mBilling.setShortCode(currentValue.trim());
				}
				else if (localName.equals("alias")) 
				{
					mBilling.setAlias(currentValue.trim());
				}
				else if (localName.equals("type")) 
				{
					mBilling.setPSMSType(currentValue.trim());
				}
				else if (localName.equals("id")) 
				{
					mBilling.setProfileId(currentValue.trim());
				}*/				
				else if (localName.equals("billing_list")) 
				{
					addingBillingOpts = false;
				}
				
				//if not closing <billing> or <billing_list> tag yet 
				if (addingBillingOpts && !localName.equals("billing"))
				{
					if(addingTexts)
					{
						if(localName.equals("texts"))
						{
							addingTexts = false;
						}
						else if (localName.equals("text"))
						{
							if(!tmpValue.contains("purchase_"))
								tmpValue = "purchase_" + tmpValue;
							mBilling.addAttribute(tmpValue, currentValue.trim());
							tmpValue = null;
						}
					}
					else
					{
						mBilling.addAttribute(localName, currentValue.trim());
						if (localName.equals("currency"))
						{
							java.util.Locale locale = java.util.Locale.getDefault();
							String localestr = locale.toString()+"@currency="+currentValue.trim();
							mBilling.addAttribute("locale", localestr);
						}
					}
				}
			}
			else if (localName.equals("content")) 
			{
				mSInfo.addItem(mItem);
			}
			else if (localName.equals("attribute")) 
			{
				mItem.addAttribute(tmpValue, currentValue.trim());
				tmpValue = null;
			}
			else if (localName.equals("billing_type_pref"))
			{
				mItem.setType_pref(currentValue.trim());
			}
            else if (localName.equals("tracking_uid"))
			{
				mItem.setTrackingId(currentValue.trim());//generic tracker for purchases
			}
			else if (localName.equals("content_list"))
			{
				addingItems = false;
			}
		}
		currentValue = "";
	}


	@Override
	public void characters (char[] ch, int start, int length) throws SAXException 
	{
		if (currentElement)
		{
			String value = new String(ch, start, length);
			// if (value.equals("\n"))
				// value = "";
			currentValue += value;
		}
	}
}


