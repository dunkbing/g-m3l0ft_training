#ifndef APP_H
#define APP_H

#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

#include "config_Android.h"

jclass Policy;
jclass LicenseCheck;
jclass hellojni;

jmethodID startGame;
jmethodID getSDFolder;
jmethodID updatePrefsID;
jmethodID updatePrefs2ID;


JNIEnv* mEnv;

int* lockPointer1;
int* lockPointer2;
int* lockPointer3;
int* lockPointer4;

void nativeStart();
void nativeGetSdFolderPath();

#endif
