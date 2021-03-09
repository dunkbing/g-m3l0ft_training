#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

import java.util.*;

/**
 * This class stores information on making a call to license server by
 * calling the API interface of the license server.
 * @author Edison Chan
 */
public class DirectServerRequestAction implements Action {
	public static final String TYPE_NAME = "direct-server-request-action";
	public static final String TYPE_NAME_COMPAT = "server-request-action";
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
	 * Costruct a new LicenseServerCallAction object.
	 * @param request ServerRequest object to be executed when performing the action.
	 */
	DirectServerRequestAction(ServerRequest request) {
		this.request = request;
	}
}

#endif	//USE_OPTUS_DRM
