@echo off


if exist %~dp0GLLegacyDistccEnv.bat (
    call %~dp0GLLegacyDistccEnv.bat
)

if exist %~dp0GLLegacyInstallerEnv.bat (
    call %~dp0GLLegacyInstallerEnv.bat
)


if not exist "%GLLEGACY_CYGWIN_BIN%\distcc.exe" (
	echo ERROR! you have to install distcc in cygwin!
	goto eof
)

mkdir %GLLEGACY_DISTCC_SERVER_TEMP%

if NOT "%GLLEGACY_NDK_HOME_1_DRIVE%"=="" (
	if not exist "%GLLEGACY_NDK_HOME_1%" (
		echo ERROR! %GLLEGACY_NDK_HOME_1% invalid path
		goto eof
	)
	subst %GLLEGACY_NDK_HOME_1_DRIVE% /d
	subst %GLLEGACY_NDK_HOME_1_DRIVE%: %GLLEGACY_NDK_HOME_1%
)

if NOT "%GLLEGACY_NDK_HOME_2_DRIVE%"=="" (
	if not exist "%GLLEGACY_NDK_HOME_2%" (
		echo ERROR! %GLLEGACY_NDK_HOME_2% invalid path
		goto eof
	)
	subst %GLLEGACY_NDK_HOME_2_DRIVE% /d
	subst %GLLEGACY_NDK_HOME_2_DRIVE%: %GLLEGACY_NDK_HOME_2%
)

if NOT "%GLLEGACY_NDK_HOME_3_DRIVE%"=="" (
	if not exist "%GLLEGACY_NDK_HOME_3%" (
		echo ERROR! %GLLEGACY_NDK_HOME_3% invalid path
		goto eof
	)
	subst %GLLEGACY_NDK_HOME_3_DRIVE% /d
	subst %GLLEGACY_NDK_HOME_3_DRIVE%: %GLLEGACY_NDK_HOME_3%
)


set PATH=%GLLEGACY_CYGWIN_BIN%;%PATH%
cd %~dp0
call %GLLEGACY_CYGWIN_BIN%\bash.exe -i distccDaemon.sh