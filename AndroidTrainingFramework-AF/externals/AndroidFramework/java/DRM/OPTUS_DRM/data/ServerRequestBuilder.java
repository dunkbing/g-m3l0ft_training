#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

import java.util.*;

/**
 * This class implements a builder for request objects.
 * @see ServerRequest
 */
public class ServerRequestBuilder {
	private String method;
	private Map<String,Object> parameters;

	/**
	 * Construct a new request builder object calling the specified method.
	 * @param method method to be invoked.
	 */
	public ServerRequestBuilder(String method) {
		this.method = method;
		this.parameters = new HashMap<String,Object>();
	}

	/**
	 * Construct a new builder from an existing ServerRequest object.
	 * @param request the ServerRequest object used as a template.
	 */
	public ServerRequestBuilder(ServerRequest request) {
		this.method = request.getMethod();
		this.parameters = new HashMap<String,Object>();

		for (Iterator<String> i = request.parameterIterator(); i.hasNext(); ) {
			String name = i.next();
			Object value = request.getParameter(name);
			this.parameters.put(name, value);
		}
	}

	/**
	 * Update the method invoked by the request.
	 * @param method method to be invoked.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, byte value) {
		this.parameters.put(name, new Byte(value));
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, short value) {
		this.parameters.put(name, new Short(value));
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, int value) {
		this.parameters.put(name, new Integer(value));
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, long value) {
		this.parameters.put(name, new Long(value));
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, float value) {
		this.parameters.put(name, new Float(value));
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, double value) {
		this.parameters.put(name, new Double(value));
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, boolean value) {
		this.parameters.put(name, new Boolean(value));
	}

	/**
	 * Set the parameter to the specified value.
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 */
	public void putParameter(String name, String value) {
		this.parameters.put(name, value);
	}

	/**
	 * Get the request object.
	 * @return request object built.
	 */
	public ServerRequest getServerRequest() {
		return new ServerRequest(this.method, this.parameters);
	}
}

#endif	//USE_OPTUS_DRM
