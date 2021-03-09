#include "gdrm_app.h"
#include <stdio.h>

extern void drmDebugLog(const char* msg, ...);

jobject m_TelephonyManager;
char  sd_folder[256];

JNIEXPORT void JNICALL JNI_FUNCTION(installer_GameInstaller_initNative)(JNIEnv* env, jobject thiz)
{
	drmDebugLog("entering native code ");
	mEnv = env;
	drmDebugLog("getting class ");
	LicenseCheck	= (jclass)(*mEnv)->NewGlobalRef(mEnv, thiz);
	drmDebugLog("getting method id ");
	startGame = (*mEnv)->GetStaticMethodID (mEnv, LicenseCheck, "startGame", "()V");
	getSDFolder = (*mEnv)->GetStaticMethodID (mEnv, LicenseCheck, "getSDFolder", "()Ljava/lang/String;");
	drmDebugLog("finish ");
};

JNIEXPORT void JNICALL JNI_FUNCTION(installer_GameInstaller_nativeStart)(JNIEnv* env, jobject thiz)
{
  nativeStart();
}


JNIEXPORT void JNI_FUNCTION(installer_GameInstaller_getPublicKey)(JNIEnv* env,jobject thiz)
{
  drmDebugLog("getting key");
  // assign a lock pointer so the application will crash if it will try to access this function in another order then the one we want
  lockPointer1=(int*) malloc(sizeof(int));
  lockPointer1[0]=1;
//   return (*env)->NewStringUTF(env, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArjzJYaersEzHcAbdWeui2v4UThDbF59uWcEAxTbRxmL97B/9JXyVl0Ta0b+owRJfgTnOwoZNe4IOAn+0rrDudGE68f4tQjH18EQKc+3jzxvjXOe0RicWr91bVpfmYfBlE1Fqc+0aevoXAdCqZz6PMNLuScD7P7daVgT+tHXSfrKBLbjkaPqab1skyIZUK3b2QF+3u8asaAPl+gUKQSHnp9Cc0BX1LpUPcteCyeNZG214ZSRT339WJ/7+dpzQo3G7DJU3N9CXfh+3y/6DLE1S3+1iC1dOAHAnCm84OQ0F3axlmiv428zvaYW88+SalCDzh2XB0k3+XmE4MRtJkFmL4QIDAQAB");
}

void nativeStart()
{	
	drmDebugLog(" start lock test");
	
	if(lockPointer3 == 0)
	{
		exit(0);
		return;
	}
	
	int size = lockPointer3[0]; // generate crash if lockPointer3 is not allocated
	lockPointer4=malloc(size*sizeof(int));
	lockPointer4[0]=1;
	drmDebugLog(" calling method");
	(*mEnv)->CallStaticVoidMethod(mEnv, LicenseCheck, startGame);
	drmDebugLog("done ");
}

void nativeGetSdFolderPath()
{
	if (getSDFolder)
	{
		jstring newString = (*mEnv)->CallStaticObjectMethod(mEnv, LicenseCheck, getSDFolder);
		const jbyte *str;
		str = (*mEnv)->GetStringUTFChars(mEnv, newString, 0);
		if (str == 0)
			return ; /* OutOfMemoryError already thrown */
		strcpy(sd_folder, str);
		(*mEnv)->ReleaseStringUTFChars(mEnv, newString, str);
	}
}