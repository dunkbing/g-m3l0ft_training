#ifndef _ACP_IN_GAME_BROWSER_H_
#define _ACP_IN_GAME_BROWSER_H_

/* Local includes */
#include "../helpers.h"

/* Library includes */

/* Standard includes */
#include <map>
#include <string>

#include <jni.h>


namespace acp_utils 
{
    namespace modules
    {
        /**
         * InGameBrowser class is a collection of static functions,
         * all related to the browser features which require a 
         * WebView instance created in java.
         */
        class InGameBrowser
        {
        public:
            /**
             * Show the forum in the given language
             *
             * @param languageIdx The language to be used.
             */
            static void ShowForum(helpers::Language languageIdx);

            /**
             * Show the customer care using the given parameters.
             *
             * @param languageIdx The language to be used.
             * @param accounts    A map containing SNS credentials and their uid.
             * @param banned      True if the player was banned, false otherwise.
             * @param banType     Type of ban, is eNoBan if user is none available.
             */
            static void ShowCustomerCare(helpers::Language languageIdx, const std::map<helpers::AccountType, std::string>& accounts, bool banned = false, helpers::BanType banType = helpers::eNoBan);

            /**
             * Shows the terms of use in the given language.
             *
             * @param languageIdx The language to be used.
             */
            static void ShowTermsOfUse(helpers::Language languageIdx);

            /**
             * Shows the privacy policy in the given language.
             *
             * @param languageIdx The language to be used.
             */
            static void ShowPrivacyPolicy(helpers::Language languageIdx);
            
			/**
			 * Shows the News
             *
             * @param languageIdx The language to be used.
			 */
			static void ShowNews(helpers::Language languageIdx, const std::map<helpers::AccountType, std::string>& accounts);
						 
			/**
			 * Refresh the Unread News Number
             *
             * @param accounts    A map containing SNS credentials and their uid.
			 */
			static void RefreshUnreadNewsNumber(const std::map<helpers::AccountType, std::string>& accounts);
            
			/**
			 * Get the Number of Unread News
			 */
			static int  GetUnreadNewsNumber();
			
			/**
			 * Redirect to the new version screen link
			 */
			static void RedirectNewVersionScreen();
			
			/**
			 *  Rate this application redirect
			 */
			static void RedirectRateThisApp();

            /**
             * Launch the browser with the given url.
             */
            static void LaunchBrowser(const char* url);

        private:
			static void SetBrowserClass();
			static jclass s_BrowserClass;
        }; // class InGameBrowser
    } // namespace modules
} // namespace acp_utils



#endif // _ACP_IN_GAME_BROWSER_H_