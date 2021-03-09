#ifndef __NATIVE_HID_CONTROLLERS_H__
#define __NATIVE_HID_CONTROLLERS_H__

#include <jni.h>

#include <list>
#include <string>


namespace acp_utils
{
	namespace modules
	{
		enum ControllerEvents
		{
			UNDEFINED			=	0,
		
			LeftTrigger			=	1,			//will take continuous values between (0, 1) 
			RightTrigger		=	2,			//will take continuous values between (0, 1)
			
			LeftStickX			=	3,			//will take continuous values between (-1, 1)
			LeftStickY			=	4,			//will take continuous values between (-1, 1)
			
			RightStickX			=	5,			//will take continuous values between (-1, 1)
			RightStickY			=	6,			//will take continuous values between (-1, 1)
				
			DpadEventUp			=	7,			//will take fixed values, 1 when pressed, 0 when released
			DpadEventDown		=	8,			//will take fixed values, 1 when pressed, 0 when released
			DpadEventLeft		=	9,			//will take fixed values, 1 when pressed, 0 when released
			DpadEventRight		=	10,			//will take fixed values, 1 when pressed, 0 when released
			
			LeftBumper			=	11,			//will take fixed values, 1 when pressed, 0 when released
			RightBumper			=	12,			//will take fixed values, 1 when pressed, 0 when released
			
			ButtonY				=	13,			//will take fixed values, 1 when pressed, 0 when released
			ButtonA				=	14,			//will take fixed values, 1 when pressed, 0 when released
			ButtonX				=	15,			//will take fixed values, 1 when pressed, 0 when released
			ButtonB				=	16,			//will take fixed values, 1 when pressed, 0 when released
			
			ButtonStart			=	17,			//will take fixed values, 1 when pressed, 0 when released
			ButtonSelect		=	18,			//will take fixed values, 1 when pressed, 0 when released
			ButtonBack			=	19,			//will take fixed values, 1 when pressed, 0 when released
			
			LeftStickButton 	= 	20,			//will take fixed values, 1 when pressed, 0 when released
			RightStickButton 	= 	21,			//will take fixed values, 1 when pressed, 0 when released
		};

		typedef void (*AppHidEventCallback) (ControllerEvents eventType, double Value);
		typedef void (*ControllerConnected) (bool isConnected);

		class HidController
		{
		
			//JNI part:
		private:
			static void SetJniVars();

		
			
			static jmethodID s_RegisterListener;
			static jmethodID s_UnRegisterListner;
			static void RegisterListener(int freq);
			static void UnregisterListener();
			
		
			
			//end JNI part
		
		
		//Internal Helper functions part:
		private:
			static	ControllerConnected					s_ControllerStateCallback;
			static 	AppHidEventCallback					s_pHidEventCallback;
			
		public://making this public so tht the JNI functions can access it
			static	std::string							s_ControllerName;	
			
			/**
			*	OnControllerStateChanged will be called from Java via NativeControllerConnected/NativeControllerDisconnected
			*/
			static	void								OnControllerStateChanged(bool StateConnected);
			static 	void								DispatchEventToCallback(int eventType, double Value);	
			
			
		//Public API	
		public:	
			static	void								RegisterStateCallback(ControllerConnected callback);
			static	void								UnRegisterStateCallback();
			static	void 								RegisterEventCallback(AppHidEventCallback callback);
			static 	void								UnRegisterEventCallback();
			static  std::string							GetControllerName();
		};

	} // namespace modules
}; // namespace acp_utils

#endif //__NATIVE_HID_CONTROLLERS_H__
