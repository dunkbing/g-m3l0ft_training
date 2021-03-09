#include "config_Android.h"

#if ACP_UT

#include <pthread.h>

#include "../../ScopeGetEnv.h"
#include "../../helpers.h"
#include "../../PushNotification/simplified_pn.h"


#include "UnitTests_DeviceConfig.h"
#include "gtest/gtest.h"
#include "../logger.h"


void OnPushNotificationResponseCB(const std::string& notificationData){	LOG_INFO("notification data on OnPushNotificationResponseCB = %s", notificationData.c_str());}

class LocalPN_UT : public ::testing::Test
{
public:
	std::string			m_token;

	int					mGetTokenResponse;

	acp_utils::helpers::LocalPn		m_lLocalPNs[5];

protected:
	LocalPN_UT()
	{
		mGetTokenResponse = -1;
	}

	virtual ~LocalPN_UT()
	{
	}

	virtual void SetUp()
	{
		mGetTokenResponse = acp_utils::modules::SimplifiedPN::GetDeviceToken(acp_utils::helpers::TRANS_GCM, LocalPN_UT::AppDeviceTokenCB, this);
		
		m_lLocalPNs[0].pn_data["subject"]	= "PNTest1";
		m_lLocalPNs[0].pn_data["body"]		= "Test 1 Body";
		m_lLocalPNs[0].pn_data["type"]		= "launch";

		m_lLocalPNs[1].pn_data["subject"]	= "PNTest2";
		m_lLocalPNs[1].pn_data["body"]		= "Test 2 Body";
		m_lLocalPNs[1].pn_data["type"]		= "launch";

		m_lLocalPNs[2].pn_data["subject"]	= "PNTest3";
		m_lLocalPNs[2].pn_data["body"]		= "Test 3 Body";
		m_lLocalPNs[2].pn_data["type"]		= "launch";

		m_lLocalPNs[3].pn_data["subject"]	= "PNTest4";
		m_lLocalPNs[3].pn_data["body"]		= "Test 4 Body";
		m_lLocalPNs[3].pn_data["type"]		= "info";

		m_lLocalPNs[4].pn_data["subject"]	= "PNTest5";
		m_lLocalPNs[4].pn_data["body"]		= "Test 5 Body";
		m_lLocalPNs[4].pn_data["type"]		= "info";


		// Get current time in seconds
		time_t now;
		time(&now);
	 
		// Set time for 1 day
		time_t scheduled = now + 86400;

		for(int i = 0; i < 5; i++)
		{
			m_lLocalPNs[i].pn_schedule = scheduled + i;
		}

		m_lLocalPNs[0].pn_group = 0;
		m_lLocalPNs[1].pn_group = 1;
		m_lLocalPNs[2].pn_group = 0;
		m_lLocalPNs[3].pn_group = 2;
		m_lLocalPNs[4].pn_group = 2;

	}

	virtual void TearDown()
	{
	}

public:
	static void AppDeviceTokenCB(const std::string& notification_token, void* caller)
	{
		LOG_INFO("AppDeviceTokenCB = %s, caller = %x", notification_token.c_str(), caller);
		
		LocalPN_UT*	obj = static_cast<LocalPN_UT*>(caller);

		obj->m_token = notification_token;
	}
};

TEST_F(LocalPN_UT, SimplifiedPN_LocalNotificationTests)
{
	//av the following 2 tests are for GetDeviceToken function:

	//removed the mutex-lock system, as it would've caused a deadlock if no internet was present. 
	//added a sleep of 10 seconds, to get the PN callback
	EXPECT_TRUE(mGetTokenResponse == 0);

	if(m_token.empty())
	{
		sleep(10);
	}

	EXPECT_TRUE(m_token.size() > UT_MIN_TOKEN_SIZE);

	acp_utils::modules::SimplifiedPN::AllowOnlineNotifications(false);
	EXPECT_FALSE(acp_utils::modules::SimplifiedPN::AreOnlineNotificationsEnabled());

	std::string data = acp_utils::modules::SimplifiedPN::IsAppLaunchedFromPN();
	EXPECT_TRUE(data.empty());
	

	int result0 = acp_utils::modules::SimplifiedPN::SendMessage(m_lLocalPNs[0]);
	int result1 = acp_utils::modules::SimplifiedPN::SendMessage(m_lLocalPNs[1]);
	int result2 = acp_utils::modules::SimplifiedPN::SendMessage(m_lLocalPNs[2]);
	int result3 = acp_utils::modules::SimplifiedPN::SendMessage(m_lLocalPNs[3]);
	int result4 = acp_utils::modules::SimplifiedPN::SendMessage(m_lLocalPNs[4]);
		
	EXPECT_TRUE(result0 == 0);
	EXPECT_TRUE(result1 == 0);
	EXPECT_TRUE(result2 == 0);
	EXPECT_TRUE(result3 == 0);
	EXPECT_TRUE(result4 == 0);
	
	int del0 = acp_utils::modules::SimplifiedPN::DeleteMessageGroup(1);
	int del1 = acp_utils::modules::SimplifiedPN::DeleteMessageGroup(0);
	int del2 = acp_utils::modules::SimplifiedPN::DeleteMessageGroup(2);
	
	EXPECT_TRUE(acp_utils::modules::SimplifiedPN::DeleteMessageGroup(1) == 0);
	EXPECT_TRUE(acp_utils::modules::SimplifiedPN::DeleteMessageGroup(0) == 1);
	EXPECT_TRUE(acp_utils::modules::SimplifiedPN::DeleteMessageGroup(2) == 0);

	//av TODO: fake a launch from PN, and check IsAppLaunchedFromPN

}

#endif //ACP_UT
