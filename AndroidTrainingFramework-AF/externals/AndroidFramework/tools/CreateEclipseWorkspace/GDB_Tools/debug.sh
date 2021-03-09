#!/bin/sh

adb shell am start -n $2/$2.GL2JNIActivity

if [ $# -ne 2 ]
then
    echo "usage: $0 /path/to/your/library.so packagename.of.your.activity"
    echo "for example:"
    echo "  $0 /code/mydemo/libs/armeabi/libmydemo.so com.nvidia.devtech.mydemo"
    exit
fi

if [ ! -f $1 ]
then
    echo "ERROR: That library file doesn't exist"
    exit
fi

cp $1 device_libs

p=`adb shell ps | grep $2 | awk '{print $2}'`
if [ "$p" = "" ];
then
    echo "ERROR: That doesn't seem to be a running process. Please make sure your"
    echo "application has been started and that you are using the correct"
    echo "namespace argument."
    exit
fi



adb forward tcp:12345 tcp:12345
adb shell run-as $2 /data/data/$2/lib/gdbserver :12345 --attach $p


