ECHO OFF 
SET installPath=%1
if NOT [%2]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; Calling this bat with more than one parameter is wrong. &echo; Otherwise you might have spaces in your path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
)	
if [%1]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat &color cf&echo;&echo; Calling this bat with no parameter is wrong. &echo; Please specify a directory path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
) 
:main
	ECHO OFF
	CLS
	rem COLOR 08
	ECHO ___________________________________________________
	ECHO Android Developer Tools Auto Installer VERSION=1.09
	ECHO ___________________________________________________
	set _OS_BIT=0
	call :check_os
	call :check_if_java_exists ||exit /b 1
	SET STRING=%installPath%
	call :check_pathstring_eligibillity ||exit /b 1		
	call :accommodate_pathstring
	SET installPath=%STRING%
	call :acquire_drive_letter ||exit /b 1
	call :check_DriveSpace ||exit /b 1
	call :check_Net ||exit /b 1
	rem call :check_for_wget_exe ||exit /b 1
	call :check_for_unzip_exe ||exit /b 1
	echo Install Path = %installPath% 
	set ADT=adt.zip
	COLOR 0E
	echo;
	ECHO _________________________________________________________________________________________________________________________________________________
	ECHO Please wait for the ADT package to come by... This ADT package has 500 MB, ETA 20-25 mins to download.
	ECHO _________________________________________________________________________________________________________________________________________________
	echo;
	if %_OS_BIT% EQU 64 (
		rem call %~dp0wget.exe  --quiet -O %temp%\%ADT% https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/adt-bundle-windows-x86_64-20130729.zip
		svn export https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/adt-bundle-windows-x86_64-20131030.zip  %temp%\%ADT%  --force
	)
	if %_OS_BIT% EQU 32 ( 
		rem call %~dp0wget.exe  --quiet -O %temp%\%ADT% https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/adt-bundle-windows-x86-20130729.zip
		svn export https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/adt-bundle-windows-x86-20131030.zip  %temp%\%ADT%  --force
	)

	if %ERRORLEVEL% == 1 ( 		
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; Fetching ADT package from the net failed. &echo; For unknown reasons. Retry this setup at a later time. &echo; Otherwise install ADT manually by going at http://developer.android.com/sdk/index.html &echo; And download the appropiate zip package. &echo; Press a key!&pause>NUL"				
		EXIT /B 1
	) else (
		if exist %temp%\%ADT% ( 
			COLOR 0F
			ECHO _________________________________________________________________________________________________________________________________________________
			ECHO Now I will unzip the %temp%\%ADT% at %installPath%. Please wait...
			ECHO _________________________________________________________________________________________________________________________________________________
			echo;
			call :unzip_ADT_Archive
			COLOR 07
			ECHO _________________________________________________________________________________________________________________________________________________
			ECHO Updating Android Developer Tools to latest API found to this date. Please hold...
			ECHO _________________________________________________________________________________________________________________________________________________	
			echo;
			call :updateADT
			COLOR 0F
			if %ERRORLEVEL% == 1 ( 
				@echo off
				start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; Unziping Ant package failed for curious reasons. &echo; In this case, retry the setup at a later time, or install ADT manually by going at &echo; http://developer.android.com/sdk/index.html and download the appropiate package. &echo; Press a key!&pause>NUL"								
				exit /b 1
			)			
		) else ( 
			@echo off
			start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; ADT package residency is invalid, at %temp%\%ADT%. &echo; Please retry later this setup,or install ADT manually by going at &echo; http://developer.android.com/sdk/index.html and download the appropiate package.  &echo; Press a key!&pause>NUL"											
			exit /b 1
		)
	)
	call :delete_adt_zip
	ECHO ADT is now ready to forge apks.
	EXIT /B 0
goto:eof

:check_if_java_exists
	IF NOT DEFINED JAVA_HOME ( 
		@echo off
		start %comspec% /c "mode 80,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; Java Development Kit(JDK) is missing from this thinking box. &echo; * Please install java with auto installer first &echo; * Or restart GLLegacyInstaller if you just have installed java &echo; * Or manually set environment variable JAVA_HOME to jdk root dir. &echo; Press a key!&pause>NUL"
		EXIT /B 1	
	)
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

:check_Net	
	for %%X in ( wmic.exe ) do (
		set wmicfound=%%~$PATH:X
	)
	if NOT defined wmicfound ( 
		@echo off
		start %comspec% /c "mode 60,10&title WARNING InstallADT.Bat &color 81&echo;&echo; Testing for access to global network has failed. &echo; Setup will continue assuming net connection is available on this thinking machine. &echo; Press a key!&pause>NUL"
		exit /b 0
	)		
	set _yahoo=1
	PING -n 2 www.yahoo.com |findstr TTL= || ( 
		set _yahoo=0
	)
	PING -n 2 www.cygwin.com |findstr TTL= || (
		if %_yahoo% EQU 0 (
			@echo off
			start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; Internet connection is off. &echo; Internet is necessary&echo;&echo; &echo; Press a key!&pause>NUL"
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
	
:check_for_wget_exe
    rem wget.exe is used to fetch setup.exe
    if exist %~dp0wget.exe (
		ECHO %_PREFIX% Wget.exe lives at %~dp0
		exit /b 0 
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo;  wget.exe is missing from this bat local dir,  &echo; You can aquire wget.exe from net(ex: http://users.ugent.be/~bpuype/wget/ &echo; Press a key!&pause>NUL"
		exit /b 1
	)
exit /b 1

:delete_adt_zip
	rem getting ridd of trash
	if exist %temp%\%ADT% ( 
		Echo Deleting %ADT% from %temp%\
		DEL /Q /F %temp%\%ADT%
	)
exit /b 0

:unzip_ADT_Archive
	call %~dp0unzip.exe -o -q %temp%\%ADT% -d %installpath%
	Move %installpath%\adt-bundle-windows* %installpath%\adt
	
	rem Move %installpath%\apache-ant* %installpath%\ant
	robocopy /move /e %installpath%\adt %installpath%	
exit /b 0

:check_for_unzip_exe
	rem unzip.exe is used to uncompress ant archive
	if exist %~dp0unzip.exe ( 
		ECHO Unzip lives to serve at %~dp0
		EXIT /B 0
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; Unzip.exe does not live in the district %~dp0, please aquire it from &echo; someGLLegacyTrunk\Externals\GLLegacy\_AndroidToolBox\tools\zip\unzip.exe by copying to %~dp0 &echo; Press a key!&pause>NUL"				
		EXIT /B 1
	)
exit /b 0

:acquire_drive_letter
	SET _Drive=%installPath:~0,1%
	if "%_DRIVE%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat:&color cf&echo;&echo; Getting drive letter from your directory path has failed, &echo; check your directory path to be valid. &echo; Press a key!&pause>NUL"			

		EXIT /B 1
	) else ( 
		ECHO ADT will be installed on drive %_DRIVE%
		exit /B 0
	)
	ECHO aqq
exit /b 0

:check_DriveSpace
	for %%X in ( wmic.exe ) do (
		set wmicfound=%%~$PATH:X
	)
	if NOT defined wmicfound ( 
		@echo off
		start %comspec% /c "mode 60,10&title WARNING InstallADT.Bat &color 81&echo;&echo; Checking free space on your thinking box has failed. &echo; Setup will continue assuming space requirements for installing ADT are met. &echo; At least 3GB are needed.  &echo; Press a key!&pause>NUL"
		exit /b 0
	)	
	for /f "usebackq delims== tokens=2" %%x in (`wmic logicaldisk where "DeviceID='%_Drive%:'" get FreeSpace /format:value`) do set FreeSpace=%%x
	SET _var= %FreeSpace%
	if %_var% GTR 31457280 ( 
	ECHo DRIVE %_Drive%:\ freespace = "%FreeSpace%"
		ECHO Drive %_Drive% free space is greater than 3GB
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Your specified drive free space is less than 1GB,  &echo; in order to install Cygwin for GLLegacy make sure to have more space than this. &echo; Press a key!&pause>NUL"
		EXIT /B 1
	)	
exit /B 0

:accommodate_pathstring
	IF "%STRING:~-1%"=="\" SET STRING=%STRING:~0,-1%
	IF "%STRING:~-1%"=="/" SET STRING=%STRING:~0,-1%
	ECHO Acomodated String: %STRING%
exit /b 0

:check_pathstring_eligibillity
	IF "%STRING:~1,2%" NEQ ":\" ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat &color cf&echo;&echo; Your specified directory path is not valid. &echo; Expected form: [DriveLetter]:\[Directories] &echo; Press a key!&pause>NUL"		
		
		EXIT /B 1
	)
	IF "%STRING:~3,3%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat &color cf&echo;&echo; Not recommended to install java directly to a drive, &echo; you must specify a directory. &echo; Press a key!&pause>NUL"		
		
		EXIT /B 1		
	) 
	if NOT exist "%STRING:~0,2%" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat &color cf&echo;&echo; Your specified drive does not exist at this thinking box. &echo; Ensure a valid directory path. &echo; Press a key!&pause>NUL"			
		
		EXIT /B 1
	) 
	Echo directory path form is met. 
exit /B 0

:updateADT
	SET updateFile=%temp%\ADTUpdateList.txt
	if not defined GLLEGACY_PYTHON_HOME ( 
		@echo off	
		rem start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat &color cf&echo;&echo; GLLEGACY_PYTHON_HOME is undefined. Please define manually or rerun the GLLegacy installer. &echo; Press a key!&pause>NUL"			
		
		exit /b 0		
	) else (
		SET execPython=%GLLEGACY_PYTHON_HOME%\python.exe
	)

	SET updateString=%installPath%\sdk\tools\android.bat list sdk
	SET updateReturn=%temp%\update.txt
	SET pythonProcScript=%~dp0ProcessUpdateList.py
	if not exist %pythonProcScript% ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallADT.Bat &color cf&echo;&echo; ProcessUpdateList.py script is missing from the folder of this bat. &echo; Press a key!&pause>NUL"			
		
		exit /b 1		
	)
	call %updateString% > %updateFile%
	%execPython% %pythonProcScript% %updateFile% > %updateReturn%
	FOR /F "tokens=1,2 delims=-=, " %%A in ( 
		%updateReturn%
	) do (  
		if %%A==LargestSDK1 SET LargestSDK1=%%B
		if %%A==LargestSDK2 SET LargestSDK2=%%B
		if %%A==LargestSDK3 SET LargestSDK3=%%B
		if %%A==LargestGAPI1 SET LargestGAPI1=%%B
		if %%A==LargestGAPI2 SET LargestGAPI2=%%B
		if %%A==LargestGAPI3 SET LargestGAPI3=%%B		
	)
	call %installpath%\sdk\tools\android.bat update sdk --no-ui --filter %LargestSDK1%,%LargestSDK2%,%LargestSDK3%,%LargestGAPI1%,%LargestGAPI2%,%LargestGAPI3%
	del %updateFile%
	del %updateReturn%
exit /B 0

:strip_whitespaces_from_string
	FOR /F "tokens=1 delims=-, " %%A in ( 
		"%STR%"
	) do ( 
		SET STR=%%A
	)
exit /b 0
