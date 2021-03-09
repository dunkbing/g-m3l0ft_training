package APP_PACKAGE.PackageUtils;

import android.view.Surface;

public class JNIBridge
{
    /*****Internet Status Functions******/
    public static native void SetConnectionType(int connectionType);
    /**End Internet Status Functions*****/

	/*****User Location Functions******/
    public static native void SetUserLocation(int status, double latitude, double longitude, float accuracy, String time);
    /**User Location Functions*****/

    /*************3D Native******************/
	public static native void NativeInit();
    public static native void NativeOnResume();
    public static native void NativeOnPause();
    public static native void NativeSurfaceChanged(Surface surface, int w, int h);
	
    /**************Misc*****************/
	
#if USE_WELCOME_SCREEN
	// Method called by goto's in welcome screen, notifying the native code
	public native static void NativeSplashScreenFunc(String gotoTag);
#endif // USE_WELCOME_SCREEN

#if USE_IGP_FREEMIUM
	// Method called when IGP is closed
	public native static void NativeOnIGPClosed();
	
#if USE_IGP_REWARDS 
	public native static void NativeSetReward(int amount, String type, String message);
#endif
#endif // USE_IGP_FREEMIUM

    /**************Input***********/
    public static native void NativeOnTouch(int eventID, float x, float y, int pointerId);
	public static native void NativeKeyAction(int keyCode, boolean pressed);
#if USE_VIRTUAL_KEYBOARD
	// Method called by virtual keyboard when used
	public native static void NativeSendKeyboardData(String textTyped);
#endif // USE_VIRTUAL_KEYBOARD
	
	/**************Battery***********/
	public static native void SetBatteryInfo(boolean isCharging, boolean usbCharge, boolean acCharge, int percent);
	public static native void NotifyLowBattery();
	
    /*******************************/
	
	/**************GoogleAdId***********/
#if ENABLE_GOOGLE_AD_ID
	public static native void SetGoogleAdIdValues(int status, String googleAdId);
#endif
	/***********************************/
}
