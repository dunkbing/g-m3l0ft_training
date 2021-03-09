@echo off

set ZORO_FOLDER=%~dp0
set H_FOLDER=%ZORO_FOLDER%

del /Q %ZORO_FOLDER%\zoroStrings.txt>>NUL

echo ARM_PATH		"lib/armeabi-v7a/lib%SO_LIB_FILE%.so">>%ZORO_FOLDER%\zoroStrings.txt
echo X86_PATH		"lib/x86/lib%SO_LIB_FILE%.so">>%ZORO_FOLDER%\zoroStrings.txt
echo CLASSES			"classes.dex">>%ZORO_FOLDER%\zoroStrings.txt
echo ARCHIVE			"archive">>%ZORO_FOLDER%\zoroStrings.txt

call %ZORO_FOLDER%\StringToH.exe %ZORO_FOLDER%\zoroStrings.txt ZORO

if not exist ZOROstrings_cpp.h (
	echo ERROR compiling strings.
	pause
) else (
	ECHO moving to %H_FOLDER%
	move /Y ZOROstrings_cpp.h %H_FOLDER%>NUL
	move /Y ZOROstrings_java.h %H_FOLDER%>NUL
)

%PYTHON% %CONFIG_UPDATE% %ZORO_FOLDER%\ZOROstrings_cpp.h

REM pause