#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

/**
 * LicenseCache implements a license cache that holds license for future use.
 * This is essentially a LicenseList object, but it is mutable.
 * @see LicenseList
 */
public class LicenseCache extends LicenseList {
	public static final String TYPE_NAME = "license-cache";

	/**
	 * Construct an empty license cache.
	 */
	public LicenseCache() {
		// do nothing
	}

	/**
	 * Add an individual license to the cache.
	 * @param license license to be added to the cache.
	 */
	public void add(License license) {
		super.add(license);
	}

	/**
	 * Add all licenses in the list to the cache.
	 * @param list list whose content is to be added to the cache.
	 */
	public void add(LicenseList list) {
		super.add(list);
	}

	/**
	 * Remove an license from the cache.
	 * @param license license to be removed from this cache.
	 */
	public boolean remove(License license) {
		return super.remove(license);
	}
}

#endif	//USE_OPTUS_DRM
