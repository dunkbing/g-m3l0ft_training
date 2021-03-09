#if USE_KDDI_GIFTING
package APP_PACKAGE.kddigifting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import APP_PACKAGE.GLUtils.Encrypter;
import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.SUtils;
import java.util.Random;
import android.provider.Settings.Secure;
import android.net.wifi.WifiManager;

import APP_PACKAGE.R;

public class KDDIGiftingHelper
{
	SET_TAG("KDDIGifting");

	private static InputLayout 		mView;
	private static Bitmap 			mGLIcon = null;
	private static Bitmap 			mGLHeader = null;
	private static int 				headerHeight = 70;
	
	private static KDDIGiftingHelper 	mThis;
	
	private static int 				currentLanguage = 0; 
	private static String 			GGI_Game = "";
	private static String 			GameVersion = GAME_VERSION_NAME;
	private static String 			UserName = "";
	private static String 			Password = "";
	private static String			UserId = "";
	
	private Display 				display;

	private static int 				SCR_W;
	private static int 				SCR_H;
	
	private String 					gameid_to_launch = "";
	private String 					LAST_RES_URL = "";
	private boolean 				didCloseButtonLoad = false;
	private boolean					isStopped = false;	
	private boolean					isInBilling = false;
	private boolean					wasAutoLoggedIn = false;
	
	private int						configOrientation = LANDSCAPE;
	private int						currentOrientation = LANDSCAPE;
	
	private static final int		LANDSCAPE = 0;
	private static final int		PORTRAIT = 1;
	private static final int		SENSOR = 2;
	
	public static final String		STRING_SHARED_PREFS = "KDDIGiftingPrefsFile";
	
	private static cMessageQueueThread	messageQueueThread = new cMessageQueueThread();
	private static boolean isFirstRun = true;
	public static boolean			isLoggedIn = true; //BUG FIX FOR 5020534, AGSB 15/05/2012
	
	public static WifiManager wifiMgr; 
		
	private static class cMessageQueueThread implements Runnable
	{
		boolean isRunning = false;
		Queue <PopupMessage> messageQueue = new LinkedList <PopupMessage>();
		cMessageQueueThread classInstance;
		
		public cMessageQueueThread()
		{
			classInstance = this;
		}
	
		class PopupMessage
		{
			Activity context = null;
			RelativeLayout anchor = null;
			String message = null;
			boolean show_icon = false;
			Bitmap icon = null; 
			Bitmap header = null; 
			
			public PopupMessage(Activity c, RelativeLayout a, String m, boolean s, Bitmap i, Bitmap h)
			{
				context = c; anchor = a; show_icon = s;
				message = m; header = h; icon = i;
			}
			
			public void show()
			{
				KDDIGiftingHelper.showPopupMessage(context, anchor, message, true);//, icon, header);
			}
		}
		
		public void addMessage(Activity context, RelativeLayout anchor, String message, boolean show_icon, Bitmap icon, Bitmap header)
		{
			try
			{
				messageQueue.offer(new PopupMessage(context, anchor, message, show_icon, icon, header));
				if (!isRunning)
					new Thread(classInstance).start();
			}
			catch(Exception e) {DBG_EXCEPTION(e);}
		}
		
		public void run()
		{
			isRunning = true;
			while(!messageQueue.isEmpty())
			{
				PopupMessage message = messageQueue.poll();
				if(message != null)
				{
					message.show();
					try {
						Thread.sleep(4000);
					} catch(InterruptedException e) {}
				}
			}
			isRunning = false;
		}
	}
	
	private static void showPopupMessage(final Activity thiz, final RelativeLayout anchor, final String message, final boolean showIcon)//, final Bitmap icon, final Bitmap header)
	{
		DBG(TAG, "showPopupMessage(): " + message);
		new Thread(new Runnable() { public void run()
		{		
			thiz.runOnUiThread(new Runnable() { public void run()
			{
			      LayoutInflater inflater;
            inflater = (LayoutInflater) thiz.getSystemService(Context.LAYOUT_INFLATER_SERVICE);              
      
            final LinearLayout popup = (LinearLayout) inflater.inflate(R.layout.gifting_popup_layout, null);
      			ImageView imageV = (ImageView)popup.findViewById(R.id.gifting_popup_ok_button);
      			TextView textV = (TextView)popup.findViewById(R.id.gifting_popup_text);
      		      		
//       		popup.setBackgroundResource(R.drawable.gifting_header);
//     			if (header != null)
//     			{
//     				popup.setBackgroundDrawable(new BitmapDrawable(header));
//     			}
//     			else
//     			{
//     				popup.setBackgroundColor(Color.argb(175, 0, 0, 0));	
//     			}		
          
			    popup.setVisibility(View.INVISIBLE);
			    
          float density = thiz.getResources().getDisplayMetrics().density;
          
          int layoutHeight = (int)thiz.getResources().getDimension(R.dimen.gifting_popup_layout_height);
          int layoutWidth= (int)thiz.getResources().getDimension(R.dimen.gifting_popup_layout_width);
          
		  //Alignment of the welconme poup
          LayoutParams lpPopup = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 	  			lpPopup.addRule(RelativeLayout.CENTER_HORIZONTAL);
   				lpPopup.addRule(RelativeLayout.CENTER_VERTICAL);
   				
//   				if (icon == null)
//   					textV.setText(message);
//   				else
  				{
     				android.text.SpannableStringBuilder spannableMessage = new android.text.SpannableStringBuilder( message );
     				String acornImageString = "@{objImage}";
  					int startIndex = message.indexOf(acornImageString);
  					DBG(TAG,"span "+startIndex+"   "+acornImageString.length());
  					
//   					android.text.style.ImageSpan span = new android.text.style.ImageSpan(thiz.getBaseContext(), R.drawable.icon_gift, android.text.style.ImageSpan.ALIGN_BASELINE);
//   					spannableMessage.setSpan( new android.text.style.ImageSpan( icon ), startIndex, startIndex + acornImageString.length(), android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE );	
  					spannableMessage.setSpan( new android.text.style.ImageSpan(thiz.getBaseContext(), R.drawable.icon_gift, android.text.style.ImageSpan.ALIGN_BASELINE), startIndex, startIndex + acornImageString.length(), android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE );
  					
  					startIndex = message.indexOf(acornImageString, (startIndex + acornImageString.length()));
  					if(startIndex > 0 )
  					{
  						DBG(TAG,"span "+startIndex+"   "+acornImageString.length());
  						spannableMessage.setSpan( new android.text.style.ImageSpan(thiz.getBaseContext(), R.drawable.icon_gift, android.text.style.ImageSpan.ALIGN_BASELINE), startIndex, startIndex + acornImageString.length(), android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE );
  					}

  					textV.setText(spannableMessage, android.widget.TextView.BufferType.SPANNABLE );
  					
//   					imageV.setBackgroundResource(R.drawable.gifting_ok);
  				}

				anchor.addView(popup,lpPopup);
// 				anchor.addView(popup);
				
        popup.setVisibility(View.VISIBLE);
        
        //Animation
				AnimationSet set = new AnimationSet(true);
		
				Animation animation = new AlphaAnimation(0.0f, 1.0f);
				animation.setDuration(100);
				set.addAnimation(animation);
		
				animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
				);
				animation.setDuration(500);
				
				set.addAnimation(animation);
				
				popup.setVisibility(View.VISIBLE);		
				popup.startAnimation(animation);
			 popup.setOnClickListener(new View.OnClickListener(){
			   public void onClick(View arg0)
			   {
      			{
      				AnimationSet set2 = new AnimationSet(true);
      
      				Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
      				animation2.setDuration(100);
      				set2.addAnimation(animation2);
      
      				animation2 = new TranslateAnimation(
      					Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
      					Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f
      				);
      				animation2.setDuration(500);
      				set2.addAnimation(animation2);
      				
      				popup.startAnimation(animation2);
      				
      				anchor.removeView(popup);
            }
         }
         });	
				
			}});
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}			
	
		}}).start();	
	}
	
	private static Activity mGameActivity;
	private static RelativeLayout mLayout;
	//tiny: have to call it where suitable, first of all to init 2 important things
	public static void Init(Activity activity, RelativeLayout layout)
	{
		mGameActivity = activity;
		mLayout = layout;
		
		nativeInit();
		
		wifiMgr = (WifiManager)mGameActivity.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
		PerformCheckUserCanReceiveKDDIGift();
	}

	public static int GetRandomNumber(int maximum, int minimum)
	{
		int value = minimum + (int)(Math.random() * (maximum - minimum));
		return value;
	}
	public static int GetRandomNumber()
	{
		return GetRandomNumber(10,10000);
	}
	
	  public static String getMac()
    {
		try
		{
			String mac = null;
			mac = wifiMgr.getConnectionInfo().getMacAddress();
			if (mac != null && mac.length() > 0)
			{
				return mac;
			}
		} 
		catch (Exception e) {
			DBG(TAG, "Exception getMac"+e.toString());	
			}
		return "00:00:00:00:00:00";
    }
    
    public static String getMac2()
    {
		try
		{
			String mac = null;
			mac = wifiMgr.getConnectionInfo().getMacAddress();
			if (mac != null && mac.length() > 0)
			{
				return mac.replace(":","");
			}
		} 
		catch (Exception e) {
			DBG(TAG, "Exception getMac2 "+e.toString());
			
			}
		return "00:00:00:00:00:00".replace(":","");
    }
    
		
	public static String getCountryCode(){
    return java.util.Locale.getDefault().getCountry();
  }
  
  public static String getHardwareName(){
    String model = Build.MANUFACTURER+"_"+Build.MODEL;
    return model.replaceAll(" ","_");
  }
  
  public static String getSystemVersion(){
    return Build.VERSION.RELEASE;
  } 

	public static String getIdentifierUpperCase()
	{
		String identifierUpperCase = Device.getDeviceId();
		identifierUpperCase=identifierUpperCase.toUpperCase();
		return identifierUpperCase;
	}
	
	public static String getIdentifierLowerCase()
	{
		String identifierLowerCase = Device.getDeviceId();
		identifierLowerCase=identifierLowerCase.toLowerCase();
		return identifierLowerCase;
	}
	
	public static final String md5(final String s)
	{
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
	 
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
	 
		} catch (NoSuchAlgorithmException e) {
			DBG_EXCEPTION(e);
		}
		return "";
	}
	
	public static String CreateUserID()
	{
		String uid = "12345";
		try
		{
			SharedPreferences settings = SUtils.getContext().getSharedPreferences(KDDIGiftingHelper.STRING_SHARED_PREFS, 0);
			uid = settings.getString("uid", null);
			
			//the first time request value, we create the new one
			if (uid == null) 
			{
				uid = getIdentifierLowerCase();// + GetRandomNumber();
				
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("uid", uid);
				editor.commit();
			}
			else
				uid = uid.replaceAll(" ", "");

		}
		catch(Exception e)
		{
			 uid = "12345";
		}
		
		DBG(TAG, " UserID === " + uid);
		
		return uid + "89";
	}
	
	public static void SetImageText(TextView txtText, String msg, Bitmap img)
	{
		String message = msg;
		// int len = imgs.getLength();
		if ( img == null )
		{
			return;
		}
		android.text.SpannableStringBuilder spannableMessage = new android.text.SpannableStringBuilder( message );
		
		String imgTakenString = "@{objImage}";

		int startIndex = message.indexOf(imgTakenString);

		spannableMessage.setSpan( 
			new android.text.style.ImageSpan( img ),
			startIndex, 
			startIndex + imgTakenString.length(),
			android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE 
		);
							
		startIndex = message.indexOf(
										imgTakenString, 
										(startIndex + imgTakenString.length())
									);
		if(startIndex > 0 )
		{
			spannableMessage.setSpan( 
				new android.text.style.ImageSpan( img ), 
				startIndex, 
				startIndex + imgTakenString.length(), 
				android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE 
			);
		}
		if (img == null)
			txtText.setText(message);
		else
			txtText.setText(spannableMessage, android.widget.TextView.BufferType.SPANNABLE );
	}
	
		
	public static boolean mIfUserCanReceiveKDDIGift = false;
	
	public static int CanUserReceiveKDDIMonthlyGift()
	{
		int value = 0;
		try{
			value = (KDDIGiftingHelper.mIfUserCanReceiveKDDIGift? 1:0);
		}catch(Exception eee)
		{		
		}
		
		DBG(TAG, "CanUserReceiveKDDIMonthlyGift === " + value);
		
		return value;
	}
	
	//Save Purchase ID
	public static String mPurchaseID = null;

	static class GiftObject
	{
		public String ID="GiftID";
		public String Name="GiftName";
		public String Type="cash";
		public String Amount="1";
		
		public void Init(String id, String name, String type, String amount)
		{
			ID = id;
			Name = name;
			Type = type;
			Amount = amount;
		}
	}	
	
	static GiftObject mGiftObject;
	public static String GetPopupMessage()
	{
		//lang = 1 ==> JP
		//else for all remaining lang
		if (mGiftObject==null)
			mGiftObject = new GiftObject();
		
		// String message = "You got a KDDI Monthly gift: " + mGiftObject.Name;
		// String message = "As a Smart Pass user, you have received a [amount] [acorn image] gift! Come and have fun every month to get more [acorn image]!";
		String message="";
		if(mGameActivity!=null)
			message = mGameActivity.getString(R.string.kddi_popup_message);
		message = message.replace("@{amount}",mGiftObject.Amount);
		
		// message += " Name: " + mGiftObject.Name;
		// message += " Amount: " + mGiftObject.Amount;
		return message;
	}
	//tiny: PerformCheckUserCanReceiveKDDIGift
	/*
	Parameters:
	1.	uid: use to indicate which user. (a random string of X chars + seed) Example: 12345 
	2.	gid: use to indicate which game. Example: ASP7 
	3.	sign:
	?	key = gameloft_kddi_gift
	?	sign = md5(uid_gid_key)
	?	Example: md5(12345_ASP7_gameloft_kddi_gift)
	
	Output:
		?	purchase_id: It's a order ID. Get it from the 1st API. 
			Example: 12345678
		? 
	*/
	// public static void PerformCheckUserCanReceiveKDDIGift(String gid, String udid, String  sign)
	public static void PerformCheckUserCanReceiveKDDIGift()
	{
		DBG(TAG, "Checking if user can receive gift...");
		final String URL = "http://confirmation.gameloft.com/partners/kddi_gift/gift_verification.php?sign=_SIGN_&gid=_GID_&uid=_UID_";
		new Thread(new Runnable() { public void run() 
		{
			String lang = Locale.getDefault().getLanguage().toUpperCase();
			String uid = CreateUserID();
			//Gamecode GGC_GAME_CODE
			String gid = "IAKD";//GGC_GAME_CODE;			
			/*
			sign:
			?	key = gameloft_kddi_gift
			?	sign = md5(uid_gid_key)
			?	Example: md5(12345_ASP7_gameloft_kddi_gift)
			*/
			String sign = "";
			String key = "gameloft_kddi_gift";
			sign = uid + "_" + gid + "_" + key;
			sign = md5(sign);

			String request = URL.replace("_SIGN_", sign)
											.replace("_GID_", gid)
											.replace("_UID_", uid)
											.replaceAll(" ", "");
											
			String response = getHttpResponse(request);	
			DBG(TAG, "userCanReceiveKDDIGift response = " + response);
			if (response == null)
				response = "FAILURE|001";
			
			String [] params = response.split("\\|");
			String message = "";
			
			//tiny: case of success
			if (response.contains("SUCCESS"))
			{
				// Params: OPERATION_RESULT|CONTENT_ID|CONTENT_NAME|CONTENT_TYPE|CONTENT_AMOUNT|PURCHASE_ID|SIGN
				String result = params[0];
				String giftID = params[1];
				String giftName = params[2];
				String giftType = params[3];
				String giftAmount = params[4];
				String purchaseID = params[5];
				
				if ((purchaseID != null || purchaseID != "") )
					mPurchaseID = purchaseID;
				
				String response_sign = params[6].trim();
				String checkSign = giftID + "_" + purchaseID + "_" + key;
				checkSign = md5(checkSign.trim());		
				
				if ( !checkSign.contains(response_sign) )
				{
					message = "Error..... Sign wrong....";
					//set the variable in JNI to false
					KDDIGiftingHelper.mIfUserCanReceiveKDDIGift = false;
					KDDIGiftingHelper.UpdateKDDIMonthlyGiftingConfig("0");
					
				}
				else
				{
					//Send this gift to update in game, increase the amount of coins
					//set the variable in JNI to true
					//int gIsAllowedToReceiveGIF = 1
					//notifyKDDISendMonthlyGift();
					
					KDDIGiftingHelper.mIfUserCanReceiveKDDIGift = true;		
					KDDIGiftingHelper.UpdateKDDIMonthlyGiftingConfig("1");			
				}
				
				//update the gift info
				
				DBG(TAG, "PerformCheckUserCanReceiveKDDIGift: message " +  message);
				if (mGiftObject==null)
					mGiftObject = new GiftObject();
				mGiftObject.Init(giftID, giftName, giftType, giftAmount);
			}
			//case of FAILURE			
			else
			{
				if ( params[1].equals("001") )
					message = "Sign Wrong...";
				else if ( params[1].equals("002") )
					message = "Purchase failed...";
				else if ( params[1].equals("003") )
					message = "Params errors...";
				
				KDDIGiftingHelper.UpdateKDDIMonthlyGiftingConfig("0");				
				DBG(TAG, "ERROR: " +  message);		
			}			
		}}).start();
	}

	//Tiny: Disable Gifting on server	
	/*
		Common values:
	?	uid: use to indicate which user. (a random string of X chars + seed) Example: 12345 
	?	gid: use to indicate which game. Example: ASP7 
	?	purchase_id: It's a order ID. Get it from the 1st API. Example: 12345678 
	?	sign:
			key = gameloft_kddi_gift 
			sign = md5(uid_gid_purchase_id_key)
		Example: 
			md5(12345_ASP7_12345678_gameloft_kddi_gift)
	*/	
	public static void DisableKDDIMonthlyGift()
	//public static void DisableKDDIMonthlyGift(String uid, String gid, String purchaseID, String sign)
	{
		if (mPurchaseID == null)
			return;
		final String URL = "http://confirmation.gameloft.com/partners/kddi_gift/gift_invalidate.php?sign=_SIGN_&gid=_GID_&uid=_UID_&purchase_id=_PURCHASEID_";
		new Thread(new Runnable() { public void run() 
		{
			String lang = Locale.getDefault().getLanguage().toUpperCase();
			String uid = CreateUserID();
			//Gamecode GGC_GAME_CODE
			String gid = "IAKD";//GGC_GAME_CODE;

			//sign = md5(uid_gid_purchase_id_key)
			String sign = "";
			String key = "gameloft_kddi_gift";			
			sign = uid + "_" + gid + "_" + mPurchaseID + "_" + key;
			sign = md5(sign);
						
			String purchaseID = mPurchaseID;
			
			String request = URL.replace("_SIGN_", sign)
											.replace("_GID_", gid)
											.replace("_UID_", uid)
											.replace("_PURCHASEID_", purchaseID)
											.replaceAll(" ", "");
											
			String response = getHttpResponse(request);
					
			if (response == null)
				response = "FAILURE";
			
			String [] params = response.split("\\|");
			String message = "";
			
			//tiny: case of success
			if (response.contains("SUCCESS"))
			{

				message = "Disable gifting: successfullly.";
				KDDIGiftingHelper.mIfUserCanReceiveKDDIGift = false;
				UpdateKDDIMonthlyGiftingConfig("0");
				// PopupKDDIMonthlyMessage("Your monthly gift will be available next month!");
			}
			//case of FAILURE			
			else
			{
				message = "Disable gifting: Error";
				KDDIGiftingHelper.mIfUserCanReceiveKDDIGift = false;
			}
			DBG(TAG, message);
		}}).start();
	}
	
	public static int CheckIfNeedToDisableKDDIMonthlyGift()
	{
		boolean doWeNeed = false;
		try
		{
			SharedPreferences settings = SUtils.getContext().getSharedPreferences(KDDIGiftingHelper.STRING_SHARED_PREFS, 0);
			String value = settings.getString("NeedToDisableKDDIMonthlyGift", null);
			DBG(TAG, "CheckIfNeedToDisableKDDIMonthlyGift...");
			
			if (value == null) 
			{
				doWeNeed = false;
			}
			else
			{
				if ( value == "1" )
					doWeNeed = true;
				else
					doWeNeed = false;
			}
		}
		catch(Exception e)
		{
			doWeNeed = false;
		}
		DBG(TAG, "Need to disable: " + doWeNeed);
		return doWeNeed? 1:0;
		
	}
	
	public static void UpdateKDDIMonthlyGiftingConfig(String value)
	{
		try
		{
			SharedPreferences settings = SUtils.getContext().getSharedPreferences(KDDIGiftingHelper.STRING_SHARED_PREFS, 0);
			String tmp = settings.getString("NeedToDisableKDDIMonthlyGift", null);
			DBG(TAG, "UpdateKDDIMonthlyGiftingConfig...");
			if (value != null) 
			{
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("NeedToDisableKDDIMonthlyGift", value);
				editor.commit();
			}
			DBG(TAG, "UpdateGiftingConfig result: " + value);
		}
		catch(Exception e)
		{
		}
	}
	
	
	public static void PopupKDDIMonthlyMessage(final Activity thiz, final RelativeLayout anchor, String msg)
	{
		final String message = msg;
		new Thread(new Runnable() 
		{ 
			public void run() 
			{
				String lang = Locale.getDefault().getLanguage().toUpperCase();
				{
					
// 					Bitmap gl_icon = null;
// 					Bitmap gl_header = null;
						
// 					//tiny: create the background
// 					gl_header = fetchImage("http://interstatic01.gameloft.com/igp/resources/glive/NEWDESIGN/headerpopup1.png");
// 					// gl_icon = fetchImage("url of icon");
// 					gl_icon = fetchImage("http://ffxiv.gamerescape.com/w/images/thumb/2/27/Iron_Acorn_Icon.png/50px-Iron_Acorn_Icon.png");
					showPopupMessage(thiz, anchor, message, true);//, gl_icon, gl_header);
				}
			}
		}).start();
	}
	
	public static int getKDDIGiftAmount()
	{
		if (mGiftObject==null)
		{
			DBG(TAG, "gift null");
			return 0;
		}
		else
		{
			try{
				DBG(TAG, "amount in string "+mGiftObject.Amount);
				int ammm = Integer.parseInt(mGiftObject.Amount);
				DBG(TAG, "amount in int "+ammm);
				return ammm;
			}catch(Exception e){DBG(TAG, "exception, returning 0");return 0;}
		}
	}
	
	public static void PopupKDDIMonthlyMessage(String message)
	{
		DBG(TAG, "Popup Gift message = "+message);
		KDDIGiftingHelper.PopupKDDIMonthlyMessage(mGameActivity, mLayout, message);
	}
	public static void PopupKDDIMonthlyMessage()
	{
		PopupKDDIMonthlyMessage(GetPopupMessage());
	}
		
	private static void saveToCache(Bitmap picture, String filename)
	{		
		try
		{
			File cacheDir = SUtils.getContext().getCacheDir();
			File cacheFile = new File(cacheDir, filename);			
			FileOutputStream fos = new FileOutputStream(cacheFile);
			picture.compress(Bitmap.CompressFormat.PNG, 100, fos);
		} catch(Exception e)
		{
			DBG_EXCEPTION(e);
			DBG(TAG, "Exception while writing to cache: " + e.getMessage());
		}
	}
	
	private static Bitmap readFromCache(String filename)
	{
		Bitmap picture = null;
		try
		{
			File cacheDir = SUtils.getContext().getCacheDir();
			File cacheFile = new File(cacheDir, filename);
			if (cacheFile.exists())
			{								
				FileInputStream fis = new FileInputStream(cacheFile);
				picture = BitmapFactory.decodeStream(fis);							
			}
		} catch(Exception e) 
		{
			DBG_EXCEPTION(e);
			DBG(TAG, "Exception while reading from cache: " + e.getMessage());
			picture = null;
		}				
		
		return picture;
	}
	
	public static String getUsername(Context thiz)
	{
		try
		{
			SharedPreferences settings = thiz.getSharedPreferences(KDDIGiftingHelper.STRING_SHARED_PREFS, 0);
			String user = settings.getString("username", null);
			if (user != null) 
				user = Encrypter.decrypt(user).replaceAll(" ", "");
			else 
				user = "";
			return user;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public static String getPassword(Context thiz)
	{
		try
		{
			SharedPreferences settings = thiz.getSharedPreferences(KDDIGiftingHelper.STRING_SHARED_PREFS, 0);
			String pass = settings.getString("password", null);
			if (pass != null) 
				pass = Encrypter.decrypt(pass).replaceAll(" ", "");
			else 
				pass = "";
			return pass;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public static String getUsername()
	{
		try
		{
			SharedPreferences settings = SUtils.getContext().getSharedPreferences(KDDIGiftingHelper.STRING_SHARED_PREFS, 0);
			String user = settings.getString("username", null);
			if (user != null) 
				user = Encrypter.decrypt(user).replaceAll(" ", "");
			else 
				user = "";
			return user;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public static String getPassword()
	{
		try
		{
			SharedPreferences settings = SUtils.getContext().getSharedPreferences(KDDIGiftingHelper.STRING_SHARED_PREFS, 0);
			String pass = settings.getString("password", null);
			if (pass != null) 
				pass = Encrypter.decrypt(pass).replaceAll(" ", "");
			else 
				pass = "";
			return pass;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	private class InputLayout extends RelativeLayout
	{
		public InputLayout(Context c) 
		{
			super(c); 
		}
		
		@Override
		public boolean dispatchKeyEventPreIme(KeyEvent event)
		{
			return super.dispatchKeyEventPreIme(event);
		}
	}
	
    private static Bitmap fetchImage(String urlstr)
	{
	    try
	    {
	        URL url = new URL(urlstr);

	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream is = connection.getInputStream();
	        Bitmap img = BitmapFactory.decodeStream(is);
	        return img;
	    }
	    catch (MalformedURLException e)
	    {
	        DBG(TAG, "RemoteImageHandler: fetchImage passed invalid URL: " + urlstr);
	    }
	    catch (IOException e)
	    {
	        DBG(TAG, "RemoteImageHandler: fetchImage IO exception: " + e);
	    }
	    return null;
	}
	        

    static private String getHttpResponse(String RequestUrl)
    {
    	String response_text = null;
		BufferedReader stream_in = null;    	
		try
		{
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(RequestUrl);
			HttpResponse response = client.execute(request);			
			stream_in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer buffer = new StringBuffer("");
            String line = "";
            while ((line = stream_in.readLine()) != null)
            {
            	buffer.append(line + "\n");
            }
            stream_in.close();
            response_text = buffer.toString();
		} catch (Exception e) { 
			DBG_EXCEPTION(e);
		}		
		finally 
		{
            if (stream_in != null) 
            {
                try 
                {
                	stream_in.close();
                } 
                catch (Exception e) { 
                	DBG_EXCEPTION(e);
                }
            }
		}
		
		return response_text;
    }
    
public static native void nativeInit();

}
#endif