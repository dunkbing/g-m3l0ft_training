@echo off

set ROOT_DIR=%~dp0
set SCRIPT_DIR=%ROOT_DIR%scripts

set FULL=%1
set PAUSE=%2


if NOT "%ANDROID_SETENV_DONE%"=="1" (
	call %SCRIPT_DIR%\setEnv_prev.bat
	call %ANDROID_FRAMEWORK_CONFIG%\config\project\setEnv.bat
	call %SCRIPT_DIR%\setEnv_post.bat
	set ANDROID_SETENV_DONE=1
)

if NOT "%ANDROID_CONFIG_DONE%"=="1" (
	call %SCRIPT_DIR%\defaults.bat
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
	set ANDROID_CONFIG_DONE=1
)


call cecho {white}{\n}
call cecho {white}{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {aqua}                 Cleaning {\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {white}{\n}


rem --- FULL CLEAN ---
if "%FULL%"=="full" (

	echo Removing _work
	if exist %WORK_FOLDER% rmdir /S /Q %WORK_FOLDER%

	echo Removing bin
	if exist %ROOT_DIR%\bin rmdir /S /Q %ROOT_DIR%\bin
	
	echo Removing android_studio_project
	if exist %ROOT_DIR%\android_studio_project rmdir /S /Q %ROOT_DIR%\android_studio_project
	
	echo Removing _project_eclipse
	if exist %ROOT_DIR%\_project_eclipse rmdir /S /Q %ROOT_DIR%\_project_eclipse
	
	echo Removing libs contents
	if exist %LIBS_FOLDER% rmdir /S /Q %LIBS_FOLDER%
	md %LIBS_FOLDER%

rem --- PARTIAL CLEAN ---
) else if "%FULL%"=="partial" (

	echo Removing %WORK_FOLDER%
	if exist %WORK_FOLDER% rmdir /S /Q %WORK_FOLDER%
	
	echo Removing bin
	if exist %ROOT_DIR%\bin rmdir /S /Q %ROOT_DIR%\bin
	
	echo Removing _project_eclipse
	if exist %ROOT_DIR%\_project_eclipse rmdir /S /Q %ROOT_DIR%\_project_eclipse
	
	echo Removing libs contents
	if exist %LIBS_FOLDER% rmdir /S /Q %LIBS_FOLDER%
	md %LIBS_FOLDER%

) else (
	echo.
	call cecho {red}    cleanup.bat - Incorrect parameter{\n}
	call cecho {white}{\n}
	echo.
	pause
)

if "%PAUSE%"=="pause_at_end" (
	call %NOTIFU% "Clean finished."
	echo.

	pause
)
