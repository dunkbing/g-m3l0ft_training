/* Local includes */
#include "InGameBrowser.h"

#include "../package_utils.h"

#include "../_internal/logger.h"

/* Library includes */

/* Standard includes */



namespace acp_utils
{
    namespace modules
    {
		jclass InGameBrowser::s_BrowserClass = NULL;

		void InGameBrowser::SetBrowserClass()
		{
			if(s_BrowserClass == NULL)
			{
				s_BrowserClass = acp_utils::api::PackageUtils::GetClass("/InGameBrowser");
			}
		}


        void InGameBrowser::ShowForum(helpers::Language languageIdx)
        {
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);

			jmethodID showForum = pEnv->GetStaticMethodID(s_BrowserClass, "showForum", "(I)V");
            pEnv->CallStaticVoidMethod(s_BrowserClass, showForum, languageIdx);
        }

        void InGameBrowser::ShowCustomerCare(helpers::Language languageIdx, const std::map<helpers::AccountType, std::string>& accounts, bool banned, helpers::BanType banType)
        {
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);

			jstring anon_id;
			jstring fb_id;
			jstring google_id;
			std::string empty("");

			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator anon = accounts.find(acp_utils::helpers::eAnon);
			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator fb = accounts.find(acp_utils::helpers::eFacebook);
			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator google = accounts.find(acp_utils::helpers::eGoogle);

			

			//no need to release NewStringUTF-> this is java and will be handled by garbage collector after jstring exists scope
			//actually, NewStringUTF gets garbaged collected, only when the method finishes in Java. If the method is initialized from a native created thread, NewStringUTF will leak. Thus adding DeleteLocalRef
			//going further with this -> DeleteLocalRef might cause a JNI exception if called from outside a native method implementation
			//this article explains best: http://endoframe.com/log/2008/01/21/exception-safe-management-of-jni-local-references/
			//thus
			//av TODO: go through all the NewStringUTF and DeleteLocalRef, and replace them with Push/Pop LocalFrame
			if(anon != accounts.end())
			{
				anon_id = pEnv->NewStringUTF(anon->second.c_str());
			}
			else
			{
				anon_id = pEnv->NewStringUTF(empty.c_str());
			}
			
			if(fb != accounts.end())
			{
				fb_id = pEnv->NewStringUTF(fb->second.c_str());//no need to release string UTF-> this is java and will be handled by garbage collector after jstring exists scope
			}
			else
			{
				fb_id = pEnv->NewStringUTF(empty.c_str());
			}

			if(google != accounts.end())
			{
				google_id = pEnv->NewStringUTF(google->second.c_str());//no need to release string UTF-> this is java and will be handled by garbage collector after jstring exists scope
			}
			else
			{
				google_id = pEnv->NewStringUTF(empty.c_str());
			}
			
			jmethodID showCC = pEnv->GetStaticMethodID(s_BrowserClass, "ShowCustomerCare", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZI)V");
			pEnv->CallStaticVoidMethod(s_BrowserClass, showCC, languageIdx, anon_id, fb_id, google_id, banned, banType);

			pEnv->DeleteLocalRef(anon_id);
			pEnv->DeleteLocalRef(fb_id);
			pEnv->DeleteLocalRef(google_id);
        }

        void InGameBrowser::ShowTermsOfUse(helpers::Language languageIdx)
        {
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);

			jmethodID showTerms = pEnv->GetStaticMethodID(s_BrowserClass, "ShowTermsOfUse", "(I)V");
            pEnv->CallStaticVoidMethod(s_BrowserClass, showTerms, languageIdx);
        }

        void InGameBrowser::ShowPrivacyPolicy(helpers::Language languageIdx)
        {
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);

			jmethodID showPrivacy = pEnv->GetStaticMethodID(s_BrowserClass, "ShowPrivacyPolicy", "(I)V");
            pEnv->CallStaticVoidMethod(s_BrowserClass, showPrivacy, languageIdx);
        }

		void InGameBrowser::ShowNews(helpers::Language languageIdx, const std::map<helpers::AccountType, std::string>& accounts)
        {
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);
			
			jstring anon_id;
			jstring fb_id;
			jstring google_id;
			std::string empty("");

			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator anon = accounts.find(acp_utils::helpers::eAnon);
			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator fb = accounts.find(acp_utils::helpers::eFacebook);
			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator google = accounts.find(acp_utils::helpers::eGoogle);

			if(anon != accounts.end())
			{
				anon_id = pEnv->NewStringUTF(anon->second.c_str());
			}
			else
			{
				anon_id = pEnv->NewStringUTF(empty.c_str());
			}
			
			if(fb != accounts.end())
			{
				fb_id = pEnv->NewStringUTF(fb->second.c_str());//no need to release string UTF-> this is java and will be handled by garbage collector after jstring exists scope
			}
			else
			{
				fb_id = pEnv->NewStringUTF(empty.c_str());
			}

			if(google != accounts.end())
			{
				google_id = pEnv->NewStringUTF(google->second.c_str());//no need to release string UTF-> this is java and will be handled by garbage collector after jstring exists scope
			}
			else
			{
				google_id = pEnv->NewStringUTF(empty.c_str());
			}

			jmethodID showNews = pEnv->GetStaticMethodID(s_BrowserClass, "showNews", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
            pEnv->CallStaticVoidMethod(s_BrowserClass, showNews, languageIdx, anon_id, fb_id, google_id);

			pEnv->DeleteLocalRef(anon_id);
			pEnv->DeleteLocalRef(fb_id);
			pEnv->DeleteLocalRef(google_id);
        }

		void InGameBrowser::RefreshUnreadNewsNumber(const std::map<helpers::AccountType, std::string>& accounts)
        {
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);
			
			jstring anon_id;
			jstring fb_id;
			jstring google_id;
			std::string empty("");

			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator anon = accounts.find(acp_utils::helpers::eAnon);
			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator fb = accounts.find(acp_utils::helpers::eFacebook);
			std::map<acp_utils::helpers::AccountType, std::string>::const_iterator google = accounts.find(acp_utils::helpers::eGoogle);

			if(anon != accounts.end())
			{
				anon_id = pEnv->NewStringUTF(anon->second.c_str());
			}
			else
			{
				anon_id = pEnv->NewStringUTF(empty.c_str());
			}
			
			if(fb != accounts.end())
			{
				fb_id = pEnv->NewStringUTF(fb->second.c_str());//no need to release string UTF-> this is java and will be handled by garbage collector after jstring exists scope
			}
			else
			{
				fb_id = pEnv->NewStringUTF(empty.c_str());
			}

			if(google != accounts.end())
			{
				google_id = pEnv->NewStringUTF(google->second.c_str());//no need to release string UTF-> this is java and will be handled by garbage collector after jstring exists scope
			}
			else
			{
				google_id = pEnv->NewStringUTF(empty.c_str());
			}

			jmethodID refreshUnreadNewsNumber = pEnv->GetStaticMethodID(s_BrowserClass, "refreshUnreadNewsNumber", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
            pEnv->CallStaticVoidMethod(s_BrowserClass, refreshUnreadNewsNumber, anon_id, fb_id, google_id);

			pEnv->DeleteLocalRef(anon_id);
			pEnv->DeleteLocalRef(fb_id);
			pEnv->DeleteLocalRef(google_id);
        }

		int InGameBrowser::GetUnreadNewsNumber()
		{
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);

			jmethodID getUnreadNews = pEnv->GetStaticMethodID(s_BrowserClass, "getUnreadNewsNumber", "()I");
			return pEnv->CallStaticIntMethod(s_BrowserClass, getUnreadNews);
		}
		
		void InGameBrowser::RedirectNewVersionScreen()
		{
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);
			
			jmethodID nvsMethod = pEnv->GetStaticMethodID(s_BrowserClass, "RedirectNewVersionScreen", "()V");
			pEnv->CallStaticVoidMethod(s_BrowserClass, nvsMethod);  
		}
		
		void InGameBrowser::RedirectRateThisApp()
		{
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);
			
			jmethodID rateMethod = pEnv->GetStaticMethodID(s_BrowserClass, "RedirectRateThisApp", "()V");
			pEnv->CallStaticVoidMethod(s_BrowserClass, rateMethod);  
		}

        void InGameBrowser::LaunchBrowser(const char* i_url)
        {
			SetBrowserClass();

			JNIEnv* pEnv = NULL;
			acp_utils::ScopeGetEnv st(pEnv);

			jstring url = pEnv->NewStringUTF(i_url);

			jmethodID showBrowser = pEnv->GetStaticMethodID(s_BrowserClass, "showInGameBrowserWithUrl", "(Ljava/lang/String;)V");
			pEnv->CallStaticVoidMethod(s_BrowserClass, showBrowser, url);  

			pEnv->DeleteLocalRef(url);
        }

    } // namespace modules
} // namespace acp_utils
