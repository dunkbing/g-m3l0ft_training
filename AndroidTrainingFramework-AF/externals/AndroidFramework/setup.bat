rmdir /s /q %~dp0..\AndroidFrameworkConfig
mkdir %~dp0..\AndroidFrameworkConfig
xcopy /s %~dp0template	%~dp0..\AndroidFrameworkConfig