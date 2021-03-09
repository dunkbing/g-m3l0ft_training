@echo off
REM *****************************************************************
REM push_saves.bat
REM 	Upload save files to device.
REM config:
REM		set values for SAVE_FILE_LIST and SAVE_FOLDER on your config.bat
REM usage:
REM 	push_saves.bat "saves_folder" [-P]
REM 	-P request grant permission for save files&folder, REQUIRED FOR JELLY BEAN DEVICES.
REM 
REM 	push_saves.bat saves\default
REM 	push_saves.bat saves\money -P
REM 	push_saves.bat saves\lvl40
REM 
REM *****************************************************************

rem fix running from drag&drop
cd %~dp0

if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)

if not exist "%ADB%" 		( goto :errorConfig )

set SRC_SAVES=%1
if "%SRC_SAVES%"==""		( goto :errordrag )
if not exist "%SRC_SAVES%"	( goto :error )
if "%SAVE_FILE_LIST%"==""	( goto :errorsavelist )

pushd %SRC_SAVES%
	echo =========================================================
	echo Uploading files
	echo to   [%SAVE_FOLDER%]
	echo from [%SRC_SAVES%]
	echo =========================================================
	if "%2" == "-P" (
		echo Granting permissions.
		rem save folder, lets echo to screen in order to show warning if app is not debuggable
		%ADB% shell "run-as %APP_PACKAGE% chmod 777 %SAVE_FOLDER%"
		rem save files
		for %%I in (%SAVE_FILE_LIST%) do (
			%ADB% shell "run-as %APP_PACKAGE% chmod 777 %SAVE_FOLDER%/%%I" >NUL
		)
		echo.
	)
	
	for %%I in (%SAVE_FILE_LIST%) do (
		if not exist "%%I" (
			echo :: WARNING: file not found for upload to device. file [%%I]
			echo.
		) else (
			echo :: Uploading file [%%I]
			%ADB% push %%I %SAVE_FOLDER%/%%I
			echo.
		)
	)
popd

REM if some error
if %ERRORLEVEL% NEQ 0 (
	goto :info
)

goto :end

:info
	echo =========================================================
	echo NOTE: notice that you can't upload files if you have a 
	echo fresh install (no saves files), you must first run your
	echo game for create new file saves, then upload your files.
	echo JELLY BEAN DEVICES REQUIRED TO USE -P IN ORDER TO GRANT
	echo PERMISSION TO FILES/FOLDER. By using -P your app must be
	echo debuggable.
	
	goto :end
	
:error
	echo =========================================================
	echo ERROR: folder not found [%SRC_SAVES%]
	goto :end
	
:errorsavelist
	echo =========================================================
	echo ERROR: save list SAVE_FILE_LIST not defined [%SAVE_FILE_LIST%]
	goto :end
	
:errordrag
	echo =========================================================
	echo ERROR: Drag folder that you want to upload to device, or 
	echo        send folder name as parameter.
	goto :end

:errorConfig
	echo =========================================================
	echo ERROR: ADB Tool not found [%ADB%]
	goto :end

:end
echo.
call %NOTIFU% "push save files finish"