#if USE_OPTUS_DRM
package com.msap.store.drm.android;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.KeyEvent;

import com.msap.store.drm.android.data.*;
import com.msap.store.drm.android.util.*;

/**
 * The base class for activity that handles license verification.
 * 
 * This class contains stubs where you may override to adapt the library, as
 * well as methods that can be called by the subclass. Implementation of
 * functionality is contained within the nested Implementation class.
 * 
 * @see CheckerImplementation
 * @author Edison Chan
 */
@SuppressWarnings(value={"unused"})
abstract public class CheckerActivity extends Activity {
	/**
	 * Maximum value of reserved request code. This class reserves a range 
	 * of request codes for internal uses. The range starts from 1 to the 
	 * value of this constant, inclusive. Therefore,  when calling 
	 * {@link android.app.Activity#startActivityForResult startActivityForResult}, 
	 * please use a request code that falls outside this range.
	 */
	public final static int REQUEST_MAX = 1;
	
	/**
	 * Error code for unknown error(s). This error code will be recevied when an
	 * unknown or unusual error happens during an operation.
	 */
	public final static int ERROR_UNKNOWN = 99;

	/**
	 * Error code for failure in sanity checks. The library will perform
	 * sanity checks before any operations to make sure that the library is in
	 * working order. Failure in sanity tests may be caused by unsupported 
	 * device, corrupted installation or some other critical errors.
	 */
	public final static int ERROR_SANITY_CHECK = 11;

	/**
	 * Reason code for failure due to user cancellation. This error code will be
	 * received when an operation cannot be completed because the user press the
	 * cancel button.
	 */
	public final static int ERROR_CANCELLED = 21;
	
	/**
	 * Reason code for failure due to local storage I/O error on the device.
	 * This error code indicates error when reading data from the device, or
	 * writing data to the device. Note that errors in sending/receiving data 
	 * from network is assigned some other error codes.
	 */
	public final static int ERROR_STORAGE = 22;

	/**
	 * Reason code for failure due to generic network error on the device.
	 * This error code indicates that when the library cannot make connection
	 * to license server. Note that if the network connection issue is due
	 * to WiFi hotspot, the ERROR_HOTSPOT is raised instead.
	 */
	public final static int ERROR_NETWORK = 23;

	/**
	 * Reason code for WiFi hotspot detected during connection to license 
	 * server. The library can detect if a user is redirected to another page
	 * for hotspot login. If this is the 
	 */
	public final static int ERROR_HOTSPOT = 24;

	/**
	 * This variable holds the reference to the checker implementation. 
	 * Subclasses can derive that class to implement new functionalities.
	 */
	private CheckerImplementation impl;

	/**
	 * Construct a new CheckerActivity object.
	 * @param impl implementation of the activity.
	 * @throws NullPointerException when impl is null.
	 */
	protected CheckerActivity(CheckerImplementation impl) {
		if (impl != null) {
			this.impl = impl;
		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * Get the unique identifier of this app. The app id is used by the license
	 * server to identify an individual app. Any apps that shares the same app 
	 * id is deemed to be the same app - just different version and build.
	 * Subclasses should override this method to give the correct app id.
	 * @return application identifier.
	 */
	abstract protected String getApplicationId();

	/**
	 * Get the unique identifier of the user who owns this app. This userid is 
	 * used by the license server to identify the caller. Subclasses should 
	 * override this method to give the correct user identifier.
	 * @return user identifier.
	 */
	abstract protected String getUserId();

	/**
	 * Get the serial number for the package. The serial number is used by the
	 * licensing server to identify a package file. Subclasses should override
	 * this method to give the correct serial number.
	 * @return serial number.
	 */
	abstract protected String getSerialNumber();

	/**
	 * Get the URL of license server API. Subclasses should override this method 
	 * to select the correct license server for this app.
	 * @return URL of license server.
	 */
	abstract protected String getLicenseServerCallUrl();

	/**
	 * Get the URL of license server product registration portal. Subclasses should 
	 * override this method to select the correct license server for this app.
	 * @return URL of license server.
	 */
	abstract protected String getLicenseServerPortalUrl();

	/**
	 * Get the identifier of the certificate returned from {@link #getCertificateData}.
	 * @return identifier of the certificate.
	 */
	abstract protected String getCertificateId();

	/**
	 * Get the certificate used for communication between the app and the license
	 * server. Subclasses should override this method to provide a certificate
	 * in DER/PEM format as a byte array.
	 * @return hex encoded certificate.
	 */
	abstract protected byte[] getCertificateData();

	/**
	 * Get the salt used for obfuscating data stored in the device. Subclasses
	 * should override this method to provide a random byte array for salting.
	 * The salt should be at least 8 bytes long (ie 64bit).
	 * @return salt used for obfuscation.
	 */
	abstract protected byte[] getObfuscationSalt();

	/**
	 * Get the parser of internal data and license server response. Subclasses
	 * may override this method to enable handling of custom license 
	 * restrictions.
	 * @return data serializer to be used by the checker.
	 */
	protected Serializer getSerializer() {
		return new Serializer();
	}

	/**
	 * Get the name of the data file that saves local data. Subclasses 
	 * may override this method to save local data to another location.
	 * @return filename of the data file.
	 */
	protected String getLicenseCacheFilename() {
		return "lcache.dat";
	}

	/**
	 * Number of rounds executed when deriving obfuscation key from AppID, 
	 * IMEI and salt. The number of rounds can affect the safety of obfuscated
	 * data. The recommended minimum is 1000, while the default value 
	 * is 1000.
	 * @return number of rounds for key derivation.
	 */
	protected int getObfuscationRound() {
		return 1000;
	}

	/**
	 * Initialize a new checker activity or reload a previously destroyed
	 * activity. Any subclasses that overrides this method should call the
	 * superclass implementation, otherwise an exception will be raised.
	 * @param savedInstanceState data for recreation of previous destroyed activity.
	 * @see android.app.Activity#onCreate
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.impl.setApplicationId(this.getApplicationId());
		this.impl.setUserId(this.getUserId());
		this.impl.setSerialNumber(this.getSerialNumber());
		this.impl.setLicenseServerCallUrl(this.getLicenseServerCallUrl());
		this.impl.setLicenseServerPortalUrl(this.getLicenseServerPortalUrl());
		this.impl.setLicenseCacheFilename(this.getLicenseCacheFilename());
		this.impl.setCertificate(this.getCertificateId(), this.getCertificateData());
		this.impl.setObfuscationSettings(this.getObfuscationSalt(), this.getObfuscationRound());
		this.impl.setSerializer(this.getSerializer());
		this.impl.setIdentifiers(this);
		this.impl.loadSavedState(savedInstanceState);
	}
	
	/**
	 * Handle results from activity spawned from this activity. Subclasses
	 * overriding this method should call the superclass implementation to
	 * make sure that internal activity results are processed.
	 * @param requestCode The integer request code originally supplied.
	 * @param resultCode The integer result code returned by the child activity.
	 * @param data An Intent, which can return result data to the caller.
	 */
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (this.impl.isInternalActivityResult(requestCode, resultCode, data)) {
			this.impl.processInternalActivityResult(requestCode, resultCode, data, this);
		}
	}

	/**
	 * Save instance data to the given bundle, so that state information can be 
	 * restored later when the activity is recreated.
	 * @param bundle bundle where instance state is saved.
	 */
	protected void onSaveInstanceState(Bundle outState) {
		this.impl.saveCurrentState(outState);
	}

	/**
	 * Finish this activity and return to the previous activity. Note that this
	 * method is overridden to set the result before actually finishing the
	 * activity.
	 */
	public void finish() {
		this.setResult(RESULT_OK, this.impl.getResultData());
		super.finish();
	}

	/**
	 * Called when the server requires some confirmation from user in the form
	 * of multiple choices. Subclasses may override this method to show custom
	 * prompt dialog.
	 * @param title title of the prompt.
	 * @param summary a brief description of the prompt.
	 * @param description the full description of the prompt.
	 * @param labels label for each choice.
	 * @param actions action for each choice.
	 */
	public void onPrompt(String title, String summary, String description, String[] labels, Action[] actions) {
		CheckerActivityPromptHandler handler = new CheckerActivityPromptHandler(actions);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setItems(labels, handler);
		builder.setOnCancelListener(handler);
		
		AlertDialog dialog = builder.create();
		dialog.setOwnerActivity(this);
		dialog.show();
	}

	/**
	 * Called when the app cannot perform the required action due to error(s). 
	 * By default, the library will immediately finishes this activity. 
	 * Subclasses may override this method to display some notification to 
	 * user before finishing this activity.
	 * @param error code for the error.
	 */
	public void onError(int error) {
		this.finish();
	}
	
	/**
	 * Execute an action. Subclasses may override this method to implement
	 * how an action is executed.
	 * @param action action to be executed.
	 */
	public void executeAction(Action action) {
		try {
			if (action instanceof DirectServerRequestAction) {
				ServerRequestBuilder builder = ((DirectServerRequestAction) action).getServerRequestBuilder();
				this.impl.updateServerRequestBuilder(builder);
				this.impl.callLicenseServer(builder.getServerRequest(), this);
			}
			else if (action instanceof WebFormServerRequestAction) {
				ServerRequestBuilder builder = ((WebFormServerRequestAction) action).getServerRequestBuilder();
				this.impl.updateServerRequestBuilder(builder);
				this.impl.openLicenseServerPortal(builder.getServerRequest(), this);
			}
			else if (action instanceof CancelAction) {
				this.onError(this.ERROR_CANCELLED);
			}
			else if (action instanceof PromptAction) {
				PromptAction pa = (PromptAction) action;
				int csize = pa.getChoiceCount();
				String title = pa.getTitle();
				String summary = pa.getSummary();
				String desc = pa.getDescription();
				String[] labels = pa.getChoiceLabels();
				Action[] actions = pa.getChoiceActions();
	
				this.onPrompt(title, summary, desc, labels, actions);
			}
		} catch (Exception ex) {
			this.onError(this.ERROR_UNKNOWN);
		}
	}


	/**
	 * A basic Dialog button click listener.
	 *
	 * This listener is for handling button clicks on default onPrompt dialog.
	 * Each instance can handle click events for all buttons on a single dialog.
	 * Note that if a dialog is cancelled a cancelled error is emitted directly.
	 *
	 * @author Edison Chan
	 */
	private class CheckerActivityPromptHandler implements 
			DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener {
		private Action[] actions;

		/**
		 * Construct a new handler for an individual choice.
		 * @param action action to be executed on click.
		 */
		public CheckerActivityPromptHandler(Action[] actions) {
			this.actions = actions;
		}

		/**
		 * Called when a button on the dialog is pressed.
		 * @param dialog dialog the clicked button belongs to.
		 * @param which the button that is clicked.
		 */
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			CheckerActivity.this.executeAction(actions[which]);
		}

		/**
	   * Called when the dialog is dismissed.
	   * @param dialog dialog that is dismissed.
	   */
		public void onCancel(DialogInterface dialog) {
			dialog.dismiss();
			CheckerActivity.this.onError(CheckerActivity.this.ERROR_CANCELLED);
		}
	}
}

#endif	//USE_AU_DRM
