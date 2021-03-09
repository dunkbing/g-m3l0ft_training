@echo off
rem **************************************************************************
rem USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE
rem **************************************************************************
rem Usage: make.bat [TYPE_OF_BUILD] [project]
rem [TYPE_OF_BUILD] = release|debug|config
rem [project] 		= use 'all' not used, but must be present for compatibility
rem **************************************************************************
rem USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE USAGE
rem **************************************************************************
if not "%ANDROID_SETENV_DONE%"=="1" (
	echo "ERROR: you have to call setEnv.bat from your GameSpeciffic folder first."
	goto :eof
)

set TYPE_OF_BUILD=%1
set PROJECT=%2
set JOBS=%3


if "%PROJECT%"=="" set PROJECT=all
if "%JOBS%"=="" set JOBS=1
	
	
set CLEAN_FLAG=-c
del /Q %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_arm%\*
del /Q %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_x86%\*


set VERBOSE_FLAG=
if "%VERBOSE%"=="1" set VERBOSE_FLAG=-v


if "%BUILD_arm%"=="true" (		
	if not exist %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_arm%\ (
		mkdir %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_arm%\
	)
)
if "%BUILD_x86%"=="true" (		
	if not exist %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_x86%\ (
		mkdir %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_x86%\
	)
)

if not exist %ANDROID_FRAMEWORK_DIR%\bin\ (
	mkdir %ANDROID_FRAMEWORK_DIR%\bin\
)

if "%FINAL_APK_DEST%"=="" (
	set FINAL_APK_DEST=%ANDROID_FRAMEWORK_DIR%\bin\
)

if not exist "%FINAL_APK_DEST%" (
	mkdir %FINAL_APK_DEST%
)

if not exist "%AF_BIN_PATH%" (
	mkdir %AF_BIN_PATH%
)



if "%BUILD_arm%"=="true" (   
		
	call cecho {white}{\n}
	call cecho {white}{\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {aqua}                 Compiling the ARM SO {\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {white}{\n}
		
			
	set CURRENT_ARCH=
 
	echo call %SLN2GCC%  -i %MAKEFILE_XML_arm%  -t %TYPE_OF_BUILD%  -p %PROJECT%  -g %NATIVE_COMPILER_arm%  -j %JOBS%  %VERBOSE_FLAG%   
	echo.
	
	call %SLN2GCC%  -i %MAKEFILE_XML_arm%  -t %TYPE_OF_BUILD%  -p %PROJECT%  -g %NATIVE_COMPILER_arm%  -j %JOBS%  %VERBOSE_FLAG%
	
	if errorlevel 1 (
		set HAS_ERRORS=1
		goto end
	)
	
	call cecho {white}{\n}
	
	
	if exist %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so (	
		if not exist "%AF_BIN_PATH%\armeabi-v7a" (		
			mkdir %AF_BIN_PATH%\armeabi-v7a
		)
		
		if "%BUILD_arm%"=="true" (	
			xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so  %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_arm%\ /Y /F		
			
			if exist %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so_with_dsym ( 
				xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so_with_dsym  %ANDROID_FRAMEWORK_DIR%\bin\   /Y /F
			)
			
			if exist %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.dsym ( 
				
				xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.dsym  %ANDROID_FRAMEWORK_DIR%\bin\ /Y /F			
				xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.dsym  %AF_BIN_PATH%\armeabi-v7a /Y /F
				
				if exist "%ANDROID_FRAMEWORK_DIR%\bin\lib%SO_LIB_FILE%.dsym" (				
					if exist "%ANDROID_FRAMEWORK_DIR%\bin\%SYMBOLS_FILE%" (
						del /F /Q %ANDROID_FRAMEWORK_DIR%\bin\%SYMBOLS_FILE%
					)
				)			
			)
		)
	) else (
		echo.
		echo SO library for armeabi-v7a not found here:
		echo %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_arm%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so
		echo Stopping build.
		echo.
		set HAS_ERRORS=1
		goto end
	)
)	






if "%BUILD_x86%"=="true" (	
		
	call cecho {white}{\n}
	call cecho {white}{\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {aqua}                 Compiling the x86 SO {\n}
	call cecho {aqua}_______________________________________________________________________________{\n}
	call cecho {white}{\n}
	call cecho {white}{\n}
		
		
	set CURRENT_ARCH=-x86
	
	echo call %SLN2GCC%  -i %MAKEFILE_XML_x86%  -t %TYPE_OF_BUILD%  -p %PROJECT%  -g %NATIVE_COMPILER_x86%  -j %JOBS%  %VERBOSE_FLAG% 
	echo.
	
	call %SLN2GCC%  -i %MAKEFILE_XML_x86%  -t %TYPE_OF_BUILD%  -p %PROJECT%  -g %NATIVE_COMPILER_x86%  -j %JOBS%  %VERBOSE_FLAG%
	if errorlevel 1 (
		set HAS_ERRORS=1
		goto end
	) 		
	call cecho {white}{\n}
		
	
	
	if exist %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so (
		if not exist "%AF_BIN_PATH%\x86" (		
			mkdir %AF_BIN_PATH%\x86
		)

		if "%BUILD_x86%"=="true" (	
			xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so  %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_x86%\  /Y /F

			if exist %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so_with_dsym ( 
				xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so_with_dsym  %ANDROID_FRAMEWORK_DIR%\bin\   /Y /F
				
			)

			if exist %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.dsym ( 
				
				xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.dsym  %ANDROID_FRAMEWORK_DIR%\bin\ /Y /F		
				xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.dsym  %AF_BIN_PATH%\x86 /Y /F
				
				if exist "%ANDROID_FRAMEWORK_DIR%\bin\lib%SO_LIB_FILE%.dsym" (				
					if exist "%ANDROID_FRAMEWORK_DIR%\bin\%SYMBOLS_FILE%" (
						del /F /Q %ANDROID_FRAMEWORK_DIR%\bin\%SYMBOLS_FILE%
					)
					
					ren %ANDROID_FRAMEWORK_DIR%\bin\lib%SO_LIB_FILE%.dsym %SYMBOLS_FILE%
				)
				
				xcopy %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.dsym  %FINAL_APK_DEST%\ /Y /F
				if exist "%FINAL_APK_DEST%\lib%SO_LIB_FILE%.dsym" (
					if exist "%FINAL_APK_DEST%\%SYMBOLS_FILE%" (
						del /F /Q %FINAL_APK_DEST%\%SYMBOLS_FILE%
					)
					ren %FINAL_APK_DEST%\lib%SO_LIB_FILE%.dsym %SYMBOLS_FILE%
				)
			)
		)	
	) else (
		echo.
		echo SO library for x86 not found here:
		echo %AF_INTERMEDIATE_PATH%\%TYPE_OF_BUILD%\%NATIVE_COMPILER_x86%\%MAIN_PROJECT%\lib%SO_LIB_FILE%.so
		echo Stopping build.
		echo.
		set HAS_ERRORS=1
		goto end
	)
)
		


rem what to do with clean?
REM if "%CLEAN%"=="1" goto end



if "%TYPE_OF_BUILD%"=="debug" (
	rem copy %ANDROID_NDK_HOME%\toolchains\arm-linux-androideabi-4.4.3\prebuilt\gdbserver  %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI%\
	if "%BUILD_arm%"=="true" (
		xcopy %ANDROID_NDK_HOME%\prebuilt\android-arm\gdbserver\gdbserver  %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_arm%\  /Y /F
	) else (
		xcopy %ANDROID_NDK_HOME%\prebuilt\android-arm\gdbserver\gdbserver  %ANDROID_FRAMEWORK_DIR%\libs\%TYPE_ARMEABI_x86%\  /Y /F
	)
) 
	


:end

cd %ROOT_DIR%
echo.

