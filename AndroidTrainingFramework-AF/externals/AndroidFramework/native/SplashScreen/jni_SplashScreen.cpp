#include "config_Android.h"
#include <jni.h>
#include <string>

#include <android/log.h>

#include "GameUtils/package_utils.h"


namespace splash_screen
{
	static jmethodID sMethodDowloadOfflineWS = NULL;
	static jmethodID sMethodShowOfflineWS = NULL;
	static jmethodID sMethodIsOfflineWSDownloaded = NULL;
	static jmethodID sMethodIsWSActive = NULL;
	static jmethodID sMethodDeleteOfflineWS = NULL;
	static jmethodID sMethodSetWSLanguage = NULL;
	static jclass mClassWelcomeScreen = NULL;


	static void		SetJniVars()
	{
		if(mClassWelcomeScreen == NULL)
		{
			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);

			mClassWelcomeScreen = acp_utils::api::PackageUtils::GetClass("/SplashScreenActivity");

			sMethodDowloadOfflineWS = pEnv->GetStaticMethodID (mClassWelcomeScreen, "downloadWS", "(Ljava/lang/String;)V");
			sMethodShowOfflineWS = pEnv->GetStaticMethodID (mClassWelcomeScreen, "showLocalWS", "(Ljava/lang/String;)V");
			sMethodIsOfflineWSDownloaded = pEnv->GetStaticMethodID (mClassWelcomeScreen, "isWSReady", "(Ljava/lang/String;)I");
			sMethodIsWSActive = pEnv->GetStaticMethodID (mClassWelcomeScreen, "isActive", "()I");
			sMethodDeleteOfflineWS = pEnv->GetStaticMethodID (mClassWelcomeScreen, "deleteWS", "(Ljava/lang/String;)V");
			sMethodSetWSLanguage = pEnv->GetStaticMethodID (mClassWelcomeScreen, "SetWSLanguage", "(I)V");
		}
	}
};



#ifdef  __cplusplus
extern "C"
#endif
void androidDownloadOfflineWS(const std::string& popupIds)
{
	splash_screen::SetJniVars();
	
	JNIEnv* pEnv = NULL;
	acp_utils::ScopeGetEnv st(pEnv);

	jstring param = pEnv->NewStringUTF(popupIds.c_str());
	pEnv->CallStaticVoidMethod(splash_screen::mClassWelcomeScreen, splash_screen::sMethodDowloadOfflineWS, param);                
}

#ifdef  __cplusplus
extern "C"
#endif
int androidIsOfflineWSDownloaded(const std::string& popupId)
{
	splash_screen::SetJniVars();
	
	JNIEnv* pEnv = NULL;
	acp_utils::ScopeGetEnv st(pEnv);
	
	jstring param = pEnv->NewStringUTF(popupId.c_str());
	int result = pEnv->CallStaticIntMethod(splash_screen::mClassWelcomeScreen, splash_screen::sMethodIsOfflineWSDownloaded, param);                
	return result;
}

#ifdef  __cplusplus
extern "C"
#endif
int androidIsWSActive()
{
	splash_screen::SetJniVars();
	
	JNIEnv* pEnv = NULL;
	acp_utils::ScopeGetEnv st(pEnv);
	
	int result = pEnv->CallStaticIntMethod(splash_screen::mClassWelcomeScreen, splash_screen::sMethodIsWSActive);                
	return result;
}

#ifdef  __cplusplus
extern "C"
#endif
void androidShowOfflineWS(const std::string& popupId)
{
	splash_screen::SetJniVars();
	
	JNIEnv* pEnv = NULL;
	acp_utils::ScopeGetEnv st(pEnv);
	
	jstring param = pEnv->NewStringUTF(popupId.c_str());
	pEnv->CallStaticVoidMethod(splash_screen::mClassWelcomeScreen, splash_screen::sMethodShowOfflineWS, param);     
}

#ifdef  __cplusplus
extern "C"
#endif
void androidDeleteOfflineWS(const std::string& popupId)
{
	splash_screen::SetJniVars();
	
	JNIEnv* pEnv = NULL;
	acp_utils::ScopeGetEnv st(pEnv);
	
	jstring param = pEnv->NewStringUTF(popupId.c_str());
	pEnv->CallStaticVoidMethod(splash_screen::mClassWelcomeScreen, splash_screen::sMethodDeleteOfflineWS, param);     
}

#ifdef  __cplusplus
extern "C"
#endif
void androidSetWSLanguage(int langIndex)
{
	splash_screen::SetJniVars();
	
	JNIEnv* pEnv = NULL;
	acp_utils::ScopeGetEnv st(pEnv);
	
	pEnv->CallStaticVoidMethod(splash_screen::mClassWelcomeScreen, splash_screen::sMethodSetWSLanguage, langIndex);     
}

#if USE_WELCOME_SCREEN_CRM
extern void splashScreenFuncGlot(const char* name);

#ifdef  __cplusplus
extern "C"
#endif
JNIEXPORT void JNICALL JNI_FUNCTION(SplashScreenActivity_splashScreenFuncGLOT) (JNIEnv*  env, jobject thiz, jstring url)
{
	acp_utils::ScopeStringChars ws_glot_str(env, url);
	
	splashScreenFuncGlot(ws_glot_str.Get());
}
#endif