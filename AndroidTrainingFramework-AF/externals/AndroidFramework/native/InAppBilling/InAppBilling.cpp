#include "config_Android.h"
#if USE_IN_APP_BILLING  && !USE_IN_APP_BILLING_CRM
#include "InAppBilling.h"
#include "InAppBillingConst.h"
#include "IABstrings_cpp.h"
#include "math.h"
#include "GameUtils/package_utils.h"

#if USE_IAPV2_LEGACY_WRAPPER
#include <inapppurchase/service/common/iap_security.h>
#include <inapppurchase/service/android_billing/iap_android_transaction_manager.h>
#endif

extern "C" void InAppBilling_GetItemList(const char* type)
{
	InAppBilling::getItemList(type);
}

extern "C" void InAppBilling_BuyItem(const char* uid)
{
	DUMP_S(uid);
	InAppBilling::buyItem(uid);
}

extern "C" void InAppBilling_SendNotifyConfirmation(const char* notifyId)
{
	InAppBilling::sendNotifyConfirmation(notifyId);
}

extern "C" int InAppBilling_getTotalItems()
{
	return InAppBilling::getTotalItems();
}

extern "C" std::string InAppBilling_GetHeaders()
{
	return InAppBilling::getHeaders();
}

extern "C" void InAppBilling_FillAttributeArrayByName(char*** array, const char* att)
{
	InAppBilling::fillAttributeArrayByName(array, att);
}

extern "C" void InAppBilling_FillIdArray(char*** array)
{
	InAppBilling::fillIdArray(array);
}

extern "C" void InAppBilling_FillTypeArray(char*** array)
{
	InAppBilling::fillTypeArray(array);
}

extern "C" void InAppBilling_FillBillingAttArrayByName(char*** array, const char* att)
{
	InAppBilling::fillBillingAttArrayByName(array, att);
}

extern "C" void InAppBilling_FreeArrayMem(char** array)
{
	DBG_FN();
	int nit = InAppBilling::getTotalItems();
	for (int i = 0; i < nit; i++)
	{
		delete[](array)[i];
	}
	delete[](array);
	DBG_FNE();
}

extern "C" std::string InAppBilling_GetShopAttributeByName(const char* name)
{
	return InAppBilling::getShopAttributeByName(name);
}

extern "C" void InAppBilling_RestoreTransactions()
{
	InAppBilling::restoreTransactions();
}
extern "C" std::string InAppBilling_GetItemTypeByID(const char* uid)
{
		return InAppBilling::getItemTypeByID(uid);
}

extern "C" std::string InAppBilling_GetAttByID(const char* uid, const char* name)
{
	return InAppBilling::getAttByID(uid, name);
}

extern "C" std::string InAppBilling_GetBillingAttByID(const char* uid, const char* name)
{
	return InAppBilling::getBillingAttByID(uid, name);
}

extern "C" void InAppBilling_GetItemInfoByID(const char* uid, IAB_STORE_ITEM& dst)
{
	 InAppBilling::getItemInfoByID(uid, dst);
}

extern "C" IAB_STORE_ITEM* InAppBilling_GetItemInfoArray()
{
	return InAppBilling::getItemInfoArray();
}

extern "C" void InAppBilling_FreeItemInfoArray(IAB_STORE_ITEM* array)
{
	delete[](array);
	DBG_FNE();
}

extern void InAppBilling_ShowPendingDialog()
{
	InAppBilling::showDialog();
}

extern "C" int InAppBilling_GetState()
{
	return InAppBilling::getState();
}

extern "C" int InAppBilling_GetLastResults(char* desc)
{
	return InAppBilling::getLastResults(desc);
}
/*
 * Class:		com_gameloft_android_shop_GameName_InAppbilling
 * Method:		nativeInit
 */

JNIEXPORT void JNICALL JNI_FUNCTION(iab_InAppBilling_nativeInit) (JNIEnv*  env, jclass iabilling, jobject context)
{
	DBG_FN();
	InAppBilling::init(iabilling, context);
	DBG_FNE();
}
JNIEXPORT void JNICALL JNI_FUNCTION(iab_InAppBilling_nativeSetContext) (JNIEnv*  env, jclass iabilling, jobject context)
{
	DBG_FN();
	InAppBilling::setContext(context);
	DBG_FNE();
}
JNIEXPORT void JNICALL JNI_FUNCTION(iab_InAppBilling_nativeSetIABObject) (JNIEnv*  env, jclass iabilling, jobject iab)
{
	DBG_FN();
	InAppBilling::setIABObject(iab);
	DBG_FNE();
}

JNIEXPORT jobject JNICALL JNI_FUNCTION(iab_InAppBilling_nativeSendData) (JNIEnv*  env, jclass iabilling, jobject bundle)
{
	return InAppBilling::nativeSendData(bundle);
}


jclass 		InAppBilling::cIABilling 		= 0;
jobject		InAppBilling::oIABilling		= 0;
jmethodID 	InAppBilling::mTotalItems		= 0;
//jmethodID 	InAppBilling::mAttribute		= 0;
//jmethodID 	InAppBilling::mItemId			= 0;
jmethodID 	InAppBilling::mGetData			= 0;
jmethodID 	InAppBilling::mGetState			= 0;


jclass 		InAppBilling::cIntent			= 0;
jobject		InAppBilling::oIntent			= 0;
jmethodID 	InAppBilling::mIntentInit		= 0;
jmethodID	InAppBilling::mIntentSetClass	= 0;
jmethodID	InAppBilling::mIntentPutExtra	= 0;

jobject		InAppBilling::oGameActivity		= 0;

jclass		InAppBilling::cBundle			= 0;
//jobject		InAppBilling::oBundle			= 0;
jmethodID	InAppBilling::mBundleInit		= 0;
jmethodID	InAppBilling::mBundlePutString	= 0;
jmethodID	InAppBilling::mBundleGetBArrays	= 0;
jmethodID	InAppBilling::mBundlePutBArrays	= 0;
jmethodID	InAppBilling::mBundleGetInt		= 0;
jmethodID	InAppBilling::mBundlePutInt		= 0;
jmethodID	InAppBilling::mBundleGetLong	= 0;
jmethodID	InAppBilling::mBundlePutLong	= 0;
jmethodID	InAppBilling::mBundleContains	= 0;
jmethodID	InAppBilling::mBundleClear		= 0;

void InAppBilling::init(jclass iabilling, jobject context)
{
DBG_FN();
	IAB_SHOW_VERSION();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	char methodName[BUFFER_STR_SIZE];
	char methodSign[BUFFER_STR_SIZE];
	char className[BUFFER_STR_SIZE];
	
	cIABilling  	= (jclass) (mEnv)->NewGlobalRef(iabilling);
	mTotalItems 	= (mEnv)->GetStaticMethodID(cIABilling,readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_TOTAL_ITEMS), //"getTotalItems"
														readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_TOTAL_ITEMS)); //"()I"
	//mAttribute  	= (mEnv)->GetStaticMethodID(cIABilling,readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_ATTRIBUTE_BY_NAME_IDX), //"getAttributeByNameIdx"
	//													readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_ATTRIBUTE_BY_NAME_IDX)); //"(Ljava/lang/String;I)[B"
	//mItemId			= (mEnv)->GetStaticMethodID(cIABilling,readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_ITEM_ID_BY_IDX), //"getItemIdByIdx"
	//													readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_ITEM_ID_BY_IDX)); //"(I)[B"
	mGetData		= (mEnv)->GetStaticMethodID(cIABilling,readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_DATA), //"getData", 
														readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_DATA)); //"(Landroid/os/Bundle;)Landroid/os/Bundle;");

	mGetState		= (mEnv)->GetStaticMethodID(cIABilling, readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_STATE), //"GetState", 
														readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_STATE)); //"()I");
														
	
	setContext(context);
//Class Intent
	cIntent = (mEnv)->FindClass(readChar(className, BUFFER_STR_SIZE, CL_DESC_INTENT)); //"android/content/Intent"
	if (!cIntent)
	{
		DBG("Can't get a reference from the class Intent...");
		return;
	}
    cIntent = (jclass)(mEnv)->NewGlobalRef(cIntent);
	mIntentInit = (mEnv)->GetMethodID(cIntent, readChar(methodName, BUFFER_STR_SIZE, MD_NME_INIT), //"<init>"
											  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_NO_ARGS_VOID)); //"()V"
	oIntent = (jobject) (mEnv)->NewObject(cIntent, mIntentInit);
	mIntentSetClass = (mEnv)->GetMethodID(cIntent, readChar(methodName, BUFFER_STR_SIZE, MD_NME_SET_CLASS_NAME), //"setClassName"
												  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_SET_CLASS_NAME_OR_PUT_EXTRA)); //"(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;"
	mIntentPutExtra = (mEnv)->GetMethodID(cIntent, readChar(methodName, BUFFER_STR_SIZE, MD_NME_PUT_EXTRA), //"putExtra"
												  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_SET_CLASS_NAME_OR_PUT_EXTRA)); //"(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;"
												  
//Class Intent

//Class Bundle
	cBundle = (mEnv)->FindClass(readChar(className, BUFFER_STR_SIZE, CL_DESC_BUNDLE)); // ("android/os/Bundle");
	if (!cBundle)
	{
		DBG("Can't get a reference from the class cBundle...");
		return;
	}
    cBundle 	= (jclass)(mEnv)->NewGlobalRef(cBundle);
	mBundleInit = (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_INIT), //"<init>"
											  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_NO_ARGS_VOID)); //"()V"
	//oBundle 	= (jobject)(mEnv)->NewObject(cBundle, mBundleInit);
	
	mBundlePutString 	= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_PUT_STRING), //"putString"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_PUT_STRING)); //"(Ljava/lang/String;Ljava/lang/String;)V"
	mBundleGetBArrays 	= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_BYTE_ARRAY), //"getByteArray"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_BYTE_ARRAY)); //"(Ljava/lang/String;)[B"
	mBundlePutBArrays 	= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_PUT_BYTE_ARRAY), //"putByteArray"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_PUT_BYTE_ARRAY)); //"(Ljava/lang/String;[B)V"
	mBundleGetInt 		= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_INT), //"getInt"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_INT)); //"(Ljava/lang/String;)I"
	mBundlePutInt 		= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_PUT_INT), //"putInt"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_PUT_INT)); //"(Ljava/lang/String;I)V"
	mBundleGetLong 		= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_GET_LONG), //"getLong"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_GET_LONG)); //"(Ljava/lang/String;)J"
	mBundlePutLong 		= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_PUT_LONG), //"putLong"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_PUT_LONG)); //"(Ljava/lang/String;J)V"
	mBundleContains 	= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_CONTAINS_KEY), //"containsKey"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_CONTAINS_KEY)); //"(Ljava/lang/String;)Z"
	mBundleClear 		= (mEnv)->GetMethodID(cBundle, readChar(methodName, BUFFER_STR_SIZE, MD_NME_CLEAR), //"clear"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_NO_ARGS_VOID)); //"()V"
	
	//oBundle 			= (jobject) (mEnv)->NewGlobalRef(oBundle);
//Class Bundle
DBG_FNE();
}

int InAppBilling::getTotalItems()
{
	return CallJNIFuncInt(cIABilling, mTotalItems);
}

std::string InAppBilling::getHeaders()
{
	DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;

	bundleClear(bundle);
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_GET_COMMON_HEADERS, bundle);
	
	jobject bdle = getData(bundle);
	
	readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
	bresult = NULL;
	bresult = bundleReadBArray(key, bdle);
	std::string result = "";
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
	if (bresult)
	{
		len = (mEnv)->GetArrayLength(bresult);
		
		char tmp[len + 1];
		memset(tmp, '\0', len + 1);
		
		(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)tmp);
		(mEnv)->DeleteLocalRef(bresult);
		
		DUMP_S(tmp);
		result = tmp;
	}
	return result;
}

void InAppBilling::fillAttributeArrayByName(char*** array, const char* att)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	int nit = getTotalItems();
	DUMP_D(nit);
	
	*array = new char*[nit];
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;
	for (int i = 0; i < nit; i++)
	{
		bundleClear(bundle);
		readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
		bundlePutInt(key, OP_GET_ITEM_ATTS, bundle);
	
		readChar(key, KEY_SIZE, IAB_INDEX);//get the key 'Index'
		bundlePutInt(key, i, bundle);
		
		readChar(key, KEY_SIZE, IAB_NAME);//get the key 'Name'
		bundlePutString(key, att, bundle);

		jobject bdle = getData(bundle);
		
		readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
		bresult = NULL;
		bresult = bundleReadBArray(key, bdle);//recycle [bitm]
		if (bresult)
		{
			len = (mEnv)->GetArrayLength(bresult);
			
			(*array)[i] =  new char[len+1];
			memset((*array)[i], '\0', len+1);
			
			(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)(*array)[i]);
			(mEnv)->DeleteLocalRef(bresult);
		}
		(mEnv)->DeleteLocalRef(bdle);
	}
	(mEnv)->DeleteLocalRef(bundle);
DBG_FNE();
}

void InAppBilling::fillIdArray(char*** array)
{
	DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
		
	int nit = getTotalItems();
	DUMP_D(nit);
	
	*array = new char*[nit];
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;
	for (int i = 0; i < nit; i++)
	{
		bundleClear(bundle);
		readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
		bundlePutInt(key, OP_GET_ID_VALUES, bundle);
	
		readChar(key, KEY_SIZE, IAB_INDEX);//get the key 'Index'
		bundlePutInt(key, i, bundle);
		
		jobject bdle = getData(bundle);
		
		readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
		bresult = NULL;
		bresult = bundleReadBArray(key, bdle);//recycle [bitm]
		
		if (bresult)
		{
			len = (mEnv)->GetArrayLength(bresult);
			
			(*array)[i] =  new char[len+1];
			memset((*array)[i], '\0', len+1);
			
			(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)(*array)[i]);
			(mEnv)->DeleteLocalRef(bresult);
		}
		(mEnv)->DeleteLocalRef(bdle);
	}
	(mEnv)->DeleteLocalRef(bundle);
DBG_FNE();
}

void InAppBilling::fillTypeArray(char*** array)
{
	DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	int nit = getTotalItems();
	DUMP_D(nit);
	
	*array = new char*[nit];
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;
	for (int i = 0; i < nit; i++)
	{
		bundleClear(bundle);
		readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
		bundlePutInt(key, OP_GET_TYPE_VALUES, bundle);
	
		readChar(key, KEY_SIZE, IAB_INDEX);//get the key 'Index'
		bundlePutInt(key, i, bundle);
		
		jobject bdle = getData(bundle);
		
		readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
		bresult = NULL;
		bresult = bundleReadBArray(key, bdle);//recycle [bitm]
		
		if (bresult)
		{
			len = (mEnv)->GetArrayLength(bresult);
			
			(*array)[i] =  new char[len+1];
			memset((*array)[i], '\0', len+1);
			
			(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)(*array)[i]);
			(mEnv)->DeleteLocalRef(bresult);
		}
		(mEnv)->DeleteLocalRef(bdle);
	}
	(mEnv)->DeleteLocalRef(bundle);
DBG_FNE();
}

void InAppBilling::fillBillingAttArrayByName(char*** array, const char* att)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	int nit = getTotalItems();
	DUMP_D(nit);
	
	*array = new char*[nit];
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;
	for (int i = 0; i < nit; i++)
	{
		bundleClear(bundle);
		readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
		bundlePutInt(key, OP_GET_BILLING_ATTS, bundle);
	
		readChar(key, KEY_SIZE, IAB_INDEX);//get the key 'Index'
		bundlePutInt(key, i, bundle);
		
		readChar(key, KEY_SIZE, IAB_NAME);//get the key 'Name'
		bundlePutString(key, att, bundle);
		
		jobject bdle = getData(bundle);
		
		readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
		bresult = NULL;
		bresult = bundleReadBArray(key, bdle);//recycle [bitm]
		
		if (bresult)
		{
			len = (mEnv)->GetArrayLength(bresult);
						
			(*array)[i] =  new char[len+1];
			memset((*array)[i], '\0', len+1);
			
			(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)(*array)[i]);
			(mEnv)->DeleteLocalRef(bresult);
		}
		(mEnv)->DeleteLocalRef(bdle);
	}
	(mEnv)->DeleteLocalRef(bundle);
DBG_FNE();
}

std::string InAppBilling::getShopAttributeByName(const char* name)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;

	bundleClear(bundle);
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_GET_SHOP_ATTS, bundle);
	
	readChar(key, KEY_SIZE, IAB_NAME);//get the key 'Name'
	bundlePutString(key, name, bundle);
	

	jobject bdle = getData(bundle);
	
	readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
	bresult = NULL;
	bresult = bundleReadBArray(key, bdle);
	std::string result = "";
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
	if (bresult)
	{
		len = (mEnv)->GetArrayLength(bresult);
		
		char tmp[len + 1];
		memset(tmp, '\0', len + 1);
		
		(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)tmp);
		(mEnv)->DeleteLocalRef(bresult);
		
		DUMP_S(tmp);
		result = tmp;
	}
	return result;
}

void InAppBilling::restoreTransactions()
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	bundleClear(bundle);
	
	char key[KEY_SIZE];
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_RESTORE_TRANS, bundle);
	
	jobject bdle = getData(bundle);
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
DBG_FNE();
}

std::string InAppBilling::getItemTypeByID(const char* uid)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;

	bundleClear(bundle);
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_GET_TYPE_WUID, bundle);

	readChar(key, KEY_SIZE, IAB_UID);//get the key 'UID'
	bundlePutString(key, uid, bundle);
	

	jobject bdle = getData(bundle);
	
	readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
	bresult = NULL;
	bresult = bundleReadBArray(key, bdle);
	std::string result = "";
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
	if (bresult)
	{
		len = (mEnv)->GetArrayLength(bresult);
		
		char tmp[len + 1];
		memset(tmp, '\0', len + 1);
		
		(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)tmp);
		(mEnv)->DeleteLocalRef(bresult);
		
		DUMP_S(tmp);
		result = tmp;
	}
	return result;
}
std::string InAppBilling::getAttByID(const char* uid, const char* name)
{
DBG_FN();

	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;

	bundleClear(bundle);
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_GET_ATT_WUID, bundle);

	
	readChar(key, KEY_SIZE, IAB_NAME);//get the key 'Name'
	bundlePutString(key, name, bundle);
	
	readChar(key, KEY_SIZE, IAB_UID);//get the key 'UID'
	bundlePutString(key, uid, bundle);
	

	jobject bdle = getData(bundle);
	
	readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
	bresult = NULL;
	bresult = bundleReadBArray(key, bdle);
	std::string result = "";
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
	if (bresult)
	{
		len = (mEnv)->GetArrayLength(bresult);
		
		char tmp[len + 1];
		memset(tmp, '\0', len + 1);
		
		(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)tmp);
		(mEnv)->DeleteLocalRef(bresult);
		
		DUMP_S(tmp);
		result = tmp;
	}
	return result;
}

std::string InAppBilling::getBillingAttByID(const char* uid, const char* name)
{ 
	DBG_FN();
	
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;

	bundleClear(bundle);
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_GET_BATT_WUID, bundle);

	
	readChar(key, KEY_SIZE, IAB_NAME);//get the key 'Name'
	bundlePutString(key, name, bundle);
	
	readChar(key, KEY_SIZE, IAB_UID);//get the key 'UID'
	bundlePutString(key, uid, bundle);
	

	jobject bdle = getData(bundle);
	
	readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
	bresult = NULL;
	bresult = bundleReadBArray(key, bdle);
	std::string result = "";
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
	if (bresult)
	{
		len = (mEnv)->GetArrayLength(bresult);
		
		char tmp[len + 1];
		memset(tmp, '\0', len + 1);
		
		(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)tmp);
		(mEnv)->DeleteLocalRef(bresult);
		
		DUMP_S(tmp);
		result = tmp;
	}
	return result;
}

void InAppBilling::getItemInfoByID(const char* uid, IAB_STORE_ITEM& dst)
{ 
	DBG_FN();
	char key[KEY_SIZE];
	dst.content_id		= uid;
	dst.content_type	= getItemTypeByID(uid);
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_NAME)] 				= getAttByID(uid, readChar(key, KEY_SIZE, IAB_NAME));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_AMOUNT)] 			= getAttByID(uid, readChar(key, KEY_SIZE, IAB_AMOUNT));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_DESCRIPTION)] 		= getAttByID(uid, readChar(key, KEY_SIZE, IAB_DESCRIPTION));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_IMAGE)] 			= getAttByID(uid, readChar(key, KEY_SIZE, IAB_IMAGE));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_WEB_DETAILS)] 		= getAttByID(uid, readChar(key, KEY_SIZE, IAB_WEB_DETAILS));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_OLD_PRICE)] 		= getAttByID(uid, readChar(key, KEY_SIZE, IAB_OLD_PRICE));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_OLD_AMOUNT)]		= getAttByID(uid, readChar(key, KEY_SIZE, IAB_OLD_AMOUNT));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_TRACKING_ID)]		= getAttByID(uid, readChar(key, KEY_SIZE, IAB_TRACKING_ID));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_AMZN_IAP_TYPE)] 	= getAttByID(uid, readChar(key, KEY_SIZE, IAB_AMZN_IAP_TYPE));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_PRICE)] 			= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_PRICE));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_FORMATTED_PRICE)] 	= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_FORMATTED_PRICE));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_CURRENCY)] 			= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_CURRENCY));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_CURRENCY_SIMBOL)] 	= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_CURRENCY_SIMBOL));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_UID)] 				= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_UID));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_PID)] 				= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_PID));
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_LOCALE)] 			= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_LOCALE));
#if USE_PHD_PSMS_BILL_FLOW
	dst.m_propertiesMap[readChar(key, KEY_SIZE, IAB_PSMS_VIRTUAL_AMOUNT)] 			= getBillingAttByID(uid, readChar(key, KEY_SIZE, IAB_PSMS_VIRTUAL_AMOUNT));
#endif
	DBG_FNE();
}

IAB_STORE_ITEM* InAppBilling::getItemInfoArray()
{ 
	DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	int nit = getTotalItems();
	DUMP_D(nit);
	
	IAB_STORE_ITEM* array = new IAB_STORE_ITEM[nit];
	
	jobject bundle = newBundle(mEnv);
	char key[KEY_SIZE];
	
	jbyteArray bresult;
	int len;
	
	for (int i = 0; i < nit; i++)
	{
		bundleClear(bundle);
		readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
		bundlePutInt(key, OP_GET_ID_VALUES, bundle);
	
		readChar(key, KEY_SIZE, IAB_INDEX);//get the key 'Index'
		bundlePutInt(key, i, bundle);
		
		jobject bdle = getData(bundle);
		
		readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'result'
		bresult = NULL;
		bresult = bundleReadBArray(key, bdle);//recycle [bitm]
		
		if (bresult)
		{
			len = (mEnv)->GetArrayLength(bresult);
			
			char uid[len+1];
			memset(uid, '\0', len+1);
			
			(mEnv)->GetByteArrayRegion(bresult, 0, len, (jbyte*)uid);
			(mEnv)->DeleteLocalRef(bresult);
			
			getItemInfoByID(uid, array[i]);
			DBG("GOT ITEM BY ID: %s", uid);
		}
		(mEnv)->DeleteLocalRef(bdle);
	}
	(mEnv)->DeleteLocalRef(bundle);
	
	DBG_FNE();
	return array;
}

void InAppBilling::getItemList(const char* type)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	bundleClear(bundle);
	
	char key[KEY_SIZE];
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_RETRIEVE_LIST, bundle);
	
	readChar(key, KEY_SIZE, IAB_LIST);//get the key 'List'
	bundlePutString(key, type, bundle);
		
	jobject bdle = getData(bundle);
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
DBG_FNE();
}

void InAppBilling::sendNotifyConfirmation(const char* notifyId)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	bundleClear(bundle);
	
	char key[KEY_SIZE];
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_SEND_CONFIRMATION, bundle);
		
	
	readChar(key, KEY_SIZE, IAB_NOTIFY_ID);//get the key 'Notify ID'
	bundlePutString(key, notifyId, bundle);
	
	jobject bdle = getData(bundle);
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
DBG_FNE();
}

void InAppBilling::showDialog()
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	bundleClear(bundle);
	
	
/// dummy values
	char key[KEY_SIZE];
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_IAB_SHOW_DIALOG, bundle);
	
	readChar(key, KEY_SIZE, IAB_CHAR_ID);//get the key 'character id'
	char cCharId[24];
	cCharId[0] = 'A';
	cCharId[1] = '\0';
	//charId = 18446744073709551615ULL; test ULL max number
	//uI64ToChar (cCharId, 24, cCharId);
	bundlePutString(key, cCharId, bundle);
	
	readChar(key, KEY_SIZE, IAB_CHAR_REGION);//get the key 'character region'
	bundlePutString(key, cCharId, bundle);
	
	readChar(key, KEY_SIZE, IAB_ITEM);//get the key 'Item'
	bundlePutString(key, cCharId, bundle);
///
	
	jobject bdle = getData(bundle);
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
	
DBG_FNE();
}

int InAppBilling::getState()
{
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	return (mEnv)->CallStaticIntMethod(cIABilling, mGetState);
}
int InAppBilling::getLastResults(char* desc)
{
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	bundleClear(bundle);
	
	char key[KEY_SIZE];
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_IAB_LAST_RESULTS, bundle);

	jobject bdle = getData(bundle);
	
	
	readChar(key, KEY_SIZE, IAB_RESULT);//get the key 'Result'
	jbyteArray bdesc;
	bdesc = bundleReadBArray(key, bdle);
	
	if (bdesc)
	{
		int len = (mEnv)->GetArrayLength(bdesc);
		memset(desc, '\0', len+1);
		(mEnv)->GetByteArrayRegion(bdesc, 0, len, (jbyte*)desc);
		(mEnv)->DeleteLocalRef(bdesc);
	}

	(mEnv)->DeleteLocalRef(bdle);
	
	readChar(key, KEY_SIZE, IAB_STATUS);//get the key 'Status'
	
	int ret = bundleReadInt(key, bundle);
	(mEnv)->DeleteLocalRef(bundle);

	return ret;
}

extern "C" void JNU_ThrowByName(JNIEnv* env, const char* name, const char* msg)
{
	jclass cls = acp_utils::api::PackageUtils::GetClass(name);

	if (cls != NULL) {
		(env)->ThrowNew(cls, msg);
	}

	(env)->DeleteLocalRef(cls);
}

extern "C" void uI64ToChar(char* dest, int size, iab_uint64 src)
{
	memset(dest,'\0',size);
	char tmpC[size];
	int i;
	if (src == 0) dest[0] = '0';
	for (i = 0;src > 0; i++)
	{
		strcpy(tmpC,dest);
		dest[0] = (src % 10 + '0');
		strcpy(dest+1,tmpC);
		src /= 10ULL;
	}
}

extern "C" void charToUI64(iab_uint64* dest, int size,  const char* src)
{
	int i;
	*dest=0;
	for(i = 0; i < size; i++)
	{
		//*dest += (src[i] -'0') * (pow(10ULL,(size-i-1)));
		*dest += (src[i] -'0') * (pow((long double)10,(size-i-1)));
	}
}

void InAppBilling::buyItem(const char* uid)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jobject bundle = newBundle(mEnv);
	bundleClear(bundle);
	
	char key[KEY_SIZE];
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	bundlePutInt(key, OP_TRANSACTION, bundle);

	readChar(key, KEY_SIZE, IAB_ITEM);//get the key 'Item'
	bundlePutString(key, uid, bundle);
		
	jobject bdle = getData(bundle);
	(mEnv)->DeleteLocalRef(bdle);
	(mEnv)->DeleteLocalRef(bundle);
DBG_FNE();
}

jobject InAppBilling::getData(jobject bundle)
{
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);

	return (jobject)(mEnv)->CallStaticObjectMethod(cIABilling, mGetData, bundle);
}

jobject InAppBilling::nativeSendData(jobject bundle)
{
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	//bundleClear(oBundle);
	jobject oBundle = newBundle(mEnv);
	int index;
	
	char key[KEY_SIZE];//IAB_OPERATION
	char cidx[INDEX_SIZE];//IAB_INDEX
	char cres[RESULT_SIZE];//IAB_RESULT
	char result[RESPONSE_BUFFER];//Response buffer
	
	readChar(key, KEY_SIZE, IAB_OPERATION);//get the key 'operation'
	int opt = bundleReadInt(key, bundle);
	
	switch(opt)
	{
		case OP_GET_STRING:
		{
			readChar(cidx, INDEX_SIZE, IAB_INDEX);//get the key 'index'
			index = bundleReadInt(cidx, bundle);
			
			readChar(result, RESPONSE_BUFFER, index);
			
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			bundlePutString(cres, result, oBundle);
		}	
		break;
		case OP_FINISH_GET_LIST:
		{
			char clist[32];
			readChar(clist, 32, IAB_LIST);//get the key 'list'
			jbyteArray blist;
			blist = bundleReadBArray(clist, bundle);
			
			if (blist)
			{
				int len = (mEnv)->GetArrayLength(blist);
				char list[len+1];
				memset(list, '\0', len+1);
				(mEnv)->GetByteArrayRegion(blist, 0, len, (jbyte*)list);
				(mEnv)->DeleteLocalRef(blist);
				InAppBilling_GetItemListCB(list, InAppBilling::getTotalItems());
			}else
				InAppBilling_GetItemListCB(NULL, 0);
		}		
		break;
		case OP_START_GET_FQC_NAME:
		{
			memset(result,'\0', RESPONSE_BUFFER);
			strcat(result,STR_APP_PACKAGE);
			char cname[128];

			readChar(cidx, INDEX_SIZE, IAB_INDEX);//get the key index
			index = bundleReadInt(cidx, bundle);
			
			readChar(cname, 128, index);
			strcat(result,cname);
			
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			bundlePutString(cres, result, oBundle);
		}	
		break;
		case OP_GET_GGLIVE_UID:
		case OP_GET_CREDENTIALS:
		{
			readChar(cidx, INDEX_SIZE, IAB_INDEX);//get the key index
			index = bundleReadInt(cidx, bundle);
			const char* resultValue;
			
			if (opt == OP_GET_GGLIVE_UID)
				resultValue = InAppBilling_GetGLLiveUser();
			else if (opt == OP_GET_CREDENTIALS)
				resultValue = InAppBilling_GetCredentials();
			else
				resultValue = "0";
					
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			bundlePutString(cres, resultValue, oBundle);
		}	
		break;
	#if USE_IN_APP_GLOT_LOGGING
		case OP_LOGGING_LOG_INFO:
		{
			readChar(cidx, INDEX_SIZE, IAB_LEVEL);//get the key level
			int level = bundleReadInt(cidx, bundle);
			
			readChar(cidx, INDEX_SIZE, IAB_STATUS);//get the key status
			int status = bundleReadInt(cidx, bundle);
			
			char cname[KEY_SIZE];
			readChar(cname, KEY_SIZE, IAB_NAME);//get the key name
			
			jbyteArray bname;
			bname = bundleReadBArray(cname, bundle);
			
			if (bname)//if bname was added
			{
				int len = (mEnv)->GetArrayLength(bname);
				char tmpData[len+1];
				memset (tmpData, '\0', len+1);
				
				(mEnv)->GetByteArrayRegion(bname, 0, len, (jbyte*)tmpData);
				(mEnv)->DeleteLocalRef(bname);
				DUMP_S(tmpData);
				InAppBilling_LogInfo(level, status, tmpData);
			}
		}
		break;
	#endif	
	#if USE_IAPV2_LEGACY_WRAPPER
	case OP_FINISH_BUY_ITEM:
			{
				jobject dbundle = newBundle(mEnv);

				bundleClear(dbundle);
				bundlePutInt(key, OP_CLOSE_WAIT_DIALOG, dbundle);

				jobject bdle = getData(dbundle);
				(mEnv)->DeleteLocalRef(dbundle);
				(mEnv)->DeleteLocalRef(bdle);
				
				char citm[KEY_SIZE];
				readChar(citm, KEY_SIZE, IAB_ITEM);//get the key item
				jbyteArray bitm;
				bitm = bundleReadBArray(citm, bundle);
				readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
				int buyError;
				buyError = bundleReadInt(cres, bundle);
				
				iap::AndroidResult aResult;
				aResult.m_result = buyError;
				
				if (bitm)//if uid was added
				{
					int len = (mEnv)->GetArrayLength(bitm);
					char cuid[len+1];
					memset(cuid, '\0', len+1);
					(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cuid);
					(mEnv)->DeleteLocalRef(bitm);
					aResult.m_itemId = cuid;
					DUMP_S(cuid);
					
					//Notify Id
					char cNotifyId[512];
					memset(cNotifyId, '\0', 512);				
					readChar(citm, KEY_SIZE, IAB_NOTIFY_ID);//get the key char id [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cNotifyId);
						(mEnv)->DeleteLocalRef(bitm);
					}
					aResult.m_notifyId = cNotifyId;
					DUMP_S(cNotifyId);
					
					char ceComTxId[64];
					memset(ceComTxId, '\0', 64);
				
					readChar(citm, KEY_SIZE, IAB_ECOM_TX_ID);//get the key EC_TX_ID [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)ceComTxId);
						(mEnv)->DeleteLocalRef(bitm);
					}
					aResult.m_eComTxId = ceComTxId;
					DUMP_S(ceComTxId);

					//TP Error code
					readChar(citm, INDEX_SIZE, IAB_TP_ERROR_CODE);//get the key error code [recycle citm]
					int tpErrorCode = 0;
					if (bundleContainsKey(citm, bundle) == JNI_TRUE)
					{
						tpErrorCode = bundleReadInt(citm, bundle);
					}
					aResult.m_thirdPartyErrorCode = tpErrorCode;
					
					//TP Error String Message
					char cErrorS[512];
					memset(cErrorS, '\0', 512);				
					readChar(citm, KEY_SIZE, IAB_TP_ERROR);//get the key error [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cErrorS);
						(mEnv)->DeleteLocalRef(bitm);
					}
					aResult.m_thirdPartyErrorString = cErrorS;
					DUMP_S(cErrorS);

					//Seconds to next transaction
					DBG("native wap info");
					int secsToTransaction = 0;
					readChar(citm, KEY_SIZE, IAB_WAP_BUNDLE_SECONDS_BEFORE_TRANS);//get the key error [recycle citm]
					if (bundleContainsKey(citm, bundle) == JNI_TRUE)
					{
						secsToTransaction = bundleReadInt(citm, bundle);
					}
					aResult.m_timeBeforeTransaction = secsToTransaction;

					//Next transaction time
					char cNextTransTime[512];
					memset(cNextTransTime, '\0', 512);				
					
					readChar(citm, KEY_SIZE, IAB_WAP_BUNDLE_NEXT_TRANS_TIME);//get the key error [recycle citm]
					if(bundleContainsKey(citm, bundle) == JNI_TRUE)
					{
						bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
						if (bitm)
						{
							len = (mEnv)->GetArrayLength(bitm);
							(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cNextTransTime);
							(mEnv)->DeleteLocalRef(bitm);
						}
					}
					aResult.m_nextTransactionTime = cNextTransTime;


					//TP Certificate
					readChar(citm, KEY_SIZE, IAB_STORE_CERT);//get the key cert [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						char cCertz[len+1];
						memset(cCertz, '\0', len+1);				
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cCertz);
						(mEnv)->DeleteLocalRef(bitm);
						aResult.m_thirdPartyCert = cCertz;
					}
					
					//shop
					char cShop[64];
					memset(cShop, '\0', 64);				
					readChar(citm, KEY_SIZE, IAB_SHOP);//get the key shop [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cShop);
						(mEnv)->DeleteLocalRef(bitm);
					}
					aResult.m_shopName = cShop;
					DUMP_S(cShop);					
				}
				iap::TransactionManager::GetInstance()->AddTransaction(aResult);
				DBG_LN();
			}	
			break;
			case OP_FINISH_RESTORE_TRANS:
			{
				char citm[KEY_SIZE];
				readChar(citm, KEY_SIZE, IAB_ITEM);//get the key item
				jbyteArray bitm;
				bitm = bundleReadBArray(citm, bundle);
				
				iap::AndroidResult aResult;
				aResult.m_result = iap::transaction::TS_RESTORED;
				
				if (bitm)//if uid was added
				{
					aResult.m_isRestore = true;
					
					int len = (mEnv)->GetArrayLength(bitm);
					char cuid[len+1];
					memset(cuid, '\0', len+1);
					(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cuid);
					(mEnv)->DeleteLocalRef(bitm);
					aResult.m_itemId = cuid;
					DUMP_S(cuid);
					
					//Notify Id
					char cNotifyId[512];
					memset(cNotifyId, '\0', 512);				
					readChar(citm, KEY_SIZE, IAB_NOTIFY_ID);//get the key char id [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cNotifyId);
						(mEnv)->DeleteLocalRef(bitm);
					}
					aResult.m_notifyId = cNotifyId;
					DUMP_S(cNotifyId);

					//TP Certificate
					readChar(citm, KEY_SIZE, IAB_STORE_CERT);//get the key cert [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						char cCertz[len+1];
						memset(cCertz, '\0', len+1);				
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cCertz);
						(mEnv)->DeleteLocalRef(bitm);
						aResult.m_thirdPartyCert = cCertz;
					}
					
					//shop
					char cShop[64];
					memset(cShop, '\0', 64);				
					readChar(citm, KEY_SIZE, IAB_SHOP);//get the key shop [recycle citm]
					bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
					if (bitm)
					{
						len = (mEnv)->GetArrayLength(bitm);
						(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cShop);
						(mEnv)->DeleteLocalRef(bitm);
					}
					aResult.m_shopName = cShop;
					DUMP_S(cShop);

					iap::TransactionManager::GetInstance()->AddTransaction(aResult);
				}
				DBG_LN();
			}	
			break;
	#else //#if USE_IAPV2_LEGACY_WRAPPER	
		case OP_FINISH_BUY_ITEM:
		{
			jobject dbundle = newBundle(mEnv);

			bundleClear(dbundle);
			bundlePutInt(key, OP_CLOSE_WAIT_DIALOG, dbundle);

			jobject bdle = getData(dbundle);
			(mEnv)->DeleteLocalRef(dbundle);
			(mEnv)->DeleteLocalRef(bdle);
			
	
			char citm[KEY_SIZE];
			readChar(citm, KEY_SIZE, IAB_ITEM);//get the key item
			jbyteArray bitm;
			bitm = bundleReadBArray(citm, bundle);
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			int buyError;
			buyError = bundleReadInt(cres, bundle);
			if (bitm)//if uid was added
			{
				int len = (mEnv)->GetArrayLength(bitm);
				char cuid[len+1];
				memset(cuid, '\0', len+1);
				(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cuid);
				(mEnv)->DeleteLocalRef(bitm);
				DUMP_S(cuid);
				
				char cNotifyID[512];
				memset(cNotifyID, '\0', 512);
			
				readChar(citm, KEY_SIZE, IAB_NOTIFY_ID);//get the key char id [recycle citm]
				bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
				if (bitm)
				{
					len = (mEnv)->GetArrayLength(bitm);
					(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cNotifyID);
					(mEnv)->DeleteLocalRef(bitm);
				}
				DUMP_S(cNotifyID);
				
				char ceComTxId[64];
				memset(ceComTxId, '\0', 64);
			
				readChar(citm, KEY_SIZE, IAB_ECOM_TX_ID);//get the key EC_TX_ID [recycle citm]
				bitm = bundleReadBArray(citm, bundle);//recycle [bitm]
				if (bitm)
				{
					len = (mEnv)->GetArrayLength(bitm);
					(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)ceComTxId);
					(mEnv)->DeleteLocalRef(bitm);
				}
				DUMP_S(ceComTxId);
			
				InAppBilling_BuyItemCB(cuid, buyError,  cNotifyID, ceComTxId);
			}else
				InAppBilling_BuyItemCB("", buyError, "", "");
			DBG_LN();
		}	
		break;
		case OP_FINISH_RESTORE_TRANS:
		{
			char citm[KEY_SIZE];
			readChar(citm, KEY_SIZE, IAB_ITEM);//get the key item
			jbyteArray bitm;
			bitm = bundleReadBArray(citm, bundle);
			if (bitm)//if uid was added
			{
				int len = (mEnv)->GetArrayLength(bitm);
				char cuid[len+1];
				memset(cuid, '\0', len+1);
				(mEnv)->GetByteArrayRegion(bitm, 0, len, (jbyte*)cuid);
				(mEnv)->DeleteLocalRef(bitm);
				DUMP_S(cuid);
				InAppBilling_RestoreTransactionCB(cuid);
			}else
				InAppBilling_RestoreTransactionCB(NULL);
			DBG_LN();
		}	
		break;
#endif //#if USE_IAPV2_LEGACY_WRAPPER
		case OP_GET_NEW_NOUNCE:
		{
			std::string nounce;
		#if USE_SECURITY_SIGNATURE_VERIFICATION_WITH_FEED
			iap::NounceGenerator nounceGenerator;
			nounce = nounceGenerator();
			
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
		#endif
			bundlePutString(cres, nounce.c_str(), oBundle);
		}	
		break;
		case OP_GET_APP_HDER:
		{
			std::string rValue;
		#if USE_SECURITY_SIGNATURE_VERIFICATION_WITH_FEED
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			readChar(result, RESPONSE_BUFFER, IAB_IGP_SHORTCODE);//recycle result
			iap::TransactionManager::GetInstance()->GetSettings(result, rValue);
		#endif
			bundlePutString(cres, rValue.c_str(), oBundle);
		}	
		break;
		case OP_GET_VERSION_HDER:
		{
			std::string rValue;
		#if USE_SECURITY_SIGNATURE_VERIFICATION_WITH_FEED
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			readChar(result, RESPONSE_BUFFER, IAB_APP_VERSION);//recycle result
			iap::TransactionManager::GetInstance()->GetSettings(result, rValue);
		#endif
			bundlePutString(cres, rValue.c_str(), oBundle);
		}	
		break;
		case OP_GET_PRODUCT_ID_HDER:
		{
			std::string rValue;
		#if USE_SECURITY_SIGNATURE_VERIFICATION_WITH_FEED
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			readChar(result, RESPONSE_BUFFER, IAB_PRODUCT_ID);//recycle result
			iap::TransactionManager::GetInstance()->GetSettings(result, rValue);
		#endif
			bundlePutString(cres, rValue.c_str(), oBundle);
		}	
		break;
		case OP_GET_ANONYMOUS_CRED:
		{
			std::string rValue;
		#if USE_IAPV2_LEGACY_WRAPPER
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			readChar(result, RESPONSE_BUFFER, IAB_ANONYMOUS_CREDENTIAL);//recycle result
			iap::TransactionManager::GetInstance()->GetSettings(result, rValue);
		#endif
			bundlePutString(cres, rValue.c_str(), oBundle);
		}	
		break;
		case OP_GET_FED_DATA_CENTER:
		{
			std::string rValue;
		#if USE_IAPV2_LEGACY_WRAPPER
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			readChar(result, RESPONSE_BUFFER, IAB_FED_DATA_CENTER);//recycle result
			iap::TransactionManager::GetInstance()->GetSettings(result, rValue);
		#endif
			bundlePutString(cres, rValue.c_str(), oBundle);
		}
		break;
		case OP_CHECK_MD5_HASH:
		{
			int result = 0;
		#if USE_SECURITY_SIGNATURE_VERIFICATION_WITH_FEED
			iap::RSAKey rsaKey = iap::RSAKey(iap::extern_signature[0], (size_t)iap::extern_signature[1], (size_t)iap::extern_signature[2]);
			jbyteArray baValue;

			std::string data;
			std::string nounce;
			std::string hash;
			DBG_LN();
			char ckey[KEY_SIZE];
			readChar(ckey, KEY_SIZE, IAB_DATA);//get the key data
			baValue = bundleReadBArray(ckey, bundle);
			if (baValue)
			{
				int len = (mEnv)->GetArrayLength(baValue);
				DBG("data len[%d]",len);
				char cTemp[len+1];
				memset(cTemp, '\0', len+1);				
				
				(mEnv)->GetByteArrayRegion(baValue, 0, len, (jbyte*)cTemp);
				(mEnv)->DeleteLocalRef(baValue);
				data = cTemp;
			} else {
				result = 1;
			}
			DBG_LN();
			readChar(ckey, KEY_SIZE, IAB_NOUNCE);//get the key nounce [recycle ckey]
			baValue = bundleReadBArray(ckey, bundle);
			if (baValue && result == 0)
			{
				int len = (mEnv)->GetArrayLength(baValue);
				char cTemp[len+1];
				memset(cTemp, '\0', len+1);				
				
				(mEnv)->GetByteArrayRegion(baValue, 0, len, (jbyte*)cTemp);
				(mEnv)->DeleteLocalRef(baValue);
				nounce = cTemp;
			} else {
				result = 1;
			}
			DBG_LN();
			readChar(ckey, KEY_SIZE, IAB_HASH);//get the key hash [recycle ckey]
			baValue = bundleReadBArray(ckey, bundle);
			if (baValue && result == 0)
			{
				int len = (mEnv)->GetArrayLength(baValue);
				char cTemp[len+1];
				memset(cTemp, '\0', len+1);				
				
				(mEnv)->GetByteArrayRegion(baValue, 0, len, (jbyte*)cTemp);
				(mEnv)->DeleteLocalRef(baValue);
				hash = cTemp;
			} else {
				result = 1;
			}
			DBG_LN();
			if (result == 0)
			{
				DBG_LN();
				DBG("hash[%s]", hash.c_str());
				DBG("nounce[%s]", nounce.c_str());
				DBG("data[%s]",data.c_str());
				result = CheckMD5Hash(data.c_str(), data.size(), nounce.c_str(), nounce.size(), hash.c_str(), rsaKey);
				DBG_LN();
			}
		#else
			result = 0;
		#endif
			readChar(cres, RESULT_SIZE, IAB_RESULT);//get the key result
			bundlePutInt(cres, result, oBundle);
		}
		break;
		
		default:
		
		break;
	}
	return oBundle;
}

const char* InAppBilling::readChar(char* dst, int size, int idx)
{
	memset(dst, '\0', size);
	
	for(int c = 0; c</*ALC_STRING_MAX*/512; c++)
	{
		int id = IAB_STRING_MAP[idx][c];
		if(id == IAB_STR_EOS)
		{
			dst[c] = '\0';
			break;
		}else
		{
			dst[c] = IAB_SMAP_CHARS[id];
		}
	}
	return dst;
}

void InAppBilling::bundlePutString(const char* key, const char* value, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);

	jstring skey = charToString(key);
	jstring svalue = charToString(value);
	(mEnv)->CallVoidMethod(bundle, mBundlePutString, skey, svalue);
	(mEnv)->DeleteLocalRef(skey);
	(mEnv)->DeleteLocalRef(svalue);
}

jbyteArray InAppBilling::bundleReadBArray(const char* key, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jstring string = charToString(key);
	jbyteArray result = (jbyteArray)(mEnv)->CallObjectMethod(bundle, mBundleGetBArrays, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

void InAppBilling::bundlePutBArray(const char* key, jbyteArray value, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	jstring skey = charToString(key);
	(mEnv)->CallVoidMethod(bundle, mBundlePutBArrays, skey, value);
	(mEnv)->DeleteLocalRef(skey);
}

int InAppBilling::bundleReadInt(const char* key, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (bundleContainsKey(key, bundle) == JNI_FALSE)
	{
		return -1;
	}
	jstring string = charToString(key);
	int result = (mEnv)->CallIntMethod(bundle, mBundleGetInt, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

void InAppBilling::bundlePutInt(const char* key, int value, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);

	jstring string = charToString(key);
	(mEnv)->CallVoidMethod(bundle, mBundlePutInt, string, value);
	(mEnv)->DeleteLocalRef(string);
}

iab_uint64 InAppBilling::bundleReadLong(const char* key, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (bundleContainsKey(key, bundle) == JNI_FALSE)
	{
		return -1;
	}
	jstring string = charToString(key);
	iab_uint64 result = (mEnv)->CallLongMethod(bundle, mBundleGetLong, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

void InAppBilling::bundlePutLong(const char* key, iab_uint64 value, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);

	jstring string = charToString(key);
	(mEnv)->CallVoidMethod(bundle, mBundlePutLong, string, value);
	(mEnv)->DeleteLocalRef(string);
}

jboolean InAppBilling::bundleContainsKey(const char* key, jobject bundle)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	jstring string = charToString(key);
	jboolean result = (mEnv)->CallBooleanMethod(bundle, mBundleContains, string);
	(mEnv)->DeleteLocalRef(string);
	return result;
}

jobject InAppBilling::newBundle(JNIEnv* mEnv)
{
	return (jobject)(mEnv)->NewObject(cBundle, mBundleInit);
}

void InAppBilling::bundleClear(jobject bundle)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	(mEnv)->CallVoidMethod(bundle, mBundleClear);
}

jstring InAppBilling::charToString(const char* str)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	return (mEnv)->NewStringUTF(str);
}

void InAppBilling::setContext(jobject context)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	
	acp_utils::ScopeGetEnv st(mEnv);
	
	oGameActivity = (jobject) (mEnv)->NewGlobalRef(context);
DBG_FNE();	
}

void InAppBilling::setIABObject(jobject obj)
{
DBG_FN();
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	oIABilling = (jobject) (mEnv)->NewGlobalRef(obj);
DBG_FNE();	
}

void InAppBilling::launchInAppBilling(const char* type)
{
DBG_FN();
	/*char methodName[BUFFER_STR_SIZE];
	char methodSign[BUFFER_STR_SIZE];
	JNIEnv* mEnv = NULL;
	ScopeGetEnv sta(mEnv, mJavaVM);
	
	jstring pkg = (mEnv)->NewStringUTF(STR_APP_PACKAGE);
	
	int len = strlen(STR_APP_PACKAGE) + strlen(IAB_CLASS_NAME_IN_APP_BILLING) + 1;
	char iabc[len];
	sprintf(iabc,"%s%s",STR_APP_PACKAGE,IAB_CLASS_NAME_IN_APP_BILLING);
	jstring iab 			= (mEnv)->NewStringUTF(iabc);
	
	jstring strtype = (mEnv)->NewStringUTF("list");
	jstring strvalue = (mEnv)->NewStringUTF(type);
	
	(mEnv)->CallObjectMethod(oIntent, mIntentSetClass, pkg, iab);
	(mEnv)->CallObjectMethod(oIntent, mIntentPutExtra, strtype, strvalue);
	

	jmethodID mid = (mEnv)->GetMethodID((mEnv)->GetObjectClass(oGameActivity), readChar(methodName, BUFFER_STR_SIZE, MD_NME_START_ACTIVITY), //"startActivity"
													  readChar(methodSign, BUFFER_STR_SIZE, MD_SIG_START_ACTIVITY)); //"(Landroid/content/Intent;)V"
	(mEnv)->CallVoidMethod(oGameActivity, mid,oIntent);
	
	(mEnv)->DeleteLocalRef(pkg);
	(mEnv)->DeleteLocalRef(iab);	
	(mEnv)->DeleteLocalRef(strtype);	
	(mEnv)->DeleteLocalRef(strvalue);	*/
DBG_FNE();
}

void InAppBilling::CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	memset(out, '\0', MAX);
	jbyteArray byteArray 	= (jbyteArray) (mEnv)->CallStaticObjectMethod(javaClass, javaMethod);
	int lon 				= (mEnv)->GetArrayLength(byteArray);
	(mEnv)->GetByteArrayRegion(byteArray, 0, lon, (jbyte*)out);
	(mEnv)->DeleteLocalRef(byteArray);
}

void InAppBilling::CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX, const char* param)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	memset(out, '\0', MAX);
	jstring string 			= (mEnv)->NewStringUTF(param);
	
	jbyteArray byteArray 	= (jbyteArray) (mEnv)->CallStaticObjectMethod(javaClass, javaMethod, string);
	int lon 				= (mEnv)->GetArrayLength(byteArray);
	(mEnv)->GetByteArrayRegion(byteArray, 0, lon, (jbyte*)out);
	
	
	(mEnv)->DeleteLocalRef(byteArray);
	(mEnv)->DeleteLocalRef(string);
}

char * InAppBilling::CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX, const char* param, int idx)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (MAX != -1) //-1 means: NO memory space is defined to out 
		memset(out, '\0', MAX);
	jstring string 			= (mEnv)->NewStringUTF(param);
	jbyteArray byteArray 	= (jbyteArray) (mEnv)->CallStaticObjectMethod(javaClass, javaMethod, string, idx);
	(mEnv)->DeleteLocalRef(string);
	
	int lon = 0;
	if (byteArray)
		lon		= (mEnv)->GetArrayLength(byteArray);
	if (lon > 0)
	{
		if (MAX == -1)
		{
			out = new char(lon + 1);
			memset(out, '\0', lon + 1);
		}
		(mEnv)->GetByteArrayRegion(byteArray, 0, lon, (jbyte*)out);
		(mEnv)->DeleteLocalRef(byteArray);
	}
return out;
}

char * InAppBilling::CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX, int idx)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (MAX != -1) //-1 means: NO memory space is defined to out 
		memset(out, '\0', MAX);
	
	jbyteArray byteArray 	= (jbyteArray) (mEnv)->CallStaticObjectMethod(javaClass, javaMethod, idx);
	int lon = 0;
	if (byteArray)lon		= (mEnv)->GetArrayLength(byteArray);
	if (lon > 0)
	{
		if (MAX == -1)
		{
			out = new char(lon + 1);
			memset(out, '\0', lon + 1);
		}
		(mEnv)->GetByteArrayRegion(byteArray, 0, lon, (jbyte*)out);
		(mEnv)->DeleteLocalRef(byteArray);
	}
return out;
}

void InAppBilling::CallJNIFuncSendChar(jclass javaClass, jmethodID javaMethod, const char* param)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	jstring string 			= (mEnv)->NewStringUTF(param);	
	(mEnv)->CallStaticObjectMethod(javaClass, javaMethod, string);	
	
	(mEnv)->DeleteLocalRef(string);
}

void InAppBilling::CallJNIFuncSendInt(jclass javaClass, jmethodID javaMethod, int param)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	(mEnv)->CallStaticObjectMethod(javaClass, javaMethod, param);
}

int InAppBilling::CallJNIFuncInt(jclass javaClass, jmethodID javaMethod)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	return (mEnv)->CallStaticIntMethod(javaClass, javaMethod);
}

int InAppBilling::CallJNIFuncBool(jclass javaClass, jmethodID javaMethod)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	return (mEnv)->CallStaticBooleanMethod(javaClass, javaMethod);
}

int InAppBilling::CallObjJNIFuncInt(jobject javaObject, jmethodID javaMethod)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	
	return (mEnv)->CallIntMethod(javaObject, javaMethod);
}

char * InAppBilling::CallObjJNIFuncChar(jobject javaObject, jmethodID javaMethod, char* out, int MAX, const char* param, int idx)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (MAX != -1) //-1 means: NO memory space is defined to out 
		memset(out, '\0', MAX);
	jstring string 			= (mEnv)->NewStringUTF(param);
	jbyteArray byteArray 	= (jbyteArray) (mEnv)->CallObjectMethod(javaObject, javaMethod, string, idx);
	int lon = 0;
	if (byteArray)lon		= (mEnv)->GetArrayLength(byteArray);
	if (lon > 0)
	{
		if (MAX == -1)
		{
			out = new char(lon + 1);
			memset(out, '\0', lon + 1);
		}
		(mEnv)->GetByteArrayRegion(byteArray, 0, lon, (jbyte*)out);
		(mEnv)->DeleteLocalRef(byteArray);
		(mEnv)->DeleteLocalRef(string);
	}
return out;
}

char * InAppBilling::CallObjJNIFuncChar(jobject javaObject, jmethodID javaMethod, char* out, int MAX, int idx)
{
	JNIEnv* mEnv = NULL;
	acp_utils::ScopeGetEnv st(mEnv);
	if (MAX != -1) //-1 means: NO memory space is defined to out 
		memset(out, '\0', MAX);
	
	jbyteArray byteArray 	= (jbyteArray) (mEnv)->CallObjectMethod(javaObject, javaMethod, idx);
	int lon = 0;
	if (byteArray)lon		= (mEnv)->GetArrayLength(byteArray);
	if (lon > 0)
	{
		if (MAX == -1)
		{
			out = new char(lon + 1);
			memset(out, '\0', lon + 1);
		}
		(mEnv)->GetByteArrayRegion(byteArray, 0, lon, (jbyte*)out);
		(mEnv)->DeleteLocalRef(byteArray);
	}
return out;
}
#endif //#if USE_IN_APP_BILLING