@echo off

echo Preprocessing Manifest and sources and resources...

if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)

echo ---------------------------------------------------
echo Installer Version: %INSTALLER_VERSION%

if "%ANDROID_SDK_REVISION%"=="" (
	call %SCRIPT_DIR%\getAndroidRevisionSDK.bat
)
echo Android SDK Version: %ANDROID_SDK_REVISION%

echo ---------------------------------------------------
echo Cleaning ...
if exist %WORK_FOLDER% 			rd /Q /S %WORK_FOLDER%
if exist %TMP_SRC_FOLDER% 		rd /Q /S %TMP_SRC_FOLDER%
if exist %ROOT_DIR%\bin\classes	rd /Q /S %ROOT_DIR%\bin\classes
if exist %ROOT_DIR%\bin\res		rd /Q /S %ROOT_DIR%\bin\res
if "%USE_HEP_PACKINFO%"=="0" (	
	call:deleteFile %APK_RESOURCES%\installer\res\raw\pack.info
) 

REM reset the ADD_EXTERNAL_LIB_PROPERTIES file
call %ADD_EXTERNAL_LIB_PROPERTIES% RESET_PATHS_COUNT

rem recreate the cleaned folders
md %WORK_FOLDER%
md %TMP_SRC_FOLDER%
md %ROOT_DIR%\bin\classes

if not exist "%TMP_RES_FOLDER%" (
	md %TMP_RES_FOLDER%
)

if not exist %TMP_RES_FOLDER%\raw (
	md %TMP_RES_FOLDER%\raw
)

if not exist "%TMP_ASSETS_FOLDER%" (
	md %TMP_ASSETS_FOLDER%
)

echo ---------------------------------------------------
echo START Preprocess ...
echo ---------------------------------------------------

if "%TMP_SRC_FOLDER%"=="" (
	echo ERROR: Enviroment not setted
	goto end
)

REM ------------- config file -------------
echo .
call buildConfigH.bat
echo .

REM ------------- Preprocess the files using an additional header file -----------------
if NOT "%PREPROCESS_USING_INCLUDE_FILE%"=="" (
	if exist "%PREPROCESS_USING_INCLUDE_FILE%" (
		set PREPROCESS_USING_INCLUDE_FILE=-include %PREPROCESS_USING_INCLUDE_FILE%
	) else (
		echo "WARNING: File %PREPROCESS_USING_INCLUDE_FILE% does not exist! please edit PREPROCESS_USING_INCLUDE_FILE in config.bat..."
		echo "WARNING: You can leave it blank or set it to a valid file!"
		set PREPROCESS_USING_INCLUDE_FILE=
	)
)

echo ---------------------------------------------------
echo Copying apk resources ...

REM ------------- Google Analytics -------------
if "%USE_GOOGLE_ANALYTICS_TRACKING%"=="1" (
REM 	call:copyToWorkFolderInstallerImagesForCrashChecker
	call:copyToWorkFolder GoogleAnalyticsTracking -l -r
)

set /A ISGOOGLEBUILD=%USE_MARKET_INSTALLER%%GOOGLE_MARKET_DOWNLOAD%%GOOGLE_STORE_V3%
set /A NEEDSSERIALKEYFILE=%USE_INSTALLER%%USE_BILLING%
IF %ISGOOGLEBUILD% GTR 0 (
	REM ------------- Creating empty file crc.bin -------------
	type NUL > %TMP_RES_FOLDER%\raw\crc.bin
	IF %NEEDSSERIALKEYFILE% EQU 0 (
		echo creating serialkey.txt
		type NUL > %TMP_RES_FOLDER%\raw\serialkey.txt
	)
)

REM ------------- Installer -------------
if "%USE_INSTALLER%" == "1" (
	echo %VERSION_MAJOR%.%VERSION_MINOR%.%VERSION_BUILD%>%TMP_RES_FOLDER%\raw\infoversion.txt
	call:copyToWorkFolder installer -r -a
)

REM ------------- Utils -------------
call:copyToWorkFolder utils -r -a

REM ------------- ADD ENCRYPTED DEMO CODE -------------
if "%USE_IGP_CODE_FROM_FILE%" == "1" (
	%TOOLS_DIR%\defines\StringEncoder.exe -encryptkey %GL_DEMO_CODE% %GGC_GAME_CODE% %TMP_RES_FOLDER%\raw\nih.bin
)

call:deleteFile %LIBS_FOLDER%\%TYPE_ARMEABI%\libgenerator.so
call:deleteFile %LIBS_FOLDER%\%TYPE_ARMEABI%\libgenerator%GGC_GAME_CODE%.so
if "%USE_SPECIFIC_GENERATOR_NAME%"=="0" (
	xcopy %APK_RESOURCES%\utils\%TYPE_ARMEABI%\libgenerator.so %LIBS_FOLDER%\%TYPE_ARMEABI% /E /Y>>NUL
) else (
	copy %APK_RESOURCES%\utils\%TYPE_ARMEABI%\libgenerator.so %LIBS_FOLDER%\%TYPE_ARMEABI%\libgenerator%GGC_GAME_CODE%.so>>NUL
)

if "%ADD_GOOGLE_PLAY_SERVICES%"=="1" (
	call %ADD_EXTERNAL_LIB_PROPERTIES% %ROOT_DIR% %~dp0/res_apk/utils/external_libs/google-play-services_lib/
)

REM ------------- Anti-Piracy -------------
REM Verizon DRM
call:deleteFile %LIBS_FOLDER%\VzwProtectedApplicationLibrary.jar
if "%VERIZON_DRM%"=="1" call:copyToWorkFolder DRM\Verizon -r -a -l

REM Gloft DRM
if "%GLOFT_DRM%"=="1" call:copyToWorkFolder DRM\Gloft -r -a

REM TMobile DRM
if "%TMOBILE_DRM%"=="1" call:copyToWorkFolder DRM\TMobile -r -a

REM Google DRM
call:deleteFile %LIBS_FOLDER%\GoogleDownloadLibrary.jar
call:deleteFile %LIBS_FOLDER%\GoogleLicesing.jar
call:deleteFile %LIBS_FOLDER%\GoogleZipFileReader.jar
if "%GOOGLE_MARKET_DOWNLOAD%"=="1" (
call:copyToWorkFolder GoogleExpansionFiles -r -l
call:copyToWorkFolder DRM\Google_DRM -r
)

REM Google DRM
if "%GOOGLE_DRM%"=="1" call:copyToWorkFolder DRM\Google_DRM -r -l

REM SKT DRM
call:deleteFile %LIBS_FOLDER%\SK_DRM_CLASS.jar
call:deleteFile %LIBS_FOLDER%\%TYPE_ARMEABI%\libARMPlugin.so
if "%USE_SKT_DRM%"=="1" call:copyToWorkFolder DRM\SKT_DRM -r -l
if "%USE_SKT_DRM%"=="1" move %LIBS_FOLDER%\libARMPlugin.so %LIBS_FOLDER%\%TYPE_ARMEABI%\.

REM ------------- Pantech ARM -------------
call:deleteFile %LIBS_FOLDER%\arm-client.jar
if "%USE_PANTECH_ARM%"=="1" call:copyToWorkFolder DRM\PANTECH_ARM -r -l

REM LGU DRM
call:deleteFile %LIBS_FOLDER%\LGT_DRM.jar
if "%USE_LGU_DRM%"=="1" call:copyToWorkFolder DRM\LGU_DRM -r -l

REM LGW DRM
call:deleteFile %LIBS_FOLDER%\lgcoconut_gb.jar
call:deleteFile %LIBS_FOLDER%\lgcoconut_kr.jar
if "%USE_LGW_DRM%"=="1" call:copyToWorkFolder DRM\LGW_DRM -r -l

REM KT DRM
call:deleteFile %LIBS_FOLDER%\KAFINFO.jar
if "%USE_KT_DRM%"=="1" call:copyToWorkFolder DRM\KT_DRM -r -l

REM Samsung DRM (Zirconia)
call:deleteFile %LIBS_FOLDER%\Zirconia.jar
call:deleteFile %LIBS_FOLDER%\%TYPE_ARMEABI%\libnativeinterface.so
if "%USE_SAMSUNG_DRM%"=="1"  call:copyToWorkFolder DRM\Samsung -r -l

rem Optus DRM for Australia Client.
if "%USE_OPTUS_DRM%"=="1"  call:copyToWorkFolder DRM\OPTUS_DRM -r -l

rem Orange DRM
if "%ORANGE_DRM%"=="1" call:copyToWorkFolder DRM\ORANGE_DRM -r

REM NODRM
rem if "%GLOFT_DRM%"=="0" (
rem 	if "%VERIZON_DRM%"=="0" (
rem 		if "%TMOBILE_DRM%"=="0" call:copyToWorkFolder DRM\NoDRM -r
rem 	)
rem )

REM ------------ KDDI Gift -------------
if "%USE_KDDI_GIFTING%"=="1" call:copyToWorkFolder KDDIGifting -r -l


REM ------------- Video Player -------------
if "%USE_VIDEO_PLAYER%"=="1" call:copyToWorkFolder video -r

REM ------------- Verizon Billing -------------
if "%USE_VZW_BILLING%" == "1" call:copyToWorkFolder VzwBilling

REM ------------- Billing -------------
if "%USE_BILLING%"=="1" call:copyToWorkFolder billing -r

REM ------------- Boku For Billing-------------
if "%USE_BOKU_FOR_BILLING%" == "1" call:copyToWorkFolder BokuForBilling -r -l

REM ------------- China For Billing-------------
if "%USE_BILLING_FOR_CHINA%"=="1" call:copyToWorkFolder billing\ChinaBilling -r -l


if "%USE_IN_APP_BILLING_CRM%"=="1" goto SKIP_IAP_PACKAGE_RESOURCE

REM ------------- In App Billing -------------
if "%USE_IN_APP_BILLING%"=="1" call:copyToWorkFolder InAppBilling -r

REM GOOGLE_STORE_V3
if "%GOOGLE_STORE_V3%"=="1" call:copyToWorkFolder InAppBilling\google_play_v3 -r

REM BAZAAR_STORE
if "%BAZAAR_STORE%"=="1" call:copyToWorkFolder InAppBilling\bazaar -r

REM YANDEX_STORE
if "%YANDEX_STORE%"=="1" call:copyToWorkFolder InAppBilling\yandex -r

REM AMAZON_STORE
if "%AMAZON_STORE%"=="1" call:copyToWorkFolder InAppBilling\amazon -r -l

REM SHENZHOUFU_STORE
if "%SHENZHOUFU_STORE%"=="1" call:copyToWorkFolder InAppBilling\shenzhoufu -r -l

REM VXINYOU_STORE
if "%VXINYOU_STORE%"=="1" call:copyToWorkFolder InAppBilling\vxinyou -r

REM ------------- In App Billing -------------
REM GAMELOFT_SHOP
if "%GAMELOFT_SHOP%"=="1" call:copyToWorkFolder InAppBilling\gameloft -r

REM BOKU_STORE
call:deleteFile %LIBS_FOLDER%\BokuSDK_v2.3.jar
call:deleteFile %LIBS_FOLDER%\BokuSDK_v3.3.jar
if "%BOKU_STORE%"=="1" call:copyToWorkFolder InAppBilling\boku -r -l

REM PANTECH_STORE
call:deleteFile %LIBS_FOLDER%\IABL_Lib.jar
if "%PANTECH_STORE%"=="1" call:copyToWorkFolder InAppBilling\pantech -r -l

REM SKT_STORE
call:deleteFile %LIBS_FOLDER%\iap_plugin-14.01.01.jar
call:deleteFile %LIBS_FOLDER%\%TYPE_ARMEABI%\libdodo.so
call:deleteFile %LIBS_FOLDER%\%TYPE_ARMEABI%\libUSToolkit.so
call:deleteFile %LIBS_FOLDER%\gson-2.2.2.jar
if "%SKT_STORE%"=="1" (
	echo :: copying InAppBilling\skt resources...
	rmdir %LIBS_FOLDER%\dev
	rmdir %LIBS_FOLDER%\release
	
	xcopy %APK_RESOURCES%\InAppBilling\skt\libs\release\iap_plugin-14.01.01.jar %LIBS_FOLDER% /E /Y>>NUL
	xcopy %APK_RESOURCES%\InAppBilling\skt\libs\release\%TYPE_ARMEABI%\libdodo.so %LIBS_FOLDER%\%TYPE_ARMEABI% /E /Y>>NUL
	xcopy %APK_RESOURCES%\InAppBilling\skt\libs\release\%TYPE_ARMEABI%\libUSToolkit.so %LIBS_FOLDER%\%TYPE_ARMEABI% /E /Y>>NUL
	
	xcopy %APK_RESOURCES%\InAppBilling\skt\libs\gson-2.2.2.jar %LIBS_FOLDER% /Y>>NUL
	xcopy %APK_RESOURCES%\InAppBilling\skt\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
	echo .
)

REM KT_STORE
call:deleteFile %LIBS_FOLDER%\InApp.jar
call:deleteFile %LIBS_FOLDER%\InAppTablet.jar
set FOLDER=InAppBilling\kt
if "%KT_STORE%" == "1" (
	echo :: copying %FOLDER% resources...
	if "%KT_TABLET_API%" == "1" (
		echo WARNING: Using KT API version for tablets [%API_LEVEL_NAME%]
		xcopy %APK_RESOURCES%\%FOLDER%\libs\InAppTablet.jar %LIBS_FOLDER% /E /Y>>NUL
	) else (
		xcopy %APK_RESOURCES%\%FOLDER%\libs\InApp.jar %LIBS_FOLDER% /E /Y>>NUL
	)
	echo .
)

REM -------------Verizon_Store-------------------
if "%VZW_STORE%" == "1" (
	echo :: Copying VZW resources...
	xcopy %APK_RESOURCES%\InAppBilling\verizon\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
	
	if "%VZW_MTX%" == "1" (
		call:deleteFile %LIBS_FOLDER%\VZW-SDK.jar
		xcopy %APK_RESOURCES%\InAppBilling\verizon\libs\VZW-SDK.jar %LIBS_FOLDER% /E /Y>>NUL
	)
	echo .
)

REM -------------Samsung Store-------------------
if "%SAMSUNG_STORE%" == "1" (
	echo :: Copying Samsung resources...
	xcopy %APK_RESOURCES%\InAppBilling\samsung\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
	call:deleteFile %LIBS_FOLDER%\plasma.jar
	xcopy %APK_RESOURCES%\InAppBilling\samsung\libs\*.aidl %TMP_SRC_FOLDER%\com\sec\android\iap\*.aidl /E /Y>>NUL
	echo .
)

REM HUAWEI_STORE
if "%HUAWEI_STORE%"=="1" call:copyToWorkFolder InAppBilling\huawei -r
:SKIP_IAP_PACKAGE_RESOURCE


REM ------------- GLLive -------------
if "%USE_GLLIVE%"=="1" call:copyToWorkFolder gllive -r

REM ------------- GLLive HTML5 ---------------
if "%USE_GLLIVE_HTML5%"=="1" call:copyToWorkFolder GLiveHTML5 -r

REM ------------- IGP HTML -------------
if "%USE_IGP_ACTIVITY%"=="1" call:copyToWorkFolder IGP_HTML -r

REM ------------- IGP FREEMIUM -------------
if "%USE_IGP_FREEMIUM%"=="1" call:copyToWorkFolder IGP_Freemium -r

REM ------------- DIRECT IGP -------------
if "%USE_DIRECT_IGP%"=="1" call:copyToWorkFolder DirectIGP -r

REM ------------- IN GAME BROWSER -----------
if "%USE_IN_GAME_BROWSER%"=="1" call:copyToWorkFolder InGameBrowser -r


REM ------------- USE IN GAME VIDEO -------------
if "%USE_IN_GAME_VIDEO%"=="1" call:copyToWorkFolder InGameVideo -r

REM ------------- ADS SERVER -------------
call:deleteFile %LIBS_FOLDER%\adcolony.jar
call:deleteFile %LIBS_FOLDER%\FlurryAnalytics_3.3.0.jar
call:deleteFile %LIBS_FOLDER%\FlurryAds_3.3.0.jar
call:deleteFile %LIBS_FOLDER%\GoogleAdMobAdsSdk-4.1.1.jar
call:deleteFile %LIBS_FOLDER%\YuMeAndroidSDK.jar
call:deleteFile %LIBS_FOLDER%\backport-util-concurrent-3.1.jar
call:deleteFile %LIBS_FOLDER%\commons-codec-1.3.jar
call:deleteFile %LIBS_FOLDER%\commons-lang-2.4.jar
call:deleteFile %LIBS_FOLDER%\commons-logging-1.1.1.jar
call:deleteFile %LIBS_FOLDER%\ical4j-1.0.jar
call:deleteFile %LIBS_FOLDER%\chartboost.jar
call:deleteFile %LIBS_FOLDER%\tapjoyconnectlibrary.jar
call:deleteFile %LIBS_FOLDER%\MobileAppTracker.jar

if "%USE_ADS_SERVER%"=="1" (
	call:copyToWorkFolder AdServer\adserver
	
	if "%ADS_USE_TAPJOY%"=="1" (
		call:copyToWorkFolder AdServer\tapjoy
	)
	if "%ADS_USE_ADCOLONY%"=="1" (
		call:copyToWorkFolder AdServer\adcolony
	)
	if "%ADS_USE_FLURRY%"=="1" (
		call:copyToWorkFolder AdServer\flurry
	)
	if "%ADS_USE_YUME%"=="1" (
		call:copyToWorkFolder AdServer\yume
	)
	if "%ADS_USE_CHARTBOOST%"=="1" (
		call:copyToWorkFolder AdServer\chartboost
	)
	if "%USE_HAS_OFFERS_TRACKING%"=="1" (
		call:copyToWorkFolder AdServer\hasoffers
	)
)

REM ------------- PSS -------------------
if "%USE_PSS%"=="1" call:copyToWorkFolder PSS -r -a

REM REM ---------- JAX games ----------------
REM if "%USE_JAXS_GAME%"=="1" call:copyToWorkFolder Jax -r

REM ------------- Push Notification -------------
if "%SIMPLIFIED_PN%"=="1" (
	call:copyToWorkFolder PushNotification -r
	if "%AMAZON_STORE%"=="1" (
		if not exist %ROOT_DIR%\ext_libs (
		echo ::: Creating ..\Package\ext_libs folder
		md %ROOT_DIR%\ext_libs
		)
		echo ::: Copying adm.jar library to ..\Package\ext_libs
		if exist %LIBS_FOLDER%\adm.jar xcopy %LIBS_FOLDER%\adm.jar %ROOT_DIR%\ext_libs /Y>>NUL
		del /S /Q %LIBS_FOLDER%\adm.jar>>NUL
		if exist %LIBS_FOLDER%\gcm.jar del /S /Q %LIBS_FOLDER%\gcm.jar>>NUL
		if exist %LIBS_FOLDER%\nokia.jar del /S /Q %LIBS_FOLDER%\nokia.jar>>NUL
	) else (
		if exist %LIBS_FOLDER%\adm.jar del /S /Q %LIBS_FOLDER%\adm.jar>>NUL
	)
	
	if "%USE_NOKIA_API%"=="1" (
		if exist %LIBS_FOLDER%\gcm.jar del /S /Q %LIBS_FOLDER%\gcm.jar>>NUL
		if exist %LIBS_FOLDER%\adm.jar del /S /Q %LIBS_FOLDER%\adm.jar>>NUL
		if exist %LIBS_FOLDER%\google-play-services.jar del /S /Q %LIBS_FOLDER%\adm.jar>>NUL
	) else (
		if exist %LIBS_FOLDER%\nokia.jar del /S /Q %LIBS_FOLDER%\nokia.jar>>NUL
	)
)

REM SET FILE=%APK_ANDROID_MANIFEST%
if "%USE_GOOGLE_ANALYTICS_TRACKING%"=="1" (
	echo ::: preprocessing %APK_RESOURCES%\GoogleAnalyticsTracking\res\xml\analytics.xml %WORK_FOLDER%\res\xml\analytics.xml
	%PREPROCESS_APP% -P -include %ANDROID_CONFIG_FILE% %APK_RESOURCES%\GoogleAnalyticsTracking\res\xml\analytics.xml %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL
	%PREPROCESS_XML_APP% %WORK_FOLDER%\res\xml\tmpXML.xml %WORK_FOLDER%\res\xml\analytics.xml>>NUL
	del /Q %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL

	echo ::: preprocessing %APK_RESOURCES%\GoogleAnalyticsTracking\res\xml\global_config.xml %WORK_FOLDER%\res\xml\global_config.xml
	%PREPROCESS_APP% -P -include %ANDROID_CONFIG_FILE% %APK_RESOURCES%\GoogleAnalyticsTracking\res\xml\global_config.xml %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL
	%PREPROCESS_XML_APP% %WORK_FOLDER%\res\xml\tmpXML.xml %WORK_FOLDER%\res\xml\global_config.xml>>NUL
	del /Q %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL
)


echo .

if "%USE_BLUE_LOGO%"=="1" (
	echo :: Using Blue Logo...
	echo .
	if exist "%APK_RESOURCES%\installer\BlueLogo\res\*.*" (
		xcopy %APK_RESOURCES%\installer\BlueLogo\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
	)
)

REM ------------- Game Specific -------------
echo :: Game Specific resources...
echo .
if exist "%APK_RES_FOLDER%" (
	xcopy %APK_RES_FOLDER%\*.* %TMP_RES_FOLDER% /E /Y>>NUL
)
if exist "%APK_ASSETS_FOLDER%" (
	xcopy %APK_ASSETS_FOLDER%\*.* %TMP_ASSETS_FOLDER% /E /Y>>NUL
)
if exist "%APK_LIBS_FOLDER%" (
    if exist "%APK_LIBS_FOLDER%\armeabi\" ( xcopy %APK_LIBS_FOLDER%\armeabi\*.* %LIBS_FOLDER%\armeabi\ /E /Y>>NUL )
    if exist "%APK_LIBS_FOLDER%\armeabi-v7a\" ( xcopy %APK_LIBS_FOLDER%\armeabi-v7a\*.* %LIBS_FOLDER%\armeabi-v7a\ /E /Y>>NUL )
    if exist "%APK_LIBS_FOLDER%\mips\" ( xcopy %APK_LIBS_FOLDER%\mips\*.* %LIBS_FOLDER%\mips\ /E /Y>>NUL )
    if exist "%APK_LIBS_FOLDER%\x86\" ( xcopy %APK_LIBS_FOLDER%\x86\*.* %LIBS_FOLDER%\x86\ /E /Y>>NUL )
	xcopy %APK_LIBS_FOLDER%\*.* %LIBS_FOLDER% /E /Y
)
echo .

rem /////////////////////////////////////////////////////
rem  Preprocessing sources files
rem /////////////////////////////////////////////////////
rem Generate data link: data.txt
set DATA_URL_FILE=%TMP_RES_FOLDER%\raw\data.txt
if "%USE_INSTALLER%"=="1" (
	echo :: Generating data link ...
	rem del /Q %DATA_URL_FILE%>>NUL
	if "%USE_MARKET_INSTALLER%"=="1" (
		echo DYNAMIC:%DOWNLOAD_URL%>%DATA_URL_FILE%
	) else (
		rem if "%USE_DYNAMIC_DOWNLOAD_LINK%"=="1" (
			echo DYNAMIC:%DOWNLOAD_URL%>%DATA_URL_FILE%
		rem ) else (
		rem echo PVRT:%DATA_DOWNLOAD_SERVER%%DOWNLOAD_FILE_NAME_PVR%>%DATA_URL_FILE%
		rem echo ETC:%DATA_DOWNLOAD_SERVER%%DOWNLOAD_FILE_NAME_ETC%>>%DATA_URL_FILE%
		rem echo ATC:%DATA_DOWNLOAD_SERVER%%DOWNLOAD_FILE_NAME_ATC%>>%DATA_URL_FILE%
		rem echo DXT:%DATA_DOWNLOAD_SERVER%%DOWNLOAD_FILE_NAME_DXT%>>%DATA_URL_FILE%
		)
	)
	echo .
)


copy %SRC_FOLDER%\java_defines.h %JNI_FOLDER%>>NUL

REM ------------- Manifest -------------
SET FILE=%APK_ANDROID_MANIFEST%
echo ::: preprocessing AndroidManifest.xml
%PREPROCESS_APP% -P -include %ANDROID_CONFIG_FILE% %FILE% %WORK_FOLDER%\tmpXML.xml>>NUL
%PREPROCESS_XML_APP% %WORK_FOLDER%\tmpXML.xml %WORK_FOLDER%\tmpXML2.xml>>NUL
%POSTPROCESS_XML_APP% %WORK_FOLDER%\tmpXML2.xml %WORK_FOLDER%\AndroidManifest.xml>>NUL
del /Q %WORK_FOLDER%\tmpXML.xml>>NUL
del /Q %WORK_FOLDER%\tmpXML2.xml>>NUL
echo .

REM ------------- common -------------
call:preprocessFolder common

REM ------------- Utils -------------
call:preprocessFolder utils "-include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------- Installer -------------
if "%USE_INSTALLER%"=="1" call:preprocessFolder installer

REM ------------- Beam -------------
if "%USE_BEAM%"=="1" call:preprocessFolder Beam

REM ------------- Verizon DRM -------------
if "%VERIZON_DRM%"=="1" call:preprocessFolder DRM\Verizon

REM ------------- Gloft DRM ------------- rem DRM\Gloft to DRM\GloftNew
if "%GLOFT_DRM%"=="1" call:preprocessFolder DRM\GloftNew

REM ------------- TMobile DRM -------------
if "%TMOBILE_DRM%"=="1" call:preprocessFolder DRM\TMobile

REM ------------- Google DRM -------------
if "%GOOGLE_DRM%"=="1" call:preprocessFolder DRM\Google_DRM

REM ------------- APK Expansion Files -------------
if "%GOOGLE_MARKET_DOWNLOAD%"=="1" call:preprocessFolder GoogleExpansionFiles


REM ------------- SKT DRM -------------
if "%USE_SKT_DRM%"=="1" call:preprocessFolder DRM\SKT_DRM

REM ------------- Pantech ARM -------------
if "%USE_PANTECH_ARM%"=="1" call:preprocessFolder DRM\PANTECH_ARM


REM ------------- LGU DRM -------------
if "%USE_LGU_DRM%"=="1" call:preprocessFolder DRM\LGU_DRM

REM ------------- LGW DRM -------------
if "%USE_LGW_DRM%"=="1" call:preprocessFolder DRM\LGW_DRM

REM ------------- KT DRM -------------
if "%USE_KT_DRM%"=="1" call:preprocessFolder DRM\KT_DRM

REM ------------- SAMSUNG DRM (Zirconia) -------------
if "%USE_SAMSUNG_DRM%"=="1" call:preprocessFolder DRM\Samsung
REM ------------- OPTUS DRM -------------
if "%USE_OPTUS_DRM%"=="1" call:preprocessFolder DRM\OPTUS_DRM
REM ------------- Orange DRM -------------
if "%ORANGE_DRM%"=="1" call:preprocessFolder DRM\ORANGE_DRM

REM ------------- Verizon Billing -------------
if "%USE_VZW_BILLING%"=="1" call:preprocessFolder VzwBilling

REM ------------- Billing -------------
if "%USE_BILLING%"=="1" call:preprocessFolder billing
set /A HAS_BILLING_AND_CRM=%USE_BILLING%%USE_IN_APP_BILLING_CRM%
if %HAS_BILLING_AND_CRM% EQU 11 call:preprocessFolder InAppBilling\common "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"


if "%USE_IN_APP_BILLING_CRM%"=="1" goto SKIP_IAP_PACKAGE_CODE
REM ------------ In App Billing && Billing -------------
if "%USE_IN_APP_BILLING%"=="1" goto DO_BILLING
if "%USE_BILLING%"=="1" goto DO_BILLING
goto SKIP_BILLING
:DO_BILLING
	call:preprocessFolder InAppBilling\common "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
:SKIP_BILLING

REM ------------ In App Billing -------------
if "%USE_IN_APP_BILLING%"=="1" call:preprocessFolder InAppBilling\src "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%GOOGLE_STORE_V3%"=="1" (
	call:preprocessFolder InAppBilling\google_play_v3 "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
	rem xcopy %SRC_FOLDER%\InAppBilling\google_play_v3\*.aidl %TMP_SRC_FOLDER%\com\android\vending\billing\*.aidl>>NUL
	xcopy %APK_RESOURCES%\InAppBilling\google_play_v3\libs\*.aidl %TMP_SRC_FOLDER%\com\android\vending\billing\*.aidl>>NUL
)

REM ------------ In App Billing -------------
if "%BAZAAR_STORE%"=="1" (
	call:preprocessFolder InAppBilling\bazaar "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
	xcopy %APK_RESOURCES%\InAppBilling\bazaar\libs\*.aidl %TMP_SRC_FOLDER%\com\android\vending\billing\*.aidl>>NUL
	rem %TMP_SRC_FOLDER%\com\bazaar\store\service\*.aidl>>NUL
)

REM ------------ In App Billing -------------
if "%YANDEX_STORE%"=="1" (
	call:preprocessFolder InAppBilling\yandex "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
	xcopy %APK_RESOURCES%\InAppBilling\yandex\libs\*.aidl %TMP_SRC_FOLDER%\com\yandex\store\service\*.aidl>>NUL
)


REM ------------ In App Billing -------------
if "%AMAZON_STORE%"=="1" call:preprocessFolder InAppBilling\amazon "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%GAMELOFT_SHOP%"=="1" call:preprocessFolder InAppBilling\gameloft "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%VZW_STORE%"=="1" (
	call:preprocessFolder InAppBilling\verizon\src "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
	if "%VZW_SCM%" == "1" call:preprocessFolder InAppBilling\verizon\scm "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
	if "%VZW_MTX%" == "1" call:preprocessFolder InAppBilling\verizon\mtx "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
)

REM ------------ In App Billing -------------
if "%SAMSUNG_STORE%"=="1" call:preprocessFolder InAppBilling\samsung "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%HUAWEI_STORE%"=="1" (
	call:preprocessFolder InAppBilling\huawei "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"
	xcopy %APK_RESOURCES%\InAppBilling\huawei\libs\*.aidl %TMP_SRC_FOLDER%\com\huawei\dsm\aidl\*.aidl>>NUL
)

REM ------------ In App Billing -------------
if "%VXINYOU_STORE%"=="1" call:preprocessFolder InAppBilling\vxinyou "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%ZTE_STORE%"=="1" call:preprocessFolder InAppBilling\zte "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h

REM ------------ In App Billing -------------
if "%PANTECH_STORE%"=="1" call:preprocessFolder InAppBilling\pantech "-include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%SKT_STORE%"=="1" call:preprocessFolder InAppBilling\skt "-include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------  
if "%KT_STORE%"=="1" call:preprocessFolder InAppBilling\kt "-include  %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%BOKU_STORE%"=="1" call:preprocessFolder InAppBilling\boku "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

REM ------------ In App Billing -------------
if "%ATET_STORE%"=="1" call:preprocessFolder InAppBilling\atet_tv "-include %NATIVE_SRC_FOLDER%\InAppBilling\iab_strings_encoded.h -include %NATIVE_SRC_FOLDER%\InAppBilling\InAppBillingConst.h"

:SKIP_IAP_PACKAGE_CODE


=======REM ------------ HEP Boku -------------
set FOLDER=BokuForBilling
set TMP_FOLDER=%TMP_SRC_FOLDER%\%FOLDER%
if "%USE_BOKU_FOR_BILLING%" == "1" (
	echo ::: preprocessing %FOLDER%
	if not exist "%TMP_FOLDER%"   md %TMP_FOLDER%
	pushd %SRC_FOLDER%\%FOLDER%
	for /R %%F in (*java) do (
		copy %%F %TMP_FOLDER%\%%~nF.jpp>>NUL
	)
	popd
	echo .
	pushd %TMP_FOLDER%
	for %%i in (*.jpp) do (
		%PREPROCESS_APP% -C -P -include %ANDROID_CONFIG_FILE% -include %NATIVE_SRC_FOLDER%\%FOLDER%\BokuConst.h -imacros %JNI_FOLDER%\java_defines.h %%i %%~ni.java>>NUL
	)
	del /S /Q *.jpp>>NUL
	popd
)



REM ------------ KDDI Gift -------------
if "%USE_KDDI_GIFTING%"=="1" call:preprocessFolder KDDIGifting

REM ------------- GLLive ------------
if "%USE_GLLIVE%"=="1" call:preprocessFolder gllive

REM ------------- GLLive HTML5 ---------------
if "%USE_GLLIVE_HTML5%"=="1" call:preprocessFolder GLiveHTML5

REM ------------- IGP HTML ------------
if "%USE_IGP_ACTIVITY%"=="1" call:preprocessFolder IGP_HTML

REM ------------- IGP FREEMIUM ------------
if "%USE_IGP_FREEMIUM%"=="1" call:preprocessFolder IGP_Freemium

REM ------------- DIRECT IGP ------------
if "%USE_DIRECT_IGP%"=="1" call:preprocessFolder DirectIGP

REM ------------- IN GAME BROWSER ------------
if "%USE_IN_GAME_BROWSER%"=="1" call:preprocessFolder InGameBrowser



REM ------------- ADS SERVER ------------
if "%USE_ADS_SERVER%"=="1" (
	call:preprocessFolder AdServer\adserver
	if "%ADS_USE_FLURRY%"=="1" (
		call:preprocessFolder AdServer\flurry
	)
	if "%ADS_USE_YUME%"=="1" (
		call:preprocessFolder AdServer\yume
	)
)


REM ------------- SPLASH SCREEN -------------
set INCLUDE_SPLASH=0
if "%USE_WELCOME_SCREEN%"=="1" set INCLUDE_SPLASH=1
if "%USE_HOC_SCREEN%"=="1" set INCLUDE_SPLASH=1
if "%INCLUDE_SPLASH%"=="1" call:preprocessFolder SplashScreen

REM ------------- Video Player ------------
if "%USE_VIDEO_PLAYER%"=="1" call:preprocessFolder video

REM ------------- Push Notification -------------
if "%SIMPLIFIED_PN%"=="1" call:preprocessFolder PushNotification

REM ------------- Google Analytics -------------
if "%USE_GOOGLE_ANALYTICS_TRACKING%"=="1" call:preprocessFolder GoogleAnalyticsTracking

REM -------------- Plugin List --------------

set FOLDER=%ANDROID_FRAMEWORK_CONFIG%
if exist %FOLDER% (
    echo ::: preprocessing GameSpecific
    pushd %FOLDER%
        setlocal enabledelayedexpansion
        For /f %%L in ('dir *java /s /b /A:-H') do (
            set sourceFile=%%L
            set onlyName=%%~nL
            set newName=!onlyName!.jpp
            copy !sourceFile! %TMP_SRC_FOLDER%\!newName!>>NUL
        )
        setlocal disabledelayedexpansion

        copy *.h %TMP_SRC_FOLDER%\>>NUL
    popd
    echo .
) else (
    @echo ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR 
    @echo ERROR: AndroidFrameworkConfig does not exist!
    @echo FIX: Run setup to generate the folder AndroidFrameworkConfig for you.
    @echo ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR 
    exit /b 1
)

cd %ANDROID_FRAMEWORK_DIR%

pushd %TMP_SRC_FOLDER%
	for %%i in (*.jpp) do (
		%PREPROCESS_APP% -C -P %PREPROCESS_USING_INCLUDE_FILE% -include %ANDROID_CONFIG_FILE% -imacros %JNI_FOLDER%\java_defines.h %%i %%~ni.java>>NUL
	)
	del /S /Q *.jpp>>NUL
popd

REM --------------End Plugin List -----------

REM ------------- Game -------------
set FOLDER=%APK_SRC_FOLDER%
echo ::: preprocessing GameSpecific
pushd %FOLDER%
	setlocal enabledelayedexpansion
	For /f %%L in ('dir *java /s /b /A:-H') do (
		set sourceFile=%%L
		set onlyName=%%~nL
		set newName=!onlyName!.jpp
		copy !sourceFile! %TMP_SRC_FOLDER%\!newName!>>NUL
	)
	setlocal disabledelayedexpansion

	copy *.h %TMP_SRC_FOLDER%\>>NUL
popd
echo .

if exist "%GAME_SPECIFIC_PROCESS%" (
	call %GAME_SPECIFIC_PROCESS%
)
cd %ANDROID_FRAMEWORK_DIR%

pushd %TMP_SRC_FOLDER%
	for %%i in (*.jpp) do (
		%PREPROCESS_APP% -C -P %PREPROCESS_USING_INCLUDE_FILE% -include %ANDROID_CONFIG_FILE% -imacros %JNI_FOLDER%\java_defines.h %%i %%~ni.java>>NUL
	)
	del /S /Q *.jpp>>NUL
popd

REM ---------- PSS -----------------
if "%USE_PSS%"=="1" call:preprocessFolder PSS

REM ------------- Data Sharing -------------
if "%USE_DATA_SHARING%"=="1" call:preprocessFolder DataSharing

REM ---------- LZMA ----------------
set FOLDER=SevenZip
set TMP_FOLDER=%TMP_SRC_FOLDER%\%FOLDER%
if "%USE_LZMA%"=="1" (	
	echo ::: copying src %FOLDER%
	if not exist "%TMP_FOLDER%" md %TMP_FOLDER%
	xcopy /s /y %SRC_FOLDER%\%FOLDER% %TMP_FOLDER%
)

REM REM ---------- JAX games ----------------
REM if "%USE_JAXS_GAME%"=="1" (
REM 	echo --------------------------
REM 	call:preprocessFolder Jax
REM 
REM 	echo - Extract data to asset.dir or Sd_common
REM 	if "%USE_JAXS_INSTALLER%"=="0" (
REM 		pushd %TMP_ASSETS_FOLDER%
REM 		for /f %%I in ('dir %JAX_RESOURCES% /b /AD-H-S') do (		
REM 			if exist "%%~nI" (
REM 				rd %%~nI /s/q >>NUL
REM 			)
REM 			md %%~nI
REM 			pushd %%~nI
REM 			for /f %%J in ('dir %JAX_RESOURCES%\%%I\*.jar /b') do (			
REM 				%TOOLS_DIR%\compress\7za.exe x -x!*.class %JAX_RESOURCES%\%%I\%%J >>NUL			
REM 			)
REM 			del /q META-INF\*.* >>NUL
REM 			rd META-INF>> NUL		
REM 			popd
REM 		)
REM 		popd
REM 	) else (
REM 		pushd %SD_RESOURCES_COMMON%
REM 		for /f %%I in ('dir %JAX_RESOURCES% /b /AD-H-S') do (		
REM 			if exist "%%~nI" (
REM 				rd %%~nI /s/q >>NUL
REM 			)
REM 			md %%~nI
REM 			pushd %%~nI
REM 			for /f %%J in ('dir %JAX_RESOURCES%\%%I\*.jar /b') do (			
REM 				%TOOLS_DIR%\compress\7za.exe x -x!*.class %JAX_RESOURCES%\%%I\%%J >>NUL			
REM 			)
REM 			del /q META-INF\*.* >>NUL
REM 			rd META-INF>> NUL
REM 			popd
REM 		)
REM 		popd	
REM 	)
REM 
REM 	echo - Fix gllib problems
REM 	for /f %%I in ('dir %JAX_RESOURCES% /b /AD-H-S') do (
REM 		pushd %LIBS_FOLDER%
REM 		for /f %%J in ('dir %JAX_RESOURCES%\%%I\*.jar /b') do (
REM 			copy %JAX_RESOURCES%\%%I\%%J %%~nI%%~xJ.Jaxstmp >>NUL
REM 			"%TOOLS_DIR%\compress\7za.exe" d %%~nI%%~xJ.Jaxstmp -x!*.class >>NUL	
REM 			"%JAVA_HOME%\bin\java" -cp "%TOOLS_DIR%\fixgllib\lib\fixgllib.jar" gameloft.cor.asm.ASMFixGLLibAndroidWrapper  -j %%~nI%%~xJ.Jaxstmp -o %%~nI.JaxsFixGllib -fixres -removefinal -keepcons -keepgc -dontBackup >>NUL
REM 		)
REM 		popd
REM 	)	
REM 	
REM 	echo - Repackage game to %APP_PACKAGE%.**
REM 	pushd %LIBS_FOLDER%
REM 	for /f %%I in ('dir *.JaxsFixGllib /b') do (	
REM 		echo    + %APP_PACKAGE%.S%%~nI
REM 		"%JAVA_HOME%\bin\java" -cp "%TOOLS_DIR%\asmpackager\lib\asmpackager.jar" gameloft.cor.asm.ASMPackager -j  %%I -o %%~nI.jar -pkg %APP_PACKAGE%.S%%~nI -onepkg >>NUL
REM 	)
REM 	popd
REM 
REM 	pushd %LIBS_FOLDER%	
REM 	del *.JaxsFixGllib
REM 	del *.Jaxstmp
REM 	del *.bak
REM 	popd
REM )

if "%USE_GLLIVE_HTML5%"=="0" (
	if exist %LIBS_FOLDER%\%TYPE_ARMEABI%\libGLiveIAP.so del /S /Q %LIBS_FOLDER%\%TYPE_ARMEABI%\libGLiveIAP.so>>NUL	
	if exist %LIBS_FOLDER%\libGLiveIAP.so del /S /Q %LIBS_FOLDER%\libGLiveIAP.so>>NUL	
	set GLLIVE_EMBED_PAYPAL=0
	set GLLIVE_EMBED_BOKU=0	
) else (
	if "%GLIVE_GL_SHOP%"=="0" (
		if exist %LIBS_FOLDER%\%TYPE_ARMEABI%\libGLiveIAP.so del /S /Q %LIBS_FOLDER%\%TYPE_ARMEABI%\libGLiveIAP.so>>NUL
		if exist %LIBS_FOLDER%\libGLiveIAP.so del /S /Q %LIBS_FOLDER%\libGLiveIAP.so>>NUL
		set GLLIVE_EMBED_PAYPAL=0
		set GLLIVE_EMBED_BOKU=0		
	) else (		
		call glive_preprocess.bat
		call glive_buildlib.bat
	)
)

if "%USE_DIRECT_IGP%"=="1" (
	call directigp_preprocess.bat
	call directigp_buildlib.bat
) else (
	if exist %LIBS_FOLDER%\%TYPE_ARMEABI%\libDirectIGP.so del /S /Q %LIBS_FOLDER%\%TYPE_ARMEABI%\libDirectIGP.so>>NUL
	if exist %LIBS_FOLDER%\libDirectIGP.so del /S /Q %LIBS_FOLDER%\libDirectIGP.so>>NUL
	set DIGP_PAYPAL=0
	set DIGP_EMBED_BOKU=0
)
	

if "%USE_IN_APP_BILLING_CRM%"=="1" (
	echo %IN_APP_PURCHASE_LIB_CRM_PATH%\project\android\crm\preprocess.bat
	call %IN_APP_PURCHASE_LIB_CRM_PATH%\project\android\crm\preprocess.bat
)
	
set POP_UPS_LIB_CRM_PATH=%~dp0..\PopUpsLib
if "%USE_POPUPSLIB%"=="1" (
	echo %POP_UPS_LIB_CRM_PATH%\prj\android\preprocess_popups.bat
	call %POP_UPS_LIB_CRM_PATH%\prj\android\preprocess_popups.bat
)
	
call %SCRIPT_DIR%\proguard.bat

REM ------------- Game Specific postbuild -------------
if exist "%GAME_SPECIFIC_POSTPROCESS%" (
	call %GAME_SPECIFIC_POSTPROCESS%
)

REM -------------  -------------
del /S /Q %TMP_SRC_FOLDER%\*.h>>NUL

echo ---------------------------------------------------
echo Creating Pdf File with the current settings...
"%PYTHON%" %PDF_CREATOR% %RESOURCES_BIN%\%APP_NAME%.pdf

:end
	echo ---------------------------------------------------
	echo END Preprocess
	echo ---------------------------------------------------
goto:eof

:copyToWorkFolder
	rem Usage:
	rem %1 Which folder
	rem %2 which folders
	echo :: copying %1 resources...
	
rem avoid params, if folder exists copy it.
	REM echo %2%3%4 | findstr "r">>NUL
	REM if errorlevel 1 set FLAG_R=1
	REM echo %2%3%4 | findstr "a">>NUL
	REM if errorlevel 1 set FLAG_A=1
	REM echo %2%3%4 | findstr "l">>NUL
	REM if errorlevel 1 set FLAG_L=1
	REM if "FLAG_R" == "1" do (
		REM xcopy %APK_RESOURCES%\%1\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
	REM )
	REM if "FLAG_A" == "1" do (
		REM xcopy %APK_RESOURCES%\%1\assets\*.* %TMP_ASSETS_FOLDER% /E /Y>>NUL
	REM )
	REM if "FLAG_L" == "1" do (
		REM xcopy %APK_RESOURCES%\%1\libs\*.* %LIBS_FOLDER% /E /Y>>NUL
	REM )
	if exist "%APK_RESOURCES%\%1\res\*.*" (
		xcopy %APK_RESOURCES%\%1\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
	)
	if exist "%APK_RESOURCES%\%1\assets\*.*" (
		xcopy %APK_RESOURCES%\%1\assets\*.* %TMP_ASSETS_FOLDER% /E /Y>>NUL
	)
	if exist "%APK_RESOURCES%\%1\libs\*.*" (
			xcopy %APK_RESOURCES%\%1\libs\*.* %LIBS_FOLDER% /E /Y>>NUL
	)
	echo .
	REM set FLAG_R=
	REM set FLAG_A=
	REM set FLAG_L=
goto:eof

:deleteFile
	if exist %1 del /Q /S %1>>NUL
goto:eof

:preprocessFolder
	if not exist "%SRC_FOLDER%\%1" (
		echo WARNING: You are trying to copy a not existing module [%1]
		goto:eof
	)
	set TMP_FOLDER=%TMP_SRC_FOLDER%\%1
	echo ::: preprocessing %1
	if not exist "%TMP_FOLDER%" md %TMP_FOLDER%

	pushd %SRC_FOLDER%\%1
		for /R %%F in (*java) do (
			copy %%F %TMP_FOLDER%\%%~nF.jpp>>NUL
		)
	popd
	echo .

	pushd %TMP_FOLDER%
		for %%i in (*.jpp) do (
			%PREPROCESS_APP% -C -P %PREPROCESS_USING_INCLUDE_FILE% -include %ANDROID_CONFIG_FILE% %~2 -imacros %JNI_FOLDER%\java_defines.h %%i %%~ni.java>>NUL
		)

		del /S /Q *.jpp>>NUL
	popd
goto:eof
:copyToWorkFolderInstallerImagesForCrashChecker
	if exist "%APK_RESOURCES%\installer\res" (
		echo copy %APK_RESOURCES%\installer\res\drawable\*.png %TMP_RES_FOLDER%\drawable\
		xcopy %APK_RESOURCES%\installer\res\drawable\*.png %TMP_RES_FOLDER%\drawable\ /E /Y>>NUL
		echo copy %APK_RESOURCES%\installer\res\drawable-xlarge\*.png
		xcopy %APK_RESOURCES%\installer\res\drawable-xlarge\*.png %TMP_RES_FOLDER%\drawable-xlarge\ /E /Y>>NUL
	)
	echo .
goto:eof
