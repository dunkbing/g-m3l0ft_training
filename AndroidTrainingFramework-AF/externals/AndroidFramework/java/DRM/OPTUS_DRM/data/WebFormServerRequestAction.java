#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

import java.util.*;

/**
 * This class stores information on making a call to license server via
 * its web portal.
 * @author Edison Chan
 */
public class WebFormServerRequestAction implements Action {
	public static final String TYPE_NAME = "webform-server-request-action";
	private ServerRequest request;

	/**
	 * Get a ServerRequest object for the action.
	 * @return ServerRequest object
	 */
	public ServerRequest getServerRequest() {
		return this.request;
	}

	/**
	 * Get a ServerRequestBuilder object for the action.
	 * @return ServerRequest object
	 */
	public ServerRequestBuilder getServerRequestBuilder() {
		return new ServerRequestBuilder(this.request);
	}

	/**
	 * Costruct a new LicenseServerPortalAction object.
	 * @param request ServerRequest object to be executed when performing the action.
	 */
	WebFormServerRequestAction(ServerRequest request) {
		this.request = request;
	}
}

#endif	//USE_OPTUS_DRM
