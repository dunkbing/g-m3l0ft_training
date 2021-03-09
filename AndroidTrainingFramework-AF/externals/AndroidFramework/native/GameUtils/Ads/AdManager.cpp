/* Local includes */
#include "AdManager.h"
#include "../package_utils.h"
#include "../_internal/logger.h"

/* Library includes */

/* Standard includes */

namespace acp_utils
{
    namespace modules
    {
        // Default values for size and position if you don't touch them
        jclass                    AdManager::s_AdServerPlugin = 0;
        AdManager::BannerSize     AdManager::s_bannerSize     = BANNER_SMALL;
        AdManager::BannerPosition AdManager::s_bannerPosition = BOTTOM_CENTER;

        void AdManager::SetBannerProperties(BannerSize size, BannerPosition pos)
        {
#if USE_ADS_SERVER
            CheckAdClass();
			
			s_bannerSize     = size;
            s_bannerPosition = pos;

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
                
            // Show normal interstitial ads
            jmethodID showInterstitial = pEnv->GetStaticMethodID(s_AdServerPlugin, "SetBannerProperties", "(II)V");
            pEnv->CallStaticVoidMethod(s_AdServerPlugin, showInterstitial, (int)size, (int)pos);
#else
            LOG_ERROR("AdManager -- SetBannerProperties with USE_ADS_SERVER 0");
#endif // USE_ADS_SERVER
        }

        bool AdManager::ShowBanner(helpers::Language language)
        {
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
            
            jmethodID showBanner = pEnv->GetStaticMethodID(s_AdServerPlugin, "ShowBanner", "(I)Z");
            return pEnv->CallStaticBooleanMethod(s_AdServerPlugin, showBanner, (int)language);
#else
            LOG_ERROR("AdManager -- ShowBanner with USE_ADS_SERVER 0");
            return false;
#endif // USE_ADS_SERVER
        }

        bool AdManager::HideBanner()
        {
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
            
            jmethodID hideBanner = pEnv->GetStaticMethodID(s_AdServerPlugin, "HideBanner", "()Z");
            return pEnv->CallStaticBooleanMethod(s_AdServerPlugin, hideBanner);
#else
            LOG_ERROR("AdManager -- HideBanner with USE_ADS_SERVER 0");
            return false;
#endif // USE_ADS_SERVER
        }

        void AdManager::ShowInterstitial(helpers::Language language, std::string tags)
        {
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
            
            if(tags == "")
            {
                // Show normal interstitial ads
                jmethodID showInterstitial = pEnv->GetStaticMethodID(s_AdServerPlugin, "ShowInterstitial", "(I)V");
                pEnv->CallStaticVoidMethod(s_AdServerPlugin, showInterstitial, language);
            }
            else
            {
                // Show interstitial ads with tags
                jmethodID showInterstitial = pEnv->GetStaticMethodID(s_AdServerPlugin, "ShowInterstitialWithTags", "(ILjava/lang/String;)V");
                jstring         tagsString = pEnv->NewStringUTF(tags.c_str());
                pEnv->CallStaticVoidMethod(s_AdServerPlugin, showInterstitial, language, tagsString);
				pEnv->DeleteLocalRef(tagsString);
            }
#else
            LOG_ERROR("AdManager -- ShowInterstitial with USE_ADS_SERVER 0");
#endif // USE_ADS_SERVER
        }

        void AdManager::LoadFreeCash(helpers::Language language)
        {
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
            
            jmethodID loadFreeCash = pEnv->GetStaticMethodID(s_AdServerPlugin, "LoadFreeCash", "(I)V");
            pEnv->CallStaticVoidMethod(s_AdServerPlugin, loadFreeCash, language);
#else
            LOG_ERROR("AdManager -- LoadFreeCash with USE_ADS_SERVER 0");
#endif // USE_ADS_SERVER
        }

        bool AdManager::IsFreeCashReady()
        {
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
            
            jmethodID isFreeCashReady = pEnv->GetStaticMethodID(s_AdServerPlugin, "IsFreeCashReady", "()Z");
            return pEnv->CallStaticBooleanMethod(s_AdServerPlugin, isFreeCashReady);
#else
            LOG_ERROR("AdManager -- IsFreeCashReady with USE_ADS_SERVER 0");
            return false;
#endif // USE_ADS_SERVER
        }


        void AdManager::ShowFreeCash(helpers::Language language)
        {
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
            
            jmethodID showFreeCash = pEnv->GetStaticMethodID(s_AdServerPlugin, "ShowFreeCash", "(I)V");
            pEnv->CallStaticVoidMethod(s_AdServerPlugin, showFreeCash, language);
#else
            LOG_ERROR("AdManager -- ShowFreeCash with USE_ADS_SERVER 0");            
#endif // USE_ADS_SERVER
        }

        void AdManager::MarkPAU(bool isPau)
        {
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);
            
            jmethodID showFreeCash = pEnv->GetStaticMethodID(s_AdServerPlugin, "SetIsPAU", "(Z)V");
            pEnv->CallStaticVoidMethod(s_AdServerPlugin, showFreeCash, isPau);
#else
            LOG_ERROR("AdManager -- MarkPAU with USE_ADS_SERVER 0");            
#endif // USE_ADS_SERVER
        }

		//idk why this isn't in the InGameBrowser.. but o well...
		std::string AdManager::ResponseForUrlRequest(const std::string& i_url)
		{
#if USE_ADS_SERVER
            CheckAdClass();

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

			jstring url = pEnv->NewStringUTF(i_url.c_str());
            
            jmethodID UrlResponse = pEnv->GetStaticMethodID(s_AdServerPlugin, "AdsHttpResponseToUrl", "(Ljava/lang/String;)Ljava/lang/String;");

            ScopeStringChars response_str(pEnv, (jstring)pEnv->CallStaticObjectMethod(s_AdServerPlugin, UrlResponse, url));
			
			pEnv->DeleteLocalRef(url);

			return response_str.Get();
#else
            LOG_ERROR("AdManager -- MarkPAU with USE_ADS_SERVER 0");            
#endif // USE_ADS_SERVER
		}

        void AdManager::CheckAdClass()
        {
            if(s_AdServerPlugin == NULL)
			{
				s_AdServerPlugin = acp_utils::api::PackageUtils::GetClass("/PackageUtils/AdServerPlugin");
			}
        }

    } // namespace modules
} // namespace acp_utils