#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

import java.util.*;
import org.json.*;

/**
 * LicenseList stores a list of unique licenses. Like Java string class, 
 * this class is immutable by external users.
 * @see License
 */
public class LicenseList implements Data {
	public static final String TYPE_NAME = "license-list";
	private List<License> list;
	
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
		return (License) this.list.get(index);
	}
	
	/**
	 * Default constructor.
	 */
	LicenseList() {
		this.list = new ArrayList<License>();
	}

	/**
	 * Add a new license to this list.
	 * @param license license to be added to this list.
	 */
	void add(License license) {
		if (this.list.contains(license) == false) {
			this.list.add(license);
		}
	}

	/**
	 * Add all license in the specified list to this list.
	 * @param list license list whose content are to be added.
	 */
	void add(LicenseList list) {
		for (Iterator<License> i = list.list.iterator(); i.hasNext(); ) {
			License license = i.next();

			if (this.list.contains(license) == false) {
				this.list.add(license);
			}
		}
	}

	/**
	 * Remove the first occurrence of the license from this list.
	 * @param license license to be removed.
	 * @return true if the license is found and removed from the list; false otherwise
	 */
	boolean remove(License license) {
		return this.list.remove(license);
	}
}

#endif	//USE_OPTUS_DRM
