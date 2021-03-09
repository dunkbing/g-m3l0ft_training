#include <jni.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <string>

#include "GameUtils\GameFunctionsToImplement.h"

#include "config_Android.h"

#if ACP_UT
#include "gtest/gtest.h"
#include "AndroidUnitTestLogPrinter.h"


bool onResumeCalled = false;
bool onSurfaceChangedCalled = false;

void* runTests(void *myself)
{
	AndroidUnitTestLogPrinter* alp = new AndroidUnitTestLogPrinter();
	alp->Init();

	/*while(!onResumeCalled || !onSurfaceChangedCalled)
	{
		sleep(10);
	}
	int result = RUN_ALL_TESTS();*/

	delete(alp);

}


int SimulateMain(int argc, char* argv[])
{
	// InitGoogleTest must be called befure we add ourselves as a listener.
	//configuring which tests to run. //av TODO read this from a file
	//::testing::GTEST_FLAG(filter) = "PackageAPI_UT.*";//http://stackoverflow.com/questions/14018434/how-to-specify-multiple-exclusion-filters-in-gtest-filter
	//::testing::GTEST_FLAG(list_tests) = true;
	::testing::InitGoogleTest(argc, argv);


	pthread_t _threadId;
	pthread_create(&_threadId, 0, runTests, 0);
}

#endif //ACP_UT

void OnGameResume()
{
#if ACP_UT
	onResumeCalled = true;
#endif //ACP_UT
}

void OnGamePause()
{
	
}

void OnGameStop()
{
}

void OnWindowStateChange(ANativeWindow* wnd)
{
#if ACP_UT
	if(wnd)
	{
		onSurfaceChangedCalled = true;
	}
#endif //ACP_UT
}

void OnGameTouchEvent(int, float, float, int)
{
	
}

void OnGameInit()
{
#if ACP_UT
	int argc = 1;
	std::vector<char*> argv;
	argv.resize(argc);

	argv[0] = "/sdcard/test";
	
	SimulateMain(&argc, &argv[0]);
#endif //ACP_UT
}

void NativeOnKeyAction(int, bool)
{
	// TODO
}

void NativeLowBattery()
{
	// TODO
}

void OnIGPClosed()
{
	// TODO
}

#ifndef ACP_UT
void OnPushNotificationResponseCB(const std::string& notificationData)
{
}
#endif //ACP_UT //moved the CB to the Unit Test area

void splashScreenFunc(const char* cs)
{
}

void OnIgpReward(int amount, char* type, char* message)
{
}

