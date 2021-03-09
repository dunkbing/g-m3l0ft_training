#include "config_Android.h"


#if USE_ADS_SERVER
#include "GameUtils\Ads\AdManager.h"

/******** This JNI implementation is required by Gaia\source\CRM\CrmManager.cpp **************/

#include <jni.h>
#include <string>

extern "C" void androidShowInterstitialWithTags(const std::string& tags)
{
	acp_utils::modules::AdManager::ShowInterstitial(-1, tags); //-1 hack, to use a possibly previously seted language
}

#endif