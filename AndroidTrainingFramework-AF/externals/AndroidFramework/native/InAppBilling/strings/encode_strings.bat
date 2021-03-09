@echo off

set PACKAGE_FOLDER=%~dp0\..\..\..\
set RES_FOLDER=%PACKAGE_FOLDER%\res_apk\InAppBilling\res\values
set H_FOLDER=%PACKAGE_FOLDER%\native\InAppBilling\

call JStrings.exe InAppBillingStrings.txt OlI1MEX2NDc IAB

if not exist iab_strings_encoded.xml (
	echo ERROR compiling strings.
	pause
) else (
	ECHO moving to %RES_FOLDER%
	move /Y iab_strings_encoded.xml %RES_FOLDER%>NUL
	ECHO moving to %H_FOLDER%
	move /Y iab_strings_encoded.h %H_FOLDER%>NUL
)