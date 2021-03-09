#include "config_Android.h"
#if USE_KDDI_GIFTING
#include "KDDIGifting.h"
#include "math.h"

extern "C" void KDDIGifting_setJavaVM(JavaVM* javaVM)
{
	KDDIGifting::setJavaVM(javaVM);
}

extern "C" int KDDI_CallSendingGift()
{
	return KDDIGifting::CallSendingGift();
}

JNIEXPORT void JNICALL JNI_FUNCTION(kddigifting_KDDIGiftingHelper_nativeInit) (JNIEnv*  env, jclass kddigifting)
{
	KDDIGifting::init(kddigifting);
}

jclass 		KDDIGifting::sClassKDDIGifting 		= 0;
JavaVM* 	KDDIGifting::mJavaVM			= 0;
jmethodID sMethodCanUserReceiveKDDIMonthlyGift			= 0;
jmethodID sMethodPerformCheckUserCanReceiveKDDIGift			= 0;
jmethodID sMethodGetKDDIGiftAmount			= 0;

jmethodID sMethodPopupKDDIMonthlyGift			= 0;
jmethodID sMethodIfNeedToDisableKDDIMonthlyGift			= 0;
jmethodID sMethodDisableKDDIMonthlyGift			= 0;


void KDDIGifting::setJavaVM(JavaVM* javaVM)
{
	mJavaVM = javaVM;
}

void KDDIGifting::init(jclass kddigifting)
{
	JNIEnv* EnvJV = NULL;                           
	(mJavaVM)->AttachCurrentThread(&EnvJV, NULL);

  sClassKDDIGifting = (jclass)EnvJV->NewGlobalRef(kddigifting);	
	
	sMethodCanUserReceiveKDDIMonthlyGift = EnvJV->GetStaticMethodID (sClassKDDIGifting, "CanUserReceiveKDDIMonthlyGift", "()I");
	sMethodPerformCheckUserCanReceiveKDDIGift = EnvJV->GetStaticMethodID (sClassKDDIGifting, "PerformCheckUserCanReceiveKDDIGift", "()V");
	sMethodGetKDDIGiftAmount = EnvJV->GetStaticMethodID (sClassKDDIGifting, "getKDDIGiftAmount", "()I");
	
	sMethodPopupKDDIMonthlyGift = EnvJV->GetStaticMethodID (sClassKDDIGifting, "PopupKDDIMonthlyMessage", "()V");
	sMethodDisableKDDIMonthlyGift = EnvJV->GetStaticMethodID (sClassKDDIGifting, "DisableKDDIMonthlyGift", "()V");
	sMethodIfNeedToDisableKDDIMonthlyGift = EnvJV->GetStaticMethodID (sClassKDDIGifting, "CheckIfNeedToDisableKDDIMonthlyGift", "()I");
}

int KDDIGifting::CallSendingGift()
{
 		int amount = 0;
		if (nativeCanUserReceiveKDDIMonthlyGift()==1)
		{
			nativePopupKDDIMonthlyMessage();
			amount = nativeGetKDDIGiftAmount();

 			nativeDisableKDDIMonthlyGift();
			if(amount>0)
				return amount;//sucess
			else
				return 0;
		}
		if ( nativeIfNeedToDisableKDDIMonthlyGift() == 1 )
		{
 			nativeDisableKDDIMonthlyGift();
			return 0;//fail
		}
}


int KDDIGifting::nativeCanUserReceiveKDDIMonthlyGift()
{
	JNIEnv* EnvJV = NULL;
	(mJavaVM)->AttachCurrentThread(&EnvJV, NULL);
	return EnvJV->CallStaticIntMethod(sClassKDDIGifting, sMethodCanUserReceiveKDDIMonthlyGift);
}

void KDDIGifting::nativePerformCheckUserCanReceiveKDDIGift()
{
	JNIEnv* EnvJV = NULL;
	(mJavaVM)->AttachCurrentThread(&EnvJV, NULL);
	EnvJV->CallStaticIntMethod(sClassKDDIGifting, sMethodPerformCheckUserCanReceiveKDDIGift);
}

int KDDIGifting::nativeGetKDDIGiftAmount()
{
	JNIEnv* EnvJV = NULL;
	(mJavaVM)->AttachCurrentThread(&EnvJV, NULL);
	return EnvJV->CallStaticIntMethod(sClassKDDIGifting, sMethodGetKDDIGiftAmount);
}

void KDDIGifting::nativePopupKDDIMonthlyMessage()
{
	JNIEnv* EnvJV = NULL;
	(mJavaVM)->AttachCurrentThread(&EnvJV, NULL);
	EnvJV->CallStaticVoidMethod(sClassKDDIGifting, sMethodPopupKDDIMonthlyGift);
}
int KDDIGifting::nativeIfNeedToDisableKDDIMonthlyGift()
{
	JNIEnv* EnvJV = NULL;
	(mJavaVM)->AttachCurrentThread(&EnvJV, NULL);
	return EnvJV->CallStaticIntMethod(sClassKDDIGifting, sMethodIfNeedToDisableKDDIMonthlyGift);
}
void KDDIGifting::nativeDisableKDDIMonthlyGift()
{
	JNIEnv* EnvJV = NULL;
	(mJavaVM)->AttachCurrentThread(&EnvJV, NULL);
	EnvJV->CallStaticVoidMethod(sClassKDDIGifting, sMethodDisableKDDIMonthlyGift);
}

#endif //#if USE_KDDI_GIFTING