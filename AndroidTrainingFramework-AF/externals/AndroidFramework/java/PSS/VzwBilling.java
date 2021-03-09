#if PSS_VZW
package APP_PACKAGE.pss;

import android.util.Log;
import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.provider.Settings;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.NetworkInfo;

import java.net.URL;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.CompoundButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.TextView;

import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import APP_PACKAGE.R;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.Device;

public class VzwBilling  /*extends Activity*/ 
{
	private String PSSUrl;
	private String PSSCommand;
	private String PSSItem;
	private String PSSGameCode;
	
	String apk_url;
	
	protected static boolean firstime = true;
	
	//Declare Objects
	WifiManager wifi;
	
	static VzwConnect VB	= new VzwConnect();
	static VzwGetNpost GNP 	= new VzwGetNpost();

  
	//String response;	
	String DeviceModelWithoutBlanks;
	
	final static int STATE_IDLE 		= 0;
	final static int STATE_LOGIN 		= 1;
	final static int STATE_REQUEST_DATA = 2;
	final static int STATE_CHECKOUT 	= 3;
	final static int STATE_PURCHASE 	= 4;
	final static int STATE_NOTIFYGL 	= 5;
	
	int mState	= STATE_IDLE;
	
	final static int STATUS_SUCCESS		= 0;
	final static int STATUS_ERROR_CODE	= -1;
	final static int STATUS_ERROR_SERVER_RESPONSE_EMPTY	= -2;
	final static int STATUS_ERROR_SERVER_GL	= -3;
	final static int STATUS_ERROR_NO_DATA_CONNECTION	= -4;
	int mStatus;
	
	
	protected ProgressDialog dialog = null;
	
	protected AlertDialog.Builder mBuyDialog;
	protected AlertDialog.Builder mRetryDialog;
	protected AlertDialog.Builder mResultDialog;

	
	private Activity mPSSActivity;
	
	private boolean mFirstTime = true;
	private boolean mWifiEnabled;
	private boolean mEnablingMobileConnection;
	//TEST rety options
	/*
	boolean isTestRetry = false;
	private boolean getTestResult()
	{
		isTestRetry = !isTestRetry;
		
		return isTestRetry;
	}*/
/////////////////////////////////////////////////////////////////////
// AsyncTasks for Buy, Checkout & Notify GL
/////////////////////////////////////////////////////////////////////
	abstract class GLAsyncTask
	{
		
		private boolean mCancelled = false;
		public GLAsyncTask ()
		{
			mCancelled = false;
		}
		
		protected abstract Integer doInBackground(String... params);
		
		protected void onPreExecute() {
		
		}
		
		protected void onPostExecute(Integer result) 
		{
		
		}

		public final void cancel(boolean Interrupt) {
			mCancelled = Interrupt;
		}
		
		public final boolean isCancelled() {
			return mCancelled;
		}
		
		protected void onCancelled() {
		
		}
		
		
		private void finishTask(final Integer result)
		{
			mPSSActivity.runOnUiThread(new Runnable ()
			{
				public void run ()
				{
					try
					{
						if (mCancelled) 
							onCancelled();
						else
							onPostExecute(result);
					}
					catch (Exception e)
					{
						DBG_EXCEPTION(e);
					}
				}
			});
		}
		
		public final void execute(final String... params) 
		{
			onPreExecute();
			new Thread( new Runnable()
			{
				public void run()
				{
					int Result = doInBackground(params);
					finishTask(Result);
				}
			}
		#if !RELEASE_VERSION
			,"Thread-doInBG"
		#endif
			).start();
				
		}
	}
	
	/*class ARb extends GLAsyncTask
	{
		public ARb()
		{
		}
	
	}*/
	//class ARequestBuy extends AsyncTask<String, Void, Integer>
	class ARequestBuy extends GLAsyncTask
	{
		public DialogInterface.OnCancelListener arOnCancelListener = new DialogInterface.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				DBG("PSS", "VZW Cancel ARequestBuy");
				VzwBilling.ARequestBuy.this.cancel(true);
			}
		};
		protected void onPreExecute() 
		{
			//DBG("PSS", "[onPreExecute]");
			//show please wait message
			dialog = new ProgressDialog(mPSSActivity);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setOnCancelListener(arOnCancelListener);
			dialog.setMessage(mPSSActivity.getString(R.string.pss_vzw_please_wait));
			dialog.show();
		}
		
		protected Integer doInBackground(String... url) 
		{
			DBG("PSS", "[ARequestBuy::doInBackground]");
			WifiManager wifiManager = (WifiManager) (mPSSActivity.getSystemService(Context.WIFI_SERVICE));
			boolean wifiEnabled  = wifiManager.isWifiEnabled();
			
			//safe wifi first time state
			if(mFirstTime)
			{
				mFirstTime = false;
				mWifiEnabled = wifiEnabled;
			}
			
			//check if need disable wifi
			if(wifiEnabled)
			{
				mEnablingMobileConnection = true;
				
				//wait for webview
				try {	Thread.sleep(500); } catch(Exception e) {}
				wifiManager.setWifiEnabled(false);
				DBG("PSS","Disabling Wi-Fi");
				while((wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING))
				{
					try {	Thread.sleep(500); } catch(Exception e) {}
					if (isCancelled()){
						mStatus = STATUS_ERROR_CODE;
						return new Integer(STATUS_ERROR_CODE);
					}
				}
				DBG("PSS","Activating mobile data connection");
				ConnectivityManager cm = (ConnectivityManager) mPSSActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
				State mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
				long TIME_OUT = 12000;
				while((mobile != State.CONNECTED) && (TIME_OUT>0))
				{
					try {	Thread.sleep(500); } catch(Exception e) {}
					mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
					TIME_OUT -= 500;
					if (isCancelled()){
						mStatus = STATUS_ERROR_CODE;
						return new Integer(STATUS_ERROR_CODE);
					}
				}
				if(mobile != State.CONNECTED)
				{
					DBG("PSS", "No data connection error");
					setStateError(STATUS_ERROR_NO_DATA_CONNECTION);
					mEnablingMobileConnection = false;
					setURL(url[0]);	//Crash fix: when 3G data isn't available.
					return new Integer(mStatus);
				}
				mEnablingMobileConnection = false;
			}else{
				//check for data connection
				DBG("PSS","Waiting mobile data connection");
				ConnectivityManager cm = (ConnectivityManager) mPSSActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
				State mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
				long TIME_OUT = 12000;
				while((mobile != State.CONNECTED) && (TIME_OUT>0)){
					try {	Thread.sleep(500); } catch(Exception e) {}
					mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
					TIME_OUT -= 500;
					if (isCancelled()){
						mStatus = STATUS_ERROR_CODE;
						return new Integer(STATUS_ERROR_CODE);
					}
				}
				DBG("PSS","mobile data connection state="+mobile);
			}
			setURL(url[0]);
			//finish job when cancelling
			if (isCancelled()){
				mStatus = STATUS_ERROR_CODE;
				return new Integer(STATUS_ERROR_CODE);
			}
			requestLogin();
			
			return new Integer(mStatus);
		}
		@Override
		protected void onCancelled()
		{
			DBG("PSS","onCancelled ARequestBuy");
			if(dialog != null)
				dialog.dismiss();	
			Pss.handledURL = null;
			VB.close();
		}

		@Override
		protected void onPostExecute(Integer result) 
		{
			DBG("PSS", "[onPostExecute]");
			Pss.handledURL = null;
			dialog.dismiss();
			
			if(result == STATUS_SUCCESS)
			{
				confirmCheckout();
			}
			else	//an error has occurs
			{
				mRetryDialog = new AlertDialog.Builder(mPSSActivity);
				mRetryDialog.setMessage(parseString(R.string.pss_vzw_try_again))
				   .setCancelable(false)
				   
				   //yes button
				   .setPositiveButton(getString(R.string.pss_vzw_yes), new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface dialog, int id) 
					   {
							new ARequestBuy().execute(PSSUrl);
					   }
				   })
				   
				   //no button
				   .setNegativeButton(getString(R.string.pss_vzw_no), new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface dialog, int id) 
					   {
							dialog.cancel();
					   }
				   });
		
				AlertDialog alert = mRetryDialog.create();
				alert.show();
			}
		}
	}
	
	//class ARequestCheckout extends AsyncTask<String, Void, Integer>
	class ARequestCheckout extends GLAsyncTask
	{
		
		protected void onPreExecute() 
		{
			//show please wait message
			DBG("PSS", "[onPreExecute]");
			dialog = new ProgressDialog(mPSSActivity);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);//rjsc added, let LOADING finish 
			dialog.setMessage(mPSSActivity.getString(R.string.pss_vzw_please_wait));
			dialog.show();
		}
		
		protected Integer doInBackground(String... url) 
		{
			//if connection was lost, wait for connection 
			ConnectivityManager cm = (ConnectivityManager) mPSSActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
			State mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
			long TIME_OUT = 5000;
			while((mobile != State.CONNECTED) && (TIME_OUT>0)){
				try {	Thread.sleep(500); } catch(Exception e) {}
				mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
				TIME_OUT -= 500;
				DBG("PSS","Waiting mobile data connection");
			}
			
			Checkout();
			
			// if(mStatus == STATUS_SUCCESS)
			// {
				// INFO("PSS", "Success: "+ getMessage());	
			// }
			// else
			// {
				// ERR("PSS", "Error: " + getMessage());
			// }
			
			
			/*if(mStatus == STATUS_SUCCESS && getTestResult())
			{
				return new Integer(-1);
			}*/
			
			return new Integer(mStatus);
		}
		
		protected void onPostExecute(Integer result) {
			DBG("PSS", "[onPostExecute]");
			Pss.handledURL = null;
			dialog.dismiss();
			
			if(result == STATUS_SUCCESS)
			{
				new ARequestGL().execute(PSSUrl);
			} 
			else 
			{
				//show retry dialog
				mRetryDialog = new AlertDialog.Builder(mPSSActivity);
				mRetryDialog.setMessage(parseString(R.string.pss_vzw_try_again))
				   .setCancelable(false)
				   
				   //yes button
				   .setPositiveButton(getString(R.string.pss_vzw_yes), new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface dialog, int id) 
					   {
							new ARequestCheckout().execute(PSSUrl);
					   }
				   })
				   .setNegativeButton(getString(R.string.pss_vzw_no), new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface dialog, int id) 
					   {
							dialog.cancel();
					   }
				   });
		
				AlertDialog alert = mRetryDialog.create();
				alert.show();
			}
		}
	} //ARequestCheckout

	//class ARequestGL extends AsyncTask<String, Void, Integer>
	class ARequestGL extends GLAsyncTask
	{
		
		protected void onPreExecute() 
		{
			DBG("PSS", "[onPreExecute]");
			apk_url = null;
			dialog = new ProgressDialog(mPSSActivity);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setMessage(mPSSActivity.getString(R.string.pss_vzw_please_wait));
			dialog.show();
		}
		
		protected Integer doInBackground(String... url) 
		{
			notifyGL();
			
			// if(mStatus == STATUS_SUCCESS)
			// {
				// INFO("PSS", "Success: "+ getMessage());	
			// }
			// else
			// {
				// ERR("PSS", "Error: " + getMessage());
			// }
			
			return new Integer(mStatus);
		}
		
		protected void onPostExecute(Integer result) {
			DBG("PSS", "[onPostExecute]");
			Pss.handledURL = null;
			dialog.dismiss();
			
			if(result == STATUS_SUCCESS)
			{
				//setup custom dialog
				Context mContext = mPSSActivity.getApplicationContext();
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.pss_start_download_dialog, 
						(ViewGroup) mPSSActivity.findViewById(R.id.DownloadOptionsLinearLayout));
				
				CheckBox wifiCheckBox = (CheckBox) layout.findViewById(R.id.checkBox1);
				wifiCheckBox.setOnCheckedChangeListener(checkBoxListener);
				
				//show thanks for purchasing
				mResultDialog = new AlertDialog.Builder(mPSSActivity);
				mResultDialog.setView(layout);
				mResultDialog.setTitle(parseString(R.string.pss_vzw_transaction_complete_title))
					.setMessage(parseString(R.string.pss_vzw_transaction_complete))
				   .setCancelable(false)
				   //show download button
				   .setPositiveButton(getString(R.string.pss_vzw_downlaod), new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int id) {
							//this event will be override with View.OnClickListener to keep both dialogs on screen.
					   }
				   });
				   
				//show the dialog
				final AlertDialog alert = mResultDialog.create();
				alert.show();

				//override DialogInterface.OnClickListener
				Button okButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
				okButton.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v) {
						if (Pss.canInstallFromOtherSources(mPSSActivity)){
							alert.dismiss();

							DBG("PSS", "Downloading APK from: ["+apk_url+"]");
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(apk_url));
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mPSSActivity.startActivity(i);
							mPSSActivity.finish();
						} else {
							AlertDialog unknown_dialog = Pss.createUnknownSourcesDialog(mPSSActivity, GNP.getItemName());
							unknown_dialog.show();
						}
					}
				});

			} 
			else 
			{
				// mRetryDialog = new AlertDialog.Builder(mPSSActivity);
				// mRetryDialog.setMessage("An error has occurs \n"+getMessage())
				   // .setCancelable(false)
				   // .setPositiveButton("OK", new DialogInterface.OnClickListener() 
				   // {
					mRetryDialog = new AlertDialog.Builder(mPSSActivity);
					mRetryDialog.setMessage(parseString(R.string.pss_vzw_try_again_glshop))
				   .setCancelable(false)
				   
				   //yes button
				   .setPositiveButton(getString(R.string.pss_vzw_yes), new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface dialog, int id) 
					   {
							new ARequestGL().execute("");
					   }
				   });
				   /*.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface dialog, int id) 
					   {
							dialog.cancel();
					   }
				   });*/
		
				AlertDialog alert = mRetryDialog.create();
				alert.show();
			}
		}
		
		CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				boolean turnWifiOn = isChecked;
				new ARConnect(turnWifiOn).execute();
			}
		};
	} //ARequestGL
/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////

	class ARConnect extends GLAsyncTask{

		boolean enableWifi;
		public ARConnect(boolean turnOnWifi){
			enableWifi = turnOnWifi;
		}
		protected void onPreExecute() 
		{
			dialog = new ProgressDialog(mPSSActivity);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setMessage(mPSSActivity.getString((enableWifi)?R.string.pss_vzw_wait_connection_wifi:R.string.pss_vzw_wait_disable_connection));
			dialog.show();
		}
		
		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			WifiManager wifiManager = (WifiManager) (mPSSActivity.getSystemService(Context.WIFI_SERVICE));
			ConnectivityManager cm = (ConnectivityManager) mPSSActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (enableWifi)
			{
				Log.d("PSS","Enabling wifi");
				wifiManager.setWifiEnabled(true);
				
				//enable WIFI
				long TIME_OUT = 10000;
				while(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED && TIME_OUT >= 0){
					try {	Thread.sleep(500); } catch(Exception e) {}
					TIME_OUT -= 500;
				}
				//Wifi is connected?
				NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				while(!mWifi.isConnected() && TIME_OUT >= 0){
					try {	Thread.sleep(500); } catch(Exception e) {}
					TIME_OUT -= 500;
				}
				
				if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
					return new Integer(STATUS_SUCCESS);
				else
					return new Integer(STATUS_ERROR_CODE);
			}else{
				//disable WI-FI and enable data connection
				Log.d("PSS","Disabling wifi");
				wifiManager.setWifiEnabled(false);
				
				State mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
				long TIME_OUT = 10000;
				while((mobile != State.CONNECTED) && (TIME_OUT>0))
				{
					try {	Thread.sleep(500); } catch(Exception e) {}
					mobile = cm.getNetworkInfo(cm.TYPE_MOBILE).getState();
					TIME_OUT -= 500;
				}
				if(mobile != State.CONNECTED){
					return new Integer(STATUS_ERROR_CODE);
				}else{
					return new Integer(STATUS_SUCCESS);
				}
			}
		}
		
		protected void onPostExecute(Integer result) {
			dialog.dismiss();
		}
		
	}
	
	public VzwBilling(Activity pss)
	{
		mPSSActivity = pss;
	}
	

	private void updateDeviceInfo() {
	//#if VZ_PRODUCTION_SERVER  
		// // Returns the phone number string for line 1, for example, the MSISDN for a GSM phone. Return null if it is unavailable.
		GNP.MDN = Device.getLineNumber();
		if(GNP.MDN.equals("00"))
			GNP.MDN = null;
		DBG( "PSS", "MDN: " + GNP.MDN);

		String DeviceMEID = "";
		// Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones. Return null if device ID is not available.
		DeviceMEID = Device.getDeviceId();
		DBG( "PSS", "Device MEID: " + DeviceMEID);
	//#endif

		// Fix for prototype devices or SIM problems
		if(GNP.MDN == null){
			GNP.MDN = "0000000000";
		}
		if(GNP.DeviceModel == null){
			GNP.DeviceModel = "PROTOTYPE";
		}
		if(GNP.FirmwareVersion == null){
			GNP.FirmwareVersion = "TESTFW";
		}

		// Device Model sin espacios
		DeviceModelWithoutBlanks = "";


		// To cross the Device Model and if there is space in 
		// target not to add it to the new Device Model
		for (int x=0; x < GNP.DeviceModel.length(); x++) {
			if (GNP.DeviceModel.charAt(x) != ' ')
				DeviceModelWithoutBlanks += GNP.DeviceModel.charAt(x);
		}
		//DeviceModelWithoutBlanks = GNP.DeviceModel;

		DBG( "PSS", "Device Model: " + DeviceModelWithoutBlanks);
		DBG( "PSS", "FirmwareVersion: " + GNP.FirmwareVersion);
	}
	
	private void setStateError(int error)
	{
		mStatus = error;
		mState = STATE_IDLE;
	}
	private void setURL(String url)
	{
	
		DBG("PSS", "[setURL]");
		JDUMP("PSS", url);
		//			pss://buy:game="1"
		PSSUrl = url; 
		
		int id = PSSUrl.indexOf("//") +2;
		int idd = PSSUrl.indexOf(":", id);
		
		PSSCommand 	= PSSUrl.substring(id, idd);
		DBG("PSS", "Command: "+PSSCommand);
		id = PSSUrl.indexOf("=", idd) +1;
		
		idd = PSSUrl.indexOf(":", id);
		String sb = PSSUrl.substring(id,idd);
		DBG("PSS", "ITEM: "+sb);
		PSSItem = sb;
		
		id = PSSUrl.indexOf("=", idd) +1;
		sb = PSSUrl.substring(id);
		DBG("PSS", "GAMECODE: "+sb);
		
		PSSGameCode = sb;
	}
	
	public void RequestBuy(String url)
	{
		INFO("PSS", "[RequestBuy]");
		new ARequestBuy().execute(url);
	}
	
	/*
	* Turn on or off wi-f.
	* Parameter.
	* 	turnOnWifi = true. Enable wifi
	* 	turnOnWifi = false. Enable data connection
	*/
	public void ChangeConnection(boolean turnOnWifi) {
		new ARConnect(turnOnWifi).execute();
	}
	
	private String parseString(int value) 
	{
		String res = mPSSActivity.getString(value);
		res = res.replace("{GAME}",GNP.getItemName()).replace("{PRICE}",GNP.getPrice());
		return res;
	}
	private String getString(int value)
	{
		String res = mPSSActivity.getString(value);
		return res;
	}
	
	
	private void confirmCheckout()
	{
		INFO("PSS", "[confirmCheckout]");
		
		//if (mBuyDialog == null)
		//{
		
			//show confirm buy message
			mBuyDialog = new AlertDialog.Builder(mPSSActivity);
			mBuyDialog.setTitle(parseString(R.string.pss_vzw_confirm_buy_title))
				.setMessage(parseString(R.string.pss_vzw_confirm_buy))
				.setCancelable(false)
			   
			   //yes button
			   .setPositiveButton(getString(R.string.pss_vzw_get_game), new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
						new ARequestCheckout().execute("");
				   }
			   })
			   
			   //no button
			   .setNegativeButton(getString(R.string.pss_vzw_cancel), new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
				   }
			   });
		//}
		
		AlertDialog alert = mBuyDialog.create();
		alert.show();
	}
	/*
	private void showResult()
	{
	
		mResultDialog = new AlertDialog.Builder(mPSSActivity);
		mResultDialog.setMessage(" "+getMessage())
		   .setCancelable(false)
		   .setPositiveButton(getString(R.string.pss_vzw_download), new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
			   }
		   });
		   
		AlertDialog alert = mResultDialog.create();
		alert.show();
	}
	*/
	public boolean isInProgress()
	{
		return (mState != STATE_IDLE);
	}
	public boolean isActivatingMobileConnection()
	{
		return mEnablingMobileConnection;
	}
	
	//Method for Request Login
	protected void requestLogin() 
	{
		DBG("PSS", "[requestLogin]");
		mState = STATE_LOGIN;
		
		clear();
		//update device info
		updateDeviceInfo();
		
		//set game info
		GNP.ViewParam = PSSItem;
		
		VB.setsUrl(null);
		VB.connect(
				"%" + GNP.userAgent +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.HSStartUP ).substring(1) +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.MDN.length() ).substring(1) + GNP.MDN +	// REQUIRED - STRING
					"%" + Integer.toHexString( 0x100 | DeviceModelWithoutBlanks.length() ).substring(1) + DeviceModelWithoutBlanks +	// REQUIRED - STRING             /* "07ADR6300" +      */
				"%" + Integer.toHexString( 0x100 | GNP.FirmwareVersion.length() ).substring(1) + GNP.FirmwareVersion +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.HandsetClientVers.length() ).substring(1) + GNP.HandsetClientVers +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.CommProtocolVers.length() ).substring(1) + GNP.CommProtocolVers +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.CompressionLevel.length() ).substring(1) + GNP.CompressionLevel +	// OPTIONAL - STRING
				"%" + Integer.toHexString( 0x100 | GNP.ReferralId.length() ).substring(1) + GNP.ReferralId +	// CONDITIONAL - STRING
				"%" + Integer.toHexString( 0x100 | GNP.Unknown.length() ).substring(1) + GNP.Unknown	// DON'T KNOW (BUT NOT NECESSARY) - STRING
				);
		VB.sendRequest();
		VB.handleProcess();
		
		if(!VB.getsResponse().equals(""))
		{
			parserXML(VB.getsResponse());
			if(codeEvaluation(GNP.Code))
			{
				setStateError(STATUS_ERROR_CODE);
			}
			else
			{
				requestGameData();
			}
		}
		else
		{
			setStateError(STATUS_ERROR_SERVER_RESPONSE_EMPTY);
		}
	}

	private static boolean codeEvaluation(String code) {
		// TODO Auto-generated method stub
    	DBG( "PSS", "CODE: " + code);
    	if(code == null)
    		return false;
    	int error = Integer.parseInt(code);
    	if( (error == -1) ||
    		(error >= 100 && error <= 110) || 
    		(error >= 201 && error <= 302) || 
    		(error >= 304 && error <= 330) || 
    		(error >= 400 && error <= 404) || 
    		(error == 406) ||
    		(error == 500) ||
    		(error == 900) ||
    		(error == 901) || 
    		(error >= 999 && error <= 1007) || 
    		(error == 4031) ||
    		(error >= 20011 && error <= 20062) ||
    		(error >= 20064 && error <= 20171) ||
    		(error >= 30000 && error <= 30003) ||
    		(error == 30025) ||
    		(error >= 30041 && error <= 30046) )
    	{
            return true;
    	}
    	else
        	return false;
	}

    
	
	//Method for Request Game Data
	protected void requestGameData() {
		INFO("PSS", "[requestGameData]");
		
		mState = STATE_REQUEST_DATA;
		VB.setsUrl(null);

		if(firstime)
		{
			Random r = new Random();
			VB.X_MOD_RF = Integer.toString(Math.abs(r.nextInt()), 9);
			VB.X_MOD_SS = VB.md5(VB.X_MOD_SC + VB.X_MOD_RF + GNP.secretString + GNP.HandsetClientVers_MD5 + GNP.CommProtocolVers_MD5);
			firstime = false;
		}
		
		VB.connect(
				"%" + GNP.userAgent +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.GetView ).substring(1) +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.ViewID.length() ).substring(1) + GNP.ViewID +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.ViewParam.length() ).substring(1) + GNP.ViewParam	// REQUIRED - String
			);
		VB.sendRequest();
		VB.handleProcess();
	
		
		if(!VB.getsResponse().equals(""))
		{
			parserXML(VB.getsResponse());
			if(codeEvaluation(GNP.Code))
			{
			
				setStateError(STATUS_ERROR_CODE);
			}
			else
			{
				mStatus = STATUS_SUCCESS;
			}
		}
		else
		{
			setStateError(STATUS_ERROR_SERVER_RESPONSE_EMPTY);
		}
	}

	protected int Checkout()
	{
		INFO("PSS", "[Checkout]");
		
		requestGameCheckout();
		
		return mStatus;
	}
	/*
	protected String getMessage()
	{
		if(mStatus == STATUS_SUCCESS && mState == STATE_PURCHASE)
		{
		
			return parseString(R.string.pss_vzw_transaction_complete);
		}
		if(mStatus != STATUS_SUCCESS)
		{
			return parseString(R.string.pss_vzw_try_again);
		}
		
		return "";
	}*/
	
	protected void notifyGL()
	{
		INFO("PSS", "[notifyGL]");
		mState = STATE_NOTIFYGL;
		Device d = new Device();
		XPlayer xp = new XPlayer(d);
		//xp.sendSavePurchaseRequest(GNP.getPrice(), "200");
		xp.sendSavePurchaseRequest(GNP.getPrice(), GNP.getCode(), PSSGameCode, PSSItem);
		
		//SUCCESS|10003|49221|http://dl.gameloft.com/cm/t1/0qRWqcfLxAC/|32165
		//this will be save true/false + response from server
		String params[] = new String[6];
		while(!xp.handleSavePurchaseRequest(params))
		{
			DBG("PSS", "Connecting to GL server...");
			try{ Thread.sleep(1000); } catch(Exception e) {}
		}
		
		Boolean res = new Boolean(params[0]);
		DBG("PSS", "SUCCESS: ["+params[0]+"]");
		DBG("PSS", "RESPONSE: ["+params[1]+"]");
		DBG("PSS", "RCODE: ["+params[2]+"]");
		DBG("PSS", "UCODE: ["+params[3]+"]");
		DBG("PSS", "URL: ["+params[4]+"]");
		DBG("PSS", "VZW: ["+params[5]+"]");

		if(res)
		{
			mStatus = STATUS_SUCCESS;
			try
			{
			
				//String apk = "http://test.gameloft.net/gmz/ntr/a6.apk";
				String apk = params[4];
				apk_url = apk;
				
				//moved to onPostExecute() to avoid crash in some devies
				//mPSSActivity.finish();
			}
			catch (Exception e) { }
		}
		else
		{
			setStateError(STATUS_ERROR_SERVER_GL);
		}
	}
	
    //Method for Request Game Checkout
	protected void requestGameCheckout()
	{
		INFO("PSS", "[requestGameCheckout]");
		mState = STATE_CHECKOUT;
		VB.setsUrl(null);
		
		DBG( "PSS", "GNP.CartLineItem_Item_ItemID = " + GNP.CartLineItem_Item_ItemID);
		DBG( "PSS", "GNP.CartLineItem_Item_ItemName = " + GNP.CartLineItem_Item_ItemName);
		DBG( "PSS", "GNP.CartLineItem_Item_PricePlanPackage_PPPID = " + GNP.CartLineItem_Item_PricePlanPackage_PPPID);
		DBG( "PSS", "GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice = " + GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice);

		VB.connect(
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
		VB.sendRequest();
		VB.handleProcess();
		
		
		if(!VB.getsResponse().equals(""))
		{
			parserXML(VB.getsResponse());
			if(codeEvaluation(GNP.Code))
			{
				setStateError(STATUS_ERROR_CODE);
			}
			else
			{
				requestGamePurchase();
			}
		}
		else
		{
			setStateError(STATUS_ERROR_SERVER_RESPONSE_EMPTY);
		}
	}

    //Method for Request Game Purchase
	protected void requestGamePurchase() 
	{
		INFO("PSS", "[requestGamePurchase]");
		
		mState = STATE_PURCHASE;
		VB.setsUrl(null);

		DBG( "PSS", "GNP.ConfirmationID = " + GNP.ConfirmationID);
		
		// Using all the parameters that have 'B' or 'C' in the "USE" column:
		VB.connect(
				"%" + GNP.userAgent +	// REQUIRED - STRING
				"%" + Integer.toHexString( 0x100 | GNP.SubmitPurchase ).substring(1) +		// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.CallPage.length() ).substring(1) + GNP.CallPage +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.ConfirmationID.length() ).substring(1) + GNP.ConfirmationID +	// REQUIRED - String
				"%" + Integer.toHexString( 0x100 | GNP.Type.length() ).substring(1) + GNP.Type	// OPTIONAL - String
				);
		VB.sendRequest();
		VB.handleProcess();
				
		
		if(!VB.getsResponse().equals(""))
		{
			parserXML(VB.getsResponse());
			if(codeEvaluation(GNP.Code))
			{
				//GNP.Desc;
				setStateError(STATUS_ERROR_CODE);
			}
			else
			{
				mStatus = STATUS_SUCCESS;
				
				//GNP.EndUserMsg
			}
		}
		else
		{
			setStateError(STATUS_ERROR_SERVER_RESPONSE_EMPTY);
		}
	}
	
	private void clear()
	{
		VB.X_MOD_RF = "";
		VB.X_MOD_SC = "";
		VB.X_MOD_SS = "";
		VB.X_MOD_RF = null;
		VB.X_MOD_SC = null;
		VB.X_MOD_SS = null;
	}
	
	void PrepareLaunchGame()
	{
		if(mWifiEnabled)
		{
			WifiManager wifiManager = (WifiManager) (mPSSActivity.getSystemService(Context.WIFI_SERVICE));
			wifiManager.setWifiEnabled(true);
		}
	}
	
	static boolean FinalResponseEnded;
	private static String parserXML(String ResponseXML)
	{
		FinalResponseEnded = false;
		XmlPullParserFactory factory = null;
		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			DBG_EXCEPTION(e);
		}
		
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
		
        try {
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			DBG_EXCEPTION(e);
		}

        try {
	        	xpp.setInput( new StringReader(ResponseXML) );
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			DBG_EXCEPTION(e);
		}
		
        int eventType = 0;
		
        try {
			eventType = xpp.getEventType();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			DBG_EXCEPTION(e);
		}
		
        String FinalResponse = "\n";
        boolean FoundCode = false;
        boolean FoundDesc = false;
        boolean FoundItem = false;
        boolean FoundItemName = false;
        boolean FoundPPPID = false;
        boolean FoundPrice = false;
        boolean FoundConfirmationID = false;
        boolean FoundEndUserMsg = false;
        
		while (eventType != XmlPullParser.END_DOCUMENT) {
        	if(eventType == XmlPullParser.START_DOCUMENT) {
        		//DBG( "PSS", "START DOCUMENT");
        	} else if(eventType == XmlPullParser.END_DOCUMENT) {
        		//DBG( "PSS", "END DOCUMENT");
        	} else if(eventType == XmlPullParser.START_TAG) {
        		//DBG( "PSS", "START TAG "+xpp.getName());
        			FinalResponse = FinalResponse + xpp.getName() + ": ";
        			if(xpp.getName().equals("code"))
        				FoundCode = true;
        			else
        				FoundCode = false;
        			if(xpp.getName().equals("desc"))
        				FoundDesc = true;
        			else
        				FoundDesc = false;
        			if(xpp.getName().equals("itemID"))
        				FoundItem = true;
        			else
        				FoundItem = false;
        			if(xpp.getName().equals("ItemName") || xpp.getName().equals("itemName"))
        				FoundItemName = true;
        			else
        				FoundItemName = false;
        			if(xpp.getName().equals("PPPID"))
        				FoundPPPID = true;
        			else
        				FoundPPPID = false;
        			if(xpp.getName().equals("purchasePrice"))
        				FoundPrice = true;
        			else
        				FoundPrice = false;
        			if(xpp.getName().equals("confirmationID"))
        				FoundConfirmationID = true;
        			else
        				FoundConfirmationID = false;
        			if(xpp.getName().equals("endUserMsg"))
        				FoundEndUserMsg = true;
        			else
        				FoundEndUserMsg = false;
        	} else if(eventType == XmlPullParser.END_TAG) {
        		//DBG( "PSS", "END TAG "+xpp.getName());
        	} else if(eventType == XmlPullParser.TEXT) {
        		//DBG( "PSS", "Text "+xpp.getText());
        		FinalResponse = FinalResponse + xpp.getText() + "\n";
        		if(FoundCode)
        		{
        			GNP.Code = xpp.getText();
        		}
        		if(FoundDesc)
        		{
        			GNP.Desc = xpp.getText();
        		}
        		if(FoundItem)
        		{
        			GNP.CartLineItem_Item_ItemID = xpp.getText();
        		}
        		if(FoundItemName)
        		{
        			GNP.CartLineItem_Item_ItemName = xpp.getText();
        		}
        		if(FoundPPPID)
        		{
        			GNP.CartLineItem_Item_PricePlanPackage_PPPID = xpp.getText();
        		}
        		if(FoundPrice)
        		{
        			GNP.CartLineItem_Item_PricePlanPackage_PurchasePrice = xpp.getText();//Double.parseDouble(xpp.getText());
        		}
        		if(FoundConfirmationID)
        			GNP.ConfirmationID = xpp.getText();
        		if(FoundEndUserMsg)
        			GNP.EndUserMsg = xpp.getText();
        	}
        	try {
        		eventType = xpp.next();
        	} catch (XmlPullParserException e) {
        		// TODO Auto-generated catch block
				DBG_EXCEPTION(e);
        		//DBG_EXCEPTION(e);
        		eventType = XmlPullParser.END_DOCUMENT;
        	} catch (IOException e) {
        		// TODO Auto-generated catch block
        		//DBG_EXCEPTION(e);
				DBG_EXCEPTION(e);
        	}
        }
		
		return FinalResponse;
	}
}
#endif