@echo off


echo Installer Version: %INSTALLER_VERSION%

if "%ANDROID_SDK_REVISION%"=="" (
	call %SCRIPT_DIR%\getAndroidRevisionSDK.bat
)
echo Android SDK Version: %ANDROID_SDK_REVISION%

echo Cleaning ...

call:cleanFolder %WORK_FOLDER%
call:cleanFolder %TMP_SRC_FOLDER%
call:cleanFolder %TMP_AIDL_FOLDER%
call:cleanFolder %TMP_RES_FOLDER%
call:cleanFolder %TMP_RES_FOLDER%\raw
call:cleanFolder %TMP_ASSETS_FOLDER%
rem call:cleanFolder %TMP_LIBS_FOLDER%
call:cleanFolder %ROOT_DIR%\bin

if "%USE_HEP_PACKINFO%"=="0" (
	call:deleteFile %APK_RESOURCES%\installer\res\raw\pack.info
)

echo.
echo START Preprocess

if "%TMP_SRC_FOLDER%"=="" (
	echo ERROR: Enviroment not setted
	goto end
)



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

call cecho {white}{\n}
call cecho {teal}Reading Java2Android
call cecho {white}{\n}

%PREPROCESS_APP% -C -P -dD "%ANDROID_CONFIG_FILE%" "%ANDROID_CONFIG_FILE%"

copy /Y "%SRC_FOLDER%\java_defines.h" "%ANDROID_FRAMEWORK_CONFIG%\configs\generated">NUL


set j2a_archs=

if "%BUILD_arm%"=="true" (
	set j2a_archs=armeabi-v7a
)

if "%BUILD_x86%"=="true" (
	if "%BUILD_arm%"=="true" (
		set j2a_archs=%j2a_archs%;x86
	) else (
		set j2a_archs=x86
	)
)

 rem Android Framework Libraries
%TOOLS_DIR%\java2android\bin\java2android.exe --java2android "%ROOT_DIR%\j2a\java2android.xml;%ANDROID_FRAMEWORK_CONFIG%\configs\project\java2android.xml" --preprocess "%PREPROCESS_APP%" --architecture "%j2a_archs%" --zip "%ZIP_APP%"

if %errorlevel% neq 0 (	
	call cecho {white}{\n}
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
		call cecho {red}	Java2Android Failed. Stop the script and fix the issue.{\n}
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {white}{\n}
	set HAS_ERRORS=1
	goto:eof
)

call cecho {white}{\n}
call cecho {teal}Copying apk resources{\n}
call cecho {white}{\n}


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
)

REM ------------- ADD ENCRYPTED DEMO CODE -------------
if "%USE_IGP_CODE_FROM_FILE%" == "1" (
	%TOOLS_DIR%\defines\StringEncoder.exe -encryptkey %GL_DEMO_CODE% %GGC_GAME_CODE% %TMP_RES_FOLDER%\raw\nih.bin
)


REM ------------- Anti-Piracy -------------










REM ------------- Push Notification -------------
if "%SIMPLIFIED_PN%"=="1" (
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
	echo ::: preprocessing %WORK_FOLDER%\res\xml\analytics.xml
	%PREPROCESS_APP% -P -include %ANDROID_CONFIG_FILE% %WORK_FOLDER%\res\xml\analytics.xml %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL
	%PREPROCESS_XML_APP% %WORK_FOLDER%\res\xml\tmpXML.xml %WORK_FOLDER%\res\xml\analytics.xml>>NUL
	del /Q %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL

	echo ::: preprocessing %WORK_FOLDER%\res\xml\global_config.xml
	%PREPROCESS_APP% -P -include %ANDROID_CONFIG_FILE%  %WORK_FOLDER%\res\xml\global_config.xml %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL
	%PREPROCESS_XML_APP% %WORK_FOLDER%\res\xml\tmpXML.xml %WORK_FOLDER%\res\xml\global_config.xml>>NUL
	del /Q %WORK_FOLDER%\res\xml\tmpXML.xml>>NUL
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

REM ------------- Manifest -------------
SET FILE=%APK_ANDROID_MANIFEST%
echo ::: preprocessing AndroidManifest.xml
%PREPROCESS_APP% -P -include %ANDROID_CONFIG_FILE% %FILE% %WORK_FOLDER%\tmpXML.xml>>NUL
%PREPROCESS_XML_APP% %WORK_FOLDER%\tmpXML.xml %WORK_FOLDER%\tmpXML2.xml>>NUL
%POSTPROCESS_XML_APP% %WORK_FOLDER%\tmpXML2.xml %WORK_FOLDER%\AndroidManifest.xml>>NUL
del /Q %WORK_FOLDER%\tmpXML.xml>>NUL
del /Q %WORK_FOLDER%\tmpXML2.xml>>NUL
echo .




	
call %SCRIPT_DIR%\proguard.bat




echo.
echo Creating Pdf File with the current settings...
"%PYTHON%" %PDF_CREATOR% %RESOURCES_BIN%\%APP_NAME%.pdf






:end
goto:eof







rem =================================================================================
rem ==== Functions: =================================================================
rem =================================================================================

:cleanFolder
	if "%1"=="" (
		echo.
		echo   cleanFolder function takes one parameter.
		echo.
		pause
	)
	if exist %1 (
		rd /Q /S %1
	)
	md %1 > nul 2> nul || (
		echo.
		call cecho {white}  [{teal}%1{white}] is an invalid path.{\n}
		echo.
	)
goto:eof

:deleteFile
	if "%1"=="" (
		echo.
		echo   deleteFile function takes one parameter.
		echo.
		pause
	)
	if exist %1 (
		del /Q /S %1>>NUL
	)
goto:eof

rem DEPRECATED
rem :copyToWorkFolder
rem 	rem Usage:
rem 	rem %1 Which folder
rem 	rem %2 which folders
rem 	echo :: copying %1 resources...
rem 	
rem rem avoid params, if folder exists copy it.
rem 	REM echo %2%3%4 | findstr "r">>NUL
rem 	REM if errorlevel 1 set FLAG_R=1
rem 	REM echo %2%3%4 | findstr "a">>NUL
rem 	REM if errorlevel 1 set FLAG_A=1
rem 	REM echo %2%3%4 | findstr "l">>NUL
rem 	REM if errorlevel 1 set FLAG_L=1
rem 	REM if "FLAG_R" == "1" do (
rem 		REM xcopy %APK_RESOURCES%\%1\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
rem 	REM )
rem 	REM if "FLAG_A" == "1" do (
rem 		REM xcopy %APK_RESOURCES%\%1\assets\*.* %TMP_ASSETS_FOLDER% /E /Y>>NUL
rem 	REM )
rem 	REM if "FLAG_L" == "1" do (
rem 		REM xcopy %APK_RESOURCES%\%1\libs\*.* %LIBS_FOLDER% /E /Y>>NUL
rem 	REM )
rem 	if exist "%APK_RESOURCES%\%1\res\*.*" (
rem 		xcopy %APK_RESOURCES%\%1\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
rem 	)
rem 	if exist "%APK_RESOURCES%\%1\assets\*.*" (
rem 		xcopy %APK_RESOURCES%\%1\assets\*.* %TMP_ASSETS_FOLDER% /E /Y>>NUL
rem 	)
rem 	if exist "%APK_RESOURCES%\%1\libs\*.*" (
rem 			xcopy %APK_RESOURCES%\%1\libs\*.* %LIBS_FOLDER% /E /Y>>NUL
rem 	)
rem 	echo .
rem 	REM set FLAG_R=
rem 	REM set FLAG_A=
rem 	REM set FLAG_L=
rem goto:eof
rem 
rem :preprocessFolder
rem 	if not exist "%SRC_FOLDER%\%1" (
rem 		echo WARNING: You are trying to copy a not existing module [%1]
rem 		goto:eof
rem 	)
rem 	set TMP_FOLDER=%TMP_SRC_FOLDER%\%1
rem 	echo ::: preprocessing %1
rem 	if not exist "%TMP_FOLDER%" md %TMP_FOLDER%
rem 
rem 	pushd %SRC_FOLDER%\%1
rem 		for /R %%F in (*java) do (
rem 			copy %%F %TMP_FOLDER%\%%~nF.jpp>>NUL
rem 		)
rem 	popd
rem 	echo .
rem 
rem 	pushd %TMP_FOLDER%
rem 		for %%i in (*.jpp) do (
rem 			%PREPROCESS_APP% -C -P %PREPROCESS_USING_INCLUDE_FILE% -include %ANDROID_CONFIG_FILE% %~2 -imacros %ANDROID_FRAMEWORK_CONFIG%\configs\generated\java_defines.h %%i %%~ni.java>>NUL
rem 		)
rem 
rem 		del /S /Q *.jpp>>NUL
rem 	popd
rem goto:eof
rem 
