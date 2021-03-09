#ifndef IN_APP_BILLING_CONST_H_INCLUDED
#define IN_APP_BILLING_CONST_H_INCLUDED

//Error response values of a transaction
#define	IAB_BUY_OK		0
#define IAB_BUY_CANCEL	1
#define IAB_BUY_FAIL	2
#define IAB_BUY_PENDING	3
#define IAB_BUY_REVOKE	4

//Operation values from java
#define OP_GET_STRING			0
#define OP_START_BUY_ITEM		1
#define OP_FINISH_BUY_ITEM		2
#define OP_START_GET_LIST		3
#define OP_FINISH_GET_LIST		4
#define OP_START_GET_FQC_NAME	5
#define OP_GET_GGLIVE_UID		6
#define OP_GET_CREDENTIALS		7
#define OP_FINISH_RESTORE_TRANS	8
#define OP_LOGGING_LOG_INFO		9
#define OP_GET_NEW_NOUNCE		10
#define OP_GET_APP_HDER			11
#define OP_GET_VERSION_HDER		12
#define OP_GET_PRODUCT_ID_HDER	13
#define OP_CHECK_MD5_HASH		14
#define OP_GET_ANONYMOUS_CRED	15
#define OP_GET_FED_DATA_CENTER	16

//Operation values from jni
#define OP_IAB_IS_SUPPORTED		0
#define OP_RETRIEVE_LIST		1
#define OP_TRANSACTION			2
#define OP_IAB_SHOW_DIALOG		3
#define OP_IAB_LAST_RESULTS		4
#define OP_GET_ID_VALUES		5
#define OP_GET_TYPE_VALUES		6
#define OP_GET_ITEM_ATTS		7
#define OP_GET_BILLING_ATTS		8
#define OP_GET_SHOP_ATTS		9
#define OP_SEND_CONFIRMATION	10
#define OP_SET_USER_TOKEN		11
#define OP_SET_USER_RTOKEN		12
#define OP_SET_USER_PASS		13
#define OP_GET_TYPE_WUID		14
#define OP_GET_ATT_WUID			15
#define OP_GET_BATT_WUID		16
#define OP_RESTORE_TRANS		17
#define OP_GET_COMMON_HEADERS	18
#define OP_CLOSE_WAIT_DIALOG	19
#define OP_RETRIEVE_LIST_TP		20
#define OP_FINISH_GET_LIST_TP	21



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

//constants
#define	GET_STR_CONST(x) 	a(OP_GET_STRING,x)
#define GET_FQCN(x)			a(OP_START_GET_FQC_NAME,x)

#define GET_GGI_STR()					(GGI)
#define GET_GGLIVE_UID()	a(OP_GET_GGLIVE_UID,1)
#define GET_CREDENTIALS()	a(OP_GET_CREDENTIALS,2)
#define GET_CLIENT_ID()					(CLIENTID)

#define GET_NEW_NOUNCE()		a(OP_GET_NEW_NOUNCE,1)
#define GET_APP_HDER()			a(OP_GET_APP_HDER,2)
#define GET_VERSION_HDER()		a(OP_GET_VERSION_HDER,3)
#define GET_PRODUCT_ID_HDER()	a(OP_GET_PRODUCT_ID_HDER,4)
#define GET_ANONYMOUS_CREDENTIAL()	a(OP_GET_ANONYMOUS_CRED,5)
#define GET_FED_DATA_CENTER()	a(OP_GET_FED_DATA_CENTER,6)

#if USE_IN_APP_GLOT_LOGGING
	//IAPLogLevelType
	#define IAP_LOG_TYPE_FATAL 		0
	#define IAP_LOG_TYPE_ERROR		1
	#define IAP_LOG_TYPE_WARNING	2
	#define IAP_LOG_TYPE_INFO		3
	#define IAP_LOG_TYPE_DEBUG		4
	#define IAP_LOG_TYPE_VERBOSE	5

	//IAPLogStatus
	#define IAP_LOG_STATUS_SEND     1
	#define IAP_LOG_STATUS_RECEIVE	2
	#define IAP_LOG_STATUS_ERROR	3
	#define IAP_LOG_STATUS_INFO		4
	
	#define LOGGING_LOG_INFO(level, status, data) 													IABLogging.getInstance().LogInfo(level, status, __FILE__ + "["+__LINE__+"]:"+data)
	#define LOGGING_LOG_REQUEST(level, data) 														IABLogging.getInstance().LogInfo(level, IAP_LOG_STATUS_SEND, data)
	#define LOGGING_LOG_RESPONSE(level, data) 														IABLogging.getInstance().LogInfo(level, IAP_LOG_STATUS_RECEIVE, data)
	
	#define LOGGING_APPEND_REQUEST_PARAM(url, methodType, requestName) 								IABLogging.getInstance().AppendLogRequestData(url, methodType, requestName)
	
	#define LOGGING_APPEND_REQUEST_HEADERS_BY_NV(paramName, paramValue, requestName)				IABLogging.getInstance().AppendParamsRequestHeaders(paramName, paramValue, requestName)
	#define LOGGING_APPEND_REQUEST_HEADERS(value, requestName)										IABLogging.getInstance().AppendParamsRequestHeaders(value, requestName)
	
	#define LOGGING_APPEND_REQUEST_PAYLOAD_BY_NV(paramName, paramValue, requestName)				IABLogging.getInstance().AppendParamsRequestPayload(paramName, paramValue, requestName)
	#define LOGGING_APPEND_REQUEST_PAYLOAD(value, requestName)										IABLogging.getInstance().AppendParamsRequestPayload(value, requestName)
	
	#define LOGGING_APPEND_RESPONSE_HEADERS_BY_NV(paramName, paramValue, requestName)				IABLogging.getInstance().AppendParamsResponseHeaders(paramName, paramValue, requestName)
	
	#define LOGGING_APPEND_RESPONSE_PARAM(data, requestName) 										IABLogging.getInstance().AppendLogResponse(data, requestName)
	#define LOGGING_APPEND_PARAMETER(result, paramName, paramValue) 								IABLogging.getInstance().AppendParams(result, paramName, paramValue)
	
	#define LOGGING_REQUEST_GET_INFO(requestName)													IABLogging.getInstance().GetRequestInfo(requestName)
	#define LOGGING_RESPONSE_GET_INFO(requestName)													IABLogging.getInstance().GetResponseInfo(requestName)
	#define LOGGING_REQUEST_GET_TIME_ELAPSED(requestName)											IABLogging.getInstance().GetTimeElapsed(requestName)
	#define LOGGING_REQUEST_REMOVE_REQUEST_INFO(requestName)										IABLogging.getInstance().RemoveRequestInfo(requestName)
#else
	#define LOGGING_LOG_INFO(level, status, data)
	#define LOGGING_LOG_REQUEST(level, data)
	#define LOGGING_LOG_RESPONSE(level, data)
	
	#define LOGGING_APPEND_REQUEST_PARAM(url, methodType, requestName)
	
	#define LOGGING_APPEND_REQUEST_HEADERS_BY_NV(paramName, paramValue, requestName)
	#define LOGGING_APPEND_REQUEST_HEADERS(value, requestName)
	
	#define LOGGING_APPEND_REQUEST_PAYLOAD_BY_NV(paramName, paramValue, requestName)
	#define LOGGING_APPEND_REQUEST_PAYLOAD(value, requestName)
	
	#define LOGGING_APPEND_RESPONSE_HEADERS_BY_NV(paramName, paramValue, requestName)
	
	#define LOGGING_APPEND_RESPONSE_PARAM(data, requestName)
	#define LOGGING_APPEND_PARAMETER(result, paramName, paramValue)
	
	#define LOGGING_REQUEST_GET_INFO(requestName)
	#define LOGGING_RESPONSE_GET_INFO(requestName)
	#define LOGGING_REQUEST_GET_TIME_ELAPSED(requestName)
	#define LOGGING_REQUEST_REMOVE_REQUEST_INFO(requestName)
#endif

#include "IABstrings_java.h"

//download images
#define DL_IMAGE_TIME_OUT 		10000
#define DL_IMAGE_BUFFER_SIZE	(128 * 1024)

#endif // !IN_APP_BILLING_CONST_H_INCLUDED

