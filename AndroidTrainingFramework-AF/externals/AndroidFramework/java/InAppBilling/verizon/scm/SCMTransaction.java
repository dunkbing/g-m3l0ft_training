#if VZW_STORE && VZW_SCM
package APP_PACKAGE.iab;

import APP_PACKAGE.iab.VzwConnect;
import APP_PACKAGE.iab.VzwGetNpost;

import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;

//XML
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.content.Context;
import android.telephony.TelephonyManager;
import java.util.Random;

public class SCMTransaction
{
	SET_TAG("SCMTransaction");

	//SCM Connections
	private VzwConnect vzwConnect;
	//SCM API interface
	private VzwGetNpost GNP;
	private XPlayer mXPlayer;
	
	//XML
	private SAXParserFactory spf;
	private SAXParser sp;
	private XMLReader xr;
	private VXMLParser xmlParser;
	
	final static int STATUS_SUCCESS						= 0;
	final static int STATUS_ERROR_CODE					= -1;
	final static int STATUS_ERROR_SERVER_RESPONSE_EMPTY	= -2;
	final static int STATUS_ERROR_SERVER_GL				= -3;
	final static int STATUS_ERROR_NO_DATA_CONNECTION	= -4;
	public static int mStatus;
	
	String current_id;
	
	protected static boolean firstime = true;
	private static boolean wasWiFiEnabled = false;
	public static boolean isRetryConnection = false;
	
	public SCMTransaction(XPlayer xplayer)
	{
		DBG(TAG,"[SCMTransaction]");
		mXPlayer = xplayer;
		try{
			spf = SAXParserFactory.newInstance();
			sp = spf.newSAXParser();
			xr = sp.getXMLReader();
				
			xmlParser = new VXMLParser();
			xr.setContentHandler(xmlParser);
			
			current_id = null;
		}catch(Exception e){
			ERR(TAG,"XML Server Parsing Exception = " + e);
		}
	}
	
	public int getStatus(){		return mStatus;		}
	private int setStatus(){		return mStatus;		}
	
	private void init()
	{
		DBG(TAG,"[init]");
		vzwConnect = null;
		GNP = null;
		
		vzwConnect = new VzwConnect();
		GNP = new VzwGetNpost();
	}
	
	public void processTransaction(String verizon_itemID, String PPPID, String price)
	{
		DBG(TAG,"[processTransaction]");
		current_id = verizon_itemID;
		init();
		
		if(!isRetryConnection)
			wasWiFiEnabled = false;
		RequestLogin();
		requestCheckout(verizon_itemID, PPPID, price);
	}
	
	/*
	*	HSStartUp SCM API call.
	*/
	private void RequestLogin()
	{
		DBG(TAG,"[RequestLogin]");
		if (!validateConnection()){
			mStatus = STATUS_ERROR_NO_DATA_CONNECTION;
			return;
		}
		INFO(TAG,"Reques Login");
		
		vzwConnect.X_MOD_RF = "";
		vzwConnect.X_MOD_SC = "";
		vzwConnect.X_MOD_SS = "";
		vzwConnect.X_MOD_RF = null;
		vzwConnect.X_MOD_SC = null;
		vzwConnect.X_MOD_SS = null;
		
		GNP.MDN = mXPlayer.getDevice().getLineNumber();
		
		//test values
		if (GNP.MDN == null || GNP.MDN.equals("00"))
			GNP.MDN = "0000000000";
		
		if (GNP.DeviceModel == null)
			GNP.DeviceModel = "PROTOTYPE";
		
		
		// To cross the Device Model and if there is space in 
		// target not to add it to the new Device Model
		String DeviceModelWithoutBlanks="";
		for (int x=0; x < GNP.DeviceModel.length(); x++) {
			if (GNP.DeviceModel.charAt(x) != ' ')
				DeviceModelWithoutBlanks += GNP.DeviceModel.charAt(x);
		}
		GNP.DeviceModel = DeviceModelWithoutBlanks;
		
		if (GNP.FirmwareVersion == null)
			GNP.FirmwareVersion = "TESTFW";
			
		
		vzwConnect.setsUrl(null);
		
		INFO(TAG,"GNP.HSStartUP "+GNP.HSStartUP);
		INFO(TAG,"GNP.MDN "+GNP.MDN);
		INFO(TAG,"GNP.DeviceModel "+GNP.DeviceModel);
		INFO(TAG,"GNP.FirmwareVersion "+GNP.FirmwareVersion);
		INFO(TAG,"GNP.HandsetClientVers "+GNP.HandsetClientVers);
		INFO(TAG,"GNP.CommProtocolVers "+GNP.CommProtocolVers);
		INFO(TAG,"GNP.CompressionLevel "+GNP.CompressionLevel);
		INFO(TAG,"GNP.ReferralId "+GNP.ReferralId);
		INFO(TAG,"GNP.Unknown "+GNP.Unknown);
		
		vzwConnect.connect(
				"%" + GNP.userAgent +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.HSStartUP ).substring(1) +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.MDN.length() ).substring(1) + GNP.MDN +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.DeviceModel.length() ).substring(1) + GNP.DeviceModel +	// REQUIRED - STRING             /* "07ADR6300" +      */
				"%" + Integer.toHexString( 0x100 | GNP.FirmwareVersion.length() ).substring(1) + GNP.FirmwareVersion +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.HandsetClientVers.length() ).substring(1) + GNP.HandsetClientVers +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.CommProtocolVers.length() ).substring(1) + GNP.CommProtocolVers +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.CompressionLevel.length() ).substring(1) + GNP.CompressionLevel +	// OPTIONAL - STRING
				"%" + Integer.toHexString( 0x100 | GNP.ReferralId.length() ).substring(1) + GNP.ReferralId +	// CONDITIONAL - STRING
				"%" + Integer.toHexString( 0x100 | GNP.Unknown.length() ).substring(1) + GNP.Unknown	// DON'T KNOW (BUT NOT NECESSARY) - STRING
				);
		vzwConnect.sendRequest();
		vzwConnect.handleProcess();
		
		if (!vzwConnect.getsResponse().equals(""))
		{
			InputSource is = new InputSource(new ByteArrayInputStream(vzwConnect.getsResponse().getBytes()));
			parseXML(is);
			
			if(isErrorCode(GNP.Code))
				mStatus = STATUS_ERROR_CODE;
			else
				mStatus = STATUS_SUCCESS;
		}else
			mStatus = STATUS_ERROR_SERVER_RESPONSE_EMPTY;
		
	}
	
	/*
	*	GetView SCM API call.
	*/
	private void requestGameData(String viewParam)
	{
		DBG(TAG,"[requestGameData]");
		vzwConnect.setsUrl(null);
		if(firstime)
		{
			Random r = new Random();
			vzwConnect.X_MOD_RF = Integer.toString(Math.abs(r.nextInt()), 9);
			vzwConnect.X_MOD_SS = vzwConnect.md5(vzwConnect.X_MOD_SC + vzwConnect.X_MOD_RF + GNP.secretString + GNP.HandsetClientVers_MD5 + GNP.CommProtocolVers_MD5);
			firstime = false;
		}
		
		GNP.ViewParam = viewParam;
		
		INFO(TAG,"GNP.GetView "+GNP.GetView);
		INFO(TAG,"GNP.ViewID "+GNP.ViewID);
		INFO(TAG,"GNP.ViewParam "+GNP.ViewParam);
		
		vzwConnect.connect(
				"%" + GNP.userAgent +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.GetView ).substring(1) +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.ViewID.length() ).substring(1) + GNP.ViewID +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.ViewParam.length() ).substring(1) + GNP.ViewParam	// REQUIRED - String
			);
		vzwConnect.sendRequest();
		vzwConnect.handleProcess();
		
		if(!vzwConnect.getsResponse().equals(""))
		{
			INFO(TAG, "requestGameData RESPONSE: "+vzwConnect.getsResponse());
			InputSource is = new InputSource(new ByteArrayInputStream(vzwConnect.getsResponse().getBytes()));
			parseXML(is);
			
			if(isErrorCode(GNP.Code))
				mStatus = STATUS_ERROR_CODE;
			else
				mStatus = STATUS_SUCCESS;
		}else
			mStatus = STATUS_ERROR_SERVER_RESPONSE_EMPTY;
	}
	
	private void requestCheckout(String itemID, String PppID, String price)
	{
		DBG(TAG,"[requestCheckout]");
		
		if (!validateConnection()){
			mStatus = STATUS_ERROR_NO_DATA_CONNECTION;
			return;
		}
		
		vzwConnect.setsUrl(null);
		
		GNP.CartLineItem_Item_ItemID = 	itemID;
		if (PppID != null)
		GNP.CartLineItem_Item_PricePlanPackage_PPPID = PppID;
		GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice = price;
		
		//GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice = VZWHelper.GetServerInfo().getGamePrice();
		INFO(TAG, "ItemId="+GNP.CartLineItem_Item_ItemID);
		INFO(TAG, "ItemName=" + GNP.CartLineItem_Item_ItemName);
		INFO(TAG, "PPPID="+GNP.CartLineItem_Item_PricePlanPackage_PPPID);
		INFO(TAG, "price="+GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice);
		
		vzwConnect.connect(
				"%" + GNP.userAgent +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.CheckoutCart ).substring(1) +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.CallPage.length() ).substring(1) + GNP.CallPage +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Category_CatID.length() ).substring(1) + GNP.CartLineItem_Category_CatID +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Category_CatName.length() ).substring(1) + GNP.CartLineItem_Category_CatName +	// OPTIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Category_PricePlanPackage_PPPID.length() ).substring(1) + GNP.CartLineItem_Category_PricePlanPackage_PPPID +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Category_PPPDesc.length() ).substring(1) + GNP.CartLineItem_Category_PPPDesc +	// CONDITIONAL - String
				"%00" + //"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Category_PricePlanPackage_PurchasePrice.length() ).substring(1) + GNP.CartLineItem_Category_PricePlanPackage_PurchasePrice +	// REQUIRED - Double
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Category_IsWishlist.length() ).substring(1) + GNP.CartLineItem_Category_IsWishlist +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Item_ItemID.length() ).substring(1) + GNP.CartLineItem_Item_ItemID +	// REQUIRED - String
				"%00" + //"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Item_ItemName.length() ).substring(1) + GNP.CartLineItem_Item_ItemName +	// OPTIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Item_PricePlanPackage_PPPID.length() ).substring(1) + GNP.CartLineItem_Item_PricePlanPackage_PPPID +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | /*Double.toString(*/GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice/*)*/.length() ).substring(1) + GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice +	// REQUIRED - Double
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Item_IsWishlist.length() ).substring(1) + GNP.CartLineItem_Item_IsWishlist +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Service_ServiceID.length() ).substring(1) + GNP.CartLineItem_Service_ServiceID +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Service_ServiceName.length() ).substring(1) + GNP.CartLineItem_Service_ServiceName +	// OPTIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Service_PPPID.length() ).substring(1) + GNP.CartLineItem_Service_PPPID +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Service_PPPDesc.length() ).substring(1) + GNP.CartLineItem_Service_PPPDesc +	// CONDITIONAL - String
				"%00" + //"%" + Integer.toHexString( 0x100 | Double.toString(GNP.CartLineItem_Service_PurchasePrice).length() ).substring(1) + GNP.CartLineItem_Service_PurchasePrice +	// CONDITIONAL - Double
				"%00" + //"%" + Integer.toHexString( 0x100 | Integer.toString(GNP.CartLineItem_ValuePak_ValuePakId).length() ).substring(1) + GNP.CartLineItem_ValuePak_ValuePakId + // REQUIRED - Integer
				"%00" + //"%" + Integer.toHexString( 0x100 | Double.toString(GNP.CartLineItem_ValuePak_PurchasePrice).length() ).substring(1) + GNP.CartLineItem_ValuePak_PurchasePrice +	// OPTIONAL - Double
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_IsWishlist.length() ).substring(1) + GNP.CartLineItem_IsWishlist +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Discount_Type.length() ).substring(1) + GNP.CartLineItem_Discount_Type +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_Discount_Code.length() ).substring(1) + GNP.CartLineItem_Discount_Code +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.Info_Name.length() ).substring(1) + GNP.Info_Name +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.Info_Value.length() ).substring(1) + GNP.Info_Value +	// CONDITIONAL - String
				"%" + Integer.toHexString( 0x100 | GNP.CartLineItem_ConfirmDupPurchase.length() ).substring(1) + GNP.CartLineItem_ConfirmDupPurchase	// OPTIONAL - String
				);
		vzwConnect.sendRequest();
		vzwConnect.handleProcess();
		
		if(!vzwConnect.getsResponse().equals(""))
		{
			InputSource is = new InputSource(new ByteArrayInputStream(vzwConnect.getsResponse().getBytes()));
			parseXML(is);
			
			if(isErrorCode(GNP.Code))
				mStatus = STATUS_ERROR_CODE;
			else{
				mStatus = STATUS_SUCCESS;
				requestPurchase();
			}
		}else
			mStatus = STATUS_ERROR_SERVER_RESPONSE_EMPTY;
	}
	
	private void requestPurchase()
	{
		DBG(TAG,"[requestPurchase]");
		INFO(TAG, "Request Purchase: "+GNP.ConfirmationID);
		
		if (!validateConnection()){
			mStatus = STATUS_ERROR_NO_DATA_CONNECTION;
			return;
		}
		
		vzwConnect.setsUrl(null);

		// Using all the parameters that have 'B' or 'C' in the "USE" column:
		vzwConnect.connect(
				"%" + GNP.userAgent +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.SubmitPurchase ).substring(1) +		// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.CallPage.length() ).substring(1) + GNP.CallPage +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.ConfirmationID.length() ).substring(1) + GNP.ConfirmationID +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.Type.length() ).substring(1) + GNP.Type	// OPTIONAL - String
				);
		vzwConnect.sendRequest();
		vzwConnect.handleProcess();
		
		if(!vzwConnect.getsResponse().equals(""))
		{
			InputSource is = new InputSource(new ByteArrayInputStream(vzwConnect.getsResponse().getBytes()));
			parseXML(is);
			
			if(isErrorCode(GNP.Code))
				mStatus = STATUS_ERROR_CODE;
			else
			{
				DBG(TAG,"*** Purchase OK ***");
				mStatus = STATUS_SUCCESS;
				enableWiFiConnection();
			}
		}else
			mStatus = STATUS_ERROR_SERVER_RESPONSE_EMPTY;
	}
	
	
	boolean validateConnection()
	{
		DBG(TAG,"[validateConnection]");
		if(IsValidSIM())
		{
			WifiManager mWifiManager = (WifiManager) (SUtils.getContext().getSystemService(Context.WIFI_SERVICE));
			if(mWifiManager.isWifiEnabled())
			{
				DBG(TAG,"Turning OFF Wifi...");
				mWifiManager.setWifiEnabled(false);
				wasWiFiEnabled = true;
				while((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING))
				{
					try {	Thread.sleep(500); } catch(Exception e) {}
				}
			}
		
			//check verizon wireles connection
			ConnectivityManager cm = (ConnectivityManager) SUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			State mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
			long TIME_OUT = 12000;
			while((mobile != State.CONNECTED) && (TIME_OUT>0))
			{
				try {Thread.sleep(500);} catch(Exception e) {}
				mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
				TIME_OUT -= 500;
			}
			
			DBG(TAG,"Mobile Network status: "+mobile);
			
			if(mobile != State.CONNECTED)
			{
				WARN(TAG,"Mobile Network Not Available!!!");
				return false;
			}
			return true;
		}
		else
			return false;
	}
	
	public static void enableWiFiConnection()
	{
		DBG(TAG,"[enableWiFiConnection]");
		DBG(TAG,"wasWiFiEnabled = " + wasWiFiEnabled);
		WifiManager mWifiManager = (WifiManager) (SUtils.getContext().getSystemService(Context.WIFI_SERVICE));
		if(!mWifiManager.isWifiEnabled() && wasWiFiEnabled)
		{
			DBG(TAG,"Turning ON Wifi...");
			mWifiManager.setWifiEnabled(true);
			wasWiFiEnabled = false;
			while((mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING))
			{
				try {	Thread.sleep(500); } catch(Exception e) {}
			}
		}
	}
	
	private boolean IsValidSIM()
	{
		TelephonyManager mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);

			if(mDeviceInfo.getPhoneType() == mDeviceInfo.PHONE_TYPE_CDMA)
				return (true);
			else
				return (mDeviceInfo.getSimState() == mDeviceInfo.SIM_STATE_READY);		
	}
	
	/*
	*	Verizon SCM API Error Codes
	*/
	private boolean isErrorCode(String code)
	{
		if(code == null)
    		return false;
		int error = Integer.parseInt(code);
		if( (error == -1) ||
    		(error >= 100 && error <= 112) || 
    		(error >= 201 && error <= 302) || 
    		(error >= 304 && error <= 330) || 
    		(error == 500) ||
    		(error == 900) ||
    		(error == 901) || 
    		(error >= 999 && error <= 1009) || 
    		(error == 4031) ||
    		(error >= 20011 && error <= 20062) ||
    		(error >= 20064 && error <= 20171) ||
			(error >= 20073 && error <= 20180) ||
    		(error >= 30000 && error <= 30028) ||
			(error >= 30030 && error <= 30035) ||
			(error >= 30037 && error <= 30040) ||
    		(error >= 30047 && error <= 30109) ||
			(error >= 30111 && error <= 30112) ||
			(error >= 30114 && error <= 30116) ||
			(error >= 50002 && error <= 50056) 
		){
            return true;
    	} else
        	return false;
	}
	
	/**
	 * Used to read the SCM API response.
	 * @param InputSource the current InputSource to use
	 */
	private void parseXML (InputSource isrc)
	{
		try {
			xr.parse(isrc);
		} catch (Exception e) {
			ERR(TAG,"parseXML Parsing Exception = " + e);
		}
	}
	
	
	public class VXMLParser extends DefaultHandler
	{
		boolean currentElement = false;
		String currentValue = null;
		
		boolean itemFound = false;
		
		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (currentElement)
				currentValue = "";
			currentElement = true;
		}
		
		@Override
		public void endElement (String uri, String localName, String qName) throws SAXException 
		{
			currentElement = false;
			if (localName.equals("code")){
				GNP.Code = currentValue;
			}else if (localName.equals("desc")){
				GNP.Desc = currentValue;
			}else if (localName.equals("itemID")){
				if (currentValue.equals(current_id)){
					itemFound = true;
					GNP.CartLineItem_Item_ItemID = currentValue;
				}
			}else if (localName.equalsIgnoreCase("ItemName")){
				if (itemFound)
					GNP.CartLineItem_Item_ItemName = currentValue;
			}else if (localName.equals("PPPID")){
				if (itemFound)
					GNP.CartLineItem_Item_PricePlanPackage_PPPID = currentValue;
			}else if (localName.equals("purchasePrice")){
				if(itemFound){
					GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice = currentValue;
					itemFound = false;
				}
			}else if (localName.equals("confirmationID")){
				GNP.ConfirmationID = currentValue;
			}else if (localName.equals("endUserMsg")){
				GNP.EndUserMsg = currentValue;
			}
			DBG(TAG, "<"+localName+">"+currentValue+"</"+localName+">");
			currentValue = "";
		}
		
		@Override
		public void characters (char[] ch, int start, int length) throws SAXException 
		{
			if (currentElement)
			{
				String value = new String(ch, start, length);
				if (value.equals("\n"))
					value = "";
				currentValue += value;
			}
		}
	}

}
#endif