#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

/**
 * Signed license is a license that contains a cryptographic signature for
 * verifying authenticity of the license.
 * @author Edison Chan
 */
public class SignedLicense extends License {
	public static final String TYPE_NAME = "signed-license";
	private String rawdata;
	private String key;
	private String signature;

	/**
	 * Get the raw data of this license. The raw data is used to compute the
	 * digital signature of the license.
	 * @return raw data of this license.
	 */
	public String getRawData() {
		return this.rawdata;
	}

	/**
	 * Get the ID of the signature key.
	 * @return ID of the key used in signing.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Get the digital signature of the license. The digital signature is
	 * computed in accordance to PKCS#1 using SHA1 and RSA.
	 * @return signature of the license.
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
	 * Create a new signed license object without any restrictions.
	 * @param rawdata Raw data of the license.
	 * @param id ID of the license.
	 * @param type type of license, like TYPE_PERMANENT or TYPE_ONETIME.
	 * @param devid ID of the device this license is granted to.
	 * @param userid ID of the user this license is granted to.
	 * @param appid ID of the app this license is granted for.
	 * @param serial Serial number of the package this license is granted for.
	 * @param key key used for signing the data.
	 * @param signature signature of the license.
	 */
	SignedLicense(String rawdata, String id, int type, String devid, String userid, String appid, String serial, String key, String signature) {
		super(id, type, devid, userid, appid, serial);
		this.rawdata = rawdata;
		this.key = key;
		this.signature = signature;
	}
	
	/**
	 * Create a new license object with restrictions.
	 * @param rawdata Raw data of the license.
	 * @param id ID of the license.
	 * @param type type of license, like TYPE_PERMANENT or TYPE_ONETIME.
	 * @param devid ID of the device this license is granted to.
	 * @param userid ID of the user this license is granted to.
	 * @param appid ID of the app this license is granted for.
	 * @param serial Serial number of the package this license is granted for.
	 * @param restrictions list of restrictions that are applicable to the license.
	 * @param key key used for signing the data.
	 * @param signature signature of the license.
	 */
	SignedLicense(String rawdata, String id, int type, String devid, String userid, String appid, String serial, LicenseRestriction[] restrictions, String key, String signature) {
		super(id, type, devid, userid, appid, serial, restrictions);
		this.rawdata = rawdata;
		this.key = key;
		this.signature = signature;
	}
}

#endif	//USE_OPTUS_DRM
