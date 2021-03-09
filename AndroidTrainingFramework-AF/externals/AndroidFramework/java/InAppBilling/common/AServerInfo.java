package APP_PACKAGE.billing.common;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import APP_PACKAGE.GLUtils.Carrier;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.R;

public abstract class AServerInfo {
	protected SAXParserFactory spf;
	protected SAXParser sp;
	protected XMLReader xr;
	
	protected Device mDevice;
	protected XPlayer mXPlayer;

	public abstract boolean getUpdateProfileInfo ();
	public boolean searchForDefaultBillingMethod (String itemId) { return false; }
	public boolean searchForAditionalProfile (String profileNeeded) { return false; }
	public String getItemAttribute (String itemId, String attribute) { return null; }
	public String getBillingAttribute (String attribute) { return null; }
	public String getBillingAttribute (String itemId, String attribute) { return null; }
	public boolean setBillingMethod (String billingType) { return false; }
	public String getSelectedItem () { return null; }
	public abstract String getGamePrice ();
	public String getItemPriceFmt () { return null; }
	public String getProfileId () { return null; }
	public String getPSMSType () { return null; }

	public String getPurchaseConfirmString() { return null; }
	public String getPurchaseOkString() { return null; }
	public String getPurchaseLaterString() { return null; }
	public String getTNC_Title() { return null; }
	public String getTNC_Title_Shop() { return null; }
	
	public String getServerNumber () { return null; }
	public String getShortCode(int target){ return null; }
	public String getUMPTIdURL(){ return null; }
	public String getUMPBillingURL(){ return null; }
	public int getPromoCode () { return -1; }
	public String getPromoCodeURL() { return null; }
	public String getAlias () { return null;}
	public abstract String getLanguage ();
	public abstract String getTNCString ();
	public String getFlowType () { return null; }
	public String getStringCurrencyValue () { return null; }
	public int getIntegerCurrencyValue () { return -1; }
	public abstract String getURLbilling ();
	#if SHENZHOUFU_STORE
	public String getShenzhoufuURLbilling () { return null; }
	public String getShenzhoufuURLresult () { return null; }
	#endif
	public String getMoneyBilling () { return null; } 
	public String getIs3GOnly () { return "1"; }
	public String getAIDBilling () { return null; }
	public String getPIDBilling () { return null; }
	public String getIIDBilling () { return null; }
	public String getItemUID () { return null; }
	public abstract String getPlatformId ();
	public String getLangId () { return null; }
	public String getItemServiceID () { return null; }
	public String getItemPriceWithTaxes () { return null; }
	public abstract boolean isProfileSelected();
	public abstract String getBillingType ();
	public abstract String getProfileType ();
}