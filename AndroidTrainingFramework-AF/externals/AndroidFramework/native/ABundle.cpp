#include "ABundle.h"
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "config_Android.h"
#include "GameUtils\ScopeGetEnv.h"
#include "GameUtils\package_utils.h"

#if !(RELEASE_VERSION)
#define DEBUG_ENABLE
#endif

#define TAG "ABundle"
#include "ADebug.h"


using namespace acp_utils;

///////////////////////////////////////////////////////////////////////////////////
// ABundle Interface
///////////////////////////////////////////////////////////////////////////////////
class ABundle {
	public:
		static void			PutString(const char* key, const char* value, jobject bundle);
		static const char* 	ReadString(const char* key, jobject bundle);
		static jbyteArray 	ReadBArray(const char* key, jobject bundle);
		static void 		PutBArray(const char* key, jbyteArray value, jobject bundle);
		static int 			ReadInt(const char* key, jobject bundle);
		static void			PutInt(const char* key, int value, jobject bundle);
		static long long 	ReadLong(const char* key, jobject bundle);
		static void			PutLong(const char* key, long long value, jobject bundle);
		static bool 	 	ReadBool(const char* key, jobject bundle);
		static void			PutBool(const char* key, bool value, jobject bundle);
		static jboolean 	ContainsKey(const char* key, jobject bundle);
		static jobject 		New();
		static void 		Clear(jobject bundle);
		
		static void 		SetJniVars();
	private:
		static jstring 		charToString(const char* str);
		
		static jclass cBundle;
		static jmethodID mInit;
		static jmethodID mPutString;
		static jmethodID mGetString;
		static jmethodID mGetByteArrays;
		static jmethodID mPutByteArrays;
		static jmethodID mGetInt;
		static jmethodID mPutInt;
		static jmethodID mGetLong;
		static jmethodID mPutLong;
		static jmethodID mGetBool;
		static jmethodID mPutBool;
		static jmethodID mContains;
		static jmethodID mClear;
};


///////////////////////////////////////////////////////////////////////////////////
// ABundle Interface
///////////////////////////////////////////////////////////////////////////////////
jclass		ABundle::cBundle		= 0;
jmethodID	ABundle::mInit			= 0;
jmethodID	ABundle::mPutString		= 0;
jmethodID	ABundle::mGetString		= 0;
jmethodID	ABundle::mGetByteArrays	= 0;
jmethodID	ABundle::mPutByteArrays	= 0;
jmethodID	ABundle::mGetInt		= 0;
jmethodID	ABundle::mPutInt		= 0;
jmethodID	ABundle::mGetLong		= 0;
jmethodID	ABundle::mPutLong		= 0;
jmethodID	ABundle::mGetBool		= 0;
jmethodID	ABundle::mPutBool		= 0;
jmethodID	ABundle::mContains		= 0;
jmethodID	ABundle::mClear			= 0;


#ifdef  __cplusplus
extern "C" {
#endif

///////////////////////////////////////////////////////////////////////////////////
// ABundle Interface
///////////////////////////////////////////////////////////////////////////////////
	
	void ABundle_PutString(const char* key, const char* value, jobject bundle)
	{
		ABundle::PutString(key, value, bundle);
	}
	
	const char* ABundle_ReadString(const char* key, jobject bundle)
	{
		return ABundle::ReadString(key, bundle);
	}
	
	jbyteArray 	ABundle_ReadBArray(const char* key, jobject bundle)
	{
		return ABundle::ReadBArray(key, bundle);
	}
	
	void ABundle_PutBArray(const char* key, jbyteArray value, jobject bundle)
	{
		ABundle::PutBArray(key, value, bundle);
	}

	int ABundle_ReadInt(const char* key, jobject bundle)
	{
		return ABundle::ReadInt(key, bundle);
	}
	void ABundle_PutInt(const char* key, int value, jobject bundle)
	{
		ABundle::PutInt(key, value, bundle);
	}
	long long ABundle_ReadLong(const char* key, jobject bundle)
	{
		return ABundle::ReadLong(key, bundle);
	}
	void ABundle_PutLong(const char* key, long long value, jobject bundle)
	{
		ABundle::PutLong(key, value, bundle);
	}
	bool ABundle_ReadBool(const char* key, jobject bundle)
	{
		return ABundle::ReadBool(key, bundle);
	}
	void ABundle_PutBool(const char* key, bool value, jobject bundle)
	{
		ABundle::PutBool(key, value, bundle);
	}

	jboolean ABundle_ContainsKey(const char* key, jobject bundle)
	{
		return ABundle::ContainsKey(key, bundle);
	}
	jobject ABundle_New()
	{
		return ABundle::New();
	}
	void ABundle_Clear(jobject bundle)
	{
		ABundle::Clear(bundle);
	}
	
#ifdef  __cplusplus
}
#endif


///////////////////////////////////////////////////////////////////////////////////
// ABundle Class
///////////////////////////////////////////////////////////////////////////////////
void ABundle::SetJniVars()
{
	if (cBundle == 0)
	{
		DBG_FN();
		JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
		
		
		//Class Bundle
		cBundle = acp_utils::api::PackageUtils::GetClass("android/os/Bundle");
			
		mInit 			= (mEnv)->GetMethodID(cBundle, "<init>", 		"()V");
		mPutString 		= (mEnv)->GetMethodID(cBundle, "putString", 	"(Ljava/lang/String;Ljava/lang/String;)V");
		mGetString 		= (mEnv)->GetMethodID(cBundle, "getString", 	"(Ljava/lang/String;)Ljava/lang/String;");
		mGetInt 		= (mEnv)->GetMethodID(cBundle, "getInt",		"(Ljava/lang/String;)I");
		mPutInt 		= (mEnv)->GetMethodID(cBundle, "putInt",		"(Ljava/lang/String;I)V");
		mGetLong 		= (mEnv)->GetMethodID(cBundle, "getLong",		"(Ljava/lang/String;)J");
		mPutLong 		= (mEnv)->GetMethodID(cBundle, "putLong",		"(Ljava/lang/String;J)V");
		mGetBool 		= (mEnv)->GetMethodID(cBundle, "getBoolean",	"(Ljava/lang/String;)Z");
		mPutBool 		= (mEnv)->GetMethodID(cBundle, "putBoolean",	"(Ljava/lang/String;Z)V");
		mContains 		= (mEnv)->GetMethodID(cBundle, "containsKey",	"(Ljava/lang/String;)Z");
		mClear 			= (mEnv)->GetMethodID(cBundle, "clear", 		"()V");
		mGetByteArrays 	= (mEnv)->GetMethodID(cBundle, "getByteArray", 	"(Ljava/lang/String;)[B");
		mPutByteArrays 	= (mEnv)->GetMethodID(cBundle, "putByteArray",	"(Ljava/lang/String;[B)V");
		DBG_FNE();	
	}

	acp_utils::api::PackageUtils::Jni_CheckForExceptions();//av todo: remove this call before live
}

void ABundle::PutString(const char* key, const char* value, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	jstring skey 	= charToString(key);
	jstring svalue 	= charToString(value);
	(mEnv)->CallVoidMethod(bundle, mPutString, skey, svalue);	
	(mEnv)->DeleteLocalRef(skey);
	(mEnv)->DeleteLocalRef(svalue);
}

const char* ABundle::ReadString(const char* key, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	jstring string = charToString(key);
	jstring result = (jstring)(mEnv)->CallObjectMethod(bundle, mGetString, string);	
	const char* out = mEnv->GetStringUTFChars(result, 0);	
	(mEnv)->DeleteLocalRef(string);
	return out;
}

jbyteArray ABundle::ReadBArray(const char* key, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	jstring string = charToString(key);
	jbyteArray result = (jbyteArray)(mEnv)->CallObjectMethod(bundle, mGetByteArrays, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

void ABundle::PutBArray(const char* key, jbyteArray value, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	jstring skey = charToString(key);
	(mEnv)->CallVoidMethod(bundle, mPutByteArrays, skey, value);
	(mEnv)->DeleteLocalRef(skey);
}

int ABundle::ReadInt(const char* key, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	if (ContainsKey(key, bundle) == JNI_FALSE)
	{
		return -1;
	}
	jstring string = charToString(key);
	int result = (mEnv)->CallIntMethod(bundle, mGetInt, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

void ABundle::PutInt(const char* key, int value, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);

	jstring string = charToString(key);
	(mEnv)->CallVoidMethod(bundle, mPutInt, string, value);
	(mEnv)->DeleteLocalRef(string);
}

long long ABundle::ReadLong(const char* key, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	if (ContainsKey(key, bundle) == JNI_FALSE)
	{
		return -1;
	}
	jstring string = charToString(key);
	long long result = (mEnv)->CallLongMethod(bundle, mGetLong, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

void ABundle::PutLong(const char* key, long long value, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);

	jstring string = charToString(key);
	(mEnv)->CallVoidMethod(bundle, mPutLong, string, value);
	(mEnv)->DeleteLocalRef(string);
}

bool ABundle::ReadBool(const char* key, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	if (ContainsKey(key, bundle) == JNI_FALSE)
	{
		return false;
	}
	jstring string = charToString(key);
	bool result = (mEnv)->CallBooleanMethod(bundle, mGetBool, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

void ABundle::PutBool(const char* key, bool value, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);

	jstring string = charToString(key);
	(mEnv)->CallVoidMethod(bundle, mPutBool, string, value);
	(mEnv)->DeleteLocalRef(string);
}

jboolean ABundle::ContainsKey(const char* key, jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	jstring string = charToString(key);
	jboolean result = (mEnv)->CallBooleanMethod(bundle, mContains, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

jobject ABundle::New()
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	return (jobject)(mEnv)->NewObject(cBundle, mInit);
}

void ABundle::Clear(jobject bundle)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	(mEnv)->CallVoidMethod(bundle, mClear);
}

jstring ABundle::charToString(const char* str)
{
	SetJniVars();

	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	return (mEnv)->NewStringUTF(str);
}
