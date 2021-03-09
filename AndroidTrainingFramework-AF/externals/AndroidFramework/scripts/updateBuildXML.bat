@echo off

rem update build.xml file
if %ANDROID_SDK_REVISION% LSS 14 (
	copy build.xml.sdk13 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% LSS 20 (
	copy build.xml.sdk14 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% EQU 20 (
	copy build.xml.sdk20 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% EQU 21 (
	copy build.xml.sdk21 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% EQU 21.1 (
	copy build.xml.sdk21 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% EQU 21.0.1 (
	copy build.xml.sdk21 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% EQU 22.0.5 (
	copy build.xml.sdk22 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% EQU 22.2.1 (
	copy build.xml.sdk222 buildTemp.xml>NUL
) else if %ANDROID_SDK_REVISION% EQU 22.3 (
	copy build.xml.sdk222 buildTemp.xml>NUL
) else (
	copy build.xml.sdk222 buildTemp.xml>NUL
)

rem Java 16 as default
set JVERSION=16
for /f tokens^=2-5^ delims^=.-_^" %%j in ('%JAVA_HOME%\bin\java -fullversion 2^>^&1') do (
	set "JVERSION=%%j%%k"
)

set USED_JDK_VERSION=16
if %JVERSION% EQU 17 (
	set USED_JDK_VERSION=17
)
echo #define			USED_JDK_VERSION				%USED_JDK_VERSION%		>>tmpConfigFile.h


SET FILE=buildTemp.xml
echo ::: preprocessing build.xml
%PREPROCESS_APP% -P -include tmpConfigFile.h %FILE% prepBuild.xml>>NUL
%PREPROCESS_XML_APP% prepBuild.xml build.xml>>NUL

del /Q prepBuild.xml>>NUL
del /Q buildTemp.xml>>NUL
del /Q tmpConfigFile.h>>NUL
echo .
