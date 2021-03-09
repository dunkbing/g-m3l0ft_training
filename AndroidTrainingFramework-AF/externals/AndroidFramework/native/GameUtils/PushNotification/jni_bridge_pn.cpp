#include "config_Android.h"

#if SIMPLIFIED_PN

#include "jni_bridge_pn.h"
#include "simplified_pn.h"

#include "..\ScopeGetEnv.h"
#include "..\GameFunctionsToImplement.h"

#include <string>

extern "C" 
{

	JNIEXPORT void JNICALL JNI_FUNCTION(PushNotification_SimplifiedAndroidUtils_nativeSendPNData) (JNIEnv* env, jclass thiz, jstring notificationData)
	{
		JNIEnv* mEnv = 0; acp_utils::ScopeGetEnv st(mEnv);
		jboolean isCopy;
		const char *strNotificationData = mEnv->GetStringUTFChars(notificationData, &isCopy);
		
		std::string stdData;
		if(isCopy == JNI_TRUE)
			stdData = strNotificationData;

		OnPushNotificationResponseCB(stdData);
		(mEnv)->ReleaseStringUTFChars(notificationData, strNotificationData);
	}

	JNIEXPORT void JNICALL JNI_FUNCTION(PushNotification_SimplifiedAndroidUtils_nativeSendRegistrationData) (JNIEnv* env, jclass thiz, jstring regData)
	{
		JNIEnv* mEnv = 0; acp_utils::ScopeGetEnv st(mEnv);
		jboolean isCopy;
		const char *strtoken = mEnv->GetStringUTFChars(regData, &isCopy);
		
		std::string stdData;
		if(isCopy == JNI_TRUE)
			stdData = strtoken;
		
		acp_utils::modules::SimplifiedPN::SendPnCallback(stdData);
		(mEnv)->ReleaseStringUTFChars(regData, strtoken);
	}

}//extern "C"



#endif //SIMPLIFIED_PN
