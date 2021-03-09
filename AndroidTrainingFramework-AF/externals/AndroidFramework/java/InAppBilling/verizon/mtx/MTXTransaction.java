#if VZW_STORE && VZW_MTX
package APP_PACKAGE.iab;
import APP_PACKAGE.R;
import APP_PACKAGE.iab.VZWHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.verizon.vcast.apps.InAppActivity;
import com.verizon.vcast.apps.InAppPurchasor;
import com.verizon.vcast.apps.InAppPurchasor.PurchaseInAppContentResult;
import com.verizon.vcast.apps.InAppPurchasor.DiscoveryParameters;
import com.verizon.vcast.apps.InAppPurchasor.InAppContents;
import com.verizon.vcast.apps.InAppPurchasor.Item;
import com.verizon.vcast.apps.InAppPurchasor.Offer;

/**
 *	This activity performs the purchase of new in app content
 *	At first we access the server to get the available In App items - each In App item correlates to a game content;
 *	Then we go through the In App items and find the content that the user is about to purchase.
 *	Lastly, we get the offer for this content and perform the purchase.
 */
public class MTXTransaction extends InAppActivity
{	
	SET_TAG("MTXTransaction");

	String keyword; // The app keyword as submitted in VDC
	InAppPurchasor.Item  gameItem;
	InAppPurchasor.Offer gameOffer;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		DBG(TAG, "[onCreate]");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.iab_purchase_content);
		
		keyword = VZW_APP_KEYWORD;
		
		// Get Offers from Server
		new AsyncTask<Object, Integer, InAppPurchasor.InAppContentOffers>()
		{
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute()
			{
				// Show progress dialog.
				progressDialog = ProgressDialog.show(MTXTransaction.this, "", (MTXTransaction.this.getResources().getString(R.string.IAB_GETTING_SERVER_INFORMATION)), true);
			}
			
			@Override
			protected InAppPurchasor.InAppContentOffers doInBackground(Object... params)
			{
				try
				{
					InAppPurchasor.InAppContents availableContentInApps;

					DiscoveryParameters discoveryParams = inAppPurchasor.new DiscoveryParameters();
					// In this simple app we do not need any specific Discovery Parameters because we do not do paging or sorting of In Apps.
					// This is why we don't call any set methods of discoveryParams
					
					// Perform the call to get the available In Apps
					availableContentInApps = inAppPurchasor.getInAppContents(keyword, discoveryParams);
					DBG(TAG, "********************************************");
					DBG(TAG, "Getting available In Apps content for [" + keyword + "]");
					DBG(TAG, "Item ID [" + VZWHelper.GetItemId() + "]");
					DBG(TAG, "Item Price [" + VZWHelper.GetGamePrice() + "]");
					DBG(TAG, "Offer Type [" + VZWHelper.GetOfferType() + "]");
					DBG(TAG, "********************************************");
					
					if(availableContentInApps != null &&
						availableContentInApps.getResult() == InAppPurchasor.PURCHASE_OK &&
						availableContentInApps.getItems() != null && 
						availableContentInApps.getItems().length != 0)
					{
						DBG(TAG, "Got a list from Verizon of [" + availableContentInApps.getItems().length + "] item(s)");
						gameItem = matchInApp(availableContentInApps, VZWHelper.GetItemId());
						
						if(gameItem == null)
						{
							WARN(TAG, "Cannot find an In App matching ID [" + VZWHelper.GetItemId() + "]");
							return null;
						}
					}
					else
					{
						WARN(TAG, "Cannot find In App content for [" + keyword + "]");
						return null;
					}
					
					// Get the Offer for the item we are purchasing.
					InAppPurchasor.InAppContentOffers contentOffers = inAppPurchasor.getInAppContentOffer(keyword, gameItem.getItemID());
					DBG(TAG, "Getting content offers...");
					
					if(contentOffers.getResult() == InAppPurchasor.LIST_REQ_OK)
						return contentOffers;
					else
						return null;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(InAppPurchasor.InAppContentOffers contentOffers)
			{
				progressDialog.dismiss();
				
				if(contentOffers != null && contentOffers.getResult() == InAppPurchasor.LIST_REQ_OK && contentOffers.getOffers() != null && contentOffers.getOffers().length != 0)
				{
					DBG(TAG, "Got a list from Verizon of [" + contentOffers.getTotalSize() +"] offer(s)");
					gameOffer = matchInOffers(contentOffers, VZWHelper.GetOfferType());
					
					if(gameOffer != null)
					{
						doPurchase();
						return;
					}
				}
				
				WARN(TAG, "Cannot find content offers for [" + keyword + "]");
				VZWHelper.IABResultCallBack(IAB_BUY_FAIL);

				showOKDialog(MTXTransaction.this, (MTXTransaction.this.getResources().getString(R.string.IAB_TRANSACTION_FAILED)), (MTXTransaction.this.getResources().getString(R.string.IAB_CANNOT_DISPLAY_AVAILABLE_ITEMS)), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						finish();
						return;
					}
				});
			}
		}.execute();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	}
	
	
	private void doPurchase()
	{
		InAppPurchasor.PurchaseParameters purchaseParameters = inAppPurchasor.new PurchaseParameters();
		purchaseParameters.setInAppName(gameItem.getItemName());
		purchaseParameters.setOfferID(gameOffer.getOfferID());
		purchaseParameters.setSku("Test");
		purchaseParameters.setPriceType(gameOffer.getPriceType());
		purchaseParameters.setPriceLine(gameOffer.getPriceLine());
		purchaseParameters.setPricingTerms(gameOffer.getPricingTerms());
		
		// In this sample all items are sold in a single fixed price. So min price and max should be the same.
		//purchaseParameters.setPrice(gameOffer.getMinPrice()); 
		purchaseParameters.setPrice(Float.parseFloat(VZWHelper.GetGamePrice()));
		
		int result = inAppPurchasor.purchaseInAppContent(keyword, gameItem.getItemID(), purchaseParameters);		
	
		if(result == InAppPurchasor.PURCHASE_INITIATION_OK)
		{
			return;
		}
		else
		{
			DBG(TAG, "Purchase Initiation Failed");
			VZWHelper.IABResultCallBack(IAB_BUY_FAIL);
			finish();
		}
	}
	
	/**
	 * Go over all the In App items that came back from the server and found the one that matches what the app is selling.
	 * @param availableContentInApps In Apps returned from server
	 * @param inAppTypeGame The type we are looking for
	 * @return
	 */
	private Item matchInApp(InAppContents availableContentInApps, String inAppType)
	{
		Item[] items = availableContentInApps.getItems();
		
		for(int i=0 ; i < items.length ; i++)
		{
			DBG(TAG, ">> Item ID [" + items[i].getItemID() + "] ---  Item name is [" + items[i].getItemName() + "] <<");
			if(items[i].getItemID().equals(inAppType))
				return items[i];
		}
		return null;
		//return items[0];
	}
	
	private Offer matchInOffers(InAppPurchasor.InAppContentOffers availableContentInOffers, String inOfferType)
	{
		Offer[] offer = availableContentInOffers.getOffers();
		
		for(int i=0 ; i < offer.length ; i++)
		{
			DBG(TAG, ">> Offer Type [" + offer[i].getPriceType() + "] ---  Offer Pricing Terms ["+offer[i].getPricingTerms()+"] <<");
			if(offer[i].getPriceType().equals(inOfferType))
				return offer[i];
		}
		return null;
	}
	
	static public void showOKDialog(Context c, String title, String message, DialogInterface.OnClickListener listener)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setTitle(title);
		//builder.setIcon(R.drawable.iconiab);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setNeutralButton(R.string.IAB_SKB_OK, listener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	@Override
	protected void onPurchaseResult(PurchaseInAppContentResult purchaseResult)
	{
		/*String title;
		String message;*/
		if(purchaseResult.getResult() == InAppPurchasor.PURCHASE_OK)
		{
			/*title = (MTXTransaction.this.getResources().getString(R.string.IAB_PURCHASE_ITEM_SUCCESS_TITLE));
			message = (MTXTransaction.this.getResources().getString(R.string.IAB_PURCHASE_ITEM_SUCCESS));*/
			VZWHelper.IABResultCallBack(IAB_BUY_OK);
		}
		else
		{
			/*title = (MTXTransaction.this.getResources().getString(R.string.IAB_PURCHASE_ITEM_FAILURE_TITLE));
			message = (MTXTransaction.this.getResources().getString(R.string.IAB_NETWORK_ERROR));*/
			VZWHelper.IABResultCallBack(IAB_BUY_FAIL);
		}
		
		/*showOKDialog(this, title, message, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{*/
				finish();
		    	/*return;					
			}
		});*/
	}
}
#endif
