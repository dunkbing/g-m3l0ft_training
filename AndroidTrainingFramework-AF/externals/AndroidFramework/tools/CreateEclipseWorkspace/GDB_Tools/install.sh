#!/bin/sh
mkdir device_libs
#adb push prebuilt/gdbserver /data
#adb shell chmod 755 /data/gdbserver
for file in $(adb shell ls /system/lib | tr "\n" " " | tr "\r" " "); do
    adb pull /system/lib/$file device_libs
done
adb pull /system/bin/app_process device_libs