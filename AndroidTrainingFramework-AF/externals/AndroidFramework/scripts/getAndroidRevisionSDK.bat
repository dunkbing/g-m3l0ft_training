@echo off

set ANDROID_SDK_REVISION=13

for /f "tokens=1,2 delims==" %%G in (%ANDROID_HOME%/tools/source.properties) do (

	if "Pkg.Revision"=="%%G" (
		set ANDROID_SDK_REVISION=%%H
		goto:eof
	)
)



