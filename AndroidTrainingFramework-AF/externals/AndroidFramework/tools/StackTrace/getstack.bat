@echo off
call config.bat

if not exist "%LOG_TXT%" (
	echo Log file not found: [%LOG_TXT%]
	echo Execute first getlog.bat batch file.
	goto eof
)
echo ===========================================

if exist "%NDK_STACK%" (
	echo Using NDK_STACK Tool [%NDK_STACK%]
	
	rem CHECK VALUES
	if not exist "%OBJS_PATH%" (
		echo :: OBJS_PATH not found.        value:[%OBJS_PATH%]
		goto eof
	)

	echo ===========================================
	echo -sym %OBJS_PATH% -dump %LOG_TXT%
	echo ===========================================
	call %NDK_STACK% -sym %OBJS_PATH% -dump %LOG_TXT% > log.temp
	
	%PYTHON% parse_stack_for_DSYM.py  %SYMBOLS_FILE% log.temp
	
) else (
	
	echo Using PARSE_STACK.PY Tool [%~dp0]

	rem CHECK VALUES
	if not exist "%SO_LIBRARY%" (
		echo :: SO_LIBRARY not found.        value:[%SO_LIBRARY%]
	)
	if not exist "%ARMABIADRR%" (
		echo :: ARMABIADRR not found.        value:[%ARMABIADRR%]
	)
	if not exist "%ARMABICPPFILT%" (
		echo :: ARMABICPPFILT not found.        value:[%ARMABICPPFILT%]
	)
	
	echo ===========================================
	echo -libSO %SO_LIBRARY% -logfile %LOG_TXT%
	echo ===========================================
	%PYTHON% parse_stack.py 
)

:eof

pause