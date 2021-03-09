@echo off

set ANDROID_FRAMEWORK_CONFIG=%1

if NOT "%ANDROID_SETENV_DONE%"=="1" (
	call %~dp0\setEnv_prev.bat
	call %ANDROID_FRAMEWORK_CONFIG%\setEnv.bat
	call %~dp0\setEnv_post.bat
	set ANDROID_SETENV_DONE=1
)

if NOT "%ANDROID_CONFIG_DONE%"=="1" (
	call %ANDROID_FRAMEWORK_DIR%\defaults.bat
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
	set ANDROID_CONFIG_DONE=1
)


call %ADB% -d uninstall %APP_PACKAGE%
rem pause
