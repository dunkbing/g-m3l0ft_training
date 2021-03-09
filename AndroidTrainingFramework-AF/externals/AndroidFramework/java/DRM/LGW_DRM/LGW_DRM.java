#if USE_LGW_DRM
package APP_PACKAGE;

import APP_PACKAGE.R;
// import com.lge.coconut.sampleapp.SampleAESObfuscator;

#if FOR_LGW_GLOBAL
import com.lge.lgworld.coconut.client.LGLicenseChecker;
import com.lge.lgworld.coconut.client.LGLicenseCheckerCallback;
import com.lge.lgworld.coconut.client.LGLicenseObfuscator;
#else
import com.lg.apps.cubeapp.coconut.client.LGLicenseChecker;
import com.lg.apps.cubeapp.coconut.client.LGLicenseCheckerCallback;
import com.lg.apps.cubeapp.coconut.client.LGLicenseObfuscator;
import com.lg.apps.cubeapp.coconut.client.util.CoconutLog;
#endif


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
// #if USE_INSTALLER
// import APP_PACKAGE.installer.GameInstaller;
// #endif

public class LGW_DRM extends Activity {
	/* Logging tag */
	SET_TAG("LGCoconutSampleApp");

	/* Application status text view*/
	private TextView mStatusText;

	/** Dialog & its IDs **/
	private Dialog mDialog = null;
	/** salt for obfuscate **/
	private static final byte[] SALT = new byte[] {78, -23, 123, -45, -3, -7, 8, 32, 89, 92, -99, -62, 44, -16, 74, -121, -39, -48, 59, 10};
	/** Public Key **/
#if FOR_LGW_GLOBAL
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq0hQVO2uvHNOivMegWwlwZNLDi4tQmMuED07am5t6zGgAWQqoJb1nuDa11mVGEPbmCrjZFzwhh004OtwM7v96TDPRUZvDt/xE6WHtYbMQGfjrttMfUrxagRdirUhgO0NtW5LTTGApg+7bL9z1kf20vjkeudE6ok0JxhU6TtQrJ4+AmtAAghMSzexnQC1trhKJJo9yQBScCZw/v65Zu0D2cMEL1T+6x/FKQLNarCq+TlGPO0KxQjua+LmQWcnC8CqpiiEvHvxaZInMXJXM2sYYYpzdp8363ARi/If183TzeQrE5p8zEzGyr68+jhCaVSV94ZU1Z86pINzaZb8pROChQIDAQAB";
#else
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxqeAsD6hLE6H5alZmyaExw79oOVeYgkyxKsmpyiHpzjNjU4PwLRSj3LqI4mBFrvHjYNH8ry7n1m5a56TWjPGpdzF6Pkb8lakh5e/42YE8qRMhe2NOA9JuPCtoJlg4VBxRwVfEAcjZYSzcETt3YdFRouSs/+NbgRZomgPrKtFhuxe2PpB6Qht8H28r4jWaK2W2CfDCLs0/2Qf6XBJber/efvxbxvqknKQV4IQBVSvuMR/y+GmmxAM1UR5TQdx7unrYcO+nj4yjxoFhOGbvmB6J0wuKW11fs3+iZGELpE8FbLgZxUiS9lKo/5THj519fwCDog8nrAN0oE/evYAGuIrpwIDAQAB";
#endif
	/** URL to get LG World application **/
#if FOR_LGW_GLOBAL
//link for LG World Global
	private static final String DOWNLOADURL_OF_LGWORLD = "http://www.lgworld.com";
#else
//link to LG World Korean
	// private static final String DOWNLOADURL_OF_LGWORLD = "http://www.lgmobile.co.kr/cyon/mobile/contents/downloadLGApps.dev?apptype=CONTENT";
	private static final String DOWNLOADURL_OF_LGWORLD = "http://www.lgmobile.co.kr/jsp/cyon/billing/appDownload.jsp";
#endif

	/** LG Coconut Client interface **/
	private LGLicenseCheckerCallback mLicenseCheckerCallback;
	private LGLicenseChecker mChecker;

	/** Dialog Type */
	private static final int DIALOG_PROGRESSING = 0;
	private static final int DIALOG_NOLICENSED  = 1;
	private static final int DIALOG_NOLGWORLD   = 2;
	private static final int DIALOG_NONETWORK   = 3;
	private static final int DIALOG_WARNING     = 4;

	/** Request code for sign-in activity */
	private static final int mRequestCode = 230866;

	/** A handler on the UI thread. */
	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isTaskRoot()) // [A] Prevent App restart when open or 
		{
			DBG("LGW DRM", "!isTaskRoot");
			finish();
			return;
		}
		setContentView(R.layout.lgw_main);
#if !RELEASE_VERSION && !FOR_LGW_GLOBAL
	CoconutLog.enable(true);
#endif
		DBG("LGCoconutApp", "onCreate()");

		mStatusText = (TextView) findViewById(R.id.status_text);
		//mStatusText.setText("Checking License...");

		mHandler = new Handler();

		// Library calls this when it's done.
		mLicenseCheckerCallback = new MyLicenseCheckerCallback();

		// Construct the LicenseChecker with a policy using default obfuscator, LGLicenseObfuscator class
		mChecker = new LGLicenseChecker(this, BASE64_PUBLIC_KEY, 
				new LGLicenseObfuscator(SALT, getPackageName(), Secure.getString(getContentResolver(), Secure.ANDROID_ID)));

		// Construct the LicenseChecker with a policy using custom obfuscator.
		// You have to implement encryption class which inherit com.lge.lgworld.coconut.client.Obfuscator interface. 
		// mChecker = new LGLicenseChecker(this, BASE64_PUBLIC_KEY, 
		// new SampleAESObfuscator(SALT, getPackageName(), Secure.getString(getContentResolver(), Secure.ANDROID_ID)));

		// Call license check
		doCheck();
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

	/**
	 * Check license
	 */
	private void doCheck() {
		// Show progressing dialog
		showDialog(DIALOG_PROGRESSING);
		// Acquire license through LG coconut library
		mChecker.checkAccess(mLicenseCheckerCallback);
	}

	/**
	 * Display dialogs
	 */
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			// Progressing pop-up for checking license
		case DIALOG_PROGRESSING:
			DBG(TAG, "DIALOG_PROGRESSING");
			ProgressDialog pDialog = new ProgressDialog(this);
			pDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getApplicationContext().getText(R.string.cancel_button),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface mDialog, int whichButton) {
					DBG(TAG, "DIALOG_PROCESSING CANCEL clicked");
					finish();
				}
			}
			);
			pDialog.setMessage(getApplicationContext().getText(R.string.checklicense_dialog_message));
			mDialog = pDialog;
			break;

			// NO LICENSED
		case DIALOG_NOLICENSED:
#if FOR_LGW_GLOBAL
			removeDialog(DIALOG_PROGRESSING);
			DBG(TAG, "DIALOG_NOLICENSED");
			mDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.unlicensed_dialog_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.unlicensed_dialog_message)
			.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_NOLICENSED);
					Intent intent = new Intent("com.lge.lgworld.coconut.service.LGLicensingSigning");
					startActivityForResult(intent, mRequestCode);
				}
			}).setNegativeButton(R.string.exit_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.create();
#else

			DBG(TAG, "DIALOG_NOLICENSED");
			removeDialog(DIALOG_PROGRESSING);
			removeDialog(DIALOG_NOLICENSED);
			Intent intent = new Intent("com.lg.apps.cubeapp.coconut.service.LGLicensingSigning");
			startActivityForResult(intent, mRequestCode);
			
			DBG(TAG, "DIALOG_NOLICENSED");
#endif
			break;

			// ERROR_NON_LGWORLD_APP
		case DIALOG_NOLGWORLD:
			removeDialog(DIALOG_PROGRESSING);
			DBG(TAG, "DIALOG_NOLGWORLD");
			mDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.no_lgworld_dialog_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.no_lgworld_dialog_message)
			.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					/* Create intent to launch browser */
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOADURL_OF_LGWORLD));	
					/* Set basic information */
					intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					/* Launch browser to install LG World application */
					getApplicationContext().startActivity(intent);
					finish();
				}
			})
			.setNegativeButton(R.string.exit_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.create();
			break;

			// No Network Error
		case DIALOG_NONETWORK:
			removeDialog(DIALOG_PROGRESSING);
			DBG(TAG, "DIALOG_NONETWORK");
			mDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.no_network_dialog_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.no_network_dialog_message)
			.setNegativeButton(R.string.ok_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.create();
			break;

			// WARNING MESSAGE
		case DIALOG_WARNING:
		default:
			removeDialog(DIALOG_PROGRESSING);
			mDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.warning_dialog_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.warning_dialog_message)
			.setNegativeButton(R.string.ok_button, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.create();
			break;
		}

		/* Block BACK key */
		mDialog.setCancelable(false);

		/* Block Search key */
		mDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
		{
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
			{
				if(keyCode == KeyEvent.KEYCODE_SEARCH)
					return true;
				else
					return false;
			}
		});

		return mDialog;
	}

	/**
	 * Receive the signing result from LG World's signing activity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		super.onActivityResult(requestCode, resultCode, intent);

		switch(requestCode){
		case mRequestCode:
			if(resultCode == RESULT_OK) { 
				// Sing-in Success
				DBG(TAG, "sign-in success");
				doCheck();
			} else {
				// User Canceled
				DBG(TAG, "User canceled");
				finish();
			}
			break;
		default:
			finish();
		}
	}

	/**
	 * Send result to main activity of application
	 */
	private void startApplication() {
		mHandler.post(new Runnable() {
			public void run() {
				//mStatusText.setText("Application is launched");
				//TMT add here
				// #if !USE_INSTALLER
						Intent myIntent = new Intent(LGW_DRM.this, CLASS_NAME.class);
				// #else
						// Intent myIntent = new Intent(LGW_DRM.this, GameInstaller.class);
				// #endif
						startActivity(myIntent);
						finish();				
			}
		});
	}

	/**
	 * Callback interface
	 */
	private class MyLicenseCheckerCallback implements LGLicenseCheckerCallback {
		public void allow() {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			removeDialog(DIALOG_PROGRESSING);
			startApplication();
		}

		public void dontAllow() {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
		#if FOR_LGW_GLOBAL
			showDialog(DIALOG_NOLICENSED);
		#else
			removeDialog(DIALOG_PROGRESSING);
			Intent intent = new Intent("com.lg.apps.cubeapp.coconut.service.LGLicensingSigning");
			startActivityForResult(intent, mRequestCode);
		#endif
		}

		public void applicationError(LicenseCheckErrorCode errorCode) {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}

			ERR(TAG, String.format("applicationError: %1$s", errorCode));

			switch(errorCode) {
				// Typically caused by a development error
			case ERROR_SERVER_FAILURE:
			case ERROR_NOT_MANAGED_PACKAGE:
			case ERROR_INVALID_PACKAGE_NAME:
			case CHECK_IN_PROGRESS:
			case INVALID_PUBLIC_KEY:
				showDialog(DIALOG_WARNING);
				break;

				// network is unavailable
			case ERROR_CONTACTING_SERVER:
				showDialog(DIALOG_NONETWORK);
				break;
				// need sign-in to LG World
			case ERROR_NON_LOGIN_INFORMATION:
#if FOR_LGW_GLOBAL
				removeDialog(DIALOG_PROGRESSING);
				Intent intent = new Intent("com.lge.lgworld.coconut.service.LGLicensingSigning");
				startActivityForResult(intent, mRequestCode);
#else
				removeDialog(DIALOG_PROGRESSING);
				mDialog = new AlertDialog.Builder(LGW_DRM.this)
				.setTitle(R.string.non_login_information_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.non_login_information_message)
				.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_NOLICENSED);
						Intent intent = new Intent("com.lg.apps.cubeapp.coconut.service.LGLicensingSigning");
						startActivityForResult(intent, mRequestCode);
					}
				}).setNegativeButton(R.string.exit_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).show();
#endif
				break;
				// LG World is not installed
			case ERROR_NON_LGWORLD_APP:
				showDialog(DIALOG_NOLGWORLD);
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mChecker.onDestroy();
	}
}
#endif
