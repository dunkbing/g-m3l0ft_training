@echo off

rem ###############################################################################################
rem ################															    ###############
rem ################					Do not modify this file! 				    ###############
rem ################															    ###############
rem ###############################################################################################
rem ################															    ###############
rem ################	   Add flags in your project's config.bat file 				###############
rem ################				in order to override defaults.					###############
rem ################															    ###############
rem ###############################################################################################

rem *********************
rem Android Framework
rem *********************
set ANDROID_FRAMEWORK_VERSION=1
set USE_GRADLE_AND_J2A=1

rem *********************
rem Installer's Version
rem *********************
set INSTALLER_VERSION_MAJOR=3
set INSTALLER_VERSION_MINOR=7
call %SCRIPT_DIR%\getInstallerRevision.bat
set INSTALLER_VERSION=%INSTALLER_VERSION_MAJOR%.%INSTALLER_VERSION_MINOR%.%INSTALLER_REVISION%

rem Android API Level
set API_LEVEL_NAME=android-10



rem *********************
rem Android Build
rem *********************
set USE_SLN2MK_BUILD=0
if "%VERBOSE%"==""  set VERBOSE=0
if "%CLEAN%"==""  set CLEAN=0

rem *********************
rem Installer
rem Compile: Java
rem *********************
set USE_INSTALLER=1
	set BUILD_FOR_FIRMWARE_1_6=0
	rem Extra size in MB needed on the sd card
	set EXTRA_SPACE_ON_SD=0
	rem Must be 0 when QA test or release
	set FORCE_WIFI_ONLY_MODE=0
	set HEP_ENABLE_WIFI_AND_3G=0
	set USE_UPDATE_VERSION=1
	rem UPDATE_NEW_VERSION_DAYS should be 1 for AUTO_UPDATE_HEP
	set UPDATE_NEW_VERSION_DAYS=7
	rem USE_BLUE_LOGO should be 1 if you want to have the blue splash screen
	set USE_BLUE_LOGO=0
	rem USE_SIMPLIFIED_INSTALLER set to 1 will use the simple installer flow
	set USE_SIMPLIFIED_INSTALLER=0
	set USE_INSTALLER_SLIDESHOW=0
	set USE_INSTALLER_SLIDESHOW_IMAGES=0
	set USE_INSTALLER_SLIDESHOW_TEXTS=0
	set AUTO_UPDATE_HEP=0
		rem only for AUTO_UPDATE_HEP version
		set USE_AUTO_UPDATE_HEP_GOLD=1
	rem Download manager, it increases the download speed
	rem set USE_DOWNLOAD_MANAGER=1
	set USE_GAME_TRACKING=1
	set USE_TRACKING_FEATURE_INSTALLER=1
	set USE_TRACKING_FEATURE_BILLING=0
	rem LZMA should be 0, because it's very slow for the moment
	set USE_LZMA=0
	rem multipledownload links
	set USE_MDL=0
	rem set USE_DYNAMIC_DOWNLOAD_LINK=0
	set PORTAL_CODE=google_market
	rem The maximum number of threads used by the download manager
	set MAX_DOWNLOAD_THREADS=5
	rem The maximum size of a Section used by the download manager
	set MAX_SECTION_SIZE=20
	set USE_BEAM=0
	set SPLIT_SIZE=2048
	set USE_MANAGED_SPACE=0
	
	rem For MEEP: Forxe using the external sd only
	set FORCE_EXTERNAL_SD_MEEP=0
	rem The external mounting position of the sd card
	set EXTERNAL_SD_LOCATION="/mnt/extsd"

	rem *********************
	rem Market place Installer
	rem *********************
	set USE_MARKET_INSTALLER=0
		set USE_TRACKING_UNSUPPORTED_DEVICE=1
		set USE_MKP_GOOGLE_WAY=0
		rem When Alpha or Beta, set USE_GOLD_SERVER to 0, and change DOWNLOAD_URL to the test server
		set USE_GOLD_SERVER=1
		REM deprecated USE_INSTALL_REFERRER now activated by USE_MARKET_INSTALLER
		REM rem track camping reference for installation
		REM set USE_INSTALL_REFERRER=1
		rem Compile: Java & Native
		set GOOGLE_DRM=0
		set GOOGLE_MARKET_DOWNLOAD=0
		set TEST_GOOGLE_DRM=0
		set UNZIP_DOWNLOADED_FILES=0
		
	rem **************
	rem  Amazon Store
	rem **************
	set USE_AMAZONMP=0

	set AMP_EPK="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArjzJYaersEzHcAbdWeui2v4UThDbF59uWcEAxTbRxmL97B/9JXyVl0Ta0b+owRJfgTnOwoZNe4IOAn+0rrDudGE68f4tQjH18EQKc+3jzxvjXOe0RicWr91bVpfmYfBlE1Fqc+0aevoXAdCqZz6PMNLuScD7P7daVgT+tHXSfrKBLbjkaPqab1skyIZUK3b2QF+3u8asaAPl+gUKQSHnp9Cc0BX1LpUPcteCyeNZG214ZSRT339WJ/7+dpzQo3G7DJU3N9CXfh+3y/6DLE1S3+1iC1dOAHAnCm84OQ0F3axlmiv428zvaYW88+SalCDzh2XB0k3+XmE4MRtJkFmL4QIDAQAB"

	rem *********************
	rem Google TV project
	rem *********************
	rem will modify the AndroidManifest to adjust to google tv projects
	set USE_GOOGLE_TV_BUILD=0

	set USE_ANDROID_TV_IGPCODE=0
	set GGC_GAME_CODE_TV=
	
	
rem ****************************
rem Google Analytics Tracking
rem ****************************
rem USE_GOOGLE_ANALYTICS_TRACKING: 1 if you want to use Google Analytics Tracking, otherwise 0. Default: 0
set USE_GOOGLE_ANALYTICS_TRACKING=0
rem GA_TRACKING_ID: Use quotes to include the ID. The Google Analytics tracking ID to which to send your data. You can disable your tracking by not providing this value.
rem Example: set GA_TRACKING_ID="UA-33292614-1"
set GA_TRACKING_ID=
rem GA_APP_DEBUG: true if the you want to test and debug Google Analytics, otherwise false.
set GA_APP_DEBUG=false
set GA_LOG_TYPE=error
rem GA_DISPATCH_PERIOD: integer representing the dispatch time in seconds. Default: 30
set GA_DISPATCH_PERIOD=30
rem GA_SAMPLE_FREQUENCY: The sample rate to use. Default is 100.0. It can be any value between 0.0 and 100.0
set GA_SAMPLE_FREQUENCY=100.0
rem GA_AUTO_ACTIVITY_TRACKING: Automatically track a screen view each time a user starts an Activity. true by default.
set GA_AUTO_ACTIVITY_TRACKING=1
rem GA_ANONYMIZELP: Tells Google Analytics to anonymize the information sent by the tracker objects by removing the last octet of the IP address prior to its storage. Note that this will slightly reduce the accuracy of geographic reporting. false by default.
set GA_ANONYMIZELP=false
rem GA_REPORT_UNCAUGHT_EXCEPTION: Automatically track an Exception each time an uncaught exception is thrown in your application. true by default.
set GA_REPORT_UNCAUGHT_EXCEPTION=true
rem GA_SESSION_TIMEOUT: The amount of time (in seconds) your application can stay in the background before the session is ended. Default is 30 seconds. Negative value disables EasyTracker session management.
set GA_SESSION_TIMEOUT=60

rem ****************************
rem GLOT Tracking
rem ****************************
rem set USE_GLOT_TRACKING=0


rem ****************************
rem Data and server information
rem ****************************
rem remember to change .jar to .zip when you upload file into gameloft server
rem set DATA_DOWNLOAD_EXT=jar

rem Gameloft shop builds
rem set DATA_DOWNLOAD_SERVER=http://ota.sai.gameloft.com/gmz/sai/
REM set DATA_DOWNLOAD_SERVER=http://ota.mex.gameloft.com/gmz/ntr/
rem set DATA_DOWNLOAD_SERVER=http://dl.gameloft.com/hdplus/android/
set DATA_DOWNLOAD_SERVER=http://test.gameloft.net/gmz/ntr/

rem Google Market Place builds
	
REM ** THESE VALUES SHOULD NOT BE CONFIGURATED BY DEFAULTS

rem *************************
rem DRM, choose only one below
rem *************************
set USE_DRM=0
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
set TMO_ITEM_ID=
REM NOTE: ask the producer to check what External ID will have the game.

rem SKT DRM
set USE_SKT_DRM=0
set SKT_DRM_AID=

rem LGU DRM
set USE_LGU_DRM=0
set LGU_DRM_AID=

rem Orange DRM
rem Compile: Java
set ORANGE_DRM=0
set ORANGE_DRM_TEST=0

rem LG World
set USE_LGW_DRM=0
set FOR_LGW_GLOBAL=1
rem NOTE: for Korea, please set FOR_LGW_GLOBAL=0

rem KT DRM
set USE_KT_DRM=0

rem Samsung DRM (Zirconia)
set USE_SAMSUNG_DRM=0

rem Optus DRM for Australia client
set USE_OPTUS_DRM=0

rem Pantech DRM for Australia client
set USE_PANTECH_ARM=0
set PANTECH_STORE_CID=""

rem HEP Antipiracy
set USE_HEP_ANTIPIRACY=0
set USE_HEP_PACKINFO=0
rem set SUPPORTED_MANUFACTURER=

rem *********************
rem GAIA online lib
rem Compile: Java
rem *********************
rem set USE_GAIA=1

rem *********************
rem GLLive
rem Compile: Java
rem *********************
set USE_GLLIVE=1
	set CHECK_GAME_EXIST=1
	set MINIMIZE_ON_CALL=1
	
rem *********************
rem GLLive HTML5
rem Compile: Java
rem *********************
set USE_GLLIVE_HTML5=0
set GLIVE_SKT=0
set GLIVE_KT=0
set GLIVE_GL_SHOP=0
set GLIVE_ANMP=1
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
set USE_IGP_FREEMIUM=0
	set FULL_SCREEN_IGP=0
	set IGP_SKT=0
	set USE_IGP_REWARDS=0
	set IGP_ENABLE_PHONE_CALL_LISTENER=0
	rem	HEP external igp info
	set USE_HEP_EXT_IGPINFO=0
	set USE_HEP_IGP_PORTAL=0

set USE_DIRECT_IGP=0

set USE_IN_GAME_BROWSER=1

set USE_IN_GAME_VIDEO=0
set IN_GAME_VIDEO_READ_FROM_APK=0

	
rem *********************
rem Ads Server
rem Compile: Java
rem *********************
set USE_ADS_SERVER=0
	set ADS_USE_TAPJOY=0
	set ADS_USE_FLURRY=0
		set ADS_FLURRY_ID=
		set ADS_FLURRY_INTERSTITIAL_ADSPACE=
		set ADS_FLURRY_FREECASH_ADSPACE=
		set ADS_FLURRY_USE_LOCATION=
	set ADS_USE_ADCOLONY=0
		set ADCOLONY_SERVERSIDE_REWARDS=0
	set ADS_USE_YUME=0
	set ADS_USE_CHARTBOOST=0
	set USE_HAS_OFFERS_TRACKING=0

set USE_WELCOME_SCREEN=0
set USE_WELCOME_SCREEN_CRM=0
set USE_HOC_SCREEN=0

set USE_POPUPSLIB=0

rem *********************
rem Controller HID 
rem Compile: Java & Native
rem *********************
set USE_HID_CONTROLLER=0	
	
rem *********************
rem Virtual Keyboard
rem Compile: Java & Native
rem *********************
set USE_VIRTUAL_KEYBOARD=1

rem *********************
rem ACP Unit Tests
rem Compile: Java & Native
rem **********************
set ACP_UT=0
	
rem *********************
rem Video Player
rem Compile: Java
rem *********************
set USE_VIDEO_PLAYER=0
	rem Use all play, stop, next, prev, skip or just skip? Defaut is 0 
	set USE_ALL_MEDIAPLAYER_FEATURE=0
	rem Pause the video after INT, or just close?
	set PAUSE_LOGO_VIDEO=1
	rem Video skip time, in milisec
	set VIDEO_SKIP_TIME=8000
	rem Pause user music when play video
	set PAUSE_USER_MUSIC=1
	rem Suspend video and not alow it resume while calling
	set VIDEO_ENABLE_PHONE_CALL_LISTENER=0
	rem Subtitles
	set USE_VIDEO_SUBTITLES=0
	rem the value that's added or substracted in order to seek backward or forward
	set VIDEO_SEEK_BACK_AND_FORWARD=3000

rem *********************
rem Billing system
rem Compile: Java & Native
rem *********************
set USE_BILLING=0

rem use a sim error message to send user to gameloft shop if there is no SIM in device and evade CC purchase
	set USE_SIM_ERROR_POPUP=0
rem in case SIM error message is used add the shop operator target here example BFL4 (latin America R 4)
	set SHOP_OPERATOR_TARGET=BFL4

	set GL_PROFILE_TYPE=4
	set GL_BILLING_VERSION=1
	set USE_BOKU_FOR_BILLING=0
	set USE_BILLING_FOR_CHINA=0
	set GL_CHANNEL=0
	set USE_TNB_CC_PROFILE=1
	set USE_VOUCHERS=0

rem *********************
rem In App Billing system
rem Compile: Java & Native
rem *********************
set USE_IN_APP_BILLING=0
	set USE_IN_APP_BILLING_CRM=0
	set IN_APP_PURCHASE_LIB_CRM_PATH=%~dp0..\in_app_purchase
	set USE_IN_APP_GLOT_LOGGING=0
	set GOOGLE_STORE_V3=0
	set AMAZON_STORE=0
	set GAMELOFT_SHOP=0
		set ENABLE_IAP_PSMS_BILLING=0
		set USE_PHD_PSMS_BILL_FLOW=0
		set BOKU_STORE=0
		set USE_MTK_SHOP_BUILD=0
		set USE_ALIPLAY_WAP=0
		set USE_UMP_R3_BILLING=0
		set SHENZHOUFU_STORE=0
		set BAZAAR_STORE=0
	set YANDEX_STORE=0
	set SKT_STORE=0
		set TEST_SKT_STORE=0
	set KT_STORE=0
		set KT_TABLET_API=0
	set PANTECH_STORE=0
	set OPTUS_STORE=0
	set VZW_STORE=0
		set VZW_SCM=0
		set VZW_MTX=0
		set VZW_APP_KEYWORD=0
		set SCM_TEST_ENVIRONMENT=0
	set SAMSUNG_STORE=0
	set ATET_STORE=0
	set ZTE_STORE=0
		set ZTE_PARTNERID=0
	set HUAWEI_STORE=0
	set VXINYOU_STORE=0
		set VXINYOU_APPID=0
		set VXINYOU_APPKEY=0
set	USE_IAPV2_LEGACY_WRAPPER=1
set ITEMS_STORED_BY_GAME=0
set USE_IAP_VALIDATION_BETA_SERVER=0
set USE_PHONEBILL_AS_DEFAULT_OPTION=0
set USE_DOWNLOAD_FED_DATA_CENTER=0

set USE_IAP_BG_SCREEN=1


set USE_KDDI_GIFTING=0

rem *************************
rem Other features
rem *************************
set USE_OPENGLES_20=1
set USE_INSTALLATION_LOCATION=1
set USE_BLUETOOTH=0
rem USE_LOW_PROFILE_MENU = 0 immersive mode
rem USE_LOW_PROFILE_MENU = 1 low profile menu
set USE_LOW_PROFILE_MENU=0
set ENABLE_GOOGLE_AD_ID=1
set USE_NATIVE_SENSORS=0
set USE_SPECIFIC_GENERATOR_NAME=0

set HDIDFV_UPDATE=1

set REMOVE_READ_PHONE_STATE_PERMISSION=0

set USE_IGP_CODE_FROM_FILE=0

set ADD_GOOGLE_PLAY_SERVICES=1


rem *************************************
rem Get user location
rem *************************************
set ENABLE_USER_LOCATION=0


rem *************************************
rem Push Notification
rem Compile: Java & Native
rem *************************************
set SIMPLIFIED_PN=0
set ADM_API_KEY=0
set ENABLE_OLD_GCM_API=0
set USE_NOKIA_API=0
	rem CLIENTID The unique identifier for the client software making the request (It must be a string containing product ID, GGI, build version, and platform with a colon separating the values like this "1094:MMHP:2.1.0:android")
	set CLIENTID=
	
	rem googleplay | amazon | shop | china | huaweichina | jsmcchina | kddi | kt | lguplus | pantech | samsung | samsungchina | shenzhoufu | skt | sonychina | verizonmicro | verizonscm	
	set DOWNLOAD_SOURCE=
	
rem *************************************


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
rem Game's config
rem *************************

REM ** THESE VALUES SHOULD NOT BE CONFIGURATED BY DEFAULTS

REM rem Application info and JNI function

REM ** THESE VALUES SHOULD NOT BE CONFIGURATED BY DEFAULTS

REM rem application folders

REM ** THESE VALUES SHOULD NOT BE CONFIGURATED BY DEFAULTS

REM rem Download data files

REM ** THESE VALUES SHOULD NOT BE CONFIGURATED BY DEFAULTS


rem **********************
rem Working Environment
rem **********************

REM ** MOVED TO BEGIN OF FILE IN ORDER TO CALL defaults.bat
REM rem now using %~dp0 due to checking paths section. %~dp0 return path + "\"

rem folders
set SRC_FOLDER=%ROOT_DIR%\java
set NATIVE_SRC_FOLDER=%ROOT_DIR%\native
set LIBS_FOLDER=%ROOT_DIR%\libs
set ANDROID_RAW_RES=%ROOT_DIR%\res\raw
rem packed resources folder
set RESOURCES_BIN=%ROOT_DIR%\bin


rem game info
set APK_RESOURCES=%ROOT_DIR%\res_apk
if "%APK_SRC_FOLDER%"=="" set APK_SRC_FOLDER=%ANDROID_FRAMEWORK_DIR%\java
set APK_ANDROID_MANIFEST=%ANDROID_FRAMEWORK_CONFIG%\configs\project\AndroidManifest.xml


rem *************************
rem Folder and data config
rem *************************
rem SD Resource dir
set SD_RESOURCES_PVRT=%ROOT_DIR%\res_sd\pvrt
set SD_RESOURCES_ETC=%ROOT_DIR%\res_sd\etc
set SD_RESOURCES_ATC=%ROOT_DIR%\res_sd\atc
set SD_RESOURCES_DXT=%ROOT_DIR%\res_sd\dxt
set SD_RESOURCES_COMMON=%ROOT_DIR%\res_sd\common
set GOOGLE_DOWNLOAD_FILE_NAME=%VERSION_MAJOR%%VERSION_MINOR%%VERSION_BUILD%.%APP_PACKAGE%
set DOWNLOAD_FILE_EXTENSION=obb

rem market packer specific
rem DEFLATED or STORED
set COMPRESS_METHOD=STORED
set COMPILE_MAIN_FOLDER=1
set COMPILE_PATCH=1

rem preprocessors and tools

set GAME_SPECIFIC_PROGUARD=%ANDROID_FRAMEORK_CONFIG%\configs\project\proguard.properties

set TOOLS_DIR=%ROOT_DIR%\tools
set PREPROCESS_APP=%TOOLS_DIR%\cpp\cpp.exe
set PREPROCESS_XML_APP=%TOOLS_DIR%\cpp\cppXML.exe
set POSTPROCESS_XML_APP=%TOOLS_DIR%\cpp\ppManifest.exe
set RESPACK=%TOOLS_DIR%\resPack\AResPack.exe
set TESTPACK=%TOOLS_DIR%\resPack\ATestPack.exe
set CONFIG_UPDATE=%TOOLS_DIR%\Config\Config.py
set PDF_CREATOR=%TOOLS_DIR%\pdf\createpdf.py
set ZIP_APP=%TOOLS_DIR%\compress\7za.exe

rem default folder for pulled saves from device
set PULLED_SAVE_FILES_DIR=%ROOT_DIR%\saves\default

rem show windows tool tip
set NOTIFU=%TOOLS_DIR%\notifu.bat

set ANDROID_TOOLS=%ANDROID_HOME%\tools
set ANDROID_PLATFORM_TOOLS=%ANDROID_HOME%\platform-tools
set ADB="%ANDROID_PLATFORM_TOOLS%\adb.exe"
if not exist "%ADB%" (
	set ADB=%ANDROID_TOOLS%\adb.exe
)

rem Working (temporal) Dirs
set WORK_FOLDER=%ROOT_DIR%\_work
set TMP_SRC_FOLDER=%WORK_FOLDER%\src
set TMP_AIDL_FOLDER=%WORK_FOLDER%\aidl
set TMP_RES_FOLDER=%WORK_FOLDER%\res
set TMP_ASSETS_FOLDER=%WORK_FOLDER%\assets
set TMP_LIBS_FOLDER=%WORK_FOLDER%\libs

if "%ANDROID_FRAMEWORK_CONFIG%"=="" (
	set ANDROID_FRAMEWORK_CONFIG=%ANDROID_FRAMEWORK_DIR%\..\AndroidFrameworkConfig
)
set ANDROID_CONFIG_FILE=%ANDROID_FRAMEWORK_CONFIG%\configs\generated\config_Android.h
set ANDROID_CONFIG_FILE_JAVA_CLASS=%APK_SRC_FOLDER%\config_Android.java

set AF_BIN_PATH=%ANDROID_FRAMEWORK_CONFIG%\build\bin
set AF_INTERMEDIATE_PATH=%ANDROID_FRAMEWORK_CONFIG%\build\generated
set AF_PRECOMPILED_PATH=%ANDROID_FRAMEWORK_CONFIG%\build\precompiled


set USE_DATA_SHARING=0

rem generated proguard file
set PROGUARD_FILE=%WORK_FOLDER%\proguard.cfg

rem this is an aditional file for preprocessing
set PREPROCESS_USING_INCLUDE_FILE=

set ADD_EXTERNAL_LIB_PROPERTIES=%ROOT_DIR%\add_external_lib_properties.bat
