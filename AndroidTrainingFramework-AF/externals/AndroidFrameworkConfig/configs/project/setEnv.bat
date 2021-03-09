@echo off


rem @note The build will be installed on the device at the end.
rem @check
if not defined INSTALL_APK (
set INSTALL_APK=1
)


rem @note Enable in order to skip native code building.
rem @note The build process will, however, still attempt to copy the SO.
rem @check
set SKIP_NATIVE_COMPILING=0


rem @note Enable in order to skip java compiling and APK building.
rem @check
set SKIP_JAVA_COMPILING=0


rem @note How many files will be compiled simultaneously.
set CPU_CORES_USED=2


rem @note The file used to compile the native code for arm architecture. 
rem @note The file should exist in AndroidFrameworkConfig\configs\project
set SLN2GCC_USED_arm=sln2gcc.xml

rem @note The file used to compile the native code for x86 architecture.
rem @note The file should exist in AndroidFrameworkConfig\configs\project
set SLN2GCC_USED_x86=sln2gcc_x86.xml


rem @note Configuration set for compiling the native code for arm.
rem @note Matched to tag <GccConfig Name="***"> in SLN2GCC_USED_arm.
set NATIVE_COMPILER_arm=armeabi-v7a


rem @note Configuration set for compiling the native code for x86.
rem @note Matched to tag <GccConfig Name="***"> in SLN2GCC_USED_x86.
set NATIVE_COMPILER_x86=x86


rem @note Main project that will result in the SO.
rem @note The project needs to exist in both the sln and the sln2gcc file.
set MAIN_PROJECT=Android


rem @note The name of the final SO library.
rem @note Correlated with s2g tag <Macro Name="USE_SPECIFIC_OUTPUT_NAME" Value="$(SO_LIB_FILE)"/>.
set SO_LIB_FILE=controller


rem @note Enable or disable distCC compiling.
rem _@check
set USE_DISTCC=1


rem @note Substitue NDK path with the following drive:
rem @combo P:|Q:|R:|S:|T:|U:
rem _@check
set SUBST_LETTER_DRIVE=


rem @note Set the HOST computers for distCC. 
rem @note Use the name or IP.
set USE_DISTCC_HOSTS=hanwks0138


rem @note Specify how many files will be compiled simultaneously using DISTCC.
set JOBS_FOR_DISTCC=2


rem @note Specify memory limits for java compile.
set JAVA_OPTS=-Xms512m -Xmx1024m


rem @hide
set SETENV_DONE=1

:end

