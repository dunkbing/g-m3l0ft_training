
rem --------------------------------------


call cecho {white}{\n}
call cecho {teal}DistCC config{\n}
call cecho {white}GLLEGACY_DISTCC = %GLLEGACY_DISTCC%{\n}
call cecho {white}GLLEGACY_DISTCC_USE_HOSTS = %GLLEGACY_DISTCC_USE_HOSTS%{\n}


rem --------------------------------------


call cecho {white}{\n}
call cecho {teal}Android NDK location config 
call cecho {white}{\n}

if not "%SUBST_LETTER_DRIVE%"=="" (
	set GLLEGACY_DISTCC_DRIVE_SELECTED=%SUBST_LETTER_DRIVE%
)

if "%GLLEGACY_DISTCC_DRIVE_SELECTED%"=="" (
    @echo GLLEGACY_DISTCC_DRIVE_SELECTED is not set!
    set ANDROID_NDK_HOME=%GLLEGACY_NDK_HOME%
	
) else (
    @echo GLLEGACY_DISTCC_DRIVE_SELECTED = %GLLEGACY_DISTCC_DRIVE_SELECTED%
    @echo GLLEGACY_NDK_HOME = %GLLEGACY_NDK_HOME%

    subst %GLLEGACY_DISTCC_DRIVE_SELECTED%: /D > NUL
    subst %GLLEGACY_DISTCC_DRIVE_SELECTED%: %GLLEGACY_NDK_HOME%
    if not exist %GLLEGACY_DISTCC_DRIVE_SELECTED%:\ndk-build (
		call cecho {red}----------------------------------------------------------{\n}
		call cecho {red}	GLLEGACY_DISTCC_DRIVE_SELECTED - %GLLEGACY_DISTCC_DRIVE_SELECTED%{\n}  
		call cecho {red}	Does not exist. Possible causes:                      {\n}  
		call cecho {red}	1. virtual drive cannot be created.                   {\n}  
		call cecho {red}	2. the letter is from a phisical drive.               {\n}  
		call cecho {red}----------------------------------------------------------{\n}
		call cecho {white}{\n}
        pause
        goto end
    )
    set ANDROID_NDK_HOME=%GLLEGACY_DISTCC_DRIVE_SELECTED%:\
)


set /p ndk_ver=<%ANDROID_NDK_HOME%\RELEASE.TXT
echo "Using Android NDK %ndk_ver% from %ANDROID_NDK_HOME%"

rem this is used in WinGDB. Some externals dependecies have to know where the ANDROID_HOME is.
setx ANDROID_HOME %ANDROID_HOME%
@echo ANDROID_HOME = %ANDROID_HOME%


rem ndk-build bash
set NDK_BUILD=%ANDROID_NDK_HOME%/ndk-build
set NDK_BUILD_CMD=%ANDROID_NDK_HOME%/ndk-build.cmd
set NDK_STACK=%ANDROID_NDK_HOME%/ndk-stack.exe


rem --------------------------------------


set TYPE_ARMEABI_arm=armeabi-v7a
set TYPE_ARMEABI_x86=x86

set MAKEFILE_XML_arm=%ANDROID_FRAMEWORK_CONFIG%\configs\project\%SLN2GCC_USED_arm%
set MAKEFILE_XML_x86=%ANDROID_FRAMEWORK_CONFIG%\configs\project\%SLN2GCC_USED_x86%

