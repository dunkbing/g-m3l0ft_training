#ifndef __JNI_BRIDGE_PN_H__
#define __JNI_BRIDGE_PN_H__

#include <jni.h>
#include "config_Android.h"

extern "C" 
{
	JNIEXPORT void JNICALL JNI_FUNCTION(PushNotification_SimplifiedAndroidUtils_nativeSendPNData) (JNIEnv* env, jclass thiz, jstring notificationData);
	JNIEXPORT void JNICALL JNI_FUNCTION(PushNotification_SimplifiedAndroidUtils_nativeSendRegistrationData) (JNIEnv* env, jclass thiz, jstring regData);	
}


#endif //__JNI_BRIDGE_PN_H__