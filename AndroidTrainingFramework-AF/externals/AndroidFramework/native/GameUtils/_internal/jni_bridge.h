#ifndef __JNI_BRIDGE_ACP_H__
#define __JNI_BRIDGE_ACP_H__

#include "config_Android.h"

extern "C" 
{
    // Network actions
	JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_SetConnectionType)  (JNIEnv*  env, jobject obj, jint connType);
    
	// Network actions
	JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_SetUserLocation)  (JNIEnv*  env, jobject obj, jint usrLocStatus, jdouble usrLocationLatitude, jdouble usrLocationLongitude, jfloat usrLocationAccuracy, jstring usrLocationTime);
	
	//Game start action:
	JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeInit)(JNIEnv* jenv, jobject obj);

	// Pause/Resume actions
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnResume)(JNIEnv* jenv, jobject obj);
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnPause)(JNIEnv* jenv, jobject obj);    

    // Surface actions
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeSurfaceChanged)(JNIEnv* jenv, jobject obj, jobject surface, jint w, jint h);

    // Input actions
	JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnTouch)(JNIEnv* jenv, jobject obj, jint eventId, jfloat x, jfloat y, jint pointerId);
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeKeyAction)(JNIEnv* jenv, jobject obj, jint keyCode, jboolean pressed);

    // Virtual keyboard action
#if USE_VIRTUAL_KEYBOARD
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeSendKeyboardData)(JNIEnv* env, jclass plugin, jstring textTyped);
#endif // USE_VIRTUAL_KEYBOARD

    // Splash Screen Custom Goto
#if USE_WELCOME_SCREEN
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeSplashScreenFunc)(JNIEnv* env, jclass plugin, jstring goToTag);
#endif // USE_WELCOME_SCREEN

    // On IGP Closed
#if USE_IGP_FREEMIUM
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnIGPClosed)(JNIEnv* env, jclass plugin);
	
#if USE_IGP_REWARDS 
	JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeSetReward)(JNIEnv*  env, jclass thiz, jint amount, jstring type, jstring message);
#endif
#endif // USE_IGP_FREEMIUM

    // Battery actions
    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_SetBatteryInfo)
        (JNIEnv* jenv, jobject obj, jboolean isCharging, jboolean usbCharge, jboolean acCharge, jint percent);

    JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NotifyLowBattery)(JNIEnv* jenv, jobject obj);
	
	
	//Begin Johny
	JNIEXPORT void JNICALL JNI_FUNCTION(OpenGLRenderer_nativeRender) (JNIEnv * env, jobject obj);
	JNIEXPORT void JNICALL JNI_FUNCTION(PushNotification_SimplifiedAndroidUtils_nativeSendPN) (JNIEnv * env, jobject obj);
	//End johny
	
	//end init funcs
}

#endif //__JNI_BRIDGE_ACP_H__
