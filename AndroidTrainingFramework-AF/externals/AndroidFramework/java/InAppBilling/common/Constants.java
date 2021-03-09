#if USE_IN_APP_BILLING || USE_BILLING
package APP_PACKAGE.billing.common;

public interface Constants {

	public final String          TAG_PURCHASE                        = "FULL";		/** Tag used to tell the server that we are trying to buy a game */	
	public final String          TAG_PURCHASE_CREDIT_CARD            = "FULL_CC";	/** Tag used to tell the server that we are trying to buy using credit card*/
	public final String          TAG_VALIDATE_PURCHASE               = "VAL_PUR";	/** Tag used to tell the server that we are trying to buy */
	
	public final int             VALIDATE_PURCHASE			= 0;
	public final int             PURCHASE_HTTP				= 1;
		
	// alternatives
	public final int             PURCHASE_PAYPAL           	= 2;

	//Credit Card Options NEW
	public final int             PURCHASE_CC_LOGIN   		= 3;
	public final int             PURCHASE_CC_USERBILL   	= 4;
	public final int             PURCHASE_CC_NEWUSERBILL	= 5;
	public final int             PURCHASE_CC_SINGLEBILL		= 6;
	public final int             PURCHASE_CC_FORGOTPW		= 7;
	public final int             PURCHASE_CC_ADDCARDBILL	= 8;
	public final int             PURCHASE_MRC				= 9;
	public final int             PURCHASE_SHENZHOUFU_TRANS_ID	= 10;

	
	public final int 			CC_BILLING_SUCCESS = 2;
	public final int 			CC_BILLING_ERROR = 3;
	public final int 			HTTP_BILLING_SUCCESS = 4;
	public final int 			HTTP_BILLING_ERROR = 5;
	public final int 			PSMS_BILLING_SUCCESS = 6;
	public final int 			PSMS_BILLING_ERROR = 7;
	
	//Credit Card Tags new
	public final String	TAG_CC_LOGIN ="login";
	public final String	TAG_CC_USERBILL ="userbill";
	public final String	TAG_CC_NEWUSERBILL ="newuserbill";
	public final String	TAG_CC_ADDCARDBILL ="addcardbill";
	public final String	TAG_CC_SINGLEBILL ="singlebill"; 
	public final String	TAG_CC_FORGOTPW ="forgotpw"; 
}

#endif//#if USE_IN_APP_BILLING || USE_BILLING
