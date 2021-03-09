package APP_PACKAGE.iab;

import android.app.Dialog;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Method;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.R;

public class CustomizeDialog extends Dialog implements OnClickListener {
	
	SET_TAG("InAppBilling");

	Button okButton;
	Button btnClose;
	RadioGroup radioG;
	RadioButton itemCarrier;
	RadioButton itemCreditCard;
	RadioButton itemPaypal;
	RadioButton itemShenzhoufu;
	RadioButton itemChangePayment;
	RadioButton itemAddNewCCC;
	TextView txtTnC;
	
	final String HTTP_BILLING		= InAppBilling.GET_STR_CONST(IAB_HTTP);		//"http";
	final String WAP_BILLING		= InAppBilling.GET_STR_CONST(IAB_WAP);		//"wap";
	String CC_BILLING			= InAppBilling.GET_STR_CONST(IAB_CC);		//"cc";
	final String BOKU_BILLING		= InAppBilling.GET_STR_CONST(IAB_BOKU);		//"boku";
	String PAYPAL_BILLING			= InAppBilling.GET_STR_CONST(IAB_PAYPAL);	//"paypal";
	final String SHENZHOUFU_BILLING		= InAppBilling.GET_STR_CONST(IAB_SHENZHOUFU);	//"shenzhoufu";
	final String SMS_BILLING		= InAppBilling.GET_STR_CONST(IAB_SMS);		//"sms";
	final String PSMS_BILLING		= InAppBilling.GET_STR_CONST(IAB_PSMS);		//"psms";
	final String UMP_R3_BILLING		= InAppBilling.GET_STR_CONST(IAB_UMP_R3);	//"ump";
	
	final int TYPE_CHOOSE_PAYMENT = 0;
	final int TYPE_CHANGE_PAYMENT = 1;
	final int TYPE_FIRST_PAYMENT  = 2;
	
	final int PHONE_BILL  		= 0;
	final int CREDIT_CARD 		= 1;
	final int CREDIT_CARD_NEW 	= 2;
	final int PAYPAL	 		= 3;
	final int CHANGE_PAYMENT	= 4;
	final int SHENZHOUFU 		= 5;
	
	int BILLING_OPTION = PHONE_BILL;
		
	String message;
	
	#define DIALOG_SMALL		1
	#define DIALOG_NORMAL		2
	#define DIALOG_LARGE		3
	public CustomizeDialog(Context context, int type)
	{
		super(context, R.style.Theme_InAppBillingNoBG);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		boolean mbIsPreviousBillingAvailable = true;
		int pmethods = 0;
		
		if(GLOFTHelper.getServerInfo().setBillingMethod("wap_other"))
		{
			CC_BILLING	= "wap_other";//InAppBilling.GET_STR_CONST(IAB_WAP_CC);		//"cc";
			DBG(TAG,"+++++++++++++++++++++++++++++++++ wap_other ACTIVATED ++++++++++++++++++++++++++++++++++++++++");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "+++++ wap_other ACTIVATED +++++");
		}
		else
		{
			DBG(TAG,"/*/-*/-*/-*/*-/*-/*-/-*/*-/*-/*-/-*/*-/*-/ wap_other NOT ACTIVATED /*/*/*/*/*/*/-*/*-/*-/*-/*-/*-/*-/");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "/*/*-/*-/ wap_other NOT ACTIVATED /*/*-/*-/");
		}


		if(GLOFTHelper.getServerInfo().setBillingMethod("wap_paypal"))
		{
			PAYPAL_BILLING = "wap_paypal";
			DBG(TAG,"+++++++++++++++++++++++++++++++++ wap_paypal ACTIVATED ++++++++++++++++++++++++++++++++++++++++");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO, "+++++ wap_paypal ACTIVATED +++++");
		}
		//Set Default Billing Method
		GLOFTHelper.getServerInfo().searchForDefaultBillingMethod(GLOFTHelper.GetItemId());

		String defaultBillingType = GLOFTHelper.getServerInfo().getBillingType();
		
		//Choose the Layout to load (Choose Payment of Change Payment)
		if ((type == TYPE_CHOOSE_PAYMENT) || (type == TYPE_FIRST_PAYMENT))
		{
			setContentView(R.layout.iab_dialog_choosepayment);
			TextView choosepaymenttext = (TextView) findViewById(R.id.txtVPayWith);
			choosepaymenttext.setText(R.string.IAB_PURCHASE_PAY_FOR_THIS_WITH);
		}
		else if (type == TYPE_CHANGE_PAYMENT)
		{
			setContentView(R.layout.iab_dialog_changepayment);
			TextView changepaymenttext = (TextView) findViewById(R.id.txtVChoosePayment);
			changepaymenttext.setText(R.string.IAB_PURCHASE_CHANGE_PAYMENT_METHOD);
			mbIsPreviousBillingAvailable=false;
		}
			
		android.view.ViewGroup.LayoutParams params = getWindow().getAttributes();
        
		params.height = android.view.ViewGroup.LayoutParams.FILL_PARENT; 
		params.width = android.view.ViewGroup.LayoutParams.FILL_PARENT; 
		
		getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		//Load all the controllers
		
		okButton = (Button) findViewById(R.id.OkButton);
		okButton.setText(R.string.IAB_SKB_BUY_NOW);
		okButton.setOnClickListener(this);
		
		/*btnClose = (Button) findViewById(R.id.btnClose);
		btnClose.setText(R.string.IAB_SKB_EXIT);
		btnClose.setOnClickListener(this);*/
		
		txtTnC = (TextView) findViewById(R.id.txtVTnC);

		if(GLOFTHelper.getServerInfo().getTNC_Title().isEmpty())
			txtTnC.setText(R.string.IAB_SKB_TCS);
		else
			txtTnC.setText(GLOFTHelper.getServerInfo().getTNC_Title());
		
		txtTnC.setOnClickListener(this);

		TextView titletext = (TextView) findViewById(R.id.txtVPurchaseTitle);
		titletext.setText(R.string.app_name);

		TextView itemtext = (TextView) findViewById(R.id.txtVItemInfo);
		//message = GLOFTHelper.getStringFormated(R.string.IAB_PURCHASE_ITEM_INFO,"{ITEM}",GLOFTHelper.GetItemDescription());
		//message = GLOFTHelper.getStringFormated(message,"{PRICE}",GLOFTHelper.GetItemPrice());
		itemtext.setText(GLOFTHelper.GetItemDescription());
		
		TextView itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
		itemtextprice.setText(GLOFTHelper.GetItemPrice());
		
		TextView totaltext = (TextView) findViewById(R.id.txtVTotal);
		totaltext.setText(R.string.IAB_PURCHASE_ITEM_TOTAL);
		
		TextView totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
		totalpricetext.setText(GLOFTHelper.GetItemPrice());
		
		
		//RadioGroup
		radioG = (RadioGroup)findViewById(R.id.itemsGroup);
		radioG.setOnClickListener(this);

		//items
		itemCarrier = (RadioButton) findViewById(R.id.itemCarrier);
		itemCarrier.setOnClickListener(this);
		itemCarrier.setText(R.string.IAB_PURCHASE_ITEM_PHONE_BILLL);
		
		itemPaypal = (RadioButton) findViewById(R.id.itemPaypal);
		itemPaypal.setOnClickListener(this);
		
		itemShenzhoufu = (RadioButton) findViewById(R.id.itemShenzhoufu);
		itemShenzhoufu.setOnClickListener(this);
		
		itemCreditCard = (RadioButton) findViewById(R.id.itemCreditCard);
		itemCreditCard.setOnClickListener(this);
		itemCreditCard.setText(R.string.IAB_PURCHASE_ITEM_CREDIT_CARD);
		
		//If a previous purchase then change the Credit Card Text
		if(SUtils.getLManager().PreviousPurchase())
		{
			itemCreditCard.setText(GLOFTHelper.ReturnFormatCC());
		}
		
		itemChangePayment = (RadioButton) findViewById(R.id.itemChangePaymentMethod);
		itemChangePayment.setOnClickListener(this);
		itemChangePayment.setText(R.string.IAB_PURCHASE_CHANGE_PAYMENT_METHOD);
		
		itemAddNewCCC = (RadioButton) findViewById(R.id.itemAddNewCreditCard);
		itemAddNewCCC.setOnClickListener(this);
		itemAddNewCCC.setText(R.string.IAB_PURCHASE_ITEM_ADD_NEW_CREDIT_CARD);


		//If run by 1st time, there's no previous Billing Selection
		if (type == TYPE_FIRST_PAYMENT)
			mbIsPreviousBillingAvailable=false;

		
		//Check if a previous purchase has been made
		if ((SUtils.getLManager().GetUserPaymentType()!=-1)&&(type != TYPE_CHANGE_PAYMENT))
		{
      		DBG(TAG,"***************Previous Purchase Realized by: "+SUtils.getLManager().GetUserPaymentType());
      		LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"* Previous Purchase Realized by: "+SUtils.getLManager().GetUserPaymentType());
			switch(SUtils.getLManager().GetUserPaymentType())
			{
				case LManager.CARRIER_PAYMENT:
					if(
					   ((GLOFTHelper.getServerInfo().setBillingMethod(HTTP_BILLING))&&(IsValidConnection())&&(IsValidSIM()))
					 ||(GLOFTHelper.getServerInfo().setBillingMethod(SMS_BILLING)&&(IsValidSIM()))
					 ||(GLOFTHelper.getServerInfo().setBillingMethod(PSMS_BILLING)&&(IsValidSIM()))
					 ||(GLOFTHelper.getServerInfo().setBillingMethod(UMP_R3_BILLING)&&(IsValidSIM()))
					 ||(GLOFTHelper.getServerInfo().setBillingMethod(WAP_BILLING)&&(IsValidSIM()))
					 ||(GLOFTHelper.getServerInfo().setBillingMethod(BOKU_BILLING)&&(IsValidSIM()))
					  )
					{
						BILLING_OPTION = PHONE_BILL;
						itemCarrier.setChecked(true);
						if((GLOFTHelper.getServerInfo().getBillingType().equals(BOKU_BILLING)) || (GLOFTHelper.getServerInfo().getBillingType().equals(WAP_BILLING)))
						{
							DBG(TAG,"Boku or WAP Detected, deleting TnC Texts");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO,"Boku or WAP Detected, deleting TnC Texts");
							txtTnC.setVisibility(txtTnC.GONE);
						}
						pmethods++;
						
						itemPaypal.setVisibility(itemPaypal.GONE);
						itemShenzhoufu.setVisibility(itemShenzhoufu.GONE);
						itemCreditCard.setVisibility(itemCreditCard.GONE);
						itemAddNewCCC.setVisibility(itemAddNewCCC.GONE);
						DBG(TAG,"And AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And AVAILABLE");
					}
					else
					{
						DBG(TAG,"And NOT AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And NOT AVAILABLE");
						mbIsPreviousBillingAvailable = false;
					}
				break;

				case LManager.PAYPAL_PAYMENT:
					if(GLOFTHelper.getServerInfo().setBillingMethod(PAYPAL_BILLING))
					{
						BILLING_OPTION = PAYPAL;
						DBG(TAG,"And AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And AVAILABLE");
						itemPaypal.setChecked(true);
						pmethods++;
						
						itemCarrier.setVisibility(itemCarrier.GONE);
						itemShenzhoufu.setVisibility(itemShenzhoufu.GONE);
						itemCreditCard.setVisibility(itemCreditCard.GONE);
						itemAddNewCCC.setVisibility(itemAddNewCCC.GONE);
					}
					else
					{
						DBG(TAG,"And NOT AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And NOT AVAILABLE");
						mbIsPreviousBillingAvailable = false;
					}
				break;
				#if SHENZHOUFU_STORE
				case LManager.SHENZHOUFU_PAYMENT:
					if(GLOFTHelper.getServerInfo().setBillingMethod(SHENZHOUFU_BILLING))
					{
						BILLING_OPTION = SHENZHOUFU;
						DBG(TAG,"And AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And AVAILABLE");
						itemShenzhoufu.setChecked(true);
						pmethods++;
						
						itemPaypal.setVisibility(itemPaypal.GONE);
						itemCarrier.setVisibility(itemCarrier.GONE);
						itemCreditCard.setVisibility(itemCreditCard.GONE);
						itemAddNewCCC.setVisibility(itemAddNewCCC.GONE);
						
						DBG(TAG,"Shenzhoufu Detected, deleting TnC Texts");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO,"Shenzhoufu Detected, deleting TnC Texts");
						txtTnC.setVisibility(txtTnC.GONE);
					}
					else
					{
						DBG(TAG,"And NOT AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And NOT AVAILABLE");
						mbIsPreviousBillingAvailable = false;
					}
				break;
				#endif
				case LManager.CREDIT_CARD_PAYMENT:
					if(GLOFTHelper.getServerInfo().setBillingMethod(CC_BILLING))
					{
						BILLING_OPTION = CREDIT_CARD;
						DBG(TAG,"And AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And AVAILABLE");
						itemCreditCard.setChecked(true);
						txtTnC.setVisibility(txtTnC.VISIBLE);
						pmethods++;
						
						itemCarrier.setVisibility(itemCarrier.GONE);
						itemPaypal.setVisibility(itemPaypal.GONE);
						itemShenzhoufu.setVisibility(itemShenzhoufu.GONE);
						itemAddNewCCC.setVisibility(itemAddNewCCC.GONE);
					}
					else
					{
						DBG(TAG,"And NOT AVAILABLE");
						LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"And NOT AVAILABLE");
						mbIsPreviousBillingAvailable = false;
					}
				break;				
			}
			
			//remove change payment option when only 1 billing option is available
      		if (InAppBilling.mServerInfo.getItemById(GLOFTHelper.GetItemId()).getBillingOptsCount() == 1)
			{
				if (BILLING_OPTION != CREDIT_CARD)
					itemChangePayment.setVisibility(itemChangePayment.GONE);
			}
			if (InAppBilling.mServerInfo.getItemById(GLOFTHelper.GetItemId()).getBillingOptsCount() > 1 && BILLING_OPTION == PHONE_BILL)
			{
				if(!GLOFTHelper.getServerInfo().setBillingMethod(PAYPAL_BILLING)
				&& !GLOFTHelper.getServerInfo().setBillingMethod(SHENZHOUFU_BILLING)
				&& !GLOFTHelper.getServerInfo().setBillingMethod(CC_BILLING))
					itemChangePayment.setVisibility(itemChangePayment.GONE);
			}
			if(BILLING_OPTION == PHONE_BILL || !GLOFTHelper.getServerInfo().isProfileSelected())
				GLOFTHelper.getServerInfo().searchForDefaultBillingMethod(GLOFTHelper.GetItemId());
			itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
			itemtextprice.setText(GLOFTHelper.GetItemPrice());
			totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
			totalpricetext.setText(GLOFTHelper.GetItemPrice());
		}
		//END OF {Check if a previous purchase has been made}
		
		
		if((!mbIsPreviousBillingAvailable) || (type == TYPE_CHANGE_PAYMENT))
		{
			DBG(TAG,"***************NONE Previous Purchase Register Stored");
			DBG(TAG,"Creating Billing Methods List");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO,"NONE Previous Purchase Register Stored");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO,"Creating Billing Methods List");
			if( 
			(!(GLOFTHelper.getServerInfo().setBillingMethod(HTTP_BILLING))
			 && !(GLOFTHelper.getServerInfo().setBillingMethod(WAP_BILLING))
			 && !(GLOFTHelper.getServerInfo().setBillingMethod(UMP_R3_BILLING))
			 && !(GLOFTHelper.getServerInfo().setBillingMethod(PSMS_BILLING))
			 && !(GLOFTHelper.getServerInfo().setBillingMethod(SMS_BILLING))
			 && !(GLOFTHelper.getServerInfo().setBillingMethod(BOKU_BILLING))
			   )
			 || (!(IsValidSIM()))
			 )
			 {
				DBG(TAG,"Phone Bill NOT Available (BOKU/SMS/WAP/HTTP)");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"Phone Bill NOT Available (BOKU/SMS/WAP/HTTP)");
				itemCarrier.setVisibility(itemCarrier.GONE);
			 }
			 else
			 {
				DBG(TAG,"Phone Bill is Available (BOKU/SMS/WAP/HTTP)");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"Phone Bill Available (BOKU/SMS/WAP/HTTP)");
				BILLING_OPTION = PHONE_BILL;
				itemCarrier.setChecked(true);
				itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
				itemtextprice.setText(GLOFTHelper.GetItemPrice());
				totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
				totalpricetext.setText(GLOFTHelper.GetItemPrice());
				
				if((GLOFTHelper.getServerInfo().getBillingType().equals(BOKU_BILLING)) || (GLOFTHelper.getServerInfo().getBillingType().equals(WAP_BILLING)))
				{
					DBG(TAG,"Boku or WAP Detected, deleting TnC Texts");
					LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO,"Phone Bill NOT Available (BOKU/SMS/WAP/HTTP)");
					txtTnC.setVisibility(txtTnC.GONE);
				}
				pmethods++;
			 }
			 
			
			if(!(GLOFTHelper.getServerInfo().setBillingMethod(PAYPAL_BILLING)))
			 {
				DBG(TAG,"Paypal is NOT Available");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"Paypal is NOT Available");
				itemPaypal.setVisibility(itemPaypal.GONE);
			 }
			 else
			 {
				DBG(TAG,"Paypal is Available");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"Paypal is Available");
				BILLING_OPTION = PAYPAL;
				itemPaypal.setChecked(true);
				itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
				itemtextprice.setText(GLOFTHelper.GetItemPrice());
				totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
				totalpricetext.setText(GLOFTHelper.GetItemPrice());

				pmethods++;
			 }
			if(!(GLOFTHelper.getServerInfo().setBillingMethod(SHENZHOUFU_BILLING)))
			 {
				DBG(TAG,"Shenzhoufu is NOT Available");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"Shenzhoufu is NOT Available");
				itemShenzhoufu.setVisibility(itemShenzhoufu.GONE);
			 }
			 else
			 {
				DBG(TAG,"Shenzhoufu is Available");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"Shenzhoufu is Available");
				BILLING_OPTION = SHENZHOUFU;
				itemShenzhoufu.setChecked(true);
				DBG(TAG,"Shenzhoufu Detected, deleting TnC Texts");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO,"Shenzhoufu Detected, deleting TnC Texts");
				txtTnC.setVisibility(txtTnC.GONE);
				pmethods++;
			 }
			if(!(GLOFTHelper.getServerInfo().setBillingMethod(CC_BILLING)))
			 {
				DBG(TAG,"CC is NOT Available");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"CC is NOT Available");
				itemCreditCard.setVisibility(itemCreditCard.GONE);
			 }
			 else
			 {
				DBG(TAG,"CC Added is Available");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_DEBUG, IAP_LOG_STATUS_INFO,"CC Added is Available");
				BILLING_OPTION = CREDIT_CARD;
				itemCreditCard.setChecked(true);
				itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
				itemtextprice.setText(GLOFTHelper.GetItemPrice());
				totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
				totalpricetext.setText(GLOFTHelper.GetItemPrice());
				txtTnC.setVisibility(txtTnC.VISIBLE);
				pmethods++;
			 }
			 GLOFTHelper.getServerInfo().searchForDefaultBillingMethod(GLOFTHelper.GetItemId());
			 if(defaultBillingType != null && !defaultBillingType.equals(""))
			 { 
  				DBG(TAG,"Setting selected billing type: "+defaultBillingType);
  				LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO,"Setting selected billing type: "+defaultBillingType);
  				if(defaultBillingType.equals(HTTP_BILLING) 
  				|| defaultBillingType.equals(WAP_BILLING)
  				|| defaultBillingType.equals(BOKU_BILLING)
  				|| defaultBillingType.equals(SMS_BILLING)
  				|| defaultBillingType.equals(PSMS_BILLING)
  				|| defaultBillingType.equals(UMP_R3_BILLING))
  				{
					BILLING_OPTION = PHONE_BILL;
					itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
					itemtextprice.setText(GLOFTHelper.GetItemPrice());
	
					totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
					totalpricetext.setText(GLOFTHelper.GetItemPrice());

  					itemCarrier.setChecked(true);
  				}
  				else if(defaultBillingType.equals(CC_BILLING)
  				||defaultBillingType.equals("wap_other"))
  				{
					BILLING_OPTION = CREDIT_CARD;
  					itemCreditCard.setChecked(true);
  				}
  				else if(defaultBillingType.equals(PAYPAL_BILLING))
  				{
					BILLING_OPTION = PAYPAL;
					itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
					itemtextprice.setText(GLOFTHelper.GetItemPrice());
	
					totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
					totalpricetext.setText(GLOFTHelper.GetItemPrice());

  					itemPaypal.setChecked(true);
  				}
  				else if(defaultBillingType.equals(SHENZHOUFU_BILLING))
  				{
					BILLING_OPTION = SHENZHOUFU;
  					itemShenzhoufu.setChecked(true);
  				}
			}
			
			if ((type == TYPE_CHANGE_PAYMENT) || (type == TYPE_FIRST_PAYMENT) || (pmethods == 1))
			{
				itemChangePayment.setVisibility(itemChangePayment.GONE);
			}

			if ((type == TYPE_CHOOSE_PAYMENT)||(type == TYPE_FIRST_PAYMENT)||(!SUtils.getLManager().PreviousPurchase()))
			{
				itemAddNewCCC.setVisibility(itemAddNewCCC.GONE);
			}

		#if USE_PHONEBILL_AS_DEFAULT_OPTION
			if(itemCarrier.getVisibility() != (View.GONE))
			{
				DBG(TAG,"Set Phone Bill as default option");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_VERBOSE, IAP_LOG_STATUS_INFO,"Set Phone Bill as default option");
				BILLING_OPTION = PHONE_BILL;
				itemCarrier.setChecked(true);
			}
		#endif
		}
		
			
			//IAP flow should never reach this part
			//if(InAppBilling.mServerInfo.getItemById(GLOFTHelper.GetItemId()).getBillingOptsCount() <= 1)
			if(pmethods<1)
			{
				WARN(TAG,"HIDING BUY NOW VERIFY THIS!!!");
				LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO,"HIDING BUY NOW VERIFY THIS!!!");
				okButton = (Button) findViewById(R.id.OkButton);
				okButton.setVisibility(txtTnC.GONE);
			}

	}
	
	private boolean IsValidConnection()
	{
		ConnectivityManager mConnectivityManager 	= (ConnectivityManager) SUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetInfo = mConnectivityManager.getActiveNetworkInfo();
		return (  ((mNetInfo.getType() == ConnectivityManager.TYPE_WIFI) && (GLOFTHelper.getServerInfo().getIs3GOnly().equals("0")))
				 ||(mNetInfo.getType() != ConnectivityManager.TYPE_WIFI)
				);
	}
	
	private boolean IsValidSIM()
	{
		TelephonyManager mDeviceInfo = (TelephonyManager)SUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);

			if(mDeviceInfo.getPhoneType() == mDeviceInfo.PHONE_TYPE_CDMA)
				return (true);
			else
				return (mDeviceInfo.getSimState() == mDeviceInfo.SIM_STATE_READY);		
	}
	
	@Override
	public void onClick(View v) {
	TextView itemtextprice;
	TextView totalpricetext;
	
		/** When OK Button is clicked, dismiss the dialog */
			
		if (v == okButton)
		{
		
			if( BILLING_OPTION != CHANGE_PAYMENT)
			{
				InAppBilling.closeBackgroundDialog();
				InAppBilling.showBackgroundDialog();
			}
		
			switch(BILLING_OPTION)
			{
			case PHONE_BILL:
				dismiss();
				new Thread(new Runnable()
				{
					public void run()
					{
						GLOFTHelper.getServerInfo().searchForDefaultBillingMethod(GLOFTHelper.GetItemId());
						if(GLOFTHelper.getServerInfo().getIs3GOnly().equals("1"))
							GLOFTHelper.GetInstance().PrepareDataConnection();
            
						if(GLOFTHelper.getServerInfo().setBillingMethod(HTTP_BILLING))
						   GLOFTHelper.GetInstance().ProcessTransactionHTTP(GLOFTHelper.GetItemId());
						
#if USE_UMP_R3_BILLING
						else if(GLOFTHelper.getServerInfo().setBillingMethod(UMP_R3_BILLING))
						{
							GLOFTHelper.GetInstance().ProcessTransactionUMP_R3(GLOFTHelper.GetItemId());
						} 
#endif
						else if(GLOFTHelper.getServerInfo().setBillingMethod(SMS_BILLING))
						   GLOFTHelper.GetInstance().ProcessTransactionSMS(GLOFTHelper.GetItemId());
						
#if USE_MTK_SHOP_BUILD || ENABLE_IAP_PSMS_BILLING
						else if(GLOFTHelper.getServerInfo().setBillingMethod(PSMS_BILLING))
						{
							if (GLOFTHelper.getServerInfo().getBillingAttribute(GLOFTHelper.GetItemId(),InAppBilling.GET_STR_CONST(IAB_PSMS_BILL_TYPE)).equals(InAppBilling.GET_STR_CONST(IAB_SILENT)))
								GLOFTHelper.GetInstance().ProcessTransactionSMS(GLOFTHelper.GetItemId());
							else
							GLOFTHelper.GetInstance().ProcessTransactionPSMS(GLOFTHelper.GetItemId());
						}  
#endif
						else if(GLOFTHelper.getServerInfo().setBillingMethod(WAP_BILLING))
						   GLOFTHelper.GetInstance().ProcessTransactionWAP(GLOFTHelper.GetItemId());
					#if BOKU_STORE
						else if(GLOFTHelper.getServerInfo().setBillingMethod(BOKU_BILLING))
						{
							GLOFTHelper.GetInstance().ProcessTransactionBOKU(GLOFTHelper.GetItemId());
						}
					#else
						else
						{
							WARN(TAG, "No valid Phone Bill Found");
							LOGGING_LOG_INFO(IAP_LOG_TYPE_WARNING, IAP_LOG_STATUS_INFO,"No valid Phone Bill Found");
						}
					#endif
					}
				}).start();

			break;
			
			case CREDIT_CARD_NEW:
				SUtils.getLManager().AddNewCreditCard();
			case CREDIT_CARD:
				dismiss();
				new Thread(new Runnable()
				{
					public void run()
					{
						if(GLOFTHelper.getServerInfo().setBillingMethod("wap_other"))
						{
							GLOFTHelper.GetInstance().ProcessTransactionWAP(GLOFTHelper.GetItemId());
						}
						else if(GLOFTHelper.getServerInfo().setBillingMethod(CC_BILLING))
						{
							GLOFTHelper.GetInstance().ProcessTransactionCC(GLOFTHelper.GetItemId());
						}
					}
				}).start();
			break;

			case PAYPAL:
				dismiss();
				new Thread(new Runnable()
				{
					public void run()
					{
						GLOFTHelper.getServerInfo().setBillingMethod(PAYPAL_BILLING);
						GLOFTHelper.GetInstance().ProcessTransactionWAPPaypal(GLOFTHelper.GetItemId());
					}
				}).start();
			break;

		#if SHENZHOUFU_STORE
			case SHENZHOUFU:
				dismiss();
				new Thread(new Runnable()
				{
					public void run()
					{
						GLOFTHelper.getServerInfo().setBillingMethod(SHENZHOUFU_BILLING);
						GLOFTHelper.GetInstance().startShenzhoufuTransaction(GLOFTHelper.GetItemId());
					}
				}).start();
			break;
		#endif //#if SHENZHOUFU_STORE
			case CHANGE_PAYMENT:
				dismiss();
			((Activity)SUtils.getContext()).runOnUiThread(new Runnable ()
				{
				String message;
				public void run ()
				{
					try
					{
						CustomizeDialog dlgChangePayment = new CustomizeDialog(SUtils.getContext(),TYPE_CHANGE_PAYMENT);
						
						dlgChangePayment.setOnCancelListener(new DialogInterface.OnCancelListener()
						{
							public void onCancel(DialogInterface dialog)
							{
								Bundle bundle = new Bundle();
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);
													
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GLOFTHelper.GetItemId()!=null)?GLOFTHelper.GetItemId().getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(GLOFTHelper.GetItemId()!=null)?GLOFTHelper.GetItemId().getBytes():null);//trash value
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
								bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
													
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
								bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);

								try{
									Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
									Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
									bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
									}
									catch(Exception ex )
									{
									ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
									}
							}
						});
						dlgChangePayment.show();
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
				});
			break;
			}
		}
			
		if (v == itemCarrier)
		{
			DBG(TAG,"*********PHONE_BILL SELECTED");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO,"PHONE_BILL SELECTED");
			BILLING_OPTION = PHONE_BILL;
			if(GLOFTHelper.getServerInfo().setBillingMethod(HTTP_BILLING))
			{
				txtTnC.setVisibility(txtTnC.VISIBLE);
			}
			else if(GLOFTHelper.getServerInfo().setBillingMethod(SMS_BILLING) 
					|| GLOFTHelper.getServerInfo().setBillingMethod(PSMS_BILLING)
					|| GLOFTHelper.getServerInfo().setBillingMethod(UMP_R3_BILLING)
				)
			{
				txtTnC.setVisibility(txtTnC.VISIBLE);
			}
			else if(GLOFTHelper.getServerInfo().setBillingMethod(WAP_BILLING))
			{
				txtTnC.setVisibility(txtTnC.GONE);
			}
			else if(GLOFTHelper.getServerInfo().setBillingMethod(BOKU_BILLING))
			{
				txtTnC.setVisibility(txtTnC.GONE);
			}
				
			itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
			if (SUtils.getContext()!= null && SUtils.getContext().getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT)
			{
				String item_price = GLOFTHelper.GetItemPrice();
				int index = item_price.indexOf("+");
				if (item_price!= null && index > 0)
					item_price = item_price.substring(0, index) + "\n" + item_price.substring(index,item_price.length());
				itemtextprice.setText(item_price);
			}
			else
				itemtextprice.setText(GLOFTHelper.GetItemPrice());
			totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
			totalpricetext.setText(GLOFTHelper.GetItemPrice());

		}
		
		if (v == itemCreditCard)
		{
			DBG(TAG,"*********CREDIT CARD SELECTED");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO,"CREDIT CARD SELECTED");
			BILLING_OPTION = CREDIT_CARD;
			GLOFTHelper.getServerInfo().setBillingMethod(CC_BILLING);
			txtTnC.setVisibility(txtTnC.VISIBLE);
			
			itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
			itemtextprice.setText(GLOFTHelper.GetItemPrice());
	
			totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
			totalpricetext.setText(GLOFTHelper.GetItemPrice());
		}
		
		if (v == itemPaypal)
		{
			DBG(TAG,"*********PAYPAL SELECTED");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO,"PAYPAL SELECTED");
			BILLING_OPTION = PAYPAL;
			GLOFTHelper.getServerInfo().setBillingMethod(PAYPAL_BILLING);
			itemtextprice = (TextView) findViewById(R.id.txtVPriceItems);
			itemtextprice.setText(GLOFTHelper.GetItemPrice());
	
			totalpricetext = (TextView) findViewById(R.id.txtVPriceTotal);
			totalpricetext.setText(GLOFTHelper.GetItemPrice());

		}
		#if SHENZHOUFU_STORE
		if (v == itemShenzhoufu)
		{
			DBG(TAG,"*********SHENZHOUFU SELECTED");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO,"SHENZHOUFU SELECTED");
			BILLING_OPTION = SHENZHOUFU;
		}
		#endif
		if (v == itemChangePayment)
		{
			DBG(TAG,"*********CHANGE PAYMENT METHOD SELECTED");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO,"CHANGE PAYMENT METHOD SELECTED");
			BILLING_OPTION = CHANGE_PAYMENT;
			okButton = (Button) findViewById(R.id.OkButton);
			okButton.setText(R.string.IAB_SKB_CONFIRM);
		}
		else if(v != txtTnC)
		{
			okButton = (Button) findViewById(R.id.OkButton);
			okButton.setText(R.string.IAB_SKB_BUY_NOW);
		}

		if (v == itemAddNewCCC)
		{
			DBG(TAG,"*********ADD NEW CREDIT CARD SELECTED");
			LOGGING_LOG_INFO(IAP_LOG_TYPE_INFO, IAP_LOG_STATUS_INFO,"ADD NEW CREDIT CARD SELECTED");
			BILLING_OPTION = CREDIT_CARD_NEW;
		}
		
		if(v == btnClose)
		{
			dismiss();
			Bundle bundle = new Bundle();
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_OPERATION), OP_FINISH_BUY_ITEM);

			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_ITEM),(GLOFTHelper.GetItemId()!=null)?GLOFTHelper.GetItemId().getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_LIST),(GLOFTHelper.GetItemId()!=null)?GLOFTHelper.GetItemId().getBytes():null);//trash value
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_REGION),(InAppBilling.mCharRegion!=null)?InAppBilling.mCharRegion.getBytes():null);
			bundle.putByteArray(InAppBilling.GET_STR_CONST(IAB_CHAR_ID),(InAppBilling.mCharId!=null)?InAppBilling.mCharId.getBytes():null);
			
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_INDEX), 1);//trash value
			bundle.putInt(InAppBilling.GET_STR_CONST(IAB_RESULT), IAB_BUY_CANCEL);

			try{
				Class myClass = Class.forName(InAppBilling.GET_FQCN(IAB_CLASS_NAME_IN_APP_BILLING));
				Method mMethod = myClass.getMethod(InAppBilling.GET_STR_CONST(IAB_METHOD_NAME_NTVE_SEND_DATA), (new Class[]{Bundle.class}));
				bundle = (Bundle)mMethod.invoke(null, (new Object[]{bundle}));
			}
			catch(Exception ex )
			{
				ERR(TAG,"A: Error invoking reflex method "+ex.getMessage());
			}
		}
		
		if(v == txtTnC)
		{
			GLOFTHelper.GetInstance().showDialog(GLOFTHelper.DIALOG_TERMS_AND_CONDITIONS,GLOFTHelper.GetItemId());
		}
	}
	
}
