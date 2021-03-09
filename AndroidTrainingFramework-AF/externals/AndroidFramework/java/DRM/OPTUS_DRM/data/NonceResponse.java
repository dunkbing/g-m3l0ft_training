#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

/**
 * ErrorResponse contains error response message from remote license server.
 */
public class NonceResponse implements ServerResponse {
	public static final String TYPE_NAME = "nonce-response";
	private String nonce;
	
	/**
	 * Get the value of the nonce string.
	 * @return value of the nonce string.
	 */
	public String getNonce() {
		return this.nonce;
	}
	
	/**
	 * Default constructor.
	 * @param nonce value of the nonce string.
	 */
	NonceResponse(String nonce) {
		this.nonce = nonce;
	}
}

#endif	//USE_OPTUS_DRM
