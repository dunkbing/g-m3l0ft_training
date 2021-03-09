ECHO OFF 
SET installPath=%1
if NOT [%2]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo; Calling this bat with more than one parameter is wrong. &echo; Or you have spaces in your path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
)		
if [%1]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstalPython.Bat &color cf&echo;&echo; Calling this bat with no parameter is wrong. &echo; Please specify a directory path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
) 	
:main
	ECHO OFF
	CLS
	ECHO __________________________________
	ECHO Python Auto Installer VERSION=1.04
	ECHO __________________________________
	set _OS_BIT=0
	call :check_os
	set STRING=%installPath%
	call :check_pathstring_eligibillity ||exit /b 1	
	call :accommodate_pathstring
	set installPath=%STRING%

	call :check_Net ||exit /b 1
	call :check_for_wget_exe ||exit /b 1
	call :uninstall_Python2_7_5
	echo Install path = %installPath% 
	set PythonArchive=python-2.7.5.msi
	
	if %_OS_BIT% EQU 64 (
		COLOR 0E
		ECHO ______________________________________________________
		ECHO Downloading python package, please wait for 1 minute...
		ECHO ______________________________________________________
		rem call %~dp0wget.exe --quiet -O %temp%\%PythonArchive% http://www.python.org/ftp/python/2.7.3/python-2.7.3.amd64.msi
		svn export https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/python-2.7.5.amd64.msi %temp%\%PythonArchive% --force
	) 
	if %_OS_BIT% EQU 32 ( 
		COLOR 0E
		ECHO ______________________________________________________
		ECHO Downloading python package, please wait for 1 minute...
		ECHO ______________________________________________________
		rem call %~dp0wget.exe --quiet -O %temp%\%PythonArchive% http://www.python.org/ftp/python/2.7.3/python-2.7.3.msi
		svn export https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/python-2.7.5.msi %temp%\%PythonArchive% --force
	)
	COLOR 0F
	if %ERRORLEVEL% == 1 ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo; Fetching python package from the net failed. For unknown reasons. &echo; Retry this setup at a later time. Otherwise install the python manually by going to &echo; http://www.python.org/download/releases/2.7.3/ and download the apropiate installer for your platform. &echo; Press a key!&pause>NUL"		
		
		exit /b 1
	) else (
		ECHO Python is ready to install.
		call :install_python
	)	
	call :delete_python
	ECHO Python is now ready to bite work.
	EXIT /B 0
goto:eof
	
:check_DriveSpace
	for /f "usebackq delims== tokens=2" %%x in (`wmic logicaldisk where "DeviceID='%_Drive%:'" get FreeSpace /format:value`) do set FreeSpace=%%x
	SET _var= %FreeSpace%
	if %_var% GTR 10485760 ( 
	ECHo DRIVE %_Drive%:\ freespace = "%FreeSpace%"
		ECHO Drive %_Drive% free space is greater than 1GB
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Your specified drive free space is less than 1GB,  &echo; in order to install Cygwin for GLLegacy make sure to have more space than this. &echo; Press a key!&pause>NUL"
		exit /B 1
	)	
exit /B 0

:check_Net
	set _yahoo=1
	PING -n 2 www.yahoo.com |findstr TTL= || ( 
		set _yahoo=0
	)
	PING -n 2 www.cygwin.com |findstr TTL= || (
		if %_yahoo% EQU 0 (
			@echo off
			start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo; Internet connection is off. &echo; Internet is necessary&echo;&echo; &echo; Press a key!&pause>NUL"
			set _NET=0
			exit /b 1 
		) 
	)
	PING -n 2 www.cygwin.com |findstr TTL= && ( 
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
exit /B

:check_for_wget_exe
    rem wget.exe is used to fetch setup.exe
    if exist %~dp0wget.exe (
		ECHO %_PREFIX% Wget.exe lives at %~dp0
		exit /b 0 
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo;  wget.exe is missing from this bat local dir,  &echo; You can aquire wget.exe from net(ex: http://users.ugent.be/~bpuype/wget/) &echo; Press a key!&pause>NUL"
		exit /b 1
	)
exit /b 1

:install_python
	msiexec /qn /i %temp%\%PythonArchive% TARGETDIR=%installPath% 
exit /b 0

:delete_python
	if exist %temp%\%PythonArchive% ( 
		Echo Deleting python archive from %temp%\
		DEL /Q /F %temp%\%PythonArchive%
	)
exit /b 0

:uninstall_Python2_7_5
	msiexec /qn /norestart /x {DBDD570E-0952-475F-9453-AB88F3DD565A} 	
exit /b 0
	
:check_pathstring_eligibillity
	IF "%STRING:~1,2%" NEQ ":\" ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo; Your specified directory path is not valid. &echo; Expected form: [DriveLetter]:\[Directories] &echo; Press a key!&pause>NUL"		
		exit /b 1
	)
	IF "%STRING:~3,3%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo; Not recommended to install python directly to a drive, &echo; you must specify a directory. &echo; Press a key!&pause>NUL"		
		exit /b 1		
	) 
	if NOT exist "%STRING:~0,2%" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo; Your specified drive does not exist at this thinking box. &echo; Ensure a valid directory path. &echo; Press a key!&pause>NUL"			
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
		start %comspec% /c "mode 60,10&title ERROR InstallPython.Bat:&color cf&echo;&echo; Getting drive letter from your directory path has failed, &echo; check your directory path to be valid. &echo; Press a key!&pause>NUL"			
		exit /B 1
	) else ( 
		ECHO Python will be installed on drive %_DRIVE%
		exit /B 0
	)
	ECHO aqq
exit /b 0

	