#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

/**
 * This class stores response from license server asking to have some actions
 * performed.
 * @author Edison Chan
 */
public class ActionResponse implements ServerResponse {
	public static final String TYPE_NAME = "action-response";
	private Action action;

	/**
	 * Get the action to be performed.
	 * @return action to be performed.
	 */
	public Action getAction() {
		return this.action;
	}

	/**
	 * Construct a new ActionResponse object.
	 * @param action action to be performed.
	 */
	ActionResponse(Action action) {
		this.action = action;
	}
}

#endif	//USE_OPTUS_DRM
