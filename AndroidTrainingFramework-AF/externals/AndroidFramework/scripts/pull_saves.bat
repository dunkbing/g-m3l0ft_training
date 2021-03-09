@echo off
REM *****************************************************************
REM pull_saves.bat
REM 	Download save files from device to local folder
REM config:
REM		default path for pulled files is saves\default
REM		you can override this folder setting PULLED_SAVE_FILES_DIR 
REM		on your config.bat.
REM usage:
REM 	pull_saves.bat [-P]
REM 
REM 	-P request grant permission for save files&folder, REQUIRED FOR JELLY BEAN DEVICES.
REM 	after save your files you can rename default folder in order
REM 	to keep these files, IE. money, lvl40, etc.
REM *****************************************************************


rem fix running from drag&drop
cd %~dp0
if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)

if not exist "%ADB%" 		( goto :errorConfig )
if not exist "%PULLED_SAVE_FILES_DIR%" ( mkdir %PULLED_SAVE_FILES_DIR% )
if "%SAVE_FILE_LIST%"=="" 	( goto :errorsavelist )


pushd %PULLED_SAVE_FILES_DIR%
	echo =========================================================
	echo Downloading files.
	echo =========================================================
	if "%1" == "-P" (
		echo Granting permissions
		rem save folder, lets echo to screen in order to show warning if app is not debuggable
		%ADB% shell "run-as %APP_PACKAGE% chmod 777 %SAVE_FOLDER%"
		rem save files.
		for %%I in (%SAVE_FILE_LIST%) do (
			%ADB% shell "run-as %APP_PACKAGE% chmod 777 %SAVE_FOLDER%/%%I" >NUL
		)
		echo.
	)
	
	for %%I in (%SAVE_FILE_LIST%) do (	
		echo :: Downloading [%%I]
		%ADB% pull %SAVE_FOLDER%/%%I
		echo.
	)
popd

goto :end

:errorConfig
	echo =========================================================
	echo ERROR: ADB Tool not found [%ADB%]
	goto :end
	
:errorsavelist
	echo =========================================================
	echo ERROR: save list SAVE_FILE_LIST not defined [%SAVE_FILE_LIST%]
	goto :end
	
:end
echo.
call %NOTIFU% "pull save files finish"