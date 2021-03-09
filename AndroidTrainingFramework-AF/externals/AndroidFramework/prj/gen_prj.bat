@echo off 
SET MAKETOOL=..\tools\premake\release\premake5.exe

%MAKETOOL% --to=android_s2g android_s2g
