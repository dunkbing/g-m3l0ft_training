package APP_PACKAGE.billing;

import java.util.ArrayList;

import APP_PACKAGE.GLUtils.Carrier;
import APP_PACKAGE.GLUtils.SUtils;

public class Profile {
	private int id = 0;
	private String name = null;
	private String status = null;
	private String country = null;
	private ArrayList<String> mcc = new ArrayList<String>();
	private ArrayList<Carrier> carrier = new ArrayList<Carrier>();
	private int type = 0;
	private SpecialStrings lang_strings = new SpecialStrings();
	private int currency_value = 0;
	private String currency_symbol = null;
	private String delivery_type = null;
	private String game_price = null;
	private String alias = null;
	private int double_option = 0;
	private int promo_code = 0;
	private String server_number = null;
	private String url_billing = null;
	private String promo_code_URL = null;
	private String buyinfo = null;
	private String MRC_details = null;
	private String MRC_buyinfo = null;
	private String MRC_discount = null;
	private boolean isCCprofile = false;
	private boolean isHTTPprofile = false;
	private boolean isMRCprofile = false;
	private boolean isSMSprofile = false;
	private boolean isWAPprofile = false;
	private boolean isWAPOTHERprofile = false;
	
#if USE_BILLING && USE_BOKU_FOR_BILLING
	private boolean isBOKUprofile = false;
	private String service_id = null;
	private String price_with_taxes = null;
	private String formated_price = null;
#endif

#if USE_BILLING_FOR_CHINA
	private boolean isIPXprofile = false;
	private String IpxUserName;
	private String IpxPassword;
	private String IpxUrlLookup;
	private String IpxProductID;
	private String IpxPinCode;
	private String IpxChannel;
	// For IPX lookup profile
	private int IpxMessagingMode;
	private String IpxTransactionId;
	private int IpxResponseCode;
	private int IPXPaidMoney;
	private int IPXSentCount;
	private ArrayList<IPXItem> IPXItemList = new ArrayList<IPXItem> ();
	
	private boolean isUMPprofile = false;
	private String UmpMessageAddress1;
	private String UmpMessageAddress2;
	private String UmpMessage;
	private String Ump_get_transid; 
	private String Ump_useable;
	private String UmpMerid;
	private String UmpGoodsid;
	private String UmpGoodsAmount;
	private String Ump_get_billing_result;
#endif
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	/**
	 * @return the mcc
	 */
	public ArrayList<String> getMcc() {
		return mcc;
	}
	/**
	 * @param mcc the mcc to set
	 */
	public void setMcc(ArrayList<String> mcc) {
		this.mcc = mcc;
	}
	/**
	 * @return the carrier
	 */
	public ArrayList<Carrier> getCarrier() {
		return carrier;
	}
	/**
	 * @param carrier the carrier to set
	 */
	public void setCarrier(ArrayList<Carrier> carrier) {
		this.carrier = carrier;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the lang_strings
	 */
	public SpecialStrings getLang_strings() {
		return lang_strings;
	}
	/**
	 * @param langStrings the lang_strings to set
	 */
	public void setLang_strings(SpecialStrings langStrings) {
		lang_strings = langStrings;
	}
	/**
	 * @return the currency_value
	 */
	public int getCurrency_value() {
		return currency_value;
	}
	/**
	 * @param currencyValue the currency_value to set
	 */
	public void setCurrency_value(int currencyValue) {
		currency_value = currencyValue;
	}
	/**
	 * @return the currency_symbol
	 */
	public String getCurrency_symbol() {
		return currency_symbol;
	}
	/**
	 * @param currencySymbol the currency_symbol to set
	 */
	public void setCurrency_symbol(String currencySymbol) {
		currency_symbol = currencySymbol;
	}
	/**
	 * @return the delivery_type
	 */
	public String getDelivery_type() {
		return delivery_type;
	}
	/**
	 * @param deliveryType the delivery_type to set
	 */
	public void setDelivery_type(String deliveryType) {
		delivery_type = deliveryType;
	}
	/**
	 * @return the game_price
	 */
	public String getGame_price() {
		return game_price;
	}
	/**
	 * @return the game_price + currency Symbol
	 */
	public String getGamePriceFmt() {
		if (game_price == null || currency_symbol == null )
			return null;
		return game_price+currency_symbol;
	}
	/**
	 * @param gamePrice the game_price to set
	 */
	public void setGame_price(String gamePrice) {
		game_price = gamePrice;
	}
	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}
	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	/**
	 * @return the double_option
	 */
	public int getDouble_option() {
		return double_option;
	}
	/**
	 * @param doubleOption the double_option to set
	 */
	public void setDouble_option(int doubleOption) {
		double_option = doubleOption;
	}
	/**
	 * @return the promo_code
	 */
	public int getPromoCode() {
		return promo_code;
	}
	/**
	 * @param promoCode the promo_code to set
	 */
	public void setPromoCode(int promoCode) {
		promo_code = promoCode;
	}
	/**
	 * @return the promo_code_URL
	 */
	public String getPromoCode_URL() {
		return promo_code_URL;
	}
	/**
	 * @return the buyinfo
	 */
	public String getBuyInfo() {
		return buyinfo;
	}
	/**
	 * @return the MRC_buyinfo
	 */
	public String getMRCBuyInfo() {
		return MRC_buyinfo;
	}
	/**
	 * @return the MRC_discount
	 */
	public String getMRCDiscount() {
		return MRC_discount;
	}
	/**
	 * @return the MRC_details
	 */
	public String getMRCDetails() {
		return MRC_details;
	}
	/**
	 * @param promoCode_URL the promo_code_URL on String format to set
	 */
	public void setPromoCode_URL(String promoCode_URL) {
		promo_code_URL = promoCode_URL;
	}
	/**
	 * @param promoCode_URL the promo_code_URL on String format to set
	 */
	public void setBuyInfo(String buy_info) {
		buyinfo = buy_info;
	}
	/**
	 * @param promoCode_URL the promo_code_URL on String format to set
	 */
	public void setMRCBuyInfo(String MRC_buy_info) {
		MRC_buyinfo = MRC_buy_info;
	}
	/**
	 * @param promoCode_URL the promo_code_URL on String format to set
	 */
	public void setMRCDiscount(String MRC_Discount) {
		MRC_discount = MRC_Discount;
	}
	/**
	 * @param promoCode_URL the promo_code_URL on String format to set
	 */
	public void setMRCDetails(String MRC_Details) {
		MRC_details = MRC_Details;
	}
	/**
	 * @return the server_number
	 */
	public String getServer_number() {
		return server_number;
	}
	/**
	 * @param serverNumber the server_number on String format to set
	 */
	public void setServer_number(String serverNumber) {
		server_number = serverNumber;
	}
	/**
	 * @return the url_billing
	 */
	public String getUrl_billing() {
		return url_billing;
	}
	/**
	 * @param urlBilling the url_billing to set
	 */
	public void setUrl_billing(String urlBilling) {
		url_billing = urlBilling;
	}
#if USE_BILLING && USE_BOKU_FOR_BILLING	
	public String getServiceId() {
		return service_id;
	}
	
	public void setServiceId(String service_id) {
		this.service_id = service_id;
	}
	
	public String getPriceWithTaxes() {
		return price_with_taxes;
	}
	
	public void setPriceWithTaxes(String price_with_taxes) {
		this.price_with_taxes = price_with_taxes;
	}
	
	public String getFormatedPrice() {
		return formated_price;
	}
	
	public void setFormatedPrice(String formated_price) {
		this.formated_price = formated_price;
	}
#endif

#if USE_BILLING_FOR_CHINA
	//  IPX profile
	public void setIPXprofile(boolean isIPXprofile) {
		this.isIPXprofile = isIPXprofile;
	}
	
	public boolean isIPXprofile() {
		return isIPXprofile;
	}
	
	public void setIpxUserName(String userName) {
		this.IpxUserName = userName;
	}
	
	public String getIpxUserName() {
		return IpxUserName;
	}
	
	public void setIpxPassword(String password) {
		this.IpxPassword = password;
	}

	public String getIpxPassword() {
		return IpxPassword;
	}
	
	public void setIpxUrlLookup(String urlLookup) {
		this.IpxUrlLookup = urlLookup;
	}

	public String getIpxUrlLookup() {
		return IpxUrlLookup;
	}
	
	public void setIpxProductID(String productID) {
		this.IpxProductID = productID;
	}

	public String getIpxProductID() {
		return IpxProductID;
	}

	public void setIpxPinCode(String pinCode) {
		this.IpxPinCode = pinCode;
	}

	public String getIpxPinCode() {
		return IpxPinCode;
	}
	
	public void setIpxChannel(String chanel) {
		this.IpxChannel = chanel;
	}

	public String getIpxChannel() {
		return IpxChannel;
	}
	// IPX lookup
	public void setIpxMessagingMode(int messagingMode) {
		this.IpxMessagingMode = messagingMode;
	}
	
	public int getIpxMessagingMode() {
		return IpxMessagingMode;
	}
	
	public void setIpxTransactionId(String transactionId) {
		this.IpxTransactionId = transactionId;
	}
	
	public String getIpxTransactionId() {
		return IpxTransactionId;
	}
	
	public void setIpxResponseCode(int responseCode) {
		this.IpxResponseCode = responseCode;
	}
	
	public int getIpxResponseCode() {
		return IpxResponseCode;
	}
	
	public void setIPXItemList(ArrayList<IPXItem> IPXItemList) {
		this.IPXItemList = IPXItemList;
	}
	
	public ArrayList<IPXItem> getIPXItemList() {
		return IPXItemList;
	}
	
	public int getIPXSMSCount() {
		return IPXItemList.size();
	}
	
	public void setIPXPaidMoney( int money)
	{
		this.IPXPaidMoney = money;
		SUtils.setPreference(SUtils.getLManager().PREFERENCES_SMS_IPX_MONEY, IPXPaidMoney, SUtils.getLManager().PREFERENCES_NAME);
	}
	
	public int getIPXPaidMoney()
	{
		return IPXPaidMoney;
	}
	
	public void addIPXPaidMoney(int amount)
	{
		setIPXPaidMoney(this.IPXPaidMoney + amount);
	}
	
	public void setIPXSentCount(int sent)
	{
		this.IPXSentCount = sent;
		SUtils.setPreference(SUtils.getLManager().PREFERENCES_SMS_IPX_COUNT, IPXSentCount, SUtils.getLManager().PREFERENCES_NAME);
	}
	
	public int getIPXSentCount()
	{
		return IPXSentCount;
	}
	
	public void addIPXSentCount(int sent)
	{
		setIPXSentCount(this.IPXSentCount + sent);
	}
	
	// UMP profile	
	public void setUMPprofile(boolean isUMPprofile) {
		this.isUMPprofile = isUMPprofile;
	}
	
	public boolean isUMPprofile() {
		return isUMPprofile;
	}
	
	public void setUmpMessageAddress1(String messageAddress1) {
		this.UmpMessageAddress1 = messageAddress1;
	}

	public String getUmpMessageAddress1() {
		return UmpMessageAddress1;
	}

	public void setUmpMessageAddress2(String messageAddress2) {
		this.UmpMessageAddress2 = messageAddress2;
	}

	public String getUmpMessageAddress2() {
		return UmpMessageAddress2;
	}

	public void setUmpMessage(String message) {
		this.UmpMessage = message;
	}

	public String getUmpMessage() {
		return UmpMessage;
	}
	// UMP R4
	public void setUmp_get_transid(String url) {
		Ump_get_transid = url;
	}

	public String getUmp_get_transid() {
		return Ump_get_transid;
	}

	public String getUmp_useable() {
		return Ump_useable;
	}

	public void setUmp_useable(String ump_useable) {
		this.Ump_useable = ump_useable;
	}

	public String getUmpMerid() {
		return UmpMerid;
	}

	public void setUmpMerid(String merid) {
		this.UmpMerid = merid;
	}

	public String getUmpGoodsid() {
		return UmpGoodsid;
	}

	public void setUmpGoodsid(String goodsid) {
		this.UmpGoodsid = goodsid;
	}

	public String getUmpGoodsAmount() {
		return UmpGoodsAmount;
	}

	public void setUmpGoodsAmount(String goods_amount) {
		this.UmpGoodsAmount = goods_amount;
	}

	public String getUmp_get_billing_result() {
		return Ump_get_billing_result;
	}

	public void setUmp_get_billing_result(String ump_get_billing_result) {
		this.Ump_get_billing_result = ump_get_billing_result;
	}
#endif
	
	/**
	 * @return the isCCprofile
	 */
	public boolean isCCprofile() {
		return isCCprofile;
	}
	/**
	 * @param isCCprofile the isCCprofile to set
	 */
	public void setCCprofile(boolean isCCprofile) {
		this.isCCprofile = isCCprofile;
	}
	/**
	 * @return the isHTTPprofile
	 */
	public boolean isHTTPprofile() {
		return isHTTPprofile;
	}
	/**
	 * @return the isMRCprofile
	 */
	public boolean isMRCprofile() {
		return isMRCprofile;
	}
	/**
	 * @param isHTTPprofile the isHTTPprofile to set
	 */
	public void setHTTPprofile(boolean isHTTPprofile) {
		this.isHTTPprofile = isHTTPprofile;
	}
	/**
	 * @param isMRCprofile the isHTTPprofile to set
	 */
	public void setMRCprofile(boolean isMRCprofile) {
		this.isMRCprofile = isMRCprofile;
	}
	/**
	 * @return the isSMSprofile
	 */
	public boolean isSMSprofile() {
		return isSMSprofile;
	}
	/**
	 * @param isSMSprofile the isSMSprofile to set
	 */
	public void setSMSprofile(boolean isSMSprofile) {
		this.isSMSprofile = isSMSprofile;
	}
	/**
	 * @return the isWAPprofile
	 */
	public boolean isWAPprofile() {
		return isWAPprofile;
	}
	/**
	 * @return the isWAPOTHERprofile
	 */
	public boolean isWAPOTHERprofile() {
		return isWAPOTHERprofile;
	}
	/**
	 * @param isWAPprofile the isWAPprofile to set
	 */
	public void setWAPprofile(boolean isWAPprofile) {
		this.isWAPprofile = isWAPprofile;
	}
	/**
	 * @param isWAPOTHERprofile the isWAPOTHERprofile to set
	 */
	public void setWAPOTHERprofile(boolean isWAPOTHERprofile) {
		this.isWAPOTHERprofile = isWAPOTHERprofile;
	}
#if USE_BILLING && USE_BOKU_FOR_BILLING	
	public boolean isBOKUprofile() {
		return isBOKUprofile;
	}
	public void setBOKUprofile(boolean isBOKUprofile) {
		this.isBOKUprofile = isBOKUprofile;
	}
#endif
}


class SpecialStrings {
	private String language = "EN";//DE,ES,FR,IT
	private String tnc = null;
	private String buyscreen = null;
	private String ompd_ed_sc = null;
	private String ompb_sc = null;
	private String ompb_ed_mc = null;
	private String ompb_mcc = null;
	private String support = null;
	private String double_option = null;
	private String double_option_text_1 = null;
	private String double_option_text_2 = null;
	
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	/**
	 * @return the tnc
	 */
	public String getTnc() {
		return tnc;
	}
	/**
	 * @param tnc the tnc to set
	 */
	public void setTnc(String tnc) {
		this.tnc = tnc;
	}
	/**
	 * @return the buyscreen
	 */
	public String getBuyscreen() {
		return buyscreen;
	}
	/**
	 * @param buyscreen the buyscreen to set
	 */
	public void setBuyscreen(String buyscreen) {
		this.buyscreen = buyscreen;
	}
	/**
	 * @return the ompd_ed_sc
	 */
	public String getOmpd_ed_sc() {
		return ompd_ed_sc;
	}
	/**
	 * @param ompdEdSc the ompd_ed_sc to set
	 */
	public void setOmpd_ed_sc(String ompdEdSc) {
		ompd_ed_sc = ompdEdSc;
	}
	/**
	 * @return the ompb_sc
	 */
	public String getOmpb_sc() {
		return ompb_sc;
	}
	/**
	 * @param ompbSc the ompb_sc to set
	 */
	public void setOmpb_sc(String ompbSc) {
		ompb_sc = ompbSc;
	}
	/**
	 * @return the ompb_ed_mc
	 */
	public String getOmpb_ed_mc() {
		return ompb_ed_mc;
	}
	/**
	 * @param ompbEdMc the ompb_ed_mc to set
	 */
	public void setOmpb_ed_mc(String ompbEdMc) {
		ompb_ed_mc = ompbEdMc;
	}
	/**
	 * @return the ompb_mcc
	 */
	public String getOmpb_mcc() {
		return ompb_mcc;
	}
	/**
	 * @param ompbMcc the ompb_mcc to set
	 */
	public void setOmpb_mcc(String ompbMcc) {
		ompb_mcc = ompbMcc;
	}
	/**
	 * @return the support
	 */
	public String getSupport() {
		return support;
	}
	/**
	 * @param support the support to set
	 */
	public void setSupport(String support) {
		this.support = support;
	}
	/**
	 * @return the double_option
	 */
	public String getDouble_option() {
		return double_option;
	}
	/**
	 * @param doubleOption the double_option to set
	 */
	public void setDouble_option(String doubleOption) {
		double_option = doubleOption;
	}
	/**
	 * @return the double_option_text_2
	 */
	public String getDouble_option_text_2() {
		return double_option_text_2;
	}
	/**
	 * @param doubleOptionText1 to set the value for double_option_text_2
	 */
	public void setDouble_option_Text_2(String doubleOptionText2) {
		double_option_text_2 = doubleOptionText2;
	}
}
