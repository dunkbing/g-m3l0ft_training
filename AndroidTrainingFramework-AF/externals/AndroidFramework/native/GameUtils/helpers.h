#ifndef __ACP_HELPERS_H__
#define __ACP_HELPERS_H__

#include <string>
#include <map>

//helper containers:

namespace acp_utils
{
	namespace helpers
	{
		enum AccountType
		{
			eAnon = 0,
			eFacebook,
			eGoogle,
		};

		enum Language
		{
			eEnglish = 0,
			eFrench,
			eDeutch,
			eItalian,
			eSpanish,
			eJapan,
			eKorean,
			eSimplifiedChinese,
			ePortugeuese_Brazil,
			eRussian,
			eTurkish,
			eArabic,
			eThai,
			eIndonesian,
			eVietnamese,
			eTraditionalChinese
		};
				
		enum BanType
		{
			eNoBan = -1,
			eFullBan = 0,
			eSocialBan,
			eJailbreakBan
		};

		enum ConnectionType
		{
			eNoConnectivity = 0,	//0
			eConnectivityWifi,		//1
			eConnectivityBlueTooth,	//2
			eConnectivityDummy,		//3
			eConnectivityEthernet,	//4
			eConnectivityWimax,		//5
			eConnectivity2g,		//6
			eConnectivity3g,		//7
			eConnectivity4g,		//8
			eConnectivityUnknown,	//9
		};

		enum EUserLocationStatus
		{
			eLocationUninitialized 	= -1,		//-1
			eLocationEnabled		= 0,		//0
			eLocationDisabled,					//1
			eLocationError	,					//2
		};

		struct UserLocation
		{
			EUserLocationStatus		status;
			double					latitude;
			double					longitude;
			float					accuracy;
			std::string				time;
		};
		
		enum NotificationTransportType
		{
			TRANS_UNKOWN = -1,
			TRANS_NONE = 0,
			TRANS_GCM, 
			TRANS_ADM,
			TRANS_NOKIA,
		};
		
		enum GAIDStatus
		{
			eNotAvailable = -2,			//-2
			eUninitialized = -1,		//-1
			eSuccessful	= 0,			//0
			eUserDisabled,				//1
			eGPSUnavailable,			//2
			eNotFinishedLoading,		//3
			eGPSAvailableWithError,		//4
			eAdIDDisabled,				//5
		};

		struct LocalPn
		{
			std::map<std::string, std::string>	pn_data;
			time_t								pn_schedule;
			int									pn_group;
		};
	
		struct SystemPath
		{
			std::string		sSdCard; //SdCard path. Shouldn't really be used by any game
			std::string		sObb;    //Obb path: Used to get the application path to the obb folder. Android/obb
			std::string		sData;   //Data path: Used to get the application path to the Data folder. Android/data
			std::string		sSave;   //Save path: Used to get the private application path. Android/data/data
			std::string		sTemp;   //Cache path: Used tp get the cache dir of your own app
		};
		
		struct SharedPreferenceContainer //or whatever name you might find more suited for this, mine sucks :)
		{
			std::string 	key;
			std::string 	pName;
		};
		
		struct HardwareIdentifiers
		{
			std::string 	sAndroidId;
			std::string		sSerial;
            std::string     sCpuSerial;
            std::string     sDeviceManufacturer;
            std::string     sDeviceModel;
			std::string     sBuildProduct;
			std::string     sBuildDevice;
			std::string		sFirmware;
			std::string		sWiFiMacAddress;
			std::string		sEthernetMacAddress;
			std::string		sImei;
			std::string		sHdidfv;
		};
		
		struct SoftwareIdentifiers
		{
            std::string     sCarrierName;
            std::string     sCountry;
            std::string     sDeviceLanguage;
			std::string 	sWebviewUserAgent;
			std::string		sApkPath;
		};

		struct DisplayInfo
		{
			float			xDpi;
			float			yDpi;
			int				nWidth;
			int				nHeight;
		};
		
		struct GameSpecificIdentifiers
		{
			std::string		sDefaultIgp;
			std::string		sInjectedIgp;
			std::string		sInjectedSerialKey;
			std::string		sGameName;
		};

        struct BatteryInfo
        {
            bool            sIsCharging;
            bool            sUsbCharge;
            bool            sACCharge;
            int             sBatteryStatus; ///< int between 0 and 100
        };
	};
};


#endif //__ACP_HELPERS_H__