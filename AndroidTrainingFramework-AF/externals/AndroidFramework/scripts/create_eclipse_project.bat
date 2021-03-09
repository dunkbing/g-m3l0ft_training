@echo off
echo ================== CREATING ACP ECLIPSE PROJECT ==================

set CREATE_ECLIPSE_PROJECT_PATH=%ROOT_DIR%\tools\CreateEclipseWorkspace
set CREATE_ECLIPSE_PROJECT_TEMPLATE=%CREATE_ECLIPSE_PROJECT_PATH%\GDB_Tools
set CREATE_ECLIPSE_PROJECT_EXE=%CREATE_ECLIPSE_PROJECT_PATH%\CreateEclipseWorkspace\bin\Release\CreateEclipseWorkspace.exe 
set DESTINATION_FOLDER=%ROOT_DIR%\_project_eclipse
set SKIPED_FILES=%DESTINATION_FOLDER%\_SkipFiles_eclipse.txt

%CREATE_ECLIPSE_PROJECT_EXE% %ROOT_DIR%\ %DESTINATION_FOLDER%\  %SKIPED_FILES%

xcopy /Y /E /Q /D %CREATE_ECLIPSE_PROJECT_TEMPLATE%\* %DESTINATION_FOLDER%\GDB_Tools\*
xcopy /Y /E /Q /D %ROOT_DIR%\ext_libs\* %DESTINATION_FOLDER%\libs\*

if exist %DESTINATION_FOLDER% echo target=%API_LEVEL_NAME%>%DESTINATION_FOLDER%\project.properties

echo.
echo ---------------------------------------------------
echo Looking for external lib dependencies...
echo ---------------------------------------------------

set "external_lib_file=%ROOT_DIR%\external_lib.properties"

for /F "skip=1 usebackq tokens=* delims=#" %%i in (%external_lib_file%) do (
    set file_line=%%i
    call :process_line
)
goto eof

:process_line
for /F "usebackq tokens=1,2 delims= " %%a in ('%file_line%') do (
	set library_reference=%%a
	set library_path=%%b
	call :copylibrary
)
goto eof

:copylibrary
:: Replacing '.' by '_' for library_reference
set library_reference=%library_reference:.=_%
:: Replacing '/' by '\' for library_path
set library_path=%library_path:/=\%

echo %library_reference% %library_path%
copy /Y %library_path%\libs\*.jar %DESTINATION_FOLDER%\libs\*
if exist %library_path%\bin\classes.jar (
copy /Y %library_path%\bin\classes.jar %DESTINATION_FOLDER%\libs\%library_reference%.jar
)

:eof