@echo off
set CYGWIN=nodosfilewarning

if exist "%ANDROID_FRAMEWORK_CONFIG%config.bat" (
	call %ANDROID_FRAMEWORK_CONFIG%config.bat
)

set ARMABIADRR=%ANDROID_NDK_HOME%\toolchains\arm-linux-androideabi-4.6\prebuilt\windows\bin\arm-linux-androideabi-addr2line.exe
set ARMABICPPFILT=%ANDROID_NDK_HOME%\toolchains\arm-linux-androideabi-4.6\prebuilt\windows\bin\arm-linux-androideabi-c++filt.exe
set SO_LIBRARY=..\..\libs\%TYPE_ARMEABI%\lib%SO_LIB_FILE%.so

if "%OBJS_PATH%"=="" (
	set OBJS_PATH=%ANDROID_FRAMEWORK_CONFIG%\obj\local\%TYPE_ARMEABI%
)
set LOG_TXT=log.txt

