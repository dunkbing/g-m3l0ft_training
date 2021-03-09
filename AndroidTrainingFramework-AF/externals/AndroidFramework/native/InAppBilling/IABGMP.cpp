#include "config_Android.h"
#if (GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE) && !USE_IN_APP_BILLING_CRM

#include "InAppBilling.h"
#include "InAppBillingConst.h"
#include "GameUtils\ScopeGetEnv.h"

JNIEXPORT jlong JNICALL JNI_FUNCTION(iab_s_gn) (JNIEnv*  env, jclass security)
{
DBG_FN();
	return InAppBilling::generateNonce(security);
DBG_FNE();
}

JNIEXPORT void JNICALL JNI_FUNCTION(iab_s_rn) (JNIEnv*  env, jclass security, jlong nonce)
{
DBG_FN();
	InAppBilling::removeNonce(security, nonce);
DBG_FNE();
}

JNIEXPORT jboolean JNICALL JNI_FUNCTION(iab_s_in) (JNIEnv*  env, jclass security, jlong nonce)
{
DBG_FN();
	return InAppBilling::isNonceKnown(security, nonce);
DBG_FNE();
}

JNIEXPORT jobject JNICALL JNI_FUNCTION(iab_s_gk) (JNIEnv*  env, jclass security, jstring strek)
{
DBG_FN();
	return InAppBilling::gk(security, strek);
DBG_FNE();
}

JNIEXPORT int JNICALL JNI_FUNCTION(iab_s_bq) (JNIEnv*  env, jclass security, jobject oPublicKey, jstring jsSignedData, jstring jsSignature)
{
DBG_FN();
	//return InAppBilling::gk(env, security);
DBG_FNE();
}

jclass 		InAppBilling::cSecurity			= 0;

jclass 		InAppBilling::cSRandom			= 0;
jobject		InAppBilling::oSRandom			= 0;
jmethodID	InAppBilling::mNextLong			= 0;

jclass 		InAppBilling::cMpNonces			= 0;
jobject		InAppBilling::oMpNonces			= 0;
jmethodID	InAppBilling::mNonceAdd			= 0;
jmethodID	InAppBilling::mNonceRemove		= 0;
jmethodID	InAppBilling::mNonceContains	= 0;

jclass 		InAppBilling::cLong				= 0;
jmethodID	InAppBilling::mLongInit			= 0;

jclass		InAppBilling::cB64				= 0;
jmethodID	InAppBilling::mDecode			= 0;
		
jclass		InAppBilling::cKeyFactory		= 0;
jobject		InAppBilling::oKeyFactory		= 0;
jmethodID	InAppBilling::mKFGetInstance	= 0;
jmethodID	InAppBilling::mKFGeneratePublic	= 0;
		
jclass		InAppBilling::cX509EKS			= 0;
jobject		InAppBilling::oX509EKS			= 0;
jmethodID	InAppBilling::mX509Init			= 0;

jobject		InAppBilling::oPK				= 0;

extern "C" void IAB_ReplaceChars(char* str, const char sc, const char rc)
{
	while(*str!='\0')
	{
		if(*str==sc)
			*str=rc;
		str++;
	}
}

void InAppBilling::init_sct(jclass security)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	char methodName[BUFFER_STR_SIZE];
	char methodSign[BUFFER_STR_SIZE];
	char className[BUFFER_STR_SIZE];
	cSecurity	  	= (jclass) (mEnv)->NewGlobalRef(security);
	
//Class SecureRandom
	cSRandom = (mEnv)->FindClass(readChar(className, BUFFER_STR_SIZE, CL_DESC_SECURERANDOM)); //"java/security/SecureRandom"
	if (!cSRandom)
	{
		DBG("Can't get a reference from the class SecureRandom...");
		return;
	}
    cSRandom 		= (jclass)(mEnv)->NewGlobalRef(cSRandom);
	jmethodID mid 	= (mEnv)->GetMethodID(cSRandom, readChar(methodName, BUFFER_STR_SIZE, MD_NME_INIT), //"<init>"
												   readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_NO_ARGS_VOID)); //"()V"
	oSRandom 		= (jobject) (mEnv)->NewObject(cSRandom, mid);
	oSRandom 		= (jobject) (mEnv)->NewGlobalRef(oSRandom);
	mNextLong 		= (mEnv)->GetMethodID(cSRandom, readChar(methodName, BUFFER_STR_SIZE, MD_NME_NEXT_LONG), //"nextLong"
												   readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_NEXT_LONG)); //"()J"
//Class SecureRandom

//Class HashSet
	cMpNonces = (mEnv)->FindClass(readChar(className, BUFFER_STR_SIZE, CL_DESC_HASHSET)); //"java/util/HashSet"
	if (!cMpNonces)
	{
		DBG("Can't get a reference from the class HashSet...");
		return;
	}
    cMpNonces 		= (jclass)(mEnv)->NewGlobalRef(cMpNonces);
	//jfieldID fid 	= (mEnv)->GetStaticFieldID(cSecurity, readChar(methodName, BUFFER_STR_SIZE, FD_NME_KNOWN_NONCES), //"sKnownNonces"
	//												   readChar(methodSign, BUFFER_STR_SIZE, FD_SIG_KNOWN_NONCES)); //"Ljava/util/HashSet;"
	//oMpNonces 		= (jobject)(mEnv)->GetStaticObjectField(cSecurity,fid);
	jmethodID mid2 	= (mEnv)->GetMethodID(cMpNonces, readChar(methodName, BUFFER_STR_SIZE, MD_NME_INIT), //"<init>"
												   readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_NO_ARGS_VOID)); //"()V"
	oMpNonces		= (jobject)(mEnv)->NewObject(cMpNonces, mid2);
	oMpNonces 		= (jobject)(mEnv)->NewGlobalRef(oMpNonces);
	
	mNonceAdd 		= (mEnv)->GetMethodID(cMpNonces, readChar(methodName, BUFFER_STR_SIZE, MD_NME_ADD), //"add"
												    readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_A_R_C)); //"(Ljava/lang/Object;)Z"
	mNonceRemove 	= (mEnv)->GetMethodID(cMpNonces, readChar(methodName, BUFFER_STR_SIZE, MD_NME_REMOVE), //"remove"
												    readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_A_R_C)); //"(Ljava/lang/Object;)Z"
	mNonceContains 	= (mEnv)->GetMethodID(cMpNonces, readChar(methodName, BUFFER_STR_SIZE, MD_NME_CONTAINS), //"contains"
												    readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_A_R_C)); //"(Ljava/lang/Object;)Z"
//Class HashSet

//Class Long
	cLong = (mEnv)->FindClass(readChar(className, BUFFER_STR_SIZE, CL_DESC_LONG)); //"java/lang/Long"
	if (!cLong)
	{
		DBG("Can't get a reference from the class Long...");
		return;
	}
    cLong 		= (jclass)(mEnv)->NewGlobalRef(cLong);
	mLongInit 	= (mEnv)->GetMethodID(cLong, readChar(methodName, BUFFER_STR_SIZE, MD_NME_INIT), //"<init>"
											readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_LONG_INIT)); //"(J)V"
//Class Long
DBG_FNE();
}

jlong InAppBilling::generateNonce(jclass security)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (!oSRandom)
	{
		init_sct(security);
	}
	jlong value = (jlong) (mEnv)->CallLongMethod(oSRandom, mNextLong);
	jobject nonce	= (jobject) (mEnv)->NewObject(cLong, mLongInit, value);
	(mEnv)->CallBooleanMethod(oMpNonces, mNonceAdd, nonce);
	(mEnv)->DeleteLocalRef(nonce);
	return value;
DBG_FNE();	
}

void InAppBilling::removeNonce(jclass security, jlong value)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (oSRandom)
	{
		jobject nonce	= (jobject) (mEnv)->NewObject(cLong, mLongInit, value);
		(mEnv)->CallBooleanMethod(oMpNonces, mNonceRemove, nonce);
		(mEnv)->DeleteLocalRef(nonce);
	}
}

jboolean InAppBilling::isNonceKnown(jclass security, jlong value)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (!oSRandom)
	{
		return JNI_FALSE;
	}
	jobject nonce	= (jobject) (mEnv)->NewObject(cLong, mLongInit, value);
	jboolean res = (mEnv)->CallBooleanMethod(oMpNonces, mNonceContains, nonce);
	(mEnv)->DeleteLocalRef(nonce);
	return res;
}

jobject InAppBilling::gk(jclass security, jstring strek)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (!oSRandom)
	{
		init_sct(security);
	}
	char methodName[BUFFER_STR_SIZE];
	char methodSign[BUFFER_STR_SIZE];
	char className[BUFFER_STR_SIZE];
	jthrowable exc;
	if (!oPK)
	{
		//Class Base64
		int len = strlen(STR_APP_PACKAGE) + strlen(readChar(className, BUFFER_STR_SIZE, IAB_BASE64_CLASS_NAME)) + 1;
		char cn64[len];
		sprintf(cn64,"%s%s",STR_APP_PACKAGE,className);
		IAB_ReplaceChars(cn64,'.','/');
		cB64 = (mEnv)->FindClass(cn64);
		if (!cB64)
		{
			DBG("Can't get a reference from the class Base64...");
			return NULL;
		}
		cB64 	= (jclass)(mEnv)->NewGlobalRef(cB64);
		mDecode = (mEnv)->GetStaticMethodID(cB64, readChar(methodName, BUFFER_STR_SIZE, MD_NME_DECODE), //"decode"
												 readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_DECODE)); //"(Ljava/lang/String;)[B"
		
		jbyteArray decodeKey = (jbyteArray) (mEnv)->CallStaticObjectMethod(cB64, mDecode, strek);
		exc = (mEnv)->ExceptionOccurred();
		if (exc) {
			//jclass newExcCls;
			//(mEnv)->ExceptionDescribe();
			(mEnv)->ExceptionClear();
			int len = strlen(STR_APP_PACKAGE) + strlen(readChar(className, BUFFER_STR_SIZE, IAB_BASE64_EXC_NAME)) + 1;
			char ex64[len];
			sprintf(ex64,"%s%s",STR_APP_PACKAGE,className);
			IAB_ReplaceChars(ex64,'.','/');
			
			JNU_ThrowByName(mEnv, ex64, readChar(className, BUFFER_STR_SIZE, IAB_EXCEPTION_MSG));
			return NULL;
		}
	//Class Base64

	//Class cX509EKS
		cX509EKS = (mEnv)->FindClass(readChar(className, BUFFER_STR_SIZE, CL_DESC_X509ENCODEDKEYSPEC)); //"java/security/spec/X509EncodedKeySpec"
		if (!cX509EKS)
		{
			DBG("Can't get a reference from the class cX509EKS...");
			return NULL;
		}
		cX509EKS 	= (jclass)(mEnv)->NewGlobalRef(cX509EKS);
		mX509Init 	= (mEnv)->GetMethodID(cX509EKS, readChar(methodName, BUFFER_STR_SIZE, MD_NME_INIT), //"<init>"
												   readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_X509EKS_INIT)); //"([B)V"
		oX509EKS	= (jobject) (mEnv)->NewObject(cX509EKS, mX509Init, decodeKey);
	//Class cX509EKS

	//Class KeyFactory
		cKeyFactory = (mEnv)->FindClass(readChar(className, BUFFER_STR_SIZE, CL_DESC_KEYFACTORY)); //"java/security/KeyFactory"
		if (!cKeyFactory)
		{
			DBG("Can't get a reference from the class KeyFactory...");
			return NULL;
		}
		jstring strvalue 	= (mEnv)->NewStringUTF(readChar(className, BUFFER_STR_SIZE, KEY_FACTORY_ALGORITHM)); //"RSA"
		cKeyFactory			= (jclass)(mEnv)->NewGlobalRef(cKeyFactory);
		mKFGetInstance 		= (mEnv)->GetStaticMethodID(cKeyFactory, readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_INSTANCE), //"getInstance"
																	readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_INSTANCE)); //"(Ljava/lang/String;)Ljava/security/KeyFactory;"
		oKeyFactory 		= (jobject) (mEnv)->CallStaticObjectMethod(cKeyFactory, mKFGetInstance, strvalue );
		exc = (mEnv)->ExceptionOccurred();
		if (exc) {
			//jclass newExcCls;
			//(mEnv)->ExceptionDescribe();
			(mEnv)->ExceptionClear();
			JNU_ThrowByName(mEnv, readChar(className, BUFFER_STR_SIZE, NO_ALGORITHM_EXC_NAME), //"java/security/NoSuchAlgorithmException"
								 readChar(methodSign, BUFFER_STR_SIZE, IAB_EXCEPTION_MSG)); //recycle methodSign
			return NULL;
		}
		mKFGeneratePublic 	= (mEnv)->GetStaticMethodID(cSecurity, readChar(methodName, BUFFER_STR_SIZE, MD_NME_SCTY_A), //"a"
																  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_SCTY_A)); //"(Ljava/security/KeyFactory;Ljava/security/spec/X509EncodedKeySpec;)Ljava/security/PublicKey;"

		//oPK = (jobject) (mEnv)->CallObjectMethod(oKeyFactory, mKFGeneratePublic, oX509EKS);
		oPK = (jobject) (mEnv)->CallStaticObjectMethod(cSecurity, mKFGeneratePublic, oKeyFactory, oX509EKS);
		exc = (mEnv)->ExceptionOccurred();
		if (exc) {
			//jclass newExcCls;
			//(mEnv)->ExceptionDescribe();
			(mEnv)->ExceptionClear();
			JNU_ThrowByName(mEnv, readChar(className, BUFFER_STR_SIZE, INVALID_KEY_SPEC_EXC_NAME), //"java.security.spec.InvalidKeySpecException"
								 readChar(methodSign, BUFFER_STR_SIZE, IAB_EXCEPTION_MSG)); //recycle methodSign
			return NULL;
		}
		oPK	= (jobject) (mEnv)->NewGlobalRef(oPK);
		(mEnv)->DeleteLocalRef(strvalue);
		(mEnv)->DeleteLocalRef(strek);
		(mEnv)->DeleteLocalRef(decodeKey);
	//Class KeyFactory
			
	}
	return oPK;
}
#endif //#if GOOGLE_STORE

