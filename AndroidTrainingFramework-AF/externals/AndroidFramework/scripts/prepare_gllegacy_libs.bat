:: This is a HACK because the AndroidCorePackage does not work propperly
    
@echo Executing: %~f0


call:copyToWorkFolder GLLEGACYCONFIG_FOLDER %GLLEGACYCONFIG_FOLDER%\_Android -r 



REM ------------- Social -------------------
if "%USE_SOCIALLIB%"=="1" (
	setlocal enabledelayedexpansion
	rem check if your game set GLSOCIALLIB_FOLDER and GLSOCIALLIB_CONFIG_FOLDER
	rem if not => set to GLSocialLib and GLSocialLib_config paths
	if "%GLSOCIALLIB_FOLDER%"=="" (
		set GLSOCIALLIB_FOLDER=%EXTERNALS_FOLDER%\GLSocialLib
	)
	if not exist "!GLSOCIALLIB_FOLDER!" (
		call:SHOW_ERROR_MESSAGE 200 "!GLSOCIALLIB_FOLDER! does not exist!" "Fix this error by editing _Android\config.bat. You can use BuildConfigurator."
	)

	if "%GLSOCIALLIB_CONFIG_FOLDER%"=="" (
		set GLSOCIALLIB_CONFIG_FOLDER=%EXTERNALS_FOLDER%\GLSocialLib_config
	)
	if not exist "!GLSOCIALLIB_CONFIG_FOLDER!" (
		call:SHOW_ERROR_MESSAGE 200 "!GLSOCIALLIB_CONFIG_FOLDER! does not exist!" "Fix this error by editing _Android\config.bat. You can use BuildConfigurator."
	)
	
	if "%ADD_EXTERNAL_LIB_PROPERTIES%"=="" (
		set ADD_EXTERNAL_LIB_PROPERTIES=%~dp0add_external_lib_properties.bat
	)
	if not exist "!ADD_EXTERNAL_LIB_PROPERTIES!" (
		call:SHOW_ERROR_MESSAGE 200 "!ADD_EXTERNAL_LIB_PROPERTIES! does not exist!" "Fix this error by editing _Android\config.bat. You can use BuildConfigurator."
	)
	
	if "%ANDROID_WIN_GDB_FOLDER%"=="" (
		set ANDROID_WIN_GDB_FOLDER=!GLLEGACYCONFIG_FOLDER!\_Android\_project_WinGDB
	)
	if not exist "!ANDROID_WIN_GDB_FOLDER!" (
		call:SHOW_ERROR_MESSAGE 200 "!ANDROID_WIN_GDB_FOLDER! does not exist!" "Fix this error by editing _Android\config.bat. You can use BuildConfigurator."
	)
	
	REM call the GLSocialLib bat
	if exist "!GLSOCIALLIB_FOLDER!\SOCIALLIB_COPY_TO_PROJECT.bat" (
		rem let GLSocialLib copy it's resources to project
		cd !GLSOCIALLIB_FOLDER!
		call !GLSOCIALLIB_FOLDER!\SOCIALLIB_COPY_TO_PROJECT.bat
		rem back to the folder where it's this bat (prepare_gllegacy_libs.bat)
		cd %ROOT_DIR%
	)
	if not exist "!GLSOCIALLIB_FOLDER!\SOCIALLIB_COPY_TO_PROJECT.bat" (
		echo WARNING !GLSOCIALLIB_FOLDER!\SOCIALLIB_COPY_TO_PROJECT.bat DOES NOT EXIST!
	)
	endLocal
)

rem deprecated because the java files from GLF was moved to _AndroidToolBox\java
rem if "%USE_GLLEGACY%"=="1" (
rem 	if "%USE_GLF_GL2JNIACTIVITY%"=="1" (
rem 		echo ::: copy glf libs
rem 		call:copyToWorkFolder GLF_ANDROID_FOLDER %GLF_ANDROID_FOLDER% -r
rem 	)
rem )

REM ---------- GLF SRC FOLDER ----------------
rem if "%USE_GLLEGACY%"=="1" (
rem 	if "%USE_GLF_GL2JNIACTIVITY%"=="1" (
rem         call:copyJAVAToWorkFolder GLF_APK_SRC_FOLDER %GLF_APK_SRC_FOLDER%
rem 	)
rem )

REM ---------- GLLEGACY SRC FOLDER ----------------
if "%USE_GLLEGACY%"=="1" (
        call:copyJAVAToWorkFolder GLLEGACY_APK_SRC_FOLDER %GLLEGACY_APK_SRC_FOLDER%
)

REM ---------- GLOT_TRACKING SRC FOLDER ----------------
if "%USE_GLOT_TRACKING%"=="1" (
		call:copyJAVAToWorkFolder GLOT_APK_SRC_FOLDER %GLOT_APK_SRC_FOLDER%
)


REM ---------- GAIA SRC FOLDER ----------------
if "%USE_GAIA%"=="1" (
		call:copyJAVAToWorkFolder GAIA_APK_SRC_FOLDER %GAIA_APK_SRC_FOLDER%
		
)


REM ---------- GAMEOPTIONS2 SRC FOLDER ----------------
if "%USE_GAMEOPTIONS2%"=="1" (
		call:copyJAVAToWorkFolder GAMEOPTIONS2_APK_SRC_FOLDER %GAMEOPTIONS2_APK_SRC_FOLDER%
		
)

goto:GLLegacy_eof

:copyToWorkFolder
	rem Usage:
    rem %1 MESSAGE
	rem %2 Which folder
	rem %3 which folders (possible values: all, libs, res)
	echo :: copying %2 resources to %LIBS_FOLDER%...

    if not exist "%2" (
        call:SHOW_ERROR_MESSAGE 100 "%1 = %2 does not exists!" "Fix this error by editing _Android\config.bat. You can use BuildConfigurator."
    )
	
	if exist "%2\res\*.*" (
		xcopy %2\res\*.* %TMP_RES_FOLDER% /E /Y>>NUL
	) else (
        @echo WARNING: "%2\res\*.*" does not exists!
    )

	if exist "%2\assets\*.*" (
		xcopy %2\assets\*.* %TMP_ASSETS_FOLDER% /E /Y>>NUL
	) else (
        @echo WARNING: "%2\assets\*.*" does not exists!
    )

	if exist "%2\lib\*.*" (
		xcopy %2\lib\*.* %LIBS_FOLDER% /E /Y>>NUL
	) else (
        @echo WARNING: "%2\lib\*.*" does not exists!
    )

	if exist "%2\libs\*.*" (
		xcopy %2\libs\*.* %LIBS_FOLDER% /E /Y>>NUL
	) else (
        @echo WARNING: "%2\libs\*.*" does not exists!
    )

	echo .
goto:GLLegacy_eof


:copyJAVAToWorkFolder
    rem Usage:
    rem %1 MESSAGE
    rem %2 Which folder
    echo ::: preprocessing %1
    if not exist "%2" (
        call:SHOW_ERROR_MESSAGE 100 "%1 = %2 does not exists!" "Fix this error by editing _Android\config.bat. You can use BuildConfigurator."
    )

    pushd %2
    setlocal enabledelayedexpansion
    For /f %%L in ('dir *java /s /b /A:-H') do (
        set sourceFile=%%L
        set onlyName=%%~nL
        set newName=!onlyName!.jpp
        copy !sourceFile! %TMP_SRC_FOLDER%\!newName!>>NUL
    )
    setlocal disabledelayedexpansion

    copy *.h %TMP_SRC_FOLDER%\>>NUL

    popd
    echo .
goto:GLLegacy_eof


:SHOW_ERROR_MESSAGE  <errorID> <Msg1> [Msg2]
rem *********************************************************
rem * Subrutine SHOW_ERROR_MESSAGE **************************
rem * Params %1 ERROR ID ************************************
rem * Params %2 Error Message *******************************
rem * Params %3 Obtional Error Message **********************
rem * The messages must be in quotas! Ex: *******************
rem call::SHOW_ERROR_MESSAGE 100 "messahe 1" "message 2" ****
rem *********************************************************
call cecho {red}ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR{#}{\n}
call cecho {red}ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR{#}{\n}
call cecho {red}In file: %~f0 {#}{\n}
call cecho {red}ERROR ID: %1 {#}{\n}
call cecho {red}ERROR MESSAGE: %2{#}{\n}
if not %3=="" ( 
call cecho {red}ERROR MESSAGE: %3{#}{\n}
)
rem reset the errorlevel by calling ver
ver > NUL
call cecho {red}ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR{#}{\n}
call cecho {red}ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR{#}{\n}

:GLLegacy_eof