#include "config_Android.h"
#include "native_hid_controllers.h"

#include "../package_utils.h"

/**********************************************JNI Part*************************************************/

extern "C" 
{
	JNIEXPORT void JNICALL JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeControllerConnected)(JNIEnv*  env, jobject thiz, jstring ControllerName)
	{
		acp_utils::ScopeStringChars cn(env, ControllerName);
		acp_utils::modules::HidController::s_ControllerName = std::string (cn.Get());
		acp_utils::modules::HidController::OnControllerStateChanged(true);
	}
	
	JNIEXPORT void JNICALL JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeControllerDisconnected)(JNIEnv*  env, jobject thiz)
	{
		acp_utils::modules::HidController::s_ControllerName = std::string("");
		acp_utils::modules::HidController::OnControllerStateChanged(false);
	}
	
	JNIEXPORT void JNICALL JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeHandleInputEvents)(JNIEnv*  env, jobject thiz, jint InternalValue, jdouble Value)
	{
		acp_utils::modules::HidController::DispatchEventToCallback(InternalValue, Value);
	}
}




jmethodID acp_utils::modules::HidController::s_RegisterListener = 0;
jmethodID acp_utils::modules::HidController::s_UnRegisterListner = 0;


namespace acp_utils
{
	namespace modules
	{

		void HidController::SetJniVars()
		{

			if(s_RegisterListener == 0 && s_UnRegisterListner == 0)
			{
				JNIEnv* env = NULL;	
				ScopeGetEnv sta(env);
				
				
				jclass controllerClass = acp_utils::api::PackageUtils::GetClass("/GLUtils/controller/NativeBridgeHIDControllers");
				s_RegisterListener = env->GetStaticMethodID (controllerClass, "NativeListenerRegistered", "(I)V");
				s_UnRegisterListner = env->GetStaticMethodID (controllerClass, "NativeListenerUnRegistered", "()V");
			}
		}

		void HidController::RegisterListener(int freq)
		{
			SetJniVars();

			JNIEnv* env = NULL;	
			ScopeGetEnv sta(env);
			
			env->CallStaticVoidMethod(acp_utils::api::PackageUtils::GetClass("/GLUtils/controller/NativeBridgeHIDControllers"), s_RegisterListener, (jint)freq);
		}

		void HidController::UnregisterListener()
		{
			SetJniVars();

			JNIEnv* env = NULL;	
			ScopeGetEnv sta(env);
			
			env->CallStaticVoidMethod(acp_utils::api::PackageUtils::GetClass("/GLUtils/controller/NativeBridgeHIDControllers"), s_UnRegisterListner);
		}

		/*******************************************************************************************************/

			
			
		/********************************************Internal Helpers*******************************************/
		std::string								HidController::s_ControllerName = "";
		ControllerConnected						HidController::s_ControllerStateCallback = NULL;
		AppHidEventCallback						HidController::s_pHidEventCallback = NULL;

		void HidController::OnControllerStateChanged(bool StateConnected)
		{
			if(s_ControllerStateCallback != NULL)
			{
				s_ControllerStateCallback(StateConnected);
			}
		}

		void HidController::DispatchEventToCallback(int eventType, double Value)
		{
			if(s_pHidEventCallback != NULL)
			{
				s_pHidEventCallback((ControllerEvents)eventType, Value);
			}
		}
		/*******************************************************************************************************/

		/**********************************************Public API***********************************************/
		void HidController::RegisterStateCallback(ControllerConnected callback)
		{
			if( s_ControllerStateCallback == NULL)
			{
				s_ControllerStateCallback = callback;
				RegisterListener(5);
			}
			else
			{
				//nop
			}
		}

		void HidController::UnRegisterStateCallback()
		{
			if(s_ControllerStateCallback != NULL)
			{
				s_ControllerStateCallback = NULL;
				UnregisterListener();
			}
			else
			{
				//nop
			}
		}

		void HidController::RegisterEventCallback(AppHidEventCallback callback)
		{
			if(s_pHidEventCallback == NULL)
			{
				s_pHidEventCallback = callback;
			}
			else
			{
				//nop
			}
		}

		void HidController::UnRegisterEventCallback()
		{
			s_pHidEventCallback = NULL;//if it was already null, no harm done 
		}

		std::string	HidController::GetControllerName()
		{
			return s_ControllerName;
		}

	}//namespace modules
}//namespace acp_utils

/*******************************************************************************************************/


