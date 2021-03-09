#if USE_HID_CONTROLLER
package APP_PACKAGE.GLUtils.controller;


public class NativeBridgeHIDControllers 
{
	/**
	 * This class provides access to JNI. This is for internal usage only. Do not call it on your own! 
	 */
	
	public static void NativeListenerRegistered(int seconds)
	{
		StandardHIDController.StartControllerListener(seconds);
	}
	
	public static void NativeListenerUnRegistered()
	{
		StandardHIDController.StopControllerListener();
	}
	
	public static native void NativeControllerConnected(String name);
	public static native void NativeControllerDisconnected();
	
	public static native void NativeHandleInputEvents(int InternalValue, double Value);
	
}
#endif //#if USE_HID_CONTROLLER