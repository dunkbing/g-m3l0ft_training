/* Local includes */
#include "InGamePromotion.h"

#include "../package_utils.h"

#include "../_internal/logger.h"

/* Library includes */

/* Standard includes */



namespace acp_utils
{
    namespace modules
    {
		jclass InGamePromotion::s_PromotionClass = NULL;

		void InGamePromotion::SetPromotionClass()
		{
			if(s_PromotionClass == NULL)
			{
				s_PromotionClass = acp_utils::api::PackageUtils::GetClass("/PackageUtils/InGamePromotionPlugin");
			}
		}

	
        // IGP Launch
        bool InGamePromotion::LaunchIGP(acp_utils::helpers::Language language, bool portrait)
        {
		#if USE_IGP_FREEMIUM

			SetPromotionClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID    launchIGP = pEnv->GetStaticMethodID(s_PromotionClass, "launchIGP", "(IZ)Z");

            return pEnv->CallStaticBooleanMethod(s_PromotionClass, launchIGP, language, portrait);

		#else
			LOG_ERROR("USE_IGP_FREEMIUM not enabled. Are you sure you need to call this function?");
			return 0;
		#endif
        }

		void InGamePromotion::RetrieveItems(acp_utils::helpers::Language language, const char* gamecode)
		{
		#if USE_IGP_FREEMIUM
		
			SetPromotionClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jstring gameCodeString = pEnv->NewStringUTF(gamecode);
            jmethodID    retrieveItems = pEnv->GetStaticMethodID(s_PromotionClass, "retrieveItems", "(ILjava/lang/String;)V");

            pEnv->CallStaticVoidMethod(s_PromotionClass, retrieveItems, language, gameCodeString);

		#else
			LOG_ERROR("USE_IGP_FREEMIUM not enabled. Are you sure you need to call this function?");
		#endif

		}
	}
}