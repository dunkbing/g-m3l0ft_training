@echo off
@echo Executing: %~f0

@echo call %~dp0\GetRelativePath.exe %1 %2 %~dp0\computed_relative_path.temp

call %~dp0\GetRelativePath.exe %1 %2>%~dp0\computed_relative_path.temp
set /p RELATIVE_PATH=<%~dp0\computed_relative_path.temp
del %~dp0\computed_relative_path.temp