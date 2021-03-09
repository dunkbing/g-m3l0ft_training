@echo off

rem **********************
rem Game Specific Configuration
rem **********************
rem Android API Level
set TARGET_API_LEVEL=15
set API_LEVEL_NAME=android-%TARGET_API_LEVEL%
set USE_YAWOAP=1
set USE_GRADLE_AND_J2A=1

rem **********************
rem The NATIVE api level can differ from TARGET_API_LEVEL
rem **********************
set NATIVE_API_LEVEL=14


rem Version numbers
set VERSION_MAJOR=1
set VERSION_MINOR=0
set VERSION_BUILD=1
set VERSION_MICRO=0
set VERSION_LETTER=a
set VERSION_NANO=0
set VERSION=%VERSION_MAJOR%%VERSION_MINOR%%VERSION_BUILD%
set GC_LETTER=a

rem *NOTE: the "Compile" tag indicate if Java/C is need to recompile by change these values.

set USE_NETWORK_STATE_LISTENER=0

rem **********************
rem GLLegacy settings
rem set USE_GLLEGACY=1
rem set USE_NATIVE_ACTIVITY=1       to use the full native Android(aka native activity)
rem **********************



rem *********************
rem Installer
rem Compile: Java
rem *********************
set USE_INSTALLER=0
set SIMPLIFIED_PN=0
set USE_SIMPLIFIED_INSTALLER=0
set USE_INSTALLER_ADS=0
set USE_BLUE_LOGO=0
set USE_MDL=0
rem set USE_DYNAMIC_DOWNLOAD_LINK=0
set USE_BEAM=0
set USE_VIRTUAL_KEYBOARD=1
set ACP_UT=0
set RELEASE_VERSION=0

rem *********************
rem Market place Installer
rem *********************
set USE_MARKET_INSTALLER=1
	rem When Alpha or Beta, set USE_GOLD_SERVER to 0, and change DOWNLOAD_URL to the test server
	set USE_GOLD_SERVER=1
	rem Compile: Java & Native
	set GOOGLE_DRM=0
	set GOOGLE_MARKET_DOWNLOAD=1
	set UNZIP_DOWNLOADED_FILES=0
set USE_JP_HD_SUBSCRIPTION=0

set AMP_EPK="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsYn2/6VQsECAQyqa5SZHczex2FOHpbPohJFVkujtX6vyElqRYxDPTenMz1gWCoGyPIETMiZMKA5Wop1HR0XRMncFOl5RNEt+/XNPqI7cH60GMj2NHcqpBPOQnJZQLV/3eXdJ43zA+onycjnA5f9UgqKPxCvqV8uKUzMaxldUOgkskqdh0MRMIB9WCdcZWh6By6rBvtX69Lg2XuG770ANNCypdqG4yoYt3gE29JFbqLuMFv/oA5lPwfUMOseOrwsFSSJufIFk0hZmRS4w6DearNNcImdzlgi8qqYrMw2FiNn2TC2h0mOp14azOEjetOgzV6GXS6FyAijwWcQ9sKrTYQIDAQAB"

rem **************
rem  Amazon Store
rem **************
set USE_AMAZONMP=0

rem ****************************
rem Data and server information
rem ****************************
rem remember to change .jar to .zip when you upload file into gameloft server
rem remember to change .jar to .amz when you upload file into admin.gameloft.org server
set DATA_DOWNLOAD_EXT=amz

rem Gameloft shop builds
rem set DATA_DOWNLOAD_SERVER=http://ota.sai.gameloft.com/gmz/sai/
REM set DATA_DOWNLOAD_SERVER=http://ota.mex.gameloft.com/gmz/ntr/
set DATA_DOWNLOAD_SERVER=http://test.gameloft.net/gmz/ntr/
rem set DATA_DOWNLOAD_SERVER=http://dl.gameloft.com/hdplus/android/


rem Google Market Place and Dynamic Download builds
if "%USE_GOLD_SERVER%"=="0" (
	rem Using the Gameloft internal test server, only when Alpha or Beta period
	set DOWNLOAD_URL=http://dl.gameloft.com/partners/androidmarket/d.cdn_test.php
) else (
	rem Using the Akamai server
	set DOWNLOAD_URL=http://dl.gameloft.com/partners/androidmarket/d.cdn.php
)

rem *************************
rem DRM, choose only one below
rem *************************
rem Verizon DRM
rem Compile: Java
set VERIZON_DRM=0
set TEST_VERIZON_DRM=0

rem Gameloft DRM
rem Compile: Java & Native
set GLOFT_DRM=0

rem TMobile DRM
rem Compile: Java
set TMOBILE_DRM=0
REM TMO_ITEM_ID value depends on the game you're working.
REM For Example: set TMO_ITEM_ID="gameloft01"
set TMO_ITEM_ID=""
REM NOTE: ask the producer to check what External ID will have the game.
 
rem SKT DRM
set USE_SKT_DRM=0
set SKT_DRM_AID=""

rem LGU DRM
set USE_LGU_DRM=0
set LGU_DRM_AID=""

rem LG World
set USE_LGW_DRM=0
set FOR_LGW_GLOBAL=1
rem NOTE: for Korea, please set FOR_LGW_GLOBAL=0
				
rem KT DRM
set USE_KT_DRM=0

rem SAMSUNG_ZIRCONIA_DRM
set USE_ZIRCONIA_DRM=0

rem Optus DRM for Australia client
set USE_OPTUS_DRM=0

rem Pantech DRM for Australia client
set USE_PANTECH_ARM=0
set PANTECH_STORE_CID=""

rem Orange DRM
rem Compile: Java
set ORANGE_DRM=0

rem HEP Antipiracy
set USE_HEP_ANTIPIRACY=0
set USE_HEP_PACKINFO=0
REM set SUPPORTED_MODELS=LT15i,LT15a
REM set SUPPORTED_DEVICE_NAMES=Sony Ericsson Xperia Arc
set SUPPORTED_MANUFACTURER=Sony Ericsson,Sony Ericsson

rem *********************
rem GLLive
rem Compile: Java
rem *********************
set USE_GLLIVE=0

rem *********************
rem GLLive HTML5
rem Compile: Java
rem *********************
set USE_GLLIVE_HTML5=0
set GLIVE_ANMP=1
set GLIVE_SKT=0
set GLIVE_KT=0
set GLIVE_GL_SHOP=0
set GLIVE_DOCOMO=0
set GLIVE_KDDI=0
set GLIVE_SAMSUNG=0
set GLIVE_ORANGE=0
set GLIVE_CYRUS=0

rem *********************
rem IGP HTML
rem Compile: Java
rem *********************
set USE_IGP_ACTIVITY=0
set USE_IGP_FREEMIUM=1
set IGP_SKT=0
set USE_IGP_REWARDS=0

set USE_DIRECT_IGP=0

set USE_HID_CONTROLLER=0
rem *********************
rem Ads Server
rem Compile: Java
rem *********************
set USE_ADS_SERVER=0
set ADS_USE_TAPJOY=0
	set ADS_TAPJOY_APP_ID=
	set ADS_TAPJOY_SECRET_KEY=
set ADS_USE_ADCOLONY=0
	set ADS_ADCO_APP_ID=
	set ADS_ADCO_ZONE_ID_1=
	set ADS_ADCO_ZONE_ID_2=
set ADS_USE_FLURRY=0
	set ADS_FLURRY_ID=
	set ADS_FLURRY_INTERSTITIAL_ADSPACE=
	set ADS_FLURRY_FREECASH_ADSPACE=
	set ADS_FLURRY_USE_LOCATION=0
set ADS_USE_BURSTLY=0
	set ADS_BURSTLY_PUB_ID=
	set ADS_BURSTLY_BANNER_ZONE_ID=
	set ADS_BURSTLY_INTERSTITIAL_ZONE_ID=
	set ADS_BURSTLY_OFFERWALL_ZONE_ID=
set ADS_USE_MOPUB=0
	set ADS_MOPUB_BANNER_ID=
	set ADS_MOPUB_INTERSTITIAL_ID=
set ADS_USE_YUME=0
	set ADS_YUME_SERVER_URL=
	set ADS_YUME_DOMAIN_ID=
set ADS_USE_CHARTBOOST=0
	set ADS_CHARTBOOST_APP_ID=
	set ADS_CHARTBOOST_APP_SIG=


rem **********************
rem Welcome Screen
rem **********************
set USE_WELCOME_SCREEN=0
set USE_WELCOME_SCREEN_CRM=0
set USE_HOC_SCREEN=0


rem *********************
rem Video Player
rem Compile: Java
rem *********************
set USE_VIDEO_PLAYER=0

rem *********************
rem Billing system
rem Compile: Java & Native
rem *********************
set USE_BILLING=0
	set USE_BOKU_FOR_BILLING=0
	set USE_BILLING_FOR_CHINA=0
	set GL_CHANNEL=0

rem use a sim error message to send user to gameloft shop if there is no SIM in device and evade CC purchase
set USE_SIM_ERROR_POPUP=0
rem in case SIM error message is used add the shop operator target here example BFL4 (latin America R 4)
set SHOP_OPERATOR_TARGET=BFL4
	
set GL_PROFILE_TYPE=4
set GL_BILLING_VERSION=1
set USE_TNB_CC_PROFILE=1
	set USE_VOUCHERS=0

rem *********************
rem In App Billing system
rem Compile: Java & Native
rem *********************
set USE_IN_APP_BILLING=0
set USE_IN_APP_BILLING_CRM=0
set IN_APP_PURCHASE_LIB_CRM_PATH=%~dp0..\..\libs\inapp_purchase
	if "%RELEASE_VERSION%"=="0"	set USE_IN_APP_GLOT_LOGGING=0
	set USE_IN_APP_GLOT_LOGGING=0
	set AMAZON_STORE=0
	set GOOGLE_STORE=0
	set GOOGLE_STORE_V3=0

	REM set /A ADD_CRM_GOOGLE=%USE_IN_APP_BILLING_CRM%%GOOGLE_STORE%
	REM IF %ADD_CRM_GOOGLE% EQU 11 (
		REM set GOOGLE_STORE=0
		REM set GOOGLE_STORE_V3=1
	REM )
Rem Hoa.nt add version name for IGP
set GAME_VERSION_NAME_IGP=%VERSION_MAJOR%.%VERSION_MINOR%.%VERSION_BUILD%%VERSION_LETTER%
	
if "%GOOGLE_STORE%"=="1" (
	set VERSION=%VERSION%%VERSION_MICRO%
) else (
	if "%GOOGLE_STORE_V3%"=="1" (
		set VERSION=%VERSION%%VERSION_MICRO%
	)
)	
	

	set GAMELOFT_SHOP=0
		set USE_MTK_SHOP_BUILD=0
		set USE_ALIPLAY_WAP=0
		set USE_UMP_R3_BILLING=0
		set BOKU_STORE=0
		set PAYPAL_STORE=0
			set TEST_PAYPAL_STORE=0
		set SHENZHOUFU_STORE=0
	set SKT_STORE=0
		set TEST_SKT_STORE=0
	set KT_STORE=0
		set KT_TABLET_API=0
	set PANTECH_STORE=0
	set OPTUS_STORE=0
	set VZW_STORE=0
		set VZW_SCM=0
		set VZW_MTX=0
		if "%VZW_MTX%"=="1" (
			set VZW_APP_KEYWORD=GAMELOFTTEST
		)
		if "%VZW_SCM%"=="1" (
			set SCM_TEST_ENVIRONMENT=0
		)
	set SAMSUNG_STORE=0
set ITEMS_STORED_BY_GAME=1
set USE_IAP_VALIDATION_BETA_SERVER=0
set USE_PHONEBILL_AS_DEFAULT_OPTION=0

set USE_AUTOROTATE=0

set USE_KDDI_GIFTING=0

rem *************************
rem Other features
rem *************************
set USE_OPENGLES_20=1


rem *************************
rem PSS: Splash Promotional Screen
rem Compile: Java
rem *************************
set USE_PSS=0
rem Add checkbox to let user disable permanently the PSS
set PSS_USE_DISABLE_OPTION=1
rem Enable Verizon Version for PSS
set PSS_VZW=0


rem *************************
rem AFile: Use resources in APK
rem Compile: Native & Java
rem *************************
set USE_AFILE=0

rem *************************
rem AssetReader: Use resources in APK
rem Compile: Native
rem *************************
set USE_ASSET_READER=0




set ENABLE_USER_LOCATION=0

rem *************************
rem Game's config
rem *************************
set GL_PRODUCT_ID=1780
set GAME_NAME=Controller
set GAME_NAME_STR="Controler Test HD+"
set GGI=53959

rem For non-demo games (PUB, MKP, ...), GL_DEMO_CODE must be the same as GGC_GAME_CODE.
rem For demo games, GL_DEMO_CODE values are GXXX for HEP HD+ and HXXX for HRP HD+.
set GL_DEMO_CODE=CNRL
set GGC_GAME_CODE=CNRL
set MAIN_CLASS_NAME=Gloft%GGC_GAME_CODE%
rem The main activity name, for now let's use Game as default
set CLASS_NAME=MainActivity

rem ANMP, AMUK, GAND, AMJP, AMEU
set OPERATOR=ANMP

rem ML, EN, UK, FR, DE, SP, IT, JP, BR, PT
set LANG=ML

rem [manufacturer]_[model]
set PHONE=Samsung_GT_i9100

rem IGP, NONIGP
if "%USE_IGP_ACTIVITY%"=="1" (
	set IGP=IGP
) else (
	if "%USE_IGP_FREEMIUM%"=="1" (
		set IGP=IGP
	) else (
		set IGP=NONIGP
	)
)

rem GLLIVE
set GLLIVE=
if "%USE_GLLIVE%"=="1" set GLLIVE=GLLive
if "%USE_GLLIVE_HTML5%"=="1" set GLLIVE=GLLive

if "%GLLIVE%"=="" (
	set APP_NAME=%GAME_NAME%_%PHONE%_%IGP%_%OPERATOR%_%LANG%_MP_MS
) else (
	set APP_NAME=%GAME_NAME%_%PHONE%_%IGP%_%OPERATOR%_%LANG%_MP_MS_%GLLIVE%
)

rem *************************
rem PORTAL_CODE 
rem codes the shop for which 
rem the game is released 
rem *************************
rem Example for Android Marketplace: 
rem set "PORTAL_CODE=google_market"  
rem *****************************************************
rem * Real Shop				* 	Code					*
rem *****************************************************
rem * Android Marketplace	* 	google_market			*
rem * GL Shop				* 	gl_shop					*
rem * SKT					* 	kr_skt					*
rem * KT					* 	kr_kt					*
rem * KR-LGU+				* 	kr_lguplus				*
rem * KDDI					* 	jp_kddi					*
rem * DOCOMO				* 	jp_docomo				*
rem * Verizon				* 	us_verizon				*
rem * Amazon				* 	amazon					*
rem * Korean Google Market	* 	korean_google_market	*
rem * LG World				* 	lg_world   				*
rem * Au-Optus				* 	au_optus   				*
rem * Samsung A-store		* 	samsung_a_store   		*
rem * Global Vodafone		* 	global_vodafone   		*
rem * GetJar				*	kr_lguplus				*
rem * Pantech App Store		*	pantech_app_store		*
rem * Sprint WapShop		*	sprint_wapshop			*
rem * Orange				*	orange					*
rem * Meizu Mstore			*	meizu_mstore			*
rem *****************************************************
set PORTAL_CODE=google_market
set SHOP_TYPE=googleplay

rem ****************************
rem Google Analytics Tracking
rem ****************************
rem USE_GOOGLE_ANALYTICS_TRACKING: 1 if you want to use Google Analytics Tracking, otherwise 0. Default: 0
set USE_GOOGLE_ANALYTICS_TRACKING=1
rem GA_TRACKING_ID: The Google Analytics tracking ID to which to send your data. You can disable your tracking by not providing this value.
rem Example: set GA_TRACKING_ID="UA-xxxxxxxx-y"
set GA_TRACKING_ID=UA-33292614-62


rem *************************************
rem Push Notification
rem Compile: Java & Native
rem *************************************
set USE_PUSH_NOTIFICATION=0
	
	rem 1 to remove all current online interactivity from the PN library, only Local PN's feature and GCM token delivery will be available on library, your app should use GAIA library to handle interactivity features.
	rem 0 to keep using PN library as usual for oldest projects, new ones should use GAIA to handle interactivity.
	set REMOVE_DEPRECATED_API_FEATURES=0
	
	rem Only for Hermes: Select pandora server 
	rem	http://valpha.gameloft.com:20000
	rem	http://vbeta.gameloft.com:20000
	rem	http://vgamma.gameloft.com:20000
	rem	http://vgold.gameloft.com:20000
	rem set PANDORA_URL=http://vbeta.gameloft.com:20000
	set PANDORA_URL=http://vgold.gameloft.com:20000

	rem Only for Hermes: CLIENTID The unique identifier for the client software making the
	rem request (It must be a string containing product ID, GGI, build version, and platform 
	rem with a colon separating the values like this "1094:50065:2.1.0:android")
	set CLIENTID=%GL_PRODUCT_ID%:%GGI%:%VERSION_MAJOR%.%VERSION_MINOR%.%VERSION_BUILD%%VERSION_LETTER%:android:%SHOP_TYPE%

	if not "%DOWNLOAD_SOURCE%"=="" (
		set CLIENTID=%GL_PRODUCT_ID%:%GGI%:%VERSION_MAJOR%.%VERSION_MINOR%.%VERSION_BUILD%%VERSION_LETTER%:android:%DOWNLOAD_SOURCE%
	)
	
	rem Use Amazon PN's for Kindle devices
	set USE_PUSH_NOTIFICATION_ADM=0
	rem The API Key is provided by Amazon for each project
	set ADM_API_KEY=0
	
	rem Used for real time PN logging using GLOT library.
	rem Note: Make sure your project currently include GLOT library before enabling this feature.
	set ACTIVATE_REAL_TIME_PN_LOGGING=0
rem *************************************


rem Application info and JNI function
set APP_PACKAGE=com.gameloft.android.%OPERATOR%.%MAIN_CLASS_NAME%
set JNI_FUNCTION=Java_com_gameloft_android_%OPERATOR%_%MAIN_CLASS_NAME%_
set JNI_CLASSES=com/gameloft/android/%OPERATOR%/%MAIN_CLASS_NAME%
set OBB_MAIN_FILE=main.sa2.Asphalt8.obb


rem application folders
rem  internal device storage for game:		"/data/data/%APP_PACKAGE%"
rem  default device sd card path:			"/sdcard/gameloft/games/%MAIN_CLASS_NAME%"
rem  some devices has external/internal sd	"/sdcard/external_sd/gameloft/games/%MAIN_CLASS_NAME%"
set SAVE_FOLDER="/data/data/%APP_PACKAGE%"
set SD_FOLDER="/sdcard/gameloft/games/%MAIN_CLASS_NAME%"

REM **********************************************************************
REM List of save files created for game.
REM used for push and pull save files from device
REM Ex.
REM set SAVE_FILE_LIST=profile1.sav profileInit.sav profile1.bak
REM set SAVE_FILE_LIST=save.sav savecarrier.sav save.bak savecarrier.bak
set SAVE_FILE_LIST=


rem Download data files
set DOWNLOAD_FILE_NAME_PVR=%GAME_NAME%_%PHONE%_PVRT.%DATA_DOWNLOAD_EXT%
set DOWNLOAD_FILE_NAME_ETC=%GAME_NAME%_%PHONE%_ETC.%DATA_DOWNLOAD_EXT%
set DOWNLOAD_FILE_NAME_ATC=%GAME_NAME%_%PHONE%_ATC.%DATA_DOWNLOAD_EXT%
set DOWNLOAD_FILE_NAME_DXT=%GAME_NAME%_%PHONE%_DXT.%DATA_DOWNLOAD_EXT%

rem ********************************************
rem 	Validating params
rem ********************************************

rem next params need USE_INSTALLER
if "%USE_INSTALLER%" == "0" (
	set USE_MARKET_INSTALLER=0
	set USE_TRACKING_FEATURE_INSTALLER=0
	set GOOGLE_DRM=0
	set VERIZON_DRM=0
	set USE_SKT_DRM=0
	set USE_LGU_DRM=0
	set USE_KT_DRM=0
	set USE_ZIRCONIA_DRM=0
	set USE_HEP_ANTIPIRACY=0
)

if "%USE_MARKET_INSTALLER%"=="1" (
	REM deprecated USE_INSTALL_REFERRER now activated by USE_MARKET_INSTALLER
	REM set USE_INSTALL_REFERRER=1
	set PORTAL_CODE=google_market
	set GLIVE_ANMP=1
	set GLIVE_GL_SHOP=0
	set GLIVE_SKT=0
	set GLIVE_KT=0
	set GLIVE_DOCOMO=0
	set GLIVE_KDDI=0
	set GLIVE_SAMSUNG=0
	set GLIVE_ORANGE=0
	set GLIVE_CYRUS=0
)
rem HEP should check daily for updates
if "%AUTO_UPDATE_HEP%"=="1" (
	set UPDATE_NEW_VERSION_DAYS=1
)

if not "%USE_ADS_SERVER%"=="1" (
	set ADS_USE_FLURRY=0
)

rem amazon devices don't support multithreading downloads
if "%PORTAL_CODE%"=="amazon" (
	set MAX_DOWNLOAD_THREADS=1
)



set USE_DATA_SHARING=1




rem ********************************************
rem JNI folder and folder with Java classes    *
rem customized for GLF and GLLegacy            *
rem ********************************************
if "%USE_GLLEGACY%"=="1" (
	
	set JNI_FOLDER=%ANDROID_PORT_TEMPLATE%\..\..\GLLegacy\AndroidPortTemplate\src\jni
	set APK_SRC_FOLDER=%ANDROID_PORT_TEMPLATE%\..\..\GLLegacy\AndroidPortTemplate\src\java
	
	if "%USE_GLF_GL2JNIACTIVITY%"=="1" (
		set GLF_JNI_FOLDER=%ANDROID_PORT_TEMPLATE%\..\..\glf\projects\android\jni
		set GLF_APK_SRC_FOLDER=%ANDROID_PORT_TEMPLATE%\..\..\glf\projects\android\src\com\gameloft\glf
			
	)

	set PREPROCESS_USING_INCLUDE_FILE=%~dp0..\..\GLLegacy\src\GLLegacy\GameEngineConfig.h
	
)

if "%NATIVE_API_LEVEL%"=="" (
	set NATIVE_API_LEVEL=9
)
if %NATIVE_API_LEVEL% LSS 9 (
	set MAKEFILE_XML=%ANDROID_FRAMEWORK_CONFIG%sln2gcc.android-8.xml
)

if "%USE_GOOGLE_ANALYTICS_TRACKING%" == "1" (
	rem GA_APP_DEBUG: true if the you want to test and debug Google Analytics, otherwise false.
	if "%RELEASE_VERSION%" == "0" (
		set GA_APP_DEBUG=true
	) else (
		set GA_APP_DEBUG=false
	)
)

rem *********************************************
rem * Hide the soft key buttons 				*
rem * and display the three small dots			*
rem *********************************************
set USE_LOW_PROFILE_MENU=1
rem @hide
set GAME_ORIENTATION_LANDSCAPE=1


rem *****************************************************************************************
rem * Very important MARK ********** Do not remove it! **************************************
rem *****************************************************************************************
rem *****************************************************************************************
rem *****************************************************************************************
rem *****************************************************************************************
rem *****************************************************************************************
rem *****************************************************************************************
rem *****************************************************************************************
rem *****************************************************************************************
rem @beginGameSpecific 
rem *****************************************************************************************
rem The settings bellow these flags, are written (added or removed) in gameSpecific_buildConfigH.bat (by BuildConfigurator)!
rem You need to use BuildConfigurator ONLY if you ADD or REMOVE the flags bellow this MARK (aka beginGameSpecific)
rem The gameSpecific_buildConfigH.bat will add this flags to config_Android.h header (when you will run make.bat <params>)
rem *****************************************************************************************
rem *****************************************************************************************


set JSON_PROFILES_FILE=%ANDROID_FRAMEWORK_CONFIG%\..\gameprofiles.json
set LOG_TAG="GAME"


rem *********************************************************
rem * End Config ********************************************
rem *********************************************************

ver > nul

