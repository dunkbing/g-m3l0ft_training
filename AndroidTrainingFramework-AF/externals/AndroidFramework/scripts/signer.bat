@echo off
REM *****************************************************************
REM signer.bat
REM 	Sign and Aling APK using Gameloft Key, useful for AMAZON
REM usage:
REM 	signer.bat unsigned_file.apk
REM		or drag and drop file
REM *****************************************************************

rem fix running from drag&drop
cd %~dp0
if exist "%ANDROID_FRAMEWORK_CONFIG%config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%config.bat
)

set FILE_TO_SIGN=%1
set FINAL_APK_NAME=%~n1_SignedAndAligned.apk

rem call to check Env & Config
call %ROOT_DIR%\checkEnv.bat
call %ROOT_DIR%\checkConfig.bat

if not "%CONFIG_ERROR%" == "" (exit 1)
if "%FILE_TO_SIGN%" == "" (goto error)

echo =========================================================
echo Signing [%FILE_TO_SIGN%]
%JAVA_HOME%\bin\jarsigner -keystore GAMELOFT_KEY.keystore -storepass 123456 %FILE_TO_SIGN% Gameloft

echo.
echo =========================================================
echo Aligning [%FILE_TO_SIGN%]
%ANDROID_HOME%\tools\zipalign -v 4 %FILE_TO_SIGN% %FINAL_APK_NAME%

echo.
echo =========================================================
echo Verifying signature [%FINAL_APK_NAME%]
%JAVA_HOME%\bin\jarsigner -verify %FINAL_APK_NAME%

goto end

:error
	echo =========================================================
	echo Usage: signer.bat unsigned_file.apk
	echo.
:end
