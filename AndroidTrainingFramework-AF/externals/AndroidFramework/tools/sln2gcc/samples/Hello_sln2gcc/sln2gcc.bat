@echo off


call %~dp0..\..\bin\sln2gcc.exe  -i sln2gcc_monolith.xml  -t release  -p all  -g gcc_config1  -j 1
