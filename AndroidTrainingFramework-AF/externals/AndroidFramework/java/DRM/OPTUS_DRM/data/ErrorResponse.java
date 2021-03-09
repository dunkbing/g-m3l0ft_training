#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

/**
 * ErrorResponse contains error response message from remote license server.
 */
public class ErrorResponse implements ServerResponse {
	public final static String TYPE_NAME = "error-response";
	private int code;
	private String message;
	
	/**
	 * Get the error code for the error.
	 * @return error code.
	 */
	public int getCode() {
		return this.code;
	}
	
	/**
	 * Get the error message.
	 * @return error message.
	 */
	public String getMessage() {
		return this.message;
	}
	
	/**
	 * Default constructor.
	 * @param code error code.
	 * @param message error message.
	 */
	ErrorResponse(int code, String message) {
		this.code = code;
		this.message = message;
	}
}

#endif	//USE_OPTUS_DRM
