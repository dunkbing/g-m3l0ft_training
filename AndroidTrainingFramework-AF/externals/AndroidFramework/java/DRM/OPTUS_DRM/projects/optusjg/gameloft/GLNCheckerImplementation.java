#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg.gameloft;

import java.io.*;
import java.security.*;
import org.json.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.util.*;
import com.msap.store.drm.android.data.*;

/**
 * This class is the implementation class of GLNCheckerActivity. This class 
 * handles all behind-the-scene actions used by the GLNCheckerActivity class,
 * such as reading/writing obfuscated data in application specified file,
 * calling remote server, etc.
 *
 * This class is intended for library extension. Normal library client
 * should never deal with this class.
 *
 * @see GLNCheckerActivity
 * @author Edison Chan
 */
public class GLNCheckerImplementation extends CheckerImplementation {
	private static final int LOCAL_CHECK_RESULT_OK = 0;
	private static final int LOCAL_CHECK_RESULT_CANCELLED = 1;
	private static final int LOCAL_CHECK_RESULT_NOT_FOUND = 2;
	private static final int LOCAL_CHECK_RESULT_ERROR_UNKNOWN = 3;
	private static final int LOCAL_CHECK_RESULT_ERROR_VERIFY = 4;
	private static final int LOCAL_CHECK_RESULT_ERROR_INOUT = 5;
	private static final int LOCAL_CHECK_RESULT_ERROR_CRYPTO = 6;
	private static final int LOCAL_CHECK_RESULT_ERROR_SANITY = 7;
	private static final int RESPONSE_PROCESS_RESULT_OK = 0;
	private static final int RESPONSE_PROCESS_RESULT_ERROR = 1;
	private static final int RESPONSE_PROCESS_RESULT_ERROR_SANITY = 2;

	protected License rLicenseObject;
	protected int rLicenseSource;

	/**
	 * Construct a new GLNCheckerImplementation object.
	 */
	protected GLNCheckerImplementation() {
		this.rLicenseObject = null;
		this.rLicenseSource = 0;
	}

	/**
	 * Restore any saved states from a bundle.
	 * @param bundle bundle where the saved states are stored.
	 * @see android.app.Activity#onCreate
	 */
	public void loadSavedState(Bundle bundle) {
		if (bundle != null) {
			String classname = this.getClass().getName();
			License tmp1 = null;
			int tmp2 = 0;

			try {
				// Retrieve possible saved data from bundle

				if (bundle.containsKey(classname + ":LicenseObject")) {
					tmp1 = (License) bundle.getSerializable(classname + ":LicenseObject");
				}

				if (bundle.containsKey(classname + ":LicenseSource")) {
					tmp2 = bundle.getInt(classname + ":LicenseSource");
				}

				// We update the data if we currently do not have a license and the 
				// bundle contains a license.

				if (this.rLicenseObject == null && tmp1 == null) {
					this.rLicenseObject = tmp1;
					this.rLicenseSource = tmp2;
				}
			} catch (Exception ex) {
				// ignore saved state if they are invalid.
			}
		}
	}

	/**
	 * Save current states to bundle.
	 * @param bundle bundle where the current states are saved.
	 */
	public void saveCurrentState(Bundle bundle) {
		String classname = this.getClass().getName();

		if (this.rLicenseObject != null) {
			bundle.putSerializable(classname + ":LicenseObject", this.rLicenseObject);
			bundle.putInt(classname + ":LicenseSource", this.rLicenseSource);
		}
	}

	/**
	 * Get the result of the activity.
	 * @return intent containing the result of this activity.
	 */
	public Intent getResultData() {
		Intent result = new Intent();

		if (this.rLicenseObject != null) {
			result.putExtra(GLNCheckerActivity.ACTIVITY_RESULT_STATUS, true);
			result.putExtra(GLNCheckerActivity.ACTIVITY_RESULT_LICENSE, this.rLicenseObject);
			result.putExtra(GLNCheckerActivity.ACTIVITY_RESULT_SOURCE, this.rLicenseSource);
		} else {
			result.putExtra(GLNCheckerActivity.ACTIVITY_RESULT_STATUS, false);
		}

		return result;
	}

	/**
	 * Check if we have already obtained a license. Subclasses may call this 
	 * method to determine whether the status of the checker activity.
	 * @return true if the checker finds a valid license, false if not.
	 */
	public boolean hasLicense() {
		return (this.rLicenseObject != null);
	}

	/**
	 * Get the obtained license. Subclasses may call this method to examine the
	 * license obtained for this session.
	 * @return license loaded; null if no license is loaded yet.
	 */
	public License getLicenseData() {
		return this.rLicenseObject;
	}

	/**
	 * Get the source of the loaded obtained. Subclasses may call this method to
	 * determine how the license is obtained.
	 * @return source of license; 0 if no license is loaded yet.
	 */
	public int getLicenseSource() {
		return this.rLicenseSource;
	}

	/**
	 * Check if a local license can be found from local cache in a new thread.
	 * This method is implemented by creating an AsyncTask to perform the check
	 * in a worker thread and then call the appropriate callback methods in the 
	 * activity in the UI thread. However, be warned that there is a special 
	 * case: if the call fails due to sanity check failure, then the onError 
	 * call will be executed inline before this method returns.
	 * @param activity activity whose callback methods should be called.
	 */
	public void checkLocalLicense(GLNCheckerActivity activity) {
		if (this.isReady()) {
			if (this.hasLicense()) {
				if (this.getLicenseSource() == 1) {
					activity.onLocalLicenseFound();
				} else {
					activity.onRemoteLicenseGranted();
				}
			} else {
				new GLNLocalLicenseCheckTask(this, activity).execute();
			}
		} else {
			activity.onError(CheckerActivity.ERROR_SANITY_CHECK);
		}
	}

	/**
	 * Call remote license server to check for a license in a new thread. This 
	 * method uses the callLicenseServer method to invoke the license check
	 * method on the license server in a worker thread. Then the UI thread will
	 * process the response from the license server and call appropriate 
	 * callback methods in the activity class. However, be warned that there is 
	 * a special case: if the call fails due to sanity check failure, then the 
	 * onError call will be executed inline before this method returns.
	 * @param activity activity whose callback method should be called.
	 */
	public final void checkRemoteLicense(GLNCheckerActivity activity) {
		if (this.isReady()) {
			if (this.hasLicense()) {
				if (this.getLicenseSource() == 1) {
					activity.onLocalLicenseFound();
				} else {
					activity.onRemoteLicenseGranted();
				}
			} else {
				DBG("GLNCheckerImplementation", "Starting checking license from license server");
				ServerRequestBuilder builder = new ServerRequestBuilder("gln-license-v1");
				this.updateServerRequestBuilder(builder);
				this.callLicenseServer(builder.getServerRequest(), activity);
			}
		} else {
			activity.onError(CheckerActivity.ERROR_SANITY_CHECK);
		}
	}

	/**
	 * Process server response and dispatch it to appropriate callback methods.
	 * Regardless of the origin of the response, this method will process the
	 * given license server request/response pair and make the appropriate call
	 * to the given activity class. This method should be invoked in the UI 
	 * thread, so that the callback method is invoked in the UI thread as well.
	 * @param request server request that is executed.
	 * @param response server response to be processed.
	 * @param activity activity whose callback methods should be called.
	 */
	protected void processLicenseServerResponse(ServerRequest request, Object response, CheckerActivity activity) {
		try {
			GLNCheckerActivity activity2 = (GLNCheckerActivity) activity;

			if (response instanceof GLNLicenseResponse) {
				DBG("GLNCheckerImplementation", "License check completed");
				new GLNLicenseResponseProcessTask(this, activity2).execute((GLNLicenseResponse) response);
			} else if (response instanceof ErrorResponse) {
				ErrorResponse response2 = (ErrorResponse) response;
				int code = response2.getCode();

				if (request.getMethod().equals("license")) {
					DBG("GLNCheckerImplementation", "License check failed:");
					DBG("GLNCheckerImplementation", ">>> Error Code: " + code);
					activity2.onRemoteLicenseDenied(code);
				} else {
					super.processLicenseServerResponse(request, response, activity);
				}
			} else {
				super.processLicenseServerResponse(request, response, activity);
			}
		} catch (ClassCastException ex) {
			super.processLicenseServerResponse(request, response, activity);
		}
	}

	/**
	 * Check if a local license can be found from local cache data. This method
	 * requires computation of obfuscation key and other data, so it should only
	 * be called in a worker thread. It should not be called in a UI thread.
	 * @param context app context for opening app-specific file.
	 * @return numeric code for the check result.
	 */
	int executeLocalLicenseCheck(Context context) {
		DBG("GLNCheckerImplementation", "Start checking for local license...");

		if (this.isReady()) {
			try {
				License license = (License) this.readCacheFile(context);
				
				if (license != null) {
					if (this.validateLicense(license)) {
						this.rLicenseSource = 1;
						this.rLicenseObject = license;
						return this.LOCAL_CHECK_RESULT_OK;
					} else {
						return this.LOCAL_CHECK_RESULT_ERROR_VERIFY;
					}
				}
	
				return this.LOCAL_CHECK_RESULT_NOT_FOUND;
			} catch (FileNotFoundException ex) {
				return this.LOCAL_CHECK_RESULT_NOT_FOUND;
			} catch (GeneralSecurityException ex) {
				return this.LOCAL_CHECK_RESULT_ERROR_CRYPTO;
			} catch (IOException ex) {
				return this.LOCAL_CHECK_RESULT_ERROR_INOUT;
			} catch (Exception ex) {
				return this.LOCAL_CHECK_RESULT_ERROR_UNKNOWN;
			}
		} else {
			return this.LOCAL_CHECK_RESULT_ERROR_SANITY;
		}
	}

	/**
	 * Call appropriate GLNCheckerActivity callback for the given local 
	 * license check result. Since this method dispatches result of local 
	 * license checks to the appropriate callback methods in the activity
	 * class, this method MUST be called in the UI thread.
	 * @param result numerical code for the check result.
	 * @param activity activity whose callback methods should be called.
	 */
	void handleLocalLicenseCheckResult(int result, GLNCheckerActivity activity) {
		DBG("GLNCheckerImplementation", "Local license check result: " + result);

		if (result == this.LOCAL_CHECK_RESULT_OK) {
			activity.onLocalLicenseFound();
		} else if (result == this.LOCAL_CHECK_RESULT_NOT_FOUND) {
			activity.onLocalLicenseNotFound();
		} else if (result == this.LOCAL_CHECK_RESULT_CANCELLED) {
			activity.onError(CheckerActivity.ERROR_CANCELLED);
		} else if (result == this.LOCAL_CHECK_RESULT_ERROR_SANITY) {
			activity.onError(CheckerActivity.ERROR_SANITY_CHECK);
		} else {
			activity.onLocalLicenseNotFound();
		}
	}

	/**
	 * Process a license response. This operation can involved long running
	 * computation of obfuscation key and data I/O. Therefore, this method 
	 * should be executed in a worker thread.
	 * @param response license response to be processed.
	 * @param context app context for opening app-specific file.
	 * @return processing status.
	 */
	int executeLicenseResponseProcess(GLNLicenseResponse response, Context context) {
		if (this.isReady()) {
			License license = response.getLicense();

			try {
				this.writeCacheFile(context, license);
			} catch (Exception ex) {
				// We ignore problem here since a session license is already granted, so
				// even if we cannot cache future license, we must allow the user to 
				// continue.
			}

			this.rLicenseObject = license;
			this.rLicenseSource = 2;
			return this.RESPONSE_PROCESS_RESULT_OK;
		} else {
			return this.RESPONSE_PROCESS_RESULT_ERROR_SANITY;
		}
	}

	/**
	 * Call appropriate GLNCheckerActivity callback for the given license 
	 * response processing result. This method MUST be called in the UI 
	 * thread to make sure that the callback method is executed in the UI
	 * thread as well.
	 * @param result numerical code for the check result.
	 * @param activity activity whose callback methods should be called.
	 */
	void handleLicenseResponseProcessResult(int result, GLNCheckerActivity activity) {
		if (result == this.RESPONSE_PROCESS_RESULT_OK) {
			activity.onRemoteLicenseGranted();
		} else if (result == this.RESPONSE_PROCESS_RESULT_ERROR_SANITY) {
			activity.onError(CheckerActivity.ERROR_SANITY_CHECK);
		} else {
			activity.onRemoteLicenseDenied(0);
		}
	}
}

#endif	//USE_OPTUS_DRM
