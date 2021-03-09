#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg.gameloft;

import android.app.*;
import android.os.*;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.data.*;

/**
 * Asynchronous task for checking local license.
 * 
 * The task does not accept any parameter. It will check license cache for 
 * any usable licenses. Once it finds one, it will call the callback method 
 * onLocalLicenseFound of the activity class. If the task cannot find any 
 * usable local license in the device, it will invoke onLocalLicenseNotFound 
 * method instead.
 *
 * @author Edison Chan
 */
class GLNLocalLicenseCheckTask extends AsyncTask<Void, Void, Integer> {
	private GLNCheckerActivity activity;
	private GLNCheckerImplementation impl;

	/**
	 * Create a new GLNLocalLicenseCheckTask object.
	 * @param impl implementation objects that checks the local license.
	 * @param activity activity to be called back.
	 */
	GLNLocalLicenseCheckTask(GLNCheckerImplementation impl, GLNCheckerActivity activity) {
		if (activity == null) {
			throw new NullPointerException();
		} else if (impl == null) {
			throw new NullPointerException();
		} else {
			this.activity = activity;
			this.impl = impl;
		}
	}

	/**
	 * Check local license in the background.
	 * @param unused an unused parameter that existence is to satisfy type constraints.
	 * @return true if a local license is found; false otherwise.
	 */
	protected Integer doInBackground(Void... unused) {
		return this.impl.executeLocalLicenseCheck(this.activity);
	}

	/**
 	 * Report the result of local license check to the activity via callbacks. 
 	 * The callbacks will be executed in the UI thread so that they can easily 
 	 * update the user interface without some black magic on their part.
	 * @param result Result of local license check.
	 */
	protected void onPostExecute(Integer result) {
		this.impl.handleLocalLicenseCheckResult(result.intValue(), this.activity);
	}

	/**
 	 * Report the result of local license check to the activity via callbacks. 
 	 * The callbacks will be executed in the UI thread so that they can easily 
 	 * update the user interface without some black magic on their part.
	 * @param result Result of local license check.
	 */
	protected void onCancelled(Integer result) {
		this.onPostExecute(result);
	}
}

#endif	//USE_OPTUS_DRM
