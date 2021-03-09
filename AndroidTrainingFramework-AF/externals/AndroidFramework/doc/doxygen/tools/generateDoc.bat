@echo off
rem This script assums that svnversion.exe is in your path

rem get latest SVN version
FOR /F "tokens=*" %%V IN ('svnversion') DO SET AF_REVISION=%%V

rem Replace in file
copy doxyAF doxyTmp
call fart.exe doxyTmp AF_REVISION "%AF_REVISION%"

rem Generate Doc
doxygen.exe doxyTmp
del /Q doxyTmp

rem Show document
call ..\html\index.html

