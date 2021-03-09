#if USE_OPTUS_DRM
package com.msap.store.drm.android.util;

/**
 * Container for a tuple of two objects.
 */
public class Tuple<F,S> {
	public final F first;
	public final S second;

	/**
	 * Construct a new pair.
	 * @param first value of the first item.
	 * @param second value of the second item.
	 */
	public Tuple(F first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Check if the input object equals to this object.
	 * @param o object to be compared with.
	 * @return true if the object equals to this object, false otherwise.
	 */
	@Override
	@SuppressWarnings(value={"unchecked"})
	public boolean equals(Object o) {
		try {
			Tuple<F,S> p = (Tuple<F,S>) o;

			if (this.first.equals(p.first) == false) {
				return false;
			} else if (this.second.equals(p.second) == false) {
				return false;
			} else {
				return true;
			}
		} catch (ClassCastException ex) {
			return false;
		}
	}

	/**
	 * Compute a hash code using the hash code of the contained objects.
	 */
	public int hashCode() {
		int result = 17;
		result += 31 * this.first.hashCode();
		result += 31 * this.second.hashCode();
		return result;
	}
}

#endif	//USE_OPTUS_DRM
