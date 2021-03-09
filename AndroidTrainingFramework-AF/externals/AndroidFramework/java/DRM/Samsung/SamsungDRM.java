/*
 * Filename SamsungDRM.java
 * Author Daeyoung Hwang
 * Organization Gameloft SEO
 * Date 2011.07.24
 * Brief shows how to implement Samsung DRM (Zirconia) for Android.
 */

#if USE_SAMSUNG_DRM
package APP_PACKAGE;

import APP_PACKAGE.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import com.samsung.zirconia.*;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/* Listener class to receive the results of license verification and inquiry */
class MyLicenseCheckListener implements LicenseCheckListener {

	public MyLicenseCheckListener(SamsungDRM instance, Zirconia zirconia)
	{
		this.m_instance = instance;
		this.m_zirconia = zirconia;
	}
	
	/* In case the results of license verification and inquiry are normal */
	@Override
	public void licenseCheckedAsValid() {
		DBG("SamsungDRM", "[MyLicenseCheckListener] License is valid");
		
		m_ownerHandler.post(new Runnable() {
			public void run() {
				DBG("SamsungDRM", "[MyLicenseCheckListener] License is valid");
				m_instance.SetLicense(true);
				
				String resMsg = "";
				resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_SUCCESS_1);
			
				m_instance.ShowDRMDialog(resMsg);
			}
		});
	}

	/* In case the results of license verification and inquiry are abnormal */
	@Override
	public void licenseCheckedAsInvalid() {
		DBG("SamsungDRM", "[MyLicenseCheckListener] License is invalid");

		m_ownerHandler.post(new Runnable() {
			public void run() {
			
				m_instance.SetLicense(false);
				
				if (m_instance.IsCheckLocal())
				{
					m_instance.ShowWarning();
					return;
				} else 
				{
					int error = m_zirconia.getError();
					
					String resMsg = "";
					
					if(error == m_zirconia.EZIRCONIA_NOT_PURCHASED)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_NOT_PURCHASED_1);
					}
					else if(error == m_zirconia.EZIRCONIA_INVALID_VALUE)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_INVALID_VALUE_1);
					}
					else if(error == m_zirconia.EZIRCONIA_CANNOT_CHECK)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_CANNOT_CHECK_1);
					}
					else if(error == m_zirconia.EZIRCONIA_RECEIVE_FAILED)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_RECEIVE_FAILED_1);
					}
					else if(error == m_zirconia.EZIRCONIA_SEND_FAILED)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_SEND_FAILED_1);
					}
					else if(error == m_zirconia.EZIRCONIA_KEY_CREATION_FAILED)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_KEY_CREATION_FAILED_1);
					}
					else if(error == m_zirconia.EZIRCONIA_CLIENT_MISMATCH)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_CLIENT_MISMATCH_1);
					}
					else if(error == m_zirconia.EZIRCONIA_VERSION_MISMATCH)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_VERSION_MISMATCH_1);
					}
					else if(error == m_zirconia.EZIRCONIA_LICENSE_MISMATCH)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_LICENSE_MISMATCH_1);
					}
					else if(error == m_zirconia.EZIRCONIA_SERVER_MISMATCH)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_SERVER_MISMATCH_1);
					}
					else if(error == m_zirconia.EZIRCONIA_APPLICATION_MODIFIED)
					{
						resMsg = m_instance.getString(R.string.DRM_EZIRCONIA_APPLICATION_MODIFIED_1);
					}
					m_instance.ShowDRMDialog(resMsg);
				
					DBG("SamsungDRM", "[MyLicenseCheckListener] License is invalid, error code = " + error + ", " + resMsg + " " + m_zirconia.EZIRCONIA_NOT_PURCHASED);
				}
			}
		});
	}
	
	SamsungDRM m_instance;
	Zirconia m_zirconia;
	Handler m_ownerHandler;
}

public class SamsungDRM extends Activity {

	private boolean 	m_isCheckLocal = false;
	private boolean 	m_isLicenseValid = false;
	private boolean   	m_flag = false;
	
	private String 		resMsg;
	private Handler 	m_handler = new Handler();

	private Zirconia 	m_zirconia = null;
	private MyLicenseCheckListener m_listener = null;

	//private ProgressDialog progressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isTaskRoot()) // [A] Prevent App restart when open or 
		{
			DBG("SamsungDRM", "!isTaskRoot");
			finish();
			return;
		}
		DBG("SamsungDRM","onCreate");
		
		InitZirconia();
		CheckLicense(true);
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
	
	@Override
	protected void onResume()
	{
		super.onResume();
		DBG("SamsungDRM","on Resume");
		
		if (m_flag)
		{
			if(isConnection() == false)
			{
				showWiFi();
			}  else 
			{
				CheckLicense(false);
			}
		}
	}
	
	public boolean IsCheckLocal()
	{
		return m_isCheckLocal;
	}
	
	public void SetLicense(boolean value)
	{
		this.m_isLicenseValid = value;
	}
    
	public boolean isConnection()
	{
		DBG("SamsungDRM","isConnection");
		// check internet connection prior to use Zirconia
		ConnectivityManager cm 	= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo 	= cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo 	= cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isWifiAvail 	= wifiInfo.isAvailable();
		boolean isWifiConn 		= wifiInfo.isConnected();
		boolean isMobileAvail 	= mobileInfo == null ? false : mobileInfo.isAvailable();
		boolean isMobileConn 	= mobileInfo == null ? false : mobileInfo.isConnected();

		String strNetworkInfo = "isWifiAvail = " + isWifiAvail + "\nisWifiConn = " + isWifiConn
							+ "\nisMobileAvail = " + isMobileAvail + "\nisMobileConn = " + isMobileConn;
		DBG("SamsungDRM",  "" +strNetworkInfo );


        // if internet connection fails, pop up alert dialog
		if ( isWifiConn == false && isMobileConn == false )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public void ShowWarning()
	{
		AlertDialog.Builder alertGetLicense = new AlertDialog.Builder(SamsungDRM.this);
		alertGetLicense.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick( DialogInterface dialog, int which) {
				dialog.dismiss();
				if(isConnection() == false)
				{
					m_flag = true;
					showWiFi();
				}  else 
				{
					CheckLicense(false);
				}
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}).setOnKeyListener(new DialogInterface.OnKeyListener(){
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyevent) {
					if (keyevent.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyevent.getKeyCode()) {
						case KeyEvent.KEYCODE_BACK:
							return true;
						case KeyEvent.KEYCODE_SEARCH:
							return true;
						default:
							break;
						}
					}
					return false;
				}

		});
		
		String strText = getString(R.string.DRM_ZIRCONIA_LOCAL_LICENSE_INVALID);
		
		alertGetLicense.setMessage( strText);
		alertGetLicense.show();
	}
	
	public void showWiFi()
	{
		DBG("SamsungDRM",  "ask user active wifi" );
		AlertDialog.Builder alert = new AlertDialog.Builder(SamsungDRM.this);
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
		    @Override
			public void onClick( DialogInterface dialog, int which ) {

			dialog.dismiss();

			// open Wi-Fi settings page
			Intent intent = new Intent( Intent.ACTION_MAIN, null );
			intent.addCategory( Intent.CATEGORY_LAUNCHER );
			ComponentName cn = new ComponentName( "com.android.settings", "com.android.settings.wifi.WifiSettings" );
			intent.setComponent( cn );
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			startActivity( intent );

			// TODO
			// when setting wifi connection is done, how to BeginZirconia() ?
			}
		}).setNegativeButton( "No", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// terminate application
				dialog.dismiss();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}).setOnKeyListener(new DialogInterface.OnKeyListener(){
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyevent) {
					if (keyevent.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyevent.getKeyCode()) {
						case KeyEvent.KEYCODE_BACK:
							return true;
						case KeyEvent.KEYCODE_SEARCH:
							return true;
						default:
							break;
						}
					}
					return false;
				}

		});

		String strText = getString(R.string.DRM_NETWORK_DIALOGUE_1);

		alert.setMessage( strText );
		alert.show();
	}
	
	ProgressDialog progressDialog;
	public void CheckLicense(boolean local)
	{
		m_isCheckLocal = local;
		m_zirconia.checkLicense(local, false);

		 if (!local)
		 {
			
			 progressDialog = ProgressDialog.show(this,"Check license.",
                     "Please wait...",  true);
		 }
	}
	
	public void InitZirconia()
	{
		m_zirconia = new Zirconia(this);

  		m_listener = new MyLicenseCheckListener(this, m_zirconia);
  		m_listener.m_ownerHandler = m_handler;
  		// m_listener.ownerTextView = m_textView;
		
		m_zirconia.setLicenseCheckListener(m_listener);
		m_zirconia.doVariablesTest();
	}
	
	public void StartGame()
	{
		boolean isShowProgressDialog = true;
		if(progressDialog!=null)
		{
			progressDialog.dismiss();	
			isShowProgressDialog = false;
		}
		
		Intent myIntent = new Intent(SamsungDRM.this, GAME_ACTIVITY_NAME.class);
		startActivity(myIntent);
		finish();
		
		// Only kill process after "check license"
		if(!isShowProgressDialog)
			android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	public void ShowDRMDialog(String message)
	{
		if (m_isLicenseValid)
			StartGame();
		else
		{
			resMsg = message;
			showDialog(0);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		DBG("SamsungDRM","OnCreateDialog");
		return new AlertDialog.Builder(this)
				.setIcon(null)
				.setTitle("")
				.setMessage(resMsg)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				})
				.setOnKeyListener(new DialogInterface.OnKeyListener(){
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent keyevent) {
					if (keyevent.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyevent.getKeyCode()) {
						case KeyEvent.KEYCODE_BACK:
							return true;
						case KeyEvent.KEYCODE_SEARCH:
							return true;
						default:
							break;
						}
					}
					return false;
				}

			}).create();
	}
}

#endif