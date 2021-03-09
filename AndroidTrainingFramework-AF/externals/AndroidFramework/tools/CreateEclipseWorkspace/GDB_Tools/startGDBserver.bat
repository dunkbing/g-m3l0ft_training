@echo off

echo file %~dp0device_libs/libEngine3D.so > gdb.setup
echo set solib-search-path %~dp0device_libs/ >> gdb.setup

bash debug.sh %1 %2
