@echo off
if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)
call %ANDROID_TOOLS%\ddms.bat
