#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg.gameloft;

import com.msap.store.drm.android.data.*;

/**
 * GLNLicenseResponse is the reply a license server will send when it 
 * wants to grant the receiver license to use the app.
 *
 * @see GLNSerializer
 * @author Edison Chan
 */
public class GLNLicenseResponse implements ServerResponse {
	public static final String TYPE_NAME = "gln-license-response";
	private License license;
	
	/**
	 * Get the license usable for the current session.
	 * @return license for the current session.
	 */
	public License getLicense() {
		return this.license;
	}
	
	/**
	 * Default constructor.
	 * @param license license.
	 */
	GLNLicenseResponse(License license) {
		this.license = license;
	}
}

#endif	//USE_OPTUS_DRM
