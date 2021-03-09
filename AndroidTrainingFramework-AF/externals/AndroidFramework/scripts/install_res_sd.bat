@echo off

if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)

rem *************
rem 	USAGE
rem *************
rem
rem 	install_res_sd.bat [atc | dxt | etc | pvrt] [-oldpath | -external]
rem
rem		use -oldpath to copy the resources to SD_FOLDER -> /sdcard/gameloft/games/[GAME_NAME]
rem		use -external to copy the resources to /sdcard/external_sd/Android/data/%APP_PACKAGE%/files
rem		use non of this two to copy the resources to /sdcard/Android/data/%APP_PACKAGE%/files
rem 	Some of the devices have both internal and external data paths and the installer could choose 
rem 	the external sd path to save the game's data. In this case you shoudl use -external

if "%1"=="" (
	echo ###
	echo ### Usage:
	echo ### "install_res_sd.bat [atc | dxt | etc | pvrt] [-oldpath | -external]"
	echo ### 
	echo ### use -oldpath to copy the resources to SD_FOLDER: %SD_FOLDER%
	echo ### use -external to copy the resources to external sd path: /sdcard/external_sd/Android/data/%APP_PACKAGE%/files
	echo ### use non of this two to copy the resources to default system path: /sdcard/Android/data/%APP_PACKAGE%/files
	goto:END
)

rem check params
set INSTALL_WHERE=normal
set INSTALL_SD_RESOURCES=

for %%p in (%*) do (
	if "%%p"=="-oldpath" 		( set INSTALL_WHERE=oldpath
	) else if "%%p"=="external" ( set INSTALL_WHERE=external
	) else if "%%p"=="pvrt" ( set INSTALL_SD_RESOURCES=%SD_RESOURCES_PVRT%
	) else if "%%p"=="atc" 	( set INSTALL_SD_RESOURCES=%SD_RESOURCES_ATC%
	) else if "%%p"=="dxt" 	( set INSTALL_SD_RESOURCES=%SD_RESOURCES_DXT%
	) else if "%%p"=="etc" 	( set INSTALL_SD_RESOURCES=%SD_RESOURCES_ETC%
	) else 					( echo Unknown param "%%p" )
)

if not exist "%INSTALL_SD_RESOURCES%" (
	echo folder not found: "%INSTALL_SD_RESOURCES%".
	goto END
)


rem check for resources folder
rem request use old path (SD_FOLDER var)
set SD_DST_FOLDER=/sdcard/Android/data/%APP_PACKAGE%/files
if "%INSTALL_WHERE%" == "oldpath" (
	set SD_DST_FOLDER=%SD_FOLDER%
) else if "%INSTALL_WHERE%" == "external" (
	set SD_DST_FOLDER=/sdcard/external_sd/Android/data/%APP_PACKAGE%/files
)

echo :::: Installing resources
echo 	:: from %INSTALL_SD_RESOURCES%
echo 	:: to %SD_DST_FOLDER%

call:copyFolder %INSTALL_SD_RESOURCES%

call:copyFolder %SD_RESOURCES_COMMON%

REM Custom resources
if not "%SD_RESOURCES_CUSTOM%"=="" (
	call:copyFolder %SD_RESOURCES_CUSTOM%
)

rem Video
if "%USE_VIDEO_FROM_SD%"=="1" (
	echo -----------------------------------------------------------
	pushd %SD_VIDEO_DIR%
		echo Copying video %VIDEO_NAME%
		call %ADB% push %VIDEO_NAME%.mp4 %SD_DST_FOLDER%/intro.mp4
		if errorlevel 1 goto ONERROR
	popd
)

rem all ok
goto END


:copyFolder
	pushd %1
		rem For each folder not hidden
		for /F %%a in ('Dir /B /AD-H %CD%') do (
			call:copyFolder %%a %2/%%~a
		)
		
		REM echo ===========================================================
		REM echo Copying folder: %2
		REM echo ===========================================================
		for %%i in (*.*) do (
			echo -----------------------------------------------------------
			echo :: %%i
			call %ADB% push %%i %SD_DST_FOLDER%%2/%%i
			if errorlevel 1 goto ONERROR
		)
	popd
goto:eof


:ONERROR
popd
pause

:END
call %NOTIFU% "install res to sd finish"
rem pause