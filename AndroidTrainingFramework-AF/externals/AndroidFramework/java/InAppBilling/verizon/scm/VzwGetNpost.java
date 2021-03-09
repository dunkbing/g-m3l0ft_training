#if VZW_STORE && VZW_SCM
package APP_PACKAGE.iab;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import APP_PACKAGE.GLUtils.Device;

public class VzwGetNpost
{
	/**
	 * The User-Agent to be used.
	 *
	 * Default is "GameLoft"; set to whatever User-Agent you need.
	 * If <code>null</code>, no user agent will be set (device defaults will 
	 * be used instead)
	 */
	String userAgent = "GameLoft";

	// Verizon SCM Apis
	// Which mean APINumber = 14
	int HSStartUP = 14;
	// Which mean APINumber = 38
	int GetContentByGroup = 38;
	// Which mean APINumber = 10
	int GetView = 10;
	// Which mean APINumber = 6
	int CheckoutCart = 6;
	// Which mean APINumber = 39
	int SubmitPurchase = 39;

	// Verizon Parameters
	String MDN = "6465996161";
    String DeviceModel 		= Build.MODEL;
    String FirmwareVersion 	= Build.VERSION.RELEASE;
    String HandsetClientVers= "3.0";
    String CommProtocolVers = "1.0";
    String CompressionLevel = "";
    String ReferralId 		= "";
    String Unknown 			= "800055";

    public String secretString = "j*__$33+yPQzz92!!~+";
    String HandsetClientVers_MD5 = HandsetClientVers;
    String CommProtocolVers_MD5 = CommProtocolVers;

    // The number of the Handset Store page from which the API was called. This field is currently unused. Zero should be given for now.
    String CallPage = "0";
    
    String CartLineItem_Category_CatID = "1";
    String CartLineItem_Category_CatName = "";
    String CartLineItem_Category_PricePlanPackage_PPPID = "";
    String CartLineItem_Category_PPPDesc = "";
    double CartLineItem_Category_PricePlanPackage_PurchasePrice = 0;
    String CartLineItem_Category_IsWishlist = "";
    String CartLineItem_Item_ItemID = "";
    String CartLineItem_Item_ItemName = "0";
    String CartLineItem_Item_PricePlanPackage_PPPID = "";
    String CartLineItem_Item_PricePlanPackage_PurchasePrice = "0.00";
    String CartLineItem_Item_IsWishlist = "N";
    String CartLineItem_Service_ServiceID = "";
    String CartLineItem_Service_ServiceName = "";
    String CartLineItem_Service_PPPID = "";
    String CartLineItem_Service_PPPDesc = "";
    double CartLineItem_Service_PurchasePrice = 0;
    int CartLineItem_ValuePak_ValuePakId = 0;
    double CartLineItem_ValuePak_PurchasePrice = 7.99;
    String CartLineItem_IsWishlist = "";
    String CartLineItem_Discount_Type = "";
    String CartLineItem_Discount_Code = "1";
    String Info_Name = "FromPage";
    String Info_Value = "0";
    String CartLineItem_ConfirmDupPurchase = "";

    String ConfirmationID = "";
    String Type = "";

    String ViewID = "CATITEMS";

    String ViewParam = ""; // CATEGORY_NAME: Let's Golf 2!
    /*
	 * 		POSITION			CATEGORY_NAME
	 * 			1				Asphalt 6 HD
	 * 			2				Let's Golf!
	 * 			3				NOVA 2 HD
	 * 			4				UNO HD
	 * 			5				Oregon Trail HD
	 * 			6				Dungeon Hunter 2 HD
	 * 			7				N.O.V.A. Near Orbit Vanguard A
	 * 			8				Modern Combat: Sandstorm
	 * 			9				Spiderman HD
	 * 		   	10				Oregon Trail
	 * 		   	11				The Oregon Trail Gold Rush
	 * 		   	12				Uno
	 * 		   	14				Let's Golf 2??
	 */

    String Code;
    String Desc;
    String EndUserMsg;
    
    public VzwGetNpost()
	{
	}

    @SuppressWarnings("null")
	public String getDeviceMEID()
    {
		return Device.getDeviceId();
    }
	
	public String getItemName()
	{
		return CartLineItem_Item_ItemName;
	}
	
	public String getPrice()
	{
		return CartLineItem_Item_PricePlanPackage_PurchasePrice;
	}
	
	public String getCode()
	{
		return Code;
	}
}
#endif
