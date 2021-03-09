@echo off



if exist %TEMP%\GLLegacyDistccENV.bat (
    call %TEMP%\GLLegacyDistccENV.bat
) else (
    @echo WARNING: "%TEMP%\GLLegacyDistccENV.bat" does not exists!
)

if exist %TEMP%\GLLegacyInstallerENV.bat (
    call %TEMP%\GLLegacyInstallerENV.bat
) else (
    @echo WARNING: "%TEMP%\GLLegacyInstallerENV.bat" does not exists!
)



start %~dp0GLLegacyInstaller.exe %~dp0InstallerSetup.xml --runAsAdmin --platforms=Android
