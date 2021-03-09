#if USE_OPTUS_DRM
package com.msap.store.drm.android;

import android.app.*;
import android.os.*;

import com.msap.store.drm.android.data.*;
import com.msap.store.drm.android.util.*;

/**
 * Asynchronous task for calling license server.
 *
 * This task will take a server request, send it to the license server 
 * and process the server response. It will invoke different callback 
 * method when it receives different responses from the license server.
 *
 * @see CheckerActivity
 * @see CheckerImplementation
 * @author Edison Chan
 */
class LicenseServerCallTask extends AsyncTask<ServerRequest, Void, Tuple<ServerRequest,Object>> {
	private CheckerActivity activity;
	private CheckerImplementation impl;

	/**
	 * Construct a new LicenseServerCallTask object.
	 * @param impl implementation object that calls the license server.
	 * @param activity activity to be called back.
	 */
	LicenseServerCallTask(CheckerImplementation impl, CheckerActivity activity) {
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
	 * Call license server to execute a remote task.
	 * @param requests request to be submitted to license server.
	 * @return response from the license server.
	 */
	protected final Tuple<ServerRequest,Object> doInBackground(ServerRequest... requests) {
		ServerRequest request = requests[0];
		Object temp = this.impl.executeLicenseServerNonceCall();

		if (this.isCancelled()) {
			return new Tuple<ServerRequest,Object>(request, new Integer(CheckerActivity.ERROR_CANCELLED));
		} else {
			if (temp instanceof String) {
				String snonce = (String) temp;
				Object response = this.impl.executeLicenseServerCall(request, snonce);

				return new Tuple<ServerRequest,Object>(request, response);
			} else {
				return new Tuple<ServerRequest,Object>(request, temp);
			}
		}
	}

	/**
 	 * Notify the activity of server response by calling the activity method
 	 * onLicenseServerResponse.
 	 * @param data response object from license server.
	 */
	protected final void onCancelled(Tuple<ServerRequest,Object> data) {
		this.onPostExecute(data);
	}

	/**
	 * Notify the activity of server response by calling the activity method
 	 * onLicenseServerResponse.
 	 * @param data response object from license server.
	 */
	protected void onPostExecute(Tuple<ServerRequest,Object> data) {
		this.impl.processLicenseServerResponse(data.first, data.second, this.activity);
	}
}

#endif	//USE_OPTUS_DRM
