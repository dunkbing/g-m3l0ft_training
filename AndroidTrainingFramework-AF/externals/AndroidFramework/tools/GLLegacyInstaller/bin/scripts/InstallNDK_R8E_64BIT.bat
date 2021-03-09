ECHO OFF 
SET installPath=%1
if NOT [%2]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Calling this bat with more than one parameter is wrong. &echo; Or you have spaces in your path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
)	
if [%1]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Calling this bat with no parameter is wrong. &echo; Please specify a directory path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
) 
:main
	ECHO OFF
	CLS
	ECHO _______________________________
	ECHO NDK Auto Installer VERSION=1.03
	ECHO _______________________________
	set STRING=%installPath%
	call :check_pathstring_eligibillity ||exit /b 1	
	call :accommodate_pathstring
	set installPath=%STRING%
	
	call :check_Net ||exit /b 1
	rem call :check_for_wget_exe ||exit /b 1
	call :check_for_unzip_exe ||exit /b 1
	echo Install Path = %installPath% 
	echo Please wait to get NDK from svn!!!
	set NDKArchive=ndk.zip
	
	rem call %~dp0wget.exe  --quiet -O %temp%\%NDKArchive% http://dl.google.com/android/ndk/android-ndk-r8d-windows.zip
	rem call %~dp0wget.exe  --quiet -O %temp%\%NDKArchive% http://dl.google.com/android/ndk/android-ndk-r8e-windows-x86_64.zip
	svn export https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/android-ndk-r8e-windows-x86_64.zip %temp%\%NDKArchive% --quiet --force
	if %ERRORLEVEL% == 1 ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Fetching NDK package from the net failed. For unknown reasons. &echo; Retry or install the NDK manually by going to http://developer.android.com/tools/sdk/ndk/index.html &echo &echo; Press a key!&pause>NUL"						
		
		
		exit /b 1
	) else (
		ECHO NDK is ready to install.
		echo Unzip the archive... please wait!!!
		call :unzip_NDK_Archive
		if %ERRORLEVEL% == 1 ( 
			@echo off
			start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Unziping Ant package failed for curious reasons. &echo; In this case, retry the setup at a later time, &echo; or install the Ant manually by going to http://ant.apache.org/bindownload.cgi and downloading apache-ant...bin.zip. &echo; Press a key!&pause>NUL"				
			
			
			exit /b 1
		)
	)
	call :delete_unzip_exe
	ECHO NDK is now ready for cplusplus game forgery.
	EXIT /B 0
goto:eof

:check_Net
	set _yahoo=1
	PING -n 2 www.yahoo.com |findstr TTL= || ( 
		set _yahoo=0
	)
	PING -n 2 www.oracle.com |findstr TTL= || (
		if %_yahoo% EQU 0 (
			@echo off
			start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Internet connection is off. &echo; This setup cannot continue if there is no connection. &echo; Press a key!&pause>NUL"				
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

:check_for_wget_exe
    rem wget.exe is used to fetch setup.exe
    if exist %~dp0wget.exe (
		ECHO wget lives in forest at %~dp0
		exit /b 0 
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Wget.exe not found at at %~dp0, &echo; in order for this automatic installer to work, you need to put wget.exe the same directory as this bat. &echo; Press a key!&pause>NUL"						
		exit /b 1
	)
exit /b 1

:delete_unzip_exe
	rem getting ridd of trash
	if exist %temp%\%NDKArchive% ( 
		Echo Deleting %NDKArchive% from %temp%\
		DEL /Q /F %temp%\%NDKArchive%
	)
exit /b 0

:unzip_NDK_Archive
	call %~dp0unzip.exe -o -q %temp%\%NDKArchive% -d %installpath%
	Move %installpath%\android-ndk* %installpath%\ndk
	robocopy /move /e %installpath%\ndk %installpath%
exit /b 0

:check_for_unzip_exe
	rem unzip.exe is used to uncompress ant archive
	if exist %~dp0unzip.exe ( 
		ECHO Unzip lives to serve at %~dp0
		exit /b
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Unzip.exe does not live in the district %~dp0, please aquire it from &echo; someGLLegacyTrunk\Externals\GLLegacy\_AndroidToolBox\tools\zip\unzip.exe by copying to %~dp0 &echo; Press a key!&pause>NUL"				
		exit /b 1
	)
exit /b 0

:check_pathstring_eligibillity
	IF "%STRING:~1,2%" NEQ ":\" ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Your specified directorypath is not valid. &echo; Expected form: [DriveLetter]:\[Directories] &echo; Press a key!&pause>NUL"		
		exit /b 1
	)
	IF "%STRING:~3,3%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Not recommended to install Ant directly to a drive, &echo; you must specify a directory. &echo; Press a key!&pause>NUL"		
		exit /b 1		
	) 
	if NOT exist "%STRING:~0,2%" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Your specified disk drive does not exist at this thinking box. &echo; Ensure a valid directorypath. &echo; Press a key!&pause>NUL"			
		exit /b 1
	) 
	Echo DirectoryPath form is met. 
exit /B 0

:accommodate_pathstring
	IF "%STRING:~-1%"=="\" SET STRING=%STRING:~0,-1%
	IF "%STRING:~-1%"=="/" SET STRING=%STRING:~0,-1%
	ECHO Final directorypath: %STRING%
exit /b 0

:acquire_drive_letter
	SET _Drive=%_ROOTDIR:~0,1%
	if "%_DRIVE%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallNDK.Bat &color cf&echo;&echo; Getting drive letter from your directorypath has failed, &echo; check your directorypath to be valid. &echo; Press a key!&pause>NUL"			
		exit /B 1
	) else ( 
		ECHO Ant will be installed on drive %_DRIVE%
		exit /B 0
	)
	ECHO aqq
exit /b 0


	

	