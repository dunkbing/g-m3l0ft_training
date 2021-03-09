@echo off

echo.
echo ---------------------------------------------------
echo Creating Nsight Debug Solution
echo ---------------------------------------------------


reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\NET Framework Setup\NDP\v4"

if %errorlevel%==0 (

	if "%ANDROID_FRAMEWORK_CONFIG%"=="" (
		echo.
		echo Environment variables are not set. Cannot create Nsight Solution.
		echo.
	) else (
	
		if "%MAKEFILE_XML%"=="" (	
			set MAKEFILE_XML=%MAKEFILE_XML_arm%
		) 
					
		if "%MAKEFILE_XML%"=="" (	
			echo.
			echo sln2gcc path not set. Cannot create Nsight Solution.
			echo.
		) else (
			call %ANDROID_PACKAGE_DIR%\Tools\NsightBuilder\NsightSlnBuilder.exe   %ANDROID_PACKAGE_DIR%  %ANDROID_FRAMEWORK_CONFIG%  %ANDROID_PACKAGE_DIR%\tools\NsightBuilder\NsightSolution  %MAKEFILE_XML%   
		)
	)
	
) else (
	echo.
	echo .NET Framework 4 was not found. Cannot create Nsight Solution. 
	echo.
)

echo ---------------------------------------------------
echo.
