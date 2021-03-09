#include "config_Android.h"

#if ACP_UT

#include "../../ScopeGetEnv.h"
#include "../../Controller/native_hid_controllers.h"

#include "UnitTests_DeviceConfig.h"
#include "gtest/gtest.h"
#include "../logger.h"

struct TestEvent
{
	acp_utils::modules::ControllerEvents	evType;
	double									Value;
}test;

void ControllerEventCB (acp_utils::modules::ControllerEvents eventType, double Value)
{
	test.evType = eventType;
	test.Value = Value;
}

void ControllerConnectedCB (bool isConnected)
{
	acp_utils::modules::HidController::RegisterEventCallback(ControllerEventCB);
}

extern "C" 
{
	JNIEXPORT void JNICALL JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeControllerConnected)			(JNIEnv*  env, jobject thiz, jstring ControllerName);
//	JNIEXPORT void JNICALL JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeControllerDisconnected)			(JNIEnv*  env, jobject thiz);
	JNIEXPORT void JNICALL JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeHandleInputEvents)				(JNIEnv*  env, jobject thiz, jint InternalValue, jdouble Value);
}


class ControllerAPI_UT : public ::testing::Test
{
public:


protected:
	ControllerAPI_UT()
	{
	}

	virtual ~ControllerAPI_UT()
	{
	}

	virtual void SetUp()
	{
		acp_utils::modules::HidController::RegisterStateCallback(ControllerConnectedCB);

		JNIEnv* pEnv = NULL;
		acp_utils::ScopeGetEnv		st(pEnv);

		jstring	ControllerNameString = pEnv->NewStringUTF(UT_CONTROLLER_NAME);
        JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeControllerConnected)(pEnv, 0, ControllerNameString);
		//pEnv->DeleteLocalRef(ControllerNameString);//av TODO: check why caling DeleteLocalRef here crashes!
		JNI_FUNCTION(GLUtils_controller_NativeBridgeHIDControllers_NativeHandleInputEvents)(pEnv, 0, UT_CONTROLLER_EV_TYPE, UT_CONTROLLER_EV_VAL);
	}

	virtual void TearDown()
	{
	}
};

TEST_F(ControllerAPI_UT, SimplifiedControllerTest)
{
	EXPECT_STREQ(UT_CONTROLLER_NAME, acp_utils::modules::HidController::GetControllerName().c_str());
	EXPECT_EQ(UT_CONTROLLER_EV_TYPE, (int)test.evType);
	EXPECT_NEAR(UT_CONTROLLER_EV_VAL, test.Value, 0.000001);
}

#endif //ACP_UT