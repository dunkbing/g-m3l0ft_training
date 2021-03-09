@echo off


REM set debug mode
echo.
echo Java build: type of build is %TYPE_OF_BUILD%.
echo.

set SUFFIX=
if "%TYPE_OF_BUILD%"=="debug" (
	set SUFFIX=_DEBUG
)

if exist "%CD%\bin\%APP_NAME%_%VERSION%%SUFFIX%.apk" (
	del /Q %CD%\bin\%APP_NAME%_%VERSION%%SUFFIX%.apk>>NUL
)

if "%ANDROID_SDK_REVISION%"=="" (
	call %SCRIPT_DIR%\getAndroidRevisionSDK.bat
)

rem check params
set S=debug-signed
set I=

set CURRENT_PATH_SAVED=%CD%

rem -------------------------------------------------------------------------------
rem			preprocessing
rem -------------------------------------------------------------------------------


call cecho {white}{\n}
call cecho {white}{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {aqua}                 Preprocessing java and resources{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {white}{\n}


if "%USE_GRADLE_AND_J2A%"=="1" (
	call cecho {teal}Using j2a resource management{\n}
	call cecho {white}{\n}
	
	call %SCRIPT_DIR%\preprocess.bat
) else (
	call cecho {teal}Not using j2a resource management{\n}
	call cecho {white}{\n}
	
	call %SCRIPT_DIR%\preprocessOld.bat
)

if not "%HAS_ERRORS%"=="" (
	goto end
)

call cecho {white}{\n}
call cecho {white}Done preprocessing{\n}
call cecho {white}{\n}


rem -------------------------------------------------------------------------------
rem			building java
rem -------------------------------------------------------------------------------

cd  %CURRENT_PATH_SAVED%

call cecho {white}{\n}
call cecho {white}{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {aqua}                 Compiling java sources{\n}
call cecho {aqua}_______________________________________________________________________________{\n}
call cecho {white}{\n}
call cecho {white}{\n}


if "%USE_GRADLE_AND_J2A%"=="1" (
	call cecho {teal}Using GRADLE build system{\n}
) else (
	call cecho {teal}Using ANT build system{\n}
)
call cecho {white}{\n}


if "%USE_GRADLE_AND_J2A%"=="0" (

	call %SCRIPT_DIR%\updateBuildXML.bat
	rem build apk, use signed / unsinged debug
	call "%APACHE_ANT%\ant" %TYPE_OF_BUILD%


	REM if apk exist rename
	if exist "%CD%\bin\GLGame-%TYPE_OF_BUILD%.apk" (
		copy bin\GLGame-%TYPE_OF_BUILD%.apk bin\%APP_NAME%_%VERSION%%SUFFIX%.apk>>NUL
		
		rem delete temporary apks
		del /Q %CD%\bin\GLGame-%TYPE_OF_BUILD%.apk>>NUL
		del /Q %CD%\bin\GLGame-%TYPE_OF_BUILD%-unaligned.apk>>NUL
	)

) else (

	cd %ANDROID_FRAMEWORK_DIR%\android_studio_project
	
	if "%TYPE_OF_BUILD%"=="debug" (
		call gradlew.bat assembleDebug
	) else (
		call gradlew.bat assembleRelease
	)
	cd  %CURRENT_PATH_SAVED%
		
	if exist "%ANDROID_FRAMEWORK_DIR%\android_studio_project\app\build\outputs\apk\app-%TYPE_OF_BUILD%.apk" (
		copy "%ANDROID_FRAMEWORK_DIR%\android_studio_project\app\build\outputs\apk\app-%TYPE_OF_BUILD%.apk" bin\%APP_NAME%_%VERSION%%SUFFIX%.apk
	)
	
	if errorlevel 1 (
		call cecho {white}{\n}
		call cecho {red}_______________________________________________________________________________{\n}
		call cecho {white}{\n}
			call cecho {red}	Java compile process returned errors.{\n}
		call cecho {red}_______________________________________________________________________________{\n}
		call cecho {white}{\n}	
		call cecho {white}{\n}	
		set HAS_ERRORS=1
		goto end
	)
)


:end

cd %ROOT_DIR%

