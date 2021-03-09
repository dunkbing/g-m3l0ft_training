#if USE_KDDI_GIFTING
#ifndef JNI_KDDDI_GIFTING_H
#define JNI_KDDDI_GIFTING_H
	
#if OS_ANDROID
#include "config_Android.h"
#include "stdio.h"
#include "string.h"

#include "kddi_gifting.h"

#ifdef  __cplusplus
extern "C" {
#endif

  void	JNI_FUNCTION(kddigifting_KDDIGiftingHelper_nativeInit) (JNIEnv*  env, jclass thiz);

#ifdef  __cplusplus
}
#endif

class KDDIGifting
{
	public:
    static void 	init(jclass game);
    static int  nativeCanUserReceiveKDDIMonthlyGift();
    static void nativePerformCheckUserCanReceiveKDDIGift();
    static void nativePopupKDDIMonthlyMessage();
    static int nativeGetKDDIGiftAmount();
    static int nativeIfNeedToDisableKDDIMonthlyGift();
    static void nativeDisableKDDIMonthlyGift();
    static void setJavaVM(JavaVM* javaVM);
    static int CallSendingGift();
	
	private:
    static jclass 		sClassKDDIGifting;
		static JavaVM* 		mJavaVM;

};


#endif

#endif
#endif