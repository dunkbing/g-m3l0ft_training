
ECHO OFF 
SET installPath=%1
if NOT [%2]==[] (
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo.&echo. Calling this bat with more than one parameter is wrong. &echo. Or you have spaces in your path. &echo.&echo. &echo. Press a key!&pause>NUL"
	EXIT /B 1
)	
if [%1]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat &color cf&echo;&echo; Calling this bat with no parameter is wrong. &echo; Please specify a directory path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
) 
:main
	ECHO OFF
	CLS
	ECHO ________________________________________________
	ECHO Java Development Kit Auto Installer VERSION=1.06
	ECHO ________________________________________________
	set _OS_BIT=0
	call :check_os	
	set STRING=%installPath%
	call :check_pathstring_eligibillity ||exit /b 1	
	call :accommodate_pathstring
	set installPath=%STRING%	

	call :check_Net ||exit /b 1
	rem call :check_for_wget_exe ||exit /b 1
	call :uninstall_java6-45
	echo installpath = %installPath% 
	
	
	if %_OS_BIT% EQU 64 (
		COLOR 0E
		ECHO _____________________________________
		ECHO Fetching java for 64bit, Please wait...
		ECHO _____________________________________
		rem call %~dp0wget.exe -O %temp%\javakit.exe --no-check-certificate --no-cookies --header "Cookie: s_nr=1359635827494; s_cc=true; gpw_e24=http%3A%2F%2Fwww.oracle.com%2Ftechnetwork%2Fjava%2Fjavase%2Fdownloads%2Fjdk6downloads-1902814.html; s_sq=%5B%5BB%5D%5D; gpv_p24=no%20value"  https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/jdk-6u39-windows-x64.exe 
		svn export https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/jdk-6u45-windows-x64.exe %temp%\javakit.exe --force
	) 
	if %_OS_BIT% EQU 32 ( 
		COLOR 0E
		ECHO _____________________________________
		ECHO Fetching java for 32bit, Please wait...
		ECHO _____________________________________
		rem call %~dp0wget.exe -O %temp%\javakit.exe --no-check-certificate --no-cookies --header "Cookie: s_nr=1359635827494; s_cc=true; gpw_e24=http%3A%2F%2Fwww.oracle.com%2Ftechnetwork%2Fjava%2Fjavase%2Fdownloads%2Fjdk6downloads-1902814.html; s_sq=%5B%5BB%5D%5D; gpv_p24=no%20value"  https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/jdk-6u39-windows-i586.exe
		svn export https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/jdk-6u45-windows-i586.exe %temp%\javakit.exe --force
	)
	COLOR 0F
	if errorlevel 1 ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo; Fetching jdk package from the net failed. &echo; For unknown reasons. Retry this setup at a later time. &echo; Otherwise install the jdk manually by going to www.oracle.com and download jdk 6u39 for your platform. &echo; Press a key!&pause>NUL"		
		
		exit /b 1
	) else (
		ECHO JDK is ready to install.
		call %temp%\javakit.exe /s INSTALLDIR=%installPath%
		if errorlevel 1 ( 
			ECHO ERROR occured!!! Try uninstall java SDK before install it!			
			pause
			call :delete_javakit.exe
			exit /b 1
		)
	)
	rem if NOT defined JAVA_HOME ( 
		ECHO Set JAVA_HOME
		SETX JAVA_HOME %installPath% /m
		if errorlevel 1 ( 
			ECHO ERROR occured!!! Could not set JAVA_HOME. Please set it manually.
			pause
		)		
	rem )
	call :delete_javakit.exe
	ECHO Java now works as coffee.
	pause
	exit
goto:eof
	
:check_DriveSpace
	Echo Internal drive = %_Drive%
	set _d=%_Drive%
	set FreeSpace=NULL
	for /f "usebackq delims== tokens=2" %%x in (`wmic logicaldisk where "DeviceID='%_Drive%:'" get FreeSpace /format:value`) do set FreeSpace=%%x
	SET _var=%FreeSpace%
	ECHO freespace = %FreeSpace%
	if %_var% GTR 10485760 ( 
	ECHo DRIVE %_Drive%:\ freespace = "%FreeSpace%"
		ECHO Freespace is greater than 1GB
		exit /b 0
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo;  Free space on %_d% is less than 1GB. More is needed. &echo; Press a key!&pause>NUL"				
		exit /b 1
	)	
exit /B 0

:check_Net
	set _yahoo=1
	PING -n 2 www.yahoo.com |findstr TTL= || ( 
		set _yahoo=0
	)
	PING -n 2 www.oracle.com |findstr TTL= || (
		if %_yahoo% EQU 0 (
			@echo off
			start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo; Internet connection is off. &echo; Internet is necessary&echo;&echo; &echo; Press a key!&pause>NUL"
			set _NET=0
			exit /b 1 
		) 
	)
	PING -n 2 www.oracle.com |findstr TTL= && ( 
		echo Internet connection is on.
		set _NET=1
		exit /b 0
	)
	rem if %_NET% EQU 1 ( 
exit /B 0
	
:check_os
	@echo off 
	Set RegQry=HKLM\Hardware\Description\System\CentralProcessor\0
	 
	REG.exe Query %RegQry% > checkOS.txt
	 
	FindStr /i "x86" < CheckOS.txt > StringCheck.txt
	 
	If %ERRORLEVEL% == 0 (
		Echo "This is 32 Bit Operating system"
		set _OS_BIT=32
	) ELSE (
		Echo "This is 64 Bit Operating System"
		set _OS_BIT=64
	)
	rem return _OS_BIT
exit /B 0

:check_for_wget_exe
    rem wget.exe is used to fetch setup.exe
    if exist %~dp0wget.exe (
		ECHO %_PREFIX% Wget.exe lives at %~dp0
		exit /b 0 
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo;  wget.exe is missing from this bat local dir,  &echo; You can aquire wget.exe from net(ex: http://users.ugent.be/~bpuype/wget/ &echo; Press a key!&pause>NUL"
		exit /b 1
	)
exit /b 1

:delete_javakit.exe
	if exist %temp%\javakit.exe ( 
		Echo Deleting javakit from %temp%\
		DEL /Q /F %temp%\javakit.exe
	)
exit /b 0

:uninstall_java6-45
	msiexec /qn /norestart /x {64A3A4F4-B792-11D6-A78A-00B0D0160450}
exit /b 0

:check_pathstring_eligibillity
	IF "%STRING:~1,2%" NEQ ":\" ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo; Your specified directory path is not valid. &echo; Expected form: [DriveLetter]:\[Directories] &echo; Press a key!&pause>NUL"		
		exit /b 1
	)
	IF "%STRING:~3,3%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo; Not recommended to install java directly to a drive, &echo; you must specify a directory. &echo; Press a key!&pause>NUL"		
		exit /b 1		
	) 
	if NOT exist "%STRING:~0,2%" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo; Your specified drive does not exist at this thinking box. &echo; Ensure a valid directory path. &echo; Press a key!&pause>NUL"			
		exit /b 1
	) 
	Echo directory path form is met. 
exit /B 0

:accommodate_pathstring
	IF "%STRING:~-1%"=="\" SET STRING=%STRING:~0,-1%
	IF "%STRING:~-1%"=="/" SET STRING=%STRING:~0,-1%
	ECHO Final directory path: %STRING%
exit /b 0

:acquire_drive_letter
	SET _Drive=%_ROOTDIR:~0,1%
	if "%_DRIVE%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallJava.Bat:&color cf&echo;&echo; Getting drive letter from your directory path has failed, &echo; check your directory path to be valid. &echo; Press a key!&pause>NUL"			
		exit /B 1
	) else ( 
		ECHO Java will be installed on drive %_DRIVE%
		exit /B 0
	)
exit /b 0


	