#ifndef __GAME_FUNC_TO_IMPLEMENT_H__
#define __GAME_FUNC_TO_IMPLEMENT_H__

/*Require includes for extern functions*/
#include <android/native_window_jni.h> //for ANativeWindow

extern void OnGameInit();
extern void OnGameResume();
extern void OnGamePause();
extern void OnWindowStateChange(ANativeWindow*);
extern void OnGameTouchEvent(int, float, float, int);
extern void NativeOnKeyAction(int, bool);
extern void NativeLowBattery();


#if USE_WELCOME_SCREEN
extern void splashScreenFunc(const char*);
#endif //USE_WELCOME_SCREEN

#if USE_IGP_FREEMIUM
extern void OnIGPClosed();
#if USE_IGP_REWARDS 
	extern void OnIgpReward(int amount, char* type, char* message);
#endif
#endif //USE_IGP_FREEMIUM

#if SIMPLIFIED_PN
	extern void OnPushNotificationResponseCB(const std::string& notificationData);
#endif //SIMPLIFIED_PN


#endif//__GAME_FUNC_TO_IMPLEMENT_H__


