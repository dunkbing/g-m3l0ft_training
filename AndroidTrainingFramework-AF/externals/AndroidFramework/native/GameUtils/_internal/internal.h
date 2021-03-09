#ifndef __ACP_INTERNAL_H__
#define __ACP_INTERNAL_H__

#include <android/native_window.h> // requires ndk r5 or newer
#include <jni.h>

#include "../helpers.h"



namespace acp_utils
{
	namespace acp_internal
	{
		/************************Global Variables*********************/

		class Internal
		{

		private: //av need to make this private!
			
            /**
             * Initializes variables that are cached in the native
             * part of the code and do not change on the lifetime of the
             * application.
             * Note: Called only once, at initialization.
             */
			static void InitializeCachedVars();

            /**
             * Refreshes the variables that are cached in the native
             * part of the code and are susceptible to change while
             * the application is interrupted.
             * Note: Called each time onResume is called.
             */
            static void RefreshCachedVars();

			static void CleanCachedVars();

		public:
			
            static void Init();
			
			static void LoadClasses(JNIEnv*);

			//setters from JNI:
			static void SetVm(JavaVM*);
			static void SetWindow(ANativeWindow*, int, int);

			static void SetConnection(const acp_utils::helpers::ConnectionType&);

			static void SetUserLocation(const acp_utils::helpers::UserLocation&);
			
			static void PreGameResume();
			static void PostGameResume();

			static void PreGamePause();
			static void PostGamePause();

            //Battery Info:
            static void SetBatteryInfo(const acp_utils::helpers::BatteryInfo&);

			//Method id caches for functions that need to go back and forth between JNI and Java
			static jmethodID	s_getAssetId;
            static bool         s_bInitialized;
		};

		
		
		/************************End Global Variables*********************/
	};
};

#endif //__ACP_INTERNAL_H__
