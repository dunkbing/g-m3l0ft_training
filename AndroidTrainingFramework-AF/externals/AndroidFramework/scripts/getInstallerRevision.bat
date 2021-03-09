for /f "tokens=5" %%i in ('SubWCRev %~dp0 ^| findstr /B "Last committed at revision"') do set INSTALLER_REVISION=%%i
