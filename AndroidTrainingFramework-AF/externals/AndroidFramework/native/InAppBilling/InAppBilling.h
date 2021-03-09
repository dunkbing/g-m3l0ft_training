#ifndef IN_APP_BILLING_H_INCLUDED
#define IN_APP_BILLING_H_INCLUDED

#include "config_Android.h"
#include "stdio.h"
#include "string.h"

#include "IABilling.h"

#define KEY_SIZE		32
#define INDEX_SIZE		8
#define RESULT_SIZE		8
#define RESPONSE_BUFFER	512
#define BUFFER_STR_SIZE	512


#ifdef  __cplusplus
extern "C" {
#endif

void	JNI_FUNCTION(iab_InAppBilling_nativeInit) (JNIEnv*  env, jclass thiz, jobject context);
void	JNI_FUNCTION(iab_InAppBilling_nativeSetContext) (JNIEnv*  env, jclass iabilling, jobject context);
void	JNI_FUNCTION(iab_InAppBilling_nativeSetIABObject) (JNIEnv*  env, jclass iabilling, jobject iab);
jobject JNI_FUNCTION(iab_InAppBilling_nativeSendData) (JNIEnv*  env, jclass iabilling, jobject bundle);

#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
jlong 		JNI_FUNCTION(iab_s_gn) (JNIEnv*  env, jclass security);
void 		JNI_FUNCTION(iab_s_rn) (JNIEnv*  env, jclass security, jlong nonce);
jboolean	JNI_FUNCTION(iab_s_in) (JNIEnv*  env, jclass security, jlong nonce);
jobject 	JNI_FUNCTION(iab_s_gk) (JNIEnv*  env, jclass security, jstring strek);
int 		JNI_FUNCTION(iab_s_bq) (JNIEnv*  env, jclass security, jobject oPublicKey, jstring jsSignedData, jstring jsSignature);
#endif //#if GOOGLE_STORE_V3
void 	JNU_ThrowByName(JNIEnv* env, const char* name, const char* msg);
void 	IAB_ReplaceChars(char* str, const char sc, const char rc);

void 	uI64ToChar(char* dest, int size, iab_uint64 src);
void 	charToUI64(iab_uint64* dest, int size,  const char* src);

void	InAppBilling_GetItemListCB(const char* list, int nit);
void	InAppBilling_GetItemListCBTP(const char* list, int nit);
void	InAppBilling_BuyItemCB(const char* uid, int buyError, const char* notifyId, const char* eComTxId);
void	InAppBilling_RestoreTransactionCB(const char* id);
void	InAppBilling_LogInfo(int level, int status, const char* Data);


const char* InAppBilling_GetGLLiveUser();
const char* InAppBilling_GetCredentials();

#ifdef  __cplusplus
}
#endif


class InAppBilling
{
	public:
		static void 	init(jclass game, jobject context);
		static void 	setContext(jobject context);
		static void 	setIABObject(jobject obj);
		static jobject 	nativeSendData(jobject bundle);
		static void 	setJavaVM(JavaVM* javaVM);
		static void 	launchInAppBilling(const char* type);
		static void 	getItemList(const char* type);
		static void 	getItemListTP(const char* type);
		static int  	getTotalItems();
		static std::string getHeaders();
		static void 	fillAttributeArrayByName(char*** array, const char* att) __attribute__ ((deprecated));
		static void 	fillIdArray(char*** array) __attribute__ ((deprecated));
		static void 	fillTypeArray(char*** array) __attribute__ ((deprecated));
		static void 	fillBillingAttArrayByName(char*** array, const char* att) __attribute__ ((deprecated));
		static void 	buyItem(const char* uid);
		static jobject 	getData(jobject bundle);
		static void 	showDialog();
		static int 		getState();
		static int		getLastResults(char* desc);
		static void		sendNotifyConfirmation(const char* notifyId);
		static const char* 	readChar(char* dst, int size, int idx);
		static std::string 	getShopAttributeByName(const char* name);
		static void		restoreTransactions();
		
		static std::string	getItemTypeByID(const char* uid);
		static std::string	getAttByID(const char* uid, const char* name);
		static std::string	getBillingAttByID(const char* uid, const char* name);
		static void 	getItemInfoByID(const char* uid, IAB_STORE_ITEM& dst);
		static IAB_STORE_ITEM* 	getItemInfoArray();
		
	#if GOOGLE_STORE_V3	|| BAZAAR_STORE || YANDEX_STORE
		static void 	init_sct(jclass security);
		static jlong 	generateNonce(jclass security);
		static void		removeNonce(jclass security, jlong nonce);
		static jboolean	isNonceKnown(jclass security, jlong nonce);
		static jobject 	gk(jclass security, jstring strek);
	#endif //#if GOOGLE_STORE_V3
		

	private:
		static void			bundlePutString(const char* key, const char* value, jobject bundle);
		static jbyteArray 	bundleReadBArray(const char* key, jobject bundle);
		static void 		bundlePutBArray(const char* key, jbyteArray value, jobject bundle);
		static int 			bundleReadInt(const char* key, jobject bundle);
		static void			bundlePutInt(const char* key, int value, jobject bundle);
		static iab_uint64 	bundleReadLong(const char* key, jobject bundle);
		static void			bundlePutLong(const char* key, iab_uint64 value, jobject bundle);
		static jboolean 	bundleContainsKey(const char* key, jobject bundle);
		static jobject 		newBundle(JNIEnv* mEnv);
		static void 		bundleClear(jobject bundle);
		static jstring 		charToString(const char* str);
		
		static void 	CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX);
		static void 	CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX, const char* param);
		static char*	CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX, const char* param, int idx);
		static char*	CallJNIFuncChar(jclass javaClass, jmethodID javaMethod, char* out, int MAX, int idx);
		static void 	CallJNIFuncSendChar(jclass javaClass, jmethodID javaMethod, const char* param);
		static void 	CallJNIFuncSendInt(jclass javaClass, jmethodID javaMethod, int param);
		static int  	CallJNIFuncInt(jclass javaClass, jmethodID javaMethod);
		static int  	CallJNIFuncBool(jclass javaClass, jmethodID javaMethod);

		static int		CallObjJNIFuncInt(jobject javaObject, jmethodID javaMethod);
		static char*	CallObjJNIFuncChar(jobject javaObject, jmethodID javaMethod, char* out, int MAX, const char* param, int idx);
		static char*	CallObjJNIFuncChar(jobject javaObject, jmethodID javaMethod, char* out, int MAX, int idx);
		
		static jclass 		cIABilling;
		static jobject 		oIABilling;
		static jmethodID 	mTotalItems;
		//static jmethodID 	mAttribute;
		//static jmethodID 	mItemId;
		static jmethodID 	mGetData;
		static jmethodID 	mGetState;
		
		static jclass 		cIntent;
		static jobject 		oIntent;
		static jmethodID 	mIntentInit;
		static jmethodID 	mIntentSetClass;
		static jmethodID 	mIntentPutExtra;
		
		static jclass 		cGame;
		static jclass 		cContext;
		static jobject		oGameActivity;
		
		static jclass		cBundle;
		static jmethodID	mBundleInit;
		static jmethodID	mBundlePutString;
		static jmethodID	mBundleGetBArrays;
		static jmethodID	mBundlePutBArrays;
		static jmethodID	mBundleGetInt;
		static jmethodID	mBundlePutInt;
		static jmethodID	mBundleGetLong;
		static jmethodID	mBundlePutLong;
		static jmethodID	mBundleContains;
		static jmethodID	mBundleClear;
		
		static JavaVM* 		mJavaVM;
	
	#if GOOGLE_STORE_V3 || BAZAAR_STORE || YANDEX_STORE
		static jclass 		cSecurity;
		
		static jclass 		cSRandom;
		static jobject		oSRandom;
		static jmethodID	mNextLong;
		
		static jclass 		cMpNonces;
		static jobject		oMpNonces;
		static jmethodID	mNonceAdd;
		static jmethodID	mNonceRemove;
		static jmethodID	mNonceContains;
		
		static jclass 		cLong;
		static jmethodID	mLongInit;
		
		static jclass		cB64;
		static jmethodID	mDecode;
		
		static jclass		cKeyFactory;//KeyFactory
		static jobject		oKeyFactory;
		static jmethodID	mKFGetInstance;
		static jmethodID	mKFGeneratePublic;
		
		static jclass		cX509EKS;//X509EncodedKeySpec
		static jobject		oX509EKS;
		static jmethodID	mX509Init;
		
		static jobject		oPK;//PublicKey
	#endif //#if GOOGLE_STORE_V3
};

#endif // !IN_APP_BILLING_H_INCLUDED

