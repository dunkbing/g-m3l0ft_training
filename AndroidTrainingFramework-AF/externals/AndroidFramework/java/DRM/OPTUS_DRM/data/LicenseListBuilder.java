#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

import java.util.*;

/**
 * LicenseListBuilder provides a tool to create a custom license list.
 * @see LicenseList
 */
public class LicenseListBuilder {
	private List<License> list;

	/**
	 * Construct an empty license cache.
	 */
	public LicenseListBuilder() {
		this.list = new ArrayList<License>();
	}

	/**
	 * Returns the number of licenses in this list.
	 * @return the number of licenses in this list.
	 */
	public int size() {
		return this.list.size();
	}

	/**
	 * Tests if the specific license is in this list.
	 * @param license a license.
	 * @return true if the specified license is in this list; false otherwise.
	 */
	public boolean contains(License license) {
		return this.list.contains(license);
	}
	
	/**
	 * Returns the license at the specified index.
	 * @param index an index to this list.
	 * @return the license at the specified index.
	 * @throws IndexOutOfBoundsException if an invalid index is given
	 */
	public License get(int index) {
		return this.list.get(index);
	}

	/**
	 * Add an individual license to the cache.
	 * @param license license to be added to the cache.
	 */
	public void add(License license) {
		this.list.add(license);
	}

	/**
	 * Remove an license from the cache.
	 * @param license license to be removed from this cache.
	 */
	public boolean remove(License license) {
		return this.list.remove(license);
	}

	/**
	 * Get license list.
	 */
	public LicenseList getLicenseList() {
		LicenseList list = new LicenseList();

		for (License license : this.list) {
			list.add(license);
		}

		return list;
	}
}

#endif	//USE_OPTUS_DRM
