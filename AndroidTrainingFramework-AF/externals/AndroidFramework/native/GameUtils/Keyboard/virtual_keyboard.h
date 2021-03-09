#ifndef _ACP_VIRTUAL_KEYBOARD_
#define _ACP_VIRTUAL_KEYBOARD_

/* Standard includes */
#include <string>

/**
 * Class for holding handlers for the virtual keyboard opened by the Android OS.
 */
namespace acp_utils
{
	namespace modules
	{

		struct KeyboardOptions
		{
			enum KeyboardType
			{
				//http://developer.android.com/reference/android/text/InputType.html
				TYPE_DATEIME_VARIATION_NORMAL = 0,
				TYPE_CLASS_TEXT,
				TYPE_CLASS_PHONE,
				TYPE_CLASS_DATETIME
			};

			enum EnterFunction
			{
				ACTION_DONE = 0,
				ACTION_NEW_LINE,
			};

			enum FullscreenOption
			{
				NOT_FULLSCREEN = 0,
				FULLSCREEN,
			};

			KeyboardOptions()
			{
				keyboardType = KeyboardOptions::TYPE_CLASS_TEXT;
				enterKeyFunction = KeyboardOptions::ACTION_DONE;
				fullscreenOption = KeyboardOptions::NOT_FULLSCREEN;
			}

			

			KeyboardType			keyboardType;
			EnterFunction			enterKeyFunction;
			FullscreenOption		fullscreenOption;
		};

		/* Callback type definition for keyboard */
		typedef void (*KeyboardOnTextChanged_CB) (const std::string& currentText);

		class VirtualKeyboard
		{
		public:
			/**
			 * Show keyboard method.
			 * @param initialText What will be displayed when the vkeyboard is open
			 * @param keyboardCB  A function ptr that will handle the changes in the vkeyboard.
			 */
			static void ShowKeyboard(const std::string& initialText, KeyboardOnTextChanged_CB keyboardCB);

			/**
			 * Show keyboard method with options.
			 * @param initialText What will be displayed when the vkeyboard is open
			 * @param keyboardCB  A function ptr that will handle the changes in the vkeyboard.
			 * @param options Parameter to change keyboard behavior. 
			 */
			static void ShowKeyboard(const std::string& initialText, KeyboardOnTextChanged_CB keyboardCB, const KeyboardOptions& options);

			/**
			 * Method to close the keyboard.
			 */
			static void HideKeyboard();

			/**
			 * Method for checking if the keyboard is on.
			 */
			static bool IsKeyboardVisible();

			/**
			 * Method for getting the text currently present in the vkeyboard.
			 */
			static std::string GetKeyboardText();
			
			/**
			*	Method for setting the keyboard text. Works only if the keyboard is visible
			*/
			static void	SetKeyboardText(const std::string&);

			/**
			 * Method for setting the callback.
			 */
			static void SetKeyboardCallback(KeyboardOnTextChanged_CB callback);

			/* Callback -- Needed public to be called by JNIBridge */
			static KeyboardOnTextChanged_CB s_vKeyboardCB;
		};

	} //namespace modules
} // namespace acp_utils
#endif // _ACP_VIRTUAL_KEYBOARD_