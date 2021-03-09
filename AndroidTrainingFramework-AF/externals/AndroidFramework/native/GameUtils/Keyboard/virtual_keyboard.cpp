/* Local includes */
#include "../package_utils.h"

#include "../_internal/logger.h"
#include "../ScopeStringChars.h"
#include "../ScopeGetEnv.h"

#include "virtual_keyboard.h"

/* Library includes */

/* Standard includes */

namespace acp_utils
{
	namespace modules
	{

		KeyboardOnTextChanged_CB VirtualKeyboard::s_vKeyboardCB = 0x0;

		// Virtual keyboard methods
		void VirtualKeyboard::ShowKeyboard(const std::string& initialText, KeyboardOnTextChanged_CB keyboardCB)
		{
			KeyboardOptions options;
			ShowKeyboard(initialText, keyboardCB, options);
		}

		// Virtual keyboard methods
		void VirtualKeyboard::ShowKeyboard(const std::string& initialText, KeyboardOnTextChanged_CB keyboardCB, const KeyboardOptions& options)
		{
		#if USE_VIRTUAL_KEYBOARD
			s_vKeyboardCB = keyboardCB;

			if(s_vKeyboardCB != NULL)
			{
				JNIEnv* pEnv;
				ScopeGetEnv sta(pEnv);

				if(pEnv != NULL)
				{
					jstring string = pEnv->NewStringUTF(initialText.c_str());

					jmethodID showKeyboard = pEnv->GetStaticMethodID(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), "ShowKeyboard", "(Ljava/lang/String;III)V");
					pEnv->CallStaticVoidMethod(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), showKeyboard, string, static_cast<jint>(options.keyboardType), static_cast<jint>(options.enterKeyFunction), static_cast<jint>(options.fullscreenOption));

					if(string != NULL)
					{
						pEnv->DeleteLocalRef(string);
					}
				}
				else
				{
					LOG_ERROR("VirtualKeyboard::ShowKeyboard -- Java Environment invalid.");
				}
			}
			else
			{
				LOG_ERROR("VirtualKeyboard::ShowKeyboard -- Keyboard callback is invalid.");
			}
		#else
			LOG_ERROR("ShowKeyboard -- Call successful but USE_VIRTUAL_KEYBOARD is not enabled.");
		#endif // USE_VIRTUAL_KEYBOARD
		}

		void VirtualKeyboard::HideKeyboard()
		{
		#if USE_VIRTUAL_KEYBOARD
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID hideKeyboard = pEnv->GetStaticMethodID(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), "HideKeyboard", "()V");
			pEnv->CallStaticVoidMethod(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), hideKeyboard);
		#else
			LOG_ERROR("HideKeyboard -- Call successful but USE_VIRTUAL_KEYBOARD is not enabled.");
		#endif // USE_VIRTUAL_KEYBOARD
		}

		bool VirtualKeyboard::IsKeyboardVisible()
		{
		#if USE_VIRTUAL_KEYBOARD
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID isKeyboardVisible = pEnv->GetStaticMethodID(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), "IsKeyboardVisible", "()Z");
			return (bool) pEnv->CallStaticBooleanMethod(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), isKeyboardVisible);            
		#else
			LOG_ERROR("IsKeyboardVisible -- Call successful but USE_VIRTUAL_KEYBOARD is not enabled.");
			return false;
		#endif // USE_VIRTUAL_KEYBOARD
		}

		std::string VirtualKeyboard::GetKeyboardText()
		{
		#if USE_VIRTUAL_KEYBOARD
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID getVKeyboardText = pEnv->GetStaticMethodID(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), "GetVKeyboardText", "()Ljava/lang/String;");           
			ScopeStringChars text(pEnv, (jstring)pEnv->CallStaticObjectMethod(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), getVKeyboardText));
			return text.Get();
		#else
			LOG_ERROR("GetKeyboardText -- Call successful but USE_VIRTUAL_KEYBOARD is not enabled.");
			return "";
		#endif // USE_VIRTUAL_KEYBOARD
		}

		void	VirtualKeyboard::SetKeyboardText(const std::string& i_text)
		{
		#if USE_VIRTUAL_KEYBOARD

			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jstring textToSet = pEnv->NewStringUTF(i_text.c_str());

			jmethodID setText = pEnv->GetStaticMethodID(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), "SetVKeyboardText", "(Ljava/lang/String;)V");
			pEnv->CallStaticVoidMethod(acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils"), setText, textToSet);            

			pEnv->DeleteLocalRef(textToSet);

		#else
				LOG_ERROR("GetKeyboardText -- Call successful but USE_VIRTUAL_KEYBOARD is not enabled.");
		#endif //USE_VIRTUAL_KEYBOARD
		}

	}//namespace modules
}//namespace acp_utils