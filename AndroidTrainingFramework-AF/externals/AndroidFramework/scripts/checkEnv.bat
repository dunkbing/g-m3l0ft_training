@echo off
rem ********************************************
rem 	Checking paths & tools
rem ********************************************
set PATH_ERROR=0
if not exist "%ANDROID_FRAMEWORK_CONFIG%" (
	set PATH_ERROR=1
	echo :: ANDROID_FRAMEWORK_CONFIG not found.        value:[%ANDROID_FRAMEWORK_CONFIG%]
)
if not exist "%APK_ANDROID_MANIFEST%" (
	set PATH_ERROR=1
	echo :: APK_ANDROID_MANIFEST not found. value:[%APK_ANDROID_MANIFEST%]
)
if not exist "%NDK_BUILD%" (
	set PATH_ERROR=1
	echo :: NDK_BUILD not found.            value:[%NDK_BUILD%]
)
if not exist "%CYGWIN_BASH%" (
	set PATH_ERROR=1
	echo :: CYGWIN_BASH not found.          value:[%CYGWIN_BASH%]
)
if not exist "%PYTHON%" (
	set PATH_ERROR=1
	echo :: PYTHON not found.               value:[%PYTHON%]
)
if not exist "%APACHE_ANT%" (
	set PATH_ERROR=1
	echo :: APACHE_ANT not found.           value:[%APACHE_ANT%]
)
if not exist "%ADB%" (
	set PATH_ERROR=1
	echo :: ADB not found.                  value:[%ADB%]
)
if not exist "%PREPROCESS_APP%" (
	set PATH_ERROR=1
	echo :: PREPROCESS_APP not found.       value:[%PREPROCESS_APP%]
)
if not exist "%PREPROCESS_XML_APP%" (
	set PATH_ERROR=1
	echo :: PREPROCESS_XML_APP not found.   value:[%PREPROCESS_XML_APP%]
)
if not exist "%RESPACK%" (
	set PATH_ERROR=1
	echo :: RESPACK not found.              value:[%RESPACK%]
)


if %PATH_ERROR% == 1 (
	echo =================================================================
	echo :: ERROR: Please break the batch CTRL+C and fix the paths.
	echo =================================================================
	pause
)
