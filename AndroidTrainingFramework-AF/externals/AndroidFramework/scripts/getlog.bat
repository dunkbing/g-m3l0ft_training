@echo OFF
if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)
call %ADB% logcat -c
call %ADB% shell setprop debug.checkjni 1
del /Q tools\StackTrace\log.txt
call %ADB% logcat 2<&1 | tools\StackTrace\mtee.exe /+ tools\StackTrace\log.txt
