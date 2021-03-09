#ifndef _ACP_AD_MANAGER_H_
#define _ACP_AD_MANAGER_H_

/* Local includes */
#include "../helpers.h"

/* Library includes */

/* Standard includes */
#include <jni.h>
#include <string>

namespace acp_utils 
{
    namespace modules 
    {
        /**
         * Class communicating with the AdServerPlugin in the Java code.
         * Responsable with setting ad server configurations
         * and requesting certain actions.
         */
        class AdManager 
        {
            /**
             * Enum defining banner size
             */
            enum BannerSize : signed int
            {
                BANNER_SIZE_NONE = -3,
                BANNER_LARGE     = -1,
                BANNER_SMALL     = -2                
            };

            /**
             * Enum defining banner position
             * Equivalent with the values in Java 
             * Note: Keep them consistent with those.
             */
            enum BannerPosition 
            {
                TOP_CENTER    = 0,
                TOP_LEFT,
                TOP_RIGHT,
                BOTTOM_CENTER,
                BOTTOM_LEFT,
                BOTTOM_RIGHT               
            };

        public:
            /**
             * Set banner properties
             * @param size BannerSize for the dimension of the banner.
             * @param pos  BannerPosition for the position of the banner.
             */
            static void SetBannerProperties(BannerSize size, BannerPosition pos);

            /**
             * Displays the banner in the given language.
             * @param  language Language id.
             * @return True in case of success, false otherwise.
             */
            static bool ShowBanner(helpers::Language language);

            /**
             * Hide the banner shown.
             * Does nothing if banner is not shown.
             * @return True if the banner was on, and was hidden... false otherwise.
             */
            static bool HideBanner();

            /**
             * Show the interstitial ad in the given language.
             * Optional: Pass tags and it will show the ad with the given tags
             * @param  language Language id.
             * @param  tags     String containting the tags for interstitial ads.
             */
            static void ShowInterstitial(helpers::Language language, std::string tags = "");

            /**
             * Show the free cash banner in the given language.
             * @param  language Language id.
             */
            static void LoadFreeCash(helpers::Language language);

            /**
             * Check if free cash banner is ready.
             */
            static bool IsFreeCashReady();

            /**
             * Show Free Cash Banner
             */
            static void ShowFreeCash(helpers::Language language);

            /**
             * Notify the ad server the status of
             * the current user (paying/non-paying)
             * For paying users, ads should not be displayed.
             *
             * @param isPau True if it's a paying user, false otherwise.
             */
            static void MarkPAU(bool isPau);


			static std::string ResponseForUrlRequest(const std::string&);

        private:
            /**
             * Lazy caching for adServerPlugin jclass reference.
             */
            static void					CheckAdClass();

            static jclass				s_AdServerPlugin; ///< AdServerClass
            static BannerSize			s_bannerSize; ///< Keeps the banner size
            static BannerPosition		s_bannerPosition; ///< Keeps the banner position
        };

    } // namespace modules

} // namespace acp_utils

#endif // _ACP_AD_MANAGER_H_