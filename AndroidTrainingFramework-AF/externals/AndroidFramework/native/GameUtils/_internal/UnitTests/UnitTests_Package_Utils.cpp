#include <android/log.h>

#include "../../package_utils.h"


#if ACP_UT
#include "UnitTests_DeviceConfig.h"
#include "gtest/gtest.h"

class PackageAPI_UT : public ::testing::Test
{
public:

	acp_utils::helpers::ConnectionType		m_nConnectionStatus;

protected:
	PackageAPI_UT()
	{
	}

	virtual ~PackageAPI_UT()
	{
	}

	virtual void SetUp()//av TODO: read this from a config file located on SDCARD!
	{
		m_nConnectionStatus = acp_utils::helpers::eConnectivityWifi;
	}

	virtual void TearDown()
	{
	}
};

TEST_F(PackageAPI_UT, BaseAPI)
{	
	EXPECT_EQ(acp_utils::api::PackageUtils::GetConnectionType(), m_nConnectionStatus);

	EXPECT_STREQ(UT_SD_CARD, acp_utils::api::PackageUtils::GetSdCardPath().c_str());
	EXPECT_STREQ(UT_OBB_FOLDER, acp_utils::api::PackageUtils::GetObbFolderPath().c_str());
	EXPECT_STREQ(UT_DATA_FOLDER, acp_utils::api::PackageUtils::GetDataFolderPath().c_str());
	EXPECT_STREQ(UT_PRIVATE_FOLDER, acp_utils::api::PackageUtils::GetSaveFolderPath().c_str());
	EXPECT_STREQ(UT_CACHE_FOLDER, acp_utils::api::PackageUtils::GetCacheFolderPath().c_str());

	std::vector<char> tmp = acp_utils::api::PackageUtils::GetAssetResource("asset_test.bin");
	std::string str(tmp.begin(),tmp.end());

	EXPECT_STREQ("123_test", str.c_str());
	EXPECT_STREQ("6", acp_utils::api::PackageUtils::GetMetaDataValue("CHANNEL_ID").c_str());
	
	// Shared Preferences test
	acp_utils::helpers::SharedPreferenceContainer spc;
	spc.key   = UT_SHARED_PREF_KEY;
	spc.pName = UT_SHARED_PREF_PNAME;
	
	acp_utils::api::PackageUtils::SavePreferenceInt(spc, UT_SHARED_PREF_INT);
	EXPECT_EQ(UT_SHARED_PREF_INT, acp_utils::api::PackageUtils::ReadSharedPreferenceInt(spc, 0));
	acp_utils::api::PackageUtils::SavePreferenceLong(spc, UT_SHARED_PREF_LONG);
	EXPECT_EQ(UT_SHARED_PREF_LONG, acp_utils::api::PackageUtils::ReadSharedPreferenceLong(spc, 0));
	acp_utils::api::PackageUtils::SavePreferenceBool(spc, UT_SHARED_PREF_BOOL);
	EXPECT_TRUE(UT_SHARED_PREF_BOOL == acp_utils::api::PackageUtils::ReadSharedPreferenceBool(spc, false));
	acp_utils::api::PackageUtils::SavePreferenceString(spc, UT_SHARED_PREF_STRING);
	EXPECT_STREQ(UT_SHARED_PREF_STRING, acp_utils::api::PackageUtils::ReadSharedPreferenceString(spc, "noString").c_str());
	
#if DEVICE_SPECIFIC_UT
	// Game Specific Identifier
	EXPECT_STREQ(UT_DEFAULT_IGP,	 acp_utils::api::PackageUtils::GetDefaultIGP().c_str());
	EXPECT_STREQ(UT_GAME_NAME,  	 acp_utils::api::PackageUtils::GetGameName().c_str());
	EXPECT_STREQ(UT_INJECTED_IGP,    acp_utils::api::PackageUtils::GetInjectedIGP().c_str());
	EXPECT_STREQ(UT_INJECTED_SERIAL, acp_utils::api::PackageUtils::GetInjectedSerialKey().c_str());
	
	// Hardware Identifiers
	EXPECT_STREQ(UT_ANDROIDID, 			 acp_utils::api::PackageUtils::GetAndroidId().c_str());
	EXPECT_STREQ(UT_SERIAL_NO, 			 acp_utils::api::PackageUtils::GetSerial().c_str());
	EXPECT_STREQ(UT_CPU_SERIAL, 		 acp_utils::api::PackageUtils::GetCPUSerial().c_str());
	EXPECT_STREQ(UT_DEVICE_MANUFACTURER, acp_utils::api::PackageUtils::GetDeviceManufacturer().c_str());
	EXPECT_STREQ(UT_DEVICE_MODEL,        acp_utils::api::PackageUtils::GetDeviceModel().c_str());
	EXPECT_STREQ(UT_FIRMWARE,   		 acp_utils::api::PackageUtils::GetFirmware().c_str());
	EXPECT_STREQ(UT_MACADDRESS,			 acp_utils::api::PackageUtils::GetMacAddress().c_str());
	EXPECT_STREQ(UT_IMEI,       		 acp_utils::api::PackageUtils::GetIMEI().c_str());
	EXPECT_STREQ(UT_HDIDFV,     		 acp_utils::api::PackageUtils::GetHDIDFVStr().c_str());
	EXPECT_STREQ(UT_BUILD_PRODUCT,     	 acp_utils::api::PackageUtils::GetBuildProduct().c_str());
	EXPECT_STREQ(UT_BUILD_DEVICE,     	 acp_utils::api::PackageUtils::GetBuildDevice().c_str());
	EXPECT_NEAR(UT_HEIGHT_INCH, 		 acp_utils::api::PackageUtils::GetHeightInInch(), 0.1f);
	EXPECT_NEAR(UT_WIDTH_INCH, 			 acp_utils::api::PackageUtils::GetWidthInInch(), 0.1f);
	EXPECT_EQ(UT_HEIGHT_PX,				 acp_utils::api::PackageUtils::GetHeight());
	EXPECT_EQ(UT_WIDTH_PX, 			 	 acp_utils::api::PackageUtils::GetWidth());
	
	// Software Identifiers
	EXPECT_STREQ(UT_CARRIER_NAME,        acp_utils::api::PackageUtils::GetCarrierName().c_str());
	EXPECT_STREQ(UT_COUNTRY,			 acp_utils::api::PackageUtils::GetCountry().c_str());
	EXPECT_STREQ(UT_LANGUAGE,            acp_utils::api::PackageUtils::GetDeviceLanguage().c_str());
	EXPECT_STREQ(UT_USER_AGENT, 		 acp_utils::api::PackageUtils::GetWebviewUserAgent().c_str());

	
	if(acp_utils::api::PackageUtils::GetGoogleAdIdStatus() == acp_utils::helpers::eNotFinishedLoading)
	{
		sleep(2);
	}

	EXPECT_STREQ(UT_GOOGLE_ADID, 		 acp_utils::api::PackageUtils::GetGoogleAdId().c_str());
	EXPECT_EQ(UT_GOOGLE_ADID_STATUS, 	 acp_utils::api::PackageUtils::GetGoogleAdIdStatus());

	//application stats:
	unsigned long long disk_space = acp_utils::api::PackageUtils::GetDiskFreeSpace();
	EXPECT_TRUE(disk_space > 0 && disk_space < UT_DISK_FREE_SPACE_THREASHOLD);

	//EXPECT_NEAR(UT_DISK_FREE_SPACE_THREASHOLD, disk_space, 150000.0f);

	EXPECT_EQ(UT_NUMBER_OF_CORES, acp_utils::api::PackageUtils::GetNumberOfCpuCores());

	EXPECT_TRUE(acp_utils::api::PackageUtils::GetCurrentCpuSpeedInHz() > 0 && acp_utils::api::PackageUtils::GetCurrentCpuSpeedInHz() <= acp_utils::api::PackageUtils::GetMaxCpuSpeedInHz());
	EXPECT_EQ(UT_MAX_CPU_SPEED, acp_utils::api::PackageUtils::GetMaxCpuSpeedInHz());
	EXPECT_STREQ(acp_utils::api::PackageUtils::GetDeviceChipset().c_str(), UT_DEVICE_CHIPSET);
	EXPECT_STREQ(acp_utils::api::PackageUtils::GetDeviceArchitecture().c_str(), UT_DEVICE_ARCH);
	EXPECT_STREQ(acp_utils::api::PackageUtils::GetDeviceMicroArch().c_str(), UT_DEVICE_MICRO_ARCH);

	EXPECT_TRUE(acp_utils::api::PackageUtils::GetCurrentAvailableRamInMegaBytes() > UT_AVAILABLE_RAM_MIN && acp_utils::api::PackageUtils::GetCurrentAvailableRamInMegaBytes() < acp_utils::api::PackageUtils::GetMaxAvailableRamInMegaBytes());
	EXPECT_NEAR(acp_utils::api::PackageUtils::GetMaxAvailableRamInMegaBytes(), UT_MAX_RAM, 1.0f);
	
	// Connection
	EXPECT_TRUE(acp_utils::api::PackageUtils::GetConnectionType() == acp_utils::helpers::eConnectivityWifi);

	// Battery
	EXPECT_TRUE(acp_utils::api::PackageUtils::GetIsBatteryCharging());
	EXPECT_TRUE(acp_utils::api::PackageUtils::GetIsUsbCharging());
	EXPECT_FALSE(acp_utils::api::PackageUtils::GetIsACCharging());
	EXPECT_GT(acp_utils::api::PackageUtils::GetBatteryStatus(), UT_BATTERY_THRESHOLD);

#endif //DEVICE_SPECIFIC_UT

	EXPECT_TRUE(acp_utils::api::PackageUtils::IsAppEnc());
	
	EXPECT_TRUE(acp_utils::api::PackageUtils::IsVideoCompleted());

	acp_utils::api::PackageUtils::SetKeepScreenOn(true);
}

#endif //ACP_UT
