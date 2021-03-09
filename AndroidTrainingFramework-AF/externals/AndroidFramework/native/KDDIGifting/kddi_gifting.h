#if USE_KDDI_GIFTING

#ifndef __KDDI_GIFTING__
#define __KDDI_GIFTING__
#include <jni.h>
#include <map>
#include <string>

#ifdef  __cplusplus
extern "C" {
#endif

  void 		KDDIGifting_setJavaVM(JavaVM* javaVM);
  int 		KDDI_CallSendingGift();

#ifdef  __cplusplus
}
#endif

#endif
#endif