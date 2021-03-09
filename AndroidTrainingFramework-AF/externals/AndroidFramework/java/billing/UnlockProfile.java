#if USE_BILLING
package APP_PACKAGE.billing;

import java.util.ArrayList;
import java.util.HashMap;

public class UnlockProfile {
	
	private int operator_id = -1;
	private int platform_id = -1;
	private int promo_code = -1;
	private String billing_type = null;
	private String promo_code_url = null;
	private ArrayList<Profile> profileList = null;
	private HashMap<String, String> noProfileMessages = null;
	private int country_id = -1;
	
#if USE_BOKU_FOR_BILLING
	private String service_id = null;
	private String price_with_taxes = null;
	private String formated_price = null;
#endif	
	
	public UnlockProfile() {
		profileList = new ArrayList<Profile>();
	}
	
	/**
	 * @return the country_id
	 */
	public int getCountry_id() {
		return country_id;
	}

	/**
	 * @param countryId the country_id to set
	 */
	public void setCountry_id(int countryId) {
		country_id = countryId;
	}

	/**
	 * @return the operator_id
	 */
	public int getOperator_id() {
		return operator_id;
	}

	/**
	 * @param operatorId the operator_id to set
	 */
	public void setOperator_id(int operatorId) {
		operator_id = operatorId;
	}

	/**
	 * @return the platform_id
	 */
	public int getPlatform_id() {
		return platform_id;
	}

	/**
	 * @param platformId the platform_id to set
	 */
	public void setPlatform_id(int platformId) {
		platform_id = platformId;
	}

	/**
	 * @return the billing_type
	 */
	public String getBilling_type() {
		return billing_type;
	}

	/**
	 * @param billingType the billing_type to set
	 */
	public void setBilling_type(String billingType) {
		billing_type = billingType;
	}
	
	/**
	 * @return the promo_code
	 */
	public int getPromoCode() {
		return promo_code;
	}

	/**
	 * @param billingType the billing_type to set
	 */
	public void setPromoCode(int promoCode) {
		promo_code = promoCode;
	}
	
	/**
	 * @return the billing_type
	 */
	public String getPromoCode_URL() {
		return promo_code_url;
	}

	/**
	 * @param billingType the billing_type to set
	 */
	public void setPromoCode_URL(String promoCodeURL) {
		promo_code_url = promoCodeURL;
	}
	
	/**
	 * @return the profileList
	 */
	public ArrayList<Profile> getProfileList() {
		return profileList;
	}

	/**
	 * @param profileList the profileList to set
	 */
	public void setProfileList(ArrayList<Profile> profileList) {
		this.profileList = profileList;
	}

#if USE_BOKU_FOR_BILLING	
	public String getServiceId() {
		return service_id;
	}
	
	public void setServiceId(String serviceId) {
		service_id = serviceId;
	}
	
	public String getPriceWithTaxes() {
		return price_with_taxes;
	}
	
	public void setPriceWithTaxes(String priceWithTaxes) {
		price_with_taxes = priceWithTaxes;
	}
	
	public String getFormatedPrice() {
		return formated_price;
	}
	
	public void setFormatedPrice(String formatedPrice) {
		formated_price = formatedPrice;
	}
#endif	


	public HashMap<String, String> getNoProfileMsg() {		
		return noProfileMessages;
	}
	
	public String getNoProfileMsg(String lang) {
		if (noProfileMessages.containsKey(lang))
			return noProfileMessages.get(lang);
		else if (noProfileMessages.containsKey("en"))
			return noProfileMessages.get("en");
		else
			return "";
	}

	public void setNoProfileMsg(String key, String value) {		
		if (noProfileMessages == null)
			noProfileMessages = new HashMap<String, String>();
		noProfileMessages.put(key, value);
	}
}

#endif//USE_BILLING