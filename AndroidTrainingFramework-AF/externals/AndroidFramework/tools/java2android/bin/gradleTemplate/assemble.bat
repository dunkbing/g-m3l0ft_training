@echo off
set WRAPPER_PATH=%~dp0
set TEMP_PATH=%CD%
cd /D %WRAPPER_PATH%
call gradlew.bat assemble
cd /D %TEMP_PATH%
