
if not defined CECHO_TOOL (    
    call %ROOT_DIR%\tools\configureTools.bat
)

rem @note_ the path to JDK
set JAVA_HOME=%GLLEGACY_JAVA_HOME%
rem @note_ the path to SDK
set ANDROID_HOME=%GLLEGACY_ANDROID_SDK_HOME%
rem @note_ the path to cygwin Bin folder
set CYGWIN_BIN=%GLLEGACY_CYGWIN_BIN%
rem @note_ the path to Apache Ant Bin folder
set APACHE_ANT=%GLLEGACY_ANT_BIN%
rem @note_ the path to python
set PYTHON_HOME=%GLLEGACY_PYTHON_HOME%
rem @hide_
set PYTHON=%PYTHON_HOME%\python.exe


set CYGWIN_BASH=%CYGWIN_BIN%\bash.exe
rem suppress dos warning path on CYGWIN
set CYGWIN=nodosfilewarning
rem add CYGWIN to PATH
set PATH=%CYGWIN_BIN%;%ANDROID_HOME%\platform-tools;%PYTHON_HOME%;%PATH%

set CYGWIN=nodosfilewarning

rem *******************************************

rem specific variables needed by AndroidFramework

set ANDROID_FRAMEWORK_DIR=%ROOT_DIR%
set ANDROID_PACKAGE_DIR=%ROOT_DIR%
set ANDROID_TOOLS=%ROOT_DIR%\tools
set ANDROID_FRAMEWORK_CONFIG=%ROOT_DIR%..\AndroidFrameworkConfig

set SLN2GCC=%ANDROID_TOOLS%\sln2gcc\bin\sln2gcc.exe

set USE_SLN2GCC=1



