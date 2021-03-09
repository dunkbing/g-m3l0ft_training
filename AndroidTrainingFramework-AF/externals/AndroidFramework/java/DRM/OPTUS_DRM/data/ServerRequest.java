#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

import java.util.*;

/**
 * ServerRequest stores information about a request to license server.
 * @author Edison Chan
 */
public class ServerRequest implements Data {
	public static final String TYPE_NAME = "server-request";
	private String method;
	private Map<String,Object> parameters;
	
	/**
	 * Get the method to be invoked on the remote server.
	 * @return method to be invoked on the remote server.
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Check if the request contains the given parameter.
	 * @param name name of the parameter.
	 * @return true if the request contains such parameter; false otherwise.
	 */
	public boolean hasParameter(String name) {
		return this.parameters.containsKey(name);
	}

	/**
	 * Get the value of the parameter in the request.
	 * @param name name of the parameter to be retrieved
	 * @return Value of the parameter
	 */
	public Object getParameter(String name) {
		return this.parameters.get(name);
	}

	/**
	 * Returns an iterator over all parameter names in this request.
	 * @return an iterator over all parameter names in this request.
	 */
	public Iterator<String> parameterIterator() {
		return this.parameters.keySet().iterator();
	}

	/**
	 * Construct a new ServerReqeust object.
	 * @param method method to be invoked on the remote server.
	 */
	ServerRequest(String method) {
		this(method, null);
	}

	/**
	 * Construct a new ServerReqeust object.
	 * @param method method to be invoked on the remote server.
	 * @param parameter parameters to be sent to the remote server.
	 */
	ServerRequest(String method, Map<String,Object> parameters) {
		this.method = method;
		this.parameters = new HashMap<String,Object>();

		if (parameters != null) {
			this.parameters.putAll(parameters);
		}
	}
	
	/**
	 * Copy constructor. Note that this copy constructor only performs
	 * a shallow copy of the parameters.
	 * @param request request where the data should be copied from.
	 */
	ServerRequest(ServerRequest request) {
		this(request.method, request.parameters);
	}
}

#endif	//USE_OPTUS_DRM
