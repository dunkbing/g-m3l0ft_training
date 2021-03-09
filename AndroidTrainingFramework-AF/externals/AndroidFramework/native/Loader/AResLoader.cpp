#include "config_Android.h"

#include "GameUtils\ScopeGetEnv.h"
#include "AResLoader.h"


//#define DEBUG_ENABLE
#define TAG	"AResLoader"
#include "ADebug.h"


jclass mcResLoader;
jmethodID midGetLength;
jmethodID midGetBytes;

void replaceChars(char* str, const char sc, const char rc);
jstring charToString(const char* str);

using namespace acp_utils;

void AResLoader_Init()
{
	DBG_FN();
	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	
	char cName[256];	memset(cName, '\0', 256);
	strcpy(cName, STR_APP_PACKAGE);	strcat(cName, "/GLUtils/ResLoader");	replaceChars(cName,'.','/');
	
	mcResLoader		= (mEnv)->FindClass(cName);
	if (!mcResLoader)
	{
		ERR("Can't get a reference from the class ResLoader...");	
		DUMP_S(cName);
		exit(0);
	}
	
	mcResLoader 			= (jclass)mEnv->NewGlobalRef(mcResLoader);
	midGetLength 			= mEnv->GetStaticMethodID (mcResLoader, "getLength", "(Ljava/lang/String;)I");	DUMP_D(midGetLength);
	midGetBytes 			= mEnv->GetStaticMethodID (mcResLoader, "getBytes", "(Ljava/lang/String;)[B");	DUMP_D(midGetBytes);
	// getResBytesID 			= mEnv->GetStaticMethodID (mClassGLResLoader, "getBytes", "(Ljava/lang/String;II)[B");
}

unsigned char* AResLoader_GetData (const char* filename)
{
	DBG_FN();
	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	jstring string = charToString(filename);
		jbyteArray mBarray = (jbyteArray)mEnv->CallStaticObjectMethod(mcResLoader, midGetBytes, string);
		int len = mEnv->GetArrayLength(mBarray);
		unsigned char* result = (unsigned char*)(malloc(len));
		mEnv->GetByteArrayRegion(mBarray, 0, len, (jbyte*)result);
		mEnv->DeleteLocalRef(mBarray);
	mEnv->DeleteLocalRef(string);
	return result;
}

// unsigned char* AResLoader_GetData (const char* filename, int offset, int loadSize)
// {
	// DBG_FN();
	// JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	// unsigned char* result;
	// jstring string = mEnv->NewStringUTF(filename);
	// jbyteArray mBarray = (jbyteArray)mEnv->CallStaticObjectMethod(mClassGLResLoader, getResBytesID, string, offset, loadSize);
	// result = (unsigned char*)(malloc(loadSize));
	// mEnv->GetByteArrayRegion(mBarray, 0, loadSize, (jbyte*)result);
	// mEnv->DeleteLocalRef(mBarray);
	// mEnv->DeleteLocalRef(string);
	// return result;
// }

unsigned int AResLoader_GetLength (const char* filename)
{
	DBG_FN();
	JNIEnv* mEnv 	= AndroidOS_GetEnv();
	jstring string 	= charToString(filename);
	
	int len = mEnv->CallStaticIntMethod(mcResLoader, midGetLength, string);
	
	mEnv->DeleteLocalRef(string);
	return len;
}

jstring charToString(const char* str)
{
	JNIEnv* mEnv = NULL; ScopeGetEnv st(mEnv);
	return (mEnv)->NewStringUTF(str);
}

void replaceChars(char* str, const char sc, const char rc)
{
	while(*str!='\0')
	{
		if(*str==sc)
			*str=rc;
		str++;
	}
}
