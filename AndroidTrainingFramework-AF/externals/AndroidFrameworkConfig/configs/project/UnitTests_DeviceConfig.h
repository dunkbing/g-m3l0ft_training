#ifndef __UT_DEVICE_CONFIG_H__S
#define __UT_DEVICE_CONFIG_H__S

#define DEVICE_SPECIFIC_UT		0

#define UT_SD_CARD				"/storage/emulated/0"
#define UT_OBB_FOLDER			"/storage/emulated/0/Android/obb/com.gameloft.android.ANMP.GloftCNRL"
#define UT_DATA_FOLDER			"/storage/emulated/0/Android/data/com.gameloft.android.ANMP.GloftCNRL/files"
#define UT_PRIVATE_FOLDER		"/data/data/com.gameloft.android.ANMP.GloftCNRL/files"
#define UT_CACHE_FOLDER			"/data/data/com.gameloft.android.ANMP.GloftCNRL/cache"

#define UT_DEFAULT_IGP          "CNRL"
#define UT_GAME_NAME            "ANMP.GloftCNRL"
#define UT_INJECTED_IGP         ""
#define UT_INJECTED_SERIAL      ""

#define UT_ANDROIDID			"781ae4961b5ab46"
#define UT_SERIAL_NO			"0146BF511501A004"
#define UT_CPU_SERIAL           "0146bf511501a004"
#define UT_DEVICE_MANUFACTURER  "samsung"
#define UT_DEVICE_MODEL         "Galaxy+Nexus"
#define UT_FIRMWARE             "4.3"
#define UT_MACADDRESS           "a0:0b:ba:b4:d7:58"
#define UT_IMEI					"358350040453612"
#define UT_HDIDFV				"67b0c7f4-14a7-49de-86b6-37126d489c09"
#define UT_BUILD_PRODUCT		"yakju"
#define UT_BUILD_DEVICE			"maguro"
#define	UT_HEIGHT_INCH			2.28f
#define	UT_WIDTH_INCH			3.75f
#define	UT_HEIGHT_PX			720
#define	UT_WIDTH_PX			1196
//#define	UT_WIDTH_PX			1280


#define UT_SHARED_PREF_KEY      "testKey"
#define UT_SHARED_PREF_PNAME    "testPName"
#define UT_SHARED_PREF_INT      7
#define UT_SHARED_PREF_LONG     9223372036854775804 // Max int - 3, just because
#define UT_SHARED_PREF_BOOL     true
#define UT_SHARED_PREF_STRING   "testStringSauPula"

#define UT_CARRIER_NAME         ""
#define UT_COUNTRY              "US"
#define UT_LANGUAGE             "en"
#define UT_USER_AGENT           "Mozilla/5.0 (Linux; U; Android 4.3; en-us; Galaxy Nexus Build/JWR66Y) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
#define UT_GOOGLE_ADID			"4649de90-fe43-44cb-a387-b7dd1ab4ec1c"
#define	UT_GOOGLE_ADID_STATUS	0	//SUCCESSFULL

#define UT_VKEYBOARD_TEXT       	"Some Cician Text"
#define UT_VKEYBOARD_TEXT_2       	"Another Text"

#define UT_BATTERY_THRESHOLD    85

#define	UT_DISK_FREE_SPACE_THREASHOLD	14495514624
#define	UT_NUMBER_OF_CORES		2
#define UT_MAX_CPU_SPEED		1200000
#define	UT_DEVICE_CHIPSET		"omap4"
#define UT_DEVICE_ARCH			"0x41"
#define	UT_DEVICE_MICRO_ARCH	"0xc09"
#define	UT_AVAILABLE_RAM_MIN	5
#define	UT_MAX_RAM				694.297

#define UT_CONTROLLER_NAME		"FakeControllerName"
#define UT_CONTROLLER_EV_TYPE	5
#define UT_CONTROLLER_EV_VAL	-0.2359423

#define UT_MIN_TOKEN_SIZE		160

#define UT_ASSET_APK_FILENAME			"julia.pgm"
#define UT_ASSET_COMPARE_FILENAME		"julia.pgm"


#include "UT_Asset_Compare.h"

#endif //__UT_DEVICE_CONFIG_H__S
