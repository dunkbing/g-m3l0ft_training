#include "config_Android.h"

#if SIMPLIFIED_PN

#include "simplified_pn.h"
#include "jni_bridge_pn.h"
#include "ABundle.h"
#include "..\ScopeGetEnv.h"
#include "..\package_utils.h"
#include "..\_internal\logger.h"


#include <jni.h>
#include <sstream>



///////////////////////////////////////////////////////////////////////////////////
// SimplifiedPN Interface
///////////////////////////////////////////////////////////////////////////////////
jmethodID	acp_utils::modules::SimplifiedPN::s_GetDeviceToken			= 0;
jmethodID	acp_utils::modules::SimplifiedPN::s_ShowAppDetailsSettings	= 0;
jmethodID 	acp_utils::modules::SimplifiedPN::s_SetEnable				= 0;
jmethodID 	acp_utils::modules::SimplifiedPN::s_IsEnabled					= 0;
jmethodID 	acp_utils::modules::SimplifiedPN::s_IsAppLaunchedFromPN		= 0;
jmethodID 	acp_utils::modules::SimplifiedPN::s_SendMessage				= 0;
jmethodID 	acp_utils::modules::SimplifiedPN::s_DeleteMessageGroup		= 0;
jclass 		acp_utils::modules::SimplifiedPN::s_ClassSimplifiedPn		= 0;

acp_utils::modules::AppReceiverCallback			acp_utils::modules::SimplifiedPN::s_pCallbackReceiver = 0;
void*											acp_utils::modules::SimplifiedPN::s_pCaller;


namespace acp_utils
{
	namespace modules
	{

		void SimplifiedPN::SetJniVars()
		{
			if(s_ClassSimplifiedPn == 0)
			{
				JNIEnv* mEnv = NULL;	
				acp_utils::ScopeGetEnv sta(mEnv);

				s_ClassSimplifiedPn 		= acp_utils::api::PackageUtils::GetClass("/PushNotification/SimplifiedAndroidUtils");
				
				s_GetDeviceToken			= mEnv->GetStaticMethodID (s_ClassSimplifiedPn, "GetDeviceToken",			"(I)I");
				s_ShowAppDetailsSettings	= mEnv->GetStaticMethodID (s_ClassSimplifiedPn, "ShowAppDetailsSettings",	"()V");
				s_SetEnable				= mEnv->GetStaticMethodID (s_ClassSimplifiedPn, "SetEnable", 				"(Z)V");
				s_IsEnabled				= mEnv->GetStaticMethodID (s_ClassSimplifiedPn, "IsEnable", 				"()Z");
				s_IsAppLaunchedFromPN	= mEnv->GetStaticMethodID (s_ClassSimplifiedPn, "IsAppLaunchedFromPN",		"()Ljava/lang/String;");
				s_SendMessage			= mEnv->GetStaticMethodID (s_ClassSimplifiedPn, "SendMessage", 			"(Landroid/os/Bundle;Ljava/lang/String;I)I");
				s_DeleteMessageGroup		= mEnv->GetStaticMethodID (s_ClassSimplifiedPn, "DeleteMessageGroup", 		"(I)I");
			}
		}

		int SimplifiedPN::GetDeviceToken(const acp_utils::helpers::NotificationTransportType& transport, AppReceiverCallback callbackReceiver, void* caller)
		{
			if(callbackReceiver == NULL)
			{
				LOG_ERROR("Trying to get Push Notification Token without supplying a callback to receive it. The token will not be retrieved!");
				return 1;
			}
			if(caller == NULL)
			{
				LOG_INFO("No caller set as parameter for PN callback. Is this intended?");
			}

			SetJniVars();

			s_pCallbackReceiver = callbackReceiver;
			s_pCaller = caller;

			JNIEnv* mEnv = NULL; acp_utils::ScopeGetEnv st(mEnv);
			return mEnv->CallStaticIntMethod(s_ClassSimplifiedPn, s_GetDeviceToken, (int)(transport));
		}

		void SimplifiedPN::ShowAppDetailsSettings()
		{
			SetJniVars();

			JNIEnv* mEnv = NULL; acp_utils::ScopeGetEnv st(mEnv);
			mEnv->CallStaticVoidMethod(s_ClassSimplifiedPn, s_ShowAppDetailsSettings);
		}

		void SimplifiedPN::AllowOnlineNotifications(const bool& option)
		{
			SetJniVars();

			JNIEnv* mEnv = NULL; acp_utils::ScopeGetEnv st(mEnv);
			mEnv->CallStaticVoidMethod(s_ClassSimplifiedPn, s_SetEnable, option);
		}

		bool SimplifiedPN::AreOnlineNotificationsEnabled()
		{
			SetJniVars();

			JNIEnv* mEnv = NULL; 
			acp_utils::ScopeGetEnv st(mEnv);

			return mEnv->CallStaticBooleanMethod(s_ClassSimplifiedPn, s_IsEnabled);
		}

		std::string SimplifiedPN::IsAppLaunchedFromPN()
		{
			SetJniVars();

			JNIEnv* mEnv = NULL; acp_utils::ScopeGetEnv st(mEnv);
			jstring string = (jstring)mEnv->CallStaticObjectMethod(s_ClassSimplifiedPn, s_IsAppLaunchedFromPN);
			
			std::string ret;	jboolean isCopy;
			const char* result = mEnv->GetStringUTFChars(string, &isCopy);

			if(isCopy == JNI_TRUE)
			{
				ret = result;
				mEnv->ReleaseStringUTFChars(string, result);
			}
			return ret;
		}

		int SimplifiedPN::SendMessage(std::map<std::string, std::string>& messageData, time_t targetTime, const int& groupType)
		{
			if (targetTime <= 0)
			{
				targetTime = 1;
			}

			/* Get current time in seconds */
			time_t now;	time(&now);
			
			/* Convert it to the structure tm for getting formatted date*/
			struct tm currentTime_tm;	localtime_r(&now, &currentTime_tm);			messageData ["creation_time"] = asctime(&currentTime_tm);
			struct tm targetTime_tm;	localtime_r(&targetTime, &targetTime_tm);	messageData ["schedule_time"] = asctime(&targetTime_tm);
			
			/* Get seconds difference for given dates */
			int targetSeconds = targetTime - now;	


			SetJniVars();

			JNIEnv* mEnv = NULL; acp_utils::ScopeGetEnv st(mEnv);

			jobject objBundle = ABundle_New();

			for (std::map<std::string, std::string>::const_iterator i = messageData.begin(); i != messageData.end(); ++i)
			{
				ABundle_PutString((i->first).c_str(), (i->second).c_str(), objBundle);
			}
			
			std::stringstream ss;//create a stringstream
			ss << targetSeconds;//add number to the stream
			std::string stdDelay = ss.str();
			
			jstring sdelay = mEnv->NewStringUTF(stdDelay.c_str());

			jint jgroupType = groupType;
			int result = mEnv->CallStaticIntMethod(s_ClassSimplifiedPn, s_SendMessage, objBundle, sdelay, jgroupType);
			mEnv->DeleteLocalRef(sdelay);
			return result;
		}

		int SimplifiedPN::SendMessage(acp_utils::helpers::LocalPn& i_pn)
		{
			return SendMessage(i_pn.pn_data, i_pn.pn_schedule, i_pn.pn_group);
		}

		int SimplifiedPN::DeleteMessageGroup(const int& groupType)
		{
			SetJniVars();

			JNIEnv* mEnv = NULL; acp_utils::ScopeGetEnv st(mEnv);
			jint jgroupType = groupType;
			int result = mEnv->CallStaticIntMethod(s_ClassSimplifiedPn, s_DeleteMessageGroup, jgroupType);
			return result;
		}

		void SimplifiedPN::SendPnCallback(const std::string&  i_data)
		{
			if(s_pCallbackReceiver != NULL)
			{
				s_pCallbackReceiver(i_data, s_pCaller);
			}
			else
			{
				LOG_ERROR("Received PN data without any callback registered. Payload will not be passed to the game. Check your register callback function to try and fix this!");
			}
		}
	}
}

#endif //#if SIMPLIFIED_PN
