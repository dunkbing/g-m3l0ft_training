@echo off
set CONFIG_ERROR=

call:check VERSION_MAJOR #
call:check VERSION_MINOR #
call:check VERSION_BUILD #
call:check VERSION_MICRO #
call:check VERSION_NANO #
call:check VERSION
call:check GC_LETTER
call:check APP_PACKAGE #
call:check CLASS_NAME #
call:check GL_PRODUCT_ID #
call:check GL_DEMO_CODE #
call:check GGC_GAME_CODE #
call:check OPERATOR #
call:check SO_LIB_FILE #


rem JNI info
call:check JNI_CLASSES #

rem Game's features
rem *************************
call:check RELEASE_VERSION
call:check USE_OPENGLES_20
call:check USE_INSTALLATION_LOCATION
call:check USE_BLUETOOTH
call:check USE_LOW_PROFILE_MENU
call:check ENABLE_GOOGLE_AD_ID
call:check ENABLE_USER_LOCATION
call:check USE_NATIVE_SENSORS
call:check HDIDFV_UPDATE
call:check REMOVE_READ_PHONE_STATE_PERMISSION
call:check USE_SPECIFIC_GENERATOR_NAME
call:check ADD_GOOGLE_PLAY_SERVICES #

REM Installer
call:check INSTALLER_VERSION
call:check INSTALLER_REVISION
call:check BUILD_FOR_FIRMWARE_1_6
call:check USE_INSTALLER
call:check USE_SIMPLIFIED_INSTALLER
call:check FORCE_WIFI_ONLY_MODE
call:check HEP_ENABLE_WIFI_AND_3G
call:check USE_UPDATE_VERSION
call:check UPDATE_NEW_VERSION_DAYS
call:check USE_INSTALLER_SLIDESHOW
call:check USE_INSTALLER_SLIDESHOW_TEXTS
call:check USE_INSTALLER_SLIDESHOW_IMAGES
call:check AUTO_UPDATE_HEP
call:check USE_AUTO_UPDATE_HEP_GOLD
rem call:check USE_DOWNLOAD_MANAGER
call:check USE_LZMA
rem call:check USE_DYNAMIC_DOWNLOAD_LINK
call:check USE_MDL
call:check MAX_SECTION_SIZE
call:check MAX_DOWNLOAD_THREADS


REM Market place Installer
call:check USE_MARKET_INSTALLER
call:check GOOGLE_MARKET_DOWNLOAD
call:check USE_MKP_GOOGLE_WAY
call:check DOWNLOAD_URL
call:check GOOGLE_DRM
call:check TEST_GOOGLE_DRM
call:check USE_GOOGLE_TV_BUILD


if "%USE_ANDROID_TV_IGPCODE%"=="1" (
call:check GGC_GAME_CODE_TV #
)

	
REM Amazon Store
call:check USE_AMAZONMP

REM RSA Key
call:check AMP_EPK #

rem Experimental
call:check PORTAL_CODE #

rem Storage info
call:check MAIN_CLASS_NAME #
call:check SD_FOLDER #
call:check SAVE_FOLDER #

rem Anti-Piracy info
call:check USE_HEP_ANTIPIRACY
if "%USE_HEP_ANTIPIRACY%"=="1" (
	call:check SUPPORTED_MODELS #
	call:check SUPPORTED_DEVICE_NAMES #
	call:check SUPPORTED_MANUFACTURER #
)

call:check USE_HEP_PACKINFO
call:check VERIZON_DRM
call:check TEST_VERIZON_DRM
call:check GLOFT_DRM
call:check USE_SKT_DRM
if "%USE_SKT_DRM%"=="1" (
	call:check SKT_DRM_AID #
)

call:check USE_LGU_DRM
if "%USE_LGU_DRM%"=="1" (
	call:check LGU_DRM_AID #
)

call:check USE_LGW_DRM

call:check USE_KT_DRM
call:check TMOBILE_DRM
if "%TMOBILE_DRM%"=="1" (
	call:check TMO_ITEM_ID #
)

call:check USE_SAMSUNG_DRM

call:check USE_OPTUS_DRM

call:check ORANGE_DRM

SET /A DRM_COUNT=%GLOFT_DRM%+%TMOBILE_DRM%+%USE_LGU_DRM%+%USE_SKT_DRM%+%VERIZON_DRM%+%GOOGLE_DRM%+%USE_HEP_ANTIPIRACY%+%USE_SAMSUNG_DRM%+%USE_LGW_DRM%+%USE_OPTUS_DRM%+%ORANGE_DRM%
if not "%DRM_COUNT%"=="0" (
	if not "%DRM_COUNT%"=="1" (
		set CONFIG_ERROR=%CONFIG_ERROR% ** You should use only 1 DRM
	)
)

rem Tracking features
call:check USE_GAME_TRACKING
call:check USE_TRACKING_FEATURE_INSTALLER
call:check USE_TRACKING_UNSUPPORTED_DEVICE
call:check USE_TRACKING_FEATURE_BILLING

rem Billing system
call:check USE_BILLING

call:check USE_SIM_ERROR_POPUP
call:check SHOP_OPERATOR_TARGET

call:check GL_PROFILE_TYPE
call:check GL_BILLING_VERSION
call:check USE_TNB_CC_PROFILE
call:check USE_VOUCHERS

rem InAppBilling System
call:check USE_IN_APP_BILLING
call:check USE_IN_APP_BILLING_CRM
if "%USE_IN_APP_BILLING_CRM%"=="1" (
	call:check IN_APP_PURCHASE_LIB_CRM_PATH #
)	
call:check USE_IAPV2_LEGACY_WRAPPER
call:check USE_DOWNLOAD_FED_DATA_CENTER
call:check ITEMS_STORED_BY_GAME
call:check USE_IAP_VALIDATION_BETA_SERVER
call:check USE_PHONEBILL_AS_DEFAULT_OPTION
call:check GOOGLE_STORE_V3
call:check ZTE_STORE
if "%ZTE_STORE%"=="1" (
	call:check ZTE_PARTNERID
)
call:check VXINYOU_STORE
if "%VXINYOU_STORE%"=="1" (
	call:check VXINYOU_APPID
	call:check VXINYOU_APPKEY
)
call:check AMAZON_STORE
call:check GAMELOFT_SHOP
call:check SKT_STORE
call:check BOKU_STORE
call:check SHENZHOUFU_STORE
call:check BAZAAR_STORE
call:check YANDEX_STORE
call:check VZW_STORE
call:check VZW_SCM
call:check VZW_MTX
call:check VZW_APP_KEYWORD
call:check SCM_TEST_ENVIRONMENT
call:check SAMSUNG_STORE
call:check ATET_STORE
call:check HUAWEI_STORE
if "%ATET_STORE%"=="1" (
	call:check ATET_APPID #
	call:check ATET_APPKEY #	
)


call:check USE_MTK_SHOP_BUILD
call:check ENABLE_IAP_PSMS_BILLING
call:check USE_ALIPLAY_WAP
call:check USE_UMP_R3_BILLING

rem KDDI Gifting
call:check USE_KDDI_GIFTING

call:check USE_IAP_BG_SCREEN

rem Controller HID 
call:check USE_HID_CONTROLLER

rem Virtual Keyboard
call:check USE_VIRTUAL_KEYBOARD

rem *********************
rem ACP Unit Tests
rem Compile: Java & Native
rem **********************
call:check ACP_UT

rem VideoPlayer
call:check USE_VIDEO_PLAYER
if "%USE_VIDEO_PLAYER%"=="1" (
	call:check USE_ALL_MEDIAPLAYER_FEATURE
	call:check PAUSE_LOGO_VIDEO
	call:check VIDEO_SKIP_TIME
	call:check PAUSE_USER_MUSIC
	call:check VIDEO_ENABLE_PHONE_CALL_LISTENER
	call:check USE_VIDEO_SUBTITLES
	call:check VIDEO_SEEK_BACK_AND_FORWARD
)

rem GLLive
call:check USE_GLLIVE
call:check CHECK_GAME_EXIST
call:check MINIMIZE_ON_CALL

call:check USE_GLLIVE_HTML5
if "%USE_GLIVE_HTML5%"=="1" (
	call:check GLIVE_SKT
	call:check GLIVE_KT
	call:check GLIVE_GL_SHOP
	call:check GLIVE_ANMP
	call:check GLIVE_DOCOMO
	call:check GLIVE_KDDI
	call:check GLIVE_SAMSUNG
	call:check GLIVE_ORANGE
	call:check GLIVE_CYRUS

	set /a GLIVE_ANMP_COUNT=%USE_MARKET_INSTALLER%+%GLIVE_ANMP%
	if not "%GLIVE_ANMP_COUNT%"=="0" (	
		set GLIVE_ANMP_COUNT=1
	)
	set /a GLIVE_COUNT=%GLIVE_SKT%+%GLIVE_KT%+%GLIVE_GL_SHOP%+%USE_AMAZONMP%+%GLIVE_ANMP_COUNT%+%GLIVE_DOCOMO%+%GLIVE_KDDI%+%GLIVE_SAMSUNG%+%GLIVE_CYRUS%
	if "%GLIVE_COUNT%"=="0" (
		set CONFIG_ERROR=%CONFIG_ERROR% ** Set at least one of these variables to 1: GLIVE_SKT GLIVE_KT GLIVE_GL_SHOP GLIVE_ANMP USE_AMAZONMP GLIVE_DOCOMO GLIVE_KDDI GLIVE_SAMSUNG GLIVE_CYRUS
	) else (
		if not "%GLIVE_COUNT%"=="1" (
			set CONFIG_ERROR=%CONFIG_ERROR% ** Set only one of these variables to 1: GLIVE_SKT GLIVE_KT GLIVE_GL_SHOP GLIVE_ANMP USE_AMAZONMP GLIVE_DOCOMO GLIVE_KDDI GLIVE_SAMSUNG GLIVE_CYRUS
		)
	)
)

rem IGP
call:check USE_IGP_ACTIVITY
call:check USE_IGP_FREEMIUM
call:check FULL_SCREEN_IGP
call:check IGP_SKT
call:check IGP_ENABLE_PHONE_CALL_LISTENER
call:check USE_HEP_EXT_IGPINFO
call:check USE_HEP_IGP_PORTAL
call:check USE_IGP_REWARDS
call:check USE_DIRECT_IGP

call:check USE_IN_GAME_BROWSER #

call:check USE_IN_GAME_VIDEO #
call:check IN_GAME_VIDEO_READ_FROM_APK #

rem Ads Server
call:check USE_ADS_SERVER

if "%USE_ADS_SERVER%"=="1" (
	call:check ADS_USE_TAPJOY
	call:check ADS_USE_ADCOLONY
	call:check ADS_USE_FLURRY
	call:check ADS_USE_YUME
	call:check ADS_USE_CHARTBOOST
	
	if "%ADS_USE_TAPJOY%"=="1" (
		call:check ADS_TAPJOY_APP_ID #
		call:check ADS_TAPJOY_SECRET_KEY #
	)
	
	if "%ADS_USE_ADCOLONY%"=="1" (
		call:check ADS_ADCO_APP_ID #
		call:check ADS_ADCO_ZONE_ID_1 #
		call:check ADS_ADCO_ZONE_ID_2
		call:check ADCOLONY_SERVERSIDE_REWARDS #
	)
	
	if "%ADS_USE_FLURRY%"=="1" (
		call:check ADS_FLURRY_ID #
		call:check ADS_FLURRY_INTERSTITIAL_ADSPACE #
		call:check ADS_FLURRY_FREECASH_ADSPACE #
		call:check ADS_FLURRY_USE_LOCATION
	)
	
	if "%ADS_USE_YUME%"=="1" (
		call:check ADS_YUME_SERVER_URL #
		call:check ADS_YUME_DOMAIN_ID #
	)
	
	if "%ADS_USE_CHARTBOOST%"=="1" (
		call:check ADS_CHARTBOOST_APP_ID #
		call:check ADS_CHARTBOOST_APP_SIG #
	)
	
	if "%USE_HAS_OFFERS_TRACKING%"=="1" (
		call:check MAT_ADVERTISER_ID #
		call:check MAT_CONVERSION_KEY #
	)
)

call:check USE_WELCOME_SCREEN
call:check USE_WELCOME_SCREEN_CRM
call:check USE_HOC_SCREEN


call:check USE_POPUPSLIB

rem PSS
call:check USE_PSS
if "%USE_PSS%"=="1" (
	call:check PSS_USE_DISABLE_OPTION
	call:check PSS_VZW
)

call:check SIMPLIFIED_PN
call:check ADM_API_KEY
call:check ENABLE_OLD_GCM_API
call:check USE_NOKIA_API
call:check CLIENTID #
if not "%CONFIG_ERROR%" == "" (goto:variablesNotDefines && exit 1)



:check
rem Call this method by passing 1 or 2 arguments:
rem 	1 argument (the variable's name)
rem			checks if that variable exists.
rem 	2 arguments:
rem			# - that variable is requred in order to continue the process

call set DEFINE_VARVALUE=%%%1%%
set DEFINE_VARNAME=%1
if "%DEFINE_VARVALUE%" == "" (
	if "%2" == "#" (
		set "CONFIG_ERROR=%CONFIG_ERROR% %DEFINE_VARNAME%"
		goto:resetVars
	)
	echo :: %1 is not set.
)

:resetVars
set DEFINE_VARVALUE=
set  DEFINE_VARNAME=
goto:eof

:variablesNotDefines
echo ==================================================================
echo :: ERROR: Please fix your configuration
echo ==================================================================
echo These variables have to be set in order to continue: %CONFIG_ERROR%
pause
goto:eof
