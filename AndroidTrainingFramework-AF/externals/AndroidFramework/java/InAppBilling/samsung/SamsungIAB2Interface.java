#if USE_IN_APP_BILLING && SAMSUNG_STORE
package APP_PACKAGE.iab;

import org.json.JSONException;
import org.json.JSONObject;
import android.text.format.DateFormat;


// Error messages
// ========================================================================
class ErrorVO
{
	private int     mErrorCode      = 0;
	private String  mErrorString	= "";
	private String  mExtraString    = "";             

	public int 		getErrorCode()							{return mErrorCode;}
	public void 	setErrorCode(int _errorCode)			{mErrorCode = _errorCode;}
	public String 	getErrorString()						{return mErrorString;}
	public void 	setErrorString(String _errorString)		{mErrorString = _errorString;}
	public String 	getExtraString()						{return mExtraString;}
	public void 	setExtraString(String _extraString)		{mExtraString = _extraString;}
}
// ========================================================================



// Item attributes
// ========================================================================
class ItemVO 
{	
	private String mItemId;             
	private String mItemName;           
	private Double mItemPrice;          
	private String mItemPriceString;
	private String mCurrencyUnit;       
	private String mItemDesc;           
	private String mItemImageUrl;       
	private String mItemDownloadUrl;    
	private String mReserved1;          
	private String mReserved2;          
	private String mType;               
	public 	ItemVO(){}
	
	public ItemVO(String mJsonItem)
	{
		try
		{	
			JSONObject jObject = new JSONObject( mJsonItem );
			mItemId = jObject.getString( "mItemId" );
			mItemName = jObject.getString( "mItemName" );
			mItemPrice = Double.parseDouble( ( jObject.getString( "mItemPrice" ) ) );
			mCurrencyUnit = jObject.getString( "mCurrencyUnit" );
			mItemDesc = jObject.getString( "mItemDesc" );
			mItemImageUrl = jObject.getString( "mItemImageUrl" );
			mItemDownloadUrl = jObject.getString( "mItemDownloadUrl" );
			mReserved1 = jObject.getString( "mReserved1" );
			mReserved2 = jObject.getString( "mReserved2" );
			mItemPriceString = jObject.getString( "mItemPriceString" );
			mType = jObject.getString( "mType" );
		}
		catch ( JSONException e ){ERR("SamsungIAB","Exception: " + e.getMessage());}
	}

	public String 	getItemId()									{return mItemId;}
	public void 	setItemId(String itemId)					{this.mItemId = itemId;}
	public String 	getItemName()								{return mItemName;}
	public void 	setItemName(String itemName)				{this.mItemName = itemName;}
	public Double 	getItemPrice()								{return mItemPrice;}
	public void 	setItemPrice(Double itemPrice)				{this.mItemPrice = itemPrice;}
	public String 	getItemPriceString()						{return mItemPriceString;}
	public void 	setItemPriceString(String itemPriceString)	{this.mItemPriceString = itemPriceString;}
	public String 	getCurrencyUnit()							{return mCurrencyUnit;}
	public void 	setCurrencyUnit(String currencyUnit)		{this.mCurrencyUnit = currencyUnit;}
	public String 	getItemDesc()								{return mItemDesc;}
	public void 	setItemDesc(String itemDesc)				{this.mItemDesc = itemDesc;}
	public String 	getItemImageUrl()							{return mItemImageUrl;}
	public void 	setItemImageUrl( String itemImageUrl)		{this.mItemImageUrl = itemImageUrl;}
	public String 	getItemDownloadUrl()						{return mItemDownloadUrl;}
	public void 	setItemDownloadUrl(String itemDownloadUrl)	{this.mItemDownloadUrl = itemDownloadUrl;}
	public String 	getReserved1()								{return mReserved1;}
	public void 	setReserved1(String reserved1)				{this.mReserved1 = reserved1;}
	public String 	getReserved2()								{return mReserved2;}
	public void 	setReserved2(String reserved2)				{this.mReserved2 = reserved2;}
	public String 	getType()									{return mType;}
	public void 	setType(String type)						{this.mType = type;}
}
// ========================================================================



// Purchase attributes
// ========================================================================
class PurchaseVO extends ItemVO
{
	private String mPaymentId;
	private String mPurchaseDate;
	private String mPurchaseId;
	private String mVerifyUrl;
	
	public PurchaseVO(String mJsonItem)
	{
		try
		{	
			JSONObject jObject = new JSONObject( mJsonItem );
			mPaymentId = jObject.getString( "mPaymentId" );
			mPurchaseId = jObject.getString( "mPurchaseId" );
			setItemId( jObject.getString( "mItemId" ) );
			setItemName( jObject.getString( "mItemName" ) );
			setItemPrice( Double.parseDouble( ( jObject.getString( "mItemPrice" ) ) ) );
			setItemPriceString( jObject.getString( "mItemPriceString" ) );
			setCurrencyUnit( jObject.getString( "mCurrencyUnit" ) );
			setItemDesc( jObject.getString( "mItemDesc" ) );
			setItemImageUrl( jObject.getString( "mItemImageUrl" ) );
			setItemDownloadUrl( jObject.getString( "mItemDownloadUrl" ) );
			setReserved1( jObject.getString( "mReserved1" ) );
			setReserved2( jObject.getString( "mReserved2" ) );
			setVerifyUrl(jObject.getString( "mVerifyUrl" ) );
			long timeInMillis = Long.parseLong( jObject.getString( "mPurchaseDate" ) );
			mPurchaseDate = DateFormat.format( "yyyy.MM.dd hh:mm:ss", timeInMillis ).toString();
		}
		catch ( JSONException e ){ERR("SamsungIAB", "Exception: " + e.getMessage());}
	}

	public String 	getVerifyUrl()							{return mVerifyUrl;}
	public void 	setVerifyUrl(String _verifyUrl)			{this.mVerifyUrl = _verifyUrl;}
	public String 	getPaymentId()							{return mPaymentId;}
	public void 	setPaymentId(String mPaymentId)			{this.mPaymentId = mPaymentId;}
	public String 	getPurchaseDate()						{return mPurchaseDate;}
	public void 	setPurchaseDate(String mPurchaseDate)	{this.mPurchaseDate = mPurchaseDate;}
	public String 	getPurchaseId()							{return mPurchaseId;}
	public void 	setPurchaseId(String purchaseId)		{this.mPurchaseId = purchaseId;}
}
// ========================================================================



// Restore Transaction attributes
// ========================================================================
class InBoxVO extends ItemVO
{
	private String mPaymentId;
	private String mPurchaseDate;

	public InBoxVO(String strJson)
	{
		try
		{	
			JSONObject jObject = new JSONObject( strJson );
			setItemId( jObject.getString( "mItemId" ) );
			setItemName( jObject.getString( "mItemName" ) );
			setItemDesc( jObject.getString( "mItemDesc" ) );
			setItemPrice( Double.parseDouble( ( jObject.getString( "mItemPrice" ) ) ) );
			setCurrencyUnit( jObject.getString( "mCurrencyUnit" ) );
			setItemImageUrl( jObject.getString( "mItemImageUrl" ) );
			setItemDownloadUrl( jObject.getString( "mItemDownloadUrl" ) );
			long timeInMillis = Long.parseLong( jObject.getString( "mPurchaseDate" ) );
			mPurchaseDate = DateFormat.format( "yyyy.MM.dd hh:mm:ss", timeInMillis ).toString();
			mPaymentId = jObject.getString( "mPaymentId" );
			setReserved1( jObject.getString( "mReserved1" ) );
			setReserved2( jObject.getString( "mReserved2" ) );
			setType( jObject.getString( "mType" ) );
			setItemPriceString( jObject.getString( "mItemPriceString" ) );
		}
		catch( JSONException e ){ERR("SamsungIAB","Exception: " + e.getMessage());}
	}

	public String 	getPaymentId()		{return mPaymentId;}
	public String 	getPurchaseDate()	{return mPurchaseDate;}
}
// ========================================================================



// Verification process attributes
// ========================================================================
class VerificationVO
{
	private String mItemId;
	private String mItemName;
	private String mItemDesc;
	private String mPurchaseDate;
	private String mPaymentId;
	private String mPaymentAmount;
	private String mStatus;

	public VerificationVO( String strJson )
	{
		try
		{	
			JSONObject jObject = new JSONObject( strJson );
			mItemId        = jObject.getString( "itemId" );
			mItemName      = jObject.getString( "itemName" );
			mItemDesc      = jObject.getString( "itemDesc" );
			mPurchaseDate  = jObject.getString( "purchaseDate" );
			mPaymentId     = jObject.getString( "paymentId" );
			mPaymentAmount = jObject.getString( "paymentAmount" );
			mStatus        = jObject.getString( "status" );
		}
		catch( JSONException e ){ERR("SamsungIAB", "Exception: " + e.getMessage());}
	}

	public String 	getmPaymentId()		{return mPaymentId;}
	public String 	getmStatus()		{return mStatus;}
}
// ========================================================================

#endif