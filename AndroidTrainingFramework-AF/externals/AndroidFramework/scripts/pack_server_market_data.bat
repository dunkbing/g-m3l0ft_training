@echo off

if exist "%ANDROID_FRAMEWORK_CONFIG%\config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%\config.bat
)


if not exist "%WORK_FOLDER%" (
	md %WORK_FOLDER%
)

if not exist "%RESOURCES_BIN%" (
	md %RESOURCES_BIN%
)
set RESPACK=%TOOLS_DIR%\resPack\AMarketResPack.exe

rem echo ** Invalid argument **
rem echo "call pack_server_data [ATC|PVRT|ETC|DXT]"

if "%1" == "" (
	FOR /D %%G in ("%SD_RESOURCES%\*") DO ( 
		set CUSTOM_DATA_FOLDER=%%~nG
		call:packFolder
	)
) else (
	set CUSTOM_DATA_FOLDER=%1
	call:packFolder
)
goto:eof

:packFolder
rem ------------------- ATC -----------------------
set RES_FOLDER=%SD_RESOURCES%\%CUSTOM_DATA_FOLDER%
set RES_FOLDER_PATCH=%SD_RESOURCES_PATCH%\%CUSTOM_DATA_FOLDER%
set OUTPUT_FILE=%GOOGLE_DOWNLOAD_FILE_NAME%_%CUSTOM_DATA_FOLDER%.%DOWNLOAD_FILE_EXTENSION%
echo resfolder_set %RES_FOLDER%
set PREFIX=%CUSTOM_DATA_FOLDER%
set OUTFILE=pack%PREFIX%
set DST_FOLDER=%WORK_FOLDER%\%OUTFILE%
if not exist "%DST_FOLDER%" (
	md %DST_FOLDER%
)


echo resfolder %RES_FOLDER%
echo zip %GOOGLE_DOWNLOAD_FILE_NAME%
echo patch %RES_FOLDER_PATCH%

%RESPACK% %RES_FOLDER% %OUTFILE%.czip %OUTFILE%_patch.czip %WORK_FOLDER%/packTmp %RES_FOLDER_PATCH% %COMPRESS_METHOD% %COMPILE_MAIN_FOLDER% %COMPILE_PATCH%

echo mooving %ROOT_DIR%\main.%OUTPUT_FILE%
move %OUTFILE%.czip %ROOT_DIR%\main.%OUTPUT_FILE%>>NUL
move %OUTFILE%_patch.czip %ROOT_DIR%\patch.%OUTPUT_FILE%>>NUL

if exist "%DST_FOLDER%" rd "%DST_FOLDER%" /s/q
if exist "%WORK_FOLDER%/packTmp" rd "%WORK_FOLDER%/packTmp" /s/q
goto:eof
