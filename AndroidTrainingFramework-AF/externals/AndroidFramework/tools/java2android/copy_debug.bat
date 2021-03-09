@echo off
if exist proj\java2android\Debug\java2android.exe (
	copy /B /Y proj\java2android\Debug\java2android.exe bin\java2android.exe>NUL
	echo Done.
) else (
	echo Could not copy.
)
