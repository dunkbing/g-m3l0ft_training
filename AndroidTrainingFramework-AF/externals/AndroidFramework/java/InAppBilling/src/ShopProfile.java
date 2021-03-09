#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

public class ShopProfile 
{
	SET_TAG("InAppBilling");
	
	private Hashtable<String, ArrayList<cItem>> mItemsMap = new Hashtable<String, ArrayList<cItem>>();
	private Hashtable<String, cItem> mItemsIds = new Hashtable<String, cItem>();
	private String countryId 		= "";
	private String countryValue		= "";
	
	private String operatorId 		= "";
	private String operatorValue	= "";
	
	private String productId 		= "";
	private String productValue		= "";
	private String platformId		= "";
	
	private String langId 			= "";
	private String langValue		= "";
	
	private String vLimits			= "";
	
	//For Promo
	private String promoDescription	= "";
	private String promoEndTime		= "";
	private String promoServerTime	= "";
	
	//PSMS PHD FLOW
	private String TNCTitle = "";
	private String TNCTitleShop = "";

	private String appId	= "";
	private String appKey	= "";
	
	/**
	 * @return the item List
	 * @param value the key name of the list
	 */
	public ArrayList<cItem> getItemList(String key) 
	{
		if (key.equals(""))
			return getItemList();
			
		if (mItemsMap.containsKey(key))
		{
			return (mItemsMap.get(key));
		}
		return null;
	}
	
	/**
	 * @return the global item List
	 */
	public ArrayList<cItem> getItemList() 
	{
		if (mItemsIds != null && mItemsIds.size() > 0)
		{
			//return (Collections.list(mItemsIds.elements()));
			ArrayList<cItem> alist = new ArrayList<cItem>();
			Enumeration <String> types = mItemsMap.keys();
			while (types.hasMoreElements())
			{	
				String value = types.nextElement();
				alist.addAll(mItemsMap.get(value));
			}
			return alist;
		}
		return null;
	}
	
	/**
	 * @return the number of items in the profile
	 */
	public int getTotalItems() 
	{
		if (mItemsIds != null)
		{
			return mItemsIds.size();
		}
		return 0;
	}
	
	/**
	 * @replace the item atribute value
	 * @param idkey the item id value
	 * @param atname the attribute name to change
	 * @param newvalue the value to be assigned to the attribute
	 */
	public void replaceItemAtributeValue(String idkey, String atname, String newvalue)
	{
		mItemsIds.get(idkey).addAttribute(atname, newvalue);
	}
	
	/**
	 * @Return the String ItemId given the uid(sample uid = "com.gameloft.mmorpg.1cash")
	 * @param value the uid value
	 */
	public String getItemIdByUID(String value)
	{
		if (value != null)
		{
			Enumeration <String> list = mItemsIds.keys();
			while (list.hasMoreElements())
			{
				cItem item = mItemsIds.get(list.nextElement());
			#if BAZAAR_STORE
				cBilling billing = item.getBillingByType(InAppBilling.GET_STR_CONST(IAB_BAZAAR));
			#elif YANDEX_STORE
				cBilling billing = item.getBillingByType(InAppBilling.GET_STR_CONST(IAB_YANDEX));
			#elif AMAZON_STORE
				cBilling billing = item.getBillingByType(InAppBilling.GET_STR_CONST(IAB_AMAZON));
			#else
				cBilling billing = item.getBillingByType(InAppBilling.GET_STR_CONST(IAB_GOOGLE));
			#endif
				//if (value.equals(billing.getUID()))
			#if YANDEX_STORE
				if (value.equals(billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PID))))
			#else
				if (value.equals(billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_UID))))
			#endif
				{
					return item.getId();
				}
			}
		}	
		return null;
	}
	
	/**
	 * @Return the String ItemId given the uid(sample uid = "com.gameloft.mmorpg.1cash")
	 * @param value the uid value
	 */
	public String getItemIdByPID(String value)
	{
		if (value != null)
		{
			Enumeration <String> list = mItemsIds.keys();
			while (list.hasMoreElements())
			{
				cItem item = mItemsIds.get(list.nextElement());
			#if BAZAAR_STORE
				cBilling billing = item.getBillingByType(InAppBilling.GET_STR_CONST(IAB_BAZAAR));
			#elif YANDEX_STORE
				cBilling billing = item.getBillingByType(InAppBilling.GET_STR_CONST(IAB_YANDEX));
			#else
				cBilling billing = item.getBillingByType(InAppBilling.GET_STR_CONST(IAB_GOOGLE));
			#endif
				//if (value.equals(billing.getUID()))
				if (value.equals(billing.getAttributeByName(InAppBilling.GET_STR_CONST(IAB_PID))))
				{
					return item.getId();
				}
			}
		}	
		return null;
	}

	/**
	 * @return the cItem 
	 * @param value the id of the item
	 */
	public cItem getItemById(String key) 
	{
		//DBG(TAG,"key \'" + key + "\'" + mItemsMap.containsKey(key));
		if (mItemsIds.containsKey(key))
		{
			return (mItemsIds.get(key));
		}
		return null;
	}

	/**
	 * @param item the cItem to be added to the list
	 */
	public void addItem(cItem item) 
	{
		if (item != null)
		{
			String key = item.getType();
			if (mItemsMap.containsKey(key))
			{
				mItemsMap.get(key).add(item);
			}
			else 
			{
				ArrayList <cItem> al = new ArrayList<cItem>();
				al.add(item);
				mItemsMap.put(key, al);
			}
			mItemsIds.put(item.getId(),item);
		}
	}
	
	/**
	 * @return the countryId
	 */
	public String getCountryId() {
		return countryId;
	}
	
	/**
	 * @param value the countryId to set
	 */
	public void setCountryId(String value) {
		countryId = value;
	}
	
	/**
	 * @return the countryValue
	 */
	public String getCountryValue() {
		return countryValue;
	}
	
	/**
	 * @param value the countryValue to set
	 */
	public void setCountryValue(String value) {
		countryValue = value;
	}
	
	/**
	 * @return the operatorId
	 */
	public String getOperatorId() {
		return operatorId;
	}
	
	/**
	 * @param value the operatorId to set
	 */
	public void setOperatorId(String value) {
		operatorId = value;
	}
	
	/**
	 * @return the operatorValue
	 */
	public String getOperatorValue() {
		return operatorValue;
	}
	
	/**
	 * @param value the operatorValue to set
	 */
	public void setOperatorValue(String value) {
		operatorValue = value;
	}
	
	/**
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}
	
	/**
	 * @param value the productId to set
	 */
	public void setProductId(String value) {
		productId = value;
	}
	
	/**
	 * @return the platformId
	 */
	public String getPlatformId() {
		return platformId;
	}
	
	/**
	 * @param value the platformId to set
	 */
	public void setPlatformId(String value) {
		platformId = value;
	}
	
	/**
	 * @return the productValue
	 */
	public String getProductValue() {
		return productValue;
	}
	
	/**
	 * @param value the productValue to set
	 */
	public void setProductValue(String value) {
		productValue = value;
	}
	
	/**
	 * @return the langId
	 */
	public String getLangId() {
		return langId;
	}
	
	/**
	 * @param value the langId to set
	 */
	public void setLangId(String value) {
		langId = value;
	}
	
	/**
	 * @return the langValue
	 */
	public String getLangValue() {
		return langValue;
	}
	
	/**
	 * @param value the langValue to set
	 */
	public void setLangValue(String value) {
		langValue = value;
	}
	
	/**
	 * @return the validation limits url
	 */
	public String getVLimitsURL() {
		return vLimits;
	}
	
	/**
	 * @param value the validation limits url to set
	 */
	public void setVLimitsURL(String value) {
		vLimits = value;
	}
	
	//For Promo BEGIN
	/**
	 * @param value the Promo Description
	 */
	public void setPromoDescription(String value) {
		promoDescription = value;
	}
	
	/**
	 * @return the Promo Description
	 */
	public String getPromoDescription() {
		return promoDescription;
	}
	
	/**
	 * @param value the PromoEndTime
	 */
	public void setPromoEndTime(String value) {
		promoEndTime = value;
	}
	
	/**
	 * @return the PromoEndTime
	 */
	public String getPromoEndTime() {
		return promoEndTime;
	}
	
	/**
	 * @param value the Promo ServerTime
	 */
	public void setPromoServerTime(String value) {
		promoServerTime = value;
	}
	
	/**
	 * @return the Promo ServerTime
	 */
	public String getPromoServerTime() {
		return promoServerTime;
	}
	//For Promo END

	/**
	 * @param value the Terms and conditions Title for PSMS PHD FLOW
	 */
	public void setTNCTitle(String value)
	{
		TNCTitle = value;
	}
	/**
	 * @return the Terms and conditions Title for PSMS PHD FLOW
	 */
	public String getTNCTitle() {
		return TNCTitle;
	}
	/**
	 * @param value the Terms and conditions Title Shop for PSMS PHD FLOW
	 */
	public void setTNCTitleShop(String value)
	{
		TNCTitleShop = value;
	}
	/**
	 * @return the Terms and conditions Title Shop for PSMS PHD FLOW
	 */
	public String getTNCTitleShop() {
		return TNCTitleShop;
	}
	/**
	 * @param value the Application ID in partner store
	 */
	public void setAppId(String value)
	{
		appId = value;
	}
	/**
	 * @param value the app private key in partner store
	 */
	public void setAppKey(String value)
	{
		appKey = value;
	}
	/**
	 * @return the Application Id on partner store
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @return Base64 Encoded app key, used by Huawei store
	 */
	public String getAppKey() {
		return appKey;
	}

	public String toString()
	{
		String result = null;
	#if !RELEASE_VERSION
		result = "****************ShopProfile*****************\n";
		result += "Country Id: \'" + countryId + "\' [" + countryValue + "]";
		result += "Operator Id: \'" + operatorId + "\' [" + operatorValue + "]";
		result += "Product Id: \'" + productId + "\' [" + productValue + "]";
		result += "Lan Id: \'" + langId + "\' [" + langValue + "]";
		Enumeration <String> list = mItemsMap.keys();
		while (list.hasMoreElements())
		{	
			String value = list.nextElement();
			result += "\n----------------------List Type: \'" + value + "\'---------------------------";
			ArrayList<cItem> items = mItemsMap.get(value);
			for (int i = 0; i < items.size(); i++)
			{
				result += "\n"+items.get(i).toString();
			}
		}
	#endif	
		return result;
	}
	
}

class cBilling
{

	public cBilling()	{ }
	
	private String billing_type 	= "";

	
	private Hashtable<String, String> mAttributes = new Hashtable<String, String>();
	
	/**
	 * @params the name and value of the billing to add/replace
	*/
	public void addAttribute(String name, String value)
	{
		if (name != null && value != null)
		{
			if (value.equals(InAppBilling.GET_STR_CONST(IAB_FORMATTED_PRICE))||
				value.equals(InAppBilling.GET_STR_CONST(IAB_CURRENCY_SIMBOL))||
				value.equals(InAppBilling.GET_STR_CONST(IAB_CURRENCY)))
			{
				value = android.text.Html.fromHtml(value).toString();
			}	
			mAttributes.put(name, value);
		}
	}
	
		/**
	 * @return the Billing attribute using the name argument
	 * @param name the billing attribute name to search for
	*/
	public String getAttributeByName(String name)
	{
		if (!mAttributes.isEmpty() && mAttributes.containsKey(name))
		{
			return mAttributes.get(name);
		}
		return ""; 
	}
	
	/**
	 * @return the cBilling billing_type
	 */
	public String getBillingType() {
		return billing_type;
	}
	
	/**
	 * @param value the cBilling billing_type to set
	 */
	public void setBillingType(String value) {
		billing_type = value;
	}
		
	public String toString()
	{
		String result = null;
	#if !RELEASE_VERSION
		result = "Type: \'" + billing_type + "' \n Billing Attributes:";
		Enumeration <String> list = mAttributes.keys();
		while (list.hasMoreElements())
		{
			String k = list.nextElement();
			result += "\nName: " + k + " \'" + mAttributes.get(k)+"\'";
		}
	#endif
		return result;		
	}
}

class cItem {

	public cItem()	{ }
	
	private String type_pref	= "";
	private String type 		= "";
	private String id	 		= "";
    private String tracking_id  = "";//generic tracker for purchases
	
	private Hashtable<String, String> mAttributes = new Hashtable<String, String>();
	
	private Hashtable<String, cBilling> mBillingOpts = new Hashtable<String, cBilling>();
	
	
	/**
	 * @params the name and value of the attribute to add/replace
	*/
	public void addAttribute(String name, String value)
	{
		if (name != null && value != null)
		{
			if (value.equals(InAppBilling.GET_STR_CONST(IAB_FORMATTED_PRICE))||
				value.equals(InAppBilling.GET_STR_CONST(IAB_CURRENCY_SIMBOL))||
				value.equals(InAppBilling.GET_STR_CONST(IAB_CURRENCY)))
			{	
				value = android.text.Html.fromHtml(value).toString();
			}	
			mAttributes.put(name, value);
		}
	}
	
		/**
	 * @return the attribue value using the name argument
	 * @param name the name of the attribute to search for
	*/
	public String getAttributeByName(String name)
	{
		if (!mAttributes.isEmpty() && mAttributes.containsKey(name))
		{
			return mAttributes.get(name);
		}
		return ""; 
	}
	
	/**
	 * @param the billing instance to add
	*/
	public void addBilling(cBilling billing)
	{
		if (billing != null)
		{
			mBillingOpts.put(billing.getBillingType(), billing);
		}
	}
	
	/**
	 * @return the default cBilling using the current type_pref
	*/
	public cBilling getDefaultBilling()
	{
		return getBillingByType(type_pref);
	}
	
	/**
	 * @return the cBilling using the type argument
	 * @param type the cItem type to search for
	*/
	public cBilling getBillingByType(String tp)
	{
		if (!mBillingOpts.isEmpty())
		{
			return mBillingOpts.get(tp);
		}
		return null; 
	}
	
	/**
	 * @return the billing options supported
	 */   	
	public int getBillingOptsCount()
	{
		return (mBillingOpts==null)?0:mBillingOpts.size();
	}
	
	/**
	 * @return the cItem id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @param value the cItem id to set
	 */
	public void setId(String value) {
		id = value;
	}

    /**
	 * @return the cItem tracking_id
	 */
	public String getTrackingId() {
		return tracking_id;
	}//generic tracker for purchases
	
	/**
	 * @param value the cItem tracking_id to set
	 */
	public void setTrackingId(String value) {
		tracking_id = value;
	}//generic tracker for purchases
	
	/**
	 * @return the cItem type_pref
	 */
	public String getType_pref() {
		return type_pref;
	}
	
	/**
	 * @param value the cItem type_pref to set
	 */
	public void setType_pref(String value) {
		type_pref = value;
	}
	
	/**
	 * @return the cItem type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param value the cItem type to set
	 */
	public void setType(String value) {
		type = value;
	}
	
	
	public String toString()
	{
		String result = null;
	#if !RELEASE_VERSION
		result = "************************Item*************************\n" +"Id: \'" + id + "\' Type: \'" + type + "\' Type_pref: \'" + type_pref+"\'";
		Enumeration <String> list = mAttributes.keys();
		while (list.hasMoreElements())
		{
			String k = list.nextElement();
			result += "\nName: " + k + " \'" + mAttributes.get(k)+"\'";
		}
		list = mBillingOpts.keys();
		cBilling billing = null;
		while (list.hasMoreElements())
		{	
			result += "\n-----Billing-------";
			String k = list.nextElement();
			billing = mBillingOpts.get(k);
			result += "\n" + billing.toString();
		}
	#endif
		return result;
	}
}
#endif//USE_IN_APP_BILLING