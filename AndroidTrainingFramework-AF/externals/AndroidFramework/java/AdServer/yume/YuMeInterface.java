/**
 *  YuMeSDKInterface.java 
 *  
 *  Purpose: Implements the functions defined by YuMe SDK interface 'YuMeAppInterface'.
 *  Also contains some internal functions acts as an internal interface
 *  for other application views to interact with YuMe SDK.
 *  Implemented as a singleton class to avoid multiple instantiations. 
 *  
 *  © 2011 YuMe, Inc. All rights reserved. 
 */

package APP_PACKAGE;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.yume.android.sdk.YuMeAPIInterface;
import com.yume.android.sdk.YuMeAPIInterfaceImpl;
import com.yume.android.sdk.YuMeAdBlockType;
import com.yume.android.sdk.YuMeAdEvent;
import com.yume.android.sdk.YuMeAdParams;
import com.yume.android.sdk.YuMeAppInterface;
import com.yume.android.sdk.YuMeException;

/**
 * Implements the functions defined by YuMe SDK interface 'YuMeAppInterface'.
 * Also contains some internal functions acts as an internal interface
 * for other application views to interact with YuMe SDK.
 * Implemented as a singleton class to avoid multiple instantiations. 
 */
public class YuMeInterface implements YuMeAppInterface {
	
	/* handle to YuMe SDK */
	private static YuMeAPIInterface yumeSDK = null;
	
	/* self instance */
	private static YuMeInterface yumeSDKInterface;
	
	/* HomeView handle */
	private Activity homeView;
	
	/* AdView handle */
	private YuMeActivity adView;
	
	//Constructor 
	private YuMeInterface() {
		/* instantiate the YuMe SDK */
		if(Integer.parseInt(Build.VERSION.SDK) >= 8) {
			if(yumeSDK == null) {
				INFO("ADSERVER", "Android API Level >= 8. Hence, instantiating YuMe SDK.");
				yumeSDK = new YuMeAPIInterfaceImpl();
			}
		} else {
			INFO("ADSERVER", "Android API Level < 8. Hence, not instantiating YuMe SDK.");
		}		
	}
	
	/**
	 * Creates the YuMeSDKInterface instance, if null.
	 * Else returns the existing YuMeSDKInterface handle.
	 * @param None. 
	 * @return yumeSDKInterface - handle to the YuMeSDKInterface module.
	 */	
	public static synchronized YuMeInterface getYuMeSDKInterface() {
		if(yumeSDKInterface == null) {
			yumeSDKInterface = new YuMeInterface();
		}
		return yumeSDKInterface;
	}
	
	/**
	 * Throws an exception when trying to clone a copy of YuMeSDKInterface.
	 * @param None. 
	 * @return None.
	 */	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Performs the clean-up of the YuMeSDKInterface.
	 * @param None. 
	 * @return None.
	 */		
	public void cleanUp() {
		/* perform the necessary clean-up */
		yumeSDKInterface = null;
	}	
	
	/**
	 * Sets the HomeView handle.
	 * @param homeView - handle to the HomeView. 
	 * @return None.
	 */	
	public void setHomeView(Activity homeView) {
		this.homeView = homeView;
	}
	
	/**
	 * Sets the AdView handle.
	 * @param adView - handle to the AdView. 
	 * @return None.
	 */
	public void setAdView(YuMeActivity adView) {
		this.adView = adView;
	}

	////////////IMPLEMENTATION OF YuMeAppInterface FUNCTIONS - START /////////// 
	/**
	 * Listens for SDK ad events.
	 * @param adBlockType - ad block type notified by the YuMe SDK.
	 * @param adEvent - ad event notified by the YuMe SDK. 
	 * @param eventInfo - ad event info notified by the YuMe SDK. 
	 * @return None.
	 */	
	
	@Override
	public void YuMeSDK_EventListener(YuMeAdBlockType adBlockType, YuMeAdEvent adEvent, String eventInfo) {
		INFO("ADSERVER", "YuMeAdBlockType: " + adBlockType + ", YuMeAdEvent: " + adEvent + ", Event Info: " + eventInfo);
		switch (adEvent) {
			case YUME_ADEVENT_ADREADY:
				if(eventInfo.equals("true")) {
					showInfo("All Ad Creatives Downloaded. Ad Ready for Playing.");
					AdServer.isYumeReady = true;
				}
				else {
					showInfo("Playlist Received, Ad Caching in Progress...");
					AdServer.isYumeReady = false;
				}
				break;
			case YUME_ADEVENT_ADNOTREADY:
				AdServer.isYumeReady = false;
				showInfo("Ad Not Ready");
				break;
			case YUME_ADEVENT_ADPRESENT:
				INFO("ADSERVER", "AD PRESENT");
				break;
			case YUME_ADEVENT_ADPLAYING:
				INFO("ADSERVER", "AD PLAYING");
				break;
			case YUME_ADEVENT_ADABSENT:
				INFO("ADSERVER", "AD ABSENT");
				break;
			case YUME_ADEVENT_ADCOMPLETED:
				INFO("ADSERVER", "AD COMPLETED");
				adView.onBackPressed();	//this takes you back to the HomeView
				break;
			case YUME_ADEVENT_ADERROR:
				adView.onBackPressed(); //this takes you back to the HomeView
				showError("Ad Error Event Received from SDK.");
				break;
			case YUME_ADEVENT_ADEXPIRED:
				INFO("ADSERVER", "AD EXPIRED");
				//prefetch another ad
				prefetchAd();
				break;
			default:
				break;
		}
	}
		
	/**
	 * Gets the activity context.
	 * @param None. 
	 * @return the activity context.
	 */
	@Override
	public Context getActivityContext() {
		/* get the adview activity context */
		if(adView != null)
			return adView.getActivityContext();

		return null;
	}
	
	/**
	 * Gets the Application context.
	 * @param None. 
	 * @return the Application context.
	 */	 
	@Override
	public Context getApplicationContext() {
		/* get the application context */
		if(homeView != null)
			return homeView.getApplicationContext();
		else if(adView != null)
			return adView.getAppContext();
		
		return null;
	}
	
	/**
	 * Gets the combined height of status bar and title bar.
	 * @param None. 
	 * @return the combined height of status bar and title bar, else 0.
	 * THIS METHOD IS NOT USED BY THE YUME SDK NOW.
	 */	
	@Override
	public int getStatusBarAndTitleBarHeight() {
		return 0;
	}
	//////////// IMPLEMENTATION OF YuMeAppInterface FUNCTIONS - END ///////////

	/**
	 * Gets the YuMe SDK Version.
	 * @param None. 
	 * @return sdkVersion - the sdk version info.
	 */	
	public String getVersion() {
		String sdkVersion = null;
		try {
			sdkVersion = yumeSDK.YuMeSDK_GetVersion();
		} catch (YuMeException e) {
			DBG_EXCEPTION(e);
			ERR("ADSERVER", e.getMessage());
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
		return sdkVersion;
	};
	
	/**
	 * Initializes YuMe SDK with ad params.
	 * @param none.
	 * @return true, if Init is success, else false.
	 */	
	public boolean initYuMeSDK(String serverUrl, String domainId) {
		boolean retVal = false;
		YuMeAdParams adParams = new YuMeAdParams();
		try {
			/* Ad server URL */
			adParams.adServerUrl = serverUrl;
		
			/* Publisher domain */
			adParams.domainId = domainId;
			
			/* Disk quota in MB for storing the prefetched assets. */
			adParams.storageSize = 10; //10 MB
			
			/* The playlist response timeout value (in seconds).
			Valid value is between 4 and 60 including.
			Default value is 5 (if timeOut < 4 default will be used).
			If timeOut is > 60 exception will be returned */
			adParams.adTimeout = 8;
			
			/* Timeout value for interruption during ad streaming (in seconds).
			Valid value is between 3 and 60 including.
			Default value is 6 (if value < 3, default will be used).
			If value is > 60 exception will be returned.*/
			adParams.videoTimeout = 8;
			
			adParams.bEnableLocationSupport = false;
		
		#if RELEASE_VERSION
			adParams.bEnableFileLogging = false;
			adParams.bEnableConsoleLogging = false;
		#endif
			
			yumeSDK.YuMeSDK_Init(adParams, this);
			retVal = true;
		} catch (YuMeException e) {
			DBG_EXCEPTION(e);
			ERR("ADSERVER", e.getMessage());
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
		return retVal;
	} ;
	
	/**
	 * De-initializes the YuMe SDK.
	 * @param None. 
	 * @return None.
	 */	
	public void deInitYuMeSDK() {
		try {
			yumeSDK.YuMeSDK_DeInit();
		} catch (YuMeException e) {
			DBG_EXCEPTION(e);
			ERR("ADSERVER", e.getMessage());
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
		yumeSDK = null;
	}
	
	/**
	 * Prefetches an ad.
	 * @param None. 
	 * @return true, if initAd is success, else false.
	 */		
	public boolean prefetchAd() {
		YuMeAdBlockType adBlockType = YuMeAdBlockType.YUME_ADBLOCKTYPE_PREROLL;
		boolean retVal = false;
		try {
			yumeSDK.YuMeSDK_InitAd(adBlockType);
			retVal = true;
			INFO("ADSERVER", "Prefetching Preroll Ad...");
		} catch (YuMeException e) {
			DBG_EXCEPTION(e);
			showError(e.getMessage());
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
		return retVal;
	}
	
	/**
	 * Requests the YuMe SDK to prefetch an ad display.
	 * @param None. 
	 * @return true, if ShowAd is success, else false.
	 */
	public boolean displayAd() {
		YuMeAdBlockType adBlockType = YuMeAdBlockType.YUME_ADBLOCKTYPE_PREROLL;
		boolean retVal = false;
		try {
			yumeSDK.YuMeSDK_ShowAd(adBlockType);
			retVal = true;
		} catch (YuMeException e) {
			DBG_EXCEPTION(e);
			showError(e.getMessage());
		} catch (Exception e){
			DBG_EXCEPTION(e);
		}
		return retVal;
	}
	
	/**
	 * Sets the parent view in YuMe SDK.
	 * @param fLayout - handle of the frame layout that holds the video view. 
	 * @param vView - handle of the video view. 
	 * @param mController - handle of the media controller.  
	 * @return None.
	 */	
	public void setParentView(FrameLayout fLayout, VideoView vView, MediaController mController) {
		try {
			yumeSDK.YuMeSDK_SetParentView(fLayout, vView, mController);
		} catch (YuMeException e) {
			DBG_EXCEPTION(e);
			ERR("ADSERVER", e.getMessage());
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		} 
	} ;
	
	/**
	 * Notifies the Back key pressed event to YuMe SDK.
	 * @param None. 
	 * @return None.
	 */		
	public void backKeyPressed() {
		try {
			yumeSDK.YuMeSDK_BackKeyPressed();
		} catch (YuMeException e) {
			DBG_EXCEPTION(e);			
			ERR("ADSERVER", e.getMessage());
		} catch (Exception e) {
			DBG_EXCEPTION(e);
		}
	} ;	
	
	/**
	 * Displays the info message.
	 * @param infoMsg - info message. 
	 * @return None.
	 */	
	public void showInfo(String infoMsg) {
		INFO("ADSERVER", infoMsg);
	}
	
	/**
	 * Displays the error message.
	 * @param errMsg - error message.
	 * @return None.
	 */	
	public void showError(String errMsg) {
		ERR("ADSERVER", errMsg);
	}
}
