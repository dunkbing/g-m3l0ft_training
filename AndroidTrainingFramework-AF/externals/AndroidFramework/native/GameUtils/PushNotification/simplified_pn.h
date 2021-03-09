#ifndef __JNI_SIMPLIFIED_NOTIFICATION_H__
#define __JNI_SIMPLIFIED_NOTIFICATION_H__

#include <string>
#include <ctime>
#include <map>
#include <jni.h>


#include "../helpers.h"

#define		PN_INFO_SUBJECT					"subject"
#define		PN_INFO_BODY					"body"
#define		PN_INFO_TYPE					"type"
#define		PN_INFO_GROUP_ID				"lID"
#define		PN_INFO_CREATION_DATE			"creation_time"
#define		PN_INFO_SCHEDULE				"schedule_time"
#define		PN_INFO_LAUNCH_GAME				"pn_launch_game"
#define		PN_INFO_GOTO_MP					"pn_goto_multiplayer"


namespace acp_utils
{
	namespace modules
	{
		typedef void (*AppReceiverCallback) (const std::string& response, void* caller);

		class SimplifiedPN
		{
		public:
			/**
			 * Get Device Token will retrieve the Online Notification token provided by the 3rd party server (Google or Amazon)
			 * @param transport: The type of transport, can be GCM (for Google Play) or ADM (for Amazon PNs)
			 * @param callbackReceiver: The callback that will receive the token
			 * @param caller: the caller which requests the token
			 * @return: 1 means invalid transport (NotificationTransportType is invalid), 0 means success and a callback will be received
			 */	
			static int						GetDeviceToken(const acp_utils::helpers::NotificationTransportType& transport, 
														   AppReceiverCallback callbackReceiver, 
														   void* caller);

			/**
			 * Method that opens the Application Details Setting page. 
			 * av TODO: It makes more sense to have this function in package_utils, rather then in the PN module!
			 */
			static void						ShowAppDetailsSettings();

			/**
			 * SetEnable will enable or disable the device to receive Online Push Notifications. The endpoint will not be disabled, just the code that displays the PN.
			 * @param option: true to enable Online PNs. false to disable Online PNs.
			 */	
			static void 					AllowOnlineNotifications(const bool& option);

			/**
			 * IsEnable will determine if the application has Online PNs enabled or not. This is only reflect calls to SetEnable. There is no way to determine if the user disabled PNs from the Application Settings.
			 */	
			static bool 					AreOnlineNotificationsEnabled();

			/**
			* IsAppLaunchedFromPN returns an serialized Json string containing PN data in case of the application was launched/resumed from a PN. Return empty string in case of the application was launched without tapping a PN.
			* 
			*/
			static std::string 				IsAppLaunchedFromPN();

			/**
			* SendMessage will put a Local PN alert in the system, and assigned it to a group. 
			* @param messageData contains PN data by using std::map<std::string, std::string>
			* @param targetTime is the target date for delivering messages.
			* @param groupType allows setting a group id in order to create multiple PN groups.
			* @return integer value for result, Operation Successful (0)
			*/
			static int						SendMessage(std::map<std::string, std::string>& messageData, time_t targetTime, const int& groupType);

			/**
			* SendMessage will put a Local PN alert in the system, and assigned it to a group. 
			* @param LocalPn: Alternative for SendMessage with parameters. See above
			* @return integer value for result, Operation Successful (0)
			*/
			static int						SendMessage(acp_utils::helpers::LocalPn& i_pn);

			/**
			* DeleteMessageGroup will delete all local PNs registered under a certain message group.
			* @param groupType is the group identifier to be deleted.
			* @return integer value for result, Operation Successful (0)
			*/
			static int						DeleteMessageGroup(const int& groupType);


		private:
			static AppReceiverCallback		s_pCallbackReceiver;
			static void						*s_pCaller;

			static jmethodID				s_GetDeviceToken;
			static jmethodID				s_ShowAppDetailsSettings;
			static jmethodID				s_SetEnable;
			static jmethodID				s_IsEnabled;
			static jmethodID				s_IsAppLaunchedFromPN;
			static jmethodID				s_SendMessage;
			static jmethodID				s_DeleteMessageGroup;
			static jclass 					s_ClassSimplifiedPn;

			static void						SetJniVars();

		public:
			//public, as it is called from Java
			static void						SendPnCallback(const std::string&);

		};//class SimplifiedPN

	}; //namespace modules

};//namespace acp_utils

#endif //PN_INFO_SUBJECT
