#ifndef __IN_APP_BILLING__
#define __IN_APP_BILLING__
#include <jni.h>
#include <map>
#include <string>
#ifndef  __cplusplus
	typedef enum {false, true} bool;
#endif
#define IAB_VERSION "2.0.9133"

#define USE_SECURITY_SIGNATURE_VERIFICATION_WITH_FEED 	0
#if USE_IAPV2_LEGACY_WRAPPER
	#define USE_SECURITY_SIGNATURE_VERIFICATION_WITH_FEED 	0//1 Temporally disabled
#endif

//#define DEBUG_NATIVE
#include <android/log.h>
#include <libgen.h>

#if defined DEBUG_NATIVE
	#define TAG "InAppBilling"
	#define DBG(...) 		__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__ )
	#define DBG_LN()		__android_log_print(ANDROID_LOG_INFO, TAG, "%s: %u", basename(__FILE__), __LINE__)
	#define DBG_FN()		__android_log_print(ANDROID_LOG_INFO, TAG, "%s: %s", basename(__FILE__), __FUNCTION__)
	#define DBG_FNE()		__android_log_print(ANDROID_LOG_INFO, TAG, "%s: END %s", basename(__FILE__), __FUNCTION__)
	#define DUMP(fmt, x)	__android_log_print(ANDROID_LOG_INFO, TAG, "%s: %u: %s "fmt, basename(__FILE__), __LINE__, #x, x)
	#define DUMP_D(x)		DUMP("%d", x)
	#define DUMP_U(x)		DUMP("%lu", x)
	#define DUMP_S(x)		DUMP("%s", x)
	#define DUMP_F(x)		DUMP("%f", x)
	#define DUMP_X(x)		DUMP("%x", x)
#else
	#define DBG(...)
	#define DBG_LN()
	#define DBG_FN()
	#define DBG_FNE()
	#define DUMP(fmt, x)
	#define DUMP_D(x)
	#define DUMP_U(x)
	#define DUMP_S(x)
	#define DUMP_F(x)
	#define DUMP_X(x)
#endif
	#define IAB_SHOW_VERSION()		__android_log_print(ANDROID_LOG_INFO, "IAP", "Version [%s]", IAB_VERSION )

#define	IAB_BUY_OK		0
#define IAB_BUY_CANCEL	1
#define IAB_BUY_FAIL	2
#define IAB_BUY_PENDING	3
#define IAB_BUY_REVOKE	4

#define STATE_NEVER_BUY			0
#define WAITING_FOR_GOOGLE 		1
#define WAITING_FOR_BOKU		2
#define WAITING_FOR_PSMS		3
#define WAITING_FOR_GAMELOFT	4
#define WAITING_FOR_GAME		5
#define WAITING_FOR_SHENZHOUFU	6
#define STATE_SUCCESS			7
#define STATE_FAIL				8
#define STATE_USER_CANCEL		9

typedef unsigned long long iab_uint64;

#ifdef  __cplusplus
extern "C" {
#endif

struct IAB_STORE_ITEM
{
	std::string 	content_id;
	std::string		content_type;
	std::map<std::string, std::string>	m_propertiesMap;
	std::string		getProperty(const char* key)
	{
		if(m_propertiesMap.find(key) != m_propertiesMap.end())
		{
			return m_propertiesMap[key];
		}
		else
		{
			return "";
		}
	}
};

void 		InAppBilling_GetItemList(const char* type);
int 		InAppBilling_getTotalItems();
std::string	InAppBilling_GetHeaders();
void 		InAppBilling_FillAttributeArrayByName(char*** array, const char* att) __attribute__ ((deprecated));
void 		InAppBilling_FillIdArray(char*** array) __attribute__ ((deprecated));
void 		InAppBilling_FillTypeArray(char*** array) __attribute__ ((deprecated));
void 		InAppBilling_FillBillingAttArrayByName(char*** array, const char* att) __attribute__ ((deprecated));
void 		InAppBilling_FreeArrayMem(char** array) __attribute__ ((deprecated));
void 		InAppBilling_BuyItem(const char* uid);
std::string	InAppBilling_GetShopAttributeByName(const char* name);
void 		InAppBilling_RestoreTransactions();

std::string	InAppBilling_GetItemTypeByID(const char* uid);
std::string	InAppBilling_GetAttByID(const char* uid, const char* name);
std::string	InAppBilling_GetBillingAttByID(const char* uid, const char* name);

void 		InAppBilling_GetItemInfoByID(const char* uid, IAB_STORE_ITEM& dst);
//void 		InAppBilling_FreeItemInfo(IAB_STORE_ITEM* dst);

IAB_STORE_ITEM*		InAppBilling_GetItemInfoArray();
void				InAppBilling_FreeItemInfoArray(IAB_STORE_ITEM*);


void 	InAppBilling_SendNotifyConfirmation(const char* notifyId);


#if GOOGLE_STORE_V3 || BOKU_STORE || BAZAAR_STORE || YANDEX_STORE
void 	InAppBilling_ShowPendingDialog();
int		InAppBilling_GetState();
int 	InAppBilling_GetLastResults(char* desc);
#endif

#ifdef  __cplusplus
}
#endif

#endif //__IN_APP_BILLING__