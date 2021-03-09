#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg.gameloft;

import android.app.*;
import android.os.*;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.data.*;

/**
 * Asynchronous task for processing license response from the license 
 * server.
 *
 * This task will take a license response, extract relevant data, update 
 * license cache and finally call the appropriate callback method in the 
 * activity class.
 *
 * @author Edison Chan
 */
class GLNLicenseResponseProcessTask extends AsyncTask<GLNLicenseResponse, Void, Integer> {
	private GLNCheckerActivity activity;
	private GLNCheckerImplementation impl;

	/**
	 * Create a new GLNLicenseResponseProcessTask object.
	 * @param impl implementation object that processes the license response.
	 * @param activity activity to be called back.
	 */
	GLNLicenseResponseProcessTask(GLNCheckerImplementation impl, GLNCheckerActivity activity) {
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
	 * Process the license response from the license server.
	 * @param responses license responses from the license server.
	 * @return numeric status of the process result.
	 */
	protected Integer doInBackground(GLNLicenseResponse... responses) {
		return this.impl.executeLicenseResponseProcess(responses[0], this.activity);
	}

	/**
 	 * Notify the activity that the license response is processed.
 		 * @param result result of the processing.
	 */
	protected void onPostExecute(Integer result) {
		this.impl.handleLicenseResponseProcessResult(result.intValue(), this.activity);
	}

	/**
 	 * Notify the activity that the license response is processed.
 		 * @param result result of the processing.
	 */
	protected void onCancelled(Integer result) {
		this.onPostExecute(result);
	}
}

#endif	//USE_OPTUS_DRM
