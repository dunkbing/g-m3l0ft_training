@echo off

if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)

call %ANDROID_PLATFORM_TOOLS%\adb kill-server
call %ANDROID_PLATFORM_TOOLS%\adb start-server
call %ANDROID_PLATFORM_TOOLS%\adb shell monkey -p %APP_PACKAGE% -v 50000 > MonkeyLog.txt
