#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

/**
 * License contains all information about a license. It contains information
 * about how the license can be used, as well as any applicable restrictions.
 */
public class License implements Data {
	public static final String TYPE_NAME = "license";
	public static final int TYPE_PERMANENT = 1;
	public static final int TYPE_ONETIME = 2;
	private int type;
	private String id;
	private String devid;
	private String userid;
	private String appid;
	private String serial;
	private LicenseRestriction[] restrictions;

	/**
	 * Get the ID of the license.
	 * @return ID of the license.
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Get the type of the license.
	 * @return type of the license.
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Get the ID of the device the license is granted to.
	 * @return ID of the device this license is granted to.
	 */
	public String getDeviceId() {
		return this.devid;
	}

	/**
	 * Get the ID of the user the license is granted to.
	 * @return ID of the user this license is granted to.
	 */
	public String getUserId() {
		return this.userid;
	}

	/**
	 * Get the ID of the app the license is granted for.
	 * @return ID of the app this license is granted for.
	 */
	public String getApplicationId() {
		return this.appid;
	}

	/**
	 * Get the serial number of the package the license is granted for.
	 * @return serial number of the package this license is granted for.
	 */
	public String getSerialNumber() {
		return this.serial;
	}

	/**
 	 * Get the number of restrictions in the license.
 	 * @return number of restrictions.
 	 */
	public int getRestrictionCount() {
		if (this.restrictions != null) {
			return this.restrictions.length;
		} else {
			return 0;
		}
	}
	
	/**
	 * Get the restriction at the specified index.
	 * @param index an index to the restriction list.
	 * @return the restriction at the specified index.
	 * @throws ArrayIndexOutOfBoundsException if an invalid index was given.
	 */
	public LicenseRestriction getRestriction(int index) {
		if (this.restrictions != null) {
			return this.restrictions[index];
		} else {
			throw new ArrayIndexOutOfBoundsException("Such restriction does not exist");
		}
	}

	/**
	 * Indicates whether some other object is equal to this license.
	 * @param object the reference with which to compare.
	 * @return true if this object is the same as the obj argument; false otherwise.
	 */
	public boolean equals(Object object) {
		if (object instanceof License) {
			return (this.id == ((License) object).id);
		} else {
			return false;
		}
	}	

	/**
	 * Create a new license object without any restrictions.
	 * @param id ID of the license.
	 * @param type type of license, like TYPE_PERMANENT or TYPE_ONETIME.
	 * @param devid ID of the device this license is granted to.
	 * @param userid ID of the user this license is granted to.
	 * @param appid ID of the app this license is granted for.
	 * @param serial Serial number of the package this license is granted for.
	 */
	License(String id, int type, String devid, String userid, String appid, String serial) {
		this.id = id;
		this.type = type;
		this.devid = devid;
		this.userid = userid;
		this.appid = appid;
		this.serial = serial;
		this.restrictions = new LicenseRestriction[0];
	}
	
	/**
	 * Create a new license object with restrictions.
	 * @param id ID of the license.
	 * @param type type of license, like TYPE_PERMANENT or TYPE_ONETIME.
	 * @param devid ID of the device this license is granted to.
	 * @param userid ID of the user this license is granted to.
	 * @param appid ID of the app this license is granted for.
	 * @param serial Serial number of the package this license is granted for.
	 * @param restrictions list of restrictions that are applicable to the license.
	 */
	License(String id, int type, String devid, String userid, String appid, String serial, LicenseRestriction[] restrictions) {
		this.id = id;
		this.type = type;
		this.devid = devid;
		this.userid = userid;
		this.appid = appid;
		this.serial = serial;
		this.restrictions = new LicenseRestriction[restrictions.length];

		for (int i = 0; i < restrictions.length; i++) {
			this.restrictions[i] = restrictions[i];
		}
	}
}

#endif	//USE_OPTUS_DRM
