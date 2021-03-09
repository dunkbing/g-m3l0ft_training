#ifndef __JAVA_DEFINES__
#define __JAVA_DEFINES__
#include "config_Android.h"
#if !RELEASE_VERSION
	#define SET_TAG(name)		static final String TAG = name
	#define DBG(tag, msg) 		android.util.Log.d(tag, __FILE__ + ": "+__LINE__+" : "+msg)
	#define ERR(tag, msg) 		android.util.Log.e(tag, __FILE__ + ": "+__LINE__+" : "+msg)
	#define INFO(tag, msg)		android.util.Log.i(tag, __FILE__ + ": "+__LINE__+" : "+msg)
	#define WARN(tag, msg)		android.util.Log.w(tag, __FILE__ + ": "+__LINE__+" : "+msg)
	#define DBG_EXCEPTION(e)	e.printStackTrace()
	#define JDUMP(tag, a)		android.util.Log.d(tag, __FILE__ + ": "+__LINE__+" : "+#a+" = " + a)

	#define TRIGGER_START_TIMER(key) 				Utils.triggerStartTimer(key)
	#define TRIGGER_END_TIMER(key) 					Utils.triggerEndTimer(key)
	#define SHOW_TIMER(key, showAll) 				Utils.showTimer(key, showAll)
	#define SHOW_TIMER_SECTION(key, start, count) 	Utils.showTimer(key, start, count)
	#define SHOW_ALL_TIMERS(showAll) 				Utils.showAllTimers(showAll)

#else
	#define SET_TAG(name)
	#define DBG(tag, msg)
	#define ERR(tag, msg)
	#define INFO(tag, msg)
	#define WARN(tag, msg)
	#define DBG_EXCEPTION(e)
	#define JDUMP(tag, a)

	#define TRIGGER_START_TIMER(key)
	#define TRIGGER_END_TIMER(key)
	#define SHOW_TIMER(key, showAll)
	#define SHOW_TIMER_SECTION(key, start, count)
	#define SHOW_ALL_TIMERS(showAll)
#endif

	#define ERR_TOAST(number) 				GameInstaller.addErrorNumber(number)
	#define ERR_TOAST_CLEAR(number) 		GameInstaller.addErrorNumber(number)

#endif
