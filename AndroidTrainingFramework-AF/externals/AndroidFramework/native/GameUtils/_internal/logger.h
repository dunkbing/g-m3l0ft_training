#ifndef LOGGER_H
#define LOGGER_H

#include <strings.h>
#include <android/log.h>

#define ACP_LOGGER_TAG "ACP_LOGGER"

#ifdef NDEBUG
	#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, ACP_LOGGER_TAG, __VA_ARGS__)
	#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, ACP_LOGGER_TAG, __VA_ARGS__)
#else
	#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, ACP_LOGGER_TAG, __VA_ARGS__)
	#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, ACP_LOGGER_TAG, __VA_ARGS__)
#endif
#ifdef NDEBUG
	#define LOG_DBG(...)
#else
	#define LOG_DBG(...) __android_log_print(ANDROID_LOG_INFO, ACP_LOGGER_TAG, __VA_ARGS__)
#endif


#endif // LOGGER_H
