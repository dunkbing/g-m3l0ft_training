#ifndef __ADEBUG_H__
#define __ADEBUG_H__

#ifndef TAG
	// #error You should define TAG before including ADebug.h
	#define TAG "GLGame"
#endif

#ifdef DEBUG_ENABLE
	#include <android/log.h>
	#include <libgen.h>
	
	#define DBG(x)			__android_log_print(ANDROID_LOG_DEBUG, 	TAG, "%s: %u %s", 		basename(__FILE__), __LINE__, x)
	#define ERR(x)			__android_log_print(ANDROID_LOG_ERROR, 	TAG, "%s: %u %s", 		basename(__FILE__), __LINE__, x)
	#define DBG_LN()		__android_log_print(ANDROID_LOG_DEBUG, 	TAG, "%s: %u", 			basename(__FILE__), __LINE__)
	#define DBG_FN()		__android_log_print(ANDROID_LOG_INFO, 	TAG, "%s: %u: %s", 		basename(__FILE__), __LINE__, __FUNCTION__)
	#define DBG_FNE()		__android_log_print(ANDROID_LOG_INFO, 	TAG, "%s: %u: END %s", 	basename(__FILE__), __LINE__, __FUNCTION__)
	#define DUMP(fmt, x)	__android_log_print(ANDROID_LOG_DEBUG, 	TAG, "%s: %u: %s "fmt, 	basename(__FILE__), __LINE__, #x, x)
	#define DUMP_D(x)		DUMP("%d", x)
	#define DUMP_S(x)		DUMP("%s", x)
	#define DUMP_F(x)		DUMP("%f", x)
	#define DUMP_X(x)		DUMP("%x", x)

	#define ASSERT(arg1)	{\
								if(!(arg1)) \
								{\
									__android_log_print(ANDROID_LOG_ERROR, TAG, "ASSERT: %s: %s: %u",__FILE__,__FUNCTION__, __LINE__);\
								}\
							}
#else
	#define DBG(x)
	#define ERR(x)
	#define DBG_LN()		
	#define DBG_FN()
	#define DBG_FNE()
	#define DUMP(fmt, x)	
	#define DUMP_D(x)		
	#define DUMP_S(x)		
	#define DUMP_F(x)		
	#define DUMP_X(x)		
	#define ASSERT(arg1)	
#endif

#endif		//__ADEBUG_H__
