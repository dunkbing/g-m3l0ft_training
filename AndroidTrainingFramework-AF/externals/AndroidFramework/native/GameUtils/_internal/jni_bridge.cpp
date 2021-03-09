/* Standard includes */
#include <stdlib.h>
#include <jni.h>
#include <android/native_window_jni.h>

/* Local includes */

#include "config_Android.h"

#include "logger.h"
#include "../package_utils.h"
#include "../GameFunctionsToImplement.h"
#include "internal.h"

#include "JNI_Bridge.h" ///< Included here because it needed declarations from package_utils.h <-- BAD



#if USE_VIRTUAL_KEYBOARD
#include "../Keyboard/virtual_keyboard.h"
#endif //USE_VIRTUAL_KEYBOARD

#include "render/videoDriver.h"
#include "PushNotification/simplified_pn.h"

#include "game.h"

JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *pvt )
{
	acp_utils::acp_internal::Internal::SetVm(vm);

	JNIEnv* pEnv;

	vm->GetEnv((void**)&pEnv, JNI_VERSION_1_6);

	acp_utils::acp_internal::Internal::LoadClasses(pEnv);

	acp_utils::api::PackageUtils::Jni_CheckForExceptions();
	
	return JNI_VERSION_1_6;
}


JNIEXPORT void JNICALL 
JNI_FUNCTION(PackageUtils_JNIBridge_SetConnectionType) (JNIEnv*  env, jobject obj, jint connType)

{
	LOG_DBG("set connection %d", connType);
	acp_utils::acp_internal::Internal::SetConnection(static_cast<acp_utils::helpers::ConnectionType>(connType));
}

JNIEXPORT void JNICALL 
JNI_FUNCTION(PackageUtils_JNIBridge_SetUserLocation) (JNIEnv*  env, jobject obj, jint usrLocStatus, jdouble usrLocationLatitude, jdouble usrLocationLongitude, jfloat usrLocationAccuracy, jstring usrLocationTime)

{
    JNIEnv* pEnv = NULL;
    acp_utils::ScopeGetEnv sta(pEnv);
	
	LOG_DBG("set UserLocation %d", usrLocStatus);
	acp_utils::helpers::UserLocation userLocation;
	userLocation.status = static_cast<acp_utils::helpers::EUserLocationStatus>(usrLocStatus);
	
    userLocation.latitude = usrLocationLatitude;
    userLocation.longitude = usrLocationLongitude;
    userLocation.accuracy = usrLocationAccuracy;
	
	acp_utils::ScopeStringChars tim(pEnv, usrLocationTime);
    userLocation.time = tim.Get();

	acp_utils::acp_internal::Internal::SetUserLocation(userLocation);
}

JNIEXPORT void JNICALL 
JNI_FUNCTION(PackageUtils_JNIBridge_NativeInit)(JNIEnv* jenv, jobject obj)
{
    acp_utils::acp_internal::Internal::Init();

	//OnGameInit();
}

JNIEXPORT void JNICALL
JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnResume)(JNIEnv* jenv, jobject obj)
{
	acp_utils::acp_internal::Internal::PreGameResume();
	
	//OnGameResume();

	acp_utils::acp_internal::Internal::PostGameResume();

}

JNIEXPORT void JNICALL
JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnPause) (JNIEnv* jenv, jobject obj)
{
	acp_utils::acp_internal::Internal::PreGamePause();
	
	//OnGamePause();

	acp_utils::acp_internal::Internal::PostGamePause();
}

JNIEXPORT void JNICALL 
JNI_FUNCTION(PackageUtils_JNIBridge_NativeSurfaceChanged)(JNIEnv* jenv, jobject obj, jobject surface, jint w, jint h)
{
	if (surface != 0) 
	{
		acp_utils::acp_internal::Internal::SetWindow(ANativeWindow_fromSurface(jenv, surface), w, h);
		//OnWindowStateChange(static_cast<ANativeWindow*>(acp_utils::api::PackageUtils::GetNativeWindow()));
    } 
	else 
	{
		ANativeWindow_release(static_cast<ANativeWindow*>(acp_utils::api::PackageUtils::GetNativeWindow()));
		acp_utils::acp_internal::Internal::SetWindow(0, 0, 0);
		//OnWindowStateChange(0);
    }
}

JNIEXPORT void JNICALL 
JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnTouch)(JNIEnv* jenv, jobject obj, jint eventId, jfloat x, jfloat y, jint pointerId)
{
	//OnGameTouchEvent(eventId, x, y, pointerId);
}

JNIEXPORT void JNICALL
JNI_FUNCTION(PackageUtils_JNIBridge_NativeKeyAction)(JNIEnv* jenv, jobject obj, jint keyCode, jboolean pressed)
{
    //NativeOnKeyAction(keyCode, pressed);
}



///////////////////////////////////////////////
// Virtual Keyboard -- Migrated from GameUtils
#if USE_VIRTUAL_KEYBOARD
JNIEXPORT void JNICALL 
JNI_FUNCTION(PackageUtils_JNIBridge_NativeSendKeyboardData)(JNIEnv* env, jclass plugin, jstring textTyped)
{
    if(acp_utils::modules::VirtualKeyboard::s_vKeyboardCB)
    {
        JNIEnv* pEnv = NULL;
        acp_utils::ScopeGetEnv sta(pEnv);

        acp_utils::ScopeStringChars text(pEnv, textTyped);
        acp_utils::modules::VirtualKeyboard::s_vKeyboardCB(text.Get());
    }
    else
    {
        LOG_ERROR("Virtual keyboard callback is NULL");
    }
}
#endif // USE_VIRTUAL_KEYBOARD



///////////////////////////////////////////////
// Welcome Screen -- Splash Screen Function
//           passing the custom "goto"
#if USE_WELCOME_SCREEN
JNIEXPORT void JNICALL 
JNI_FUNCTION(PackageUtils_JNIBridge_NativeSplashScreenFunc)(JNIEnv* env, jclass plugin, jstring goToTag)
{
	acp_utils::ScopeStringChars ws_str(env, goToTag);
	
	splashScreenFunc( ws_str.Get() );
}
#endif // USE_WELCOME_SCREEN

///////////////////////////////////////////////
// IGP - Notifies the native code that the IGP
//       activity was closed.
#if USE_IGP_FREEMIUM
JNIEXPORT void JNICALL
JNI_FUNCTION(PackageUtils_JNIBridge_NativeOnIGPClosed)(JNIEnv* env, jclass plugin)
{
    //OnIGPClosed();
}
	
#if USE_IGP_REWARDS 
JNIEXPORT void JNICALL JNI_FUNCTION(PackageUtils_JNIBridge_NativeSetReward)(JNIEnv*  env, jclass thiz, jint amount, jstring type, jstring message)
{
	acp_utils::ScopeStringChars nativeType(env, type);
	acp_utils::ScopeStringChars nativeMsg(env, message);
	OnIgpReward(amount, nativeType.Get(), nativeMsg.Get());
}
#endif

#endif // USE_IGP_FREEMIUM

///////////////////////////////////////////////
// Battery Actions
JNIEXPORT void JNICALL
JNI_FUNCTION(PackageUtils_JNIBridge_SetBatteryInfo)(JNIEnv* jenv, jobject obj, jboolean isCharging, jboolean usbCharge, jboolean acCharge, jint percent)
{
    acp_utils::helpers::BatteryInfo bi;
    bi.sIsCharging    = isCharging;
    bi.sUsbCharge     = usbCharge;
    bi.sACCharge      = acCharge;
    bi.sBatteryStatus = percent;

    acp_utils::acp_internal::Internal::SetBatteryInfo(bi);
}

JNIEXPORT void JNICALL
JNI_FUNCTION(PackageUtils_JNIBridge_NotifyLowBattery)(JNIEnv* jenv, jobject obj)
{
    //NativeLowBattery();
}

JNIEXPORT void JNICALL
JNI_FUNCTION(OpenGLRenderer_nativeRender) (JNIEnv * env, jobject obj)
{
	Game::GetInstance()->Draw();
	/*VideoDriver::GetInstance()->CleanScreen();
	VideoDriver::GetInstance()->DrawCircle(100.0f, 100.0f, 100.0f);
	VideoDriver::GetInstance()->FillRect(0, 0, 100, 100);*/
}
