@echo off

set ROOT_DIR=%~dp0
set SCRIPT_DIR=%ROOT_DIR%scripts

set TYPE_OF_BUILD=%1
set PAUSE=%2

if NOT "%ANDROID_SETENV_DONE%"=="1" (
	call %SCRIPT_DIR%\setEnv_prev.bat
	call %ANDROID_FRAMEWORK_CONFIG%\configs\project\setEnv.bat
	call %SCRIPT_DIR%\setEnv_post.bat
	set ANDROID_SETENV_DONE=1
)

if NOT "%ANDROID_CONFIG_DONE%"=="1" (
	call %SCRIPT_DIR%\defaults.bat
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
	set ANDROID_CONFIG_DONE=1
)

if "%PAUSE%"=="pause_at_end" (
	call cecho {white}{\n}
	call cecho {white}{\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {aqua}                 Installing {\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {white}{\n}
) 

if "%TYPE_OF_BUILD%"=="debug" (
	call %ADB% install -r %ANDROID_FRAMEWORK_DIR%\bin\%APP_NAME%_%VERSION%_DEBUG.apk
) else (
	call %ADB% install -r %ANDROID_FRAMEWORK_DIR%\bin\%APP_NAME%_%VERSION%.apk
)


if "%PAUSE%"=="pause_at_end" (
	call %NOTIFU% "Install finished."
	echo.

	pause
)

