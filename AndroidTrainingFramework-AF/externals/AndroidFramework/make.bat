@echo off

rem **************************************************************************
rem Usage: make.bat [type_of_build] [arm_type] [NO_PAUSE]
rem [type_of_build] = release|debug|config
rem [arm_type] = arm|x86|all
rem **************************************************************************

TITLE Android Compile Process
echo.
echo.


set TYPE_OF_BUILD=%1
set ARCHITECTURE_TYPE=%2
set NO_PAUSE=%3


if "%TYPE_OF_BUILD%"=="" (
	echo Usage: make.bat  [type_of_build]  [arm_type]  [no_pause]
	echo        type_of_build = release, debug, config
	echo        arm_type = arm, x86, all
	echo        no_pause = no_pause, [nothing]
	
	goto errend
)
if "%ARCHITECTURE_TYPE%"=="" (
	echo Usage: make.bat  [type_of_build]  [arm_type]  [no_pause]
	echo        type_of_build = release, debug, config
	echo        arm_type = arm, x86, all
	echo        no_pause = no_pause, [nothing]
		
	goto errend
)


set GENERAL_CONFIG_DONE=0
set ANDROID_CONFIG_DONE=0
set ANDROID_SETENV_DONE=0
set HAS_ERRORS=


set ROOT_DIR=%~dp0
set SCRIPT_DIR=%ROOT_DIR%scripts



set CHECK_THE_TOOLS=true
call %ROOT_DIR%\tools\configureTools.bat 
if not "%HAS_ERRORS%"=="" (
	goto errend
)



call cecho {white}{\n}
call cecho {white}{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {aqua}                 Basic project settings{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {white}{\n}


set PROJECT=all


set ANDROID_FRAMEWORK_CONFIG=%~dp0..\AndroidFrameworkConfig


echo Type of Build is %TYPE_OF_BUILD%
echo Architecture type is %ARCHITECTURE_TYPE%
echo.
echo Root folder is:   %ROOT_DIR%
echo Config folder is: %ANDROID_FRAMEWORK_CONFIG%
echo.

if "%TYPE_OF_BUILD%"=="release" (
	echo Building release version.
	set RELEASE_VERSION=1
	
) else if "%TYPE_OF_BUILD%"=="debug" (
	set RELEASE_VERSION=0
	echo Building debug version.
	
) else if "%TYPE_OF_BUILD%"=="config" (
    echo Building Android CONFIG
	echo Generate only config file!
	
) else (
	echo %TYPE_OF_BUILD% No type of build was set. Defaulting to release build.
	set TYPE_OF_BUILD=release
	set RELEASE_VERSION=1
	
    set HAS_ERRORS=1
	goto :end
)


set BUILD_arm=false
set BUILD_x86=false


if "%ARCHITECTURE_TYPE%"=="all" (
	set BUILD_arm=true
	set BUILD_x86=true
	
	echo Building arm native SO.
	echo Building x86 native SO.
)

if "%ARCHITECTURE_TYPE%"=="arm" (
	set BUILD_arm=true
	echo Building arm native SO.
)

if "%ARCHITECTURE_TYPE%"=="x86" (
	set BUILD_x86=true
	echo Building x86 native SO.
)


if not exist "%ANDROID_FRAMEWORK_CONFIG%\configs\project\setEnv.bat" (
	call cecho {white}{\n}
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {red}	Cannot find: %ANDROID_FRAMEWORK_CONFIG%\configs\project\setEnv.bat{\n}    
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {white}{\n}
	
	set HAS_ERRORS=1
	goto :eof
) 

cd %ROOT_DIR%

call %SCRIPT_DIR%\setEnv_prev.bat
call %ANDROID_FRAMEWORK_CONFIG%\configs\project\setEnv.bat
call %SCRIPT_DIR%\setEnv_post.bat

if not "%TYPE_OF_BUILD%"=="config" ( 
	if exist "%CYGWIN_BIN%\bash.exe" (
		
		call cecho {white}{\n}
		call cecho {teal}Create Cygwin user folder...
		call cecho {white}{\n}
		
		"%CYGWIN_BIN%\bash.exe" --login -i %ANDROID_FRAMEWORK_DIR%\logoutbash.sh
	)
)

set ANDROID_SETENV_DONE=1



call cecho {white}{\n}
call cecho {white}{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {aqua}                 Configuration settings{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {white}{\n}


call %SCRIPT_DIR%\defaults.bat
call %ANDROID_FRAMEWORK_CONFIG%\config.bat

echo.
echo The folowing values depend of the current SHOP.					
echo OPERATOR = %OPERATOR%												
echo DOWNLOAD_SOURCE = %DOWNLOAD_SOURCE%									
echo GGI = %GGI%															
echo GL_DEMO_CODE = %GL_DEMO_CODE%										
echo GGC_GAME_CODE = %GGC_GAME_CODE%										
echo.


if not "%HAS_ERRORS%"=="" (
	call cecho {white}{\n}
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {red}	config.bat returned errors{\n}    
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {white}{\n}
	pause	
    goto :end
)
set ANDROID_CONFIG_DONE=1


call %SCRIPT_DIR%\checkEnv.bat
call %SCRIPT_DIR%\checkConfig.bat

if not "%HAS_ERRORS%"=="" (
	pause	
    goto :end
)


call cecho {white}{\n}
call cecho {white}{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {aqua}                 Writing config_Android.h{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {white}{\n}

set ANDROID_CONFIG_FILE_TEMP=%ANDROID_CONFIG_FILE%.temp

call %SCRIPT_DIR%\buildConfigH.bat

if "%TYPE_OF_BUILD%"=="config" ( 
    goto end
)

if not "%HAS_ERRORS%"=="" (
    goto errend
)



rem -------------------------------------------------------------------------------
rem			cleanup
rem -------------------------------------------------------------------------------

rem call %ANDROID_FRAMEWORK_DIR%\clean.bat %TYPE_OF_BUILD% %ARCHITECTURE_TYPE% dont_pause_at_end partial

rem -------------------------------------------------------------------------------
rem			native compile
rem -------------------------------------------------------------------------------



set SP=%cd%
set MAKE_START_TIME=%time%
	

set skipNativeCompiling=%SKIP_NATIVE_COMPILING%
if "%skipNativeCompiling%"=="" set skipNativeCompiling=0


if "%skipNativeCompiling%"=="0" (	
	call %SCRIPT_DIR%\make_native.bat %TYPE_OF_BUILD% %PROJECT% %CPU_CORES_USED%
		
) else (	
	call cecho {white}{\n}
	call cecho {white}{\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {aqua}                 Native build skipped {\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {white}{\n}
	

	if exist "%ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_arm%\lib%SO_LIB_FILE%.so" (
		echo Found arm SO library here: %ANDROID_FRAMEWORK_DIR%libs\%TYPE_ARMEABI_arm%\lib%SO_LIB_FILE%.so	
	) else (
		echo No native arm SO library found: %ANDROID_FRAMEWORK_DIR%libs\%TYPE_ARMEABI_arm%\lib%SO_LIB_FILE%.so
	)
	
	if exist "%ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_x86%\lib%SO_LIB_FILE%.so" (
		echo Found x86 SO library here: %ANDROID_FRAMEWORK_DIR%libs\%TYPE_ARMEABI_x86%\lib%SO_LIB_FILE%.so
	) else (
		echo No native x86 SO library found: %ANDROID_FRAMEWORK_DIR%libs\%TYPE_ARMEABI_x86%\lib%SO_LIB_FILE%.so
	)
	
	
	if not exist "%ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_arm%\lib%SO_LIB_FILE%.so" (	
		if not exist "%ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_x86%\lib%SO_LIB_FILE%.so" (
		
			call cecho {white}{\n}
			call cecho {red}_______________________________________________________________________________{\n}
			call cecho {white}{\n}
				call cecho {red}	No native SO lib found. Cannot build APK.{\n}
			call cecho {red}_______________________________________________________________________________{\n}
			call cecho {white}{\n}	
			call cecho {white}{\n}
			
			set HAS_ERRORS=1
			goto errend			
		)
	)
	
	echo.
	echo Using all SO libraries found.
)


if not "%HAS_ERRORS%"=="" (	
	call cecho {white}{\n}
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
		call cecho {red}	Native compile process returnd errors.{\n}
	call cecho {red}_______________________________________________________________________________{\n}
	call cecho {white}{\n}	
	call cecho {white}{\n}
	goto errend
)


if not "%PROJECT%"=="all" goto end
if "%CLEAN%"=="1" goto end
if "%SKIP_JAVA_COMPILING%"=="1" goto end



rem -------------------------------------------------------------------------------
rem 			java compile
rem -------------------------------------------------------------------------------


cd %ANDROID_FRAMEWORK_DIR%
call %SCRIPT_DIR%\make_java.bat 

if not "%HAS_ERRORS%"=="" (	
	goto errend
)
	
call cecho {white}{\n}


if "%TYPE_OF_BUILD%"=="debug" ( 
	set FINAL_APK=%AF_BIN_PATH%\%APP_NAME%_%VERSION%_DEBUG.apk
	copy %ANDROID_FRAMEWORK_DIR%\bin\%APP_NAME%_%VERSION%_DEBUG.apk      %AF_BIN_PATH%
	
	if "%INSTALL_APK%"=="1" (
		call cecho {white}{\n}
		call cecho {teal}Installing the APK...
		call cecho {white}{\n}
		call %ANDROID_FRAMEWORK_DIR%\install.bat debug
	)

	call cecho {white}{\n}
	call cecho {teal}Creating the Eclipse project...
	call cecho {white}{\n}
	call %SCRIPT_DIR%\create_eclipse_project.bat
	
	call cecho {white}{\n}
	call cecho {teal}Creating the WinGDB project...
	call cecho {white}{\n}		
	call %SCRIPT_DIR%\create_wingdb_project.bat
	
	call cecho {white}{\n}
	call cecho {teal}Creating the Nsight project...
	call cecho {white}{\n}		
	call %SCRIPT_DIR%\create_nsight_project.bat
	
) else (
	set FINAL_APK=%AF_BIN_PATH%\%APP_NAME%_%VERSION%.apk
	copy %ANDROID_FRAMEWORK_DIR%\bin\%APP_NAME%_%VERSION%.apk    %AF_BIN_PATH%
		
	if "%INSTALL_APK%"=="1" (
		call cecho {white}{\n}
		call cecho {teal}Installing the APK...
		call cecho {white}{\n}
		call %ANDROID_FRAMEWORK_DIR%\install.bat release
	)
)


if exist "%FINAL_APK%" (	
	if not "%FINAL_APK_DEST%"=="" (
		xcopy "%FINAL_APK%" "%FINAL_APK_DEST%"  /Y /F
	)	
) else (
	echo WARNING: "%FINAL_APK%"  does not exist!
)

	

:end

cd %SP%


if not "%HAS_ERRORS%"=="" (
    set HAS_ERRORS=1
    goto :errend
)


:errend

rem anything to do?

:end

echo.
echo.


if "%NO_PAUSE%"=="" (
pause
)

REM waitfor /si AndroidBuildDone

if "%HAS_ERRORS%"=="1" exit /b 1
