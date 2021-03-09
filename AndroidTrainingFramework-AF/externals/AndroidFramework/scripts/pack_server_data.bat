@echo off

set ROOT_DIR=%~dp0..
set script_dir=%~dp0


call %script_dir%\..\tools\configureTools.bat

call %script_dir%\setEnv_prev.bat
call %script_dir%\..\..\AndroidFrameworkConfig\configs\project\setEnv.bat
call %script_dir%\setEnv_post.bat

call %script_dir%\defaults.bat
call %script_dir%\..\..\AndroidFrameworkConfig\config.bat



if "%USE_LZMA%"=="1" (
	set LZMA_PARAM=LZMA
) else (
	set LZMA_PARAM=zip
)


if not exist "%WORK_FOLDER%" (
	md %WORK_FOLDER%
)

if not exist "%RESOURCES_BIN%" (
	md %RESOURCES_BIN%
)

if "%1"=="" (
	call:packATC
	call:packPVRT
	call:packETC
	call:packDXT
	goto:eof
)
echo %1
if "%1"=="ATC" (
	call:packATC
	goto:eof
)
if "%1"=="PVRT" (
	call:packPVRT
	goto:eof
)
if "%1"=="ETC" (
	call:packETC
	goto:eof
)
if "%1"=="DXT" (
	call:packDXT
	goto:eof
)
echo ** Invalid argument **
echo "call pack_server_data [ATC|PVRT|ETC|DXT]"
goto:eof


	
:packATC
rem ------------------- ATC -----------------------
set RES_FOLDER=%SD_RESOURCES_ATC%
set PREFIX=atc
set OUTFILE=pack%PREFIX%
set DST_FOLDER=%WORK_FOLDER%\%OUTFILE%
if not exist "%DST_FOLDER%" (
	md %DST_FOLDER%
)
if "%USE_HEP_PACKINFO%"=="1" (
	if exist "%RES_FOLDER%\%OUTFILE%.info" (
		del /S /Q %RES_FOLDER%\%OUTFILE%.info
	)
	if exist "%RES_FOLDER%\.nomedia" (
		del /S /Q .nomedia	
	)
)
echo.
echo Building %PREFIX%
xcopy %SD_RESOURCES_COMMON%\*.* %DST_FOLDER% /Y /E
if exist "%SD_RESOURCES_CUSTOM%" (
	xcopy %SD_RESOURCES_CUSTOM%\*.* %DST_FOLDER% /Y /E
)
xcopy %RES_FOLDER%\*.* %DST_FOLDER% /Y /E

cd ..
	
	%RESPACK% %LZMA_PARAM% %SPLIT_SIZE% %DST_FOLDER% %OUTFILE%.czip %OUTFILE%.info %WORK_FOLDER%/packTmp 
	if "%USE_HEP_PACKINFO%"=="1" (	
		copy %OUTFILE%.info %APK_RESOURCES%\installer\res\raw\pack.info
	) else (
	move %OUTFILE%.info %RESOURCES_BIN%>>NUL
	)
	move %OUTFILE%.czip %ROOT_DIR%\%DOWNLOAD_FILE_NAME_ATC%>>NUL

cd scripts

if exist "%DST_FOLDER%" rd "%DST_FOLDER%" /s/q
if exist "%WORK_FOLDER%/packTmp" rd "%WORK_FOLDER%/packTmp" /s/q
goto:eof



:packPVRT
rem ------------------- PVRT -----------------------
set RES_FOLDER=%SD_RESOURCES_PVRT%
set PREFIX=pvrt
set OUTFILE=pack%PREFIX%
set DST_FOLDER=%WORK_FOLDER%\%OUTFILE%
if not exist "%DST_FOLDER%" (
	md %DST_FOLDER%
)
if "%USE_HEP_PACKINFO%"=="1" (
	if exist "%RES_FOLDER%\%OUTFILE%.info" (
		del /S /Q %RES_FOLDER%\%OUTFILE%.info
	)
	if exist "%RES_FOLDER%\.nomedia" (
		del /S /Q .nomedia	
	)
)
echo.
echo Building %PREFIX%
xcopy %SD_RESOURCES_COMMON%\*.* %DST_FOLDER% /Y /E
if exist "%SD_RESOURCES_CUSTOM%" (
	xcopy %SD_RESOURCES_CUSTOM%\*.* %DST_FOLDER% /Y /E
)
xcopy %RES_FOLDER%\*.* %DST_FOLDER% /Y /E

cd ..

	%RESPACK% %LZMA_PARAM% %SPLIT_SIZE% %DST_FOLDER% %OUTFILE%.czip %OUTFILE%.info %WORK_FOLDER%/packTmp
	if "%USE_HEP_PACKINFO%"=="1" (	
		copy %OUTFILE%.info %APK_RESOURCES%\installer\res\raw\pack.info
	) else (
	move %OUTFILE%.info %RESOURCES_BIN%>>NUL
	)
	move %OUTFILE%.czip %ROOT_DIR%\%DOWNLOAD_FILE_NAME_PVR%>>NUL

cd scripts

if exist "%DST_FOLDER%" rd "%DST_FOLDER%" /s/q
if exist "%WORK_FOLDER%/packTmp" rd "%WORK_FOLDER%/packTmp" /s/q
goto:eof



:packETC
rem ------------------- ETC ------------------------
set RES_FOLDER=%SD_RESOURCES_ETC%
set PREFIX=etc
set OUTFILE=pack%PREFIX%
set DST_FOLDER=%WORK_FOLDER%\%OUTFILE%
if not exist "%DST_FOLDER%" (
	md %DST_FOLDER%
)
if "%USE_HEP_PACKINFO%"=="1" (
	if exist "%RES_FOLDER%\%OUTFILE%.info" (
		del /S /Q %RES_FOLDER%\%OUTFILE%.info
	)
	if exist "%RES_FOLDER%\.nomedia" (
		del /S /Q .nomedia	
	)
)
xcopy %SD_RESOURCES_COMMON%\*.* %DST_FOLDER% /Y /E
if exist "%SD_RESOURCES_CUSTOM%" (
	xcopy %SD_RESOURCES_CUSTOM%\*.* %DST_FOLDER% /Y /E
)
xcopy %RES_FOLDER%\*.* %DST_FOLDER% /Y /E

cd ..

	%RESPACK% %LZMA_PARAM% %SPLIT_SIZE% %DST_FOLDER% %OUTFILE%.czip %OUTFILE%.info %WORK_FOLDER%/packTmp 

	if "%USE_HEP_PACKINFO%"=="1" (	
		copy %OUTFILE%.info %APK_RESOURCES%\installer\res\raw\pack.info
	) else (
		move %OUTFILE%.info %RESOURCES_BIN%>>NUL
	)
	move %OUTFILE%.czip %ROOT_DIR%\%DOWNLOAD_FILE_NAME_ETC%>>NUL

cd scripts

if exist "%DST_FOLDER%" rd "%DST_FOLDER%" /s/q
if exist "%WORK_FOLDER%/packTmp" rd "%WORK_FOLDER%/packTmp" /s/q
goto:eof



:packDXT
rem ------------------- DXT -----------------------
set RES_FOLDER=%SD_RESOURCES_DXT%
set PREFIX=dxt
set OUTFILE=pack%PREFIX%
set DST_FOLDER=%WORK_FOLDER%\%OUTFILE%
if not exist "%DST_FOLDER%" (
	md %DST_FOLDER%
)
if "%USE_HEP_PACKINFO%"=="1" (
	if exist "%RES_FOLDER%\%OUTFILE%.info" (
		del /S /Q %RES_FOLDER%\%OUTFILE%.info
	)
	if exist "%RES_FOLDER%\.nomedia" (
		del /S /Q .nomedia	
	)
)
echo.
echo Building %PREFIX%
xcopy %SD_RESOURCES_COMMON%\*.* %DST_FOLDER% /Y /E
if exist "%SD_RESOURCES_CUSTOM%" (
	xcopy %SD_RESOURCES_CUSTOM%\*.* %DST_FOLDER% /Y /E
)
xcopy %RES_FOLDER%\*.* %DST_FOLDER% /Y /E

cd ..

	%RESPACK% %LZMA_PARAM% %SPLIT_SIZE% %DST_FOLDER% %OUTFILE%.czip %OUTFILE%.info %WORK_FOLDER%/packTmp
	if "%USE_HEP_PACKINFO%"=="1" (	
		copy %OUTFILE%.info %APK_RESOURCES%\installer\res\raw\pack.info
	) else (
		move %OUTFILE%.info %RESOURCES_BIN%>>NUL
	)
	move %OUTFILE%.czip %ROOT_DIR%\%DOWNLOAD_FILE_NAME_DXT%>>NUL
	
cd scripts

if exist "%DST_FOLDER%" rd "%DST_FOLDER%" /s/q
if exist "%WORK_FOLDER%/packTmp" rd "%WORK_FOLDER%/packTmp" /s/q
goto:eof

