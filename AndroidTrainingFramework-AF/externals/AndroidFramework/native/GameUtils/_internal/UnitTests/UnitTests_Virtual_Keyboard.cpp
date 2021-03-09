#include <android/log.h>

#include "../../Keyboard/virtual_keyboard.h"
#include "../../package_utils.h"

#if ACP_UT
#include "AndroidUnitTestLogPrinter.h"
#include "UnitTests_DeviceConfig.h"
#include "gtest/gtest.h"
#include "../logger.h"

#if USE_VIRTUAL_KEYBOARD

class VirtualKeyboardAPI_UT : public ::testing::Test
{
public:
	static void VKeyboardChanged(const std::string& currentText)
	{
		LOG_INFO("Virtual Keyboard callback received with %s", currentText.c_str());
	}

protected:
	VirtualKeyboardAPI_UT()
	{
		// TODO (maybe?)
	}
	
	virtual ~VirtualKeyboardAPI_UT()
	{
		// TODO (maybe?)
	}
	
	virtual void SetUp()
	{
		acp_utils::modules::VirtualKeyboard::ShowKeyboard(std::string(UT_VKEYBOARD_TEXT), VKeyboardChanged);
		sleep(2); //< Opening the keyboard is async, wait for it to open...
	}
	
	virtual void TearDown()
	{
		acp_utils::modules::VirtualKeyboard::HideKeyboard();
	}
};

TEST_F(VirtualKeyboardAPI_UT, BaseAPI)
{
	JNIEnv* pEnv = NULL;
	acp_utils::ScopeGetEnv	se(pEnv);
	jclass AU = acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils");
	jmethodID injectKeysMethodID = pEnv->GetStaticMethodID(AU, "InjectKeys", "(I)V");
    

	//checking if the keyboard is visible and has the UT_VK_TEXT
	EXPECT_TRUE(acp_utils::modules::VirtualKeyboard::IsKeyboardVisible());
	EXPECT_STREQ(UT_VKEYBOARD_TEXT, acp_utils::modules::VirtualKeyboard::GetKeyboardText().c_str());

	//setting (async) a new keyboard text, without closing the keyboard
	acp_utils::modules::VirtualKeyboard::SetKeyboardText(UT_VKEYBOARD_TEXT_2);
	sleep(2);
	
	//checking if the new text was properly set
	EXPECT_STREQ(UT_VKEYBOARD_TEXT_2, acp_utils::modules::VirtualKeyboard::GetKeyboardText().c_str());

	//closing the keyboard by back key
	pEnv->CallStaticVoidMethod(AU, injectKeysMethodID, 4);//4 = keycode_back
	sleep(2);
	
	//keyboard should be closed by the back key
	EXPECT_FALSE(acp_utils::modules::VirtualKeyboard::IsKeyboardVisible());
	
	//trying to close the keyboard by api call. (Old back, pressing back and calling hide would crash an app)
	acp_utils::modules::VirtualKeyboard::HideKeyboard();
	sleep(2);
	
	//keyboard should be closed by the back key
	EXPECT_FALSE(acp_utils::modules::VirtualKeyboard::IsKeyboardVisible());
	
	//showing the keyboard back with the initial text
	acp_utils::modules::VirtualKeyboard::ShowKeyboard(std::string(UT_VKEYBOARD_TEXT), VKeyboardChanged);
	sleep(2);
	EXPECT_STREQ(UT_VKEYBOARD_TEXT, acp_utils::modules::VirtualKeyboard::GetKeyboardText().c_str());

	//now closing the keyboard by keycode back
	pEnv->CallStaticVoidMethod(AU, injectKeysMethodID, 4);//4 = keycode_back
	sleep(2);

	//showing an empty keyboard, and adding qr to it. 
	acp_utils::modules::VirtualKeyboard::ShowKeyboard(std::string(""), VKeyboardChanged);
	sleep(2);

	pEnv->CallStaticVoidMethod(AU, injectKeysMethodID, 45);//45 = keycode_Q
	sleep(2);
	pEnv->CallStaticVoidMethod(AU, injectKeysMethodID, 46);//46 = keycode_R
	sleep(2);

	//testing if the keyboard text is qr
	EXPECT_STREQ("qr", acp_utils::modules::VirtualKeyboard::GetKeyboardText().c_str());

	//closing the keyboard by keycode enter
	pEnv->CallStaticVoidMethod(AU, injectKeysMethodID, 66 );//66 = keycode_enter
	sleep(2);

	//testing if the keyboard was closed by enter
	EXPECT_FALSE(acp_utils::modules::VirtualKeyboard::IsKeyboardVisible());
}

#endif // USE_VIRTUAL_KEYBOARD

#endif // ACP_UT