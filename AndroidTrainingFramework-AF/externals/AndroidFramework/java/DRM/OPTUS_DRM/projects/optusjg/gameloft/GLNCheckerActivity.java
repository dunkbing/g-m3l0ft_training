#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg.gameloft;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;
import android.view.KeyEvent;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.util.*;
import com.msap.store.drm.android.data.*;
import com.msap.store.drm.android.projects.optusjg.*;

/**
 * This class contains the implementation of CheckerActivity that provides
 * support for the test site.
 *
 * @see GLNCheckerImplementation
 * @author Edison Chan
 */
abstract public class GLNCheckerActivity extends OptusGamesCheckerActivity {
	/**
	 * A key in the extra bundle of the activity result; the value contains 
	 * a boolean which states whether the app can continue (possibly with 
	 * some restrictions).
	 */
	public final static String ACTIVITY_RESULT_STATUS = "com.msap.store.drm.android.projects.gln.status";

	/**
	 * A key in the extra bundle of the activity result; the value contains
	 * a license object for the session. Client can get more information about
	 * rights and restrictions from this object.
	 */
	public final static String ACTIVITY_RESULT_LICENSE = "com.msap.store.drm.android.projects.gln.license";

	/**
	 * A key in the extra bundle of the activity result; the value contains
	 * a numerical value indicating the source of the license. 1 for license
	 * obtained from local license cache; 2 for license acquired via a call to
	 * license server.
	 */
	public final static String ACTIVITY_RESULT_SOURCE = "com.msap.store.drm.android.projects.gln.source";

	/**
	 * Implementation used by this GLNCheckerActivity object.
	 */
	private GLNCheckerImplementation impl;

	/**
	 * Flag to determine if Android manfiest metadata is loaded or not.
	 */
	private boolean cLoaded = false;

	/**
	 * Bundle that contains all Android manifest metadata.
	 */
	private Bundle cBundle = null;

	/**
	 * Construct a new GLNCheckerActivity object.
	 */
	protected GLNCheckerActivity() {
		this(new GLNCheckerImplementation());
	}

	/**
	 * Construct a new GLNCheckerActivity object with the given implementation.
	 * @param impl implementation used by this GLNCheckerActivity object.
	 */
	GLNCheckerActivity(GLNCheckerImplementation impl) {
		super(impl);
		this.impl = impl;
	}

	/**
	 * Initialize the activity and start checking local license. Sublcasses 
	 * can override this method to implement better user interfaces, provided 
	 * that they call the implementation of superclass.
	 * @param savedInstanceState saved data to resurrect destroyed activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.startLocalLicenseCheck();
	}

	/**
	 * Get the application identifer from package manifest and return it.
	 * @return application id.
	 */
	@Override
	protected final String getApplicationId() {
		return this.readManifest("com.msap.store.drm.android.appid");
	}

	/**
	 * Get the user identifer from package manifest and return it.
	 * @return user id.
	 */
	@Override
	protected final String getUserId() {
		return this.readManifest("com.msap.store.drm.android.userid");
	}

	/**
	 * Get the package serial number from package manifest and return it.
	 * @return serial number.
	 */
	@Override
	protected final String getSerialNumber() {
		return this.readManifest("com.msap.store.drm.android.serial");
	}

	/**
	 * Return the URL of the API endpoint of the license server.
	 * @return URL of the license server API endpoint.
	 */
	@Override
	protected final String getLicenseServerCallUrl() {
		return this.readManifest("com.msap.store.drm.android.api_url");
	}

	/**
	 * Get the URL of license server product registration portal.
	 * @return URL of license server.
	 */
	@Override
	protected final String getLicenseServerPortalUrl() {
		return this.readManifest("com.msap.store.drm.android.portal_url");
	}

	/**
	 * Get the parser of internal data and license server response. Subclasses
	 * may override this method to enable handling of custom license 
	 * restrictions.
	 * @return data serializer to be used by the checker.
	 */
	@Override
	protected final Serializer getSerializer() {
		return new GLNSerializer();
	}

	/**
	 * Called when a local license is found on the device.
	 */
	public final void onLocalLicenseFound() {
		this.showSuccessScreen();
	}

	/**
	 * Called when no local license is found on the device.
	 */
	public final void onLocalLicenseNotFound() {
		this.startRemoteLicenseCheck();
	}

	/**
	 * Called when a license is granted by license server.
	 */
	public final void onRemoteLicenseGranted() {
		this.showSuccessScreen();
	}
	
	/**
	 * Called whenever a license request to license server is denied.
	 * @param reason reason for the denial.
	 */
	public final void onRemoteLicenseDenied(int reason) {
		this.showDenialScreen();
	}

	/**
	 * Called whenever a license request to license server is denied. 
	 */
	public final void onRemoteLicenseDenied() {
		this.showDenialScreen();
	}

	/**
	 * Called when the servcer requires confirmation from user.
	 * @param title title of the prompt.
	 * @param summary a brief description of the prompt.
	 * @param description the full description of the prompt.
	 * @param labels label for each choice.
	 * @param actions action for each choice.
	 */
	@Override
	public void onPrompt(String title, String summary, String description, String[] labels, Action[] actions) {
		super.onPrompt(title, summary, description, labels, actions);
	}

	/**
	 * Called when the app cannot perform the required action due to error(s). 
	 * @param error code for the error.
	 */
	@Override
	public final void onError(int error) {
		if (error == 23) {
			this.showNetworkErrorScreen();
		} else {
			this.showMiscErrorScreen();
		}
	}

	/**
	 * Called whenever an unrecognized link is clicked on the user interface.
	 * @param url URL of the link clicked.
	 */
	@Override
	public final void onLinkClick(String url) {
		if (url.equals("action:retry")) {
			this.startRemoteLicenseCheck();
		}
	}

	/**
	 * Execute an action.
	 * @param action Action to be executed.
	 */
	@Override
	public final void executeAction(Action action) {
		super.executeAction(action);
	}

	/**
	 * Check if we have already obtained a license.
	 * @return true if the checker finds a valid license, false if not.
	 */
	public final boolean hasLicense() {
		return this.impl.hasLicense();
	}

	/**
	 * Get the obtained license.
	 * @return license loaded; null if no license is loaded yet.
	 */
	public final License getLicenseData() {
		return this.impl.getLicenseData();
	}

	/**
	 * Get the source of the loaded obtained.
	 * @return source of license; 0 if no license is loaded yet.
	 */
	public final int getLicenseSource() {
		return this.impl.getLicenseSource();
	}

	/**
	 * Start the check of local license on the device.
	 */
	public final void startLocalLicenseCheck() {
		this.showCheckingScreen();
		this.impl.checkLocalLicense(this);
	}

	/**
	 * Check remote license using API provided by the license server.
	 */
	public final void startRemoteLicenseCheck() {
		this.showCheckingScreen();
		this.impl.checkRemoteLicense(this);
	}

	/**
	 * Read package manifest and retrieve relevant metadata from it.
	 * @param key name of the metadata entry in the manifest.
	 * @return value of the metadata if found; null if it does not exist.
	 */
	private String readManifest(String key) {
		if (this.cLoaded == false) {
			try {
				PackageManager packman = getPackageManager();
				ApplicationInfo appinfo = packman.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
				Bundle metadata = appinfo.metaData;
		
				this.cLoaded = true;
				this.cBundle = metadata;
			} catch (Exception ex) {
				this.cLoaded = true;
				this.cBundle = null;
			}
		}

		if (this.cBundle != null) {
			if (this.cBundle.containsKey(key)) {
				return this.cBundle.getString(key);
			}
		}

		return null;
	}
	
	//VTN disable back key.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK) || (keyCode == KeyEvent.KEYCODE_SEARCH)) 
		{
	       return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}

#endif	//USE_OPTUS_DRM
