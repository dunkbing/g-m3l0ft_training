echo off
set _ROOTDIR=%1
ECHO Cygwin Auto Installer v1.0
if NOT [%2]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo&echo Calling this bat with more than one parameter is wrong. &ECHO;&echo; Or you have spaces in your path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
)	
if [%1]==[] (
	@echo off
	start %comspec% /c "mode 60,10&title ERROR InstalCygwin.Bat &color cf&echo;&echo; Calling this bat with no parameter is wrong. &echo; Please specify a directory path. &echo;&echo; &echo; Press a key!&pause>NUL"
	EXIT /B 1
) 
rem
rem Install or update Cygwin.
rem
rem Features of this batch file:
rem   * Mostly hands-free, except for stopping of running Cygwin
rem     processes and configuration of newly installed services.
rem   * Stops and starts Cygwin services.
rem   * Lists running Cygwin processes (setup.exe informs you that
rem     they are running, but does not list them).
rem   * Fetches latest setup.exe from cygwin.com.
rem   * Installs standard set of packages.
rem   * Updates all installed packages.
rem   * Runs rebaseall.
rem   * Installs standard services (syslogd, sshd).
rem   * Installs cyglsa.
rem
rem Copyright (c) 2012 Tom Schutter
rem All rights reserved.
rem
rem Redistribution and use in source and binary forms, with or without
rem modification, are permitted provided that the following conditions
rem are met:
rem
rem    - Redistributions of source code must retain the above copyright
rem      notice, this list of conditions and the following disclaimer.
rem    - Redistributions in binary form must reproduce the aboveprobably
rem      copyright notice, this list of conditions and the following
rem      disclaimer in the documentation and/or other materials provided
rem      with the distribution.
rem
rem THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
rem "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
rem LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
rem FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
rem COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
rem INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
rem BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
rem LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
rem CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
rem LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
rem ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
rem POSSIBILITY OF SUCH DAMAGE.
rem

setlocal
goto :main

:load_config
    set _PACKAGES=bash,make,libgcc1,gcc-core,gcc-g++,distcc,gawk,rsync,openssh,autossh,libssh2-devel,libssh2_1
    set _SITE=http://mirrors.kernel.org/sourceware/cygwin
    if "%_PACKAGES%" == "" (
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Cygwin packages definition is empty, most probably this bat was modified precariously.  &echo; Please write _PACKAGES=bash,gcc ... etc in this bat. &echo; Press a key!&pause>NUL"
        exit /b 1
    )
    if "%_ROOTDIR%" == "" (
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; The specified directory for cygwin is empty,  &echo; Specify a valid path. &echo; Press a key!&pause>NUL"
        exit /b 1
    )
    if "%_SITE%" == "" (
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Cygwin site is not defined in this bat,  &echo; specify one like http://mirrors.kernel.org/sourceware/cygwin &echo; Press a key!&pause>NUL"	
        exit /b 1
    )
exit /b 0


:check_admin
    rem Check for ADMIN privileges
    rem https://sites.google.com/site/eneerge/home/BatchGotAdmin

    "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system" >nul 2>&1
    if ERRORLEVEL 1 (
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; You do not have Administrator privileges,  &echo; Rerun from a "Run as Administrator" command prompt &echo; Press a key!&pause>NUL"		
        exit /b 1
	) else ( 
		echo You have Admin privileges
	)
exit /b 0


:check_for_wget_exe
    rem wget.exe is used to fetch setup.exe
	rem set _wget = %~dp0wget.exe
    if exist %~dp0wget.exe (
		ECHO  Wget.exe lives at %~dp0
		exit /b 0 
	) else ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo;  wget.exe is missing from this bat local dir,  &echo; You can aquire wget.exe from net(ex: http://users.ugent.be/~bpuype/wget/) &echo; Press a key!&pause>NUL"
		exit /b 1
	)
exit /b 1


:stop_services
    rem Stop all Cygwin services.
    if not exist "%_ROOTDIR%\bin\cygrunsrv.exe" (
		echo "%_ROOTDIR%\bin\cygrunsrv.exe" does not exist
		goto :eof 
	)
    for /f "usebackq" %%s in (`"%_ROOTDIR%\bin\cygrunsrv.exe" --list`) do (
        echo  Stopping %%s
        "%_ROOTDIR%\bin\cygrunsrv.exe" --stop %%s
    )
goto :eof


:start_service
    rem Start a single Cygwin service.
    echo Starting %1
    "%_ROOTDIR%\bin\cygrunsrv.exe" --start %1
goto :eof


:start_services
    rem Start all Cygwin services.

    rem Start syslogd first.
    for /f "usebackq" %%s in (`"%_ROOTDIR%\bin\cygrunsrv.exe" --list`) do (
        if "%%s" == "syslogd" call :start_service %%s
    )
    rem Start rest of services.
    for /f "usebackq" %%s in (`"%_ROOTDIR%\bin\cygrunsrv.exe" --list`) do (
        if not "%%s" == "syslogd" call :start_service %%s
    )
goto :eof


:check_for_cygwin_proc
    rem Check for any Cygwin processes.
    echo Checking for running Cygwin processes
    if not exist "%_ROOTDIR%\bin\ps.exe" (
		echo "%_ROOTDIR%\bin\ps.exe" does not exist
		exit /b 0
	)
    set _TEMPFILE=%TEMP%\cygwin_setup.txt
    :retry
        "%_ROOTDIR%\bin\ps.exe" -l | findstr /v /c:"/usr/bin/ps" > "%_TEMPFILE%"
        findstr /v /r /c:"PID.*COMMAND" "%_TEMPFILE%" | findstr /r /c:"^..*" > NUL:
        if ERRORLEVEL 1 goto :ignore
        echo Found running Cygwin processes
        type "%_TEMPFILE%"
        del "%_TEMPFILE%"
        :ask_abort_retry_ignore
            set /p _ANSWER=Abort, Retry, or Ignore?
            set _ANSWER=%_ANSWER:~0,1%
            if "%_ANSWER%" == "a" exit /b 1
            if "%_ANSWER%" == "A" exit /b 1
            if "%_ANSWER%" == "r" goto :retry
            if "%_ANSWER%" == "R" goto :retry
            if "%_ANSWER%" == "i" goto :ignore
            if "%_ANSWER%" == "I" goto :ignore
        goto :ask_abort_retry_ignore
    :ignore
exit /b 0



:rebaseall
    rem Run rebaseall.
    rem This should be handled by setup.exe, but at this time
    rem setup.exe does not handle all cases.  And running rebaseall
    rem unnecessarily should cause no harm.
    rem See http://cygwin.com/ml/cygwin/2012-08/msg00320.html
    echo  Running rebaseall
    "%_ROOTDIR%\bin\dash.exe" -c 'cd /usr/bin; PATH=. ; rebaseall'
goto :eof


:create_passwd
    rem Create local passwd and group files.
    if not exist "%_ROOTDIR%\etc\passwd" (
        echo Creating /etc/passwd
        "%_ROOTDIR%\bin\bash.exe" --login -i -c '/usr/bin/mkpasswd --local > /etc/passwd'
    )
    if not exist "%_ROOTDIR%\etc\group" (
        echo Creating /etc/group
        "%_ROOTDIR%\bin\bash.exe" --login -i -c '/usr/bin/mkgroup --local > /etc/group'
    )
goto :eof


:config_syslogd
    sc query syslogd | findstr "service does not exist" > NUL:
    if ERRORLEVEL 1 goto :eof

    echo  Configuring syslogd
    if not exist "%_ROOTDIR%\bin\syslogd-config" (
        echo  ERROR: inetutils not installed
        goto :eof
    )
    "%_ROOTDIR%\bin\bash.exe" --login -i -c "/usr/bin/syslogd-config"
goto :eof


:config_sshd
    sc query sshd | findstr "service does not exist" > NUL:
    if ERRORLEVEL 1 goto :eof

    echo Configuring sshd
    if not exist "%_ROOTDIR%\bin\ssh-host-config" (
        echo ERROR: opensshd not installed
        goto :eof
    )
    findstr /r "^sshd:" "%_ROOTDIR%\etc\passwd"
    if not ERRORLEVEL 1 (
        echo ERROR: sshd account found in /etc/passwd
        goto :eof
    )
    "%_ROOTDIR%\bin\bash.exe" --login -i -c "/usr/bin/ssh-host-config"
    echo Disabling reverse DNS lookup by sshd
    "%_ROOTDIR%\bin\bash.exe" --login -i -c "/usr/bin/sed -i -e 's/#UseDNS yes/UseDNS no/' /etc/sshd_config"
goto :eof


:config_lsa
    REM reg query HKLM\SYSTEM\CurrentControlSet\Control\Lsa /v "Authentication Packages" | findstr cyglsa > NUL:
    REM if not ERRORLEVEL 1 goto :eof
    REM echo Configuring cyglsa
    REM "%_ROOTDIR%\bin\bash.exe" --login -i -c "/usr/bin/cyglsa-config"
	ECHO Executing bash.exe --login -i
	start "" "%_ROOTDIR%\bin\bash.exe" --login -i "mkpasswd -l -c >> /etc/passwd; mkgroup -l -c >> /etc/group"
exit /B 0

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
			start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Internet connection is off. &echo; Internet is necessary&echo;&echo; &echo; Press a key!&pause>NUL"
			set _NET=0
			exit /b 1 
		) else ( 
			@echo off
			start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Cygwin site is offline. As this is an odd scenario,  &echo; you might try to redo this setup at later moments or get help from GLLegacy developers.&echo;&echo; &echo; Press a key!&pause>NUL"
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

:check_admin
    rem Check for ADMIN privileges
    rem https://sites.google.com/site/eneerge/home/BatchGotAdmin

    "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system" >nul 2>&1
    if ERRORLEVEL 1 (
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Your user has no Administrator privileges, &echo; You might rerun this with "Run as Administrator" &echo; Press a key!&pause>NUL"			
        exit /b 1
	) else ( 
		echo You have Admin privileges
	)
exit /b 0

:check_pathstring_eligibillity
	IF "%STRING:~1,2%" NEQ ":\" ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Your specified directory path is not valid. &echo; Expected form: [DriveLetter]:\[Directories] &echo; Press a key!&pause>NUL"		
		exit /b 1
	)
	IF "%STRING:~3,3%"=="" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Not recommended to install cygwin directly to a drive, &echo; you must specify a directory. &echo; Press a key!&pause>NUL"		
		exit /b 1		
	) 
	if NOT exist "%STRING:~0,2%" ( 
		@echo off	
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Your specified disk drive does not exist at this thinking box. &echo; Ensure a valid directory path. &echo; Press a key!&pause>NUL"			
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
		start %comspec% /c "mode 60,10&title ERROR InstallCygwin.Bat:&color cf&echo;&echo; Getting drive letter from your directory path has failed, &echo; check your directory path to be valid. &echo; Press a key!&pause>NUL"			
		exit /B 1
	) else ( 
		ECHO Cygwin will be installed on drive %_DRIVE%
		exit /B 0
	)
exit /b 0


:copy_cygwin_from_svn
	echo Please wait to checkout Cygwin from svn!!!
	svn checkout https://svn01/vc/gllegacy/release_v2.0/trunk/tools/sdk/Cygwin  %_ROOTDIR% --force
	if %ERRORLEVEL% == 1 ( 
		@echo off
		start %comspec% /c "mode 60,10&title ERROR InstallAnt.Bat:&color cf&echo;&echo; Fetching Cygwin package from svn failed. For unknown reasons. &echo; Press a key!&pause>NUL"						
		exit /b 1
	)
	echo @echo off>%_ROOTDIR%\Cygwin.bat
	echo. >>%_ROOTDIR%\Cygwin.bat
	echo %_ROOTDIR%\bin\bash --login -i>>%_ROOTDIR%\Cygwin.bat
	rem start "" "%_ROOTDIR%\bin\bash.exe" --login -i "mkpasswd -l -c >> /etc/passwd; mkgroup -l -c >> /etc/group"
exit /b 0

:main
    rem Global constants.
	ECHO OFF 
	CLS
	set STRING=%_ROOTDIR%
	ECHO __________________________________
	ECHO Cygwin Auto Installer VERSION=1.06
	ECHO __________________________________
	call :check_pathstring_eligibillity ||exit /b 1	
	call :accommodate_pathstring
	set _ROOTDIR=%STRING%
	call :acquire_drive_letter ||exit /b 1
	call :check_DriveSpace ||exit /b 1
	call :check_Net ||exit /b 1
    rem call :load_config ||exit /b 1
    rem call :check_admin ||exit /b 1
    rem call :check_for_wget_exe ||exit /b 1
    call :stop_services
    call :check_for_cygwin_proc ||exit /b 1
	call :copy_cygwin_from_svn ||exit /b 1
	call :config_lsa
	ECHO Cygwin is ready.
   EXIT /B 0
:exit