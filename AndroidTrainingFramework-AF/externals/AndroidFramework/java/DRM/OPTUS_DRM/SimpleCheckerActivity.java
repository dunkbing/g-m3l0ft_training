#if USE_OPTUS_DRM
package com.msap.store.drm.android;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.data.*;
import com.msap.store.drm.android.projects.optusjg.gameloft.*;

public class SimpleCheckerActivity extends GLNCheckerActivity {
	private static final byte[] SALT = { -1, 10, 110, 127, 0, 5, 13, -76, -94, 12, 34, 76, -12, -110, -99, 55, 17, 89, -92, 10 };
	private static final String CERTID = "123";
	private static final byte[] CERTDATA = { 48, -126, 3, -89, 48, -126, 2, -113, -96, 3, 2, 1, 2, 2, 9, 0, -80, 116, -109, -26, 109, -86, -81, -101, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0, 48, 105, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 72, 75, 49, 21, 48, 19, 6, 3, 85, 4, 7, 12, 12, 68, 101, 102, 97, 117, 108, 116, 32, 67, 105, 116, 121, 49, 30, 48, 28, 6, 3, 85, 4, 10, 12, 21, 77, 111, 98, 105, 108, 101, 32, 83, 116, 114, 101, 97, 109, 115, 32, 72, 75, 32, 76, 116, 100, 49, 35, 48, 33, 6, 3, 85, 4, 3, 12, 26, 65, 110, 100, 114, 111, 105, 100, 32, 68, 82, 77, 32, 67, 101, 114, 116, 105, 102, 105, 99, 97, 116, 101, 32, 35, 49, 48, 32, 23, 13, 49, 49, 48, 52, 50, 54, 48, 55, 51, 53, 51, 50, 90, 24, 15, 50, 49, 49, 49, 48, 52, 48, 50, 48, 55, 51, 53, 51, 50, 90, 48, 105, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2, 72, 75, 49, 21, 48, 19, 6, 3, 85, 4, 7, 12, 12, 68, 101, 102, 97, 117, 108, 116, 32, 67, 105, 116, 121, 49, 30, 48, 28, 6, 3, 85, 4, 10, 12, 21, 77, 111, 98, 105, 108, 101, 32, 83, 116, 114, 101, 97, 109, 115, 32, 72, 75, 32, 76, 116, 100, 49, 35, 48, 33, 6, 3, 85, 4, 3, 12, 26, 65, 110, 100, 114, 111, 105, 100, 32, 68, 82, 77, 32, 67, 101, 114, 116, 105, 102, 105, 99, 97, 116, 101, 32, 35, 49, 48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -41, 126, 11, -66, 125, 1, -58, 64, -59, 80, -51, 62, -24, -66, -60, 41, -23, -90, -91, 82, -119, -126, 23, -17, -29, -117, 103, -63, -89, 1, 62, 101, 23, -73, -98, -43, -2, 40, -61, 52, 97, -59, -70, 125, -101, 76, -22, 84, -109, 38, -32, 18, -29, -19, -39, 106, 117, 6, -27, 28, 121, -86, 64, -84, 46, 51, 33, -14, 94, -97, 126, -65, -118, -12, 54, 83, -103, 98, -27, 70, 91, 88, 39, 73, -16, 89, -83, -100, -124, 18, 30, 18, 50, 16, -116, -23, -55, 34, -106, 55, -64, 40, 62, 91, -18, 36, 122, -101, -105, -93, 45, -33, -123, -68, -113, -88, -15, -88, -19, -18, 111, 59, 93, 29, 92, -12, -61, 44, -86, -67, 61, 46, -15, -104, 74, 117, -78, 28, 73, 116, -25, 59, -62, -47, -58, -62, -83, 6, 87, -3, -92, 124, -13, -63, 89, -66, -102, -100, 99, -18, -40, 77, 51, -97, 93, -59, 38, -54, -39, 91, -38, -76, 27, 69, -43, -12, -37, 64, 83, -114, -104, -105, 69, -38, 83, -85, 11, -53, 81, -65, 13, -108, -80, -4, 18, -15, -59, 52, 81, -48, -74, 91, 7, 62, -47, 104, 84, -4, -81, 74, 8, 42, -117, 58, -99, 121, -109, 37, -115, -47, 125, -124, -87, 70, -85, -111, -45, -2, -4, -103, 35, -22, 61, 66, -8, 14, 13, -72, -26, 40, -95, 16, 8, -108, 70, 104, 39, -30, 126, -14, -82, -13, -119, 112, 12, -111, 2, 3, 1, 0, 1, -93, 80, 48, 78, 48, 29, 6, 3, 85, 29, 14, 4, 22, 4, 20, 74, -71, -29, 26, -15, 88, 13, -30, 56, -101, -113, -2, 71, -78, -5, 89, -82, -48, 59, -85, 48, 31, 6, 3, 85, 29, 35, 4, 24, 48, 22, -128, 20, 74, -71, -29, 26, -15, 88, 13, -30, 56, -101, -113, -2, 71, -78, -5, 89, -82, -48, 59, -85, 48, 12, 6, 3, 85, 29, 19, 4, 5, 48, 3, 1, 1, -1, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 5, 5, 0, 3, -126, 1, 1, 0, -87, -9, -25, -73, 43, -8, 77, 119, 27, 58, 8, 116, -34, 3, -95, -12, -117, -7, 73, 66, -15, 69, 24, -108, -10, 58, 89, 123, 89, -86, -121, 4, 102, 65, 56, -62, -28, 104, -59, 30, 82, 67, 59, -67, -81, 83, 92, 84, 65, -57, -118, -82, -96, -94, 103, -104, -51, -60, 4, 68, 98, -23, 5, -12, -70, 18, -112, 100, -79, -58, 10, 41, 57, 16, -15, 119, 57, 122, -79, -78, -128, 25, 55, -102, 113, 84, 121, 106, -3, -53, 108, -102, 81, 96, 84, 0, -106, 76, -59, 56, -73, -59, -18, 119, -55, 38, 94, 4, 56, -83, 58, 93, 15, 99, 36, -19, -68, 98, -26, 56, -113, 31, 76, 1, -107, 118, -54, 102, -1, 24, -83, -65, -10, -43, -122, -4, 69, -73, 76, 37, 17, 13, 23, 4, -57, 9, -127, 66, -55, -127, 73, 40, 5, 52, 106, -119, -86, 62, 107, -50, 104, -69, 40, 86, -19, 49, 44, 79, 108, 57, 106, 112, -3, 127, -2, 9, -16, 59, -98, -112, 63, 96, 8, 34, 45, 63, -33, 36, -42, 18, 87, 33, -110, 45, 29, 61, -118, -37, -53, 60, -34, 28, 1, -105, 51, -59, -106, 48, -122, 124, 69, -92, -118, -96, 89, -121, 14, -105, -90, 104, -91, 54, -81, 40, -82, -63, 90, -30, 88, -56, 124, 50, -61, -75, -124, 59, -38, -44, -54, -81, -40, -126, 107, -91, -102, -113, 28, 69, -48, -82, -55, 13, -115, 66, -7, 107 };

	@Override
	protected void onStart()
	{
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	}
	
	@Override
	protected String getCertificateId() {
		return this.CERTID;
	}

	@Override
	protected byte[] getCertificateData() {
		return this.CERTDATA;
	}

	@Override
	protected byte[] getObfuscationSalt() {
		return this.SALT;
	}
}

#endif	//USE_OPTUS_DRM
